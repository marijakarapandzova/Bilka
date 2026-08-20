package com.plantpulse.plantservice.infrastructure.external

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PerenualSearchResponse(
    val data: List<PerenualPlant> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PerenualPlant(
    val id: Int,
    val common_name: String?,
    val scientific_name: List<String>?,
    val other_name: List<String>?,
    val cycle: String?,
    val watering: String?,
    val watering_general_benchmark: PerenualWateringBenchmark?,
    val watering_period: String?,
    val watering_frequency_days: Int?,
    val watering_frequency_watering_interval: String?,
    val soil: List<String>?,
    val sunlight: List<String>?,
    val sunlight_general_category: String?,
    val sunlight_hours: Int?,
    val temperature: PerenualTemperature?,
    val temperature_min_c: Int?,
    val temperature_max_c: Int?,
    val humidity: PerenualHumidity?,
    val humidity_general_benchmark: String?,
    val hardiness: PerenualHardiness?,
    val hardiness_min_c: Int?,
    val hardiness_max_c: Int?,
    val ph: PerenualPH?,
    val ph_min: Float?,
    val ph_max: Float?,
    val growth_rate: String?,
    val difficulty_level: String?,
    val maintenance: String?,
    val pest_susceptibility: List<String>?,
    val disease_susceptibility: List<String>?,
    val propagation: List<String>?,
    val care_guides: String?,
    val description: String?,
    val image_url: String?,
    val default_image: PerenualImage?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PerenualWateringBenchmark(
    val value: String?,
    val description: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PerenualTemperature(
    val min_c: Int?,
    val max_c: Int?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PerenualHumidity(
    val min: Int?,
    val max: Int?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PerenualHardiness(
    val min_c: Int?,
    val max_c: Int?,
    val min_f: Int?,
    val max_f: Int?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PerenualPH(
    val min: Float?,
    val max: Float?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PerenualImage(
    val original_url: String?,
    val medium_url: String?,
    val small_url: String?,
    val thumbnail: String?
)
