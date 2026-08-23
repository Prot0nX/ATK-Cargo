package com.atk.atk_cargo.security

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.GppBad
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atk.atk_cargo.core.domain.AnimationManager
import kotlinx.coroutines.delay

@Composable
fun SecurityBlockScreen(
    isLoading: Boolean,
    errorType: SecurityErrorType,
    onExit: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val primaryColor = MaterialTheme.colorScheme.primary

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
 // پس‌زمینه زنده صنعتی با گرادیان ملایم، هم‌راستا با HomeScreen
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = if (isDark) 0.12f else 0.06f),
                            primaryColor.copy(alpha = 0f)
                        ),
                        center = Offset(width * 0.85f, height * 0.08f),
                        radius = width * 0.75f
                    ),
                    center = Offset(width * 0.85f, height * 0.08f),
                    radius = width * 0.75f
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = if (isDark) 0.08f else 0.04f),
                            primaryColor.copy(alpha = 0f)
                        ),
                        center = Offset(width * 0.15f, height * 0.85f),
                        radius = width * 0.75f
                    ),
                    center = Offset(width * 0.15f, height * 0.85f),
                    radius = width * 0.75f
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    LoadingContent()
                } else {
                    ErrorContent(
                        errorType = errorType,
                        isDark = isDark,
                        onExit = onExit,
                        onRetry = onRetry
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AdvancedOrbitalScanner()
        Spacer(modifier = Modifier.height(24.dp))

        var stage by remember { mutableIntStateOf(0) }
        LaunchedEffect(Unit) {
            delay(4000)
            stage = 1
            delay(5000)
            stage = 2
        }

        val textAlpha: Float = if (AnimationManager.areAnimationsEnabled) {
            val infiniteTransition = rememberInfiniteTransition(label = "loading_text")
            infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = EaseInOutCubic),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "textAlpha"
            ).value
        } else {
            1f
        }

        Text(
            text = when (stage) {
                0 -> "در حال ارزیابی امنیت برنامه..."
                1 -> "در حال بررسی مجوز دسترسی..."
                else -> "ارتباط با سرور کند است، لطفاً صبر کنید..."
            },
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.alpha(textAlpha)
        )
    }
}

