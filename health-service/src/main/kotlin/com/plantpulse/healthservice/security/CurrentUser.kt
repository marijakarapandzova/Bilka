package com.plantpulse.healthservice.security

import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

object CurrentUser {
    // Matches plant-service's dev fallback so the two services behave the
    // same way when no bearer token is presented (e.g. local demo/testing).
    private val DEV_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")

    fun id(): UUID {
        val principal = SecurityContextHolder.getContext().authentication?.principal
        return if (principal is UUID) principal else DEV_USER_ID
    }
}
