package com.plantpulse.plantservice.infrastructure.messaging.repository

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Repository

/**
 * Kafka adapter for the EventMessagingRepository port.
 * This is a concrete implementation of the Hexagonal Architecture adapter pattern.
 * It handles the technical details of sending events to Kafka topics.
 */
@Repository
class KafkaMessagingRepositoryImpl(
    private val kafkaTemplate: KafkaTemplate<String, String>
) : EventMessagingRepository {
    private val log = LoggerFactory.getLogger(KafkaMessagingRepositoryImpl::class.java)

    /**
     * Sends a message to a Kafka topic.
     *
     * @param topic The Kafka topic name (e.g., "plant.added")
     * @param key The message key for partitioning (e.g., plant ID)
     * @param payload The JSON payload to send
     */
    override fun send(topic: String, key: String, payload: String) {
        try {
            log.info("Sending message to Kafka topic: $topic with key: $key")
            log.debug("Payload: $payload")

            val sendResult = kafkaTemplate.send(topic, key, payload).get()

            log.info(
                "Successfully published event to topic {} partition {} offset {} with key {}",
                topic,
                sendResult.recordMetadata.partition(),
                sendResult.recordMetadata.offset(),
                key
            )
        } catch (ex: Exception) {
            log.error("Failed to publish event to Kafka topic {}: {}", topic, ex.message, ex)
            throw ex
        }
    }
}
