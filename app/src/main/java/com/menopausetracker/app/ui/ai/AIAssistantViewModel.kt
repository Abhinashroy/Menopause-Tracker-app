package com.menopausetracker.app.ui.ai

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.menopausetracker.app.data.model.Suggestion
import com.menopausetracker.app.data.model.Symptom
import com.menopausetracker.app.data.repository.AIAssistantRepository
import com.menopausetracker.app.data.repository.SymptomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AIAssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val aiAssistantRepository = AIAssistantRepository()
    private val symptomRepository = SymptomRepository(application)
    private val sharedPreferences = application.getSharedPreferences("ai_suggestions", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _suggestions = MutableLiveData<List<Suggestion>>()
    val suggestions: LiveData<List<Suggestion>> = _suggestions

    private val _currentSuggestions = mutableListOf<Suggestion>()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Single deletion info for RecyclerView optimization
    private val _deletedSuggestionIndex = MutableLiveData<Int>()
    val deletedSuggestionIndex: LiveData<Int> = _deletedSuggestionIndex

    // Current selected suggestion for detail view
    private val _selectedSuggestion = MutableLiveData<Suggestion>()
    val selectedSuggestion: LiveData<Suggestion> = _selectedSuggestion

    companion object {
        private const val SUGGESTIONS_KEY = "saved_suggestions"
        private const val MAX_RECENT_SYMPTOMS = 2
    }

    init {
        loadSavedSuggestions()
    }

    private fun loadSavedSuggestions() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val savedSuggestionsJson = sharedPreferences.getString(SUGGESTIONS_KEY, null)
                if (!savedSuggestionsJson.isNullOrEmpty()) {
                    val type = object : TypeToken<List<Suggestion>>() {}.type
                    try {
                        val savedSuggestions = gson.fromJson<List<Suggestion>>(savedSuggestionsJson, type)
                        _currentSuggestions.clear()
                        _currentSuggestions.addAll(savedSuggestions)
                        withContext(Dispatchers.Main) {
                            _suggestions.value = _currentSuggestions.toList()
                        }
                    } catch (e: Exception) {
                        // Handle errors with deserialization of older suggestions
                        // Clear the invalid suggestions and start fresh
                        withContext(Dispatchers.Main) {
                            _error.value = "Unable to load previous suggestions. Starting fresh."
                        }
                        sharedPreferences.edit {
                            remove(SUGGESTIONS_KEY)
                        }
                        _currentSuggestions.clear()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "Error loading saved suggestions: ${e.message}"
                }
            }
        }
    }

    private fun saveSuggestions() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val suggestionsJson = gson.toJson(_currentSuggestions)
                sharedPreferences.edit {
                    putString(SUGGESTIONS_KEY, suggestionsJson)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "Error saving suggestions"
                }
            }
        }
    }

    fun getAdvice(prompt: String) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Fetch recent symptoms (max 2)
                val recentSymptoms = getRecentSymptoms(MAX_RECENT_SYMPTOMS)

                // If prompt is blank and no recent symptoms, show error
                if (prompt.isBlank() && recentSymptoms.isEmpty()) {
                    _error.postValue("Please enter a question or log symptoms for personalized advice.")
                    _isLoading.postValue(false)
                    return@launch
                }

                // Generate suggestion based on prompt and recent symptoms
                val result = aiAssistantRepository.generateSuggestion(prompt, recentSymptoms)

                result.onSuccess { suggestion: Suggestion ->
                    _currentSuggestions.add(0, suggestion)
                    if (_currentSuggestions.size > 50) {
                        _currentSuggestions.removeAt(_currentSuggestions.size - 1)
                    }
                    _suggestions.postValue(_currentSuggestions.toList())
                    _error.postValue(null)
                    saveSuggestions()
                }.onFailure { exception: Throwable ->
                    _error.postValue(exception.message)
                }
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    private suspend fun getRecentSymptoms(limit: Int): List<Symptom> {
        return withContext(Dispatchers.IO) {
            try {
                symptomRepository.getRecentSymptoms(limit)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    fun deleteSuggestion(suggestionId: String) {
        if (suggestionId.isEmpty()) return

        val indexToRemove = _currentSuggestions.indexOfFirst { it.id == suggestionId }
        if (indexToRemove >= 0) {
            _currentSuggestions.removeAt(indexToRemove)
            _deletedSuggestionIndex.value = indexToRemove
            _suggestions.value = _currentSuggestions.toList()
            saveSuggestions()
        }
    }

    fun clearErrors() {
        _error.value = null
    }

    // Method to set the selected suggestion when navigating to details
    fun setSelectedSuggestion(suggestion: Suggestion) {
        _selectedSuggestion.value = suggestion
    }
}
