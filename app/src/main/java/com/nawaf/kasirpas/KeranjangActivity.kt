package com.nawaf.kasirpas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nawaf.kasirpas.databinding.ActivityKeranjangBinding
import java.text.NumberFormat
import java.util.Locale

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
            val intent = Intent(this, PesananActivity::class.java)
            val total = quantity * hargaSatuan
            intent.putExtra("TOTAL_HARGA", total)
            startActivity(intent)
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun updateTotal() {
        binding.txtQuantity.text = quantity.toString()
        val total = quantity * hargaSatuan

        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val hargaRupiah = formatRupiah.format(total.toLong()).replace(",00", "")

        binding.totalPrice.text = hargaRupiah
    }
}
