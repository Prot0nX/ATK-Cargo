package com.atk.atk_cargo.api

import com.atk.atk_cargo.data.model.ActiveSessionResponse
import com.atk.atk_cargo.data.model.ActiveShipInfo
import com.atk.atk_cargo.data.model.CargoDeleteResponse
import com.atk.atk_cargo.data.model.CargoInfo
import com.atk.atk_cargo.data.model.CargoInfoRequest
import com.atk.atk_cargo.data.model.CargoInfoResponse
import com.atk.atk_cargo.data.model.CargoInfoSearch
import com.atk.atk_cargo.data.model.CargoSearchResponse
import com.atk.atk_cargo.data.model.CheckExistenceRequest
import com.atk.atk_cargo.data.model.CheckExistenceResponse
import com.atk.atk_cargo.data.model.ComprehensiveAnalysisResponse
import com.atk.atk_cargo.data.model.CreateUserRequest
import com.atk.atk_cargo.data.model.DeleteUserRequest
import com.atk.atk_cargo.data.model.FilteredSummaryResponse
import com.atk.atk_cargo.data.model.ForceLogoutRequest
import com.atk.atk_cargo.data.model.ForceLogoutResponse
import com.atk.atk_cargo.data.model.InitialInfo
import com.atk.atk_cargo.data.model.LoadableTonnageResponse
import com.atk.atk_cargo.data.model.LoginRequest
import com.atk.atk_cargo.data.model.LoginResponse
import com.atk.atk_cargo.data.model.LogoutRequest
import com.atk.atk_cargo.data.model.LogoutResponse
import com.atk.atk_cargo.data.model.MonitoringEventsResponse
import com.atk.atk_cargo.data.model.MonitoringSummaryResponse
import com.atk.atk_cargo.data.model.PermissionSyncRequest
import com.atk.atk_cargo.data.model.PermissionSyncResponse
import com.atk.atk_cargo.data.model.Quota
import com.atk.atk_cargo.data.model.QuotaDetails
import com.atk.atk_cargo.data.model.QuotaExistenceMultipleResponse
import com.atk.atk_cargo.data.model.QuotaItem
import com.atk.atk_cargo.data.model.QuotaStatusResponse
import com.atk.atk_cargo.data.model.RealTimeDataResponse
import com.atk.atk_cargo.data.model.SaveOrUpdateResponse
import com.atk.atk_cargo.data.model.ScaleReceiptCheckResponse
import com.atk.atk_cargo.data.model.SessionCheckRequest
import com.atk.atk_cargo.data.model.SessionResponse
import com.atk.atk_cargo.data.model.Ship
import com.atk.atk_cargo.data.model.ShipsData
import com.atk.atk_cargo.data.model.SuccessResponse
import com.atk.atk_cargo.data.model.UpdateUserRequest
import com.atk.atk_cargo.data.model.User
import com.atk.atk_cargo.data.model.Warehouse
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiServiceV2 {

    @GET("api/v2/index.php")
    suspend fun getShipsList(
        @Query("route") route: String = "ships"
    ): Response<ApiResponse2<ShipsData>>

    @GET("api/v2/index.php")
    suspend fun getShipDetails(
        @Query("route") route: String,
        // "no-cache" کش محلی OkHttp را دور می‌زند تا رفرش بعد از نوشتن، پاسخ قدیمیِ کش‌شده را برنگرداند
        @Header("Cache-Control") cacheControl: String? = null
    ): Response<Ship>

    @GET("api/v2/index.php")
    suspend fun getWarehouseDetails(
        @Query("route") route: String
    ): Response<Warehouse>

    @GET("api/v2/index.php")
    suspend fun getShipQuotas(
        @Query("route") route: String,
        @Header("Cache-Control") cacheControl: String? = null
    ): Response<List<Quota>>

    @GET("api/v2/index.php")
    suspend fun getQuotaDetails(
        @Query("route") route: String
    ): Response<QuotaDetails>

    @GET("api/v2/index.php")
    suspend fun getFilteredQuotas(
        @Query("route") route: String = "quotas/filtered",
        @Query("shipName") shipName: String,
        @Query("startDateTime") startDateTime: String,
        @Query("endDateTime") endDateTime: String
    ): Response<List<Quota>>

    @GET("api/v2/index.php")
    suspend fun getFilteredSummary(
        @Query("route") route: String = "quotas/filtered-summary",
        @Query("shipName") shipName: String,
        @Query("warehouseName") warehouseName: String,
        @Query("selectedQuota") selectedQuota: String,
        @Query("startDateTime") startDateTime: String,
        @Query("endDateTime") endDateTime: String
    ): Response<FilteredSummaryResponse>

    @GET("api/v2/index.php")
    suspend fun checkQuotaStatus(
        @Query("route") route: String,
        @Query("shipName") shipName: String? = null,
        @Query("cargoType") cargoType: String? = null,
        @Query("shippingCompany") shippingCompany: String? = null,
        @Query("warehouse") warehouse: String? = null
    ): Response<QuotaStatusResponse>

    @GET("api/v2/index.php")
    suspend fun getGroupedQuotas(
        @Query("route") route: String = "quotas/grouped",
        @Query("shipName") shipName: String
    ): Response<Map<String, Map<String, List<QuotaItem>>>>

    @GET("api/v2/index.php")
    suspend fun getLoadableTonnage(
        @Query("route") route: String,
        @Query("shippingCompany") shippingCompany: String,
        @Query("warehouse") warehouse: String,
        @Query("cargoType") cargoType: String
    ): Response<LoadableTonnageResponse>

    @POST("api/v2/index.php")
    suspend fun editQuota(
        @Query("route") route: String = "quotas/edit",
        @Query("id") id: Int,
        @Query("oldQuotaNumber") oldQuotaNumber: String,
        @Query("newQuotaNumber") newQuotaNumber: String,
        @Query("shipName") shipName: String,
        @Query("shippingCompany") shippingCompany: String,
        @Query("warehouse") warehouse: String,
        @Query("cargoType") cargoType: String,
        @Query("totalTonnage") totalTonnage: Float
    ): Response<SuccessResponse>

    @POST("api/v2/index.php")
    suspend fun updateQuotaPercentage(
        @Query("route") route: String,
        @Query("percentage") percentage: Double
    ): Response<SuccessResponse>

    @POST("api/v2/index.php")
    suspend fun toggleQuotaStatus(
        @Query("route") route: String
    ): Response<SuccessResponse>

    @POST("api/v2/index.php")
    suspend fun updateQuotaPercentageRestriction(
        @Query("route") route: String,
        @Query("isEnabled") isEnabled: Int
    ): Response<SuccessResponse>

    @POST("api/v2/index.php")
    suspend fun deleteQuota(
        @Query("route") route: String = "quotas/delete",
        @Query("quotaNumber") quotaNumber: String,
        @Query("shipName") shipName: String,
        @Query("warehouse") warehouse: String,
        @Query("shippingCompany") shippingCompany: String,
        @Query("cargoType") cargoType: String
    ): Response<SuccessResponse>

    @POST("api/v2/index.php")
    suspend fun updateTemporaryTonnage(
        @Query("route") route: String,
        @Query("enabled") enabled: Int,
        @Query("tonnage") tonnage: Double? = null
    ): Response<SuccessResponse>

    @GET("api/v2/index.php")
    suspend fun checkQuotaExistenceCargo(
        @Query("route") route: String = "quotas/existence",
        @Query("quotaNumber") quotaNumber: String,
        @Query("shipName") shipName: String
    ): Response<QuotaExistenceMultipleResponse>

    // ===== CARGO =====

    @GET("api/v2/index.php")
    suspend fun getActiveShips(
        @Query("route") route: String = "ships/active"
    ): Response<List<ActiveShipInfo>>

    @GET("api/v2/index.php")
    suspend fun getCargoInfo(
        @Query("route") route: String = "cargo/initial-info",
        @Query("quotaNumber") quotaNumber: String,
        @Query("shippingCompany") shippingCompany: String,
        @Query("warehouse") warehouse: String,
        @Query("cargoType") cargoType: String
    ): Response<CargoInfoResponse>

    @GET("api/v2/index.php")
    suspend fun checkScaleReceiptNumber(
        @Query("route") route: String = "cargo/scale-receipt/check",
        @Query("scaleReceiptNumber") scaleReceiptNumber: String
    ): Response<ScaleReceiptCheckResponse>

    @GET("api/v2/index.php")
    suspend fun getCargoInfoByReceiptNumber(
        @Query("route") route: String = "cargo/search/scale-receipt",
        @Query("receipt") receiptNumber: String
    ): Response<CargoInfoSearch>

    @GET("api/v2/index.php")
    suspend fun getCargoInfoByTrackingNumber(
        @Query("route") route: String = "cargo/search/tracking",
        @Query("tracking") trackingNumber: String
    ): Response<CargoSearchResponse>

    @POST("api/v2/index.php")
    suspend fun saveOrUpdateCargoInfo(
        @Body cargoInfo: CargoInfo,
        @Query("route") route: String = "cargo"
    ): Response<SaveOrUpdateResponse>

    // PATCH/DELETE قبلاً POST بودند؛ فعل معنایی درست، هماهنگ با routes/api_v2.php سمت سرور
    @PATCH("api/v2/index.php")
    suspend fun updateCargoInfo(
        @Body cargoInfo: CargoInfo,
        @Query("route") route: String = "cargo/update"
    ): Response<SaveOrUpdateResponse>

    // @DELETE استاندارد Retrofit با @Body کار نمی‌کند، پس این endpoint (که id/password در بدنه می‌فرستد) از @HTTP(hasBody=true) استفاده می‌کند
    @HTTP(method = "DELETE", path = "api/v2/index.php", hasBody = true)
    suspend fun deleteCargo(
        @Body cargoInfoRequest: CargoInfoRequest,
        @Query("route") route: String = "cargo/delete"
    ): Response<CargoDeleteResponse>

    @POST("api/v2/index.php")
    suspend fun confirmCargo(
        @Body request: Map<String, String>,
        @Query("route") route: String = "cargo/confirm"
    ): Response<Map<String, JsonElement>>

    @POST("api/v2/index.php")
    suspend fun saveInitialInfo(
        @Body initialInfo: InitialInfo,
        @Query("route") route: String = "cargo/initial-info"
    ): Response<Void>

    // ===== USERS =====
    // برخلاف v1، اینجا نیازی به @Query("action") نیست چون handler هر route خودش action را قبل از UserController::handle تنظیم می‌کند

    @GET("api/v2/index.php")
    suspend fun getAllUsers(
        @Query("route") route: String = "users"
    ): List<User>

    @GET("api/v2/index.php")
    suspend fun getAllUsersWithStatus(
        @Query("route") route: String = "users/status"
    ): List<User>

    @GET("api/v2/index.php")
    suspend fun getSelfProfile(
        @Query("route") route: String = "users/self"
    ): User

    @GET("api/v2/index.php")
    suspend fun getAdminUsers(
        @Query("route") route: String = "users/admins"
    ): List<User>

    @GET("api/v2/index.php")
    suspend fun getActiveDeviceId(
        @Query("route") route: String = "users/active-device",
        @Query("username") username: String
    ): Response<ActiveSessionResponse>

    @POST("api/v2/index.php")
    suspend fun createUser(
        @Body request: CreateUserRequest,
        @Query("route") route: String = "users"
    ): Response<SuccessResponse>

    // PATCH/DELETE — قبلاً POST بودند (Phase4 #33؛ همان دلیل بالا).
    @PATCH("api/v2/index.php")
    suspend fun updateUser(
        @Body request: UpdateUserRequest,
        @Query("route") route: String
    ): ApiResponse

    // @HTTP(hasBody=true) نه @DELETE — همان دلیل بالا (@DELETE استاندارد Retrofit با @Body کامپایل نمی‌شود)
    @HTTP(method = "DELETE", path = "api/v2/index.php", hasBody = true)
    suspend fun deleteUser(
        @Body request: DeleteUserRequest,
        @Query("route") route: String
    ): ApiResponse

    @POST("api/v2/index.php")
    suspend fun forceLogoutUser(
        @Body request: ForceLogoutRequest,
        @Query("route") route: String = "users/force-logout"
    ): Response<ForceLogoutResponse>

    // ===== CHAT =====

    @GET("api/v2/index.php")
    suspend fun getChatMessages(
        @Query("route") route: String = "chat/messages",
        @Query("lastMessageId") lastMessageId: Int = 0,
        @Query("olderThanId") olderThanId: Int = 0,
        @Query("limit") limit: Int = 50,
        @Query("username") username: String
    ): Response<ChatMessagesResponse>

    @POST("api/v2/index.php")
    suspend fun sendChatMessage(
        @Body request: SendMessageRequest,
        @Query("route") route: String = "chat/messages"
    ): Response<SendMessageResponse>

    // PATCH/DELETE — قبلاً POST بودند (Phase4 #33؛ همان دلیل بالا).
    @PATCH("api/v2/index.php")
    suspend fun editChatMessage(
        @Body request: EditMessageRequest,
        @Query("route") route: String
    ): Response<ApiResponse>

    // @HTTP(hasBody=true) نه @DELETE — همان دلیل بالا (@DELETE استاندارد Retrofit با @Body کامپایل نمی‌شود)
    @HTTP(method = "DELETE", path = "api/v2/index.php", hasBody = true)
    suspend fun deleteChatMessage(
        @Body request: DeleteMessageRequest,
        @Query("route") route: String
    ): Response<ApiResponse>

    // ===== ANALYTICS =====

    @GET("api/v2/index.php")
    suspend fun getRealTimeLoadingData(
        @Query("route") route: String = "analytics/realtime",
        @Query("shiftOffset") shiftOffset: Int = 0
    ): Response<RealTimeDataResponse>

    @GET("api/v2/index.php")
    suspend fun getComprehensiveAnalysis(
        @Query("route") route: String = "analytics/comprehensive",
        @Query("offset") offset: Int = 0
    ): Response<ComprehensiveAnalysisResponse>

    @POST("api/v2/index.php")
    suspend fun logAnalyticsExport(
        @Query("route") route: String = "analytics/export-log",
        @Query("scope") scope: String,
        @Query("groupCount") groupCount: Int
    ): Response<Unit>

    // ===== UTILITY =====

    @POST("api/v2/index.php")
    suspend fun checkExistence(
        @Body request: CheckExistenceRequest,
        @Query("route") route: String = "utility/check-existence"
    ): Response<CheckExistenceResponse>

    @POST("api/v2/index.php")
    suspend fun syncPermissions(
        @Body request: PermissionSyncRequest,
        @Query("route") route: String = "utility/sync-permissions"
    ): Response<PermissionSyncResponse>

    // ===== AUTH =====
    // auth/refresh قبلاً جدا در TokenRefresher.kt (با OkHttp خام) پیاده‌سازی شده و همان الگوی route=... را استفاده می‌کند

    @POST("api/v2/index.php")
    suspend fun checkLogin(
        @Body loginRequest: LoginRequest,
        @Query("route") route: String = "auth/login"
    ): Response<LoginResponse>

    @POST("api/v2/index.php")
    suspend fun checkSession(
        @Body request: SessionCheckRequest,
        @Query("route") route: String = "auth/session"
    ): Response<SessionResponse>

    @POST("api/v2/index.php")
    suspend fun logout(
        @Body logoutRequest: LogoutRequest,
        @Query("route") route: String = "auth/logout"
    ): Response<LogoutResponse>

    // ===== MONITORING (DEEP_CODE_AUDIT.md فاز۳ #۳۲ فاز ج) =====

    @GET("api/v2/index.php")
    suspend fun getMonitoringSummary(
        @Query("route") route: String = "monitoring/summary"
    ): Response<MonitoringSummaryResponse>

    @GET("api/v2/index.php")
    suspend fun getMonitoringEvents(
        @Query("route") route: String = "monitoring/events",
        @Query("status") status: String = "open",
        @Query("limit") limit: Int = 50,
        @Query("beforeId") beforeId: Int? = null
    ): Response<MonitoringEventsResponse>

    @POST("api/v2/index.php")
    suspend fun acknowledgeMonitoringEvent(
        @Query("route") route: String
    ): Response<ApiResponse>
}

