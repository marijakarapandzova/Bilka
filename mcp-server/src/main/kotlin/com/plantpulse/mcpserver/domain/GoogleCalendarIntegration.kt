package com.plantpulse.mcpserver.domain

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.util.UUID

/** A PlantPulse user's linked Google account for watering-schedule calendar sync. */
@Entity
@Table(name = "google_calendar_integrations")
class GoogleCalendarIntegration(
    @Id
    val plantpulseUserId: UUID,

    var refreshTokenEncrypted: String,
    var calendarId: String = "primary",
    var connectedAt: Instant = Instant.now()
) {
    protected constructor() : this(UUID.randomUUID(), "")
}

interface GoogleCalendarIntegrationRepository : JpaRepository<GoogleCalendarIntegration, UUID>
