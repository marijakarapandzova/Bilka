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
            .subject(email)
            .claim("userId", userId)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact()
    }

    fun extractEmail(token: String): String = parseClaims(token).subject

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
