package com.atk.atk_cargo.feature.reports.presentation.ships.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atk.atk_cargo.api.Ship
import com.atk.atk_cargo.api.ShipSortingMode
import com.atk.atk_cargo.feature.reports.domain.formatNumber
import com.atk.atk_cargo.ui.theme.Blue700
import com.atk.atk_cargo.ui.theme.DeepOrange100
import com.atk.atk_cargo.ui.theme.DeepOrange900
import com.atk.atk_cargo.ui.theme.Teal900

@Composable
fun ShipCard(
    ship: Ship,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (isActive) Teal900.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        ShipCardContent(ship = ship)
    }
}

@Composable
private fun ShipCardContent(
    ship: Ship,
    modifier: Modifier = Modifier
) {
    val loadedTonnage = ship.totalTonnage - ship.remainingTonnage

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(14.dp)
    ) {
        // ردیف اول: نام کشتی | محموله + مقدار کل
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "کل:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatNumber(ship.totalTonnage.toInt()),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // نام کشتی | محموله در سمت راست
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.End
            ) {
                if (!ship.cargoType.isNullOrBlank()) {
                    Text(
                        text = ship.cargoType,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Teal900
                    )
                    Text(
                        text = "  |  ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                Text(
                    text = ship.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ردیف دوم: بارگیری و مانده
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // بارگیری
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = DeepOrange100.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 11.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "بارگیری:",
                        style = MaterialTheme.typography.labelSmall,
                        color = DeepOrange900.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "↑ ${formatNumber(loadedTonnage.toInt())}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepOrange900
                    )
                }
            }

            // مانده
            StatBox(
                value = "↓ ${formatNumber(ship.remainingTonnage.toInt())}",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatBox(
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 11.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "مانده:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun sortShips(ships: List<Ship>, sortingMode: ShipSortingMode): List<Ship> {
    return when (sortingMode) {
        ShipSortingMode.REMAINING_TONNAGE_ASC -> ships.sortedBy { it.remainingTonnage }
        ShipSortingMode.REMAINING_TONNAGE_DESC -> ships.sortedByDescending { it.remainingTonnage }
        ShipSortingMode.LOADED_TONNAGE_ASC -> ships.sortedBy { it.totalTonnage - it.remainingTonnage }
        ShipSortingMode.LOADED_TONNAGE_DESC -> ships.sortedByDescending { it.totalTonnage - it.remainingTonnage }
        ShipSortingMode.NAME_ASC -> ships.sortedBy { it.name }
        ShipSortingMode.NAME_DESC -> ships.sortedByDescending { it.name }
    }
}

@Composable
fun ShipSortingSelector(
    currentMode: ShipSortingMode,
    onModeChange: (ShipSortingMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShipSortingModeButton(
            text = "تناژ مانده",
            icon = if (currentMode == ShipSortingMode.REMAINING_TONNAGE_ASC) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
            isSelected = currentMode == ShipSortingMode.REMAINING_TONNAGE_ASC || currentMode == ShipSortingMode.REMAINING_TONNAGE_DESC,
            onClick = {
                val newMode = if (currentMode == ShipSortingMode.REMAINING_TONNAGE_ASC) {
                    ShipSortingMode.REMAINING_TONNAGE_DESC
                } else {
                    ShipSortingMode.REMAINING_TONNAGE_ASC
                }
                onModeChange(newMode)
            }
        )

        ShipSortingModeButton(
            text = "تناژ بارگیری",
            icon = if (currentMode == ShipSortingMode.LOADED_TONNAGE_ASC) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
            isSelected = currentMode == ShipSortingMode.LOADED_TONNAGE_ASC || currentMode == ShipSortingMode.LOADED_TONNAGE_DESC,
            onClick = {
                val newMode = if (currentMode == ShipSortingMode.LOADED_TONNAGE_ASC) {
                    ShipSortingMode.LOADED_TONNAGE_DESC
                } else {
                    ShipSortingMode.LOADED_TONNAGE_ASC
                }
                onModeChange(newMode)
            }
        )

        ShipSortingModeButton(
            text = "ترتیب اسم",
            trailingLabel = if (currentMode == ShipSortingMode.NAME_DESC) "Z-A" else "A-Z",
            isSelected = currentMode == ShipSortingMode.NAME_ASC || currentMode == ShipSortingMode.NAME_DESC,
            onClick = {
                val newMode = if (currentMode == ShipSortingMode.NAME_ASC) {
                    ShipSortingMode.NAME_DESC
                } else {
                    ShipSortingMode.NAME_ASC
                }
                onModeChange(newMode)
            }
        )
    }
}

@Composable
private fun ShipSortingModeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    trailingLabel: String? = null
) {
    val contentColor = if (isSelected) Blue700 else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 11.sp
        )
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(13.dp)
            )
        }
        if (trailingLabel != null) {
            Text(
                text = trailingLabel,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}
