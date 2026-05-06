package com.nawaf.kasirpas.model

import com.google.gson.annotations.SerializedName

data class Category(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    val name: String,
    @SerializedName("is_active")
    val isActive: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)
