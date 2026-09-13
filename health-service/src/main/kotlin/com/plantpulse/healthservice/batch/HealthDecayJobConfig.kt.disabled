package com.plantpulse.healthservice.batch

import com.plantpulse.healthservice.config.HealthProperties
import com.plantpulse.healthservice.domain.health.HealthScoreCalculator
import com.plantpulse.healthservice.domain.health.HealthSnapshot
import com.plantpulse.healthservice.domain.health.HealthSnapshotRepository
import com.plantpulse.healthservice.domain.health.SnapshotSource
import com.plantpulse.healthservice.domain.plant.PlantHealthProfile
import com.plantpulse.healthservice.domain.plant.PlantHealthProfileRepository
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.support.ListItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

/** Output of the decay processor: the profile plus the score/status it should move to. */
data class DecayUpdate(val profile: PlantHealthProfile, val newScore: Int, val newStatus: com.plantpulse.healthservice.domain.health.HealthStatus)

/**
 * Keeps the health timeline continuous for plants nobody has checked on
 * lately: a plant that's gone quiet slowly drifts down instead of freezing
 * at its last score forever, so "no news" still shows up as a trend on the
 * Jan->May graph. Runs once a day; every stale-eligible plant gets exactly
 * one grace-period-scaled nudge per run.
 */
@Configuration
class HealthDecayJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val profileRepository: PlantHealthProfileRepository,
    private val snapshotRepository: HealthSnapshotRepository,
    private val scoreCalculator: HealthScoreCalculator,
    private val properties: HealthProperties
) {
    @Bean
    fun healthDecayJob(healthDecayStep: Step): Job =
        JobBuilder("healthDecayJob", jobRepository)
            .start(healthDecayStep)
            .build()

    @Bean
    fun healthDecayStep(staleProfileReader: ListItemReader<PlantHealthProfile>): Step =
        StepBuilder("healthDecayStep", jobRepository)
            .chunk<PlantHealthProfile, DecayUpdate>(20, transactionManager)
            .reader(staleProfileReader)
            .processor(decayProcessor())
            .writer(decaySnapshotWriter())
            .build()

    @Bean
    @StepScope
    fun staleProfileReader(): ListItemReader<PlantHealthProfile> {
        val now = Instant.now()
        val startOfToday = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant()
        val graceCutoff = now.minus(properties.decayGracePeriodDays, ChronoUnit.DAYS)

        val stale = profileRepository.findByActiveTrue().filter { profile ->
            val lastActivity = profile.lastObservationAt ?: profile.addedAt
            lastActivity.isBefore(graceCutoff) &&
                !snapshotRepository.existsByPlantIdAndSourceAndRecordedAtAfter(profile.id, SnapshotSource.DECAY, startOfToday)
        }
        return ListItemReader(stale)
    }

    @Bean
    fun decayProcessor(): ItemProcessor<PlantHealthProfile, DecayUpdate> = ItemProcessor { profile ->
        val previous = snapshotRepository.findTopByPlantIdOrderByRecordedAtDesc(profile.id)
        val newScore = scoreCalculator.decay(profile.currentHealthScore, daysStale = 1, pointsPerDay = properties.decayPointsPerDay)
        if (newScore == profile.currentHealthScore) {
            null // already at the decay floor - nothing meaningful to record
        } else {
            DecayUpdate(profile, newScore, scoreCalculator.deriveStatus(newScore, previous))
        }
    }

    @Bean
    fun decaySnapshotWriter(): ItemWriter<DecayUpdate> = ItemWriter { chunk ->
        chunk.items.forEach { update ->
            update.profile.applyDecay(update.newScore, update.newStatus)
            profileRepository.save(update.profile)
            snapshotRepository.save(
                HealthSnapshot(
                    plantId = update.profile.id,
                    userId = update.profile.userId,
                    healthScore = update.newScore,
                    status = update.newStatus,
                    diseaseName = update.profile.currentDiseaseName,
                    diseaseMatchPercentage = update.profile.currentDiseaseMatchPercentage,
                    cityLocation = update.profile.cityLocation,
                    source = SnapshotSource.DECAY
                )
            )
        }
    }
}
