package com.nawaf.kasirpas.activity

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.LinearInterpolator
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.nawaf.kasirpas.MainActivity
import com.nawaf.kasirpas.api.RetrofitClient
import com.nawaf.kasirpas.databinding.ActivitySplashBinding
import com.nawaf.kasirpas.utils.PreferenceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var prefManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefManager = PreferenceManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        startAppFlow()
    }

    private fun startAppFlow() {
        val animator = ValueAnimator.ofInt(0, 100)
        animator.duration = 2000
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener { animation ->
            binding.loadingProgress.progress = animation.animatedValue as Int
        }
        animator.start()

        lifecycleScope.launch {
            delay(2000)
            checkSessionAndNavigate()
        }
    }

    private suspend fun checkSessionAndNavigate() {
        // 1. Cek apakah ada token dan status login
        val token = prefManager.getToken()
        if (token == null || !prefManager.isLogin()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 2. Verifikasi Session ke Backend
        try {
            val response = RetrofitClient.authApi.getSession("Bearer $token")
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.user != null) {
                    prefManager.saveUser(body.user)
                    // Jika login valid, baru cek onboarding
                    navigateToNext()
                } else {
                    navigateToLogin()
                }
            } else {
                navigateToLogin()
            }
        } catch (e: Exception) {
            // Jika error koneksi, tetap lanjut ke halaman berikutnya (mode offline)
            navigateToNext()
        } finally {
            finish()
        }
    }

    private fun navigateToNext() {
        if (!prefManager.isOnboarded()) {
            startActivity(Intent(this, OnboardingActivity::class.java))
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun navigateToLogin() {
        prefManager.setLogin(false)
        startActivity(Intent(this, LoginActivity::class.java))
    }
}
