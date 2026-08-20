package com.plantpulse.plantservice.domain.observation

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ObservationRepository : JpaRepository<Observation, UUID> {
    fun findByPlantIdOrderByLoggedAtDesc(plantId: UUID): List<Observation>
}
