package com.plantpulse.plantservice.domain.species

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SpeciesRepository : JpaRepository<Species, UUID> {
    fun findByNameContainingIgnoreCase(name: String): List<Species>
    fun findByScientificNameContainingIgnoreCase(scientificName: String): List<Species>
}
