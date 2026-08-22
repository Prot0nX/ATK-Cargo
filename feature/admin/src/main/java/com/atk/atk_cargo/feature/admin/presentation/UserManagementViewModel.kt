package com.atk.atk_cargo.feature.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atk.atk_cargo.data.model.CreateUserRequest
import com.atk.atk_cargo.data.model.UpdateUserRequest
import com.atk.atk_cargo.data.model.User
import com.atk.atk_cargo.feature.admin.data.AdminOperationResult
import com.atk.atk_cargo.feature.admin.data.AdminRepository
import com.atk.atk_cargo.feature.admin.data.ForceLogoutResult
import kotlinx.coroutines.launch

// تماس‌های شبکه با viewModelScope اجرا می‌شوند تا با خروج از صفحه کنسل نشوند؛ نتیجه با callback به Composable برمی‌گردد
class UserManagementViewModel(
    private val repository: AdminRepository
) : ViewModel() {

    fun fetchUsersWithStatus(
        onSuccess: (List<User>) -> Unit,
        onError: (String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                onSuccess(repository.fetchUsersWithStatus())
            } catch (e: Exception) {
                onError(e.message)
            }
        }
    }

    fun updateUser(
        request: UpdateUserRequest,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit,
        onError: (String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                when (val result = repository.updateUser(request)) {
                    is AdminOperationResult.Success -> onSuccess()
                    is AdminOperationResult.Failure -> onFailure(result.message)
                }
            } catch (e: Exception) {
                onError(e.message)
            }
        }
    }

    fun deleteUser(
        userId: Int,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit,
        onError: (String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                when (val result = repository.deleteUser(userId)) {
                    is AdminOperationResult.Success -> onSuccess()
                    is AdminOperationResult.Failure -> onFailure(result.message)
                }
            } catch (e: Exception) {
                onError(e.message)
            }
        }
    }

    fun forceLogoutUser(
        user: User,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit,
        onError: (String) -> Unit,
        onFinally: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                when (val result = repository.forceLogoutUser(user)) {
                    is ForceLogoutResult.Success -> onSuccess(result.message)
                    is ForceLogoutResult.Failure -> onFailure(result.message)
                }
            } catch (e: Exception) {
                onError("خطا در ارتباط با سرور: ${e.message}")
            } finally {
                onFinally()
            }
        }
    }

    fun createUser(
        request: CreateUserRequest,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit,
        onError: (String) -> Unit,
        onFinally: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                when (val result = repository.createUser(request)) {
                    is AdminOperationResult.Success -> onSuccess()
                    is AdminOperationResult.Failure -> onFailure(result.message)
                }
            } catch (e: Exception) {
                onError("خطا در ارتباط: ${e.message}")
            } finally {
                onFinally()
            }
        }
    }
}
