package com.atk.atk_cargo.feature.home.di

import com.atk.atk_cargo.feature.home.presentation.ProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// وابستگی‌های فیچر خانه/پروفایل؛ قبلاً در appModule متمرکز بود (DEEP_CODE_AUDIT.md فاز۳ #۳۳)
val homeModule = module {
    viewModel { ProfileViewModel(get()) }
}
