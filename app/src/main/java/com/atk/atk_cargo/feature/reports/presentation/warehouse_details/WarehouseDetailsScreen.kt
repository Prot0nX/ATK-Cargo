package com.atk.atk_cargo.feature.reports.presentation.warehouse_details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atk.atk_cargo.api.FilteredSummary
import com.atk.atk_cargo.api.Quota
import com.atk.atk_cargo.api.VoucherDetail
import com.atk.atk_cargo.api.Warehouse
import com.atk.atk_cargo.feature.reports.domain.formatNumber
import com.atk.atk_cargo.feature.reports.domain.persianDateFormat
import com.atk.atk_cargo.feature.reports.presentation.warehouse_details.components.DateTimePicker
import com.atk.atk_cargo.ui.theme.Blue100
import com.atk.atk_cargo.ui.theme.Blue300
import com.atk.atk_cargo.ui.theme.Blue400
import com.atk.atk_cargo.ui.theme.Blue50
import com.atk.atk_cargo.ui.theme.Blue700
import com.atk.atk_cargo.ui.theme.Blue900
import com.atk.atk_cargo.ui.theme.Corner2XL
import com.atk.atk_cargo.ui.theme.Corner3XL
import com.atk.atk_cargo.ui.theme.CornerL
import com.atk.atk_cargo.ui.theme.CornerM
import com.atk.atk_cargo.ui.theme.CornerXL
import com.atk.atk_cargo.ui.theme.Gray100
import com.atk.atk_cargo.ui.theme.Gray200
import com.atk.atk_cargo.ui.theme.Gray400
import com.atk.atk_cargo.ui.theme.Gray50
import com.atk.atk_cargo.ui.theme.Gray500
import com.atk.atk_cargo.ui.theme.PrimaryBlue
import com.atk.atk_cargo.ui.theme.PrimaryBlueLight
import com.atk.atk_cargo.ui.theme.Slate200
import com.atk.atk_cargo.ui.theme.Slate300
import com.atk.atk_cargo.ui.theme.Slate600
import com.atk.atk_cargo.ui.theme.Slate700
import com.atk.atk_cargo.ui.theme.Slate800
import com.atk.atk_cargo.ui.theme.SurfaceVariantDark
import com.atk.atk_cargo.ui.viewmodel.ReportsViewModel

