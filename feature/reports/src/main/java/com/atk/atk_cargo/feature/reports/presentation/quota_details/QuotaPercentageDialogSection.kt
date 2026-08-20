package com.atk.atk_cargo.feature.reports.presentation.quota_details

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atk.atk_cargo.data.model.CalculationResult
import com.atk.atk_cargo.data.model.Quota
import com.atk.atk_cargo.data.model.QuotaPercentageData
import com.atk.atk_cargo.feature.reports.domain.formatNumber
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

// این فایل دیالوگ «تنظیم درصد کوتاژ» را از QuotasListScreen.kt جدا نگه می‌دارد (A1-6، بازسازی ساختاری).
// وابسته به قطعات internal تعریف‌شده در QuotasListScreen.kt (پالت رنگ Quota*, QuotaDialogHeader,
// calculateValues) که چون هم‌پکیج هستند نیازی به import ندارند.

@Composable
fun QuotaPercentageDialog(
    quota: Quota,
    onDismiss: () -> Unit,
    onConfirm: (QuotaPercentageData) -> Unit
) {
    var percentage by remember { mutableDoubleStateOf(quota.percentage ?: 0.0) }
    var calculatedValues by remember { mutableStateOf(calculateValues(quota.totalTonnage, percentage, quota.remainingTonnage)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .heightIn(max = 600.dp)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                DialogHeader(quota)

                Spacer(modifier = Modifier.height(24.dp))

                PercentageInputTab(
                    percentage = percentage,
                    calculatedValues = calculatedValues,
                    onPercentageChange = { newPercentage ->
                        if (newPercentage in 0.0..2.0) {
                            percentage = newPercentage
                            calculatedValues = calculateValues(
                                quota.totalTonnage,
                                newPercentage,
                                quota.remainingTonnage
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            val quotaData = QuotaPercentageData(
                                id = quota.id ?: 0,
                                quotaNumber = quota.number,
                                percentage = percentage,
                                calculations = calculatedValues,
                                isEnabled = if (percentage > 0.00) 1 else 0
                            )
                            onConfirm(quotaData)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = QuotaTealAccent)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text("تایید و ذخیره")
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text("انصراف")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogHeader(quota: Quota) {
    QuotaDialogHeader(
        icon = Icons.Default.AddTask,
        title = "تنظیم درصد کوتاژ",
        subtitle = "شماره کوتاژ: ${quota.number}"
    )
}

private fun lerp(start: Int, end: Int, fraction: Float): Int {
    return (start + (end - start) * fraction).roundToInt()
}

@Composable
private fun PercentageInputTab(
    percentage: Double,
    calculatedValues: CalculationResult,
    onPercentageChange: (Double) -> Unit
) {
    var isFineMode by remember { mutableStateOf(true) }
    val adjustmentStep = if (isFineMode) 0.01 else 0.10

    Column(modifier = Modifier.fillMaxWidth()) {
        val progress = percentage / 2.0
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(QuotaTealAccent.copy(alpha = 0.12f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.toFloat())
                    .fillMaxHeight()
                    .background(QuotaTealAccent)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PercentModePill(
                text = "سریع (0.1%)",
                isSelected = !isFineMode,
                onClick = { isFineMode = false },
                modifier = Modifier.weight(1f)
            )
            PercentModePill(
                text = "دقیق (0.01%)",
                isSelected = isFineMode,
                onClick = { isFineMode = true },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PercentStepButton(
                onClick = { onPercentageChange(percentage - adjustmentStep) },
                enabled = percentage > 0.00,
                icon = Icons.Default.Remove
            )

            PercentageDisplay(percentage)

            PercentStepButton(
                onClick = { onPercentageChange(percentage + adjustmentStep) },
                enabled = percentage < 2.00,
                icon = Icons.Default.Add
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        QuickSelectButtons(
            currentPercentage = percentage,
            onPercentageSelected = onPercentageChange
        )

        Spacer(modifier = Modifier.height(36.dp))

        ResultsPreview(calculatedValues)
    }
}

@Composable
private fun PercentModePill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(11.dp),
        color = if (isSelected) QuotaTealAccent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) QuotaOnTealAccent else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 9.dp)
        )
    }
}

// این تابع قبلاً «IconButton» نام داشت (در QuotasListScreen.kt)؛ چون
// androidx.compose.material3.IconButton هم استفاده می‌شود، برای جلوگیری از تداخل
// نام هنگام انتقال به این فایل، به PercentStepButton تغییر نام یافت.
@Composable
private fun PercentStepButton(
    onClick: () -> Unit,
    enabled: Boolean,
    icon: ImageVector
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        label = ""
    )

    Box(
        modifier = Modifier
            .size(46.dp)
            .scale(scale)
            .background(
                color = if (enabled) QuotaTealAccentBg else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                shape = CircleShape
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) QuotaTealAccent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun PercentageDisplay(percentage: Double) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = QuotaTealAccentBg,
        modifier = Modifier.width(120.dp)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "%.2f%%".format(percentage),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = QuotaTealAccent
            )
        }
    }
}

@Composable
private fun QuickSelectButtons(
    currentPercentage: Double,
    onPercentageSelected: (Double) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        listOf(1.50, 1.00, 0.70, 0.50, 0.00).forEach { value ->
            QuickSelectButton(
                value = value,
                isSelected = currentPercentage == value,
                onClick = { onPercentageSelected(value) }
            )
        }
    }
}

@Composable
private fun RowScope.QuickSelectButton(
    value: Double,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) QuotaTealAccent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.weight(1f)
    ) {
        Text(
            text = "%.2f%%".format(value),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) QuotaOnTealAccent else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ResultsPreview(calculatedValues: CalculationResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(13.dp)
            )
            .padding(16.dp)
    ) {
        ResultRow(
            label = "تناژ درصد",
            value = formatNumber(calculatedValues.percentageAmount.toInt())
        )
        ResultRow(
            label = "مانده درصد",
            value = formatNumber(calculatedValues.remainingAfterPercentage.toInt())
        )
        ResultRow(
            label = "مانده کل درصد",
            value = formatNumber(calculatedValues.totalRemainingAfterPercentage.toInt())
        )
    }
}

@Composable
private fun ResultRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val weightValue = value.replace(",", "").toFloatOrNull() ?: 0f

        val (displayValue, suffix) = when {
            weightValue >= 1_000_000 -> {
                val thousandTons = (weightValue / 1_000).toInt()
                thousandTons to "هزار تن"
            }
            weightValue >= 1_000 -> {
                val tons = (weightValue).toInt()
                tons to "تن"
            }
            else -> {
                weightValue.toInt() to "کیلو"
            }
        }

        AnimatedNumber(
            targetValue = displayValue,
            suffix = suffix
        )
    }
}

@Composable
fun AnimatedNumber(
    targetValue: Int,
    suffix: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    var previousValue by remember { mutableIntStateOf(0) }
    var displayValue by remember { mutableIntStateOf(0) }

    LaunchedEffect(targetValue) {
        val startValue = previousValue
        previousValue = targetValue

        (0..100).forEach { step ->
            val progress = step / 100f
            displayValue = lerp(startValue, targetValue, progress)
            delay(5.milliseconds)
        }
        displayValue = targetValue
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatNumber(displayValue),
            style = style,
            fontWeight = fontWeight,
            color = color
        )
        Text(
            text = suffix,
            style = style,
            color = color.copy(alpha = 0.7f)
        )
    }
}
