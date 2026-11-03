package app.vercel.stillleaf.data.repository

import android.content.Context
import android.util.Log
import app.vercel.stillleaf.data.model.CareLog
import app.vercel.stillleaf.data.model.CareRequirement
import app.vercel.stillleaf.data.model.CareTaskType
import app.vercel.stillleaf.data.model.HealthStatusType
import app.vercel.stillleaf.data.model.Plant
import app.vercel.stillleaf.data.model.PlantPhoto
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime

object PlantPersistenceManager {

    private const val TAG = "PlantPersistence"
    private const val MASTER_PLANTS_FILE = "still_leaf_master_plants.json"

    private fun getFileNameForUser(userId: String): String {
        val sanitized = userId.lowercase().replace("[^a-z0-9]".toRegex(), "_")
        return "still_leaf_plants_${sanitized}.json"
    }

    /**
     * Pre-populated sample plants strictly for official Google Play App Review testing account (test@testaccount.com).
     */
    private fun getSampleTestPlants(): List<Plant> {
        val plantId1 = "plant_test_monstera"
        val plantId2 = "plant_test_peacelily"

        val p1 = Plant(
            id = plantId1,
            nickname = "Monstera",
            botanicalName = "Monstera deliciosa",
            commonName = "Swiss Cheese Plant",
            primaryPhotoUrl = "https://images.unsplash.com/photo-1614594975525-e45190c55d0b?auto=format&fit=crop&w=800&q=80",
            dateAdded = LocalDate.now().minusDays(90),
            careRequirements = CareRequirement(
                sunlight = "Bright Indirect Light",
                idealTemperature = "18°C - 27°C",
                idealHumidity = "50% - 70%",
                waterIntervalDays = 7,
                feedIntervalDays = 30,
                rotateIntervalDays = 14,
                trimIntervalDays = 60,
                careInsight = "Thrives in bright ambient light. Allow topsoil to dry before watering."
            ),
            location = "Indoor • Living Room",
            healthStatusType = HealthStatusType.HEALTHY,
            healthStatusText = "Healthy",
            photos = listOf(
                PlantPhoto(
                    id = "photo_test_1",
                    plantId = plantId1,
                    photoUrl = "https://images.unsplash.com/photo-1614594975525-e45190c55d0b?auto=format&fit=crop&w=800&q=80",
                    dateTaken = LocalDate.now().minusDays(90),
                    ageInDays = 0,
                    note = "Day 0 - Baseline Whole Plant Snapshot",
                    isQuarterlyMilestone = true,
                    milestoneTitle = "Day 0 - Baseline"
                )
            )
        )

        val p2 = Plant(
            id = plantId2,
            nickname = "Peace Lily",
            botanicalName = "Spathiphyllum wallisii",
            commonName = "Peace Lily",
            primaryPhotoUrl = "https://images.unsplash.com/photo-1593691509543-c55fb32e7355?auto=format&fit=crop&w=800&q=80",
            dateAdded = LocalDate.now().minusDays(45),
            careRequirements = CareRequirement(
                sunlight = "Medium Indirect Light",
                idealTemperature = "18°C - 24°C",
                idealHumidity = "50% - 80%",
                waterIntervalDays = 5,
                feedIntervalDays = 30,
                rotateIntervalDays = 14,
                trimIntervalDays = 60,
                careInsight = "Prefers moist, well-draining soil and humid ambient air."
            ),
            location = "Indoor • Office",
            healthStatusType = HealthStatusType.HEALTHY,
            healthStatusText = "Healthy",
            photos = listOf(
                PlantPhoto(
                    id = "photo_test_2",
                    plantId = plantId2,
                    photoUrl = "https://images.unsplash.com/photo-1593691509543-c55fb32e7355?auto=format&fit=crop&w=800&q=80",
                    dateTaken = LocalDate.now().minusDays(45),
                    ageInDays = 0,
                    note = "Day 0 - Baseline Whole Plant Snapshot",
                    isQuarterlyMilestone = true,
                    milestoneTitle = "Day 0 - Baseline"
                )
            )
        )

        return listOf(p1, p2)
    }

    /**
     * Clears local plant files for an account.
     */
    fun clearUserPlantsLocally(context: Context, userId: String) {
        try {
            val fileName = getFileNameForUser(userId)
            val userFile = File(context.filesDir, fileName)
            if (userFile.exists()) userFile.delete()

            val masterFile = File(context.filesDir, MASTER_PLANTS_FILE)
            if (masterFile.exists()) masterFile.delete()

            Log.d(TAG, "Cleared local plant files for account [$userId].")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing local plant files for user [$userId]", e)
        }
    }

