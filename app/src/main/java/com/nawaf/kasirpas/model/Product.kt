package com.nawaf.kasirpas.model

import com.google.gson.annotations.SerializedName

data class Product(
    val id: Int,
    val name: String,
    val price: String,
    val description: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("category_id")
    val categoryId: Int?,
    @SerializedName("deleted_at")
    val deletedAt: String?,
    @SerializedName("is_active")
    val isActive: Int?,
    @SerializedName("user_id")
    val userId: Int?,
    val category: Category?,
    val stocks: List<Stock>?
)
