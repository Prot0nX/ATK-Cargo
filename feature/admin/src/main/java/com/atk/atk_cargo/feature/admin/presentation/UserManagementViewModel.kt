package com.atk.atk_cargo.feature.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atk.atk_cargo.api.ApiServiceV2
import com.atk.atk_cargo.api.ApiV2Routes
import com.atk.atk_cargo.data.model.CreateUserRequest
import com.atk.atk_cargo.data.model.DeleteUserRequest
import com.atk.atk_cargo.data.model.ForceLogoutRequest
import com.atk.atk_cargo.data.model.UpdateUserRequest
import com.atk.atk_cargo.data.model.User
import kotlinx.coroutines.launch

// تماس‌های شبکه با viewModelScope اجرا می‌شوند تا با خروج از صفحه کنسل نشوند؛ نتیجه با callback به Composable برمی‌گردد
class UserManagementViewModel(
    private val apiServiceV2: ApiServiceV2
) : ViewModel() {

    fun fetchUsersWithStatus(
        onSuccess: (List<User>) -> Unit,
        onError: (String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = try {
                    apiServiceV2.getAllUsersWithStatus()
                } catch (_: Exception) {
                    apiServiceV2.getAllUsers()
                }
                onSuccess(response)
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
                val response = apiServiceV2.updateUser(
                    request = request,
                    route = ApiV2Routes.userUpdate(request.id)
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

    fun deleteUser(
        userId: Int,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit,
        onError: (String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val request = DeleteUserRequest(userId = userId)
                val response = apiServiceV2.deleteUser(
                    request = request,
                    route = ApiV2Routes.userDelete(userId)
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

    fun forceLogoutUser(
        user: User,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit,
        onError: (String) -> Unit,
        onFinally: () -> Unit
    ) {
        viewModelScope.launch {
            try {
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
                if (response.isSuccessful && response.body()?.success == true) {
                    onSuccess("کاربر ${user.username} با موفقیت از سیستم خارج شد")
                } else {
                    onFailure(response.body()?.message ?: "خطا در خروج اجباری کاربر")
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
                val response = apiServiceV2.createUser(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    onSuccess()
                } else {
                    onFailure("خطا در ایجاد کاربر: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                onError("خطا در ارتباط: ${e.message}")
            } finally {
                onFinally()
            }
        }
    }
}
