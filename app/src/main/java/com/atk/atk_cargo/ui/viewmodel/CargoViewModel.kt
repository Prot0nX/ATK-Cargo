package com.atk.atk_cargo.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atk.atk_cargo.api.ApiV2Routes
import com.atk.atk_cargo.api.RetrofitClient.apiServiceV2
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.data.model.CargoDeleteResponse
import com.atk.atk_cargo.data.model.CargoInfo
import com.atk.atk_cargo.data.model.CargoInfoRequest
import com.atk.atk_cargo.data.model.MatchingQuota
import com.atk.atk_cargo.data.model.MessageType
import com.atk.atk_cargo.data.model.QuotaExistenceMultipleResponse
import com.atk.atk_cargo.data.model.SaveOrUpdateResponse
import com.atk.atk_cargo.data.repository.ReportsRepository
import com.atk.atk_cargo.domain.model.Cargo
import com.atk.atk_cargo.domain.model.CargoConfirmStatus
import com.atk.atk_cargo.domain.model.CargoStatus
import com.atk.atk_cargo.domain.model.QuotaInfo
import com.atk.atk_cargo.domain.model.toDomain
import com.atk.atk_cargo.domain.model.toDto
import com.atk.atk_cargo.utils.JalaliDateUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

class CargoViewModelFactory(
    private val repository: ReportsRepository,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CargoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CargoViewModel(repository, userPreferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * حالت یکدست صفحه‌ی ثبت/نظارت حواله (DEEP_CODE_AUDIT.md #Phase3.5) —
 * جایگزین ۱۶ StateFlow مستقلی که قبلاً هر کدام یک subscription جدا در
 * Composableهای مصرف‌کننده داشتند. پیام‌های snackbar (`resultMessage`/
 * `showAnimatedMessage`/`messageType`) عمداً بیرون از این state ماندند چون
 * ماهیت‌شان یک صف رویداد یک‌باره‌مصرف است، نه state پایدار صفحه — با یک
 * data class واحد ترکیب‌شان به معنای رفتار متفاوت (پیام‌ها با هر تغییر state
 * دیگر دوباره emit می‌شوند) بود.
 */
data class CargoUiState(
    val cargoInfoList: List<Cargo> = emptyList(),
    val filteredCargoInfoList: List<Cargo> = emptyList(),
    val scaleReceiptNumber: String = "",
    val clearInputFields: Boolean = false,
    val initialInfo: QuotaInfo? = null,
    val totalNetWeight: String = "",
    val isSubmitting: Boolean = false,
    val showNetWeightDialog: Boolean = false,
    val showDuplicateConfirmationDialog: Boolean = false,
    val duplicateWarningMessage: String = "",
    val loadableTonnage: String = "",
    val loadableTrucks18Wheeler: String = "",
    val loadableTrucks10Wheeler: String = "",
    val duplicateTrackingNumbers: List<String> = emptyList(),
    val showDuplicateDialog: Boolean = false,
    val selectedShipNames: Set<String> = emptySet()
)

class CargoViewModel(
    private val repository: ReportsRepository,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {
    private val quotaValidationUseCase = com.atk.atk_cargo.feature.cargo.domain.QuotaValidationUseCase(repository)

    private val _uiState = MutableStateFlow(CargoUiState())
    val uiState: StateFlow<CargoUiState> = _uiState.asStateFlow()

    // ===== state داخلی که هیچ‌وقت مستقیم توسط UI مصرف نمی‌شود (بدون تغییر) =====
    private val _loadedWeight = MutableStateFlow("")
    private val _cargoCount = MutableStateFlow(0)
    private val _cargoWeight = MutableStateFlow("")
    private val _remainingWeight = MutableStateFlow("")
    private val _averageNetWeight = MutableStateFlow("")
    private val _remainingServices = MutableStateFlow("")
    private val _totalServices = MutableStateFlow("")
    private val _isQuotaActive = MutableStateFlow<Boolean?>(null)
    private val _pendingCargoInfo = MutableStateFlow<CargoInfo?>(null)

    private val snackbarQueue = com.atk.atk_cargo.feature.cargo.domain.CargoSnackbarQueue()
    val resultMessage: StateFlow<String> = snackbarQueue.resultMessage
    val showAnimatedMessage: StateFlow<Boolean> = snackbarQueue.showAnimatedMessage
    val messageType: StateFlow<MessageType> = snackbarQueue.messageType

    // آخرین جستجوی کاربر؛ باید بعد از هر بارگذاری (polling/refresh/تأیید)
    // دوباره اعمال شود، وگرنه لیست فیلترشده زیر انگشت کاربر با کل لیست
    // جایگزین می‌شود در حالی که متن جستجو هنوز در کادر جستجو باقی است.
    private var lastSearchQuery: String = ""

    fun updateSelectedShips(ships: Set<String>) {
        _uiState.update { it.copy(selectedShipNames = ships) }
    }

    private fun addMessageToQueue(message: String, type: MessageType) {
        snackbarQueue.addMessageToQueue(message, type)
    }

    fun dismissMessage() {
        snackbarQueue.dismissMessage()
    }

    // سرور اکنون isActive را مستقیماً برای هر ردیف نتیجه محاسبه می‌کند
    // (AppApiController::checkQuotaExistenceCargo)، پس دیگر لازم نیست به
    // ازای هر کوتاژ منطبق یک درخواست جداگانه‌ی checkQuotaStatus زده شود
    // (رِیس N+1 قبلی).
    suspend fun checkQuotaExistenceCargo(quotaNumber: String, shipName: String): QuotaExistenceMultipleResponse {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiServiceV2.checkQuotaExistenceCargo(quotaNumber = quotaNumber, shipName = shipName)
                if (response.isSuccessful) {
                    response.body() ?: throw Exception("پاسخ خالی از سرور")
                } else {
                    throw Exception("خطا در درخواست: ${response.code()}")
                }
            } catch (e: Exception) {
                throw Exception("خطا در بررسی وجود کوتاژ: ${e.message}")
            }
        }
    }

    // quota از قبل توسط QuotaEntryDialog اعتبارسنجی شده (وجود دارد، متعلق به
    // همین کشتی است، و فعال است)؛ اینجا آن بررسی دوباره تکرار نمی‌شود.
    // برخلاف پیاده‌سازی قبلی که یک InitialInfo ناقص با صفرهای دستی می‌ساخت و
    // آن را بلافاصله روی state می‌نشاند (تناژ/تعداد سرویس صفر لحظه‌ای روی UI،
    // و tempTonnageStatus=false که کنترل تناژ موقت را در همان بازه دور
    // می‌زد)، اینجا مستقیماً از سرور اطلاعات واقعی و کامل کوتاژ خوانده
    // می‌شود. quota.quotaNumber به‌صورت String به loadCargoInfoList می‌رود، پس
    // صفرهای ابتدایی (مثل «0123») برخلاف toIntOrNull() قبلی از بین نمی‌روند.
    fun switchQuota(quota: MatchingQuota) {
        viewModelScope.launch {
            loadCargoInfoList(
                quotaNumber = quota.quotaNumber,
                shippingCompany = quota.shippingCompany,
                warehouse = quota.warehouse,
                cargoType = quota.cargoType,
                onComplete = {
                    showMessage("کوتاژ با موفقیت تغییر یافت به: ${quota.quotaNumber}", MessageType.SUCCESS)
                }
            )
        }
    }

    fun filterCargoInfoList(query: String) {
        lastSearchQuery = query
        _uiState.update { state ->
            val filtered = if (query.isEmpty()) {
                state.cargoInfoList
            } else {
                state.cargoInfoList.filter { it.trackingNumber.contains(query, ignoreCase = true) }
            }
            state.copy(filteredCargoInfoList = filtered)
        }
    }

    fun updateCargoConfirmation(cargoId: Int?) {
        _uiState.update { state ->
            state.copy(cargoInfoList = state.cargoInfoList.map { cargoInfo ->
                if (cargoInfo.id == cargoId) {
                    cargoInfo.copy(
                        confirm = CargoConfirmStatus.CONFIRMED.wireValue,
                        exitTime = cargoInfo.exitTime?.takeIf { it.isNotBlank() } ?: getCurrentTime(),
                        exitDate = cargoInfo.exitDate?.takeIf { it.isNotBlank() } ?: getCurrentDate()
                    )
                } else {
                    cargoInfo
                }
            })
        }
        filterCargoInfoList(lastSearchQuery)
    }

    fun showMessage(message: String, type: MessageType) {
        snackbarQueue.showMessage(message, type)
    }

    private var lastQuotaStatusCheck: Long = 0
    private var lastLoadableTonnageUpdate: Long = 0
    private var cachedQuotaStatus: Boolean? = null
    private var cachedLoadableTonnage: String? = null
    private val loadableTonnageCacheTimeout = 30_000L

    private fun clearApiCache() {
        cachedQuotaStatus = null
        cachedLoadableTonnage = null
        lastQuotaStatusCheck = 0
        lastLoadableTonnageUpdate = 0
    }

    private fun checkForDuplicateTrackingNumbers(cargoList: List<Cargo>): List<String> {
        val trackingNumberCounts = mutableMapOf<String, Int>()
        cargoList.forEach { cargo ->
            val trackingNumber = cargo.trackingNumber.trim()
            trackingNumberCounts[trackingNumber] = trackingNumberCounts.getOrDefault(trackingNumber, 0) + 1
        }
        return trackingNumberCounts.filter { it.value > 1 }.keys.toList()
    }

    fun dismissDuplicateDialog() {
        _uiState.update { it.copy(showDuplicateDialog = false, duplicateTrackingNumbers = emptyList()) }
    }

    // این متد از چند مسیر متفاوت صدا زده می‌شود: هم لمس دستی دکمه‌ی
    // «بروزرسانی» توسط کاربر، هم به‌صورت داخلی بعد از ثبت/خروج حواله
    // (handleSuccessResponse)، غیرفعال‌شدن خودکار کوتاژ (toggleQuotaStatus) و
    // ویرایش حواله (updateCargoInfo) — یعنی صرفاً برای هماهنگ نگه‌داشتن لیست
    // با سرور، نه چون کاربر درخواست «بروزرسانی» داده. قبلاً پیام «اطلاعات در
    // ساعت ... به‌روزرسانی شد» همیشه از همینجا (با showMessage، یعنی دیالوگ
    // MessageDialog) نمایش داده می‌شد، پس بعد از هر ثبت حواله‌ی جدید یک دیالوگ
    // اضافه‌ی بی‌ربط هم روی دیالوگ موفقیت اصلی صف می‌کشید. حالا این پیام فقط
    // در صورت پاس‌دادن [onManualRefreshComplete] ساخته می‌شود؛ فقط دکمه‌ی
    // «بروزرسانی» (RegisterCargoScreen) این پارامتر را پر می‌کند و آن را به‌جای
    // دیالوگ در قالب Snackbar نشان می‌دهد. بقیه‌ی فراخوان‌ها بدون این پارامتر،
    // کاملاً بی‌صدا فقط لیست را همگام می‌کنند.
    fun refreshCargoInfo(onManualRefreshComplete: ((message: String) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.value.initialInfo?.let { info ->
                try {
                    if (info.loadingQuotaNumber.toString().isBlank() || info.shippingCompany.isBlank() ||
                        info.loadingWarehouse.isBlank() || info.cargoType.isBlank()) {
                        return@launch
                    }

                    val oldCargoList = _uiState.value.cargoInfoList

                    loadCargoInfoList(
                        quotaNumber = info.loadingQuotaNumber.toString(),
                        shippingCompany = info.shippingCompany,
                        warehouse = info.loadingWarehouse,
                        cargoType = info.cargoType,
                        onComplete = {
                            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                            val newCargoList = _uiState.value.cargoInfoList
                            val hasStatusChanges = oldCargoList.any { oldCargo ->
                                val newCargo = newCargoList.find { it.trackingNumber == oldCargo.trackingNumber }
                                newCargo != null && oldCargo.status != newCargo.status
                            }

                            if (hasStatusChanges) {
                                updateLoadableTonnageIfNeeded()
                            }

                            onManualRefreshComplete?.invoke(
                                if (hasStatusChanges) "وضعیت حواله‌ها به‌روزرسانی شد" else "اطلاعات در ساعت $currentTime به‌روزرسانی شد"
                            )
                        }
                    )
                } catch (e: Exception) {
                    showErrorMessage("خطا در به‌روزرسانی اطلاعات: ${e.message}")
                }
            } ?: run {
                Log.e("CargoViewModel_Log", "Attempted to refresh with null or empty initial info")
            }
        }
    }

    fun hideNetWeightDialog() {
        _uiState.update { it.copy(showNetWeightDialog = false, scaleReceiptNumber = "") }
    }

    fun submitCargoInfo(
        trackingNumber: String,
        netWeight: String,
        scaleReceiptNumber: String,
        shortageWeight: String,
        excessWeight: String,
        numberOfPeople: String
    ) {
        viewModelScope.launch {
            try {
                if (_uiState.value.isSubmitting) {
                    return@launch
                }

                _uiState.update { it.copy(isSubmitting = true) }

                if (!performBasicValidation(trackingNumber)) {
                    return@launch
                }

                val initialInfo = _uiState.value.initialInfo ?: run {
                    showErrorMessage("اطلاعات اولیه در دسترس نیست")
                    return@launch
                }

                val isNewCargo = _uiState.value.cargoInfoList.none { it.trackingNumber == trackingNumber }
                val validationResult = quotaValidationUseCase.validateQuotaStatusAndPercentage(initialInfo, isNewCargo)

                if (!validationResult.isValid) {
                    if (validationResult.percentageReached && validationResult.quotaIdToToggle != null) {
                        showMessage(validationResult.warningMessage ?: validationResult.message, MessageType.WARNING)

                        delay(1000.milliseconds)
                        toggleQuotaStatus(validationResult.quotaIdToToggle, initialInfo.loadingQuotaNumber.toString())
                    } else {
                        showErrorMessage(validationResult.message)
                    }
                    _isQuotaActive.value = validationResult.isActive
                    return@launch
                }

                _isQuotaActive.value = true

                val tempTonnageResult = quotaValidationUseCase.validateTempTonnage(initialInfo)
                if (!tempTonnageResult.isValid) {
                    showErrorMessage(tempTonnageResult.errorMessage ?: "")
                    return@launch
                }

                val inputValidationResult = quotaValidationUseCase.validateInputData(
                    trackingNumber, netWeight, numberOfPeople, shortageWeight, excessWeight, scaleReceiptNumber
                )
                if (!inputValidationResult.isValid) {
                    showErrorMessage(inputValidationResult.errorMessage ?: "")
                    return@launch
                }

                val userInfo = getUserInfo()
                if (userInfo == null) {
                    showErrorMessage("اطلاعات کاربری در دسترس نیست. لطفاً دوباره وارد شوید.")
                    return@launch
                }

                val cargoInfo = prepareCargoInfoForSubmission(
                    trackingNumber, numberOfPeople, userInfo.first, userInfo.second,
                    netWeight, scaleReceiptNumber, shortageWeight, excessWeight,
                    initialInfo
                )

                sendCargoInfoToServer(
                    cargoInfo, trackingNumber, netWeight,
                    scaleReceiptNumber, shortageWeight, excessWeight
                )
            } catch (e: Exception) {
                showErrorMessage("خطا در ثبت اطلاعات بار: ${e.message}")
            } finally {
                _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    private fun performBasicValidation(trackingNumber: String): Boolean {
        if (trackingNumber.isBlank()) {
            showErrorMessage("شماره حواله نمی‌تواند خالی باشد.")
            return false
        }
        return true
    }

    private suspend fun getUserInfo(): Pair<String, String>? {
        return try {
            val username = userPreferencesManager.username.first()
            val userType = userPreferencesManager.userType.first()
            if (username.isNotBlank() && userType.isNotBlank()) {
                Pair(username, userType)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun showErrorMessage(message: String) {
        snackbarQueue.showMessage(message, MessageType.ERROR)
    }

    private fun prepareCargoInfoForSubmission(
        trackingNumber: String,
        numberOfPeople: String,
        username: String,
        userType: String,
        netWeight: String,
        scaleReceiptNumber: String,
        shortageWeight: String,
        excessWeight: String,
        initialInfo: QuotaInfo
    ): CargoInfo {
        val isExit = netWeight.isNotBlank()
        val finalNumberOfPeople = numberOfPeople.ifEmpty { "1" }

        return CargoInfo(
            trackingNumber = trackingNumber,
            numberOfPeople = finalNumberOfPeople,
            username = username,
            userType = userType,
            entryTime = getCurrentTime(),
            netWeight = netWeight,
            scaleReceiptNumber = scaleReceiptNumber,
            shortageWeight = shortageWeight,
            excessWeight = excessWeight,
            exitTime = if (isExit) getCurrentTime() else null,
            exitDate = if (isExit) getCurrentDate() else null,
            status = if (isExit) CargoStatus.EXITED.wireValue else CargoStatus.ENTERED.wireValue,
            shipName = initialInfo.shipName,
            loadingWarehouse = initialInfo.loadingWarehouse,
            cargoType = initialInfo.cargoType,
            shippingCompany = initialInfo.shippingCompany,
            loadingQuotaNumber = initialInfo.loadingQuotaNumber.toString(),
            confirm = CargoConfirmStatus.PENDING.wireValue,
            confirmation = "no"
        )
    }

    private suspend fun sendCargoInfoToServer(
        cargoInfo: CargoInfo,
        trackingNumber: String,
        netWeight: String,
        scaleReceiptNumber: String,
        shortageWeight: String,
        excessWeight: String
    ) {
        try {
            val response = apiServiceV2.saveOrUpdateCargoInfo(cargoInfo)

            if (response.isSuccessful) {
                val responseBody = response.body()

                when {
                    responseBody?.error == true -> {
                        handleErrorResponse(responseBody)
                    }
                    responseBody?.status == "confirmation_needed" -> {
                        showMessage(responseBody.message, MessageType.WARNING)
                    }
                    else -> {
                        handleSuccessResponse(responseBody, trackingNumber, netWeight, scaleReceiptNumber, shortageWeight, excessWeight)
                        clearApiCache()
                    }
                }
            } else {
                _pendingCargoInfo.value = cargoInfo
                handleErrorHttpResponse(response)
            }
        } catch (e: Exception) {
            showErrorMessage("خطا در ارتباط با سرور: ${e.message}")
        }
    }

    private fun handleErrorHttpResponse(response: Response<SaveOrUpdateResponse>) {
        val errorBody = response.errorBody()?.string()
        val errorCode = response.code()

        try {
            val parsedError = parseErrorResponse(errorBody)
            if (parsedError != null) {
                if (parsedError.warning == true && errorCode == 409) {
                    handle24HourWarning(parsedError)
                } else {
                    handleErrorResponse(parsedError)
                }
            } else {
                showErrorMessage("خطا در ارسال اطلاعات بار: کد خطا $errorCode")
            }
        } catch (e: Exception) {
            showErrorMessage("خطا در پردازش پاسخ سرور: ${e.message}")
        }
    }

    private fun handle24HourWarning(responseBody: SaveOrUpdateResponse) {
        if (responseBody.requiresConfirmation == true) {
            _uiState.update { it.copy(duplicateWarningMessage = responseBody.message, showDuplicateConfirmationDialog = true) }
        } else {
            showMessage(responseBody.message, MessageType.WARNING)
        }
    }

    private fun handleErrorResponse(responseBody: SaveOrUpdateResponse) {
        val msg = when (responseBody.status) {
            "duplicate_voucher" -> {
                "حواله مورد نظر برای کشتی ${responseBody.shipName ?: ""} در شماره کوتاژ ${responseBody.loadingQuotaNumber ?: ""} قبلا ثبت شده است!"
            }
            else -> responseBody.message
        }
        showErrorMessage(msg)
    }

    private fun handleSuccessResponse(
        responseBody: SaveOrUpdateResponse?,
        trackingNumber: String,
        netWeight: String,
        scaleReceiptNumber: String,
        shortageWeight: String,
        excessWeight: String
    ) {
        val msg = when {
            netWeight.isNotBlank() -> {
                "شماره حواله $trackingNumber با شماره قبض باسکول $scaleReceiptNumber در تاریخ ${responseBody?.exitDate ?: "نامشخص"} و ساعت ${responseBody?.exitTime ?: "نامشخص"} و وزن خالص $netWeight خروج آن ثبت و سرویس آن بسته شد."
            }
            shortageWeight.isNotBlank() -> "حواله [$trackingNumber] با [$shortageWeight] کیلوگرم کسری بار ثبت شد!"
            excessWeight.isNotBlank() -> "حواله [$trackingNumber] با [$excessWeight] کیلوگرم اضافه بار ثبت شد!"
            else -> responseBody?.message ?: "حواله جدید با شماره [$trackingNumber] ثبت شد."
        }
        showMessage(msg, MessageType.SUCCESS)
        _uiState.update { it.copy(clearInputFields = true) }

        if (netWeight.isNotBlank()) {
            updateLocalCargoListForExit(trackingNumber, netWeight, responseBody)
        }
        refreshCargoInfo()
    }

    private fun updateLocalCargoListForExit(trackingNumber: String, netWeight: String, responseBody: SaveOrUpdateResponse?) {
        _uiState.update { state ->
            val updatedList = state.cargoInfoList.map { cargo ->
                if (cargo.trackingNumber == trackingNumber) {
                    cargo.copy(
                        status = CargoStatus.EXITED.wireValue,
                        netWeight = netWeight,
                        exitDate = responseBody?.exitDate ?: getCurrentDate(),
                        exitTime = responseBody?.exitTime ?: getCurrentTime()
                    )
                } else {
                    cargo
                }
            }
            state.copy(cargoInfoList = updatedList, filteredCargoInfoList = updatedList)
        }
    }

    private fun parseErrorResponse(errorBody: String?): SaveOrUpdateResponse? {
        return try {
            Gson().fromJson(errorBody, SaveOrUpdateResponse::class.java)
        } catch (_: Exception) {
            null
        }
    }

    fun dismissDuplicateConfirmationDialog() {
        _uiState.update { it.copy(showDuplicateConfirmationDialog = false, duplicateWarningMessage = "") }
        _pendingCargoInfo.value = null
    }

    fun confirmDuplicateCargoRegistration() {
        val cargoInfo = _pendingCargoInfo.value
        if (cargoInfo != null) {
            val updatedCargoInfo = cargoInfo.copy(duplicateConfirmation = "proceed")
            viewModelScope.launch {
                try {
                    val response = apiServiceV2.saveOrUpdateCargoInfo(updatedCargoInfo)
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody?.error == true) {
                            handleErrorResponse(responseBody)
                        } else {
                            handleSuccessResponse(
                                responseBody,
                                cargoInfo.trackingNumber,
                                cargoInfo.netWeight,
                                cargoInfo.scaleReceiptNumber,
                                cargoInfo.shortageWeight,
                                cargoInfo.excessWeight
                            )
                        }
                    } else {
                        handleErrorHttpResponse(response)
                    }
                } catch (e: Exception) {
                    showErrorMessage("خطا در ارتباط با سرور: ${e.message}")
                }
            }
        }
        dismissDuplicateConfirmationDialog()
    }

    fun cancelDuplicateCargoRegistration() {
        dismissDuplicateConfirmationDialog()
        showMessage("ثبت حواله لغو شد.", MessageType.ERROR)
    }

    fun resetClearInputFields() {
        _uiState.update { it.copy(clearInputFields = false) }
    }

    private fun isValidScaleReceipt(scaleReceipt: String): Boolean {
        return scaleReceipt.all { it.isDigit() }
    }

    private suspend fun checkScaleReceiptNumber(scaleReceiptNumber: String): Boolean {
        if (!isValidScaleReceipt(scaleReceiptNumber)) {
            showMessage("شماره قبض باسکول معتبر نیست. لطفاً دوباره اسکن کنید.", MessageType.ERROR)
            return false
        }

        return try {
            val response = apiServiceV2.checkScaleReceiptNumber(scaleReceiptNumber = scaleReceiptNumber)
            if (response.isSuccessful) {
                val result = response.body()
                if (result?.exists == true) {
                    showMessage(result.message, MessageType.ERROR)
                    val detailMsg = buildString {
                        append("شماره قبض باسکول تکراری است!\n\n")
                        append("این شماره قبلاً ثبت شده است:\n")
                        append("• شماره حواله: ${result.trackingNumber ?: "نامشخص"}\n")
                        append("• تناژ: ${result.netWeight ?: "نامشخص"} کیلوگرم\n")
                        append("• شماره کوتاژ: ${result.loadingQuotaNumber ?: "نامشخص"}")
                    }
                    showMessage(detailMsg, MessageType.ERROR)
                    false
                } else {
                    true
                }
            } else {
                val errorBody = response.errorBody()?.string()
                showMessage("خطا در بررسی شماره قبض باسکول: $errorBody", MessageType.ERROR)
                false
            }
        } catch (e: Exception) {
            showMessage("خطا در ارتباط با سرور: ${e.message}", MessageType.ERROR)
            false
        }
    }

    fun updateScaleReceiptNumber(barcode: String) {
        viewModelScope.launch {
            if (isValidScaleReceipt(barcode)) {
                if (checkScaleReceiptNumber(barcode)) {
                    _uiState.update { it.copy(scaleReceiptNumber = barcode, showNetWeightDialog = true) }
                }
            } else {
                showMessage("شماره قبض باسکول معتبر نیست. لطفاً دوباره اسکن کنید.", MessageType.ERROR)
            }
        }
    }

    // قبلاً این تابع دو بار (یک بار با repository.getInitialInfo و یک بار با
    // repository.getCargoInfo) همان endpoint سرور (getInitialInfo.php) را
    // صدا می‌زد که هر بار خودش ۴ کوئری روی سرور اجرا می‌کند، به‌علاوهٔ دو بار
    // getLoadableTonnage. آن فراخوانی اول (و ReportsRepository.getInitialInfo
    // که فقط همین‌جا مصرف می‌شد) کاملاً حذف شد؛ نتیجه یک درخواست getCargoInfo
    // و یک درخواست getLoadableTonnage به‌جای ۴ درخواست در هر بار refresh.
    fun loadCargoInfoList(
        quotaNumber: String,
        shippingCompany: String,
        warehouse: String,
        cargoType: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    repository.getCargoInfo(quotaNumber, shippingCompany, warehouse, cargoType)
                }

                val cargoList = result.cargoInfoList.map { it.toDomain() }
                val duplicateTrackingNumbers = checkForDuplicateTrackingNumbers(cargoList)
                if (duplicateTrackingNumbers.isNotEmpty()) {
                    _uiState.update { it.copy(duplicateTrackingNumbers = duplicateTrackingNumbers, showDuplicateDialog = true) }
                    Log.w("CargoViewModel_Log", "حواله‌های تکراری شناسایی شدند: ${duplicateTrackingNumbers.joinToString(", ")}")
                }

                _uiState.update { it.copy(cargoInfoList = cargoList, initialInfo = result.initialInfo.toDomain()) }
                // جستجوی فعال کاربر (در صورت وجود) دوباره اعمال می‌شود؛
                // وگرنه هر بارگذاری (polling هر ۳۰ ثانیه، refresh، تأیید
                // حواله) بی‌صدا لیست فیلترشده را با کل لیست جایگزین می‌کرد.
                filterCargoInfoList(lastSearchQuery)

                _cargoWeight.value = result.initialInfo.cargoWeight.toString()
                _uiState.update { it.copy(totalNetWeight = result.initialInfo.totalNetWeight.toString()) }
                _remainingWeight.value = result.initialInfo.remainingWeight.toString()
                _averageNetWeight.value = result.initialInfo.averageNetWeight.toString()
                _remainingServices.value = result.initialInfo.remainingServices.toString()
                _totalServices.value = result.initialInfo.totalVoucherCount.toString()

                val qNumber = result.initialInfo.loadingQuotaNumber.toString()
                val sCompany = result.initialInfo.shippingCompany
                val wHouse = result.initialInfo.loadingWarehouse
                val cType = result.initialInfo.cargoType

                // launch ساده (بدون CoroutineScope مستقل) به‌عنوان فرزند همین
                // coroutine که به viewModelScope وصل است اجرا می‌شود؛ با از
                // بین رفتن ViewModel به‌درستی لغو می‌شود.
                launch {
                    try {
                        val response = withContext(Dispatchers.IO) {
                            apiServiceV2.getLoadableTonnage(
                                route = ApiV2Routes.quotaLoadableTonnage(qNumber),
                                shippingCompany = sCompany,
                                warehouse = wHouse,
                                cargoType = cType
                            )
                        }

                        if (response.isSuccessful && response.body()?.success == true) {
                            val data = response.body()!!
                            data.loadableTonnage?.let { tonnage ->
                                val formattedValue = if (tonnage < 0) {
                                    "-" + DecimalFormat("#,###").format(abs(tonnage.roundToInt()))
                                } else {
                                    DecimalFormat("#,###").format(tonnage.roundToInt())
                                }
                                _uiState.update { it.copy(loadableTonnage = formattedValue) }
                            }

                            data.trucks18Wheeler?.let { count ->
                                _uiState.update { it.copy(loadableTrucks18Wheeler = count.toString()) }
                            }

                            data.trucks10Wheeler?.let { count ->
                                _uiState.update { it.copy(loadableTrucks10Wheeler = count.toString()) }
                            }
                        } else {
                            Log.e("CargoViewModel_Log", "Error in API call for loadable tonnage during initial load")
                        }
                    } catch (e: Exception) {
                        Log.e("CargoViewModel_Log", "Error calculating loadable tonnage", e)
                    }
                }

                updateInfoValues()
                onComplete()
            } catch (e: Exception) {
                showMessage("خطا در ارتباط با سرور: ${e.localizedMessage}", MessageType.ERROR)
                onComplete()
            }
        }
    }

    suspend fun toggleQuotaStatus(id: Int, quotaNumber: String) {
        try {
            val response = apiServiceV2.toggleQuotaStatus(route = ApiV2Routes.quotaToggleStatus(id))
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody?.success == true) {
                    clearApiCache()
                    refreshCargoInfo()
                } else {
                    addMessageToQueue("خطا در تغییر وضعیت کوتاژ: ${responseBody?.message}", MessageType.ERROR)
                }
            } else {
                val errorBody = response.errorBody()?.string()
                addMessageToQueue("خطا در تغییر وضعیت کوتاژ: $errorBody", MessageType.ERROR)
            }
        } catch (e: Exception) {
            addMessageToQueue("خطا در ارتباط با سرور: ${e.message}", MessageType.ERROR)
        }
    }

    fun updateCargoInfo(cargoInfo: Cargo, netWeight: String) {
        viewModelScope.launch {
            try {
                val updatedCargoInfo = cargoInfo.copy(
                    netWeight = netWeight,
                    exitTime = getCurrentTime(),
                    exitDate = getCurrentDate(),
                    status = CargoStatus.EXITED.wireValue
                )

                val response = apiServiceV2.saveOrUpdateCargoInfo(updatedCargoInfo.toDto())
                if (response.isSuccessful) {
                    _uiState.update { state ->
                        val updatedList = state.cargoInfoList.map { cargo ->
                            if (cargo.id == cargoInfo.id) updatedCargoInfo else cargo
                        }
                        state.copy(cargoInfoList = updatedList, filteredCargoInfoList = updatedList)
                    }

                    clearApiCache()
                    refreshCargoInfo()

                    showMessage("اطلاعات بروزرسانی شد", MessageType.SUCCESS)
                } else {
                    showErrorMessage("خطا در به روز رسانی اطلاعات بار")
                }
            } catch (e: Exception) {
                showErrorMessage("خطا در به روز رسانی اطلاعات بار: ${e.message}")
            }
        }
    }

    fun updateInfoValues() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val exitedCargos = _uiState.value.cargoInfoList.filter { it.status == CargoStatus.EXITED.wireValue }
                val netWeights = exitedCargos.mapNotNull { it.netWeight.toFloatOrNull() }
                val totalNet = netWeights.sum()
                val averageNet = if (netWeights.isNotEmpty()) netWeights.average() else 0.0
                val cargoWeightValue = _cargoWeight.value.replace(",", "").toFloatOrNull() ?: 0f
                val remaining = (cargoWeightValue - totalNet).coerceAtLeast(0f)
                val remainingServicesCount = if (averageNet > 0) (remaining / averageNet).toInt() else 0

                withContext(Dispatchers.Main) {
                    _remainingWeight.value = DecimalFormat("#,###").format(remaining.roundToInt())
                    _loadedWeight.value = DecimalFormat("#,###").format(totalNet.roundToInt())
                    _uiState.update { it.copy(totalNetWeight = DecimalFormat("#,###").format(totalNet.roundToInt())) }
                    _averageNetWeight.value = DecimalFormat("#,###").format(averageNet.roundToInt())
                    _remainingServices.value = remainingServicesCount.toString()
                    _totalServices.value = _cargoCount.value.toString()

                    val state = _uiState.value
                    if (state.loadableTrucks18Wheeler.isBlank() || state.loadableTrucks10Wheeler.isBlank()) {
                        val loadableTonnageValue = state.loadableTonnage.replace(",", "").toDoubleOrNull() ?: 0.0
                        updateLoadableTrucksCount(loadableTonnageValue)
                    }
                }
            } catch (e: Exception) {
                Log.e("CargoViewModel_Log", "Error updating info values", e)
            }
        }
    }

    // بررسی رمز و حذف حواله در همان یک درخواست به deleteCargoInfo.php انجام
    // می‌شود (سرور خودش رمز را بررسی و شمارنده‌ی تلاش را کنترل می‌کند)؛ قبلاً
    // این دو عملیات با دو فراخوانی HTTP جدا (checkPassword سپس deleteCargo)
    // انجام می‌شد که چیزی جلوی فراخوانی مستقیم deleteCargo بدون بررسی رمز را
    // نمی‌گرفت.
    fun deleteCargo(cargoInfoRequest: CargoInfoRequest) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiServiceV2.deleteCargo(cargoInfoRequest)
                }
                if (response.isSuccessful) {
                    showMessage(response.body()?.message ?: "حواله با موفقیت حذف شد.", MessageType.SUCCESS)

                    updateLoadableTonnageIfNeeded()

                    _uiState.value.initialInfo?.let { info ->
                        loadCargoInfoList(
                            quotaNumber = info.loadingQuotaNumber.toString(),
                            shippingCompany = info.shippingCompany,
                            warehouse = info.loadingWarehouse,
                            cargoType = info.cargoType,
                            onComplete = {}
                        )
                    }
                } else {
                    showErrorMessage(parseDeleteErrorMessage(response.errorBody()?.string()))
                }
            } catch (e: Exception) {
                showErrorMessage("استثنا در حذف حواله: ${e.message}")
            }
        }
    }

    private fun parseDeleteErrorMessage(errorBody: String?): String {
        return try {
            Gson().fromJson(errorBody, CargoDeleteResponse::class.java)?.message ?: "خطا در حذف حواله"
        } catch (_: Exception) {
            "خطا در حذف حواله"
        }
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm", Locale.US).format(System.currentTimeMillis())
    }

    private fun getCurrentDate(): String {
        return JalaliDateUtils.getCurrentJalaliDateString()
    }

    private fun updateLoadableTonnageIfNeeded(forceUpdate: Boolean = false) {
        val currentTime = System.currentTimeMillis()
        if (!forceUpdate && currentTime - lastLoadableTonnageUpdate < loadableTonnageCacheTimeout && cachedLoadableTonnage != null) {
            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            try {
                _uiState.value.initialInfo?.let { info ->
                    val response = withContext(Dispatchers.IO) {
                        apiServiceV2.getLoadableTonnage(
                            route = ApiV2Routes.quotaLoadableTonnage(info.loadingQuotaNumber.toString()),
                            shippingCompany = info.shippingCompany,
                            warehouse = info.loadingWarehouse,
                            cargoType = info.cargoType
                        )
                    }

                    if (response.isSuccessful && response.body()?.success == true) {
                        val data = response.body()!!
                        withContext(Dispatchers.Main.immediate) {
                            data.loadableTonnage?.let { tonnage ->
                                val formattedTonnage = DecimalFormat("#,###").format(tonnage.roundToInt())
                                _uiState.update { it.copy(loadableTonnage = formattedTonnage) }
                                cachedLoadableTonnage = formattedTonnage
                                lastLoadableTonnageUpdate = currentTime
                            }

                            data.trucks18Wheeler?.let { count ->
                                _uiState.update { it.copy(loadableTrucks18Wheeler = count.toString()) }
                            }

                            data.trucks10Wheeler?.let { count ->
                                _uiState.update { it.copy(loadableTrucks10Wheeler = count.toString()) }
                            }
                        }
                    } else {
                        Log.e("CargoViewModel_Log", "Error in API call for loadable tonnage: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("CargoViewModel_Log", "Error updating loadable tonnage", e)
            }
        }
    }

    private fun updateLoadableTrucksCount(loadableTonnage: Double) {
        val trucks18Wheeler = if (loadableTonnage > 0) (loadableTonnage / 25000.0).toInt() else 0
        val trucks10Wheeler = if (loadableTonnage > 0) (loadableTonnage / 15000.0).toInt() else 0
        _uiState.update { it.copy(loadableTrucks18Wheeler = trucks18Wheeler.toString(), loadableTrucks10Wheeler = trucks10Wheeler.toString()) }
    }

    fun setInitialInfo(initialInfo: QuotaInfo) {
        _uiState.update { it.copy(initialInfo = initialInfo) }
    }

    fun resetCurrentSelection() {
        _uiState.update {
            it.copy(
                initialInfo = null,
                cargoInfoList = emptyList(),
                filteredCargoInfoList = emptyList(),
                scaleReceiptNumber = "",
                clearInputFields = true,
                loadableTonnage = "",
                loadableTrucks18Wheeler = "",
                loadableTrucks10Wheeler = ""
            )
        }
        clearApiCache()
    }
}
