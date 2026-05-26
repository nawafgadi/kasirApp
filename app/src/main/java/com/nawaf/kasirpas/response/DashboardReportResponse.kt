package com.nawaf.kasirpas.response

import com.google.gson.annotations.SerializedName

data class DashboardReportResponse(
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: DashboardReportData
)

data class DashboardReportData(
    @SerializedName("hari_ini") val hariIni: ReportSummary,
    @SerializedName("minggu_ini") val mingguIni: ReportSummary,
    @SerializedName("bulan_ini") val bulanIni: ReportSummary,
    @SerializedName("tahun_ini") val tahunIni: ReportSummary,
    @SerializedName("sepanjang_masa") val sepanjangMasa: ReportSummary,
    @SerializedName("grafik_data") val grafikData: GrafikData
)

data class ReportSummary(
    @SerializedName("total_pendapatan") val totalPendapatan: Double,
    @SerializedName("pendapatan_vs_sebelumnya") val pendapatanVsSebelumnya: PendapatanVsSebelumnya?,
    @SerializedName("total_transaksi") val totalTransaksi: Int,
    @SerializedName("rata_rata_keranjang") val rataRataKeranjang: Double,
    @SerializedName("tren_penjualan") val trenPenjualan: List<TrenPenjualan>,
    @SerializedName("produk_terlaris") val produkTerlaris: List<ProdukTerlaris>,
    @SerializedName("transaksi_terakhir") val transaksiTerakhir: List<TransaksiTerakhir>
)

data class PendapatanVsSebelumnya(
    @SerializedName("nilai_sebelumnya") val nilaiSebelumnya: Double,
    @SerializedName("persentase_perubahan") val persentasePerubahan: Double
)

data class TrenPenjualan(
    @SerializedName("date") val date: String,
    @SerializedName("total") val total: Double
)

data class ProdukTerlaris(
    @SerializedName("name") val name: String,
    @SerializedName("total_quantity") val totalQuantity: Int
)

data class TransaksiTerakhir(
    @SerializedName("id") val id: Int,
    @SerializedName("trx_type") val trxType: String,
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("trx_date") val trxDate: String
)

data class GrafikData(
    @SerializedName("minggu_ini") val mingguIni: GrafikPeriod,
    @SerializedName("bulan_ini") val bulanIni: GrafikPeriod,
    @SerializedName("tahun_ini") val tahunIni: GrafikPeriod
)

data class GrafikPeriod(
    @SerializedName("labels") val labels: List<String>,
    @SerializedName("values") val values: List<Double>
)
