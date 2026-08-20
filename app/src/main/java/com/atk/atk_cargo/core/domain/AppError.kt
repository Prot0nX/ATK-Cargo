package com.atk.atk_cargo.core.domain

import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * نگاشت خطای متمرکز (DEEP_CODE_REVIEW.md Phase3 #22).
 *
 * دامنه‌ی این نسخه عمداً محدود است: خودِ نوع [AppError] و نگاشت آن هنوز به
 * کدهای فراخوانی‌کننده (ViewModel/Screen) سیم‌کشی نشده‌اند — پیام‌های فعلی
 * هرکدام context-specific هستند (مثلاً «خطا در بررسی شماره قبض باسکول»)
 * و با یک پیام عمومی یکسان جایگزین کردن‌شان یک رگرسیون UX است، نه بهبود.
 * آنچه در همین فاز واقعاً رفع شد باگ مستقل و واقعی گزارش بود: در ۹ فایلی
 * که مستقیماً شبکه را صدا می‌زنند، `catch (e: Exception)` بدون جداسازی
 * `CancellationException` نوشته شده بود؛ چون `CancellationException` در
 * Kotlin زیرمجموعه‌ی `Exception` است، لغو یک coroutine (مثلاً با خروج
 * کاربر از صفحه) به‌اشتباه به‌عنوان «خطای سرور» به کاربر نمایش داده می‌شد.
 * این فایل برای استفاده‌ی تدریجی/آینده نگه داشته شده، نه یک بازنویسی کامل.
 */
sealed interface AppError {
    data object Network : AppError
    data object Timeout : AppError
    data class Server(val code: Int) : AppError
    data class Validation(val message: String) : AppError
}

fun Throwable.toAppError(): AppError = when (this) {
    is CancellationException -> throw this // هرگز نباید بلعیده شود
    is SocketTimeoutException -> AppError.Timeout
    is HttpException -> AppError.Server(code())
    is IOException -> AppError.Network
    else -> AppError.Server(-1)
}

fun AppError.toUserMessage(): String = when (this) {
    AppError.Network -> "اتصال به اینترنت برقرار نیست."
    AppError.Timeout -> "زمان پاسخ سرور به پایان رسید. دوباره تلاش کنید."
    is AppError.Server -> "خطای سرور. لطفاً بعداً تلاش کنید."
    is AppError.Validation -> message
}
