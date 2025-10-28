package com.atk.atk_cargo.api

// ML Kit imports removed and replaced with Tesseract
import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.os.Environment
import android.os.Parcelable
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.FileProvider
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.atk.atk_cargo.SnackbarMessage
import com.atk.atk_cargo.api.RetrofitClient.apiService
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.BaseFont.IDENTITY_H
import com.itextpdf.text.pdf.BaseFont.createFont
import com.itextpdf.text.pdf.ColumnText
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import retrofit2.Response
import java.io.File
import java.io.FileNotFoundException
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.LinkedList
import java.util.Locale
import java.util.Queue
import kotlin.math.abs
import kotlin.math.roundToInt

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
    private val _cargoInfoList = MutableStateFlow<List<CargoInfo>>(emptyList())
    val cargoInfoList: StateFlow<List<CargoInfo>> = _cargoInfoList.asStateFlow()
    private val _snackbarMessage = MutableStateFlow<SnackbarMessage?>(null)
    val snackbarMessage: StateFlow<SnackbarMessage?> = _snackbarMessage.asStateFlow()
    private val _scaleReceiptNumber = MutableStateFlow("")
    val scaleReceiptNumber: StateFlow<String> = _scaleReceiptNumber
    private val _loadedWeight = MutableStateFlow("")
    val loadedWeight: StateFlow<String> = _loadedWeight.asStateFlow()
    private val _cargoCount = MutableStateFlow(0)
    private val _clearInputFields = MutableStateFlow(false)
    val clearInputFields: StateFlow<Boolean> = _clearInputFields.asStateFlow()
    private val _initialInfo = MutableStateFlow<InitialInfo?>(null)
    val initialInfo: StateFlow<InitialInfo?> = _initialInfo.asStateFlow()
    private val _cargoWeight = MutableStateFlow("")
    val cargoWeight: StateFlow<String> = _cargoWeight.asStateFlow()
    private val _totalNetWeight = MutableStateFlow("")
    val totalNetWeight: StateFlow<String> = _totalNetWeight.asStateFlow()
    private val _remainingWeight = MutableStateFlow("")
    val remainingWeight: StateFlow<String> = _remainingWeight.asStateFlow()
    private val _averageNetWeight = MutableStateFlow("")
    val averageNetWeight: StateFlow<String> = _averageNetWeight.asStateFlow()
    private val _remainingServices = MutableStateFlow("")
    val remainingServices: StateFlow<String> = _remainingServices.asStateFlow()
    private val _totalServices = MutableStateFlow("")
    private val _resultMessage = MutableStateFlow("")
    val resultMessage: StateFlow<String> = _resultMessage.asStateFlow()
    private val _showAnimatedMessage = MutableStateFlow(false)
    val showAnimatedMessage: StateFlow<Boolean> = _showAnimatedMessage.asStateFlow()
    private val _messageType = MutableStateFlow(MessageType.SUCCESS)
    val messageType: StateFlow<MessageType> = _messageType.asStateFlow()
    private val _showNetWeightDialog = MutableStateFlow(false)
    val showNetWeightDialog: StateFlow<Boolean> = _showNetWeightDialog.asStateFlow()

    // متغیر برای نشان دادن وضعیت ثبت حواله
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    // متغیرهای مربوط به دیالوگ تأیید حواله تکراری
    private val _showDuplicateConfirmationDialog = MutableStateFlow(false)
    val showDuplicateConfirmationDialog: StateFlow<Boolean> = _showDuplicateConfirmationDialog.asStateFlow()
    private val _duplicateWarningMessage = MutableStateFlow("")
    val duplicateWarningMessage: StateFlow<String> = _duplicateWarningMessage.asStateFlow()
    private val _pendingCargoInfo = MutableStateFlow<CargoInfo?>(null)
    private val _filteredCargoInfoList = MutableStateFlow<List<CargoInfo>>(emptyList())
    val filteredCargoInfoList: StateFlow<List<CargoInfo>> = _filteredCargoInfoList.asStateFlow()
    private val _isQuotaActive = MutableStateFlow<Boolean?>(null)
    private val _shownWarningForQuotas = mutableSetOf<String>()
    private val _pendingMessages = MutableStateFlow<Queue<Pair<String, MessageType>>>(LinkedList())
    private val _isShowingMessage = MutableStateFlow(false)
    private val _loadableTonnage = MutableStateFlow("")
    val loadableTonnage: StateFlow<String> = _loadableTonnage.asStateFlow()

    // اضافه کردن StateFlow برای تعداد ماشین‌های قابل بارگیری
    private val _loadableTrucks18Wheeler = MutableStateFlow("")
    val loadableTrucks18Wheeler: StateFlow<String> = _loadableTrucks18Wheeler.asStateFlow()

    private val _loadableTrucks10Wheeler = MutableStateFlow("")
    val loadableTrucks10Wheeler: StateFlow<String> = _loadableTrucks10Wheeler.asStateFlow()

    private val _cachedTrackingNumbers = MutableStateFlow<Set<String>>(emptySet())

    // StateFlow های مربوط به حواله‌های تکراری
    private val _duplicateTrackingNumbers = MutableStateFlow<List<String>>(emptyList())
    val duplicateTrackingNumbers: StateFlow<List<String>> = _duplicateTrackingNumbers.asStateFlow()

    private val _showDuplicateDialog = MutableStateFlow(false)
    val showDuplicateDialog: StateFlow<Boolean> = _showDuplicateDialog.asStateFlow()

    // وضعیت کشتی‌های انتخاب شده
    private val _selectedShipNames = MutableStateFlow<Set<String>>(emptySet())
    val selectedShipNames: StateFlow<Set<String>> = _selectedShipNames.asStateFlow()

    // به‌روزرسانی کشتی‌های انتخاب شده
    fun updateSelectedShips(ships: Set<String>) {
        _selectedShipNames.value = ships
    }

    init {
        viewModelScope.launch {
            _pendingMessages.collect { messageQueue ->
                if (!_isShowingMessage.value && messageQueue.isNotEmpty()) {
                    showNextMessage()
                }
            }
        }
    }

    private fun addMessageToQueue(message: String, type: MessageType) {
        val currentQueue = _pendingMessages.value
        currentQueue.offer(message to type)
        _pendingMessages.value = currentQueue
    }

    private fun showNextMessage() {
        val messageQueue = _pendingMessages.value
        if (messageQueue.isNotEmpty()) {
            val (message, type) = messageQueue.poll()!!
            _resultMessage.value = message
            _showAnimatedMessage.value = true
            _messageType.value = type
            _isShowingMessage.value = true
        }
    }

    fun dismissMessage() {
        _showAnimatedMessage.value = false
        _resultMessage.value = ""
        _isShowingMessage.value = false
        if (_pendingMessages.value.isNotEmpty()) {
            showNextMessage()
        }
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
                Log.d("CargoViewModel", "Checking quota existence: $quotaNumber for ship: $shipName")
                val response = apiService.checkQuotaExistenceCargo(quotaNumber = quotaNumber, shipName = shipName)
                if (response.isSuccessful) {
                    val quotaResponse = response.body() ?: throw Exception("پاسخ خالی از سرور")

                    // بررسی وضعیت هر کوتاژ براساس ترکیب تمام فیلدها
                    val updatedQuotas = quotaResponse.matchingQuotas.map { quota ->
                        val quotaStatus = checkDetailedQuotaStatus(
                            quotaNumber = quota.quotaNumber,
                            shipName = shipName,
                            shippingCompany = quota.shippingCompany,
                            cargoType = quota.cargoType
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
        cargoType: String
    ): QuotaStatusResponse {
        return try {
            val response = apiService.checkQuotaStatus(
                quotaNumber = quotaNumber,
                shipName = shipName,
                cargoType = cargoType,
                shippingCompany = shippingCompany
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

    fun updateCargoConfirmation(trackingNumber: String) {
        _cargoInfoList.value = _cargoInfoList.value.map { cargoInfo ->
            if (cargoInfo.trackingNumber == trackingNumber) {
                cargoInfo.copy(
                    confirm = "تائید شده",
                    // Only update exitTime and exitDate if they're null
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
        _resultMessage.value = message
        _showAnimatedMessage.value = true
        _messageType.value = type
    }

    // کش برای نتایج API با زمان انقضا
    private var lastQuotaStatusCheck: Long = 0
    private var lastLoadableTonnageUpdate: Long = 0
    private var cachedQuotaStatus: Boolean? = null
    private var cachedLoadableTonnage: String? = null
    private val quotaStatusCacheTimeout = 15_000L // 15 ثانیه
    private val loadableTonnageCacheTimeout = 30_000L // 30 ثانیه

    /**
     * پاک کردن کش APIها برای اطمینان از دریافت آخرین اطلاعات
     * این تابع زمانی استفاده می‌شود که تغییری در وضعیت حواله‌ها رخ داده است
     */
    private fun clearApiCache() {
        cachedQuotaStatus = null
        cachedLoadableTonnage = null
        lastQuotaStatusCheck = 0
        lastLoadableTonnageUpdate = 0
    }

    private fun checkForDuplicateTrackingNumbers(cargoList: List<CargoInfo>): List<String> {
        val trackingNumberCounts = mutableMapOf<String, Int>()

        // شمارش تعداد تکرار هر شماره حواله
        cargoList.forEach { cargo ->
            val trackingNumber = cargo.trackingNumber.trim()
            trackingNumberCounts[trackingNumber] = trackingNumberCounts.getOrDefault(trackingNumber, 0) + 1
        }

        // استخراج شماره حواله‌هایی که بیش از یک بار تکرار شده‌اند
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
                    // اطمینان از اعتبار اطلاعات جاری
                    if (info.loadingQuotaNumber.toString().isBlank() || info.shippingCompany.isBlank() ||
                        info.loadingWarehouse.isBlank() || info.cargoType.isBlank()) {
                        Log.e("CargoViewModel", "Invalid initial info for refresh: $info")
                        return@launch
                    }

                    // بررسی وضعیت کوتاژ با کش (فقط در صورت نیاز)
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastQuotaStatusCheck > quotaStatusCacheTimeout || cachedQuotaStatus == null) {
                        checkQuotaStatus(info)
                        lastQuotaStatusCheck = currentTime
                    }

                    // لاگ قبل از بروزرسانی
                    Log.d("CargoViewModel", "قبل از بروزرسانی - حواله‌های خروج: ${_cargoInfoList.value.count { it.status == "خروج" }}")

                    // نگهداری آخرین لیست برای مقایسه
                    val oldCargoList = _cargoInfoList.value

                    loadCargoInfoList(
                        quotaNumber = info.loadingQuotaNumber.toString(),
                        shippingCompany = info.shippingCompany,
                        warehouse = info.loadingWarehouse,
                        cargoType = info.cargoType,
                        onComplete = {
                            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

                            // بررسی تغییرات لیست
                            val newCargoList = _cargoInfoList.value
                            val hasStatusChanges = oldCargoList.any { oldCargo ->
                                val newCargo = newCargoList.find { it.trackingNumber == oldCargo.trackingNumber }
                                newCargo != null && oldCargo.status != newCargo.status
                            }

                            // نمایش پیام مناسب
                            if (hasStatusChanges) {
                                showUpdateMessage(
                                    "وضعیت حواله‌ها به‌روزرسانی شد"
                                )
                                // فقط در صورت تغییر وضعیت، تناژ قابل بارگیری را بروزرسانی کن
                                updateLoadableTonnageIfNeeded()
                            } else {
                                showUpdateMessage(
                                    "اطلاعات در ساعت $currentTime به‌روزرسانی شد"
                                )
                            }
                        }
                    )
                } catch (e: Exception) {
                    Log.e("CargoViewModel", "Error in refreshCargoInfo: ${e.message}", e)
                    _resultMessage.value = "خطا در به‌روزرسانی اطلاعات: ${e.message}"
                    _showAnimatedMessage.value = true
                    _messageType.value = MessageType.ERROR
                }
            } ?: run {
                Log.e("CargoViewModel", "Attempted to refresh with null or empty initial info")
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
                // بررسی وضعیت ارسال فعلی برای جلوگیری از درخواست‌های تکراری
                if (_isSubmitting.value) {
                    return@launch
                }

                // تنظیم وضعیت ثبت به true
                _isSubmitting.value = true

                // اعتبارسنجی سریع اولیه برای جلوگیری از ارسال‌های غیرضروری به سرور
                if (trackingNumber.isBlank()) {
                    showErrorMessage("شماره حواله نمی‌تواند خالی باشد.")
                    return@launch
                }

                // بررسی اطلاعات اولیه
                val initialInfo = _initialInfo.value ?: run {
                    showErrorMessage("اطلاعات اولیه در دسترس نیست")
                    return@launch
                }

                // بررسی کش وضعیت کوتاژ (فقط در صورت نیاز)
                val currentTime = System.currentTimeMillis()
                val quotaActive = cachedQuotaStatus

                if (currentTime - lastQuotaStatusCheck > quotaStatusCacheTimeout || quotaActive == null) {
                    // بررسی وضعیت کوتاژ (درصد و فعال بودن)
                    checkAndHandleQuotaPercentage(initialInfo.loadingQuotaNumber.toString())
                    checkQuotaStatus(initialInfo)
                    cachedQuotaStatus = _isQuotaActive.value
                    lastQuotaStatusCheck = currentTime
                } else {
                    _isQuotaActive.value = quotaActive
                    Log.d("CargoViewModel", "Using cached quota status: $quotaActive")
                }

                if (_isQuotaActive.value != true) {
                    val message = if (_messageType.value == MessageType.WARNING) {
                        "امکان ثبت حواله برای این کوتاژ وجود ندارد. لطفاً وضعیت کوتاژ را بررسی کنید."
                    } else {
                        "کوتاژ غیرفعال است و امکان ثبت حواله جدید وجود ندارد"
                    }
                    showErrorMessage(message)
                    return@launch
                }

                // بررسی تناژ موقت - اگر فعال است و مقدار آن صفر یا منفی است، امکان ثبت حواله جدید یا خروج وجود ندارد
                if (initialInfo.tempTonnageStatus && initialInfo.tempTonnageAmount != null) {
                    if (initialInfo.tempTonnageAmount <= 0) {
                        showErrorMessage("تناژ موقت به پایان رسیده است. امکان ثبت حواله جدید یا خروج وجود ندارد. لطفاً با مسئول خود بررسی کنید.")
                        return@launch
                    }
                }

                // بررسی تکراری نبودن حواله
                val isNewCargo = !isTrackingNumberDuplicate(trackingNumber)

                // بررسی جامع اعتبار داده‌های ورودی
                if (!validateInputData(trackingNumber, netWeight, numberOfPeople, shortageWeight, excessWeight, isNewCargo, scaleReceiptNumber)) {
                    return@launch
                }

                // دریافت اطلاعات کاربری
                val username = userPreferencesManager.username.first()
                val userType = userPreferencesManager.userType.first()

                if (username.isBlank() || userType.isBlank()) {
                    showErrorMessage("اطلاعات کاربری در دسترس نیست. لطفاً دوباره وارد شوید.")
                    return@launch
                }

                // آماده‌سازی مدل داده برای ارسال
                val cargoInfo = prepareCargoInfoForSubmission(
                    trackingNumber, numberOfPeople, username, userType,
                    netWeight, scaleReceiptNumber, shortageWeight, excessWeight,
                    initialInfo
                )

                // ارسال اطلاعات به سرور با مدیریت خطا
                sendCargoInfoToServer(
                    cargoInfo, trackingNumber, netWeight,
                    scaleReceiptNumber, shortageWeight, excessWeight
                )
            } catch (e: Exception) {
                Log.e("CargoViewModel", "خطا در submitCargoInfo: ${e.message}", e)
                showErrorMessage("خطا در ثبت اطلاعات بار: ${e.message}")
            } finally {
                // تنظیم وضعیت ثبت به false
                _isSubmitting.value = false
            }
        }
    }

    private fun showErrorMessage(message: String) {
        _resultMessage.value = message
        _showAnimatedMessage.value = true
        _messageType.value = MessageType.ERROR
    }

    private fun validateInputData(
        trackingNumber: String,
        netWeight: String,
        numberOfPeople: String,
        shortageWeight: String,
        excessWeight: String,
        isNewCargo: Boolean,
        scaleReceiptNumber: String
    ): Boolean {
        // بررسی شماره حواله
        if (trackingNumber.isBlank()) {
            showErrorMessage("شماره حواله نمی‌تواند خالی باشد.")
            return false
        }

        // بررسی تعداد نفرات برای حواله‌های جدید
        if (isNewCargo) {
            val peopleCount = numberOfPeople.toIntOrNull()
            if (peopleCount == null || peopleCount < 1) {
                showErrorMessage("تعداد نفرات باید عددی بزرگتر از صفر باشد.")
                return false
            }
        }

        // بررسی وزن خالص برای حواله‌های خروجی
        if (netWeight.isNotBlank()) {
            val weight = netWeight.toFloatOrNull()
            if (weight == null || weight <= 0) {
                showErrorMessage("وزن خالص باید عددی مثبت باشد.")
                return false
            }

            if (weight < 5000 || weight > 45000) {
                showErrorMessage("وزن خالص باید بین 5000 تا 45000 کیلوگرم باشد.")
                return false
            }

            // بررسی شماره قبض باسکول
            if (scaleReceiptNumber.isBlank()) {
                showErrorMessage("برای ثبت خروج، شماره قبض باسکول الزامی است.")
                return false
            }

            if (scaleReceiptNumber.length < 8 || scaleReceiptNumber.length > 10) {
                showErrorMessage("شماره قبض باسکول باید بین 8 تا 10 رقم باشد.")
                return false
            }
        }

        // بررسی کسری و اضافه بار
        if (shortageWeight.isNotBlank()) {
            val shortage = shortageWeight.toFloatOrNull()
            if (shortage == null || shortage < 0) {
                showErrorMessage("کسری بار باید عددی مثبت یا صفر باشد.")
                return false
            }
        }

        if (excessWeight.isNotBlank()) {
            val excess = excessWeight.toFloatOrNull()
            if (excess == null || excess < 0) {
                showErrorMessage("اضافه بار باید عددی مثبت یا صفر باشد.")
                return false
            }
        }

        return true
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

        return CargoInfo(
            trackingNumber = trackingNumber,
            numberOfPeople = numberOfPeople,
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
                        _resultMessage.value = responseBody.message
                        _showAnimatedMessage.value = true
                        _messageType.value = MessageType.WARNING
                    }
                    else -> {
                        handleSuccessResponse(responseBody, trackingNumber, netWeight, scaleReceiptNumber, shortageWeight, excessWeight)
                        // فقط در صورت موفقیت، کش را پاک کنیم
                        clearApiCache()
                    }
                }
            } else {
                // ذخیره CargoInfo برای استفاده در دیالوگ تأیید
                _pendingCargoInfo.value = cargoInfo
                handleErrorHttpResponse(response)
            }
        } catch (e: Exception) {
            Log.e("CargoViewModel", "خطا در ارسال به سرور: ${e.message}", e)
            showErrorMessage("خطا در ارتباط با سرور: ${e.message}")
        }
    }

    private fun handleErrorHttpResponse(response: Response<SaveOrUpdateResponse>) {
        val errorBody = response.errorBody()?.string()
        val errorCode = response.code()

        try {
            val parsedError = parseErrorResponse(errorBody)
            if (parsedError != null) {
                // بررسی اینکه آیا این یک هشدار 24 ساعته است
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
            // نمایش دیالوگ تأیید برای حواله تکراری
            _duplicateWarningMessage.value = responseBody.message
            _showDuplicateConfirmationDialog.value = true
        } else {
            // نمایش پیام هشدار معمولی
            _resultMessage.value = responseBody.message
            _showAnimatedMessage.value = true
            _messageType.value = MessageType.WARNING
        }
    }

    private fun handleErrorResponse(responseBody: SaveOrUpdateResponse) {
        _resultMessage.value = when (responseBody.status) {
            "duplicate_voucher" -> {
                "حواله مورد نظر برای کشتی ${responseBody.shipName ?: ""} در شماره کوتاژ ${responseBody.loadingQuotaNumber ?: ""} قبلا ثبت شده است!"
            }
            else -> responseBody.message
        }
        _showAnimatedMessage.value = true
        _messageType.value = MessageType.ERROR
    }

    private fun handleSuccessResponse(
        responseBody: SaveOrUpdateResponse?,
        trackingNumber: String,
        netWeight: String,
        scaleReceiptNumber: String,
        shortageWeight: String,
        excessWeight: String
    ) {
        _resultMessage.value = when {
            netWeight.isNotBlank() -> {
                "شماره حواله $trackingNumber با شماره قبض باسکول $scaleReceiptNumber در تاریخ ${responseBody?.exitDate ?: "نامشخص"} و ساعت ${responseBody?.exitTime ?: "نامشخص"} و وزن خالص $netWeight خروج آن ثبت و سرویس آن بسته شد."
            }
            shortageWeight.isNotBlank() -> "حواله [$trackingNumber] با [$shortageWeight] کیلوگرم کسری بار ثبت شد!"
            excessWeight.isNotBlank() -> "حواله [$trackingNumber] با [$excessWeight] کیلوگرم اضافه بار ثبت شد!"
            else -> responseBody?.message ?: "حواله جدید با شماره [$trackingNumber] ثبت شد."
        }
        _showAnimatedMessage.value = true
        _messageType.value = MessageType.SUCCESS
        _clearInputFields.value = true

        // بروزرسانی فوری وضعیت حواله در لیست محلی
        if (netWeight.isNotBlank()) {
            updateLocalCargoListForExit(trackingNumber, netWeight, responseBody)
        }

        refreshCargoInfo()
    }

    private fun updateLocalCargoListForExit(trackingNumber: String, netWeight: String, responseBody: SaveOrUpdateResponse?) {
        // این متد وضعیت حواله را بلافاصله در لیست محلی به‌روز می‌کند
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

        // بروزرسانی فیلتر شده هم برای نمایش صحیح در دسته‌بندی‌ها
        _filteredCargoInfoList.value = updatedList

        // لاگ برای دیباگ
        Log.d("CargoViewModel", "حواله با شماره $trackingNumber به وضعیت خروج تغییر یافت")
        Log.d("CargoViewModel", "تعداد کل حواله‌ها: ${updatedList.size}, تعداد حواله‌های خروج: ${updatedList.count { it.status == "خروج" }}")
    }

    private fun parseErrorResponse(errorBody: String?): SaveOrUpdateResponse? {
        return try {
            Gson().fromJson(errorBody, SaveOrUpdateResponse::class.java)
        } catch (_: Exception) {
            null
        }
    }

    // توابع مدیریت دیالوگ تأیید حواله تکراری
    fun dismissDuplicateConfirmationDialog() {
        _showDuplicateConfirmationDialog.value = false
        _duplicateWarningMessage.value = ""
        _pendingCargoInfo.value = null
    }

    fun confirmDuplicateCargoRegistration() {
        val cargoInfo = _pendingCargoInfo.value
        if (cargoInfo != null) {
            // ارسال مجدد با تأیید کاربر
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
                    Log.e("CargoViewModel", "خطا در ارسال مجدد حواله: ${e.message}", e)
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

    private suspend fun checkQuotaStatus(initialInfo: InitialInfo) {
        try {
            val status = repository.checkQuotaStatus(
                quotaNumber = initialInfo.loadingQuotaNumber.toString(),
                shipName = initialInfo.shipName,
                cargoType = initialInfo.cargoType,
                shippingCompany = initialInfo.shippingCompany
            )

            // کش کردن نتیجه
            cachedQuotaStatus = status.isActive
            _isQuotaActive.value = status.isActive

            if (status.isActive) {
                // اگر کوتاژ فعال است، بررسی وضعیت درصد
                checkAndHandleQuotaPercentage(initialInfo.loadingQuotaNumber.toString())
            } else {
                // پیام غیرفعال بودن بعد از هشدار درصدی نمایش داده می‌شود
                delay(5000) // تاخیر بیشتر از هشدار درصدی
                showMessage(status.message, MessageType.WARNING)
            }
        } catch (e: Exception) {
            Log.e("CargoViewModel", "Error in checkQuotaStatus", e)
            _resultMessage.value = "خطا در بررسی وضعیت کوتاژ: ${e.message ?: "خطای ناشناخته"}"
            _messageType.value = MessageType.ERROR
            _showAnimatedMessage.value = true
            _isQuotaActive.value = false
            cachedQuotaStatus = false
        }
    }

    fun resetClearInputFields() {
        _clearInputFields.value = false
    }

    private fun isValidScaleReceipt(scaleReceipt: String): Boolean {
        // بررسی عددی بودن
        if (!scaleReceipt.all { it.isDigit() }) {
            return false
        }

        // بررسی طول (8 رقمی بودن)
        if (scaleReceipt.length != 8) {
            return false
        }

        // بررسی دو رقم اول
        val firstTwoDigits = scaleReceipt.substring(0, 2)
        return !(firstTwoDigits != "43" && firstTwoDigits != "44" && firstTwoDigits != "45" && firstTwoDigits != "46")
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
                    // استفاده از API جدید برای دریافت فوری تناژ قابل بارگیری
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
                                    // نمایش مقدار تناژ قابل بارگیری حتی اگر منفی باشد
                                    val formattedValue = if (tonnage < 0) {
                                        "-" + DecimalFormat("#,###").format(abs(tonnage.roundToInt()))
                                    } else {
                                        DecimalFormat("#,###").format(tonnage.roundToInt())
                                    }
                                    _loadableTonnage.value = formattedValue
                                }

                                // بروزرسانی تعداد کامیون‌ها از مقادیر محاسبه‌شده در سرور
                                data.trucks18Wheeler?.let { count ->
                                    _loadableTrucks18Wheeler.value = count.toString()
                                }

                                data.trucks10Wheeler?.let { count ->
                                    _loadableTrucks10Wheeler.value = count.toString()
                                }

                                Log.d("CargoViewModel", "Initial loadable tonnage updated via API: ${_loadableTonnage.value}")
                            }
                        } else {
                            // در صورت خطا، فقط لاگ می‌کنیم و از محاسبه محلی خودداری می‌کنیم
                            Log.e("CargoViewModel", "Error in API call for initial loadable tonnage")
                        }
                    } catch (e: Exception) {
                        Log.e("CargoViewModel", "Error in API call for loadable tonnage", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("CargoViewModel", "Error in pre-loading tonnage data", e)
            }
        }

        // سپس بقیه اطلاعات را بارگذاری می‌کنیم
        viewModelScope.launch {
            try {
                // دریافت اطلاعات اصلی در Dispatchers.IO
                val result = withContext(Dispatchers.IO) {
                    repository.getCargoInfo(quotaNumber, shippingCompany, warehouse, cargoType)
                }

                // بررسی حواله‌های تکراری قبل از بروزرسانی UI
                val duplicateTrackingNumbers = checkForDuplicateTrackingNumbers(result.cargoInfoList)

                if (duplicateTrackingNumbers.isNotEmpty()) {
                    // نمایش پیام هشدار برای حواله‌های تکراری
                    withContext(Dispatchers.Main.immediate) {
                        _duplicateTrackingNumbers.value = duplicateTrackingNumbers
                        _showDuplicateDialog.value = true
                        Log.w("CargoViewModel", "حواله‌های تکراری شناسایی شدند: ${duplicateTrackingNumbers.joinToString(", ")}")
                    }
                }

                // نمایش تمام حواله‌ها (شامل تکراری‌ها) در لیست
                _cargoInfoList.value = result.cargoInfoList
                _initialInfo.value = result.initialInfo
                _filteredCargoInfoList.value = result.cargoInfoList

                // لاگ برای دیباگ بعد از بارگذاری
                Log.d("CargoViewModel", "بارگذاری داده‌ها - تعداد کل: ${result.cargoInfoList.size}, حواله‌های خروج: ${result.cargoInfoList.count { it.status == "خروج" }}")

                // ذخیره شماره‌های حواله در کش
                val allTrackingNumbers = result.allTrackingNumbers?.toSet() ?: emptySet()
                _cachedTrackingNumbers.value = allTrackingNumbers

                // به‌روزرسانی مقادیر اولیه
                withContext(Dispatchers.Main.immediate) {
                    _cargoWeight.value = result.initialInfo.cargoWeight.toString()
                    _totalNetWeight.value = result.initialInfo.totalNetWeight.toString()
                    _remainingWeight.value = result.initialInfo.remainingWeight.toString()
                    _averageNetWeight.value = result.initialInfo.averageNetWeight.toString()
                    _remainingServices.value = result.initialInfo.remainingServices.toString()
                    _totalServices.value = result.initialInfo.totalVoucherCount.toString()
                }

                // بعد از دریافت اطلاعات اولیه، درخواست محاسبه تناژ قابل بارگیری را به سرور ارسال می‌کنیم
                val quotaNumber = result.initialInfo.loadingQuotaNumber.toString()
                val shippingCompany = result.initialInfo.shippingCompany
                val warehouse = result.initialInfo.loadingWarehouse
                val cargoType = result.initialInfo.cargoType

                // استفاده از CoroutineScope جدید برای اجرای با اولویت بالا
                CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
                    try {
                        val response = withContext(Dispatchers.IO) {
                            apiService.getLoadableTonnage(
                                quotaNumber = quotaNumber,
                                shippingCompany = shippingCompany,
                                warehouse = warehouse,
                                cargoType = cargoType
                            )
                        }

                        if (response.isSuccessful && response.body()?.success == true) {
                            val data = response.body()!!
                            withContext(Dispatchers.Main.immediate) {
                                data.loadableTonnage?.let { tonnage ->
                                    _loadableTonnage.value = DecimalFormat("#,###").format(tonnage.roundToInt())
                                }

                                // استفاده از مقادیر محاسبه‌شده در سمت سرور
                                data.trucks18Wheeler?.let { count ->
                                    _loadableTrucks18Wheeler.value = count.toString()
                                }

                                data.trucks10Wheeler?.let { count ->
                                    _loadableTrucks10Wheeler.value = count.toString()
                                }
                            }
                        } else {
                            Log.e("CargoViewModel", "Error in API call for loadable tonnage during initial load")
                        }
                    } catch (e: Exception) {
                        Log.e("CargoViewModel", "Error calculating loadable tonnage", e)
                    }
                }

                // بررسی وضعیت کوتاژها با اولویت پایین بعد از بروزرسانی تناژ
                launch(Dispatchers.IO) {
                    try {
                        // تاخیر اندک برای اطمینان از اینکه UI ابتدا بروزرسانی شود
                        delay(100)

                        val currentQuotas = _cargoInfoList.value
                        currentQuotas.forEach { cargoInfo ->
                            val quota = repository.getShipQuotas(cargoInfo.shipName)
                                .find { it.number == cargoInfo.loadingQuotaNumber }

                            quota?.let {
                                checkQuotaPercentage(it)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("CargoViewModel", "Error checking quota percentages", e)
                    }
                }

                // به‌روزرسانی مقادیر نهایی
                updateInfoValues()

                // اعلام اتمام بارگذاری
                onComplete()
            } catch (e: Exception) {
                Log.e("CargoViewModel", "Error loading cargo info", e)
                showMessage("خطا در ارتباط با سرور: ${e.localizedMessage}", MessageType.ERROR)
                onComplete()
            }
        }
    }

    private fun isTrackingNumberDuplicate(trackingNumber: String): Boolean {
        if (_cachedTrackingNumbers.value.contains(trackingNumber)) {
            return true
        }

        return _cargoInfoList.value.any { it.trackingNumber == trackingNumber }
    }

    suspend fun toggleQuotaStatus(quotaNumber: String) {
        try {
            val response = apiService.toggleQuotaStatus(
                action = "toggleQuotaStatus",
                quotaNumber = quotaNumber
            )
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody?.success == true) {
                    // پاک کردن کش برای اطمینان از دریافت آخرین وضعیت
                    clearApiCache()

                    // استفاده از refreshCargoInfo که شامل بهینه‌سازی‌های کش است
                    refreshCargoInfo()
                } else {
                    addMessageToQueue(
                        "خطا در تغییر وضعیت کوتاژ: ${responseBody?.message}",
                        MessageType.ERROR
                    )
                }
            } else {
                val errorBody = response.errorBody()?.string()
                addMessageToQueue(
                    "خطا در تغییر وضعیت کوتاژ: $errorBody",
                    MessageType.ERROR
                )
            }
        } catch (e: Exception) {
            addMessageToQueue(
                "خطا در ارتباط با سرور: ${e.message}",
                MessageType.ERROR
            )
        }
    }

    private fun checkQuotaPercentage(quota: Quota) {
        if (quota.isPercentageRestricted == true && quota.percentage != null) {
            if (!_shownWarningForQuotas.contains(quota.number)) {
                val percentageAmount = quota.totalTonnage * (quota.percentage / 100)
                val remainingTonnage = quota.remainingTonnage

                if (remainingTonnage <= percentageAmount) {
                    viewModelScope.launch {
                        _shownWarningForQuotas.add(quota.number)

                        // اول نمایش هشدار درصدی
                        addMessageToQueue(
                            "کوتاژ ${quota.number} به حد نصاب ${quota.percentage}% رسیده است و غیرفعال خواهد شد",
                            MessageType.WARNING
                        )

                        // تاخیر کوتاه قبل از غیرفعال کردن
                        delay(3000)
                        toggleQuotaStatus(quota.number)
                    }
                }
            }
        }
    }

    private suspend fun checkAndHandleQuotaPercentage(quotaNumber: String) {
        try {
            val response = apiService.getShipQuotas(shipName = _initialInfo.value?.shipName ?: "")
            if (response.isSuccessful) {
                val quotas = response.body()
                quotas?.find { it.number == quotaNumber }?.let { quota ->
                    if (quota.isPercentageRestricted == true && quota.percentage != null) {
                        if (!_shownWarningForQuotas.contains(quota.number)) {
                            val percentageAmount = quota.totalTonnage * (quota.percentage / 100)
                            val remainingTonnage = quota.remainingTonnage

                            if (remainingTonnage <= percentageAmount) {
                                _shownWarningForQuotas.add(quota.number)

                                // اول نمایش هشدار درصدی
                                _resultMessage.value = "کوتاژ ${quota.number} به حد نصاب ${quota.percentage}% رسیده است و غیرفعال خواهد شد"
                                _showAnimatedMessage.value = true
                                _messageType.value = MessageType.WARNING

                                // تاخیر کوتاه قبل از غیرفعال کردن
                                delay(3000)

                                toggleQuotaStatus(quotaNumber)
                                throw Exception("امکان ثبت حواله جدید وجود ندارد")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            throw e
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
                    // بروزرسانی مستقیم در لیست محلی
                    val updatedList = _cargoInfoList.value.map { cargo ->
                        if (cargo.trackingNumber == cargoInfo.trackingNumber) {
                            updatedCargoInfo
                        } else {
                            cargo
                        }
                    }

                    // بروزرسانی هر دو لیست برای نمایش صحیح
                    _cargoInfoList.value = updatedList
                    _filteredCargoInfoList.value = updatedList


                    // پاک کردن کش و فراخوانی refreshCargoInfo فقط در صورت نیاز
                    clearApiCache()

                    refreshCargoInfo()

                    _resultMessage.value = "اطلاعات بروزرسانی شد"
                    _showAnimatedMessage.value = true
                    _messageType.value = MessageType.SUCCESS
                } else {
                    _resultMessage.value = "خطا در به روز رسانی اطلاعات بار"
                    _showAnimatedMessage.value = true
                    _messageType.value = MessageType.ERROR
                }
            } catch (e: Exception) {
                _resultMessage.value = "خطا در به روز رسانی اطلاعات بار: ${e.message}"
                _showAnimatedMessage.value = true
                _messageType.value = MessageType.ERROR
            }
        }
    }

    fun updateInfoValues() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                // محاسبه وزن خالص کل و تعداد حواله‌های خارج شده
                val exitedCargos = _cargoInfoList.value.filter { it.status == "خروج" }
                val netWeights = exitedCargos.mapNotNull { it.netWeight.toFloatOrNull() }
                val totalNet = netWeights.sum()

                // محاسبه میانگین وزن خالص
                val averageNet = if (netWeights.isNotEmpty()) netWeights.average() else 0.0

                // محاسبه وزن باقیمانده
                val cargoWeightValue = _cargoWeight.value.replace(",", "").toFloatOrNull() ?: 0f
                val remaining = (cargoWeightValue - totalNet).coerceAtLeast(0f)

                // محاسبه تعداد حواله‌های باقیمانده
                val remainingServicesCount = if (averageNet > 0) (remaining / averageNet).toInt() else 0

                // به‌روزرسانی مقادیر در StateFlow‌ها (انتقال به نخ اصلی)
                withContext(Dispatchers.Main) {
                    _remainingWeight.value = DecimalFormat("#,###").format(remaining.roundToInt())
                    _loadedWeight.value = DecimalFormat("#,###").format(totalNet.roundToInt())
                    _totalNetWeight.value = DecimalFormat("#,###").format(totalNet.roundToInt())
                    _averageNetWeight.value = DecimalFormat("#,###").format(averageNet.roundToInt())
                    _remainingServices.value = remainingServicesCount.toString()
                    _totalServices.value = _cargoCount.value.toString()

                    // بررسی و به‌روزرسانی تعداد ماشین‌های قابل بارگیری
                    if (_loadableTrucks18Wheeler.value.isBlank() || _loadableTrucks10Wheeler.value.isBlank()) {
                        val loadableTonnageValue = _loadableTonnage.value.replace(",", "").toDoubleOrNull() ?: 0.0
                        updateLoadableTrucksCount(loadableTonnageValue)
                    }
                }
            } catch (e: Exception) {
                Log.e("CargoViewModel", "Error updating info values", e)
            }
        }
    }

    fun deleteCargo(cargoInfoRequest: CargoInfoRequest, password: String) {
        viewModelScope.launch {
            try {
                // بررسی صحت رمز عبور در رشته IO
                val passwordResponse = withContext(Dispatchers.IO) {
                    apiService.checkPassword(password, "delete_info")
                }
                if (passwordResponse.isSuccessful && passwordResponse.body()?.success == true) {
                    // حذف حواله در رشته IO
                    val deleteResponse = withContext(Dispatchers.IO) {
                        apiService.deleteCargo(cargoInfoRequest)
                    }
                    if (deleteResponse.isSuccessful) {
                        _resultMessage.value = "حواله با موفقیت حذف شد."
                        _showAnimatedMessage.value = true
                        _messageType.value = MessageType.SUCCESS

                        // پاک کردن کش برای اطمینان از دریافت آخرین اطلاعات
                        clearApiCache()

                        // استفاده از refreshCargoInfo که شامل تمام بهینه‌سازی‌ها است
                        refreshCargoInfo()
                    } else {
                        _resultMessage.value = "خطا در حذف حواله: ${deleteResponse.errorBody()?.string()}"
                        _showAnimatedMessage.value = true
                        _messageType.value = MessageType.ERROR
                    }
                } else {
                    _resultMessage.value = passwordResponse.body()?.message ?: "خطا در بررسی رمز عبور"
                    _showAnimatedMessage.value = true
                    _messageType.value = MessageType.ERROR
                }
            } catch (e: Exception) {
                _resultMessage.value = "استثنا در حذف حواله: ${e.message}"
                _showAnimatedMessage.value = true
                _messageType.value = MessageType.ERROR
            }
        }
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm", Locale.US).format(System.currentTimeMillis())
    }

    private fun getCurrentDate(): String {
        return gregorianToJalali(Calendar.getInstance())
    }

    // بروزرسانی هوشمند تناژ قابل بارگیری با کش و کنترل زمان
    private fun updateLoadableTonnageIfNeeded(forceUpdate: Boolean = false) {
        val currentTime = System.currentTimeMillis()

        // بررسی نیاز به بروزرسانی بر اساس کش
        if (!forceUpdate && currentTime - lastLoadableTonnageUpdate < loadableTonnageCacheTimeout && cachedLoadableTonnage != null) {
            Log.d("CargoViewModel", "Using cached loadable tonnage: $cachedLoadableTonnage")
            return
        }

        // استفاده از CoroutineScope جدید با اولویت بالا
        CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
            try {
                _initialInfo.value?.let { info ->
                    // استفاده از API مستقیم برای دریافت سریع تناژ قابل بارگیری
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

                        // استفاده از Main.immediate برای بروزرسانی فوری UI
                        withContext(Dispatchers.Main.immediate) {
                            data.loadableTonnage?.let { tonnage ->
                                val formattedTonnage = DecimalFormat("#,###").format(tonnage.roundToInt())
                                _loadableTonnage.value = formattedTonnage
                                cachedLoadableTonnage = formattedTonnage
                                lastLoadableTonnageUpdate = currentTime
                            }

                            // استفاده از مقادیر محاسبه‌شده در سمت سرور
                            data.trucks18Wheeler?.let { count ->
                                _loadableTrucks18Wheeler.value = count.toString()
                            }

                            data.trucks10Wheeler?.let { count ->
                                _loadableTrucks10Wheeler.value = count.toString()
                            }

                            Log.d("CargoViewModel", "Loadable tonnage updated via API: ${_loadableTonnage.value}")
                        }
                    } else {
                        // در صورت خطا، فقط لاگ می‌کنیم و از محاسبه سمت کلاینت خودداری می‌کنیم
                        Log.e("CargoViewModel", "Error in API call for loadable tonnage: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("CargoViewModel", "Error updating loadable tonnage", e)
            }
        }
    }

    private fun updateLoadableTrucksCount(loadableTonnage: Double) {
        // محاسبه تعداد ماشین‌های 18 چرخ (فقط برای مقادیر مثبت)
        val trucks18Wheeler = if (loadableTonnage > 0) (loadableTonnage / 25000.0).toInt() else 0

        // محاسبه تعداد ماشین‌های 10 چرخ (فقط برای مقادیر مثبت)
        val trucks10Wheeler = if (loadableTonnage > 0) (loadableTonnage / 15000.0).toInt() else 0

        // مقادیر را در StateFlow ها قرار می‌دهیم
        _loadableTrucks18Wheeler.value = trucks18Wheeler.toString()
        _loadableTrucks10Wheeler.value = trucks10Wheeler.toString()
    }

    fun setInitialInfo(initialInfo: InitialInfo) {
        _initialInfo.value = initialInfo
    }
}

