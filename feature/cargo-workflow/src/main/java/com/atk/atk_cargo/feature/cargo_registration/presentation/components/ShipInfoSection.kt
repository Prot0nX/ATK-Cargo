package com.atk.atk_cargo.feature.cargo_registration.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AddChart
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atk.atk_cargo.domain.model.Cargo
import com.atk.atk_cargo.domain.model.CargoConfirmStatus
import com.atk.atk_cargo.domain.model.CargoStatus
import com.atk.atk_cargo.domain.model.ShipInfo
import com.atk.atk_cargo.feature.cargo_registration.domain.format.formatNumber
import com.atk.atk_cargo.feature.cargo_registration.domain.format.toEnglishNumbers
import com.atk.atk_cargo.feature.cargoworkflow.R
import java.text.DecimalFormat
import java.util.Locale

@Composable
fun ShipInfoSection(
    shipInfo: ShipInfo,
    isInfoVisible: Boolean,
    onToggleVisibility: () -> Unit,
    loadableTonnage: Float?,
    loadableTrucks18Wheeler: Int?,
    loadableTrucks10Wheeler: Int?
) {
    val loadedPercentage = remember(shipInfo.cargoWeight, shipInfo.totalNetWeight) {
        try {
            val totalWeight = shipInfo.cargoWeight.replace(",", "").toFloatOrNull() ?: 0f
            val loadedWeight = shipInfo.totalNetWeight.replace(",", "").toFloatOrNull() ?: 0f
            if (totalWeight > 0) {
                String.format(Locale.ENGLISH, "%.1f", (loadedWeight / totalWeight) * 100)
            } else "0.0"
        } catch (_: Exception) {
            "0.0"
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // هدر مینیمال
            TopHeader(
                onToggle = onToggleVisibility,
                loadedPercentage = loadedPercentage.toFloat(),
                shipName = shipInfo.shipName,
                cargoType = shipInfo.cargoType,
                quotaNumber = shipInfo.loadingQuotaNumber,
                loadableTonnage = loadableTonnage,
                loadableTrucks18Wheeler = loadableTrucks18Wheeler,
                loadableTrucks10Wheeler = loadableTrucks10Wheeler,
                isExpanded = isInfoVisible,
                tempTonnageStatus = shipInfo.tempTonnageStatus,
                tempTonnageAmount = shipInfo.tempTonnageAmount,
                cargoOwner = shipInfo.cargoOwner
            )

            // محتوای قابل گسترش
            AnimatedVisibility(
                visible = isInfoVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ExpandedContent(
                    shipInfo = shipInfo
                )
            }
        }
    }
}

@Composable
private fun TopHeader(
    onToggle: () -> Unit,
    loadedPercentage: Float,
    shipName: String,
    cargoType: String,
    quotaNumber: String,
    loadableTonnage: Float?,
    loadableTrucks18Wheeler: Int?,
    loadableTrucks10Wheeler: Int?,
    isExpanded: Boolean,
    tempTonnageStatus: Boolean = false,
    tempTonnageAmount: Float? = null,
    cargoOwner: String = ""
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = "rotation"
    )

    val tonnageValue = loadableTonnage ?: 0f
    val tonnageColor = if (tonnageValue < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // سطر ۱: نوع کالا | نام کشتی (راست) --- تناژ مجاز (چپ)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // نوع کالا | نام کشتی
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        if (cargoType.isNotBlank()) {
                            Text(
                                text = cargoType,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "|",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = shipName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // عنوان تناژ مجاز با آیکون
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "تناژ مجاز",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "بستن" else "باز کردن",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(16.dp)
                                .rotate(rotationAngle)
                        )
                    }
                }

                // سطر ۲: کوتاژ | صاحب کالا (راست با وزن حداکثری جهت جلوگیری از ۲ خطی شدن) --- مقدار تناژ (چپ)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayQuota = if (cargoOwner.isNotBlank()) {
                        "$quotaNumber | $cargoOwner"
                    } else {
                        quotaNumber
                    }

                    Text(
                        text = displayQuota,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    val formattedTonnage = DecimalFormat("#,###").format(tonnageValue.toInt())
                    val displayText = if (tempTonnageStatus && tempTonnageAmount != null) {
                        val formattedTempTonnage = DecimalFormat("#,###").format(tempTonnageAmount.toInt())
                        "$formattedTonnage ($formattedTempTonnage)"
                    } else {
                        formattedTonnage
                    }

                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = tonnageColor
                    )
                }

                // سطر ۳: آیکون‌های اختصاصی کامیون‌های ۱۰ چرخ (زرد) و ۱۸ چرخ (آبی)
                val truck10Color = Color(0xFFD97706) // زرد / امبر (Golden Yellow)
                val truck18Color = Color(0xFF2563EB) // آبی (Vibrant Blue)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // کامیون ۱۰ چرخ (اختصاصی - زرد)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_truck_10_wheeler),
                            contentDescription = "کامیون ۱۰ چرخ",
                            tint = truck10Color,
                            modifier = Modifier.size(22.dp)
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = truck10Color.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "10",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = truck10Color,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Text(
                            text = "= ${loadableTrucks10Wheeler ?: 0}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = truck10Color
                        )
                    }

                    Text(
                        text = "|",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // تریلی ۱۸ چرخ (اختصاصی - آبی)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_truck_18_wheeler),
                            contentDescription = "تریلی ۱۸ چرخ",
                            tint = truck18Color,
                            modifier = Modifier.size(24.dp)
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = truck18Color.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "18",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = truck18Color,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Text(
                            text = "= ${loadableTrucks18Wheeler ?: 0}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = truck18Color
                        )
                    }
                }
            }
        }

        // نوار پیشرفت مینیمال
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(loadedPercentage / 100f)
                    .height(2.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun ExpandedContent(
    shipInfo: ShipInfo,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // آمار سریع در دو ستون
        QuickStatsGrid(shipInfo)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // اطلاعات تفصیلی در دو ستون
        DetailedInfoGrid(shipInfo)
    }
}

