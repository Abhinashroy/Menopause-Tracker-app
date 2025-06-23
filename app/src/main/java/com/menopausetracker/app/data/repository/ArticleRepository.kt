package com.menopausetracker.app.data.repository

import android.content.Context
import com.menopausetracker.app.data.api.RssArticleService
import com.menopausetracker.app.data.db.ArticleDao
import com.menopausetracker.app.data.db.ArticleDatabase
import com.menopausetracker.app.data.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * Repository to manage article data from both remote API and local database
 */
class ArticleRepository(private val context: Context) {
    private val rssArticleService = RssArticleService(context)
    private val articleDao: ArticleDao

    init {
        val database = ArticleDatabase.getInstance(context)
        articleDao = database.articleDao()
    }

    /**
     * Get recent articles, refreshing from network if needed
     */
    suspend fun getArticles(forceRefresh: Boolean = false): Flow<List<Article>> {
        if (forceRefresh) {
            refreshArticles()
        }

        return articleDao.getRecentArticles(50)
    }

    /**
     * Get articles from a specific category
     */
    fun getArticlesByCategory(category: String): Flow<List<Article>> {
        return articleDao.getArticlesByCategory(category)
    }

    /**
     * Get saved/bookmarked articles
     */
    fun getSavedArticles(): Flow<List<Article>> {
        return articleDao.getSavedArticles()
    }

    /**
     * Get a specific article by its ID
     */
    suspend fun getArticleById(id: String): Article? {
        return articleDao.getArticleById(id)
    }

    /**
     * Save or unsave an article
     */
    suspend fun toggleArticleSaved(articleId: String, isSaved: Boolean) = withContext(Dispatchers.IO) {
        articleDao.updateArticleSavedStatus(articleId, isSaved)
    }

    /**
     * Refresh articles from online sources
     */
    suspend fun refreshArticles(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Use the RssArticleService to fetch articles (removed fallback)
            val articles = rssArticleService.fetchArticles()

            // If no articles were found, return false to indicate failure
            if (articles.isEmpty()) {
                return@withContext false
            }

            // Keep saved status for existing articles
            val updatedArticles = articles.map { article ->
                val existing = articleDao.getArticleById(article.id)
                if (existing != null && existing.isSaved) {
                    article.copy(isSaved = true)
                } else {
                    article
                }
            }

            articleDao.insertArticles(updatedArticles)

            // Clean up old, unsaved articles (older than 30 days)
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -30)
            val thirtyDaysAgo = calendar.timeInMillis
            articleDao.deleteOldUnsavedArticles(thirtyDaysAgo)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
