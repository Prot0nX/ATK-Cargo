package com.atk.atk_cargo.security

import android.content.ClipData
import android.content.pm.PackageManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atk.atk_cargo.core.domain.AnimationManager
import kotlinx.coroutines.launch
import java.security.MessageDigest
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

private object UIConfig {
    val CornerRadiusLarge = 28.dp
    val CornerRadiusMedium = 16.dp
    val CornerRadiusSmall = 12.dp
    
    // انیمیشن‌های پس‌زمینه
    const val BG_ANIMATION_DURATION_1 = 18000
    const val BG_ANIMATION_DURATION_2 = 24000
    
    // رنگ‌ها به صورت داینامیک از تم سیستم مشتق می‌شوند
    @Composable
    fun getGlassBorderBrush(isDark: Boolean): Brush {
        return Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = if (isDark) 0.15f else 0.40f),
                Color.White.copy(alpha = if (isDark) 0.03f else 0.12f)
            )
        )
    }
}

@Composable
fun SecurityBlockScreen(
    isLoading: Boolean,
    errorType: SecurityErrorType = SecurityErrorType.TAMPERED,
    onRetry: (() -> Unit)? = null
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // ۱. پس‌زمینه داینامیک انیمیشنی با حرکت اوربیتالی ذرات نور
            DynamicPremiumBackground(isDark = isDark)

            if (isLoading) {
                // ۲. لودینگ پیشرفته و تعاملی اسکن امنیتی
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AdvancedOrbitalScanner()
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    val textAlpha: Float = if (AnimationManager.areAnimationsEnabled()) {
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
                        text = "در حال ارزیابی امنیت برنامه...",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.alpha(textAlpha)
                    )
                }
            } else {
                // ۳. کارت خطای امنیتی با افکت گلس‌مورفیسم و ورود انیمیشنی
                val slideAnim = remember { Animatable(80f) }
                val alphaAnim = remember { Animatable(0f) }

                LaunchedEffect(errorType) {
                    launch {
                        slideAnim.animateTo(
                            targetValue = 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                    launch {
                        alphaAnim.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(600, easing = EaseOutCubic)
                        )
                    }
                }

                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                        .offset(y = slideAnim.value.dp)
                        .alpha(alphaAnim.value),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // فضای خالی برای تراز وسط بهتر در صفحات طولانی
                    Spacer(modifier = Modifier.height(20.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(UIConfig.CornerRadiusLarge),
                        border = BorderStroke(1.dp, UIConfig.getGlassBorderBrush(isDark)),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.65f else 0.85f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // آیکون هشدار متحرک با افکت Sonar و چرخش پالس
                            AnimatedWarningIcon()

                            Spacer(modifier = Modifier.height(20.dp))

                            // عنوان خطا
                            Text(
                                text = when (errorType) {
                                    SecurityErrorType.TAMPERED -> "خطای امنیتی ساختار"
                                    SecurityErrorType.LICENSE_NOT_FOUND -> "خطای احراز لایسنس"
                                    SecurityErrorType.LICENSE_INACTIVE -> "غیرفعال بودن لایسنس"
                                    SecurityErrorType.NETWORK_ERROR -> "خطای ارتباط با شبکه"
                                    SecurityErrorType.UNKNOWN_ERROR -> "خطای ناشناخته سامانه"
                                },
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center
                                ),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // پیام جزئیات خطا در پنل گلس‌مورفیسم قرمز
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = if (isDark) 0.15f else 0.25f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(UIConfig.CornerRadiusSmall),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = when (errorType) {
                                            SecurityErrorType.TAMPERED -> "فایل‌های اجرایی برنامه دستکاری شده‌اند!"
                                            SecurityErrorType.LICENSE_NOT_FOUND -> "مجوز استفاده برای این دستگاه ثبت نشده است."
                                            SecurityErrorType.LICENSE_INACTIVE -> "مدت اعتبار لایسنس برنامه به اتمام رسیده است."
                                            SecurityErrorType.NETWORK_ERROR -> "عدم امکان برقراری ارتباط امن با سرور."
                                            SecurityErrorType.UNKNOWN_ERROR -> "سیستم با یک خطای غیرمنتظره روبرو شد."
                                        },
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    Text(
                                        text = when (errorType) {
                                            SecurityErrorType.TAMPERED -> "این موضوع نشان‌دهنده نصب از منابع غیر رسمی یا تغییر در ساختار کدهاست."
                                            SecurityErrorType.LICENSE_NOT_FOUND -> "لطفاً جهت فعال‌سازی و صدور کلید دسترسی جدید اقدام کنید."
                                            SecurityErrorType.LICENSE_INACTIVE -> "برای تمدید و فعال‌سازی مجدد خدمات با مدیر سامانه تماس بگیرید."
                                            SecurityErrorType.NETWORK_ERROR -> "اتصال شبکه خود را بررسی کرده یا دقایقی دیگر مجدداً تلاش نمایید."
                                            SecurityErrorType.UNKNOWN_ERROR -> "در صورت تکرار مشکل، آن را به پشتیبانی فنی گزارش دهید."
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // پنل مراحل رفع مشکل با طراحی متالیک و مینیمال
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                                shape = RoundedCornerShape(UIConfig.CornerRadiusMedium)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = "مراحل پیشنهادی رفع مشکل:",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    val steps = when (errorType) {
                                        SecurityErrorType.TAMPERED -> listOf(
                                            "برنامه فعلی را به طور کامل از روی دستگاه حذف کنید.",
                                            "با واحد پشتیبانی و فروش شرکت تماس بگیرید."
                                        )
                                        SecurityErrorType.LICENSE_NOT_FOUND, SecurityErrorType.LICENSE_INACTIVE -> listOf(
                                            "با واحد پشتیبانی و فروش شرکت تماس بگیرید.",
                                        )
                                        SecurityErrorType.NETWORK_ERROR -> listOf(
                                            "اتصال داده یا وای‌فای دستگاه را بررسی کنید.",
                                            "در صورت فعال بودن فیلترشکن، آن را خاموش کرده و برنامه را مجدداً باز کنید."
                                        )
                                        SecurityErrorType.UNKNOWN_ERROR -> listOf(
                                            "یک‌بار حافظه کش برنامه را پاک کرده و مجدداً تلاش کنید.",
                                            "در صورت عدم رفع مشکل با پشتیبان فنی سامانه هماهنگ شوید."
                                        )
                                    }

                                    steps.forEachIndexed { _, stepText ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 4.dp)
                                                    .size(8.dp)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                                        shape = CircleShape
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
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

                            // بخش اطلاعات توسعه‌دهنده و سایت با طراحی کارت گلس
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.12f else 0.25f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(UIConfig.CornerRadiusSmall),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
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
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                    Text(
                                        text = "بندر امام خمینی (ره)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // دکمه‌ی تلاش مجدد فقط برای خطاهای موقتی (قطعی شبکه)، نه خطاهای تأییدشده مثل TAMPERED یا LICENSE_INACTIVE
                    if (onRetry != null &&
                        (errorType == SecurityErrorType.NETWORK_ERROR || errorType == SecurityErrorType.UNKNOWN_ERROR)
                    ) {
                        RetryButton(onClick = onRetry)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // دکمه مدرن و انیمیشنی خروج از برنامه
                    InteractiveExitButton(
                        onClick = { android.os.Process.killProcess(android.os.Process.myPid()) }
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun VersionExpiredDialog(
    onExit: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // ۱. پس‌زمینه داینامیک روان
            DynamicPremiumBackground(isDark = isDark)

            val scaleAnim = remember { Animatable(0.85f) }
            val alphaAnim = remember { Animatable(0f) }

            LaunchedEffect(Unit) {
                launch {
                    scaleAnim.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
                launch {
                    alphaAnim.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(500)
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .scale(scaleAnim.value)
                    .alpha(alphaAnim.value),
                shape = RoundedCornerShape(UIConfig.CornerRadiusLarge),
                border = BorderStroke(1.dp, UIConfig.getGlassBorderBrush(isDark)),
                color = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.7f else 0.85f),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // انیمیشن آیکون دانلود و آپدیت
                    AnimatedUpdateIcon(isDark = isDark)

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "نیاز به بروزرسانی برنامه",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        ),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = if (isDark) 0.15f else 0.25f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(UIConfig.CornerRadiusMedium)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "این نسخه از برنامه دیگر پشتیبانی نمی‌شود!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "برای دسترسی مجدد به پنل بارها و کارتابل ترخیص کالا، لطفاً آخرین نسخه منتشر شده را دانلود و نصب کنید.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    InteractiveExitButton(
                        onClick = onExit
                    )
                }
            }
        }
    }
}

@Composable
fun getAppSignatureHash(): String {
    val context = LocalContext.current
    return remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
            val signatures = packageInfo.signingInfo?.apkContentsSigners ?: emptyArray()
            if (signatures.isNotEmpty()) {
                val messageDigest = MessageDigest.getInstance("SHA-256")
                val hashBytes = messageDigest.digest(signatures[0].toByteArray())
                hashBytes.joinToString("") { "%02x".format(it) }
            } else {
                "امضا یافت نشد"
            }
        } catch (e: Exception) {
            "خطا در استخراج امضا: ${e.message}"
        }
    }
}

@Composable
fun ShowSignatureHashSection(
    signatureHash: String,
    modifier: Modifier = Modifier
) {
    SignatureHashCard(
        signatureHash = signatureHash,
        modifier = modifier
    )
}

@Composable
fun SignatureHashCard(
    signatureHash: String,
    modifier: Modifier = Modifier
) {
    val clipboard: Clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var showCopiedMessage by remember { mutableStateOf(false) }

    LaunchedEffect(showCopiedMessage) {
        if (showCopiedMessage) {
            kotlinx.coroutines.delay(2000.milliseconds)
            showCopiedMessage = false
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "clickScale"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    scope.launch {
                        val clipData = ClipData.newPlainText("signature_hash", signatureHash)
                        clipboard.setClipEntry(ClipEntry(clipData))
                        showCopiedMessage = true
                    }
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(UIConfig.CornerRadiusMedium)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "کد امنیتی سخت‌افزار (هش امضا)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(UIConfig.CornerRadiusSmall)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = signatureHash,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "کپی کردن",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // انیمیشن پاپ‌آپ و لغزش زیبای تیک کپی شد
            Box(
                modifier = Modifier.height(28.dp),
                contentAlignment = Alignment.Center
            ) {
                if (showCopiedMessage) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981), // سبز جذاب
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "با موفقیت کپی شد",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF10B981)
                        )
                    }
                } else {
                    Text(
                        text = "برای کپی کردن کلیک کنید",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun DynamicPremiumBackground(isDark: Boolean) {
    val t1: Float
    val t2: Float
    if (AnimationManager.areAnimationsEnabled()) {
        val infiniteTransition = rememberInfiniteTransition(label = "bg_flow")

        t1 = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2 * Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(UIConfig.BG_ANIMATION_DURATION_1, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "t1"
        ).value

        t2 = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2 * Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(UIConfig.BG_ANIMATION_DURATION_2, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "t2"
        ).value
    } else {
        t1 = 0f
        t2 = 0f
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // رسم پس‌زمینه رنگ پایه
        drawRect(color = if (isDark) Color(0xFF0A0F1D) else Color(0xFFF1F5F9))
        
        // هاله اوربیتال ۱ (قرمز هشدار)
        val x1 = w * 0.5f + cos(t1) * (w * 0.25f)
        val y1 = h * 0.35f + sin(t1) * (h * 0.12f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    errorColor.copy(alpha = if (isDark) 0.12f else 0.07f),
                    Color.Transparent
                ),
                center = Offset(x1, y1),
                radius = w * 0.85f
            ),
            radius = w * 0.85f,
            center = Offset(x1, y1)
        )
        
        // هاله اوربیتال ۲ (آبی اصلی تم)
        val x2 = w * 0.5f + sin(t2) * (w * 0.3f)
        val y2 = h * 0.65f + cos(t2) * (h * 0.15f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = if (isDark) 0.10f else 0.05f),
                    Color.Transparent
                ),
                center = Offset(x2, y2),
                radius = w * 0.75f
            ),
            radius = w * 0.75f,
            center = Offset(x2, y2)
        )
    }
}

