package com.atk.atk_cargo.feature.cargo_details.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atk.atk_cargo.data.model.CargoInfo

// این فایل دیالوگ «جزئیات حواله» (CargoDetailsDialog) و کامپوننت‌های اختصاصی‌اش را از
// CargoDetailsComponents.kt جدا نگه می‌دارد (A1-6، بازسازی ساختاری). وابسته به پالت رنگ
// internal تعریف‌شده در CargoDetailsComponents.kt (Cargo*) که چون هم‌پکیج است نیازی به
// import ندارد.

@Composable
fun CargoDetailsDialog(
    info: CargoInfo,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    showConfirmButton: Boolean,
    isConfirming: Boolean = false
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val windowInfo = LocalWindowInfo.current
        val density = LocalDensity.current
        val maxHeight = with(density) { (windowInfo.containerSize.height * 0.85f).toDp() }

        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = maxHeight),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                ModernDialogHeader(
                    trackingNumber = info.trackingNumber,
                    isConfirmed = info.confirm == "تائید شده"
                )

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModernCargoStatusSection(info)

                    ModernWeightInfoSection(info)

                    ModernCargoDetailsGrid(info)

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    ModernDialogActions(
                        onDismiss = onDismiss,
                        onConfirm = onConfirm,
                        showConfirmButton = showConfirmButton,
                        isConfirming = isConfirming
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernDialogHeader(trackingNumber: String, isConfirmed: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "حواله $trackingNumber",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isConfirmed)
                    CargoAccentBg
                else
                    MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                border = BorderStroke(
                    1.dp,
                    if (isConfirmed)
                        CargoAccentBorder
                    else
                        MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isConfirmed) Icons.Default.CheckCircle else Icons.Default.Clear,
                        contentDescription = null,
                        tint = if (isConfirmed)
                            CargoAccent
                        else
                            MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isConfirmed) "تأیید شده" else "در انتظار تأیید",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isConfirmed)
                            CargoAccent
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CargoAccentBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Inventory,
                contentDescription = null,
                tint = CargoAccent,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ModernCargoStatusSection(info: CargoInfo) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CargoMutedBg
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "وضعیت حواله",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CargoTitleColor
                )
                Icon(
                    imageVector = Icons.Default.DirectionsBoat,
                    contentDescription = null,
                    tint = CargoAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            ModernCargoStatusTimeline(info)
        }
    }
}

@Composable
fun ModernCargoStatusTimeline(info: CargoInfo) {
    val steps = listOf(
        "ورود" to Icons.Default.Inventory,
        "بارگیری" to Icons.Default.Scale,
        "خروج" to Icons.Default.CheckCircle
    )
    val displayStatus = if (info.confirm == "تائید شده" && info.status == "ورود") "بارگیری" else info.status
    val currentStepIndex = steps.indexOfFirst { it.first == displayStatus }.coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
                .height(2.dp)
                .align(Alignment.Center)
                .background(
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    RoundedCornerShape(1.dp)
                )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, (step, icon) ->
                ModernTimelineStep(
                    step = step,
                    icon = icon,
                    isCompleted = index <= currentStepIndex
                )
            }
        }
    }
}

@Composable
fun ModernTimelineStep(
    step: String,
    icon: ImageVector,
    isCompleted: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = when {
                isCompleted -> CargoAccent
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            },
            shadowElevation = if (isCompleted) 8.dp else 0.dp
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = when {
                        isCompleted -> if (isSystemInDarkTheme()) Color(0xFF042F2E) else Color.White
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Text(
            text = step,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isCompleted -> CargoAccent
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            },
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ModernWeightInfoSection(info: CargoInfo) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            CargoAccentBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "اطلاعات وزن",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CargoTitleColor
                )
                Icon(
                    imageVector = Icons.Default.Scale,
                    contentDescription = null,
                    tint = CargoAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val hasShortage = info.shortageWeight.isNotBlank() && info.shortageWeight != "0"
                val hasExcess = info.excessWeight.isNotBlank() && info.excessWeight != "0"

                ModernWeightInfoItem(
                    label = "وزن خالص",
                    value = info.netWeight,
                    icon = Icons.Default.Scale,
                    color = CargoAccent,
                    modifier = Modifier.weight(1f)
                )

                if (hasShortage) {
                    ModernWeightInfoItem(
                        label = "کسری",
                        value = info.shortageWeight,
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (hasExcess) {
                    ModernWeightInfoItem(
                        label = "اضافه",
                        value = info.excessWeight,
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        color = CargoAccent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ModernWeightInfoItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ModernCargoDetailsGrid(info: CargoInfo) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "rotation"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CargoMutedBg
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = CargoAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "جزئیات حواله",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CargoTitleColor
                    )
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "بستن" else "نمایش بیشتر",
                    tint = CargoMutedText,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(rotation)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ModernInfoCard(
                            icon = Icons.Default.DirectionsBoat,
                            label = "نام کشتی",
                            value = info.shipName,
                            modifier = Modifier.weight(1f)
                        )
                        ModernInfoCard(
                            icon = Icons.Default.Warehouse,
                            label = "انبار بارگیری",
                            value = info.loadingWarehouse,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ModernInfoCard(
                            icon = Icons.Default.Category,
                            label = "نوع کالا",
                            value = info.cargoType,
                            modifier = Modifier.weight(1f)
                        )
                        ModernInfoCard(
                            icon = Icons.Default.Business,
                            label = "شرکت باربری",
                            value = info.shippingCompany,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ModernInfoCard(
                            icon = Icons.Default.ConfirmationNumber,
                            label = "شماره کوتاژ",
                            value = info.loadingQuotaNumber,
                            modifier = Modifier.weight(1f)
                        )
                        ModernInfoCard(
                            icon = Icons.Default.Group,
                            label = "تعداد نفرات",
                            value = info.numberOfPeople,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernInfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            CargoCardBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CargoMutedText,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = CargoMutedText
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CargoTitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ModernDialogActions(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    showConfirmButton: Boolean,
    isConfirming: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (showConfirmButton) {
            Button(
                onClick = onConfirm,
                enabled = !isConfirming,
                modifier = Modifier
                    .weight(2f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CargoAccent,
                    contentColor = if (isSystemInDarkTheme()) Color(0xFF042F2E) else Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 2.dp
                )
            ) {
                if (isConfirming) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = if (isSystemInDarkTheme()) Color(0xFF042F2E) else Color.White
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "تأیید حواله",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Button(
            onClick = onDismiss,
            enabled = !isConfirming,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CargoMutedBg,
                contentColor = CargoMutedText
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "بستن",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
