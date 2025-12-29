package com.atk.atk_cargo

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.atk.atk_cargo.api.ActiveShipInfo
import com.atk.atk_cargo.api.ColorSelector
import com.atk.atk_cargo.api.MessageType
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.api.cardColors
import com.atk.atk_cargo.api.validateServerSession
import com.atk.atk_cargo.ui.theme.ATKCargoTheme
import com.atk.atk_cargo.ui.theme.Blue50
import com.atk.atk_cargo.ui.theme.Blue500
import com.atk.atk_cargo.ui.theme.Purple50
import com.atk.atk_cargo.ui.theme.Purple500
import com.atk.atk_cargo.ui.theme.Red50
import com.atk.atk_cargo.ui.theme.Red500
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLEncoder

enum class ShipFilterTab(val title: String) {
    ALL("همه کشتی‌ها"),
    LOADING("در حال بارگیری"),
    COMPLETED("تکمیل شده")
}

class CargoCounterViewModel : ViewModel() {
    // وضعیت کشتی‌های انتخاب شده
    private val _selectedShipNames = MutableStateFlow<Set<String>>(emptySet())
    val selectedShipNames: StateFlow<Set<String>> = _selectedShipNames.asStateFlow()
    
    // وضعیت کشتی فعلی باز شده
    private val _expandedShipName = MutableStateFlow<String?>(null)
    val expandedShipName: StateFlow<String?> = _expandedShipName.asStateFlow()
    
    // وضعیت تب انتخاب شده
    private val _selectedTab = MutableStateFlow(ShipFilterTab.LOADING)
    val selectedTab: StateFlow<ShipFilterTab> = _selectedTab.asStateFlow()
    
    // به‌روزرسانی کشتی‌های انتخاب شده
    fun updateSelectedShips(ships: Set<String>) {
        _selectedShipNames.update { ships }
    }
    
    // به‌روزرسانی وضعیت باز/بسته بودن کشتی
    fun updateExpandedShipName(shipName: String?) {
        _expandedShipName.update { shipName }
    }
    
    // به‌روزرسانی تب انتخاب شده
    fun updateSelectedTab(tab: ShipFilterTab) {
        _selectedTab.update { tab }
    }
}

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

data class CargoSnackbarMessage(
    val text: String,
    val type: MessageType
)

fun filterShipsByTab(
    groupedShips: Map<String, List<ActiveShipInfo>>,
    tab: ShipFilterTab
): Map<String, List<ActiveShipInfo>> {
    return when (tab) {
        ShipFilterTab.ALL -> groupedShips
        ShipFilterTab.LOADING -> {
            // کشتی‌هایی که حداقل یک ورودی دارند (در حال بارگیری)
            groupedShips.mapValues { (_, ships) ->
                ships.filter { it.entryVouchers > 0 }
            }.filter { (_, ships) -> ships.isNotEmpty() }
        }
        ShipFilterTab.COMPLETED -> {
            // کشتی‌هایی که همه حواله‌های آن‌ها خروج شده است
            groupedShips.mapValues { (_, ships) ->
                ships.filter { ship ->
                    val total = ship.entryVouchers + ship.exitVouchers
                    total > 0 && ship.exitVouchers >= total
                }
            }.filter { (_, ships) -> ships.isNotEmpty() }
        }
    }
}

