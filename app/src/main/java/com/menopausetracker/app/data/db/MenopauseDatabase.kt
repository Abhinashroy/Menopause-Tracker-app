package com.menopausetracker.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.menopausetracker.app.data.model.Symptom
import com.menopausetracker.app.data.model.Tracking

/**
 * Room database for the menopause tracker app
 */
@Database(
    entities = [Symptom::class, Tracking::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class MenopauseDatabase : RoomDatabase() {
    abstract fun symptomDao(): SymptomDao
    abstract fun trackingDao(): TrackingDao

    companion object {
        @Volatile
        private var INSTANCE: MenopauseDatabase? = null

        fun getInstance(context: Context): MenopauseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MenopauseDatabase::class.java,
                    "menopause_database"
                )
                .fallbackToDestructiveMigration()
                .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
