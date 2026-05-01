package com.nawaf.kasirpas

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.nawaf.kasirpas.activity.BillingActivity
import com.nawaf.kasirpas.activity.OnboardingActivity
import com.nawaf.kasirpas.activity.main.CheckoutFragment
import com.nawaf.kasirpas.activity.main.KasirFragment
import com.nawaf.kasirpas.activity.main.LaporanFragment
import com.nawaf.kasirpas.activity.main.PengaturanFragment
import com.nawaf.kasirpas.api.RetrofitClient
import com.nawaf.kasirpas.databinding.ActivityMainBinding
import com.nawaf.kasirpas.utils.PreferenceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Random
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefManager: PreferenceManager
    private var particleJob: Job? = null
    private val random = Random()

    override fun onCreate(savedInstanceState: Bundle?) {
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
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.appBarLayout.updatePadding(top = systemBars.top)
            binding.bottomNavigation.updatePadding(bottom = systemBars.bottom)
            insets
        }

        setupBottomNavigation()
        setupHeader()
        checkActiveSubscription()
        
        if (savedInstanceState == null) {
            // Initial fragment
            setSelectedTab(R.id.nav_kasir)
        }
    }

    private fun setupHeader() {
        binding.btnUpgrade.setOnClickListener {
            startActivity(Intent(this, BillingActivity::class.java))
        }

        binding.ivProfile.load("https://img.freepik.com/free-vector/businessman-character-avatar-isolated_24877-60111.jpg?semt=ais_hybrid&w=740&q=80") {
            crossfade(true)
            transformations(CircleCropTransformation())
        }
    }

    private fun checkActiveSubscription() {
        val token = prefManager.getToken() ?: return
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.billingApi.getActiveSubscription("Bearer $token")
                if (response.isSuccessful) {
                    val activeSub = response.body()?.data
                    if (activeSub != null && activeSub.status == "ACTIVE") {
                        val remainingDays = calculateRemainingDays(activeSub.endDate)
                        if (remainingDays >= 0) {
                            binding.btnUpgrade.text = "$remainingDays Hari Lagi"
                            startDustParticles()
                        } else {
                            binding.btnUpgrade.text = "Upgrade"
                            stopDustParticles()
                        }
                    } else {
                        binding.btnUpgrade.text = "Upgrade"
                        stopDustParticles()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startDustParticles() {
        if (particleJob != null) return
        
        particleJob = lifecycleScope.launch {
            while (isActive) {
                createParticle()
                delay(300) // Create new particle every 300ms
            }
        }
    }

    private fun stopDustParticles() {
        particleJob?.cancel()
        particleJob = null
        binding.particleContainer.removeAllViews()
    }

    private fun createParticle() {
        val particle = ImageView(this).apply {
            setImageResource(R.drawable.ic_particle)
            alpha = 0f
            scaleX = 0.5f + random.nextFloat()
            scaleY = scaleX
        }

        val size = (10 * resources.displayMetrics.density).toInt()
        val params = ViewGroup.LayoutParams(size, size)
        binding.particleContainer.addView(particle, params)

        // Random starting position around the button
        val containerWidth = binding.containerUpgrade.width
        val containerHeight = binding.containerUpgrade.height
        
        val startX = random.nextInt(containerWidth).toFloat()
        val startY = random.nextInt(containerHeight).toFloat()
        
        particle.x = startX
        particle.y = startY

        // Animation: Fly upwards and fade out
        val flyUp = ObjectAnimator.ofPropertyValuesHolder(
            particle,
            PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, particle.translationY, particle.translationY - 100f - random.nextInt(100)),
            PropertyValuesHolder.ofFloat(View.TRANSLATION_X, particle.translationX, particle.translationX + (random.nextInt(100) - 50).toFloat()),
            PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 0.8f, 0f),
            PropertyValuesHolder.ofFloat(View.SCALE_X, particle.scaleX, 0f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, particle.scaleY, 0f)
        ).apply {
            duration = 1500 + random.nextInt(1000).toLong()
            interpolator = AccelerateDecelerateInterpolator()
        }

        flyUp.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                binding.particleContainer.removeView(particle)
            }
        })
        
        flyUp.start()
    }

    private fun calculateRemainingDays(endDateStr: String): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val endDate = sdf.parse(endDateStr)
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            
            if (endDate != null) {
                val diffInMillis = endDate.time - today.time
                TimeUnit.MILLISECONDS.toDays(diffInMillis)
            } else 0
        } catch (e: Exception) {
            0
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            
            // Optimization: Only replace if it's a different fragment
            when (item.itemId) {
                R.id.nav_kasir -> {
                    if (currentFragment !is KasirFragment) replaceFragment(KasirFragment())
                    true
                }
                R.id.nav_checkout -> {
                    if (currentFragment !is CheckoutFragment) replaceFragment(CheckoutFragment())
                    true
                }
                R.id.nav_laporan -> {
                    if (currentFragment !is LaporanFragment) replaceFragment(LaporanFragment())
                    true
                }
                R.id.nav_pengaturan -> {
                    if (currentFragment !is PengaturanFragment) replaceFragment(PengaturanFragment())
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Programmatically change the selected tab and update the fragment.
     * This fixes Issue 1 (BottomNav state sync).
     */
    fun setSelectedTab(itemId: Int) {
        if (binding.bottomNavigation.selectedItemId != itemId) {
            binding.bottomNavigation.selectedItemId = itemId
        } else {
            // Force replace if it's the first load or same ID but fragment container is empty
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            if (currentFragment == null) {
                val fragment = when(itemId) {
                    R.id.nav_kasir -> KasirFragment()
                    R.id.nav_checkout -> CheckoutFragment()
                    R.id.nav_laporan -> LaporanFragment()
                    R.id.nav_pengaturan -> PengaturanFragment()
                    else -> KasirFragment()
                }
                replaceFragment(fragment, false)
            }
        }
    }

    private fun replaceFragment(fragment: Fragment, animate: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()
        if (animate) {
            // Smooth fade transitions
            transaction.setCustomAnimations(
                R.anim.fragment_fade_in, 
                R.anim.fragment_fade_out
            )
        }
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.commit()
    }
}
