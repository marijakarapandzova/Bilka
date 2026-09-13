package com.plantpulse.healthservice.api.admin

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Manual triggers for the scheduled batch jobs — useful for demos and
 * troubleshooting without waiting for the next cron firing. In a hardened
 * deployment these would be gated behind an admin role at the API Gateway.
 */
@RestController
@RequestMapping("/api/admin/batch")
@Tag(name = "Batch Admin", description = "Manually trigger the scheduled batch jobs")
class BatchTriggerController(
    private val jobLauncher: JobLauncher,
    private val dailyReminderJob: Job,
    private val healthDecayJob: Job,
    private val outbreakSweepJob: Job
) {

    @PostMapping("/reminders")
    @Operation(summary = "Run the daily watering-reminder job now")
    fun triggerReminders(): ResponseEntity<Map<String, Any>> = run(dailyReminderJob, "dailyReminderJob")

    @PostMapping("/health-decay")
    @Operation(summary = "Run the health-score decay job now")
    fun triggerHealthDecay(): ResponseEntity<Map<String, Any>> = run(healthDecayJob, "healthDecayJob")

    @PostMapping("/outbreak-sweep")
    @Operation(summary = "Run the regional outbreak sweep now")
    fun triggerOutbreakSweep(): ResponseEntity<Map<String, Any>> = run(outbreakSweepJob, "outbreakSweepJob")

    private fun run(job: Job, name: String): ResponseEntity<Map<String, Any>> {
        val params = JobParametersBuilder().addLong("runAt", System.currentTimeMillis()).toJobParameters()
        val execution = jobLauncher.run(job, params)
        return ResponseEntity.ok(
            mapOf("job" to name, "executionId" to execution.id, "status" to execution.status.toString())
        )
    }
}
