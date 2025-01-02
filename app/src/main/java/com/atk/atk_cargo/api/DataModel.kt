package com.atk.atk_cargo.api

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.os.Environment
import android.os.Parcelable
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.ColumnText
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.patrykandpatrick.vico.core.extension.sumOf
import kotlinx.coroutines.Dispatchers
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
import java.io.File
import java.io.FileNotFoundException
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.LinkedList
import java.util.Locale
import java.util.Queue
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
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
    val cargoCount: StateFlow<Int> = _cargoCount.asStateFlow()
    private val _clearInputFields = MutableStateFlow(false)
    val clearInputFields: StateFlow<Boolean> = _clearInputFields.asStateFlow()
    private fun MutableStateFlow<List<CargoInfo>>.update(function: (List<CargoInfo>) -> List<CargoInfo>) {
        this.value = function(this.value)
    }
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
    val totalServices: StateFlow<String> = _totalServices.asStateFlow()
    private val _resultMessage = MutableStateFlow("")
    val resultMessage: StateFlow<String> = _resultMessage.asStateFlow()
    private val _showAnimatedMessage = MutableStateFlow(false)
    val showAnimatedMessage: StateFlow<Boolean> = _showAnimatedMessage.asStateFlow()
    private val _messageType = MutableStateFlow(MessageType.SUCCESS)
    val messageType: StateFlow<MessageType> = _messageType.asStateFlow()
    private val _showNetWeightDialog = MutableStateFlow(false)
    val showNetWeightDialog: StateFlow<Boolean> = _showNetWeightDialog.asStateFlow()
    private val _filteredCargoInfoList = MutableStateFlow<List<CargoInfo>>(emptyList())
    val filteredCargoInfoList: StateFlow<List<CargoInfo>> = _filteredCargoInfoList.asStateFlow()
    private val _isQuotaActive = MutableStateFlow<Boolean?>(null)
    private val _shownWarningForQuotas = mutableSetOf<String>()
    private val _pendingMessages = MutableStateFlow<Queue<Pair<String, MessageType>>>(LinkedList())
    private val _isShowingMessage = MutableStateFlow(false)

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

    private fun showUpdateMessage(message: String, type: MessageType) {
        _snackbarMessage.value = SnackbarMessage(message, type)
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

    @SuppressLint("SimpleDateFormat")
    fun isWithinExitTimeRange(exitDateStr: String, exitTimeStr: String): Boolean {
        val currentJalaliDate = getCurrentDate()
        val currentTime = LocalTime.now()

        val exitDateTime = LocalDateTime.parse("$exitDateStr $exitTimeStr", DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))
        val exitTime = exitDateTime.toLocalTime()

        val morningStart = LocalTime.of(7, 30)
        val eveningEnd = LocalTime.of(18, 30)
        val nightStart = LocalTime.of(19, 30)
        val nightEnd = LocalTime.of(7, 30)

        return when (currentTime) {
            in morningStart..eveningEnd -> {
                exitDateStr == currentJalaliDate && exitTime in morningStart..currentTime
            }
            in nightStart..LocalTime.MAX -> {
                exitDateStr == currentJalaliDate && exitTime in nightStart..currentTime
            }
            in LocalTime.MIN..nightEnd -> {
                val yesterdayCalendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }
                val yesterdayJalaliDate = JalaliCalendar(yesterdayCalendar).toString()
                (exitDateStr == yesterdayJalaliDate && exitTime >= nightStart) ||
                        (exitDateStr == currentJalaliDate && exitTime <= currentTime)
            }
            else -> false
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

    fun refreshCargoInfo() {
        viewModelScope.launch {
            _initialInfo.value?.let { info ->
                try {
                    checkQuotaStatus(info)
                    loadCargoInfoList(
                        quotaNumber = info.loadingQuotaNumber.toString(),
                        shippingCompany = info.shippingCompany,
                        warehouse = info.loadingWarehouse,
                        cargoType = info.cargoType,
                        onProgress = {
                        },
                        onComplete = {
                            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                            showUpdateMessage(
                                "اطلاعات در ساعت $currentTime به‌روزرسانی شد",
                                MessageType.SUCCESS
                            )
                        }
                    )
                } catch (e: Exception) {
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
                val initialInfo = _initialInfo.value
                _initialInfo.value?.let { info ->
                    checkAndHandleQuotaPercentage(info.loadingQuotaNumber.toString())
                }
                if (initialInfo == null) {
                    _resultMessage.value = "اطلاعات اولیه در دسترس نیست"
                    _showAnimatedMessage.value = true
                    _messageType.value = MessageType.ERROR
                    return@launch
                }

                checkQuotaStatus(initialInfo)

                if (_isQuotaActive.value != true) {
                    val message = if (_messageType.value == MessageType.WARNING) {
                        "امکان ثبت حواله برای این کوتاژ وجود ندارد. لطفاً وضعیت کوتاژ را بررسی کنید."
                    } else {
                        "کوتاژ غیرفعال است و امکان ثبت حواله جدید وجود ندارد"
                    }
                    _resultMessage.value = message
                    _showAnimatedMessage.value = true
                    _messageType.value = MessageType.ERROR
                    return@launch
                }

                // بررسی اینکه آیا حواله جدید است یا خیر
                val isNewCargo = _cargoInfoList.value.none { it.trackingNumber == trackingNumber }

                // بررسی اعتبار داده‌های ورودی
                if (!isValidInput(
                        trackingNumber,
                        netWeight,
                        numberOfPeople,
                        shortageWeight,
                        excessWeight,
                        isNewCargo
                    )
                ) {
                    _resultMessage.value = "داده‌های ورودی نامعتبر است."
                    _showAnimatedMessage.value = true
                    _messageType.value = MessageType.ERROR
                    return@launch
                }

                // دریافت نام کاربری و نوع کاربر از UserPreferencesManager
                val username = userPreferencesManager.username.first()
                val userType = userPreferencesManager.userType.first()

                if (username.isBlank() || userType.isBlank()) {
                    _resultMessage.value = "اطلاعات کاربری در دسترس نیست. لطفاً دوباره وارد شوید."
                    _showAnimatedMessage.value = true
                    _messageType.value = MessageType.ERROR
                    return@launch
                }

                // ایجاد شیء CargoInfo
                val cargoInfo = CargoInfo(
                    trackingNumber = trackingNumber,
                    numberOfPeople = numberOfPeople,
                    username = username,
                    userType = userType,
                    entryTime = getCurrentTime(),
                    netWeight = netWeight,
                    scaleReceiptNumber = scaleReceiptNumber,
                    shortageWeight = shortageWeight,
                    excessWeight = excessWeight,
                    exitTime = if (netWeight.isBlank()) null else getCurrentTime(),
                    exitDate = if (netWeight.isBlank()) null else getCurrentDate(),
                    status = if (netWeight.isBlank()) "ورود" else "خروج",
                    shipName = initialInfo.shipName,
                    loadingWarehouse = initialInfo.loadingWarehouse,
                    cargoType = initialInfo.cargoType,
                    shippingCompany = initialInfo.shippingCompany,
                    loadingQuotaNumber = initialInfo.loadingQuotaNumber.toString(),
                    confirm = "",
                    confirmation = "no"
                )

                // ارسال اطلاعات به سرور
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
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorCode = response.code()
                    val parsedError = parseErrorResponse(errorBody)
                    if (parsedError != null) {
                        handleErrorResponse(parsedError)
                    } else {
                        _resultMessage.value = "خطا در ارسال اطلاعات بار: کد وضعیت $errorCode"
                        _showAnimatedMessage.value = true
                        _messageType.value = MessageType.ERROR
                    }
                }
            } catch (e: Exception) {
                _resultMessage.value = "خطا در ثبت اطلاعات بار: ${e.message}"
                _showAnimatedMessage.value = true
                _messageType.value = MessageType.ERROR
            }
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

        _initialInfo.value?.let { info ->
            loadCargoInfoList(
                quotaNumber = info.loadingQuotaNumber.toString(),
                shippingCompany = info.shippingCompany,
                warehouse = info.loadingWarehouse,
                cargoType = info.cargoType,
                onProgress = {},
                onComplete = {}
            )
        } ?: run {
            Log.e("CargoViewModel", "Unable to reload cargo info: Initial info is null")
        }
    }

    private fun parseErrorResponse(errorBody: String?): SaveOrUpdateResponse? {
        return try {
            Gson().fromJson(errorBody, SaveOrUpdateResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun checkQuotaStatus(initialInfo: InitialInfo) {
        try {
            val status = repository.checkQuotaStatus(
                quotaNumber = initialInfo.loadingQuotaNumber.toString(),
                shipName = initialInfo.shipName,
                cargoType = initialInfo.cargoType,
                shippingCompany = initialInfo.shippingCompany
            )

            if (status.isActive) {
                // اگر کوتاژ فعال است، بررسی وضعیت درصد
                checkAndHandleQuotaPercentage(initialInfo.loadingQuotaNumber.toString())
            }

            _isQuotaActive.value = status.isActive

            if (!status.status) {
                // پیام غیرفعال بودن بعد از هشدار درصدی نمایش داده می‌شود
                delay(5000) // تاخیر بیشتر از هشدار درصدی
                showMessage(status.message, MessageType.WARNING)
                return
            }
        } catch (e: Exception) {
            Log.e("CargoViewModel", "Error in checkQuotaStatus", e)
            _resultMessage.value = "خطا در بررسی وضعیت کوتاژ: ${e.message ?: "خطای ناشناخته"}"
            _messageType.value = MessageType.ERROR
            _showAnimatedMessage.value = true
            _isQuotaActive.value = false
        }
    }

    fun resetClearInputFields() {
        _clearInputFields.value = false
    }

    private fun isValidInput(
        trackingNumber: String,
        netWeight: String,
        numberOfPeople: String,
        shortageWeight: String,
        excessWeight: String,
        isNewCargo: Boolean
    ): Boolean {
        // شماره حواله همیشه باید وارد شود
        if (trackingNumber.isBlank()) return false

        // اگر حواله جدید است، تعداد نفرات باید یک عدد مثبت باشد
        if (isNewCargo) {
            val peopleCount = numberOfPeople.toIntOrNull()
            if (peopleCount == null || peopleCount <= 0) return false
        }

        // بررسی وزن خالص (اگر وارد شده باشد)
        if (netWeight.isNotBlank() && netWeight.toDoubleOrNull() == null) return false

        // بررسی کسری بار (اگر وارد شده باشد)
        if (shortageWeight.isNotBlank() && shortageWeight.toDoubleOrNull() == null) return false

        // بررسی اضافه بار (اگر وارد شده باشد)
        if (excessWeight.isNotBlank() && excessWeight.toDoubleOrNull() == null) return false

        return true
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
        return !(firstTwoDigits != "39" && firstTwoDigits != "40" && firstTwoDigits != "41")
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
        onProgress: (Float) -> Unit,
        onComplete: () -> Unit,
        showLoadingDialog: Boolean = true
    ) {
        viewModelScope.launch {
            try {
                if (showLoadingDialog) onProgress(0.05f) // شروع با 5%

                // دریافت اطلاعات کارگو
                val cargoInfoResponse = repository.getCargoInfo(
                    quotaNumber = quotaNumber,
                    shippingCompany = shippingCompany,
                    warehouse = warehouse,
                    cargoType = cargoType
                )

                if (showLoadingDialog) onProgress(0.15f) // دریافت اطلاعات اولیه 15%

                // بررسی تطابق نوع بار
                if (cargoInfoResponse.initialInfo.cargoType != cargoType) {
                    handleError(
                        message = "خطا: نوع بار انتخاب شده با اطلاعات کوتاژ مطابقت ندارد",
                        type = MessageType.ERROR,
                        onComplete = onComplete
                    )
                    return@launch
                }

                if (showLoadingDialog) onProgress(0.25f) // بررسی تطابق 25%

                // ذخیره و بررسی اطلاعات اولیه
                handleInitialInfo(cargoInfoResponse.initialInfo)

                if (showLoadingDialog) onProgress(0.35f) // پردازش اطلاعات اولیه 35%

                // تنظیم مقادیر اولیه
                setInitialValues(cargoInfoResponse.initialInfo)

                if (showLoadingDialog) onProgress(0.45f) // تنظیم مقادیر اولیه 45%

                // پردازش و فیلتر لیست کارگو
                val totalItems = cargoInfoResponse.cargoInfoList.size
                val filteredList = filterCargoList(cargoInfoResponse.cargoInfoList, cargoType)

                if (showLoadingDialog) onProgress(0.55f) // فیلتر کردن لیست 55%

                // بروزرسانی تدریجی لیست کارگو
                updateCargoListInChunks(
                    filteredList = filteredList,
                    totalItems = totalItems,
                    showLoadingDialog = showLoadingDialog,
                    onProgress = onProgress
                )

                if (showLoadingDialog) onProgress(0.85f) // پردازش چانک‌ها 85%

                // بروزرسانی نهایی مقادیر و فیلترها
                updateFinalValues()

                if (showLoadingDialog) onProgress(1f) // اتمام 100%

                onComplete()

                val currentQuotas = _cargoInfoList.value
                currentQuotas.forEach { cargoInfo ->
                    val quota = repository.getShipQuotas(cargoInfo.shipName)
                        .find { it.number == cargoInfo.loadingQuotaNumber }

                    quota?.let {
                        checkQuotaPercentage(it)
                    }
                }

            } catch (e: Exception) {
                handleError(
                    message = "خطا در بارگیری اطلاعات: ${e.message}",
                    type = MessageType.ERROR,
                    onComplete = onComplete,
                    error = e
                )
            }
        }
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
                    _initialInfo.value?.let { info ->
                        loadCargoInfoList(
                            quotaNumber = info.loadingQuotaNumber.toString(),
                            shippingCompany = info.shippingCompany,
                            warehouse = info.loadingWarehouse,
                            cargoType = info.cargoType,
                            onProgress = {},
                            onComplete = {}
                        )
                    }
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
                        delay(5000)
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
                                delay(5000)

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
                    // بعد از موفقیت در ثبت خروج، وضعیت درصد را چک می‌کنیم
                    _initialInfo.value?.let { info ->
                        val quotas = repository.getShipQuotas(info.shipName)
                        quotas.find { it.number == cargoInfo.loadingQuotaNumber }?.let {
                            checkQuotaPercentage(it)
                        }
                    }

                    _resultMessage.value = "اطلاعات بروزرسانی شد"
                    _showAnimatedMessage.value = true
                    _messageType.value = MessageType.SUCCESS

                    refreshCargoInfo()
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

    private fun handleError(
        message: String,
        type: MessageType,
        onComplete: () -> Unit,
        error: Exception? = null
    ) {
        error?.let { Log.e("CargoViewModel", "Error in loadCargoInfoList", it) }
        _resultMessage.value = message
        _showAnimatedMessage.value = true
        _messageType.value = type
        onComplete()
    }

    private suspend fun handleInitialInfo(initialInfo: InitialInfo) {
        _initialInfo.value = initialInfo
        initialInfo.let { info ->
            checkQuotaStatus(info)
            if (_isQuotaActive.value != true) {
                _resultMessage.value = "کوتاژ غیرفعال است و امکان ثبت اطلاعات وجود ندارد!"
                _showAnimatedMessage.value = true
                _messageType.value = MessageType.WARNING
            }
        }
    }

    private fun setInitialValues(initialInfo: InitialInfo) {
        _cargoWeight.value = DecimalFormat("#,###").format(initialInfo.cargoWeight.roundToInt())
        _cargoCount.value = initialInfo.totalVoucherCount
        _totalServices.value = initialInfo.totalVoucherCount.toString()
    }

    private fun filterCargoList(cargoList: List<CargoInfo>, cargoType: String): List<CargoInfo> {
        _cargoInfoList.update { emptyList() }
        return cargoList.filter { cargo -> cargo.cargoType == cargoType }
    }

    private fun updateCargoListInChunks(
        filteredList: List<CargoInfo>,
        totalItems: Int,
        showLoadingDialog: Boolean,
        onProgress: (Float) -> Unit
    ) {
        val startProgress = 0.55f
        val endProgress = 0.85f
        val progressRange = endProgress - startProgress

        filteredList.chunked(10).forEachIndexed { chunkIndex, chunk ->
            _cargoInfoList.update { currentList -> currentList + chunk }

            if (showLoadingDialog) {
                val chunkProgress = (chunkIndex + 1).toFloat() / ((totalItems + 9) / 10)
                val currentProgress = startProgress + (progressRange * chunkProgress)
                onProgress(currentProgress.coerceIn(0f, endProgress))
            }
        }
    }

    private fun updateFinalValues() {
        updateInfoValues()
        filterCargoInfoList("")
    }

    fun updateInfoValues() {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                try {
                    val netWeights = _cargoInfoList.value
                        .filter { it.status == "خروج" }
                        .mapNotNull { it.netWeight.toFloatOrNull() }
                    val totalNet = netWeights.sum()
                    val formattedTotalNet = DecimalFormat("#,###").format(totalNet.roundToInt())
                    val cargoWeightValue = _cargoWeight.value.replace(",", "").toFloatOrNull() ?: 0f
                    val remaining = (cargoWeightValue - totalNet).coerceAtLeast(0f)
                    val averageNet = if (netWeights.isNotEmpty()) netWeights.average() else 0.0

                    _remainingWeight.value = DecimalFormat("#,###").format(remaining.roundToInt())
                    _loadedWeight.value = DecimalFormat("#,###").format(totalNet.roundToInt())
                    _totalNetWeight.value = formattedTotalNet
                    _remainingWeight.value = DecimalFormat("#,###").format(remaining.roundToInt())
                    _averageNetWeight.value = DecimalFormat("#,###").format(averageNet.roundToInt())
                    _remainingServices.value = if (averageNet > 0) (remaining / averageNet).toInt().toString() else "0"
                    _totalServices.value = _cargoCount.value.toString()
                } catch (e: Exception) {
                    Log.e("CargoViewModel", "Error in updateInfoValues: ${e.message}")
                }
            }
        }
    }

    fun deleteCargo(cargoInfoRequest: CargoInfoRequest, password: String) {
        viewModelScope.launch {
            try {
                val passwordResponse =
                    apiService.checkPassword(password, "delete_info")
                if (passwordResponse.isSuccessful && passwordResponse.body()?.success == true) {
                    val response = apiService.deleteCargo(cargoInfoRequest)
                    if (response.isSuccessful) {
                        _resultMessage.value = "حواله با موفقیت حذف شد."
                        _showAnimatedMessage.value = true
                        _messageType.value = MessageType.SUCCESS

                        _initialInfo.value?.let { info ->
                            loadCargoInfoList(
                                quotaNumber = info.loadingQuotaNumber.toString(),
                                shippingCompany = info.shippingCompany,
                                warehouse = info.loadingWarehouse,
                                cargoType = info.cargoType,
                                onProgress = {},
                                onComplete = {}
                            )
                        }
                    } else {
                        _resultMessage.value = "خطا در حذف حواله: ${response.errorBody()?.string()}"
                        _showAnimatedMessage.value = true
                        _messageType.value = MessageType.ERROR
                    }
                } else {
                    _resultMessage.value =
                        passwordResponse.body()?.message ?: "خطا در بررسی رمز عبور"
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
}

class ReportsViewModel(
    private val repository: ReportsRepository,
    application: Application
) : AndroidViewModel(application) {
    private val _realTimeLoadingData = MutableStateFlow<List<RealTimeLoadingData>>(emptyList())
    val realTimeLoadingData: StateFlow<List<RealTimeLoadingData>> = _realTimeLoadingData
    private val _loadingError = MutableStateFlow<String?>(null)
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
    private val _messageSendingStatus = MutableStateFlow<MessageSendingStatus>(MessageSendingStatus.Idle)
    val messageSendingStatus: StateFlow<MessageSendingStatus> = _messageSendingStatus
    private val _shipColorMap = MutableStateFlow<Map<String, Color>>(emptyMap())
    val shipColorMap: StateFlow<Map<String, Color>> = _shipColorMap.asStateFlow()
    private val _quotaColorMap = MutableStateFlow<Map<String, Color>>(emptyMap())
    val quotaColorMap: StateFlow<Map<String, Color>> = _quotaColorMap.asStateFlow()
    private val colorSelector = ColorSelector(cardColors)
    private val _snackbarMessages = MutableSharedFlow<String>()
    val snackbarMessages = _snackbarMessages.asSharedFlow()
    private fun showSnackbar(message: String) {
        viewModelScope.launch {
            _snackbarMessages.emit(message)
        }
    }
    private val _exportResult = MutableStateFlow<String?>(null)
    private val _voucherAnalytics = MutableStateFlow<VoucherAnalytics?>(null)
    val voucherAnalytics: StateFlow<VoucherAnalytics?> = _voucherAnalytics.asStateFlow()
    private val _warehouseAnalytics = MutableStateFlow<List<WarehouseAnalytics>>(emptyList())
    val warehouseAnalytics: StateFlow<List<WarehouseAnalytics>> = _warehouseAnalytics.asStateFlow()
    private val _shipAnalytics = MutableStateFlow<List<ShipAnalytics>>(emptyList())
    val shipAnalytics: StateFlow<List<ShipAnalytics>> = _shipAnalytics.asStateFlow()
    private val _companyAnalytics = MutableStateFlow<List<ShippingCompanyAnalytics>>(emptyList())
    val companyAnalytics: StateFlow<List<ShippingCompanyAnalytics>> = _companyAnalytics.asStateFlow()
    private val _quotaAnalytics = MutableStateFlow<List<QuotaAnalytics>>(emptyList())
    val quotaAnalytics: StateFlow<List<QuotaAnalytics>> = _quotaAnalytics.asStateFlow()
    private val analytics = LoadingAnalytics()
    private val _comprehensiveAnalytics = MutableStateFlow<ComprehensiveAnalytics?>(null)
    val comprehensiveAnalytics: StateFlow<ComprehensiveAnalytics?> = _comprehensiveAnalytics.asStateFlow()
    private val _analyticsLoadingState = MutableStateFlow<LoadingState>(LoadingState.Idle)
    val analyticsLoadingState: StateFlow<LoadingState> = _analyticsLoadingState.asStateFlow()

    init {
        loadShips()
    }

    fun updateAnalytics(loadingData: List<RealTimeLoadingData>) {
        viewModelScope.launch {
            _voucherAnalytics.value = analytics.analyzeVouchers(loadingData)
            _warehouseAnalytics.value = analytics.analyzeWarehouses(loadingData)
            _shipAnalytics.value = analytics.analyzeShips(loadingData)
            _companyAnalytics.value = analytics.analyzeShippingCompanies(loadingData)
            _quotaAnalytics.value = analytics.analyzeQuotas(loadingData)
        }
    }

    fun formatNumber(number: Number): String {
        return NumberFormat.getNumberInstance(Locale("en", "US")).format(number)
    }

    fun formatPercentage(value: Float): String {
        return "%.1f%%".format(value)
    }

    fun formatWeight(weightKg: Float): String {
        return when {
            weightKg >= 1_000_000 -> "%.1f هزار تن".format(weightKg / 1_000_000)
            weightKg >= 1_000 -> "%.1f تن".format(weightKg / 1_000)
            else -> "${weightKg.toInt()} کیلوگرم"
        }
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

                // ایجاد نقشه رنگ‌ها برای کشتی‌ها و کوتاژها
                val newColorMap = response.data.groupBy { it.shipName }.mapValues { (_, data) ->
                    val shipColor = _shipColorMap.value.getOrElse(data.first().shipName) {
                        colorSelector.getNextColor()
                    }
                    val adjustedColor = adjustColorForTheme(shipColor, isDarkTheme)
                    Pair(adjustedColor, adjustedColor) // رنگ یکسان برای کشتی و کوتاژها
                }

                _shipColorMap.value = newColorMap.mapValues { it.value.first }
                _quotaColorMap.value = response.data.associate {
                    it.loadingQuotaNumber to (newColorMap[it.shipName]?.second ?: adjustColorForTheme(defaultColor, isDarkTheme))
                }
            } catch (e: Exception) {
                _loadingError.value = "خطا در دریافت اطلاعات: ${e.message}"
            }
        }
    }

    fun calculateWeightDetails(loadingData: List<RealTimeLoadingData>): Map<String, Map<String, Float>> {
        return loadingData.groupBy { it.shipName }
            .mapValues { (_, shipData) ->
                shipData.groupBy { it.loadingWarehouse }
                    .mapValues { (_, warehouseData) ->
                        warehouseData.sumOf { it.totalNetWeight.toDouble().toFloat() }
                    }
            }
    }

    fun loadShipDetails(shipName: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val shipDetails = repository.getShipDetails(shipName)
                _selectedShip.value = shipDetails
                _currentShipName.value = shipName
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error("خطا در بارگیری جزئیات کشتی: ${e.message}")
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
            _uiState.value = UiState.Loading
            try {
                val quotas = repository.getShipQuotas(shipName)
                _selectedShipQuotas.value = quotas
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error("خطا در بارگیری کوتاژهای کشتی: ${e.message}")
            }
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
                    // بارگذاری مجدد اطلاعات کشتی با نام جدید
                    loadShipDetails(newQuotaData.shipName)
                    // بارگذاری مجدد لیست کوتاژها با نام جدید
                    loadShipQuotas(newQuotaData.shipName)
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
                    // بروزرسانی لیست کوتاژها
                    _currentShipName.value?.let { shipName ->
                        loadShipQuotas(shipName)
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

    fun toggleQuotaStatus(quotaNumber: String) {
        viewModelScope.launch {
            try {
                val success = repository.toggleQuotaStatus(quotaNumber)
                if (success) {
                    // Refresh the quotas list
                    loadShipQuotas(_selectedShip.value?.name ?: "")
                    showSnackbar("وضعیت کوتاژ با موفقیت تغییر کرد")
                } else {
                    showSnackbar("خطا در تغییر وضعیت کوتاژ")
                }
            } catch (e: Exception) {
                showSnackbar("خطا در تغییر وضعیت کوتاژ: ${e.message}")
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
                    // Refresh the quotas list
                    loadShipQuotas(_selectedShip.value?.name ?: "")
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

    fun sendMessage(
        title: String,
        body: String,
        recipients: List<String>,
        senderId: Int,
        senderType: String
    ) {
        viewModelScope.launch {
            _messageSendingStatus.value = MessageSendingStatus.Sending
            try {
                val result = repository.sendMessage(title, body, recipients, senderId, senderType)
                _messageSendingStatus.value =
                    if (result) MessageSendingStatus.Success else MessageSendingStatus.Error("Failed to send message")
            } catch (e: Exception) {
                _messageSendingStatus.value =
                    MessageSendingStatus.Error(e.message ?: "Unknown error occurred")
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
                val document = Document(PageSize.A4, 36f, 36f, 54f, 36f)
                val writer = PdfWriter.getInstance(document, outputStream)
                document.open()

                val baseFont = BaseFont.createFont("assets/fonts/B NAZANIN.TTF", BaseFont.IDENTITY_H, BaseFont.EMBEDDED)
                val farsiFont = Font(baseFont, 10f, Font.NORMAL)
                val farsiBoldFont = Font(baseFont, 12f, Font.BOLD)
                val farsiHeaderFont = Font(baseFont, 18f, Font.BOLD)
                val defaultFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL)
                val defaultBoldFont = Font(Font.FontFamily.HELVETICA, 11f, Font.BOLD)

                // تعریف رنگ‌ها
                val primaryColor = BaseColor(0, 121, 107)
                val secondaryColor = BaseColor(224, 242, 241)

                // اضافه کردن سربرگ
                addHeader(document, farsiHeaderFont)

                // اضافه کردن اطلاعات خلاصه
                addSummaryInfo(document, data, farsiBoldFont, farsiFont, secondaryColor)

                // ایجاد جدول اصلی
                val table = createMainTable(
                    data,
                    farsiBoldFont,
                    farsiFont,
                    defaultFont,
                    defaultBoldFont,
                    primaryColor,
                    secondaryColor
                )

                document.add(table)

                // اضافه کردن پاورقی
                addFooter(document, writer, defaultFont)

                document.close()
            }
        } catch (e: Exception) {
            Log.e("CreatePdfFile", "Error creating PDF file", e)
            throw e
        }

        fileName
    }

    private fun addHeader(document: Document, font: Font) {
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(1f, 2f))

        // اضافه کردن عنوان گزارش
        val titleCell = PdfPCell()
        titleCell.border = Rectangle.NO_BORDER
        titleCell.paddingRight = 10f
        addRtlText(titleCell, "گزارش خلاصه حواله‌ها", font, Element.ALIGN_RIGHT)
        table.addCell(titleCell)

        document.add(table)
        document.add(Paragraph(" ")) // فاصله
    }

    private fun addSummaryInfo(
        document: Document,
        data: FilteredSummary,
        boldFont: Font,
        normalFont: Font,
        secondaryColor: BaseColor
    ) {
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(1f, 1f))

        fun addInfoRow(label: String, value: String) {
            // ابتدا سلول مقدار را اضافه می‌کنیم
            val valueCell = PdfPCell()
            valueCell.paddingRight = 5f
            addRtlText(valueCell, value, normalFont, Element.ALIGN_RIGHT)
            table.addCell(valueCell)

            // سپس سلول عنوان را اضافه می‌کنیم
            val labelCell = PdfPCell()
            labelCell.backgroundColor = secondaryColor
            labelCell.paddingRight = 5f
            labelCell.paddingTop = 8f
            labelCell.paddingBottom = 8f
            addRtlText(labelCell, label, boldFont, Element.ALIGN_RIGHT)
            table.addCell(labelCell)
        }

        addInfoRow("شماره کوتاژ:", data.quotaNumber)
        addInfoRow("از تاریخ و ساعت:", "${formatDate(data.startDate)} - ${data.startTime}")
        addInfoRow("تا تاریخ و ساعت:", "${formatDate(data.endDate)} - ${data.endTime}")
        addInfoRow("تعداد کل حواله‌ها:", data.voucherCount.toString())
        addInfoRow("وزن خالص کل:", "${formatNumber(data.totalNetWeight.toInt())} کیلوگرم")

        document.add(table)
        document.add(Paragraph(" ")) // فاصله
    }

    private fun createMainTable(
        data: FilteredSummary,
        boldFarsiFont: Font,
        normalFarsiFont: Font,
        normalFont: Font,
        boldFont: Font,
        primaryColor: BaseColor,
        secondaryColor: BaseColor
    ): PdfPTable {
        val table = PdfPTable(6)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(3f, 2f, 2f, 2f, 2f, 2f))

        fun createCell(content: String, isHeader: Boolean = false, isFarsi: Boolean = false): PdfPCell {
            val font = when {
                isHeader && isFarsi -> boldFarsiFont.apply { color = BaseColor.WHITE }
                isHeader && !isFarsi -> boldFont.apply { color = BaseColor.WHITE }
                isFarsi -> normalFarsiFont
                else -> normalFont
            }
            val cell = PdfPCell()
            cell.paddingTop = 8f
            cell.paddingBottom = 8f
            if (isHeader) {
                cell.backgroundColor = primaryColor
            }
            if (isFarsi) {
                addRtlText(cell, content, font, Element.ALIGN_CENTER)
            } else {
                cell.phrase = Phrase(content, font)
                cell.horizontalAlignment = Element.ALIGN_CENTER
            }
            return cell
        }

        // سرستون‌های جدول
        table.addCell(createCell("شماره حواله", isHeader = true, isFarsi = true))
        table.addCell(createCell("ساعت ورود", isHeader = true, isFarsi = true))
        table.addCell(createCell("ساعت خروج", isHeader = true, isFarsi = true))
        table.addCell(createCell("تاریخ خروج", isHeader = true, isFarsi = true))
        table.addCell(createCell("وزن خالص", isHeader = true, isFarsi = true))
        table.addCell(createCell("شماره قبض", isHeader = true, isFarsi = true))

        // داده‌های جدول
        data.voucherDetails.forEachIndexed { index, detail ->
            val rowColor = if (index % 2 == 0) BaseColor.WHITE else secondaryColor
            table.addCell(createCell(detail.trackingNumber, isFarsi = false).apply { backgroundColor = rowColor })
            table.addCell(createCell(detail.entryTime, isFarsi = false).apply { backgroundColor = rowColor })
            table.addCell(createCell(detail.exitTime, isFarsi = false).apply { backgroundColor = rowColor })
            table.addCell(createCell(formatDate(detail.exitDate), isFarsi = false).apply { backgroundColor = rowColor })
            table.addCell(createCell(formatNumber(detail.netWeight.toInt()), isFarsi = false).apply { backgroundColor = rowColor })
            table.addCell(createCell(detail.scaleReceiptNumber, isFarsi = false).apply { backgroundColor = rowColor })
        }

        return table
    }

    private fun addRtlText(cell: PdfPCell, text: String, font: Font, alignment: Int) {
        val column = ColumnText(null)
        column.runDirection = PdfWriter.RUN_DIRECTION_RTL
        column.alignment = alignment
        column.addElement(Paragraph(text, font))
        cell.column = column
    }

    private fun formatDate(date: String): String {
        val parts = date.split("/")
        return if (parts.size == 3) "${parts[0]}/${parts[1]}/${parts[2]}" else date
    }

    @SuppressLint("SimpleDateFormat", "DefaultLocale")
    private fun addFooter(document: Document, writer: PdfWriter, font: Font) {
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())

        for (pageNumber in 1..writer.pageNumber) {
            val footerTable = PdfPTable(3)
            footerTable.totalWidth = document.pageSize.width - document.leftMargin() - document.rightMargin()
            footerTable.setWidths(floatArrayOf(1f, 1f, 1f))

            // شماره صفحه
            val cell1 = PdfPCell(Phrase(String.format("صفحه %d از %d", pageNumber, writer.pageNumber), font))
            cell1.horizontalAlignment = Element.ALIGN_LEFT
            cell1.border = Rectangle.NO_BORDER
            footerTable.addCell(cell1)

            // تاریخ و زمان تولید گزارش
            val cell2 = PdfPCell(Phrase("تاریخ تولید گزارش: $time", font))
            cell2.horizontalAlignment = Element.ALIGN_CENTER
            cell2.border = Rectangle.NO_BORDER
            footerTable.addCell(cell2)

            // نام شرکت یا اطلاعات تماس
            val cell3 = PdfPCell(Phrase("شرکت ای تی کی", font))
            cell3.horizontalAlignment = Element.ALIGN_RIGHT
            cell3.border = Rectangle.NO_BORDER
            footerTable.addCell(cell3)

            footerTable.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin(), writer.directContent)
        }
    }

    private fun formatNumber(number: Int): String {
        return NumberFormat.getNumberInstance(Locale("en", "US")).format(number)
    }

    sealed class MessageSendingStatus {
        data object Idle : MessageSendingStatus()
        data object Sending : MessageSendingStatus()
        data object Success : MessageSendingStatus()
        data class Error(val message: String) : MessageSendingStatus()
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
            Log.e("ReportsRepository", "Exception in getCargoInfo: ${e.message}", e)
            throw e
        }
    }

    suspend fun getShipsList(): ShipsData = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getShipsList()
            if (response.isSuccessful) {
                response.body()?.data ?: ShipsData(emptyList(), emptyList())
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
                } catch (e: Exception) {
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
                    // پردازش startDateTime و endDateTime
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
                    val result = response.body()?.cargoInfo
                    result
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun sendMessage(
        title: String,
        body: String,
        recipients: List<String>,
        senderId: Int,
        senderType: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val messageRequest = MessageRequest(
                title = title,
                body = body,
                recipients = recipients,
                senderId = senderId,
                senderType = senderType
            )
            val response = apiService.sendMessage(messageRequest)

            if (response.isSuccessful) {
                val responseBody = response.body()
                val result = responseBody?.success ?: false
                if (!result) {
                    Log.w(
                        "ReportsRepository",
                        "Server returned success: false. Message: ${responseBody?.message}"
                    )
                }
                result
            } else {
                val errorBody = response.errorBody()?.string()
                throw Exception("Server error: ${response.code()}, Error body: $errorBody")
            }
        } catch (e: Exception) {
            throw Exception("Error sending message: ${e.message}")
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

class LoadingAnalytics {

    // تحلیل کلی حواله‌ها
    fun analyzeVouchers(loadingData: List<RealTimeLoadingData>): VoucherAnalytics {
        val totalEntry = loadingData.sumOf { it.entryVouchers.toFloat() }
        val totalExit = loadingData.sumOf { it.exitVouchers.toFloat() }
        val totalVouchers = totalEntry + totalExit

        val exitPercentage = if (totalVouchers > 0) {
            (totalExit / totalVouchers) * 100
        } else 0f

        val averageWeight = if (totalExit > 0) {
            loadingData.sumOf { it.totalNetWeight.toDouble().toFloat() } / totalExit
        } else 0f

        return VoucherAnalytics(
            totalEntryVouchers = totalEntry.toInt(),
            totalExitVouchers = totalExit.toInt(),
            exitPercentage = exitPercentage,
            averageExitWeight = averageWeight
        )
    }

    // تحلیل عملکرد انبارها
    fun analyzeWarehouses(loadingData: List<RealTimeLoadingData>): List<WarehouseAnalytics> {
        val totalOperationWeight = loadingData.sumOf { it.totalNetWeight.toDouble().toFloat() }

        return loadingData
            .groupBy { it.loadingWarehouse }
            .map { (warehouse, data) ->
                val warehouseWeight = data.sumOf { it.totalNetWeight.toDouble().toFloat() }
                val totalVouchers = data.sumOf { it.exitVouchers.toFloat() }

                WarehouseAnalytics(
                    warehouseName = warehouse,
                    totalWeight = warehouseWeight,
                    averageWeight = if (totalVouchers > 0) warehouseWeight / totalVouchers else 0f,
                    operationPercentage = if (totalOperationWeight > 0) {
                        (warehouseWeight / totalOperationWeight * 100)
                    } else 0f,
                    rank = 0 // رتبه‌بندی بعداً تنظیم می‌شود
                )
            }
            .sortedByDescending { it.totalWeight }
            .mapIndexed { index, analytics ->
                analytics.copy(rank = index + 1)
            }
    }

    // تحلیل عملکرد کشتی‌ها
    fun analyzeShips(loadingData: List<RealTimeLoadingData>): List<ShipAnalytics> {
        return loadingData
            .groupBy { it.shipName }
            .map { (ship, data) ->
                val entryVouchers = data.sumOf { it.entryVouchers.toFloat() }
                val exitVouchers = data.sumOf { it.exitVouchers.toFloat() }
                val totalVouchers = entryVouchers + exitVouchers
                val loadedWeight = data.sumOf { it.totalNetWeight.toDouble().toFloat() }
                val warehouses = data.map { it.loadingWarehouse }.distinct()

                ShipAnalytics(
                    shipName = ship,
                    totalEntryVouchers = entryVouchers.toInt(),
                    totalExitVouchers = exitVouchers.toInt(),
                    totalVouchers = totalVouchers.toInt(),
                    totalNetWeight = loadedWeight,
                    averageWeight = if (exitVouchers > 0) loadedWeight / exitVouchers else 0f,
                    warehouses = warehouses,
                    warehouseCount = warehouses.size,
                    exitRatio = if (totalVouchers > 0) (exitVouchers / totalVouchers) * 100 else 0f
                )
            }
    }

    // تحلیل عملکرد شرکت‌های باربری
    fun analyzeShippingCompanies(loadingData: List<RealTimeLoadingData>): List<ShippingCompanyAnalytics> {
        val totalOperationWeight = loadingData.sumOf { it.totalNetWeight.toDouble().toFloat() }

        return loadingData
            .groupBy { it.shippingCompany }
            .map { (company, data) ->
                val companyWeight = data.sumOf { it.totalNetWeight.toDouble().toFloat() }
                val totalVouchers = data.sumOf { it.exitVouchers.toFloat() }

                ShippingCompanyAnalytics(
                    companyName = company,
                    totalVouchers = totalVouchers.toInt(),
                    totalWeight = companyWeight,
                    operationPercentage = if (totalOperationWeight > 0) {
                        (companyWeight / totalOperationWeight * 100)
                    } else 0f
                )
            }
            .sortedByDescending { it.totalWeight }
    }

    // تحلیل عملکرد کوتاژها
    fun analyzeQuotas(loadingData: List<RealTimeLoadingData>): List<QuotaAnalytics> {
        return loadingData
            .groupBy { it.loadingQuotaNumber }
            .map { (quotaNumber, data) ->
                val entryVouchers = data.sumOf { it.entryVouchers.toFloat() }
                val exitVouchers = data.sumOf { it.exitVouchers.toFloat() }
                val totalVouchers = entryVouchers + exitVouchers
                val totalWeight = data.sumOf { it.totalNetWeight.toDouble().toFloat() }
                val cargoWeight = data.firstOrNull()?.cargoWeight?.toFloat() ?: 0f
                val percentageCompleted = if (cargoWeight > 0) {
                    (totalWeight / cargoWeight * 100).coerceIn(0f, 100f)
                } else 0f

                QuotaAnalytics(
                    quotaNumber = quotaNumber,
                    shippingCompany = data.first().shippingCompany,
                    totalVouchers = totalVouchers.toInt(),
                    entryVouchers = entryVouchers.toInt(),
                    exitVouchers = exitVouchers.toInt(),
                    totalWeight = totalWeight,
                    averageWeight = if (exitVouchers > 0) totalWeight / exitVouchers else 0f,
                    operationEfficiency = if (totalVouchers > 0) {
                        (exitVouchers / totalVouchers * 100)
                    } else 0f,
                    weightPerHour = totalWeight / 24f,
                    loadingRate = if (exitVouchers > 0) totalWeight / exitVouchers else 0f,
                    completionRate = percentageCompleted,
                    warehouseName = data.first().loadingWarehouse
                )
            }
            .sortedByDescending { it.totalWeight }
    }
}

data class VoucherAnalytics(
    val totalEntryVouchers: Int,
    val totalExitVouchers: Int,
    val exitPercentage: Float,
    val averageExitWeight: Float
)

data class WarehouseAnalytics(
    val warehouseName: String,
    val totalWeight: Float,
    val averageWeight: Float,
    val operationPercentage: Float,
    val rank: Int
)

data class ShipAnalytics(
    val shipName: String,
    val totalEntryVouchers: Int,
    val totalExitVouchers: Int,
    val totalVouchers: Int,
    val totalNetWeight: Float,
    val averageWeight: Float,
    val warehouses: List<String>,
    val warehouseCount: Int,
    val exitRatio: Float
)

data class ShippingCompanyAnalytics(
    val companyName: String,
    val totalVouchers: Int,
    val totalWeight: Float,
    val operationPercentage: Float
)

data class QuotaAnalytics(
    val quotaNumber: String,
    val shippingCompany: String,
    val warehouseName: String,
    val totalVouchers: Int,
    val entryVouchers: Int,
    val exitVouchers: Int,
    val totalWeight: Float,
    val averageWeight: Float,
    val loadingRate: Float,
    val weightPerHour: Float,
    val operationEfficiency: Float,
    val completionRate: Float
)

@SuppressLint("DefaultLocale")
fun gregorianToJalali(gregorian: Calendar): String {
    val gy = gregorian.get(Calendar.YEAR)
    val gm = gregorian.get(Calendar.MONTH) + 1
    val gd = gregorian.get(Calendar.DAY_OF_MONTH)

    val g_d_m = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365)
    val j_days_in_month = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

    val gy2 = if (gm > 2) gy + 1 else gy
    var days =
        355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + g_d_m[gm - 1]

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
        if (days < j_days_in_month[i]) {
            jm = i + 1
            break
        }
        days -= j_days_in_month[i]
    }

    val jd = days + 1

    return String.format("%04d/%02d/%02d", jy, jm, jd)
}

@OptIn(ExperimentalGetImage::class)
suspend fun recognizeText(image: InputImage): String {
    return withContext(Dispatchers.Default) {
        suspendCoroutine { continuation ->
            val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            textRecognizer.process(image)
                .addOnSuccessListener { result ->
                    continuation.resume(result.text)
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }
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
    val userType: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val userType: String?
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
    val cargoInfoList: List<CargoInfo>
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
    val isActive: Int = 1,
    val totalVoucherCount: Int = 0
) : Parcelable

data class CargoInfo(
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
    val confirmation: String? = null
)

data class SaveOrUpdateResponse(
    val error: Boolean?,
    val message: String,
    val status: String?,
    val trackingNumber: String?,
    val shipName: String?,
    val loadingQuotaNumber: String?,
    val exitDate: String?,
    val exitTime: String?
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
    val remainingServices: String
)

data class MenuItem(
    val title: String,
    val iconResourceId: Int,
    val route: String
)

data class MessageRequest(
    val action: String = "sendMessage",
    val title: String,
    val body: String,
    val recipients: List<String>,
    val senderId: Int,
    val senderType: String
)

data class SuccessResponse(
    val success: Boolean,
    val message: String,
    val userId: Int? = null
)

data class Message(
    val id: Int,
    val title: String,
    val body: String,
    val dateTime: String,
    val senderType: String,
    val readBy: List<String>? = null
)

data class MessageReadRequest(
    val messageId: Int,
    val username: String,
    val action: String = "markAsRead"
)

data class CargoInfoSearch(
    val cargoInfo: CargoInfo?
)

data class SessionCheckRequest(val username: String)

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

data class TabInfo(
    val title: String,
    val icon: ImageVector
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
    val isPercentageRestricted: Boolean? = false
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
    val value: String,
    val icon: ImageVector,
    val color: Color
)

enum class ShipSection {
    ACTIVE, INACTIVE
}

data class FabItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)

data class ChangeLogInfo(
    val newFeatures: List<String> = emptyList(),
    val improvements: List<String> = emptyList(),
    val fixes: List<String> = emptyList(),
    val others: List<String> = emptyList()
)

data class UpdateInfo(
    val latestVersion: String,
    val downloadUrl: String,
    val changeLog: ChangeLogInfo,
    val updatePriority: String = "normal",
    val updateMessage: String = "",
    val forceUpdate: Boolean = false,
    val updateSize: String = "0",
    val releaseDate: String = "",
    val minAndroidVersion: Int = 21
)

class ColorSelector(private val colors: List<Color>) {
    private var currentIndex = 0

    fun getNextColor(): Color {
        val color = colors[currentIndex]
        currentIndex = (currentIndex + 3) % colors.size
        return color
    }

    fun reset() {
        currentIndex = 0
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

object AppColors {
    // Light Theme Colors
    val LightPrimary = Color(0xFF6200EE)
    val LightOnPrimary = Color.White
    val LightPrimaryContainer = Color(0xFFE8DEF8)
    val LightOnPrimaryContainer = Color(0xFF21005D)
    val LightSecondary = Color(0xFF089C8F)
    val LightOnSecondary = Color.Black
    val LightSecondaryContainer = Color(0xFF56E7E1)
    val LightOnSecondaryContainer = Color(0xFF00504D)
    val LightBackground = Color(0xFFF3F3F3)
    val LightOnBackground = Color(0xFF1C1B1F)
    val LightSurface = Color.White
    val LightOnSurface = Color(0xFF1C1B1F)
    val LightError = Color(0xFFE9153B)
    val LightOnError = Color.White

    // Dark Theme Colors
    val DarkPrimary = Color(0xFFBB86FC)
    val DarkOnPrimary = Color.Black
    val DarkPrimaryContainer = Color(0xFF4F378B)
    val DarkOnPrimaryContainer = Color(0xFFE8DEF8)
    val DarkSecondary = Color(0xFF03DAC6)
    val DarkOnSecondary = Color.Black
    val DarkSecondaryContainer = Color(0xFF00504D)
    val DarkOnSecondaryContainer = Color(0xFFCEFAF8)
    val DarkBackground = Color(0xFF121212)
    val DarkOnBackground = Color(0xFFE3E3E3)
    val DarkSurface = Color(0xFF1E1E1E)
    val DarkOnSurface = Color(0xFFE3E3E3)
    val DarkError = Color(0xFFCF6679)
    val DarkOnError = Color.Black
}

val cardColors = listOf(
    Color(0xFFD50000), // Red
    Color(0xFF4CAF50), // Green
    Color(0xFF2196F3), // Blue
    Color(0xFFE91E63), // Pink
    Color(0xFF00BCD4), // Cyan
    Color(0xFF9C27B0), // Purple
    Color(0xFF009688), // Teal
    Color(0xFF795548), // Brown
    Color(0xFF673AB7), // Deep Purple
    Color(0xFF009688), // Dark Teal
    Color(0xFFF4511E), // Deep Orange
    Color(0xFF512DA8), // Deep Purple
    Color(0xFF1976D2), // Deep Blue
    Color(0xFFD32F2F), // Dark Red
    Color(0xFF388E3C), // Dark Green
    Color(0xFF0097A7), // Dark Cyan
    Color(0xFFC2185B), // Dark Pink
    Color(0xFF7B1FA2), // Dark Purple
    Color(0xFF303F9F), // Deep Indigo
    Color(0xFF689F38), // Olive Green
    Color(0xFF00695C), // Very Dark Teal
    Color(0xFF827717), // Dark Yellow
    Color(0xFF6A1B9A)  // Deep Purple
)

fun Float.toTon(): Int = (this / 1000).toInt()

sealed class LoadingState {
    object Idle : LoadingState()
    object Loading : LoadingState()
    object Success : LoadingState()
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
    val warehousePeakAnalysis: List<WarehousePeakAnalysis>?
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
    val warehousePeakAnalysis: List<WarehousePeakData> = emptyList()
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