package com.atk.atk_cargo.feature.reports.presentation.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

// فرم‌های قابل‌ویرایش اطلاعات حواله در حالت ویرایش دیالوگ نتیجه‌ی جستجو — از CargoEditSearchDialogsSection.kt جدا شد (فاز۴ #۴۰)
@Composable
internal fun EditableCargoMainInfo(
    trackingNumber: String,
    onTrackingNumberChange: (String) -> Unit,
    scaleReceiptNumber: String,
    onScaleReceiptNumberChange: (String) -> Unit,
    loadingQuotaNumber: String,
    onLoadingQuotaNumberChange: (String) -> Unit,
    numberOfPeople: String,
    onNumberOfPeopleChange: (String) -> Unit
) {
    InfoCard(
        mainColor = MaterialTheme.colorScheme.primary,
        title = "اطلاعات اصلی",
        icon = Icons.Default.Description,
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = trackingNumber,
                    onValueChange = onTrackingNumberChange,
                    label = { Text("شماره حواله") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Numbers, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = scaleReceiptNumber,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            onScaleReceiptNumberChange(it)
                        }
                    },
                    label = { Text("قبض باسکول") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Receipt, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = loadingQuotaNumber,
                    onValueChange = onLoadingQuotaNumberChange,
                    label = { Text("شماره کوتاژ") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Newspaper, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = numberOfPeople,
                    onValueChange = onNumberOfPeopleChange,
                    label = { Text("تعداد افراد") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.People, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    )
}

@Composable
internal fun EditableCargoWeightInfo(
    netWeight: String,
    onNetWeightChange: (String) -> Unit,
    shortageWeight: String,
    onShortageWeightChange: (String) -> Unit,
    excessWeight: String,
    onExcessWeightChange: (String) -> Unit
) {
    InfoCard(
        mainColor = MaterialTheme.colorScheme.secondary,
        title = "اطلاعات وزن",
        icon = Icons.Default.Scale,
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = netWeight,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            onNetWeightChange(it)
                        }
                    },
                    label = { Text("وزن خالص (کیلوگرم)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Scale, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = shortageWeight,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            onShortageWeightChange(it)
                        }
                    },
                    label = { Text("کسری بار (کیلوگرم)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = excessWeight,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            onExcessWeightChange(it)
                        }
                    },
                    label = { Text("اضافه بار (کیلوگرم)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    )
}

@Composable
internal fun EditableCargoTimeInfo(
    entryTime: String,
    onEntryTimeChange: (String) -> Unit,
    exitTime: String,
    onExitTimeChange: (String) -> Unit,
    exitDate: String,
    onExitDateChange: (String) -> Unit,
    status: String,
    onStatusChange: (String) -> Unit
) {
    InfoCard(
        mainColor = MaterialTheme.colorScheme.tertiary,
        title = "زمان‌بندی",
        icon = Icons.Default.Schedule,
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = entryTime,
                    onValueChange = onEntryTimeChange,
                    label = { Text("ساعت ورود") },
                    leadingIcon = {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Login, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = exitTime,
                    onValueChange = onExitTimeChange,
                    label = { Text("ساعت خروج") },
                    leadingIcon = {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = exitDate,
                    onValueChange = onExitDateChange,
                    label = { Text("تاریخ خروج") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = status,
                    onValueChange = onStatusChange,
                    label = { Text("وضعیت") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    )
}
