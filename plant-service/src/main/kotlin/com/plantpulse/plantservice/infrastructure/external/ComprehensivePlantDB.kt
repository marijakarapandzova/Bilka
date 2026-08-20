package com.plantpulse.plantservice.infrastructure.external

/**
 * Comprehensive plant care database with detailed growing information.
 * Data sourced from horticultural guides and gardening databases.
 */

data class PlantCareData(
    val commonNames: List<String>,
    val scientificNames: List<String>,
    val careGuides: String,
    val wateringInstructions: String,
    val lightRequirements: String,
    val temperatureRange: String,
    val humidityRequirements: String,
    val soilRequirements: String,
    val maintenanceGuide: String,
    val propagationMethods: String,
    val commonPests: String,
    val commonDiseases: String,
    val hardiness: String,
    val growthRate: String,
    val plantCycle: String,
    val careLevel: String
)

object ComprehensivePlantDB {
    private val plants = mapOf(
        // Japanese Maple variants
        "acer palmatum linearilobum" to PlantCareData(
            commonNames = listOf("Linearilobum Japanese Maple", "Bamboo-Leaf Japanese Maple"),
            scientificNames = listOf("Acer palmatum 'Linearilobum'"),
            careGuides = "The Linearilobum Japanese Maple is a beautiful and unique tree with stunning foliage that combines bright, vibrant hues of yellow, orange and red throughout the seasons. Its gracefully arranged and finely divided leaves are long and narrow, resembling conifer needles. The Linearilobum has great variety in its form and size, making it ideal for focal points in gardens or small spaces. It is a relatively low maintenance tree and can thrive in moderately acidic or moist conditions.",
            wateringInstructions = "Water once a week, providing about 1 inch of water. The best time to water is early in the morning, so the foliage can dry quickly and avoid fungal diseases. During periods of extreme heat, you may need to water more often to maintain moisture levels. Avoid overwatering, as Japanese maples prefer moist, but not soaking wet, soil. In winter, reduce watering frequency.",
            lightRequirements = "Prefers partial sunlight and shade. Should receive bright, filtered sunlight for about 4-6 hours a day, preferably during morning or late afternoon hours when the sun's rays are less intense. When located in direct sunlight, the plant may become sunburned or suffer from leaf scorch. Best to avoid too much direct sun, especially during the hottest part of the day.",
            temperatureRange = "Hardiness Zone 5 (-20 to -10°F / -29 to -23°C)",
            humidityRequirements = "Prefers moderate to high humidity. Benefits from misting during hot, dry periods. Adequate air circulation helps prevent fungal issues.",
            soilRequirements = "Thrives in moderately acidic to neutral soil (pH 5.5-7.0). Prefers well-draining, humus-rich soil. Amend soil with compost or leaf mold. Avoid compacted or heavy clay soils.",
            maintenanceGuide = "Prune twice a year to maintain shape and size. In early summer, lightly trim back branches to control size. In late winter or early spring, prune dead or damaged branches. Remove less than one-third of total branches at any time to maintain natural balance. Use sharp, clean pruning shears and avoid tearing branches.",
            propagationMethods = "Propagate from softwood cuttings in early summer or hardwood cuttings in winter. Seeds can be sown in fall. Air layering is also effective.",
            commonPests = "Generally pest-resistant. Occasional issues with scale insects, spider mites, and aphids. Monitor for Japanese beetles in summer.",
            commonDiseases = "Susceptible to leaf scorch in hot, dry conditions. Can develop powdery mildew in humid conditions. Ensure good air circulation and avoid overhead watering.",
            hardiness = "Zone 5-8",
            growthRate = "Low to Moderate (6-12 inches per year)",
            plantCycle = "Perennial, Deciduous",
            careLevel = "Medium"
        ),

        "acer palmatum" to PlantCareData(
            commonNames = listOf("Japanese Maple", "Smooth Japanese Maple"),
            scientificNames = listOf("Acer palmatum"),
            careGuides = "Japanese maples are elegant, deciduous trees prized for their delicate foliage and graceful form. They are relatively low-maintenance when planted in appropriate conditions. Can thrive in various garden settings, from traditional Japanese gardens to contemporary landscapes. The finely dissected leaves provide year-round interest with spring emergence, summer shade, and spectacular fall colors.",
            wateringInstructions = "Water regularly, especially during the growing season and the first 2 years after planting. Keep soil moist but well-draining. Young trees need more frequent watering than established specimens. Once established, they are fairly drought-tolerant but perform better with regular moisture. Water deeply but less frequently rather than shallow daily watering.",
            lightRequirements = "Prefers dappled shade to partial shade. In northern regions, can tolerate more direct sun. In hot climates, afternoon shade is essential to prevent leaf scorch. Morning sun with afternoon shade is ideal. Intense afternoon sun can cause leaf burn.",
            temperatureRange = "Hardiness Zone 5-8 (-20 to 20°F / -29 to -7°C)",
            humidityRequirements = "Prefers moderate humidity. Benefits from afternoon misting in hot weather. Morning watering helps prevent fungal issues.",
            soilRequirements = "Requires well-draining soil with good organic matter content (pH 5.5-7.0). Amend planting area with compost or peat moss. Avoid wet, poorly draining sites which can lead to root rot.",
            maintenanceGuide = "Prune in late winter or early spring to maintain shape and remove dead branches. Remove any crossing or inward-growing branches. Avoid heavy pruning as Japanese maples have delicate branching structure. Minimal pruning needed once established.",
            propagationMethods = "Propagate from softwood cuttings in early summer or hardwood cuttings in winter. Seeds can be collected in fall and stratified for spring germination.",
            commonPests = "Generally pest-resistant. Occasionally affected by scale insects, spider mites, or Japanese beetles. Regular monitoring helps catch issues early.",
            commonDiseases = "Susceptible to anthracnose and leaf scorch in hot, dry climates. Root rot can occur in poorly drained soil. Ensure good drainage and air circulation.",
            hardiness = "Zone 5-8",
            growthRate = "Slow to Moderate (12-24 inches per year)",
            plantCycle = "Perennial, Deciduous",
            careLevel = "Easy to Moderate"
        ),

        "sansevieria trifasciata" to PlantCareData(
            commonNames = listOf("Snake Plant", "Mother-in-law's Tongue", "Viper's Bowstring Hemp"),
            scientificNames = listOf("Sansevieria trifasciata", "Dracaena trifasciata"),
            careGuides = "Snake plants are extremely hardy and low-maintenance houseplants, perfect for beginners. They are nearly indestructible and tolerate a wide range of conditions. Known for their ability to purify air, they produce oxygen at night. Can thrive in neglect and are ideal for offices or homes with irregular watering schedules.",
            wateringInstructions = "Water sparingly - only when soil is completely dry. In growing season (spring/summer), water every 2-3 weeks. In winter, reduce to once a month or less. Overwatering is the main cause of problems. Allow soil to dry out completely between waterings. Water less in cooler months.",
            lightRequirements = "Tolerates low light but grows best in bright, indirect light. Can survive in direct sun, though intense afternoon sun may cause leaves to look washed out. Adapts to most light conditions except deep shade.",
            temperatureRange = "Prefers 60-75°F (15-24°C). Can tolerate temperatures as low as 50°F (10°C) but growth slows. Avoid temperatures below 50°F.",
            humidityRequirements = "Very tolerant of low humidity. No special humidity requirements. Does not benefit from misting.",
            soilRequirements = "Requires well-draining potting soil. Use cactus/succulent mix or add perlite to regular potting soil. Good drainage is essential to prevent root rot.",
            maintenanceGuide = "Minimal pruning needed. Remove dead leaves at the base. Wipe leaves occasionally with damp cloth to remove dust. Repot every 2-3 years when crowded.",
            propagationMethods = "Propagate from leaf cuttings placed in soil or water. Separate rhizomes when repotting. Leaf propagation takes several months to develop roots.",
            commonPests = "Rarely affected by pests. Occasionally spider mites in very dry conditions. Mealybugs may appear on stressed plants.",
            commonDiseases = "Root rot from overwatering is the main issue. Fungal leaf spots can occur in very humid conditions. Ensure good drainage.",
            hardiness = "Houseplant (zones 10-12 outdoors)",
            growthRate = "Slow",
            plantCycle = "Perennial, Evergreen",
            careLevel = "Easy"
        ),

        "epipremnum aureum" to PlantCareData(
            commonNames = listOf("Pothos", "Devil's Ivy", "Golden Pothos", "Taro Vine"),
            scientificNames = listOf("Epipremnum aureum", "Rhaphidophora aurea"),
            careGuides = "Pothos is one of the most popular houseplants due to its attractive heart-shaped leaves and extraordinary resilience. It's an excellent climber or trailer and adapts well to various growing conditions. Known for air-purifying properties. Nearly impossible to kill, making it ideal for beginners and busy plant parents.",
            wateringInstructions = "Water when top 1-2 inches of soil are dry. In growing season, typically every 7-10 days. In winter, reduce watering to every 2-3 weeks. Prefers slightly moist but not soggy soil. Lower watering needs than most houseplants. Let soil dry out somewhat between waterings.",
            lightRequirements = "Thrives in bright, indirect light but adapts to low light conditions. Can tolerate direct sun but may scorch leaves. Does well in office settings with artificial lighting. More variegation appears in brighter light.",
            temperatureRange = "Prefers 65-85°F (18-29°C). Can tolerate temperatures as low as 60°F (15°C). Avoid temperatures below 50°F (10°C).",
            humidityRequirements = "Not particular about humidity. Tolerates dry indoor air well. Occasional misting can help keep leaves clean and prevent spider mites.",
            soilRequirements = "Requires well-draining potting soil. Standard potting mix works well. Mix in perlite for extra drainage. Repot every 12-18 months.",
            maintenanceGuide = "Prune regularly to encourage bushier growth and prevent leggy appearance. Pinch off stem tips. Wipe leaves occasionally with damp cloth. Train on moss pole or trellis for climbing or allow to trail.",
            propagationMethods = "Very easy to propagate from stem cuttings. Place cuttings in water or moist soil. Roots develop within 1-2 weeks. Can also propagate via nodes.",
            commonPests = "Spider mites are the most common pest. Mealybugs and scale occasionally appear. Inspect regularly and treat early.",
            commonDiseases = "Generally disease-free. Root rot can occur from overwatering. Leaf spots may develop in very humid conditions.",
            hardiness = "Houseplant (zones 11-12 outdoors)",
            growthRate = "Fast",
            plantCycle = "Perennial, Evergreen",
            careLevel = "Very Easy"
        ),

        "chlorophytum comosum" to PlantCareData(
            commonNames = listOf("Spider Plant", "Ribbon Plant", "St. Bernard's Lily"),
            scientificNames = listOf("Chlorophytum comosum"),
            careGuides = "Spider plants are among the easiest houseplants to grow and care for. They produce attractive arching, variegated leaves and produce decorative runners with baby plantlets. Excellent for hanging baskets. Known for air-purifying properties.",
            wateringInstructions = "Water regularly during growing season, keeping soil consistently moist but not waterlogged. In winter, reduce watering slightly. Allow top inch of soil to dry between waterings. Sensitive to fluoride and chlorine in water - use filtered or distilled water if possible.",
            lightRequirements = "Thrives in bright, indirect light. Can tolerate partial shade and direct sun. Variegation is most pronounced in bright light. Can handle office lighting.",
            temperatureRange = "Prefers 60-75°F (15-24°C). Can tolerate temperatures as low as 50°F (10°C). Avoid sudden temperature changes.",
            humidityRequirements = "Tolerates average indoor humidity. Doesn't require misting but benefits from occasional leaf cleaning.",
            soilRequirements = "Prefers well-draining, fertile potting soil. Standard potting mix works well. Repot every year or when crowded.",
            maintenanceGuide = "Remove dead leaves and runners as desired. Leave runners to develop plantlets for new plants. Divide crowded plants during repotting. Generally minimal care needed.",
            propagationMethods = "Propagate from plantlets (baby plants) on runners. Place in water until roots develop, then pot in soil. Can also divide mature plants.",
            commonPests = "Spider mites and mealybugs occasionally appear. Usually pest-free.",
            commonDiseases = "Generally disease-free. Leaf tips may brown from fluoride in water.",
            hardiness = "Houseplant (zones 9-11 outdoors)",
            growthRate = "Moderate to Fast",
            plantCycle = "Perennial, Evergreen",
            careLevel = "Very Easy"
        ),

        "spathiphyllum wallisii" to PlantCareData(
            commonNames = listOf("Peace Lily", "White Flag", "Mauna Loa Plant"),
            scientificNames = listOf("Spathiphyllum wallisii"),
            careGuides = "Peace lilies are elegant houseplants with glossy leaves and striking white spathes. They are excellent air purifiers and relatively low-maintenance. One of the few flowering houseplants that bloom indoors reliably. Ideal for low-light areas.",
            wateringInstructions = "Water when soil surface feels dry. Typically every 7-10 days. Peace lilies will noticeably droop when thirsty, then perk up after watering - use this as a guide. Keep soil consistently moist but not waterlogged. Reduce watering in winter.",
            lightRequirements = "Prefers low to moderate indirect light. Thrives in shade. Tolerates low office lighting. Can survive in dim corners but flowers better with more light. Avoid direct sun which can scorch leaves.",
            temperatureRange = "Prefers 65-80°F (18-27°C). Can tolerate temperatures as low as 60°F (15°C). Sensitive to cold drafts.",
            humidityRequirements = "Prefers high humidity. Benefits from regular misting, humidity trays, or placement in bathrooms. Wipe leaves occasionally.",
            soilRequirements = "Requires fertile, well-draining potting soil. Mix with peat moss or orchid bark. Repot every 1-2 years.",
            maintenanceGuide = "Remove faded flowers and dead leaves. Cut flower stems back to base after blooming. Dust leaves regularly. Generally minimal pruning needed.",
            propagationMethods = "Divide at the roots during repotting. Separate plantlets that develop from base of plant.",
            commonPests = "Spider mites and mealybugs can appear in dry conditions. Scale insects occasionally.",
            commonDiseases = "Generally disease-free. Leaf tips may brown from low humidity or chlorine in water.",
            hardiness = "Houseplant (zones 11-12 outdoors)",
            growthRate = "Slow to Moderate",
            plantCycle = "Perennial, Evergreen",
            careLevel = "Easy"
        )
    )

