package com.plantpulse.plantservice.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfigurationSource

@Configuration
@Profile("!test")
class SecurityConfig(
    private val corsConfigurationSource: CorsConfigurationSource
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource) }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .anonymous { }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("OPTIONS", "/**").permitAll()
                auth.requestMatchers("/api/auth/**").permitAll()  // Old auth endpoints (for transition)
                auth.requestMatchers("/api/species/**").permitAll()  // Public species browsing
                auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()  // API docs
                auth.requestMatchers("/actuator/**").permitAll()  // Health checks
                auth.anyRequest().authenticated()  // Everything else requires auth
            }
            // Custom JWT validation is handled by JwtAuthenticationFilter
            // Disable default OAuth2 JWT validation to allow custom JWT tokens

        return http.build()
    }
}
