package com.kleyvissonmatias.poc.whatsapp.agent.lexi.infrastructure.adapter.inbound.controller

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.dto.InboundWebhookRequest
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.dto.WebhookResponse
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.service.ProcessInboundMessageUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/v1/webhook")
@Tag(name = "WhatsApp Webhook", description = "WhatsApp message webhook endpoints")
class WebhookController(
    private val processInboundMessageUseCase: ProcessInboundMessageUseCase
) {

    @PostMapping("/messages")
    @Operation(
        summary = "Receive incoming WhatsApp message",
        description = "Receives a message from WhatsApp, queues it for processing, and returns immediately with 202 Accepted"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "202",
                description = "Message accepted and queued for processing",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = WebhookResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request parameters",
                content = [Content(mediaType = "application/json")]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [Content(mediaType = "application/json")]
            )
        ]
    )
    suspend fun receiveMessage(
        @RequestBody request: InboundWebhookRequest
    ): ResponseEntity<WebhookResponse> {
        val aggregate = processInboundMessageUseCase.execute(request)

        val response = WebhookResponse(
            status = WebhookResponse.STATUS_QUEUED,
            messageId = aggregate.inboundMessage.messageId.toString(),
            timestamp = Instant.now().toString()
        )

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response)
    }
}