package com.atk.atk_cargo.feature.startup.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsBoat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun ServerSyncingScreen() {
    val primaryColor = MaterialTheme.colorScheme.primary
    val bgColor = MaterialTheme.colorScheme.background
    val onBgColor = MaterialTheme.colorScheme.onBackground

    val infiniteTransition = rememberInfiniteTransition(label = "server_sync")

    // ===== ANIMATIONS =====
    val orbitRotation1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "orbit1"
    )
    val orbitRotation2 by infiniteTransition.animateFloat(
        initialValue = 360f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(5500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "orbit2"
    )
    val orbitRotation3 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "orbit3"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f, targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "iconScale"
    )
    val particleOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "p1"
    )
    val particleOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "p2"
    )
    val particleOffset3 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "p3"
    )
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "shimmer"
    )

    val contentAlpha = remember { Animatable(0f) }
    val contentTranslateY = remember { Animatable(30f) }
    LaunchedEffect(Unit) {
        launch {
            contentAlpha.animateTo(1f, animationSpec = tween(800, easing = EaseOutCubic))
        }
        launch {
            contentTranslateY.animateTo(0f, animationSpec = tween(800, easing = EaseOutCubic))
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = bgColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(contentAlpha.value)
                .offset(y = contentTranslateY.value.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val w = size.width
                val h = size.height
                val particleColor = primaryColor.copy(alpha = 0.12f)
                val particleColorFaint = primaryColor.copy(alpha = 0.06f)

                val particles = listOf(
                    Triple(0.15f, 0.2f, particleOffset1),
                    Triple(0.82f, 0.15f, particleOffset2),
                    Triple(0.08f, 0.75f, particleOffset3),
                    Triple(0.88f, 0.8f, particleOffset1),
                    Triple(0.5f, 0.1f, particleOffset2),
                    Triple(0.35f, 0.85f, particleOffset3),
                    Triple(0.7f, 0.35f, particleOffset1),
                    Triple(0.25f, 0.55f, particleOffset2),
                )

                particles.forEachIndexed { index, (baseX, baseY, offset) ->
                    val floatRange = 40f + (index * 8f)
                    val yOffset = kotlin.math.sin(offset * 2 * Math.PI.toFloat()) * floatRange
                    val xOffset = kotlin.math.cos(offset * 2 * Math.PI.toFloat() + index) * (floatRange * 0.5f)
                    val radius = (3f + (index % 3) * 2.5f).dp.toPx()
                    val alpha = 0.3f + kotlin.math.sin(offset * Math.PI.toFloat()) * 0.4f
                    drawCircle(
                        color = if (index % 2 == 0) particleColor.copy(alpha = alpha * 0.5f) else particleColorFaint.copy(alpha = alpha * 0.4f),
                        radius = radius,
                        center = androidx.compose.ui.geometry.Offset(
                            x = baseX * w + xOffset,
                            y = baseY * h + yOffset
                        )
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .rotate(orbitRotation1)
                            .border(
                                width = 1.dp,
                                brush = Brush.sweepGradient(
                                    listOf(
                                        primaryColor.copy(alpha = 0.0f),
                                        primaryColor.copy(alpha = 0.3f),
                                        primaryColor.copy(alpha = 0.0f),
                                        primaryColor.copy(alpha = 0.15f),
                                        primaryColor.copy(alpha = 0.0f)
                                    )
                                ),
                                shape = CircleShape
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopCenter)
                                .offset(y = (-4).dp)
                                .background(
                                    color = primaryColor.copy(alpha = 0.7f),
                                    shape = CircleShape
                                )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .rotate(orbitRotation2)
                            .border(
                                width = 1.5.dp,
                                brush = Brush.sweepGradient(
                                    listOf(
                                        primaryColor.copy(alpha = 0.0f),
                                        primaryColor.copy(alpha = 0.4f),
                                        primaryColor.copy(alpha = 0.0f),
                                        primaryColor.copy(alpha = 0.2f),
                                        primaryColor.copy(alpha = 0.0f)
                                    )
                                ),
                                shape = CircleShape
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .align(Alignment.CenterEnd)
                                .offset(x = 3.dp)
                                .background(
                                    color = primaryColor.copy(alpha = 0.8f),
                                    shape = CircleShape
                                )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .rotate(orbitRotation3)
                            .border(
                                width = 1.dp,
                                brush = Brush.sweepGradient(
                                    listOf(
                                        primaryColor.copy(alpha = 0.0f),
                                        primaryColor.copy(alpha = 0.25f),
                                        primaryColor.copy(alpha = 0.0f),
                                    )
                                ),
                                shape = CircleShape
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .align(Alignment.BottomCenter)
                                .offset(y = 2.dp)
                                .background(
                                    color = primaryColor.copy(alpha = 0.6f),
                                    shape = CircleShape
                                )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(iconScale * 1.2f)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = glowAlpha),
                                        primaryColor.copy(alpha = glowAlpha * 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .scale(iconScale)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        primaryColor,
                                        primaryColor.copy(alpha = 0.8f)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DirectionsBoat,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "در حال ارتباط با سرور",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = onBgColor
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "لطفاً چند لحظه شکیبا باشید",
                    style = MaterialTheme.typography.bodyLarge,
                    color = onBgColor.copy(alpha = 0.55f)
                )

                Spacer(modifier = Modifier.height(48.dp))

                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(primaryColor.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.4f)
                            .offset(x = (shimmerOffset * 220).dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        primaryColor.copy(alpha = 0.6f),
                                        primaryColor,
                                        primaryColor.copy(alpha = 0.6f),
                                        Color.Transparent,
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val dotAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.2f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(
                                    durationMillis = 600,
                                    delayMillis = index * 200,
                                    easing = FastOutSlowInEasing
                                ),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "dot$index"
                        )
                        val dotScale by infiniteTransition.animateFloat(
                            initialValue = 0.7f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(
                                    durationMillis = 600,
                                    delayMillis = index * 200,
                                    easing = FastOutSlowInEasing
                                ),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "dotScale$index"
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .scale(dotScale)
                                .alpha(dotAlpha)
                                .background(
                                    color = primaryColor,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}
