package com.nawaf.kasirpas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nawaf.kasirpas.databinding.ActivityKonfirmasiPembayaranBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KonfirmasiPembayaranActivity : AppCompatActivity() {
    private var binding: ActivityKonfirmasiPembayaranBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKonfirmasiPembayaranBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        // Ambil data dari Intent
        val intent = intent
        val totalHarga = intent.getIntExtra("TOTAL_HARGA", 0)
        val metodePembayaran = intent.getStringExtra("METODE_PEMBAYARAN")

        setupViews(totalHarga, metodePembayaran)

        binding!!.btnSelesai.setOnClickListener { v ->
            // Kembali ke halaman utama (Home) setelah selesai
            val homeIntent = Intent(
                this,
                HomeActivity::class.java
            )
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(homeIntent)
            finish() // Tutup semua activity di atas Home
        }
    }

    private fun setupViews(totalHarga: Int, metode: String?) {
        // Format Rupiah
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val hargaFormatted = formatRupiah.format(totalHarga.toLong()).replace(",00", "")

        // Format Tanggal
        val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID"))
        val tanggalFormatted = sdf.format(Date())

        // Set data ke TextViews
        binding!!.tanggalTransaksi.text = tanggalFormatted
        binding!!.totalPembayaran.text = hargaFormatted
        binding!!.metodePembayaran.text = "Metode: " + (metode ?: "N/A")

        // Mengisi data dummy lainnya
        binding!!.statusTransaksi.text = getString(R.string.status_transaksi_berhasil)
        binding!!.namaBarang.text = getString(R.string.nama_barang_dummy)
        binding!!.orderId.text = getString(R.string.order_id_dummy)
    }
}