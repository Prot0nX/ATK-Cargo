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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.atk.atk_cargo.api.CargoViewModel
import com.atk.atk_cargo.api.CargoViewModelFactory
import com.atk.atk_cargo.api.ReportsRepository
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.api.validateServerSession
import com.atk.atk_cargo.data.model.CargoInfo
import com.atk.atk_cargo.data.model.InitialInfo
import com.atk.atk_cargo.data.model.MessageType
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
import java.net.URLDecoder
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

suspend fun confirmCargo(info: CargoInfo, username: String, userType: String): Result<String> {
    return try {
        val requestBody = mapOf(
            "id" to (info.id?.toString() ?: "0"),
            "username" to username,
            "userType" to userType
        )

        val response = RetrofitClient.apiService.confirmCargo(requestBody)

        if (response.isSuccessful) {
            val responseBody = response.body()
            val message = responseBody?.get("message")?.asString ?: "عملیات با موفقیت انجام شد"
            Result.success(message)
        } else {
            Result.failure(Exception("خطا در ارتباط با سرور: ${response.code()}"))
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}

suspend fun handleCargoConfirmation(
    viewModel: CargoViewModel,
    info: CargoInfo,
    username: String,
    userType: String,
    quotaNumber: String,
    shippingCompany: String,
    warehouse: String,
    cargoType: String
) {
    try {
        val result = confirmCargo(info, username, userType)
        result.fold(
            onSuccess = { message ->
                viewModel.updateCargoConfirmation(info.id)
                viewModel.loadCargoInfoList(
                    quotaNumber = quotaNumber,
                    shippingCompany = shippingCompany,
                    warehouse = warehouse,
                    cargoType = cargoType,
                    onComplete = {
                        viewModel.showMessage(message, MessageType.SUCCESS)
                    }
                )
            },
            onFailure = { error ->
                Log.e("CargoDetailsScreen", "Error confirming cargo: ${error.message}", error)
                viewModel.showMessage("خطا: ${error.message}", MessageType.ERROR)
            }
        )
    } catch (e: Exception) {
        Log.e("CargoDetailsScreen", "Exception in cargo confirmation process", e)
        viewModel.showMessage("خطای غیرمنتظره: ${e.message}", MessageType.ERROR)
    }
}
suspend fun handleQuotaChangeInDetails(
    quotaCode: String,
    currentInitialInfo: InitialInfo?,
    viewModel: CargoViewModel,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    if (currentInitialInfo == null) {
        onError("اطلاعات اولیه یافت نشد")
        return
    }

    try {
        val response = viewModel.checkQuotaExistenceCargo(quotaCode, currentInitialInfo.shipName)
        if (response.exists && response.matchingQuotas.isNotEmpty()) {
            val selectedQuota = response.matchingQuotas.first()
            if (selectedQuota.shipName == currentInitialInfo.shipName) {
                if (selectedQuota.isActive) {
                    val fullQuotaNumber = selectedQuota.quotaNumber
                    val fullQuotaInt = fullQuotaNumber.toIntOrNull() ?: 0
                    val newInitialInfo = InitialInfo(
                        shipName = selectedQuota.shipName,
                        loadingWarehouse = selectedQuota.warehouse,
                        cargoType = selectedQuota.cargoType,
                        shippingCompany = selectedQuota.shippingCompany,
                        cargoWeight = 0f,
                        loadingQuotaNumber = fullQuotaInt,
                        remainingWeight = 0f,
                        totalNetWeight = 0f,
                        averageNetWeight = 0f,
                        remainingServices = 0
                    )
                    viewModel.setInitialInfo(newInitialInfo)
                    viewModel.loadCargoInfoList(
                        quotaNumber = fullQuotaNumber,
                        shippingCompany = selectedQuota.shippingCompany,
                        warehouse = selectedQuota.warehouse,
                        cargoType = selectedQuota.cargoType,
                        onComplete = {
                            viewModel.updateInfoValues()
                            onSuccess(fullQuotaNumber)
                        }
                    )
                } else {
                    onError("کوتاژ $quotaCode در حال حاضر غیرفعال است")
                }
            } else {
                onError("خطا: کوتاژ $quotaCode متعلق به کشتی ${selectedQuota.shipName} است، نه ${currentInitialInfo.shipName}")
            }
        } else {
            onError("کوتاژ $quotaCode برای کشتی ${currentInitialInfo.shipName} یافت نشد")
        }
    } catch (e: Exception) {
        onError("خطا در بررسی کوتاژ: ${e.message}")
    }
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
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    val effectiveRepository = remember(repository) {
        repository ?: ReportsRepository(RetrofitClient.apiService)
    }
    val viewModel: CargoViewModel = passedViewModel ?: viewModel(factory = CargoViewModelFactory(effectiveRepository, userPreferencesManager))
    val coroutineScope = rememberCoroutineScope()
    val username by userPreferencesManager.username.collectAsState(initial = "")
    val userType by userPreferencesManager.userType.collectAsState(initial = "")
    val cargoInfoList by viewModel.cargoInfoList.collectAsState()
    val filteredCargoInfoList by viewModel.filteredCargoInfoList.collectAsState()
    val initialInfo by viewModel.initialInfo.collectAsState()
    val resultMessage by viewModel.resultMessage.collectAsState()
    val showAnimatedMessage by viewModel.showAnimatedMessage.collectAsState()
    val messageType by viewModel.messageType.collectAsState()
    var selectedCargoInfo by remember { mutableStateOf<CargoInfo?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var snackbarMessage by remember { mutableStateOf<SnackbarMessage?>(null) }
    var isFabExpanded by remember { mutableStateOf(false) }
    var showQuotaEntryDialog by remember { mutableStateOf(false) }

    fun showUpdateMessage(message: String, type: MessageType) {
        snackbarMessage = SnackbarMessage(message, type)
    }
    
    val groupedCargoList by remember(filteredCargoInfoList) {
        derivedStateOf {
            filteredCargoInfoList.groupBy { it.confirm == "تائید شده" }
                .toSortedMap(compareBy { it })
        }
    }

    LaunchedEffect(Unit) {
        try {
            val result = validateServerSession(userPreferencesManager)
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
            val decodedShippingCompany = URLDecoder.decode(shippingCompany, "UTF-8")
            val decodedWarehouse = URLDecoder.decode(warehouse, "UTF-8")
            val decodedcargoType = URLDecoder.decode(cargoType, "UTF-8")

            viewModel.loadCargoInfoList(
                quotaNumber = quotaNumber,
                shippingCompany = decodedShippingCompany,
                warehouse = decodedWarehouse,
                cargoType = decodedcargoType,
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

    LaunchedEffect(Unit) {
        while (true) {
            delay(30000.milliseconds)
            if (quotaNumber.isNotBlank()) {
                refreshData(
                    viewModel = viewModel,
                    quotaNumber = quotaNumber,
                    shippingCompany = shippingCompany,
                    warehouse = warehouse,
                    cargoType = cargoType
                ) {
                    viewModel.updateInfoValues()
                    showUpdateMessage("اطلاعات با موفقیت بروزرسانی شد", MessageType.SUCCESS)
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
            onDismiss = { selectedCargoInfo = null },
            onConfirm = {
                coroutineScope.launch {
                    handleCargoConfirmation(
                        viewModel = viewModel,
                        info = info,
                        username = username,
                        userType = userType,
                        quotaNumber = quotaNumber,
                        shippingCompany = shippingCompany,
                        warehouse = warehouse,
                        cargoType = cargoType
                    )
                    selectedCargoInfo = null
                }
            },
            showConfirmButton = info.status == "ورود" && info.confirm != "تائید شده"
        )
    }

    if (showQuotaEntryDialog) {
        QuotaEntryDialog(
            showDialog = true,
            onDismiss = { showQuotaEntryDialog = false },
            onConfirm = { quotaCode ->
                showQuotaEntryDialog = false
                coroutineScope.launch {
                    isLoading = true
                    handleQuotaChangeInDetails(
                        quotaCode = quotaCode,
                        currentInitialInfo = initialInfo,
                        viewModel = viewModel,
                        onSuccess = { fullQuota ->
                            isLoading = false
                            showUpdateMessage("اطلاعات کوتاژ $fullQuota با موفقیت بارگذاری شد", MessageType.SUCCESS)
                        },
                        onError = { errorMsg ->
                            isLoading = false
                            showUpdateMessage(errorMsg, MessageType.ERROR)
                        }
                    )
                }
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
