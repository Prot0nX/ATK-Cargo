package com.atk.atk_cargo

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
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
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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

    // New state for ActiveQuotasDialog
    var showActiveQuotasDialog by remember { mutableStateOf(false) }

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
                    onRefresh = { refreshData() },
                    onClickCount = { showActiveQuotasDialog = true }
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

            // New ActiveQuotasDialog integration
            if (showActiveQuotasDialog) {
                ActiveQuotasDialog(
                    activeShips = activeShips,
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
fun AnimatedHeader(
    shipCount: Int,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onClickCount: () -> Unit
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
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize * 0.9f
                ),
                fontWeight = FontWeight.Bold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clickable badge with animation effect
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onClickCount)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { onClickCount() }
                            )
                        }
                ) {
                    val pulseState = remember { androidx.compose.runtime.mutableFloatStateOf(1f) }

                    LaunchedEffect(Unit) {
                        // Create a subtle pulse animation for the badge
                        while (true) {
                            animate(
                                initialValue = 1f,
                                targetValue = 1.1f,
                                animationSpec = tween(
                                    durationMillis = 800,
                                    easing = FastOutSlowInEasing
                                )
                            ) { value, _ -> pulseState.floatValue = value }

                            animate(
                                initialValue = 1.1f,
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = 800,
                                    easing = FastOutSlowInEasing
                                )
                            ) { value, _ -> pulseState.floatValue = value }

                            delay(1500)
                        }
                    }

                    Badge(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = pulseState.floatValue
                                scaleY = pulseState.floatValue
                            }
                    ) {
                        Text(
                            text = shipCount.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = MaterialTheme.typography.titleMedium.fontSize * 1.1f
                            ),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
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

    val translateX by animateDpAsState(
        targetValue = if (animatedVisibility) 0.dp else 300.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow
        ), label = ""
    )

    val alpha by animateFloatAsState(
        targetValue = if (animatedVisibility) 1f else 0f,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ), label = ""
    )

    LaunchedEffect(isVisible) {
        if (isVisible) {
            animatedVisibility = true
            delay(3000)
            animatedVisibility = false
            delay(600)
            onDismiss()
        }
    }

    if (isVisible || animatedVisibility) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            Surface(
                modifier = Modifier
                    .padding(bottom = 34.dp)
                    .width(290.dp)
                    .wrapContentHeight()
                    .offset(x = translateX)
                    .alpha(alpha)
                    .graphicsLayer {
                        rotationY = (translateX.value / 300f) * -10f
                    },
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    bottomStart = 12.dp,
                    topEnd = 0.dp,
                    bottomEnd = 0.dp
                ),
                color = when (message.type) {
                    MessageType.SUCCESS -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                    MessageType.ERROR -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
                    MessageType.WARNING -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f)
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = message.message,
                        style = MaterialTheme.typography.bodyMedium,
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
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize * 0.9f
                ),
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
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = MaterialTheme.typography.titleMedium.fontSize * 1.1f
                        ),
                        fontWeight = FontWeight.Bold,
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
    var playCount by remember { mutableIntStateOf(0) }

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
fun ActiveQuotasDialog(
    activeShips: List<ActiveShipInfo>,
    onDismiss: () -> Unit
) {
    val groupedShips = activeShips.groupBy { it.shipName }
    val totalQuotas = activeShips.size
    val totalVouchers = activeShips.sumOf { it.entryVouchers + it.exitVouchers }
    val completedVouchers = activeShips.sumOf { it.exitVouchers }
    
    // اضافه کردن متغیر جستجو
    var searchQuery by remember { mutableStateOf("") }
    
    // اضافه کردن متغیر برای نگهداری کشتی باز شده
    var expandedShipName by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with overall statistics
                ActiveQuotasDialogHeader(
                    totalQuotas = totalQuotas,
                    totalVouchers = totalVouchers,
                    completedVouchers = completedVouchers,
                    onDismiss = onDismiss
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // اضافه کردن فیلد جستجو
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    placeholder = { Text("جستجوی انبار...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "جستجو",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "پاک کردن",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Ships and quotas details
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Sort ships by remaining vouchers in descending order, then by total vouchers
                    // Filter out ships with no vouchers
                    groupedShips.entries
                        .filter { (_, ships) ->
                            ships.any { it.entryVouchers + it.exitVouchers > 0 }
                        }
                        .sortedWith(
                            compareByDescending<Map.Entry<String, List<ActiveShipInfo>>> { (_, ships) ->
                                val total = ships.sumOf { it.entryVouchers + it.exitVouchers }
                                val completed = ships.sumOf { it.exitVouchers }
                                total - completed  // Remaining vouchers
                            }.thenByDescending { (_, ships) ->
                                ships.sumOf { it.entryVouchers + it.exitVouchers }  // Total vouchers
                            }
                        )
                        .forEach { (shipName, ships) ->
                            // فیلتر کردن کشتی‌ها بر اساس جستجو
                            val filteredShips = if (searchQuery.isEmpty()) {
                                ships.filter { it.entryVouchers + it.exitVouchers > 0 }
                            } else {
                                ships.filter { 
                                    (it.entryVouchers + it.exitVouchers > 0) && 
                                    it.loadingWarehouse.contains(searchQuery, ignoreCase = true)
                                }
                            }
                            
                            // نمایش کارت کشتی فقط اگر انبارهای فیلتر شده وجود داشته باشند
                            if (filteredShips.isNotEmpty()) {
                                item {
                                    ShipQuotasCard(
                                        shipName = shipName,
                                        ships = filteredShips,
                                        searchQuery = searchQuery,
                                        expanded = expandedShipName == shipName,
                                        onExpandChange = { isExpanded ->
                                            expandedShipName = if (isExpanded) shipName else null
                                        }
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
private fun ActiveQuotasDialogHeader(
    totalQuotas: Int,
    totalVouchers: Int,
    completedVouchers: Int,
    onDismiss: () -> Unit
) {
    Column {
        // Title and close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Badge(
                    modifier = Modifier.size(38.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = totalQuotas.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "کوتاژهای فعال",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "بستن",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // طراحی جدید و مینیمال برای نمایش آمار
        CompactStatsRow(
            totalVouchers = totalVouchers,
            completedVouchers = completedVouchers,
            remainingVouchers = totalVouchers - completedVouchers
        )
    }
}

@Composable
private fun CompactStatsRow(
    totalVouchers: Int,
    completedVouchers: Int,
    remainingVouchers: Int
) {
    val progressPercentage = if (totalVouchers > 0) {
        (completedVouchers.toFloat() / totalVouchers) * 100f
    } else 0f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // نوار پیشرفت با درصد
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { progressPercentage / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp),
                    color = when {
                        progressPercentage > 90 -> MaterialTheme.colorScheme.primary
                        progressPercentage > 50 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "${progressPercentage.toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        progressPercentage > 90 -> MaterialTheme.colorScheme.primary
                        progressPercentage > 50 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // آمار در یک ردیف افقی
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompactStatItem(
                    value = totalVouchers,
                    label = "کل",
                    icon = Icons.AutoMirrored.Filled.ViewList,
                    color = MaterialTheme.colorScheme.primary
                )
                
                VerticalDivider(
                    modifier = Modifier.height(24.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    thickness = 1.dp
                )
                
                CompactStatItem(
                    value = completedVouchers,
                    label = "خروجی",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = MaterialTheme.colorScheme.tertiary
                )
                
                VerticalDivider(
                    modifier = Modifier.height(24.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    thickness = 1.dp
                )
                
                CompactStatItem(
                    value = remainingVouchers,
                    label = "مانده",
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun CompactStatItem(
    value: Int,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Column(
            horizontalAlignment = Alignment.Start
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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

@Composable
private fun ShipQuotasCard(
    shipName: String,
    ships: List<ActiveShipInfo>,
    searchQuery: String,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit
) {
    // حذف متغیر expanded داخلی
    val totalVouchers = ships.sumOf { it.entryVouchers + it.exitVouchers }
    val completedVouchers = ships.sumOf { it.exitVouchers }
    val remainingVouchers = totalVouchers - completedVouchers
    val progressPercentage = if (totalVouchers > 0) {
        (completedVouchers.toFloat() / totalVouchers) * 100f
    } else 0f

    // Group ships by warehouse and sort warehouses by total vouchers in descending order
    val warehouseGroups = ships.groupBy { it.loadingWarehouse }
    
    // اضافه کردن متغیر برای نگهداری انبار باز شده
    var expandedWarehouse by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandChange(!expanded) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                progressPercentage > 90 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                progressPercentage > 70 -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                progressPercentage > 50 -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = when {
                progressPercentage > 90 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                progressPercentage > 70 -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                progressPercentage > 50 -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Ship header
            ShipCardHeader(
                shipName = shipName,
                quotaCount = ships.size,
                totalVouchers = totalVouchers,
                completedVouchers = completedVouchers,
                remainingVouchers = remainingVouchers,
                progressPercentage = progressPercentage,
                expanded = expanded
            )

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Warehouse sections sorted by total vouchers in descending order
                    warehouseGroups.entries
                        .sortedByDescending { (_, warehouseShips) ->
                            warehouseShips.sumOf { it.entryVouchers + it.exitVouchers }
                        }
                        .forEach { (warehouseName, warehouseShips) ->
                            WarehouseSection(
                                warehouseName = warehouseName,
                                ships = warehouseShips,
                                expanded = expandedWarehouse == warehouseName,
                                onExpandChange = { isExpanded ->
                                    expandedWarehouse = if (isExpanded) warehouseName else null
                                }
                            )
                        }
                }
            }
        }
    }
}

@Composable
private fun WarehouseSection(
    warehouseName: String,
    ships: List<ActiveShipInfo>,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit
) {
    // Filter out ships with zero vouchers
    val shipsWithVouchers = ships.filter { it.entryVouchers + it.exitVouchers > 0 }

    // If no ships have vouchers, don't render this warehouse
    if (shipsWithVouchers.isEmpty()) return

    val totalVouchers = shipsWithVouchers.sumOf { it.entryVouchers + it.exitVouchers }
    val completedVouchers = shipsWithVouchers.sumOf { it.exitVouchers }
    val remainingVouchers = totalVouchers - completedVouchers
    val isCompleted = totalVouchers > 0 && remainingVouchers == 0

    Surface(
        modifier = Modifier.clickable { onExpandChange(!expanded) },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = when {
                isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                remainingVouchers > 0 -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            }
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Show checkmark for completed warehouses
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "تکمیل شده",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Warehouse,
                            contentDescription = null,
                            tint = when {
                                remainingVouchers > 0 -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                else -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = warehouseName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (isCompleted) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(تکمیل شده)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (remainingVouchers > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "$remainingVouchers مانده",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Text(
                        text = "$completedVouchers/$totalVouchers",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // List quotas for this warehouse, sorted by remaining vouchers (descending) then by total vouchers
                    shipsWithVouchers.sortedWith(
                        compareByDescending<ActiveShipInfo> {
                            val total = it.entryVouchers + it.exitVouchers
                            val remaining = total - it.exitVouchers
                            remaining
                        }.thenByDescending {
                            it.entryVouchers + it.exitVouchers
                        }
                    ).forEach { ship ->
                        QuotaItem(quota = ship)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuotaItem(quota: ActiveShipInfo) {
    val totalVouchers = quota.entryVouchers + quota.exitVouchers

    // Skip rendering if no vouchers
    if (totalVouchers == 0) return

    val remainingVouchers = totalVouchers - quota.exitVouchers
    val isCompleted = remainingVouchers == 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = when {
                    isCompleted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    remainingVouchers > 0 -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                },
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Quota number with potential checkmark for completed quotas
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(60.dp)
        ) {
            // Show checkmark for completed quotas
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "تکمیل شده",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = extractLastDigits(quota.loadingQuotaNumber),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                    isCompleted -> MaterialTheme.colorScheme.primary
                    remainingVouchers > 0 -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }

        // Progress indicator for quotas with vouchers
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = quota.exitVouchers.toFloat() / totalVouchers)
                        .background(
                            color = when {
                                isCompleted -> MaterialTheme.colorScheme.primary
                                remainingVouchers > 0 -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            }
                        )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Voucher counts
            if (remainingVouchers > 0) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "$remainingVouchers",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = "${quota.completedVouchers}/$totalVouchers",
                style = MaterialTheme.typography.bodySmall,
                color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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

private val ActiveShipInfo.completedVouchers get() = exitVouchers

@Composable
private fun ShipCardHeader(
    shipName: String,
    quotaCount: Int,
    totalVouchers: Int,
    completedVouchers: Int,
    remainingVouchers: Int,
    progressPercentage: Float,
    expanded: Boolean
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
            // Ship icon with badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        color = when {
                            progressPercentage > 90 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            progressPercentage > 70 -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            progressPercentage > 50 -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                        }
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBoat,
                    contentDescription = null,
                    tint = when {
                        progressPercentage > 90 -> MaterialTheme.colorScheme.primary
                        progressPercentage > 70 -> MaterialTheme.colorScheme.secondary
                        progressPercentage > 50 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Ship name and counts
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = shipName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        progressPercentage > 90 -> MaterialTheme.colorScheme.onPrimaryContainer
                        progressPercentage > 70 -> MaterialTheme.colorScheme.onSecondaryContainer
                        progressPercentage > 50 -> MaterialTheme.colorScheme.onTertiaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$quotaCount کوتاژ | $completedVouchers از $totalVouchers حواله",
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        progressPercentage > 90 -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        progressPercentage > 70 -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        progressPercentage > 50 -> MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    }
                )
            }

            // Remaining vouchers chip
            if (remainingVouchers > 0) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
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
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Expand/collapse icon
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = if (expanded) "بستن" else "باز کردن",
                tint = when {
                    progressPercentage > 90 -> MaterialTheme.colorScheme.onPrimaryContainer
                    progressPercentage > 70 -> MaterialTheme.colorScheme.onSecondaryContainer
                    progressPercentage > 50 -> MaterialTheme.colorScheme.onTertiaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { rotationZ = rotationState }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = { progressPercentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = when {
                progressPercentage > 90 -> MaterialTheme.colorScheme.primary
                progressPercentage > 70 -> MaterialTheme.colorScheme.secondary
                progressPercentage > 50 -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
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
