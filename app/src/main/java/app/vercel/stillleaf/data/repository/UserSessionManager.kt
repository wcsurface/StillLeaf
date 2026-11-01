package app.vercel.stillleaf.data.repository

import android.content.Context
import android.util.Log
import app.vercel.stillleaf.data.model.AuthUser
import app.vercel.stillleaf.data.model.SubscriptionStatus
import org.json.JSONObject

object UserSessionManager {

    private const val TAG = "UserSessionManager"
    private const val PREFS_NAME = "still_leaf_session_prefs"
    private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
    private const val KEY_USER_JSON = "key_user_json"
    private const val KEY_SUB_PREFIX = "sub_status_"

    /**
     * Saves the authenticated user session and subscription entitlement status to persistent SharedPreferences.
     */
    fun saveUserSession(context: Context, user: AuthUser) {
        try {
            // Read any previously saved subscription status for this account if upgrading occurred
            val savedSubStatus = getSavedSubscriptionStatus(context, user.id, user.subscriptionStatus)

            val updatedUser = user.copy(subscriptionStatus = savedSubStatus)

            val userJson = JSONObject().apply {
                put("id", updatedUser.id)
                put("email", updatedUser.email)
                put("displayName", updatedUser.displayName)
                put("authProvider", updatedUser.authProvider)
                put("subscriptionStatus", updatedUser.subscriptionStatus.name)
                put("token", updatedUser.token ?: "")
            }

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_JSON, userJson.toString())
                .putString(KEY_SUB_PREFIX + updatedUser.id, updatedUser.subscriptionStatus.name)
                .apply()

            Log.d(TAG, "Successfully persisted session for user [${user.email}] with status [${updatedUser.subscriptionStatus.name}].")
        } catch (e: Exception) {
            Log.e(TAG, "Error persisting user session", e)
        }
    }

    /**
     * Updates and persists a user's upgraded subscription status locally and prepares server sync.
     */
    fun updateUserSubscriptionStatus(context: Context, userId: String, newStatus: SubscriptionStatus) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_SUB_PREFIX + userId, newStatus.name).apply()

            val currentUser = getSavedUserSession(context)
            if (currentUser != null && currentUser.id == userId) {
                val updatedUser = currentUser.copy(subscriptionStatus = newStatus)
                saveUserSession(context, updatedUser)
            }
            Log.d(TAG, "Updated subscription status for account [$userId] to [${newStatus.name}].")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user subscription status", e)
        }
    }

    /**
     * Reads saved subscription status for a specific user ID.
     */
    fun getSavedSubscriptionStatus(context: Context, userId: String, fallback: SubscriptionStatus = SubscriptionStatus.FREE): SubscriptionStatus {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val statusString = prefs.getString(KEY_SUB_PREFIX + userId, null) ?: return fallback
            return SubscriptionStatus.valueOf(statusString)
        } catch (e: Exception) {
            return fallback
        }
    }

    /**
     * Reads saved user session on app launch. Returns null if not logged in.
     */
    fun getSavedUserSession(context: Context): AuthUser? {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
            if (!isLoggedIn) return null

            val userJsonString = prefs.getString(KEY_USER_JSON, null) ?: return null
            val userJson = JSONObject(userJsonString)
            val userId = userJson.getString("id")

            val statusString = prefs.getString(KEY_SUB_PREFIX + userId, userJson.optString("subscriptionStatus", "FREE")) ?: "FREE"
            val subscriptionStatus = try {
                SubscriptionStatus.valueOf(statusString)
            } catch (_: Exception) {
                SubscriptionStatus.FREE
            }

            return AuthUser(
                id = userId,
                email = userJson.getString("email"),
                displayName = userJson.getString("displayName"),
                authProvider = userJson.optString("authProvider", "GOOGLE"),
                subscriptionStatus = subscriptionStatus,
                token = userJson.optString("token", "")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring saved user session", e)
            return null
        }
    }

    /**
     * Clears active session on explicit sign-out.
     */
    fun clearUserSession(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_IS_LOGGED_IN, false).remove(KEY_USER_JSON).apply()
            Log.d(TAG, "User session signed out successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing user session", e)
        }
    }
}
