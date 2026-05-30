package com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.port

interface MessageQueuePort {
    suspend fun enqueue(jobId: String): Boolean
    suspend fun dequeue(): String?
    fun getQueueSize(): Int
}