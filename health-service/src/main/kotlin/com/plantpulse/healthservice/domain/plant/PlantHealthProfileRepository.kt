package com.plantpulse.healthservice.domain.plant

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PlantHealthProfileRepository : JpaRepository<PlantHealthProfile, UUID> {
    fun findByUserIdAndActiveTrue(userId: UUID): List<PlantHealthProfile>
    fun findByIdAndUserId(id: UUID, userId: UUID): PlantHealthProfile?
    fun findByActiveTrue(): List<PlantHealthProfile>
    fun findByCityLocationAndActiveTrue(cityLocation: String): List<PlantHealthProfile>
}
