package com.atk.atk_cargo.domain.session

import kotlinx.coroutines.flow.StateFlow

// انتزاع نازک روی StartupViewModel، محدود به همان چیزی که از طریق LocalStartupViewModel خوانده می‌شود، تا چرخه‌ی وابستگی نسازد
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
