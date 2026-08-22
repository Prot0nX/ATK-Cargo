package com.atk.atk_cargo.feature.reports.presentation.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atk.atk_cargo.data.model.CargoInfo

// دیالوگ تأیید نهایی قبل از ذخیره‌ی ویرایش حواله — از CargoEditSearchDialogsSection.kt جدا شد (فاز۴ #۴۰)
@Composable
fun CargoEditConfirmDialog(
    cargoInfo: CargoInfo,
    editedTrackingNumber: String,
    editedNumberOfPeople: String,
    editedEntryTime: String,
    editedNetWeight: String,
    editedScaleReceiptNumber: String,
    editedShortageWeight: String,
    editedExcessWeight: String,
    editedExitTime: String,
    editedExitDate: String,
    editedStatus: String,
    editedLoadingQuotaNumber: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
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
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .heightIn(max = 700.dp)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                CargoEditConfirmHeader()

                Spacer(modifier = Modifier.height(24.dp))

                CargoChangesPreview(
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
                    editedLoadingQuotaNumber = editedLoadingQuotaNumber
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text("تایید و ذخیره")
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text("انصراف")
                        }
                    }
                }
            }
        }
    }
}
