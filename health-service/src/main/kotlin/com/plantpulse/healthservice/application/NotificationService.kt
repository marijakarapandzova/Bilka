package com.plantpulse.healthservice.application

import com.plantpulse.healthservice.domain.notification.Notification
import com.plantpulse.healthservice.domain.notification.NotificationRepository
import com.plantpulse.healthservice.domain.notification.NotificationType
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository
) {
    private val log = LoggerFactory.getLogger(NotificationService::class.java)

    /** Creates a notification, silently skipping it if dedupeKey already exists today. */
    @Transactional
    fun createIfAbsent(
        userId: UUID,
        plantId: UUID?,
        type: NotificationType,
        title: String,
        message: String,
        dedupeKey: String?
    ): Notification? {
        if (dedupeKey != null && notificationRepository.existsByDedupeKey(dedupeKey)) {
            log.debug("Skipping duplicate notification for dedupeKey={}", dedupeKey)
            return null
        }
        return notificationRepository.save(
            Notification(
                userId = userId,
                plantId = plantId,
                type = type,
                title = title,
                message = message,
                dedupeKey = dedupeKey
            )
        )
    }

    fun listForUser(userId: UUID, pageable: Pageable): Page<Notification> =
        notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)

    fun unread(userId: UUID): List<Notification> =
        notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)

    fun unreadCount(userId: UUID): Long = notificationRepository.countByUserIdAndReadFalse(userId)

    @Transactional
    fun markRead(id: UUID, userId: UUID): Notification {
        val notification = notificationRepository.findByIdAndUserId(id, userId)
            ?: throw NotificationNotFoundException(id)
        notification.markRead()
        return notificationRepository.save(notification)
    }

    @Transactional
    fun markAllRead(userId: UUID) {
        val unread = notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
        unread.forEach { it.markRead() }
        notificationRepository.saveAll(unread)
    }
}

class NotificationNotFoundException(id: UUID) : RuntimeException("Notification not found: $id")
