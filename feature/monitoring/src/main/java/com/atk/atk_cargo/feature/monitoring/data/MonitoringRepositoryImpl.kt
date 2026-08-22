package com.atk.atk_cargo.feature.monitoring.data

import com.atk.atk_cargo.api.ApiResponse
import com.atk.atk_cargo.api.ApiServiceV2
import com.atk.atk_cargo.api.ApiV2Routes
import com.atk.atk_cargo.data.model.MonitoringEvent
import com.atk.atk_cargo.data.model.MonitoringSummaryResponse
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MonitoringRepositoryImpl(
    private val apiServiceV2: ApiServiceV2
) : MonitoringRepository {

    override suspend fun getSummary(): MonitoringSummaryResponse = withContext(Dispatchers.IO) {
        val response = apiServiceV2.getMonitoringSummary()
        if (response.isSuccessful) {
            response.body() ?: throw Exception("پاسخ خالی از سرور")
        } else {
            throw Exception("خطا در دریافت خلاصه‌ی مانیتورینگ: کد ${response.code()}")
        }
    }

    override suspend fun getEvents(status: String, limit: Int, beforeId: Int?): List<MonitoringEvent> =
        withContext(Dispatchers.IO) {
            val response = apiServiceV2.getMonitoringEvents(status = status, limit = limit, beforeId = beforeId)
            if (response.isSuccessful) {
                response.body()?.events ?: emptyList()
            } else {
                throw Exception("خطا در دریافت رویدادها: کد ${response.code()}")
            }
        }

    override suspend fun acknowledgeEvent(id: Int): AcknowledgeResult = withContext(Dispatchers.IO) {
        try {
            val response = apiServiceV2.acknowledgeMonitoringEvent(
                route = ApiV2Routes.monitoringEventAcknowledge(id)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                AcknowledgeResult.Success
            } else {
                val message = parseErrorMessage(response.errorBody()?.string())
                    ?: response.body()?.message
                    ?: "خطا در تأیید رویداد"
                AcknowledgeResult.Failure(message)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AcknowledgeResult.Failure("خطا در ارتباط با سرور: ${e.message}")
        }
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            Gson().fromJson(errorBody, ApiResponse::class.java)?.message
        } catch (_: Exception) {
            null
        }
    }
}
