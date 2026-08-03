package com.atk.atk_cargo.feature.cargo_registration.presentation

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.navigation.compose.rememberNavController
import com.atk.atk_cargo.data.model.CargoInfo
import com.atk.atk_cargo.data.model.InitialInfo
import com.atk.atk_cargo.data.model.MessageType
import com.atk.atk_cargo.data.model.ShipInfo
import com.atk.atk_cargo.data.model.WarningStatus
import com.atk.atk_cargo.feature.cargo_entry.presentation.SelectInfoScreenContent
import com.atk.atk_cargo.feature.cargo_entry.presentation.StatusSnackbar
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.CargoInfoDetailsDialog
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.DialogPassword
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.DuplicateConfirmationDialog
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.DuplicateTrackingNumbersDialog
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.ErrorHandlingCargoInfoRow
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.ExitStatusDialog
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.FormSection
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.MessageDialog
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.NetWeightDialog
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.QuotaEntryDialog
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.QuotaWarningDialog
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.ShipInfoSection
import com.atk.atk_cargo.feature.startup.domain.AnimationManager
import com.atk.atk_cargo.ui.theme.Amber700
import com.atk.atk_cargo.ui.theme.Gray300
import com.atk.atk_cargo.ui.theme.Gray500
import com.atk.atk_cargo.ui.theme.Gray600
import com.atk.atk_cargo.ui.theme.Green600
import com.atk.atk_cargo.ui.viewmodel.CargoViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private val RegisterAccent = Color(0xFF0D9488)
private val RegisterAccentBg = Color(0xFFDCEFEA)
private val RegisterAccentBorder = Color(0xFFB9DED7)
private val RegisterCardBorder = Color(0xFFE4E6E9)
private val RegisterMutedBg = Color(0xFFF3F4F5)
private val RegisterMutedText = Color(0xFF8A8F98)
private val RegisterTitleColor = Color(0xFF1F2937)

