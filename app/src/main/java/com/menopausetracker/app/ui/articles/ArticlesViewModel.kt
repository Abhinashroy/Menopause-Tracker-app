package com.menopausetracker.app.ui.articles

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.menopausetracker.app.data.model.Article
import com.menopausetracker.app.data.repository.ArticleRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ArticlesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ArticleRepository(application)

    // Current article filter category
    private val _currentCategory = MutableLiveData<String>()
    val currentCategory: LiveData<String> = _currentCategory

    // Loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Error state
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Articles data
    private var _articles = MutableLiveData<List<Article>>()
    val articles: LiveData<List<Article>> get() = _articles

    // Saved articles
    val savedArticles = repository.getSavedArticles().asLiveData()

    // Track whether we're showing saved articles or all articles
    private val _showingSavedArticles = MutableLiveData<Boolean>(false)

    init {
        // Initialize articles data with ALL articles regardless of category
        viewModelScope.launch {
            // Get all articles without filtering by category
            _articles.value = repository.getArticles(false).first()
            refreshArticles(true) // Force refresh to get latest articles
        }
    }

    /**
     * Refresh articles from network sources
     */
    fun refreshArticles(forceRefresh: Boolean = true) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val success = repository.refreshArticles()
                if (!success) {
                    _errorMessage.value = "Unable to refresh articles. Showing cached content."
                }

                // Always load all articles regardless of category
                _articles.value = repository.getArticles(false).first()

                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Network error: ${e.message ?: "Unknown error"}"
            }
        }
    }

    /**
     * Filter articles by category - modified to show all articles
     * This is kept for compatibility but now simply shows all articles
     */
    fun filterByCategory(category: String) {
        _currentCategory.value = category

        viewModelScope.launch {
            // Always return all articles regardless of the category selection
            _articles.value = repository.getArticles(false).first()
        }
    }

    /**
     * Toggle article saved/bookmarked status
     */
    fun toggleArticleSaved(article: Article) {
        viewModelScope.launch {
            repository.toggleArticleSaved(article.id, !article.isSaved)
        }
    }

    /**
     * Get a specific article by its ID
     */
    fun getArticleById(id: String): LiveData<Article?> {
        val result = MutableLiveData<Article?>()
        viewModelScope.launch {
            result.postValue(repository.getArticleById(id))
        }
        return result
    }

    /**
     * Switch to showing only saved articles
     */
    fun showSavedArticles() {
        _isLoading.value = true
        _showingSavedArticles.value = true

        viewModelScope.launch {
            try {
                _articles.value = repository.getSavedArticles().first()
            } catch (e: Exception) {
                _articles.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Switch to showing all articles (default view)
     */
    fun showAllArticles() {
        _showingSavedArticles.value = false

        // If a category filter is active, reapply it
        _currentCategory.value?.let {
            if (it != "All") {
                filterByCategory(it)
                return
            }
        }

        // Otherwise just refresh articles
        refreshArticles(false)
    }

    /**
     * Check if we're currently showing saved articles
     */
    fun isShowingSavedArticles(): Boolean {
        return _showingSavedArticles.value == true
    }

    /**
     * Refresh only saved articles
     * Used when returning to the saved articles screen
     */
    fun refreshSavedArticles() {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Load fresh saved articles data from repository
                _articles.value = repository.getSavedArticles().first()
            } catch (e: Exception) {
                // If there's an error, keep existing list or show empty
                if (_articles.value == null) {
                    _articles.value = emptyList()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
