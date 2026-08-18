package com.atk.atk_cargo.feature.reports.presentation.quota_details

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.atk.atk_cargo.feature.reports.domain.persianCollator
import com.atk.atk_cargo.feature.reports.domain.shareQuotasData
import com.atk.atk_cargo.feature.reports.presentation.quota_details.components.DeleteQuotaDialog
import com.atk.atk_cargo.feature.reports.presentation.quota_details.components.ToggleQuotaStatusDialog
import com.atk.atk_cargo.ui.theme.CornerL
import com.atk.atk_cargo.ui.theme.DeepOrange100
import com.atk.atk_cargo.ui.theme.DeepOrange300
import com.atk.atk_cargo.ui.theme.DeepOrange900
import com.atk.atk_cargo.ui.theme.Green300
import com.atk.atk_cargo.ui.theme.Green600
import com.atk.atk_cargo.ui.theme.Green700
import com.atk.atk_cargo.ui.theme.Purple200
import com.atk.atk_cargo.ui.theme.Purple700
import com.atk.atk_cargo.ui.theme.Red400
import com.atk.atk_cargo.ui.theme.Red500
import com.atk.atk_cargo.ui.theme.Red900
import com.atk.atk_cargo.ui.viewmodel.ReportsViewModel
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

// internal (نه private) چون QuotaPercentageDialogSection.kt هم به این پالت نیاز دارد
internal val QuotaTealAccent: Color
    @Composable get() = MaterialTheme.colorScheme.primary

internal val QuotaTealAccentBg: Color
    @Composable get() = MaterialTheme.colorScheme.primaryContainer

internal val QuotaOnTealAccent: Color
    @Composable get() = MaterialTheme.colorScheme.onPrimary

val QuotaDeepOrangeAccent: Color
    @Composable get() = if (isSystemInDarkTheme()) DeepOrange300 else DeepOrange900

val QuotaDeepOrangeAccentBg: Color
    @Composable get() = if (isSystemInDarkTheme()) DeepOrange900.copy(alpha = 0.18f) else DeepOrange100.copy(alpha = 0.6f)

private val QuotaPurpleAccent: Color
    @Composable get() = if (isSystemInDarkTheme()) Purple200 else Purple700

private val QuotaRedAccent: Color
    @Composable get() = if (isSystemInDarkTheme()) Red400 else Red500

private val QuotaRedDeleteAccent: Color
    @Composable get() = if (isSystemInDarkTheme()) Red400 else Red900

private val QuotaGreenAccent: Color
    @Composable get() = if (isSystemInDarkTheme()) Green300 else Green600

private val QuotaGreenActionAccent: Color
    @Composable get() = if (isSystemInDarkTheme()) Green300 else Green700

