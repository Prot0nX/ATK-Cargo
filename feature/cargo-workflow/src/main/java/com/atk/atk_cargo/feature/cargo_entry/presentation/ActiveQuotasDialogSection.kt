package com.atk.atk_cargo.feature.cargo_entry.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atk.atk_cargo.data.model.ActiveShipInfo
import com.atk.atk_cargo.data.model.RealTimeLoadingData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

// بخش دیالوگ «کوتاژهای فعال» که از SelectInfoScreen.kt جدا شده و به قطعات internal هم‌پکیج آن وابسته است

@Composable
internal fun ActiveQuotasDialog(
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var realTimeData by remember { mutableStateOf<List<RealTimeLoadingData>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val convertedShips = remember(realTimeData) {
        realTimeData.map { data ->
            ActiveShipInfo(
                shipName = data.shipName,
                loadingWarehouse = data.loadingWarehouse,
                cargoType = data.cargoType ?: "",
                shippingCompany = data.shippingCompany,
                loadingQuotaNumber = data.loadingQuotaNumber,
                entryVouchers = data.entryVouchers,
                exitVouchers = data.exitVouchers,
                totalNetWeight = data.totalNetWeight
            )
        }
    }

    // دریافت داده‌ها از API
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            realTimeData = reportsRepository.getRealTimeLoadingData().data
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = "خطا در ارتباط با سرور: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // فیلتر کشتی‌ها برای نمایش فقط کشتی‌هایی که حداقل یک ورود یا خروج دارند
    val filteredShips = remember(convertedShips) {
        convertedShips.filter { it.entryVouchers + it.exitVouchers > 0 }
    }

    val groupedShips = remember(filteredShips) { filteredShips.groupBy { it.shipName } }
    val totalVouchers = remember(filteredShips) { filteredShips.sumOf { it.entryVouchers + it.exitVouchers } }
    val completedVouchers = remember(filteredShips) { filteredShips.sumOf { it.exitVouchers } }
    val totalNetWeight = remember(filteredShips) { filteredShips.sumOf { it.totalNetWeight } }

    // فیلتر وضعیت - پیش‌فرض "در حال انجام"
    var filterState by remember { mutableStateOf(FilterState.PENDING) }

    // انتخاب حالت نمایش - پیش فرض نمایش لیستی
    var viewMode by remember { mutableStateOf(ViewMode.FLAT) }

    // متغیر برای نگهداری کشتی باز شده
    var expandedShipName by remember { mutableStateOf<String?>(null) }

    // انیمیشن ورود دیالوگ
    var dialogVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        dialogVisible = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        val dialogScale by animateFloatAsState(
            targetValue = if (dialogVisible) 1f else 0.96f,
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            ),
            label = "dialog_scale"
        )

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = dialogScale
                    scaleY = dialogScale
                },
            color = QuotasScreenBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // سربرگ با دکمه‌ی بروزرسانی
                QuotasHeader(
                    totalVouchers = totalVouchers,
                    completedVouchers = completedVouchers,
                    totalNetWeight = totalNetWeight,
                    onDismiss = onDismiss,
                    viewMode = viewMode,
                    onViewModeChange = { viewMode = it },
                    isLoading = isLoading,
                    onRefresh = {
                        coroutineScope.launch {
                            try {
                                isLoading = true
                                realTimeData = reportsRepository.getRealTimeLoadingData().data
                                errorMessage = null
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                errorMessage = "خطا در ارتباط با سرور: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                )

                // نمایش خطا اگر وجود داشته باشد
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    errorMessage?.let {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // نوار فیلتر
                FilterBar(
                    filterState = filterState,
                    onFilterStateChange = { filterState = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // نمایش لودینگ
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "در حال دریافت اطلاعات...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // محتوای اصلی دیالوگ با حالت‌های مختلف نمایش
                    if (viewMode == ViewMode.GROUPED) {
                        // حالت گروه‌بندی شده بر اساس کشتی
                        GroupedShipsContent(
                            groupedShips = groupedShips,
                            searchQuery = "",
                            filterState = filterState,
                            expandedShipName = expandedShipName,
                            onExpandShip = { shipName ->
                                expandedShipName = if (expandedShipName == shipName) null else shipName
                            }
                        )
                    } else {
                        // حالت نمایش همه کوتاژها به صورت لیست
                        FlatQuotasContent(
                            activeShips = filteredShips,
                            searchQuery = "",
                            filterState = filterState
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuotasHeader(
    totalVouchers: Int,
    completedVouchers: Int,
    totalNetWeight: Int,
    onDismiss: () -> Unit,
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit,
    isLoading: Boolean,
    onRefresh: () -> Unit
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
                .background(QuotasAccentBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = null,
                tint = QuotasAccent,
                modifier = Modifier.size(16.dp)
            )
        }

        // عنوان و آمار
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "آمار بارگیری",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = QuotasTitleColor
            )

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(100),
                color = QuotasAccentBg
            ) {
                Text(
                    text = "${formatNumber(totalNetWeight)} kg",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = QuotasAccent,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        // دکمه‌های تغییر حالت نمایش
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(QuotasMutedBg)
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // دکمه نمایش گروه‌بندی شده
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (viewMode == ViewMode.GROUPED) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { onViewModeChange(ViewMode.GROUPED) }
                    .size(34.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = "نمایش گروه‌بندی شده",
                    tint = if (viewMode == ViewMode.GROUPED) QuotasAccent else QuotasMutedText,
                    modifier = Modifier.size(18.dp)
                )
            }

            // دکمه نمایش لیستی
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (viewMode == ViewMode.FLAT) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { onViewModeChange(ViewMode.FLAT) }
                    .size(34.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ViewList,
                    contentDescription = "نمایش لیستی",
                    tint = if (viewMode == ViewMode.FLAT) QuotasAccent else QuotasMutedText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // دکمه بروزرسانی
        if (!isLoading) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(QuotasMutedBg)
                    .clickable(onClick = onRefresh)
                    .size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "بروزرسانی",
                    tint = QuotasMutedText,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // دکمه بستن
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(QuotasMutedBg)
                .clickable(onClick = onDismiss)
                .size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "بستن",
                tint = QuotasTitleColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }

    // کارت آمار با طراحی جدید و کوچکتر
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, QuotasCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
        ) {
            // نوار پیشرفت با درصد
            val progressPercentage = if (totalVouchers > 0) {
                (completedVouchers.toFloat() / totalVouchers) * 100f
            } else 0f

            val progressColor = when {
                progressPercentage >= 90f -> QuotasAccent
                progressPercentage >= 60f -> QuotasWarning
                else -> MaterialTheme.colorScheme.error
            }

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
                        .fillMaxWidth(fraction = progressPercentage / 100f)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    progressColor.copy(alpha = 0.7f),
                                    progressColor
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // آمار در یک ردیف
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // کارت آمار کل حواله‌ها
                StatItem(
                    value = totalVouchers,
                    label = "کل حواله‌ها",
                    color = QuotasAccent
                )

                VerticalDivider(
                    modifier = Modifier.height(24.dp),
                    color = QuotasCardBorder
                )

                // کارت آمار حواله‌های خروجی
                StatItem(
                    value = completedVouchers,
                    label = "خروجی",
                    color = QuotasWarning
                )

                VerticalDivider(
                    modifier = Modifier.height(24.dp),
                    color = QuotasCardBorder
                )

                // کارت آمار حواله‌های باقیمانده
                StatItem(
                    value = totalVouchers - completedVouchers,
                    label = "باقیمانده",
                    color = MaterialTheme.colorScheme.error
                )

                if (progressPercentage > 0) {
                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        color = QuotasCardBorder
                    )

                    Text(
                        text = "${progressPercentage.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    value: Int,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = QuotasMutedText
        )
    }
}

@Composable
private fun FilterBar(
    filterState: FilterState,
    onFilterStateChange: (FilterState) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = filterState == FilterState.ALL,
            onClick = { onFilterStateChange(FilterState.ALL) },
            label = "همه",
            containerColor = QuotasTitleColor,
            modifier = Modifier.weight(1f)
        )

        FilterChip(
            selected = filterState == FilterState.PENDING,
            onClick = { onFilterStateChange(FilterState.PENDING) },
            label = "در حال انجام",
            containerColor = QuotasWarning,
            modifier = Modifier.weight(1f)
        )

        FilterChip(
            selected = filterState == FilterState.COMPLETED,
            onClick = { onFilterStateChange(FilterState.COMPLETED) },
            label = "تکمیل شده",
            containerColor = QuotasAccent,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun VerticalDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outline,
    thickness: Dp = 1.dp
) {
    Box(
        modifier = modifier
            .width(thickness)
            .background(color)
    )
}
