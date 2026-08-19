package com.atk.atk_cargo.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.atk.atk_cargo.domain.session.UserPreferencesStore
import com.atk.atk_cargo.feature.chat.data.ChatPreferencesStore
import com.atk.atk_cargo.security.CryptoManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesManager(
    private val context: Context,
    private val cryptoManager: CryptoManager = CryptoManager()
) : TokenStore, UserPreferencesStore, ChatPreferencesStore {
    private val dataStore: DataStore<Preferences> = context.dataStore

    // خواندن IOException یک‌بار در یک نقطه (به‌جای ۹+ بار تکرار همان ۷ خط catch)؛
    // خطای دیگری غیر از IOException همچنان پرتاب می‌شود، فقط خطای عدم دسترسی به
    // دیسک با preferences خالی جایگزین می‌شود
    private val safePreferences: Flow<Preferences> = dataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }

    private fun <T> preference(key: Preferences.Key<T>, default: T): Flow<T> =
        safePreferences.map { it[key] ?: default }

    override val username: Flow<String> = preference(USERNAME_KEY, "").map { cryptoManager.decrypt(it) }

    override val userType: Flow<String> = preference(USER_TYPE_KEY, "")

    override val permissions: Flow<Map<String, Boolean>> = preference(PERMISSIONS_KEY, "").map { encryptedJson ->
        val json = cryptoManager.decrypt(encryptedJson).ifEmpty { "{}" }
        try {
            val type = object : TypeToken<Map<String, Boolean>>() {}.type
            Gson().fromJson<Map<String, Boolean>>(json, type) ?: emptyMap()
        } catch (_: Exception) {
            emptyMap()
        }
    }

    override val deviceId: Flow<String> = preference(DEVICE_ID_KEY, "")

    override val sessionToken: Flow<String> = preference(SESSION_TOKEN_KEY, "").map { cryptoManager.decrypt(it) }

    // I-05: refresh token — فقط توسط TokenAuthenticator خوانده می‌شود.
    val refreshToken: Flow<String> = preference(REFRESH_TOKEN_KEY, "").map { cryptoManager.decrypt(it) }

    val hardwareScore: Flow<Int> = preference(HARDWARE_SCORE_KEY, -1)

    val loadingNotificationsEnabled: Flow<Boolean> = preference(LOADING_NOTIFICATIONS_ENABLED_KEY, true)

    val chatNotificationsEnabled: Flow<Boolean> = preference(CHAT_NOTIFICATIONS_ENABLED_KEY, true)

    override val chatFontSize: Flow<Int> = preference(CHAT_FONT_SIZE_KEY, 14)

    override val chatMyBubbleColor: Flow<Long> = preference(CHAT_MY_BUBBLE_COLOR_KEY, 0xFF1E88E5) // Blue

    override val chatOtherBubbleColor: Flow<Long> = preference(CHAT_OTHER_BUBBLE_COLOR_KEY, 0xFFFFFFFF) // White

    override val chatBackgroundId: Flow<Int> = preference(CHAT_BACKGROUND_ID_KEY, 0)

    override val chatBubbleShape: Flow<Int> = preference(CHAT_BUBBLE_SHAPE_KEY, 0)

    val lastNotifiedMessageId: Flow<Int> = preference(LAST_NOTIFIED_MESSAGE_ID_KEY, 0)

    // ===== رنگ تم برنامه =====
    val themeColor: Flow<Long> = preference(APP_THEME_COLOR_KEY, DEFAULT_THEME_COLOR)

    // ===== پیاده‌سازی TokenStore (مرز core:network — DEEP_CODE_AUDIT.md #Phase4.2) =====
    override suspend fun getUsername(): String = username.first()
    override suspend fun getDeviceId(): String = deviceId.first()
    override suspend fun getSessionToken(): String = sessionToken.first()
    override suspend fun getRefreshToken(): String = refreshToken.first()
    override suspend fun clearCredentials() = clearUserCredentials()

    override suspend fun saveUserCredentials(username: String, userType: String, deviceId: String, sessionToken: String, permissions: Map<String, Boolean>?) {
        dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = cryptoManager.encrypt(username)
            preferences[USER_TYPE_KEY] = userType
            preferences[IS_LOGGED_IN_KEY] = true
            if (deviceId.isNotEmpty()) {
                preferences[DEVICE_ID_KEY] = deviceId
            }
            if (sessionToken.isNotEmpty()) {
                preferences[SESSION_TOKEN_KEY] = cryptoManager.encrypt(sessionToken)
            }
            if (permissions != null) {
                val json = Gson().toJson(permissions)
                preferences[PERMISSIONS_KEY] = cryptoManager.encrypt(json)
            }
        }
        // AuthSession باید بلافاصله (نه با تأخیر خواندن مجدد DataStore) به‌روز شود
        // چون درخواست‌های بعدی API فوراً به این مقادیر برای هدرهای احراز هویت نیاز دارند
        AuthSession.username = username
        if (deviceId.isNotEmpty()) AuthSession.deviceId = deviceId
        if (sessionToken.isNotEmpty()) AuthSession.sessionToken = sessionToken
    }

    override suspend fun saveSessionToken(sessionToken: String) {
        dataStore.edit { preferences ->
            preferences[SESSION_TOKEN_KEY] = cryptoManager.encrypt(sessionToken)
        }
        AuthSession.sessionToken = sessionToken
    }

    /**
     * ذخیره‌ی جفت توکن تازه بعد از یک POST /auth/refresh موفق (I-05). جدا از
     * saveSessionToken چون همیشه هر دو مقدار با هم rotate می‌شوند، نه جداگانه.
     */
    override suspend fun saveRefreshedTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[SESSION_TOKEN_KEY] = cryptoManager.encrypt(accessToken)
            preferences[REFRESH_TOKEN_KEY] = cryptoManager.encrypt(refreshToken)
        }
        AuthSession.sessionToken = accessToken
        AuthSession.refreshToken = refreshToken
    }

    override suspend fun saveRefreshToken(refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[REFRESH_TOKEN_KEY] = cryptoManager.encrypt(refreshToken)
        }
        AuthSession.refreshToken = refreshToken
    }

    suspend fun savePermissions(permissions: Map<String, Boolean>) {
        dataStore.edit { preferences ->
            val json = Gson().toJson(permissions)
            preferences[PERMISSIONS_KEY] = cryptoManager.encrypt(json)
        }
    }

    override suspend fun setLoginState(isLoggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN_KEY] = isLoggedIn
        }
    }

    suspend fun setLoadingNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[LOADING_NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }

    suspend fun setChatNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[CHAT_NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }

    suspend fun hasBatteryOptimizationBeenRequested(): Boolean {
        return preference(BATTERY_OPTIMIZATION_REQUESTED_KEY, false).first()
    }

    suspend fun markBatteryOptimizationRequested() {
        dataStore.edit { preferences ->
            preferences[BATTERY_OPTIMIZATION_REQUESTED_KEY] = true
        }
    }

    suspend fun getLastSessionVerifiedTimestamp(): Long {
        return preference(LAST_SESSION_VERIFIED_TIMESTAMP_KEY, 0L).first()
    }

    suspend fun saveLastSessionVerifiedTimestamp(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_SESSION_VERIFIED_TIMESTAMP_KEY] = timestamp
        }
    }

    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN_KEY] = false
        }
    }

    suspend fun saveHardwareScore(score: Int) {
        dataStore.edit { preferences ->
            preferences[HARDWARE_SCORE_KEY] = score
        }
    }

    override suspend fun clearUserCredentials() {
        dataStore.edit { preferences ->
            preferences.remove(USERNAME_KEY)
            preferences.remove(USER_TYPE_KEY)
            preferences.remove(SESSION_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(PERMISSIONS_KEY)
            preferences[IS_LOGGED_IN_KEY] = false
        }
        AuthSession.clear()

        // پاکسازی ترجیحات مربوط به بارگیری
        context.getSharedPreferences("loading_alerts", Context.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences("LoadingCheckPrefs", Context.MODE_PRIVATE).edit().clear().apply()
    }

    override suspend fun saveChatSettings(fontSize: Int, myColor: Long, otherColor: Long, backgroundId: Int, bubbleShape: Int) {
        dataStore.edit { preferences ->
            preferences[CHAT_FONT_SIZE_KEY] = fontSize
            preferences[CHAT_MY_BUBBLE_COLOR_KEY] = myColor
            preferences[CHAT_OTHER_BUBBLE_COLOR_KEY] = otherColor
            preferences[CHAT_BACKGROUND_ID_KEY] = backgroundId
            preferences[CHAT_BUBBLE_SHAPE_KEY] = bubbleShape
        }
    }

    suspend fun saveLastNotifiedMessageId(id: Int) {
        dataStore.edit { preferences ->
            preferences[LAST_NOTIFIED_MESSAGE_ID_KEY] = id
        }
    }

    suspend fun saveThemeColor(color: Long) {
        dataStore.edit { preferences ->
            preferences[APP_THEME_COLOR_KEY] = color
        }
    }

    companion object {
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val USER_TYPE_KEY = stringPreferencesKey("user_type")
        private val DEVICE_ID_KEY = stringPreferencesKey("device_id")
        private val SESSION_TOKEN_KEY = stringPreferencesKey("session_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val PERMISSIONS_KEY = stringPreferencesKey("user_permissions")
        private val HARDWARE_SCORE_KEY = intPreferencesKey("hardware_score")
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
        private val BATTERY_OPTIMIZATION_REQUESTED_KEY = booleanPreferencesKey("battery_optimization_requested")
        private val LAST_SESSION_VERIFIED_TIMESTAMP_KEY = longPreferencesKey("last_session_verified_timestamp")

        // Chat Settings
        private val CHAT_FONT_SIZE_KEY = intPreferencesKey("chat_font_size")
        private val CHAT_MY_BUBBLE_COLOR_KEY = longPreferencesKey("chat_my_bubble_color")
        private val CHAT_OTHER_BUBBLE_COLOR_KEY = longPreferencesKey("chat_other_bubble_color")
        private val CHAT_BACKGROUND_ID_KEY = intPreferencesKey("chat_background_id")
        private val CHAT_BUBBLE_SHAPE_KEY = intPreferencesKey("chat_bubble_shape")

        // Notification
        private val LAST_NOTIFIED_MESSAGE_ID_KEY = intPreferencesKey("last_notified_message_id")
        private val LOADING_NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("loading_notifications_enabled")
        private val CHAT_NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("chat_notifications_enabled")

        // Theme
        val APP_THEME_COLOR_KEY = longPreferencesKey("app_theme_color")
        const val DEFAULT_THEME_COLOR = 0xFF137fecL
    }
}
