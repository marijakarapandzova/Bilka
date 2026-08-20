package com.plantpulse.plantservice.api.observation

import com.plantpulse.plantservice.application.ObservationService
import com.plantpulse.plantservice.security.CurrentUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/plants/{plantId}/observations")
@Tag(name = "Observation API", description = "Track plant observations and health metrics")
class ObservationController(private val observationService: ObservationService) {

    @PostMapping
    @Operation(summary = "Log observation", description = "Record an observation about a plant (health status, symptoms, etc)")
    fun logObservation(
        @PathVariable plantId: UUID,
        @Valid @RequestBody request: LogObservationRequest
    ): ResponseEntity<ObservationResponse> {
        val response = observationService.logObservation(plantId, CurrentUser.id(), request)
        return ResponseEntity.status(201).body(response)
    }

    @GetMapping
    @Operation(summary = "Get observation history", description = "Retrieve all observations recorded for a specific plant")
    fun history(@PathVariable plantId: UUID): List<ObservationResponse> =
        observationService.getHistory(plantId, CurrentUser.id())
}
