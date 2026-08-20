package com.atk.atk_cargo.feature.reports.presentation.dialogs

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.atk.atk_cargo.core.domain.AnimationManager
import com.atk.atk_cargo.data.model.RealTimeLoadingData
import com.atk.atk_cargo.data.model.ShiftInfo
import com.atk.atk_cargo.feature.reports.domain.formatNumber
import com.atk.atk_cargo.feature.reports.presentation.ships.SearchField
import com.atk.atk_cargo.ui.theme.Green300
import com.atk.atk_cargo.ui.theme.Green50
import com.atk.atk_cargo.ui.theme.Green700
import com.atk.atk_cargo.ui.theme.Red400
import com.atk.atk_cargo.ui.theme.Red50
import com.atk.atk_cargo.ui.theme.Red700
import com.atk.atk_cargo.ui.viewmodel.ReportsViewModel.RealTimeUiState
import kotlinx.coroutines.launch

// internal (نه private) چون RealTimeLoadingCardSection.kt هم به این‌ها نیاز دارد
internal val RealTimeAccent: Color
    @Composable get() = MaterialTheme.colorScheme.primary

internal val RealTimeAccentBg: Color
    @Composable get() = MaterialTheme.colorScheme.primaryContainer

internal val RealTimeAccentBorder: Color
    @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)

internal val RealTimeCardBorder: Color
    @Composable get() = MaterialTheme.colorScheme.outlineVariant

internal val RealTimeMutedBg: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

internal val RealTimeMutedText: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

internal val RealTimeTitleColor: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface

