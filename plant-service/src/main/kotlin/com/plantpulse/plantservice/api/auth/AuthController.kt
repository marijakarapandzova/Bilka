package com.plantpulse.plantservice.api.auth

import com.plantpulse.plantservice.application.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

data class KeycloakLoginRequest(
    @field:NotBlank
    val username: String,

    @field:NotBlank
    val password: String
)

data class KeycloakTokenResponse(
    val access_token: String,
    val token_type: String? = null,
    val expires_in: Long? = null,
    val refresh_token: String? = null
)

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication API", description = "User registration and login")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Create a new user account")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.status(201).body(authService.register(request))
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate a user and receive JWT token")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.login(request))
    }

    @PostMapping("/keycloak-login")
    @Operation(summary = "Login via Keycloak OAuth2", description = "Authenticate with Keycloak and receive access token")
    fun keycloakLogin(@Valid @RequestBody request: KeycloakLoginRequest): ResponseEntity<KeycloakTokenResponse> {
        return ResponseEntity.ok(authService.keycloakLogin(request.username, request.password))
    }
}
