package com.menopausetracker.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Represents an article about menopause or women's health topics
 */
@Entity(tableName = "articles")
data class Article(
    @PrimaryKey
    val id: String,
    val title: String,
    val summary: String,
    val content: String,
    val imageUrl: String?,
    val sourceUrl: String,
    val sourceName: String,
    val publishDate: Date,
    val isSaved: Boolean = false,
    val category: String = "General",
    val readTimeMinutes: Int = 0,
    val lastFetchDate: Date = Date()
)
