package com.atk.atk_cargo.feature.cargo_registration.presentation.components

import android.content.ClipData
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.atk.atk_cargo.R
import com.atk.atk_cargo.data.model.CargoInfo
import com.atk.atk_cargo.data.model.CargoInfoRequest
import com.atk.atk_cargo.data.model.MessageType
import com.atk.atk_cargo.data.model.WarningStatus
import com.atk.atk_cargo.ui.theme.Teal200
import com.atk.atk_cargo.ui.viewmodel.CargoViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import kotlin.time.Duration.Companion.milliseconds

private val DialogAccent = Color(0xFF0D9488)
private val DialogAccentBg = Color(0xFFDCEFEA)
private val DialogAccentBorder = Color(0xFFB9DED7)
private val DialogMutedBg = Color(0xFFF3F4F5)
private val DialogMutedText = Color(0xFF8A8F98)
private val DialogTitleColor = Color(0xFF1F2937)

private val CargoDetailsAccentLight = Color(0xFF0D9488)

/** رنگ‌های تیل سازگار با تم روشن/تاریک برای دیالوگ جزئیات حواله. */
private class CargoDetailsPalette(
    val accent: Color,
    val accentBg: Color,
    val accentBorder: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val mutedBg: Color,
    val mutedText: Color,
    val titleColor: Color
)

