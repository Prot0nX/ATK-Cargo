package com.atk.atk_cargo.feature.cargoworkflow.di

import com.atk.atk_cargo.feature.cargo_counter.presentation.CargoCounterViewModel
import com.atk.atk_cargo.feature.cargo_entry.data.InitialInfoRepository
import com.atk.atk_cargo.feature.cargo_entry.data.InitialInfoRepositoryImpl
import com.atk.atk_cargo.feature.cargo_entry.presentation.InitialInfoViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// وابستگی‌های فیچر ثبت/شمارش حواله؛ قبلاً در appModule متمرکز بود
val cargoWorkflowModule = module {
    single<InitialInfoRepository> { InitialInfoRepositoryImpl(get()) }
    viewModel { InitialInfoViewModel(get()) }
    viewModel { CargoCounterViewModel(get()) }
}
