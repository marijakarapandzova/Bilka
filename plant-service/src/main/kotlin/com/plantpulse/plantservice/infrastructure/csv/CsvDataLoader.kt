package com.plantpulse.plantservice.infrastructure.csv

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
            // Try to load from the repository root
            val csvFile = File("../Indoor_Plant_Health_and_Growth_Factors.csv")
            if (!csvFile.exists()) {
                logger.warn("CSV file not found at expected location")
                return
            }

            val lines = csvFile.readLines()
            if (lines.isEmpty()) {
                logger.warn("CSV file is empty")
                return
            }

            val headers = lines[0].split(",")
            val plantIdIndex = headers.indexOf("Plant_ID")

            if (plantIdIndex == -1) {
                logger.warn("Plant_ID column not found in CSV")
                return
            }

            val data = mutableMapOf<String, Map<String, String>>()

            for (i in 1 until lines.size) {
                val values = lines[i].split(",")
                if (values.size == headers.size) {
                    val plantId = values[plantIdIndex].trim()
                    val rowData = mutableMapOf<String, String>()

                    for (j in headers.indices) {
                        rowData[headers[j].trim()] = values[j].trim()
                    }

                    data[plantId] = rowData
                }
            }

            csvData = data
            logger.info("Loaded CSV data for ${data.size} plants")
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