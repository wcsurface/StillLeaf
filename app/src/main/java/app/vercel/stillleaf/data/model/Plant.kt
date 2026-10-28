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
    val careInsight: String
)

data class Plant(
    val id: String,
    val nickname: String,
    val botanicalName: String,
    val commonName: String,
    val primaryPhotoUrl: String,
    val dateAdded: LocalDate,
    val careRequirements: CareRequirement,
    val photos: List<PlantPhoto> = emptyList(),
    val recentLogs: List<CareLog> = emptyList()
)
