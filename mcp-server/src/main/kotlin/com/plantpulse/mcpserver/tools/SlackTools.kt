package com.plantpulse.mcpserver.tools

import com.plantpulse.mcpserver.checklist.DailyChecklistBuilder
import com.plantpulse.mcpserver.client.PlantPulseClient
import com.plantpulse.mcpserver.client.SlackClient
import com.plantpulse.mcpserver.crypto.TokenCipher
import com.plantpulse.mcpserver.domain.SlackIntegrationRepository
import com.plantpulse.mcpserver.session.SessionStore
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service

@Service
class SlackTools(
    private val sessionStore: SessionStore,
    private val slackClient: SlackClient,
    private val slackIntegrationRepository: SlackIntegrationRepository,
    private val plantPulseClient: PlantPulseClient,
    private val checklistBuilder: DailyChecklistBuilder,
    private val tokenCipher: TokenCipher
) {

    @Tool(
        description = "Get a Slack 'Add to Slack' install link for the logged-in PlantPulse user. Once they " +
            "click it and approve, PlantPulse will DM them a daily watering checklist every morning."
    )
    fun getSlackInstallLink(
        @ToolParam(description = "session_token returned by plantpulse_login") sessionToken: String
    ): String {
        val session = sessionStore.get(sessionToken)
        val url = slackClient.buildInstallUrl(state = session.plantpulseUserId.toString())
        return "Send the user this link to connect Slack: $url"
    }

    @Tool(description = "Build today's watering/care checklist for the logged-in user and post it to their connected Slack DM right now.")
    fun sendDailyChecklistNow(
        @ToolParam(description = "session_token returned by plantpulse_login") sessionToken: String
    ): String {
        val session = sessionStore.get(sessionToken)
        val integration = slackIntegrationRepository.findByPlantpulseUserId(session.plantpulseUserId)
            .orElseThrow { IllegalStateException("No Slack workspace linked yet. Call get_slack_install_link first.") }

        val dashboard = plantPulseClient.getDashboard(session.jwt)
        val message = checklistBuilder.build(dashboard)
        slackClient.postMessage(tokenCipher.decrypt(integration.botTokenEncrypted), integration.channelId, message)

        return "Checklist sent to Slack."
    }
}
