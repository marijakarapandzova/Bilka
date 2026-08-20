package com.plantpulse.plantservice.domain.shared

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface MessageRepository : JpaRepository<Message, UUID> {
    fun findByReceiverIdOrderBySentAtDesc(receiverId: UUID): List<Message>
    fun findBySenderIdOrReceiverIdOrderBySentAtDesc(senderId: UUID, receiverId: UUID): List<Message>
}
