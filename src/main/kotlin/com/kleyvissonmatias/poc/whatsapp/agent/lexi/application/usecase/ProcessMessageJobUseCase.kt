package com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.usecase

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.entity.OutboundMessage
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.exception.JobNotFoundException
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.port.LlmPort
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.port.MessageRepository
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.port.OutboundMessagingPort
import org.slf4j.LoggerFactory

class ProcessMessageJobUseCase(
    private val messageRepository: MessageRepository,
    private val llmPort: LlmPort,
    private val outboundMessagingPort: OutboundMessagingPort
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun execute(jobId: String): Boolean {
        val aggregate = messageRepository.findByJobId(jobId)
            ?: throw JobNotFoundException("Job not found: $jobId")

        val processingAggregate = aggregate.copy(messageJob = aggregate.messageJob.markAsProcessing())
        messageRepository.save(processingAggregate)

        return try {
            val llmResponse = llmPort.generateResponse(processingAggregate.inboundMessage.content)

            val outboundMessage = OutboundMessage.create(
                recipientId = processingAggregate.inboundMessage.senderId,
                content = llmResponse,
                correlatedWithMessageId = processingAggregate.inboundMessage.messageId
            )

            val sent = outboundMessagingPort.sendMessage(outboundMessage)
            messageRepository.save(
                processingAggregate.copy(
                    messageJob = processingAggregate.messageJob.markAsCompleted(),
                    outboundMessage = outboundMessage
                )
            )
            sent
        } catch (ex: Exception) {
            logger.error("Failed to process job $jobId: ${ex.message}", ex)
            messageRepository.save(
                processingAggregate.copy(
                    messageJob = processingAggregate.messageJob.markAsFailed(ex.message ?: "Processing failed")
                )
            )
            false
        }
    }
}