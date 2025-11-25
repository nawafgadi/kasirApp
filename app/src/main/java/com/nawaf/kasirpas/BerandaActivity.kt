package com.nawaf.kasirpas

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class BerandaActivity : AppCompatActivity() {

    // Kunci untuk Intent, praktik terbaik untuk menghindari salah ketik
    companion object {
        const val EXTRA_NAMA_MAKANAN = "extra_nama_makanan"
        const val EXTRA_HARGA_MAKANAN = "extra_harga_makanan"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beranda)

        // Panggil fungsi untuk mengatur semua listener klik
        setupListeners()
    }

    private fun setupListeners() {
        // --- LISTENER UNTUK TOMBOL PLUS PRODUK ---
        val addBerasButton = findViewById<ImageButton>(R.id.add_button1)

        // Definisikan data untuk produk ini
        val namaProduk = "Beras Premium"
        val hargaProduk = 15000

        // Pasang listener HANYA pada tombol plus
        addBerasButton.setOnClickListener {
            // Panggil fungsi yang tugasnya hanya pindah halaman
            openDetailActivity(namaProduk, hargaProduk)
        }

        // --- LISTENER UNTUK FOOTER ---
        val navHome = findViewById<ImageView>(R.id.navHome)
        val navGrafik = findViewById<ImageView>(R.id.navGrafik)
        val navOrder = findViewById<ImageView>(R.id.navOrder)
        val navProfile = findViewById<ImageView>(R.id.navProfile)

        navHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        navGrafik.setOnClickListener {
            Toast.makeText(this, "Navigasi ke Laporan...", Toast.LENGTH_SHORT).show()
        }
        navOrder.setOnClickListener {
            Toast.makeText(this, "Navigasi ke Order...", Toast.LENGTH_SHORT).show()
        }
        navProfile.setOnClickListener {
            Toast.makeText(this, "Navigasi ke Profil...", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Fungsi ini bertanggung jawab untuk membuka halaman detail makanan
     * sambil membawa data produk.
     */
    private fun openDetailActivity(nama: String, harga: Int) {
        val intent = Intent(this, detailMakananActivity::class.java)
        intent.putExtra(EXTRA_NAMA_MAKANAN, nama)
        intent.putExtra(EXTRA_HARGA_MAKANAN, harga)
        startActivity(intent)
    }
}
