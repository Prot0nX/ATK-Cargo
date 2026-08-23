package com.atk.atk_cargo.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atk.atk_cargo.ui.theme.ATKCargoTheme
import kotlinx.coroutines.delay

val PERSIAN_MONTHS = listOf(
    "فروردین", "اردیبهشت", "خرداد",
    "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر",
    "دی", "بهمن", "اسفند"
)

fun getDaysInPersianMonth(year: Int, month: Int): Int {
    return when (month) {
        in 1..6 -> 31
        in 7..11 -> 30
        12 -> if (isPersianLeapYear(year)) 30 else 29
        else -> 30
    }
}

fun isPersianLeapYear(year: Int): Boolean {
    val remainder = year % 33
    return remainder == 1 || remainder == 5 || remainder == 9 || 
           remainder == 13 || remainder == 17 || remainder == 22 || 
           remainder == 26 || remainder == 30
}

@Composable
fun DateRangePicker(
    startDate: String,
    endDate: String,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.m)
    ) {
        DateClickableField(
            label = "تاریخ شروع",
            value = startDate,
            onClick = { showStartDatePicker = true },
            modifier = Modifier.weight(1f),
            accentColor = accentColor
        )
        DateClickableField(
            label = "تاریخ پایان",
            value = endDate,
            onClick = { showEndDatePicker = true },
            modifier = Modifier.weight(1f),
            accentColor = accentColor
        )
    }

    PersianDatePickerDialog(
        isOpen = showStartDatePicker,
        title = "انتخاب تاریخ شروع",
        initialDate = startDate,
        accentColor = accentColor,
        onDismiss = { showStartDatePicker = false },
        onDateSelected = { date ->
            onStartDateChange(date)
            showStartDatePicker = false
        }
    )

    PersianDatePickerDialog(
        isOpen = showEndDatePicker,
        title = "انتخاب تاریخ پایان",
        initialDate = endDate,
        accentColor = accentColor,
        minDate = startDate,
        onDismiss = { showEndDatePicker = false },
        onDateSelected = { date ->
            onEndDateChange(date)
            showEndDatePicker = false
        }
    )
}

@Composable
private fun DateClickableField(
    label: String,
    value: String,
    onClick: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = ATKCargoTheme.appShapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        border = BorderStroke(ATKCargoTheme.dimensions.borderWidthThin, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ATKCargoTheme.spacing.l, vertical = ATKCargoTheme.spacing.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(ATKCargoTheme.spacing.xs))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(ATKCargoTheme.dimensions.iconMedium)
            )
        }
    }
}

