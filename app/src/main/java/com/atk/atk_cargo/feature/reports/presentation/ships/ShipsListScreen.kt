package com.atk.atk_cargo.feature.reports.presentation.ships

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.atk.atk_cargo.api.ShiftInfo
import com.atk.atk_cargo.api.Ship
import com.atk.atk_cargo.feature.reports.presentation.components.EmptyShipsState
import com.atk.atk_cargo.feature.reports.presentation.dialogs.RealTimeLoadingBottomSheet
import com.atk.atk_cargo.feature.reports.presentation.ships.components.ShipCard
import com.atk.atk_cargo.feature.reports.presentation.ships.components.ShipSortingSelector
import com.atk.atk_cargo.feature.reports.presentation.ships.components.sortShips
import com.atk.atk_cargo.ui.theme.Blue400
import com.atk.atk_cargo.ui.theme.Blue700
import com.atk.atk_cargo.ui.viewmodel.ReportsViewModel

private val ShipsTabAccent: Color
    @Composable get() = if (isSystemInDarkTheme()) Blue400 else Blue700

@Composable
fun ShipsList(viewModel: ReportsViewModel, onShipSelected: (String) -> Unit) {
    val shipsData by viewModel.ships.collectAsState()
    var searchTerm by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val realTimeLoadingData by viewModel.realTimeLoadingData.collectAsState()
    var showRealTimeDialog by remember { mutableStateOf(false) }
    val shiftInfo by viewModel.shiftInfo.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()
    val defaultColor = MaterialTheme.colorScheme.primary
    val currentShipSortingMode by viewModel.shipSortingMode.collectAsState()
    val filteredActiveShips by remember(shipsData.activeShips, searchTerm, currentShipSortingMode) {
        derivedStateOf {
            val filtered = shipsData.activeShips.filter {
                it.name.contains(searchTerm, ignoreCase = true)
            }
            Log.d("ShipsList_Log", "کشتی‌های فعال فیلتر شده: ${filtered.size} - با عبارت جستجو: '$searchTerm'")
            sortShips(filtered, currentShipSortingMode)
        }
    }
    val filteredInactiveShips by remember(shipsData.inactiveShips, searchTerm, currentShipSortingMode) {
        derivedStateOf {
            val filtered = shipsData.inactiveShips.filter {
                it.name.contains(searchTerm, ignoreCase = true)
            }
            Log.d("ShipsList_Log", "کشتی‌های غیرفعال فیلتر شده: ${filtered.size} - با عبارت جستجو: '$searchTerm'")
            sortShips(filtered, currentShipSortingMode)
        }
    }

    LaunchedEffect(Unit) {
        try {
            viewModel.loadShips()
        } catch (e: Exception) {
            Log.e("ShipsList_Log", "خطا در بارگذاری لیست کشتی‌ها: ${e.message}", e)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            SearchField(
                searchQuery = searchTerm,
                onSearchQueryChange = { searchTerm = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            ShipsTabSelector(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it },
                activeShipsCount = filteredActiveShips.size,
                inactiveShipsCount = filteredInactiveShips.size
            )

            Spacer(modifier = Modifier.height(8.dp))

            ShipSortingSelector(
                currentMode = currentShipSortingMode,
                onModeChange = viewModel::setShipSortingMode
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTabIndex) {
                    0 -> ShipsTabContent(
                        ships = filteredActiveShips,
                        isActive = true,
                        onShipSelected = { shipName ->
                            viewModel.setCurrentShipName(shipName)
                            onShipSelected(shipName)
                        }
                    )
                    1 -> ShipsTabContent(
                        ships = filteredInactiveShips,
                        isActive = false,
                        onShipSelected = { shipName ->
                            viewModel.setCurrentShipName(shipName)
                            onShipSelected(shipName)
                        }
                    )
                }
            }
        }
    }

    RealTimeLoadingBottomSheet(
        isOpen = showRealTimeDialog,
        onDismiss = { showRealTimeDialog = false },
        loadingData = realTimeLoadingData,
        shiftInfo = shiftInfo ?: ShiftInfo("", "", "", "", ""),
        onRefresh = { viewModel.loadRealTimeData(isDarkTheme, defaultColor) },
        viewModel = viewModel
    )
}

@Composable
fun ShipsTabSelector(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    activeShipsCount: Int,
    inactiveShipsCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(0) },
                shape = RoundedCornerShape(8.dp),
                color = if (selectedTabIndex == 0) MaterialTheme.colorScheme.surface else Color.Transparent,
                shadowElevation = if (selectedTabIndex == 0) 1.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBoat,
                        contentDescription = null,
                        tint = if (selectedTabIndex == 0) ShipsTabAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "کشتی فعال",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTabIndex == 0) ShipsTabAccent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedTabIndex == 0) ShipsTabAccent.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "$activeShipsCount",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabIndex == 0) ShipsTabAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(1) },
                shape = RoundedCornerShape(8.dp),
                color = if (selectedTabIndex == 1) MaterialTheme.colorScheme.surface else Color.Transparent,
                shadowElevation = if (selectedTabIndex == 1) 1.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Archive,
                        contentDescription = null,
                        tint = if (selectedTabIndex == 1) ShipsTabAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "کشتی غیرفعال",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTabIndex == 1) ShipsTabAccent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedTabIndex == 1) ShipsTabAccent.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "$inactiveShipsCount",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabIndex == 1) ShipsTabAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShipsTabContent(
    ships: List<Ship>,
    isActive: Boolean,
    onShipSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (ships.isEmpty()) {
        EmptyShipsState(isActive = isActive)
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(
                items = ships,
                key = { ship -> ship.name }
            ) { ship ->
                ShipCard(
                    ship = ship,
                    isActive = isActive,
                    onClick = { onShipSelected(ship.name) }
                )
            }
        }
    }
}

@Composable
fun SearchField(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "جستجو بر اساس نام کشتی، شماره...",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Surface(
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
    ) {
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxSize(),
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            leadingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "پاک کردن",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Right)
        )
    }
}
