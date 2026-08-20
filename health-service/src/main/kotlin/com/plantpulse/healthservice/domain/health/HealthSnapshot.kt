package com.plantpulse.healthservice.domain.health

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

/**
 * One point on a plant's health timeline (the "Jan 95 -> Apr 41 -> May 58"
 * graph). Written on every observation and, for plants that haven't been
 * observed recently, once per day by the decay batch job — so the timeline
 * never has gaps even if the user stops logging observations.
 *
 * cityLocation is denormalized from PlantHealthProfile purely so the
 * outbreak-detection query can group by city without a join.
 */
@Entity
@Table(name = "health_snapshots")
class HealthSnapshot(

    @Column(nullable = false)
    val plantId: UUID,

    @Column(nullable = false)
    val userId: UUID,

    @Column(nullable = false)
    val healthScore: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: HealthStatus,

    val diseaseName: String? = null,
    val diseaseMatchPercentage: Int? = null,

    val cityLocation: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val source: SnapshotSource,

    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, updatable = false)
    val recordedAt: Instant = Instant.now()
) {
    protected constructor() : this(
        plantId = UUID.randomUUID(),
        userId = UUID.randomUUID(),
        healthScore = 100,
        status = HealthStatus.HEALTHY,
        source = SnapshotSource.INITIAL
    )
}
