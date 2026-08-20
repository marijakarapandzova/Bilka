package com.plantpulse.plantservice.api.plant

import java.time.Instant
import java.util.UUID

data class PlantResponse(
    val id: UUID,
    val speciesId: UUID,
    val speciesName: String,
    val nickname: String,
    val room: String?,
    val currentPhotoUrl: String?,
    val wateringFrequencyDays: Int,
    val addedAt: Instant,
    val lastWateredAt: Instant? = null,
    val photoMatchConfidencePercent: Int? = null
)
