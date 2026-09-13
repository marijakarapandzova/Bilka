package com.plantpulse.healthservice.security

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

/**
 * Test-only security configuration that permits all requests.
 * Activated only when spring.profiles.active includes "test".
 * 
 * This allows Pact tests to hit endpoints without JWT validation,
 * while keeping the production SecurityConfig unchanged.
 */
@TestConfiguration
@Profile("test")
class TestSecurityConfig {

    @Bean
    fun testSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth.anyRequest().permitAll()
            }
        return http.build()
    }
}
