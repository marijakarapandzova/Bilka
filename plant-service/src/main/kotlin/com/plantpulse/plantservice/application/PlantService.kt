package com.plantpulse.plantservice.application

import com.plantpulse.plantservice.api.plant.AddPlantByPhotoRequest
import com.plantpulse.plantservice.api.plant.AddPlantManualRequest
import com.plantpulse.plantservice.api.plant.PlantResponse
import com.plantpulse.plantservice.api.plant.UpdatePlantRequest
import com.plantpulse.plantservice.domain.plant.Plant
import com.plantpulse.plantservice.domain.plant.PlantId
import com.plantpulse.plantservice.domain.plant.PlantRepository
import com.plantpulse.plantservice.domain.species.Species
import com.plantpulse.plantservice.domain.species.SpeciesRepository
import com.plantpulse.plantservice.infrastructure.external.PlantIdClient
import com.plantpulse.plantservice.infrastructure.messaging.PlantAddedEvent
import com.plantpulse.plantservice.infrastructure.messaging.PlantEventPublisher
import com.plantpulse.plantservice.infrastructure.messaging.PlantRemovedEvent
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PlantService(
    private val plantRepository: PlantRepository,
    private val speciesRepository: SpeciesRepository,
    private val plantIdClient: PlantIdClient,
    private val eventPublisher: PlantEventPublisher
) {

    @Transactional
    fun addByPhoto(userId: UUID, request: AddPlantByPhotoRequest): PlantResponse {
        val identification = plantIdClient.identify(request.imageBase64)
            ?: throw PhotoIdentificationFailedException("No match found or Plant.id API unavailable")

        // Best-effort match against our own knowledge base by name
        val species = speciesRepository.findByNameContainingIgnoreCase(identification.matchedName)
            .firstOrNull()
            ?: throw PhotoIdentificationFailedException(
                "Identified as '${identification.matchedName}' but no matching species in the knowledge base"
            )

        val plant = createAndPersistPlant(userId, species, request.nickname, request.room)
        return plant.toResponse(species).copy(photoMatchConfidencePercent = identification.confidencePercent)
    }

    @Transactional
    fun addManual(userId: UUID, request: AddPlantManualRequest): PlantResponse {
        val species = speciesRepository.findById(request.speciesId)
            .orElseThrow { SpeciesNotFoundException(request.speciesId) }

        val plant = createAndPersistPlant(userId, species, request.nickname, request.room, request.currentPhotoUrl)
        return plant.toResponse(species)
    }

    fun getGarden(userId: UUID): List<PlantResponse> =
        plantRepository.findByUserId(userId).mapNotNull { plant ->
            val species = speciesRepository.findById(plant.speciesId).orElse(null)
            if (species != null) {
                plant.toResponse(species)
            } else {
                // Log but don't fail if species is missing
                org.slf4j.LoggerFactory.getLogger(this::class.java)
                    .warn("Plant {} references missing species {}", plant.id, plant.speciesId)
                null
            }
        }

    fun getById(plantId: UUID, userId: UUID): PlantResponse {
        val plant = findOwnedPlant(plantId, userId)
        val species = speciesRepository.findById(plant.speciesId).orElseThrow {
            SpeciesNotFoundException(plant.speciesId)
        }
        return plant.toResponse(species)
    }

    @Transactional
    fun update(plantId: UUID, userId: UUID, request: UpdatePlantRequest): PlantResponse {
        val plant = findOwnedPlant(plantId, userId)

        request.nickname?.let { plant.rename(it) }
        request.room?.let { plant.moveToRoom(it) }

        val saved = plantRepository.save(plant)
        val species = speciesRepository.findById(saved.speciesId).orElseThrow {
            SpeciesNotFoundException(saved.speciesId)
        }
        return saved.toResponse(species)
    }

    @Transactional
    fun delete(plantId: UUID, userId: UUID) {
        val plant = findOwnedPlant(plantId, userId)
        plantRepository.delete(plant)
        try {
            eventPublisher.publishPlantRemoved(PlantRemovedEvent(plantId = PlantId(plant.id), userId = userId))
        } catch (ex: Exception) {
            // Kafka not available - continue anyway for testing
        }
    }

    @Transactional
    fun logWatering(plantId: UUID, userId: UUID): PlantResponse {
        val plant = findOwnedPlant(plantId, userId)
        plant.logWatering()
        val saved = plantRepository.save(plant)
        val species = speciesRepository.findById(saved.speciesId).orElseThrow {
            SpeciesNotFoundException(saved.speciesId)
        }
        return saved.toResponse(species)
    }

    private fun createAndPersistPlant(userId: UUID, species: Species, nickname: String, room: String?, photoUrl: String? = null): Plant {
        val plant = Plant(
            userId = userId,
            speciesId = species.id,
            nickname = nickname,
            room = room,
            currentPhotoUrl = photoUrl
        )
        println("🌱 Creating plant for userId=$userId, speciesId=${species.id}, nickname=$nickname")
        val saved = plantRepository.save(plant)
        println("✅ Plant saved to database: id=${saved.id}, userId=${saved.userId}")

        try {
            eventPublisher.publishPlantAdded(
                PlantAddedEvent(
                    plantId = PlantId(saved.id),
                    userId = userId,
                    speciesId = species.id,
                    wateringFrequencyDays = species.wateringFrequencyDays
                )
            )
        } catch (ex: Exception) {
            // Kafka not available - continue anyway for testing
        }

        return saved
    }

    private fun findOwnedPlant(plantId: UUID, userId: UUID): Plant {
        val plant = plantRepository.findById(plantId).orElseThrow { PlantNotFoundException(plantId) }
        if (plant.userId != userId) throw PlantAccessDeniedException()
        return plant
    }

    private fun Plant.toResponse(species: Species) = PlantResponse(
        id = id,
        speciesId = species.id,
        speciesName = species.name,
        nickname = nickname,
        room = room,
        currentPhotoUrl = currentPhotoUrl,
        wateringFrequencyDays = species.wateringFrequencyDays,
        addedAt = addedAt,
        lastWateredAt = lastWateredAt
    )
}
