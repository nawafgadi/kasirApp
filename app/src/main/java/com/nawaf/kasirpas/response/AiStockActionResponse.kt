package com.nawaf.kasirpas.response

import com.nawaf.kasirpas.model.AiRecommendation
import com.nawaf.kasirpas.model.AiRecommendationAction

data class AiStockActionResponse(
    val success: Boolean,
    val message: String,
    val data: AiStockActionData?
)

data class AiStockActionData(
    val action: AiRecommendationAction?,
    val recommendation: AiRecommendation?
)
