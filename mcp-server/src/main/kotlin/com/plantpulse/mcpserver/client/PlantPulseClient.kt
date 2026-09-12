package com.plantpulse.mcpserver.client

import com.plantpulse.mcpserver.config.McpProperties
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.util.UUID

/**
 * Talks to plant-service and health-service directly (not through api-gateway), using the
 * internal PlantPulse JWT obtained via plantpulse_login. See mcp-server's README for why:
 * api-gateway only validates Keycloak JWTs, which this service never obtains.
 */
@Component
class PlantPulseClient(properties: McpProperties) {

    private val plantServiceClient = RestClient.create(properties.plantServiceUrl)
    private val healthServiceClient = RestClient.create(properties.healthServiceUrl)

    fun login(email: String, password: String): AuthResponse =
        plantServiceClient.post()
            .uri("/api/auth/login")
            .body(LoginRequest(email, password))
            .retrieve()
            .body(AuthResponse::class.java)
            ?: error("plant-service returned an empty login response")

    fun listPlants(jwt: String): List<PlantResponse> =
        plantServiceClient.get()
            .uri("/api/plants")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwt")
            .retrieve()
            .body(object : ParameterizedTypeReference<List<PlantResponse>>() {})
            ?: emptyList()

    fun logObservation(jwt: String, plantId: UUID, request: LogObservationRequest): ObservationResponse =
        plantServiceClient.post()
            .uri("/api/plants/{plantId}/observations", plantId)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwt")
            .body(request)
            .retrieve()
            .body(ObservationResponse::class.java)
            ?: error("plant-service returned an empty observation response")

    fun getDashboard(jwt: String): DashboardResponse =
        healthServiceClient.get()
            .uri("/api/health/dashboard")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwt")
            .retrieve()
            .body(DashboardResponse::class.java)
            ?: error("health-service returned an empty dashboard response")
}
