package com.nawaf.kasirpas.activity

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.nawaf.kasirpas.databinding.ActivityBillingBinding

class BillingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBillingBinding

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

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Memberikan padding atas pada AppBar agar tidak tertutup status bar (jam/baterai)
            binding.toolbar.updatePadding(top = 0) // Reset padding default jika ada
            
            // Kita atur padding root agar seluruh konten turun di bawah status bar
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            
            insets
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}
