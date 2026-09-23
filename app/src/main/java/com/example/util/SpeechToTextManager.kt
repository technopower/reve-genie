package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SpeechToTextManager(private val context: Context) {
    private val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("VOICE_FLOW", "onReadyForSpeech")
                _isListening.value = true
                _error.value = null
            }
            override fun onBeginningOfSpeech() {
                Log.d("VOICE_FLOW", "onBeginningOfSpeech")
            }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                Log.d("VOICE_FLOW", "onEndOfSpeech")
                _isListening.value = false
            }
            override fun onError(error: Int) {
                Log.e("VOICE_FLOW", "SpeechRecognizer error code: $error")
                _isListening.value = false
                _error.value = "Speech recognition error: $error"
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    Log.d("VOICE_FLOW", "onResults recognized: ${matches[0]}")
                    _text.value = matches[0]
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    Log.d("VOICE_FLOW", "onPartialResults: ${matches[0]}")
                    _text.value = matches[0]
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    fun startListening(language: String = "bn-BD") {
        Log.d("VOICE_FLOW", "startListening with language: $language")
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        try {
            speechRecognizer.startListening(intent)
        } catch (e: Exception) {
            Log.e("VOICE_FLOW", "Failed to start listening", e)
            _error.value = "Failed to start mic: ${e.message}"
        }
    }

    fun stopListening() {
        Log.d("VOICE_FLOW", "stopListening")
        try {
            speechRecognizer.stopListening()
        } catch (e: Exception) {
            Log.e("VOICE_FLOW", "Error stopping listening", e)
        }
        _isListening.value = false
    }

    fun destroy() {
        Log.d("VOICE_FLOW", "destroy SpeechRecognizer")
        try {
            speechRecognizer.destroy()
        } catch (e: Exception) {
            Log.e("VOICE_FLOW", "Error destroying speech recognizer", e)
        }
    }
}
