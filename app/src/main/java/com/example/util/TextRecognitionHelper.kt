package com.example.util

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

object TextRecognitionHelper {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognizeText(bitmap: Bitmap): String {
        Log.d("SCANNER_FLOW", "Starting ML Kit OCR text recognition on bitmap (width: ${bitmap.width}, height: ${bitmap.height})")
        val image = InputImage.fromBitmap(bitmap, 0)
        return try {
            val result = recognizer.process(image).await()
            val text = result.text
            Log.d("SCANNER_FLOW", "OCR success. Extracted text length: ${text.length}")
            text
        } catch (e: Exception) {
            Log.e("SCANNER_FLOW", "OCR failure: ${e.javaClass.simpleName} - ${e.message}", e)
            "Error recognizing text: ${e.message}"
        }
    }
}
