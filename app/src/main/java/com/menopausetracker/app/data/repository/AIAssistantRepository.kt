package com.menopausetracker.app.data.repository

import com.menopausetracker.app.data.model.Suggestion
import com.menopausetracker.app.data.model.Symptom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Repository for handling AI health suggestions based on user prompts and logged symptoms.
 */
class AIAssistant {

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
            prompt.contains("diet", ignoreCase = true) || prompt.contains("food", ignoreCase = true) -> "Diet and Nutrition"
            prompt.contains("exercise", ignoreCase = true) || prompt.contains("workout", ignoreCase = true) -> "Exercise Benefits"
            prompt.contains("supplement", ignoreCase = true) || prompt.contains("vitamin", ignoreCase = true) -> "Supplement Information"
            prompt.contains("memory", ignoreCase = true) || prompt.contains("brain fog", ignoreCase = true) -> "Cognitive Health"
            prompt.contains("weight", ignoreCase = true) -> "Weight Management"
            prompt.contains("sex", ignoreCase = true) || prompt.contains("libido", ignoreCase = true) -> "Sexual Health"
            prompt.contains("bone", ignoreCase = true) || prompt.contains("osteoporosis", ignoreCase = true) -> "Bone Health"
            prompt.contains("period", ignoreCase = true) || prompt.contains("menstrual", ignoreCase = true) -> "Menstrual Changes"
            prompt.contains("skin", ignoreCase = true) || prompt.contains("dry", ignoreCase = true) -> "Skin Care"
            prompt.contains("heart", ignoreCase = true) || prompt.contains("cardiovascular", ignoreCase = true) -> "Heart Health"
            prompt.contains("joint", ignoreCase = true) || prompt.contains("pain", ignoreCase = true) -> "Pain Management"
            else -> "Menopause Information"
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

