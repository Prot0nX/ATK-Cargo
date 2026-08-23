package com.atk.atk_cargo.data.model

import com.google.gson.annotations.SerializedName

// یک ردیف از monitoring_events
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

// یک ردیف از audit_log (تب «لاگ تغییرات» صفحه مانیتورینگ)
data class AuditLogEntry(
    val id: Long,
    val username: String,
    val action: String,
    val entityType: String,
    val entityId: String,
    val details: com.google.gson.JsonElement? = null,
    val createdAt: String
)

data class AuditLogResponse(
    val success: Boolean,
    val logs: List<AuditLogEntry> = emptyList()
)
