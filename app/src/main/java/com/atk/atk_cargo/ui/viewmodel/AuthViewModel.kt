package com.atk.atk_cargo.ui.viewmodel

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atk.atk_cargo.api.ApiService
import com.atk.atk_cargo.api.LoginRequest
import com.atk.atk_cargo.api.SessionResponse
import com.atk.atk_cargo.api.UserPreferencesManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.atk.atk_cargo.utils.hashPassword

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class AuthViewModel(
    private val apiService: ApiService,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    fun login(username: String, password: String, appVersion: String, deviceId: String) {
        if (username.isBlank() || password.isBlank()) {
            _loginState.value = LoginUiState.Error("نام کاربری یا رمز عبور نمی‌تواند خالی باشد.")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            try {
                val hashedPassword = hashPassword(password)
                val deviceModel = Build.MODEL ?: "Unknown"
                val androidVersion = Build.VERSION.RELEASE ?: "Unknown"

                val loginRequest = LoginRequest(
                    username = username,
                    password = hashedPassword,
                    userType = "",
                    deviceModel = deviceModel,
                    deviceId = deviceId,
                    androidVersion = androidVersion,
                    appVersion = appVersion
                )

                val response = apiService.checkLogin(loginRequest)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && responseBody.success) {
                        responseBody.sessionToken?.let { sessionToken ->
                            userPreferencesManager.saveSessionToken(sessionToken)
                        }

                        userPreferencesManager.saveUserCredentials(
                            username,
                            responseBody.userType ?: "",
                            deviceId,
                            responseBody.sessionToken ?: "",
                            responseBody.permissions
                        )
                        userPreferencesManager.setLoginState(true)
                        _loginState.value = LoginUiState.Success
                    } else {
                        _loginState.value = LoginUiState.Error(responseBody?.message ?: "خطا در ورود")
                    }
                } else {
                    val errorMessage = if (response.code() == 409) {
                        try {
                            val errorBody = response.errorBody()?.string()
                            val gson = Gson()
                            val errorResponse = gson.fromJson(errorBody, SessionResponse::class.java)
                            errorResponse?.message ?: "شما در حال حاضر از دستگاه دیگری وارد شده‌اید."
                        } catch (_: Exception) {
                            "شما در حال حاضر از دستگاه دیگری وارد شده‌اید."
                        }
                    } else {
                        when (response.code()) {
                            401 -> "نام کاربری یا رمز عبور اشتباه است"
                            403 -> "دسترسی مجاز نیست"
                            500 -> "خطای سرور"
                            else -> "خطا در اتصال"
                        }
                    }
                    _loginState.value = LoginUiState.Error(errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is java.net.UnknownHostException -> "عدم دسترسی به اینترنت"
                    is java.net.SocketTimeoutException -> "زمان اتصال به پایان رسید"
                    else -> "خطا در اتصال"
                }
                _loginState.value = LoginUiState.Error(errorMessage)
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginUiState.Idle
    }


}
