package com.atk.atk_cargo.feature.reports.presentation.dialogs

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.atk.atk_cargo.api.QuotaItem
import com.atk.atk_cargo.ui.viewmodel.ReportsViewModel

// محتوای بدنه‌ی دیالوگ «مدیریت کوتاژها» (تب‌ها/فیلتر/مرتب‌سازی/لیست کشتی‌ها) —
// از QuotaManagementDialog.kt به این فایل منتقل شد (DEEP_CODE_AUDIT.md
// #Phase3.7، شکستن God Composable). پوسته‌ی دیالوگ (Dialog+Header) و رنگ‌های
// Quota* هم‌چنان در QuotaManagementDialog.kt هستند؛ هم‌پکیج است، نیازی به
// import اضافه نیست.

enum class SortType {
    NAME_ASC, NAME_DESC,
    QUOTA_COUNT_ASC, QUOTA_COUNT_DESC,
    TOTAL_WEIGHT_ASC, TOTAL_WEIGHT_DESC
}

data class QuotaFilters(
    val showActiveOnly: Boolean = false,
    val showInactiveOnly: Boolean = false,
    val selectedWarehouses: Set<String> = emptySet(),
    val minWeight: Float? = null,
    val maxWeight: Float? = null,
    val hasTemporaryTonnage: Boolean? = null
)

