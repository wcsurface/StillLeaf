package app.vercel.stillleaf.data.model

enum class SubscriptionStatus {
    FREE,
    PRO;

    val isPro: Boolean
        get() = this == PRO

    val maxPlantsAllowed: Int
        get() = if (isPro) Int.MAX_VALUE else 2
}
