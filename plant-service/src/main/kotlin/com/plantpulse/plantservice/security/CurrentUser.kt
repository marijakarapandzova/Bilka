package com.plantpulse.plantservice.security

import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

object CurrentUser {
    private val DEV_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")

    fun id(): UUID {
        val principal = SecurityContextHolder.getContext().authentication?.principal
        return if (principal is UUID) {
            principal
        } else {
            DEV_USER_ID // Dev mode: return default user
        }
    }
}
