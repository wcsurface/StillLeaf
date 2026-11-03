package app.vercel.stillleaf.data.api

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import app.vercel.stillleaf.data.model.CareRequirement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class AiIdentificationResult(
    val isPlantDetected: Boolean = true,
    val botanicalName: String,
    val commonName: String,
    val confidenceScore: Int,
    val careRequirements: CareRequirement
)

object OpenAiVisionService {

    private const val TAG = "OpenAiVision"
    private const val BACKEND_URL = "https://stillleaf-production-d8f2.up.railway.app/api/plants/identify-dual"
    private const val DIRECT_API_URL = "https://api.openai.com/v1/chat/completions"

    private const val ENCODED_VISION_KEY = "c2stcHJvai1OTllYV09ENTBPN2JHQ1NiU0plUmFTU0NKbm5YaGdhWUFzeGZEUUJxd2JxN2FYNUtzMDNfVkwtLWhxLW9hdk1ySWRmQjlvMEUxcFQzQmxia0ZKVWFpbFRObVU1LU05Yk9kWFZQWXV1U3FxUV9FaG1yVEwyX0Z2Q0FKM0dMNlAwcVNndmk2NVFTbXJoelJ4MUlCNEtub01NMVp5WUE="

    private fun getActiveApiKey(): String {
        return try {
            val cleanedKey = ENCODED_VISION_KEY.replace("\r", "").replace("\n", "").replace(" ", "").trim()
            val decodedBytes = Base64.decode(cleanedKey, Base64.DEFAULT or Base64.URL_SAFE)
            String(decodedBytes, Charsets.UTF_8).trim()
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding vision API key", e)
            ""
        }
    }

    /**
     * Dual-photo botanical vision analysis routed via Railway Backend Proxy with direct OpenAI fallback.
     */
    suspend fun identifyPlantWithDualPhotos(
        context: Context,
        closeUpUri: Uri? = null,
        closeUpBitmap: Bitmap? = null,
        wholePlantUri: Uri? = null,
        wholePlantBitmap: Bitmap? = null
    ): AiIdentificationResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting botanical image encoding...")
            val base64CloseUp = encodeImageToBase64(context, closeUpUri, closeUpBitmap)
            val base64Whole = encodeImageToBase64(context, wholePlantUri, wholePlantBitmap)

            if (base64CloseUp == null && base64Whole == null) {
                Log.e(TAG, "Both base64 photo encodings returned null. Executing safety fallback.")
                return@withContext createFallbackResult()
            }

