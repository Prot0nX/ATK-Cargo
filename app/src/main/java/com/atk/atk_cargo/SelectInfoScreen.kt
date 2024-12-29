package com.atk.atk_cargo

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.atk.atk_cargo.api.ActiveShipInfo
import com.atk.atk_cargo.api.CargoViewModel
import com.atk.atk_cargo.api.ColorSelector
import com.atk.atk_cargo.api.InitialInfo
import com.atk.atk_cargo.api.MatchingQuota
import com.atk.atk_cargo.api.MessageType
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.api.ShiftInfo
import com.atk.atk_cargo.api.adjustColorForTheme
import com.atk.atk_cargo.api.cardColors
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SelectInfoScreenContent(navController: NavController, viewModel: CargoViewModel) {
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var activeShips by remember { mutableStateOf<List<ActiveShipInfo>>(emptyList()) }
    val context = LocalContext.current
    val groupedShips = activeShips.groupBy { it.shipName }
    var currentShiftInfo by remember { mutableStateOf<ShiftInfo?>(null) }
    var isChecking by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogContent by remember { mutableStateOf<@Composable () -> Unit>({}) }
    var selectedShip by remember { mutableStateOf<ActiveShipInfo?>(null) }
    val isDarkTheme = isSystemInDarkTheme()
    val colorSelector = remember { ColorSelector(cardColors) }
    val shipColorMap = remember { mutableStateOf<Map<String, Color>>(emptyMap()) }
    var snackbarMessage by remember { mutableStateOf<SnackbarMessage?>(null) }

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
        dialogContent = {
            QuotaSelectionDialog(
                matchingQuotas = matchingQuotas,
                ship = ship,
                onQuotaSelected = { selectedQuota ->
                    showDialog = false
                    navigateToRegisterCargoActivity(context, selectedQuota)
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
                        updateShipColors(activeShips)
                        currentShiftInfo = realTimeDataResponse.shiftInfo
                        showUpdateMessage("اطلاعات با موفقیت بروزرسانی شد", MessageType.SUCCESS)
                        viewModel.updateInfoValues()
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
                loadActiveShips { ships ->
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
                                    navigateToRegisterCargoActivity(context, matchingQuota)
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
                                    navigateToRegisterCargoActivity(context, response.matchingQuotas[0])
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
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30000)
            refreshData()
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
                    onRefresh = { refreshData() }
                )

                GroupedShipList(
                    groupedShips = groupedShips,
                    onEnter = { ship, enteredQuota ->
                        handleQuotaEntry(ship, enteredQuota)
                    },
                    shipColorMap = shipColorMap.value
                )
            }

            if (showDialog) {
                dialogContent()
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

@Composable
fun StatusSnackbar(
    message: SnackbarMessage,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var animatedVisibility by remember { mutableStateOf(false) }

    val translateY by animateDpAsState(
        targetValue = if (animatedVisibility) 0.dp else 100.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = ""
    )

    val alpha by animateFloatAsState(
        targetValue = if (animatedVisibility) 1f else 0f,
        animationSpec = tween(durationMillis = 350), label = ""
    )

    LaunchedEffect(isVisible) {
        if (isVisible) {
            animatedVisibility = true
            delay(7000)
            animatedVisibility = false
            delay(450)
            onDismiss()
        }
    }

    if (isVisible || animatedVisibility) {
        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 32.dp)
                    .offset(y = translateY)
                    .alpha(alpha),
                shape = RoundedCornerShape(12.dp),
                color = when (message.type) {
                    MessageType.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
                    MessageType.ERROR -> MaterialTheme.colorScheme.errorContainer
                    MessageType.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
                },
                border = BorderStroke(
                    width = 1.dp,
                    color = when (message.type) {
                        MessageType.SUCCESS -> MaterialTheme.colorScheme.primary
                        MessageType.ERROR -> MaterialTheme.colorScheme.error
                        MessageType.WARNING -> MaterialTheme.colorScheme.tertiary
                    }.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .animateContentSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = when (message.type) {
                            MessageType.SUCCESS -> Icons.Outlined.CheckCircle
                            MessageType.ERROR -> Icons.Default.Close
                            MessageType.WARNING -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = when (message.type) {
                            MessageType.SUCCESS -> MaterialTheme.colorScheme.primary
                            MessageType.ERROR -> MaterialTheme.colorScheme.error
                            MessageType.WARNING -> MaterialTheme.colorScheme.tertiary
                        },
                        modifier = Modifier.size(24.dp)
                    )

                    Text(
                        text = message.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = when (message.type) {
                            MessageType.SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
                            MessageType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                            MessageType.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QuotaSelectionDialog(
    matchingQuotas: List<MatchingQuota>,
    ship: ActiveShipInfo,
    onQuotaSelected: (MatchingQuota) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.78f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header Section
                DialogHeader(ship = ship, quotaCount = matchingQuotas.size, onDismiss = onDismiss)

                // Quotas List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(matchingQuotas) { quota ->
                        QuotaItem(
                            quota = quota,
                            matchingQuotas = matchingQuotas,
                            onClick = onQuotaSelected
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogHeader(
    ship: ActiveShipInfo,
    quotaCount: Int,
    onDismiss: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "بستن")
            }
            Text(
                text = "انتخاب کوتاژ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DirectionsBoat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = ship.shipName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.size(24.dp))
            }
        }

        Text(
            text = "$quotaCount کوتاژ مشابه یافت شد",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
fun QuotaItem(
    quota: MatchingQuota,
    matchingQuotas: List<MatchingQuota>,
    onClick: (MatchingQuota) -> Unit
) {
    val differentFields = findDifferentFields(quota, matchingQuotas)
    val isActive = quota.isActive

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isActive) { onClick(quota) }
            .alpha(if (isActive) 1f else 0.9f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !isActive -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                differentFields.isNotEmpty() -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
        ),
        border = when {
            !isActive -> BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            )
            differentFields.isNotEmpty() -> BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
            else -> null
        }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .alpha(if (isActive) 1f else 0.9f)
        ) {
            QuotaHeader(
                quota = quota,
                isActive = isActive,
                hasDifferences = differentFields.isNotEmpty()
            )

            Spacer(modifier = Modifier.height(8.dp))
            QuotaDetails(quota, differentFields, isActive)

            // نشانگر غیرفعال بودن
            if (!isActive) {
                Spacer(modifier = Modifier.height(8.dp))
                InactiveIndicator()
            }
        }
    }
}

@Composable
private fun InactiveIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                RoundedCornerShape(4.dp)
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "این کوتاژ در حال حاضر قابل انتخاب نیست",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun QuotaDetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    isDifferent: Boolean,
    isDisabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = when {
                isDisabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                isDifferent -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            },
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = if (isDisabled) 0.4f else 0.7f
            ),
            modifier = Modifier.width(80.dp)
        )

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (isDifferent) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDisabled) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDisabled) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDisabled) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

