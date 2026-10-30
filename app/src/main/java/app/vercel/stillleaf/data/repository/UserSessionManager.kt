package app.vercel.stillleaf.data.repository

import android.content.Context
import android.util.Log
import app.vercel.stillleaf.data.model.AuthUser
import org.json.JSONObject

object UserSessionManager {

    private const val TAG = "UserSessionManager"
    private const val PREFS_NAME = "still_leaf_session_prefs"
    private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
    private const val KEY_USER_JSON = "key_user_json"

    /**
     * Saves the authenticated user session to persistent SharedPreferences.
     */
    fun saveUserSession(context: Context, user: AuthUser) {
        try {
            val userJson = JSONObject().apply {
                put("id", user.id)
                put("email", user.email)
                put("displayName", user.displayName)
                put("authProvider", user.authProvider)
                put("token", user.token ?: "")
            }

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_JSON, userJson.toString())
                .apply()

            Log.d(TAG, "Successfully persisted session for user [${user.email}].")
        } catch (e: Exception) {
            Log.e(TAG, "Error persisting user session", e)
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

            return AuthUser(
                id = userJson.getString("id"),
                email = userJson.getString("email"),
                displayName = userJson.getString("displayName"),
                authProvider = userJson.optString("authProvider", "GOOGLE"),
                token = userJson.optString("token", "")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring saved user session", e)
            return null
        }
    }

    /**
     * Clears user session on explicit sign-out.
     */
    fun clearUserSession(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            Log.d(TAG, "User session cleared successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing user session", e)
        }
    }
}
