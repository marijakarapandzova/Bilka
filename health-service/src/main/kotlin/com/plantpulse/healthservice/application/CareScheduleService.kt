package com.plantpulse.healthservice.application

import com.plantpulse.healthservice.api.plant.CareScheduleResponse
import com.plantpulse.healthservice.domain.plant.PlantHealthProfile
import com.plantpulse.healthservice.domain.plant.WateringUrgency
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
class CareScheduleService {

    fun scheduleFor(profile: PlantHealthProfile, now: Instant = Instant.now()): CareScheduleResponse {
        val nextWateringDate = profile.nextWateringDate()
        val hoursUntil = Duration.between(now, nextWateringDate).toHours()
        val daysUntil = Math.floorDiv(hoursUntil, 24)

        val urgency = when {
            hoursUntil < 0 -> WateringUrgency.OVERDUE
            hoursUntil < 24 -> WateringUrgency.DUE_TODAY
            else -> WateringUrgency.UPCOMING
        }

        return CareScheduleResponse(
            plantId = profile.id,
            nextWateringDate = nextWateringDate,
            daysUntilWatering = daysUntil,
            wateringUrgency = urgency,
            skipWatering = profile.skipWatering,
            skipWateringReason = profile.skipWateringReason
        )
    }
}