// C-1/C-2: این کامپوزبل نه ReportsViewModel می‌گیرد و نه منطق polling/تایمر
// خودش دارد — فقط RealTimeUiState (که کل چرخه‌ی داده/شمارنده/refresh/خطا را
// در ViewModel نگه می‌دارد) را رندر می‌کند و از طریق lambdaها عمل می‌کند؛
// برای تست‌پذیری/پیش‌نمایش بهتر و تا با چرخش صفحه ریست نشود.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealTimeLoadingBottomSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    uiState: RealTimeUiState,
    shiftOffset: Int,
    shipColorMap: Map<String, Color>,
    // caller این را داخل repeatOnLifecycle(RESUMED) اجرا می‌کند؛ suspend می‌ماند
    // تا لغو شود (پس‌زمینه رفتن اپ یا بسته‌شدن دیالوگ).
    onStartPolling: suspend () -> Unit,
    onShiftOffsetChange: (Int) -> Unit,
    // پیام خطای واقعی بعد از پایان درخواست را برمی‌گرداند (null یعنی موفق) تا
    // دکمه‌ی refresh دستی بر اساس نتیجه‌ی واقعی Toast نشان دهد.
    onManualRefresh: suspend () -> String?,
    onShare: (List<RealTimeLoadingData>, ShiftInfo) -> String
) {
    // وقتی بسته است نباید loadingData را پردازش کند؛ در غیر این صورت هر
    // آپدیت داده‌ی Real-Time این کامپوزبل را حتی وقتی روی صفحه نمایش داده
    // نمی‌شود بازترسیم می‌کند.
    if (!isOpen) return

    var expandedShip by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isCurrentShift = remember(shiftOffset) { shiftOffset == 0 }
    val shiftInfo = uiState.shiftInfo ?: ShiftInfo("", "", "", "", "")

    val filteredLoadingData = remember(uiState.data, searchQuery) {
        if (searchQuery.isBlank()) {
            uiState.data
        } else {
            uiState.data.filter { data ->
                data.shipName.contains(searchQuery, ignoreCase = true) ||
                        data.loadingWarehouse.contains(searchQuery, ignoreCase = true) ||
                        data.loadingQuotaNumber.contains(searchQuery, ignoreCase = true) ||
                        data.shippingCompany.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // آمار سربرگ (StatisticItem) عمداً از filteredLoadingData محاسبه می‌شود، نه
    // uiState.data خام؛ در غیر این صورت با جستجو تعداد کارت‌ها کم می‌شود اما وزن
    // کل/تعداد ورودی-خروجی ثابت می‌ماند و با آنچه کاربر می‌بیند ناسازگار است.
    val totalEntryVouchers = remember(filteredLoadingData, isCurrentShift) {
        if (isCurrentShift) filteredLoadingData.sumOf { it.entryVouchers } else 0
    }
    val totalExitVouchers = remember(filteredLoadingData) { filteredLoadingData.sumOf { it.exitVouchers } }
    val totalNetWeight = remember(filteredLoadingData) { filteredLoadingData.sumOf { it.totalNetWeight.toDouble() }.toFloat() }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    LaunchedEffect(true) {
        if (isOpen) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                onStartPolling()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    DialogHeader(
                        loadingDataCount = filteredLoadingData.size,
                        isRefreshing = uiState.isRefreshing,
                        refreshProgress = uiState.secondsToNextRefresh / 30f,
                        onRefreshClick = {
                            if (!uiState.isRefreshing) {
                                scope.launch {
                                    // Toast بر اساس مقدار واقعی برگشتی از onManualRefresh (بعد از
                                    // پایان درخواست) نمایش داده می‌شود، نه بی‌قید و شرط. isRefreshing
                                    // و شمارنده خودشان از uiState (که ViewModel به‌روز می‌کند) می‌آیند.
                                    val error = onManualRefresh()
                                    if (error != null) {
                                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "اطلاعات بروزرسانی شد", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        onShareClick = {
                            val shareText = onShare(filteredLoadingData, shiftInfo)
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "اشتراک‌گذاری")
                            context.startActivity(shareIntent)
                        }
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(6.dp))

                        SearchField(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        RealTimeShiftNavigation(
                            shiftOffset = shiftOffset,
                            shiftInfo = shiftInfo,
                            onShiftOffsetChange = onShiftOffsetChange
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        StatisticItem(
                            totalEntryVouchers = totalEntryVouchers,
                            totalExitVouchers = totalExitVouchers,
                            totalNetWeight = totalNetWeight,
                            showEntry = isCurrentShift
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "کشتی‌های فعال",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = RealTimeMutedText
                            )
                            Surface(
                                shape = RoundedCornerShape(100),
                                color = RealTimeAccentBg
                            ) {
                                Text(
                                    text = "${filteredLoadingData.groupBy { it.shipName }.size} مورد",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = RealTimeAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        val groupedLoadingData = remember(filteredLoadingData) {
                            val cargoTypeCounts = filteredLoadingData
                                .groupBy { it.cargoType ?: "نامشخص" }
                                .mapValues { it.value.map { data -> data.shipName }.distinct().size }

                            filteredLoadingData.groupBy { "${it.cargoType ?: "نامشخص"} | ${it.shipName}" }
                                .toList()
                                .sortedWith(
                                    compareByDescending<Pair<String, List<RealTimeLoadingData>>> { (_, shipData) ->
                                        cargoTypeCounts[shipData.first().cargoType ?: "نامشخص"] ?: 0
                                    }.thenBy { it.first }
                                )
                        }

                        // AnimatedContent قبلاً روی کل لیست بود؛ هر آپدیت داده (هر polling
                        // ۳۰ ثانیه‌ای) یک LazyColumn تازه می‌ساخت و موقعیت اسکرول کاربر را
                        // به ابتدای لیست ریست می‌کرد. با یک LazyColumn پایدار + key موجود
                        // روی هر آیتم + Modifier.animateItem()، هم اسکرول حفظ می‌شود و هم
                        // جابه‌جایی/تغییر ردیف‌ها انیمیت می‌شود.
                        val listState = rememberLazyListState()
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            items(
                                items = groupedLoadingData,
                                key = { it.first }
                            ) { (groupName, shipData) ->
                                val actualShipName = shipData.first().shipName
                                val shipColor = shipColorMap[actualShipName]
                                    ?: MaterialTheme.colorScheme.primary
                                ShipCard(
                                    modifier = Modifier.animateItem(),
                                    shipName = groupName,
                                    isExpanded = expandedShip == groupName,
                                    onExpandToggle = {
                                        expandedShip =
                                            if (expandedShip == groupName) null else groupName
                                    },
                                    entryVouchers = if (isCurrentShift) shipData.sumOf { it.entryVouchers } else 0,
                                    exitVouchers = shipData.sumOf { it.exitVouchers },
                                    showEntry = isCurrentShift,
                                    content = {
                                        val warehouseGroups = shipData.groupBy { it.loadingWarehouse }

                                        Column(
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            warehouseGroups.forEach { (warehouse, quotas) ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 4.dp),
                                                    horizontalArrangement = Arrangement.Start
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(100),
                                                        color = RealTimeAccentBg
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Warehouse,
                                                                contentDescription = null,
                                                                tint = RealTimeAccent,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                            Text(
                                                                text = "انبار: $warehouse",
                                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                                fontWeight = FontWeight.Bold,
                                                                color = RealTimeAccent
                                                            )
                                                        }
                                                    }
                                                }

                                                quotas.sortedWith(
                                                    compareBy<RealTimeLoadingData> { it.shippingCompany }
                                                        .thenByDescending { it.entryVouchers }
                                                ).forEach { quota ->
                                                    RealTimeLoadingCard(
                                                        data = quota,
                                                        showEntry = isCurrentShift
                                                    )
                                                    Spacer(
                                                        modifier = Modifier.height(8.dp)
                                                    )
                                                }

                                                if (warehouse != warehouseGroups.keys.last()) {
                                                    HorizontalDivider(
                                                        modifier = Modifier.padding(
                                                            vertical = 8.dp
                                                        ),
                                                        color = shipColor.copy(alpha = 0.1f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

            }

            Spacer(modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(16.dp))
        }

        AnimatedVisibility(
            visible = uiState.isRefreshing,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            RefreshOverlay(
                isRefreshing = uiState.isRefreshing,
                remainingSeconds = uiState.secondsToNextRefresh
            )
        }
    }
}

@Composable
private fun RealTimeShiftNavigation(
    shiftOffset: Int,
    shiftInfo: ShiftInfo,
    onShiftOffsetChange: (Int) -> Unit
) {
    val formattedDate = shiftInfo.startDate ?: ""
    val shiftType = shiftInfo.type ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(RealTimeMutedBg)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onShiftOffsetChange(shiftOffset + 1) },
            enabled = shiftOffset < 0,
            modifier = Modifier.size(26.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "شیفت بعد",
                tint = if (shiftOffset < 0) RealTimeAccent else RealTimeMutedText.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "شیفت $shiftType",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = RealTimeTitleColor
                )
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = RealTimeMutedText,
                    modifier = Modifier.size(12.dp)
                )
            }
            if (formattedDate.isNotEmpty()) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = RealTimeMutedText
                )
            }
        }

        IconButton(
            onClick = { onShiftOffsetChange(shiftOffset - 1) },
            enabled = shiftOffset > -14,
            modifier = Modifier.size(26.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "شیفت قبل",
                tint = if (shiftOffset > -14) RealTimeAccent else RealTimeMutedText.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun RefreshOverlay(
    isRefreshing: Boolean,
    remainingSeconds: Int
) {
    val rotationAngle: Float
    val rippleEffect: Float
    if (AnimationManager.areAnimationsEnabled()) {
        val infiniteTransition = rememberInfiniteTransition(label = "")
        rotationAngle = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = ""
        ).value

        rippleEffect = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ), label = ""
        ).value
    } else {
        rotationAngle = 0f
        rippleEffect = 0f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.Center)
                .scale(1f + rippleEffect * 0.2f)
                .alpha(1f - rippleEffect)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
        )

        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refreshing",
            modifier = Modifier
                .size(50.dp)
                .rotate(rotationAngle)
                .align(Alignment.Center),
            tint = Color.White
        )

        Text(
            text = if (isRefreshing) "بروزرسانی..." else "$remainingSeconds",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
fun ShipCard(
    shipName: String,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    entryVouchers: Int,
    exitVouchers: Int,
    showEntry: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, RealTimeCardBorder)
    ) {
        Column(modifier = Modifier.padding(vertical = 0.dp)) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onExpandToggle),
                color = Color.Transparent
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(RealTimeAccentBg, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBoat,
                                    contentDescription = null,
                                    tint = RealTimeAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            Text(
                                text = shipName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = RealTimeTitleColor,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (showEntry) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (isDarkTheme) Red400.copy(alpha = 0.15f) else Red50.copy(alpha = 0.7f),
                                            border = BorderStroke(0.5.dp, if (isDarkTheme) Red400.copy(alpha = 0.3f) else Red400.copy(alpha = 0.2f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    text = formatNumber(entryVouchers),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isDarkTheme) Red400 else Red700
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.ArrowDownward,
                                                    contentDescription = null,
                                                    tint = if (isDarkTheme) Red400 else Red700,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                            }
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (isDarkTheme) Green300.copy(alpha = 0.15f) else Green50.copy(alpha = 0.7f),
                                        border = BorderStroke(0.5.dp, if (isDarkTheme) Green300.copy(alpha = 0.3f) else Green300.copy(alpha = 0.2f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = formatNumber(exitVouchers),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDarkTheme) Green300 else Green700
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ArrowUpward,
                                                contentDescription = null,
                                                tint = if (isDarkTheme) Green300 else Green700,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "بستن" else "بازکردن",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }

                    HorizontalDivider(
                        color = RealTimeCardBorder,
                        thickness = 1.dp
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
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun DialogHeader(
    onShareClick: () -> Unit = {},
    onRefreshClick: () -> Unit = {},
    isRefreshing: Boolean = false,
    refreshProgress: Float = 0f,
    loadingDataCount: Int = 0
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(34.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { refreshProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = RealTimeAccent.copy(alpha = 0.35f),
                    strokeWidth = 2.dp,
                    trackColor = RealTimeAccentBg,
                )

                IconButton(
                    onClick = onRefreshClick,
                    modifier = Modifier
                        .size(26.dp)
                        .background(RealTimeAccentBg, CircleShape)
                ) {
                    // P-6: animateFloatAsState یک مقدار هدف ثابت دارد و برای spec
                    // بی‌نهایت (infiniteRepeatable) طراحی نشده؛ Animatable با یک
                    // حلقه‌ی چرخش صریح، الگوی درست برای «تا وقتی X است بچرخ» است.
                    val rotation = remember { Animatable(0f) }
                    LaunchedEffect(isRefreshing) {
                        if (isRefreshing) {
                            while (true) {
                                rotation.animateTo(
                                    targetValue = rotation.value + 360f,
                                    animationSpec = tween(1000, easing = LinearEasing)
                                )
                            }
                        } else {
                            rotation.animateTo(0f, animationSpec = tween(300))
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "بروزرسانی",
                        tint = RealTimeAccent,
                        modifier = Modifier
                            .size(14.dp)
                            .rotate(rotation.value)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    "بارگیری لحظه‌ای",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RealTimeTitleColor
                )
                Text(
                    "$loadingDataCount کوتاژ فعال",
                    style = MaterialTheme.typography.labelSmall,
                    color = RealTimeMutedText
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(RealTimeMutedBg)
                    .clickable(onClick = onShareClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "اشتراک‌گذاری",
                    tint = RealTimeTitleColor,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

