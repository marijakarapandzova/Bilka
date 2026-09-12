package com.plantpulse.mcpserver.config

import com.plantpulse.mcpserver.tools.CalendarTools
import com.plantpulse.mcpserver.tools.DiagnosisTools
import com.plantpulse.mcpserver.tools.PlantPulseTools
import com.plantpulse.mcpserver.tools.SlackTools
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class McpToolsConfig {

    @Bean
    fun plantPulseToolCallbacks(
        plantPulseTools: PlantPulseTools,
        diagnosisTools: DiagnosisTools,
        slackTools: SlackTools,
        calendarTools: CalendarTools
    ): ToolCallbackProvider =
        MethodToolCallbackProvider.builder()
            .toolObjects(plantPulseTools, diagnosisTools, slackTools, calendarTools)
            .build()
}
