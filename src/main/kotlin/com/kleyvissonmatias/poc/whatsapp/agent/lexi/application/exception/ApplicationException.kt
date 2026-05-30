package com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.exception

sealed class ApplicationException(
    override val message: String,
    val statusCode: Int,
    override val cause: Throwable? = null
) : Exception(message, cause)

class InvalidWebhookRequestException(message: String) : ApplicationException(message, 400)

class LlmServiceException(message: String, cause: Throwable? = null) : ApplicationException(message, 503, cause)