@Composable
private fun AdvancedOrbitalScanner(modifier: Modifier = Modifier) {
    val rotation1: Float
    val rotation2: Float
    val pulseScale: Float
    val scanOffset: Float
    if (AnimationManager.areAnimationsEnabled()) {
        val infiniteTransition = rememberInfiniteTransition(label = "scanner")

        rotation1 = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation1"
        ).value

        rotation2 = infiniteTransition.animateFloat(
            initialValue = 360f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(6000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation2"
        ).value

        pulseScale = infiniteTransition.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        ).value

        scanOffset = infiniteTransition.animateFloat(
            initialValue = -35.dp.value,
            targetValue = 35.dp.value,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scan"
        ).value
    } else {
        rotation1 = 0f
        rotation2 = 0f
        pulseScale = 1f
        scanOffset = 0f
    }
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error

    Box(
        modifier = modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        // حلقه بیرونی خط‌چین متحرک
        Canvas(modifier = Modifier.fillMaxSize().rotate(rotation1)) {
            drawCircle(
                color = primaryColor.copy(alpha = 0.25f),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                )
            )
        }
        
        // حلقه میانی نقطه‌چین معکوس
        Canvas(modifier = Modifier.size(120.dp).rotate(rotation2)) {
            drawCircle(
                color = errorColor.copy(alpha = 0.35f),
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 12f), 0f)
                )
            )
        }
        
        // هاله رادار میانی
        Box(
            modifier = Modifier
                .size(75.dp)
                .scale(pulseScale)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.15f), Color.Transparent)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {}
        
        // آیکون شیلد مرکزی
        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            modifier = Modifier.size(44.dp).scale(pulseScale),
            tint = primaryColor
        )
        
        // خط اسکن لیزری متحرک
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(2.dp)
                .offset(y = scanOffset.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, primaryColor, Color.Transparent)
                    )
                )
        )
    }
}

