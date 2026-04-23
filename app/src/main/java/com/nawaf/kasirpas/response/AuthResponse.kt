package com.nawaf.kasirpas.response

import com.google.gson.annotations.SerializedName
import com.nawaf.kasirpas.model.User

data class AuthResponse(
    val message: String,
    val token: String?,
    @SerializedName("token_type")
    val tokenType: String?,
    val user: User?
)
