package com.nawaf.kasirpas.model

import com.google.gson.annotations.SerializedName

data class Product(
    val id: Int? = null,
    val name: String,
    val price: Long,
    val stock: Int,
    val description: String? = null,
    val image: String? = null,
    @SerializedName("user_id")
    val userId: Int
)
