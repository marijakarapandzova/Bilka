package com.plantpulse.plantservice.infrastructure.messaging.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.plantpulse.plantservice.domain.shared.AbstractEvent
import com.plantpulse.plantservice.infrastructure.messaging.service.EventMessagingService
import org.springframework.stereotype.Component

/**
 * Generic event handler that publishes all AbstractEvent instances to the messaging system.
 *
 * This is a single handler for ALL events. Instead of writing a new @EventHandler for each
 * event type (as in the old approach), every event type that should be published externally
 * just needs to override toExternalEvent(). This scales elegantly as new events are added.
 *
 * How it works:
 * 1. The handler listens to AbstractEvent (the base class of all domain events)
 * 2. For each event, it calls toExternalEvent() to get the external representation
 * 3. If toExternalEvent() returns null, the event is not published (stays internal only)
 * 4. If it returns a non-null object, the handler serializes and publishes it
 * 5. The topic name is automatically derived from the event class name (e.g., PlantAddedEvent -> "plant.added")
 */
@Component
class EventMessagingEventHandler(
    private val eventMessagingService: EventMessagingService
) {
    private val objectMapper = ObjectMapper()
        .registerKotlinModule()
        .registerModule(JavaTimeModule())

    /**
     * Publishes external events to Kafka.
     * Called automatically by Spring whenever a bean publishes an event on the application event bus.
     * Note: Spring Events are not the same as domain events in Axon. This handler would typically
     * be used in a non-Axon system, or integrated with Axon's event bus via a dedicated event listener.
     * For Axon-based systems, consider using Axon's @EventHandler annotation instead.
     */
    fun publishEvent(event: AbstractEvent) {
        // Ask the event if it should be published externally
        val externalEvent = event.toExternalEvent() ?: return

        // Serialize the external event to JSON
        val eventJSON = objectMapper.writeValueAsString(externalEvent)

        // Publish using the auto-derived topic name and the aggregate's identifier as the key
        eventMessagingService.send(
            topic = event.eventTopic(),
            key = event.identifier.value.toString(),
            payload = eventJSON
        )
    }
}
