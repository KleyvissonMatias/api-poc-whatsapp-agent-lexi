package com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.exception.InvalidWebhookRequestException

data class InboundWebhookRequest(
    @JsonProperty("sender_id")
    val senderId: String,

    @JsonProperty("message")
    val message: String,

    @JsonProperty("metadata")
    val metadata: Map<String, String>? = null
) {
    fun validate() {
        if (senderId.isBlank()) {
            throw InvalidWebhookRequestException("sender_id cannot be blank")
        }
        if (message.isBlank()) {
            throw InvalidWebhookRequestException("message cannot be blank")
        }
        if (message.length > MAX_MESSAGE_LENGTH) {
            throw InvalidWebhookRequestException("message cannot exceed $MAX_MESSAGE_LENGTH characters")
        }
    }

    companion object {
        private const val MAX_MESSAGE_LENGTH = 4096
    }
}