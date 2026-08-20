package com.plantpulse.plantservice.infrastructure.external

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class PerenualClient(
    @Value("\${plantpulse.perenual.api-key:demo}") private val apiKey: String,
    private val restClient: RestClient
) {
    private val log = LoggerFactory.getLogger(PerenualClient::class.java)
    private val baseUrl = "https://perenual.com/api/species-list"

    fun isConfigured(): Boolean = apiKey.isNotBlank() && apiKey != "demo"

    /** Search Perenual by common name, fetches full details to get care information */
    fun searchPlant(commonName: String): PerenualPlant? {
        return try {
            val response = restClient.get()
                .uri("$baseUrl?q={q}&key={key}", commonName, apiKey)
                .retrieve()
                .body(PerenualSearchResponse::class.java)

            val basic = response?.data?.firstOrNull() ?: return null

            // Try to fetch full details for comprehensive care data
            return try {
                getPlantDetails(basic.id) ?: basic
            } catch (ex: Exception) {
                log.debug("Could not fetch full details for {}, using basic data: {}", basic.common_name, ex.message)
                basic
            }
        } catch (ex: Exception) {
            log.warn("Perenual search failed for '{}': {}", commonName, ex.message)
            null
        }
    }

    /** Get plant details by ID - returns full care information */
    fun getPlantDetails(plantId: Int): PerenualPlant? {
        return try {
            restClient.get()
                .uri("https://perenual.com/api/species/{id}?key={key}", plantId, apiKey)
                .retrieve()
                .body(PerenualPlant::class.java)
        } catch (ex: Exception) {
            log.debug("Detail fetch failed for id {}: {}", plantId, ex.message)
            null
        }
    }

    /** Fetch a catalog of popular plants (no query, just browse) */
    fun getBrowsableCatalog(page: Int = 1): List<PerenualPlant> {
        return try {
            val response = restClient.get()
                .uri("$baseUrl?page={page}&key={key}", page, apiKey)
                .retrieve()
                .body(PerenualSearchResponse::class.java)
            response?.data ?: emptyList()
        } catch (ex: Exception) {
            log.debug("Failed to fetch catalog page {}: {}", page, ex.message)
            emptyList()
        }
    }

    /** Enrich plant data with comprehensive care database or curated guides */
    fun enrichPlantData(plant: PerenualPlant): Map<String, Any?> {
        // Try comprehensive database first
        if (ComprehensivePlantDB.hasComprehensiveData(plant)) {
            return ComprehensivePlantDB.enrichPlantData(plant)
        }
        // Fall back to manual care guides
        return PlantCareGuidesDB.enrichPlantData(plant)
    }

    /** Check if we have detailed care data for a plant */
    fun hasEnrichedData(plant: PerenualPlant): Boolean {
        return ComprehensivePlantDB.hasComprehensiveData(plant) || PlantCareGuidesDB.hasGuide(plant.id)
    }
}
