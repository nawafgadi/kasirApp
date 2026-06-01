package com.nawaf.kasirpas.activity.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import com.nawaf.kasirpas.MainActivity
import com.nawaf.kasirpas.R
import com.nawaf.kasirpas.activity.HistoryActivity
import com.nawaf.kasirpas.activity.ui.theme.KasirAppTheme
import com.nawaf.kasirpas.response.*
import com.nawaf.kasirpas.utils.PreferenceManager
import com.nawaf.kasirpas.viewmodel.ReportViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

class LaporanFragment : Fragment() {

    private val reportViewModel: ReportViewModel by viewModels()
    private lateinit var prefManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        prefManager = PreferenceManager(requireContext())

        return ComposeView(requireContext()).apply {
            setContent {
                KasirAppTheme {
                    LaporanScreen(viewModel = reportViewModel, prefManager = prefManager)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Data loading is optimized to use caching and is initiated inside the Compose LaporanScreen's LaunchedEffect
    }

    fun refreshData() {
        val token = prefManager.getToken() ?: ""
        reportViewModel.loadDashboardReports(token, forceRefresh = true)
    }
}

// Custom LiveData Observer helper to prevent runtime dependencies issues
@Composable
fun <T> LiveData<T>.observeAsStateHelper(initial: T): State<T> {
    val state = remember { mutableStateOf(initial) }
    DisposableEffect(this) {
        val observer = androidx.lifecycle.Observer<T> { value ->
            if (value != null) {
                state.value = value
            }
        }
        observeForever(observer)
        onDispose {
            removeObserver(observer)
        }
    }
    return state
}


@Composable
fun LaporanScreen(viewModel: ReportViewModel, prefManager: PreferenceManager) {
    val reportDataState = viewModel.reportData.observeAsStateHelper(null)
    val isLoadingState = viewModel.isLoading.observeAsStateHelper(false)
    val isUsingMockState = viewModel.isUsingMock.observeAsStateHelper(false)

    val reportData = reportDataState.value
    val isLoading = isLoadingState.value
    val isUsingMock = isUsingMockState.value

    // Load initial data
    LaunchedEffect(Unit) {
        val token = prefManager.getToken() ?: ""
        viewModel.loadDashboardReports(token)
    }

    LaporanContent(
        reportData = reportData,
        isLoading = isLoading,
        isUsingMock = isUsingMock,
        onRefresh = {
            val token = prefManager.getToken() ?: ""
            viewModel.loadDashboardReports(token, forceRefresh = true)
        }
    )
}

@Composable
fun LaporanContent(
    reportData: DashboardReportData?,
    isLoading: Boolean,
    isUsingMock: Boolean,
    onRefresh: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(1) } // Default: Minggu Ini

    val scrollState = rememberLazyListState()

    // Sync SwipeRefreshLayout state from MainActivity to only be enabled when at top
    val context = LocalContext.current
    LaunchedEffect(scrollState.firstVisibleItemIndex, scrollState.firstVisibleItemScrollOffset) {
        val isAtTop = scrollState.firstVisibleItemIndex == 0 && scrollState.firstVisibleItemScrollOffset == 0
        try {
            (context as? MainActivity)?.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefresh)?.isEnabled = isAtTop
        } catch (e: Exception) {
            // Silently fail if not in MainActivity context (e.g. Preview)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Ultra clean dashboard light background
    ) {
        if (isLoading && reportData == null) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFF653DA7)
            )
        } else if (reportData != null) {
            val selectedSummary = when (selectedTab) {
                0 -> reportData.hariIni
                1 -> reportData.mingguIni
                2 -> reportData.bulanIni
                3 -> reportData.tahunIni
                else -> reportData.sepanjangMasa
            }

            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Header Gradient
                item {
                    DashboardHeader(isUsingMock = isUsingMock)
                }

                // Period Selectors
                item {
                    PeriodTabsSelector(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                }

                // Key Metric Cards
                item {
                    MetricsGrid(summary = selectedSummary)
                }

                // Dynamic Interactive Graphical Chart
                item {
                    ChartCardSection(
                        selectedTab = selectedTab,
                        grafikData = reportData.grafikData,
                        summary = selectedSummary
                    )
                }

                // Top Selling Products
                item {
                    TopProductsSection(products = selectedSummary.produkTerlaris)
                }

                // Recent Transactions
                item {
                    RecentTransactionsSection(transactions = selectedSummary.transaksiTerakhir)
                }
            }
        } else {
            // Error handling fallback layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Gagal memuat laporan",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onRefresh,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF653DA7))
                ) {
                    Text("Coba Lagi")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LaporanScreenPreview() {
    KasirAppTheme {
        // Mock data for preview
        val mockSummary = ReportSummary(
            totalPendapatan = 1500000.0,
            pendapatanVsSebelumnya = PendapatanVsSebelumnya(1200000.0, 25.0),
            totalTransaksi = 42,
            rataRataKeranjang = 35714.0,
            trenPenjualan = emptyList(),
            produkTerlaris = listOf(
                ProdukTerlaris("Kopi Kenangan", 20),
                ProdukTerlaris("Roti Bakar", 15)
            ),
            transaksiTerakhir = listOf(
                TransaksiTerakhir(1, "SALE", 50000.0, "2023-10-01 10:00:00")
            )
        )
        
        val mockData = DashboardReportData(
            hariIni = mockSummary,
            mingguIni = mockSummary,
            bulanIni = mockSummary,
            tahunIni = mockSummary,
            sepanjangMasa = mockSummary,
            grafikData = GrafikData(
                mingguIni = GrafikPeriod(listOf("Sen", "Sel"), listOf(100.0, 200.0)),
                bulanIni = GrafikPeriod(listOf("M1", "M2"), listOf(1000.0, 2000.0)),
                tahunIni = GrafikPeriod(listOf("Jan", "Feb"), listOf(10000.0, 20000.0))
            )
        )

        LaporanContent(
            reportData = mockData,
            isLoading = false,
            isUsingMock = true,
            onRefresh = {}
        )
    }
}

@Composable
fun DashboardHeader(isUsingMock: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF653DA7),
                        Color(0xFF8B5CF6)
                    )
                ),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 40.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Analisis Laporan",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Ringkasan performa finansial bisnis Anda",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
                
                // Badge Status
