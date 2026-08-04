package com.atk.atk_cargo.feature.reports.presentation.quota_details.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.atk.atk_cargo.ui.theme.Red100
import com.atk.atk_cargo.ui.theme.Red400
import com.atk.atk_cargo.ui.theme.Red700
import com.atk.atk_cargo.ui.theme.Teal50
import com.atk.atk_cargo.ui.theme.Teal900

private val QuotaDialogTealAccent: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF2DD4BF) else Teal900

private val QuotaDialogTealAccentBg: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF134E4A) else Teal50

private val QuotaDialogRedAccent: Color
    @Composable get() = if (isSystemInDarkTheme()) Red400 else Red700

private val QuotaDialogRedAccentBg: Color
    @Composable get() = if (isSystemInDarkTheme()) Red700.copy(alpha = 0.25f) else Red100

private val QuotaDialogOnTealAccent: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF042F2E) else Color.White

private val QuotaDialogOnRedAccent: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF450A0A) else Color.White

@Composable
private fun QuotaConfirmDialog(
    iconBg: Color,
    iconTint: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
    confirmLabel: String,
    confirmColor: Color,
    onConfirmColor: Color = Color.White,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(color = iconBg, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.4f
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(13.dp),
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = QuotaDialogTealAccent,
                                contentColor = QuotaDialogOnTealAccent
                            )
                        ) {
                            Text("انصراف", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        }
                        TextButton(
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(13.dp),
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = confirmColor,
                                contentColor = onConfirmColor
                            )
                        ) {
                            Text(confirmLabel, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteQuotaDialog(
    quotaNumber: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    QuotaConfirmDialog(
        iconBg = QuotaDialogRedAccentBg,
        iconTint = QuotaDialogRedAccent,
        icon = Icons.Default.Delete,
        title = "حذف کوتاژ",
        body = "آیا از حذف کوتاژ شماره $quotaNumber و تمام حواله‌های مرتبط با آن اطمینان دارید؟ این عملیات قابل بازگشت نیست.",
        confirmLabel = "حذف",
        confirmColor = QuotaDialogRedAccent,
        onConfirmColor = QuotaDialogOnRedAccent,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun ToggleQuotaStatusDialog(
    quotaNumber: String,
    isActive: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    QuotaConfirmDialog(
        iconBg = QuotaDialogTealAccentBg,
        iconTint = QuotaDialogTealAccent,
        icon = Icons.Default.PowerSettingsNew,
        title = "تغییر وضعیت کوتاژ",
        body = "آیا از ${if (isActive) "غیرفعال" else "فعال"} کردن کوتاژ شماره $quotaNumber اطمینان دارید؟",
        confirmLabel = "تأیید",
        confirmColor = QuotaDialogTealAccent,
        onConfirmColor = QuotaDialogOnTealAccent,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
