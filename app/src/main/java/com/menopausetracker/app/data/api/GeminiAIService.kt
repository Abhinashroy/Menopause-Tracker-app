package com.menopausetracker.app.data.api

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

/**
 * Service class for interacting with Google's Gemini AI API using Retrofit
 */
class GeminiAIService(private val apiKey: String) {

    // Create OkHttpClient with API key interceptor
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(ApiKeyInterceptor(apiKey))
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    // Initialize Retrofit with the Gemini API base URL
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Create the API interface
    private val geminiApi = retrofit.create(GeminiApi::class.java)

    /**
     * Get personalized recommendations for menopause symptoms
     * @param symptoms Description of symptoms the user is experiencing
     * @return AI-generated recommendations as String
     */
    suspend fun getRecommendations(symptoms: String): String {
        val prompt = buildPrompt(symptoms)
        val request = GeminiRequest.createForText(prompt)

        try {
            // Make the API call
            val response = geminiApi.generateContent(request).execute()

            if (response.isSuccessful && response.body() != null) {
                return extractResponseText(response.body()!!)
            } else {
                return "Error getting recommendations: ${response.errorBody()?.string() ?: "Unknown error"}"
            }
        } catch (e: Exception) {
            return handleError(e)
        }
    }

    /**
     * Build a prompt with specific instructions for the AI to generate helpful,
     * responsible recommendations for menopause symptoms
     */
    private fun buildPrompt(symptoms: String): String {
        return """
            You are a helpful assistant providing information about menopause symptoms and potential relief options.
            
            Important guidelines:
            - Provide evidence-based suggestions only
            - Never suggest medical treatments or diagnose conditions
            - Focus on lifestyle changes, relaxation techniques, and general wellness tips
            - Keep responses concise and organized in bullet points
            - Always include a disclaimer about consulting healthcare providers
            
            User symptoms: $symptoms
            
            Please provide supportive, educational information that might help with these symptoms:
        """.trimIndent()
    }

    /**
     * Extract the generated text from the API response
     */
    private fun extractResponseText(response: GeminiResponse): String {
        // Get the text from the first candidate's content, if available
        val textFromCandidates = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

        return textFromCandidates ?: "Sorry, no recommendations could be generated at this time. Please try again later."
    }

    /**
     * Handle errors from the API and return appropriate messages
     */
    private fun handleError(exception: Exception): String {
        return when (exception) {
            is IOException -> "Network error. Please check your internet connection and try again."
            else -> "Sorry, an error occurred while generating recommendations. Please try again later."
        }
    }

    /**
     * Interceptor to add API key to all requests
     */
    private class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            val originalHttpUrl = original.url

            val url: HttpUrl = originalHttpUrl.newBuilder()
                .addQueryParameter("key", apiKey)
                .build()

            val requestBuilder = original.newBuilder()
                .url(url)

            val request = requestBuilder.build()
            return chain.proceed(request)
        }
    }
}
