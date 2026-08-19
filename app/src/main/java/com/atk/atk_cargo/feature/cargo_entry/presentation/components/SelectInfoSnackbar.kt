package com.atk.atk_cargo.feature.cargo_entry.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.atk.atk_cargo.api.MessageType
import com.atk.atk_cargo.feature.cargo_entry.presentation.SnackbarMessage
import kotlinx.coroutines.delay

@Composable
private fun snackbarAccent(type: MessageType, isDark: Boolean): Color = when (type) {
    MessageType.SUCCESS -> if (isDark) Color(0xFF2DD4BF) else Color(0xFF0D9488)
    MessageType.ERROR -> if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
    MessageType.WARNING -> if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
}

@Composable
private fun snackbarAccentBg(type: MessageType, isDark: Boolean, accent: Color): Color = when (type) {
    MessageType.SUCCESS -> if (isDark) accent.copy(alpha = 0.16f) else Color(0xFFDCEFEA)
    MessageType.ERROR -> if (isDark) accent.copy(alpha = 0.16f) else Color(0xFFFDE8E8)
    MessageType.WARNING -> if (isDark) accent.copy(alpha = 0.16f) else Color(0xFFFEF3E2)
}

@Composable
fun StatusSnackbar(
    message: SnackbarMessage,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var animatedVisibility by remember { mutableStateOf(false) }

    val translateY by animateDpAsState(
        targetValue = if (animatedVisibility) 0.dp else 100.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ), label = ""
    )

    val alpha by animateFloatAsState(
        targetValue = if (animatedVisibility) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ), label = ""
    )

    LaunchedEffect(isVisible) {
        if (isVisible) {
            animatedVisibility = true
            delay(3000)
            animatedVisibility = false
            delay(300)
            onDismiss()
        }
    }

    if (isVisible || animatedVisibility) {
        val isDark = isSystemInDarkTheme()
        val accent = snackbarAccent(message.type, isDark)
        val accentBg = snackbarAccentBg(message.type, isDark, accent)
        val icon = when (message.type) {
            MessageType.SUCCESS -> Icons.Outlined.CheckCircle
            MessageType.ERROR -> Icons.Default.Close
            MessageType.WARNING -> Icons.Default.Info
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .graphicsLayer {
                        translationY = translateY.toPx()
                        this.alpha = alpha
                    },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, accent.copy(alpha = if (isDark) 0.35f else 0.3f)),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = message.message,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
