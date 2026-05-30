package com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.usecase

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.aggregate.MessageAggregate
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.entity.InboundMessage
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.exception.JobNotFoundException
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.port.LlmPort
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.port.MessageRepository
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.port.OutboundMessagingPort
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.MessageContent
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.MessageId
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.ProcessingStatus
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.SenderId
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*

class ProcessMessageJobUseCaseTest {

    private val messageRepository = mock(MessageRepository::class.java)
    private val llmPort = mock(LlmPort::class.java)
    private val outboundMessagingPort = mock(OutboundMessagingPort::class.java)
    private val useCase = ProcessMessageJobUseCase(messageRepository, llmPort, outboundMessagingPort)

    private fun buildAggregate() = MessageAggregate.createNew(
        InboundMessage.create(
            senderId = SenderId("5511999999999"),
            content = MessageContent("Hello")
        )
    )

    @Test
    fun `should process job successfully`() = runBlocking {
        val jobId = "job-123"
        val aggregate = buildAggregate()

        `when`(messageRepository.findByJobId(jobId)).thenReturn(aggregate)
        `when`(llmPort.generateResponse(any(MessageContent::class.java))).thenReturn(MessageContent("AI Response"))
        `when`(outboundMessagingPort.sendMessage(any())).thenReturn(true)
        `when`(messageRepository.save(any(MessageAggregate::class.java))).thenAnswer { it.arguments[0] as MessageAggregate }

        val result = useCase.execute(jobId)

        assertTrue(result)
        verify(messageRepository).findByJobId(jobId)
        verify(llmPort).generateResponse(aggregate.inboundMessage.content)
        verify(outboundMessagingPort).sendMessage(any())
        verify(messageRepository, times(2)).save(any(MessageAggregate::class.java))
    }

    @Test
    fun `should return false and mark job as FAILED when LLM throws exception`() = runBlocking {
        val jobId = "job-456"
        val inboundMessage = InboundMessage.create(
            senderId = SenderId("5511999999999"),
            content = MessageContent("Hello")
        )
        val aggregate = MessageAggregate.createNew(inboundMessage)
        val savedStates = mutableListOf<ProcessingStatus>()

        val trackingRepository = object : MessageRepository {
            override suspend fun save(aggregate: MessageAggregate): MessageAggregate {
                savedStates.add(aggregate.messageJob.status)
                return aggregate
            }

            override suspend fun findById(messageId: MessageId): MessageAggregate? = null
            override suspend fun findByJobId(jobId: String): MessageAggregate? = aggregate.takeIf { jobId == "job-456" }
        }

        val throwingLlmPort = object : LlmPort {
            override suspend fun generateResponse(userMessage: MessageContent): MessageContent {
                throw RuntimeException("LLM timeout")
            }
        }

        val testUseCase = ProcessMessageJobUseCase(trackingRepository, throwingLlmPort, outboundMessagingPort)

        val result = testUseCase.execute(jobId)

        assertFalse(result)
        assertEquals(2, savedStates.size)
        assertEquals(ProcessingStatus.PROCESSING, savedStates[0])
        assertEquals(ProcessingStatus.FAILED, savedStates[1])
    }

    @Test
    fun `should throw JobNotFoundException when job does not exist`() = runBlocking<Unit> {
        `when`(messageRepository.findByJobId("missing-job")).thenReturn(null)
        assertThrows<JobNotFoundException> { useCase.execute("missing-job") }
    }
}