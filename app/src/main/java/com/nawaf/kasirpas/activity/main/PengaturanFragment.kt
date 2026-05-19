package com.nawaf.kasirpas.activity.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.nawaf.kasirpas.activity.AiBussyHoursActivity
import com.nawaf.kasirpas.activity.AiStocksActivity
import com.nawaf.kasirpas.activity.KelolaKategoriActivity
import com.nawaf.kasirpas.activity.KelolaProductActivity
import com.nawaf.kasirpas.activity.LoginActivity
import com.nawaf.kasirpas.activity.ui.theme.KasirAppTheme
import com.nawaf.kasirpas.api.RetrofitClient
import com.nawaf.kasirpas.utils.PreferenceManager
import kotlinx.coroutines.launch

// Shared Design Colors (Matching BillingActivity)
val SettingsPrimary = Color(0xFF653DA7)
val SettingsSurface = Color(0xFFFCF9F8)
val SettingsOnSurface = Color(0xFF1C1B1B)
val SettingsOnSurfaceVariant = Color(0xFF4A4452)

class PengaturanFragment : Fragment() {

    private lateinit var prefManager: PreferenceManager
    private var isProMax by mutableStateOf(false)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        prefManager = PreferenceManager(requireContext())
        
        return ComposeView(requireContext()).apply {
            setContent {
                KasirAppTheme {
                    PengaturanScreen(
                        prefManager = prefManager,
                        isProMax = isProMax,
                        onLogout = { performLogout() },
                        onNavigate = { activityClass ->
                            startActivity(Intent(requireContext(), activityClass))
                        },
                        onAiFeatureClick = { activityClass ->
                            if (isProMax) {
                                startActivity(Intent(requireContext(), activityClass))
                            } else {
                                Toast.makeText(requireContext(), "🔒 Fitur ini hanya untuk member PROMAX", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkSubscriptionStatus()
    }

    private fun checkSubscriptionStatus() {
        val token = prefManager.getToken() ?: return
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.billingApi.getActiveSubscription("Bearer $token")
                if (response.isSuccessful) {
                    val activeSub = response.body()?.data
                    isProMax = activeSub != null && activeSub.status == "ACTIVE" && activeSub.planName == "PRO_MAX"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun performLogout() {
        prefManager.clear()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PengaturanScreen(
    prefManager: PreferenceManager,
    isProMax: Boolean,
    onLogout: () -> Unit,
    onNavigate: (Class<*>) -> Unit,
    onAiFeatureClick: (Class<*>) -> Unit
) {
    val user = prefManager.getUser()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Pengaturan",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SettingsOnSurface
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            LuxuryBottomNav()
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(SettingsSurface)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Profile Section
            if (user != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = "https://img.freepik.com/free-vector/businessman-character-avatar-isolated_24877-60111.jpg",
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = user.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SettingsOnSurface
                        )
                        Text(
                            text = "Store Manager • ${user.email}",
                            fontSize = 14.sp,
                            color = SettingsOnSurfaceVariant
                        )
                    }
                }
            }

            // General Management
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Manajemen Toko",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SettingsPrimary,
                    modifier = Modifier.padding(start = 4.dp)
                )
                SettingsItem(
                    icon = Icons.Outlined.Category,
                    title = "Kelola Kategori",
                    onClick = { onNavigate(KelolaKategoriActivity::class.java) }
                )
                SettingsItem(
                    icon = Icons.Outlined.Inventory2,
                    title = "Kelola Produk",
                    onClick = { onNavigate(KelolaProductActivity::class.java) }
                )
            }

            // AI Features Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Fitur AI (PROMAX)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SettingsPrimary,
                    modifier = Modifier.padding(start = 4.dp)
                )
                SettingsItem(
                    icon = Icons.AutoMirrored.Outlined.ShowChart,
                    title = "Analisis Stok AI",
                    isLocked = !isProMax,
                    onClick = { onAiFeatureClick(AiStocksActivity::class.java) }
                )
                SettingsItem(
                    icon = Icons.Outlined.Timer,
                    title = "Prediksi Jam Sibuk",
                    isLocked = !isProMax,
                    onClick = { onAiFeatureClick(AiBussyHoursActivity::class.java) }
                )
            }

            // Account Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = "Keluar Akun",
                    titleColor = Color.Red,
                    showChevron = false,
                    onClick = onLogout
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    titleColor: Color = SettingsOnSurface,
    isLocked: Boolean = false,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp,
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isLocked) Color.Gray else SettingsPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isLocked) Color.Gray else titleColor,
                modifier = Modifier.weight(1f)
            )
            if (showChevron) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun LuxuryBottomNav() {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.height(72.dp)
    ) {
        val items = listOf(
            Triple("Home", Icons.Outlined.Home, false),
            Triple("Sales", Icons.AutoMirrored.Outlined.ReceiptLong, false),
            Triple("Inventory", Icons.Outlined.Inventory2, false),
            Triple("Settings", Icons.Outlined.Settings, true)
        )
        
        items.forEach { (label, icon, isSelected) ->
            NavigationBarItem(
                selected = isSelected,
                onClick = { /* Nav logic managed by MainActivity */ },
                icon = { 
                    Icon(
                        icon, 
                        contentDescription = label,
                        modifier = Modifier.size(24.dp)
                    ) 
                },
                label = { 
                    Text(
                        label, 
                        fontSize = 10.sp, 
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    ) 
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SettingsPrimary,
                    selectedTextColor = SettingsPrimary,
                    unselectedIconColor = Color.Gray.copy(alpha = 0.6f),
                    unselectedTextColor = Color.Gray.copy(alpha = 0.6f),
                    indicatorColor = SettingsPrimary.copy(alpha = 0.1f)
                )
            )
        }
    }
}
