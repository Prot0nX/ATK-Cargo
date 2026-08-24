package com.atk.atk_cargo.api

// وابستگی‌ها و ایمپورت‌ها.
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// تنظیمات و متغیرهای سراسری.
private const val TAG = "PermissionPoller"

// فاصله زمانی بین هر بار بررسی permissions از سرور (پیش‌فرض ۳ دقیقه)
private const val POLL_INTERVAL_MS = 3 * 60 * 1000L // 3 minutes

// پیاده‌سازی منطق اصلی.

// مدیریت به‌روزرسانی زنده سطوح دسترسی: هر POLL_INTERVAL_MS از سرور permissions می‌گیرد و DataStore/StateFlow را برای re-compose خودکار Composable ها به‌روز می‌کند
class PermissionPoller(
    private val userPreferencesManager: UserPreferencesManager
) {
 // scope اختصاصی با SupervisorJob تا خطای یک iteration مانع iteration های بعدی نشود
    private val pollerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pollingJob: Job? = null

 // StateFlow داخلی برای انتشار آخرین permissions دریافت‌شده از سرور
    private val _livePermissions = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val livePermissions: StateFlow<Map<String, Boolean>> = _livePermissions.asStateFlow()

 // شروع polling؛ اگر job فعالی وجود داشته باشد ابتدا cancel می‌شود
    fun start() {
        pollingJob?.cancel()
        pollingJob = pollerScope.launch {
            while (isActive) {
                fetchAndApply()
                delay(POLL_INTERVAL_MS)
            }
        }
        Log.d(TAG, "Permission polling started (interval=${POLL_INTERVAL_MS / 1000}s)")
    }

 // توقف polling — هنگام logout یا انتقال به background
    fun stop() {
        pollingJob?.cancel()
        pollingJob = null
        Log.d(TAG, "Permission polling stopped")
    }

 // پایان کامل چرخه حیات: pollerScope را نیز cancel می‌کند؛ باید در onDispose فراخوانی شود وگرنه scope زنده می‌ماند
    fun destroy() {
        pollingJob?.cancel()
        pollingJob = null
        pollerScope.cancel()
        Log.d(TAG, "PermissionPoller destroyed")
    }

 // دریافت فوری permissions از سرور، مثلاً پس از بازگشت از background یا refresh دستی
    fun fetchNow() {
        pollerScope.launch { fetchAndApply() }
    }

 // ===== PRIVATE HELPERS =====

    private suspend fun fetchAndApply() {
        try {
            val username     = userPreferencesManager.username.first()
            val deviceId     = userPreferencesManager.deviceId.first()
            val sessionToken = userPreferencesManager.sessionToken.first()

            if (username.isEmpty()) {
                Log.w(TAG, "No active user — skipping poll")
                return
            }

            val request  = PermissionSyncRequest(username, deviceId, sessionToken)
            val response = RetrofitClient.apiServiceV2.syncPermissions(request)

 // ۴۰۱ باید پیش از isSuccessful چک شود، وگرنه پاک‌سازی نشست منقضی هرگز اجرا نمی‌شد
            if (response.code() == 401) {
                Log.w(TAG, "Session expired, clearing credentials")
                userPreferencesManager.clearUserCredentials()
                stop()
                return
            }

            if (!response.isSuccessful) {
                Log.w(TAG, "Sync failed: HTTP ${response.code()}")
                return
            }

            val body = response.body()
            if (body?.success == true) {
                val newPerms = body.permissions ?: emptyMap()

 // فقط در صورت تغییر واقعی، DataStore و StateFlow را به‌روز کن
                val current = _livePermissions.value
                if (current != newPerms) {
                    userPreferencesManager.savePermissions(newPerms)
                    _livePermissions.value = newPerms
                    Log.i(TAG, "Permissions updated for $username → ${newPerms.keys}")
                }
            }
        } catch (e: Exception) {
 // خطاهای شبکه سایلنت handle می‌شوند تا polling ادامه یابد
            Log.w(TAG, "Poll error: ${e.message}")
        }
    }
}
