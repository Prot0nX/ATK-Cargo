package com.atk.atk_cargo.workers

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.atk.atk_cargo.api.AppNotificationManager
import com.atk.atk_cargo.api.ShiftInfo
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.data.model.RealTimeLoadingData
import com.atk.atk_cargo.data.repository.ReportsRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// جایگزین LoadingNotificationService (Foreground Service دائمی) که سقف زمانی dataSync در Android 14+ را نقض می‌کرد
class LoadingNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {
    private val userPreferencesManager: UserPreferencesManager by inject()
    private val reportsRepository: ReportsRepository by inject()

    companion object {
        const val UNIQUE_WORK_NAME = "LoadingNotificationWorker"
        const val KEY_IS_REFRESH = "is_refresh"
        private const val PREFS_NAME = "LoadingNotificationPrefs"
        private const val KEY_LAST_UPDATE_TIME = "last_update_time"
        private const val KEY_ENABLED = "notifications_enabled"
    }

    override suspend fun doWork(): ListenableWorker.Result {
        return try {
            fetchAndNotify(inputData.getBoolean(KEY_IS_REFRESH, false))
            ListenableWorker.Result.success()
        } catch (e: Exception) {
            Log.e("LoadingNotificationWorker", "دریافت اطلاعات بارگیری شکست خورد", e)
            ListenableWorker.Result.retry()
        }
    }

 // isRefresh=true یعنی بروزرسانی دستی که باید از بررسی enabled/شیفت غیرفعال عبور کند
    private suspend fun fetchAndNotify(isRefresh: Boolean) {
        val userType = userPreferencesManager.userType.first()
        if (userType != "admin") {
            return
        }

        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!isRefresh && !prefs.getBoolean(KEY_ENABLED, true)) {
            return
        }

        if (!isRefresh && isCurrentShiftDisabled()) {
            return
        }

 // همان ReportsRepository دیالوگ «بارگیری لحظه‌ای» تا کش HTTP بین این polling و polling دیالوگ مشترک باشد
        val loadingData = reportsRepository.getRealTimeLoadingData()

        val previousData = getCachedLoadingData()
        val hasDataChanged = previousData == null || previousData.toSet() != loadingData.data.toSet()

        prefs.edit { putLong(KEY_LAST_UPDATE_TIME, System.currentTimeMillis()) }
        saveCurrentShiftInfo(loadingData.shiftInfo)
        cacheLoadingData(loadingData.data)

        if (loadingData.data.isNotEmpty() && hasDataChanged) {
            val mutedShips = applicationContext.getSharedPreferences("ship_notifications_prefs", Context.MODE_PRIVATE)
                .getStringSet("muted_ships", emptySet()) ?: emptySet()

            AppNotificationManager(applicationContext).notifyLoadingData(loadingData.data, mutedShips)
        }
    }

    private fun saveCurrentShiftInfo(shiftInfo: ShiftInfo) {
        val shiftId = "${shiftInfo.type}_${shiftInfo.startDate}"

        val prefs = applicationContext.getSharedPreferences("ShiftNotificationsPrefs", Context.MODE_PRIVATE)
        val previousShiftId = prefs.getString("current_shift_id", "") ?: ""

        if (previousShiftId.isNotEmpty() && previousShiftId != shiftId) {
            prefs.edit {
                remove("disabled_$previousShiftId")
                putString("current_shift_id", shiftId)
            }
        } else {
            prefs.edit { putString("current_shift_id", shiftId) }
        }
    }

    private fun isCurrentShiftDisabled(): Boolean {
        val prefs = applicationContext.getSharedPreferences("ShiftNotificationsPrefs", Context.MODE_PRIVATE)
        val currentShiftId = prefs.getString("current_shift_id", "") ?: ""
        if (currentShiftId.isEmpty()) {
            return false
        }
        return prefs.getBoolean("disabled_$currentShiftId", false)
    }

    private fun getCachedLoadingData(): List<RealTimeLoadingData>? {
        return try {
            val jsonData = applicationContext.getSharedPreferences("LoadingDataCache", Context.MODE_PRIVATE)
                .getString("cached_data", null) ?: return null
            val type = object : TypeToken<List<RealTimeLoadingData>>() {}.type
            Gson().fromJson(jsonData, type)
        } catch (_: Exception) {
            null
        }
    }

    private fun cacheLoadingData(data: List<RealTimeLoadingData>) {
        try {
            val jsonData = Gson().toJson(data)
            applicationContext.getSharedPreferences("LoadingDataCache", Context.MODE_PRIVATE)
                .edit { putString("cached_data", jsonData) }
        } catch (e: Exception) {
            Log.e("LoadingNotificationWorker", "ذخیره کش داده‌های بارگیری شکست خورد", e)
        }
    }
}
