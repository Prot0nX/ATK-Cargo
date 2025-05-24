package com.atk.atk_cargo

//noinspection UsingMaterialAndMaterial3Libraries
import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.TabRowDefaults.Divider
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
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import com.atk.atk_cargo.api.UserPreferencesManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.util.InternalAPI
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    
    // StateFlow collectors
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
    var updateTime by remember { mutableStateOf(getCurrentTime()) }
    var isLoading by remember { mutableStateOf(true) }
    
    val groupedCargoList by remember(filteredCargoInfoList) {
        derivedStateOf {
            filteredCargoInfoList.groupBy { it.confirm == "تائید شده" }
                .toSortedMap(compareBy { it })
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

    // Auto-refresh timer
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
                updateTime = getCurrentTime()
                viewModel.updateInfoValues()
                Toast.makeText(context, "اطلاعات به‌روزرسانی شد", Toast.LENGTH_SHORT).show()
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
                                updateTime = getCurrentTime()
                                viewModel.updateInfoValues()
                                Toast.makeText(context, "اطلاعات به‌روزرسانی شد", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    updateTime = updateTime
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { isExpanded = !isExpanded }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // هدر کشتی در هر دو حالت باز و بسته نمایش داده می‌شود
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // نام کشتی با آیکون دریایی
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBoat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = initialInfo?.shipName ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // آیکون باز/بسته کردن
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "بستن" else "باز کردن",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // حالت باز - اطلاعات کامل
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    initialInfo?.let { info ->
                        // نمایش گرید اطلاعات در حالت باز
                        InfoGrid(initialInfo = info)
                    }
                }
            }

            // حالت بسته - چیپ‌های اطلاعات ضروری
            if (!isExpanded) {
                InfoChips(initialInfo = initialInfo)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // شماره کوتاژ
        ModernInfoChip(
            icon = Icons.Default.ConfirmationNumber,
            label = "کوتاژ",
            value = initialInfo?.loadingQuotaNumber?.toString() ?: "",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        // انبار
        ModernInfoChip(
            icon = Icons.Default.Warehouse,
            label = "انبار",
            value = initialInfo?.loadingWarehouse ?: "",
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )

        // شرکت باربری
        ModernInfoChip(
            icon = Icons.Default.Business,
            label = "شرکت",
            value = initialInfo?.shippingCompany ?: "",
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ModernInfoChip(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = color.copy(alpha = 0.7f)
                )

                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CargoInfoCard(cargoInfo: CargoInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(
                0.5.dp,
                when {
                    cargoInfo.confirm == "تائید شده" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    cargoInfo.status == "ورود" -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                },
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when {
                cargoInfo.confirm == "تائید شده" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                cargoInfo.status == "ورود" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ستون سمت راست - شماره حواله و تعداد نفرات
            Column(
                modifier = Modifier.weight(0.65f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // شماره حواله و وضعیت تأیید
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = cargoInfo.trackingNumber,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                    if (cargoInfo.confirm == "تائید شده") {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // تعداد نفرات
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "تعداد نفرات: ${cargoInfo.numberOfPeople}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // نمایش کسری و اضافه بار در یک خط
                if (cargoInfo.shortageWeight.isNotBlank() || cargoInfo.excessWeight.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // کسری
                        if (cargoInfo.shortageWeight.isNotBlank() && (cargoInfo.shortageWeight.toDoubleOrNull() ?: 0.0) > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${cargoInfo.shortageWeight} kg",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // اضافه بار
                        if (cargoInfo.excessWeight.isNotBlank() && (cargoInfo.excessWeight.toDoubleOrNull() ?: 0.0) > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${cargoInfo.excessWeight} kg",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // خط عمودی جداکننده
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            )

            // ستون سمت چپ - وضعیت حواله
            Column(
                modifier = Modifier.weight(0.35f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // نشانگر وضعیت
                StatusBadge(
                    status = cargoInfo.status,
                    isConfirmed = cargoInfo.confirm == "تائید شده"
                )

                // تاریخ و زمان مربوطه
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (cargoInfo.status) {
                        "ورود" -> "ورود: ${cargoInfo.entryTime}"
                        "خروج" -> "خروج: ${cargoInfo.exitTime}"
                        else -> ""
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SearchAndRefreshSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    updateTime: String
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("جستجوی حواله") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            trailingIcon = {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "پاک کردن")
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onRefresh,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "بارگذاری مجدد",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("بروزرسانی اطلاعات", color = MaterialTheme.colorScheme.onPrimary)
            }

            Text(
                text = "آخرین بروزرسانی: $updateTime",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GroupHeader(
    title: String,
    isExpanded: Boolean,
    isConfirmed: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                color = if (isConfirmed) Color(0xFF4CAF50) else Color(0xFFF44336)
            ),
            fontWeight = FontWeight.Bold
        )
        Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = if (isExpanded) "بستن" else "باز کردن"
        )
    }
}

@Composable
fun CargoListSection(
    groupedCargoList: Map<Boolean, List<CargoInfo>>,
    onCargoSelected: (CargoInfo) -> Unit
) {
    var isConfirmedExpanded by remember { mutableStateOf(false) }
    var isUnconfirmedExpanded by remember { mutableStateOf(true) }

    // وقتی یک گروه باز می‌شود، گروه دیگر بسته می‌شود
    // همیشه فقط یکی از آنها باز است
    fun toggleConfirmedSection() {
        if (!isConfirmedExpanded) {
            isConfirmedExpanded = true
            isUnconfirmedExpanded = false
        }
    }

    fun toggleUnconfirmedSection() {
        if (!isUnconfirmedExpanded) {
            isUnconfirmedExpanded = true
            isConfirmedExpanded = false
        }
    }

    // محاسبه آمار فقط برای گروه تأیید شده
    val confirmedStats = remember(groupedCargoList[true]) {
        groupedCargoList[true]?.let { confirmedCargos ->
            val totalWeight = confirmedCargos.sumOf { it.netWeight.toDoubleOrNull() ?: 0.0 }
            val avgWeight = if (confirmedCargos.isNotEmpty()) totalWeight / confirmedCargos.size else 0.0
            Triple(confirmedCargos.size, totalWeight, avgWeight)
        } ?: Triple(0, 0.0, 0.0)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // گروه تأیید نشده
        groupedCargoList[false]?.let { unconfirmedCargos ->
            item(key = "unconfirmed_header") {
                Column {
                    GroupHeader(
                        title = "تأیید نشده (${unconfirmedCargos.size})",
                        isExpanded = isUnconfirmedExpanded,
                        isConfirmed = false,
                        onToggle = { toggleUnconfirmedSection() }
                    )
                }
            }

            if (isUnconfirmedExpanded) {
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

            item(key = "unconfirmed_divider") {
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            }
        }

        // گروه تأیید شده
        groupedCargoList[true]?.let { confirmedCargos ->
            item(key = "confirmed_header") {
                Column {
                    GroupHeader(
                        title = "تأیید شده (${confirmedCargos.size})",
                        isExpanded = isConfirmedExpanded,
                        isConfirmed = true,
                        onToggle = { toggleConfirmedSection() }
                    )
                    if (isConfirmedExpanded && confirmedCargos.isNotEmpty()) {
                        GroupStats(
                            count = confirmedStats.first,
                            totalWeight = confirmedStats.second,
                            avgWeight = confirmedStats.third,
                            isConfirmed = true
                        )
                    }
                }
            }

            if (isConfirmedExpanded) {
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

            item(key = "confirmed_divider") {
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun GroupStats(
    count: Int,
    totalWeight: Double,
    avgWeight: Double,
    isConfirmed: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isConfirmed) 
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else 
                MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem(
                label = "تعداد",
                value = count.toString(),
                icon = Icons.Default.Inventory
            )
            StatItem(
                label = "مجموع وزن",
                value = "${String.format("%,d", totalWeight.toLong())} کیلوگرم",
                icon = Icons.Default.Scale
            )
            StatItem(
                label = "میانگین",
                value = "${String.format("%,d", avgWeight.toLong())} کیلوگرم",
                icon = Icons.AutoMirrored.Filled.TrendingUp
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatusBadge(status: String, isConfirmed: Boolean) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = when {
            isConfirmed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            status == "ورود" -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
            else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        },
        border = BorderStroke(
            1.dp,
            when {
                isConfirmed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                status == "ورود" -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = when (status) {
                    "ورود" -> Icons.Default.Inventory
                    "خروج" -> Icons.Default.CheckCircle
                    else -> Icons.Default.DirectionsBoat
                },
                contentDescription = null,
                tint = when {
                    isConfirmed -> MaterialTheme.colorScheme.primary
                    status == "ورود" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.secondary
                },
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = status,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = when {
                    isConfirmed -> MaterialTheme.colorScheme.primary
                    status == "ورود" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.secondary
                }
            )
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
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                DialogHeader(info.trackingNumber)
                Spacer(modifier = Modifier.height(16.dp))
                CargoStatusTimeline(info)
                Spacer(modifier = Modifier.height(16.dp))
                WeightInfoSection(info)
                Spacer(modifier = Modifier.height(16.dp))
                AdditionalInfoSection(info)
                Spacer(modifier = Modifier.height(24.dp))
                DialogActions(onDismiss, onConfirm, showConfirmButton)
            }
        }
    }
}

@Composable
private fun DialogHeader(trackingNumber: String) {
    Text(
        text = "جزئیات حواله $trackingNumber",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun CargoStatusTimeline(info: CargoInfo) {
    val steps = listOf("ورود", "بارگیری", "خروج")
    val currentStepIndex = steps.indexOf(info.status).coerceAtLeast(0)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, step ->
            TimelineStep(
                step = step,
                isCompleted = index <= currentStepIndex,
                isLast = index == steps.lastIndex
            )
        }
    }
}

@Composable
fun TimelineStep(step: String, isCompleted: Boolean, isLast: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    if (isCompleted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        if (!isLast) {
            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .height(16.dp)
                    .background(
                        if (isCompleted) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            )
        }
        Text(
            text = step,
            style = MaterialTheme.typography.bodySmall,
            color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WeightInfoSection(info: CargoInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "اطلاعات وزن",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                WeightInfoItem("وزن خالص", info.netWeight, MaterialTheme.colorScheme.primary)
                WeightInfoItem("کسری", info.shortageWeight, MaterialTheme.colorScheme.error)
                WeightInfoItem("اضافه", info.excessWeight, MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

@Composable
fun WeightInfoItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Text(
            text = "$value کیلوگرم",
            style = MaterialTheme.typography.titleSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AdditionalInfoSection(info: CargoInfo) {
    Column {
        InfoRow("نام کشتی", info.shipName)
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        InfoRow("انبار بارگیری", info.loadingWarehouse)
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        InfoRow("نوع کالا", info.cargoType)
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        InfoRow("شرکت باربری", info.shippingCompany)
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        InfoRow("شماره کوتاژ", info.loadingQuotaNumber)
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        InfoRow("تعداد نفرات", info.numberOfPeople)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DialogActions(onDismiss: () -> Unit, onConfirm: () -> Unit, showConfirmButton: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = onDismiss) {
            Text("بستن")
        }
        if (showConfirmButton) {
            Button(onClick = onConfirm) {
                Text("تایید حواله")
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

@OptIn(InternalAPI::class)
suspend fun confirmCargo(info: CargoInfo, username: String, userType: String): Result<String> {
    val client = HttpClient(CIO)
    return try {
        val url = "https://atk-nk.click/Cargo/Test/confirm_cargo.php"
        val requestBody = Json.encodeToString(mapOf(
            "trackingNumber" to info.trackingNumber,
            "loadingQuotaNumber" to info.loadingQuotaNumber,
            "username" to username,
            "userType" to userType
        ))

        val response: HttpResponse = client.post(url) {
            contentType(io.ktor.http.ContentType.Application.Json)
            body = requestBody
        }

        client.close()

        if (response.status == HttpStatusCode.OK) {
            val responseBody = response.bodyAsText()
            val jsonResponse = Json.decodeFromString<Map<String, String>>(responseBody)
            Result.success(jsonResponse["message"] ?: "عملیات با موفقیت انجام شد")
        } else {
            Result.failure(Exception("خطا در ارتباط با سرور: ${response.status}"))
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
                viewModel.updateCargoConfirmation(info.trackingNumber)
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

fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date())
}