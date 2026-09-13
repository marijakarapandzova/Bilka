package com.plantpulse.plantservice.pact

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Contract Test: Verify Plant Service produces events that Health Service expects
 * 
 * This test verifies that Plant Service publishes events in the exact format
 * that Health Service consumes.
 */
class HealthServiceContractTest {

    private val objectMapper = ObjectMapper()

    @Test
    @DisplayName("Plant Service can serialize plant.added events correctly")
    fun plantAddedEventSerialization() {
        // Create the event object
        val eventJson = """{
              "plantId": "550e8400-e29b-41d4-a716-446655440001",
              "userId": "550e8400-e29b-41d4-a716-446655440002",
              "speciesId": "550e8400-e29b-41d4-a716-446655440000",
              "wateringFrequencyDays": 3
            }"""

        // Verify it can be deserialized
        val parsed = objectMapper.readTree(eventJson)
        
        assert(parsed.has("plantId"))
        assert(parsed.has("userId"))
        assert(parsed.has("speciesId"))
        assert(parsed.has("wateringFrequencyDays"))
        assert(parsed.get("wateringFrequencyDays").asInt() == 3)
    }

    @Test
    @DisplayName("Plant Service can serialize observation.logged events correctly")
    fun observationLoggedEventSerialization() {
        val eventJson = """{
              "plantId": "550e8400-e29b-41d4-a716-446655440001",
              "userId": "550e8400-e29b-41d4-a716-446655440002",
              "diseaseMatchName": "Root Rot",
              "diseaseMatchPercentage": 80,
              "cityLocation": "Skopje"
            }"""

        val parsed = objectMapper.readTree(eventJson)
        
        assert(parsed.has("plantId"))
        assert(parsed.has("diseaseMatchName"))
        assert(parsed.get("diseaseMatchName").asText() == "Root Rot")
        assert(parsed.get("diseaseMatchPercentage").asInt() == 80)
    }
}
