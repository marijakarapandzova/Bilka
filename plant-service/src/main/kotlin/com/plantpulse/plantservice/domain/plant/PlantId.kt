package com.plantpulse.plantservice.domain.plant

import com.plantpulse.plantservice.domain.shared.UuidIdentifier
import java.util.UUID

/**
 * Value object representing a Plant aggregate identifier.
 * Implements the Identifier interface to be used with the event hierarchy.
 */
data class PlantId(override val value: String) : UuidIdentifier {
    constructor(uuid: UUID) : this(uuid.toString())
    constructor() : this(UUID.randomUUID())

    fun toUUID(): UUID = UUID.fromString(value)

    override fun toString(): String = value
}
