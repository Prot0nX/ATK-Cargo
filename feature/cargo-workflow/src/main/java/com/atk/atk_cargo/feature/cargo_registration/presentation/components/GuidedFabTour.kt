package com.atk.atk_cargo.feature.cargo_registration.presentation.components

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.toSize
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

// ===== تور راهنمای دکمه شناور (FAB) =====
// نمایش یک‌باره برای هر کاربر، مطابق موقعیت واقعی دکمه شناور و آیتم‌های آن روی صفحه

data class GuidedTourStep(
    val bounds: Rect,
    val title: String,
    val description: String,
    val icon: ImageVector
)

// موقعیت و اندازه‌ی واقعی عنصر هدف را (نسبت به ریشه‌ی صفحه) برای هایلایت تور ثبت می‌کند
fun Modifier.tourTarget(onBoundsChanged: (Rect) -> Unit): Modifier = this.onGloballyPositioned { coordinates ->
    onBoundsChanged(Rect(offset = coordinates.positionInRoot(), size = coordinates.size.toSize()))
}

object GuidedTourPreferences {
    private const val PREFS_NAME = "atk_guided_tour_prefs"

    const val KEY_REGISTER_CARGO_FAB = "register_cargo_fab_tour_seen"
    const val KEY_CARGO_DETAILS_FAB = "cargo_details_fab_tour_seen"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasSeen(context: Context, key: String): Boolean = prefs(context).getBoolean(key, false)

    fun markSeen(context: Context, key: String) {
        prefs(context).edit().putBoolean(key, true).apply()
    }
}

@Composable
fun GuidedFabTourOverlay(
    steps: List<GuidedTourStep>,
    currentStep: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    if (steps.isEmpty() || currentStep !in steps.indices) return
    val step = steps[currentStep]
    val density = LocalDensity.current
    val accent = MaterialTheme.colorScheme.primary

    BackHandler(onBack = onSkip)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }
        val highlightPaddingPx = with(density) { 10.dp.toPx() }
        val cornerRadiusPx = with(density) { 20.dp.toPx() }

        val highlightLeft = max(0f, step.bounds.left - highlightPaddingPx)
        val highlightTop = max(0f, step.bounds.top - highlightPaddingPx)
        val highlightRight = min(screenWidthPx, step.bounds.right + highlightPaddingPx)
        val highlightBottom = min(screenHeightPx, step.bounds.bottom + highlightPaddingPx)
        val highlightWidth = (highlightRight - highlightLeft).coerceAtLeast(0f)
        val highlightHeight = (highlightBottom - highlightTop).coerceAtLeast(0f)

        val infiniteTransition = rememberInfiniteTransition(label = "fab_tour_pulse")
        val pulse by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1100, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "fab_tour_pulse_value"
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onNext
                )
        ) {
            val scrimPath = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(Rect(Offset.Zero, Size(size.width, size.height)), Path.Direction.CounterClockwise)
                addRoundRect(
                    RoundRect(
                        rect = Rect(highlightLeft, highlightTop, highlightRight, highlightBottom),
                        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                    ),
                    Path.Direction.CounterClockwise
                )
            }
            drawPath(scrimPath, color = Color.Black.copy(alpha = 0.72f))

            val ringExpand = 10f * pulse
            drawRoundRect(
                color = accent.copy(alpha = (1f - pulse) * 0.8f),
                topLeft = Offset(highlightLeft - ringExpand, highlightTop - ringExpand),
                size = Size(highlightWidth + ringExpand * 2, highlightHeight + ringExpand * 2),
                cornerRadius = CornerRadius(cornerRadiusPx + ringExpand, cornerRadiusPx + ringExpand),
                style = Stroke(width = with(density) { 2.dp.toPx() })
            )
            drawRoundRect(
                color = accent,
                topLeft = Offset(highlightLeft, highlightTop),
                size = Size(highlightWidth, highlightHeight),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                style = Stroke(width = with(density) { 2.5.dp.toPx() })
            )
        }

        val cardWidth = min(320.dp, maxWidth - 32.dp)
        val cardWidthPx = with(density) { cardWidth.toPx() }
        val screenMarginPx = with(density) { 16.dp.toPx() }
        val targetGapPx = with(density) { 18.dp.toPx() }

 // با Layout سفارشی و place() (نه placeRelative)، جای‌گذاری کاملاً مطلق و مستقل از راست‌به‌چپ بودن صفحه انجام می‌شود
 // تا کادر دقیقاً مطابق مختصات واقعی هدف (که با positionInRoot مطلق ثبت شده) قرار گیرد و از لبه صفحه بیرون نزند
        Layout(
            content = {
                TourStepCard(
                    step = step,
                    stepIndex = currentStep,
                    totalSteps = steps.size,
                    accent = accent,
                    onNext = onNext,
                    onSkip = onSkip
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { measurables, constraints ->
            val cardConstraints = Constraints(maxWidth = cardWidthPx.roundToInt())
            val placeable = measurables.first().measure(cardConstraints)

            val idealLeft = highlightLeft + highlightWidth / 2f - placeable.width / 2f
            val cardLeftPx = idealLeft.coerceIn(
                screenMarginPx,
                (screenWidthPx - placeable.width - screenMarginPx).coerceAtLeast(screenMarginPx)
            )

            val spaceBelow = screenHeightPx - highlightBottom
            val spaceAbove = highlightTop
            val placeBelow = spaceBelow > spaceAbove
            val cardTopPx = if (placeBelow) {
                (highlightBottom + targetGapPx).coerceAtMost(
                    (screenHeightPx - placeable.height - screenMarginPx).coerceAtLeast(screenMarginPx)
                )
            } else {
                (highlightTop - targetGapPx - placeable.height).coerceAtLeast(screenMarginPx)
            }

            layout(constraints.maxWidth, constraints.maxHeight) {
                placeable.place(cardLeftPx.roundToInt(), cardTopPx.roundToInt())
            }
        }
    }
}

@Composable
private fun TourStepCard(
    step: GuidedTourStep,
    stepIndex: Int,
    totalSteps: Int,
    accent: Color,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 14.dp,
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(accent.copy(alpha = 0.14f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = step.icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onSkip, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "رد کردن تور",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    repeat(totalSteps) { index ->
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (index == stepIndex) 18.dp else 6.dp)
                                .background(
                                    color = if (index == stepIndex) accent else accent.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(50)
                                )
                        )
                    }
                }

                Surface(
                    onClick = onNext,
                    shape = RoundedCornerShape(12.dp),
                    color = accent
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (stepIndex == totalSteps - 1) "متوجه شدم" else "بعدی",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (stepIndex < totalSteps - 1) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
