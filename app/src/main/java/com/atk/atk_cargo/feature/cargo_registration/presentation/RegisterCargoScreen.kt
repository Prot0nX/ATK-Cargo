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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.atk.atk_cargo.data.model.CargoInfo
import com.atk.atk_cargo.data.model.InitialInfo
import com.atk.atk_cargo.data.model.MessageType
import com.atk.atk_cargo.data.model.ShipInfo
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.CargoInfoDetailsDialog
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.DuplicateConfirmationDialog
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.DuplicateTrackingNumbersDialog
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.ErrorHandlingCargoInfoRow
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.FormSection
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.MessageDialog
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.NetWeightDialog
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.QuotaEntryDialog
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.ShipInfoSection
import com.atk.atk_cargo.feature.startup.domain.AnimationManager
import com.atk.atk_cargo.ui.theme.Amber700
import com.atk.atk_cargo.ui.theme.Green600
import com.atk.atk_cargo.ui.viewmodel.CargoViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private const val AUTO_REFRESH_INTERVAL_SECONDS = 60

private class RegisterPalette(
    val accent: Color,
    val accentBg: Color,
    val accentBorder: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val mutedBg: Color,
    val mutedText: Color
)

@Composable
private fun rememberRegisterPalette(): RegisterPalette {
    val isDark = isSystemInDarkTheme()
    val accent = MaterialTheme.colorScheme.primary
    return RegisterPalette(
        accent = accent,
        accentBg = accent.copy(alpha = if (isDark) 0.18f else 0.16f),
        accentBorder = accent.copy(alpha = 0.4f),
        cardBg = MaterialTheme.colorScheme.surface,
        cardBorder = MaterialTheme.colorScheme.outlineVariant,
        mutedBg = MaterialTheme.colorScheme.surfaceVariant,
        mutedText = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("DefaultLocale")
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
    val palette = rememberRegisterPalette()
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
    var shortageWeight by remember { mutableStateOf("") }
    var excessWeight by remember { mutableStateOf("") }
    val selectedCargoInfo = remember { mutableStateOf<CargoInfo?>(null) }
    val showDetailDialog = remember { mutableStateOf(false) }
    var isInfoVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isFormExpanded by remember { mutableStateOf(true) }
    val cargoUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clearInputFields = cargoUiState.clearInputFields
    val showNetWeightDialog = cargoUiState.showNetWeightDialog
    val scaleReceiptNumber = cargoUiState.scaleReceiptNumber
    val focusManager = LocalFocusManager.current
    val loadableTonnage = cargoUiState.loadableTonnage
    val loadableTrucks18Wheeler = cargoUiState.loadableTrucks18Wheeler
    val loadableTrucks10Wheeler = cargoUiState.loadableTrucks10Wheeler
    val listState = rememberLazyListState()
    val showDuplicateConfirmationDialog = cargoUiState.showDuplicateConfirmationDialog
    val duplicateWarningMessage = cargoUiState.duplicateWarningMessage
    val showDuplicateDialog = cargoUiState.showDuplicateDialog
    val duplicateTrackingNumbers = cargoUiState.duplicateTrackingNumbers
    val isSubmitting = cargoUiState.isSubmitting
    var showQuotaEntryDialog by remember { mutableStateOf(false) }

    fun clearInputFields() {
        trackingNumber = ""
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

    // clearInputFields فقط در مسیر موفقیت ست می‌شود، پس اگر یک ثبت با دیالوگ
    // (تأیید تکراری، خطای اعتبارسنجی) لغو شود مقادیر کسری/اضافه و قبض باسکول در
    // فرم می‌مانند و با تایپ شماره حواله‌ی بعدی روی آن حواله اعمال می‌شوند —
    // یعنی کسری یک حواله می‌تواند به حواله‌ای دیگر بچسبد. این مقادیر مخصوص یک
    // شماره حواله‌اند، پس با تغییر آن باید صفر شوند.
    LaunchedEffect(trackingNumber) {
        shortageWeight = ""
        excessWeight = ""
        if (scaleReceiptNumber.isNotEmpty()) {
            // updateScaleReceiptNumber عمداً استفاده نشد: آن تابع بارکد را
            // معتبر می‌شمارد و درخواست شبکه‌ی checkScaleReceiptNumber را صدا
            // می‌زند؛ hideNetWeightDialog فقط state را صفر می‌کند.
            viewModel.hideNetWeightDialog()
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    // قبلاً این فیلتر و partition/sort در بدنه‌ی Composable و بدون remember
    // با هر بازترکیب (هر فریم انیمیشن FAB، هر تغییر isPressed) از نو محاسبه
    // می‌شدند، و پایین‌تر همین فیلتر جستجو یک‌بار دیگر هم روی currentItems
    // تکرار می‌شد. اینجا همه در یک remember جمع شده‌اند.
    val (nonExitedCargos, exitedCargos) = remember(cargoInfoList, searchQuery) {
        val filtered = if (searchQuery.isEmpty()) {
            cargoInfoList
        } else {
            cargoInfoList.filter { it.trackingNumber.contains(searchQuery, ignoreCase = true) }
        }
        filtered.partition { it.status == "ورود" }
    }

    val visibleItems = remember(nonExitedCargos, exitedCargos, selectedTab) {
        if (selectedTab == 0) {
            nonExitedCargos.sortedByDescending { it.entryTime }
        } else {
            exitedCargos.sortedByDescending { "${it.exitDate} ${it.exitTime}" }
        }
    }

    val fabInteractionSource = remember { MutableInteractionSource() }
    val isPressed by fabInteractionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessHigh
        ),
        label = "fab_scale"
    )

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
                        interactionSource = fabInteractionSource,
                        indication = null
                    ) { showQuotaEntryDialog = true },
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = palette.accent
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
                        contentDescription = "تغییر کشتی",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        // قبلاً این یک Column ثابت (بدون اسکرول) بود و فقط لیست حواله‌ها
        // داخلش یک LazyColumn با ارتفاع سقف‌دار (450dp) داشت؛ اگر مجموع هدر
        // (وقتی ShipInfoSection/FormSection باز باشند) + آن سقف از ارتفاع
        // صفحه بیشتر می‌شد، چیزی برای دیدن باقی صفحه اسکرول نمی‌شد. حالا کل
        // صفحه یک LazyColumn واحد است. چون LazyColumn داخل LazyColumn ممکن
        // نیست، ردیف‌های حواله دیگر یک LazyColumn جدا نیستند؛ تعداد آن‌ها برای
        // هر کوتاژ معمولاً چند ده مورد است نه هزاران، پس یک Column معمولی
        // همراه با key() برای هویت پایدار هر ردیف استفاده شده تا هم اسکرول
        // کل صفحه یکپارچه شود و هم ظاهر کارت (Surface با border/rounded
        // corner که کل لیست را دربرمی‌گیرد) دست‌نخورده بماند.
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
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

                                // این دکمه فقط برای ثبت ورود تازه یا کسری/اضافه
                                // بار است؛ خروج (وزن خالص) فقط از مسیر اسکن
                                // بارکد → NetWeightDialog انجام می‌شود، پس
                                // netWeight همیشه خالی است.
                                viewModel.submitCargoInfo(
                                    trackingNumber,
                                    "",
                                    scaleReceiptNumber,
                                    shortageWeight,
                                    excessWeight,
                                    numberOfPeople
                                )

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
                        color = palette.cardBg,
                        shadowElevation = 1.dp,
                        border = BorderStroke(1.dp, palette.cardBorder)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isFormExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isFormExpanded) "بستن فرم" else "باز کردن فرم",
                                tint = palette.mutedText,
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
                                tint = palette.accent,
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
                            focusedContainerColor = palette.cardBg,
                            unfocusedContainerColor = palette.cardBg,
                            focusedBorderColor = palette.accent,
                            unfocusedBorderColor = palette.cardBorder
                        )
                    )

                    var isRefreshing by remember { mutableStateOf(false) }
                    var rotationState by remember { mutableFloatStateOf(0f) }
                    // پیش‌فرض غیرفعال تا مصرف داده/تعداد درخواست به سرور
                    // اضافه نشود؛ فقط با لمس طولانی دکمهٔ «بروزرسانی» روشن
                    // می‌شود، نه به‌صورت پیش‌فرض برای همه.
                    var isAutoRefreshEnabled by remember { mutableStateOf(false) }
                    var secondsUntilNextRefresh by remember { mutableIntStateOf(AUTO_REFRESH_INTERVAL_SECONDS) }
                    val rotation = animateFloatAsState(
                        targetValue = rotationState,
                        animationSpec = if (AnimationManager.areAnimationsEnabled()) tween(400) else tween(0),
                        label = "rotation"
                    )

                    // لمس دستی دکمه: پیام نتیجه فقط اینجا (با پاس‌دادن
                    // onManualRefreshComplete) به شکل Snackbar نمایش داده
                    // می‌شود، نه دیالوگ MessageDialog — چون refreshCargoInfo از
                    // مسیرهای دیگری هم صدا زده می‌شود (ثبت/خروج حواله،
                    // غیرفعال‌شدن خودکار کوتاژ) که نباید این پیام را نشان دهند.
                    fun triggerManualRefresh() {
                        if (!isRefreshing) {
                            isRefreshing = true
                            rotationState += 360f
                            viewModel.refreshCargoInfo { message ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                            coroutineScope.launch {
                                delay(1200.milliseconds)
                                isRefreshing = false
                            }
                        }
                    }

                    // تیک خودکار هر ۶۰ ثانیه: مستقیماً loadCargoInfoList صدا
                    // زده می‌شود (نه refreshCargoInfo)؛ این تیک پیام
                    // «به‌روزرسانی شد» جداگانه نمی‌سازد، بلکه فقط پیام
                    // «بروزرسانی خودکار» خودش را پایین‌تر نشان می‌دهد.
                    fun triggerSilentAutoRefresh() {
                        if (!isRefreshing) {
                            isRefreshing = true
                            rotationState += 360f
                            initialInfo?.let { info ->
                                viewModel.loadCargoInfoList(
                                    quotaNumber = info.loadingQuotaNumber.toString(),
                                    shippingCompany = info.shippingCompany,
                                    warehouse = info.loadingWarehouse,
                                    cargoType = info.cargoType,
                                    onComplete = {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("لیست به‌صورت خودکار بروزرسانی شد")
                                        }
                                    }
                                )
                            }
                            coroutineScope.launch {
                                delay(1200.milliseconds)
                                isRefreshing = false
                            }
                        }
                    }

                    // با ترک صفحه (navigate away)، این LaunchedEffect لغو
                    // می‌شود و polling متوقف می‌گردد؛ حالت isAutoRefreshEnabled
                    // هم چون remember ساده است (نه rememberSaveable) با
                    // برگشت به صفحه دوباره غیرفعال شروع می‌شود.
                    LaunchedEffect(isAutoRefreshEnabled) {
                        if (isAutoRefreshEnabled) {
                            secondsUntilNextRefresh = AUTO_REFRESH_INTERVAL_SECONDS
                            while (true) {
                                delay(1000L.milliseconds)
                                secondsUntilNextRefresh -= 1
                                if (secondsUntilNextRefresh <= 0) {
                                    triggerSilentAutoRefresh()
                                    secondsUntilNextRefresh = AUTO_REFRESH_INTERVAL_SECONDS
                                }
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .height(52.dp)
                            .combinedClickable(
                                enabled = !isRefreshing,
                                onClick = { triggerManualRefresh() },
                                onLongClick = {
                                    isAutoRefreshEnabled = !isAutoRefreshEnabled
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (isAutoRefreshEnabled) {
                                                "بروزرسانی خودکار هر ۶۰ ثانیه فعال شد"
                                            } else {
                                                "بروزرسانی خودکار غیرفعال شد"
                                            }
                                        )
                                    }
                                }
                            ),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isAutoRefreshEnabled) Green600.copy(alpha = 0.16f) else palette.accentBg,
                        border = BorderStroke(1.dp, if (isAutoRefreshEnabled) Green600 else palette.accentBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isAutoRefreshEnabled) "$secondsUntilNextRefresh ثانیه" else "بروزرسانی",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isAutoRefreshEnabled) Green600 else palette.accent
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "بروزرسانی",
                                modifier = Modifier
                                    .size(20.dp)
                                    .rotate(rotation.value),
                                tint = if (isAutoRefreshEnabled) Green600 else palette.accent
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = palette.mutedBg
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
                            color = if (selectedTab == 0) palette.cardBg else Color.Transparent,
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
                                    tint = if (selectedTab == 0) Amber700 else palette.mutedText,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ورود شده",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == 0) Amber700 else palette.mutedText
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selectedTab == 0) Amber700.copy(alpha = 0.15f) else palette.mutedBg
                                ) {
                                    Text(
                                        text = "${nonExitedCargos.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTab == 0) Amber700 else palette.mutedText,
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
                            color = if (selectedTab == 1) palette.cardBg else Color.Transparent,
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
                                    tint = if (selectedTab == 1) Green600 else palette.mutedText,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "خروج شده",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == 1) Green600 else palette.mutedText
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selectedTab == 1) Green600.copy(alpha = 0.15f) else palette.mutedBg
                                ) {
                                    Text(
                                        text = "${exitedCargos.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTab == 1) Green600 else palette.mutedText,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = palette.cardBg,
                    border = BorderStroke(1.dp, palette.cardBorder)
                ) {
                    if (visibleItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotEmpty()) "موردی یافت نشد" else "لیست خالی است",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.mutedText
                            )
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            visibleItems.forEachIndexed { index, info ->
                                key(info.id ?: info.trackingNumber) {
                                    ErrorHandlingCargoInfoRow(
                                        info = info,
                                        onRowClick = { selectedInfo ->
                                            selectedCargoInfo.value = selectedInfo
                                            showDetailDialog.value = true
                                        },
                                        duplicateTrackingNumbers = duplicateTrackingNumbers
                                    )
                                }
                                if (index < visibleItems.lastIndex) {
                                    HorizontalDivider(
                                        color = palette.cardBorder,
                                        thickness = 1.dp
                                    )
                                }
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
                    }
                )
            }
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
                    viewModel.hideNetWeightDialog()
                },
                onDismiss = {
                    viewModel.hideNetWeightDialog()
                }
            )
        }
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
            onConfirm = { selectedQuota ->
                viewModel.switchQuota(selectedQuota)
                showQuotaEntryDialog = false
            },
            shipName = initialInfo?.shipName ?: "",
            currentQuota = initialInfo?.loadingQuotaNumber?.toString() ?: "",
            viewModel = viewModel
        )
    }
}
