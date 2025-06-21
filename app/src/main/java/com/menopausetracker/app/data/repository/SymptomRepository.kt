package com.menopausetracker.app.data.repository

import android.content.Context
import com.menopausetracker.app.data.db.MenopauseDatabase
import com.menopausetracker.app.data.model.Symptom

/**
 * Repository for managing symptom data
 */
class SymptomRepository(context: Context) {
    private val symptomDao = MenopauseDatabase.getInstance(context).symptomDao()

    suspend fun insertSymptom(symptom: Symptom): Long {
        return symptomDao.insertSymptom(symptom)
    }

    suspend fun getAllSymptoms(): List<Symptom> {
        return symptomDao.getAllSymptoms()
    }

    suspend fun getRecentSymptoms(limit: Int): List<Symptom> {
        return symptomDao.getRecentSymptoms(limit)
    }

    suspend fun getSymptomById(symptomId: Long): Symptom? {
        return symptomDao.getSymptomById(symptomId)
    }

    suspend fun deleteSymptom(symptom: Symptom) {
        symptomDao.deleteSymptom(symptom)
    }

    suspend fun deleteAllSymptoms() {
        symptomDao.deleteAllSymptoms()
    }
}
