package com.atk.atk_cargo.feature.cargo.di

import com.atk.atk_cargo.feature.cargo.viewmodel.CargoViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// وابستگی‌های فیچر شمارش کوتاژ؛ قبلاً در appModule متمرکز بود
val cargoModule = module {
    viewModel { CargoViewModel(get(), get()) }
}
