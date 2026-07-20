package com.atk.atk_cargo.feature.cargo_counter.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.atk.atk_cargo.data.model.ActiveShipInfo
import com.atk.atk_cargo.data.model.MessageType
import com.atk.atk_cargo.feature.cargo_counter.presentation.CargoSnackbarMessage
import com.atk.atk_cargo.feature.cargo_counter.presentation.ShipFilterTab
import com.atk.atk_cargo.feature.startup.domain.AnimationManager
import com.atk.atk_cargo.ui.theme.Blue50
import com.atk.atk_cargo.ui.theme.Blue500
import com.atk.atk_cargo.ui.theme.Purple50
import com.atk.atk_cargo.ui.theme.Purple500
import com.atk.atk_cargo.ui.theme.Red50
import com.atk.atk_cargo.ui.theme.Red500
import kotlinx.coroutines.delay

@Composable
fun TabBar(
    selectedTab: ShipFilterTab,
    onTabSelected: (ShipFilterTab) -> Unit,
    allCount: Int,
    loadingCount: Int,
    completedCount: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ShipFilterTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                val count = when (tab) {
                    ShipFilterTab.ALL -> allCount
                    ShipFilterTab.LOADING -> loadingCount
                    ShipFilterTab.COMPLETED -> completedCount
                }
                
                val activeColor = when (tab) {
                    ShipFilterTab.ALL -> MaterialTheme.colorScheme.primary
                    ShipFilterTab.LOADING -> Color(0xFF2196F3) // Blue
                    ShipFilterTab.COMPLETED -> Color(0xFF4CAF50) // Green
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(tab) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (isSelected) 1.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = when (tab) {
                            ShipFilterTab.ALL -> Icons.Default.DirectionsBoat
                            ShipFilterTab.LOADING -> Icons.Default.LocalShipping
                            ShipFilterTab.COMPLETED -> Icons.Default.Check
                        }
                        
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) activeColor else Color(0xFF9E9E9E),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) activeColor else Color(0xFF9E9E9E)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Badge تعداد
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) activeColor.copy(alpha = 0.1f) else Color(0xFFE0E0E0)
                        ) {
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) activeColor else Color(0xFF757575),
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
fun GroupedShipList(
    groupedShips: Map<String, List<ActiveShipInfo>>,
    expandedShipName: String?,
    onExpand: (String?) -> Unit,
    onClick: (ActiveShipInfo) -> Unit,
    shipColorMap: Map<String, Color>
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        groupedShips.forEach { (shipName, ships) ->
            item {
                ShipGroup(
                    shipName = shipName,
                    ships = ships,
                    isExpanded = expandedShipName == shipName,
                    onExpand = { onExpand(shipName) },
                    onClick = onClick,
                    color = shipColorMap[shipName] ?: MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ShipHeader(
    shipName: String,
    ships: List<ActiveShipInfo>,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    color: Color
) {
    val totalEntry = ships.sumOf { it.entryVouchers }
    val totalExit = ships.sumOf { it.exitVouchers }
    val total = totalEntry + totalExit
    val cargoType = ships.firstOrNull()?.cargoType ?: ""
    val isCompleted = total in 1..totalExit

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpand() },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = color.copy(alpha = 0.15f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBoat,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = shipName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        if (isCompleted) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "تکمیل شده",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    if (cargoType.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = color.copy(alpha = 0.7f),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = cargoType,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = color.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = total.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                
                Text(
                    text = "حواله",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "بستن" else "بازکردن",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ShipGroup(
    shipName: String,
    ships: List<ActiveShipInfo>,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onClick: (ActiveShipInfo) -> Unit,
    color: Color
) {
    val filteredShips = remember(ships) {
        ships.filter { it.entryVouchers + it.exitVouchers > 0 }
    }

    if (filteredShips.isEmpty()) {
        return
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column {
            ShipHeader(
                shipName = shipName,
                ships = filteredShips,
                isExpanded = isExpanded,
                onExpand = onExpand,
                color = color
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        )
                ) {
                    WarehouseList(
                        ships = filteredShips,
                        onClick = onClick,
                        cardColor = color
                    )
                }
            }
        }
    }
}

@Composable
private fun WarehouseCard(
    warehouse: String,
    ships: List<ActiveShipInfo>,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onClick: (ActiveShipInfo) -> Unit,
    color: Color
) {
    val filteredShips = remember(ships) {
        ships.filter { it.entryVouchers + it.exitVouchers > 0 }
    }
    
    if (filteredShips.isEmpty()) {
        return
    }
    
    val totalEntry = filteredShips.sumOf { it.entryVouchers }
    val totalExit = filteredShips.sumOf { it.exitVouchers }
    val total = totalEntry + totalExit
    val progress = if (total > 0) totalExit.toFloat() / total else 0f

    val entryColor = Color(0xFF2196F3)
    val exitColor = Color(0xFF4CAF50)

    val sortedShips = remember(filteredShips) {
        filteredShips.sortedWith(
            compareBy<ActiveShipInfo> { 
                val isCompleted = it.exitVouchers >= it.entryVouchers && it.entryVouchers > 0
                if (isCompleted) 1 else 0
            }.thenBy { 
                if (it.entryVouchers > 0) {
                    it.exitVouchers.toFloat() / it.entryVouchers
                } else 0f
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.05f)
        ),
    ) {
        Column(modifier = Modifier.padding(vertical = 0.dp)) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpand() },
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
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
                                shape = CircleShape,
                                color = color.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warehouse,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = warehouse,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                val warehouseCargoType = filteredShips.firstOrNull()?.cargoType ?: ""
                                if (warehouseCargoType.isNotBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Category,
                                            contentDescription = null,
                                            tint = color.copy(alpha = 0.7f),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = warehouseCargoType,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = color.copy(alpha = 0.85f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "${filteredShips.size} کوتاژ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "ورود",
                                        tint = entryColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = totalEntry.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = entryColor,
                                        modifier = Modifier.padding(start = 2.dp, end = 4.dp)
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .height(14.dp)
                                            .width(1.dp)
                                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                                    )
                                    
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "خروج",
                                        tint = exitColor,
                                        modifier = Modifier
                                            .padding(start = 4.dp)
                                            .size(14.dp)
                                    )
                                    Text(
                                        text = totalExit.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = exitColor,
                                        modifier = Modifier.padding(start = 2.dp, end = 4.dp)
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .height(14.dp)
                                            .width(1.dp)
                                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                                    )
                                    
                                    Icon(
                                        imageVector = Icons.Default.ConfirmationNumber,
                                        contentDescription = "کل",
                                        tint = color,
                                        modifier = Modifier
                                            .padding(start = 4.dp)
                                            .size(14.dp)
                                    )
                                    Text(
                                        text = total.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = color,
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Text(
                                text = "${(progress * 100).toInt()}% تکمیل",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = color,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = if (AnimationManager.areAnimationsEnabled()) fadeIn() + expandVertically() else fadeIn(),
                exit = if (AnimationManager.areAnimationsEnabled()) fadeOut() + shrinkVertically() else fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp,
                        top = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sortedShips.forEach { shipInfo ->
                        QuotaCard(
                            shipInfo = shipInfo,
                            onClick = onClick,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuotaCard(
    shipInfo: ActiveShipInfo,
    onClick: (ActiveShipInfo) -> Unit,
    color: Color
) {
    val total = shipInfo.entryVouchers + shipInfo.exitVouchers
    val progress = if (total > 0) shipInfo.exitVouchers.toFloat() / total else 0f
    val progressColor = when {
        progress >= 0.9f -> color.copy(alpha = 0.9f)
        progress >= 0.5f -> color.copy(alpha = 0.7f)
        else -> color.copy(alpha = 0.5f)
    }
    val isActive = total > 0 && shipInfo.exitVouchers < total
    val cardBgColor = if (isActive) {
        color.copy(alpha = 0.03f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { onClick(shipInfo) },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBgColor
        ),
        border = BorderStroke(1.dp, color.copy(alpha = if (isActive) 0.3f else 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
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
                        shape = CircleShape,
                        color = progressColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = progressColor,
                            modifier = Modifier
                                .padding(6.dp)
                                .size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "کوتاژ: ${shipInfo.loadingQuotaNumber.takeLast(4)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (shipInfo.cargoType.isNotEmpty()) {
                            Text(
                                text = shipInfo.cargoType,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "ورود",
                        tint = color,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = shipInfo.entryVouchers.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = color,
                        modifier = Modifier.padding(start = 2.dp, end = 4.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .height(12.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    )
                    
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "خروج",
                        tint = color,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(12.dp)
                    )
                    Text(
                        text = shipInfo.exitVouchers.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = color,
                        modifier = Modifier.padding(start = 2.dp, end = 4.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .height(12.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    )
                    
                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = "کل",
                        tint = progressColor,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(12.dp)
                    )
                    Text(
                        text = total.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun WarehouseList(
    ships: List<ActiveShipInfo>,
    onClick: (ActiveShipInfo) -> Unit,
    cardColor: Color
) {
    val groupedByWarehouse = ships.groupBy { it.loadingWarehouse }
    var expandedWarehouse by remember { mutableStateOf<String?>(null) }

    val filteredGroupedByWarehouse = remember(groupedByWarehouse) {
        groupedByWarehouse.mapValues { (_, warehouseShips) ->
            warehouseShips.filter { it.entryVouchers + it.exitVouchers > 0 }
        }.filter { (_, warehouseShips) -> 
            warehouseShips.isNotEmpty() 
        }
    }
    
    val sortedWarehouses = remember(filteredGroupedByWarehouse) {
        filteredGroupedByWarehouse.entries.sortedWith(
            compareBy<Map.Entry<String, List<ActiveShipInfo>>> { entry ->
                val warehouseShips = entry.value
                val totalEntry = warehouseShips.sumOf { it.entryVouchers }
                val totalExit = warehouseShips.sumOf { it.exitVouchers }

                if (totalEntry in 1..totalExit) {
                    1
                } else {
                    0
                }
            }.thenBy { entry ->
                val warehouseShips = entry.value
                val totalEntry = warehouseShips.sumOf { it.entryVouchers }
                val totalExit = warehouseShips.sumOf { it.exitVouchers }
                val total = totalEntry + totalExit
                
                if (total > 0) {
                    totalExit.toFloat() / total
                } else {
                    0f
                }
            }
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        sortedWarehouses.forEach { (warehouse, warehouseShips) ->
            WarehouseCard(
                warehouse = warehouse,
                ships = warehouseShips,
                isExpanded = expandedWarehouse == warehouse,
                onExpand = {
                    expandedWarehouse = if (expandedWarehouse == warehouse) null else warehouse
                },
                onClick = onClick,
                color = cardColor
            )
        }
    }
}

@Composable
fun StatusSnackbar(
    message: CargoSnackbarMessage,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var animatedVisibility by remember { mutableStateOf(false) }

    val translateY by animateDpAsState(
        targetValue = if (animatedVisibility) 0.dp else 100.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ), label = ""
    )

    val alpha by animateFloatAsState(
        targetValue = if (animatedVisibility) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ), label = ""
    )

    LaunchedEffect(isVisible) {
        if (isVisible) {
            animatedVisibility = true
            delay(3000)
            animatedVisibility = false
            delay(300)
            onDismiss()
        }
    }

    if (isVisible || animatedVisibility) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .offset(y = translateY)
                    .alpha(alpha),
                shape = RoundedCornerShape(16.dp),
                color = when (message.type) {
                    MessageType.SUCCESS -> Blue50
                    MessageType.ERROR -> Red50
                    MessageType.WARNING -> Purple50
                },
                border = BorderStroke(1.dp, when (message.type) {
                    MessageType.SUCCESS -> Blue500.copy(alpha = 0.2f)
                    MessageType.ERROR -> Red500.copy(alpha = 0.2f)
                    MessageType.WARNING -> Purple500.copy(alpha = 0.2f)
                }),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = when (message.type) {
                            MessageType.SUCCESS -> Icons.Outlined.CheckCircle
                            MessageType.ERROR -> Icons.Default.Close
                            MessageType.WARNING -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = when (message.type) {
                            MessageType.SUCCESS -> Blue500
                            MessageType.ERROR -> Red500
                            MessageType.WARNING -> Purple500
                        },
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = when (message.type) {
                            MessageType.SUCCESS -> Blue500
                            MessageType.ERROR -> Red500
                            MessageType.WARNING -> Purple500
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedHeader(
    shipCount: Int,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onSelectShips: () -> Unit = {},
    selectedShipsCount: Int = 0
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "بارگیری‌های فعال",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBoat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "$shipCount کوتاژ فعال",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (selectedShipsCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "$selectedShipsCount کشتی انتخاب شده",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onSelectShips() }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBoat,
                            contentDescription = "انتخاب کشتی‌ها",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onRefresh() }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRefreshing) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "بروزرسانی",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
