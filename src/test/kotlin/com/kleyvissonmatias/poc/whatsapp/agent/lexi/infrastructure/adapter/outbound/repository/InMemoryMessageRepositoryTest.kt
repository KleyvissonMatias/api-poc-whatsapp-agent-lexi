package com.kleyvissonmatias.poc.whatsapp.agent.lexi.infrastructure.adapter.outbound.repository

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.aggregate.MessageAggregate
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.entity.InboundMessage
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.MessageContent
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.MessageId
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.SenderId
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InMemoryMessageRepositoryTest {

    private lateinit var repository: InMemoryMessageRepository

    @BeforeEach
    fun setUp() {
        repository = InMemoryMessageRepository()
    }

    private fun buildAggregate(): MessageAggregate =
        MessageAggregate.createNew(
            InboundMessage.create(
                senderId = SenderId("5511999999999"),
                content = MessageContent("Hello")
            )
        )

    @Test
    fun `should save and return aggregate`() = runBlocking {
        val aggregate = buildAggregate()
        val saved = repository.save(aggregate)
        assertEquals(aggregate, saved)
    }

    @Test
    fun `should find aggregate by message id`() = runBlocking {
        val aggregate = buildAggregate()
        repository.save(aggregate)

        val found = repository.findById(aggregate.getMessageId())
        assertNotNull(found)
        assertEquals(aggregate.getMessageId(), found!!.getMessageId())
    }

    @Test
    fun `should return null for unknown message id`() = runBlocking {
        val result = repository.findById(MessageId.generate())
        assertNull(result)
    }

    @Test
    fun `should find aggregate by job id`() = runBlocking {
        val aggregate = buildAggregate()
        repository.save(aggregate)

        val found = repository.findByJobId(aggregate.messageJob.jobId)
        assertNotNull(found)
        assertEquals(aggregate.messageJob.jobId, found!!.messageJob.jobId)
    }

    @Test
    fun `should return null for unknown job id`() = runBlocking {
        val result = repository.findByJobId("non-existent-job")
        assertNull(result)
    }

    @Test
    fun `should overwrite existing aggregate on save`() = runBlocking {
        val aggregate = buildAggregate()
        repository.save(aggregate)

        val updated = aggregate.copy(messageJob = aggregate.messageJob.markAsProcessing())
        repository.save(updated)

        val found = repository.findById(aggregate.getMessageId())
        assertNotNull(found)
        assertEquals(updated.messageJob.status, found!!.messageJob.status)
    }
}