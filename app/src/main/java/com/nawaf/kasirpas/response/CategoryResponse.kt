package com.nawaf.kasirpas.response

import com.google.gson.annotations.SerializedName
import com.nawaf.kasirpas.model.Category
import com.nawaf.kasirpas.model.Product

data class CategoryResponse(
    val message: String,
    val data: List<Category>
)

data class SingleCategoryResponse(
    val message: String,
    val data: Category
)

data class CategoryWithProductsResponse(
    val message: String,
    val data: List<CategoryWithProducts>
)

data class CategoryWithProducts(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    val name: String,
    @SerializedName("isActive")
    val isActive: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    val products: List<Product>? = null
)

data class DeleteCategoryResponse(
    val message: String
)
