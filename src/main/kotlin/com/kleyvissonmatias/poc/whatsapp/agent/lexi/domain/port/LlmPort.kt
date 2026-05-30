package com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.port

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.MessageContent

interface LlmPort {
    suspend fun generateResponse(userMessage: MessageContent): MessageContent
}