package com.plantpulse.plantservice.infrastructure.external

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class TrefleClient(
    @Value("\${plantpulse.trefle.api-key}") private val apiKey: String,
    @Value("\${plantpulse.trefle.base-url}") private val baseUrl: String,
    private val restClient: RestClient
) {
    private val log = LoggerFactory.getLogger(TrefleClient::class.java)

    fun isConfigured(): Boolean = apiKey.isNotBlank()

    /** Searches Trefle by common name, returns the first match's Trefle id, or null. */
    fun searchFirstMatchId(commonName: String): Long? {
        return try {
            val response = restClient.get()
                .uri("$baseUrl/plants/search?q={q}&token={token}", commonName, apiKey)
                .retrieve()
                .body(TrefleSearchResponse::class.java)

            response?.data?.firstOrNull()?.id
        } catch (ex: Exception) {
            log.warn("Trefle search failed for '{}': {}", commonName, ex.message)
            null
        }
    }

    /** Fetches full growth details for a Trefle plant id. */
    fun getSpeciesDetail(trefleId: Long): TrefleSpeciesDetail? {
        return try {
            val response = restClient.get()
                .uri("$baseUrl/species/{id}?token={token}", trefleId, apiKey)
                .retrieve()
                .body(TrefleSpeciesDetailResponse::class.java)

            response?.data
        } catch (ex: Exception) {
            log.warn("Trefle detail fetch failed for id {}: {}", trefleId, ex.message)
            null
        }
    }
}
