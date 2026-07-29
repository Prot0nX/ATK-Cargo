package com.atk.atk_cargo.ui.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atk.atk_cargo.api.RetrofitClient.apiService
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.data.model.CargoInfo
import com.atk.atk_cargo.data.model.CargoInfoRequest
import com.atk.atk_cargo.data.model.InitialInfo
import com.atk.atk_cargo.data.model.MessageType
import com.atk.atk_cargo.data.model.QuotaExistenceMultipleResponse
import com.atk.atk_cargo.data.model.QuotaStatusResponse
import com.atk.atk_cargo.data.model.SaveOrUpdateResponse
import com.atk.atk_cargo.data.repository.ReportsRepository
import com.atk.atk_cargo.feature.cargo_entry.presentation.SnackbarMessage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
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

class CargoViewModel(
    private val repository: ReportsRepository,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {
    private val quotaValidationUseCase = com.atk.atk_cargo.feature.cargo.domain.QuotaValidationUseCase(repository)
    private val _cargoInfoList = MutableStateFlow<List<CargoInfo>>(emptyList())
    val cargoInfoList: StateFlow<List<CargoInfo>> = _cargoInfoList.asStateFlow()
    private val _snackbarMessage = MutableStateFlow<SnackbarMessage?>(null)
    val snackbarMessage: StateFlow<SnackbarMessage?> = _snackbarMessage.asStateFlow()
    private val _scaleReceiptNumber = MutableStateFlow("")
    val scaleReceiptNumber: StateFlow<String> = _scaleReceiptNumber
    private val _loadedWeight = MutableStateFlow("")
    private val _cargoCount = MutableStateFlow(0)
    private val _clearInputFields = MutableStateFlow(false)
    val clearInputFields: StateFlow<Boolean> = _clearInputFields.asStateFlow()
    private val _initialInfo = MutableStateFlow<InitialInfo?>(null)
    val initialInfo: StateFlow<InitialInfo?> = _initialInfo.asStateFlow()
    private val _cargoWeight = MutableStateFlow("")
    private val _totalNetWeight = MutableStateFlow("")
    val totalNetWeight: StateFlow<String> = _totalNetWeight.asStateFlow()
    private val _remainingWeight = MutableStateFlow("")
    private val _averageNetWeight = MutableStateFlow("")
    private val _remainingServices = MutableStateFlow("")
    private val _totalServices = MutableStateFlow("")
    private val snackbarQueue = com.atk.atk_cargo.feature.cargo.domain.CargoSnackbarQueue()
    val resultMessage: StateFlow<String> = snackbarQueue.resultMessage
    val showAnimatedMessage: StateFlow<Boolean> = snackbarQueue.showAnimatedMessage
    val messageType: StateFlow<MessageType> = snackbarQueue.messageType
    private val _showNetWeightDialog = MutableStateFlow(false)
    val showNetWeightDialog: StateFlow<Boolean> = _showNetWeightDialog.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _showDuplicateConfirmationDialog = MutableStateFlow(false)
    val showDuplicateConfirmationDialog: StateFlow<Boolean> = _showDuplicateConfirmationDialog.asStateFlow()
    private val _duplicateWarningMessage = MutableStateFlow("")
    val duplicateWarningMessage: StateFlow<String> = _duplicateWarningMessage.asStateFlow()
    private val _pendingCargoInfo = MutableStateFlow<CargoInfo?>(null)
    private val _filteredCargoInfoList = MutableStateFlow<List<CargoInfo>>(emptyList())
    val filteredCargoInfoList: StateFlow<List<CargoInfo>> = _filteredCargoInfoList.asStateFlow()
    private val _isQuotaActive = MutableStateFlow<Boolean?>(null)
    private val _loadableTonnage = MutableStateFlow("")
    val loadableTonnage: StateFlow<String> = _loadableTonnage.asStateFlow()

    private val _loadableTrucks18Wheeler = MutableStateFlow("")
    val loadableTrucks18Wheeler: StateFlow<String> = _loadableTrucks18Wheeler.asStateFlow()

    private val _loadableTrucks10Wheeler = MutableStateFlow("")
    val loadableTrucks10Wheeler: StateFlow<String> = _loadableTrucks10Wheeler.asStateFlow()

    private val _cachedTrackingNumbers = MutableStateFlow<Set<String>>(emptySet())

    private val _duplicateTrackingNumbers = MutableStateFlow<List<String>>(emptyList())
    val duplicateTrackingNumbers: StateFlow<List<String>> = _duplicateTrackingNumbers.asStateFlow()

    private val _showDuplicateDialog = MutableStateFlow(false)
    val showDuplicateDialog: StateFlow<Boolean> = _showDuplicateDialog.asStateFlow()

    private val _selectedShipNames = MutableStateFlow<Set<String>>(emptySet())
    val selectedShipNames: StateFlow<Set<String>> = _selectedShipNames.asStateFlow()

    fun updateSelectedShips(ships: Set<String>) {
        _selectedShipNames.value = ships
    }

    private fun addMessageToQueue(message: String, type: MessageType) {
        snackbarQueue.addMessageToQueue(message, type)
    }

    fun dismissMessage() {
        snackbarQueue.dismissMessage()
    }

    private fun showUpdateMessage(message: String) {
        _snackbarMessage.value = SnackbarMessage(message, MessageType.SUCCESS)
    }

    fun dismissSnackbar() {
        _snackbarMessage.value = null
    }

    suspend fun checkQuotaExistenceCargo(quotaNumber: String, shipName: String): QuotaExistenceMultipleResponse {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.checkQuotaExistenceCargo(quotaNumber = quotaNumber, shipName = shipName)
                if (response.isSuccessful) {
                    val quotaResponse = response.body() ?: throw Exception("پاسخ خالی از سرور")

                    val updatedQuotas = quotaResponse.matchingQuotas.map { quota ->
                        val quotaStatus = checkDetailedQuotaStatus(
                            quotaNumber = quota.quotaNumber,
                            shipName = shipName,
                            shippingCompany = quota.shippingCompany,
                            cargoType = quota.cargoType,
                            warehouse = quota.warehouse
                        )
                        quota.copy(isActive = quotaStatus.isActive)
                    }

                    return@withContext QuotaExistenceMultipleResponse(
                        exists = quotaResponse.exists,
                        matchingQuotas = updatedQuotas,
                        message = quotaResponse.message
                    )
                } else {
                    throw Exception("خطا در درخواست: ${response.code()}")
                }
            } catch (e: Exception) {
                throw Exception("خطا در بررسی وجود کوتاژ: ${e.message}")
            }
        }
    }

    private suspend fun checkDetailedQuotaStatus(
        quotaNumber: String,
        shipName: String,
        shippingCompany: String,
        cargoType: String,
        warehouse: String
    ): QuotaStatusResponse {
        return try {
            val response = apiService.checkQuotaStatus(
                quotaNumber = quotaNumber,
                shipName = shipName,
                cargoType = cargoType,
                shippingCompany = shippingCompany,
                warehouse = warehouse
            )
            if (response.isSuccessful) {
                response.body() ?: throw Exception("پاسخ خالی از سرور")
            } else {
                QuotaStatusResponse(
                    isActive = false,
                    status = false,
                    message = "خطا در بررسی وضعیت کوتاژ",
                    details = null
                )
            }
        } catch (e: Exception) {
            QuotaStatusResponse(
                isActive = false,
                status = false,
                message = "خطا در بررسی وضعیت کوتاژ: ${e.message}",
                details = null
            )
        }
    }

    fun filterCargoInfoList(query: String) {
        _filteredCargoInfoList.value = if (query.isEmpty()) {
            _cargoInfoList.value
        } else {
            _cargoInfoList.value.filter { it.trackingNumber.contains(query, ignoreCase = true) }
        }
    }

    fun updateCargoConfirmation(cargoId: Int?) {
        _cargoInfoList.value = _cargoInfoList.value.map { cargoInfo ->
            if (cargoInfo.id == cargoId) {
                cargoInfo.copy(
                    confirm = "تائید شده",
                    exitTime = cargoInfo.exitTime?.takeIf { it.isNotBlank() } ?: getCurrentTime(),
                    exitDate = cargoInfo.exitDate?.takeIf { it.isNotBlank() } ?: getCurrentDate()
                )
            } else {
                cargoInfo
            }
        }
        filterCargoInfoList("")
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

    private fun checkForDuplicateTrackingNumbers(cargoList: List<CargoInfo>): List<String> {
        val trackingNumberCounts = mutableMapOf<String, Int>()
        cargoList.forEach { cargo ->
            val trackingNumber = cargo.trackingNumber.trim()
            trackingNumberCounts[trackingNumber] = trackingNumberCounts.getOrDefault(trackingNumber, 0) + 1
        }
        return trackingNumberCounts.filter { it.value > 1 }.keys.toList()
    }

    fun dismissDuplicateDialog() {
        _showDuplicateDialog.value = false
        _duplicateTrackingNumbers.value = emptyList()
    }

    fun refreshCargoInfo() {
        viewModelScope.launch {
            _initialInfo.value?.let { info ->
                try {
                    if (info.loadingQuotaNumber.toString().isBlank() || info.shippingCompany.isBlank() ||
                        info.loadingWarehouse.isBlank() || info.cargoType.isBlank()) {
                        return@launch
                    }

                    val oldCargoList = _cargoInfoList.value

                    loadCargoInfoList(
                        quotaNumber = info.loadingQuotaNumber.toString(),
                        shippingCompany = info.shippingCompany,
                        warehouse = info.loadingWarehouse,
                        cargoType = info.cargoType,
                        onComplete = {
                            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                            val newCargoList = _cargoInfoList.value
                            val hasStatusChanges = oldCargoList.any { oldCargo ->
                                val newCargo = newCargoList.find { it.trackingNumber == oldCargo.trackingNumber }
                                newCargo != null && oldCargo.status != newCargo.status
                            }

                            if (hasStatusChanges) {
                                showUpdateMessage("وضعیت حواله‌ها به‌روزرسانی شد")
                                updateLoadableTonnageIfNeeded()
                            } else {
                                showUpdateMessage("اطلاعات در ساعت $currentTime به‌روزرسانی شد")
                            }
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
        _showNetWeightDialog.value = false
        _scaleReceiptNumber.value = ""
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
                if (_isSubmitting.value) {
                    return@launch
                }

                _isSubmitting.value = true

                if (!performBasicValidation(trackingNumber)) {
                    return@launch
                }

                val initialInfo = _initialInfo.value ?: run {
                    showErrorMessage("اطلاعات اولیه در دسترس نیست")
                    return@launch
                }

                val validationResult = quotaValidationUseCase.validateQuotaStatusAndPercentage(initialInfo)
                
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
                _isSubmitting.value = false
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
        initialInfo: InitialInfo
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

    private suspend fun sendCargoInfoToServer(
        cargoInfo: CargoInfo,
        trackingNumber: String,
        netWeight: String,
        scaleReceiptNumber: String,
        shortageWeight: String,
        excessWeight: String
    ) {
        try {
            val response = apiService.saveOrUpdateCargoInfo(cargoInfo)

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
            _duplicateWarningMessage.value = responseBody.message
            _showDuplicateConfirmationDialog.value = true
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
        _clearInputFields.value = true

        if (netWeight.isNotBlank()) {
            updateLocalCargoListForExit(trackingNumber, netWeight, responseBody)
        }
        refreshCargoInfo()
    }

    private fun updateLocalCargoListForExit(trackingNumber: String, netWeight: String, responseBody: SaveOrUpdateResponse?) {
        val updatedList = _cargoInfoList.value.map { cargo ->
            if (cargo.trackingNumber == trackingNumber) {
                cargo.copy(
                    status = "خروج",
                    netWeight = netWeight,
                    exitDate = responseBody?.exitDate ?: getCurrentDate(),
                    exitTime = responseBody?.exitTime ?: getCurrentTime()
                )
            } else {
                cargo
            }
        }
        _cargoInfoList.value = updatedList
        _filteredCargoInfoList.value = updatedList
    }

    private fun parseErrorResponse(errorBody: String?): SaveOrUpdateResponse? {
        return try {
            Gson().fromJson(errorBody, SaveOrUpdateResponse::class.java)
        } catch (_: Exception) {
            null
        }
    }

    fun dismissDuplicateConfirmationDialog() {
        _showDuplicateConfirmationDialog.value = false
        _duplicateWarningMessage.value = ""
        _pendingCargoInfo.value = null
    }

    fun confirmDuplicateCargoRegistration() {
        val cargoInfo = _pendingCargoInfo.value
        if (cargoInfo != null) {
            val updatedCargoInfo = cargoInfo.copy(duplicateConfirmation = "proceed")
            viewModelScope.launch {
                try {
                    val response = apiService.saveOrUpdateCargoInfo(updatedCargoInfo)
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
        _clearInputFields.value = false
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
            val response = apiService.checkScaleReceiptNumber(scaleReceiptNumber)
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
                    _scaleReceiptNumber.value = barcode
                    _showNetWeightDialog.value = true
                }
            } else {
                showMessage("شماره قبض باسکول معتبر نیست. لطفاً دوباره اسکن کنید.", MessageType.ERROR)
            }
        }
    }

    fun loadCargoInfoList(
        quotaNumber: String,
        shippingCompany: String,
        warehouse: String,
        cargoType: String,
        onComplete: () -> Unit = {}
    ) {
        CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
            try {
                val initialShipInfo = withContext(Dispatchers.IO) {
                    repository.getInitialInfo(quotaNumber, shippingCompany, warehouse, cargoType)
                }

                initialShipInfo?.let { info ->
                    try {
                        val response = withContext(Dispatchers.IO) {
                            apiService.getLoadableTonnage(
                                quotaNumber = info.loadingQuotaNumber.toString(),
                                shippingCompany = info.shippingCompany,
                                warehouse = info.loadingWarehouse,
                                cargoType = info.cargoType
                            )
                        }

                        if (response.isSuccessful && response.body()?.success == true) {
                            val data = response.body()!!

                            withContext(Dispatchers.Main.immediate) {
                                data.loadableTonnage?.let { tonnage ->
                                    val formattedValue = if (tonnage < 0) {
                                        "-" + DecimalFormat("#,###").format(abs(tonnage.roundToInt()))
                                    } else {
                                        DecimalFormat("#,###").format(tonnage.roundToInt())
                                    }
                                    _loadableTonnage.value = formattedValue
                                }

                                data.trucks18Wheeler?.let { count ->
                                    _loadableTrucks18Wheeler.value = count.toString()
                                }

                                data.trucks10Wheeler?.let { count ->
                                    _loadableTrucks10Wheeler.value = count.toString()
                                }
                            }
                        } else {
                            Log.e("CargoViewModel_Log", "Error in API call for initial loadable tonnage")
                        }
                    } catch (e: Exception) {
                        Log.e("CargoViewModel_Log", "Error in API call for loadable tonnage", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("CargoViewModel_Log", "Error in pre-loading tonnage data", e)
            }
        }

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    repository.getCargoInfo(quotaNumber, shippingCompany, warehouse, cargoType)
                }

                val duplicateTrackingNumbers = checkForDuplicateTrackingNumbers(result.cargoInfoList)
                if (duplicateTrackingNumbers.isNotEmpty()) {
                    withContext(Dispatchers.Main.immediate) {
                        _duplicateTrackingNumbers.value = duplicateTrackingNumbers
                        _showDuplicateDialog.value = true
                        Log.w("CargoViewModel_Log", "حواله‌های تکراری شناسایی شدند: ${duplicateTrackingNumbers.joinToString(", ")}")
                    }
                }

                _cargoInfoList.value = result.cargoInfoList
                _initialInfo.value = result.initialInfo
                _filteredCargoInfoList.value = result.cargoInfoList

                val allTrackingNumbers = result.allTrackingNumbers?.toSet() ?: emptySet()
                _cachedTrackingNumbers.value = allTrackingNumbers

                withContext(Dispatchers.Main.immediate) {
                    _cargoWeight.value = result.initialInfo.cargoWeight.toString()
                    _totalNetWeight.value = result.initialInfo.totalNetWeight.toString()
                    _remainingWeight.value = result.initialInfo.remainingWeight.toString()
                    _averageNetWeight.value = result.initialInfo.averageNetWeight.toString()
                    _remainingServices.value = result.initialInfo.remainingServices.toString()
                    _totalServices.value = result.initialInfo.totalVoucherCount.toString()
                }

                val qNumber = result.initialInfo.loadingQuotaNumber.toString()
                val sCompany = result.initialInfo.shippingCompany
                val wHouse = result.initialInfo.loadingWarehouse
                val cType = result.initialInfo.cargoType

                CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
                    try {
                        val response = withContext(Dispatchers.IO) {
                            apiService.getLoadableTonnage(
                                quotaNumber = qNumber,
                                shippingCompany = sCompany,
                                warehouse = wHouse,
                                cargoType = cType
                            )
                        }

                        if (response.isSuccessful && response.body()?.success == true) {
                            val data = response.body()!!
                            withContext(Dispatchers.Main.immediate) {
                                data.loadableTonnage?.let { tonnage ->
                                    _loadableTonnage.value = DecimalFormat("#,###").format(tonnage.roundToInt())
                                }

                                data.trucks18Wheeler?.let { count ->
                                    _loadableTrucks18Wheeler.value = count.toString()
                                }

                                data.trucks10Wheeler?.let { count ->
                                    _loadableTrucks10Wheeler.value = count.toString()
                                }
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
            val response = apiService.toggleQuotaStatus(
                id = id,
                quotaNumber = quotaNumber
            )
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

    fun updateCargoInfo(cargoInfo: CargoInfo, netWeight: String) {
        viewModelScope.launch {
            try {
                val updatedCargoInfo = cargoInfo.copy(
                    netWeight = netWeight,
                    exitTime = getCurrentTime(),
                    exitDate = getCurrentDate(),
                    status = "خروج"
                )

                val response = apiService.saveOrUpdateCargoInfo(updatedCargoInfo)
                if (response.isSuccessful) {
                    val updatedList = _cargoInfoList.value.map { cargo ->
                        if (cargo.id == cargoInfo.id) updatedCargoInfo else cargo
                    }

                    _cargoInfoList.value = updatedList
                    _filteredCargoInfoList.value = updatedList

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
                val exitedCargos = _cargoInfoList.value.filter { it.status == "خروج" }
                val netWeights = exitedCargos.mapNotNull { it.netWeight.toFloatOrNull() }
                val totalNet = netWeights.sum()
                val averageNet = if (netWeights.isNotEmpty()) netWeights.average() else 0.0
                val cargoWeightValue = _cargoWeight.value.replace(",", "").toFloatOrNull() ?: 0f
                val remaining = (cargoWeightValue - totalNet).coerceAtLeast(0f)
                val remainingServicesCount = if (averageNet > 0) (remaining / averageNet).toInt() else 0

                withContext(Dispatchers.Main) {
                    _remainingWeight.value = DecimalFormat("#,###").format(remaining.roundToInt())
                    _loadedWeight.value = DecimalFormat("#,###").format(totalNet.roundToInt())
                    _totalNetWeight.value = DecimalFormat("#,###").format(totalNet.roundToInt())
                    _averageNetWeight.value = DecimalFormat("#,###").format(averageNet.roundToInt())
                    _remainingServices.value = remainingServicesCount.toString()
                    _totalServices.value = _cargoCount.value.toString()

                    if (_loadableTrucks18Wheeler.value.isBlank() || _loadableTrucks10Wheeler.value.isBlank()) {
                        val loadableTonnageValue = _loadableTonnage.value.replace(",", "").toDoubleOrNull() ?: 0.0
                        updateLoadableTrucksCount(loadableTonnageValue)
                    }
                }
            } catch (e: Exception) {
                Log.e("CargoViewModel_Log", "Error updating info values", e)
            }
        }
    }

    fun deleteCargo(cargoInfoRequest: CargoInfoRequest, password: String) {
        viewModelScope.launch {
            try {
                val passwordResponse = withContext(Dispatchers.IO) {
                    apiService.checkPassword(password, "delete_info")
                }
                if (passwordResponse.isSuccessful && passwordResponse.body()?.success == true) {
                    val deleteResponse = withContext(Dispatchers.IO) {
                        apiService.deleteCargo(cargoInfoRequest)
                    }
                    if (deleteResponse.isSuccessful) {
                        showMessage("حواله با موفقیت حذف شد.", MessageType.SUCCESS)

                        updateLoadableTonnageIfNeeded()

                        _initialInfo.value?.let { info ->
                            loadCargoInfoList(
                                quotaNumber = info.loadingQuotaNumber.toString(),
                                shippingCompany = info.shippingCompany,
                                warehouse = info.loadingWarehouse,
                                cargoType = info.cargoType,
                                onComplete = {}
                            )
                        }
                    } else {
                        showErrorMessage("خطا در حذف حواله: ${deleteResponse.errorBody()?.string()}")
                    }
                } else {
                    showErrorMessage(passwordResponse.body()?.message ?: "خطا در بررسی رمز عبور")
                }
            } catch (e: Exception) {
                showErrorMessage("استثنا در حذف حواله: ${e.message}")
            }
        }
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm", Locale.US).format(System.currentTimeMillis())
    }

    private fun getCurrentDate(): String {
        return gregorianToJalali(Calendar.getInstance())
    }

    private fun updateLoadableTonnageIfNeeded(forceUpdate: Boolean = false) {
        val currentTime = System.currentTimeMillis()
        if (!forceUpdate && currentTime - lastLoadableTonnageUpdate < loadableTonnageCacheTimeout && cachedLoadableTonnage != null) {
            return
        }

        CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
            try {
                _initialInfo.value?.let { info ->
                    val response = withContext(Dispatchers.IO) {
                        apiService.getLoadableTonnage(
                            quotaNumber = info.loadingQuotaNumber.toString(),
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
                                _loadableTonnage.value = formattedTonnage
                                cachedLoadableTonnage = formattedTonnage
                                lastLoadableTonnageUpdate = currentTime
                            }

                            data.trucks18Wheeler?.let { count ->
                                _loadableTrucks18Wheeler.value = count.toString()
                            }

                            data.trucks10Wheeler?.let { count ->
                                _loadableTrucks10Wheeler.value = count.toString()
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
        _loadableTrucks18Wheeler.value = trucks18Wheeler.toString()
        _loadableTrucks10Wheeler.value = trucks10Wheeler.toString()
    }

    fun setInitialInfo(initialInfo: InitialInfo) {
        _initialInfo.value = initialInfo
    }

    fun resetCurrentSelection() {
        _initialInfo.value = null
        _cargoInfoList.value = emptyList()
        _filteredCargoInfoList.value = emptyList()
        _scaleReceiptNumber.value = ""
        _clearInputFields.value = true
        _loadableTonnage.value = ""
        _loadableTrucks18Wheeler.value = ""
        _loadableTrucks10Wheeler.value = ""
        clearApiCache()
    }

    @SuppressLint("DefaultLocale")
    private fun gregorianToJalali(gregorian: Calendar): String {
        val gy = gregorian.get(Calendar.YEAR)
        val gm = gregorian.get(Calendar.MONTH) + 1
        val gd = gregorian.get(Calendar.DAY_OF_MONTH)

        val gregorianDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365)
        val jalaliDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        val gy2 = if (gm > 2) gy + 1 else gy
        var days =
            355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + gregorianDaysInMonth[gm - 1]

        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461

        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }

        var jm = 0
        for (i in 0..11) {
            if (days < jalaliDaysInMonth[i]) {
                jm = i + 1
                break
            }
            days -= jalaliDaysInMonth[i]
        }
        val jd = days + 1
        return String.format("%04d/%02d/%02d", jy, jm, jd)
    }
}
