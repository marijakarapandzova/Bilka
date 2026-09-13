package com.plantpulse.plantservice.api.shared

import com.plantpulse.plantservice.application.SharedPlantService
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/shared-plants")
@Tag(name = "Shared Plants API", description = "Share and discover plants in your community")
class SharedPlantController(private val sharedPlantService: SharedPlantService) {

    @PostMapping("/share")
    @Operation(summary = "Share a plant", description = "Share one of your plants with the community")
    fun sharePlant(@Valid @RequestBody request: SharePlantRequest): ResponseEntity<SharedPlantResponse> =
        ResponseEntity.status(201).body(
            sharedPlantService.sharePlant(
                CurrentUser.id(),
                CurrentUser.username(),
                request
            )
        )

    @GetMapping
    @Operation(summary = "Get all shared plants", description = "Browse all plants shared by users in the community")
    fun getAllShared(): List<SharedPlantResponse> =
        sharedPlantService.getAllSharedPlants()

    @GetMapping("/city/{city}")
    @Operation(summary = "Get plants by city", description = "Find all shared plants in a specific city")
    fun getByCity(@PathVariable city: String): CityPlantsResponse =
        sharedPlantService.getCityPlants(city)

    @GetMapping("/cities")
    @Operation(summary = "Get cities with plants", description = "List all cities where plants have been shared")
    fun getCities(): List<String> =
        sharedPlantService.getCitiesWithPlants()

    @GetMapping("/my-shared")
    @Operation(summary = "Get my shared plants", description = "Retrieve all plants you have shared with the community")
    fun getMyShared(): List<SharedPlantResponse> =
        sharedPlantService.getSharedPlantsByUser(CurrentUser.id())
}
