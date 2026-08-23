package com.atk.atk_cargo.feature.home.di

import com.atk.atk_cargo.feature.home.data.HomeRepository
import com.atk.atk_cargo.feature.home.data.HomeRepositoryImpl
import com.atk.atk_cargo.feature.home.presentation.ProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// وابستگی‌های فیچر خانه/پروفایل؛ قبلاً در appModule متمرکز بود
val homeModule = module {
    single<HomeRepository> { HomeRepositoryImpl(get()) }
    viewModel { ProfileViewModel(get()) }
}
