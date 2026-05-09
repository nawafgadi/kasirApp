package com.nawaf.kasirpas.api

import com.nawaf.kasirpas.response.AiBussyHoursResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Header

interface AiApi {
    @GET("ai/runs/latest/busy-hours")
    suspend fun getLatestBusyHours(
        @Header("Authorization") token: String
    ): Response<AiBussyHoursResponse>

    @POST("ai/runs/analyze-busy-hours")
    suspend fun analyzeBusyHours(
        @Header("Authorization") token: String,
        @Body body: RequestBody
    ): Response<AiBussyHoursResponse> // Sesuaikan return type ini jika backend mengembalikan struktur berbeda saat nge-post.
}
