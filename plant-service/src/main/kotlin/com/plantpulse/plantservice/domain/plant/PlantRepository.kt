package com.plantpulse.plantservice.domain.plant

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PlantRepository : JpaRepository<Plant, UUID> {
    fun findByUserId(userId: UUID): List<Plant>
}
