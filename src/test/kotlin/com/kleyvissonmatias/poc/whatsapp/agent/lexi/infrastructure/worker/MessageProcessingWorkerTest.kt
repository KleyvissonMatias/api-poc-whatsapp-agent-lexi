package com.kleyvissonmatias.poc.whatsapp.agent.lexi.infrastructure.worker

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.port.MessageQueuePort
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.usecase.ProcessMessageJobUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class MessageProcessingWorkerTest {

    private lateinit var messageQueuePort: MessageQueuePort
    private lateinit var processMessageJobUseCase: ProcessMessageJobUseCase
    private lateinit var worker: MessageProcessingWorker

    @BeforeEach
    fun setUp() {
        messageQueuePort = mock(MessageQueuePort::class.java)
        processMessageJobUseCase = mock(ProcessMessageJobUseCase::class.java)
        worker = MessageProcessingWorker(messageQueuePort, processMessageJobUseCase)
    }

    @AfterEach
    fun tearDown() {
        worker.stop()
    }

    @Test
    fun `should start without error`() {
        worker.start()
    }

    @Test
    fun `should not start twice`() = runBlocking {
        `when`(messageQueuePort.dequeue()).thenReturn(null)

        worker.start()
        worker.start()

        delay(100)
        worker.stop()
    }

    @Test
    fun `should process a job when dequeued`() = runBlocking {
        `when`(messageQueuePort.dequeue()).thenReturn("job-1").thenReturn(null)
        `when`(processMessageJobUseCase.execute("job-1")).thenReturn(true)

        worker.start()
        delay(200)
        worker.stop()

        verify(processMessageJobUseCase, atLeastOnce()).execute("job-1")
    }

    @Test
    fun `should continue processing after a job failure`() = runBlocking {
        `when`(messageQueuePort.dequeue()).thenReturn("job-fail").thenReturn("job-ok").thenReturn(null)
        `when`(processMessageJobUseCase.execute("job-fail")).thenThrow(RuntimeException("LLM error"))
        `when`(processMessageJobUseCase.execute("job-ok")).thenReturn(true)

        worker.start()
        delay(300)
        worker.stop()

        verify(processMessageJobUseCase, atLeastOnce()).execute("job-fail")
        verify(processMessageJobUseCase, atLeastOnce()).execute("job-ok")
    }

    @Test
    fun `should stop gracefully`() {
        worker.start()
        worker.stop()
    }
}