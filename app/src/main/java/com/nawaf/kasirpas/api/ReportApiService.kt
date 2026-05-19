package com.nawaf.kasirpas.api

import com.nawaf.kasirpas.response.SalesHistoryResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.Url

interface ReportApiService {
    @GET("reports/sales-history")
    suspend fun getSalesHistory(
        @Header("Authorization") token: String,
        @Query("period") period: String? = "semua",
        @Query("per_page") perPage: Int? = 10,
        @Query("search") search: String? = "",
        @Query("page") page: Int? = 1
    ): Response<SalesHistoryResponse>

    @GET
    suspend fun getSalesHistoryNextPage(
        @Header("Authorization") token: String,
        @Url url: String
    ): Response<SalesHistoryResponse>
}
