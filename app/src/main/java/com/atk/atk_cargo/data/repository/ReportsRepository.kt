package com.atk.atk_cargo.data.repository

import android.util.Log
import com.atk.atk_cargo.api.ApiService
import com.atk.atk_cargo.data.model.CargoInfo
import com.atk.atk_cargo.data.model.CargoInfoResponse
import com.atk.atk_cargo.data.model.ComprehensiveAnalysisResponse
import com.atk.atk_cargo.data.model.FilteredSummary
import com.atk.atk_cargo.data.model.InitialInfo
import com.atk.atk_cargo.data.model.Quota
import com.atk.atk_cargo.data.model.QuotaDetails
import com.atk.atk_cargo.data.model.QuotaStatusResponse
import com.atk.atk_cargo.data.model.RealTimeDataResponse
import com.atk.atk_cargo.data.model.SaveOrUpdateResponse
import com.atk.atk_cargo.data.model.Ship
import com.atk.atk_cargo.data.model.ShipsData
import com.atk.atk_cargo.data.model.Warehouse
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReportsRepository(private val apiService: ApiService) {
    suspend fun getCargoInfo(
        quotaNumber: String,
        shippingCompany: String,
        warehouse: String,
        cargoType: String
    ): CargoInfoResponse = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCargoInfo(
                quotaNumber = quotaNumber,
                shippingCompany = shippingCompany,
                warehouse = warehouse,
                cargoType = cargoType
            )

            if (response.isSuccessful) {
                response.body() ?: throw Exception("Empty response body")
            } else {
                val errorBody = response.errorBody()?.string()
                throw Exception("Failed to fetch cargo info. Response code: ${response.code()}, Error body: $errorBody")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    // فقط اطلاعات اولیه را بدون لیست حواله‌ها دریافت می‌کند
    suspend fun getInitialInfo(
        quotaNumber: String,
        shippingCompany: String,
        warehouse: String,
        cargoType: String
    ): InitialInfo? = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCargoInfo(
                quotaNumber = quotaNumber,
                shippingCompany = shippingCompany,
                warehouse = warehouse,
                cargoType = cargoType
            )

            if (response.isSuccessful) {
                response.body()?.initialInfo
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getShipsList(): ShipsData = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getShipsList()
            if (response.isSuccessful) {
                val shipsData = response.body()?.data ?: ShipsData(emptyList(), emptyList())
                shipsData
            } else {
                ShipsData(emptyList(), emptyList())
            }
        } catch (e: Exception) {
            ShipsData(emptyList(), emptyList())
        }
    }

    suspend fun getShipDetails(shipName: String): Ship = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getShipDetails(shipName = shipName)
            if (response.isSuccessful) {
                response.body() ?: throw Exception("Ship details not found")
            } else {
                throw Exception("Failed to fetch ship details: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getWarehouseDetails(shipName: String, warehouseName: String): Warehouse =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getWarehouseDetails(
                    shipName = shipName,
                    warehouseName = warehouseName
                )
                if (response.isSuccessful) {
                    val warehouseDetails = response.body() ?: throw Exception("Body is null")

                    warehouseDetails
                } else {
                    throw Exception(
                        "Failed to fetch warehouse details: ${
                            response.errorBody()?.string()
                        }"
                    )
                }
            } catch (e: Exception) {
                throw Exception("Error fetching warehouse details: ${e.message}")
            }
        }

    suspend fun getQuotaDetails(quotaNumber: String): QuotaDetails = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getQuotaDetails(quotaNumber = quotaNumber)
            if (response.isSuccessful) {
                val quotaDetails = response.body()
                quotaDetails ?: throw Exception("Quota details not found")
            } else {
                throw Exception("Failed to fetch quota details: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            throw Exception("Error fetching quota details: ${e.message}")
        }
    }

    suspend fun getShipQuotas(shipName: String): List<Quota> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getShipQuotas(shipName = shipName)
            if (response.isSuccessful) {
                val quotas = response.body() ?: throw Exception("Body is null")
                quotas
            } else {
                throw Exception("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            throw Exception("Error fetching ship quotas: ${e.message}")
        }
    }

    suspend fun getFilteredQuotas(shipName: String, startDateTime: String, endDateTime: String): List<Quota> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFilteredQuotas(
                shipName = shipName,
                startDateTime = startDateTime,
                endDateTime = endDateTime
            )
            if (response.isSuccessful) {
                val quotas = response.body() ?: throw Exception("Body is null")
                quotas
            } else {
                val errorBody = response.errorBody()?.string()
                throw Exception("Server error: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            throw Exception("Error fetching filtered quotas: ${e.message}")
        }
    }

    suspend fun checkQuotaStatus(
        quotaNumber: String,
        shipName: String,
        cargoType: String,
        shippingCompany: String,
        warehouse: String
    ): QuotaStatusResponse = withContext(Dispatchers.IO) {
        try {
            val response = apiService.checkQuotaStatus(
                quotaNumber = quotaNumber,
                shipName = shipName,
                cargoType = cargoType,
                shippingCompany = shippingCompany,
                warehouse = warehouse
            )

            if (response.isSuccessful) {
                val result = response.body()
                result
                    ?: QuotaStatusResponse(
                        isActive = false,
                        status = false,
                        message = "خطا در دریافت وضعیت کوتاژ: پاسخ خالی از سرور",
                        details = null
                    )
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java)?.error
                        ?: "خطای سرور: ${response.code()}"
                } catch (_: Exception) {
                    "خطای سرور: ${response.code()}"
                }

                QuotaStatusResponse(
                    isActive = false,
                    status = false,
                    message = errorMessage,
                    details = null
                )
            }
        } catch (e: Exception) {
            QuotaStatusResponse(
                isActive = false,
                status = false,
                message = "خطا در بررسی وضعیت کوتاژ: ${e.message ?: "خطای ناشناخته"}",
                details = null
            )
        }
    }

    data class ErrorResponse(
        val error: String? = null,
        val message: String? = null
    )

    suspend fun editQuota(
        id: Int,
        oldQuotaNumber: String,
        newQuotaNumber: String,
        shipName: String,
        shippingCompany: String,
        warehouse: String,
        cargoType: String,
        totalTonnage: Float
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = apiService.editQuota(
                id = id,
                oldQuotaNumber = oldQuotaNumber,
                newQuotaNumber = newQuotaNumber,
                shipName = shipName,
                shippingCompany = shippingCompany,
                warehouse = warehouse,
                cargoType = cargoType,
                totalTonnage = totalTonnage
            )
            if (response.isSuccessful) {
                response.body()?.success ?: false
            } else {
                throw Exception("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            throw Exception("Error editing quota: ${e.message}")
        }
    }

    suspend fun updateQuotaPercentage(quotaNumber: String, percentage: Double): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val isEnabled = if (percentage > 0.0) 1 else 0
                val response = apiService.updateQuotaPercentage(
                    quotaNumber = quotaNumber,
                    percentage = percentage,
                    isEnabled = isEnabled
                )
                if (response.isSuccessful) {
                    response.body()?.success == true
                } else {
                    throw Exception("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                throw Exception("Error updating quota percentage: ${e.message}")
            }
        }
    }

    suspend fun toggleQuotaStatus(id: Int, quotaNumber: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = apiService.toggleQuotaStatus(id = id, quotaNumber = quotaNumber)
            if (response.isSuccessful) {
                response.body()?.success ?: false
            } else {
                throw Exception("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            throw Exception("Error toggling quota status: ${e.message}")
        }
    }

    suspend fun updateQuotaPercentageRestriction(quotaNumber: String, isEnabled: Int): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateQuotaPercentageRestriction(
                    quotaNumber = quotaNumber,
                    isEnabled = isEnabled
                )
                if (response.isSuccessful) {
                    response.body()?.success ?: false
                } else {
                    throw Exception("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                throw Exception("Error updating quota percentage restriction: ${e.message}")
            }
        }

    suspend fun updateTemporaryTonnage(
        quotaNumber: String,
        enabled: Int,
        tonnage: Double? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateTemporaryTonnage(
                quotaNumber = quotaNumber,
                enabled = enabled,
                tonnage = tonnage
            )
            if (response.isSuccessful) {
                response.body()?.success ?: false
            } else {
                throw Exception("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            throw Exception("Error updating temporary tonnage: ${e.message}")
        }
    }

    suspend fun deleteQuota(
        quotaNumber: String,
        shipName: String,
        warehouse: String,
        shippingCompany: String,
        cargoType: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteQuota(
                quotaNumber = quotaNumber,
                shipName = shipName,
                warehouse = warehouse,
                shippingCompany = shippingCompany,
                cargoType = cargoType
            )
            if (response.isSuccessful) {
                response.body()?.success ?: false
            } else {
                throw Exception("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            throw Exception("Error deleting quota: ${e.message}")
        }
    }

    suspend fun getFilteredSummary(
        shipName: String,
        warehouseName: String,
        selectedQuota: String,
        startDateTime: String,
        endDateTime: String
    ): FilteredSummary = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFilteredSummary(
                shipName = shipName,
                warehouseName = warehouseName,
                selectedQuota = selectedQuota,
                startDateTime = startDateTime,
                endDateTime = endDateTime
            )

            if (response.isSuccessful) {
                val responseBody = response.body()

                if (responseBody != null) {
                    val (startDate, startTime) = startDateTime.split(" ", limit = 2).let {
                        if (it.size == 2) it[0] to it[1] else it[0] to ""
                    }
                    val (endDate, endTime) = endDateTime.split(" ", limit = 2).let {
                        if (it.size == 2) it[0] to it[1] else it[0] to ""
                    }

                    return@withContext FilteredSummary(
                        totalNetWeight = responseBody.totalNetWeight,
                        voucherCount = responseBody.voucherCount,
                        voucherDetails = responseBody.voucherDetails ?: emptyList(),
                        quotaNumber = selectedQuota,
                        shipName = shipName,
                        startDate = startDate,
                        startTime = startTime,
                        endDate = endDate,
                        endTime = endTime
                    )
                } else {
                    throw Exception("Response body is null")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                throw Exception("Server error: ${response.code()}, Error body: $errorBody")
            }
        } catch (e: Exception) {
            throw Exception("Error fetching filtered summary: ${e.message}")
        }
    }

    suspend fun getRealTimeLoadingData(shiftOffset: Int = 0): RealTimeDataResponse = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRealTimeLoadingData(shiftOffset = shiftOffset)
            if (response.isSuccessful) {
                response.body() ?: throw Exception("Body is null")
            } else {
                throw Exception("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            throw Exception("Error fetching real-time loading data: ${e.message}")
        }
    }

    suspend fun getCargoInfoByReceiptNumber(receiptNumber: String): CargoInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCargoInfoByReceiptNumber(receiptNumber)

                if (response.isSuccessful) {
                    val body = response.body()
                    val result = body?.cargoInfo
                    result
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getCargoInfoByTrackingNumber(trackingNumber: String): List<CargoInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCargoInfoByTrackingNumber(trackingNumber)

                if (response.isSuccessful) {
                    val responseBody = response.body()

                    if (responseBody != null) {
                        if (!responseBody.error.isNullOrEmpty()) {
                            return@withContext emptyList()
                        }

                        val cargoInfoList = mutableListOf<CargoInfo>()
                        val searchResults = responseBody.cargoInfoList ?: emptyList()

                        searchResults.forEach { cargoInfoSearch ->
                            cargoInfoSearch.cargoInfo?.let { cargoInfo ->
                                cargoInfoList.add(cargoInfo)
                            } ?: Log.w("CargoSearch", "⚠️ CargoInfoSearch item has null cargoInfo")
                        }

                        return@withContext cargoInfoList
                    } else {
                        return@withContext emptyList()
                    }
                } else {
                    return@withContext emptyList()
                }
            } catch (e: Exception) {
                return@withContext emptyList()
            }
        }
    }

    suspend fun updateCargoInfo(cargoInfo: CargoInfo): Result<SaveOrUpdateResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateCargoInfo(cargoInfo)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Result.success(body)
                    } else {
                        Result.failure(Exception("پاسخ سرور خالی است"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()

                    // تلاش برای parse کردن JSON خطا
                    try {
                        if (!errorBody.isNullOrEmpty() && errorBody.trim().startsWith("{")) {
                            val errorJson = Gson().fromJson(errorBody, JsonObject::class.java)
                            if (errorJson.has("message")) {
                                Log.e("ReportsRepository", "💬 پیام خطا: ${errorJson.get("message").asString}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ReportsRepository", "خطا در parse کردن JSON: ${e.message}")
                    }

                    Result.failure(Exception("خطا در بروزرسانی: $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getComprehensiveAnalysis(offset: Int = 0): ComprehensiveAnalysisResponse {
        val response = apiService.getComprehensiveAnalysis(offset = offset)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string()}")
        }
    }
}
