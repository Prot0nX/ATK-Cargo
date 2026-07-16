package com.atk.atk_cargo.feature.cargo_details.presentation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.atk.atk_cargo.data.model.CargoInfo
import com.atk.atk_cargo.data.model.InitialInfo
import com.atk.atk_cargo.data.model.MessageType
import com.atk.atk_cargo.api.ReportsRepository
import com.atk.atk_cargo.api.CargoViewModel
import com.atk.atk_cargo.api.CargoViewModelFactory
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.api.validateServerSession
import com.atk.atk_cargo.feature.cargo_entry.presentation.SnackbarMessage
import com.atk.atk_cargo.feature.cargo_entry.presentation.StatusSnackbar
import com.atk.atk_cargo.feature.cargo_registration.presentation.components.MessageDialog
import com.atk.atk_cargo.feature.cargo_details.presentation.components.CargoDetailsDialog
import com.atk.atk_cargo.feature.cargo_details.presentation.components.CargoListSection
import com.atk.atk_cargo.feature.cargo_details.presentation.components.InitialInfoSection
import com.atk.atk_cargo.feature.cargo_details.presentation.components.SearchAndRefreshSection
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLDecoder

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

@Composable
fun CargoDetailsScreen(
    navController: NavController,
    quotaNumber: String,
    shippingCompany: String,
    warehouse: String,
    cargoType: String,
    repository: ReportsRepository
) {
    val context = LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    val viewModel: CargoViewModel = viewModel(factory = CargoViewModelFactory(repository, userPreferencesManager))
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
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("CargoDetailsScreen", "خطا در بررسی وضعیت ورود: ${e.message}")
            navController.navigate("home") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    LaunchedEffect(Unit) {
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
    }

    LaunchedEffect(cargoInfoList) {
        viewModel.filterCargoInfoList(searchQuery)
        viewModel.updateInfoValues()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30000)
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
}
