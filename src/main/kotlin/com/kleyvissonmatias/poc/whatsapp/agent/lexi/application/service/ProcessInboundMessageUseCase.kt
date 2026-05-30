package com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.service

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.dto.InboundWebhookRequest
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.port.MessageQueuePort
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.aggregate.MessageAggregate
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.entity.InboundMessage
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.exception.InvalidMessageException
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.exception.InvalidSenderException
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.repository.MessageRepository
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.MessageContent
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.SenderId

class ProcessInboundMessageUseCase(
    private val messageRepository: MessageRepository,
    private val messageQueuePort: MessageQueuePort
) {
    suspend fun execute(request: InboundWebhookRequest): MessageAggregate {
        request.validate()

        val senderId = try {
            SenderId(request.senderId)
        } catch (ex: IllegalArgumentException) {
            throw InvalidSenderException(ex.message ?: "Invalid sender ID")
        }

        val messageContent = try {
            MessageContent(request.message)
        } catch (ex: IllegalArgumentException) {
            throw InvalidMessageException(ex.message ?: "Invalid message content")
        }

        val inboundMessage = InboundMessage.create(
            senderId = senderId,
            content = messageContent,
            metadata = request.metadata ?: emptyMap()
        )

        val aggregate = MessageAggregate.createNew(inboundMessage)
        val savedAggregate = messageRepository.save(aggregate)

        messageQueuePort.enqueue(savedAggregate.messageJob.jobId)

        return savedAggregate
    }
}