object ApiV2Routes {
    fun shipDetails(shipName: String): String = "ships/$shipName"
    fun warehouseDetails(shipName: String, warehouseName: String): String =
        "ships/$shipName/warehouses/$warehouseName"
    fun shipQuotas(shipName: String): String = "ships/$shipName/quotas"
    fun quotaDetails(quotaNumber: String): String = "quotas/$quotaNumber"
    fun quotaStatus(quotaNumber: String): String = "quotas/$quotaNumber/status"
    fun userUpdate(id: Int): String = "users/$id/update"
    fun userDelete(id: Int): String = "users/$id/delete"
    fun chatMessageEdit(messageId: Int): String = "chat/messages/$messageId/edit"
    fun chatMessageDelete(messageId: Int): String = "chat/messages/$messageId/delete"
    fun quotaPercentage(id: Int): String = "quotas/$id/percentage"
    fun quotaToggleStatus(id: Int): String = "quotas/$id/toggle-status"
    fun quotaPercentageRestriction(id: Int): String = "quotas/$id/percentage-restriction"
    fun quotaTemporaryTonnage(quotaNumber: String): String = "quotas/$quotaNumber/temporary-tonnage"
    fun quotaLoadableTonnage(quotaNumber: String): String = "quotas/$quotaNumber/loadable-tonnage"
    fun monitoringEventAcknowledge(id: Int): String = "monitoring/events/$id/acknowledge"
}

data class ApiResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)

data class ApiResponse2<T>(
    @SerializedName("data") val data: T
)
