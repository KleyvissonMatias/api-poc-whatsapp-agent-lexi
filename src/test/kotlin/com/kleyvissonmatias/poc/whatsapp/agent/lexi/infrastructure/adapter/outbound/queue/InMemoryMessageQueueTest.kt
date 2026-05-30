package com.kleyvissonmatias.poc.whatsapp.agent.lexi.infrastructure.adapter.outbound.queue

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InMemoryMessageQueueTest {

    private lateinit var queue: InMemoryMessageQueue

    @BeforeEach
    fun setUp() {
        queue = InMemoryMessageQueue()
    }

    @Test
    fun `should enqueue and return true`() = runBlocking {
        val result = queue.enqueue("job-1")
        assertTrue(result)
    }

    @Test
    fun `should dequeue enqueued job`() = runBlocking {
        queue.enqueue("job-1")
        val dequeued = queue.dequeue()
        assertEquals("job-1", dequeued)
    }

    @Test
    fun `should return null when queue is empty`() = runBlocking {
        val result = queue.dequeue()
        assertNull(result)
    }

    @Test
    fun `should respect FIFO order`() = runBlocking {
        queue.enqueue("job-1")
        queue.enqueue("job-2")
        queue.enqueue("job-3")

        assertEquals("job-1", queue.dequeue())
        assertEquals("job-2", queue.dequeue())
        assertEquals("job-3", queue.dequeue())
    }

    @Test
    fun `should report correct queue size`() = runBlocking {
        assertEquals(0, queue.getQueueSize())
        queue.enqueue("job-1")
        queue.enqueue("job-2")
        assertEquals(2, queue.getQueueSize())
        queue.dequeue()
        assertEquals(1, queue.getQueueSize())
    }

    @Test
    fun `should return null after all items are dequeued`() = runBlocking {
        queue.enqueue("job-1")
        queue.dequeue()
        val result = queue.dequeue()
        assertNull(result)
        assertEquals(0, queue.getQueueSize())
    }
}