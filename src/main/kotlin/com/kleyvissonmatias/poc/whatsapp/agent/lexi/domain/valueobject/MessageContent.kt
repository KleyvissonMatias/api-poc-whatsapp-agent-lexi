package com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject

data class MessageContent(
    val text: String
) {
    init {
        require(text.isNotBlank()) { "Message text cannot be blank" }
        require(text.length <= MAX_TEXT_LENGTH) { "Message text cannot exceed $MAX_TEXT_LENGTH characters" }
    }

    override fun toString(): String = text

    companion object {
        const val MAX_TEXT_LENGTH = 4096
    }
}
