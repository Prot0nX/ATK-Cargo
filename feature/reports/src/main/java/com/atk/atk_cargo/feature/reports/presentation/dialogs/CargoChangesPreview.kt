package com.atk.atk_cargo.feature.reports.presentation.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.atk.atk_cargo.data.model.CargoInfo

// پیش‌نمایش فهرست تغییرات اعمال‌شده روی حواله قبل از ذخیره — از CargoEditSearchDialogsSection.kt جدا شد (فاز۴ #۴۰)
@Composable
internal fun CargoChangesPreview(
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
    editedLoadingQuotaNumber: String
) {
    val changes = remember(
        cargoInfo,
        editedTrackingNumber,
        editedNumberOfPeople,
        editedEntryTime,
        editedNetWeight,
        editedScaleReceiptNumber,
        editedShortageWeight,
        editedExcessWeight,
        editedExitTime,
        editedExitDate,
        editedStatus,
        editedLoadingQuotaNumber
    ) {
        mutableListOf<Triple<String, String, String>>().apply {
            if (cargoInfo.trackingNumber != editedTrackingNumber) {
                add(Triple("شماره حواله", cargoInfo.trackingNumber, editedTrackingNumber))
            }
            if (cargoInfo.numberOfPeople != editedNumberOfPeople) {
                add(Triple("تعداد نفرات", cargoInfo.numberOfPeople, editedNumberOfPeople))
            }
            if (cargoInfo.entryTime != editedEntryTime) {
                add(Triple("زمان ورود", cargoInfo.entryTime, editedEntryTime))
            }
            if (cargoInfo.netWeight != editedNetWeight) {
                add(Triple("وزن خالص", cargoInfo.netWeight, editedNetWeight))
            }
            if (cargoInfo.scaleReceiptNumber != editedScaleReceiptNumber) {
                add(Triple("شماره قبض باسکول", cargoInfo.scaleReceiptNumber, editedScaleReceiptNumber))
            }
            if (cargoInfo.shortageWeight != editedShortageWeight) {
                add(Triple("وزن کسری", cargoInfo.shortageWeight, editedShortageWeight))
            }
            if (cargoInfo.excessWeight != editedExcessWeight) {
                add(Triple("وزن اضافی", cargoInfo.excessWeight, editedExcessWeight))
            }
            if ((cargoInfo.exitTime ?: "") != editedExitTime) {
                add(Triple("زمان خروج", cargoInfo.exitTime ?: "", editedExitTime))
            }
            if ((cargoInfo.exitDate ?: "") != editedExitDate) {
                add(Triple("تاریخ خروج", cargoInfo.exitDate ?: "", editedExitDate))
            }
            if (cargoInfo.status != editedStatus) {
                add(Triple("وضعیت", cargoInfo.status, editedStatus))
            }
            if (cargoInfo.loadingQuotaNumber != editedLoadingQuotaNumber) {
                add(Triple("شماره کوتاژ", cargoInfo.loadingQuotaNumber, editedLoadingQuotaNumber))
            }
        }
    }

    if (changes.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "تغییرات اعمال شده (${changes.size} مورد)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                changes.forEach { (field, oldValue, newValue) ->
                    ChangeItem(
                        fieldName = field,
                        oldValue = oldValue,
                        newValue = newValue
                    )
                }
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "هیچ تغییری اعمال نشده است",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChangeItem(
    fieldName: String,
    oldValue: String,
    newValue: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = fieldName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = oldValue.ifEmpty { "خالی" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = newValue.ifEmpty { "خالی" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
