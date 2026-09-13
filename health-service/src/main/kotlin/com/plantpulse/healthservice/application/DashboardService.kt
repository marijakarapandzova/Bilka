package com.plantpulse.healthservice.application

import com.plantpulse.healthservice.api.dashboard.DashboardPlantCard
import com.plantpulse.healthservice.api.dashboard.DashboardResponse
import com.plantpulse.healthservice.api.outbreak.RegionalOutbreakResponse
import com.plantpulse.healthservice.api.plant.CareScheduleResponse
import com.plantpulse.healthservice.api.plant.HealthSnapshotResponse
import com.plantpulse.healthservice.api.plant.PlantHealthResponse
import com.plantpulse.healthservice.domain.health.DiseaseCareTips
import com.plantpulse.healthservice.domain.health.HealthSnapshotRepository
import com.plantpulse.healthservice.domain.plant.PlantHealthProfile
import com.plantpulse.healthservice.domain.plant.PlantHealthProfileRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class DashboardService(
    private val profileRepository: PlantHealthProfileRepository,
    private val snapshotRepository: HealthSnapshotRepository,
    private val outbreakDetectionService: OutbreakDetectionService,
    private val careScheduleService: CareScheduleService,
    private val notificationService: NotificationService
) {

    fun dashboardFor(userId: UUID): DashboardResponse {
        val plants = profileRepository.findByUserIdAndActiveTrue(userId)

        val cities = plants.mapNotNull { it.cityLocation }.toSet()
        val activeOutbreaks = cities.flatMap { outbreakDetectionService.activeOutbreaksForCity(it) }
            .map { it.toResponse() }

        return DashboardResponse(
            plants = plants.map { it.toCard() },
            unreadNotifications = notificationService.unreadCount(userId),
            activeRegionalOutbreaks = activeOutbreaks
        )
    }

    fun plantHealth(plantId: UUID, userId: UUID): PlantHealthResponse {
        val profile = profileRepository.findByIdAndUserId(plantId, userId)
        return if (profile != null) {
            profile.toDetail()
        } else {
            // Return default health response for plants without a profile yet
            // This allows frontend to show initial 75/100 score until observations are logged
            val now = java.time.Instant.now()
            PlantHealthResponse(
                plantId = plantId,
                nickname = "Plant",
                speciesName = "Unknown",
                healthScore = 75,
                status = "STABLE",
                diseaseName = "Healthy",
                diseaseMatchPercentage = 100,
                treatmentSteps = emptyList(),
                careSchedule = CareScheduleResponse(
                    plantId = plantId,
                    nextWateringDate = now.plusSeconds(86400), // Tomorrow
                    daysUntilWatering = 1,
                    wateringUrgency = com.plantpulse.healthservice.domain.plant.WateringUrgency.UPCOMING,
                    skipWatering = false,
                    skipWateringReason = null
                ),
                lastObservationAt = null,
                lastWateredAt = now
            )
        }
    }

    fun history(plantId: UUID, userId: UUID): List<HealthSnapshotResponse> {
        val profile = profileRepository.findByIdAndUserId(plantId, userId)
            ?: throw PlantHealthNotFoundException(plantId)
        return snapshotRepository.findByPlantIdOrderByRecordedAtDesc(profile.id).map {
            HealthSnapshotResponse(
                healthScore = it.healthScore,
                status = it.status.name,
                diseaseName = it.diseaseName,
                diseaseMatchPercentage = it.diseaseMatchPercentage,
                source = it.source.name,
                recordedAt = it.recordedAt
            )
        }
    }

    fun careSchedule(plantId: UUID, userId: UUID): CareScheduleResponse {
        val profile = profileRepository.findByIdAndUserId(plantId, userId)
            ?: throw PlantHealthNotFoundException(plantId)
        return careScheduleService.scheduleFor(profile)
    }

    private fun PlantHealthProfile.toCard(): DashboardPlantCard {
        val schedule = careScheduleService.scheduleFor(this)
        return DashboardPlantCard(
            plantId = id,
            nickname = nickname,
            speciesName = speciesName,
            healthScore = currentHealthScore,
            status = currentStatus.name,
            wateringUrgency = schedule.wateringUrgency,
            daysUntilWatering = schedule.daysUntilWatering,
            nextWateringDate = schedule.nextWateringDate,
            skipWatering = schedule.skipWatering,
            skipWateringReason = schedule.skipWateringReason,
            activeDisease = currentDiseaseName?.takeUnless { it.equals("Healthy", ignoreCase = true) }
        )
    }

    private fun PlantHealthProfile.toDetail(): PlantHealthResponse {
        val disease = currentDiseaseName ?: "Healthy"
        return PlantHealthResponse(
            plantId = id,
            nickname = nickname,
            speciesName = speciesName,
            healthScore = currentHealthScore,
            status = currentStatus.name,
            diseaseName = currentDiseaseName,
            diseaseMatchPercentage = currentDiseaseMatchPercentage,
            treatmentSteps = DiseaseCareTips.forDisease(disease),
            careSchedule = careScheduleService.scheduleFor(this),
            lastObservationAt = lastObservationAt,
            lastWateredAt = lastWateredAt
        )
    }

    private fun com.plantpulse.healthservice.domain.outbreak.RegionalOutbreak.toResponse() = RegionalOutbreakResponse(
        id = id,
        cityLocation = cityLocation,
        diseaseName = diseaseName,
        affectedPlantCount = affectedPlantCount,
        windowStart = windowStart,
        windowEnd = windowEnd,
        detectedAt = detectedAt
    )
}

class PlantHealthNotFoundException(id: UUID) : RuntimeException("No health profile found for plant: $id")
