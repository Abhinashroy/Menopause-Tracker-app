package com.menopausetracker.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Data class representing a menopause symptom entry
 */
@Entity(tableName = "symptoms")
data class Symptom(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Date = Date(),
    val description: String = "",
    val severity: Int = 0,
    val hotFlashes: Boolean = false,
    val nightSweats: Boolean = false,
    val moodChanges: Boolean = false,
    val sleepIssues: Boolean = false,
    val fatigue: Boolean = false,
    val otherSymptoms: String = ""
)
