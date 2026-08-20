package com.plantpulse.healthservice.domain.notification

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

enum class NotificationType {
    WATERING_REMINDER,
    DISEASE_ALERT,
    HEALTH_DECLINE,
    RECOVERY,
    OUTBREAK_ALERT
}

@Entity
@Table(name = "notifications")
class Notification(

    @Column(nullable = false)
    val userId: UUID,

    val plantId: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: NotificationType,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false, length = 1000)
    val message: String,

    // Prevents the same reminder/alert from being created twice for the same
    // day/plant/disease (e.g. "WATERING:{plantId}:2026-07-31"). Checked via
    // NotificationRepository.existsByDedupeKey before insert.
    @Column(length = 200)
    val dedupeKey: String? = null,

    @Column(nullable = false)
    var read: Boolean = false,

    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
) {
    protected constructor() : this(
        userId = UUID.randomUUID(),
        type = NotificationType.WATERING_REMINDER,
        title = "",
        message = ""
    )

    fun markRead() {
        read = true
    }
}
