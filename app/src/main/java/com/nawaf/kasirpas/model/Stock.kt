package com.nawaf.kasirpas.model

import com.google.gson.annotations.SerializedName

data class Stock(
    val id: String?,
    @SerializedName("product_id")
    val productId: Int?,
    @SerializedName("stock_on_hand")
    val stockOnHand: Int?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    @SerializedName("deleted_at")
    val deletedAt: String?
)
