package com.atk.atk_cargo.feature.cargo_details.presentation.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atk.atk_cargo.domain.model.Cargo
import com.atk.atk_cargo.domain.model.CargoConfirmStatus
import com.atk.atk_cargo.domain.model.CargoStatus
import com.atk.atk_cargo.domain.model.QuotaInfo
import com.atk.atk_cargo.ui.theme.ATKCargoTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

// internal (نه private) چون CargoDetailsDialogSection.kt هم به این‌ها نیاز دارد
internal val CargoAccent: Color
    @Composable get() = MaterialTheme.colorScheme.primary

internal val CargoAccentBg: Color
    @Composable get() = MaterialTheme.colorScheme.primaryContainer

internal val CargoAccentBorder: Color
    @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)

internal val CargoCardBorder: Color
    @Composable get() = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

internal val CargoMutedBg: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

internal val CargoMutedText: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

internal val CargoTitleColor: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface

@Composable
fun InitialInfoSection(
    initialInfo: QuotaInfo?
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = CargoAccentBg,
                    border = BorderStroke(1.dp, CargoAccentBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBoat,
                            contentDescription = null,
                            tint = CargoAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = initialInfo?.shipName ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CargoAccent
                            )
                            val cargoTypeValue = initialInfo?.cargoType ?: ""
                            if (cargoTypeValue.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Category,
                                        contentDescription = null,
                                        tint = CargoAccent.copy(alpha = 0.7f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = cargoTypeValue,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = CargoAccent.copy(alpha = 0.85f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }



                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CargoMutedBg)
                        .clickable { isExpanded = !isExpanded },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "بستن" else "باز کردن",
                        tint = CargoMutedText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            InfoChips(initialInfo = initialInfo)

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    initialInfo?.let { info ->
                        InfoGrid(initialInfo = info)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoGrid(initialInfo: QuotaInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = CargoMutedBg
        ),
        border = BorderStroke(
            width = 1.dp,
            color = CargoCardBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoGridItem(
                    icon = Icons.Default.Warehouse,
                    label = "انبار بارگیری",
                    value = initialInfo.loadingWarehouse,
                    modifier = Modifier.weight(1f)
                )

                InfoGridItem(
                    icon = Icons.Default.Business,
                    label = "شرکت باربری",
                    value = initialInfo.shippingCompany,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoGridItem(
                    icon = Icons.Default.Category,
                    label = "نوع کالا",
                    value = initialInfo.cargoType,
                    modifier = Modifier.weight(1f)
                )

                InfoGridItem(
                    icon = Icons.Default.ConfirmationNumber,
                    label = "شماره کوتاژ",
                    value = initialInfo.loadingQuotaNumber.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun InfoGridItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CargoAccent,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = CargoMutedText
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = CargoTitleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun InfoChips(initialInfo: QuotaInfo?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        InfoGridCard(
            icon = Icons.Default.Business,
            value = initialInfo?.shippingCompany ?: "",
            modifier = Modifier.weight(1f)
        )

        InfoGridCard(
            icon = Icons.Default.Warehouse,
            value = initialInfo?.loadingWarehouse ?: "",
            modifier = Modifier.weight(1f)
        )

        InfoGridCard(
            icon = Icons.Default.ConfirmationNumber,
            value = initialInfo?.loadingQuotaNumber?.toString() ?: "",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun InfoGridCard(
    icon: ImageVector,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(9.dp),
        color = CargoMutedBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CargoAccent,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = CargoTitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CargoInfoCard(cargoInfo: Cargo, onClick: () -> Unit) {
    val isExited = cargoInfo.status == CargoStatus.EXITED.wireValue
    val isConfirmed = cargoInfo.confirm == CargoConfirmStatus.CONFIRMED.wireValue

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CargoCardBorder),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.width(80.dp)
            ) {
                val statusColor = if (isExited) ATKCargoTheme.semanticColors.cargoExit else CargoAccent
                Surface(
                    shape = ATKCargoTheme.appShapes.chip,
                    color = statusColor.copy(alpha = 0.1f),
                    border = BorderStroke(
                        ATKCargoTheme.dimensions.borderWidthThin,
                        statusColor.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = ATKCargoTheme.spacing.m, vertical = ATKCargoTheme.spacing.xs),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cargoInfo.status,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }

                Text(
                    text = if (isExited) cargoInfo.exitTime ?: "" else cargoInfo.entryTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = cargoInfo.trackingNumber,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isConfirmed) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "تأیید شده",
                            tint = CargoAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تعداد نفرات: ${cargoInfo.numberOfPeople}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SearchAndRefreshSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onRefresh: () -> Unit
) {
    var isRefreshing by remember { mutableStateOf(false) }
    var rotationState by remember { mutableFloatStateOf(0f) }
    val rotation = animateFloatAsState(
        targetValue = rotationState,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "rotation"
    )
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(11.dp),
                color = CargoMutedBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = CargoMutedText,
                        modifier = Modifier.size(18.dp)
                    )

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "جستجوی شماره حواله",
                                style = MaterialTheme.typography.bodySmall,
                                color = CargoMutedText
                            )
                        }

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodySmall.copy(
                                color = CargoTitleColor,
                                textAlign = TextAlign.Left
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .height(44.dp)
                    .clickable(enabled = !isRefreshing) {
                        if (!isRefreshing) {
                            isRefreshing = true
                            rotationState += 360f
                            onRefresh()
                            coroutineScope.launch {
                                delay(1500.milliseconds)
                                isRefreshing = false
                            }
                        }
                    },
                shape = RoundedCornerShape(11.dp),
                color = CargoAccentBg,
                border = BorderStroke(1.dp, CargoAccentBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "بروزرسانی",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CargoAccent
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "بروزرسانی",
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(rotation.value),
                        tint = CargoAccent
                    )
                }
            }
        }
    }
}

@Composable
fun TabsSection(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    confirmedCount: Int,
    unconfirmedCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = CargoMutedBg
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
                    color = if (selectedTab == 0) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (selectedTab == 0) 1.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val errorColor = MaterialTheme.colorScheme.error
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (selectedTab == 0) errorColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(ATKCargoTheme.dimensions.iconMedium)
                        )
                        Spacer(modifier = Modifier.width(ATKCargoTheme.spacing.s))
                        Text(
                            text = "تأیید نشده",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 0) errorColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(ATKCargoTheme.spacing.s))
                        Surface(
                            shape = ATKCargoTheme.appShapes.chip,
                            color = if (selectedTab == 0) errorColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "$unconfirmedCount",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 0) errorColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = ATKCargoTheme.spacing.xs, vertical = ATKCargoTheme.spacing.xxs)
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(1) },
                    shape = ATKCargoTheme.appShapes.small,
                    color = if (selectedTab == 1) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (selectedTab == 1) ATKCargoTheme.elevation.level1 else ATKCargoTheme.elevation.level0
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = ATKCargoTheme.spacing.s),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val successColor = ATKCargoTheme.semanticColors.success
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (selectedTab == 1) successColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(ATKCargoTheme.dimensions.iconMedium)
                        )
                        Spacer(modifier = Modifier.width(ATKCargoTheme.spacing.s))
                        Text(
                            text = "تأیید شده",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 1) successColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(ATKCargoTheme.spacing.s))
                        Surface(
                            shape = ATKCargoTheme.appShapes.chip,
                            color = if (selectedTab == 1) successColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "$confirmedCount",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 1) successColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = ATKCargoTheme.spacing.xs, vertical = ATKCargoTheme.spacing.xxs)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CargoListSection(
    groupedCargoList: Map<Boolean, List<Cargo>>,
    onCargoSelected: (Cargo) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val unconfirmedCount = groupedCargoList[false]?.size ?: 0
    val confirmedCount = groupedCargoList[true]?.size ?: 0

    val confirmedStats = remember(groupedCargoList[true]) {
        groupedCargoList[true]?.let { confirmedCargos ->
            val totalWeight = confirmedCargos.sumOf { it.netWeight?.value ?: 0.0 }
            val avgWeight = if (confirmedCargos.isNotEmpty()) totalWeight / confirmedCargos.size else 0.0
            Triple(confirmedCargos.size, totalWeight, avgWeight)
        } ?: Triple(0, 0.0, 0.0)
    }

 // مرتب‌سازی در remember نگه داشته می‌شود تا در هر recomposition دوباره اجرا نشود
    val sortedUnconfirmed = remember(groupedCargoList[false]) {
        groupedCargoList[false]?.sortedByDescending { it.entryTime } ?: emptyList()
    }
    val sortedConfirmed = remember(groupedCargoList[true]) {
        groupedCargoList[true]?.sortedByDescending { "${it.exitDate} ${it.exitTime}" } ?: emptyList()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabsSection(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            confirmedCount = confirmedCount,
            unconfirmedCount = unconfirmedCount
        )

        if (selectedTab == 1 && confirmedCount > 0) {
            GroupStats(
                count = confirmedStats.first,
                totalWeight = confirmedStats.second,
                avgWeight = confirmedStats.third
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (selectedTab == 0) {
                items(
                    items = sortedUnconfirmed,
 // trackingNumber می‌تواند تکراری باشد؛ id کلید یکتای رکورد است
                    key = { it.id ?: it.trackingNumber.hashCode() }
                ) { cargoInfo ->
                    CargoInfoCard(
                        cargoInfo = cargoInfo,
                        onClick = { onCargoSelected(cargoInfo) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                items(
                    items = sortedConfirmed,
                    key = { it.id ?: it.trackingNumber.hashCode() }
                ) { cargoInfo ->
                    CargoInfoCard(
                        cargoInfo = cargoInfo,
                        onClick = { onCargoSelected(cargoInfo) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun GroupStats(
    count: Int,
    totalWeight: Double,
    avgWeight: Double
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = CargoAccentBg,
        border = BorderStroke(1.dp, CargoAccentBorder),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                StatItem(
                    label = "تعداد",
                    value = count.toString()
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(CargoAccentBorder)
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                StatItem(
                    label = "مجموع وزن",
                    value = String.format("%,d", totalWeight.toLong())
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(CargoAccentBorder)
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                StatItem(
                    label = "میانگین",
                    value = String.format("%,d", avgWeight.toLong())
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    unit: String = ""
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

