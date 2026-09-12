package com.plantpulse.healthservice.infrastructure.messaging

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.UUID

/**
 * External Event DTOs for Health Service's consumption of Plant Service events.
 *
 * These DTOs represent the external events published by Plant Service.
 * They are deliberately NOT shared across services — each service owns its own
 * view of the contract. This decoupling allows Plant Service to evolve its
 * internal model freely as long as the JSON shape remains compatible.
 *
 * Using @JsonIgnoreProperties(ignoreUnknown = true) provides forward compatibility:
 * if Plant Service adds new fields, Health Service won't break.
 */

/**
 * Kafka topic names derived from event class names.
 * These match the pattern: EventName -> "event.name"
 * e.g., PlantAddedEvent -> "plant.added"
 */
object KafkaTopics {
    const val PLANT_ADDED = "plant.added"
    const val OBSERVATION_LOGGED = "observation.logged"
    const val PLANT_WATERED = "plant.watered"
    const val PLANT_REMOVED = "plant.removed"
}

/**
 * Published by Plant Service when a plant is added.
 * Health Service initializes health tracking for this plant.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PlantAddedEvent(
    val plantId: Any,  // Can be UUID string or wrapped {"value": "..."}
    val userId: UUID,
    val speciesId: UUID,
    val wateringFrequencyDays: Int
) {
    fun getPlantId(): UUID {
        return when (plantId) {
            is UUID -> plantId
            is String -> UUID.fromString(plantId)
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                val map = plantId as Map<String, Any>
                UUID.fromString(map["value"].toString())
            }
            else -> UUID.fromString(plantId.toString())
        }
    }
}

/**
 * Published by Plant Service when an observation (disease/symptom diagnosis) is logged.
 * Health Service updates plant health scores and detects regional outbreaks.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ObservationLoggedEvent(
    val plantId: Any, // Can be UUID string or wrapped {"value": "..."}
    val userId: UUID,
    val diseaseMatchName: String,
    val diseaseMatchPercentage: Int,
    val cityLocation: String?
) {
    fun getPlantId(): UUID {
        return when (plantId) {
            is UUID -> plantId
            is String -> UUID.fromString(plantId)
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                val map = plantId as Map<String, Any>
                UUID.fromString(map["value"].toString())
            }
            else -> UUID.fromString(plantId.toString())
        }
    }
}

/**
 * Published when a plant is watered.
 * This is an internal-only event from Plant Service — not published externally.
 * Health Service doesn't need every watering event for its health tracking.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PlantWateredEvent(
    val plantId: Any,  // Can be UUID string or wrapped {"value": "..."}
    val userId: UUID
) {
    fun getPlantId(): UUID {
        return when (plantId) {
            is UUID -> plantId
            is String -> UUID.fromString(plantId)
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                val map = plantId as Map<String, Any>
                UUID.fromString(map["value"].toString())
            }
            else -> UUID.fromString(plantId.toString())
        }
    }
}

/**
 * Published by Plant Service when a plant is removed.
 * Health Service stops tracking health for this plant.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PlantRemovedEvent(
    val plantId: Any,  // Can be UUID string or wrapped {"value": "..."}
    val userId: UUID
) {
    fun getPlantId(): UUID {
        return when (plantId) {
            is UUID -> plantId
            is String -> UUID.fromString(plantId)
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                val map = plantId as Map<String, Any>
                UUID.fromString(map["value"].toString())
            }
            else -> UUID.fromString(plantId.toString())
        }
    }
}
