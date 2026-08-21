package com.atk.atk_cargo.feature.reports.presentation.warehouse_details

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.atk.atk_cargo.core.domain.AnimationManager
import com.atk.atk_cargo.data.model.FilteredSummary
import com.atk.atk_cargo.data.model.Quota
import com.atk.atk_cargo.data.model.Warehouse
import com.atk.atk_cargo.feature.reports.domain.formatNumber
import com.atk.atk_cargo.feature.reports.presentation.warehouse_details.components.DateTimePicker
import com.atk.atk_cargo.ui.viewmodel.ReportsViewModel

private val WarehouseTealAccent: Color
    @Composable get() = MaterialTheme.colorScheme.primary

private val WarehouseTealAccentBg: Color
    @Composable get() = MaterialTheme.colorScheme.primaryContainer

// internal (نه private) چون VoucherDetailsDialogSection.kt هم به این‌ها نیاز دارد
internal val WarehouseAccent: Color
    @Composable get() = MaterialTheme.colorScheme.primary

internal val WarehouseAccentBg: Color
    @Composable get() = MaterialTheme.colorScheme.primaryContainer

internal val WarehouseAccentBorder: Color
    @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)

internal val WarehouseCardBorder: Color
    @Composable get() = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

internal val WarehouseMutedBg: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

internal val WarehouseMutedText: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

internal val WarehouseTitleColor: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface

