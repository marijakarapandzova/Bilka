package com.plantpulse.healthservice.application

import com.plantpulse.healthservice.config.HealthProperties
import com.plantpulse.healthservice.domain.health.DiseaseCareTips
import com.plantpulse.healthservice.domain.health.HealthScoreCalculator
import com.plantpulse.healthservice.domain.health.HealthSnapshot
import com.plantpulse.healthservice.domain.health.HealthSnapshotRepository
import com.plantpulse.healthservice.domain.health.HealthStatus
import com.plantpulse.healthservice.domain.health.SnapshotSource
import com.plantpulse.healthservice.domain.notification.NotificationType
import com.plantpulse.healthservice.domain.plant.PlantHealthProfile
import com.plantpulse.healthservice.domain.plant.PlantHealthProfileRepository
import com.plantpulse.healthservice.infrastructure.messaging.ObservationLoggedEvent
import com.plantpulse.healthservice.infrastructure.messaging.PlantAddedEvent
import com.plantpulse.healthservice.infrastructure.messaging.PlantRemovedEvent
import com.plantpulse.healthservice.infrastructure.messaging.PlantWateredEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Projects plant-service's domain events onto health-service's own read
 * model and reacts to them (health scoring, reminders eligibility, disease
 * alerts, outbreak checks). This is the single place event-driven state
 * changes happen — the Kafka listener is a thin adapter on top of it.
 */
