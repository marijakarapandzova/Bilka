package com.plantpulse.healthservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "plantpulse.health")
class HealthProperties {
    lateinit var reminderCron: String
    lateinit var decayCron: String
    lateinit var outbreakSweepCron: String
    var outbreakWindowDays: Long = 7
    var outbreakMinAffectedPlants: Long = 3
    var outbreakMinMatchPercentage: Int = 55
    var diseaseAlertMinMatchPercentage: Int = 50
    var decayGracePeriodDays: Long = 5
    var decayPointsPerDay: Int = 2
}
