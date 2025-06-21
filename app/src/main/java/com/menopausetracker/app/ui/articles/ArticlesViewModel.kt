package com.menopausetracker.app.ui.articles

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.menopausetracker.app.data.model.Article
import com.menopausetracker.app.data.repository.ArticleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ArticlesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ArticleRepository(application)

    private val _articles = MutableLiveData<List<Article>>()
    val articles: LiveData<List<Article>> = _articles

    private val _isShowingSavedArticles = MutableLiveData(false)
    val isShowingSavedArticles: LiveData<Boolean> = _isShowingSavedArticles

    init {
        loadArticles()
    }

    private fun loadArticles() {
        CoroutineScope(Dispatchers.IO).launch {
            val articleList = if (_isShowingSavedArticles.value == true) {
                repository.getSavedArticles()
            } else {
                repository.getAllArticles()
            }
            _articles.postValue(articleList)
        }
    }

    fun toggleSaveArticle(article: Article) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.toggleSaveArticle(article)
            loadArticles() // Reload the list after saving
        }
    }

    fun toggleSavedArticlesView() {
        _isShowingSavedArticles.value = !(_isShowingSavedArticles.value ?: false)
        loadArticles()
    }
} 