package com.nawaf.kasirpas.response

import com.nawaf.kasirpas.model.Profile

data class ProfileResponse(
    val message: String,
    val data: Profile?
)

data class ProfileDeleteResponse(
    val message: String
)
