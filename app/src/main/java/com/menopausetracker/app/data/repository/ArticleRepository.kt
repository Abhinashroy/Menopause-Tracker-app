package com.menopausetracker.app.data.repository

import android.app.Application
import com.menopausetracker.app.data.db.ArticleDatabase
import com.menopausetracker.app.data.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ArticleRepository(application: Application) {
    private val database = ArticleDatabase.getInstance(application)
    private val articleDao = database.articleDao()

    // Hardcoded articles for demonstration
    private val hardcodedArticles = listOf(
        Article(
            id = "1",
            title = "Understanding Menopause Symptoms",
            content = "Menopause is a natural biological process that marks the end of a woman's reproductive years. Common symptoms include hot flashes, night sweats, mood changes, and sleep problems. Understanding these symptoms can help you better manage this transition period.",
            imageUrl = "https://example.com/menopause-symptoms.jpg"
        ),
        Article(
            id = "2",
            title = "Natural Remedies for Hot Flashes",
            content = "Hot flashes are one of the most common symptoms of menopause. This article explores various natural remedies that may help manage hot flashes, including lifestyle changes, dietary modifications, and herbal supplements.",
            imageUrl = "https://example.com/hot-flashes.jpg"
        ),
        Article(
            id = "3",
            title = "Managing Sleep During Menopause",
            content = "Sleep disturbances are common during menopause. Learn about the connection between menopause and sleep, and discover practical tips for improving your sleep quality during this transition.",
            imageUrl = "https://example.com/sleep-menopause.jpg"
        )
    )

    suspend fun getAllArticles(): List<Article> = withContext(Dispatchers.IO) {
        // In a real app, this would fetch from an API
        // For now, we'll use hardcoded articles
        val savedArticles = articleDao.getSavedArticles()
        hardcodedArticles.map { article ->
            article.copy(isSaved = savedArticles.any { it.id == article.id })
        }
    }

    suspend fun getSavedArticles(): List<Article> = withContext(Dispatchers.IO) {
        articleDao.getSavedArticles()
    }

    suspend fun toggleSaveArticle(article: Article) = withContext(Dispatchers.IO) {
        if (article.isSaved) {
            articleDao.deleteArticle(article)
        } else {
            articleDao.insertArticle(article)
        }
    }
} 