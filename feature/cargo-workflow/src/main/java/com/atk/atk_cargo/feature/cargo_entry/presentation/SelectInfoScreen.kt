package com.atk.atk_cargo.feature.cargo_entry.presentation

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.atk.atk_cargo.api.SessionValidationOutcome
import com.atk.atk_cargo.api.TokenStore
import com.atk.atk_cargo.api.validateServerSession
import com.atk.atk_cargo.core.startup.LocalStartupViewModel
import com.atk.atk_cargo.core.ui.components.ColorSelector
import com.atk.atk_cargo.core.ui.components.adjustColorForTheme
import com.atk.atk_cargo.core.ui.components.cardColors
import com.atk.atk_cargo.data.model.ActiveShipInfo
import com.atk.atk_cargo.data.model.InitialInfo
import com.atk.atk_cargo.data.model.MatchingQuota
import com.atk.atk_cargo.data.model.MessageType
import com.atk.atk_cargo.data.model.RealTimeLoadingData
import com.atk.atk_cargo.data.repository.ReportsRepository
import com.atk.atk_cargo.domain.model.toDomain
import com.atk.atk_cargo.feature.cargo.viewmodel.CargoViewModel
import com.atk.atk_cargo.feature.cargo_entry.presentation.components.AnimatedHeader
import com.atk.atk_cargo.feature.cargo_entry.presentation.components.QuotaEntryDialog
import com.atk.atk_cargo.feature.cargo_entry.presentation.components.QuotaSelectionDialog
import com.atk.atk_cargo.feature.cargo_entry.presentation.components.ShipSelectionDialog
import com.atk.atk_cargo.feature.cargo_entry.presentation.components.StatusSnackbar
import com.atk.atk_cargo.feature.cargo_registration.navigation.navigateToCargoRegistration
import com.google.gson.Gson
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

// دسترسی به شبکه از طریق Repository؛ internal است چون ActiveQuotasDialogSection.kt هم آن را استفاده می‌کند
internal val reportsRepository by lazy { ReportsRepository() }

internal val QuotasAccent: Color
    @Composable get() = MaterialTheme.colorScheme.primary

internal val QuotasAccentBg: Color
    @Composable get() = MaterialTheme.colorScheme.primaryContainer

internal val QuotasAccentBorder: Color
    @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)

internal val QuotasWarning: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFE8A855) else Color(0xFFC2760A)

internal val QuotasWarningBg: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFE8A855).copy(alpha = 0.18f) else Color(0xFFF3E4D2)

internal val QuotasWarningBorder: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFE8A855).copy(alpha = 0.35f) else Color(0xFFE7C79B)

internal val QuotasCardBorder: Color
    @Composable get() = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

internal val QuotasMutedBg: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

internal val QuotasMutedText: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

internal val QuotasTitleColor: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface

internal val QuotasScreenBg: Color
    @Composable get() = MaterialTheme.colorScheme.surface

