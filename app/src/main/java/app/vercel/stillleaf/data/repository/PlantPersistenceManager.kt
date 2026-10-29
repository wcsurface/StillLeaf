package app.vercel.stillleaf.data.repository

import android.content.Context
import android.util.Log
import app.vercel.stillleaf.data.model.CareRequirement
import app.vercel.stillleaf.data.model.HealthStatusType
import app.vercel.stillleaf.data.model.Plant
import app.vercel.stillleaf.data.model.PlantPhoto
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate

object PlantPersistenceManager {

    private const val TAG = "PlantPersistence"
    private const val FILE_NAME = "still_leaf_plants.json"

    /**
     * Saves the user's current plant sanctuary list and all photos to local device storage.
     */
    fun savePlantsLocally(context: Context, plants: List<Plant>) {
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
                }
                jsonArray.put(plantJson)
            }

            val file = File(context.filesDir, FILE_NAME)
            file.writeText(jsonArray.toString())
            Log.d(TAG, "Successfully persisted ${plants.size} plants to local storage.")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving plants to local storage", e)
        }
    }

    /**
     * Restores saved plant sanctuary list and photos from local device storage on app launch.
     */
    fun loadPlantsLocally(context: Context): List<Plant> {
        try {
            val file = File(context.filesDir, FILE_NAME)
            if (!file.exists()) {
                Log.d(TAG, "No local plant storage file found. Returning empty sanctuary.")
                return emptyList()
            }

            val jsonString = file.readText()
            if (jsonString.isBlank()) {
                return emptyList()
            }

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
                    photos = photosList
                )
                plantList.add(plant)
            }

            Log.d(TAG, "Successfully restored ${plantList.size} plants from local storage.")
            return plantList
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring plants from local storage", e)
            return emptyList()
        }
    }
}
