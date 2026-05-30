package com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class ErrorResponse(
    @JsonProperty("error")
    val error: String,

    @JsonProperty("message")
    val message: String,

    @JsonProperty("timestamp")
    val timestamp: String = Instant.now().toString(),

    @JsonProperty("path")
    val path: String? = null
)