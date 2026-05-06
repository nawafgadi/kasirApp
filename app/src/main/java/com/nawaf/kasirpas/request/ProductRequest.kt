package com.nawaf.kasirpas.request

import com.google.gson.annotations.SerializedName

data class ProductRequest(
    val name: String,
    val price: Double,
    val description: String? = null,
    val stock: Int,
    @SerializedName("category_id")
    val categoryId: Int? = null,
    val image: String? = null // This might need Multipart for actual image upload, but keeping it as String for now
)
