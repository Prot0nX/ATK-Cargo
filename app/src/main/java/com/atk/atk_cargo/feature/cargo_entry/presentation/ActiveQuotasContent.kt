package com.atk.atk_cargo.feature.cargo_entry.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atk.atk_cargo.api.ActiveShipInfo
import com.atk.atk_cargo.api.formatNumber

// محتوای دیالوگ «کوتاژهای فعال» (لیست/کارت/آیتم) — از SelectInfoScreen.kt به
// این فایل منتقل شد (DEEP_CODE_AUDIT.md #Phase3.7، شکستن God Composable).
// پوسته‌ی دیالوگ (Header/FilterBar/AnimatedContent) در ActiveQuotasDialogSection.kt
// است؛ رنگ‌های Quotas* در SelectInfoScreen.kt مانده‌اند چون توسط هر دو فایل
// مصرف می‌شوند. هم‌پکیج است، پس نیازی به import اضافه در فایل‌های دیگر نیست.

enum class FilterState {
    ALL, PENDING, COMPLETED
}

enum class ViewMode {
    GROUPED, FLAT
}

@Composable
internal fun GroupedShipsContent(
    groupedShips: Map<String, List<ActiveShipInfo>>,
    searchQuery: String,
    filterState: FilterState,
    expandedShipName: String?,
    onExpandShip: (String) -> Unit
) {
    // فیلتر کردن کشتی‌ها با توجه به جستجو و وضعیت فیلتر
    val filteredShips = groupedShips.entries.filter { (shipName, ships) ->
        // فیلتر کردن کشتی‌هایی که حداقل یک ورود یا خروج دارند
        val hasActivity = ships.any { it.entryVouchers + it.exitVouchers > 0 }

        // فیلتر بر اساس متن جستجو
        val matchesSearch = searchQuery.isEmpty() ||
                shipName.contains(searchQuery, ignoreCase = true) ||
                ships.any {
                    it.loadingWarehouse.contains(searchQuery, ignoreCase = true) ||
                            it.loadingQuotaNumber.contains(searchQuery, ignoreCase = true)
                }

        // فیلتر بر اساس وضعیت تکمیل
        val matchesFilter = when (filterState) {
            FilterState.ALL -> true
            FilterState.PENDING -> ships.any { ship ->
                val totalVouchers = ship.entryVouchers + ship.exitVouchers
                totalVouchers > 0 && ship.exitVouchers < totalVouchers
            }
            FilterState.COMPLETED -> ships.all { ship ->
                val totalVouchers = ship.entryVouchers + ship.exitVouchers
                totalVouchers == 0 || ship.exitVouchers == totalVouchers
            }
        }

        hasActivity && matchesSearch && matchesFilter
    }

    if (filteredShips.isEmpty()) {
        // نمایش حالت خالی بودن نتایج
        EmptySearchResult(
            searchQuery = searchQuery,
            filterState = filterState
        )
    } else {
        // نمایش لیست کشتی‌های فیلتر شده
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // مرتب‌سازی کشتی‌ها بر اساس تعداد حواله‌های باقیمانده (نزولی)
            items(
                items = filteredShips.sortedWith(
                    compareByDescending<Map.Entry<String, List<ActiveShipInfo>>> { (_, ships) ->
                        val total = ships.sumOf { it.entryVouchers + it.exitVouchers }
                        val completed = ships.sumOf { it.exitVouchers }
                        total - completed  // حواله‌های باقیمانده
                    }.thenByDescending { (_, ships) ->
                        ships.sumOf { it.entryVouchers + it.exitVouchers }  // کل حواله‌ها
                    }
                ).toList(),
                key = { it.key }
            ) { (shipName, ships) ->
                ShipCard(
                    shipName = shipName,
                    ships = ships,
                    expanded = expandedShipName == shipName,
                    onExpandChange = { onExpandShip(shipName) },
                    searchQuery = searchQuery
                )
            }
        }
    }
}

