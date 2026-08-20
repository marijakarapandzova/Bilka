package com.plantpulse.plantservice.application

import com.plantpulse.plantservice.api.shared.CityPlantsResponse
import com.plantpulse.plantservice.api.shared.MessageResponse
import com.plantpulse.plantservice.api.shared.SendMessageRequest
import com.plantpulse.plantservice.api.shared.SharePlantRequest
import com.plantpulse.plantservice.api.shared.SharedPlantResponse
import com.plantpulse.plantservice.domain.plant.PlantRepository
import com.plantpulse.plantservice.domain.shared.Message
import com.plantpulse.plantservice.domain.shared.MessageRepository
import com.plantpulse.plantservice.domain.shared.SharedPlant
import com.plantpulse.plantservice.domain.shared.SharedPlantRepository
import com.plantpulse.plantservice.domain.species.SpeciesRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class SharedPlantService(
    private val sharedPlantRepository: SharedPlantRepository,
    private val messageRepository: MessageRepository,
    private val plantRepository: PlantRepository,
    private val speciesRepository: SpeciesRepository
) {

    @Transactional
    fun sharePlant(userId: UUID, userName: String, request: SharePlantRequest): SharedPlantResponse {
        val plant = plantRepository.findById(request.plantId)
            .orElseThrow { Exception("Plant not found") }

        if (plant.userId != userId) throw Exception("Unauthorized")

        val species = speciesRepository.findById(plant.speciesId)
            .orElseThrow { Exception("Species not found") }

        val sharedPlant = SharedPlant(
            userId = userId,
            plantId = plant.id,
            plantName = plant.nickname,
            speciesName = species.name,
            city = request.city,
            photoUrl = request.photoUrl ?: plant.currentPhotoUrl,
            userName = userName
        )

        val saved = sharedPlantRepository.save(sharedPlant)
        return saved.toResponse()
    }

    fun getSharedPlantsByCity(city: String): List<SharedPlantResponse> =
        sharedPlantRepository.findByCity(city).map { it.toResponse() }

    fun getAllSharedPlants(): List<SharedPlantResponse> =
        sharedPlantRepository.findAll().map { it.toResponse() }

    fun getSharedPlantsByUser(userId: UUID): List<SharedPlantResponse> =
        sharedPlantRepository.findByUserId(userId).map { it.toResponse() }

    fun getCitiesWithPlants(): List<String> =
        sharedPlantRepository.findAll()
            .map { it.city }
            .distinct()
            .sorted()

    fun getCityPlants(city: String): CityPlantsResponse =
        CityPlantsResponse(
            city = city,
            plants = getSharedPlantsByCity(city)
        )

    @Transactional
    fun sendMessage(senderId: UUID, senderName: String, request: SendMessageRequest): MessageResponse {
        val message = Message(
            senderId = senderId,
            senderName = senderName,
            receiverId = request.receiverId,
            content = request.content
        )
        val saved = messageRepository.save(message)
        return saved.toResponse()
    }

    fun getInboxMessages(userId: UUID): List<MessageResponse> =
        messageRepository.findByReceiverIdOrderBySentAtDesc(userId).map { it.toResponse() }

    fun getConversation(userId: UUID, otherUserId: UUID): List<MessageResponse> =
        messageRepository.findBySenderIdOrReceiverIdOrderBySentAtDesc(userId, otherUserId)
            .filter { (it.senderId == userId && it.receiverId == otherUserId) || (it.senderId == otherUserId && it.receiverId == userId) }
            .map { it.toResponse() }

    private fun SharedPlant.toResponse() = SharedPlantResponse(
        id = id,
        userId = userId,
        plantId = plantId,
        plantName = plantName,
        speciesName = speciesName,
        city = city,
        photoUrl = photoUrl,
        userName = userName,
        sharedAt = sharedAt
    )

    private fun Message.toResponse() = MessageResponse(
        id = id,
        senderId = senderId,
        senderName = senderName,
        receiverId = receiverId,
        content = content,
        sentAt = sentAt
    )
}
