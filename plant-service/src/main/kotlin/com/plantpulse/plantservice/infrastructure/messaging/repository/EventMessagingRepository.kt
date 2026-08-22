package com.plantpulse.plantservice.infrastructure.messaging.repository

/**
 * Repository interface for publishing events to external messaging systems.
 * This is a port in the Hexagonal Architecture (Ports & Adapters) pattern.
 * Concrete implementations (e.g., Kafka, RabbitMQ, in-memory for testing) are adapters.
 */
interface EventMessagingRepository {
    fun send(topic: String, key: String, payload: String)
}
