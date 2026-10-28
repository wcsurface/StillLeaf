package app.vercel.stillleaf.data.model

import java.time.LocalDateTime

data class CareLog(
    val id: String,
    val plantId: String,
    val taskType: CareTaskType,
    val completedAt: LocalDateTime = LocalDateTime.now()
)
