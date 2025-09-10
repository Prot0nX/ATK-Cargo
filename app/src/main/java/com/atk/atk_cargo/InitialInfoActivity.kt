package com.atk.atk_cargo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.focus.onFocusChanged
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
                    shadowElevation = 8.dp,
                    tonalElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val stepIcon = when (currentStep) {
                                        0 -> Icons.Default.DirectionsBoat
                                        1 -> Icons.Default.Category
                                        else -> Icons.Default.CheckCircle
                                    }
                                    Icon(
                                        imageVector = stepIcon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = steps[currentStep],
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = when (currentStep) {
                                            0 -> "مشخصات پایه‌ای کشتی و انبار"
                                            1 -> "جزئیات محموله و شرکت باربری"
                                            else -> "بررسی نهایی اطلاعات و ثبت"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${currentStep + 1}/${steps.size}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Step indicators
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            steps.forEachIndexed { index, _ ->
                                val isActive = index <= currentStep
                                val animatedSize = animateDpAsState(
                                    targetValue = if (index == currentStep) 12.dp else 8.dp,
                                    label = "step_size_$index"
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(animatedSize.value)
                                        .background(
                                            if (isActive) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                            }
                        }
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
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 2.dp
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Header
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.primaryContainer,
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsBoat,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = "مشخصات کشتی و محل بارگیری",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "لطفاً مشخصات کشتی و انبار بارگیری را وارد کنید",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )

                                    Text(
                                        text = "اطلاعات کشتی",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )

                                    // Ship name field with enhanced design
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
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(
                                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DirectionsBoat,
                                                    contentDescription = null,
                                                    tint = if (isValidShipName(shipName))
                                                        MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        },
                                        trailingIcon = {
                                            if (shipName.isNotEmpty() && isValidShipName(shipName)) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
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
                                        shape = RoundedCornerShape(12.dp),
                                        keyboardOptions = KeyboardOptions(
                                            imeAction = ImeAction.Next
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = {
                                                trimShipName()
                                                focusManager.moveFocus(FocusDirection.Next)
                                            }
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onFocusChanged {
                                                if (!it.isFocused) {
                                                    trimShipName()
                                                }
                                            },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                        )
                                    )

                                    Text(
                                        text = "محل بارگیری",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                    )

                                    // Warehouse field with enhanced design
                                    OutlinedTextField(
                                        value = loadingWarehouse,
                                        onValueChange = { input ->
                                            // حذف کاراکتر newline از ورودی
                                            loadingWarehouse = input.replace("\n", "")
                                        },
                                        label = { Text("نام انبار") },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(
                                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Warehouse,
                                                    contentDescription = null,
                                                    tint = if (isValidWarehouseName(loadingWarehouse))
                                                        MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        },
                                        trailingIcon = {
                                            if (loadingWarehouse.isNotEmpty() && isValidWarehouseName(loadingWarehouse)) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        },
                                        supportingText = {
                                            if (!isValidWarehouseName(loadingWarehouse) && loadingWarehouse.isNotEmpty()) {
                                                Text(
                                                    "نام انبار باید بین 3 تا 50 کاراکتر باشد",
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        },
                                        isError = !isValidWarehouseName(loadingWarehouse) && loadingWarehouse.isNotEmpty(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        keyboardOptions = KeyboardOptions(
                                            imeAction = ImeAction.Next
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = {
                                                trimLoadingWarehouse()
                                                focusManager.moveFocus(FocusDirection.Next)
                                            }
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onFocusChanged {
                                                if (!it.isFocused) {
                                                    trimLoadingWarehouse()
                                                }
                                            },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Enhanced continue button
                                    Button(
                                        onClick = { currentStep++ },
                                        enabled = isValidShipName(shipName) && isValidWarehouseName(loadingWarehouse),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                        ),
                                        elevation = ButtonDefaults.buttonElevation(
                                            defaultElevation = 4.dp,
                                            pressedElevation = 8.dp,
                                            disabledElevation = 0.dp
                                        )
                                    ) {
                                        Text(
                                            "ادامه",
                                            style = MaterialTheme.typography.titleMedium
                                        )
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
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 2.dp
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Header
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.primaryContainer,
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Category,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = "اطلاعات محموله و باربری",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "لطفاً جزئیات محموله و شرکت باربری را وارد کنید",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )

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
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(
                                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                                            CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.Category,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            },
                                            trailingIcon = {
                                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                            },
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
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            Icons.Default.Category,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = "اطلاعات شرکت باربری",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                                    )

                                    OutlinedTextField(
                                        value = shippingCompany,
                                        onValueChange = { input ->
                                            shippingCompany = input.replace("\n", "")
                                        },
                                        label = { Text("شرکت باربری") },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(
                                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocalShipping,
                                                    contentDescription = null,
                                                    tint = if (isValidPersianText(shippingCompany))
                                                        MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        },
                                        trailingIcon = {
                                            if (shippingCompany.isNotEmpty() && isValidPersianText(shippingCompany)) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                        ),
                                        keyboardOptions = KeyboardOptions(
                                            imeAction = ImeAction.Next
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = {
                                                trimShippingCompany()
                                                focusManager.moveFocus(FocusDirection.Next)
                                            }
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onFocusChanged {
                                                if (!it.isFocused) {
                                                    trimShippingCompany()
                                                }
                                            }
                                    )

                                    Text(
                                        text = "اطلاعات محموله",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
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
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(
                                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Scale,
                                                    contentDescription = null,
                                                    tint = if (isValidWeight(cargoWeight))
                                                        MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        },
                                        trailingIcon = {
                                            if (cargoWeight.isNotEmpty() && isValidWeight(cargoWeight)) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                        ),
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
                                            val filteredInput = input.replace(Regex("[^0-9]"), "")
                                            loadingQuotaNumber = filteredInput
                                        },
                                        label = { Text("شماره کوتاژ") },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(
                                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Numbers,
                                                    contentDescription = null,
                                                    tint = if (isValidQuotaNumber(loadingQuotaNumber))
                                                        MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        },
                                        trailingIcon = {
                                            if (loadingQuotaNumber.isNotEmpty() && isValidQuotaNumber(loadingQuotaNumber)) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        },
                                        supportingText = {
                                            if (!isValidQuotaNumber(loadingQuotaNumber) && loadingQuotaNumber.isNotEmpty()) {
                                                Text(
                                                    "شماره کوتاژ حداقل باید 5 رقم باشد",
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        },
                                        isError = !isValidQuotaNumber(loadingQuotaNumber) && loadingQuotaNumber.isNotEmpty(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                        ),
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

                                    OutlinedTextField(
                                        value = cargoOwner,
                                        onValueChange = { input ->
                                            cargoOwner = input.replace("\n", "")
                                        },
                                        label = { Text("صاحب کالا") },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(
                                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = if (isValidPersianText(cargoOwner))
                                                        MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        },
                                        trailingIcon = {
                                            if (cargoOwner.isNotEmpty() && isValidPersianText(cargoOwner)) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        },
                                        supportingText = {
                                            if (!isValidPersianText(cargoOwner) && cargoOwner.isNotEmpty()) {
                                                Text(
                                                    "نام صاحب کالا باید شامل 3 تا 50 حرف فارسی باشد",
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        },
                                        isError = !isValidPersianText(cargoOwner) && cargoOwner.isNotEmpty(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                        ),
                                        keyboardOptions = KeyboardOptions(
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                trimCargoOwner()
                                                focusManager.clearFocus()
                                            }
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onFocusChanged {
                                                if (!it.isFocused) {
                                                    trimCargoOwner()
                                                }
                                            }
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { currentStep-- },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(56.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "قبلی",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Button(
                                            onClick = { currentStep++ },
                                            enabled = isValidPersianText(shippingCompany) &&
                                                    isValidWeight(cargoWeight) &&
                                                    isValidQuotaNumber(loadingQuotaNumber) &&
                                                    isValidPersianText(cargoOwner),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(56.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                            ),
                                            elevation = ButtonDefaults.buttonElevation(
                                                defaultElevation = 4.dp,
                                                pressedElevation = 8.dp,
                                                disabledElevation = 0.dp
                                            )
                                        ) {
                                            Text(
                                                "ادامه",
                                                style = MaterialTheme.typography.titleMedium
                                            )
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
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 2.dp
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Header
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.primaryContainer,
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = "بررسی و تأیید نهایی",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "لطفاً اطلاعات وارد شده را بررسی کرده و در صورت صحت، تایید نمایید",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )

                                    // Summary Card with enhanced design
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .background(
                                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                                CircleShape
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Info,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }

                                                    Column {
                                                        Text(
                                                            text = "خلاصه اطلاعات",
                                                            style = MaterialTheme.typography.labelLarge,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                        Text(
                                                            text = "بررسی کنید و در صورت نیاز ویرایش نمایید",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }

                                            EnhancedInfoGroup(
                                                title = "اطلاعات کشتی",
                                                icon = Icons.Default.DirectionsBoat,
                                                items = listOf(
                                                    Pair("نام کشتی", shipName),
                                                    Pair("نام انبار", loadingWarehouse)
                                                )
                                            )

                                            EnhancedInfoGroup(
                                                title = "مشخصات محموله",
                                                icon = Icons.Default.Category,
                                                items = listOf(
                                                    Pair("نوع کالا", cargoType),
                                                    Pair("تناژ پروانه", "${formatNumber(cargoWeight)} کیلوگرم")
                                                )
                                            )

                                            EnhancedInfoGroup(
                                                title = "اطلاعات شرکت",
                                                icon = Icons.Default.LocalShipping,
                                                items = listOf(
                                                    Pair("شرکت باربری", shippingCompany),
                                                    Pair("شماره کوتاژ", loadingQuotaNumber),
                                                    Pair("صاحب کالا", cargoOwner)
                                                )
                                            )
                                        }
                                    }

                                    // Action Buttons with enhanced design
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { currentStep-- },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(56.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "ویرایش",
                                                style = MaterialTheme.typography.titleMedium
                                            )
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
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(56.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                            ),
                                            elevation = ButtonDefaults.buttonElevation(
                                                defaultElevation = 4.dp,
                                                pressedElevation = 8.dp
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Save,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "ثبت نهایی",
                                                style = MaterialTheme.typography.titleMedium
                                            )
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
                                remainingWeight = cargoWeight.toFloatOrNull() ?: 0f,
                                totalNetWeight = 0f,
                                averageNetWeight = 0f,
                                remainingServices = 0,
                                cargoOwner = cargoOwner
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
                            remainingWeight = cargoWeight.toFloatOrNull() ?: 0f,
                            totalNetWeight = 0f,
                            averageNetWeight = 0f,
                            remainingServices = 0,
                            cargoOwner = cargoOwner
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
    cargoOwner: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
)  {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                .scale(
                    animateFloatAsState(
                        targetValue = if (isVisible) 1f else 0.8f,
                        animationSpec = tween(300),
                        label = ""
                    ).value
                )
                .alpha(
                    animateFloatAsState(
                        targetValue = if (isVisible) 1f else 0f,
                        animationSpec = tween(300),
                        label = ""
                    ).value
                ),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                "تایید نهایی اطلاعات",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "لطفاً اطلاعات زیر را با دقت بررسی کنید",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content Sections
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EnhancedInfoGroup(
                        title = "اطلاعات کشتی",
                        icon = Icons.Default.DirectionsBoat,
                        items = listOf(
                            Pair("نام کشتی", shipName),
                            Pair("نام انبار", loadingWarehouse)
                        )
                    )
                    EnhancedInfoGroup(
                        title = "مشخصات محموله",
                        icon = Icons.Default.Category,
                        items = listOf(
                            Pair("نوع کالا", cargoType),
                            Pair("تناژ پروانه", "${formatNumber(cargoWeight)} تن")
                        )
                    )
                    EnhancedInfoGroup(
                        title = "اطلاعات شرکت",
                        icon = Icons.Default.LocalShipping,
                        items = listOf(
                            Pair("شرکت باربری", shippingCompany),
                            Pair("شماره کوتاژ", loadingQuotaNumber),
                            Pair("صاحب کالا", cargoOwner)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "ویرایش",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "تایید",
                            style = MaterialTheme.typography.titleMedium
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
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                .scale(
                    animateFloatAsState(
                        targetValue = if (isVisible) 1f else 0.8f,
                        animationSpec = tween(300),
                        label = ""
                    ).value
                )
                .alpha(
                    animateFloatAsState(
                        targetValue = if (isVisible) 1f else 0f,
                        animationSpec = tween(300),
                        label = ""
                    ).value
                ),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning Icon with Background
                Box(
                    modifier = Modifier
                        .size(80.dp)
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
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    text = "شماره کوتاژ تکراری",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                Text(
                    text = "شماره کوتاژ وارد شده با مشخصات فعلی قبلاً ثبت شده است. چه کاری می‌خواهید انجام دهید؟",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onReviewEdit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "ویرایش اطلاعات",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    OutlinedButton(
                        onClick = onNavigateToRegister,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Input,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "انتقال به صفحه ثبت",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
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
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                .scale(
                    animateFloatAsState(
                        targetValue = if (isVisible) 1f else 0.8f,
                        animationSpec = tween(300),
                        label = ""
                    ).value
                )
                .alpha(
                    animateFloatAsState(
                        targetValue = if (isVisible) 1f else 0f,
                        animationSpec = tween(300),
                        label = ""
                    ).value
                ),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Info Icon with Background
                Box(
                    modifier = Modifier
                        .size(80.dp)
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
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    text = "ثبت اطلاعات جدید",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                Text(
                    text = "شماره کوتاژ قبلاً با مشخصات متفاوتی ثبت شده است. آیا مایل به ثبت اطلاعات جدید هستید؟",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Buttons with RTL support
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "تایید و ادامه",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "انصراف",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
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
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(vertical = 1.dp)
        )

        items.forEach { (label, value) ->
            EnhancedInfoRow(label, value)
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
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                .scale(
                    animateFloatAsState(
                        targetValue = if (isVisible) 1f else 0.8f,
                        animationSpec = tween(300),
                        label = ""
                    ).value
                )
                .alpha(
                    animateFloatAsState(
                        targetValue = if (isVisible) 1f else 0f,
                        animationSpec = tween(300),
                        label = ""
                    ).value
                ),
            shape = RoundedCornerShape(28.dp),
            color = containerColor,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            color = contentColor.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = contentColor
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (isError) "خطا" else "موفقیت",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = contentColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 28.dp)
                )

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = contentColor,
                        contentColor = containerColor
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        "تایید",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

fun isValidShipName(name: String): Boolean {
    return name.matches(Regex("^[a-zA-Z0-9 ]{3,50}$"))
}

fun isValidWarehouseName(text: String): Boolean {
    return text.length in 3..50
}

fun isValidPersianText(text: String): Boolean {
    return text.matches(Regex("^[\\u0600-\\u06FF\\s0-9]{3,50}$"))
}

fun isValidWeight(weight: String): Boolean {
    val weightValue = weight.toLongOrNull()
    return weightValue != null && weightValue in 1..9999999999
}

fun isValidQuotaNumber(number: String): Boolean {
    return number.all { it.isDigit() } && number.length >= 5
}

private fun formatNumber(number: String): String {
    return try {
        val value = number.toLong()
        "%,d".format(Locale.US, value)
    } catch (_: Exception) {
        number
    }
}