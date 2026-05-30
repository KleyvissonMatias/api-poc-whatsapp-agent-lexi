package com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.entity

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.MessageId
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.ProcessingStatus
import java.time.Instant
import java.util.*

data class MessageJob(
    val jobId: String,
    val messageId: MessageId,
    val status: ProcessingStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
    val errorMessage: String? = null
) {
    fun markAsProcessing(): MessageJob = copy(status = ProcessingStatus.PROCESSING, updatedAt = Instant.now())

    fun markAsCompleted(): MessageJob = copy(status = ProcessingStatus.COMPLETED, updatedAt = Instant.now())

    fun markAsFailed(error: String): MessageJob = copy(
        status = ProcessingStatus.FAILED,
        updatedAt = Instant.now(),
        errorMessage = error
    )

    companion object {
        private const val JOB_ID_PREFIX = "job"

        fun create(messageId: MessageId): MessageJob {
            val now = Instant.now()
            return MessageJob(
                jobId = "$JOB_ID_PREFIX-${UUID.randomUUID()}",
                messageId = messageId,
                status = ProcessingStatus.PENDING,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}