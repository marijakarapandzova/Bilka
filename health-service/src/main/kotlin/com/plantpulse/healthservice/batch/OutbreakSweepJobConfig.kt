package com.plantpulse.healthservice.batch

import com.plantpulse.healthservice.application.OutbreakDetectionService
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

/**
 * Periodic full sweep of the outbreak-detection geo query: refreshes every
 * still-active cluster's affected-plant count and window, and deactivates
 * ones that have fallen back under threshold. A single aggregate operation,
 * so a Tasklet step fits better here than chunk-oriented reader/writer.
 */
@Configuration
class OutbreakSweepJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val outbreakDetectionService: OutbreakDetectionService
) {
    @Bean
    fun outbreakSweepJob(outbreakSweepStep: Step): Job =
        JobBuilder("outbreakSweepJob", jobRepository)
            .start(outbreakSweepStep)
            .build()

    @Bean
    fun outbreakSweepStep(): Step =
        StepBuilder("outbreakSweepStep", jobRepository)
            .tasklet(
                Tasklet { _, _ ->
                    outbreakDetectionService.sweep()
                    RepeatStatus.FINISHED
                },
                transactionManager
            )
            .build()
}
