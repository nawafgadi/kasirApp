package com.nawaf.kasirpas.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.lifecycleScope
import com.nawaf.kasirpas.activity.ui.theme.KasirAppTheme
import com.nawaf.kasirpas.api.RetrofitClient
import com.nawaf.kasirpas.model.AiRecommendation
import com.nawaf.kasirpas.model.AiRecommendationAction
import com.nawaf.kasirpas.model.AiStockRun
import com.nawaf.kasirpas.request.AiActionRequest
import com.nawaf.kasirpas.utils.PreferenceManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDateString(dateStr: String): String {
    val cleanDateStr = when {
        dateStr.contains("T") -> dateStr.substringBefore("T")
        dateStr.contains(" ") -> dateStr.substringBefore(" ")
        else -> dateStr
    }
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = parser.parse(cleanDateStr)
        if (date != null) {
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            formatter.format(date)
        } else {
            cleanDateStr
        }
    } catch (e: Exception) {
        cleanDateStr
    }
}

class AiStocksActivity : ComponentActivity() {
    private lateinit var prefManager: PreferenceManager

    // State pemantauan UI
    private var uiState by mutableStateOf(AiStocksUiState.LOADING)
    private var errorMessage by mutableStateOf("")
    private var aiStockRun by mutableStateOf<AiStockRun?>(null)
    private var dataLoaded = false // prevent re-fetching on every resume

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefManager = PreferenceManager(this)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            KasirAppTheme {
                AiStocksScreen(
                    uiState = uiState,
                    errorMessage = errorMessage,
                    aiStockRun = aiStockRun,
                    onBackClick = { finish() },
                    onRefresh = {
                        dataLoaded = false
                        fetchData()
                    },
                    onActionClick = { recommendationId, actionType, customQty ->
                        updateAction(recommendationId, actionType, customQty)
                    },
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
        val token = prefManager.getToken()
        if (token == null) {
            uiState = AiStocksUiState.ERROR
            errorMessage = "Token otentikasi tidak ditemukan. Harap login kembali."
            return
        }

        uiState = AiStocksUiState.LOADING
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

                if (!isSubActive) {
                    uiState = AiStocksUiState.PRO
                    return@launch
                }

                val response = RetrofitClient.aiApi.getLatestStocks("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    val runData = response.body()?.data
                    if (runData != null && runData.aiRecommendations.isNotEmpty()) {
                        aiStockRun = runData
                        uiState = AiStocksUiState.SUCCESS
                    } else {
                        uiState = AiStocksUiState.EMPTY
                    }
                } else {
                    when (response.code()) {
                        403 -> {
                            uiState = AiStocksUiState.PRO
                        }
                        404 -> {
                            uiState = AiStocksUiState.EMPTY
                        }
                        else -> {
                            uiState = AiStocksUiState.ERROR
                            val errorBody = response.errorBody()?.string()
                            errorMessage = try {
                                org.json.JSONObject(errorBody ?: "").getString("message")
                            } catch (e: Exception) {
                                "Gagal memuat rekomendasi stok"
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                uiState = AiStocksUiState.ERROR
                errorMessage = "Terjadi kesalahan koneksi. Pastikan internet Anda aktif."
            } finally {
                dataLoaded = true
            }
        }
    }

    private fun updateAction(recommendationId: Int, actionType: String, customQty: Int?) {
        val token = prefManager.getToken() ?: return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.aiApi.updateRecommendationAction(
                    token = "Bearer $token",
                    recommendationId = recommendationId,
                    body = AiActionRequest(actionType = actionType)
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val productName = aiStockRun?.aiRecommendations?.find { it.id == recommendationId }?.productName ?: "Produk"
                    val successMsg = if (actionType == "DONE") {
                        "Restok produk $productName disetujui: ${customQty ?: 0} unit!"
                    } else {
                        "Rekomendasi $productName berhasil diabaikan."
                    }
                    Toast.makeText(this@AiStocksActivity, successMsg, Toast.LENGTH_SHORT).show()

                    // Update UI state secara lokal terlebih dahulu demi UX yang smooth
                    val currentRun = aiStockRun
                    if (currentRun != null) {
                        val updatedRecommendations = currentRun.aiRecommendations.map { rec ->
                            if (rec.id == recommendationId) {
                                val mockAction = AiRecommendationAction(
                                    id = response.body()?.data?.id ?: 0,
                                    aiRecommendationId = recommendationId,
                                    actionType = actionType,
                                    actionAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date()),
                                    createdAt = null,
                                    updatedAt = null
                                )
                                rec.copy(aiRecommendationActions = listOf(mockAction))
                            } else rec
                        }
                        aiStockRun = currentRun.copy(aiRecommendations = updatedRecommendations)
                    }

                    // Pemicu fetch background untuk sinkronisasi mutakhir dengan server
                    fetchLatestDataInSilence()
                } else {
                    if (response.code() == 403) {
                        uiState = AiStocksUiState.PRO
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMsg = try {
                            org.json.JSONObject(errorBody ?: "").getString("message")
                        } catch (e: Exception) {
                            "Gagal memperbarui tindakan"
                        }
                        Toast.makeText(this@AiStocksActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@AiStocksActivity, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchLatestDataInSilence() {
        val token = prefManager.getToken() ?: return
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.aiApi.getLatestStocks("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    val runData = response.body()?.data
                    if (runData != null) {
                        aiStockRun = runData
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

enum class AiStocksUiState {
    LOADING,
    SUCCESS,
    EMPTY,
    PRO,
    ERROR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiStocksScreen(
    uiState: AiStocksUiState,
    errorMessage: String,
    aiStockRun: AiStockRun?,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onActionClick: (Int, String, Int?) -> Unit,
    onUpgradeClick: () -> Unit
) {
    var selectedRecForRestock by remember { mutableStateOf<AiRecommendation?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AI Stock Optimizer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF1E293B)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh, enabled = uiState != AiStocksUiState.LOADING) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Segarkan",
                            tint = Color(0xFF1E293B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    scrolledContainerColor = Color.White
                ),
                modifier = Modifier.shadow(2.dp)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                AiStocksUiState.LOADING -> {
                    LoadingLayout()
                }

                AiStocksUiState.PRO -> {
                    ProUpgradeLayout(onUpgradeClick = onUpgradeClick)
                }

                AiStocksUiState.EMPTY -> {
                    EmptyLayout()
                }

                AiStocksUiState.ERROR -> {
                    ErrorLayout(errorMessage = errorMessage, onRetryClick = onRefresh)
                }

                AiStocksUiState.SUCCESS -> {
                    if (aiStockRun != null) {
                        SuccessLayout(
                            run = aiStockRun,
                            onRestockClick = { rec -> selectedRecForRestock = rec },
                            onIgnoreClick = { rec -> onActionClick(rec.id, "IGNORE", null) }
                        )
                    } else {
                        EmptyLayout()
                    }
                }
            }

            // Dialog pemilihan Restock kustom
            selectedRecForRestock?.let { rec ->
                RestockDialog(
                    recommendation = rec,
                    onDismiss = { selectedRecForRestock = null },
                    onConfirm = { qty ->
                        onActionClick(rec.id, "DONE", qty)
                        selectedRecForRestock = null
                    }
                )
            }
        }
    }
}

@Composable
fun LoadingLayout() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = Color(0xFF4F46E5),
            strokeWidth = 3.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Menghubungkan ke Mesin AI...",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF64748B)
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
            text = "Optimasi Stok AI",
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
            text = "Prediksikan stok barang Anda secara cerdas selama 14 hari ke depan untuk menghindari kekurangan stok dan memaksimalkan pendapatan.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            BenefitRow(
                icon = Icons.Default.Inventory,
                title = "Prediksi Stok 14 Hari",
                desc = "Analisis kebutuhan stok 14 hari ke depan dengan akurasi AI"
            )
            BenefitRow(
                icon = Icons.Default.TrendingUp,
                title = "Insight Musiman",
                desc = "Rekomendasi berdasarkan tren penjualan musiman"
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

@Composable
fun EmptyLayout() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Inventory2,
            contentDescription = "Empty Stock recommendations",
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Belum Ada Analisis Stok",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Data analisis stok belum tersedia. Analisis akan dijalankan secara otomatis oleh sistem.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun ErrorLayout(
    errorMessage: String,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = "Error Icon",
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Gagal Mengambil Data",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetryClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4F46E5)
            )
        ) {
            Text(text = "Coba Lagi")
        }
    }
}

@Composable
fun SuccessLayout(
    run: AiStockRun,
    onRestockClick: (AiRecommendation) -> Unit,
    onIgnoreClick: (AiRecommendation) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        run.seasonalInsight?.let { insight ->
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
                    border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF818CF8), shape = CircleShape)
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "Insight Musiman",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "Insight Penjualan AI",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3730A3),
                                fontSize = 15.sp
                            )
                        }

                        if (insight.hasUpcomingHoliday == true) {
                            val adviceText = insight.insight
                            if (!adviceText.isNullOrEmpty()) {
                                Text(
                                    text = adviceText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF312E81),
                                    lineHeight = 20.sp
                                )
                            }

                            insight.trends?.let { trends ->
                                if (trends.isNotEmpty()) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        trends.forEach { trend ->
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        Color(0xFFE0E7FF),
                                                        shape = RoundedCornerShape(6.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = trend,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF4338CA),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Tidak ada hari raya dalam beberapa hari ke depan. Terima kasih.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF64748B),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Rekomendasi Restock AI",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // List item rekomendasi
        items(
            items = run.aiRecommendations,
            key = { it.id }
        ) { recommendation ->
            RecommendationItem(
                recommendation = recommendation,
                onRestockClick = onRestockClick,
                onIgnoreClick = onIgnoreClick
            )
        }
    }
}

