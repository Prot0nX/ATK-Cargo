package com.atk.atk_cargo.feature.reports.presentation.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atk.atk_cargo.api.QuotaItem
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.feature.reports.domain.formatNumber
import com.atk.atk_cargo.feature.reports.presentation.ships.SearchField
import com.atk.atk_cargo.ui.viewmodel.ReportsViewModel
import kotlin.math.roundToInt

@Composable
fun QuotaManagementDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    viewModel: ReportsViewModel
) {
    if (!isVisible) return

    val currentShipName by viewModel.selectedShip.collectAsState()
    var quotaData by remember { mutableStateOf<Map<String, Map<String, List<QuotaItem>>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var expandedShip by remember { mutableStateOf<String?>(null) }
    var expandedQuota by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableStateOf(0) }

    val refreshData: () -> Unit = {
        refreshTrigger++
    }

    LaunchedEffect(currentShipName, refreshTrigger) {
        try {
            isLoading = true
            val response = RetrofitClient.apiService.getGroupedQuotas(shipName = currentShipName?.name ?: "")
            if (response.isSuccessful) {
                quotaData = response.body() ?: emptyMap()
                errorMessage = null
            } else {
                errorMessage = "خطا در دریافت داده‌ها: ${response.code()}"
            }
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                QuotaManagementHeaderCard(onClose = onDismiss)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    SearchField(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "جستجوی کوتاژ",
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        when {
                            isLoading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "در حال بارگذاری کوتاژها...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                            errorMessage != null -> {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Text(
                                            text = "خطا: $errorMessage",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                            else -> {
                                QuotaManagementContent(
                                    quotaData = quotaData,
                                    searchQuery = searchQuery,
                                    expandedShip = expandedShip,
                                    expandedQuota = expandedQuota,
                                    onShipToggle = { shipName ->
                                        expandedShip = if (expandedShip == shipName) null else shipName
                                        expandedQuota = null
                                    },
                                    onQuotaToggle = { quotaKey ->
                                        expandedQuota = if (expandedQuota == quotaKey) null else quotaKey
                                    },
                                    onRefreshData = refreshData,
                                    viewModel = viewModel
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
fun QuotaManagementHeaderCard(
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = "مدیریت کوتاژها",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "بستن",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

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

                    val weightFilter = {
                        val weight = quota.temporaryTonnageValue ?: 0f
                        val minOk = filters.minWeight?.let { weight >= it } ?: true
                        val maxOk = filters.maxWeight?.let { weight <= it } ?: true
                        minOk && maxOk
                    }()

                    val tempTonnageFilter = filters.hasTemporaryTonnage?.let { hasTemp ->
                        if (hasTemp) quota.temporaryTonnageEnabled else !quota.temporaryTonnageEnabled
                    } ?: true

                    statusFilter && warehouseFilter && weightFilter && tempTonnageFilter
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

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        )
                    )
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "فیلترهای پیشرفته",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "وضعیت کوتاژها",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
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
                            label = { Text("فقط فعال") }
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
                            label = { Text("فقط غیرفعال") }
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "تناژ موقت",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
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
                            label = { Text("دارای تناژ موقت") }
                        )

                        FilterChip(
                            modifier = Modifier.weight(1f),
                            selected = tempFilters.hasTemporaryTonnage == false,
                            onClick = {
                                tempFilters = tempFilters.copy(
                                    hasTemporaryTonnage = if (tempFilters.hasTemporaryTonnage == false) null else false
                                )
                            },
                            label = { Text("بدون تناژ موقت") }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            tempFilters = QuotaFilters()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "پاک کردن",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onFiltersChanged(tempFilters)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "اعمال کردن",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
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

    Card(
        modifier = modifier.size(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (activeFiltersCount > 0) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            }
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onShowFiltersDialog() },
            contentAlignment = Alignment.Center
        ) {
            Box {
                Icon(
                    imageVector = Icons.Default.Filter,
                    contentDescription = "فیلترها",
                    tint = if (activeFiltersCount > 0) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(24.dp)
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

    Card(
        modifier = modifier.size(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Box {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onShowSortMenuChange(!showSortMenu) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "مرتب‌سازی",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { onShowSortMenuChange(false) },
                modifier = Modifier.width(250.dp)
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
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (sortType == sortTypeOption)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface
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

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
}

@Composable
fun QuotaTabItem(
    tab: TabData,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        },
        animationSpec = tween(300),
        label = "background"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(300),
        label = "content"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${tab.title} (${tab.count})",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
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
            items(quotaData.entries.toList()) { (shipName, cargoOwners) ->
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutSlowInEasing
                )
            ),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
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
                                .size(36.dp)
                                .background(
                                    color = if (quota.isActive)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    else
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = if (quota.isActive)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
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
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                CompactStatChip(
                                    icon = Icons.Default.Warehouse,
                                    value = quota.warehouse,
                                    color = MaterialTheme.colorScheme.tertiary
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
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = quota.cargoOwner,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary,
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

                                    if (quota.temporaryTonnageEnabled && quota.temporaryTonnageValue != null) {
                                        CompactStatChip(
                                            icon = Icons.Default.Scale,
                                            value = "${formatNumber(quota.temporaryTonnageValue.roundToInt())} تن",
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "بستن" else "گسترش",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
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

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
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
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "تناژ موقت",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
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
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        if (isUpdating) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            FilledIconButton(
                                onClick = onUpdateTonnage,
                                enabled = tempTonnageValue.isNotEmpty(),
                                modifier = Modifier.size(48.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (tempTonnageValue.isNotEmpty())
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    contentColor = if (tempTonnageValue.isNotEmpty())
                                        MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
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
    val containerColor = if (isActive) {
        Color(0xFF4CAF50).copy(alpha = 0.15f)
    } else {
        Color(0xFFF44336).copy(alpha = 0.15f)
    }
    val contentColor = if (isActive) {
        Color(0xFF2E7D32)
    } else {
        Color(0xFFC62828)
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutSlowInEasing
                )
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (allQuotasInactive)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(
            width = if (allQuotasInactive) 2.dp else 1.dp,
            color = if (allQuotasInactive)
                MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
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
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBoat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = shipName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
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
        color = if (MaterialTheme.colorScheme.surface == Color(0xFF0f172a))
            Color(0xFF1e293b) else Color(0xFFEFF6FF),
        border = BorderStroke(0.5.dp, if (MaterialTheme.colorScheme.surface == Color(0xFF0f172a))
            Color(0xFF334155) else Color(0xFF93C5FD))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = if (MaterialTheme.colorScheme.surface == Color(0xFF0f172a))
                    Color(0xFF93c5fd) else Color(0xFF1E40AF),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (MaterialTheme.colorScheme.surface == Color(0xFF0f172a))
                    Color(0xFF93c5fd) else Color(0xFF1E40AF)
            )
        }
    }
}
