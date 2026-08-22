package com.atk.atk_cargo.feature.cargo.di

import com.atk.atk_cargo.ui.viewmodel.CargoViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// وابستگی‌های فیچر شمارش کوتاژ؛ قبلاً در appModule متمرکز بود (DEEP_CODE_AUDIT.md فاز۳ #۳۳)
val cargoModule = module {
    viewModel { CargoViewModel(get(), get()) }
}
