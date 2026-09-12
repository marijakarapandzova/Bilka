package com.plantpulse.mcpserver.session

import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class LinkedSession(
    val plantpulseUserId: UUID,
    val email: String,
    val jwt: String,
    val createdAt: Instant = Instant.now()
)

/**
 * Holds the PlantPulse login behind each MCP tool call.
 *
 * MCP tool calls carry no built-in notion of "who is calling", so `plantpulse_login`
 * mints an opaque session_token here and every other PlantPulse-scoped tool takes that
 * token as a parameter. It's in-memory and per-process by design: it only needs to
 * outlive one chat session, not survive a server restart.
 */
@Component
class SessionStore {
    private val sessions = ConcurrentHashMap<String, LinkedSession>()

    fun put(session: LinkedSession): String {
        val token = UUID.randomUUID().toString()
        sessions[token] = session
        return token
    }

    fun get(sessionToken: String): LinkedSession =
        sessions[sessionToken]
            ?: throw IllegalArgumentException(
                "Unknown or expired session_token. Call plantpulse_login again to get a new one."
            )
}
