package com.nawaf.kasirpas.model

import com.google.gson.annotations.SerializedName

data class Category(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    val name: String,
    @SerializedName("is_active")
    val isActiveRaw: Any,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
) {
    val isActive: Int
        get() = when (isActiveRaw) {
            is Boolean -> if (isActiveRaw) 1 else 0
            is Number -> isActiveRaw.toInt()
            is String -> isActiveRaw.toString().toDoubleOrNull()?.toInt() ?: 0
            else -> 0
        }
}