@Composable
private fun EmptySearchResult(
    searchQuery: String,
    filterState: FilterState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (searchQuery.isNotEmpty())
                "نتیجه‌ای برای \"$searchQuery\" یافت نشد"
            else when(filterState) {
                FilterState.PENDING -> "هیچ کوتاژ در حال انجامی یافت نشد"
                FilterState.COMPLETED -> "هیچ کوتاژ تکمیل شده‌ای یافت نشد"
                else -> "هیچ کوتاژی یافت نشد"
            },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "لطفاً جستجو یا فیلتر را تغییر دهید",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ShipCard(
    shipName: String,
    ships: List<ActiveShipInfo>,
    expanded: Boolean,
    onExpandChange: () -> Unit,
    searchQuery: String
) {
    val totalVouchers = ships.sumOf { it.entryVouchers + it.exitVouchers }
    val completedVouchers = ships.sumOf { it.exitVouchers }
    val remainingVouchers = totalVouchers - completedVouchers
    val totalNetWeight = ships.sumOf { it.totalNetWeight }
    val progressPercentage = if (totalVouchers > 0) {
        (completedVouchers.toFloat() / totalVouchers) * 100f
    } else 0f

    // گروه‌بندی بر اساس انبار
    val warehouseGroups = ships.groupBy { it.loadingWarehouse }

    // حفظ وضعیت باز/بسته بودن هر انبار
    var expandedWarehouse by remember { mutableStateOf<String?>(null) }

    val cardColor = when {
        progressPercentage >= 100f -> QuotasAccentBg
        progressPercentage >= 75f -> QuotasAccentBg.copy(alpha = 0.6f)
        progressPercentage >= 50f -> QuotasWarningBg
        else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
    }

    val borderColor = when {
        progressPercentage >= 100f -> QuotasAccentBorder
        progressPercentage >= 75f -> QuotasAccentBorder
        progressPercentage >= 50f -> QuotasWarningBorder
        else -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
    }

    val onBackgroundColor = when {
        progressPercentage >= 100f -> QuotasAccent
        progressPercentage >= 75f -> QuotasAccent
        progressPercentage >= 50f -> QuotasWarning
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandChange() }
            .alpha(if (ships.all { it.entryVouchers + it.exitVouchers == 0 }) 0.7f else 1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(width = 1.dp, color = borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // سربرگ کشتی با طراحی جدید
            ShipHeader(
                shipName = shipName,
                cargoType = ships.firstOrNull()?.cargoType ?: "",
                quotaCount = ships.count { it.entryVouchers + it.exitVouchers > 0 },
                totalVouchers = totalVouchers,
                completedVouchers = completedVouchers,
                remainingVouchers = remainingVouchers,
                progressPercentage = progressPercentage,
                totalNetWeight = totalNetWeight,
                expanded = expanded,
                onBackgroundColor = onBackgroundColor
            )

            // نمایش محتوای گروه‌بندی شده کشتی وقتی باز است
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // نمایش انبارها به ترتیب نزولی حواله‌های باقیمانده
                    warehouseGroups.entries
                        .filter { (_, ships) -> ships.any { it.entryVouchers + it.exitVouchers > 0 } }
                        .sortedWith(
                            compareByDescending<Map.Entry<String, List<ActiveShipInfo>>> { (_, ships) ->
                                val total = ships.sumOf { it.entryVouchers + it.exitVouchers }
                                val completed = ships.sumOf { it.exitVouchers }
                                total - completed  // حواله‌های باقیمانده
                            }.thenByDescending { (_, ships) ->
                                ships.sumOf { it.entryVouchers + it.exitVouchers }  // کل حواله‌ها
                            }
                        )
                        .forEach { (warehouseName, warehouseShips) ->
                            // فیلتر کردن بر اساس متن جستجو
                            val filteredShips = if (searchQuery.isEmpty()) {
                                warehouseShips.filter { it.entryVouchers + it.exitVouchers > 0 }
                            } else {
                                warehouseShips.filter {
                                    it.entryVouchers + it.exitVouchers > 0 &&
                                            (it.loadingWarehouse.contains(searchQuery, ignoreCase = true) ||
                                                    it.loadingQuotaNumber.contains(searchQuery, ignoreCase = true))
                                }
                            }

                            if (filteredShips.isNotEmpty()) {
                                WarehouseSection(
                                    warehouseName = warehouseName,
                                    ships = filteredShips,
                                    expanded = expandedWarehouse == warehouseName,
                                    onExpandChange = { isExpanded ->
                                        expandedWarehouse = if (isExpanded) warehouseName else null
                                    },
                                    searchQuery = searchQuery
                                )
                            }
                        }
                }
            }
        }
    }
}

