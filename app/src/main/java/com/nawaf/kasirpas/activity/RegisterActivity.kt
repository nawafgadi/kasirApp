package com.nawaf.kasirpas.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.nawaf.kasirpas.activity.ui.theme.KasirAppTheme
import com.nawaf.kasirpas.api.RetrofitClient
import com.nawaf.kasirpas.request.RegisterRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val LuxuryPrimary = Color(0xFF653DA7)
private val LuxuryAccent = Color(0xFF8E7AB5)
private val LuxurySurface = Color(0xFFFCF9F8)

// Premium Easings for Smooth Animations
private val SmoothEaseInOut = CubicBezierEasing(0.42f, 0.0f, 0.58f, 1.0f)
private val SmoothEaseOut = CubicBezierEasing(0.16f, 1.0f, 0.3f, 1.0f)

class RegisterActivity : ComponentActivity() {

    private val isLoading = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Immersive Mode
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())

        setContent {
            KasirAppTheme {
                RegisterScreen(
                    isLoading = isLoading.value,
                    onRegister = { name, email, password -> performRegister(name, email, password) },
                    onNavigateToLogin = {
                        startActivity(Intent(this, LoginActivity::class.java))
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish()
                    }
                )
            }
        }
    }

    private fun performRegister(name: String, email: String, password: String) {
        isLoading.value = true
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.authApi.register(RegisterRequest(email, password, name))
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    Toast.makeText(this@RegisterActivity, authResponse?.message ?: "Registration successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = try {
                        org.json.JSONObject(errorBody ?: "").getString("message")
                    } catch (e: Exception) {
                        "Registration failed"
                    }
                    Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading.value = false
            }
        }
    }
}