@Composable
fun WarehouseDetails(
    shipName: String,
    warehouseName: String,
    viewModel: ReportsViewModel,
    onBack: () -> Unit = {}
) {
    val reportsUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val warehouse = reportsUiState.selectedWarehouse
    val uiState = reportsUiState.status
    val filteredSummary = reportsUiState.filteredSummary
    var selectedQuota by remember { mutableStateOf<String?>(null) }
    var startDateTime by remember { mutableStateOf<String?>(null) }
    var endDateTime by remember { mutableStateOf<String?>(null) }
    var availableDates by remember { mutableStateOf<List<String>>(emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(shipName, warehouseName) {
        viewModel.loadWarehouseDetails(shipName, warehouseName)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            WarehouseDetailHeader(
                warehouseName = warehouse?.name ?: warehouseName,
                shipName = shipName,
                onBack = onBack
            )
            HorizontalDivider(color = WarehouseCardBorder.copy(alpha = 0.7f))

            WarehouseContent(
                warehouse = warehouse,
                uiState = uiState,
                filteredSummary = filteredSummary,
                selectedQuota = selectedQuota,
                startDateTime = startDateTime,
                endDateTime = endDateTime,
                availableDates = availableDates,
                shipName = shipName,
                onQuotaSelected = { quota ->
                    selectedQuota = if (selectedQuota == quota.number) null else quota.number
                    availableDates = if (selectedQuota != null) {
                        warehouse?.quotas
                            ?.find { it.number == selectedQuota }
                            ?.exitDates?.map { it.date }
                            ?.distinct()
                            ?.sorted()
                            ?: emptyList()
                    } else {
                        emptyList()
                    }
                    startDateTime = null
                    endDateTime = null
                    viewModel.clearFilteredSummary()
                },
                onStartDateTimeSelected = { dateTime ->
                    startDateTime = dateTime
                    if (dateTime == null || (endDateTime != null && endDateTime!! < dateTime)) {
                        endDateTime = null
                    }
                    viewModel.clearFilteredSummary()
                },
                onEndDateTimeSelected = { dateTime ->
                    endDateTime = dateTime
                    viewModel.clearFilteredSummary()
                },
                onFilterApplied = {
                    selectedQuota?.let { quota ->
                        viewModel.getFilteredSummary(
                            shipName = shipName,
                            warehouseName = warehouse?.name ?: "",
                            selectedQuota = quota,
                            startDateTime = startDateTime,
                            endDateTime = endDateTime
                        )
                    }
                }
            )
        }
    }

    LaunchedEffect(snackbarHostState) {
        viewModel.snackbarMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
}

@Composable
private fun WarehouseDetailHeader(
    warehouseName: String,
    shipName: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(WarehouseMutedBg)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "بازگشت",
                tint = WarehouseTitleColor,
                modifier = Modifier.size(16.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(WarehouseAccentBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warehouse,
                contentDescription = null,
                tint = WarehouseAccent,
                modifier = Modifier.size(16.dp)
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = warehouseName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = WarehouseTitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBoat,
                    contentDescription = null,
                    tint = WarehouseMutedText,
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = shipName,
                    style = MaterialTheme.typography.labelSmall,
                    color = WarehouseMutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun WarehouseContent(
    warehouse: Warehouse?,
    uiState: ReportsViewModel.UiState,
    filteredSummary: FilteredSummary?,
    selectedQuota: String?,
    startDateTime: String?,
    endDateTime: String?,
    availableDates: List<String>,
    shipName: String,
    onQuotaSelected: (Quota) -> Unit,
    onStartDateTimeSelected: (String?) -> Unit,
    onEndDateTimeSelected: (String?) -> Unit,
    onFilterApplied: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is ReportsViewModel.UiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is ReportsViewModel.UiState.Error -> {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            is ReportsViewModel.UiState.Success -> {
                warehouse?.let { warehouseDetails ->
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = WarehouseMutedBg,
                        border = BorderStroke(1.dp, WarehouseCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(WarehouseAccentBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Filter,
                                        contentDescription = null,
                                        tint = WarehouseAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Text(
                                    text = "فیلتر و جستجو",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = WarehouseTitleColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    QuotaSelector(
                        quotas = warehouseDetails.quotas,
                        selectedQuota = selectedQuota,
                        onQuotaSelected = onQuotaSelected
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DateTimePicker(
                            label = "از تاریخ و زمان",
                            selectedDateTime = startDateTime,
                            availableDates = availableDates,
                            onDateTimeSelected = onStartDateTimeSelected,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        DateTimePicker(
                            label = "تا تاریخ و زمان",
                            selectedDateTime = endDateTime,
                            availableDates = availableDates.filter { it >= (startDateTime?.split(" ")?.first() ?: "") },
                            onDateTimeSelected = {
                                onEndDateTimeSelected(it)
                                if (it != null) {
                                    onFilterApplied()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    filteredSummary?.let { summary ->
                        VoucherDetailsButton(summary)
                    }
                }
            }
        }
    }
}

@Composable
fun QuotaSelector(
    quotas: List<Quota>,
    selectedQuota: String?,
    onQuotaSelected: (Quota) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, WarehouseCardBorder),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(WarehouseAccentBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = WarehouseAccent,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Text(
                        text = "انتخاب شماره کوتاژ",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = WarehouseTitleColor
                    )
                }

                Text(
                    text = "${quotas.size} کوتاژ",
                    style = MaterialTheme.typography.labelSmall,
                    color = WarehouseMutedText.copy(alpha = 0.7f)
                )
            }

            if (quotas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "کوتاژی یافت نشد",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    items(
                        items = quotas,
                        key = { "${it.number}|${it.shipName}|${it.warehouse}|${it.shippingCompany}|${it.cargoType}" }
                    ) { quota ->
                        QuotaChip(
                            quota = quota,
                            isSelected = selectedQuota == quota.number,
                            onSelect = { onQuotaSelected(quota) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuotaChip(
    quota: Quota,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // dotAlpha باید فقط وقتی isSelected==true فعال شود تا مثل باگ badgeScale اجرای بی‌مورد نداشته باشد
    val dotAlpha: Float = if (isSelected && AnimationManager.areAnimationsEnabled) {
        val infiniteTransition = rememberInfiniteTransition(label = "dot pulse")
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot alpha"
        ).value
    } else {
        1f
    }

    val accent = WarehouseAccent
    val onAccent = MaterialTheme.colorScheme.onPrimary

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> accent
            isPressed -> WarehouseMutedBg.copy(alpha = 0.8f)
            else -> WarehouseMutedBg
        },
        label = "background color"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            onAccent
        } else {
            WarehouseMutedText
        },
        label = "content color"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale animation"
    )

    Surface(
        onClick = onSelect,
        modifier = Modifier
            .height(42.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = CircleShape,
        color = backgroundColor,
        border = if (isSelected) null else BorderStroke(1.dp, WarehouseCardBorder),
        interactionSource = interactionSource,
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = quota.number,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = contentColor
            )

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .graphicsLayer {
                        alpha = if (isSelected) dotAlpha else 1f
                    }
                    .background(
                        color = when {
                            isSelected -> onAccent
                            quota.isActive -> accent
                            else -> MaterialTheme.colorScheme.error
                        },
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
fun VoucherDetailsButton(summary: FilteredSummary) {
    var showVoucherDetailsDialog by remember { mutableStateOf(false) }

    Surface(
        onClick = { showVoucherDetailsDialog = true },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = WarehouseMutedBg,
        border = BorderStroke(width = 1.dp, color = WarehouseCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(WarehouseAccentBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = WarehouseAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "مشاهده جزئیات حواله‌ها",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = WarehouseTitleColor
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${formatNumber(summary.voucherCount)} حواله",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = WarehouseAccent
                        )

                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = WarehouseMutedText.copy(alpha = 0.5f)
                        )

                        Text(
                            text = formatNumber(summary.totalNetWeight.toInt()),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = WarehouseMutedText
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = WarehouseMutedText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    if (showVoucherDetailsDialog) {
        VoucherDetailsDialog(
            summary = summary,
            onDismiss = { showVoucherDetailsDialog = false }
        )
    }
}

@Composable
fun WarehousesSection(
    warehouses: List<Warehouse>,
    onWarehouseSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(
                items = warehouses,
                key = { it.name }
            ) { warehouse ->
                WarehouseCard(
                    warehouse = warehouse,
                    onClick = { onWarehouseSelected(warehouse.name) }
                )
            }
        }
    }
}

@Composable
private fun WarehouseCard(
    warehouse: Warehouse,
    onClick: () -> Unit
) {
    val totalTonnage = warehouse.totalTonnage
    val loadedTonnage = warehouse.loadedTonnage
    val remainingTonnage = warehouse.remainingTonnage
    val accentColor = WarehouseTealAccent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warehouse,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = warehouse.name,
                        style = MaterialTheme.typography.titleMedium,
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = formatNumber(totalTonnage.toInt()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 9.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مانده:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "↓ ${formatNumber(remainingTonnage.toInt())}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = WarehouseTealAccentBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 9.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "بارگیری:",
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "↑ ${formatNumber(loadedTonnage.toInt())}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
            }
        }
    }
}
