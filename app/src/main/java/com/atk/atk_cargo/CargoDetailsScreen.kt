package com.atk.atk_cargo

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.CircularProgressIndicator
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.ViewList
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
    val cargoCount by viewModel.cargoCount.collectAsState()
    val username by userPreferencesManager.username.collectAsState(initial = "")
    val userType by userPreferencesManager.userType.collectAsState(initial = "")
    val cargoInfoList by viewModel.cargoInfoList.collectAsState()
    val filteredCargoInfoList by viewModel.filteredCargoInfoList.collectAsState()
    val initialInfo by viewModel.initialInfo.collectAsState()
    val cargoWeight by viewModel.cargoWeight.collectAsState()
    val remainingWeight by viewModel.remainingWeight.collectAsState()
    val loadedWeight by viewModel.loadedWeight.collectAsState()
    val resultMessage by viewModel.resultMessage.collectAsState()
    val showAnimatedMessage by viewModel.showAnimatedMessage.collectAsState()
    val messageType by viewModel.messageType.collectAsState()
    var selectedCargoInfo by remember { mutableStateOf<CargoInfo?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var updateTime by remember { mutableStateOf(getCurrentTime()) }
    var loadingProgress by remember { mutableFloatStateOf(0f) }
    var showLoadingDialog by remember { mutableStateOf(true) }
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
            onProgress = { progress -> loadingProgress = progress },
            onComplete = { showLoadingDialog = false }
        )
    }

    LaunchedEffect(cargoInfoList) {
        viewModel.filterCargoInfoList(searchQuery)
    }

//    LaunchedEffect(Unit) {
//        while (true) {
//            delay(30000)
//            refreshData(
//                viewModel,
//                quotaNumber = quotaNumber,
//                shippingCompany = shippingCompany,
//                warehouse = warehouse,
//                cargoType = cargoType
//            ) {
//                updateTime = getCurrentTime()
//                Toast.makeText(context, "اطلاعات به‌روزرسانی شد", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            InitialInfoSection(
                initialInfo = initialInfo,
                cargoWeight = cargoWeight,
                cargoCount = cargoCount,
                remainingWeight = remainingWeight,
                loadedWeight = loadedWeight
            )

            SearchAndRefreshSection(
                searchQuery = searchQuery,
                onSearchQueryChange = {
                    searchQuery = it
                    viewModel.filterCargoInfoList(it)
                },
                onRefresh = {
                    coroutineScope.launch {
                        refreshData(
                            viewModel = viewModel,
                            quotaNumber = quotaNumber,
                            shippingCompany = shippingCompany,
                            warehouse = warehouse,
                            cargoType = cargoType
                        ) {
                            updateTime = getCurrentTime()
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

    if (showLoadingDialog) {
        LoadingDialog(
            progress = loadingProgress,
            onDismiss = { showLoadingDialog = false }
        )
    }
}

@Composable
fun InitialInfoSection(
    initialInfo: InitialInfo?,
    cargoWeight: String,
    cargoCount: Int,
    remainingWeight: String,
    loadedWeight: String
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
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBoat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = initialInfo?.shipName ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "بستن" else "باز کردن",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    initialInfo?.let { info ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                MiniInfoRow(Icons.Default.Warehouse, "انبار", info.loadingWarehouse)
                                MiniInfoRow(Icons.Default.Category, "نوع کالا", info.cargoType)
                                MiniInfoRow(Icons.Default.Scale, "وزن کل", cargoWeight)
                                MiniInfoRow(Icons.AutoMirrored.Filled.TrendingDown, "تناژ مانده", remainingWeight)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                MiniInfoRow(Icons.Default.Business, "شرکت باربری", info.shippingCompany)
                                MiniInfoRow(Icons.Default.ConfirmationNumber, "شماره کوتاژ", info.loadingQuotaNumber.toString())
                                MiniInfoRow(Icons.Default.Inventory, "تعداد حواله", cargoCount.toString())
                                MiniInfoRow(Icons.AutoMirrored.Filled.TrendingUp, "تناژ بارگیری شده", loadedWeight)
                            }
                        }
                    }
                }
            }

            if (!isExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MiniInfoChip(Icons.AutoMirrored.Filled.TrendingDown, "مانده", remainingWeight)
                    MiniInfoChip(Icons.AutoMirrored.Filled.ViewList, "کوتاژ", initialInfo?.loadingQuotaNumber.toString())
                    MiniInfoChip(Icons.AutoMirrored.Filled.TrendingUp, "بارگیری", loadedWeight)
                }
            }
        }
    }
}

