package com.plantpulse.healthservice.infrastructure.feign

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import feign.Logger
import feign.RequestInterceptor
import feign.codec.ErrorDecoder
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import com.plantpulse.healthservice.infrastructure.feign.fallback.PlantServiceFallback
import java.time.Instant
import java.util.UUID

/**
 * OpenFeign client for Plant Service.
 *
 * This declarative HTTP client makes synchronous calls to Plant Service
 * to validate cross-service constraints before creating/updating entities
 * in Health Service.
 *
 * Circuit Breaker: Resilience4j automatically wraps these calls. If Plant Service
 * fails too many times, the circuit opens and fallback methods are invoked.
 *
 * Fallback Strategy: PlantServiceFallback.kt implements this interface with
 * conservative defaults (fail-safe).
 */
@FeignClient(
    name = "plant-service",
    url = "\${feign.plant-service.url:http://plant-service:8081}",
    fallback = PlantServiceFallback::class,
    configuration = [PlantServiceFeignClientConfig::class]
)
interface PlantServiceFeignClient {

    /**
     * Validates that a plant exists in Plant Service before Health Service
     * creates its health profile.
     *
     * Returns 200 OK if found, 404 Not Found if not found.
     *
     * Circuit Breaker: OPEN → fallback returns 503 Service Unavailable
     * Fallback Strategy: Pessimistic (fail-safe) - skip profile creation if
     * we can't confirm plant exists.
     *
     * @param plantId UUID of the plant to validate
     * @param userId Optional: if provided, validates plant belongs to this user
     * @return ResponseEntity containing PlantDto if found
     */
    @GetMapping("/api/plants/{plantId}")
    fun validatePlantExists(
        @PathVariable plantId: UUID,
        @RequestParam(required = false) userId: UUID? = null
    ): ResponseEntity<PlantDto>

    /**
     * Fetches species details for health assessment and care schedule calculations.
     * Health Service needs watering frequency and species metadata.
     *
     * Circuit Breaker: OPEN → fallback returns minimal default species data
     * Fallback Strategy: Semi-optimistic (fail-open) - return generic watering
     * frequency if we can't reach Plant Service.
     *
     * @param speciesId UUID of the species
     * @return ResponseEntity containing SpeciesDto with care requirements
     */
    @GetMapping("/api/species/{speciesId}")
    fun getSpecies(
        @PathVariable speciesId: UUID
    ): ResponseEntity<SpeciesDto>

    /**
     * Validates that a plant belongs to the user before processing observations.
     * Prevents Health Service from creating snapshots for plants the user doesn't own.
     *
     * Circuit Breaker: OPEN → fallback returns "invalid"
     * Fallback Strategy: Pessimistic - reject if we can't verify ownership.
     *
     * @param plantId UUID of the plant
     * @param userId UUID of the user (expected owner)
     * @return ResponseEntity with validation result
     */
    @GetMapping("/api/plants/{plantId}/validate")
    fun validatePlantOwnership(
        @PathVariable plantId: UUID,
        @RequestParam userId: UUID
    ): ResponseEntity<ValidationResultDto>
}

/**
 * DTO for Plant Service's plant response.
 * Decorated with @JsonIgnoreProperties for forward compatibility.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PlantDto(
    val id: UUID,
    val speciesId: UUID,
    val speciesName: String,
    val nickname: String,
    val wateringFrequencyDays: Int,
    val addedAt: String
)

/**
 * DTO for Plant Service's species response.
 * Contains care requirements needed by health-service for assessment.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SpeciesDto(
    val id: UUID,
    val name: String,
    val scientificName: String,
    val wateringFrequencyDays: Int,
    val careDifficulty: String
)

/**
 * DTO for validation result.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ValidationResultDto(
    val valid: Boolean,
    val message: String? = null
)

/**
 * Configuration class for PlantServiceFeignClient.
 * Registers request interceptors, error decoders, and retry policies.
 */
@Configuration
class PlantServiceFeignClientConfig {

    /**
     * Logger level: FULL for development, BASIC for production.
     * Logs request/response details for debugging.
     */
    @Bean
    fun feignLoggerLevel(): Logger.Level = Logger.Level.FULL
}
