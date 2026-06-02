@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.nawaf.kasirpas.activity

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.nawaf.kasirpas.R
import com.nawaf.kasirpas.api.RetrofitClient
import com.nawaf.kasirpas.model.Category
import com.nawaf.kasirpas.model.Product
import com.nawaf.kasirpas.utils.PreferenceManager
import com.nawaf.kasirpas.utils.DataCache
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.*

class KelolaProductActivity : ComponentActivity() {

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        preferenceManager = PreferenceManager(this)

        setContent {
            var productsList by remember { mutableStateOf<List<Product>>(emptyList()) }
            var categoriesList by remember { mutableStateOf<List<Category>>(emptyList()) }
            
            var showLoading by remember { mutableStateOf(false) }
            var loadingMessage by remember { mutableStateOf("") }
            
            var showAddEditBottomSheet by remember { mutableStateOf(false) }
            var editingProduct by remember { mutableStateOf<Product?>(null) }
            var showDeleteDialog by remember { mutableStateOf<Product?>(null) }

            val coroutineScope = rememberCoroutineScope()

            fun showToast(msg: String) {
                Toast.makeText(this@KelolaProductActivity, msg, Toast.LENGTH_SHORT).show()
            }

            fun fetchCategories(onComplete: () -> Unit = {}) {
                val cached = DataCache.categories
                if (cached != null) {
                    categoriesList = cached
                    onComplete()
                    return
                }

                val token = preferenceManager.getToken() ?: return
                showLoading = true
                loadingMessage = "Memuat Kategori..."
                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.categoryApi.getCategories("Bearer $token")
                        if (response.isSuccessful) {
                            val list = response.body()?.data ?: emptyList()
                            DataCache.categories = list
                            categoriesList = list
                            onComplete()
                        } else {
                            showToast("Gagal memuat kategori")
                        }
                    } catch (e: Exception) {
                        showToast("Error: ${e.message}")
                    } finally {
                        showLoading = false
                    }
                }
            }

            fun fetchProductsSilently(token: String) {
                coroutineScope.launch {
                    try {
                        val response = RetrofitClient.productApi.getProducts("Bearer $token")
                        if (response.isSuccessful) {
                            val list = response.body()?.data ?: emptyList()
                            DataCache.products = list
                            productsList = list
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            fun fetchProducts() {
                val token = preferenceManager.getToken() ?: return
                val cached = DataCache.products
                if (cached != null) {
                    productsList = cached
                    fetchProductsSilently(token)
                } else {
                    showLoading = true
                    loadingMessage = "Memuat Produk..."
                    lifecycleScope.launch {
                        try {
                            val response = RetrofitClient.productApi.getProducts("Bearer $token")
                            if (response.isSuccessful) {
                                val list = response.body()?.data ?: emptyList()
                                DataCache.products = list
                                productsList = list
                            } else {
                                showToast("Gagal mengambil data: ${response.message()}")
                            }
                        } catch (e: Exception) {
                            showToast("Error: ${e.message}")
                        } finally {
                            showLoading = false
                        }
                    }
                }
            }

            fun createPartFromString(string: String): RequestBody {
                return string.toRequestBody(MultipartBody.FORM)
            }

            fun saveProduct(
                name: String,
                price: String,
                stock: String,
                categoryId: Int?,
                imageUri: Uri?,
                productId: Int?
            ) {
                val token = preferenceManager.getToken() ?: return

                val namePart = createPartFromString(name)
                val pricePart = createPartFromString(price)
                val stockPart = createPartFromString(stock)
                val catPart = categoryId?.toString()?.let { createPartFromString(it) }

                val imagePart = imageUri?.let { uri ->
                    val file = getFileFromUri(uri)
                    if (file != null) {
                        val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("image", file.name, reqFile)
                    } else null
                }

                showLoading = true
                loadingMessage = if (productId == null) "Menambah Produk..." else "Memperbarui Produk..."
                
                lifecycleScope.launch {
                    try {
                        val response = if (productId == null) {
                            RetrofitClient.productApi.storeProduct(
                                token = "Bearer $token",
                                name = namePart,
                                price = pricePart,
                                description = null,
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
                                description = null,
                                stock = stockPart,
                                categoryId = catPart,
                                image = imagePart
                            )
                        }

                        if (response.isSuccessful) {
                            val msg = if (productId == null) "Produk berhasil ditambah" else "Produk berhasil diupdate"
                            showToast(msg)
                            DataCache.products = null // Invalidate cache
                            fetchProducts()
                            showAddEditBottomSheet = false
                        } else {
                            showToast("Gagal menyimpan produk: ${response.message()}")
                        }
                    } catch (e: Exception) {
                        showToast("Error: ${e.message}")
                    } finally {
                        showLoading = false
                    }
                }
            }

            fun deleteProduct(product: Product) {
                val token = preferenceManager.getToken() ?: return

                showLoading = true
                loadingMessage = "Menghapus Produk..."
                
                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.productApi.deleteProduct("Bearer $token", product.id)
                        if (response.isSuccessful) {
                            showToast("${product.name} berhasil dihapus")
                            DataCache.products = null // Invalidate cache
                            fetchProducts()
                        } else {
                            showToast("Gagal menghapus produk")
                        }
                    } catch (e: Exception) {
                        showToast("Error: ${e.message}")
                    } finally {
                        showLoading = false
                    }
                }
            }

            LaunchedEffect(Unit) {
                fetchProducts()
            }

            KelolaProductScreen(
                products = productsList,
                onBack = { finish() },
                onAddClick = {
                    fetchCategories {
                        editingProduct = null
                        showAddEditBottomSheet = true
                    }
                },
                onEditClick = { product ->
                    fetchCategories {
                        editingProduct = product
                        showAddEditBottomSheet = true
                    }
                },
                onDeleteClick = { product ->
                    showDeleteDialog = product
                }
            )

            LoadingDialog(show = showLoading, message = loadingMessage)

            if (showDeleteDialog != null) {
                val product = showDeleteDialog!!
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = null },
                    title = { Text("Hapus Produk") },
                    text = { Text("Apakah Anda yakin ingin menghapus ${product.name}?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = null
                                deleteProduct(product)
                            }
                        ) {
                            Text("Hapus", color = Color(0xFFBA1A1A))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = null }) {
                            Text("Batal", color = Color(0xFF6B7280))
                        }
                    },
                    containerColor = Color.White
                )
            }

            if (showAddEditBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showAddEditBottomSheet = false },
                    containerColor = Color.White,
                    dragHandle = {
                        BottomSheetDefaults.DragHandle(color = Color(0xFFE5E7EB))
                    }
                ) {
                    var nameText by remember { mutableStateOf(editingProduct?.name ?: "") }
                    var priceText by remember { mutableStateOf(editingProduct?.price ?: "") }
                    val initialStock = editingProduct?.stocks?.sumOf { it.stockOnHand ?: 0 }?.toString() ?: ""
                    var stockText by remember { mutableStateOf(initialStock) }
                    var selectedCategoryId by remember { mutableStateOf(editingProduct?.categoryId) }
                    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
                    
                    var nameError by remember { mutableStateOf<String?>(null) }
                    var priceError by remember { mutableStateOf<String?>(null) }
                    var stockError by remember { mutableStateOf<String?>(null) }

                    val imagePickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri: Uri? ->
                        uri?.let {
                            selectedImageUri = it
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (editingProduct != null) "Edit Produk" else "Tambah Produk Baru",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1C1B1B)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Lengkapi detail informasi produk di bawah ini",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280)
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Photo Picker Box
                        Card(
                            modifier = Modifier
                                .size(140.dp)
                                .align(Alignment.CenterHorizontally)
                                .clickable {
                                    imagePickerLauncher.launch("image/*")
                                },
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (selectedImageUri != null) {
                                    AsyncImage(
                                        model = selectedImageUri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else if (!editingProduct?.imageUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = editingProduct?.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            tint = Color(0xFF9CA3AF),
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Pilih Foto",
                                            color = Color(0xFF653DA7),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = nameText,
                            onValueChange = {
                                nameText = it
                                if (it.isNotEmpty()) nameError = null
                            },
                            label = { Text("Nama Produk") },
                            isError = nameError != null,
                            supportingText = nameError?.let { { Text(it) } },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF653DA7),
                                focusedLabelColor = Color(0xFF653DA7),
                                unfocusedBorderColor = Color(0xFFD1D5DB),
                                unfocusedLabelColor = Color(0xFF6B7280),
                                focusedTextColor = Color(0xFF1C1B1B),
                                unfocusedTextColor = Color(0xFF1C1B1B),
                                cursorColor = Color(0xFF653DA7)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = priceText,
                                onValueChange = {
                                    priceText = it
                                    if (it.isNotEmpty()) priceError = null
                                },
                                label = { Text("Harga Jual") },
                                isError = priceError != null,
                                supportingText = priceError?.let { { Text(it) } },
                                prefix = { Text("Rp ") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF653DA7),
                                    focusedLabelColor = Color(0xFF653DA7),
                                    unfocusedBorderColor = Color(0xFFD1D5DB),
                                    unfocusedLabelColor = Color(0xFF6B7280),
                                    focusedTextColor = Color(0xFF1C1B1B),
                                    unfocusedTextColor = Color(0xFF1C1B1B),
                                    cursorColor = Color(0xFF653DA7)
                                )
                            )

                            OutlinedTextField(
                                value = stockText,
                                onValueChange = {
                                    stockText = it
                                    if (it.isNotEmpty()) stockError = null
                                },
                                label = { Text("Stok") },
                                isError = stockError != null,
                                supportingText = stockError?.let { { Text(it) } },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF653DA7),
                                    focusedLabelColor = Color(0xFF653DA7),
                                    unfocusedBorderColor = Color(0xFFD1D5DB),
                                    unfocusedLabelColor = Color(0xFF6B7280),
                                    focusedTextColor = Color(0xFF1C1B1B),
                                    unfocusedTextColor = Color(0xFF1C1B1B),
                                    cursorColor = Color(0xFF653DA7)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Category Dropdown Box
                        var expanded by remember { mutableStateOf(false) }
                        val rotationAngle by animateFloatAsState(
                            targetValue = if (expanded) 180f else 0f,
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                            label = "dropdown_arrow"
                        )
                        
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Kategori Produk",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF653DA7),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp)
                                    .background(Color(0xFFF9FAFB), RoundedCornerShape(14.dp))
                                    .border(
                                        width = 1.dp,
                                        color = if (expanded) Color(0xFF653DA7) else Color(0xFFD1D5DB),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { expanded = true }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = Color(0xFF653DA7),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        val selectedCategoryName = categoriesList.find { it.id == selectedCategoryId }?.name
                                        Text(
                                            text = selectedCategoryName ?: "Pilih Kategori",
                                            color = if (selectedCategoryName != null) Color(0xFF1C1B1B) else Color(0xFF9CA3AF),
                                            fontSize = 15.sp,
                                            fontWeight = if (selectedCategoryName != null) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    }
                                    
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Pilih Kategori",
                                        tint = Color(0xFF6B7280),
                                        modifier = Modifier
                                            .size(24.dp)
                                            .graphicsLayer { rotationZ = rotationAngle }
                                    )
                                }
                                
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .background(Color.White, RoundedCornerShape(16.dp))
                                        .border(0.5.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)),
                                    containerColor = Color.White
                                ) {
                                    categoriesList.forEach { category ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Folder,
                                                        contentDescription = null,
                                                        tint = if (selectedCategoryId == category.id) Color(0xFF653DA7) else Color(0xFF9CA3AF),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Text(
                                                        text = category.name,
                                                        fontWeight = if (selectedCategoryId == category.id) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (selectedCategoryId == category.id) Color(0xFF653DA7) else Color(0xFF1C1B1B)
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedCategoryId = category.id
                                                expanded = false
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                val name = nameText.trim()
                                val price = priceText.trim()
                                val stock = stockText.trim()

                                var hasError = false
                                if (name.isEmpty()) {
                                    nameError = "Nama tidak boleh kosong"
                                    hasError = true
                                }
                                if (price.isEmpty()) {
                                    priceError = "Harga tidak boleh kosong"
                                    hasError = true
                                }
                                if (stock.isEmpty()) {
                                    stockError = "Stok tidak boleh kosong"
                                    hasError = true
                                }

                                if (!hasError) {
                                    saveProduct(
                                        name = name,
                                        price = price,
                                        stock = stock,
                                        categoryId = selectedCategoryId,
                                        imageUri = selectedImageUri,
                                        productId = editingProduct?.id
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF653DA7)
                            )
                        ) {
                            Text(
                                text = if (editingProduct != null) "Update Produk" else "Simpan Produk",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
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
}

@Composable
fun KelolaProductScreen(
    products: List<Product>,
    onBack: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (Product) -> Unit,
    onDeleteClick: (Product) -> Unit
) {
    val total = products.size
    val habis = products.count { (it.stocks?.sumOf { s -> s.stockOnHand ?: 0 } ?: 0) <= 0 }

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                tonalElevation = 2.dp
            ) {
                Column {
                    Spacer(modifier = Modifier.statusBarsPadding())
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kelola Produk",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1C1B1B),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = onAddClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF653DA7)
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(
                                text = "+ Tambah",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                }
            }
        },
        containerColor = Color(0xFFFCF9F8)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCardProduct(
                        label = "Total Produk",
                        value = total.toString(),
                        textColor = Color(0xFF1C1B1B),
                        modifier = Modifier.weight(1f)
                    )
                    StatCardProduct(
                        label = "Habis",
                        value = habis.toString(),
                        textColor = Color(0xFFBA1A1A),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "DAFTAR PRODUK",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            items(products, key = { it.id }) { product ->
                ProductRowItem(
                    product = product,
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick
                )
            }
        }
    }
}

@Composable
fun StatCardProduct(
    label: String,
    value: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFF3F4F6)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF4B5563)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = textColor
            )
        }
    }
}

@Composable
fun ProductRowItem(
    product: Product,
    onEditClick: (Product) -> Unit,
    onDeleteClick: (Product) -> Unit
) {
    val totalStock = product.stocks?.sumOf { it.stockOnHand ?: 0 } ?: 0
    val price = product.price.toDoubleOrNull() ?: 0.0

    fun formatRupiah(number: Double): String {
        val localeID = Locale("in", "ID")
        val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
        return formatRupiah.format(number).replace("Rp", "Rp ").replace(",00", "")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image Box
            Card(
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (!product.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = product.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_inventory),
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.Center),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1C1B1B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.category?.name ?: "Tanpa Kategori",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatRupiah(price),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF653DA7)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4B5563))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Stok: $totalStock",
                        fontSize = 12.sp,
                        color = Color(0xFF4B5563)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onEditClick(product) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(
                    onClick = { onDeleteClick(product) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = Color(0xFFBA1A1A),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
