package com.atk.atk_cargo.feature.auth.data

import kotlinx.coroutines.flow.Flow

// ===== TYPES / INTERFACES =====

// قرارداد لایه داده احراز هویت که پیاده‌سازی را از ViewModel پنهان می‌کند
interface AuthRepository {

    // ورود کاربر با هش‌کردن رمز عبور و ارسال آن همراه مشخصات دستگاه به سرور
    suspend fun login(
        username: String,
        password: String,
        appVersion: String
    ): LoginResult

    // Flow نام کاربری جاری
    val currentUsername: Flow<String>

    // پاکسازی اطلاعات نشست کاربر
    suspend fun clearSession()
}

// ===== TYPES =====

// نتیجه عملیات ورود
sealed class LoginResult {
    data object Success : LoginResult()
    data class Error(val message: String) : LoginResult()
    data class ConflictSession(val message: String) : LoginResult()
}
