package com.menopausetracker.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.menopausetracker.app.R
import com.menopausetracker.app.data.model.Symptom
import com.menopausetracker.app.data.model.Tracking
import com.menopausetracker.app.data.repository.RecommendationRepository
import com.menopausetracker.app.data.repository.SymptomRepository
import com.menopausetracker.app.data.repository.TrackingRepository
import com.menopausetracker.app.util.GreetingManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.concurrent.TimeUnit

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val recommendationRepository = RecommendationRepository(application)
    private val symptomRepository = SymptomRepository(application)
    private val trackingRepository = TrackingRepository(application)
    private val greetingManager = GreetingManager(application)

    private val _recommendations = MutableLiveData<String>()
    val recommendations: LiveData<String> = _recommendations

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _greeting = MutableLiveData<String>()
    val greeting: LiveData<String> = _greeting

    private val _daysCount = MutableLiveData<Int>()
    val daysCount: LiveData<Int> = _daysCount

    private val _isTrackingActive = MutableLiveData<Boolean>()
    val isTrackingActive: LiveData<Boolean> = _isTrackingActive

    private val _symptomSaved = MutableLiveData<Boolean>()
    val symptomSaved: LiveData<Boolean> = _symptomSaved

    init {
        loadGreeting()
        loadTrackingInfo()
    }

    private fun loadGreeting() {
        _greeting.value = greetingManager.getDailyGreeting()
    }

    private fun loadTrackingInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            val tracking = trackingRepository.getTracking()
            withContext(Dispatchers.Main) {
                if (tracking != null && tracking.isActive) {
                    val startDate = tracking.startDate
                    val currentDate = Date()
                    val diffInMillis = currentDate.time - startDate.time
                    val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()
                    _daysCount.value = diffInDays
                    _isTrackingActive.value = true
                } else {
                    _daysCount.value = 0
                    _isTrackingActive.value = false
                }
            }
        }
    }

    fun getRecommendations(symptoms: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                _isLoading.postValue(true)
                _error.postValue(null)
                
                val result = recommendationRepository.getRecommendations(symptoms)
                withContext(Dispatchers.Main) {
                    _recommendations.value = result
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = when {
                        e.message?.contains("Unable to resolve host") == true -> 
                            getApplication<Application>().getString(R.string.no_internet)
                        else -> e.message ?: "An error occurred"
                    }
                }
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun logSymptom(
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
                val symptom = Symptom(
                    date = Date(),
                    description = description,
                    severity = severity,
                    hotFlashes = hotFlashes,
                    nightSweats = nightSweats,
                    moodChanges = moodChanges,
                    sleepIssues = sleepIssues,
                    fatigue = fatigue
                )

                symptomRepository.insertSymptom(symptom)

                // Start or update tracking if not already active
                val tracking = trackingRepository.getTracking()
                if (tracking == null || !tracking.isActive) {
                    trackingRepository.insertTracking(
                        Tracking(
                            startDate = Date(),
                            daysCount = 0,
                            isActive = true
                        )
                    )
                }

                withContext(Dispatchers.Main) {
                    _symptomSaved.value = true
                    loadTrackingInfo()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = e.message ?: "Error saving symptom"
                    _symptomSaved.value = false
                }
            }
        }
    }

    fun reportRecovery() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tracking = trackingRepository.getTracking()
                if (tracking != null) {
                    trackingRepository.insertTracking(
                        tracking.copy(isActive = false)
                    )
                }
                withContext(Dispatchers.Main) {
                    _isTrackingActive.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = e.message ?: "Error reporting recovery"
                }
            }
        }
    }

    fun resetSymptomSaved() {
        _symptomSaved.value = false
    }
}

