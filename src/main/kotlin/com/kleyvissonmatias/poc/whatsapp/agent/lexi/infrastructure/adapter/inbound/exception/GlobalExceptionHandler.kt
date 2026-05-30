package com.kleyvissonmatias.poc.whatsapp.agent.lexi.infrastructure.adapter.inbound.exception

import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.dto.ErrorResponse
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.application.exception.ApplicationException
import com.kleyvissonmatias.poc.whatsapp.agent.lexi.domain.exception.DomainException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.MethodNotAllowedException
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebInputException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(ApplicationException::class)
    fun handleApplicationException(
        ex: ApplicationException,
        request: ServerHttpRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Application exception: ${ex.message}")

        val response = ErrorResponse(
            error = ex::class.simpleName ?: "ApplicationException",
            message = ex.message,
            path = request.path.value()
        )

        return ResponseEntity
            .status(HttpStatus.valueOf(ex.statusCode))
            .body(response)
    }

    @ExceptionHandler(DomainException::class)
    fun handleDomainException(
        ex: DomainException,
        request: ServerHttpRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Domain exception: ${ex.message}")

        val response = ErrorResponse(
            error = ex::class.simpleName ?: "DomainException",
            message = ex.message,
            path = request.path.value()
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response)
    }

    @ExceptionHandler(MethodNotAllowedException::class)
    fun handleMethodNotAllowedException(
        ex: MethodNotAllowedException,
        request: ServerHttpRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Method not allowed: ${request.method} ${request.path}")
        return createErrorResponse(
            HttpStatus.METHOD_NOT_ALLOWED,
            "MethodNotAllowed",
            "The requested method is not supported for this endpoint",
            request
        )
    }

    @ExceptionHandler(ServerWebInputException::class)
    fun handleServerWebInputException(
        ex: ServerWebInputException,
        request: ServerHttpRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Web input exception: ${ex.reason ?: ex.message}")
        return createErrorResponse(HttpStatus.BAD_REQUEST, "BadRequest", ex.reason ?: "Invalid request input", request)
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(
        ex: ResponseStatusException,
        request: ServerHttpRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Response status exception: ${ex.statusCode} - ${ex.reason ?: ex.message}")
        return createErrorResponse(
            HttpStatus.valueOf(ex.statusCode.value()),
            "ResponseStatusError",
            ex.reason ?: "An error occurred processing the request",
            request
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: ServerHttpRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected exception: ${ex::class.simpleName} - ${ex.message}", ex)

        val response = ErrorResponse(
            error = "InternalServerError",
            message = "An unexpected error occurred",
            path = request.path.value()
        )

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(response)
    }

    private fun createErrorResponse(
        status: HttpStatus,
        error: String,
        message: String,
        request: ServerHttpRequest
    ): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            error = error,
            message = message,
            path = request.path.value()
        )
        return ResponseEntity.status(status).body(response)
    }
}