@Composable
fun QuotaManagementContent(
    quotaData: Map<String, Map<String, List<QuotaItem>>>,
    searchQuery: String,
    expandedShip: String?,
    expandedQuota: String?,
    onShipToggle: (String) -> Unit,
    onQuotaToggle: (String) -> Unit,
    onRefreshData: () -> Unit,
    viewModel: ReportsViewModel
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var sortType by remember { mutableStateOf(SortType.NAME_ASC) }
    var showSortMenu by remember { mutableStateOf(false) }
    var filters by remember { mutableStateOf(QuotaFilters()) }
    var showFiltersDialog by remember { mutableStateOf(false) }
    val filteredData = remember(quotaData, searchQuery, filters) {
        var result = quotaData

        if (searchQuery.isNotBlank()) {
            result = result.mapNotNull { (shipName, cargoOwners) ->
                val filteredCargoOwners = cargoOwners.mapNotNull { (cargoOwner, quotas) ->
                    val filteredQuotas = quotas.filter { quota ->
                        quota.number.contains(searchQuery, ignoreCase = false)
                    }
                    if (filteredQuotas.isNotEmpty()) {
                        cargoOwner to filteredQuotas
                    } else null
                }.toMap()

                if (filteredCargoOwners.isNotEmpty()) {
                    shipName to filteredCargoOwners
                } else null
            }.toMap()
        }

        result = result.mapNotNull { (shipName, cargoOwners) ->
            val filteredCargoOwners = cargoOwners.mapNotNull { (cargoOwner, quotas) ->
                val filteredQuotas = quotas.filter { quota ->
                    val statusFilter = when {
                        filters.showActiveOnly -> quota.isActive
                        filters.showInactiveOnly -> !quota.isActive
                        else -> true
                    }

                    val warehouseFilter = if (filters.selectedWarehouses.isEmpty()) {
                        true
                    } else {
                        filters.selectedWarehouses.contains(quota.warehouse)
                    }

                    val tempTonnageFilter = filters.hasTemporaryTonnage?.let { hasTemp ->
                        if (hasTemp) quota.temporaryTonnageEnabled else !quota.temporaryTonnageEnabled
                    } ?: true

                    statusFilter && warehouseFilter && tempTonnageFilter
                }

                if (filteredQuotas.isNotEmpty()) {
                    cargoOwner to filteredQuotas
                } else null
            }.toMap()

            if (filteredCargoOwners.isNotEmpty()) {
                shipName to filteredCargoOwners
            } else null
        }.toMap()

        result
    }

    val sortedData = remember(filteredData, sortType) {
        val sortedMap = filteredData.toList().sortedWith { (shipName1, cargoOwners1), (shipName2, cargoOwners2) ->
            when (sortType) {
                SortType.NAME_ASC -> shipName1.compareTo(shipName2)
                SortType.NAME_DESC -> shipName2.compareTo(shipName1)
                SortType.QUOTA_COUNT_ASC -> {
                    val count1 = cargoOwners1.values.flatten().size
                    val count2 = cargoOwners2.values.flatten().size
                    count1.compareTo(count2)
                }
                SortType.QUOTA_COUNT_DESC -> {
                    val count1 = cargoOwners1.values.flatten().size
                    val count2 = cargoOwners2.values.flatten().size
                    count2.compareTo(count1)
                }
                SortType.TOTAL_WEIGHT_ASC -> {
                    val weight1 = cargoOwners1.values.flatten().sumOf { it.temporaryTonnageValue?.toDouble() ?: 0.0 }
                    val weight2 = cargoOwners2.values.flatten().sumOf { it.temporaryTonnageValue?.toDouble() ?: 0.0 }
                    weight1.compareTo(weight2)
                }
                SortType.TOTAL_WEIGHT_DESC -> {
                    val weight1 = cargoOwners1.values.flatten().sumOf { it.temporaryTonnageValue?.toDouble() ?: 0.0 }
                    val weight2 = cargoOwners2.values.flatten().sumOf { it.temporaryTonnageValue?.toDouble() ?: 0.0 }
                    weight2.compareTo(weight1)
                }
            }
        }.toMap()
        sortedMap
    }

    val (activeShipsData, inactiveShipsData) = remember(sortedData) {
        val active = mutableMapOf<String, Map<String, List<QuotaItem>>>()
        val inactive = mutableMapOf<String, Map<String, List<QuotaItem>>>()

        sortedData.forEach { (shipName, cargoOwners) ->
            val allQuotas = cargoOwners.values.flatten()
            val hasActiveQuota = allQuotas.any { it.isActive }

            if (hasActiveQuota) {
                active[shipName] = cargoOwners
            } else {
                inactive[shipName] = cargoOwners
            }
        }

        Pair(active.toMap(), inactive.toMap())
    }

    Column {
        QuotaTabSelector(
            selectedTabIndex = selectedTabIndex,
            onTabSelected = { selectedTabIndex = it },
            activeShipsCount = activeShipsData.size,
            inactiveShipsCount = inactiveShipsData.size
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
        ) {
            SortOptionsCard(
                sortType = sortType,
                onSortTypeChange = { sortType = it },
                showSortMenu = showSortMenu,
                onShowSortMenuChange = { showSortMenu = it }
            )

            FilterOptionsCard(
                filters = filters,
                onShowFiltersDialog = { showFiltersDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val currentData = when (selectedTabIndex) {
                0 -> activeShipsData
                1 -> inactiveShipsData
                else -> activeShipsData
            }

            QuotaTabContent(
                quotaData = currentData,
                isActive = selectedTabIndex == 0,
                expandedShip = expandedShip,
                expandedQuota = expandedQuota,
                onShipToggle = onShipToggle,
                onQuotaToggle = onQuotaToggle,
                onRefreshData = onRefreshData,
                viewModel = viewModel
            )
        }

        if (showFiltersDialog) {
            AdvancedFiltersDialog(
                filters = filters,
                onFiltersChanged = { newFilters -> filters = newFilters },
                onDismiss = { showFiltersDialog = false }
            )
        }
    }
}

@Composable
fun AdvancedFiltersDialog(
    filters: QuotaFilters,
    onFiltersChanged: (QuotaFilters) -> Unit,
    onDismiss: () -> Unit
) {
    var tempFilters by remember { mutableStateOf(filters) }

    val filterChipColors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.surface,
        labelColor = QuotaMutedText,
        selectedContainerColor = QuotaAccentBg,
        selectedLabelColor = QuotaAccent
    )
    val filterChipBorder = androidx.compose.material3.FilterChipDefaults.filterChipBorder(
        enabled = true,
        selected = false,
        borderColor = MaterialTheme.colorScheme.outlineVariant,
        selectedBorderColor = QuotaAccentBorder
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(QuotaModalGradientTop, QuotaModalGradientBottom)),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "فیلترهای پیشرفته",
                        style = MaterialTheme.typography.titleMedium,
                        color = QuotaTitleColor,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(QuotaMutedBg)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = QuotaTitleColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "وضعیت کوتاژها",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = QuotaMutedText
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            modifier = Modifier.weight(1f),
                            selected = tempFilters.showActiveOnly,
                            onClick = {
                                tempFilters = if (tempFilters.showActiveOnly) {
                                    tempFilters.copy(
                                        showActiveOnly = false,
                                        showInactiveOnly = false
                                    )
                                } else {
                                    tempFilters.copy(
                                        showActiveOnly = true,
                                        showInactiveOnly = false
                                    )
                                }
                            },
                            label = { Text("فقط فعال") },
                            colors = filterChipColors,
                            border = filterChipBorder
                        )

                        FilterChip(
                            modifier = Modifier.weight(1f),
                            selected = tempFilters.showInactiveOnly,
                            onClick = {
                                tempFilters = if (tempFilters.showInactiveOnly) {
                                    tempFilters.copy(
                                        showActiveOnly = false,
                                        showInactiveOnly = false
                                    )
                                } else {
                                    tempFilters.copy(
                                        showActiveOnly = false,
                                        showInactiveOnly = true
                                    )
                                }
                            },
                            label = { Text("فقط غیرفعال") },
                            colors = filterChipColors,
                            border = filterChipBorder
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "تناژ موقت",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = QuotaMutedText
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            modifier = Modifier.weight(1f),
                            selected = tempFilters.hasTemporaryTonnage == true,
                            onClick = {
                                tempFilters = tempFilters.copy(
                                    hasTemporaryTonnage = if (tempFilters.hasTemporaryTonnage == true) null else true
                                )
                            },
                            label = { Text("دارای تناژ موقت") },
                            colors = filterChipColors,
                            border = filterChipBorder
                        )

                        FilterChip(
                            modifier = Modifier.weight(1f),
                            selected = tempFilters.hasTemporaryTonnage == false,
                            onClick = {
                                tempFilters = tempFilters.copy(
                                    hasTemporaryTonnage = if (tempFilters.hasTemporaryTonnage == false) null else false
                                )
                            },
                            label = { Text("بدون تناژ موقت") },
                            colors = filterChipColors,
                            border = filterChipBorder
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(13.dp))
                            .background(QuotaMutedBg)
                            .clickable { tempFilters = QuotaFilters() }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("پاک کردن", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = QuotaMutedText)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(13.dp))
                            .background(QuotaAccent)
                            .clickable {
                                onFiltersChanged(tempFilters)
                                onDismiss()
                            }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("اعمال کردن", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = QuotaOnAccent)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterOptionsCard(
    modifier: Modifier = Modifier,
    filters: QuotaFilters,
    onShowFiltersDialog: () -> Unit
) {
    val activeFiltersCount = listOfNotNull(
        if (filters.showActiveOnly || filters.showInactiveOnly) 1 else null,
        if (filters.selectedWarehouses.isNotEmpty()) 1 else null,
        if (filters.minWeight != null || filters.maxWeight != null) 1 else null,
        if (filters.hasTemporaryTonnage != null) 1 else null
    ).size

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (activeFiltersCount > 0) QuotaAccentBg else QuotaMutedBg)
            .clickable { onShowFiltersDialog() },
        contentAlignment = Alignment.Center
    ) {
        Box {
            Icon(
                imageVector = Icons.Default.Filter,
                contentDescription = "فیلترها",
                tint = if (activeFiltersCount > 0) QuotaAccent else QuotaMutedText,
                modifier = Modifier.size(20.dp)
            )

            if (activeFiltersCount > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = 8.dp, y = (-8).dp)
                        .size(16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.error,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = activeFiltersCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onError,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SortOptionsCard(
    sortType: SortType,
    onSortTypeChange: (SortType) -> Unit,
    showSortMenu: Boolean,
    onShowSortMenuChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortOptions = listOf(
        SortType.NAME_ASC to "نام (الف تا ی)",
        SortType.NAME_DESC to "نام (ی تا الف)",
        SortType.QUOTA_COUNT_ASC to "تعداد کوتاژ (کم به زیاد)",
        SortType.QUOTA_COUNT_DESC to "تعداد کوتاژ (زیاد به کم)",
        SortType.TOTAL_WEIGHT_ASC to "وزن کل (کم به زیاد)",
        SortType.TOTAL_WEIGHT_DESC to "وزن کل (زیاد به کم)"
    )

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(QuotaMutedBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onShowSortMenuChange(!showSortMenu) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = "مرتب‌سازی",
                tint = QuotaMutedText,
                modifier = Modifier.size(20.dp)
            )
        }

        DropdownMenu(
            expanded = showSortMenu,
            onDismissRequest = { onShowSortMenuChange(false) },
            modifier = Modifier.width(250.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            sortOptions.forEach { (sortTypeOption, label) ->
                DropdownMenuItem(
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (sortType == sortTypeOption) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = QuotaAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Spacer(modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (sortType == sortTypeOption) QuotaAccent else QuotaTitleColor
                            )
                        }
                    },
                    onClick = {
                        onSortTypeChange(sortTypeOption)
                        onShowSortMenuChange(false)
                    }
                )
            }
        }
    }
}

