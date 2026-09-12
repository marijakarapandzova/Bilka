package com.plantpulse.mcpserver.checklist

import com.plantpulse.mcpserver.client.DashboardPlantCard
import com.plantpulse.mcpserver.client.DashboardResponse
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class DailyChecklistBuilder {

    /** Builds a Slack `chat.postMessage`-ready mrkdwn checklist from a dashboard snapshot. */
    fun build(dashboard: DashboardResponse): String {
        val actionable = dashboard.plants.filter { !it.skipWatering && it.wateringUrgency != "UPCOMING" }
        val diseased = dashboard.plants.filter { it.activeDisease != null }
        val upcomingCount = dashboard.plants.count { it.wateringUrgency == "UPCOMING" }

        if (actionable.isEmpty() && diseased.isEmpty()) {
            return "*🌱 PlantPulse — ${LocalDate.now()}*\nNothing due today. Your garden is all caught up! 🎉"
        }

        val lines = StringBuilder("*🌱 Your PlantPulse checklist — ${LocalDate.now()}*\n")

        val overdue = actionable.filter { it.wateringUrgency == "OVERDUE" }
        if (overdue.isNotEmpty()) {
            lines.append("\n:rotating_light: *Overdue*\n")
            overdue.forEach { lines.append(wateringLine(it)) }
        }

        val dueToday = actionable.filter { it.wateringUrgency == "DUE_TODAY" }
        if (dueToday.isNotEmpty()) {
            lines.append("\n:droplet: *Water today*\n")
            dueToday.forEach { lines.append(wateringLine(it)) }
        }

        if (diseased.isNotEmpty()) {
            lines.append("\n:herb: *Needs attention*\n")
            diseased.forEach {
                lines.append("• ${it.nickname} — possible ${it.activeDisease} (health score ${it.healthScore})\n")
            }
        }

        if (upcomingCount > 0) {
            lines.append("\n_$upcomingCount more plant(s) not due yet._")
        }

        return lines.toString()
    }

    private fun wateringLine(card: DashboardPlantCard): String {
        val detail = when (card.wateringUrgency) {
            "OVERDUE" -> "overdue by ${-card.daysUntilWatering} day(s)"
            else -> "due today"
        }
        return "• ${card.nickname} (${card.speciesName}) — $detail\n"
    }
}
