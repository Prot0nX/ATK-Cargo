package com.atk.atk_cargo.feature.cargo.domain.usecase

import com.atk.atk_cargo.api.ApiService
import com.atk.atk_cargo.api.CargoInfo
import com.atk.atk_cargo.api.InitialInfo
import com.atk.atk_cargo.api.SaveOrUpdateResponse
import retrofit2.Response

class SubmitCargoUseCase(
    private val apiService: ApiService
) {
    fun prepareCargoInfoForSubmission(
        trackingNumber: String,
        numberOfPeople: String,
        username: String,
        userType: String,
        netWeight: String,
        scaleReceiptNumber: String,
        shortageWeight: String,
        excessWeight: String,
        initialInfo: InitialInfo,
        currentTime: String,
        currentDate: String
    ): CargoInfo {
        val isExit = netWeight.isNotBlank()
        val finalNumberOfPeople = numberOfPeople.ifEmpty { "1" }

        return CargoInfo(
            trackingNumber = trackingNumber,
            numberOfPeople = finalNumberOfPeople,
            username = username,
            userType = userType,
            entryTime = currentTime,
            netWeight = netWeight,
            scaleReceiptNumber = scaleReceiptNumber,
            shortageWeight = shortageWeight,
            excessWeight = excessWeight,
            exitTime = if (isExit) currentTime else null,
            exitDate = if (isExit) currentDate else null,
            status = if (isExit) "خروج" else "ورود",
            shipName = initialInfo.shipName,
            loadingWarehouse = initialInfo.loadingWarehouse,
            cargoType = initialInfo.cargoType,
            shippingCompany = initialInfo.shippingCompany,
            loadingQuotaNumber = initialInfo.loadingQuotaNumber.toString(),
            confirm = "",
            confirmation = "no"
        )
    }

    suspend fun executeSaveOrUpdate(cargoInfo: CargoInfo): Response<SaveOrUpdateResponse> {
        return apiService.saveOrUpdateCargoInfo(cargoInfo)
    }
}
