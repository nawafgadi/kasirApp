@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.nawaf.kasirpas.activity
import androidx.compose.ui.tooling.preview.Preview

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.nawaf.kasirpas.activity.ui.theme.KasirAppTheme
import com.nawaf.kasirpas.api.RetrofitClient
import com.nawaf.kasirpas.model.BintangWarung
import com.nawaf.kasirpas.model.PortfolioInsight
import com.nawaf.kasirpas.model.Profile
import com.nawaf.kasirpas.model.User
import com.nawaf.kasirpas.utils.PreferenceManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

// Luxury Brand Colors for Profile Screen
val ProfilePrimary = Color(0xFF653DA7)
val ProfileSecondary = Color(0xFF8B64C7)
val ProfileBackground = Color(0xFFFCF9F8)
val ProfileSurface = Color(0xFFFFFFFF)
val ProfileOnSurface = Color(0xFF1C1B1B)
val ProfileOnSurfaceVariant = Color(0xFF6B7280)
val ProfileOutline = Color(0xFFCCC3D3)
val PremiumGold = Color(0xFFFFD700)
val PremiumText = Color(0xFF4E342E)

class ProfileActivity : ComponentActivity() {

    private lateinit var preferenceManager: PreferenceManager
    
    // UI state states
    private var profileState by mutableStateOf<Profile?>(null)
    private var userState by mutableStateOf<User?>(null)
    private var isLoadingState by mutableStateOf(true)
    private var isSavingState by mutableStateOf(false)
    private var bioTextState by mutableStateOf("")
    private var selectedImageUriState by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        preferenceManager = PreferenceManager(this)

        // Load data from preference manager
        userState = preferenceManager.getUser()

        setContent {
            KasirAppTheme {
                ProfileScreen(
                    profile = profileState,
                    user = userState,
                    isLoading = isLoadingState,
                    isSaving = isSavingState,
                    bioText = bioTextState,
                    selectedImageUri = selectedImageUriState,
                    onBioChange = { bioTextState = it },
                    onImageSelected = { selectedImageUriState = it },
                    onBack = { finish() },
                    onSave = { updateProfile(bioTextState, selectedImageUriState) },
                    onReset = { deleteProfile() },
                    onUpgradeClick = {
                        startActivity(Intent(this, BillingActivity::class.java))
                    }
                )
            }
        }

