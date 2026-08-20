package com.plantpulse.healthservice.domain.health

/**
 * Coarse trend label shown alongside the numeric 0-100 health score, derived
 * by comparing the newest score against the previous one (and, for CRITICAL,
 * an absolute floor regardless of trend).
 */
enum class HealthStatus {
    HEALTHY,
    STABLE,
    DECLINING,
    RECOVERING,
    CRITICAL
}

enum class SnapshotSource {
    OBSERVATION, // computed immediately after the user logged an observation
    DECAY,       // computed by the nightly batch job for plants with no recent activity
    INITIAL      // seeded when the plant was first added
}
