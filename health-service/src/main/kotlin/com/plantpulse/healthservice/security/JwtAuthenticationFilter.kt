package com.plantpulse.healthservice.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

/**
 * Health-service has no User table of its own (it doesn't own identity data),
 * so unlike plant-service's filter it resolves the principal straight from
 * the JWT's `userId` claim instead of looking a user up by email.
 */
@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    private val publicPaths = listOf(
        "/swagger-ui",
        "/v3/api-docs",
        "/actuator"
    )

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val path = request.requestURI

        if (publicPaths.any { path.startsWith(it) }) {
            filterChain.doFilter(request, response)
            return
        }

        val header = request.getHeader("Authorization")

        if (header != null && header.startsWith("Bearer ")) {
            try {
                val token = header.substringAfter("Bearer ").trim()
                if (jwtService.isTokenValid(token) && SecurityContextHolder.getContext().authentication == null) {
                    val userId = UUID.fromString(jwtService.extractUserId(token))
                    val authToken = UsernamePasswordAuthenticationToken(userId, null, emptyList())
                    authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authToken
                }
            } catch (ex: Exception) {
                // Invalid/unparseable token: fall through, CurrentUser will use the dev fallback
            }
        }

        filterChain.doFilter(request, response)
    }
}
