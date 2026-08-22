package com.plantpulse.plantservice.infrastructure.messaging

import com.plantpulse.plantservice.infrastructure.messaging.service.EventMessagingService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Legacy event publisher for backward compatibility.
 * In the new architecture, events publish themselves via toExternalEvent().
 * This class is kept for transition purposes but delegates to the new EventMessagingService.
 *
 * For new code, consider directly publishing events through a domain-driven approach,
 * letting the EventMessagingEventHandler process them automatically.
 */
@Component
class PlantEventPublisher(
    private val eventMessagingService: EventMessagingService
) {
    private val log = LoggerFactory.getLogger(PlantEventPublisher::class.java)

    fun publishPlantAdded(event: PlantAddedEvent) {
        publishEvent(event)
    }

    fun publishObservationLogged(event: ObservationLoggedEvent) {
        publishEvent(event)
    }

    fun publishPlantRemoved(event: PlantRemovedEvent) {
        publishEvent(event)
    }

    private fun publishEvent(event: PlantEvent) {
        // Get the external event representation
        val externalEvent = event.toExternalEvent() ?: return

        log.info("Publishing {} to Kafka topic {}", event.javaClass.simpleName, event.eventTopic())

        try {
            eventMessagingService.send(
                topic = event.eventTopic(),
                key = event.identifier.value.toString(),
                payload = com.fasterxml.jackson.databind.ObjectMapper()
                    .registerModule(com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                    .writeValueAsString(externalEvent)
            )
        } catch (ex: Exception) {
            log.error("Failed to publish event: {}", ex.message, ex)
        }
    }
}
