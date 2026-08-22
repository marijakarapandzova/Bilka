package com.plantpulse.plantservice.infrastructure.messaging

import com.plantpulse.plantservice.domain.plant.PlantId
import com.plantpulse.plantservice.infrastructure.messaging.events.PlantAddedEvent
import com.plantpulse.plantservice.infrastructure.messaging.events.ObservationLoggedEvent
import com.plantpulse.plantservice.infrastructure.messaging.events.PlantWateredEvent
import com.plantpulse.plantservice.infrastructure.messaging.events.PlantRemovedEvent
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation
import io.github.springwolf.kafka.annotation.KafkaAsyncOperationBinding
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * AsyncAPI Documentation for Plant Service Kafka Events
 *
 * This class documents all Kafka events that Plant Service publishes to other services.
 * These events are published by EventMessagingEventHandler during aggregate command processing.
 *
 * The methods are never called at runtime - they exist purely for Springwolf documentation generation.
 * The actual event publishing happens through EventMessagingEventHandler.
 *
 * Access documentation at: http://localhost:8081/springwolf/asyncapi-ui.html
 */
@Component
class KafkaEventDocumentation {

    /**
     * Documents the plant.added event published by Plant Service
     *
     * Published when: A new plant is added to the garden
     * Consumed by: Health Service (creates health profile)
     * Topic: plant.added
     *
     * This is a critical event that triggers health profile creation in downstream services.
     */
    @AsyncPublisher(
        operation = AsyncOperation(
            channelName = "plant.added",
            description = "Published when a new plant is added to a user's garden. Other services use this to initialize tracking.",
            payloadType = PlantAddedEvent::class
        )
    )
    @KafkaAsyncOperationBinding
    fun plantAdded(payload: PlantAddedEvent) {
        // This method is never called - it exists only for documentation
    }

    /**
     * Documents the observation.logged event published by Plant Service
     *
     * Published when: A disease or symptom observation is logged for a plant
     * Consumed by: Health Service (updates health scores, detects outbreaks)
     * Topic: observation.logged
     *
     * This event carries disease diagnosis data used for health assessment.
     */
    @AsyncPublisher(
        operation = AsyncOperation(
            channelName = "observation.logged",
            description = "Published when an observation (disease/symptom diagnosis) is recorded for a plant.",
            payloadType = ObservationLoggedEvent::class
        )
    )
    @KafkaAsyncOperationBinding
    fun observationLogged(payload: ObservationLoggedEvent) {
        // This method is never called - it exists only for documentation
    }

    /**
     * Documents the plant.watered event published by Plant Service
     *
     * Published when: A plant is watered
     * Consumed by: Health Service (may trigger reminders reset, health checks)
     * Topic: plant.watered
     *
     * Care-tracking event for update watering history and schedules.
     */
    @AsyncPublisher(
        operation = AsyncOperation(
            channelName = "plant.watered",
            description = "Published when a plant is watered, for tracking care history.",
            payloadType = PlantWateredEvent::class
        )
    )
    @KafkaAsyncOperationBinding
    fun plantWatered(payload: PlantWateredEvent) {
        // This method is never called - it exists only for documentation
    }

    /**
     * Documents the plant.removed event published by Plant Service
     *
     * Published when: A plant is deleted from the garden
     * Consumed by: Health Service (removes health profile, stops tracking)
     * Topic: plant.removed
     *
     * Cleanup event that ensures downstream services stop tracking the plant.
     */
    @AsyncPublisher(
        operation = AsyncOperation(
            channelName = "plant.removed",
            description = "Published when a plant is removed from a user's garden. Triggers cleanup in downstream services.",
            payloadType = PlantRemovedEvent::class
        )
    )
    @KafkaAsyncOperationBinding
    fun plantRemoved(payload: PlantRemovedEvent) {
        // This method is never called - it exists only for documentation
    }
}
