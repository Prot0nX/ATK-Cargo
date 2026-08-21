package com.atk.atk_cargo.core.domain

import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

// نگاشت خطای متمرکز (DEEP_CODE_REVIEW.md Phase3 #22)؛ هنوز به ViewModel/Screen سیم‌کشی نشده (پیام‌های context-specific فعلی حفظ می‌شوند)، اما catch(Exception) در ۹ فایل شبکه اصلاح شد تا CancellationException به‌اشتباه «خطای سرور» نمایش داده نشود — این فایل برای استفاده‌ی تدریجی/آینده نگه داشته شده است
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