class ReportsViewModel(
    private val repository: ReportsRepository,
    application: Application
) : AndroidViewModel(application) {
    private val _realTimeLoadingData = MutableStateFlow<List<RealTimeLoadingData>>(emptyList())
    val realTimeLoadingData: StateFlow<List<RealTimeLoadingData>> = _realTimeLoadingData
    private val _loadingError = MutableStateFlow<String?>(null)
    val loadingError: StateFlow<String?> = _loadingError
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState
    private val _ships = MutableStateFlow(ShipsData(emptyList(), emptyList()))
    val ships: StateFlow<ShipsData> = _ships
    private val _selectedShip = MutableStateFlow<Ship?>(null)
    val selectedShip: StateFlow<Ship?> = _selectedShip
    private val _selectedWarehouse = MutableStateFlow<Warehouse?>(null)
    val selectedWarehouse: StateFlow<Warehouse?> = _selectedWarehouse
    private val _selectedQuotaDetails = MutableStateFlow<QuotaDetails?>(null)
    val selectedQuotaDetails: StateFlow<QuotaDetails?> = _selectedQuotaDetails
    private val _selectedShipQuotas = MutableStateFlow<List<Quota>>(emptyList())
    val selectedShipQuotas: StateFlow<List<Quota>> = _selectedShipQuotas
    private val _filteredSummary = MutableStateFlow<FilteredSummary?>(null)
    val filteredSummary: StateFlow<FilteredSummary?> = _filteredSummary
    private val _shiftInfo = MutableStateFlow<ShiftInfo?>(null)
    val shiftInfo: StateFlow<ShiftInfo?> = _shiftInfo
    private val _currentShipName = MutableStateFlow<String?>(null)
    private val _shipColorMap = MutableStateFlow<Map<String, Color>>(emptyMap())
    val shipColorMap: StateFlow<Map<String, Color>> = _shipColorMap.asStateFlow()
    private val _quotaColorMap = MutableStateFlow<Map<String, Color>>(emptyMap())
    private val colorSelector = ColorSelector(cardColors)
    private val _snackbarMessages = MutableSharedFlow<String>()
    val snackbarMessages = _snackbarMessages.asSharedFlow()
    private fun showSnackbar(message: String) {
        viewModelScope.launch {
            _snackbarMessages.emit(message)
        }
    }
    private val _exportResult = MutableStateFlow<String?>(null)
    private val _comprehensiveAnalytics = MutableStateFlow<ComprehensiveAnalytics?>(null)
    val comprehensiveAnalytics: StateFlow<ComprehensiveAnalytics?> = _comprehensiveAnalytics.asStateFlow()
    private val _analyticsLoadingState = MutableStateFlow<LoadingState>(LoadingState.Idle)
    val analyticsLoadingState: StateFlow<LoadingState> = _analyticsLoadingState.asStateFlow()

    // اضافه کردن State های جدید
    private val _groupingMode = MutableStateFlow(QuotaGroupingMode.BY_SHIP)
    val groupingMode: StateFlow<QuotaGroupingMode> = _groupingMode

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filteredQuotas = MutableStateFlow<List<QuotaCompletionData>>(emptyList())
    val filteredQuotas: StateFlow<List<QuotaCompletionData>> = _filteredQuotas

    private val _warehouseQuotaGroupingMode = MutableStateFlow(WarehouseQuotaGroupingMode.BY_CARGO_OWNER)
    val warehouseQuotaGroupingMode: StateFlow<WarehouseQuotaGroupingMode> = _warehouseQuotaGroupingMode.asStateFlow()

    // متغیرهای مربوط به بازه زمانی انتخاب شده
    private val _selectedDateRange = MutableStateFlow<Pair<String, String>?>(null)
    val selectedDateRange: StateFlow<Pair<String, String>?> = _selectedDateRange.asStateFlow()

    // متغیرهای مربوط به مرتب‌سازی کوتاژها
    private val _quotaSortingMode = MutableStateFlow(QuotaSortingMode.REMAINING_TONNAGE_ASC)
    val quotaSortingMode: StateFlow<QuotaSortingMode> = _quotaSortingMode.asStateFlow()

    // متغیرهای مربوط به مرتب‌سازی گروه‌ها
    private val _groupSortingMode = MutableStateFlow(GroupSortingMode.REMAINING_TONNAGE_ASC)
    val groupSortingMode: StateFlow<GroupSortingMode> = _groupSortingMode.asStateFlow()

    // متغیرهای مربوط به مرتب‌سازی کشتی‌ها
    private val _shipSortingMode = MutableStateFlow(ShipSortingMode.REMAINING_TONNAGE_ASC)
    val shipSortingMode: StateFlow<ShipSortingMode> = _shipSortingMode.asStateFlow()


    // تابع تغییر حالت گروه‌بندی
    fun setGroupingMode(mode: QuotaGroupingMode) {
        _groupingMode.value = mode
        updateFilteredQuotas()
    }

    // تابع به‌روزرسانی کوئری جستجو
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        updateFilteredQuotas()
    }

    // تابع به‌روزرسانی لیست اولیه کوتاژها
    fun updateInitialQuotas(quotas: List<QuotaCompletionData>) {
        viewModelScope.launch {
            _initialQuotas.value = quotas  // ذخیره لیست اولیه
            updateFilteredQuotas()
        }
    }

    // تابع به‌روزرسانی لیست فیلتر شده
    private fun updateFilteredQuotas() {
        viewModelScope.launch {
            val query = _searchQuery.value.trim().lowercase()

            // فیلتر کردن بر اساس جستجو
            val filtered = if (query.isEmpty()) {
                _initialQuotas.value
            } else {
                _initialQuotas.value.filter { quota ->
                    quota.shipName.lowercase().contains(query) ||
                            quota.loadingQuotaNumber.contains(query) ||
                            quota.shippingCompany.lowercase().contains(query)
                }
            }

            // گروه‌بندی و مرتب‌سازی ترکیبی
            _filteredQuotas.value = when (_groupingMode.value) {
                QuotaGroupingMode.BY_SHIP -> {
                    filtered.groupBy { it.shipName }
                        .map { (shipName, quotas) ->
                            Triple(
                                shipName,
                                quotas.size,
                                quotas.sumOf { it.last_24h_weight.toDouble() }.toFloat()
                            )
                        }
                        .sortedWith(
                            compareByDescending<Triple<String, Int, Float>> { it.second }
                                .thenByDescending { it.third }
                        )
                        .flatMap { (shipName, _, _) ->
                            filtered.filter { it.shipName == shipName }
                        }
                }
                QuotaGroupingMode.BY_CARRIER -> {
                    filtered.groupBy { it.shippingCompany }
                        .map { (carrier, quotas) ->
                            Triple(
                                carrier,
                                quotas.size,
                                quotas.sumOf { it.last_24h_weight.toDouble() }.toFloat()
                            )
                        }
                        .sortedWith(
                            compareByDescending<Triple<String, Int, Float>> { it.second }
                                .thenByDescending { it.third }
                        )
                        .flatMap { (carrier, _, _) ->
                            filtered.filter { it.shippingCompany == carrier }
                        }
                }
            }
        }
    }

    private val _initialQuotas = MutableStateFlow<List<QuotaCompletionData>>(emptyList())

    init {
        loadShips()
    }

    fun formatNumber(number: Number): String {
        return NumberFormat.getNumberInstance(Locale("en", "US")).format(number)
    }

    fun setCurrentShipName(shipName: String) {
        _currentShipName.value = shipName
    }

    fun clearCurrentShipData() {
        _currentShipName.value = null
        _selectedShip.value = null
        _selectedShipQuotas.value = emptyList()
    }

    fun loadShips() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val shipsData = repository.getShipsList()
                _ships.value = shipsData
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error("خطا در بارگیری لیست کشتی‌ها: ${e.message}")
            }
        }
    }

    fun loadRealTimeData(isDarkTheme: Boolean, defaultColor: Color) {
        viewModelScope.launch {
            try {
                val response = repository.getRealTimeLoadingData()
                _realTimeLoadingData.value = response.data.sortedByDescending { it.entryVouchers }
                _shiftInfo.value = response.shiftInfo
                _loadingError.value = null

                // گام 1: ابتدا اسامی کشتی‌ها را استخراج می‌کنیم
                val shipNames = response.data.map { it.shipName }.distinct().toSet()

                // گام 2: تخصیص رنگ‌های کاملاً متمایز فقط به کشتی‌ها
                // از روش جدید استفاده می‌کنیم که رنگ‌های غیرتکراری را اختصاص می‌دهد
                val shipColors = colorSelector.assignDistinctColors(shipNames)
                    .mapValues { (_, color) -> adjustColorForTheme(color, isDarkTheme) }

                // گام 3: به‌روزرسانی رنگ‌های کشتی‌ها در ViewModel
                _shipColorMap.value = shipColors

                // برای حفظ سازگاری با کدهای دیگر، رنگ کوتاژها را برابر با رنگ کشتی مربوطه قرار می‌دهیم
                val quotaColors = mutableMapOf<String, Color>()
                response.data.forEach { data ->
                    // رنگ کوتاژ را برابر با رنگ کشتی مربوطه قرار می‌دهیم
                    val shipColor = shipColors[data.shipName] ?: adjustColorForTheme(defaultColor, isDarkTheme)
                    quotaColors[data.loadingQuotaNumber] = shipColor
                }
                _quotaColorMap.value = quotaColors

                // اطلاعات تشخیصی برای خطایابی
                Log.d("ColorManager", "Ships: ${shipNames.size}, Unique colors: ${shipColors.values.toSet().size}")

            } catch (e: Exception) {
                _loadingError.value = "خطا در دریافت اطلاعات: ${e.message}"
            }
        }
    }

    private val _shipDetailsLoadingState = MutableStateFlow<LoadingState>(LoadingState.Idle)
    val shipDetailsLoadingState: StateFlow<LoadingState> = _shipDetailsLoadingState.asStateFlow()
    
    private val _shipQuotasLoadingState = MutableStateFlow<LoadingState>(LoadingState.Idle)
    val shipQuotasLoadingState: StateFlow<LoadingState> = _shipQuotasLoadingState.asStateFlow()
    
    private val _isLoadingShipDetails = MutableStateFlow(false)
    val isLoadingShipDetails: StateFlow<Boolean> = _isLoadingShipDetails.asStateFlow()
    
    private val _isLoadingShipQuotas = MutableStateFlow(false)
    val isLoadingShipQuotas: StateFlow<Boolean> = _isLoadingShipQuotas.asStateFlow()

    fun loadShipDetails(shipName: String) {
        viewModelScope.launch {
            _isLoadingShipDetails.value = true
            _shipDetailsLoadingState.value = LoadingState.Idle
            
            try {
                val shipDetails = withContext(Dispatchers.IO) {
                    repository.getShipDetails(shipName)
                }
                
                _selectedShip.value = shipDetails
                _currentShipName.value = shipName
                
                _shipDetailsLoadingState.value = LoadingState.Idle
                
                if (_shipQuotasLoadingState.value !is LoadingState.Error) {
                    _uiState.value = UiState.Success
                }
                
            } catch (e: Exception) {
                val errorMessage = "خطا در بارگیری جزئیات کشتی: ${e.message}"
                _shipDetailsLoadingState.value = LoadingState.Error(errorMessage)
                
                if (_selectedShip.value == null) {
                    _uiState.value = UiState.Error(errorMessage)
                }
            } finally {
                _isLoadingShipDetails.value = false
            }
        }
    }

    fun loadWarehouseDetails(shipName: String, warehouseName: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val warehouseDetails = repository.getWarehouseDetails(shipName, warehouseName)
                val availableDates = warehouseDetails.quotas
                    .flatMap { quota ->
                        quota.exitDates?.map { exitDate -> exitDate.date } ?: emptyList()
                    }
                    .distinct()
                    .sorted()
                _selectedWarehouse.value =
                    warehouseDetails.copy(availableExitDates = availableDates)
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load warehouse details: ${e.message}")
            }
        }
    }

    fun loadQuotaDetails(quotaNumber: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val quotaDetails = repository.getQuotaDetails(quotaNumber)
                _selectedQuotaDetails.value = quotaDetails
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error("خطا در بارگیری جزئیات کوتاژ: ${e.message}")
            }
        }
    }

    fun loadShipQuotas(shipName: String) {
        viewModelScope.launch {
            _isLoadingShipQuotas.value = true
            _shipQuotasLoadingState.value = LoadingState.Idle
            
            try {
                val quotas = withContext(Dispatchers.IO) {
                    repository.getShipQuotas(shipName)
                }
                
                _selectedShipQuotas.value = quotas
                
                _shipQuotasLoadingState.value = LoadingState.Idle
                
                if (_shipDetailsLoadingState.value !is LoadingState.Error) {
                    _uiState.value = UiState.Success
                }
                
            } catch (e: Exception) {
                val errorMessage = "خطا در بارگیری کوتاژهای کشتی: ${e.message}"
                _shipQuotasLoadingState.value = LoadingState.Error(errorMessage)
                
                if (_selectedShipQuotas.value.isEmpty()) {
                    _uiState.value = UiState.Error(errorMessage)
                }
            } finally {
                _isLoadingShipQuotas.value = false
            }
        }
    }

    fun loadShipDataAsync(shipName: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            try {
                val shipDetailsDeferred = async { repository.getShipDetails(shipName) }
                val shipQuotasDeferred = async { repository.getShipQuotas(shipName) }
                
                val shipDetails = shipDetailsDeferred.await()
                val quotas = shipQuotasDeferred.await()
                
                _selectedShip.value = shipDetails
                _currentShipName.value = shipName
                _selectedShipQuotas.value = quotas
                
                _shipDetailsLoadingState.value = LoadingState.Idle
                _shipQuotasLoadingState.value = LoadingState.Idle
                _uiState.value = UiState.Success
                
            } catch (e: Exception) {
                val errorMessage = "خطا در بارگیری اطلاعات کشتی: ${e.message}"
                _uiState.value = UiState.Error(errorMessage)
                _shipDetailsLoadingState.value = LoadingState.Error(errorMessage)
                _shipQuotasLoadingState.value = LoadingState.Error(errorMessage)
            }
        }
    }

    fun refreshShipDataSilently(shipName: String) {
        viewModelScope.launch {
            try {
                val shipDetailsDeferred = async { repository.getShipDetails(shipName) }
                val shipQuotasDeferred = async { repository.getShipQuotas(shipName) }
                
                _selectedShip.value = shipDetailsDeferred.await()
                _selectedShipQuotas.value = shipQuotasDeferred.await()
                _currentShipName.value = shipName
                
            } catch (e: Exception) {
                showSnackbar("خطا در بروزرسانی اطلاعات: ${e.message}")
            }
        }
    }

    fun loadFilteredShipQuotas(shipName: String, startDateTime: String, endDateTime: String) {
        viewModelScope.launch {
            try {
                val quotas = repository.getFilteredQuotas(shipName, startDateTime, endDateTime)
                _selectedShipQuotas.value = quotas
                // ذخیره بازه زمانی انتخاب شده
                _selectedDateRange.value = Pair(startDateTime, endDateTime)
            } catch (e: Exception) {
                _loadingError.value = "خطا در بارگیری کوتاژهای فیلتر شده: ${e.message}"
            }
        }
    }

    fun clearLoadingError() {
        _loadingError.value = null
    }

    fun clearSelectedDateRange() {
        _selectedDateRange.value = null
        // بارگذاری مجدد داده‌ها بدون فیلتر زمانی
        _selectedShip.value?.name?.let { shipName ->
            loadShipQuotas(shipName)
        }
    }

    fun getFilteredSummary(
        shipName: String,
        warehouseName: String,
        selectedQuota: String,
        startDateTime: String?,
        endDateTime: String?
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val summary = repository.getFilteredSummary(
                    shipName = shipName,
                    warehouseName = warehouseName,
                    selectedQuota = selectedQuota,
                    startDateTime = startDateTime ?: "",
                    endDateTime = endDateTime ?: ""
                )
                _filteredSummary.value = summary
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error("خطا در دریافت خلاصه فیلتر شده: ${e.message}")
            }
        }
    }

    fun editQuota(oldQuotaNumber: String, newQuotaData: QuotaEditData) {
        viewModelScope.launch {
            try {
                val success = repository.editQuota(
                    oldQuotaNumber = oldQuotaNumber,
                    newQuotaNumber = newQuotaData.quotaNumber,
                    shipName = newQuotaData.shipName,
                    shippingCompany = newQuotaData.shippingCompany,
                    warehouse = newQuotaData.warehouse,
                    cargoType = newQuotaData.cargoType,
                    totalTonnage = newQuotaData.totalTonnage
                )
                if (success) {
                    // به‌روزرسانی نام فعلی کشتی
                    _currentShipName.value = newQuotaData.shipName
                    // بروزرسانی فوری و بدون تاخیر اطلاعات
                    refreshShipDataSilently(newQuotaData.shipName)
                    showSnackbar("کوتاژ با موفقیت ویرایش شد")
                } else {
                    showSnackbar("خطا در ویرایش کوتاژ")
                }
            } catch (e: Exception) {
                showSnackbar("خطا در ویرایش کوتاژ: ${e.message}")
            }
        }
    }

    fun updateQuotaPercentage(data: QuotaPercentageData) {
        viewModelScope.launch {
            try {
                val success = repository.updateQuotaPercentage(
                    quotaNumber = data.quotaNumber,
                    percentage = data.percentage
                )
                if (success) {
                    _currentShipName.value?.let { shipName ->
                        refreshShipDataSilently(shipName)
                    }
                    showSnackbar("درصد کوتاژ با موفقیت بروزرسانی شد")
                } else {
                    showSnackbar("خطا در بروزرسانی درصد کوتاژ")
                }
            } catch (e: Exception) {
                showSnackbar("خطا در بروزرسانی درصد کوتاژ: ${e.message}")
            }
        }
    }

    fun toggleQuotaStatus(quotaNumber: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val success = repository.toggleQuotaStatus(quotaNumber)
                if (success) {
                    _currentShipName.value?.let { shipName ->
                        refreshShipDataSilently(shipName)
                    }
                    loadShips()
                    showSnackbar("وضعیت کوتاژ با موفقیت تغییر کرد")
                } else {
                    showSnackbar("خطا در تغییر وضعیت کوتاژ")
                }
            } catch (e: Exception) {
                showSnackbar("خطا در تغییر وضعیت کوتاژ: ${e.message}")
            } finally {
                onComplete()
            }
        }
    }

    fun toggleQuotaPercentageRestriction(quotaNumber: String, isRestricted: Boolean, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val success = repository.updateQuotaPercentageRestriction(
                    quotaNumber = quotaNumber,
                    isEnabled = if (isRestricted) 1 else 0
                )
                if (success) {
                    // Reload quotas to refresh the UI
                    _currentShipName.value?.let { shipName ->
                        loadShipQuotas(shipName)
                    }
                    showSnackbar(
                        if (isRestricted) "محدودیت درصد کوتاژ فعال شد"
                        else "محدودیت درصد کوتاژ غیرفعال شد"
                    )
                } else {
                    showSnackbar("خطا در تغییر وضعیت محدودیت درصد کوتاژ")
                }
            } catch (e: Exception) {
                showSnackbar("خطا در تغییر وضعیت محدودیت درصد کوتاژ: ${e.message}")
            } finally {
                onComplete()
            }
        }
    }

    fun updateTemporaryTonnage(
        quotaNumber: String,
        enabled: Boolean,
        tonnage: Double? = null,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val success = repository.updateTemporaryTonnage(
                    quotaNumber = quotaNumber,
                    enabled = if (enabled) 1 else 0,
                    tonnage = tonnage
                )
                if (success) {
                    // Reload quotas to refresh the UI
                    _currentShipName.value?.let { shipName ->
                        loadShipQuotas(shipName)
                    }
                    // Refresh ships list to update temporary tonnage status
                    loadShips()
                    showSnackbar(
                        if (enabled) "تناژ موقت با موفقیت فعال شد"
                        else "تناژ موقت غیرفعال شد"
                    )
                } else {
                    showSnackbar("خطا در به‌روزرسانی تناژ موقت")
                }
            } catch (e: Exception) {
                showSnackbar("خطا در به‌روزرسانی تناژ موقت: ${e.message}")
            } finally {
                onComplete()
            }
        }
    }

    fun deleteQuota(quota: Quota) {
        viewModelScope.launch {
            try {
                val success = repository.deleteQuota(
                    quotaNumber = quota.number,
                    shipName = quota.shipName ?: "",
                    warehouse = quota.warehouse ?: "",
                    shippingCompany = quota.shippingCompany,
                    cargoType = quota.cargoType ?: ""
                )
                if (success) {
                    // Silently refresh data without affecting main UI state
                    refreshShipDataSilently(_selectedShip.value?.name ?: "")
                    showSnackbar("کوتاژ با موفقیت حذف شد")
                } else {
                    showSnackbar("خطا در حذف کوتاژ")
                }
            } catch (e: Exception) {
                showSnackbar("خطا در حذف کوتاژ: ${e.message}")
            }
        }
    }

    fun clearFilteredSummary() {
        _filteredSummary.value = null
    }

    fun performAdvancedSearch(receiptNumber: String, onResult: (Result<CargoInfo?>) -> Unit) {
        viewModelScope.launch {
            try {
                val result = repository.getCargoInfoByReceiptNumber(receiptNumber)
                onResult(Result.success(result))
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    fun performAdvancedSearchByTracking(trackingNumber: String, onResult: (Result<List<CargoInfo>>) -> Unit) {
        viewModelScope.launch {
            try {
                val result = repository.getCargoInfoByTrackingNumber(trackingNumber)
                onResult(Result.success(result))
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    fun updateCargoInfo(cargoInfo: CargoInfo, onResult: (Result<SaveOrUpdateResponse>) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("ReportsViewModel", "🔄 ViewModel: شروع بروزرسانی")
                Log.d("ReportsViewModel", "📦 ViewModel: CargoInfo = $cargoInfo")
                
                val result = repository.updateCargoInfo(cargoInfo)
                
                result.fold(
                    onSuccess = { response ->
                        Log.d("ReportsViewModel", "✅ ViewModel: موفقیت - $response")
                    },
                    onFailure = { error ->
                        Log.e("ReportsViewModel", "❌ ViewModel: خطا - ${error.message}")
                    }
                )
                
                onResult(result)
            } catch (e: Exception) {
                Log.e("ReportsViewModel", "💥 ViewModel: Exception - ${e.message}", e)
                onResult(Result.failure(e))
            }
        }
    }

    fun exportData(format: String, data: FilteredSummary) {
        viewModelScope.launch {
            try {
                val result = when (format.lowercase()) {
                    "pdf" -> createPdfFile(data)
                    else -> throw IllegalArgumentException("Unsupported format")
                }
                _exportResult.value = result
                showFileOptions(result)
            } catch (e: Exception) {
                Log.e("ExportData", "Error exporting data", e)
                _exportResult.value = "خطا در ایجاد فایل: ${e.message}"
                showSnackbar("خطا در ایجاد فایل. لطفاً دوباره تلاش کنید.")
            }
        }
    }

    private fun showFileOptions(fileName: String) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
                if (!file.exists()) {
                    throw FileNotFoundException("File not found: $fileName")
                }

                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val shareIntent = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }, "اشتراک‌گذاری فایل PDF")

                val chooserIntent = Intent.createChooser(viewIntent, "انتخاب عملیات").apply {
                    putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(shareIntent))
                }

                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                context.startActivity(chooserIntent)
            } catch (e: Exception) {
                Log.e("ShowFileOptions", "Error showing file options", e)
                showSnackbar("خطا در نمایش گزینه‌های فایل. لطفاً دوباره تلاش کنید.")
            }
        }
    }

    private suspend fun createPdfFile(data: FilteredSummary): String = withContext(Dispatchers.IO) {
        val fileName = "report_${System.currentTimeMillis()}.pdf"
        val file = File(getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        try {
            file.outputStream().use { outputStream ->
                val document = Document(PageSize.A4, 30f, 30f, 40f, 30f) // کاهش حاشیه‌ها
                val writer = PdfWriter.getInstance(document, outputStream)
                document.open()

                // بارگیری فونت فارسی با بهبود مدیریت خطا
                val persianFontManager = PersianFontManager()
                val fonts = persianFontManager.loadFonts()

                // تعریف رنگ‌های بهبود یافته
                val colorScheme = PdfColorScheme()

                // اضافه کردن سربرگ مدرن
                addModernHeader(document, fonts.headerFont, colorScheme)

                // اضافه کردن اطلاعات خلاصه با قالب‌بندی بهتر
                addEnhancedSummaryInfo(document, data, fonts, colorScheme)

                // ایجاد جدول اصلی با بهبود layout
                val table = createEnhancedMainTable(data, fonts, colorScheme)
                document.add(table)

                // اضافه کردن پاورقی فارسی
                addPersianFooter(document, writer, fonts.normalFont, colorScheme)

                document.close()
            }
        } catch (e: Exception) {
            Log.e("CreatePdfFile", "Error creating PDF file", e)
            throw e
        }

        fileName
    }

    private fun addModernHeader(document: Document, font: Font, colorScheme: PdfColorScheme) {
        // ایجاد جدول سربرگ با قالب‌بندی مدرن
        val headerTable = PdfPTable(1)
        headerTable.widthPercentage = 100f
        headerTable.spacingAfter = 15f

        // سلول اصلی سربرگ
        val headerCell = PdfPCell()
        headerCell.backgroundColor = colorScheme.primary
        headerCell.border = Rectangle.NO_BORDER
        headerCell.paddingTop = 20f
        headerCell.paddingBottom = 20f
        headerCell.paddingLeft = 15f
        headerCell.paddingRight = 15f

        // تنظیم فونت سفید برای سربرگ
        val whiteHeaderFont = Font(font.baseFont, font.size, font.style)
        whiteHeaderFont.color = BaseColor.WHITE

        addPersianText(headerCell, "گزارش خلاصه حواله‌ها", whiteHeaderFont, Element.ALIGN_CENTER, false)
        headerTable.addCell(headerCell)

        // اضافه کردن خط جداکننده زیبا
        val separatorTable = PdfPTable(1)
        separatorTable.widthPercentage = 100f
        separatorTable.spacingAfter = 10f

        val separatorCell = PdfPCell()
        separatorCell.backgroundColor = colorScheme.accent
        separatorCell.border = Rectangle.NO_BORDER
        separatorCell.fixedHeight = 3f
        separatorTable.addCell(separatorCell)

        document.add(headerTable)
        document.add(separatorTable)
    }

    private fun addEnhancedSummaryInfo(
        document: Document,
        data: FilteredSummary,
        fonts: PdfFonts,
        colorScheme: PdfColorScheme
    ) {
        // ایجاد جدول اطلاعات خلاصه با قالب‌بندی مدرن
        val infoTable = PdfPTable(2)
        infoTable.widthPercentage = 100f
        infoTable.setWidths(floatArrayOf(1.2f, 0.8f)) // نسبت بهتر برای ستون‌ها
        infoTable.spacingAfter = 20f

        fun addModernInfoRow(label: String, value: String, isHighlight: Boolean = false) {
            // سلول مقدار با فونت فارسی (سمت راست - ستون اول)
            val valueCell = PdfPCell()
            valueCell.backgroundColor = colorScheme.white
            valueCell.border = Rectangle.BOX
            valueCell.borderColor = colorScheme.lightGray
            valueCell.borderWidth = 1f
            valueCell.paddingTop = 12f
            valueCell.paddingBottom = 12f
            valueCell.paddingLeft = 15f
            valueCell.paddingRight = 15f

            val valueFont = if (isHighlight) fonts.boldFont else fonts.normalFont
            valueFont.color = colorScheme.text
            addPersianText(valueCell, value, valueFont, Element.ALIGN_RIGHT)
            infoTable.addCell(valueCell)

            // سلول عنوان با استایل مدرن (سمت چپ - ستون دوم)
            val labelCell = PdfPCell()
            labelCell.backgroundColor = if (isHighlight) colorScheme.accent else colorScheme.secondary
            labelCell.border = Rectangle.NO_BORDER
            labelCell.paddingTop = 12f
            labelCell.paddingBottom = 12f
            labelCell.paddingLeft = 15f
            labelCell.paddingRight = 15f

            val labelFont = if (isHighlight) fonts.boldFont else fonts.normalFont
            if (isHighlight) labelFont.color = BaseColor.WHITE
            addPersianText(labelCell, label, labelFont, Element.ALIGN_LEFT, false)
            infoTable.addCell(labelCell)
        }

        // اضافه کردن اطلاعات با استایل مناسب
        addModernInfoRow("شماره کوتاژ:", data.quotaNumber)
        addModernInfoRow("از تاریخ و ساعت:", "${convertToShamsiDate(data.startDate)} - ${convertToPersianNumbers(data.startTime)}")
        addModernInfoRow("تا تاریخ و ساعت:", "${convertToShamsiDate(data.endDate)} - ${convertToPersianNumbers(data.endTime)}")
        addModernInfoRow("تعداد کل حواله‌ها:", convertToPersianNumbers(data.voucherCount.toString()), true)
        addModernInfoRow("وزن خالص کل:", "${formatPersianNumber(data.totalNetWeight.toInt())} کیلوگرم", true)

        document.add(infoTable)
    }

    // تابع قالب‌بندی اعداد فارسی با جداکننده هزارگان
    private fun formatPersianNumber(number: Int): String {
        val formatted = NumberFormat.getNumberInstance(Locale("en", "US")).format(number)
        return convertToPersianNumbers(formatted)
    }

    private fun createEnhancedMainTable(
        data: FilteredSummary,
        fonts: PdfFonts,
        colorScheme: PdfColorScheme
    ): PdfPTable {
        val table = PdfPTable(6)
        table.widthPercentage = 100f
        // ترتیب عرض ستون‌ها برعکس شده (از راست به چپ): شماره قبض، وزن خالص، تاریخ خروج، ساعت خروج، ساعت ورود، شماره حواله
        table.setWidths(floatArrayOf(2.2f, 2f, 2f, 1.8f, 1.8f, 2.5f))
        table.spacingBefore = 10f

        fun createModernCell(
            content: String,
            isHeader: Boolean = false,
            isPersian: Boolean = true,
            isNumeric: Boolean = false
        ): PdfPCell {
            val cell = PdfPCell()

            // تنظیمات padding بهتر
            cell.paddingTop = if (isHeader) 15f else 10f
            cell.paddingBottom = if (isHeader) 15f else 10f
            cell.paddingLeft = 8f
            cell.paddingRight = 8f

            if (isHeader) {
                // استایل سرستون
                cell.backgroundColor = colorScheme.primary
                cell.border = Rectangle.BOX
                cell.borderColor = colorScheme.white
                cell.borderWidth = 1f

                val headerFont = Font(fonts.boldFont.baseFont, 11f, Font.BOLD)
                headerFont.color = BaseColor.WHITE

                addPersianText(cell, content, headerFont, Element.ALIGN_CENTER, false)
            } else {
                // استایل سلول‌های داده
                cell.backgroundColor = colorScheme.white
                cell.border = Rectangle.BOX
                cell.borderColor = colorScheme.lightGray
                cell.borderWidth = 0.5f

                val cellFont = fonts.normalFont
                cellFont.color = colorScheme.text

                if (isPersian) {
                    val processedContent = if (isNumeric) convertToPersianNumbers(content) else content
                    addPersianText(cell, processedContent, cellFont, Element.ALIGN_CENTER)
                } else {
                    cell.phrase = Phrase(content, cellFont)
                    cell.horizontalAlignment = Element.ALIGN_CENTER
                }
            }

            return cell
        }

        // سرستون‌های جدول با ترتیب راست به چپ فارسی (از راست: شماره قبض تا چپ: شماره حواله)
        table.addCell(createModernCell("شماره قبض", isHeader = true))
        table.addCell(createModernCell("وزن خالص (کیلوگرم)", isHeader = true))
        table.addCell(createModernCell("تاریخ خروج", isHeader = true))
        table.addCell(createModernCell("ساعت خروج", isHeader = true))
        table.addCell(createModernCell("ساعت ورود", isHeader = true))
        table.addCell(createModernCell("شماره حواله", isHeader = true))

        // داده‌های جدول با ترتیب راست به چپ فارسی
        data.voucherDetails.forEachIndexed { index, detail ->
            // رنگ‌بندی متناوب برای بهتر خوانی
            val isEvenRow = index % 2 == 0
            val rowColor = if (isEvenRow) colorScheme.white else colorScheme.lightGray

            // شماره قبض (سمت راست)
            table.addCell(createModernCell(detail.scaleReceiptNumber, isPersian = false).apply {
                backgroundColor = rowColor
            })

            // وزن خالص
            table.addCell(createModernCell(formatPersianNumber(detail.netWeight.toInt()), isPersian = true).apply {
                backgroundColor = rowColor
            })

            // تاریخ خروج
            table.addCell(createModernCell(convertToShamsiDate(detail.exitDate), isPersian = true).apply {
                backgroundColor = rowColor
            })

            // ساعت خروج
            table.addCell(createModernCell(detail.exitTime, isPersian = true, isNumeric = true).apply {
                backgroundColor = rowColor
            })

            // ساعت ورود
            table.addCell(createModernCell(detail.entryTime, isPersian = true, isNumeric = true).apply {
                backgroundColor = rowColor
            })

            // شماره حواله (سمت چپ)
            table.addCell(createModernCell(detail.trackingNumber, isPersian = false).apply {
                backgroundColor = rowColor
            })
        }

        return table
    }

    // کلاس مدیریت فونت‌های فارسی
    private inner class PersianFontManager {
        fun loadFonts(): PdfFonts {
            return try {
                val baseFont = createFont("assets/fonts/B NAZANIN.TTF", IDENTITY_H, true)
                val fallbackFont = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL)

                PdfFonts(
                    normalFont = Font(baseFont, 11f, Font.NORMAL),
                    boldFont = Font(baseFont, 12f, Font.BOLD),
                    headerFont = Font(baseFont, 20f, Font.BOLD),
                    titleFont = Font(baseFont, 16f, Font.BOLD),
                    subtitleFont = Font(baseFont, 14f, Font.BOLD),
                    fallbackFont = fallbackFont
                )
            } catch (e: Exception) {
                Log.w("PersianFontManager", "Failed to load Persian font, using fallback", e)
                // در صورت عدم موفقیت در بارگیری فونت فارسی، از فونت پیش‌فرض استفاده می‌کنیم
                val fallback = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL)
                PdfFonts(
                    normalFont = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL),
                    boldFont = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD),
                    headerFont = Font(Font.FontFamily.HELVETICA, 20f, Font.BOLD),
                    titleFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD),
                    subtitleFont = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD),
                    fallbackFont = fallback
                )
            }
        }
    }

    // کلاس نگهداری فونت‌ها
    private data class PdfFonts(
        val normalFont: Font,
        val boldFont: Font,
        val headerFont: Font,
        val titleFont: Font,
        val subtitleFont: Font,
        val fallbackFont: Font
    )

    // کلاس رنگ‌بندی PDF
    private class PdfColorScheme {
        val primary = BaseColor(0, 96, 100)           // تیره‌تر برای بهتر خوانی
        val secondary = BaseColor(240, 248, 255)      // آبی خیلی روشن
        val accent = BaseColor(255, 193, 7)           // زرد برای تأکید
        val text = BaseColor(33, 37, 41)              // خاکستری تیره برای متن
        val lightGray = BaseColor(248, 249, 250)      // خاکستری روشن
        val white = BaseColor.WHITE
    }

    // تابع بهبود یافته برای افزودن متن فارسی با پشتیبانی از اعداد فارسی
    private fun addPersianText(
        cell: PdfPCell,
        text: String,
        font: Font,
        alignment: Int = Element.ALIGN_RIGHT,
        convertNumbers: Boolean = true
    ) {
        val processedText = if (convertNumbers) convertToPersianNumbers(text) else text
        val column = ColumnText(null)
        column.runDirection = PdfWriter.RUN_DIRECTION_RTL
        column.alignment = alignment
        column.addElement(Paragraph(processedText, font))
        cell.column = column
    }

    // تابع تبدیل اعداد انگلیسی به فارسی
    private fun convertToPersianNumbers(text: String): String {
        val persianDigits = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
        var result = text

        for (i in 0..9) {
            result = result.replace(i.toString(), persianDigits[i])
        }

        return result
    }

    // تابع تبدیل تاریخ میلادی به شمسی (برای تاریخ‌های string)
    private fun convertToShamsiDate(date: String): String {
        return try {
            val parts = date.split("/")
            if (parts.size == 3) {
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                val day = parts[2].toInt()

                // استفاده از الگوریتم تبدیل
                val shamsiDate = gregorianToShamsi(year, month, day)
                val yearStr = shamsiDate.year.toString()
                val monthStr = shamsiDate.month.toString().padStart(2, '0')
                val dayStr = shamsiDate.day.toString().padStart(2, '0')

                "${convertToPersianNumbers(yearStr)}/${convertToPersianNumbers(monthStr)}/${convertToPersianNumbers(dayStr)}"
            } else {
                convertToPersianNumbers(date)
            }
        } catch (e: Exception) {
            Log.w("DateConverter", "Failed to convert date: $date", e)
            convertToPersianNumbers(date)
        }
    }

    // تابع تبدیل تاریخ میلادی کامل به شمسی برای استفاده در پاورقی
    private fun convertGregorianToShamsi(gregorianDate: Date): String {
        return try {
            val calendar = Calendar.getInstance()
            calendar.time = gregorianDate

            val gregorianYear = calendar.get(Calendar.YEAR)
            val gregorianMonth = calendar.get(Calendar.MONTH) + 1
            val gregorianDay = calendar.get(Calendar.DAY_OF_MONTH)

            // محاسبه تاریخ شمسی
            val shamsiDate = gregorianToShamsi(gregorianYear, gregorianMonth, gregorianDay)

            val year = shamsiDate.year.toString()
            val month = shamsiDate.month.toString().padStart(2, '0')
            val day = shamsiDate.day.toString().padStart(2, '0')

            "${convertToPersianNumbers(year)}/${convertToPersianNumbers(month)}/${convertToPersianNumbers(day)}"
        } catch (e: Exception) {
            Log.w("ShamsiConverter", "Failed to convert Gregorian to Shamsi", e)
            // در صورت خطا، تاریخ میلادی را به صورت فارسی برمی‌گردانیم
            val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            convertToPersianNumbers(dateFormat.format(gregorianDate))
        }
    }

    // کلاس داده برای نگهداری تاریخ شمسی
    private data class ShamsiDate(val year: Int, val month: Int, val day: Int)

    // تابع تبدیل تاریخ میلادی به شمسی (الگوریتم بهبود یافته)
    private fun gregorianToShamsi(gYear: Int, gMonth: Int, gDay: Int): ShamsiDate {
        val gMonthDays = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)

        val gy = gYear - 1600
        val gm = gMonth - 1
        val gd = gDay - 1

        var gDayNo = 365 * gy + ((gy + 3) / 4) - ((gy + 99) / 100) + ((gy + 399) / 400) - 80 + gd + gMonthDays[gm]

        // بررسی سال کبیسه
        if (gm > 1 && ((gYear % 4 == 0 && gYear % 100 != 0) || (gYear % 400 == 0))) {
            gDayNo++
        }

        var jDayNo = gDayNo - 79

        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jYear = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jYear += ((jDayNo - 1) / 365)
            jDayNo = (jDayNo - 1) % 365
        }

        val jMonth: Int
        val jDay: Int

        if (jDayNo < 186) {
            // ماه‌های فروردین تا شهریور (۶ ماه اول - هر کدام ۳۱ روز)
            jMonth = 1 + jDayNo / 31
            jDay = 1 + (jDayNo % 31)
        } else {
            // ماه‌های مهر تا اسفند (۶ ماه آخر - هر کدام ۳۰ روز)
            jMonth = 7 + (jDayNo - 186) / 30
            jDay = 1 + ((jDayNo - 186) % 30)
        }

        return ShamsiDate(jYear, jMonth, jDay)
    }

    @SuppressLint("SimpleDateFormat", "DefaultLocale")
    private fun addPersianFooter(
        document: Document,
        writer: PdfWriter,
        font: Font,
        colorScheme: PdfColorScheme
    ) {
        // تولید تاریخ و زمان فارسی
        val currentDate = Date()
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        // تبدیل تاریخ کنونی به شمسی
        val persianDate = convertGregorianToShamsi(currentDate)
        val persianTime = convertToPersianNumbers(timeFormat.format(currentDate))

        for (pageNumber in 1..writer.pageNumber) {
            val footerTable = PdfPTable(3)
            footerTable.totalWidth = document.pageSize.width - document.leftMargin() - document.rightMargin()
            footerTable.setWidths(floatArrayOf(1f, 1.5f, 1f))

            // شماره صفحه (سمت چپ)
            val pageCell = PdfPCell()
            pageCell.border = Rectangle.NO_BORDER
            pageCell.paddingTop = 10f
            pageCell.paddingBottom = 5f
            val pageText = "صفحه ${convertToPersianNumbers(pageNumber.toString())} از ${convertToPersianNumbers(writer.pageNumber.toString())}"
            addPersianText(pageCell, pageText, font, Element.ALIGN_LEFT)
            footerTable.addCell(pageCell)

            // تاریخ و زمان تولید گزارش (وسط)
            val dateTimeCell = PdfPCell()
            dateTimeCell.border = Rectangle.NO_BORDER
            dateTimeCell.paddingTop = 10f
            dateTimeCell.paddingBottom = 5f
            val dateTimeText = "تاریخ ایجاد گزارش: $persianDate - ساعت: $persianTime"
            addPersianText(dateTimeCell, dateTimeText, font, Element.ALIGN_CENTER)
            footerTable.addCell(dateTimeCell)

            // نام شرکت (سمت راست)
            val companyCell = PdfPCell()
            companyCell.border = Rectangle.NO_BORDER
            companyCell.paddingTop = 10f
            companyCell.paddingBottom = 5f
            addPersianText(companyCell, "امین تجار خوزستان", font, Element.ALIGN_RIGHT, false)
            footerTable.addCell(companyCell)

            // رسم جدول پاورقی
            footerTable.writeSelectedRows(
                0, -1,
                document.leftMargin(),
                document.bottomMargin() + 30f, // فاصله بهتر از پایین صفحه
                writer.directContent
            )
        }
    }

    fun loadComprehensiveAnalytics() {
        viewModelScope.launch {
            try {
                _analyticsLoadingState.value = LoadingState.Loading
                val response = repository.getComprehensiveAnalysis()

                if (response.success) {
                    _comprehensiveAnalytics.value = ComprehensiveAnalytics(
                        peakHoursAnalysis = response.data.peakHoursAnalysis?.map { hour ->
                            PeakHourData(
                                hour = hour.hour,
                                total_operations = hour.total_operations,
                                entries = hour.entries,
                                exits = hour.exits,
                                percentage = hour.percentage
                            )
                        } ?: emptyList(),
                        shiftPerformanceAnalysis = response.data.shiftPerformanceAnalysis?.map { shift ->
                            ShiftPerformanceData(
                                shift = shift.shift,
                                total_operations = shift.total_operations,
                                total_vouchers = shift.total_vouchers,
                                active_carriers_list = shift.active_carriers_list,
                                avg_completion_time = shift.avg_completion_time,
                                completed_operations = shift.completed_operations,
                                total_weight_tons = shift.total_weight_tons,
                                peak_hour = shift.peak_hour,
                                peak_hour_operations = shift.peak_hour_operations,
                                peak_hour_vouchers = shift.peak_hour_vouchers,
                                peak_hour_detail = shift.peak_hour_detail,
                                avg_weight_per_operation = shift.avg_weight_per_operation,
                                delayed_operations_detail = shift.delayed_operations_detail,
                                completion_rate = shift.completion_rate,
                                efficiency_score = shift.efficiency_score,
                                total_active_carriers = shift.total_active_carriers,
                                weight_standard_deviation = shift.weight_standard_deviation
                            )
                        } ?: emptyList(),
                        quotaCompletionAnalysis = response.data.quotaCompletionAnalysis?.map { quota ->
                            QuotaCompletionData(
                                loadingQuotaNumber = quota.loadingQuotaNumber,
                                total_quota_weight = quota.total_quota_weight,
                                total_vouchers = quota.total_vouchers,
                                completion_percentage = quota.completion_percentage,
                                avg_completion_hours = quota.avg_completion_hours,
                                shipName = quota.shipName,
                                shippingCompany = quota.shippingCompany,
                                last_24h_weight = quota.last_24h_weight,
                                last_24h_vouchers = quota.last_24h_vouchers
                            )
                        } ?: emptyList(),
                        quotaProgressAnalysis = response.data.quotaProgressAnalysis?.map { progress ->
                            QuotaProgressData(
                                loadingQuotaNumber = progress.loadingQuotaNumber,
                                exitDate = progress.exitDate,
                                daily_vouchers = progress.daily_vouchers,
                                daily_weight = progress.daily_weight,
                                cumulative_completion = progress.cumulative_completion
                            )
                        } ?: emptyList(),
                        quotaPredictionAnalysis = response.data.quotaPredictionAnalysis?.map { prediction ->
                            QuotaPredictionData(
                                loadingQuotaNumber = prediction.loadingQuotaNumber,
                                daily_rate = prediction.daily_rate,
                                total_completed_weight = prediction.total_completed_weight,
                                total_weight = prediction.total_weight,
                                estimated_days_remaining = prediction.estimated_days_remaining
                            )
                        } ?: emptyList(),
                        carrierPerformanceAnalysis = response.data.carrierPerformanceAnalysis?.map { carrier ->
                            CarrierPerformanceAnalysis(
                                shippingCompany = carrier.shippingCompany,
                                total_deliveries = carrier.total_deliveries,
                                completed_deliveries = carrier.completed_deliveries,
                                total_quotas = carrier.total_quotas,
                                completed_quotas = carrier.completed_quotas,
                                avg_net_weight = carrier.avg_net_weight,
                                min_weight = carrier.min_weight,
                                max_weight = carrier.max_weight,
                                avg_operation_time = carrier.avg_operation_time,
                                operations_per_hour = carrier.operations_per_hour,
                                peak_hour = carrier.peak_hour,
                                peak_hour_operations = carrier.peak_hour_operations,
                                quality_score = carrier.quality_score,
                                performance_category = carrier.performance_category,
                                completed_quota_numbers = carrier.completed_quota_numbers,
                            )
                        } ?: emptyList(),
                        warehouseEfficiencyAnalysis = response.data.warehouseEfficiencyAnalysis?.map { warehouse ->
                            WarehouseEfficiencyData(
                                loadingWarehouse = warehouse.loadingWarehouse,
                                active_quotas = warehouse.active_quotas,
                                total_operations = warehouse.total_operations,
                                total_processed_weight = warehouse.total_processed_weight,
                                daily_throughput = warehouse.daily_throughput,
                                daily_operations = warehouse.daily_operations,
                                operation_percentage = warehouse.operation_percentage
                            )
                        } ?: emptyList(),
                        warehouseSpeedAnalysis = response.data.warehouseSpeedAnalysis?.map { speed ->
                            WarehouseSpeedData(
                                loadingWarehouse = speed.loadingWarehouse,
                                total_operations = speed.total_operations,
                                avg_processing_minutes = speed.avg_processing_minutes,
                                completed_operations = speed.completed_operations,
                                avg_processed_weight = speed.avg_processed_weight,
                                completion_rate = speed.completion_rate,
                                weight_per_minute = speed.weight_per_minute
                            )
                        } ?: emptyList(),
                        warehouseTrafficAnalysis = response.data.warehouseTrafficAnalysis?.map { traffic ->
                            WarehouseTrafficData(
                                loadingWarehouse = traffic.loadingWarehouse,
                                hour = traffic.hour,
                                entries = traffic.entries,
                                exits = traffic.exits,
                                hour_percentage = traffic.hour_percentage,
                                avg_processed_weight = traffic.avg_processed_weight
                            )
                        } ?: emptyList(),
                        warehousePeakAnalysis = response.data.warehousePeakAnalysis?.map { peak ->
                            WarehousePeakData(
                                loadingWarehouse = peak.loadingWarehouse,
                                hour = peak.hour,
                                operation_count = peak.operation_count,
                                avg_weight = peak.avg_weight,
                                period_percentage = peak.period_percentage,
                                activity_level = peak.activity_level
                            )
                        } ?: emptyList(),
                        cargoOwnerAnalysis = response.data.cargoOwnerAnalysis?.map { ship ->
                            CargoOwnerData(
                                shipName = ship.shipName,
                                owner_count = ship.owner_count,
                                total_vouchers = ship.total_vouchers,
                                total_net_weight = ship.total_net_weight,
                                owners = ship.owners.map { owner ->
                                    CargoOwnerDetailsData(
                                        cargoOwner = owner.cargoOwner,
                                        voucher_count = owner.voucher_count,
                                        net_weight = owner.net_weight,
                                        quota_count = owner.quota_count
                                    )
                                }
                            )
                        } ?: emptyList()
                    )
                    _analyticsLoadingState.value = LoadingState.Success
                } else {
                    _analyticsLoadingState.value = LoadingState.Error("خطا در دریافت اطلاعات تحلیلی")
                }
            } catch (e: Exception) {
                _analyticsLoadingState.value = LoadingState.Error(e.message ?: "خطای ناشناخته")
            }
        }
    }

    sealed class LoadingState {
        object Idle : LoadingState()
        object Loading : LoadingState()
        object Success : LoadingState()
        data class Error(val message: String) : LoadingState()
    }

    sealed class UiState {
        data object Loading : UiState()
        data object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AtkCargoApplication)
                val repository = application.container.reportsRepository
                ReportsViewModel(repository, application)
            }
        }
    }

    fun setWarehouseQuotaGroupingMode(mode: WarehouseQuotaGroupingMode) {
        _warehouseQuotaGroupingMode.value = mode
    }

    fun setQuotaSortingMode(mode: QuotaSortingMode) {
        _quotaSortingMode.value = mode
    }

    fun setGroupSortingMode(mode: GroupSortingMode) {
        _groupSortingMode.value = mode
    }

    fun setShipSortingMode(mode: ShipSortingMode) {
        _shipSortingMode.value = mode
    }

    fun shareRealTimeLoadingData(loadingData: List<RealTimeLoadingData>, shiftInfo: ShiftInfo?): String {
        // محاسبه کل حواله‌های خروجی
        val totalExitVouchers = loadingData.sumOf { it.exitVouchers }

        // ساخت متن قابل اشتراک‌گذاری
        val shareText = StringBuilder()

        // عنوان گزارش با اطلاعات شیفت و تاریخ شمسی
        val shiftType = shiftInfo?.type ?: "نامشخص"
        val calendar = Calendar.getInstance()

        // تنظیم تاریخ براساس قوانین شیفت شب
        if (shiftType == "شب") {
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)
            // اگر شیفت شب باشد و ساعت کمتر از 7:30 صبح باشد، یک روز از تاریخ کم می‌کنیم
            if (currentHour < 7 || (currentHour == 7 && currentMinute < 30)) {
                calendar.add(Calendar.DAY_OF_MONTH, -1)
            }
        }

        val jalaliDate = gregorianToJalali(calendar)
        shareText.append("بارگیری [$shiftType] $jalaliDate - کل: $totalExitVouchers حواله\n\n")

        // ایجاد یک لیست از تمام ترکیب‌های انبار-کشتی با حواله‌های خروجی آنها
        val combinedData = mutableListOf<Triple<String, String, Int>>()

        // گروه‌بندی داده‌ها بر اساس انبار
        val warehouseGroupedData = loadingData.groupBy { it.loadingWarehouse }

        warehouseGroupedData.forEach { (warehouseName, data) ->
            // گروه‌بندی داده‌های هر انبار بر اساس کشتی
            val shipGroupedData = data.groupBy { it.shipName }

            shipGroupedData.forEach { (shipName, shipData) ->
                // محاسبه تعداد حواله‌های خروج شده برای این انبار و کشتی
                val exitVouchers = shipData.sumOf { it.exitVouchers }

                // اضافه کردن به لیست ترکیبی فقط اگر حواله خروجی داشته باشد
                if (exitVouchers > 0) {
                    val capitalizedShipName = shipName.lowercase().replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    }
                    combinedData.add(Triple(warehouseName, capitalizedShipName, exitVouchers))
                }
            }
        }

        // مرتب‌سازی لیست براساس نام کشتی
        combinedData.sortBy { it.second }

        // افزودن اطلاعات مرتب شده به متن خروجی
        combinedData.forEach { (warehouseName, shipName, exitVouchers) ->
            shareText.append("* $warehouseName [$shipName]: $exitVouchers\n")
        }

        return shareText.toString()
    }
}

