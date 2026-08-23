package com.atk.atk_cargo.feature.monitoring.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.atk.atk_cargo.core.ui.components.EmptyState
import com.atk.atk_cargo.core.ui.components.ErrorState
import com.atk.atk_cargo.core.ui.components.LoadingOverlay
import com.atk.atk_cargo.data.model.MonitoringEvent
import com.atk.atk_cargo.ui.theme.ATKCargoTheme
import com.atk.atk_cargo.utils.JalaliDateUtils

private data class StatusFilterOption(val value: String, val label: String)

private val STATUS_FILTERS = listOf(
    StatusFilterOption("open", "باز"),
    StatusFilterOption("acknowledged", "تأییدشده"),
    StatusFilterOption("all", "همه")
)

// دسته‌های تب — "all" یعنی بدون فیلتر شدت؛ بقیه دقیقاً مقدار خام severity سرور
private val SEVERITY_TABS = listOf("all", "critical", "warning", "info")

private val SEVERITY_LABEL = mapOf(
    "critical" to "بحرانی",
    "warning" to "هشدار",
    "info" to "اطلاعاتی"
)

// تاریخ/زمان سرور (میلادی، "yyyy-MM-dd HH:mm:ss") به شمسی — دقیقاً هم‌ارز toLocaleDateString('fa-IR')
// داشبورد وب (PHP/Monitoring/assets/app.js) که مرورگر خودش شمسی نمایش می‌دهد؛ سمت اندروید باید صریح تبدیل شود
private fun formatJalaliDateTime(raw: String): String {
    val date = JalaliDateUtils.formatDate(raw)
    val time = JalaliDateUtils.formatTime(raw)
    return if (time.isNotEmpty()) "$date $time" else date
}

// فیلتر ترکیبی شدت + جستجوی متنی روی رویدادهای همین لحظه بارگذاری‌شده؛ کاملاً سمت کلاینت است
// (بدون درخواست شبکه‌ی جدید) چون هر بار حداکثر ۱۰۰ رویداد برای یک وضعیت (باز/تأییدشده/همه) واکشی می‌شود
private fun filterEvents(events: List<MonitoringEvent>, severity: String, query: String): List<MonitoringEvent> {
    val bySeverity = if (severity == "all") events else events.filter { it.severity == severity }
    val trimmedQuery = query.trim()
    if (trimmedQuery.isEmpty()) return bySeverity
    return bySeverity.filter {
        it.eventType.contains(trimmedQuery, ignoreCase = true) ||
            it.message.contains(trimmedQuery, ignoreCase = true) ||
            it.source.contains(trimmedQuery, ignoreCase = true)
    }
}

// صفحه‌ی نظارت — مصرف‌کننده‌ی api/v2/monitoring/* (فاز الف) با همان بازخوانی خودکار هر ۳۰ ثانیه‌ی
// داشبورد وب (فاز ب). زبان طراحی عمداً هم‌راستا با UserManagementDialog/ComprehensiveAnalyticsDialog
// شد (هدر با نشان آیکون، کارت‌های آماری، فیلتر segmented، کارت‌های لیست با حاشیه‌ی ظریف) نه TopAppBar
// پیش‌فرض Material3 — تا با بقیه‌ی صفحات مدیریتی اپ یکدست باشد.
@Composable
fun MonitoringScreen(
    viewModel: MonitoringViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        viewModel.startAutoRefresh()
        onDispose { viewModel.stopAutoRefresh() }
    }

    var selectedEvent by remember { mutableStateOf<MonitoringEvent?>(null) }
    var severityTab by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredEvents = remember(uiState.events, severityTab, searchQuery) {
        filterEvents(uiState.events, severityTab, searchQuery)
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            MonitoringHeader(onBackClick = onBackClick, onRefreshClick = { viewModel.refreshNow() })
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .7f))

            KpiSection(uiState)

            SearchField(query = searchQuery, onQueryChange = { searchQuery = it })

            SeverityTabRow(
                events = uiState.events,
                selected = severityTab,
                onSelect = { severityTab = it }
            )

            StatusFilterRow(
                selected = uiState.statusFilter,
                onSelect = { viewModel.setStatusFilter(it) }
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading && uiState.events.isEmpty() && uiState.errorMessage == null -> {
                        LoadingOverlay(isLoading = true)
                    }
                    uiState.errorMessage != null && uiState.events.isEmpty() -> {
                        ErrorState(
                            message = uiState.errorMessage ?: "خطایی رخ داده است.",
                            onRetryClick = { viewModel.refreshNow() }
                        )
                    }
                    filteredEvents.isEmpty() -> {
                        EmptyState(
                            message = if (uiState.events.isEmpty()) "رویدادی برای نمایش وجود ندارد." else "رویدادی مطابق فیلتر یافت نشد."
                        )
                    }
                    else -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(
                                    start = ATKCargoTheme.spacing.l,
                                    end = ATKCargoTheme.spacing.l,
                                    top = ATKCargoTheme.spacing.s,
                                    bottom = ATKCargoTheme.spacing.l
                                ),
                                verticalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.s)
                            ) {
                                items(filteredEvents, key = { it.id }) { event ->
                                    EventCard(
                                        event = event,
                                        onClick = { selectedEvent = event },
                                        onAcknowledge = { viewModel.acknowledge(event.id) }
                                    )
                                }
                            }
                            RefreshHint()
                        }
                    }
                }
            }
        }
    }

    selectedEvent?.let { event ->
        EventDetailsDialog(
            event = event,
            onAcknowledge = {
                viewModel.acknowledge(event.id)
                selectedEvent = null
            },
            onDismiss = { selectedEvent = null }
        )
    }
}

