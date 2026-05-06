package com.nawaf.kasirpas.activity

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.nawaf.kasirpas.adapter.ManageProductAdapter
import com.nawaf.kasirpas.api.RetrofitClient
import com.nawaf.kasirpas.databinding.ActivityKelolaProductBinding
import com.nawaf.kasirpas.databinding.BottomSheetProductBinding
import com.nawaf.kasirpas.model.Category
import com.nawaf.kasirpas.model.Product
import com.nawaf.kasirpas.request.ProductRequest
import com.nawaf.kasirpas.utils.PreferenceManager
import kotlinx.coroutines.launch

class KelolaProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKelolaProductBinding
    private lateinit var adapter: ManageProductAdapter
    private lateinit var preferenceManager: PreferenceManager
    private var categories: List<Category> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKelolaProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)

        setupNavigation()
        setupRecyclerView()
        fetchCategories()
        fetchProducts()
    }

    private fun setupNavigation() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnTambahProduk.setOnClickListener {
            showProductForm()
        }
    }

    private fun setupRecyclerView() {
        adapter = ManageProductAdapter(
            products = emptyList(),
            onEditClick = { product ->
                showProductForm(product)
            },
            onDeleteClick = { product ->
                deleteProduct(product)
            }
        )
        binding.rvProduk.layoutManager = LinearLayoutManager(this)
        binding.rvProduk.adapter = adapter
    }

    private fun fetchCategories() {
        val token = preferenceManager.getToken() ?: return
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.categoryApi.getCategories("Bearer $token")
                if (response.isSuccessful) {
                    categories = response.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchProducts() {
        val token = preferenceManager.getToken() ?: return
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.productApi.getProducts("Bearer $token")
                if (response.isSuccessful) {
                    val products = response.body()?.data ?: emptyList()
                    adapter.updateData(products)
                    updateStats(products)
                } else {
                    showToast("Gagal mengambil data: ${response.message()}")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun showProductForm(product: Product? = null) {
        val dialog = BottomSheetDialog(this)
        val dialogBinding = BottomSheetProductBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        // Setup Category Dropdown
        val categoryNames = categories.map { it.name }
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categoryNames)
        dialogBinding.acCategory.setAdapter(categoryAdapter)

        var selectedCategoryId: Int? = product?.categoryId

        dialogBinding.acCategory.setOnItemClickListener { _, _, position, _ ->
            selectedCategoryId = categories[position].id
        }

        if (product != null) {
            dialogBinding.tvTitle.text = "Edit Produk"
            dialogBinding.etProductName.setText(product.name)
            dialogBinding.etProductPrice.setText(product.price)
            val currentStock = product.stocks?.sumOf { it.stockOnHand ?: 0 } ?: 0
            dialogBinding.etProductStock.setText(currentStock.toString())
            dialogBinding.etDescription.setText(product.description)
            dialogBinding.acCategory.setText(product.category?.name ?: "", false)
            dialogBinding.btnSave.text = "Update Produk"
        }

        dialogBinding.btnSave.setOnClickListener {
            val name = dialogBinding.etProductName.text.toString().trim()
            val priceStr = dialogBinding.etProductPrice.text.toString().trim()
            val stockStr = dialogBinding.etProductStock.text.toString().trim()
            val description = dialogBinding.etDescription.text.toString().trim()

            if (name.isEmpty()) {
                dialogBinding.tilProductName.error = "Nama tidak boleh kosong"
                return@setOnClickListener
            }
            if (priceStr.isEmpty()) {
                dialogBinding.tilProductPrice.error = "Harga tidak boleh kosong"
                return@setOnClickListener
            }
            if (stockStr.isEmpty()) {
                dialogBinding.tilProductStock.error = "Stok tidak boleh kosong"
                return@setOnClickListener
            }

            val request = ProductRequest(
                name = name,
                price = priceStr.toDoubleOrNull() ?: 0.0,
                description = if (description.isEmpty()) null else description,
                stock = stockStr.toIntOrNull() ?: 0,
                categoryId = selectedCategoryId
            )

            saveProduct(request, product?.id)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveProduct(request: ProductRequest, productId: Int? = null) {
        val token = preferenceManager.getToken() ?: return
        lifecycleScope.launch {
            try {
                val response = if (productId == null) {
                    RetrofitClient.productApi.storeProduct("Bearer $token", request)
                } else {
                    RetrofitClient.productApi.updateProduct("Bearer $token", productId, request)
                }

                if (response.isSuccessful) {
                    val msg = if (productId == null) "Produk berhasil ditambah" else "Produk berhasil diupdate"
                    showToast(msg)
                    fetchProducts()
                } else {
                    showToast("Gagal menyimpan produk: ${response.message()}")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun deleteProduct(product: Product) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Hapus Produk")
            .setMessage("Apakah Anda yakin ingin menghapus ${product.name}?")
            .setPositiveButton("Hapus") { _, _ ->
                val token = preferenceManager.getToken() ?: return@setPositiveButton
                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.productApi.deleteProduct("Bearer $token", product.id)
                        if (response.isSuccessful) {
                            showToast("${product.name} berhasil dihapus")
                            fetchProducts()
                        } else {
                            showToast("Gagal menghapus produk")
                        }
                    } catch (e: Exception) {
                        showToast("Error: ${e.message}")
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateStats(products: List<Product>) {
        val total = products.size
        val stokMenipis = products.count { (it.stocks?.sumOf { s -> s.stockOnHand ?: 0 } ?: 0) in 1..5 }
        val habis = products.count { (it.stocks?.sumOf { s -> s.stockOnHand ?: 0 } ?: 0) <= 0 }

        binding.tvTotalProduk.text = total.toString()
        binding.tvStokMenipis.text = stokMenipis.toString()
        binding.tvStokHabis.text = habis.toString()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
