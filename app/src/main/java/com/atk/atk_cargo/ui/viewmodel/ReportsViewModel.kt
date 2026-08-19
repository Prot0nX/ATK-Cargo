package com.atk.atk_cargo.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import com.atk.atk_cargo.data.model.Warehouse
import com.atk.atk_cargo.data.model.WarehouseQuotaGroupingMode
import com.atk.atk_cargo.data.model.adjustColorForTheme
import com.atk.atk_cargo.data.model.cardColors
import com.atk.atk_cargo.data.repository.HttpStatusException
import com.atk.atk_cargo.data.repository.ReportsRepository
import com.atk.atk_cargo.feature.reports.domain.QuotaGroup
import com.atk.atk_cargo.feature.reports.domain.buildQuotaGroups
import com.atk.atk_cargo.utils.JalaliDateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class ReportsViewModel(
    private val repository: ReportsRepository,
    application: Application
) : AndroidViewModel(application) {
    // C-1: کل چرخه‌ی polling دیالوگ «بارگیری لحظه‌ای» (داده، شمارنده، وضعیت
    // refresh، خطا) در این یک StateFlow جمع شده تا منطق شبکه/تایمر داخل
    // Composable نباشد و با چرخش صفحه ریست نشود.
    private val _realTimeUiState = MutableStateFlow(RealTimeUiState())
    val realTimeUiState: StateFlow<RealTimeUiState> = _realTimeUiState.asStateFlow()

    // فاز ۳.۵: وضعیت مشترک صفحه‌ی گزارش کشتی/کوتاژ (لیست کشتی‌ها، کشتی/انبار/
    // کوتاژ انتخاب‌شده، خلاصه‌ی فیلترشده، بازه‌ی تاریخ، حالت‌های loading/error
    // مرتبط) در یک UiState واحد جمع شده — دقیقاً هم‌راستا با الگوی CargoUiState
    // در CargoViewModel. حوزه‌های دیگر (polling لحظه‌ای، تحلیل جامع، حالت‌های
    // مرتب‌سازی/گروه‌بندی/جستجو، رویدادهای snackbar) عمداً جدا نگه داشته
    // شده‌اند چون هرکدام یک concern مستقل با چرخه‌ی حیات/الگوی به‌روزرسانی خودشان
    // هستند، نه بخشی از همین صفحه.
    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    // فقط داخلی است (هیچ‌وقت به UI expose نشده) پس بیرون از ReportsUiState
    // نگه داشته شده — هم‌راستا با فیلدهای internal-only در CargoViewModel.
    private val _currentShipName = MutableStateFlow<String?>(null)
    private val _shipColorMap = MutableStateFlow<Map<String, Color>>(emptyMap())
    val shipColorMap: StateFlow<Map<String, Color>> = _shipColorMap.asStateFlow()
    private val colorSelector = ColorSelector(cardColors)
    private val _snackbarMessages = MutableSharedFlow<String>()
    val snackbarMessages = _snackbarMessages.asSharedFlow()
    private fun showSnackbar(message: String) {
        viewModelScope.launch {
            _snackbarMessages.emit(message)
        }
    }
    private val _shipNotFoundEvent = MutableSharedFlow<Unit>()
    val shipNotFoundEvent = _shipNotFoundEvent.asSharedFlow()
    private fun isShipNotFoundError(e: Exception): Boolean =
        e is HttpStatusException && e.statusCode == 404
    private val _comprehensiveAnalytics = MutableStateFlow<ComprehensiveAnalytics?>(null)
    val comprehensiveAnalytics: StateFlow<ComprehensiveAnalytics?> = _comprehensiveAnalytics.asStateFlow()
    private val _analyticsLoadingState = MutableStateFlow<LoadingState>(LoadingState.Idle)
    val analyticsLoadingState: StateFlow<LoadingState> = _analyticsLoadingState.asStateFlow()

    private val _analyticsDateOffset = MutableStateFlow(0)
    val analyticsDateOffset: StateFlow<Int> = _analyticsDateOffset.asStateFlow()

    // B-3 (گزارش تحلیل جامع عملیات): بدون این Job، تعویض سریع تاریخ چند
    // درخواست هم‌زمان می‌ساخت و آخرین پاسخِ رسیده (نه آخرینِ درخواست‌شده) در
    // _comprehensiveAnalytics می‌نشست. هم‌راستا با الگوی realTimeFetchJob.
    private var analyticsFetchJob: Job? = null

    fun setAnalyticsDateOffset(offset: Int) {
        if (offset in -ANALYTICS_MAX_DAYS_BACK..0) {
            _analyticsDateOffset.value = offset
            loadComprehensiveAnalytics()
        }
    }

    private val _realTimeShiftOffset = MutableStateFlow(0)
    val realTimeShiftOffset: StateFlow<Int> = _realTimeShiftOffset.asStateFlow()

    fun setRealTimeShiftOffset(offset: Int, isDarkTheme: Boolean) {
        if (offset <= 0) {
            _realTimeShiftOffset.value = offset
            viewModelScope.launch { fetchRealTimeDataCoordinated(isDarkTheme) }
        }
    }

    private val _groupingMode = MutableStateFlow(QuotaGroupingMode.BY_CARGO_OWNER)
    val groupingMode: StateFlow<QuotaGroupingMode> = _groupingMode

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // C-1 (گزارش تحلیل جامع عملیات): قبلاً داده از اینجا به QuotaAnalysis
    // Composable می‌رفت و از طریق updateInitialQuotas دوباره به ViewModel
    // push می‌شد تا فیلتر/گروه‌بندی شود — یعنی بدون رندر شدن UI، خط لوله داده
    // کار نمی‌کرد و منبع حقیقت دو تکه بود (_comprehensiveAnalytics و یک
    // _initialQuotas جداگانه). اینجا مستقیماً از همان StateFlow پاسخ سرور
    // مشتق می‌شود. debounce روی جستجو + flowOn(Default) هم فیلتر/گروه‌بندی
    // (که قبلاً برای هر ضربه کلید هم در ViewModel هم دوباره در Composable
    // روی رشته اصلی تکرار می‌شد) را یک‌بار و خارج از رشته اصلی UI انجام
    // می‌دهد (P-3/P-4). خروجی از قبل فیلتر «فعال بودن در این روز کاری»
    // (last_24h_vouchers > 0) را هم شامل می‌شود که قبلاً فقط در Composable
    // انجام می‌شد (B-4).
    @OptIn(kotlinx.coroutines.FlowPreview::class)
    val analyticsGroups: StateFlow<List<QuotaGroup>> =
        combine(
            _comprehensiveAnalytics,
            _searchQuery.debounce(250),
            _groupingMode
        ) { analytics, query, mode ->
            val active = analytics?.quotaCompletionAnalysis.orEmpty().filter { it.last_24h_vouchers > 0 }
            val trimmedQuery = query.trim().lowercase()
            val filtered = if (trimmedQuery.isEmpty()) {
                active
            } else {
                active.filter { quota ->
                    quota.shipName.lowercase().contains(trimmedQuery) ||
                        quota.loadingQuotaNumber.contains(trimmedQuery) ||
                        quota.shippingCompany.lowercase().contains(trimmedQuery)
                }
            }
            buildQuotaGroups(filtered, mode)
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _warehouseQuotaGroupingMode = MutableStateFlow(WarehouseQuotaGroupingMode.BY_CARGO_OWNER)
    val warehouseQuotaGroupingMode: StateFlow<WarehouseQuotaGroupingMode> = _warehouseQuotaGroupingMode.asStateFlow()

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
    }

    // تابع به‌روزرسانی کوئری جستجو
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // بارگذاری اولیه توسط ShipsListScreen (LaunchedEffect) انجام می‌شود؛ فراخوانی
    // این‌جا هم باعث دو درخواست هم‌زمان روی سنگین‌ترین کوئری سرور می‌شد.

    fun formatNumber(number: Number): String {
        return NumberFormat.getNumberInstance(Locale.US).format(number)
    }

    fun setCurrentShipName(shipName: String) {
        _currentShipName.value = shipName
    }

    fun clearCurrentShipData() {
        _currentShipName.value = null
        _uiState.update { it.copy(selectedShip = null, selectedShipQuotas = emptyList()) }
    }

    fun loadShips() {
        viewModelScope.launch {
            _uiState.update { it.copy(status = UiState.Loading) }
            try {
                val shipsData = repository.getShipsList()
                _uiState.update { it.copy(ships = shipsData, status = UiState.Success) }
            } catch (e: Exception) {
                _uiState.update { it.copy(status = UiState.Error("خطا در بارگیری لیست کشتی‌ها: ${e.message}")) }
            }
        }
    }

    private var realTimeFetchJob: Job? = null
    companion object {
        private const val REAL_TIME_REFRESH_INTERVAL_MS = 30_000L

        // C-4 (گزارش تحلیل جامع عملیات): قبلاً عدد -7 مستقل هم اینجا و هم در
        // ComprehensiveAnalyticsDialog.kt hardcode شده بود و هیچ‌چیز هماهنگی
        // آن‌ها را تضمین نمی‌کرد. معادل سمت سرور همین مقدار
        // AnalyticsController::MAX_ANALYTICS_DAYS_BACK در PHP است؛ چون کلاینت
        // و سرور دو runtime جدا هستند، این عدد باید دستی هم‌زمان با آن ثابت
        // تغییر کند.
        const val ANALYTICS_MAX_DAYS_BACK = 7
    }

    private suspend fun fetchRealTimeData(isDarkTheme: Boolean) {
        try {
            val response = repository.getRealTimeLoadingData(_realTimeShiftOffset.value)
            _realTimeUiState.update {
                it.copy(
                    data = response.data.sortedByDescending { d -> d.entryVouchers },
                    shiftInfo = response.shiftInfo,
                    error = null
                )
            }

            // تخصیص رنگ‌های کاملاً متمایز فقط به کشتی‌ها (تنها مصرف واقعی رنگ در
            // دیالوگ بارگیری لحظه‌ای؛ نگاشت مشابه به‌ازای هر کوتاژ قبلاً هم محاسبه
            // می‌شد هم در ViewModel نگه داشته می‌شد اما هیچ‌جا خوانده نمی‌شد — P-5).
            val shipNames = response.data.map { it.shipName }.distinct().toSet()
            val shipColors = colorSelector.assignDistinctColors(shipNames)
                .mapValues { (_, color) -> adjustColorForTheme(color, isDarkTheme) }
            _shipColorMap.value = shipColors

        } catch (e: Exception) {
            _realTimeUiState.update { it.copy(error = "خطا در دریافت اطلاعات: ${e.message}") }
        }
    }

    /**
     * درخواست قبلی (مثلاً از یک کلیک سریع روی فلش‌های شیفت یا از تیک polling
     * هم‌زمان با refresh دستی) لغو می‌شود تا پاسخی که دیرتر برسد، نه لزوماً
     * پاسخ متعلق به آخرین درخواست، state را بازنویسی نکند؛ سپس تا پایان واقعی
     * صبر می‌کند تا caller بتواند بر اساس نتیجه واکنش نشان دهد.
     */
    private suspend fun fetchRealTimeDataCoordinated(isDarkTheme: Boolean) {
        realTimeFetchJob?.cancel()
        val job = viewModelScope.launch { fetchRealTimeData(isDarkTheme) }
        realTimeFetchJob = job
        job.join()
    }

    /**
     * C-1: کل حلقه‌ی polling دیالوگ «بارگیری لحظه‌ای» اینجاست، نه در Composable.
     * caller این تابع را داخل repeatOnLifecycle(RESUMED) اجرا می‌کند؛ با لغو آن
     * کوروتین (پس‌زمینه رفتن اپ یا بسته‌شدن دیالوگ)، این حلقه هم متوقف می‌شود.
     */
    suspend fun startRealTimePolling(isDarkTheme: Boolean) {
        var nextRefreshAt = System.currentTimeMillis() + REAL_TIME_REFRESH_INTERVAL_MS
        _realTimeUiState.update { it.copy(secondsToNextRefresh = 30) }
        fetchRealTimeDataCoordinated(isDarkTheme)
        while (true) {
            delay(200)
            val remainingMs = nextRefreshAt - System.currentTimeMillis()
            val remainingSeconds = (remainingMs / 1000L).toInt().coerceAtLeast(0)
            _realTimeUiState.update { it.copy(secondsToNextRefresh = remainingSeconds) }
            if (remainingMs <= 0) {
                _realTimeUiState.update { it.copy(isRefreshing = true) }
                fetchRealTimeDataCoordinated(isDarkTheme)
                delay(500)
                _realTimeUiState.update { it.copy(isRefreshing = false, secondsToNextRefresh = 30) }
                // هدف بعدی از روی هدف قبلی محاسبه می‌شود نه "الان + ۳۰ ثانیه"،
                // وگرنه مدت fetch/delay(500) هر دور به drift اضافه می‌شود.
                nextRefreshAt = maxOf(
                    nextRefreshAt + REAL_TIME_REFRESH_INTERVAL_MS,
                    System.currentTimeMillis() + 1000L
                )
            }
        }
    }

    /**
     * برای دکمه‌ی refresh دستی؛ پیام خطای واقعی بعد از پایان درخواست را
     * برمی‌گرداند (null یعنی موفق) تا UI بر اساس نتیجه‌ی واقعی Toast نشان دهد.
     */
    suspend fun refreshRealTimeDataManually(isDarkTheme: Boolean): String? {
        _realTimeUiState.update { it.copy(isRefreshing = true) }
        fetchRealTimeDataCoordinated(isDarkTheme)
        delay(300)
        _realTimeUiState.update { it.copy(isRefreshing = false, secondsToNextRefresh = 30) }
        return _realTimeUiState.value.error
    }

    fun loadWarehouseDetails(shipName: String, warehouseName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(status = UiState.Loading) }
            try {
                val warehouseDetails = repository.getWarehouseDetails(shipName, warehouseName)
                val availableDates = warehouseDetails.quotas
                    .flatMap { quota ->
                        quota.exitDates?.map { exitDate -> exitDate.date } ?: emptyList()
                    }
                    .distinct()
                    .sorted()
                _uiState.update {
                    it.copy(
                        selectedWarehouse = warehouseDetails.copy(availableExitDates = availableDates),
                        status = UiState.Success
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(status = UiState.Error("Failed to load warehouse details: ${e.message}")) }
            }
        }
    }

    fun loadQuotaDetails(quotaNumber: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(status = UiState.Loading) }
            try {
                val quotaDetails = repository.getQuotaDetails(quotaNumber)
                _uiState.update { it.copy(selectedQuotaDetails = quotaDetails, status = UiState.Success) }
            } catch (e: Exception) {
                _uiState.update { it.copy(status = UiState.Error("خطا در بارگیری جزئیات کوتاژ: ${e.message}")) }
            }
        }
    }

    fun loadShipQuotas(shipName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingShipQuotas = true, shipQuotasLoadingState = LoadingState.Idle) }

            try {
                val quotas = withContext(Dispatchers.IO) {
                    repository.getShipQuotas(shipName)
                }

                _uiState.update {
                    it.copy(
                        selectedShipQuotas = quotas,
                        shipQuotasLoadingState = LoadingState.Idle,
                        status = if (it.shipDetailsLoadingState !is LoadingState.Error) UiState.Success else it.status
                    )
                }

            } catch (e: Exception) {
                val errorMessage = "خطا در بارگیری کوتاژهای کشتی: ${e.message}"
                _uiState.update {
                    it.copy(
                        shipQuotasLoadingState = LoadingState.Error(errorMessage),
                        status = if (it.selectedShipQuotas.isEmpty()) UiState.Error(errorMessage) else it.status
                    )
                }
            } finally {
                _uiState.update { it.copy(isLoadingShipQuotas = false) }
            }
        }
    }

    fun loadShipDataAsync(shipName: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(status = UiState.Loading, isLoadingShipDetails = true, isLoadingShipQuotas = true)
            }

            try {
                supervisorScope {
                    val shipDetailsDeferred = async { repository.getShipDetails(shipName) }
                    val shipQuotasDeferred = async { repository.getShipQuotas(shipName) }

                    val shipDetails = shipDetailsDeferred.await()
                    val quotas = shipQuotasDeferred.await()

                    _currentShipName.value = shipName
                    _uiState.update {
                        it.copy(
                            selectedShip = shipDetails,
                            selectedShipQuotas = quotas,
                            shipDetailsLoadingState = LoadingState.Idle,
                            shipQuotasLoadingState = LoadingState.Idle,
                            status = UiState.Success
                        )
                    }
                }
            } catch (e: Exception) {
                if (isShipNotFoundError(e)) {
                    _shipNotFoundEvent.emit(Unit)
                    return@launch
                }
                val errorMessage = "خطا در بارگیری اطلاعات کشتی: ${e.message}"
                _uiState.update {
                    it.copy(
                        status = UiState.Error(errorMessage),
                        shipDetailsLoadingState = LoadingState.Error(errorMessage),
                        shipQuotasLoadingState = LoadingState.Error(errorMessage)
                    )
                }
            } finally {
                _uiState.update { it.copy(isLoadingShipDetails = false, isLoadingShipQuotas = false) }
            }
        }
    }

    fun refreshShipDataSilently(shipName: String) {
        viewModelScope.launch {
            try {
                supervisorScope {
                    val shipDetailsDeferred = async { repository.getShipDetails(shipName) }
                    val shipQuotasDeferred = async { repository.getShipQuotas(shipName) }

                    val shipDetails = shipDetailsDeferred.await()
                    val quotas = shipQuotasDeferred.await()
                    _currentShipName.value = shipName
                    _uiState.update { it.copy(selectedShip = shipDetails, selectedShipQuotas = quotas) }
                }
            } catch (e: Exception) {
                if (isShipNotFoundError(e)) {
                    _shipNotFoundEvent.emit(Unit)
                    return@launch
                }
                showSnackbar("خطا در بروزرسانی اطلاعات: ${e.message}")
            }
        }
    }

    fun loadFilteredShipQuotas(shipName: String, startDateTime: String, endDateTime: String) {
        viewModelScope.launch {
            try {
                val quotas = repository.getFilteredQuotas(shipName, startDateTime, endDateTime)
                _uiState.update {
                    it.copy(selectedShipQuotas = quotas, selectedDateRange = Pair(startDateTime, endDateTime))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(loadingError = "خطا در بارگیری کوتاژهای فیلتر شده: ${e.message}") }
            }
        }
    }

    fun clearLoadingError() {
        _uiState.update { it.copy(loadingError = null) }
    }

    fun clearSelectedDateRange() {
        _uiState.update { it.copy(selectedDateRange = null) }
        _uiState.value.selectedShip?.name?.let { shipName ->
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
            _uiState.update { it.copy(status = UiState.Loading) }
            try {
                val summary = repository.getFilteredSummary(
                    shipName = shipName,
                    warehouseName = warehouseName,
                    selectedQuota = selectedQuota,
                    startDateTime = startDateTime ?: "",
                    endDateTime = endDateTime ?: ""
                )
                _uiState.update { it.copy(filteredSummary = summary, status = UiState.Success) }
            } catch (e: Exception) {
                _uiState.update { it.copy(status = UiState.Error("خطا در دریافت خلاصه فیلتر شده: ${e.message}")) }
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
                    id = data.id,
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

    // quotaNumber دیگر برای درخواست سرور استفاده نمی‌شود (سرور فقط با id کار
    // می‌کند)؛ پارامتر برای سازگاری با فراخوان‌های موجود در UI نگه داشته شده است.
    fun toggleQuotaStatus(id: Int, quotaNumber: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val success = repository.toggleQuotaStatus(id)
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

    /**
     * غیرفعال کردن دسته‌ای کوتاژهای هشداردار (دکمه‌ی گروهی دیالوگ هشدار).
     * برخلاف toggleQuotaStatus (که به ازای هر فراخوانی یک رفرش کامل انجام
     * می‌دهد و برای N کوتاژ به N×۴ درخواست HTTP می‌رسد و سقف Rate Limit
     * پروکسی -۶۰ درخواست در دقیقه- را رد می‌کند)، اینجا همه‌ی toggleها ابتدا
     * اجرا و فقط یک‌بار در پایان رفرش می‌شوند. هر id قبل از ارسال با آخرین
     * وضعیت شناخته‌شده‌ی کوتاژها چک می‌شود تا کوتاژی که بین محاسبه‌ی هشدار و
     * کلیک کاربر از جای دیگری غیرفعال شده، دوباره فعال نشود.
     */
    fun deactivateQuotasInBulk(quotaIds: List<Int>, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val stillActiveIds = quotaIds.filter { id ->
                    id > 0 && _uiState.value.selectedShipQuotas.find { it.id == id }?.isActive != false
                }

                var successCount = 0
                var failureCount = 0
                stillActiveIds.forEach { id ->
                    try {
                        if (repository.toggleQuotaStatus(id)) successCount++ else failureCount++
                    } catch (e: Exception) {
                        failureCount++
                    }
                }

                _currentShipName.value?.let { shipName ->
                    refreshShipDataSilently(shipName)
                }
                loadShips()

                showSnackbar(
                    when {
                        failureCount == 0 && successCount > 0 -> "$successCount کوتاژ با موفقیت غیرفعال شد"
                        successCount > 0 -> "$successCount کوتاژ غیرفعال شد، $failureCount مورد ناموفق بود"
                        else -> "خطا در غیرفعال کردن کوتاژها"
                    }
                )
            } finally {
                onComplete()
            }
        }
    }

    fun toggleQuotaPercentageRestriction(id: Int, isRestricted: Boolean, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val success = repository.updateQuotaPercentageRestriction(
                    id = id,
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
                    refreshShipDataSilently(_uiState.value.selectedShip?.name ?: "")
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
        _uiState.update { it.copy(filteredSummary = null) }
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


    fun loadComprehensiveAnalytics() {
        analyticsFetchJob?.cancel()
        analyticsFetchJob = viewModelScope.launch {
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
            } catch (e: HttpStatusException) {
                // A-3: به‌جای نمایش کد/بدنه خام JSON سرور، پیام فارسی واضح بر اساس
                // کد وضعیت HTTP (که قبلاً به‌خاطر throw Exception ساده تشخیص‌پذیر
                // نبود) نمایش داده می‌شود.
                val message = when (e.statusCode) {
                    401 -> "نشست شما منقضی شده است. لطفاً دوباره وارد شوید."
                    403 -> "شما مجوز مشاهده آمار تحلیلی را ندارید."
                    429 -> "درخواست‌های زیاد. لطفاً کمی صبر کنید و دوباره تلاش کنید."
                    else -> "خطا در دریافت اطلاعات تحلیلی (کد ${e.statusCode})"
                }
                _analyticsLoadingState.value = LoadingState.Error(message)
            } catch (e: Exception) {
                _analyticsLoadingState.value = LoadingState.Error(e.message ?: "خطای ناشناخته")
            }
        }
    }

    // A-5 (گزارش تحلیل جامع عملیات): fire-and-forget — UI منتظر نتیجه این
    // فراخوانی نمی‌ماند تا اشتراک‌گذاری واقعی (OS share sheet) بدون تأخیر باز شود.
    fun logAnalyticsExport(scope: String, groupCount: Int) {
        viewModelScope.launch {
            repository.logAnalyticsExport(scope, groupCount)
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

    data class ReportsUiState(
        val status: UiState = UiState.Loading,
        val ships: ShipsData = ShipsData(emptyList(), emptyList()),
        val selectedShip: Ship? = null,
        val selectedWarehouse: Warehouse? = null,
        val selectedQuotaDetails: QuotaDetails? = null,
        val selectedShipQuotas: List<Quota> = emptyList(),
        val filteredSummary: FilteredSummary? = null,
        val selectedDateRange: Pair<String, String>? = null,
        val shipDetailsLoadingState: LoadingState = LoadingState.Idle,
        val shipQuotasLoadingState: LoadingState = LoadingState.Idle,
        val isLoadingShipDetails: Boolean = false,
        val isLoadingShipQuotas: Boolean = false,
        val loadingError: String? = null
    )

    data class RealTimeUiState(
        val data: List<RealTimeLoadingData> = emptyList(),
        val shiftInfo: ShiftInfo? = null,
        val isRefreshing: Boolean = false,
        val error: String? = null,
        val secondsToNextRefresh: Int = 30
    )



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
        val jalaliDate = shiftInfo?.startDate ?: JalaliDateUtils.formatDate(System.currentTimeMillis().toString())
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
}
