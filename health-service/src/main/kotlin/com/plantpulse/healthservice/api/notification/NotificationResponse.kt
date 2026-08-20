package com.plantpulse.healthservice.api.notification

import java.time.Instant
import java.util.UUID

data class NotificationResponse(
    val id: UUID,
    val plantId: UUID?,
    val type: String,
    val title: String,
    val message: String,
    val read: Boolean,
    val createdAt: Instant
)
