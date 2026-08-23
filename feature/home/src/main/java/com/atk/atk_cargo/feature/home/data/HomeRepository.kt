package com.atk.atk_cargo.feature.home.data

import com.atk.atk_cargo.data.model.User

// قرارداد لایه داده صفحه‌ی پروفایل که ارتباط مستقیم ProfileViewModel با ApiServiceV2 را حذف می‌کند
interface HomeRepository {

    suspend fun getSelfProfile(): User

    suspend fun changePassword(
        user: User,
        currentPassword: String,
        newPassword: String
    ): ChangePasswordResult
}

sealed class ChangePasswordResult {
    data object Success : ChangePasswordResult()
    data class Failure(val message: String) : ChangePasswordResult()
}
