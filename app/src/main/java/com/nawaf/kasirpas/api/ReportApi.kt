package com.nawaf.kasirpas.api

import com.nawaf.kasirpas.response.SalesHistoryResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ReportApi {
    @GET("reports/sales-history")
    suspend fun getSalesHistory(
        @Header("Authorization") token: String,
        @Query("page") page: Int? = null,
        @Query("search") search: String? = null,
        @Query("filter") filter: String? = null
    ): Response<SalesHistoryResponse>
}
