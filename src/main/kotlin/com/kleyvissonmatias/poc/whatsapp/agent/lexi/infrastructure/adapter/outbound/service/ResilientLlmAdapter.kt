package com.kleyvissonmatias.poc.whatsapp.agent.lexi.infrastructure.adapter.outbound.service

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.exception.LlmServiceException
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.port.LlmPort
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.MessageContent
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class ResilientLlmAdapter(
    private val delegate: LlmPort,
    private val circuitBreaker: CircuitBreaker
) : LlmPort {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun generateResponse(userMessage: MessageContent): MessageContent {
        if (!circuitBreaker.tryAcquirePermission()) {
            logger.warn("CircuitBreaker: Call rejected - state=${circuitBreaker.state}")
            throw LlmServiceException("LLM service temporarily unavailable (circuit breaker OPEN)")
        }

        val startNanos = System.nanoTime()
        return try {
            val result = delegate.generateResponse(userMessage)
            circuitBreaker.onSuccess(System.nanoTime() - startNanos, TimeUnit.NANOSECONDS)
            result
        } catch (ex: Exception) {
            circuitBreaker.onError(System.nanoTime() - startNanos, TimeUnit.NANOSECONDS, ex)
            logger.warn("CircuitBreaker: Call failed - state=${circuitBreaker.state}, cause=${ex.message}")
            throw LlmServiceException("LLM call failed: ${ex.message}", ex)
        }
    }
}