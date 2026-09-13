package com.plantpulse.plantservice.security

import com.plantpulse.plantservice.domain.user.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    private val publicPaths = listOf(
        "/api/auth/",
        "/api/species/",  // Allow browsing species without auth
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

        // Skip JWT validation for public paths (development mode)
        if (publicPaths.any { path.startsWith(it) }) {
            filterChain.doFilter(request, response)
            return
        }

        val header = request.getHeader("Authorization")

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val token = header.substringAfter("Bearer ").trim()

            if (jwtService.isTokenValid(token) && SecurityContextHolder.getContext().authentication == null) {
                val email = jwtService.extractEmail(token)
                val user = userRepository.findByEmail(email)

                if (user != null) {
                    val authToken = UsernamePasswordAuthenticationToken(user.id, null, emptyList())
                    authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authToken
                }
            }
        } catch (ex: Exception) {
            // Token validation failed silently
        }

        filterChain.doFilter(request, response)
    }
}
