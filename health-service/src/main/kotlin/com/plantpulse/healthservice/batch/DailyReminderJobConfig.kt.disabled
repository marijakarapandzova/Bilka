package com.plantpulse.healthservice.batch

import com.plantpulse.healthservice.application.NotificationService
import com.plantpulse.healthservice.domain.notification.NotificationType
import com.plantpulse.healthservice.domain.plant.PlantHealthProfile
import com.plantpulse.healthservice.domain.plant.PlantHealthProfileRepository
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.support.ListItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * "Time to water Big Green!" — runs daily, finds every active plant whose
 * watering window has arrived (or passed), and drops one reminder
 * notification per plant. Chunk-oriented so large gardens are processed in
 * bounded batches rather than one giant transaction.
 */
@Configuration
class DailyReminderJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val profileRepository: PlantHealthProfileRepository,
    private val notificationService: NotificationService
) {
    @Bean
    fun dailyReminderJob(dailyReminderStep: Step): Job =
        JobBuilder("dailyReminderJob", jobRepository)
            .start(dailyReminderStep)
            .build()

    @Bean
    fun dailyReminderStep(dueForWateringReader: ListItemReader<PlantHealthProfile>): Step =
        StepBuilder("dailyReminderStep", jobRepository)
            .chunk<PlantHealthProfile, PlantHealthProfile>(20, transactionManager)
            .reader(dueForWateringReader)
            .writer(reminderWriter())
            .build()

    @Bean
    @StepScope
    fun dueForWateringReader(): ListItemReader<PlantHealthProfile> {
        val now = Instant.now()
        val due = profileRepository.findByActiveTrue().filter { it.nextWateringDate() <= now }
        return ListItemReader(due)
    }

    @Bean
    fun reminderWriter(): ItemWriter<PlantHealthProfile> = ItemWriter { chunk ->
        val today = LocalDate.now(ZoneOffset.UTC)
        chunk.items.forEach { profile ->
            val overdue = profile.nextWateringDate().isBefore(Instant.now())
            notificationService.createIfAbsent(
                userId = profile.userId,
                plantId = profile.id,
                type = NotificationType.WATERING_REMINDER,
                title = "Time to water ${profile.nickname}!",
                message = "Your ${profile.speciesName} is ${if (overdue) "overdue for watering" else "due today"}.",
                dedupeKey = "WATERING:${profile.id}:$today"
            )
        }
    }
}
