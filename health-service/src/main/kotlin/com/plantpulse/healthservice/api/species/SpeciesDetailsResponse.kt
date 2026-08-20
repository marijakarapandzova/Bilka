package com.plantpulse.healthservice.api.species

data class SpeciesDetailsResponse(
    // Basic info
    val id: String? = null,
    val name: String? = null,
    val scientificName: String? = null,

    // CSV Fields - Physical Characteristics
    val height_cm: Double? = null,
    val leaf_count: Int? = null,
    val new_growth_count: Int? = null,

    // CSV Fields - Care Requirements
    val watering_frequency_days: Int? = null,
    val watering_amount_ml: Double? = null,
    val sunlight_exposure: String? = null,
    val fertilizer_type: String? = null,
    val fertilizer_amount_ml: Double? = null,

    // CSV Fields - Environmental Conditions
    val roomTemperatureC: Double? = null,
    val humidity_percent: Double? = null,

    // CSV Fields - Soil
    val soil_type: String? = null,
    val soil_moisture_percent: Double? = null,

    // CSV Fields - Pest & Disease
    val pest_presence: String? = null,
    val pest_severity: String? = null,

    // CSV Fields - Health
    val health_notes: String? = null,
    val health_score: Int? = null,

    // Data source
    val data_source: String = "CSV"
)