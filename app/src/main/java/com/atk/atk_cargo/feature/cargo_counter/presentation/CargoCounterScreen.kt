package com.atk.atk_cargo.feature.cargo_counter.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.atk.atk_cargo.api.ActiveShipInfo
import com.atk.atk_cargo.api.ApiServiceV2
import com.atk.atk_cargo.api.ColorSelector
import com.atk.atk_cargo.api.MessageType
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.api.cardColors
import com.atk.atk_cargo.api.validateServerSession
import com.atk.atk_cargo.data.model.RealTimeDataResponse
import com.atk.atk_cargo.domain.model.toDomain
import com.atk.atk_cargo.feature.cargo_counter.presentation.components.AnimatedHeader
import com.atk.atk_cargo.feature.cargo_counter.presentation.components.GroupedShipList
import com.atk.atk_cargo.feature.cargo_counter.presentation.components.StatusSnackbar
import com.atk.atk_cargo.feature.cargo_counter.presentation.components.TabBar
import com.atk.atk_cargo.feature.cargo_entry.presentation.ShipSelectionDialog
import com.atk.atk_cargo.feature.home.navigation.navigateToHome
import com.atk.atk_cargo.feature.reports.navigation.navigateToCargoDetails
import com.atk.atk_cargo.ui.theme.ATKCargoTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

private val CargoCounterAccent: Color
    @Composable get() = MaterialTheme.colorScheme.primary

private val CargoCounterAccentBg: Color
    @Composable get() = MaterialTheme.colorScheme.primaryContainer

enum class ShipFilterTab(val title: String) {
    ALL("همه کشتی‌ها"),
    LOADING("در حال بارگیری"),
    COMPLETED("تکمیل شده")
}

class CargoCounterViewModel(
    private val apiServiceV2: ApiServiceV2
) : ViewModel() {
    private val _selectedShipNames = MutableStateFlow<Set<String>>(emptySet())
    val selectedShipNames: StateFlow<Set<String>> = _selectedShipNames.asStateFlow()

    private val _expandedShipName = MutableStateFlow<String?>(null)
    val expandedShipName: StateFlow<String?> = _expandedShipName.asStateFlow()

    private val _selectedTab = MutableStateFlow(ShipFilterTab.LOADING)
    val selectedTab: StateFlow<ShipFilterTab> = _selectedTab.asStateFlow()

    fun updateSelectedShips(ships: Set<String>) {
        _selectedShipNames.update { ships }
    }

    fun updateExpandedShipName(shipName: String?) {
        _expandedShipName.update { shipName }
    }

    fun updateSelectedTab(tab: ShipFilterTab) {
        _selectedTab.update { tab }
    }

    // هر دو تماس شبکه‌ی زیر قبلاً با rememberCoroutineScope() از داخل
    // Composable اجرا می‌شدند — با خروج کاربر از صفحه کنسل می‌شدند
    // (DEEP_CODE_REVIEW.md Top20 #5). viewModelScope در برابر ناوبری مقاوم
    // است؛ منطق سطربه‌سطر عیناً حفظ شده، UI (SnackbarHostState) دست‌نخورده
    // در Composable می‌ماند و ViewModel فقط نتیجه را با callback برمی‌گرداند.
    fun loadActiveShips(
        onSuccess: (List<ActiveShipInfo>) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = apiServiceV2.getActiveShips()
                if (response.isSuccessful) {
                    response.body()?.let(onSuccess) ?: onError("داده‌های دریافتی خالی است")
                } else {
                    onError("خطا در دریافت اطلاعات کشتی‌های فعال")
                }
            } catch (e: Exception) {
                onError("خطا در ارتباط با سرور: ${e.message}")
            }
        }
    }

    fun refreshRealTimeLoadingData(
        onSuccess: (RealTimeDataResponse) -> Unit,
        onEmpty: () -> Unit,
        onError: (String) -> Unit,
        onFinally: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = apiServiceV2.getRealTimeLoadingData()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) onSuccess(body) else onEmpty()
                } else {
                    onError("خطا در دریافت اطلاعات: ${response.code()}")
                }
            } catch (_: Exception) {
                onError("خطا در ارتباط با سرور")
            } finally {
                onFinally()
            }
        }
    }
}

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
            groupedShips.mapValues { (_, ships) ->
                ships.filter { it.entryVouchers > 0 }
            }.filter { (_, ships) -> ships.isNotEmpty() }
        }
        ShipFilterTab.COMPLETED -> {
            groupedShips.mapValues { (_, ships) ->
                ships.filter { ship ->
                    val total = ship.entryVouchers + ship.exitVouchers
                    total > 0 && ship.exitVouchers >= total
                }
            }.filter { (_, ships) -> ships.isNotEmpty() }
        }
    }
}

