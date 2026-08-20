package com.plantpulse.healthservice.api.plant

import com.plantpulse.healthservice.domain.plant.WateringUrgency
import java.time.Instant
import java.util.UUID

data class CareScheduleResponse(
    val plantId: UUID,
    val nextWateringDate: Instant,
    val daysUntilWatering: Long,
    val wateringUrgency: WateringUrgency,
    val skipWatering: Boolean,
    val skipWateringReason: String?
)

data class HealthSnapshotResponse(
    val healthScore: Int,
    val status: String,
    val diseaseName: String?,
    val diseaseMatchPercentage: Int?,
    val source: String,
    val recordedAt: Instant
)

data class PlantHealthResponse(
    val plantId: UUID,
    val nickname: String,
    val speciesName: String,
    val healthScore: Int,
    val status: String,
    val diseaseName: String?,
    val diseaseMatchPercentage: Int?,
    val treatmentSteps: List<String>,
    val careSchedule: CareScheduleResponse,
    val lastObservationAt: Instant?,
    val lastWateredAt: Instant?
)
