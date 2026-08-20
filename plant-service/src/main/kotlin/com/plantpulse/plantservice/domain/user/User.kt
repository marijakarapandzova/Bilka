package com.plantpulse.plantservice.domain.user

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
class User(

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var passwordHash: String,

    @Embedded
    var location: Location? = null,

    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
) {
    // JPA needs a no-arg constructor
    protected constructor() : this(
        email = "",
        passwordHash = "",
        location = null
    )

    fun updateLocation(newLocation: Location) {
        this.location = newLocation
    }
}
