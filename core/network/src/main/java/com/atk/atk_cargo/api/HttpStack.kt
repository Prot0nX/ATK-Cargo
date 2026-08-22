package com.atk.atk_cargo.api

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

// OkHttpClient پایه‌ی مشترک بین همه‌ی مسیرهای شبکه‌ی برنامه (Retrofit، رفرش توکن، دانلود آپدیت، تأیید امنیتی)؛
// قبلاً هرکدام OkHttpClient/HttpURLConnection جدا با connection pool مجزا داشتند، یعنی TLS handshake تکراری
// به همان هاست در startup. هر مصرف‌کننده با shared.newBuilder() فقط تفاوت خودش (timeout/authenticator/...)
// را روی همین یک connection pool مشترک اعمال می‌کند (DEEP_CODE_AUDIT.md فاز۳ #۲۷)
object HttpStack {
    // چند صفحه هم‌زمان poll می‌کنند؛ pool بزرگ‌تر از پیش‌فرض OkHttp یعنی اتصالات idle دوباره استفاده می‌شوند نه بسته/باز
    private val sharedConnectionPool = ConnectionPool(10, 5, TimeUnit.MINUTES)

    val shared: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectionPool(sharedConnectionPool)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
}
