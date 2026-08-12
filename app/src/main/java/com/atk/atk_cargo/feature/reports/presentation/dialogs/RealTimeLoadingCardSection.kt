package com.atk.atk_cargo.feature.reports.presentation.dialogs

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atk.atk_cargo.api.RealTimeLoadingData
import com.atk.atk_cargo.feature.reports.domain.formatNumber
import com.atk.atk_cargo.ui.theme.Green300
import com.atk.atk_cargo.ui.theme.Green50
import com.atk.atk_cargo.ui.theme.Green700
import com.atk.atk_cargo.ui.theme.Red400
import com.atk.atk_cargo.ui.theme.Red50
import com.atk.atk_cargo.ui.theme.Red700

// این فایل کارت‌های اطلاعاتی (RealTimeLoadingCard, CompactInfo, StatisticItem) را از
// RealTimeLoadingBottomSheet.kt جدا نگه می‌دارد (A1-6، بازسازی ساختاری). وابسته به پالت
// رنگ internal تعریف‌شده در RealTimeLoadingBottomSheet.kt (RealTime*) که چون هم‌پکیج
// است نیازی به import ندارد.

@Composable
fun RealTimeLoadingCard(
    data: RealTimeLoadingData,
    showEntry: Boolean = true
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(14.dp),
        color = RealTimeMutedBg,
        border = BorderStroke(1.dp, RealTimeCardBorder),
    ) {
        val verticalLineColor = RealTimeAccent
        // Path به‌جای ساخته‌شدن در هر draw pass، یک‌بار با remember نگه داشته
        // می‌شود و در drawBehind فقط reset+بازسازی می‌شود (P-4: کاهش GC churn
        // با چند ده کارت هم‌زمان + animateContentSize).
        val verticalLinePath = remember { Path() }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val lineWidth = 4.dp.toPx()
                    val cornerRadius = 4.dp.toPx()
                    val x = size.width - lineWidth

                    verticalLinePath.reset()
                    verticalLinePath.apply {
                        moveTo(x, 0f)
                        lineTo(x + lineWidth - cornerRadius, 0f)
                        arcTo(
                            rect = androidx.compose.ui.geometry.Rect(
                                offset = Offset(x + lineWidth - cornerRadius * 2, 0f),
                                size = androidx.compose.ui.geometry.Size(cornerRadius * 2, cornerRadius * 2)
                            ),
                            startAngleDegrees = 90f,
                            sweepAngleDegrees = -90f,
                            forceMoveTo = false
                        )
                        lineTo(x + lineWidth, size.height - cornerRadius)
                        arcTo(
                            rect = androidx.compose.ui.geometry.Rect(
                                offset = Offset(x + lineWidth - cornerRadius * 2, size.height - cornerRadius * 2),
                                size = androidx.compose.ui.geometry.Size(cornerRadius * 2, cornerRadius * 2)
                            ),
                            startAngleDegrees = 0f,
                            sweepAngleDegrees = -90f,
                            forceMoveTo = false
                        )
                        lineTo(x, size.height)
                        close()
                    }
                    drawPath(verticalLinePath, verticalLineColor)
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "شماره کوتاژ",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = RealTimeMutedText
                    )

                    // C-4: شرکت باربری قبلاً اینجا (کنار آیکون Person) و هم در ردیف
                    // آماری پایین («شرکت باربری») نمایش داده می‌شد؛ نسخه‌ی هدر حذف شد
                    // چون ردیف پایین با برچسب صریح خواناتر و کامل‌تر است.
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, RealTimeCardBorder),
                    ) {
                        Text(
                            text = data.loadingQuotaNumber,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = RealTimeTitleColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            letterSpacing = 1.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val dashLineColor = RealTimeCardBorder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .drawBehind {
                                val pathEffect =
                                    PathEffect.dashPathEffect(floatArrayOf(10f, 5f), 0f)
                                drawLine(
                                    color = dashLineColor,
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = pathEffect
                                )
                            }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "شرکت باربری",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                text = data.shippingCompany,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        VerticalDivider(
                            modifier = Modifier.height(40.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "وزن خالص",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                text = formatNumber(data.totalNetWeight),
                                style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.Ltr),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        VerticalDivider(
                            modifier = Modifier.height(40.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                // ترتیب برچسب باید با ترتیب مقادیر زیرش (که به‌دلیل
                                // textDirection = Ltr همیشه خروج/ورود چاپ می‌شود) یکی باشد.
                                text = if (showEntry) "خروج/ورود" else "خروجی",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                text = if (showEntry) "${data.exitVouchers} / ${data.entryVouchers}" else formatNumber(data.exitVouchers),
                                style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.Ltr),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactInfo(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color.copy(alpha = 0.07f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StatisticItem(
    totalEntryVouchers: Int,
    totalExitVouchers: Int,
    totalNetWeight: Float,
    showEntry: Boolean = true
) {
    val isDarkTheme = isSystemInDarkTheme()
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    val animatedTotalWeight by animateFloatAsState(
        targetValue = if (startAnimation) totalNetWeight else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "weight animation"
    )
    val animatedEntryVouchers by animateFloatAsState(
        targetValue = if (startAnimation) totalEntryVouchers.toFloat() else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "entry animation"
    )
    val animatedExitVouchers by animateFloatAsState(
        targetValue = if (startAnimation) totalExitVouchers.toFloat() else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "exit animation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        color = RealTimeMutedBg,
        border = BorderStroke(1.dp, RealTimeCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Scale,
                        contentDescription = null,
                        tint = RealTimeMutedText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "وزن کل",
                        style = MaterialTheme.typography.labelMedium,
                        color = RealTimeMutedText,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "kg",
                            style = MaterialTheme.typography.labelSmall,
                            color = RealTimeMutedText,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = formatNumber(animatedTotalWeight.toInt()),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showEntry) {
                    val totalVouchers = animatedEntryVouchers.toInt() + animatedExitVouchers.toInt()
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = RealTimeAccentBg,
                        border = BorderStroke(1.dp, RealTimeAccentBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = formatNumber(totalVouchers),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RealTimeAccent
                                )
                                Icon(
                                    imageVector = Icons.Default.AllInbox,
                                    contentDescription = null,
                                    tint = RealTimeAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = "کل",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Medium,
                                color = RealTimeAccent.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDarkTheme) Red400.copy(alpha = 0.15f) else Red50,
                        border = BorderStroke(1.dp, if (isDarkTheme) Red400.copy(alpha = 0.3f) else Red400.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = formatNumber(animatedEntryVouchers.toInt()),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkTheme) Red400 else Red700
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = if (isDarkTheme) Red400 else Red700,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = "ورودی",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Medium,
                                color = if (isDarkTheme) Red400.copy(alpha = 0.7f) else Red700.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDarkTheme) Green300.copy(alpha = 0.15f) else Green50,
                    border = BorderStroke(1.dp, if (isDarkTheme) Green300.copy(alpha = 0.3f) else Green300.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = formatNumber(animatedExitVouchers.toInt()),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) Green300 else Green700
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (isDarkTheme) Green300 else Green700,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = "خروجی",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Medium,
                            color = if (isDarkTheme) Green300.copy(alpha = 0.7f) else Green700.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
