package com.plantpulse.healthservice.domain.notification

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface NotificationRepository : JpaRepository<Notification, UUID> {
    fun findByUserIdOrderByCreatedAtDesc(userId: UUID, pageable: Pageable): Page<Notification>
    fun findByUserIdAndReadFalseOrderByCreatedAtDesc(userId: UUID): List<Notification>
    fun countByUserIdAndReadFalse(userId: UUID): Long
    fun existsByDedupeKey(dedupeKey: String): Boolean
    fun findByIdAndUserId(id: UUID, userId: UUID): Notification?
}
