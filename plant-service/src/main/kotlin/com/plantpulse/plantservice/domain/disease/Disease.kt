package com.plantpulse.plantservice.domain.disease

import com.plantpulse.plantservice.domain.observation.Growth
import com.plantpulse.plantservice.domain.observation.LeafColor
import com.plantpulse.plantservice.domain.observation.LeafTexture
import com.plantpulse.plantservice.domain.observation.Observation
import com.plantpulse.plantservice.domain.observation.SoilMoisture

data class SymptomProfile(
    val leafColor: LeafColor? = null,
    val leafTexture: LeafTexture? = null,
    val soilMoisture: SoilMoisture? = null,
    val visiblePests: Boolean? = null,
    val growth: Growth? = null
)

data class Disease(
    val name: String,
    val symptomProfile: SymptomProfile,
    val treatmentSteps: List<String>
)

data class DiseaseMatch(
    val diseaseName: String,
    val matchPercentage: Int,
    val treatmentSteps: List<String>
)

object DiseaseKnowledgeBase {

    val diseases: List<Disease> = listOf(
        Disease(
            name = "Root Rot",
            symptomProfile = SymptomProfile(
                leafColor = LeafColor.YELLOW,
                leafTexture = LeafTexture.MUSHY,
                soilMoisture = SoilMoisture.WATERLOGGED,
                visiblePests = false,
                growth = Growth.STUNTED
            ),
            treatmentSteps = listOf(
                "Stop watering immediately",
                "Remove plant from pot",
                "Trim any black/mushy roots",
                "Repot in fresh dry soil",
                "Ensure pot has drainage holes"
            )
        ),
        Disease(
            name = "Underwatering / Drought Stress",
            symptomProfile = SymptomProfile(
                leafColor = LeafColor.BROWN,
                leafTexture = LeafTexture.CRISPY,
                soilMoisture = SoilMoisture.DRY,
                visiblePests = false,
                growth = Growth.SLOW
            ),
            treatmentSteps = listOf(
                "Water thoroughly until it drains from the bottom",
                "Increase watering frequency slightly",
                "Move away from direct heat sources",
                "Mist leaves to raise humidity"
            )
        ),
        Disease(
            name = "Pest Infestation",
            symptomProfile = SymptomProfile(
                leafColor = LeafColor.SPOTTED,
                leafTexture = LeafTexture.WILTING,
                soilMoisture = SoilMoisture.MOIST,
                visiblePests = true,
                growth = Growth.SLOW
            ),
            treatmentSteps = listOf(
                "Isolate the plant from others",
                "Wipe leaves with a damp cloth to remove pests",
                "Apply insecticidal soap or neem oil",
                "Repeat treatment every 7 days for 3 weeks"
            )
        ),
        Disease(
            name = "Fungal Leaf Spot",
            symptomProfile = SymptomProfile(
                leafColor = LeafColor.SPOTTED,
                leafTexture = LeafTexture.HEALTHY,
                soilMoisture = SoilMoisture.MOIST,
                visiblePests = false,
                growth = Growth.NORMAL
            ),
            treatmentSteps = listOf(
                "Remove and discard affected leaves",
                "Avoid getting water on the leaves when watering",
                "Improve air circulation around the plant",
                "Apply a fungicide if the spread continues"
            )
        ),
        Disease(
            name = "Nutrient Deficiency",
            symptomProfile = SymptomProfile(
                leafColor = LeafColor.YELLOW,
                leafTexture = LeafTexture.HEALTHY,
                soilMoisture = SoilMoisture.MOIST,
                visiblePests = false,
                growth = Growth.SLOW
            ),
            treatmentSteps = listOf(
                "Apply a balanced liquid fertilizer",
                "Check that the pot size still fits the root system",
                "Ensure the plant gets adequate light",
                "Re-evaluate watering schedule"
            )
        ),
        Disease(
            name = "Healthy",
            symptomProfile = SymptomProfile(
                leafColor = LeafColor.GREEN,
                leafTexture = LeafTexture.HEALTHY,
                soilMoisture = SoilMoisture.MOIST,
                visiblePests = false,
                growth = Growth.NORMAL
            ),
            treatmentSteps = listOf(
                "No issues detected — keep up the current care routine"
            )
        )
    )
}

/**
 * Matches an Observation against the disease knowledge base.
 * Score = number of matching symptom fields / total fields compared, as a percentage.
 */
class SymptomMatcher {

    fun match(observation: Observation): DiseaseMatch {
        val bestMatch = DiseaseKnowledgeBase.diseases
            .map { disease -> disease to scoreMatch(disease.symptomProfile, observation) }
            .maxByOrNull { it.second }
            ?: error("Disease knowledge base is empty")

        val (disease, score) = bestMatch

        return DiseaseMatch(
            diseaseName = disease.name,
            matchPercentage = score,
            treatmentSteps = disease.treatmentSteps
        )
    }

    private fun scoreMatch(profile: SymptomProfile, observation: Observation): Int {
        var totalFields = 0
        var matchedFields = 0

        profile.leafColor?.let {
            totalFields++
            if (it == observation.leafColor) matchedFields++
        }
        profile.leafTexture?.let {
            totalFields++
            if (it == observation.leafTexture) matchedFields++
        }
        profile.soilMoisture?.let {
            totalFields++
            if (it == observation.soilMoisture) matchedFields++
        }
        profile.visiblePests?.let {
            totalFields++
            if (it == observation.visiblePests) matchedFields++
        }
        profile.growth?.let {
            totalFields++
            if (it == observation.growth) matchedFields++
        }

        if (totalFields == 0) return 0
        return (matchedFields * 100) / totalFields
    }
}
