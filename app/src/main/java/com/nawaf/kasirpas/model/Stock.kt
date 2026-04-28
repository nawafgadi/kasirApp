package com.nawaf.kasirpas.model

data class Stock(
    val id: String,
    val product_id: Int,
    val stock_on_hand: Int,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?
)
