package com.atk.atk_cargo.data.model

import com.google.gson.annotations.SerializedName

// یک ردیف از monitoring_events (فاز الف/PHP)
data class MonitoringEvent(
    val id: Int,
    val eventType: String,
    val severity: String,
    val message: String,
    val source: String,
    val dedupeKey: String? = null,
    val createdAt: String,
    val acknowledgedAt: String? = null,
    val acknowledgedBy: String? = null
)

data class MonitoringEventsResponse(
    val success: Boolean,
    val events: List<MonitoringEvent> = emptyList()
)

data class MonitoringOpenAlertCounts(
    @SerializedName("open_total") val openTotal: Int,
    @SerializedName("open_critical") val openCritical: Int,
    @SerializedName("open_warning") val openWarning: Int,
    @SerializedName("open_info") val openInfo: Int
)

data class MonitoringHealthStatus(
    val healthy: Boolean,
    val status: Map<String, Boolean> = emptyMap(),
    val missingTables: List<String> = emptyList(),
    val checkedAt: String
)

data class MonitoringSummaryResponse(
    val success: Boolean,
    val openAlerts: MonitoringOpenAlertCounts,
    val health: MonitoringHealthStatus
)
