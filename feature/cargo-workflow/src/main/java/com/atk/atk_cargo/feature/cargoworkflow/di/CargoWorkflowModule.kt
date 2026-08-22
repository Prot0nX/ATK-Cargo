package com.atk.atk_cargo.feature.cargoworkflow.di

import com.atk.atk_cargo.feature.cargo_counter.presentation.CargoCounterViewModel
import com.atk.atk_cargo.feature.cargo_entry.presentation.InitialInfoViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// وابستگی‌های فیچر ثبت/شمارش حواله؛ قبلاً در appModule متمرکز بود (DEEP_CODE_AUDIT.md فاز۳ #۳۳)
val cargoWorkflowModule = module {
    viewModel { InitialInfoViewModel(get()) }
    viewModel { CargoCounterViewModel(get()) }
}
