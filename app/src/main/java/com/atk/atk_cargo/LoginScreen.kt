package com.atk.atk_cargo

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.atk.atk_cargo.api.ApiService
import com.atk.atk_cargo.api.Constants
import com.atk.atk_cargo.api.LoginRequest
import com.atk.atk_cargo.api.SessionResponse
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.ui.theme.BackgroundDark
import com.atk.atk_cargo.ui.theme.BackgroundLight
import com.atk.atk_cargo.ui.theme.BorderDark
import com.atk.atk_cargo.ui.theme.SurfaceDark
import com.atk.atk_cargo.ui.theme.SurfaceLight
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.MessageDigest
import java.util.UUID

@SuppressLint("HardwareIds")
@Composable
fun LoginScreen(
    onLoginChecked: (Boolean, String, String, String) -> Unit,
    updateSessionValidity: (Boolean) -> Unit,
    userPreferencesManager: UserPreferencesManager
) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    
    // State variables
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var isUsernameFocused by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    // Responsive values based on screen size
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val isSmallScreen = screenHeight < 600.dp
    val dynamicPadding = if (isSmallScreen) 16.dp else 24.dp
    val fieldHeight = 56.dp

    val apiService = remember {
        Retrofit.Builder()
            .baseUrl(Constants.getBaseUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // Main container with full screen design
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) BackgroundDark else BackgroundLight)
    ) {
        // Full screen column layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
        ) {
            // Header Section with Background Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(256.dp)
            ) {
                // Background image
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.login_bg),
                    contentDescription = "Login Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    if (isDarkTheme) BackgroundDark.copy(alpha = 0.8f) else BackgroundLight.copy(
                                        alpha = 0.8f
                                    ),
                                    if (isDarkTheme) BackgroundDark else BackgroundLight
                                )
                            )
                        )
                )

                // Header content with statusBarsPadding
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = dynamicPadding, vertical = 16.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon container
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(12.dp)
                            )
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(12.dp),
                                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "ATK Cargo",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = if (isDarkTheme) Color.White else Color(0xFF1F2937)
                    )

                    Text(
                        text = "مدیریت هوشمند فرآیند بارگیری",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                    )
                }
            }

            // Form Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = dynamicPadding)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Form content
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Username field
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "نام کاربری",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF374151)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = if (isUsernameFocused)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        if (isDarkTheme) BorderDark else Color(0xFFE5E7EB),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(
                                    if (isDarkTheme) SurfaceDark else SurfaceLight,
                                    RoundedCornerShape(12.dp)
                                )
                                .shadow(
                                    elevation = 1.dp,
                                    shape = RoundedCornerShape(12.dp),
                                    ambientColor = Color.Black.copy(alpha = 0.05f),
                                    spotColor = Color.Black.copy(alpha = 0.05f)
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon container
                            Box(
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 8.dp)
                                    .background(
                                        if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (isUsernameFocused)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        if (isDarkTheme) Color(0xFF6B7280) else Color(0xFF9CA3AF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            OutlinedTextField(
                                value = username,
                                onValueChange = {
                                    username = it.trim()
                                    if (errorMessage != null) errorMessage = null
                                },
                                placeholder = {
                                    Text(
                                        text = "atk...",
                                        color = if (isDarkTheme) Color(0xFF9DA6B9) else Color(
                                            0xFF9CA3AF
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = fieldHeight)
                                    .onFocusChanged { focusState ->
                                        isUsernameFocused = focusState.isFocused
                                    },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    cursorColor = MaterialTheme.colorScheme.primary,
                                    unfocusedTextColor = if (isDarkTheme) Color.White else Color(
                                        0xFF1F2937
                                    ),
                                    focusedTextColor = if (isDarkTheme) Color.White else Color(
                                        0xFF1F2937
                                    )
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    textAlign = TextAlign.Start,
                                    color = if (isDarkTheme) Color.White else Color(0xFF1F2937)
                                ),
                                interactionSource = remember { MutableInteractionSource() }
                            )
                        }
                    }

                    // Password field
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "رمز عبور",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF374151)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = if (isPasswordFocused)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        if (isDarkTheme) BorderDark else Color(0xFFE5E7EB),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(
                                    if (isDarkTheme) SurfaceDark else SurfaceLight,
                                    RoundedCornerShape(12.dp)
                                )
                                .shadow(
                                    elevation = 1.dp,
                                    shape = RoundedCornerShape(12.dp),
                                    ambientColor = Color.Black.copy(alpha = 0.05f),
                                    spotColor = Color.Black.copy(alpha = 0.05f)
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon container
                            Box(
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 8.dp)
                                    .background(
                                        if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isPasswordFocused)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        if (isDarkTheme) Color(0xFF6B7280) else Color(0xFF9CA3AF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            OutlinedTextField(
                                value = password,
                                onValueChange = {
                                    password = it.filter { char -> char.isDigit() }
                                    if (errorMessage != null) errorMessage = null
                                },
                                placeholder = {
                                    Text(
                                        text = "••••••••",
                                        color = if (isDarkTheme) Color(0xFF9DA6B9) else Color(
                                            0xFF9CA3AF
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = fieldHeight)
                                    .onFocusChanged { focusState ->
                                        isPasswordFocused = focusState.isFocused
                                    },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    cursorColor = MaterialTheme.colorScheme.primary,
                                    unfocusedTextColor = if (isDarkTheme) Color.White else Color(
                                        0xFF1F2937
                                    ),
                                    focusedTextColor = if (isDarkTheme) Color.White else Color(
                                        0xFF1F2937
                                    )
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.NumberPassword,
                                    imeAction = ImeAction.Done
                                ),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    textAlign = TextAlign.Start,
                                    color = if (isDarkTheme) Color.White else Color(0xFF1F2937),
                                    letterSpacing = (0.1).em
                                ),
                                visualTransformation = if (showPassword) VisualTransformation.None
                                else PasswordVisualTransformation(),
                                interactionSource = remember { MutableInteractionSource() }
                            )

                            // Visibility toggle button
                            IconButton(
                                onClick = { showPassword = !showPassword },
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(48.dp)
                            ) {
                                Icon(
                                    if (showPassword) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = if (showPassword) "پنهان کردن رمز" else "نمایش رمز",
                                    tint = if (isDarkTheme) Color(0xFF6B7280) else Color(0xFF9CA3AF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Error message
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.errorContainer,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Login button
                    Button(
                        onClick = {
                            if (username.isNotBlank() && password.isNotBlank() && !isLoading) {
                                coroutineScope.launch {
                                    isLoading = true
                                    errorMessage = null

                                    try {
                                        val hashedPassword = hashPassword(password)
                                        val deviceModel = Build.MODEL ?: "Unknown"
                                        val androidVersion = Build.VERSION.RELEASE ?: "Unknown"
                                        val deviceId = Build.DISPLAY ?: UUID.randomUUID().toString()
                                        val appVersion = try {
                                            context.packageManager.getPackageInfo(
                                                context.packageName,
                                                0
                                            ).versionName ?: "Unknown"
                                        } catch (_: Exception) {
                                            "Unknown"
                                        }

                                        val loginRequest = LoginRequest(
                                            username = username,
                                            password = hashedPassword,
                                            userType = "",
                                            deviceModel = deviceModel,
                                            deviceId = deviceId,
                                            androidVersion = androidVersion,
                                            appVersion = appVersion
                                        )

                                        val response = apiService.checkLogin(loginRequest)

                                        if (response.isSuccessful) {
                                            val responseBody = response.body()
                                            if (responseBody != null && responseBody.success) {
                                                responseBody.sessionToken?.let { sessionToken ->
                                                    launch {
                                                        userPreferencesManager.saveSessionToken(
                                                            sessionToken
                                                        )
                                                    }
                                                }

                                                onLoginChecked(
                                                    true,
                                                    responseBody.message,
                                                    responseBody.userType ?: "",
                                                    username
                                                )
                                                updateSessionValidity(true)
                                            } else {
                                                errorMessage =
                                                    responseBody?.message ?: "خطا در ورود"
                                            }
                                        } else {
                                            // برای کد 409، سعی می‌کنیم پیام سرور را دریافت کنیم
                                            if (response.code() == 409) {
                                                try {
                                                    val errorBody = response.errorBody()?.string()
                                                    val gson = Gson()
                                                    val errorResponse = gson.fromJson(
                                                        errorBody,
                                                        SessionResponse::class.java
                                                    )
                                                    errorMessage = errorResponse?.message
                                                        ?: "شما در حال حاضر از دستگاه دیگری وارد شده‌اید. لطفاً ابتدا از آن دستگاه خارج شوید."
                                                } catch (_: Exception) {
                                                    errorMessage =
                                                        "شما در حال حاضر از دستگاه دیگری وارد شده‌اید. لطفاً ابتدا از دستگاه اولی خارج شوید."
                                                }
                                            } else {
                                                errorMessage = when (response.code()) {
                                                    401 -> "نام کاربری یا رمز عبور اشتباه است"
                                                    403 -> "دسترسی مجاز نیست"
                                                    500 -> "خطای سرور"
                                                    else -> "خطا در اتصال"
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = when (e) {
                                            is java.net.UnknownHostException -> "عدم دسترسی به اینترنت"
                                            is java.net.SocketTimeoutException -> "زمان اتصال به پایان رسید"
                                            else -> "خطا در اتصال"
                                        }
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(12.dp),
                                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            ),
                        enabled = username.isNotBlank() && password.isNotBlank() && !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = if (isDarkTheme) Color(0xFF374151) else Color(
                                0xFFE5E7EB
                            )
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isDarkTheme) "ورود" else "ورود به سامانه",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        Icons.AutoMirrored.Filled.Login,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Footer at bottom
                    Column {
                        val appVersion = remember {
                            try {
                                context.packageManager.getPackageInfo(
                                    context.packageName,
                                    0
                                ).versionName ?: "2.4.0"
                            } catch (_: Exception) {
                                "2.4.0"
                            }
                        }

                        Text(
                            text = "v$appVersion",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = if (isDarkTheme) Color(0xFF6B7280) else Color(0xFF9CA3AF),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Fixed bottom spacing
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

fun hashPassword(password: String): String {
    return MessageDigest.getInstance("SHA-256")
        .digest(password.toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }
}