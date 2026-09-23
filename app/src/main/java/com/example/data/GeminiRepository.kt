package com.example.data

import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Data model for dual-stage translation result containing original text, corrected source sentence, and final translation.
 */
data class TranslationResult(
    val originalText: String,
    val correctedText: String,
    val translatedText: String,
    val success: Boolean,
    val error: String? = null
)

/**
 * Repository handling multi-provider AI and Translation requests:
 * 1. OpenAI (via PythonApiClient.chatWithOpenAI) for text academic questions & reasoning.
 * 2. Google Gemini (via PythonApiClient.analyzeImageWithGemini) for multimodal camera & image analysis.
 * 3. Google Translation + Correction (via PythonApiClient.translateText) for fast dual-stage translation.
 */
object GeminiRepository {
    private const val TAG = "AI_ROUTING_REPOSITORY"

    init {
        Log.d(TAG, "GeminiRepository initialized with multi-provider routing (OpenAI, Gemini, Google Translation)")
    }

    /**
     * Dual-stage translation pipeline:
     * 1. Detect source language & correct the source sentence naturally (grammar, spelling, capitalization, missing prepositions/articles).
     * 2. Translate the corrected sentence into the target language.
     * 3. Return both corrected sentence and final translation.
     */
    suspend fun translateWithCorrection(
        text: String,
        sourceLang: String,
        targetLang: String
    ): TranslationResult = withContext(Dispatchers.IO) {
        Log.d("TRANSLATION_FLOW", "Dual-stage translation start: $sourceLang -> $targetLang. Input: '$text'")
        try {
            val response = PythonApiClient.translateText(
                text = text,
                sourceLanguage = sourceLang,
                targetLanguage = targetLang
            )
            if (response.success && response.translatedText.isNotBlank()) {
                val corrected = response.correctedText?.takeIf { it.isNotBlank() } ?: text
                Log.d("TRANSLATION_FLOW", "Translation success. Corrected: '$corrected', Translated: '${response.translatedText}'")
                TranslationResult(
                    originalText = text,
                    correctedText = corrected,
                    translatedText = response.translatedText,
                    success = true
                )
            } else {
                val err = response.error ?: "Translation failed"
                Log.e("TRANSLATION_FLOW", "Translation failure: $err")
                TranslationResult(
                    originalText = text,
                    correctedText = text,
                    translatedText = "Error: $err",
                    success = false,
                    error = err
                )
            }
        } catch (e: Exception) {
            Log.e("TRANSLATION_FLOW", "Translation exception: ${e.javaClass.simpleName} - ${e.message}", e)
            TranslationResult(
                originalText = text,
                correctedText = text,
                translatedText = "Error: ${e.message}",
                success = false,
                error = e.message
            )
        }
    }

    /**
     * Backward-compatible translate signature.
     */
    suspend fun translate(text: String, sourceLang: String, targetLang: String): String {
        return translateWithCorrection(text, sourceLang, targetLang).translatedText
    }

    /**
     * Routes text academic questions to OpenAI as the primary AI Teacher.
     */
    suspend fun getAiTeacherResponse(query: String): String = withContext(Dispatchers.IO) {
        Log.d("VOICE_FLOW", "AI Teacher request start (OpenAI). Query: $query")
        try {
            val response = PythonApiClient.chatWithOpenAI(
                message = query,
                context = "Academic Voice AI Teacher Query (NCTB & International Curriculum)",
                language = "Bengali/English"
            )
            if (response.success && response.reply.isNotBlank()) {
                Log.d("VOICE_FLOW", "AI Teacher request success. Response length: ${response.reply.length}")
                response.reply
            } else {
                val err = response.error ?: "I couldn't understand that. Please try again."
                Log.e("VOICE_FLOW", "AI Teacher request failure: $err")
                "Error: $err"
            }
        } catch (e: Exception) {
            Log.e("VOICE_FLOW", "AI Teacher request exception: ${e.javaClass.simpleName} - ${e.message}", e)
            "Error: ${e.message}"
        }
    }

    /**
     * Routes extracted OCR text questions to OpenAI for step-by-step academic problem solving.
     */
    suspend fun solveQuestionFromText(text: String): String = withContext(Dispatchers.IO) {
        val prompt = "Solve this educational question step-by-step. Use a clear mix of Bangla and English if appropriate. Provide formulas, intermediate steps, and the final answer clearly.\n\nQuestion: $text"
        Log.d("SCANNER_FLOW", "Scanner AI solve request start (OpenAI). Text length: ${text.length}")
        try {
            val response = PythonApiClient.chatWithOpenAI(
                message = prompt,
                context = "Scanner OCR Question Solver",
                language = "Bengali/English"
            )
            if (response.success && response.reply.isNotBlank()) {
                Log.d("SCANNER_FLOW", "Scanner AI solve request success. Result length: ${response.reply.length}")
                response.reply
            } else {
                val err = response.error ?: "I couldn't solve this question."
                Log.e("SCANNER_FLOW", "Scanner AI solve request failure: $err")
                "Error: $err"
            }
        } catch (e: Exception) {
            Log.e("SCANNER_FLOW", "Scanner AI solve request exception: ${e.javaClass.simpleName} - ${e.message}", e)
            "Error: ${e.message}"
        }
    }

    /**
     * Routes camera / textbook images to Google Gemini for multimodal image analysis and visual problem solving.
     */
    suspend fun solveQuestionFromImage(bitmap: Bitmap, question: String = ""): String = withContext(Dispatchers.IO) {
        Log.d("SCANNER_FLOW", "Multimodal Image analysis start (Gemini). Bitmap: ${bitmap.width}x${bitmap.height}")
        try {
            val response = PythonApiClient.analyzeImageWithGemini(
                bitmap = bitmap,
                question = question.ifBlank { "Solve this question or analyze this textbook image step-by-step." },
                context = "Multimodal Camera / Gallery Scanner Solver",
                language = "Bengali/English"
            )
            if (response.success && response.reply.isNotBlank()) {
                Log.d("SCANNER_FLOW", "Gemini image analysis success. Reply length: ${response.reply.length}")
                response.reply
            } else {
                val err = response.error ?: "Gemini image analysis failed."
                Log.e("SCANNER_FLOW", "Gemini image analysis failure: $err")
                "Error: $err"
            }
        } catch (e: Exception) {
            Log.e("SCANNER_FLOW", "Gemini image analysis exception: ${e.javaClass.simpleName} - ${e.message}", e)
            "Error: ${e.message}"
        }
    }
}
