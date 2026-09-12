package com.plantpulse.mcpserver.tools

import com.plantpulse.mcpserver.client.LogObservationRequest
import com.plantpulse.mcpserver.client.PlantPulseClient
import com.plantpulse.mcpserver.session.SessionStore
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class DiagnosisTools(
    private val plantPulseClient: PlantPulseClient,
    private val sessionStore: SessionStore
) {

    @Tool(
        description = "Diagnose a plant from a photo you (the assistant) have already looked at. Look at the " +
            "photo yourself and fill in the symptom fields below, then this tool submits them to PlantPulse's " +
            "disease matcher and returns the diagnosis plus treatment steps."
    )
    fun diagnosePlantPhoto(
        @ToolParam(description = "session_token returned by plantpulse_login") sessionToken: String,
        @ToolParam(description = "id of the plant, from list_my_plants") plantId: String,
        @ToolParam(description = "Predominant leaf color you observe: GREEN, YELLOW, BROWN, or SPOTTED") leafColor: String,
        @ToolParam(description = "Leaf texture you observe: HEALTHY, WILTING, MUSHY, or CRISPY") leafTexture: String,
        @ToolParam(description = "Soil moisture you observe: DRY, MOIST, or WATERLOGGED") soilMoisture: String,
        @ToolParam(description = "Whether you can see pests (insects, webs, eggs) in the photo") visiblePests: Boolean,
        @ToolParam(description = "New growth you observe: NORMAL, SLOW, STUNTED, or NONE") growth: String,
        @ToolParam(description = "Any other notes worth recording, or empty string") notes: String
    ): String {
        val session = sessionStore.get(sessionToken)
        val observation = plantPulseClient.logObservation(
            session.jwt,
            UUID.fromString(plantId),
            LogObservationRequest(
                leafColor = leafColor.uppercase(),
                leafTexture = leafTexture.uppercase(),
                soilMoisture = soilMoisture.uppercase(),
                visiblePests = visiblePests,
                growth = growth.uppercase(),
                notes = notes.ifBlank { null }
            )
        )

        val match = observation.diseaseMatch
        return "Diagnosis: ${match.diseaseName} (${match.matchPercentage}% match). " +
            "Health score: ${observation.healthScore ?: "n/a"}. " +
            "Recommended steps: ${match.treatmentSteps.joinToString("; ").ifBlank { "none" }}."
    }
}
