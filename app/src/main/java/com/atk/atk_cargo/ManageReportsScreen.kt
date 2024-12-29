package com.atk.atk_cargo

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.atk.atk_cargo.api.AppColors
import com.atk.atk_cargo.api.CalculationResult
import com.atk.atk_cargo.api.CargoInfo
import com.atk.atk_cargo.api.CarrierPerformanceAnalysis
import com.atk.atk_cargo.api.ColorSelector
import com.atk.atk_cargo.api.ComprehensiveAnalytics
import com.atk.atk_cargo.api.FabItem
import com.atk.atk_cargo.api.FilteredSummary
import com.atk.atk_cargo.api.Quota
import com.atk.atk_cargo.api.QuotaAnalytics
import com.atk.atk_cargo.api.QuotaCompletionData
import com.atk.atk_cargo.api.QuotaDetails
import com.atk.atk_cargo.api.QuotaEditData
import com.atk.atk_cargo.api.QuotaPercentageData
import com.atk.atk_cargo.api.QuotaPredictionData
import com.atk.atk_cargo.api.RealTimeLoadingData
import com.atk.atk_cargo.api.ReportsViewModel
import com.atk.atk_cargo.api.ShiftInfo
import com.atk.atk_cargo.api.ShiftPerformanceData
import com.atk.atk_cargo.api.Ship
import com.atk.atk_cargo.api.ShipAnalytics
import com.atk.atk_cargo.api.ShipSection
import com.atk.atk_cargo.api.ShippingCompanyAnalytics
import com.atk.atk_cargo.api.TabInfo
import com.atk.atk_cargo.api.VoucherAnalytics
import com.atk.atk_cargo.api.VoucherDetail
import com.atk.atk_cargo.api.Warehouse
import com.atk.atk_cargo.api.WarehouseAnalytics
import com.atk.atk_cargo.api.WarehouseEfficiencyData
import com.atk.atk_cargo.api.WarehousePeakData
import com.atk.atk_cargo.api.WarehouseSpeedData
import com.atk.atk_cargo.api.WarehouseTrafficData
import com.atk.atk_cargo.api.WarningStatus
import com.atk.atk_cargo.api.adjustColorForTheme
import com.atk.atk_cargo.api.cardColors
import com.atk.atk_cargo.api.toTon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun ATKCargoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        darkColorScheme(
            primary = AppColors.DarkPrimary,
            onPrimary = AppColors.DarkOnPrimary,
            primaryContainer = AppColors.DarkPrimaryContainer,
            onPrimaryContainer = AppColors.DarkOnPrimaryContainer,
            secondary = AppColors.DarkSecondary,
            onSecondary = AppColors.DarkOnSecondary,
            secondaryContainer = AppColors.DarkSecondaryContainer,
            onSecondaryContainer = AppColors.DarkOnSecondaryContainer,
            background = AppColors.DarkBackground,
            onBackground = AppColors.DarkOnBackground,
            surface = AppColors.DarkSurface,
            onSurface = AppColors.DarkOnSurface,
            error = AppColors.DarkError,
            onError = AppColors.DarkOnError
        )
    } else {
        lightColorScheme(
            primary = AppColors.LightPrimary,
            onPrimary = AppColors.LightOnPrimary,
            primaryContainer = AppColors.LightPrimaryContainer,
            onPrimaryContainer = AppColors.LightOnPrimaryContainer,
            secondary = AppColors.LightSecondary,
            onSecondary = AppColors.LightOnSecondary,
            secondaryContainer = AppColors.LightSecondaryContainer,
            onSecondaryContainer = AppColors.LightOnSecondaryContainer,
            background = AppColors.LightBackground,
            onBackground = AppColors.LightOnBackground,
            surface = AppColors.LightSurface,
            onSurface = AppColors.LightOnSurface,
            error = AppColors.LightError,
            onError = AppColors.LightOnError
        )
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}

