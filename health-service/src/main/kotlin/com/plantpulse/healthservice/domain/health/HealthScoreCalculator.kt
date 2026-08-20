package com.plantpulse.healthservice.domain.health

import org.springframework.stereotype.Component
import kotlin.math.roundToInt

/**
 * Deterministic, explainable scoring — no ML model, just a rule that maps a
 * disease match straight onto the 0-100 health score:
 *
 *  - "Healthy" match of P%      -> 70 + 0.30*P   (a perfect 100% match scores 100)
 *  - Any other disease, P%      -> 100 - 0.65*P  (e.g. Root Rot at 91% -> ~41,
 *                                                  matching the product spec's example)
 *
 * Status is a trend label layered on top by comparing against the previous
 * snapshot, not just the raw score.
 */
@Component
class HealthScoreCalculator {

    fun scoreForDiseaseMatch(diseaseName: String, matchPercentage: Int): Int {
        val match = matchPercentage.coerceIn(0, 100)
        val raw = if (diseaseName.equals("Healthy", ignoreCase = true)) {
            70.0 + (match * 0.30)
        } else {
            100.0 - (match * 0.65)
        }
        return raw.roundToInt().coerceIn(5, 100)
    }

    fun deriveStatus(newScore: Int, previous: HealthSnapshot?): HealthStatus {
        val previousScore = previous?.healthScore
        return when {
            newScore < 55 -> HealthStatus.CRITICAL
            previousScore == null -> if (newScore >= 85) HealthStatus.HEALTHY else HealthStatus.STABLE
            newScore > previousScore + 2 &&
                (previous.status == HealthStatus.DECLINING || previous.status == HealthStatus.CRITICAL) -> HealthStatus.RECOVERING
            newScore < previousScore - 3 -> HealthStatus.DECLINING
            newScore >= 85 -> HealthStatus.HEALTHY
            else -> HealthStatus.STABLE
        }
    }

    /** Gentle downward drift for plants nobody has checked on in a while. */
    fun decay(currentScore: Int, daysStale: Int, pointsPerDay: Int): Int =
        (currentScore - daysStale * pointsPerDay).coerceIn(15, 100)
}
