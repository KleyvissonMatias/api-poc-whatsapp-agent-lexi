package com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.entity

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.MessageContent
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.MessageId
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.SenderId
import java.time.Instant

data class InboundMessage(
    val messageId: MessageId,
    val senderId: SenderId,
    val content: MessageContent,
    val receivedAt: Instant,
    val metadata: Map<String, String> = emptyMap()
) {
    companion object {
        fun create(
            senderId: SenderId,
            content: MessageContent,
            metadata: Map<String, String> = emptyMap()
        ): InboundMessage {
            return InboundMessage(
                messageId = MessageId.generate(),
                senderId = senderId,
                content = content,
                receivedAt = Instant.now(),
                metadata = metadata
            )
        }
    }
}