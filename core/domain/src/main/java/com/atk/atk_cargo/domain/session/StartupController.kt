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
    // وقتی یک صفحه در میانه‌ی کار متوجه می‌شود نشست واقعاً باطل شده: پیام را نمایش می‌دهد و کاربر را به صفحه‌ی ورود برمی‌گرداند
    fun notifySessionExpired()
    // نمایش یک پیام گذرا (مثلاً خطای شبکه) بدون تغییر وضعیت نشست؛ با Toast سطح Activity نمایش داده می‌شود تا با ناوبری صفحه از بین نرود
    fun showMessage(message: String)
}
