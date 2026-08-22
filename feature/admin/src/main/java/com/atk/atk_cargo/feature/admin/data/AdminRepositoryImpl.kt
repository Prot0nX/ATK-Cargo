package com.atk.atk_cargo.feature.admin.data

import com.atk.atk_cargo.api.ApiServiceV2
import com.atk.atk_cargo.api.ApiV2Routes
import com.atk.atk_cargo.data.model.CreateUserRequest
import com.atk.atk_cargo.data.model.DeleteUserRequest
import com.atk.atk_cargo.data.model.ForceLogoutRequest
import com.atk.atk_cargo.data.model.UpdateUserRequest
import com.atk.atk_cargo.data.model.User

class AdminRepositoryImpl(
    private val apiServiceV2: ApiServiceV2
) : AdminRepository {

    override suspend fun fetchUsersWithStatus(): List<User> {
        return try {
            apiServiceV2.getAllUsersWithStatus()
        } catch (_: Exception) {
            apiServiceV2.getAllUsers()
        }
    }

    override suspend fun updateUser(request: UpdateUserRequest): AdminOperationResult {
        val response = apiServiceV2.updateUser(
            request = request,
            route = ApiV2Routes.userUpdate(request.id)
        )
        return if (response.success) {
            AdminOperationResult.Success
        } else {
            AdminOperationResult.Failure(response.message)
        }
    }

    override suspend fun deleteUser(userId: Int): AdminOperationResult {
        val response = apiServiceV2.deleteUser(
            request = DeleteUserRequest(userId = userId),
            route = ApiV2Routes.userDelete(userId)
        )
        return if (response.success) {
            AdminOperationResult.Success
        } else {
            AdminOperationResult.Failure(response.message)
        }
    }

    override suspend fun forceLogoutUser(user: User): ForceLogoutResult {
        val activeDeviceId: String = try {
            val sessionResponse = apiServiceV2.getActiveDeviceId(username = user.username)
            if (sessionResponse.isSuccessful && sessionResponse.body()?.success == true) {
                sessionResponse.body()?.deviceId ?: ""
            } else ""
        } catch (_: Exception) { "" }

        val request = ForceLogoutRequest(
            username = user.username,
            deviceId = activeDeviceId
        )
        val response = apiServiceV2.forceLogoutUser(request)
        return if (response.isSuccessful && response.body()?.success == true) {
            ForceLogoutResult.Success("کاربر ${user.username} با موفقیت از سیستم خارج شد")
        } else {
            ForceLogoutResult.Failure(response.body()?.message ?: "خطا در خروج اجباری کاربر")
        }
    }

    override suspend fun createUser(request: CreateUserRequest): AdminOperationResult {
        val response = apiServiceV2.createUser(request)
        return if (response.isSuccessful && response.body()?.success == true) {
            AdminOperationResult.Success
        } else {
            AdminOperationResult.Failure("خطا در ایجاد کاربر: ${response.errorBody()?.string()}")
        }
    }
}
