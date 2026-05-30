package com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.usecase

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.dto.InboundWebhookRequest
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.exception.InvalidWebhookRequestException
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.port.MessageQueuePort
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.aggregate.MessageAggregate
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.port.MessageRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*

class ProcessInboundMessageUseCaseTest {

    private val messageRepository = mock(MessageRepository::class.java)
    private val messageQueuePort = mock(MessageQueuePort::class.java)
    private val useCase = ProcessInboundMessageUseCase(messageRepository, messageQueuePort)

    @Test
    fun `should process and enqueue inbound message`() = runBlocking {
        val request = InboundWebhookRequest(
            senderId = "5511999999999",
            message = "Hello Lexi"
        )

        `when`(messageRepository.save(any(MessageAggregate::class.java))).thenAnswer { it.arguments[0] as MessageAggregate }
        `when`(messageQueuePort.enqueue(any())).thenReturn(true)

        val result = useCase.execute(request)

        assertNotNull(result)
        assertEquals("5511999999999", result.inboundMessage.senderId.value)
        assertEquals("Hello Lexi", result.inboundMessage.content.text)

        verify(messageRepository).save(any(MessageAggregate::class.java))
        verify(messageQueuePort).enqueue(result.messageJob.jobId)
    }

    @Test
    fun `should throw when sender_id is blank`() = runBlocking<Unit> {
        val request = InboundWebhookRequest(senderId = "", message = "Hello")
        assertThrows<InvalidWebhookRequestException> { useCase.execute(request) }
    }

    @Test
    fun `should throw when message is blank`() = runBlocking<Unit> {
        val request = InboundWebhookRequest(senderId = "5511999999999", message = "")
        assertThrows<InvalidWebhookRequestException> { useCase.execute(request) }
    }
}