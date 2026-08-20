package com.plantpulse.healthservice.domain.plant

import com.plantpulse.healthservice.domain.health.HealthStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

/**
 * Health-service's own read model of a plant, built entirely from
 * plant-service's Kafka events (plant.added / plant.watered /
 * observation.logged / plant.removed). The id intentionally matches
 * plant-service's Plant.id so events can be applied idempotently.
 *
 * Denormalizes the latest health score/status/disease so dashboard reads
 * never need to join against the (much larger) snapshot timeline table.
 */
@Entity
@Table(name = "plant_health_profiles")
class PlantHealthProfile(

    @Id
    val id: UUID,

    @Column(nullable = false)
    val userId: UUID,

    @Column(nullable = false)
    val speciesId: UUID,

    @Column(nullable = false)
    var speciesName: String,

    @Column(nullable = false)
    var nickname: String,

    var cityLocation: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,

    @Column(nullable = false)
    var wateringFrequencyDays: Int,

    @Column(nullable = false, updatable = false)
    val addedAt: Instant,

    var lastWateredAt: Instant? = null,
    var lastObservationAt: Instant? = null,

    @Column(nullable = false)
    var currentHealthScore: Int = 100,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var currentStatus: HealthStatus = HealthStatus.HEALTHY,

    var currentDiseaseName: String? = null,
    var currentDiseaseMatchPercentage: Int? = null,

    @Column(nullable = false)
    var skipWatering: Boolean = false,
    var skipWateringReason: String? = null,

    @Column(nullable = false)
    var active: Boolean = true
) {
    protected constructor() : this(
        id = UUID.randomUUID(),
        userId = UUID.randomUUID(),
        speciesId = UUID.randomUUID(),
        speciesName = "",
        nickname = "",
        wateringFrequencyDays = 7,
        addedAt = Instant.now()
    )

    fun nextWateringDate(): Instant =
        (lastWateredAt ?: addedAt).plusSeconds(wateringFrequencyDays.toLong() * 86_400)

    fun markWatered(at: Instant) {
        lastWateredAt = at
    }

    fun markRemoved() {
        active = false
    }

    fun applyObservation(
        healthScore: Int,
        status: HealthStatus,
        diseaseName: String,
        diseaseMatchPercentage: Int,
        soilWaterlogged: Boolean,
        observedAt: Instant
    ) {
        currentHealthScore = healthScore
        currentStatus = status
        currentDiseaseName = diseaseName
        currentDiseaseMatchPercentage = diseaseMatchPercentage
        lastObservationAt = observedAt

        skipWatering = soilWaterlogged || diseaseName.equals("Root Rot", ignoreCase = true)
        skipWateringReason = when {
            diseaseName.equals("Root Rot", ignoreCase = true) -> "Root Rot suspected"
            soilWaterlogged -> "Soil is still waterlogged"
            else -> null
        }
    }

    fun applyDecay(healthScore: Int, status: HealthStatus) {
        currentHealthScore = healthScore
        currentStatus = status
    }
}
