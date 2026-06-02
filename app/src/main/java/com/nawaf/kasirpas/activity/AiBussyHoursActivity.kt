@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.nawaf.kasirpas.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.nawaf.kasirpas.activity.ui.theme.KasirAppTheme
import com.nawaf.kasirpas.api.RetrofitClient
import com.nawaf.kasirpas.model.AiRun
import com.nawaf.kasirpas.model.DailyForecast
import com.nawaf.kasirpas.model.HourlyPrediction
import com.nawaf.kasirpas.model.ProductPrediction
import com.nawaf.kasirpas.utils.PreferenceManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Premium Color System for Busy Hours
val BusyPrimary = Color(0xFF653DA7)
val BusySecondary = Color(0xFF9E70F0)
val BusyAccent = Color(0xFFFF5722) // Fire red-orange for peak hours
val BusyModerateAccent = Color(0xFFFFB300) // Orange-yellow for moderate
val BusyLowAccent = Color(0xFF4CAF50) // Green for low busy hours
val BusyBackground = Color(0xFFFCF9F8)
val BusySurface = Color(0xFFFFFFFF)
val BusyOnSurface = Color(0xFF1C1B1B)
val BusyOnSurfaceVariant = Color(0xFF6B7280)
val BusyOutline = Color(0xFFECE6F2)

class AiBussyHoursActivity : ComponentActivity() {

    private lateinit var prefManager: PreferenceManager

    // Screen State
    private var aiRunState by mutableStateOf<AiRun?>(null)
    private var selectedForecastState by mutableStateOf<DailyForecast?>(null)
    private var isLoadingState by mutableStateOf(true)
    private var isProUser by mutableStateOf(true) // default to true, checked in fetchData
    private var dataLoaded = false // prevent re-fetching on every resume

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        prefManager = PreferenceManager(this)

        setContent {
            KasirAppTheme {
                AiBussyHoursContent(
                    aiRun = aiRunState,
                    selectedForecast = selectedForecastState,
                    isLoading = isLoadingState,
                    isProUser = isProUser,
                    onBack = { finish() },
                    onRefresh = {
                        dataLoaded = false
                        fetchData()
                    },
                    onForecastSelected = { selectedForecastState = it },
                    onUpgradeClick = {
                        startActivity(Intent(this, BillingActivity::class.java))
                    }
                )
            }
        }

