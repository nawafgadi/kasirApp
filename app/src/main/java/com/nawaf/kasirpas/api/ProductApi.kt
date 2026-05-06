package com.nawaf.kasirpas.api

import com.nawaf.kasirpas.request.ProductRequest
import com.nawaf.kasirpas.response.ProductResponse
import com.nawaf.kasirpas.response.SingleProductResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ProductApi {
    @GET("products")
    suspend fun getProducts(
        @Header("Authorization") token: String
    ): Response<ProductResponse>

    @Multipart
    @POST("products")
    suspend fun storeProduct(
        @Header("Authorization") token: String,
        @Part("name") name: RequestBody,
        @Part("price") price: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("stock") stock: RequestBody,
        @Part("category_id") categoryId: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Response<SingleProductResponse>

    @Multipart
    @POST("products/{id}")
    suspend fun updateProduct(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Part("_method") method: RequestBody,
        @Part("name") name: RequestBody,
        @Part("price") price: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("stock") stock: RequestBody,
        @Part("category_id") categoryId: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Response<SingleProductResponse>

    @DELETE("products/{id}")
    suspend fun deleteProduct(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Void>
}
