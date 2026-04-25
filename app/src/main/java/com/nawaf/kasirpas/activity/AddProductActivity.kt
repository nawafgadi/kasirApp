package com.nawaf.kasirpas.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nawaf.kasirpas.api.RetrofitClient
import com.nawaf.kasirpas.databinding.ActivityAddProductBinding
import com.nawaf.kasirpas.request.ProductRequest
import com.nawaf.kasirpas.utils.PreferenceManager
import kotlinx.coroutines.launch

class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductBinding
    private lateinit var prefManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefManager = PreferenceManager(this)

        binding.btnSaveProduct.setOnClickListener {
            saveProduct()
        }
    }

    private fun saveProduct() {
        val name = binding.etProductName.text.toString().trim()
        val priceStr = binding.etProductPrice.text.toString().trim()
        val stockStr = binding.etProductStock.text.toString().trim()

        if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toLong()
        val stock = stockStr.toInt()

        // Mendapatkan user_id dari guard/PreferenceManager
        val userId = prefManager.getUser()?.id ?: 0

        if (userId == 0) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val request = ProductRequest(
            name = name,
            price = price,
            stock = stock,
            userId = userId
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.productApi.createProduct(request)
                if (response.isSuccessful) {
                    Toast.makeText(this@AddProductActivity, "Product saved successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddProductActivity, "Failed to save product", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddProductActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
