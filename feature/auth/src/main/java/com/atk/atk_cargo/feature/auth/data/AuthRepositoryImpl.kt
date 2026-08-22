package com.atk.atk_cargo.feature.auth.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import com.atk.atk_cargo.api.ApiServiceV2
import com.atk.atk_cargo.data.model.LoginRequest
import com.atk.atk_cargo.data.model.SessionResponse
import com.atk.atk_cargo.domain.session.UserPreferencesStore
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID

// ===== DEPENDENCIES / IMPORTS =====

// پیاده‌سازی AuthRepository که تمام ارتباط با API و DataStore را متمرکز می‌کند
class AuthRepositoryImpl(
    private val context: Context,
    private val apiServiceV2: ApiServiceV2,
    private val userPreferencesManager: UserPreferencesStore
) : AuthRepository {

    // ===== SERVICES =====

    // شناسه پایدار دستگاه: ابتدا ANDROID_ID، در صورت نبود از UUID ذخیره‌شده در DataStore
    @SuppressLint("HardwareIds")
    private suspend fun getOrCreateDeviceId(): String {
        return withContext(Dispatchers.IO) {
            try {
                val androidId = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ANDROID_ID
                )
                if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
                    return@withContext androidId
                }
            } catch (_: Exception) { /* Fallback */ }

            // Fallback: UUID ذخیره‌شده در DataStore
            val storedDeviceId = userPreferencesManager.deviceId.first()
            if (storedDeviceId.isNotBlank()) {
                return@withContext storedDeviceId
            }

            // ایجاد UUID جدید و ذخیره دائمی در DataStore
            val newDeviceId = UUID.randomUUID().toString()
            userPreferencesManager.saveUserCredentials(
                username = userPreferencesManager.username.first(),
                userType = userPreferencesManager.userType.first(),
                deviceId = newDeviceId
            )
            newDeviceId
        }
    }

    // ===== CORE LOGIC =====

    override suspend fun login(
        username: String,
        password: String,
        appVersion: String
    ): LoginResult = withContext(Dispatchers.IO) {
        try {
            val deviceId = getOrCreateDeviceId()
            val deviceModel = Build.MODEL ?: "Unknown"
            val androidVersion = Build.VERSION.RELEASE ?: "Unknown"

            val loginRequest = LoginRequest(
                username = username,
                password = password,
                userType = "",
                deviceModel = deviceModel,
                deviceId = deviceId,
                androidVersion = androidVersion,
                appVersion = appVersion
            )

            val response = apiServiceV2.checkLogin(loginRequest)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    body.sessionToken?.let { token ->
                        userPreferencesManager.saveSessionToken(token)
                    }
                    // اگر سرور قدیمی این فیلد را نفرستد، refreshToken چیزی ذخیره نمی‌شود
                    body.refreshToken?.let { refreshToken ->
                        userPreferencesManager.saveRefreshToken(refreshToken)
                    }
                    userPreferencesManager.saveUserCredentials(
                        username = username,
                        userType = body.userType ?: "",
                        deviceId = deviceId,
                        sessionToken = body.sessionToken ?: "",
                        permissions = body.permissions
                    )
                    userPreferencesManager.setLoginState(true)
                    LoginResult.Success
                } else {
                    LoginResult.Error(body?.message ?: "خطا در ورود")
                }
            } else {
                if (response.code() == 409) {
                    val conflictMessage = try {
                        val errorBody = response.errorBody()?.string()
                        Gson().fromJson(errorBody, SessionResponse::class.java)?.message
                            ?: "شما در حال حاضر از دستگاه دیگری وارد شده‌اید."
                    } catch (_: Exception) {
                        "شما در حال حاضر از دستگاه دیگری وارد شده‌اید."
                    }
                    LoginResult.ConflictSession(conflictMessage)
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "نام کاربری یا رمز عبور اشتباه است"
                        403 -> "دسترسی مجاز نیست"
                        // قبلاً هرگز از سرور نمی‌رسید (locked login همیشه 200 بود)؛ از نسخه‌ی ۴.۱.۰ به بعد واقعی است (فاز۳ #۲۸)
                        429 -> "تعداد تلاش‌های ناموفق بیش از حد مجاز است. لطفاً ۱۵ دقیقه دیگر تلاش کنید."
                        500 -> "خطای سرور"
                        else -> "خطا در اتصال"
                    }
                    LoginResult.Error(errorMessage)
                }
            }
        } catch (e: SecurityException) {
            LoginResult.Error("خطای امنیتی: امکان پردازش رمز عبور وجود ندارد.")
        } catch (e: java.net.UnknownHostException) {
            LoginResult.Error("عدم دسترسی به اینترنت")
        } catch (e: java.net.SocketTimeoutException) {
            LoginResult.Error("زمان اتصال به پایان رسید")
        } catch (e: Exception) {
            LoginResult.Error("خطا در اتصال")
        }
    }

    override val currentUsername: Flow<String>
        get() = userPreferencesManager.username

    override suspend fun clearSession() {
        userPreferencesManager.clearUserCredentials()
    }
}
