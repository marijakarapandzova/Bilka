package com.plantpulse.plantservice.api.plant

import com.plantpulse.plantservice.application.PlantService
import com.plantpulse.plantservice.domain.plant.PlantId
import com.plantpulse.plantservice.infrastructure.messaging.PlantAddedEvent
import com.plantpulse.plantservice.infrastructure.messaging.PlantEventPublisher
import com.plantpulse.plantservice.security.CurrentUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/plants")
@Tag(name = "Plant API", description = "Manage your plants and garden")
class PlantController(
    private val plantService: PlantService,
    private val eventPublisher: PlantEventPublisher
) {

    @PostMapping("/by-photo")
    @Operation(summary = "Add plant by photo", description = "Identify and add a plant to your garden by uploading a photo")
    fun addByPhoto(@Valid @RequestBody request: AddPlantByPhotoRequest): ResponseEntity<PlantResponse> =
        ResponseEntity.status(201).body(plantService.addByPhoto(CurrentUser.id(), request))

    @PostMapping("/manual")
    @Operation(summary = "Add plant manually", description = "Manually add a plant to your garden by providing species details")
    fun addManual(@Valid @RequestBody request: AddPlantManualRequest): ResponseEntity<PlantResponse> =
        ResponseEntity.status(201).body(plantService.addManual(CurrentUser.id(), request))

    @GetMapping
    @Operation(summary = "Get my garden", description = "Retrieve all plants in your garden")
    fun myGarden(): List<PlantResponse> = plantService.getGarden(CurrentUser.id())

    @GetMapping("/{id}")
    @Operation(summary = "Get plant details", description = "Retrieve detailed information about a specific plant")
    fun getOne(@PathVariable id: UUID): PlantResponse = plantService.getById(id, CurrentUser.id())

    @PatchMapping("/{id}")
    @Operation(summary = "Update plant", description = "Update plant information such as name or notes")
    fun update(@PathVariable id: UUID, @RequestBody request: UpdatePlantRequest): PlantResponse =
        plantService.update(id, CurrentUser.id(), request)

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete plant", description = "Remove a plant from your garden")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        plantService.delete(id, CurrentUser.id())
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/water")
    @Operation(summary = "Log watering", description = "Record that you watered a plant")
    fun logWatering(@PathVariable id: UUID): ResponseEntity<PlantResponse> {
        return ResponseEntity.ok(plantService.logWatering(id, CurrentUser.id()))
    }

    @PostMapping("/sync-health-profiles")
    @Operation(summary = "Sync health profiles for all plants", description = "Initialize health profiles in Health Service for all existing plants")
    fun syncHealthProfiles(): ResponseEntity<Map<String, Any>> {
        val plants = plantService.getGarden(CurrentUser.id())
        plants.forEach { plant ->
            try {
                eventPublisher.publishPlantAdded(
                    PlantAddedEvent(
                        plantId = PlantId(plant.id.toString()),
                        userId = CurrentUser.id(),
                        speciesId = plant.speciesId,
                        wateringFrequencyDays = plant.wateringFrequencyDays
                    )
                )
            } catch (e: Exception) {
                // Continue even if one fails
                System.err.println("Failed to sync plant ${plant.id}: ${e.message}")
            }
        }
        return ResponseEntity.ok(mapOf(
            "status" to "success",
            "message" to "Synced ${plants.size} plants to Health Service",
            "count" to plants.size
        ))
    }
}
