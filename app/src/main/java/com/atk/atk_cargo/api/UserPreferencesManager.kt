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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesManager(private val context: Context) {
    private val dataStore: DataStore<Preferences> = context.dataStore

    val username = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[USERNAME_KEY] ?: ""
        }

    val userType = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[USER_TYPE_KEY] ?: ""
        }

    val deviceId = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[DEVICE_ID_KEY] ?: ""
        }

    val sessionToken = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[SESSION_TOKEN_KEY] ?: ""
        }

    val hardwareScore = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[HARDWARE_SCORE_KEY] ?: -1
        }

    val deviceSpecs = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[DEVICE_SPECS_KEY] ?: ""
        }

    val isLoggedIn = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[IS_LOGGED_IN_KEY] ?: false
        }

    suspend fun saveUserCredentials(username: String, userType: String, deviceId: String = "", sessionToken: String = "") {
        dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = username
            preferences[USER_TYPE_KEY] = userType
            preferences[IS_LOGGED_IN_KEY] = true
            if (deviceId.isNotEmpty()) {
                preferences[DEVICE_ID_KEY] = deviceId
            }
            if (sessionToken.isNotEmpty()) {
                preferences[SESSION_TOKEN_KEY] = sessionToken
            }
        }
    }

    suspend fun saveSessionToken(sessionToken: String) {
        dataStore.edit { preferences ->
            preferences[SESSION_TOKEN_KEY] = sessionToken
        }
    }

    suspend fun clearSessionToken() {
        dataStore.edit { preferences ->
            preferences.remove(SESSION_TOKEN_KEY)
        }
    }

    suspend fun setLoginState(isLoggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN_KEY] = isLoggedIn
        }
    }

    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN_KEY] = false
        }
    }

    suspend fun saveHardwareScore(score: Int, deviceSpecs: String) {
        dataStore.edit { preferences ->
            preferences[HARDWARE_SCORE_KEY] = score
            preferences[DEVICE_SPECS_KEY] = deviceSpecs
            preferences[SCORE_TIMESTAMP_KEY] = System.currentTimeMillis()
        }
    }

    suspend fun getHardwareScore(): Int {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[HARDWARE_SCORE_KEY] ?: -1
            }.first()
    }

    suspend fun getDeviceSpecs(): String {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[DEVICE_SPECS_KEY] ?: ""
            }.first()
    }

    suspend fun getScoreTimestamp(): Long {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[SCORE_TIMESTAMP_KEY] ?: 0L
            }.first()
    }

    suspend fun clearHardwareScore() {
        dataStore.edit { preferences ->
            preferences.remove(HARDWARE_SCORE_KEY)
            preferences.remove(DEVICE_SPECS_KEY)
            preferences.remove(SCORE_TIMESTAMP_KEY)
        }
    }

    suspend fun clearUserCredentials() {
        dataStore.edit { preferences ->
            preferences.remove(USERNAME_KEY)
            preferences.remove(USER_TYPE_KEY)
            preferences.remove(SESSION_TOKEN_KEY)
            preferences[IS_LOGGED_IN_KEY] = false
        }

        // پاکسازی ترجیحات مربوط به بارگیری
        context.getSharedPreferences("loading_alerts", Context.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences("LoadingCheckPrefs", Context.MODE_PRIVATE).edit().clear().apply()
    }

    companion object {
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val USER_TYPE_KEY = stringPreferencesKey("user_type")
        private val DEVICE_ID_KEY = stringPreferencesKey("device_id")
        private val SESSION_TOKEN_KEY = stringPreferencesKey("session_token")
        private val HARDWARE_SCORE_KEY = intPreferencesKey("hardware_score")
        private val DEVICE_SPECS_KEY = stringPreferencesKey("device_specs")
        private val SCORE_TIMESTAMP_KEY = longPreferencesKey("score_timestamp")
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
    }
}