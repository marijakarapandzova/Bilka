package com.plantpulse.plantservice.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtService(
    @Value("\${plantpulse.jwt.secret}") private val secret: String,
    @Value("\${plantpulse.jwt.expiration-ms}") private val expirationMs: Long
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateToken(email: String, userId: String): String {
        val now = Date()
        val expiry = Date(now.time + expirationMs)

        return Jwts.builder()
            .subject(userId)  // Use userId as "sub" claim for backend compatibility
            .claim("email", email)  // Store email as a separate claim
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact()
    }

    fun extractEmail(token: String): String {
        val claims = parseClaims(token)
        // Try to get from "email" claim first (new format)
        val email = claims.get("email", String::class.java)
        return email ?: claims.subject  // Fallback to subject if email claim not found
    }

    fun extractUserId(token: String): String =
        parseClaims(token).get("userId", String::class.java)

    fun isTokenValid(token: String): Boolean {
        return try {
            parseClaims(token)
            true
        } catch (ex: Exception) {
            false
        }
    }

    private fun parseClaims(token: String) =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
}
