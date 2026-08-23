package com.atk.atk_cargo.feature.monitoring.data

import com.atk.atk_cargo.data.model.AuditLogEntry
import com.atk.atk_cargo.data.model.MonitoringEvent
import com.atk.atk_cargo.data.model.MonitoringSummaryResponse

// قرارداد لایه داده مانیتورینگ که ارتباط مستقیم MonitoringViewModel با ApiServiceV2 را حذف می‌کند
interface MonitoringRepository {

    suspend fun getSummary(): MonitoringSummaryResponse

    /** @param status یکی از open، acknowledged یا all */
    suspend fun getEvents(status: String, limit: Int = 100, beforeId: Int? = null): List<MonitoringEvent>

    suspend fun acknowledgeEvent(id: Int): AcknowledgeResult

    suspend fun getAuditLogs(limit: Int = 100, beforeId: Long? = null): List<AuditLogEntry>
}

sealed class AcknowledgeResult {
    data object Success : AcknowledgeResult()
    data class Failure(val message: String) : AcknowledgeResult()
}
