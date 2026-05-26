package com.nawaf.kasirpas.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nawaf.kasirpas.api.RetrofitClient
import com.nawaf.kasirpas.response.TransactionHistory
import com.nawaf.kasirpas.utils.PreferenceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

// Premium Color Palette
val SurfaceColor = Color(0xFFFBFBFB) // Cleaner white-ish
val OnSurfaceColor = Color(0xFF1D1B20)
val PrimaryColor = Color(0xFF6750A4)
val PrimaryContainerColor = Color(0xFFEADDFF)
val OnPrimaryContainerColor = Color(0xFF21005D)
val SurfaceContainerLow = Color(0xFFF3F3F7) // More "sunken" feel
val SurfaceContainerHigh = Color(0xFFECE6F0)
val OutlineVariantColor = Color(0xFFE0E0E0) // Softer divider
val TertiaryColor = Color(0xFF006874)
val TertiaryFixedColor = Color(0xFF97F0FF)
val OutlineColor = Color(0xFF79747E)
val GreyTextColor = Color(0xFF939094)

class HistoryActivity : ComponentActivity() {

    private lateinit var prefManager: PreferenceManager

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
            HistoryScreen(
                onBack = { finish() },
                prefManager = prefManager
            )
        }
    }
}

@Composable
fun HistoryScreen(onBack: () -> Unit, prefManager: PreferenceManager) {
    var searchQuery by remember { mutableStateOf("") }
    var currentPeriod by remember { mutableStateOf("semua") }
    var transactions by remember { mutableStateOf(listOf<TransactionHistory>()) }
    var currentPage by remember { mutableStateOf(1) }
    var lastPage by remember { mutableStateOf(1) }
    var nextPageUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var lastLoadedPage by remember { mutableStateOf(0) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    fun fetchHistory(pageToLoad: Int = 1, isRefresh: Boolean = false) {
        val token = prefManager.getToken() ?: return
        if (isLoading) return

        // Prevent loading the same page multiple times unless refreshing
        if (!isRefresh && pageToLoad <= lastLoadedPage) return

        isLoading = true
        if (isRefresh) {
            lastLoadedPage = 1
        } else {
            lastLoadedPage = pageToLoad
        }

        scope.launch {
            try {
                val response = RetrofitClient.reportApiService.getSalesHistory(
                    token = "Bearer $token",
                    period = currentPeriod,
                    search = searchQuery.ifEmpty { null },
                    perPage = 10,
                    page = pageToLoad
                )
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    data?.let {
                        if (isRefresh) {
                            transactions = it.transactions
                        } else {
                            // Filter duplicates just in case
                            val existingIds = transactions.map { t -> t.id }.toSet()
                            val newTrx =
                                it.transactions.filter { nt -> !existingIds.contains(nt.id) }
                            transactions = transactions + newTrx
                        }
                        currentPage = it.currentPage
                        lastPage = it.lastPage
                        nextPageUrl = it.nextPageUrl
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(currentPeriod) {
        fetchHistory(1, true)
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            delay(600)
            fetchHistory(1, true)
        } else if (transactions.isNotEmpty() || currentPage > 1) {
            fetchHistory(1, true)
        }
    }

    // Infinite Scroll Logic: Trigger when reaching near the end
    LaunchedEffect(listState) {
        snapshotFlow {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem?.index
        }.collect { lastIndex ->
            val totalItems = listState.layoutInfo.totalItemsCount
            if (lastIndex != null && lastIndex >= totalItems - 5 && !isLoading && currentPage < lastPage) {
                fetchHistory(currentPage + 1, false)
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                tonalElevation = 2.dp,
                shadowElevation = 0.dp // We use a custom soft border instead
            ) {
                Column {
                    Spacer(modifier = Modifier.statusBarsPadding())
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(SurfaceContainerLow, CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = OnSurfaceColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Riwayat Penjualan",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OnSurfaceColor,
                                letterSpacing = (-0.5).sp
                            )
                        }
                        IconButton(onClick = { /* Filter */ }) {
                            Icon(
                                Icons.AutoMirrored.Filled.List,
                                contentDescription = "Filter",
                                tint = OnSurfaceColor
                            )
                        }
                    }
                    HorizontalDivider(
                        color = OutlineVariantColor.copy(alpha = 0.5f),
                        thickness = 1.dp
                    )
                }
            }
        },
        containerColor = SurfaceColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    top = 20.dp,
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 40.dp
                ),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Minimalist Search Bar Item
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainerLow)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = OutlineColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(fontSize = 15.sp, color = OnSurfaceColor),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Cari transaksi atau tanggal...",
                                            fontSize = 15.sp,
                                            color = OutlineColor.copy(alpha = 0.7f)
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                }

                // Smooth Filter Chips
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PremiumFilterChip(
                            text = "Semua",
                            isSelected = currentPeriod == "semua",
                            onClick = { currentPeriod = "semua" }
                        )
                        PremiumFilterChip(
                            text = "Hari Ini",
                            isSelected = currentPeriod == "hari_ini",
                            onClick = { currentPeriod = "hari_ini" }
                        )
                    }
                }

                // Empty State
                if (!isLoading && transactions.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 80.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = OutlineColor.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Belum ada transaksi ditemukan",
                                color = OutlineColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Transactions List
                items(transactions, key = { it.id }) { transaction ->
                    PremiumTransactionCard(transaction)
                }

                // Loading indicator
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = PrimaryColor,
                                strokeWidth = 3.dp
                            )
                        }
                    }
                }

                // Pagination
                item {
                    PaginationSection(
                        currentPage = currentPage,
                        lastPage = lastPage,
                        canLoadMore = currentPage < lastPage,
                        onLoadMore = { fetchHistory(currentPage + 1, false) }
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryColor else SurfaceContainerLow,
        animationSpec = tween(durationMillis = 300)
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else OnSurfaceColor.copy(alpha = 0.7f),
        animationSpec = tween(durationMillis = 300)
    )

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = bgColor
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}