        if (!dataLoaded) {
            fetchData()
        }
    }


    private fun fetchData() {
        val token = prefManager.getToken() ?: return
        isLoadingState = true

        lifecycleScope.launch {
            try {
                // Check active subscription first!
                val subResponse = RetrofitClient.billingApi.getActiveSubscription("Bearer $token")
                val isSubActive = if (subResponse.isSuccessful) {
                    val activeSub = subResponse.body()?.data
                    activeSub != null && activeSub.status == "ACTIVE"
                } else {
                    false
                }

                isProUser = isSubActive
                if (!isSubActive) {
                    isLoadingState = false
                    return@launch
                }

                val response = RetrofitClient.aiApi.getLatestBusyHours("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    val aiRun = response.body()?.data
                    aiRunState = aiRun
                    if (aiRun != null && aiRun.dailyForecasts.isNotEmpty()) {
                        val allDays = aiRun.dailyForecasts
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        val todayStr = sdf.format(Date())

                        // Find index of today in forecasts
                        var startIndex = allDays.indexOfFirst { it.forecastDate.startsWith(todayStr) }
                        if (startIndex == -1) startIndex = 0

                        val limitedDays = allDays.drop(startIndex).take(3)
                        if (limitedDays.isNotEmpty()) {
                            // If previously selected day exists in the list, keep it, otherwise set first
                            val currentSelected = selectedForecastState
                            val match = limitedDays.find { it.id == currentSelected?.id }
                            selectedForecastState = match ?: limitedDays[0]
                        }
                    } else {
                        selectedForecastState = null
                    }
                } else {
                    Toast.makeText(this@AiBussyHoursActivity, "Data analisis belum tersedia.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@AiBussyHoursActivity, "Terjadi kesalahan saat memuat data", Toast.LENGTH_SHORT).show()
            } finally {
                isLoadingState = false
                dataLoaded = true
            }
        }
    }

}

@Composable
fun AiBussyHoursContent(
    aiRun: AiRun?,
    selectedForecast: DailyForecast?,
    isLoading: Boolean,
    isProUser: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onForecastSelected: (DailyForecast) -> Unit,
    onUpgradeClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Prediksi Jam Sibuk AI",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BusyOnSurface
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Kembali",
                            tint = BusyPrimary
                        )
                    }
                },
                actions = {
                    if (isProUser) {
                        IconButton(onClick = onRefresh, enabled = !isLoading) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = BusyPrimary
                            )
                        }
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
                .background(BusyBackground)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = BusyPrimary
                )
            } else if (!isProUser) {
                ProUpgradeLayout(onUpgradeClick = onUpgradeClick)
            } else if (aiRun == null) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = BusyPrimary.copy(alpha = 0.4f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Belum Ada Analisis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BusyOnSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Data analisis jam sibuk belum tersedia. Analisis akan dijalankan secara otomatis oleh sistem.",
                        fontSize = 13.sp,
                        color = BusyOnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Main Content
                val allDays = aiRun.dailyForecasts
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val todayStr = sdf.format(Date())

                // Find current day indexes to show 3 days limit
                var startIndex = allDays.indexOfFirst { it.forecastDate.startsWith(todayStr) }
                if (startIndex == -1) startIndex = 0
                val limitedDays = allDays.drop(startIndex).take(3)

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // Header Banner with Gradient & Summary Info
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(BusyPrimary, BusySecondary)
                                    )
                                )
                                .padding(horizontal = 24.dp, vertical = 24.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Prediksi Cerdas Bisnis",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Analisis Jam Sibuk",
                                            color = Color.White,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Terakhir diperbarui: ${formatRunDate(aiRun.generatedAt)}",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Horizontal Day Selector
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp, bottom = 12.dp)
                        ) {
                            Text(
                                "Pilih Hari Prediksi",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BusyOnSurface,
                                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 10.dp)
                            )

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(limitedDays) { forecast ->
                                    val isSelected = selectedForecast?.id == forecast.id
                                    DaySelectorCard(
                                        forecast = forecast,
                                        isSelected = isSelected,
                                        onClick = { onForecastSelected(forecast) }
                                    )
                                }
                            }
                        }
                    }

                    // Detailed Forecast for the Selected Day
                    if (selectedForecast != null) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "${translateDay(selectedForecast.dayName)}, ${formatForecastDate(selectedForecast.forecastDate)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BusyOnSurface
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Daily summary boxes
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    SummaryMiniCard(
                                        title = "Est. Total Transaksi",
                                        value = "${formatDecimalString(selectedForecast.totalPredictedTrx)} Trx",
                                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                                        iconTint = BusyPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    SummaryMiniCard(
                                        title = "Est. Pendapatan",
                                        value = "Rp ${formatRupiah(selectedForecast.totalPredictedRevenue)}",
                                        icon = Icons.Default.Payments,
                                        iconTint = BusyLowAccent,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = BusyAccent.copy(alpha = 0.05f)),
                                    border = BorderStroke(1.dp, BusyAccent.copy(alpha = 0.15f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(14.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(BusyAccent.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.LocalFireDepartment,
                                                contentDescription = null,
                                                tint = BusyAccent,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                "Prediksi Jam Puncak Sibuk",
                                                fontSize = 11.sp,
                                                color = BusyOnSurfaceVariant,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                selectedForecast.peakHour,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BusyAccent
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    "Prediksi Tiap Jam & Produk Terlaris",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BusyOnSurface
                                )
                            }
                        }

                        // Hourly list items
                        if (selectedForecast.hourlyPredictions.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 8.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, BusyOutline.copy(alpha = 0.5f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Tidak ada data transaksi terprediksi untuk hari ini.",
                                            fontSize = 13.sp,
                                            color = BusyOnSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(selectedForecast.hourlyPredictions) { hourForecast ->
                                HourlyForecastItem(hourForecast)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DaySelectorCard(
    forecast: DailyForecast,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) BusyPrimary else Color.White
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) BusyPrimary else BusyOutline.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = translateDay(forecast.dayName),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else BusyOnSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatShortDate(forecast.forecastDate),
                fontSize = 11.sp,
                color = if (isSelected) Color.White.copy(alpha = 0.8f) else BusyOnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.2f) else BusyPrimary.copy(alpha = 0.08f)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${formatDecimalString(forecast.totalPredictedTrx)} Trx",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else BusyPrimary
                )
            }
        }
    }
}

