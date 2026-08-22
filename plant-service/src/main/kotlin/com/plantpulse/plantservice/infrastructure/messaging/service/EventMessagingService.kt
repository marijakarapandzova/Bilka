package com.plantpulse.plantservice.infrastructure.messaging.service

/**
 * Service interface for publishing events to external messaging systems.
 * Decouples the domain layer from any specific messaging technology.
 * The domain layer should only know about this interface, not about Kafka.
 */
interface EventMessagingService {
    fun send(topic: String, key: String, payload: String)
}
