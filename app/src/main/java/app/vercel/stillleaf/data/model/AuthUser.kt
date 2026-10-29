package app.vercel.stillleaf.data.model

data class AuthUser(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val authProvider: String = "EMAIL", // "EMAIL" or "GOOGLE"
    val subscriptionStatus: SubscriptionStatus = SubscriptionStatus.FREE,
    val token: String? = null
)
