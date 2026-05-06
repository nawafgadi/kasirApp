package com.nawaf.kasirpas.api

import com.nawaf.kasirpas.request.ProductRequest
import com.nawaf.kasirpas.response.ProductResponse
import com.nawaf.kasirpas.response.SingleProductResponse
import retrofit2.Response
import retrofit2.http.*

interface ProductApi {
    @GET("products")
    suspend fun getProducts(
        @Header("Authorization") token: String
    ): Response<ProductResponse>

    @POST("products")
    suspend fun storeProduct(
        @Header("Authorization") token: String,
        @Body request: ProductRequest
    ): Response<SingleProductResponse>

    @PUT("products/{id}")
    suspend fun updateProduct(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: ProductRequest
    ): Response<SingleProductResponse>

    @DELETE("products/{id}")
    suspend fun deleteProduct(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Void>
}
