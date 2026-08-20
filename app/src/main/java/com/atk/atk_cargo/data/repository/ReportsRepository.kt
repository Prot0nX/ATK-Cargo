package com.atk.atk_cargo.data.repository

import android.util.Log
import com.atk.atk_cargo.api.ApiServiceV2
import com.atk.atk_cargo.api.ApiV2Routes
import com.atk.atk_cargo.data.model.ActiveShipInfo
import com.atk.atk_cargo.data.model.CargoInfo
import com.atk.atk_cargo.data.model.CargoInfoResponse
import com.atk.atk_cargo.data.model.ComprehensiveAnalysisResponse
import com.atk.atk_cargo.data.model.FilteredSummary
import com.atk.atk_cargo.data.model.Quota
import com.atk.atk_cargo.data.model.QuotaDetails
import com.atk.atk_cargo.data.model.QuotaItem
import com.atk.atk_cargo.data.model.QuotaStatusResponse
import com.atk.atk_cargo.data.model.RealTimeDataResponse
import com.atk.atk_cargo.data.model.SaveOrUpdateResponse
import com.atk.atk_cargo.data.model.Ship
import com.atk.atk_cargo.data.model.ShipsData
import com.atk.atk_cargo.data.model.Warehouse
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * برای پاسخ‌های ناموفق HTTP که کد وضعیت‌شان معنادار است (مثلاً ۴۰۴ برای
 * «یافت نشد»)؛ فراخوان‌کننده به‌جای تطبیق رشته‌ی فارسی پیام خطا می‌تواند
 * مستقیماً statusCode را چک کند.
 */
class HttpStatusException(val statusCode: Int, message: String) : Exception(message)

