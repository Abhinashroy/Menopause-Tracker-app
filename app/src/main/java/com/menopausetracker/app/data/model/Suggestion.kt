package com.menopausetracker.app.data.model

/**
 * Data class representing an AI health suggestion
 */
data class Suggestion(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val content: String,
    val prompt: String = "", // Added prompt field to store user's original question
    val timestamp: Long = System.currentTimeMillis()
)
