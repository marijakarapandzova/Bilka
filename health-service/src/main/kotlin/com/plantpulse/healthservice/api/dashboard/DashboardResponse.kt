package com.plantpulse.healthservice.api.dashboard

import com.plantpulse.healthservice.api.outbreak.RegionalOutbreakResponse
import com.plantpulse.healthservice.domain.plant.WateringUrgency
import java.time.Instant
import java.util.UUID

data class DashboardPlantCard(
    val plantId: UUID,
    val nickname: String,
    val speciesName: String,
    val healthScore: Int,
    val status: String,
    val wateringUrgency: WateringUrgency,
    val daysUntilWatering: Long,
    val nextWateringDate: Instant,
    val skipWatering: Boolean,
    val skipWateringReason: String?,
    val activeDisease: String?
)

data class DashboardResponse(
    val plants: List<DashboardPlantCard>,
    val unreadNotifications: Long,
    val activeRegionalOutbreaks: List<RegionalOutbreakResponse>
)
