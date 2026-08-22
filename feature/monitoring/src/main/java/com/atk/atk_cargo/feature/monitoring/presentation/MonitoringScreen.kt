package com.atk.atk_cargo.feature.monitoring.presentation

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.atk.atk_cargo.core.ui.components.StatisticsCard
import com.atk.atk_cargo.data.model.MonitoringEvent
import com.atk.atk_cargo.ui.theme.ATKCargoTheme

private data class StatusFilterOption(val value: String, val label: String)

private val STATUS_FILTERS = listOf(
    StatusFilterOption("open", "باز"),
    StatusFilterOption("acknowledged", "تأییدشده"),
    StatusFilterOption("all", "همه")
)

private val SEVERITY_LABEL = mapOf(
    "critical" to "بحرانی",
    "warning" to "هشدار",
    "info" to "اطلاعاتی"
)

// صفحه‌ی نظارت — مصرف‌کننده‌ی api/v2/monitoring/* (فاز الف) با همان بازخوانی خودکار هر ۳۰ ثانیه‌ی
// داشبورد وب (فاز ب)؛ DEEP_CODE_AUDIT.md فاز۳ #۳۲ فاز ج
@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            Surface(shadowElevation = 2.dp, color = MaterialTheme.colorScheme.surface) {
                TopAppBar(
                    title = { Text("مانیتورینگ", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, "بازگشت")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.refreshNow() }) {
                            Icon(Icons.Default.Refresh, "بروزرسانی")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SummarySection(uiState)
            FilterChipsRow(
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
                    uiState.events.isEmpty() -> {
                        EmptyState(message = "رویدادی برای نمایش وجود ندارد.")
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(ATKCargoTheme.spacing.m),
                            verticalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.s)
                        ) {
                            items(uiState.events, key = { it.id }) { event ->
                                EventCard(
                                    event = event,
                                    onClick = { selectedEvent = event },
                                    onAcknowledge = { viewModel.acknowledge(event.id) }
                                )
                            }
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

@Composable
private fun SummarySection(uiState: MonitoringUiState) {
    val counts = uiState.openAlerts
    val health = uiState.health

    Column(modifier = Modifier.padding(ATKCargoTheme.spacing.m)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.s)
        ) {
            StatisticsCard(
                title = "سلامت سیستم",
                value = when {
                    health == null -> "—"
                    health.healthy -> "سالم"
                    else -> "ناسالم"
                },
                description = "بروزرسانی خودکار هر ۳۰ ثانیه",
                icon = Icons.Default.MonitorHeart,
                color = if (health?.healthy == false) MaterialTheme.colorScheme.error else ATKCargoTheme.semanticColors.success,
                modifier = Modifier.weight(1f)
            )
            StatisticsCard(
                title = "بحرانی",
                value = counts?.openCritical?.toString() ?: "—",
                description = "هشدار باز",
                icon = Icons.Default.ErrorOutline,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(ATKCargoTheme.spacing.s))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.s)
        ) {
            StatisticsCard(
                title = "هشدار",
                value = counts?.openWarning?.toString() ?: "—",
                description = "هشدار باز",
                icon = Icons.Default.Warning,
                color = ATKCargoTheme.semanticColors.warning,
                modifier = Modifier.weight(1f)
            )
            StatisticsCard(
                title = "اطلاعاتی",
                value = counts?.openInfo?.toString() ?: "—",
                description = "هشدار باز",
                icon = Icons.Default.Info,
                color = ATKCargoTheme.semanticColors.info,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FilterChipsRow(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ATKCargoTheme.spacing.m),
        horizontalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.s)
    ) {
        STATUS_FILTERS.forEach { option ->
            FilterChip(
                selected = selected == option.value,
                onClick = { onSelect(option.value) },
                label = { Text(option.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
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
private fun severityIcon(severity: String): ImageVector = when (severity) {
    "critical" -> Icons.Default.ErrorOutline
    "warning" -> Icons.Default.Warning
    else -> Icons.Default.Info
}

@Composable
private fun EventCard(
    event: MonitoringEvent,
    onClick: () -> Unit,
    onAcknowledge: () -> Unit
) {
    val color = severityColor(event.severity)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = ATKCargoTheme.appShapes.card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = ATKCargoTheme.elevation.cardDefault)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ATKCargoTheme.spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = severityIcon(event.severity),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(ATKCargoTheme.dimensions.iconMedium)
            )
            Spacer(modifier = Modifier.width(ATKCargoTheme.spacing.s))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = SEVERITY_LABEL[event.severity] ?: event.severity,
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = event.eventType,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = event.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (event.acknowledgedAt == null) {
                IconButton(onClick = onAcknowledge) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "تأیید رویداد",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
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
        title = { Text(event.eventType) },
        text = {
            Column {
                DetailRow("شدت", SEVERITY_LABEL[event.severity] ?: event.severity)
                DetailRow("پیام", event.message)
                DetailRow("منبع", event.source)
                DetailRow("زمان ثبت", event.createdAt)
                DetailRow("وضعیت", if (event.acknowledgedAt != null) "تأییدشده در ${event.acknowledgedAt}" else "باز")
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
            androidx.compose.material3.TextButton(onClick = onDismiss) {
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