class ReportsRepository(private val apiService: ApiService) {
    suspend fun getCargoInfo(
        quotaNumber: String,
        shippingCompany: String,
        warehouse: String,
        cargoType: String
    ): CargoInfoResponse {
        try {
            val response = RetrofitClient.apiService.getCargoInfo(
                quotaNumber = quotaNumber,
                shippingCompany = shippingCompany,
                warehouse = warehouse,
                cargoType = cargoType
            )

            if (response.isSuccessful) {
                return response.body() ?: throw Exception("Empty response body")
            } else {
                val errorBody = response.errorBody()?.string()
                throw Exception("Failed to fetch cargo info. Response code: ${response.code()}, Error body: $errorBody")
            }
        } catch (e: Exception) {
            Log.e("ReportsRepository_Log", "Exception in getCargoInfo: ${e.message}", e)
            throw e
        }
    }

    // فقط اطلاعات اولیه را بدون لیست حواله‌ها دریافت می‌کند
    suspend fun getInitialInfo(
        quotaNumber: String,
        shippingCompany: String,
        warehouse: String,
        cargoType: String
    ): InitialInfo? {
        try {
            val response = RetrofitClient.apiService.getCargoInfo(
                quotaNumber = quotaNumber,
                shippingCompany = shippingCompany,
                warehouse = warehouse,
                cargoType = cargoType
            )

            if (response.isSuccessful) {
                return response.body()?.initialInfo
            } else {
                Log.e("ReportsRepository_Log", "Error fetching initial info: ${response.code()}")
                return null
            }
        } catch (e: Exception) {
            Log.e("ReportsRepository_Log", "Exception in getInitialInfo: ${e.message}", e)
            return null
        }
    }