    /**
     * Saves the user's current plant sanctuary list to user account storage and global master backup.
     */
    fun savePlantsLocally(context: Context, userId: String, plants: List<Plant>) {
        try {
            val jsonArray = JSONArray()
            plants.forEach { plant ->
                val plantJson = JSONObject().apply {
                    put("id", plant.id)
                    put("nickname", plant.nickname)
                    put("botanicalName", plant.botanicalName)
                    put("commonName", plant.commonName)
                    put("primaryPhotoUrl", plant.primaryPhotoUrl)
                    put("dateAdded", plant.dateAdded.toString())
                    put("location", plant.location)
                    put("healthStatusType", plant.healthStatusType.name)
                    put("healthStatusText", plant.healthStatusText)

                    // Care Requirements JSON
                    val careJson = JSONObject().apply {
                        put("sunlight", plant.careRequirements.sunlight)
                        put("idealTemperature", plant.careRequirements.idealTemperature)
                        put("idealHumidity", plant.careRequirements.idealHumidity)
                        put("waterIntervalDays", plant.careRequirements.waterIntervalDays)
                        put("feedIntervalDays", plant.careRequirements.feedIntervalDays)
                        put("rotateIntervalDays", plant.careRequirements.rotateIntervalDays)
                        put("trimIntervalDays", plant.careRequirements.trimIntervalDays)
                        put("careInsight", plant.careRequirements.careInsight)
                    }
                    put("careRequirements", careJson)

                    // Photos Array
                    val photosArray = JSONArray()
                    plant.photos.forEach { photo ->
                        val photoJson = JSONObject().apply {
                            put("id", photo.id)
                            put("plantId", photo.plantId)
                            put("photoUrl", photo.photoUrl)
                            put("dateTaken", photo.dateTaken.toString())
                            put("ageInDays", photo.ageInDays)
                            put("note", photo.note ?: "")
                            put("isQuarterlyMilestone", photo.isQuarterlyMilestone)
                            put("milestoneTitle", photo.milestoneTitle ?: "")
                        }
                        photosArray.put(photoJson)
                    }
                    put("photos", photosArray)

                    // Recent Care Logs Array
                    val logsArray = JSONArray()
                    plant.recentLogs.forEach { log ->
                        val logJson = JSONObject().apply {
                            put("id", log.id)
                            put("plantId", log.plantId)
                            put("taskType", log.taskType.name)
                            put("completedAtIso", log.completedAt.toString())
                        }
                        logsArray.put(logJson)
                    }
                    put("recentLogs", logsArray)
                }
                jsonArray.put(plantJson)
            }

            val jsonString = jsonArray.toString()

            // 1. Save to User Account File
            if (userId.isNotBlank()) {
                val fileName = getFileNameForUser(userId)
                val userFile = File(context.filesDir, fileName)
                userFile.writeText(jsonString)
            }

            // 2. Save to Global Master Backup File
            if (plants.isNotEmpty()) {
                val masterFile = File(context.filesDir, MASTER_PLANTS_FILE)
                masterFile.writeText(jsonString)
            }

            Log.d(TAG, "Successfully persisted ${plants.size} plants for account [$userId].")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving plants for user [$userId]", e)
        }
    }

