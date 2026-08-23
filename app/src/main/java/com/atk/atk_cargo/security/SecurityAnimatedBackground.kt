package com.atk.atk_cargo.security

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.atk.atk_cargo.core.domain.AnimationManager
import kotlin.math.cos
import kotlin.math.sin

// پس‌زمینه‌ی انیمیشنی اوربیتال و اسکنر رادار مشترک بین صفحات امنیتی — از SecurityScreen.kt جدا شد
@Composable
internal fun DynamicPremiumBackground(isDark: Boolean) {
    val t1: Float
    val t2: Float
    if (AnimationManager.areAnimationsEnabled) {
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
internal fun AdvancedOrbitalScanner(modifier: Modifier = Modifier) {
    val rotation1: Float
    val rotation2: Float
    val pulseScale: Float
    val scanOffset: Float
    if (AnimationManager.areAnimationsEnabled) {
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
