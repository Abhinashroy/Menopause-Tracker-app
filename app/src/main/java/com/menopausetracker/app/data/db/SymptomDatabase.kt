package com.menopausetracker.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

@Database(entities = [SymptomEntry::class], version = 1, exportSchema = false)
abstract class SymptomDatabase : RoomDatabase() {
    abstract fun symptomDao(): OldSymptomDao

    companion object {
        @Volatile
        private var INSTANCE: SymptomDatabase? = null

        fun getInstance(context: Context): SymptomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SymptomDatabase::class.java,
                    "legacy_symptom_database" // Changed database name
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

@Entity(tableName = "legacy_symptoms") // Changed table name to avoid conflicts
data class SymptomEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val symptoms: String,
    val timestamp: Long
)

@Dao
interface OldSymptomDao { // Renamed to clarify this is the old implementation
    @Query("SELECT * FROM legacy_symptoms ORDER BY timestamp DESC") // Updated table name
    suspend fun getAllSymptoms(): List<SymptomEntry>

    @Insert
    suspend fun insert(entry: SymptomEntry)

    @Query("DELETE FROM legacy_symptoms") // Updated table name
    suspend fun deleteAll()
}

