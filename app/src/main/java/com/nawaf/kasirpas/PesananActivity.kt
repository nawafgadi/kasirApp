package com.nawaf.kasirpas

// PERBAIKAN 1: Import kelas Intent
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nawaf.kasirpas.databinding.ActivityPesananBinding
import java.text.NumberFormat
import java.util.Locale

class PesananActivity : AppCompatActivity() {
    private var binding: ActivityPesananBinding? = null
    private var totalHarga = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPesananBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        // Mengambil total harga dari Intent (dari KeranjangActivity)
        totalHarga = intent.getIntExtra("TOTAL_HARGA", 0)

        updateTotal()

        // Mengatur listener untuk tombol kembali
        binding!!.btnBack.setOnClickListener { v ->
            finish() // Menutup activity saat ini
        }

        // --- INI BAGIAN YANG DIPERBAIKI ---
        // Mengatur listener untuk tombol bayar
        binding!!.btnBayar.setOnClickListener { v ->
            // Hapus Toast yang lama
            // Toast.makeText(this, "Pembayaran berhasil!", Toast.LENGTH_SHORT).show();

            // PERBAIKAN 2: Buat Intent untuk pindah ke PembayaranActivity
            val intent = Intent(
                this,
                PembayaranActivity::class.java
            )


            // (Opsional tapi bagus) Kirim juga total harga ke halaman pembayaran
            intent.putExtra("TOTAL_HARGA", totalHarga)


            // PERBAIKAN 3: Jalankan perintah pindah halaman
            startActivity(intent)
        }
    }

    private fun updateTotal() {
        // Format harga ke dalam format Rupiah
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val formattedPrice = formatRupiah.format(totalHarga.toLong()).replace(",00", "")

        binding!!.txtTotalHarga.text = formattedPrice
        binding!!.txtTotalFooter.text = formattedPrice
    }
}