package com.nawaf.kasirpas.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.nawaf.kasirpas.api.RetrofitClient
import com.nawaf.kasirpas.databinding.ActivityBillingBinding
import com.nawaf.kasirpas.request.BillingRequest
import com.nawaf.kasirpas.utils.PreferenceManager
import kotlinx.coroutines.launch

class BillingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBillingBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)
        binding = ActivityBillingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbar.updatePadding(top = 0)
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupListeners()
        fetchActiveSubscription()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSubscribePro.setOnClickListener {
            subscribe("PRO")
        }

        binding.btnSubscribeProMax.setOnClickListener {
            subscribe("PRO_MAX")
        }
    }

    private fun fetchActiveSubscription() {
        val token = preferenceManager.getToken() ?: return
        
        // Show progress bar and hide content to avoid flicker
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutContent.visibility = View.INVISIBLE
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.billingApi.getActiveSubscription("Bearer $token")
                if (response.isSuccessful) {
                    val activeSub = response.body()?.data
                    if (activeSub != null && activeSub.status == "ACTIVE") {
                        // Tampilkan Kartu Aktif dengan Detail
                        binding.cardActiveSubscription.visibility = View.VISIBLE
                        binding.layoutPlanSelection.visibility = View.GONE
                        binding.cardTrust.visibility = View.VISIBLE
                        
                        binding.tvActivePlanName.text = activeSub.planName
                        binding.tvActivePlanExpiry.text = formatExpiryDate(activeSub.endDate)
                        
                        // Pesan manis
                        binding.tvSweetMessage.text = "Terima kasih telah mempercayai kami! ✨\nBisnis Anda kini semakin kuat."
                        
                        // Ambil metode pembayaran dari transaksi terakhir
                        val paymentMethod = activeSub.payments?.firstOrNull()?.paymentType ?: "-"
                        binding.tvPaymentMethod.text = paymentMethod.uppercase()
                    } else {
                        // Tidak ada langganan aktif, tampilkan pilihan paket
                        binding.cardActiveSubscription.visibility = View.GONE
                        binding.layoutPlanSelection.visibility = View.VISIBLE
                        binding.cardTrust.visibility = View.VISIBLE
                    }
                } else {
                    binding.cardActiveSubscription.visibility = View.GONE
                    binding.layoutPlanSelection.visibility = View.VISIBLE
                    binding.cardTrust.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                binding.cardActiveSubscription.visibility = View.GONE
                binding.layoutPlanSelection.visibility = View.VISIBLE
                binding.cardTrust.visibility = View.VISIBLE
            } finally {
                // Hide progress bar and show content after data is processed
                binding.progressBar.visibility = View.GONE
                binding.layoutContent.visibility = View.VISIBLE
            }
        }
    }

    private fun formatExpiryDate(dateStr: String): String {
        // Contoh sederhana: 2026-05-28 -> 28 Mei 2026
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val outputFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("id", "ID"))
            val date = inputFormat.parse(dateStr)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun subscribe(planName: String) {
        val token = preferenceManager.getToken()
        if (token == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val request = BillingRequest(planName)
                val response = RetrofitClient.billingApi.subscribe("Bearer $token", request)
                
                if (response.isSuccessful && response.body() != null) {
                    val paymentUrl = response.body()!!.paymentUrl
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl))
                    startActivity(intent)
                    Toast.makeText(this@BillingActivity, "Membuka halaman pembayaran...", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@BillingActivity, "Gagal membuat pesanan", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@BillingActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
