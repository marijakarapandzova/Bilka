package com.plantpulse.mcpserver.tools

import com.plantpulse.mcpserver.client.PlantPulseClient
import com.plantpulse.mcpserver.crypto.TokenCipher
import com.plantpulse.mcpserver.domain.LinkedAccount
import com.plantpulse.mcpserver.domain.LinkedAccountRepository
import com.plantpulse.mcpserver.session.LinkedSession
import com.plantpulse.mcpserver.session.SessionStore
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class PlantPulseTools(
    private val plantPulseClient: PlantPulseClient,
    private val sessionStore: SessionStore,
    private val linkedAccountRepository: LinkedAccountRepository,
    private val tokenCipher: TokenCipher
) {

    @Tool(description = "Log in to PlantPulse with an email and password. Returns a session_token that must be passed to every other PlantPulse tool in this conversation.")
    fun plantpulseLogin(
        @ToolParam(description = "The user's PlantPulse account email") email: String,
        @ToolParam(description = "The user's PlantPulse account password") password: String
    ): String {
        val auth = plantPulseClient.login(email, password)
        val sessionToken = sessionStore.put(LinkedSession(auth.userId, auth.email, auth.token))

        // Password is re-used by the daily Slack cron job to re-authenticate without an open
        // chat session (see LinkedAccount doc comment) — stored encrypted, never in plaintext.
        linkedAccountRepository.save(
            LinkedAccount(auth.userId, auth.email, tokenCipher.encrypt(password), Instant.now())
        )

        return "Logged in as ${auth.email}. session_token: $sessionToken " +
            "(pass this to other PlantPulse tools for the rest of this conversation)."
    }

    @Tool(description = "List the logged-in user's plants, including each plant's id, nickname and species. Use this to resolve a plant name to its id.")
    fun listMyPlants(
        @ToolParam(description = "session_token returned by plantpulse_login") sessionToken: String
    ): String {
        val session = sessionStore.get(sessionToken)
        val plants = plantPulseClient.listPlants(session.jwt)
        if (plants.isEmpty()) return "No plants found in this garden yet."

        return plants.joinToString("\n") {
            "- ${it.nickname} (${it.speciesName}) — id: ${it.id}, room: ${it.room ?: "unspecified"}, " +
                "waters every ${it.wateringFrequencyDays} day(s), last watered: ${it.lastWateredAt ?: "never"}"
        }
    }
}
