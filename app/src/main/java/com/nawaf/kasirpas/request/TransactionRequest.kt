package com.nawaf.kasirpas.request

import com.google.gson.annotations.SerializedName

data class TransactionRequest(
    @SerializedName("trx_type")
    val trxType: String = "SALE",
    @SerializedName("trx_date")
    val trxDate: String,
    @SerializedName("payment_method")
    val paymentMethod: String,
    @SerializedName("items")
    val items: List<TransactionItemRequest>
)

data class TransactionItemRequest(
    @SerializedName("product_id")
    val productId: Int,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("unit_price")
    val unitPrice: Double
)
