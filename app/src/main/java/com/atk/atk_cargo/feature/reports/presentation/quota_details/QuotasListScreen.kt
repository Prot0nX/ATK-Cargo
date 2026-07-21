package com.atk.atk_cargo.feature.reports.presentation.quota_details

import com.atk.atk_cargo.feature.reports.presentation.quota_details.components.DeleteQuotaDialog
import com.atk.atk_cargo.feature.reports.presentation.quota_details.components.ToggleQuotaStatusDialog

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atk.atk_cargo.api.CalculationResult
import com.atk.atk_cargo.api.GroupSortingMode
import com.atk.atk_cargo.api.Quota
import com.atk.atk_cargo.api.QuotaEditData
import com.atk.atk_cargo.api.QuotaPercentageData
import com.atk.atk_cargo.api.QuotaSortingMode
import com.atk.atk_cargo.api.WarehouseQuotaGroupingMode
import com.atk.atk_cargo.feature.reports.domain.buildQuotasShareText
import com.atk.atk_cargo.feature.reports.domain.calculateProgress
import com.atk.atk_cargo.feature.reports.domain.formatNumber
import com.atk.atk_cargo.feature.reports.domain.formatWeightWithDetail
import com.atk.atk_cargo.feature.reports.domain.shareQuotasData
import com.atk.atk_cargo.ui.theme.Blue400
import com.atk.atk_cargo.ui.theme.Blue700
import com.atk.atk_cargo.ui.theme.Corner2XL
import com.atk.atk_cargo.ui.theme.CornerL
import com.atk.atk_cargo.ui.theme.CornerXL
import com.atk.atk_cargo.ui.theme.Green600
import com.atk.atk_cargo.ui.theme.Green700
import com.atk.atk_cargo.ui.theme.PrimaryBlueLight
import com.atk.atk_cargo.ui.theme.Purple700
import com.atk.atk_cargo.ui.theme.Red500
import com.atk.atk_cargo.ui.theme.Red900
import com.atk.atk_cargo.ui.viewmodel.ReportsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun QuotasList(
    quotas: List<Quota>,
    searchQuery: String,
    groupingMode: StateFlow<WarehouseQuotaGroupingMode>,
    onGroupingModeChange: (WarehouseQuotaGroupingMode) -> Unit,
    onEdit: (String, QuotaEditData) -> Unit,
    onToggleStatus: (Int, String) -> Unit,
    onDelete: (Quota) -> Unit,
    viewModel: ReportsViewModel
) {
    val currentGroupingMode by groupingMode.collectAsState()
    val currentSortingMode by viewModel.quotaSortingMode.collectAsState()
    val currentGroupSortingMode by viewModel.groupSortingMode.collectAsState()
    val isMinimalMode by viewModel.isMinimalQuotaMode.collectAsState()
    var expandedGroup by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        GroupingModeSelector(
            currentMode = currentGroupingMode,
            onModeChange = onGroupingModeChange,
            onModeLongClick = { mode ->
                if (mode == WarehouseQuotaGroupingMode.BY_CARGO_OWNER) {
                    viewModel.toggleMinimalQuotaMode()
                }
            }
        )

        var shareGroupedQuotas by remember { mutableStateOf<LinkedHashMap<String?, List<Quota>>?>(null) }
        val context = LocalContext.current
        val isDarkTheme = isSystemInDarkTheme()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                onClick = {
                    shareGroupedQuotas?.let { quotas ->
                        val shipName = viewModel.selectedShip.value?.name ?: ""
                        val shareText = buildQuotasShareText(quotas, shipName)
                        shareQuotasData(context, shareText)
                    }
                },
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(CornerXL),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "اشتراک‌گذاری اطلاعات",
                        tint = if (isDarkTheme) PrimaryBlueLight else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            val isGroupSortingSelected = currentGroupSortingMode == GroupSortingMode.REMAINING_TONNAGE_ASC || currentGroupSortingMode == GroupSortingMode.REMAINING_TONNAGE_DESC
            val groupSortingIcon = if (currentGroupSortingMode == GroupSortingMode.REMAINING_TONNAGE_ASC) {
                Icons.Default.ArrowUpward
            } else {
                Icons.Default.ArrowDownward
            }
            Surface(
                modifier = Modifier.weight(1f),
                onClick = {
                    val newMode = if (currentGroupSortingMode == GroupSortingMode.REMAINING_TONNAGE_ASC) {
                        GroupSortingMode.REMAINING_TONNAGE_DESC
                    } else {
                        GroupSortingMode.REMAINING_TONNAGE_ASC
                    }
                    viewModel.setGroupSortingMode(newMode)
                },
                shape = RoundedCornerShape(CornerXL),
                color = if (isGroupSortingSelected) {
                    if (isDarkTheme) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
                border = if (isGroupSortingSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                tonalElevation = if (isGroupSortingSelected) 1.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مانده گروه",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isGroupSortingSelected) {
                            if (isDarkTheme) PrimaryBlueLight else MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = if (isGroupSortingSelected) FontWeight.Bold else FontWeight.Medium
                    )
                    if (isGroupSortingSelected) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = groupSortingIcon,
                            contentDescription = null,
                            tint = if (isDarkTheme) PrimaryBlueLight else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            val isQuotaSortingSelected = currentSortingMode == QuotaSortingMode.REMAINING_TONNAGE_ASC || currentSortingMode == QuotaSortingMode.REMAINING_TONNAGE_DESC
            val quotaSortingIcon = if (currentSortingMode == QuotaSortingMode.REMAINING_TONNAGE_ASC) {
                Icons.Default.ArrowUpward
            } else {
                Icons.Default.ArrowDownward
            }
            Surface(
                modifier = Modifier.weight(1f),
                onClick = {
                    val newMode = if (currentSortingMode == QuotaSortingMode.REMAINING_TONNAGE_ASC) {
                        QuotaSortingMode.REMAINING_TONNAGE_DESC
                    } else {
                        QuotaSortingMode.REMAINING_TONNAGE_ASC
                    }
                    viewModel.setQuotaSortingMode(newMode)
                },
                shape = RoundedCornerShape(CornerXL),
                color = if (isQuotaSortingSelected) {
                    if (isDarkTheme) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
                border = if (isQuotaSortingSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                tonalElevation = if (isQuotaSortingSelected) 1.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مانده کوتاژ",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isQuotaSortingSelected) {
                            if (isDarkTheme) PrimaryBlueLight else MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = if (isQuotaSortingSelected) FontWeight.Bold else FontWeight.Medium
                    )
                    if (isQuotaSortingSelected) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = quotaSortingIcon,
                            contentDescription = null,
                            tint = if (isDarkTheme) PrimaryBlueLight else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        val selectedDateRange by viewModel.selectedDateRange.collectAsState()
        selectedDateRange?.let { (startDate, endDate) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "از: ${startDate.replace(" ", " - ")} | تا: ${endDate.replace(" ", " - ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = { viewModel.clearSelectedDateRange() },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "حذف فیلتر",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        val groupedQuotas = remember(quotas, currentGroupingMode, currentSortingMode, currentGroupSortingMode, searchQuery) {
            fun getWeightInKg(tonnage: Float): Float {
                return tonnage * 1000f
            }

            fun calculateRemainingAfterPercentage(quota: Quota): Float {
                val percentageAmount = quota.totalTonnage * ((quota.percentage ?: 0.0) / 100)
                return quota.remainingTonnage - percentageAmount.toFloat()
            }

            val groupedMap = quotas
                .filter { quota ->
                    quota.number.contains(searchQuery, ignoreCase = true) ||
                            quota.shippingCompany.contains(searchQuery, ignoreCase = true) ||
                            (quota.cargoOwner?.contains(searchQuery, ignoreCase = true) == true)
                }
                .groupBy {
                    when (currentGroupingMode) {
                        WarehouseQuotaGroupingMode.BY_SHIPPING_COMPANY -> it.shippingCompany
                        WarehouseQuotaGroupingMode.BY_CARGO_OWNER -> it.cargoOwner
                        WarehouseQuotaGroupingMode.BY_WAREHOUSE -> "${it.warehouse} | ${it.cargoOwner}"
                    }
                }
                .mapValues { (groupName, groupQuotas) ->
                    val sorted = groupQuotas.sortedWith(
                        compareByDescending<Quota> { it.isActive }
                            .thenBy { quota ->
                                val remainingAfterPercentage = calculateRemainingAfterPercentage(quota)
                                val weightInKg = getWeightInKg(remainingAfterPercentage)
                                when (currentSortingMode) {
                                    QuotaSortingMode.REMAINING_TONNAGE_ASC -> weightInKg
                                    QuotaSortingMode.REMAINING_TONNAGE_DESC -> -weightInKg
                                }
                            }
                    )
                    sorted
                }

            val sortedEntries = when (currentGroupSortingMode) {
                GroupSortingMode.ALPHABETICAL -> {
                    groupedMap.entries.sortedBy { it.key }
                }
                GroupSortingMode.REMAINING_TONNAGE_ASC -> {
                    groupedMap.entries.sortedBy { (groupName, _) ->
                        val totalRemainingKg = groupedMap[groupName]?.sumOf { quota ->
                            val remainingAfterPercentage = calculateRemainingAfterPercentage(quota)
                            getWeightInKg(remainingAfterPercentage).toDouble()
                        } ?: 0.0
                        totalRemainingKg
                    }
                }
                GroupSortingMode.REMAINING_TONNAGE_DESC -> {
                    groupedMap.entries.sortedByDescending { (groupName, _) ->
                        val totalRemainingKg = groupedMap[groupName]?.sumOf { quota ->
                            val remainingAfterPercentage = calculateRemainingAfterPercentage(quota)
                            getWeightInKg(remainingAfterPercentage).toDouble()
                        } ?: 0.0
                        totalRemainingKg
                    }
                }
            }

            LinkedHashMap<String?, List<Quota>>().apply {
                sortedEntries.forEach { (key, value) ->
                    put(key, value)
                }
            }
        }.also { result ->
            result.forEach { (groupName, items) ->
                Log.d("atkcargo", "  Group '$groupName': ${items.size} items - ${items.joinToString(", ") { it.number }}")
            }
            shareGroupedQuotas = result
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = groupedQuotas.entries.toList(),
                key = { (groupName, _) -> groupName ?: "" }
            ) { (groupName, sortedQuotas) ->
                groupName?.let {
                    QuotaGroupExpansionPanel(
                        groupName = it,
                        quotas = sortedQuotas,
                        isExpanded = expandedGroup == groupName,
                        onExpandToggle = {
                            expandedGroup = if (expandedGroup == groupName) null else groupName
                        },
                        onEdit = onEdit,
                        onToggleStatus = onToggleStatus,
                        onDelete = onDelete,
                        onPercentageChange = viewModel::updateQuotaPercentage,
                        shipName = viewModel.selectedShip.value?.name ?: "",
                        isMinimalMode = isMinimalMode && currentGroupingMode == WarehouseQuotaGroupingMode.BY_CARGO_OWNER
                    )
                }
            }
        }
    }
}

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
    val isDarkTheme = isSystemInDarkTheme()

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
        shape = RoundedCornerShape(CornerXL),
        border = BorderStroke(
            1.dp,
            if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = groupName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDarkTheme) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatNumber(totalWeight.toInt()),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkTheme) PrimaryBlueLight else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Scale,
                            contentDescription = null,
                            tint = if (isDarkTheme) PrimaryBlueLight else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "بارگیری:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatNumber(loadedWeight.toInt()),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkTheme) Blue400 else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (isDarkTheme) Blue400 else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDarkTheme) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مانده: ${formatNumber(remainingWeight.toInt())}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkTheme) PrimaryBlueLight else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (isDarkTheme) PrimaryBlueLight else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
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
fun QuotaCard(
    quota: Quota,
    isExpanded: Boolean = false,
    onExpandToggle: (Boolean) -> Unit = { _ -> },
    onEdit: (String, QuotaEditData) -> Unit,
    onToggleStatus: (Int, String) -> Unit,
    onDelete: (Quota) -> Unit,
    onPercentageChange: (QuotaPercentageData) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showToggleDialog by remember { mutableStateOf(false) }
    var showPercentageDialog by remember { mutableStateOf(false) }
    val cardColor = if (quota.isActive) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    }
    val contentColor = if (quota.isActive) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    }
    val accentColor = if (quota.isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    }
    val progress = calculateProgress(quota.loadedTonnage, quota.totalTonnage)
    val isDarkTheme = isSystemInDarkTheme()
    val loadedTonnage = quota.loadedTonnage
    val remainingTonnage = quota.remainingTonnage

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandToggle(!isExpanded) },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(Corner2XL),
        border = BorderStroke(
            width = 1.dp,
            color = if (quota.isActive) {
                if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            } else {
                if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (quota.isActive) 1.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (quota.isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = if (quota.isActive) "فعال" else "غیرفعال",
                            tint = if (quota.isActive) Green600 else Red500,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = quota.number,
                            style = MaterialTheme.typography.titleMedium,
                            color = contentColor,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    val calculatedValues = calculateValues(
                        totalTonnage = quota.totalTonnage,
                        percentage = quota.percentage ?: 0.0,
                        remainingTonnage = quota.remainingTonnage
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        quota.cargoType?.let { type ->
                            Text(
                                text = type,
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = " | ",
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor.copy(alpha = 0.5f)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Scale,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${formatWeightWithDetail(calculatedValues.totalRemainingAfterPercentage.toFloat())} (%.2f%%)".format(quota.percentage ?: 0.0),
                            style = MaterialTheme.typography.bodySmall,
                            color = accentColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDarkTheme) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatNumber(quota.totalTonnage.toInt()),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkTheme) PrimaryBlueLight else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Scale,
                            contentDescription = null,
                            tint = if (isDarkTheme) PrimaryBlueLight else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "بارگیری:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatNumber(loadedTonnage.toInt()),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkTheme) Blue400 else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (isDarkTheme) Blue400 else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDarkTheme) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مانده: ${formatNumber(remainingTonnage.toInt())}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkTheme) PrimaryBlueLight else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (isDarkTheme) PrimaryBlueLight else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            if (!isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = accentColor,
                    trackColor = accentColor.copy(alpha = 0.1f)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "پیشرفت بارگیری",
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = accentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = accentColor,
                            trackColor = accentColor.copy(alpha = 0.1f)
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("تناژ کل", formatNumber(quota.totalTonnage.toInt()), accentColor)
                            VerticalDivider(
                                modifier = Modifier.height(24.dp),
                                color = contentColor.copy(alpha = 0.1f)
                            )
                            StatItem("بارگیری شده", formatNumber(quota.loadedTonnage.toInt()), accentColor)
                            VerticalDivider(
                                modifier = Modifier.height(24.dp),
                                color = contentColor.copy(alpha = 0.1f)
                            )
                            StatItem("تعداد حواله", formatNumber(quota.voucherCount), accentColor)
                        }
                    }

                    HorizontalDivider(
                        color = contentColor.copy(alpha = 0.1f)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ActionButton(
                            icon = Icons.Default.Edit,
                            label = "ویرایش",
                            color = Blue700,
                            onClick = { showEditDialog = true }
                        )
                        ActionButton(
                            icon = Icons.Default.Build,
                            label = "درصد",
                            color = Purple700,
                            onClick = { showPercentageDialog = true }
                        )
                        ActionButton(
                            icon = if (quota.isActive) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                            label = if (quota.isActive) "غیرفعال" else "فعال",
                            color = if (quota.isActive) Red500 else Green700,
                            onClick = { showToggleDialog = true }
                        )
                        ActionButton(
                            icon = Icons.Default.Delete,
                            label = "حذف",
                            color = Red900,
                            onClick = { showDeleteDialog = true }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        DeleteQuotaDialog(
            quotaNumber = quota.number,
            onConfirm = {
                onDelete(quota)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showPercentageDialog) {
        QuotaPercentageDialog(
            quota = quota,
            onDismiss = { showPercentageDialog = false },
            onConfirm = onPercentageChange
        )
    }

    if (showToggleDialog) {
        ToggleQuotaStatusDialog(
            quotaNumber = quota.number,
            isActive = quota.isActive,
            onConfirm = {
                onToggleStatus(quota.id ?: 0, quota.number)
                showToggleDialog = false
            },
            onDismiss = { showToggleDialog = false }
        )
    }

    if (showEditDialog) {
        EditQuotaDialog(
            quotaData = QuotaEditData(
                id = quota.id ?: 0,
                quotaNumber = quota.number,
                shipName = quota.shipName ?: "",
                shippingCompany = quota.shippingCompany,
                warehouse = quota.warehouse ?: "",
                cargoType = quota.cargoType ?: "",
                totalTonnage = quota.totalTonnage
            ),
            onConfirm = { editedData ->
                onEdit(quota.number, editedData)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
fun MinimalQuotaCard(
    quota: Quota,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val accentColor = if (quota.isActive) {
        if (isDarkTheme) PrimaryBlueLight else MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    }

    Card(
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(CornerL),
        border = BorderStroke(
            1.dp,
            if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = quota.number,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (quota.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = if (quota.isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (quota.isActive) Green600 else Red500,
                    modifier = Modifier.size(16.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Scale,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${formatNumber(quota.remainingTonnage.toInt())} تن",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
            }

            val progress = calculateProgress(quota.loadedTonnage, quota.totalTonnage)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = color.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun GroupingModeSelector(
    currentMode: WarehouseQuotaGroupingMode,
    onModeChange: (WarehouseQuotaGroupingMode) -> Unit,
    onModeLongClick: (WarehouseQuotaGroupingMode) -> Unit = {}
) {
    val isDarkTheme = isSystemInDarkTheme()

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
            onLongClick = { onModeLongClick(WarehouseQuotaGroupingMode.BY_SHIPPING_COMPANY) },
            isDarkTheme = isDarkTheme
        )
        GroupingModeButton(
            text = "صاحب کالا",
            icon = Icons.Default.Person,
            isSelected = currentMode == WarehouseQuotaGroupingMode.BY_CARGO_OWNER,
            onClick = { onModeChange(WarehouseQuotaGroupingMode.BY_CARGO_OWNER) },
            onLongClick = { onModeLongClick(WarehouseQuotaGroupingMode.BY_CARGO_OWNER) },
            isDarkTheme = isDarkTheme
        )
        GroupingModeButton(
            text = "انبار",
            icon = Icons.Default.Warehouse,
            isSelected = currentMode == WarehouseQuotaGroupingMode.BY_WAREHOUSE,
            onClick = { onModeChange(WarehouseQuotaGroupingMode.BY_WAREHOUSE) },
            onLongClick = { onModeLongClick(WarehouseQuotaGroupingMode.BY_WAREHOUSE) },
            isDarkTheme = isDarkTheme
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
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        if (isDarkTheme) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isSelected) {
        if (isDarkTheme) PrimaryBlueLight else MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val borderColor = if (isSelected) {
        if (isDarkTheme) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    } else {
        Color.Transparent
    }

    Surface(
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(CornerL),
        color = backgroundColor,
        border = if (borderColor != Color.Transparent) BorderStroke(1.dp, borderColor) else null,
        tonalElevation = 1.dp
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

@Composable
fun DeleteQuotaDialog(
    quotaNumber: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    com.atk.atk_cargo.feature.reports.presentation.quota_details.components.DeleteQuotaDialog(
        quotaNumber = quotaNumber,
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
    com.atk.atk_cargo.feature.reports.presentation.quota_details.components.ToggleQuotaStatusDialog(
        quotaNumber = quotaNumber,
        isActive = isActive,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditQuotaDialog(
    quotaData: QuotaEditData,
    onConfirm: (QuotaEditData) -> Unit,
    onDismiss: () -> Unit
) {
    var editedData by remember { mutableStateOf(quotaData) }
    var expanded by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    val cargoTypes = listOf("سویا", "دانه روغنی", "ذرت", "گندم", "جو")

    fun isValidQuotaNumber(number: String): Boolean {
        return number.all { it.isDigit() } && number.length in 5..10
    }

    fun formatNumberString(number: String): String {
        return number.map { char ->
            when (char) {
                '۰' -> '0'
                '۱' -> '1'
                '۲' -> '2'
                '۳' -> '3'
                '۴' -> '4'
                '۵' -> '5'
                '۶' -> '6'
                '۷' -> '7'
                '۸' -> '8'
                '۹' -> '9'
                else -> char
            }
        }.filter { it.isDigit() || it.isLetter() || it == ' ' }.joinToString("")
    }

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
                .heightIn(max = 700.dp)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                EditQuotaDialogHeader(quotaData.quotaNumber)

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = editedData.quotaNumber,
                    onValueChange = {
                        editedData = editedData.copy(quotaNumber = formatNumberString(it))
                    },
                    label = { Text("شماره کوتاژ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = !isValidQuotaNumber(editedData.quotaNumber),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
                if (!isValidQuotaNumber(editedData.quotaNumber)) {
                    Text(
                        "شماره کوتاژ باید حداقل 5 رقم باشد",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = editedData.shipName,
                    onValueChange = {
                        editedData = editedData.copy(shipName = formatNumberString(it).uppercase(Locale.ROOT))
                    },
                    label = { Text("نام کشتی") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = editedData.shippingCompany,
                    onValueChange = {
                        editedData = editedData.copy(shippingCompany = formatNumberString(it))
                    },
                    label = { Text("شرکت باربری") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = editedData.warehouse,
                    onValueChange = {
                        editedData = editedData.copy(warehouse = formatNumberString(it))
                    },
                    label = { Text("انبار") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = editedData.cargoType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("نوع کالا") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        cargoTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    editedData = editedData.copy(cargoType = type)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = formatNumberString(editedData.totalTonnage.toInt().toString()),
                    onValueChange = {
                        val newValue = formatNumberString(it).toIntOrNull() ?: editedData.totalTonnage.toInt()
                        editedData = editedData.copy(totalTonnage = newValue.toFloat())
                    },
                    label = { Text("تناژ کل") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            if (isValidQuotaNumber(editedData.quotaNumber)) {
                                showConfirmationDialog = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = editedData != quotaData && isValidQuotaNumber(editedData.quotaNumber),
                        shape = RoundedCornerShape(12.dp)
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

    if (showConfirmationDialog) {
        Dialog(
            onDismissRequest = { showConfirmationDialog = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    ConfirmationDialogHeader()

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "آیا از تغییرات انجام شده مطمئن هستید",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                onConfirm(editedData)
                                showConfirmationDialog = false
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
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
                                Text("ذخیره")
                            }
                        }

                        OutlinedButton(
                            onClick = { showConfirmationDialog = false },
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
}

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
            shape = RoundedCornerShape(24.dp),
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
                                quotaNumber = quota.number,
                                percentage = percentage,
                                calculations = calculatedValues,
                                isEnabled = if (percentage > 0.00) 1 else 0
                            )
                            onConfirm(quotaData)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                ),
                label = ""
            )

            Icon(
                imageVector = Icons.Default.AddTask,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(32.dp)
                    .scale(scale)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "تنظیم درصد کوتاژ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "شماره کوتاژ: ${quota.number}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
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
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.toFloat())
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(4.dp)
                ) {
                    Button(
                        onClick = { isFineMode = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFineMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (isFineMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "دقیق (0.01%)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Button(
                        onClick = { isFineMode = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isFineMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (!isFineMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "سریع (0.1%)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onPercentageChange(percentage - adjustmentStep) },
                enabled = percentage > 0.00,
                icon = Icons.Default.Remove
            )

            PercentageDisplay(percentage)

            IconButton(
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
private fun IconButton(
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
            .size(48.dp)
            .scale(scale)
            .background(
                color = if (enabled) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                },
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
            tint = if (enabled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            }
        )
    }
}

@Composable
private fun PercentageDisplay(percentage: Double) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        modifier = Modifier.width(120.dp)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "%.2f%%".format(percentage),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(0.00, 0.50, 0.70, 1.00, 1.50).forEach { value ->
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
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            }
        ),
        modifier = Modifier.weight(1f)
    ) {
        Text(
            text = "%.2f%%".format(value),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
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
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
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
            delay(5)
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

fun calculateValues(
    totalTonnage: Float,
    percentage: Double,
    remainingTonnage: Float
): CalculationResult {
    val percentageAmount = totalTonnage * (percentage / 100)
    val remainingAfterPercentage = totalTonnage - percentageAmount
    val totalRemainingAfterPercentage = remainingTonnage - percentageAmount

    return CalculationResult(
        percentageAmount = percentageAmount,
        remainingAfterPercentage = remainingAfterPercentage,
        totalRemainingAfterPercentage = totalRemainingAfterPercentage
    )
}

@Composable
private fun EditQuotaDialogHeader(quotaNumber: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                ),
                label = ""
            )

            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(32.dp)
                    .scale(scale)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "ویرایش اطلاعات کوتاژ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "شماره کوتاژ: $quotaNumber",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ConfirmationDialogHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                ),
                label = ""
            )

            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(32.dp)
                    .scale(scale)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "تأیید ذخیره‌سازی",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "لطفاً تصمیم خود را تأیید کنید",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
