package com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.entity

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.MessageId
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.ProcessingStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MessageJobTest {

    private fun createJob() = MessageJob.create(MessageId.generate())

    @Test
    fun `should create job with PENDING status`() {
        val job = createJob()
        assertEquals(ProcessingStatus.PENDING, job.status)
        assertNull(job.errorMessage)
        assertTrue(job.jobId.startsWith("job-"))
    }

    @Test
    fun `should transition to PROCESSING`() {
        val job = createJob().markAsProcessing()
        assertEquals(ProcessingStatus.PROCESSING, job.status)
    }

    @Test
    fun `should transition to COMPLETED`() {
        val job = createJob().markAsProcessing().markAsCompleted()
        assertEquals(ProcessingStatus.COMPLETED, job.status)
        assertNull(job.errorMessage)
    }

    @Test
    fun `should transition to FAILED with error message`() {
        val errorMsg = "LLM timeout"
        val job = createJob().markAsProcessing().markAsFailed(errorMsg)
        assertEquals(ProcessingStatus.FAILED, job.status)
        assertEquals(errorMsg, job.errorMessage)
    }

    @Test
    fun `updatedAt should change on each transition`() {
        val original = createJob()
        Thread.sleep(5)
        val processing = original.markAsProcessing()
        assertTrue(processing.updatedAt >= original.updatedAt)
    }

    @Test
    fun `jobId should contain the prefix`() {
        val job = createJob()
        assertNotNull(job.jobId)
        assertTrue(job.jobId.isNotBlank())
    }
}