package com.plantpulse.healthservice.domain.health

/**
 * Compact, static "what to do about it" copy used in disease-alert
 * notifications. This deliberately mirrors plant-service's
 * DiseaseKnowledgeBase treatment steps rather than calling back into
 * plant-service for them: it's small, stable reference data, and each
 * service keeping its own copy avoids a synchronous runtime dependency
 * between the two services for something this cheap to duplicate.
 */
object DiseaseCareTips {

    private val tips: Map<String, List<String>> = mapOf(
        "Root Rot" to listOf(
            "Stop watering immediately",
            "Remove plant from pot",
            "Trim any black/mushy roots",
            "Repot in fresh dry soil",
            "Ensure pot has drainage holes"
        ),
        "Underwatering / Drought Stress" to listOf(
            "Water thoroughly until it drains from the bottom",
            "Increase watering frequency slightly",
            "Move away from direct heat sources",
            "Mist leaves to raise humidity"
        ),
        "Pest Infestation" to listOf(
            "Isolate the plant from others",
            "Wipe leaves with a damp cloth to remove pests",
            "Apply insecticidal soap or neem oil",
            "Repeat treatment every 7 days for 3 weeks"
        ),
        "Fungal Leaf Spot" to listOf(
            "Remove and discard affected leaves",
            "Avoid getting water on the leaves when watering",
            "Improve air circulation around the plant",
            "Apply a fungicide if the spread continues"
        ),
        "Nutrient Deficiency" to listOf(
            "Apply a balanced liquid fertilizer",
            "Check that the pot size still fits the root system",
            "Ensure the plant gets adequate light",
            "Re-evaluate watering schedule"
        ),
        "Healthy" to listOf("No issues detected — keep up the current care routine")
    )

    fun forDisease(diseaseName: String): List<String> =
        tips[diseaseName] ?: listOf("Monitor the plant closely and log another observation in a few days")
}
