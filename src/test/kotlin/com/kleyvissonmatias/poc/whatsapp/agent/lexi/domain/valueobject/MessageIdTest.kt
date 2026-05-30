package com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MessageIdTest {

    @Test
    fun `should create message id with given value`() {
        val id = MessageId.of("abc-123")
        assertEquals("abc-123", id.value)
    }

    @Test
    fun `should throw when value is blank`() {
        assertThrows<IllegalArgumentException> { MessageId.of("") }
        assertThrows<IllegalArgumentException> { MessageId.of("   ") }
    }

    @Test
    fun `generate should produce unique ids`() {
        val id1 = MessageId.generate()
        val id2 = MessageId.generate()
        assertNotEquals(id1, id2)
    }

    @Test
    fun `toString should return the value`() {
        val id = MessageId.of("my-id")
        assertEquals("my-id", id.toString())
    }
}