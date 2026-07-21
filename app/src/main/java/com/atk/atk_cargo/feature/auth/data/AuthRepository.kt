package com.atk.atk_cargo.feature.auth.data

import kotlinx.coroutines.flow.Flow

// ===== TYPES / INTERFACES =====

/**
 * قرارداد لایه داده احراز هویت.
 * ViewModel فقط با این اینترفیس کار می‌کند — پیاده‌سازی جزئیات (API، DataStore) از آن پنهان است.
 */
interface AuthRepository {

    /**
     * ورود کاربر با نام کاربری و رمز عبور.
     * پسورد به‌صورت SHA-256 هش می‌شود و همراه مشخصات دستگاه به سرور ارسال می‌شود.
     *
     * @param username نام کاربری
     * @param password رمز عبور (متن خام — هشینگ داخل Repository انجام می‌شود)
     * @param appVersion نسخه برنامه
     * @return نتیجه ورود
     */
    suspend fun login(
        username: String,
        password: String,
        appVersion: String
    ): LoginResult

    /**
     * Flow نام کاربری جاری (برای state آگاهی)
     */
    val currentUsername: Flow<String>

    /**
     * پاکسازی اطلاعات نشست کاربر
     */
    suspend fun clearSession()
}

// ===== TYPES =====

/**
 * نتیجه عملیات ورود — مُهر و موم شده برای ایمنی کامل
 */
sealed class LoginResult {
    data object Success : LoginResult()
    data class Error(val message: String) : LoginResult()
    data class ConflictSession(val message: String) : LoginResult()
}