@Composable
fun PersianDatePickerDialog(
    isOpen: Boolean,
    title: String,
    initialDate: String,
    accentColor: Color,
    minDate: String? = null,
    onDismiss: () -> Unit,
    onDateSelected: (date: String) -> Unit
) {
    if (isOpen) {
        val dateParts = initialDate.split("/")
        var selectedYear by remember { mutableIntStateOf(dateParts.getOrNull(0)?.toIntOrNull() ?: 1404) }
        var selectedMonth by remember { mutableIntStateOf(dateParts.getOrNull(1)?.toIntOrNull() ?: 1) }
        var selectedDay by remember { mutableIntStateOf(dateParts.getOrNull(2)?.toIntOrNull() ?: 1) }

        val years = (1403..1410).toList()

        val yearListState = rememberLazyListState()
        val monthListState = rememberLazyListState()
        val dayListState = rememberLazyListState()

 // Auto-center year
        LaunchedEffect(selectedYear, isOpen) {
            val index = years.indexOf(selectedYear)
            if (index >= 0) {
                delay(100)
                val viewportWidth = yearListState.layoutInfo.viewportSize.width
                val itemWidth = yearListState.layoutInfo.visibleItemsInfo.find { it.index == index }?.size ?: 0
                yearListState.animateScrollToItem(index, -(viewportWidth / 2) + (itemWidth / 2))
            }
        }

 // Auto-center month
        LaunchedEffect(selectedMonth, isOpen) {
            val index = selectedMonth - 1
            if (index >= 0) {
                delay(100)
                val viewportWidth = monthListState.layoutInfo.viewportSize.width
                val itemWidth = monthListState.layoutInfo.visibleItemsInfo.find { it.index == index }?.size ?: 0
                monthListState.animateScrollToItem(index, -(viewportWidth / 2) + (itemWidth / 2))
            }
        }

 // Auto-center day
        LaunchedEffect(selectedDay, isOpen) {
            val index = selectedDay - 1
            if (index >= 0) {
                delay(100)
                val viewportWidth = dayListState.layoutInfo.viewportSize.width
                val itemWidth = dayListState.layoutInfo.visibleItemsInfo.find { it.index == index }?.size ?: 0
                dayListState.animateScrollToItem(index, -(viewportWidth / 2) + (itemWidth / 2))
            }
        }

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight()
                    .padding(vertical = ATKCargoTheme.spacing.l),
                shape = ATKCargoTheme.appShapes.dialog,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = ATKCargoTheme.elevation.dialog,
                shadowElevation = ATKCargoTheme.elevation.level5
            ) {
                Column(
                    modifier = Modifier
                        .padding(ATKCargoTheme.spacing.dialogContentPadding)
                        .fillMaxWidth()
                ) {
 // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "%04d/%02d/%02d".format(selectedYear, selectedMonth, selectedDay),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = accentColor
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = accentColor.copy(alpha = 0.1f)
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.padding(ATKCargoTheme.spacing.m).size(ATKCargoTheme.dimensions.iconDefault)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(ATKCargoTheme.spacing.xl))

 // Year Selector
                    Text(
                        text = "سال",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = ATKCargoTheme.spacing.s)
                    )
                    LazyRow(
                        state = yearListState,
                        horizontalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.s),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = ATKCargoTheme.spacing.xs)
                    ) {
                        items(years, key = { it }) { year ->
                            val isSelected = year == selectedYear
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedYear = year },
                                label = { Text(year.toString()) },
                                shape = ATKCargoTheme.appShapes.chip,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accentColor,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(ATKCargoTheme.spacing.l))

 // Month Selector
                    Text(
                        text = "ماه",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = ATKCargoTheme.spacing.s)
                    )
                    LazyRow(
                        state = monthListState,
                        horizontalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.s),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = ATKCargoTheme.spacing.xs)
                    ) {
                        items(12) { index ->
                            val month = index + 1
                            val isSelected = month == selectedMonth
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedMonth = month },
                                label = { Text(PERSIAN_MONTHS[index]) },
                                shape = ATKCargoTheme.appShapes.chip,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accentColor,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(ATKCargoTheme.spacing.l))

 // Day Selector
                    Text(
                        text = "روز",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = ATKCargoTheme.spacing.s)
                    )
                    val daysInMonth = getDaysInPersianMonth(selectedYear, selectedMonth)
                    if (selectedDay > daysInMonth) selectedDay = daysInMonth

                    LazyRow(
                        state = dayListState,
                        horizontalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.xs),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = ATKCargoTheme.spacing.xs)
                    ) {
                        items(daysInMonth) { index ->
                            val day = index + 1
                            val isSelected = day == selectedDay
                            
                            val isEnabled = minDate?.let { minD ->
                                val minParts = minD.split("/")
                                val minYear = minParts[0].toIntOrNull() ?: 0
                                val minMonth = minParts.getOrNull(1)?.toIntOrNull() ?: 0
                                val minDay = minParts.getOrNull(2)?.toIntOrNull() ?: 0
                                
                                when {
                                    selectedYear > minYear -> true
                                    selectedYear < minYear -> false
                                    selectedMonth > minMonth -> true
                                    selectedMonth < minMonth -> false
                                    else -> day >= minDay
                                }
                            } ?: true

                            FilterChip(
                                selected = isSelected,
                                enabled = isEnabled,
                                onClick = { selectedDay = day },
                                label = { Text(day.toString()) },
                                shape = CircleShape,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accentColor,
                                    selectedLabelColor = Color.White,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(ATKCargoTheme.spacing.xxl))

 // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = ATKCargoTheme.appShapes.button
                        ) {
                            Text("انصراف")
                        }
                        Spacer(modifier = Modifier.width(ATKCargoTheme.spacing.m))
                        Button(
                            onClick = {
                                onDateSelected("%04d/%02d/%02d".format(selectedYear, selectedMonth, selectedDay))
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                contentColor = Color.White
                            ),
                            shape = ATKCargoTheme.appShapes.button
                        ) {
                            Text("تأیید")
                        }
                    }
                }
            }
        }
    }
}
