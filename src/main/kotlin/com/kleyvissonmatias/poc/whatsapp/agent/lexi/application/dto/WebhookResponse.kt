package com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class WebhookResponse(
    @JsonProperty("status")
    val status: String,

    @JsonProperty("message_id")
    val messageId: String,

    @JsonProperty("timestamp")
    val timestamp: String
) {
    companion object {
        const val STATUS_QUEUED = "queued"
    }
}