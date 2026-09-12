package com.plantpulse.mcpserver.crypto

import com.plantpulse.mcpserver.config.McpProperties
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES-GCM at-rest encryption for Slack bot tokens / Google refresh tokens.
 * Key is derived (SHA-256) from `plantpulse.token-encryption-key`, mirroring how
 * plant-service/health-service take their JWT secret from a single configured string.
 */
@Component
class TokenCipher(properties: McpProperties) {

    private val key = SecretKeySpec(
        MessageDigest.getInstance("SHA-256").digest(properties.tokenEncryptionKey.toByteArray()),
        "AES"
    )
    private val secureRandom = SecureRandom()

    fun encrypt(plaintext: String): String {
        val iv = ByteArray(12).also { secureRandom.nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val ciphertext = cipher.doFinal(plaintext.toByteArray())
        return Base64.getEncoder().encodeToString(iv + ciphertext)
    }

    fun decrypt(encoded: String): String {
        val raw = Base64.getDecoder().decode(encoded)
        val iv = raw.copyOfRange(0, 12)
        val ciphertext = raw.copyOfRange(12, raw.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        return String(cipher.doFinal(ciphertext))
    }
}
