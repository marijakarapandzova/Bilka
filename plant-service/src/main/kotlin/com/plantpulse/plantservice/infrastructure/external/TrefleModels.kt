package com.plantpulse.plantservice.infrastructure.external

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TrefleSearchResponse(
    val data: List<TrefleSearchItem> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TrefleSearchItem(
    val id: Long,
    val common_name: String?,
    val scientific_name: String?,
    val family_common_name: String?,
    val image_url: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TrefleSpeciesDetailResponse(
    val data: TrefleSpeciesDetail?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TrefleSpeciesDetail(
    val id: Long,
    val common_name: String?,
    val scientific_name: String?,
    val family_common_name: String?,
    val image_url: String?,
    val growth: TrefleGrowth?,
    val specifications: TrefleSpecifications?,
    val taxonomy: TrefleFullTaxonomy?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TrefleGrowth(
    val light: Int?,               // scale 0-10
    val atmospheric_humidity: Int?, // scale 0-10
    val watering: String?          // "none" | "minimum" | "average" | "frequent"
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TrefleSpecifications(
    val height: TrefleRange?,
    val width: TrefleRange?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TrefleRange(
    val min: Double?,
    val max: Double?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TrefleFullTaxonomy(
    val family: String?,
    val genus: String?,
    val species: String?
)
