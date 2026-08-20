package com.plantpulse.healthservice.application

import com.plantpulse.healthservice.config.HealthProperties
import com.plantpulse.healthservice.domain.health.HealthSnapshotRepository
import com.plantpulse.healthservice.domain.notification.NotificationType
import com.plantpulse.healthservice.domain.outbreak.RegionalOutbreak
import com.plantpulse.healthservice.domain.outbreak.RegionalOutbreakRepository
import com.plantpulse.healthservice.domain.plant.PlantHealthProfileRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

/**
 * Detects regional disease clusters with a simple geo query: N-or-more
 * distinct plants in the same city matched the same disease within a
 * rolling window (see HealthSnapshotRepository.findOutbreakCandidates /
 * countDistinctAffectedPlants). No user ever reports an outbreak — it falls
 * out of everyone's individual observations.
 */
@Service
class OutbreakDetectionService(
    private val snapshotRepository: HealthSnapshotRepository,
    private val outbreakRepository: RegionalOutbreakRepository,
    private val plantProfileRepository: PlantHealthProfileRepository,
    private val notificationService: NotificationService,
    private val properties: HealthProperties
) {
    private val log = LoggerFactory.getLogger(OutbreakDetectionService::class.java)

    fun activeOutbreaks(): List<RegionalOutbreak> = outbreakRepository.findByActiveTrue()

    fun activeOutbreaksForCity(city: String): List<RegionalOutbreak> =
        outbreakRepository.findByCityLocationAndActiveTrue(city)

    /** Near-real-time nudge, called right after a diseased observation snapshot is stored. */
    @Transactional
    fun checkCityForDisease(city: String, diseaseName: String, at: Instant = Instant.now()) {
        val since = at.minus(properties.outbreakWindowDays, ChronoUnit.DAYS)
        val count = snapshotRepository.countDistinctAffectedPlants(
            city, diseaseName, since, properties.outbreakMinMatchPercentage
        )
        if (count >= properties.outbreakMinAffectedPlants) {
            upsertOutbreak(city, diseaseName, count.toInt(), since, at)
        }
    }

    /** Full sweep across all cities/diseases, run by the scheduled batch job; also expires stale outbreaks. */
    @Transactional
    fun sweep(at: Instant = Instant.now()) {
        val since = at.minus(properties.outbreakWindowDays, ChronoUnit.DAYS)
        val candidates = snapshotRepository.findOutbreakCandidates(
            since, properties.outbreakMinMatchPercentage, properties.outbreakMinAffectedPlants
        )

        val stillActive = candidates.map { it.cityLocation to it.diseaseName }.toSet()
        candidates.forEach { c -> upsertOutbreak(c.cityLocation, c.diseaseName, c.affectedPlantCount.toInt(), since, at) }

        outbreakRepository.findByActiveTrue()
            .filterNot { (it.cityLocation to it.diseaseName) in stillActive }
            .forEach {
                it.deactivate()
                outbreakRepository.save(it)
                log.info("Outbreak resolved: {} in {}", it.diseaseName, it.cityLocation)
            }
    }

    private fun upsertOutbreak(city: String, disease: String, count: Int, windowStart: Instant, windowEnd: Instant) {
        val existing = outbreakRepository.findByCityLocationAndDiseaseNameAndActiveTrue(city, disease)
        val isNew = existing == null
        val outbreak = existing?.also { it.refresh(count, windowStart, windowEnd) }
            ?: RegionalOutbreak(
                cityLocation = city,
                diseaseName = disease,
                affectedPlantCount = count,
                windowStart = windowStart,
                windowEnd = windowEnd
            )
        outbreakRepository.save(outbreak)

        if (isNew) {
            log.info("New regional outbreak detected: {} affecting {} plants near {}", disease, count, city)
            notifyAtRiskUsers(outbreak)
        }
    }

    private fun notifyAtRiskUsers(outbreak: RegionalOutbreak) {
        val today = LocalDate.now(ZoneOffset.UTC)
        val affectedProfiles = plantProfileRepository.findByCityLocationAndActiveTrue(outbreak.cityLocation)

        affectedProfiles.groupBy { it.userId }.forEach { (userId, plants) ->
            val riskPlant = plants.firstOrNull()
            val riskNote = riskPlant?.let { " Your ${it.speciesName} (\"${it.nickname}\") may be at risk." } ?: ""
            notificationService.createIfAbsent(
                userId = userId,
                plantId = null,
                type = NotificationType.OUTBREAK_ALERT,
                title = "Regional outbreak: ${outbreak.diseaseName}",
                message = "${outbreak.diseaseName} detected in ${outbreak.affectedPlantCount} plants near " +
                    "${outbreak.cityLocation} this week.$riskNote Check soil drainage and symptoms closely.",
                dedupeKey = "OUTBREAK:${outbreak.cityLocation}:${outbreak.diseaseName}:$today"
            )
        }
    }
}
