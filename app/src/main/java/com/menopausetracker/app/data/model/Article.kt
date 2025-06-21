package com.menopausetracker.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val imageUrl: String,
    var isSaved: Boolean = false
)

