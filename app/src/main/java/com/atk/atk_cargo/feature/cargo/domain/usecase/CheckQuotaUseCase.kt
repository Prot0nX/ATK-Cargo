package com.atk.atk_cargo.feature.cargo.domain.usecase

import com.atk.atk_cargo.api.ApiService
import com.atk.atk_cargo.api.QuotaExistenceMultipleResponse
import retrofit2.Response

class CheckQuotaUseCase(
    private val apiService: ApiService
) {
    suspend fun checkQuotaExistenceCargo(
        quotaNumber: String,
        shipName: String
    ): Response<QuotaExistenceMultipleResponse> {
        return apiService.checkQuotaExistenceCargo(
            quotaNumber = quotaNumber,
            shipName = shipName
        )
    }
}
