package com.atk.atk_cargo.domain.session

import kotlinx.coroutines.flow.Flow

/**
 * انتزاع نازک روی UserPreferencesManager برای تنظیمات تم/اعلان — همان الگوی
 * UserPreferencesStore (Phase4 #29). home (تنها مصرف‌کننده‌ی themeColor/
 * hardwareScore/notification flags) نمی‌تواند به UserPreferencesManager
 * concrete وابسته شود.
 */
interface UserSettingsStore {
    val themeColor: Flow<Long>
    val hardwareScore: Flow<Int>
    val loadingNotificationsEnabled: Flow<Boolean>
    val chatNotificationsEnabled: Flow<Boolean>

    suspend fun saveThemeColor(color: Long)
    suspend fun setLoadingNotificationsEnabled(enabled: Boolean)
    suspend fun setChatNotificationsEnabled(enabled: Boolean)
}
