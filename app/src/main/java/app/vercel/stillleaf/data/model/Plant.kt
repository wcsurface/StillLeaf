package app.vercel.stillleaf.data.model

import java.time.LocalDate

data class CareRequirement(
    val sunlight: String,
    val idealTemperature: String,
    val idealHumidity: String,
    val waterIntervalDays: Int = 7,
    val feedIntervalDays: Int = 30,
    val rotateIntervalDays: Int = 14,
    val trimIntervalDays: Int = 90,
    val careInsight: String,
    val nourishType: String = "Balanced Organic Liquid Fertilizer (10-10-10)",
    val nourishAmount: String = "1/2 teaspoon diluted per gallon of water, applied every 30 days during active growth."
)

data class Plant(
    val id: String,
    val nickname: String,
    val botanicalName: String,
    val commonName: String,
    val primaryPhotoUrl: String,
    val dateAdded: LocalDate,
    val careRequirements: CareRequirement,
    val location: String = "Indoor • Living Room",
    val healthStatusType: HealthStatusType = HealthStatusType.HEALTHY,
    val healthStatusText: String = "Healthy",
    val activeDiagnosis: PlantDiagnosis? = null,
    val photos: List<PlantPhoto> = emptyList(),
    val recentLogs: List<CareLog> = emptyList()
)
