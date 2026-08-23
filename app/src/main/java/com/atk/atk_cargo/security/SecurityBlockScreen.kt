package com.atk.atk_cargo.security

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atk.atk_cargo.core.domain.AnimationManager
import kotlinx.coroutines.launch

// صفحه‌ی مسدودسازی امنیتی (لودینگ ارزیابی/کارت خطا) — از SecurityScreen.kt جدا شد
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
