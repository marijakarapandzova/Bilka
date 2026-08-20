package com.plantpulse.healthservice.domain.outbreak

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

/**
 * A detected disease cluster: N-or-more distinct plants in the same city
 * matched the same disease within the rolling detection window. Stays
 * `active` until a sweep of the window no longer finds it above threshold.
 */
@Entity
@Table(name = "regional_outbreaks")
class RegionalOutbreak(

    @Column(nullable = false)
    var cityLocation: String,

    @Column(nullable = false)
    var diseaseName: String,

    @Column(nullable = false)
    var affectedPlantCount: Int,

    @Column(nullable = false)
    var windowStart: Instant,

    @Column(nullable = false)
    var windowEnd: Instant,

    @Column(nullable = false)
    var active: Boolean = true,

    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, updatable = false)
    val detectedAt: Instant = Instant.now(),

    @Column(nullable = false)
    var lastUpdatedAt: Instant = Instant.now()
) {
    protected constructor() : this(
        cityLocation = "",
        diseaseName = "",
        affectedPlantCount = 0,
        windowStart = Instant.now(),
        windowEnd = Instant.now()
    )

    fun refresh(affectedPlantCount: Int, windowStart: Instant, windowEnd: Instant) {
        this.affectedPlantCount = affectedPlantCount
        this.windowStart = windowStart
        this.windowEnd = windowEnd
        this.active = true
        this.lastUpdatedAt = Instant.now()
    }

    fun deactivate() {
        this.active = false
        this.lastUpdatedAt = Instant.now()
    }
}
