package com.nawaf.kasirpas.request

import com.google.gson.annotations.SerializedName

data class CategoryRequest(
    val name: String,
    val description: String? = null
)

data class CategoryStatusRequest(
    @SerializedName("is_active")
    val isActive: Boolean
)
