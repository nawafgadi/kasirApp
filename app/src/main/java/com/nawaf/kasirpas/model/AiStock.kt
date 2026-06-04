package com.nawaf.kasirpas.model

import com.google.gson.annotations.SerializedName

data class AiStockRun(
    val id: String,
    @SerializedName("user_id") val userId: String,
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
    val summary: String? = null,
    val detail: String? = null
)

data class AiRecommendation(
    val id: Int,
    @SerializedName("ai_run_id") val aiRunId: String,
    @SerializedName("product_id") val productId: String,
    @SerializedName("product_name") val productName: String,
    @SerializedName("current_stock") val currentStock: Int,
    @SerializedName("avg_daily_sales") val avgDailySales: Float?,
    @SerializedName("recommed_restok_qty") val recommendRestockQty: Int,
    @SerializedName("restock_min") val restockMin: Int?,
    @SerializedName("restock_max") val restockMax: Int?,
    @SerializedName("restock_label") val restockLabel: String?,
    @SerializedName("risk_level") val riskLevel: String?,
    @SerializedName("urgency_description") val urgencyDescription: String?,
    @SerializedName("days_until_emty") val daysUntilEmpty: Int?,
    @SerializedName("estimated_emty_date") val estimatedEmptyDate: String?,
    val risk: String?,
    val description: String?,
    @SerializedName("risk_point") val riskPoint: Int,
    @SerializedName("seasonal_min") val seasonalMin: Int?,
    @SerializedName("seasonal_max") val seasonalMax: Int?,
    @SerializedName("seasonal_label") val seasonalLabel: String?,
    @SerializedName("seasonal_holiday") val seasonalHoliday: String?,
    @SerializedName("seasonal_reason") val seasonalReason: String?,
    @SerializedName("selected_stocks") val selectedStocks: List<AiStockEntry>?,
    @SerializedName("selected_seasonal_stocks") val selectedSeasonalStocks: List<AiStockEntry>?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    val product: SimpleProductStock?,
    @SerializedName("ai_recommendation_actions") val aiRecommendationActions: List<AiRecommendationAction>?
)

data class AiStockEntry(
    val id: String,
    @SerializedName("product_id") val productId: String,
    @SerializedName("stock_on_hand") val stockOnHand: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class SimpleProductStock(
    val id: String,
    @SerializedName("user_id") val userId: String?,
    val name: String,
    val price: String?,
    val description: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("is_active") val isActive: Boolean?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    val stocks: List<AiStockEntry>?
)

data class AiRecommendationAction(
    val id: Int,
    @SerializedName("ai_recommendation_id") val aiRecommendationId: Int,
    @SerializedName("action_type") val actionType: String,
    @SerializedName("action_at") val actionAt: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)
