package com.atk.atk_cargo.api

class AppContainer() {
    private val apiService: ApiService by lazy {
        RetrofitClient.apiService
    }

    val reportsRepository: ReportsRepository by lazy {
        ReportsRepository(apiService)
    }
}