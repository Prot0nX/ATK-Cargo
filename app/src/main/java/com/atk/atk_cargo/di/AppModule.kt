package com.atk.atk_cargo.di

import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.api.UpdateManager
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.core.startup.StartupViewModel
import com.atk.atk_cargo.data.db.AppDatabase
import com.atk.atk_cargo.data.repository.ChatRepository
import com.atk.atk_cargo.data.repository.ReportsRepository
import com.atk.atk_cargo.feature.auth.data.AuthRepository
import com.atk.atk_cargo.feature.auth.data.AuthRepositoryImpl
import com.atk.atk_cargo.feature.auth.domain.LoginUseCase
import com.atk.atk_cargo.feature.auth.domain.LogoutUseCase
import com.atk.atk_cargo.feature.auth.viewmodel.AuthViewModel
import com.atk.atk_cargo.security.CryptoManager
import com.atk.atk_cargo.security.SecurityVerifier
import com.atk.atk_cargo.ui.viewmodel.CargoViewModel
import com.atk.atk_cargo.ui.viewmodel.ReportsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // ===== API Service =====
    single { RetrofitClient.apiService }

    // ===== Security =====
    single { CryptoManager() }
    single { SecurityVerifier(androidContext()) }

    // ===== Preferences Manager =====
    single { UserPreferencesManager(androidContext(), get()) }

    // ===== دیتابیس محلی و مخازن =====
    single { AppDatabase.getDatabase(androidContext()) }
    single { ChatRepository(get<AppDatabase>().chatDao(), { get() }, get()) }
    single { ReportsRepository(get()) }
    single<AuthRepository> {
        AuthRepositoryImpl(
            context = androidContext(),
            apiService = get(),
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
}

