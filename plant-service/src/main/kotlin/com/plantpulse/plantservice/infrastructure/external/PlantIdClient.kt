package com.plantpulse.plantservice.infrastructure.external

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class PlantIdClient(
    @Value("\${plantpulse.plantid.api-key}") private val apiKey: String,
    @Value("\${plantpulse.plantid.api-url}") private val apiUrl: String
) {
    private val log = LoggerFactory.getLogger(PlantIdClient::class.java)
    private val restClient = RestClient.create()

    fun isConfigured(): Boolean = apiKey.isNotBlank()

    /**
     * Sends a base64-encoded image to Plant.id and returns the best match.
     * Returns null if unconfigured, the call fails, or no suggestions come back.
     */
    fun identify(imageBase64: String): PhotoIdentificationResult? {
        if (!isConfigured()) {
            log.warn("PLANT_ID_API_KEY not set — cannot identify photo.")
            return null
        }

        return try {
            val response = restClient.post()
                .uri(apiUrl)
                .header("Api-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(PlantIdRequest(images = listOf(imageBase64)))
                .retrieve()
                .body(PlantIdResponse::class.java)

            val best = response?.suggestions?.maxByOrNull { it.probability } ?: return null

            PhotoIdentificationResult(
                matchedName = best.plant_name,
                confidencePercent = (best.probability * 100).toInt()
            )
        } catch (ex: Exception) {
            log.warn("Plant.id identification failed: {}", ex.message)
            null
        }
    }
}
