package com.nawaf.kasirpas.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.nawaf.kasirpas.R
import com.nawaf.kasirpas.activity.ui.theme.KasirAppTheme
import com.nawaf.kasirpas.utils.PreferenceManager
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

// World Class Palette
private val LuxuryPrimary = Color(0xFF653DA7)
private val LuxurySurface = Color(0xFFFCF9F8)
private val LuxuryTertiary = Color(0xFF005D68)
private val LuxuryGlass = Color.White.copy(alpha = 0.4f)
private val LuxuryBorder = Color.White.copy(alpha = 0.3f)

class OnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Make it truly immersive
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        val prefManager = PreferenceManager(this)

        setContent {
            KasirAppTheme {
                OnboardingScreen(
                    onFinish = {
                        prefManager.setOnboarded(true)
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, android.R.anim.fade_in, android.R.anim.fade_out)
                        } else {
                            @Suppress("DEPRECATION")
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        }
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val scope = rememberCoroutineScope()
    val onboardingData = listOf(
        OnboardingItem(
            title = "Kendali Bisnis Modern",
            description = "Kelola inventaris dan kasir dalam satu genggaman yang intuitif.",
            icon = Icons.Rounded.Storefront
        ),
        OnboardingItem(
            title = "Kecerdasan Buatan (AI)",
            description = "Biarkan AI kami bekerja memprediksi stok dan jam tersibuk untuk keuntungan maksimal.",
            icon = Icons.Rounded.AutoGraph
        ),
        OnboardingItem(
            title = "Analisis Tanpa Batas",
            description = "Akses laporan performa real-time kapan saja, di mana saja.",
            icon = Icons.AutoMirrored.Rounded.ReceiptLong
        )
    )

    val pagerState = rememberPagerState(pageCount = { onboardingData.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxurySurface)
    ) {
        // 1. Refined Mesh Gradient & Floating Bubbles
        MeshGradientBackground()
        FloatingBubbles()

        // 2. Skip Button with Immersive Padding
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 24.dp, end = 24.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Surface(
                onClick = onFinish,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = CircleShape,
                modifier = Modifier.size(86.dp, 40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Lewati",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuxuryPrimary
                    )
                }
            }
        }

        // 3. Main Content Pager
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                OnboardingPageContent(
                    data = onboardingData[page],
                    pageOffset = pageOffset,
                    index = page
                )
            }

            // 4. Bottom Navigation (Indicator & Button)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LuxuryWormIndicator(
                    count = onboardingData.size,
                    currentPage = pagerState.currentPage
                )

                Spacer(modifier = Modifier.height(44.dp))

                val isLastPage = pagerState.currentPage == onboardingData.size - 1
                
                Button(
                    onClick = {
                        if (isLastPage) onFinish()
                        else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = LuxuryPrimary.copy(alpha = 0.5f)
                        ),
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryPrimary),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (isLastPage) "Mulai Berbisnis" else "Lanjutkan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
                
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}

