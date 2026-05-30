package com.kleyvissonmatias.poc.whatsapp.agent.lexi.infrastructure.adapter.outbound.service

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.exception.LlmServiceException
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.port.LlmPort
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.MessageContent
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import kotlin.random.Random

class FakeLlmService(
    private val failureRate: Float = DEFAULT_FAILURE_RATE
) : LlmPort {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun generateResponse(userMessage: MessageContent): MessageContent {
        logger.info("LLM: Processing message (length=${userMessage.text.length})")
        delay(LLM_DELAY_MS)

        if (Random.nextFloat() < failureRate) {
            logger.warn("LLM: Simulated transient failure triggered")
            throw LlmServiceException("Simulated LLM transient failure")
        }

        logger.info("LLM: Processing completed")
        return MessageContent("Response to: ${userMessage.text}")
    }

    companion object {
        const val DEFAULT_FAILURE_RATE = 0.3f
        private const val LLM_DELAY_MS = 10_000L
    }
}