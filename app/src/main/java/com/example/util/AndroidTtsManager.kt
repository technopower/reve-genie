package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Android Text-to-Speech manager for AI Teacher explanations.
 * Supports Bangla and English narration with clean markdown sanitization.
 */
object AndroidTtsManager : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speakingMessageId = MutableStateFlow<String?>(null)
    val speakingMessageId: StateFlow<String?> = _speakingMessageId.asStateFlow()

    fun init(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    _speakingMessageId.value = utteranceId
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    _speakingMessageId.value = null
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    _speakingMessageId.value = null
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    _speakingMessageId.value = null
                }
            })
        }
    }

    fun speak(text: String, messageId: String = "ai_msg") {
        val cleanText = sanitizeTextForSpeech(text)
        if (cleanText.isBlank()) return

        val isBangla = containsBangla(cleanText)
        val targetLocale = if (isBangla) Locale("bn", "BD") else Locale.US

        tts?.let { engine ->
            val langResult = engine.setLanguage(targetLocale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to default or English if Bangla voice pack is unavailable
                engine.setLanguage(Locale.US)
            }
            engine.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, messageId)
            _isSpeaking.value = true
            _speakingMessageId.value = messageId
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        _isSpeaking.value = false
        _speakingMessageId.value = null
    }

    fun toggle(text: String, messageId: String) {
        if (_isSpeaking.value && _speakingMessageId.value == messageId) {
            stop()
        } else {
            speak(text, messageId)
        }
    }

    private fun containsBangla(text: String): Boolean {
        return text.any { it in '\u0980'..'\u09FF' }
    }

    fun sanitizeTextForSpeech(text: String): String {
        return text
            .replace(Regex("""\*\*(.*?)\*\*"""), "$1")
            .replace(Regex("""\*(.*?)\*"""), "$1")
            .replace(Regex("""#+\s*"""), "")
            .replace(Regex("""`{1,3}[^`]*`{1,3}"""), "")
            .replace("•", "")
            .replace("—", "-")
            .replace("⇒", "সুতরাং")
            .replace("→", "থেকে")
            .replace(Regex("""[📖🎯⚡💡✨]"""), "")
            .replace(Regex("""[\n\r]+"""), ". ")
            .trim()
    }
}
