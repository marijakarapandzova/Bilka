package com.plantpulse.plantservice.api.shared

import java.time.Instant
import java.util.UUID

data class MessageResponse(
    val id: UUID,
    val senderId: UUID,
    val senderName: String,
    val receiverId: UUID,
    val content: String,
    val sentAt: Instant
)

data class SendMessageRequest(
    val receiverId: UUID,
    val content: String
)
