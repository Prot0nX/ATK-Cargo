package com.atk.atk_cargo.feature.cargo_registration.presentation.components.dialogs

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.atk.atk_cargo.feature.cargo_registration.domain.ocr.extractNumber
import com.atk.atk_cargo.feature.cargo_registration.domain.ocr.preprocessImage
import com.atk.atk_cargo.feature.cargo_registration.domain.ocr.recognizeTextFromImage
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.EnhancedCameraPreview
import kotlinx.coroutines.launch

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

    fun validateAndConfirm() {
        val weight = netWeight.toIntOrNull()
        if (weight != null && weight in 1000..60000) {
            onConfirm(netWeight)
        } else {
            isError = true
        }
    }

    StandardDialogShell(
        onDismissRequest = {
            focusManager.clearFocus()
            onDismiss()
        },
        dismissOnBackPress = false,
        dismissOnClickOutside = false
    ) {
        DialogBadge(icon = Icons.Default.Scale, tint = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(16.dp))

        DialogTitle(text = "ثبت وزن خالص", color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(16.dp))

        DialogContentCard(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
            shape = RoundedCornerShape(DialogContentCornerRadius),
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

        DialogContentCard {
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

            Surface(
                onClick = { showCamera = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DialogButtonHeight),
                shape = RoundedCornerShape(DialogButtonCornerRadius),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = "شروع اسکن",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "اسکن تناژ خالص",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "برای اسکن خودکار وزن، یکی از حالت‌های اسکن را انتخاب کنید",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            fontSize = 10.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        DialogButtonRow(
            secondaryText = "انصراف",
            onSecondaryClick = {
                focusManager.clearFocus()
                onDismiss()
            },
            primaryText = "ثبت وزن خالص",
            onPrimaryClick = {
                focusManager.clearFocus()
                validateAndConfirm()
            }
        )
    }

    if (showCamera) {
        Dialog(onDismissRequest = {
            showCamera = false
        }) {
            EnhancedCameraPreview(
                onImageCaptured = { image, detectedWeight ->
                    showCamera = false
                    coroutineScope.launch {
                        try {
                            if (!detectedWeight.isNullOrEmpty()) {
                                val weightValue = detectedWeight.toDoubleOrNull()
                                if (weightValue != null && weightValue in 1000.0..60000.0) {
                                    netWeight = detectedWeight
                                } else {
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
