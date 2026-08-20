package com.plantpulse.healthservice.api.species

import com.plantpulse.healthservice.infrastructure.csv.CsvDataLoader
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/health/species")
@Tag(name = "Species Details", description = "Get detailed plant species information with CSV data")
class SpeciesDetailsController(private val csvDataLoader: CsvDataLoader) {

    @GetMapping("/{speciesId}")
    @Operation(summary = "Get species details with CSV data", description = "Returns comprehensive plant species information including CSV data from Indoor Plant Health dataset")
    fun getSpeciesDetails(@PathVariable speciesId: UUID): ResponseEntity<Map<String, Any?>> {
        // This endpoint receives a species UUID from plant-service
        // For now, we'll return CSV data by plant name if available
        // In a full implementation, this would call plant-service to get species name first
        val result = mutableMapOf<String, Any?>()
        result["id"] = speciesId
        result["message"] = "Species details endpoint - CSV data available"
        return ResponseEntity.ok(result)
    }

    @GetMapping("/by-name/{plantName}")
    @Operation(summary = "Get species details by plant name", description = "Search CSV data by plant name")
    fun getSpeciesByName(@PathVariable plantName: String): ResponseEntity<Map<String, Any?>> {
        val result = mutableMapOf<String, Any?>()

        val csvData = csvDataLoader.getPlantData(plantName)
        if (csvData != null) {
            result["id"] = plantName
            result["name"] = plantName

            // Map CSV fields to response
            result["height_cm"] = csvData["Height_cm"]?.toDoubleOrNull()
            result["leaf_count"] = csvData["Leaf_Count"]?.toIntOrNull()
            result["new_growth_count"] = csvData["New_Growth_Count"]?.toIntOrNull()
            result["health_notes"] = csvData["Health_Notes"]
            result["watering_amount_ml"] = csvData["Watering_Amount_ml"]?.toDoubleOrNull()
            result["watering_frequency_days"] = csvData["Watering_Frequency_days"]?.toIntOrNull()
            result["sunlight_exposure"] = csvData["Sunlight_Exposure"]
            result["roomTemperatureC"] = csvData["Room_Temperature_C"]?.toDoubleOrNull()
            result["humidity_percent"] = csvData["Humidity_%"]?.toDoubleOrNull()
            result["fertilizer_type"] = csvData["Fertilizer_Type"]
            result["fertilizer_amount_ml"] = csvData["Fertilizer_Amount_ml"]?.toDoubleOrNull()
            result["pest_presence"] = csvData["Pest_Presence"]
            result["pest_severity"] = csvData["Pest_Severity"]
            result["soil_moisture_percent"] = csvData["Soil_Moisture_%"]?.toDoubleOrNull()
            result["soil_type"] = csvData["Soil_Type"]
            result["health_score"] = csvData["Health_Score"]?.toIntOrNull()
            result["data_source"] = "CSV"

            return ResponseEntity.ok(result)
        }

        result["error"] = "Plant data not found in CSV"
        return ResponseEntity.notFound().build()
    }
}