package com.plantpulse.plantservice.api.auth

import java.util.UUID

data class AuthResponse(
    val token: String,
    val userId: UUID,
    val email: String
)
