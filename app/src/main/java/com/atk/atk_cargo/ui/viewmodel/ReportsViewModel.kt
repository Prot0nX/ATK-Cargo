package com.atk.atk_cargo.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.os.Environment
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.atk.atk_cargo.api.ThirdPartyRetrofitClient
import com.atk.atk_cargo.data.model.CargoInfo
import com.atk.atk_cargo.data.model.ColorSelector
import com.atk.atk_cargo.data.model.ComprehensiveAnalytics
import com.atk.atk_cargo.data.model.FilteredSummary
import com.atk.atk_cargo.data.model.GroupSortingMode
import com.atk.atk_cargo.data.model.Quota
import com.atk.atk_cargo.data.model.QuotaCompletionData
import com.atk.atk_cargo.data.model.QuotaDetails
import com.atk.atk_cargo.data.model.QuotaEditData
import com.atk.atk_cargo.data.model.QuotaGroupingMode
import com.atk.atk_cargo.data.model.QuotaPercentageData
import com.atk.atk_cargo.data.model.QuotaSortingMode
import com.atk.atk_cargo.data.model.RealTimeLoadingData
import com.atk.atk_cargo.data.model.SaveOrUpdateResponse
import com.atk.atk_cargo.data.model.ShiftInfo
import com.atk.atk_cargo.data.model.Ship
import com.atk.atk_cargo.data.model.ShipSortingMode
import com.atk.atk_cargo.data.model.ShipsData
import com.atk.atk_cargo.data.model.ThirdPartyOrder
import com.atk.atk_cargo.data.model.ThirdPartyOrderRequest
import com.atk.atk_cargo.data.model.Warehouse
import com.atk.atk_cargo.data.model.WarehouseQuotaGroupingMode
import com.atk.atk_cargo.data.model.adjustColorForTheme
import com.atk.atk_cargo.data.model.cardColors
import com.atk.atk_cargo.data.repository.ReportsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class ReportsViewModel(
    private val repository: ReportsRepository,
    application: Application
) : AndroidViewModel(application) {
    private val exportPdfUseCase = com.atk.atk_cargo.feature.reports.domain.ExportPdfUseCase(application)
    private val _realTimeLoadingData = MutableStateFlow<List<RealTimeLoadingData>>(emptyList())
    val realTimeLoadingData: StateFlow<List<RealTimeLoadingData>> = _realTimeLoadingData
    private val _thirdPartyOrders = MutableStateFlow<List<ThirdPartyOrder>>(emptyList())
    val thirdPartyOrders: StateFlow<List<ThirdPartyOrder>> = _thirdPartyOrders
    private val _thirdPartyLoadingError = MutableStateFlow<String?>(null)
    val thirdPartyLoadingError: StateFlow<String?> = _thirdPartyLoadingError
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

    private val _analyticsDateOffset = MutableStateFlow(0)
    val analyticsDateOffset: StateFlow<Int> = _analyticsDateOffset.asStateFlow()

    fun setAnalyticsDateOffset(offset: Int) {
        if (offset in -7..0) {
            _analyticsDateOffset.value = offset
            loadComprehensiveAnalytics()
        }
    }

    private val _realTimeShiftOffset = MutableStateFlow(0)
    val realTimeShiftOffset: StateFlow<Int> = _realTimeShiftOffset.asStateFlow()

    fun setRealTimeShiftOffset(offset: Int, isDarkTheme: Boolean, defaultColor: Color) {
        if (offset <= 0) {
            _realTimeShiftOffset.value = offset
            loadRealTimeData(isDarkTheme, defaultColor)
        }
    }

    private val _groupingMode = MutableStateFlow(QuotaGroupingMode.BY_CARGO_OWNER)
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


    private val _isMinimalQuotaMode = MutableStateFlow(false)
    val isMinimalQuotaMode: StateFlow<Boolean> = _isMinimalQuotaMode.asStateFlow()

    fun toggleMinimalQuotaMode() {
        _isMinimalQuotaMode.value = !_isMinimalQuotaMode.value
    }

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
                QuotaGroupingMode.BY_CARGO_OWNER -> {
                    filtered.groupBy { "${it.shipName}|${it.warehouse ?: "نامشخص"}|${it.cargoType ?: "نامشخص"}" }
                        .map { (compositeKey, quotas) ->
                            Triple(
                                compositeKey,
                                quotas.size,
                                quotas.sumOf { it.last_24h_weight.toDouble() }.toFloat()
                            )
                        }
                        .sortedWith(
                            compareBy<Triple<String, Int, Float>> { it.first }
                        )
                        .flatMap { (compositeKey, _, _) ->
                            filtered.filter { "${it.shipName}|${it.warehouse ?: "نامشخص"}|${it.cargoType ?: "نامشخص"}" == compositeKey }
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
        return NumberFormat.getNumberInstance(Locale.US).format(number)
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
                val response = repository.getRealTimeLoadingData(_realTimeShiftOffset.value)
                _realTimeLoadingData.value = response.data.sortedByDescending { it.entryVouchers }
                _shiftInfo.value = response.shiftInfo
                _loadingError.value = null

                // گام 1: ابتدا اسامی کشتی‌ها را استخراج می‌کنیم
                val shipNames = response.data.map { it.shipName }.distinct().toSet()

                // گام 2: تخصیص رنگ‌های کاملاً متمایز فقط به کشتی‌ها
                val shipColors = colorSelector.assignDistinctColors(shipNames)
                    .mapValues { (_, color) -> adjustColorForTheme(color, isDarkTheme) }

                // گام 3: به‌روزرسانی رنگ‌های کشتی‌ها در ViewModel
                _shipColorMap.value = shipColors

                // برای حفظ سازگاری با کدهای دیگر، رنگ کوتاژها را برابر با رنگ کشتی مربوطه قرار می‌دهیم
                val quotaColors = mutableMapOf<String, Color>()
                response.data.forEach { data ->
                    val shipColor = shipColors[data.shipName] ?: adjustColorForTheme(defaultColor, isDarkTheme)
                    quotaColors[data.loadingQuotaNumber] = shipColor
                }
                _quotaColorMap.value = quotaColors

            } catch (e: Exception) {
                _loadingError.value = "خطا در دریافت اطلاعات: ${e.message}"
            }
        }
    }

    fun loadThirdPartyOrders() {
        viewModelScope.launch {
            try {
                _thirdPartyLoadingError.value = null
                
                // تولید تاریخ‌های شمسی بر اساس ساعت فعلی
                val calendar = Calendar.getInstance()
                val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                
                val todayShamsi = gregorianToJalali(calendar)
                val date1Formatted: String
                val date2Formatted: String

                if (currentHour >= 7) {
                    date1Formatted = todayShamsi
                    date2Formatted = todayShamsi
                } else {
                    calendar.add(Calendar.DAY_OF_MONTH, -1)
                    val yesterdayShamsi = gregorianToJalali(calendar)
                    date1Formatted = yesterdayShamsi
                    date2Formatted = todayShamsi
                }
                
                val requestBody = ThirdPartyOrderRequest(
                    companyCode = "36429",
                    date1 = date1Formatted,
                    date2 = date2Formatted,
                    reportName = "گزارش درجريان تفصيلي - 4"
                )
                
                val response = withContext(Dispatchers.IO) {
                    ThirdPartyRetrofitClient.thirdPartyApiService.getThirdPartyOrders(requestBody)
                }
                
                if (response.isSuccessful) {
                    val orders = response.body()?.value ?: emptyList()
                    _thirdPartyOrders.value = orders
                    if (orders.isEmpty()) {
                        _thirdPartyLoadingError.value = "اطلاعاتی یافت نشد"
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "بدون پیام خطا"
                    _thirdPartyLoadingError.value = "خطا در دریافت اطلاعات: کد ${response.code()}\n$errorBody"
                    Log.e("ReportsViewModel", "API Error ${response.code()}: $errorBody")
                }
            } catch (e: Exception) {
                _thirdPartyLoadingError.value = "خطا در دریافت اطلاعات: ${e.message}"
                Log.e("ReportsViewModel", "Error loading third party orders", e)
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
                    id = newQuotaData.id,
                    oldQuotaNumber = oldQuotaNumber,
                    newQuotaNumber = newQuotaData.quotaNumber,
                    shipName = newQuotaData.shipName,
                    shippingCompany = newQuotaData.shippingCompany,
                    warehouse = newQuotaData.warehouse,
                    cargoType = newQuotaData.cargoType,
                    totalTonnage = newQuotaData.totalTonnage
                )
                if (success) {
                    _currentShipName.value = newQuotaData.shipName
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

    fun toggleQuotaStatus(id: Int, quotaNumber: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val success = repository.toggleQuotaStatus(id, quotaNumber)
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
                    _currentShipName.value?.let { shipName ->
                        loadShipQuotas(shipName)
                    }
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
                onResult(Result.failure(e))
            }
        }
    }

    fun exportData(format: String, data: FilteredSummary) {
        viewModelScope.launch {
            try {
                val result = when (format.lowercase()) {
                    "pdf" -> exportPdfUseCase(data)
                    else -> throw IllegalArgumentException("Unsupported format")
                }
                _exportResult.value = result
                showFileOptions(result)
            } catch (e: Exception) {
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
                showSnackbar("خطا در نمایش گزینه‌های فایل. لطفاً دوباره تلاش کنید.")
            }
        }
    }

    fun loadComprehensiveAnalytics() {
        viewModelScope.launch {
            try {
                _analyticsLoadingState.value = LoadingState.Loading
                val response = repository.getComprehensiveAnalysis(_analyticsDateOffset.value)

                if (response.success) {
                    _comprehensiveAnalytics.value = ComprehensiveAnalytics(
                        dateInfo = response.data.dateInfo,
                        quotaCompletionAnalysis = response.data.quotaCompletionAnalysis?.map { quota ->
                            QuotaCompletionData(
                                loadingQuotaNumber = quota.loadingQuotaNumber,
                                shipName = quota.shipName,
                                shippingCompany = quota.shippingCompany,
                                last_24h_weight = quota.last_24h_weight,
                                last_24h_vouchers = quota.last_24h_vouchers,
                                cargoOwner = quota.cargoOwner,
                                warehouse = quota.warehouse,
                                cargoType = quota.cargoType
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
        val totalExitVouchers = loadingData.sumOf { it.exitVouchers }
        val shareText = StringBuilder()
        val shiftType = shiftInfo?.type ?: "نامشخص"
        val calendar = Calendar.getInstance()

        if (shiftType == "شب") {
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)
            if (currentHour < 7 || (currentHour == 7 && currentMinute < 30)) {
                calendar.add(Calendar.DAY_OF_MONTH, -1)
            }
        }

        val jalaliDate = gregorianToJalali(calendar)
        shareText.append("بارگیری [$shiftType] $jalaliDate - کل: $totalExitVouchers حواله\n\n")

        val combinedData = mutableListOf<Triple<String, String, Int>>()
        val warehouseGroupedData = loadingData.groupBy { it.loadingWarehouse }

        warehouseGroupedData.forEach { (warehouseName, data) ->
            val shipGroupedData = data.groupBy { it.shipName }
            shipGroupedData.forEach { (shipName, shipData) ->
                val exitVouchers = shipData.sumOf { it.exitVouchers }
                if (exitVouchers > 0) {
                    val capitalizedShipName = shipName.lowercase().replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    }
                    combinedData.add(Triple(warehouseName, capitalizedShipName, exitVouchers))
                }
            }
        }

        combinedData.sortBy { it.second }
        combinedData.forEach { (warehouseName, shipName, exitVouchers) ->
            shareText.append("* $warehouseName [$shipName]: $exitVouchers\n")
        }
        return shareText.toString()
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
