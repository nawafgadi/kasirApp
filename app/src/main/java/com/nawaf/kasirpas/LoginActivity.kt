package com.nawaf.kasirpas

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Gunakan nama file "UserPrefs" yang sama dengan RegisterActivity
        sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnMasuk = findViewById<Button>(R.id.btnMasuk)
        val tvDaftar = findViewById<TextView>(R.id.tvDaftar)
        val tvLupaSandi = findViewById<TextView>(R.id.tvLupaSandi)

        btnMasuk.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan kata sandi harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ambil data yang tersimpan saat registrasi
            val savedEmail = sharedPrefs.getString("email", "")
            val savedPassword = sharedPrefs.getString("password", "")
            val savedName = sharedPrefs.getString("name", "Pengguna") // Ambil nama yang disimpan

            if (email == savedEmail && password == savedPassword) {
                // --- PERBAIKAN DI SINI ---
                // Simpan status login dan nama pengguna yang berhasil login
                val editor = sharedPrefs.edit()
                editor.putBoolean("isLoggedIn", true)
                editor.putString("loggedInUserName", savedName) // Simpan nama pengguna saat ini
                editor.apply()

                Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish() // Tutup LoginActivity agar tidak bisa kembali dengan tombol back
            } else {
                Toast.makeText(this, "Email atau kata sandi salah", Toast.LENGTH_SHORT).show()
            }
        }

        tvDaftar.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvLupaSandi.setOnClickListener {
            Toast.makeText(this, "Fitur lupa kata sandi belum tersedia", Toast.LENGTH_SHORT).show()
        }
    }
}
