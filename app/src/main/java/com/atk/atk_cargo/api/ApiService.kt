package com.atk.atk_cargo.api

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

    @GET("app_api.php")
    suspend fun checkQuotaExistenceCargo(
        @Query("action") action: String = "checkQuotaExistenceCargo",
        @Query("quotaNumber") quotaNumber: String,
        @Query("shipName") shipName: String
    ): Response<QuotaExistenceMultipleResponse>

    @GET("getInitialInfo.php")
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

    @POST("deleteCargoInfo.php")
    suspend fun deleteCargo(@Body cargoInfoRequest: CargoInfoRequest): Response<Void>

    @FormUrlEncoded
    @POST("check_password.php")
    suspend fun checkPassword(
        @Field("password") password: String,
        @Field("passwordType") passwordType: String
    ): Response<PasswordCheckResponse>

    @POST("check_Auth.php")
    suspend fun checkLogin(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @GET("app_api.php")
    suspend fun getShipsList(@Query("action") action: String = "getShipsList"): Response<ApiResponse2<ShipsData>>

    @GET("app_api.php")
    suspend fun getShipDetails(
        @Query("action") action: String = "getShipDetails",
        @Query("shipName") shipName: String
    ): Response<Ship>

    @GET("app_api.php")
    suspend fun getWarehouseDetails(
        @Query("action") action: String = "getWarehouseDetails",
        @Query("shipName") shipName: String,
        @Query("warehouseName") warehouseName: String
    ): Response<Warehouse>

    @GET("app_api.php")
    suspend fun getQuotaDetails(
        @Query("action") action: String = "getQuotaDetails",
        @Query("quotaNumber") quotaNumber: String
    ): Response<QuotaDetails>

    @GET("app_api.php")
    suspend fun getShipQuotas(
        @Query("action") action: String = "getQuotasList",
        @Query("shipName") shipName: String
    ): Response<List<Quota>>

    @GET("app_api.php")
    suspend fun getFilteredSummary(
        @Query("action") action: String,
        @Query("shipName") shipName: String,
        @Query("warehouseName") warehouseName: String,
        @Query("selectedQuota") selectedQuota: String,
        @Query("startDateTime") startDateTime: String,
        @Query("endDateTime") endDateTime: String
    ): Response<FilteredSummaryResponse>

    @GET("app_api.php")
    suspend fun editQuota(
        @Query("action") action: String = "editQuota",
        @Query("oldQuotaNumber") oldQuotaNumber: String,
        @Query("newQuotaNumber") newQuotaNumber: String,
        @Query("shipName") shipName: String,
        @Query("shippingCompany") shippingCompany: String,
        @Query("warehouse") warehouse: String,
        @Query("cargoType") cargoType: String,
        @Query("totalTonnage") totalTonnage: Float
    ): Response<SuccessResponse>

    @GET("app_api.php")
    suspend fun updateQuotaPercentage(
        @Query("action") action: String = "updateQuotaPercentage",
        @Query("quotaNumber") quotaNumber: String,
        @Query("percentage") percentage: Double,
        @Query("isEnabled") isEnabled: Int
    ): Response<SuccessResponse>

    @GET("app_api.php")
    suspend fun toggleQuotaStatus(
        @Query("action") action: String = "toggleQuotaStatus",
        @Query("quotaNumber") quotaNumber: String
    ): Response<SuccessResponse>

    @GET("app_api.php")
    suspend fun updateQuotaPercentageRestriction(
        @Query("action") action: String = "updateQuotaPercentageRestriction",
        @Query("quotaNumber") quotaNumber: String,
        @Query("isEnabled") isEnabled: Int
    ): Response<SuccessResponse>

    @GET("app_api.php")
    suspend fun deleteQuota(
        @Query("action") action: String = "deleteQuota",
        @Query("quotaNumber") quotaNumber: String,
        @Query("shipName") shipName: String,
        @Query("warehouse") warehouse: String,
        @Query("shippingCompany") shippingCompany: String,
        @Query("cargoType") cargoType: String
    ): Response<SuccessResponse>

    @GET("app_api.php")
    suspend fun checkQuotaStatus(
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

    @POST("message_api.php")
    suspend fun sendMessage(@Body messageRequest: MessageRequest): Response<SuccessResponse>

    @POST("message_api.php")
    suspend fun markMessageAsRead(
        @Body request: MessageReadRequest
    ): Response<SuccessResponse>

    @GET("message_api.php")
    suspend fun getNewMessages(
        @Query("action") action: String = "getNewMessages",
        @Query("userType") userType: String,
        @Query("lastCheckTime") lastCheckTime: String,
        @Query("includeReadStatus") includeReadStatus: Boolean = true
    ): Response<List<Message>>

    @POST("check_session.php")
    suspend fun checkSession(@Body request: SessionCheckRequest): Response<SessionResponse>

    @GET("advancedsearch.php")
    suspend fun getCargoInfoByReceiptNumber(
        @Query("receipt") receiptNumber: String
    ): Response<CargoInfoSearch>

    @GET("app_api.php")
    suspend fun getLoadableTonnage(
        @Query("action") action: String = "getLoadableTonnage",
        @Query("quotaNumber") quotaNumber: String
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
}

data class ApiResponse(
    val success: Boolean,
    val message: String
)

data class ApiResponse2<T>(
    val data: T
)