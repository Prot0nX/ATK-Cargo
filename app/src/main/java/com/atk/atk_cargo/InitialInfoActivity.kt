package com.atk.atk_cargo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Scale
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
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
import com.atk.atk_cargo.api.CheckExistenceRequest
import com.atk.atk_cargo.api.InitialInfo
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.ui.theme.ATKCargoTheme
import kotlinx.coroutines.launch
import java.util.Locale

class InitialInfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ATKCargoTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        InitialInfoScreen()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitialInfoScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // State variables
    var currentStep by remember { mutableIntStateOf(0) }
    var shipName by remember { mutableStateOf("") }
    var loadingWarehouse by remember { mutableStateOf("") }
    var cargoType by remember { mutableStateOf("ذرت") }
    var shippingCompany by remember { mutableStateOf("") }
    var cargoWeight by remember { mutableStateOf("") }
    var loadingQuotaNumber by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var isErrorDialog by remember { mutableStateOf(false) }
    var shouldNavigate by remember { mutableStateOf(false) }
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var showPartialMatchDialog by remember { mutableStateOf(false) }

    val cargoTypes = remember { listOf("ذرت", "سویا", "دانه روغنی", "گندم", "جو") }
    val steps = remember { listOf("مشخصات کشتی", "اطلاعات بار", "تایید نهایی") }

    LaunchedEffect(Unit) {
        focusManager.clearFocus()
    }

    // Main surface with background and padding
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                Surface(
                    shadowElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = steps[currentStep],
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${currentStep + 1} از ${steps.size}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        LinearProgressIndicator(
                            progress = { (currentStep + 1).toFloat() / steps.size },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
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
                // Step content with animations
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        slideInHorizontally { height -> height } + fadeIn() togetherWith
                                slideOutHorizontally { height -> -height } + fadeOut()
                    },
                    label = "StepContent"
                ) { step ->
                    when (step) {
                        0 -> {
                            // Ship Details Step
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    OutlinedTextField(
                                        value = shipName,
                                        onValueChange = { input ->
                                            val filteredInput = input.replace("\n", "")
                                            if (filteredInput.all { it.isLetterOrDigit() || it.isWhitespace() }) {
                                                shipName = filteredInput.uppercase(Locale.ROOT)
                                            }
                                        },
                                        label = { Text("نام کشتی (انگلیسی)") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsBoat,
                                                contentDescription = null,
                                                tint = if (isValidShipName(shipName))
                                                    MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        },
                                        supportingText = {
                                            if (!isValidShipName(shipName) && shipName.isNotEmpty()) {
                                                Text(
                                                    "نام کشتی باید شامل 3 تا 50 حرف انگلیسی باشد",
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        },
                                        isError = !isValidShipName(shipName) && shipName.isNotEmpty(),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            imeAction = ImeAction.Next
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = {
                                                focusManager.moveFocus(FocusDirection.Next)
                                            }
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = loadingWarehouse,
                                        onValueChange = { input ->
                                            // حذف کاراکتر newline از ورودی
                                            loadingWarehouse = input.replace("\n", "")
                                        },
                                        label = { Text("نام انبار") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Warehouse,
                                                contentDescription = null,
                                                tint = if (isValidPersianText(loadingWarehouse))
                                                    MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        },
                                        supportingText = {
                                            if (!isValidPersianText(loadingWarehouse) && loadingWarehouse.isNotEmpty()) {
                                                Text(
                                                    "نام انبار باید شامل 3 تا 50 حرف فارسی باشد",
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        },
                                        isError = !isValidPersianText(loadingWarehouse) && loadingWarehouse.isNotEmpty(),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            imeAction = ImeAction.Next
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = {
                                                focusManager.moveFocus(FocusDirection.Next)
                                            }
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Button(
                                        onClick = { currentStep++ },
                                        enabled = isValidShipName(shipName) && isValidPersianText(loadingWarehouse),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                    ) {
                                        Text("ادامه")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null
                                        )
                                    }
                                }
                            }
                        }
                        1 -> {
                            // Cargo Details Step
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    ExposedDropdownMenuBox(
                                        expanded = expanded,
                                        onExpandedChange = { expanded = !expanded }
                                    ) {
                                        OutlinedTextField(
                                            value = cargoType,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("نوع کالا") },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Category,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            },
                                            trailingIcon = {
                                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .menuAnchor()
                                        )
                                        ExposedDropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
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

                                    OutlinedTextField(
                                        value = shippingCompany,
                                        onValueChange = { input ->
                                            shippingCompany = input.replace("\n", "")
                                        },
                                        label = { Text("شرکت باربری") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.LocalShipping,
                                                contentDescription = null,
                                                tint = if (isValidPersianText(shippingCompany))
                                                    MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            imeAction = ImeAction.Next
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = {
                                                focusManager.moveFocus(FocusDirection.Next)
                                            }
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = cargoWeight,
                                        onValueChange = { input ->
                                            val filteredInput = input.replace("\n", "")
                                            if (filteredInput.all { it.isDigit() } && filteredInput.length <= 10) {
                                                cargoWeight = filteredInput
                                            }
                                        },
                                        label = { Text("تناژ پروانه (کیلوگرم)") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Scale,
                                                contentDescription = null,
                                                tint = if (isValidWeight(cargoWeight))
                                                    MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Next
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = {
                                                focusManager.moveFocus(FocusDirection.Next)
                                            }
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = loadingQuotaNumber,
                                        onValueChange = { input ->
                                            val filteredInput = input.replace("\n", "")
                                            if (filteredInput.all { it.isDigit() } && filteredInput.length <= 10) {
                                                loadingQuotaNumber = filteredInput
                                            }
                                        },
                                        label = { Text("شماره کوتاژ") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Numbers,
                                                contentDescription = null,
                                                tint = if (isValidQuotaNumber(loadingQuotaNumber))
                                                    MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                focusManager.clearFocus()
                                            }
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { currentStep-- },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("قبلی")
                                        }

                                        Button(
                                            onClick = { currentStep++ },
                                            enabled = isValidPersianText(shippingCompany) &&
                                                    isValidWeight(cargoWeight) &&
                                                    isValidQuotaNumber(loadingQuotaNumber),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("ادامه")
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            // Final Confirmation Step
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Summary Card
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text(
                                                "خلاصه اطلاعات",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            HorizontalDivider()

                                            SummaryItem(
                                                label = "نام کشتی",
                                                value = shipName,
                                                icon = Icons.Default.DirectionsBoat
                                            )
                                            SummaryItem(
                                                label = "نام انبار",
                                                value = loadingWarehouse,
                                                icon = Icons.Default.Warehouse
                                            )
                                            SummaryItem(
                                                label = "نوع کالا",
                                                value = cargoType,
                                                icon = Icons.Default.Category
                                            )
                                            SummaryItem(
                                                label = "شرکت باربری",
                                                value = shippingCompany,
                                                icon = Icons.Default.LocalShipping
                                            )
                                            SummaryItem(
                                                label = "تناژ پروانه",
                                                value = "${formatNumber(cargoWeight)} کیلوگرم",
                                                icon = Icons.Default.Scale
                                            )
                                            SummaryItem(
                                                label = "شماره کوتاژ",
                                                value = loadingQuotaNumber,
                                                icon = Icons.Default.Numbers
                                            )
                                        }
                                    }

                                    // Action Buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { currentStep-- },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("ویرایش")
                                        }

                                        Button(
                                            onClick = {
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
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Save, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("ثبت نهایی")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog States
    if (showDialog) {
        ModernAlertDialog(
            message = dialogMessage,
            isError = isErrorDialog,
            onDismiss = {
                showDialog = false
                if (!isErrorDialog && shouldNavigate) {
                    val intent = Intent(context, RegisterCargoActivity::class.java).apply {
                        putExtra(
                            "initialInfo",
                            InitialInfo(
                                shipName = shipName,
                                loadingWarehouse = loadingWarehouse,
                                cargoType = cargoType,
                                shippingCompany = shippingCompany,
                                cargoWeight = cargoWeight.toFloatOrNull() ?: 0f,
                                loadingQuotaNumber = loadingQuotaNumber.toIntOrNull() ?: 0,
                                remainingWeight = 0f,
                                totalNetWeight = 0f,
                                averageNetWeight = 0f,
                                remainingServices = 0
                            )
                        )
                    }
                    context.startActivity(intent)
                }
            }
        )
    }

    if (showConfirmationDialog) {
        ConfirmationDialog(
            shipName = shipName,
            loadingWarehouse = loadingWarehouse,
            cargoType = cargoType,
            shippingCompany = shippingCompany,
            cargoWeight = cargoWeight,
            loadingQuotaNumber = loadingQuotaNumber,
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
                            remainingWeight = 0f,
                            totalNetWeight = 0f,
                            averageNetWeight = 0f,
                            remainingServices = 0
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
                val intent = Intent(context, RegisterCargoActivity::class.java).apply {
                    putExtra(
                        "initialInfo",
                        InitialInfo(
                            shipName = shipName,
                            loadingWarehouse = loadingWarehouse,
                            cargoType = cargoType,
                            shippingCompany = shippingCompany,
                            cargoWeight = cargoWeight.toFloatOrNull() ?: 0f,
                            loadingQuotaNumber = loadingQuotaNumber.toIntOrNull() ?: 0,
                            remainingWeight = 0f,
                            totalNetWeight = 0f,
                            averageNetWeight = 0f,
                            remainingServices = 0
                        )
                    )
                }
                context.startActivity(intent)
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
    shippingCompany: String,
    cargoWeight: String,
    loadingQuotaNumber: String,
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
                .animateContentSize()
                .scale(animateFloatAsState(if (isVisible) 1f else 0.8f, label = "").value)
                .alpha(animateFloatAsState(if (isVisible) 1f else 0f, label = "").value),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Section
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "تایید نهایی اطلاعات",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    "لطفاً اطلاعات زیر را با دقت بررسی کنید",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Content Sections
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Ship Info Section
                    InfoSection(
                        title = "اطلاعات کشتی",
                        icon = Icons.Default.DirectionsBoat,
                        items = listOf(
                            "نام کشتی" to shipName,
                            "نام انبار" to loadingWarehouse
                        )
                    )

                    // Cargo Info Section
                    InfoSection(
                        title = "مشخصات محموله",
                        icon = Icons.Default.Category,
                        items = listOf(
                            "نوع کالا" to cargoType,
                            "تناژ پروانه" to "${formatNumber(cargoWeight)} تن",
                        )
                    )

                    // Company Info Section
                    InfoSection(
                        title = "اطلاعات شرکت",
                        icon = Icons.Default.LocalShipping,
                        items = listOf(
                            "شرکت باربری" to shippingCompany,
                            "شماره کوتاژ" to loadingQuotaNumber
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تایید و ثبت نهایی")
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("بازبینی و ویرایش")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoSection(
    title: String,
    icon: ImageVector,
    items: List<Pair<String, String>>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Info Items
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { (label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
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
                .animateContentSize()
                .scale(animateFloatAsState(if (isVisible) 1f else 0.8f, label = "").value)
                .alpha(animateFloatAsState(if (isVisible) 1f else 0f, label = "").value),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning Icon with Background
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "شماره کوتاژ تکراری",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = "شماره کوتاژ وارد شده با مشخصات فعلی قبلاً ثبت شده است. چه کاری می‌خواهید انجام دهید؟",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onReviewEdit,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ویرایش اطلاعات")
                    }

                    OutlinedButton(
                        onClick = onNavigateToRegister,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Input,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("انتقال به صفحه ثبت")
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
                .animateContentSize()
                .scale(animateFloatAsState(if (isVisible) 1f else 0.8f, label = "").value)
                .alpha(animateFloatAsState(if (isVisible) 1f else 0f, label = "").value),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Info Icon with Background
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "ثبت اطلاعات جدید",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = "شماره کوتاژ قبلاً با مشخصات متفاوتی ثبت شده است. آیا مایل به ثبت اطلاعات جدید هستید؟",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons with RTL support
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تایید و ادامه")
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("انصراف")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ModernAlertDialog(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit
) {
    val icon = if (isError) Icons.Default.Error else Icons.Default.CheckCircle
    val containerColor = if (isError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = if (isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .scale(animateFloatAsState(if (isVisible) 1f else 0.8f, label = "").value)
                .alpha(animateFloatAsState(if (isVisible) 1f else 0f, label = "").value),
            shape = RoundedCornerShape(28.dp),
            color = containerColor,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 16.dp),
                    tint = contentColor
                )

                Text(
                    text = if (isError) "خطا" else "موفقیت",
                    style = MaterialTheme.typography.headlineSmall,
                    color = contentColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("تایید")
                }
            }
        }
    }
}

fun isValidShipName(name: String): Boolean {
    return name.matches(Regex("^[a-zA-Z0-9 ]{3,50}$"))
}

fun isValidPersianText(text: String): Boolean {
    return text.matches(Regex("^[\\u0600-\\u06FF\\s0-9]{3,50}$"))
}

fun isValidWeight(weight: String): Boolean {
    val weightValue = weight.toLongOrNull()
    return weightValue != null && weightValue in 1..9999999999
}

fun isValidQuotaNumber(number: String): Boolean {
    return number.all { it.isDigit() } && number.length in 5..10
}

private fun formatNumber(number: String): String {
    return try {
        val value = number.toLong()
        "%,d".format(Locale.US, value)
    } catch (e: Exception) {
        number
    }
}