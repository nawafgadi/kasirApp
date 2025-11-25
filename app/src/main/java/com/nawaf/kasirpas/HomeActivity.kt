package com.nawaf.kasirpas

import android.content.Intent
import android.content.SharedPreferences // PERBAIKAN 1: Import SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText

class HomeActivity : AppCompatActivity() {

    private lateinit var searchEditText: TextInputEditText
    private lateinit var clearSearchButton: ImageButton
    private lateinit var restaurantCard: CardView
    private lateinit var fabAddButton: ImageView
    private lateinit var userNameTextView: TextView // PERBAIKAN 2: Deklarasikan TextView untuk nama

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- PERBAIKAN 3: Panggil fungsi untuk menampilkan nama pengguna ---
        displayUserData()

        setupSearchFunctionality()
        setupNavigation()
        setupFooterCustom()
        setupFab()
    }

    // --- PERBAIKAN 4: Buat fungsi baru untuk mengambil dan menampilkan data ---
    private fun displayUserData() {
        // Hubungkan ke TextView di layout
        userNameTextView = findViewById(R.id.userName)

        // Akses SharedPreferences
        val sharedPrefs: SharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // Ambil nama yang sudah disimpan saat login, beri nilai default "Pengguna" jika tidak ada
        val loggedInUserName = sharedPrefs.getString("loggedInUserName", "Pengguna")

        // Set teks TextView dengan nama yang didapat
        userNameTextView.text = loggedInUserName
    }

    // --- Sisa kode Anda yang lain (dikonversi ke Kotlin) ---
    private fun setupNavigation() {
        restaurantCard = findViewById(R.id.restaurantCard)
        restaurantCard.setOnClickListener {
            val intent = Intent(this, BerandaActivity::class.java)
            startActivity(intent)
            Toast.makeText(
                this,
                "Berpindah ke Halaman Beranda (Katalog Produk)",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupSearchFunctionality() {
        searchEditText = findViewById(R.id.searchEditText)
        clearSearchButton = findViewById(R.id.clearSearchButton)
        val searchCard = findViewById<CardView>(R.id.searchCard)

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearSearchButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                filterRestaurants(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        clearSearchButton.setOnClickListener {
            searchEditText.text?.clear()
            filterRestaurants("")
            hideKeyboard()
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }

        searchCard.setOnClickListener {
            searchEditText.requestFocus()
            showKeyboard()
        }
    }

    private fun setupFab() {
        fabAddButton = findViewById(R.id.imageView)
        fabAddButton.setOnClickListener {
            Toast.makeText(this, "Aksi Tambah Cepat (Quick Add)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupFooterCustom() {
        val navHome = findViewById<ImageView>(R.id.navHome)
        val navGrafik = findViewById<ImageView>(R.id.navGrafik)
        val navOrder = findViewById<ImageView>(R.id.navOrder)
        val navProfile = findViewById<ImageView>(R.id.navProfile)

        navHome.setColorFilter(Color.parseColor("#4A5C2F"))
        navGrafik.setColorFilter(Color.parseColor("#A9A9A9"))
        navOrder.setColorFilter(Color.parseColor("#A9A9A9"))
        navProfile.setColorFilter(Color.parseColor("#A9A9A9"))

        navGrafik.setOnClickListener {
            Toast.makeText(this, "Navigasi ke Laporan/Grafik", Toast.LENGTH_SHORT).show()
        }
        navOrder.setOnClickListener {
            Toast.makeText(this, "Navigasi ke Pesanan/Order", Toast.LENGTH_SHORT).show()
        }
        navProfile.setOnClickListener {
            Toast.makeText(this, "Navigasi ke Profil", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterRestaurants(query: String) {
        val restaurantName = findViewById<TextView>(R.id.restaurantName)
        val restaurantCard = findViewById<CardView>(R.id.restaurantCard)
        val originalName = "Toko SRC"

        if (query.isEmpty() || originalName.contains(query, ignoreCase = true)) {
            restaurantName.text = originalName
            restaurantCard.visibility = View.VISIBLE
        } else {
            restaurantCard.visibility = View.GONE
        }
    }

    private fun performSearch() {
        val query = searchEditText.text.toString().trim()
        if (query.isNotEmpty()) {
            Toast.makeText(this, "Mencari: $query", Toast.LENGTH_SHORT).show()
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
