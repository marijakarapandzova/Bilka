package com.plantpulse.healthservice.batch

import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Cron-driven entry point for the three batch jobs. Each run gets a unique
 * `runAt` parameter (Spring Batch treats identical JobParameters as "already
 * executed" and refuses to rerun a completed job), so a fresh, independent
 * JobInstance is created on every scheduled firing.
 */
@Component
class BatchScheduler(
    private val jobLauncher: JobLauncher,
    private val dailyReminderJob: Job,
    private val healthDecayJob: Job,
    private val outbreakSweepJob: Job
) {
    private val log = LoggerFactory.getLogger(BatchScheduler::class.java)

    @Scheduled(cron = "\${plantpulse.health.reminder-cron}")
    fun runDailyReminders() = launch(dailyReminderJob, "dailyReminderJob")

    @Scheduled(cron = "\${plantpulse.health.decay-cron}")
    fun runHealthDecay() = launch(healthDecayJob, "healthDecayJob")

    @Scheduled(cron = "\${plantpulse.health.outbreak-sweep-cron}")
    fun runOutbreakSweep() = launch(outbreakSweepJob, "outbreakSweepJob")

    private fun launch(job: Job, name: String) {
        try {
            val params = JobParametersBuilder()
                .addLong("runAt", System.currentTimeMillis())
                .toJobParameters()
            val execution = jobLauncher.run(job, params)
            log.info("Launched {} -> {} (status={})", name, execution.id, execution.status)
        } catch (ex: Exception) {
            log.error("Failed to launch {}: {}", name, ex.message, ex)
        }
    }
}
