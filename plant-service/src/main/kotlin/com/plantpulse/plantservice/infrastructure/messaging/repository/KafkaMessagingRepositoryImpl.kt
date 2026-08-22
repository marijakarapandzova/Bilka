package com.plantpulse.plantservice.infrastructure.messaging.repository

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Repository

/**
 * Kafka-specific adapter for publishing events to Kafka brokers.
 * This is the only place in the codebase that directly depends on KafkaTemplate.
 * If you wanted to switch to RabbitMQ, only this class would need to change.
 */
@Repository
class KafkaMessagingRepositoryImpl(
    private val kafkaTemplate: KafkaTemplate<String, String>
) : EventMessagingRepository {

    private val logger = LoggerFactory.getLogger(KafkaMessagingRepositoryImpl::class.java)

    override fun send(topic: String, key: String, payload: String) {
        kafkaTemplate.send(topic, key, payload)
            .whenComplete { result, ex ->
                if (ex != null) {
                    logger.error(
                        "Failed to publish event [{}] to Kafka: {}",
                        key, ex.message, ex
                    )
                } else {
                    logger.info(
                        "Published event [{}] to topic {} partition {} offset {}",
                        key,
                        result.recordMetadata.topic(),
                        result.recordMetadata.partition(),
                        result.recordMetadata.offset()
                    )
                }
            }
    }
}
