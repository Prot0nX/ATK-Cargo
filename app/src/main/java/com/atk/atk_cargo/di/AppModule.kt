package com.atk.atk_cargo.di

// ارتقای DSL ویومدل به Koin 4.x چندسکویی در org.koin.core.module.dsl.
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.api.TokenStore
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.data.db.AppDatabase
import com.atk.atk_cargo.domain.session.UserPreferencesStore
import com.atk.atk_cargo.domain.session.UserSettingsStore
import com.atk.atk_cargo.feature.chat.data.ChatPreferencesStore
import com.atk.atk_cargo.security.CryptoManager
import com.atk.atk_cargo.security.SecurityVerifier
import com.atk.atk_cargo.startup.StartupViewModel
import com.atk.atk_cargo.startup.data.StartupSessionRepository
import com.atk.atk_cargo.startup.data.StartupSessionRepositoryImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

// فقط زیرساخت مشترک بین فیچرها (احراز هویت نشست، دیتابیس محلی، امنیت) اینجا می‌ماند.
val appModule = module {
 // ===== API Service =====
 // استفاده انحصاری از Router v2 به عنوان تنها API stack کلاینت.
    single { RetrofitClient.apiServiceV2 }

 // ===== Security =====
    single { CryptoManager() }
    single { SecurityVerifier(androidContext()) }

 // ===== Preferences Manager =====
 // اتصال UserPreferencesManager به اینترفیس مرزی UserPreferencesStore برای استفاده در فیچرهای مستقل.
    single { UserPreferencesManager(androidContext(), get()) } bind UserPreferencesStore::class
 // بازگردانی سینگلتون UserPreferencesManager به عنوان ChatPreferencesStore.
    single<ChatPreferencesStore> { get<UserPreferencesManager>() }
 // اتصال UserPreferencesManager به TokenStore (core:network) برای استفاده در لایه‌های دیگر.
    single<TokenStore> { get<UserPreferencesManager>() }
 // اتصال UserPreferencesManager به UserSettingsStore (core:domain) جهت تفکیک وابستگی.
    single<UserSettingsStore> { get<UserPreferencesManager>() }

 // ===== دیتابیس محلی =====
    single { AppDatabase.getDatabase(androidContext()) }

 // ===== Startup =====
    single<StartupSessionRepository> { StartupSessionRepositoryImpl(get()) }

 // ===== ViewModelهایی که خودشان هنوز در :app هستند =====
    viewModel { StartupViewModel(androidApplication(), get(), get(), get(), get(), get()) }
}