//                Box(
//                    modifier = Modifier
//                        .background(
//                            if (isUsingMock) Color(0xFFFF9800) else Color(0xFF10B981),
//                            shape = RoundedCornerShape(50)
//                        )
//                        .padding(horizontal = 12.dp, vertical = 6.dp)
//                ) {
//                    Text(
//                        text = if (isUsingMock) "Mode Demo" else "Sinkron",
//                        color = Color.White,
//                        fontSize = 10.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
            }
        }
    }
}

@Composable
fun PeriodTabsSelector(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Hari Ini", "Minggu Ini", "Bulan Ini", "Tahun Ini", "Semua")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .offset(y = (-20).dp)
            .shadow(4.dp, shape = RoundedCornerShape(24.dp))
            .background(Color.White, shape = RoundedCornerShape(24.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTab == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) Color(0xFF653DA7) else Color.Transparent)
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = if (isSelected) Color.White else Color(0xFF757575),
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun MetricsGrid(summary: ReportSummary) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Total Pendapatan Card
        MetricItemCard(
            title = "Total Pendapatan",
            value = formatRupiah(summary.totalPendapatan),
            icon = Icons.AutoMirrored.Outlined.TrendingUp,
            iconColor = Color(0xFF10B981),
            badgeContent = summary.pendapatanVsSebelumnya?.let {
                val isUp = it.persentasePerubahan >= 0
                Pair("${if (isUp) "+" else ""}${it.persentasePerubahan}% vs sebelumnya", isUp)
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Total Transaksi Card
            Box(modifier = Modifier.weight(1f)) {
                SmallMetricCard(
                    title = "Total Transaksi",
                    value = "${summary.totalTransaksi} Trx",
                    icon = Icons.Outlined.Receipt,
                    iconColor = Color(0xFF3B82F6)
                )
            }

            // Rata Rata Keranjang Card
            Box(modifier = Modifier.weight(1f)) {
                SmallMetricCard(
                    title = "Rerata Keranjang",
                    value = formatRupiah(summary.rataRataKeranjang),
                    icon = Icons.Outlined.ShoppingBag,
                    iconColor = Color(0xFFF59E0B)
                )
            }
        }
    }
}

