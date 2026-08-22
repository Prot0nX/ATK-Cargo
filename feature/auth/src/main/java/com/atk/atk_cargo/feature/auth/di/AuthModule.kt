package com.atk.atk_cargo.feature.auth.di

import com.atk.atk_cargo.feature.auth.data.AuthRepository
import com.atk.atk_cargo.feature.auth.data.AuthRepositoryImpl
import com.atk.atk_cargo.feature.auth.domain.LoginUseCase
import com.atk.atk_cargo.feature.auth.domain.LogoutUseCase
import com.atk.atk_cargo.feature.auth.viewmodel.AuthViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// وابستگی‌های فیچر ورود؛ قبلاً در appModule متمرکز بود (DEEP_CODE_AUDIT.md فاز۳ #۳۳)
val authModule = module {
    single<AuthRepository> {
        AuthRepositoryImpl(
            context = androidContext(),
            apiServiceV2 = get(),
            userPreferencesManager = get()
        )
    }
    single { LoginUseCase(get()) }
    single { LogoutUseCase(get(), get()) }
    viewModel { AuthViewModel(loginUseCase = get(), context = androidContext()) }
}
