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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atk.atk_cargo.api.QuotaItem
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.feature.reports.domain.formatNumber
import com.atk.atk_cargo.feature.reports.presentation.ships.SearchField
import com.atk.atk_cargo.ui.viewmodel.ReportsViewModel
import kotlin.math.roundToInt

private val QuotaAccent: Color
    @Composable get() = MaterialTheme.colorScheme.primary

private val QuotaOnAccent: Color
    @Composable get() = MaterialTheme.colorScheme.onPrimary

private val QuotaAccentBg: Color
    @Composable get() = MaterialTheme.colorScheme.primaryContainer

private val QuotaAccentBorder: Color
    @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)

private val QuotaCardBorder: Color
    @Composable get() = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

private val QuotaMutedBg: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

private val QuotaMutedText: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

private val QuotaTitleColor: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface

private val QuotaScreenBg: Color
    @Composable get() = MaterialTheme.colorScheme.surface

private val QuotaModalGradientTop: Color
    @Composable get() = MaterialTheme.colorScheme.surface

private val QuotaModalGradientBottom: Color
    @Composable get() = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else Color(0xFFF2FAF8)

@Composable
fun QuotaManagementDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    viewModel: ReportsViewModel
) {
    if (!isVisible) return

    val currentShipName by viewModel.selectedShip.collectAsStateWithLifecycle()
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
            modifier = Modifier.fillMaxSize(),
            color = QuotaScreenBg
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                QuotaManagementHeaderCard(onClose = onDismiss)
                HorizontalDivider(color = QuotaCardBorder.copy(alpha = 0.7f))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    SearchField(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "جستجوی کوتاژ",
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(modifier = Modifier.height(10.dp))

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
                                        CircularProgressIndicator(color = QuotaAccent)
                                        Text(
                                            text = "در حال بارگذاری کوتاژها...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                            errorMessage != null -> {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp),
                                    color = MaterialTheme.colorScheme.errorContainer,
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(QuotaScreenBg)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(QuotaMutedBg)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "بستن",
                tint = QuotaTitleColor,
                modifier = Modifier.size(15.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(QuotaAccentBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Inventory,
                contentDescription = null,
                tint = QuotaAccent,
                modifier = Modifier.size(16.dp)
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = "مدیریت کوتاژها",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = QuotaTitleColor
            )
            Text(
                text = "کوتاژهای هر کشتی",
                style = MaterialTheme.typography.labelSmall,
                color = QuotaMutedText
            )
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

                                    if (quota.temporaryTonnageEnabled && quota.temporaryTonnageValue != null) {
                                        CompactStatChip(
                                            icon = Icons.Default.Scale,
                                            value = "${formatNumber(quota.temporaryTonnageValue.roundToInt())} تن",
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
