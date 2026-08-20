package com.plantpulse.plantservice.domain.shared

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "shared_plants")
class SharedPlant(
    @Column(nullable = false)
    val userId: UUID,

    @Column(nullable = false)
    val plantId: UUID,

    @Column(nullable = false)
    val plantName: String,

    @Column(nullable = false)
    val speciesName: String,

    @Column(nullable = false, length = 100)
    val city: String,

    @Column(length = 2000)
    val photoUrl: String? = null,

    @Column(nullable = false)
    val userName: String,

    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, updatable = false)
    val sharedAt: Instant = Instant.now()
) {
    protected constructor() : this(
        userId = UUID.randomUUID(),
        plantId = UUID.randomUUID(),
        plantName = "",
        speciesName = "",
        city = "",
        photoUrl = null,
        userName = ""
    )
}
