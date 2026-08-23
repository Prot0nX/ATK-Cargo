package com.atk.atk_cargo.feature.cargo.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atk.atk_cargo.data.model.CargoDeleteResponse
import com.atk.atk_cargo.data.model.CargoInfo
import com.atk.atk_cargo.data.model.CargoInfoRequest
import com.atk.atk_cargo.data.model.MatchingQuota
import com.atk.atk_cargo.data.model.MessageType
import com.atk.atk_cargo.data.model.QuotaExistenceMultipleResponse
import com.atk.atk_cargo.data.model.SaveOrUpdateResponse
import com.atk.atk_cargo.domain.model.Cargo
import com.atk.atk_cargo.domain.model.CargoConfirmStatus
import com.atk.atk_cargo.domain.model.CargoStatus
import com.atk.atk_cargo.domain.model.Kilograms
import com.atk.atk_cargo.domain.model.QuotaInfo
import com.atk.atk_cargo.domain.model.toDomain
import com.atk.atk_cargo.domain.model.toDto
import com.atk.atk_cargo.domain.repository.QuotaRepository
import com.atk.atk_cargo.domain.session.UserPreferencesStore
import com.atk.atk_cargo.feature.cargo.domain.CargoSnackbarQueue
import com.atk.atk_cargo.feature.cargo.domain.QuotaValidationUseCase
import com.atk.atk_cargo.utils.JalaliDateUtils
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

