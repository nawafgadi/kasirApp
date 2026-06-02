package com.nawaf.kasirpas.model

import com.google.gson.annotations.SerializedName

data class AiStockRun(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("type_ai") val typeAi: String,
    val status: String,
    @SerializedName("generated_at") val generatedAt: String,
    @SerializedName("error_message") val errorMessage: String?,
    @SerializedName("seasonal_insight") val seasonalInsight: SeasonalInsight?,
    @SerializedName("total_products") val totalProducts: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("ai_recommendations") val aiRecommendations: List<AiRecommendation>
)

data class SeasonalInsight(
    @SerializedName("seasonal_advice") val seasonalAdvice: String? = null,
    @SerializedName("has_upcoming_holiday") val hasUpcomingHoliday: Boolean? = null,
    @SerializedName("upcoming_holidays") val upcomingHolidays: List<UpcomingHoliday>? = null,
    val source: String? = null,
    val insightOverride: String? = null,
    val trendsOverride: List<String>? = null
) {
    val insight: String?
        get() = seasonalAdvice ?: insightOverride

    val trends: List<String>?
        get() = upcomingHolidays?.map { "${it.name} (${it.daysAway} hari)" } ?: trendsOverride
}

data class UpcomingHoliday(
    val date: String,
    val name: String,
    @SerializedName("days_away") val daysAway: Int,
    val impact: String
)

data class SeasonalRecommendation(
    val id: Int,
    @SerializedName("ai_recommendation_id") val aiRecommendationId: Int,
    val min: Int?,
    val max: Int?,
    val label: String?,
    val holiday: String?,
    val reason: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

data class AiRecommendation(
    val id: Int,
    @SerializedName("ai_run_id") val aiRunId: Int,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("product_name") val productName: String,
    @SerializedName("product_price") val productPrice: String,
    @SerializedName("current_stock") val currentStock: Int,
    @SerializedName("avg_daily_sales") val avgDailySales: String,
    @SerializedName("recommed_restok_qty") val recommendRestockQty: Int,
    @SerializedName("restock_min") val restockMin: Int?,
    @SerializedName("restock_max") val restockMax: Int?,
    @SerializedName("restock_label") val restockLabel: String?,
    @SerializedName("target_days_coverage") val targetDaysCoverage: Int?,
    @SerializedName("risk_level") val riskLevel: String?,
    @SerializedName("urgency_description") val urgencyDescription: String?,
    @SerializedName("days_until_emty") val daysUntilEmpty: Int?,
    @SerializedName("estimated_emty_date") val estimatedEmptyDate: String?,
    val risk: String?,
    val description: String?,
    @SerializedName("risk_point") val riskPoint: Int,
    @SerializedName("stock_timeline") val stockTimeline: List<StockTimeline>?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val product: SimpleProductStock?,
    @SerializedName("ai_recommendation_actions") val aiRecommendationActions: List<AiRecommendationAction>?,
    @SerializedName("seasonal_recommendation") val seasonalRecommendation: SeasonalRecommendation? = null
)

data class StockTimeline(
    val date: String,
    @SerializedName("projected_stock") val projectedStock: Int
)

data class SimpleProductStock(
    val id: Int,
    @SerializedName("category_id") val categoryId: Int?,
    val name: String,
    val sku: String?,
    val price: String?,
    val cost: String?,
    val status: String?
)

data class AiRecommendationAction(
    val id: Int,
    @SerializedName("ai_recommendation_id") val aiRecommendationId: Int,
    @SerializedName("action_type") val actionType: String, // "DONE" or "IGNORE"
    @SerializedName("action_at") val actionAt: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)
