package com.plantpulse.plantservice.api.plant

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class AddPlantByPhotoRequest(
    @field:NotBlank
    val imageBase64: String,

    @field:NotBlank
    val nickname: String,

    val room: String? = null
)

data class AddPlantManualRequest(
    @field:NotNull
    val speciesId: UUID,

    @field:NotBlank
    val nickname: String,

    val room: String? = null,

    val currentPhotoUrl: String? = null
)

data class UpdatePlantRequest(
    val nickname: String? = null,
    val room: String? = null
)
