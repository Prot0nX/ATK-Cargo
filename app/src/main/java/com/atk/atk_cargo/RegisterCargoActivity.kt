package com.atk.atk_cargo

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddChart
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
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
import androidx.lifecycle.lifecycleScope
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
import com.atk.atk_cargo.api.validateServerSession
import com.atk.atk_cargo.ui.theme.ATKCargoTheme
import com.atk.atk_cargo.ui.theme.Amber700
import com.atk.atk_cargo.ui.theme.BorderLight
import com.atk.atk_cargo.ui.theme.Gray300
import com.atk.atk_cargo.ui.theme.Gray500
import com.atk.atk_cargo.ui.theme.Gray600
import com.atk.atk_cargo.ui.theme.Green600
import com.atk.atk_cargo.ui.theme.Red500
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.roundToInt
import kotlin.math.sqrt
import androidx.compose.material3.MaterialTheme as MaterialTheme3
import androidx.compose.material3.Surface as Surface3
import androidx.compose.material3.Text as Text3

suspend fun handleQuotaEntry(
    quotaCode: String,
    currentInitialInfo: InitialInfo?,
    viewModel: CargoViewModel,
    activity: RegisterCargoActivity,
    snackbarHostState: SnackbarHostState,
    onQuotaChanged: () -> Unit = {}
) {
    if (currentInitialInfo == null) {
        snackbarHostState.showSnackbar("اطلاعات اولیه یافت نشد")
        return
    }

    try {
        // بررسی وجود کوتاژ جدید
        val response = viewModel.checkQuotaExistenceCargo(quotaCode, currentInitialInfo.shipName)
        
        if (response.exists && response.matchingQuotas.isNotEmpty()) {
            val selectedQuota = response.matchingQuotas.first()
            
            // بررسی اینکه کوتاژ متعلق به همان کشتی باشد
            if (selectedQuota.shipName == currentInitialInfo.shipName) {
                // ایجاد InitialInfo جدید
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
                
                // بروزرسانی اطلاعات در Activity
                activity.updateInitialInfo(newInitialInfo)
                
                // فراخوانی callback برای نمایش دیالوگ TopHeader
                onQuotaChanged()
                
                // نمایش پیام موفقیت
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

@Composable
fun DuplicateTrackingNumbersDialog(
    duplicateNumbers: List<String>,
    onDismiss: () -> Unit,
    onSearchTrackingNumber: (String) -> Unit = {}
) {
    var selectedTrackingNumber by remember { mutableStateOf<String?>(null) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .scale(
                    animateFloatAsState(
                        targetValue = if (isVisible) 1f else 0.8f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "scale"
                    ).value
                )
                .alpha(
                    animateFloatAsState(
                        targetValue = if (isVisible) 1f else 0f,
                        animationSpec = tween(300),
                        label = "alpha"
                    ).value
                ),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header مینیمال
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // آیکون هشدار مینیمال
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "حواله‌های تکراری",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${duplicateNumbers.size} شماره حواله تکراری",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // لیست مینیمال حواله‌های تکراری
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(duplicateNumbers) { trackingNumber ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedTrackingNumber = trackingNumber
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedTrackingNumber == trackingNumber) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                                tonalElevation = if (selectedTrackingNumber == trackingNumber) 2.dp else 0.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // نقطه رنگی برای نشان دادن تکراری بودن
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                MaterialTheme.colorScheme.error,
                                                CircleShape
                                            )
                                    )

                                    Text(
                                        text = trackingNumber,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = if (selectedTrackingNumber == trackingNumber) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                        modifier = Modifier.weight(1f)
                                    )

                                    // آیکون انتخاب
                                    if (selectedTrackingNumber == trackingNumber) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // دکمه‌های عملیات
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // دکمه جستجو (فقط اگر حواله‌ای انتخاب شده باشد)
                    if (selectedTrackingNumber != null) {
                        Button(
                            onClick = {
                                selectedTrackingNumber?.let { trackingNumber ->
                                    onSearchTrackingNumber(trackingNumber)
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "جستجو",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // دکمه بستن
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (selectedTrackingNumber != null) "بستن" else "متوجه شدم",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

class RegisterCargoActivity : ComponentActivity() {
    private lateinit var viewModel: CargoViewModel
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        result.contents?.let { barcode ->
            updateScaleReceiptNumber(barcode)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ATK-Log", "RegisterCargoActivity: onCreate started")

        // بررسی وضعیت ورود از سمت سرور
        val userPreferencesManager = UserPreferencesManager(this)
        
        lifecycleScope.launch {
            try {
                val result = validateServerSession(userPreferencesManager)
                result.fold(
                    onSuccess = {
                        // Session معتبر است، ادامه می‌دهد
                    },
                    onFailure = {
                        finish()
                        startActivity(Intent(this@RegisterCargoActivity, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        return@launch
                    }
                )
            } catch (e: Exception) {
                Log.e("RegisterCargoActivity", "خطا در بررسی وضعیت ورود: ${e.message}")
                finish()
                startActivity(Intent(this@RegisterCargoActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                return@launch
            }
        }

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
        val viewModelFactory = CargoViewModelFactory(reportsRepository, userPreferencesManager)
        viewModel = ViewModelProvider(this, viewModelFactory)[CargoViewModel::class.java]

        setContent {
            ATKCargoTheme(darkTheme = isSystemInDarkTheme()) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val cargoInfoList by viewModel.cargoInfoList.collectAsState()
                        val initialInfo by viewModel.initialInfo.collectAsState()
                        val resultMessage by viewModel.resultMessage.collectAsState()
                        val showAnimatedMessage by viewModel.showAnimatedMessage.collectAsState()
                        val messageType by viewModel.messageType.collectAsState()

                        RegisterCargoScreen(
                            initialInfo = initialInfo,
                            cargoInfoList = cargoInfoList,
                            resultMessage = resultMessage,
                            showAnimatedMessage = showAnimatedMessage,
                            messageType = messageType,
                            viewModel = viewModel,
                            activity = this
                        )

                        LaunchedEffect(initialInfoExtra) {
                            initialInfoExtra.let { info ->
                                viewModel.setInitialInfo(info)
                                viewModel.refreshCargoInfo()
                            }
                        }
                    }
                }
            }
        }
    }

    fun startBarcodeScanner() {
        Log.d("ATK-Log", "RegisterCargoActivity: Starting barcode scanner")
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
        val cleanedBarcode = barcode.replace(Regex("[^0-9]"), "")
        Log.d("ATK-Log", "RegisterCargoActivity: Barcode received: $barcode -> Cleaned: $cleanedBarcode")

        viewModel.updateScaleReceiptNumber(cleanedBarcode)
    }

    // متد بروزرسانی اطلاعات اولیه برای تغییر کوتاژ
    fun updateInitialInfo(newInitialInfo: InitialInfo) {
        viewModel.setInitialInfo(newInitialInfo)
        viewModel.refreshCargoInfo()
    }
}

private suspend fun recognizeTextFromImage(image: InputImage): String = suspendCancellableCoroutine { continuation ->
    val recognizer = TextRecognition.getClient(com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS)
    
    recognizer.process(image)
        .addOnSuccessListener { visionText ->
            // بازگرداندن متن استخراج شده
            continuation.resume(visionText.text)
        }
        .addOnFailureListener { e ->
            // در صورت خطا، رشته خالی برگردان
            continuation.resume("")
            e.printStackTrace()
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
    activity: RegisterCargoActivity,
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

    val filteredCargoInfoList by remember(
        cargoInfoList,
        searchQuery
    ) {
        derivedStateOf {
            val filtered = cargoInfoList.filter { cargoInfo ->
                val matches = cargoInfo.trackingNumber.contains(
                    searchQuery,
                    ignoreCase = true
                )
                if (searchQuery.isNotEmpty()) {
                    Log.d("RegisterCargoActivity_Log", "Checking ${cargoInfo.trackingNumber} against '$searchQuery': $matches")
                }
                matches
            }
            filtered
        }
    }

    // تفکیک حواله‌ها به دو دسته خروج نشده و خروج شده
    val (nonExitedCargos, exitedCargos) = filteredCargoInfoList.partition { it.status == "ورود" }

    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
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
                        containerColor = MaterialTheme.colorScheme.primary
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
                            tint = MaterialTheme.colorScheme.onPrimary,
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
                    tempTonnageAmount = initialInfo?.tempTonnageAmount
                )

                ShipInfoSection(
                    shipInfo = shipInfo,
                    isInfoVisible = isInfoVisible,
                    onToggleVisibility = { isInfoVisible = !isInfoVisible },
                    loadableTonnage = loadableTonnage,
                    loadableTrucks18Wheeler = loadableTrucks18Wheeler,
                    loadableTrucks10Wheeler = loadableTrucks10Wheeler
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
                                Log.d("ATK-Log", "RegisterCargoScreen: 'Exit Voucher' clicked for tracking: $trackingNumber")
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

                                // جلوگیری از نمایش TopHeaderInfoDialog پس از ثبت حواله
                                updateType = "cargo_submit"
                                focusManager.clearFocus()
                            }
                        }
                    )
                }

                // دکمه باز/بسته کردن فرم - طراحی جدید
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-8).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface3(
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { isFormExpanded = !isFormExpanded },
                        shape = CircleShape,
                        color = MaterialTheme3.colorScheme.surface,
                        shadowElevation = 1.dp,
                        border = BorderStroke(1.dp, MaterialTheme3.colorScheme.outline)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isFormExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isFormExpanded) "بستن فرم" else "باز کردن فرم",
                                tint = MaterialTheme3.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // بخش جستجو و دکمه بروزرسانی
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // فیلد جستجوی شماره حواله
                    Box(modifier = Modifier.weight(1f)) {
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { newValue ->
                                searchQuery = newValue
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(
                                    color = MaterialTheme3.colorScheme.surface,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme3.colorScheme.outline,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(start = 40.dp, end = 16.dp),
                            textStyle = MaterialTheme3.typography.bodyMedium.copy(
                                color = MaterialTheme3.colorScheme.onSurface,
                                textAlign = TextAlign.Left
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (searchQuery.isEmpty()) {
                                        Text3(
                                            text = "جستجوی شماره حواله",
                                            style = MaterialTheme3.typography.bodyMedium,
                                            color = MaterialTheme3.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        // آیکون جستجو سمت راست
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme3.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // دکمه بروزرسانی
                    var isRefreshing by remember { mutableStateOf(false) }
                    var rotationState by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
                    val rotation = animateFloatAsState(
                        targetValue = rotationState,
                        animationSpec = if (AnimationManager.areAnimationsEnabled()) tween(400, easing = FastOutSlowInEasing) else tween(0),
                        label = "rotation"
                    )

                    Surface3(
                        modifier = Modifier
                            .height(48.dp)
                            .clickable(enabled = !isRefreshing) {
                                if (!isRefreshing) {
                                    isRefreshing = true
                                    rotationState += 360f
                                    viewModel.refreshCargoInfo()
                                    coroutineScope.launch {
                                        delay(1500)
                                        isRefreshing = false
                                    }
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme3.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text3(
                                text = "بروزرسانی",
                                style = MaterialTheme3.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme3.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "بروزرسانی",
                                modifier = Modifier
                                    .size(18.dp)
                                    .rotate(rotation.value),
                                tint = MaterialTheme3.colorScheme.primary
                            )
                        }
                    }
                }

                // تب‌های ورود شده و خروج شده
                var selectedTab by remember { mutableIntStateOf(0) }
                
                // Tab Bar
                Surface3(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme3.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // تب ورود شده
                        Surface3(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = 0 },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedTab == 0) MaterialTheme3.colorScheme.surface else Color.Transparent,
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
                                Text3(
                                    text = "ورود شده",
                                    style = MaterialTheme3.typography.labelMedium,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == 0) Amber700 else Gray500
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Badge تعداد
                                Surface3(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selectedTab == 0) Amber700.copy(alpha = 0.1f) else Gray300
                                ) {
                                    Text3(
                                        text = "${nonExitedCargos.size}",
                                        style = MaterialTheme3.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTab == 0) Amber700 else Gray600,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // تب خروج شده
                        Surface3(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = 1 },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedTab == 1) MaterialTheme3.colorScheme.surface else Color.Transparent,
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
                                Text3(
                                    text = "خروج شده",
                                    style = MaterialTheme3.typography.labelMedium,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == 1) Green600 else Gray500
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Badge تعداد
                                Surface3(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selectedTab == 1) Green600.copy(alpha = 0.1f) else Gray300
                                ) {
                                    Text3(
                                        text = "${exitedCargos.size}",
                                        style = MaterialTheme3.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTab == 1) Green600 else Gray600,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // محتوای تب‌ها
                val currentItems = if (selectedTab == 0) {
                    nonExitedCargos.sortedByDescending { it.entryTime }
                } else {
                    exitedCargos.sortedByDescending { "${it.exitDate} ${it.exitTime}" }
                }
                
                // فیلتر بر اساس جستجو
                val filteredItems = if (searchQuery.isNotEmpty()) {
                    currentItems.filter { it.trackingNumber.contains(searchQuery, ignoreCase = true) }
                } else {
                    currentItems
                }

                // لیست آیتم‌ها
                Surface3(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme3.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme3.colorScheme.outline)
                ) {
                    if (filteredItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text3(
                                text = if (searchQuery.isNotEmpty()) "موردی یافت نشد" else "لیست خالی است",
                                style = MaterialTheme3.typography.bodyMedium,
                                color = MaterialTheme3.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
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
                                        color = MaterialTheme3.colorScheme.surfaceVariant,
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
                            // جلوگیری از نمایش TopHeaderInfoDialog پس از خروج حواله
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
                        // جلوگیری از نمایش TopHeaderInfoDialog پس از ثبت حواله
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

    // دیالوگ تأیید بارنامه تکراری
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

    // دیالوگ نمایش حواله‌های تکراری
    if (showDuplicateDialog) {
        DuplicateTrackingNumbersDialog(
            duplicateNumbers = duplicateTrackingNumbers,
            onDismiss = {
                viewModel.dismissDuplicateDialog()
            },
            onSearchTrackingNumber = { trackingNumber ->
                // تنظیم شماره حواله در فیلد جستجو
                searchQuery = trackingNumber
                // اسکرول به بالای لیست برای نمایش نتایج جستجو
                coroutineScope.launch {
                    listState.animateScrollToItem(0)
                }
            }
        )
    }
    
    // دیالوگ تغییر کوتاژ
    if (showQuotaEntryDialog) {
        QuotaEntryDialog(
            showDialog = true,
            onDismiss = { showQuotaEntryDialog = false },
            onConfirm = { quotaCode ->
                coroutineScope.launch {
                    handleQuotaEntry(quotaCode, initialInfo, viewModel, activity, snackbarHostState) {
                        // نمایش دیالوگ TopHeader پس از تغییر موفقیت‌آمیز کوتاژ
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

@Composable
fun QuotaWarningDialog(
    warning: WarningStatus,
    onDismiss: () -> Unit,
    viewModel: CargoViewModel,
) {
    val coroutineScope = rememberCoroutineScope()

    // انیمیشن‌های ورودی
    val scale = remember { androidx.compose.animation.core.Animatable(0.8f) }
    val alpha = remember { androidx.compose.animation.core.Animatable(0f) }

    // انیمیشن پالس برای آیکون هشدار
    val iconScale = remember { androidx.compose.animation.core.Animatable(1f) }

    // انیمیشن‌های ورودی
    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }

        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(300)
            )
        }

        // انیمیشن پالس برای آیکون هشدار
        launch {
            while (true) {
                iconScale.animateTo(
                    targetValue = 1.2f,
                    animationSpec = tween(600, easing = FastOutSlowInEasing)
                )
                iconScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(600, easing = FastOutSlowInEasing)
                )
                delay(1000)
            }
        }
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
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .padding(vertical = 8.dp)
                .scale(scale.value)
                .alpha(alpha.value),
            shape = RoundedCornerShape(24.dp),
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // آیکون هشدار با انیمیشن
                Box(
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                ) {
                    // دایره خارجی با گرادیان
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .scale(iconScale.value)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.0f)
                                    )
                                ),
                                shape = CircleShape
                            )
                    )

                    // دایره داخلی با آیکون
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.Center)
                            .background(
                                color = MaterialTheme.colorScheme.error,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // عنوان با طراحی جدید
                Text(
                    text = "هشدار محدودیت درصد کوتاژ",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // کارت اطلاعات کوتاژ
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "شماره کوتاژ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    warning.quotaNumber,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "${warning.percentage}%",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // نمایش نوار پیشرفت با طراحی بهبود یافته
                        Box(modifier = Modifier.fillMaxWidth()) {
                            // پس‌زمینه نوار پیشرفت
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                            )

                            // نوار پیشرفت اصلی
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth((warning.percentage / 100f).toFloat())
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                MaterialTheme.colorScheme.error
                                            )
                                        )
                                    )
                            )

                            // نقاط نشانگر در نوار پیشرفت
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (i in 1..4) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (warning.percentage >= i * 25) Color.White.copy(
                                                    alpha = 0.9f
                                                )
                                                else Color.Transparent
                                            )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // پیام هشدار
                Text(
                    text = "کوتاژ به حد نصاب مجاز رسیده است و امکان ثبت حواله جدید و خروج وجود ندارد.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Justify,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                // دکمه‌های عملیات
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // دکمه بستن
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Text(
                            "بستن",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    // دکمه تایید
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.toggleQuotaStatus(warning.quotaId ?: 0, warning.quotaNumber)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Text(
                            "متوجه شدم",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun processScannedQuota(scannedCode: String): String {
    val trimmedCode = scannedCode.trim()
    val processedCode = when {
        trimmedCode.contains("-") -> trimmedCode.split("-").last()
        trimmedCode.startsWith("990000") -> trimmedCode.substring(6)
        else -> trimmedCode
    }
    return if (processedCode.length > 4) {
        processedCode.takeLast(4)
    } else {
        processedCode
    }
}

@Composable
fun QuotaEntryDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    shipName: String,
    currentQuota: String,
    viewModel: CargoViewModel
) {
    var quotaEntry by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    val barcodeLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { scannedCode ->
            val processedCode = processScannedQuota(scannedCode)
            quotaEntry = processedCode
            if (isError) {
                isError = false
                errorMessage = ""
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

    // Reset state when dialog opens
    LaunchedEffect(showDialog) {
        if (showDialog) {
            quotaEntry = ""
            isError = false
            errorMessage = ""
            isLoading = false
            delay(150) // Slightly longer delay for better UX
            focusRequester.requestFocus()
        }
    }

    if (showDialog) {
        Dialog(
            onDismissRequest = { if (!isLoading) onDismiss() },
            properties = DialogProperties(
                dismissOnBackPress = !isLoading,
                dismissOnClickOutside = !isLoading,
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp)
                    .height(440.dp)
                    .animateContentSize(
                        animationSpec = tween(
                            durationMillis = 200,
                            easing = FastOutSlowInEasing
                        )
                    ),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                // ساختار جدید برای ثابت کردن دکمه‌ها در پایین
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // محتوای اصلی دیالوگ
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Enhanced Header Section with Icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ConfirmationNumber,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                                
                                Text(
                                    text = "تغییر کوتاژ",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            IconButton(
                                onClick = { if (!isLoading) onDismiss() },
                                enabled = !isLoading,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "بستن",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Enhanced Ship Info Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBoat,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "کشتی: $shipName",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "کوتاژ فعلی: $currentQuota",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        // Enhanced Input Section
                        DialogContent(
                            quotaEntry = quotaEntry,
                            onQuotaEntryChange = { newValue ->
                                if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                                    quotaEntry = newValue
                                    if (isError) {
                                        isError = false
                                        errorMessage = ""
                                    }
                                }
                            },
                            isError = isError,
                            errorMessage = errorMessage,
                            focusRequester = focusRequester,
                            isLoading = isLoading
                        )
                    }

                    // دکمه‌های ثابت در پایین - جدا از محتوای اصلی
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Primary Action Button
                        Button(
                            onClick = {
                                when {
                                    quotaEntry.isEmpty() -> {
                                        isError = true
                                        errorMessage = "لطفاً کوتاژ جدید را وارد کنید"
                                    }
                                    quotaEntry.length != 4 -> {
                                        isError = true
                                        errorMessage = "کوتاژ باید دقیقاً 4 رقم باشد"
                                    }
                                    quotaEntry == currentQuota -> {
                                        isError = true
                                        errorMessage = "کوتاژ جدید نمی‌تواند مشابه کوتاژ فعلی باشد"
                                    }
                                    else -> {
                                        // بهینه‌سازی: بررسی سریع قبل از شروع coroutine
                                        if (quotaEntry.all { it.isDigit() }) {
                                            isLoading = true
                                            coroutineScope.launch {
                                                try {
                                                    // بررسی وجود کوتاژ قبل از تأیید
                                                    val response = viewModel.checkQuotaExistenceCargo(quotaEntry, shipName)
                                                    
                                                    if (response.exists && response.matchingQuotas.isNotEmpty()) {
                                                        val selectedQuota = response.matchingQuotas.first()
                                                        
                                                        // بررسی اینکه کوتاژ متعلق به همان کشتی باشد
                                                        if (selectedQuota.shipName == shipName) {
                                                            // بررسی وضعیت فعال بودن کوتاژ
                                                            if (selectedQuota.isActive) {
                                                                // کوتاژ معتبر و فعال است، ادامه دهید
                                                                onConfirm(quotaEntry)
                                                            } else {
                                                                // کوتاژ غیرفعال است
                                                                isError = true
                                                                errorMessage = "کوتاژ $quotaEntry در حال حاضر غیرفعال است و قابل انتخاب نیست"
                                                            }
                                                        } else {
                                                            // کوتاژ متعلق به کشتی دیگری است
                                                            isError = true
                                                            errorMessage = "کوتاژ $quotaEntry متعلق به کشتی ${selectedQuota.shipName} است"
                                                        }
                                                    } else {
                                                        // کوتاژ وجود ندارد
                                                        isError = true
                                                        errorMessage = "کوتاژ $quotaEntry برای کشتی $shipName یافت نشد"
                                                    }
                                                } catch (e: Exception) {
                                                    // خطا در بررسی کوتاژ
                                                    isError = true
                                                    errorMessage = "خطا در بررسی کوتاژ: ${e.message}"
                                                } finally {
                                                    isLoading = false
                                                }
                                            }
                                        } else {
                                            // بهینه‌سازی: نمایش فوری خطا برای ورودی غیرعددی
                                            isError = true
                                            errorMessage = "کوتاژ باید فقط شامل اعداد باشد"
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoading) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Text(
                                        text = "پردازش...",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "تأیید",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }

                        // Glassmorphism Scan Button
                        Surface(
                            onClick = { startBarcodeScanner() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            ),
                            shadowElevation = 0.dp,
                            tonalElevation = 0.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                            ),
                                            start = Offset(0f, 0f),
                                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isLoading) 
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        else 
                                            MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "اسکن",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isLoading) 
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        else 
                                            MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogContent(
    quotaEntry: String,
    onQuotaEntryChange: (String) -> Unit,
    isError: Boolean,
    errorMessage: String,
    focusRequester: FocusRequester,
    isLoading: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Input Field with Enhanced Design
        OutlinedTextField(
            value = quotaEntry,
            onValueChange = onQuotaEntryChange,
            label = { 
                Text(
                    "کوتاژ جدید",
                    style = MaterialTheme.typography.bodyMedium
                ) 
            },
            placeholder = { 
                Text(
                    "مثال: 1234",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                ) 
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.ConfirmationNumber,
                    contentDescription = null,
                    tint = if (isError) MaterialTheme.colorScheme.error 
                          else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (quotaEntry.isNotEmpty()) {
                    IconButton(
                        onClick = { onQuotaEntryChange("") },
                        enabled = !isLoading,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "پاک کردن",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { 
                    // Handle done action if needed
                }
            ),
            singleLine = true,
            isError = isError,
            enabled = !isLoading,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) MaterialTheme.colorScheme.error 
                                   else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                     else MaterialTheme.colorScheme.outline,
                focusedLabelColor = if (isError) MaterialTheme.colorScheme.error 
                                  else MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error,
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp)
        )

        // Character Counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Helper Text
            Text(
                text = "کوتاژ باید 4 رقم باشد",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            
            // Character Counter
            Text(
                text = "${quotaEntry.length}/4",
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    quotaEntry.length == 4 -> MaterialTheme.colorScheme.primary
                    quotaEntry.length > 4 -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                }
            )
        }

        // Enhanced Error Display - بهینه‌سازی شده
        AnimatedVisibility(
            visible = isError && errorMessage.isNotEmpty(),
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 150, // سریع‌تر
                    easing = FastOutSlowInEasing
                )
            ) + expandVertically(
                animationSpec = tween(
                    durationMillis = 150,
                    easing = FastOutSlowInEasing
                )
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = 100, // خروج سریع‌تر
                    easing = LinearEasing
                )
            ) + shrinkVertically(
                animationSpec = tween(
                    durationMillis = 100,
                    easing = LinearEasing
                )
            )
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    1.dp, 
                    MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Progress Indicator for Loading State - بهینه‌سازی شده
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 150,
                    easing = FastOutSlowInEasing
                )
            ) + expandVertically(
                animationSpec = tween(
                    durationMillis = 150,
                    easing = FastOutSlowInEasing
                )
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = 100,
                    easing = LinearEasing
                )
            ) + shrinkVertically(
                animationSpec = tween(
                    durationMillis = 100,
                    easing = LinearEasing
                )
            )
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "در حال بررسی کوتاژ...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun NetWeightDialog(
    scaleReceiptNumber: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var netWeight by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(true) }
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

    fun validateAndConfirm() {
        val weight = netWeight.toIntOrNull()
        if (weight != null && weight in 1000..60000) {
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
            val dialogWidth = maxWidth * 0.92f
            AnimatedVisibility(
                visible = true,
                enter = dialogEnterTransition,
                exit = dialogExitTransition
            ) {
                Surface(
                    modifier = Modifier
                        .width(dialogWidth)
                        .clip(RoundedCornerShape(20.dp))
                        .align(Alignment.Center),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // هدر مینیمال
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Scale,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    "ثبت وزن خالص",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

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
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // نمایش شماره قبض باسکول در کارت مینیمال
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        "شماره قبض باسکول",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        fontSize = 11.sp
                                    )

                                    Text(
                                        scaleReceiptNumber,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // فیلد ورودی وزن خالص مینیمال
                        OutlinedTextField(
                            value = netWeight,
                            onValueChange = {
                                netWeight = it
                                isError = false
                            },
                            label = {
                                Text("وزن خالص (کیلوگرم)")
                            },
                            placeholder = {
                                Text(
                                    "مثال: 25000",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Scale,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                validateAndConfirm()
                            }),
                            isError = isError,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        if (isError) {
                            Text(
                                "وزن خالص باید بین 1000 تا 60000 کیلوگرم باشد",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // بخش انتخاب حالت اسکن مینیمال
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Camera,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "اسکن هوشمند تناژ خالص",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // دکمه شروع اسکن
                                Button(
                                    onClick = { 
                                        showCamera = true 
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp),
                                    colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                                    shape = RoundedCornerShape(10.dp),
                                    elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 3.dp,
                                pressedElevation = 6.dp
                            )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Camera,
                                            contentDescription = "شروع اسکن",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            "اسکن تناژ خالص",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // دکمه تایید مینیمال
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                validateAndConfirm()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 3.dp,
                                pressedElevation = 6.dp
                            )
                        ) {
                            Text(
                                "ثبت وزن خالص",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        // راهنمای کاربر مینیمال
                        Text(
                            "برای اسکن خودکار وزن، یکی از حالت‌های اسکن را انتخاب کنید",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }

    // باز شدن خودکار دوربین در حالت اسکن سریع
    if (showCamera) {
        Dialog(onDismissRequest = {
            showCamera = false
        }) {
            EnhancedCameraPreview(
                // همیشه ML_KIT
                onImageCaptured = { image, detectedWeight ->
                    showCamera = false
                    coroutineScope.launch {
                        try {
                            // Use the detected weight directly if available
                            if (!detectedWeight.isNullOrEmpty()) {
                                // Check if within valid range
                                val weightValue = detectedWeight.toDoubleOrNull()
                                if (weightValue != null && weightValue in 1000.0..60000.0) {
                                    netWeight = detectedWeight
                                } else {
                                    // Fallback to image processing if weight is invalid
                                    val preprocessedImage = preprocessImage(image)
                                    val recognizedText = recognizeTextFromImage(preprocessedImage)
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
                                val recognizedText = recognizeTextFromImage(preprocessedImage)
                                recognizedWeight = extractNumber(recognizedText)

                                if (recognizedWeight.isNotEmpty()) {
                                    netWeight = recognizedWeight
                                } else {
                                    isError = true
                                }
                            }
                        } catch (_: Exception) {
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
    // مجموعه‌ای از الگوها با اولویت‌بندی برای تشخیص عدد تناژ
    val patterns = listOf(
        // الگوی 1: دنبال عبارت‌های مخصوص وزن خالص با فرمت‌های مختلف
        Regex("""(?:وزن\s*خالص|خالص|NET\s*WEIGHT|NET)[:\s=]*(\d{1,3}(?:[,. ]\d{3})+)""", RegexOption.IGNORE_CASE),
        
        // الگوی 2: اعداد با فرمت خاص که معمولاً در قبض‌های باسکول استفاده می‌شود
        Regex("""(\d{2}[,. ]\d{3}[,. ]\d{3})"""),
        Regex("""(\d{2,3}[,. ]\d{3})"""),
        
        // الگوی 3: عبارت‌های دیگر مرتبط با وزن در قبض باسکول
        Regex("""وزن(?:\s+با)?(?:\s+بار)?:?\s*(\d{1,3}(?:[,. ]\d{3})+)""", RegexOption.IGNORE_CASE),
        
        // الگوی 4: اعداد 5 یا 6 رقمی که معمولاً می‌توانند وزن باشند
        Regex("""(\b\d{5,6}\b)""")
    )

    // پیش‌پردازش متن برای بهبود تشخیص
    val normalizedText = text
        .replace('\n', ' ')            // تبدیل خط جدید به فاصله
        .replace(Regex("""[\u200C\u200F\u202A-\u202E]"""), "")  // حذف کاراکترهای کنترلی یونیکد
    
    // جستجو با الگوهای مختلف براساس اولویت
    for (pattern in patterns) {
        val matches = pattern.findAll(normalizedText)
        val candidates = matches.mapNotNull { match -> 
            try {
                // پاکسازی عدد از کاراکترهای غیرعددی
                val cleanNumber = match.groupValues[1].replace(Regex("""\D"""), "")
                if (cleanNumber.length >= 4) {
                    cleanNumber.toDouble()
                } else {
                    null
                }
            } catch (_: Exception) {
                null 
            }
        }.filter { 
            // فیلتر کردن اعداد در محدوده منطقی وزن (بین 1000 و 60000 کیلوگرم)
            it in 1000.0..60000.0
        }.toList()
        
        if (candidates.isNotEmpty()) {
            // انتخاب محتمل‌ترین عدد براساس معیارهای وزن معمول
            val mostLikely = when {
                // اعداد نزدیک به میانگین وزن کامیون‌های معمول ارجحیت دارند
                candidates.any { it in 20000.0..30000.0 } -> 
                    candidates.filter { it in 20000.0..30000.0 }.average()
                    
                // در غیر این صورت بزرگترین عدد معتبر را انتخاب کن
                else -> candidates.maxOrNull() ?: 0.0
            }
            
            return String.format("%d", mostLikely.roundToInt())
        }
    }
    
    // روش نهایی: استخراج همه اعداد و فیلتر براساس محدوده منطقی
    val allNumbersRegex = Regex("""(\d{1,3}(?:[,. ]\d{3})*|\d{4,6})""")
    val allMatches = allNumbersRegex.findAll(normalizedText)
    
    val weightCandidates = allMatches.mapNotNull { matchResult ->
        try {
            val cleaned = matchResult.value.replace(Regex("""\D"""), "")
            if (cleaned.length >= 4) {
                cleaned.toDouble()
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }.filter { it in 1000.0..60000.0 }.toList()
    
    // اگر اعدادی پیدا شدند، محتمل‌ترین را انتخاب کن
    return weightCandidates.maxOrNull()?.let {
        String.format("%d", it.roundToInt())
    } ?: ""
}

fun preprocessImage(imageProxy: ImageProxy): InputImage {
    val bitmap = imageProxy.toBitmap()
    val width = bitmap.width
    val height = bitmap.height

    // ایجاد بیت‌مپ برای پردازش
    val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(outputBitmap)
    
    // مرحله 1: افزایش کنتراست و شارپنس برای بهبود خوانایی متن
    val enhancementMatrix = ColorMatrix(floatArrayOf(
        2.5f, 0f, 0f, 0f, -50f,    // افزایش کنتراست کانال قرمز
        0f, 2.5f, 0f, 0f, -50f,    // افزایش کنتراست کانال سبز
        0f, 0f, 2.5f, 0f, -50f,    // افزایش کنتراست کانال آبی
        0f, 0f, 0f, 1.2f, 0f       // افزایش کنتراست آلفا
    ))
    
    val enhancementPaint = Paint().apply {
        colorFilter = ColorMatrixColorFilter(enhancementMatrix)
    }
    
    // اعمال فیلتر بهبود کنتراست
    canvas.drawBitmap(bitmap, 0f, 0f, enhancementPaint)
    
    // مرحله 2: تبدیل به تصویر باینری با آستانه‌گذاری محلی
    val pixels = IntArray(width * height)
    outputBitmap.getPixels(pixels, 0, width, 0, 0, width, height)
    
    // استفاده از الگوریتم آستانه‌گذاری سازگار
    adaptiveThresholding(pixels, width, height)
    
    // مرحله 3: حذف نویز با فیلتر میانه
    medianFilter(pixels, width, height)
    
    // مرحله 4: تقویت لبه‌ها برای بهبود تشخیص اعداد
    enhanceEdges(pixels, width, height)
    
    // اعمال پیکسل‌های پردازش شده روی تصویر خروجی
    outputBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    
    return InputImage.fromBitmap(outputBitmap, imageProxy.imageInfo.rotationDegrees)
}

private fun adaptiveThresholding(pixels: IntArray, width: Int, height: Int) {
    val windowSize = 15  // اندازه پنجره برای محاسبه آستانه محلی
    val c = 10          // ثابت کاهش از میانگین محلی
    
    for (y in 0 until height) {
        for (x in 0 until width) {
            val pos = y * width + x
            
            // محاسبه میانگین در پنجره محلی
            var sum = 0
            var count = 0
            
            for (wy in maxOf(0, y - windowSize / 2) until minOf(height, y + windowSize / 2 + 1)) {
                for (wx in maxOf(0, x - windowSize / 2) until minOf(width, x + windowSize / 2 + 1)) {
                    val pixel = pixels[wy * width + wx]
                    val gray = (pixel and 0xFF) + ((pixel shr 8) and 0xFF) + ((pixel shr 16) and 0xFF)
                    sum += gray / 3
                    count++
                }
            }
            
            val threshold = if (count > 0) sum / count - c else 128
            
            // اعمال آستانه محلی
            val pixel = pixels[pos]
            val gray = ((pixel and 0xFF) + ((pixel shr 8) and 0xFF) + ((pixel shr 16) and 0xFF)) / 3
            
            pixels[pos] = if (gray > threshold) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
        }
    }
}

private fun medianFilter(pixels: IntArray, width: Int, height: Int) {
    val output = pixels.copyOf()
    val windowSize = 3
    val window = IntArray(windowSize * windowSize)
    
    for (y in 1 until height - 1) {
        for (x in 1 until width - 1) {
            var idx = 0
            
            // جمع‌آوری مقادیر پیکسل‌های همسایه
            for (wy in -1..1) {
                for (wx in -1..1) {
                    window[idx++] = pixels[(y + wy) * width + (x + wx)]
                }
            }
            
            // مرتب‌سازی و انتخاب مقدار میانه
            window.sort()
            output[y * width + x] = window[windowSize * windowSize / 2]
        }
    }
    
    // کپی نتایج به آرایه اصلی
    for (i in pixels.indices) {
        pixels[i] = output[i]
    }
}

private fun enhanceEdges(pixels: IntArray, width: Int, height: Int) {
    val output = pixels.copyOf()
    val sobelX = arrayOf(
        intArrayOf(-1, 0, 1),
        intArrayOf(-2, 0, 2),
        intArrayOf(-1, 0, 1)
    )
    
    val sobelY = arrayOf(
        intArrayOf(1, 2, 1),
        intArrayOf(0, 0, 0),
        intArrayOf(-1, -2, -1)
    )
    
    for (y in 1 until height - 1) {
        for (x in 1 until width - 1) {
            var sumX = 0
            var sumY = 0
            
            for (wy in -1..1) {
                for (wx in -1..1) {
                    val pixel = pixels[(y + wy) * width + (x + wx)]
                    val gray = if (pixel == 0xFFFFFFFF.toInt()) 255 else 0
                    
                    sumX += gray * sobelX[wy + 1][wx + 1]
                    sumY += gray * sobelY[wy + 1][wx + 1]
                }
            }
            
            val magnitude = minOf(255, sqrt((sumX * sumX + sumY * sumY).toDouble()).toInt())
            
            // تقویت لبه‌ها اگر مقدار بیش از آستانه باشد
            if (magnitude > 30) {
                output[y * width + x] = 0xFF000000.toInt()  // لبه‌ها سیاه می‌شوند
            }
        }
    }
    
    // ادغام لبه‌های تقویت شده با تصویر اصلی
    for (i in pixels.indices) {
        // اگر پیکسل در تصویر اصلی سیاه است یا در خروجی لبه تشخیص داده شده، آن را سیاه نگه دار
        if (pixels[i] == 0xFF000000.toInt() || output[i] == 0xFF000000.toInt()) {
            pixels[i] = 0xFF000000.toInt()
        }
    }
}

@Composable
fun ScannerGuideOverlay(
    scanAreaSize: Float = 0.7f,
    guideColor: Color = Color.Green.copy(alpha = 0.7f),
    guideThickness: Float = 2f,
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
    private val context: Context,
    private val onNumbersDetected: (List<String>, String) -> Unit,
    private val onAnalysisStateChanged: ((Boolean) -> Unit)? = null
) : ImageAnalysis.Analyzer {
    
    private val textRecognizer = TextRecognition.getClient(com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS)
    private var lastDetectionTime = 0L
    private val detectionCooldown = 1500L // کاهش زمان انتظار برای سرعت بیشتر
    private var isProcessingWithAI = false
    
    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        
        if (isProcessingWithAI || currentTime - lastDetectionTime < detectionCooldown) {
            imageProxy.close()
            return
        }
        
        lastDetectionTime = currentTime
        isProcessingWithAI = true
        onAnalysisStateChanged?.invoke(true)
        
        processImage(imageProxy)
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImage(imageProxy: ImageProxy) {
        val inputImage = InputImage.fromMediaImage(
            imageProxy.image!!,
            imageProxy.imageInfo.rotationDegrees
        )
        
        textRecognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val detectedText = visionText.text
                val numbers = extractNetWeights(detectedText)
                
                isProcessingWithAI = false
                onAnalysisStateChanged?.invoke(false)
                
                if (numbers.isNotEmpty()) {
                    val bestNumber = numbers.first()
                    onNumbersDetected(numbers, bestNumber)
                } else {
                    onNumbersDetected(emptyList(), "")
                }
            }
            .addOnFailureListener { e ->
                isProcessingWithAI = false
                onAnalysisStateChanged?.invoke(false)
                onNumbersDetected(emptyList(), "")
                e.printStackTrace()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun extractNetWeights(text: String): List<String> {
        val result = mutableListOf<String>()
        
        val patterns = listOf(
            Regex("(?:وزن\\s*خالص|خالص)[\\s:=]*(\\d{1,2}[,.]\\d{3})"),
            Regex("(?:وزن\\s*خالص|خالص)[\\s:=]*(\\d{5,6})"),
            Regex("\\b(\\d{5,6})\\b"),
            Regex("\\b(\\d{1,2}[,.]\\d{3})\\b")
        )
        
        for (pattern in patterns) {
            val matches = pattern.findAll(text)
            matches.forEach { matchResult ->
                val numberStr = matchResult.groupValues[1].replace(Regex("[,.]"), "")
                val number = numberStr.toIntOrNull()
                if (number != null && number in 1000..60000) {
                    result.add(number.toString())
                }
            }
            
            if (result.isNotEmpty()) break
        }
        
        return result
    }
}

@Composable
fun ErrorHandlingCargoInfoRow(
    info: CargoInfo,
    onRowClick: (CargoInfo) -> Unit,
    duplicateTrackingNumbers: List<String> = emptyList()
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
            duplicateTrackingNumbers = duplicateTrackingNumbers, // انتقال پارامتر به کامپوننت فرزند
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
    searchQuery: String = "",
    onItemClick: (CargoInfo) -> Unit,
    duplicateTrackingNumbers: List<String> = emptyList(),
    isExpanded: Boolean = initiallyExpanded,
    onExpandedChange: (Boolean) -> Unit = {}
) {
    LaunchedEffect(searchQuery, items) {
        if (searchQuery.isNotEmpty()) {
            val hasMatchingItem = items.any { 
                it.trackingNumber.contains(searchQuery, ignoreCase = true)
            }
            if (hasMatchingItem) {
                onExpandedChange(true)
            }
        }
    }
    
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = "rotation"
    )

    Surface3(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .animateContentSize(),
        tonalElevation = 0.5.dp,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = when (title) {
                "ورود شده" -> MaterialTheme3.colorScheme.secondary.copy(alpha = 0.6f)
                "خروج شده" -> MaterialTheme3.colorScheme.primary.copy(alpha = 0.6f)
                else -> MaterialTheme3.colorScheme.outline.copy(alpha = 0.3f)
            }
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // هدر مینیمال
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandedChange(!isExpanded) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // آیکون وضعیت
                    Surface3(
                        shape = CircleShape,
                        color = when (title) {
                            "ورود شده" -> MaterialTheme3.colorScheme.primaryContainer
                            "خروج شده" -> MaterialTheme3.colorScheme.secondaryContainer
                            else -> MaterialTheme3.colorScheme.surfaceVariant
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = when (title) {
                                    "ورود شده" -> Icons.Default.AddChart
                                    "خروج شده" -> Icons.Default.LocalShipping
                                    else -> Icons.Default.Receipt
                                },
                                contentDescription = null,
                                tint = when (title) {
                                    "ورود شده" -> MaterialTheme3.colorScheme.onPrimaryContainer
                                    "خروج شده" -> MaterialTheme3.colorScheme.onSecondaryContainer
                                    else -> MaterialTheme3.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    // عنوان و تعداد
                    Column {
                        Text3(
                            text = title,
                            style = MaterialTheme3.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme3.colorScheme.onSurface
                        )
                        Text3(
                            text = "${items.size} حواله",
                            style = MaterialTheme3.typography.labelSmall,
                            color = MaterialTheme3.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // آیکون گسترش
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "بستن" else "باز کردن",
                    tint = MaterialTheme3.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(rotationAngle)
                )
            }

            // محتوای قابل گسترش
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(items) { info ->
                        ErrorHandlingCargoInfoRow(
                            info = info,
                            onRowClick = onItemClick,
                            duplicateTrackingNumbers = duplicateTrackingNumbers // انتقال پارامتر به کامپوننت فرزند
                        )
                    }
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
    totalNetWeight: Float,
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
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "آمار حواله‌های خروجی",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
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
    icon: ImageVector,
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
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = NumberFormat.getNumberInstance(Locale("en", "US")).format(animatedCount),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
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
    isSubmitting: Boolean,
    onSubmit: () -> Unit,
) {
    val isDuplicate = remember(trackingNumber, cargoInfoList) {
        trackingNumber.isNotBlank() && cargoInfoList.any { it.trackingNumber == trackingNumber }
    }
    val isTrackingNumberValid = remember(trackingNumber) {
        trackingNumber.isEmpty() || trackingNumber.all { it.isDigit() }
    }
    val currentCargo = remember(trackingNumber, cargoInfoList) {
        if (isDuplicate) cargoInfoList.find { it.trackingNumber == trackingNumber } else null
    }
    val canEditWeights = remember(currentCargo) {
        currentCargo?.let {
            when {
                it.status == "ورود" && it.confirm == "تائید شده" -> true
                it.confirm == "در انتظار تائید" -> false
                it.status == "خروج" -> false
                else -> false
            }
        } == true
    }
    val isSubmitEnabled = remember(
        trackingNumber,
        numberOfPeople,
        isDuplicate,
        shortageWeight,
        excessWeight,
        canEditWeights,
        isSubmitting,
        isTrackingNumberValid
    ) {
        when {
            isSubmitting -> false
            trackingNumber.isBlank() -> false
            !isTrackingNumberValid -> false
            !isDuplicate -> (numberOfPeople.toIntOrNull() ?: 1) > 0
            !canEditWeights -> false
            else -> {
                val hasShortage = shortageWeight.isNotBlank() && shortageWeight != "0"
                val hasExcess = excessWeight.isNotBlank() && excessWeight != "0"
                hasShortage || hasExcess
            }
        }
    }
    val messageAlpha = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(isDuplicate) {
        if (isDuplicate) {
            messageAlpha.snapTo(0f)
            messageAlpha.animateTo(1f, animationSpec = tween(300))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // عنوان فرم - سمت راست با آیکون
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ListAlt,
                contentDescription = null,
                tint = MaterialTheme3.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text3(
                text = if (isDuplicate) "ویرایش حواله" else "ثبت حواله جدید",
                style = MaterialTheme3.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme3.colorScheme.primary
            )
        }

        // کارت فرم اصلی
        Surface3(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme3.colorScheme.surface,
            shadowElevation = 1.dp,
            border = BorderStroke(1.dp, MaterialTheme3.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // بخش اول: شماره حواله و تعداد نفرات
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // فیلد شماره حواله
                        Box(modifier = Modifier.weight(1f)) {
                            BasicTextField(
                                value = trackingNumber,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                        onTrackingNumberChange(newValue)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .background(
                                        color = MaterialTheme3.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if ((isDuplicate && !canEditWeights) || !isTrackingNumberValid)
                                            MaterialTheme3.colorScheme.error
                                        else
                                            BorderLight,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(start = 40.dp, end = 12.dp),
                                    textStyle = MaterialTheme3.typography.bodyMedium.copy(
                                    color = MaterialTheme3.colorScheme.onSurface,
                                    textAlign = TextAlign.Left
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (trackingNumber.isEmpty()) {
                                            Text3(
                                                text = "شماره حواله",
                                                style = MaterialTheme3.typography.bodyMedium,
                                                color = MaterialTheme3.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                            // آیکون سمت راست
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Numbers,
                                    contentDescription = null,
                                    tint = MaterialTheme3.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // فیلد تعداد نفرات - کنترل افزایشی/کاهشی
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .background(
                                    color = if (!isDuplicate) MaterialTheme3.colorScheme.surfaceVariant else MaterialTheme3.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme3.colorScheme.outline,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val currentValue = numberOfPeople.toIntOrNull() ?: 1

                                // دکمه افزایش
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clickable(enabled = !isDuplicate && currentValue < 5) {
                                            if (currentValue < 5) {
                                                onNumberOfPeopleChange((currentValue + 1).toString())
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "افزایش",
                                        tint = if (!isDuplicate && currentValue < 5) MaterialTheme3.colorScheme.onSurfaceVariant else MaterialTheme3.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // مقدار و آیکون
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (!isDuplicate) MaterialTheme3.colorScheme.onSurfaceVariant else MaterialTheme3.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text3(
                                        text = numberOfPeople.ifEmpty { "1" },
                                        style = MaterialTheme3.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isDuplicate) MaterialTheme3.colorScheme.onSurface else MaterialTheme3.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }

                                // دکمه کاهش
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clickable(enabled = !isDuplicate && currentValue > 1) {
                                            if (currentValue > 1) {
                                                onNumberOfPeopleChange((currentValue - 1).toString())
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "کاهش",
                                        tint = if (!isDuplicate && currentValue > 1) MaterialTheme3.colorScheme.onSurfaceVariant else MaterialTheme3.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // بخش کسری بار و اضافه بار - فقط برای حواله‌های تایید شده و آماده خروج
                    AnimatedVisibility(
                        visible = isCargoConfirmed && !isCargoExited,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // کسری بار
                            Box(modifier = Modifier.weight(1f)) {
                                BasicTextField(
                                    value = shortageWeight,
                                    onValueChange = { newValue ->
                                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                            onShortageWeightChange(newValue)
                                        }
                                    },
                                    enabled = canEditWeights,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .background(
                                            color = if (canEditWeights) MaterialTheme3.colorScheme.surfaceVariant else MaterialTheme3.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme3.colorScheme.outline,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(start = 40.dp, end = 12.dp),
                                    textStyle = MaterialTheme3.typography.bodyMedium.copy(
                                        color = if (canEditWeights) MaterialTheme3.colorScheme.onSurface else MaterialTheme3.colorScheme.onSurface.copy(alpha = 0.5f),
                                        textAlign = TextAlign.Left
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (shortageWeight.isEmpty()) {
                                                Text3(
                                                    text = "کسری بار",
                                                    style = MaterialTheme3.typography.bodyMedium,
                                                    color = if (canEditWeights) MaterialTheme3.colorScheme.onSurfaceVariant else MaterialTheme3.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                                // آیکون سمت راست
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .padding(start = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                        contentDescription = null,
                                        tint = if (canEditWeights) MaterialTheme3.colorScheme.error else MaterialTheme3.colorScheme.error.copy(alpha = 0.5f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // اضافه بار
                            Box(modifier = Modifier.weight(1f)) {
                                BasicTextField(
                                    value = excessWeight,
                                    onValueChange = { newValue ->
                                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                            onExcessWeightChange(newValue)
                                        }
                                    },
                                    enabled = canEditWeights,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .background(
                                            color = if (canEditWeights) MaterialTheme3.colorScheme.surfaceVariant else MaterialTheme3.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme3.colorScheme.outline,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(start = 40.dp, end = 12.dp),
                                    textStyle = MaterialTheme3.typography.bodyMedium.copy(
                                        color = if (canEditWeights) MaterialTheme3.colorScheme.onSurface else MaterialTheme3.colorScheme.onSurface.copy(alpha = 0.5f),
                                        textAlign = TextAlign.Left
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (excessWeight.isEmpty()) {
                                                Text3(
                                                    text = "اضافه بار",
                                                    style = MaterialTheme3.typography.bodyMedium,
                                                    color = if (canEditWeights) MaterialTheme3.colorScheme.onSurfaceVariant else MaterialTheme3.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                                // آیکون سمت راست
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .padding(start = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = if (canEditWeights) MaterialTheme3.colorScheme.tertiary else MaterialTheme3.colorScheme.tertiary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // دکمه‌های ثبت و خروج
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // دکمه ثبت حواله
                        Surface3(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clickable(enabled = isSubmitEnabled, onClick = onSubmit),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSubmitEnabled) {
                                if (isDuplicate) Amber700 else Green600
                            } else MaterialTheme3.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = null,
                                    tint = if (isSubmitEnabled) Color.White else MaterialTheme3.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text3(
                                    text = if (isDuplicate) "ثبت تغییرات" else "ثبت حواله",
                                    style = MaterialTheme3.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSubmitEnabled) Color.White else MaterialTheme3.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }

                        // دکمه خروج حواله
                        Surface3(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clickable(
                                    enabled = isCargoConfirmed && !isCargoExited,
                                    onClick = onScanBarcode
                                ),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCargoConfirmed && !isCargoExited) Red500 else MaterialTheme3.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = if (isCargoConfirmed && !isCargoExited) Color.White else MaterialTheme3.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text3(
                                    text = "خروج حواله",
                                    style = MaterialTheme3.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCargoConfirmed && !isCargoExited) Color.White else MaterialTheme3.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // نمایش پیام خطای اعتبارسنجی شماره حواله
        AnimatedVisibility(
            visible = !isTrackingNumberValid && trackingNumber.isNotBlank(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "فیلد شماره حواله فقط می‌تواند شامل اعداد باشد. لطفاً مقدار وارد شده را اصلاح نمایید.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // نمایش پیام وضعیت با انیمیشن
        AnimatedVisibility(
            visible = isDuplicate,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            val (messageText, messageColor) = when {
                currentCargo?.status == "خروج" -> 
                    Pair("این حواله قبلاً خروج شده و قابل تغییر نیست!", MaterialTheme.colorScheme.error)
                currentCargo?.confirm == "در انتظار تائید" -> 
                    Pair("این حواله هنوز تائید نشده و قابل ویرایش نیست!", MaterialTheme.colorScheme.error)
                canEditWeights -> 
                    Pair("امکان ثبت کسری/اضافه بار یا خروج حواله وجود دارد!", MaterialTheme.colorScheme.primary)
                else -> 
                    Pair("این حواله هنوز تائید نشده و قابل ویرایش نیست!", MaterialTheme.colorScheme.error)
            }

            Surface(
                color = messageColor.copy(alpha = 0.08f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .alpha(messageAlpha.value)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (messageColor == MaterialTheme.colorScheme.error)
                            Icons.Default.Info else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = messageColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = messageText,
                        style = MaterialTheme.typography.labelSmall,
                        color = messageColor
                    )
                }
            }
        }

        // نمایش شماره قبض باسکول
        AnimatedVisibility(
            visible = scaleReceiptNumber.isNotBlank(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "قبض باسکول: $scaleReceiptNumber",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun MessageDialog(
    message: String,
    type: MessageType,
    visible: Boolean,
    onDismiss: () -> Unit,
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

    val (backgroundColor, iconTint, titleText) = when (type) {
        MessageType.SUCCESS -> Triple(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.primary,
            "عملیات موفق"
        )
        MessageType.WARNING -> Triple(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.tertiary,
            "هشدار"
        )
        MessageType.ERROR -> Triple(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.error,
            "خطا"
        )
    }

    if (visible) {
        Dialog(
            onDismissRequest = { /* Prevent dismissal on outside click */ },
            properties = DialogProperties(
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = backgroundColor,
                tonalElevation = 8.dp
            ) {
                AnimatedVisibility(
                    visible = true,
                    enter = dialogEnterTransition,
                    exit = dialogExitTransition
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header with colored circle background
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(iconTint.copy(alpha = 0.1f), CircleShape)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LottieAnimation(
                                composition = composition,
                                progress = { lottieAnimatable.progress },
                                modifier = Modifier.size(80.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Title
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = iconTint
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Message with card background
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = iconTint.copy(alpha = 0.05f),
                            border = BorderStroke(1.dp, iconTint.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Justify
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Button with gradient background
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = iconTint,
                                contentColor = when (type) {
                                    MessageType.SUCCESS -> MaterialTheme.colorScheme.onPrimary
                                    MessageType.WARNING -> MaterialTheme.colorScheme.onTertiary
                                    MessageType.ERROR -> MaterialTheme.colorScheme.onError
                                }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                "متوجه شدم",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShipInfoSection(
    shipInfo: ShipInfo,
    isInfoVisible: Boolean,
    onToggleVisibility: () -> Unit,
    loadableTonnage: String,
    loadableTrucks18Wheeler: String,
    loadableTrucks10Wheeler: String,
) {
    val loadedPercentage = remember(shipInfo.cargoWeight, shipInfo.totalNetWeight) {
        try {
            val totalWeight = shipInfo.cargoWeight.replace(",", "").toFloatOrNull() ?: 0f
            val loadedWeight = shipInfo.totalNetWeight.replace(",", "").toFloatOrNull() ?: 0f
            if (totalWeight > 0) {
                String.format(Locale.ENGLISH, "%.1f", (loadedWeight / totalWeight) * 100)
            } else "0.0"
        } catch (_: Exception) {
            "0.0"
        }
    }

    Surface3(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
            .animateContentSize(),
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // هدر مینیمال
            TopHeader(
                onToggle = onToggleVisibility,
                loadedPercentage = loadedPercentage.toFloat(),
                shipName = shipInfo.shipName,
                cargoType = shipInfo.cargoType,
                quotaNumber = shipInfo.loadingQuotaNumber,
                loadableTonnage = loadableTonnage,
                loadableTrucks18Wheeler = loadableTrucks18Wheeler,
                loadableTrucks10Wheeler = loadableTrucks10Wheeler,
                isExpanded = isInfoVisible,
                tempTonnageStatus = shipInfo.tempTonnageStatus,
                tempTonnageAmount = shipInfo.tempTonnageAmount
            )

            // محتوای قابل گسترش
            AnimatedVisibility(
                visible = isInfoVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ExpandedContent(
                    shipInfo = shipInfo
                )
            }
        }
    }
}

@Composable
private fun TopHeader(
    onToggle: () -> Unit,
    loadedPercentage: Float,
    shipName: String,
    cargoType: String,
    quotaNumber: String,
    loadableTonnage: String,
    loadableTrucks18Wheeler: String,
    loadableTrucks10Wheeler: String,
    isExpanded: Boolean,
    tempTonnageStatus: Boolean = false,
    tempTonnageAmount: Float? = null,
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = "rotation"
    )

    val tonnageValue = loadableTonnage.replace(",", "").toDoubleOrNull() ?: 0.0
    val tonnageColor = if (tonnageValue < 0) MaterialTheme3.colorScheme.error else MaterialTheme3.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
    ) {
        Surface3(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme3.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // سمت چپ: تناژ مجاز و اطلاعات کامیون‌ها
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // تناژ مجاز با آیکون
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text3(
                                text = "تناژ مجاز",
                                style = MaterialTheme3.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme3.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "بستن" else "باز کردن",
                                tint = MaterialTheme3.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(16.dp)
                                    .rotate(rotationAngle)
                            )
                        }

                        // مقدار تناژ
                        val displayText = if (tempTonnageStatus && tempTonnageAmount != null) {
                            val formattedTempTonnage = DecimalFormat("#,###").format(tempTonnageAmount.toInt())
                            "$loadableTonnage ($formattedTempTonnage)"
                        } else {
                            loadableTonnage
                        }

                        Text3(
                            text = displayText,
                            style = MaterialTheme3.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = tonnageColor
                        )

                        // اطلاعات کامیون‌ها
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text3(
                                text = "$loadableTrucks10Wheeler = 10چرخ",
                                style = MaterialTheme3.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = tonnageColor
                            )
                            Text3(
                                text = "|",
                                style = MaterialTheme3.typography.labelLarge,
                                color = MaterialTheme3.colorScheme.onSurfaceVariant
                            )
                            Text3(
                                text = "$loadableTrucks18Wheeler = 18چرخ",
                                style = MaterialTheme3.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = tonnageColor
                            )
                        }
                    }

                    // نام کشتی، نوع کالا و شماره کوتاژ
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // نام کشتی | نوع کالا
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (cargoType.isNotBlank()) {
                                Text3(
                                    text = cargoType,
                                    style = MaterialTheme3.typography.titleSmall,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme3.colorScheme.onSurface
                                )
                                Text3(
                                    text = "|",
                                    style = MaterialTheme3.typography.titleSmall,
                                    color = MaterialTheme3.colorScheme.onSurface
                                )
                            }
                            Text3(
                                text = shipName,
                                style = MaterialTheme3.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme3.colorScheme.onSurface,
                                letterSpacing = (-0.5).sp
                            )
                        }
                        Text3(
                            text = quotaNumber,
                            style = MaterialTheme3.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme3.colorScheme.primary
                        )
                    }
                }
            }
        }

        // نوار پیشرفت مینیمال
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(MaterialTheme3.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(loadedPercentage / 100f)
                    .height(2.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme3.colorScheme.primary,
                                MaterialTheme3.colorScheme.secondary
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun ExpandedContent(
    shipInfo: ShipInfo,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // آمار سریع در دو ستون
        QuickStatsGrid(shipInfo)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // اطلاعات تفصیلی در دو ستون
        DetailedInfoGrid(shipInfo)
    }
}

@Composable
private fun QuickStatsGrid(shipInfo: ShipInfo) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            icon = Icons.Default.Scale,
            value = formatNumber(shipInfo.cargoWeight),
            label = "تناژ کل",
            color = MaterialTheme3.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Default.BarChart,
            value = formatNumber(shipInfo.remainingWeight),
            label = "باقیمانده کل",
            color = MaterialTheme3.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            icon = Icons.Default.LocalShipping,
            value = toEnglishNumbers(shipInfo.totalServices),
            label = "حواله‌ها",
            color = MaterialTheme3.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Default.AddChart,
            value = formatNumber(shipInfo.totalNetWeight),
            label = "بارگیری شده",
            color = MaterialTheme3.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface3(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.08f),
        tonalElevation = 0.5.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text3(
                    text = value,
                    style = MaterialTheme3.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme3.colorScheme.onSurface
                )
                Text3(
                    text = label,
                    style = MaterialTheme3.typography.bodySmall,
                    color = MaterialTheme3.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetailedInfoGrid(shipInfo: ShipInfo) {
    Surface3(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme3.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        tonalElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme3.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text3(
                    text = "اطلاعات تفصیلی",
                    style = MaterialTheme3.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme3.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // دو ستونه
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DetailInfoItem("انبار", shipInfo.loadingWarehouse, Icons.Default.Warehouse)
                    DetailInfoItem("نوع کالا", shipInfo.cargoType, Icons.Default.Category)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DetailInfoItem("شرکت باربری", shipInfo.shippingCompany, Icons.Default.Business)
                    DetailInfoItem("کوتاژ", shipInfo.loadingQuotaNumber, Icons.Default.ConfirmationNumber)
                }
            }
        }
    }
}

@Composable
private fun DetailInfoItem(
    label: String,
    value: String,
    icon: ImageVector,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme3.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text3(
                text = label,
                style = MaterialTheme3.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme3.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text3(
            text = value,
            style = MaterialTheme3.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme3.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CargoInfoRow(
    info: CargoInfo,
    onRowClick: (CargoInfo) -> Unit,
    onError: (String) -> Unit,
    duplicateTrackingNumbers: List<String> = emptyList()
) {
    val formattedNetWeight = remember(info.netWeight) {
        try {
            DecimalFormat("#,###").format(info.netWeight.toDoubleOrNull() ?: 0)
        } catch (_: Exception) {
            onError("Invalid netWeight: ${info.netWeight}")
            "0"
        }
    }
    val isDuplicate = duplicateTrackingNumbers.contains(info.trackingNumber)
    val isExited = info.status == "خروج"
    val isConfirmed = info.confirm == "تائید شده"
    val iconColor = if (isExited) MaterialTheme3.colorScheme.primary else MaterialTheme3.colorScheme.secondary
    val iconBgColor = if (isExited) 
        MaterialTheme3.colorScheme.primaryContainer.copy(alpha = 0.5f) 
    else 
        MaterialTheme3.colorScheme.secondaryContainer.copy(alpha = 0.5f)
    val displayDate = if (isExited && info.exitDate != null) info.exitDate else ""
    val displayTime = if (isExited && info.exitTime != null) info.exitTime else info.entryTime

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRowClick(info) }
            .padding(12.dp)
    ) {
        // هدر: آیکون، شماره حواله و تاریخ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // آیکون و شماره حواله
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // آیکون دایره‌ای
                Box {
                    Surface3(
                        shape = CircleShape,
                        color = if (isDuplicate) MaterialTheme3.colorScheme.tertiaryContainer.copy(alpha = 0.5f) else iconBgColor,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = if (isDuplicate) Icons.Default.ContentCopy 
                                    else if (isExited) Icons.Default.LocalShipping 
                                    else Icons.AutoMirrored.Filled.Assignment,
                                contentDescription = null,
                                tint = if (isDuplicate) MaterialTheme3.colorScheme.tertiary else iconColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    // نشانگر تأیید/عدم تأیید برای حواله‌های ورودی
                    if (!isExited) {
                        Surface3(
                            shape = CircleShape,
                            color = if (isConfirmed) Color(0xFF4CAF50) else Color(0xFFFF9800),
                            modifier = Modifier
                                .size(14.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp),
                            border = BorderStroke(2.dp, MaterialTheme3.colorScheme.surface)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = if (isConfirmed) Icons.Default.Check else Icons.Default.Schedule,
                                    contentDescription = if (isConfirmed) "تأیید شده" else "در انتظار تأیید",
                                    tint = Color.White,
                                    modifier = Modifier.size(9.dp)
                                )
                            }
                        }
                    }
                }
                
                // شماره حواله
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text3(
                            text = "شماره حواله",
                            style = MaterialTheme3.typography.labelSmall,
                            color = MaterialTheme3.colorScheme.onSurfaceVariant
                        )
                        
                        // Badge وضعیت تأیید برای حواله‌های ورودی
                        if (!isExited) {
                            Surface3(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isConfirmed) 
                                    Color(0xFF4CAF50).copy(alpha = 0.15f) 
                                else 
                                    Color(0xFFFF9800).copy(alpha = 0.15f)
                            ) {
                                Text3(
                                    text = if (isConfirmed) "تأیید شده" else "در انتظار",
                                    style = MaterialTheme3.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isConfirmed) Color(0xFF2E7D32) else Color(0xFFE65100),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text3(
                        text = info.trackingNumber,
                        style = MaterialTheme3.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme3.colorScheme.onSurface
                    )
                }
            }

            // تاریخ
            Surface3(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme3.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme3.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme3.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Text3(
                        text = displayDate ?: "",
                        style = MaterialTheme3.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme3.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // گرید اطلاعات: تعداد نفرات | وزن خالص | ساعت
        Surface3(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme3.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, MaterialTheme3.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // تعداد نفرات
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = MaterialTheme3.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text3(
                            text = info.numberOfPeople,
                            style = MaterialTheme3.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme3.colorScheme.onSurface
                        )
                        Text3(
                            text = "نفر",
                            style = MaterialTheme3.typography.labelSmall,
                            color = MaterialTheme3.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // جداکننده
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(MaterialTheme3.colorScheme.outline)
                )

                // وزن خالص
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Scale,
                        contentDescription = null,
                        tint = MaterialTheme3.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.size(17.dp)
                    )
                    Text3(
                        text = formattedNetWeight,
                        style = MaterialTheme3.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme3.colorScheme.onSurface
                    )
                }

                // جداکننده
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(MaterialTheme3.colorScheme.outline)
                )

                // ساعت
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme3.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                    Text3(
                        text = displayTime,
                        style = MaterialTheme3.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme3.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedIcon() {
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
        imageVector = Icons.Filled.CheckCircle,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .size(64.dp)
            .scale(scale)
    )
}

@Composable
fun DialogPassword(
    message: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
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
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedIcon()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "بروزرسانی اطلاعات حواله",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = message,
                    textAlign = TextAlign.Right,
                    style = MaterialTheme.typography.bodyLarge,
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
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
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
    onDismiss: () -> Unit,
    onUpdateTypeChange: (String) -> Unit = {}
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current
    
    // تب انتخاب شده: 0 = اطلاعات اصلی، 1 = وزن، 2 = زمان و تاریخ
    var selectedTab by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.75f)
                .clip(RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 50.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // هدر با آیکون دایره‌ای
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                shape = CircleShape
                            )
                            .border(
                                width = 8.dp,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PriorityHigh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // عنوان
                    Text(
                        text = "جزئیات حواله",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // بخش شماره حواله
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "شماره حواله: ${info.trackingNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.align(Alignment.Center)
                            )
                            
                            // دکمه کپی
                            Surface(
                                onClick = {
                                    coroutineScope.launch {
                                        clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("tracking", info.trackingNumber)))
                                        snackbarHostState.showSnackbar("شماره حواله کپی شد")
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .size(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "کپی",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // کارت محتوا با تب‌ها
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // تب‌ها
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // تب اطلاعات اصلی
                                    DetailTabButton(
                                        title = "اطلاعات اصلی",
                                        icon = Icons.Default.Info,
                                        isSelected = selectedTab == 0,
                                        onClick = { selectedTab = 0 },
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    // تب وزن
                                    DetailTabButton(
                                        title = "وزن",
                                        icon = Icons.Default.Scale,
                                        isSelected = selectedTab == 1,
                                        onClick = { selectedTab = 1 },
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    // تب زمان و تاریخ
                                    DetailTabButton(
                                        title = "زمان و تاریخ",
                                        icon = Icons.Default.Schedule,
                                        isSelected = selectedTab == 2,
                                        onClick = { selectedTab = 2 },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            
                            // محتوای تب‌ها
                            AnimatedContent(
                                targetState = selectedTab,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(300)) togetherWith
                                            fadeOut(animationSpec = tween(300))
                                },
                                label = "tab_content"
                            ) { tab ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(16.dp)
                                ) {
                                    when (tab) {
                                        0 -> MainInfoTabContent(
                                            info = info,
                                            onCopyScaleReceipt = {
                                                coroutineScope.launch {
                                                    clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("quota", info.scaleReceiptNumber)))
                                                    snackbarHostState.showSnackbar("شماره قبض باسکول کپی شد")
                                                }
                                            }
                                        )
                                        1 -> WeightInfoTabContent(info = info)
                                        2 -> TimeInfoTabContent(info = info)
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // دکمه‌های پایین
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // دکمه بستن
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1.2f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 2.dp,
                                pressedElevation = 4.dp
                            )
                        ) {
                            Text(
                                text = "بستن",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // دکمه حذف
                        Button(
                            onClick = { showDeleteConfirmation = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "حذف",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        DeleteDialog(
            onConfirm = {
                coroutineScope.launch {
                    val request = CargoInfoRequest(
                        id = info.id ?: 0
                    )
                    viewModel.deleteCargo(request, password)
                    onUpdateTypeChange("cargo_delete")
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
private fun DetailTabButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent,
        label = "tab_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "tab_content"
    )
    
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
        
        // خط زیر تب فعال
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                    )
            )
        }
    }
}

@Composable
private fun MainInfoTabContent(
    info: CargoInfo,
    onCopyScaleReceipt: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        DetailInfoRow(label = "نام کشتی", value = info.shipName, isUppercase = true)
        DetailInfoRow(label = "انبار بارگیری", value = info.loadingWarehouse)
        DetailInfoRow(label = "نوع کالا", value = info.cargoType)
        DetailInfoRow(label = "شرکت حمل و نقل", value = info.shippingCompany)
        DetailInfoRow(
            label = "شماره قبض باسکول",
            value = info.scaleReceiptNumber,
            showCopyIcon = true,
            onCopy = onCopyScaleReceipt,
            isLast = true
        )
    }
}

@Composable
private fun WeightInfoTabContent(info: CargoInfo) {
    val formattedNetWeight = remember(info.netWeight) {
        try {
            val weight = info.netWeight.replace(",", "").toDoubleOrNull() ?: 0.0
            DecimalFormat("#,###").format(weight.toLong())
        } catch (e: Exception) {
            info.netWeight
        }
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        DetailInfoRow(label = "وزن خالص", value = "$formattedNetWeight کیلوگرم", isLast = true)
    }
}

@Composable
private fun TimeInfoTabContent(info: CargoInfo) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        DetailInfoRow(label = "زمان ورود", value = info.entryTime ?: "--")
        DetailInfoRow(label = "زمان خروج", value = info.exitTime ?: "--")
        DetailInfoRow(label = "تاریخ خروج", value = info.exitDate ?: "--", isLast = true)
    }
}

@Composable
private fun DetailInfoRow(
    label: String,
    value: String,
    isUppercase: Boolean = false,
    showCopyIcon: Boolean = false,
    onCopy: (() -> Unit)? = null,
    isLast: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onCopy != null) {
                        Modifier.clickable { onCopy() }
                    } else {
                        Modifier
                    }
                )
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showCopyIcon) {
                    Surface(
                        onClick = { onCopy?.invoke() },
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "کپی",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                
                Text(
                    text = if (isUppercase) value.uppercase() else value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        if (!isLast) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                thickness = 1.dp
            )
        }
    }
}

@Composable
private fun DeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_warning))
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
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
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
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header with colored circle background
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), CircleShape)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LottieAnimation(
                            composition = composition,
                            progress = { lottieAnimatable.progress },
                            modifier = Modifier.size(80.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = "حذف حواله",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Message with card background
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = "آیا از حذف این حواله اطمینان دارید؟ این عملیات غیرقابل بازگشت است.",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Justify
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        label = { Text("رمز عبور") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.error,
                            focusedLabelColor = MaterialTheme.colorScheme.error
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons with improved styling
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
                            onClick = onConfirm,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            ),
                            enabled = password.isNotEmpty()
                        ) {
                            Text(
                                text = "تایید حذف",
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

private fun formatNumber(value: String): String {
    return try {
        // حذف کاما و تبدیل به عدد
        val number = value.replace(",", "").toDoubleOrNull() ?: return toEnglishNumbers(value)

        // فرمت‌بندی با کاما و اعداد انگلیسی
        DecimalFormat("#,###.##", /**/DecimalFormatSymbols(Locale.ENGLISH)).format(number)
    } catch (_: Exception) {
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

@Composable
fun EnhancedCameraPreview(
    onImageCaptured: (ImageProxy, String?) -> Unit,
    onError: (ImageCaptureException) -> Unit,
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var preview: Preview? by remember { mutableStateOf(null) }
    var camera: Camera? by remember { mutableStateOf(null) }
    val lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    val executor = ContextCompat.getMainExecutor(context)

    // Weight detection state
    var detectedNumber by remember { mutableStateOf<String?>(null) }
    var isValidWeight by remember { mutableStateOf(false) }
    var processingActive by remember { mutableStateOf(true) }

    // AI Analysis state
    var isAIAnalyzing by remember { mutableStateOf(false) }
    var analysisSource by remember { mutableStateOf<String?>(null) }

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
                        .setTargetRotation(previewView.display.rotation)
                        .setTargetResolution(android.util.Size(1440, 1080)) // Using explicit resolution instead of aspect ratio
                        .build()
                        .also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()

                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetResolution(android.util.Size(1440, 1080)) // Using 4:3 ratio with explicit resolution
                        .build()

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setTargetResolution(android.util.Size(1280, 960)) // 4:3 aspect ratio using explicit resolution
                        .build()
                        .apply {
                            setAnalyzer(executor, EnhancedNumberAnalyzer(
                                context = context,
                                onNumbersDetected = { extractedNumbers, bestEstimate ->
                                    if (processingActive) {

                                        if (bestEstimate.isNotEmpty()) {
                                            detectedNumber = bestEstimate
                                            val weight = bestEstimate.toDoubleOrNull()
                                            isValidWeight = weight != null && weight in 1000.0..60000.0

                                            // تنظیم منبع تحلیل
                                            analysisSource = "ML_KIT"
                                        } else {
                                            // اگر نتیجه‌ای نیست، state ها را پاک کن
                                            detectedNumber = null
                                            analysisSource = null
                                        }
                                    }
                                },
                                onAnalysisStateChanged = { isAnalyzing ->
                                    // Callback برای وضعیت تحلیل AI
                                    isAIAnalyzing = isAnalyzing
                                }
                            ))
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
                        hasTorch = camera?.cameraInfo?.hasFlashUnit() == true
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

        // Header با اطلاعات حالت اسکن
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.Black.copy(alpha = 0.8f),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // نمایش حالت اسکن فعال
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val (icon, title, color) = Triple("📱", "اسکن داخلی", Color(0xFF4CAF50))

                    Text(
                        text = icon,
                        fontSize = 20.sp
                    )
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // نمایش وضعیت تحلیل
                    if (isAIAnalyzing) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = color,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }

                // راهنمای کاربر
                Text(
                    text = "قبض باسکول را در کادر قرار دهید - پردازش سریع",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Display detected weight با انیمیشن نرم
        AnimatedVisibility(
            visible = detectedNumber != null,
            enter = fadeIn(animationSpec = tween(400)) + slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(400)
            ),
            exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(300)
            )
        ) {
            detectedNumber?.let { number ->
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isValidWeight)
                        Color(0xFF4CAF50).copy(alpha = 0.95f)
                    else
                        Color(0xFFE57373).copy(alpha = 0.95f),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // نمایش منبع تحلیل
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "📱 ML",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            Icon(
                                imageVector = if (isValidWeight) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${NumberFormat.getNumberInstance(Locale("en", "US")).format(number.toDoubleOrNull() ?: 0)} کیلوگرم",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        if (!isValidWeight && number.isNotEmpty()) {
                            Text(
                                text = "وزن باید بین 1,000 تا 60,000 کیلوگرم باشد",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // نمایش وضعیت تحلیل AI
        AnimatedVisibility(
            visible = isAIAnalyzing,
            enter = fadeIn(animationSpec = tween(300)) + expandIn(
                expandFrom = Alignment.Center,
                animationSpec = tween(300)
            ),
            exit = fadeOut(animationSpec = tween(300)) + shrinkOut(
                shrinkTowards = Alignment.Center,
                animationSpec = tween(300)
            )
        ) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.8f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = Color(0xFF2196F3),
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "تحلیل هوشمند در حال انجام...",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Capture button
        var isCapturing by remember { mutableStateOf(false) }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .size(72.dp)
                .clickable(enabled = !isCapturing) {
                    // بررسی اینکه آیا عددی تشخیص داده شده است یا خیر
                    if (detectedNumber == null) {
                        // نمایش پیام خطا با استفاده از Toast
                        Toast.makeText(context, "هنوز عددی تشخیص داده نشده است!", Toast.LENGTH_SHORT).show()
                        return@clickable
                    }

                    processingActive = false
                    isCapturing = true

                    imageCapture?.takePicture(
                        executor,
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                onImageCaptured(image, detectedNumber)
                                isCapturing = false
                            }

                            override fun onError(exception: ImageCaptureException) {
                                onError(exception)
                                isCapturing = false
                            }
                        }
                    )
                },
            shape = CircleShape,
            color = if (detectedNumber != null) Color.White.copy(alpha = 0.9f) else Color.Gray.copy(alpha = 0.5f), // تغییر رنگ دکمه
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (isCapturing) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = Color.Black,
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "گرفتن عکس",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Flashlight button
        if (hasTorch) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 24.dp)
                    .size(56.dp)
                    .clickable {
                        isTorchOn = !isTorchOn
                        camera?.cameraControl?.enableTorch(isTorchOn)
                    },
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.6f),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
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
    }

    // Release resources when leaving the screen
    DisposableEffect(lifecycleOwner) {
        onDispose {
            processingActive = false
        }
    }
}

@Composable
fun DuplicateConfirmationDialog(
    message: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.lottie_warning)
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

    val warningColor = MaterialTheme.colorScheme.tertiary
    val backgroundColor = MaterialTheme.colorScheme.surface
    val cardBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = backgroundColor,
            tonalElevation = 8.dp
        ) {
            AnimatedVisibility(
                visible = true,
                enter = dialogEnterTransition,
                exit = dialogExitTransition
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header with colored circle background
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(warningColor.copy(alpha = 0.1f), CircleShape)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LottieAnimation(
                            composition = composition,
                            progress = { lottieAnimatable.progress },
                            modifier = Modifier.size(80.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = "حواله تکراری",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Message with card background
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = cardBackgroundColor,
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Justify
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Confirm Button
                        Button(
                            onClick = onConfirm,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 12.dp
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text(
                                "ثبت حواله",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Cancel Button
                        OutlinedButton(
                            onClick = onCancel,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text(
                                "انصراف",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
