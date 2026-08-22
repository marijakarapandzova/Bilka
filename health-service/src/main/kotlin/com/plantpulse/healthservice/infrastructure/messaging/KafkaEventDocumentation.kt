package com.plantpulse.healthservice.infrastructure.messaging

import com.plantpulse.healthservice.infrastructure.messaging.events.PlantAddedEvent
import com.plantpulse.healthservice.infrastructure.messaging.events.ObservationLoggedEvent
import com.plantpulse.healthservice.infrastructure.messaging.events.PlantRemovedEvent
import io.github.springwolf.core.asyncapi.annotations.AsyncListener
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher
import io.github.springwolf.kafka.annotation.KafkaAsyncOperationBinding
import org.springframework.stereotype.Component

/**
 * AsyncAPI Documentation for Health Service Kafka Events
 *
 * This class documents all Kafka events that Health Service consumes from other services.
 * These are CONSUMED events, not published events, but we document them here for reference.
 *
 * The methods are never called at runtime - they exist purely for Springwolf documentation generation.
 * The actual event consumption happens through PlantEventListener.
 *
 * Access documentation at: http://localhost:8082/springwolf/asyncapi-ui.html
 */
@Component
class KafkaEventDocumentation {

    /**
     * Documents the plant.added event consumed from Plant Service
     *
     * Published by: Plant Service
     * Consumed by: Health Service (PlantEventListener)
     * Topic: plant.added
     *
     * When Plant Service creates a new plant, this event is published.
     * Health Service listens and creates a corresponding health profile.
     */
    @AsyncListener(
        operation = AsyncOperation(
            channelName = "plant.added",
            description = "Consumed when a new plant is added in Plant Service. Health Service creates a health profile.",
            payloadType = PlantAddedEvent::class
        )
    )
    @KafkaAsyncOperationBinding
    fun plantAdded(payload: PlantAddedEvent) {
        // This method is never called - it exists only for documentation
    }

    /**
     * Documents the observation.logged event consumed from Plant Service
     *
     * Published by: Plant Service
     * Consumed by: Health Service (PlantEventListener)
     * Topic: observation.logged
     *
     * When Plant Service logs an observation (disease/symptom diagnosis),
     * this event is published. Health Service updates health scores and detects outbreaks.
     */
    @AsyncListener(
        operation = AsyncOperation(
            channelName = "observation.logged",
            description = "Consumed when a disease observation is logged in Plant Service. Health Service updates health scores.",
            payloadType = ObservationLoggedEvent::class
        )
    )
    @KafkaAsyncOperationBinding
    fun observationLogged(payload: ObservationLoggedEvent) {
        // This method is never called - it exists only for documentation
    }

    /**
     * Documents the plant.removed event consumed from Plant Service
     *
     * Published by: Plant Service
     * Consumed by: Health Service (PlantEventListener)
     * Topic: plant.removed
     *
     * When Plant Service deletes a plant, this event is published.
     * Health Service stops tracking health for that plant.
     */
    @AsyncListener(
        operation = AsyncOperation(
            channelName = "plant.removed",
            description = "Consumed when a plant is removed in Plant Service. Health Service stops tracking.",
            payloadType = PlantRemovedEvent::class
        )
    )
    @KafkaAsyncOperationBinding
    fun plantRemoved(payload: PlantRemovedEvent) {
        // This method is never called - it exists only for documentation
    }
}
