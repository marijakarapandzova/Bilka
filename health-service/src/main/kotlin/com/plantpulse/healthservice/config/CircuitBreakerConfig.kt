package com.plantpulse.healthservice.config

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import feign.FeignException
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.slf4j.LoggerFactory
import java.time.Duration

/**
 * Configuration for Resilience4j Circuit Breaker.
 *
 * Protects Health Service from cascading failures when Plant Service is unavailable.
 * Uses the "circuit breaker" pattern to detect failures and stop sending requests
 * to a failing service, allowing it to recover.
 *
 * States:
 * 1. CLOSED (normal):
 *    - Requests pass through to Plant Service
 *    - Failure rate is monitored
 *    - If failure rate exceeds threshold → transitions to OPEN
 *
 * 2. OPEN (service failing):
 *    - Requests are NOT sent to Plant Service (short-circuit)
 *    - Fallback methods are invoked immediately
 *    - No wasted time waiting for timeouts
 *    - After waitDuration → transitions to HALF_OPEN
 *
 * 3. HALF_OPEN (testing recovery):
 *    - Small number of test requests are allowed through
 *    - If they succeed → transitions back to CLOSED
 *    - If they fail → transitions back to OPEN
 */
@Configuration
class CircuitBreakerConfig {
    private val log = LoggerFactory.getLogger(CircuitBreakerConfig::class.java)

    /**
     * Creates the Circuit Breaker Registry with default configuration.
     *
     * The registry manages all circuit breaker instances in the application.
     * Configuration applied here becomes the default for all circuit breakers.
     */
    @Bean
    fun circuitBreakerRegistry(): CircuitBreakerRegistry {
        val config = CircuitBreakerConfig.custom()
            // === Failure Detection ===
            /**
             * failureRateThreshold: Open circuit after this % of calls fail
             * Example: 50% means if 5 out of 10 calls fail, circuit opens
             */
            .failureRateThreshold(50f)

            /**
             * minimumNumberOfCalls: Don't evaluate failure rate until N calls
             * Why? Prevents circuit from opening on just 1 unlucky request
             * Example: With 5, circuit doesn't open until we've made 5+ calls
             */
            .minimumNumberOfCalls(5)

            // === Slow Call Detection ===
            /**
             * slowCallRateThreshold: Open circuit if this % of calls are slow
             * Example: 50% means if 5 out of 10 calls exceed slowCallDurationThreshold,
             * the circuit opens (even if they don't fail)
             */
            .slowCallRateThreshold(50f)

            /**
             * slowCallDurationThreshold: Define "slow" as any call taking longer than this
             * Example: 2000ms means any call taking >2 seconds is considered slow
             */
            .slowCallDurationThreshold(Duration.ofSeconds(2))

            // === Recovery Timing ===
            /**
             * waitDurationInOpenState: How long to wait before testing recovery
             * Example: 30s means after opening, wait 30 seconds then try HALF_OPEN
             * Development: Use smaller value (e.g., 5s) for faster testing
             */
            .waitDurationInOpenState(Duration.ofSeconds(30))

            /**
             * permittedNumberOfCallsInHalfOpenState: How many test calls to allow in HALF_OPEN
             * Example: 3 means try 3 requests. If all succeed, go back to CLOSED
             */
            .permittedNumberOfCallsInHalfOpenState(3)

            // === Exception Handling ===
            /**
             * recordExceptions: Which exceptions should trigger circuit breaker logic
             * These are considered "failures" that count toward opening the circuit
             */
            .recordExceptions(
                *arrayOf(
                    java.net.ConnectException::class.java,       // Network connection refused
                    java.net.SocketTimeoutException::class.java, // Read timeout
                    feign.FeignException.ServiceUnavailable::class.java,  // HTTP 503
                    feign.FeignException.GatewayTimeout::class.java       // HTTP 504
                )
            )

            /**
             * ignoreExceptions: Which exceptions should NOT trigger circuit breaker logic
             * These are considered "valid responses", not failures
             */
            .ignoreExceptions(
                feign.FeignException.NotFound::class.java    // HTTP 404 - expected for missing plants
            )

            .build()

        val registry = CircuitBreakerRegistry.of(config)
        log.info("Circuit Breaker Registry initialized with default config")

        return registry
    }

    /**
     * Creates a named circuit breaker instance for Plant Service client.
     *
     * The name "plant-service" is used by the @CircuitBreaker annotation
     * in the Feign client fallback configuration.
     *
     * Each circuit breaker tracks its own state independently, but all
     * use the same failure thresholds and timing from the config above.
     */
    @Bean(name = arrayOf("plantServiceCircuitBreaker"))
    fun plantServiceCircuitBreaker(
        registry: CircuitBreakerRegistry
    ): CircuitBreaker {
        log.info("Creating circuit breaker for plant-service")

        val cb = registry.circuitBreaker("plant-service", registry.defaultConfig)

        // Listen to state transitions (for logging and monitoring)
        cb.getEventPublisher()
            .onStateTransition { event ->
                log.warn(
                    "CIRCUIT BREAKER STATE CHANGE: plant-service {} → {}",
                    event.stateTransition.fromState,
                    event.stateTransition.toState
                )
            }
            .onSuccess { event ->
                log.debug("Circuit breaker success: {} (duration: {}ms)",
                    event.creationTime, event.elapsedDuration.toMillis())
            }
            .onError { event ->
                log.error(
                    "Circuit breaker error: {} - {} (duration: {}ms)",
                    event.throwable.javaClass.simpleName,
                    event.throwable.message,
                    event.elapsedDuration.toMillis()
                )
            }
            .onIgnoredError { event ->
                log.debug("Circuit breaker ignored error (not counted): {} - {}",
                    event.throwable.javaClass.simpleName,
                    event.throwable.message)
            }

        return cb
    }
}

/**
 * Development Configuration for Circuit Breaker.
 *
 * Use this for local development to speed up circuit breaker testing.
 * Override properties in application-dev.yml instead of changing this code.
 *
 * Example application-dev.yml:
 *
 * resilience4j:
 *   circuitbreaker:
 *     instances:
 *       plant-service:
 *         failureRateThreshold: 30       # Lower threshold = fail faster
 *         waitDurationInOpenState: 5000  # Shorter wait = test recovery faster
 *         minimumNumberOfCalls: 3        # Test with fewer calls
 */