// هم‌الگوی AnalyticsHeaderCard در ComprehensiveAnalyticsDialog: دکمه‌ی بازگشت دایره‌ای + نشان آیکون مربعی + عنوان/زیرعنوان
@Composable
private fun MonitoringHeader(onBackClick: () -> Unit, onRefreshClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ATKCargoTheme.spacing.xl, vertical = ATKCargoTheme.spacing.l),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.m)
    ) {
        CircleIconButton(icon = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت", onClick = onBackClick)

        Box(
            modifier = Modifier
                .size(34.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Sensors,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(ATKCargoTheme.dimensions.iconMedium)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "مانیتورینگ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "رویدادهای سلامت و امنیت سیستم",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        CircleIconButton(icon = Icons.Default.Refresh, contentDescription = "بروزرسانی", onClick = onRefreshClick)
    }
}

@Composable
private fun CircleIconButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(ATKCargoTheme.dimensions.iconSmall)
        )
    }
}

// چهار کاشی آماری داخل یک ظرف variant، هم‌الگوی دشبورد UserManagementDialog (خلاصه‌ی کاربران) و AnalyticsStatChip
@Composable
private fun KpiSection(uiState: MonitoringUiState) {
    val counts = uiState.openAlerts
    val health = uiState.health
    val healthy = health?.healthy

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ATKCargoTheme.spacing.l)
            .background(MaterialTheme.colorScheme.surfaceVariant, ATKCargoTheme.appShapes.large)
            .padding(ATKCargoTheme.spacing.xs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.xs)
        ) {
            KpiTile(
                icon = Icons.Default.Sensors,
                value = when (healthy) { true -> "سالم"; false -> "ناسالم"; null -> "—" },
                label = "وضعیت سیستم",
                color = if (healthy == false) MaterialTheme.colorScheme.error else ATKCargoTheme.semanticColors.success,
                modifier = Modifier.weight(1f)
            )
            KpiTile(
                icon = Icons.Default.Warning,
                value = counts?.openCritical?.toString() ?: "—",
                label = "بحرانی",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
            KpiTile(
                icon = Icons.Default.Warning,
                value = counts?.openWarning?.toString() ?: "—",
                label = "هشدار",
                color = ATKCargoTheme.semanticColors.warning,
                modifier = Modifier.weight(1f)
            )
            KpiTile(
                icon = Icons.Default.Info,
                value = counts?.openInfo?.toString() ?: "—",
                label = "اطلاعاتی",
                color = ATKCargoTheme.semanticColors.info,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun KpiTile(icon: ImageVector, value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(vertical = ATKCargoTheme.spacing.s, horizontal = ATKCargoTheme.spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.xxs)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(ATKCargoTheme.dimensions.iconSmall))
        Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// نوار جستجو — هم‌الگوی AnalyticsSearchField (ارتفاع ۴۴dp، شعاع ۱۱dp، بدون حاشیه، بک‌گراند surfaceVariant)
@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ATKCargoTheme.spacing.l, vertical = ATKCargoTheme.spacing.s)
            .height(44.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(11.dp))
            .padding(horizontal = ATKCargoTheme.spacing.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.s)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(ATKCargoTheme.dimensions.iconSmall)
        )
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = "جستجو در نوع، پیام یا منبع رویداد…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (query.isNotEmpty()) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "پاک کردن جستجو",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(ATKCargoTheme.dimensions.iconSmall)
                    .clickable { onQueryChange("") }
            )
        }
    }
}

// دسته‌بندی رویدادها بر اساس شدت، به‌صورت تب واقعی (نه چیپ) با شمارنده‌ی هر دسته
@Composable
private fun SeverityTabRow(events: List<MonitoringEvent>, selected: String, onSelect: (String) -> Unit) {
    val selectedIndex = SEVERITY_TABS.indexOf(selected).coerceAtLeast(0)

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = ATKCargoTheme.spacing.l,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        divider = {}
    ) {
        SEVERITY_TABS.forEach { severity ->
            val count = if (severity == "all") events.size else events.count { it.severity == severity }
            val label = if (severity == "all") "همه" else (SEVERITY_LABEL[severity] ?: severity)
            Tab(
                selected = selected == severity,
                onClick = { onSelect(severity) },
                text = {
                    Text(
                        text = "$label ($count)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected == severity) FontWeight.Bold else FontWeight.Medium
                    )
                }
            )
        }
    }
}

