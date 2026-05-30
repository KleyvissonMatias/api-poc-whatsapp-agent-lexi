package com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.entity

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.MessageContent
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.SenderId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant

class InboundMessageTest {

    @Test
    fun `should create inbound message with generated id and current timestamp`() {
        val before = Instant.now()
        val message = InboundMessage.create(
            senderId = SenderId("5511999999999"),
            content = MessageContent("Hello!")
        )
        val after = Instant.now()

        assertNotNull(message.messageId)
        assertEquals("5511999999999", message.senderId.value)
        assertEquals("Hello!", message.content.text)
        assertTrue(message.receivedAt >= before)
        assertTrue(message.receivedAt <= after)
        assertTrue(message.metadata.isEmpty())
    }

    @Test
    fun `should create inbound message with metadata`() {
        val metadata = mapOf("source" to "whatsapp", "version" to "1")
        val message = InboundMessage.create(
            senderId = SenderId("5511999999999"),
            content = MessageContent("Hi!"),
            metadata = metadata
        )
        assertEquals(metadata, message.metadata)
    }

    @Test
    fun `each created message should have a unique id`() {
        val msg1 = InboundMessage.create(SenderId("111"), MessageContent("A"))
        val msg2 = InboundMessage.create(SenderId("111"), MessageContent("A"))
        assertTrue(msg1.messageId != msg2.messageId)
    }
}