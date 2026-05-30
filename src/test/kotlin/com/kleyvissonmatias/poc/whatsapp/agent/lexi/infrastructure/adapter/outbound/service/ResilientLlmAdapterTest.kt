package com.kleyvissonmatias.poc.whatsapp.agent.lexi.infrastructure.adapter.outbound.service

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.exception.LlmServiceException
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.port.LlmPort
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.MessageContent
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class ResilientLlmAdapterTest {

    private fun createCircuitBreaker(
        failureRateThreshold: Float = 100.0f,
        slidingWindowSize: Int = 2,
        minimumNumberOfCalls: Int = 2
    ): CircuitBreaker {
        val config = CircuitBreakerConfig.custom()
            .failureRateThreshold(failureRateThreshold)
            .slidingWindowSize(slidingWindowSize)
            .minimumNumberOfCalls(minimumNumberOfCalls)
            .build()
        return CircuitBreaker.of(UUID.randomUUID().toString(), config)
    }

    private fun successLlm(response: MessageContent = MessageContent("Response")): LlmPort =
        object : LlmPort {
            override suspend fun generateResponse(userMessage: MessageContent) = response
        }

    private fun failingLlm(error: String = "LLM error"): LlmPort =
        object : LlmPort {
            override suspend fun generateResponse(userMessage: MessageContent): MessageContent =
                throw RuntimeException(error)
        }

    private class CountingLlm(private val inner: LlmPort) : LlmPort {
        var callCount = 0
        override suspend fun generateResponse(userMessage: MessageContent): MessageContent {
            callCount++
            return inner.generateResponse(userMessage)
        }
    }

    @Test
    fun `should delegate call and return response when circuit is closed`() = runBlocking {
        val cb = createCircuitBreaker()
        val expected = MessageContent("Response")
        val adapter = ResilientLlmAdapter(successLlm(expected), cb)

        val result = adapter.generateResponse(MessageContent("Hello"))

        assertEquals(expected, result)
        assertEquals(CircuitBreaker.State.CLOSED, cb.state)
    }

    @Test
    fun `should wrap delegate exception in LlmServiceException`() = runBlocking<Unit> {
        val cb = createCircuitBreaker(minimumNumberOfCalls = 10)
        val adapter = ResilientLlmAdapter(failingLlm("timeout"), cb)

        val ex = assertThrows<LlmServiceException> { adapter.generateResponse(MessageContent("Hello")) }
        assertEquals("LLM call failed: timeout", ex.message)
    }

    @Test
    fun `should open circuit after consecutive failures`() = runBlocking<Unit> {
        val cb = createCircuitBreaker(failureRateThreshold = 100.0f, slidingWindowSize = 2, minimumNumberOfCalls = 2)
        val adapter = ResilientLlmAdapter(failingLlm(), cb)

        repeat(2) { assertThrows<LlmServiceException> { adapter.generateResponse(MessageContent("Hello")) } }

        assertEquals(CircuitBreaker.State.OPEN, cb.state)
    }

    @Test
    fun `should reject call immediately when circuit is open without invoking delegate`() = runBlocking<Unit> {
        val cb = createCircuitBreaker()
        cb.transitionToOpenState()
        val counting = CountingLlm(successLlm())
        val adapter = ResilientLlmAdapter(counting, cb)

        val ex = assertThrows<LlmServiceException> { adapter.generateResponse(MessageContent("Hello")) }
        assertEquals("LLM service temporarily unavailable (circuit breaker OPEN)", ex.message)
        assertEquals(0, counting.callCount)
    }

    @Test
    fun `should record success and keep circuit closed`() = runBlocking {
        val cb = createCircuitBreaker(slidingWindowSize = 5, minimumNumberOfCalls = 5)
        val adapter = ResilientLlmAdapter(successLlm(), cb)

        repeat(5) { adapter.generateResponse(MessageContent("Hello")) }

        assertEquals(CircuitBreaker.State.CLOSED, cb.state)
        assertEquals(5, cb.metrics.numberOfSuccessfulCalls)
    }
}