    /**
     * Search for plant care data by common name or scientific name
     */
    fun searchPlant(query: String): PlantCareData? {
        val lowerQuery = query.lowercase().trim()

        // Direct key match
        plants[lowerQuery]?.let { return it }

        // Search in common names
        for ((_, data) in plants) {
            if (data.commonNames.any { it.lowercase().contains(lowerQuery) }) {
                return data
            }
        }

        // Search in scientific names
        for ((_, data) in plants) {
            if (data.scientificNames.any { it.lowercase().contains(lowerQuery) }) {
                return data
            }
        }

        return null
    }

    /**
     * Get plant care data by scientific name (exact match)
     */
    fun getByScientificName(scientificName: String): PlantCareData? {
        val lowerName = scientificName.lowercase().trim()
        for ((_, data) in plants) {
            if (data.scientificNames.any { it.lowercase() == lowerName }) {
                return data
            }
        }
        return null
    }

    /**
     * Get plant care data by common name (exact match)
     */
    fun getByCommonName(commonName: String): PlantCareData? {
        val lowerName = commonName.lowercase().trim()
        for ((_, data) in plants) {
            if (data.commonNames.any { it.lowercase() == lowerName }) {
                return data
            }
        }
        return null
    }

    /**
     * Enrich Perenual plant data with comprehensive care information
     */
    fun enrichPlantData(perenualPlant: PerenualPlant): Map<String, Any?> {
        // Try to find comprehensive care data by scientific name or common name
        var careData = perenualPlant.scientific_name?.firstOrNull()?.let { getByScientificName(it) }
        if (careData == null) {
            careData = perenualPlant.common_name?.let { searchPlant(it) }
        }

        val enriched = mutableMapOf<String, Any?>()
        enriched["id"] = perenualPlant.id
        enriched["common_name"] = perenualPlant.common_name
        enriched["scientific_name"] = perenualPlant.scientific_name

        if (careData != null) {
            // Use comprehensive database data
            enriched["care_guides"] = careData.careGuides
            enriched["watering"] = careData.wateringInstructions
            enriched["sunlight"] = careData.lightRequirements
            enriched["temperature"] = careData.temperatureRange
            enriched["humidity"] = careData.humidityRequirements
            enriched["soil"] = careData.soilRequirements
            enriched["maintenance"] = careData.maintenanceGuide
            enriched["propagation"] = careData.propagationMethods
            enriched["pest_susceptibility"] = careData.commonPests
            enriched["disease_susceptibility"] = careData.commonDiseases
            enriched["hardiness"] = careData.hardiness
            enriched["growth_rate"] = careData.growthRate
            enriched["cycle"] = careData.plantCycle
            enriched["care_level"] = careData.careLevel
            enriched["data_source"] = "COMPREHENSIVE_DB"
        } else {
            // Fall back to Perenual API data
            enriched["care_guides"] = perenualPlant.care_guides
            enriched["watering"] = perenualPlant.watering
            enriched["sunlight"] = perenualPlant.sunlight
            enriched["maintenance"] = perenualPlant.maintenance
            enriched["propagation"] = perenualPlant.propagation
            enriched["pest_susceptibility"] = perenualPlant.pest_susceptibility
            enriched["disease_susceptibility"] = perenualPlant.disease_susceptibility
            enriched["hardiness"] = perenualPlant.hardiness
            enriched["growth_rate"] = perenualPlant.growth_rate
            enriched["cycle"] = perenualPlant.cycle
            enriched["care_level"] = perenualPlant.difficulty_level
            enriched["data_source"] = "PERENUAL_API"
        }

        // Always include basic API data
        enriched["api_data"] = mapOf(
            "temperature_min_c" to perenualPlant.temperature_min_c,
            "temperature_max_c" to perenualPlant.temperature_max_c,
            "ph_min" to perenualPlant.ph_min,
            "ph_max" to perenualPlant.ph_max,
            "soil_list" to perenualPlant.soil,
            "image_url" to perenualPlant.default_image?.medium_url
        )

        return enriched
    }

    /**
     * Check if we have comprehensive data for a plant
     */
    fun hasComprehensiveData(perenualPlant: PerenualPlant): Boolean {
        return perenualPlant.scientific_name?.firstOrNull()?.let { getByScientificName(it) != null }
            ?: perenualPlant.common_name?.let { searchPlant(it) != null }
            ?: false
    }
}
