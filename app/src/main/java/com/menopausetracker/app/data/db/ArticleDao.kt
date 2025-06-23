package com.menopausetracker.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.menopausetracker.app.data.model.Article
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Article entities
 */
@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles WHERE isSaved = 1 ORDER BY publishDate DESC")
    fun getSavedArticles(): Flow<List<Article>>

    @Query("SELECT * FROM articles ORDER BY publishDate DESC LIMIT :limit")
    fun getRecentArticles(limit: Int): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE category = :category ORDER BY publishDate DESC")
    fun getArticlesByCategory(category: String): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE id = :id")
    suspend fun getArticleById(id: String): Article?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<Article>)

    @Update
    suspend fun updateArticle(article: Article)

    @Query("UPDATE articles SET isSaved = :isSaved WHERE id = :id")
    suspend fun updateArticleSavedStatus(id: String, isSaved: Boolean)

    @Query("DELETE FROM articles WHERE isSaved = 0 AND lastFetchDate < :olderThan")
    suspend fun deleteOldUnsavedArticles(olderThan: Long)

    /**
     * Clear all saved articles by setting their isSaved status to false
     */
    @Query("UPDATE articles SET isSaved = 0 WHERE isSaved = 1")
    suspend fun clearSavedArticles()
}
