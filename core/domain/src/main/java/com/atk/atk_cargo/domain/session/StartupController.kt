package com.atk.atk_cargo.domain.session

import kotlinx.coroutines.flow.StateFlow

/**
 * انتزاع نازک روی StartupViewModel — همان الگوی UserPreferencesStore/
 * QuotaRepository (Phase4 #29). home (تنها مصرف‌کننده‌ی LocalStartupViewModel/
 * LocalNotificationPermissionRequester) نمی‌تواند به StartupViewModel واقعی
 * (کلاس بزرگ‌تر با UpdateManager/SecurityVerifier/ChatRepository که در app
 * باقی می‌ماند) وابسته شود که چرخه‌ی وابستگی می‌ساخت.
 *
 * دامنه‌ی این اینترفیس دقیقاً همان چیزی است که از طریق LocalStartupViewModel
 * در سراسر پروژه خوانده می‌شود (home + MainScreen.kt در app)، نه کل سطح
 * StartupViewModel.
 */
interface StartupController {
    val isSessionValid: StateFlow<Boolean>
    val pendingNavigationDestination: StateFlow<String?>

    fun consumePendingNavigation()
    fun updateSessionValidity(isValid: Boolean)
    fun startLoadingNotificationService()
    fun stopLoadingNotificationService()
    fun startChatNotificationWorker()
    fun stopChatNotificationService()
}
