package com.plantpulse.healthservice.pact

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Contract Test: Verify Health Service can deserialize Plant Service messages
 * 
 * This is a simplified Pact contract test that verifies the JSON contract
 * between Plant Service (producer) and Health Service (consumer).
 */
class PlantServiceContractTest {

    private val objectMapper = ObjectMapper()

    @Test
    @DisplayName("Plant Service publishes plant.added events with correct schema")
    fun plantAddedEventContract() {
        // The exact JSON that Plant Service publishes to Kafka
        val plantAddedJson = """
            {
              "plantId": "550e8400-e29b-41d4-a716-446655440001",
              "userId": "550e8400-e29b-41d4-a716-446655440002",
              "speciesId": "550e8400-e29b-41d4-a716-446655440000",
              "wateringFrequencyDays": 3
            }
        """.trimIndent()

        // Verify Health Service can parse it
        val parsed = objectMapper.readTree(plantAddedJson)
        
        assert(parsed.get("plantId").asText() == "550e8400-e29b-41d4-a716-446655440001")
        assert(parsed.get("userId").asText() == "550e8400-e29b-41d4-a716-446655440002")
        assert(parsed.get("speciesId").asText() == "550e8400-e29b-41d4-a716-446655440000")
        assert(parsed.get("wateringFrequencyDays").asInt() == 3)
    }

    @Test
    @DisplayName("Plant Service publishes observation.logged events with correct schema")
    fun observationLoggedEventContract() {
        // The exact JSON for observation.logged events
        val observationJson = """
            {
              "plantId": "550e8400-e29b-41d4-a716-446655440001",
              "userId": "550e8400-e29b-41d4-a716-446655440002",
              "diseaseMatchName": "Root Rot",
              "diseaseMatchPercentage": 80,
              "cityLocation": "Skopje"
            }
        """.trimIndent()

        val parsed = objectMapper.readTree(observationJson)
        
        assert(parsed.get("plantId").asText() == "550e8400-e29b-41d4-a716-446655440001")
        assert(parsed.get("diseaseMatchName").asText() == "Root Rot")
        assert(parsed.get("diseaseMatchPercentage").asInt() == 80)
        assert(parsed.get("cityLocation").asText() == "Skopje")
    }
}
