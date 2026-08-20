package com.atk.atk_cargo.di

// Koin 4.x: ViewModel DSL از org.koin.androidx.viewmodel.dsl به یک ماژول
// چندسکویی (Multiplatform) در org.koin.core.module.dsl منتقل شد — Phase4 #36.
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.api.TokenStore
import com.atk.atk_cargo.api.UpdateManager
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.core.startup.StartupViewModel
import com.atk.atk_cargo.data.db.AppDatabase
import com.atk.atk_cargo.data.repository.ReportsRepository
import com.atk.atk_cargo.domain.repository.QuotaRepository
import com.atk.atk_cargo.domain.session.UserPreferencesStore
import com.atk.atk_cargo.domain.session.UserSettingsStore
import com.atk.atk_cargo.feature.admin.presentation.UserManagementViewModel
import com.atk.atk_cargo.feature.auth.data.AuthRepository
import com.atk.atk_cargo.feature.auth.data.AuthRepositoryImpl
import com.atk.atk_cargo.feature.auth.domain.LoginUseCase
import com.atk.atk_cargo.feature.auth.domain.LogoutUseCase
import com.atk.atk_cargo.feature.auth.viewmodel.AuthViewModel
import com.atk.atk_cargo.feature.cargo_counter.presentation.CargoCounterViewModel
import com.atk.atk_cargo.feature.cargo_entry.presentation.InitialInfoViewModel
import com.atk.atk_cargo.feature.chat.data.ChatPreferencesStore
import com.atk.atk_cargo.feature.chat.data.ChatRepository
import com.atk.atk_cargo.feature.home.presentation.ProfileViewModel
import com.atk.atk_cargo.security.CryptoManager
import com.atk.atk_cargo.security.SecurityVerifier
import com.atk.atk_cargo.ui.viewmodel.CargoViewModel
import com.atk.atk_cargo.ui.viewmodel.ReportsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    // ===== API Service =====
    // v1 (protected_proxy.php) کاملاً حذف شده — Router v2 تنها API stack
    // کلاینت است (DEEP_CODE_AUDIT.md #Phase3.1/3.2).
    single { RetrofitClient.apiServiceV2 }

    // ===== Security =====
    single { CryptoManager() }
    single { SecurityVerifier(androidContext()) }

    // ===== Preferences Manager =====
    // featureها (auth، admin، ...) نمی‌توانند به app وابسته شوند (app به
    // آن‌ها وابسته است، نه برعکس)، پس به‌جای UserPreferencesManager مستقیم،
    // اینترفیس مرزی UserPreferencesStore (در core:domain) را می‌خواهند؛ bind
    // این پیاده‌سازی را زیر آن نوع هم در دسترس get()/koinInject() می‌گذارد.
    single { UserPreferencesManager(androidContext(), get()) } bind UserPreferencesStore::class
    // یک single جدا (نه bind زنجیره‌ای — Koin اجازه نمی‌دهد دو bind پشت‌سرهم
    // روی انواع نامرتبط زده شود): همان singleton بالا را با get() برمی‌گرداند.
    single<ChatPreferencesStore> { get<UserPreferencesManager>() }
    // مشابه بالا برای TokenStore (core:network) — validateServerSession در
    // چند feature (cargo_entry، cargo_counter) به این اینترفیس نیاز دارد، نه
    // به کلاس concrete app-only (Phase4 #29 پیشنیاز).
    single<TokenStore> { get<UserPreferencesManager>() }
    // مشابه بالا برای UserSettingsStore (core:domain) — تنظیمات تم/اعلان که
    // home به آن‌ها نیاز دارد، نه به کلاس concrete app-only.
    single<UserSettingsStore> { get<UserPreferencesManager>() }

    // ===== دیتابیس محلی و مخازن =====
    single { AppDatabase.getDatabase(androidContext()) }
    single { ChatRepository(get<AppDatabase>().chatDao(), get(), get()) }
    single { ReportsRepository(get()) } bind QuotaRepository::class
    single<AuthRepository> {
        AuthRepositoryImpl(
            context = androidContext(),
            apiServiceV2 = get(),
            userPreferencesManager = get()
        )
    }

    // ===== Use Cases =====
    single { LoginUseCase(get()) }
    single { LogoutUseCase(get(), get()) }

    // ===== ViewModels =====
    viewModel { ReportsViewModel(get(), androidApplication()) }
    viewModel { CargoViewModel(get(), get()) }
    viewModel { AuthViewModel(loginUseCase = get(), context = androidContext()) }
    single { UpdateManager(androidContext()) }
    viewModel { StartupViewModel(androidApplication(), get(), get(), get(), get()) }
    viewModel { InitialInfoViewModel(get()) }
    viewModel { CargoCounterViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
    viewModel { UserManagementViewModel(get()) }
}

