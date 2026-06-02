package com.nawaf.kasirpas.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.nawaf.kasirpas.MainActivity
import com.nawaf.kasirpas.R
import com.nawaf.kasirpas.activity.ui.theme.KasirAppTheme
import com.nawaf.kasirpas.api.RetrofitClient
import com.nawaf.kasirpas.utils.PreferenceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// High-End Palette
private val LuxuryPrimary = Color(0xFF653DA7)
private val LuxuryAccent = Color(0xFF8E7AB5)
private val LuxurySurface = Color(0xFFFCF9F8)

// Premium Easings for Smooth Animations
private val SmoothEaseInOut = CubicBezierEasing(0.42f, 0.0f, 0.58f, 1.0f)
private val SmoothEaseOut = CubicBezierEasing(0.16f, 1.0f, 0.3f, 1.0f)

class SplashActivity : ComponentActivity() {

    private lateinit var prefManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Immersive Mode
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())

        prefManager = PreferenceManager(this)

        setContent {
            KasirAppTheme {
                SplashScreen(onAnimationFinished = {
                    lifecycleScope.launch {
                        navigateNext()
                    }
                })
            }
        }
    }

    private suspend fun navigateNext() {
        // 1. Pertama: Cek onboarding (first install)
        if (!prefManager.isOnboarded()) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
            return
        }

        // 2. Kedua: Cek apakah user sudah login
        val token = prefManager.getToken()
        if (token == null || !prefManager.isLogin()) {
            navigateToLogin()
            finish()
            return
        }

        // 3. Ketiga: Verifikasi session ke backend (token masih valid?)
        try {
            val response = RetrofitClient.authApi.getSession("Bearer $token")

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.user != null) {
                    prefManager.saveUser(body.user)
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    navigateToLogin()
                }
            } else {
                navigateToLogin()
            }
        } catch (e: Exception) {
            // Jika error koneksi, tetap lanjut ke MainActivity (mode offline)
            startActivity(Intent(this, MainActivity::class.java))
        } finally {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }

    private fun navigateToLogin() {
        prefManager.setLogin(false)
        startActivity(Intent(this, LoginActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    var startAnimations by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnimations = true
    }

    // Logo entrance animations
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 100, easing = SmoothEaseOut),
        label = "logo_alpha"
    )
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0.6f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 100, easing = SmoothEaseOut),
        label = "logo_scale"
    )

    // Wait until logo finished scaling/fading in before starting bobbing
    var isLogoEntranceFinished by remember { mutableStateOf(false) }
    LaunchedEffect(startAnimations) {
        if (startAnimations) {
            delay(1100) // delay (100) + animation duration (1000)
            isLogoEntranceFinished = true
        }
    }

    // Smooth multiplier to fade-in the bobbing animation to prevent jumps
    val floatMultiplier by animateFloatAsState(
        targetValue = if (isLogoEntranceFinished) 1f else 0f,
        animationSpec = tween(1200, easing = SmoothEaseInOut),
        label = "float_multiplier"
    )

    // Floating and breathing animations for the logo
    val infiniteTransition = rememberInfiniteTransition(label = "logo_pulse")
    val logoFloat by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = SmoothEaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    // Brand entrance animations
    val brandAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 400, easing = SmoothEaseOut),
        label = "brand_alpha"
    )
    val brandOffsetY by animateFloatAsState(
        targetValue = if (startAnimations) 0f else 40f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 400, easing = SmoothEaseOut),
        label = "brand_offset"
    )

    // Progress entrance animations
    val progressAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 700, easing = SmoothEaseOut),
        label = "progress_alpha"
    )
    val progressOffsetY by animateFloatAsState(
        targetValue = if (startAnimations) 0f else 30f,
        animationSpec = tween(durationMillis = 800, delayMillis = 700, easing = SmoothEaseOut),
        label = "progress_offset"
    )

    // Smooth loading progress bar simulation
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(startAnimations) {
        if (startAnimations) {
            delay(700) // Start filling when progress bar fades in
            animatedProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1500, easing = SmoothEaseInOut)
            )
            onAnimationFinished()
        }
    }

    // Determine status text based on current progress
    val statusText = when {
        animatedProgress.value < 0.35f -> stringResource(R.string.initializing)
        animatedProgress.value < 0.75f -> "CONNECTING TO SECURE GATEWAY"
        else -> "DECRYPTING SESSION DATA"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxurySurface)
    ) {
        // Luxury Moving Background Orbs
        MeshBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Spacer to balance the layout
            Spacer(modifier = Modifier.height(64.dp))

            // Center Content: Logo and Brand Name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Floating Soft Glow Logo Icon
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .graphicsLayer {
                            alpha = logoAlpha
                            scaleX = logoScale
                            scaleY = logoScale
                            translationY = logoFloat * floatMultiplier
                        }
                        .shadow(24.dp, CircleShape, spotColor = LuxuryPrimary.copy(alpha = 0.5f))
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(LuxuryPrimary, LuxuryPrimary.copy(alpha = 0.8f))
                            ),
                            shape = CircleShape
                        )
                        .border(3.dp, Color.White.copy(alpha = 0.7f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Storefront,
                        contentDescription = "LuxePOS Logo",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Brand Name & Tagline
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer {
                        alpha = brandAlpha
                        translationY = brandOffsetY
                    }
                ) {
                    Text(
                        text = stringResource(R.string.brand_name),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = LuxuryPrimary,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.tagline),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuxuryAccent,
                        letterSpacing = 2.sp
                    )
                }
            }

            // Bottom Content: Progress Bar and Initializing Text
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 64.dp)
                    .graphicsLayer {
                        alpha = progressAlpha
                        translationY = progressOffsetY
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Modern Elegant Progress Bar
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(4.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress.value)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(LuxuryPrimary, LuxuryAccent)
                                ),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = statusText.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    KasirAppTheme {
        SplashScreen(onAnimationFinished = {})
    }
}

// Helper to convert Android Interpolator to Compose Easing
private fun android.view.animation.Interpolator.toEasing(): Easing = Easing { x ->
    getInterpolation(x)
}

@Composable
private fun MeshBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh_bg")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = SmoothEaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(160.dp)
        ) {
            // Floating Soft Orbs in the background
            drawCircle(
                color = LuxuryPrimary.copy(alpha = 0.15f),
                radius = 700f,
                center = center.copy(
                    x = center.x * (0.5f + 0.5f * animOffset),
                    y = center.y * (0.5f - 0.5f * animOffset)
                )
            )
            drawCircle(
                color = LuxuryAccent.copy(alpha = 0.10f),
                radius = 900f,
                center = center.copy(
                    x = center.x * (1.5f - 0.5f * animOffset),
                    y = center.y * (1.5f + 0.5f * animOffset)
                )
            )
        }
    }
}

