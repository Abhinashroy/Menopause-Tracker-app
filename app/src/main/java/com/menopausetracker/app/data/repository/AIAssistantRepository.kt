package com.menopausetracker.app.data.repository

import android.util.Log
import com.menopausetracker.app.data.model.Suggestion
import com.menopausetracker.app.data.model.Symptom
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID
import java.io.IOException
import kotlin.system.measureTimeMillis

/**
 * Repository for generating health suggestions using the Gemini model.
 */
class AIAssistantRepository {
    companion object {
        private const val TAG = "AIAssistantRepo"
        private const val API_TIMEOUT_MS = 10000L // 10 seconds
        private const val MODEL_NAME = "gemini-2.0-flash"
        private const val API_KEY = "AIzaSyD7UzsETcBH411hr13elPTa5aH0KF5yrkI" // Replace with actual API key
    }

    // In-memory cache for suggestions
    private var cachedSuggestions: List<Suggestion> = emptyList()

    // Fallback responses for common symptoms
    private val fallbackSuggestions = mapOf(
        "Hot Flashes" to "For hot flashes, try wearing layered clothing that can be easily removed. Keep a portable fan nearby and stay hydrated. Identify and avoid triggers like spicy foods, alcohol, and caffeine. Deep breathing exercises when a hot flash begins may help reduce its intensity.",
        "Night Sweats" to "To manage night sweats, use moisture-wicking bedding and sleepwear. Keep your bedroom cool (around 65°F/18°C) and avoid triggers before bedtime. Consider a cooling mattress pad or pillow. Keep water nearby to stay hydrated throughout the night.",
        "Sleep Issues" to "To improve sleep quality, maintain a consistent sleep schedule, even on weekends. Create a restful environment by keeping your bedroom dark, quiet, and cool. Avoid screens at least an hour before bedtime. Consider relaxation techniques like gentle stretching or reading before sleep.",
        "Mood Changes" to "For mood swings, practice mindfulness techniques such as meditation or deep breathing. Regular exercise can help stabilize mood. Consider keeping a mood journal to identify triggers. Ensure you're getting adequate sleep, as fatigue can worsen mood fluctuations.",
        "Fatigue" to "To combat fatigue, prioritize activities based on your energy levels throughout the day. Incorporate short rest periods into your schedule. Stay hydrated and maintain a balanced diet rich in iron and B vitamins. Regular, moderate exercise can actually boost energy levels despite initial effort.",
        "Breast Tenderness" to "For breast tenderness, wear a supportive bra, possibly even during sleep. Apply cool compresses for comfort. Limit salt, caffeine, and alcohol intake. Consider evening primrose oil supplements after consulting your healthcare provider."
    )

    // Topic fallback responses
    private val topicFallbackResponses = mapOf(
        "general" to "• Menopause is a natural transition marking the end of reproductive years\n• Most women experience menopause between 45-55 years of age\n• Symptoms can vary widely between individuals\n• Consider consulting with a healthcare provider for personalized advice",
        "diet" to "• Stay hydrated and maintain a balanced diet\n• Foods rich in calcium and vitamin D support bone health\n• Consider limiting caffeine, alcohol, and spicy foods which can trigger hot flashes\n• Incorporate whole grains, fruits, vegetables, and lean proteins",
        "exercise" to "• Regular physical activity helps manage symptoms and improve mood\n• Weight-bearing exercises support bone health\n• Aim for at least 150 minutes of moderate exercise weekly\n• Activities like walking, swimming, and yoga are particularly beneficial",
        "sleep" to "• Maintain a consistent sleep schedule\n• Keep your bedroom cool, dark, and quiet\n• Avoid screens at least an hour before bedtime\n• Consider relaxation techniques like deep breathing or gentle stretching before sleep"
    )

