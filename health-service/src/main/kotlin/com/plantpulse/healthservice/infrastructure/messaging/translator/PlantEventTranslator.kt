package com.plantpulse.healthservice.infrastructure.messaging.translator

import com.plantpulse.healthservice.infrastructure.messaging.ObservationLoggedEvent
import com.plantpulse.healthservice.infrastructure.messaging.PlantAddedEvent
import com.plantpulse.healthservice.infrastructure.messaging.PlantRemovedEvent
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Anti-Corruption Layer (ACL) Translator for Health Service.
 *
 * This translator sits at the boundary between Plant Service's domain model
 * and Health Service's domain model. It converts external events (DTO-like)
 * from Plant Service into domain commands that Health Service understands.
 *
 * Why use a translator?
 * - Plant Service and Health Service are separate Bounded Contexts in DDD terminology
 * - Each has its own Ubiquitous Language (vocabulary and business rules)
 * - Without an ACL, Health Service would depend on Plant Service's classes,
 *   creating tight coupling
 * - With an ACL, each service can evolve independently
 *
 * Example:
 * Plant Service says: "PlantAddedEvent with plantId, userId, speciesId"
 * Health Service needs: "Initialize health tracking for this plant with watering requirements"
 * The translator bridges this gap.
 */
@Service
class PlantEventTranslator {

    /**
     * Translates PlantAddedEvent from Plant Service into Health Service's domain understanding.
     *
     * Plant Service publishes: "I added a new plant with this species and user"
     * Health Service interprets: "I need to create a health profile for this plant and initialize monitoring"
     */
    fun toPlantHealthProfileCommand(event: PlantAddedEvent): PlantHealthProfileCommand {
        return PlantHealthProfileCommand(
            plantId = event.plantId,
            userId = event.userId,
            speciesId = event.speciesId,
            wateringFrequencyDays = event.wateringFrequencyDays
        )
    }

    /**
     * Translates ObservationLoggedEvent from Plant Service into Health Service's domain understanding.
     *
     * Plant Service publishes: "I logged an observation with disease detection"
     * Health Service interprets: "I need to update this plant's health score and check for outbreaks"
     */
    fun toObservationReceivedCommand(event: ObservationLoggedEvent): ObservationReceivedCommand {
        return ObservationReceivedCommand(
            plantId = event.plantId,
            userId = event.userId,
            diseaseMatchName = event.diseaseMatchName,
            diseaseMatchPercentage = event.diseaseMatchPercentage,
            cityLocation = event.cityLocation
        )
    }

    /**
     * Translates PlantRemovedEvent from Plant Service into Health Service's domain understanding.
     *
     * Plant Service publishes: "I deleted this plant"
     * Health Service interprets: "I should stop tracking health for this plant and clean up its data"
     */
    fun toPlantHealthProfileRemovedCommand(event: PlantRemovedEvent): PlantHealthProfileRemovedCommand {
        return PlantHealthProfileRemovedCommand(
            plantId = event.plantId,
            userId = event.userId
        )
    }
}

/**
 * Domain command in Health Service's Ubiquitous Language.
 * This is what the health service understands and acts upon.
 */
data class PlantHealthProfileCommand(
    val plantId: UUID,
    val userId: UUID,
    val speciesId: UUID,
    val wateringFrequencyDays: Int
)

/**
 * Domain command in Health Service's Ubiquitous Language.
 * Represents the fact that an observation was received that affects health assessment.
 */
data class ObservationReceivedCommand(
    val plantId: UUID,
    val userId: UUID,
    val diseaseMatchName: String,
    val diseaseMatchPercentage: Int,
    val cityLocation: String?
)

/**
 * Domain command in Health Service's Ubiquitous Language.
 * Represents the need to stop tracking a plant's health.
 */
data class PlantHealthProfileRemovedCommand(
    val plantId: UUID,
    val userId: UUID
)
