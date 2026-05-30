package com.kleyvissonmatias.poc.whatsapp.agent.lexi.infrastructure.adapter.outbound.queue

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.port.MessageQueuePort
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentLinkedQueue

class InMemoryMessageQueue : MessageQueuePort {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val queue: ConcurrentLinkedQueue<String> = ConcurrentLinkedQueue()

    override suspend fun enqueue(jobId: String): Boolean {
        queue.add(jobId)
        logger.info("Queue: Job enqueued - jobId=$jobId (size: ${queue.size})")
        return true
    }

    override suspend fun dequeue(): String? {
        return try {
            val jobId = queue.poll()
            if (jobId != null) {
                logger.info("Queue: Job dequeued - jobId=$jobId (size: ${queue.size})")
            }
            jobId
        } catch (ex: Exception) {
            logger.error("Queue: Error dequeuing job", ex)
            null
        }
    }

    override fun getQueueSize(): Int = queue.size
}