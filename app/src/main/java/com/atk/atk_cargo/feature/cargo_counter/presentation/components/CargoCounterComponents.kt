package com.atk.atk_cargo.feature.cargo_counter.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
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
import com.atk.atk_cargo.data.model.MessageType
import com.atk.atk_cargo.feature.cargo_counter.presentation.CargoSnackbarMessage
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds


// TabBar/GroupedShipList/ShipGroup و سلسله‌مراتب کارت انبار/کوتاژ به
// CargoCounterListContent.kt منتقل شدند (DEEP_CODE_AUDIT.md #Phase3.7،
// شکستن God Composable). هم‌پکیج‌اند، نیازی به import اضافه نیست.
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
    message: CargoSnackbarMessage,
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
            delay(3000.milliseconds)
            animatedVisibility = false
            delay(300.milliseconds)
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
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedHeader(
    shipCount: Int,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onSelectShips: () -> Unit = {},
    selectedShipsCount: Int = 0
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "بارگیری‌های فعال",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBoat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "$shipCount کوتاژ فعال",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (selectedShipsCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "$selectedShipsCount کشتی انتخاب شده",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onSelectShips() }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBoat,
                            contentDescription = "انتخاب کشتی‌ها",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onRefresh() }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRefreshing) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "بروزرسانی",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
