package com.atk.atk_cargo

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.atk.atk_cargo.api.CargoInfo
import com.atk.atk_cargo.api.CargoViewModel
import com.atk.atk_cargo.api.CargoViewModelFactory
import com.atk.atk_cargo.api.InitialInfo
import com.atk.atk_cargo.api.MessageType
import com.atk.atk_cargo.api.ReportsRepository
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.api.validateServerSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLDecoder

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
        viewModel.updateInfoValues() // به‌روزرسانی مقادیر پس از تغییر لیست
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30000) // هر 30 ثانیه
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
            
            // نمایش نشانگر بارگذاری
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

@Composable
fun InitialInfoSection(
    initialInfo: InitialInfo?
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // هدر کشتی + دکمه expand
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // نام کشتی
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBoat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = initialInfo?.shipName ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // دکمه expand/collapse
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { isExpanded = !isExpanded },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "بستن" else "باز کردن",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Grid 3 ستونی (همیشه نمایش داده می‌شود)
            InfoChips(initialInfo = initialInfo)

            // حالت باز - اطلاعات کامل
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    initialInfo?.let { info ->
                        InfoGrid(initialInfo = info)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoGrid(initialInfo: InitialInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ردیف اول
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoGridItem(
                    icon = Icons.Default.Warehouse,
                    label = "انبار بارگیری",
                    value = initialInfo.loadingWarehouse,
                    modifier = Modifier.weight(1f)
                )

                InfoGridItem(
                    icon = Icons.Default.Business,
                    label = "شرکت باربری",
                    value = initialInfo.shippingCompany,
                    modifier = Modifier.weight(1f)
                )
            }

            // ردیف دوم
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoGridItem(
                    icon = Icons.Default.Category,
                    label = "نوع کالا",
                    value = initialInfo.cargoType,
                    modifier = Modifier.weight(1f)
                )

                InfoGridItem(
                    icon = Icons.Default.ConfirmationNumber,
                    label = "شماره کوتاژ",
                    value = initialInfo.loadingQuotaNumber.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun InfoGridItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun InfoChips(initialInfo: InitialInfo?) {
    // Grid 3 ستونی عمودی مشابه HTML
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // شرکت باربری
        InfoGridCard(
            icon = Icons.Default.Business,
            value = initialInfo?.shippingCompany ?: "",
            modifier = Modifier.weight(1f)
        )

        // انبار
        InfoGridCard(
            icon = Icons.Default.Warehouse,
            value = initialInfo?.loadingWarehouse ?: "",
            modifier = Modifier.weight(1f)
        )

        // شماره کوتاژ
        InfoGridCard(
            icon = Icons.Default.ConfirmationNumber,
            value = initialInfo?.loadingQuotaNumber?.toString() ?: "",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun InfoGridCard(
    icon: ImageVector,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CargoInfoCard(cargoInfo: CargoInfo, onClick: () -> Unit) {
    val isExited = cargoInfo.status == "خروج"
    val isConfirmed = cargoInfo.confirm == "تائید شده"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // سمت چپ: وضعیت + زمان
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.width(80.dp)
            ) {
                // Badge وضعیت
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isExited) 
                        Color(0xFF4CAF50).copy(alpha = 0.1f) 
                    else 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    border = BorderStroke(
                        1.dp,
                        if (isExited) Color(0xFF4CAF50).copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cargoInfo.status,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isExited) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // زمان
                Text(
                    text = if (isExited) cargoInfo.exitTime ?: "" else cargoInfo.entryTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // سمت راست: شماره حواله + تعداد نفرات
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // شماره حواله + آیکون تأیید
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = cargoInfo.trackingNumber,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isConfirmed) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "تأیید شده",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // تعداد نفرات
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تعداد نفرات: ${cargoInfo.numberOfPeople}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SearchAndRefreshSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onRefresh: () -> Unit
) {
    var isRefreshing by remember { mutableStateOf(false) }
    var rotationState by remember { mutableFloatStateOf(0f) }
    val rotation = animateFloatAsState(
        targetValue = rotationState,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "rotation"
    )
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        // فیلد جستجو و دکمه بروزرسانی در یک Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // فیلد جستجوی شماره حواله
            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { newValue ->
                        onSearchQueryChange(newValue)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(start = 40.dp, end = 16.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Left
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "جستجوی شماره حواله",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                // آیکون جستجو سمت راست
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // دکمه بروزرسانی
            Surface(
                modifier = Modifier
                    .height(48.dp)
                    .clickable(enabled = !isRefreshing) {
                        if (!isRefreshing) {
                            isRefreshing = true
                            rotationState += 360f
                            onRefresh()
                            coroutineScope.launch {
                                delay(1500)
                                isRefreshing = false
                            }
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "بروزرسانی",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "بروزرسانی",
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(rotation.value),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun TabsSection(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    confirmedCount: Int,
    unconfirmedCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        // تب‌های تأیید شده/نشده
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // تب تأیید نشده
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(0) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedTab == 0) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (selectedTab == 0) 1.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (selectedTab == 0) Color(0xFFF44336) else Color(0xFF9E9E9E),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تأیید نشده",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 0) Color(0xFFF44336) else Color(0xFF9E9E9E)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Badge تعداد
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedTab == 0) Color(0xFFF44336).copy(alpha = 0.1f) else Color(0xFFE0E0E0)
                        ) {
                            Text(
                                text = "$unconfirmedCount",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 0) Color(0xFFF44336) else Color(0xFF757575),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // تب تأیید شده
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(1) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedTab == 1) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (selectedTab == 1) 1.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (selectedTab == 1) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تأیید شده",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 1) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Badge تعداد
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedTab == 1) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFE0E0E0)
                        ) {
                            Text(
                                text = "$confirmedCount",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 1) Color(0xFF4CAF50) else Color(0xFF757575),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CargoListSection(
    groupedCargoList: Map<Boolean, List<CargoInfo>>,
    onCargoSelected: (CargoInfo) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // محاسبه تعداد هر گروه
    val unconfirmedCount = groupedCargoList[false]?.size ?: 0
    val confirmedCount = groupedCargoList[true]?.size ?: 0

    // محاسبه آمار فقط برای گروه تأیید شده
    val confirmedStats = remember(groupedCargoList[true]) {
        groupedCargoList[true]?.let { confirmedCargos ->
            val totalWeight = confirmedCargos.sumOf { it.netWeight.toDoubleOrNull() ?: 0.0 }
            val avgWeight = if (confirmedCargos.isNotEmpty()) totalWeight / confirmedCargos.size else 0.0
            Triple(confirmedCargos.size, totalWeight, avgWeight)
        } ?: Triple(0, 0.0, 0.0)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // تب‌های تأیید شده/نشده
        TabsSection(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            confirmedCount = confirmedCount,
            unconfirmedCount = unconfirmedCount
        )

        // کارت آمار برای تب تأیید شده
        if (selectedTab == 1 && confirmedCount > 0) {
            GroupStats(
                count = confirmedStats.first,
                totalWeight = confirmedStats.second,
                avgWeight = confirmedStats.third
            )
        }

        // لیست حواله‌ها بر اساس تب انتخاب شده
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (selectedTab == 0) {
                // تب تأیید نشده
                groupedCargoList[false]?.let { unconfirmedCargos ->
                    items(
                        items = unconfirmedCargos.sortedByDescending { it.entryTime },
                        key = { it.trackingNumber }
                    ) { cargoInfo ->
                        CargoInfoCard(
                            cargoInfo = cargoInfo,
                            onClick = { onCargoSelected(cargoInfo) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                // تب تأیید شده
                groupedCargoList[true]?.let { confirmedCargos ->
                    items(
                        items = confirmedCargos.sortedByDescending { "${it.exitDate} ${it.exitTime}" },
                        key = { it.trackingNumber }
                    ) { cargoInfo ->
                        CargoInfoCard(
                            cargoInfo = cargoInfo,
                            onClick = { onCargoSelected(cargoInfo) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun GroupStats(
    count: Int,
    totalWeight: Double,
    avgWeight: Double
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // تعداد
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                StatItem(
                    label = "تعداد",
                    value = count.toString()
                )
            }

            // جداکننده
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            )

            // مجموع وزن
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                StatItem(
                    label = "مجموع وزن",
                    value = String.format("%,d", totalWeight.toLong())
                )
            }

            // جداکننده
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            )

            // میانگین
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                StatItem(
                    label = "میانگین",
                    value = String.format("%,d", avgWeight.toLong())
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    unit: String = ""
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CargoDetailsDialog(
    info: CargoInfo,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    showConfirmButton: Boolean
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val windowInfo = LocalWindowInfo.current
        val density = LocalDensity.current
        val maxHeight = with(density) { (windowInfo.containerSize.height * 0.85f).toDp() }
        
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = maxHeight),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header
                ModernDialogHeader(
                    trackingNumber = info.trackingNumber,
                    isConfirmed = info.confirm == "تائید شده"
                )
                
                // Content
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModernCargoStatusSection(info)
                    
                    ModernWeightInfoSection(info)
                    
                    ModernCargoDetailsGrid(info)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Footer
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    ModernDialogActions(
                        onDismiss = onDismiss,
                        onConfirm = onConfirm,
                        showConfirmButton = showConfirmButton
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernDialogHeader(trackingNumber: String, isConfirmed: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // عنوان و badge در راست
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "حواله $trackingNumber",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isConfirmed)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else
                    MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                border = BorderStroke(
                    1.dp,
                    if (isConfirmed)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    else
                        MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isConfirmed) Icons.Default.CheckCircle else Icons.Default.Clear,
                        contentDescription = null,
                        tint = if (isConfirmed)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isConfirmed) "تأیید شده" else "در انتظار تأیید",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isConfirmed)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // آیکون در چپ
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ModernCargoStatusSection(info: CargoInfo) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "وضعیت حواله",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.DirectionsBoat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            ModernCargoStatusTimeline(info)
        }
    }
}

@Composable
fun ModernCargoStatusTimeline(info: CargoInfo) {
    val steps = listOf(
        "ورود" to Icons.Default.Inventory,
        "بارگیری" to Icons.Default.Scale,
        "خروج" to Icons.Default.CheckCircle
    )
    // اگر حواله تأیید شده باشد، وضعیت را "بارگیری" نشان بده
    val displayStatus = if (info.confirm == "تائید شده" && info.status == "ورود") "بارگیری" else info.status
    val currentStepIndex = steps.indexOfFirst { it.first == displayStatus }.coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        // خط افقی پشت دایره‌ها
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
                .height(2.dp)
                .align(Alignment.Center)
                .background(
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    RoundedCornerShape(1.dp)
                )
        )

        // دایره‌های مراحل
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, (step, icon) ->
                ModernTimelineStep(
                    step = step,
                    icon = icon,
                    isCompleted = index <= currentStepIndex
                )
            }
        }
    }
}

@Composable
fun ModernTimelineStep(
    step: String, 
    icon: ImageVector,
    isCompleted: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // دایره وضعیت
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = when {
                isCompleted -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            },
            shadowElevation = if (isCompleted) 8.dp else 0.dp
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = when {
                        isCompleted -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        // متن مرحله
        Text(
            text = step,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isCompleted -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            },
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ModernWeightInfoSection(info: CargoInfo) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "اطلاعات وزن",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.Scale,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val hasShortage = info.shortageWeight.isNotBlank() && info.shortageWeight != "0"
                val hasExcess = info.excessWeight.isNotBlank() && info.excessWeight != "0"

                ModernWeightInfoItem(
                    label = "وزن خالص",
                    value = info.netWeight,
                    icon = Icons.Default.Scale,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                if (hasShortage) {
                    ModernWeightInfoItem(
                        label = "کسری",
                        value = info.shortageWeight,
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (hasExcess) {
                    ModernWeightInfoItem(
                        label = "اضافه",
                        value = info.excessWeight,
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ModernWeightInfoItem(
    label: String, 
    value: String, 
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ModernCargoDetailsGrid(info: CargoInfo) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "rotation"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "جزئیات حواله",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "بستن" else "نمایش بیشتر",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(rotation)
                )
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ModernInfoCard(
                            icon = Icons.Default.DirectionsBoat,
                            label = "نام کشتی",
                            value = info.shipName,
                            modifier = Modifier.weight(1f)
                        )
                        ModernInfoCard(
                            icon = Icons.Default.Warehouse,
                            label = "انبار بارگیری",
                            value = info.loadingWarehouse,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ModernInfoCard(
                            icon = Icons.Default.Category,
                            label = "نوع کالا",
                            value = info.cargoType,
                            modifier = Modifier.weight(1f)
                        )
                        ModernInfoCard(
                            icon = Icons.Default.Business,
                            label = "شرکت باربری",
                            value = info.shippingCompany,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ModernInfoCard(
                            icon = Icons.Default.ConfirmationNumber,
                            label = "شماره کوتاژ",
                            value = info.loadingQuotaNumber,
                            modifier = Modifier.weight(1f)
                        )
                        ModernInfoCard(
                            icon = Icons.Default.Group,
                            label = "تعداد نفرات",
                            value = info.numberOfPeople,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernInfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ModernDialogActions(onDismiss: () -> Unit, onConfirm: () -> Unit, showConfirmButton: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // دکمه تایید
        if (showConfirmButton) {
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .weight(2f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "تأیید حواله",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // دکمه بستن
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "بستن",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

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