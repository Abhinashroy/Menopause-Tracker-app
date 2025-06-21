package com.menopausetracker.app.data.repository

import android.app.Application
import com.menopausetracker.app.data.api.GeminiAIService
import com.menopausetracker.app.data.api.RecommendationApi
import com.menopausetracker.app.data.api.RecommendationRequest
import com.menopausetracker.app.data.db.SymptomEntry
import com.menopausetracker.app.data.db.SymptomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RecommendationRepository(application: Application) {
    private val database = SymptomDatabase.getInstance(application)
    private val symptomDao = database.symptomDao()

    // Gemini AI Service
    private val geminiApiKey = "AIzaSyD7UzsETcBH411hr13elPTa5aH0KF5yrkI"
    private val geminiAIService = GeminiAIService(geminiApiKey)

    // Legacy API setup - kept for backward compatibility
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.menopausetracker.com/") // Using a proper URL format
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(RecommendationApi::class.java)

    suspend fun getRecommendations(symptoms: String): String = withContext(Dispatchers.IO) {
        // Save symptoms to local database
        val entry = SymptomEntry(
            symptoms = symptoms,
            timestamp = System.currentTimeMillis()
        )
        symptomDao.insert(entry)

        try {
            // Use Gemini AI for recommendations instead of the previous API
            geminiAIService.getRecommendations(symptoms)
        } catch (e: Exception) {
            e.printStackTrace()
            // If Gemini API call fails, return a default message
            "I'm sorry, I couldn't connect to the recommendation service. Please check your internet connection and try again."
        }
    }

    suspend fun getAllSymptoms(): List<SymptomEntry> = withContext(Dispatchers.IO) {
        symptomDao.getAllSymptoms()
    }

    suspend fun deleteAllSymptoms() = withContext(Dispatchers.IO) {
        symptomDao.deleteAll()
    }
}