@Composable
fun MetricItemCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    badgeContent: Pair<String, Boolean>? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    color = Color(0xFF757575),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    color = Color(0xFF1C1B1B),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                if (badgeContent != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (badgeContent.second) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = "Trend Icon",
                            tint = if (badgeContent.second) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = badgeContent.first,
                            color = if (badgeContent.second) Color(0xFF10B981) else Color(0xFFEF4444),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor.copy(alpha = 0.1f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SmallMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color(0xFF757575),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(iconColor.copy(alpha = 0.1f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = Color(0xFF1C1B1B),
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun ChartCardSection(selectedTab: Int, grafikData: GrafikData, summary: ReportSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tren Grafik Penjualan",
                color = Color(0xFF1C1B1B),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tekan titik grafik untuk rincian data interaktif",
                color = Color(0xFF757575),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            when (selectedTab) {
                0 -> {
                    // Today Comparison layout
                    TodayComparisonWidget(summary)
                }
                1 -> {
                    // Weekly line chart
                    InteractiveCanvasChart(
                        labels = grafikData.mingguIni.labels,
                        values = grafikData.mingguIni.values,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
                2 -> {
                    // Monthly line chart
                    InteractiveCanvasChart(
                        labels = grafikData.bulanIni.labels,
                        values = grafikData.bulanIni.values,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
                3 -> {
                    // Yearly line chart
                    InteractiveCanvasChart(
                        labels = grafikData.tahunIni.labels,
                        values = grafikData.tahunIni.values,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
                else -> {
                    // All time graphic
                    AllTimeProgressWidget(summary)
                }
            }
        }
    }
}

@Composable
fun TodayComparisonWidget(summary: ReportSummary) {
    val current = summary.totalPendapatan
    val previous = summary.pendapatanVsSebelumnya?.nilaiSebelumnya ?: 0.0
    val maxVal = maxOf(current, previous, 1.0)
    
    val currentRatio = (current / maxVal).toFloat()
    val previousRatio = (previous / maxVal).toFloat()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Today bar
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Hari Ini", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(formatRupiah(current), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(currentRatio)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF653DA7), Color(0xFF8B5CF6))
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                )
            }
        }

        // Yesterday bar
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Kemarin", color = Color.Gray, fontSize = 13.sp)
                Text(formatRupiah(previous), color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(previousRatio)
                        .fillMaxHeight()
                        .background(Color(0xFF9CA3AF), shape = RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

@Composable
fun AllTimeProgressWidget(summary: ReportSummary) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color(0xFFF3F4F6), CircleShape)
                .border(8.dp, Color(0xFF653DA7), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${summary.totalTransaksi}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF653DA7)
                )
                Text(
                    text = "Trx",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Total pendapatan akumulatif sepanjang masa:",
            fontSize = 12.sp,
            color = Color.Gray
        )
        Text(
            text = formatRupiah(summary.totalPendapatan),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF10B981)
        )
    }
}

@Composable
fun InteractiveCanvasChart(
    labels: List<String>,
    values: List<Double>,
    modifier: Modifier = Modifier
) {
    if (values.isEmpty()) return

    val maxValue = values.maxOrNull() ?: 1.0
    val minValue = 0.0
    val range = maxValue - minValue

    var selectedIndex by remember(labels, values) { mutableStateOf(-1) }

    val primaryColor = Color(0xFF653DA7)
    val gridColor = Color(0xFFF1F5F9)

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(labels, values) {
                    detectTapGestures(
                        onPress = { offset ->
                            val width = size.width
                            val paddingLeft = 110f // room for Y Labels
                            val paddingRight = 40f
                            val chartWidth = width - paddingLeft - paddingRight
                            val stepX = chartWidth / (values.size - 1).coerceAtLeast(1)
                            
                            val localTouchX = offset.x - paddingLeft
                            val rawIndex = (localTouchX / stepX).roundToInt()
                            val idx = rawIndex.coerceIn(0, values.size - 1)
                            selectedIndex = idx
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height

            val paddingLeft = 110f
            val paddingRight = 40f
            val paddingTop = 40f
            val paddingBottom = 60f

            val chartWidth = width - paddingLeft - paddingRight
            val chartHeight = height - paddingTop - paddingBottom

            // Draw 4 Horizontal Grid Lines and Y labels
            val gridLinesCount = 3
            for (i in 0..gridLinesCount) {
                val ratio = i.toFloat() / gridLinesCount
                val y = paddingTop + chartHeight * (1 - ratio)

                drawLine(
                    color = gridColor,
                    start = Offset(paddingLeft, y),
                    end = Offset(width - paddingRight, y),
                    strokeWidth = 2f
                )

                val labelVal = minValue + range * ratio
                drawContext.canvas.nativeCanvas.drawText(
                    formatCompactCurrency(labelVal),
                    10f,
                    y + 10f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#94A3B8")
                        textSize = 28f
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                )
            }

            val stepX = chartWidth / (values.size - 1).coerceAtLeast(1)

            // Generate Path points
            val points = values.mapIndexed { idx, valItem ->
                val x = paddingLeft + idx * stepX
                val yRatio = if (range != 0.0) ((valItem - minValue) / range).toFloat() else 0.5f
                val y = paddingTop + chartHeight * (1 - yRatio)
                Offset(x, y)
            }

            // Draw X coordinates labels
            for (i in values.indices) {
                val x = paddingLeft + i * stepX
                drawContext.canvas.nativeCanvas.drawText(
                    labels.getOrElse(i) { "" },
                    x,
                    height - 10f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#64748B")
                        textSize = 26f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }

            // Smooth Bezier Curve Path Drawing
            if (points.isNotEmpty()) {
                val strokePath = Path()
                val fillPath = Path()

                strokePath.moveTo(points[0].x, points[0].y)
                fillPath.moveTo(points[0].x, points[0].y)

                for (i in 1 until points.size) {
                    val pPrev = points[i - 1]
                    val pCurr = points[i]
                    
                    val controlX1 = pPrev.x + (stepX / 2f)
                    val controlY1 = pPrev.y
                    val controlX2 = pCurr.x - (stepX / 2f)
                    val controlY2 = pCurr.y

                    strokePath.cubicTo(controlX1, controlY1, controlX2, controlY2, pCurr.x, pCurr.y)
                    fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, pCurr.x, pCurr.y)
                }

                fillPath.lineTo(points.last().x, paddingTop + chartHeight)
                fillPath.lineTo(points[0].x, paddingTop + chartHeight)
                fillPath.close()

                // Area Gradient Filling
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.35f),
                            primaryColor.copy(alpha = 0.0f)
                        ),
                        startY = paddingTop,
                        endY = paddingTop + chartHeight
                    )
                )

                // Thick Curve Line Stroke Drawing
                drawPath(
                    path = strokePath,
                    color = primaryColor,
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )
            }

            // Render interactive tap highlight nodes
            if (selectedIndex in values.indices) {
                val pt = points[selectedIndex]
                
                // Vertical anchor dash line
                drawLine(
                    color = primaryColor.copy(alpha = 0.4f),
                    start = Offset(pt.x, paddingTop),
                    end = Offset(pt.x, paddingTop + chartHeight),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // White outer indicator ring
                drawCircle(
                    color = Color.White,
                    radius = 16f,
                    center = pt
                )
                // Solid colored inner node dot
                drawCircle(
                    color = primaryColor,
                    radius = 10f,
                    center = pt
                )
            }
        }

        // Animated popover Tooltip UI overlay
        if (selectedIndex in values.indices) {
            val touchedVal = values[selectedIndex]
            val touchedLabel = labels.getOrElse(selectedIndex) { "" }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-10).dp)
                    .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = touchedLabel,
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatRupiah(touchedVal),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun TopProductsSection(products: List<ProdukTerlaris>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Produk Terlaris",
                color = Color(0xFF1C1B1B),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Berdasarkan volume penjualan kuantitas produk",
                color = Color(0xFF757575),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            val maxQty = products.maxOfOrNull { it.totalQuantity } ?: 1

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                products.forEachIndexed { index, product ->
                    val ratio = (product.totalQuantity.toFloat() / maxQty).coerceIn(0.05f, 1f)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Ranking index circle
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    when (index) {
                                        0 -> Color(0xFFFFD700) // Gold
                                        1 -> Color(0xFFC0C0C0) // Silver
                                        2 -> Color(0xFFCD7F32) // Bronze
                                        else -> Color(0xFFE2E8F0)
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (index < 3) Color(0xFF1E293B) else Color(0xFF64748B)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))

                        // Product details and progress bar
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = product.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1C1B1B)
                                )
                                Text(
                                    text = "${product.totalQuantity} terjual",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF653DA7)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Custom beautifully rounded progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(ratio)
                                        .fillMaxHeight()
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(Color(0xFF653DA7), Color(0xFF8B5CF6))
                                            ),
                                            shape = RoundedCornerShape(4.dp)
                                        )
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
fun RecentTransactionsSection(transactions: List<TransaksiTerakhir>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Transaksi Terakhir",
                color = Color(0xFF1C1B1B),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Daftar invoice penjualan teratas yang sukses diproses",
                color = Color(0xFF757575),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                transactions.take(5).forEach { trx ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFECFDF5), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = trx.trxType,
                                    color = Color(0xFF059669),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Invoice #${trx.id}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1C1B1B)
                                )
                                Text(
                                    text = trx.trxDate,
                                    color = Color(0xFF64748B),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        
                        Text(
                            text = formatRupiah(trx.totalAmount),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color(0xFF1C1B1B)
                        )
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(top = 8.dp))
                }
            }

            if (transactions.isNotEmpty()) {
                val context = LocalContext.current
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = {
                        context.startActivity(Intent(context, HistoryActivity::class.java))
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        "Lihat Semua Transaksi",
                        color = Color(0xFF653DA7),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Helpers functions
fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount).replace("Rp", "Rp ")
}

fun formatCompactCurrency(value: Double): String {
    return when {
        value >= 1_000_000_000 -> String.format(Locale.US, "%.1fM", value / 1_000_000_000.0) // Milyar
        value >= 1_000_000 -> String.format(Locale.US, "%.1fjt", value / 1_000_000.0) // Juta
        value >= 1_000 -> String.format(Locale.US, "%.1fk", value / 1_000.0) // Ribu
        else -> String.format(Locale.US, "%.0f", value)
    }
}