package com.plantpulse.plantservice.api.shared

import java.time.Instant
import java.util.UUID

data class SharedPlantResponse(
    val id: UUID,
    val userId: UUID,
    val plantId: UUID,
    val plantName: String,
    val speciesName: String,
    val city: String,
    val photoUrl: String?,
    val userName: String,
    val sharedAt: Instant
)

data class SharePlantRequest(
    val plantId: UUID,
    val city: String,
    val photoUrl: String?
)

data class CityPlantsResponse(
    val city: String,
    val plants: List<SharedPlantResponse>
)
