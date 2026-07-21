package com.atk.atk_cargo.feature.cargo_entry.presentation.components

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.atk.atk_cargo.api.MessageType
import com.atk.atk_cargo.feature.cargo_entry.presentation.SnackbarMessage
import com.atk.atk_cargo.ui.theme.Blue50
import com.atk.atk_cargo.ui.theme.Blue500
import com.atk.atk_cargo.ui.theme.Purple50
import com.atk.atk_cargo.ui.theme.Purple500
import com.atk.atk_cargo.ui.theme.Red50
import com.atk.atk_cargo.ui.theme.Red500
import kotlinx.coroutines.delay

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
                    .offset(y = translateY)
                    .alpha(alpha),
                shape = RoundedCornerShape(16.dp),
                color = when (message.type) {
                    MessageType.SUCCESS -> Blue50
                    MessageType.ERROR -> Red50
                    MessageType.WARNING -> Purple50
                },
                border = BorderStroke(1.dp, when (message.type) {
                    MessageType.SUCCESS -> Blue500.copy(alpha = 0.2f)
                    MessageType.ERROR -> Red500.copy(alpha = 0.2f)
                    MessageType.WARNING -> Purple500.copy(alpha = 0.2f)
                }),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = when (message.type) {
                            MessageType.SUCCESS -> Icons.Outlined.CheckCircle
                            MessageType.ERROR -> Icons.Default.Close
                            MessageType.WARNING -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = when (message.type) {
                            MessageType.SUCCESS -> Blue500
                            MessageType.ERROR -> Red500
                            MessageType.WARNING -> Purple500
                        },
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = message.message,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = when (message.type) {
                            MessageType.SUCCESS -> Blue500
                            MessageType.ERROR -> Red500
                            MessageType.WARNING -> Purple500
                        }
                    )
                }
            }
        }
    }
}
