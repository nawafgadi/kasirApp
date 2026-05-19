package com.nawaf.kasirpas.response

import com.google.gson.annotations.SerializedName

data class SalesHistoryResponse(
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: SalesHistoryData
)

data class SalesHistoryData(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("data") val transactions: List<TransactionHistory>,
    @SerializedName("first_page_url") val firstPageUrl: String?,
    @SerializedName("from") val from: Int?,
    @SerializedName("last_page") val lastPage: Int,
    @SerializedName("last_page_url") val lastPageUrl: String?,
    @SerializedName("next_page_url") val nextPageUrl: String?,
    @SerializedName("path") val path: String?,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("prev_page_url") val prevPageUrl: String?,
    @SerializedName("to") val to: Int?,
    @SerializedName("total") val total: Int
)

data class TransactionHistory(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("trx_type") val trxType: String,
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("trx_date") val trxDate: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("user") val user: UserHistory,
    @SerializedName("items") val items: List<TransactionItemHistory>
)

data class UserHistory(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String
)

data class TransactionItemHistory(
    @SerializedName("id") val id: Int,
    @SerializedName("transaction_id") val transactionId: Int,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("subtotal") val subtotal: Double,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("product") val product: ProductHistory
)

data class ProductHistory(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double,
    @SerializedName("category_id") val categoryId: Int
)
