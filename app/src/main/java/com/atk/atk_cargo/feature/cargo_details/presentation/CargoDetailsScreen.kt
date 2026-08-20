package com.atk.atk_cargo.feature.cargo_details.presentation

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.atk.atk_cargo.api.CargoViewModel
import com.atk.atk_cargo.api.CargoViewModelFactory
import com.atk.atk_cargo.api.ReportsRepository
import com.atk.atk_cargo.api.TokenStore
import com.atk.atk_cargo.domain.session.UserPreferencesStore
import com.atk.atk_cargo.api.validateServerSession
import com.atk.atk_cargo.data.model.MessageType
import com.atk.atk_cargo.domain.model.Cargo
import com.atk.atk_cargo.domain.model.CargoConfirmStatus
import com.atk.atk_cargo.domain.model.CargoStatus
import com.atk.atk_cargo.feature.cargo_details.presentation.components.CargoDetailsDialog
import com.atk.atk_cargo.feature.cargo_details.presentation.components.CargoListSection
import com.atk.atk_cargo.feature.cargo_details.presentation.components.InitialInfoSection
import com.atk.atk_cargo.feature.cargo_details.presentation.components.SearchAndRefreshSection
import com.atk.atk_cargo.feature.cargo_entry.presentation.SnackbarMessage
import com.atk.atk_cargo.feature.cargo_entry.presentation.StatusSnackbar
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.MessageDialog
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.QuotaEntryDialog
import com.atk.atk_cargo.feature.home.navigation.navigateToHome
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

private fun refreshData(
    viewModel: CargoViewModel,
    quotaNumber: String,
    shippingCompany: String,
    warehouse: String,
    cargoType: String,
    onComplete: () -> Unit
) {
    viewModel.loadCargoInfoList(
        quotaNumber = quotaNumber,
        shippingCompany = shippingCompany,
        warehouse = warehouse,
        cargoType = cargoType,
        onComplete = onComplete
    )
}

