package com.nawaf.kasirpas.api

import com.nawaf.kasirpas.request.TransactionRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface TransactionApi {
    @POST("transactions")
    suspend fun storeTransaction(
        @Header("Authorization") token: String,
        @Body request: TransactionRequest
    ): Response<ResponseBody>
}
