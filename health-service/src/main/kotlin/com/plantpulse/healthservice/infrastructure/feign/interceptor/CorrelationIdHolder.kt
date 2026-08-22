package com.plantpulse.healthservice.infrastructure.feign.interceptor

import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Thread-local holder for correlation ID.
 *
 * Enables distributed tracing across service boundaries by maintaining
 * a unique ID for each user request as it flows through multiple services.
 *
 * The correlation ID is:
 * 1. Extracted from incoming HTTP request headers (or generated if missing)
 * 2. Stored in ThreadLocal for this request's thread
 * 3. Injected by CorrelationIdInterceptor into all outgoing Feign calls
 * 4. Logged with every operation for debugging
 *
 * Usage pattern:
 * - Controller/Kafka listener: Set correlation ID from request
 * - Feign interceptor: Retrieve and attach to HTTP headers
 * - Log statements: Include correlation ID in every log
 * - Downstream services: Repeat this process
 *
 * Result: Full request trace across multiple services in log aggregation tools (ELK, etc.)
 */
@Component
class CorrelationIdHolder {
    private val correlationIdThreadLocal = ThreadLocal<String?>()

    /**
     * Sets the correlation ID for this request thread.
     * If null is passed, clears the ThreadLocal to prevent leaks.
     */
    fun setCorrelationId(correlationId: String?) {
        if (correlationId != null) {
            correlationIdThreadLocal.set(correlationId)
        } else {
            correlationIdThreadLocal.remove()
        }
    }

    /**
     * Retrieves the correlation ID for this request thread.
     * If not set, generates a new UUID and stores it.
     * This ensures every operation has a correlation ID.
     */
    fun getCorrelationId(): String? =
        correlationIdThreadLocal.get() ?: generateNewCorrelationId()

    /**
     * Clears the correlation ID from ThreadLocal.
     * Should be called in a finally block or servlet filter to prevent
     * ThreadLocal leaks in thread pool environments.
     */
    fun clear() {
        correlationIdThreadLocal.remove()
    }

    /**
     * Generates a new UUID-based correlation ID.
     * Called when no correlation ID exists for this request thread.
     */
    private fun generateNewCorrelationId(): String {
        val newId = UUID.randomUUID().toString()
        correlationIdThreadLocal.set(newId)
        return newId
    }
}
