package com.atk.atk_cargo.feature.reports.presentation


import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.atk.atk_cargo.api.TokenStore
import com.atk.atk_cargo.api.validateServerSession
import com.atk.atk_cargo.core.ui.components.PersianDatePickerDialog
import com.atk.atk_cargo.data.model.CargoInfo
import com.atk.atk_cargo.domain.session.UserPreferencesStore
import com.atk.atk_cargo.feature.reports.presentation.components.FloatingActionButton
import com.atk.atk_cargo.feature.reports.presentation.details.ShipDetails
import com.atk.atk_cargo.feature.reports.presentation.dialogs.AdvancedSearchDialog
import com.atk.atk_cargo.feature.reports.presentation.dialogs.ComprehensiveAnalyticsDialog
import com.atk.atk_cargo.feature.reports.presentation.dialogs.MultipleSearchResultDialog
import com.atk.atk_cargo.feature.reports.presentation.dialogs.QuotaManagementDialog
import com.atk.atk_cargo.feature.reports.presentation.dialogs.RealTimeLoadingBottomSheet
import com.atk.atk_cargo.feature.reports.presentation.dialogs.SearchResultDialog
import com.atk.atk_cargo.feature.reports.presentation.dialogs.SearchType
import com.atk.atk_cargo.feature.reports.presentation.quota_details.QuotaDetails
import com.atk.atk_cargo.feature.reports.presentation.quota_details.QuotasDialog
import com.atk.atk_cargo.feature.reports.presentation.ships.ShipsList
import com.atk.atk_cargo.feature.reports.presentation.warehouse_details.WarehouseDetails
import com.atk.atk_cargo.feature.reports.viewmodel.ReportsViewModel
import com.atk.atk_cargo.ui.theme.ATKCargoTheme
import com.atk.atk_cargo.ui.theme.Corner3XL
import com.atk.atk_cargo.ui.theme.CornerL
import com.atk.atk_cargo.ui.theme.CornerXL
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

