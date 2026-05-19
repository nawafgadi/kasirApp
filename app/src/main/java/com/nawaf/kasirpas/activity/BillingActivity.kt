package com.nawaf.kasirpas.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.nawaf.kasirpas.activity.ui.theme.KasirAppTheme
import com.nawaf.kasirpas.api.RetrofitClient
import com.nawaf.kasirpas.request.BillingRequest
import com.nawaf.kasirpas.utils.PreferenceManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Luxury Brand Colors
val BillingPrimary = Color(0xFF653DA7)
val BillingSecondary = Color(0xFF655B68)
val BillingSurface = Color(0xFFFCF9F8)
val BillingOnSurface = Color(0xFF1C1B1B)
val BillingOnSurfaceVariant = Color(0xFF4A4452)
val BillingSecondaryContainer = Color(0xFFECDEEE)
val BillingOnSecondaryContainer = Color(0xFF6B616E)

class BillingActivity : ComponentActivity() {

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        preferenceManager = PreferenceManager(this)

        setContent {
            KasirAppTheme {
                BillingScreen(
                    onBack = { finish() },
                    preferenceManager = preferenceManager
                ) { planName -> subscribe(planName) }
            }
        }
    }

    private fun subscribe(planName: String) {
        val token = preferenceManager.getToken()
        if (token == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val request = BillingRequest(planName)
                val response = RetrofitClient.billingApi.subscribe("Bearer $token", request)
                
                if (response.isSuccessful && response.body() != null) {
                    val paymentUrl = response.body()!!.paymentUrl
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl))
                    startActivity(intent)
                } else {
                    Toast.makeText(this@BillingActivity, "Gagal membuat pesanan", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@BillingActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    onBack: () -> Unit,
    preferenceManager: PreferenceManager,
    onSubscribe: (String) -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun fetchActiveSubscription() {
        val token = preferenceManager.getToken() ?: return
        isLoading = true
        scope.launch {
            try {
                RetrofitClient.billingApi.getActiveSubscription("Bearer $token")
                // We just trigger the call, but don't display the card as requested
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchActiveSubscription()
    }

    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Plans & Billing",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BillingOnSurface
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = BillingPrimary
                        )
                    }
                },
                actions = {
                    Text(
                        "LuxePOS",
                        modifier = Modifier.padding(end = 16.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BillingPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            LuxuryBottomNav()
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(BillingSurface)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = BillingPrimary
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    HeroSection()
                    PricingCardPro(onSubscribe)
                    BentoFeaturedSection()
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun HeroSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 16.dp)
    ) {
        Text(
            text = "Upgrade Bisnis Anda",
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = BillingPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 50.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Pilih paket yang sesuai dengan skala operasional retail Anda. Mulai dengan dasar yang kuat atau maksimalkan potensi dengan AI.",
            fontSize = 16.sp,
            color = BillingOnSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.fillMaxWidth(0.9f)
        )
    }
}

@Composable
fun PricingCardPro(onSubscribe: (String) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "buttonScale")

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = BillingPrimary.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Badge EFISIENSI
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                shape = CircleShape,
                color = BillingSecondaryContainer
            ) {
                Text(
                    text = "EFISIENSI",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BillingOnSecondaryContainer,
                    letterSpacing = 1.sp
                )
            }

            Column(modifier = Modifier.padding(28.dp)) {
                Text(
                    text = "Pro",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BillingOnSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "Rp 29.000",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BillingOnSurface
                    )
                    Text(
                        text = "/bulan",
                        fontSize = 16.sp,
                        color = BillingOnSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(28.dp))
                
                val features = listOf(
                    "Impor Transaksi",
                    "Ekspor Laporan",
                    "Manajemen Inventaris Dasar",
                    "Analisis Bisnis Mendalam"
                )
                
                features.forEach { feature ->
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = BillingPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = feature,
                            fontSize = 15.sp,
                            color = BillingOnSurfaceVariant,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { onSubscribe("PRO") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .scale(scale),
                    interactionSource = interactionSource,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BillingPrimary),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Text(
                        text = "Pilih Paket",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun BentoFeaturedSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Aman & Terpercaya Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0EDED))
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Aman & Terpercaya",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BillingOnSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Data transaksi Anda dilindungi dengan enkripsi tingkat perbankan. Backup otomatis setiap 24 jam.",
                        fontSize = 14.sp,
                        color = BillingOnSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(BillingPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = BillingPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
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
            Triple("Billing", Icons.Outlined.Payments, true)
        )
        
        items.forEach { (label, icon, isSelected) ->
            NavigationBarItem(
                selected = isSelected,
                onClick = { /* Nav logic */ },
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
                    selectedIconColor = BillingPrimary,
                    selectedTextColor = BillingPrimary,
                    unselectedIconColor = BillingSecondary.copy(alpha = 0.6f),
                    unselectedTextColor = BillingSecondary.copy(alpha = 0.6f),
                    indicatorColor = BillingPrimary.copy(alpha = 0.1f)
                )
            )
        }
    }
}
