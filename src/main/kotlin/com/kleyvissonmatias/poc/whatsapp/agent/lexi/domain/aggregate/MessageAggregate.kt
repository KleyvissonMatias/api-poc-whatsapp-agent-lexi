package com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.aggregate

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.entity.InboundMessage
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.entity.MessageJob
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.entity.OutboundMessage
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.MessageId

data class MessageAggregate(
    val inboundMessage: InboundMessage,
    val messageJob: MessageJob,
    val outboundMessage: OutboundMessage? = null
) {
    fun getMessageId(): MessageId = inboundMessage.messageId

    fun isProcessed(): Boolean = outboundMessage != null

    companion object {
        fun createNew(inboundMessage: InboundMessage): MessageAggregate {
            val job = MessageJob.create(inboundMessage.messageId)
            return MessageAggregate(
                inboundMessage = inboundMessage,
                messageJob = job
            )
        }
    }
}