        fetchProfile()
    }

    private fun fetchProfile() {
        val token = preferenceManager.getToken()
        if (token == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        isLoadingState = true
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.profileApi.getProfile("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!.data
                    profileState = data
                    bioTextState = data?.bio ?: ""
                } else {
                    Toast.makeText(this@ProfileActivity, "Gagal mengambil data profil", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ProfileActivity, "Error koneksi: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoadingState = false
            }
        }
    }

    private fun updateProfile(bio: String, imageUri: Uri?) {
        val token = preferenceManager.getToken() ?: return
        
        val bioPart = bio.toRequestBody(MultipartBody.FORM)
        val methodPart = "PUT".toRequestBody(MultipartBody.FORM)

        val imagePart = imageUri?.let { uri ->
            val file = getFileFromUri(uri)
            if (file != null) {
                val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("image", file.name, reqFile)
            } else null
        }

        isSavingState = true
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.profileApi.updateProfile(
                    token = "Bearer $token",
                    method = methodPart,
                    bio = bioPart,
                    image = imagePart
                )
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(this@ProfileActivity, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    selectedImageUriState = null
                    fetchProfile() // Reload profile data
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Gagal memperbarui profil"
                    Toast.makeText(this@ProfileActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isSavingState = false
            }
        }
    }

    private fun deleteProfile() {
        val token = preferenceManager.getToken() ?: return
        isSavingState = true
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.profileApi.deleteProfile("Bearer $token")
                if (response.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "Profil berhasil direset ke default!", Toast.LENGTH_SHORT).show()
                    bioTextState = ""
                    selectedImageUriState = null
                    fetchProfile() // Reload to reset and show default profile details
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Gagal mereset profil"
                    Toast.makeText(this@ProfileActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isSavingState = false
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
fun ProfileScreen(
    profile: Profile?,
    user: User?,
    isLoading: Boolean,
    isSaving: Boolean,
    bioText: String,
    selectedImageUri: Uri?,
    onBioChange: (String) -> Unit,
    onImageSelected: (Uri) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit,
    onUpgradeClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    // Has changes check
    val hasChanges = (profile?.bio ?: "") != bioText || selectedImageUri != null

    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Profil Saya",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfileOnSurface
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Kembali",
                            tint = ProfilePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(ProfileBackground)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = ProfilePrimary
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Profile Header Banner with Gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(ProfilePrimary, ProfileSecondary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Circular Avatar
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .clickable { imagePickerLauncher.launch("image/*") }
                                    .background(Color.White)
                                    .padding(3.dp)
                                    .clip(CircleShape)
                            ) {
                                if (selectedImageUri != null) {
                                    AsyncImage(
                                        model = selectedImageUri,
                                        contentDescription = "Selected profile image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else if (!profile?.imageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = profile?.imageUrl,
                                        contentDescription = "Profile image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    AsyncImage(
                                        model = "https://img.freepik.com/free-vector/businessman-character-avatar-isolated_24877-60111.jpg?semt=ais_hybrid&w=740&q=80",
                                        contentDescription = "Default avatar",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                
                                // Camera Icon Overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = "Ubah Foto",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // User display name & badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = user?.name ?: "Nama Pengguna",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (profile?.aiPortfolio != null) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = "PRO",
                                        tint = PremiumGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        "PRO",
                                        color = PremiumGold,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // User Info Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, ProfileOutline.copy(alpha = 0.4f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.Person,
                                        contentDescription = null,
                                        tint = ProfilePrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            "Nama Lengkap",
                                            fontSize = 11.sp,
                                            color = ProfileOnSurfaceVariant,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            user?.name ?: "-",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = ProfileOnSurface
                                        )
                                    }
                                }

                                HorizontalDivider(color = ProfileOutline.copy(alpha = 0.2f))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.Email,
                                        contentDescription = null,
                                        tint = ProfilePrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            "Email",
                                            fontSize = 11.sp,
                                            color = ProfileOnSurfaceVariant,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            user?.email ?: "-",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = ProfileOnSurface
                                        )
                                    }
                                }
                            }
                        }

                        // Editable Bio Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, ProfileOutline.copy(alpha = 0.4f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = ProfilePrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        "Biografi Toko",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ProfileOnSurface
                                    )
                                }

                                OutlinedTextField(
                                    value = bioText,
                                    onValueChange = onBioChange,
                                    placeholder = { 
                                        Text(
                                            "Tulis deskripsi singkat tentang warung/toko Anda di sini...",
                                            fontSize = 13.sp,
                                            color = ProfileOnSurfaceVariant.copy(alpha = 0.6f)
                                        ) 
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ProfilePrimary,
                                        unfocusedBorderColor = ProfileOutline,
                                        focusedTextColor = ProfileOnSurface,
                                        unfocusedTextColor = ProfileOnSurface
                                    ),
                                    maxLines = 4,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Done
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Save Changes and Reset Profile buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = onReset,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color(0xFFBA1A1A)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFBA1A1A))
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Reset", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = onSave,
                                        enabled = hasChanges,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ProfilePrimary,
                                            contentColor = Color.White,
                                            disabledContainerColor = ProfileOutline.copy(alpha = 0.6f),
                                            disabledContentColor = Color.White
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Save,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Simpan", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // AI weekly Portfolio analysis
                        if (profile?.aiPortfolio != null) {
                            profile.aiPortfolio.portfolioInsight?.let { insight ->
                                PortfolioInsightSection(insight)
                            } ?: Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, ProfileOutline.copy(alpha = 0.4f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.Store,
                                        contentDescription = null,
                                        tint = ProfilePrimary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        "Analisis AI Sedang Diproses",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = ProfileOnSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Kecerdasan buatan kami sedang menganalisis data transaksi Anda untuk minggu ini. Harap cek kembali secara berkala.",
                                        textAlign = TextAlign.Center,
                                        fontSize = 12.sp,
                                        color = ProfileOnSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            // Promo Upgrade to PRO card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F5FF)),
                                border = BorderStroke(1.dp, ProfilePrimary.copy(alpha = 0.15f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(ProfilePrimary.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Diamond,
                                            contentDescription = null,
                                            tint = ProfilePrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "Buka AI Weekly Business Insights",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = ProfilePrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Dapatkan laporan analisis AI eksklusif mingguan (Omset, Hari Teramai, Rekomendasi Penjualan, & Produk Bintang Warung) secara otomatis.",
                                        textAlign = TextAlign.Center,
                                        fontSize = 12.sp,
                                        color = ProfileOnSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = onUpgradeClick,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ProfilePrimary,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Upgrade ke PRO Sekarang", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Loading overlay dialog
    LoadingDialog(show = isSaving, message = "Memperbarui profil...")
}

@Composable
fun PortfolioInsightSection(insight: PortfolioInsight) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ProfilePrimary.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFAFAFE), Color(0xFFF6F0FF))
                    )
                )
                .padding(20.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ProfilePrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Diamond,
                        contentDescription = null,
                        tint = ProfilePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        "AI Weekly Portfolio",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfilePrimary
                    )
                    Text(
                        "Periode: ${insight.periode ?: "-"}",
                        fontSize = 11.sp,
                        color = ProfileOnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary metrics grid
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBox(
                        title = "Total Omset",
                        value = "Rp ${formatRupiah(insight.totalOmsetMingguIni)}",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Total Transaksi",
                        value = "${insight.totalTransaksi ?: 0}",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBox(
                        title = "Rata-rata Omset / Hari",
                        value = "Rp ${formatRupiah(insight.rataRataOmsetPerHari)}",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Rata-rata Transaksi / Hari",
                        value = "${insight.rataRataTransaksiPerHari ?: "0"} / Hari",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Peak Day
            if (!insight.hariRamaiTanggal.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, ProfileOutline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Store,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Hari Teramai",
                                fontSize = 11.sp,
                                color = ProfileOnSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "${insight.hariRamaiTanggal} (Omset: Rp ${formatRupiah(insight.hariRamaiOmset)})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ProfileOnSurface
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Best Seller Section (Bintang Warung)
            val bintang = insight.bintangWarung
            if (!bintang.isNullOrEmpty()) {
                Text(
                    "⭐ Produk Bintang Warung",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfileOnSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, ProfileOutline.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        bintang.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(ProfilePrimary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        item.nama,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ProfileOnSurface
                                    )
                                }
                                Text(
                                    "${item.terjual} terjual (Rp ${formatRupiah(item.omset.toString())})",
                                    fontSize = 12.sp,
                                    color = ProfileOnSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Poor Selling Section (Produk Kurang Laku)
            val kurangLaku = insight.produkKurangLaku
            if (!kurangLaku.isNullOrEmpty()) {
                Text(
                    "⚠️ Produk Kurang Laku",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfileOnSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, ProfileOutline.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        kurangLaku.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFBA1A1A))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    item,
                                    fontSize = 12.sp,
                                    color = ProfileOnSurface
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // AI Insight Text Box
            if (!insight.insight.isNullOrBlank()) {
                Text(
                    "💡 AI Insight & Rekomendasi",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfileOnSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ProfilePrimary.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, ProfilePrimary.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        val lines = insight.insight.split("\n")
                        lines.forEachIndexed { index, line ->
                            if (line.isNotBlank()) {
                                Text(
                                    text = line,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    color = ProfileOnSurface,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(bottom = if (index == lines.lastIndex) 0.dp else 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ProfileOutline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                title,
                fontSize = 10.sp,
                color = ProfileOnSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ProfileOnSurface
            )
        }
    }
}

private fun formatRupiah(amount: String?): String {
    if (amount == null) return "0"
    return try {
        val parsed = amount.toDoubleOrNull() ?: 0.0
        val formatter = java.text.DecimalFormat("#,###")
        formatter.format(parsed).replace(",", ".")
    } catch (e: Exception) {
        amount
    }
}

@Preview(showBackground = true, name = "Profile Normal")
@Composable
fun ProfilePreview() {
    KasirAppTheme {
        ProfileScreen(
            profile = Profile(
                id = 1,
                userId = 1,
                bio = "Warung Serba Ada yang menyediakan kebutuhan harian Anda.",
                imageUrl = null,
                createdAt = null,
                updatedAt = null,
                aiPortfolio = null
            ),
            user = User(
                id = 1,
                name = "Warung Nawaf",
                email = "nawaf@example.com"
            ),
            isLoading = false,
            isSaving = false,
            bioText = "Warung Serba Ada yang menyediakan kebutuhan harian Anda.",
            selectedImageUri = null,
            onBioChange = {},
            onImageSelected = {},
            onBack = {},
            onSave = {},
            onReset = {},
            onUpgradeClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Profile PRO State")
@Composable
fun ProfileProPreview() {
    KasirAppTheme {
        ProfileScreen(
            profile = Profile(
                id = 1,
                userId = 1,
                bio = "Toko kelontong modern dengan layanan prima.",
                imageUrl = null,
                createdAt = null,
                updatedAt = null,
                aiPortfolio = com.nawaf.kasirpas.model.AiPortfolio(
                    id = 1,
                    userId = 1,
                    typeAi = "WEEKLY",
                    status = "COMPLETED",
                    generatedAt = "2023-10-27",
                    errorMessage = null,
                    createdAt = null,
                    updatedAt = null,
                    portfolioInsight = PortfolioInsight(
                        id = 1,
                        aiRunId = 1,
                        userId = 1,
                        insight = "Penjualan Anda sangat stabil minggu ini. Pertahankan stok produk 'Beras 5kg' karena peminatnya tinggi.\nCoba buat promo bundling untuk produk yang kurang laku.",
                        tanggalLaporan = "2023-10-27",
                        periode = "20 - 27 Okt 2023",
                        totalOmsetMingguIni = "1500000",
                        totalTransaksi = 45,
                        rataRataTransaksiPerHari = "6",
                        rataRataOmsetPerHari = "214000",
                        bintangWarung = listOf(
                            BintangWarung("Beras 5kg", 10, 650000.0),
                            BintangWarung("Minyak Goreng 2L", 8, 280000.0)
                        ),
                        hariRamaiTanggal = "Sabtu, 21 Okt",
                        hariRamaiOmset = "450000",
                        produkKurangLaku = listOf("Sabun Colek X", "Kopi Sachet Y"),
                        source = null,
                        generatedAt = null,
                        validUntil = null
                    )
                )
            ),
            user = User(
                id = 1,
                name = "Nawaf Pro Store",
                email = "pro@nawaf.com"
            ),
            isLoading = false,
            isSaving = false,
            bioText = "Toko kelontong modern dengan layanan prima.",
            selectedImageUri = null,
            onBioChange = {},
            onImageSelected = {},
            onBack = {},
            onSave = {},
            onReset = {},
            onUpgradeClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
fun ProfileLoadingPreview() {
    KasirAppTheme {
        ProfileScreen(
            profile = null,
            user = null,
            isLoading = true,
            isSaving = false,
            bioText = "",
            selectedImageUri = null,
            onBioChange = {},
            onImageSelected = {},
            onBack = {},
            onSave = {},
            onReset = {},
            onUpgradeClick = {}
        )
    }
}