@Composable
fun RecommendationItem(
    recommendation: AiRecommendation,
    onRestockClick: (AiRecommendation) -> Unit,
    onIgnoreClick: (AiRecommendation) -> Unit
) {
    // Mengecek apakah sudah ada riwayat tindakan pada item ini
    val activeAction = recommendation.aiRecommendationActions?.lastOrNull()
    val isActionProcessed = activeAction != null

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Nama Produk & Label Risiko
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recommendation.productName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    recommendation.product?.sku?.let { sku ->
                        Text(
                            text = sku,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Urgency Badge
                val (badgeColor, badgeTextColor, badgeLabel) = when (recommendation.riskLevel?.uppercase()) {
                    "CRITICAL" -> Triple(Color(0xFFFEE2E2), Color(0xFFEF4444), recommendation.restockLabel ?: "Critical")
                    "HIGH" -> Triple(Color(0xFFFEE2E2), Color(0xFFEF4444), recommendation.restockLabel ?: "High Risk")
                    "MEDIUM" -> Triple(Color(0xFFFEF3C7), Color(0xFFF59E0B), recommendation.restockLabel ?: "Medium Risk")
                    "NORMAL" -> Triple(Color(0xFFD1FAE5), Color(0xFF10B981), recommendation.restockLabel ?: "Normal")
                    else -> Triple(Color(0xFFD1FAE5), Color(0xFF10B981), recommendation.restockLabel ?: "Safe")
                }

                Box(
                    modifier = Modifier
                        .background(badgeColor, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = badgeLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor
                    )
                }
            }

            Divider(color = Color(0xFFF1F5F9))

            // Metrics: Stok Saat ini & Penjualan rata-rata harian
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("STOK SAAT INI", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    Text(
                        text = "${recommendation.currentStock} Unit",
                        fontWeight = FontWeight.Bold,
                        color = if (recommendation.currentStock <= 5) Color(0xFFEF4444) else Color(0xFF334155),
                        fontSize = 15.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("RATA-RATA HARIAN", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    Text(
                        text = "${recommendation.avgDailySales} Unit/Hari",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155),
                        fontSize = 15.sp
                    )
                }
            }

            // Keterangan Habis Prediksi AI
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Error,
                    contentDescription = "Prediksi Habis",
                    tint = Color(0xFFE11D48),
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = recommendation.urgencyDescription ?: "Stok kritis diprediksi segera habis.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE11D48)
                    )
                    recommendation.estimatedEmptyDate?.let { date ->
                        Text(
                            text = "Estimasi habis: ${formatDateString(date)}" + (recommendation.daysUntilEmpty?.let { " ($it hari lagi)" } ?: ""),
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            // Rekomendasi Restock Qty & Resiko
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Rekomendasi Restock: ${recommendation.recommendRestockQty} Unit",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF4F46E5)
                )
                recommendation.risk?.let { riskMsg ->
                    Text(
                        text = "Risiko: $riskMsg",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 16.sp
                    )
                }
            }

            // Rekomendasi Musiman (jika ada)
            recommendation.seasonalRecommendation?.let { seasonal ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F3FF), shape = RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFDDD6FE), shape = RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Event,
                            contentDescription = "Event Musiman",
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Rekomendasi Musiman (${seasonal.holiday ?: "Event"})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7C3AED)
                        )
                    }
                    seasonal.label?.let { label ->
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF5B21B6)
                        )
                    }
                    if (!seasonal.reason.isNullOrBlank()) {
                        Text(
                            text = seasonal.reason,
                            fontSize = 11.sp,
                            color = Color(0xFF6D28D9),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Tindakan / Aksi
            if (isActionProcessed && activeAction != null) {
                val (bgActionColor, textActionColor, actionLabel) = if (activeAction.actionType == "DONE") {
                    Triple(Color(0xFFD1FAE5), Color(0xFF065F46), "Disetujui ✓")
                } else {
                    Triple(Color(0xFFF1F5F9), Color(0xFF475569), "Diabaikan ✕")
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgActionColor, shape = RoundedCornerShape(10.dp))
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = actionLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = textActionColor
                    )
                }
            } else if (recommendation.riskLevel?.uppercase() != "NORMAL") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onIgnoreClick(recommendation) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF64748B)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text("Abaikan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = { onRestockClick(recommendation) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4F46E5)
                        ),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Restock",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            "Restok",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RestockDialog(
    recommendation: AiRecommendation,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    // Range restock minimum ke maksimum (menggunakan seasonal recommendation jika ada)
    val minVal = (recommendation.seasonalRecommendation?.min ?: recommendation.restockMin ?: 1).toFloat().coerceAtLeast(1f)
    val maxVal = (recommendation.seasonalRecommendation?.max ?: recommendation.restockMax ?: minVal.toInt()).toFloat().coerceAtLeast(minVal)
    val defaultVal = (if (recommendation.seasonalRecommendation != null) {
        recommendation.seasonalRecommendation.max ?: recommendation.recommendRestockQty
    } else {
        recommendation.recommendRestockQty
    }).toFloat().coerceIn(minVal, maxVal)

    var selectedQty by remember { mutableStateOf(defaultVal.toInt()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title (tambahkan nama produk secara jelas agar user tahu produk apa yang di-restok)
                Text(
                    text = "Konfirmasi Restok:\n${recommendation.productName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Text(
                    text = "Tentukan jumlah kuantitas stok terbaik untuk ${recommendation.productName} antara batas aman rekomendasi AI.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B),
                    lineHeight = 20.sp
                )

                // Info Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9), shape = RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("MINIMAL", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        Text("${recommendation.seasonalRecommendation?.min ?: recommendation.restockMin ?: 0} Unit", fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (recommendation.seasonalRecommendation != null) "AI MUSIMAN" else "AI DEFAULT",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${recommendation.seasonalRecommendation?.max ?: recommendation.recommendRestockQty} Unit",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F46E5)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("MAKSIMAL", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        Text("${recommendation.seasonalRecommendation?.max ?: recommendation.restockMax ?: 0} Unit", fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    }
                }

                // seasonal recommendation info box (jika ada)
                recommendation.seasonalRecommendation?.let { seasonal ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F3FF), shape = RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFDDD6FE), shape = RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Event,
                                    contentDescription = "Event Musiman",
                                    tint = Color(0xFF7C3AED),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Rekomendasi Musiman (${seasonal.holiday ?: "Event"})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7C3AED)
                                )
                            }
                            seasonal.label?.let { label ->
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF5B21B6)
                                )
                            }
                        }
                    }
                }

                // Stepper + Label Kuantitas Terpilih
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Jumlah Dipesan:",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155),
                        fontSize = 14.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Button minus
                        IconButton(
                            onClick = { if (selectedQty > minVal.toInt()) selectedQty-- },
                            enabled = selectedQty > minVal.toInt(),
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (selectedQty > minVal.toInt()) Color(0xFFF1F5F9) else Color(0xFFF8FAFC),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Kurang",
                                tint = if (selectedQty > minVal.toInt()) Color(0xFF4F46E5) else Color(0xFFCBD5E1),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = "$selectedQty",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF4F46E5),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        // Button plus
                        IconButton(
                            onClick = { if (selectedQty < maxVal.toInt()) selectedQty++ },
                            enabled = selectedQty < maxVal.toInt(),
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (selectedQty < maxVal.toInt()) Color(0xFFF1F5F9) else Color(0xFFF8FAFC),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Tambah",
                                tint = if (selectedQty < maxVal.toInt()) Color(0xFF4F46E5) else Color(0xFFCBD5E1),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Slider Pemilihan
                if (maxVal > minVal) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Slider(
                            value = selectedQty.toFloat(),
                            onValueChange = { selectedQty = it.toInt() },
                            valueRange = minVal..maxVal,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF4F46E5),
                                activeTrackColor = Color(0xFF818CF8),
                                inactiveTrackColor = Color(0xFFE2E8F0)
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Geser untuk merubah jumlah",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                            val isRecommendedQtySelected = if (recommendation.seasonalRecommendation != null) {
                                selectedQty == recommendation.seasonalRecommendation.max
                            } else {
                                selectedQty == recommendation.recommendRestockQty
                            }
                            if (isRecommendedQtySelected) {
                                Text(
                                    text = if (recommendation.seasonalRecommendation != null) "Rekomendasi Musiman Terpilih" else "Rekomendasi AI Terpilih",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Dialog Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B)),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Batal", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onConfirm(selectedQty) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4F46E5),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                    ) {
                        Text("Terima", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AiStocksScreenPreview() {
    KasirAppTheme {
        AiStocksScreen(
            uiState = AiStocksUiState.SUCCESS,
            errorMessage = "",
            aiStockRun = AiStockRun(
                id = 12,
                userId = 3,
                typeAi = "STOCKS",
                status = "COMPLETED",
                generatedAt = "2026-05-21T16:22:29.000000Z",
                errorMessage = null,
                seasonalInsight = com.nawaf.kasirpas.model.SeasonalInsight(
                    insightOverride = "Penjualan meningkat sebesar 25% pada akhir pekan untuk kategori minuman segar.",
                    trendsOverride = listOf("Weekend spike", "Warm weather preference")
                ),
                totalProducts = 2,
                createdAt = "2026-05-21T16:22:29.000000Z",
                updatedAt = "2026-05-21T16:22:29.000000Z",
                aiRecommendations = listOf(
                    AiRecommendation(
                        id = 45,
                        aiRunId = 12,
                        productId = 8,
                        productName = "Kopi Susu Gula Aren",
                        productPrice = "18000.00",
                        currentStock = 5,
                        avgDailySales = "12.50",
                        recommendRestockQty = 50,
                        restockMin = 20,
                        restockMax = 50,
                        restockLabel = "High Restock Needed",
                        targetDaysCoverage = 14,
                        riskLevel = "CRITICAL",
                        urgencyDescription = "Stok kritis akan habis dalam kurun waktu 1 hari.",
                        daysUntilEmpty = 1,
                        estimatedEmptyDate = "2026-05-22",
                        risk = "Kehilangan potensi omset harian akibat kehabisan stok.",
                        description = "Stok kritis akan habis dalam kurun waktu 1 hari.",
                        riskPoint = 95,
                        stockTimeline = listOf(),
                        createdAt = "2026-05-21T16:22:29.000000Z",
                        updatedAt = "2026-05-21T16:22:29.000000Z",
                        product = null,
                        aiRecommendationActions = listOf(),
                        seasonalRecommendation = com.nawaf.kasirpas.model.SeasonalRecommendation(
                            id = 1,
                            aiRecommendationId = 45,
                            min = 40,
                            max = 60,
                            label = "Restok musiman 40 - 60 item untuk Hari Raya.",
                            holiday = "Hari Raya",
                            reason = "Hari Raya meningkatkan penjualan minuman manis.",
                            createdAt = "2026-05-21T16:22:29.000000Z",
                            updatedAt = "2026-05-21T16:22:29.000000Z"
                        )
                    ),
                    AiRecommendation(
                        id = 46,
                        aiRunId = 12,
                        productId = 9,
                        productName = "Minyak Goreng Bimoli 2L",
                        productPrice = "34000.00",
                        currentStock = 45,
                        avgDailySales = "1.20",
                        recommendRestockQty = 0,
                        restockMin = 0,
                        restockMax = 0,
                        restockLabel = "Stok Aman",
                        targetDaysCoverage = null,
                        riskLevel = "NORMAL",
                        urgencyDescription = "Stok aman dan melimpah.",
                        daysUntilEmpty = null,
                        estimatedEmptyDate = null,
                        risk = "Normal",
                        description = "Stok aman dan melimpah.",
                        riskPoint = 10,
                        stockTimeline = listOf(),
                        createdAt = "2026-05-21T16:22:29.000000Z",
                        updatedAt = "2026-05-21T16:22:29.000000Z",
                        product = null,
                        aiRecommendationActions = listOf(),
                        seasonalRecommendation = null
                    )
                )
            ),
            onBackClick = {},
            onRefresh = {},
            onActionClick = { _, _, _ -> },
            onUpgradeClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RestockDialogWithSeasonalPreview() {
    KasirAppTheme {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            RestockDialog(
                recommendation = AiRecommendation(
                    id = 45,
                    aiRunId = 12,
                    productId = 8,
                    productName = "Kopi Susu Gula Aren",
                    productPrice = "18000.00",
                    currentStock = 5,
                    avgDailySales = "12.50",
                    recommendRestockQty = 50,
                    restockMin = 20,
                    restockMax = 50,
                    restockLabel = "High Restock Needed",
                    targetDaysCoverage = 14,
                    riskLevel = "CRITICAL",
                    urgencyDescription = "Stok kritis akan habis dalam kurun waktu 1 hari.",
                    daysUntilEmpty = 1,
                    estimatedEmptyDate = "2026-05-22",
                    risk = "Kehilangan potensi omset harian akibat kehabisan stok.",
                    description = "Stok kritis akan habis dalam kurun waktu 1 hari.",
                    riskPoint = 95,
                    stockTimeline = listOf(),
                    createdAt = "2026-05-21T16:22:29.000000Z",
                    updatedAt = "2026-05-21T16:22:29.000000Z",
                    product = null,
                    aiRecommendationActions = listOf(),
                    seasonalRecommendation = com.nawaf.kasirpas.model.SeasonalRecommendation(
                        id = 1,
                        aiRecommendationId = 45,
                        min = 40,
                        max = 60,
                        label = "Restok musiman 40 - 60 item untuk Hari Raya.",
                        holiday = "Hari Raya",
                        reason = "Hari Raya meningkatkan penjualan minuman manis.",
                        createdAt = "2026-05-21T16:22:29.000000Z",
                        updatedAt = "2026-05-21T16:22:29.000000Z"
                    )
                ),
                onDismiss = {},
                onConfirm = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RestockDialogWithoutSeasonalPreview() {
    KasirAppTheme {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            RestockDialog(
                recommendation = AiRecommendation(
                    id = 46,
                    aiRunId = 12,
                    productId = 9,
                    productName = "Minyak Goreng Bimoli 2L",
                    productPrice = "34000.00",
                    currentStock = 45,
                    avgDailySales = "1.20",
                    recommendRestockQty = 20,
                    restockMin = 10,
                    restockMax = 30,
                    restockLabel = "Stok Aman",
                    targetDaysCoverage = null,
                    riskLevel = "NORMAL",
                    urgencyDescription = "Stok aman.",
                    daysUntilEmpty = null,
                    estimatedEmptyDate = null,
                    risk = "Normal",
                    description = "Stok aman.",
                    riskPoint = 10,
                    stockTimeline = listOf(),
                    createdAt = "2026-05-21T16:22:29.000000Z",
                    updatedAt = "2026-05-21T16:22:29.000000Z",
                    product = null,
                    aiRecommendationActions = listOf(),
                    seasonalRecommendation = null
                ),
                onDismiss = {},
                onConfirm = {}
            )
        }
    }
}