package com.atk.atk_cargo.feature.cargo_entry.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.atk.atk_cargo.feature.cargo_entry.domain.formatNumber

// دیالوگ‌های تأیید، تکراری، عدم‌تطابق و پیام صفحه‌ی ثبت اطلاعات اولیه بار که از InitialInfoScreen.kt منتقل شده‌اند

@Composable
fun ConfirmationDialog(
    shipName: String,
    loadingWarehouse: String,
    cargoType: String,
    cargoWeight: String,
    loadingQuotaNumber: String,
    cargoOwner: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = rememberInitialInfoPalette()
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val dialogScale by animateFloatAsState(if (isVisible) 1f else 0.9f, label = "dialog_scale")
    val dialogAlpha by animateFloatAsState(if (isVisible) 1f else 0f, label = "dialog_alpha")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = dialogScale
                    scaleY = dialogScale
                    alpha = dialogAlpha
                },
            shape = RoundedCornerShape(24.dp),
            color = palette.cardBg,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(palette.accentBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.FactCheck, contentDescription = null, tint = palette.accent)
                    }
                    Column {
                        Text("تأیید نهایی", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        Text("آیا از صحت اطلاعات اطمینان دارید؟", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    EnhancedInfoGroup(
                        title = "اطلاعات کشتی و بار",
                        icon = Icons.Default.DirectionsBoat,
                        items = listOf(
                            "کشتی" to shipName,
                            "انبار" to loadingWarehouse,
                            "نوع کالا" to cargoType
                        )
                    )
                    EnhancedInfoGroup(
                        title = "جزییات محموله",
                        icon = Icons.Default.Inventory,
                        items = listOf(
                            "وزن کل" to "${formatNumber(cargoWeight)} کیلوگرم",
                            "شماره کوتاژ" to loadingQuotaNumber,
                            "صاحب کالا" to cargoOwner
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, palette.cardBorder)
                    ) {
                        Text("انصراف", style = MaterialTheme.typography.titleSmall)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = Color.White)
                    ) {
                        Text("تأیید و ثبت", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@Composable
fun DuplicateDialog(
    onDismiss: () -> Unit,
    onReviewEdit: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val palette = rememberInitialInfoPalette()
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val dialogScale by animateFloatAsState(if (isVisible) 1f else 0.9f, label = "dialog_scale")
    val dialogAlpha by animateFloatAsState(if (isVisible) 1f else 0f, label = "dialog_alpha")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = dialogScale
                    scaleY = dialogScale
                    alpha = dialogAlpha
                },
            shape = RoundedCornerShape(24.dp),
            color = palette.cardBg,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("شماره کوتاژ تکراری", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text(
                        "این شماره کوتاژ قبلاً در سیستم ثبت شده است. چه اقدامی می‌خواهید انجام دهید؟",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onReviewEdit,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ویرایش اطلاعات", style = MaterialTheme.typography.titleSmall)
                    }
                    OutlinedButton(
                        onClick = onNavigateToRegister,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, palette.cardBorder)
                    ) {
                        Text("مشاهده در لیست", style = MaterialTheme.typography.titleSmall)
                    }
                    TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text("بستن", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun PartialMatchDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val palette = rememberInitialInfoPalette()
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val dialogScale by animateFloatAsState(if (isVisible) 1f else 0.9f, label = "dialog_scale")
    val dialogAlpha by animateFloatAsState(if (isVisible) 1f else 0f, label = "dialog_alpha")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = dialogScale
                    scaleY = dialogScale
                    alpha = dialogAlpha
                },
            shape = RoundedCornerShape(24.dp),
            color = palette.cardBg,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(palette.accentBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = palette.accent, modifier = Modifier.size(32.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ثبت جدید", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text(
                        "این شماره کوتاژ قبلاً با مشخصات متفاوتی ثبت شده است. آیا مایل به ثبت اطلاعات جدید هستید؟",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, palette.cardBorder)
                    ) {
                        Text("انصراف")
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = Color.White)
                    ) {
                        Text("تأیید و ادامه")
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedInfoGroup(
    title: String,
    icon: ImageVector,
    items: List<Pair<String, String>>
) {
    val palette = rememberInitialInfoPalette()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.mutedBg.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .border(1.dp, palette.cardBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = palette.accent, modifier = Modifier.size(18.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, color = palette.accent, fontWeight = FontWeight.Bold)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { (label, value) ->
                EnhancedInfoRow(label, value)
            }
        }
    }
}

@Composable
private fun EnhancedInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ModernAlertDialog(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit
) {
    val palette = rememberInitialInfoPalette()
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    val dialogScale by animateFloatAsState(if (isVisible) 1f else 0.9f, label = "dialog_scale")
    val dialogAlpha by animateFloatAsState(if (isVisible) 1f else 0f, label = "dialog_alpha")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = dialogScale
                    scaleY = dialogScale
                    alpha = dialogAlpha
                },
            shape = RoundedCornerShape(24.dp),
            color = palette.cardBg,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.size(64.dp).background(
                        if (isError) MaterialTheme.colorScheme.errorContainer else palette.accentBg,
                        CircleShape
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isError) MaterialTheme.colorScheme.error else palette.accent,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(message, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isError) MaterialTheme.colorScheme.error else palette.accent,
                        contentColor = Color.White
                    )
                ) {
                    Text("متوجه شدم")
                }
            }
        }
    }
}
