package com.plantpulse.plantservice.api.species

import com.plantpulse.plantservice.application.SpeciesService
import com.plantpulse.plantservice.infrastructure.csv.CsvDataLoader
import com.plantpulse.plantservice.infrastructure.external.KagglePlantDatabase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/species")
@Tag(name = "Species API", description = "Browse and manage plant species")
class SpeciesController(
    private val speciesService: SpeciesService,
    private val csvDataLoader: CsvDataLoader
) {

    @GetMapping
    @Operation(summary = "Search species", description = "Search for plant species by name or query")
    fun search(@RequestParam(required = false) query: String?): List<SpeciesResponse> =
        speciesService.search(query)

    @GetMapping("/catalog/browse")
    @Operation(summary = "Browse catalog", description = "Browse the plant species catalog (paginated)")
    fun browseCatalog(@RequestParam(defaultValue = "1") page: Int): List<SpeciesResponse> =
        speciesService.fetchPerenualCatalog(page)

    @GetMapping("/{id}")
    @Operation(summary = "Get species", description = "Get detailed information about a specific species")
    fun getById(@PathVariable id: UUID): SpeciesResponse =
        speciesService.getById(id)

    @PostMapping
    @Operation(summary = "Create species", description = "Add a new plant species to the database")
    fun create(@RequestBody request: CreateSpeciesRequest): ResponseEntity<SpeciesResponse> =
        ResponseEntity.status(201).body(speciesService.create(request))

    @GetMapping("/{id}/details")
    @Operation(summary = "Get full details", description = "Get comprehensive details about a species including care requirements and CSV data")
    fun getDetails(@PathVariable id: UUID): ResponseEntity<Map<String, Any?>> {
        val species = speciesService.getEntityById(id)
        val result = mutableMapOf<String, Any?>()

        result["id"] = species.id
        result["name"] = species.name
        result["scientificName"] = species.scientificName
        result["imageUrl"] = species.imageUrl
        result["careDifficulty"] = species.careDifficulty
        result["wateringFrequencyDays"] = species.wateringFrequencyDays
        result["lightNeeds"] = species.lightNeeds
        result["description"] = species.description ?: "No description available"

        // Load CSV data if available
        val csvData = csvDataLoader.getPlantData(species.name)
        if (csvData != null) {
            // Map CSV fields to response, converting types as needed
            result["height_cm"] = csvData["Height_cm"]?.toDoubleOrNull()
            result["leaf_count"] = csvData["Leaf_Count"]?.toIntOrNull()
            result["new_growth_count"] = csvData["New_Growth_Count"]?.toIntOrNull()
            result["health_notes"] = csvData["Health_Notes"]
            result["watering_amount_ml"] = csvData["Watering_Amount_ml"]?.toDoubleOrNull()
            result["sunlight_exposure"] = csvData["Sunlight_Exposure"]
            result["roomTemperatureC"] = csvData["Room_Temperature_C"]?.toDoubleOrNull()
            result["humidity_percent"] = csvData["Humidity_%"]?.toDoubleOrNull()
            result["fertilizer_type"] = csvData["Fertilizer_Type"]
            result["fertilizer_amount_ml"] = csvData["Fertilizer_Amount_ml"]?.toDoubleOrNull()
            result["pest_presence"] = csvData["Pest_Presence"]
            result["pest_severity"] = csvData["Pest_Severity"]
            result["soilMoisturePercent"] = csvData["Soil_Moisture_%"]?.toDoubleOrNull()
            result["soilType"] = csvData["Soil_Type"]
            result["health_score"] = csvData["Health_Score"]?.toIntOrNull()
            result["data_source"] = "CSV"
            return ResponseEntity.ok(result)
        }

        // Fallback to Kaggle data if CSV not available
        if (species.fullTrefleData != null && species.fullTrefleData!!.isNotBlank()) {
            try {
                val objectMapper = com.fasterxml.jackson.databind.ObjectMapper()
                @Suppress("UNCHECKED_CAST")
                val kaggleData = objectMapper.readValue(species.fullTrefleData!!, Map::class.java) as Map<String, Any?>
                result.putAll(kaggleData)
                result["data_source"] = "KAGGLE"
                return ResponseEntity.ok(result)
            } catch (e: Exception) {
                // If parsing fails, continue with database data
            }
        }

        // Try to find in Kaggle database for comprehensive data
        if (species.name.isNotBlank()) {
            KagglePlantDatabase.getPlant(species.name)?.let { kagglePlant ->
                val kaggleData = KagglePlantDatabase.plantToMap(kagglePlant)
                result.putAll(kaggleData)
                result["data_source"] = "KAGGLE"
            }
        }

        if (!result.containsKey("data_source")) {
            result["data_source"] = "DATABASE"
        }

        return ResponseEntity.ok(result)
    }
}
