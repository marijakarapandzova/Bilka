package com.plantpulse.mcpserver.client

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.plantpulse.mcpserver.config.McpProperties
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class GoogleTokenResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("refresh_token") val refreshToken: String? = null,
    @JsonProperty("expires_in") val expiresIn: Long = 0
)

data class CalendarDate(val date: String)
data class CalendarEventRequest(
    val summary: String,
    val description: String? = null,
    val start: CalendarDate,
    val end: CalendarDate,
    val recurrence: List<String>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CalendarEventResponse(val id: String)

/** Thin wrapper around Google's OAuth2 + Calendar v3 REST APIs. */
@Component
class GoogleCalendarClient(private val properties: McpProperties) {

    private val oauthClient = RestClient.create("https://oauth2.googleapis.com")
    private val calendarClient = RestClient.create("https://www.googleapis.com/calendar/v3")

    fun buildAuthUrl(state: String): String {
        val redirect = URLEncoder.encode(properties.google.redirectUri, StandardCharsets.UTF_8)
        val scope = URLEncoder.encode("https://www.googleapis.com/auth/calendar.events", StandardCharsets.UTF_8)
        return "https://accounts.google.com/o/oauth2/v2/auth" +
            "?client_id=${properties.google.clientId}" +
            "&redirect_uri=$redirect" +
            "&response_type=code" +
            "&access_type=offline" +
            "&prompt=consent" +
            "&scope=$scope" +
            "&state=$state"
    }

    fun exchangeCode(code: String): GoogleTokenResponse {
        val form: MultiValueMap<String, String> = LinkedMultiValueMap()
        form.add("client_id", properties.google.clientId)
        form.add("client_secret", properties.google.clientSecret)
        form.add("code", code)
        form.add("redirect_uri", properties.google.redirectUri)
        form.add("grant_type", "authorization_code")

        return oauthClient.post().uri("/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(GoogleTokenResponse::class.java)
            ?: error("Google returned an empty token response")
    }

    private fun refreshAccessToken(refreshToken: String): String {
        val form: MultiValueMap<String, String> = LinkedMultiValueMap()
        form.add("client_id", properties.google.clientId)
        form.add("client_secret", properties.google.clientSecret)
        form.add("refresh_token", refreshToken)
        form.add("grant_type", "refresh_token")

        val response = oauthClient.post().uri("/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(GoogleTokenResponse::class.java)
            ?: error("Google returned an empty token-refresh response")
        return response.accessToken
    }

    /**
     * Creates the recurring "water this plant" event if [existingEventId] is null, otherwise
     * updates it in place. Returns the event id to persist for next time.
     */
    fun upsertWateringEvent(
        refreshToken: String,
        calendarId: String,
        existingEventId: String?,
        plantNickname: String,
        wateringFrequencyDays: Int
    ): String {
        val accessToken = refreshAccessToken(refreshToken)
        val today = LocalDate.now()
        val request = CalendarEventRequest(
            summary = "💧 Water $plantNickname",
            description = "Auto-synced from PlantPulse. Repeats every $wateringFrequencyDays day(s).",
            start = CalendarDate(today.toString()),
            end = CalendarDate(today.plusDays(1).toString()),
            recurrence = listOf("RRULE:FREQ=DAILY;INTERVAL=$wateringFrequencyDays")
        )

        val response = if (existingEventId == null) {
            calendarClient.post()
                .uri("/calendars/{calendarId}/events", calendarId)
                .header("Authorization", "Bearer $accessToken")
                .body(request)
                .retrieve()
                .body(CalendarEventResponse::class.java)
        } else {
            calendarClient.put()
                .uri("/calendars/{calendarId}/events/{eventId}", calendarId, existingEventId)
                .header("Authorization", "Bearer $accessToken")
                .body(request)
                .retrieve()
                .body(CalendarEventResponse::class.java)
        }

        return response?.id ?: error("Google Calendar returned no event id")
    }
}
