package com.plantpulse.plantservice.domain.shared

/**
 * Base interface for all aggregate identifiers.
 * Ensures every identifier has a typed value that can be used as a Kafka message key.
 */
interface Identifier<T> {
    val value: T

    override fun toString(): String
}

/**
 * Marker interface for UUID-based identifiers
 */
interface UuidIdentifier : Identifier<String>