@Composable
fun WarehouseDetails(
    shipName: String,
    warehouseName: String,
    viewModel: ReportsViewModel
) {
    val warehouse by viewModel.selectedWarehouse.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val filteredSummary by viewModel.filteredSummary.collectAsState()
    var selectedQuota by remember { mutableStateOf<String?>(null) }
    var startDateTime by remember { mutableStateOf<String?>(null) }
    var endDateTime by remember { mutableStateOf<String?>(null) }
    var availableDates by remember { mutableStateOf<List<String>>(emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(shipName, warehouseName) {
        viewModel.loadWarehouseDetails(shipName, warehouseName)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                },
                onExport = { format ->
                    filteredSummary?.let { summary ->
                        viewModel.exportData(format, summary)
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
    onFilterApplied: () -> Unit,
    onExport: (String) -> Unit
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
                    WarehouseMainCard(
                        warehouseName = warehouseDetails.name,
                        shipName = shipName
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color(0xFFE0E7FF).copy(alpha = 0.8f),
                        border = BorderStroke(1.dp, if (isSystemInDarkTheme()) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f) else Color(0xFFC7D2FE))
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
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFEEF2FF),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Filter,
                                            contentDescription = null,
                                            tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF4F46E5),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "فیلتر و جستجو",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else Color(0xFF374151)
                                )
                            }

                            ExportOptions(
                                onExport = { format ->
                                    filteredSummary?.let {
                                        onExport(format)
                                    }
                                }
                            )
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
fun ExportOptions(onExport: (String) -> Unit) {
    Surface(
        onClick = { onExport("pdf") },
        shape = RoundedCornerShape(12.dp),
        color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceVariant else Color.White,
        border = BorderStroke(1.dp, if (isSystemInDarkTheme()) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f) else Color(0xFFF3F4F6)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "خروجی PDF",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else Color(0xFF2563EB)
            )

            Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = "خروجی PDF",
                tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else Color(0xFF2563EB),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}



@Composable
fun WarehouseMainCard(warehouseName: String, shipName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoCard(
                    icon = Icons.Default.DirectionsBoat,
                    title = "کشتی",
                    value = shipName,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                InfoCard(
                    icon = Icons.Default.Store,
                    title = "انبار",
                    value = warehouseName,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun InfoCard(
    icon: ImageVector,
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )

                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.1f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = "انتخاب شماره کوتاژ",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${quotas.size} کوتاژ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
                    items(quotas) { quota ->
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

    val infiniteTransition = rememberInfiniteTransition(label = "dot pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot alpha"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isPressed -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        label = "background color"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
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
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
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
                            isSelected -> Color.White
                            quota.isActive -> if (isSystemInDarkTheme()) Color(0xFF60A5FA) else Color(0xFF2563EB)
                            else -> if (isSystemInDarkTheme()) Color(0xFFF87171) else Color(0xFFEF4444)
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
    val isDark = isSystemInDarkTheme()

    Surface(
        onClick = { showVoucherDetailsDialog = true },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFEFF6FF).copy(alpha = 0.8f),
        border = BorderStroke(
            width = 1.dp,
            color = if (isDark) Color(0xFF374151) else Color(0xFFDBEAFE)
        )
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
                Surface(
                    shape = CircleShape,
                    color = if (isDark) Color(0xFF374151) else Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "مشاهده جزئیات حواله‌ها",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF111827)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${formatNumber(summary.voucherCount)} حواله",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color(0xFF93C5FD) else Color(0xFF2563EB)
                        )

                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = if (isDark) Color(0xFF6B7280) else Color(0xFF9CA3AF).copy(alpha = 0.5f)
                        )

                        Text(
                            text = formatNumber(summary.totalNetWeight.toInt()),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                        )
                    }
                }
            }

            Surface(
                shape = CircleShape,
                color = if (isDark) Color(0xFF374151) else Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF9CA3AF) else Color(0xFF4B5563),
                        modifier = Modifier.size(20.dp)
                    )
                }
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
fun VoucherDetailsDialog(
    summary: FilteredSummary,
    onDismiss: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    var searchQuery by remember { mutableStateOf("") }
    var sortType by remember { mutableStateOf(VoucherSortType.DATE_DESC) }

    val filteredVoucherDetails by remember(searchQuery, sortType, summary.voucherDetails) {
        derivedStateOf {
            val filtered = summary.voucherDetails.filter { voucher ->
                val matchesSearch = if (searchQuery.isEmpty()) {
                    true
                } else {
                    voucher.trackingNumber.contains(searchQuery, ignoreCase = true) ||
                            voucher.scaleReceiptNumber.contains(searchQuery, ignoreCase = true) ||
                            voucher.exitDate.contains(searchQuery, ignoreCase = true) ||
                            voucher.username?.contains(searchQuery, ignoreCase = true) == true ||
                            voucher.confirmUsername?.contains(searchQuery, ignoreCase = true) == true
                }
                matchesSearch
            }

            when (sortType) {
                VoucherSortType.DATE_ASC -> filtered.sortedBy { it.exitDate }
                VoucherSortType.DATE_DESC -> filtered.sortedByDescending { it.exitDate }
                VoucherSortType.WEIGHT_ASC -> filtered.sortedBy { it.netWeight }
                VoucherSortType.WEIGHT_DESC -> filtered.sortedByDescending { it.netWeight }
            }
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
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(Corner3XL),
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(CornerL),
                            color = if (isDarkTheme) Blue900.copy(alpha = 0.3f) else Blue100.copy(alpha = 0.8f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "جزئیات حواله‌ها",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Text(
                                text = "${formatNumber(filteredVoucherDetails.size)} از ${formatNumber(summary.voucherCount)} حواله",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = PrimaryBlue,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Surface(
                        onClick = onDismiss,
                        shape = CircleShape,
                        color = if (isDarkTheme) Slate800 else Gray100,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "بستن",
                                tint = if (isDarkTheme) Slate300 else Gray500,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    VoucherSearchAndFilter(
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        sortType = sortType,
                        onSortTypeChange = { sortType = it },
                        isDarkTheme = isDarkTheme
                    )

                    if (filteredVoucherDetails.isEmpty()) {
                        EmptyVoucherList()
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "لیست حواله‌ها",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) Slate300 else Slate700
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "مرتب‌سازی: ${sortType.persianName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDarkTheme) Gray500 else Gray400
                                )

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = null,
                                    tint = if (isDarkTheme) Gray500 else Gray400,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
                        ) {
                            items(
                                items = filteredVoucherDetails,
                                key = { it.trackingNumber + "_" + it.scaleReceiptNumber }
                            ) { voucher ->
                                VoucherItem(voucher, isDarkTheme)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoucherSearchAndFilter(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    sortType: VoucherSortType,
    onSortTypeChange: (VoucherSortType) -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SearchTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = "جستجو در حواله‌ها...",
            isDarkTheme = isDarkTheme
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(VoucherSortType.DATE_ASC, VoucherSortType.DATE_DESC).forEach { sortOption ->
                    SortChip(
                        type = sortOption,
                        isSelected = sortType == sortOption,
                        onClick = { onSortTypeChange(sortOption) },
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(VoucherSortType.WEIGHT_ASC, VoucherSortType.WEIGHT_DESC).forEach { sortOption ->
                    SortChip(
                        type = sortOption,
                        isSelected = sortType == sortOption,
                        onClick = { onSortTypeChange(sortOption) },
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerXL),
        color = if (isDarkTheme) SurfaceVariantDark else Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(
            width = 1.dp,
            color = if (isFocused) {
                PrimaryBlue.copy(alpha = 0.5f)
            } else {
                if (isDarkTheme) Slate700 else Gray200
            }
        )
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            cursorBrush = SolidColor(PrimaryBlue),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = if (isDarkTheme) Color.White else Slate800,
                textDirection = TextDirection.Rtl
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDarkTheme) Gray500 else Gray400
                            )
                        }
                        innerTextField()
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = if (isFocused) PrimaryBlue else if (isDarkTheme) Gray500 else Gray400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )
    }
}

@Composable
fun EmptyVoucherList() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Inventory,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "حواله‌ای یافت نشد",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "لطفا فیلترها را تغییر دهید یا جستجوی دیگری انجام دهید",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun VoucherItem(voucher: VoucherDetail, isDarkTheme: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "rotation"
    )

    Surface(
        onClick = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(CornerXL),
        color = if (isDarkTheme) SurfaceVariantDark else Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, if (isDarkTheme) Slate700 else Gray100)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(PrimaryBlue, CircleShape)
                    )

                    Column {
                        Text(
                            text = voucher.trackingNumber,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = if (isDarkTheme) Color.White else Slate800
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "قبض باسکول:",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDarkTheme) Gray500 else Gray400
                            )

                            Text(
                                text = voucher.scaleReceiptNumber,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDarkTheme) Gray400 else Gray500
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(CornerM),
                        color = if (isDarkTheme) Blue900.copy(alpha = 0.2f) else Blue50
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = formatNumber(voucher.netWeight.toInt()),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )

                            Text(
                                text = "کیلوگرم",
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryBlue.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ExpandLess,
                        contentDescription = if (expanded) "بستن" else "باز کردن",
                        tint = Gray400,
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(rotationState)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                VoucherExpandedDetails(voucher, isDarkTheme)
            }
        }
    }
}

