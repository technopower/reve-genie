package com.example.data

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

/**
 * Data model for outbound chat requests to OpenAI via Python FastAPI backend.
 */
data class PythonChatRequest(
    val message: String,
    val context: String? = null,
    val language: String? = null,
    val systemPrompt: String? = null
)

/**
 * Data model for inbound chat responses from OpenAI via Python FastAPI backend.
 */
data class PythonChatResponse(
    val success: Boolean,
    val reply: String,
    val error: String? = null
)

/**
 * Data model for outbound image analysis requests to Gemini via Python FastAPI backend.
 */
data class PythonImageAnalysisRequest(
    val image: String,
    val question: String? = null,
    val context: String? = null,
    val language: String? = null
)

/**
 * Data model for inbound image analysis responses from Gemini via Python FastAPI backend.
 */
data class PythonImageAnalysisResponse(
    val success: Boolean,
    val reply: String,
    val error: String? = null
)

/**
 * Data model for outbound translation requests to Google Translation via Python FastAPI backend.
 */
data class PythonTranslationRequest(
    val text: String,
    val sourceLanguage: String? = "auto",
    val targetLanguage: String? = "en"
)

/**
 * Data model for inbound translation responses from Google Translation via Python FastAPI backend.
 */
data class PythonTranslationResponse(
    val success: Boolean,
    val correctedText: String? = null,
    val translatedText: String,
    val error: String? = null
)

/**
 * HTTP client for communicating with the Python FastAPI backend.
 * Uses BackendConfig to dynamically resolve the base URL for Android Emulator or Physical Hardware.
 */
object PythonApiClient {
    private const val TAG = "PYTHON_API_CLIENT"

    val BASE_URL: String get() = BackendConfig.BASE_URL
    val CHAT_ENDPOINT: String get() = "$BASE_URL/chat"
    val ANALYZE_IMAGE_ENDPOINT: String get() = "$BASE_URL/analyze-image"
    val TRANSLATE_ENDPOINT: String get() = "$BASE_URL/translate"
    val HEALTH_ENDPOINT: String get() = "$BASE_URL/health"

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(35, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Primary method to route text academic queries to OpenAI via Python FastAPI backend.
     */
    suspend fun chatWithOpenAI(
        message: String,
        context: String? = null,
        language: String? = null,
        systemPrompt: String? = null
    ): PythonChatResponse = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Translator API Base URL: $BASE_URL | Sending OpenAI chat request to $CHAT_ENDPOINT: '$message'")

            val jsonPayload = JSONObject().apply {
                put("message", message)
                if (!context.isNullOrBlank()) put("context", context)
                if (!language.isNullOrBlank()) put("language", language)
                if (!systemPrompt.isNullOrBlank()) put("system_prompt", systemPrompt)
            }

            val requestBody = jsonPayload.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(CHAT_ENDPOINT)
                .post(requestBody)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                Log.d(TAG, "OpenAI endpoint returned HTTP ${response.code}")

                if (response.isSuccessful && responseBody.isNotBlank()) {
                    val json = JSONObject(responseBody)
                    val success = json.optBoolean("success", true)
                    val reply = json.optString("reply", "")
                    val error = if (json.has("error") && !json.isNull("error")) json.getString("error") else null

                    PythonChatResponse(
                        success = success,
                        reply = reply,
                        error = error
                    )
                } else {
                    val errorMsg = parseErrorMessage(responseBody, "Server returned HTTP ${response.code}")
                    PythonChatResponse(success = false, reply = "", error = errorMsg)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to AI Teacher backend at $CHAT_ENDPOINT (BASE_URL=$BASE_URL)", e)
            val friendlyError = "AI Teacher server is unreachable. Please make sure the phone and computer are on the same network and the backend server is running."
            PythonChatResponse(
                success = false,
                reply = "",
                error = friendlyError
            )
        }
    }

    /**
     * Backward-compatible alias for chatWithOpenAI.
     */
    suspend fun sendChatMessage(
        message: String,
        context: String? = null,
        language: String? = null,
        systemPrompt: String? = null
    ): PythonChatResponse = chatWithOpenAI(message, context, language, systemPrompt)

    suspend fun sendChatMessage(request: PythonChatRequest): PythonChatResponse {
        return chatWithOpenAI(
            message = request.message,
            context = request.context,
            language = request.language,
            systemPrompt = request.systemPrompt
        )
    }

