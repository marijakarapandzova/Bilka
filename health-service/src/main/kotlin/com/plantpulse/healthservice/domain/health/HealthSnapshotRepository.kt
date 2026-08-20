package com.plantpulse.healthservice.domain.health

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.UUID

interface HealthSnapshotRepository : JpaRepository<HealthSnapshot, UUID> {

    fun findByPlantIdOrderByRecordedAtDesc(plantId: UUID): List<HealthSnapshot>

    fun findTopByPlantIdOrderByRecordedAtDesc(plantId: UUID): HealthSnapshot?

    fun findTopByPlantIdAndRecordedAtBeforeOrderByRecordedAtDesc(plantId: UUID, before: Instant): HealthSnapshot?

    fun existsByPlantIdAndSourceAndRecordedAtAfter(plantId: UUID, source: SnapshotSource, after: Instant): Boolean

    /**
     * Geo query for outbreak detection: how many *distinct* plants in a city
     * have shown the same non-healthy disease match (at or above the
     * confidence threshold) within the rolling detection window.
     */
    @Query(
        """
        SELECT new com.plantpulse.healthservice.domain.health.OutbreakCandidate(
            s.cityLocation, s.diseaseName, COUNT(DISTINCT s.plantId)
        )
        FROM HealthSnapshot s
        WHERE s.recordedAt >= :since
          AND s.cityLocation IS NOT NULL
          AND s.diseaseName IS NOT NULL
          AND s.diseaseName <> 'Healthy'
          AND s.diseaseMatchPercentage >= :minMatchPercentage
        GROUP BY s.cityLocation, s.diseaseName
        HAVING COUNT(DISTINCT s.plantId) >= :minAffectedPlants
        """
    )
    fun findOutbreakCandidates(
        since: Instant,
        minMatchPercentage: Int,
        minAffectedPlants: Long
    ): List<OutbreakCandidate>

    /** Cheap, targeted version of the query above for the real-time check that runs right after an observation. */
    @Query(
        """
        SELECT COUNT(DISTINCT s.plantId)
        FROM HealthSnapshot s
        WHERE s.cityLocation = :city
          AND s.diseaseName = :diseaseName
          AND s.recordedAt >= :since
          AND s.diseaseMatchPercentage >= :minMatchPercentage
        """
    )
    fun countDistinctAffectedPlants(
        city: String,
        diseaseName: String,
        since: Instant,
        minMatchPercentage: Int
    ): Long
}

/** Projection used by the outbreak-detection GROUP BY query above. */
data class OutbreakCandidate(
    val cityLocation: String,
    val diseaseName: String,
    val affectedPlantCount: Long
)
