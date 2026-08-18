package com.atk.atk_cargo.feature.cargo.domain.usecase

import com.atk.atk_cargo.api.ApiServiceV2
import com.atk.atk_cargo.api.QuotaExistenceMultipleResponse
import retrofit2.Response

class CheckQuotaUseCase(
    private val apiServiceV2: ApiServiceV2 = com.atk.atk_cargo.api.RetrofitClient.apiServiceV2
) {
    suspend fun checkQuotaExistenceCargo(
        quotaNumber: String,
        shipName: String
    ): Response<QuotaExistenceMultipleResponse> {
        return apiServiceV2.checkQuotaExistenceCargo(
            quotaNumber = quotaNumber,
            shipName = shipName
        )
    }
}
