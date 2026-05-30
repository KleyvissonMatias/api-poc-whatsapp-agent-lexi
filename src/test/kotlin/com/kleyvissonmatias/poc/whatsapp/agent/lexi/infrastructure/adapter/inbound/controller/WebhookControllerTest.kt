package com.kleyvissonmatias.poc.whatsapp.agent.lexi.infrastructure.adapter.inbound.controller

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.dto.InboundWebhookRequest
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.dto.WebhookResponse
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.port.LlmPort
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.port.MessageRepository
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.MessageId
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.ProcessingStatus
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.infrastructure.adapter.outbound.service.SimulatedLlmAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(WebhookControllerTest.TestConfig::class)
class WebhookControllerTest {

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun llmPort(): LlmPort = SimulatedLlmAdapter(failureRate = 0.0f, delayMs = 100L)
    }

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var messageRepository: MessageRepository

    @Test
    fun `should accept message and return 202 and queue it asynchronously`() {
        val request = InboundWebhookRequest(
            senderId = "5511999999999",
            message = "Hello Lexi!"
        )

        val response = webTestClient.post()
            .uri("/api/v1/webhook/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isAccepted
            .expectBody(WebhookResponse::class.java)
            .returnResult().responseBody!!

        assertEquals("queued", response.status)
        assertNotNull(response.messageId)
        assertNotNull(response.timestamp)

        runBlocking {
            val messageId = MessageId.of(response.messageId)
            var finalStatus = ProcessingStatus.PENDING

            for (i in 1..20) {
                val aggregate = messageRepository.findById(messageId)
                if (aggregate != null) {
                    finalStatus = aggregate.messageJob.status
                    if (finalStatus == ProcessingStatus.COMPLETED || finalStatus == ProcessingStatus.FAILED) break
                }
                delay(250)
            }

            assertEquals(ProcessingStatus.COMPLETED, finalStatus)

            val processedAggregate = messageRepository.findById(messageId)
            assertTrue(processedAggregate!!.isProcessed())
            assertNotNull(processedAggregate.outboundMessage)
            assertEquals("Response to: Hello Lexi!", processedAggregate.outboundMessage?.content?.toString())
        }
    }

    @Test
    fun `should return 400 for invalid request with blank sender`() {
        val request = InboundWebhookRequest(senderId = "", message = "Hello")

        webTestClient.post()
            .uri("/api/v1/webhook/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `should return 400 for invalid request with blank message`() {
        val request = InboundWebhookRequest(senderId = "5511999999999", message = "")

        webTestClient.post()
            .uri("/api/v1/webhook/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
    }
}