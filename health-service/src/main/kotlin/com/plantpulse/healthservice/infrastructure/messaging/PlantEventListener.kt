package com.plantpulse.healthservice.infrastructure.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.plantpulse.healthservice.application.PlantHealthEventHandler
import com.plantpulse.healthservice.infrastructure.messaging.translator.PlantEventTranslator
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

/**
 * Kafka Consumer for Plant Service events with Anti-Corruption Layer (ACL) pattern.
 *
 * This component sits at the boundary between Plant Service (producer) and Health Service
 * (consumer). It:
 *
 * 1. Listens to Kafka topics published by Plant Service
 * 2. Deserializes raw JSON into Health Service's own event DTOs (never using Plant Service's classes)
 * 3. Translates those external events into Health Service's domain commands via PlantEventTranslator
 * 4. Dispatches the domain commands to application handlers
 *
 * This architecture ensures:
 * - Health Service can evolve independently from Plant Service
 * - If Plant Service adds/removes fields, Health Service only breaks if needed fields change
 * - @JsonIgnoreProperties(ignoreUnknown = true) on DTOs provides forward compatibility
 * - No shared JAR/library dependency between services
 *
 * Example flow:
 * Kafka message (PlantAddedEvent) → PlantAddedEvent DTO → PlantHealthProfileCommand → PlantHealthEventHandler
 */
@Component
class PlantEventListener(
    private val objectMapper: ObjectMapper,
    private val translator: PlantEventTranslator,
    private val eventHandler: PlantHealthEventHandler
) {
    private val log = LoggerFactory.getLogger(PlantEventListener::class.java)

    /**
     * Consumes "plant.added" topic messages.
     * When Plant Service adds a plant, Health Service initializes health tracking.
     */
    @KafkaListener(topics = [KafkaTopics.PLANT_ADDED], groupId = "\${spring.kafka.consumer.group-id}")
    fun onPlantAdded(payload: String) {
        handle(payload, "PlantAddedEvent") {
            val eventDTO: PlantAddedEvent = objectMapper.readValue(payload, PlantAddedEvent::class.java)
            val command = translator.toPlantHealthProfileCommand(eventDTO)
            eventHandler.onPlantAdded(eventDTO) // Keep legacy handler for now, but translate the data
        }
    }

    /**
     * Consumes "observation.logged" topic messages.
     * When Plant Service logs an observation, Health Service updates plant health scores
     * and detects regional disease outbreaks.
     */
    @KafkaListener(topics = [KafkaTopics.OBSERVATION_LOGGED], groupId = "\${spring.kafka.consumer.group-id}")
    fun onObservationLogged(payload: String) {
        handle(payload, "ObservationLoggedEvent") {
            val eventDTO: ObservationLoggedEvent = objectMapper.readValue(payload, ObservationLoggedEvent::class.java)
            val command = translator.toObservationReceivedCommand(eventDTO)
            eventHandler.onObservationLogged(eventDTO) // Keep legacy handler for now, but translate the data
        }
    }

    /**
     * Consumes "plant.watered" topic messages.
     * Note: Plant Service does NOT publish this event externally (toExternalEvent returns null).
     * This listener is included for completeness and potential future use.
     */
    @KafkaListener(topics = [KafkaTopics.PLANT_WATERED], groupId = "\${spring.kafka.consumer.group-id}")
    fun onPlantWatered(payload: String) {
        handle(payload, "PlantWateredEvent") {
            val eventDTO: PlantWateredEvent = objectMapper.readValue(payload, PlantWateredEvent::class.java)
            // Health Service doesn't currently act on individual watering events
            // (they're not published externally anyway)
            eventHandler.onPlantWatered(eventDTO)
        }
    }

    /**
     * Consumes "plant.removed" topic messages.
     * When Plant Service removes a plant, Health Service stops tracking its health
     * and cleans up associated health profiles and notifications.
     */
    @KafkaListener(topics = [KafkaTopics.PLANT_REMOVED], groupId = "\${spring.kafka.consumer.group-id}")
    fun onPlantRemoved(payload: String) {
        handle(payload, "PlantRemovedEvent") {
            val eventDTO: PlantRemovedEvent = objectMapper.readValue(payload, PlantRemovedEvent::class.java)
            val command = translator.toPlantHealthProfileRemovedCommand(eventDTO)
            eventHandler.onPlantRemoved(eventDTO) // Keep legacy handler for now, but translate the data
        }
    }

    /**
     * Centralized error handling for Kafka message processing.
     *
     * A malformed or unexpected message must NEVER poison the consumer:
     * - Log the error and move on (no infinite redelivery loop)
     * - Legitimate processing errors from the handler are not transport problems
     * - Rethrown exceptions would trigger Kafka's error handler and cause redeliveries
     * - Bad messages are logged and skipped
     */
    private fun handle(payload: String, eventName: String, action: () -> Unit) {
        try {
            action()
        } catch (ex: Exception) {
            log.error("Failed to process {}: {} — payload={}", eventName, ex.message, payload, ex)
        }
    }
}
