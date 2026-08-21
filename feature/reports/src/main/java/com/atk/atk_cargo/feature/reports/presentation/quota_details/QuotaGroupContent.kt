package com.atk.atk_cargo.feature.reports.presentation.quota_details

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.atk.atk_cargo.data.model.Quota
import com.atk.atk_cargo.data.model.QuotaEditData
import com.atk.atk_cargo.data.model.QuotaPercentageData
import com.atk.atk_cargo.data.model.WarehouseQuotaGroupingMode
import com.atk.atk_cargo.feature.reports.domain.buildQuotasShareText
import com.atk.atk_cargo.feature.reports.domain.formatNumber
import com.atk.atk_cargo.feature.reports.domain.shareQuotasData
import com.atk.atk_cargo.ui.theme.CornerL

// پنل گسترش‌پذیر گروه کوتاژها و سلکتور حالت گروه‌بندی، منتقل‌شده از QuotasListScreen.kt (شکستن God Composable)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuotaGroupExpansionPanel(
    groupName: String,
    quotas: List<Quota>,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onEdit: (String, QuotaEditData) -> Unit,
    onToggleStatus: (Int, String) -> Unit,
    onDelete: (Quota) -> Unit,
    onPercentageChange: (QuotaPercentageData) -> Unit,
    shipName: String = "",
    isMinimalMode: Boolean = false
) {
    val context = LocalContext.current
    val loadedWeight = quotas.sumOf { it.loadedTonnage.toDouble() }
    val totalWeight = quotas.sumOf { it.totalTonnage.toDouble() }
    val remainingWeight = totalWeight - loadedWeight
    var expandedQuotaId by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .combinedClickable(
                onClick = onExpandToggle,
                onLongClick = {
                    val singleGroupMap = LinkedHashMap<String?, List<Quota>>().apply {
                        put(groupName, quotas)
                    }
                    val shareText = buildQuotasShareText(singleGroupMap, shipName, includeVoucherCount = true)
                    shareQuotasData(context, shareText)
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, QuotaTealAccent.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(16.dp)
                            .scale(scaleX = 1f, scaleY = if (isExpanded) -1f else 1f)
                    )
                    Text(
                        text = groupName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "کل:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatNumber(totalWeight.toInt()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(9.dp),
                    color = QuotaDeepOrangeAccentBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "بارگیری:",
                            style = MaterialTheme.typography.labelSmall,
                            color = QuotaDeepOrangeAccent.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "↑ ${formatNumber(loadedWeight.toInt())}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = QuotaDeepOrangeAccent
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(9.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مانده:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "↓ ${formatNumber(remainingWeight.toInt())}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))

                if (isMinimalMode) {
                    quotas.chunked(2).forEach { rowQuotas ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowQuotas.forEach { quota ->
                                MinimalQuotaCard(
                                    quota = quota,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowQuotas.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    quotas.forEach { quota ->
                        QuotaCard(
                            quota = quota,
                            isExpanded = expandedQuotaId == quota.id,
                            onExpandToggle = { isExpand ->
                                expandedQuotaId = if (isExpand) quota.id else null
                            },
                            onEdit = onEdit,
                            onToggleStatus = onToggleStatus,
                            onDelete = onDelete,
                            onPercentageChange = onPercentageChange
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun GroupingModeSelector(
    currentMode: WarehouseQuotaGroupingMode,
    onModeChange: (WarehouseQuotaGroupingMode) -> Unit,
    onModeLongClick: (WarehouseQuotaGroupingMode) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GroupingModeButton(
            text = "باربری",
            icon = Icons.Default.LocalShipping,
            isSelected = currentMode == WarehouseQuotaGroupingMode.BY_SHIPPING_COMPANY,
            onClick = { onModeChange(WarehouseQuotaGroupingMode.BY_SHIPPING_COMPANY) },
            onLongClick = { onModeLongClick(WarehouseQuotaGroupingMode.BY_SHIPPING_COMPANY) }
        )
        GroupingModeButton(
            text = "صاحب کالا",
            icon = Icons.Default.Person,
            isSelected = currentMode == WarehouseQuotaGroupingMode.BY_CARGO_OWNER,
            onClick = { onModeChange(WarehouseQuotaGroupingMode.BY_CARGO_OWNER) },
            onLongClick = { onModeLongClick(WarehouseQuotaGroupingMode.BY_CARGO_OWNER) }
        )
        GroupingModeButton(
            text = "انبار",
            icon = Icons.Default.Warehouse,
            isSelected = currentMode == WarehouseQuotaGroupingMode.BY_WAREHOUSE,
            onClick = { onModeChange(WarehouseQuotaGroupingMode.BY_WAREHOUSE) },
            onLongClick = { onModeLongClick(WarehouseQuotaGroupingMode.BY_WAREHOUSE) }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroupingModeButton(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) QuotaTealAccentBg else Color.Transparent
    val contentColor = if (isSelected) QuotaTealAccent else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isSelected) QuotaTealAccent.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Surface(
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(CornerL),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
