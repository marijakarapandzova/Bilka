package com.plantpulse.plantservice.api.species

import com.plantpulse.plantservice.domain.species.CareDifficulty
import com.plantpulse.plantservice.domain.species.LightNeeds

data class CreateSpeciesRequest(
    val name: String,
    val scientificName: String,
    val careDifficulty: CareDifficulty,
    val wateringFrequencyDays: Int,
    val lightNeeds: LightNeeds,
    val description: String? = null
)
