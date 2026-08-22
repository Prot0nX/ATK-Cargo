package com.atk.atk_cargo.feature.reports.presentation.ships

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.atk.atk_cargo.core.ui.components.ErrorState
import com.atk.atk_cargo.data.model.Ship
import com.atk.atk_cargo.feature.reports.domain.sortShips
import com.atk.atk_cargo.feature.reports.presentation.components.EmptyShipsState
import com.atk.atk_cargo.feature.reports.presentation.ships.components.ShipCard
import com.atk.atk_cargo.feature.reports.presentation.ships.components.ShipSortingSelector
import com.atk.atk_cargo.feature.reports.viewmodel.ReportsViewModel

private data class ShipsTabItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val count: Int
)


private fun Ship.matchesSearch(query: String): Boolean {
    if (query.isBlank()) return true
    return name.contains(query, ignoreCase = true) || cargoType?.contains(query, ignoreCase = true) == true
}

@Composable
fun ShipsList(viewModel: ReportsViewModel, onShipSelected: (String) -> Unit) {
    val reportsUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shipsData = reportsUiState.ships
    val uiState = reportsUiState.status
    var searchTerm by rememberSaveable { mutableStateOf("") }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val activeListState = rememberLazyListState()
    val inactiveListState = rememberLazyListState()
    val currentShipSortingMode by viewModel.shipSortingMode.collectAsStateWithLifecycle()
    // چون ورودی‌ها همین‌جا به remember داده شده‌اند، derivedStateOf اضافی بی‌فایده است
    val filteredActiveShips = remember(shipsData.activeShips, searchTerm, currentShipSortingMode) {
        val filtered = shipsData.activeShips.filter { it.matchesSearch(searchTerm) }
        sortShips(filtered, currentShipSortingMode)
    }
    val filteredInactiveShips = remember(shipsData.inactiveShips, searchTerm, currentShipSortingMode) {
        val filtered = shipsData.inactiveShips.filter { it.matchesSearch(searchTerm) }
        sortShips(filtered, currentShipSortingMode)
    }

    LaunchedEffect(Unit) {
        viewModel.loadShips()
    }

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
            when (uiState) {
                is ReportsViewModel.UiState.Loading if shipsData.activeShips.isEmpty() && shipsData.inactiveShips.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is ReportsViewModel.UiState.Error if shipsData.activeShips.isEmpty() && shipsData.inactiveShips.isEmpty() -> {
                    ErrorState(
                        modifier = Modifier.fillMaxSize(),
                        message = (uiState as ReportsViewModel.UiState.Error).message,
                        onRetryClick = viewModel::loadShips
                    )
                }

                else -> {
                    when (selectedTabIndex) {
                        0 -> ShipsTabContent(
                            ships = filteredActiveShips,
                            isActive = true,
                            listState = activeListState,
                            onShipSelected = { shipName ->
                                viewModel.setCurrentShipName(shipName)
                                onShipSelected(shipName)
                            }
                        )

                        1 -> ShipsTabContent(
                            ships = filteredInactiveShips,
                            isActive = false,
                            listState = inactiveListState,
                            onShipSelected = { shipName ->
                                viewModel.setCurrentShipName(shipName)
                                onShipSelected(shipName)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShipsTabSelector(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    activeShipsCount: Int,
    inactiveShipsCount: Int,
    modifier: Modifier = Modifier
) {
    val tabs = remember(activeShipsCount, inactiveShipsCount) {
        listOf(
            ShipsTabItem("کشتی فعال", Icons.Default.DirectionsBoat, activeShipsCount),
            ShipsTabItem("کشتی غیرفعال", Icons.Default.Archive, inactiveShipsCount)
        )
    }
    val accentColor = MaterialTheme.colorScheme.primary

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
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedTabIndex == index
                val tabColor = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .selectable(
                            selected = isSelected,
                            role = Role.Tab,
                            onClick = { onTabSelected(index) }
                        ),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (isSelected) 1.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = null,
                            tint = tabColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = tabColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) accentColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${tab.count}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = tabColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
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
    listState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier
) {
    if (ships.isEmpty()) {
        EmptyShipsState(isActive = isActive)
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(
                items = ships,
                // کلید باید ترکیبی از نام و نوع محموله باشد وگرنه LazyColumn با کلید تکراری کرش می‌کند
                key = { ship -> "${ship.name}|${ship.cargoType.orEmpty()}" },
                contentType = { "ship" }
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
    placeholder: String = "جستجو بر اساس نام کشتی یا نوع محموله...",
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
