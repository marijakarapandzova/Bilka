package com.plantpulse.mcpserver.batch

import com.plantpulse.mcpserver.checklist.DailyChecklistBuilder
import com.plantpulse.mcpserver.client.PlantPulseClient
import com.plantpulse.mcpserver.client.SlackClient
import com.plantpulse.mcpserver.crypto.TokenCipher
import com.plantpulse.mcpserver.domain.LinkedAccountRepository
import com.plantpulse.mcpserver.domain.SlackIntegrationRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Mirrors health-service's batch/BatchScheduler.kt: a config-driven daily cron job that pushes
 * every linked user's checklist without requiring an open MCP/chat session (see
 * send_daily_checklist_now in SlackTools for the on-demand equivalent).
 */
@Component
class DailySlackReminderScheduler(
    private val slackIntegrationRepository: SlackIntegrationRepository,
    private val linkedAccountRepository: LinkedAccountRepository,
    private val plantPulseClient: PlantPulseClient,
    private val slackClient: SlackClient,
    private val checklistBuilder: DailyChecklistBuilder,
    private val tokenCipher: TokenCipher
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "\${plantpulse.reminder-cron}")
    fun sendDailyChecklists() {
        val integrations = slackIntegrationRepository.findAll()
        log.info("Running daily Slack checklist job for {} linked user(s)", integrations.size)

        for (integration in integrations) {
            try {
                val account = linkedAccountRepository.findById(integration.plantpulseUserId).orElse(null)
                if (account == null) {
                    log.warn("No linked account for {}, skipping", integration.plantpulseUserId)
                    continue
                }

                val auth = plantPulseClient.login(account.email, tokenCipher.decrypt(account.passwordEncrypted))
                val dashboard = plantPulseClient.getDashboard(auth.token)
                val message = checklistBuilder.build(dashboard)
                slackClient.postMessage(tokenCipher.decrypt(integration.botTokenEncrypted), integration.channelId, message)
            } catch (ex: Exception) {
                log.error("Failed to send daily checklist for {}", integration.plantpulseUserId, ex)
            }
        }
    }
}
