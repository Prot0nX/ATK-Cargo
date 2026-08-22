package com.atk.atk_cargo.feature.admin.di

import com.atk.atk_cargo.feature.admin.presentation.UserManagementViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// وابستگی‌های فیچر مدیریت کاربران؛ قبلاً در appModule متمرکز بود (DEEP_CODE_AUDIT.md فاز۳ #۳۳)
val adminModule = module {
    viewModel { UserManagementViewModel(get()) }
}
