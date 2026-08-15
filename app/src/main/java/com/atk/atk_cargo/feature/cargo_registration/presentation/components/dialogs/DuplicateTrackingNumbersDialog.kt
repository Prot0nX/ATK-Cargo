package com.atk.atk_cargo.feature.cargo_registration.presentation.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DuplicateTrackingNumbersDialog(
    duplicateNumbers: List<String>,
    onDismiss: () -> Unit,
    onSearchTrackingNumber: (String) -> Unit = {}
) {
    var selectedTrackingNumber by remember { mutableStateOf<String?>(null) }

    StandardDialogShell(onDismissRequest = onDismiss) {
        DialogBadge(icon = Icons.Default.ContentCopy, tint = MaterialTheme.colorScheme.error)

        Spacer(modifier = Modifier.height(16.dp))

        DialogTitle(text = "حواله‌های تکراری", color = MaterialTheme.colorScheme.onSurface)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${duplicateNumbers.size} شماره حواله تکراری",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(DialogContentCornerRadius),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(duplicateNumbers) { trackingNumber ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTrackingNumber = trackingNumber
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedTrackingNumber == trackingNumber) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        tonalElevation = if (selectedTrackingNumber == trackingNumber) 2.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        MaterialTheme.colorScheme.error,
                                        CircleShape
                                    )
                            )

                            Text(
                                text = trackingNumber,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (selectedTrackingNumber == trackingNumber) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                modifier = Modifier.weight(1f)
                            )

                            if (selectedTrackingNumber == trackingNumber) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        DialogButtonRow(
            primaryText = "جستجو",
            onPrimaryClick = {
                selectedTrackingNumber?.let { trackingNumber ->
                    onSearchTrackingNumber(trackingNumber)
                    onDismiss()
                }
            },
            primaryEnabled = selectedTrackingNumber != null,
            primaryIcon = Icons.Default.Search,
            secondaryText = "بستن",
            onSecondaryClick = onDismiss
        )
    }
}
