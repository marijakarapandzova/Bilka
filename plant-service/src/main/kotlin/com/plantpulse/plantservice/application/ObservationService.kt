package com.plantpulse.plantservice.application

import com.plantpulse.plantservice.api.observation.DiseaseMatchResponse
import com.plantpulse.plantservice.api.observation.LogObservationRequest
import com.plantpulse.plantservice.api.observation.ObservationResponse
import com.plantpulse.plantservice.domain.disease.SymptomMatcher
import com.plantpulse.plantservice.domain.observation.Observation
import com.plantpulse.plantservice.domain.observation.ObservationRepository
import com.plantpulse.plantservice.domain.plant.PlantId
import com.plantpulse.plantservice.domain.plant.PlantRepository
import com.plantpulse.plantservice.domain.user.UserRepository
import com.plantpulse.plantservice.infrastructure.messaging.ObservationLoggedEvent
import com.plantpulse.plantservice.infrastructure.messaging.PlantEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ObservationService(
    private val observationRepository: ObservationRepository,
    private val plantRepository: PlantRepository,
    private val userRepository: UserRepository,
    private val eventPublisher: PlantEventPublisher
) {
    private val symptomMatcher = SymptomMatcher()

    @Transactional
    fun logObservation(plantId: UUID, userId: UUID, request: LogObservationRequest): ObservationResponse {
        val plant = plantRepository.findById(plantId).orElseThrow { PlantNotFoundException(plantId) }
        if (plant.userId != userId) throw PlantAccessDeniedException()

        val observation = Observation(
            plantId = plantId,
            leafColor = request.leafColor,
            leafTexture = request.leafTexture,
            soilMoisture = request.soilMoisture,
            visiblePests = request.visiblePests,
            growth = request.growth,
            notes = request.notes,
            photoUrl = request.photoUrl,
            heightCm = request.heightCm,
            leafCount = request.leafCount,
            newGrowthCount = request.newGrowthCount,
            healthNotes = request.healthNotes,
            wateringAmountMl = request.wateringAmountMl,
            wateringFrequencyDays = request.wateringFrequencyDays,
            sunlightExposure = request.sunlightExposure,
            roomTemperatureC = request.roomTemperatureC,
            humidityPercent = request.humidityPercent,
            fertilizerType = request.fertilizerType,
            fertilizerAmountMl = request.fertilizerAmountMl,
            pestPresence = request.pestPresence,
            pestSeverity = request.pestSeverity,
            soilMoisturePercent = request.soilMoisturePercent,
            soilType = request.soilType,
            healthScore = request.healthScore
        )
        val saved = observationRepository.save(observation)

        val diseaseMatch = try {
            symptomMatcher.match(saved)
        } catch (ex: Exception) {
            com.plantpulse.plantservice.domain.disease.DiseaseMatch("Unknown", 0, emptyList())
        }

        try {
            val user = userRepository.findById(userId).orElse(null)
            eventPublisher.publishObservationLogged(
                ObservationLoggedEvent(
                    observationId = saved.id,
                    plantId = PlantId(plantId),
                    userId = userId,
                    diseaseMatchName = diseaseMatch.diseaseName,
                    diseaseMatchPercentage = diseaseMatch.matchPercentage,
                    cityLocation = request.cityLocation ?: user?.location?.city
                )
            )
        } catch (ex: Exception) {
            // Kafka not available - continue anyway for testing
        }

        return saved.toResponse(diseaseMatch)
    }

    fun getHistory(plantId: UUID, userId: UUID): List<ObservationResponse> {
        val plant = plantRepository.findById(plantId).orElseThrow { PlantNotFoundException(plantId) }
        if (plant.userId != userId) throw PlantAccessDeniedException()

        return observationRepository.findByPlantIdOrderByLoggedAtDesc(plantId).map { observation ->
            observation.toResponse(symptomMatcher.match(observation))
        }
    }

    private fun Observation.toResponse(diseaseMatch: com.plantpulse.plantservice.domain.disease.DiseaseMatch) =
        ObservationResponse(
            id = id,
            plantId = plantId,
            loggedAt = loggedAt,
            diseaseMatch = DiseaseMatchResponse(
                diseaseName = diseaseMatch.diseaseName,
                matchPercentage = diseaseMatch.matchPercentage,
                treatmentSteps = diseaseMatch.treatmentSteps
            ),
            leafColor = leafColor.name,
            leafTexture = leafTexture.name,
            soilMoisture = soilMoisture.name,
            visiblePests = visiblePests,
            growth = growth.name,
            notes = notes,
            photoUrl = photoUrl,
            heightCm = heightCm,
            leafCount = leafCount,
            newGrowthCount = newGrowthCount,
            healthNotes = healthNotes,
            wateringAmountMl = wateringAmountMl,
            wateringFrequencyDays = wateringFrequencyDays,
            sunlightExposure = sunlightExposure,
            roomTemperatureC = roomTemperatureC,
            humidityPercent = humidityPercent,
            fertilizerType = fertilizerType,
            fertilizerAmountMl = fertilizerAmountMl,
            pestPresence = pestPresence,
            pestSeverity = pestSeverity,
            soilMoisturePercent = soilMoisturePercent,
            soilType = soilType,
            healthScore = healthScore
        )
}
