package com.plantpulse.plantservice.infrastructure.messaging

import au.com.dius.pact.provider.junit5.AmpqTestTarget
import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider
import au.com.dius.pact.provider.junit5.Provider
import au.com.dius.pact.provider.junit5.loader.PactFolder
import com.fasterxml.jackson.databind.ObjectMapper
import com.plantpulse.plantservice.core.MessageAndMetadata
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

/**
 * Pact Provider Test: Plant Service publishing Kafka events
 *
 * Verifies that Plant Service's published messages satisfy the contracts defined by Health Service.
 *
 * Plant Service publishes three events to Kafka:
 * 1. plant.added - When a plant is added to the system
 * 2. observation.logged - When a disease/symptom observation is recorded
 * 3. plant.removed - When a plant is removed from the system
 *
 * How it works:
 * 1. Loads pact files from src/test/resources/pacts/
 * 2. For each message interaction defined in the pact:
 *    a. Calls @PactVerifyProvider method to generate the message
 *    b. Serializes the message to JSON bytes
 *    c. Verifies the bytes match what the pact expects (exact field names, types, values)
 *
 * AmpqTestTarget (Async Message):
 * - No HTTP server needed
 * - No Spring context needed
 * - Pact calls the @PactVerifyProvider method directly via reflection
 * - The method returns bytes; Pact compares them to the pact contract
 *
 * Generated pact file location (from Health Service):
 * target/pacts/health_consumer_kafka-plant_service_provider_kafka.json
 *
 * Should be copied to: src/test/resources/pacts/health_consumer_kafka-plant_service_provider_kafka.json
 */
@Provider("plant_service_provider_kafka")
@au.com.dius.pact.provider.junit5.Consumer("health_consumer_kafka")
@PactFolder("pacts")
class PactPlantEventProducerTest {

    private val objectMapper = ObjectMapper()

    companion object {
        private val PLANT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
        private val USER_ID = UUID.fromString("770e8400-e29b-41d4-a716-446655440000")
        private val SPECIES_ID = UUID.fromString("660e8400-e29b-41d4-a716-446655440000")
    }

    /**
     * Configure Pact to use AmpqTestTarget (async message testing).
     * No HTTP: Pact will call our @PactVerifyProvider methods directly.
     */
    @BeforeEach
    fun before(context: PactVerificationContext) {
        context.setTarget(AmpqTestTarget())
    }

    /**
     * Standard Pact provider test template.
     * @TestTemplate with PactVerificationInvocationContextProvider generates one test
     * per message interaction found in the loaded pact file.
     */
    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider::class)
    fun pactVerificationTestTemplate(context: PactVerificationContext) {
        context.verifyInteraction()
    }

    // ============================================================
    // Message Verification Methods
    // ============================================================
    // Each @PactVerifyProvider method corresponds to an expectsToReceive(...)
    // in the consumer test. The method name doesn't matter, but the argument
    // to @PactVerifyProvider must match the expectsToReceive(...) exactly.

    /**
     * Verifies: plant.added event
     * Called by Pact when verifying the "Plant Added Event" interaction.
     * Returns the JSON bytes that Plant Service would publish.
     */
    @au.com.dius.pact.provider.junit5.PactVerifyProvider("Plant Added Event")
    fun verifyPlantAddedMessage(): MessageAndMetadata {
        // Create the event DTO that Plant Service would serialize
        val event = PlantAddedExternalEvent(
            plantId = PLANT_ID,
            userId = USER_ID,
            speciesId = SPECIES_ID,
            wateringFrequencyDays = 7
        )

        // Serialize to JSON bytes
        val jsonBytes = objectMapper.writeValueAsString(event).toByteArray()

        // Return with content type metadata
        return MessageAndMetadata(
            jsonBytes,
            mapOf("contentType" to "application/json")
        )
    }

    /**
     * Verifies: observation.logged event
     * Called by Pact when verifying the "Observation Logged Event" interaction.
     */
    @au.com.dius.pact.provider.junit5.PactVerifyProvider("Observation Logged Event")
    fun verifyObservationLoggedMessage(): MessageAndMetadata {
        // Create the event DTO that Plant Service would serialize
        val event = ObservationLoggedExternalEvent(
            plantId = PLANT_ID,
            userId = USER_ID,
            diseaseMatchName = "Leaf Spot",
            diseaseMatchPercentage = 85,
            cityLocation = "Skopje"
        )

        // Serialize to JSON bytes
        val jsonBytes = objectMapper.writeValueAsString(event).toByteArray()

        // Return with content type metadata
        return MessageAndMetadata(
            jsonBytes,
            mapOf("contentType" to "application/json")
        )
    }

    /**
     * Verifies: plant.removed event
     * Called by Pact when verifying the "Plant Removed Event" interaction.
     */
    @au.com.dius.pact.provider.junit5.PactVerifyProvider("Plant Removed Event")
    fun verifyPlantRemovedMessage(): MessageAndMetadata {
        // Create the event DTO that Plant Service would serialize
        val event = PlantRemovedExternalEvent(
            plantId = PLANT_ID,
            userId = USER_ID
        )

        // Serialize to JSON bytes
        val jsonBytes = objectMapper.writeValueAsString(event).toByteArray()

        // Return with content type metadata
        return MessageAndMetadata(
            jsonBytes,
            mapOf("contentType" to "application/json")
        )
    }
}

/**
 * Helper class: Wraps message bytes and metadata for Pact
 */
data class MessageAndMetadata(
    val payload: ByteArray,
    val metadata: Map<String, String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageAndMetadata

        if (!payload.contentEquals(other.payload)) return false
        if (metadata != other.metadata) return false

        return true
    }

    override fun hashCode(): Int {
        var result = payload.contentHashCode()
        result = 31 * result + metadata.hashCode()
        return result
    }
}
