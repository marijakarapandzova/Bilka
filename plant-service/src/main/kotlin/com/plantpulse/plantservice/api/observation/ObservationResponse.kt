package com.plantpulse.plantservice.api.observation

import java.time.Instant
import java.util.UUID

data class DiseaseMatchResponse(
    val diseaseName: String,
    val matchPercentage: Int,
    val treatmentSteps: List<String>
)

data class ObservationResponse(
    val id: UUID,
    val plantId: UUID,
    val loggedAt: Instant,
    val diseaseMatch: DiseaseMatchResponse,
    // Symptom observations
    val leafColor: String,
    val leafTexture: String,
    val soilMoisture: String,
    val visiblePests: Boolean,
    val growth: String,
    val notes: String?,
    val photoUrl: String?,
    // Kaggle dataset compatible measurements
    val heightCm: Double?,
    val leafCount: Int?,
    val newGrowthCount: Int?,
    val healthNotes: String?,
    val wateringAmountMl: Double?,
    val wateringFrequencyDays: Int?,
    val sunlightExposure: String?,
    val roomTemperatureC: Double?,
    val humidityPercent: Double?,
    val fertilizerType: String?,
    val fertilizerAmountMl: Double?,
    val pestPresence: String?,
    val pestSeverity: String?,
    val soilMoisturePercent: Double?,
    val soilType: String?,
    val healthScore: Int?
)
