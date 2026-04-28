package com.nawaf.kasirpas.api

import com.nawaf.kasirpas.response.ProductResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface ProductApi {
    @GET("products")
    suspend fun getProducts(
        @Header("Authorization") token: String
    ): Response<ProductResponse>
}
