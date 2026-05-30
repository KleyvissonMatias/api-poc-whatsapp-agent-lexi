package com.kleyvissonmatias.poc.whatsapp.agent.lexi.infrastructure.adapter.outbound.repository

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.aggregate.MessageAggregate
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.repository.MessageRepository
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.MessageId
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class InMemoryMessageRepository : MessageRepository {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val storage: MutableMap<String, MessageAggregate> = ConcurrentHashMap()
    private val jobIdIndex: MutableMap<String, String> = ConcurrentHashMap()

    override suspend fun save(aggregate: MessageAggregate): MessageAggregate {
        val key = aggregate.getMessageId().toString()
        storage[key] = aggregate
        jobIdIndex[aggregate.messageJob.jobId] = key
        logger.debug("Message saved: messageId=$key, jobId=${aggregate.messageJob.jobId}, status=${aggregate.messageJob.status}")
        return aggregate
    }

    override suspend fun findById(messageId: MessageId): MessageAggregate? {
        return storage[messageId.toString()].also {
            if (it != null) {
                logger.debug("Message found: messageId=$messageId")
            } else {
                logger.debug("Message not found: messageId=$messageId")
            }
        }
    }

    override suspend fun findByJobId(jobId: String): MessageAggregate? {
        val key = jobIdIndex[jobId] ?: run {
            logger.debug("Message not found by jobId: jobId=$jobId")
            return null
        }
        return storage[key].also {
            logger.debug("Message found by jobId: jobId=$jobId")
        }
    }
}