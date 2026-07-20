package com.atk.atk_cargo.feature.reports.presentation.ships.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atk.atk_cargo.api.Ship
import com.atk.atk_cargo.api.ShipSortingMode
import com.atk.atk_cargo.feature.reports.domain.formatNumber
import com.atk.atk_cargo.ui.theme.BackgroundDark
import com.atk.atk_cargo.ui.theme.Blue400
import com.atk.atk_cargo.ui.theme.Corner2XL
import com.atk.atk_cargo.ui.theme.PrimaryBlueDark
import com.atk.atk_cargo.ui.theme.Red400
import com.atk.atk_cargo.ui.theme.Slate300
import com.atk.atk_cargo.ui.theme.TextSecondaryDark

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
            .padding(vertical = 2.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(Corner2XL),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
        ),
        tonalElevation = 1.dp
    ) {
        ShipCardContent(
            ship = ship,
            isActive = isActive
        )
    }
}

@Composable
private fun ShipCardContent(
    ship: Ship,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val loadedTonnage = ship.totalTonnage - ship.remainingTonnage
    val isDarkTheme = isSystemInDarkTheme()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(0.35f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatBox(
                label = "مانده:",
                value = formatNumber(ship.remainingTonnage.toInt()),
                icon = Icons.Default.ArrowDownward,
                backgroundColor = if (isDarkTheme) BackgroundDark.copy(alpha = 0.8f) 
                else if (isActive) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) 
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                contentColor = if (isDarkTheme) Slate300 else MaterialTheme.colorScheme.onSurface,
                labelColor = if (isDarkTheme) TextSecondaryDark else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            StatBox(
                label = "بارگیری:",
                value = formatNumber(loadedTonnage.toInt()),
                icon = Icons.Default.ArrowUpward,
                backgroundColor = if (isDarkTheme) MaterialTheme.colorScheme.error.copy(alpha = 0.1f) 
                else MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.error,
                labelColor = if (isDarkTheme) Red400 else MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                borderColor = if (isDarkTheme) MaterialTheme.colorScheme.error.copy(alpha = 0.1f) else Color.Transparent
            )
        }

        Column(
            modifier = Modifier.weight(0.60f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (ship.cargoType.isNullOrBlank()) ship.name else "${ship.cargoType} | ${ship.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if (isDarkTheme) MaterialTheme.colorScheme.onSurface 
                    else if (isActive) PrimaryBlueDark 
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Left
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth(0.55f)
            ) {
                StatBox(
                    label = "کل:",
                    value = formatNumber(ship.totalTonnage.toInt()),
                    icon = Icons.Default.Scale,
                    backgroundColor = if (isDarkTheme) BackgroundDark.copy(alpha = 0.6f) 
                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    contentColor = if (isDarkTheme) Blue400 else PrimaryBlueDark,
                    labelColor = if (isDarkTheme) TextSecondaryDark else MaterialTheme.colorScheme.onSurfaceVariant,
                    borderColor = if (isDarkTheme) Color.White.copy(alpha = 0.1f) 
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    labelColor: Color,
    borderColor: Color = Color.Transparent
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        border = if (borderColor != Color.Transparent) BorderStroke(1.dp, borderColor) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                fontWeight = FontWeight.Bold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (icon == Icons.Default.Scale) MaterialTheme.colorScheme.primary else labelColor,
                    modifier = Modifier.size(12.dp)
                )
            }
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
            icon = Icons.AutoMirrored.Filled.Sort,
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
            icon = Icons.Default.SortByAlpha,
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
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 8.dp),
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
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(14.dp)
        )
    }
}
