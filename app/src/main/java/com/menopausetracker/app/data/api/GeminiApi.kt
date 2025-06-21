package com.menopausetracker.app.data.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Retrofit API interface for Google Gemini API
 */
interface GeminiApi {
    @Headers("Content-Type: application/json")
    @POST("v1/models/gemini-2.0-flash:generateContent")
    fun generateContent(
        @Body request: GeminiRequest
    ): Call<GeminiResponse>
}

/**
 * Request format for Gemini API
 */
data class GeminiRequest(
    val contents: List<ContentItem>
) {
    companion object {
        fun createForText(text: String): GeminiRequest {
            return GeminiRequest(
                contents = listOf(
                    ContentItem(parts = listOf(Part(text = text)))
                )
            )
        }
    }
}

data class ContentItem(
    val parts: List<Part>
)

data class Part(
    val text: String? = null
)

/**
 * Response format for Gemini API
 */
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val promptFeedback: PromptFeedback? = null
)

data class Candidate(
    val content: ContentItem? = null,
    val finishReason: String? = null,
    val index: Int? = null
)

data class PromptFeedback(
    val blockReason: String? = null
)