private fun findDifferentFields(
    currentQuota: MatchingQuota,
    allQuotas: List<MatchingQuota>
): Set<String> {
    val differentFields = mutableSetOf<String>()
    val sameNumberQuotas = allQuotas.filter { it.quotaNumber == currentQuota.quotaNumber }

    if (sameNumberQuotas.size > 1) {
        if (sameNumberQuotas.map { it.shippingCompany }.distinct().size > 1) {
            differentFields.add("shippingCompany")
        }

        if (sameNumberQuotas.map { it.warehouse }.distinct().size > 1) {
            differentFields.add("warehouse")
        }

        if (sameNumberQuotas.map { it.cargoType }.distinct().size > 1) {
            differentFields.add("cargoType")
        }
    }

    return differentFields
}

@Composable
private fun QuotaHeader(
    quota: MatchingQuota,
    isActive: Boolean,
    hasDifferences: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = quota.quotaNumber,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = when {
                    !isActive -> MaterialTheme.colorScheme.error
                    hasDifferences -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            Spacer(modifier = Modifier.width(8.dp))

            // نمایش برچسب وضعیت
            when {
                !isActive -> {
                    StatusBadge(
                        text = "غیرفعال",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        if (isActive) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "انتخاب",
                tint = if (hasDifferences) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun StatusBadge(
    text: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.1f),
        contentColor = color,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun QuotaDetails(
    quota: MatchingQuota,
    differentFields: Set<String>,
    isActive: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        QuotaDetailItem(
            icon = Icons.Default.Business,
            label = "شرکت باربری",
            value = quota.shippingCompany,
            isDifferent = "shippingCompany" in differentFields,
            isDisabled = !isActive
        )
        QuotaDetailItem(
            icon = Icons.Default.Warehouse,
            label = "انبار",
            value = quota.warehouse,
            isDifferent = "warehouse" in differentFields,
            isDisabled = !isActive
        )
        QuotaDetailItem(
            icon = Icons.Default.Category,
            label = "نوع کالا",
            value = quota.cargoType,
            isDifferent = "cargoType" in differentFields,
            isDisabled = !isActive
        )
    }
}

@Composable
fun AnimatedHeader(
    shipCount: Int,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "بارگیری‌های فعال",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = shipCount.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                UpdateButton(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh
                )
            }
        }
    }
}

@Composable
fun UpdateButton(
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_success))
    var isPlaying by remember { mutableStateOf(false) }
    var playCount by remember { mutableStateOf(0) }

    LaunchedEffect(isRefreshing, playCount) {
        if (isRefreshing || playCount > 0) {
            isPlaying = true
        }
    }

    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        restartOnPlay = false,
        iterations = 1
    )

    IconButton(
        onClick = {
            if (!isRefreshing) {
                onRefresh()
                playCount++
            }
        },
        enabled = !isRefreshing
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(48.dp)
        )
    }

    LaunchedEffect(progress) {
        if (progress == 1f) {
            isPlaying = false
            if (playCount > 0) {
                playCount--
            }
        }
    }
}

