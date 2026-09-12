package com.plantpulse.mcpserver.domain

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.util.Optional
import java.util.UUID

/** A PlantPulse user's linked Slack workspace + the DM channel their daily checklist is posted to. */
@Entity
@Table(name = "slack_integrations")
class SlackIntegration(
    @Id
    val plantpulseUserId: UUID,

    var teamId: String,
    var botTokenEncrypted: String,
    var channelId: String,
    var installedAt: Instant = Instant.now()
) {
    protected constructor() : this(UUID.randomUUID(), "", "", "")
}

interface SlackIntegrationRepository : JpaRepository<SlackIntegration, UUID> {
    fun findByPlantpulseUserId(plantpulseUserId: UUID): Optional<SlackIntegration>
}
