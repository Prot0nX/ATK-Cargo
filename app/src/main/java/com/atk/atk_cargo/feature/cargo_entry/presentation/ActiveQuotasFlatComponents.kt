package com.atk.atk_cargo.feature.cargo_entry.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atk.atk_cargo.api.ActiveShipInfo
import com.atk.atk_cargo.api.formatNumber

// بخش «کارت کوتاژ در نمای مسطح (Flat)» دیالوگ کوتاژهای فعال — از
// ActiveQuotasContent.kt استخراج شد (DEEP_CODE_REVIEW.md Phase4 #30، تفکیک
// فایل ۱٬۴۲۰ خطی). internal چون از FlatQuotasContent در فایل اصلی صدا زده
// می‌شود؛ بدون هیچ تغییر منطقی، صرفاً جابه‌جایی.

@Composable
internal fun FlatQuotaCard(
    quota: ActiveShipInfo,
    searchQuery: String
) {
    val totalVouchers = quota.entryVouchers + quota.exitVouchers
    val remainingVouchers = totalVouchers - quota.exitVouchers
    val isCompleted = remainingVouchers == 0

    val cardColor = when {
        isCompleted -> QuotasAccentBg.copy(alpha = 0.5f)
        remainingVouchers > 0 -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        else -> QuotasMutedBg.copy(alpha = 0.5f)
    }

    val borderColor = when {
        isCompleted -> QuotasAccentBorder
        remainingVouchers > 0 -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
        else -> QuotasCardBorder
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(width = 1.dp, color = borderColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // ردیف اول: کشتی و شماره کوتاژ
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // کشتی و شماره کوتاژ
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // آیکون کشتی
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                color = if (isCompleted)
                                    QuotasAccentBg
                                else if (remainingVouchers > 0)
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                else
                                    QuotasMutedBg
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBoat,
                            contentDescription = null,
                            tint = if (isCompleted)
                                QuotasAccent
                            else if (remainingVouchers > 0)
                                MaterialTheme.colorScheme.error
                            else
                                QuotasMutedText,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (searchQuery.isNotEmpty() && quota.shipName.contains(searchQuery, ignoreCase = true)) {
                                val parts = quota.shipName.split(
                                    searchQuery,
                                    ignoreCase = true
                                )
                                Row {
                                    for (i in parts.indices) {
                                        if (i > 0) {
                                            Text(
                                                text = searchQuery,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(
                                            text = parts[i],
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = quota.shipName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (quota.cargoType.isNotBlank()) {
                                Text(
                                    text = "|",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = quota.cargoType,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // شماره کوتاژ با هایلایت متن جستجو شده
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = "تکمیل شده",
                                    tint = QuotasAccent,
                                    modifier = Modifier.size(12.dp)
                                )

                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            if (searchQuery.isNotEmpty() && quota.loadingQuotaNumber.contains(searchQuery, ignoreCase = true)) {
                                val parts = quota.loadingQuotaNumber.split(
                                    searchQuery,
                                    ignoreCase = true
                                )
                                Row {
                                    Text(
                                        text = "کوتاژ: ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    for (i in parts.indices) {
                                        if (i > 0) {
                                            Text(
                                                text = searchQuery,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(
                                            text = parts[i],
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "کوتاژ: ${quota.loadingQuotaNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // آمار حواله‌ها
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    // نمایش برچسب وضعیت
                    if (isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = QuotasAccentBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "تکمیل شده",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = QuotasAccent
                                )
                            }
                        }
                    } else if (remainingVouchers > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "$remainingVouchers مانده",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // آمار حواله‌های خروج شده و کل
                    Text(
                        text = "${quota.exitVouchers}/$totalVouchers حواله",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCompleted)
                            QuotasAccent
                        else
                            QuotasMutedText
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // وزن خالص خروج شده (تناژ خروجی)
                    Text(
                        text = "${formatNumber(quota.totalNetWeight)} kg",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted)
                            QuotasAccent
                        else
                            QuotasMutedText.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ردیف دوم: انبار و نوار پیشرفت
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // آیکون انبار
                Icon(
                    imageVector = Icons.Default.Warehouse,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // نام انبار با هایلایت متن جستجو شده
                if (searchQuery.isNotEmpty() && quota.loadingWarehouse.contains(searchQuery, ignoreCase = true)) {
                    val parts = quota.loadingWarehouse.split(
                        searchQuery,
                        ignoreCase = true
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        for (i in parts.indices) {
                            if (i > 0) {
                                Text(
                                    text = searchQuery,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = parts[i],
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Text(
                        text = quota.loadingWarehouse,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // نوار پیشرفت با درصد
                if (totalVouchers > 0) {
                    val progressPercentage = (quota.exitVouchers.toFloat() / totalVouchers) * 100f

                    Text(
                        text = "${progressPercentage.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCompleted)
                            QuotasAccent
                        else if (remainingVouchers > 0)
                            MaterialTheme.colorScheme.error
                        else
                            QuotasMutedText,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // نوار پیشرفت با حالت گرادیانت
            if (totalVouchers > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(QuotasMutedBg)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = quota.exitVouchers.toFloat() / totalVouchers)
                            .background(
                                brush = if (isCompleted) {
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            QuotasAccent.copy(alpha = 0.7f),
                                            QuotasAccent
                                        )
                                    )
                                } else if (remainingVouchers > 0) {
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            MaterialTheme.colorScheme.error
                                        )
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            )
                    )
                }
            }
        }
    }
}
