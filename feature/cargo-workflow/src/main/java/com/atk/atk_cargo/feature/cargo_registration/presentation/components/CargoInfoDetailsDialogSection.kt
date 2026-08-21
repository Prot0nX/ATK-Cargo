package com.atk.atk_cargo.feature.cargo_registration.presentation.components

import android.content.ClipData
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.atk.atk_cargo.data.model.CargoInfoRequest
import com.atk.atk_cargo.domain.model.Cargo
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.dialogs.DialogBadgeSize
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.dialogs.DialogButtonRow
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.dialogs.DialogContentCard
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.dialogs.DialogContentCornerRadius
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.dialogs.DialogMessageText
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.dialogs.DialogTitle
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.dialogs.StandardDialogShell
import com.atk.atk_cargo.feature.cargoworkflow.R
import com.atk.atk_cargo.ui.viewmodel.CargoViewModel
import kotlinx.coroutines.launch

// این فایل دیالوگ جزئیات حواله و دیالوگ حذف وابسته‌اش را از RegisterCargoDialogs.kt جدا نگه می‌دارد

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
    val accent = MaterialTheme.colorScheme.primary
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
fun CargoInfoDetailsDialog(
    info: Cargo,
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
                // نتیجه‌ی واقعی از طریق کانال پیام‌رسانی خود ViewModel نمایش داده می‌شود
                val request = CargoInfoRequest(id = info.id ?: 0, password = password)
                viewModel.deleteCargo(request)
                onUpdateTypeChange("cargo_delete")
                onDismiss()
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
    info: Cargo,
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
private fun WeightInfoTabContent(info: Cargo) {
    val formattedNetWeight = remember(info.netWeight) {
        info.netWeight?.formatted() ?: "0"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        DetailInfoRow(label = "وزن خالص", value = "$formattedNetWeight کیلوگرم", isLast = true)
    }
}

@Composable
private fun TimeInfoTabContent(info: Cargo) {
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

    val errorColor = MaterialTheme.colorScheme.error

    StandardDialogShell(onDismissRequest = onDismiss, dismissOnClickOutside = false) {
        Box(
            modifier = Modifier
                .size(DialogBadgeSize)
                .background(errorColor.copy(alpha = 0.1f), CircleShape)
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

        DialogTitle(text = "حذف حواله", color = errorColor)

        Spacer(modifier = Modifier.height(16.dp))

        DialogContentCard(
            color = errorColor.copy(alpha = 0.05f),
            borderColor = errorColor.copy(alpha = 0.2f)
        ) {
            DialogMessageText(
                message = "آیا از حذف این حواله اطمینان دارید؟ این عملیات غیرقابل بازگشت است.",
                color = MaterialTheme.colorScheme.onSurface
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
            shape = RoundedCornerShape(DialogContentCornerRadius),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = errorColor,
                focusedLabelColor = errorColor
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        DialogButtonRow(
            primaryText = "تایید حذف",
            onPrimaryClick = onConfirm,
            primaryEnabled = password.isNotEmpty(),
            primaryColor = errorColor,
            secondaryText = "انصراف",
            onSecondaryClick = onDismiss
        )
    }
}
