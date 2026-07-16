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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
        horizontalArrangement = Arrangement.spacedBy(12.dp)
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

    if (showStartDatePicker) {
        PersianDatePickerDialog(
            isOpen = showStartDatePicker,
            title = "انتخاب تاریخ شروع",
            initialDate = startDate,
            accentColor = accentColor,
            onDismiss = { showStartDatePicker = false },
            onDateSelected = {
                onStartDateChange(it)
                showStartDatePicker = false
            }
        )
    }

    if (showEndDatePicker) {
        PersianDatePickerDialog(
            isOpen = showEndDatePicker,
            title = "انتخاب تاریخ پایان",
            initialDate = endDate,
            accentColor = accentColor,
            minDate = startDate,
            onDismiss = { showEndDatePicker = false },
            onDateSelected = {
                onEndDateChange(it)
                showEndDatePicker = false
            }
        )
    }
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
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
                modifier = Modifier.size(20.dp)
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
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 16.dp,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
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
                                modifier = Modifier.padding(12.dp).size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Year Selector
                    Text(
                        text = "سال",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(
                        state = yearListState,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(years) { year ->
                            val isSelected = year == selectedYear
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedYear = year },
                                label = { Text(year.toString()) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accentColor,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Month Selector
                    Text(
                        text = "ماه",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(
                        state = monthListState,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(12) { index ->
                            val month = index + 1
                            val isSelected = month == selectedMonth
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedMonth = month },
                                label = { Text(PERSIAN_MONTHS[index]) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accentColor,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Day Selector
                    Text(
                        text = "روز",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    val daysInMonth = getDaysInPersianMonth(selectedYear, selectedMonth)
                    if (selectedDay > daysInMonth) selectedDay = daysInMonth

                    LazyRow(
                        state = dayListState,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 4.dp)
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

                    Spacer(modifier = Modifier.height(24.dp))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("انصراف")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                onDateSelected("%04d/%02d/%02d".format(selectedYear, selectedMonth, selectedDay))
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("تأیید")
                        }
                    }
                }
            }
        }
    }
}