@Composable
fun ManageReportsScreen(viewModel: ReportsViewModel) {
    val navController = rememberNavController()
    var showQuotasDialog by remember { mutableStateOf(false) }
    var selectedShipForQuotas by remember { mutableStateOf<String?>(null) }
    val messageSendingStatus by viewModel.messageSendingStatus.collectAsState()
    var showSearchResultDialog by remember { mutableStateOf(false) }
    var showRealTimeDialog by remember { mutableStateOf(false) }
    var showMessageDialog by remember { mutableStateOf(false) }
    var showAdvancedSearchDialog by remember { mutableStateOf(false) }
    var showAnalyticsDialog by remember { mutableStateOf(false) }
    val realTimeLoadingData by viewModel.realTimeLoadingData.collectAsState()
    val shiftInfo by viewModel.shiftInfo.collectAsState()
    var searchResult by remember { mutableStateOf<CargoInfo?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val isDarkTheme = isSystemInDarkTheme()
    val defaultColor = MaterialTheme.colorScheme.primary

    ATKCargoTheme {
        Scaffold { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "shipsList",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("shipsList") {
                    ShipsList(
                        viewModel = viewModel,
                        onShipSelected = { shipName ->
                            navController.navigate("shipDetails/$shipName")
                        }
                    )
                }
                composable("shipDetails/{shipName}") { backStackEntry ->
                    val shipName = backStackEntry.arguments?.getString("shipName") ?: return@composable
                    ShipDetails(
                        initialShipName = shipName,
                        viewModel = viewModel,
                        onWarehouseSelected = { warehouseName ->
                            navController.navigate("warehouseDetails/$shipName/$warehouseName")
                        }
                    )
                }
                composable("warehouseDetails/{shipName}/{warehouseName}") { backStackEntry ->
                    val shipName = backStackEntry.arguments?.getString("shipName") ?: return@composable
                    val warehouseName = backStackEntry.arguments?.getString("warehouseName") ?: return@composable
                    WarehouseDetails(
                        shipName = shipName,
                        warehouseName = warehouseName,
                        viewModel = viewModel
                    )
                }
                composable("quotaDetails/{quotaNumber}") { backStackEntry ->
                    val quotaNumber = backStackEntry.arguments?.getString("quotaNumber") ?: return@composable
                    QuotaDetails(
                        quotaNumber = quotaNumber,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    if (showQuotasDialog && selectedShipForQuotas != null) {
        QuotasDialog(
            shipName = selectedShipForQuotas!!,
            quotas = viewModel.selectedShipQuotas.collectAsState().value,
            onDismiss = {
                showQuotasDialog = false
                selectedShipForQuotas = null
            },
            onEdit = { oldQuotaNumber, newQuotaData ->
                viewModel.editQuota(oldQuotaNumber, newQuotaData)
            },
            onToggleStatus = { quotaNumber ->
                viewModel.toggleQuotaStatus(quotaNumber)
            },
            onDelete = { quotaNumber ->
                viewModel.deleteQuota(quotaNumber)
            },
            viewModel = viewModel
        )
    }

    LaunchedEffect(searchResult) {
        if (searchResult != null) {
            showSearchResultDialog = true
        }
    }

    ComprehensiveAnalyticsDialog(
        isVisible = showAnalyticsDialog,
        onDismiss = { showAnalyticsDialog = false },
        viewModel = viewModel
    )

    FloatingActionButton(
        onRealTimeLoadingClick = {
            showRealTimeDialog = true
        },
        onSendMessageClick = {
            showMessageDialog = true
        },
        onAdvancedSearchClick = {
            showAdvancedSearchDialog = true
        },
        onAnalyticsClick = {
            showAnalyticsDialog = true
        }
    )

    SendMessageDialog(
        isOpen = showMessageDialog,
        onDismiss = { showMessageDialog = false },
        onSendMessage = { title, body, recipients ->
            val senderId = 1
            val senderType = "admin"
            viewModel.sendMessage(title, body, recipients, senderId, senderType)
        },
        messageSendingStatus = messageSendingStatus
    )

    RealTimeLoadingBottomSheet(
        isOpen = showRealTimeDialog,
        onDismiss = { showRealTimeDialog = false },
        loadingData = realTimeLoadingData,
        shiftInfo = shiftInfo ?: ShiftInfo("", "", ""),
        onRefresh = { viewModel.loadRealTimeData(isDarkTheme, defaultColor) },
        viewModel = viewModel
    )

    AdvancedSearchDialog(
        isOpen = showAdvancedSearchDialog,
        onDismiss = { showAdvancedSearchDialog = false },
        onSearch = { receiptNumber ->
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
        }
    )

    searchResult?.let { cargo ->
        SearchResultDialog(
            cargoInfo = cargo,
            onDismiss = { searchResult = null }
        )
    }

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
}

@Composable
fun ShipsList(viewModel: ReportsViewModel, onShipSelected: (String) -> Unit) {
    val shipsData by viewModel.ships.collectAsState()
    var searchTerm by remember { mutableStateOf("") }
    var expandedSection by rememberSaveable { mutableStateOf(ShipSection.ACTIVE) }
    val realTimeLoadingData by viewModel.realTimeLoadingData.collectAsState()
    var showRealTimeDialog by remember { mutableStateOf(false) }
    val shiftInfo by viewModel.shiftInfo.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()
    val defaultColor = MaterialTheme.colorScheme.primary
    val filteredActiveShips by remember(shipsData.activeShips, searchTerm) {
        derivedStateOf {
            shipsData.activeShips.filter {
                it.name.contains(searchTerm, ignoreCase = true)
            }
        }
    }
    val filteredInactiveShips by remember(shipsData.inactiveShips, searchTerm) {
        derivedStateOf {
            shipsData.inactiveShips.filter {
                it.name.contains(searchTerm, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadShips()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            SearchTextField(
                value = searchTerm,
                onValueChange = { searchTerm = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top
                ) {
                    ShipSection(
                        title = "کشتی‌های فعال",
                        ships = filteredActiveShips,
                        isExpanded = expandedSection == ShipSection.ACTIVE,
                        onExpandChange = {
                            expandedSection = if (expandedSection == ShipSection.ACTIVE)
                                ShipSection.INACTIVE else ShipSection.ACTIVE
                        },
                        onShipSelected = { shipName ->
                            viewModel.setCurrentShipName(shipName)
                            onShipSelected(shipName)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(if (expandedSection == ShipSection.ACTIVE) 0.9f else 0.1f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ShipSection(
                        title = "کشتی‌های غیرفعال",
                        ships = filteredInactiveShips,
                        isExpanded = expandedSection == ShipSection.INACTIVE,
                        onExpandChange = {
                            expandedSection = if (expandedSection == ShipSection.INACTIVE)
                                ShipSection.ACTIVE else ShipSection.INACTIVE
                        },
                        onShipSelected = { shipName ->
                            viewModel.setCurrentShipName(shipName)
                            onShipSelected(shipName)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(if (expandedSection == ShipSection.INACTIVE) 0.9f else 0.1f)
                    )
                }
            }
        }

        FloatingActionButton(
            onRealTimeLoadingClick = {
                showRealTimeDialog = true
            },
            onSendMessageClick = {
            },
            onAdvancedSearchClick = {
            },
            onAnalyticsClick = {
            }
        )
    }

    RealTimeLoadingBottomSheet(
        isOpen = showRealTimeDialog,
        onDismiss = { showRealTimeDialog = false },
        loadingData = realTimeLoadingData,
        shiftInfo = shiftInfo ?: ShiftInfo("", "", ""),
        onRefresh = { viewModel.loadRealTimeData(isDarkTheme, defaultColor) },
        viewModel = viewModel
    )
}

@SuppressLint("RememberReturnType")
@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val animatedColor by animateColorAsState(
        targetValue = if (isFocused)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.outline,
        label = "color"
    )
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1f else 0.92f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val filteredValue = newValue.filter { char ->
                char.isLetterOrDigit() && char.code < 128
            }.uppercase()
            onValueChange(filteredValue)
        },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused }
            .scale(scale)
            .heightIn(min = 56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        leadingIcon = {
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(animatedColor.copy(alpha = if (isFocused) 0.1f else 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "جستجو",
                    tint = animatedColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = value.isNotEmpty(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "پاک کردن",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        placeholder = {
            Text(
                text = "نام کشتی را وارد کنید...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        supportingText = if (value.isNotEmpty()) {
            {
                Text(
                    text = "نتایج جستجو برای: $value",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else null,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            textAlign = TextAlign.Right
        ),
        singleLine = true,
        interactionSource = interactionSource,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Ascii
        )
    )
}

@Composable
fun ShipSection(
    title: String,
    ships: List<Ship>,
    isExpanded: Boolean,
    onExpandChange: () -> Unit,
    onShipSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSectionActive = title.contains("فعال")
    val mainColor = if (isSectionActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSectionActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = mainColor.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandChange() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(mainColor.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSectionActive) Icons.Default.DirectionsBoat else Icons.Default.Archive,
                            contentDescription = null,
                            tint = mainColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = mainColor
                        )
                        Text(
                            text = "${ships.size} کشتی • ${formatWeightWithDetail(ships.sumOf { it.remainingTonnage.toDouble() }.toFloat())}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = mainColor.copy(alpha = 0.7f)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "بستن" else "باز کردن",
                    modifier = Modifier.rotate(if (isExpanded) 180f else 0f),
                    tint = mainColor
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                TonnageInfo(ships)
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = mainColor.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(ships) { ship ->
                        ShipCard(
                            ship = ship,
                            onClick = { onShipSelected(ship.name) },
                            color = mainColor,
                            isActive = ship.isActive
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TonnageInfo(ships: List<Ship>) {
    val totalTonnage = ships.sumOf { it.totalTonnage.toDouble() }.toFloat()
    val loadedTonnage = ships.sumOf { (it.totalTonnage - it.remainingTonnage).toDouble() }.toFloat()
    val remainingTonnage = totalTonnage - loadedTonnage
    val progress = calculateProgress(loadedTonnage, totalTonnage)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            TonnageInfoItem("کل", totalTonnage, MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            TonnageInfoItem("بارگیری شده", loadedTonnage, MaterialTheme.colorScheme.secondary)
        }
        Column(horizontalAlignment = Alignment.End) {
            TonnageInfoItem("مانده", remainingTonnage, MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${(progress * 100).toInt()}% تکمیل شده",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun TonnageInfoItem(label: String, value: Float, color: Color) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = formatNumber(value.toInt()),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = "$label تن",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ShipCard(
    ship: Ship,
    onClick: () -> Unit,
    color: Color,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val cardAlpha = if (isActive) 0.1f else 0.05f
    val contentAlpha = if (isActive) 1f else 0.35f
    val borderAlpha = if (isActive) 0.2f else 0.05f
    val secondaryAlpha = if (isActive) 0.7f else 0.25f

    val progress = calculateProgress(
        ship.totalTonnage - ship.remainingTonnage,
        ship.totalTonnage
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = cardAlpha)
        ),
        border = BorderStroke(1.dp, color.copy(alpha = borderAlpha)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color.copy(alpha = if (isActive) 0.1f else 0.03f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBoat,
                            contentDescription = null,
                            tint = color.copy(alpha = contentAlpha),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = ship.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = color.copy(alpha = contentAlpha)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = color.copy(alpha = secondaryAlpha),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${formatNumber(ship.quotaCount)} کوتاژ",
                                style = MaterialTheme.typography.bodySmall,
                                color = color.copy(alpha = secondaryAlpha)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isActive) {
                        color.copy(alpha = 0.1f)
                    } else {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.05f)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (isActive) {
                            color.copy(alpha = borderAlpha)
                        } else {
                            MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isActive) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = if (isActive) "${(progress * 100).toInt()}%" else "غیرفعال",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (isActive) {
                                color.copy(alpha = contentAlpha)
                            } else {
                                MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                            }
                        )
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isActive) {
                        color.copy(alpha = contentAlpha)
                    } else {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                    },
                    trackColor = color.copy(alpha = 0.1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = formatNumber(ship.totalTonnage.toInt()),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = color.copy(alpha = contentAlpha)
                    )
                    Text(
                        text = "کل",
                        style = MaterialTheme.typography.bodySmall,
                        color = color.copy(alpha = secondaryAlpha)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = formatNumber((ship.totalTonnage - ship.remainingTonnage).toInt()),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = color.copy(alpha = contentAlpha)
                    )
                    Text(
                        text = "بارگیری",
                        style = MaterialTheme.typography.bodySmall,
                        color = color.copy(alpha = secondaryAlpha)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = formatNumber(ship.remainingTonnage.toInt()),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = color.copy(alpha = contentAlpha)
                    )
                    Text(
                        text = "مانده",
                        style = MaterialTheme.typography.bodySmall,
                        color = color.copy(alpha = secondaryAlpha)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShipDetails(
    initialShipName: String,
    viewModel: ReportsViewModel,
    onWarehouseSelected: (String) -> Unit
) {
    val ship by viewModel.selectedShip.collectAsState()
    val selectedShipQuotas by viewModel.selectedShipQuotas.collectAsState()
    var showWarningDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    val warnings = remember(selectedShipQuotas) {
        selectedShipQuotas.mapNotNull { quota -> calculateWarningStatus(quota) }
    }

    val tabs = listOf(
        TabItem(
            icon = Icons.Filled.Analytics,
            title = "آمار کلی"
        ),
        TabItem(
            icon = Icons.Filled.Warehouse,
            title = "انبار و کوتاژ"
        )
    )

    LaunchedEffect(warnings) {
        if (warnings.isNotEmpty()) {
            showWarningDialog = true
        }
    }

    LaunchedEffect(initialShipName) {
        viewModel.clearCurrentShipData()
        viewModel.loadShipDetails(initialShipName)
        viewModel.loadShipQuotas(initialShipName)
    }

    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }

    LaunchedEffect(selectedTabIndex) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(selectedTabIndex)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ModernTabRows(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { index ->
                    selectedTabIndex = index
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                tabs = tabs
            )

            when (uiState) {
                is ReportsViewModel.UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ReportsViewModel.UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (uiState as ReportsViewModel.UiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is ReportsViewModel.UiState.Success -> {
                    ship?.let { shipDetails ->
                        HorizontalPager(
                            state = pagerState,
                            userScrollEnabled = true,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            AnimatedContent(
                                targetState = page,
                                transitionSpec = {
                                    if (targetState > initialState) {
                                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                                slideOutHorizontally { width -> -width } + fadeOut()
                                    } else {
                                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                                slideOutHorizontally { width -> width } + fadeOut()
                                    }
                                },
                                label = "Page transition"
                            ) { targetPage ->
                                when (targetPage) {
                                    0 -> ShipStatisticsTab(shipDetails)
                                    1 -> WarehousesAndQuotasTab(
                                        shipDetails = shipDetails,
                                        onWarehouseSelected = onWarehouseSelected,
                                        viewModel = viewModel
                                    )
                                }
                            }
                        }
                    } ?: run {
                        Text("اطلاعات کشتی در دسترس نیست")
                    }
                }
            }
        }
    }

    if (showWarningDialog && warnings.isNotEmpty()) {
        QuotaWarningDialog(
            warnings = warnings,
            onDismiss = { showWarningDialog = false },
            viewModel = viewModel
        )
    }
}

@Composable
private fun ModernTabRows(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<TabItem>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, tab ->
                ModernTabs(
                    selected = selectedTabIndex == index,
                    onClick = { onTabSelected(index) },
                    icon = tab.icon,
                    text = tab.title,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ModernTabs(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isPressed -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
            selected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
            else -> Color.Transparent
        },
        label = "Background color animation"
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
        },
        label = "Content color animation"
    )

    Surface(
        onClick = onClick,
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        interactionSource = interactionSource,
        modifier = modifier
            .height(40.dp)
            .graphicsLayer {
                scaleX = if (isPressed) 0.97f else 1f
                scaleY = if (isPressed) 0.97f else 1f
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = contentColor
            )
        }
    }
}

data class TabItem(
    val icon: ImageVector,
    val title: String
)

@Composable
fun ShipStatisticsTab(shipDetails: Ship) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        MainInfoSection(shipDetails)
    }
}

@Composable
fun MainInfoSection(shipDetails: Ship) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Basic Info Card
        item {
            StatisticsCard(
                title = "اطلاعات کشتی",
                icon = Icons.Default.DirectionsBoat,
                content = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // شناسه‌های اصلی
                        StatRow(
                            label = "نام کشتی",
                            value = shipDetails.name,
                            icon = Icons.Default.Title
                        )
                        StatRow(
                            label = "وضعیت",
                            value = if (shipDetails.isActive) "فعال" else "غیرفعال",
                            icon = if (shipDetails.isActive) Icons.Default.Check else Icons.Default.Close,
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        // آمار انبارها و کوتاژها
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                StatBox(
                                    title = "تعداد انبارها",
                                    value = formatNumber(shipDetails.warehouseCount),
                                    icon = Icons.Default.Warehouse,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                StatBox(
                                    title = "تعداد کوتاژها",
                                    value = formatNumber(shipDetails.quotaCount),
                                    icon = Icons.Default.Description,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                StatBox(
                                    title = "تعداد حواله ها",
                                    value = formatNumber(shipDetails.totalVoucherCount),
                                    icon = Icons.Default.Receipt,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            )
        }

        // Tonnage Card
        item {
            StatisticsCard(
                title = "آمار تناژ و بارگیری",
                icon = Icons.Default.Scale,
                content = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // پیشرفت کلی
                        val progress = calculateProgress(
                            shipDetails.totalTonnage - shipDetails.remainingTonnage,
                            shipDetails.totalTonnage
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "پیشرفت کلی بارگیری",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${(progress * 100).toInt()}% تکمیل شده",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            CircularProgress(
                                progress = progress,
                                size = 60.dp,
                                strokeWidth = 8.dp
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        // آمار اصلی تناژ
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TonnageStatBox(
                                title = "تناژ کل",
                                value = shipDetails.totalTonnage,
                                icon = Icons.Default.Scale,
                                color = MaterialTheme.colorScheme.primary
                            )
                            TonnageStatBox(
                                title = "بارگیری شده",
                                value = shipDetails.totalTonnage - shipDetails.remainingTonnage,
                                icon = Icons.Default.LocalShipping,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            TonnageStatBox(
                                title = "مانده",
                                value = shipDetails.remainingTonnage,
                                icon = Icons.Default.PendingActions,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun StatBox(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color.copy(alpha = 0.8f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun TonnageStatBox(
    title: String,
    value: Float,
    icon: ImageVector,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.size(60.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier
                    .padding(16.dp)
                    .size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = "${formatNumber(value.toInt())} تن",
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CircularProgress(
    progress: Float,
    size: Dp,
    strokeWidth: Dp
) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            strokeWidth = strokeWidth
        )
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun StatisticsCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content
            content()
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun WarehousesAndQuotasTab(
    shipDetails: Ship,
    onWarehouseSelected: (String) -> Unit,
    viewModel: ReportsViewModel
) {
    var selectedSection by remember { mutableIntStateOf(0) }
    val sections = listOf("انبارها", "کوتاژها")
    var searchQuery by remember { mutableStateOf("") }
    val selectedShipQuotas by viewModel.selectedShipQuotas.collectAsState()
    val filterInput: (String) -> String = { input ->
        when (selectedSection) {
            0 -> input.filter { char ->
                char.isDigit() || char.toString().matches(Regex("[\\u0600-\\u06FF\\s]"))
            }
            1 -> input.filter { char -> char.isDigit() }
            else -> input
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { newValue ->
                searchQuery = filterInput(newValue)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = {
                Text(
                    when (selectedSection) {
                        0 -> "جستجوی نام انبار..."
                        1 -> "جستجوی شماره کوتاژ..."
                        else -> "جستجو..."
                    }
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "جستجو"
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = when (selectedSection) {
                    0 -> KeyboardType.Text
                    1 -> KeyboardType.NumberPassword
                    else -> KeyboardType.Text
                },
                imeAction = ImeAction.Search
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            sections.forEachIndexed { index, title ->
                SectionButton(
                    title = title,
                    isSelected = selectedSection == index,
                    onClick = {
                        selectedSection = index
                        searchQuery = ""
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        when (selectedSection) {
            0 -> WarehousesSection(
                warehouses = shipDetails.warehouses.filter {
                    it.name.contains(searchQuery, ignoreCase = true)
                },
                onWarehouseSelected = onWarehouseSelected
            )
            1 -> QuotasList(
                quotas = selectedShipQuotas,
                searchQuery = searchQuery,
                onEdit = { oldQuotaNumber, newQuotaData ->
                    viewModel.editQuota(oldQuotaNumber, newQuotaData)
                },
                onToggleStatus = { quotaNumber ->
                    viewModel.toggleQuotaStatus(quotaNumber)
                },
                onDelete = { quota ->
                    viewModel.deleteQuota(quota)
                },
                viewModel = viewModel
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuotaWarningDialog(
    warnings: List<WarningStatus>,
    onDismiss: () -> Unit,
    viewModel: ReportsViewModel
) {
    val pagerState = rememberPagerState(pageCount = { warnings.size })
    val coroutineScope = rememberCoroutineScope()
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dialog scale"
    )
    val alpha by animateFloatAsState(
        targetValue = 1f,
        label = "dialog alpha"
    )

    if (warnings.isEmpty()) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.8f)
                .padding(vertical = 8.dp)
                .scale(scale)
                .alpha(alpha),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Section - Fixed
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    DialogHeader(
                        currentPage = pagerState.currentPage,
                        totalPages = warnings.size,
                        onClose = onDismiss
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Content Section - Scrollable
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(warnings) { warning ->
                        QuotaCard(
                            warning = warning,
                            viewModel = viewModel
                        )
                    }
                }

                // Navigation Section - Fixed
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    PageNavigation(
                        currentPage = pagerState.currentPage,
                        pageCount = warnings.size,
                        onNavigate = { page ->
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(page)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogHeader(
    currentPage: Int,
    totalPages: Int,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Header Top Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Warning Icon & Title
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "هشدار درصد کوتاژ",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }

            // Close Button
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "بستن",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Page Counter (if more than one warning)
        if (totalPages > 1) {
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.wrapContentWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = buildString {
                            append(currentPage + 1)
                            append(" از ")
                            append(totalPages)
                            append(" کوتاژ")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Divider
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
private fun PageNavigation(
    currentPage: Int,
    pageCount: Int,
    onNavigate: (Int) -> Unit
) {
    if (pageCount <= 1) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Page Indicator Dots
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            repeat(pageCount) { page ->
                PageIndicatorDot(
                    isSelected = page == currentPage,
                    onClick = { onNavigate(page) }
                )
                if (page < pageCount - 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }

        // Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NavigationButton(
                text = "قبلی",
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                enabled = currentPage > 0,
                onClick = { onNavigate(currentPage - 1) }
            )

            NavigationButton(
                text = "بعدی",
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                enabled = currentPage < pageCount - 1,
                onClick = { onNavigate(currentPage + 1) }
            )
        }
    }
}

@Composable
private fun PageIndicatorDot(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
        },
        label = "dot color"
    )

    val size by animateDpAsState(
        targetValue = if (isSelected) 10.dp else 8.dp,
        label = "dot size"
    )

    Box(
        modifier = Modifier
            .size(size)
            .background(color, CircleShape)
            .clickable(onClick = onClick)
    )
}

@Composable
private fun NavigationButton(
    text: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        ),
        border = BorderStroke(
            1.dp,
            if (enabled) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            }
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
private fun QuotaCard(
    warning: WarningStatus,
    viewModel: ReportsViewModel
) {
    var isExpanded by remember { mutableStateOf(false) }
    var isPercentageRestrictionLoading by remember { mutableStateOf(false) }
    var isStatusToggleLoading by remember { mutableStateOf(false) }
    val mainColor = MaterialTheme.colorScheme.error
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.05f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        )
    ) {
        Column {
            QuotaCardHeader(
                shortQuotaNumber = warning.quotaNumber.takeLast(4),
                warning = warning,
                isExpanded = isExpanded,
                onExpandClick = { isExpanded = !isExpanded }
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 8.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    )
                ) {
                    QuotaCardContent(warning = warning)

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = mainColor.copy(alpha = 0.1f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Percentage Restriction Control
                        if (isPercentageRestrictionLoading) {
                            LoadingActionButton(
                                label = if (warning.isPercentageRestricted) "آزاد کردن درصد" else "محدود کردن درصد",
                                color = mainColor
                            )
                        } else {
                            ActionButton(
                                icon = if (warning.isPercentageRestricted) {
                                    Icons.Default.ToggleOff
                                } else {
                                    Icons.Default.ToggleOn
                                },
                                label = if (warning.isPercentageRestricted) "آزاد کردن درصد" else "محدود کردن درصد",
                                color = mainColor,
                                onClick = {
                                    isPercentageRestrictionLoading = true
                                    viewModel.toggleQuotaPercentageRestriction(
                                        quotaNumber = warning.quotaNumber,
                                        isRestricted = !warning.isPercentageRestricted
                                    ) {
                                        isPercentageRestrictionLoading = false
                                    }
                                }
                            )
                        }

                        // Status Toggle Control
                        if (isStatusToggleLoading) {
                            LoadingActionButton(
                                label = if (warning.isActive) "غیرفعال‌سازی کوتاژ" else "فعال‌سازی کوتاژ",
                                color = mainColor
                            )
                        } else {
                            ActionButton(
                                icon = if (warning.isActive) {
                                    Icons.Default.Close
                                } else {
                                    Icons.Default.Check
                                },
                                label = if (warning.isActive) " غیرفعال‌سازی کوتاژ" else "فعال‌سازی کوتاژ",
                                color = mainColor,
                                onClick = {
                                    isStatusToggleLoading = true
                                    scope.launch {
                                        viewModel.toggleQuotaStatus(warning.quotaNumber)
                                        isStatusToggleLoading = false
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingActionButton(
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = color.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = color,
                strokeWidth = 2.dp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun QuotaCardHeader(
    shortQuotaNumber: String,
    warning: WarningStatus,
    isExpanded: Boolean,
    onExpandClick: () -> Unit
) {
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "expand icon rotation"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onExpandClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuotaNumberBadge(number = shortQuotaNumber)

            val diff = abs(warning.remainingTonnage - warning.percentageAmount)
            Column {
                Text(
                    text = "${formatNumber(diff.toInt())} تن",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "اختلاف با درصد",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ExpandMore,
            contentDescription = if (isExpanded) "بستن" else "بازکردن",
            modifier = Modifier.rotate(rotationState),
            tint = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun QuotaNumberBadge(number: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = number,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QuotaCardContent(warning: WarningStatus) {
    Column(
        modifier = Modifier.padding(
            start = 8.dp,
            end = 8.dp,
            bottom = 8.dp
        )
    ) {
        HorizontalDivider(
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoSection(
                title = "اطلاعات اصلی",
                items = listOf(
                    "شماره کوتاژ" to warning.quotaNumber,
                    "درصد تنظیم شده" to "${warning.percentage.format(1)}%"
                )
            )

            InfoSection(
                title = "جزئیات تناژ",
                items = listOf(
                    "تناژ درصد" to "${formatNumber(warning.percentageAmount.toInt())} تن",
                    "تناژ مانده" to "${formatNumber(warning.remainingTonnage.toInt())} تن"
                )
            )
        }
    }
}

@Composable
private fun InfoSection(
    title: String,
    items: List<Pair<String, String>>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.error.copy(alpha = 0.05f),
                RoundedCornerShape(12.dp)
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        items.forEach { (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun calculateWarningStatus(quota: Quota): WarningStatus? {
    if (!quota.isActive) {
        return null
    }

    if (!quota.isPercentageRestricted!! || quota.percentage == null) {
        return null
    }

    val totalTonnage = quota.totalTonnage
    val percentageAmount = totalTonnage * (quota.percentage / 100)
    val remainingTonnage = quota.remainingTonnage
    val warningThreshold = 5000f

    val diff = abs(remainingTonnage - percentageAmount)

    return if (diff <= warningThreshold) {
        WarningStatus(
            show = true,
            quotaNumber = quota.number,
            percentage = quota.percentage,
            remainingTonnage = remainingTonnage,
            percentageAmount = percentageAmount,
            isActive = true,
            isPercentageRestricted = quota.isPercentageRestricted
        )
    } else null
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuotaPercentageDialog(
    quota: Quota,
    onDismiss: () -> Unit,
    onConfirm: (QuotaPercentageData) -> Unit
) {
    var percentage by remember { mutableDoubleStateOf(quota.percentage ?: 0.0) }
    var calculatedValues by remember { mutableStateOf(calculateValues(quota.totalTonnage, percentage, quota.remainingTonnage)) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var isContentVisible by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(150)
        isContentVisible = true
    }

    // هماهنگ‌سازی selectedTab با تغییرات pagerState
    LaunchedEffect(pagerState.currentPage) {
        selectedTab = pagerState.currentPage
    }

    // هماهنگ‌سازی pagerState با تغییرات selectedTab
    LaunchedEffect(selectedTab) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(selectedTab)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .heightIn(max = (LocalConfiguration.current.screenHeightDp * 0.8).dp)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                DialogHeader(quota)

                Spacer(modifier = Modifier.height(24.dp))

                // Tabs
                DialogTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { index ->
                        selectedTab = index
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 300.dp)
                ) {
                    HorizontalPager(
                        state = pagerState
                    ) { page ->
                        when (page) {
                            0 -> PercentageInputTab(
                                percentage = percentage,
                                calculatedValues = calculatedValues,
                                onPercentageChange = { newPercentage ->
                                    if (newPercentage in 0.0..2.0) {
                                        percentage = newPercentage
                                        calculatedValues = calculateValues(
                                            quota.totalTonnage,
                                            newPercentage,
                                            quota.remainingTonnage
                                        )
                                    }
                                }
                            )
                            1 -> CalculationDetailsTab(calculatedValues)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            val quotaData = QuotaPercentageData(
                                quotaNumber = quota.number,
                                percentage = percentage,
                                calculations = calculatedValues,
                                isEnabled = if (percentage > 0.0) 1 else 0
                            )
                            onConfirm(quotaData)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text("تایید و ذخیره")
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text("انصراف")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogHeader(quota: Quota) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Animated Icon Container
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                ),
                label = ""
            )

            Icon(
                imageVector = Icons.Default.AddTask,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(32.dp)
                    .scale(scale)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "تنظیم درصد کوتاژ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "شماره کوتاژ: ${quota.number}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun DialogTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            val tabs = listOf(
                TabItem(Icons.Default.AddTask, "تنظیم درصد"),
                TabItem(Icons.Default.Info, "جزئیات محاسبات")
            )

            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedTab == index
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    },
                    label = ""
                )

                Surface(
                    onClick = { onTabSelected(index) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(12.dp),
                    color = backgroundColor
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = null,
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            },
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                if (index < tabs.lastIndex) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}

private fun lerp(start: Int, end: Int, fraction: Float): Int {
    return (start + (end - start) * fraction).roundToInt()
}

@Composable
private fun PercentageInputTab(
    percentage: Double,
    calculatedValues: CalculationResult,
    onPercentageChange: (Double) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        val progress = percentage / 2.0
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.toFloat())
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Percentage Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Decrease Button
            EnhancedIconButton(
                onClick = { onPercentageChange(percentage - 0.1) },
                enabled = percentage > 0.0,
                icon = Icons.Default.Remove
            )

            // Current Percentage Display
            PercentageDisplay(percentage)

            // Increase Button
            EnhancedIconButton(
                onClick = { onPercentageChange(percentage + 0.1) },
                enabled = percentage < 2.0,
                icon = Icons.Default.Add
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Select Buttons
        QuickSelectButtons(
            currentPercentage = percentage,
            onPercentageSelected = onPercentageChange
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Results Preview
        ResultsPreview(calculatedValues)
    }
}

@Composable
private fun EnhancedIconButton(
    onClick: () -> Unit,
    enabled: Boolean,
    icon: ImageVector
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        label = ""
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .scale(scale)
            .background(
                color = if (enabled) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                },
                shape = CircleShape
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = false),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            }
        )
    }
}

@Composable
private fun PercentageDisplay(percentage: Double) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        modifier = Modifier.width(120.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "%.1f%%".format(percentage),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun QuickSelectButtons(
    currentPercentage: Double,
    onPercentageSelected: (Double) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(0.0, 0.7, 1.0, 1.5, 2.0).forEach { value ->
            QuickSelectButton(
                value = value,
                isSelected = currentPercentage == value,
                onClick = { onPercentageSelected(value) }
            )
        }
    }
}

@Composable
private fun RowScope.QuickSelectButton(
    value: Double,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            }
        ),
        modifier = Modifier.weight(1f)
    ) {
        Text(
            text = "%.1f%%".format(value),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ResultsPreview(calculatedValues: CalculationResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        ResultRow(
            label = "تناژ درصد",
            value = formatNumber(calculatedValues.percentageAmount.toInt()),
            suffix = "تن"
        )
        ResultRow(
            label = "مانده درصد",
            value = formatNumber(calculatedValues.remainingAfterPercentage.toInt()),
            suffix = "تن"
        )
        ResultRow(
            label = "مانده کل درصد",
            value = formatNumber(calculatedValues.totalRemainingAfterPercentage.toInt()),
            suffix = "تن"
        )
    }
}

@Composable
private fun ResultRow(
    label: String,
    value: String,
    suffix: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        AnimatedNumber(
            targetValue = value.replace(",", "").toInt(),
            suffix = suffix
        )
    }
}

@Composable
fun AnimatedNumber(
    targetValue: Int,
    suffix: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    var previousValue by remember { mutableIntStateOf(0) }
    var displayValue by remember { mutableIntStateOf(0) }

    LaunchedEffect(targetValue) {
        val startValue = previousValue
        previousValue = targetValue

        (0..100).forEach { step ->
            val progress = step / 100f
            displayValue = lerp(startValue, targetValue, progress)
            delay(5)
        }
        displayValue = targetValue
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatNumber(displayValue),
            style = style,
            fontWeight = fontWeight,
            color = color
        )
        Text(
            text = suffix,
            style = style,
            color = color.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun CalculationDetailsTab(calculatedValues: CalculationResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        DetailCard(
            title = "تناژ درصد",
            value = formatNumber(calculatedValues.percentageAmount.toInt()),
            suffix = "تن",
            icon = Icons.Default.Scale,
            description = "مقدار تناژ محاسبه شده بر اساس درصد تعیین شده"
        )

        Spacer(modifier = Modifier.height(16.dp))

        DetailCard(
            title = "مانده درصد",
            value = formatNumber(calculatedValues.remainingAfterPercentage.toInt()),
            suffix = "تن",
            icon = Icons.Default.Remove,
            description = "مقدار باقیمانده از تناژ کل پس از کسر درصد"
        )

        Spacer(modifier = Modifier.height(16.dp))

        DetailCard(
            title = "مانده کل",
            value = formatNumber(calculatedValues.totalRemainingAfterPercentage.toInt()),
            suffix = "تن",
            icon = Icons.Default.Info,
            description = "مقدار نهایی باقیمانده پس از اعمال درصد"
        )
    }
}

@Composable
private fun DetailCard(
    title: String,
    value: String,
    suffix: String,
    icon: ImageVector,
    description: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedNumber(
                        targetValue = value.replace(",", "").toInt(),
                        suffix = suffix,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

private fun calculateValues(
    totalTonnage: Float,
    percentage: Double,
    remainingTonnage: Float
): CalculationResult {
    val percentageAmount = totalTonnage * (percentage / 100)
    val remainingAfterPercentage = totalTonnage - percentageAmount
    val totalRemainingAfterPercentage = remainingTonnage - percentageAmount

    return CalculationResult(
        percentageAmount = percentageAmount,
        remainingAfterPercentage = remainingAfterPercentage,
        totalRemainingAfterPercentage = totalRemainingAfterPercentage
    )
}

@Composable
fun QuotasList(
    quotas: List<Quota>,
    searchQuery: String,
    onEdit: (String, QuotaEditData) -> Unit,
    onToggleStatus: (String) -> Unit,
    onDelete: (Quota) -> Unit,
    viewModel: ReportsViewModel
) {
    val sortedQuotas = quotas.sortedWith(
        compareBy<Quota> { !it.isActive }
            .thenBy { it.remainingTonnage }
    )

    val groupedAndSortedQuotas = sortedQuotas
        .groupBy { it.shippingCompany }
        .toSortedMap()

    val filteredQuotas = remember(searchQuery, groupedAndSortedQuotas) {
        if (searchQuery.isEmpty()) {
            groupedAndSortedQuotas
        } else {
            groupedAndSortedQuotas.mapValues { (_, quotas) ->
                quotas.filter { it.number.contains(searchQuery, ignoreCase = true) }
            }.filter { it.value.isNotEmpty() }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filteredQuotas.forEach { (company, companyQuotas) ->
                item(key = company) {
                    CompanyHeader(
                        company = company,
                        quotaCount = companyQuotas.size
                    )
                }

                items(
                    items = companyQuotas,
                    key = { it.number }
                ) { quota ->
                    QuotaCard(
                        quota = quota,
                        onEdit = onEdit,
                        onToggleStatus = onToggleStatus,
                        onDelete = onDelete,
                        onPercentageChange = { percentageData ->
                            viewModel.updateQuotaPercentage(percentageData)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun CompanyHeader(
    company: String,
    quotaCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = company,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "شرکت باربری",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "$quotaCount کوتاژ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SectionButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.padding(horizontal = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            1.dp,
            if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            }
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
private fun WarehousesSection(
    warehouses: List<Warehouse>,
    onWarehouseSelected: (String) -> Unit
) {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.surfaceTint,
        MaterialTheme.colorScheme.inversePrimary
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        items(warehouses) { warehouse ->
            val colorIndex = warehouses.indexOf(warehouse) % colors.size
            WarehouseCard(
                warehouse = warehouse,
                color = colors[colorIndex],
                onClick = { onWarehouseSelected(warehouse.name) }
            )
        }
    }
}

@Composable
private fun WarehouseCard(
    warehouse: Warehouse,
    color: Color,
    onClick: () -> Unit
) {
    val loadedTonnage = warehouse.totalTonnage - warehouse.remainingTonnage
    val progress = calculateProgress(loadedTonnage, warehouse.totalTonnage)
    var animatedProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(progress) {
        animate(
            initialValue = animatedProgress,
            targetValue = progress,
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        ) { value, _ -> animatedProgress = value }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Title and Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(color.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warehouse,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = warehouse.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${warehouse.quotaCount} کوتاژ فعال",
                            style = MaterialTheme.typography.bodyMedium,
                            color = color.copy(alpha = 0.7f)
                        )
                    }
                }

                // Progress and Remaining Section
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = formatWeightWithDetail(warehouse.remainingTonnage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = color.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${(progress * 100).roundToInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .background(color.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                )
            }

            // Total and Loaded Weight Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Total Weight (Right Side)
                StatsBox(
                    icon = Icons.Default.Scale,
                    label = "تناژ کل",
                    value = formatWeightWithDetail(warehouse.totalTonnage),
                    color = color,
                    alignment = Alignment.Start
                )

                // Loaded Weight (Left Side)
                StatsBox(
                    icon = Icons.Default.LocalShipping,
                    label = "بارگیری شده",
                    value = formatWeightWithDetail(loadedTonnage),
                    color = color,
                    alignment = Alignment.End
                )
            }
        }
    }
}

@Composable
private fun StatsBox(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    alignment: Alignment.Horizontal
) {
    val (mainValue, detail) = remember(value) {
        value.split(" (").let { parts ->
            if (parts.size > 1) {
                Pair(parts[0], parts[1].removeSuffix(")"))
            } else {
                Pair(parts[0], null)
            }
        }
    }

    Column(
        horizontalAlignment = alignment,
        modifier = Modifier.width(IntrinsicSize.Max)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            if (alignment == Alignment.End) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color.copy(alpha = 0.7f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color.copy(alpha = 0.7f)
                )
            }
        }
        Text(
            text = mainValue,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        detail?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = color.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    value: String,
    description: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = color
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = color.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun ProgressBar(title: String, progress: Float, value: Int, color: Color, suffix: String) {
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    val animatedProgressValue by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000), label = ""
    )

    LaunchedEffect(progress) {
        animatedProgress = animatedProgressValue
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "${formatNumber(value)}$suffix",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun WarehouseDetails(
    shipName: String,
    warehouseName: String,
    viewModel: ReportsViewModel
) {
    val warehouse by viewModel.selectedWarehouse.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val filteredSummary by viewModel.filteredSummary.collectAsState()
    var selectedQuota by remember { mutableStateOf<String?>(null) }
    var startDateTime by remember { mutableStateOf<String?>(null) }
    var endDateTime by remember { mutableStateOf<String?>(null) }
    var availableDates by remember { mutableStateOf<List<String>>(emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(shipName, warehouseName) {
        viewModel.loadWarehouseDetails(shipName, warehouseName)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            WarehouseContent(
                warehouse = warehouse,
                uiState = uiState,
                filteredSummary = filteredSummary,
                selectedQuota = selectedQuota,
                startDateTime = startDateTime,
                endDateTime = endDateTime,
                availableDates = availableDates,
                shipName = shipName,
                onQuotaSelected = { quota ->
                    selectedQuota = if (selectedQuota == quota.number) null else quota.number
                    availableDates = if (selectedQuota != null) {
                        warehouse?.quotas
                            ?.find { it.number == selectedQuota }
                            ?.exitDates?.map { it.date }
                            ?.distinct()
                            ?.sorted()
                            ?: emptyList()
                    } else {
                        emptyList()
                    }
                    startDateTime = null
                    endDateTime = null
                    viewModel.clearFilteredSummary()
                },
                onStartDateTimeSelected = { dateTime ->
                    startDateTime = dateTime
                    if (dateTime == null || (endDateTime != null && endDateTime!! < dateTime)) {
                        endDateTime = null
                    }
                    viewModel.clearFilteredSummary()
                },
                onEndDateTimeSelected = { dateTime ->
                    endDateTime = dateTime
                    viewModel.clearFilteredSummary()
                },
                onFilterApplied = {
                    selectedQuota?.let { quota ->
                        viewModel.getFilteredSummary(
                            shipName = shipName,
                            warehouseName = warehouse?.name ?: "",
                            selectedQuota = quota,
                            startDateTime = startDateTime,
                            endDateTime = endDateTime
                        )
                    }
                },
                onExport = { format ->
                    filteredSummary?.let { summary ->
                        viewModel.exportData(format, summary)
                    }
                }
            )
        }
    }

    // نمایش Snackbar
    LaunchedEffect(snackbarHostState) {
        viewModel.snackbarMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
}

@Composable
fun WarehouseContent(
    warehouse: Warehouse?,
    uiState: ReportsViewModel.UiState,
    filteredSummary: FilteredSummary?,
    selectedQuota: String?,
    startDateTime: String?,
    endDateTime: String?,
    availableDates: List<String>,
    shipName: String,
    onQuotaSelected: (Quota) -> Unit,
    onStartDateTimeSelected: (String?) -> Unit,
    onEndDateTimeSelected: (String?) -> Unit,
    onFilterApplied: () -> Unit,
    onExport: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is ReportsViewModel.UiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is ReportsViewModel.UiState.Error -> {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            is ReportsViewModel.UiState.Success -> {
                warehouse?.let { warehouseDetails ->
                    WarehouseMainCard(
                        warehouseName = warehouseDetails.name,
                        shipName = shipName,
                        voucherCount = warehouseDetails.quotas.sumOf { it.voucherCount }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "فیلتر و خروجی",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        ExportOptions(
                            onExport = { format ->
                                filteredSummary?.let {
                                    onExport(format)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    QuotaSelector(
                        quotas = warehouseDetails.quotas,
                        selectedQuota = selectedQuota,
                        onQuotaSelected = onQuotaSelected
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DateTimePicker(
                            label = "از تاریخ و زمان",
                            selectedDateTime = startDateTime,
                            availableDates = availableDates,
                            onDateTimeSelected = onStartDateTimeSelected,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        DateTimePicker(
                            label = "تا تاریخ و زمان",
                            selectedDateTime = endDateTime,
                            availableDates = availableDates.filter { it >= (startDateTime?.split(" ")?.first() ?: "") },
                            onDateTimeSelected = {
                                onEndDateTimeSelected(it)
                                if (it != null) {
                                    onFilterApplied()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    filteredSummary?.let { summary ->
                        InformationReport(summary)
                    }
                }
            }
        }
    }
}

@Composable
fun ExportOptions(onExport: (String) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { onExport("pdf") },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF")
            Spacer(Modifier.width(4.dp))
            Text("PDF")
        }
    }
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    timePickerState: TimePickerState
) {
    Dialog(onDismissRequest = onCancel) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "انتخاب زمان",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                // نمایش ساعت و دقیقه به صورت RTL
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // دقیقه
                    Text(
                        text = String.format("%02d", timePickerState.minute),
                        style = MaterialTheme.typography.displayMedium
                    )
                    Text(
                        text = ":",
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    // ساعت
                    Text(
                        text = String.format("%02d", timePickerState.hour),
                        style = MaterialTheme.typography.displayMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                TimePicker(state = timePickerState)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onCancel) {
                        Text("لغو")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onConfirm) {
                        Text("تأیید")
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePicker(
    label: String,
    selectedDateTime: String?,
    availableDates: List<String>,
    onDateTimeSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var tempDate by remember { mutableStateOf<String?>(null) }
    var tempTime by remember { mutableStateOf<String?>(null) }

    OutlinedTextField(
        value = selectedDateTime ?: "انتخاب تاریخ و زمان",
        onValueChange = { },
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "انتخاب تاریخ و زمان")
            }
        },
        modifier = modifier
    )

    if (showDatePicker) {
        Dialog(onDismissRequest = { showDatePicker = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("انتخاب تاریخ", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn {
                        items(availableDates) { date ->
                            Text(
                                text = date,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        tempDate = date
                                        showDatePicker = false
                                        showTimePicker = true
                                    }
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState()
        TimePickerDialog(
            onCancel = { showTimePicker = false },
            onConfirm = {
                tempTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                val dateTimeString = "$tempDate $tempTime"
                onDateTimeSelected(dateTimeString)
                showTimePicker = false
            },
            timePickerState = timePickerState
        )
    }
}

@Composable
fun WarehouseMainCard(warehouseName: String, shipName: String, voucherCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "اطلاعات انبار و کشتی",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.Default.Warehouse,
                    contentDescription = "Warehouse Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoCard(
                    icon = Icons.Default.Store,
                    title = "انبار",
                    value = warehouseName,
                    color = MaterialTheme.colorScheme.secondary
                )
                InfoCard(
                    icon = Icons.Default.DirectionsBoat,
                    title = "کشتی",
                    value = shipName,
                    color = MaterialTheme.colorScheme.tertiary
                )
                InfoCard(
                    icon = Icons.Default.Description,
                    title = "حواله‌ها",
                    value = voucherCount.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun InfoCard(icon: ImageVector, title: String, value: String, color: Color) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun QuotaSelector(
    quotas: List<Quota>,
    selectedQuota: String?,
    onQuotaSelected: (Quota) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(quotas) { quota ->
            QuotaChip(
                quota = quota,
                isSelected = selectedQuota == quota.number,
                onSelect = { onQuotaSelected(quota) }
            )
        }
    }
}

@Composable
fun QuotaChip(
    quota: Quota,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onSelect,
        label = { Text(quota.number) }
    )
}

@Composable
fun InformationReport(summary: FilteredSummary) {
    var searchQuery by remember { mutableStateOf("") }
    var filteredVoucherDetails by remember { mutableStateOf(summary.voucherDetails) }

    Column(modifier = Modifier.padding(4.dp)) {
        Text(
            "اطلاعات حواله‌ها",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 2.dp)
        )

        AttractiveSummaryCards(
            voucherCount = summary.voucherCount,
            totalWeight = summary.totalNetWeight
        )

        Spacer(modifier = Modifier.height(4.dp))

        QuotaSearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { newQuery ->
                searchQuery = newQuery
                filteredVoucherDetails = if (newQuery.isEmpty()) {
                    summary.voucherDetails
                } else {
                    summary.voucherDetails.filter { voucher ->
                        voucher.trackingNumber.contains(newQuery, ignoreCase = true) ||
                                voucher.scaleReceiptNumber.contains(newQuery, ignoreCase = true) ||
                                voucher.exitDate.contains(newQuery, ignoreCase = true)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn {
            items(filteredVoucherDetails) { detail ->
                ExpandableVoucherItem(detail)
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun AttractiveSummaryCards(voucherCount: Int, totalWeight: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SummaryCard(
            icon = Icons.Default.Description,
            title = "تعداد حواله‌ها",
            value = formatNumber(voucherCount),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        SummaryCard(
            icon = Icons.Default.Scale,
            title = "وزن خالص کل",
            value = formatNumber(totalWeight.toInt()),
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SummaryCard(
    icon: ImageVector,
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = color
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Composable
fun ExpandableVoucherItem(detail: VoucherDetail) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { isExpanded = !isExpanded }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "شماره حواله: ${detail.trackingNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                VoucherDetailContent(detail)
            }
        }
    }
}

@Composable
fun VoucherDetailContent(detail: VoucherDetail) {
    Column {
        VoucherDetailRow("ساعت ورود", detail.entryTime)
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        VoucherDetailRow("ساعت خروج", detail.exitTime)
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        VoucherDetailRow("تاریخ خروج", detail.exitDate)
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        VoucherDetailRow("وزن خالص", "${formatNumber(detail.netWeight.toInt())} کیلوگرم")
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        VoucherDetailRow("شماره قبض باسکول", detail.scaleReceiptNumber)
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        VoucherDetailRow("ثبت کننده", detail.username ?: "")
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        VoucherDetailRow("تائیدکننده", detail.confirmUsername ?: "")
    }
}

@Composable
fun VoucherDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun QuotaSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("جستجو در حواله‌ها...") },
        singleLine = true,
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
    )
}

@Composable
fun QuotaDetails(
    quotaNumber: String,
    viewModel: ReportsViewModel
) {
    val quotaDetails by viewModel.selectedQuotaDetails.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(quotaNumber) {
        viewModel.loadQuotaDetails(quotaNumber)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        when (uiState) {
            is ReportsViewModel.UiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            is ReportsViewModel.UiState.Error -> {
                Text(
                    text = (uiState as ReportsViewModel.UiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            is ReportsViewModel.UiState.Success -> {
                quotaDetails?.let { details ->
                    QuotaMainCard(details)
                    Spacer(modifier = Modifier.height(16.dp))
                    QuotaInfoCards(details)
                    Spacer(modifier = Modifier.height(16.dp))
                    QuotaProgressBar(details)
                    Spacer(modifier = Modifier.height(16.dp))
                    QuotaAdditionalInfo(details)
                } ?: run {
                    Text("اطلاعات کوتاژ در دسترس نیست")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotasDialog(
    shipName: String,
    quotas: List<Quota>,
    onDismiss: () -> Unit,
    onEdit: (String, QuotaEditData) -> Unit,
    onToggleStatus: (String) -> Unit,
    onDelete: (Quota) -> Unit,
    viewModel: ReportsViewModel
) {
    val sheetState = rememberModalBottomSheetState()
    var searchQuery by remember { mutableStateOf("") }
    val isDarkTheme = isSystemInDarkTheme()

    val sortedQuotas = quotas.sortedWith(
        compareBy<Quota> { !it.isActive }
            .thenBy { it.remainingTonnage }
    )

    val groupedAndSortedQuotas = sortedQuotas
        .groupBy { it.shippingCompany }
        .toSortedMap()

    val filteredQuotas = remember(searchQuery, groupedAndSortedQuotas) {
        if (searchQuery.isEmpty()) {
            groupedAndSortedQuotas
        } else {
            groupedAndSortedQuotas.mapValues { (_, quotas) ->
                quotas.filter { it.number.contains(searchQuery, ignoreCase = true) }
            }.filter { it.value.isNotEmpty() }
        }
    }

    val colorSelector = remember { ColorSelector(cardColors) }
    val colorMap = remember(groupedAndSortedQuotas) {
        colorSelector.reset()
        groupedAndSortedQuotas.keys.associateWith {
            adjustColorForTheme(colorSelector.getNextColor(), isDarkTheme).copy(alpha = 0.6f)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        windowInsets = WindowInsets(0)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "کوتاژهای کشتی $shipName",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("جستجوی کوتاژ") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))


                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredQuotas.forEach { (company, companyQuotas) ->
                            item {
                                Text(
                                    text = company,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colorMap[company] ?: MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(companyQuotas) { quota ->
                                QuotaCard(
                                    quota = quota,
                                    onEdit = onEdit,
                                    onToggleStatus = onToggleStatus,
                                    onDelete = { onDelete(it) },
                                    onPercentageChange = { percentageData ->
                                        viewModel.updateQuotaPercentage(percentageData)
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .height(16.dp))
            }
        }
    }
}

@Composable
fun QuotaCard(
    quota: Quota,
    onEdit: (String, QuotaEditData) -> Unit,
    onToggleStatus: (String) -> Unit,
    onDelete: (Quota) -> Unit,
    onPercentageChange: (QuotaPercentageData) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showToggleDialog by remember { mutableStateOf(false) }
    var showPercentageDialog by remember { mutableStateOf(false) }
    val cardColor = if (quota.isActive) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
    }
    val contentColor = if (quota.isActive) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }
    val accentColor = if (quota.isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(
            width = 1.dp,
            color = if (quota.isActive) {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = accentColor.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "کوتاژ ${quota.number}",
                            style = MaterialTheme.typography.titleMedium,
                            color = contentColor,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            quota.cargoType?.let { type ->
                                Text(
                                    text = type,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = contentColor.copy(alpha = 0.7f)
                                )
                            }
                            Text(
                                text = "| ${formatWeightWithDetail(quota.remainingTonnage)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (quota.isActive) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        } else {
                            MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        },
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (quota.isActive) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                            }
                        )
                    ) {
                        Text(
                            text = if (quota.isActive) "فعال" else "غیرفعال",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (quota.isActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "بستن" else "بازکردن",
                        tint = contentColor
                    )
                }
            }

            // Expanded Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Progress Section
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val progress = calculateProgress(quota.loadedTonnage, quota.totalTonnage)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "پیشرفت بارگیری",
                                style = MaterialTheme.typography.bodyMedium,
                                color = contentColor
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = accentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = accentColor,
                            trackColor = accentColor.copy(alpha = 0.1f)
                        )
                    }

                    // Stats Section in one row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem("تناژ کل", "${formatNumber(quota.totalTonnage.toInt())} تن", accentColor)
                        StatItem("بارگیری شده", "${formatNumber(quota.loadedTonnage.toInt())} تن", accentColor)
                        StatItem("تعداد حواله", formatNumber(quota.voucherCount), accentColor)
                    }

                    // Actions Section
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = contentColor.copy(alpha = 0.1f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ActionButton(
                            icon = Icons.Default.Edit,
                            label = "ویرایش",
                            color = MaterialTheme.colorScheme.primary,
                            onClick = { showEditDialog = true }
                        )
                        ActionButton(
                            icon = Icons.Default.AddTask,
                            label = "درصد",
                            color = MaterialTheme.colorScheme.secondary,
                            onClick = { showPercentageDialog = true }
                        )
                        ActionButton(
                            icon = if (quota.isActive) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                            label = if (quota.isActive) "غیرفعال‌سازی" else "فعال‌سازی",
                            color = if (quota.isActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            onClick = { showToggleDialog = true }
                        )
                        ActionButton(
                            icon = Icons.Default.Delete,
                            label = "حذف",
                            color = MaterialTheme.colorScheme.error,
                            onClick = { showDeleteDialog = true }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        DeleteQuotaDialog(
            quotaNumber = quota.number,
            onConfirm = {
                onDelete(quota)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showPercentageDialog) {
        QuotaPercentageDialog(
            quota = quota,
            onDismiss = { showPercentageDialog = false },
            onConfirm = onPercentageChange
        )
    }

    if (showToggleDialog) {
        ToggleQuotaStatusDialog(
            quotaNumber = quota.number,
            isActive = quota.isActive,
            onConfirm = {
                onToggleStatus(quota.number)
                showToggleDialog = false
            },
            onDismiss = { showToggleDialog = false }
        )
    }

    if (showEditDialog) {
        EditQuotaDialog(
            quotaData = QuotaEditData(
                quotaNumber = quota.number,
                shipName = quota.shipName ?: "",
                shippingCompany = quota.shippingCompany,
                warehouse = quota.warehouse ?: "",
                cargoType = quota.cargoType ?: "",
                totalTonnage = quota.totalTonnage
            ),
            onConfirm = { editedData ->
                onEdit(quota.number, editedData)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = color.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DeleteQuotaDialog(
    quotaNumber: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "تأیید حذف کوتاژ",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "آیا از حذف کوتاژ شماره $quotaNumber و تمام حواله‌های مرتبط با آن اطمینان دارید؟",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
                    ) {
                        Button(
                            onClick = onConfirm,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("حذف", color = MaterialTheme.colorScheme.onError)
                        }
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("انصراف", color = MaterialTheme.colorScheme.onSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToggleQuotaStatusDialog(
    quotaNumber: String,
    isActive: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "تغییر وضعیت کوتاژ",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "آیا از ${if (isActive) "غیرفعال" else "فعال"} کردن کوتاژ شماره $quotaNumber اطمینان دارید؟",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
                    ) {
                        Button(
                            onClick = onConfirm,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("تأیید", color = MaterialTheme.colorScheme.onPrimary)
                        }
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("انصراف", color = MaterialTheme.colorScheme.onSecondary)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditQuotaDialog(
    quotaData: QuotaEditData,
    onConfirm: (QuotaEditData) -> Unit,
    onDismiss: () -> Unit
) {
    var editedData by remember { mutableStateOf(quotaData) }
    var expanded by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    val cargoTypes = listOf("سویا", "دانه روغنی", "ذرت", "گندم", "جو")

    fun isValidQuotaNumber(number: String): Boolean {
        return number.all { it.isDigit() } && number.length in 5..10
    }

    fun formatNumber(number: String): String {
        return number.map { char ->
            when (char) {
                '۰' -> '0'
                '۱' -> '1'
                '۲' -> '2'
                '۳' -> '3'
                '۴' -> '4'
                '۵' -> '5'
                '۶' -> '6'
                '۷' -> '7'
                '۸' -> '8'
                '۹' -> '9'
                else -> char
            }
        }.filter { it.isDigit() || it.isLetter() }.joinToString("")
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "ویرایش اطلاعات کوتاژ",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = editedData.quotaNumber,
                    onValueChange = {
                        editedData = editedData.copy(quotaNumber = formatNumber(it))
                    },
                    label = { Text("شماره کوتاژ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = !isValidQuotaNumber(editedData.quotaNumber),
                    modifier = Modifier.fillMaxWidth()
                )
                if (!isValidQuotaNumber(editedData.quotaNumber)) {
                    Text(
                        "شماره کوتاژ باید حداقل 5 رقم باشد",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editedData.shipName,
                    onValueChange = {
                        editedData = editedData.copy(shipName = formatNumber(it).uppercase(Locale.ROOT))
                    },
                    label = { Text("نام کشتی") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editedData.shippingCompany,
                    onValueChange = {
                        editedData = editedData.copy(shippingCompany = formatNumber(it))
                    },
                    label = { Text("شرکت باربری") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editedData.warehouse,
                    onValueChange = {
                        editedData = editedData.copy(warehouse = formatNumber(it))
                    },
                    label = { Text("انبار") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = editedData.cargoType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("نوع کالا") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
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
                                    editedData = editedData.copy(cargoType = type)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = formatNumber(editedData.totalTonnage.toInt().toString()),
                    onValueChange = {
                        val newValue = formatNumber(it).toIntOrNull() ?: editedData.totalTonnage.toInt()
                        editedData = editedData.copy(totalTonnage = newValue.toFloat())
                    },
                    label = { Text("تناژ کل") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
                ) {
                    Button(
                        onClick = {
                            // نمایش دیالوگ تأییدیه
                            if (isValidQuotaNumber(editedData.quotaNumber)) {
                                showConfirmationDialog = true
                            }
                        },
                        enabled = editedData != quotaData && isValidQuotaNumber(editedData.quotaNumber),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("ذخیره", color = MaterialTheme.colorScheme.onPrimary)
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("انصراف", color = MaterialTheme.colorScheme.onSecondary)
                    }
                }
            }
        }
    }

    // دیالوگ تأییدیه برای ذخیره‌سازی
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm(editedData)
                    showConfirmationDialog = false
                }) {
                    Text("بله")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text("خیر")
                }
            },
            title = { Text("تأیید ذخیره‌سازی") },
            text = { Text("آیا مطمئن هستید که می‌خواهید اطلاعات را ذخیره کنید؟") }
        )
    }
}

@Composable
fun QuotaMainCard(details: QuotaDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "کوتاژ ${details.number}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun QuotaInfoCards(details: QuotaDetails) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        InfoCard(
            title = "تناژ کل",
            value = formatNumber(details.totalTonnage.toTon()),
            description = "تن",
            icon = Icons.Default.Scale,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        InfoCard(
            title = "تناژ بارگیری شده",
            value = formatNumber(details.loadedTonnage.toTon()),
            description = "تن",
            icon = Icons.Default.LocalShipping,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        InfoCard(
            title = "تناژ مانده",
            value = formatNumber(details.remainingTonnage.toTon()),
            description = "تن",
            icon = Icons.Default.PendingActions,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        InfoCard(
            title = "تعداد حواله",
            value = formatNumber(details.voucherCount),
            description = "حواله",
            icon = Icons.Default.Receipt,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuotaProgressBar(details: QuotaDetails) {
    ProgressBar(
        title = "درصد بارگیری",
        progress = calculateProgress(details.loadedTonnage, details.totalTonnage),
        value = calculatePercentage(details.loadedTonnage, details.totalTonnage),
        color = MaterialTheme.colorScheme.primary,
        suffix = "%"
    )
}

@Composable
fun QuotaAdditionalInfo(details: QuotaDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("تاریخ شروع: ${details.startDate}", style = MaterialTheme.typography.bodyMedium)
            Text("تاریخ پایان: ${details.endDate}", style = MaterialTheme.typography.bodyMedium)
            details.additionalInfo?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text("اطلاعات اضافی: $it", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealTimeLoadingBottomSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    loadingData: List<RealTimeLoadingData>,
    shiftInfo: ShiftInfo,
    onRefresh: () -> Unit,
    viewModel: ReportsViewModel
) {
    val shipColorMap by viewModel.shipColorMap.collectAsState()
    val quotaColorMap by viewModel.quotaColorMap.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()
    val defaultColor = MaterialTheme.colorScheme.primary
    var remainingSeconds by remember { mutableIntStateOf(30) }
    var lastUpdateTime by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }
    var expandedShip by remember { mutableStateOf<String?>(null) }
    var showWeightDetailsDialog by remember { mutableStateOf(false) }
    val totalEntryVouchers = remember(loadingData) { loadingData.sumOf { it.entryVouchers } }
    val totalExitVouchers = remember(loadingData) { loadingData.sumOf { it.exitVouchers } }
    val totalNetWeight = remember(loadingData) { loadingData.sumOf { it.totalNetWeight.toDouble() }.toFloat() }
    var showAnalyticsDialog by remember { mutableStateOf(false) }
    val averageWeight = remember(loadingData, totalExitVouchers) {
        if (totalExitVouchers > 0) totalNetWeight / totalExitVouchers else 0f
    }

    LaunchedEffect(isOpen) {
        if (isOpen) {
            viewModel.loadRealTimeData(isDarkTheme, defaultColor)
            lastUpdateTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            while (true) {
                delay(1000)
                remainingSeconds--
                if (remainingSeconds <= 0) {
                    isRefreshing = true
                    viewModel.loadRealTimeData(isDarkTheme, defaultColor)
                    onRefresh()
                    remainingSeconds = 30
                    lastUpdateTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                    delay(500)
                    isRefreshing = false
                }
            }
        }
    }

    if (isOpen) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(),
            windowInsets = WindowInsets(0)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        DialogHeader(
                            activeLoadingsCount = loadingData.size,
                            remainingSeconds = remainingSeconds,
                            lastUpdateTime = lastUpdateTime,
                            shiftInfo = shiftInfo,
                            onShowActiveQuotas = {
                                showAnalyticsDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        StatisticItem(
                            totalVouchers = totalEntryVouchers + totalExitVouchers,
                            totalEntryVouchers = totalEntryVouchers,
                            totalExitVouchers = totalExitVouchers,
                            totalNetWeight = totalNetWeight,
                            averageWeight = averageWeight,
                            onWeightDetailsClick = { showWeightDetailsDialog = true }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        AnimatedContent(
                            targetState = loadingData,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(durationMillis = 300)) togetherWith
                                        fadeOut(animationSpec = tween(durationMillis = 300))
                            },
                            modifier = Modifier.weight(1f),
                            label = "LoadingDataContent"
                        ) { targetLoadingData ->
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(targetLoadingData.groupBy { it.shipName }.toList(), key = { it.first }) { (shipName, shipData) ->
                                    val shipColor = shipColorMap[shipName] ?: MaterialTheme.colorScheme.primary
                                    ShipCard(
                                        shipName = shipName,
                                        isExpanded = expandedShip == shipName,
                                        onExpandToggle = {
                                            expandedShip = if (expandedShip == shipName) null else shipName
                                        },
                                        entryVouchers = shipData.sumOf { it.entryVouchers },
                                        exitVouchers = shipData.sumOf { it.exitVouchers },
                                        color = shipColor,
                                        content = {
                                            ShipContent(
                                                quotas = shipData.sortedWith(
                                                    compareBy<RealTimeLoadingData> { it.loadingWarehouse }
                                                        .thenBy { it.shippingCompany }
                                                        .thenByDescending { it.entryVouchers }
                                                ),
                                                quotaColorMap = quotaColorMap
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .height(16.dp))
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = isRefreshing,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    RefreshOverlay(
                        isRefreshing = isRefreshing,
                        remainingSeconds = remainingSeconds
                    )
                }
            }
        }
    }

    if (showAnalyticsDialog) {
        LoadingAnalyticsDialog(
            viewModel = viewModel,
            loadingData = loadingData,
            onDismiss = { showAnalyticsDialog = false }
        )
    }

    if (showWeightDetailsDialog) {
        WeightDetailsDialog(
            weightDetails = viewModel.calculateWeightDetails(loadingData),
            onDismiss = { showWeightDetailsDialog = false }
        )
    }
}

@Composable
fun RefreshOverlay(
    isRefreshing: Boolean,
    remainingSeconds: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    val rippleEffect by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        // Ripple effect
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.Center)
                .scale(1f + rippleEffect * 0.2f)
                .alpha(1f - rippleEffect)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
        )

        // Rotating refresh icon
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refreshing",
            modifier = Modifier
                .size(50.dp)
                .rotate(rotationAngle)
                .align(Alignment.Center),
            tint = Color.White
        )

        // Countdown text
        Text(
            text = if (isRefreshing) "بروزرسانی..." else "$remainingSeconds",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
fun ShipCard(
    shipName: String,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    entryVouchers: Int,
    exitVouchers: Int,
    color: Color,
    content: @Composable () -> Unit
) {
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "Expand Icon Rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onExpandToggle),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBoat,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = shipName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ورودی: $entryVouchers",
                        style = MaterialTheme.typography.bodySmall,
                        color = color
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "خروجی: $exitVouchers",
                        style = MaterialTheme.typography.bodySmall,
                        color = color
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        modifier = Modifier.rotate(rotationState),
                        tint = color
                    )
                }
            }
            // Use fully qualified name here
            AnimatedVisibility(visible = isExpanded) {
                content()
            }
        }
    }
}

@Composable
fun ShipContent(
    quotas: List<RealTimeLoadingData>,
    quotaColorMap: Map<String, Color>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        quotas.forEach { quota ->
            key(quota.loadingQuotaNumber) {
                RealTimeLoadingCard(
                    data = quota,
                    color = quotaColorMap[quota.loadingQuotaNumber] ?: Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun DialogHeader(
    activeLoadingsCount: Int,
    remainingSeconds: Int,
    lastUpdateTime: String,
    shiftInfo: ShiftInfo,
    onShowActiveQuotas: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "بارگیری لحظه‌ای",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.width(8.dp))
                AnimatedCountdown(seconds = remainingSeconds)
            }

            Button(
                onClick = onShowActiveQuotas,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("کوتاژ‌های فعال: $activeLoadingsCount")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (shiftInfo.type.contains("روز"))
                        Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = null,
                    tint = if (shiftInfo.type.contains("روز"))
                        Color(0xFFFFB74D) else Color(0xFF5C6BC0)
                )
                Text(
                    "شیفت فعلی: ${shiftInfo.type}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                "آخرین بروزرسانی: $lastUpdateTime",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun RealTimeLoadingCard(
    data: RealTimeLoadingData,
    color: Color
) {
    var expandedInfo by remember { mutableStateOf(false) }
    val totalVouchers = data.entryVouchers + data.exitVouchers
    val rotationState by animateFloatAsState(
        targetValue = if (expandedInfo) 180f else 0f,
        label = ""
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .clickable { expandedInfo = !expandedInfo }
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quota Info با آیکن
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // آیکن دایره‌ای
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(color.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // اطلاعات اصلی
                    Column {
                        Text(
                            text = data.loadingQuotaNumber,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = data.shippingCompany,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                // نمایشگر آمار به صورت Badge
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge برای ورودی
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = color.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${data.entryVouchers}",
                                style = MaterialTheme.typography.labelMedium,
                                color = color
                            )
                        }
                    }

                    // Badge برای خروجی
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = color.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${data.exitVouchers}",
                                style = MaterialTheme.typography.labelMedium,
                                color = color
                            )
                        }
                    }

                    // آیکن Expand
                    IconButton(
                        onClick = { expandedInfo = !expandedInfo },
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(rotationState)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = if (expandedInfo) "بستن" else "باز کردن",
                            tint = color
                        )
                    }
                }
            }

            // Expanded Content
            AnimatedVisibility(
                visible = expandedInfo,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                ) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = color.copy(alpha = 0.1f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // انبار
                        InfoColumn(
                            icon = Icons.Default.Warehouse,
                            label = "انبار",
                            value = data.loadingWarehouse,
                            color = color
                        )

                        // وزن خالص
                        InfoColumn(
                            icon = Icons.Default.Scale,
                            label = "وزن خالص",
                            value = "${formatNumber(data.totalNetWeight)} کیلو",
                            color = color
                        )

                        // تعداد کل
                        InfoColumn(
                            icon = Icons.Default.Inventory,  // یا هر آیکن مناسب دیگر
                            label = "تعداد کل",
                            value = formatNumber(totalVouchers),
                            color = color
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoColumn(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // آیکن و برچسب
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        // مقدار
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun LoadingAnalyticsDialog(
    viewModel: ReportsViewModel,
    loadingData: List<RealTimeLoadingData>,
    onDismiss: () -> Unit
) {
    // به‌روزرسانی آنالیتیکس زمانی که دیالوگ باز می‌شود
    LaunchedEffect(loadingData) {
        viewModel.updateAnalytics(loadingData)
    }

    // جمع‌آوری وضعیت‌های تحلیل
    val voucherAnalytics by viewModel.voucherAnalytics.collectAsState()
    val warehouseAnalytics by viewModel.warehouseAnalytics.collectAsState()
    val shipAnalytics by viewModel.shipAnalytics.collectAsState()
    val companyAnalytics by viewModel.companyAnalytics.collectAsState()
    val quotaAnalytics by viewModel.quotaAnalytics.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            var selectedTab by remember { mutableStateOf(AnalyticsTab.VOUCHERS) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تحلیل عملیات بارگیری",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tabs
                TabRow(
                    selectedTab = selectedTab,
                    onTabSelect = { selectedTab = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Content
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        AnalyticsTab.VOUCHERS -> voucherAnalytics?.let {
                            VouchersAnalyticsContent(
                                analytics = it,
                                viewModel = viewModel
                            )
                        }
                        AnalyticsTab.WAREHOUSES -> WarehousesAnalyticsContent(
                            analytics = warehouseAnalytics,
                            viewModel = viewModel
                        )
                        AnalyticsTab.SHIPS -> ShipsAnalyticsContent(
                            analytics = shipAnalytics,
                            viewModel = viewModel
                        )
                        AnalyticsTab.COMPANIES -> CompaniesAnalyticsContent(
                            analytics = companyAnalytics,
                            viewModel = viewModel
                        )
                        AnalyticsTab.QUOTAS -> QuotasAnalyticsContent(
                            analytics = quotaAnalytics,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabRow(
    selectedTab: AnalyticsTab,
    onTabSelect: (AnalyticsTab) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        LazyRow(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = {
                items(AnalyticsTab.entries.toList()) { tab ->
                    AnalyticTabButton(
                        selected = selectedTab == tab,
                        onClick = { onTabSelect(tab) },
                        tabInfo = getTabInfo(tab)
                    )
                }
            }
        )
    }
}

@Composable
private fun AnalyticTabButton(
    selected: Boolean,
    onClick: () -> Unit,
    tabInfo: TabInfo
) {
    Surface(
        modifier = Modifier
            .wrapContentWidth()
            .height(40.dp),
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = tabInfo.icon,
                contentDescription = null,
                tint = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                text = tabInfo.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

enum class AnalyticsTab {
    VOUCHERS, WAREHOUSES, SHIPS, COMPANIES, QUOTAS
}

private fun getTabInfo(tab: AnalyticsTab) = when(tab) {
    AnalyticsTab.VOUCHERS -> TabInfo("حواله‌ها", Icons.Default.Receipt)
    AnalyticsTab.SHIPS -> TabInfo("کشتی‌ها", Icons.Default.DirectionsBoat)
    AnalyticsTab.COMPANIES -> TabInfo("باربری‌ها", Icons.Default.Business)
    AnalyticsTab.WAREHOUSES -> TabInfo("انبارها", Icons.Default.Warehouse)
    AnalyticsTab.QUOTAS -> TabInfo("کوتاژها", Icons.Default.Description)
}

@Composable
private fun VouchersAnalyticsContent(
    analytics: VoucherAnalytics,
    viewModel: ReportsViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            // Total Row - Spans full width
            item(span = { GridItemSpan(2) }) {
                BigStatCard(
                    title = "کل حواله‌ها",
                    value = viewModel.formatNumber(analytics.totalEntryVouchers + analytics.totalExitVouchers),
                    icon = Icons.Default.Inventory,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Entry Vouchers
            item {
                DetailStatCard(
                    title = "حواله‌های ورودی",
                    mainValue = viewModel.formatNumber(analytics.totalEntryVouchers),
                    icon = Icons.Default.ArrowDownward,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Exit Vouchers
            item {
                DetailStatCard(
                    title = "حواله‌های خروجی",
                    mainValue = viewModel.formatNumber(analytics.totalExitVouchers),
                    icon = Icons.Default.ArrowUpward,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            // Exit Percentage
            item {
                DetailStatCard(
                    title = "درصد خروج از کل",
                    mainValue = viewModel.formatPercentage(analytics.exitPercentage),
                    icon = Icons.Default.PieChart,
                    color = MaterialTheme.colorScheme.secondary,
                    secondaryValue = "${viewModel.formatNumber(analytics.totalExitVouchers)} از ${viewModel.formatNumber(analytics.totalEntryVouchers + analytics.totalExitVouchers)}"
                )
            }

            // Average Weight
            item {
                DetailStatCard(
                    title = "میانگین وزن خروج",
                    mainValue = viewModel.formatWeight(analytics.averageExitWeight),
                    icon = Icons.Default.Scale,
                    color = MaterialTheme.colorScheme.error,
                    secondaryValue = "به ازای هر حواله"
                )
            }
        }
    }
}

@Composable
private fun BigStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Title and Icon
            Row(
                modifier = Modifier.align(Alignment.TopStart),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Value
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
private fun DetailStatCard(
    title: String,
    mainValue: String,
    icon: ImageVector,
    color: Color,
    secondaryValue: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Main Value
            Text(
                text = mainValue,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )

            // Secondary Value (if exists)
            secondaryValue?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun WarehousesAnalyticsContent(
    analytics: List<WarehouseAnalytics>,
    viewModel: ReportsViewModel
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(analytics) { warehouse ->
            WarehouseCard(
                warehouse = warehouse,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun WarehouseCard(
    warehouse: WarehouseAnalytics,
    viewModel: ReportsViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RankBadge(rank = warehouse.rank)
                    Column {
                        Text(
                            text = warehouse.warehouseName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "عملکرد: ${viewModel.formatPercentage(warehouse.operationPercentage)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { warehouse.operationPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WarehouseStatItem(
                    label = "وزن کل",
                    value = viewModel.formatWeight(warehouse.totalWeight),
                    icon = Icons.Default.Scale
                )
                WarehouseStatItem(
                    label = "میانگین وزن",
                    value = viewModel.formatWeight(warehouse.averageWeight),
                    icon = Icons.Default.Analytics
                )
            }
        }
    }
}

@Composable
private fun RankBadge(rank: Int) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                color = when (rank) {
                    1 -> Color(0xFFFFD700) // Gold
                    2 -> Color(0xFFC0C0C0) // Silver
                    3 -> Color(0xFFCD7F32) // Bronze
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = rank.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (rank <= 3) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WarehouseStatItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
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
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ShipsAnalyticsContent(
    analytics: List<ShipAnalytics>,
    viewModel: ReportsViewModel
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(analytics) { ship ->
            ShipProgressCard(
                ship = ship,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun ShipProgressCard(
    ship: ShipAnalytics,
    viewModel: ReportsViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    val backgroundColor = MaterialTheme.colorScheme.surface
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = true),
                        onClick = { expanded = !expanded }
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = ship.shipName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${ship.warehouseCount} انبار فعال",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "بستن" else "بازکردن",
                    modifier = Modifier.graphicsLayer {
                        rotationZ = if (expanded) 180f else 0f
                    }
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ShipExpandedContent(ship = ship, viewModel = viewModel)
            }

            Spacer(modifier = Modifier.height(12.dp))
            ProgressSection(
                loaded = ship.totalExitVouchers.toFloat(),
                total = ship.totalVouchers.toFloat()
            )
        }
    }
}

@Composable
private fun ShipExpandedContent(
    ship: ShipAnalytics,
    viewModel: ReportsViewModel
) {
    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
    ) {
        // آمار حواله‌ها
        StatisticsRow(ship = ship, viewModel = viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        // اطلاعات وزن
        WeightInfoRow(ship = ship, viewModel = viewModel)

        if (ship.warehouses.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ship.warehouses) { warehouse ->
                    WarehouseChip(name = warehouse)
                }
            }
        }
    }
}

@Composable
private fun StatisticsRow(
    ship: ShipAnalytics,
    viewModel: ReportsViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ShipStatBoxs(
            value = viewModel.formatNumber(ship.totalEntryVouchers),
            label = "ورودی",
            icon = Icons.Default.ArrowDownward,
            tint = MaterialTheme.colorScheme.primary
        )
        ShipStatBoxs(
            value = viewModel.formatNumber(ship.totalExitVouchers),
            label = "خروجی",
            icon = Icons.Default.ArrowUpward,
            tint = MaterialTheme.colorScheme.secondary
        )
        ShipStatBoxs(
            value = viewModel.formatPercentage(ship.exitRatio),
            label = "درصد خروج",
            icon = Icons.Default.PieChart,
            tint = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun WeightInfoRow(
    ship: ShipAnalytics,
    viewModel: ReportsViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = viewModel.formatWeight(ship.totalNetWeight),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "وزن کل",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = viewModel.formatWeight(ship.averageWeight),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "میانگین وزن",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ShipStatBoxs(
    value: String,
    label: String,
    icon: ImageVector,
    tint: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = tint
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun WarehouseChip(name: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warehouse,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ProgressSection(loaded: Float, total: Float) {
    val progress = if (total > 0f) loaded / total else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "Progress Animation"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "پیشرفت بارگیری",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    }
}

@Composable
private fun CompaniesAnalyticsContent(
    analytics: List<ShippingCompanyAnalytics>,
    viewModel: ReportsViewModel
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(analytics) { company ->
            CompanyCard(
                company = company,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun CompanyCard(
    company: ShippingCompanyAnalytics,
    viewModel: ReportsViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
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
                    text = company.companyName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "سهم عملیات: ${viewModel.formatPercentage(company.operationPercentage)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CompanyStatItem(
                    label = "تعداد حواله",
                    value = viewModel.formatNumber(company.totalVouchers),
                    icon = Icons.Default.Receipt
                )
                CompanyStatItem(
                    label = "وزن کل",
                    value = viewModel.formatWeight(company.totalWeight),
                    icon = Icons.Default.Scale
                )
            }

            LinearProgressIndicator(
                progress = { company.operationPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
private fun CompanyStatItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
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
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QuotasAnalyticsContent(
    analytics: List<QuotaAnalytics>,
    viewModel: ReportsViewModel
) {
    var expandedQuotaId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(analytics) { quota ->
                QuotaAnalyticsCard(
                    quota = quota,
                    isExpanded = expandedQuotaId == quota.quotaNumber,
                    onExpandToggle = {
                        expandedQuotaId = if (expandedQuotaId == quota.quotaNumber) null else quota.quotaNumber
                    },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun QuotaAnalyticsCard(
    quota: QuotaAnalytics,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    viewModel: ReportsViewModel
) {
    val operationProgress = remember(quota) {
        if (quota.totalVouchers > 0) {
            (quota.exitVouchers.toFloat() / quota.totalVouchers * 100).coerceIn(0f, 100f)
        } else 0f
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandToggle() }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Section - Always visible
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "کوتاژ ${quota.quotaNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${operationProgress.toInt()}%",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // شرکت باربری
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = quota.shippingCompany,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                        CircleShape
                                    )
                            )

                            // نام انبار
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warehouse,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = quota.warehouseName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "بستن" else "باز کردن",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Progress Bar - Always visible
            LinearProgressIndicator(
                progress = { operationProgress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )

            // Expanded Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // آمار حواله‌ها
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        VoucherStatBox(
                            title = "ورودی",
                            value = quota.entryVouchers,
                            icon = Icons.Default.ArrowDownward,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        VoucherStatBox(
                            title = "خروجی",
                            value = quota.exitVouchers,
                            icon = Icons.Default.ArrowUpward,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        VoucherStatBox(
                            title = "کل",
                            value = quota.totalVouchers,
                            icon = Icons.Default.Inventory,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // آمار وزن
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WeightStatBox(
                            title = "وزن بارگیری شده",
                            value = viewModel.formatWeight(quota.totalWeight),
                            icon = Icons.Default.Scale,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        WeightStatBox(
                            title = "میانگین وزن در ساعت",
                            value = viewModel.formatWeight(quota.weightPerHour),
                            icon = Icons.Default.Schedule,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WeightStatBox(
                            title = "میانگین وزن هر حواله",
                            value = viewModel.formatWeight(quota.averageWeight),
                            icon = Icons.Default.Analytics,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                        WeightStatBox(
                            title = "درصد تکمیل",
                            value = "${quota.completionRate.toInt()}%",
                            icon = Icons.Default.PieChart,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // کارایی عملیات
                    EfficiencySection(efficiency = quota.operationEfficiency)
                }
            }
        }
    }
}

@Composable
private fun VoucherStatBox(
    title: String,
    value: Int,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun WeightStatBox(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun EfficiencySection(efficiency: Float) {
    val (color, icon, description) = when {
        efficiency >= 90 -> Triple(
            MaterialTheme.colorScheme.primary,
            Icons.Default.Stars,
            "عملکرد عالی"
        )
        efficiency >= 70 -> Triple(
            MaterialTheme.colorScheme.secondary,
            Icons.Default.Star,
            "عملکرد خوب"
        )
        efficiency >= 50 -> Triple(
            MaterialTheme.colorScheme.tertiary,
            Icons.AutoMirrored.Filled.StarHalf,
            "عملکرد متوسط"
        )
        else -> Triple(
            MaterialTheme.colorScheme.error,
            Icons.Default.StarBorder,
            "نیاز به بهبود"
        )
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "کارایی عملیات",
                    style = MaterialTheme.typography.titleMedium,
                    color = color
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color.copy(alpha = 0.7f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "${efficiency.toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WeightDetailsDialog(
    weightDetails: Map<String, Map<String, Float>>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                DialogHeader()
                Spacer(modifier = Modifier.height(8.dp))
                WeightDetailsList(weightDetails)
                Spacer(modifier = Modifier.height(8.dp))
                DialogFooter(onDismiss)
            }
        }
    }
}

@Composable
fun DialogHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Scale,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "جزئیات وزن خالص",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun WeightDetailsList(weightDetails: Map<String, Map<String, Float>>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(weightDetails.toList()) { (shipName, warehouses) ->
            ShipWeightCard(shipName, warehouses.mapValues { it.value.roundToInt() })
        }
    }
}

@Composable
fun DialogFooter(onDismiss: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Text("بستن")
        }
    }
}

@Composable
fun ShipWeightCard(shipName: String, warehouses: Map<String, Int>) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "Expand Icon Rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBoat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = shipName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(rotationState),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    warehouses.forEach { (warehouseName, weight) ->
                        WarehouseWeightItem(warehouseName, weight)
                    }
                }
            }
        }
    }
}

@Composable
fun WarehouseWeightItem(warehouseName: String, weight: Int) {
    val animatedWeight = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(weight) {
        animatedWeight.animateTo(
            targetValue = weight.toFloat(),
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Warehouse,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = warehouseName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        AnimatedWeight(weight = animatedWeight.value.roundToInt())
    }
}

@Composable
fun AnimatedWeight(weight: Int) {
    Text(
        text = "${formatNumber(weight)} کیلوگرم",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun StatisticItem(
    totalVouchers: Int,
    totalEntryVouchers: Int,
    totalExitVouchers: Int,
    totalNetWeight: Float,
    averageWeight: Float,
    onWeightDetailsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatChip(
                label = "کل",
                value = totalVouchers,
                color = MaterialTheme.colorScheme.primary
            )
            StatChip(
                label = "ورودی",
                value = totalEntryVouchers,
                color = MaterialTheme.colorScheme.tertiary
            )
            StatChip(
                label = "خروجی",
                value = totalExitVouchers,
                color = MaterialTheme.colorScheme.secondary
            )
            WeightStatChip(
                totalNetWeight = totalNetWeight,
                averageWeight = averageWeight,
                onClick = onWeightDetailsClick
            )
        }
    }
}

@Composable
fun StatChip(
    label: String,
    value: Int,
    color: Color
) {
    var startAnimation by remember { mutableStateOf(false) }
    val animatedValue by animateIntAsState(
        targetValue = if (startAnimation) value else 0,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing), label = ""
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = animatedValue.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun WeightStatChip(
    totalNetWeight: Float,
    averageWeight: Float,
    onClick: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    val animatedTotalWeight by animateFloatAsState(
        targetValue = if (startAnimation) totalNetWeight else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing), label = ""
    )
    val animatedAverageWeight by animateFloatAsState(
        targetValue = if (startAnimation) averageWeight else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing), label = ""
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "کل: ${formatNumber(animatedTotalWeight.toInt())}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = "میانگین: ${formatNumber(animatedAverageWeight.toInt())}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun AnimatedCountdown(seconds: Int, totalSeconds: Int = 30) {
    val animatedProgress by animateFloatAsState(
        targetValue = seconds.toFloat() / totalSeconds.toFloat(),
        animationSpec = tween(durationMillis = 1000), label = ""
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .padding(4.dp)
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.secondary,
            strokeWidth = 2.dp,
        )
        Text(
            text = "$seconds",
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun FloatingActionButton(
    onRealTimeLoadingClick: () -> Unit,
    onSendMessageClick: () -> Unit,
    onAdvancedSearchClick: () -> Unit,
    onAnalyticsClick: () -> Unit
) {
    var expandedFab by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expandedFab) 45f else 0f,
        label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.padding(16.dp)
        ) {
            AnimatedVisibility(
                visible = expandedFab,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    MiniFab(
                        item = FabItem(
                            icon = Icons.Default.Refresh,
                            label = "بارگیری لحظه‌ای",
                            onClick = onRealTimeLoadingClick
                        ),
                        onDismiss = { expandedFab = false }
                    )
                    MiniFab(
                        item = FabItem(
                            icon = Icons.AutoMirrored.Filled.Message,
                            label = "ارسال پیام",
                            onClick = onSendMessageClick
                        ),
                        onDismiss = { expandedFab = false }
                    )
                    MiniFab(
                        item = FabItem(
                            icon = Icons.Default.Search,
                            label = "جستجوی پیشرفته",
                            onClick = onAdvancedSearchClick
                        ),
                        onDismiss = { expandedFab = false }
                    )
                    MiniFab(
                        item = FabItem(
                            icon = Icons.Default.Analytics,
                            label = "آمار جامع",
                            onClick = onAnalyticsClick
                        ),
                        onDismiss = { expandedFab = false }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                onClick = { expandedFab = !expandedFab },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(56.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "منو",
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(rotation)
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniFab(
    item: FabItem,
    onDismiss: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.05f else 1f,
        label = ""
    )

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is HoverInteraction.Enter -> isHovered = true
                is HoverInteraction.Exit -> isHovered = false
            }
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .graphicsLayer {
                clip = false
            }
            .alpha(if (isHovered) 1f else 0.9f)
    ) {
        // Label
        Surface(
            modifier = Modifier.padding(end = 12.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // Mini FAB
        Surface(
            onClick = {
                item.onClick()
                onDismiss()
            },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier
                .size(48.dp)
                .scale(scale)
                .hoverable(interactionSource)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun AdvancedSearchDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit
) {
    var receiptNumber by remember { mutableStateOf("") }
    val mainColor = MaterialTheme.colorScheme.primary

    if (isOpen) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = mainColor
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(mainColor.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = mainColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "جستجوی پیشرفته",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "جستجو بر اساس شماره قبض باسکول",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = mainColor.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "بستن",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Search Input
                    OutlinedTextField(
                        value = receiptNumber,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() }) {
                                receiptNumber = it
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = mainColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = mainColor
                            )
                        },
                        label = {
                            Text(
                                "شماره قبض باسکول",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (receiptNumber.isNotBlank()) {
                                    onSearch(receiptNumber)
                                }
                            }
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (receiptNumber.isNotBlank()) {
                                    onSearch(receiptNumber)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = receiptNumber.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = mainColor,
                                disabledContainerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)
                            )
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("جستجو")
                            }
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f))
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
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
}

@Composable
fun SearchResultDialog(
    cargoInfo: CargoInfo,
    onDismiss: () -> Unit
) {
    val mainColor = MaterialTheme.colorScheme.primary

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .padding(16.dp)
                .animateContentSize(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                width = 1.dp,
                color = mainColor
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(mainColor.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = mainColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "نتیجه جستجو",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "قبض باسکول: ${cargoInfo.scaleReceiptNumber}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(mainColor.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        CargoMainInfo(cargoInfo)
                    }

                    item {
                        CargoWeightInfo(cargoInfo)
                    }

                    item {
                        CargoTimeInfo(cargoInfo)
                    }

                    item {
                        CargoShippingInfo(cargoInfo)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("بستن")
                }
            }
        }
    }
}

@Composable
private fun CargoMainInfo(cargoInfo: CargoInfo) {
    InfoCard(
        mainColor = MaterialTheme.colorScheme.primary,
        title = "اطلاعات اصلی",
        icon = Icons.Default.Description,
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailRowCargo(
                    icon = Icons.Default.Numbers,
                    label = "شماره حواله",
                    value = cargoInfo.trackingNumber
                )
                DetailRowCargo(
                    icon = Icons.Default.Receipt,
                    label = "قبض باسکول",
                    value = cargoInfo.scaleReceiptNumber
                )
                DetailRowCargo(
                    icon = Icons.Default.Newspaper,
                    label = "شماره کوتاژ",
                    value = cargoInfo.loadingQuotaNumber
                )
            }
        }
    )
}

@Composable
private fun CargoWeightInfo(cargoInfo: CargoInfo) {
    InfoCard(
        mainColor = MaterialTheme.colorScheme.secondary,
        title = "اطلاعات وزن",
        icon = Icons.Default.Scale,
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailRowCargo(
                    icon = Icons.Default.Scale,
                    label = "وزن خالص",
                    value = "${formatNumber(cargoInfo.netWeight.toIntOrNull() ?: 0)} کیلوگرم"
                )
                DetailRowCargo(
                    icon = Icons.Default.ArrowDownward,
                    label = "کسری بار",
                    value = "${formatNumber(cargoInfo.shortageWeight.toIntOrNull() ?: 0)} کیلوگرم"
                )
                DetailRowCargo(
                    icon = Icons.Default.ArrowUpward,
                    label = "اضافه بار",
                    value = "${formatNumber(cargoInfo.excessWeight.toIntOrNull() ?: 0)} کیلوگرم"
                )
            }
        }
    )
}

@Composable
private fun CargoTimeInfo(cargoInfo: CargoInfo) {
    InfoCard(
        mainColor = MaterialTheme.colorScheme.tertiary,
        title = "زمان‌بندی",
        icon = Icons.Default.Schedule,
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailRowCargo(
                    icon = Icons.AutoMirrored.Filled.Login,
                    label = "ساعت ورود",
                    value = cargoInfo.entryTime
                )
                DetailRowCargo(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    label = "ساعت خروج",
                    value = cargoInfo.exitTime ?: "-"
                )
                DetailRowCargo(
                    icon = Icons.Default.DateRange,
                    label = "تاریخ خروج",
                    value = cargoInfo.exitDate ?: "-"
                )
            }
        }
    )
}

@Composable
private fun CargoShippingInfo(cargoInfo: CargoInfo) {
    InfoCard(
        mainColor = MaterialTheme.colorScheme.primary,
        title = "اطلاعات تکمیلی",
        icon = Icons.Default.Info,
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailRowCargo(
                    icon = Icons.Default.DirectionsBoat,
                    label = "کشتی",
                    value = cargoInfo.shipName
                )
                DetailRowCargo(
                    icon = Icons.Default.Warehouse,
                    label = "انبار بارگیری",
                    value = cargoInfo.loadingWarehouse
                )
                DetailRowCargo(
                    icon = Icons.Default.Inventory,
                    label = "نوع کالا",
                    value = cargoInfo.cargoType
                )
                DetailRowCargo(
                    icon = Icons.Default.LocalShipping,
                    label = "شرکت بارگیری",
                    value = cargoInfo.shippingCompany
                )
            }
        }
    )
}

@Composable
private fun InfoCard(
    mainColor: Color,
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, mainColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(mainColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = mainColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = mainColor,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
private fun DetailRowCargo(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SendMessageDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSendMessage: (String, String, List<String>) -> Unit,
    messageSendingStatus: ReportsViewModel.MessageSendingStatus
) {
    var messageTitle by remember { mutableStateOf("") }
    var messageBody by remember { mutableStateOf("") }
    var selectedRecipients by remember { mutableStateOf(setOf<String>()) }
    var isVisible by remember { mutableStateOf(false) }
    var isContentVisible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = ""
    )
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 0.4f else 0f,
        animationSpec = tween(300),
        label = ""
    )

    LaunchedEffect(isOpen) {
        if (isOpen) {
            isVisible = true
            delay(150)
            isContentVisible = true
        } else {
            isContentVisible = false
            delay(150)
            isVisible = false
        }
    }

    if (isOpen) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = alpha))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .fillMaxHeight(0.7f)
                        .scale(scale)
                        .align(Alignment.Center)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* Prevent click through */ },
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 8.dp,
                    shadowElevation = 16.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        DialogHeader(
                            isVisible = isContentVisible,
                            onClose = onDismiss
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        MessageInputFields(
                            isVisible = isContentVisible,
                            messageTitle = messageTitle,
                            messageBody = messageBody,
                            onTitleChange = { messageTitle = it },
                            onBodyChange = { messageBody = it }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        RecipientSelector(
                            isVisible = isContentVisible,
                            selectedRecipients = selectedRecipients,
                            onSelectionChanged = { recipient, isSelected ->
                                selectedRecipients = if (isSelected) {
                                    selectedRecipients + recipient
                                } else {
                                    selectedRecipients - recipient
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        StatusIndicator(
                            isVisible = isContentVisible,
                            status = messageSendingStatus
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        ActionButtons(
                            isVisible = isContentVisible,
                            isEnabled = messageTitle.isNotBlank() &&
                                    messageBody.isNotBlank() &&
                                    selectedRecipients.isNotEmpty() &&
                                    messageSendingStatus !is ReportsViewModel.MessageSendingStatus.Sending,
                            onSend = {
                                onSendMessage(
                                    messageTitle,
                                    messageBody,
                                    selectedRecipients.toList()
                                )
                            },
                            onCancel = onDismiss
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogHeader(
    isVisible: Boolean,
    onClose: () -> Unit
) {
    val slideOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else (-50).dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = ""
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        label = ""
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = slideOffset)
            .alpha(alpha),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Message,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "ارسال پیام جدید",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "بستن",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MessageInputFields(
    isVisible: Boolean,
    messageTitle: String,
    messageBody: String,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit
) {
    val offsetY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 50.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = ""
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        label = ""
    )

    val bodyFieldFocusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .offset(y = offsetY)
            .alpha(alpha)
    ) {
        AnimatedMessageTextField(
            value = messageTitle,
            onValueChange = { value ->
                val newValue = value.replace("\n", "")
                onTitleChange(newValue)
            },
            label = "عنوان پیام",
            icon = Icons.Default.Title,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    bodyFieldFocusRequester.requestFocus()
                }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedMessageTextField(
            value = messageBody,
            onValueChange = onBodyChange,
            label = "متن پیام",
            icon = Icons.AutoMirrored.Filled.Message,
            minHeight = 120.dp,
            maxLines = 5,
            modifier = Modifier.focusRequester(bodyFieldFocusRequester),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
            )
        )
    }
}

@Composable
private fun AnimatedMessageTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    minHeight: Dp = 56.dp,
    maxLines: Int = 1,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = if (isFocused)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        label = ""
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isFocused)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        else
            MaterialTheme.colorScheme.surface,
        label = ""
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFocused)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused },
                maxLines = maxLines,
                singleLine = maxLines == 1,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (value.isEmpty()) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

@Composable
private fun RecipientSelector(
    isVisible: Boolean,
    selectedRecipients: Set<String>,
    onSelectionChanged: (String, Boolean) -> Unit
) {
    val recipients = listOf(
        Triple("admin", "مدیر", "👨‍💼"),
        Triple("operator", "باسکولچی", "⚖️"),
        Triple("verifier", "بارشمار", "📋")
    )

    val offsetY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 50.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = ""
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        label = ""
    )

    Column(
        modifier = Modifier
            .offset(y = offsetY)
            .alpha(alpha)
    ) {
        Text(
            text = "گیرندگان پیام",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            horizontalArrangement = Arrangement.Start,
            maxItemsInEachRow = 3
        ) {
            recipients.forEach { (id, label, emoji) ->
                RecipientChip(
                    text = "$emoji $label",
                    isSelected = selectedRecipients.contains(id),
                    onSelect = { onSelectionChanged(id, !selectedRecipients.contains(id)) }
                )
            }
        }
    }
}

@Composable
private fun RecipientChip(
    text: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface,
        label = ""
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        label = ""
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.onPrimaryContainer
        else
            MaterialTheme.colorScheme.onSurface,
        label = ""
    )

    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun StatusIndicator(
    isVisible: Boolean,
    status: ReportsViewModel.MessageSendingStatus
) {
    val offsetY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 50.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = ""
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        label = ""
    )

    val statusColor by animateColorAsState(
        targetValue = when (status) {
            is ReportsViewModel.MessageSendingStatus.Success -> MaterialTheme.colorScheme.tertiary
            is ReportsViewModel.MessageSendingStatus.Error -> MaterialTheme.colorScheme.error
            is ReportsViewModel.MessageSendingStatus.Sending -> MaterialTheme.colorScheme.primary
            else -> Color.Transparent
        },
        label = ""
    )

    AnimatedVisibility(
        visible = status !is ReportsViewModel.MessageSendingStatus.Idle,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = offsetY)
                .alpha(alpha),
            shape = RoundedCornerShape(12.dp),
            color = statusColor.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (status) {
                    is ReportsViewModel.MessageSendingStatus.Sending -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = statusColor,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "در حال ارسال پیام...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = statusColor
                        )
                    }
                    is ReportsViewModel.MessageSendingStatus.Success -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "پیام با موفقیت ارسال شد",
                            style = MaterialTheme.typography.bodyMedium,
                            color = statusColor
                        )
                    }
                    is ReportsViewModel.MessageSendingStatus.Error -> {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = status.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = statusColor
                        )
                    }
                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(
    isVisible: Boolean,
    isEnabled: Boolean,
    onSend: () -> Unit,
    onCancel: () -> Unit
) {
    val offsetY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 50.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = ""
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        label = ""
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = offsetY)
            .alpha(alpha),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // دکمه ارسال
        Button(
            onClick = onSend,
            enabled = isEnabled,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text("ارسال پیام")
            }
        }

        // دکمه لغو
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text("انصراف")
            }
        }
    }
}

@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    maxItemsInEachRow: Int = Int.MAX_VALUE,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val horizontalSpacing = 8.dp.roundToPx()
        val verticalSpacing = 8.dp.roundToPx()
        val rows = mutableListOf<List<Placeable>>()
        var rowPlaceables = mutableListOf<Placeable>()
        var rowWidth = 0
        var totalHeight = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0))

            if (rowPlaceables.size >= maxItemsInEachRow ||
                rowWidth + placeable.width + (if (rowPlaceables.isEmpty()) 0 else horizontalSpacing) > constraints.maxWidth) {
                rows.add(rowPlaceables)
                totalHeight += rowPlaceables.maxOfOrNull { it.height } ?: 0
                if (rows.size > 1) totalHeight += verticalSpacing
                rowPlaceables = mutableListOf(placeable)
                rowWidth = placeable.width
            } else {
                rowWidth += placeable.width + (if (rowPlaceables.isEmpty()) 0 else horizontalSpacing)
                rowPlaceables.add(placeable)
            }
        }

        if (rowPlaceables.isNotEmpty()) {
            rows.add(rowPlaceables)
            totalHeight += rowPlaceables.maxOfOrNull { it.height } ?: 0
            if (rows.size > 1) totalHeight += verticalSpacing
        }

        layout(constraints.maxWidth, totalHeight) {
            var yPosition = 0

            rows.forEach { row ->
                var xPosition = when (horizontalArrangement) {
                    Arrangement.Start -> 0
                    Arrangement.Center -> (constraints.maxWidth - (row.sumOf { it.width } + (row.size - 1) * horizontalSpacing)) / 2
                    Arrangement.End -> constraints.maxWidth - (row.sumOf { it.width } + (row.size - 1) * horizontalSpacing)
                    else -> 0
                }

                row.forEach { placeable ->
                    placeable.place(xPosition, yPosition)
                    xPosition += placeable.width + horizontalSpacing
                }
                yPosition += row.maxOfOrNull { it.height } ?: 0
                if (row != rows.last()) yPosition += verticalSpacing
            }
        }
    }
}

@Composable
fun ComprehensiveAnalyticsDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    viewModel: ReportsViewModel
) {
    var selectedTab by remember { mutableStateOf(AnalyticsTabType.PEAK_HOURS) }
    val analyticsData by viewModel.comprehensiveAnalytics.collectAsState()
    val loadingState by viewModel.analyticsLoadingState.collectAsState()

    LaunchedEffect(isVisible) {
        if (isVisible) {
            viewModel.loadComprehensiveAnalytics()
        }
    }

    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.9f),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    AnalyticsHeader(
                        title = "تحلیل جامع عملیات",
                        onClose = onDismiss
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AnalyticsTabRow(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    when (loadingState) {
                        is ReportsViewModel.LoadingState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        is ReportsViewModel.LoadingState.Error -> {
                            val error = (loadingState as ReportsViewModel.LoadingState.Error).message
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(error)
                            }
                        }
                        is ReportsViewModel.LoadingState.Success -> {
                            SwipeableTabContent(
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it },
                                data = analyticsData,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        ReportsViewModel.LoadingState.Idle -> {
                            LaunchedEffect(Unit) {
                                viewModel.loadComprehensiveAnalytics()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeableTabContent(
    selectedTab: AnalyticsTabType,
    onTabSelected: (AnalyticsTabType) -> Unit,
    data: ComprehensiveAnalytics?,
    modifier: Modifier = Modifier
) {
    val tabs = remember {
        listOf(
            AnalyticsTabType.PEAK_HOURS,
            AnalyticsTabType.QUOTAS,
            AnalyticsTabType.CARRIERS,
            AnalyticsTabType.WAREHOUSES
        )
    }

    var offsetX by remember { mutableFloatStateOf(0f) }
    val draggableState = rememberDraggableState { delta ->
        offsetX += delta
    }

    val currentIndex = tabs.indexOf(selectedTab)
    val animatedOffset by animateFloatAsState(
        targetValue = 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "offset"
    )

    // تشخیص جهت لایوت
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    Box(
        modifier = modifier
            .fillMaxSize()
            .draggable(
                state = draggableState,
                orientation = Orientation.Horizontal,
                onDragStarted = { },
                onDragStopped = {
                    when {
                        // در حالت RTL: حرکت به راست برای تب بعدی
                        isRtl && offsetX > 100f && currentIndex < tabs.lastIndex -> {
                            onTabSelected(tabs[currentIndex + 1])
                        }
                        // در حالت RTL: حرکت به چپ برای تب قبلی
                        isRtl && offsetX < -100f && currentIndex > 0 -> {
                            onTabSelected(tabs[currentIndex - 1])
                        }
                        // در حالت LTR: حرکت به راست برای تب قبلی
                        !isRtl && offsetX > 100f && currentIndex > 0 -> {
                            onTabSelected(tabs[currentIndex - 1])
                        }
                        // در حالت LTR: حرکت به چپ برای تب بعدی
                        !isRtl && offsetX < -100f && currentIndex < tabs.lastIndex -> {
                            onTabSelected(tabs[currentIndex + 1])
                        }
                    }
                    offsetX = 0f
                }
            )
    ) {
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                // تنظیم انیمیشن با توجه به RTL
                if (isRtl) {
                    slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { fullWidth ->
                            if (targetState.ordinal > initialState.ordinal) -fullWidth else fullWidth
                        }
                    ) togetherWith slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = { fullWidth ->
                            if (targetState.ordinal > initialState.ordinal) fullWidth else -fullWidth
                        }
                    )
                } else {
                    slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { fullWidth ->
                            if (targetState.ordinal > initialState.ordinal) fullWidth else -fullWidth
                        }
                    ) togetherWith slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = { fullWidth ->
                            if (targetState.ordinal > initialState.ordinal) -fullWidth else fullWidth
                        }
                    )
                }
            },
            modifier = Modifier.offset {
                val xOffset = if (isRtl) -offsetX else offsetX
                IntOffset((xOffset + animatedOffset).roundToInt(), 0)
            }, label = ""
        ) { tab ->
            when (tab) {
                AnalyticsTabType.PEAK_HOURS -> data?.shiftPerformanceAnalysis?.let { shiftData ->
                    PeakHoursAnalysis(shiftData = shiftData)
                }
                AnalyticsTabType.QUOTAS -> {
                    if (data?.quotaCompletionAnalysis != null) {
                        QuotaAnalysis(
                            completionData = data.quotaCompletionAnalysis,
                            predictionData = data.quotaPredictionAnalysis
                        )
                    }
                }
                AnalyticsTabType.CARRIERS -> data?.carrierPerformanceAnalysis?.let { carrierData ->
                    CarrierAnalysis(carrierPerformanceData = carrierData)
                }
                AnalyticsTabType.WAREHOUSES -> {
                    if (data?.warehouseEfficiencyAnalysis != null) {
                        WarehouseAnalysis(
                            efficiencyData = data.warehouseEfficiencyAnalysis,
                            speedData = data.warehouseSpeedAnalysis,
                            trafficData = data.warehouseTrafficAnalysis,
                            peakData = data.warehousePeakAnalysis
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "داده‌ای برای نمایش وجود ندارد",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class AnalyticsTabType {
    PEAK_HOURS,
    QUOTAS,
    CARRIERS,
    WAREHOUSES
}

@Composable
private fun AnalyticsHeader(
    title: String,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "گزارشات 24 ساعت گذشته",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "بستن",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AnalyticsTabRow(
    selectedTab: AnalyticsTabType,
    onTabSelected: (AnalyticsTabType) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = remember {
        listOf(
            TabInfo(AnalyticsTabType.PEAK_HOURS, "24 ساعت گذشته", Icons.Default.Schedule),
            TabInfo(AnalyticsTabType.QUOTAS, "وضعیت کوتاژها", Icons.Default.Description),
            TabInfo(AnalyticsTabType.CARRIERS, "باربری‌ها", Icons.Default.LocalShipping),
            TabInfo(AnalyticsTabType.WAREHOUSES, "انبارها", Icons.Default.Warehouse)
        )
    }
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val centerOffset = remember { mutableIntStateOf(0) }
    var swipeOffset by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        tonalElevation = 2.dp
    ) {
        LazyRow(
            state = lazyListState,
            modifier = Modifier
                .padding(4.dp)
                .pointerInput(Unit) {
                    var startX = 0f
                    var currentIndex = tabs.indexOfFirst { it.type == selectedTab }

                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            startX = offset.x
                            isDragging = true
                        },
                        onDragEnd = {
                            isDragging = false
                            val dragPercentage = (swipeOffset / size.width).absoluteValue

                            if (dragPercentage > 0.3f) {
                                val direction = if (swipeOffset > 0) -1 else 1
                                val newIndex = (currentIndex + direction).coerceIn(0, tabs.lastIndex)

                                if (newIndex != currentIndex) {
                                    onTabSelected(tabs[newIndex].type)
                                    currentIndex = newIndex
                                }
                            }

                            swipeOffset = 0f
                        },
                        onDragCancel = {
                            isDragging = false
                            swipeOffset = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()

                            val canSwipeLeft = currentIndex < tabs.lastIndex
                            val canSwipeRight = currentIndex > 0

                            swipeOffset = when {
                                !canSwipeLeft && dragAmount < 0 -> 0f
                                !canSwipeRight && dragAmount > 0 -> 0f
                                else -> (swipeOffset + dragAmount).coerceIn(-size.width.toFloat(), size.width.toFloat())
                            }
                        }
                    )
                }
        ) {
            val totalWidth = centerOffset.intValue

            items(tabs) { tab ->
                val isSelected = selectedTab == tab.type

                LaunchedEffect(isSelected) {
                    if (isSelected && !isDragging) {
                        val currentIndex = tabs.indexOf(tab)
                        val scrollPosition = when (currentIndex) {
                            0 -> 0
                            tabs.lastIndex -> totalWidth
                            else -> {
                                val itemOffset = (currentIndex * 200) - (totalWidth / 2) + 100
                                itemOffset.coerceIn(0, totalWidth)
                            }
                        }

                        coroutineScope.launch {
                            lazyListState.animateScrollToItem(
                                index = currentIndex,
                                scrollOffset = -scrollPosition
                            )
                        }
                    }
                }

                ModernTab(
                    selected = isSelected,
                    onClick = { onTabSelected(tab.type) },
                    icon = tab.icon,
                    label = tab.label,
                    modifier = Modifier
                        .onSizeChanged { size ->
                            if (centerOffset.intValue == 0) {
                                centerOffset.intValue = size.width
                            }
                        }
                        .graphicsLayer {
                            if (isDragging) {
                                translationX = swipeOffset * 0.5f
                                alpha = 1f - (swipeOffset.absoluteValue / size.width * 0.3f)
                            }
                        }
                )
            }
        }
    }
}

@Composable
private fun ModernTab(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    val transition = updateTransition(selected, label = "tab")
    val backgroundColor by transition.animateColor(
        label = "backgroundColor",
        transitionSpec = {
            if (false isTransitioningTo true) {
                spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            } else {
                spring(dampingRatio = Spring.DampingRatioNoBouncy)
            }
        }
    ) { isSelected ->
        if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface
        }
    }
    val contentColor by transition.animateColor(
        label = "contentColor"
    ) { isSelected ->
        if (isSelected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    }
    val elevation by transition.animateDp(
        label = "elevation",
        transitionSpec = {
            if (false isTransitioningTo true) {
                spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            } else {
                spring(dampingRatio = Spring.DampingRatioNoBouncy)
            }
        }
    ) { isSelected ->
        if (isSelected) 4.dp else 0.dp
    }

    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        tonalElevation = elevation,
        shadowElevation = elevation
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
        }
    }
}

private data class TabInfo(
    val type: AnalyticsTabType,
    val label: String,
    val icon: ImageVector
)

@Composable
fun PeakHoursAnalysis(
    shiftData: List<ShiftPerformanceData>
) {
    var expandedShiftId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = shiftData,
            key = { it.shift }
        ) { shift ->
            ModernShiftCard(
                shift = shift,
                isDay = shift.shift == "روز",
                isExpanded = expandedShiftId == shift.shift,
                onExpandChange = { shouldExpand ->
                    expandedShiftId = if (shouldExpand) shift.shift else null
                }
            )
        }
    }
}

@Composable
private fun ModernShiftCard(
    shift: ShiftPerformanceData,
    isDay: Boolean,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (isDay) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { onExpandChange(!isExpanded) }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.05f)
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shift Info
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShiftIcon(isDay = isDay, color = color)

                    Column {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "شیفت ${shift.shift}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Badge(
                                containerColor = color.copy(alpha = 0.1f),
                                contentColor = color
                            ) {
                                Text("${formatNumber(shift.total_vouchers)} حواله")
                            }
                            Badge(
                                containerColor = color.copy(alpha = 0.1f),
                                contentColor = color
                            ) {
                                Text("${formatNumber(shift.total_weight_tons.roundToInt())} تن")
                            }
                        }
                    }
                }

                // Expand Icon
                IconButton(onClick = { onExpandChange(!isExpanded) }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = color
                    )
                }
            }

            // Quick Stats
            if (!isExpanded) {
                ShiftQuickStats(shift, color)
            }

            // Expanded Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ShiftDetailedStats(shift, color)
            }
        }
    }
}

@Composable
private fun ShiftQuickStats(
    shift: ShiftPerformanceData,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatItem(
            icon = Icons.AutoMirrored.Filled.Assignment,
            value = formatNumber(shift.total_operations),
            label = "کوتاژها",
            color = color
        )
        StatItem(
            icon = Icons.Default.Timer,
            value = "${shift.avg_completion_time.roundToInt()} دقیقه",
            label = "میانگین زمان",
            color = color
        )
        StatItem(
            icon = Icons.Default.Scale,
            value = formatNumber(shift.avg_weight_per_operation.roundToInt()),
            label = "میانگین وزن",
            color = color
        )
    }
}

@Composable
private fun ShiftDetailedStats(
    shift: ShiftPerformanceData,
    color: Color
) {
    Column(
        modifier = Modifier.padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(color = color.copy(alpha = 0.1f))

        // آمار کلی
        GeneralStatsSection(shift, color)

        // باربری‌های فعال
        HorizontalDivider(color = color.copy(alpha = 0.1f))
        CarriersSection(shift, color)

        // ساعت اوج
        if (shift.peak_hour != null && shift.peak_hour_detail != null) {
            HorizontalDivider(color = color.copy(alpha = 0.1f))
            PeakHoursSection(shift, color)
        }

        // عملیات‌های تاخیردار
        if (!shift.delayed_operations_detail.isNullOrEmpty()) {
            HorizontalDivider(color = color.copy(alpha = 0.1f))
            DelayedOperationsSection(shift, color)
        }
    }
}

@Composable
private fun GeneralStatsSection(
    shift: ShiftPerformanceData,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle(
                icon = Icons.Default.Analytics,
                title = "آمار کلی",
                color = color
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatColumn(
                    label = "تعداد کوتاژ",
                    value = formatNumber(shift.total_operations),
                    color = color
                )
                StatColumn(
                    label = "تعداد حواله",
                    value = formatNumber(shift.total_vouchers),
                    color = color
                )
                StatColumn(
                    label = "عملیات تاخیردار",
                    value = formatNumber(shift.total_delayed_operations),
                    color = color
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatColumn(
                    label = "وزن کل",
                    value = "${formatNumber(shift.total_weight_tons.roundToInt())} تن",
                    color = color
                )
                StatColumn(
                    label = "میانگین زمان",
                    value = "${shift.avg_completion_time.roundToInt()} دقیقه",
                    color = color
                )
            }
        }
    }
}

@Composable
private fun StatColumn(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun CarriersSection(
    shift: ShiftPerformanceData,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle(
                icon = Icons.Default.LocalShipping,
                title = "باربری‌های فعال",
                color = color
            )

            Text(
                text = "${shift.total_active_carriers} باربری فعال",
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )

            val carrierList = shift.active_carriers_list.split(",")
            carrierList.forEach { carrierInfo ->
                val (name, quotas, vouchers, weight) = carrierInfo.trim().split(":")
                CarrierItem(
                    name = name,
                    quotaCount = quotas.toInt(),
                    voucherCount = vouchers.toInt(),
                    totalWeight = weight.toDouble(),
                    color = color
                )
            }
        }
    }
}

@Composable
private fun PeakHoursSection(
    shift: ShiftPerformanceData,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle(
                icon = Icons.Default.Schedule,
                title = "ساعت اوج عملیات",
                color = color
            )

            Text(
                text = buildString {
                    append("ساعت ${shift.peak_hour}")
                    append(" با ${formatNumber(shift.peak_hour_operations ?: 0)} کوتاژ")
                    append(" و ${formatNumber(shift.peak_hour_vouchers ?: 0)} حواله")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )

            shift.peak_hour_detail?.split("|")?.forEach { detail ->
                val (carrier, kotazh, vouchers, weight, time) = detail.split(":")
                PeakHourItem(
                    carrier = carrier,
                    kotazh = kotazh,
                    voucherCount = vouchers.toInt(),
                    weight = weight.toDouble(),
                    time = time,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun DelayedOperationsSection(
    shift: ShiftPerformanceData,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle(
                icon = Icons.Default.Warning,
                title = "عملیات‌های تاخیردار",
                color = color
            )

            shift.delayed_operations_detail?.split("|")?.forEach { delayInfo ->
                val (kotazh, carrier, vouchers, hours, count) = delayInfo.split(":")
                DelayedOperationItem(
                    kotazh = kotazh,
                    carrier = carrier,
                    voucherNumbers = vouchers,
                    hours = hours.toFloat(),
                    voucherCount = count.toInt(),
                    color = color
                )
            }
        }
    }
}

@Composable
private fun CarrierItem(
    name: String,
    quotaCount: Int,
    voucherCount: Int,
    totalWeight: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // نام باربری در یک ردیف جداگانه
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )

            // اطلاعات باربری در ردیف دوم
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChipText("${formatNumber(quotaCount)} کوتاژ", color)
                    ChipText("${formatNumber(voucherCount)} حواله", color)
                    ChipText("${formatNumber(totalWeight.roundToInt())} تن", color)
                }
            }
        }
    }
}

@Composable
private fun PeakHourItem(
    carrier: String,
    kotazh: String,
    voucherCount: Int,
    weight: Double,
    time: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // ردیف اول: نام باربری و شماره کوتاژ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = carrier,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color
                )
                Text(
                    text = "کوتاژ: $kotazh",
                    style = MaterialTheme.typography.bodySmall,
                    color = color.copy(alpha = 0.7f)
                )
            }

            // ردیف دوم: حواله، تناژ و زمان
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChipText("${formatNumber(voucherCount)} حواله", color)
                    ChipText("${formatNumber(weight.roundToInt())} تن", color)
                    ChipText("${time}:00", color)
                }
            }
        }
    }
}

@Composable
private fun DelayedOperationItem(
    kotazh: String,
    carrier: String,
    voucherNumbers: String,
    hours: Float,
    voucherCount: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = carrier,
                        style = MaterialTheme.typography.bodyMedium,
                        color = color
                    )
                    Text(
                        text = "کوتاژ: $kotazh",
                        style = MaterialTheme.typography.bodySmall,
                        color = color.copy(alpha = 0.7f)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "بیشترین: ${formatHoursToPersian(hours)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (hours > 2) MaterialTheme.colorScheme.error else color
                    )
                    ChipText("$voucherCount حواله", color)
                }
            }
            if (voucherNumbers.isNotEmpty()) {
                Text(
                    text = "حواله‌ها: $voucherNumbers",
                    style = MaterialTheme.typography.bodySmall,
                    color = color.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ChipText(
    text: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun ShiftIcon(
    isDay: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .background(
                color = color.copy(alpha = 0.1f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isDay) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = if (isDay) "شیفت روز" else "شیفت شب",
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun QuotaAnalysis(
    completionData: List<QuotaCompletionData>,
    predictionData: List<QuotaPredictionData>
) {
    // متغیر برای ذخیره شماره کوتاژ باز شده
    var expandedQuotaNumber by remember { mutableStateOf<String?>(null) }

    val activeQuotas = completionData
        .filter { it.last_24h_vouchers > 0 }
        .sortedByDescending { it.last_24h_vouchers }

    if (activeQuotas.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "کوتاژ فعالی در 24 ساعت گذشته وجود ندارد",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = activeQuotas,
            key = { it.loadingQuotaNumber }
        ) { quota ->
            ModernQuotaCard(
                quota = quota,
                prediction = predictionData.find { it.loadingQuotaNumber == quota.loadingQuotaNumber },
                isExpanded = expandedQuotaNumber == quota.loadingQuotaNumber,
                onExpandChange = { shouldExpand ->
                    expandedQuotaNumber = if (shouldExpand) {
                        quota.loadingQuotaNumber
                    } else {
                        null
                    }
                }
            )
        }
    }
}

@Composable
private fun ModernQuotaCard(
    quota: QuotaCompletionData,
    prediction: QuotaPredictionData?,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val color = when {
        quota.completion_percentage >= 80 -> MaterialTheme.colorScheme.primary
        quota.completion_percentage >= 50 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { onExpandChange(!isExpanded) }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.05f)
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quota Info
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Icon with Background
                    QuotaStatusIcon(percentage = quota.completion_percentage, color = color)

                    // Quota Details
                    Column {
                        Text(
                            text = "کوتاژ ${quota.loadingQuotaNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Badge(
                                containerColor = color.copy(alpha = 0.1f),
                                contentColor = color
                            ) {
                                Text("${formatNumber(quota.last_24h_vouchers)} حواله جدید")
                            }
                            Badge(
                                containerColor = color.copy(alpha = 0.1f),
                                contentColor = color
                            ) {
                                Text("${formatNumber(quota.last_24h_weight.toInt())} تناژ")
                            }
                        }
                    }
                }

                // Expand Icon
                IconButton(onClick = { onExpandChange(!isExpanded) }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = color
                    )
                }
            }

            // Quick Stats
            if (!isExpanded) {
                QuotaQuickStats(quota, color)
            }

            // Expanded Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                QuotaDetailedStats(quota, prediction, color)
            }
        }
    }
}

@Composable
private fun QuotaStatusIcon(percentage: Float, color: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(color.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when {
                percentage >= 80 -> Icons.Default.Star
                percentage >= 50 -> Icons.Default.CheckCircle
                else -> Icons.Default.Warning
            },
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun QuotaQuickStats(
    quota: QuotaCompletionData,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatItem(
            icon = Icons.Default.LocalShipping,
            value = quota.shippingCompany,
            label = "باربری",
            color = color
        )
        StatItem(
            icon = Icons.Default.Scale,
            value = formatNumber(quota.total_vouchers),
            label = "حواله‌ها",
            color = color
        )
        StatItem(
            icon = Icons.Default.Timer,
            value = formatHoursToPersian(quota.avg_completion_hours),
            label = "میانگین زمان",
            color = color
        )
    }
}

@Composable
private fun QuotaDetailedStats(
    quota: QuotaCompletionData,
    prediction: QuotaPredictionData?,
    color: Color
) {
    Column(
        modifier = Modifier.padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(color = color.copy(alpha = 0.1f))

        // مشخصات کشتی و اطلاعات وزن (ادغام شده)
        ShipAndWeightSection(quota, color)

        // پیش‌بینی تکمیل
        prediction?.let {
            HorizontalDivider(color = color.copy(alpha = 0.1f))
            PredictionSection(it, color)
        }
    }
}

@Composable
private fun ShipAndWeightSection(
    quota: QuotaCompletionData,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // مشخصات کشتی
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle(
                    icon = Icons.Default.DirectionsBoat,
                    title = "مشخصات کشتی و وزن",
                    color = color
                )

                Text(
                    text = quota.shipName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            }

            // ردیف اول: شرکت باربری و وزن کل کوتاژ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuataInfoColumn(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.LocalShipping,
                    label = "شرکت باربری",
                    value = quota.shippingCompany,
                    color = color
                )
                QuataInfoColumn(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Scale,
                    label = "وزن کل کوتاژ",
                    value = formatNumber(quota.total_quota_weight.toInt()),
                    color = color
                )
            }

            // ردیف دوم: میانگین زمانی و وزن خالص
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuataInfoColumn(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Timer,
                    label = "میانگین زمانی",
                    value = formatHoursToPersian(quota.avg_completion_hours),
                    color = color
                )
                QuataInfoColumn(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Update,
                    label = "وزن خالص",
                    value = formatNumber(quota.last_24h_weight.toInt()),
                    color = color
                )
            }
        }
    }
}

@Composable
private fun QuataInfoColumn(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PredictionSection(
    prediction: QuotaPredictionData,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle(
                icon = Icons.Default.Update,
                title = "پیش‌بینی تکمیل",
                color = color
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(
                    label = "نرخ روزانه",
                    value = "${formatNumber(prediction.daily_rate.toInt())} کیلوگرم",
                    color = color
                )
                InfoItem(
                    label = "زمان باقیمانده",
                    value = formatHoursToPersian(prediction.estimated_days_remaining),
                    color = color
                )
            }
        }
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String,
    color: Color
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
fun CarrierAnalysis(
    carrierPerformanceData: List<CarrierPerformanceAnalysis>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = carrierPerformanceData.sortedByDescending { it.quality_score },
            key = { it.shippingCompany }
        ) { carrier ->
            ModernCarrierCard(carrier = carrier)
        }
    }
}

@Composable
private fun ModernCarrierCard(
    carrier: CarrierPerformanceAnalysis,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val color = when {
        carrier.quality_score >= 80 -> MaterialTheme.colorScheme.primary
        carrier.quality_score >= 70 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { isExpanded = !isExpanded }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.05f)
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Company Info
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Icon with Background
                    StatusIndicator(carrier.quality_score, color)

                    // Company Details
                    Column {
                        Text(
                            text = carrier.shippingCompany,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Badge(
                                containerColor = color.copy(alpha = 0.1f),
                                contentColor = color
                            ) {
                                Text(carrier.performance_category)
                            }
                            Badge(
                                containerColor = color.copy(alpha = 0.1f),
                                contentColor = color
                            ) {
                                Text("${carrier.quality_score.roundToInt()}%")
                            }
                        }
                    }
                }

                // Expand Icon
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = color
                    )
                }
            }

            // Quick Stats
            if (!isExpanded) {
                QuickStats(carrier, color)
            }

            // Expanded Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                DetailedStats(carrier, color)
            }
        }
    }
}

@Composable
private fun StatusIndicator(score: Float, color: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(color.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when {
                score >= 80 -> Icons.Default.Star
                score >= 70 -> Icons.Default.CheckCircle
                else -> Icons.Default.Warning
            },
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun QuickStats(carrier: CarrierPerformanceAnalysis, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatItem(
            icon = Icons.Default.LocalShipping,
            value = formatNumber(carrier.completed_deliveries),
            label = "حواله‌ها",
            color = color
        )
        StatItem(
            icon = Icons.Default.Folder,
            value = formatNumber(carrier.total_quotas),
            label = "کوتاژها",
            color = color
        )
        StatItem(
            icon = Icons.Default.Speed,
            value = "${carrier.operations_per_hour}",
            label = "حواله بر ساعت",
            color = color
        )
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun DetailedStats(carrier: CarrierPerformanceAnalysis, color: Color) {
    Column(
        modifier = Modifier.padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(color = color.copy(alpha = 0.1f))

        // آمار اصلی در کارت‌های جداگانه
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Timer,
                label = "زمان عملیات",
                value = formatHoursToPersian(carrier.avg_operation_time / 60f),
                color = color
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Speed,
                label = "سرعت عملیات",
                value = "${carrier.operations_per_hour}/h",
                color = color
            )
        }

        // بخش کوتاژها
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = color.copy(alpha = 0.05f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "کوتاژهای فعال",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "${carrier.completed_quotas} از ${carrier.total_quotas}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = color
                    )
                }

                carrier.completed_quota_numbers?.let { quotaNumbers ->
                    QuotaNumbersGrid(
                        quotaNumbers = quotaNumbers.split(","),
                        color = color
                    )
                }
            }
        }

        // اطلاعات وزن در یک کارت
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = color.copy(alpha = 0.05f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Scale,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "اطلاعات وزن",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WeightInfo(
                        label = "میانگین",
                        value = formatNumber(carrier.avg_net_weight.roundToInt()),
                        color = color
                    )
                    WeightInfo(
                        label = "حداقل",
                        value = formatNumber(carrier.min_weight.roundToInt()),
                        color = color
                    )
                    WeightInfo(
                        label = "حداکثر",
                        value = formatNumber(carrier.max_weight.roundToInt()),
                        color = color
                    )
                }
            }
        }

        // زمان اوج فعالیت
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = color.copy(alpha = 0.05f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "ساعت اوج",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "${formatNumber(carrier.peak_hour_operations)} عملیات در ساعت ${carrier.peak_hour}:00",
                    style = MaterialTheme.typography.bodyMedium,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
private fun QuotaNumbersGrid(
    quotaNumbers: List<String>,
    color: Color
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .heightIn(max = 120.dp)
            .fillMaxWidth()
    ) {
        items(quotaNumbers) { quotaNumber ->
            QuotaChip(quotaNumber.trim(), color)
        }
    }
}

@Composable
private fun WeightInfo(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun QuotaChip(
    quotaNumber: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Text(
            text = quotaNumber,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun WarehouseAnalysis(
    efficiencyData: List<WarehouseEfficiencyData>,
    speedData: List<WarehouseSpeedData>,
    trafficData: List<WarehouseTrafficData>,
    peakData: List<WarehousePeakData>
) {
    var expandedWarehouse by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = efficiencyData.sortedByDescending { it.daily_throughput },
            key = { it.loadingWarehouse }
        ) { warehouse ->
            ModernWarehouseCard(
                warehouse = warehouse,
                speedData = speedData.find { it.loadingWarehouse == warehouse.loadingWarehouse },
                trafficData = trafficData.filter { it.loadingWarehouse == warehouse.loadingWarehouse },
                peakData = peakData.filter { it.loadingWarehouse == warehouse.loadingWarehouse },
                isExpanded = expandedWarehouse == warehouse.loadingWarehouse,
                onExpandChange = {
                    expandedWarehouse = if (expandedWarehouse == warehouse.loadingWarehouse) null
                    else warehouse.loadingWarehouse
                }
            )
        }
    }
}

@Composable
private fun ModernWarehouseCard(
    warehouse: WarehouseEfficiencyData,
    speedData: WarehouseSpeedData?,
    trafficData: List<WarehouseTrafficData>,
    peakData: List<WarehousePeakData>,
    isExpanded: Boolean,
    onExpandChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    val efficiency = warehouse.daily_throughput / 1000 // تبدیل به تن
    val color = when {
        efficiency >= 100 -> MaterialTheme.colorScheme.primary
        efficiency >= 50 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { onExpandChange() }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.05f)
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // هدر کارت
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // آیکون وضعیت
                    WarehouseStatusIcon(color = color)

                    // اطلاعات اصلی
                    Column {
                        Text(
                            text = warehouse.loadingWarehouse,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Badge(
                                containerColor = color.copy(alpha = 0.1f),
                                contentColor = color
                            ) {
                                Text("${formatNumber(warehouse.active_quotas)} کوتاژ فعال")
                            }
                            Badge(
                                containerColor = color.copy(alpha = 0.1f),
                                contentColor = color
                            ) {
                                Text("${formatNumber(efficiency.roundToInt())} تن در روز")
                            }
                        }
                    }
                }

                IconButton(onClick = { onExpandChange() }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = color
                    )
                }
            }

            // آمار سریع
            if (!isExpanded) {
                QuickWarehouseStats(warehouse, color)
            }

            // محتوای گسترش‌یافته
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                DetailedWarehouseStats(
                    warehouse = warehouse,
                    speedData = speedData,
                    trafficData = trafficData,
                    peakData = peakData,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun WarehouseStatusIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .background(color.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warehouse,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun QuickWarehouseStats(
    warehouse: WarehouseEfficiencyData,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatItem(
            icon = Icons.AutoMirrored.Filled.Assignment,
            value = formatNumber(warehouse.total_operations),
            label = "عملیات",
            color = color
        )
        StatItem(
            icon = Icons.Default.Speed,
            value = String.format("%.1f", warehouse.daily_operations),
            label = "عملیات در روز",
            color = color
        )
        StatItem(
            icon = Icons.Default.Scale,
            value = formatNumber((warehouse.total_processed_weight / 1000).roundToInt()),
            label = "تناژ بارگیری (تن)",
            color = color
        )
    }
}

@Composable
private fun DetailedWarehouseStats(
    warehouse: WarehouseEfficiencyData,
    speedData: WarehouseSpeedData?,
    trafficData: List<WarehouseTrafficData>,
    peakData: List<WarehousePeakData>,
    color: Color
) {
    Column(
        modifier = Modifier.padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HorizontalDivider(color = color.copy(alpha = 0.1f))

        // بخش کارایی
        WarehouseEfficiencySection(warehouse, color)

        // اطلاعات سرعت عملیات
        speedData?.let {
            HorizontalDivider(color = color.copy(alpha = 0.1f))
            WarehouseSpeedSection(it, color)
        }

        // الگوهای ترافیکی
        if (trafficData.isNotEmpty()) {
            HorizontalDivider(color = color.copy(alpha = 0.1f))
            WarehouseTrafficSection(trafficData, color)
        }

        // ساعات اوج
        if (peakData.isNotEmpty()) {
            HorizontalDivider(color = color.copy(alpha = 0.1f))
            WarehousePeakTimesSection(peakData, color)
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun WarehouseEfficiencySection(
    warehouse: WarehouseEfficiencyData,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(
            icon = Icons.Default.Analytics,
            title = "کارایی انبار",
            color = color
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Filled.Assignment,
                label = "کوتاژهای فعال",
                value = formatNumber(warehouse.active_quotas),
                color = color
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Speed,
                label = "عملیات روزانه",
                value = String.format("%.1f", warehouse.daily_operations),
                color = color
            )
        }

        // نمایش درصد عملیات
        LinearProgressIndicator(
            progress = { warehouse.operation_percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.1f)
        )

        Text(
            text = "${warehouse.operation_percentage}% از کل عملیات",
            style = MaterialTheme.typography.bodySmall,
            color = color,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
private fun WarehouseSpeedSection(
    speedData: WarehouseSpeedData,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(
            icon = Icons.Default.Timer,
            title = "سرعت عملیات",
            color = color
        )

        // کارت‌های آماری
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Timer,
                label = "میانگین زمان",
                value = "${speedData.avg_processing_minutes.roundToInt()} دقیقه",
                color = color
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Scale,
                label = "وزن در دقیقه",
                value = "${formatNumber(speedData.weight_per_minute.roundToInt())} کیلوگرم",
                color = color
            )
        }

        // نرخ تکمیل
        CompletionRateIndicator(
            rate = speedData.completion_rate,
            color = color
        )
    }
}

@Composable
private fun WarehouseTrafficSection(
    trafficData: List<WarehouseTrafficData>,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(
            icon = Icons.Default.Timeline,
            title = "الگوی ترافیک",
            color = color
        )

        // نمودار ترافیک ساعتی
        Surface(
            color = color.copy(alpha = 0.05f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                trafficData.forEach { hourData ->
                    HourlyTrafficRow(
                        hour = hourData.hour,
                        entries = hourData.entries,
                        exits = hourData.exits,
                        percentage = hourData.hour_percentage,
                        avgWeight = hourData.avg_processed_weight,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
private fun WarehousePeakTimesSection(
    peakData: List<WarehousePeakData>,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(
            icon = Icons.Default.Schedule,
            title = "ساعات اوج",
            color = color
        )

        // نمایش ساعات اوج
        Surface(
            color = color.copy(alpha = 0.05f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                peakData.forEach { peak ->
                    PeakTimeRow(
                        hour = peak.hour,
                        operationCount = peak.operation_count,
                        avgWeight = peak.avg_weight,
                        percentage = peak.period_percentage,
                        activityLevel = peak.activity_level,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletionRateIndicator(
    rate: Float,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "نرخ تکمیل عملیات",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${rate.roundToInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
        LinearProgressIndicator(
            progress = { rate / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.1f)
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun HourlyTrafficRow(
    hour: Int,
    entries: Int,
    exits: Int,
    percentage: Float,
    avgWeight: Float,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ساعت و آمار
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // نمایش ساعت
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = hour.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            }

            // آمار ورود و خروج
            Column {
                Text(
                    text = "${formatNumber(entries)} ورود • ${formatNumber(exits)} خروج",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${formatNumber(avgWeight.roundToInt())} کیلوگرم",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // درصد
        Text(
            text = String.format("%.1f%%", percentage),
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PeakTimeRow(
    hour: Int,
    operationCount: Int,
    avgWeight: Float,
    percentage: Float,
    activityLevel: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ساعت و آمار
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // نمایش ساعت با پس‌زمینه
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = hour.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            }

            // اطلاعات عملیات
            Column {
                Text(
                    text = "${formatNumber(operationCount)} عملیات",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${formatNumber(avgWeight.roundToInt())} کیلوگرم",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // نمایش وضعیت فعالیت
        ActivityLevelBadge(
            activityLevel = activityLevel,
            percentage = percentage,
            color = color
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun ActivityLevelBadge(
    activityLevel: String,
    percentage: Float,
    color: Color
) {
    Badge(
        containerColor = color.copy(alpha = 0.1f),
        contentColor = color
    ) {
        Text(
            text = "$activityLevel (${String.format("%.1f%%", percentage)})",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun SectionTitle(
    icon: ImageVector,
    title: String,
    color: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@SuppressLint("DefaultLocale")
fun formatWeightWithDetail(weightInKg: Float): String {
    val exactValue = formatNumber(weightInKg.toInt())

    val simplifiedWeight = when {
        weightInKg >= 1_000_000 -> {
            val thousandTons = weightInKg / 1_000_000
            if (thousandTons % 1 == 0f) {
                "${thousandTons.toInt()} هزار تن"
            } else {
                val formattedThousandTons = formatNumber((weightInKg / 1_000).toInt())
                "$formattedThousandTons هزار تن"
            }
        }
        weightInKg >= 1_000 -> {
            val tons = (weightInKg / 1_000)
            if (tons % 1 == 0f) {
                "${tons.toInt()} تن"
            } else {
                String.format("%.1f تن", tons)
            }
        }
        else -> "${weightInKg.toInt()} کیلو"
    }

    return "$simplifiedWeight ($exactValue)"
}

fun calculateProgress(value: Float, total: Float): Float {
    return if (total > 0f) (value / total).coerceIn(0f, 1f) else 0f
}

fun calculatePercentage(value: Float, total: Float): Int {
    return if (total > 0f) ((value / total) * 100).toInt().coerceIn(0, 100) else 0
}

fun Double.format(digits: Int) = "%.${digits}f".format(this)

private fun formatNumber(number: Int): String {
    return NumberFormat.getNumberInstance(Locale("en", "US")).format(number)
}

fun formatHoursToPersian(hours: Float): String {
    if (hours <= 0) return "0 دقیقه"

    val wholeHours = hours.toInt()
    val minutes = ((hours - wholeHours) * 60).roundToInt()

    return when {
        wholeHours > 0 && minutes > 0 -> "${formatNumber(wholeHours)} ساعت و ${formatNumber(minutes)} دقیقه"
        wholeHours > 0 -> "${formatNumber(wholeHours)} ساعت"
        minutes > 0 -> "${formatNumber(minutes)} دقیقه"
        else -> "0 دقیقه"
    }
}