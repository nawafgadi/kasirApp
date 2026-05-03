package com.nawaf.kasirpas.model

import com.google.gson.annotations.SerializedName

data class AiRun(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("type_ai") val typeAi: String,
    val status: String,
    @SerializedName("generated_at") val generatedAt: String,
    @SerializedName("busy_hour_daily_forecasts") val dailyForecasts: List<DailyForecast>
)

data class DailyForecast(
    val id: Int,
    @SerializedName("forecast_date") val forecastDate: String,
    @SerializedName("day_name") val dayName: String,
    @SerializedName("total_predicted_trx") val totalPredictedTrx: String,
    @SerializedName("total_predicted_revenue") val totalPredictedRevenue: String,
    @SerializedName("peak_hour") val peakHour: String,
    @SerializedName("hourly_predictions") val hourlyPredictions: List<HourlyPrediction>
)

data class HourlyPrediction(
    val id: Int,
    val hour: String,
    @SerializedName("predicted_transactions") val predictedTransactions: String,
    @SerializedName("predicted_revenue") val predictedRevenue: String,
    @SerializedName("busy_level") val busyLevel: String,
    val emoji: String,
    @SerializedName("product_predictions") val productPredictions: List<ProductPrediction>
)

data class ProductPrediction(
    val id: Int,
    @SerializedName("product_name") val productName: String,
    val probability: String,
    @SerializedName("estimated_qty") val estimatedQty: String,
    @SerializedName("estimated_revenue") val estimatedRevenue: String,
    val product: SimpleProduct?
)

data class SimpleProduct(
    val id: Int,
    val name: String,
    @SerializedName("image_url") val imageUrl: String?
)
