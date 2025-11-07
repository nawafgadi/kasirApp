package com.nawaf.kasirpas

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nawaf.kasirpas.databinding.ActivityPesananBinding

class PesananActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPesananBinding
    private var totalHarga = 30000 // Ini bisa diganti sesuai data dari keranjang

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPesananBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateTotal()

        // Mengatur listener untuk tombol kembali
        binding.btnBack.setOnClickListener {
            finish() // Menutup activity saat ini
        }

        // Mengatur listener untuk tombol bayar
        binding.btnBayar.setOnClickListener {
            Toast.makeText(this, "Pembayaran berhasil!", Toast.LENGTH_SHORT).show()
            // Di sini Anda bisa menambahkan logika untuk pindah ke activity lain
        }
    }

    private fun updateTotal() {
        // Format harga ke dalam format Rupiah (contoh: Rp30.000)
        val formattedPrice = "Rp${String.format("%,d", totalHarga).replace(',', '.')}"
        binding.txtTotalHarga.text = formattedPrice
        binding.txtTotalFooter.text = formattedPrice
    }
}
