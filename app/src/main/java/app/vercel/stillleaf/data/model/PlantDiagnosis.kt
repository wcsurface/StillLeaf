package app.vercel.stillleaf.data.model

data class PlantDiagnosis(
    val id: String,
    val issueDescription: String,
    val photoUrl: String? = null,
    val diagnosisTitle: String,
    val confidenceScore: Int = 92,
    val severityLevel: String = "Moderate",
    val rootCauseSummary: String,
    val cureSteps: List<String>,
    val specialTreatmentTaskName: String? = null,
    val diagnosedAt: String
)
