package com.atk.atk_cargo

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.atk.atk_cargo.api.ActiveShipInfo
import com.atk.atk_cargo.api.ColorSelector
import com.atk.atk_cargo.api.MessageType
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.api.ShiftInfo
import com.atk.atk_cargo.api.cardColors
import com.atk.atk_cargo.ui.theme.ATKCargoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder

val cardColors = listOf(
    Color(0xFF1976D2), // آبی تیره
    Color(0xFF388E3C), // سبز تیره
    Color(0xFFF57C00), // نارنجی
    Color(0xFF7B1FA2), // بنفش
    Color(0xFFD32F2F), // قرمز
    Color(0xFF00796B), // سبز آبی
    Color(0xFF5D4037), // قهوه‌ای
    Color(0xFF455A64), // آبی خاکستری
    Color(0xFF689F38), // سبز لایم
    Color(0xFFE64A19)  // نارنجی تیره
)

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

    fun updateShipColors(ships: List<ActiveShipInfo>) {
        colorSelector.reset()
        shipColorMap.value = ships.associate { ship ->
            val baseColor = colorSelector.getNextColor()
            val adjustedColor = if (isDarkTheme) {
                baseColor.copy(alpha = 0.8f)
            } else {
                baseColor
            }
            ship.shipName to adjustedColor
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
                        shipColorMap = shipColorMap.value
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
    shipColorMap: Map<String, Color>
) {
    // فیلتر کردن گروه‌های کشتی که حداقل یک کوتاژ با حواله دارند
    val filteredGroupedShips = remember(groupedShips) {
        groupedShips.mapValues { (_, ships) ->
            ships.filter { it.entryVouchers + it.exitVouchers > 0 }
        }.filter { (_, ships) -> 
            ships.isNotEmpty() 
        }
    }
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        filteredGroupedShips.forEach { (shipName, ships) ->
            item {
                ShipGroup(
                    shipName = shipName,
                    ships = ships,
                    isExpanded = expandedShipName == shipName,
                    onExpand = { onExpand(shipName) },
                    onClick = onClick,
                    color = shipColorMap[shipName] ?: MaterialTheme.colorScheme.primary
                )
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
    val progress = if (total > 0) totalExit.toFloat() / total else 0f

    // رنگ‌های اصلی برای آمار با کنتراست بهتر و هارمونی رنگی بیشتر
    val entryColor = Color(0xFF2196F3) // آبی روشن برای ورود
    val exitColor = Color(0xFF4CAF50) // سبز برای خروج
    val totalColor = color // رنگ اصلی کشتی برای کل

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpand() },
        color = Color.Transparent,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // ردیف اول - نام کشتی، آیکون و آمار اصلی
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // آیکون و نام کشتی
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // آیکون کشتی با افکت ساده‌تر
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        color.copy(alpha = 0.6f),
                                        color.copy(alpha = 0.2f)
                                    )
                                )
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBoat,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // نام کشتی
                    Text(
                        text = shipName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // آمار خلاصه و آیکون باز/بسته کردن
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    // آمار گرافیکی
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(4.dp)
                    ) {
                        // آمار ورود با آیکون
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "ورود",
                                tint = entryColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = totalEntry.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = entryColor
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .height(16.dp)
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                        )
                        
                        // آمار خروج با آیکون
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "خروج",
                                tint = exitColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = totalExit.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = exitColor
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .height(16.dp)
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                        )
                        
                        // آمار کل با آیکون
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ConfirmationNumber,
                                contentDescription = "کل",
                                tint = totalColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = total.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = totalColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // آیکون باز/بسته کردن با انیمیشن چرخش
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (isExpanded) "بستن" else "بازکردن",
                        tint = color,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer {
                                rotationZ = if (isExpanded) 180f else 0f
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // نوار پیشرفت با درصد
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // نمایش آیکون‌های کوچک برای نوع آمار
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MiniIcon(
                            icon = Icons.Default.ArrowDownward,
                            color = entryColor,
                            contentDescription = "ورود"
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        MiniIcon(
                            icon = Icons.Default.ArrowUpward,
                            color = exitColor,
                            contentDescription = "خروج"
                        )
                    }
                    
                    // درصد پیشرفت
                    Text(
                        text = "${(progress * 100).toInt()}% تکمیل",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = color
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // نوار پیشرفت با انیمیشن
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        color.copy(alpha = 0.7f),
                                        color
                                    )
                                )
                            )
                            .animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniIcon(
    icon: ImageVector,
    color: Color,
    contentDescription: String? = null
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = color,
        modifier = Modifier.size(14.dp)
    )
}

