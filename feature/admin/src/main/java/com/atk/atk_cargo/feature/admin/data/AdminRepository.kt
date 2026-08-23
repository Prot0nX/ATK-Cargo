package com.atk.atk_cargo.feature.admin.data

import com.atk.atk_cargo.data.model.CreateUserRequest
import com.atk.atk_cargo.data.model.UpdateUserRequest
import com.atk.atk_cargo.data.model.User

// قرارداد لایه داده مدیریت کاربران که ارتباط مستقیم UserManagementViewModel با ApiServiceV2 را حذف می‌کند
interface AdminRepository {

    // اگر users/status در دسترس نباشد (سرور قدیمی)، به‌صورت داخلی به getAllUsers بازمی‌گردد
    suspend fun fetchUsersWithStatus(): List<User>

    suspend fun updateUser(request: UpdateUserRequest): AdminOperationResult

    suspend fun deleteUser(userId: Int): AdminOperationResult

    suspend fun forceLogoutUser(user: User): ForceLogoutResult

    suspend fun createUser(request: CreateUserRequest): AdminOperationResult
}

sealed class AdminOperationResult {
    data object Success : AdminOperationResult()
    data class Failure(val message: String) : AdminOperationResult()
}

sealed class ForceLogoutResult {
    data class Success(val message: String) : ForceLogoutResult()
    data class Failure(val message: String) : ForceLogoutResult()
}