class CargoViewModelFactory(
    private val repository: QuotaRepository,
    private val userPreferencesManager: UserPreferencesStore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CargoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CargoViewModel(repository, userPreferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// دیالوگ‌های صفحه‌ی ثبت/نظارت حواله به‌عنوان یک state یکتا به‌جای ۳ پرچم boolean مستقل؛ با sealed interface فقط یک دیالوگ هم‌زمان فعال می‌شود
sealed interface CargoDialog {
    data object None : CargoDialog
    data object NetWeight : CargoDialog
    data class DuplicateConfirmation(val message: String) : CargoDialog
    data class Duplicates(val trackingNumbers: List<String>) : CargoDialog
}

// حالت یکدست صفحه‌ی ثبت/نظارت حواله به‌جای ۱۶ StateFlow مستقل؛ پیام‌های snackbar چون صف رویداد یک‌باره‌مصرف‌اند عمداً بیرون این state ماندند
data class CargoUiState(
    val cargoInfoList: List<Cargo> = emptyList(),
    val scaleReceiptNumber: String = "",
    val clearInputFields: Boolean = false,
    val initialInfo: QuotaInfo? = null,
    val totalNetWeight: Float = 0f,
    val isSubmitting: Boolean = false,
    val dialog: CargoDialog = CargoDialog.None,
    val loadableTonnage: Float? = null,
    val loadableTrucks18Wheeler: Int? = null,
    val loadableTrucks10Wheeler: Int? = null,
    val selectedShipNames: Set<String> = emptySet()
)

class CargoViewModel(
    private val repository: QuotaRepository,
    private val userPreferencesManager: UserPreferencesStore,
    // پیش‌فرض واقعی Dispatchers.IO است؛ فقط برای تست با یک TestDispatcher جایگزین می‌شود تا با scheduler مجازی تست هماهنگ شود
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val quotaValidationUseCase = QuotaValidationUseCase(repository)

    private val _uiState = MutableStateFlow(CargoUiState())
    val uiState: StateFlow<CargoUiState> = _uiState.asStateFlow()

    // نگهدارنده‌ی حواله‌ی در انتظار تأیید ثبت تکراری؛ چون فقط بین submitCargoInfo و confirmDuplicateCargoRegistration
    // رد و بدل می‌شود و هیچ Composable مستقیم آن را نمی‌خواند، عمداً بیرون CargoUiState ماند
    private val _pendingCargoInfo = MutableStateFlow<CargoInfo?>(null)

    private val snackbarQueue = CargoSnackbarQueue()
    val resultMessage: StateFlow<String> = snackbarQueue.resultMessage
    val showAnimatedMessage: StateFlow<Boolean> = snackbarQueue.showAnimatedMessage
    val messageType: StateFlow<MessageType> = snackbarQueue.messageType

    fun updateSelectedShips(ships: Set<String>) {
        _uiState.update { it.copy(selectedShipNames = ships) }
    }

    private fun addMessageToQueue(message: String, type: MessageType) {
        snackbarQueue.addMessageToQueue(message, type)
    }

    fun dismissMessage() {
        snackbarQueue.dismissMessage()
    }

    // سرور اکنون isActive را مستقیماً برای هر ردیف نتیجه محاسبه می‌کند، پس دیگر نیازی به درخواست جداگانه‌ی checkQuotaStatus برای هر کوتاژ نیست
    suspend fun checkQuotaExistenceCargo(quotaNumber: String, shipName: String): QuotaExistenceMultipleResponse {
        return withContext(ioDispatcher) {
            try {
                val response = repository.checkQuotaExistenceCargo(quotaNumber = quotaNumber, shipName = shipName)
                if (response.isSuccessful) {
                    response.body() ?: throw Exception("پاسخ خالی از سرور")
                } else {
                    throw Exception("خطا در درخواست: ${response.code()}")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                throw Exception("خطا در بررسی وجود کوتاژ: ${e.message}")
            }
        }
    }

    // quota از قبل توسط QuotaEntryDialog اعتبارسنجی شده؛ اینجا اطلاعات واقعی و کامل کوتاژ مستقیماً از سرور خوانده می‌شود، نه یک InitialInfo ناقص و موقت
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
    }

    fun showMessage(message: String, type: MessageType) {
        snackbarQueue.showMessage(message, type)
    }

 // کش تناژ قابل‌بارگیری به Repository منتقل شد؛ اینجا فقط نامعتبرش می‌کنیم
    private suspend fun clearApiCache() {
        repository.invalidateLoadableTonnageCache()
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
        _uiState.update { it.copy(dialog = CargoDialog.None) }
    }

    // این متد از چند مسیر صدا زده می‌شود تا لیست با سرور همگام بماند؛ پیام «به‌روزرسانی شد» فقط وقتی [onManualRefreshComplete] پاس داده شود ساخته می‌شود، نه در فراخوانی‌های بی‌صدای داخلی
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
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    showErrorMessage("خطا در به‌روزرسانی اطلاعات: ${e.message}")
                }
            } ?: run {
                Log.e("CargoViewModel_Log", "Attempted to refresh with null or empty initial info")
            }
        }
    }

    fun hideNetWeightDialog() {
        _uiState.update { it.copy(dialog = CargoDialog.None, scaleReceiptNumber = "") }
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
                    val quotaIdToToggle = validationResult.quotaIdToToggle
                    if (validationResult.percentageReached && quotaIdToToggle != null) {
                        showMessage(validationResult.warningMessage ?: validationResult.message, MessageType.WARNING)

                        delay(1000.milliseconds)
                        toggleQuotaStatus(quotaIdToToggle, initialInfo.loadingQuotaNumber.toString())
                    } else {
                        showErrorMessage(validationResult.message)
                    }
                    return@launch
                }

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
            } catch (e: CancellationException) {
                throw e
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
            val response = repository.saveOrUpdateCargoInfo(cargoInfo)

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
        } catch (e: CancellationException) {
            throw e
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            showErrorMessage("خطا در پردازش پاسخ سرور: ${e.message}")
        }
    }

    private fun handle24HourWarning(responseBody: SaveOrUpdateResponse) {
        if (responseBody.requiresConfirmation == true) {
            _uiState.update { it.copy(dialog = CargoDialog.DuplicateConfirmation(responseBody.message)) }
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
                        netWeight = Kilograms.parse(netWeight),
                        exitDate = responseBody?.exitDate ?: getCurrentDate(),
                        exitTime = responseBody?.exitTime ?: getCurrentTime()
                    )
                } else {
                    cargo
                }
            }
            state.copy(cargoInfoList = updatedList)
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
        _uiState.update { it.copy(dialog = CargoDialog.None) }
        _pendingCargoInfo.value = null
    }

    fun confirmDuplicateCargoRegistration() {
        val cargoInfo = _pendingCargoInfo.value
        if (cargoInfo != null) {
            val updatedCargoInfo = cargoInfo.copy(duplicateConfirmation = "proceed")
            viewModelScope.launch {
                try {
                    val response = repository.saveOrUpdateCargoInfo(updatedCargoInfo)
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
                } catch (e: CancellationException) {
                    throw e
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
            val response = repository.checkScaleReceiptNumber(scaleReceiptNumber = scaleReceiptNumber)
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            showMessage("خطا در ارتباط با سرور: ${e.message}", MessageType.ERROR)
            false
        }
    }

    fun updateScaleReceiptNumber(barcode: String) {
        viewModelScope.launch {
            if (isValidScaleReceipt(barcode)) {
                if (checkScaleReceiptNumber(barcode)) {
                    _uiState.update { it.copy(scaleReceiptNumber = barcode, dialog = CargoDialog.NetWeight) }
                }
            } else {
                showMessage("شماره قبض باسکول معتبر نیست. لطفاً دوباره اسکن کنید.", MessageType.ERROR)
            }
        }
    }

    // قبلاً همین endpoint سرور دو بار جدا صدا زده می‌شد؛ آن فراخوانی تکراری حذف شد و حالا فقط یک درخواست getCargoInfo و یک getLoadableTonnage در هر refresh انجام می‌شود
    fun loadCargoInfoList(
        quotaNumber: String,
        shippingCompany: String,
        warehouse: String,
        cargoType: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val result = withContext(ioDispatcher) {
                    repository.getCargoInfo(quotaNumber, shippingCompany, warehouse, cargoType)
                }

                val cargoList = result.cargoInfoList.map { it.toDomain() }
                val duplicateTrackingNumbers = checkForDuplicateTrackingNumbers(cargoList)
                if (duplicateTrackingNumbers.isNotEmpty()) {
                    _uiState.update { it.copy(dialog = CargoDialog.Duplicates(duplicateTrackingNumbers)) }
                    Log.w("CargoViewModel_Log", "حواله‌های تکراری شناسایی شدند: ${duplicateTrackingNumbers.joinToString(", ")}")
                }

                _uiState.update {
                    it.copy(
                        cargoInfoList = cargoList,
                        initialInfo = result.initialInfo.toDomain(),
                        totalNetWeight = result.initialInfo.totalNetWeight
                    )
                }

                val qNumber = result.initialInfo.loadingQuotaNumber.toString()
                val sCompany = result.initialInfo.shippingCompany
                val wHouse = result.initialInfo.loadingWarehouse
                val cType = result.initialInfo.cargoType

                // launch ساده به‌عنوان فرزند همین coroutine متصل به viewModelScope اجرا می‌شود؛ با از بین رفتن ViewModel به‌درستی لغو می‌شود.
 // forceRefresh=true چون این بارگذاری اولیه‌ی یک کوتاژ است، نه یک بررسی دوره‌ای؛ همیشه باید تازه باشد
                launch {
                    try {
                        val data = repository.getLoadableTonnage(
                            quotaNumber = qNumber,
                            shippingCompany = sCompany,
                            warehouse = wHouse,
                            cargoType = cType,
                            forceRefresh = true
                        )

                        if (data != null) {
                            data.loadableTonnage?.let { tonnage ->
                                _uiState.update { it.copy(loadableTonnage = tonnage) }
                            }

                            data.trucks18Wheeler?.let { count ->
                                _uiState.update { it.copy(loadableTrucks18Wheeler = count) }
                            }

                            data.trucks10Wheeler?.let { count ->
                                _uiState.update { it.copy(loadableTrucks10Wheeler = count) }
                            }
                        } else {
                            Log.e("CargoViewModel_Log", "Error in API call for loadable tonnage during initial load")
                        }
                    } catch (e: CancellationException) {
                        // لغو خودِ این coroutine باید عادی propagate شود، وگرنه لغو با پاک‌شدن ViewModel بی‌صدا بلعیده می‌شد
                        throw e
                    } catch (e: Throwable) {
                        // Throwable عمداً: این یک بروزرسانی best-effort است و نباید با لغو parent coroutine کل بارگذاری لیست را خراب کند
                        Log.e("CargoViewModel_Log", "Error calculating loadable tonnage", e)
                    }
                }

                updateInfoValues()
                onComplete()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showMessage("خطا در ارتباط با سرور: ${e.localizedMessage}", MessageType.ERROR)
                onComplete()
            }
        }
    }

    private fun parseCargoConfirmError(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            com.google.gson.JsonParser.parseString(errorBody)
                .asJsonObject.get("message")?.asString
        } catch (_: Exception) {
            null
        }
    }

    // قبلاً در CargoDetailsScreen.kt با rememberCoroutineScope() فراخوانی می‌شد که با خروج کاربر از صفحه در میانه‌ی راه کنسل می‌شد؛ حالا به viewModelScope منتقل شده
    fun confirmCargo(
        info: Cargo,
        username: String,
        userType: String,
        quotaNumber: String,
        shippingCompany: String,
        warehouse: String,
        cargoType: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val requestBody = mapOf(
                    "id" to (info.id?.toString() ?: "0"),
                    "username" to username,
                    "userType" to userType,
                    "loadingQuotaNumber" to info.loadingQuotaNumber,
                    "shipName" to info.shipName
                )

                val result = try {
                    val response = repository.confirmCargo(requestBody)
                    if (response.isSuccessful) {
                        val message = response.body()?.get("message")?.asString ?: "عملیات با موفقیت انجام شد"
                        Result.success(message)
                    } else {
                        // سرور برای خطاهای واقعی پیام فارسی گویا در بدنه‌ی خطا می‌فرستد؛ قبلاً این پیام دور ریخته می‌شد و کاربر فقط کد HTTP می‌دید
                        val serverMessage = parseCargoConfirmError(response.errorBody()?.string())
                        Result.failure(Exception(serverMessage ?: "خطا در ارتباط با سرور: ${response.code()}"))
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("CargoViewModel_Log", "Error confirming cargo", e)
                    Result.failure(e)
                }

                result.fold(
                    onSuccess = { message ->
                        updateCargoConfirmation(info.id)
                        loadCargoInfoList(
                            quotaNumber = quotaNumber,
                            shippingCompany = shippingCompany,
                            warehouse = warehouse,
                            cargoType = cargoType,
                            onComplete = { showMessage(message, MessageType.SUCCESS) }
                        )
                    },
                    onFailure = { error ->
                        Log.e("CargoViewModel_Log", "Error confirming cargo: ${error.message}", error)
                        showMessage("خطا: ${error.message}", MessageType.ERROR)
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("CargoViewModel_Log", "Exception in cargo confirmation process", e)
                showMessage("خطای غیرمنتظره: ${e.message}", MessageType.ERROR)
            } finally {
                onComplete()
            }
        }
    }

    suspend fun toggleQuotaStatus(id: Int, quotaNumber: String) {
        try {
            val response = repository.toggleCargoQuotaStatus(id)
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            addMessageToQueue("خطا در ارتباط با سرور: ${e.message}", MessageType.ERROR)
        }
    }

    fun updateCargoInfo(cargoInfo: Cargo, netWeight: String) {
        viewModelScope.launch {
            try {
                val updatedCargoInfo = cargoInfo.copy(
                    netWeight = Kilograms.parse(netWeight),
                    exitTime = getCurrentTime(),
                    exitDate = getCurrentDate(),
                    status = CargoStatus.EXITED.wireValue
                )

                val response = repository.saveOrUpdateCargoInfo(updatedCargoInfo.toDto())
                if (response.isSuccessful) {
                    _uiState.update { state ->
                        val updatedList = state.cargoInfoList.map { cargo ->
                            if (cargo.id == cargoInfo.id) updatedCargoInfo else cargo
                        }
                        state.copy(cargoInfoList = updatedList)
                    }

                    clearApiCache()
                    refreshCargoInfo()

                    showMessage("اطلاعات بروزرسانی شد", MessageType.SUCCESS)
                } else {
                    showErrorMessage("خطا در به روز رسانی اطلاعات بار")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showErrorMessage("خطا در به روز رسانی اطلاعات بار: ${e.message}")
            }
        }
    }

    // قبلاً اینجا مجموعه‌ای از مقادیر میانی (remainingWeight، averageNetWeight، remainingServices، totalServices)
    // هم محاسبه و در StateFlowهای جدا ذخیره می‌شد، اما هیچ‌کدام نه توسط UI و نه در جای دیگری از این کلاس خوانده
 // نمی‌شدند — محاسبه‌ای کاملاً مرده. تنها مقدار واقعاً مصرف‌شده totalNetWeight در CargoUiState بود
    fun updateInfoValues() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val exitedCargos = _uiState.value.cargoInfoList.filter { it.status == CargoStatus.EXITED.wireValue }
                val netWeights = exitedCargos.mapNotNull { cargo ->
                    // netWeight null یعنی رشته‌ی خام نامعتبر بود یا هنوز باسکول نشده؛ هر دو باید از میانگین/جمع کنار گذاشته شوند، نه به ۰ افتند
                    cargo.netWeight
                }
                val totalNet = netWeights.fold(Kilograms.ZERO) { acc, w -> acc + w }

                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(totalNetWeight = totalNet.value.toFloat()) }

                    val state = _uiState.value
                    if (state.loadableTrucks18Wheeler == null || state.loadableTrucks10Wheeler == null) {
                        updateLoadableTrucksCount(state.loadableTonnage ?: 0f)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("CargoViewModel_Log", "Error updating info values", e)
            }
        }
    }

    // بررسی رمز و حذف حواله در همان یک درخواست به deleteCargoInfo.php انجام می‌شود؛ قبلاً با دو فراخوانی جدا بود که جلوی حذف بدون بررسی رمز را نمی‌گرفت
    fun deleteCargo(cargoInfoRequest: CargoInfoRequest) {
        viewModelScope.launch {
            try {
                val response = withContext(ioDispatcher) {
                    repository.deleteCargo(cargoInfoRequest)
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
            } catch (e: CancellationException) {
                throw e
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

    // کش TTL ۳۰ ثانیه‌ای قبلاً اینجا (سه فیلد جدا + دستی) بود؛ حالا در Repository نگه‌داری می‌شود، پس این متد فقط forceRefresh
 // را عبور می‌دهد و منطق تازه/کهنه‌بودن را به repository.getLoadableTonnage واگذار می‌کند
    private fun updateLoadableTonnageIfNeeded(forceUpdate: Boolean = false) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                _uiState.value.initialInfo?.let { info ->
                    val data = withContext(ioDispatcher) {
                        repository.getLoadableTonnage(
                            quotaNumber = info.loadingQuotaNumber.toString(),
                            shippingCompany = info.shippingCompany,
                            warehouse = info.loadingWarehouse,
                            cargoType = info.cargoType,
                            forceRefresh = forceUpdate
                        )
                    }

                    if (data != null) {
                        withContext(Dispatchers.Main.immediate) {
                            data.loadableTonnage?.let { tonnage ->
                                _uiState.update { it.copy(loadableTonnage = tonnage) }
                            }

                            data.trucks18Wheeler?.let { count ->
                                _uiState.update { it.copy(loadableTrucks18Wheeler = count) }
                            }

                            data.trucks10Wheeler?.let { count ->
                                _uiState.update { it.copy(loadableTrucks10Wheeler = count) }
                            }
                        }
                    } else {
                        Log.e("CargoViewModel_Log", "Error in API call for loadable tonnage")
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("CargoViewModel_Log", "Error updating loadable tonnage", e)
            }
        }
    }

    private fun updateLoadableTrucksCount(loadableTonnage: Float) {
        val trucks18Wheeler = if (loadableTonnage > 0) (loadableTonnage / 25000f).toInt() else 0
        val trucks10Wheeler = if (loadableTonnage > 0) (loadableTonnage / 15000f).toInt() else 0
        _uiState.update { it.copy(loadableTrucks18Wheeler = trucks18Wheeler, loadableTrucks10Wheeler = trucks10Wheeler) }
    }

    fun setInitialInfo(initialInfo: QuotaInfo) {
        _uiState.update { it.copy(initialInfo = initialInfo) }
    }

    fun resetCurrentSelection() {
        _uiState.update {
            it.copy(
                initialInfo = null,
                cargoInfoList = emptyList(),
                scaleReceiptNumber = "",
                clearInputFields = true,
                loadableTonnage = null,
                loadableTrucks18Wheeler = null,
                loadableTrucks10Wheeler = null
            )
        }
        viewModelScope.launch { clearApiCache() }
    }
}
