package com.plantpulse.healthservice.api.outbreak

import java.time.Instant
import java.util.UUID

data class RegionalOutbreakResponse(
    val id: UUID,
    val cityLocation: String,
    val diseaseName: String,
    val affectedPlantCount: Int,
    val windowStart: Instant,
    val windowEnd: Instant,
    val detectedAt: Instant
)