@SuppressLint("MutableCollectionMutableState")
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
    val currentGroupingMode by groupingMode.collectAsStateWithLifecycle()
    val currentSortingMode by viewModel.quotaSortingMode.collectAsStateWithLifecycle()
    val currentGroupSortingMode by viewModel.groupSortingMode.collectAsStateWithLifecycle()
    val isMinimalMode by viewModel.isMinimalQuotaMode.collectAsStateWithLifecycle()
    val reportsUiState by viewModel.uiState.collectAsStateWithLifecycle()
    var expandedGroup by rememberSaveable { mutableStateOf<String?>(null) }

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isQuotaSortingSelected = currentSortingMode == QuotaSortingMode.REMAINING_TONNAGE_ASC || currentSortingMode == QuotaSortingMode.REMAINING_TONNAGE_DESC
            val quotaSortingIcon = if (currentSortingMode == QuotaSortingMode.REMAINING_TONNAGE_ASC) {
                Icons.Default.ArrowUpward
            } else {
                Icons.Default.ArrowDownward
            }
            val isGroupSortingSelected = currentGroupSortingMode == GroupSortingMode.REMAINING_TONNAGE_ASC || currentGroupSortingMode == GroupSortingMode.REMAINING_TONNAGE_DESC
            val groupSortingIcon = if (currentGroupSortingMode == GroupSortingMode.REMAINING_TONNAGE_ASC) {
                Icons.Default.ArrowUpward
            } else {
                Icons.Default.ArrowDownward
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SortPill(
                    text = "مانده کوتاژ",
                    icon = quotaSortingIcon,
                    isSelected = isQuotaSortingSelected,
                    onClick = {
                        val newMode = if (currentSortingMode == QuotaSortingMode.REMAINING_TONNAGE_ASC) {
                            QuotaSortingMode.REMAINING_TONNAGE_DESC
                        } else {
                            QuotaSortingMode.REMAINING_TONNAGE_ASC
                        }
                        viewModel.setQuotaSortingMode(newMode)
                    }
                )
                SortPill(
                    text = "مانده گروه",
                    icon = groupSortingIcon,
                    isSelected = isGroupSortingSelected,
                    onClick = {
                        val newMode = if (currentGroupSortingMode == GroupSortingMode.REMAINING_TONNAGE_ASC) {
                            GroupSortingMode.REMAINING_TONNAGE_DESC
                        } else {
                            GroupSortingMode.REMAINING_TONNAGE_ASC
                        }
                        viewModel.setGroupSortingMode(newMode)
                    }
                )
            }

            Surface(
                onClick = {
                    shareGroupedQuotas?.let { quotas ->
                        val shipName = reportsUiState.selectedShip?.name ?: ""
                        val shareText = buildQuotasShareText(quotas, shipName)
                        shareQuotasData(context, shareText)
                    }
                },
                modifier = Modifier.size(30.dp),
                shape = RoundedCornerShape(9.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "اشتراک‌گذاری اطلاعات",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        val selectedDateRange = reportsUiState.selectedDateRange
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
                    // cargoOwner ممکن است null باشد؛ نگاشت آن به کلید null یعنی
                    // کوتاژهای بدون صاحب کالا بدون هیچ خطایی از لیست حذف می‌شوند
                    // (groupName?.let در پایین) یا در صورت هم‌زمانی با گروه ""
                    // باعث تداخل کلید LazyColumn می‌شوند. برچسب صریح مطابق همان
                    // قراردادی است که سرور در getGroupedQuotas استفاده می‌کند.
                    when (currentGroupingMode) {
                        WarehouseQuotaGroupingMode.BY_SHIPPING_COMPANY -> it.shippingCompany
                        WarehouseQuotaGroupingMode.BY_CARGO_OWNER -> it.cargoOwner ?: "نامشخص"
                        WarehouseQuotaGroupingMode.BY_WAREHOUSE -> "${it.warehouse} | ${it.cargoOwner ?: "نامشخص"}"
                    }
                }
                .mapValues { (_, groupQuotas) ->
                    groupQuotas.sortedWith(
                        compareByDescending<Quota> { it.isActive }
                            .thenBy { quota ->
                                val remaining = calculateRemainingAfterPercentage(quota)
                                when (currentSortingMode) {
                                    QuotaSortingMode.REMAINING_TONNAGE_ASC -> remaining
                                    QuotaSortingMode.REMAINING_TONNAGE_DESC -> -remaining
                                }
                            }
                    )
                }

            val sortedEntries = when (currentGroupSortingMode) {
                GroupSortingMode.ALPHABETICAL -> {
                    // sortedBy روی String از ترتیب کد یونیکد استفاده می‌کند، نه
                    // ترتیب الفبایی فارسی؛ حروف مشابه عربی/فارسی ("ی"/"ي"،
                    // "ک"/"ك") در کدپوینت‌های دور از هم مرتب می‌شوند.
                    groupedMap.entries.sortedWith(compareBy(persianCollator) { it.key ?: "" })
                }
                GroupSortingMode.REMAINING_TONNAGE_ASC, GroupSortingMode.REMAINING_TONNAGE_DESC -> {
                    // مجموع مانده‌ی هر گروه یک‌بار محاسبه می‌شود، نه به ازای هر
                    // مقایسه‌ی sort (قبلاً groupedMap[groupName]?.sumOf {...}
                    // داخل کامپریتور بود: هم jump اضافی در Map، هم جمع تکراری).
                    val remainingTotals = groupedMap.mapValues { (_, groupQuotas) ->
                        groupQuotas.sumOf { calculateRemainingAfterPercentage(it).toDouble() }
                    }
                    if (currentGroupSortingMode == GroupSortingMode.REMAINING_TONNAGE_ASC) {
                        groupedMap.entries.sortedBy { (groupName, _) -> remainingTotals[groupName] ?: 0.0 }
                    } else {
                        groupedMap.entries.sortedByDescending { (groupName, _) -> remainingTotals[groupName] ?: 0.0 }
                    }
                }
            }

            val result = LinkedHashMap<String?, List<Quota>>().apply {
                sortedEntries.forEach { (key, value) ->
                    put(key, value)
                }
            }
            shareGroupedQuotas = result
            result
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
                        shipName = reportsUiState.selectedShip?.name ?: "",
                        isMinimalMode = isMinimalMode && currentGroupingMode == WarehouseQuotaGroupingMode.BY_CARGO_OWNER
                    )
                }
            }
        }
    }
}