@Composable
private fun rememberCargoDetailsPalette(): CargoDetailsPalette {
    val isDark = isSystemInDarkTheme()
    val accent = if (isDark) Teal200 else CargoDetailsAccentLight
    return CargoDetailsPalette(
        accent = accent,
        accentBg = accent.copy(alpha = if (isDark) 0.18f else 0.16f),
        accentBorder = accent.copy(alpha = 0.4f),
        cardBg = MaterialTheme.colorScheme.surface,
        cardBorder = MaterialTheme.colorScheme.outlineVariant,
        mutedBg = MaterialTheme.colorScheme.surfaceVariant,
        mutedText = MaterialTheme.colorScheme.onSurfaceVariant,
        titleColor = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun DuplicateTrackingNumbersDialog(
    duplicateNumbers: List<String>,
    onDismiss: () -> Unit,
    onSearchTrackingNumber: (String) -> Unit = {}
) {
    com.atk.atk_cargo.feature.cargo_registration.presentation.components.dialogs.DuplicateTrackingNumbersDialog(
        duplicateNumbers = duplicateNumbers,
        onDismiss = onDismiss,
        onSearchTrackingNumber = onSearchTrackingNumber
    )
}



@Composable
fun QuotaWarningDialog(
    warning: WarningStatus,
    onDismiss: () -> Unit,
    viewModel: CargoViewModel,
) {
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { androidx.compose.animation.core.Animatable(0.8f) }
    val alpha = remember { androidx.compose.animation.core.Animatable(0f) }
    val iconScale = remember { androidx.compose.animation.core.Animatable(1f) }

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
                delay(1000.milliseconds)
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
                Box(
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                ) {
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

                Text(
                    text = "هشدار محدودیت درصد کوتاژ",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

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

                        Box(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                            )

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

                Text(
                    text = "کوتاژ به حد نصاب مجاز رسیده است و امکان ثبت حواله جدید و خروج وجود ندارد.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Justify,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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

    LaunchedEffect(showDialog) {
        if (showDialog) {
            quotaEntry = ""
            isError = false
            errorMessage = ""
            isLoading = false
            delay(150.milliseconds)
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(DialogAccentBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ConfirmationNumber,
                                        contentDescription = null,
                                        tint = DialogAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Text(
                                    text = "تغییر کوتاژ",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DialogTitleColor
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(DialogMutedBg)
                                    .clickable(enabled = !isLoading) { onDismiss() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "بستن",
                                    tint = DialogTitleColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = DialogAccentBg,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, DialogAccentBorder)
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
                                    tint = DialogAccent,
                                    modifier = Modifier.size(24.dp)
                                )

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "کشتی: $shipName",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DialogAccent
                                    )
                                    Text(
                                        text = "کوتاژ فعلی: $currentQuota",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = DialogAccent.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                                        if (quotaEntry.all { it.isDigit() }) {
                                            isLoading = true
                                            coroutineScope.launch {
                                                try {
                                                    val response = viewModel.checkQuotaExistenceCargo(quotaEntry, shipName)
                                                    
                                                    if (response.exists && response.matchingQuotas.isNotEmpty()) {
                                                        val selectedQuota = response.matchingQuotas.first()
                                                        
                                                        if (selectedQuota.shipName == shipName) {
                                                            if (selectedQuota.isActive) {
                                                                onConfirm(quotaEntry)
                                                            } else {
                                                                isError = true
                                                                errorMessage = "کوتاژ $quotaEntry در حال حاضر غیرفعال است و قابل انتخاب نیست"
                                                            }
                                                        } else {
                                                            isError = true
                                                            errorMessage = "کوتاژ $quotaEntry متعلق به کشتی ${selectedQuota.shipName} است"
                                                        }
                                                    } else {
                                                        isError = true
                                                        errorMessage = "کوتاژ $quotaEntry برای کشتی $shipName یافت نشد"
                                                    }
                                                } catch (e: Exception) {
                                                    isError = true
                                                    errorMessage = "خطا در بررسی کوتاژ: ${e.message}"
                                                } finally {
                                                    isLoading = false
                                                }
                                            }
                                        } else {
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
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DialogAccent,
                                disabledContainerColor = DialogAccent.copy(alpha = 0.4f)
                            )
                        ) {
                            if (isLoading) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
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

                        Surface(
                            onClick = { startBarcodeScanner() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            color = DialogAccentBg,
                            border = BorderStroke(1.dp, DialogAccentBorder)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
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
                                        tint = if (isLoading) DialogAccent.copy(alpha = 0.5f) else DialogAccent
                                    )
                                    Text(
                                        text = "اسکن",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isLoading) DialogAccent.copy(alpha = 0.5f) else DialogAccent,
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
                          else DialogAccent,
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
            singleLine = true,
            isError = isError,
            enabled = !isLoading,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) MaterialTheme.colorScheme.error
                                   else DialogAccent,
                unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                     else Color(0xFFE5E7EA),
                focusedLabelColor = if (isError) MaterialTheme.colorScheme.error
                                  else DialogAccent,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error,
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "کوتاژ باید 4 رقم باشد",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            
            Text(
                text = "${quotaEntry.length}/4",
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    quotaEntry.length == 4 -> DialogAccent
                    quotaEntry.length > 4 -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                }
            )
        }

        AnimatedVisibility(
            visible = isError && errorMessage.isNotEmpty(),
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
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DialogAccentBg,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = DialogAccent
                    )

                    Text(
                        text = "در حال بررسی کوتاژ...",
                        style = MaterialTheme.typography.bodySmall,
                        color = DialogAccent,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun NetWeightDialog(
    scaleReceiptNumber: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    com.atk.atk_cargo.feature.cargo_registration.presentation.components.dialogs.NetWeightDialog(
        scaleReceiptNumber = scaleReceiptNumber,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun ExitStatusDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    exitVouchersCount: Int,
    totalNetWeight: Float,
) {
    com.atk.atk_cargo.feature.cargo_registration.presentation.components.dialogs.ExitStatusDialog(
        showDialog = showDialog,
        onDismiss = onDismiss,
        exitVouchersCount = exitVouchersCount,
        totalNetWeight = totalNetWeight
    )
}

@Composable
fun MessageDialog(
    message: String,
    type: MessageType,
    visible: Boolean,
    onDismiss: () -> Unit
) {
    if (visible) {
        val color = when (type) {
            MessageType.SUCCESS -> Color(0xFF4CAF50)
            MessageType.ERROR -> MaterialTheme.colorScheme.error
            MessageType.WARNING -> Color(0xFFFF9800)
        }
        
        val icon = when (type) {
            MessageType.SUCCESS -> Icons.Default.CheckCircle
            MessageType.ERROR -> Icons.Default.Close
            MessageType.WARNING -> Icons.Default.Warning
        }

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
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(color.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "اطلاع‌رسانی",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val context = androidx.compose.ui.platform.LocalContext.current
                    @Suppress("DEPRECATION")
                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = if (message.contains("\n")) TextAlign.Start else TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .clickable {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(message))
                                android.widget.Toast.makeText(context, "کپی شد!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = color)
                    ) {
                        Text("متوجه شدم")
                    }
                }
            }
        }
    }
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
        val transition = updateTransition(targetState = isVisible, label = "mTransition")
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
                        colors = ButtonDefaults.buttonColors(containerColor = DialogAccent),
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
        tint = DialogAccent,
        modifier = Modifier
            .size(64.dp)
            .scale(scale)
    )
}

@Composable
fun CargoInfoDetailsDialog(
    info: CargoInfo,
    viewModel: CargoViewModel,
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit,
    onUpdateTypeChange: (String) -> Unit = {}
) {
    val palette = rememberCargoDetailsPalette()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current
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
            color = palette.cardBg,
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

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                color = palette.accentBg,
                                shape = CircleShape
                            )
                            .border(
                                width = 8.dp,
                                color = palette.cardBg.copy(alpha = 0.5f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    color = palette.accent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PriorityHigh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "جزئیات حواله",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = palette.accent
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = palette.accentBg,
                        border = BorderStroke(1.dp, palette.accentBorder)
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
                                color = palette.titleColor,
                                modifier = Modifier.align(Alignment.Center)
                            )

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
                                color = palette.cardBg
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "کپی",
                                        tint = palette.accent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = palette.cardBg
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
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
                                    CargoDetailTabItem(
                                        title = "اطلاعات اصلی",
                                        icon = Icons.Default.Info,
                                        isSelected = selectedTab == 0,
                                        onClick = { selectedTab = 0 },
                                        palette = palette,
                                        modifier = Modifier.weight(1f)
                                    )

                                    CargoDetailTabItem(
                                        title = "وزن",
                                        icon = Icons.Default.Scale,
                                        isSelected = selectedTab == 1,
                                        onClick = { selectedTab = 1 },
                                        palette = palette,
                                        modifier = Modifier.weight(1f)
                                    )

                                    CargoDetailTabItem(
                                        title = "زمان و تاریخ",
                                        icon = Icons.Default.Schedule,
                                        isSelected = selectedTab == 2,
                                        onClick = { selectedTab = 2 },
                                        palette = palette,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

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
                
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    color = palette.cardBg,
                    border = BorderStroke(1.dp, palette.cardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1.2f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.accentBg,
                                contentColor = palette.accent
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "بستن",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
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
private fun CargoDetailTabItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    palette: CargoDetailsPalette,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) palette.cardBg else Color.Transparent,
        shadowElevation = if (isSelected) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) palette.accent else palette.mutedText,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) palette.accent else palette.mutedText,
                maxLines = 1
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
        } catch (_: Exception) {
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
    val palette = rememberCargoDetailsPalette()
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
                        color = palette.accentBg,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "کپی",
                                tint = palette.accent,
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

                    Text(
                        text = "حذف حواله",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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

                    Text(
                        text = "حواله تکراری",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onConfirm,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DialogAccent,
                                contentColor = Color.White
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
