package com.atk.atk_cargo.security

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// دیالوگ اعلام نیاز به بروزرسانی نسخه‌ی منقضی‌شده — از SecurityScreen.kt جدا شد
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
