package com.nawaf.kasirpas

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class TambahMenuActivity : AppCompatActivity() {

    private lateinit var ivFoodImage: ImageView
    private lateinit var etFoodName: EditText
    private lateinit var etDescription: EditText
    private lateinit var etPrice: EditText
    private lateinit var btnEdit: Button
    private lateinit var btnSave: Button

    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_menu)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        ivFoodImage = findViewById(R.id.ivFoodImage)
        etFoodName = findViewById(R.id.etFoodName)
        etDescription = findViewById(R.id.etDescription)
        etPrice = findViewById(R.id.etPrice)
        btnEdit = findViewById(R.id.btnEdit)
        btnSave = findViewById(R.id.btnSave)
    }

    private fun setupClickListeners() {
        // Tambah Gambar click listener
        ivFoodImage.setOnClickListener {
            openImagePicker()
        }

        // Edit button click listener
        btnEdit.setOnClickListener {
            clearForm()
            Toast.makeText(this, "Form telah direset", Toast.LENGTH_SHORT).show()
        }

        // Simpan button click listener
        btnSave.setOnClickListener {
            saveMenu()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            ivFoodImage.setImageURI(selectedImageUri)
        }
    }

    private fun saveMenu() {
        val foodName = etFoodName.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val price = etPrice.text.toString().trim()

        if (foodName.isEmpty() || description.isEmpty() || price.isEmpty()) {
            Toast.makeText(this, "Harap isi semua field", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Harap pilih gambar makanan", Toast.LENGTH_SHORT).show()
            return
        }

        // Di sini Anda bisa menambahkan logika untuk menyimpan data ke database
        // atau mengirim data ke server

        val menuData = MenuData(
            name = foodName,
            description = description,
            price = price.toDoubleOrNull() ?: 0.0,
            imageUri = selectedImageUri
        )

        Toast.makeText(this, "Menu berhasil disimpan: $foodName", Toast.LENGTH_SHORT).show()

        // Optional: Kembali ke activity sebelumnya atau reset form
        // finish()
        // clearForm()
    }

    private fun clearForm() {
        etFoodName.text.clear()
        etDescription.text.clear()
        etPrice.text.clear()
        ivFoodImage.setImageResource(R.drawable.ic_add_image)
        selectedImageUri = null
    }

    // Data class untuk menyimpan data menu
    data class MenuData(
        val name: String,
        val description: String,
        val price: Double,
        val imageUri: Uri?
    )
}