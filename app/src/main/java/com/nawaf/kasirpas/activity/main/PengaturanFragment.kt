package com.nawaf.kasirpas.activity.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.nawaf.kasirpas.activity.AiBussyHoursActivity
import com.nawaf.kasirpas.activity.AiStocksActivity
import com.nawaf.kasirpas.activity.KelolaKategoriActivity
import com.nawaf.kasirpas.activity.KelolaProductActivity
import com.nawaf.kasirpas.activity.LoginActivity
import com.nawaf.kasirpas.activity.ui.theme.KasirAppTheme
import com.nawaf.kasirpas.utils.PreferenceManager

// Luxury Design Colors
val SettingsPrimary = Color(0xFF653DA7)
val SettingsSurface = Color(0xFFFCF9F8)
val SettingsOnSurface = Color(0xFF1C1B1B)
val SettingsOnSurfaceVariant = Color(0xFF757575)

class PengaturanFragment : Fragment() {

    private lateinit var prefManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        prefManager = PreferenceManager(requireContext())

        return ComposeView(requireContext()).apply {
            setContent {
                KasirAppTheme {
                    PengaturanScreen(
                        onLogout = { performLogout() },
                        onNavigate = { activityClass ->
                            startActivity(Intent(requireContext(), activityClass))
                        }
                    )
                }
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
@Preview(showBackground = true)
@Composable
fun PengaturanScreenPreview() {
    KasirAppTheme {
        // Panggil fungsi asli dengan memberikan lambda kosong atau dummy
        PengaturanScreen(
            onLogout = { /* Tidak melakukan apa-apa di preview */ },
            onNavigate = { /* Tidak melakukan apa-apa di preview */ }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PengaturanScreen(
    onLogout: () -> Unit,
    onNavigate: (Class<*>) -> Unit
) {
    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = SettingsSurface,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Modern Header Title
            item {
                Text(
                    text = "Pengaturan",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = SettingsOnSurface,
                    modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
                )
            }

            // Manajemen Toko Section
            item {
                Column {
                    Text(
                        text = "MANAJEMEN TOKO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SettingsOnSurfaceVariant,
                        modifier = Modifier.padding(start = 12.dp, bottom = 12.dp)
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(28.dp),
                                spotColor = Color.Black.copy(alpha = 0.05f)
                            ),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        Column {
                            PremiumSettingsRow(
                                icon = Icons.Outlined.Category,
                                title = "Kelola Kategori",
                                onClick = { onNavigate(KelolaKategoriActivity::class.java) }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                thickness = 0.5.dp,
                                color = Color.LightGray.copy(alpha = 0.2f)
                            )
                            PremiumSettingsRow(
                                icon = Icons.Outlined.Inventory2,
                                title = "Kelola Produk",
                                onClick = { onNavigate(KelolaProductActivity::class.java) }
                            )
                        }
                    }
                }
            }

            // Fitur Pintar Pro Section
            item {
                Column {
                    Text(
                        text = "FITUR PINTAR PRO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SettingsOnSurfaceVariant,
                        modifier = Modifier.padding(start = 12.dp, bottom = 12.dp)
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(28.dp),
                                spotColor = Color.Black.copy(alpha = 0.05f)
                            ),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        Column {
                            PremiumSettingsRow(
                                icon = Icons.AutoMirrored.Outlined.ShowChart,
                                title = "Analisis Stok AI",
                                onClick = { onNavigate(AiStocksActivity::class.java) }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                thickness = 0.5.dp,
                                color = Color.LightGray.copy(alpha = 0.2f)
                            )
                            PremiumSettingsRow(
                                icon = Icons.Outlined.Timer,
                                title = "Prediksi Jam Sibuk",
                                onClick = { onNavigate(AiBussyHoursActivity::class.java) }
                            )
                        }
                    }
                }
            }

            // Logout Section
            item {
                Spacer(modifier = Modifier.height(40.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(Color(0xFFFFEBEE), CircleShape)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Keluar Akun",
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun PremiumSettingsRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = ripple(color = SettingsPrimary.copy(alpha = 0.03f))
            )
            .padding(vertical = 18.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pastel background for icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(SettingsPrimary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SettingsPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(18.dp))
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = SettingsOnSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.LightGray.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
    }
}
