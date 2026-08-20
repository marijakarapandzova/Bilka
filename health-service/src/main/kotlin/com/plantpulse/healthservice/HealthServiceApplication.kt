package com.plantpulse.healthservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Health & Intelligence Service.
 *
 * Owns care scheduling, health scoring, watering reminders, disease/outbreak
 * notifications and regional outbreak detection. It never talks to
 * plant-service's database directly — it builds its own read model purely
 * from the domain events plant-service publishes to Kafka
 * (plant.added / plant.watered / observation.logged / plant.removed),
 * which keeps the two services independently deployable and scalable.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
class HealthServiceApplication

fun main(args: Array<String>) {
    runApplication<HealthServiceApplication>(*args)
}
