package com.atk.atk_cargo.feature.cargo_entry.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.atk.atk_cargo.data.model.ActiveShipInfo

// محتوای دیالوگ «کوتاژهای فعال»؛ نمای گروه‌بندی‌شده و نمای مسطح برای کاهش حجم فایل به فایل‌های جداگانه منتقل شده‌اند و اینجا فقط پوسته‌ی مشترک دو نما باقی مانده است

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
    // فیلتر+مرتب‌سازی در remember نگه داشته می‌شود تا در هر recomposition (مثلاً تایپ در جستجو) دوباره اجرا نشود
    val filteredShips = remember(groupedShips, searchQuery, filterState) {
        groupedShips.entries.filter { (shipName, ships) ->
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
        }.sortedWith(
            // مرتب‌سازی کشتی‌ها بر اساس تعداد حواله‌های باقیمانده (نزولی)
            compareByDescending<Map.Entry<String, List<ActiveShipInfo>>> { (_, ships) ->
                val total = ships.sumOf { it.entryVouchers + it.exitVouchers }
                val completed = ships.sumOf { it.exitVouchers }
                total - completed  // حواله‌های باقیمانده
            }.thenByDescending { (_, ships) ->
                ships.sumOf { it.entryVouchers + it.exitVouchers }  // کل حواله‌ها
            }
        )
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
            items(
                items = filteredShips,
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
internal fun FlatQuotasContent(
    activeShips: List<ActiveShipInfo>,
    searchQuery: String,
    filterState: FilterState
) {
    // فیلتر+مرتب‌سازی در remember نگه داشته می‌شود تا در هر recomposition دوباره اجرا نشود
    val filteredQuotas = remember(activeShips, searchQuery, filterState) {
        activeShips.filter { ship ->
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
        }.sortedWith(
            // مرتب‌سازی بر اساس حواله‌های باقیمانده (نزولی)، سپس کشتی و انبار
            compareByDescending<ActiveShipInfo> { ship ->
                val total = ship.entryVouchers + ship.exitVouchers
                val remaining = total - ship.exitVouchers
                remaining  // حواله‌های باقیمانده
            }.thenBy { it.shipName }
                .thenBy { it.loadingWarehouse }
        )
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
            items(
                items = filteredQuotas,
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

fun formatNumber(number: Number): String {
    return java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(number)
}
