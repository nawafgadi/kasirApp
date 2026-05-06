package com.nawaf.kasirpas.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.nawaf.kasirpas.adapter.ManageCategoryAdapter
import com.nawaf.kasirpas.api.RetrofitClient
import com.nawaf.kasirpas.databinding.ActivityKelolaKategoriBinding
import com.nawaf.kasirpas.databinding.BottomSheetCategoryBinding
import com.nawaf.kasirpas.model.Category
import com.nawaf.kasirpas.request.CategoryRequest
import com.nawaf.kasirpas.request.CategoryStatusRequest
import com.nawaf.kasirpas.utils.PreferenceManager
import kotlinx.coroutines.launch

class KelolaKategoriActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKelolaKategoriBinding
    private lateinit var adapter: ManageCategoryAdapter
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityKelolaKategoriBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)
        
        setupNavigation()
        setupRecyclerView()
        fetchKategori()
    }

    private fun setupNavigation() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        
        binding.btnTambahKategori.setOnClickListener {
            showCategoryForm()
        }
    }

    private fun setupRecyclerView() {
        adapter = ManageCategoryAdapter(
            categories = emptyList(),
            onEditClick = { category ->
                showCategoryForm(category)
            },
            onDeleteClick = { category ->
                deleteKategori(category)
            },
            onStatusChange = { category, isChecked ->
                updateKategoriStatus(category, isChecked)
            }
        )
        
        binding.rvKategori.layoutManager = LinearLayoutManager(this)
        binding.rvKategori.adapter = adapter
    }

    private fun showCategoryForm(category: Category? = null) {
        val dialog = BottomSheetDialog(this)
        val dialogBinding = BottomSheetCategoryBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        if (category != null) {
            dialogBinding.tvTitle.text = "Edit Kategori"
            dialogBinding.etCategoryName.setText(category.name)
            dialogBinding.btnSave.text = "Update Kategori"
        }

        dialogBinding.btnSave.setOnClickListener {
            val name = dialogBinding.etCategoryName.text.toString().trim()
            if (name.isEmpty()) {
                dialogBinding.tilCategoryName.error = "Nama kategori tidak boleh kosong"
                return@setOnClickListener
            }
            
            saveCategory(name, category?.id)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveCategory(name: String, categoryId: Int? = null) {
        val token = preferenceManager.getToken() ?: return
        val request = CategoryRequest(name = name)

        lifecycleScope.launch {
            try {
                val response = if (categoryId == null) {
                    RetrofitClient.categoryApi.storeCategory("Bearer $token", request)
                } else {
                    RetrofitClient.categoryApi.updateCategory("Bearer $token", categoryId, request)
                }

                if (response.isSuccessful) {
                    val msg = if (categoryId == null) "Kategori berhasil ditambah" else "Kategori berhasil diupdate"
                    showToast(msg)
                    fetchKategori()
                } else {
                    showToast("Gagal menyimpan kategori: ${response.message()}")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun fetchKategori() {
        val token = preferenceManager.getToken()
        if (token == null) {
            showToast("Token tidak ditemukan")
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.categoryApi.getCategories("Bearer $token")
                if (response.isSuccessful) {
                    val categories = response.body()?.data ?: emptyList()
                    adapter.updateData(categories)
                    updateStats(categories)
                } else {
                    showToast("Gagal mengambil data: ${response.message()}")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun updateKategoriStatus(category: Category, isChecked: Boolean) {
        val token = preferenceManager.getToken() ?: return
        val status = if (isChecked) 1 else 0
        val request = CategoryStatusRequest(isActive = status)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.categoryApi.updateCategoryStatus(
                    "Bearer $token",
                    category.id,
                    request
                )
                if (response.isSuccessful) {
                    showToast("Status ${category.name} berhasil diperbarui")
                    // Refresh data to update stats
                    fetchKategori()
                } else {
                    showToast("Gagal memperbarui status")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun deleteKategori(category: Category) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Hapus Kategori")
            .setMessage("Apakah Anda yakin ingin menghapus kategori ${category.name}?")
            .setPositiveButton("Hapus") { _, _ ->
                val token = preferenceManager.getToken() ?: return@setPositiveButton

                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.categoryApi.deleteCategory("Bearer $token", category.id)
                        if (response.isSuccessful) {
                            showToast("${category.name} berhasil dihapus")
                            fetchKategori()
                        } else {
                            showToast("Gagal menghapus kategori")
                        }
                    } catch (e: Exception) {
                        showToast("Error: ${e.message}")
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateStats(categories: List<Category>) {
        val total = categories.size
        val terlihat = categories.count { it.isActive == 1 }
        val tersembunyi = total - terlihat

        binding.tvTotalKategori.text = total.toString()
        binding.tvTerlihatKategori.text = terlihat.toString()
        binding.tvTersembunyiKategori.text = tersembunyi.toString()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
