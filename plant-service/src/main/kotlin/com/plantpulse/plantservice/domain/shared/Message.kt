package com.plantpulse.plantservice.domain.shared

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "messages")
class Message(
    @Column(nullable = false)
    val senderId: UUID,

    @Column(nullable = false)
    val senderName: String,

    @Column(nullable = false)
    val receiverId: UUID,

    @Column(nullable = false, length = 2000)
    val content: String,

    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, updatable = false)
    val sentAt: Instant = Instant.now()
) {
    protected constructor() : this(
        senderId = UUID.randomUUID(),
        senderName = "",
        receiverId = UUID.randomUUID(),
        content = ""
    )
}
