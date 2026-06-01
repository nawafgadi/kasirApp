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
import kotlinx.coroutines.launch

private val LuxuryPrimary = Color(0xFF653DA7)
private val LuxuryAccent = Color(0xFF8E7AB5)
private val LuxurySurface = Color(0xFFFCF9F8)

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

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

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
            LogoHeader()

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(1000)) + slideInVertically(tween(1000)) { it / 2 }
            ) {
                // Glassmorphism Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
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
                            onValueChange = { name = it },
                            label = { Text("Nama Lengkap") },
                            placeholder = { Text("John Doe") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = { Icon(Icons.Rounded.Person, null, tint = LuxuryPrimary) },
                            singleLine = true,
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuxuryPrimary,
                                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.4f),
                                focusedTextColor = Color(0xFF1C1B1B),
                                unfocusedTextColor = Color(0xFF1C1B1B),
                                cursorColor = LuxuryPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Input Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            placeholder = { Text("email@bisnis.com") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = { Icon(Icons.Rounded.Mail, null, tint = LuxuryPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuxuryPrimary,
                                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.4f),
                                focusedTextColor = Color(0xFF1C1B1B),
                                unfocusedTextColor = Color(0xFF1C1B1B),
                                cursorColor = LuxuryPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Input Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
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
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuxuryPrimary,
                                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.4f),
                                focusedTextColor = Color(0xFF1C1B1B),
                                unfocusedTextColor = Color(0xFF1C1B1B),
                                cursorColor = LuxuryPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Gradient Register Button
                        Button(
                            onClick = { onRegister(name, email, password) },
                            enabled = !isLoading && name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty(),
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
            }

            // Security Footer
            Spacer(modifier = Modifier.height(48.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(0.5f)
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
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Reverse),
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
private fun LogoHeader() {
    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float"
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .graphicsLayer { translationY = floatAnim }
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
