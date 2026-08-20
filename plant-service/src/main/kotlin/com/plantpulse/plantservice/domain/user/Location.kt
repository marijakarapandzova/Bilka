package com.plantpulse.plantservice.domain.user

import jakarta.persistence.Embeddable

@Embeddable
data class Location(
    val city: String,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    // JPA needs a no-arg constructor
    protected constructor() : this(city = "", latitude = null, longitude = null)
}
