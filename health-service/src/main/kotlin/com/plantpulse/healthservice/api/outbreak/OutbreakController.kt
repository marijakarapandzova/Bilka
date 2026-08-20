package com.plantpulse.healthservice.api.outbreak

import com.plantpulse.healthservice.application.OutbreakDetectionService
import com.plantpulse.healthservice.domain.outbreak.RegionalOutbreak
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/alerts/regional")
@Tag(name = "Regional Alerts", description = "Disease clusters detected across users in the same city")
class OutbreakController(private val outbreakDetectionService: OutbreakDetectionService) {

    @GetMapping
    @Operation(summary = "List every currently active regional outbreak")
    fun all(): List<RegionalOutbreakResponse> = outbreakDetectionService.activeOutbreaks().map { it.toResponse() }

    @GetMapping("/{city}")
    @Operation(summary = "List active outbreaks near a specific city")
    fun forCity(@PathVariable city: String): List<RegionalOutbreakResponse> =
        outbreakDetectionService.activeOutbreaksForCity(city).map { it.toResponse() }

    private fun RegionalOutbreak.toResponse() = RegionalOutbreakResponse(
        id = id,
        cityLocation = cityLocation,
        diseaseName = diseaseName,
        affectedPlantCount = affectedPlantCount,
        windowStart = windowStart,
        windowEnd = windowEnd,
        detectedAt = detectedAt
    )
}
