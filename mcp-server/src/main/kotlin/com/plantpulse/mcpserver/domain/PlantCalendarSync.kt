package com.plantpulse.mcpserver.domain

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.util.Optional
import java.util.UUID

/** Maps a plant to the recurring Google Calendar event that reminds the user to water it. */
@Entity
@Table(name = "plant_calendar_syncs")
class PlantCalendarSync(
    @Id
    val plantId: UUID,

    val plantpulseUserId: UUID,
    var googleEventId: String,
    var wateringFrequencyDays: Int,
    var updatedAt: Instant = Instant.now()
) {
    protected constructor() : this(UUID.randomUUID(), UUID.randomUUID(), "", 0)
}

interface PlantCalendarSyncRepository : JpaRepository<PlantCalendarSync, UUID> {
    fun findByPlantpulseUserId(plantpulseUserId: UUID): List<PlantCalendarSync>
    fun findByPlantIdAndPlantpulseUserId(plantId: UUID, plantpulseUserId: UUID): Optional<PlantCalendarSync>
}
