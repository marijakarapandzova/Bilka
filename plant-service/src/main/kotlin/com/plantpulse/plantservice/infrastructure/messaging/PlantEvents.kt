package com.plantpulse.plantservice.infrastructure.messaging

import com.plantpulse.plantservice.domain.plant.PlantId
import com.plantpulse.plantservice.domain.shared.AbstractEvent
import java.time.Instant
import java.util.UUID

/**
 * Kafka topics for Plant and related events.
 * Topic names follow the pattern derived from event class names:
 * PlantAddedEvent -> "plant.added", etc.
 */
object KafkaTopics {
    const val PLANT_ADDED = "plant.added"
    const val OBSERVATION_LOGGED = "observation.logged"
    const val PLANT_WATERED = "plant.watered"
    const val PLANT_REMOVED = "plant.removed"
}

/**
 * Intermediate abstract class for all Plant aggregate events.
 * Ensures every plant event carries the plantId and extends AbstractEvent.
 */
abstract class PlantEvent(
    open val plantId: PlantId
) : AbstractEvent(plantId)

// ============ PLANT EVENTS ============

/**
 * Published when a plant is added to the system.
 * External consumers (like Health Service) need to know about this to initialize health tracking.
 */
data class PlantAddedEvent(
    override val plantId: PlantId,
    val userId: UUID,
    val speciesId: UUID,
    val wateringFrequencyDays: Int,
    val addedAt: Instant = Instant.now()
) : PlantEvent(plantId) {

    /**
     * External representation of PlantAddedEvent.
     * Only includes information that external services need:
     * - which plant was added
     * - which user owns it
     * - which species it is
     * - how often it needs watering
     *
     * Internal details like addedAt timestamp are not published externally
     * because external consumers don't need to depend on our internal clock.
     */
    override fun toExternalEvent(): PlantAddedExternalEvent {
        return PlantAddedExternalEvent(
            plantId = this.plantId,
            userId = this.userId,
            speciesId = this.speciesId,
            wateringFrequencyDays = this.wateringFrequencyDays
        )
    }
}

/**
 * External representation of PlantAddedEvent.
 * This is what gets published to Kafka and what external services receive.
 */
data class PlantAddedExternalEvent(
    val plantId: PlantId,
    val userId: UUID,
    val speciesId: UUID,
    val wateringFrequencyDays: Int
)

/**
 * Published when an observation (disease or symptom diagnosis) is logged.
 * Health Service consumers listen to this to update plant health scores and detect outbreaks.
 * This event is published externally to trigger health analysis in other services.
 */
data class ObservationLoggedEvent(
    override val plantId: PlantId,
    val observationId: UUID,
    val userId: UUID,
    val diseaseMatchName: String,
    val diseaseMatchPercentage: Int,
    val cityLocation: String?,
    val loggedAt: Instant = Instant.now()
) : PlantEvent(plantId) {

    /**
     * External representation includes the disease information needed by Health Service
     * for health score updates and outbreak detection.
     * User ID is included so Health Service can track whose plant is affected.
     * cityLocation is included to support regional outbreak analysis.
     */
    override fun toExternalEvent(): ObservationLoggedExternalEvent {
        return ObservationLoggedExternalEvent(
            plantId = this.plantId,
            userId = this.userId,
            diseaseMatchName = this.diseaseMatchName,
            diseaseMatchPercentage = this.diseaseMatchPercentage,
            cityLocation = this.cityLocation
        )
    }
}

/**
 * External representation of ObservationLoggedEvent.
 */
data class ObservationLoggedExternalEvent(
    val plantId: PlantId,
    val userId: UUID,
    val diseaseMatchName: String,
    val diseaseMatchPercentage: Int,
    val cityLocation: String?
)

/**
 * Published when a plant is watered.
 * Internal event — Health Service doesn't need to know about every watering event.
 * toExternalEvent() is not overridden, so this event stays internal.
 */
data class PlantWateredEvent(
    override val plantId: PlantId,
    val userId: UUID,
    val wateredAt: Instant = Instant.now()
) : PlantEvent(plantId)

/**
 * Published when a plant is removed from the system.
 * Health Service should stop tracking this plant's health.
 * This event is published externally so Health Service can clean up its data.
 */
data class PlantRemovedEvent(
    override val plantId: PlantId,
    val userId: UUID,
    val removedAt: Instant = Instant.now()
) : PlantEvent(plantId) {

    /**
     * External representation — Health Service needs to know which plant to stop tracking.
     */
    override fun toExternalEvent(): PlantRemovedExternalEvent {
        return PlantRemovedExternalEvent(
            plantId = this.plantId,
            userId = this.userId
        )
    }
}

/**
 * External representation of PlantRemovedEvent.
 */
data class PlantRemovedExternalEvent(
    val plantId: PlantId,
    val userId: UUID
)
