package com.atk.atk_cargo.feature.reports.presentation.dialogs

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atk.atk_cargo.data.model.CargoInfo
import com.atk.atk_cargo.feature.reports.viewmodel.ReportsViewModel
import kotlinx.coroutines.launch

// دیالوگ نتیجه‌ی جستجوی حواله (مشاهده/ویرایش) — از CargoEditSearchDialogsSection.kt جدا شد
@Composable
fun SearchResultDialog(
    cargoInfo: CargoInfo,
    onDismiss: () -> Unit,
    onRefresh: (CargoInfo) -> Unit,
    searchType: SearchType?,
    searchValue: String?,
    viewModel: ReportsViewModel
) {
    val mainColor = MaterialTheme.colorScheme.primary
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var isEditMode by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    var editedTrackingNumber by remember { mutableStateOf(cargoInfo.trackingNumber) }
    var editedNumberOfPeople by remember { mutableStateOf(cargoInfo.numberOfPeople) }
    var editedEntryTime by remember { mutableStateOf(cargoInfo.entryTime) }
    var editedNetWeight by remember { mutableStateOf(cargoInfo.netWeight) }
    var editedScaleReceiptNumber by remember { mutableStateOf(cargoInfo.scaleReceiptNumber) }
    var editedShortageWeight by remember { mutableStateOf(cargoInfo.shortageWeight) }
    var editedExcessWeight by remember { mutableStateOf(cargoInfo.excessWeight) }
    var editedExitTime by remember { mutableStateOf(cargoInfo.exitTime ?: "") }
    var editedExitDate by remember { mutableStateOf(cargoInfo.exitDate ?: "") }
    var editedStatus by remember { mutableStateOf(cargoInfo.status) }
    var editedLoadingQuotaNumber by remember { mutableStateOf(cargoInfo.loadingQuotaNumber) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(16.dp)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.9f)
                    .padding(paddingValues)
                    .padding(16.dp)
                    .animateContentSize(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = mainColor
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
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
                                    .size(48.dp)
                                    .background(mainColor.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isEditMode) Icons.Default.Edit else Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = mainColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isEditMode) "ویرایش اطلاعات" else "نتیجه جستجو",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "قبض باسکول: ${if (isEditMode) editedScaleReceiptNumber else cargoInfo.scaleReceiptNumber}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .background(mainColor.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "بستن",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            if (isEditMode) {
                                EditableCargoMainInfo(
                                    trackingNumber = editedTrackingNumber,
                                    onTrackingNumberChange = { editedTrackingNumber = it },
                                    scaleReceiptNumber = editedScaleReceiptNumber,
                                    onScaleReceiptNumberChange = { editedScaleReceiptNumber = it },
                                    loadingQuotaNumber = editedLoadingQuotaNumber,
                                    onLoadingQuotaNumberChange = { editedLoadingQuotaNumber = it },
                                    numberOfPeople = editedNumberOfPeople,
                                    onNumberOfPeopleChange = { editedNumberOfPeople = it }
                                )
                            } else {
                                CargoMainInfo(cargoInfo = cargoInfo)
                            }
                        }

                        item {
                            if (isEditMode) {
                                EditableCargoWeightInfo(
                                    netWeight = editedNetWeight,
                                    onNetWeightChange = { editedNetWeight = it },
                                    shortageWeight = editedShortageWeight,
                                    onShortageWeightChange = { editedShortageWeight = it },
                                    excessWeight = editedExcessWeight,
                                    onExcessWeightChange = { editedExcessWeight = it }
                                )
                            } else {
                                CargoWeightInfo(cargoInfo = cargoInfo)
                            }
                        }

                        item {
                            if (isEditMode) {
                                EditableCargoTimeInfo(
                                    entryTime = editedEntryTime,
                                    onEntryTimeChange = { editedEntryTime = it },
                                    exitTime = editedExitTime,
                                    onExitTimeChange = { editedExitTime = it },
                                    exitDate = editedExitDate,
                                    onExitDateChange = { editedExitDate = it },
                                    status = editedStatus,
                                    onStatusChange = { editedStatus = it }
                                )
                            } else {
                                CargoTimeInfo(cargoInfo = cargoInfo)
                            }
                        }

                        if (!isEditMode) {
                            item {
                                CargoShippingInfo(cargoInfo = cargoInfo)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isEditMode) {
                            OutlinedButton(
                                onClick = {
                                    isEditMode = false
                                    editedTrackingNumber = cargoInfo.trackingNumber
                                    editedNumberOfPeople = cargoInfo.numberOfPeople
                                    editedEntryTime = cargoInfo.entryTime
                                    editedNetWeight = cargoInfo.netWeight
                                    editedScaleReceiptNumber = cargoInfo.scaleReceiptNumber
                                    editedShortageWeight = cargoInfo.shortageWeight
                                    editedExcessWeight = cargoInfo.excessWeight
                                    editedExitTime = cargoInfo.exitTime ?: ""
                                    editedExitDate = cargoInfo.exitDate ?: ""
                                    editedStatus = cargoInfo.status
                                    editedLoadingQuotaNumber = cargoInfo.loadingQuotaNumber
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("لغو")
                            }

                            Button(
                                onClick = { showConfirmDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ذخیره")
                            }
                        } else {
                            Button(
                                onClick = { isEditMode = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ویرایش")
                            }

                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("بستن")
                            }
                        }
                    }
                }
            }
        }

        if (showConfirmDialog) {
            CargoEditConfirmDialog(
                cargoInfo = cargoInfo,
                editedTrackingNumber = editedTrackingNumber,
                editedNumberOfPeople = editedNumberOfPeople,
                editedEntryTime = editedEntryTime,
                editedNetWeight = editedNetWeight,
                editedScaleReceiptNumber = editedScaleReceiptNumber,
                editedShortageWeight = editedShortageWeight,
                editedExcessWeight = editedExcessWeight,
                editedExitTime = editedExitTime,
                editedExitDate = editedExitDate,
                editedStatus = editedStatus,
                editedLoadingQuotaNumber = editedLoadingQuotaNumber,
                onDismiss = { showConfirmDialog = false },
                onConfirm = {
                    showConfirmDialog = false
                    isSaving = true

                    val updatedCargo = cargoInfo.copy(
                        trackingNumber = editedTrackingNumber,
                        numberOfPeople = editedNumberOfPeople,
                        entryTime = editedEntryTime,
                        netWeight = editedNetWeight,
                        scaleReceiptNumber = editedScaleReceiptNumber,
                        shortageWeight = editedShortageWeight,
                        excessWeight = editedExcessWeight,
                        exitTime = editedExitTime.ifEmpty { null },
                        exitDate = editedExitDate.ifEmpty { null },
                        status = editedStatus,
                        loadingQuotaNumber = editedLoadingQuotaNumber
                    )

                    Log.d("CargoEdit", "🔄 شروع بروزرسانی - ID: ${updatedCargo.id}")
                    Log.d("CargoEdit", "📦 داده‌های ویرایش شده: $updatedCargo")

                    viewModel.updateCargoInfo(updatedCargo) { result ->
                        isSaving = false
                        result.fold(
                            onSuccess = { response ->
                                Log.d("CargoEdit", "✅ پاسخ موفق: $response")
                                if (response.error == false) {
                                    isEditMode = false
                                    if (searchType != null && searchValue != null) {
                                        when (searchType) {
                                            SearchType.RECEIPT_NUMBER -> {
                                                viewModel.performAdvancedSearch(searchValue) { searchResult ->
                                                    searchResult.fold(
                                                        onSuccess = { refreshedCargo ->
                                                            if (refreshedCargo != null) {
                                                                onRefresh(refreshedCargo)
                                                                scope.launch {
                                                                    snackbarHostState.showSnackbar(
                                                                        message = "✅ ${response.message}",
                                                                        duration = SnackbarDuration.Short
                                                                    )
                                                                }
                                                            }
                                                        },
                                                        onFailure = {
                                                            scope.launch {
                                                                    snackbarHostState.showSnackbar(
                                                                        message = "بروزرسانی انجام شد اما خطا در دریافت اطلاعات جدید",
                                                                        duration = SnackbarDuration.Long
                                                                    )
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                            SearchType.TRACKING_NUMBER -> {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "✅ ${response.message}",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "✅ ${response.message}",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                } else {
                                    Log.e("CargoEdit", "❌ خطا در response: ${response.message}")
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "❌ ${response.message}",
                                            duration = SnackbarDuration.Long
                                        )
                                    }
                                }
                            },
                            onFailure = { error ->
                                Log.e("CargoEdit", "💥 Exception: ${error.message}", error)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "❌ خطا: ${error.localizedMessage}",
                                        duration = SnackbarDuration.Long
                                    )
                                }
                            }
                        )
                    }
                }
            )
        }
    }
}
