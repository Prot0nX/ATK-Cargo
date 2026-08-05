package com.atk.atk_cargo.feature.home.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val NotificationAccent = Color(0xFF0D9488)
private val NotificationAccentBg = Color(0xFFDCEFEA)
private val NotificationCardBorder = Color(0xFFE4E6E9)
private val NotificationMutedBg = Color(0xFFF3F4F5)
private val NotificationMutedText = Color(0xFF8A8F98)
private val NotificationTitleColor = Color(0xFF1F2937)

@Composable
fun NotificationSettingRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (enabled) NotificationAccentBg.copy(alpha = 0.5f) else NotificationMutedBg,
        border = BorderStroke(1.dp, if (enabled) NotificationAccentBg else NotificationCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(
                            if (enabled) NotificationAccentBg else Color.White,
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (enabled) NotificationAccent else NotificationMutedText
                    )
                }

                Switch(
                    checked = enabled,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = NotificationAccent,
                        checkedBorderColor = NotificationAccent,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = NotificationMutedText.copy(alpha = 0.4f),
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = NotificationTitleColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = NotificationMutedText
                )
            }
        }
    }
}