@Composable
fun PremiumTransactionCard(transaction: TransactionHistory) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    // Logika penentuan warna berdasarkan tipe transaksi
    val isSale = transaction.trxType.equals("SALE", ignoreCase = true)
    val accentColor = if (isSale) TertiaryColor else Color(0xFFF59E0B) // Cyan vs Amber
    val badgeBgColor = if (isSale) TertiaryFixedColor.copy(alpha = 0.5f) else Color(0xFFFFE9D1)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            ),
        color = Color.White,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)) {
            // Garis aksen vertikal dinamis
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(accentColor)
            )

            Column(modifier = Modifier
                .padding(18.dp)
                .weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        // Badge dinamis (SALE / PURCHASE)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeBgColor)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = transaction.trxType.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                    Text(
                        text = formatter.format(transaction.totalAmount).replace("Rp", "Rp ")
                            .replace(",00", ""),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryColor
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = GreyTextColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = transaction.trxDate,
                        fontSize = 12.sp,
                        color = GreyTextColor,
                        fontWeight = FontWeight.Light
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = GreyTextColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = transaction.user.name, fontSize = 12.sp, color = GreyTextColor)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Product list in a light gray container
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerLow.copy(alpha = 0.6f))
                        .padding(12.dp)
                ) {
                    transaction.items.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.quantity}x ${item.product.name}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = OnSurfaceColor.copy(alpha = 0.8f)
                            )
                            Text(
                                text = formatter.format(item.product.price).replace("Rp", "Rp ")
                                    .replace(",00", ""),
                                fontSize = 12.sp,
                                color = OnSurfaceColor.copy(alpha = 0.6f)
                            )
                        }
                        if (index < transaction.items.size - 1) {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaginationSection(
    currentPage: Int,
    lastPage: Int,
    canLoadMore: Boolean,
    onLoadMore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "HALAMAN $currentPage DARI $lastPage",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = OutlineColor.copy(alpha = 0.6f),
            letterSpacing = 1.5.sp
        )
        if (canLoadMore) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onLoadMore,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariantColor),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "Muat Lebih Banyak",
                    color = PrimaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
