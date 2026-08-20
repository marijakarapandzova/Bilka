package com.plantpulse.healthservice.api.plant

import com.plantpulse.healthservice.application.DashboardService
import com.plantpulse.healthservice.security.CurrentUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/health/plants")
@Tag(name = "Plant Health", description = "Health score, timeline and care schedule for a single plant")
class PlantHealthController(private val dashboardService: DashboardService) {

    @GetMapping("/{plantId}")
    @Operation(summary = "Get current health snapshot for a plant")
    fun health(@PathVariable plantId: UUID): PlantHealthResponse =
        dashboardService.plantHealth(plantId, CurrentUser.id())

    @GetMapping("/{plantId}/history")
    @Operation(summary = "Get the health-score timeline for a plant", description = "Powers the Jan 95 -> Apr 41 -> May 58 recovery graph")
    fun history(@PathVariable plantId: UUID): List<HealthSnapshotResponse> =
        dashboardService.history(plantId, CurrentUser.id())

    @GetMapping("/{plantId}/care-schedule")
    @Operation(summary = "Get the next watering date and urgency for a plant")
    fun careSchedule(@PathVariable plantId: UUID): CareScheduleResponse =
        dashboardService.careSchedule(plantId, CurrentUser.id())
}