@Composable
private fun ShipHeader(
    shipName: String,
    cargoType: String,
    quotaCount: Int,
    totalVouchers: Int,
    completedVouchers: Int,
    remainingVouchers: Int,
    progressPercentage: Float,
    totalNetWeight: Int,
    expanded: Boolean,
    onBackgroundColor: Color
) {
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "rotation"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // آیکون کشتی با طراحی جدید
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        color = onBackgroundColor.copy(alpha = 0.2f)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBoat,
                    contentDescription = null,
                    tint = onBackgroundColor,
                    modifier = Modifier.size(32.dp)
                )

                // نمایش درصد پیشرفت دور آیکون
                if (totalVouchers > 0) {
                    CircularProgressIndicator(
                        progress = { progressPercentage / 100f },
                        modifier = Modifier.size(52.dp),
                        color = onBackgroundColor,
                        trackColor = onBackgroundColor.copy(alpha = 0.1f),
                        strokeWidth = 2.dp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // اطلاعات کشتی
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = shipName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = onBackgroundColor
                    )
                    if (cargoType.isNotBlank()) {
                        Text(
                            text = "|",
                            style = MaterialTheme.typography.titleMedium,
                            color = onBackgroundColor
                        )
                        Text(
                            text = cargoType,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = onBackgroundColor.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (quotaCount > 0) {
                        "$quotaCount کوتاژ | $completedVouchers از $totalVouchers حواله | ${formatNumber(totalNetWeight)} kg"
                    } else {
                        "بدون کوتاژ فعال"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = onBackgroundColor.copy(alpha = 0.7f)
                )
            }

            // نمایش تعداد حواله‌های باقیمانده
            if (remainingVouchers > 0) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "$remainingVouchers مانده",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else if (totalVouchers > 0) {
                // نمایش برچسب تکمیل شده برای کشتی‌هایی که همه حواله‌هایشان خروج شده
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = QuotasAccentBg,
                    border = BorderStroke(1.dp, QuotasAccentBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = QuotasAccent,
                            modifier = Modifier.size(12.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "تکمیل شده",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = QuotasAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // آیکون باز/بسته کردن
            Icon(
                imageVector = Icons.Default.ExpandLess,
                contentDescription = if (expanded) "بستن" else "باز کردن",
                tint = onBackgroundColor,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer { rotationZ = rotationState }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // نوار پیشرفت با طراحی جدید
        LinearProgressIndicator(
            progress = { progressPercentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = onBackgroundColor,
            trackColor = onBackgroundColor.copy(alpha = 0.1f),
            strokeCap = StrokeCap.Round
        )

        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))

            // نمایش درصد پیشرفت به صورت متنی
            Text(
                text = "${progressPercentage.toInt()}% تکمیل شده",
                style = MaterialTheme.typography.bodySmall,
                color = onBackgroundColor.copy(alpha = 0.7f),
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
internal fun FlatQuotasContent(
    activeShips: List<ActiveShipInfo>,
    searchQuery: String,
    filterState: FilterState
) {
    // فیلتر کردن کوتاژها بر اساس جستجو و وضعیت
    val filteredQuotas = activeShips.filter { ship ->
        // فقط کوتاژهایی که حواله دارند نمایش داده شوند
        val hasVouchers = ship.entryVouchers + ship.exitVouchers > 0

        // فیلتر بر اساس متن جستجو
        val matchesSearch = searchQuery.isEmpty() ||
                ship.shipName.contains(searchQuery, ignoreCase = true) ||
                ship.loadingWarehouse.contains(searchQuery, ignoreCase = true) ||
                ship.loadingQuotaNumber.contains(searchQuery, ignoreCase = true)

        // فیلتر بر اساس وضعیت تکمیل
        val matchesFilter = when (filterState) {
            FilterState.ALL -> true
            FilterState.PENDING -> {
                val totalVouchers = ship.entryVouchers + ship.exitVouchers
                totalVouchers > 0 && ship.exitVouchers < totalVouchers
            }
            FilterState.COMPLETED -> {
                val totalVouchers = ship.entryVouchers + ship.exitVouchers
                totalVouchers > 0 && ship.exitVouchers == totalVouchers
            }
        }

        hasVouchers && matchesSearch && matchesFilter
    }

    if (filteredQuotas.isEmpty()) {
        // نمایش حالت خالی بودن نتایج
        EmptySearchResult(
            searchQuery = searchQuery,
            filterState = filterState
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // مرتب‌سازی بر اساس حواله‌های باقیمانده (نزولی)، سپس کشتی و انبار
            items(
                items = filteredQuotas.sortedWith(
                    compareByDescending<ActiveShipInfo> { ship ->
                        val total = ship.entryVouchers + ship.exitVouchers
                        val remaining = total - ship.exitVouchers
                        remaining  // حواله‌های باقیمانده
                    }.thenBy { it.shipName }
                        .thenBy { it.loadingWarehouse }
                ),
                key = { "${it.loadingQuotaNumber}|${it.shipName}|${it.loadingWarehouse}|${it.shippingCompany}|${it.cargoType}" }
            ) { quota ->
                FlatQuotaCard(
                    quota = quota,
                    searchQuery = searchQuery
                )
            }
        }
    }
}

@Composable
private fun FlatQuotaCard(
    quota: ActiveShipInfo,
    searchQuery: String
) {
    val totalVouchers = quota.entryVouchers + quota.exitVouchers
    val remainingVouchers = totalVouchers - quota.exitVouchers
    val isCompleted = remainingVouchers == 0

    val cardColor = when {
        isCompleted -> QuotasAccentBg.copy(alpha = 0.5f)
        remainingVouchers > 0 -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        else -> QuotasMutedBg.copy(alpha = 0.5f)
    }

    val borderColor = when {
        isCompleted -> QuotasAccentBorder
        remainingVouchers > 0 -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
        else -> QuotasCardBorder
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(width = 1.dp, color = borderColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // ردیف اول: کشتی و شماره کوتاژ
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // کشتی و شماره کوتاژ
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // آیکون کشتی
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                color = if (isCompleted)
                                    QuotasAccentBg
                                else if (remainingVouchers > 0)
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                else
                                    QuotasMutedBg
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBoat,
                            contentDescription = null,
                            tint = if (isCompleted)
                                QuotasAccent
                            else if (remainingVouchers > 0)
                                MaterialTheme.colorScheme.error
                            else
                                QuotasMutedText,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (searchQuery.isNotEmpty() && quota.shipName.contains(searchQuery, ignoreCase = true)) {
                                val parts = quota.shipName.split(
                                    searchQuery,
                                    ignoreCase = true
                                )
                                Row {
                                    for (i in parts.indices) {
                                        if (i > 0) {
                                            Text(
                                                text = searchQuery,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(
                                            text = parts[i],
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = quota.shipName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (quota.cargoType.isNotBlank()) {
                                Text(
                                    text = "|",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = quota.cargoType,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // شماره کوتاژ با هایلایت متن جستجو شده
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = "تکمیل شده",
                                    tint = QuotasAccent,
                                    modifier = Modifier.size(12.dp)
                                )

                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            if (searchQuery.isNotEmpty() && quota.loadingQuotaNumber.contains(searchQuery, ignoreCase = true)) {
                                val parts = quota.loadingQuotaNumber.split(
                                    searchQuery,
                                    ignoreCase = true
                                )
                                Row {
                                    Text(
                                        text = "کوتاژ: ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    for (i in parts.indices) {
                                        if (i > 0) {
                                            Text(
                                                text = searchQuery,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(
                                            text = parts[i],
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "کوتاژ: ${quota.loadingQuotaNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // آمار حواله‌ها
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    // نمایش برچسب وضعیت
                    if (isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = QuotasAccentBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "تکمیل شده",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = QuotasAccent
                                )
                            }
                        }
                    } else if (remainingVouchers > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "$remainingVouchers مانده",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // آمار حواله‌های خروج شده و کل
                    Text(
                        text = "${quota.exitVouchers}/$totalVouchers حواله",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCompleted)
                            QuotasAccent
                        else
                            QuotasMutedText
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // وزن خالص خروج شده (تناژ خروجی)
                    Text(
                        text = "${formatNumber(quota.totalNetWeight)} kg",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted)
                            QuotasAccent
                        else
                            QuotasMutedText.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ردیف دوم: انبار و نوار پیشرفت
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // آیکون انبار
                Icon(
                    imageVector = Icons.Default.Warehouse,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // نام انبار با هایلایت متن جستجو شده
                if (searchQuery.isNotEmpty() && quota.loadingWarehouse.contains(searchQuery, ignoreCase = true)) {
                    val parts = quota.loadingWarehouse.split(
                        searchQuery,
                        ignoreCase = true
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        for (i in parts.indices) {
                            if (i > 0) {
                                Text(
                                    text = searchQuery,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = parts[i],
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Text(
                        text = quota.loadingWarehouse,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // نوار پیشرفت با درصد
                if (totalVouchers > 0) {
                    val progressPercentage = (quota.exitVouchers.toFloat() / totalVouchers) * 100f

                    Text(
                        text = "${progressPercentage.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCompleted)
                            QuotasAccent
                        else if (remainingVouchers > 0)
                            MaterialTheme.colorScheme.error
                        else
                            QuotasMutedText,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // نوار پیشرفت با حالت گرادیانت
            if (totalVouchers > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(QuotasMutedBg)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = quota.exitVouchers.toFloat() / totalVouchers)
                            .background(
                                brush = if (isCompleted) {
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            QuotasAccent.copy(alpha = 0.7f),
                                            QuotasAccent
                                        )
                                    )
                                } else if (remainingVouchers > 0) {
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            MaterialTheme.colorScheme.error
                                        )
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            )
                    )
                }
            }
        }
    }
}

private fun extractLastDigits(quotaNumber: String): String {
    return if (quotaNumber.length > 4) {
        quotaNumber.takeLast(4)
    } else {
        quotaNumber
    }
}

@Composable
private fun WarehouseSection(
    warehouseName: String,
    ships: List<ActiveShipInfo>,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    searchQuery: String
) {
    // حذف کشتی‌هایی که حواله ندارند
    val shipsWithVouchers = ships.filter { it.entryVouchers + it.exitVouchers > 0 }

    // اگر هیچ کشتی‌ای حواله نداشته باشد، چیزی نمایش نمی‌دهیم
    if (shipsWithVouchers.isEmpty()) return

    val totalVouchers = shipsWithVouchers.sumOf { it.entryVouchers + it.exitVouchers }
    val completedVouchers = shipsWithVouchers.sumOf { it.exitVouchers }
    val remainingVouchers = totalVouchers - completedVouchers
    val totalNetWeight = shipsWithVouchers.sumOf { it.totalNetWeight }
    val isCompleted = totalVouchers > 0 && remainingVouchers == 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandChange(!expanded) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else if (remainingVouchers > 0)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isCompleted)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            else if (remainingVouchers > 0)
                MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // اطلاعات انبار
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                color = if (isCompleted)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else if (remainingVouchers > 0)
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                    ) {
                        // آیکون مناسب با وضعیت انبار
                        Icon(
                            imageVector = if (isCompleted)
                                Icons.Outlined.CheckCircle
                            else
                                Icons.Default.Warehouse,
                            contentDescription = null,
                            tint = if (isCompleted)
                                MaterialTheme.colorScheme.primary
                            else if (remainingVouchers > 0)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        // نام انبار با هایلایت کردن متن جستجو شده
                        if (searchQuery.isNotEmpty() && warehouseName.contains(searchQuery, ignoreCase = true)) {
                            val parts = warehouseName.split(
                                searchQuery,
                                ignoreCase = true
                            )
                            Row {
                                for (i in parts.indices) {
                                    if (i > 0) {
                                        Text(
                                            text = searchQuery,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = parts[i],
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = warehouseName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // وضعیت تکمیل انبار
                        if (isCompleted) {
                            Text(
                                text = "تکمیل شده",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // آمار حواله‌ها و وزن انبار
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // نمایش تعداد حواله‌های باقیمانده
                    if (remainingVouchers > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "$remainingVouchers مانده",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // نمایش آمار عددی و وزن انبار
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$completedVouchers",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "/$totalVouchers",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "${formatNumber(totalNetWeight)} kg",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // آیکون باز/بسته کردن
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.Info,
                        contentDescription = if (expanded) "بستن" else "جزئیات بیشتر",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(16.dp)
                    )
                }
            }

            // نوار پیشرفت انبار
            if (totalVouchers > 0) {
                val progressPercentage = (completedVouchers.toFloat() / totalVouchers) * 100f

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progressPercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (isCompleted)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // لیست کوتاژها
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // مرتب‌سازی کوتاژها بر اساس حواله‌های باقیمانده (نزولی) و سپس بر اساس کل حواله‌ها
                    shipsWithVouchers
                        .sortedWith(
                            compareByDescending<ActiveShipInfo> { ship ->
                                val total = ship.entryVouchers + ship.exitVouchers
                                val remaining = total - ship.exitVouchers
                                remaining  // حواله‌های باقیمانده
                            }.thenByDescending { ship ->
                                ship.entryVouchers + ship.exitVouchers  // کل حواله‌ها
                            }
                        )
                        .forEach { ship ->
                            QuotaItem(
                                quota = ship,
                                searchQuery = searchQuery
                            )
                        }
                }
            }
        }
    }
}

@Composable
private fun QuotaItem(
    quota: ActiveShipInfo,
    searchQuery: String
) {
    val totalVouchers = quota.entryVouchers + quota.exitVouchers

    // اگر حواله نداشته باشد، نمایش نمی‌دهیم
    if (totalVouchers == 0) return

    val remainingVouchers = totalVouchers - quota.exitVouchers
    val isCompleted = remainingVouchers == 0
    val quotaNumber = quota.loadingQuotaNumber

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else if (remainingVouchers > 0)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // نمایش شماره کوتاژ با هایلایت اگر جستجو شده باشد
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.width(70.dp)
            ) {
                // نمایش آیکون تیک برای کوتاژهای تکمیل شده
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "تکمیل شده",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))
                }

                // هایلایت متن جستجو شده در شماره کوتاژ
                val shortQuotaNumber = extractLastDigits(quotaNumber)
                if (searchQuery.isNotEmpty() && quotaNumber.contains(searchQuery, ignoreCase = true)) {
                    val parts = quotaNumber.split(
                        searchQuery,
                        ignoreCase = true
                    )
                    Row {
                        for (i in parts.indices) {
                            if (i > 0) {
                                Text(
                                    text = searchQuery,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = parts[i],
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted)
                                    MaterialTheme.colorScheme.primary
                                else if (remainingVouchers > 0)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    Text(
                        text = shortQuotaNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted)
                            MaterialTheme.colorScheme.primary
                        else if (remainingVouchers > 0)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // نوار پیشرفت با حالت گرادیانت
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = quota.exitVouchers.toFloat() / totalVouchers)
                        .background(
                            brush = if (isCompleted) {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                        MaterialTheme.colorScheme.primary
                                    )
                                )
                            } else if (remainingVouchers > 0) {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                        MaterialTheme.colorScheme.error
                                    )
                                )
                            } else {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // نمایش آمار حواله‌ها و وزن خالص خروج شده
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // آمار حواله‌ها
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // تعداد حواله‌های باقیمانده
                    if (remainingVouchers > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "$remainingVouchers",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // آمار کلی حواله‌ها
                    Text(
                        text = "${quota.exitVouchers}/$totalVouchers",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCompleted)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // وزن خالص خروج شده (تناژ خروجی)
                Text(
                    text = "${formatNumber(quota.totalNetWeight)} kg",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
internal fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) containerColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) containerColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(containerColor)
                )

                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) containerColor else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}
