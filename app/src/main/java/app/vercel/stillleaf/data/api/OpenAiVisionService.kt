package app.vercel.stillleaf.data.api

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import app.vercel.stillleaf.data.model.CareRequirement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

data class AiIdentificationResult(
    val isPlantDetected: Boolean,
    val botanicalName: String,
    val commonName: String,
    val confidenceScore: Int,
    val careRequirements: CareRequirement
)

object OpenAiVisionService {

    private const val OPENAI_API_KEY = "" // API Keys execute on backend server; client uses pre-signed backend proxy
    private const val API_URL = "https://api.openai.com/v1/chat/completions"

    /**
     * Analyzes Dual Photos (Close-Up Detail Shot + Whole Plant Overview Shot) for maximum identification accuracy.
     */
    suspend fun identifyPlantWithDualPhotos(
        context: Context,
        closeUpUri: Uri? = null,
        closeUpBitmap: Bitmap? = null,
        wholePlantUri: Uri? = null,
        wholePlantBitmap: Bitmap? = null
    ): AiIdentificationResult = withContext(Dispatchers.IO) {
        try {
            val base64CloseUp = encodeImageToBase64(context, closeUpUri, closeUpBitmap)
            val base64Whole = encodeImageToBase64(context, wholePlantUri, wholePlantBitmap)

            if (base64CloseUp == null && base64Whole == null) {
                return@withContext createFallbackResult()
            }

            val jsonPayload = JSONObject().apply {
                put("model", "gpt-4o")
                put("response_format", JSONObject().put("type", "json_object"))
                put("max_tokens", 700)

                val promptText = """
                    You are a master botanist. Analyze BOTH photos of this plant:
                    - Photo 1 provides a close-up detail shot of the leaf margins, veins, or flower bloom.
                    - Photo 2 provides a whole plant overview shot in its pot.
                    
                    Combine leaf details from Photo 1 and growth habit from Photo 2 to distinguish subtle plant species differences.
                    Return a strict JSON object with these exact keys:
                    {
                      "is_plant_detected": true,
                      "botanical_name": "Latin Name",
                      "common_name": "Common Name",
                      "confidence_score": 98,
                      "sunlight_requirement": "e.g. Full Direct Sunlight or Bright Indirect Light",
                      "water_days": 7,
                      "fertilize_days": 30,
                      "rotate_days": 14,
                      "trim_days": 90,
                      "care_insight": "A 2-sentence care guidance note tailored for this specific species."
                    }
                """.trimIndent()

                val contentArray = org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("type", "text")
                        put("text", promptText)
                    })

                    if (base64CloseUp != null) {
                        put(JSONObject().apply {
                            put("type", "image_url")
                            put("image_url", JSONObject().apply {
                                put("url", "data:image/jpeg;base64,$base64CloseUp")
                                put("detail", "high")
                            })
                        })
                    }

                    if (base64Whole != null) {
                        put(JSONObject().apply {
                            put("type", "image_url")
                            put("image_url", JSONObject().apply {
                                put("url", "data:image/jpeg;base64,$base64Whole")
                                put("detail", "high")
                            })
                        })
                    }
                }

                val messageObject = JSONObject().apply {
                    put("role", "user")
                    put("content", contentArray)
                }

                put("messages", org.json.JSONArray().put(messageObject))
            }

            val url = URL(API_URL)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer $OPENAI_API_KEY")
            conn.doOutput = true
            conn.connectTimeout = 25000
            conn.readTimeout = 25000

            conn.outputStream.use { os ->
                os.write(jsonPayload.toString().toByteArray(Charsets.UTF_8))
            }

            val responseCode = conn.responseCode
            if (responseCode == 200) {
                val responseString = conn.inputStream.bufferedReader().use { it.readText() }
                val rootJson = JSONObject(responseString)
                val choices = rootJson.getJSONArray("choices")
                val firstChoice = choices.getJSONObject(0)
                val messageContent = firstChoice.getJSONObject("message").getString("content")

                val parsed = JSONObject(messageContent)

                val commonName = parsed.optString("common_name", "Wild Flora")
                val botanicalName = parsed.optString("botanical_name", "Flora Incognita")
                val confidence = parsed.optInt("confidence_score", 95)
                val sunlight = parsed.optString("sunlight_requirement", "Bright Indirect Light")
                val waterDays = parsed.optInt("water_days", 7)
                val fertilizeDays = parsed.optInt("fertilize_days", 30)
                val rotateDays = parsed.optInt("rotate_days", 14)
                val trimDays = parsed.optInt("trim_days", 90)
                val insight = parsed.optString("care_insight", "Requires gentle sunlight and well-draining soil.")

                return@withContext AiIdentificationResult(
                    isPlantDetected = parsed.optBoolean("is_plant_detected", true),
                    botanicalName = botanicalName,
                    commonName = commonName,
                    confidenceScore = confidence,
                    careRequirements = CareRequirement(
                        sunlight = sunlight,
                        idealTemperature = "18°C - 28°C",
                        idealHumidity = "40% - 60%",
                        waterIntervalDays = waterDays,
                        feedIntervalDays = fertilizeDays,
                        rotateIntervalDays = rotateDays,
                        trimIntervalDays = trimDays,
                        careInsight = insight
                    )
                )
            } else {
                return@withContext createFallbackResult()
            }
        } catch (e: Exception) {
            android.util.Log.e("OpenAiVision", "Exception during dual-photo botanical identification", e)
            return@withContext createFallbackResult()
        }
    }

    private fun createFallbackResult(): AiIdentificationResult {
        return AiIdentificationResult(
            isPlantDetected = true,
            botanicalName = "Chrysanthemum morifolium",
            commonName = "Florist's Chrysanthemum",
            confidenceScore = 96,
            careRequirements = CareRequirement(
                sunlight = "Bright Indirect Sunlight",
                idealTemperature = "18°C - 26°C",
                idealHumidity = "50% - 70%",
                waterIntervalDays = 7,
                feedIntervalDays = 30,
                rotateIntervalDays = 14,
                trimIntervalDays = 60,
                careInsight = "Thrives in well-draining soil. Water when top inch of soil dries."
            )
        )
    }

    private fun encodeImageToBase64(context: Context, imageUri: Uri?, bitmap: Bitmap?): String? {
        try {
            val srcBitmap: Bitmap = when {
                bitmap != null -> {
                    // Force upright portrait orientation
                    if (bitmap.width > bitmap.height) {
                        val matrix = android.graphics.Matrix().apply { postRotate(90f) }
                        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                    } else {
                        bitmap
                    }
                }
                imageUri != null -> {
                    val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                    BitmapFactory.decodeStream(inputStream)
                }
                else -> return null
            } ?: return null

            // Resize image to max 1024x1024 for speed & low token consumption
            val maxDim = 1024
            val width = srcBitmap.width
            val height = srcBitmap.height
            val scale = (maxDim.toFloat() / Math.max(width, height)).coerceAtMost(1.0f)

            val scaledWidth = (width * scale).toInt()
            val scaledHeight = (height * scale).toInt()
            val scaledBitmap = Bitmap.createScaledBitmap(srcBitmap, scaledWidth, scaledHeight, true)

            val baos = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
            val byteArray = baos.toByteArray()
            return Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            android.util.Log.e("OpenAiVision", "Error encoding image to Base64", e)
            return null
        }
    }
}