@Composable
fun SummaryMiniCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BusyOutline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    title,
                    fontSize = 10.sp,
                    color = BusyOnSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                value,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BusyOnSurface
            )
        }
    }
}

@Composable
fun HourlyForecastItem(prediction: HourlyPrediction) {
    val levelColor = when (prediction.busyLevel.uppercase(Locale.US)) {
        "PEAK" -> BusyAccent
        "MODERATE" -> BusyModerateAccent
        "LOW" -> BusyLowAccent
        else -> BusyLowAccent
    }

    val levelText = when (prediction.busyLevel.uppercase(Locale.US)) {
        "PEAK" -> "Puncak Sibuk"
        "MODERATE" -> "Sedang"
        "LOW" -> "Sepi"
        else -> prediction.busyLevel
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BusyOutline.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row: Time and Level Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        prediction.hour,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BusyOnSurface
                    )
                    Text(
                        text = prediction.emoji,
                        fontSize = 16.sp
                    )
                }

                // Level Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(levelColor.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        levelText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = levelColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub info stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Estimasi Transaksi:",
                    fontSize = 12.sp,
                    color = BusyOnSurfaceVariant
                )
                Text(
                    "${formatDecimalString(prediction.predictedTransactions)} Transaksi",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BusyOnSurface
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Estimasi Pendapatan:",
                    fontSize = 12.sp,
                    color = BusyOnSurfaceVariant
                )
                Text(
                    "Rp ${formatRupiah(prediction.predictedRevenue)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BusyOnSurface
                )
            }

            // Product predictions if available
            val products = prediction.productPredictions
            if (products.isNotEmpty()) {
                HorizontalDivider(
                    color = BusyOutline.copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                Text(
                    "🎯 Prediksi Produk Terlaris:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BusyPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    products.forEach { prod ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(BusyBackground)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Image or Icon
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BusyOutline.copy(alpha = 0.5f))
                            ) {
                                val imageUrl = prod.product?.imageUrl
                                if (!imageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    // Soft text representation
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            prod.productName.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = BusyPrimary,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Name & Qty
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    prod.productName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BusyOnSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "Estimasi: ${formatDecimalString(prod.estimatedQty)} unit (Rp ${formatRupiah(prod.estimatedRevenue)})",
                                    fontSize = 10.sp,
                                    color = BusyOnSurfaceVariant
                                )
                            }

                            // Probability Badge
                            val probPct = try {
                                val d = prod.probability.toDoubleOrNull() ?: 0.0
                                (d * 100).toInt()
                            } catch (e: Exception) {
                                0
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BusyPrimary.copy(alpha = 0.08f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "$probPct%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BusyPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Utility formatting functions
private fun formatRunDate(dateStr: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val date = inputFormat.parse(dateStr)
        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateStr
    }
}

private fun formatForecastDate(dateStr: String): String {
    return try {
        val inputFormat = if (dateStr.contains("T")) {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        } else {
            SimpleDateFormat("yyyy-MM-dd", Locale.US)
        }
        val date = inputFormat.parse(dateStr)
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateStr
    }
}

private fun formatShortDate(dateStr: String): String {
    return try {
        val inputFormat = if (dateStr.contains("T")) {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        } else {
            SimpleDateFormat("yyyy-MM-dd", Locale.US)
        }
        val date = inputFormat.parse(dateStr)
        val outputFormat = SimpleDateFormat("dd MMM", Locale("id", "ID"))
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateStr
    }
}

private fun translateDay(dayName: String): String {
    return when (dayName.lowercase(Locale.US)) {
        "sunday" -> "Minggu"
        "monday" -> "Senin"
        "tuesday" -> "Selasa"
        "wednesday" -> "Rabu"
        "thursday" -> "Kamis"
        "friday" -> "Jumat"
        "saturday" -> "Sabtu"
        else -> dayName
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

private fun formatDecimalString(valueStr: String): String {
    return try {
        val parsed = valueStr.toDoubleOrNull() ?: 0.0
        if (parsed % 1.0 == 0.0) {
            String.format(Locale.US, "%.0f", parsed)
        } else {
            String.format(Locale.US, "%.1f", parsed)
        }
    } catch (e: Exception) {
        valueStr
    }
}

@Preview(showBackground = true)
@Composable
fun AiBussyHoursScreenPreview() {
    val mockRun = AiRun(
        id = 1,
        userId = 1,
        typeAi = "BUSY",
        status = "COMPLETED",
        generatedAt = "2026-05-24T10:00:00.000000Z",
        dailyForecasts = listOf(
            DailyForecast(
                id = 1,
                forecastDate = "2026-05-24T00:00:00.000000Z",
                dayName = "Sunday",
                totalPredictedTrx = "1.20",
                totalPredictedRevenue = "15041.00",
                peakHour = "19:00",
                hourlyPredictions = listOf(
                    HourlyPrediction(
                        id = 1,
                        hour = "12:00",
                        predictedTransactions = "0.95",
                        predictedRevenue = "45000.00",
                        busyLevel = "MODERATE",
                        emoji = "😌",
                        productPredictions = listOf(
                            ProductPrediction(
                                id = 1,
                                productName = "Air mineral 600ml",
                                probability = "0.85",
                                estimatedQty = "12",
                                estimatedRevenue = "1050.00",
                                product = null
                            )
                        )
                    ),
                    HourlyPrediction(
                        id = 2,
                        hour = "19:00",
                        predictedTransactions = "1.50",
                        predictedRevenue = "15023.00",
                        busyLevel = "PEAK",
                        emoji = "🔥",
                        productPredictions = listOf(
                            ProductPrediction(
                                id = 2,
                                productName = "Sabun cuci piring",
                                probability = "0.92",
                                estimatedQty = "8",
                                estimatedRevenue = "16500.00",
                                product = null
                            )
                        )
                    )
                )
            )
        )
    )

    KasirAppTheme {
        AiBussyHoursContent(
            aiRun = mockRun,
            selectedForecast = mockRun.dailyForecasts[0],
            isLoading = false,
            isProUser = true,
            onBack = {},
            onRefresh = {},
            onForecastSelected = {},
            onUpgradeClick = {}
        )
    }
}

@Composable
private fun ProUpgradeLayout(onUpgradeClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF653DA7).copy(alpha = 0.15f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
                .border(
                    2.dp, Color(0xFF653DA7).copy(alpha = 0.2f), CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = "Locked Feature",
                tint = Color(0xFF653DA7),
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Prediksi Jam Sibuk AI",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF653DA7).copy(alpha = 0.1f)
        ) {
            Text(
                text = "Fitur PRO",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF653DA7)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Gunakan kecerdasan buatan untuk memprediksi jam sibuk dan omset pendapatan per jam secara cerdas.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            BenefitRow(
                icon = Icons.Default.Schedule,
                title = "Prediksi Per Jam",
                desc = "Ketahui jam sibuk dan sepi setiap hari secara detail"
            )
            BenefitRow(
                icon = Icons.Default.Payments,
                title = "Proyeksi Pendapatan",
                desc = "Estimasi pendapatan per jam untuk perencanaan lebih baik"
            )
            BenefitRow(
                icon = Icons.Default.ShoppingCart,
                title = "Produk Terlaris",
                desc = "Lihat produk yang perlu diprioritaskan di setiap jam"
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onUpgradeClick,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF653DA7),
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(14.dp),
                    spotColor = Color(0xFF653DA7).copy(alpha = 0.4f)
                )
        ) {
            Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Upgrade Langganan PRO",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun BenefitRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF653DA7).copy(alpha = 0.1f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF653DA7), modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
            Text(desc, fontSize = 12.sp, color = Color(0xFF64748B))
        }
    }
}
