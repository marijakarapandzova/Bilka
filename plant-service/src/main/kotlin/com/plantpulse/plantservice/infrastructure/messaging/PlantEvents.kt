package com.plantpulse.plantservice.infrastructure.messaging

import java.time.Instant
import java.util.UUID

data class PlantAddedEvent(
    val plantId: UUID,
    val userId: UUID,
    val speciesId: UUID,
    val wateringFrequencyDays: Int,
    val addedAt: Instant = Instant.now()
)

data class ObservationLoggedEvent(
    val observationId: UUID,
    val plantId: UUID,
    val userId: UUID,
    val diseaseMatchName: String,
    val diseaseMatchPercentage: Int,
    val cityLocation: String?,
    val loggedAt: Instant = Instant.now()
)

data class PlantRemovedEvent(
    val plantId: UUID,
    val userId: UUID,
    val removedAt: Instant = Instant.now()
)
