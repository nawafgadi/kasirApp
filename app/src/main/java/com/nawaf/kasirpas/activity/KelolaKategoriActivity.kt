@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.nawaf.kasirpas.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.nawaf.kasirpas.api.RetrofitClient
import com.nawaf.kasirpas.model.Category
import com.nawaf.kasirpas.request.CategoryRequest
import com.nawaf.kasirpas.request.CategoryStatusRequest
import com.nawaf.kasirpas.utils.PreferenceManager
import com.nawaf.kasirpas.utils.DataCache
import kotlinx.coroutines.launch

class KelolaKategoriActivity : ComponentActivity() {

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        preferenceManager = PreferenceManager(this)

        setContent {
            var categoriesList by remember { mutableStateOf<List<Category>>(emptyList()) }
            var showLoading by remember { mutableStateOf(false) }
            var loadingMessage by remember { mutableStateOf("") }
            
            var showAddEditBottomSheet by remember { mutableStateOf(false) }
            var editingCategory by remember { mutableStateOf<Category?>(null) }
            var showDeleteDialog by remember { mutableStateOf<Category?>(null) }

            val coroutineScope = rememberCoroutineScope()

            fun showToast(msg: String) {
                Toast.makeText(this@KelolaKategoriActivity, msg, Toast.LENGTH_SHORT).show()
            }

            fun fetchKategoriSilently(token: String) {
                coroutineScope.launch {
                    try {
                        val response = RetrofitClient.categoryApi.getCategories("Bearer $token")
                        if (response.isSuccessful) {
                            val list = response.body()?.data ?: emptyList()
                            DataCache.categories = list
                            categoriesList = list
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            fun fetchKategori() {
                val token = preferenceManager.getToken()
                if (token == null) {
                    showToast("Token tidak ditemukan")
                    return
                }

                val cached = DataCache.categories
                if (cached != null) {
                    categoriesList = cached
                    fetchKategoriSilently(token)
                } else {
                    showLoading = true
                    loadingMessage = "Memuat Kategori..."
                    lifecycleScope.launch {
                        try {
                            val response = RetrofitClient.categoryApi.getCategories("Bearer $token")
                            if (response.isSuccessful) {
                                val list = response.body()?.data ?: emptyList()
                                DataCache.categories = list
                                categoriesList = list
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

            fun saveCategory(name: String, categoryId: Int? = null) {
                val token = preferenceManager.getToken() ?: return
                val request = CategoryRequest(name = name)

                showLoading = true
                loadingMessage = if (categoryId == null) "Menambah Kategori..." else "Memperbarui Kategori..."
                
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
                            DataCache.categories = null // Invalidate cache
                            fetchKategori()
                        } else {
                            showToast("Gagal menyimpan kategori: ${response.message()}")
                        }
                    } catch (e: Exception) {
                        showToast("Error: ${e.message}")
                    } finally {
                        showLoading = false
                    }
                }
            }

            fun updateKategoriStatus(category: Category, isChecked: Boolean) {
                val token = preferenceManager.getToken() ?: return
                val request = CategoryStatusRequest(isActive = isChecked)

                showLoading = true
                loadingMessage = "Memperbarui Status..."
                
                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.categoryApi.updateCategoryStatus(
                            "Bearer $token",
                            category.id,
                            request
                        )
                        if (response.isSuccessful) {
                            showToast("Status ${category.name} berhasil diperbarui")
                            DataCache.categories = null // Invalidate cache
                            fetchKategori()
                        } else {
                            showToast("Gagal memperbarui status")
                        }
                    } catch (e: Exception) {
                        showToast("Error: ${e.message}")
                    } finally {
                        showLoading = false
                    }
                }
            }

            fun deleteKategori(category: Category) {
                val token = preferenceManager.getToken() ?: return

                showLoading = true
                loadingMessage = "Menghapus Kategori..."
                
                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.categoryApi.deleteCategory("Bearer $token", category.id)
                        if (response.isSuccessful) {
                            showToast("${category.name} berhasil dihapus")
                            DataCache.categories = null // Invalidate cache
                            fetchKategori()
                        } else {
                            showToast("Gagal menghapus kategori")
                        }
                    } catch (e: Exception) {
                        showToast("Error: ${e.message}")
                    } finally {
                        showLoading = false
                    }
                }
            }

            LaunchedEffect(Unit) {
                fetchKategori()
            }

            KelolaKategoriScreen(
                categories = categoriesList,
                onBack = { finish() },
                onAddClick = {
                    editingCategory = null
                    showAddEditBottomSheet = true
                },
                onEditClick = { category ->
                    editingCategory = category
                    showAddEditBottomSheet = true
                },
                onDeleteClick = { category ->
                    showDeleteDialog = category
                },
                onStatusChange = { category, isChecked ->
                    updateKategoriStatus(category, isChecked)
                }
            )

            LoadingDialog(show = showLoading, message = loadingMessage)

            if (showDeleteDialog != null) {
                val category = showDeleteDialog!!
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = null },
                    title = { Text("Hapus Kategori") },
                    text = { Text("Apakah Anda yakin ingin menghapus kategori ${category.name}?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = null
                                deleteKategori(category)
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
                    var nameText by remember { mutableStateOf(editingCategory?.name ?: "") }
                    var nameError by remember { mutableStateOf<String?>(null) }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (editingCategory != null) "Edit Kategori" else "Tambah Kategori Baru",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1C1B1B)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Masukkan nama kategori baru untuk mengelompokkan produk Anda",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedTextField(
                            value = nameText,
                            onValueChange = {
                                nameText = it
                                if (it.isNotEmpty()) nameError = null
                            },
                            label = { Text("Nama Kategori") },
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
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = {
                                val trimmed = nameText.trim()
                                if (trimmed.isEmpty()) {
                                    nameError = "Nama kategori tidak boleh kosong"
                                } else {
                                    showAddEditBottomSheet = false
                                    saveCategory(trimmed, editingCategory?.id)
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
                                text = if (editingCategory != null) "Update Kategori" else "Simpan Kategori",
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KelolaKategoriScreen(
    categories: List<Category>,
    onBack: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (Category) -> Unit,
    onDeleteClick: (Category) -> Unit,
    onStatusChange: (Category, Boolean) -> Unit
) {
    val total = categories.size
    val terlihat = categories.count { it.isActive == 1 }
    val tersembunyi = total - terlihat

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
                            text = "Kelola Kategori",
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCardKategori(
                        label = "Total Kategori",
                        value = total.toString(),
                        textColor = Color(0xFF1C1B1B),
                        modifier = Modifier.weight(1f)
                    )
                    StatCardKategori(
                        label = "Terlihat",
                        value = terlihat.toString(),
                        textColor = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    StatCardKategori(
                        label = "Tersembunyi",
                        value = tersembunyi.toString(),
                        textColor = Color(0xFF1C1B1B),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "DAFTAR KATEGORI",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            items(categories, key = { it.id }) { category ->
                CategoryRowItem(
                    category = category,
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick,
                    onStatusChange = onStatusChange
                )
            }
        }
    }
}

@Composable
fun StatCardKategori(
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
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFF4B5563)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = textColor
            )
        }
    }
}

@Composable
fun CategoryRowItem(
    category: Category,
    onEditClick: (Category) -> Unit,
    onDeleteClick: (Category) -> Unit,
    onStatusChange: (Category, Boolean) -> Unit
) {
    val isActive = category.isActive == 1

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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF6ECFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    tint = Color(0xFF653DA7),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1C1B1B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isActive) "Terlihat" else "Tersembunyi",
                    fontSize = 14.sp,
                    color = if (isActive) Color(0xFF10B981) else Color(0xFF6B7280)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tampil",
                    fontSize = 12.sp,
                    color = Color(0xFF4B5563)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isActive,
                    onCheckedChange = { isChecked ->
                        onStatusChange(category, isChecked)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF10B981),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFD1D5DB),
                        uncheckedBorderColor = Color.Transparent
                    )
                )
                IconButton(
                    onClick = { onEditClick(category) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(
                    onClick = { onDeleteClick(category) },
                    modifier = Modifier.size(48.dp)
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
