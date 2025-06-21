package com.menopausetracker.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.menopausetracker.app.data.model.Tracking

/**
 * Data Access Object for Tracking entities
 */
@Dao
interface TrackingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracking(tracking: Tracking): Long

    @Query("SELECT * FROM tracking WHERE id = 1")
    suspend fun getTracking(): Tracking?

    @Query("DELETE FROM tracking")
    suspend fun deleteTracking()
}
