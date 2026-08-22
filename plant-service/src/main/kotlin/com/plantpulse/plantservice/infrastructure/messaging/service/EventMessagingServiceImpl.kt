package com.plantpulse.plantservice.infrastructure.messaging.service

import com.plantpulse.plantservice.infrastructure.messaging.repository.EventMessagingRepository
import org.springframework.stereotype.Service

/**
 * Implementation of EventMessagingService that delegates to a repository.
 * This layer can add cross-cutting concerns like logging, metrics, etc.
 */
@Service
class EventMessagingServiceImpl(
    private val eventMessagingRepository: EventMessagingRepository
) : EventMessagingService {
    override fun send(topic: String, key: String, payload: String) {
        eventMessagingRepository.send(topic, key, payload)
    }
}
