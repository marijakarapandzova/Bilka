package com.plantpulse.mcpserver.client

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.Instant
import java.util.UUID

// These DTOs mirror response shapes from plant-service/health-service, but only carry the
// fields this MCP server actually uses. @JsonIgnoreProperties(ignoreUnknown = true) lets them
// deserialize the full real responses (which have more fields) without breaking.

// Mirrors plant-service's AuthResponse (api/auth/AuthResponse.kt)
@JsonIgnoreProperties(ignoreUnknown = true)
data class AuthResponse(val token: String, val userId: UUID, val email: String)

data class LoginRequest(val email: String, val password: String)

// Mirrors plant-service's PlantResponse (api/plant/PlantResponse.kt)
@JsonIgnoreProperties(ignoreUnknown = true)
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

// Mirrors plant-service's LogObservationRequest (api/observation/LogObservationRequest.kt).
// Only the fields the diagnose_plant_photo tool actually fills are included here;
// plant-service defaults the rest.
data class LogObservationRequest(
    val leafColor: String,
    val leafTexture: String,
    val soilMoisture: String,
    val visiblePests: Boolean,
    val growth: String,
    val notes: String? = null
)

// Mirrors plant-service's ObservationResponse (api/observation/ObservationResponse.kt)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DiseaseMatchResponse(val diseaseName: String, val matchPercentage: Int, val treatmentSteps: List<String>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ObservationResponse(
    val id: UUID,
    val plantId: UUID,
    val diseaseMatch: DiseaseMatchResponse,
    val healthScore: Int?
)

// Mirrors health-service's DashboardResponse (api/dashboard/DashboardResponse.kt)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DashboardPlantCard(
    val plantId: UUID,
    val nickname: String,
    val speciesName: String,
    val healthScore: Int,
    val status: String,
    val wateringUrgency: String,
    val daysUntilWatering: Long,
    val nextWateringDate: Instant,
    val skipWatering: Boolean,
    val skipWateringReason: String?,
    val activeDisease: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DashboardResponse(
    val plants: List<DashboardPlantCard>,
    val unreadNotifications: Long
)
