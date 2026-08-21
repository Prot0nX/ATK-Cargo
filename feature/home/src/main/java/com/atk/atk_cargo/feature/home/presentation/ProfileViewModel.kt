package com.atk.atk_cargo.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atk.atk_cargo.api.ApiServiceV2
import com.atk.atk_cargo.api.ApiV2Routes
import com.atk.atk_cargo.data.model.UpdateUserRequest
import com.atk.atk_cargo.data.model.User
import kotlinx.coroutines.launch

// تماس‌های شبکه با viewModelScope اجرا می‌شوند تا با خروج از صفحه کنسل نشوند
class ProfileViewModel(
    private val apiServiceV2: ApiServiceV2
) : ViewModel() {

    fun loadSelfProfile(
        onSuccess: (User) -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                onSuccess(apiServiceV2.getSelfProfile())
            } catch (_: Exception) {
                onError()
            }
        }
    }

    fun changePassword(
        user: User,
        currentPassword: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit,
        onError: (String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
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
                if (response.success) {
                    onSuccess()
                } else {
                    onFailure(response.message)
                }
            } catch (e: Exception) {
                onError(e.message)
            }
        }
    }
}