                // Add advice for the prompt in a consistent format with symptom advice
                responseBuilder.append("• For your question about ${getQuestionTopic(prompt)}: ")
                responseBuilder.append(getAdviceForPrompt(prompt))
                responseBuilder.append("\n\n")
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
                responseBuilder.append("Regarding your question about ${getQuestionTopic(prompt)}:\n\n")
                responseBuilder.append("• ")
                responseBuilder.append(getAdviceForPrompt(prompt))
            }
        }

        return responseBuilder.toString()
    }

    // Helper method to determine the topic of the question
    private fun getQuestionTopic(prompt: String): String {
        return when {
            prompt.contains("hot flash", ignoreCase = true) -> "hot flashes"
            prompt.contains("sweat", ignoreCase = true) -> "night sweats"
            prompt.contains("mood", ignoreCase = true) -> "mood changes"
            prompt.contains("sleep", ignoreCase = true) -> "sleep"
            prompt.contains("tired", ignoreCase = true) || prompt.contains("fatigue", ignoreCase = true) -> "fatigue"
            prompt.contains("diet", ignoreCase = true) || prompt.contains("food", ignoreCase = true) || prompt.contains("eat", ignoreCase = true) -> "diet"
            prompt.contains("exercise", ignoreCase = true) || prompt.contains("workout", ignoreCase = true) || prompt.contains("activity", ignoreCase = true) -> "exercise"
            prompt.contains("supplement", ignoreCase = true) || prompt.contains("vitamin", ignoreCase = true) -> "supplements"
            prompt.contains("memory", ignoreCase = true) || prompt.contains("brain fog", ignoreCase = true) || prompt.contains("concentration", ignoreCase = true) -> "cognitive changes"
            prompt.contains("weight", ignoreCase = true) || prompt.contains("gain", ignoreCase = true) -> "weight management"
            prompt.contains("sex", ignoreCase = true) || prompt.contains("libido", ignoreCase = true) || prompt.contains("vaginal dryness", ignoreCase = true) -> "intimate health"
            prompt.contains("bone", ignoreCase = true) || prompt.contains("osteoporosis", ignoreCase = true) -> "bone health"
            prompt.contains("headache", ignoreCase = true) || prompt.contains("migraine", ignoreCase = true) -> "headaches"
            prompt.contains("period", ignoreCase = true) || prompt.contains("menstrual", ignoreCase = true) -> "menstrual changes"
            prompt.contains("skin", ignoreCase = true) || prompt.contains("dry", ignoreCase = true) -> "skin changes"
            prompt.contains("heart", ignoreCase = true) || prompt.contains("cardiovascular", ignoreCase = true) -> "heart health"
            prompt.contains("joint", ignoreCase = true) || prompt.contains("pain", ignoreCase = true) -> "joint pain"
            else -> "menopause"
        }
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
        // Extract relevant keywords from the prompt
        val cleanedPrompt = prompt.toLowerCase()

        // Check for specific topics and provide tailored responses
        return when {
            cleanedPrompt.contains("hot flash") ->
                "Hot flashes can be managed by maintaining a cooler environment, dressing in layers, avoiding triggers like spicy foods, " +
                "caffeine and alcohol, and practicing deep breathing techniques when a hot flash begins."

            cleanedPrompt.contains("sweat") ->
                "Night sweats can be disruptive to sleep. Consider using moisture-wicking sleepwear and bedding, " +
                "keeping your bedroom cool, and avoiding alcohol and caffeine before bedtime."

            cleanedPrompt.contains("mood") ->
                "Mood changes during menopause are common due to hormonal fluctuations. Regular exercise, stress management techniques, " +
                "adequate sleep, and social support can help. If mood changes are severe, consider speaking with a healthcare provider."

            cleanedPrompt.contains("sleep") ->
                "Good sleep hygiene is essential. Establish a consistent sleep schedule, create a cool and dark sleeping environment, " +
                "avoid screens before bedtime, limit caffeine and alcohol, and consider relaxation techniques like meditation or gentle yoga."

            cleanedPrompt.contains("tired") || cleanedPrompt.contains("fatigue") ->
                "Fatigue during menopause can be managed by ensuring adequate sleep, staying hydrated, eating a balanced diet, " +
                "engaging in regular physical activity, and managing stress. Consider having your iron and thyroid levels checked."

            cleanedPrompt.contains("diet") || cleanedPrompt.contains("food") || cleanedPrompt.contains("eat") ->
                "A balanced diet rich in calcium, vitamin D, and whole foods can help manage menopause symptoms. Consider adding foods with phytoestrogens like soy, flaxseed, and legumes. Limit processed foods, caffeine, and alcohol which can trigger hot flashes."

            cleanedPrompt.contains("exercise") || cleanedPrompt.contains("workout") || cleanedPrompt.contains("activity") ->
                "Regular exercise can help manage weight, improve mood, and promote better sleep during menopause. Aim for a mix of aerobic activity, strength training, and flexibility exercises. Walking, swimming, yoga, and tai chi are excellent options."

            cleanedPrompt.contains("supplement") || cleanedPrompt.contains("vitamin") ->
                "Some supplements like black cohosh, red clover, and evening primrose oil may help with menopause symptoms, but research results are mixed. Always consult with your healthcare provider before starting any supplements as they may interact with medications."

            cleanedPrompt.contains("memory") || cleanedPrompt.contains("brain fog") || cleanedPrompt.contains("concentration") ->
                "Mental fogginess during menopause is common. Stay mentally active with puzzles, reading, and learning new skills. Regular exercise, adequate sleep, and stress management can also help improve cognitive function."

            cleanedPrompt.contains("weight") || cleanedPrompt.contains("gain") ->
                "Weight changes are common during menopause due to hormonal shifts, decreased muscle mass, and metabolic changes. Focus on nutrient-dense foods, portion control, regular physical activity, and strength training to maintain a healthy weight."

            cleanedPrompt.contains("sex") || cleanedPrompt.contains("libido") || cleanedPrompt.contains("vaginal dryness") ->
                "Changes in sexual desire and comfort are common during menopause. Vaginal moisturizers, lubricants, and regular sexual activity can help maintain vaginal health. Discuss persistent issues with your healthcare provider as treatments are available."

            cleanedPrompt.contains("bone") || cleanedPrompt.contains("osteoporosis") ->
                "Bone density typically decreases during menopause. Ensure adequate calcium (1000-1200mg daily) and vitamin D intake, engage in weight-bearing exercises, and consider a bone density scan if you're concerned about osteoporosis risk."

            cleanedPrompt.contains("headache") || cleanedPrompt.contains("migraine") ->
                "Headaches may change in pattern or intensity during menopause due to hormonal fluctuations. Identify and avoid triggers, maintain a regular sleep schedule, stay hydrated, and practice stress management techniques."

            cleanedPrompt.contains("period") || cleanedPrompt.contains("menstrual") || cleanedPrompt.contains("bleeding") ->
                "Menstrual changes are common during perimenopause. Periods may become irregular, lighter or heavier than usual. Track your cycle patterns and report significant changes to your healthcare provider, especially heavy bleeding or periods that resume after 12 months without one."

            cleanedPrompt.contains("hormone") || cleanedPrompt.contains("hrt") || cleanedPrompt.contains("therapy") ->
                "Hormone replacement therapy (HRT) can effectively manage many menopause symptoms. It comes in various forms including pills, patches, gels, and creams. Benefits and risks vary depending on your age, health history, and when you start treatment. Discuss options with a healthcare provider specializing in women's health."

            cleanedPrompt.contains("skin") || cleanedPrompt.contains("dry") || cleanedPrompt.contains("itchy") ->
                "Skin changes during menopause include increased dryness, reduced elasticity, and sometimes itchiness due to decreased estrogen. Use gentle, fragrance-free cleansers, moisturize daily, drink plenty of water, and consider using a humidifier. Products with hyaluronic acid, glycerin, or ceramides can be particularly helpful."

            cleanedPrompt.contains("heart") || cleanedPrompt.contains("cardiovascular") ->
                "Heart health becomes increasingly important during and after menopause as estrogen's protective effects decline. Focus on a heart-healthy diet with omega-3 fatty acids, regular cardiovascular exercise, maintaining healthy weight and blood pressure, limiting alcohol, and not smoking. Regular health screenings are essential."

            cleanedPrompt.contains("joint") || cleanedPrompt.contains("pain") || cleanedPrompt.contains("ache") ->
                "Joint pain during menopause can be related to hormonal changes. Maintaining a healthy weight reduces joint stress. Regular, low-impact exercise like swimming or cycling helps preserve joint function. Anti-inflammatory foods, adequate hydration, and proper stretching may also provide relief."

            cleanedPrompt.contains("hair") || cleanedPrompt.contains("thinning") ->
                "Hair thinning is common during menopause due to hormonal changes. Try gentle hair care products, avoid heat styling when possible, and consider a diet rich in protein, iron, and vitamins. Topical minoxidil may help, and in some cases, prescription treatments might be appropriate after consulting with a dermatologist."

            cleanedPrompt.contains("anxiety") || cleanedPrompt.contains("stress") || cleanedPrompt.contains("worry") ->
                "Anxiety can increase during menopause due to hormonal fluctuations. Regular exercise, mindfulness meditation, and breathing techniques can help manage stress. Limit caffeine and alcohol, maintain social connections, and consider cognitive-behavioral therapy if anxiety interferes with daily life."

            // For any other questions, provide specific menopause-related information rather than generic advice
            else -> {
                val menopauseTopics = listOf(
                    "Menopause typically occurs between ages 45-55, with perimenopause beginning several years earlier. Symptoms vary widely in type, intensity, and duration. The transition is confirmed after 12 consecutive months without a period. Understanding your specific stage can help you better manage symptoms.",

                    "Managing menopause symptoms often involves a personalized approach combining lifestyle changes, mind-body practices, and possibly medical treatments. Regular physical activity, a balanced diet rich in whole foods, stress reduction techniques, and adequate sleep form the foundation of symptom management.",

                    "During menopause, your body undergoes significant hormonal changes, primarily decreasing estrogen and progesterone levels. These changes can affect multiple body systems beyond reproductive health, including bone density, cardiovascular health, and metabolism, making holistic self-care especially important.",

                    "Self-care during menopause includes staying well-hydrated, maintaining a consistent sleep schedule, practicing stress management, nurturing social connections, and setting reasonable expectations for yourself. Many women find this a good time to reevaluate priorities and focus on personal well-being.",

                    "Complementary approaches like acupuncture, mindfulness meditation, and yoga show promising results for managing various menopause symptoms. When combined with conventional treatments and lifestyle modifications, these practices may provide additional relief and improve overall quality of life."
                )

                // Use the prompt's hash code to select a relevant response that will be consistent for the same question
                val responseIndex = Math.abs(prompt.hashCode()) % menopauseTopics.size
                menopauseTopics[responseIndex]
            }
        }
    }
}
