package com.plantpulse.healthservice.api.dashboard

import com.plantpulse.healthservice.application.DashboardService
import com.plantpulse.healthservice.security.CurrentUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/health")
@Tag(name = "Dashboard", description = "Aggregated garden health overview")
class DashboardController(private val dashboardService: DashboardService) {

    @GetMapping("/dashboard")
    @Operation(
        summary = "Get my garden's health dashboard",
        description = "Per-plant health score, watering countdown and active regional alerts for the current user"
    )
    fun dashboard(): DashboardResponse = dashboardService.dashboardFor(CurrentUser.id())
}
