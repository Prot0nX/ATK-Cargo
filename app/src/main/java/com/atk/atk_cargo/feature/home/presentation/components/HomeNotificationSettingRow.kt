package com.atk.atk_cargo.feature.home.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.atk.atk_cargo.ui.theme.Teal200

private val NotificationAccentLight = Color(0xFF0D9488)

@Composable
fun NotificationSettingRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val accent = if (isDark) Teal200 else NotificationAccentLight
    val accentBg = accent.copy(alpha = if (isDark) 0.2f else 0.16f)
    val cardBorder = MaterialTheme.colorScheme.outlineVariant
    val mutedBg = MaterialTheme.colorScheme.surfaceVariant
    val mutedText = MaterialTheme.colorScheme.onSurfaceVariant
    val titleColor = MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (enabled) accentBg else mutedBg,
        border = BorderStroke(1.dp, if (enabled) accent.copy(alpha = 0.4f) else cardBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(
                            if (enabled) accentBg else MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (enabled) accent else mutedText
                    )
                }

                CompactSwitch(
                    checked = enabled,
                    accent = accent,
                    mutedText = mutedText,
                    onCheckedChange = onCheckedChange
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = mutedText,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CompactSwitch(
    checked: Boolean,
    accent: Color,
    mutedText: Color,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 17.dp else 3.dp,
        animationSpec = tween(150),
        label = "switch_thumb"
    )

    Box(
        modifier = modifier
            .size(width = 36.dp, height = 20.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (checked) accent else mutedText.copy(alpha = 0.35f))
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(14.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