@Composable
private fun ErrorContent(
    errorType: SecurityErrorType,
    isDark: Boolean,
    onExit: () -> Unit,
    onRetry: (() -> Unit)?
) {
    val isDeviceOnline = rememberIsDeviceOnline()
    val presentation = errorType.toPresentation(isDeviceOnline)
    val scrollState = rememberScrollState()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400)) + slideInVertically(
            animationSpec = tween(400),
            initialOffsetY = { it / 6 }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            val containerColor = if (isDark) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                MaterialTheme.colorScheme.surface
            }
            val borderColor = if (isDark) {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .semantics { contentDescription = "${presentation.title}: ${presentation.headline}" },
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, borderColor),
                colors = CardDefaults.cardColors(containerColor = containerColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
 // تایل آیکون شدت خطا
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(presentation.accentColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = presentation.icon,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = presentation.accentColor
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = presentation.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        ),
                        color = presentation.accentColor,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

 // بلوک پیام جزئیات خطا
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(presentation.accentColor.copy(alpha = if (isDark) 0.10f else 0.07f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = presentation.headline,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center,
                                color = presentation.accentColor
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = presentation.detail,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

 // پنل مراحل رفع مشکل
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "مراحل پیشنهادی رفع مشکل",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            presentation.steps.forEachIndexed { index, stepText ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(presentation.accentColor.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (index + 1).toString(),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = presentation.accentColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = stepText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

 // بخش اطلاعات شرکت
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.10f else 0.06f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "سامانه ترخیص بار",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "شرکت امین تجار خوزستان",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "بندر امام خمینی (ره)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (onRetry != null && presentation.canRetry) {
                RetryButton(onClick = onRetry)
                Spacer(modifier = Modifier.height(12.dp))
            }

            InteractiveExitButton(onClick = onExit)

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

private data class SecurityErrorPresentation(
    val title: String,
    val headline: String,
    val detail: String,
    val steps: List<String>,
    val icon: ImageVector,
    val accentColor: Color,
    val canRetry: Boolean
)

@Composable
private fun SecurityErrorType.toPresentation(isDeviceOnline: Boolean): SecurityErrorPresentation {
    val errorColor = MaterialTheme.colorScheme.error
    val warningColor = Color(0xFFF59E0B)

    return when (this) {
        SecurityErrorType.TAMPERED -> SecurityErrorPresentation(
            title = "خطای امنیتی ساختار",
            headline = "فایل‌های اجرایی برنامه دستکاری شده‌اند!",
            detail = "این موضوع نشان‌دهنده نصب از منابع غیر رسمی یا تغییر در ساختار کدهاست.",
            steps = listOf(
                "برنامه فعلی را به طور کامل از روی دستگاه حذف کنید.",
                "با واحد پشتیبانی و فروش شرکت تماس بگیرید."
            ),
            icon = Icons.Rounded.GppBad,
            accentColor = errorColor,
            canRetry = false
        )

        SecurityErrorType.ENVIRONMENT_COMPROMISED -> SecurityErrorPresentation(
            title = "محیط اجرای ناامن",
            headline = "ابزار اشکال‌زدایی یا تزریق کد روی دستگاه فعال است.",
            detail = "برای حفاظت از اطلاعات محموله‌ها، اجرای برنامه در این شرایط ممکن نیست.",
            steps = listOf(
                "ابزارهای دیباگ/تزریق (مانند Frida یا Xposed) را غیرفعال کنید.",
                "برنامه را روی یک دستگاه بدون تغییرات سیستمی اجرا کنید.",
                "در صورت ادامه مشکل با پشتیبانی فنی تماس بگیرید."
            ),
            icon = Icons.Rounded.BugReport,
            accentColor = errorColor,
            canRetry = false
        )

        SecurityErrorType.LICENSE_NOT_FOUND -> SecurityErrorPresentation(
            title = "خطای احراز لایسنس",
            headline = "مجوز استفاده برای این دستگاه ثبت نشده است.",
            detail = "لطفاً جهت فعال‌سازی و صدور کلید دسترسی جدید اقدام کنید.",
            steps = listOf(
                "با واحد پشتیبانی و فروش شرکت تماس بگیرید."
            ),
            icon = Icons.Rounded.Key,
            accentColor = Color(0xFF4F46E5),
            canRetry = false
        )

        SecurityErrorType.LICENSE_INACTIVE -> SecurityErrorPresentation(
            title = "غیرفعال بودن لایسنس",
            headline = "مدت اعتبار لایسنس برنامه به اتمام رسیده است.",
            detail = "برای تمدید و فعال‌سازی مجدد خدمات با مدیر سامانه تماس بگیرید.",
            steps = listOf(
                "با واحد پشتیبانی و فروش شرکت تماس بگیرید."
            ),
            icon = Icons.Rounded.EventBusy,
            accentColor = warningColor,
            canRetry = false
        )

        SecurityErrorType.NETWORK_ERROR -> if (isDeviceOnline) {
            SecurityErrorPresentation(
                title = "خطای ارتباط با سرور",
                headline = "ارتباط با سرور برقرار نشد.",
                detail = "دستگاه شما آنلاین است اما سرور در دسترس نیست.",
                steps = listOf(
                    "در صورت فعال بودن فیلترشکن، آن را خاموش کرده و برنامه را مجدداً باز کنید.",
                    "چند دقیقه دیگر مجدداً تلاش کنید."
                ),
                icon = Icons.Rounded.CloudOff,
                accentColor = warningColor,
                canRetry = true
            )
        } else {
            SecurityErrorPresentation(
                title = "خطای ارتباط با شبکه",
                headline = "دستگاه شما به اینترنت متصل نیست.",
                detail = "برای ادامه، اتصال اینترنت دستگاه را برقرار کنید.",
                steps = listOf(
                    "اتصال داده یا وای‌فای دستگاه را بررسی کنید.",
                    "در صورت فعال بودن حالت پرواز، آن را خاموش کنید."
                ),
                icon = Icons.Default.SignalCellularOff,
                accentColor = warningColor,
                canRetry = true
            )
        }

        SecurityErrorType.UNKNOWN_ERROR -> SecurityErrorPresentation(
            title = "خطای ناشناخته سامانه",
            headline = "سیستم با یک خطای غیرمنتظره روبرو شد.",
            detail = "در صورت تکرار مشکل، آن را به پشتیبانی فنی گزارش دهید.",
            steps = listOf(
                "یک‌بار حافظه کش برنامه را پاک کرده و مجدداً تلاش کنید.",
                "در صورت عدم رفع مشکل با پشتیبان فنی سامانه هماهنگ شوید."
            ),
            icon = Icons.Rounded.ErrorOutline,
            accentColor = warningColor,
            canRetry = true
        )
    }
}

@Composable
private fun rememberIsDeviceOnline(): Boolean {
    val context = LocalContext.current
    var isOnline by remember { mutableStateOf(true) }

    DisposableEffect(context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isOnline = true
            }

            override fun onLost(network: Network) {
                isOnline = false
            }
        }

        try {
            val activeNetwork = connectivityManager?.activeNetwork
            val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
            isOnline = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, networkCallback)
        } catch (_: Exception) {
            isOnline = true
        }

        onDispose {
            try {
                connectivityManager?.unregisterNetworkCallback(networkCallback)
            } catch (_: Exception) {
            }
        }
    }

    return isOnline
}
