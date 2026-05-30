package com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject

import java.util.*

@ConsistentCopyVisibility
data class MessageId private constructor(val value: String) {
    init {
        require(value.isNotBlank()) { "MessageId cannot be blank" }
    }

    override fun toString(): String = value

    companion object {
        fun generate(): MessageId = MessageId(UUID.randomUUID().toString())
        fun of(value: String): MessageId = MessageId(value)
    }
}