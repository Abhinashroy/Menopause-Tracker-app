package com.menopausetracker.app.data.api

import retrofit2.http.Body
import retrofit2.http.POST

interface RecommendationApi {
    @POST("recommendations")
    suspend fun getRecommendations(@Body request: RecommendationRequest): RecommendationResponse
}

data class RecommendationRequest(
    val symptoms: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class RecommendationResponse(
    val recommendation: String,
    val timestamp: Long
) 