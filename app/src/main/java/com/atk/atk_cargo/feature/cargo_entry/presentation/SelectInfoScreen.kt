package com.atk.atk_cargo.feature.cargo_entry.presentation

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.atk.atk_cargo.api.ActiveShipInfo
import com.atk.atk_cargo.api.CargoViewModel
import com.atk.atk_cargo.api.ColorSelector
import com.atk.atk_cargo.api.InitialInfo
import com.atk.atk_cargo.api.MatchingQuota
import com.atk.atk_cargo.api.MessageType
import com.atk.atk_cargo.api.RealTimeLoadingData
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.api.adjustColorForTheme
import com.atk.atk_cargo.api.cardColors
import com.atk.atk_cargo.api.formatNumber
import com.atk.atk_cargo.api.validateServerSession
import com.atk.atk_cargo.data.repository.ReportsRepository
import com.atk.atk_cargo.feature.cargo_entry.presentation.components.AnimatedHeader
import com.atk.atk_cargo.feature.cargo_entry.presentation.components.QuotaEntryDialog
import com.atk.atk_cargo.feature.cargo_entry.presentation.components.QuotaSelectionDialog
import com.atk.atk_cargo.feature.cargo_entry.presentation.components.ShipSelectionDialog
import com.atk.atk_cargo.feature.cargo_entry.presentation.components.StatusSnackbar
import com.atk.atk_cargo.feature.cargo_registration.navigation.navigateToCargoRegistration
import com.atk.atk_cargo.feature.home.navigation.navigateToHome
import com.google.gson.Gson
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

// دسترسی به شبکه از طریق Repository، نه مستقیم از RetrofitClient در کد UI
// internal (نه private) چون توسط ActiveQuotasDialogSection.kt هم استفاده می‌شود
internal val reportsRepository by lazy { ReportsRepository(RetrofitClient.apiService) }

internal val QuotasAccent: Color
    @Composable get() = MaterialTheme.colorScheme.primary

internal val QuotasAccentBg: Color
    @Composable get() = MaterialTheme.colorScheme.primaryContainer

private val QuotasAccentBorder: Color
    @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)

internal val QuotasWarning: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFE8A855) else Color(0xFFC2760A)

private val QuotasWarningBg: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFE8A855).copy(alpha = 0.18f) else Color(0xFFF3E4D2)

private val QuotasWarningBorder: Color
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
fun SelectInfoScreenContent(navController: NavController, viewModel: CargoViewModel) {
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
            Log.e("SelectInfoScreen", "خطا در بررسی وضعیت ورود: ${e.message}")
            navController.navigateToHome()
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
    val selectedShipNames by viewModel.selectedShipNames.collectAsStateWithLifecycle(initialValue = emptySet())
    val filteredShips = remember(activeShips, selectedShipNames) {
        activeShips.filter { selectedShipNames.contains(it.shipName) }
    }
    val groupedShips = remember(filteredShips) {
        filteredShips.groupBy { it.shipName }
    }
    var showActiveQuotasDialog by remember { mutableStateOf(false) }
    var isQuotaEntryDialogOpen by remember { mutableStateOf(false) }
    var realTimeDataList by remember { mutableStateOf<List<RealTimeLoadingData>>(emptyList()) }

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
                        }
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
    onDialogStateChange: (Boolean) -> Unit
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
                        }
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
                        }
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
    onDialogStateChange: (Boolean) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    // Update dialog state
    LaunchedEffect(showDialog) {
        onDialogStateChange(showDialog)
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
    onDialogStateChange: (Boolean) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(showDialog) {
        onDialogStateChange(showDialog)
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

private fun extractLastDigits(quotaNumber: String): String {
    return if (quotaNumber.length > 4) {
        quotaNumber.takeLast(4)
    } else {
        quotaNumber
    }
}

// دیالوگ کوتاژهای فعال (ActiveQuotasDialog) و کامپوننت‌های اختصاصی‌اش
// به ActiveQuotasDialogSection.kt منتقل شدند (A1-6، بازسازی ساختاری).

enum class FilterState {
    ALL, PENDING, COMPLETED
}

enum class ViewMode {
    GROUPED, FLAT
}

// QuotasHeader, StatItem, FilterBar, VerticalDivider به ActiveQuotasDialogSection.kt منتقل شدند.

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
        viewModel.setInitialInfo(initialInfo)
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