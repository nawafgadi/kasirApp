package com.nawaf.kasirpas.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.nawaf.kasirpas.R
import com.nawaf.kasirpas.api.RetrofitClient
import com.nawaf.kasirpas.databinding.ActivityBillingBinding
import com.nawaf.kasirpas.request.BillingRequest
import com.nawaf.kasirpas.utils.PreferenceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BillingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBillingBinding
    private lateinit var preferenceManager: PreferenceManager
    private var loadingDialog: Dialog? = null

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

    private fun showLoading(message: String) {
        if (loadingDialog == null) {
            loadingDialog = Dialog(this).apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setContentView(R.layout.layout_loading)
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }
        loadingDialog?.findViewById<TextView>(R.id.tvLoadingMessage)?.text = message
        loadingDialog?.show()
    }

    private fun hideLoading() {
        loadingDialog?.dismiss()
    }

    private fun fetchActiveSubscription() {
        val token = preferenceManager.getToken() ?: return
        
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutContent.visibility = View.INVISIBLE
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.billingApi.getActiveSubscription("Bearer $token")
                if (response.isSuccessful) {
                    val activeSub = response.body()?.data
                    if (activeSub != null && activeSub.status == "ACTIVE") {
                        binding.cardActiveSubscription.visibility = View.VISIBLE
                        binding.layoutPlanSelection.visibility = View.GONE
                        binding.cardTrust.visibility = View.VISIBLE
                        
                        binding.tvActivePlanName.text = activeSub.planName
                        binding.tvActivePlanExpiry.text = formatExpiryDate(activeSub.endDate)
                        
                        binding.tvSweetMessage.text = "Terima kasih telah mempercayai kami! ✨\nBisnis Anda kini semakin kuat."
                        
                        val paymentMethod = activeSub.payments?.firstOrNull()?.paymentType ?: "-"
                        binding.tvPaymentMethod.text = paymentMethod.uppercase()
                    } else {
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
                binding.progressBar.visibility = View.GONE
                binding.layoutContent.visibility = View.VISIBLE
            }
        }
    }

    private fun formatExpiryDate(dateStr: String): String {
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

        showLoading("Menyiapkan pembayaran...")

        lifecycleScope.launch {
            try {
                val request = BillingRequest(planName)
                val response = RetrofitClient.billingApi.subscribe("Bearer $token", request)
                
                // Beri sedikit delay agar transisi tidak terlalu cepat (UX)
                delay(1000)
                hideLoading()

                if (response.isSuccessful && response.body() != null) {
                    val paymentUrl = response.body()!!.paymentUrl
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl))
                    startActivity(intent)
                } else {
                    Toast.makeText(this@BillingActivity, "Gagal membuat pesanan", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                hideLoading()
                Toast.makeText(this@BillingActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingDialog?.dismiss()
    }
}
