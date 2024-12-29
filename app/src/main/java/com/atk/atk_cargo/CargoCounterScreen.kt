package com.atk.atk_cargo

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.atk.atk_cargo.api.ActiveShipInfo
import com.atk.atk_cargo.api.ColorSelector
import com.atk.atk_cargo.api.MessageType
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.api.ShiftInfo
import com.atk.atk_cargo.api.adjustColorForTheme
import com.atk.atk_cargo.api.cardColors
import com.atk.atk_cargo.ui.theme.ATKCargoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder

@Composable
fun CargoCounterScreen(navController: NavController) {
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var activeShips by remember { mutableStateOf<List<ActiveShipInfo>>(emptyList()) }
    val groupedShips = activeShips.groupBy { it.shipName }
    var currentShiftInfo by remember { mutableStateOf<ShiftInfo?>(null) }
    var expandedShipName by remember { mutableStateOf<String?>(null) }
    val isDarkTheme = isSystemInDarkTheme()
    val colorSelector = remember { ColorSelector(cardColors) }
    val shipColorMap = remember { mutableStateOf<Map<String, Color>>(emptyMap()) }
    var snackbarMessage by remember { mutableStateOf<SnackbarMessage?>(null) }
    var groupByCargoType by remember { mutableStateOf(true) }

    fun updateShipColors(ships: List<ActiveShipInfo>) {
        colorSelector.reset()
        shipColorMap.value = ships.associate { ship ->
            ship.shipName to adjustColorForTheme(colorSelector.getNextColor(), isDarkTheme)
        }
    }

    fun showUpdateMessage(message: String, type: MessageType) {
        snackbarMessage = SnackbarMessage(message, type)
    }

    fun updateStatistics() {
        isRefreshing = true
        coroutineScope.launch {
            try {
                val response = RetrofitClient.apiService.getRealTimeLoadingData()
                if (response.isSuccessful) {
                    val realTimeDataResponse = response.body()
                    if (realTimeDataResponse != null) {
                        activeShips = activeShips.map { ship ->
                            val updatedData = realTimeDataResponse.data.find { it.loadingQuotaNumber == ship.loadingQuotaNumber }
                            if (updatedData != null) {
                                ship.copy(
                                    entryVouchers = updatedData.entryVouchers,
                                    exitVouchers = updatedData.exitVouchers
                                )
                            } else {
                                ship
                            }
                        }
                        currentShiftInfo = realTimeDataResponse.shiftInfo
                        showUpdateMessage("اطلاعات با موفقیت بروزرسانی شد", MessageType.SUCCESS)
                    } else {
                        showUpdateMessage("داده‌های دریافتی خالی است", MessageType.WARNING)
                    }
                } else {
                    showUpdateMessage("خطا در دریافت اطلاعات: ${response.code()}", MessageType.ERROR)
                }
            } catch (e: Exception) {
                showUpdateMessage("خطا در ارتباط با سرور", MessageType.ERROR)
            } finally {
                isRefreshing = false
            }
        }
    }

    fun refreshData() {
        isRefreshing = true
        coroutineScope.launch {
            try {
                loadActiveShips(coroutineScope, snackbarHostState) { ships ->
                    activeShips = ships
                    updateShipColors(ships)
                    updateStatistics()
                }
            } catch (e: Exception) {
                showUpdateMessage("خطا در بروزرسانی داده‌ها", MessageType.ERROR)
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadActiveShips(coroutineScope, snackbarHostState) { ships ->
            activeShips = ships
            updateShipColors(ships)
            updateStatistics()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30000)
            updateStatistics()
        }
    }

    ATKCargoTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                ) {
                    AnimatedHeader(
                        shipCount = activeShips.size,
                        isRefreshing = isRefreshing,
                        onRefresh = { refreshData() }
                    )
                    GroupedShipList(
                        groupedShips = groupedShips,
                        expandedShipName = expandedShipName,
                        onExpand = { shipName ->
                            expandedShipName = if (expandedShipName == shipName) null else shipName
                        },
                        onClick = { selectedShip ->
                            navigateToCargoDetailsScreen(navController, selectedShip)
                        },
                        shipColorMap = shipColorMap.value,
                        groupByCargoType = groupByCargoType,
                        onGroupingChanged = { groupByCargoType = it }
                    )
                }

                snackbarMessage?.let { message ->
                    StatusSnackbar(
                        message = message,
                        isVisible = true,
                        onDismiss = { snackbarMessage = null }
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupedShipList(
    groupedShips: Map<String, List<ActiveShipInfo>>,
    expandedShipName: String?,
    onExpand: (String?) -> Unit,
    onClick: (ActiveShipInfo) -> Unit,
    shipColorMap: Map<String, Color>,
    groupByCargoType: Boolean,
    onGroupingChanged: (Boolean) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        groupedShips.forEach { (shipName, ships) ->
            item {
                ShipGroup(
                    shipName = shipName,
                    ships = ships,
                    isExpanded = expandedShipName == shipName,
                    onExpand = { onExpand(shipName) },
                    onClick = onClick,
                    color = shipColorMap[shipName] ?: MaterialTheme.colorScheme.primary,
                    groupByCargoType = groupByCargoType,
                    onGroupingChanged = onGroupingChanged
                )
            }
        }
    }
}

@Composable
private fun ShipGroup(
    shipName: String,
    ships: List<ActiveShipInfo>,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onClick: (ActiveShipInfo) -> Unit,
    color: Color,
    groupByCargoType: Boolean,
    onGroupingChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column {
            ShipHeader(
                shipName = shipName,
                ships = ships,
                isExpanded = isExpanded,
                onExpand = onExpand,
                color = color
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    WarehouseList(
                        ships = ships,
                        onClick = onClick,
                        cardColor = color,
                        groupByCargoType = groupByCargoType,
                        onGroupingChanged = onGroupingChanged
                    )
                }
            }
        }
    }
}

