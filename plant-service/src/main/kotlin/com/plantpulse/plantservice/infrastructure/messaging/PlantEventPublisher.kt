package com.plantpulse.plantservice.infrastructure.messaging

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

object KafkaTopics {
    const val PLANT_ADDED = "plant.added"
    const val OBSERVATION_LOGGED = "observation.logged"
    const val PLANT_REMOVED = "plant.removed"
}

@Component
class PlantEventPublisher(
    @Autowired(required = false)
    private val kafkaTemplate: KafkaTemplate<String, Any>?
) {
    private val log = LoggerFactory.getLogger(PlantEventPublisher::class.java)

    fun publishPlantAdded(event: PlantAddedEvent) {
        if (kafkaTemplate == null) return
        log.info("Publishing PlantAddedEvent for plant {}", event.plantId)
        kafkaTemplate.send(KafkaTopics.PLANT_ADDED, event.plantId.toString(), event)
    }

    fun publishObservationLogged(event: ObservationLoggedEvent) {
        if (kafkaTemplate == null) return
        log.info("Publishing ObservationLoggedEvent for plant {}", event.plantId)
        kafkaTemplate.send(KafkaTopics.OBSERVATION_LOGGED, event.plantId.toString(), event)
    }

    fun publishPlantRemoved(event: PlantRemovedEvent) {
        if (kafkaTemplate == null) return
        log.info("Publishing PlantRemovedEvent for plant {}", event.plantId)
        kafkaTemplate.send(KafkaTopics.PLANT_REMOVED, event.plantId.toString(), event)
    }
}