// هم‌الگوی AnalyticsGroupingModeButton: انتخاب‌شده = بک‌گراند/حاشیه‌ی primaryContainer، غیرفعال = فقط حاشیه‌ی خنثی
@Composable
private fun StatusFilterRow(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ATKCargoTheme.spacing.l, vertical = ATKCargoTheme.spacing.m),
        horizontalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.s)
    ) {
        STATUS_FILTERS.forEach { option ->
            val isSelected = selected == option.value
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        RoundedCornerShape(11.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = .45f) else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(11.dp)
                    )
                    .clickable { onSelect(option.value) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun severityColor(severity: String): Color = when (severity) {
    "critical" -> MaterialTheme.colorScheme.error
    "warning" -> ATKCargoTheme.semanticColors.warning
    else -> ATKCargoTheme.semanticColors.info
}

@Composable
private fun severityContainerColor(severity: String): Color = when (severity) {
    "critical" -> MaterialTheme.colorScheme.errorContainer
    "warning" -> ATKCargoTheme.semanticColors.warningContainer
    else -> ATKCargoTheme.semanticColors.infoContainer
}

@Composable
private fun severityIcon(severity: String): ImageVector = when (severity) {
    "critical" -> Icons.Default.Warning
    "warning" -> Icons.Default.Warning
    else -> Icons.Default.Info
}

// هم‌الگوی AnalyticsQuotaCard: کارت با حاشیه‌ی ظریف به‌جای سایه، نشان آیکون رنگی، ردیف meta زیر یک خط جداکننده
@Composable
private fun EventCard(
    event: MonitoringEvent,
    onClick: () -> Unit,
    onAcknowledge: () -> Unit
) {
    val color = severityColor(event.severity)
    val isOpen = event.acknowledgedAt == null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(ATKCargoTheme.spacing.m)
    ) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.s)) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(severityContainerColor(event.severity), RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = severityIcon(event.severity),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(ATKCargoTheme.dimensions.iconSmall)
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = SEVERITY_LABEL[event.severity] ?: event.severity,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    StatusBadge(isOpen = isOpen)
                }
                Text(
                    text = event.eventType,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = event.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(ATKCargoTheme.spacing.xs))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .6f))
                Spacer(modifier = Modifier.height(ATKCargoTheme.spacing.xxs))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetaItem(icon = Icons.Default.Sensors, text = event.source)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.xs)) {
                        MetaItem(icon = Icons.Default.Schedule, text = formatJalaliDateTime(event.createdAt))
                        if (isOpen) {
                            AcknowledgeChip(onClick = onAcknowledge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(isOpen: Boolean) {
    val (bg, fg, label) = if (isOpen) {
        Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error, "باز")
    } else {
        Triple(ATKCargoTheme.semanticColors.successContainer, ATKCargoTheme.semanticColors.success, "تأییدشده")
    }
    Row(
        modifier = Modifier
            .background(bg, RoundedCornerShape(8.dp))
            .padding(horizontal = ATKCargoTheme.spacing.s, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(modifier = Modifier.size(6.dp).background(fg, CircleShape))
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = fg)
    }
}

@Composable
private fun MetaItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AcknowledgeChip(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "تأیید رویداد",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun RefreshHint() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ATKCargoTheme.spacing.s),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(ATKCargoTheme.spacing.xs))
        Text(
            text = "بروزرسانی خودکار هر ۳۰ ثانیه",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EventDetailsDialog(
    event: MonitoringEvent,
    onAcknowledge: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        shape = ATKCargoTheme.appShapes.dialog,
        title = { Text(event.eventType, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                DetailRow("شدت", SEVERITY_LABEL[event.severity] ?: event.severity)
                DetailRow("پیام", event.message)
                DetailRow("منبع", event.source)
                DetailRow("زمان ثبت", formatJalaliDateTime(event.createdAt))
                val acknowledgedAt = event.acknowledgedAt
                DetailRow(
                    "وضعیت",
                    if (acknowledgedAt != null) "تأییدشده در ${formatJalaliDateTime(acknowledgedAt)}" else "باز"
                )
                event.acknowledgedBy?.let { DetailRow("تأییدکننده", it) }
            }
        },
        confirmButton = {
            if (event.acknowledgedAt == null) {
                Button(onClick = onAcknowledge) {
                    Text("تأیید رویداد")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("بستن")
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = ATKCargoTheme.spacing.xs)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
