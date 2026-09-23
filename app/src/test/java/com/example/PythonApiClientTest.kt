package com.example

import com.example.data.PythonApiClient
import com.example.data.PythonChatRequest
import com.example.data.PythonChatResponse
import org.junit.Assert.*
import org.junit.Test

class PythonApiClientTest {

    @Test
    fun testEndpoints() {
        val baseUrl = PythonApiClient.BASE_URL
        assertTrue(baseUrl.startsWith("http://"))
        assertEquals("$baseUrl/chat", PythonApiClient.CHAT_ENDPOINT)
        assertEquals("$baseUrl/health", PythonApiClient.HEALTH_ENDPOINT)
    }

    @Test
    fun testChatRequestDataClass() {
        val request = PythonChatRequest(
            message = "Explain photosynthesis in Bengali",
            context = "SSC Biology, Chapter 4",
            language = "Bengali",
            systemPrompt = "Custom tutor prompt"
        )

        assertEquals("Explain photosynthesis in Bengali", request.message)
        assertEquals("SSC Biology, Chapter 4", request.context)
        assertEquals("Bengali", request.language)
        assertEquals("Custom tutor prompt", request.systemPrompt)
    }

    @Test
    fun testChatResponseDataClass() {
        val successResponse = PythonChatResponse(
            success = true,
            reply = "Photosynthesis is the process...",
            error = null
        )
        assertTrue(successResponse.success)
        assertEquals("Photosynthesis is the process...", successResponse.reply)
        assertNull(successResponse.error)

        val errorResponse = PythonChatResponse(
            success = false,
            reply = "",
            error = "AI service temporarily unavailable"
        )
        assertFalse(errorResponse.success)
        assertEquals("", errorResponse.reply)
        assertEquals("AI service temporarily unavailable", errorResponse.error)
    }
}
