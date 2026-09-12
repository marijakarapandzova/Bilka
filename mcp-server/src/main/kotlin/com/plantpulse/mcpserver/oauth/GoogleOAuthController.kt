package com.plantpulse.mcpserver.oauth

import com.plantpulse.mcpserver.client.GoogleCalendarClient
import com.plantpulse.mcpserver.crypto.TokenCipher
import com.plantpulse.mcpserver.domain.GoogleCalendarIntegration
import com.plantpulse.mcpserver.domain.GoogleCalendarIntegrationRepository
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/** Plain (non-MCP) HTTP endpoint Google redirects the browser to after the user grants consent. */
@RestController
class GoogleOAuthController(
    private val googleCalendarClient: GoogleCalendarClient,
    private val googleCalendarIntegrationRepository: GoogleCalendarIntegrationRepository,
    private val tokenCipher: TokenCipher
) {

    @GetMapping("/google/oauth/callback", produces = [MediaType.TEXT_HTML_VALUE])
    fun callback(@RequestParam code: String, @RequestParam state: String): ResponseEntity<String> {
        val plantpulseUserId = UUID.fromString(state)
        val tokens = googleCalendarClient.exchangeCode(code)
        val refreshToken = tokens.refreshToken
            ?: error(
                "Google did not return a refresh token. If you've authorized this app before, revoke access " +
                    "at https://myaccount.google.com/permissions and try again so Google issues a fresh one."
            )

        googleCalendarIntegrationRepository.save(
            GoogleCalendarIntegration(plantpulseUserId, tokenCipher.encrypt(refreshToken))
        )

        return ResponseEntity.ok(
            "<html><body><h3>Google Calendar connected!</h3>" +
                "<p>Ask the assistant to run sync_watering_calendar to add your watering schedule. You can close this tab.</p>" +
                "</body></html>"
        )
    }
}
