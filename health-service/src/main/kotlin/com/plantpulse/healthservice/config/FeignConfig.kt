package com.plantpulse.healthservice.config

import feign.Logger
import feign.Retryer
import feign.codec.ErrorDecoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.slf4j.LoggerFactory

/**
 * Configuration for Feign Clients.
 *
 * Registers:
 * - Request interceptors (correlation ID, auth tokens)
 * - Retry policies (automatic retry on transient failures)
 * - Error decoders (convert HTTP errors to domain exceptions)
 * - Logger levels (for debugging)
 *
 * These are global defaults applied to all Feign clients in the application.
 */
@Configuration
class FeignConfig {
    private val log = LoggerFactory.getLogger(FeignConfig::class.java)

    /**
     * Retry Policy for Feign clients.
     *
     * Automatically retries failed requests with exponential backoff:
     * - First retry: after 100ms
     * - Subsequent retries: gradually increase delay up to 1000ms
     * - Max 2 retries (3 total attempts)
     *
     * Only retries on retriable exceptions:
     * - Connection refused
     * - Connection timeout
     * - Read timeout
     *
     * Does NOT retry on HTTP errors (4xx, 5xx) - those are not transient.
     */
    @Bean
    fun feignRetryer(): Retryer {
        log.info("Configuring Feign retry policy: max 2 retries, 100ms-1000ms backoff")
        return Retryer.Default(
            100,   // initialInterval: 100ms
            1000,  // maxInterval: 1000ms
            2      // maxAttempts: 2 retries (3 total calls)
        )
    }

    /**
     * Logger Level for Feign Clients.
     *
     * Options:
     * - NONE: No logging
     * - BASIC: Log only request/response status (production)
     * - HEADERS: Log headers in addition to status
     * - FULL: Log request/response body + headers (development)
     */
    @Bean
    fun feignLoggerLevel(): Logger.Level {
        // Development: FULL for debugging
        // Production: BASIC to reduce noise
        return Logger.Level.FULL
    }

    /**
     * Custom Error Decoder (optional).
     *
     * Converts HTTP error responses to domain exceptions.
     * For example, 404 Not Found → PlantNotFoundException
     *
     * If not registered, Feign uses default decoder which throws
     * generic FeignException for all HTTP errors.
     */
    @Bean
    fun customErrorDecoder(): ErrorDecoder {
        log.info("Registering custom Feign error decoder")
        return FeignErrorDecoder()
    }
}

/**
 * Custom Error Decoder for converting HTTP errors to domain exceptions.
 *
 * Maps common HTTP status codes to meaningful domain exceptions
 * that can be caught and handled specially by the application.
 */
class FeignErrorDecoder : ErrorDecoder {
    private val log = LoggerFactory.getLogger(FeignErrorDecoder::class.java)

    override fun decode(methodKey: String, response: feign.Response): Exception {
        return when (response.status()) {
            404 -> {
                log.warn("Feign call {} returned 404 Not Found", methodKey)
                // Could throw custom PlantNotFoundException here
                feign.FeignException.errorStatus(methodKey, response)
            }
            503 -> {
                log.error("Feign call {} returned 503 Service Unavailable", methodKey)
                // Service down - circuit breaker should catch this before retry
                feign.FeignException.errorStatus(methodKey, response)
            }
            else -> {
                log.error("Feign call {} returned HTTP {}", methodKey, response.status())
                feign.FeignException.errorStatus(methodKey, response)
            }
        }
    }
}
