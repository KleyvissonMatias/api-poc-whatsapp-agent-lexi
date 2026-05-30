package com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject

data class SenderId(
    val value: String
) {
    init {
        require(value.isNotBlank()) { "SenderId cannot be blank" }
    }

    override fun toString(): String = value
}
