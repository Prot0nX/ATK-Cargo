package com.atk.atk_cargo.feature.auth.domain

import com.atk.atk_cargo.feature.auth.data.AuthRepository
import com.atk.atk_cargo.feature.auth.data.LoginResult

// ===== TYPES =====

/**
 * Use Case ورود کاربر.
 * نقطه تماس یکتا بین ViewModel و لایه داده برای عملیات Login.
 * تمام منطق ارسال درخواست، هشینگ و ذخیره‌سازی داخل AuthRepository است.
 */
class LoginUseCase(
    private val authRepository: AuthRepository
) {
    /**
     * @param username نام کاربری (تریم‌شده)
     * @param password رمز عبور خام
     * @param appVersion نسخه جاری برنامه
     */
    suspend operator fun invoke(
        username: String,
        password: String,
        appVersion: String
    ): LoginResult {
        if (username.isBlank() || password.isBlank()) {
            return LoginResult.Error("نام کاربری یا رمز عبور نمی‌تواند خالی باشد.")
        }
        return authRepository.login(username, password, appVersion)
    }
}
