package com.atk.atk_cargo.feature.cargo_registration.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.atk.atk_cargo.data.model.CargoInfo
import com.atk.atk_cargo.ui.theme.Amber700
import com.atk.atk_cargo.ui.theme.BorderLight
import com.atk.atk_cargo.ui.theme.Green600
import com.atk.atk_cargo.ui.theme.Red500

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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isDuplicate) "ویرایش حواله" else "ثبت حواله جدید",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // کارت فرم اصلی
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if ((isDuplicate && !canEditWeights) || !isTrackingNumberValid)
                                            MaterialTheme.colorScheme.error
                                        else
                                            BorderLight,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(start = 40.dp, end = 12.dp),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
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
                                            Text(
                                                text = "شماره حواله",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                    color = if (!isDuplicate) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
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
                                        tint = if (!isDuplicate && currentValue < 5) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
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
                                        tint = if (!isDuplicate) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = numberOfPeople.ifEmpty { "1" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isDuplicate) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
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
                                        tint = if (!isDuplicate && currentValue > 1) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
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
                                            color = if (canEditWeights) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outline,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(start = 40.dp, end = 12.dp),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (canEditWeights) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
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
                                                Text(
                                                    text = "کسری بار",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (canEditWeights) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
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
                                        tint = if (canEditWeights) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
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
                                            color = if (canEditWeights) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outline,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(start = 40.dp, end = 12.dp),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (canEditWeights) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
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
                                                Text(
                                                    text = "اضافه بار",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (canEditWeights) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
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
                                        tint = if (canEditWeights) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
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
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clickable(enabled = isSubmitEnabled, onClick = onSubmit),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSubmitEnabled) {
                                if (isDuplicate) Amber700 else Green600
                            } else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = null,
                                    tint = if (isSubmitEnabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isDuplicate) "ثبت تغییرات" else "ثبت حواله",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSubmitEnabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }

                        // دکمه خروج حواله
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clickable(
                                    enabled = isCargoConfirmed && !isCargoExited,
                                    onClick = onScanBarcode
                                ),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCargoConfirmed && !isCargoExited) Red500 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = if (isCargoConfirmed && !isCargoExited) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "خروج حواله",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCargoConfirmed && !isCargoExited) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
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