@Composable
private fun ShipGroup(
    shipName: String,
    ships: List<ActiveShipInfo>,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onClick: (ActiveShipInfo) -> Unit,
    color: Color
) {
    // فیلتر کردن کوتاژهایی که حداقل یک حواله دارند
    val filteredShips = remember(ships) {
        ships.filter { it.entryVouchers + it.exitVouchers > 0 }
    }

    // اگر هیچ کوتاژی با حواله وجود ندارد، هیچ چیزی نمایش نده
    if (filteredShips.isEmpty()) {
        return
    }

    // انیمیشن برای گوشه‌های کارت
    val animatedCornerSize by animateDpAsState(
        targetValue = if (isExpanded) 16.dp else 20.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "corner"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(animatedCornerSize),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column {
            ShipHeader(
                shipName = shipName,
                ships = filteredShips,
                isExpanded = isExpanded,
                onExpand = onExpand,
                color = color
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 12.dp,
                            vertical = 8.dp
                        )
                ) {
                    // لیست انبارها با طراحی بهبود یافته
                    WarehouseList(
                        ships = filteredShips,
                        onClick = onClick,
                        cardColor = color
                    )
                }
            }
        }
    }
}

@Composable
private fun WarehouseCard(
    warehouse: String,
    ships: List<ActiveShipInfo>,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onClick: (ActiveShipInfo) -> Unit,
    color: Color
) {
    // فیلتر کردن کوتاژهایی که حداقل یک حواله دارند
    val filteredShips = remember(ships) {
        ships.filter { it.entryVouchers + it.exitVouchers > 0 }
    }
    
    // اگر هیچ کوتاژی با حواله وجود ندارد، هیچ چیزی نمایش نده
    if (filteredShips.isEmpty()) {
        return
    }
    
    val totalEntry = filteredShips.sumOf { it.entryVouchers }
    val totalExit = filteredShips.sumOf { it.exitVouchers }
    val total = totalEntry + totalExit
    val progress = if (total > 0) totalExit.toFloat() / total else 0f

    // رنگ‌های آمار
    val entryColor = Color(0xFF2196F3) // آبی روشن برای ورود
    val exitColor = Color(0xFF4CAF50) // سبز برای خروج

    // مرتب‌سازی کوتاژها:
    val sortedShips = remember(filteredShips) {
        filteredShips.sortedWith(
            compareBy<ActiveShipInfo> { 
                // ابتدا کوتاژهای تکمیل شده را به انتها منتقل می‌کنیم
                val isCompleted = it.exitVouchers >= it.entryVouchers && it.entryVouchers > 0
                if (isCompleted) 1 else 0
            }.thenBy { 
                // سپس کوتاژهای تکمیل نشده را براساس درصد پیشرفت به صورت صعودی مرتب می‌کنیم
                // (کمترین درصد اول نمایش داده می‌شود)
                if (it.entryVouchers > 0) {
                    it.exitVouchers.toFloat() / it.entryVouchers
                } else 0f
            }
        )
    }

    // گروه‌بندی داخلی بر اساس کالا یا باربری
    var groupByCargoType by remember { mutableStateOf(true) }
    val groupedItems = if (groupByCargoType) {
        sortedShips.groupBy { it.cargoType }
    } else {
        sortedShips.groupBy { it.shippingCompany }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.05f)
        ),
    ) {
        Column(modifier = Modifier.padding(vertical = 0.dp)) {
            // هدر انبار
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpand() },
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // اطلاعات انبار
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = color.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warehouse,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = warehouse,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                
                                Text(
                                    text = "${groupedItems.size} ${if (groupByCargoType) "نوع کالا" else "باربری"} | ${ships.size} کوتاژ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // آمار و آیکون باز/بسته کردن
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // نمایش گرافیکی آمار
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    // آمار ورود
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "ورود",
                                        tint = entryColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = totalEntry.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = entryColor,
                                        modifier = Modifier.padding(start = 2.dp, end = 4.dp)
                                    )
                                    
                                    // جداکننده
                                    Box(
                                        modifier = Modifier
                                            .height(14.dp)
                                            .width(1.dp)
                                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                                    )
                                    
                                    // آمار خروج
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "خروج",
                                        tint = exitColor,
                                        modifier = Modifier
                                            .padding(start = 4.dp)
                                            .size(14.dp)
                                    )
                                    Text(
                                        text = totalExit.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = exitColor,
                                        modifier = Modifier.padding(start = 2.dp, end = 4.dp)
                                    )
                                    
                                    // جداکننده
                                    Box(
                                        modifier = Modifier
                                            .height(14.dp)
                                            .width(1.dp)
                                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                                    )
                                    
                                    // آمار کل
                                    Icon(
                                        imageVector = Icons.Default.ConfirmationNumber,
                                        contentDescription = "کل",
                                        tint = color,
                                        modifier = Modifier
                                            .padding(start = 4.dp)
                                            .size(14.dp)
                                    )
                                    Text(
                                        text = total.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = color,
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Text(
                                text = "${(progress * 100).toInt()}% تکمیل",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // نوار پیشرفت
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = color,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }

            // محتوای باز شونده
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp,
                        top = 0.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // دکمه‌های تغییر نوع گروه‌بندی
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (groupByCargoType) color.copy(alpha = 0.2f) else Color.Transparent,
                            border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { groupByCargoType = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "نوع کالا",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (groupByCargoType) FontWeight.Bold else FontWeight.Normal,
                                    color = if (groupByCargoType) color else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (!groupByCargoType) color.copy(alpha = 0.2f) else Color.Transparent,
                            border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { groupByCargoType = false }
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "باربری",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (!groupByCargoType) FontWeight.Bold else FontWeight.Normal,
                                    color = if (!groupByCargoType) color else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // نمایش گروه‌ها
                    if (groupedItems.size == 1) {
                        // اگر فقط یک نوع کالا یا باربری وجود دارد، مستقیماً کوتاژها را نمایش بده
                        val (groupKey, groupShips) = groupedItems.entries.first()
                        
                        // نمایش عنوان
                        GroupHeader(
                            title = if (groupByCargoType) "کالا: $groupKey" else "باربری: $groupKey",
                            icon = if (groupByCargoType) Icons.Default.Category else Icons.Default.LocalShipping,
                            color = color
                        )

                        // نمایش کوتاژها در گرید یا لیست
                        if (groupShips.size > 4) {
                            QuotaGrid(
                                ships = groupShips,
                                onClick = onClick
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                groupShips.forEach { shipInfo ->
                                    CompactQuotaCard(
                                        shipInfo = shipInfo,
                                        onClick = onClick,
                                        color = color
                                    )
                                }
                            }
                        }
                    } else {
                        // اگر بیش از یک نوع کالا یا باربری وجود دارد، از ساختار گروهی استفاده کن
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            groupedItems.forEach { (groupKey, groupShips) ->
                                CompactGroupCard(
                                    title = if (groupByCargoType) "کالا: $groupKey" else "باربری: $groupKey",
                                    icon = if (groupByCargoType) Icons.Default.Category else Icons.Default.LocalShipping,
                                    ships = groupShips,
                                    onClick = onClick,
                                    color = color
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupHeader(
    title: String,
    icon: ImageVector,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CompactQuotaCard(
    shipInfo: ActiveShipInfo,
    onClick: (ActiveShipInfo) -> Unit,
    color: Color
) {
    val total = shipInfo.entryVouchers + shipInfo.exitVouchers
    val progress = if (total > 0) shipInfo.exitVouchers.toFloat() / total else 0f
    val progressColor = when {
        progress >= 0.9f -> color.copy(alpha = 0.9f)
        progress >= 0.5f -> color.copy(alpha = 0.7f)
        else -> color.copy(alpha = 0.5f)
    }
    
    // رنگ‌های آمار
    val entryColor = color.copy(alpha = 0.7f)
    val exitColor = color.copy(alpha = 0.9f)
    
    // تعیین وضعیت فعالیت کوتاژ
    val isActive = total > 0 && shipInfo.exitVouchers < total
    val cardBgColor = if (isActive) {
        color.copy(alpha = 0.03f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { onClick(shipInfo) },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBgColor
        ),
        border = BorderStroke(1.dp, color.copy(alpha = if (isActive) 0.3f else 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // اطلاعات اصلی کوتاژ
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // آیکون وضعیت
                    Surface(
                        shape = CircleShape,
                        color = progressColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = progressColor,
                            modifier = Modifier
                                .padding(6.dp)
                                .size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // اطلاعات کوتاژ
                    Column {
                        Text(
                            text = "کوتاژ: ${shipInfo.loadingQuotaNumber.takeLast(4)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (shipInfo.cargoType.isNotEmpty()) {
                            Text(
                                text = shipInfo.cargoType,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // آمار کوتاژ - نمایش گرافیکی
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    // آمار ورود
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "ورود",
                        tint = entryColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = shipInfo.entryVouchers.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = entryColor,
                        modifier = Modifier.padding(start = 2.dp, end = 4.dp)
                    )
                    
                    // جداکننده
                    Box(
                        modifier = Modifier
                            .height(12.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    )
                    
                    // آمار خروج
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "خروج",
                        tint = exitColor,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(12.dp)
                    )
                    Text(
                        text = shipInfo.exitVouchers.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = exitColor,
                        modifier = Modifier.padding(start = 2.dp, end = 4.dp)
                    )
                    
                    // جداکننده
                    Box(
                        modifier = Modifier
                            .height(12.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    )
                    
                    // آمار کل
                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = "کل",
                        tint = progressColor,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(12.dp)
                    )
                    Text(
                        text = total.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = progressColor,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
            
            // نوار پیشرفت
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun CompactGroupCard(
    title: String,
    icon: ImageVector,
    ships: List<ActiveShipInfo>,
    onClick: (ActiveShipInfo) -> Unit,
    color: Color
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    // فیلتر کردن کوتاژهایی که حداقل یک حواله دارند
    val filteredShips = remember(ships) {
        ships.filter { it.entryVouchers + it.exitVouchers > 0 }
    }
    
    // اگر هیچ کوتاژی با حواله وجود ندارد، هیچ چیزی نمایش نده
    if (filteredShips.isEmpty()) {
        return
    }
    
    val totalEntry = filteredShips.sumOf { it.entryVouchers }
    val totalExit = filteredShips.sumOf { it.exitVouchers }
    val total = totalEntry + totalExit
    val progress = if (total > 0) totalExit.toFloat() / total else 0f

    // مرتب‌سازی کوتاژها با همان منطق
    val sortedShips = remember(filteredShips) {
        filteredShips.sortedWith(
            compareBy<ActiveShipInfo> { 
                // ابتدا کوتاژهای تکمیل شده را به انتها منتقل می‌کنیم
                val isCompleted = it.exitVouchers >= it.entryVouchers && it.entryVouchers > 0
                if (isCompleted) 1 else 0
            }.thenBy { 
                // سپس کوتاژهای تکمیل نشده را براساس درصد پیشرفت به صورت صعودی مرتب می‌کنیم
                // (کمترین درصد اول نمایش داده می‌شود)
                if (it.entryVouchers > 0) {
                    it.exitVouchers.toFloat() / it.entryVouchers
                } else 0f
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column {
            // هدر گروه
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                color = color.copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // عنوان گروه
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = color.copy(alpha = 0.15f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // آمار و آیکون باز/بسته کردن
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$totalExit/$total",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    // نوار پیشرفت
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = color,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            // کارت‌های کوتاژ
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 12.dp,
                        end = 12.dp,
                        top = 4.dp,
                        bottom = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // نمایش تعداد کوتاژها
                    Text(
                        text = "${sortedShips.size} کوتاژ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    // نمایش کوتاژها در گرید
                    if (sortedShips.size > 4) {
                        // نمایش گرید برای تعداد زیاد کوتاژ
                        QuotaGrid(
                            ships = sortedShips,
                            onClick = onClick
                        )
                    } else {
                        // نمایش لیست برای تعداد کم کوتاژ
                        sortedShips.forEach { shipInfo ->
                            CompactQuotaCard(
                                shipInfo = shipInfo,
                                onClick = onClick,
                                color = color
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuotaGrid(
    ships: List<ActiveShipInfo>,
    onClick: (ActiveShipInfo) -> Unit
) {
    val chunkedShips = ships.chunked(2)
    
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        chunkedShips.forEach { rowShips ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowShips.forEach { shipInfo ->
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        GridQuotaCard(
                            shipInfo = shipInfo,
                            onClick = onClick
                        )
                    }
                }
                
                // اگر تعداد آیتم‌ها در ردیف کمتر از 2 است، فضای خالی اضافه کن
                if (rowShips.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun GridQuotaCard(
    shipInfo: ActiveShipInfo,
    onClick: (ActiveShipInfo) -> Unit
) {
    val total = shipInfo.entryVouchers + shipInfo.exitVouchers
    val progress = if (total > 0) shipInfo.exitVouchers.toFloat() / total else 0f
    val progressColor = when {
        progress >= 0.9f -> Color(0xFF4CAF50) // سبز
        progress >= 0.5f -> Color(0xFFFFA000) // نارنجی
        else -> Color(0xFF2196F3) // آبی
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(shipInfo) },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(1.dp, progressColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // شماره کوتاژ
            Text(
                text = shipInfo.loadingQuotaNumber.takeLast(4),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // آمار
            Text(
                text = "${shipInfo.exitVouchers}/${total}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = progressColor
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // نوار پیشرفت
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun WarehouseList(
    ships: List<ActiveShipInfo>,
    onClick: (ActiveShipInfo) -> Unit,
    cardColor: Color
) {
    val groupedByWarehouse = ships.groupBy { it.loadingWarehouse }
    var expandedWarehouse by remember { mutableStateOf<String?>(null) }

    // فیلتر کردن انبارهایی که حداقل یک کوتاژ با حواله دارند
    val filteredGroupedByWarehouse = remember(groupedByWarehouse) {
        groupedByWarehouse.mapValues { (_, warehouseShips) ->
            warehouseShips.filter { it.entryVouchers + it.exitVouchers > 0 }
        }.filter { (_, warehouseShips) -> 
            warehouseShips.isNotEmpty() 
        }
    }
    
    // مرتب‌سازی انبارها براساس درصد پیشرفت کل
    val sortedWarehouses = remember(filteredGroupedByWarehouse) {
        filteredGroupedByWarehouse.entries.sortedWith(
            compareBy<Map.Entry<String, List<ActiveShipInfo>>> { entry ->
                val warehouseShips = entry.value
                val totalEntry = warehouseShips.sumOf { it.entryVouchers }
                val totalExit = warehouseShips.sumOf { it.exitVouchers }

                // انبارهایی که کاملاً تکمیل شده‌اند در انتها قرار می‌گیرند
                if (totalEntry in 1..totalExit) {
                    1
                } else {
                    0
                }
            }.thenBy { entry ->
                val warehouseShips = entry.value
                val totalEntry = warehouseShips.sumOf { it.entryVouchers }
                val totalExit = warehouseShips.sumOf { it.exitVouchers }
                val total = totalEntry + totalExit
                
                // انبارهای تکمیل نشده براساس درصد پیشرفت به صورت صعودی مرتب می‌شوند
                if (total > 0) {
                    totalExit.toFloat() / total
                } else {
                    0f
                }
            }
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        sortedWarehouses.forEach { (warehouse, warehouseShips) ->
            WarehouseCard(
                warehouse = warehouse,
                ships = warehouseShips,
                isExpanded = expandedWarehouse == warehouse,
                onExpand = {
                    expandedWarehouse = if (expandedWarehouse == warehouse) null else warehouse
                },
                onClick = onClick,
                color = cardColor
            )
        }
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