@Composable
private fun SortPill(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(9.dp),
        color = if (isSelected) QuotaTealAccent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) QuotaOnTealAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) QuotaOnTealAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(11.dp)
            )
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
        QuotaTealAccent
    } else {
        QuotaTealAccent.copy(alpha = 0.5f)
    }
    val loadedTonnage = quota.loadedTonnage
    val remainingTonnage = quota.remainingTonnage

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandToggle(!isExpanded) },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (quota.isActive) {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                            tint = if (quota.isActive) QuotaGreenAccent else QuotaRedAccent,
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
                        Text(
                            text = "|",
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.4f)
                        )
                        Text(
                            text = "${formatNumber(quota.voucherCount)} حواله",
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "کل:",
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.6f)
                    )
                    Text(
                        text = formatNumber(quota.totalTonnage.toInt()),
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor,
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
                            color = contentColor
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = QuotaTealAccentBg.copy(alpha = if (quota.isActive) 1f else 0.5f)
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

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

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
                            color = QuotaTealAccent,
                            onClick = { showEditDialog = true }
                        )
                        ActionButton(
                            icon = Icons.Default.Build,
                            label = "درصد",
                            color = QuotaPurpleAccent,
                            onClick = { showPercentageDialog = true }
                        )
                        ActionButton(
                            icon = if (quota.isActive) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                            label = if (quota.isActive) "غیرفعال" else "فعال",
                            color = if (quota.isActive) QuotaRedAccent else QuotaGreenActionAccent,
                            onClick = { showToggleDialog = true }
                        )
                        ActionButton(
                            icon = Icons.Default.Delete,
                            label = "حذف",
                            color = QuotaRedDeleteAccent,
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
    val accentColor = if (quota.isActive) {
        QuotaTealAccent
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
            if (quota.isActive) QuotaTealAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    tint = if (quota.isActive) QuotaGreenAccent else QuotaRedAccent,
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

@Composable
private fun EditFieldColumn(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        content()
    }
}

@Composable
private fun EditFieldBox(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false,
    readOnly: Boolean = false,
    ltr: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val borderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            readOnly = readOnly || onClick != null,
            enabled = onClick == null,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = if (ltr) TextAlign.Left else TextAlign.Right
            ),
            keyboardOptions = keyboardOptions,
            cursorBrush = SolidColor(QuotaTealAccent)
        )
        trailing?.invoke()
    }
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
            shape = RoundedCornerShape(20.dp),
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

                EditFieldColumn(label = "شماره کوتاژ") {
                    EditFieldBox(
                        value = editedData.quotaNumber,
                        onValueChange = {
                            editedData = editedData.copy(quotaNumber = formatNumberString(it))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = !isValidQuotaNumber(editedData.quotaNumber)
                    )
                }
                if (!isValidQuotaNumber(editedData.quotaNumber)) {
                    Text(
                        "شماره کوتاژ باید حداقل 5 رقم باشد",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                EditFieldColumn(label = "نام کشتی") {
                    EditFieldBox(
                        value = editedData.shipName,
                        onValueChange = {
                            editedData = editedData.copy(shipName = formatNumberString(it).uppercase(Locale.ROOT))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                EditFieldColumn(label = "شرکت باربری") {
                    EditFieldBox(
                        value = editedData.shippingCompany,
                        onValueChange = {
                            editedData = editedData.copy(shippingCompany = formatNumberString(it))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EditFieldColumn(label = "انبار", modifier = Modifier.weight(1f)) {
                        EditFieldBox(
                            value = editedData.warehouse,
                            onValueChange = {
                                editedData = editedData.copy(warehouse = formatNumberString(it))
                            }
                        )
                    }

                    EditFieldColumn(label = "نوع کالا", modifier = Modifier.weight(1f)) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            EditFieldBox(
                                value = editedData.cargoType,
                                onValueChange = {},
                                onClick = { expanded = !expanded },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                                trailing = {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
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
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                EditFieldColumn(label = "تناژ کل") {
                    EditFieldBox(
                        value = formatNumberString(editedData.totalTonnage.toInt().toString()),
                        onValueChange = {
                            val newValue = formatNumberString(it).toIntOrNull() ?: editedData.totalTonnage.toInt()
                            editedData = editedData.copy(totalTonnage = newValue.toFloat())
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        ltr = true
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(13.dp),
                        border = BorderStroke(
                            width = 1.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                        )
                    ) {
                        Text("انصراف", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = {
                            if (isValidQuotaNumber(editedData.quotaNumber)) {
                                showConfirmationDialog = true
                            }
                        },
                        modifier = Modifier.weight(1.4f),
                        enabled = editedData != quotaData && isValidQuotaNumber(editedData.quotaNumber),
                        shape = RoundedCornerShape(13.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = QuotaTealAccent)
                    ) {
                        Text("تایید و ذخیره", fontWeight = FontWeight.Bold)
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
                shape = RoundedCornerShape(20.dp),
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
internal fun QuotaDialogHeader(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .background(color = QuotaTealAccentBg, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = QuotaTealAccent,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun EditQuotaDialogHeader(quotaNumber: String) {
    QuotaDialogHeader(
        icon = Icons.Default.Edit,
        title = "ویرایش اطلاعات کوتاژ",
        subtitle = "شماره کوتاژ: $quotaNumber"
    )
}

@Composable
private fun ConfirmationDialogHeader() {
    QuotaDialogHeader(
        icon = Icons.Default.Check,
        title = "تأیید ذخیره‌سازی",
        subtitle = "لطفاً تصمیم خود را تأیید کنید"
    )
}
