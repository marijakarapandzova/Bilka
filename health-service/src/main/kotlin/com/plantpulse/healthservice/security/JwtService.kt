package com.plantpulse.healthservice.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.crypto.SecretKey

/**
 * Health-service never issues tokens — it only verifies ones issued by
 * plant-service's auth flow, using the same HMAC secret. This keeps
 * authentication centralized in one place while letting every service
 * independently authorize requests without a network round-trip.
 */
@Component
class JwtService(
    @Value("\${plantpulse.jwt.secret}") private val secret: String
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun extractUserId(token: String): String =
        parseClaims(token).get("userId", String::class.java)

    fun isTokenValid(token: String): Boolean = try {
        parseClaims(token)
        true
    } catch (ex: Exception) {
        false
    }

    private fun parseClaims(token: String) =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
}
