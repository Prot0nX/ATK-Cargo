package com.atk.atk_cargo.feature.auth.domain

import com.atk.atk_cargo.feature.auth.data.AuthRepository
import com.atk.atk_cargo.feature.auth.data.LoginResult

// ===== TYPES =====

// Use Case ورود کاربر؛ نقطه تماس یکتا بین ViewModel و AuthRepository
class LoginUseCase(
    private val authRepository: AuthRepository
) {
    // پارامترها: نام کاربری تریم‌شده، رمز خام، نسخه جاری برنامه
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
