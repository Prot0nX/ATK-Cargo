package com.atk.atk_cargo

//noinspection UsingMaterialAndMaterial3Libraries
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInBack
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddChart
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.atk.atk_cargo.api.CargoInfo
import com.atk.atk_cargo.api.CargoInfoRequest
import com.atk.atk_cargo.api.CargoViewModel
import com.atk.atk_cargo.api.CargoViewModelFactory
import com.atk.atk_cargo.api.InitialInfo
import com.atk.atk_cargo.api.MessageType
import com.atk.atk_cargo.api.ReportsRepository
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.api.ShipInfo
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.api.WarningStatus
import com.atk.atk_cargo.api.recognizeText
import com.atk.atk_cargo.ui.theme.Green800
import com.atk.atk_cargo.ui.theme.Theme2
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.patrykandpatrick.vico.core.extension.sumOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

class RegisterCargoActivity : ComponentActivity() {
    private lateinit var viewModel: CargoViewModel
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        result.contents?.let { barcode ->
            updateScaleReceiptNumber(barcode)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val initialInfoExtra = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("initialInfo", InitialInfo::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("initialInfo") as? InitialInfo
        }

        if (initialInfoExtra == null) {
            finish()
            return
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val reportsRepository = ReportsRepository(RetrofitClient.apiService)
        val userPreferencesManager = UserPreferencesManager(this)
        val viewModelFactory = CargoViewModelFactory(reportsRepository, userPreferencesManager)
        viewModel = ViewModelProvider(this, viewModelFactory)[CargoViewModel::class.java]

        setContent {
            Theme2 {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colors.background
                    ) {
                        val cargoInfoList by viewModel.cargoInfoList.collectAsState()
                        val initialInfo by viewModel.initialInfo.collectAsState()
                        val cargoWeight by viewModel.cargoWeight.collectAsState()
                        val totalNetWeight by viewModel.totalNetWeight.collectAsState()
                        val remainingWeight by viewModel.remainingWeight.collectAsState()
                        val averageNetWeight by viewModel.averageNetWeight.collectAsState()
                        val remainingServices by viewModel.remainingServices.collectAsState()
                        val totalServices by viewModel.totalServices.collectAsState()
                        val resultMessage by viewModel.resultMessage.collectAsState()
                        val showAnimatedMessage by viewModel.showAnimatedMessage.collectAsState()
                        val messageType by viewModel.messageType.collectAsState()

                        RegisterCargoScreen(
                            initialInfo = initialInfo,
                            cargoInfoList = cargoInfoList,
                            cargoWeight = cargoWeight,
                            totalNetWeight = totalNetWeight,
                            remainingWeight = remainingWeight,
                            averageNetWeight = averageNetWeight,
                            remainingServices = remainingServices,
                            totalServices = totalServices,
                            resultMessage = resultMessage,
                            showAnimatedMessage = showAnimatedMessage,
                            messageType = messageType,
                            viewModel = viewModel,
                            activity = this
                        )

                        LaunchedEffect(initialInfoExtra) {
                            initialInfoExtra.let { info ->
                                viewModel.loadCargoInfoList(
                                    quotaNumber = info.loadingQuotaNumber.toString(),
                                    shippingCompany = info.shippingCompany,
                                    warehouse = info.loadingWarehouse,
                                    cargoType = info.cargoType,
                                    onComplete = {}
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun startBarcodeScanner() {
        val options = ScanOptions()
            .setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
            .setPrompt("اسکن قبض یاسکول")
            .setCameraId(0)
            .setBeepEnabled(false)
            .setBarcodeImageEnabled(true)
            .setOrientationLocked(false)

        barcodeLauncher.launch(options)
    }

    private fun updateScaleReceiptNumber(barcode: String) {
        if (isValidScaleReceipt(barcode)) {
            val cleanedBarcode = barcode.replace(Regex("[^0-9]"), "")
            viewModel.updateScaleReceiptNumber(cleanedBarcode)
        } else {
            showErrorMessage("قبض باسکول نامعتبر است. لطفاً قبض باسکول صحیح را اسکن کنید!")
        }
    }

    private fun isValidScaleReceipt(scaleReceipt: String): Boolean {
        val cleanedReceipt = scaleReceipt.replace(Regex("[^0-9]"), "")

        return cleanedReceipt.length == 8
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope", "DefaultLocale")
@Composable
fun RegisterCargoScreen(
    initialInfo: InitialInfo?,
    cargoInfoList: List<CargoInfo>,
    cargoWeight: String,
    totalNetWeight: String,
    remainingWeight: String,
    averageNetWeight: String,
    remainingServices: String,
    totalServices: String,
    resultMessage: String,
    showAnimatedMessage: Boolean,
    messageType: MessageType,
    viewModel: CargoViewModel,
    activity: RegisterCargoActivity
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
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
    var exitDateQuery by remember { mutableStateOf("") }
    var searchMode by remember { mutableStateOf(SearchMode.TRACKING_NUMBER) }
    var isFormExpanded by remember { mutableStateOf(true) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    val clearInputFields by viewModel.clearInputFields.collectAsState()
    val showNetWeightDialog by viewModel.showNetWeightDialog.collectAsState()
    val scaleReceiptNumber by viewModel.scaleReceiptNumber.collectAsState()
    val focusManager = LocalFocusManager.current
    var showExitStatusDialog by remember { mutableStateOf(false) }
    var showStatisticsDialog by remember { mutableStateOf(false) }
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    var showQuotaWarning by remember { mutableStateOf<WarningStatus?>(null) }
    val loadableTonnage by viewModel.loadableTonnage.collectAsState()

//    LaunchedEffect(Unit) {
//        while (true) {
//            delay(30000)
//            viewModel.refreshCargoInfo()
//        }
//    }

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

    val filteredCargoInfoList by remember(
        cargoInfoList,
        searchQuery,
        exitDateQuery,
        searchMode
    ) {
        derivedStateOf {
            cargoInfoList.filter { cargoInfo ->
                when (searchMode) {
                    SearchMode.TRACKING_NUMBER -> cargoInfo.trackingNumber.contains(
                        searchQuery,
                        ignoreCase = true
                    )
                    SearchMode.EXIT_DATE -> cargoInfo.exitDate?.contains(
                        exitDateQuery,
                        ignoreCase = true
                    ) ?: false
                }
            }
        }
    }

    // تفکیک حواله‌ها به دو دسته خروج نشده و خروج شده
    val (nonExitedCargos, exitedCargos) = filteredCargoInfoList.partition { it.status == "ورود" }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .navigationBarsPadding(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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
                    remainingServices = initialInfo?.remainingServices.toString()
                )

                ShipInfoSection(
                    shipInfo = shipInfo,
                    isInfoVisible = isInfoVisible,
                    onToggleVisibility = { isInfoVisible = !isInfoVisible },
                    loadableTonnage = loadableTonnage
                )

                Spacer(modifier = Modifier.height(4.dp))

                AnimatedVisibility(
                    visible = isFormExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
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
                                activity.startBarcodeScanner()
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
                        onSubmit = {
                            coroutineScope.launch {
                                if (trackingNumber.isBlank()) {
                                    snackbarHostState.showSnackbar("لطفاً شماره حواله را وارد کنید.")
                                    return@launch
                                }

                                val isNewCargo = cargoInfoList.none { it.trackingNumber == trackingNumber }
                                if (isNewCargo) {
                                    val numberOfPeopleValue = numberOfPeople.toIntOrNull()
                                    if (numberOfPeopleValue == null || numberOfPeopleValue < 1) {
                                        snackbarHostState.showSnackbar("تعداد نفرات باید عددی بزرگتر از صفر باشد.")
                                        return@launch
                                    }
                                }

                                val netWeightValue = netWeight.toIntOrNull()
                                if (netWeight.isNotBlank() && (netWeightValue == null || netWeightValue !in 5000..45000)) {
                                    snackbarHostState.showSnackbar("وزن خالص باید بین 5000 تا 45000 کیلوگرم باشد.")
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

                                focusManager.clearFocus()
                            }
                        }
                    )
                }

                IconButton(
                    onClick = { isFormExpanded = !isFormExpanded },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        imageVector = if (isFormExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isFormExpanded) "بستن فرم" else "باز کردن فرم",
                        tint = MaterialTheme.colors.onSurface
                    )
                }

                Divider(
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                    thickness = 1.dp
                )

                AnimatedVisibility(
                    visible = isSearchExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    SearchSection(
                        searchMode = searchMode,
                        onSearchModeChange = { searchMode = it },
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        exitDateQuery = exitDateQuery,
                        onExitDateQueryChange = { exitDateQuery = it },
                        onSearch = { /* Search is handled automatically via filteredCargoInfoList */ }
                    )
                }

                IconButton(
                    onClick = { isSearchExpanded = !isSearchExpanded },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        imageVector = if (isSearchExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isSearchExpanded) "بستن جستجو" else "باز کردن جستجو",
                        tint = MaterialTheme.colors.onSurface
                    )
                }

                // دکمه بروزرسانی
                Button(
                    onClick = { viewModel.refreshCargoInfo() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "بروزرسانی")
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("بروزرسانی اطلاعات")
                }

                ExpandableSection(
                    title = "ورود شده",
                    items = nonExitedCargos.sortedByDescending { it.entryTime },
                    initiallyExpanded = true,
                    onItemClick = { selectedInfo ->
                        selectedCargoInfo.value = selectedInfo
                        showDetailDialog.value = true
                    }
                )

                ExpandableSection(
                    title = "خروج شده",
                    items = exitedCargos.sortedByDescending { "${it.exitDate} ${it.exitTime}" },
                    initiallyExpanded = false,
                    onItemClick = { selectedInfo ->
                        selectedCargoInfo.value = selectedInfo
                        showDetailDialog.value = true
                    }
                )
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
                        snackbarHostState = snackbarHostState
                    ) {
                        showDetailDialog.value = false
                    }
                }
            }

            if (showConfirmationDialog) {
                DialogPassword(
                    message = confirmationMessage,
                    onConfirm = {
                        cargoInfoToUpdate?.let { cargoInfo ->
                            viewModel.updateCargoInfo(cargoInfo, netWeight)
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

            if (showExitStatusDialog) {
                ExitStatusDialog(
                    showDialog = true,
                    onDismiss = { showExitStatusDialog = false },
                    exitVouchersCount = exitedCargos.size,
                    totalNetWeight = exitedCargos.sumOf { it.netWeight.toFloatOrNull() ?: 0f }
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
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
                color = MaterialTheme.colors.surface
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
}

@Composable
fun QuotaWarningDialog(
    warning: WarningStatus,
    onDismiss: () -> Unit,
    viewModel: CargoViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = ""
    )
    val alpha by animateFloatAsState(targetValue = 1f, label = "")

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
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 8.dp)
                .scale(scale)
                .alpha(alpha),
            shape = RoundedCornerShape(16.dp),
            elevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            MaterialTheme.colors.error.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colors.error,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "هشدار محدودیت درصد کوتاژ",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.error,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Message
                Text(
                    text = "کوتاژ ${warning.quotaNumber} به حد نصاب ${warning.percentage}% رسیده است و امکان ثبت حواله جدید و خروج وجود ندارد.",
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Close Button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.toggleQuotaStatus(warning.quotaNumber)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.error,
                        contentColor = MaterialTheme.colors.onError
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("متوجه شدم")
                }
            }
        }
    }
}

@Composable
fun NetWeightDialog(
    scaleReceiptNumber: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var netWeight by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    var recognizedWeight by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
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

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    fun validateAndConfirm() {
        val weight = netWeight.toIntOrNull()
        if (weight != null && weight in 5000..45000) {
            onConfirm(netWeight)
        } else {
            isError = true
        }
    }

    Dialog(
        onDismissRequest = {
            focusManager.clearFocus()
            onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        BoxWithConstraints {
            val dialogWidth = maxWidth * 0.9f
            AnimatedVisibility(
                visible = true,
                enter = dialogEnterTransition,
                exit = dialogExitTransition
            ) {
                Surface(
                    modifier = Modifier
                        .width(dialogWidth)
                        .clip(RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colors.primary,
                                        MaterialTheme.colors.primaryVariant
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Text(
                                    "ثبت وزن خالص",
                                    style = MaterialTheme.typography.h5,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                IconButton(
                                    onClick = {
                                        focusManager.clearFocus()
                                        onDismiss()
                                    },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "بستن",
                                        tint = Color.White
                                    )
                                }
                            }

                            Text(
                                "شماره قبض باسکول: $scaleReceiptNumber",
                                style = MaterialTheme.typography.body1,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = netWeight,
                                    onValueChange = {
                                        netWeight = it
                                        isError = false
                                    },
                                    label = {
                                        Text("وزن خالص (کیلوگرم)", color = Color.White.copy(alpha = 0.7f))
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    keyboardActions = KeyboardActions(onDone = {
                                        focusManager.clearFocus()
                                        validateAndConfirm()
                                    }),
                                    isError = isError,
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(focusRequester),
                                    textStyle = LocalTextStyle.current.copy(
                                        textAlign = TextAlign.Center,
                                        color = Color.White
                                    ),
                                    singleLine = true,
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = Color.White,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                                        cursorColor = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { showCamera = true },
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(Color.White, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Camera,
                                        contentDescription = "اسکن وزن",
                                        tint = MaterialTheme.colors.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            if (isError) {
                                Text(
                                    "وزن خالص باید بین 5000 تا 45000 کیلوگرم باشد",
                                    color = Color.Red,
                                    style = MaterialTheme.typography.caption,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    validateAndConfirm()
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "تائید",
                                    color = MaterialTheme.colors.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCamera) {
        Dialog(onDismissRequest = {
            showCamera = false
        }) {
            CameraPreview(
                onImageCaptured = { image, detectedWeight ->
                    showCamera = false
                    coroutineScope.launch {
                        try {
                            // Use the detected weight directly if available
                            if (!detectedWeight.isNullOrEmpty()) {
                                // Check if within valid range
                                val weightValue = detectedWeight.toDoubleOrNull()
                                if (weightValue != null && weightValue in 5000.0..45000.0) {
                                    netWeight = detectedWeight
                                } else {
                                    // Fallback to image processing if weight is invalid
                                    val preprocessedImage = preprocessImage(image)
                                    val recognizedText = recognizeText(preprocessedImage)
                                    recognizedWeight = extractNumber(recognizedText)

                                    if (recognizedWeight.isNotEmpty()) {
                                        netWeight = recognizedWeight
                                    } else {
                                        isError = true
                                    }
                                }
                            } else {
                                // Fallback to original implementation if no weight detected
                                val preprocessedImage = preprocessImage(image)
                                val recognizedText = recognizeText(preprocessedImage)
                                recognizedWeight = extractNumber(recognizedText)

                                if (recognizedWeight.isNotEmpty()) {
                                    netWeight = recognizedWeight
                                } else {
                                    isError = true
                                }
                            }
                        } catch (e: Exception) {
                            isError = true
                        }
                    }
                },
                onError = {
                    showCamera = false
                    isError = true
                }
            )
        }
    }
}

@SuppressLint("DefaultLocale")
fun extractNumber(text: String): String {
    val regex = Regex("""(\d{1,3}(?:,\d{3})*(?:\.\d+)?)""")
    val matches = regex.findAll(text)

    val numbers = matches.mapNotNull { matchResult ->
        matchResult.value.replace(",", "").toDoubleOrNull()
    }.toList()

    return numbers.maxOrNull()?.let {
        String.format("%d", it.roundToInt())
    } ?: ""
}

fun preprocessImage(imageProxy: ImageProxy): InputImage {
    val bitmap = imageProxy.toBitmap()
    val width = bitmap.width
    val height = bitmap.height

    val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(outputBitmap)
    val paint = Paint()

    val colorMatrix = ColorMatrix(floatArrayOf(
        1.5f, 0f, 0f, 0f, -50f,
        0f, 1.5f, 0f, 0f, -50f,
        0f, 0f, 1.5f, 0f, -50f,
        0f, 0f, 0f, 1f, 0f
    ))

    paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
    canvas.drawBitmap(bitmap, 0f, 0f, paint)

    return InputImage.fromBitmap(outputBitmap, imageProxy.imageInfo.rotationDegrees)
}

@Composable
fun CameraPreview(
    onImageCaptured: (ImageProxy, String?) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var preview: Preview? by remember { mutableStateOf(null) }
    var camera: Camera? by remember { mutableStateOf(null) }
    val lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    val executor = ContextCompat.getMainExecutor(context)

    // Weight detection state
    var detectedNumber by remember { mutableStateOf<String?>(null) }
    var detectedNumbers by remember { mutableStateOf<List<String>>(emptyList()) }
    var isValidWeight by remember { mutableStateOf(false) }
    var processingActive by remember { mutableStateOf(true) }

    // Camera status
    var hasTorch by remember { mutableStateOf(false) }
    var isTorchOn by remember { mutableStateOf(false) }

    // Scanner guide parameters
    val guideColor = Color.Green.copy(alpha = 0.7f)
    val guideThickness = 2.dp
    val scanAreaSize = 0.7f // 70% of screen width

    Box(modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)
    ) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    this.scaleType = PreviewView.ScaleType.FILL_CENTER
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    preview = Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build()
                        .also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()

                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build()

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build()
                        .apply {
                            setAnalyzer(executor, EnhancedNumberAnalyzer { extractedNumbers, bestEstimate ->
                                if (processingActive) {
                                    detectedNumbers = extractedNumbers

                                    // Using smarter algorithm to select best number
                                    if (bestEstimate.isNotEmpty()) {
                                        detectedNumber = bestEstimate
                                        val weight = bestEstimate.toDoubleOrNull()
                                        isValidWeight = weight != null && weight in 5000.0..45000.0
                                    }
                                }
                            })
                        }

                    try {
                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture,
                            imageAnalysis
                        )

                        // Check flashlight support
                        hasTorch = camera?.cameraInfo?.hasFlashUnit() ?: false
                    } catch (exc: Exception) {
                        exc.printStackTrace()
                    }
                }, executor)
                previewView
            },
            modifier = Modifier.matchParentSize()
        )

        // Scanner guide overlay
        ScannerGuideOverlay(
            scanAreaSize = scanAreaSize,
            guideColor = guideColor,
            guideThickness = guideThickness.value
        )

        // Display detected weight
        if (detectedNumber != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .background(
                        color = if (isValidWeight)
                            Color(0xFF4CAF50).copy(alpha = 0.7f)
                        else
                            Color(0xFFE57373).copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = "وزن تشخیص داده شده:",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "${NumberFormat.getNumberInstance(Locale("en", "US")).format(detectedNumber?.toDoubleOrNull() ?: 0)} کیلوگرم",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                if (!isValidWeight && detectedNumber?.isNotEmpty() == true) {
                    Text(
                        text = "وزن باید بین 5,000 تا 45,000 کیلوگرم باشد",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        // Display guidance message
        if (detectedNumber == null) {
            Text(
                text = "قبض باسکول را در کادر قرار دهید",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            )
        }

        // Capture button
        IconButton(
            onClick = {
                processingActive = false
                imageCapture?.takePicture(
                    executor,
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            // Pass the current detected weight along with the image
                            onImageCaptured(image, detectedNumber)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            onError(exception)
                        }
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .size(64.dp)
                .background(Color.White.copy(alpha = 0.7f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Camera,
                contentDescription = "گرفتن عکس",
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
        }

        // Flashlight button
        if (hasTorch) {
            IconButton(
                onClick = {
                    isTorchOn = !isTorchOn
                    camera?.cameraControl?.enableTorch(isTorchOn)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isTorchOn) Icons.Default.FlashOff else Icons.Default.FlashOn,
                    contentDescription = "چراغ قوه",
                    tint = if (isTorchOn) Color.Yellow else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    // Release resources when leaving the screen
    DisposableEffect(lifecycleOwner) {
        onDispose {
            processingActive = false
        }
    }
}

@Composable
fun ScannerGuideOverlay(
    scanAreaSize: Float = 0.7f,
    guideColor: Color = Color.Green.copy(alpha = 0.7f),
    guideThickness: Float = 2f
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val scanAreaWidth = width * scanAreaSize
        val scanAreaHeight = height * scanAreaSize
        val left = (width - scanAreaWidth) / 2
        val top = (height - scanAreaHeight) / 2

        // Main rectangle frame
        drawRect(
            color = guideColor,
            topLeft = Offset(left, top),
            size = Size(scanAreaWidth, scanAreaHeight),
            style = Stroke(width = guideThickness)
        )

        // Corner indicators
        val cornerSize = 20f

        // Top-left corner
        drawLine(
            color = guideColor,
            start = Offset(left, top),
            end = Offset(left + cornerSize, top),
            strokeWidth = guideThickness
        )
        drawLine(
            color = guideColor,
            start = Offset(left, top),
            end = Offset(left, top + cornerSize),
            strokeWidth = guideThickness
        )

        // Top-right corner
        drawLine(
            color = guideColor,
            start = Offset(left + scanAreaWidth, top),
            end = Offset(left + scanAreaWidth - cornerSize, top),
            strokeWidth = guideThickness
        )
        drawLine(
            color = guideColor,
            start = Offset(left + scanAreaWidth, top),
            end = Offset(left + scanAreaWidth, top + cornerSize),
            strokeWidth = guideThickness
        )

        // Bottom-left corner
        drawLine(
            color = guideColor,
            start = Offset(left, top + scanAreaHeight),
            end = Offset(left + cornerSize, top + scanAreaHeight),
            strokeWidth = guideThickness
        )
        drawLine(
            color = guideColor,
            start = Offset(left, top + scanAreaHeight),
            end = Offset(left, top + scanAreaHeight - cornerSize),
            strokeWidth = guideThickness
        )

        // Bottom-right corner
        drawLine(
            color = guideColor,
            start = Offset(left + scanAreaWidth, top + scanAreaHeight),
            end = Offset(left + scanAreaWidth - cornerSize, top + scanAreaHeight),
            strokeWidth = guideThickness
        )
        drawLine(
            color = guideColor,
            start = Offset(left + scanAreaWidth, top + scanAreaHeight),
            end = Offset(left + scanAreaWidth, top + scanAreaHeight - cornerSize),
            strokeWidth = guideThickness
        )

        // Horizontal guide line
        drawLine(
            color = guideColor.copy(alpha = 0.4f),
            start = Offset(left, top + scanAreaHeight / 2),
            end = Offset(left + scanAreaWidth, top + scanAreaHeight / 2),
            strokeWidth = guideThickness / 2,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    }
}

class EnhancedNumberAnalyzer(
    private val onNumbersDetected: (List<String>, String) -> Unit
) : ImageAnalysis.Analyzer {
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val previousNumbers = mutableListOf<DetectedNumber>()
    private val maxHistorySize = 15
    private var lastDetectionTime = 0L
    private val detectionCooldown = 100L // میلی‌ثانیه

    private data class DetectedNumber(
        val value: String,
        val confidence: Float,
        val timestamp: Long
    )

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastDetectionTime < detectionCooldown) {
            imageProxy.close()
            return
        }
        lastDetectionTime = currentTime

        val preprocessedImage = enhancedPreprocessImage(imageProxy)
        textRecognizer.process(preprocessedImage)
            .addOnSuccessListener { visionText ->
                val text = visionText.text
                val detectedNumbers = extractNetWeights(text)

                // محاسبه اطمینان برای هر عدد تشخیص داده شده
                val numbersWithConfidence = detectedNumbers.map { number ->
                    val confidence = calculateConfidence(number, visionText)
                    DetectedNumber(number, confidence, currentTime)
                }

                // به‌روزرسانی تاریخچه با حفظ اعداد معتبر
                updateHistory(numbersWithConfidence)

                // انتخاب بهترین تخمین با استفاده از الگوریتم وزن‌دار
                val bestEstimate = selectBestEstimate()

                onNumbersDetected(detectedNumbers, bestEstimate)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun calculateConfidence(number: String, visionText: Text): Float {
        var confidence = 0f
        
        // بررسی وضوح و کیفیت متن
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                if (line.text.contains(number)) {
                    // محاسبه امتیاز بر اساس وضوح متن
                    confidence = maxOf(confidence, calculateTextQuality(line))
                }
            }
        }

        // اعتبارسنجی محدوده عدد
        val numValue = number.toDoubleOrNull() ?: return 0f
        confidence *= when (numValue) {
            in 10000.0..30000.0 -> 1.2f  // محدوده معمول
            in 5000.0..45000.0 -> 1.0f   // محدوده قابل قبول
            else -> 0.5f                          // خارج از محدوده معمول
        }

        return confidence.coerceIn(0f, 1f)
    }

    private fun calculateTextQuality(line: Text.Line): Float {
        var quality = 0f
        
        // بررسی زاویه متن
        quality += if (abs(line.angle) < 5) 0.3f else 0.1f
        
        // بررسی اندازه متن
        val height = line.boundingBox?.height() ?: 0
        quality += when {
            height > 40 -> 0.4f  // متن بزرگ و واضح
            height > 20 -> 0.3f  // متن متوسط
            else -> 0.1f         // متن کوچک
        }

        // بررسی کنتراست محلی
        quality += 0.3f // مقدار پایه برای کنتراست
        
        return quality
    }

    private fun updateHistory(newNumbers: List<DetectedNumber>) {
        // حذف اعداد قدیمی
        val currentTime = System.currentTimeMillis()
        previousNumbers.removeAll { currentTime - it.timestamp > 2000 } // حذف اعداد قدیمی‌تر از 2 ثانیه

        // اضافه کردن اعداد جدید
        previousNumbers.addAll(newNumbers)

        // محدود کردن اندازه تاریخچه
        while (previousNumbers.size > maxHistorySize) {
            previousNumbers.removeAt(0)
        }
    }

    private fun selectBestEstimate(): String {
        if (previousNumbers.isEmpty()) return ""

        // گروه‌بندی اعداد و محاسبه امتیاز کل هر عدد
        val scores = previousNumbers
            .groupBy { it.value }
            .mapValues { (_, detections) ->
                val frequencyScore = detections.size.toFloat() / previousNumbers.size
                val confidenceScore = detections.maxOf { it.confidence }
                val timeScore = detections.maxOf { 1.0f - (System.currentTimeMillis() - it.timestamp) / 2000.0f }
                
                // ترکیب امتیازها با وزن‌های مختلف
                (frequencyScore * 0.4f + confidenceScore * 0.4f + timeScore * 0.2f)
            }

        // انتخاب عدد با بالاترین امتیاز
        return scores.maxByOrNull { it.value }?.key ?: ""
    }

    private fun enhancedPreprocessImage(imageProxy: ImageProxy): InputImage {
        val bitmap = imageProxy.toBitmap()
        val width = bitmap.width
        val height = bitmap.height

        val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)

        // افزایش کنتراست و روشنایی برای تشخیص بهتر متون کمرنگ
        val colorMatrix = ColorMatrix(floatArrayOf(
            2.5f, 0f, 0f, 0f, -70f,  // افزایش کنتراست قرمز
            0f, 2.5f, 0f, 0f, -70f,  // افزایش کنتراست سبز
            0f, 0f, 2.5f, 0f, -70f,  // افزایش کنتراست آبی
            0f, 0f, 0f, 1.3f, 0f     // افزایش شفافیت
        ))

        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
            // اضافه کردن فیلتر شارپنس برای وضوح بیشتر
            maskFilter = BlurMaskFilter(1f, BlurMaskFilter.Blur.NORMAL)
        }

        // اعمال فیلترهای پیشرفته
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        // تبدیل به سیاه و سفید با آستانه تطبیقی
        val pixels = IntArray(width * height)
        outputBitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        // محاسبه آستانه تطبیقی برای هر بخش از تصویر
        val blockSize = 15
        for (y in 0 until height step blockSize) {
            for (x in 0 until width step blockSize) {
                val blockThreshold = calculateLocalThreshold(pixels, x, y, 
                    minOf(blockSize, width - x), 
                    minOf(blockSize, height - y), 
                    width)
                
                applyThreshold(pixels, x, y, 
                    minOf(blockSize, width - x), 
                    minOf(blockSize, height - y), 
                    width, blockThreshold)
            }
        }
        
        outputBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        return InputImage.fromBitmap(outputBitmap, imageProxy.imageInfo.rotationDegrees)
    }

    private fun calculateLocalThreshold(pixels: IntArray, startX: Int, startY: Int, 
                                     blockWidth: Int, blockHeight: Int, stride: Int): Int {
        var sum = 0
        var count = 0
        
        for (y in startY until startY + blockHeight) {
            for (x in startX until startX + blockWidth) {
                val pixel = pixels[y * stride + x]
                val gray = ((pixel shr 16 and 0xFF) + (pixel shr 8 and 0xFF) + (pixel and 0xFF)) / 3
                sum += gray
                count++
            }
        }
        
        // محاسبه آستانه با استفاده از میانگین محلی
        return (sum / count) - 10 // کاهش آستانه برای تشخیص بهتر متون کمرنگ
    }

    private fun applyThreshold(pixels: IntArray, startX: Int, startY: Int, 
                             blockWidth: Int, blockHeight: Int, stride: Int, threshold: Int) {
        for (y in startY until startY + blockHeight) {
            for (x in startX until startX + blockWidth) {
                val idx = y * stride + x
                val pixel = pixels[idx]
                val gray = ((pixel shr 16 and 0xFF) + (pixel shr 8 and 0xFF) + (pixel and 0xFF)) / 3
                pixels[idx] = if (gray > threshold) -1 else -16777216 // White = -1, Black = -16777216
            }
        }
    }

    private fun extractNetWeights(text: String): List<String> {
        val results = mutableListOf<String>()
        
        // الگوهای متداول وزن خالص در قبض‌های باسکول با انعطاف‌پذیری بیشتر
        val patterns = listOf(
            // الگوهای دقیق با کلمات کلیدی
            Regex("(?:وزن\\s*خالص|خالص)[\\s:]*[\\d۰-۹,.\\s]+(?:کیلو(?:گرم)?|KG)?", RegexOption.IGNORE_CASE),
            Regex("(?:NET\\s*WEIGHT|NET)[\\s:]*[\\d,.\\s]+(?:KG|Kg|kg)?", RegexOption.IGNORE_CASE),
            
            // الگوهای عمومی برای اعداد در محدوده وزن
            Regex("(\\d{1,3}(?:[,\\s]\\d{3})*(?:\\.\\d+)?)", RegexOption.IGNORE_CASE),
            
            // الگو برای اعداد فارسی
            Regex("[۰-۹]{2,6}(?:[,،٫]?[۰-۹]{3})*"),
            
            // الگوی ساده برای اعداد در محدوده مورد نظر
            Regex("\\b\\d{4,6}\\b")
        )

        // تبدیل اعداد فارسی به انگلیسی
        val persianDigits = "۰۱۲۳۴۵۶۷۸۹"
        val englishDigits = "0123456789"
        var normalizedText = text
        for (i in persianDigits.indices) {
            normalizedText = normalizedText.replace(persianDigits[i], englishDigits[i])
        }

        // جستجوی الگوها در متن نرمال‌سازی شده
        for (pattern in patterns) {
            val matches = pattern.findAll(normalizedText)
            for (match in matches) {
                // استخراج فقط اعداد از متن یافت شده
                val numberStr = match.value.replace(Regex("[^0-9.]"), "")
                
                try {
                    val number = numberStr.toDoubleOrNull()
                    if (number != null) {
                        // اعتبارسنجی محدوده وزن با تلرانس بیشتر
                        if (number in 4000.0..50000.0) {
                            val roundedNumber = number.roundToInt()
                            results.add(roundedNumber.toString())
                        }
                    }
                } catch (e: Exception) {
                    // نادیده گرفتن خطاهای تبدیل عدد
                    continue
                }
            }
        }

        // اگر هیچ عددی پیدا نشد، از روش ساده‌تر استفاده کن
        if (results.isEmpty()) {
            val simpleNumbers = extractSimpleNumbers(normalizedText)
            results.addAll(simpleNumbers)
        }

        // حذف اعداد تکراری و مرتب‌سازی بر اساس فراوانی
        return results.groupBy { it }
            .mapValues { it.value.size }
            .entries
            .sortedByDescending { it.value }
            .map { it.key }
            .distinct()
    }

    private fun extractSimpleNumbers(text: String): List<String> {
        // جستجوی ساده برای اعداد 4 تا 6 رقمی
        val regex = Regex("\\b(\\d{4,6})\\b")
        val matches = regex.findAll(text)

        return matches.mapNotNull { matchResult ->
            try {
                val number = matchResult.value.toDoubleOrNull()
                if (number != null && number in 4000.0..50000.0) {
                    number.roundToInt().toString()
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }.distinct().toList()
    }
}

@Composable
fun ErrorHandlingCargoInfoRow(
    info: CargoInfo,
    onRowClick: (CargoInfo) -> Unit
) {
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(info) {
        hasError = false
        errorMessage = ""
    }

    if (hasError) {
        Text("Error: $errorMessage", color = Color.Red)
    } else {
        CargoInfoRow(
            info = info,
            onRowClick = onRowClick,
            onError = { error ->
                hasError = true
                errorMessage = "Error rendering item ${info.trackingNumber}: $error"
            }
        )
    }
}

@Composable
fun ExpandableSection(
    title: String,
    items: List<CargoInfo>,
    initiallyExpanded: Boolean = false,
    onItemClick: (CargoInfo) -> Unit
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$title (${items.size})",
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primary
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "بستن" else "باز کردن",
                tint = MaterialTheme.colors.primary
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn()
            ) {
                items(items) { info ->
                    ErrorHandlingCargoInfoRow(
                        info = info,
                        onRowClick = onItemClick
                    )
                }
            }
        }
    }
}

@Composable
fun ExitStatusDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    exitVouchersCount: Int,
    totalNetWeight: Float
) {
    if (showDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "آمار حواله‌های خروجی",
                        style = MaterialTheme.typography.h5,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.primary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    AnimatedCounter(
                        label = "تعداد حواله‌ها",
                        count = exitVouchersCount,
                        icon = Icons.Default.ConfirmationNumber
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedCounter(
                        label = "جمع وزن خالص (تن)",
                        count = totalNetWeight.toInt(),
                        icon = Icons.Default.Scale
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("بستن")
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedCounter(
    label: String,
    count: Int,
    icon: ImageVector
) {
    var animatedCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(count) {
        animate(
            initialValue = 0f,
            targetValue = count.toFloat(),
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        ) { value, _ ->
            animatedCount = value.toInt()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colors.primary,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = NumberFormat.getNumberInstance(Locale("en", "US")).format(animatedCount),
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primary
            )
        }
    }
}

@Composable
fun FormSection(
    trackingNumber: String,
    onTrackingNumberChange: (String) -> Unit,
    scaleReceiptNumber: String,
    onScanBarcode: () -> Unit,
    shortageWeight: String,
    onShortageWeightChange: (String) -> Unit,
    excessWeight: String,
    onExcessWeightChange: (String) -> Unit,
    numberOfPeople: String,
    onNumberOfPeopleChange: (String) -> Unit,
    cargoInfoList: List<CargoInfo>,
    isCargoConfirmed: Boolean,
    isCargoExited: Boolean,
    onSubmit: () -> Unit
) {
    val isDuplicate = remember(trackingNumber, cargoInfoList) {
        trackingNumber.isNotBlank() && cargoInfoList.any { it.trackingNumber == trackingNumber }
    }

    // دریافت اطلاعات حواله فعلی
    val currentCargo = remember(trackingNumber, cargoInfoList) {
        if (isDuplicate) cargoInfoList.find { it.trackingNumber == trackingNumber } else null
    }

    // بررسی امکان ویرایش کسری/اضافه بار
    val canEditWeights = remember(currentCargo) {
        currentCargo?.let {
            when {
                it.status == "ورود" && it.confirm == "تائید شده" -> true
                it.confirm == "در انتظار تائید" -> false
                it.status == "خروج" -> false
                else -> false
            }
        } ?: false
    }

    // منطق جدید برای فعال/غیرفعال کردن دکمه ثبت
    val isSubmitEnabled = remember(
        trackingNumber,
        isDuplicate,
        shortageWeight,
        excessWeight,
        canEditWeights
    ) {
        when {
            trackingNumber.isBlank() -> false
            !isDuplicate -> true
            !canEditWeights -> false
            else -> {
                val hasShortage = shortageWeight.isNotBlank() && shortageWeight != "0"
                val hasExcess = excessWeight.isNotBlank() && excessWeight != "0"
                hasShortage || hasExcess
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = trackingNumber,
                onValueChange = onTrackingNumberChange,
                label = { Text("شماره حواله") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = isDuplicate && !canEditWeights
            )
            OutlinedTextField(
                value = numberOfPeople,
                onValueChange = onNumberOfPeopleChange,
                label = { Text("تعداد نفرات") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = !isDuplicate
            )
        }

        // نمایش پیام متناسب با وضعیت
        if (isDuplicate) {
            Text(
                text = when {
                    currentCargo?.status == "خروج" -> "این حواله قبلاً خروج شده و قابل تغییر نیست!"
                    currentCargo?.confirm == "در انتظار تائید" -> "این حواله هنوز تائید نشده و قابل ویرایش نیست!"
                    canEditWeights -> "امکان ثبت کسری/اضافه بار یا خروج حواله وجود دارد!"
                    else -> "این حواله هنوز تائید نشده و قابل ویرایش نیست!"
                },
                color = when {
                    currentCargo?.status == "خروج" -> MaterialTheme.colors.error
                    currentCargo?.confirm == "در انتظار تائید" -> MaterialTheme.colors.error
                    canEditWeights -> MaterialTheme.colors.primary
                    else -> MaterialTheme.colors.error
                },
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = shortageWeight,
                onValueChange = onShortageWeightChange,
                label = { Text("کسری بار") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = canEditWeights
            )
            OutlinedTextField(
                value = excessWeight,
                onValueChange = onExcessWeightChange,
                label = { Text("اضافه بار") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = canEditWeights
            )
        }

        if (scaleReceiptNumber.isNotBlank()) {
            Text(
                text = "شماره قبض باسکول: $scaleReceiptNumber",
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onSubmit,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Green800,
                    contentColor = Color.White
                ),
                enabled = isSubmitEnabled
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isDuplicate) "ثبت تغییرات حواله" else "ثبت حواله جدید"
                )
            }

            Button(
                onClick = onScanBarcode,
                modifier = Modifier.weight(1f),
                enabled = isCargoConfirmed && !isCargoExited,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("خروج حواله")
            }
        }
    }
}

@Composable
fun MessageDialog(
    message: String,
    type: MessageType,
    visible: Boolean,
    onDismiss: () -> Unit
) {
    val composition by rememberLottieComposition(
        when (type) {
            MessageType.SUCCESS -> LottieCompositionSpec.RawRes(R.raw.lottie_success)
            MessageType.WARNING -> LottieCompositionSpec.RawRes(R.raw.lottie_warning)
            MessageType.ERROR -> LottieCompositionSpec.RawRes(R.raw.lottie_error)
        }
    )
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

    val backgroundColor = when (type) {
        MessageType.SUCCESS -> Color(0xFF4CAF50)
        MessageType.WARNING -> Color(0xFFFFA000)
        MessageType.ERROR -> Color(0xFFF44336)
    }

    if (visible) {
        Dialog(
            onDismissRequest = { /* Prevent dismissal on outside click */ },
            properties = DialogProperties(dismissOnClickOutside = false)
        ) {
            BoxWithConstraints {
                val dialogWidth = maxWidth
                AnimatedVisibility(
                    visible = true,
                    enter = dialogEnterTransition,
                    exit = dialogExitTransition
                ) {
                    Surface(
                        modifier = Modifier
                            .width(dialogWidth)
                            .padding(8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = backgroundColor,
                        elevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LottieAnimation(
                                composition = composition,
                                progress = { lottieAnimatable.progress },
                                modifier = Modifier.size(120.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = message,
                                style = MaterialTheme.typography.body1.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Right
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("باشه", color = backgroundColor)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchSection(
    searchMode: SearchMode,
    onSearchModeChange: (SearchMode) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    exitDateQuery: String,
    onExitDateQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "جستجو براساس:",
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.87f)
            )
            Switch(
                checked = searchMode == SearchMode.EXIT_DATE,
                onCheckedChange = { onSearchModeChange(if (it) SearchMode.EXIT_DATE else SearchMode.TRACKING_NUMBER) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colors.primary,
                    checkedTrackColor = MaterialTheme.colors.primary.copy(alpha = 0.5f),
                    uncheckedThumbColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    uncheckedTrackColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
                )
            )
            Text(
                if (searchMode == SearchMode.TRACKING_NUMBER) "شماره حواله" else "تاریخ خروج",
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.87f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = if (searchMode == SearchMode.TRACKING_NUMBER) searchQuery else exitDateQuery,
                onValueChange = if (searchMode == SearchMode.TRACKING_NUMBER) onSearchQueryChange else onExitDateQueryChange,
                label = {
                    Text(
                        if (searchMode == SearchMode.TRACKING_NUMBER) "شماره حواله" else "تاریخ خروج",
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = if (searchMode == SearchMode.TRACKING_NUMBER) KeyboardType.Number else KeyboardType.Text),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    cursorColor = MaterialTheme.colors.primary,
                    focusedBorderColor = MaterialTheme.colors.primary,
                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                )
            )
            Button(
                onClick = onSearch,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary
                )
            ) {
                Text("جستجو")
            }
        }
    }
}

enum class SearchMode {
    TRACKING_NUMBER,
    EXIT_DATE
}

@Composable
fun ShipInfoSection(
    shipInfo: ShipInfo,
    isInfoVisible: Boolean,
    onToggleVisibility: () -> Unit,
    loadableTonnage: String
) {
    val loadedPercentage = remember(shipInfo.cargoWeight, shipInfo.totalNetWeight) {
        try {
            val totalWeight = shipInfo.cargoWeight.replace(",", "").toFloatOrNull() ?: 0f
            val loadedWeight = shipInfo.totalNetWeight.replace(",", "").toFloatOrNull() ?: 0f
            if (totalWeight > 0) {
                String.format(Locale.ENGLISH, "%.1f", (loadedWeight / totalWeight) * 100)
            } else "0.0"
        } catch (e: Exception) {
            "0.0"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(8.dp),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HeaderInfo(
                onToggle = onToggleVisibility,
                loadedPercentage = loadedPercentage.toFloat(),
                shipName = shipInfo.shipName,
                quotaNumber = shipInfo.loadingQuotaNumber,
                loadableTonnage = loadableTonnage
            )

            AnimatedVisibility(
                visible = isInfoVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    QuickStatsRow(shipInfo)
                    Spacer(modifier = Modifier.height(16.dp))
                    DetailedInfoGrid(shipInfo)
                }
            }
        }
    }
}

@Composable
private fun HeaderInfo(
    onToggle: () -> Unit,
    loadedPercentage: Float,
    shipName: String,
    quotaNumber: String,
    loadableTonnage: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsBoat,
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = shipName,
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "کوتاژ: $quotaNumber",
                    style = MaterialTheme.typography.body2,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // نمایش تناژ قابل بارگیری
            Column(
                horizontalAlignment = Alignment.End
            ) {
                val tonnageValue = loadableTonnage.replace(",", "").toDoubleOrNull() ?: 0.0
                val (textColor, statusText) = if (tonnageValue < 0) {
                    MaterialTheme.colors.error to "بیش از حد"
                } else {
                    MaterialTheme.colors.primary to "قابل بارگیری"
                }
                
                Text(
                    text = "$loadableTonnage کیلوگرم",
                    style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        LinearProgressIndicator(
            progress = loadedPercentage / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = MaterialTheme.colors.primary,
            backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun QuickStatsRow(shipInfo: ShipInfo) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(
            icon = Icons.Default.Scale,
            value = formatNumber(shipInfo.cargoWeight),
            label = "تناژ کل",
            color = MaterialTheme.colors.primary
        )
        StatItem(
            icon = Icons.Default.BarChart,
            value = formatNumber(shipInfo.remainingWeight),
            label = "باقیمانده",
            color = MaterialTheme.colors.secondary
        )
        StatItem(
            icon = Icons.Default.LocalShipping,
            value = toEnglishNumbers(shipInfo.totalServices),
            label = "حواله‌ها",
            color = MaterialTheme.colors.primaryVariant
        )
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.subtitle2,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.caption,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun DetailedInfoGrid(shipInfo: ShipInfo) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colors.surface,
        elevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            DetailInfoItem("انبار", shipInfo.loadingWarehouse, Icons.Default.Warehouse)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            DetailInfoItem("نوع کالا", shipInfo.cargoType, Icons.Default.Category)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            DetailInfoItem("شرکت باربری", shipInfo.shippingCompany, Icons.Default.Business)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            DetailInfoItem("بارگیری شده", formatNumber(shipInfo.totalNetWeight), Icons.Default.AddChart)
        }
    }
}

@Composable
private fun DetailInfoItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colors.primary.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.body2,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onSurface
        )
    }
}

@Composable
fun CargoInfoRow(
    info: CargoInfo,
    onRowClick: (CargoInfo) -> Unit,
    onError: (String) -> Unit
) {
    val formattedNetWeight = remember(info.netWeight) {
        try {
            DecimalFormat("#,###").format(info.netWeight.toDoubleOrNull() ?: 0)
        } catch (e: Exception) {
            onError("Invalid netWeight: ${info.netWeight}")
            "0"
        }
    }
    val formattedShortageWeight = remember(info.shortageWeight) {
        if (info.shortageWeight.isNotBlank()) {
            try {
                DecimalFormat("#,###").format(info.shortageWeight.toDoubleOrNull() ?: 0)
            } catch (e: Exception) {
                onError("Invalid shortageWeight: ${info.shortageWeight}")
                ""
            }
        } else ""
    }
    val formattedExcessWeight = remember(info.excessWeight) {
        if (info.excessWeight.isNotBlank()) {
            try {
                DecimalFormat("#,###").format(info.excessWeight.toDoubleOrNull() ?: 0)
            } catch (e: Exception) {
                onError("Invalid excessWeight: ${info.excessWeight}")
                ""
            }
        } else ""
    }
    val borderColor = if (info.status == "خروج") Color.Green else Color.Red
    val iconTint = if (info.confirm == "تائید شده") Color.Green else Color.Red

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .border(1.dp, borderColor, shape = RoundedCornerShape(8.dp))
            .clickable { onRowClick(info) },
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoBox(
                    icon = Icons.Default.LocalShipping,
                    label = "حواله",
                    value = info.trackingNumber,
                    Modifier.weight(1f)
                )
                InfoBox(
                    icon = Icons.Default.Event,
                    label = "تاریخ خروج",
                    value = info.exitDate,
                    Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (info.confirm == "تائید شده") Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "وزن خالص: $formattedNetWeight",
                    style = MaterialTheme.typography.body2,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    if (formattedShortageWeight.isNotBlank()) {
                        Text(
                            text = "کسری: $formattedShortageWeight",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.secondary
                        )
                    }
                    if (formattedExcessWeight.isNotBlank()) {
                        if (formattedShortageWeight.isNotBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = "اضافه: $formattedExcessWeight",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoBox(
    icon: ImageVector,
    label: String,
    value: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colors.onSurface,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = value ?: "-",
                style = MaterialTheme.typography.body2
            )
        }
    }
}

@Composable
private fun AnimatedIcon(isError: Boolean) {
    val transition = rememberInfiniteTransition(label = "")
    val scale by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Icon(
        imageVector = if (isError) Icons.Filled.Error else Icons.Filled.CheckCircle,
        contentDescription = null,
        tint = if (isError) MaterialTheme.colors.error else MaterialTheme.colors.primary,
        modifier = Modifier
            .size(64.dp)
            .scale(scale)
    )
}

@Composable
fun DialogPassword(
    message: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(key1 = Unit) {
        isVisible = true
    }

    Dialog(onDismissRequest = onDismiss) {
        val transition = updateTransition(targetState = isVisible, label = "مgTransition")
        val scale by transition.animateFloat(
            transitionSpec = { tween(durationMillis = 500) }, label = "scale"
        ) { visible -> if (visible) 1f else 0.8f }

        val alpha by transition.animateFloat(
            transitionSpec = { tween(durationMillis = 500) }, label = "alpha"
        ) { visible -> if (visible) 1f else 0f }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .scale(scale)
                .alpha(alpha)
                .clip(RoundedCornerShape(16.dp)),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedIcon(isError = false)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "بروزرسانی اطلاعات حواله",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = message,
                    textAlign = TextAlign.Right,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(26.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("رمز عبور") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                if (isError) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            if (password.isNotBlank()) {
                                onConfirm(password)
                            } else {
                                isError = true
                                errorMessage = "رمز عبور نمی‌تواند خالی باشد"
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("تائید")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = {
                            isVisible = false
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("لغو")
                    }
                }
            }
        }
    }
}

@Composable
fun CargoInfoDetailsDialog(
    info: CargoInfo,
    viewModel: CargoViewModel,
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(16.dp)),
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Minimal Header
                MinimalHeader(info, onDismiss)

                // Scrollable Content with Expandable Sections
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    item {
                        ExpandableSection(
                            title = "اطلاعات اصلی",
                            icon = Icons.Default.Info,
                            initiallyExpanded = true
                        ) {
                            MainInfoContent(info)
                        }
                    }

                    item {
                        ExpandableSection(
                            title = "اطلاعات وزن",
                            icon = Icons.Default.Scale
                        ) {
                            WeightInfoContent(info)
                        }
                    }

                    item {
                        ExpandableSection(
                            title = "زمان‌ها",
                            icon = Icons.Default.Schedule
                        ) {
                            TimeInfoContent(info)
                        }
                    }

                    item {
                        ExpandableSection(
                            title = "اطلاعات تکمیلی",
                            icon = Icons.Default.MoreVert,
                            initiallyExpanded = true
                        ) {
                            AdditionalInfoContent(info)
                        }
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }

                // Minimal Footer
                MinimalFooter(
                    onDelete = { showDeleteConfirmation = true },
                    onDismiss = onDismiss
                )
            }
        }
    }

    if (showDeleteConfirmation) {
        DeleteDialog(
            onConfirm = {
                coroutineScope.launch {
                    val request = CargoInfoRequest(
                        trackingNumber = info.trackingNumber,
                        shipName = info.shipName,
                        loadingWarehouse = info.loadingWarehouse,
                        cargoType = info.cargoType,
                        shippingCompany = info.shippingCompany,
                        loadingQuotaNumber = info.loadingQuotaNumber
                    )
                    viewModel.deleteCargo(request, password)
                    snackbarHostState.showSnackbar("حواله با موفقیت حذف شد")
                    onDismiss()
                }
            },
            onDismiss = { showDeleteConfirmation = false },
            password = password,
            onPasswordChange = { password = it }
        )
    }
}

@Composable
private fun MinimalHeader(info: CargoInfo, onDismiss: () -> Unit) {
    val isLightTheme = MaterialTheme.colors.isLight

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = when {
            isLightTheme -> MaterialTheme.colors.primary.copy(alpha = 0.85f)
            else -> MaterialTheme.colors.surface
        },
        elevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = when {
                            isLightTheme -> listOf(
                                MaterialTheme.colors.primary,
                                MaterialTheme.colors.primary.copy(alpha = 0.95f)
                            )

                            else -> listOf(
                                MaterialTheme.colors.surface,
                                MaterialTheme.colors.surface.copy(alpha = 0.95f)
                            )
                        }
                    )
                )
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            // Close Button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(
                        color = when {
                            isLightTheme -> Color.White.copy(alpha = 0.2f)
                            else -> Color.White.copy(alpha = 0.1f)
                        },
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "بستن",
                    tint = when {
                        isLightTheme -> Color.White
                        else -> MaterialTheme.colors.onSurface
                    }
                )
            }

            Column(modifier = Modifier.padding(end = 56.dp)) {
                // Title Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = when {
                            isLightTheme -> Color.White.copy(alpha = 0.2f)
                            else -> MaterialTheme.colors.primary.copy(alpha = 0.1f)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp),
                            tint = when {
                                isLightTheme -> Color.White
                                else -> MaterialTheme.colors.primary
                            }
                        )
                    }

                    Text(
                        text = "حواله #${info.trackingNumber}",
                        style = MaterialTheme.typography.h6.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = when {
                            isLightTheme -> Color.White
                            else -> MaterialTheme.colors.onSurface
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Status Section
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusChip(
                        status = info.status,
                        isLightTheme = isLightTheme
                    )

                    if (info.confirm == "تائید شده") {
                        ConfirmationChip(isLightTheme = isLightTheme)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    status: String,
    isLightTheme: Boolean
) {
    val (backgroundColor, textColor, icon) = when (status) {
        "خروج" -> Triple(
            if (isLightTheme) Color(0xFF00C853) else Color(0xFF00E676),
            if (isLightTheme) Color.White else Color.Black,
            Icons.Default.LocalShipping
        )
        "ورود" -> Triple(
            if (isLightTheme) Color(0xFF2196F3) else Color(0xFF64B5F6),
            if (isLightTheme) Color.White else Color.Black,
            Icons.Default.AddChart
        )
        else -> Triple(
            if (isLightTheme) Color.Gray else Color.LightGray,
            if (isLightTheme) Color.White else Color.Black,
            Icons.Default.Info
        )
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor.copy(alpha = if (isLightTheme) 0.2f else 0.9f),
        border = BorderStroke(
            width = 1.dp,
            color = backgroundColor.copy(alpha = if (isLightTheme) 0.5f else 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = status,
                style = MaterialTheme.typography.caption.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = textColor
            )
        }
    }
}

@Composable
private fun ConfirmationChip(isLightTheme: Boolean) {
    val backgroundColor = if (isLightTheme) {
        Color(0xFF00C853)
    } else {
        Color(0xFF00E676)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor.copy(alpha = if (isLightTheme) 0.2f else 0.9f),
        border = BorderStroke(
            width = 1.dp,
            color = backgroundColor.copy(alpha = if (isLightTheme) 0.5f else 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isLightTheme) Color.White else Color.Black,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "تائید شده",
                style = MaterialTheme.typography.caption.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (isLightTheme) Color.White else Color.Black
            )
        }
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    icon: ImageVector,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = ""
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f)),
        color = MaterialTheme.colors.surface.copy(alpha = 0.7f)
    ) {
        Column {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                color = MaterialTheme.colors.primaryVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "بستن" else "باز کردن",
                        modifier = Modifier.rotate(rotationState)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.surface.copy(alpha = 0.95f))
                        .padding(12.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun CopyableInfoItem(
    label: String,
    value: String,
    context: Context = LocalContext.current,
    scope: CoroutineScope = rememberCoroutineScope()
) {
    var isCopied by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
        )

        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable {
                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(label, value)
                    clipboard.setPrimaryClip(clip)
                    isCopied = true
                    scope.launch {
                        delay(2000)
                        isCopied = false
                    }
                },
            color = if (isCopied) MaterialTheme.colors.primary.copy(alpha = 0.1f) else Color.Transparent
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.body2.copy(
                        fontWeight = FontWeight.Medium,
                        color = if (isCopied) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
                    )
                )

                AnimatedVisibility(
                    visible = isCopied,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colors.primary
                        )
                        Text(
                            text = "کپی شد",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MainInfoContent(info: CargoInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InfoItem("نام کشتی", info.shipName)
        InfoItem("نوع کالا", info.cargoType)
        CopyableInfoItem("شماره قبض باسکول", info.scaleReceiptNumber)
    }
}

@Composable
private fun WeightInfoContent(info: CargoInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        WeightItem(
            label = "وزن خالص",
            value = formatNumber(info.netWeight),
            valueColor = MaterialTheme.colors.primary
        )
        if (info.shortageWeight.isNotBlank()) {
            WeightItem(
                label = "کسری بار",
                value = formatNumber(info.shortageWeight),
                valueColor = Color.Red
            )
        }
        if (info.excessWeight.isNotBlank()) {
            WeightItem(
                label = "اضافه بار",
                value = formatNumber(info.excessWeight),
                valueColor = Color.Green
            )
        }
    }
}

@Composable
private fun TimeInfoContent(info: CargoInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InfoItem("زمان ورود", info.entryTime)
        info.exitTime?.let { InfoItem("زمان خروج", it) }
        info.exitDate?.let { InfoItem("تاریخ خروج", it) }
    }
}

@Composable
private fun AdditionalInfoContent(info: CargoInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InfoItem("تعداد نفرات", info.numberOfPeople)
        InfoItem("انبار", info.loadingWarehouse)
        InfoItem("شرکت باربری", info.shippingCompany)
        CopyableInfoItem("شماره کوتاژ", info.loadingQuotaNumber)
        StatusItem(
            "وضعیت تائید",
            if (info.confirm == "تائید شده") "تائید شده" else "در انتظار تائید",
            if (info.confirm == "تائید شده") Color.Green else Color.Gray
        )
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.body2,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun WeightItem(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = "$value کیلوگرم",
            style = MaterialTheme.typography.body2,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

@Composable
private fun StatusItem(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
        )
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = color.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
        ) {
            Text(
                text = value,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.caption,
                color = color
            )
        }
    }
}

@Composable
private fun MinimalFooter(onDelete: () -> Unit, onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("بستن")
            }
            Button(
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.error
                )
            ) {
                Text("حذف حواله")
            }
        }
    }
}

@Composable
private fun LoadingDialog(
    progress: Float,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loading_rotation"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "progress_animation"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(16.dp)),
            elevation = 8.dp,
            color = MaterialTheme.colors.surface
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(280.dp)
                    .padding(24.dp)
            ) {
                // Background Progress
                CircularProgressIndicator(
                    modifier = Modifier.size(200.dp),
                    progress = 1f,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                    strokeWidth = 12.dp
                )

                // Animated Progress
                CircularProgressIndicator(
                    modifier = Modifier.size(200.dp),
                    progress = animatedProgress,
                    color = MaterialTheme.colors.primary,
                    strokeWidth = 12.dp
                )

                // Rotating Loading Indicator
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(180.dp)
                        .rotate(rotation),
                    color = MaterialTheme.colors.secondary.copy(alpha = 0.2f),
                    strokeWidth = 3.dp
                )

                // Center Content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Progress Percentage
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.h4,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Loading Status Chip
                    Surface(
                        color = MaterialTheme.colors.secondary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "در حال بارگذاری",
                                style = MaterialTheme.typography.body1,
                                color = MaterialTheme.colors.onSurface
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            LoadingDots()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingDots() {
    val dotSize = 5.dp
    val delayUnit = 200

    @Composable
    fun Dot(delay: Int) {
        var visible by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            while (true) {
                delay(delay.toLong())
                visible = !visible
                delay(1000)
            }
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(
                animationSpec = tween(200, easing = LinearEasing)
            ),
            exit = fadeOut(
                animationSpec = tween(200, easing = LinearEasing)
            )
        ) {
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
            )
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Dot(0)
        Dot(delayUnit)
        Dot(delayUnit * 2)
    }
}

@Composable
private fun DeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "تائید حذف حواله",
                style = MaterialTheme.typography.h6,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column {
                Text("آیا از حذف این حواله اطمینان دارید؟")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("رمز عبور") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colors.error)
            ) {
                Text("حذف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}

private fun formatNumber(value: String): String {
    return try {
        // حذف کاما و تبدیل به عدد
        val number = value.replace(",", "").toDoubleOrNull() ?: return toEnglishNumbers(value)

        // فرمت‌بندی با کاما و اعداد انگلیسی
        DecimalFormat("#,###.##", /**/DecimalFormatSymbols(Locale.ENGLISH)).format(number)
    } catch (e: Exception) {
        toEnglishNumbers(value)
    }
}

private fun toEnglishNumbers(input: String): String {
    val persianNumbers = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    val arabicNumbers = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val englishNumbers = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

    var result = input

    // تبدیل اعداد فارسی به انگلیسی
    for (i in persianNumbers.indices) {
        result = result.replace(persianNumbers[i], englishNumbers[i])
    }

    // تبدیل اعداد عربی به انگلیسی
    for (i in arabicNumbers.indices) {
        result = result.replace(arabicNumbers[i], englishNumbers[i])
    }

    return result
}