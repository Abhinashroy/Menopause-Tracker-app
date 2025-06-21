package com.menopausetracker.app.data.repository

import android.content.Context
import com.menopausetracker.app.data.db.MenopauseDatabase
import com.menopausetracker.app.data.model.Tracking

/**
 * Repository for managing tracking data
 */
class TrackingRepository(context: Context) {
    private val trackingDao = MenopauseDatabase.getInstance(context).trackingDao()

    suspend fun insertTracking(tracking: Tracking): Long {
        return trackingDao.insertTracking(tracking)
    }

    suspend fun getTracking(): Tracking? {
        return trackingDao.getTracking()
    }

    suspend fun deleteTracking() {
        trackingDao.deleteTracking()
    }
}
