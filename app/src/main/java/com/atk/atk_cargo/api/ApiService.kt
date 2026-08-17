package com.atk.atk_cargo.api

import com.atk.atk_cargo.data.model.RealTimeDataResponse
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    // ===== INITIAL INFO =====

    @POST("protected_proxy.php")
    suspend fun saveInitialInfo(
        @Body initialInfo: InitialInfo,
        @Query("target") target: String = "saveInitialInfo.php"
    ): Response<Void>

    @POST("protected_proxy.php")
    suspend fun checkExistence(
        @Body request: CheckExistenceRequest,
        @Query("target") target: String = "checkExistence.php"
    ): Response<CheckExistenceResponse>

    @GET("protected_proxy.php")
    suspend fun checkScaleReceiptNumber(
        @Query("scaleReceiptNumber") scaleReceiptNumber: String,
        @Query("target") target: String = "check_scale_receipt.php",
        @Query("action") action: String = "checkScaleReceiptNumber"
    ): Response<ScaleReceiptCheckResponse>

    @GET("protected_proxy.php")
    suspend fun checkQuotaExistenceCargo(
        @Query("target") target: String = "app_api.php",
        @Query("action") action: String = "checkQuotaExistenceCargo",
        @Query("quotaNumber") quotaNumber: String,
        @Query("shipName") shipName: String
    ): Response<QuotaExistenceMultipleResponse>

    @GET("protected_proxy.php")
    suspend fun getCargoInfo(
        @Query("quotaNumber") quotaNumber: String,
        @Query("shippingCompany") shippingCompany: String,
        @Query("warehouse") warehouse: String,
        @Query("cargoType") cargoType: String,
        @Query("target") target: String = "getInitialInfo.php",
        @Query("action") action: String = "getCargoInfo"
    ): Response<CargoInfoResponse>

    @GET("protected_proxy.php")
    suspend fun getActiveShips(
        @Query("target") target: String = "getActiveShips.php",
        @Query("action") action: String = "getActiveShips"
    ): Response<List<ActiveShipInfo>>

    @POST("protected_proxy.php")
    suspend fun saveOrUpdateCargoInfo(
        @Body cargoInfo: CargoInfo,
        @Query("target") target: String = "saveOrUpdateCargoInfo.php"
    ): Response<SaveOrUpdateResponse>

    @POST("protected_proxy.php")
    suspend fun updateCargoInfo(
        @Body cargoInfo: CargoInfo,
        @Query("target") target: String = "updateCargoInfo.php"
    ): Response<SaveOrUpdateResponse>

    @POST("protected_proxy.php")
    suspend fun deleteCargo(
        @Body cargoInfoRequest: CargoInfoRequest,
        @Query("target") target: String = "deleteCargoInfo.php"
    ): Response<CargoDeleteResponse>

    @FormUrlEncoded
    @POST("protected_proxy.php")
    suspend fun checkPassword(
        @Field("password") password: String,
        @Field("passwordType") passwordType: String,
        @Query("target") target: String = "check_password.php",
        @Query("action") action: String = "checkPassword"
    ): Response<PasswordCheckResponse>

    @POST("protected_proxy.php")
    suspend fun checkLogin(
        @Body loginRequest: LoginRequest,
        @Query("target") target: String = "check_Auth.php"
    ): Response<LoginResponse>

    // ===== SHIPS & WAREHOUSES =====

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

    // ===== QUOTAS =====

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

    // POST به‌جای GET: این عملیات داده را تغییر می‌دهد و نباید قابل بازپخش/کش
    // باشد (نگاه کنید به AppApiController::WRITE_ACTIONS سمت سرور).
    @POST("protected_proxy.php")
    suspend fun editQuota(
        @Query("target") target: String = "app_api.php",
        @Query("action") action: String = "editQuota",
        @Query("id") id: Int,
        @Query("oldQuotaNumber") oldQuotaNumber: String,
        @Query("newQuotaNumber") newQuotaNumber: String,
        @Query("shipName") shipName: String,
        @Query("shippingCompany") shippingCompany: String,
        @Query("warehouse") warehouse: String,
        @Query("cargoType") cargoType: String,
        @Query("totalTonnage") totalTonnage: Float
    ): Response<SuccessResponse>

    // این سه endpoint عمداً فقط با id (کلید یکتای InitialInfo) کار می‌کنند،
    // نه quotaNumber که یکتا نیست (سرور دیگر quotaNumber را برای این‌ها نمی‌پذیرد).
    @POST("protected_proxy.php")
    suspend fun updateQuotaPercentage(
        @Query("target") target: String = "app_api.php",
        @Query("action") action: String = "updateQuotaPercentage",
        @Query("id") id: Int,
        @Query("percentage") percentage: Double,
        @Query("isEnabled") isEnabled: Int
    ): Response<SuccessResponse>

    @POST("protected_proxy.php")
    suspend fun toggleQuotaStatus(
        @Query("target") target: String = "app_api.php",
        @Query("action") action: String = "toggleQuotaStatus",
        @Query("id") id: Int
    ): Response<SuccessResponse>

    @POST("protected_proxy.php")
    suspend fun updateQuotaPercentageRestriction(
        @Query("target") target: String = "app_api.php",
        @Query("action") action: String = "updateQuotaPercentageRestriction",
        @Query("id") id: Int,
        @Query("isEnabled") isEnabled: Int
    ): Response<SuccessResponse>

    @POST("protected_proxy.php")
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
        @Query("shippingCompany") shippingCompany: String? = null,
        @Query("warehouse") warehouse: String? = null
    ): Response<QuotaStatusResponse>

    @GET("protected_proxy.php")
    suspend fun getGroupedQuotas(
        @Query("target") target: String = "app_api.php",
        @Query("action") action: String = "getGroupedQuotas",
        @Query("shipName") shipName: String
    ): Response<Map<String, Map<String, List<QuotaItem>>>>

    @POST("protected_proxy.php")
    suspend fun updateTemporaryTonnage(
        @Query("target") target: String = "app_api.php",
        @Query("action") action: String = "updateTemporaryTonnage",
        @Query("quotaNumber") quotaNumber: String,
        @Query("enabled") enabled: Int,
        @Query("tonnage") tonnage: Double? = null
    ): Response<SuccessResponse>

    @GET("protected_proxy.php")
    suspend fun getLoadableTonnage(
        @Query("target") target: String = "app_api.php",
        @Query("action") action: String = "getLoadableTonnage",
        @Query("quotaNumber") quotaNumber: String,
        @Query("shippingCompany") shippingCompany: String,
        @Query("warehouse") warehouse: String,
        @Query("cargoType") cargoType: String
    ): Response<LoadableTonnageResponse>

    // ===== REAL-TIME & ANALYTICS =====

    @GET("protected_proxy.php")
    suspend fun getRealTimeLoadingData(
        @Query("action") action: String = "getRealTimeData",
        @Query("shiftOffset") shiftOffset: Int = 0,
        @Query("target") target: String = "realTimeLoadingData.php"
    ): Response<RealTimeDataResponse>

    @GET("protected_proxy.php")
    suspend fun getComprehensiveAnalysis(
        @Query("action") action: String = "getComprehensiveAnalysis",
        @Query("offset") offset: Int = 0,
        @Query("target") target: String = "realTimeLoadingData.php"
    ): Response<ComprehensiveAnalysisResponse>

    // A-5 (گزارش تحلیل جامع عملیات): ثبت ممیزی سمت سرور برای اشتراک‌گذاری
    // خلاصه تحلیل جامع؛ عملیات نوشتنی (لاگ) است، پس برخلاف دو تابع GET بالا
    // با POST فرستاده می‌شود.
    @POST("protected_proxy.php")
    suspend fun logAnalyticsExport(
        @Query("action") action: String = "logAnalyticsExport",
        @Query("scope") scope: String,
        @Query("groupCount") groupCount: Int,
        @Query("target") target: String = "realTimeLoadingData.php"
    ): Response<Unit>


    // ===== SEARCH =====

    @GET("protected_proxy.php")
    suspend fun getCargoInfoByReceiptNumber(
        @Query("receipt") receiptNumber: String,
        @Query("target") target: String = "search_by_scaleReceipt.php",
        @Query("action") action: String = "getCargoInfoByReceipt"
    ): Response<CargoInfoSearch>

    @GET("protected_proxy.php")
    suspend fun getCargoInfoByTrackingNumber(
        @Query("tracking") trackingNumber: String,
        @Query("target") target: String = "search_by_tracking.php",
        @Query("action") action: String = "getCargoInfoByTracking"
    ): Response<CargoSearchResponse>

    // ===== SESSION & AUTH =====

    @POST("protected_proxy.php")
    suspend fun checkSession(
        @Body request: SessionCheckRequest,
        @Query("target") target: String = "check_session.php"
    ): Response<SessionResponse>

    @POST("protected_proxy.php")
    suspend fun syncPermissions(
        @Body request: PermissionSyncRequest,
        @Query("target") target: String = "sync_permissions.php"
    ): Response<PermissionSyncResponse>

    @POST("protected_proxy.php")
    suspend fun logout(
        @Body logoutRequest: LogoutRequest,
        @Query("target") target: String = "check_logout.php"
    ): Response<LogoutResponse>

    @POST("protected_proxy.php")
    suspend fun forceLogoutUser(
        @Body request: ForceLogoutRequest,
        @Query("target") target: String = "users_api.php"
    ): Response<ForceLogoutResponse>

    // ===== CARGO OPS =====

    @POST("protected_proxy.php")
    suspend fun confirmCargo(
        @Body request: Map<String, String>,
        @Query("target") target: String = "confirm_cargo.php"
    ): Response<Map<String, JsonElement>>

    // ===== USERS =====

    @GET("protected_proxy.php")
    suspend fun getAllUsers(
        @Query("action") action: String = "getAllUsers",
        @Query("target") target: String = "users_api.php"
    ): List<User>

    @GET("protected_proxy.php")
    suspend fun getAllUsersWithStatus(
        @Query("action") action: String = "getAllUsersWithStatus",
        @Query("target") target: String = "users_api.php"
    ): List<User>

    @GET("protected_proxy.php")
    suspend fun getActiveDeviceId(
        @Query("action") action: String = "getActiveDeviceId",
        @Query("username") username: String,
        @Query("target") target: String = "users_api.php"
    ): Response<ActiveSessionResponse>

    @POST("protected_proxy.php")
    suspend fun createUser(
        @Body request: CreateUserRequest,
        @Query("target") target: String = "users_api.php",
        @Query("action") action: String = "createUser"
    ): Response<SuccessResponse>

    @POST("protected_proxy.php")
    suspend fun updateUser(
        @Body request: UpdateUserRequest,
        @Query("target") target: String = "users_api.php",
        @Query("action") action: String = "updateUser"
    ): ApiResponse

    @POST("protected_proxy.php")
    suspend fun deleteUser(
        @Body request: DeleteUserRequest,
        @Query("target") target: String = "users_api.php",
        @Query("action") action: String = "deleteUser"
    ): ApiResponse

    // ===== CHAT API ENDPOINTS =====

    @GET("protected_proxy.php")
    suspend fun getChatMessages(
        @Query("action") action: String = "getMessages",
        @Query("lastMessageId") lastMessageId: Int = 0,
        @Query("olderThanId") olderThanId: Int = 0,
        @Query("limit") limit: Int = 50,
        @Query("username") username: String,
        @Query("target") target: String = "chat_api.php"
    ): Response<ChatMessagesResponse>

    @POST("protected_proxy.php")
    suspend fun sendChatMessage(
        @Body request: SendMessageRequest,
        @Query("target") target: String = "chat_api.php",
        @Query("action") action: String = "sendMessage"
    ): Response<SendMessageResponse>

    @POST("protected_proxy.php")
    suspend fun editChatMessage(
        @Body request: EditMessageRequest,
        @Query("target") target: String = "chat_api.php",
        @Query("action") action: String = "editMessage"
    ): Response<ApiResponse>

    @POST("protected_proxy.php")
    suspend fun deleteChatMessage(
        @Body request: DeleteMessageRequest,
        @Query("target") target: String = "chat_api.php",
        @Query("action") action: String = "deleteMessage"
    ): Response<ApiResponse>

    @GET("protected_proxy.php")
    suspend fun getUnreadChatCount(
        @Query("action") action: String = "getUnreadCount",
        @Query("username") username: String,
        @Query("target") target: String = "chat_api.php"
    ): Response<UnreadCountResponse>
}

data class ApiResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)

data class ApiResponse2<T>(
    @SerializedName("data") val data: T
)