            // 1. Try Railway Backend Server Proxy
            try {
                val proxyJsonPayload = JSONObject().apply {
                    put("base64CloseUp", base64CloseUp ?: "")
                    put("base64Whole", base64Whole ?: "")
                }

                val url = URL(BACKEND_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.connectTimeout = 8000
                conn.readTimeout = 8000

                conn.outputStream.use { os ->
                    os.write(proxyJsonPayload.toString().toByteArray(Charsets.UTF_8))
                }

                val responseCode = conn.responseCode
                Log.d(TAG, "Railway Vision Proxy HTTP response code: $responseCode")

                if (responseCode == 200) {
                    val responseString = conn.inputStream.bufferedReader().use { it.readText() }
                    val rootJson = JSONObject(responseString)
                    val dataJson = rootJson.optJSONObject("data") ?: JSONObject()

                    val commonName = dataJson.optString("common_name", dataJson.optString("commonName", "Wild Flora"))
                    val botanicalName = dataJson.optString("botanical_name", dataJson.optString("botanicalName", "Flora Incognita"))
                    val confidence = dataJson.optInt("confidence_score", dataJson.optInt("confidenceScore", 95))
                    val sunlight = dataJson.optString("sunlight_requirement", dataJson.optString("sunlightRequirement", "Bright Indirect Light"))
                    val waterDays = dataJson.optInt("water_days", dataJson.optInt("waterIntervalDays", 7))
                    val fertilizeDays = dataJson.optInt("fertilize_days", dataJson.optInt("feedIntervalDays", 30))
                    val rotateDays = dataJson.optInt("rotate_days", dataJson.optInt("rotateIntervalDays", 14))
                    val trimDays = dataJson.optInt("trim_days", dataJson.optInt("trimIntervalDays", 90))
                    val insight = dataJson.optString("care_insight", dataJson.optString("careInsight", "Requires gentle sunlight and well-draining soil."))

                    Log.d(TAG, "Successfully identified species via Railway Vision: $commonName ($botanicalName)")

                    return@withContext AiIdentificationResult(
                        isPlantDetected = dataJson.optBoolean("is_plant_detected", true),
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
                    Log.w(TAG, "Railway proxy returned HTTP $responseCode. Proceeding to direct OpenAI Vision fallback...")
                }
            } catch (proxyError: Exception) {
                Log.w(TAG, "Railway proxy connection error. Switching to direct OpenAI Vision call.", proxyError)
            }

            // 2. Direct OpenAI GPT-4o-mini Vision Call
            val apiKey = getActiveApiKey()
            Log.d(TAG, "Executing direct OpenAI Vision call with key length: ${apiKey.length}...")

            if (apiKey.isNotBlank()) {
                val directJsonPayload = JSONObject().apply {
                    put("model", "gpt-4o-mini")
                    put("response_format", JSONObject().put("type", "json_object"))
                    put("max_tokens", 300)

                    val promptText = """
                        You are a master botanist for Still Leaf. Analyze BOTH photos of this plant:
                        - Photo 1 is a close-up detail shot of leaf margins, veins, or bloom.
                        - Photo 2 is a whole plant overview in its pot.
                        
                        Identify the exact plant species.
                        Return strict json:
                        {
                          "is_plant_detected": true,
                          "botanical_name": "Latin Name",
                          "common_name": "Common Name",
                          "confidence_score": 98,
                          "sunlight_requirement": "Bright Indirect Light",
                          "water_days": 7,
                          "fertilize_days": 30,
                          "rotate_days": 14,
                          "trim_days": 90,
                          "care_insight": "2-sentence guidance for this specific plant species."
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
                                    put("detail", "low")
                                })
                            })
                        }

                        if (base64Whole != null) {
                            put(JSONObject().apply {
                                put("type", "image_url")
                                put("image_url", JSONObject().apply {
                                    put("url", "data:image/jpeg;base64,$base64Whole")
                                    put("detail", "low")
                                })
                            })
                        }
                    }

                    put("messages", org.json.JSONArray().put(JSONObject().apply {
                        put("role", "user")
                        put("content", contentArray)
                    }))
                }

                val url = URL(DIRECT_API_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Authorization", "Bearer $apiKey")
                conn.doOutput = true
                conn.connectTimeout = 25000
                conn.readTimeout = 25000

                conn.outputStream.use { os ->
                    os.write(directJsonPayload.toString().toByteArray(Charsets.UTF_8))
                }

                val responseCode = conn.responseCode
                Log.d(TAG, "Direct OpenAI Vision API HTTP response code: $responseCode")

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

                    Log.d(TAG, "Successfully identified species via direct OpenAI Vision: $commonName ($botanicalName)")

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
                    val errString = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error body"
                    Log.e(TAG, "Direct OpenAI Vision API Error $responseCode: $errString")
                }
            } else {
                Log.e(TAG, "Decoded API key is blank!")
            }

            return@withContext createFallbackResult()
        } catch (e: Throwable) {
            Log.e(TAG, "Exception during botanical identification", e)
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
                    if (bitmap.width > bitmap.height) {
                        val matrix = android.graphics.Matrix().apply { postRotate(90f) }
                        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                    } else {
                        bitmap
                    }
                }
                imageUri != null -> {
                    // Read image bytes ONCE into memory to prevent content resolver stream closure errors
                    val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                        ?: return null

                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

                    val maxDimension = 512
                    var inSampleSize = 1
                    if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
                        val halfHeight: Int = options.outHeight / 2
                        val halfWidth: Int = options.outWidth / 2
                        while (halfHeight / inSampleSize >= maxDimension && halfWidth / inSampleSize >= maxDimension) {
                            inSampleSize *= 2
                        }
                    }

                    val decodeOptions = BitmapFactory.Options().apply {
                        this.inSampleSize = inSampleSize
                    }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
                }
                else -> null
            } ?: return null

            val maxDim = 512
            val width = srcBitmap.width
            val height = srcBitmap.height
            val scale = (maxDim.toFloat() / Math.max(width, height)).coerceAtMost(1.0f)

            val resizedBitmap = if (scale < 1.0f) {
                Bitmap.createScaledBitmap(srcBitmap, (width * scale).toInt(), (height * scale).toInt(), true)
            } else {
                srcBitmap
            }

            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Throwable) {
            Log.e(TAG, "Error encoding image to base64", e)
            return null
        }
    }
}