@Composable
fun OnboardingPageContent(data: OnboardingItem, pageOffset: Float, index: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Visual Mockup with Enhanced Parallax (Faster movement for visual)
        Box(
            modifier = Modifier
                .height(340.dp)
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = pageOffset * 350f // Faster movement for parallax
                    alpha = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                    scaleX = 1f - (pageOffset.absoluteValue * 0.15f)
                    scaleY = 1f - (pageOffset.absoluteValue * 0.15f)
                },
            contentAlignment = Alignment.Center
        ) {
            when (index) {
                0 -> SlideOneVisual()
                1 -> SlideTwoVisual()
                2 -> SlideThreeVisual()
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Text Content with Slower Parallax
        Column(
            modifier = Modifier.graphicsLayer {
                translationX = pageOffset * 100f // Slower movement
            },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = data.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1B),
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = data.description,
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp),
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
fun SlideOneVisual() {
    Box(contentAlignment = Alignment.Center) {
        // Background Glass Card
        Box(
            modifier = Modifier
                .size(240.dp, 180.dp)
                .rotate(-10f)
                .offset(x = (-15).dp, y = (-20).dp)
                .background(LuxuryGlass, RoundedCornerShape(24.dp))
                .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.5f)), RoundedCornerShape(24.dp))
        ) {
            // Faint content inside the background card for depth
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .blur(1.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color.White.copy(alpha = 0.6f), CircleShape)
                    )
                    Spacer(Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(80.dp, 10.dp)
                            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    )
                }
                Spacer(Modifier.height(15.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.3f))
                )
                Spacer(Modifier.height(15.dp))
                Box(
                    modifier = Modifier
                        .size(120.dp, 8.dp)
                        .background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                )
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .size(90.dp, 8.dp)
                        .background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                )
            }
        }
        
        // Main Foreground Card
        Surface(
            modifier = Modifier
                .size(260.dp, 210.dp)
                .rotate(6f)
                .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = LuxuryPrimary.copy(alpha = 0.25f)),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(LuxuryPrimary.copy(0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Storefront,
                                contentDescription = null,
                                tint = LuxuryPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "NawafPOS",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2C2C2C)
                            )
                            Text(
                                text = "Meja 04 • Baru saja",
                                fontSize = 9.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Lunas",
                            color = Color(0xFF2E7D32),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Divider
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 1.dp)

                // Items list
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "2x Kopi Susu Aren", fontSize = 11.sp, color = Color(0xFF5C5C5C))
                        Text(text = "Rp 36.000", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2C2C2C))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "1x Croissant Cokelat", fontSize = 11.sp, color = Color(0xFF5C5C5C))
                        Text(text = "Rp 22.000", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2C2C2C))
                    }
                }

                // Divider
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 1.dp)

                // Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Bayar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C2C2C)
                    )
                    Text(
                        text = "Rp 58.000",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LuxuryPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun SlideTwoVisual() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth().height(260.dp)
    ) {
        // Main Dashboard Card
        Surface(
            modifier = Modifier
                .size(270.dp, 210.dp)
                .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = LuxuryPrimary.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(LuxuryPrimary.copy(0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoGraph,
                                contentDescription = null,
                                tint = LuxuryPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Prediksi Tren AI",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2C2C2C)
                            )
                            Text(
                                text = "Minggu ini • Akurasi 98%",
                                fontSize = 9.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    Text(
                        text = "+42.5%",
                        color = Color(0xFF2E7D32),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Chart Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(95.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // Grid lines (subtle background)
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.LightGray.copy(alpha = 0.15f))
                            )
                        }
                        Spacer(modifier = Modifier.height(1.dp))
                    }

                    // Bar Chart
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val days = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
                        val heightFractions = listOf(0.4f, 0.6f, 0.9f, 0.7f, 1.0f, 0.85f)
                        
                        heightFractions.forEachIndexed { i, heightFraction ->
                            val animatedHeight by animateFloatAsState(
                                targetValue = heightFraction,
                                animationSpec = tween(1000, delayMillis = i * 100, easing = FastOutSlowInEasing),
                                label = "chart"
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(18.dp)
                                        .fillMaxHeight(animatedHeight * 0.8f) // Scale down slightly to fit labels
                                        .background(
                                            brush = Brush.verticalGradient(
                                                listOf(
                                                    LuxuryPrimary,
                                                    LuxuryPrimary.copy(alpha = 0.4f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = days[i],
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                // AI Insight Notification Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LuxuryPrimary.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚡",
                            fontSize = 11.sp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = "Jam sibuk diprediksi pukul 16.00 - 18.00.",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = LuxuryPrimary
                        )
                    }
                }
            }
        }

        // Floating AI Pill (at the top right, overlapping)
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-10).dp, y = (-12).dp)
                .shadow(8.dp, CircleShape),
            shape = CircleShape,
            color = LuxuryTertiary,
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFF00FFCC), CircleShape)
                )
                Text(
                    text = "AI Aktif",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SlideThreeVisual() {
    Box(contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier
                .size(240.dp, 230.dp)
                .rotate(-4f)
                .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(0.15f)),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFFE8F5E9), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Laporan Ringkas",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2C2C2C)
                            )
                            Text(
                                text = "Bulan Mei • Otomatis",
                                fontSize = 9.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Divider
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 1.dp)

                // Sales Metric Display
                Column {
                    Text(
                        text = "Total Pendapatan",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Rp 24.850.000",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1B5E20),
                        letterSpacing = (-0.5).sp
                    )
                }

                // Mini table rows
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Transaksi", fontSize = 11.sp, color = Color(0xFF5C5C5C))
                        Text(text = "582 Order", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2C2C2C))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Produk Terlaris", fontSize = 11.sp, color = Color(0xFF5C5C5C))
                        Text(text = "Kopi Susu Aren", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2C2C2C))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Rata-rata Order", fontSize = 11.sp, color = Color(0xFF5C5C5C))
                        Text(text = "Rp 42.600", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2C2C2C))
                    }
                }

                // Bottom CTA inside card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .background(LuxuryPrimary.copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                            contentDescription = null,
                            tint = LuxuryPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ekspor Laporan (PDF)",
                            color = LuxuryPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MeshGradientBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(listOf(LuxuryPrimary.copy(alpha = 0.05f), Color.Transparent)),
            radius = size.width * 1.2f,
            center = androidx.compose.ui.geometry.Offset(0f, 0f)
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(LuxuryTertiary.copy(alpha = 0.04f), Color.Transparent)),
            radius = size.width * 1.5f,
            center = androidx.compose.ui.geometry.Offset(size.width, size.height)
        )
    }
}

@Composable
fun FloatingBubbles() {
    val infiniteTransition = rememberInfiniteTransition(label = "bubbles")
    
    val bubbleAnim1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Reverse), label = ""
    )
    val bubbleAnim2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Reverse), label = ""
    )

    Canvas(modifier = Modifier.fillMaxSize().blur(120.dp)) {
        drawCircle(
            color = LuxuryPrimary.copy(alpha = 0.04f),
            radius = 350f,
            center = androidx.compose.ui.geometry.Offset(
                x = size.width * 0.15f + (bubbleAnim1 * 120f),
                y = size.height * 0.35f + (bubbleAnim2 * 180f)
            )
        )
        drawCircle(
            color = LuxuryTertiary.copy(alpha = 0.03f),
            radius = 500f,
            center = androidx.compose.ui.geometry.Offset(
                x = size.width * 0.85f - (bubbleAnim2 * 250f),
                y = size.height * 0.65f - (bubbleAnim1 * 150f)
            )
        )
    }
}

@Composable
fun LuxuryWormIndicator(count: Int, currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { i ->
            val isSelected = i == currentPage
            val width by animateDpAsState(
                targetValue = if (isSelected) 34.dp else 10.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = ""
            )
            Box(
                modifier = Modifier
                    .padding(5.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) LuxuryPrimary else LuxuryPrimary.copy(alpha = 0.25f))
                    .size(width = width, height = 10.dp)
            )
        }
    }
}

data class OnboardingItem(val title: String, val description: String, val icon: ImageVector)

@Preview(showBackground = true)
@Composable
fun OnboardingFinalPreview() {
    MaterialTheme {
        OnboardingScreen(onFinish = {})
    }
}
