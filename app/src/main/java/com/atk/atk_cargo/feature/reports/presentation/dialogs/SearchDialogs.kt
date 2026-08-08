package com.atk.atk_cargo.feature.reports.presentation.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atk.atk_cargo.api.CargoInfo
import com.atk.atk_cargo.feature.reports.domain.formatNumber

enum class SearchType {
    RECEIPT_NUMBER,
    TRACKING_NUMBER
}


@Composable
fun MultipleSearchResultDialog(
    cargoInfoList: List<CargoInfo>,
    onDismiss: () -> Unit,
    onSelectCargo: (CargoInfo) -> Unit
) {
    val mainColor = MaterialTheme.colorScheme.primary

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .heightIn(max = 700.dp)
                .padding(16.dp)
                .animateContentSize(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                width = 1.dp,
                color = mainColor
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(mainColor.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = null,
                                tint = mainColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "نتایج جستجو",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${cargoInfoList.size} نتیجه یافت شد",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(mainColor.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = cargoInfoList,
                        key = { cargo -> (cargo.trackingNumber ?: "") + "_" + (cargo.scaleReceiptNumber ?: "") }
                    ) { cargoInfo ->
                        val context = LocalContext.current
                        CargoSearchResultCard(
                            cargoInfo = cargoInfo,
                            onClick = { onSelectCargo(cargoInfo) },
                            mainColor = mainColor,
                            onShare = { cargo ->
                                shareCargoInfo(cargo, context)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = mainColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("بستن")
                }
            }
        }
    }
}

@Composable
private fun CargoSearchResultCard(
    cargoInfo: CargoInfo,
    onClick: () -> Unit,
    mainColor: Color,
    onShare: (CargoInfo) -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, mainColor.copy(alpha = 0.3f)),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(mainColor.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Numbers,
                            contentDescription = null,
                            tint = mainColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "حواله: ${cargoInfo.trackingNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = mainColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onShare(cargoInfo) },
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "اشتراک‌گذاری",
                            tint = mainColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val context = LocalContext.current
                    InfoRowCompact(
                        icon = Icons.Default.Receipt,
                        label = "قبض باسکول",
                        value = cargoInfo.scaleReceiptNumber,
                        isClickable = true,
                        onCopy = {
                            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipData = ClipData.newPlainText("شماره قبض باسکول", cargoInfo.scaleReceiptNumber)
                            clipboardManager.setPrimaryClip(clipData)
                            Toast.makeText(context, "شماره قبض باسکول کپی شد", Toast.LENGTH_SHORT).show()
                        }
                    )
                    InfoRowCompact(
                        icon = Icons.Default.Scale,
                        label = "وزن خالص",
                        value = "${formatNumber(cargoInfo.netWeight.toIntOrNull() ?: 0)} کیلوگرم"
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoRowCompact(
                        icon = Icons.Default.DirectionsBoat,
                        label = "کشتی",
                        value = cargoInfo.shipName
                    )
                    InfoRowCompact(
                        icon = Icons.Default.LocalShipping,
                        label = "شرکت",
                        value = cargoInfo.shippingCompany
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "ورود: ${cargoInfo.entryTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (cargoInfo.exitTime != null)
                        Color(0xFF4CAF50).copy(alpha = 0.1f)
                    else
                        Color(0xFFFF9800).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (cargoInfo.exitTime != null) "خروج شده" else "در انتظار خروج",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (cargoInfo.exitTime != null)
                            Color(0xFF4CAF50)
                        else
                            Color(0xFFFF9800),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRowCompact(
    icon: ImageVector,
    label: String,
    value: String,
    isClickable: Boolean = false,
    onCopy: (() -> Unit)? = null
) {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = if (isClickable && onCopy != null) {
            Modifier.clickable {
                onCopy()
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(label, value)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "$label کپی شد", Toast.LENGTH_SHORT).show()
            }
        } else Modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(14.dp)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isClickable && onCopy != null) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "کپی",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

private fun shareCargoInfo(cargoInfo: CargoInfo, context: Context) {
    val shareText = buildString {
        appendLine("📦 اطلاعات حواله")
        appendLine("━━━━━━━━━━━━━━━━━━━━")
        appendLine("🔢 شماره حواله: ${cargoInfo.trackingNumber}")
        appendLine("🧾 قبض باسکول: ${cargoInfo.scaleReceiptNumber}")
        appendLine("⚖️ وزن خالص: ${formatNumber(cargoInfo.netWeight.toIntOrNull() ?: 0)} کیلوگرم")
        appendLine("🚢 کشتی: ${cargoInfo.shipName}")
        appendLine("🏢 شرکت: ${cargoInfo.shippingCompany}")
        appendLine("📅 زمان ورود: ${cargoInfo.entryTime}")
        if (cargoInfo.exitTime != null) {
            appendLine("🚪 زمان خروج: ${cargoInfo.exitTime}")
            appendLine("✅ وضعیت: خروج شده")
        } else {
            appendLine("⏳ وضعیت: در انتظار خروج")
        }
        appendLine("━━━━━━━━━━━━━━━━━━━━")
        appendLine("📱 ارسال شده از اپلیکیشن ATK Cargo")
    }

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "اطلاعات حواله ${cargoInfo.trackingNumber}")
    }

    val chooserIntent = Intent.createChooser(shareIntent, "اشتراک‌گذاری اطلاعات حواله")

    try {
        context.startActivity(chooserIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "خطا در اشتراک‌گذاری: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
