package com.plantpulse.plantservice.application

import com.plantpulse.plantservice.api.auth.AuthResponse
import com.plantpulse.plantservice.api.auth.LoginRequest
import com.plantpulse.plantservice.api.auth.RegisterRequest
import com.plantpulse.plantservice.domain.user.Location
import com.plantpulse.plantservice.domain.user.User
import com.plantpulse.plantservice.domain.user.UserRepository
import com.plantpulse.plantservice.security.JwtService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
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
}
