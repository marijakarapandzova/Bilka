package com.plantpulse.healthservice.infrastructure.feign

import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.core.model.RequestResponsePact
import au.com.dius.pact.core.model.annotations.Pact
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.util.*

/**
 * Pact Consumer Test: Health Service calling Plant Service REST endpoints
 *
 * This test defines contracts for three HTTP interactions:
 * 1. GET /api/plants/{plantId} - Validate plant exists
 * 2. GET /api/species/{speciesId} - Get species details
 * 3. GET /api/plants/{plantId}/validate - Validate plant ownership
 *
 * No @SpringBootTest needed: Pact starts a mock HTTP server for us.
 * The test uses plain RestTemplate to call the mock, verifying that
 * Health Service's PlantServiceFeignClient can parse the responses.
 *
 * Generated pact file: target/pacts/health_consumer_plant_service.json
 */
@ExtendWith(PactConsumerTestExt::class)
@PactTestFor(providerName = "plant_service_provider", hostInterface = "localhost")
class PactPlantServiceConsumerTest {

    companion object {
        private val PLANT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
        private val USER_ID = UUID.fromString("770e8400-e29b-41d4-a716-446655440000")
        private val SPECIES_ID = UUID.fromString("660e8400-e29b-41d4-a716-446655440000")
    }

    // ============================================================
    // Contract 1: GET /api/plants/{plantId}
    // ============================================================

    @Pact(consumer = "health_consumer_plant_service")
    fun validatePlantExistsPact(builder: PactDslWithProvider): RequestResponsePact {
        val responseBody = PactDslJsonBody()
            .stringValue("id", PLANT_ID.toString())
            .stringValue("speciesId", SPECIES_ID.toString())
            .stringValue("speciesName", "Monstera Deliciosa")
            .stringValue("nickname", "My Monstera")
            .numberValue("wateringFrequencyDays", 7)
            .stringValue("addedAt", "2026-08-22T10:30:00Z")

        return builder
            .given("Plant with ID $PLANT_ID exists")
            .uponReceiving("A request to validate plant exists")
            .path("/api/plants/$PLANT_ID")
            .method("GET")
            .willRespondWith()
            .status(200)
            .headers(mapOf("Content-Type" to "application/json"))
            .body(responseBody)
            .toPact()
    }

    @Test
    @PactTestFor(pactMethod = "validatePlantExistsPact", port = "9999")
    fun validatePlantExists_returns200WithPlantDetails(mockServer: au.com.dius.pact.consumer.MockServer) {
        // Arrange
        val mockUrl = mockServer.getUrl()
        val response: ResponseEntity<PlantDto> = RestTemplate().getForEntity(
            "$mockUrl/api/plants/$PLANT_ID",
            PlantDto::class.java
        )

        // Assert
        assertThat(response.statusCodeValue).isEqualTo(200)
        assertThat(response.body).isNotNull
        assertThat(response.body!!.id).isEqualTo(PLANT_ID)
        assertThat(response.body!!.speciesName).isEqualTo("Monstera Deliciosa")
        assertThat(response.body!!.wateringFrequencyDays).isEqualTo(7)
    }

    // ============================================================
    // Contract 2: GET /api/species/{speciesId}
    // ============================================================

    @Pact(consumer = "health_consumer_plant_service")
    fun getSpeciesPact(builder: PactDslWithProvider): RequestResponsePact {
        val responseBody = PactDslJsonBody()
            .stringValue("id", SPECIES_ID.toString())
            .stringValue("name", "Monstera Deliciosa")
            .stringValue("scientificName", "Monstera deliciosa")
            .numberValue("wateringFrequencyDays", 7)
            .stringValue("careDifficulty", "MEDIUM")

        return builder
            .given("Species with ID $SPECIES_ID exists")
            .uponReceiving("A request to fetch species details")
            .path("/api/species/$SPECIES_ID")
            .method("GET")
            .willRespondWith()
            .status(200)
            .headers(mapOf("Content-Type" to "application/json"))
            .body(responseBody)
            .toPact()
    }

    @Test
    @PactTestFor(pactMethod = "getSpeciesPact", port = "9999")
    fun getSpecies_returns200WithSpeciesDetails(mockServer: au.com.dius.pact.consumer.MockServer) {
        // Arrange
        val mockUrl = mockServer.getUrl()
        val response: ResponseEntity<SpeciesDto> = RestTemplate().getForEntity(
            "$mockUrl/api/species/$SPECIES_ID",
            SpeciesDto::class.java
        )

        // Assert
        assertThat(response.statusCodeValue).isEqualTo(200)
        assertThat(response.body).isNotNull
        assertThat(response.body!!.id).isEqualTo(SPECIES_ID)
        assertThat(response.body!!.name).isEqualTo("Monstera Deliciosa")
        assertThat(response.body!!.wateringFrequencyDays).isEqualTo(7)
        assertThat(response.body!!.careDifficulty).isEqualTo("MEDIUM")
    }

    // ============================================================
    // Contract 3: GET /api/plants/{plantId}/validate
    // ============================================================

    @Pact(consumer = "health_consumer_plant_service")
    fun validatePlantOwnershipPact(builder: PactDslWithProvider): RequestResponsePact {
        val responseBody = PactDslJsonBody()
            .booleanValue("valid", true)
            .stringValue("message", "Plant belongs to user")

        return builder
            .given("Plant $PLANT_ID belongs to user $USER_ID")
            .uponReceiving("A request to validate plant ownership")
            .path("/api/plants/$PLANT_ID/validate")
            .method("GET")
            .query("userId=$USER_ID")
            .willRespondWith()
            .status(200)
            .headers(mapOf("Content-Type" to "application/json"))
            .body(responseBody)
            .toPact()
    }

    @Test
    @PactTestFor(pactMethod = "validatePlantOwnershipPact", port = "9999")
    fun validatePlantOwnership_returns200WithValidationResult(mockServer: au.com.dius.pact.consumer.MockServer) {
        // Arrange
        val mockUrl = mockServer.getUrl()
        val response: ResponseEntity<ValidationResultDto> = RestTemplate().getForEntity(
            "$mockUrl/api/plants/$PLANT_ID/validate?userId=$USER_ID",
            ValidationResultDto::class.java
        )

        // Assert
        assertThat(response.statusCodeValue).isEqualTo(200)
        assertThat(response.body).isNotNull
        assertThat(response.body!!.valid).isTrue
        assertThat(response.body!!.message).contains("belongs to user")
    }
}
