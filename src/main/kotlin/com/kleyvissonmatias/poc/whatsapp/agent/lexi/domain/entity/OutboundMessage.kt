package com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.entity

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.MessageContent
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.MessageId
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.SenderId
import java.time.Instant

data class OutboundMessage(
    val messageId: MessageId,
    val recipientId: SenderId,
    val content: MessageContent,
    val correlatedWithMessageId: MessageId,
    val sentAt: Instant,
    val metadata: Map<String, String> = emptyMap()
) {
    companion object {
        fun create(
            recipientId: SenderId,
            content: MessageContent,
            correlatedWithMessageId: MessageId,
            metadata: Map<String, String> = emptyMap()
        ): OutboundMessage {
            return OutboundMessage(
                messageId = MessageId.generate(),
                recipientId = recipientId,
                content = content,
                correlatedWithMessageId = correlatedWithMessageId,
                sentAt = Instant.now(),
                metadata = metadata
            )
        }
    }
}