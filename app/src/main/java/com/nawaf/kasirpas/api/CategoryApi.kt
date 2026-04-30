package com.nawaf.kasirpas.api

import com.nawaf.kasirpas.request.CategoryRequest
import com.nawaf.kasirpas.request.CategoryStatusRequest
import com.nawaf.kasirpas.response.*
import retrofit2.Response
import retrofit2.http.*

interface CategoryApi {
    @GET("categories")
    suspend fun getCategories(
        @Header("Authorization") token: String
    ): Response<CategoryResponse>

    @PATCH("categories/products")
    suspend fun getCategoriesWithProducts(
        @Header("Authorization") token: String
    ): Response<CategoryWithProductsResponse>

    @POST("categories")
    suspend fun storeCategory(
        @Header("Authorization") token: String,
        @Body request: CategoryRequest
    ): Response<SingleCategoryResponse>

    @GET("categories/{id}")
    suspend fun showCategory(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<SingleCategoryResponse>

    @PUT("categories/{id}")
    suspend fun updateCategory(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: CategoryRequest
    ): Response<SingleCategoryResponse>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<DeleteCategoryResponse>

    @PATCH("categories/{id}/status")
    suspend fun updateCategoryStatus(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: CategoryStatusRequest
    ): Response<SingleCategoryResponse>
}
