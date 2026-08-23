package com.atk.atk_cargo.feature.cargo_registration.presentation.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.atk.atk_cargo.data.model.ActiveShipInfo
import com.atk.atk_cargo.data.model.MatchingQuota
import com.atk.atk_cargo.data.model.MessageType
import com.atk.atk_cargo.feature.cargo.viewmodel.CargoViewModel
import com.atk.atk_cargo.feature.cargo_entry.presentation.components.QuotaSelectionDialog
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.dialogs.DialogBadge
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.dialogs.DialogBadgeSize
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.dialogs.DialogButtonCornerRadius
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.dialogs.DialogButtonHeight
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.dialogs.DialogButtonRow
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.dialogs.DialogContentCard
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.dialogs.DialogMessageText
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.dialogs.DialogTitle
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.dialogs.StandardDialogShell
import com.atk.atk_cargo.feature.cargoworkflow.R
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private val DialogAccent: Color
    @Composable get() = MaterialTheme.colorScheme.primary
private val DialogAccentBg: Color
    @Composable get() = MaterialTheme.colorScheme.primaryContainer
private val DialogAccentBorder: Color
    @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
private val DialogTitleColor = Color(0xFF1F2937)

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
    onConfirm: (MatchingQuota) -> Unit,
    shipName: String,
    currentQuota: String,
    viewModel: CargoViewModel
) {
    var quotaEntry by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showQuotaSelectionDialog by remember { mutableStateOf(false) }
    var matchingQuotasForSelection by remember { mutableStateOf<List<MatchingQuota>>(emptyList()) }
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    // اعمال کوتاژ انتخاب‌شده؛ هم برای تطبیق تکی و هم انتخاب از دیالوگ کوتاژهای مشابه استفاده می‌شود
    fun applySelectedQuota(selectedQuota: MatchingQuota) {
        if (selectedQuota.shipName == shipName) {
            if (selectedQuota.isActive) {
                onConfirm(selectedQuota)
            } else {
                isError = true
                errorMessage = "کوتاژ ${selectedQuota.quotaNumber} در حال حاضر غیرفعال است و قابل انتخاب نیست"
            }
        } else {
            isError = true
            errorMessage = "کوتاژ ${selectedQuota.quotaNumber} متعلق به کشتی ${selectedQuota.shipName} است"
        }
    }
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
            showQuotaSelectionDialog = false
            matchingQuotasForSelection = emptyList()
            delay(150.milliseconds)
            focusRequester.requestFocus()
        }
    }

    if (showDialog && showQuotaSelectionDialog) {
        QuotaSelectionDialog(
            matchingQuotas = matchingQuotasForSelection,
            ship = ActiveShipInfo(
                shipName = shipName,
                loadingWarehouse = "",
                cargoType = "",
                shippingCompany = "",
                loadingQuotaNumber = ""
            ),
            onQuotaSelected = { selectedQuota ->
                showQuotaSelectionDialog = false
                applySelectedQuota(selectedQuota)
            },
            onDismiss = { showQuotaSelectionDialog = false }
        )
    } else if (showDialog) {
        StandardDialogShell(
            onDismissRequest = { if (!isLoading) onDismiss() },
            dismissOnBackPress = !isLoading,
            dismissOnClickOutside = !isLoading
        ) {
            DialogBadge(icon = Icons.Default.ConfirmationNumber, tint = DialogAccent)

            Spacer(modifier = Modifier.height(16.dp))

            DialogTitle(text = "تغییر کوتاژ", color = DialogTitleColor)

            Spacer(modifier = Modifier.height(16.dp))

            DialogContentCard(color = DialogAccentBg, borderColor = DialogAccentBorder) {
                Row(
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

            Spacer(modifier = Modifier.height(20.dp))

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

            Spacer(modifier = Modifier.height(20.dp))

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

                                            when {
                                                !response.exists || response.matchingQuotas.isEmpty() -> {
                                                    isError = true
                                                    errorMessage = "کوتاژ $quotaEntry برای کشتی $shipName یافت نشد"
                                                }
                                                response.matchingQuotas.size > 1 -> {
                                                    // چند کوتاژ تکراری منطبق یافت شد؛ کاربر باید مورد دقیق را انتخاب کند
                                                    matchingQuotasForSelection = response.matchingQuotas
                                                    showQuotaSelectionDialog = true
                                                }
                                                else -> {
                                                    applySelectedQuota(response.matchingQuotas.first())
                                                }
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
                        .height(DialogButtonHeight),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(DialogButtonCornerRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DialogAccent,
                        disabledContainerColor = DialogAccent.copy(alpha = 0.4f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 12.dp
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
                        .height(DialogButtonHeight),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(DialogButtonCornerRadius),
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

        StandardDialogShell(onDismissRequest = onDismiss) {
            DialogBadge(icon = icon, tint = color)

            Spacer(modifier = Modifier.height(16.dp))

            DialogTitle(text = "اطلاع‌رسانی", color = color)

            Spacer(modifier = Modifier.height(16.dp))

            DialogContentCard {
                DialogMessageText(
                    message = message,
                    textAlign = if (message.contains("\n")) TextAlign.Start else TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            DialogButtonRow(
                primaryText = "متوجه شدم",
                onPrimaryClick = onDismiss,
                primaryColor = color
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

    val warningColor = MaterialTheme.colorScheme.tertiary

    StandardDialogShell(
        onDismissRequest = onDismiss,
        dismissOnClickOutside = false
    ) {
        Box(
            modifier = Modifier
                .size(DialogBadgeSize)
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

        DialogTitle(text = "حواله تکراری", color = MaterialTheme.colorScheme.onSurface)

        Spacer(modifier = Modifier.height(16.dp))

        DialogContentCard {
            DialogMessageText(message)
        }

        Spacer(modifier = Modifier.height(24.dp))

        DialogButtonRow(
            primaryText = "ثبت حواله",
            onPrimaryClick = onConfirm,
            primaryColor = DialogAccent,
            secondaryText = "انصراف",
            onSecondaryClick = onCancel
        )
    }
}
