package com.menopausetracker.app.data.repository

import com.menopausetracker.app.data.model.Suggestion
import com.menopausetracker.app.data.model.Symptom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Repository for handling AI health suggestions based on user prompts and logged symptoms.
 */
class AIAssistantRepository {

    /**
     * Generates a suggestion based on user input and up to 2 most recent symptoms
     * @param prompt User's question or prompt
     * @param recentSymptoms List of user's recently logged symptoms (up to 2)
     * @return Result containing a suggestion or failure
     */
    suspend fun generateSuggestion(prompt: String, recentSymptoms: List<Symptom> = emptyList()): Result<Suggestion> = withContext(Dispatchers.IO) {
        try {
            // Check if there's a valid prompt or symptoms
            if (prompt.isBlank() && recentSymptoms.isEmpty()) {
                return@withContext Result.failure(Exception("Please enter a question or log symptoms for personalized advice."))
            }

            // Generate personalized content based on prompt and symptoms (up to 2)
            val suggestion = Suggestion(
                id = UUID.randomUUID().toString(),
                title = getGeneratedTitle(prompt, recentSymptoms),
                content = getGeneratedContent(prompt, recentSymptoms.take(2)),
                prompt = prompt // Store the user's original prompt
            )

            Result.success(suggestion)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Original method to maintain backward compatibility
    suspend fun generateSuggestion(prompt: String): Result<Suggestion> {
        return generateSuggestion(prompt, emptyList())
    }

    // Helper functions to generate contextual titles based on prompt and symptoms
    private fun getGeneratedTitle(prompt: String, symptoms: List<Symptom>): String {
        // If no prompt but has symptoms, generate title based on symptoms
        if (prompt.isBlank() && symptoms.isNotEmpty()) {
            return when {
                symptoms.any { it.hotFlashes } -> "Managing Hot Flashes"
                symptoms.any { it.nightSweats } -> "Dealing with Night Sweats"
                symptoms.any { it.moodChanges } -> "Mood Management Tips"
                symptoms.any { it.sleepIssues } -> "Improving Sleep Quality"
                symptoms.any { it.fatigue } -> "Combating Fatigue"
                else -> "Health Suggestion"
            }
        }

        // Otherwise use prompt for title generation
        return when {
            prompt.contains("hot flash", ignoreCase = true) -> "Managing Hot Flashes"
            prompt.contains("sweat", ignoreCase = true) -> "Dealing with Night Sweats"
            prompt.contains("mood", ignoreCase = true) -> "Mood Management Tips"
            prompt.contains("sleep", ignoreCase = true) -> "Improving Sleep Quality"
            prompt.contains("tired", ignoreCase = true) || prompt.contains("fatigue", ignoreCase = true) -> "Combating Fatigue"
            else -> "Health Advice"
        }
    }

    // Helper function to generate content based on prompt and logged symptoms
    private fun getGeneratedContent(prompt: String, symptoms: List<Symptom>): String {
        val relevantSymptoms = mutableListOf<String>()

        // Extract relevant symptoms
        symptoms.forEach { symptom ->
            when {
                symptom.hotFlashes -> relevantSymptoms.add("hot flashes")
                symptom.nightSweats -> relevantSymptoms.add("night sweats")
                symptom.moodChanges -> relevantSymptoms.add("mood changes")
                symptom.sleepIssues -> relevantSymptoms.add("sleep issues")
                symptom.fatigue -> relevantSymptoms.add("fatigue")
                symptom.otherSymptoms.isNotBlank() -> relevantSymptoms.add(symptom.otherSymptoms)
            }
        }

        // Create personalized response based on prompt and symptoms
        val responseBuilder = StringBuilder()

        when {
            // If both prompt and symptoms are present
            prompt.isNotBlank() && relevantSymptoms.isNotEmpty() -> {
                responseBuilder.append("Based on your question and recently logged symptoms (${relevantSymptoms.joinToString(", ")}), here's my advice:\n\n")

                // Add advice for each relevant symptom
                relevantSymptoms.distinct().forEach { symptom ->
                    responseBuilder.append(getAdviceForSymptom(symptom))
                }

                // Add advice for the prompt
                responseBuilder.append("\nRegarding your specific question: ")
                responseBuilder.append(getAdviceForPrompt(prompt))
            }

            // If only symptoms are present (no prompt)
            prompt.isBlank() && relevantSymptoms.isNotEmpty() -> {
                responseBuilder.append("Based on your recently logged symptoms (${relevantSymptoms.joinToString(", ")}), here's my advice:\n\n")

                // Add advice for each relevant symptom
                relevantSymptoms.distinct().forEach { symptom ->
                    responseBuilder.append(getAdviceForSymptom(symptom))
                }
            }

            // If only prompt is present (no symptoms)
            else -> {
                responseBuilder.append(getAdviceForPrompt(prompt))
            }
        }

        return responseBuilder.toString()
    }

    private fun getAdviceForSymptom(symptom: String): String {
        return when {
            symptom.contains("hot flash", ignoreCase = true) ->
                "• For hot flashes: Try wearing lightweight, breathable clothing and keep your environment cool. " +
                "Regular exercise and limiting spicy foods, caffeine, and alcohol may also help reduce hot flashes.\n\n"

            symptom.contains("night sweat", ignoreCase = true) ->
                "• For night sweats: Use moisture-wicking sleepwear and bedding. Keep your bedroom cool and well-ventilated. " +
                "Try to maintain a consistent sleep schedule and avoid triggers like alcohol or caffeine before bed.\n\n"

            symptom.contains("mood", ignoreCase = true) ->
                "• For mood changes: Regular physical activity can help improve mood. Consider practicing mindfulness or yoga. " +
                "Maintain social connections and don't hesitate to speak with a healthcare provider if mood changes are affecting your quality of life.\n\n"

            symptom.contains("sleep", ignoreCase = true) ->
                "• For sleep issues: Establish a regular sleep schedule. Keep your bedroom cool and dark. " +
                "Avoid screen time and caffeine before bedtime. Consider relaxation techniques like meditation.\n\n"

            symptom.contains("fatigue", ignoreCase = true) || symptom.contains("tired", ignoreCase = true) ->
                "• For fatigue: Ensure you're getting adequate rest. Consider short power naps (15-20 minutes) during the day. " +
                "Regular moderate exercise can actually boost energy levels. Evaluate your iron levels with your healthcare provider.\n\n"

            else ->
                "• For your other symptoms: It's important to track these symptoms and discuss them with your healthcare provider " +
                "for personalized advice and treatment options.\n\n"
        }
    }

    private fun getAdviceForPrompt(prompt: String): String {
        return when {
            prompt.contains("hot flash", ignoreCase = true) ->
                "Hot flashes can be managed by maintaining a cooler environment, dressing in layers, avoiding triggers like spicy foods, " +
                "caffeine and alcohol, and practicing deep breathing techniques when a hot flash begins."

            prompt.contains("sweat", ignoreCase = true) ->
                "Night sweats can be disruptive to sleep. Consider using moisture-wicking sleepwear and bedding, " +
                "keeping your bedroom cool, and avoiding alcohol and caffeine before bedtime."

            prompt.contains("mood", ignoreCase = true) ->
                "Mood changes during menopause are common due to hormonal fluctuations. Regular exercise, stress management techniques, " +
                "adequate sleep, and social support can help. If mood changes are severe, consider speaking with a healthcare provider."

            prompt.contains("sleep", ignoreCase = true) ->
                "Good sleep hygiene is essential. Establish a consistent sleep schedule, create a cool and dark sleeping environment, " +
                "avoid screens before bedtime, limit caffeine and alcohol, and consider relaxation techniques like meditation or gentle yoga."

            prompt.contains("tired", ignoreCase = true) || prompt.contains("fatigue", ignoreCase = true) ->
                "Fatigue during menopause can be managed by ensuring adequate sleep, staying hydrated, eating a balanced diet, " +
                "engaging in regular physical activity, and managing stress. Consider having your iron and thyroid levels checked."

            else ->
                "It's important to track your symptoms and discuss them with your healthcare provider " +
                "who can provide personalized advice based on your specific health needs and menopause stage."
        }
    }
}
