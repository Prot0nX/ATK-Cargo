package com.atk.atk_cargo.security

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.atk.atk_cargo.core.domain.AnimationManager

// آیکون‌های متحرک هشدار و بروزرسانی صفحات امنیتی — از SecurityScreen.kt جدا شد (فاز۴ #۴۰)
@Composable
internal fun AnimatedWarningIcon() {
    val pulseScale: Float
    val pulseAlpha: Float
    val shakeAngle: Float
    if (AnimationManager.areAnimationsEnabled) {
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
internal fun AnimatedUpdateIcon(isDark: Boolean) {
    val arrowOffset: Float
    val iconScale: Float
    if (AnimationManager.areAnimationsEnabled) {
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
