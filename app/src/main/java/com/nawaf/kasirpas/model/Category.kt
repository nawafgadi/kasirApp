package com.nawaf.kasirpas.model

data class Category(
    val id: Int,
    val user_id: Int,
    val name: String,
    val isActive: Int,
    val created_at: String,
    val updated_at: String
)
