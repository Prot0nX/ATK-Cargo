package com.atk.atk_cargo.feature.reports.presentation.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.atk.atk_cargo.data.model.Quota
import com.atk.atk_cargo.data.model.Ship
import com.atk.atk_cargo.data.model.WarningStatus
import com.atk.atk_cargo.feature.reports.domain.QuotaWarningThresholds
import com.atk.atk_cargo.feature.reports.domain.calculateProgress
import com.atk.atk_cargo.feature.reports.domain.formatNumber
import com.atk.atk_cargo.feature.reports.domain.formatWeightWithDetail
import com.atk.atk_cargo.feature.reports.presentation.dialogs.QuotaWarningDialog
import com.atk.atk_cargo.feature.reports.presentation.quota_details.QuotasList
import com.atk.atk_cargo.feature.reports.presentation.ships.SearchField
import com.atk.atk_cargo.feature.reports.presentation.warehouse_details.WarehousesSection
import com.atk.atk_cargo.feature.reports.viewmodel.ReportsViewModel
import com.atk.atk_cargo.ui.theme.DeepOrange100
import com.atk.atk_cargo.ui.theme.DeepOrange300
import com.atk.atk_cargo.ui.theme.DeepOrange900

private val ShipDetailsTealAccent: Color
    @Composable get() = MaterialTheme.colorScheme.primary

private val ShipDetailsTealAccentBg: Color
    @Composable get() = MaterialTheme.colorScheme.primaryContainer

private val ShipDetailsDeepOrangeAccent: Color
    @Composable get() = if (isSystemInDarkTheme()) DeepOrange300 else DeepOrange900

private val ShipDetailsDeepOrangeAccentBg: Color
    @Composable get() = if (isSystemInDarkTheme()) DeepOrange900.copy(alpha = 0.18f) else DeepOrange100.copy(alpha = 0.6f)

private val ShipDetailsTabAccent: Color
    @Composable get() = MaterialTheme.colorScheme.primary

