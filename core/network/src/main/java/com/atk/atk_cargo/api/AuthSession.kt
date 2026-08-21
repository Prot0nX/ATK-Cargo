package com.atk.atk_cargo.api

// نگه‌دارنده‌ی درون‌حافظه‌ی هویت نشست فعلی برای افزودن خودکار هدرهای احراز هویت به هر درخواست، بدون نیاز به پاس دادن دستی در هر Repository/ApiService
object AuthSession {
    @Volatile
    var username: String = ""

    @Volatile
    var deviceId: String = ""

    @Volatile
    var sessionToken: String = ""

    // refresh token هرگز در هدر درخواست‌های معمولی فرستاده نمی‌شود، فقط توسط TokenAuthenticator برای POST /auth/refresh خوانده می‌شود
    @Volatile
    var refreshToken: String = ""

    fun clear() {
        username = ""
        deviceId = ""
        sessionToken = ""
        refreshToken = ""
    }
}
