package com.atk.atk_cargo.feature.cargo_registration.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.atk.atk_cargo.data.model.CargoInfo
import com.atk.atk_cargo.ui.theme.Amber700
import com.atk.atk_cargo.ui.theme.Green600
import com.atk.atk_cargo.ui.theme.Red500
import com.atk.atk_cargo.ui.theme.Teal200

private val FormAccentLight = Color(0xFF0D9488)

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
    val isDark = isSystemInDarkTheme()
    val formAccent = if (isDark) Teal200 else FormAccentLight
    val formAccentBg = formAccent.copy(alpha = if (isDark) 0.18f else 0.16f)
    val formCardBg = MaterialTheme.colorScheme.surface
    val formCardBorder = MaterialTheme.colorScheme.outlineVariant
    val formMutedText = MaterialTheme.colorScheme.onSurfaceVariant
    val formTitleColor = MaterialTheme.colorScheme.onSurface

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
    val messageAlpha = remember { Animatable(0f) }
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
        // کارت ورودی‌های اصلی
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = formCardBg,
            shadowElevation = 0.dp,
            border = BorderStroke(1.dp, formCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // سطر ۱: شماره حواله و کنترلر تعداد نفرات
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // فیلد شماره حواله
                    OutlinedTextField(
                        value = trackingNumber,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                onTrackingNumberChange(newValue)
                            }
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .heightIn(min = 48.dp),
                        placeholder = {
                            Text(
                                text = "شماره حواله",
                                style = MaterialTheme.typography.bodyMedium,
                                color = formMutedText
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Numbers,
                                contentDescription = null,
                                tint = formMutedText,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        isError = (isDuplicate && !canEditWeights) || !isTrackingNumberValid,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            if (isSubmitEnabled) onSubmit()
                        }),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = formAccent,
                            unfocusedBorderColor = formCardBorder
                        )
                    )

                    // کنترلر تعداد نفرات
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = formCardBg,
                        border = BorderStroke(1.dp, formCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val currentValue = numberOfPeople.toIntOrNull() ?: 1

                            // دکمه کاهش (-)
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clickable(enabled = !isDuplicate && currentValue > 1) {
                                        if (currentValue > 1) {
                                            onNumberOfPeopleChange((currentValue - 1).toString())
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "کاهش نفرات",
                                    tint = if (!isDuplicate && currentValue > 1) formAccent else formMutedText.copy(alpha = 0.4f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // مقدار نفرات
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = numberOfPeople.ifEmpty { "1" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = formTitleColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = formAccent,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            // دکمه افزایش (+)
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clickable(enabled = !isDuplicate && currentValue < 5) {
                                        if (currentValue < 5) {
                                            onNumberOfPeopleChange((currentValue + 1).toString())
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "افزایش نفرات",
                                    tint = if (!isDuplicate && currentValue < 5) formAccent else formMutedText.copy(alpha = 0.4f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // سطر ۲: کسری بار و اضافه بار
                AnimatedVisibility(
                    visible = isCargoConfirmed && !isCargoExited,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = shortageWeight,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                    onShortageWeightChange(newValue)
                                }
                            },
                            enabled = canEditWeights,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            label = { Text("کسری بار") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = excessWeight,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                    onExcessWeightChange(newValue)
                                }
                            },
                            enabled = canEditWeights,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            label = { Text("اضافه بار") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // سطر ۳: دکمه‌های اقدام
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // دکمه ثبت
                    val submitColor = if (isDuplicate) Amber700 else Green600
                    Surface(
                        onClick = onSubmit,
                        enabled = isSubmitEnabled,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = formCardBg,
                        border = BorderStroke(1.dp, if (isSubmitEnabled) submitColor.copy(alpha = 0.4f) else formCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isDuplicate) "ثبت تغییرات" else "ثبت حواله",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSubmitEnabled) formTitleColor else formMutedText.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = null,
                                tint = if (isSubmitEnabled) submitColor else formMutedText.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // دکمه خروج
                    val exitEnabled = isCargoConfirmed && !isCargoExited
                    Surface(
                        onClick = onScanBarcode,
                        enabled = exitEnabled,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = formCardBg,
                        border = BorderStroke(1.dp, if (exitEnabled) Red500.copy(alpha = 0.4f) else formCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "خروج حواله",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (exitEnabled) formTitleColor else formMutedText.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = if (exitEnabled) Red500 else formMutedText.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // پیام هشدار اعتبارسنجی
        AnimatedVisibility(
            visible = !isTrackingNumberValid && trackingNumber.isNotBlank(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "شماره حواله فقط باید شامل اعداد باشد.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // وضعیت تکراری/موجود
        AnimatedVisibility(
            visible = isDuplicate,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            val (messageText, messageColor) = when {
                currentCargo?.status == "خروج" ->
                    Pair("این حواله قبلاً خروج شده است!", MaterialTheme.colorScheme.error)
                currentCargo?.confirm == "در انتظار تائید" ->
                    Pair("این حواله هنوز تائید نشده و در انتظار تائید است.", MaterialTheme.colorScheme.error)
                canEditWeights ->
                    Pair("حواله تائید شده؛ امکان ثبت کسری/اضافه یا خروج وجود دارد.", formAccent)
                else ->
                    Pair("این حواله غیرقابل ویرایش است.", MaterialTheme.colorScheme.error)
            }

            Surface(
                color = messageColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .alpha(messageAlpha.value)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (messageColor == MaterialTheme.colorScheme.error) Icons.Default.Info else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = messageColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = messageText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = messageColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // شماره قبض اسکن شده
        AnimatedVisibility(
            visible = scaleReceiptNumber.isNotBlank(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                color = formAccentBg,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = formAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "قبض باسکول دریافت شد: $scaleReceiptNumber",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = formAccent
                    )
                }
            }
        }
    }
}
