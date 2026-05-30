package com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MessageContentTest {

    @Test
    fun `should create valid message content`() {
        val content = MessageContent("Hello, world!")
        assertEquals("Hello, world!", content.text)
    }

    @Test
    fun `should throw when text is blank`() {
        assertThrows<IllegalArgumentException> { MessageContent("") }
        assertThrows<IllegalArgumentException> { MessageContent("   ") }
    }

    @Test
    fun `should throw when text exceeds max length`() {
        val oversized = "a".repeat(MessageContent.MAX_TEXT_LENGTH + 1)
        assertThrows<IllegalArgumentException> { MessageContent(oversized) }
    }

    @Test
    fun `should accept text at max length boundary`() {
        val atLimit = "a".repeat(MessageContent.MAX_TEXT_LENGTH)
        val content = MessageContent(atLimit)
        assertEquals(MessageContent.MAX_TEXT_LENGTH, content.text.length)
    }

    @Test
    fun `toString should return the text`() {
        val content = MessageContent("test message")
        assertEquals("test message", content.toString())
    }
}
