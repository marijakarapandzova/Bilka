package com.plantpulse.plantservice.infrastructure.external

import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader

data class KagglePlantEntry(
    val plantName: String,
    val height: Double,
    val leafCount: Int,
    val newGrowthCount: Int,
    val healthNotes: String,
    val wateringAmountMl: Double,
    val wateringFrequencyDays: Int,
    val sunlightExposure: String,
    val roomTemperatureC: Double,
    val humidity: Double,
    val fertilizerType: String,
    val fertilizerAmountMl: Double,
    val pestPresence: String,
    val pestSeverity: String,
    val soilMoisture: Double,
    val soilType: String,
    val healthScore: Int
)

object KagglePlantDatabase {
    private val log = LoggerFactory.getLogger(KagglePlantDatabase::class.java)
    private val plants = mutableListOf<KagglePlantEntry>()
    private val plantsByName = mutableMapOf<String, KagglePlantEntry>()

    init {
        loadDatabase()
    }

    private fun loadDatabase() {
        try {
            val resource = this::class.java.classLoader.getResourceAsStream("plants-database.csv")
                ?: throw IllegalStateException("plants-database.csv not found in resources")

            val reader = BufferedReader(InputStreamReader(resource))
            val header = reader.readLine() // Skip header

            var lineCount = 0
            reader.useLines { lines ->
                lines.forEach { line ->
                    try {
                        val entry = parseCsvLine(line)
                        if (entry != null) {
                            plants.add(entry)
                            plantsByName[entry.plantName.lowercase()] = entry
                            lineCount++
                        }
                    } catch (e: Exception) {
                        log.debug("Error parsing CSV line: {}", e.message)
                    }
                }
            }

            log.info("✓ Loaded {} plants from Kaggle database", lineCount)
        } catch (e: Exception) {
            log.error("Failed to load Kaggle plant database: {}", e.message)
            throw RuntimeException("Failed to load plants database", e)
        }
    }

    private fun parseCsvLine(line: String): KagglePlantEntry? {
        val parts = line.split(",")
        if (parts.size < 17) return null

        return try {
            KagglePlantEntry(
                plantName = parts[0].trim(),
                height = parts[1].trim().toDoubleOrNull() ?: 0.0,
                leafCount = parts[2].trim().toIntOrNull() ?: 0,
                newGrowthCount = parts[3].trim().toIntOrNull() ?: 0,
                healthNotes = parts[4].trim(),
                wateringAmountMl = parts[5].trim().toDoubleOrNull() ?: 0.0,
                wateringFrequencyDays = parts[6].trim().toIntOrNull() ?: 7,
                sunlightExposure = parts[7].trim(),
                roomTemperatureC = parts[8].trim().toDoubleOrNull() ?: 20.0,
                humidity = parts[9].trim().toDoubleOrNull() ?: 50.0,
                fertilizerType = parts[10].trim(),
                fertilizerAmountMl = parts[11].trim().toDoubleOrNull() ?: 0.0,
                pestPresence = parts[12].trim(),
                pestSeverity = parts[13].trim(),
                soilMoisture = parts[14].trim().toDoubleOrNull() ?: 50.0,
                soilType = parts[15].trim(),
                healthScore = parts[16].trim().toIntOrNull() ?: 3
            )
        } catch (e: Exception) {
            log.debug("Error parsing plant entry: {}", e.message)
            null
        }
    }

    /**
     * Search for plants by name (supports partial matching)
     */
    fun searchPlants(query: String, limit: Int = 10): List<KagglePlantEntry> {
        val lowerQuery = query.lowercase().trim()
        return plants.filter {
            it.plantName.lowercase().contains(lowerQuery)
        }.take(limit)
    }

    /**
     * Get plant by exact name
     */
    fun getPlant(plantName: String): KagglePlantEntry? {
        return plantsByName[plantName.lowercase().trim()]
    }

    /**
     * Get all plants
     */
    fun getAllPlants(): List<KagglePlantEntry> = plants.toList()

    /**
     * Get total plant count
     */
    fun getPlantCount(): Int = plants.size

    /**
     * Convert to a map for API response
     */
    fun plantToMap(entry: KagglePlantEntry): Map<String, Any?> = mapOf(
        "id" to entry.plantName.hashCode(),
        "name" to entry.plantName,
        "scientificName" to entry.plantName,
        "watering_frequency_days" to entry.wateringFrequencyDays,
        "watering_amount_ml" to entry.wateringAmountMl,
        "watering_info" to "Water every ${entry.wateringFrequencyDays} days, approximately ${entry.wateringAmountMl}ml",
        "sunlight_exposure" to entry.sunlightExposure,
        "room_temperature_c" to entry.roomTemperatureC,
        "humidity_percent" to entry.humidity,
        "soil_type" to entry.soilType,
        "soil_moisture_percent" to entry.soilMoisture,
        "fertilizer_type" to entry.fertilizerType,
        "fertilizer_amount_ml" to entry.fertilizerAmountMl,
        "pest_presence" to entry.pestPresence,
        "pest_severity" to entry.pestSeverity,
        "health_notes" to entry.healthNotes,
        "health_score" to entry.healthScore,
        "height_cm" to entry.height,
        "leaf_count" to entry.leafCount,
        "new_growth_count" to entry.newGrowthCount,
        "data_source" to "KAGGLE"
    )
}