@Composable
fun ShipDetails(
    initialShipName: String,
    viewModel: ReportsViewModel,
    onWarehouseSelected: (String) -> Unit,
    onSectionChanged: (Int) -> Unit,
    onShipNotFound: () -> Unit = {}
) {
    val reportsUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ship = reportsUiState.selectedShip
    val selectedShipQuotas = reportsUiState.selectedShipQuotas
    var showWarningDialog by rememberSaveable { mutableStateOf(false) }
    // شماره کوتاژهای هشداری که کاربر قبلاً بسته؛ چون Set در Bundle ذخیره نمی‌شود، به‌صورت CSV نگه داشته می‌شود
    var dismissedWarningQuotaNumbersCsv by rememberSaveable { mutableStateOf("") }
    val uiState = reportsUiState.status
    val isLoadingShipDetails = reportsUiState.isLoadingShipDetails
    val isLoadingShipQuotas = reportsUiState.isLoadingShipQuotas
    val shipDetailsLoadingState = reportsUiState.shipDetailsLoadingState
    val warnings = remember(selectedShipQuotas) {
        selectedShipQuotas.mapNotNull { quota -> calculateWarningStatus(quota) }
    }
    val dismissedWarningQuotaNumbers = remember(dismissedWarningQuotaNumbersCsv) {
        dismissedWarningQuotaNumbersCsv.split(",").filterTo(mutableSetOf()) { it.isNotEmpty() }
    }
    val newWarnings = remember(warnings, dismissedWarningQuotaNumbers) {
        warnings.filter { it.quotaNumber !in dismissedWarningQuotaNumbers }
    }

    LaunchedEffect(newWarnings) {
        if (newWarnings.isNotEmpty()) {
            showWarningDialog = true
        }
    }

    LaunchedEffect(initialShipName) {
        viewModel.clearCurrentShipData()
        viewModel.loadShipDataAsync(initialShipName)
    }

    LaunchedEffect(Unit) {
        viewModel.shipNotFoundEvent.collect {
            onShipNotFound()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            when {
                uiState is ReportsViewModel.UiState.Error -> {
                    ErrorStateCard(
                        title = "خطا در سیستم",
                        message = (uiState as? ReportsViewModel.UiState.Error)?.message ?: "خطای نامشخص"
                    )
                }

                isLoadingShipDetails || isLoadingShipQuotas -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = when {
                                    isLoadingShipDetails && isLoadingShipQuotas -> "در حال بارگذاری اطلاعات کشتی..."
                                    isLoadingShipDetails -> "در حال بارگذاری جزئیات کشتی..."
                                    else -> "در حال بارگذاری سهمیه‌های کشتی..."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                shipDetailsLoadingState is ReportsViewModel.LoadingState.Error -> {
                    ErrorStateCard(
                        title = "خطا در بارگذاری اطلاعات کشتی",
                        message = (shipDetailsLoadingState as? ReportsViewModel.LoadingState.Error)?.message
                            ?: "خطا در دریافت اطلاعات از سرور"
                    )
                }

                ship != null -> {
                    ship?.let { shipDetails ->
                        WarehousesAndQuotasTab(
                            shipDetails = shipDetails,
                            selectedShipQuotas = selectedShipQuotas,
                            onWarehouseSelected = onWarehouseSelected,
                            viewModel = viewModel,
                            onSectionChanged = onSectionChanged
                        )
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .wrapContentHeight()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "اطلاعات کشتی در حال بارگذاری می باشند",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "لطفاً صبر کنید...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    if (showWarningDialog && newWarnings.isNotEmpty()) {
        QuotaWarningDialog(
            warnings = newWarnings,
            onDismiss = {
                showWarningDialog = false
                val dismissedNow = dismissedWarningQuotaNumbers + newWarnings.map { it.quotaNumber }
                dismissedWarningQuotaNumbersCsv = dismissedNow.joinToString(",")
            },
            viewModel = viewModel
        )
    }
}

@Composable
private fun ErrorStateCard(title: String, message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WarehousesAndQuotasTab(
    shipDetails: Ship,
    selectedShipQuotas: List<Quota>,
    onWarehouseSelected: (String) -> Unit,
    viewModel: ReportsViewModel,
    onSectionChanged: (Int) -> Unit
) {
    var selectedSection by rememberSaveable { mutableIntStateOf(0) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val reportsUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoadingShipQuotas = reportsUiState.isLoadingShipQuotas
    val shipQuotasLoadingState = reportsUiState.shipQuotasLoadingState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ShipHeaderCard(shipDetails = shipDetails)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            SearchField(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = if (selectedSection == 0) {
                    "جستجو بر اساس شماره کوتاژ، شرکت حمل یا صاحب کالا..."
                } else {
                    "جستجو بر اساس نام انبار..."
                },
                keyboardType = KeyboardType.Text
            )

            Spacer(modifier = Modifier.height(8.dp))

            WarehouseQuotasTabs(
                selectedTabIndex = selectedSection,
                onTabSelected = { index ->
                    selectedSection = index
                    searchQuery = ""
                    onSectionChanged(index)
                },
                quotasCount = selectedShipQuotas.size,
                warehousesCount = shipDetails.warehouses.size
            )

            Spacer(modifier = Modifier.height(4.dp))

            when (selectedSection) {
                0 -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        QuotasList(
                            quotas = selectedShipQuotas,
                            searchQuery = searchQuery,
                            groupingMode = viewModel.warehouseQuotaGroupingMode,
                            onGroupingModeChange = viewModel::setWarehouseQuotaGroupingMode,
                            onEdit = viewModel::editQuota,
                            onToggleStatus = { id, quotaNumber -> viewModel.toggleQuotaStatus(id, quotaNumber) },
                            onDelete = viewModel::deleteQuota,
                            viewModel = viewModel
                        )

                        if (isLoadingShipQuotas) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                                tonalElevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "به‌روزرسانی کوتاژها...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        if (shipQuotasLoadingState is ReportsViewModel.LoadingState.Error && selectedShipQuotas.isNotEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                                tonalElevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "خطا در به‌روزرسانی",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    val filteredWarehouses = remember(shipDetails.warehouses, searchQuery) {
                        shipDetails.warehouses.filter {
                            it.name.contains(searchQuery, ignoreCase = true)
                        }
                    }
                    WarehousesSection(
                        warehouses = filteredWarehouses,
                        onWarehouseSelected = onWarehouseSelected
                    )
                }
            }
        }
    }
}

@Composable
private fun ShipHeaderCard(shipDetails: Ship) {
    val loadedTonnage = shipDetails.loadedTonnage
    val progress = calculateProgress(loadedTonnage, shipDetails.totalTonnage)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ShipDetailsTealAccentBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = ShipDetailsTealAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = shipDetails.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${formatNumber(shipDetails.warehouses.size)} انبار · ${formatNumber(shipDetails.quotaCount)} کوتاژ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(13.dp),
                color = ShipDetailsDeepOrangeAccentBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = ShipDetailsDeepOrangeAccent
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(ShipDetailsDeepOrangeAccent.copy(alpha = 0.18f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .background(ShipDetailsDeepOrangeAccent)
                        )
                    }
                    Text(
                        text = "مانده: ${formatWeightWithDetail(shipDetails.remainingTonnage)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = ShipDetailsDeepOrangeAccent,
                        maxLines = 1
                    )
                    Text(
                        text = "کل: ${formatWeightWithDetail(shipDetails.totalTonnage)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun WarehouseQuotasTabs(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    quotasCount: Int,
    warehousesCount: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TabItem(
                icon = Icons.Default.Receipt,
                label = "کوتاژها",
                count = quotasCount,
                isSelected = selectedTabIndex == 0,
                onClick = { onTabSelected(0) }
            )
            TabItem(
                icon = Icons.Default.Warehouse,
                label = "انبارها",
                count = warehousesCount,
                isSelected = selectedTabIndex == 1,
                onClick = { onTabSelected(1) }
            )
        }
    }
}

@Composable
private fun RowScope.TabItem(
    icon: ImageVector,
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .weight(1f)
            .semantics { role = Role.Tab; selected = isSelected }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        shadowElevation = if (isSelected) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) ShipDetailsTabAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) ShipDetailsTabAccent else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) ShipDetailsTabAccent.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) ShipDetailsTabAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

