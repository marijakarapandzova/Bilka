package com.plantpulse.healthservice.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

/**
 * Test Security Configuration
 *
 * Activated only when @ActiveProfiles("test") is used (Pact provider tests).
 * Permits all HTTP requests without authentication.
 *
 * This allows Pact provider tests to send HTTP requests to the running app
 * without needing to provide JWT tokens or deal with authentication.
 *
 * Production SecurityConfig (with @Profile("!test")) is disabled during tests.
 */
@Configuration
@Profile("test")
class TestSecurityConfig {

    @Bean
    fun testSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth.anyRequest().permitAll()
            }
            .build()
    }
}
