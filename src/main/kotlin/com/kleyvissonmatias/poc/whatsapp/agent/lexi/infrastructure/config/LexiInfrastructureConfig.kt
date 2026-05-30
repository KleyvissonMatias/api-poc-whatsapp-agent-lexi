package com.kleyvissonmatias.poc.whatsapp.agent.lexi.infrastructure.config

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.port.MessageQueuePort
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.usecase.ProcessInboundMessageUseCase
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.usecase.ProcessMessageJobUseCase
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.port.LlmPort
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.port.MessageRepository
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.port.OutboundMessagingPort
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.infrastructure.adapter.outbound.queue.InMemoryMessageQueue
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.infrastructure.adapter.outbound.repository.InMemoryMessageRepository
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.infrastructure.adapter.outbound.service.ResilientLlmAdapter
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.infrastructure.adapter.outbound.service.SimulatedLlmAdapter
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.infrastructure.adapter.outbound.service.WhatsAppOutboundService
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.infrastructure.worker.MessageProcessingWorker
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class LexiInfrastructureConfig {

    @Bean
    fun circuitBreaker(): CircuitBreaker {
        val config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50.0f)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .permittedNumberOfCallsInHalfOpenState(3)
            .build()
        return CircuitBreaker.of("lexi-llm-circuit-breaker", config)
    }

    @Bean
    fun messageRepository(): MessageRepository = InMemoryMessageRepository()

    @Bean
    fun messageQueuePort(): MessageQueuePort = InMemoryMessageQueue()

    @Bean
    fun llmPort(circuitBreaker: CircuitBreaker): LlmPort = ResilientLlmAdapter(SimulatedLlmAdapter(), circuitBreaker)

    @Bean
    fun outboundMessagingPort(): OutboundMessagingPort = WhatsAppOutboundService()

    @Bean
    fun processInboundMessageUseCase(
        messageRepository: MessageRepository,
        messageQueuePort: MessageQueuePort
    ): ProcessInboundMessageUseCase =
        ProcessInboundMessageUseCase(messageRepository, messageQueuePort)

    @Bean
    fun processMessageJobUseCase(
        messageRepository: MessageRepository,
        llmPort: LlmPort,
        outboundMessagingPort: OutboundMessagingPort
    ): ProcessMessageJobUseCase = ProcessMessageJobUseCase(messageRepository, llmPort, outboundMessagingPort)

    @Bean
    fun messageProcessingWorker(
        messageQueuePort: MessageQueuePort,
        processMessageJobUseCase: ProcessMessageJobUseCase
    ): MessageProcessingWorker = MessageProcessingWorker(messageQueuePort, processMessageJobUseCase)

    @Bean
    fun workerRunner(worker: MessageProcessingWorker): CommandLineRunner = CommandLineRunner {
        worker.start()
    }
}