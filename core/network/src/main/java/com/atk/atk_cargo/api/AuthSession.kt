package com.atk.atk_cargo.api

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking

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

 // تا زمان پر شدن اولیه از DataStore (AtkCargoApplication.onCreate) کامل نمی‌شود؛ رفع race در cold start که درخواست‌های زودهنگام بدون هدر احراز هویت می‌رفتند
    private val readyDeferred = CompletableDeferred<Unit>()

    fun markReady() {
        readyDeferred.complete(Unit)
    }

    // فقط باید از یک thread پس‌زمینه (مثل thread دیسپچر OkHttp) صدا زده شود، نه از Main
    fun awaitReady() {
        if (!readyDeferred.isCompleted) {
            runBlocking { readyDeferred.await() }
        }
    }

    fun clear() {
        username = ""
        deviceId = ""
        sessionToken = ""
        refreshToken = ""
    }
}
