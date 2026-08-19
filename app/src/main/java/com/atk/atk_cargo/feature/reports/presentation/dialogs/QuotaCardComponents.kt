package com.atk.atk_cargo.feature.reports.presentation.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.atk.atk_cargo.api.QuotaItem
import com.atk.atk_cargo.feature.reports.domain.formatNumber
import com.atk.atk_cargo.ui.viewmodel.ReportsViewModel
import kotlin.math.roundToInt

// کارت‌های تکی «کوتاژ» و «کشتی» و اجزای اختصاصی‌شان — از
// QuotaManagementDialog.kt به این فایل منتقل شد (DEEP_CODE_AUDIT.md
// #Phase3.7، شکستن God Composable). رنگ‌های Quota* در QuotaManagementDialog.kt
// هستند؛ هم‌پکیج است، نیازی به import اضافه نیست.

@Composable
fun IntegratedQuotaCard(
    quota: QuotaItem,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onRefreshData: () -> Unit,
    viewModel: ReportsViewModel,
    modifier: Modifier = Modifier
) {
    var isUpdating by remember { mutableStateOf(false) }
    var isStatusToggling by remember { mutableStateOf(false) }
    var tempTonnageEnabled by remember { mutableStateOf(quota.temporaryTonnageEnabled) }
    var tempTonnageValue by remember { mutableStateOf(quota.temporaryTonnageValue?.toString() ?: "") }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutSlowInEasing
                )
            ),
        shape = RoundedCornerShape(12.dp),
        color = QuotaMutedBg,
        border = BorderStroke(1.dp, QuotaCardBorder)
    ) {
        Column {
            Surface(
                onClick = onToggleExpand,
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = if (quota.isActive) QuotaAccentBg else MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(9.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = if (quota.isActive) QuotaAccent else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "کوتاژ ${quota.number}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = QuotaTitleColor
                                )

                                CompactStatChip(
                                    icon = Icons.Default.Warehouse,
                                    value = quota.warehouse,
                                    color = QuotaAccent
                                )
                            }

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
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = QuotaMutedText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = quota.cargoOwner,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = QuotaMutedText,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Switch(
                                        checked = quota.isActive,
                                        onCheckedChange = {
                                            isStatusToggling = true
                                            viewModel.toggleQuotaStatus(quota.id ?: 0, quota.number) {
                                                isStatusToggling = false
                                                onRefreshData()
                                            }
                                        },
                                        enabled = !isStatusToggling
                                    )

                                    val temporaryTonnageValue = quota.temporaryTonnageValue
                                    if (quota.temporaryTonnageEnabled && temporaryTonnageValue != null) {
                                        CompactStatChip(
                                            icon = Icons.Default.Scale,
                                            value = "${formatNumber(temporaryTonnageValue.roundToInt())} تن",
                                            color = QuotaAccent
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "بستن" else "گسترش",
                        tint = QuotaMutedText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(200)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(200)) + fadeOut()
            ) {
                TempTonnageSection(
                    quota = quota,
                    tempTonnageEnabled = tempTonnageEnabled,
                    tempTonnageValue = tempTonnageValue,
                    isUpdating = isUpdating,
                    isStatusToggling = isStatusToggling,
                    onTempTonnageEnabledChange = { enabled ->
                        tempTonnageEnabled = enabled
                        if (!enabled) {
                            isUpdating = true
                            viewModel.updateTemporaryTonnage(
                                quotaNumber = quota.number,
                                enabled = false,
                                tonnage = null
                            ) {
                                isUpdating = false
                                onRefreshData()
                            }
                        }
                    },
                    onTempTonnageValueChange = { newValue ->
                        if (newValue.all { char -> char.isDigit() || char == '.' }) {
                            tempTonnageValue = newValue
                        }
                    },
                    onUpdateTonnage = {
                        isUpdating = true
                        viewModel.updateTemporaryTonnage(
                            quotaNumber = quota.number,
                            enabled = tempTonnageEnabled,
                            tonnage = if (tempTonnageEnabled && tempTonnageValue.isNotEmpty()) {
                                tempTonnageValue.toDoubleOrNull()
                            } else null
                        ) {
                            isUpdating = false
                            onRefreshData()
                        }
                    },
                    onStatusToggle = {
                        isStatusToggling = true
                        viewModel.toggleQuotaStatus(quota.id ?: 0, quota.number) {
                            isStatusToggling = false
                            onRefreshData()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CompactStatChip(
    icon: ImageVector,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TempTonnageSection(
    quota: QuotaItem,
    tempTonnageEnabled: Boolean,
    tempTonnageValue: String,
    isUpdating: Boolean,
    isStatusToggling: Boolean,
    onTempTonnageEnabledChange: (Boolean) -> Unit,
    onTempTonnageValueChange: (String) -> Unit,
    onUpdateTonnage: () -> Unit,
    onStatusToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoChip(
                icon = Icons.Default.LocalShipping,
                value = quota.shippingCompany
            )

            StatusButton(
                isActive = quota.isActive,
                isLoading = isStatusToggling,
                onToggle = onStatusToggle
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = QuotaAccentBg.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, QuotaAccentBorder)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Scale,
                            contentDescription = null,
                            tint = QuotaAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "تناژ موقت",
                            style = MaterialTheme.typography.titleSmall,
                            color = QuotaAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Switch(
                        checked = tempTonnageEnabled,
                        onCheckedChange = onTempTonnageEnabledChange,
                        enabled = !isUpdating
                    )
                }

                AnimatedVisibility(
                    visible = tempTonnageEnabled,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = tempTonnageValue,
                            onValueChange = { newValue ->
                                val filteredValue = newValue.filter { it.isDigit() }
                                onTempTonnageValueChange(filteredValue)
                            },
                            modifier = Modifier.weight(1f),
                            label = {
                                Text(
                                    "مقدار (کیلوگرم)",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            enabled = !isUpdating,
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = QuotaAccent,
                                focusedLabelColor = QuotaAccent,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        if (isUpdating) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(color = QuotaAccentBg, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = QuotaAccent
                                )
                            }
                        } else {
                            FilledIconButton(
                                onClick = onUpdateTonnage,
                                enabled = tempTonnageValue.isNotEmpty(),
                                modifier = Modifier.size(48.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (tempTonnageValue.isNotEmpty())
                                        QuotaAccent
                                    else QuotaAccent.copy(alpha = 0.3f),
                                    contentColor = QuotaOnAccent
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "ذخیره تناژ",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoChip(
    icon: ImageVector,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StatusButton(
    isActive: Boolean,
    isLoading: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val containerColor = if (isActive) {
        (if (isDarkTheme) Color(0xFF4ADE80) else Color(0xFF4CAF50)).copy(alpha = if (isDarkTheme) 0.2f else 0.15f)
    } else {
        (if (isDarkTheme) Color(0xFFF87171) else Color(0xFFF44336)).copy(alpha = if (isDarkTheme) 0.2f else 0.15f)
    }
    val contentColor = if (isActive) {
        if (isDarkTheme) Color(0xFF86EFAC) else Color(0xFF2E7D32)
    } else {
        if (isDarkTheme) Color(0xFFFCA5A5) else Color(0xFFC62828)
    }

    if (isLoading) {
        Box(
            modifier = modifier
                .background(
                    color = containerColor,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = contentColor
            )
        }
    } else {
        FilledTonalButton(
            onClick = onToggle,
            modifier = modifier,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isActive) "فعال" else "غیرفعال",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun QuotaShipExpansionPanel(
    shipName: String,
    cargoOwners: Map<String, List<QuotaItem>>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    expandedQuota: String?,
    onQuotaToggle: (String) -> Unit,
    onRefreshData: () -> Unit,
    viewModel: ReportsViewModel,
    allQuotasInactive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val totalQuotas = cargoOwners.values.sumOf { it.size }
    val activeQuotas = cargoOwners.values.flatten().count { it.isActive }
    val totalWeight = cargoOwners.values.flatten().map { it.temporaryTonnageValue ?: 0.0f }.sum()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutSlowInEasing
                )
            ),
        shape = RoundedCornerShape(14.dp),
        color = if (allQuotasInactive) {
            if (isSystemInDarkTheme()) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f) else Color(0xFFFDF1EF)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (allQuotasInactive) {
                if (isSystemInDarkTheme()) MaterialTheme.colorScheme.error.copy(alpha = 0.4f) else Color(0xFFF0B9AE)
            } else {
                QuotaCardBorder
            }
        )
    ) {
        Column {
            Surface(
                onClick = onToggleExpand,
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(QuotaAccentBg, RoundedCornerShape(9.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBoat,
                                contentDescription = null,
                                tint = QuotaAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column {
                            Text(
                                text = shipName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = QuotaTitleColor
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AnalyticsStatChipMini(
                                    value = "$totalQuotas",
                                    label = "کوتاژ"
                                )
                                AnalyticsStatChipMini(
                                    value = "$activeQuotas",
                                    label = "فعال"
                                )
                                if (totalWeight > 0) {
                                    AnalyticsStatChipMini(
                                        value = formatNumber(totalWeight.roundToInt()),
                                        label = "تن"
                                    )
                                }
                            }
                        }
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "بستن" else "گسترش",
                        tint = QuotaMutedText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val allQuotas = cargoOwners.values.flatten()
                        .sortedWith(
                            compareBy<QuotaItem> { it.cargoOwner }
                                .thenByDescending { it.isActive }
                                .thenBy { it.number }
                        )

                    allQuotas.forEach { quota ->
                        IntegratedQuotaCard(
                            quota = quota,
                            isExpanded = expandedQuota == quota.quotaKey,
                            onToggleExpand = { onQuotaToggle(quota.quotaKey) },
                            onRefreshData = onRefreshData,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsStatChipMini(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = QuotaAccentBg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = QuotaAccent,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = QuotaAccent
            )
        }
    }
}
