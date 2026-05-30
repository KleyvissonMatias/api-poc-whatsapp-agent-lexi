package com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class InboundWebhookRequest(
    @JsonProperty("sender_id")
    val senderId: String,

    @JsonProperty("message")
    val message: String,

    @JsonProperty("metadata")
    val metadata: Map<String, String>? = null
)