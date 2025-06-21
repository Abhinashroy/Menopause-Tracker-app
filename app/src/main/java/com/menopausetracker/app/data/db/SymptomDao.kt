package com.menopausetracker.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import com.menopausetracker.app.data.model.Symptom

/**
 * Data Access Object for Symptom entities
 */
@Dao
interface SymptomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptom(symptom: Symptom): Long

    @Query("SELECT * FROM symptoms ORDER BY date DESC")
    suspend fun getAllSymptoms(): List<Symptom>

    @Query("SELECT * FROM symptoms ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentSymptoms(limit: Int): List<Symptom>

    @Query("SELECT * FROM symptoms WHERE id = :symptomId")
    suspend fun getSymptomById(symptomId: Long): Symptom?

    @Delete
    suspend fun deleteSymptom(symptom: Symptom)

    @Query("DELETE FROM symptoms")
    suspend fun deleteAllSymptoms()
}
