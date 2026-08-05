package com.atk.atk_cargo.feature.reports.presentation.dialogs

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Scale
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
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atk.atk_cargo.api.RealTimeLoadingData
import com.atk.atk_cargo.api.ShiftInfo
import com.atk.atk_cargo.feature.reports.domain.formatNumber
import com.atk.atk_cargo.feature.reports.presentation.ships.SearchField
import com.atk.atk_cargo.ui.theme.Green300
import com.atk.atk_cargo.ui.theme.Green50
import com.atk.atk_cargo.ui.theme.Green700
import com.atk.atk_cargo.ui.theme.Red400
import com.atk.atk_cargo.ui.theme.Red50
import com.atk.atk_cargo.ui.theme.Red700
import com.atk.atk_cargo.ui.viewmodel.ReportsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val RealTimeAccent = Color(0xFF0D9488)
private val RealTimeAccentBg = Color(0xFFDCEFEA)
private val RealTimeAccentBorder = Color(0xFFB9DED7)
private val RealTimeCardBorder = Color(0xFFDCEEE9)
private val RealTimeMutedBg = Color(0xFFF3F4F5)
private val RealTimeMutedText = Color(0xFF8A8F98)
private val RealTimeTitleColor = Color(0xFF1F2937)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealTimeLoadingBottomSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    loadingData: List<RealTimeLoadingData>,
    shiftInfo: ShiftInfo,
    onRefresh: () -> Unit,
    viewModel: ReportsViewModel
) {
    val shipColorMap by viewModel.shipColorMap.collectAsState()
    val shiftOffset by viewModel.realTimeShiftOffset.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()
    val defaultColor = MaterialTheme.colorScheme.primary
    var remainingSeconds by remember { mutableIntStateOf(30) }
    var isRefreshing by remember { mutableStateOf(false) }
    var expandedShip by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isCurrentShift = remember(shiftOffset) { shiftOffset == 0 }
    val totalEntryVouchers = remember(loadingData, isCurrentShift) {
        if (isCurrentShift) loadingData.sumOf { it.entryVouchers } else 0
    }
    val totalExitVouchers = remember(loadingData) { loadingData.sumOf { it.exitVouchers } }
    val totalNetWeight = remember(loadingData) { loadingData.sumOf { it.totalNetWeight.toDouble() }.toFloat() }

    val filteredLoadingData = remember(loadingData, searchQuery) {
        if (searchQuery.isBlank()) {
            loadingData
        } else {
            loadingData.filter { data ->
                data.shipName.contains(searchQuery, ignoreCase = true) ||
                        data.loadingWarehouse.contains(searchQuery, ignoreCase = true) ||
                        data.loadingQuotaNumber.contains(searchQuery, ignoreCase = true) ||
                        data.shippingCompany.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(isOpen) {
        if (isOpen) {
            viewModel.loadRealTimeData(isDarkTheme, defaultColor)
            while (true) {
                delay(1000)
                remainingSeconds--
                if (remainingSeconds <= 0) {
                    isRefreshing = true
                    viewModel.loadRealTimeData(isDarkTheme, defaultColor)
                    onRefresh()
                    remainingSeconds = 30
                    delay(500)
                    isRefreshing = false
                }
            }
        }
    }

    if (isOpen) {
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
                            isRefreshing = isRefreshing,
                            refreshProgress = remainingSeconds / 30f,
                            onRefreshClick = {
                                if (!isRefreshing) {
                                    scope.launch {
                                        isRefreshing = true
                                        viewModel.loadRealTimeData(isDarkTheme, defaultColor)
                                        onRefresh()
                                        remainingSeconds = 30
                                        delay(800)
                                        isRefreshing = false
                                        Toast.makeText(context, "اطلاعات بروزرسانی شد", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onShareClick = {
                                val shareText = viewModel.shareRealTimeLoadingData(filteredLoadingData, shiftInfo)
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
                                viewModel = viewModel,
                                shiftInfo = shiftInfo,
                                isDarkTheme = isDarkTheme,
                                defaultColor = defaultColor
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

                            AnimatedContent(
                                targetState = filteredLoadingData,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(durationMillis = 300)) togetherWith
                                            fadeOut(animationSpec = tween(durationMillis = 300))
                                },
                                modifier = Modifier.weight(1f),
                                label = "LoadingDataContent"
                            ) { targetLoadingData ->
                                val groupedLoadingData = remember(targetLoadingData) {
                                    val cargoTypeCounts = targetLoadingData
                                        .groupBy { it.cargoType ?: "نامشخص" }
                                        .mapValues { it.value.map { data -> data.shipName }.distinct().size }

                                    targetLoadingData.groupBy { "${it.cargoType ?: "نامشخص"} | ${it.shipName}" }
                                        .toList()
                                        .sortedWith(
                                            compareByDescending<Pair<String, List<RealTimeLoadingData>>> { (_, shipData) ->
                                                cargoTypeCounts[shipData.first().cargoType ?: "نامشخص"] ?: 0
                                            }.thenBy { it.first }
                                        )
                                }

                                LazyColumn(
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

                }

                Spacer(modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .height(16.dp))
            }

            AnimatedVisibility(
                visible = isRefreshing,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                RefreshOverlay(
                    isRefreshing = isRefreshing,
                    remainingSeconds = remainingSeconds
                )
            }
        }
    }
}

@Composable
private fun RealTimeShiftNavigation(
    viewModel: ReportsViewModel,
    shiftInfo: ShiftInfo,
    isDarkTheme: Boolean,
    defaultColor: Color
) {
    val offset by viewModel.realTimeShiftOffset.collectAsState()
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
            onClick = { viewModel.setRealTimeShiftOffset(offset + 1, isDarkTheme, defaultColor) },
            enabled = offset < 0,
            modifier = Modifier.size(26.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "شیفت بعد",
                tint = if (offset < 0) RealTimeAccent else RealTimeMutedText.copy(alpha = 0.4f),
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
            onClick = { viewModel.setRealTimeShiftOffset(offset - 1, isDarkTheme, defaultColor) },
            enabled = offset > -14,
            modifier = Modifier.size(26.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "شیفت قبل",
                tint = if (offset > -14) RealTimeAccent else RealTimeMutedText.copy(alpha = 0.4f),
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
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    val rippleEffect by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

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
    content: @Composable () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
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
                        .background(Color.White)
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
        color = Color.White
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
                    val rotation by animateFloatAsState(
                        targetValue = if (isRefreshing) 360f else 0f,
                        animationSpec = if (isRefreshing) {
                            infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            )
                        } else {
                            tween(300)
                        },
                        label = "refresh_rotation"
                    )
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "بروزرسانی",
                        tint = RealTimeAccent,
                        modifier = Modifier
                            .size(14.dp)
                            .rotate(rotation)
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

@Composable
fun RealTimeLoadingCard(
    data: RealTimeLoadingData,
    showEntry: Boolean = true
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(14.dp),
        color = RealTimeMutedBg,
        border = BorderStroke(1.dp, Color(0xFFE9EAED)),
    ) {
        val verticalLineColor = RealTimeAccent

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val lineWidth = 4.dp.toPx()
                    val cornerRadius = 4.dp.toPx()
                    val x = size.width - lineWidth

                    val path = Path().apply {
                        moveTo(x, 0f)
                        lineTo(x + lineWidth - cornerRadius, 0f)
                        arcTo(
                            rect = androidx.compose.ui.geometry.Rect(
                                offset = Offset(x + lineWidth - cornerRadius * 2, 0f),
                                size = androidx.compose.ui.geometry.Size(cornerRadius * 2, cornerRadius * 2)
                            ),
                            startAngleDegrees = 90f,
                            sweepAngleDegrees = -90f,
                            forceMoveTo = false
                        )
                        lineTo(x + lineWidth, size.height - cornerRadius)
                        arcTo(
                            rect = androidx.compose.ui.geometry.Rect(
                                offset = Offset(x + lineWidth - cornerRadius * 2, size.height - cornerRadius * 2),
                                size = androidx.compose.ui.geometry.Size(cornerRadius * 2, cornerRadius * 2)
                            ),
                            startAngleDegrees = 0f,
                            sweepAngleDegrees = -90f,
                            forceMoveTo = false
                        )
                        lineTo(x, size.height)
                        close()
                    }
                    drawPath(path, verticalLineColor)
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "شماره کوتاژ",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = RealTimeMutedText
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = RealTimeAccent,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = data.shippingCompany,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.Bold,
                                color = RealTimeAccent
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, RealTimeCardBorder),
                        ) {
                            Text(
                                text = data.loadingQuotaNumber,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = RealTimeTitleColor,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                letterSpacing = 1.sp,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val dashLineColor = RealTimeCardBorder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .drawBehind {
                                val pathEffect =
                                    PathEffect.dashPathEffect(floatArrayOf(10f, 5f), 0f)
                                drawLine(
                                    color = dashLineColor,
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = pathEffect
                                )
                            }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "شرکت باربری",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                text = data.shippingCompany,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        VerticalDivider(
                            modifier = Modifier.height(40.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "وزن خالص",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                text = formatNumber(data.totalNetWeight),
                                style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.Ltr),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        VerticalDivider(
                            modifier = Modifier.height(40.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (showEntry) "ورود/خروج" else "خروجی",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                text = if (showEntry) "${data.exitVouchers} / ${data.entryVouchers}" else formatNumber(data.exitVouchers),
                                style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.Ltr),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactInfo(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color.copy(alpha = 0.07f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StatisticItem(
    totalEntryVouchers: Int,
    totalExitVouchers: Int,
    totalNetWeight: Float,
    showEntry: Boolean = true
) {
    val isDarkTheme = isSystemInDarkTheme()
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    val animatedTotalWeight by animateFloatAsState(
        targetValue = if (startAnimation) totalNetWeight else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "weight animation"
    )
    val animatedEntryVouchers by animateFloatAsState(
        targetValue = if (startAnimation) totalEntryVouchers.toFloat() else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "entry animation"
    )
    val animatedExitVouchers by animateFloatAsState(
        targetValue = if (startAnimation) totalExitVouchers.toFloat() else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "exit animation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        color = RealTimeMutedBg,
        border = BorderStroke(1.dp, Color(0xFFE9EAED))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Scale,
                        contentDescription = null,
                        tint = RealTimeMutedText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "وزن کل",
                        style = MaterialTheme.typography.labelMedium,
                        color = RealTimeMutedText,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color.White
                    ) {
                        Text(
                            text = "kg",
                            style = MaterialTheme.typography.labelSmall,
                            color = RealTimeMutedText,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = formatNumber(animatedTotalWeight.toInt()),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showEntry) {
                    val totalVouchers = animatedEntryVouchers.toInt() + animatedExitVouchers.toInt()
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = RealTimeAccentBg,
                        border = BorderStroke(1.dp, RealTimeAccentBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = formatNumber(totalVouchers),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RealTimeAccent
                                )
                                Icon(
                                    imageVector = Icons.Default.AllInbox,
                                    contentDescription = null,
                                    tint = RealTimeAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = "کل",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Medium,
                                color = RealTimeAccent.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDarkTheme) Red400.copy(alpha = 0.15f) else Red50,
                        border = BorderStroke(1.dp, if (isDarkTheme) Red400.copy(alpha = 0.3f) else Red400.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = formatNumber(animatedEntryVouchers.toInt()),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkTheme) Red400 else Red700
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = if (isDarkTheme) Red400 else Red700,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = "ورودی",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Medium,
                                color = if (isDarkTheme) Red400.copy(alpha = 0.7f) else Red700.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDarkTheme) Green300.copy(alpha = 0.15f) else Green50,
                    border = BorderStroke(1.dp, if (isDarkTheme) Green300.copy(alpha = 0.3f) else Green300.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = formatNumber(animatedExitVouchers.toInt()),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) Green300 else Green700
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (isDarkTheme) Green300 else Green700,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = "خروجی",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Medium,
                            color = if (isDarkTheme) Green300.copy(alpha = 0.7f) else Green700.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
