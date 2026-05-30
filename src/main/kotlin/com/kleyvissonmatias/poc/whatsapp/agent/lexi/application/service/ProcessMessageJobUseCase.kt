package com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.service

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.entity.OutboundMessage
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.exception.JobNotFoundException
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.port.LlmPort
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.port.OutboundMessagingPort
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.repository.MessageRepository
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

        messageRepository.save(aggregate.copy(messageJob = aggregate.messageJob.markAsProcessing()))

        return try {
            val llmResponse = llmPort.generateResponse(aggregate.inboundMessage.content)

            val outboundMessage = OutboundMessage.create(
                recipientId = aggregate.inboundMessage.senderId,
                content = llmResponse,
                correlatedWithMessageId = aggregate.inboundMessage.messageId
            )

            outboundMessagingPort.sendMessage(outboundMessage).also {
                messageRepository.save(
                    aggregate.copy(
                        messageJob = aggregate.messageJob.markAsCompleted(),
                        outboundMessage = outboundMessage
                    )
                )
            }
        } catch (ex: Exception) {
            logger.error("Failed to process job $jobId: ${ex.message}", ex)
            messageRepository.save(
                aggregate.copy(messageJob = aggregate.messageJob.markAsFailed(ex.message ?: "Processing failed"))
            )
            false
        }
    }
}