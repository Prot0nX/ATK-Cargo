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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
    private val quotasCache = mutableMapOf<String, List<Quota>>()
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
    private val _loadableTonnage = MutableStateFlow("")
    val loadableTonnage: StateFlow<String> = _loadableTonnage.asStateFlow()
    
    // اضافه کردن StateFlow برای تعداد ماشین‌های قابل بارگیری
    private val _loadableTrucks18Wheeler = MutableStateFlow("")
    val loadableTrucks18Wheeler: StateFlow<String> = _loadableTrucks18Wheeler.asStateFlow()
    
    private val _loadableTrucks10Wheeler = MutableStateFlow("")
    val loadableTrucks10Wheeler: StateFlow<String> = _loadableTrucks10Wheeler.asStateFlow()
    
    private val _warehouseQuotaGroupingMode = MutableStateFlow(WarehouseQuotaGroupingMode.BY_SHIPPING_COMPANY)
    val warehouseQuotaGroupingMode: StateFlow<WarehouseQuotaGroupingMode> = _warehouseQuotaGroupingMode.asStateFlow()
    
    
    private val _cachedTrackingNumbers = MutableStateFlow<Set<String>>(emptySet())
    val cachedTrackingNumbers: StateFlow<Set<String>> = _cachedTrackingNumbers.asStateFlow()
    
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
        // بروزرسانی فوری تناژ قابل بارگیری قبل از هر کار دیگر
        updateLoadableTonnage()
        
        viewModelScope.launch {
            _initialInfo.value?.let { info ->
                try {
                    // اطمینان از اعتبار اطلاعات جاری
                    if (info.loadingQuotaNumber.toString().isBlank() || info.shippingCompany.isBlank() ||
                        info.loadingWarehouse.isBlank() || info.cargoType.isBlank()) {
                        Log.e("CargoViewModel", "Invalid initial info for refresh: $info")
                        return@launch
                    }
                    
                    // بررسی وضعیت کوتاژ و سپس بارگذاری اطلاعات
                    checkQuotaStatus(info)
                    
                    // وقفه کوتاه برای دریافت بهترین داده‌ها
                    delay(300)
                    
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
                                    "وضعیت حواله‌ها به‌روزرسانی شد",
                                    MessageType.SUCCESS
                                )
                            } else {
                                showUpdateMessage(
                                    "اطلاعات در ساعت $currentTime به‌روزرسانی شد",
                                    MessageType.SUCCESS
                                )
                            }
                            
                            // بروزرسانی نهایی تناژ قابل بارگیری بعد از تکمیل همه عملیات‌ها
                            updateLoadableTonnage()
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

                // بررسی وضعیت کوتاژ (درصد و فعال بودن)
                checkAndHandleQuotaPercentage(initialInfo.loadingQuotaNumber.toString())
                checkQuotaStatus(initialInfo)

                if (_isQuotaActive.value != true) {
                    val message = if (_messageType.value == MessageType.WARNING) {
                        "امکان ثبت حواله برای این کوتاژ وجود ندارد. لطفاً وضعیت کوتاژ را بررسی کنید."
                    } else {
                        "کوتاژ غیرفعال است و امکان ثبت حواله جدید وجود ندارد"
                    }
                    showErrorMessage(message)
                    return@launch
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
                    }
                }
            } else {
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
                handleErrorResponse(parsedError)
            } else {
                showErrorMessage("خطا در ارسال اطلاعات بار: کد خطا $errorCode")
            }
        } catch (e: Exception) {
            showErrorMessage("خطا در پردازش پاسخ سرور: ${e.message}")
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

        // فوراً تناژ قابل بارگیری را بروزرسانی می‌کنیم
        updateLoadableTonnage()
        
        _initialInfo.value?.let { info ->
            // بارگذاری مجدد اطلاعات با تأکید بر دریافت به‌روزترین داده‌ها
            loadCargoInfoList(
                quotaNumber = info.loadingQuotaNumber.toString(),
                shippingCompany = info.shippingCompany,
                warehouse = info.loadingWarehouse,
                cargoType = info.cargoType,
                onComplete = {
                    // یک بروزرسانی اضافی بعد از بارگذاری مجدد
                    viewModelScope.launch {
                        delay(300) // تأخیر کوتاه
                        refreshCargoInfo() // بروزرسانی مجدد بعد از دریافت اطلاعات
                    }
                }
            )
        } ?: run {
            Log.e("CargoViewModel", "Unable to reload cargo info: Initial info is null")
        }
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
        return !(firstTwoDigits != "42" && firstTwoDigits != "43" && firstTwoDigits != "44")
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

                // به‌روزرسانی UI با اطلاعات اصلی
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
                    // فوراً تناژ قابل بارگیری را بروزرسانی می‌کنیم
                    updateLoadableTonnage()
                    
                    // یک تأخیر کوتاه برای اطمینان از ثبت کامل در سرور
                    delay(500)
                    
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
                    
                    // لاگ برای دیباگ
                    Log.d("CargoViewModel", "حواله با شماره ${cargoInfo.trackingNumber} به وضعیت خروج تغییر یافت")
                    Log.d("CargoViewModel", "تعداد کل حواله‌ها: ${updatedList.size}, تعداد حواله‌های خروج: ${updatedList.count { it.status == "خروج" }}")
                    
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
                        
                        // فوراً تناژ قابل بارگیری را بروزرسانی می‌کنیم
                        updateLoadableTonnage()
                        
                        // بارگذاری مجدد اطلاعات پس از حذف
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

    // بروزرسانی فوری مقدار تناژ قابل بارگیری با اولویت بالا
    private fun updateLoadableTonnage() {
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
                                _loadableTonnage.value = DecimalFormat("#,###").format(tonnage.roundToInt())
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

    private val _warehouseQuotaGroupingMode = MutableStateFlow(WarehouseQuotaGroupingMode.BY_SHIPPING_COMPANY)
    val warehouseQuotaGroupingMode: StateFlow<WarehouseQuotaGroupingMode> = _warehouseQuotaGroupingMode.asStateFlow()
    
    

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
                                quotas.sumOf { it.last_24h_weight }
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
                                quotas.sumOf { it.last_24h_weight }
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

    fun loadFilteredShipQuotas(shipName: String, startDateTime: String, endDateTime: String) {
        viewModelScope.launch {
            try {
                val quotas = repository.getFilteredQuotas(shipName, startDateTime, endDateTime)
                _selectedShipQuotas.value = quotas
            } catch (e: Exception) {
                _loadingError.value = "خطا در بارگیری کوتاژهای فیلتر شده: ${e.message}"
            }
        }
    }

    fun clearLoadingError() {
        _loadingError.value = null
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

    fun shareRealTimeLoadingData(loadingData: List<RealTimeLoadingData>, shiftInfo: ShiftInfo?): String {
        // محاسبه کل حواله‌های خروجی
        val totalExitVouchers = loadingData.sumOf { it.exitVouchers.toFloat() }.toInt()
        
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
                val exitVouchers = shipData.sumOf { it.exitVouchers.toFloat() }.toInt()
                
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
            Log.e("ReportsRepository", "Exception in getCargoInfo: ${e.message}", e)
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
                Log.e("ReportsRepository", "Error fetching initial info: ${response.code()}")
                return null
            }
        } catch (e: Exception) {
            Log.e("ReportsRepository", "Exception in getInitialInfo: ${e.message}", e)
            return null
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
                Log.e("ReportsRepository", "Server error ${response.code()}: $errorBody")
                throw Exception("Server error: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Log.e("ReportsRepository", "Exception in getFilteredQuotas: ${e.message}", e)
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
                } catch (e: Exception) {
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

data class SuccessResponse(
    val success: Boolean,
    val message: String,
    val userId: Int? = null
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
    Color(0xFF6A1B9A),  // Deep Purple
    
    // رنگ های جدید اضافه شده
    Color(0xFF00897B), // Teal 600
    Color(0xFF8BC34A), // Light Green
    Color(0xFFFF5722), // Deep Orange
    Color(0xFF5D4037), // Brown 700
    Color(0xFF00796B), // Teal 700
    Color(0xFF3F51B5), // Indigo
    Color(0xFFFF8F00), // Amber 800
    Color(0xFF558B2F), // Light Green 800
    Color(0xFF283593), // Indigo 800
    Color(0xFF1565C0), // Blue 800
    Color(0xFF6200EA), // Deep Purple A700
    Color(0xFF2962FF), // Blue A700
    Color(0xFF00B8D4), // Cyan A700
    Color(0xFF00C853), // Green A700
    Color(0xFF4A148C), // Purple 900
    Color(0xFFFF6F00), // Amber 900
    Color(0xFF33691E), // Light Green 900
    Color(0xFFFFA000), // Orange 700
    Color(0xFF039BE5), // Light Blue 600
    Color(0xFFBF360C)  // Deep Orange 900
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