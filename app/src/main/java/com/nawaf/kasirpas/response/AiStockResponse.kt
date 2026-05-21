package com.nawaf.kasirpas.response

import com.nawaf.kasirpas.model.AiStockRun

data class AiStockResponse(
    val success: Boolean,
    val message: String,
    val data: AiStockRun?
)
