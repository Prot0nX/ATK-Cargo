package com.atk.atk_cargo.feature.auth.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atk.atk_cargo.feature.auth.data.LoginResult
import com.atk.atk_cargo.feature.auth.domain.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ===== TYPES / ENUMS =====

// وضعیت جاری UI صفحه ورود
sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

// وضعیت فرم ورود با State Hoisting تا چرخش صفحه ورودی‌ها را پاک نکند
data class LoginFormState(
    val username: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false
)

// ===== CORE LOGIC =====

// ViewModel صفحه ورود: نگهداری حالت فرم و مدیریت چرخه ورود از طریق LoginUseCase
class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val context: Context
) : ViewModel() {

 // ===== STATE =====

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _formState = MutableStateFlow(LoginFormState())
    val formState: StateFlow<LoginFormState> = _formState.asStateFlow()

 // ===== HELPERS =====

    private fun getAppVersion(): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "Unknown"
        } catch (_: Exception) {
            "Unknown"
        }
    }

 // ===== CORE LOGIC =====

 // به‌روزرسانی نام کاربری در حالت فرم و ریست خطا
    fun onUsernameChanged(value: String) {
        _formState.update { it.copy(username = value) }
        if (_loginState.value is LoginUiState.Error) {
            _loginState.value = LoginUiState.Idle
        }
    }

 // به‌روزرسانی رمز عبور (فقط اعداد) در حالت فرم و ریست خطا
    fun onPasswordChanged(value: String) {
        _formState.update { it.copy(password = value.filter { c -> c.isDigit() }) }
        if (_loginState.value is LoginUiState.Error) {
            _loginState.value = LoginUiState.Idle
        }
    }

 // تغییر وضعیت نمایش/پنهان کردن رمز عبور
    fun togglePasswordVisibility() {
        _formState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

 // شروع فرآیند ورود؛ هشینگ و درخواست شبکه داخل AuthRepository انجام می‌شود
    fun login() {
        val form = _formState.value
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            val result = loginUseCase(
                username = form.username.trim(),
                password = form.password,
                appVersion = getAppVersion()
            )
            _loginState.value = when (result) {
                is LoginResult.Success          -> LoginUiState.Success
                is LoginResult.Error            -> LoginUiState.Error(result.message)
                is LoginResult.ConflictSession  -> LoginUiState.Error(result.message)
            }
        }
    }

 // بازنشانی کامل وضعیت ورود و فرم هنگام خروج کاربر
    fun resetState() {
        _loginState.value = LoginUiState.Idle
        _formState.value = LoginFormState()
    }
}