@Composable
private fun GroupedShipList(
    groupedShips: Map<String, List<ActiveShipInfo>>,
    onEnter: (ActiveShipInfo, String) -> Unit,
    shipColorMap: Map<String, Color>
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        groupedShips.forEach { (shipName, ships) ->
            item {
                ShipGroup(
                    shipName = shipName,
                    ships = ships,
                    onEnter = onEnter,
                    color = shipColorMap[shipName] ?: MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ShipGroup(
    shipName: String,
    ships: List<ActiveShipInfo>,
    onEnter: (ActiveShipInfo, String) -> Unit,
    color: Color
) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBoat,
                    contentDescription = null,
                    tint = color
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = shipName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "کل: ${ships.sumOf { it.entryVouchers + it.exitVouchers }} | خروج: ${ships.sumOf { it.exitVouchers }} | مانده: ${ships.sumOf { it.entryVouchers + it.exitVouchers } - ships.sumOf { it.exitVouchers }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = color.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Scan QR Code",
                    tint = color
                )
            }
        }
    }

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
fun QuotaEntryDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onScanBarcode: () -> Unit,
    shipName: String,
    ships: List<ActiveShipInfo>
) {
    var quotaEntry by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(showDialog) {
        if (showDialog) {
            quotaEntry = ""
            isError = false
            errorMessage = ""
            delay(100)
            focusRequester.requestFocus()
        }
    }

    if (showDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                // Header Section
                DialogHeader(shipName)

                // Input Section
                DialogContent(
                    quotaEntry = quotaEntry,
                    isError = isError,
                    errorMessage = errorMessage,
                    focusRequester = focusRequester,
                    onQuotaChange = { newValue ->
                        if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                            quotaEntry = newValue
                            isError = false
                            errorMessage = ""
                        }
                    },
                    onDone = {
                        validateAndSubmit(quotaEntry, ships, onConfirm) { msg ->
                            isError = true
                            errorMessage = msg
                        }
                    }
                )

                // Actions Section
                DialogActions(
                    quotaEntry = quotaEntry,
                    onConfirm = {
                        validateAndSubmit(quotaEntry, ships, onConfirm) { msg ->
                            isError = true
                            errorMessage = msg
                        }
                    },
                    onScanBarcode = onScanBarcode,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
private fun DialogHeader(shipName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsBoat,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = shipName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun DialogContent(
    quotaEntry: String,
    isError: Boolean,
    errorMessage: String,
    focusRequester: FocusRequester,
    onQuotaChange: (String) -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier.padding(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 8.dp
        )
    ) {
        OutlinedTextField(
            value = quotaEntry,
            onValueChange = onQuotaChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                letterSpacing = 6.sp,
                fontWeight = FontWeight.Medium
            ),
            placeholder = {
                Text(
                    "4 رقم آخر کوتاژ",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            isError = isError,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            shape = RoundedCornerShape(12.dp)
        )

        AnimatedVisibility(
            visible = isError,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun DialogActions(
    quotaEntry: String,
    onConfirm: () -> Unit,
    onScanBarcode: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onConfirm,
            modifier = Modifier.weight(1f),
            enabled = quotaEntry.length == 4
        ) {
            Text("تأیید")
        }
        Button(
            onClick = onScanBarcode,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Icon(
                Icons.Default.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("اسکن")
        }
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f)
        ) {
            Text("انصراف")
        }
    }
}

private fun validateAndSubmit(
    quotaEntry: String,
    ships: List<ActiveShipInfo>,
    onConfirm: (String) -> Unit,
    onError: (String) -> Unit
) {
    when {
        quotaEntry.length != 4 -> {
            onError("لطفاً 4 رقم آخر کوتاژ را وارد کنید")
        }
        !ships.any { it.loadingQuotaNumber.endsWith(quotaEntry) } -> {
            onError("کوتاژ مورد نظر یافت نشد")
        }
        else -> onConfirm(quotaEntry)
    }
}

private fun navigateToRegisterCargoActivity(context: Context, selectedQuota: MatchingQuota) {
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
        remainingServices = 0
    )
    val intent = Intent(context, RegisterCargoActivity::class.java).apply {
        putExtra("initialInfo", initialInfo)
        putExtra("barcode", selectedQuota.quotaNumber)
    }
    context.startActivity(intent)
}

private suspend fun loadActiveShips(
    onSuccess: (List<ActiveShipInfo>) -> Unit
) {
    try {
        val response = RetrofitClient.apiService.getActiveShips()

        if (response.isSuccessful) {
            response.body()?.let { ships ->
                onSuccess(ships)
            } ?: throw Exception("داده‌های دریافتی خالی است")
        } else {
            throw Exception("خطا در دریافت اطلاعات کشتی‌های فعال")
        }
    } catch (e: Exception) {
        Log.e("LoadActiveShips", "استثنا در بارگیری کشتی‌های فعال", e)
        throw Exception("خطا در ارتباط با سرور: ${e.message}")
    }
}

data class SnackbarMessage(
    val message: String,
    val type: MessageType
)