// onSessionInvalid از بیرون (:app) پاس داده می‌شود تا این ماژول به feature:home وابسته نشود (که خودش به feature:reports
// وابسته است و وابستگی معکوس ایجاد می‌کرد)؛ نبود آن یعنی این صفحه مستقل از گراف ناوبری اصلی میزبانی شده، پس با
// راه‌اندازی مجدد اکتیویتی پیش‌فرض برنامه (بدون رفرنس مستقیم به MainActivity) بازیابی می‌شود (DEEP_CODE_AUDIT.md فاز۳ #۲۴)
@RequiresApi(Build.VERSION_CODES.HONEYCOMB)
@Composable
fun ManageReportsScreen(viewModel: ReportsViewModel, onSessionInvalid: (() -> Unit)? = null) {
    val context = LocalContext.current
    // UserPreferencesManager (پیاده‌سازی واقعی) در :app است؛ این ماژول از طریق اینترفیس‌های مرزی TokenStore/UserPreferencesStore
    // به همان سینگلتون دسترسی دارد (الگوی از قبل موجود در AppModule.kt، DEEP_CODE_AUDIT.md فاز۳ #۲۴)
    val tokenStore = koinInject<TokenStore>()
    val userPreferencesStore = koinInject<UserPreferencesStore>()

    fun relaunchApp() {
        if (onSessionInvalid != null) {
            onSessionInvalid()
        } else {
            context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { intent ->
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            val result = validateServerSession(tokenStore)
            result.fold(
                onSuccess = {
                    // Session معتبر است، ادامه می‌دهد
                },
                onFailure = { relaunchApp() }
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("ManageReportsScreen", "خطا در بررسی وضعیت ورود: ${e.message}")
            relaunchApp()
        }
    }

    val navController = rememberNavController()
    var showQuotasDialog by remember { mutableStateOf(false) }
    var selectedShipForQuotas by remember { mutableStateOf<String?>(null) }
    var showRealTimeDialog by remember { mutableStateOf(false) }
    var showAdvancedSearchDialog by remember { mutableStateOf(false) }
    var showAnalyticsDialog by remember { mutableStateOf(false) }
    var showDateRangeDialog by remember { mutableStateOf(false) }
    var showQuotaManagementDialog by remember { mutableStateOf(false) }
    var currentSelectedSection by remember { mutableIntStateOf(0) }
    var isInShipDetailsScreen by remember { mutableStateOf(false) }
    val realTimeUiState by viewModel.realTimeUiState.collectAsStateWithLifecycle()
    var searchResult by remember { mutableStateOf<CargoInfo?>(null) }
    var multipleSearchResults by remember { mutableStateOf<List<CargoInfo>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lastSearchType by remember { mutableStateOf<SearchType?>(null) }
    var lastSearchValue by remember { mutableStateOf<String?>(null) }
    val isDarkTheme = isSystemInDarkTheme()
    val reportsUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentShipName = reportsUiState.selectedShip
    val loadingError = reportsUiState.loadingError

    // A-1: مجوز view_reports اکنون سمت کلاینت هم چک می‌شود؛ userPermissions از همان Flow ذخیره‌شده‌ی DataStore می‌آید که PermissionPoller به‌روز می‌کند (حداکثر با تأخیر یک دور polling، ۳ دقیقه)
    val userPermissions by userPreferencesStore.permissions.collectAsStateWithLifecycle(initialValue = emptyMap())
    val canViewReports = userPermissions["view_reports"] == true

    ATKCargoTheme(darkTheme = isSystemInDarkTheme()) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = "shipsList",
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("shipsList") {
                        LaunchedEffect(Unit) {
                            currentSelectedSection = 0
                            isInShipDetailsScreen = false
                            viewModel.clearSelectedDateRange()
                        }

                        ShipsList(
                            viewModel = viewModel,
                            onShipSelected = { shipName ->
                                // نام کشتی از سرور می‌آید؛ بدون encode، نامی حاوی '/'، '?' یا '#' مسیر ناوبری را می‌شکند
                                navController.navigate("shipDetails/${Uri.encode(shipName)}") {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    composable("shipDetails/{shipName}") { backStackEntry ->
                        val shipName = backStackEntry.arguments?.getString("shipName")
                            ?.let { Uri.decode(it) } ?: return@composable
                        LaunchedEffect(Unit) {
                            isInShipDetailsScreen = true
                        }
                        ShipDetails(
                            initialShipName = shipName,
                            viewModel = viewModel,
                            onWarehouseSelected = { warehouseName ->
                                navController.navigate("warehouseDetails/${Uri.encode(shipName)}/${Uri.encode(warehouseName)}") {
                                    launchSingleTop = true
                                }
                            },
                            onSectionChanged = { section ->
                                currentSelectedSection = section
                            },
                            onShipNotFound = {
                                navController.popBackStack("shipsList", inclusive = false)
                            }
                        )
                    }
                    composable("warehouseDetails/{shipName}/{warehouseName}") { backStackEntry ->
                        val shipName = backStackEntry.arguments?.getString("shipName")
                            ?.let { Uri.decode(it) } ?: return@composable
                        val warehouseName = backStackEntry.arguments?.getString("warehouseName")
                            ?.let { Uri.decode(it) } ?: return@composable
                        LaunchedEffect(Unit) {
                            isInShipDetailsScreen = false
                        }
                        WarehouseDetails(
                            shipName = shipName,
                            warehouseName = warehouseName,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("quotaDetails/{quotaNumber}") { backStackEntry ->
                        val quotaNumber = backStackEntry.arguments?.getString("quotaNumber") ?: return@composable
                        LaunchedEffect(Unit) {
                            isInShipDetailsScreen = false
                        }
                        QuotaDetails(
                            quotaNumber = quotaNumber,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }

    if (showQuotasDialog && selectedShipForQuotas != null) {
        QuotasDialog(
            shipName = selectedShipForQuotas!!,
            quotas = reportsUiState.selectedShipQuotas,
            onDismiss = {
                showQuotasDialog = false
                selectedShipForQuotas = null
            },
            onEdit = { oldQuotaNumber, newQuotaData ->
                viewModel.editQuota(oldQuotaNumber, newQuotaData)
            },
            onToggleStatus = { id, quotaNumber ->
                viewModel.toggleQuotaStatus(id, quotaNumber)
            },
            onDelete = { quotaNumber ->
                viewModel.deleteQuota(quotaNumber)
            },
            viewModel = viewModel
        )
    }

    // دفاع دوم (defense-in-depth): بدون مجوز کاربر دیالوگ باز نمی‌شود، حتی اگر مسیر دیگری showAnalyticsDialog را true کند
    ComprehensiveAnalyticsDialog(
        isVisible = showAnalyticsDialog && canViewReports,
        onDismiss = { showAnalyticsDialog = false },
        viewModel = viewModel
    )

    FloatingActionButton(
        onRealTimeLoadingClick = {
            showRealTimeDialog = true
        },
        onAdvancedSearchClick = {
            showAdvancedSearchDialog = true
        },
        onAnalyticsClick = if (canViewReports) {
            { showAnalyticsDialog = true }
        } else null,
        onDateRangeClick = if (isInShipDetailsScreen && currentSelectedSection == 0) {
            { showDateRangeDialog = true }
        } else null,
        onQuotaManagementClick = {
            showQuotaManagementDialog = true
        }
    )

    val realTimeShipColorMap by viewModel.shipColorMap.collectAsStateWithLifecycle()
    val realTimeShiftOffset by viewModel.realTimeShiftOffset.collectAsStateWithLifecycle()

    RealTimeLoadingBottomSheet(
        isOpen = showRealTimeDialog,
        onDismiss = { showRealTimeDialog = false },
        uiState = realTimeUiState,
        shiftOffset = realTimeShiftOffset,
        shipColorMap = realTimeShipColorMap,
        onStartPolling = { viewModel.startRealTimePolling(isDarkTheme) },
        onShiftOffsetChange = { offset -> viewModel.setRealTimeShiftOffset(offset, isDarkTheme) },
        onManualRefresh = { viewModel.refreshRealTimeDataManually(isDarkTheme) },
        onShare = { data, info -> viewModel.shareRealTimeLoadingData(data, info) }
    )

    AdvancedSearchDialog(
        isOpen = showAdvancedSearchDialog,
        onDismiss = { showAdvancedSearchDialog = false },
        onSearchReceipt = { receiptNumber ->
            lastSearchType = SearchType.RECEIPT_NUMBER
            lastSearchValue = receiptNumber
            viewModel.performAdvancedSearch(receiptNumber) { result ->
                result.fold(
                    onSuccess = { cargoInfo ->
                        if (cargoInfo != null) {
                            searchResult = cargoInfo
                            showAdvancedSearchDialog = false
                        } else {
                            errorMessage = "اطلاعاتی برای این شماره قبض یافت نشد."
                        }
                    },
                    onFailure = { error ->
                        errorMessage = "خطا در جستجو: ${error.localizedMessage}"
                    }
                )
            }
        },
        onSearchTracking = { trackingNumber ->
            lastSearchType = SearchType.TRACKING_NUMBER
            lastSearchValue = trackingNumber
            viewModel.performAdvancedSearchByTracking(trackingNumber) { result ->
                result.fold(
                    onSuccess = { cargoInfoList ->
                        if (cargoInfoList.isNotEmpty()) {
                            multipleSearchResults = cargoInfoList
                            showAdvancedSearchDialog = false
                        } else {
                            errorMessage = "اطلاعاتی برای این شماره حواله یافت نشد."
                        }
                    },
                    onFailure = { error ->
                        errorMessage = "خطا در جستجو: ${error.localizedMessage}"
                    }
                )
            }
        }
    )

    searchResult?.let { cargo ->
        SearchResultDialog(
            cargoInfo = cargo,
            onDismiss = { searchResult = null },
            onRefresh = { updatedCargo ->
                searchResult = updatedCargo
            },
            searchType = lastSearchType,
            searchValue = lastSearchValue,
            viewModel = viewModel
        )
    }

    multipleSearchResults?.let { cargoList ->
        MultipleSearchResultDialog(
            cargoInfoList = cargoList,
            onDismiss = { multipleSearchResults = null },
            onSelectCargo = { selectedCargo ->
                multipleSearchResults = null
                searchResult = selectedCargo
            }
        )
    }

    QuotaManagementDialog(
        isVisible = showQuotaManagementDialog,
        onDismiss = { showQuotaManagementDialog = false },
        viewModel = viewModel
    )

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("خطا") },
            text = { Text(errorMessage!!) },
            confirmButton = {
                Button(onClick = { errorMessage = null }) {
                    Text("تایید")
                }
            }
        )
    }

    // نمایش خطاهای بارگیری
    if (loadingError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearLoadingError() },
            title = { Text("خطا") },
            text = { Text(loadingError!!) },
            confirmButton = {
                Button(onClick = { viewModel.clearLoadingError() }) {
                    Text("تایید")
                }
            }
        )
    }

    PersianDateRangePickerDialog(
        isOpen = showDateRangeDialog,
        onDismiss = { showDateRangeDialog = false },
        onDateRangeSelected = { startDate, endDate ->
            val shipName = currentShipName?.name
            if (shipName != null) {
                viewModel.loadFilteredShipQuotas(shipName, startDate, endDate)
            }
            showDateRangeDialog = false
        }
    )
}

fun getDaysInPersianMonth(year: Int, month: Int): Int {
    return when (month) {
        in 1..6 -> 31
        in 7..11 -> 30
        12 -> if (isPersianLeapYear(year)) 30 else 29
        else -> 30
    }
}

fun isPersianLeapYear(year: Int): Boolean {
    val remainder = year % 33
    return remainder == 1 || remainder == 5 || remainder == 9 || 
           remainder == 13 || remainder == 17 || remainder == 22 || 
           remainder == 26 || remainder == 30
}

@SuppressLint("DefaultLocale")
fun addOneDayToPersianDate(date: String): String {
    val parts = date.split("/")
    if (parts.size != 3) return date

    var year = parts[0].toIntOrNull() ?: return date
    var month = parts[1].toIntOrNull() ?: return date
    var day = parts[2].toIntOrNull() ?: return date

    day++
    val daysInMonth = getDaysInPersianMonth(year, month)

    if (day > daysInMonth) {
        day = 1
        month++
    }

    if (month > 12) {
        month = 1
        year++
    }

    return "%04d/%02d/%02d".format(year, month, day)
}

@SuppressLint("DefaultLocale")
@Composable
fun PersianDateRangePickerDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onDateRangeSelected: (startDate: String, endDate: String) -> Unit
) {
    // State variables for selected dates - حفظ آخرین انتخاب
    var startDateText by rememberSaveable { mutableStateOf("1404/01/01") }
    var startTimeText by rememberSaveable { mutableStateOf("07:00") }
    var endDateText by rememberSaveable { mutableStateOf("1404/01/02") }
    var endTimeText by rememberSaveable { mutableStateOf("07:00") }

    if (isOpen) {
        // Dialog states
        var showStartDatePicker by remember { mutableStateOf(false) }
        var showStartTimePicker by remember { mutableStateOf(false) }
        var showEndDatePicker by remember { mutableStateOf(false) }
        var showEndTimePicker by remember { mutableStateOf(false) }

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .wrapContentHeight()
                    .padding(16.dp),
                shape = RoundedCornerShape(Corner3XL),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 12.dp,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // عنوان دیالوگ مدرن
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "انتخاب بازه زمانی",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "بازه گزارش مورد نظر خود را مشخص کنید",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // بخش انتخاب تاریخ‌ها
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // بخش شروع
                        DateTimeSelectionCard(
                            title = "از تاریخ",
                            dateValue = startDateText,
                            timeValue = startTimeText,
                            icon = Icons.Default.CalendarToday,
                            accentColor = MaterialTheme.colorScheme.primary,
                            onDateClick = { showStartDatePicker = true },
                            onTimeClick = { showStartTimePicker = true },
                            modifier = Modifier.weight(1f)
                        )

                        // بخش پایان
                        DateTimeSelectionCard(
                            title = "تا تاریخ",
                            dateValue = endDateText,
                            timeValue = endTimeText,
                            icon = Icons.Default.Event,
                            accentColor = MaterialTheme.colorScheme.secondary,
                            onDateClick = { showEndDatePicker = true },
                            onTimeClick = { showEndTimePicker = true },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // دکمه‌های عمل مدرن
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(CornerXL),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Text("انصراف", style = MaterialTheme.typography.titleMedium)
                        }

                        Button(
                            onClick = {
                                val startDateTime = "$startDateText $startTimeText"
                                val endDateTime = "$endDateText $endTimeText"
                                onDateRangeSelected(startDateTime, endDateTime)
                            },
                            shape = RoundedCornerShape(CornerXL),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("اعمال فیلتر", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // دیالوگ انتخاب تاریخ شروع
        if (showStartDatePicker) {
            DatePickerDialog(
                isOpen = true,
                title = "تاریخ شروع گزارش",
                initialDate = startDateText,
                accentColor = MaterialTheme.colorScheme.primary,
                onDismiss = { showStartDatePicker = false },
                onDateSelected = { date ->
                    startDateText = date
                    endDateText = addOneDayToPersianDate(date)
                    showStartDatePicker = false
                }
            )
        }

        // دیالوگ انتخاب زمان شروع
        if (showStartTimePicker) {
            TimePickerDialog(
                isOpen = true,
                title = "ساعت شروع",
                initialTime = startTimeText,
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                titleColor = MaterialTheme.colorScheme.primary,
                onDismiss = { showStartTimePicker = false },
                onTimeSelected = { time ->
                    startTimeText = time
                    showStartTimePicker = false
                }
            )
        }

        // دیالوگ انتخاب تاریخ پایان
        if (showEndDatePicker) {
            DatePickerDialog(
                isOpen = true,
                title = "تاریخ پایان گزارش",
                initialDate = endDateText,
                accentColor = MaterialTheme.colorScheme.secondary,
                minDate = startDateText,
                onDismiss = { showEndDatePicker = false },
                onDateSelected = { date ->
                    endDateText = date
                    showEndDatePicker = false
                }
            )
        }

        // دیالوگ انتخاب زمان پایان
        if (showEndTimePicker) {
            TimePickerDialog(
                isOpen = true,
                title = "ساعت پایان",
                initialTime = endTimeText,
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                titleColor = MaterialTheme.colorScheme.secondary,
                onDismiss = { showEndTimePicker = false },
                onTimeSelected = { time ->
                    endTimeText = time
                    showEndTimePicker = false
                }
            )
        }
    }
}

@Composable
fun DateTimeSelectionCard(
    title: String,
    dateValue: String,
    timeValue: String,
    icon: ImageVector,
    accentColor: Color,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(CornerXL),
            color = accentColor.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // دکمه تاریخ
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CornerL))
                        .clickable { onDateClick() }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, null, tint = accentColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateValue,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = accentColor.copy(alpha = 0.1f)
                )
                
                // دکمه ساعت
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CornerL))
                        .clickable { onTimeClick() }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AccessTime, null, tint = accentColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = timeValue,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun DatePickerDialog(
    isOpen: Boolean,
    title: String,
    initialDate: String,
    accentColor: Color,
    minDate: String? = null,
    onDismiss: () -> Unit,
    onDateSelected: (date: String) -> Unit
) {
    PersianDatePickerDialog(
        isOpen = isOpen,
        title = title,
        initialDate = initialDate,
        accentColor = accentColor,
        minDate = minDate,
        onDismiss = onDismiss,
        onDateSelected = onDateSelected
    )
}

@SuppressLint("DefaultLocale")
@Composable
fun TimePickerDialog(
    isOpen: Boolean,
    title: String,
    initialTime: String,
    containerColor: Color,
    titleColor: Color,
    onDismiss: () -> Unit,
    onTimeSelected: (time: String) -> Unit
) {
    if (isOpen) {
        // Parse initial time
        val timeParts = initialTime.split(":")
        var selectedHour by remember { mutableIntStateOf(timeParts.getOrNull(0)?.toIntOrNull() ?: 8) }
        var selectedMinute by remember { mutableIntStateOf(timeParts.getOrNull(1)?.toIntOrNull() ?: 0) }

        val hours = (0..23).toList()
        val minutes = (0..59).toList()

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(Corner3XL),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 12.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth()
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.AccessTime, null, tint = titleColor, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Time Value Display
                    Surface(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        shape = RoundedCornerShape(CornerXL),
                        color = containerColor
                    ) {
                        Text(
                            text = "%02d:%02d".format(selectedHour, selectedMinute),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = titleColor,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ScrollableSelector(
                            label = "ساعت",
                            items = hours,
                            selectedItem = selectedHour,
                            onItemSelected = { selectedHour = it },
                            modifier = Modifier.weight(1f),
                            formatItem = { "%02d".format(it) }
                        )

                        ScrollableSelector(
                            label = "دقیقه",
                            items = minutes,
                            selectedItem = selectedMinute,
                            onItemSelected = { selectedMinute = it },
                            modifier = Modifier.weight(1f),
                            formatItem = { "%02d".format(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                onTimeSelected("%02d:%02d".format(selectedHour, selectedMinute))
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(CornerL)
                        ) {
                            Text("انتخاب")
                        }
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(CornerL)
                        ) {
                            Text("انصراف")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScrollableSelector(
    label: String,
    items: List<Int>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    formatItem: (Int) -> String = { it.toString() }
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                val density = LocalDensity.current
                val selectedIndex = items.indexOf(selectedItem)
                val itemHeight = 40.dp
                val visibleItemsCount = 3
                val viewportHeight = 140.dp
                val centerOffset = visibleItemsCount / 2
                val viewportCenterY = with(density) { viewportHeight.toPx() / 2f }

                val listState = rememberLazyListState(
                    initialFirstVisibleItemIndex = if (selectedIndex >= 0) maxOf(0, selectedIndex - centerOffset) else 0
                )

                LaunchedEffect(Unit) {
                    if (selectedIndex >= 0) {
                        delay(50.milliseconds)
                        val layoutInfo = listState.layoutInfo
                        val targetItem = layoutInfo.visibleItemsInfo.firstOrNull { it.index == selectedIndex }
                        if (targetItem != null) {
                            val itemCenter = targetItem.offset + targetItem.size / 2f
                            val scrollOffset = itemCenter - viewportCenterY
                            if (abs(scrollOffset) > 1f) {
                                listState.animateScrollBy(scrollOffset)
                            }
                        }
                    }
                }

                LaunchedEffect(selectedItem) {
                    val newIndex = items.indexOf(selectedItem)
                    if (newIndex >= 0) {
                        val layoutInfo = listState.layoutInfo
                        val targetItem = layoutInfo.visibleItemsInfo.firstOrNull { it.index == newIndex }
                        if (targetItem != null) {
                            val itemCenter = targetItem.offset + targetItem.size / 2f
                            val scrollOffset = itemCenter - viewportCenterY
                            if (abs(scrollOffset) > 1f) {
                                listState.animateScrollBy(scrollOffset)
                            }
                        }
                    }
                }

                LaunchedEffect(listState.isScrollInProgress) {
                    if (!listState.isScrollInProgress) {
                        val layoutInfo = listState.layoutInfo
                        val viewportCenterYPx = layoutInfo.viewportSize.height / 2f

                        val centerItem = layoutInfo.visibleItemsInfo.minByOrNull { itemInfo ->
                            val itemCenter = itemInfo.offset + itemInfo.size / 2f
                            abs(itemCenter - viewportCenterYPx)
                        }

                        centerItem?.let {
                            val centerItemValue = items.getOrNull(it.index)
                            if (centerItemValue != null && centerItemValue != selectedItem) {
                                onItemSelected(centerItemValue)
                            }
                        }
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 50.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(items, key = { it }) { item ->
                        val isSelected = item == selectedItem
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(itemHeight)
                                .clickable { onItemSelected(item) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = formatItem(item),
                                style = if (isSelected) {
                                    MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                } else {
                                    MaterialTheme.typography.bodyLarge
                                },
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                }
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxWidth().height(itemHeight).align(Alignment.Center)
                ) {
                    // خط بالا
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(0.8f).align(Alignment.TopCenter),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )
                    // خط پایین
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(0.8f).align(Alignment.BottomCenter),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}