package com.nawaf.kasirpas.response

import com.nawaf.kasirpas.model.AiRun

data class AiBussyHoursResponse(
    val success: Boolean,
    val message: String,
    val data: AiRun?
)
