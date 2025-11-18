package com.menopausetracker.app.data.repository

import android.app.Application
import com.menopausetracker.app.data.api.GeminiAIService
import com.menopausetracker.app.data.db.SymptomDatabase
import com.menopausetracker.app.data.db.SymptomEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecommendationRepository(application: Application) {
    private val database = SymptomDatabase.getInstance(application)
    private val symptomDao = database.symptomDao()

    // Gemini AI Service
    private val geminiApiKey = "AIzaSyD7UzsETcBH411hr13elPTa5aH0KF5yrkI"
    private val geminiAIService = GeminiAIService(geminiApiKey)

    suspend fun getRecommendations(symptoms: String): String = withContext(Dispatchers.IO) {
        // Detailed debugging - Step 1: Check if we can access the symptom database at all
        println("DEBUG: Accessing symptom database...")

        try {
            // Step 2: Try to get ALL symptoms with detailed logging
            val allSymptoms = symptomDao.getAllSymptoms()
            println("DEBUG: Found total of ${allSymptoms.size} symptoms in database")

            // Print first few symptoms if they exist
            if (allSymptoms.isNotEmpty()) {
                println("DEBUG: First symptom: ${allSymptoms[0].symptoms}, timestamp: ${allSymptoms[0].timestamp}")
            } else {
                println("DEBUG: No symptoms found in database!")
            }

            // Get recent symptoms (max 5) from history
            val recentSymptoms = allSymptoms.take(5)
            println("DEBUG: Taking ${recentSymptoms.size} recent symptoms")

            // Build a string from recent symptom history
            val historicalSymptoms = if (recentSymptoms.isNotEmpty()) {
                val symptomsText = recentSymptoms.joinToString(", ") { symptomEntry -> symptomEntry.symptoms }
                println("DEBUG: Using historical symptoms: $symptomsText")
                symptomsText
            } else {
                // No symptoms in history, check if current symptoms are provided
                if (symptoms.isNotBlank()) {
                    println("DEBUG: No history found, falling back to current symptoms: $symptoms")
                    symptoms
                } else {
                    println("DEBUG: No history found AND no current symptoms provided!")
                    return@withContext "Please log some symptoms first to get personalized recommendations. No symptoms found in your history."
                }
            }

            // Use Gemini AI for recommendations based on historical symptoms
            try {
                println("DEBUG: Requesting AI recommendations for: $historicalSymptoms")
                geminiAIService.getRecommendations(historicalSymptoms)
            } catch (e: Exception) {
                println("DEBUG: AI recommendation error: ${e.message}")
                e.printStackTrace()
                // If Gemini API call fails, return a default message
                "I'm sorry, I couldn't connect to the recommendation service. Please check your internet connection and try again."
            }
        } catch (e: Exception) {
            println("DEBUG: Database access error: ${e.message}")
            e.printStackTrace()
            "Error accessing symptom history: ${e.message}"
        }
    }

    suspend fun getAllSymptoms(): List<SymptomEntry> = withContext(Dispatchers.IO) {
        symptomDao.getAllSymptoms()
    }

    suspend fun deleteAllSymptoms() = withContext(Dispatchers.IO) {
        symptomDao.deleteAll()
    }

    /**
     * Saves a symptom to the legacy database so it can be used for recommendations
     */
    suspend fun saveSymptom(symptoms: String) = withContext(Dispatchers.IO) {
        try {
            println("DEBUG: Saving symptom to legacy database: $symptoms")

            // Create a legacy symptom entry
            val entry = SymptomEntry(
                symptoms = symptoms,
                timestamp = System.currentTimeMillis()
            )

            // Save to database
            symptomDao.insert(entry)

            // Verify it was saved by reading back
            val allSymptoms = symptomDao.getAllSymptoms()
            println("DEBUG: After saving, total symptom count: ${allSymptoms.size}")
            if (allSymptoms.isNotEmpty()) {
                println("DEBUG: Last symptom saved: ${allSymptoms[0].symptoms}")
            }
        } catch (e: Exception) {
            println("DEBUG: Error saving symptom to legacy database: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Clears all symptom history from the database
     */
    suspend fun clearSymptomHistory() = withContext(Dispatchers.IO) {
        try {
            println("DEBUG: Clearing all symptom history from database")
            deleteAllSymptoms()
            println("DEBUG: Symptom history cleared successfully")
        } catch (e: Exception) {
            println("DEBUG: Error clearing symptom history: ${e.message}")
            e.printStackTrace()
        }
    }
}
