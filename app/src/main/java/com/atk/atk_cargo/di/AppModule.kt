package com.atk.atk_cargo.di

import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.data.repository.ReportsRepository
import com.atk.atk_cargo.security.SecurityVerifier
import com.atk.atk_cargo.ui.viewmodel.CargoViewModel
import com.atk.atk_cargo.ui.viewmodel.ReportsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

import com.atk.atk_cargo.security.CryptoManager
import com.atk.atk_cargo.feature.auth.viewmodel.AuthViewModel

val appModule = module {
    // API Service
    single { RetrofitClient.apiService }
    
    // Repository
    single { ReportsRepository(get()) }
    
    // Security
    single { SecurityVerifier(androidContext()) }
    single { CryptoManager() }
    
    // Preferences Manager
    single { UserPreferencesManager(androidContext(), get()) }
    
    // ViewModels
    viewModel { ReportsViewModel(get(), androidApplication()) }
    viewModel { CargoViewModel(get(), get()) }
    viewModel { AuthViewModel(get(), get()) }
}
