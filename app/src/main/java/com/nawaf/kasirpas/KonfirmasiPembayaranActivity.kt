package com.nawaf.kasirpas

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nawaf.kasirpas.databinding.ActivityKonfirmasiPembayaranBinding

class KonfirmasiPembayaranActivity : AppCompatActivity() {

    // Deklarasikan variabel binding untuk mengakses view dengan aman
    private lateinit var binding: ActivityKonfirmasiPembayaranBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate layout menggunakan View Binding
        binding = ActivityKonfirmasiPembayaranBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mengisi tampilan dengan data dari string resources
        setupViews()

        // Mengatur listener untuk tombol selesai
        binding.btnSelesai.setOnClickListener {
            Toast.makeText(this, "Transaksi selesai!", Toast.LENGTH_SHORT).show()
            finish() // Menutup activity saat ini
        }
    }

    private fun setupViews() {
        // Menggunakan binding untuk mengakses elemen UI, lebih aman dari findViewById
        binding.tanggalTransaksi.text = getString(R.string.tanggal_transaksi_dummy)
        binding.statusTransaksi.text = getString(R.string.status_transaksi_berhasil)
        binding.namaBarang.text = getString(R.string.nama_barang_dummy)
        binding.orderId.text = getString(R.string.order_id_dummy)
        binding.totalPembayaran.text = getString(R.string.total_pembayaran_dummy)
        binding.metodePembayaran.text = getString(R.string.metode_pembayaran_dummy)
    }
}
