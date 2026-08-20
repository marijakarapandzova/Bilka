package com.plantpulse.healthservice.infrastructure.csv

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File

@Component
class CsvDataLoader {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var csvData: Map<String, Map<String, String>> = emptyMap()

    init {
        loadCsvData()
    }

    private fun loadCsvData() {
        try {
            // Try multiple paths for the CSV file
            val possiblePaths = listOf(
                File("Indoor_Plant_Health_and_Growth_Factors.csv"),           // Current directory (Docker)
                File("../Indoor_Plant_Health_and_Growth_Factors.csv"),        // Parent directory (local)
                File("/app/Indoor_Plant_Health_and_Growth_Factors.csv")       // Docker /app directory
            )

            var csvFile: File? = null
            for (path in possiblePaths) {
                if (path.exists()) {
                    csvFile = path
                    break
                }
            }

            if (csvFile == null) {
                logger.warn("CSV file not found in any of the expected locations")
                return
            }

            if (!csvFile.exists()) {
                logger.warn("CSV file not found at: ${csvFile.absolutePath}")
                return
            }

            val lines = csvFile.readLines()
            if (lines.isEmpty()) {
                logger.warn("CSV file is empty")
                return
            }

            // Find the first non-empty line as header
            val headerLine = lines.firstOrNull { it.isNotBlank() }
            if (headerLine == null || headerLine.isBlank()) {
                logger.warn("No valid header line found in CSV")
                return
            }

            // Remove BOM if present and split headers
            val cleanedLine = if (headerLine.startsWith('﻿')) headerLine.substring(1) else headerLine
            val headers = cleanedLine.split(",").map { it.trim() }
            logger.info("CSV Headers found: ${headers.joinToString(", ")}")

            // Look for Plant_Name column (the plant identifier)
            val plantNameIndex = headers.indexOfFirst { it.equals("Plant_Name", ignoreCase = true) }

            if (plantNameIndex == -1) {
                logger.warn("Plant_Name column not found in CSV. Headers: $headers")
                return
            }

            logger.info("Found Plant_Name at index $plantNameIndex: ${headers[plantNameIndex]}")

            val data = mutableMapOf<String, Map<String, String>>()

            for (i in 1 until lines.size) {
                val values = lines[i].split(",")
                if (values.size == headers.size) {
                    val plantName = values[plantNameIndex].trim()
                    val rowData = mutableMapOf<String, String>()

                    for (j in headers.indices) {
                        rowData[headers[j].trim()] = values[j].trim()
                    }

                    data[plantName] = rowData
                    logger.debug("Loaded CSV data for plant: $plantName")
                }
            }

            csvData = data
            logger.info("Loaded CSV data for ${data.size} plants from ${csvFile.absolutePath}")
        } catch (e: Exception) {
            logger.error("Error loading CSV data: ${e.message}", e)
        }
    }

    fun getPlantData(plantName: String): Map<String, String>? {
        return csvData[plantName]
    }

    fun getAllData(): Map<String, Map<String, String>> {
        return csvData
    }
}