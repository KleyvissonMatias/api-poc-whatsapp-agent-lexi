package com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.port

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.entity.OutboundMessage

interface OutboundMessagingPort {
    suspend fun sendMessage(message: OutboundMessage): Boolean
}