@Composable
fun SelectInfoScreenContent(
    navController: NavController,
    viewModel: CargoViewModel,
    onSessionInvalid: () -> Unit
) {
    val context = LocalContext.current
    val userPreferencesManager = koinInject<TokenStore>()
    val startupViewModel = LocalStartupViewModel.current

    LaunchedEffect(Unit) {
        try {
            when (validateServerSession(userPreferencesManager)) {
                SessionValidationOutcome.Valid -> {
 // نشست معتبر است، ادامه می‌دهد
                }
                SessionValidationOutcome.Invalid -> {
 // نشست واقعاً باطل شده؛ پیام نمایش داده می‌شود و کاربر به صفحه‌ی ورود بازمی‌گردد
                    startupViewModel.notifySessionExpired()
                }
                SessionValidationOutcome.NetworkError -> {
                    startupViewModel.showMessage("خطا در برقراری ارتباط با سرور. لطفاً دوباره تلاش کنید.")
                    onSessionInvalid()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("SelectInfoScreen", "خطا در بررسی وضعیت ورود: ${e.message}")
            startupViewModel.showMessage("خطا در بررسی وضعیت ورود. لطفاً دوباره تلاش کنید.")
            onSessionInvalid()
        }
    }

    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var activeShips by remember { mutableStateOf<List<ActiveShipInfo>>(emptyList()) }
    var isChecking by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogContent by remember { mutableStateOf<@Composable () -> Unit>({}) }
    var selectedShip by remember { mutableStateOf<ActiveShipInfo?>(null) }
    val isDarkTheme = isSystemInDarkTheme()
    val colorSelector = remember { ColorSelector(cardColors) }
    val shipColorMap = remember { mutableStateOf<Map<String, Color>>(emptyMap()) }
    var snackbarMessage by remember { mutableStateOf<SnackbarMessage?>(null) }
    var showShipSelectionDialog by remember { mutableStateOf(false) }
    val cargoUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedShipNames = cargoUiState.selectedShipNames
    val filteredShips = remember(activeShips, selectedShipNames) {
        activeShips.filter { selectedShipNames.contains(it.shipName) }
    }
    val groupedShips = remember(filteredShips) {
        filteredShips.groupBy { it.shipName }
    }
    var showActiveQuotasDialog by remember { mutableStateOf(false) }
    var isQuotaEntryDialogOpen by remember { mutableStateOf(false) }
    var realTimeDataList by remember { mutableStateOf<List<RealTimeLoadingData>>(emptyList()) }
 // وقتی از دیالوگ «انتخاب و مدیریت کشتی‌ها» فقط یک کشتی انتخاب و تأیید شود، دیالوگ ورود کوتاژ همان کشتی به‌صورت خودکار باز می‌شود
    var autoOpenQuotaEntryForShip by remember { mutableStateOf<String?>(null) }

    fun updateShipColors(ships: List<ActiveShipInfo>) {
        colorSelector.reset()
        shipColorMap.value = ships.associate { ship ->
            ship.shipName to adjustColorForTheme(colorSelector.getNextColor(), isDarkTheme)
        }
    }

    fun showUpdateMessage(message: String, type: MessageType) {
        snackbarMessage = SnackbarMessage(message, type)
    }

    fun processScannedQuota(scannedCode: String): String {
        val trimmedCode = scannedCode.trim()
        return when {
            trimmedCode.contains("-") -> trimmedCode.split("-").last()
            trimmedCode.startsWith("990000") -> trimmedCode.substring(6)
            else -> trimmedCode
        }
    }



    fun showQuotaSelectionDialog(matchingQuotas: List<MatchingQuota>, ship: ActiveShipInfo) {
        showDialog = true
        dialogContent = @Composable {
            QuotaSelectionDialog(
                matchingQuotas = matchingQuotas,
                ship = ship,
                onQuotaSelected = { selectedQuota ->
                    showDialog = false
                    navigateToRegisterCargoActivity(navController, selectedQuota, viewModel)
                },
                onDismiss = {
                    showDialog = false
                }
            )
        }
    }

    fun updateStatistics() {
        coroutineScope.launch {
            try {
                reportsRepository.getRealTimeLoadingData()
                showUpdateMessage("اطلاعات با موفقیت بروزرسانی شد", MessageType.SUCCESS)
                viewModel.updateInfoValues()
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
                loadActiveShips { ships ->
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

    fun fetchRealTimeData() {
        coroutineScope.launch {
            try {
                realTimeDataList = reportsRepository.getRealTimeLoadingData().data
            } catch (_: Exception) {
 // خطایی رخ داده، اما ادامه می‌دهیم با داده‌های ActiveShipInfo
            }
        }
    }

    val barcodeLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { scannedCode ->
            val processedCode = processScannedQuota(scannedCode)
            coroutineScope.launch {
                isChecking = true
                try {
                    selectedShip?.let { ship ->
                        val response = viewModel.checkQuotaExistenceCargo(processedCode, ship.shipName)
                        when {
                            !response.exists -> {
                                showUpdateMessage(
                                    "خطا: کوتاژ $processedCode برای کشتی ${ship.shipName} یافت نشد.",
                                    MessageType.ERROR
                                )
                            }
                            response.matchingQuotas.isEmpty() -> {
                                showUpdateMessage(
                                    "خطا: هیچ کوتاژ منطبقی برای کد $processedCode یافت نشد.",
                                    MessageType.ERROR
                                )
                            }
                            response.matchingQuotas.size > 1 -> {
                                showQuotaSelectionDialog(response.matchingQuotas, ship)
                            }
                            else -> {
                                val matchingQuota = response.matchingQuotas.first()
                                if (matchingQuota.shipName == ship.shipName) {
                                    navigateToRegisterCargoActivity(navController, matchingQuota, viewModel)
                                } else {
                                    showUpdateMessage(
                                        "خطا: کوتاژ $processedCode متعلق به کشتی ${matchingQuota.shipName} است، نه کشتی ${ship.shipName}!",
                                        MessageType.ERROR
                                    )
                                }
                            }
                        }
                    } ?: run {
                        showUpdateMessage("لطفاً ابتدا یک کشتی را انتخاب کنید.", MessageType.ERROR)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    showUpdateMessage(
                        "خطا در بررسی کوتاژ: ${e.message}",MessageType.ERROR)
                } finally {
                    isChecking = false
                }
            }
        }
    }

    fun startBarcodeScanner() {
        val options = ScanOptions()
            .setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
            .setPrompt("اسکن کوتاژ")
            .setCameraId(0)
            .setBeepEnabled(false)
            .setBarcodeImageEnabled(true)
            .setOrientationLocked(false)

        barcodeLauncher.launch(options)
    }

    fun handleQuotaEntry(ship: ActiveShipInfo, enteredQuota: String) {
        coroutineScope.launch {
            if (enteredQuota.isNotEmpty()) {
                val processedCode = processScannedQuota(enteredQuota)
                val validationResult = validateQuota(processedCode, listOf(ship), viewModel)
                validationResult.fold(
                    onSuccess = {
                        try {
                            val response = viewModel.checkQuotaExistenceCargo(processedCode, ship.shipName)
                            when {
                                response.matchingQuotas.size > 1 -> {
                                    showQuotaSelectionDialog(response.matchingQuotas, ship)
                                }
                                else -> {
                                    navigateToRegisterCargoActivity(navController, response.matchingQuotas[0], viewModel)
                                }
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            showUpdateMessage("خطا در بررسی کوتاژ: ${e.message}", MessageType.ERROR)
                        }
                    },
                    onFailure = { error ->
                        showUpdateMessage(error.message ?: "خطای نامشخص", MessageType.ERROR)
                    }
                )
            } else {
                selectedShip = ship
                startBarcodeScanner()
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
        fetchRealTimeData()
    }

    LaunchedEffect(selectedShipNames, activeShips) {
 // اگر کشتی‌های فعال بارگذاری شده و هیچ کشتی انتخاب نشده باشد
        if (activeShips.isNotEmpty() && selectedShipNames.isEmpty()) {
            delay(500.milliseconds) // تاخیر کوتاه برای اطمینان از بارگذاری کامل UI
            showShipSelectionDialog = true
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                delay(30000.milliseconds)
 // اگر دیالوگ ورود کوتاژ باز است، بروزرسانی نکن
                if (!isQuotaEntryDialogOpen) {
                    refreshData()
                    fetchRealTimeData()
                }
            }
        }
    }

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
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
                    onClickCount = { showActiveQuotasDialog = true },
                    onSelectShips = { showShipSelectionDialog = true },
                    selectedShipsCount = selectedShipNames.size
                )

                Spacer(modifier = Modifier.height(12.dp))

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
                    GroupedShipList(
                        groupedShips = groupedShips,
                        realTimeDataList = realTimeDataList,
                        onEnter = { ship, enteredQuota ->
                            handleQuotaEntry(ship, enteredQuota)
                        },
                        shipColorMap = shipColorMap.value,
                        onDialogStateChange = { isOpen ->
                            isQuotaEntryDialogOpen = isOpen
                        },
                        autoOpenShipName = autoOpenQuotaEntryForShip,
                        onAutoOpenConsumed = { autoOpenQuotaEntryForShip = null }
                    )
                }
            }

            if (showDialog) {
                dialogContent()
            }

 // دیالوگ انتخاب کشتی‌ها
            if (showShipSelectionDialog) {
                ShipSelectionDialog(
                    ships = activeShips,
                    selectedShipNames = selectedShipNames,
                    onSelectShip = { selectedShips ->
                        viewModel.updateSelectedShips(selectedShips)
 // فقط وقتی دقیقاً یک کشتی انتخاب شده، دیالوگ ورود کوتاژ آن به‌صورت خودکار باز می‌شود
                        autoOpenQuotaEntryForShip = selectedShips.singleOrNull()
                    },
                    onDismiss = { showShipSelectionDialog = false }
                )
            }

            if (showActiveQuotasDialog) {
                ActiveQuotasDialog(
                    onDismiss = { showActiveQuotasDialog = false }
                )
            }

            if (isChecking) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
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

@Composable
private fun GroupedShipList(
    groupedShips: Map<String, List<ActiveShipInfo>>,
    realTimeDataList: List<RealTimeLoadingData>,
    onEnter: (ActiveShipInfo, String) -> Unit,
    shipColorMap: Map<String, Color>,
    onDialogStateChange: (Boolean) -> Unit,
    autoOpenShipName: String? = null,
    onAutoOpenConsumed: () -> Unit = {}
) {
    var isDialogOpen by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        groupedShips.forEach { (shipName, ships) ->
            item(key = shipName) {
 // یافتن داده‌های لحظه‌ای مربوط به این کشتی
                val shipRealTimeData = remember(realTimeDataList, shipName) {
                    realTimeDataList.filter { it.shipName == shipName }
                }

                if (shipRealTimeData.isNotEmpty()) {
 // استفاده از داده‌های لحظه‌ای
                    ShipGroupWithRealTimeData(
                        shipName = shipName,
                        realTimeData = shipRealTimeData,
                        ships = ships, // برای ارسال به دیالوگ
                        onEnter = onEnter,
                        color = shipColorMap[shipName] ?: MaterialTheme.colorScheme.primary,
                        onDialogStateChange = { isOpen ->
                            isDialogOpen = isOpen
                            onDialogStateChange(isOpen)
                        },
                        autoOpen = autoOpenShipName == shipName,
                        onAutoOpenConsumed = onAutoOpenConsumed
                    )
                } else {
 // استفاده از داده‌های معمولی
                    ShipGroup(
                        shipName = shipName,
                        ships = ships,
                        onEnter = onEnter,
                        color = shipColorMap[shipName] ?: MaterialTheme.colorScheme.primary,
                        onDialogStateChange = { isOpen ->
                            isDialogOpen = isOpen
                            onDialogStateChange(isOpen)
                        },
                        autoOpen = autoOpenShipName == shipName,
                        onAutoOpenConsumed = onAutoOpenConsumed
                    )
                }
            }
        }
    }
}

@Composable
private fun ShipGroup(
    shipName: String,
    ships: List<ActiveShipInfo>,
    onEnter: (ActiveShipInfo, String) -> Unit,
    color: Color,
    onDialogStateChange: (Boolean) -> Unit,
    autoOpen: Boolean = false,
    onAutoOpenConsumed: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }

 // Update dialog state
    LaunchedEffect(showDialog) {
        onDialogStateChange(showDialog)
    }

 // باز شدن خودکار دیالوگ ورود کوتاژ وقتی این کشتی تنها انتخاب تازه از دیالوگ انتخاب کشتی‌ها بوده
    LaunchedEffect(autoOpen) {
        if (autoOpen) {
            showDialog = true
            onAutoOpenConsumed()
        }
    }

    val total = ships.sumOf { it.entryVouchers + it.exitVouchers }
    val completed = ships.sumOf { it.exitVouchers }
    val remaining = total - completed

    ShipCardDesign(
        shipName = shipName,
        cargoType = ships.firstOrNull()?.cargoType ?: "",
        total = total,
        completed = completed,
        remaining = remaining,
        color = color,
        onClick = { showDialog = true }
    )

    QuotaEntryDialog(
        showDialog = showDialog,
        onDismiss = { showDialog = false },
        onConfirm = { enteredQuota ->
            val matchingShip = ships.find { it.loadingQuotaNumber.endsWith(enteredQuota) }
            if (matchingShip != null) {
                onEnter(matchingShip, enteredQuota)
                showDialog = false
            }
        },
        onScanBarcode = {
            onEnter(ships.first(), "")
            showDialog = false
        },
        shipName = shipName,
        ships = ships
    )
}

@Composable
private fun ShipGroupWithRealTimeData(
    shipName: String,
    realTimeData: List<RealTimeLoadingData>,
    ships: List<ActiveShipInfo>,
    onEnter: (ActiveShipInfo, String) -> Unit,
    color: Color,
    onDialogStateChange: (Boolean) -> Unit,
    autoOpen: Boolean = false,
    onAutoOpenConsumed: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(showDialog) {
        onDialogStateChange(showDialog)
    }

 // باز شدن خودکار دیالوگ ورود کوتاژ وقتی این کشتی تنها انتخاب تازه از دیالوگ انتخاب کشتی‌ها بوده
    LaunchedEffect(autoOpen) {
        if (autoOpen) {
            showDialog = true
            onAutoOpenConsumed()
        }
    }

    val totalVouchers = realTimeData.sumOf { it.entryVouchers + it.exitVouchers }
    val completedVouchers = realTimeData.sumOf { it.exitVouchers }
    val remainingVouchers = totalVouchers - completedVouchers

    ShipCardDesign(
        shipName = shipName,
        cargoType = realTimeData.firstOrNull()?.cargoType ?: "",
        total = totalVouchers,
        completed = completedVouchers,
        remaining = remainingVouchers,
        color = color,
        onClick = { showDialog = true }
    )

    QuotaEntryDialog(
        showDialog = showDialog,
        onDismiss = { showDialog = false },
        onConfirm = { enteredQuota ->
            val matchingShip = ships.find { it.loadingQuotaNumber.endsWith(enteredQuota) }
            if (matchingShip != null) {
                onEnter(matchingShip, enteredQuota)
                showDialog = false
            }
        },
        onScanBarcode = {
            onEnter(ships.first(), "")
            showDialog = false
        },
        shipName = shipName,
        ships = ships
    )
}

@Composable
private fun ShipCardDesign(
    shipName: String,
    cargoType: String,
    total: Int,
    completed: Int,
    remaining: Int,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
 // Top Row: Title and Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(color.copy(alpha = 0.16f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBoat,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }

 // نام کشتی و نوع کالا
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = shipName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = color,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    if (cargoType.isNotBlank()) {
                        Spacer(modifier = Modifier.height(1.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = cargoType,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

 // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatChip(label = "کل", value = total, color = color, modifier = Modifier.weight(1f))
                StatChip(label = "خروج", value = completed, color = color, modifier = Modifier.weight(1f))
                StatChip(label = "مانده", value = remaining, color = color, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(9.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = "$value",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// محتوای دیالوگ کوتاژهای فعال به ActiveQuotasContent.kt منتقل شده و پوسته‌ی دیالوگ در ActiveQuotasDialogSection.kt است
private fun navigateToRegisterCargoActivity(
    navController: NavController,
    selectedQuota: MatchingQuota,
    viewModel: CargoViewModel? = null
) {
    val initialInfo = InitialInfo(
        shipName = selectedQuota.shipName,
        loadingWarehouse = selectedQuota.warehouse,
        cargoType = selectedQuota.cargoType,
        shippingCompany = selectedQuota.shippingCompany,
        cargoWeight = 0f,
        loadingQuotaNumber = selectedQuota.quotaNumber.toIntOrNull() ?: 0,
        remainingWeight = 0f,
        totalNetWeight = 0f,
        averageNetWeight = 0f,
        remainingServices = 0,
        cargoOwner = selectedQuota.cargoOwner ?: ""
    )
    if (viewModel != null) {
        viewModel.setInitialInfo(initialInfo.toDomain())
        viewModel.refreshCargoInfo()
    } else {
        val json = Gson().toJson(initialInfo)
        navController.navigateToCargoRegistration(json, selectedQuota.quotaNumber)
    }
}

private suspend fun loadActiveShips(
    onSuccess: (List<ActiveShipInfo>) -> Unit
) {
    try {
        onSuccess(reportsRepository.getActiveShips())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e("LoadActiveShips", "استثنا در بارگیری کشتی‌های فعال", e)
        throw e
    }
}

private suspend fun validateQuota(
    quotaNumber: String,
    ships: List<ActiveShipInfo>,
    viewModel: CargoViewModel
): Result<ActiveShipInfo> {
    val matchingShip = ships.find { it.loadingQuotaNumber.endsWith(quotaNumber) }
    return if (matchingShip != null) {
        val response = viewModel.checkQuotaExistenceCargo(quotaNumber, matchingShip.shipName)
        if (response.exists) {
            Result.success(matchingShip)
        } else {
            Result.failure(Exception("کوتاژ $quotaNumber برای کشتی ${matchingShip.shipName} یافت نشد."))
        }
    } else {
        Result.failure(Exception("کوتاژ $quotaNumber برای کشتی انتخاب شده یافت نشد."))
    }
}

data class SnackbarMessage(
    val message: String,
    val type: MessageType
)

@Composable
fun StatusSnackbar(
    message: SnackbarMessage,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    StatusSnackbar(
        message = message,
        isVisible = isVisible,
        onDismiss = onDismiss,
        modifier = modifier
    )
}

@Composable
fun ShipSelectionDialog(
    ships: List<ActiveShipInfo>,
    selectedShipNames: Set<String>,
    onSelectShip: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    ShipSelectionDialog(
        ships = ships,
        selectedShipNames = selectedShipNames,
        onSelectShip = onSelectShip,
        onDismiss = onDismiss
    )
}

