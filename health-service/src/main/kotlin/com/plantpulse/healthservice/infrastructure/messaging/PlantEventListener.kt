package com.plantpulse.healthservice.infrastructure.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.plantpulse.healthservice.application.PlantHealthEventHandler
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

/**
 * Consumes plant-service's domain events as raw JSON (StringDeserializer)
 * and maps each payload into health-service's own event DTOs before handing
 * off to PlantHealthEventHandler. Parsing manually — rather than trusting
 * Kafka type headers — keeps the two services free of any shared class
 * dependency.
 */
@Component
class PlantEventListener(
    private val objectMapper: ObjectMapper,
    private val eventHandler: PlantHealthEventHandler
) {
    private val log = LoggerFactory.getLogger(PlantEventListener::class.java)

    @KafkaListener(topics = [KafkaTopics.PLANT_ADDED], groupId = "\${spring.kafka.consumer.group-id}")
    fun onPlantAdded(payload: String) {
        handle(payload, "PlantAddedEvent") { eventHandler.onPlantAdded(objectMapper.readValue(payload, PlantAddedEvent::class.java)) }
    }

    @KafkaListener(topics = [KafkaTopics.OBSERVATION_LOGGED], groupId = "\${spring.kafka.consumer.group-id}")
    fun onObservationLogged(payload: String) {
        handle(payload, "ObservationLoggedEvent") {
            eventHandler.onObservationLogged(objectMapper.readValue(payload, ObservationLoggedEvent::class.java))
        }
    }

    @KafkaListener(topics = [KafkaTopics.PLANT_WATERED], groupId = "\${spring.kafka.consumer.group-id}")
    fun onPlantWatered(payload: String) {
        handle(payload, "PlantWateredEvent") { eventHandler.onPlantWatered(objectMapper.readValue(payload, PlantWateredEvent::class.java)) }
    }

    @KafkaListener(topics = [KafkaTopics.PLANT_REMOVED], groupId = "\${spring.kafka.consumer.group-id}")
    fun onPlantRemoved(payload: String) {
        handle(payload, "PlantRemovedEvent") { eventHandler.onPlantRemoved(objectMapper.readValue(payload, PlantRemovedEvent::class.java)) }
    }

    /**
     * A malformed/unexpected payload must never poison the consumer (no
     * infinite redelivery loop) — log and move on. Legitimate processing
     * errors from the handler are the caller's business logic to fix, not a
     * transport problem, so they're logged the same way here rather than
     * rethrown into the container's error handler.
     */
    private fun handle(payload: String, eventName: String, action: () -> Unit) {
        try {
            action()
        } catch (ex: Exception) {
            log.error("Failed to process {}: {} — payload={}", eventName, ex.message, payload, ex)
        }
    }
}
