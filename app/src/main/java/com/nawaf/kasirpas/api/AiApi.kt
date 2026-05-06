package com.nawaf.kasirpas.api

import com.nawaf.kasirpas.response.AiBussyHoursResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface AiApi {
    @GET("ai/runs/latest/busy-hours")
    suspend fun getLatestBusyHours(
        @Header("Authorization") token: String
    ): Response<AiBussyHoursResponse>
}
