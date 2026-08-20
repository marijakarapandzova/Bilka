package com.plantpulse.healthservice.infrastructure.messaging

import java.time.Instant
import java.util.UUID

/**
 * Structural copies of the events plant-service publishes. Deliberately not
 * a shared library dependency between the two services — each service owns
 * its own view of the contract, which is the point of choreographed,
 * event-driven microservices: plant-service can evolve its internal event
 * classes freely as long as this JSON shape stays compatible.
 */

object KafkaTopics {
    const val PLANT_ADDED = "plant.added"
    const val OBSERVATION_LOGGED = "observation.logged"
    const val PLANT_WATERED = "plant.watered"
    const val PLANT_REMOVED = "plant.removed"
}

data class PlantAddedEvent(
    val plantId: UUID,
    val userId: UUID,
    val speciesId: UUID,
    val speciesName: String,
    val nickname: String,
    val wateringFrequencyDays: Int,
    val cityLocation: String?,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val addedAt: Instant = Instant.now()
)

data class ObservationLoggedEvent(
    val observationId: UUID,
    val plantId: UUID,
    val userId: UUID,
    val diseaseMatchName: String,
    val diseaseMatchPercentage: Int,
    val leafColor: String,
    val leafTexture: String,
    val soilMoisture: String,
    val growth: String,
    val visiblePests: Boolean,
    val cityLocation: String?,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val loggedAt: Instant = Instant.now()
)

data class PlantWateredEvent(
    val plantId: UUID,
    val userId: UUID,
    val wateredAt: Instant = Instant.now()
)

data class PlantRemovedEvent(
    val plantId: UUID,
    val userId: UUID,
    val removedAt: Instant = Instant.now()
)
