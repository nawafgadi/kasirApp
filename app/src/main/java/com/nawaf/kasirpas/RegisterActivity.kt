package com.nawaf.kasirpas

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        val etNama = findViewById<EditText>(R.id.etNama)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etTelepon = findViewById<EditText>(R.id.etTelepon)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val cbAgreement = findViewById<CheckBox>(R.id.cbAgreement)
        val btnDaftar = findViewById<Button>(R.id.btnDaftar)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        btnDaftar.setOnClickListener {
            val nama = etNama.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val telepon = etTelepon.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            when {
                nama.isEmpty() || email.isEmpty() || telepon.isEmpty() ||
                        password.isEmpty() || confirmPassword.isEmpty() -> {
                    Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                }
                password != confirmPassword -> {
                    Toast.makeText(this, "Kata sandi tidak sama", Toast.LENGTH_SHORT).show()
                }
                !cbAgreement.isChecked -> {
                    Toast.makeText(this, "Setujui kebijakan aplikasi terlebih dahulu", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val editor = sharedPrefs.edit()
                    editor.putString("nama", nama)
                    editor.putString("email", email)
                    editor.putString("telepon", telepon)
                    editor.putString("password", password)
                    editor.apply()

                    Toast.makeText(this, "Pendaftaran berhasil!", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
