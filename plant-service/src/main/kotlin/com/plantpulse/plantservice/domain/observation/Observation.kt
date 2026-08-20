package com.plantpulse.plantservice.domain.observation

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

enum class LeafColor { GREEN, YELLOW, BROWN, SPOTTED }
enum class LeafTexture { HEALTHY, WILTING, MUSHY, CRISPY }
enum class SoilMoisture { DRY, MOIST, WATERLOGGED }
enum class Growth { NORMAL, SLOW, STUNTED, NONE }

@Entity
@Table(name = "observations")
class Observation(

    @Column(nullable = false)
    val plantId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var leafColor: LeafColor,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var leafTexture: LeafTexture,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var soilMoisture: SoilMoisture,

    @Column(nullable = false)
    var visiblePests: Boolean,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var growth: Growth,

    @Column(length = 1000)
    var notes: String? = null,

    var photoUrl: String? = null,

    // Kaggle dataset compatible fields - actual measurements
    var heightCm: Double? = null,
    var leafCount: Int? = null,
    var newGrowthCount: Int? = null,
    var healthNotes: String? = null,
    var wateringAmountMl: Double? = null,
    var wateringFrequencyDays: Int? = null,
    var sunlightExposure: String? = null,
    var roomTemperatureC: Double? = null,
    var humidityPercent: Double? = null,
    var fertilizerType: String? = null,
    var fertilizerAmountMl: Double? = null,
    var pestPresence: String? = null,
    var pestSeverity: String? = null,
    var soilMoisturePercent: Double? = null,
    var soilType: String? = null,
    var healthScore: Int? = null,

    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, updatable = false)
    val loggedAt: Instant = Instant.now()
) {
    protected constructor() : this(
        plantId = UUID.randomUUID(),
        leafColor = LeafColor.GREEN,
        leafTexture = LeafTexture.HEALTHY,
        soilMoisture = SoilMoisture.MOIST,
        visiblePests = false,
        growth = Growth.NORMAL
    )
}