@Composable
private fun ShipHeader(
    shipName: String,
    ships: List<ActiveShipInfo>,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    color: Color
) {
    val totalEntry = ships.sumOf { it.entryVouchers }
    val totalExit = ships.sumOf { it.exitVouchers }
    val total = totalEntry + totalExit

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpand() },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBoat,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = shipName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItem(label = "ورود", value = totalEntry, color = color)
                        StatDivider(color = color)
                        StatItem(label = "خروج", value = totalExit, color = color)
                        StatDivider(color = color)
                        StatItem(label = "کل", value = total, color = color, isBold = true)
                    }
                }
            }
            Icon(
                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (isExpanded) "بستن" else "بازکردن",
                tint = color
            )
        }
    }
}

@Composable
private fun GroupingToggleButton(
    groupByCargoType: Boolean,
    onGroupingChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp)),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(2.dp)
            ) {
                if (groupByCargoType) {
                    Surface(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(10.dp)),
                        color = color
                    ) {}
                }

                // Content
                Row(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { onGroupingChanged(true) }
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = if (groupByCargoType) Color.White else color,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "کالا",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (groupByCargoType) Color.White else color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(2.dp)
            ) {
                if (!groupByCargoType) {
                    Surface(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(10.dp)),
                        color = color
                    ) {}
                }

                // Content
                Row(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { onGroupingChanged(false) }
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = if (!groupByCargoType) Color.White else color,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "باربری",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (!groupByCargoType) Color.White else color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun WarehouseList(
    ships: List<ActiveShipInfo>,
    onClick: (ActiveShipInfo) -> Unit,
    cardColor: Color,
    groupByCargoType: Boolean,
    onGroupingChanged: (Boolean) -> Unit
) {
    var expandedWarehouse by remember { mutableStateOf<String?>(null) }
    val groupedByWarehouse = ships.groupBy { it.loadingWarehouse }

    val warehouseColors = remember(cardColor) {
        val baseHsv = FloatArray(3)
        android.graphics.Color.colorToHSV(cardColor.toArgb(), baseHsv)

        groupedByWarehouse.keys.mapIndexed { index, warehouse ->
            val hue = (baseHsv[0] + (index * 15)) % 360
            val saturation = (baseHsv[1] + (index * 0.1f)).coerceIn(0f, 1f)
            val value = (baseHsv[2] + (index * 0.1f)).coerceIn(0.3f, 0.9f)

            warehouse to Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value)))
        }.toMap()
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                GroupingToggleButton(
                    groupByCargoType = groupByCargoType,
                    onGroupingChanged = onGroupingChanged,
                    color = cardColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        groupedByWarehouse.forEach { (warehouse, warehouseShips) ->
            WarehouseGroup(
                warehouse = warehouse,
                ships = warehouseShips,
                onClick = onClick,
                warehouseColor = warehouseColors[warehouse] ?: cardColor,
                isExpanded = expandedWarehouse == warehouse,
                onExpand = { isExpanded ->
                    expandedWarehouse = if (isExpanded) warehouse else null
                },
                groupByCargoType = groupByCargoType
            )
        }
    }
}

