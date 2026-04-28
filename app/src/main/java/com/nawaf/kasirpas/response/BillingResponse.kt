package com.nawaf.kasirpas.response

import com.google.gson.annotations.SerializedName
import com.nawaf.kasirpas.model.Payment
import com.nawaf.kasirpas.model.Subscription

data class SubscribeResponse(
    val message: String,
    val subscription: Subscription,
    val payment: Payment,
    @SerializedName("payment_url")
    val paymentUrl: String
)

data class ActiveSubscriptionResponse(
    val message: String,
    val data: Subscription? // Sesuai JSON terbaru, data adalah objek tunggal, bukan List
)
