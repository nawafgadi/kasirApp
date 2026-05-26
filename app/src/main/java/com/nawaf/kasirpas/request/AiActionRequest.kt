package com.nawaf.kasirpas.request

import com.google.gson.annotations.SerializedName

data class AiActionRequest(
    @SerializedName("action_type") val actionType: String
)
