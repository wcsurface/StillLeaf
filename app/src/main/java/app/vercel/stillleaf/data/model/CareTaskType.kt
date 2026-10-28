package app.vercel.stillleaf.data.model

enum class CareTaskType(val displayName: String, val defaultIntervalDays: Int) {
    WATER("Water", 7),
    FEED("Nourish", 30),
    ROTATE("Rotate", 14),
    TRIM("Prune", 90),
    PHOTO("Photograph", 90),
    SPECIAL_TREATMENT("Special Care", 7)
}
