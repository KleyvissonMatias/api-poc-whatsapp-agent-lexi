package com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.usecase

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.dto.InboundWebhookRequest
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.exception.InvalidWebhookRequestException
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.port.MessageQueuePort
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.aggregate.MessageAggregate
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.entity.InboundMessage
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.port.MessageRepository
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.MessageContent
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.SenderId

class ProcessInboundMessageUseCase(
    private val messageRepository: MessageRepository,
    private val messageQueuePort: MessageQueuePort
) {
    suspend fun execute(request: InboundWebhookRequest): MessageAggregate {
        if (request.senderId.isBlank()) {
            throw InvalidWebhookRequestException("sender_id cannot be blank")
        }
        if (request.message.isBlank()) {
            throw InvalidWebhookRequestException("message cannot be blank")
        }
        if (request.message.length > MessageContent.MAX_TEXT_LENGTH) {
            throw InvalidWebhookRequestException("message cannot exceed ${MessageContent.MAX_TEXT_LENGTH} characters")
        }

        val inboundMessage = InboundMessage.create(
            senderId = SenderId(request.senderId),
            content = MessageContent(request.message),
            metadata = request.metadata ?: emptyMap()
        )

        val aggregate = MessageAggregate.createNew(inboundMessage)
        val savedAggregate = messageRepository.save(aggregate)

        messageQueuePort.enqueue(savedAggregate.messageJob.jobId)

        return savedAggregate
    }
}