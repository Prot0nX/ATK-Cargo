package com.atk.atk_cargo.feature.update.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atk.atk_cargo.api.UpdateInfo
import com.atk.atk_cargo.api.UpdateManager

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    downloadProgress: UpdateManager.DownloadProgress,
    downloadState: UpdateManager.DownloadState,
    onUpdateClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onCancelClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val dialogAlpha by animateFloatAsState(targetValue = 1f, label = "")

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Dialog(
            onDismissRequest = {
                if (downloadState !is UpdateManager.DownloadState.Downloading) onDismiss()
            },
            properties = DialogProperties(
                dismissOnBackPress = downloadState !is UpdateManager.DownloadState.Downloading,
                dismissOnClickOutside = downloadState !is UpdateManager.DownloadState.Downloading,
                usePlatformDefaultWidth = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .wrapContentHeight()
                    .alpha(dialogAlpha),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEFF1F5)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ModernUpdateHeader(
                        version = updateInfo.latestVersion
                    )

                    ModernUpdateContent(
                        message = updateInfo.updateMessage
                    )

                    ModernUpdateActionSection(
                        downloadState = downloadState,
                        downloadProgress = downloadProgress,
                        updateSize = updateInfo.updateSize,
                        onUpdateClick = onUpdateClick,
                        onPauseClick = onPauseClick,
                        onResumeClick = onResumeClick,
                        onCancelClick = onCancelClick
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadingState(
    progress: UpdateManager.DownloadProgress,
    onPauseClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val progressAnimation by animateFloatAsState(
            targetValue = progress.progress / 100,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = ""
        )

        DownloadProgressInfo(progress = progress)

        LinearProgressIndicator(
            progress = { progressAnimation },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        DownloadActionButtons(
            onPauseClick = onPauseClick,
            onCancelClick = onCancelClick
        )
    }
}

@Composable
private fun DownloadProgressInfo(progress: UpdateManager.DownloadProgress) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DownloadSizeInfo(
            downloadedSize = progress.downloadedSize,
            totalSize = progress.totalSize
        )

        DownloadSpeedInfo(speed = progress.speed)
    }
}

@Composable
private fun DownloadSizeInfo(
    downloadedSize: Pair<String, String>,
    totalSize: Pair<String, String>
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = downloadedSize.first,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = downloadedSize.second,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "/",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = totalSize.first,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = totalSize.second,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun DownloadSpeedInfo(speed: Pair<String, String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = speed.first,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = speed.second,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun DownloadActionButtons(
    onPauseClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onCancelClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("لغو")
        }
        Button(
            onClick = onPauseClick,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                Icons.Default.Pause,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("توقف")
        }
    }
}

@Composable
private fun PausedState(
    progress: UpdateManager.DownloadProgress,
    onResumeClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Pause,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "دانلود متوقف شده",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            DownloadSizeInfo(
                downloadedSize = progress.downloadedSize,
                totalSize = progress.totalSize
            )
        }

        val progressAnimation by animateFloatAsState(
            targetValue = progress.progress / 100,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = ""
        )

        LinearProgressIndicator(
            progress = { progressAnimation },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancelClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("لغو")
            }
            Button(
                onClick = onResumeClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("ادامه")
            }
        }
    }
}

@Composable
private fun ModernUpdateHeader(
    version: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "به‌روزرسانی جدید",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )
        
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFDCEEFE),
            modifier = Modifier.wrapContentWidth()
        ) {
            Text(
                text = version,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A73E8),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun ModernUpdateContent(
    message: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "")
        val bounce by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -12f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = ""
        )
        
        Surface(
            modifier = Modifier
                .size(80.dp)
                .offset(y = bounce.dp),
            shape = CircleShape,
            color = Color(0xFFEFF6FF)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SystemUpdate,
                    contentDescription = null,
                    tint = Color(0xFF1A73E8),
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        
        if (message.isNotEmpty()) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun ModernUpdateActionSection(
    downloadState: UpdateManager.DownloadState,
    downloadProgress: UpdateManager.DownloadProgress,
    updateSize: String,
    onUpdateClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (downloadState) {
            is UpdateManager.DownloadState.Idle -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SdStorage,
                            contentDescription = null,
                            tint = Color(0xFF6B7280).copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "حجم فایل:",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280)
                        )
                    }
                    
                    val sizeLabel = remember(updateSize) {
                        val t = updateSize.trim()
                        if (t.matches(Regex("^[0-9]+(\\.[0-9]+)?$"))) "$t MB" else t
                    }
                    Text(
                        text = sizeLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1F2937)
                    )
                }
                
                Button(
                    onClick = onUpdateClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A73E8)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "شروع دانلود",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            is UpdateManager.DownloadState.Downloading -> {
                DownloadingState(
                    progress = downloadProgress,
                    onPauseClick = onPauseClick,
                    onCancelClick = onCancelClick
                )
            }

            is UpdateManager.DownloadState.Paused -> {
                PausedState(
                    progress = downloadProgress,
                    onResumeClick = onResumeClick,
                    onCancelClick = onCancelClick
                )
            }

            is UpdateManager.DownloadState.Error -> {
                ErrorState(
                    message = downloadState.message,
                    onRetryClick = onUpdateClick,
                    onCancelClick = onCancelClick
                )
            }

            else -> {}
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetryClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancelClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("انصراف")
            }
            Button(
                onClick = onRetryClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("تلاش مجدد")
            }
        }
    }
}
