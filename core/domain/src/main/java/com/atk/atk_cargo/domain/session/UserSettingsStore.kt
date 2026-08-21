package com.atk.atk_cargo.domain.session

import kotlinx.coroutines.flow.Flow

// انتزاع نازک روی UserPreferencesManager برای تنظیمات تم/اعلان تا home به کلاس concrete وابسته نشود
interface UserSettingsStore {
    val themeColor: Flow<Long>
    val loadingNotificationsEnabled: Flow<Boolean>
    val chatNotificationsEnabled: Flow<Boolean>

    suspend fun saveThemeColor(color: Long)
    suspend fun setLoadingNotificationsEnabled(enabled: Boolean)
    suspend fun setChatNotificationsEnabled(enabled: Boolean)
}
