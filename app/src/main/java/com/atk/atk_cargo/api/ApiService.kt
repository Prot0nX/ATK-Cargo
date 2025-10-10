package com.atk.atk_cargo.api

import com.google.gson.JsonElement
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("saveInitialInfo.php")
    suspend fun saveInitialInfo(@Body initialInfo: InitialInfo): Response<Void>

    @POST("checkExistence.php")
    suspend fun checkExistence(@Body request: CheckExistenceRequest): Response<CheckExistenceResponse>

    @GET("check_scale_receipt.php")
    suspend fun checkScaleReceiptNumber(
        @Query("scaleReceiptNumber") scaleReceiptNumber: String
    ): Response<ScaleReceiptCheckResponse>

    @GET("protected_proxy.php")
    suspend fun checkQuotaExistenceCargo(
        @Query("target") target: String = "app_api.php",
        @Query("action") action: String = "checkQuotaExistenceCargo",
        @Query("quotaNumber") quotaNumber: String,
        @Query("shipName") shipName: String
    ): Response<QuotaExistenceMultipleResponse>

    @GET("getInitialInfo2.php")
    suspend fun getCargoInfo(
        @Query("quotaNumber") quotaNumber: String,
        @Query("shippingCompany") shippingCompany: String,
        @Query("warehouse") warehouse: String,
        @Query("cargoType") cargoType: String
    ): Response<CargoInfoResponse>

    @GET("getActiveShips.php")
    suspend fun getActiveShips(): Response<List<ActiveShipInfo>>

    @POST("saveOrUpdateCargoInfo.php")
    suspend fun saveOrUpdateCargoInfo(@Body cargoInfo: CargoInfo): Response<SaveOrUpdateResponse>

    @POST("updateCargoInfo.php")
    suspend fun updateCargoInfo(@Body cargoInfo: CargoInfo): Response<SaveOrUpdateResponse>

    @POST("deleteCargoInfo2.php")
    suspend fun deleteCargo(@Body cargoInfoRequest: CargoInfoRequest): Response<Void>

    @FormUrlEncoded
    @POST("check_password.php")
    suspend fun checkPassword(
        @Field("password") password: String,
        @Field("passwordType") passwordType: String
    ): Response<PasswordCheckResponse>

    @POST("check_Auth.php")
    suspend fun checkLogin(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @GET("protected_proxy.php")
    suspend fun getShipsList(
        @Query("target") target: String = "app_api.php",
        @Query("action") action: String = "getShipsList",
        @Query("startDateTime") startDateTime: String? = null,
        @Query("endDateTime") endDateTime: String? = null
    ): Response<ApiResponse2<ShipsData>>

    @GET("protected_proxy.php")
    suspend fun getShipDetails(
        @Query("target") target: String = "app_api.php",
        @Query("action") action: String = "getShipDetails",
        @Query("shipName") shipName: String,
        @Query("startDateTime") startDateTime: String? = null,
        @Query("endDateTime") endDateTime: String? = null
    ): Response<Ship>

    @GET("protected_proxy.php")
    suspend fun getWarehouseDetails(
        @Query("target") target: String = "app_api.php",
        @Query("action") action: String = "getWarehouseDetails",
        @Query("shipName") shipName: String,
        @Query("warehouseName") warehouseName: String,
        @Query("startDateTime") startDateTime: String? = null,
        @Query("endDateTime") endDateTime: String? = null
    ): Response<Warehouse>

    @GET("protected_proxy.php")
    suspend fun getQuotaDetails(
        @Query("target") target: String = "app_api.php",
        @Query("action") action: String = "getQuotaDetails",
        @Query("quotaNumber") quotaNumber: String
    ): Response<QuotaDetails>

    @GET("protected_proxy.php")
    suspend fun getShipQuotas(
        @Query("target") target: String = "app_api.php",
        @Query("action") action: String = "getQuotasList",
        @Query("shipName") shipName: String
    ): Response<List<Quota>>

    @GET("protected_proxy.php")
    suspend fun getFilteredQuotas(
        @Query("target") target: String = "app_api.php",
        @Query("action") action: String = "getFilteredQuotas",
        @Query("shipName") shipName: String,
        @Query("startDateTime") startDateTime: String,
        @Query("endDateTime") endDateTime: String
    ): Response<List<Quota>>

    @GET("protected_proxy.php")
    suspend fun getFilteredSummary(
        @Query("target") target: String = "app_api.php",
        @Query("action") action: String = "getFilteredSummary",
        @Query("shipName") shipName: String,
        @Query("warehouseName") warehouseName: String,
        @Query("selectedQuota") selectedQuota: String,
        @Query("startDateTime") startDateTime: String,
        @Query("endDateTime") endDateTime: String
    ): Response<FilteredSummaryResponse>

    @GET("protected_proxy.php")
    suspend fun editQuota(
        @Query("target") target: String = "app_api.php",
        @Query("action") action: String = "editQuota",
        @Query("oldQuotaNumber") oldQuotaNumber: String,
        @Query("newQuotaNumber") newQuotaNumber: String,
        @Query("shipName") shipName: String,
        @Query("shippingCompany") shippingCompany: String,
        @Query("warehouse") warehouse: String,
        @Query("cargoType") cargoType: String,
        @Query("totalTonnage") totalTonnage: Float
    ): Response<SuccessResponse>

    @GET("protected_proxy.php")
    suspend fun updateQuotaPercentage(
        @Query("target") target: String = "app_api.php",
        @Query("action") action: String = "updateQuotaPercentage",
        @Query("quotaNumber") quotaNumber: String,
        @Query("percentage") percentage: Double,
        @Query("isEnabled") isEnabled: Int
    ): Response<SuccessResponse>

    @GET("protected_proxy.php")
    suspend fun toggleQuotaStatus(
        @Query("target") target: String = "app_api.php",
        @Query("action") action: String = "toggleQuotaStatus",
        @Query("quotaNumber") quotaNumber: String
    ): Response<SuccessResponse>

    @GET("protected_proxy.php")
    suspend fun updateQuotaPercentageRestriction(
        @Query("target") target: String = "app_api.php",
        @Query("action") action: String = "updateQuotaPercentageRestriction",
        @Query("quotaNumber") quotaNumber: String,
        @Query("isEnabled") isEnabled: Int
    ): Response<SuccessResponse>

    @GET("protected_proxy.php")
    suspend fun deleteQuota(
        @Query("target") target: String = "app_api.php",
        @Query("action") action: String = "deleteQuota",
        @Query("quotaNumber") quotaNumber: String,
        @Query("shipName") shipName: String,
        @Query("warehouse") warehouse: String,
        @Query("shippingCompany") shippingCompany: String,
        @Query("cargoType") cargoType: String
    ): Response<SuccessResponse>

    @GET("protected_proxy.php")
    suspend fun checkQuotaStatus(
        @Query("target") target: String = "app_api.php",
        @Query("action") action: String = "checkQuotaStatus",
        @Query("quotaNumber") quotaNumber: String,
        @Query("shipName") shipName: String? = null,
        @Query("cargoType") cargoType: String? = null,
        @Query("shippingCompany") shippingCompany: String? = null
    ): Response<QuotaStatusResponse>

    @GET("realTimeLoadingData.php")
    suspend fun getRealTimeLoadingData(@Query("action") action: String = "getRealTimeData"): Response<RealTimeDataResponse>

    @GET("realTimeLoadingData.php")
    suspend fun getComprehensiveAnalysis(@Query("action") action: String = "getComprehensiveAnalysis"): Response<ResponseBody>

    @POST("check_session.php")
    suspend fun checkSession(@Body request: SessionCheckRequest): Response<SessionResponse>

    @GET("search_by_scaleReceipt.php")
    suspend fun getCargoInfoByReceiptNumber(
        @Query("receipt") receiptNumber: String
    ): Response<CargoInfoSearch>

    @GET("search_by_tracking.php")
    suspend fun getCargoInfoByTrackingNumber(
        @Query("tracking") trackingNumber: String
    ): Response<CargoSearchResponse>

    @GET("protected_proxy.php")
    suspend fun getLoadableTonnage(
        @Query("target") target: String = "app_api.php",
        @Query("action") action: String = "getLoadableTonnage",
        @Query("quotaNumber") quotaNumber: String,
        @Query("shippingCompany") shippingCompany: String,
        @Query("warehouse") warehouse: String,
        @Query("cargoType") cargoType: String
    ): Response<LoadableTonnageResponse>

    @GET("users_api.php")
    suspend fun getAllUsers(
        @Query("action") action: String = "getAllUsers"
    ): List<User>

    @POST("users_api.php")
    suspend fun createUser(@Body request: CreateUserRequest): Response<SuccessResponse>

    @POST("users_api.php")
    suspend fun updateUser(
        @Body request: UpdateUserRequest
    ): ApiResponse

    @POST("users_api.php")
    suspend fun deleteUser(
        @Body request: DeleteUserRequest
    ): ApiResponse

    @POST("check_logout.php")
    suspend fun logout(@Body logoutRequest: LogoutRequest): Response<LogoutResponse>

    @POST("confirm_cargo.php")
    suspend fun confirmCargo(@Body request: Map<String, String>): Response<Map<String, JsonElement>>

    @GET("protected_proxy.php")
    suspend fun getGroupedQuotas(
        @Query("target") target: String = "app_api.php",
        @Query("action") action: String = "getGroupedQuotas",
        @Query("shipName") shipName: String
    ): Response<Map<String, Map<String, List<QuotaItem>>>>

    @GET("protected_proxy.php")
    suspend fun updateTemporaryTonnage(
        @Query("target") target: String = "app_api.php",
        @Query("action") action: String = "updateTemporaryTonnage",
        @Query("quotaNumber") quotaNumber: String,
        @Query("enabled") enabled: Int,
        @Query("tonnage") tonnage: Double? = null
    ): Response<SuccessResponse>
}

data class ApiResponse(
    val success: Boolean,
    val message: String
)

data class ApiResponse2<T>(
    val data: T
)