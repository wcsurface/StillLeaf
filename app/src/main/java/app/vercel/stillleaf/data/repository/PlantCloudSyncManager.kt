package app.vercel.stillleaf.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime

object PlantCloudSyncManager {

    private const val TAG = "PlantCloudSync"
    private const val BASE_URL = "https://stillleaf-production-d8f2.up.railway.app/api/plants"

    private fun resolveLocalPhotoUrl(context: Context, rawUrl: String, plantId: String): String {
        if (rawUrl.isBlank()) return ""

        if (rawUrl.startsWith("data:image/jpeg;base64,")) {
            try {
                val photosDir = File(context.filesDir, "plant_photos")
                if (!photosDir.exists()) photosDir.mkdirs()

                val destFile = File(photosDir, "restored_${plantId}.jpg")
                val base64Data = rawUrl.substringAfter("data:image/jpeg;base64,")
                val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                destFile.writeBytes(bytes)
                return Uri.fromFile(destFile).toString()
            } catch (e: Exception) {
                Log.e(TAG, "Error saving restored cloud photo to local file", e)
                return rawUrl
            }
        }

        return rawUrl
    }

    private fun convertLocalPhotoToBase64(context: Context, photoUrl: String): String {
        if (photoUrl.isBlank()) return ""
        if (photoUrl.startsWith("data:image/jpeg;base64,")) return photoUrl

        try {
            val uri = Uri.parse(photoUrl)
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return photoUrl
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

            val maxDim = 800
            var inSampleSize = 1
            if (options.outHeight > maxDim || options.outWidth > maxDim) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= maxDim && halfWidth / inSampleSize >= maxDim) {
                    inSampleSize *= 2
                }
            }
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { this.inSampleSize = inSampleSize })
            if (bitmap != null) {
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos)
                val base64Str = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                return "data:image/jpeg;base64,$base64Str"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error converting local photo to base64 for cloud backup", e)
        }
        return photoUrl
    }

    /**
     * Fetches user's complete plant sanctuary from Railway PostgreSQL Database across all devices by userId or email.
     */
    suspend fun fetchUserPlantsFromCloud(context: Context, userId: String, email: String = ""): List<Plant> = withContext(Dispatchers.IO) {
        if (userId.isBlank() && email.isBlank()) return@withContext emptyList()
        try {
            val queryUrl = "$BASE_URL?userId=$userId&email=$email"
            val url = URL(queryUrl)
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

                    val sunlight = plantJson.optString("sunlightRequirement", "Bright Indirect Light")
                    val waterInterval = plantJson.optInt("waterIntervalDays", 7)
                    val feedInterval = plantJson.optInt("feedIntervalDays", 30)
                    val rotateInterval = plantJson.optInt("rotateIntervalDays", 14)
                    val trimInterval = plantJson.optInt("trimIntervalDays", 60)
                    val careSummary = plantJson.optString("careSummary", "Thrives in ambient daylight.")

                    val careRequirements = CareRequirement(
                        sunlight = sunlight,
                        idealTemperature = plantJson.optString("idealTemperature", "18°C - 27°C"),
                        idealHumidity = plantJson.optString("idealHumidity", "50% - 70%"),
                        waterIntervalDays = waterInterval,
                        feedIntervalDays = feedInterval,
                        rotateIntervalDays = rotateInterval,
                        trimIntervalDays = trimInterval,
                        careInsight = careSummary
                    )

                    val photosArray = plantJson.optJSONArray("photos") ?: JSONArray()
                    val photosList = mutableListOf<PlantPhoto>()
                    for (j in 0 until photosArray.length()) {
                        val photoJson = photosArray.getJSONObject(j)
                        val rawPhotoUrl = photoJson.optString("photoUrl", "")
                        val localPhotoUrl = resolveLocalPhotoUrl(context, rawPhotoUrl, plantJson.getString("id"))

                        photosList.add(
                            PlantPhoto(
                                id = photoJson.optString("id", "p_$j"),
                                plantId = plantJson.getString("id"),
                                photoUrl = localPhotoUrl,
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

                    val firstPhotoUrl = photosList.firstOrNull()?.photoUrl ?: ""

                    val plant = Plant(
                        id = plantJson.getString("id"),
                        nickname = plantJson.getString("nickname"),
                        botanicalName = plantJson.optString("botanicalName", ""),
                        commonName = plantJson.optString("commonName", ""),
                        primaryPhotoUrl = firstPhotoUrl,
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
                    Log.d(TAG, "Restored ${cloudPlants.size} cloud plants from Railway PostgreSQL for [$userId / $email].")
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
     * Backs up a plant profile to Railway PostgreSQL Database via auto-upsert /sync endpoint.
     */
    suspend fun savePlantToCloud(context: Context, userId: String, email: String, plant: Plant) = withContext(Dispatchers.IO) {
        if (userId.isBlank() && email.isBlank()) return@withContext
        try {
            val persistentPhotoUrl = convertLocalPhotoToBase64(context, plant.primaryPhotoUrl)

            val jsonPayload = JSONObject().apply {
                put("userId", userId)
                put("email", email)
                put("plant", JSONObject().apply {
                    put("id", plant.id)
                    put("nickname", plant.nickname)
                    put("botanicalName", plant.botanicalName)
                    put("commonName", plant.commonName)
                    put("location", plant.location)
                    put("primaryPhotoUrl", persistentPhotoUrl)
                    put("careRequirements", JSONObject().apply {
                        put("sunlight", plant.careRequirements.sunlight)
                        put("waterIntervalDays", plant.careRequirements.waterIntervalDays)
                        put("feedIntervalDays", plant.careRequirements.feedIntervalDays)
                        put("rotateIntervalDays", plant.careRequirements.rotateIntervalDays)
                        put("trimIntervalDays", plant.careRequirements.trimIntervalDays)
                        put("careInsight", plant.careRequirements.careInsight)
                    })
                })
            }

            val syncUrl = "$BASE_URL/sync"
            val url = URL(syncUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 10000

            conn.outputStream.use { os ->
                os.write(jsonPayload.toString().toByteArray(Charsets.UTF_8))
            }

            val responseCode = conn.responseCode
            Log.d(TAG, "Backed up plant [${plant.nickname}] to Railway PostgreSQL (HTTP $responseCode).")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing plant to Railway PostgreSQL", e)
        }
    }
}
