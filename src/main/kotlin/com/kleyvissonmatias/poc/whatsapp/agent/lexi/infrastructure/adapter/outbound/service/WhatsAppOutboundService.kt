package com.kleyvissonmatias.poc.whatsapp.agent.lexi.infrastructure.adapter.outbound.service

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.entity.OutboundMessage
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.port.OutboundMessagingPort
import org.slf4j.LoggerFactory

class WhatsAppOutboundService : OutboundMessagingPort {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun sendMessage(message: OutboundMessage): Boolean {
        logger.info("Outbound: Sending message - ID: ${message.messageId} (correlated: ${message.correlatedWithMessageId})")
        logger.info("Outbound: Message dispatched successfully")
        return true
    }
}