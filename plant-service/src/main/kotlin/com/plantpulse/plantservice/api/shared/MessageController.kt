package com.plantpulse.plantservice.api.shared

import com.plantpulse.plantservice.application.SharedPlantService
import com.plantpulse.plantservice.security.CurrentUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/messages")
@Tag(name = "Messages API", description = "Communicate with other plant enthusiasts")
class MessageController(private val sharedPlantService: SharedPlantService) {

    @PostMapping
    @Operation(summary = "Send message", description = "Send a message to another user")
    fun sendMessage(@Valid @RequestBody request: SendMessageRequest): ResponseEntity<MessageResponse> =
        ResponseEntity.status(201).body(
            sharedPlantService.sendMessage(
                CurrentUser.id(),
                "User",
                request
            )
        )

    @GetMapping("/inbox")
    @Operation(summary = "Get inbox", description = "Retrieve all messages in your inbox")
    fun getInbox(): List<MessageResponse> =
        sharedPlantService.getInboxMessages(CurrentUser.id())

    @GetMapping("/conversation/{userId}")
    @Operation(summary = "Get conversation", description = "Retrieve all messages in a conversation with a specific user")
    fun getConversation(@PathVariable userId: UUID): List<MessageResponse> =
        sharedPlantService.getConversation(CurrentUser.id(), userId)
}