    /**
     * Generate a suggestion based on user prompt and recent symptoms.
     * Returns a Result containing either a Suggestion or an Exception.
     */
    suspend fun generateSuggestion(prompt: String, recentSymptoms: List<Symptom>): Result<Suggestion> {
        return try {
            var resultSuggestion: Suggestion? = null

            val elapsedTime = measureTimeMillis {
                resultSuggestion = withTimeoutOrNull(API_TIMEOUT_MS) {
                    generateWithAI(prompt, recentSymptoms)
                }

                // If AI generation failed or timed out, use fallback
                if (resultSuggestion == null) {
                    resultSuggestion = generateFallbackSuggestion(prompt, recentSymptoms)
                }
            }

            Log.d(TAG, "Suggestion generated in $elapsedTime ms")

            // Return the result outside of measureTimeMillis
            if (resultSuggestion != null) {
                Result.success(resultSuggestion!!)
            } else {
                Result.failure(IOException("Failed to generate suggestion"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating suggestion", e)
            Result.failure(e)
        }
    }

    /**
     * Attempt to generate response using Gemini API
     */
    private suspend fun generateWithAI(prompt: String, recentSymptoms: List<Symptom>): Suggestion? {
        try {
            // Configure safety settings
            val safetySettings = listOf(
                SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.ONLY_HIGH),
                SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.ONLY_HIGH),
                SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.ONLY_HIGH),
                SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.ONLY_HIGH)
            )

            // Create the generative model
            val generativeModel = GenerativeModel(
                modelName = "gemini-2.0-flash", // Using the constant defined in companion object
                apiKey = "AIzaSyD7UzsETcBH411hr13elPTa5aH0KF5yrkI",       // Using the constant defined in companion object
                safetySettings = safetySettings
            )

            // Add special instructions to preserve newlines
            val contextualPrompt = buildContextualPrompt(prompt, recentSymptoms)

            // Generate content
            val response = generativeModel.generateContent(contextualPrompt)
            var responseText = response.text?.trim() ?: throw IOException("Empty response from AI model")

            // Clean the response text to remove markdown formatting but preserve newlines
            responseText = cleanResponseText(responseText)

            // Create and return suggestion
            return createSuggestion(prompt, responseText)
        } catch (e: Exception) {
            Log.e(TAG, "Error in AI generation", e)
            return null
        }
    }

    /**
     * Clean response text by removing markdown symbols and formatting
     * while carefully preserving line breaks
     */
    private fun cleanResponseText(text: String): String {
        // First, replace all Windows-style line breaks with Unix-style
        var cleaned = text.replace("\r\n", "\n")

        // Next, normalize any double line breaks to ensure consistent handling
        cleaned = cleaned.replace(Regex("\n{3,}"), "\n\n")

        // Now process markdown elements while preserving line structure
        cleaned = cleaned
            // Replace markdown headers with plain text
            .replace(Regex("^#{1,6}\\s+(.+)$", RegexOption.MULTILINE), "$1")
            // Replace markdown bold with plain text
            .replace(Regex("\\*\\*([^*]+?)\\*\\*"), "$1")
            .replace(Regex("__([^_]+?)__"), "$1")
            // Replace markdown italic with plain text
            .replace(Regex("\\*([^*]+?)\\*"), "$1")
            .replace(Regex("_([^_]+?)_"), "$1")
            // Replace markdown bullet points with regular bullet points
            .replace(Regex("^\\s*[\\*\\-]\\s+(.+)$", RegexOption.MULTILINE), "• $1")
            // Replace markdown lists with simple bullet points
            .replace(Regex("^\\s*\\d+\\.\\s+(.+)$", RegexOption.MULTILINE), "• $1")
            // Clean up any other markdown artifacts
            .replace(Regex("`([^`]+)`"), "$1")

        // Clean up any remaining markdown symbols
        cleaned = cleaned
            .replace("**", "")
            .replace("*", "")
            .replace("__", "")
            .replace("_", "")
            // Remove links but keep link text
            .replace(Regex("\\[([^\\]]+)\\]\\([^)]+\\)"), "$1")

        // Clean up whitespace but preserve line breaks
        cleaned = cleaned
            // Replace multiple spaces with a single space
            .replace(Regex("[ \\t]{2,}"), " ")
            .trim()

        return cleaned
    }

    /**
     * Build a contextual prompt based on user input and recent symptoms
     */
    private fun buildContextualPrompt(prompt: String, recentSymptoms: List<Symptom>): String {
        val basePrompt = StringBuilder(
            "You are a helpful health assistant providing evidence-based information about menopause. " +
            "Provide concise, practical suggestions. Focus on lifestyle adjustments, self-care strategies, " +
            "and general information. Do not diagnose conditions or prescribe medications. " +
            "Keep responses under 500 characters when possible. DO NOT use markdown formatting, asterisks, or any special formatting. " +
            "Use simple text formatting with clear line breaks between paragraphs and bullet points starting with • character. " +
            "Preserve all line breaks in your response exactly as intended for display."
        )

        // Add symptom context if available
        if (recentSymptoms.isNotEmpty()) {
            basePrompt.append("\n\nRecent symptoms reported by the user:")
            recentSymptoms.forEach { symptom ->
                val symptomList = mutableListOf<String>()
                if (symptom.hotFlashes) symptomList.add("Hot Flashes")
                if (symptom.nightSweats) symptomList.add("Night Sweats")
                if (symptom.sleepIssues) symptomList.add("Sleep Issues")
                if (symptom.moodChanges) symptomList.add("Mood Changes")
                if (symptom.fatigue) symptomList.add("Fatigue")
                if (symptom.otherSymptoms.isNotEmpty()) symptomList.add(symptom.otherSymptoms)

                basePrompt.append("\n- Symptoms on ${symptom.date}: ${symptomList.joinToString(", ")}")
                basePrompt.append(" (Severity: ${symptom.severity}/10)")
                if (symptom.description.isNotEmpty()) {
                    basePrompt.append(". Notes: ${symptom.description}")
                }
            }
        }

        // Add user query
        basePrompt.append("\n\nUser query: ")
        basePrompt.append(if (prompt.isNotEmpty()) prompt else "Please provide general advice based on my recent symptoms.")

        return basePrompt.toString()
    }

    /**
     * Generate a fallback suggestion when AI is unavailable
     */
    private fun generateFallbackSuggestion(prompt: String, recentSymptoms: List<Symptom>): Suggestion {
        // Generate title based on prompt or symptoms
        val title = when {
            prompt.isNotEmpty() -> "Response to: ${prompt.take(50)}${if (prompt.length > 50) "..." else ""}"
            recentSymptoms.isNotEmpty() -> "Advice based on recent symptoms"
            else -> "General menopause information"
        }

        // Generate content based on symptoms or prompt
        val content = when {
            // If we have matching symptoms, use symptom-specific advice
            recentSymptoms.isNotEmpty() -> {
                val symptomAdvice = StringBuilder()

                // Check for specific symptoms
                recentSymptoms.forEach { symptom ->
                    if (symptom.hotFlashes) symptomAdvice.append("${fallbackSuggestions["Hot Flashes"]}\n\n")
                    if (symptom.nightSweats) symptomAdvice.append("${fallbackSuggestions["Night Sweats"]}\n\n")
                    if (symptom.sleepIssues) symptomAdvice.append("${fallbackSuggestions["Sleep Issues"]}\n\n")
                    if (symptom.moodChanges) symptomAdvice.append("${fallbackSuggestions["Mood Changes"]}\n\n")
                    if (symptom.fatigue) symptomAdvice.append("${fallbackSuggestions["Fatigue"]}\n\n")
                }

                // If no specific advice was found, use general advice
                if (symptomAdvice.isEmpty()) {
                    topicFallbackResponses["general"] ?: "Consider tracking your symptoms and discussing them with your healthcare provider."
                } else {
                    symptomAdvice.toString().trim()
                }
            }

            // If we have a prompt, try to match it with a topic
            prompt.isNotEmpty() -> {
                val lowerPrompt = prompt.lowercase()
                when {
                    lowerPrompt.contains("diet") || lowerPrompt.contains("food") || lowerPrompt.contains("eat") ->
                        topicFallbackResponses["diet"] ?: ""
                    lowerPrompt.contains("exercise") || lowerPrompt.contains("workout") || lowerPrompt.contains("activity") ->
                        topicFallbackResponses["exercise"] ?: ""
                    lowerPrompt.contains("sleep") || lowerPrompt.contains("insomnia") || lowerPrompt.contains("night") ->
                        topicFallbackResponses["sleep"] ?: ""
                    else -> topicFallbackResponses["general"] ?: ""
                }
            }

            // Default fallback
            else -> topicFallbackResponses["general"] ?:
                "Menopause is a natural transition. Consider discussing your symptoms with your healthcare provider."
        }

        return Suggestion(
            id = UUID.randomUUID().toString(),
            title = title,
            content = content,
            prompt = prompt,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * Create a suggestion with a generated title
     */
    private fun createSuggestion(prompt: String, content: String): Suggestion {
        // Create a title from the first few words of content or prompt
        val title = when {
            content.isNotEmpty() -> {
                val firstLine = content.substringBefore('\n').trim()
                if (firstLine.length > 50) "${firstLine.substring(0, 47)}..." else firstLine
            }
            prompt.isNotEmpty() -> {
                if (prompt.length > 50) "Response to: ${prompt.substring(0, 47)}..." else "Response to: $prompt"
            }
            else -> "Menopause Information"
        }

        return Suggestion(
            id = UUID.randomUUID().toString(),
            title = title,
            content = content,
            prompt = prompt,
            timestamp = System.currentTimeMillis()
        )
    }
}
