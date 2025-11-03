package com.nawaf.kasirpas

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText


class HomeActivity : AppCompatActivity() {

    private lateinit var searchEditText: TextInputEditText
    private lateinit var clearSearchButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupSearchFunctionality()
    }

    private fun setupSearchFunctionality() {
        // Inisialisasi view
        searchEditText = findViewById(R.id.searchEditText)
        clearSearchButton = findViewById(R.id.clearSearchButton)

        // Listen perubahan teks di search field
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Tampilkan/sembunyikan tombol clear berdasarkan apakah ada teks
                clearSearchButton.visibility = if (s.isNullOrEmpty()) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

                // Filter data berdasarkan input pencarian
                filterRestaurants(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Tombol clear search
        clearSearchButton.setOnClickListener {
            searchEditText.text?.clear()
            // Sembunyikan keyboard setelah clear
            hideKeyboard()
        }

        // Action search dari keyboard
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }

        // Handle klik pada search card (opsional - fokus ke input text)
        val searchCard = findViewById<androidx.cardview.widget.CardView>(R.id.searchCard)
        searchCard.setOnClickListener {
            searchEditText.requestFocus()
            showKeyboard()
        }
    }

    private fun filterRestaurants(query: String) {
        // Implementasi filter restoran berdasarkan query
        // Untuk sekarang kita buat simple dulu
        if (query.isNotEmpty()) {
            // Di sini nanti bisa dihubungkan dengan RecyclerView untuk filter list restoran
            // Untuk sementara, kita tampilkan toast atau update UI sederhana
            val restaurantName = findViewById<TextView>(R.id.restaurantName)
            val originalName = "Rumah Makan A" // Simpan nama asli

            // Contoh sederhana: highlight text yang match
            if (originalName.contains(query, ignoreCase = true)) {
                restaurantName.text = originalName
            } else {
                // Jika tidak match, bisa ditampilkan pesan atau disembunyikan
                restaurantName.text = "Tidak ditemukan"
            }
        } else {
            // Reset ke keadaan semula jika query kosong
            val restaurantName = findViewById<TextView>(R.id.restaurantName)
            restaurantName.text = "Rumah Makan A"
        }
    }

    private fun performSearch() {
        val query = searchEditText.text.toString().trim()

        if (query.isNotEmpty()) {
            // Lakukan aksi search di sini
            Toast.makeText(this, "Mencari: $query", Toast.LENGTH_SHORT).show()

            // Di sini nanti bisa implementasi:
            // 1. Filter list restoran di RecyclerView
            // 2. Panggil API untuk search
            // 3. Navigasi ke halaman search results

            // Sembunyikan keyboard setelah search
            hideKeyboard()
        }
    }

    private fun showKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }
}