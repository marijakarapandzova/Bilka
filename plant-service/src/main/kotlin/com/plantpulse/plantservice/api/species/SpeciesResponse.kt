package com.plantpulse.plantservice.api.species

import com.plantpulse.plantservice.domain.species.CareDifficulty
import com.plantpulse.plantservice.domain.species.LightNeeds
import java.util.UUID

data class SpeciesResponse(
    val id: UUID,
    val name: String,
    val scientificName: String,
    val careDifficulty: CareDifficulty,
    val wateringFrequencyDays: Int,
    val lightNeeds: LightNeeds,
    val description: String?,
    val imageUrl: String? = null,
    val familyCommonName: String? = null,
    val lightLevel: Int? = null,
    val atmosphericHumidity: Int? = null,
    val wateringFrequency: String? = null,
    val temperatureMin: String? = null,
    val temperatureMax: String? = null,
    val phMin: String? = null,
    val phMax: String? = null,
    val soilDescription: String? = null,
    val flowerDescription: String? = null,
    val fruitDescription: String? = null,
    val growthHabit: String? = null,
    val nativeRange: String? = null,
    val fullTrefleData: String? = null,
    // CSV Data Fields
    val height_cm: Double? = null,
    val leaf_count: Int? = null,
    val new_growth_count: Int? = null,
    val health_notes: String? = null,
    val watering_amount_ml: Double? = null,
    val sunlight_exposure: String? = null,
    val roomTemperatureC: Double? = null,
    val humidity_percent: Double? = null,
    val fertilizer_type: String? = null,
    val fertilizer_amount_ml: Double? = null,
    val pest_presence: String? = null,
    val pest_severity: String? = null,
    val soilMoisturePercent: Double? = null,
    val soilType: String? = null,
    val health_score: Int? = null
)
