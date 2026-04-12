package com.nawaf.kasirpas.response

import com.nawaf.kasirpas.model.User

data class AuthResponse(
    val token: String,
    val message: String,
    val user: User
)
