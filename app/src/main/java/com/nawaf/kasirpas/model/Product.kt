package com.nawaf.kasirpas.model

data class Product(
    val id: Int,
    val name: String,
    val price: String,
    val description: String,
    val created_at: String,
    val updated_at: String,
    val image_url: String,
    val category_id: Int,
    val deleted_at: String?,
    val is_active: Boolean,
    val user_id: Int,
    val category: Category,
    val stocks: List<Stock>
)
