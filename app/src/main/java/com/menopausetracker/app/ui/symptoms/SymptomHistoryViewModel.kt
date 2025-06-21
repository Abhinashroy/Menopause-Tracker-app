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

class SymptomHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val symptomRepository = SymptomRepository(application)

    private val _symptoms = MutableLiveData<List<Symptom>>()
    val symptoms: LiveData<List<Symptom>> = _symptoms

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _symptomDeleted = MutableLiveData<Boolean>()
    val symptomDeleted: LiveData<Boolean> = _symptomDeleted

    fun loadSymptoms() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                _isLoading.postValue(true)
                val allSymptoms = symptomRepository.getAllSymptoms()
                withContext(Dispatchers.Main) {
                    _symptoms.value = allSymptoms
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = e.message ?: "Error loading symptoms"
                }
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun deleteSymptom(symptom: Symptom) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                symptomRepository.deleteSymptom(symptom)

                // Refresh the list after deletion
                val updatedSymptoms = symptomRepository.getAllSymptoms()

                withContext(Dispatchers.Main) {
                    _symptomDeleted.value = true
                    _symptoms.value = updatedSymptoms
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = e.message ?: "Error deleting symptom"
                }
            }
        }
    }

    fun resetSymptomDeleted() {
        _symptomDeleted.value = false
    }
}