@Composable
private fun AnimatedWarningIcon() {
    val pulseScale: Float
    val pulseAlpha: Float
    val shakeAngle: Float
    if (AnimationManager.areAnimationsEnabled()) {
        val pulseTransition = rememberInfiniteTransition(label = "pulse_warning")

        pulseScale = pulseTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.45f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = EaseOutCubic),
                repeatMode = RepeatMode.Restart
            ),
            label = "pulseScale"
        ).value
        pulseAlpha = pulseTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = EaseOutCubic),
                repeatMode = RepeatMode.Restart
            ),
            label = "pulseAlpha"
        ).value
        shakeAngle = pulseTransition.animateFloat(
            initialValue = -6f,
            targetValue = 6f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "shakeAngle"
        ).value
    } else {
        pulseScale = 1f
        pulseAlpha = 0f
        shakeAngle = 0f
    }

    val errorColor = MaterialTheme.colorScheme.error

    Box(
        modifier = Modifier.size(90.dp),
        contentAlignment = Alignment.Center
    ) {
        // حلقه پالس بیرونی (موج رادار)
        Box(
            modifier = Modifier
                .size(70.dp)
                .scale(pulseScale)
                .alpha(pulseAlpha)
                .background(
                    color = errorColor.copy(alpha = 0.35f),
                    shape = CircleShape
                )
        )

        // ظرف اصلی آیکون با لرزش ظریف
        Box(
            modifier = Modifier
                .size(70.dp)
                .rotate(shakeAngle)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(errorColor, errorColor.copy(alpha = 0.8f))
                    ),
                    shape = CircleShape
                )
                .shadow(elevation = 6.dp, shape = CircleShape, clip = false),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(34.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun AnimatedUpdateIcon(isDark: Boolean) {
    val arrowOffset: Float
    val iconScale: Float
    if (AnimationManager.areAnimationsEnabled()) {
        val pulseTransition = rememberInfiniteTransition(label = "pulse_update")

        arrowOffset = pulseTransition.animateFloat(
            initialValue = -10f,
            targetValue = 10f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "arrow"
        ).value
        iconScale = pulseTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        ).value
    } else {
        arrowOffset = 0f
        iconScale = 1f
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier.size(90.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .scale(iconScale)
                .background(
                    color = primaryColor.copy(alpha = if (isDark) 0.12f else 0.18f),
                    shape = CircleShape
                )
                .border(2.dp, primaryColor.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .offset(y = arrowOffset.dp / 3),
                tint = primaryColor
            )
        }
    }
}

@Composable
private fun RetryButton(
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "retryButtonScale"
    )
    val primaryColor = MaterialTheme.colorScheme.primary

    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = primaryColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(buttonScale),
        shape = RoundedCornerShape(UIConfig.CornerRadiusMedium)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "تلاش مجدد",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = Color.White
            )
        }
    }
}

@Composable
private fun InteractiveExitButton(
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )
    val errorColor = MaterialTheme.colorScheme.error

    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(buttonScale)
            .background(
                brush = Brush.horizontalGradient(
                    listOf(errorColor, Color(0xFFDC2626)) // گرادینت قرمز جذاب متالیک
                ),
                shape = RoundedCornerShape(UIConfig.CornerRadiusMedium)
            )
            .clip(RoundedCornerShape(UIConfig.CornerRadiusMedium)),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "خروج امن از برنامه",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = Color.White
            )
        }
    }
}
