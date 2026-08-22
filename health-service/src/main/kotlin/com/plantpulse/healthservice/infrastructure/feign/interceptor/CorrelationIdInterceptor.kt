package com.plantpulse.healthservice.infrastructure.feign.interceptor

import feign.RequestInterceptor
import feign.RequestTemplate
import org.slf4j.LoggerFactory

/**
 * Feign Request Interceptor that attaches correlation ID to all outgoing HTTP requests.
 *
 * Every HTTP call to downstream services automatically includes the X-Correlation-ID header,
 * enabling distributed tracing across the entire microservices architecture.
 *
 * Flow:
 * 1. Incoming request arrives at HealthService
 * 2. Servlet filter extracts/generates X-Correlation-ID header
 * 3. CorrelationIdHolder stores it in ThreadLocal
 * 4. When HealthService makes Feign call to PlantService:
 *    - This interceptor retrieves ID from CorrelationIdHolder
 *    - Adds it to the outgoing HTTP request header
 * 5. PlantService receives request with same X-Correlation-ID
 * 6. PlantService logs and potentially makes further calls with same ID
 * 7. All logs are correlated in centralized log aggregation (ELK, Splunk, etc.)
 *
 * This is essential for debugging production issues: follow the correlation ID
 * through logs of multiple services to see the complete request flow.
 */
@org.springframework.stereotype.Component
class CorrelationIdInterceptor(
    private val correlationIdHolder: CorrelationIdHolder
) : RequestInterceptor {

    private val log = LoggerFactory.getLogger(CorrelationIdInterceptor::class.java)

    override fun apply(requestTemplate: RequestTemplate) {
        // Check if correlation ID already exists in request (from upstream)
        if (requestTemplate.headers().containsKey("X-Correlation-ID")) {
            log.debug("Correlation ID already present in request: {}",
                requestTemplate.headers()["X-Correlation-ID"]?.firstOrNull())
            return  // Don't override, use existing
        }

        // Retrieve correlation ID from holder (or generate new one if missing)
        val correlationId = correlationIdHolder.getCorrelationId()

        if (correlationId != null) {
            requestTemplate.header("X-Correlation-ID", correlationId)
            log.debug("Added X-Correlation-ID header to Feign request: {}", correlationId)
        } else {
            log.warn("No correlation ID available for Feign request")
        }
    }
}
