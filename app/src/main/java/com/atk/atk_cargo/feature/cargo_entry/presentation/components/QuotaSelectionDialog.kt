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
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atk.atk_cargo.api.ActiveShipInfo
import com.atk.atk_cargo.api.MatchingQuota

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
                .fillMaxHeight(0.75f)
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header Section
                DialogHeader(ship = ship, quotaCount = matchingQuotas.size, onDismiss = onDismiss)

                // Quotas List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(matchingQuotas) { quota ->
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
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "بستن",
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "انتخاب کوتاژ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(36.dp))
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DirectionsBoat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = ship.shipName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.size(20.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$quotaCount کوتاژ مشابه یافت شد",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (quotaCount > 1) {
                Spacer(modifier = Modifier.width(8.dp))

                // راهنمای رنگ‌ها به صورت خلاصه در یک خط
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(10.dp)
                        )

                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.secondary, CircleShape)
                        )

                        Text(
                            text = "موارد متفاوت",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp
                        )
                    }
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
            .alpha(if (isActive) 1f else 0.7f),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !isActive -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                differentFields.isNotEmpty() -> MaterialTheme.colorScheme.surface
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        border = when {
            !isActive -> BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            )
            differentFields.isNotEmpty() -> BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
            else -> null
        },
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            QuotaHeader(
                quota = quota,
                isActive = isActive,
                hasDifferences = differentFields.isNotEmpty()
            )

            if (differentFields.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${differentFields.size} فیلد متفاوت",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            QuotaDetails(quota, differentFields, isActive)

            // نشانگر غیرفعال بودن
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
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                RoundedCornerShape(4.dp)
            )
            .padding(6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "این کوتاژ در حال حاضر قابل انتخاب نیست",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun QuotaDetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    isDifferent: Boolean,
    isDisabled: Boolean,
    fieldType: String
) {
    val differenceColor = when (fieldType) {
        "shippingCompany" -> MaterialTheme.colorScheme.primary
        "warehouse" -> MaterialTheme.colorScheme.tertiary
        "cargoType" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(if (isDifferent) 6.dp else 0.dp))
            .background(
                if (isDifferent) differenceColor.copy(alpha = 0.08f) else Color.Transparent
            )
            .padding(if (isDifferent) 6.dp else 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = when {
                isDisabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                isDifferent -> differenceColor
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            },
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                isDisabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                isDifferent -> differenceColor
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            },
            fontWeight = if (isDifferent) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.width(70.dp),
            fontSize = 12.sp
        )

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (isDifferent) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isDisabled) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    } else {
                        differenceColor.copy(alpha = 0.12f)
                    },
                    border = BorderStroke(
                        width = 0.5.dp,
                        color = differenceColor.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(differenceColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (isDisabled) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            } else {
                                differenceColor
                            },
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDisabled) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontSize = 13.sp
                )
            }
        }
    }
}

private fun findDifferentFields(
    currentQuota: MatchingQuota,
    allQuotas: List<MatchingQuota>
): Set<String> {
    val differentFields = mutableSetOf<String>()
    val sameNumberQuotas = allQuotas.filter { it.quotaNumber == currentQuota.quotaNumber }

    if (sameNumberQuotas.size > 1) {
        if (sameNumberQuotas.map { it.shippingCompany }.distinct().size > 1) {
            differentFields.add("shippingCompany")
        }

        if (sameNumberQuotas.map { it.warehouse }.distinct().size > 1) {
            differentFields.add("warehouse")
        }

        if (sameNumberQuotas.map { it.cargoType }.distinct().size > 1) {
            differentFields.add("cargoType")
        }
    }

    return differentFields
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = when {
                    !isActive -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                    hasDifferences -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    else -> Color.Transparent
                },
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = quota.quotaNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        !isActive -> MaterialTheme.colorScheme.error
                        hasDifferences -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            when {
                !isActive -> {
                    StatusBadge(
                        text = "غیرفعال",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                hasDifferences -> {
                    StatusBadge(
                        text = "متفاوت",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (isActive) {
            Surface(
                shape = CircleShape,
                color = if (hasDifferences) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "انتخاب",
                        tint = if (hasDifferences) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(
    text: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.1f),
        contentColor = color,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 9.sp
        )
    }
}

@Composable
private fun QuotaDetails(
    quota: MatchingQuota,
    differentFields: Set<String>,
    isActive: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        QuotaDetailItem(
            icon = Icons.Default.Business,
            label = "شرکت باربری",
            value = quota.shippingCompany,
            isDifferent = "shippingCompany" in differentFields,
            isDisabled = !isActive,
            fieldType = "shippingCompany"
        )
        QuotaDetailItem(
            icon = Icons.Default.Warehouse,
            label = "انبار",
            value = quota.warehouse,
            isDifferent = "warehouse" in differentFields,
            isDisabled = !isActive,
            fieldType = "warehouse"
        )
        QuotaDetailItem(
            icon = Icons.Default.Category,
            label = "نوع کالا",
            value = quota.cargoType,
            isDifferent = "cargoType" in differentFields,
            isDisabled = !isActive,
            fieldType = "cargoType"
        )
    }
}
