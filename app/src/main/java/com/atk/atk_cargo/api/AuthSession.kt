package com.atk.atk_cargo.api

/**
 * نگه‌دارنده‌ی درون‌حافظه‌ی هویت نشست فعلی برای افزودن خودکار هدرهای احراز هویت
 * به هر درخواست (توسط RetrofitClient) بدون نیاز به پاس دادن دستی این مقادیر در
 * هر Repository/ApiService. مقدار اولیه‌ی آن هنگام راه‌اندازی اپ از
 * UserPreferencesManager خوانده می‌شود (AtkCargoApplication) و پس از آن توسط
 * UserPreferencesManager با هر ورود/خروج به‌روز نگه داشته می‌شود.
 */
object AuthSession {
    @Volatile
    var username: String = ""

    @Volatile
    var deviceId: String = ""

    @Volatile
    var sessionToken: String = ""

    fun clear() {
        username = ""
        deviceId = ""
        sessionToken = ""
    }
}
