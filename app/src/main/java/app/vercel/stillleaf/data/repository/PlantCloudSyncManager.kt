package app.vercel.stillleaf.data.repository

import android.content.Context
import android.util.Log
import app.vercel.stillleaf.data.model.CareLog
import app.vercel.stillleaf.data.model.CareRequirement
import app.vercel.stillleaf.data.model.CareTaskType
import app.vercel.stillleaf.data.model.HealthStatusType
import app.vercel.stillleaf.data.model.Plant
import app.vercel.stillleaf.data.model.PlantPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime

object PlantCloudSyncManager {

    private const val TAG = "PlantCloudSync"
    private const val BASE_URL = "https://stillleaf-production.up.railway.app/api/plants"

    /**
     * Fetches user's complete plant sanctuary from Railway PostgreSQL Database across all devices.
     */
    suspend fun fetchUserPlantsFromCloud(context: Context, userId: String): List<Plant> = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext emptyList()
        try {
            val url = URL("$BASE_URL?userId=$userId")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            val responseCode = conn.responseCode
            if (responseCode == 200) {
                val responseString = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonRoot = JSONObject(responseString)
                val dataArray = jsonRoot.optJSONArray("data") ?: JSONArray()

                val cloudPlants = mutableListOf<Plant>()
                for (i in 0 until dataArray.length()) {
                    val plantJson = dataArray.getJSONObject(i)

                    val careJson = plantJson.optJSONObject("careRequirements") ?: JSONObject()
                    val careRequirements = CareRequirement(
                        sunlight = careJson.optString("sunlight", "Bright Indirect Light"),
                        idealTemperature = careJson.optString("idealTemperature", "18°C - 27°C"),
                        idealHumidity = careJson.optString("idealHumidity", "50% - 70%"),
                        waterIntervalDays = careJson.optInt("waterIntervalDays", 7),
                        feedIntervalDays = careJson.optInt("feedIntervalDays", 30),
                        rotateIntervalDays = careJson.optInt("rotateIntervalDays", 14),
                        trimIntervalDays = careJson.optInt("trimIntervalDays", 60),
                        careInsight = careJson.optString("careInsight", "Thrives in ambient daylight.")
                    )

                    val photosArray = plantJson.optJSONArray("photos") ?: JSONArray()
                    val photosList = mutableListOf<PlantPhoto>()
                    for (j in 0 until photosArray.length()) {
                        val photoJson = photosArray.getJSONObject(j)
                        photosList.add(
                            PlantPhoto(
                                id = photoJson.optString("id", "p_$j"),
                                plantId = plantJson.getString("id"),
                                photoUrl = photoJson.optString("photoUrl", ""),
                                dateTaken = LocalDate.parse(photoJson.optString("takenAt", LocalDate.now().toString()).take(10)),
                                ageInDays = photoJson.optInt("ageInDays", 0),
                                note = photoJson.optString("note", ""),
                                isQuarterlyMilestone = photoJson.optBoolean("isQuarterlyMilestone", true)
                            )
                        )
                    }

                    val logsArray = plantJson.optJSONArray("careLogs") ?: JSONArray()
                    val logsList = mutableListOf<CareLog>()
                    for (k in 0 until logsArray.length()) {
                        val logJson = logsArray.getJSONObject(k)
                        val taskType = try { CareTaskType.valueOf(logJson.getString("taskType")) } catch (_: Exception) { CareTaskType.WATER }
                        logsList.add(
                            CareLog(
                                id = logJson.optString("id", "log_$k"),
                                plantId = plantJson.getString("id"),
                                taskType = taskType,
                                completedAt = LocalDateTime.now()
                            )
                        )
                    }

                    val healthTypeString = plantJson.optString("healthStatusType", "HEALTHY")
                    val healthType = try { HealthStatusType.valueOf(healthTypeString) } catch (_: Exception) { HealthStatusType.HEALTHY }

                    val plant = Plant(
                        id = plantJson.getString("id"),
                        nickname = plantJson.getString("nickname"),
                        botanicalName = plantJson.optString("botanicalName", ""),
                        commonName = plantJson.optString("commonName", ""),
                        primaryPhotoUrl = photosList.firstOrNull()?.photoUrl ?: "",
                        dateAdded = LocalDate.now(),
                        careRequirements = careRequirements,
                        location = plantJson.optString("location", "Indoor • Living Room"),
                        healthStatusType = healthType,
                        healthStatusText = plantJson.optString("healthStatusText", "Healthy"),
                        photos = photosList,
                        recentLogs = logsList
                    )
                    cloudPlants.add(plant)
                }

                if (cloudPlants.isNotEmpty()) {
                    PlantPersistenceManager.savePlantsLocally(context, userId, cloudPlants)
                    Log.d(TAG, "Restored ${cloudPlants.size} cloud plants from Railway PostgreSQL for [$userId].")
                }
                return@withContext cloudPlants
            } else {
                Log.w(TAG, "Railway cloud sync response code: $responseCode")
                return@withContext emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user plants from Railway cloud", e)
            return@withContext emptyList()
        }
    }

    /**
     * Backs up a newly added plant profile to Railway PostgreSQL Database.
     */
    suspend fun savePlantToCloud(userId: String, plant: Plant) = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext
        try {
            val jsonPayload = JSONObject().apply {
                put("userId", userId)
                put("plantId", plant.id)
                put("nickname", plant.nickname)
                put("botanicalName", plant.botanicalName)
                put("commonName", plant.commonName)
                put("location", plant.location)
                put("primaryPhotoUrl", plant.primaryPhotoUrl)
                put("careRequirements", JSONObject().apply {
                    put("sunlight", plant.careRequirements.sunlight)
                    put("waterIntervalDays", plant.careRequirements.waterIntervalDays)
                    put("feedIntervalDays", plant.careRequirements.feedIntervalDays)
                    put("rotateIntervalDays", plant.careRequirements.rotateIntervalDays)
                    put("trimIntervalDays", plant.careRequirements.trimIntervalDays)
                    put("careInsight", plant.careRequirements.careInsight)
                })
            }

            val url = URL(BASE_URL)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 10000

            conn.outputStream.use { os ->
                os.write(jsonPayload.toString().toByteArray(Charsets.UTF_8))
            }

            val responseCode = conn.responseCode
            Log.d(TAG, "Saved plant [${plant.nickname}] to Railway PostgreSQL (HTTP $responseCode).")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving plant to Railway PostgreSQL", e)
        }
    }
}