    /**
     * Multimodal method to route camera/image questions to Google Gemini via Python FastAPI backend.
     */
    suspend fun analyzeImageWithGemini(
        imageBase64: String,
        question: String? = null,
        context: String? = null,
        language: String? = null
    ): PythonImageAnalysisResponse = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Translator API Base URL: $BASE_URL | Sending Gemini image analysis request to $ANALYZE_IMAGE_ENDPOINT")

            val jsonPayload = JSONObject().apply {
                put("image", imageBase64)
                if (!question.isNullOrBlank()) put("question", question)
                if (!context.isNullOrBlank()) put("context", context)
                if (!language.isNullOrBlank()) put("language", language)
            }

            val requestBody = jsonPayload.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(ANALYZE_IMAGE_ENDPOINT)
                .post(requestBody)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                Log.d(TAG, "Gemini endpoint returned HTTP ${response.code}")

                if (response.isSuccessful && responseBody.isNotBlank()) {
                    val json = JSONObject(responseBody)
                    val success = json.optBoolean("success", true)
                    val reply = json.optString("reply", "")
                    val error = if (json.has("error") && !json.isNull("error")) json.getString("error") else null

                    PythonImageAnalysisResponse(
                        success = success,
                        reply = reply,
                        error = error
                    )
                } else {
                    val errorMsg = parseErrorMessage(responseBody, "Gemini server returned HTTP ${response.code}")
                    PythonImageAnalysisResponse(success = false, reply = "", error = errorMsg)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to Gemini endpoint at $ANALYZE_IMAGE_ENDPOINT (BASE_URL=$BASE_URL)", e)
            val friendlyError = "Gemini server is unreachable. Please make sure the phone and computer are on the same network and the backend server is running."
            PythonImageAnalysisResponse(
                success = false,
                reply = "",
                error = friendlyError
            )
        }
    }

    /**
     * Convenience method to convert a Bitmap into base64 and analyze with Gemini.
     */
    suspend fun analyzeImageWithGemini(
        bitmap: Bitmap,
        question: String? = null,
        context: String? = null,
        language: String? = null
    ): PythonImageAnalysisResponse {
        val base64String = withContext(Dispatchers.Default) {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        }
        return analyzeImageWithGemini(
            imageBase64 = base64String,
            question = question,
            context = context,
            language = language
        )
    }

    /**
     * Dual-stage translation method routing to Python FastAPI backend (Correction + Translation).
     */
    suspend fun translateText(
        text: String,
        sourceLanguage: String? = "auto",
        targetLanguage: String? = "en"
    ): PythonTranslationResponse = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Translator API Base URL: $BASE_URL | Sending translation request to $TRANSLATE_ENDPOINT: '$text'")

            val jsonPayload = JSONObject().apply {
                put("text", text)
                put("source_language", sourceLanguage ?: "auto")
                put("target_language", targetLanguage ?: "en")
            }

            val requestBody = jsonPayload.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(TRANSLATE_ENDPOINT)
                .post(requestBody)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                Log.d(TAG, "Translation endpoint returned HTTP ${response.code}")

                if (response.isSuccessful && responseBody.isNotBlank()) {
                    val json = JSONObject(responseBody)
                    val success = json.optBoolean("success", true)
                    val corrected = if (json.has("corrected_text") && !json.isNull("corrected_text")) json.getString("corrected_text") else null
                    val translated = json.optString("translated_text", "")
                    val error = if (json.has("error") && !json.isNull("error")) json.getString("error") else null

                    PythonTranslationResponse(
                        success = success,
                        correctedText = corrected,
                        translatedText = translated,
                        error = error
                    )
                } else {
                    val errorMsg = parseErrorMessage(responseBody, "Translation server returned HTTP ${response.code}")
                    PythonTranslationResponse(success = false, correctedText = null, translatedText = "", error = errorMsg)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to Translation endpoint at $TRANSLATE_ENDPOINT (BASE_URL=$BASE_URL)", e)
            val friendlyError = "Translation server is unreachable. Please make sure the phone and computer are on the same network and the backend server is running."
            PythonTranslationResponse(
                success = false,
                correctedText = null,
                translatedText = "",
                error = friendlyError
            )
        }
    }

    /**
     * Health check endpoint to verify Python FastAPI backend status.
     */
    suspend fun checkHealth(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(HEALTH_ENDPOINT)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.w(TAG, "Health check failed for $HEALTH_ENDPOINT: ${e.message}")
            false
        }
    }

    private fun parseErrorMessage(responseBody: String, defaultMsg: String): String {
        return try {
            val json = JSONObject(responseBody)
            json.optString("error", defaultMsg)
        } catch (_: Exception) {
            defaultMsg
        }
    }
}
