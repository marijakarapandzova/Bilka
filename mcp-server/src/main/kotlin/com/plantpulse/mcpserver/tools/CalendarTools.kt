package com.plantpulse.mcpserver.tools

import com.plantpulse.mcpserver.client.GoogleCalendarClient
import com.plantpulse.mcpserver.client.PlantPulseClient
import com.plantpulse.mcpserver.crypto.TokenCipher
import com.plantpulse.mcpserver.domain.GoogleCalendarIntegrationRepository
import com.plantpulse.mcpserver.domain.PlantCalendarSync
import com.plantpulse.mcpserver.domain.PlantCalendarSyncRepository
import com.plantpulse.mcpserver.session.SessionStore
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service

@Service
class CalendarTools(
    private val sessionStore: SessionStore,
    private val googleCalendarClient: GoogleCalendarClient,
    private val googleCalendarIntegrationRepository: GoogleCalendarIntegrationRepository,
    private val plantCalendarSyncRepository: PlantCalendarSyncRepository,
    private val plantPulseClient: PlantPulseClient,
    private val tokenCipher: TokenCipher
) {

    @Tool(description = "Get a Google OAuth consent link for the logged-in PlantPulse user, needed before sync_watering_calendar can run.")
    fun getGoogleCalendarAuthLink(
        @ToolParam(description = "session_token returned by plantpulse_login") sessionToken: String
    ): String {
        val session = sessionStore.get(sessionToken)
        val url = googleCalendarClient.buildAuthUrl(state = session.plantpulseUserId.toString())
        return "Send the user this link to connect Google Calendar: $url"
    }

    @Tool(
        description = "Create or update a recurring 'water this plant' Google Calendar event for every plant " +
            "the logged-in user owns, based on each plant's watering frequency. Requires Google Calendar to " +
            "already be connected via get_google_calendar_auth_link."
    )
    fun syncWateringCalendar(
        @ToolParam(description = "session_token returned by plantpulse_login") sessionToken: String
    ): String {
        val session = sessionStore.get(sessionToken)
        val integration = googleCalendarIntegrationRepository.findById(session.plantpulseUserId)
            .orElseThrow { IllegalStateException("No Google account linked yet. Call get_google_calendar_auth_link first.") }

        val refreshToken = tokenCipher.decrypt(integration.refreshTokenEncrypted)
        val plants = plantPulseClient.listPlants(session.jwt)
        var synced = 0

        for (plant in plants) {
            val existing = plantCalendarSyncRepository.findByPlantIdAndPlantpulseUserId(plant.id, session.plantpulseUserId)
            val eventId = googleCalendarClient.upsertWateringEvent(
                refreshToken = refreshToken,
                calendarId = integration.calendarId,
                existingEventId = existing.map { it.googleEventId }.orElse(null),
                plantNickname = plant.nickname,
                wateringFrequencyDays = plant.wateringFrequencyDays
            )

            if (existing.isPresent) {
                existing.get().googleEventId = eventId
                existing.get().wateringFrequencyDays = plant.wateringFrequencyDays
                plantCalendarSyncRepository.save(existing.get())
            } else {
                plantCalendarSyncRepository.save(
                    PlantCalendarSync(plant.id, session.plantpulseUserId, eventId, plant.wateringFrequencyDays)
                )
            }
            synced++
        }

        return "Synced $synced plant(s) to Google Calendar."
    }
}
