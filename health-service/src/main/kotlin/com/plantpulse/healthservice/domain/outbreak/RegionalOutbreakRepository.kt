package com.plantpulse.healthservice.domain.outbreak

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RegionalOutbreakRepository : JpaRepository<RegionalOutbreak, UUID> {
    fun findByCityLocationAndDiseaseNameAndActiveTrue(cityLocation: String, diseaseName: String): RegionalOutbreak?
    fun findByCityLocationAndActiveTrue(cityLocation: String): List<RegionalOutbreak>
    fun findByActiveTrue(): List<RegionalOutbreak>
}