@Composable
fun CargoCounterScreen(navController: NavController) {
    val context = LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    
    LaunchedEffect(Unit) {
        try {
            val result = validateServerSession(userPreferencesManager)
            result.fold(
                onSuccess = {
                    // Session معتبر است، ادامه می‌دهد
                },
                onFailure = {
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("CargoCounterScreen", "خطا در بررسی وضعیت ورود: ${e.message}")
            navController.navigate("home") {
                popUpTo(0) { inclusive = true }
            }
            return@LaunchedEffect
        }
    }
    
    // استفاده از ViewModel برای حفظ وضعیت
    val viewModel: CargoCounterViewModel = viewModel()
    
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var activeShips by remember { mutableStateOf<List<ActiveShipInfo>>(emptyList()) }
    
    // دریافت کشتی‌های انتخاب شده از ViewModel
    val selectedShipNames by viewModel.selectedShipNames.collectAsState(initial = emptySet())
    
    val filteredShips = activeShips.filter { selectedShipNames.contains(it.shipName) }
    val groupedShips = filteredShips.groupBy { it.shipName }

    // دریافت وضعیت باز/بسته بودن کشتی از ViewModel
    val expandedShipName by viewModel.expandedShipName.collectAsState(initial = null)
    
    // دریافت تب انتخاب شده از ViewModel
    val selectedTab by viewModel.selectedTab.collectAsState(initial = ShipFilterTab.ALL)
    
    val isDarkTheme = isSystemInDarkTheme()
    val colorSelector = remember { ColorSelector(cardColors) }
    val shipColorMap = remember { mutableStateOf<Map<String, Color>>(emptyMap()) }
    var snackbarMessage by remember { mutableStateOf<CargoSnackbarMessage?>(null) }
    var showShipSelectionDialog by remember { mutableStateOf(false) }

    // نمایش خودکار دیالوگ انتخاب کشتی وقتی هیچ کشتی انتخاب نشده
    LaunchedEffect(selectedShipNames, activeShips) {
        if (selectedShipNames.isEmpty() && activeShips.isNotEmpty()) {
            showShipSelectionDialog = true
        }
    }

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
        snackbarMessage = CargoSnackbarMessage(message, type)
    }

    fun updateStatistics() {
        isRefreshing = true
        coroutineScope.launch {
            try {
                val response = RetrofitClient.apiService.getRealTimeLoadingData()
                if (response.isSuccessful) {
                    val realTimeDataResponse = response.body()
                    if (realTimeDataResponse != null) {
                        showUpdateMessage("اطلاعات با موفقیت بروزرسانی شد", MessageType.SUCCESS)
                        // بروزرسانی اطلاعات کشتی‌ها با استفاده از داده‌های دریافتی
                        if (realTimeDataResponse.data.isNotEmpty()) {
                            activeShips = activeShips.map { ship ->
                                val updatedData = realTimeDataResponse.data.find { 
                                    it.loadingQuotaNumber == ship.loadingQuotaNumber &&
                                    it.shipName == ship.shipName &&
                                    it.loadingWarehouse == ship.loadingWarehouse &&
                                    it.shippingCompany == ship.shippingCompany
                                }
                                if (updatedData != null) {
                                    ship.copy(
                                        entryVouchers = updatedData.entryVouchers,
                                        exitVouchers = updatedData.exitVouchers
                                    )
                                } else {
                                    ship
                                }
                            }
                        }
                    } else {
                        showUpdateMessage("داده‌های دریافتی خالی است", MessageType.WARNING)
                    }
                } else {
                    showUpdateMessage("خطا در دریافت اطلاعات: ${response.code()}", MessageType.ERROR)
                }
            } catch (_: Exception) {
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
            } catch (_: Exception) {
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
                        onRefresh = { refreshData() },
                        onSelectShips = { showShipSelectionDialog = true },
                        selectedShipsCount = selectedShipNames.size
                    )
                    
                    // محاسبه تعداد کشتی‌های در حال بارگیری و تکمیل شده
                    val loadingCount = groupedShips.count { (_, ships) ->
                        ships.any { it.entryVouchers > 0 }
                    }
                    val completedCount = groupedShips.count { (_, ships) ->
                        ships.any { ship ->
                            val total = ship.entryVouchers + ship.exitVouchers
                            total > 0 && ship.exitVouchers >= total
                        }
                    }
                    
                    // نمایش تب‌ها
                    if (selectedShipNames.isNotEmpty()) {
                        TabBar(
                            selectedTab = selectedTab,
                            onTabSelected = { tab -> viewModel.updateSelectedTab(tab) },
                            loadingCount = loadingCount,
                            completedCount = completedCount
                        )
                    }
                    
                    if (selectedShipNames.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.8f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBoat,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(80.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "لطفاً کشتی مورد نظر را انتخاب کنید",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Button(
                                    onClick = { showShipSelectionDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsBoat,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text("انتخاب کشتی")
                                }
                            }
                        }
                    } else {
                        val filteredGroupedShips = filterShipsByTab(groupedShips, selectedTab)
                        GroupedShipList(
                            groupedShips = filteredGroupedShips,
                            expandedShipName = expandedShipName,
                            onExpand = { shipName ->
                                viewModel.updateExpandedShipName(if (expandedShipName == shipName) null else shipName)
                            },
                            onClick = { selectedShip ->
                                navigateToCargoDetailsScreen(navController, selectedShip)
                            },
                            shipColorMap = shipColorMap.value
                        )
                    }
                }

                snackbarMessage?.let { message ->
                    StatusSnackbar(
                        message = message,
                        isVisible = true,
                        onDismiss = { snackbarMessage = null }
                    )
                }
                
                if (showShipSelectionDialog) {
                    ShipSelectionDialog(
                        ships = activeShips,
                        selectedShipNames = selectedShipNames,
                        onSelectShip = { selectedShips: Set<String> ->
                            viewModel.updateSelectedShips(selectedShips)
                        },
                        onDismiss = { showShipSelectionDialog = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun TabBar(
    selectedTab: ShipFilterTab,
    onTabSelected: (ShipFilterTab) -> Unit,
    loadingCount: Int,
    completedCount: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 2.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            ShipFilterTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                val count = when (tab) {
                    ShipFilterTab.ALL -> ""
                    ShipFilterTab.LOADING -> "($loadingCount)"
                    ShipFilterTab.COMPLETED -> "($completedCount)"
                }
                
                Surface(
                    modifier = Modifier
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)) else null
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val icon = when (tab) {
                            ShipFilterTab.ALL -> Icons.Default.DirectionsBoat
                            ShipFilterTab.LOADING -> Icons.Default.LocalShipping
                            ShipFilterTab.COMPLETED -> Icons.Default.Check
                        }
                        
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Text(
                            text = "${tab.title} $count",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        groupedShips.forEach { (shipName, ships) ->
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
    
    // محاسبه وضعیت تکمیل شدن
    val isCompleted = total in 1..totalExit

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpand() },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // آیکون و نام کشتی
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = color.copy(alpha = 0.15f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBoat,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = shipName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        // نمایش badge تکمیل شده
                        if (isCompleted) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "تکمیل شده",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // آمار و آیکون باز/بسته کردن
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = total.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                
                Text(
                    text = "حواله",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "بستن" else "بازکردن",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
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
                            horizontal = 16.dp,
                            vertical = 12.dp
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
                                    text = "${filteredShips.size} کوتاژ",
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

            // محتوای باز شونده - نمایش مستقیم کوتاژها بدون گروه‌بندی
            AnimatedVisibility(
                visible = isExpanded,
                enter = if (AnimationManager.areAnimationsEnabled()) fadeIn() + expandVertically() else fadeIn(),
                exit = if (AnimationManager.areAnimationsEnabled()) fadeOut() + shrinkVertically() else fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp,
                        top = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sortedShips.forEach { shipInfo ->
                        QuotaCard(
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

@Composable
private fun QuotaCard(
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
                        tint = color,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = shipInfo.entryVouchers.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = color,
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
                        tint = color,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(12.dp)
                    )
                    Text(
                        text = shipInfo.exitVouchers.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = color,
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
                        color = color,
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

@Composable
fun StatusSnackbar(
    message: CargoSnackbarMessage,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var animatedVisibility by remember { mutableStateOf(false) }

    val translateY by animateDpAsState(
        targetValue = if (animatedVisibility) 0.dp else 100.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ), label = ""
    )

    val alpha by animateFloatAsState(
        targetValue = if (animatedVisibility) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ), label = ""
    )

    LaunchedEffect(isVisible) {
        if (isVisible) {
            animatedVisibility = true
            delay(3000)
            animatedVisibility = false
            delay(300)
            onDismiss()
        }
    }

    if (isVisible || animatedVisibility) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .offset(y = translateY)
                    .alpha(alpha),
                shape = RoundedCornerShape(16.dp),
                color = when (message.type) {
                    MessageType.SUCCESS -> Blue50
                    MessageType.ERROR -> Red50
                    MessageType.WARNING -> Purple50
                },
                border = BorderStroke(1.dp, when (message.type) {
                    MessageType.SUCCESS -> Blue500.copy(alpha = 0.2f)
                    MessageType.ERROR -> Red500.copy(alpha = 0.2f)
                    MessageType.WARNING -> Purple500.copy(alpha = 0.2f)
                }),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = when (message.type) {
                            MessageType.SUCCESS -> Icons.Outlined.CheckCircle
                            MessageType.ERROR -> Icons.Default.Close
                            MessageType.WARNING -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = when (message.type) {
                            MessageType.SUCCESS -> Blue500
                            MessageType.ERROR -> Red500
                            MessageType.WARNING -> Purple500
                        },
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = when (message.type) {
                            MessageType.SUCCESS -> Blue500
                            MessageType.ERROR -> Red500
                            MessageType.WARNING -> Purple500
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedHeader(
    shipCount: Int,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onSelectShips: () -> Unit = {},
    selectedShipsCount: Int = 0
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "بارگیری‌های فعال",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBoat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "$shipCount کوتاژ فعال",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (selectedShipsCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "$selectedShipsCount کشتی انتخاب شده",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // دکمه انتخاب کشتی
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onSelectShips() }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBoat,
                            contentDescription = "انتخاب کشتی‌ها",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // دکمه بروزرسانی
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onRefresh() }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRefreshing) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "بروزرسانی",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
