package app.vercel.stillleaf.data.model

import java.time.LocalDate

data class PlantPhoto(
    val id: String,
    val plantId: String,
    val photoUrl: String,
    val dateTaken: LocalDate,
    val ageInDays: Int,
    val note: String? = null,
    val isQuarterlyMilestone: Boolean = false,
    val milestoneTitle: String? = null
)
