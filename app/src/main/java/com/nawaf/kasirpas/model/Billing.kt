package com.nawaf.kasirpas.model

import com.google.gson.annotations.SerializedName

data class Subscription(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("plan_name")
    val planName: String,
    val price: String?,
    @SerializedName("duration_days")
    val durationDays: Int,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("end_date")
    val endDate: String,
    val status: String,
    val payments: List<Payment>? = null
)

data class Payment(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("subscription_id")
    val subscriptionId: Int,
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("gross_amount")
    val grossAmount: String?,
    @SerializedName("payment_type")
    val paymentType: String?,
    @SerializedName("transaction_time")
    val transactionTime: String?,
    val status: String
)
