package com.atk.atk_cargo.feature.cargo_entry.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atk.atk_cargo.api.ActiveShipInfo
import com.atk.atk_cargo.ui.theme.ATKCargoTheme

private val ShipDialogAccent: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF2DD4BF) else Color(0xFF0D9488)

private val ShipDialogOnAccent: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF042F2E) else Color.White

private val ShipDialogAccentBg: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF134E4A) else Color(0xFFDCEFEA)

private val ShipDialogAccentBorder: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF1F6F63) else Color(0xFFB9DED7)

private val ShipDialogCardBorder: Color
    @Composable get() = MaterialTheme.colorScheme.outlineVariant

private val ShipDialogMutedBg: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

private val ShipDialogMutedText: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

private val ShipDialogTitleColor: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface

private val ShipDialogGradientBottom: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF0F2E2A) else Color(0xFFF2FAF8)

private val ShipDialogGradientTop: Color
    @Composable get() = MaterialTheme.colorScheme.surface

private val ShipDialogFieldBorder: Color
    @Composable get() = MaterialTheme.colorScheme.outlineVariant

private val ShipDialogCancelText: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

@Composable
fun ShipSelectionDialog(
    ships: List<ActiveShipInfo>,
    selectedShipNames: Set<String>,
    onSelectShip: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val groupedShips = remember(ships) { ships.groupBy { it.shipName } }
    var selectedShips by remember { mutableStateOf(selectedShipNames) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCargoFilter by remember { mutableStateOf<String?>(null) }

    val cargoTypes = remember(ships) {
        ships.map { it.cargoType }.filter { it.isNotBlank() }.distinct()
    }

    val filteredShips = remember(groupedShips, searchQuery, selectedCargoFilter) {
        groupedShips.entries.filter { (shipName, shipList) ->
            val matchesQuery = searchQuery.isEmpty() ||
                shipName.contains(searchQuery, ignoreCase = true) ||
                shipList.any { it.loadingWarehouse.contains(searchQuery, ignoreCase = true) }
            val matchesCargo = selectedCargoFilter == null || shipList.any { it.cargoType == selectedCargoFilter }
            matchesQuery && matchesCargo
        }.sortedWith(
            compareByDescending<Map.Entry<String, List<ActiveShipInfo>>> { selectedShips.contains(it.key) }
                .thenBy { it.key }
        )
    }

    val palette = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        ATKCargoTheme.semanticColors.cargoExit,
        ATKCargoTheme.semanticColors.warning,
        ATKCargoTheme.semanticColors.cargoEntry
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(listOf(ShipDialogGradientTop, ShipDialogGradientBottom)),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Title Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(ShipDialogAccentBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBoat,
                            contentDescription = null,
                            tint = ShipDialogAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = "انتخاب و مدیریت کشتی‌ها",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = ShipDialogTitleColor
                        )
                        Text(
                            text = "${selectedShips.size} از ${groupedShips.size} کشتی انتخاب شده",
                            style = MaterialTheme.typography.labelSmall,
                            color = ShipDialogMutedText
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ShipDialogMutedBg)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "بستن",
                            tint = ShipDialogTitleColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = ShipDialogAccent,
                        unfocusedBorderColor = ShipDialogFieldBorder
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "جستجو",
                            tint = ShipDialogAccent
                        )
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable { searchQuery = "" },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Clear, "پاک کردن", tint = ShipDialogMutedText, modifier = Modifier.size(16.dp))
                            }
                        }
                    } else null,
                    placeholder = {
                        Text("جستجوی نام کشتی یا انبار...", style = MaterialTheme.typography.bodyMedium)
                    },
                    singleLine = true
                )

                if (cargoTypes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val chipColors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = ShipDialogMutedText,
                        iconColor = ShipDialogMutedText,
                        selectedContainerColor = ShipDialogAccentBg,
                        selectedLabelColor = ShipDialogAccent,
                        selectedLeadingIconColor = ShipDialogAccent
                    )
                    val chipBorder = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = false,
                        borderColor = ShipDialogFieldBorder,
                        selectedBorderColor = ShipDialogAccentBorder
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCargoFilter == null,
                                onClick = { selectedCargoFilter = null },
                                label = { Text("همه کالاها", style = MaterialTheme.typography.labelMedium) },
                                leadingIcon = {
                                    Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(14.dp))
                                },
                                colors = chipColors,
                                border = chipBorder
                            )
                        }
                        items(cargoTypes) { cargo ->
                            FilterChip(
                                selected = selectedCargoFilter == cargo,
                                onClick = {
                                    selectedCargoFilter = if (selectedCargoFilter == cargo) null else cargo
                                },
                                label = { Text(cargo, style = MaterialTheme.typography.labelMedium) },
                                colors = chipColors,
                                border = chipBorder
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Ships List
                if (filteredShips.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "هیچ کشتی مطابق با جستجو پیدا نشد",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ShipDialogMutedText
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredShips, key = { it.key }) { (shipName, shipList) ->
                            val isSelected = selectedShips.contains(shipName)
                            val shipIndex = groupedShips.keys.indexOf(shipName)
                            val shipColor = palette.getOrElse(if (shipIndex >= 0) shipIndex % palette.size else 0) { ShipDialogAccent }

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) shipColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) shipColor.copy(alpha = 0.6f) else ShipDialogCardBorder
                                ),
                                onClick = {
                                    selectedShips = if (isSelected) selectedShips - shipName else selectedShips + shipName
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(shipColor.copy(alpha = 0.16f), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsBoat,
                                            contentDescription = null,
                                            tint = shipColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = shipName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ShipDialogTitleColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        val cargo = shipList.firstOrNull()?.cargoType ?: ""
                                        val warehouse = shipList.firstOrNull()?.loadingWarehouse ?: ""
                                        Text(
                                            text = "${shipList.size} کوتاژ فعال | $cargo ($warehouse)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ShipDialogMutedText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            selectedShips = if (checked) selectedShips + shipName else selectedShips - shipName
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = shipColor,
                                            uncheckedColor = ShipDialogMutedText.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(if (selectedShips.isNotEmpty()) ShipDialogAccent else ShipDialogAccent.copy(alpha = 0.4f))
                            .clickable(enabled = selectedShips.isNotEmpty()) {
                                onSelectShip(selectedShips)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = ShipDialogOnAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تایید انتخاب (${selectedShips.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ShipDialogOnAccent)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(0.8f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(ShipDialogMutedBg)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("انصراف", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = ShipDialogCancelText)
                    }
                }
            }
        }
    }
}
