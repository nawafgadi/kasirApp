package com.nawaf.kasirpas

import android.content.Intent
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import coil.load
import coil.transform.CircleCropTransformation
import com.nawaf.kasirpas.activity.BillingActivity
import com.nawaf.kasirpas.activity.OnboardingActivity
import com.nawaf.kasirpas.activity.main.CheckoutFragment
import com.nawaf.kasirpas.activity.main.KasirFragment
import com.nawaf.kasirpas.activity.main.LaporanFragment
import com.nawaf.kasirpas.activity.main.PengaturanFragment
import com.nawaf.kasirpas.databinding.ActivityMainBinding
import com.nawaf.kasirpas.utils.PreferenceManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Memaksa ikon status bar berwarna gelap agar terlihat di background putih
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)
        
        prefManager = PreferenceManager(this)
        
        if (!prefManager.isOnboarded()) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Menangani Safe Area dengan benar
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Padding atas hanya untuk AppBarLayout agar konten tidak tertutup jam/baterai
            binding.appBarLayout.updatePadding(top = systemBars.top)
            
            // Padding bawah untuk BottomNav agar tidak tertutup navigasi gesture HP
            binding.bottomNavigation.updatePadding(bottom = systemBars.bottom)
            
            insets
        }

        setupBottomNavigation()
        setupHeader()
        
        if (savedInstanceState == null) {
            replaceFragment(KasirFragment(), false)
        }
    }

    private fun setupHeader() {
        binding.btnUpgrade.setOnClickListener {
            startActivity(Intent(this, BillingActivity::class.java))
        }

        // Load profile picture from URL using Coil
        binding.ivProfile.load("https://img.freepik.com/free-vector/businessman-character-avatar-isolated_24877-60111.jpg?semt=ais_hybrid&w=740&q=80") {
            crossfade(true)
            transformations(CircleCropTransformation())
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_kasir -> {
                    replaceFragment(KasirFragment())
                    true
                }
                R.id.nav_checkout -> {
                    replaceFragment(CheckoutFragment())
                    true
                }
                R.id.nav_laporan -> {
                    replaceFragment(LaporanFragment())
                    true
                }
                R.id.nav_pengaturan -> {
                    replaceFragment(PengaturanFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment, animate: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()
        if (animate) {
            transaction.setCustomAnimations(R.anim.fragment_fade_in, 0, 0, 0)
        }
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.commit()
    }
}
