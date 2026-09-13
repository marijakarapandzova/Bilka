package com.plantpulse.plantservice.application

import com.plantpulse.plantservice.api.auth.AuthResponse
import com.plantpulse.plantservice.api.auth.KeycloakTokenResponse
import com.plantpulse.plantservice.api.auth.LoginRequest
import com.plantpulse.plantservice.api.auth.RegisterRequest
import com.plantpulse.plantservice.domain.user.Location
import com.plantpulse.plantservice.domain.user.User
import com.plantpulse.plantservice.domain.user.UserRepository
import com.plantpulse.plantservice.security.JwtService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import org.springframework.util.LinkedMultiValueMap

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val restTemplate: RestTemplate,
    @Value("\${keycloak.url:http://localhost:8090}")
    private val keycloakUrl: String,
    @Value("\${keycloak.realm:finki-services}")
    private val keycloakRealm: String,
    @Value("\${keycloak.client-id:gateway-tester}")
    private val keycloakClientId: String,
    @Value("\${keycloak.client-secret:fxp6CM5F28RKPdXLvqKONTU7kNmpRaA9}")
    private val keycloakClientSecret: String
) {

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw EmailAlreadyRegisteredException(request.email)
        }

        val user = User(
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            location = request.city?.let { Location(city = it) }
        )

        val saved = userRepository.save(user)
        val token = jwtService.generateToken(saved.email, saved.id.toString())

        return AuthResponse(token = token, userId = saved.id, email = saved.email)
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw InvalidCredentialsException()

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw InvalidCredentialsException()
        }

        val token = jwtService.generateToken(user.email, user.id.toString())
        return AuthResponse(token = token, userId = user.id, email = user.email)
    }

    fun keycloakLogin(username: String, password: String): KeycloakTokenResponse {
        val tokenUrl = "$keycloakUrl/realms/$keycloakRealm/protocol/openid-connect/token"

        val body = LinkedMultiValueMap<String, String>().apply {
            add("client_id", keycloakClientId)
            add("client_secret", keycloakClientSecret)
            add("username", username)
            add("password", password)
            add("grant_type", "password")
        }

        val response = restTemplate.postForObject(
            tokenUrl,
            org.springframework.http.HttpEntity(body, org.springframework.http.HttpHeaders().apply {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
            }),
            KeycloakTokenResponse::class.java
        ) ?: throw IllegalStateException("Failed to get token from Keycloak")

        return response
    }
}
