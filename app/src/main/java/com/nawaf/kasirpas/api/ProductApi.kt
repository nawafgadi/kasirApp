package com.nawaf.kasirpas.api

import com.nawaf.kasirpas.model.Product
import com.nawaf.kasirpas.request.ProductRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ProductApi {
    @POST("products")
    suspend fun createProduct(@Body request: ProductRequest): Response<Product>
}
