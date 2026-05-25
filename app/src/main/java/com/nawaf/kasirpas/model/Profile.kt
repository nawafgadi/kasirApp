package com.nawaf.kasirpas.model

import com.google.gson.annotations.SerializedName

data class Profile(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    val bio: String?,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    @SerializedName("ai_portfolio")
    val aiPortfolio: AiPortfolio?
)

data class AiPortfolio(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("type_ai")
    val typeAi: String?,
    val status: String?,
    @SerializedName("generated_at")
    val generatedAt: String?,
    @SerializedName("error_message")
    val errorMessage: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    @SerializedName("portfolio_insight")
    val portfolioInsight: PortfolioInsight?
)

data class PortfolioInsight(
    val id: Int,
    @SerializedName("ai_run_id")
    val aiRunId: Int,
    @SerializedName("user_id")
    val userId: Int,
    val insight: String?,
    @SerializedName("tanggal_laporan")
    val tanggalLaporan: String?,
    val periode: String?,
    @SerializedName("total_omset_minggu_ini")
    val totalOmsetMingguIni: String?,
    @SerializedName("total_transaksi")
    val totalTransaksi: Int?,
    @SerializedName("rata_rata_transaksi_per_hari")
    val rataRataTransaksiPerHari: String?,
    @SerializedName("rata_rata_omset_per_hari")
    val rataRataOmsetPerHari: String?,
    @SerializedName("bintang_warung")
    val bintangWarung: List<BintangWarung>?,
    @SerializedName("hari_ramai_tanggal")
    val hariRamaiTanggal: String?,
    @SerializedName("hari_ramai_omset")
    val hariRamaiOmset: String?,
    @SerializedName("produk_kurang_laku")
    val produkKurangLaku: List<String>?,
    val source: String?,
    @SerializedName("generated_at")
    val generatedAt: String?,
    @SerializedName("valid_until")
    val validUntil: String?
)

data class BintangWarung(
    val nama: String,
    val terjual: Int,
    val omset: Double
)
