package com.plantpulse.healthservice.infrastructure.feign.fallback

import com.plantpulse.healthservice.infrastructure.feign.PlantDto
import com.plantpulse.healthservice.infrastructure.feign.PlantServiceFeignClient
import com.plantpulse.healthservice.infrastructure.feign.SpeciesDto
import com.plantpulse.healthservice.infrastructure.feign.ValidationResultDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory
import java.util.UUID

/**
 * Fallback implementation for PlantServiceFeignClient.
 *
 * This is invoked when:
 * 1. Circuit breaker is OPEN (too many failures to Plant Service)
 * 2. Network timeout or connection error
 * 3. Plant Service is unreachable
 *
 * Strategy: PESSIMISTIC (Fail-Safe)
 * - When we can't confirm a plant exists, assume it doesn't
 * - Better to reject a valid operation temporarily than create orphaned data
 * - Prevents cascading failures
 *
 * This conservative approach protects data consistency over availability.
 */
@Component
class PlantServiceFallback : PlantServiceFeignClient {
    private val log = LoggerFactory.getLogger(PlantServiceFallback::class.java)

    /**
     * Fallback for validatePlantExists.
     *
     * When Plant Service is down, we cannot confirm plant exists.
     * Return 503 Service Unavailable to signal health profile creation should be skipped.
     *
     * Impact: User's plant.added event won't create a health profile until
     * Plant Service recovers and this operation is retried.
     */
    override fun validatePlantExists(
        plantId: UUID,
        userId: UUID?
    ): ResponseEntity<PlantDto> {
        log.warn(
            "FALLBACK: Plant Service unavailable - cannot validate plant {} for user {}",
            plantId, userId
        )
        // PESSIMISTIC: Return error to skip profile creation
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
    }

    /**
     * Fallback for getSpecies.
     *
     * When Plant Service is down, return minimal default species data.
     * This allows observation processing to continue but with generic watering frequency.
     *
     * Impact: Health assessments proceed but with safe default (7-day watering cycle)
     * instead of species-specific requirements.
     */
    override fun getSpecies(speciesId: UUID): ResponseEntity<SpeciesDto> {
        log.warn("FALLBACK: Plant Service unavailable - returning default species for {}", speciesId)

        // SEMI-OPTIMISTIC: Return safe defaults to allow processing to continue
        return ResponseEntity.ok(
            SpeciesDto(
                id = speciesId,
                name = "Unknown Species",
                scientificName = "Unknown",
                wateringFrequencyDays = 7,  // Safe default (weekly watering)
                careDifficulty = "MODERATE"
            )
        )
    }

    /**
     * Fallback for validatePlantOwnership.
     *
     * When Plant Service is down, we cannot verify ownership.
     * Return "invalid" to prevent processing observations for unverified plants.
     *
     * Impact: Observations won't be recorded until Plant Service is available
     * and we can verify the plant-to-user relationship.
     */
    override fun validatePlantOwnership(
        plantId: UUID,
        userId: UUID
    ): ResponseEntity<ValidationResultDto> {
        log.warn(
            "FALLBACK: Plant Service unavailable - cannot validate ownership of plant {} for user {}",
            plantId, userId
        )

        // PESSIMISTIC: Return invalid to prevent processing if we can't verify
        return ResponseEntity.ok(
            ValidationResultDto(
                valid = false,
                message = "Cannot validate: Plant Service is unavailable"
            )
        )
    }
}
