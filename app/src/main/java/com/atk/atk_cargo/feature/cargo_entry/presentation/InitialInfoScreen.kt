package com.atk.atk_cargo.feature.cargo_entry.presentation

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.atk.atk_cargo.feature.cargo_registration.navigation.navigateToCargoRegistration
import com.google.gson.Gson
import com.atk.atk_cargo.api.CheckExistenceRequest
import com.atk.atk_cargo.api.InitialInfo
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.feature.cargo_entry.domain.formatNumber
import com.atk.atk_cargo.feature.cargo_entry.domain.isValidPersianText
import com.atk.atk_cargo.feature.cargo_entry.domain.isValidQuotaNumber
import com.atk.atk_cargo.feature.cargo_entry.domain.isValidShipName
import com.atk.atk_cargo.feature.cargo_entry.domain.isValidWarehouseName
import com.atk.atk_cargo.feature.cargo_entry.domain.isValidWeight
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitialInfoScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var currentStep by remember { mutableIntStateOf(0) }
    var shipName by remember { mutableStateOf("") }
    var loadingWarehouse by remember { mutableStateOf("") }
    var cargoType by remember { mutableStateOf("ذرت") }
    var shippingCompany by remember { mutableStateOf("") }
    var cargoWeight by remember { mutableStateOf("") }
    var loadingQuotaNumber by remember { mutableStateOf("") }
    var cargoOwner by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var isErrorDialog by remember { mutableStateOf(false) }
    var shouldNavigate by remember { mutableStateOf(false) }
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var showPartialMatchDialog by remember { mutableStateOf(false) }

    fun trimShipName() {
        shipName = shipName.trim()
    }

    fun trimLoadingWarehouse() {
        loadingWarehouse = loadingWarehouse.trim()
    }

    fun trimShippingCompany() {
        shippingCompany = shippingCompany.trim()
    }

    fun trimCargoOwner() {
        cargoOwner = cargoOwner.trim()
    }

    val cargoTypes = remember { listOf("ذرت", "سویا", "دانه روغنی", "گندم", "جو") }

    LaunchedEffect(Unit) {
        focusManager.clearFocus()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (currentStep > 0) currentStep-- else (context as? ComponentActivity)?.finish()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "برگشت",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Text(
                            text = "ثبت اطلاعات اولیه بار",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width } + fadeOut()
                    },
                    label = "StepContent"
                ) { step ->
                    when (step) {
                        0 -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "مشخصات کشتی",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "لطفاً مشخصات کشتی و انبار بارگیری را وارد کنید.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    CustomInput(
                                        label = "نام کشتی (انگلیسی)",
                                        value = shipName,
                                        onValueChange = { input ->
                                            val filteredInput = input.replace("\n", "")
                                            if (filteredInput.all { it.isLetterOrDigit() || it.isWhitespace() }) {
                                                shipName = filteredInput.uppercase(Locale.ROOT)
                                            }
                                        },
                                        placeholder = "نام کشتی را وارد کنید",
                                        leadingIcon = Icons.Default.DirectionsBoat,
                                        isError = !isValidShipName(shipName) && shipName.isNotEmpty(),
                                        supportingText = if (!isValidShipName(shipName) && shipName.isNotEmpty()) "نام کشتی باید شامل 3 تا 50 حرف انگلیسی باشد" else null,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
                                    )

                                    CustomInput(
                                        label = "نام انبار",
                                        value = loadingWarehouse,
                                        onValueChange = { loadingWarehouse = it.replace("\n", "") },
                                        placeholder = "انبار بارگیری را وارد کنید",
                                        leadingIcon = Icons.Default.Warehouse,
                                        isError = !isValidWarehouseName(loadingWarehouse) && loadingWarehouse.isNotEmpty(),
                                        supportingText = if (!isValidWarehouseName(loadingWarehouse) && loadingWarehouse.isNotEmpty()) "نام انبار باید بین 3 تا 50 کاراکتر باشد" else null,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        trimShipName()
                                        trimLoadingWarehouse()
                                        currentStep++
                                    },
                                    enabled = isValidShipName(shipName) && isValidWarehouseName(loadingWarehouse),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                                ) {
                                    Text("مرحله بعد", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                        1 -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                SummaryCard(
                                    title = "جزئیات کشتی",
                                    subtitle = "$shipName • $loadingWarehouse",
                                    icon = Icons.Default.DirectionsBoat,
                                    onEdit = { currentStep = 0 }
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "اطلاعات بار",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "لطفاً مشخصات بار ورودی را وارد کنید.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "نوع کالا",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        ExposedDropdownMenuBox(
                                            expanded = expanded,
                                            onExpandedChange = { expanded = !expanded }
                                        ) {
                                            OutlinedTextField(
                                                value = cargoType,
                                                onValueChange = {},
                                                readOnly = true,
                                                placeholder = { Text("انتخاب نوع کالا", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                            )
                                            ExposedDropdownMenu(
                                                expanded = expanded,
                                                onDismissRequest = { expanded = false },
                                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                            ) {
                                                cargoTypes.forEach { type ->
                                                    DropdownMenuItem(
                                                        text = { Text(type) },
                                                        onClick = {
                                                            cargoType = type
                                                            expanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    CustomInput(
                                        label = "شرکت حمل و نقل",
                                        value = shippingCompany,
                                        onValueChange = { shippingCompany = it.replace("\n", "") },
                                        placeholder = "نام شرکت حمل و نقل",
                                        leadingIcon = Icons.Default.LocalShipping,
                                        isError = !isValidPersianText(shippingCompany) && shippingCompany.isNotEmpty(),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            CustomInput(
                                                label = "وزن کل",
                                                value = cargoWeight,
                                                onValueChange = { input ->
                                                    val filteredInput = input.replace("\n", "")
                                                    if (filteredInput.all { it.isDigit() } && filteredInput.length <= 10) {
                                                        cargoWeight = filteredInput
                                                    }
                                                },
                                                placeholder = "0",
                                                suffix = "kg",
                                                isError = !isValidWeight(cargoWeight) && cargoWeight.isNotEmpty(),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                                            )
                                        }
                                        Box(modifier = Modifier.weight(1f)) {
                                            CustomInput(
                                                label = "شماره کوتاژ",
                                                value = loadingQuotaNumber,
                                                onValueChange = { input ->
                                                    val filteredInput = input.replace(Regex("[^0-9]"), "")
                                                    loadingQuotaNumber = filteredInput
                                                },
                                                isLtr = true,
                                                placeholder = "00000",
                                                isError = !isValidQuotaNumber(loadingQuotaNumber) && loadingQuotaNumber.isNotEmpty(),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                                            )
                                        }
                                    }

                                    CustomInput(
                                        label = "نام صاحب کالا",
                                        value = cargoOwner,
                                        onValueChange = { cargoOwner = it.replace("\n", "") },
                                        placeholder = "نام صاحب کالا را وارد کنید",
                                        leadingIcon = Icons.Default.Person,
                                        isError = !isValidPersianText(cargoOwner) && cargoOwner.isNotEmpty(),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { currentStep-- },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("بازگشت", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                    }

                                    Button(
                                        onClick = {
                                            trimShippingCompany()
                                            trimCargoOwner()
                                            currentStep++
                                        },
                                        enabled = isValidPersianText(shippingCompany) &&
                                                isValidWeight(cargoWeight) &&
                                                isValidQuotaNumber(loadingQuotaNumber) &&
                                                isValidPersianText(cargoOwner),
                                        modifier = Modifier
                                            .weight(2f)
                                            .height(52.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = Color.White
                                        ),
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                                    ) {
                                        Text("مرحله بعد", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                        2 -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "بررسی اطلاعات",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "لطفاً اطلاعات زیر را با دقت بررسی و تأیید کنید.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                SummaryCard(
                                    title = "کشتی و انبار",
                                    subtitle = "$shipName • $loadingWarehouse",
                                    icon = Icons.Default.DirectionsBoat,
                                    onEdit = { currentStep = 0 }
                                )

                                SummaryCard(
                                    title = "محموله و باربری",
                                    subtitle = "$cargoType • $shippingCompany",
                                    icon = Icons.Default.Category,
                                    onEdit = { currentStep = 1 }
                                )

                                SummaryCard(
                                    title = "مالکیت و کوتاژ",
                                    subtitle = "$cargoOwner • $loadingQuotaNumber",
                                    icon = Icons.Default.Person,
                                    onEdit = { currentStep = 1 }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { currentStep-- },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Text("بازگشت", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                    }

                                    Button(
                                        onClick = {
                                            trimShipName()
                                            trimLoadingWarehouse()
                                            trimShippingCompany()
                                            trimCargoOwner()
                                            scope.launch {
                                                try {
                                                    val request = CheckExistenceRequest(
                                                        loadingQuotaNumber = loadingQuotaNumber.toInt(),
                                                        shipName = shipName,
                                                        loadingWarehouse = loadingWarehouse,
                                                        cargoType = cargoType,
                                                        shippingCompany = shippingCompany
                                                    )
                                                    val response = RetrofitClient.apiService.checkExistence(request)
                                                    when (response.body()?.status) {
                                                        "not_exists" -> showConfirmationDialog = true
                                                        "exists" -> showDuplicateDialog = true
                                                        "partial_match" -> showPartialMatchDialog = true
                                                        else -> {
                                                            dialogMessage = "خطا در ارتباط با سرور"
                                                            isErrorDialog = true
                                                            showDialog = true
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    dialogMessage = "خطا در ارتباط با سرور: ${e.localizedMessage}"
                                                    isErrorDialog = true
                                                    showDialog = true
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(2f)
                                            .height(52.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = Color.White
                                        ),
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                                    ) {
                                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("ثبت نهایی اطلاعات", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        ModernAlertDialog(
            message = dialogMessage,
            isError = isErrorDialog,
            onDismiss = {
                showDialog = false
                if (!isErrorDialog && shouldNavigate) {
                    val info = InitialInfo(
                        shipName = shipName,
                        loadingWarehouse = loadingWarehouse,
                        cargoType = cargoType,
                        shippingCompany = shippingCompany,
                        cargoWeight = cargoWeight.toFloatOrNull() ?: 0f,
                        loadingQuotaNumber = loadingQuotaNumber.toIntOrNull() ?: 0,
                        remainingWeight = cargoWeight.toFloatOrNull() ?: 0f,
                        totalNetWeight = 0f,
                        averageNetWeight = 0f,
                        remainingServices = 0,
                        cargoOwner = cargoOwner
                    )
                    val json = Gson().toJson(info)
                    navController.navigateToCargoRegistration(json)
                }
            }
        )
    }

    if (showConfirmationDialog) {
        ConfirmationDialog(
            shipName = shipName,
            loadingWarehouse = loadingWarehouse,
            cargoType = cargoType,
            cargoWeight = cargoWeight,
            loadingQuotaNumber = loadingQuotaNumber,
            cargoOwner = cargoOwner,
            onConfirm = {
                scope.launch {
                    try {
                        val initialInfo = InitialInfo(
                            shipName = shipName,
                            loadingWarehouse = loadingWarehouse,
                            cargoType = cargoType,
                            shippingCompany = shippingCompany,
                            cargoWeight = cargoWeight.toFloatOrNull() ?: 0f,
                            loadingQuotaNumber = loadingQuotaNumber.toIntOrNull() ?: 0,
                            remainingWeight = cargoWeight.toFloatOrNull() ?: 0f,
                            totalNetWeight = 0f,
                            averageNetWeight = 0f,
                            remainingServices = 0,
                            cargoOwner = cargoOwner
                        )

                        val response = RetrofitClient.apiService.saveInitialInfo(initialInfo)
                        if (response.isSuccessful) {
                            dialogMessage = "اطلاعات با موفقیت ثبت شد"
                            isErrorDialog = false
                            showDialog = true
                            shouldNavigate = true
                            showConfirmationDialog = false
                        } else {
                            dialogMessage = "خطا در ثبت اطلاعات"
                            isErrorDialog = true
                            showDialog = true
                        }
                    } catch (e: Exception) {
                        dialogMessage = "خطا در ارتباط با سرور: ${e.localizedMessage}"
                        isErrorDialog = true
                        showDialog = true
                    }
                }
            },
            onDismiss = { showConfirmationDialog = false }
        )
    }

    if (showDuplicateDialog) {
        DuplicateDialog(
            onDismiss = { showDuplicateDialog = false },
            onReviewEdit = { showDuplicateDialog = false },
            onNavigateToRegister = {
                showDuplicateDialog = false
                val info = InitialInfo(
                    shipName = shipName,
                    loadingWarehouse = loadingWarehouse,
                    cargoType = cargoType,
                    shippingCompany = shippingCompany,
                    cargoWeight = cargoWeight.toFloatOrNull() ?: 0f,
                    loadingQuotaNumber = loadingQuotaNumber.toIntOrNull() ?: 0,
                    remainingWeight = cargoWeight.toFloatOrNull() ?: 0f,
                    totalNetWeight = 0f,
                    averageNetWeight = 0f,
                    remainingServices = 0,
                    cargoOwner = cargoOwner
                )
                val json = Gson().toJson(info)
                navController.navigateToCargoRegistration(json)
            }
        )
    }

    if (showPartialMatchDialog) {
        PartialMatchDialog(
            onDismiss = { showPartialMatchDialog = false },
            onConfirm = {
                showPartialMatchDialog = false
                showConfirmationDialog = true
            }
        )
    }
}

@Composable
fun ConfirmationDialog(
    shipName: String,
    loadingWarehouse: String,
    cargoType: String,
    cargoWeight: String,
    loadingQuotaNumber: String,
    cargoOwner: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .scale(animateFloatAsState(if (isVisible) 1f else 0.9f, label = "").value)
                .alpha(animateFloatAsState(if (isVisible) 1f else 0f, label = "").value),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.FactCheck, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Column {
                        Text("تأیید نهایی", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        Text("آیا از صحت اطلاعات اطمینان دارید؟", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    EnhancedInfoGroup(
                        title = "اطلاعات کشتی و بار",
                        icon = Icons.Default.DirectionsBoat,
                        items = listOf(
                            "کشتی" to shipName,
                            "انبار" to loadingWarehouse,
                            "نوع کالا" to cargoType
                        )
                    )
                    EnhancedInfoGroup(
                        title = "جزییات محموله",
                        icon = Icons.Default.Inventory,
                        items = listOf(
                            "وزن کل" to "${formatNumber(cargoWeight)} کیلوگرم",
                            "شماره کوتاژ" to loadingQuotaNumber,
                            "صاحب کالا" to cargoOwner
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("انصراف", style = MaterialTheme.typography.titleSmall)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تأیید و ثبت", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@Composable
fun DuplicateDialog(
    onDismiss: () -> Unit,
    onReviewEdit: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .scale(animateFloatAsState(if (isVisible) 1f else 0.9f, label = "").value)
                .alpha(animateFloatAsState(if (isVisible) 1f else 0f, label = "").value),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("شماره کوتاژ تکراری", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text(
                        "این شماره کوتاژ قبلاً در سیستم ثبت شده است. چه اقدامی می‌خواهید انجام دهید؟",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onReviewEdit,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ویرایش اطلاعات", style = MaterialTheme.typography.titleSmall)
                    }
                    OutlinedButton(
                        onClick = onNavigateToRegister,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("مشاهده در لیست", style = MaterialTheme.typography.titleSmall)
                    }
                    TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text("بستن", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun PartialMatchDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .scale(animateFloatAsState(if (isVisible) 1f else 0.9f, label = "").value)
                .alpha(animateFloatAsState(if (isVisible) 1f else 0f, label = "").value),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ثبت جدید", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text(
                        "این شماره کوتاژ قبلاً با مشخصات متفاوتی ثبت شده است. آیا مایل به ثبت اطلاعات جدید هستید؟",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("انصراف")
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تأیید و ادامه")
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedInfoGroup(
    title: String,
    icon: ImageVector,
    items: List<Pair<String, String>>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { (label, value) ->
                EnhancedInfoRow(label, value)
            }
        }
    }
}

@Composable
private fun EnhancedInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ModernAlertDialog(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .scale(animateFloatAsState(if (isVisible) 1f else 0.9f, label = "").value)
                .alpha(animateFloatAsState(if (isVisible) 1f else 0f, label = "").value),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.size(64.dp).background(
                        if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(message, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp)) {
                    Text("متوجه شدم")
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(subtitle, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun CustomInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    suffix: String? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isLtr: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        CompositionLocalProvider(LocalLayoutDirection provides if (isLtr) LayoutDirection.Ltr else LayoutDirection.Rtl) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) } },
                suffix = suffix?.let { { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
                isError = isError,
                supportingText = supportingText?.let { { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) } },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
