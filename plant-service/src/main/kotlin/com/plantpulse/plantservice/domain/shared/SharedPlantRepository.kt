package com.plantpulse.plantservice.domain.shared

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface SharedPlantRepository : JpaRepository<SharedPlant, UUID> {
    fun findByCity(city: String): List<SharedPlant>
    fun findByUserId(userId: UUID): List<SharedPlant>
    fun findByPlantId(plantId: UUID): SharedPlant?
}
