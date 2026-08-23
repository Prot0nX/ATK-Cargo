package com.atk.atk_cargo.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import com.atk.atk_cargo.data.model.MessageType
import com.atk.atk_cargo.ui.theme.ATKCargoTheme
import kotlinx.coroutines.delay

@Composable
fun StatusSnackbar(
    message: String,
    type: MessageType,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    durationMillis: Long = 3000
) {
    var animatedVisibility by remember { mutableStateOf(false) }

    val translateY by animateDpAsState(
        targetValue = if (animatedVisibility) ATKCargoTheme.spacing.none else ATKCargoTheme.spacing.max * 1.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "translateY"
    )

    val alpha by animateFloatAsState(
        targetValue = if (animatedVisibility) 1f else 0f,
        animationSpec = tween(
            durationMillis = ATKCargoTheme.motion.durationLong1,
            easing = FastOutSlowInEasing
        ), label = "alpha"
    )

    LaunchedEffect(isVisible) {
        if (isVisible) {
            animatedVisibility = true
            delay(durationMillis)
            animatedVisibility = false
            delay(300)
            onDismiss()
        }
    }

    if (isVisible || animatedVisibility) {
        val semanticColors = ATKCargoTheme.semanticColors

        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(
                    bottom = ATKCargoTheme.spacing.xxl,
                    start = ATKCargoTheme.spacing.l,
                    end = ATKCargoTheme.spacing.l
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .graphicsLayer {
 // GraphicsLayerScope خودش Density است، پس تبدیل Dp→px بدون LocalDensity ممکن است
                        translationY = translateY.toPx()
                        this.alpha = alpha
                    },
                shape = ATKCargoTheme.appShapes.large,
                color = when (type) {
                    MessageType.SUCCESS -> semanticColors.successContainer
                    MessageType.ERROR -> MaterialTheme.colorScheme.errorContainer
                    MessageType.WARNING -> semanticColors.warningContainer
                },
                border = BorderStroke(
                    width = ATKCargoTheme.dimensions.borderWidthThin,
                    color = when (type) {
                        MessageType.SUCCESS -> semanticColors.success.copy(alpha = 0.3f)
                        MessageType.ERROR -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                        MessageType.WARNING -> semanticColors.warning.copy(alpha = 0.3f)
                    }
                ),
                shadowElevation = ATKCargoTheme.elevation.cardDefault
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = ATKCargoTheme.spacing.l,
                        vertical = ATKCargoTheme.spacing.m
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = when (type) {
                            MessageType.SUCCESS -> Icons.Outlined.CheckCircle
                            MessageType.ERROR -> Icons.Default.Close
                            MessageType.WARNING -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = when (type) {
                            MessageType.SUCCESS -> semanticColors.success
                            MessageType.ERROR -> MaterialTheme.colorScheme.error
                            MessageType.WARNING -> semanticColors.warning
                        },
                        modifier = Modifier.size(ATKCargoTheme.dimensions.iconMedium)
                    )

                    Spacer(modifier = Modifier.width(ATKCargoTheme.spacing.m))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = when (type) {
                            MessageType.SUCCESS -> semanticColors.onSuccessContainer
                            MessageType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                            MessageType.WARNING -> semanticColors.onWarningContainer
                        }
                    )
                }
            }
        }
    }
}

