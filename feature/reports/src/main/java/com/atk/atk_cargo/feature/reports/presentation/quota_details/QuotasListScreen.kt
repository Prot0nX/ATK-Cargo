package com.atk.atk_cargo.feature.reports.presentation.quota_details

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.atk.atk_cargo.data.model.GroupSortingMode
import com.atk.atk_cargo.data.model.Quota
import com.atk.atk_cargo.data.model.QuotaEditData
import com.atk.atk_cargo.data.model.QuotaSortingMode
import com.atk.atk_cargo.data.model.WarehouseQuotaGroupingMode
import com.atk.atk_cargo.feature.reports.domain.buildQuotasShareText
import com.atk.atk_cargo.feature.reports.domain.persianCollator
import com.atk.atk_cargo.feature.reports.domain.shareQuotasData
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
import com.atk.atk_cargo.feature.reports.viewmodel.ReportsViewModel
import kotlinx.coroutines.flow.StateFlow

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

internal val QuotaPurpleAccent: Color
    @Composable get() = if (isSystemInDarkTheme()) Purple200 else Purple700

internal val QuotaRedAccent: Color
    @Composable get() = if (isSystemInDarkTheme()) Red400 else Red500

internal val QuotaRedDeleteAccent: Color
    @Composable get() = if (isSystemInDarkTheme()) Red400 else Red900

internal val QuotaGreenAccent: Color
    @Composable get() = if (isSystemInDarkTheme()) Green300 else Green600

internal val QuotaGreenActionAccent: Color
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
                    // به‌جای نگاشت cargoOwner تهی به کلید null، برچسب صریح "نامشخص" مطابق قرارداد سرور در getGroupedQuotas استفاده می‌شود تا تداخل کلید پیش نیاید
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
                    // sortedBy معمولی ترتیب کدپوینت یونیکد را می‌دهد نه الفبای فارسی، پس از persianCollator استفاده می‌شود
                    groupedMap.entries.sortedWith(compareBy(persianCollator) { it.key ?: "" })
                }
                GroupSortingMode.REMAINING_TONNAGE_ASC, GroupSortingMode.REMAINING_TONNAGE_DESC -> {
                    // مجموع مانده‌ی هر گروه یک‌بار از پیش محاسبه می‌شود تا در هر مقایسه‌ی sort تکرار نشود
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

// پنل گروه‌بندی در QuotaGroupContent.kt و کارت/دیالوگ ویرایش کوتاژ در QuotaCardComponents.kt هستند (شکستن God Composable)
