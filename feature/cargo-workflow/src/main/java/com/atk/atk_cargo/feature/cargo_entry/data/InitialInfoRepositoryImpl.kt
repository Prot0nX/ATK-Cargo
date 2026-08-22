package com.atk.atk_cargo.feature.cargo_entry.data

import com.atk.atk_cargo.api.ApiServiceV2
import com.atk.atk_cargo.data.model.CheckExistenceRequest
import com.atk.atk_cargo.data.model.InitialInfo
import com.atk.atk_cargo.feature.cargo_entry.presentation.ExistenceCheckStatus

class InitialInfoRepositoryImpl(
    private val apiServiceV2: ApiServiceV2
) : InitialInfoRepository {

    override suspend fun checkExistence(request: CheckExistenceRequest): ExistenceCheckStatus? {
        val response = apiServiceV2.checkExistence(request)
        return when (response.body()?.status) {
            "not_exists" -> ExistenceCheckStatus.NOT_EXISTS
            "exists" -> ExistenceCheckStatus.EXISTS
            "partial_match" -> ExistenceCheckStatus.PARTIAL_MATCH
            else -> null
        }
    }

    override suspend fun submitInitialInfo(info: InitialInfo): Boolean {
        val response = apiServiceV2.saveInitialInfo(info)
        return response.isSuccessful
    }
}
