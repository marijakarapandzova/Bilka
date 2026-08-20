package com.plantpulse.healthservice.api.notification

import com.plantpulse.healthservice.application.NotificationService
import com.plantpulse.healthservice.domain.notification.Notification
import com.plantpulse.healthservice.security.CurrentUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Watering reminders, disease alerts and regional outbreak alerts")
class NotificationController(private val notificationService: NotificationService) {

    @GetMapping
    @Operation(summary = "List my notifications", description = "Newest first, paginated")
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): Page<NotificationResponse> =
        notificationService.listForUser(CurrentUser.id(), PageRequest.of(page, size)).map { it.toResponse() }

    @GetMapping("/unread")
    @Operation(summary = "List my unread notifications")
    fun unread(): List<NotificationResponse> = notificationService.unread(CurrentUser.id()).map { it.toResponse() }

    @GetMapping("/unread-count")
    @Operation(summary = "Count my unread notifications")
    fun unreadCount(): Map<String, Long> = mapOf("unread" to notificationService.unreadCount(CurrentUser.id()))

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    fun markRead(@PathVariable id: UUID): NotificationResponse =
        notificationService.markRead(id, CurrentUser.id()).toResponse()

    @PostMapping("/read-all")
    @Operation(summary = "Mark all my notifications as read")
    fun markAllRead() = notificationService.markAllRead(CurrentUser.id())

    private fun Notification.toResponse() = NotificationResponse(
        id = id,
        plantId = plantId,
        type = type.name,
        title = title,
        message = message,
        read = read,
        createdAt = createdAt
    )
}