    /**
     * Reads plants from file content string.
     */
    private fun parsePlantsFromJson(jsonString: String): List<Plant> {
        if (jsonString.isBlank()) return emptyList()
        val jsonArray = JSONArray(jsonString)
        val plantList = mutableListOf<Plant>()

        for (i in 0 until jsonArray.length()) {
            val plantJson = jsonArray.getJSONObject(i)

            val careJson = plantJson.getJSONObject("careRequirements")
            val careRequirements = CareRequirement(
                sunlight = careJson.optString("sunlight", "Bright Indirect Light"),
                idealTemperature = careJson.optString("idealTemperature", "18°C - 27°C"),
                idealHumidity = careJson.optString("idealHumidity", "50% - 70%"),
                waterIntervalDays = careJson.optInt("waterIntervalDays", 7),
                feedIntervalDays = careJson.optInt("feedIntervalDays", 30),
                rotateIntervalDays = careJson.optInt("rotateIntervalDays", 14),
                trimIntervalDays = careJson.optInt("trimIntervalDays", 60),
                careInsight = careJson.optString("careInsight", "Thrives in ambient light.")
            )

            val photosArray = plantJson.optJSONArray("photos") ?: JSONArray()
            val photosList = mutableListOf<PlantPhoto>()
            for (j in 0 until photosArray.length()) {
                val photoJson = photosArray.getJSONObject(j)
                val photo = PlantPhoto(
                    id = photoJson.optString("id", "p_$j"),
                    plantId = photoJson.optString("plantId", plantJson.getString("id")),
                    photoUrl = photoJson.optString("photoUrl", ""),
                    dateTaken = LocalDate.parse(photoJson.optString("dateTaken", LocalDate.now().toString())),
                    ageInDays = photoJson.optInt("ageInDays", 0),
                    note = photoJson.optString("note", ""),
                    isQuarterlyMilestone = photoJson.optBoolean("isQuarterlyMilestone", false),
                    milestoneTitle = photoJson.optString("milestoneTitle", "")
                )
                photosList.add(photo)
            }

            val logsArray = plantJson.optJSONArray("recentLogs") ?: JSONArray()
            val logsList = mutableListOf<CareLog>()
            for (k in 0 until logsArray.length()) {
                val logJson = logsArray.getJSONObject(k)
                val taskType = try {
                    CareTaskType.valueOf(logJson.getString("taskType"))
                } catch (_: Exception) {
                    CareTaskType.WATER
                }
                val completedAt = try {
                    LocalDateTime.parse(logJson.getString("completedAtIso"))
                } catch (_: Exception) {
                    LocalDateTime.now()
                }
                logsList.add(
                    CareLog(
                        id = logJson.optString("id", "log_$k"),
                        plantId = logJson.optString("plantId", plantJson.getString("id")),
                        taskType = taskType,
                        completedAt = completedAt
                    )
                )
            }

            val healthTypeString = plantJson.optString("healthStatusType", "HEALTHY")
            val healthType = try {
                HealthStatusType.valueOf(healthTypeString)
            } catch (_: Exception) {
                HealthStatusType.HEALTHY
            }

            val plant = Plant(
                id = plantJson.getString("id"),
                nickname = plantJson.getString("nickname"),
                botanicalName = plantJson.optString("botanicalName", ""),
                commonName = plantJson.optString("commonName", ""),
                primaryPhotoUrl = plantJson.optString("primaryPhotoUrl", ""),
                dateAdded = LocalDate.parse(plantJson.optString("dateAdded", LocalDate.now().toString())),
                careRequirements = careRequirements,
                location = plantJson.optString("location", "Indoor • Living Room"),
                healthStatusType = healthType,
                healthStatusText = plantJson.optString("healthStatusText", "Healthy"),
                photos = photosList,
                recentLogs = logsList
            )
            plantList.add(plant)
        }
        return plantList
    }

    /**
     * Loads plants for a specific user account ID. All real user accounts start with an EMPTY sanctuary.
     * Pre-seeds sample plants STRICTLY for the official Google Play Reviewer account (test@testaccount.com).
     */
    fun loadPlantsLocally(context: Context, userId: String): List<Plant> {
        if (userId.isBlank()) return emptyList()
        try {
            val fileName = getFileNameForUser(userId)
            val userFile = File(context.filesDir, fileName)

            if (userFile.exists()) {
                val jsonString = userFile.readText()
                val plants = parsePlantsFromJson(jsonString)
                if (plants.isNotEmpty()) {
                    return plants
                }
            }

            // Strict check: ONLY pre-seed for the official Google Play App Review test account
            val isExactGoogleReviewerAccount = userId.equals("usr_test_testaccount_com", ignoreCase = true) ||
                                               userId.equals("test@testaccount.com", ignoreCase = true)

            if (isExactGoogleReviewerAccount) {
                val samplePlants = getSampleTestPlants()
                savePlantsLocally(context, userId, samplePlants)
                Log.d(TAG, "Pre-seeded ${samplePlants.size} plants strictly for Google Play reviewer account [$userId].")
                return samplePlants
            }

            Log.d(TAG, "User account [$userId]. Returning authentic empty sanctuary (0 plants).")
            return emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring plants for user [$userId]", e)
            return emptyList()
        }
    }

    /**
     * Fallback loader for background notifications across saved local account files.
     */
    fun loadAllLocalPlants(context: Context): List<Plant> {
        val files = context.filesDir.listFiles { _, name -> name.startsWith("still_leaf_plants_") } ?: emptyArray()
        if (files.isEmpty()) {
            return loadPlantsLocally(context, "guest")
        }
        return files.flatMap { file ->
            val userId = file.name.removePrefix("still_leaf_plants_").removeSuffix(".json")
            loadPlantsLocally(context, userId)
        }
    }
}
