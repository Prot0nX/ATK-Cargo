package com.atk.atk_cargo.feature.cargo_entry.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atk.atk_cargo.data.model.ActiveShipInfo
import com.atk.atk_cargo.data.model.MatchingQuota

@Composable
fun QuotaSelectionDialog(
    matchingQuotas: List<MatchingQuota>,
    ship: ActiveShipInfo,
    onQuotaSelected: (MatchingQuota) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.80f)
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
 // Header Section
                DialogHeader(ship = ship, quotaCount = matchingQuotas.size, onDismiss = onDismiss)

                Spacer(modifier = Modifier.height(12.dp))

 // فهرست کوتاژها.
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(matchingQuotas, key = { "${it.quotaNumber}_${it.shipName}_${it.warehouse}_${it.cargoType}" }) { quota ->
                        QuotaItem(
                            quota = quota,
                            matchingQuotas = matchingQuotas,
                            onClick = onQuotaSelected
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogHeader(
    ship: ActiveShipInfo,
    quotaCount: Int,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "بستن",
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "انتخاب دقیق کوتاژ منطبق",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(36.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DirectionsBoat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ship.shipName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$quotaCount کوتاژ مشابه برای این شماره یافت شد",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun QuotaItem(
    quota: MatchingQuota,
    matchingQuotas: List<MatchingQuota>,
    onClick: (MatchingQuota) -> Unit
) {
    val differentFields = findDifferentFields(quota, matchingQuotas)
    val isActive = quota.isActive

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isActive) { onClick(quota) }
            .alpha(if (isActive) 1f else 0.65f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !isActive -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                differentFields.isNotEmpty() -> MaterialTheme.colorScheme.surface
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }
        ),
        border = BorderStroke(
            1.dp,
            when {
                !isActive -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                differentFields.isNotEmpty() -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            QuotaHeader(
                quota = quota,
                isActive = isActive,
                hasDifferences = differentFields.isNotEmpty()
            )

            Spacer(modifier = Modifier.height(8.dp))
            QuotaDetails(quota, differentFields, isActive)

            if (!isActive) {
                Spacer(modifier = Modifier.height(8.dp))
                InactiveIndicator()
            }
        }
    }
}

@Composable
private fun InactiveIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "این کوتاژ غیرفعال است و امکان ثبت حواله ندارد",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun QuotaHeader(
    quota: MatchingQuota,
    isActive: Boolean,
    hasDifferences: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "کوتاژ: ${quota.quotaNumber}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            if (hasDifferences && isActive) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "دارای تفاوت فیلد",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        if (isActive) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "انتخاب",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuotaDetails(
    quota: MatchingQuota,
    differentFields: Set<String>,
    isActive: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        QuotaDetailItem(
            icon = Icons.Default.Business,
            label = "شرکت باربری",
            value = quota.shippingCompany,
            isDifferent = "shippingCompany" in differentFields,
            isDisabled = !isActive
        )
        QuotaDetailItem(
            icon = Icons.Default.Warehouse,
            label = "انبار بارگیری",
            value = quota.warehouse,
            isDifferent = "warehouse" in differentFields,
            isDisabled = !isActive
        )
        QuotaDetailItem(
            icon = Icons.Default.Category,
            label = "نوع کالا",
            value = quota.cargoType,
            isDifferent = "cargoType" in differentFields,
            isDisabled = !isActive
        )
    }
}

@Composable
private fun QuotaDetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    isDifferent: Boolean,
    isDisabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isDifferent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDifferent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(90.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isDifferent) FontWeight.ExtraBold else FontWeight.Medium
            ),
            color = if (isDifferent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun findDifferentFields(
    currentQuota: MatchingQuota,
    allQuotas: List<MatchingQuota>
): Set<String> {
    val differentFields = mutableSetOf<String>()
    val sameNumberQuotas = allQuotas.filter { it.quotaNumber == currentQuota.quotaNumber }

    if (sameNumberQuotas.size > 1) {
        if (sameNumberQuotas.map { it.shippingCompany }.distinct().size > 1) differentFields.add("shippingCompany")
        if (sameNumberQuotas.map { it.warehouse }.distinct().size > 1) differentFields.add("warehouse")
        if (sameNumberQuotas.map { it.cargoType }.distinct().size > 1) differentFields.add("cargoType")
    }

    return differentFields
}