suspend fun handleQuotaEntry(
    quotaCode: String,
    currentInitialInfo: InitialInfo?,
    viewModel: CargoViewModel,
    snackbarHostState: SnackbarHostState,
    onQuotaChanged: () -> Unit = {}
) {
    if (currentInitialInfo == null) {
        snackbarHostState.showSnackbar("اطلاعات اولیه یافت نشد")
        return
    }

    try {
        val response = viewModel.checkQuotaExistenceCargo(quotaCode, currentInitialInfo.shipName)
        
        if (response.exists && response.matchingQuotas.isNotEmpty()) {
            val selectedQuota = response.matchingQuotas.first()
            
            if (selectedQuota.shipName == currentInitialInfo.shipName) {
                val newInitialInfo = InitialInfo(
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
                
                viewModel.setInitialInfo(newInitialInfo)
                viewModel.refreshCargoInfo()
                onQuotaChanged()
                snackbarHostState.showSnackbar("کوتاژ با موفقیت تغییر یافت به: ${selectedQuota.quotaNumber}")
            } else {
                snackbarHostState.showSnackbar("خطا: کوتاژ $quotaCode متعلق به کشتی ${selectedQuota.shipName} است، نه کشتی ${currentInitialInfo.shipName}!")
            }
        } else {
            snackbarHostState.showSnackbar("کوتاژ $quotaCode برای کشتی ${currentInitialInfo.shipName} یافت نشد")
        }
    } catch (e: Exception) {
        snackbarHostState.showSnackbar("خطا در بررسی کوتاژ: ${e.message}")
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope", "DefaultLocale")
@Composable
fun RegisterCargoScreen(
    initialInfo: InitialInfo?,
    cargoInfoList: List<CargoInfo>,
    resultMessage: String,
    showAnimatedMessage: Boolean,
    messageType: MessageType,
    viewModel: CargoViewModel,
    onChangeSelectionClick: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val barcodeLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { barcode ->
            val cleanedBarcode = barcode.replace(Regex("[^0-9]"), "")
            Log.d("ATK-Log", "Barcode received: $barcode -> Cleaned: $cleanedBarcode")
            viewModel.updateScaleReceiptNumber(cleanedBarcode)
        }
    }
    var trackingNumber by remember { mutableStateOf("") }
    var numberOfPeople by remember { mutableStateOf("") }
    var netWeight by remember { mutableStateOf("") }
    var shortageWeight by remember { mutableStateOf("") }
    var excessWeight by remember { mutableStateOf("") }
    val selectedCargoInfo = remember { mutableStateOf<CargoInfo?>(null) }
    val showDetailDialog = remember { mutableStateOf(false) }
    var isInfoVisible by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    val confirmationMessage by remember { mutableStateOf("") }
    var cargoInfoToUpdate by remember { mutableStateOf<CargoInfo?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isFormExpanded by remember { mutableStateOf(true) }
    val clearInputFields by viewModel.clearInputFields.collectAsState()
    val showNetWeightDialog by viewModel.showNetWeightDialog.collectAsState()
    val scaleReceiptNumber by viewModel.scaleReceiptNumber.collectAsState()
    val focusManager = LocalFocusManager.current
    var showExitStatusDialog by remember { mutableStateOf(false) }
    var showStatisticsDialog by remember { mutableStateOf(false) }
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    var showQuotaWarning by remember { mutableStateOf<WarningStatus?>(null) }
    val loadableTonnage by viewModel.loadableTonnage.collectAsState()
    val loadableTrucks18Wheeler by viewModel.loadableTrucks18Wheeler.collectAsState()
    val loadableTrucks10Wheeler by viewModel.loadableTrucks10Wheeler.collectAsState()
    var updateType by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val showDuplicateConfirmationDialog by viewModel.showDuplicateConfirmationDialog.collectAsState()
    val duplicateWarningMessage by viewModel.duplicateWarningMessage.collectAsState()
    val showDuplicateDialog by viewModel.showDuplicateDialog.collectAsState()
    val duplicateTrackingNumbers by viewModel.duplicateTrackingNumbers.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    var showQuotaEntryDialog by remember { mutableStateOf(false) }

    fun clearInputFields() {
        trackingNumber = ""
        netWeight = ""
        numberOfPeople = ""
        shortageWeight = ""
        excessWeight = ""
    }

    LaunchedEffect(clearInputFields) {
        if (clearInputFields) {
            clearInputFields()
            viewModel.resetClearInputFields()
        }
    }

    val filteredCargoInfoList by remember(cargoInfoList, searchQuery) {
        derivedStateOf {
            cargoInfoList.filter { cargoInfo ->
                val matches = cargoInfo.trackingNumber.contains(searchQuery, ignoreCase = true)
                if (searchQuery.isNotEmpty()) {
                    Log.d("RegisterCargoActivity_Log", "Checking ${cargoInfo.trackingNumber} against '$searchQuery': $matches")
                }
                matches
            }
        }
    }

    val (nonExitedCargos, exitedCargos) = filteredCargoInfoList.partition { it.status == "ورود" }

    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessHigh
        ),
        label = "fab_scale"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .navigationBarsPadding(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                Card(
                    modifier = Modifier
                        .size(56.dp)
                        .scale(scale)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showQuotaEntryDialog = true }
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val down = event.changes.firstOrNull()?.pressed == true
                                    isPressed = down
                                }
                            }
                        },
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = RegisterAccent
                    ),
                    border = BorderStroke(4.dp, MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = "تغییر کوتاژ",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                val shipInfo = ShipInfo(
                    shipName = initialInfo?.shipName ?: "",
                    loadingWarehouse = initialInfo?.loadingWarehouse ?: "",
                    cargoType = initialInfo?.cargoType ?: "",
                    shippingCompany = initialInfo?.shippingCompany ?: "",
                    loadingQuotaNumber = initialInfo?.loadingQuotaNumber.toString(),
                    cargoWeight = initialInfo?.cargoWeight.toString(),
                    remainingWeight = initialInfo?.remainingWeight.toString(),
                    totalNetWeight = initialInfo?.totalNetWeight.toString(),
                    averageNetWeight = initialInfo?.averageNetWeight.toString(),
                    totalServices = initialInfo?.totalVoucherCount.toString(),
                    remainingServices = initialInfo?.remainingServices.toString(),
                    tempTonnageStatus = initialInfo?.tempTonnageStatus ?: false,
                    tempTonnageAmount = initialInfo?.tempTonnageAmount,
                    cargoOwner = initialInfo?.cargoOwner ?: ""
                )

                ShipInfoSection(
                    shipInfo = shipInfo,
                    isInfoVisible = isInfoVisible,
                    onToggleVisibility = { isInfoVisible = !isInfoVisible },
                    loadableTonnage = loadableTonnage,
                    loadableTrucks18Wheeler = loadableTrucks18Wheeler,
                    loadableTrucks10Wheeler = loadableTrucks10Wheeler,
                    onChangeSelectionClick = onChangeSelectionClick
                )
                
                Spacer(modifier = Modifier.height(4.dp))

                AnimatedVisibility(
                    visible = isFormExpanded,
                    enter = if (AnimationManager.areAnimationsEnabled()) expandVertically() + fadeIn() else fadeIn(),
                    exit = if (AnimationManager.areAnimationsEnabled()) shrinkVertically() + fadeOut() else fadeOut()
                ) {
                    FormSection(
                        trackingNumber = trackingNumber,
                        onTrackingNumberChange = { trackingNumber = it },
                        scaleReceiptNumber = scaleReceiptNumber,
                        onScanBarcode = {
                            val existingCargoInfo = cargoInfoList.find { it.trackingNumber == trackingNumber }
                            val isCargoConfirmed = existingCargoInfo?.confirm == "تائید شده"
                            val isCargoExited = existingCargoInfo?.status == "خروج"

                            if (isCargoConfirmed && !isCargoExited) {
                                val options = ScanOptions()
                                    .setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
                                    .setPrompt("اسکن قبض باسکول")
                                    .setCameraId(0)
                                    .setBeepEnabled(false)
                                    .setBarcodeImageEnabled(true)
                                    .setOrientationLocked(false)
                                barcodeLauncher.launch(options)
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("امکان اسکن بارکد وجود ندارد.")
                                }
                            }
                        },
                        shortageWeight = shortageWeight,
                        onShortageWeightChange = { shortageWeight = it },
                        excessWeight = excessWeight,
                        onExcessWeightChange = { excessWeight = it },
                        numberOfPeople = numberOfPeople,
                        onNumberOfPeopleChange = { numberOfPeople = it },
                        cargoInfoList = cargoInfoList,
                        isCargoConfirmed = cargoInfoList.find { it.trackingNumber == trackingNumber }?.confirm == "تائید شده",
                        isCargoExited = cargoInfoList.find { it.trackingNumber == trackingNumber }?.status == "خروج",
                        isSubmitting = isSubmitting,
                        onSubmit = {
                            Log.d("ATK-Log", "RegisterCargoScreen: Submit button clicked for tracking: $trackingNumber")
                            coroutineScope.launch {
                                if (trackingNumber.isBlank()) {
                                    snackbarHostState.showSnackbar("لطفاً شماره حواله را وارد کنید.")
                                    return@launch
                                }

                                val isNewCargo = cargoInfoList.none { it.trackingNumber == trackingNumber }
                                if (isNewCargo) {
                                    val numberOfPeopleValue = numberOfPeople.toIntOrNull() ?: 1
                                    if (numberOfPeopleValue < 1) {
                                        snackbarHostState.showSnackbar("تعداد نفرات باید عددی بزرگتر از صفر باشد.")
                                        return@launch
                                    }
                                }

                                val netWeightValue = netWeight.toIntOrNull()
                                if (netWeight.isNotBlank() && (netWeightValue == null || netWeightValue !in 1000..60000)) {
                                    snackbarHostState.showSnackbar("وزن خالص باید بین 1000 تا 60000 کیلوگرم باشد.")
                                    return@launch
                                }

                                viewModel.submitCargoInfo(
                                    trackingNumber,
                                    netWeight,
                                    scaleReceiptNumber,
                                    shortageWeight,
                                    excessWeight,
                                    numberOfPeople
                                )

                                updateType = "cargo_submit"
                                focusManager.clearFocus()
                            }
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-8).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { isFormExpanded = !isFormExpanded },
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 1.dp,
                        border = BorderStroke(1.dp, RegisterCardBorder)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isFormExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isFormExpanded) "بستن فرم" else "باز کردن فرم",
                                tint = RegisterMutedText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // نوار ابزار جستجو و بروزرسانی مدرن صنعتی
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        placeholder = {
                            Text(
                                text = "جستجوی شماره حواله...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = RegisterAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = RegisterAccent,
                            unfocusedBorderColor = Color(0xFFE5E7EA)
                        )
                    )

                    var isRefreshing by remember { mutableStateOf(false) }
                    var rotationState by remember { mutableFloatStateOf(0f) }
                    val rotation = animateFloatAsState(
                        targetValue = rotationState,
                        animationSpec = if (AnimationManager.areAnimationsEnabled()) tween(400) else tween(0),
                        label = "rotation"
                    )

                    Surface(
                        modifier = Modifier
                            .height(52.dp)
                            .clickable(enabled = !isRefreshing) {
                                if (!isRefreshing) {
                                    isRefreshing = true
                                    rotationState += 360f
                                    viewModel.refreshCargoInfo()
                                    coroutineScope.launch {
                                        delay(1200.milliseconds)
                                        isRefreshing = false
                                    }
                                }
                            },
                        shape = RoundedCornerShape(14.dp),
                        color = RegisterAccentBg,
                        border = BorderStroke(1.dp, RegisterAccentBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "بروزرسانی",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = RegisterAccent
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "بروزرسانی",
                                modifier = Modifier
                                    .size(20.dp)
                                    .rotate(rotation.value),
                                tint = RegisterAccent
                            )
                        }
                    }
                }

                var selectedTab by remember { mutableIntStateOf(0) }
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = RegisterMutedBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = 0 },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedTab == 0) Color.White else Color.Transparent,
                            shadowElevation = if (selectedTab == 0) 1.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Assignment,
                                    contentDescription = null,
                                    tint = if (selectedTab == 0) Amber700 else Gray500,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ورود شده",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == 0) Amber700 else Gray500
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selectedTab == 0) Amber700.copy(alpha = 0.1f) else Gray300
                                ) {
                                    Text(
                                        text = "${nonExitedCargos.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTab == 0) Amber700 else Gray600,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = 1 },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedTab == 1) Color.White else Color.Transparent,
                            shadowElevation = if (selectedTab == 1) 1.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = if (selectedTab == 1) Green600 else Gray500,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "خروج شده",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == 1) Green600 else Gray500
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selectedTab == 1) Green600.copy(alpha = 0.1f) else Gray300
                                ) {
                                    Text(
                                        text = "${exitedCargos.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTab == 1) Green600 else Gray600,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                val currentItems = if (selectedTab == 0) {
                    nonExitedCargos.sortedByDescending { it.entryTime }
                } else {
                    exitedCargos.sortedByDescending { "${it.exitDate} ${it.exitTime}" }
                }
                
                val filteredItems = if (searchQuery.isNotEmpty()) {
                    currentItems.filter { it.trackingNumber.contains(searchQuery, ignoreCase = true) }
                } else {
                    currentItems
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, RegisterCardBorder)
                ) {
                    if (filteredItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotEmpty()) "موردی یافت نشد" else "لیست خالی است",
                                style = MaterialTheme.typography.bodyMedium,
                                color = RegisterMutedText
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 450.dp),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            items(filteredItems) { info ->
                                ErrorHandlingCargoInfoRow(
                                    info = info,
                                    onRowClick = { selectedInfo ->
                                        selectedCargoInfo.value = selectedInfo
                                        showDetailDialog.value = true
                                    },
                                    duplicateTrackingNumbers = duplicateTrackingNumbers
                                )
                                if (info != filteredItems.last()) {
                                    HorizontalDivider(
                                        color = RegisterCardBorder,
                                        thickness = 1.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showAnimatedMessage) {
                MessageDialog(
                    message = resultMessage,
                    type = messageType,
                    visible = true,
                    onDismiss = { viewModel.dismissMessage() }
                )
            }

            selectedCargoInfo.value?.let { info ->
                if (showDetailDialog.value) {
                    CargoInfoDetailsDialog(
                        info = info,
                        viewModel = viewModel,
                        snackbarHostState = snackbarHostState,
                        onDismiss = {
                            showDetailDialog.value = false
                        },
                        onUpdateTypeChange = { newUpdateType ->
                            updateType = newUpdateType
                        }
                    )
                }
            }

            if (showConfirmationDialog) {
                DialogPassword(
                    message = confirmationMessage,
                    onConfirm = {
                        cargoInfoToUpdate?.let { cargoInfo ->
                            viewModel.updateCargoInfo(cargoInfo, netWeight)
                            updateType = "cargo_update"
                            showConfirmationDialog = false
                            cargoInfoToUpdate = null
                        }
                    },
                    onDismiss = { showConfirmationDialog = false }
                )
            }

            if (showNetWeightDialog) {
                NetWeightDialog(
                    scaleReceiptNumber = scaleReceiptNumber,
                    onConfirm = { enteredNetWeight ->
                        Log.d("ATK-Log", "NetWeightDialog: Weight confirmed: $enteredNetWeight for tracking: $trackingNumber")
                        viewModel.submitCargoInfo(
                            trackingNumber,
                            enteredNetWeight,
                            scaleReceiptNumber,
                            shortageWeight,
                            excessWeight,
                            numberOfPeople
                        )
                        updateType = "cargo_submit"
                        viewModel.hideNetWeightDialog()
                    },
                    onDismiss = {
                        viewModel.hideNetWeightDialog()
                    }
                )
            }

            if (showExitStatusDialog) {
                ExitStatusDialog(
                    showDialog = true,
                    onDismiss = { showExitStatusDialog = false },
                    exitVouchersCount = exitedCargos.size,
                    totalNetWeight = exitedCargos.sumOf { (it.netWeight.toFloatOrNull() ?: 0f).toDouble() }.toFloat()
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        snackbarMessage?.let { message ->
            StatusSnackbar(
                message = message,
                isVisible = true,
                onDismiss = viewModel::dismissSnackbar,
                modifier = Modifier.zIndex(Float.MAX_VALUE)
            )
        }
    }

    if (showStatisticsDialog) {
        Dialog(
            onDismissRequest = { showStatisticsDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                SelectInfoScreenContent(
                    navController = rememberNavController(),
                    viewModel = viewModel
                )
            }
        }
    }

    if (showQuotaWarning != null) {
        QuotaWarningDialog(
            warning = showQuotaWarning!!,
            onDismiss = {
                showQuotaWarning = null
            },
            viewModel = viewModel
        )
    }

    if (showDuplicateConfirmationDialog) {
        DuplicateConfirmationDialog(
            message = duplicateWarningMessage,
            onConfirm = {
                viewModel.confirmDuplicateCargoRegistration()
            },
            onCancel = {
                viewModel.cancelDuplicateCargoRegistration()
            },
            onDismiss = {
                viewModel.dismissDuplicateConfirmationDialog()
            }
        )
    }

    if (showDuplicateDialog) {
        DuplicateTrackingNumbersDialog(
            duplicateNumbers = duplicateTrackingNumbers,
            onDismiss = {
                viewModel.dismissDuplicateDialog()
            },
            onSearchTrackingNumber = { trackingNumber ->
                searchQuery = trackingNumber
                coroutineScope.launch {
                    listState.animateScrollToItem(0)
                }
            }
        )
    }
    
    if (showQuotaEntryDialog) {
        QuotaEntryDialog(
            showDialog = true,
            onDismiss = { showQuotaEntryDialog = false },
            onConfirm = { quotaCode ->
                coroutineScope.launch {
                    handleQuotaEntry(quotaCode, initialInfo, viewModel, snackbarHostState) {
                        updateType = "quota_change"
                    }
                }
                showQuotaEntryDialog = false
            },
            shipName = initialInfo?.shipName ?: "",
            currentQuota = initialInfo?.loadingQuotaNumber?.toString() ?: "",
            viewModel = viewModel
        )
    }
    
    LaunchedEffect(initialInfo, loadableTonnage) {
        if (initialInfo != null && loadableTonnage.isNotEmpty()) {
            val excludedUpdateTypes = setOf("cargo_submit", "cargo_update", "cargo_delete")
            if (updateType == null || updateType !in excludedUpdateTypes) {
                if (updateType == null) {
                    updateType = "initial"
                }
            }
        }
    }
}