@Composable
private fun QuickStatsGrid(shipInfo: ShipInfo) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            icon = Icons.Default.Scale,
            value = formatNumber(shipInfo.cargoWeight),
            label = "تناژ کل",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Default.BarChart,
            value = formatNumber(shipInfo.remainingWeight),
            label = "باقیمانده کل",
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            icon = Icons.Default.LocalShipping,
            value = toEnglishNumbers(shipInfo.totalServices),
            label = "حواله‌ها",
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Default.AddChart,
            value = formatNumber(shipInfo.totalNetWeight),
            label = "بارگیری شده",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.08f),
        tonalElevation = 0.5.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetailedInfoGrid(shipInfo: ShipInfo) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        tonalElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "اطلاعات تفصیلی",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // دو ستونه
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DetailInfoItem("انبار", shipInfo.loadingWarehouse, Icons.Default.Warehouse)
                    DetailInfoItem("نوع کالا", shipInfo.cargoType, Icons.Default.Category)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DetailInfoItem("شرکت باربری", shipInfo.shippingCompany, Icons.Default.Business)
                    DetailInfoItem("کوتاژ", shipInfo.loadingQuotaNumber, Icons.Default.ConfirmationNumber)
                }
            }
        }
    }
}

@Composable
private fun DetailInfoItem(
    label: String,
    value: String,
    icon: ImageVector,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CargoInfoRow(
    info: Cargo,
    onRowClick: (Cargo) -> Unit,
    onError: (String) -> Unit,
    duplicateTrackingNumbers: List<String> = emptyList()
) {
    val formattedNetWeight = remember(info.netWeight) {
        try {
            DecimalFormat("#,###").format(info.netWeight?.value ?: 0.0)
        } catch (_: Exception) {
            onError("Invalid netWeight: ${info.netWeight}")
            "0"
        }
    }
    val isDuplicate = duplicateTrackingNumbers.contains(info.trackingNumber)
    val isExited = info.status == CargoStatus.EXITED.wireValue
    val isConfirmed = info.confirm == CargoConfirmStatus.CONFIRMED.wireValue
    val iconColor = if (isExited) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val iconBgColor = if (isExited) 
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) 
    else 
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
    // متغیر محلی لازم است چون exitDate/exitTime از ماژول دیگر smart-cast نمی‌شوند
    val exitDate = info.exitDate
    val exitTime = info.exitTime
    val displayDate = if (isExited && exitDate != null) exitDate else ""
    val displayTime = if (isExited && exitTime != null) exitTime else info.entryTime

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRowClick(info) }
            .padding(12.dp)
    ) {
        // هدر: آیکون، شماره حواله و تاریخ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // آیکون و شماره حواله
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // آیکون دایره‌ای
                Box {
                    Surface(
                        shape = CircleShape,
                        color = if (isDuplicate) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f) else iconBgColor,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = if (isDuplicate) Icons.Default.ContentCopy 
                                    else if (isExited) Icons.Default.LocalShipping 
                                    else Icons.AutoMirrored.Filled.Assignment,
                                contentDescription = null,
                                tint = if (isDuplicate) MaterialTheme.colorScheme.tertiary else iconColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    // نشانگر تأیید/عدم تأیید برای حواله‌های ورودی
                    if (!isExited) {
                        Surface(
                            shape = CircleShape,
                            color = if (isConfirmed) Color(0xFF4CAF50) else Color(0xFFFF9800),
                            modifier = Modifier
                                .size(14.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = if (isConfirmed) Icons.Default.Check else Icons.Default.Schedule,
                                    contentDescription = if (isConfirmed) "تأیید شده" else "در انتظار تأیید",
                                    tint = Color.White,
                                    modifier = Modifier.size(9.dp)
                                )
                            }
                        }
                    }
                }
                
                // شماره حواله
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "شماره حواله",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Badge وضعیت تأیید برای حواله‌های ورودی
                        if (!isExited) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isConfirmed) 
                                    Color(0xFF4CAF50).copy(alpha = 0.15f) 
                                else 
                                    Color(0xFFFF9800).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isConfirmed) "تأیید شده" else "در انتظار",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isConfirmed) Color(0xFF2E7D32) else Color(0xFFE65100),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = info.trackingNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // تاریخ
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = displayDate ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // گرید اطلاعات: تعداد نفرات | وزن خالص | ساعت
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // تعداد نفرات
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = info.numberOfPeople,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "نفر",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // جداکننده
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )

                // وزن خالص
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Scale,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = formattedNetWeight,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // جداکننده
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )

                // ساعت
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = displayTime,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorHandlingCargoInfoRow(
    info: Cargo,
    onRowClick: (Cargo) -> Unit,
    duplicateTrackingNumbers: List<String> = emptyList()
) {
    var hasError by remember { androidx.compose.runtime.mutableStateOf(false) }
    var errorMessage by remember { androidx.compose.runtime.mutableStateOf("") }

    LaunchedEffect(info) {
        hasError = false
        errorMessage = ""
    }

    if (hasError) {
        Text("Error: $errorMessage", color = Color.Red)
    } else {
        CargoInfoRow(
            info = info,
            onRowClick = onRowClick,
            duplicateTrackingNumbers = duplicateTrackingNumbers,
            onError = { error ->
                hasError = true
                errorMessage = "Error rendering item ${info.trackingNumber}: $error"
            }
        )
    }
}
