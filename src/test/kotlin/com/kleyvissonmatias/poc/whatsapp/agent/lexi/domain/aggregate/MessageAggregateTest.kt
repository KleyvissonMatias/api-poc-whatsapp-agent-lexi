package com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.aggregate

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.entity.InboundMessage
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.entity.OutboundMessage
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.MessageContent
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.ProcessingStatus
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.SenderId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MessageAggregateTest {

    private fun buildInbound() = InboundMessage.create(
        senderId = SenderId("5511999999999"),
        content = MessageContent("Hello")
    )

    @Test
    fun `should create new aggregate with pending job`() {
        val inboundMessage = buildInbound()
        val aggregate = MessageAggregate.createNew(inboundMessage)

        assertEquals(inboundMessage, aggregate.inboundMessage)
        assertEquals(inboundMessage.messageId, aggregate.messageJob.messageId)
        assertEquals(ProcessingStatus.PENDING, aggregate.messageJob.status)
        assertFalse(aggregate.isProcessed())
    }

    @Test
    fun `isProcessed should return false when outbound message is absent`() {
        val aggregate = MessageAggregate.createNew(buildInbound())
        assertFalse(aggregate.isProcessed())
    }

    @Test
    fun `isProcessed should return true when outbound message is present`() {
        val inboundMessage = buildInbound()
        val aggregate = MessageAggregate.createNew(inboundMessage)

        val outboundMessage = OutboundMessage.create(
            recipientId = inboundMessage.senderId,
            content = MessageContent("Reply"),
            correlatedWithMessageId = inboundMessage.messageId
        )
        val processedAggregate = aggregate.copy(outboundMessage = outboundMessage)

        assertTrue(processedAggregate.isProcessed())
        assertNotNull(processedAggregate.outboundMessage)
    }

    @Test
    fun `getMessageId should return inbound message id`() {
        val inboundMessage = buildInbound()
        val aggregate = MessageAggregate.createNew(inboundMessage)

        assertEquals(inboundMessage.messageId, aggregate.getMessageId())
    }
}