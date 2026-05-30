package com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.repository

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.aggregate.MessageAggregate
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.valueobject.MessageId

interface MessageRepository {
    suspend fun save(aggregate: MessageAggregate): MessageAggregate
    suspend fun findById(messageId: MessageId): MessageAggregate?
    suspend fun findByJobId(jobId: String): MessageAggregate?
}