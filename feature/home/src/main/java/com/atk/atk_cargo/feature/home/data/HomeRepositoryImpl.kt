package com.atk.atk_cargo.feature.home.data

import com.atk.atk_cargo.api.ApiServiceV2
import com.atk.atk_cargo.api.ApiV2Routes
import com.atk.atk_cargo.data.model.UpdateUserRequest
import com.atk.atk_cargo.data.model.User

class HomeRepositoryImpl(
    private val apiServiceV2: ApiServiceV2
) : HomeRepository {

    override suspend fun getSelfProfile(): User = apiServiceV2.getSelfProfile()

    override suspend fun changePassword(
        user: User,
        currentPassword: String,
        newPassword: String
    ): ChangePasswordResult {
        val updateRequest = UpdateUserRequest(
            id = user.id,
            username = user.username,
            fullName = null,
            password = newPassword,
            currentPassword = currentPassword,
            userType = user.userType
        )
        val response = apiServiceV2.updateUser(
            request = updateRequest,
            route = ApiV2Routes.userUpdate(updateRequest.id)
        )
        return if (response.success) {
            ChangePasswordResult.Success
        } else {
            ChangePasswordResult.Failure(response.message)
        }
    }
}
