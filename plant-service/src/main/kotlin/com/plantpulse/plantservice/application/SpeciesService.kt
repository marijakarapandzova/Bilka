package com.plantpulse.plantservice.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.plantpulse.plantservice.api.species.CreateSpeciesRequest
import com.plantpulse.plantservice.api.species.SpeciesResponse
import com.plantpulse.plantservice.domain.species.CareDifficulty
import com.plantpulse.plantservice.domain.species.LightNeeds
import com.plantpulse.plantservice.domain.species.Species
import com.plantpulse.plantservice.domain.species.SpeciesRepository
import com.plantpulse.plantservice.infrastructure.external.KagglePlantDatabase
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SpeciesService(
    private val speciesRepository: SpeciesRepository
) {
    private val log = LoggerFactory.getLogger(SpeciesService::class.java)
    private val objectMapper = ObjectMapper()

    fun search(query: String?): List<SpeciesResponse> {
        val localResults = if (query.isNullOrBlank()) {
            speciesRepository.findAll().map { it.toResponse() }
        } else {
            val byName = speciesRepository.findByNameContainingIgnoreCase(query)
            val byScientific = speciesRepository.findByScientificNameContainingIgnoreCase(query)
            (byName + byScientific).distinctBy { it.id }.map { it.toResponse() }
        }

        // Enrich local results with Kaggle data if missing
        val enrichedLocalResults = localResults.map { species ->
            if (species.fullTrefleData.isNullOrBlank()) {
                // Try to find in Kaggle and enrich
                KagglePlantDatabase.getPlant(species.name)?.let { kagglePlant ->
                    val kaggleJson = try {
                        objectMapper.writeValueAsString(KagglePlantDatabase.plantToMap(kagglePlant))
                    } catch (e: Exception) {
                        null
                    }

                    val updatedSpecies = speciesRepository.findById(species.id).get().apply {
                        fullTrefleData = kaggleJson
                        wateringFrequencyDays = kagglePlant.wateringFrequencyDays
                    }
                    speciesRepository.save(updatedSpecies)
                    updatedSpecies.toResponse()
                } ?: species
            } else {
                species
            }
        }

        // If no query or good local results, return them
        if (query.isNullOrBlank() || enrichedLocalResults.size >= 3) {
            return enrichedLocalResults
        }

        // Search Kaggle database for more results
        return try {
            val kagglePlants = KagglePlantDatabase.searchPlants(query, limit = 10)
            val seenIds = enrichedLocalResults.map { it.id }.toMutableSet()

            val kaggleResults = kagglePlants.mapNotNull { kagglePlant ->
                val speciesId = UUID.nameUUIDFromBytes("kaggle-${kagglePlant.plantName}".toByteArray())

                // Skip if we've already seen this ID
                if (speciesId in seenIds) {
                    return@mapNotNull null
                }
                seenIds.add(speciesId)

                // Check if already in database
                val existingSpecies = speciesRepository.findById(speciesId).orElse(null)

                if (existingSpecies != null) {
                    existingSpecies.toResponse()
                } else {
                    // Save new species from Kaggle
                    val careDiff = mapHealthScoreToDifficulty(kagglePlant.healthScore)
                    val lightNeed = mapSunlightToLightNeeds(kagglePlant.sunlightExposure)

                    val kaggleJson = try {
                        objectMapper.writeValueAsString(KagglePlantDatabase.plantToMap(kagglePlant))
                    } catch (e: Exception) {
                        null
                    }

                    val newSpecies = Species(
                        id = speciesId,
                        name = kagglePlant.plantName,
                        scientificName = kagglePlant.plantName,
                        careDifficulty = careDiff,
                        wateringFrequencyDays = kagglePlant.wateringFrequencyDays,
                        lightNeeds = lightNeed,
                        description = kagglePlant.healthNotes.ifBlank { "Indoor plant from Kaggle database" },
                        imageUrl = null,
                        temperatureMin = kagglePlant.roomTemperatureC.toString(),
                        temperatureMax = kagglePlant.roomTemperatureC.toString(),
                        phMin = null,
                        phMax = null,
                        soilDescription = kagglePlant.soilType,
                        flowerDescription = null,
                        growthHabit = null,
                        nativeRange = null,
                        lightLevel = null,
                        atmosphericHumidity = kagglePlant.humidity.toInt(),
                        wateringFrequency = "${kagglePlant.wateringAmountMl}ml every ${kagglePlant.wateringFrequencyDays} days",
                        fullTrefleData = kaggleJson
                    )
                    speciesRepository.save(newSpecies)
                    newSpecies.toResponse()
                }
            }

            enrichedLocalResults + kaggleResults
        } catch (ex: Exception) {
            log.debug("Kaggle search failed: {}", ex.message)
            enrichedLocalResults
        }
    }

    fun getById(id: UUID): SpeciesResponse =
        speciesRepository.findById(id)
            .orElseThrow { SpeciesNotFoundException(id) }
            .toResponse()

    fun getEntityById(id: UUID): Species =
        speciesRepository.findById(id).orElseThrow { SpeciesNotFoundException(id) }

    fun create(request: CreateSpeciesRequest): SpeciesResponse {
        val species = Species(
            name = request.name,
            scientificName = request.scientificName,
            careDifficulty = request.careDifficulty,
            wateringFrequencyDays = request.wateringFrequencyDays,
            lightNeeds = request.lightNeeds,
            description = request.description
        )
        val saved = speciesRepository.save(species)
        return saved.toResponse()
    }

    fun fetchPerenualCatalog(page: Int = 1): List<SpeciesResponse> {
        // Return all Kaggle plants (paginate if needed)
        val offset = (page - 1) * 50
        val seenIds = mutableSetOf<UUID>()

        return KagglePlantDatabase.getAllPlants()
            .drop(offset)
            .take(50)
            .mapNotNull { kagglePlant ->
                val speciesId = UUID.nameUUIDFromBytes("kaggle-${kagglePlant.plantName}".toByteArray())

                // Skip if we've already processed this ID (handles duplicate plant names)
                if (speciesId in seenIds) {
                    return@mapNotNull null
                }
                seenIds.add(speciesId)

                val existingSpecies = speciesRepository.findById(speciesId).orElse(null)

                if (existingSpecies != null) {
                    existingSpecies.toResponse()
                } else {
                    val careDiff = mapHealthScoreToDifficulty(kagglePlant.healthScore)
                    val lightNeed = mapSunlightToLightNeeds(kagglePlant.sunlightExposure)

                    val kaggleJson = try {
                        objectMapper.writeValueAsString(KagglePlantDatabase.plantToMap(kagglePlant))
                    } catch (e: Exception) {
                        null
                    }

                    val newSpecies = Species(
                        id = speciesId,
                        name = kagglePlant.plantName,
                        scientificName = kagglePlant.plantName,
                        careDifficulty = careDiff,
                        wateringFrequencyDays = kagglePlant.wateringFrequencyDays,
                        lightNeeds = lightNeed,
                        description = kagglePlant.healthNotes.ifBlank { "Indoor plant from Kaggle database" },
                        imageUrl = null,
                        temperatureMin = kagglePlant.roomTemperatureC.toString(),
                        temperatureMax = kagglePlant.roomTemperatureC.toString(),
                        soilDescription = kagglePlant.soilType,
                        atmosphericHumidity = kagglePlant.humidity.toInt(),
                        wateringFrequency = "${kagglePlant.wateringAmountMl}ml every ${kagglePlant.wateringFrequencyDays} days",
                        fullTrefleData = kaggleJson
                    )
                    speciesRepository.save(newSpecies)
                    newSpecies.toResponse()
                }
            }
    }

    private fun mapHealthScoreToDifficulty(healthScore: Int): CareDifficulty {
        return when {
            healthScore >= 4 -> CareDifficulty.EASY
            healthScore == 3 -> CareDifficulty.MODERATE
            else -> CareDifficulty.DIFFICULT
        }
    }

    private fun mapSunlightToLightNeeds(sunlight: String): LightNeeds {
        return when {
            sunlight.lowercase().contains("direct") -> LightNeeds.DIRECT_SUN
            sunlight.lowercase().contains("indirect") -> LightNeeds.BRIGHT_INDIRECT
            sunlight.lowercase().contains("low") || sunlight.lowercase().contains("shade") -> LightNeeds.LOW
            else -> LightNeeds.MEDIUM
        }
    }

    private fun Species.toResponse() = SpeciesResponse(
        id = id,
        name = name,
        scientificName = scientificName,
        careDifficulty = careDifficulty,
        wateringFrequencyDays = wateringFrequencyDays,
        lightNeeds = lightNeeds,
        description = description,
        imageUrl = imageUrl,
        familyCommonName = familyCommonName,
        lightLevel = lightLevel,
        atmosphericHumidity = atmosphericHumidity,
        wateringFrequency = wateringFrequency,
        temperatureMin = temperatureMin,
        temperatureMax = temperatureMax,
        phMin = phMin,
        phMax = phMax,
        soilDescription = soilDescription,
        flowerDescription = flowerDescription,
        fruitDescription = fruitDescription,
        growthHabit = growthHabit,
        nativeRange = nativeRange,
        fullTrefleData = fullTrefleData
    )
}
