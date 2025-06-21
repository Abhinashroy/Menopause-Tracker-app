package com.menopausetracker.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Data class representing the user's recovery tracking information
 */
@Entity(tableName = "tracking")
data class Tracking(
    @PrimaryKey
    val id: Int = 1, // Single row in table
    val startDate: Date = Date(),
    val daysCount: Int = 0,
    val isActive: Boolean = true
)
