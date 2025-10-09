package com.atk.atk_cargo

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInBack
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.atk.atk_cargo.api.adjustColorForTheme
import com.atk.atk_cargo.api.cardColors
import com.atk.atk_cargo.api.validateServerSession
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SelectInfoScreenContent(navController: NavController, viewModel: CargoViewModel) {
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
            Log.e("SelectInfoScreen", "خطا در بررسی وضعیت ورود: ${e.message}")
            navController.navigate("home") {
                popUpTo(0) { inclusive = true }
            }
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

    // دریافت کشتی‌های انتخاب شده از ViewModel
    val selectedShipNames by viewModel.selectedShipNames.collectAsState(initial = emptySet())

    val filteredShips = activeShips.filter { selectedShipNames.contains(it.shipName) }
    val groupedShips = filteredShips.groupBy { it.shipName }

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
                        showUpdateMessage("اطلاعات با موفقیت بروزرسانی شد", MessageType.SUCCESS)
                        viewModel.updateInfoValues()
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

    // LaunchedEffect برای باز کردن خودکار دیالوگ انتخاب کشتی
    LaunchedEffect(selectedShipNames, activeShips) {
        // اگر کشتی‌های فعال بارگذاری شده و هیچ کشتی انتخاب نشده باشد
        if (activeShips.isNotEmpty() && selectedShipNames.isEmpty()) {
            delay(500) // تاخیر کوتاه برای اطمینان از بارگذاری کامل UI
            showShipSelectionDialog = true
        }
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
                    onClickCount = { showActiveQuotasDialog = true },
                    onSelectShips = { showShipSelectionDialog = true },
                    selectedShipsCount = selectedShipNames.size
                )

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
                        onEnter = { ship, enteredQuota ->
                            handleQuotaEntry(ship, enteredQuota)
                        },
                        shipColorMap = shipColorMap.value
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

            // New ActiveQuotasDialog integration
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
fun AnimatedHeader(
    shipCount: Int,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onClickCount: () -> Unit,
    onSelectShips: () -> Unit = {},
    selectedShipsCount: Int = 0
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = if (AnimationManager.areAnimationsEnabled()) fadeIn() + slideInVertically() else fadeIn(),
        exit = if (AnimationManager.areAnimationsEnabled()) fadeOut() + slideOutVertically() else fadeOut()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // بخش سمت چپ - عنوان و اطلاعات
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "بارگیری‌های فعال",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBoat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "$shipCount کوتاژ فعال",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (selectedShipsCount > 0) {
                        Spacer(modifier = Modifier.width(12.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "$selectedShipsCount انتخاب شده",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // بخش سمت راست - دکمه‌ها
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Badge تعداد کوتاژها (قابل کلیک)
                Surface(
                    onClick = onClickCount,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = shipCount.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // دکمه انتخاب کشتی
                Surface(
                    onClick = onSelectShips,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBoat,
                            contentDescription = "انتخاب کشتی‌ها",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // دکمه بروزرسانی
                Surface(
                    onClick = { onRefresh() },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "بروزرسانی",
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
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
        animationSpec = if (AnimationManager.areAnimationsEnabled()) spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow
        ) else tween(0), label = ""
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
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.75f)
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header Section
                DialogHeader(ship = ship, quotaCount = matchingQuotas.size, onDismiss = onDismiss)

                // Quotas List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
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
    Column(
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "بستن",
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "انتخاب کوتاژ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(36.dp))
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DirectionsBoat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = ship.shipName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.size(20.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$quotaCount کوتاژ مشابه یافت شد",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (quotaCount > 1) {
                Spacer(modifier = Modifier.width(8.dp))

                // راهنمای رنگ‌ها به صورت خلاصه در یک خط
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(10.dp)
                        )

                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.secondary, CircleShape)
                        )

                        Text(
                            text = "موارد متفاوت",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
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
            .alpha(if (isActive) 1f else 0.7f),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !isActive -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                differentFields.isNotEmpty() -> MaterialTheme.colorScheme.surface
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        border = when {
            !isActive -> BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            )
            differentFields.isNotEmpty() -> BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
            else -> null
        },
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            QuotaHeader(
                quota = quota,
                isActive = isActive,
                hasDifferences = differentFields.isNotEmpty()
            )

            if (differentFields.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${differentFields.size} فیلد متفاوت",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp
                    )
                }
            }

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
            .padding(6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "این کوتاژ در حال حاضر قابل انتخاب نیست",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun QuotaDetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    isDifferent: Boolean,
    isDisabled: Boolean,
    fieldType: String
) {
    // انتخاب رنگ برای هر نوع فیلد متفاوت
    val differenceColor = when (fieldType) {
        "shippingCompany" -> MaterialTheme.colorScheme.primary
        "warehouse" -> MaterialTheme.colorScheme.tertiary
        "cargoType" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(if (isDifferent) 6.dp else 0.dp))
            .background(
                if (isDifferent) differenceColor.copy(alpha = 0.08f) else Color.Transparent
            )
            .padding(if (isDifferent) 6.dp else 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = when {
                isDisabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                isDifferent -> differenceColor
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            },
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                isDisabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                isDifferent -> differenceColor
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            },
            fontWeight = if (isDifferent) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.width(70.dp),
            fontSize = 12.sp
        )

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (isDifferent) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isDisabled) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    } else {
                        differenceColor.copy(alpha = 0.12f)
                    },
                    border = BorderStroke(
                        width = 0.5.dp,
                        color = differenceColor.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(differenceColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (isDisabled) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            } else {
                                differenceColor
                            },
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDisabled) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontSize = 13.sp
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
            // شماره کوتاژ
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = when {
                    !isActive -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                    hasDifferences -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    else -> Color.Transparent
                },
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = quota.quotaNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        !isActive -> MaterialTheme.colorScheme.error
                        hasDifferences -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // نمایش برچسب وضعیت
            when {
                !isActive -> {
                    StatusBadge(
                        text = "غیرفعال",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                hasDifferences -> {
                    StatusBadge(
                        text = "متفاوت",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (isActive) {
            Surface(
                shape = CircleShape,
                color = if (hasDifferences) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "انتخاب",
                        tint = if (hasDifferences) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
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
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 9.sp
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
            isDisabled = !isActive,
            fieldType = "shippingCompany"
        )
        QuotaDetailItem(
            icon = Icons.Default.Warehouse,
            label = "انبار",
            value = quota.warehouse,
            isDifferent = "warehouse" in differentFields,
            isDisabled = !isActive,
            fieldType = "warehouse"
        )
        QuotaDetailItem(
            icon = Icons.Default.Category,
            label = "نوع کالا",
            value = quota.cargoType,
            isDifferent = "cargoType" in differentFields,
            isDisabled = !isActive,
            fieldType = "cargoType"
        )
    }
}

@Composable
private fun GroupedShipList(
    groupedShips: Map<String, List<ActiveShipInfo>>,
    onEnter: (ActiveShipInfo, String) -> Unit,
    shipColorMap: Map<String, Color>
) {
    // لود داده‌های لحظه‌ای برای هر کشتی
    val coroutineScope = rememberCoroutineScope()
    var realTimeDataList by remember { mutableStateOf<List<RealTimeLoadingData>>(emptyList()) }

    // برای به‌روزرسانی داده‌های لحظه‌ای
    var updateCounter by remember { mutableIntStateOf(0) }

    LaunchedEffect(updateCounter) {
        coroutineScope.launch {
            try {
                val response = RetrofitClient.apiService.getRealTimeLoadingData()
                if (response.isSuccessful) {
                    val responseData = response.body()
                    if (responseData != null) {
                        realTimeDataList = responseData.data
                    }
                }
            } catch (_: Exception) {
                // خطایی رخ داده، اما ادامه می‌دهیم با داده‌های ActiveShipInfo
            } finally {
            }
        }
    }

    // برای به‌روزرسانی خودکار داده‌ها
    LaunchedEffect(Unit) {
        while (true) {
            delay(30000)
            updateCounter++
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        groupedShips.forEach { (shipName, ships) ->
            item {
                // یافتن داده‌های لحظه‌ای مربوط به این کشتی
                val shipRealTimeData = realTimeDataList.filter { it.shipName == shipName }

                if (shipRealTimeData.isNotEmpty()) {
                    // استفاده از داده‌های لحظه‌ای
                    ShipGroupWithRealTimeData(
                        shipName = shipName,
                        realTimeData = shipRealTimeData,
                        ships = ships, // برای ارسال به دیالوگ
                        onEnter = onEnter,
                        color = shipColorMap[shipName] ?: MaterialTheme.colorScheme.primary
                    )
                } else {
                    // استفاده از داده‌های معمولی
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
}

@Composable
private fun ShipGroupWithRealTimeData(
    shipName: String,
    realTimeData: List<RealTimeLoadingData>,
    ships: List<ActiveShipInfo>,
    onEnter: (ActiveShipInfo, String) -> Unit,
    color: Color
) {
    var showDialog by remember { mutableStateOf(false) }

    // محاسبه آمار از داده‌های لحظه‌ای
    val totalVouchers = realTimeData.sumOf { it.entryVouchers + it.exitVouchers }
    val completedVouchers = realTimeData.sumOf { it.exitVouchers }
    val remainingVouchers = totalVouchers - completedVouchers

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

                    // نمایش آمار با فرمت مشابه MinimalQuotasHeader
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "کل: $totalVouchers | خروج: $completedVouchers | مانده: $remainingVouchers",
                            style = MaterialTheme.typography.bodyMedium,
                            color = color.copy(alpha = 0.7f)
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "اطلاعات بیشتر",
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
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var realTimeData by remember { mutableStateOf<List<RealTimeLoadingData>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val convertedShips = remember(realTimeData) {
        realTimeData.map { data ->
            ActiveShipInfo(
                shipName = data.shipName,
                loadingWarehouse = data.loadingWarehouse,
                cargoType = "",  // این فیلد در RealTimeLoadingData نیست
                shippingCompany = data.shippingCompany,
                loadingQuotaNumber = data.loadingQuotaNumber,
                entryVouchers = data.entryVouchers,
                exitVouchers = data.exitVouchers
            )
        }
    }

    // دریافت داده‌ها از API
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            val response = RetrofitClient.apiService.getRealTimeLoadingData()
            if (response.isSuccessful) {
                val responseData = response.body()
                if (responseData != null) {
                    realTimeData = responseData.data
                } else {
                    errorMessage = "داده‌های دریافتی خالی است"
                }
            } else {
                errorMessage = "خطا در دریافت اطلاعات: ${response.code()}"
            }
        } catch (e: Exception) {
            errorMessage = "خطا در ارتباط با سرور: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // فیلتر کشتی‌ها برای نمایش فقط کشتی‌هایی که حداقل یک ورود یا خروج دارند
    val filteredShips = convertedShips.filter { it.entryVouchers + it.exitVouchers > 0 }

    val groupedShips = filteredShips.groupBy { it.shipName }
    val totalQuotas = filteredShips.size
    val totalVouchers = filteredShips.sumOf { it.entryVouchers + it.exitVouchers }
    val completedVouchers = filteredShips.sumOf { it.exitVouchers }

    // فیلتر وضعیت - پیش‌فرض "در حال انجام"
    var filterState by remember { mutableStateOf(FilterState.PENDING) }

    // انتخاب حالت نمایش - پیش فرض نمایش لیستی
    var viewMode by remember { mutableStateOf(ViewMode.FLAT) }

    // متغیر برای نگهداری کشتی باز شده
    var expandedShipName by remember { mutableStateOf<String?>(null) }

    // انیمیشن ورود دیالوگ
    var dialogVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        dialogVisible = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        val dialogElevation by animateDpAsState(
            targetValue = if (dialogVisible) 8.dp else 0.dp,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "dialog_elevation"
        )

        val dialogScale by animateFloatAsState(
            targetValue = if (dialogVisible) 1f else 0.9f,
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            ),
            label = "dialog_scale"
        )

        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .graphicsLayer {
                    scaleX = dialogScale
                    scaleY = dialogScale
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = dialogElevation)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // سربرگ با دکمه‌ی بروزرسانی
                MinimalQuotasHeader(
                    totalQuotas = totalQuotas,
                    totalVouchers = totalVouchers,
                    completedVouchers = completedVouchers,
                    onDismiss = onDismiss,
                    viewMode = viewMode,
                    onViewModeChange = { viewMode = it },
                    isLoading = isLoading,
                    onRefresh = {
                        coroutineScope.launch {
                            try {
                                isLoading = true
                                val response = RetrofitClient.apiService.getRealTimeLoadingData()
                                if (response.isSuccessful) {
                                    val responseData = response.body()
                                    if (responseData != null) {
                                        realTimeData = responseData.data
                                        errorMessage = null
                                    } else {
                                        errorMessage = "داده‌های دریافتی خالی است"
                                    }
                                } else {
                                    errorMessage = "خطا در دریافت اطلاعات: ${response.code()}"
                                }
                            } catch (e: Exception) {
                                errorMessage = "خطا در ارتباط با سرور: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                )

                // نمایش خطا اگر وجود داشته باشد
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    errorMessage?.let {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // نوار فیلتر
                FilterBar(
                    filterState = filterState,
                    onFilterStateChange = { filterState = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // نمایش لودینگ
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "در حال دریافت اطلاعات...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // محتوای اصلی دیالوگ با حالت‌های مختلف نمایش
                    if (viewMode == ViewMode.GROUPED) {
                        // حالت گروه‌بندی شده بر اساس کشتی
                        GroupedShipsContent(
                            groupedShips = groupedShips,
                            searchQuery = "",
                            filterState = filterState,
                            expandedShipName = expandedShipName,
                            onExpandShip = { shipName ->
                                expandedShipName = if (expandedShipName == shipName) null else shipName
                            }
                        )
                    } else {
                        // حالت نمایش همه کوتاژها به صورت لیست
                        FlatQuotasContent(
                            activeShips = filteredShips,
                            searchQuery = "",
                            filterState = filterState
                        )
                    }
                }
            }
        }
    }
}

enum class FilterState {
    ALL, PENDING, COMPLETED
}

enum class ViewMode {
    GROUPED, FLAT
}

@Composable
private fun MinimalQuotasHeader(
    totalQuotas: Int,
    totalVouchers: Int,
    completedVouchers: Int,
    onDismiss: () -> Unit,
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // عنوان و آمار
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "کوتاژهای فعال",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
            ) {
                Text(
                    text = "$totalQuotas کوتاژ",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        // دکمه‌های تغییر حالت نمایش و بستن
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // دکمه‌های تغییر حالت نمایش
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // دکمه نمایش گروه‌بندی شده
                IconButton(
                    onClick = { onViewModeChange(ViewMode.GROUPED) },
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (viewMode == ViewMode.GROUPED)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                Color.Transparent
                        )
                        .size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = "نمایش گروه‌بندی شده",
                        tint = if (viewMode == ViewMode.GROUPED)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // دکمه نمایش لیستی
                IconButton(
                    onClick = { onViewModeChange(ViewMode.FLAT) },
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (viewMode == ViewMode.FLAT)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                Color.Transparent
                        )
                        .size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ViewList,
                        contentDescription = "نمایش لیستی",
                        tint = if (viewMode == ViewMode.FLAT)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // دکمه بروزرسانی
            if (!isLoading) {
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "بروزرسانی",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // دکمه بستن
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "بستن",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }

    }

    // کارت آمار با طراحی جدید و کوچکتر
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            // نوار پیشرفت با درصد
            val progressPercentage = if (totalVouchers > 0) {
                (completedVouchers.toFloat() / totalVouchers) * 100f
            } else 0f

            val progressColor = when {
                progressPercentage >= 90f -> MaterialTheme.colorScheme.primary
                progressPercentage >= 60f -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.error
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = progressPercentage / 100f)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    progressColor.copy(alpha = 0.7f),
                                    progressColor
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // آمار در یک ردیف
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // کارت آمار کل حواله‌ها
                StatItem(
                    value = totalVouchers,
                    label = "کل حواله‌ها",
                    color = MaterialTheme.colorScheme.primary
                )

                VerticalDivider(
                    modifier = Modifier.height(24.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                // کارت آمار حواله‌های خروجی
                StatItem(
                    value = completedVouchers,
                    label = "خروجی",
                    color = MaterialTheme.colorScheme.tertiary
                )

                VerticalDivider(
                    modifier = Modifier.height(24.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                // کارت آمار حواله‌های باقیمانده
                StatItem(
                    value = totalVouchers - completedVouchers,
                    label = "باقیمانده",
                    color = MaterialTheme.colorScheme.error
                )

                if (progressPercentage > 0) {
                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )

                    Text(
                        text = "${progressPercentage.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    value: Int,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
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

@Composable
private fun FilterBar(
    filterState: FilterState,
    onFilterStateChange: (FilterState) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = filterState == FilterState.ALL,
            onClick = { onFilterStateChange(FilterState.ALL) },
            label = "همه",
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        FilterChip(
            selected = filterState == FilterState.PENDING,
            onClick = { onFilterStateChange(FilterState.PENDING) },
            label = "در حال انجام",
            containerColor = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )

        FilterChip(
            selected = filterState == FilterState.COMPLETED,
            onClick = { onFilterStateChange(FilterState.COMPLETED) },
            label = "تکمیل شده",
            containerColor = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )
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
private fun GroupedShipsContent(
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
            items(filteredShips.sortedWith(
                compareByDescending<Map.Entry<String, List<ActiveShipInfo>>> { (_, ships) ->
                    val total = ships.sumOf { it.entryVouchers + it.exitVouchers }
                    val completed = ships.sumOf { it.exitVouchers }
                    total - completed  // حواله‌های باقیمانده
                }.thenByDescending { (_, ships) ->
                    ships.sumOf { it.entryVouchers + it.exitVouchers }  // کل حواله‌ها
                }
            ).toList()) { (shipName, ships) ->
                ImprovedShipCard(
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
private fun ImprovedShipCard(
    shipName: String,
    ships: List<ActiveShipInfo>,
    expanded: Boolean,
    onExpandChange: () -> Unit,
    searchQuery: String
) {
    val totalVouchers = ships.sumOf { it.entryVouchers + it.exitVouchers }
    val completedVouchers = ships.sumOf { it.exitVouchers }
    val remainingVouchers = totalVouchers - completedVouchers
    val progressPercentage = if (totalVouchers > 0) {
        (completedVouchers.toFloat() / totalVouchers) * 100f
    } else 0f

    // گروه‌بندی بر اساس انبار
    val warehouseGroups = ships.groupBy { it.loadingWarehouse }

    // حفظ وضعیت باز/بسته بودن هر انبار
    var expandedWarehouse by remember { mutableStateOf<String?>(null) }

    val cardColor = when {
        progressPercentage >= 100f -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        progressPercentage >= 75f -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
        progressPercentage >= 50f -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
    }

    val borderColor = when {
        progressPercentage >= 100f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        progressPercentage >= 75f -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
        progressPercentage >= 50f -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
    }

    val onBackgroundColor = when {
        progressPercentage >= 100f -> MaterialTheme.colorScheme.onPrimaryContainer
        progressPercentage >= 75f -> MaterialTheme.colorScheme.onSecondaryContainer
        progressPercentage >= 50f -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onErrorContainer
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
            ImprovedShipHeader(
                shipName = shipName,
                quotaCount = ships.count { it.entryVouchers + it.exitVouchers > 0 },
                totalVouchers = totalVouchers,
                completedVouchers = completedVouchers,
                remainingVouchers = remainingVouchers,
                progressPercentage = progressPercentage,
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
                                ImprovedWarehouseSection(
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
private fun ImprovedShipHeader(
    shipName: String,
    quotaCount: Int,
    totalVouchers: Int,
    completedVouchers: Int,
    remainingVouchers: Int,
    progressPercentage: Float,
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
                Text(
                    text = shipName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = onBackgroundColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (quotaCount > 0) {
                        "$quotaCount کوتاژ | $completedVouchers از $totalVouchers حواله"
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
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "تکمیل شده",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
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
private fun FlatQuotasContent(
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
            items(filteredQuotas.sortedWith(
                compareByDescending<ActiveShipInfo> { ship ->
                    val total = ship.entryVouchers + ship.exitVouchers
                    val remaining = total - ship.exitVouchers
                    remaining  // حواله‌های باقیمانده
                }.thenBy { it.shipName }
                    .thenBy { it.loadingWarehouse }
            )) { quota ->
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
        isCompleted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        remainingVouchers > 0 -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
    }

    val borderColor = when {
        isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        remainingVouchers > 0 -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
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
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else if (remainingVouchers > 0)
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBoat,
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

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        // نام کشتی با هایلایت متن جستجو شده
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

                        // شماره کوتاژ با هایلایت متن جستجو شده
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = "تکمیل شده",
                                    tint = MaterialTheme.colorScheme.primary,
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
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "تکمیل شده",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
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
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
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
                            MaterialTheme.colorScheme.primary
                        else if (remainingVouchers > 0)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
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
            }
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

@Composable
private fun ShipSelectionDialog(
    ships: List<ActiveShipInfo>,
    selectedShipNames: Set<String>,
    onSelectShip: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val groupedShips = ships.groupBy { it.shipName }
    val selectedShips = remember { mutableStateOf(selectedShipNames) }
    val searchQuery = remember { mutableStateOf("") }
    val initialSortedShipEntries = remember(groupedShips, searchQuery.value) {
        val filtered = if (searchQuery.value.isEmpty()) {
            groupedShips.entries
        } else {
            groupedShips.entries.filter { (shipName, _) ->
                shipName.contains(searchQuery.value, ignoreCase = true)
            }
        }

        val alphabeticallySorted = filtered.sortedBy { (shipName, _) ->
            shipName
        }

        alphabeticallySorted.sortedWith(
            compareByDescending { (shipName, _) ->
                selectedShipNames.contains(shipName)
            }
        )
    }
    val shipColors = remember {
        initialSortedShipEntries.associate { (shipName, _) ->
            val index = initialSortedShipEntries.indexOfFirst { it.key == shipName }
            val colorIndex = index % cardColors.size
            shipName to cardColors[colorIndex]
        }
    }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.ship))
    val lottieAnimatable = rememberLottieAnimatable()

    LaunchedEffect(composition) {
        lottieAnimatable.animate(
            composition = composition,
            iterations = LottieConstants.IterateForever,
        )
    }

    val dialogEnterTransition = remember {
        expandIn(
            expandFrom = Alignment.Center,
            animationSpec = tween(300, easing = EaseOutBack)
        ) + fadeIn(animationSpec = tween(300))
    }
    val dialogExitTransition = remember {
        shrinkOut(
            shrinkTowards = Alignment.Center,
            animationSpec = tween(300, easing = EaseInBack)
        ) + fadeOut(animationSpec = tween(300))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            AnimatedVisibility(
                visible = true,
                enter = dialogEnterTransition,
                exit = dialogExitTransition
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), 
                            CircleShape
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LottieAnimation(
                        composition = composition,
                        progress = { lottieAnimatable.progress },
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "انتخاب کشتی‌ها",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "کشتی‌های مورد نظر خود را برای نمایش انتخاب کنید",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // Search field
                    OutlinedTextField(
                        value = searchQuery.value,
                        onValueChange = { searchQuery.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "جستجو",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = if (searchQuery.value.isNotEmpty()) {
                            {
                                IconButton(
                                    onClick = { searchQuery.value = "" }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "پاک کردن",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else null,
                        placeholder = {
                            Text("جستجوی نام کشتی...")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            keyboardType = KeyboardType.Ascii,
                            imeAction = ImeAction.Search
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    // Statistics cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBoat,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${initialSortedShipEntries.size} فعال",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${selectedShips.value.size} انتخاب",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // لیست کشتی‌ها
                val primaryColor = MaterialTheme.colorScheme.primary
                val surfaceColor = MaterialTheme.colorScheme.surface
                val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant
                val onSurfaceColor = MaterialTheme.colorScheme.onSurface

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(initialSortedShipEntries.toList()) { (shipName, shipList) ->
                        val isSelected = selectedShips.value.contains(shipName)
                        val shipColor = shipColors[shipName] ?: primaryColor

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedShips.value = if (isSelected) {
                                        selectedShips.value - shipName
                                    } else {
                                        selectedShips.value + shipName
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) shipColor.copy(alpha = 0.15f) else surfaceColor,
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) shipColor else outlineVariantColor
                            ),
                            tonalElevation = if (isSelected) 3.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // آیکون کشتی با رنگ اختصاصی
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    shipColor.copy(alpha = 0.5f),
                                                    shipColor.copy(alpha = 0.2f)
                                                )
                                            )
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsBoat,
                                        contentDescription = null,
                                        tint = shipColor,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // اطلاعات کشتی
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = shipName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) shipColor else onSurfaceColor
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // تعداد کوتاژ - فقط این اطلاعات نمایش داده می‌شود
                                    Surface(
                                        color = shipColor.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "${shipList.size} کوتاژ",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = shipColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                // چک‌باکس انتخاب
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = {
                                        selectedShips.value = if (it) {
                                            selectedShips.value + shipName
                                        } else {
                                            selectedShips.value - shipName
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = shipColor,
                                        uncheckedColor = outlineVariantColor
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            text = "انصراف",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    // Confirm Button
                    Button(
                        onClick = {
                            onSelectShip(selectedShips.value)
                            onDismiss()
                        },
                        enabled = selectedShips.value.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "تایید",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun ImprovedWarehouseSection(
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

                // آمار حواله‌ها
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

                    // نمایش آمار کلی
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
                            ImprovedQuotaItem(
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
private fun ImprovedQuotaItem(
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

            // نمایش آمار حواله‌ها
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

@Composable
private fun FilterChip(
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

@Composable
private fun ShipGroup(
    shipName: String,
    ships: List<ActiveShipInfo>,
    onEnter: (ActiveShipInfo, String) -> Unit,
    color: Color
) {
    var showDialog by remember { mutableStateOf(false) }

    // محاسبه آمار
    val totalVouchers = ships.sumOf { it.entryVouchers + it.exitVouchers }
    val completedVouchers = ships.sumOf { it.exitVouchers }
    val remainingVouchers = totalVouchers - completedVouchers

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

                    // نمایش آمار
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "کل: $totalVouchers | خروج: $completedVouchers | مانده: $remainingVouchers",
                            style = MaterialTheme.typography.bodyMedium,
                            color = color.copy(alpha = 0.7f)
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "اطلاعات بیشتر",
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