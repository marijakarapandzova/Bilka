package com.plantpulse.plantservice.domain.shared

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Base class for all domain events.
 * Provides automatic event type naming, topic derivation, and external event projection.
 */
abstract class AbstractEvent(open val identifier: Identifier<out Any>) {

    /**
     * Returns the event type (class name), automatically included in JSON serialization.
     * Used by consumers to route events to the correct handler.
     */
    @JsonProperty("_eventType")
    fun eventType(): String = this.javaClass.simpleName

    /**
     * Derives the Kafka topic name from the event class name.
     * Examples:
     * - PlantAddedEvent → "plant.added"
     * - ObservationLoggedEvent → "observation.logged"
     * - PlantRemovedEvent → "plant.removed"
     *
     * This ensures topic names are automatically consistent across producer and consumer.
     */
    @JsonIgnore
    fun eventTopic(): String =
        this.javaClass.simpleName
            .removeSuffix("Event")
            .replace(Regex("([a-z])([A-Z])"), "$1.$2")
            .lowercase()

    /**
     * Override this method in events that should be published externally to Kafka.
     * Returning null means "do not publish this event" — it stays internal only.
     *
     * This allows each event to control what data is exposed to other microservices,
     * implementing the Anti-Corruption Layer pattern at the event level.
     */
    @JsonIgnore
    open fun toExternalEvent(): Any? = null
}
