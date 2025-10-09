package com.atk.atk_cargo

import android.annotation.SuppressLint
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
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
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
import com.atk.atk_cargo.ml.LocalOCRProcessor
import com.atk.atk_cargo.ui.theme.ATKCargoTheme
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
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

enum class ScanMode {
    LOCAL_AI_SCAN,  // پردازش پیشرفته با ML Kit
    ML_KIT_SCAN     // ML Kit ساده
}

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

@Composable
fun TopHeaderInfoDialog(
    loadingQuotaNumber: Int,
    loadableTonnage: String,
    tempTonnageStatus: Boolean,
    tempTonnageAmount: Float?,
    loadableTrucks18Wheeler: String,
    loadableTrucks10Wheeler: String,
    onDismiss: () -> Unit
) {
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

    val backgroundColor = MaterialTheme.colorScheme.surface
    val iconTint = MaterialTheme.colorScheme.primary

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
                        Icon(
                            imageVector = Icons.Default.DirectionsBoat,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(68.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = "اطلاعات کشتی",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = iconTint
                    )
                    
                    if (loadingQuotaNumber > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "شماره کوتاژ: $loadingQuotaNumber",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Information content with card background
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = iconTint.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, iconTint.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // تناژ مجاز
                            InfoRow(
                                icon = Icons.Default.Scale,
                                label = "تناژ مجاز",
                                value = if (loadableTonnage.isNotEmpty()) "$loadableTonnage تن" else "نامشخص",
                                iconColor = iconTint
                            )
                            
                            // تناژ موقت
                            if (tempTonnageStatus && tempTonnageAmount != null) {
                                InfoRow(
                                    icon = Icons.Default.Schedule,
                                    label = "تناژ موقت",
                                    value = "${tempTonnageAmount.toInt()} تن",
                                    iconColor = MaterialTheme.colorScheme.secondary
                                )
                            }
                            
                            HorizontalDivider(
                                color = iconTint.copy(alpha = 0.2f),
                                thickness = 1.dp
                            )
                            
                            // کامیون‌های 18 چرخ
                            InfoRow(
                                icon = Icons.Default.LocalShipping,
                                label = "تعداد کامیون مجاز 18 چرخ",
                                value = if (loadableTrucks18Wheeler.isNotEmpty()) "$loadableTrucks18Wheeler دستگاه" else "0 دستگاه",
                                iconColor = MaterialTheme.colorScheme.tertiary
                            )
                            
                            // کامیون‌های 10 چرخ
                            InfoRow(
                                icon = Icons.Default.LocalShipping,
                                label = "تعداد کامیون مجاز 10 چرخ",
                                value = if (loadableTrucks10Wheeler.isNotEmpty()) "$loadableTrucks10Wheeler دستگاه" else "0 دستگاه",
                                iconColor = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Button with gradient background
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = iconTint,
                            contentColor = MaterialTheme.colorScheme.onPrimary
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

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
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
        
        // بررسی اعتبار قبض باسکول با پیام‌های خطای مناسب
        if (cleanedBarcode.length != 8) {
            showErrorMessage("قبض باسکول باید 8 رقمی باشد. لطفاً قبض باسکول صحیح را اسکن کنید!")
            return
        }
        
        val firstTwoDigits = cleanedBarcode.substring(0, 2)
        if (firstTwoDigits != "42" && firstTwoDigits != "43" && firstTwoDigits != "44") {
            showErrorMessage("قبض باسکول باید با 42، 43 یا 44 شروع شود. لطفاً قبض باسکول صحیح را اسکن کنید!")
            return
        }
        
        // اگر به اینجا برسیم، قبض باسکول معتبر است
        viewModel.updateScaleReceiptNumber(cleanedBarcode)
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // متد بروزرسانی اطلاعات اولیه برای تغییر کوتاژ
    fun updateInitialInfo(newInitialInfo: InitialInfo) {
        viewModel.setInitialInfo(newInitialInfo)
        viewModel.refreshCargoInfo()
    }
}

private suspend fun recognizeTextFromImage(image: InputImage): String = suspendCancellableCoroutine { continuation ->
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
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
    
    // State for controlling which expandable section is open (only one at a time)
    var expandedSectionTitle by remember { mutableStateOf("ورود شده") }
    
    // متغیر برای کنترل نمایش دیالوگ اطلاعات TopHeader
    var showTopHeaderInfoDialog by remember { mutableStateOf(false) }
    
    // متغیر برای تشخیص نوع بروزرسانی (اولیه، تغییر کوتاژ، یا بروزرسانی عادی)
    var updateType by remember { mutableStateOf<String?>(null) }
    
    // LazyListState برای کنترل اسکرول لیست حواله‌ها
    val listState = rememberLazyListState()
    
    // متغیرهای مربوط به دیالوگ تأیید حواله تکراری
    val showDuplicateConfirmationDialog by viewModel.showDuplicateConfirmationDialog.collectAsState()
    val duplicateWarningMessage by viewModel.duplicateWarningMessage.collectAsState()
    
    // متغیرهای مربوط به دیالوگ نمایش حواله‌های تکراری
    val showDuplicateDialog by viewModel.showDuplicateDialog.collectAsState()
    val duplicateTrackingNumbers by viewModel.duplicateTrackingNumbers.collectAsState()
    
    // وضعیت ثبت حواله
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    
    // متغیرهای مربوط به دیالوگ تغییر کوتاژ
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

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .navigationBarsPadding(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                // Glassmorphism Floating Action Button
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.05f)
                                ),
                                radius = 100f
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                            ),
                            shape = CircleShape
                        )
                        .shadow(
                            elevation = 12.dp,
                            shape = CircleShape,
                            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showQuotaEntryDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Inner glass effect
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.2f),
                                        Color.White.copy(alpha = 0.05f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    ),
                                    start = Offset(0f, 0f),
                                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Icon with subtle glow effect
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ConfirmationNumber,
                                contentDescription = "تغییر کوتاژ",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                            )
                        }
                    }
                    
                    // Subtle highlight on top
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.3f),
                                        Color.Transparent,
                                        Color.Transparent
                                    ),
                                    startY = 0f,
                                    endY = 50f
                                )
                            )
                    )
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

                                // جلوگیری از نمایش TopHeaderInfoDialog پس از ثبت حواله
                                updateType = "cargo_submit"
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
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    thickness = 1.dp
                )

                // بخش جستجو و دکمه بروزرسانی
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // فیلد جستجوی شماره حواله (60% عرض)
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { newValue ->
                            searchQuery = newValue
                        },
                        label = { Text("جستجوی شماره حواله") },
                        modifier = Modifier.weight(0.6f),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "جستجو"
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )

                    // دکمه بروزرسانی مینیمال (40% عرض)
                    var isRefreshing by remember { mutableStateOf(false) }
                    var rotationState by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
                    val rotation = animateFloatAsState(
                        targetValue = rotationState,
                        animationSpec = if (AnimationManager.areAnimationsEnabled()) tween(400, easing = FastOutSlowInEasing) else tween(0),
                        label = "rotation"
                    )

                    Surface3(
                        modifier = Modifier
                            .weight(0.4f)
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
                        tonalElevation = 0.5.dp,
                        shape = RoundedCornerShape(8.dp),
                        color = if (isRefreshing) 
                            MaterialTheme3.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        else 
                            MaterialTheme3.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "بروزرسانی",
                                modifier = Modifier
                                    .size(20.dp)
                                    .rotate(rotation.value),
                                tint = MaterialTheme3.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text3(
                                text = if (isRefreshing) "در حال بروزرسانی..." else "بروزرسانی",
                                style = MaterialTheme3.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme3.colorScheme.onPrimaryContainer
                            )
                        }
                    }
            }

                ExpandableSection(
                    title = "ورود شده",
                    items = nonExitedCargos.sortedByDescending { it.entryTime },
                    initiallyExpanded = true,
                    searchQuery = searchQuery,
                    duplicateTrackingNumbers = duplicateTrackingNumbers,
                    isExpanded = expandedSectionTitle == "ورود شده",
                    onExpandedChange = { expanded ->
                        expandedSectionTitle = if (expanded) "ورود شده" else ""
                    },
                    onItemClick = { selectedInfo ->
                        selectedCargoInfo.value = selectedInfo
                        showDetailDialog.value = true
                    }
                )

                ExpandableSection(
                    title = "خروج شده",
                    items = exitedCargos.sortedByDescending { "${it.exitDate} ${it.exitTime}" },
                    initiallyExpanded = false,
                    searchQuery = searchQuery,
                    duplicateTrackingNumbers = duplicateTrackingNumbers,
                    isExpanded = expandedSectionTitle == "خروج شده",
                    onExpandedChange = { expanded ->
                        expandedSectionTitle = if (expanded) "خروج شده" else ""
                    },
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
                        snackbarHostState = snackbarHostState,
                        searchQuery = searchQuery,
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
                        showTopHeaderInfoDialog = true
                    }
                }
                showQuotaEntryDialog = false
            },
            shipName = initialInfo?.shipName ?: "",
            currentQuota = initialInfo?.loadingQuotaNumber?.toString() ?: "",
            viewModel = viewModel
        )
    }
    
    // دیالوگ نمایش اطلاعات TopHeader در زمان بارگذاری صفحه
    if (showTopHeaderInfoDialog) {
        TopHeaderInfoDialog(
            loadingQuotaNumber = initialInfo?.loadingQuotaNumber ?: 0,
            loadableTonnage = loadableTonnage,
            tempTonnageStatus = initialInfo?.tempTonnageStatus ?: false,
            tempTonnageAmount = initialInfo?.tempTonnageAmount,
            loadableTrucks18Wheeler = loadableTrucks18Wheeler,
            loadableTrucks10Wheeler = loadableTrucks10Wheeler,
            onDismiss = { showTopHeaderInfoDialog = false }
        )
    }
    
    LaunchedEffect(initialInfo, loadableTonnage) {
        if (initialInfo != null && loadableTonnage.isNotEmpty()) {
            val excludedUpdateTypes = setOf("cargo_submit", "cargo_update", "cargo_delete")
            if (updateType == null || updateType !in excludedUpdateTypes) {
                if (updateType == null) {
                    showTopHeaderInfoDialog = true
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
                                viewModel.toggleQuotaStatus(warning.quotaNumber)
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
    return when {
        trimmedCode.contains("-") -> trimmedCode.split("-").last()
        trimmedCode.startsWith("990000") -> trimmedCode.substring(6)
        else -> trimmedCode
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
    
    // Barcode scanner setup
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
                                                            // کوتاژ معتبر است، ادامه دهید
                                                            onConfirm(quotaEntry)
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
    var showCamera by remember { mutableStateOf(true) } // تغییر به true برای باز شدن خودکار دوربین
    var scanMode by remember { mutableStateOf(ScanMode.LOCAL_AI_SCAN) } // پیش‌فرض: اسکن سریع
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
                                "وزن خالص باید بین 5000 تا 45000 کیلوگرم باشد",
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
                                        "انتخاب حالت اسکن",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // استفاده از کامپوننت ScanModeSelector جدید
                                ScanModeSelector(
                                    currentMode = scanMode,
                                    onModeChanged = { selectedMode ->
                                        scanMode = selectedMode
                                    }
                                )
                                
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
                                            "شروع اسکن قبض باسکول",
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
                selectedScanMode = scanMode,
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
            // فیلتر کردن اعداد در محدوده منطقی وزن (بین 5000 و 45000 کیلوگرم)
            it in 5000.0..45000.0
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
    }.filter { it in 5000.0..45000.0 }.toList()
    
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
    private val onAnalysisStateChanged: ((Boolean) -> Unit)? = null,
    private val scanMode: ScanMode = ScanMode.LOCAL_AI_SCAN // پیش‌فرض: مدل لوکال
) : ImageAnalysis.Analyzer {
    
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var lastDetectionTime = 0L
    private val detectionCooldown = 1500L // کاهش زمان انتظار برای سرعت بیشتر
    private var isProcessingWithAI = false
    
    // پردازشگر لوکال OCR
    private val localOCRProcessor by lazy { LocalOCRProcessor(context) }

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
        
        val bitmap = imageProxy.toBitmap()
        
        when (scanMode) {
            ScanMode.LOCAL_AI_SCAN -> {
                // استفاده از پردازش پیشرفته ML Kit
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val result = localOCRProcessor.processImage(bitmap)
                        
                        withContext(Dispatchers.Main) {
                            isProcessingWithAI = false
                            onAnalysisStateChanged?.invoke(false)
                            
                            if (!result.isNullOrBlank() && isValidWeight(result)) {
                                onNumbersDetected(listOf(result), result)
                            } else {
                                // fallback به ML Kit در صورت عدم موفقیت
                                fallbackToMLKit(imageProxy)
                                return@withContext
                            }
                            imageProxy.close()
                        }
                    } catch (_: Exception) {
                        withContext(Dispatchers.Main) {
                            fallbackToMLKit(imageProxy)
                        }
                    }
                }
            }
            ScanMode.ML_KIT_SCAN -> {
                fallbackToMLKit(imageProxy)
            }
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun fallbackToMLKit(imageProxy: ImageProxy) {
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
    
    private fun isValidWeight(weight: String): Boolean {
        val weightValue = weight.toDoubleOrNull()
        return weightValue != null && weightValue in 5000.0..45000.0
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
                if (number != null && number in 5000..45000) {
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
    val actualExpanded = isExpanded
    
    // Auto-expand if search query matches any item in this section
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
        targetValue = if (actualExpanded) 180f else 0f,
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
                    .clickable { onExpandedChange(!actualExpanded) }
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
                    contentDescription = if (actualExpanded) "بستن" else "باز کردن",
                    tint = MaterialTheme3.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(rotationAngle)
                )
            }

            // محتوای قابل گسترش
            AnimatedVisibility(
                visible = actualExpanded,
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

    // بررسی اعتبار شماره حواله (فقط اعداد)
    val isTrackingNumberValid = remember(trackingNumber) {
        trackingNumber.isEmpty() || trackingNumber.all { it.isDigit() }
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
        } == true
    }

    // منطق فعال/غیرفعال کردن دکمه ثبت
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
            !isDuplicate -> numberOfPeople.isNotBlank() && numberOfPeople.toIntOrNull() != null && numberOfPeople.toIntOrNull()!! > 0
            !canEditWeights -> false
            else -> {
                val hasShortage = shortageWeight.isNotBlank() && shortageWeight != "0"
                val hasExcess = excessWeight.isNotBlank() && excessWeight != "0"
                hasShortage || hasExcess
            }
        }
    }

    // انیمیشن برای نمایش پیام‌ها
    val messageAlpha = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(isDuplicate) {
        if (isDuplicate) {
            messageAlpha.snapTo(0f)
            messageAlpha.animateTo(1f, animationSpec = tween(300))
        }
    }

    Surface3(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        tonalElevation = 0.5.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // عنوان فرم مینیمال
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = MaterialTheme3.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text3(
                    text = if (isDuplicate) "ویرایش حواله" else "ثبت حواله جدید",
                    style = MaterialTheme3.typography.titleSmall,
                    color = MaterialTheme3.colorScheme.primary
                )
            }

            // بخش اول: شماره حواله و تعداد نفرات
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = trackingNumber,
                    onValueChange = { newValue ->
                        // فقط اجازه ورود اعداد
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            onTrackingNumberChange(newValue)
                        }
                    },
                    label = { Text3("شماره حواله") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = (isDuplicate && !canEditWeights) || !isTrackingNumberValid,
                            leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = MaterialTheme3.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isTrackingNumberValid) MaterialTheme3.colorScheme.primary else MaterialTheme3.colorScheme.error,
                        unfocusedBorderColor = if (isTrackingNumberValid) MaterialTheme3.colorScheme.outline else MaterialTheme3.colorScheme.error,
                        focusedLabelColor = if (isTrackingNumberValid) MaterialTheme3.colorScheme.primary else MaterialTheme3.colorScheme.error,
                        cursorColor = MaterialTheme3.colorScheme.primary,
                        errorBorderColor = MaterialTheme3.colorScheme.error,
                        errorLabelColor = MaterialTheme3.colorScheme.error
                    )
                )
                
                OutlinedTextField(
                    value = numberOfPeople,
                    onValueChange = onNumberOfPeopleChange,
                    label = { Text3("تعداد نفرات") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = !isDuplicate,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (!isDuplicate) 
                                MaterialTheme3.colorScheme.onSurfaceVariant
                            else 
                                MaterialTheme3.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme3.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme3.colorScheme.outline,
                        focusedLabelColor = MaterialTheme3.colorScheme.primary,
                        cursorColor = MaterialTheme3.colorScheme.primary,
                        disabledBorderColor = MaterialTheme3.colorScheme.outline.copy(alpha = 0.5f),
                        disabledLabelColor = MaterialTheme3.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        disabledTextColor = MaterialTheme3.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                )
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

            // بخش دوم: کسری بار و اضافه بار - فقط برای حواله‌های تایید شده و آماده خروج
            AnimatedVisibility(
                visible = isCargoConfirmed && !isCargoExited,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // کسری بار
                        OutlinedTextField(
                            value = shortageWeight,
                            onValueChange = onShortageWeightChange,
                            label = { Text("کسری بار") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            enabled = canEditWeights,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = if (canEditWeights) 
                                        MaterialTheme.colorScheme.error
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.error,
                                unfocusedBorderColor = if (shortageWeight.isNotBlank() && shortageWeight != "0")
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                else
                                    MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.error,
                                cursorColor = MaterialTheme.colorScheme.error,
                                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        )
                        
                        // اضافه بار
                        OutlinedTextField(
                            value = excessWeight,
                            onValueChange = onExcessWeightChange,
                            label = { Text("اضافه بار") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            enabled = canEditWeights,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = if (canEditWeights) 
                                        MaterialTheme.colorScheme.tertiary
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                                unfocusedBorderColor = if (excessWeight.isNotBlank() && excessWeight != "0")
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                                else
                                    MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                                cursorColor = MaterialTheme.colorScheme.tertiary,
                                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
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

            Spacer(modifier = Modifier.height(10.dp))

            // دکمه‌های ثبت و اسکن
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // دکمه ثبت
                Button(
                    onClick = onSubmit,
                    modifier = Modifier.weight(1f),
                    enabled = isSubmitEnabled,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isDuplicate) Icons.Default.Save else Icons.Default.AddCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isDuplicate) "ثبت تغییرات" else "ثبت حواله",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                
                // دکمه اسکن بارکد / خروج حواله
                Button(
                    onClick = onScanBarcode,
                    modifier = Modifier.weight(1f),
                    enabled = isCargoConfirmed && !isCargoExited,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner, 
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "خروج حواله",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
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
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .animateContentSize(),
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // هدر مینیمال
            TopHeader(
                onToggle = onToggleVisibility,
                loadedPercentage = loadedPercentage.toFloat(),
                shipName = shipInfo.shipName,
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // اطلاعات اصلی
            Column(modifier = Modifier.weight(1f)) {
                Text3(
                    text = shipName,
                    style = MaterialTheme3.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme3.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Surface3(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme3.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text3(
                            text = quotaNumber,
                            style = MaterialTheme3.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme3.colorScheme.primary,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }

            // اطلاعات تناژ و کامیون‌ها
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                // تناژ مجاز
                val tonnageValue = loadableTonnage.replace(",", "").toDoubleOrNull() ?: 0.0
                val tonnageColor = if (tonnageValue < 0) MaterialTheme3.colorScheme.error else MaterialTheme3.colorScheme.primary
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text3(
                        text = "تناژ مجاز",
                        style = MaterialTheme3.typography.labelMedium,
                        color = MaterialTheme3.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    
                    // نمایش تناژ مجاز و تناژ موقت
                    val displayText = if (tempTonnageStatus && tempTonnageAmount != null) {
                        val formattedTempTonnage = DecimalFormat("#,###").format(tempTonnageAmount.toInt())
                        "$loadableTonnage ($formattedTempTonnage)"
                    } else {
                        loadableTonnage
                    }
                    
                    Text3(
                        text = displayText,
                        style = MaterialTheme3.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = tonnageColor
                    )
                }
                
                // کامیون‌ها
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompactTruckInfo("18چ", loadableTrucks18Wheeler)
                    Text3(
                        text = "|",
                        style = MaterialTheme3.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme3.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    CompactTruckInfo("10چ", loadableTrucks10Wheeler)
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
private fun CompactTruckInfo(
    type: String,
    count: String,
) {
    val countValue = count.toIntOrNull() ?: 0
    val color = if (countValue <= 0) MaterialTheme3.colorScheme.error else MaterialTheme3.colorScheme.primary
    
    // انیمیشن چشمک زن برای مقادیر 0
    val alpha by if (countValue <= 0) {
        val infiniteTransition = rememberInfiniteTransition(label = "blinking")
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "blinking_alpha"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.alpha(alpha)
    ) {
        Icon(
            imageVector = Icons.Default.LocalShipping,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Text3(
            text = "$count = $type",
            style = MaterialTheme3.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = color
        )
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
    val formattedShortageWeight = remember(info.shortageWeight) {
        if (info.shortageWeight.isNotBlank()) {
            try {
                DecimalFormat("#,###").format(info.shortageWeight.toDoubleOrNull() ?: 0)
            } catch (_: Exception) {
                onError("Invalid shortageWeight: ${info.shortageWeight}")
                ""
            }
        } else ""
    }
    val formattedExcessWeight = remember(info.excessWeight) {
        if (info.excessWeight.isNotBlank()) {
            try {
                DecimalFormat("#,###").format(info.excessWeight.toDoubleOrNull() ?: 0)
            } catch (_: Exception) {
                onError("Invalid excessWeight: ${info.excessWeight}")
                ""
            }
        } else ""
    }

    // بررسی اینکه آیا این حواله تکراری است یا نه
    val isDuplicate = duplicateTrackingNumbers.contains(info.trackingNumber)

    Surface3(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRowClick(info) },
        tonalElevation = 0.5.dp,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme3.colorScheme.surface,
        border = BorderStroke(
            width = if (isDuplicate) 2.dp else 1.dp, // ضخامت بیشتر برای حواله‌های تکراری
            color = when {
                isDuplicate -> MaterialTheme3.colorScheme.tertiary // رنگ خاص برای حواله‌های تکراری
                info.status == "خروج" -> MaterialTheme3.colorScheme.primary
                info.confirm == "تائید شده" -> MaterialTheme3.colorScheme.secondary
                else -> MaterialTheme3.colorScheme.error
            }.copy(alpha = if (isDuplicate) 0.8f else 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            // ردیف اول: شماره حواله، وضعیت تائید و تاریخ خروج
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // آیکون وضعیت
                    Surface3(
                        shape = CircleShape,
                        color = when {
                            isDuplicate -> MaterialTheme3.colorScheme.tertiaryContainer // رنگ خاص برای حواله‌های تکراری
                            info.status == "خروج" -> MaterialTheme3.colorScheme.primaryContainer
                            info.confirm == "تائید شده" -> MaterialTheme3.colorScheme.secondaryContainer
                            else -> MaterialTheme3.colorScheme.errorContainer
                        },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = when {
                                    isDuplicate -> Icons.Default.ContentCopy // آیکون خاص برای حواله‌های تکراری
                                    info.status == "خروج" -> Icons.Default.LocalShipping
                                    info.confirm == "تائید شده" -> Icons.Default.Check
                                    else -> Icons.Default.Schedule
                                },
                                contentDescription = null,
                                tint = when {
                                    isDuplicate -> MaterialTheme3.colorScheme.onTertiaryContainer
                                    info.status == "خروج" -> MaterialTheme3.colorScheme.onPrimaryContainer
                                    info.confirm == "تائید شده" -> MaterialTheme3.colorScheme.onSecondaryContainer
                                    else -> MaterialTheme3.colorScheme.onErrorContainer
                                },
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                    
                    // شماره حواله
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text3(
                            text = "حواله:",
                            style = MaterialTheme3.typography.labelSmall,
                            color = MaterialTheme3.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text3(
                            text = info.trackingNumber,
                            style = MaterialTheme3.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme3.colorScheme.onSurface
                        )
                    }
                    
                    // تعداد نفرات
                    Surface3(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme3.colorScheme.surface.copy(alpha = 0.5f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme3.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(10.dp)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text3(
                                    text = "تعداد نفرات:",
                                    style = MaterialTheme3.typography.labelSmall,
                                    color = MaterialTheme3.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                                Text3(
                                    text = info.numberOfPeople,
                                    style = MaterialTheme3.typography.labelSmall,
                                    color = MaterialTheme3.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // تاریخ خروج
                if (info.exitDate != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text3(
                            text = "تاریخ خروج:",
                            style = MaterialTheme3.typography.labelSmall,
                            color = MaterialTheme3.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text3(
                            text = info.exitDate,
                            style = MaterialTheme3.typography.labelSmall,
                            color = MaterialTheme3.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ردیف دوم: ساعت ورود، ساعت خروج و وزن خالص
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // ساعت ورود و خروج
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ساعت ورود
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddChart,
                            contentDescription = null,
                            tint = MaterialTheme3.colorScheme.secondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text3(
                                text = "ورود:",
                                style = MaterialTheme3.typography.labelSmall,
                                color = MaterialTheme3.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text3(
                                text = info.entryTime,
                                style = MaterialTheme3.typography.labelSmall,
                                color = MaterialTheme3.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // ساعت خروج
                    if (info.exitTime != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = MaterialTheme3.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text3(
                                    text = "خروج:",
                                    style = MaterialTheme3.typography.labelSmall,
                                    color = MaterialTheme3.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text3(
                                    text = info.exitTime,
                                    style = MaterialTheme3.typography.labelSmall,
                                    color = MaterialTheme3.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // وزن خالص
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Scale,
                        contentDescription = null,
                        tint = MaterialTheme3.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text3(
                            text = "وزن خالص:",
                            style = MaterialTheme3.typography.labelSmall,
                            color = MaterialTheme3.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text3(
                            text = formattedNetWeight,
                            style = MaterialTheme3.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme3.colorScheme.onSurface
                        )
                    }
                }
            }

            // ردیف سوم: کسری و اضافه بار (در صورت وجود)
            if (formattedShortageWeight.isNotBlank() || formattedExcessWeight.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (formattedShortageWeight.isNotBlank()) {
                        Surface3(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme3.colorScheme.errorContainer.copy(alpha = 0.3f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = MaterialTheme3.colorScheme.error,
                                    modifier = Modifier.size(10.dp)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text3(
                                        text = "کسری:",
                                        style = MaterialTheme3.typography.labelSmall,
                                        color = MaterialTheme3.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text3(
                                        text = formattedShortageWeight,
                                        style = MaterialTheme3.typography.labelSmall,
                                        color = MaterialTheme3.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                    if (formattedExcessWeight.isNotBlank()) {
                        Surface3(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme3.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = MaterialTheme3.colorScheme.tertiary,
                                    modifier = Modifier.size(10.dp)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text3(
                                        text = "اضافه:",
                                        style = MaterialTheme3.typography.labelSmall,
                                        color = MaterialTheme3.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text3(
                                        text = formattedExcessWeight,
                                        style = MaterialTheme3.typography.labelSmall,
                                        color = MaterialTheme3.colorScheme.tertiary
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
    searchQuery: String = "",
    onDismiss: () -> Unit,
    onUpdateTypeChange: (String) -> Unit = {}
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    
    var expandedSection by remember { mutableStateOf("اطلاعات اصلی") }
    
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty() && info.trackingNumber.contains(searchQuery, ignoreCase = true)) {
            expandedSection = "اطلاعات اصلی"
        }
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.info))
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
                    // Header with Lottie animation similar to MessageDialog
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = "جزئیات حواله",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tracking number with card background similar to MessageDialog
                    Surface(
                        onClick = {
                            coroutineScope.launch {
                                clipboard.setText(AnnotatedString(info.trackingNumber))
                                snackbarHostState.showSnackbar("شماره حواله کپی شد")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "شماره حواله: ${info.trackingNumber}",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "کپی",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Scrollable Content with Expandable Sections
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // اطلاعات اصلی
                        item {
                            ExpandableSection(
                                title = "اطلاعات اصلی",
                                icon = Icons.Default.Info,
                                isExpanded = expandedSection == "اطلاعات اصلی",
                                onExpandedChange = { expanded ->
                                    expandedSection = if (expanded) "اطلاعات اصلی" else ""
                                },
                                accentColor = MaterialTheme.colorScheme.primary
                            ) {
                                MainInfoContent(
                                    info = info,
                                    onCopyScaleReceipt = {
                                        coroutineScope.launch {
                                            clipboard.setText(AnnotatedString(info.loadingQuotaNumber))
                                            snackbarHostState.showSnackbar("شماره قبض باسکول کپی شد")
                                        }
                                    }
                                )
                            }
                        }

                        // اطلاعات وزن
                        item {
                            ExpandableSection(
                                title = "اطلاعات وزن",
                                icon = Icons.Default.Scale,
                                isExpanded = expandedSection == "اطلاعات وزن",
                                onExpandedChange = { expanded ->
                                    expandedSection = if (expanded) "اطلاعات وزن" else ""
                                },
                                accentColor = MaterialTheme.colorScheme.tertiary
                            ) {
                                WeightInfoContent(info)
                            }
                        }

                        // اطلاعات زمان و تاریخ
                        item {
                            ExpandableSection(
                                title = "اطلاعات زمان و تاریخ",
                                icon = Icons.Default.Schedule,
                                isExpanded = expandedSection == "اطلاعات زمان و تاریخ",
                                onExpandedChange = { expanded ->
                                    expandedSection = if (expanded) "اطلاعات زمان و تاریخ" else ""
                                },
                                accentColor = MaterialTheme.colorScheme.secondary
                            ) {
                                TimeInfoContent(info)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action buttons similar to MessageDialog style
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Delete Button
                        Button(
                            onClick = { showDeleteConfirmation = true },
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
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "حذف",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Close Button
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text(
                                text = "بستن",
                                style = MaterialTheme.typography.labelLarge
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
                        trackingNumber = info.trackingNumber,
                        shipName = info.shipName,
                        loadingWarehouse = info.loadingWarehouse,
                        cargoType = info.cargoType,
                        shippingCompany = info.shippingCompany,
                        loadingQuotaNumber = info.loadingQuotaNumber
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
private fun ExpandableSection(
    title: String,
    icon: ImageVector,
    initiallyExpanded: Boolean = false,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    isExpanded: Boolean? = null,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var internalExpanded by remember { mutableStateOf(initiallyExpanded) }
    val currentExpanded = isExpanded ?: internalExpanded
    val rotationState by animateFloatAsState(
        targetValue = if (currentExpanded) 180f else 0f,
        label = "rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { 
                    if (onExpandedChange != null) {
                        onExpandedChange(!currentExpanded)
                    } else {
                        internalExpanded = !internalExpanded
                    }
                }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Icon with background
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.1f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Animated rotation for the expand/collapse icon
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (currentExpanded) "بستن" else "باز کردن",
                tint = accentColor.copy(alpha = 0.7f),
                modifier = Modifier.graphicsLayer { rotationZ = rotationState }
            )
        }

        // Content
        AnimatedVisibility(
            visible = currentExpanded,
            enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
            exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HorizontalDivider(
                    modifier = Modifier.padding(bottom = 8.dp),
                    thickness = 1.dp,
                    color = accentColor.copy(alpha = 0.1f)
                )
                content()
            }
        }
    }
}

@Composable
private fun MainInfoContent(
    info: CargoInfo,
    onCopyScaleReceipt: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoItem(label = "نام کشتی", value = info.shipName)
        InfoItem(label = "انبار بارگیری", value = info.loadingWarehouse)
        InfoItem(label = "نوع کالا", value = info.cargoType)
        InfoItem(label = "شرکت حمل و نقل", value = info.shippingCompany)
        InfoItem(
            label = "شماره قبض باسکول", 
            value = info.loadingQuotaNumber,
            onClick = onCopyScaleReceipt
        )
    }
}

@Composable
private fun WeightInfoContent(info: CargoInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        WeightItem(value = info.netWeight)
    }
}

@Composable
private fun TimeInfoContent(info: CargoInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoItem(label = "زمان ورود", value = info.entryTime ?: "--")
        InfoItem(label = "زمان خروج", value = info.exitTime ?: "--")
        InfoItem(label = "تاریخ خروج", value = info.exitDate ?: "--")
    }
}

@Composable
private fun InfoItem(label: String, value: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (onClick != null) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "کپی",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun WeightItem(value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "وزن خالص",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Text(
            text = "$value کیلوگرم",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
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
    selectedScanMode: ScanMode,
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
                                            isValidWeight = weight != null && weight in 5000.0..45000.0

                                            // تنظیم منبع تحلیل بر اساس حالت انتخاب شده
                                            analysisSource = when (selectedScanMode) {
                                                ScanMode.ML_KIT_SCAN -> "ML_KIT"
                                                ScanMode.LOCAL_AI_SCAN -> "LOCAL_AI"
                                            }
                                        } else {
                                            // اگر نتیجه‌ای نیست، state ها را پاک کن
                                            detectedNumber = null
                                            analysisSource = null
                                        }
                                    }
                                },
                                onAnalysisStateChanged = { isAnalyzing ->
                                    // Callback برای وضعیت تحلیل AI
                                    isAIAnalyzing = isAnalyzing && selectedScanMode == ScanMode.ML_KIT_SCAN
                                },
                                scanMode = selectedScanMode // ارسال حالت اسکن انتخاب شده
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
                    val (icon, title, color) = when (selectedScanMode) {
                        ScanMode.ML_KIT_SCAN -> Triple("📱", "اسکن داخلی", Color(0xFF4CAF50))
                        ScanMode.LOCAL_AI_SCAN -> Triple("🤖", "اسکن هوشمند", Color(0xFF2196F3))
                    }

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
                    if (isAIAnalyzing && selectedScanMode == ScanMode.ML_KIT_SCAN) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = color,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }

                // راهنمای کاربر
                Text(
                    text = when (selectedScanMode) {
                        ScanMode.ML_KIT_SCAN -> "قبض باسکول را در کادر قرار دهید - پردازش سریع"
                        ScanMode.LOCAL_AI_SCAN -> "قبض باسکول را در کادر قرار دهید - تحلیل هوشمند"
                    },
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
                                    text = when (analysisSource) {
                                        "ML_KIT" -> "📱 ML"
                                        "LOCAL_AI" -> "🤖 LOCAL_AI"
                                        else -> "🔍"
                                    },
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
                                text = "وزن باید بین 5,000 تا 45,000 کیلوگرم باشد",
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
            visible = isAIAnalyzing && selectedScanMode == ScanMode.ML_KIT_SCAN,
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
            color = Color.White.copy(alpha = 0.9f),
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
fun ScanModeSelector(
    currentMode: ScanMode,
    onModeChanged: (ScanMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ScanModeButton(
            text = "اسکن سریع",
            icon = Icons.Default.Psychology,
            description = "سریع و دقیق",
            isSelected = currentMode == ScanMode.LOCAL_AI_SCAN,
            onClick = { onModeChanged(ScanMode.LOCAL_AI_SCAN) },
            modifier = Modifier.weight(1f)
        )
        
        ScanModeButton(
            text = "اسکن ساده",
            icon = Icons.Default.QrCodeScanner,
            description = "پایه و سریع",
            isSelected = currentMode == ScanMode.ML_KIT_SCAN,
            onClick = { onModeChanged(ScanMode.ML_KIT_SCAN) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ScanModeButton(
    text: String,
    icon: ImageVector,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) 
            else 
                Color.Transparent,
            contentColor = if (isSelected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
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
                                pressedElevation = 8.dp
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