class ReportsRepository(
    private val apiServiceV2: ApiServiceV2 = com.atk.atk_cargo.api.RetrofitClient.apiServiceV2
) {
    suspend fun getCargoInfo(
        quotaNumber: String,
        shippingCompany: String,
        warehouse: String,
        cargoType: String
    ): CargoInfoResponse = withContext(Dispatchers.IO) {
        try {
            val response = apiServiceV2.getCargoInfo(
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getShipsList(): ShipsData = withContext(Dispatchers.IO) {
        val response = apiServiceV2.getShipsList()
        if (response.isSuccessful) {
            response.body()?.data ?: throw Exception("پاسخ سرور خالی است")
        } else {
            throw Exception("خطا در دریافت لیست کشتی‌ها: کد ${response.code()}")
        }
    }

    // forceRefresh=true وقتی لازم است که این متد بلافاصله بعد از یک نوشتن
    // موفق (toggleQuotaStatus/editQuota/deleteQuota/...) صدا زده می‌شود؛
    // بدون آن، کش دیسک OkHttp تا max-age سرور (۶ ثانیه) پاسخ قدیمی را بدون
    // حتی یک درخواست شبکه برمی‌گرداند و UI تغییر را نشان نمی‌دهد.
    suspend fun getShipDetails(shipName: String, forceRefresh: Boolean = false): Ship = withContext(Dispatchers.IO) {
        val response = apiServiceV2.getShipDetails(
            route = ApiV2Routes.shipDetails(shipName),
            cacheControl = if (forceRefresh) "no-cache" else null
        )
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Ship details not found")
        } else {
            throw HttpStatusException(
                response.code(),
                "Failed to fetch ship details: ${response.errorBody()?.string()}"
            )
        }
    }

    suspend fun getWarehouseDetails(shipName: String, warehouseName: String): Warehouse =
        withContext(Dispatchers.IO) {
            try {
                val response = apiServiceV2.getWarehouseDetails(
                    route = ApiV2Routes.warehouseDetails(shipName, warehouseName)
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
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                throw Exception("Error fetching warehouse details: ${e.message}")
            }
        }

    suspend fun getQuotaDetails(quotaNumber: String): QuotaDetails = withContext(Dispatchers.IO) {
        try {
            val response = apiServiceV2.getQuotaDetails(route = ApiV2Routes.quotaDetails(quotaNumber))
            if (response.isSuccessful) {
                val quotaDetails = response.body()
                quotaDetails ?: throw Exception("Quota details not found")
            } else {
                throw Exception("Failed to fetch quota details: ${response.errorBody()?.string()}")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Error fetching quota details: ${e.message}")
        }
    }

    suspend fun getShipQuotas(shipName: String, forceRefresh: Boolean = false): List<Quota> = withContext(Dispatchers.IO) {
        try {
            val response = apiServiceV2.getShipQuotas(
                route = ApiV2Routes.shipQuotas(shipName),
                cacheControl = if (forceRefresh) "no-cache" else null
            )
            if (response.isSuccessful) {
                val quotas = response.body() ?: throw Exception("Body is null")
                quotas
            } else {
                throw Exception("Server error: ${response.code()}")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Error fetching ship quotas: ${e.message}")
        }
    }

    suspend fun getFilteredQuotas(shipName: String, startDateTime: String, endDateTime: String): List<Quota> = withContext(Dispatchers.IO) {
        try {
            val response = apiServiceV2.getFilteredQuotas(
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Error fetching filtered quotas: ${e.message}")
        }
    }

    // منتقل‌شده از QuotaManagementDialog.kt که مستقیماً RetrofitClient.apiServiceV2
    // را از داخل LaunchedEffect صدا می‌زد (DEEP_CODE_REVIEW.md Top20 #5).
    suspend fun getGroupedQuotas(shipName: String): Map<String, Map<String, List<QuotaItem>>> =
        withContext(Dispatchers.IO) {
            val response = apiServiceV2.getGroupedQuotas(shipName = shipName)
            if (response.isSuccessful) {
                response.body() ?: emptyMap()
            } else {
                throw Exception("خطا در دریافت داده‌ها: ${response.code()}")
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
            val response = apiServiceV2.checkQuotaStatus(
                route = ApiV2Routes.quotaStatus(quotaNumber),
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
        } catch (e: CancellationException) {
            throw e
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
            val response = apiServiceV2.editQuota(
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Error editing quota: ${e.message}")
        }
    }

    suspend fun updateQuotaPercentage(id: Int, percentage: Double): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiServiceV2.updateQuotaPercentage(
                    route = ApiV2Routes.quotaPercentage(id),
                    percentage = percentage
                )
                if (response.isSuccessful) {
                    response.body()?.success == true
                } else {
                    throw Exception("Server error: ${response.code()}")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                throw Exception("Error updating quota percentage: ${e.message}")
            }
        }
    }

    suspend fun toggleQuotaStatus(id: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = apiServiceV2.toggleQuotaStatus(route = ApiV2Routes.quotaToggleStatus(id))
            if (response.isSuccessful) {
                response.body()?.success ?: false
            } else {
                throw Exception("Server error: ${response.code()}")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Error toggling quota status: ${e.message}")
        }
    }

    suspend fun updateQuotaPercentageRestriction(id: Int, isEnabled: Int): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val response = apiServiceV2.updateQuotaPercentageRestriction(
                    route = ApiV2Routes.quotaPercentageRestriction(id),
                    isEnabled = isEnabled
                )
                if (response.isSuccessful) {
                    response.body()?.success ?: false
                } else {
                    throw Exception("Server error: ${response.code()}")
                }
            } catch (e: CancellationException) {
                throw e
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
            val response = apiServiceV2.updateTemporaryTonnage(
                route = ApiV2Routes.quotaTemporaryTonnage(quotaNumber),
                enabled = enabled,
                tonnage = tonnage
            )
            if (response.isSuccessful) {
                response.body()?.success ?: false
            } else {
                throw Exception("Server error: ${response.code()}")
            }
        } catch (e: CancellationException) {
            throw e
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
            val response = apiServiceV2.deleteQuota(
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
        } catch (e: CancellationException) {
            throw e
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
            val response = apiServiceV2.getFilteredSummary(
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Error fetching filtered summary: ${e.message}")
        }
    }

    suspend fun getActiveShips(): List<ActiveShipInfo> = withContext(Dispatchers.IO) {
        try {
            val response = apiServiceV2.getActiveShips()
            if (response.isSuccessful) {
                response.body() ?: throw Exception("داده‌های دریافتی خالی است")
            } else {
                throw Exception("خطا در دریافت اطلاعات کشتی‌های فعال")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw Exception("خطا در ارتباط با سرور: ${e.message}")
        }
    }

    // بدون try/catch عمومی (مطابق الگوی getShipDetails)؛ در غیر این صورت
    // HttpStatusException زیر دوباره در یک Exception ساده بسته‌بندی می‌شد و
    // کلاینت نمی‌توانست بین ۴۰۱ (نشست نامعتبر)، ۴۲۹ (rate limit) و خطای شبکه
    // تشخیص دهد.
    suspend fun getRealTimeLoadingData(shiftOffset: Int = 0): RealTimeDataResponse = withContext(Dispatchers.IO) {
        val response = apiServiceV2.getRealTimeLoadingData(shiftOffset = shiftOffset)
        if (response.isSuccessful) {
            response.body() ?: throw Exception("داده‌های دریافتی خالی است")
        } else {
            throw HttpStatusException(
                response.code(),
                "خطا در دریافت اطلاعات بارگیری لحظه‌ای: ${response.errorBody()?.string()}"
            )
        }
    }

    suspend fun getCargoInfoByReceiptNumber(receiptNumber: String): CargoInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiServiceV2.getCargoInfoByReceiptNumber(receiptNumber = receiptNumber)

                if (response.isSuccessful) {
                    val body = response.body()
                    val result = body?.cargoInfo
                    result
                } else {
                    null
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getCargoInfoByTrackingNumber(trackingNumber: String): List<CargoInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiServiceV2.getCargoInfoByTrackingNumber(trackingNumber = trackingNumber)

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
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                return@withContext emptyList()
            }
        }
    }

    suspend fun updateCargoInfo(cargoInfo: CargoInfo): Result<SaveOrUpdateResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiServiceV2.updateCargoInfo(cargoInfo)

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
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e("ReportsRepository", "خطا در parse کردن JSON: ${e.message}")
                    }

                    Result.failure(Exception("خطا در بروزرسانی: $errorBody"))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // A-3 (گزارش تحلیل جامع عملیات): بدون try/catch عمومی و با HttpStatusException
    // (مطابق الگوی getRealTimeLoadingData)؛ در غیر این صورت کلاینت نمی‌توانست
    // ۴۰۱ (نشست نامعتبر) و ۴۰۳ (نبود مجوز view_reports) را از خطای شبکه
    // تشخیص دهد، و بدنه خام JSON خطای سرور مستقیم در ErrorStateCard به کاربر
    // نمایش داده می‌شد.
    suspend fun getComprehensiveAnalysis(offset: Int = 0): ComprehensiveAnalysisResponse =
        withContext(Dispatchers.IO) {
            val response = apiServiceV2.getComprehensiveAnalysis(offset = offset)
            if (response.isSuccessful) {
                response.body() ?: throw Exception("داده‌های دریافتی خالی است")
            } else {
                throw HttpStatusException(
                    response.code(),
                    "خطا در دریافت اطلاعات تحلیلی (کد ${response.code()})"
                )
            }
        }

    // A-5: عمداً بدون throw — این فقط یک لاگ ممیزی سمت سرور است؛ اگر شکست
    // بخورد (شبکه قطع، سرور down) نباید جلوی اشتراک‌گذاری واقعی کاربر
    // (که با Intent.ACTION_SEND و کاملاً سمت کلاینت انجام می‌شود) را بگیرد.
    suspend fun logAnalyticsExport(scope: String, groupCount: Int) {
        withContext(Dispatchers.IO) {
            try {
                apiServiceV2.logAnalyticsExport(scope = scope, groupCount = groupCount)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // بی‌اهمیت برای UX؛ فقط ممیزی سمت سرور است.
            }
        }
    }
}
