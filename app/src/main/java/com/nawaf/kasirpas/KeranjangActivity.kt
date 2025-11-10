package com.nawaf.kasirpas

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nawaf.kasirpas.databinding.ActivityKeranjangBinding

class KeranjangActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKeranjangBinding
    private var quantity = 2
    private val hargaSatuan = 15000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeranjangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateTotal()

        binding.btnPlus.setOnClickListener {
            quantity++
            updateTotal()
        }

        binding.btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                updateTotal()
            }
        }

        binding.btnPesan.setOnClickListener {
            // Logika pemesanan, misal ke halaman pembayaran
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun updateTotal() {
        binding.txtQuantity.text = quantity.toString()
        val total = quantity * hargaSatuan
        binding.totalPrice.text = "Rp${String.format("%,d", total).replace(',', '.')}"
    }
}