@Composable
fun RegisterScreen(
    isLoading: Boolean,
    onRegister: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    fun validateName(): Boolean {
        return if (name.isBlank()) {
            nameError = "Nama lengkap tidak boleh kosong"
            false
        } else if (name.length < 2) {
            nameError = "Nama minimal 2 karakter"
            false
        } else {
            nameError = null
            true
        }
    }

    fun validateEmail(): Boolean {
        return if (email.isBlank()) {
            emailError = "Email tidak boleh kosong"
            false
        } else if (!email.contains("@") || !email.contains(".")) {
            emailError = "Format email tidak valid"
            false
        } else {
            emailError = null
            true
        }
    }

    fun validatePassword(): Boolean {
        return if (password.isBlank()) {
            passwordError = "Password tidak boleh kosong"
            false
        } else if (password.length < 6) {
            passwordError = "Password minimal 6 karakter"
            false
        } else {
            passwordError = null
            true
        }
    }

    fun validateAll(): Boolean {
        val n = validateName()
        val e = validateEmail()
        val p = validatePassword()
        return n && e && p
    }

    var startAnimations by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnimations = true
    }

    // Entrance Animations
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 100, easing = SmoothEaseOut),
        label = "logo_alpha"
    )
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0.7f,
        animationSpec = tween(durationMillis = 800, delayMillis = 100, easing = SmoothEaseOut),
        label = "logo_scale"
    )

    var isLogoEntranceFinished by remember { mutableStateOf(false) }
    LaunchedEffect(startAnimations) {
        if (startAnimations) {
            delay(900) // delay (100) + duration (800)
            isLogoEntranceFinished = true
        }
    }

    val floatMultiplier by animateFloatAsState(
        targetValue = if (isLogoEntranceFinished) 1f else 0f,
        animationSpec = tween(1200, easing = SmoothEaseInOut),
        label = "float_multiplier"
    )

    val cardAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0f,
        animationSpec = tween(durationMillis = 900, delayMillis = 300, easing = SmoothEaseOut),
        label = "card_alpha"
    )
    val cardOffsetY by animateFloatAsState(
        targetValue = if (startAnimations) 0f else 60f,
        animationSpec = tween(durationMillis = 900, delayMillis = 300, easing = SmoothEaseOut),
        label = "card_offset"
    )

    val footerAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 0.5f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 600, easing = SmoothEaseOut),
        label = "footer_alpha"
    )
    val footerOffsetY by animateFloatAsState(
        targetValue = if (startAnimations) 0f else 20f,
        animationSpec = tween(durationMillis = 800, delayMillis = 600, easing = SmoothEaseOut),
        label = "footer_offset"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        MeshBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LogoHeader(
                floatMultiplier = floatMultiplier,
                scaleVal = logoScale,
                alphaVal = logoAlpha
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Glassmorphism Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = cardAlpha
                        translationY = cardOffsetY
                    }
                    .shadow(40.dp, RoundedCornerShape(24.dp), spotColor = LuxuryPrimary.copy(alpha = 0.15f))
                    .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(24.dp))
                    .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.5f)), RoundedCornerShape(24.dp))
                    .padding(28.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Create Account",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1C1B1B),
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = "Mulai kelola bisnis Anda sekarang",
                        fontSize = 15.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
                    )

                    // Input Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = null
                        },
                        label = { Text("Nama Lengkap") },
                        placeholder = { Text("John Doe") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.Rounded.Person, null, tint = LuxuryPrimary) },
                        singleLine = true,
                        enabled = !isLoading,
                        isError = nameError != null,
                        supportingText = nameError?.let { { Text(it, color = Color(0xFFB00020)) } },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryPrimary,
                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.4f),
                            focusedTextColor = Color(0xFF1C1B1B),
                            unfocusedTextColor = Color(0xFF1C1B1B),
                            cursorColor = LuxuryPrimary,
                            errorBorderColor = Color(0xFFB00020)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        label = { Text("Email") },
                        placeholder = { Text("email@bisnis.com") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.Rounded.Mail, null, tint = LuxuryPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        enabled = !isLoading,
                        isError = emailError != null,
                        supportingText = emailError?.let { { Text(it, color = Color(0xFFB00020)) } },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryPrimary,
                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.4f),
                            focusedTextColor = Color(0xFF1C1B1B),
                            unfocusedTextColor = Color(0xFF1C1B1B),
                            cursorColor = LuxuryPrimary,
                            errorBorderColor = Color(0xFFB00020)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                        },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.Rounded.Lock, null, tint = LuxuryPrimary) },
                        trailingIcon = {
                            val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(icon, null)
                            }
                        },
                        enabled = !isLoading,
                        isError = passwordError != null,
                        supportingText = passwordError?.let { { Text(it, color = Color(0xFFB00020)) } },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryPrimary,
                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.4f),
                            focusedTextColor = Color(0xFF1C1B1B),
                            unfocusedTextColor = Color(0xFF1C1B1B),
                            cursorColor = LuxuryPrimary,
                            errorBorderColor = Color(0xFFB00020)
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Gradient Register Button
                    Button(
                        onClick = {
                            if (validateAll()) {
                                onRegister(name, email, password)
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .shadow(20.dp, RoundedCornerShape(18.dp), spotColor = LuxuryPrimary),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(listOf(LuxuryPrimary, LuxuryAccent)),
                                    shape = RoundedCornerShape(18.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        "Mohon Tunggu...",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            } else {
                                Text("Daftar Sekarang", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Sudah punya akun? ", color = Color.Gray, fontSize = 14.sp)
                        Text(
                            text = "Masuk",
                            color = LuxuryPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { if (!isLoading) onNavigateToLogin() }
                        )
                    }
                }
            }

            // Security Footer
            Spacer(modifier = Modifier.height(48.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.graphicsLayer {
                    alpha = footerAlpha
                    translationY = footerOffsetY
                }
            ) {
                Icon(Icons.Rounded.Security, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SECURE & ENCRYPTED",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun MeshBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(10000, easing = SmoothEaseInOut), RepeatMode.Reverse),
        label = "offset"
    )

    Box(modifier = Modifier.fillMaxSize().background(LuxurySurface)) {
        Canvas(modifier = Modifier.fillMaxSize().blur(150.dp)) {
            // Floating Soft Orbs
            drawCircle(
                color = LuxuryPrimary.copy(alpha = 0.12f),
                radius = 800f,
                center = center.copy(x = center.x * animOffset, y = center.y * (1 - animOffset))
            )
            drawCircle(
                color = LuxuryAccent.copy(alpha = 0.08f),
                radius = 1000f,
                center = center.copy(x = center.x * (1 - animOffset), y = center.y * animOffset)
            )
        }
    }
}

@Composable
private fun LogoHeader(
    floatMultiplier: Float,
    scaleVal: Float,
    alphaVal: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(2200, easing = SmoothEaseInOut), RepeatMode.Reverse),
        label = "float"
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .graphicsLayer {
                alpha = alphaVal
                scaleX = scaleVal
                scaleY = scaleVal
                translationY = floatAnim * floatMultiplier
            }
            .shadow(16.dp, CircleShape, spotColor = LuxuryPrimary)
            .background(LuxuryPrimary, CircleShape)
            .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Rounded.Storefront, null, tint = Color.White, modifier = Modifier.size(32.dp))
    }
}

@Preview(name = "Register - Normal", showBackground = true)
@Composable
fun RegisterPreview() {
    KasirAppTheme {
        RegisterScreen(isLoading = false, onRegister = { _, _, _ -> }, onNavigateToLogin = {})
    }
}

@Preview(name = "Register - Loading", showBackground = true)
@Composable
fun RegisterLoadingPreview() {
    KasirAppTheme {
        RegisterScreen(isLoading = true, onRegister = { _, _, _ -> }, onNavigateToLogin = {})
    }
}
