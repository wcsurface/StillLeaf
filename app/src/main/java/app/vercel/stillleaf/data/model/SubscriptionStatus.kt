package app.vercel.stillleaf.data.model

enum class SubscriptionStatus {
    FREE,
    MONTHLY,
    ANNUAL,
    LIFETIME;

    val isPro: Boolean
        get() = this != FREE

    val maxPlantsAllowed: Int
        get() = if (isPro) Int.MAX_VALUE else 3

    val freeScansRemaining: Int
        get() = if (isPro) Int.MAX_VALUE else 3
}
