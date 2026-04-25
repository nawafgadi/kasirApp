package com.nawaf.kasirpas.request

import com.google.gson.annotations.SerializedName

data class ProductRequest(
    val name: String,
    val price: Long,
    val stock: Int,
    val description: String? = null,
    @SerializedName("user_id")
    val userId: Int
)
