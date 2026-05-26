package com.nawaf.kasirpas.api

import com.nawaf.kasirpas.response.AiBussyHoursResponse
import com.nawaf.kasirpas.response.AiStockResponse
import com.nawaf.kasirpas.response.AiStockActionResponse
import com.nawaf.kasirpas.request.AiActionRequest
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Header
import retrofit2.http.Path

interface AiApi {
    @GET("ai/runs/latest/busy-hours")
    suspend fun getLatestBusyHours(
        @Header("Authorization") token: String
    ): Response<AiBussyHoursResponse>

    @POST("ai/runs/analyze-busy-hours")
    suspend fun analyzeBusyHours(
        @Header("Authorization") token: String,
        @Body body: RequestBody
    ): Response<AiBussyHoursResponse>

    @GET("ai/runs/latest/stocks")
    suspend fun getLatestStocks(
        @Header("Authorization") token: String
    ): Response<AiStockResponse>

    @POST("ai/runs/analyze")
    suspend fun analyzeStocks(
        @Header("Authorization") token: String
    ): Response<AiStockResponse>

    @PATCH("ai/recommendations/{recommendationId}/action")
    suspend fun updateRecommendationAction(
        @Header("Authorization") token: String,
        @Path("recommendationId") recommendationId: Int,
        @Body body: AiActionRequest
    ): Response<AiStockActionResponse>
}


