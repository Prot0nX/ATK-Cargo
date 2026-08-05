package com.atk.atk_cargo.feature.cargo_entry.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atk.atk_cargo.api.ActiveShipInfo
import com.atk.atk_cargo.ui.theme.Teal200
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private val QuotaEntryAccentLight = Color(0xFF0D9488)

/** رنگ‌های تیل سازگار با تم روشن/تاریک برای دیالوگ ورود 4 رقم آخر کوتاژ. */
private class QuotaEntryPalette(
    val accent: Color,
    val accentBg: Color,
    val accentBorder: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val mutedText: Color,
    val titleColor: Color
)

@Composable
private fun rememberQuotaEntryPalette(): QuotaEntryPalette {
    val isDark = isSystemInDarkTheme()
    val accent = if (isDark) Teal200 else QuotaEntryAccentLight
    return QuotaEntryPalette(
        accent = accent,
        accentBg = accent.copy(alpha = if (isDark) 0.18f else 0.16f),
        accentBorder = accent.copy(alpha = 0.4f),
        cardBg = MaterialTheme.colorScheme.surface,
        cardBorder = MaterialTheme.colorScheme.outlineVariant,
        mutedText = MaterialTheme.colorScheme.onSurfaceVariant,
        titleColor = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun QuotaEntryDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onScanBarcode: () -> Unit,
    shipName: String,
    ships: List<ActiveShipInfo>
) {
    val palette = rememberQuotaEntryPalette()
    var quotaEntry by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(showDialog) {
        if (showDialog) {
            quotaEntry = ""
            isError = false
            errorMessage = ""
            delay(100.milliseconds)
            focusRequester.requestFocus()
        }
    }

    if (showDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                color = palette.cardBg,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(palette.accentBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBoat,
                                contentDescription = null,
                                tint = palette.accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = shipName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.titleColor
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    QuotaEntryContent(
                        quotaEntry = quotaEntry,
                        isError = isError,
                        errorMessage = errorMessage,
                        focusRequester = focusRequester,
                        palette = palette,
                        onQuotaChange = { newValue ->
                            if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                                quotaEntry = newValue
                                isError = false
                                errorMessage = ""
                            }
                        },
                        onDone = {
                            validateAndSubmit(quotaEntry, ships, onConfirm) { msg ->
                                isError = true
                                errorMessage = msg
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    QuotaEntryActions(
                        quotaEntry = quotaEntry,
                        palette = palette,
                        onConfirm = {
                            validateAndSubmit(quotaEntry, ships, onConfirm) { msg ->
                                isError = true
                                errorMessage = msg
                            }
                        },
                        onScanBarcode = onScanBarcode,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun QuotaEntryContent(
    quotaEntry: String,
    isError: Boolean,
    errorMessage: String,
    focusRequester: FocusRequester,
    palette: QuotaEntryPalette,
    onQuotaChange: (String) -> Unit,
    onDone: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = quotaEntry,
            onValueChange = onQuotaChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                letterSpacing = 6.sp,
                fontWeight = FontWeight.Medium
            ),
            placeholder = {
                Text(
                    "4 رقم آخر کوتاژ",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = palette.mutedText.copy(alpha = 0.6f)
                )
            },
            isError = isError,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) MaterialTheme.colorScheme.error else palette.accent,
                unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else palette.cardBorder,
                focusedContainerColor = palette.cardBg,
                unfocusedContainerColor = palette.cardBg,
                cursorColor = palette.accent,
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )

        Text(
            text = "${quotaEntry.length}/4",
            style = MaterialTheme.typography.bodySmall,
            color = if (quotaEntry.length == 4) palette.accent else palette.mutedText.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        AnimatedVisibility(
            visible = isError,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
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
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun QuotaEntryActions(
    quotaEntry: String,
    palette: QuotaEntryPalette,
    onConfirm: () -> Unit,
    onScanBarcode: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                enabled = quotaEntry.length == 4,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.accent,
                    contentColor = Color.White,
                    disabledContainerColor = palette.accent.copy(alpha = 0.4f)
                )
            ) {
                Text("تأیید", fontWeight = FontWeight.Bold)
            }

            Surface(
                onClick = onScanBarcode,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = palette.accentBg,
                border = BorderStroke(1.dp, palette.accentBorder)
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = palette.accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("اسکن", color = palette.accent, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("انصراف", color = palette.mutedText)
        }
    }
}

private fun validateAndSubmit(
    quotaEntry: String,
    ships: List<ActiveShipInfo>,
    onConfirm: (String) -> Unit,
    onError: (String) -> Unit
) {
    when {
        quotaEntry.length != 4 -> {
            onError("لطفاً 4 رقم آخر کوتاژ را وارد کنید")
        }
        !ships.any { it.loadingQuotaNumber.endsWith(quotaEntry) } -> {
            onError("کوتاژ مورد نظر یافت نشد")
        }
        else -> onConfirm(quotaEntry)
    }
}