@Composable
fun CargoDetailsScreen(
    navController: NavController,
    quotaNumber: String = "",
    shippingCompany: String = "",
    warehouse: String = "",
    cargoType: String = "",
    repository: ReportsRepository? = null,
    passedViewModel: CargoViewModel? = null,
    onChangeSelectionClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val userPreferencesManager = koinInject<UserPreferencesStore>()
    val tokenStore = koinInject<TokenStore>()
    val effectiveRepository = remember(repository) {
        repository ?: ReportsRepository()
    }
    val viewModel: CargoViewModel = passedViewModel ?: viewModel(factory = CargoViewModelFactory(effectiveRepository, userPreferencesManager))
    val coroutineScope = rememberCoroutineScope()
    val username by userPreferencesManager.username.collectAsStateWithLifecycle(initialValue = "")
    val userType by userPreferencesManager.userType.collectAsStateWithLifecycle(initialValue = "")
    val cargoUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cargoInfoList = cargoUiState.cargoInfoList
    val filteredCargoInfoList = cargoUiState.filteredCargoInfoList
    val initialInfo = cargoUiState.initialInfo
    val resultMessage by viewModel.resultMessage.collectAsStateWithLifecycle()
    val showAnimatedMessage by viewModel.showAnimatedMessage.collectAsStateWithLifecycle()
    val messageType by viewModel.messageType.collectAsStateWithLifecycle()
    var selectedCargoInfo by remember { mutableStateOf<Cargo?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var snackbarMessage by remember { mutableStateOf<SnackbarMessage?>(null) }
    var isFabExpanded by remember { mutableStateOf(false) }
    var showQuotaEntryDialog by remember { mutableStateOf(false) }
    var isConfirmingCargo by remember { mutableStateOf(false) }

    fun showUpdateMessage(message: String, type: MessageType) {
        snackbarMessage = SnackbarMessage(message, type)
    }
    
    // باگ واقعی بود (نه بهینه‌سازی): remember بدون کلید یعنی این derivedStateOf
    // فقط در همان اولین composition ساخته می‌شود و closure آن برای همیشه به
    // همان مقدار اولیه‌ی filteredCargoInfoList (یک List معمولی، نه یک State)
    // گیر می‌ماند — derivedStateOf فقط زمانی خودش تغییرات را ردیابی می‌کند که
    // داخل block خودش یک Compose State بخواند (`.value`)، نه یک val معمولی
    // که از بیرون closure شده. نتیجه: بعد از اولین بار (معمولاً وقتی لیست هنوز
    // خالی است، قبل از رسیدن پاسخ شبکه)، groupedCargoList دیگر هرگز بروز
    // نمی‌شد — دقیقاً همان چیزی که باعث می‌شد تب‌های «تائید نشده/تائید شده»
    // بعد از بارگذاری واقعی لیست همچنان خالی بمانند. filteredCargoInfoList
    // اکنون به‌عنوان کلید remember داده می‌شود (همان الگویی که sortedUnconfirmed/
    // sortedConfirmed/confirmedStats در CargoListSection از قبل درست استفاده
    // می‌کردند)، پس با هر تغییر واقعی لیست دوباره محاسبه می‌شود.
    val groupedCargoList = remember(filteredCargoInfoList) {
        filteredCargoInfoList.groupBy { it.confirm == CargoConfirmStatus.CONFIRMED.wireValue }
            .toSortedMap(compareBy { it })
    }

    LaunchedEffect(Unit) {
        try {
            val result = validateServerSession(tokenStore)
            result.fold(
                onSuccess = {
                    // Session معتبر است، ادامه می‌دهد
                },
                onFailure = {
                    navController.navigateToHome()
                }
            )
        } catch (e: Exception) {
            Log.e("CargoDetailsScreen", "خطا در بررسی وضعیت ورود: ${e.message}")
            navController.navigateToHome()
        }
    }

    LaunchedEffect(Unit) {
        if (quotaNumber.isNotBlank()) {
            // پارامترهای ورودی همین‌جا decode نمی‌شوند: ReportsNavigation از
            // قبل decode شده تحویل می‌دهد و polling/refresh/تأیید هم مقدار
            // خام را می‌فرستند؛ decode دوباره فقط اینجا باعث ناسازگاری بین
            // بارگذاری اول و بروزرسانی‌های بعدی می‌شد (و روی '%' کرش می‌کرد).
            viewModel.loadCargoInfoList(
                quotaNumber = quotaNumber,
                shippingCompany = shippingCompany,
                warehouse = warehouse,
                cargoType = cargoType,
                onComplete = {
                    isLoading = false
                    viewModel.updateInfoValues()
                }
            )
        } else {
            isLoading = false
        }
    }

    LaunchedEffect(cargoInfoList) {
        viewModel.filterCargoInfoList(searchQuery)
        viewModel.updateInfoValues()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                delay(30000.milliseconds)
                if (quotaNumber.isNotBlank()) {
                    // برخلاف refresh دستی، این بروزرسانی خودکار هر ۳۰ ثانیه
                    // است و کاربر درخواستش نکرده؛ نمایش اسنک‌بار «موفقیت» در
                    // هر تیک باعث می‌شد کاربر یاد بگیرد اسنک‌بارها را نادیده
                    // بگیرد و پیام خطای واقعی هم همان‌جا گم شود.
                    refreshData(
                        viewModel = viewModel,
                        quotaNumber = quotaNumber,
                        shippingCompany = shippingCompany,
                        warehouse = warehouse,
                        cargoType = cargoType
                    ) {
                        viewModel.updateInfoValues()
                    }
                }
            }
        }
    }

    Scaffold { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                InitialInfoSection(
                    initialInfo = initialInfo
                )

                SearchAndRefreshSection(
                    searchQuery = searchQuery,
                    onSearchQueryChange = {
                        searchQuery = it
                        viewModel.filterCargoInfoList(it)
                    },
                    onRefresh = {
                        coroutineScope.launch {
                            isLoading = true
                            refreshData(
                                viewModel = viewModel,
                                quotaNumber = quotaNumber,
                                shippingCompany = shippingCompany,
                                warehouse = warehouse,
                                cargoType = cargoType
                            ) {
                                isLoading = false
                                viewModel.updateInfoValues()
                                showUpdateMessage("اطلاعات با موفقیت بروزرسانی شد", MessageType.SUCCESS)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                CargoListSection(
                    groupedCargoList = groupedCargoList,
                    onCargoSelected = { selectedCargoInfo = it }
                )
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            snackbarMessage?.let { message ->
                StatusSnackbar(
                    message = message,
                    isVisible = true,
                    onDismiss = { snackbarMessage = null }
                )
            }

            // منوی شناور پایین صفحه (۲ آیتم: ۱. تغییر کوتاژ، ۲. تغییر کشتی)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnimatedVisibility(
                    visible = isFabExpanded,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // آیتم ۱: تغییر کوتاژ
                        FloatingActionButtonItem(
                            text = "تغییر کوتاژ",
                            icon = Icons.Default.ConfirmationNumber,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            onClick = {
                                isFabExpanded = false
                                showQuotaEntryDialog = true
                            }
                        )

                        // آیتم ۲: تغییر کشتی
                        FloatingActionButtonItem(
                            text = "تغییر کشتی",
                            icon = Icons.Default.DirectionsBoat,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            onClick = {
                                isFabExpanded = false
                                onChangeSelectionClick?.invoke()
                            }
                        )
                    }
                }

                // دکمه اصلی شناور (FAB)
                FloatingActionButton(
                    onClick = { isFabExpanded = !isFabExpanded },
                    modifier = Modifier.size(56.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = if (isFabExpanded) Icons.Default.Close else Icons.Default.Tune,
                        contentDescription = "منوی تغییرات",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    if (showAnimatedMessage) {
        MessageDialog(
            message = resultMessage,
            type = messageType,
            visible = true,
            onDismiss = { viewModel.dismissMessage() }
        )
    }

    selectedCargoInfo?.let { info ->
        CargoDetailsDialog(
            info = info,
            onDismiss = { if (!isConfirmingCargo) selectedCargoInfo = null },
            onConfirm = {
                if (!isConfirmingCargo) {
                    isConfirmingCargo = true
                    viewModel.confirmCargo(
                        info = info,
                        username = username,
                        userType = userType,
                        quotaNumber = quotaNumber,
                        shippingCompany = shippingCompany,
                        warehouse = warehouse,
                        cargoType = cargoType,
                        onComplete = {
                            isConfirmingCargo = false
                            selectedCargoInfo = null
                        }
                    )
                }
            },
            showConfirmButton = info.status == CargoStatus.ENTERED.wireValue && info.confirm != CargoConfirmStatus.CONFIRMED.wireValue,
            isConfirming = isConfirmingCargo
        )
    }

    if (showQuotaEntryDialog) {
        QuotaEntryDialog(
            showDialog = true,
            onDismiss = { showQuotaEntryDialog = false },
            onConfirm = { selectedQuota ->
                // QuotaEntryDialog پیش از صدا زدن onConfirm خودش وجود کوتاژ،
                // تعلق آن به همین کشتی و فعال بودنش را بررسی کرده؛ تکرار آن
                // بررسی‌ها اینجا لازم نیست. switchQuota به‌جای ساختن دستی یک
                // InitialInfo ناقص (با صفرهای موقت)، اطلاعات واقعی و کامل
                // کوتاژ را از سرور می‌خواند.
                viewModel.switchQuota(selectedQuota)
                showQuotaEntryDialog = false
            },
            shipName = initialInfo?.shipName ?: "",
            currentQuota = initialInfo?.loadingQuotaNumber?.toString() ?: quotaNumber,
            viewModel = viewModel
        )
    }
}

@Composable
private fun FloatingActionButtonItem(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
