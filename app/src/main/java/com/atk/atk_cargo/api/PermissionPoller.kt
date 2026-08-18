package com.atk.atk_cargo.api

// ===== DEPENDENCIES / IMPORTS =====
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

// ===== CONFIGURATION & GLOBALS =====
private const val TAG = "PermissionPoller"

/**
 * فاصله زمانی بین هر بار بررسی permissions از سرور (به میلی‌ثانیه).
 * مقدار پیش‌فرض: ۳ دقیقه. می‌توان در صورت نیاز تغییر داد.
 */
private const val POLL_INTERVAL_MS = 3 * 60 * 1000L // 3 minutes

// ===== CORE LOGIC / IMPLEMENTATION =====

/**
 * PermissionPoller — مدیریت به‌روزرسانی زنده سطوح دسترسی
 *
 * این کلاس یک coroutine ادواری اجرا می‌کند که هر [POLL_INTERVAL_MS]
 * یک‌بار با endpoint سرور ارتباط برقرار کرده و آخرین permissions کاربر جاری
 * را دریافت می‌کند. در صورت تغییر، DataStore و StateFlow به‌روز می‌شوند و
 * هر Composable که از آن‌ها subscribe شده، بدون هیچ تعاملی از سمت کاربر
 * re-compose خواهد شد.
 *
 * چرخه حیات:
 *   - start()  → زمانی که کاربر لاگین کرده و به صفحه اصلی می‌رسد
 *   - stop()   → زمانی که کاربر logout می‌کند یا برنامه به background می‌رود
 */
class PermissionPoller(
    private val userPreferencesManager: UserPreferencesManager
) {
    // scope اختصاصی با SupervisorJob تا خطای یک iteration ،
    // iteration های بعدی را متوقف نکند.
    private val pollerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pollingJob: Job? = null

    // StateFlow داخلی برای انتشار آخرین permissions دریافت‌شده از سرور
    private val _livePermissions = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val livePermissions: StateFlow<Map<String, Boolean>> = _livePermissions.asStateFlow()

    /**
     * شروع polling — معمولاً بلافاصله پس از ورود کاربر فراخوانی می‌شود.
     * اگر قبلاً یک job فعال وجود داشته باشد، ابتدا cancel می‌شود.
     */
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

    /**
     * توقف polling — هنگام logout یا انتقال به background
     */
    fun stop() {
        pollingJob?.cancel()
        pollingJob = null
        Log.d(TAG, "Permission polling stopped")
    }

    /**
     * پایان کامل چرخه حیات — pollerScope را نیز cancel می‌کند.
     * باید هنگام خروج Composable از ترکیب‌بندی (onDispose) فراخوانی شود،
     * در غیر این صورت SupervisorJob و coroutine scope اختصاصی برای همیشه
     * زنده می‌مانند حتی پس از توقف polling.
     */
    fun destroy() {
        pollingJob?.cancel()
        pollingJob = null
        pollerScope.cancel()
        Log.d(TAG, "PermissionPoller destroyed")
    }

    /**
     * دریافت فوری permissions از سرور (مثلاً بلافاصله پس از بازگشت برنامه
     * از background یا در صورت نیاز به refresh دستی).
     */
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

            // response.isSuccessful فقط برای کد ۲۰۰-۲۹۹ true است، پس بررسی
            // response.code() == 401 داخل شاخه‌ی isSuccessful هرگز اجرا
            // نمی‌شد و پاک‌سازی نشست منقضی/force-logout عملاً کد مرده بود
            // (DEEP_CODE_AUDIT.md #Phase1.9). حالا ۴۰۱ قبل از بررسی isSuccessful چک می‌شود.
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
