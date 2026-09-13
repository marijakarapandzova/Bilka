package com.plantpulse.healthservice.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import java.util.UUID

object CurrentUser {
    // Matches plant-service's dev fallback so the two services behave the
    // same way when no bearer token is presented (e.g. local demo/testing).
    private val DEV_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
    private const val DEV_USERNAME = "dev-user"

    fun id(): UUID {
        val authentication = SecurityContextHolder.getContext().authentication

        // Try to get JWT from Keycloak
        if (authentication?.principal is Jwt) {
            val jwt = authentication.principal as Jwt
            val subClaim = jwt.getClaimAsString("sub")
            if (subClaim != null) {
                return try {
                    UUID.fromString(subClaim)
                } catch (e: IllegalArgumentException) {
                    DEV_USER_ID
                }
            }
        }

        // Fallback for direct principal (shouldn't happen with Keycloak)
        val principal = authentication?.principal
        return if (principal is UUID) principal else DEV_USER_ID
    }

    fun username(): String {
        val authentication = SecurityContextHolder.getContext().authentication

        // Try to get username from Keycloak JWT
        if (authentication?.principal is Jwt) {
            val jwt = authentication.principal as Jwt
            val usernameClaim = jwt.getClaimAsString("preferred_username")
            if (usernameClaim != null) {
                return usernameClaim
            }
        }

        // Fallback for dev mode
        return DEV_USERNAME
    }
}
