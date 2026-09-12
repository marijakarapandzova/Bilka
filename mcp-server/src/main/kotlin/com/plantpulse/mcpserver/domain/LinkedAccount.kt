package com.plantpulse.mcpserver.domain

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.util.UUID

/**
 * One row per PlantPulse user who has ever logged in through the MCP server.
 *
 * [passwordEncrypted] is stored (AES-GCM, see TokenCipher) so the daily Slack reminder cron
 * job can re-authenticate to plant-service on the user's behalf without an open chat session
 * — PlantPulse JWTs expire, so a session_token/JWT captured during plantpulse_login can't be
 * relied on hours later when the cron fires. This mirrors plant-service's own auth model
 * (email+password, no OAuth) and is the tradeoff of automating a login-gated daily digest.
 */
@Entity
@Table(name = "linked_accounts")
class LinkedAccount(
    @Id
    val plantpulseUserId: UUID,

    var email: String,
    var passwordEncrypted: String,

    var lastLoginAt: Instant = Instant.now()
) {
    protected constructor() : this(UUID.randomUUID(), "", "")
}

interface LinkedAccountRepository : JpaRepository<LinkedAccount, UUID>
