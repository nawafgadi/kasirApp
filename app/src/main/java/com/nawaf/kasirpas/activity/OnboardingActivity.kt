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
                .size(230.dp, 170.dp)
                .rotate(-12f)
                .offset(y = (-25).dp)
                .background(LuxuryGlass, RoundedCornerShape(24.dp))
                .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.5f)), RoundedCornerShape(24.dp))
                .blur(4.dp)
        )
        // Main Foreground Card
        Surface(
            modifier = Modifier
                .size(250.dp, 190.dp)
                .rotate(6f)
                .shadow(32.dp, RoundedCornerShape(28.dp), spotColor = LuxuryPrimary.copy(alpha = 0.25f)),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(44.dp).background(LuxuryPrimary.copy(0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Storefront, null, tint = LuxuryPrimary, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Box(Modifier.size(90.dp, 10.dp).background(Color.LightGray.copy(0.3f), CircleShape))
                        Spacer(Modifier.height(6.dp))
                        Box(Modifier.size(50.dp, 8.dp).background(Color.LightGray.copy(0.2f), CircleShape))
                    }
                }
                Spacer(Modifier.weight(1f))
                Box(Modifier.fillMaxWidth().height(44.dp).background(LuxuryPrimary.copy(0.05f), RoundedCornerShape(12.dp)))
            }
        }
    }
}

@Composable
fun SlideTwoVisual() {
    Box(contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier
                .offset(x = 110.dp, y = (-90).dp)
                .shadow(16.dp, CircleShape),
            shape = CircleShape,
            color = LuxuryTertiary,
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.5f))
        ) {
            Text(
                "AI Optimized",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
            )
        }

        Row(
            modifier = Modifier.height(200.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            listOf(0.4f, 0.7f, 1f, 0.6f, 0.9f).forEachIndexed { i, heightFraction ->
                val animatedHeight by animateFloatAsState(
                    targetValue = heightFraction,
                    animationSpec = tween(1200, delayMillis = i * 150, easing = FastOutSlowInEasing),
                    label = "chart"
                )
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .fillMaxHeight(animatedHeight)
                        .background(
                            brush = Brush.verticalGradient(listOf(LuxuryPrimary, LuxuryPrimary.copy(0.4f))),
                            shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                        )
                        .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f)), RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
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
                .size(210.dp, 270.dp)
                .rotate(-5f)
                .shadow(40.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(0.12f)),
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.85f),
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.6f))
        ) {
            Column(Modifier.padding(24.dp)) {
                Box(Modifier.size(36.dp).background(Color(0xFF4CAF50).copy(0.12f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Check, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.height(20.dp))
                Box(Modifier.fillMaxWidth().height(2.dp).background(Color.LightGray.copy(0.3f)))
                Spacer(Modifier.height(20.dp))
                repeat(4) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Box(Modifier.size(70.dp, 10.dp).background(Color.LightGray.copy(0.4f), CircleShape))
                        Box(Modifier.size(45.dp, 10.dp).background(Color.LightGray.copy(0.2f), CircleShape))
                    }
                }
                Spacer(Modifier.weight(1f))
                Box(Modifier.fillMaxWidth().height(34.dp).background(Color.Black.copy(0.06f), RoundedCornerShape(10.dp)))
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