private fun calculateWarningStatus(quota: Quota): WarningStatus? {
    if (!quota.isActive) {
        return null
    }

    val remainingTonnage = quota.remainingTonnage
    val percentage = quota.percentage

    if (percentage != null && percentage == 0.0 && remainingTonnage < QuotaWarningThresholds.ZERO_PERCENT_REMAINING_KG) {
        val totalTonnage = quota.totalTonnage
        val percentageAmount = totalTonnage * (percentage / 100)
        return WarningStatus(
            show = true,
            quotaId = quota.id,
            quotaNumber = quota.number,
            percentage = percentage,
            remainingTonnage = remainingTonnage,
            percentageAmount = percentageAmount,
            isActive = true,
            isPercentageRestricted = quota.isPercentageRestricted ?: false
        )
    }

    if (quota.isPercentageRestricted != true || percentage == null) {
        return null
    }

    val totalTonnage = quota.totalTonnage
    val percentageAmount = totalTonnage * (percentage / 100)
    val warningThreshold = QuotaWarningThresholds.PERCENTAGE_CAP_PROXIMITY_KG

    // قدرمطلق استفاده نمی‌شود؛ فقط وقتی مانده به سقف درصد نزدیک شده یا از آن گذشته هشدار داده می‌شود
    if (remainingTonnage - percentageAmount <= warningThreshold) {
        return WarningStatus(
            show = true,
            quotaId = quota.id,
            quotaNumber = quota.number,
            percentage = percentage,
            remainingTonnage = remainingTonnage,
            percentageAmount = percentageAmount,
            isActive = true,
            isPercentageRestricted = true
        )
    }

    return null
}