    suspend fun getShipsList(): ShipsData = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getShipsList()
            if (response.isSuccessful) {
                val shipsData = response.body()?.data ?: ShipsData(emptyList(), emptyList())
                shipsData
            } else {
                Log.e("ReportsRepository_Log", "خطا در دریافت لیست کشتی‌ها - کد خطا: ${response.code()}, پیام خطا: ${response.errorBody()?.string()}")
                ShipsData(emptyList(), emptyList())
            }
        } catch (e: Exception) {
            Log.e("ReportsRepository_Log", "استثنا در دریافت لیست کشتی‌ها: ${e.message}", e)
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
                Log.e("ReportsRepository_Log", "Server error ${response.code()}: $errorBody")
                throw Exception("Server error: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Log.e("ReportsRepository_Log", "Exception in getFilteredQuotas: ${e.message}", e)
            throw Exception("Error fetching filtered quotas: ${e.message}")
        }
    }

    suspend fun checkQuotaStatus(
        quotaNumber: String,
        shipName: String,
        cargoType: String,
        shippingCompany: String
    ): QuotaStatusResponse = withContext(Dispatchers.IO) {
        try {
            val response = apiService.checkQuotaStatus(
                quotaNumber = quotaNumber,
                shipName = shipName,
                cargoType = cargoType,
                shippingCompany = shippingCompany
            )

            if (response.isSuccessful) {
                val result = response.body()
                if (result != null) {
                    Log.d("Repository", "Quota status response: $result")
                    result
                } else {
                    Log.e("Repository", "Empty response body")
                    QuotaStatusResponse(
                        isActive = false,
                        status = false,
                        message = "خطا در دریافت وضعیت کوتاژ: پاسخ خالی از سرور",
                        details = null
                    )
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java)?.error
                        ?: "خطای سرور: ${response.code()}"
                } catch (_: Exception) {
                    "خطای سرور: ${response.code()}"
                }

                Log.e("Repository", "Server error: $errorMessage")
                QuotaStatusResponse(
                    isActive = false,
                    status = false,
                    message = errorMessage,
                    details = null
                )
            }
        } catch (e: Exception) {
            Log.e("Repository", "Error checking quota status", e)
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

    suspend fun toggleQuotaStatus(quotaNumber: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = apiService.toggleQuotaStatus(quotaNumber = quotaNumber)
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
                action = "getFilteredSummary",
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

    suspend fun getRealTimeLoadingData(): RealTimeDataResponse = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRealTimeLoadingData()
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
                    val errorBody = response.errorBody()?.string()
                    Log.e("CargoSearch", "❌ خطا ${response.code()}: $errorBody")
                    
                    // تلاش برای parse کردن JSON خطا
                    try {
                        if (!errorBody.isNullOrEmpty() && errorBody.trim().startsWith("{")) {
                            val errorJson = Gson().fromJson(errorBody, JsonObject::class.java)
                            if (errorJson.has("message")) {
                                Log.e("CargoSearch", "💬 پیام خطا: ${errorJson.get("message").asString}")
                            }
                            if (errorJson.has("file")) {
                                Log.e("CargoSearch", "📁 فایل: ${errorJson.get("file").asString}")
                            }
                            if (errorJson.has("line")) {
                                Log.e("CargoSearch", "📍 خط: ${errorJson.get("line").asInt}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("CargoSearch", "خطا در parse کردن JSON: ${e.message}")
                    }
                    null
                }
            } catch (e: Exception) {
                Log.e("CargoSearch", "💥 Exception در جستجو: ${e.message}", e)
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
                            Log.w("CargoSearch", "⚠️ خطا در response: ${responseBody.error}")
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
                        Log.w("CargoSearch", "⚠️ Response body null است")
                        return@withContext emptyList()
                    }
                } else {
                    // Handle error response
                    try {
                        val errorBody = response.errorBody()?.string()
                        Log.e("CargoSearch", "❌ خطا ${response.code()}: $errorBody")

                        if (!errorBody.isNullOrEmpty()) {
                            // Check if error message is JSON
                            if (errorBody.trim().startsWith("{")) {
                                val errorJson = Gson().fromJson(errorBody, JsonObject::class.java)
                                if (errorJson.has("error")) {
                                    val errorMessage = errorJson.get("error").asString
                                    Log.e("CargoSearch", "❌ پیام خطا: $errorMessage")
                                }
                            } else {
                                Log.w("CargoSearch", "🚨 Server error (non-JSON): $errorBody")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("CargoSearch", "❌ Error parsing error response: ${e.message}")
                    }

                    return@withContext emptyList()
                }
            } catch (e: Exception) {
                Log.e("CargoSearch", "💥 Exception در جستجو: ${e.message}", e)
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
                        Log.e("ReportsRepository", "❌ پاسخ سرور خالی است")
                        Result.failure(Exception("پاسخ سرور خالی است"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ReportsRepository", "❌ خطا ${response.code()}: $errorBody")
                    
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
                Log.e("ReportsRepository", "💥 Exception در بروزرسانی: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getComprehensiveAnalysis(): ComprehensiveAnalysisResponse {
        val response = apiService.getComprehensiveAnalysis()
        if (response.isSuccessful) {
            val rawResponse = response.body()?.string() ?: throw Exception("Empty response")
            val jsonStart = rawResponse.indexOf("{")
            val jsonResponse = rawResponse.substring(jsonStart)
            return Gson().fromJson(jsonResponse, ComprehensiveAnalysisResponse::class.java)
        } else {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string()}")
        }
    }
}

@SuppressLint("DefaultLocale")
fun gregorianToJalali(gregorian: Calendar): String {
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

data class QuotaExistenceMultipleResponse(
    val exists: Boolean,
    val matchingQuotas: List<MatchingQuota>,
    val message: String
)

data class MatchingQuota(
    val quotaNumber: String,
    val shipName: String,
    val shippingCompany: String,
    val cargoType: String,
    val warehouse: String,
    val isActive: Boolean = true
)

data class LoginRequest(
    val username: String,
    val password: String,
    val userType: String,
    val deviceModel: String = "",
    val deviceId: String = "",
    val androidVersion: String = ""
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val userType: String?,
    val sessionToken: String? = null
)

data class LogoutRequest(
    val username: String,
    val deviceId: String = "",
    val sessionToken: String? = null
)

data class LogoutResponse(
    val success: Boolean,
    val message: String
)

data class ActiveShipInfo(
    val shipName: String,
    val loadingWarehouse: String,
    val cargoType: String,
    val shippingCompany: String,
    val loadingQuotaNumber: String,
    var entryVouchers: Int = 0,
    var exitVouchers: Int = 0
)

data class CargoInfoResponse(
    val initialInfo: InitialInfo,
    val cargoInfoList: List<CargoInfo>,
    val stats: CargoStats? = null,
    val allTrackingNumbers: List<String>? = null
)

data class CargoStats(
    val totalNetWeight: Int,
    val exitedVouchers: Int,
    val remainingWeight: Int,
    val averageNetWeight: Float,
    val remainingServices: Int
)

enum class MessageType {
    SUCCESS, WARNING, ERROR
}

data class PasswordCheckResponse(
    val success: Boolean,
    val message: String
)

data class CheckExistenceRequest(
    val loadingQuotaNumber: Int,
    val shipName: String,
    val loadingWarehouse: String,
    val cargoType: String,
    val shippingCompany: String
)

data class CheckExistenceResponse(
    val status: String,
    val message: String
)

@Parcelize
data class InitialInfo(
    val shipName: String,
    val loadingWarehouse: String,
    val cargoType: String,
    val shippingCompany: String,
    val cargoWeight: Float,
    val loadingQuotaNumber: Int,
    val remainingWeight: Float,
    val totalNetWeight: Float,
    val averageNetWeight: Float,
    val remainingServices: Int,
    val cargoOwner: String = "",
    val isActive: Int = 1,
    val totalVoucherCount: Int = 0,
    val tempTonnageStatus: Boolean = false,
    val tempTonnageAmount: Float? = null
) : Parcelable

data class CargoInfo(
    val id: Int? = null,
    val trackingNumber: String,
    val numberOfPeople: String,
    val username: String,
    val userType: String,
    val entryTime: String,
    val netWeight: String,
    val scaleReceiptNumber: String,
    val shortageWeight: String,
    val excessWeight: String,
    val exitTime: String?,
    val exitDate: String?,
    val status: String,
    val shipName: String,
    val loadingWarehouse: String,
    val cargoType: String,
    val shippingCompany: String,
    val loadingQuotaNumber: String,
    var confirm: String,
    val confirmation: String? = null,
    val duplicateConfirmation: String? = null
)

data class SaveOrUpdateResponse(
    val error: Boolean?,
    val message: String,
    val status: String?,
    val trackingNumber: String?,
    val shipName: String?,
    val loadingQuotaNumber: String?,
    val exitDate: String?,
    val exitTime: String?,
    val warning: Boolean? = null,
    val existingCargo: ExistingCargo? = null,
    val requiresConfirmation: Boolean? = null
)

data class ExistingCargo(
    val loadingQuotaNumber: String,
    val loadingWarehouse: String,
    val exitTime: String?,
    val exitDate: String?,
    val status: String
)

data class CargoInfoRequest(
    val trackingNumber: String,
    val shipName: String,
    val loadingWarehouse: String,
    val cargoType: String,
    val shippingCompany: String,
    val loadingQuotaNumber: String
)

data class ShipInfo(
    val shipName: String,
    val loadingWarehouse: String,
    val cargoType: String,
    val shippingCompany: String,
    val loadingQuotaNumber: String,
    val cargoWeight: String,
    val remainingWeight: String,
    val totalNetWeight: String,
    val averageNetWeight: String,
    val totalServices: String,
    val remainingServices: String,
    val tempTonnageStatus: Boolean = false,
    val tempTonnageAmount: Float? = null
)

data class MenuItem(
    val title: String,
    val iconResourceId: Int,
    val route: String
)

data class SuccessResponse(
    val success: Boolean,
    val message: String,
    val userId: Int? = null
)

data class CargoInfoSearch(
    val cargoInfo: CargoInfo?
)

data class CargoSearchResponse(
    val cargoInfoList: List<CargoInfoSearch>? = null,
    val totalCount: Int? = null,
    val error: String? = null
)

data class SessionCheckRequest(
    val username: String,
    val deviceId: String = "",
    val sessionToken: String? = null
)

data class SessionResponse(val success: Boolean, val message: String, val userType: String?)

data class RealTimeDataResponse(
    val shiftInfo: ShiftInfo,
    val data: List<RealTimeLoadingData>
)

data class ShiftInfo(
    val start: String,
    val end: String,
    val type: String
)

data class WarningStatus(
    val show: Boolean,
    val quotaNumber: String,
    val percentage: Double,
    val remainingTonnage: Float,
    val percentageAmount: Double,
    val isActive: Boolean = true,
    val isPercentageRestricted: Boolean = false
)

data class QuotaPercentageData(
    val quotaNumber: String,
    val percentage: Double,
    val calculations: CalculationResult,
    val isEnabled: Int = if (percentage > 0.0) 1 else 0
)

data class CalculationResult(
    val percentageAmount: Double,
    val remainingAfterPercentage: Double,
    val totalRemainingAfterPercentage: Double
)

data class RealTimeLoadingData(
    val loadingQuotaNumber: String,
    val shipName: String,
    val loadingWarehouse: String,
    val shippingCompany: String,
    val entryVouchers: Int,
    val exitVouchers: Int,
    val totalNetWeight: Int,
    val cargoWeight: Int
)

data class ShipsData(
    val activeShips: List<Ship>,
    val inactiveShips: List<Ship>
)

data class Ship(
    val name: String,
    val warehouseCount: Int,
    val quotaCount: Int,
    val totalTonnage: Float,
    val remainingTonnage: Float,
    val totalVoucherCount: Int,
    val isActive: Boolean,
    val warehouses: List<Warehouse>
)

data class Quota(
    val number: String,
    val shipName: String?,
    val warehouse: String?,
    val cargoType: String?,
    val totalTonnage: Float,
    val remainingTonnage: Float,
    val loadedTonnage: Float,
    val voucherCount: Int,
    val exitDates: List<ExitDateInfo>?,
    val isActive: Boolean,
    val shippingCompany: String,
    val percentage: Double? = null,
    val isPercentageRestricted: Boolean? = false,
    val cargoOwner: String? = null
)

data class ExitDateInfo(
    val date: String,
    val time: String
)

data class QuotaEditData(
    val quotaNumber: String,
    val shipName: String,
    val shippingCompany: String,
    val warehouse: String,
    val cargoType: String,
    val totalTonnage: Float
)

data class Warehouse(
    val name: String,
    val quotaCount: Int,
    val totalTonnage: Float,
    val loadedTonnage: Float,
    val remainingTonnage: Float,
    val quotas: List<Quota>,
    val availableExitDates: List<String>
)

data class QuotaDetails(
    val number: String,
    val totalTonnage: Float,
    val loadedTonnage: Float,
    val remainingTonnage: Float,
    val voucherCount: Int,
    val startDate: String,
    val endDate: String,
    val additionalInfo: String?
)

data class FilteredSummaryResponse(
    val totalNetWeight: Float,
    val voucherCount: Int,
    val voucherDetails: List<VoucherDetail>?
)

data class VoucherDetail(
    val trackingNumber: String,
    val entryTime: String,
    val netWeight: Float,
    val exitTime: String,
    val exitDate: String,
    val scaleReceiptNumber: String,
    val username: String?,
    val confirmUsername: String?
)

data class FilteredSummary(
    val totalNetWeight: Float,
    val voucherCount: Int,
    val voucherDetails: List<VoucherDetail>,
    val quotaNumber: String,
    val shipName: String,
    val startDate: String,
    val startTime: String,
    val endDate: String,
    val endTime: String
)

data class QuotaStatusResponse(
    val isActive: Boolean,
    val status: Boolean,
    val message: String,
    val details: QuotaStatusDetails? = null
)

data class QuotaStatusDetails(
    val quotaNumber: String,
    val shipName: String,
    val cargoType: String,
    val shippingCompany: String,
    val totalWeight: Float,
    val loadedWeight: Float,
    val remainingCapacity: Float,
    val percentageLoaded: Float,
    val existingQuotas: List<ExistingQuota>? = null
)

data class ExistingQuota(
    val quotaNumber: String,
    val shipName: String,
    val cargoType: String,
    val shippingCompany: String
)

data class ScaleReceiptCheckResponse(
    val exists: Boolean,
    val message: String
)

data class User(
    val id: Int,
    val username: String,
    val fullName: String? = null,
    val password: String? = null,
    val userType: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class UpdateUserRequest(
    val action: String = "updateUser",
    val id: Int,
    val username: String? = null,
    val fullName: String? = null,
    val password: String? = null,
    val userType: String? = null
)

data class DeleteUserRequest(
    val action: String = "deleteUser",
    val userId: Int
)

data class CreateUserRequest(
    val action: String = "createUser",
    val username: String,
    val fullName: String,
    val password: String,
    val userType: String
)

data class UserTypeInfo(
    val label: String,
    val description: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

data class FabItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)

data class UpdateInfo(
    val latestVersion: String,
    val downloadUrl: String,
    val minRequiredVersion: String = "1.0",
    val updatePriority: String = "normal",
    val updateMessage: String = "",
    val forceUpdate: Boolean = false,
    val updateSize: String = "0",
    val releaseDate: String = "",
    val minAndroidVersion: Int = 21,
    val minAppVersion: String = "1.0",
    val excludedVersions: List<String> = emptyList()
)

class ColorSelector(private val colors: List<Color>) {
    // تمام رنگ‌های اختصاص داده شده به هر شناسه
    private val assignedColors = mutableMapOf<String, Color>()
    // کلید مپ: رنگ، مقدار: آیا استفاده شده است؟
    private val usedColors = mutableMapOf<Color, Boolean>()
    private val random = java.util.Random(System.currentTimeMillis())

    init {
        // در شروع، همه رنگ‌ها به عنوان استفاده نشده علامت‌گذاری می‌شوند
        colors.forEach { usedColors[it] = false }
    }

    /**
     * تخصیص رنگ‌های متمایز به مجموعه‌ای از شناسه‌ها
     * @param identifiers مجموعه شناسه‌هایی که باید به آنها رنگ اختصاص داده شود
     * @return نگاشت از شناسه به رنگ اختصاص داده شده
     */
    fun assignDistinctColors(identifiers: Set<String>): Map<String, Color> {
        // اگر تعداد شناسه‌ها بیشتر از تعداد رنگ‌های موجود است، باید رنگ‌های جدید تولید کنیم
        if (identifiers.size > colors.size) {
            return assignColorsWithGeneration(identifiers)
        }

        // پاک کردن تمام رنگ‌های قبلی
        reset()

        val result = mutableMapOf<String, Color>()
        val availableColors = colors.toMutableList()

        // ابتدا شناسه‌هایی که قبلاً رنگی به آنها اختصاص داده شده را پردازش می‌کنیم
        // تا حد امکان همان رنگ‌های قبلی را حفظ کنیم
        identifiers.filter { assignedColors.containsKey(it) }.forEach { id ->
            val previousColor = assignedColors[id]
            if (previousColor != null && previousColor in availableColors) {
                result[id] = previousColor
                availableColors.remove(previousColor)
                usedColors[previousColor] = true
            }
        }

        // برای شناسه‌های باقیمانده، رنگ‌های جدید اختصاص می‌دهیم
        identifiers.filter { !result.containsKey(it) }.forEach { id ->
            if (availableColors.isNotEmpty()) {
                // اختصاص رنگ تصادفی از رنگ‌های باقیمانده
                val colorIndex = random.nextInt(availableColors.size)
                val selectedColor = availableColors[colorIndex]
                result[id] = selectedColor
                availableColors.removeAt(colorIndex)
                assignedColors[id] = selectedColor
                usedColors[selectedColor] = true
            }
        }

        return result
    }

    /**
     * وقتی تعداد شناسه‌ها بیشتر از تعداد رنگ‌های موجود است، رنگ‌های جدید تولید می‌کنیم
     */
    private fun assignColorsWithGeneration(identifiers: Set<String>): Map<String, Color> {
        val result = mutableMapOf<String, Color>()

        // برای هر شناسه، یک رنگ منحصر به فرد تولید می‌کنیم
        identifiers.forEachIndexed { index, id ->
            val color = if (index < colors.size) {
                // استفاده از رنگ‌های از پیش تعریف شده
                colors[index]
            } else {
                // تولید رنگ جدید با HSL برای اطمینان از تمایز
                val hue = (360f * index / identifiers.size) % 360f
                val saturation = 0.7f + (random.nextFloat() * 0.3f) // 0.7-1.0
                val lightness = 0.4f + (random.nextFloat() * 0.3f) // 0.4-0.7

                val hsl = floatArrayOf(hue, saturation, lightness)
                Color(ColorUtils.HSLToColor(hsl))
            }

            result[id] = color
            assignedColors[id] = color
        }

        return result
    }

    /**
     * گرفتن رنگ بعدی از رنگ‌های استفاده نشده
     * اگر تمام رنگ‌ها استفاده شده باشند، یک رنگ تصادفی برمی‌گرداند
     */
    fun getNextColor(): Color {
        // بررسی می‌کنیم آیا رنگ‌های استفاده نشده وجود دارند
        val unusedColors = usedColors.filter { !it.value }.keys.toList()

        if (unusedColors.isNotEmpty()) {
            // انتخاب یک رنگ استفاده نشده
            val selectedColor = unusedColors[random.nextInt(unusedColors.size)]
            usedColors[selectedColor] = true
            return selectedColor
        }

        // اگر تمام رنگ‌ها استفاده شده‌اند، یک رنگ را به صورت تصادفی انتخاب می‌کنیم
        return colors[random.nextInt(colors.size)]
    }

    /**
     * بازنشانی وضعیت استفاده از رنگ‌ها
     */
    fun reset() {
        colors.forEach { usedColors[it] = false }
    }
}

fun adjustColorForTheme(color: Color, isDarkTheme: Boolean): Color {
    val hsl = FloatArray(7)
    ColorUtils.colorToHSL(color.toArgb(), hsl)

    hsl[3] = if (isDarkTheme) {
        0.7f
    } else {
        0.4f
    }

    hsl[1] = 0.5f

    return Color(ColorUtils.HSLToColor(hsl))
}

val cardColors = listOf(
    // رنگ‌های بهینه شده برای تم روشن و تیره
    Color(0xFFEF5350), // Red 400 - ملایم‌تر از قرمز تند
    Color(0xFF66BB6A), // Green 400 - سبز متعادل
    Color(0xFF42A5F5), // Blue 400 - آبی ملایم
    Color(0xFFEC407A), // Pink 400 - صورتی متعادل
    Color(0xFF26C6DA), // Cyan 400 - فیروزه‌ای ملایم
    Color(0xFFAB47BC), // Purple 400 - بنفش متعادل
    Color(0xFF26A69A), // Teal 400 - سبز دریایی ملایم
    Color(0xFF8D6E63), // Brown 400 - قهوه‌ای ملایم
    Color(0xFF7E57C2), // Deep Purple 400 - بنفش عمیق ملایم
    Color(0xFF29B6F6), // Light Blue 400 - آبی روشن
    Color(0xFFFF7043), // Deep Orange 400 - نارنجی ملایم
    Color(0xFF9CCC65), // Light Green 400 - سبز روشن
    Color(0xFF5C6BC0), // Indigo 400 - نیلی ملایم
    Color(0xFFFFCA28), // Amber 400 - زرد کهربایی
    Color(0xFF78909C), // Blue Grey 400 - خاکستری آبی
    Color(0xFFA1887F), // Brown 300 - قهوه‌ای روشن
    Color(0xFFE57373), // Red 300 - قرمز روشن
    Color(0xFF81C784), // Green 300 - سبز روشن
    Color(0xFF64B5F6), // Blue 300 - آبی روشن
    Color(0xFFF06292), // Pink 300 - صورتی روشن
    Color(0xFF4DD0E1), // Cyan 300 - فیروزه‌ای روشن
    Color(0xFFBA68C8), // Purple 300 - بنفش روشن
    Color(0xFF4DB6AC), // Teal 300 - سبز دریایی روشن
    Color(0xFFA5A5A5), // Grey 400 - خاکستری متعادل

    // رنگ‌های تکمیلی بهینه شده
    Color(0xFF90CAF9), // Blue 200 - آبی خیلی ملایم
    Color(0xFFA5D6A7), // Green 200 - سبز خیلی ملایم
    Color(0xFFFFAB91), // Deep Orange 200 - نارنجی ملایم
    Color(0xFFCE93D8), // Purple 200 - بنفش ملایم
    Color(0xFF80DEEA), // Cyan 200 - فیروزه‌ای ملایم
    Color(0xFFFFF59D), // Yellow 200 - زرد ملایم
    Color(0xFFBCAAA4), // Brown 200 - قهوه‌ای ملایم
    Color(0xFFB39DDB), // Deep Purple 200 - بنفش عمیق ملایم
    Color(0xFF81D4FA), // Light Blue 200 - آبی روشن ملایم
    Color(0xFFC5E1A5), // Light Green 200 - سبز روشن ملایم
    Color(0xFF9FA8DA), // Indigo 200 - نیلی ملایم
    Color(0xFFFFE082), // Amber 200 - کهربایی ملایم
    Color(0xFFB0BEC5), // Blue Grey 200 - خاکستری آبی ملایم
    Color(0xFFD7CCC8), // Brown 100 - قهوه‌ای خیلی ملایم
    Color(0xFFFFCDD2), // Red 100 - قرمز خیلی ملایم
    Color(0xFFC8E6C9), // Green 100 - سبز خیلی ملایم
    Color(0xFFBBDEFB), // Blue 100 - آبی خیلی ملایم
    Color(0xFFF8BBD9), // Pink 100 - صورتی خیلی ملایم
    Color(0xFFB2EBF2), // Cyan 100 - فیروزه‌ای خیلی ملایم
    Color(0xFFE1BEE7)  // Purple 100 - بنفش خیلی ملایم
)

fun Float.toTon(): Int = (this / 1000).toInt()

sealed class LoadingState {
    object Idle : LoadingState()
    data class Error(val message: String) : LoadingState()
}

data class ComprehensiveAnalysisResponse(
    val success: Boolean,
    val data: AnalyticsData
)

data class AnalyticsData(
    val peakHoursAnalysis: List<PeakHourAnalysis>?,
    val shiftPerformanceAnalysis: List<ShiftPerformanceResponse>?,
    val quotaCompletionAnalysis: List<QuotaCompletionAnalysis>?,
    val quotaProgressAnalysis: List<QuotaProgressAnalysis>?,
    val quotaPredictionAnalysis: List<QuotaPredictionAnalysis>?,
    val carrierPerformanceAnalysis: List<CarrierPerformanceAnalysis>?,
    val warehouseEfficiencyAnalysis: List<WarehouseEfficiencyAnalysis>?,
    val warehouseSpeedAnalysis: List<WarehouseSpeedAnalysis>?,
    val warehouseTrafficAnalysis: List<WarehouseTrafficAnalysis>?,
    val warehousePeakAnalysis: List<WarehousePeakAnalysis>?,
    val cargoOwnerAnalysis: List<CargoOwnerAnalysis>?
)

data class ComprehensiveAnalytics(
    val peakHoursAnalysis: List<PeakHourData> = emptyList(),
    val shiftPerformanceAnalysis: List<ShiftPerformanceData> = emptyList(),
    val quotaCompletionAnalysis: List<QuotaCompletionData> = emptyList(),
    val quotaProgressAnalysis: List<QuotaProgressData> = emptyList(),
    val quotaPredictionAnalysis: List<QuotaPredictionData> = emptyList(),
    val carrierPerformanceAnalysis: List<CarrierPerformanceAnalysis> = emptyList(),
    val warehouseEfficiencyAnalysis: List<WarehouseEfficiencyData> = emptyList(),
    val warehouseSpeedAnalysis: List<WarehouseSpeedData> = emptyList(),
    val warehouseTrafficAnalysis: List<WarehouseTrafficData> = emptyList(),
    val warehousePeakAnalysis: List<WarehousePeakData> = emptyList(),
    val cargoOwnerAnalysis: List<CargoOwnerData> = emptyList()
)

data class PeakHourAnalysis(
    val hour: Int,
    val total_operations: Int,
    val entries: Int,
    val exits: Int,
    val percentage: Float
)

data class PeakHourData(
    val hour: Int,
    val total_operations: Int,
    val entries: Int,
    val exits: Int,
    val percentage: Float
)

data class ShiftPerformanceResponse(
    val shift: String,
    val total_operations: Int,
    val total_vouchers: Int,
    val active_carriers_list: String,
    val avg_completion_time: Float,
    val completed_operations: Int,
    val total_weight_tons: Float,
    val peak_hour: Int?,
    val peak_hour_operations: Int?,
    val peak_hour_vouchers: Int?,
    val peak_hour_detail: String?,
    val avg_weight_per_operation: Float,
    val delayed_operations_detail: String?,
    val completion_rate: Float,
    val efficiency_score: Float,
    val total_active_carriers: Int,
    val total_delayed_operations: Int,
    val weight_standard_deviation: Float
)

data class ShiftPerformanceData(
    val shift: String,
    val total_operations: Int,
    val total_vouchers: Int,
    val active_carriers_list: String,
    val avg_completion_time: Float,
    val completed_operations: Int,
    val total_weight_tons: Float,
    val peak_hour: Int?,
    val peak_hour_operations: Int?,
    val peak_hour_vouchers: Int?,
    val peak_hour_detail: String?,
    val avg_weight_per_operation: Float,
    val delayed_operations_detail: String?,
    val completion_rate: Float,
    val efficiency_score: Float,
    val total_active_carriers: Int,
    val weight_standard_deviation: Float
)

data class QuotaCompletionAnalysis(
    val loadingQuotaNumber: String,
    val total_quota_weight: Float,
    val total_vouchers: Int,
    val completion_percentage: Float,
    val avg_completion_hours: Float,
    val shipName: String,
    val shippingCompany: String,
    val last_24h_weight: Float,
    val last_24h_vouchers: Int
)

data class QuotaProgressAnalysis(
    val loadingQuotaNumber: String,
    val exitDate: String,
    val daily_vouchers: Int,
    val daily_weight: Float,
    val cumulative_completion: Float
)

data class QuotaPredictionAnalysis(
    val loadingQuotaNumber: String,
    val daily_rate: Float,
    val total_completed_weight: Float,
    val total_weight: Float,
    val estimated_days_remaining: Float
)

data class QuotaCompletionData(
    val loadingQuotaNumber: String,
    val total_quota_weight: Float,
    val total_vouchers: Int,
    val completion_percentage: Float,
    val avg_completion_hours: Float,
    val shipName: String,
    val shippingCompany: String,
    val last_24h_weight: Float,
    val last_24h_vouchers: Int
)

data class QuotaProgressData(
    val loadingQuotaNumber: String,
    val exitDate: String,
    val daily_vouchers: Int,
    val daily_weight: Float,
    val cumulative_completion: Float
)

data class QuotaPredictionData(
    val loadingQuotaNumber: String,
    val daily_rate: Float,
    val total_completed_weight: Float,
    val total_weight: Float,
    val estimated_days_remaining: Float
)

data class CarrierPerformanceAnalysis(
    val shippingCompany: String,
    val total_deliveries: Int,
    val completed_deliveries: Int,
    val total_quotas: Int,
    val completed_quotas: Int,
    val completed_quota_numbers: String?,
    val avg_net_weight: Float,
    val min_weight: Float,
    val max_weight: Float,
    val avg_operation_time: Float,
    val operations_per_hour: Float,
    val peak_hour: Int,
    val peak_hour_operations: Int,
    val quality_score: Float,
    val performance_category: String
)

data class WarehouseEfficiencyData(
    val loadingWarehouse: String,
    val active_quotas: Int,
    val total_operations: Int,
    val total_processed_weight: Float,
    val daily_throughput: Float,
    val daily_operations: Float,
    val operation_percentage: Float
)

data class WarehouseSpeedData(
    val loadingWarehouse: String,
    val total_operations: Int,
    val avg_processing_minutes: Float,
    val completed_operations: Int,
    val avg_processed_weight: Float,
    val completion_rate: Float,
    val weight_per_minute: Float
)

data class WarehouseTrafficData(
    val loadingWarehouse: String,
    val hour: Int,
    val entries: Int,
    val exits: Int,
    val hour_percentage: Float,
    val avg_processed_weight: Float
)

data class WarehousePeakData(
    val loadingWarehouse: String,
    val hour: Int,
    val operation_count: Int,
    val avg_weight: Float,
    val period_percentage: Float,
    val activity_level: String
)

data class WarehouseEfficiencyAnalysis(
    val loadingWarehouse: String,
    val active_quotas: Int,
    val total_operations: Int,
    val total_processed_weight: Float,
    val daily_throughput: Float,
    val daily_operations: Float,
    val operation_percentage: Float
)

data class WarehouseSpeedAnalysis(
    val loadingWarehouse: String,
    val total_operations: Int,
    val avg_processing_minutes: Float,
    val completed_operations: Int,
    val avg_processed_weight: Float,
    val completion_rate: Float,
    val weight_per_minute: Float
)

data class WarehouseTrafficAnalysis(
    val loadingWarehouse: String,
    val hour: Int,
    val entries: Int,
    val exits: Int,
    val hour_percentage: Float,
    val avg_processed_weight: Float
)

data class WarehousePeakAnalysis(
    val loadingWarehouse: String,
    val hour: Int,
    val operation_count: Int,
    val avg_weight: Float,
    val period_percentage: Float,
    val activity_level: String
)

enum class QuotaGroupingMode {
    BY_SHIP,
    BY_CARRIER
}

enum class WarehouseQuotaGroupingMode {
    BY_SHIPPING_COMPANY,
    BY_CARGO_OWNER,
    BY_WAREHOUSE
}

enum class QuotaSortingMode {
    REMAINING_TONNAGE_ASC,
    REMAINING_TONNAGE_DESC
}

enum class GroupSortingMode {
    ALPHABETICAL,
    REMAINING_TONNAGE_ASC,
    REMAINING_TONNAGE_DESC
}

enum class ShipSortingMode {
    REMAINING_TONNAGE_ASC,
    REMAINING_TONNAGE_DESC,
    LOADED_TONNAGE_ASC,
    LOADED_TONNAGE_DESC,
    NAME_ASC,
    NAME_DESC
}

data class CargoOwnerAnalysis(
    val shipName: String,
    val owner_count: Int,
    val total_vouchers: Int,
    val total_net_weight: Float,
    val owners: List<CargoOwnerDetailsData>
)

data class CargoOwnerDetailsData(
    val cargoOwner: String,
    val voucher_count: Int,
    val net_weight: Float,
    val quota_count: Int
)

data class CargoOwnerData(
    val shipName: String,
    val owner_count: Int,
    val total_vouchers: Int,
    val total_net_weight: Float,
    val owners: List<CargoOwnerDetailsData>
)

data class LoadableTonnageResponse(
    val success: Boolean,
    val loadableTonnage: Float?,
    val remainingTonnage: Float?,
    val totalTonnage: Float?,
    val loadedTonnage: Float?,
    val percentage: Float?,
    val isPercentageRestricted: Boolean?,
    val trucks18Wheeler: Int?,
    val trucks10Wheeler: Int?,
    val message: String?
)

data class QuotaItem(
    val number: String,
    val shipName: String,
    val warehouse: String,
    val cargoType: String,
    val shippingCompany: String,
    val cargoOwner: String,
    val isActive: Boolean,
    val temporaryTonnageEnabled: Boolean,
    val temporaryTonnageValue: Float?,
    val quotaKey: String
)

data class QuotaTonnageWarning(
    val shipName: String,
    val cargoOwner: String,
    val quotaNumber: String,
    val currentRemaining: String,
    val voucherCount: String,
    val remainingAfterExit: String,
    val isNegative: Boolean,
    val isActive: Boolean = false
)

fun formatNumber(number: Number): String {
    return NumberFormat.getNumberInstance(Locale("en", "US")).format(number)
}