@Composable
fun MiniInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun MiniInfoChip(icon: ImageVector, label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
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
    // مدیریت وضعیت باز یا بسته بودن هر گروه به صورت جداگانه
    var isConfirmedExpanded by remember { mutableStateOf(false) }
    var isUnconfirmedExpanded by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // گروه تأیید نشده
        groupedCargoList[false]?.let { unconfirmedCargos ->
            val groupTitle = "تأیید نشده (${unconfirmedCargos.size})"
            item(key = "unconfirmed_header") {
                GroupHeader(
                    title = groupTitle,
                    isExpanded = isUnconfirmedExpanded,
                    isConfirmed = false,
                    onToggle = { isUnconfirmedExpanded = !isUnconfirmedExpanded }
                )
            }

            if (isUnconfirmedExpanded) {
                items(
                    items = unconfirmedCargos,
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
            val groupTitle = "تأیید شده"
            item(key = "confirmed_header") {
                GroupHeader(
                    title = groupTitle,
                    isExpanded = isConfirmedExpanded,
                    isConfirmed = true,
                    onToggle = { isConfirmedExpanded = !isConfirmedExpanded }
                )
            }

            if (isConfirmedExpanded) {
                items(
                    items = confirmedCargos,
                    key = { it.trackingNumber }/**/
                ) { cargoInfo ->
                    CargoInfoCard(
                        cargoInfo = cargoInfo,
                        onClick = { onCargoSelected(cargoInfo) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item(key = "confirmed_spacer") {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item(key = "confirmed_divider") {
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
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
                cargoInfo.status == "ورود" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tracking Number and Confirmation Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
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

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (cargoInfo.status) {
                        "ورود" -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        "خروج" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    border = BorderStroke(
                        0.5.dp,
                        when (cargoInfo.status) {
                            "ورود" -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                            "خروج" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.outline
                        }
                    )
                ) {
                    Text(
                        text = cargoInfo.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = when (cargoInfo.status) {
                            "ورود" -> MaterialTheme.colorScheme.error
                            "خروج" -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Weight Status Section
            if (cargoInfo.shortageWeight.isNotBlank() || cargoInfo.excessWeight.isNotBlank() || cargoInfo.netWeight.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // People Count
                    PeopleCount(cargoInfo.numberOfPeople)

                    // Weight Badges
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        WeightStatusBadges(
                            shortageWeight = cargoInfo.shortageWeight,
                            excessWeight = cargoInfo.excessWeight
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PeopleCount(count: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = count,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun WeightStatusBadges(
    shortageWeight: String,
    excessWeight: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (shortageWeight.isNotBlank() && (shortageWeight.toDoubleOrNull() ?: 0.0) > 0) {
            WeightBadge(
                value = shortageWeight,
                label = "کسری",
                icon = Icons.AutoMirrored.Filled.TrendingDown,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (excessWeight.isNotBlank() && (excessWeight.toDoubleOrNull() ?: 0.0) > 0) {
            WeightBadge(
                value = excessWeight,
                label = "اضافه",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun WeightBadge(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "$label: $value کیلوگرم",
                style = MaterialTheme.typography.labelSmall,
                color = color
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

@Composable
private fun LoadingDialog(
    progress: Float,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loading_rotation"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "progress_animation"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(28.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 6.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(280.dp)
                    .padding(24.dp)
            ) {
                // Background Progress
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(200.dp),
                    progress = 1f,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 10.dp,
                    strokeCap = StrokeCap.Round
                )

                // Animated Progress
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(200.dp),
                    progress = animatedProgress,
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 10.dp,
                    strokeCap = StrokeCap.Round
                )

                // Rotating Loading Indicator
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(180.dp)
                        .rotate(rotation),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                    strokeWidth = 2.dp
                )

                // Center Content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Progress Percentage
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Loading Status Chip with MD3 styling
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "در حال بارگذاری",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            LoadingDots()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingDots() {
    val dotSize = 4.dp
    val delayUnit = 200

    @Composable
    fun Dot(delay: Int) {
        var visible by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            while (true) {
                delay(delay.toLong())
                visible = !visible
                delay(1000)
            }
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(
                animationSpec = tween(200)
            ),
            exit = fadeOut(
                animationSpec = tween(200)
            )
        ) {
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSecondaryContainer)
            )
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Dot(0)
        Dot(delayUnit)
        Dot(delayUnit * 2)
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
        onProgress = { /* You can handle progress updates here if needed */ },
        onComplete = onComplete
    )
}

@OptIn(InternalAPI::class)
suspend fun confirmCargo(info: CargoInfo, username: String, userType: String): Result<String> {
    val client = HttpClient(CIO)
    return try {
        val url = "https://atk-nk.site/confirm_cargo.php"
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
                    onProgress = { /* Handle progress if needed */ },
                    onComplete = {
                        viewModel.showMessage(message, MessageType.SUCCESS)
                    },
                    showLoadingDialog = false
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