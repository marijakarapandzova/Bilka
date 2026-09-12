package com.plantpulse.mcpserver.oauth

import com.plantpulse.mcpserver.client.SlackClient
import com.plantpulse.mcpserver.crypto.TokenCipher
import com.plantpulse.mcpserver.domain.SlackIntegration
import com.plantpulse.mcpserver.domain.SlackIntegrationRepository
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Plain (non-MCP) HTTP endpoint Slack redirects the browser to after the user approves the
 * app install. `state` carries the plantpulseUserId that get_slack_install_link embedded.
 */
@RestController
class SlackOAuthController(
    private val slackClient: SlackClient,
    private val slackIntegrationRepository: SlackIntegrationRepository,
    private val tokenCipher: TokenCipher
) {

    @GetMapping("/slack/oauth/callback", produces = [MediaType.TEXT_HTML_VALUE])
    fun callback(@RequestParam code: String, @RequestParam state: String): ResponseEntity<String> {
        val plantpulseUserId = UUID.fromString(state)
        val oauth = slackClient.exchangeCode(code)
        val botToken = oauth.accessToken ?: error("Slack did not return a bot token")
        val teamId = oauth.team?.id ?: error("Slack did not return a team id")
        val slackUserId = oauth.authedUser?.id ?: error("Slack did not return the authorizing user's id")

        val channelId = slackClient.openDirectMessage(botToken, slackUserId)

        slackIntegrationRepository.save(
            SlackIntegration(
                plantpulseUserId = plantpulseUserId,
                teamId = teamId,
                botTokenEncrypted = tokenCipher.encrypt(botToken),
                channelId = channelId
            )
        )

        return ResponseEntity.ok(
            "<html><body><h3>Slack connected!</h3>" +
                "<p>You'll get a daily PlantPulse watering checklist in Slack. You can close this tab.</p>" +
                "</body></html>"
        )
    }
}
