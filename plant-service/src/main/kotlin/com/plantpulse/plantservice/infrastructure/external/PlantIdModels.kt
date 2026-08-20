package com.plantpulse.plantservice.infrastructure.external

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class PlantIdRequest(
    val images: List<String>,
    val modifiers: List<String> = listOf("crops_fast"),
    val plant_details: List<String> = listOf("common_names")
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PlantIdResponse(
    val suggestions: List<PlantIdSuggestion> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PlantIdSuggestion(
    val plant_name: String,
    val probability: Double
)

data class PhotoIdentificationResult(
    val matchedName: String,
    val confidencePercent: Int
)
