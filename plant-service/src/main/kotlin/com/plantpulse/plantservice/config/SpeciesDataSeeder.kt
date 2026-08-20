package com.plantpulse.plantservice.config

import com.plantpulse.plantservice.domain.species.CareDifficulty
import com.plantpulse.plantservice.domain.species.LightNeeds
import com.plantpulse.plantservice.domain.species.Species
import com.plantpulse.plantservice.domain.species.SpeciesRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component

@Component
class SpeciesDataSeeder(
    private val speciesRepository: SpeciesRepository
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(SpeciesDataSeeder::class.java)

    override fun run(vararg args: String?) {
        if (speciesRepository.count() > 0) {
            log.info("Species already seeded ({} entries) — skipping.", speciesRepository.count())
            return
        }

        seedSpeciesFromCsv()
    }

    private fun seedSpeciesFromCsv() {
        try {
            val resource = ClassPathResource("plants-database.csv")
            val speciesList = mutableListOf<Species>()
            val seenNames = mutableSetOf<String>()

            resource.inputStream.bufferedReader().use { reader ->
                reader.readLine() // skip header
                reader.forEachLine { line ->
                    val parts = line.split(",")
                    if (parts.isNotEmpty()) {
                        val plantName = parts[0].trim()
                        if (plantName !in seenNames) {
                            seenNames.add(plantName)
                            val heightCm = parts.getOrNull(1)?.toDoubleOrNull()
                            val leafCount = parts.getOrNull(2)?.toIntOrNull()
                            val newGrowthCount = parts.getOrNull(3)?.toIntOrNull()
                            val wateringAmountMl = parts.getOrNull(5)?.toDoubleOrNull()
                            val wateringFreq = parts.getOrNull(6)?.toIntOrNull() ?: 7
                            val lightExposure = parts.getOrNull(7)?.trim() ?: "Indirect"
                            val temperature = parts.getOrNull(8)?.toDoubleOrNull()
                            val humidity = parts.getOrNull(9)?.toDoubleOrNull()
                            val soilType = parts.getOrNull(15)?.trim() ?: "Loamy"
                            val healthScore = parts.getOrNull(16)?.toIntOrNull()

                            val species = Species(
                                name = plantName,
                                scientificName = plantName,
                                careDifficulty = mapCareDifficultyFromFreq(wateringFreq),
                                wateringFrequencyDays = wateringFreq,
                                lightNeeds = mapLightFromExposure(lightExposure),
                                soilDescription = soilType,
                                temperatureMin = temperature?.toString(),
                                atmosphericHumidity = humidity?.toInt(),
                                description = "Height: ${heightCm?.toInt()}cm. Soil: $soilType. Light: $lightExposure. Temperature: ${temperature?.toInt()}°C. Humidity: ${humidity?.toInt()}%"
                            )
                            speciesList.add(species)
                        }
                    }
                }
            }

            for (species in speciesList) {
                speciesRepository.save(species)
            }

            // Add test species for testing watering feature
            val testSpeciesId = java.util.UUID.fromString("550e8400-e29b-41d4-a716-446655440001")
            if (!speciesRepository.existsById(testSpeciesId)) {
                val testSpecies = Species(
                    id = testSpeciesId,
                    name = "Test Plant - Water Every 7 Days",
                    scientificName = "Test Plant",
                    careDifficulty = CareDifficulty.EASY,
                    wateringFrequencyDays = 7,
                    lightNeeds = LightNeeds.BRIGHT_INDIRECT,
                    description = "Test species for watering feature"
                )
                speciesRepository.save(testSpecies)
            }

            log.info("Seeded {} species from CSV + test species.", speciesList.size)
        } catch (e: Exception) {
            log.error("Failed to seed species from CSV", e)
            seedDefaultSpecies()
        }
    }

    private fun mapCareDifficultyFromFreq(wateringFreq: Int): CareDifficulty = when {
        wateringFreq <= 2 -> CareDifficulty.EASY
        wateringFreq <= 7 -> CareDifficulty.MODERATE
        else -> CareDifficulty.DIFFICULT
    }

    private fun mapLightFromExposure(exposure: String): LightNeeds = when {
        exposure.contains("direct", ignoreCase = true) -> LightNeeds.DIRECT_SUN
        exposure.contains("Indirect", ignoreCase = true) -> LightNeeds.BRIGHT_INDIRECT
        exposure.contains("Low", ignoreCase = true) || exposure.contains("corner", ignoreCase = true) -> LightNeeds.LOW
        else -> LightNeeds.MEDIUM
    }

    private fun seedDefaultSpecies() {
        val defaultSpecies = listOf(
            Species(name = "Monstera", scientificName = "Monstera deliciosa", careDifficulty = CareDifficulty.EASY, wateringFrequencyDays = 7, lightNeeds = LightNeeds.BRIGHT_INDIRECT, description = "Easy to care for tropical plant"),
            Species(name = "Tomato", scientificName = "Solanum lycopersicum", careDifficulty = CareDifficulty.MODERATE, wateringFrequencyDays = 3, lightNeeds = LightNeeds.DIRECT_SUN, description = "Popular vegetable plant"),
            Species(name = "Basil", scientificName = "Ocimum basilicum", careDifficulty = CareDifficulty.EASY, wateringFrequencyDays = 2, lightNeeds = LightNeeds.BRIGHT_INDIRECT, description = "Aromatic herb"),
            Species(name = "Snake Plant", scientificName = "Sansevieria trifasciata", careDifficulty = CareDifficulty.EASY, wateringFrequencyDays = 14, lightNeeds = LightNeeds.LOW, description = "Hardy indoor plant"),
            Species(name = "Pothos", scientificName = "Epipremnum aureum", careDifficulty = CareDifficulty.EASY, wateringFrequencyDays = 7, lightNeeds = LightNeeds.MEDIUM, description = "Climbing vine plant"),
            Species(name = "Peace Lily", scientificName = "Spathiphyllum wallisii", careDifficulty = CareDifficulty.EASY, wateringFrequencyDays = 5, lightNeeds = LightNeeds.LOW, description = "Flowering indoor plant"),
            Species(name = "Spider Plant", scientificName = "Chlorophytum comosum", careDifficulty = CareDifficulty.EASY, wateringFrequencyDays = 7, lightNeeds = LightNeeds.MEDIUM, description = "Easy care plant with long leaves"),
            Species(name = "Aloe Vera", scientificName = "Aloe barbadensis", careDifficulty = CareDifficulty.EASY, wateringFrequencyDays = 14, lightNeeds = LightNeeds.DIRECT_SUN, description = "Succulent with healing properties"),
            Species(name = "Rubber Plant", scientificName = "Ficus elastica", careDifficulty = CareDifficulty.MODERATE, wateringFrequencyDays = 7, lightNeeds = LightNeeds.BRIGHT_INDIRECT, description = "Large leafy tropical plant"),
            Species(name = "ZZ Plant", scientificName = "Zamioculcas zamiifolia", careDifficulty = CareDifficulty.EASY, wateringFrequencyDays = 10, lightNeeds = LightNeeds.LOW, description = "Very hardy indoor plant")
        )

        for (species in defaultSpecies) {
            speciesRepository.save(species)
        }

        log.info("Seeded {} default species.", defaultSpecies.size)
    }
}
