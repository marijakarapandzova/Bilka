package com.plantpulse.healthservice.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfigurationSource

/**
 * JWT identifies the caller (populating CurrentUser) but, matching
 * plant-service's current posture, endpoints stay open at this layer —
 * ownership/authorization is enforced in the service layer against the
 * resolved userId. Real deployments would tighten this alongside the API
 * Gateway's Keycloak-backed enforcement.
 */
@Configuration
@Profile("!test")
class SecurityConfig(
    private val corsConfigurationSource: CorsConfigurationSource
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource) }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .anonymous { }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("OPTIONS", "/**").permitAll()
                auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()  // API docs
                auth.requestMatchers("/actuator/**").permitAll()  // Health checks
                auth.anyRequest().permitAll()  // For now, permit all (enforcement at service layer)
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { }  // Use Keycloak JWT validation
            }

        return http.build()
    }
}