private fun navigateToCargoDetailsScreen(
    navController: NavController,
    shipInfo: ActiveShipInfo,
    sharedViewModel: com.atk.atk_cargo.api.CargoViewModel? = null
) {
    if (sharedViewModel != null) {
        val initialInfo = com.atk.atk_cargo.data.model.InitialInfo(
            shipName = shipInfo.shipName,
            loadingWarehouse = shipInfo.loadingWarehouse,
            cargoType = shipInfo.cargoType,
            shippingCompany = shipInfo.shippingCompany,
            cargoWeight = 0f,
            loadingQuotaNumber = shipInfo.loadingQuotaNumber.toIntOrNull() ?: 0,
            remainingWeight = 0f,
            totalNetWeight = 0f,
            averageNetWeight = 0f,
            remainingServices = 0
        )
        sharedViewModel.setInitialInfo(initialInfo.toDomain())
        sharedViewModel.loadCargoInfoList(
            quotaNumber = shipInfo.loadingQuotaNumber,
            shippingCompany = shipInfo.shippingCompany,
            warehouse = shipInfo.loadingWarehouse,
            cargoType = shipInfo.cargoType
        )
    } else {
        try {
            val encodedShippingCompany = java.net.URLEncoder.encode(shipInfo.shippingCompany, "UTF-8")
            val encodedWarehouse = java.net.URLEncoder.encode(shipInfo.loadingWarehouse, "UTF-8")
            val encodedCargoType = java.net.URLEncoder.encode(shipInfo.cargoType, "UTF-8")

            Log.d("Navigation", "Navigating to CargoDetails for quota: ${shipInfo.loadingQuotaNumber}")
            navController.navigateToCargoDetails(
                quotaNumber = shipInfo.loadingQuotaNumber,
                shippingCompany = encodedShippingCompany,
                warehouse = encodedWarehouse,
                cargoType = encodedCargoType
            )
        } catch (e: Exception) {
            Log.e("Navigation", "Navigation error: ${e.message}")
            e.printStackTrace()
        }
    }
}

@Composable
fun CargoCounterScreen(
    navController: NavController,
    sharedViewModel: com.atk.atk_cargo.api.CargoViewModel? = null
) {
    val context = LocalContext.current
    val userPreferencesManager = koinInject<UserPreferencesManager>()
    
    LaunchedEffect(Unit) {
        try {
            val result = validateServerSession(userPreferencesManager)
            result.fold(
                onSuccess = {
                    // Session معتبر است، ادامه می‌دهد
                },
                onFailure = {
                    navController.navigateToHome()
                }
            )
        } catch (e: Exception) {
            Log.e("CargoCounterScreen", "خطا در بررسی وضعیت ورود: ${e.message}")
            navController.navigateToHome()
            return@LaunchedEffect
        }
    }
    
    val viewModel: CargoCounterViewModel = koinViewModel()
    
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var activeShips by remember { mutableStateOf<List<ActiveShipInfo>>(emptyList()) }
    
    val selectedShipNames by viewModel.selectedShipNames.collectAsStateWithLifecycle(initialValue = emptySet())
    val filteredShips = activeShips.filter { selectedShipNames.contains(it.shipName) }
    val groupedShips = filteredShips.groupBy { it.shipName }

    val expandedShipName by viewModel.expandedShipName.collectAsStateWithLifecycle(initialValue = null)
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle(initialValue = ShipFilterTab.LOADING)
    
    val isDarkTheme = isSystemInDarkTheme()
    val colorSelector = remember { ColorSelector(cardColors) }
    val shipColorMap = remember { mutableStateOf<Map<String, Color>>(emptyMap()) }
    var snackbarMessage by remember { mutableStateOf<CargoSnackbarMessage?>(null) }
    var showShipSelectionDialog by remember { mutableStateOf(false) }

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
        viewModel.refreshRealTimeLoadingData(
            onSuccess = { realTimeDataResponse ->
                showUpdateMessage("اطلاعات با موفقیت بروزرسانی شد", MessageType.SUCCESS)
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
            },
            onEmpty = { showUpdateMessage("داده‌های دریافتی خالی است", MessageType.WARNING) },
            onError = { message -> showUpdateMessage(message, MessageType.ERROR) },
            onFinally = { isRefreshing = false }
        )
    }

    fun refreshData() {
        isRefreshing = true
        viewModel.loadActiveShips(
            onSuccess = { ships ->
                activeShips = ships
                updateShipColors(ships)
                updateStatistics()
            },
            onError = { message ->
                isRefreshing = false
                coroutineScope.launch { snackbarHostState.showSnackbar(message) }
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.loadActiveShips(
            onSuccess = { ships ->
                activeShips = ships
                updateShipColors(ships)
                updateStatistics()
            },
            onError = { message ->
                coroutineScope.launch { snackbarHostState.showSnackbar(message) }
            }
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                delay(30000.milliseconds)
                updateStatistics()
            }
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
                    
                    val loadingCount = groupedShips.count { (_, ships) ->
                        ships.any { it.entryVouchers > 0 }
                    }
                    val completedCount = groupedShips.count { (_, ships) ->
                        ships.any { ship ->
                            val total = ship.entryVouchers + ship.exitVouchers
                            total > 0 && ship.exitVouchers >= total
                        }
                    }
                    
                    if (selectedShipNames.isNotEmpty()) {
                        val allCount = groupedShips.size
                        TabBar(
                            selectedTab = selectedTab,
                            onTabSelected = { tab -> viewModel.updateSelectedTab(tab) },
                            allCount = allCount,
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
                                Box(
                                    modifier = Modifier
                                        .size(88.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(CargoCounterAccentBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsBoat,
                                        contentDescription = null,
                                        tint = CargoCounterAccent,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "لطفاً کشتی مورد نظر را انتخاب کنید",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { showShipSelectionDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CargoCounterAccent
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsBoat,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text("انتخاب کشتی", fontWeight = FontWeight.Bold)
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
                                navigateToCargoDetailsScreen(navController, selectedShip, sharedViewModel)
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
