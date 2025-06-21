package com.menopausetracker.app.ui.symptoms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.menopausetracker.app.data.model.Symptom
import com.menopausetracker.app.data.repository.SymptomRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class EditSymptomViewModel(application: Application) : AndroidViewModel(application) {
    private val symptomRepository = SymptomRepository(application)

    private val _symptom = MutableLiveData<Symptom?>()
    val symptom: LiveData<Symptom?> = _symptom

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _symptomSaved = MutableLiveData<Boolean>()
    val symptomSaved: LiveData<Boolean> = _symptomSaved

    private var currentSymptomId: Long = 0

    fun loadSymptom(symptomId: Long) {
        if (symptomId <= 0) return

        currentSymptomId = symptomId
        CoroutineScope(Dispatchers.IO).launch {
            try {
                _isLoading.postValue(true)
                val loadedSymptom = symptomRepository.getSymptomById(symptomId)
                withContext(Dispatchers.Main) {
                    _symptom.value = loadedSymptom
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = e.message ?: "Error loading symptom"
                }
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun saveSymptom(
        description: String,
        severity: Int,
        hotFlashes: Boolean,
        nightSweats: Boolean,
        moodChanges: Boolean,
        sleepIssues: Boolean,
        fatigue: Boolean
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                _isLoading.postValue(true)

                val symptom = if (currentSymptomId > 0) {
                    // Update existing symptom
                    _symptom.value?.copy(
                        description = description,
                        severity = severity,
                        hotFlashes = hotFlashes,
                        nightSweats = nightSweats,
                        moodChanges = moodChanges,
                        sleepIssues = sleepIssues,
                        fatigue = fatigue
                    ) ?: Symptom(
                        id = currentSymptomId,
                        date = Date(),
                        description = description,
                        severity = severity,
                        hotFlashes = hotFlashes,
                        nightSweats = nightSweats,
                        moodChanges = moodChanges,
                        sleepIssues = sleepIssues,
                        fatigue = fatigue
                    )
                } else {
                    // Create new symptom
                    Symptom(
                        date = Date(),
                        description = description,
                        severity = severity,
                        hotFlashes = hotFlashes,
                        nightSweats = nightSweats,
                        moodChanges = moodChanges,
                        sleepIssues = sleepIssues,
                        fatigue = fatigue
                    )
                }

                symptomRepository.insertSymptom(symptom)

                withContext(Dispatchers.Main) {
                    _symptomSaved.value = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = e.message ?: "Error saving symptom"
                }
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun resetSymptomSaved() {
        _symptomSaved.value = false
    }
}
