package com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.exception

sealed class DomainException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause)

class InvalidMessageException(message: String) : DomainException(message)

class InvalidSenderException(message: String) : DomainException(message)

class JobNotFoundException(message: String) : DomainException(message)