@Composable
fun VoucherExpandedDetails(voucher: VoucherDetail, isDarkTheme: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDarkTheme) Slate800.copy(alpha = 0.5f) else Gray50)
            .padding(16.dp),
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
                Text(
                    text = "ساعت ورود",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDarkTheme) Gray500 else Gray400
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isDarkTheme) Blue900.copy(alpha = 0.4f) else Blue100
                ) {
                    Text(
                        text = voucher.entryTime,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Blue300 else Blue700,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = persianDateFormat(voucher.exitDate),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Slate300 else Slate700
                )
                Surface(
                    shape = CircleShape,
                    color = if (isDarkTheme) Slate700 else Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ساعت خروج",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDarkTheme) Gray500 else Gray400
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isDarkTheme) Blue900.copy(alpha = 0.4f) else Blue100
                ) {
                    Text(
                        text = voucher.exitTime,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Blue300 else Blue700,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        HorizontalDivider(
            color = if (isDarkTheme) Slate700 else Gray200,
            thickness = 1.dp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = PrimaryBlue.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "تاییدکننده",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = voucher.confirmUsername ?: "-",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Slate200 else Slate700
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = PrimaryBlue.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "باسکولچی",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = voucher.username ?: "-",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Slate200 else Slate700
                )
            }
        }
    }
}

enum class VoucherSortType(val persianName: String) {
    DATE_DESC("تاریخ نزولی"),
    DATE_ASC("تاریخ صعودی"),
    WEIGHT_DESC("وزن نزولی"),
    WEIGHT_ASC("وزن صعودی")
}

@Composable
fun SortChip(
    type: VoucherSortType,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val icon = when (type) {
        VoucherSortType.DATE_ASC -> Icons.Default.ArrowUpward
        VoucherSortType.DATE_DESC -> Icons.Default.ArrowDownward
        VoucherSortType.WEIGHT_ASC -> Icons.AutoMirrored.Filled.TrendingUp
        VoucherSortType.WEIGHT_DESC -> Icons.AutoMirrored.Filled.TrendingDown
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(CornerL),
        color = if (isSelected) PrimaryBlue else if (isDarkTheme) SurfaceVariantDark else Color.White,
        shadowElevation = if (isSelected) 4.dp else 2.dp,
        border = if (!isSelected) BorderStroke(1.dp, if (isDarkTheme) Slate700 else Color.Transparent) else null,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = type.persianName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else if (isDarkTheme) Slate300 else Slate600
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else if (isDarkTheme) Slate300 else Slate600,
                modifier = Modifier.size(16.dp)
            )
        }
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
    val isDarkTheme = isSystemInDarkTheme()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(Corner2XL),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = warehouse.name,
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
                            text = formatNumber(totalTonnage.toInt()),
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
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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
        }
    }
}
