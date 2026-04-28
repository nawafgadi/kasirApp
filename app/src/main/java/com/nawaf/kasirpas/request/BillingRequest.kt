package com.nawaf.kasirpas.request

import com.google.gson.annotations.SerializedName

data class BillingRequest(
    @SerializedName("plan_name")
    val planName: String
)
