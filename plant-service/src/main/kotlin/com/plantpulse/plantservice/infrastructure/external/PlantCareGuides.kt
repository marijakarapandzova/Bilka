package com.plantpulse.plantservice.infrastructure.external

// Manually curated care guides for popular plants
// This supplements the Perenual API data with detailed care instructions

data class PlantCareGuide(
    val perenualId: Int,
    val careGuides: String?,
    val watering: String?,
    val sunlight: String?,
    val maintenance: String?,
    val hardiness: String?,
    val cycle: String?,
    val growthRate: String?,
    val careLevel: String?
)

object PlantCareGuidesDB {
    private val guides = mapOf(
        // Linearilobum Japanese Maple (ID 77)
        77 to PlantCareGuide(
            perenualId = 77,
            careGuides = "The Linearilobum Japanese Maple is a beautiful and unique tree. Its stunning foliage combines bright, vibrant hues of yellow, orange and red that it displays throughout the different seasons. Its gracefully arranged and finely divided leaves are long and narrow, resembling conifer needles. The Linearilobum has a great deal of variety in its form and size, making it both an ideal choice for a focal point in any garden, or for small spaces. It is also a relatively low maintenance tree and can thrive in moderately acidic or moist conditions.",
            watering = "Water once a week, providing about 1 inch of water. The best time to water is early in the morning, so the foliage can dry quickly and avoid fungal diseases. During periods of extreme heat, you may need to water more often in order to maintain moisture levels. Avoid overwatering, as Japanese maples prefer moist, but not soaking wet, soil.",
            sunlight = "The Linearilobum Japanese Maple is a slow-growing, deciduous shrub that prefers partial sunlight and shade. This plant should receive bright, filtered sunlight for about 4-6 hours a day, preferably during morning or late afternoon hours when the sun's rays are less intense. When located in direct sunlight, the plant may become sunburned or suffer from leaf scorch. It is best to avoid too much direct sun, especially during the hottest part of the day.",
            maintenance = "Linearilobum Japanese Maple should be pruned twice a year to maintain its shape and size. In the early summer, lightly trim back the branches to control the size of the tree. Then in the late winter or early spring, prune back any branches that are dead or damaged. Remove less than a third of the total branches on the tree at any 1 time to maintain a natural balance. When pruning, use sharp, clean pruning shears and avoid tearing branches when possible.",
            hardiness = "Hardiness Zone 5",
            cycle = "Perennial",
            growthRate = "Low",
            careLevel = "Medium"
        ),

        // Japanese Maple (ID 27)
        27 to PlantCareGuide(
            perenualId = 27,
            careGuides = "Japanese maples are elegant, deciduous trees prized for their delicate foliage and graceful form. They are relatively low-maintenance and can thrive in various garden settings, from traditional Japanese gardens to contemporary landscapes.",
            watering = "Water regularly, especially during the growing season. Keep the soil moist but well-draining. Young trees need more frequent watering. Once established, they are drought-tolerant but perform better with regular moisture.",
            sunlight = "Prefers partial shade to dappled sunlight. In northern regions, they can tolerate more sun. In hot climates, afternoon shade is essential to prevent leaf scorch. Morning sun with afternoon shade is ideal.",
            maintenance = "Prune in late winter or early spring to maintain shape and remove dead branches. Avoid heavy pruning as Japanese maples have delicate branches. Remove any crossing or inward-growing branches.",
            hardiness = "Hardiness Zone 5-8",
            cycle = "Perennial",
            growthRate = "Slow to moderate",
            careLevel = "Easy to moderate"
        ),

        // Snake Plant (ID 1002 - example, find actual ID from Perenual)
        // 1002 to PlantCareGuide(
        //     perenualId = 1002,
        //     careGuides = "Snake plants are extremely hardy and low-maintenance houseplants...",
        //     watering = "Water sparingly, only when soil is completely dry...",
        //     etc...
        // ),

        // Add more plants here following the same format
        // Template:
        // PERENUAL_ID to PlantCareGuide(
        //     perenualId = PERENUAL_ID,
        //     careGuides = "Full description of the plant...",
        //     watering = "How often and how to water...",
        //     sunlight = "Light requirements...",
        //     maintenance = "Pruning and maintenance tips...",
        //     hardiness = "Hardiness zone/temperature range...",
        //     cycle = "Perennial/Annual/etc...",
        //     growthRate = "Slow/Moderate/Fast...",
        //     careLevel = "Easy/Moderate/Difficult..."
        // ),
    )

    fun getGuide(perenualId: Int): PlantCareGuide? = guides[perenualId]

    fun hasGuide(perenualId: Int): Boolean = guides.containsKey(perenualId)

    fun getAllGuides(): Map<Int, PlantCareGuide> = guides

    fun enrichPlantData(perenualPlant: PerenualPlant): Map<String, Any?> {
        val guide = getGuide(perenualPlant.id)
        val result = mutableMapOf<String, Any?>()

        result["id"] = perenualPlant.id
        result["common_name"] = perenualPlant.common_name
        result["scientific_name"] = perenualPlant.scientific_name
        result["other_name"] = perenualPlant.other_name

        // Use guide data if available, otherwise use API data
        result["care_guides"] = guide?.careGuides ?: perenualPlant.care_guides
        result["watering"] = guide?.watering ?: perenualPlant.watering
        result["sunlight"] = guide?.sunlight ?: perenualPlant.sunlight
        result["maintenance"] = guide?.maintenance ?: perenualPlant.maintenance
        result["hardiness"] = guide?.hardiness ?: perenualPlant.hardiness
        result["cycle"] = guide?.cycle ?: perenualPlant.cycle
        result["growth_rate"] = guide?.growthRate ?: perenualPlant.growth_rate
        result["care_level"] = guide?.careLevel ?: perenualPlant.difficulty_level

        // Always include original API data
        result["api_data"] = mapOf(
            "temperature_min_c" to perenualPlant.temperature_min_c,
            "temperature_max_c" to perenualPlant.temperature_max_c,
            "ph_min" to perenualPlant.ph_min,
            "ph_max" to perenualPlant.ph_max,
            "soil" to perenualPlant.soil,
            "image_url" to perenualPlant.default_image?.medium_url
        )

        return result
    }
}
