package com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SenderIdTest {

    @Test
    fun `should create sender id with valid value`() {
        val senderId = SenderId("5511999999999")
        assertEquals("5511999999999", senderId.value)
    }

    @Test
    fun `should throw when value is blank`() {
        assertThrows<IllegalArgumentException> { SenderId("") }
        assertThrows<IllegalArgumentException> { SenderId("   ") }
    }

    @Test
    fun `toString should return the value`() {
        val senderId = SenderId("5511999999999")
        assertEquals("5511999999999", senderId.toString())
    }
}
