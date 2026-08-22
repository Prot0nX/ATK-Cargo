package com.atk.atk_cargo.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atk.atk_cargo.data.model.User
import com.atk.atk_cargo.feature.home.data.ChangePasswordResult
import com.atk.atk_cargo.feature.home.data.HomeRepository
import kotlinx.coroutines.launch

// تماس‌های شبکه با viewModelScope اجرا می‌شوند تا با خروج از صفحه کنسل نشوند
class ProfileViewModel(
    private val repository: HomeRepository
) : ViewModel() {

    fun loadSelfProfile(
        onSuccess: (User) -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                onSuccess(repository.getSelfProfile())
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
                when (val result = repository.changePassword(user, currentPassword, newPassword)) {
                    is ChangePasswordResult.Success -> onSuccess()
                    is ChangePasswordResult.Failure -> onFailure(result.message)
                }
            } catch (e: Exception) {
                onError(e.message)
            }
        }
    }
}