data class TabData(
    val title: String,
    val count: Int,
    val icon: ImageVector
)

@Composable
fun QuotaTabSelector(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    activeShipsCount: Int,
    inactiveShipsCount: Int,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        TabData("کشتی فعال", activeShipsCount, Icons.Default.CheckCircle),
        TabData("کشتی غیرفعال", inactiveShipsCount, Icons.Default.Cancel)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(QuotaMutedBg)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEachIndexed { index, tab ->
            QuotaTabItem(
                tab = tab,
                isSelected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuotaTabItem(
    tab: TabData,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedBackground = MaterialTheme.colorScheme.surface
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) selectedBackground else Color.Transparent,
        animationSpec = tween(300),
        label = "background"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) QuotaAccent else QuotaMutedText,
        animationSpec = tween(300),
        label = "content"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = backgroundColor,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(15.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "${tab.title} (${tab.count})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

@Composable
fun QuotaTabContent(
    quotaData: Map<String, Map<String, List<QuotaItem>>>,
    isActive: Boolean,
    expandedShip: String?,
    expandedQuota: String?,
    onShipToggle: (String) -> Unit,
    onQuotaToggle: (String) -> Unit,
    onRefreshData: () -> Unit,
    viewModel: ReportsViewModel,
    modifier: Modifier = Modifier
) {
    if (quotaData.isEmpty()) {
        EmptyQuotaState(isActive = isActive)
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(
                items = quotaData.entries.toList(),
                key = { (shipName, _) -> shipName }
            ) { (shipName, cargoOwners) ->
                val allQuotas = cargoOwners.values.flatten()
                val allQuotasInactive = allQuotas.isNotEmpty() && allQuotas.all { !it.isActive }

                QuotaShipExpansionPanel(
                    shipName = shipName,
                    cargoOwners = cargoOwners,
                    isExpanded = expandedShip == shipName,
                    onToggleExpand = { onShipToggle(shipName) },
                    expandedQuota = expandedQuota,
                    onQuotaToggle = onQuotaToggle,
                    onRefreshData = onRefreshData,
                    viewModel = viewModel,
                    allQuotasInactive = allQuotasInactive
                )
            }
        }
    }
}

@Composable
fun EmptyQuotaState(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isActive) "هیچ کشتی با کوتاژ فعالی یافت نشد" else "هیچ کشتی با کوتاژ غیرفعالی یافت نشد",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isActive) "تمام کشتی‌ها دارای کوتاژ غیرفعال هستند" else "تمام کشتی‌ها دارای کوتاژ فعال هستند",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