@Service
class PlantHealthEventHandler(
    private val profileRepository: PlantHealthProfileRepository,
    private val snapshotRepository: HealthSnapshotRepository,
    private val scoreCalculator: HealthScoreCalculator,
    private val notificationService: NotificationService,
    private val outbreakDetectionService: OutbreakDetectionService,
    private val properties: HealthProperties
) {
    private val log = LoggerFactory.getLogger(PlantHealthEventHandler::class.java)

    @Transactional
    fun onPlantAdded(event: PlantAddedEvent) {
        val existing = profileRepository.findById(event.plantId).orElse(null)
        val profile = existing?.also {
            it.speciesName = event.speciesName
            it.nickname = event.nickname
            it.wateringFrequencyDays = event.wateringFrequencyDays
            it.cityLocation = event.cityLocation
            it.latitude = event.latitude
            it.longitude = event.longitude
            it.active = true
        } ?: PlantHealthProfile(
            id = event.plantId,
            userId = event.userId,
            speciesId = event.speciesId,
            speciesName = event.speciesName,
            nickname = event.nickname,
            cityLocation = event.cityLocation,
            latitude = event.latitude,
            longitude = event.longitude,
            wateringFrequencyDays = event.wateringFrequencyDays,
            addedAt = event.addedAt
        )
        profileRepository.save(profile)

        if (existing == null) {
            snapshotRepository.save(
                HealthSnapshot(
                    plantId = profile.id,
                    userId = profile.userId,
                    healthScore = profile.currentHealthScore,
                    status = profile.currentStatus,
                    diseaseName = "Healthy",
                    diseaseMatchPercentage = 100,
                    cityLocation = profile.cityLocation,
                    source = SnapshotSource.INITIAL,
                    recordedAt = profile.addedAt
                )
            )
        }
        log.info("Plant health profile upserted for plant {}", event.plantId)
    }

    @Transactional
    fun onPlantWatered(event: PlantWateredEvent) {
        val profile = profileRepository.findById(event.plantId).orElse(null)
        if (profile == null) {
            log.warn("Ignoring PlantWateredEvent for unknown plant {}", event.plantId)
            return
        }
        profile.markWatered(event.wateredAt)
        profileRepository.save(profile)
    }

    @Transactional
    fun onPlantRemoved(event: PlantRemovedEvent) {
        val profile = profileRepository.findById(event.plantId).orElse(null)
        if (profile == null) {
            log.warn("Ignoring PlantRemovedEvent for unknown plant {}", event.plantId)
            return
        }
        profile.markRemoved()
        profileRepository.save(profile)
    }

    @Transactional
    fun onObservationLogged(event: ObservationLoggedEvent) {
        val profile = profileRepository.findById(event.plantId).orElse(null)
        if (profile == null) {
            log.warn("Ignoring ObservationLoggedEvent for unknown plant {} (plant.added not yet processed?)", event.plantId)
            return
        }

        // Location can change/arrive after the plant was added (e.g. user sets city later).
        if (event.cityLocation != null) {
            profile.cityLocation = event.cityLocation
            profile.latitude = event.latitude
            profile.longitude = event.longitude
        }

        val previousSnapshot = snapshotRepository.findTopByPlantIdOrderByRecordedAtDesc(event.plantId)
        val newScore = scoreCalculator.scoreForDiseaseMatch(event.diseaseMatchName, event.diseaseMatchPercentage)
        val newStatus = scoreCalculator.deriveStatus(newScore, previousSnapshot)

        profile.applyObservation(
            healthScore = newScore,
            status = newStatus,
            diseaseName = event.diseaseMatchName,
            diseaseMatchPercentage = event.diseaseMatchPercentage,
            soilWaterlogged = event.soilMoisture.equals("WATERLOGGED", ignoreCase = true),
            observedAt = event.loggedAt
        )
        profileRepository.save(profile)

        snapshotRepository.save(
            HealthSnapshot(
                plantId = profile.id,
                userId = profile.userId,
                healthScore = newScore,
                status = newStatus,
                diseaseName = event.diseaseMatchName,
                diseaseMatchPercentage = event.diseaseMatchPercentage,
                cityLocation = profile.cityLocation,
                source = SnapshotSource.OBSERVATION,
                recordedAt = event.loggedAt
            )
        )

        raiseObservationNotifications(profile, previousSnapshot?.status, newStatus, event)

        if (profile.cityLocation != null &&
            !event.diseaseMatchName.equals("Healthy", ignoreCase = true) &&
            event.diseaseMatchPercentage >= properties.outbreakMinMatchPercentage
        ) {
            outbreakDetectionService.checkCityForDisease(profile.cityLocation!!, event.diseaseMatchName, event.loggedAt)
        }
    }

    private fun raiseObservationNotifications(
        profile: PlantHealthProfile,
        previousStatus: HealthStatus?,
        newStatus: HealthStatus,
        event: ObservationLoggedEvent
    ) {
        val today = event.loggedAt.toString().substring(0, 10)

        if (!event.diseaseMatchName.equals("Healthy", ignoreCase = true) &&
            event.diseaseMatchPercentage >= properties.diseaseAlertMinMatchPercentage
        ) {
            val steps = DiseaseCareTips.forDisease(event.diseaseMatchName).joinToString(separator = "\n") { "• $it" }
            notificationService.createIfAbsent(
                userId = profile.userId,
                plantId = profile.id,
                type = NotificationType.DISEASE_ALERT,
                title = "${profile.nickname} may have ${event.diseaseMatchName}",
                message = "Symptoms match: ${event.diseaseMatchPercentage}%\n\nWhat to do:\n$steps",
                dedupeKey = "DISEASE:${profile.id}:${event.observationId}"
            )
        }

        if (newStatus == HealthStatus.DECLINING && previousStatus != HealthStatus.DECLINING) {
            notificationService.createIfAbsent(
                userId = profile.userId,
                plantId = profile.id,
                type = NotificationType.HEALTH_DECLINE,
                title = "${profile.nickname}'s health is declining",
                message = "Health score dropped to ${profile.currentHealthScore}/100. Review the latest observation and treatment steps.",
                dedupeKey = "DECLINE:${profile.id}:$today"
            )
        }

        if (newStatus == HealthStatus.RECOVERING && previousStatus != HealthStatus.RECOVERING) {
            notificationService.createIfAbsent(
                userId = profile.userId,
                plantId = profile.id,
                type = NotificationType.RECOVERY,
                title = "${profile.nickname} is recovering",
                message = "Health score is back up to ${profile.currentHealthScore}/100. Keep up the current care routine.",
                dedupeKey = "RECOVERY:${profile.id}:$today"
            )
        }
    }
}
