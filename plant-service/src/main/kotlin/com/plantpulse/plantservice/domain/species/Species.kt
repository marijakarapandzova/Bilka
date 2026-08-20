package com.plantpulse.plantservice.domain.species

import jakarta.persistence.*
import java.util.UUID

enum class LightNeeds {
    LOW, MEDIUM, BRIGHT_INDIRECT, DIRECT_SUN
}

enum class CareDifficulty {
    EASY, MODERATE, DIFFICULT
}

@Entity
@Table(name = "species")
class Species(

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var scientificName: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var careDifficulty: CareDifficulty,

    @Column(nullable = false)
    var wateringFrequencyDays: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var lightNeeds: LightNeeds,

    @Column(length = 1000)
    var description: String? = null,

    @Column(length = 500)
    var imageUrl: String? = null,

    @Column(length = 255)
    var familyCommonName: String? = null,

    var lightLevel: Int? = null, // 0-10 scale from Trefle

    var atmosphericHumidity: Int? = null, // 0-10 scale from Trefle

    @Column(length = 50)
    var wateringFrequency: String? = null, // "none", "minimum", "average", "frequent"

    @Column(length = 500)
    var temperatureMin: String? = null,

    @Column(length = 500)
    var temperatureMax: String? = null,

    @Column(length = 500)
    var phMin: String? = null,

    @Column(length = 500)
    var phMax: String? = null,

    @Column(length = 1000)
    var soilDescription: String? = null,

    @Column(length = 1000)
    var flowerDescription: String? = null,

    @Column(length = 1000)
    var fruitDescription: String? = null,

    @Column(length = 500)
    var growthHabit: String? = null,

    @Column(length = 500)
    var nativeRange: String? = null,

    @Column(length = 10000)
    var fullTrefleData: String? = null, // Store raw JSON for complete data

    @Id
    val id: UUID = UUID.randomUUID()
) {
    protected constructor() : this(
        name = "",
        scientificName = "",
        careDifficulty = CareDifficulty.EASY,
        wateringFrequencyDays = 7,
        lightNeeds = LightNeeds.MEDIUM,
        description = null,
        imageUrl = null,
        familyCommonName = null,
        lightLevel = null,
        atmosphericHumidity = null,
        wateringFrequency = null
    )
}
