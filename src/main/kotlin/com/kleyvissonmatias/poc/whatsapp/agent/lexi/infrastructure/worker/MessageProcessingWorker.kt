package com.kleyvissonmatias.poc.whatsapp.agent.lexi.infrastructure.worker

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.port.MessageQueuePort
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.usecase.ProcessMessageJobUseCase
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean

class MessageProcessingWorker(
    private val messageQueuePort: MessageQueuePort,
    private val processMessageJobUseCase: ProcessMessageJobUseCase
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val isRunning = AtomicBoolean(false)
    private val workerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start() {
        if (isRunning.compareAndSet(false, true)) {
            logger.info("Worker: Starting message processing")
            workerScope.launch {
                processMessages()
            }
        }
    }

    fun stop() {
        if (isRunning.compareAndSet(true, false)) {
            logger.info("Worker: Stopping message processing")
            workerScope.cancel()
        }
    }

    private suspend fun processMessages() {
        while (currentCoroutineContext().isActive && isRunning.get()) {
            try {
                val jobId = messageQueuePort.dequeue()

                if (jobId != null) {
                    logger.info("Worker: Processing job - jobId=$jobId")
                    try {
                        processMessageJobUseCase.execute(jobId)
                        logger.info("Worker: Job processed successfully - jobId=$jobId")
                    } catch (ex: Exception) {
                        logger.error("Worker: Error processing job - jobId=$jobId", ex)
                    }
                } else {
                    delay(EMPTY_QUEUE_DELAY_MS)
                }
            } catch (ex: Exception) {
                logger.error("Worker: Error in processing loop", ex)
                delay(ERROR_RETRY_DELAY_MS)
            }
        }
    }

    companion object {
        private const val EMPTY_QUEUE_DELAY_MS = 1000L
        private const val ERROR_RETRY_DELAY_MS = 5000L
    }
}