@Composable
private fun WarehouseGroup(
    warehouse: String,
    ships: List<ActiveShipInfo>,
    onClick: (ActiveShipInfo) -> Unit,
    warehouseColor: Color,
    isExpanded: Boolean,
    onExpand: (Boolean) -> Unit,
    groupByCargoType: Boolean
) {
    val totalEntry = ships.sumOf { it.entryVouchers }
    val totalExit = ships.sumOf { it.exitVouchers }
    val total = totalEntry + totalExit

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        color = warehouseColor.copy(alpha = 0.08f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpand(!isExpanded) },
                color = warehouseColor.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warehouse,
                            contentDescription = null,
                            tint = warehouseColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "انبار: $warehouse",
                                style = MaterialTheme.typography.titleMedium,
                                color = warehouseColor,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SmallStatItem(label = "ورود", value = totalEntry, color = warehouseColor)
                                SmallStatDivider(color = warehouseColor)
                                SmallStatItem(label = "خروج", value = totalExit, color = warehouseColor)
                                SmallStatDivider(color = warehouseColor)
                                SmallStatItem(label = "کل", value = total, color = warehouseColor, isBold = true)
                            }
                        }
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = warehouseColor
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (groupByCargoType) {
                        ships.groupBy { it.cargoType }.forEach { (cargoType, cargoShips) ->
                            FinalGroupCard(
                                title = "کالا: $cargoType",
                                ships = cargoShips,
                                onClick = onClick,
                                cardColor = warehouseColor,
                                groupByCargoType = true
                            )
                        }
                    } else {
                        ships.groupBy { it.shippingCompany }.forEach { (company, companyShips) ->
                            FinalGroupCard(
                                title = "باربری: $company",
                                ships = companyShips,
                                onClick = onClick,
                                cardColor = warehouseColor,
                                groupByCargoType = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FinalGroupCard(
    title: String,
    ships: List<ActiveShipInfo>,
    onClick: (ActiveShipInfo) -> Unit,
    cardColor: Color,
    groupByCargoType: Boolean
) {
    Surface(
        color = cardColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = cardColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = cardColor,
                    fontWeight = FontWeight.Bold
                )
            }

            ships.forEach { shipInfo ->
                ShipCard(
                    shipInfo = shipInfo,
                    onClick = onClick,
                    cardColor = cardColor.copy(alpha = 0.9f),
                    groupByCargoType = groupByCargoType
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: Int, color: Color, isBold: Boolean = false) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodyMedium,
        color = color.copy(alpha = 0.7f),
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
    )
}

@Composable
private fun SmallStatItem(label: String, value: Int, color: Color, isBold: Boolean = false) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodySmall,
        color = color.copy(alpha = 0.7f),
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
    )
}

@Composable
private fun StatDivider(color: Color) {
    Text(
        text = "•",
        style = MaterialTheme.typography.bodyMedium,
        color = color.copy(alpha = 0.3f)
    )
}

@Composable
private fun SmallStatDivider(color: Color) {
    Text(
        text = "•",
        style = MaterialTheme.typography.bodySmall,
        color = color.copy(alpha = 0.3f)
    )
}

@Composable
private fun ShipCard(
    shipInfo: ActiveShipInfo,
    onClick: (ActiveShipInfo) -> Unit,
    cardColor: Color,
    groupByCargoType: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(shipInfo) },
        shape = RoundedCornerShape(8.dp),
        color = cardColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    // تغییر متن نمایشی بر اساس نوع گروه‌بندی
                    text = "${shipInfo.loadingQuotaNumber.takeLast(4)} • ${if (groupByCargoType) shipInfo.shippingCompany else shipInfo.cargoType}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    QuotaStats(
                        entry = shipInfo.entryVouchers,
                        exit = shipInfo.exitVouchers
                    )
                }
            }

            Button(
                onClick = { onClick(shipInfo) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = cardColor
                ),
                modifier = Modifier
                    .height(32.dp)
                    .padding(start = 8.dp)
            ) {
                Text(
                    "انتخاب",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun QuotaStats(
    entry: Int,
    exit: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "ورود: $entry",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )
        Text(
            text = "•",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f)
        )
        Text(
            text = "خروج: $exit",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )
        Text(
            text = "•",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f)
        )
        Text(
            text = "کل: ${entry + exit}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun loadActiveShips(
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onSuccess: (List<ActiveShipInfo>) -> Unit
) {
    coroutineScope.launch {
        try {
            val response = RetrofitClient.apiService.getActiveShips()
            if (response.isSuccessful) {
                response.body()?.let { ships ->
                    onSuccess(ships)
                } ?: showErrorMessage(snackbarHostState, "داده‌های دریافتی خالی است")
            } else {
                showErrorMessage(snackbarHostState, "خطا در دریافت اطلاعات کشتی‌های فعال")
            }
        } catch (e: Exception) {
            showErrorMessage(snackbarHostState, "خطا در ارتباط با سرور: ${e.message}")
        }
    }
}

private fun navigateToCargoDetailsScreen(navController: NavController, shipInfo: ActiveShipInfo) {
    try {
        val encodedShippingCompany = URLEncoder.encode(shipInfo.shippingCompany, "UTF-8")
        val encodedWarehouse = URLEncoder.encode(shipInfo.loadingWarehouse, "UTF-8")
        val encodedCargoType = URLEncoder.encode(shipInfo.cargoType, "UTF-8")

        val route = buildString {
            append("cargoDetailsScreen/")
            append(shipInfo.loadingQuotaNumber)
            append("/")
            append(encodedShippingCompany)
            append("/")
            append(encodedWarehouse)
            append("/")
            append(encodedCargoType)
        }

        Log.d("Navigation", "Navigating to: $route")

        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
        }
    } catch (e: Exception) {
        Log.e("Navigation", "Navigation error: ${e.message}")
        e.printStackTrace()
    }
}

private suspend fun showErrorMessage(snackbarHostState: SnackbarHostState, message: String) {
    snackbarHostState.showSnackbar(message)
}