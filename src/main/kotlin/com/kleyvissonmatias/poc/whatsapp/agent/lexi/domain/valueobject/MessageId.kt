package com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject

import java.util.*

data class MessageId(
    val value: String = UUID.randomUUID().toString()
) {
    init {
        require(value.isNotBlank()) { "MessageId cannot be blank" }
    }

    override fun toString(): String = value

    companion object {
        fun generate(): MessageId = MessageId()
    }
}
