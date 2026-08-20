package com.plantpulse.plantservice.domain.plant

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "plants")
class Plant(

    @Column(nullable = false)
    val userId: UUID,

    @Column(nullable = false)
    val speciesId: UUID,

    @Column(nullable = false)
    var nickname: String,

    var room: String? = null,

    var currentPhotoUrl: String? = null,

    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, updatable = false)
    val addedAt: Instant = Instant.now(),

    var lastWateredAt: Instant? = null
) {
    protected constructor() : this(
        userId = UUID.randomUUID(),
        speciesId = UUID.randomUUID(),
        nickname = ""
    )

    fun rename(newNickname: String) {
        this.nickname = newNickname
    }

    fun moveToRoom(newRoom: String) {
        this.room = newRoom
    }

    fun updatePhoto(url: String) {
        this.currentPhotoUrl = url
    }

    fun logWatering() {
        this.lastWateredAt = Instant.now()
    }
}
