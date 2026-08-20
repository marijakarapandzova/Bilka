package com.plantpulse.plantservice.api.observation

import com.plantpulse.plantservice.domain.observation.Growth
import com.plantpulse.plantservice.domain.observation.LeafColor
import com.plantpulse.plantservice.domain.observation.LeafTexture
import com.plantpulse.plantservice.domain.observation.SoilMoisture
import jakarta.validation.constraints.NotNull

data class LogObservationRequest(
    @field:NotNull
    val leafColor: LeafColor,

    @field:NotNull
    val leafTexture: LeafTexture,

    @field:NotNull
    val soilMoisture: SoilMoisture,

    @field:NotNull
    val visiblePests: Boolean,

    @field:NotNull
    val growth: Growth,

    val notes: String? = null,
    val photoUrl: String? = null,

    // Kaggle dataset compatible fields - actual measurements
    val heightCm: Double? = null,
    val leafCount: Int? = null,
    val newGrowthCount: Int? = null,
    val healthNotes: String? = null,
    val wateringAmountMl: Double? = null,
    val wateringFrequencyDays: Int? = null,
    val sunlightExposure: String? = null,
    val roomTemperatureC: Double? = null,
    val humidityPercent: Double? = null,
    val fertilizerType: String? = null,
    val fertilizerAmountMl: Double? = null,
    val pestPresence: String? = null,
    val pestSeverity: String? = null,
    val soilMoisturePercent: Double? = null,
    val soilType: String? = null,
    val healthScore: Int? = null
)
