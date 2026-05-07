package com.nawaf.kasirpas.activity

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.nawaf.kasirpas.adapter.ManageProductAdapter
import com.nawaf.kasirpas.api.RetrofitClient
import com.nawaf.kasirpas.databinding.ActivityKelolaProductBinding
import com.nawaf.kasirpas.databinding.BottomSheetProductBinding
import com.nawaf.kasirpas.model.Category
import com.nawaf.kasirpas.model.Product
import com.nawaf.kasirpas.utils.PreferenceManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class KelolaProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKelolaProductBinding
    private lateinit var adapter: ManageProductAdapter
    private lateinit var preferenceManager: PreferenceManager
    private var categories: List<Category> = emptyList()

    private var selectedImageUri: Uri? = null
    private var currentDialogBinding: BottomSheetProductBinding? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            currentDialogBinding?.ivProductImage?.load(it)
        }
    }

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

        currentDialogBinding = dialogBinding
        selectedImageUri = null

        val categoryNames = categories.map { it.name }
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categoryNames)
        dialogBinding.acCategory.setAdapter(categoryAdapter)

        var selectedCategoryId: Int? = product?.categoryId

        dialogBinding.acCategory.setOnItemClickListener { _, _, position, _ ->
            selectedCategoryId = categories[position].id
        }

        dialogBinding.cvImagePicker.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        if (product != null) {
            dialogBinding.tvTitle.text = "Edit Produk"
            dialogBinding.etProductName.setText(product.name)
            dialogBinding.etProductPrice.setText(product.price)
            val currentStock = product.stocks?.sumOf { it.stockOnHand ?: 0 } ?: 0
            dialogBinding.etProductStock.setText(currentStock.toString())
            dialogBinding.acCategory.setText(product.category?.name ?: "", false)
            dialogBinding.btnSave.text = "Update Produk"
            product.imageUrl?.let {
                dialogBinding.ivProductImage.load(it)
            }
        }

        dialogBinding.btnSave.setOnClickListener {
            val name = dialogBinding.etProductName.text.toString().trim()
            val priceStr = dialogBinding.etProductPrice.text.toString().trim()
            val stockStr = dialogBinding.etProductStock.text.toString().trim()

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

            saveProduct(
                name = name,
                price = priceStr,
                stock = stockStr,
                description = "",
                categoryId = selectedCategoryId,
                productId = product?.id,
                dialog = dialog
            )
        }

        dialog.setOnDismissListener {
            currentDialogBinding = null
        }

        dialog.show()
    }

    private fun getFileFromUri(uri: Uri): File? {
        val fileName = getFileName(uri) ?: "temp_image.jpg"
        val tempFile = File(cacheDir, fileName)
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name
    }

    private fun createPartFromString(string: String): RequestBody {
        return string.toRequestBody(MultipartBody.FORM)
    }

    private fun saveProduct(
        name: String,
        price: String,
        stock: String,
        description: String,
        categoryId: Int?,
        productId: Int?,
        dialog: BottomSheetDialog
    ) {
        val token = preferenceManager.getToken() ?: return

        val namePart = createPartFromString(name)
        val pricePart = createPartFromString(price)
        val stockPart = createPartFromString(stock)
        val descPart = if (description.isNotEmpty()) createPartFromString(description) else null
        val catPart = categoryId?.toString()?.let { createPartFromString(it) }

        val imagePart = selectedImageUri?.let { uri ->
            val file = getFileFromUri(uri)
            if (file != null) {
                val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("image", file.name, reqFile)
            } else null
        }

        lifecycleScope.launch {
            try {
                val response = if (productId == null) {
                    RetrofitClient.productApi.storeProduct(
                        token = "Bearer $token",
                        name = namePart,
                        price = pricePart,
                        description = descPart,
                        stock = stockPart,
                        categoryId = catPart,
                        image = imagePart
                    )
                } else {
                    val methodPart = createPartFromString("PUT")
                    RetrofitClient.productApi.updateProduct(
                        token = "Bearer $token",
                        id = productId,
                        method = methodPart,
                        name = namePart,
                        price = pricePart,
                        description = descPart,
                        stock = stockPart,
                        categoryId = catPart,
                        image = imagePart
                    )
                }

                if (response.isSuccessful) {
                    val msg = if (productId == null) "Produk berhasil ditambah" else "Produk berhasil diupdate"
                    showToast(msg)
                    fetchProducts()
                    dialog.dismiss()
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
        val habis = products.count { (it.stocks?.sumOf { s -> s.stockOnHand ?: 0 } ?: 0) <= 0 }

        binding.tvTotalProduk.text = total.toString()
        binding.tvStokHabis.text = habis.toString()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
