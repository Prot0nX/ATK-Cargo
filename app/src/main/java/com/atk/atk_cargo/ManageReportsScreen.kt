package com.atk.atk_cargo

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
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
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LowPriority
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.Typography
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
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
import com.atk.atk_cargo.api.CargoOwnerData
import com.atk.atk_cargo.api.CargoOwnerDetailsData
import com.atk.atk_cargo.api.CarrierPerformanceAnalysis
import com.atk.atk_cargo.api.ColorSelector
import com.atk.atk_cargo.api.FabItem
import com.atk.atk_cargo.api.FilteredSummary
import com.atk.atk_cargo.api.Quota
import com.atk.atk_cargo.api.QuotaCompletionData
import com.atk.atk_cargo.api.QuotaDetails
import com.atk.atk_cargo.api.QuotaEditData
import com.atk.atk_cargo.api.QuotaGroupingMode
import com.atk.atk_cargo.api.QuotaPercentageData
import com.atk.atk_cargo.api.QuotaPredictionData
import com.atk.atk_cargo.api.RealTimeLoadingData
import com.atk.atk_cargo.api.ReportsViewModel
import com.atk.atk_cargo.api.ShiftInfo
import com.atk.atk_cargo.api.ShiftPerformanceData
import com.atk.atk_cargo.api.Ship
import com.atk.atk_cargo.api.ShipSection
import com.atk.atk_cargo.api.VoucherDetail
import com.atk.atk_cargo.api.Warehouse
import com.atk.atk_cargo.api.WarehouseEfficiencyData
import com.atk.atk_cargo.api.WarehouseGroupingMode
import com.atk.atk_cargo.api.WarehouseQuotaGroupingMode
import com.atk.atk_cargo.api.WarningStatus
import com.atk.atk_cargo.api.adjustColorForTheme
import com.atk.atk_cargo.api.cardColors
import com.atk.atk_cargo.api.toTon
import com.atk.atk_cargo.ui.theme.getCompletionColor
import com.atk.atk_cargo.ui.theme.getCompletionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
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
	var showSearchResultDialog by remember { mutableStateOf(false) }
	var showRealTimeDialog by remember { mutableStateOf(false) }
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
		onAdvancedSearchClick = {
			showAdvancedSearchDialog = true
		},
		onAnalyticsClick = {
			showAnalyticsDialog = true
		}
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
			ModernSearchField(
				searchQuery = searchTerm,
				onSearchQueryChange = { searchTerm = it }
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
fun ModernSearchField(
	searchQuery: String,
	onSearchQueryChange: (String) -> Unit,
	modifier: Modifier = Modifier,
	placeholder: String = "جستجوی کشتی، انبار یا کوتاژ"
) {
	var isFocused by remember { mutableStateOf(false) }
	val focusRequester = remember { FocusRequester() }
	
	// انیمیشن‌های مختلف برای حالت‌های مختلف
	val borderColor by animateColorAsState(
		targetValue = when {
			isFocused -> MaterialTheme.colorScheme.primary
			searchQuery.isNotEmpty() -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
			else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
		},
		animationSpec = tween(200),
		label = "border color"
	)
	
	val backgroundColor by animateColorAsState(
		targetValue = when {
			isFocused -> MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
			searchQuery.isNotEmpty() -> MaterialTheme.colorScheme.primary.copy(alpha = 0.02f)
			else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
		},
		animationSpec = tween(200),
		label = "background color"
	)
	
	val iconTint by animateColorAsState(
		targetValue = when {
			isFocused -> MaterialTheme.colorScheme.primary
			searchQuery.isNotEmpty() -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
			else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
		},
		animationSpec = tween(200),
		label = "icon tint"
	)
	
	Surface(
		modifier = modifier
			.height(48.dp)
			.animateContentSize(),
		shape = RoundedCornerShape(24.dp),
		color = backgroundColor,
		border = BorderStroke(
			width = if (isFocused) 1.5.dp else 1.dp,
			color = borderColor
		)
	) {
		Row(
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 16.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			// آیکون جستجو
			Icon(
				imageVector = Icons.Default.Search,
				contentDescription = null,
				tint = iconTint,
				modifier = Modifier.size(20.dp)
			)
			
			Spacer(modifier = Modifier.width(12.dp))
			
			// فیلد متنی
			Box(
				modifier = Modifier.weight(1f),
				contentAlignment = Alignment.CenterStart
			) {
				BasicTextField(
					value = searchQuery,
					onValueChange = onSearchQueryChange,
					modifier = Modifier
						.fillMaxWidth()
						.focusRequester(focusRequester)
						.onFocusChanged { focusState ->
							isFocused = focusState.isFocused
						},
					textStyle = MaterialTheme.typography.bodyMedium.copy(
						color = MaterialTheme.colorScheme.onSurface,
						textDirection = TextDirection.Rtl
					),
					singleLine = true,
					cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
					decorationBox = { innerTextField ->
						innerTextField()
					}
				)
				
				// متن راهنما
				if (searchQuery.isEmpty() && !isFocused) {
					Text(
						text = placeholder,
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
						textAlign = TextAlign.Start
					)
				}
			}
			
			// دکمه پاک کردن
			if (searchQuery.isNotEmpty()) {
				IconButton(
					onClick = { onSearchQueryChange("") },
					modifier = Modifier.size(32.dp)
				) {
					Icon(
						imageVector = Icons.Default.Clear,
						contentDescription = "پاک کردن",
						tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
						modifier = Modifier.size(18.dp)
					)
				}
			}
		}
	}
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
	val mainColor = if (isSectionActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
	var expandedShipName by remember { mutableStateOf<String?>(null) }
	
	// انیمیشن چرخش آیکون
	val rotationState by animateFloatAsState(
		targetValue = if (isExpanded) 180f else 0f,
		animationSpec = tween(durationMillis = 300),
		label = "rotation"
	)

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
				MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
			} else {
				MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
			}
		),
		shape = RoundedCornerShape(12.dp),
		border = BorderStroke(
			width = 1.dp,
			color = mainColor.copy(alpha = 0.15f)
		)
	) {
		Column(modifier = Modifier.padding(16.dp)) {
			// هدر بخش
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.clip(RoundedCornerShape(8.dp))
					.clickable(
						interactionSource = remember { MutableInteractionSource() },
						indication = rememberRipple(bounded = true, color = mainColor),
						onClick = onExpandChange
					)
					.padding(vertical = 8.dp, horizontal = 4.dp),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(12.dp)
				) {
					// آیکون شناسه بخش
					Surface(
						shape = CircleShape,
						color = mainColor.copy(alpha = 0.1f),
						modifier = Modifier.size(40.dp)
					) {
						Box(contentAlignment = Alignment.Center) {
							Icon(
								imageVector = if (isSectionActive) Icons.Default.DirectionsBoat else Icons.Default.Archive,
								contentDescription = null,
								tint = mainColor,
								modifier = Modifier.size(20.dp)
							)
						}
					}
					
					// اطلاعات اصلی بخش
					Column {
						Text(
							text = title,
							style = MaterialTheme.typography.titleMedium,
							fontWeight = FontWeight.Bold,
							color = mainColor
						)
						
						Row(
							verticalAlignment = Alignment.CenterVertically,
							horizontalArrangement = Arrangement.spacedBy(4.dp)
						) {
							Text(
								text = "${ships.size} کشتی",
								style = MaterialTheme.typography.bodySmall,
								color = mainColor.copy(alpha = 0.7f)
							)
							
							Box(
								modifier = Modifier
									.size(4.dp)
									.background(mainColor.copy(alpha = 0.5f), CircleShape)
							)
							
							Text(
								text = formatWeightWithDetail(ships.sumOf { it.remainingTonnage.toDouble() }.toFloat()),
								style = MaterialTheme.typography.bodySmall,
								color = mainColor.copy(alpha = 0.7f)
							)
						}
					}
				}

				// آیکون باز/بسته کردن با انیمیشن
				Icon(
					imageVector = Icons.Default.KeyboardArrowDown,
					contentDescription = if (isExpanded) "بستن" else "باز کردن",
					modifier = Modifier
						.size(24.dp)
						.rotate(rotationState),
					tint = mainColor
				)
			}

			// محتوای قابل باز/بسته شدن
			AnimatedVisibility(
				visible = isExpanded,
				enter = expandVertically() + fadeIn(
					animationSpec = tween(durationMillis = 300)
				),
				exit = shrinkVertically() + fadeOut(
					animationSpec = tween(durationMillis = 200)
				)
			) {
				Column(modifier = Modifier.padding(top = 12.dp)) {
					// اطلاعات تناژ
					TonnageInfo(ships)
					
					Spacer(modifier = Modifier.height(12.dp))
					HorizontalDivider(color = mainColor.copy(alpha = 0.1f))
					Spacer(modifier = Modifier.height(12.dp))
					
					// لیست کشتی‌ها
					LazyColumn(
						verticalArrangement = Arrangement.spacedBy(8.dp),
						contentPadding = PaddingValues(vertical = 4.dp)
					) {
						items(ships) { ship ->
							ShipCard(
								ship = ship,
								onClick = { onShipSelected(ship.name) },
								color = mainColor,
								isActive = ship.isActive,
								isExpanded = expandedShipName == ship.name,
								onExpandToggle = {
									expandedShipName = if (expandedShipName == ship.name) null else ship.name
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
private fun TonnageInfo(ships: List<Ship>) {
	val totalTonnage = ships.sumOf { it.totalTonnage.toDouble() }.toFloat()
	val loadedTonnage = ships.sumOf { (it.totalTonnage - it.remainingTonnage).toDouble() }.toFloat()
	totalTonnage - loadedTonnage

	Surface(
		shape = RoundedCornerShape(8.dp),
		color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
		modifier = Modifier.fillMaxWidth()
	) {
		Column(
			modifier = Modifier.padding(12.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			
			// مقادیر پیشرفت
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceEvenly,
				verticalAlignment = Alignment.CenterVertically
			) {
				// بخش مقدار کل
				TonnageChip(
					icon = Icons.Default.Scale,
					label = "کل",
					value = formatNumber(totalTonnage.toInt()),
					color = MaterialTheme.colorScheme.primary
				)
				
				// بخش بارگیری شده
				TonnageChip(
					icon = Icons.Default.Inventory,
					label = "بارگیری",
					value = formatNumber(loadedTonnage.toInt()),
					color = MaterialTheme.colorScheme.secondary
				)
			}
		}
	}
}

@Composable
private fun TonnageChip(
	icon: ImageVector,
	label: String,
	value: String,
	color: Color
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(4.dp)
	) {
		Icon(
			imageVector = icon,
			contentDescription = null,
			tint = color,
			modifier = Modifier.size(16.dp)
		)
		
		Column(horizontalAlignment = Alignment.Start) {
			Text(
				text = value,
				style = MaterialTheme.typography.titleSmall,
				fontWeight = FontWeight.Bold,
				color = color
			)
			Text(
				text = "$label تن",
				style = MaterialTheme.typography.bodySmall,
				color = color.copy(alpha = 0.7f)
			)
		}
	}
}

@Composable
fun ShipCard(
	ship: Ship,
	onClick: () -> Unit,
	color: Color,
	isActive: Boolean,
	isExpanded: Boolean,
	onExpandToggle: () -> Unit,
	modifier: Modifier = Modifier
) {
	val cardAlpha = if (isActive) 0.08f else 0.03f
	val contentAlpha = if (isActive) 1f else 0.4f
	val borderAlpha = if (isActive) 0.15f else 0.05f

	calculateProgress(
		ship.totalTonnage - ship.remainingTonnage,
		ship.totalTonnage
	)
	
	// انیمیشن چرخش آیکون باز/بسته کردن
	val rotationState by animateFloatAsState(
		targetValue = if (isExpanded) 180f else 0f,
		animationSpec = tween(durationMillis = 300),
		label = "rotation"
	)

	Card(
		modifier = modifier
			.fillMaxWidth()
			.animateContentSize(
				animationSpec = spring(
					dampingRatio = Spring.DampingRatioMediumBouncy,
					stiffness = Spring.StiffnessLow
				)
			)
			.clip(RoundedCornerShape(10.dp))
			.clickable(
				interactionSource = remember { MutableInteractionSource() },
				indication = rememberRipple(bounded = true),
				onClick = onExpandToggle
			),
		colors = CardDefaults.cardColors(
			containerColor = color.copy(alpha = cardAlpha)
		),
		border = BorderStroke(1.dp, color.copy(alpha = borderAlpha)),
		shape = RoundedCornerShape(10.dp)
	) {
		Column(modifier = Modifier.padding(12.dp)) {
			// هدر کارت کشتی
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				// نام کشتی و آیکون
				Row(
					horizontalArrangement = Arrangement.spacedBy(10.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					// آیکون کشتی
					Surface(
						shape = CircleShape,
						color = color.copy(alpha = 0.1f),
						modifier = Modifier.size(36.dp)
					) {
						Box(contentAlignment = Alignment.Center) {
							Icon(
								imageVector = Icons.Default.DirectionsBoat,
								contentDescription = null,
								tint = color.copy(alpha = contentAlpha),
								modifier = Modifier.size(18.dp)
							)
						}
					}
					
					// نام کشتی - محدود به یک خط
					Text(
						text = if (ship.name.length > 9) "${ship.name.take(9)}..." else ship.name,
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.SemiBold,
						color = color.copy(alpha = contentAlpha),
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
						modifier = Modifier.widthIn(max = 120.dp)
					)
				}
				
				// اطلاعات آماری کوتاژها و تناژ باقیمانده
				Row(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					// آمار کوتاژها
					InfoChip(
						icon = Icons.Default.Description,
						value = formatNumber(ship.quotaCount),
						color = color.copy(alpha = contentAlpha)
					)
					
					// آمار تناژ باقیمانده
					InfoChip(
						icon = Icons.Default.Scale,
						value = formatNumber(ship.remainingTonnage.toInt()),
						color = color.copy(alpha = contentAlpha)
					)
					
					// آیکون باز/بسته کردن
					Icon(
						imageVector = Icons.Default.KeyboardArrowDown,
						contentDescription = if (isExpanded) "بستن" else "باز کردن",
						modifier = Modifier
							.size(24.dp)
							.rotate(rotationState),
						tint = color.copy(alpha = contentAlpha)
					)
				}
			}
			
			// فضای خالی قبل از محتوای توسعه یافته
			Spacer(modifier = Modifier.height(4.dp))

			// محتوای قابل باز/بسته شدن
			AnimatedVisibility(
				visible = isExpanded,
				enter = expandVertically() + fadeIn(),
				exit = shrinkVertically() + fadeOut()
			) {
				ExpandedContent(ship, color, contentAlpha, onClick)
			}
		}
	}
}

@Composable
private fun InfoChip(icon: ImageVector, value: String, color: Color) {
	Surface(
		shape = RoundedCornerShape(6.dp),
		color = color.copy(alpha = 0.08f),
		border = BorderStroke(0.5.dp, color.copy(alpha = 0.15f))
	) {
		Row(
			modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
			horizontalArrangement = Arrangement.spacedBy(4.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				imageVector = icon,
				contentDescription = null,
				tint = color,
				modifier = Modifier.size(12.dp)
			)
			Text(
				text = value,
				style = MaterialTheme.typography.bodySmall,
				fontWeight = FontWeight.Medium,
				color = color
			)
		}
	}
}

@Composable
private fun ExpandedContent(
	ship: Ship,
	color: Color,
	contentAlpha: Float,
	onClick: () -> Unit
) {
	Column(
		modifier = Modifier.padding(top = 12.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp)
	) {
		// جداکننده افقی
		HorizontalDivider(
			color = color.copy(alpha = 0.1f),
			thickness = 1.dp
		)
		
		// اطلاعات آماری تناژ
		Surface(
			shape = RoundedCornerShape(8.dp),
			color = color.copy(alpha = 0.05f),
			modifier = Modifier.fillMaxWidth()
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(12.dp),
				horizontalArrangement = Arrangement.SpaceEvenly,
				verticalAlignment = Alignment.CenterVertically
			) {
				// تناژ کل
				DetailInfoItem(
					icon = Icons.Default.Scale,
					label = "تناژ کل",
					value = formatNumber(ship.totalTonnage.toInt()),
					color = color.copy(alpha = contentAlpha)
				)
				
				// جداکننده عمودی
				Box(
					modifier = Modifier
						.height(30.dp)
						.width(1.dp)
						.background(color.copy(alpha = 0.1f))
				)
				
				// تناژ بارگیری شده
				DetailInfoItem(
					icon = Icons.Default.Inventory,
					label = "بارگیری شده",
					value = formatNumber((ship.totalTonnage - ship.remainingTonnage).toInt()),
					color = color.copy(alpha = contentAlpha)
				)
				
				// جداکننده عمودی
				Box(
					modifier = Modifier
						.height(30.dp)
						.width(1.dp)
						.background(color.copy(alpha = 0.1f))
				)
				
				// تناژ باقیمانده
				DetailInfoItem(
					icon = Icons.Default.PendingActions,
					label = "مانده",
					value = formatNumber(ship.remainingTonnage.toInt()),
					color = color.copy(alpha = contentAlpha)
				)
			}
		}
		
		// اطلاعات انبارها و کوتاژها
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			// آمار انبارها
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(6.dp)
			) {
				Icon(
					imageVector = Icons.Default.Warehouse,
					contentDescription = null,
					modifier = Modifier.size(16.dp),
					tint = color.copy(alpha = contentAlpha)
				)
				Text(
					text = "${ship.warehouseCount} انبار",
					style = MaterialTheme.typography.bodyMedium,
					color = color.copy(alpha = contentAlpha)
				)
			}
			
			// آمار کوتاژها
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(6.dp)
			) {
				Icon(
					imageVector = Icons.Default.Description,
					contentDescription = null,
					modifier = Modifier.size(16.dp),
					tint = color.copy(alpha = contentAlpha)
				)
				Text(
					text = "${formatNumber(ship.quotaCount)} کوتاژ",
					style = MaterialTheme.typography.bodyMedium,
					color = color.copy(alpha = contentAlpha)
				)
			}
		}

		// دکمه مشاهده جزئیات
		Button(
			onClick = onClick,
			modifier = Modifier
				.fillMaxWidth()
				.height(40.dp),
			colors = ButtonDefaults.buttonColors(
				containerColor = color.copy(alpha = 0.1f),
				contentColor = color
			),
			shape = RoundedCornerShape(8.dp),
			contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
		) {
			Text(
				"مشاهده جزئیات",
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Medium
			)
			Spacer(modifier = Modifier.width(8.dp))
			Icon(
				imageVector = Icons.AutoMirrored.Filled.ArrowForward,
				contentDescription = "مشاهده جزئیات",
				modifier = Modifier.size(16.dp)
			)
		}
	}
}

@Composable
private fun DetailInfoItem(
	icon: ImageVector,
	label: String,
	value: String,
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
			modifier = Modifier.size(16.dp)
		)
		
		Text(
			text = value,
			style = MaterialTheme.typography.titleSmall,
			fontWeight = FontWeight.Bold,
			color = color
		)
		
		Text(
			text = label,
			style = MaterialTheme.typography.bodySmall,
			color = color.copy(alpha = 0.7f)
		)
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
	val pagerState = rememberPagerState(pageCount = { 1 })
	val coroutineScope = rememberCoroutineScope()
	val warnings = remember(selectedShipQuotas) {
		selectedShipQuotas.mapNotNull { quota -> calculateWarningStatus(quota) }
	}

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
							userScrollEnabled = false,
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
									0 -> WarehousesAndQuotasTab(
										shipDetails = shipDetails,
										selectedShipQuotas = selectedShipQuotas,
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

data class TabItem(
	val icon: ImageVector,
	val title: String
)

@Composable
fun WarehousesAndQuotasTab(
	shipDetails: Ship,
	selectedShipQuotas: List<Quota>,
	onWarehouseSelected: (String) -> Unit,
	viewModel: ReportsViewModel
) {
	var selectedSection by remember { mutableIntStateOf(0) }
	val sections = listOf("انبارها", "کوتاژها")
	var searchQuery by remember { mutableStateOf("") }

	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		// هدر صفحه با اطلاعات کشتی
		ShipHeaderCard(shipDetails = shipDetails)
		
		// محتوای اصلی با پدینگ مناسب
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 16.dp)
		) {
			// فیلد جستجو - طراحی مینیمال و بهینه
			Spacer(modifier = Modifier.height(8.dp))
			ModernSearchField(
				searchQuery = searchQuery,
				onSearchQueryChange = { searchQuery = it },
				modifier = Modifier.fillMaxWidth()
			)
			
			Spacer(modifier = Modifier.height(8.dp))

			// تب‌های دسته‌بندی
			ModernSegmentedTabs(
				selectedTabIndex = selectedSection,
				onTabSelected = { index ->
					selectedSection = index
					searchQuery = ""
				},
				tabs = sections
			)
			
			Spacer(modifier = Modifier.height(8.dp))

			// محتوای انتخاب شده
			when (selectedSection) {
				0 -> WarehousesSection(
					warehouses = shipDetails.warehouses.filter {
						it.name.contains(searchQuery, ignoreCase = true)
					},
					onWarehouseSelected = onWarehouseSelected,
					groupingMode = viewModel.warehouseGroupingMode,
					onGroupingModeChange = viewModel::setWarehouseGroupingMode
				)
				1 -> QuotasList(
					quotas = selectedShipQuotas,
					searchQuery = searchQuery,
					groupingMode = viewModel.warehouseQuotaGroupingMode,
					onGroupingModeChange = viewModel::setWarehouseQuotaGroupingMode,
					onEdit = viewModel::editQuota,
					onToggleStatus = viewModel::toggleQuotaStatus,
					onDelete = viewModel::deleteQuota,
					viewModel = viewModel
				)
			}
		}
	}
}

@Composable
private fun ShipHeaderCard(shipDetails: Ship) {
	val loadedTonnage = shipDetails.totalTonnage - shipDetails.remainingTonnage
	val progress = calculateProgress(loadedTonnage, shipDetails.totalTonnage)
	
	Surface(
		modifier = Modifier
			.fillMaxWidth(),
		color = MaterialTheme.colorScheme.primary
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 8.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			// هدر اصلی با نام کشتی
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				// نام کشتی و آیکون
				Row(
					horizontalArrangement = Arrangement.spacedBy(12.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					// آیکون کشتی
					Surface(
						shape = CircleShape,
						color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f),
						modifier = Modifier.size(48.dp)
					) {
						Box(contentAlignment = Alignment.Center) {
							Icon(
								imageVector = Icons.Default.DirectionsBoat,
								contentDescription = null,
								tint = MaterialTheme.colorScheme.onPrimary,
								modifier = Modifier.size(24.dp)
							)
						}
					}
					
					// نام کشتی و وضعیت
					Column {
						Text(
							text = shipDetails.name,
							style = MaterialTheme.typography.headlineSmall,
							color = MaterialTheme.colorScheme.onPrimary,
							fontWeight = FontWeight.Bold
						)
						
						Text(
							text = "کشتی ${if (shipDetails.isActive) "فعال" else "غیرفعال"}",
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
						)
					}
				}
				
				// آمار کشتی
				Row(
					horizontalArrangement = Arrangement.spacedBy(12.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					// تعداد کوتاژ
					HeaderStatItem(
						icon = Icons.Default.Description,
						value = formatNumber(shipDetails.quotaCount),
						label = "کوتاژ"
					)
					
					// تعداد انبار
					HeaderStatItem(
						icon = Icons.Default.Warehouse,
						value = formatNumber(shipDetails.warehouses.size),
						label = "انبار"
					)
				}
			}
			
			// نوار پیشرفت و آمار تناژ
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 8.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				// آمار تناژ
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					// تناژ کل
					Text(
						text = "تناژ کل: ${formatNumber(shipDetails.totalTonnage.toInt())} تن",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
					)
					
					// تناژ باقی‌مانده
					Text(
						text = "مانده: ${formatNumber(shipDetails.remainingTonnage.toInt())} تن",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
					)
				}
				
				// نوار پیشرفت
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(8.dp)
						.clip(RoundedCornerShape(4.dp))
						.background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
				) {
					Box(
						modifier = Modifier
							.fillMaxWidth(progress)
							.fillMaxHeight()
							.background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
					)
				}
				
				// درصد پیشرفت
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(
						text = "${(progress * 100).roundToInt()}% تکمیل شده",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
					)
					
					Text(
						text = "${(100 - (progress * 100).roundToInt())}% باقی‌مانده",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
					)
				}
			}
		}
	}
}

@Composable
private fun HeaderStatItem(
	icon: ImageVector,
	value: String,
	label: String
) {
	Surface(
		shape = RoundedCornerShape(8.dp),
		color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)
	) {
		Row(
			modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
			horizontalArrangement = Arrangement.spacedBy(6.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				imageVector = icon,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onPrimary,
				modifier = Modifier.size(16.dp)
			)
			
			Column(horizontalAlignment = Alignment.Start) {
				Text(
					text = value,
					style = MaterialTheme.typography.titleSmall,
					color = MaterialTheme.colorScheme.onPrimary,
					fontWeight = FontWeight.Bold
				)
				
				Text(
					text = label,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
				)
			}
		}
	}
}

@Composable
private fun ModernSegmentedTabs(
	selectedTabIndex: Int,
	onTabSelected: (Int) -> Unit,
	tabs: List<String>
) {
	Surface(
		modifier = Modifier.fillMaxWidth(),
		shape = RoundedCornerShape(12.dp),
		color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
		border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
	) {
		Row(
			modifier = Modifier.padding(4.dp),
			horizontalArrangement = Arrangement.spacedBy(4.dp)
		) {
			tabs.forEachIndexed { index, title ->
				val isSelected = selectedTabIndex == index
				
				Surface(
					onClick = { onTabSelected(index) },
					modifier = Modifier.weight(1f),
					shape = RoundedCornerShape(8.dp),
					color = if (isSelected) {
						MaterialTheme.colorScheme.primary
					} else {
						Color.Transparent
					}
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
								MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
							},
							fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
						)
					}
				}
			}
		}
	}
}

@Composable
fun QuotasList(
	quotas: List<Quota>,
	searchQuery: String,
	groupingMode: StateFlow<WarehouseQuotaGroupingMode>,
	onGroupingModeChange: (WarehouseQuotaGroupingMode) -> Unit,
	onEdit: (String, QuotaEditData) -> Unit,
	onToggleStatus: (String) -> Unit,
	onDelete: (Quota) -> Unit,
	viewModel: ReportsViewModel
) {
	val currentGroupingMode by groupingMode.collectAsState()
	var expandedGroup by remember { mutableStateOf<String?>(null) }

	Column(modifier = Modifier.fillMaxSize()) {
		GroupingModeSelector(
			currentMode = currentGroupingMode,
			onModeChange = onGroupingModeChange
		)

		Spacer(modifier = Modifier.height(16.dp))

		val groupedQuotas = remember(quotas, currentGroupingMode, searchQuery) {
			quotas
				.filter { quota ->
					quota.number.contains(searchQuery, ignoreCase = true) ||
							quota.shippingCompany.contains(searchQuery, ignoreCase = true) ||
							(quota.cargoOwner?.contains(searchQuery, ignoreCase = true) == true)
				}
				.groupBy {
					when (currentGroupingMode) {
						WarehouseQuotaGroupingMode.BY_SHIPPING_COMPANY -> it.shippingCompany
						WarehouseQuotaGroupingMode.BY_CARGO_OWNER -> it.cargoOwner ?: "نامشخص"
					}
				}
				.mapValues { (_, groupQuotas) ->
					groupQuotas.sortedWith(
						compareByDescending<Quota> { it.isActive }
							.thenBy { it.remainingTonnage }
					)
				}
				.toSortedMap(compareBy { it })
		}

		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			groupedQuotas.forEach { (groupName, sortedQuotas) ->
				item {
					QuotaGroupExpansionPanel(
						groupName = groupName,
						quotas = sortedQuotas,
						currentGroupingMode = currentGroupingMode,
						isExpanded = expandedGroup == groupName,
						onExpandToggle = {
							expandedGroup = if (expandedGroup == groupName) null else groupName
						},
						onEdit = onEdit,
						onToggleStatus = onToggleStatus,
						onDelete = onDelete,
						onPercentageChange = viewModel::updateQuotaPercentage
					)
				}
			}
		}
	}
}

@Composable
fun QuotaGroupExpansionPanel(
	groupName: String,
	quotas: List<Quota>,
	currentGroupingMode: WarehouseQuotaGroupingMode,
	isExpanded: Boolean,
	onExpandToggle: () -> Unit,
	onEdit: (String, QuotaEditData) -> Unit,
	onToggleStatus: (String) -> Unit,
	onDelete: (Quota) -> Unit,
	onPercentageChange: (QuotaPercentageData) -> Unit
) {
	val loadedWeight = quotas.sumOf { it.loadedTonnage.toDouble() }
	val totalWeight = quotas.sumOf { it.totalTonnage.toDouble() }
	val remainingWeight = totalWeight - loadedWeight

	Card(
		modifier = Modifier
			.fillMaxWidth()
			.animateContentSize(),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
		),
		shape = RoundedCornerShape(12.dp),
		border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp)
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.clickable(onClick = onExpandToggle),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Row(
					horizontalArrangement = Arrangement.spacedBy(12.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					GroupIcon(currentGroupingMode)
					Column {
						Text(
							text = groupName,
							style = MaterialTheme.typography.titleMedium,
							fontWeight = FontWeight.Bold
						)
						QuotaStats(loadedWeight, remainingWeight)
					}
				}
				ExpandIcon(isExpanded)
			}

			if (isExpanded) {
				Spacer(modifier = Modifier.height(12.dp))
				HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
				Spacer(modifier = Modifier.height(12.dp))

				quotas.forEach { quota ->
					QuotaCard(
						quota = quota,
						onEdit = onEdit,
						onToggleStatus = onToggleStatus,
						onDelete = onDelete,
						onPercentageChange = onPercentageChange
					)
					Spacer(modifier = Modifier.height(8.dp))
				}
			}
		}
	}
}

@Composable
private fun GroupIcon(groupingMode: WarehouseQuotaGroupingMode) {
	val icon = when (groupingMode) {
		WarehouseQuotaGroupingMode.BY_SHIPPING_COMPANY -> Icons.Default.LocalShipping
		WarehouseQuotaGroupingMode.BY_CARGO_OWNER -> Icons.Default.Person
	}

	Surface(
		shape = CircleShape,
		color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
		modifier = Modifier.size(40.dp)
	) {
		Icon(
			imageVector = icon,
			contentDescription = null,
			tint = MaterialTheme.colorScheme.primary,
			modifier = Modifier
				.padding(8.dp)
				.size(24.dp)
		)
	}
}

@Composable
private fun QuotaStats(loadedWeight: Double, remainingWeight: Double) {
	Row(
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		StatChip(
			icon = Icons.Default.ArrowUpward,
			value = formatNumber(loadedWeight.toInt()),
			color = MaterialTheme.colorScheme.primary,
			label = "بارگیری"
		)
		StatChip(
			icon = Icons.Default.ArrowDownward,
			value = formatNumber(remainingWeight.toInt()),
			color = MaterialTheme.colorScheme.tertiary,
			label = "مانده"
		)
	}
}

@Composable
private fun StatChip(icon: ImageVector, value: String, color: Color, label: String? = null) {
	Surface(
		shape = RoundedCornerShape(16.dp),
		color = color.copy(alpha = 0.1f)
	) {
		Row(
			modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
			horizontalArrangement = Arrangement.spacedBy(4.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				imageVector = icon,
				contentDescription = null,
				tint = color,
				modifier = Modifier.size(16.dp)
			)
			if (label != null) {
				Column {
					Text(
						text = label,
						style = MaterialTheme.typography.labelSmall,
						color = color.copy(alpha = 0.7f)
					)
					Text(
						text = value,
						style = MaterialTheme.typography.bodySmall,
						color = color,
						fontWeight = FontWeight.Bold
					)
				}
			} else {
				Text(
					text = value,
					style = MaterialTheme.typography.bodySmall,
					color = color
				)
			}
		}
	}
}

@Composable
private fun ExpandIcon(isExpanded: Boolean) {
	Icon(
		imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
		contentDescription = if (isExpanded) "بستن" else "باز کردن",
		tint = MaterialTheme.colorScheme.primary
	)
}

@Composable
fun GroupingModeSelector(
	currentMode: WarehouseQuotaGroupingMode,
	onModeChange: (WarehouseQuotaGroupingMode) -> Unit
) {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 8.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
		),
		shape = RoundedCornerShape(12.dp),
		border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 12.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				Surface(
					shape = CircleShape,
					color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
					modifier = Modifier.size(36.dp)
				) {
					Box(contentAlignment = Alignment.Center) {
						Icon(
							imageVector = Icons.Default.Filter,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.primary,
							modifier = Modifier.size(20.dp)
						)
					}
				}
				Text(
					text = "نوع دسته‌بندی",
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Medium
				)
			}
			
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				Text(
					text = if (currentMode == WarehouseQuotaGroupingMode.BY_CARGO_OWNER) "صاحب کالا" else "شرکت باربری",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
				
				Switch(
					checked = currentMode == WarehouseQuotaGroupingMode.BY_CARGO_OWNER,
					onCheckedChange = { isChecked ->
						onModeChange(
							if (isChecked) WarehouseQuotaGroupingMode.BY_CARGO_OWNER
							else WarehouseQuotaGroupingMode.BY_SHIPPING_COMPANY
						)
					},
					thumbContent = {
						Icon(
							imageVector = if (currentMode == WarehouseQuotaGroupingMode.BY_CARGO_OWNER)
								Icons.Default.Person else Icons.Default.LocalShipping,
							contentDescription = null,
							modifier = Modifier.size(SwitchDefaults.IconSize)
						)
					}
				)
			}
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
			isPercentageRestricted = true
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
private fun WarehousesSection(
	warehouses: List<Warehouse>,
	onWarehouseSelected: (String) -> Unit,
	groupingMode: StateFlow<WarehouseGroupingMode>,
	onGroupingModeChange: (WarehouseGroupingMode) -> Unit
) {
	val colors = listOf(
		MaterialTheme.colorScheme.primary,
		MaterialTheme.colorScheme.secondary,
		MaterialTheme.colorScheme.tertiary,
		MaterialTheme.colorScheme.error,
		MaterialTheme.colorScheme.surfaceTint,
		MaterialTheme.colorScheme.inversePrimary
	)
	
	val currentGroupingMode by groupingMode.collectAsState()
	
	Log.d("ATK_DEBUG", "WarehousesSection: currentGroupingMode=$currentGroupingMode, warehouses count=${warehouses.size}")
	
	// گروه‌بندی انبارها بر اساس حالت انتخاب شده
	val groupedWarehouses = try {
		when (currentGroupingMode) {
			WarehouseGroupingMode.OVERALL -> {
				Log.d("ATK_DEBUG", "Grouping: OVERALL mode")
				mapOf("کلی" to warehouses)
			}
			WarehouseGroupingMode.BY_CARGO_OWNER -> {
				Log.d("ATK_DEBUG", "Grouping: BY_CARGO_OWNER mode")
				// گروه‌بندی بر اساس صاحب کالا
				val warehousesByCargoOwner = mutableMapOf<String, MutableList<Warehouse>>()
				
				// برای هر انبار، کوتاژهای آن را بررسی می‌کنیم
				warehouses.forEach { warehouse ->
					try {
						// استخراج صاحبان کالا از کوتاژهای انبار
						Log.d("ATK_DEBUG", "Processing warehouse: ${warehouse.name}, quotas: ${warehouse.quotas.size}")
						val cargoOwners = warehouse.quotas.mapNotNull { it.cargoOwner }.distinct().filter { it.isNotBlank() }
						Log.d("ATK_DEBUG", "Found cargo owners: $cargoOwners for warehouse: ${warehouse.name}")
						
						// اگر صاحب کالایی وجود نداشت، در گروه "نامشخص" قرار می‌دهیم
						if (cargoOwners.isEmpty()) {
							Log.d("ATK_DEBUG", "No cargo owners for warehouse: ${warehouse.name}, adding to 'نامشخص' group")
							warehousesByCargoOwner.getOrPut("نامشخص") { mutableListOf() }.add(warehouse)
						} else {
							// برای هر صاحب کالا، انبار را در گروه مربوطه قرار می‌دهیم
							cargoOwners.forEach { owner ->
								Log.d("ATK_DEBUG", "Adding warehouse: ${warehouse.name} to owner: $owner group")
								warehousesByCargoOwner.getOrPut(owner) { mutableListOf() }.add(warehouse)
							}
						}
					} catch (e: Exception) {
						Log.e("ATK_DEBUG", "Error processing warehouse ${warehouse.name}: ${e.message}", e)
						// در صورت بروز خطا برای یک انبار، آن را در گروه "نامشخص" قرار می‌دهیم
						warehousesByCargoOwner.getOrPut("نامشخص") { mutableListOf() }.add(warehouse)
					}
				}
				
				// اگر هیچ گروهی وجود نداشت، یک گروه پیش‌فرض ایجاد می‌کنیم
				if (warehousesByCargoOwner.isEmpty()) {
					Log.d("ATK_DEBUG", "No cargo owner groups found, creating default group")
					warehousesByCargoOwner["کلی"] = warehouses.toMutableList()
				}
				
				Log.d("ATK_DEBUG", "Final cargo owner groups: ${warehousesByCargoOwner.keys}, total groups: ${warehousesByCargoOwner.size}")
				warehousesByCargoOwner
			}
		}
	} catch (e: Exception) {
		Log.e("ATK_DEBUG", "Error in grouping warehouses: ${e.message}", e)
		// در صورت بروز هر گونه خطا، به حالت پیش‌فرض برمی‌گردیم
		mapOf("کلی" to warehouses)
	}

	Column(
		modifier = Modifier.fillMaxWidth(),
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		// سلکتور حالت دسته‌بندی
		WarehouseGroupingModeSelector(
			currentMode = currentGroupingMode,
			onModeChange = onGroupingModeChange
		)
		
		// لیست انبارها
		LazyColumn(
			verticalArrangement = Arrangement.spacedBy(12.dp),
			contentPadding = PaddingValues(vertical = 4.dp)
		) {
			groupedWarehouses.forEach { (groupName, warehousesInGroup) ->
				Log.d("ATK_DEBUG", "Rendering group: $groupName, warehouses: ${warehousesInGroup.size}")
				
				// نمایش عنوان گروه اگر در حالت دسته‌بندی بر اساس صاحب کالا باشیم
				if (currentGroupingMode == WarehouseGroupingMode.BY_CARGO_OWNER) {
					item {
						Text(
							text = groupName,
							style = MaterialTheme.typography.titleMedium,
							fontWeight = FontWeight.Bold,
							modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 4.dp)
						)
					}
				}
				
				// نمایش انبارهای گروه
				items(warehousesInGroup) { warehouse ->
					val colorIndex = warehousesInGroup.indexOf(warehouse) % colors.size
					Log.d("ATK_DEBUG", "Rendering warehouse card for: ${warehouse.name}, in group: $groupName")
					WarehouseCard(
						warehouse = warehouse,
						color = colors[colorIndex],
						onClick = { onWarehouseSelected(warehouse.name) },
						groupingMode = currentGroupingMode,
						cargoOwner = if (currentGroupingMode == WarehouseGroupingMode.BY_CARGO_OWNER) groupName else null
					)
				}
			}
		}
	}
}

@Composable
private fun WarehouseGroupingModeSelector(
	currentMode: WarehouseGroupingMode,
	onModeChange: (WarehouseGroupingMode) -> Unit
) {
	Log.d("ATK_DEBUG", "WarehouseGroupingModeSelector: current mode = $currentMode")
	
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 8.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
		),
		shape = RoundedCornerShape(12.dp),
		border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				Surface(
					shape = CircleShape,
					color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
					modifier = Modifier.size(24.dp)
				) {
					Box(contentAlignment = Alignment.Center) {
						Icon(
							imageVector = Icons.Default.Filter,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.primary,
							modifier = Modifier.size(16.dp)
						)
					}
				}
				Text(
					text = "نوع دسته‌بندی",
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Medium
				)
			}
			
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				Text(
					text = if (currentMode == WarehouseGroupingMode.BY_CARGO_OWNER) "صاحب کالا" else "کلی",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
				
				Switch(
					checked = currentMode == WarehouseGroupingMode.BY_CARGO_OWNER,
					onCheckedChange = { isChecked ->
						Log.d("ATK_DEBUG", "Mode switch changed to: ${if (isChecked) "BY_CARGO_OWNER" else "OVERALL"}")
						onModeChange(
							if (isChecked) WarehouseGroupingMode.BY_CARGO_OWNER
							else WarehouseGroupingMode.OVERALL
						)
					},
					thumbContent = {
						Icon(
							imageVector = if (currentMode == WarehouseGroupingMode.BY_CARGO_OWNER)
								Icons.Default.Person else Icons.AutoMirrored.Filled.ViewList,
							contentDescription = null,
							modifier = Modifier.size(SwitchDefaults.IconSize)
						)
					}
				)
			}
		}
	}
}

// تابع محاسبه اطلاعات تناژی بر اساس صاحب کالا
private fun calculateTonnageByCargoOwner(warehouse: Warehouse, cargoOwner: String?): Triple<Float, Float, Float> {
	Log.d("ATK_DEBUG", "calculateTonnageByCargoOwner for warehouse: ${warehouse.name}, cargoOwner: $cargoOwner")
	
	// اگر حالت کلی باشد یا cargoOwner برابر با "کلی" باشد، از اطلاعات کلی انبار استفاده می‌کنیم
	if (cargoOwner == null || cargoOwner == "کلی" || cargoOwner == "نامشخص") {
		Log.d("ATK_DEBUG", "cargoOwner is null or 'کلی' or 'نامشخص', using warehouse overall tonnage: total=${warehouse.totalTonnage}, loaded=${warehouse.loadedTonnage}, remaining=${warehouse.remainingTonnage}")
		return Triple(warehouse.totalTonnage, warehouse.loadedTonnage, warehouse.remainingTonnage)
	}
	
	try {
		// فیلتر کردن کوتاژهای مربوط به صاحب کالای مورد نظر
		val ownerQuotas = warehouse.quotas.filter { it.cargoOwner == cargoOwner }
		Log.d("ATK_DEBUG", "Filtered quotas for owner: $cargoOwner, found: ${ownerQuotas.size} quotas out of ${warehouse.quotas.size}")
		
		// اگر هیچ کوتاژی برای این صاحب کالا پیدا نشد، از اطلاعات کلی استفاده می‌کنیم
		if (ownerQuotas.isEmpty()) {
			Log.d("ATK_DEBUG", "No quotas found for owner: $cargoOwner, using warehouse overall tonnage")
			return Triple(0f, 0f, 0f)
		}
		
		// محاسبه مجموع تناژها
		val totalTonnage = ownerQuotas.sumOf { it.totalTonnage.toDouble() }.toFloat()
		val loadedTonnage = ownerQuotas.sumOf { it.loadedTonnage.toDouble() }.toFloat()
		val remainingTonnage = ownerQuotas.sumOf { it.remainingTonnage.toDouble() }.toFloat()
		
		Log.d("ATK_DEBUG", "Calculated tonnage for owner: $cargoOwner - total: $totalTonnage, loaded: $loadedTonnage, remaining: $remainingTonnage")
		
		return Triple(totalTonnage, loadedTonnage, remainingTonnage)
	} catch (e: Exception) {
		Log.e("ATK_DEBUG", "Error in calculateTonnageByCargoOwner: ${e.message}", e)
		// در صورت بروز هر گونه خطا، مقادیر صفر برمی‌گردانیم تا از کرش جلوگیری شود
		return Triple(0f, 0f, 0f)
	}
}

@Composable
private fun WarehouseCard(
	warehouse: Warehouse,
	color: Color,
	onClick: () -> Unit,
	groupingMode: WarehouseGroupingMode = WarehouseGroupingMode.OVERALL,
	cargoOwner: String? = null
) {
	Log.d("ATK_DEBUG", "WarehouseCard for: ${warehouse.name}, groupingMode: $groupingMode, cargoOwner: $cargoOwner")
	
	// محاسبه اطلاعات تناژی بر اساس حالت دسته‌بندی
	var triple: Triple<Float, Float, Float> = Triple(0f, 0f, 0f)
	try {
		triple = if (groupingMode == WarehouseGroupingMode.BY_CARGO_OWNER && cargoOwner != null && cargoOwner != "نامشخص" && cargoOwner != "کلی") {
			calculateTonnageByCargoOwner(warehouse, cargoOwner)
		} else {
			// حالت کلی
			Log.d("ATK_DEBUG", "Using overall tonnage for warehouse: ${warehouse.name}")
			Triple(warehouse.totalTonnage, warehouse.loadedTonnage, warehouse.remainingTonnage)
		}
	} catch (e: Exception) {
		Log.e("ATK_DEBUG", "Error calculating tonnage: ${e.message}", e)
	}
	
	val (totalTonnage, loadedTonnage, remainingTonnage) = triple
	
	Log.d("ATK_DEBUG", "Final tonnage values for warehouse card: ${warehouse.name} - total: $totalTonnage, loaded: $loadedTonnage, remaining: $remainingTonnage")

		Card(
			modifier = Modifier
				.fillMaxWidth()
				.clickable(onClick = onClick),
			colors = CardDefaults.cardColors(
				containerColor = MaterialTheme.colorScheme.surface
			),
			border = BorderStroke(1.dp, color.copy(alpha = 0.15f)),
			shape = RoundedCornerShape(12.dp),
			elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
		) {
			Column(
				modifier = Modifier.padding(12.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				// هدر کارت - طراحی مینیمال‌تر
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					// نام انبار و آیکون
					Row(
						horizontalArrangement = Arrangement.spacedBy(8.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						// آیکون انبار
						Surface(
							shape = CircleShape,
							color = color.copy(alpha = 0.1f),
							modifier = Modifier.size(32.dp)
						) {
							Box(contentAlignment = Alignment.Center) {
								Icon(
									imageVector = Icons.Default.Warehouse,
									contentDescription = null,
									tint = color,
									modifier = Modifier.size(16.dp)
								)
							}
						}
						
						// نام انبار
						Text(
							text = warehouse.name,
							style = MaterialTheme.typography.titleMedium,
							color = MaterialTheme.colorScheme.onSurface,
							fontWeight = FontWeight.Medium
						)
					}
					
					// تعداد کوتاژ
					Badge(
						containerColor = color.copy(alpha = 0.1f),
						contentColor = color
					) {
						// اگر در حالت صاحب کالا هستیم، فقط کوتاژهای مربوط به آن صاحب کالا را نمایش دهیم
						val quotaCount = if (groupingMode == WarehouseGroupingMode.BY_CARGO_OWNER && cargoOwner != null && cargoOwner != "نامشخص") {
							val count = warehouse.quotas.count { it.cargoOwner == cargoOwner }
							Log.d("ATK_DEBUG", "Filtered quota count for owner: $cargoOwner in warehouse: ${warehouse.name} = $count")
							count
						} else {
							Log.d("ATK_DEBUG", "Using overall quota count for warehouse: ${warehouse.name} = ${warehouse.quotaCount}")
							warehouse.quotaCount
						}
						
						Text(
							text = "$quotaCount کوتاژ",
							style = MaterialTheme.typography.bodySmall,
							modifier = Modifier.padding(horizontal = 4.dp)
						)
					}
				}
				
				// نوار پیشرفت
				val progress = if (totalTonnage > 0) loadedTonnage / totalTonnage else 0f
				Log.d("ATK_DEBUG", "Progress bar calculation: loaded=$loadedTonnage / total=$totalTonnage = $progress")
				
				LinearProgressIndicator(
					progress = { progress },
					modifier = Modifier
						.fillMaxWidth()
						.height(4.dp),
					color = color,
					trackColor = color.copy(alpha = 0.1f)
				)
				
				// آمار تناژ - طراحی مینیمال‌تر
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					// تناژ بارگیری شده
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(4.dp)
					) {
						Icon(
							imageVector = Icons.Default.Inventory,
							contentDescription = null,
							tint = color,
							modifier = Modifier.size(14.dp)
						)
						Text(
							text = "${formatNumber(loadedTonnage.toInt())} تن بارگیری",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
					
					// تناژ مانده
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(4.dp)
					) {
						Icon(
							imageVector = Icons.Default.Store,
							contentDescription = null,
							tint = color,
							modifier = Modifier.size(14.dp)
						)
						Text(
							text = "${formatNumber(remainingTonnage.toInt())} تن مانده",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				}
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
						shipName = shipName
					)

					Spacer(modifier = Modifier.height(16.dp))

						Card(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 8.dp, vertical = 4.dp),
		shape = RoundedCornerShape(12.dp)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			// عنوان با آیکون
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				Surface(
					shape = CircleShape,
					color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
					modifier = Modifier.size(32.dp)
				) {
					Box(contentAlignment = Alignment.Center) {
						Icon(
							imageVector = Icons.Default.Filter,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.primary,
							modifier = Modifier.size(16.dp)
						)
					}
				}
				
			Text(
					text = "فیلتر و جستجو",
				style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onSurface
			)
			}
			
			// دکمه خروجی
						ExportOptions(
							onExport = { format ->
								filteredSummary?.let {
									onExport(format)
								}
							}
						)
		}
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
						VoucherDetailsButton(summary)
					}
				}
			}
		}
	}
}

@Composable
fun ExportOptions(onExport: (String) -> Unit) {
	Surface(
			onClick = { onExport("pdf") },
		shape = RoundedCornerShape(20.dp),
		color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
		border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
	) {
		Row(
			modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
			Icon(
				imageVector = Icons.Default.PictureAsPdf,
				contentDescription = "خروجی PDF",
				tint = MaterialTheme.colorScheme.tertiary,
				modifier = Modifier.size(18.dp)
			)
			
					Text(
				text = "خروجی PDF",
				style = MaterialTheme.typography.bodyMedium.copy(
					fontWeight = FontWeight.Medium
				),
				color = MaterialTheme.colorScheme.tertiary
			)
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
	
	val interactionSource = remember { MutableInteractionSource() }
	val isHovered by interactionSource.collectIsHoveredAsState()

	Column(modifier = modifier) {
		// عنوان با طراحی مینیمال
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(6.dp),
			modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
		) {
			Icon(
				imageVector = if (selectedDateTime != null) {
					Icons.Default.Schedule
				} else {
					Icons.Default.DateRange
				},
				contentDescription = null,
				tint = MaterialTheme.colorScheme.tertiary,
				modifier = Modifier.size(14.dp)
			)
			
			Text(
				text = label,
				style = MaterialTheme.typography.titleSmall.copy(
					fontWeight = FontWeight.Medium,
					fontSize = MaterialTheme.typography.bodySmall.fontSize
				),
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
		
		// کامپوننت تاریخ و زمان مدرن و مینیمال برای فارسی
		Surface(
			onClick = { showDatePicker = true },
			modifier = Modifier
				.fillMaxWidth()
				.hoverable(interactionSource),
			shape = RoundedCornerShape(12.dp),
			color = if (isHovered || selectedDateTime != null) {
				MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f)
			} else {
				MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
			},
			border = BorderStroke(
				width = if (selectedDateTime != null) 1.5.dp else 1.dp,
				color = if (selectedDateTime != null) {
					MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
				} else if (isHovered) {
					MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
				} else {
					MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
				}
			)
		) {
			Row(
				modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				// آیکون کوچک‌تر تاریخ/زمان با انیمیشن رنگ
				val iconTint by animateColorAsState(
					targetValue = if (selectedDateTime != null) {
						MaterialTheme.colorScheme.tertiary
					} else if (isHovered) {
						MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
					} else {
						MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
					},
					label = "icon color"
				)
				
				Icon(
					imageVector = if (selectedDateTime != null) {
						Icons.Default.Schedule
					} else {
						Icons.Default.DateRange
					},
					contentDescription = null,
					tint = iconTint,
					modifier = Modifier.size(18.dp)
				)
				
				// نمایش تاریخ و زمان با فرمت فارسی بهینه‌شده
				Text(
					text = selectedDateTime?.let { persianDateTimeFormat(it) } 
						?: "انتخاب ${label.lowercase()}",
					style = MaterialTheme.typography.bodyMedium,
					color = if (selectedDateTime != null) {
						MaterialTheme.colorScheme.onSurface
					} else {
						MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
					},
					modifier = Modifier.weight(1f)
				)
				
				// دکمه پاک کردن مینیمال با انیمیشن
				if (selectedDateTime != null) {
					Surface(
						onClick = { onDateTimeSelected(null) },
						shape = CircleShape,
						color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
						modifier = Modifier.size(24.dp)
					) {
						Box(contentAlignment = Alignment.Center) {
							Icon(
								imageVector = Icons.Default.Close,
								contentDescription = "پاک کردن",
								tint = MaterialTheme.colorScheme.tertiary,
								modifier = Modifier.size(14.dp)
							)
						}
					}
				}
			}
		}
	}

	// دیالوگ انتخاب تاریخ با طراحی بهینه‌شده برای فارسی
	if (showDatePicker) {
		Dialog(
			onDismissRequest = { showDatePicker = false },
			properties = DialogProperties(usePlatformDefaultWidth = false)
		) {
			Surface(
				modifier = Modifier
					.fillMaxWidth(0.9f)
					.wrapContentHeight(),
				shape = RoundedCornerShape(28.dp),
				color = MaterialTheme.colorScheme.surface,
				tonalElevation = 6.dp
			) {
				Column(modifier = Modifier.padding(vertical = 20.dp, horizontal = 16.dp)) {
					// هدر دیالوگ مدرن
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.CenterVertically
					) {
						// عنوان با آیکون
						Row(
							verticalAlignment = Alignment.CenterVertically,
							horizontalArrangement = Arrangement.spacedBy(12.dp)
						) {
							Surface(
								shape = CircleShape,
								color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
								modifier = Modifier.size(36.dp)
							) {
								Box(contentAlignment = Alignment.Center) {
									Icon(
										imageVector = Icons.Default.DateRange,
										contentDescription = null,
										tint = MaterialTheme.colorScheme.tertiary,
										modifier = Modifier.size(18.dp)
									)
								}
							}
							
							Text(
								text = "انتخاب تاریخ",
								style = MaterialTheme.typography.titleLarge,
								fontWeight = FontWeight.Bold
							)
						}
						
						// دکمه بستن مینیمال
						Surface(
							onClick = { showDatePicker = false },
							shape = CircleShape,
							color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
							modifier = Modifier.size(32.dp)
						) {
							Box(contentAlignment = Alignment.Center) {
								Icon(
									imageVector = Icons.Default.Close,
									contentDescription = "بستن",
									tint = MaterialTheme.colorScheme.onSurfaceVariant,
									modifier = Modifier.size(16.dp)
								)
							}
						}
					}
					
					Spacer(modifier = Modifier.height(16.dp))
					HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
					Spacer(modifier = Modifier.height(16.dp))
					
					// لیست تاریخ‌ها
					if (availableDates.isEmpty()) {
						Box(
								modifier = Modifier
									.fillMaxWidth()
								.height(120.dp),
							contentAlignment = Alignment.Center
						) {
							Text(
								text = "تاریخی موجود نیست",
								style = MaterialTheme.typography.bodyLarge,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					} else {
						LazyColumn(
							modifier = Modifier.heightIn(max = 300.dp),
							verticalArrangement = Arrangement.spacedBy(8.dp)
						) {
							items(availableDates) { date ->
								PersianDateItem(
									date = date,
									onClick = {
										tempDate = date
										showDatePicker = false
										showTimePicker = true
									}
								)
							}
						}
					}
				}
			}
		}
	}

	// دیالوگ انتخاب زمان
	if (showTimePicker) {
		val timePickerState = rememberTimePickerState()
		ModernTimePickerDialog(
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
fun PersianDateItem(
	date: String,
	onClick: () -> Unit
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isPressed by interactionSource.collectIsPressedAsState()
	
	Surface(
		onClick = onClick,
		modifier = Modifier.fillMaxWidth(),
		shape = RoundedCornerShape(12.dp),
		color = if (isPressed) {
			MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
		} else {
			MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
		},
		interactionSource = interactionSource
	) {
		Row(
		modifier = Modifier
			.fillMaxWidth()
				.padding(vertical = 12.dp, horizontal = 16.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Icon(
				imageVector = Icons.Default.DateRange,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.primary,
				modifier = Modifier.size(20.dp)
			)
			
			Text(
				text = persianDateFormat(date),
				style = MaterialTheme.typography.bodyLarge,
				fontWeight = FontWeight.Medium,
				color = MaterialTheme.colorScheme.onSurface
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTimePickerDialog(
	onCancel: () -> Unit,
	onConfirm: () -> Unit,
	timePickerState: TimePickerState
) {
	Dialog(
		onDismissRequest = onCancel,
		properties = DialogProperties(usePlatformDefaultWidth = false)
	) {
		Surface(
			modifier = Modifier
				.fillMaxWidth(0.9f)
				.wrapContentHeight(),
			shape = RoundedCornerShape(24.dp),
			color = MaterialTheme.colorScheme.surface,
			tonalElevation = 6.dp
		) {
			Column(
				modifier = Modifier.padding(24.dp),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				// هدر دیالوگ
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
						text = "انتخاب زمان",
					style = MaterialTheme.typography.titleLarge,
						fontWeight = FontWeight.Bold
					)
					
					IconButton(
						onClick = onCancel,
						modifier = Modifier
							.size(36.dp)
							.background(
								MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
								CircleShape
							)
					) {
						Icon(
							imageVector = Icons.Default.Close,
							contentDescription = "بستن",
							tint = MaterialTheme.colorScheme.primary,
							modifier = Modifier.size(18.dp)
						)
					}
				}
				
				Spacer(modifier = Modifier.height(24.dp))
				
				// نمایش زمان انتخاب شده
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.background(
							MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
							RoundedCornerShape(16.dp)
						)
						.padding(vertical = 16.dp),
					horizontalArrangement = Arrangement.Center,
					verticalAlignment = Alignment.CenterVertically
				) {
					// نمایش زمان به صورت RTL
					Text(
						text = String.format("%02d", timePickerState.minute),
						style = MaterialTheme.typography.displayMedium,
						color = MaterialTheme.colorScheme.primary,
						fontWeight = FontWeight.Bold
					)
					Text(
						text = ":",
						style = MaterialTheme.typography.displayMedium,
						color = MaterialTheme.colorScheme.primary,
					fontWeight = FontWeight.Bold,
						modifier = Modifier.padding(horizontal = 8.dp)
					)
					Text(
						text = String.format("%02d", timePickerState.hour),
						style = MaterialTheme.typography.displayMedium,
						color = MaterialTheme.colorScheme.primary,
						fontWeight = FontWeight.Bold
					)
				}

				Spacer(modifier = Modifier.height(16.dp))
				
				TimePicker(state = timePickerState)
				
				Spacer(modifier = Modifier.height(24.dp))
				
				// دکمه‌های تایید و لغو
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(12.dp)
				) {
					// دکمه لغو
					Surface(
						onClick = onCancel,
						modifier = Modifier.weight(1f),
						shape = RoundedCornerShape(12.dp),
						color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
					) {
						Text(
							text = "لغو",
							style = MaterialTheme.typography.bodyLarge,
							fontWeight = FontWeight.Medium,
							modifier = Modifier
								.fillMaxWidth()
								.padding(vertical = 12.dp),
							textAlign = TextAlign.Center
						)
					}
					
					// دکمه تایید
					Surface(
						onClick = onConfirm,
						modifier = Modifier.weight(1f),
						shape = RoundedCornerShape(12.dp),
					color = MaterialTheme.colorScheme.primary
					) {
						Text(
							text = "تایید",
							style = MaterialTheme.typography.bodyLarge,
							fontWeight = FontWeight.Medium,
							color = MaterialTheme.colorScheme.onPrimary,
							modifier = Modifier
								.fillMaxWidth()
								.padding(vertical = 12.dp),
							textAlign = TextAlign.Center
						)
					}
				}
			}
		}
	}
}

// تبدیل تاریخ میلادی به قالب شمسی بهتر
fun persianDateFormat(dateString: String): String {
	// این تابع می‌تواند با کد واقعی تبدیل تاریخ جایگزین شود
	// در اینجا فقط قالب نمایش تاریخ را بهبود می‌دهیم
	return dateString
}

// تبدیل تاریخ و زمان به قالب شمسی بهتر
fun persianDateTimeFormat(dateTimeString: String): String {
	// این تابع می‌تواند با کد واقعی تبدیل تاریخ و زمان جایگزین شود
	val parts = dateTimeString.split(" ")
	return if (parts.size > 1) {
		"${persianDateFormat(parts[0])} ساعت ${parts[1]}"
	} else {
		dateTimeString
	}
}

@Composable
fun WarehouseMainCard(warehouseName: String, shipName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header - کامپکت و مینیمال
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // عنوان و نشانگر
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Warehouse,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    Text(
                        text = "اطلاعات کشتی و انبار",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // نشانگر تعداد آیتم‌ها
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = "2 مورد",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // کارت‌های اطلاعاتی مینیمال با طراحی افقی
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // کارت کشتی - طراحی افقی و مینیمال
                MinimalInfoCard(
                    icon = Icons.Default.DirectionsBoat,
                    title = "کشتی",
                    value = shipName,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                
                // کارت انبار - طراحی افقی و مینیمال
                MinimalInfoCard(
                    icon = Icons.Default.Store, 
                    title = "انبار",
                    value = warehouseName,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MinimalInfoCard(
    icon: ImageVector,
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // آیکون
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            // محتوا
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = color
                )
                
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun QuotaSelector(
    quotas: List<Quota>,
    selectedQuota: String?,
    onQuotaSelected: (Quota) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // هدر با عنوان و تعداد کوتاژ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // عنوان بخش
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    
                    Text(
                        text = "انتخاب شماره کوتاژ",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // نمایش تعداد کوتاژها
                Text(
                    text = "${quotas.size} کوتاژ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // لیست کوتاژها با اسکرول افقی
            if (quotas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "کوتاژی یافت نشد",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(quotas) { quota ->
                        MinimalQuotaChip(
                            quota = quota,
                            isSelected = selectedQuota == quota.number,
                            onSelect = { onQuotaSelected(quota) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MinimalQuotaChip(
    quota: Quota,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isPressed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        label = "background color"
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        label = "content color"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isPressed || isSelected) 0.dp else 1.dp,
        label = "elevation animation"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale animation"
    )
    
    Surface(
        onClick = onSelect,
        modifier = Modifier
            .height(36.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(18.dp),
        color = backgroundColor,
        tonalElevation = elevation,
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // نمایش نشانگر فعال/غیرفعال بودن کوتاژ
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (quota.isActive) {
                            if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.tertiary
                        } else {
                            if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        },
                        shape = CircleShape
                    )
            )
            
            // شماره کوتاژ
            Text(
                text = quota.number,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = contentColor
            )
        }
    }
}

@Composable
fun VoucherDetailsButton(summary: FilteredSummary) {
	var showVoucherDetailsDialog by remember { mutableStateOf(false) }
	
	Card(
		onClick = { showVoucherDetailsDialog = true },
		modifier = Modifier
		    .fillMaxWidth()
		    .padding(horizontal = 8.dp, vertical = 4.dp),
		shape = RoundedCornerShape(16.dp),
		colors = CardDefaults.cardColors(
		    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
		),
	) {
		Row(
			modifier = Modifier.padding(16.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			// اطلاعات اصلی
			Row(
			    horizontalArrangement = Arrangement.spacedBy(12.dp),
			    verticalAlignment = Alignment.CenterVertically,
			    modifier = Modifier.weight(1f)
			) {
				// آیکون
				Surface(
					shape = CircleShape,
					color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
					modifier = Modifier.size(40.dp)
				) {
					Box(contentAlignment = Alignment.Center) {
						Icon(
							imageVector = Icons.Default.Description,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.tertiary,
							modifier = Modifier.size(20.dp)
						)
					}
				}
				
				// اطلاعات و آمار
				Column {
					Text(
						text = "مشاهده جزئیات حواله‌ها",
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.onTertiaryContainer
					)
					
					Row(
						horizontalArrangement = Arrangement.spacedBy(8.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						// تعداد حواله‌ها
						Text(
							text = "${formatNumber(summary.voucherCount)} حواله",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
						)
						
						// جداکننده
						Box(
							modifier = Modifier
								.size(3.dp)
								.background(MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.4f), CircleShape)
						)
						
						// وزن کل
						Text(
							text = "${formatNumber(summary.totalNetWeight.toInt())} کیلوگرم",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
						)
					}
				}
			}
			
			// آیکون نمایش جزئیات و انیمیشن
			Surface(
			    shape = CircleShape,
			    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
			    modifier = Modifier.size(32.dp)
			) {
				Box(contentAlignment = Alignment.Center) {
					Icon(
						imageVector = Icons.AutoMirrored.Filled.ArrowForward,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.tertiary,
						modifier = Modifier.size(18.dp)
					)
				}
			}
		}
	}
	
	// دیالوگ نمایش جزئیات حواله‌ها
	if (showVoucherDetailsDialog) {
		VoucherDetailsDialog(
			summary = summary,
			onDismiss = { showVoucherDetailsDialog = false }
		)
	}
}

@Composable
fun VoucherDetailsDialog(
	summary: FilteredSummary,
	onDismiss: () -> Unit
) {
	var searchQuery by remember { mutableStateOf("") }
	var sortType by remember { mutableStateOf(VoucherSortType.DATE_DESC) }
	
	val filteredVoucherDetails by remember(searchQuery, sortType, summary.voucherDetails) {
		derivedStateOf {
			// ابتدا فیلتر کردن
			val filtered = summary.voucherDetails.filter { voucher ->
				val matchesSearch = if (searchQuery.isEmpty()) {
					true
				} else {
					voucher.trackingNumber.contains(searchQuery, ignoreCase = true) ||
							voucher.scaleReceiptNumber.contains(searchQuery, ignoreCase = true) ||
							voucher.exitDate.contains(searchQuery, ignoreCase = true) ||
                            voucher.username?.contains(searchQuery, ignoreCase = true) == true ||
                            voucher.confirmUsername?.contains(searchQuery, ignoreCase = true) == true
				}
				
				matchesSearch
			}
			
			// مرتب‌سازی
			when (sortType) {
				VoucherSortType.DATE_ASC -> filtered.sortedBy { it.exitDate }
				VoucherSortType.DATE_DESC -> filtered.sortedByDescending { it.exitDate }
				VoucherSortType.WEIGHT_ASC -> filtered.sortedBy { it.netWeight }
				VoucherSortType.WEIGHT_DESC -> filtered.sortedByDescending { it.netWeight }
			}
		}
	}
	
	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(
			usePlatformDefaultWidth = false,
			dismissOnBackPress = true,
			dismissOnClickOutside = true
		)
	) {
		Surface(
			modifier = Modifier
				.fillMaxWidth(0.95f)
				.fillMaxHeight(0.9f),
			shape = RoundedCornerShape(28.dp),
			color = MaterialTheme.colorScheme.surface,
			tonalElevation = 4.dp
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(20.dp)
			) {
				// هدر با طراحی مینیمال
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					// عنوان با آیکون و نمایش تعداد
					Row(
						horizontalArrangement = Arrangement.spacedBy(12.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						Surface(
							shape = CircleShape,
							color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
							modifier = Modifier.size(40.dp)
						) {
							Box(contentAlignment = Alignment.Center) {
								Icon(
									imageVector = Icons.Default.Description,
									contentDescription = null,
									tint = MaterialTheme.colorScheme.tertiary,
									modifier = Modifier.size(20.dp)
								)
							}
						}
						
						Column {
							Text(
								text = "جزئیات حواله‌ها",
								style = MaterialTheme.typography.titleLarge,
								fontWeight = FontWeight.Bold
							)
							
							// نمایش آمار فیلتر شده با طراحی بهتر
							Row(
								verticalAlignment = Alignment.CenterVertically,
								horizontalArrangement = Arrangement.spacedBy(4.dp)
							) {
								Text(
									text = "${formatNumber(filteredVoucherDetails.size)} از ${formatNumber(summary.voucherCount)}",
									style = MaterialTheme.typography.labelMedium,
									fontWeight = FontWeight.Medium,
									color = MaterialTheme.colorScheme.tertiary
								)
								
								Text(
									text = "حواله",
									style = MaterialTheme.typography.labelMedium,
									color = MaterialTheme.colorScheme.onSurfaceVariant
								)
								
								// نمایش وضعیت فیلتر
								if (searchQuery.isNotEmpty()) {
									Surface(
										shape = RoundedCornerShape(4.dp),
										color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
										modifier = Modifier.padding(start = 4.dp)
									) {
										Text(
											text = "فیلتر شده",
											style = MaterialTheme.typography.labelSmall,
											color = MaterialTheme.colorScheme.tertiary,
											modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
										)
									}
								}
							}
						}
					}
					
					// دکمه بستن با طراحی بهتر
					Surface(
						onClick = onDismiss,
						shape = CircleShape,
						color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
						modifier = Modifier.size(36.dp)
					) {
						Box(contentAlignment = Alignment.Center) {
							Icon(
								imageVector = Icons.Default.Close,
								contentDescription = "بستن",
								tint = MaterialTheme.colorScheme.onSurfaceVariant,
								modifier = Modifier.size(18.dp)
							)
						}
					}
				}
				
				Spacer(modifier = Modifier.height(20.dp))
				
				// جستجو و فیلترها - کامپکت و مینیمال
				VoucherSearchAndFilter(
					searchQuery = searchQuery,
					onSearchChange = { searchQuery = it },
					sortType = sortType,
					onSortTypeChange = { sortType = it }
				)
				
				Spacer(modifier = Modifier.height(12.dp))
				
				// لیست حواله‌ها
				if (filteredVoucherDetails.isEmpty()) {
					EmptyVoucherList()
				} else {
					// تیتر لیست با تعداد آیتم‌ها
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.padding(vertical = 8.dp, horizontal = 4.dp),
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.CenterVertically
					) {
						Text(
							text = "لیست حواله‌ها",
							style = MaterialTheme.typography.titleSmall,
							fontWeight = FontWeight.Medium,
							color = MaterialTheme.colorScheme.onSurface
						)
						
						// نمایش اطلاعات مرتب‌سازی
						Row(
							verticalAlignment = Alignment.CenterVertically,
							horizontalArrangement = Arrangement.spacedBy(4.dp)
						) {
							Icon(
								imageVector = if (sortType == VoucherSortType.DATE_ASC || sortType == VoucherSortType.WEIGHT_ASC) 
									Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
								contentDescription = null,
								tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
								modifier = Modifier.size(14.dp)
							)
							
							Text(
								text = "مرتب‌سازی: ${sortType.persianName}",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
							)
						}
					}
					
					// لیست حواله‌ها با فاصله‌گذاری بهتر
					LazyColumn(
						modifier = Modifier.weight(1f),
						verticalArrangement = Arrangement.spacedBy(8.dp),
						contentPadding = PaddingValues(bottom = 16.dp, top = 4.dp)
					) {
						items(filteredVoucherDetails) { voucher ->
							ModernVoucherItem(voucher)
						}
					}
				}
			}
		}
	}
}

@Composable
fun VoucherSearchAndFilter(
	searchQuery: String,
	onSearchChange: (String) -> Unit,
	sortType: VoucherSortType,
	onSortTypeChange: (VoucherSortType) -> Unit
) {
	Column(
		modifier = Modifier.fillMaxWidth(),
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		// جستجو - طراحی مینیمال
		SearchTextField(
			value = searchQuery,
			onValueChange = onSearchChange,
			placeholder = "جستجو در حواله‌ها..."
		)
		
		// نوار فیلترها - طراحی منظم و کارآمد
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			// دکمه‌های مرتب‌سازی - طراحی مینیمال با نمایش در دو ردیف
			Column(
				verticalArrangement = Arrangement.spacedBy(8.dp),
				modifier = Modifier.weight(1f)
			) {
				// ردیف اول - گزینه‌های تاریخ
				Row(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					modifier = Modifier.fillMaxWidth()
				) {
					listOf(VoucherSortType.DATE_DESC, VoucherSortType.DATE_ASC).forEach { sortOption ->
						MinimalSortChip(
							type = sortOption,
							isSelected = sortType == sortOption,
							onClick = { onSortTypeChange(sortOption) },
							modifier = Modifier.weight(1f)
						)
					}
				}
				
				// ردیف دوم - گزینه‌های وزن
				Row(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					modifier = Modifier.fillMaxWidth()
				) {
					listOf(VoucherSortType.WEIGHT_DESC, VoucherSortType.WEIGHT_ASC).forEach { sortOption ->
						MinimalSortChip(
							type = sortOption,
							isSelected = sortType == sortOption,
							onClick = { onSortTypeChange(sortOption) },
							modifier = Modifier.weight(1f)
						)
					}
				}
			}
		}
	}
}

@Composable
fun SearchTextField(
	value: String,
	onValueChange: (String) -> Unit,
	placeholder: String,
	modifier: Modifier = Modifier
) {
	var isFocused by remember { mutableStateOf(false) }
	
	Surface(
		modifier = modifier.fillMaxWidth(),
		shape = RoundedCornerShape(12.dp),
		color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
		border = BorderStroke(
			width = if (isFocused) 1.5.dp else 1.dp,
			color = if (isFocused) {
				MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
			} else {
				MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
			}
		)
	) {
		BasicTextField(
			value = value,
			onValueChange = onValueChange,
			singleLine = true,
			cursorBrush = SolidColor(MaterialTheme.colorScheme.tertiary),
			textStyle = MaterialTheme.typography.bodyMedium.copy(
				color = MaterialTheme.colorScheme.onSurface,
				textDirection = TextDirection.Rtl
			),
			modifier = Modifier
				.fillMaxWidth()
				.onFocusChanged { isFocused = it.isFocused },
			decorationBox = { innerTextField ->
				Row(
					modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					// آیکون جستجو
					Icon(
						imageVector = Icons.Default.Search,
						contentDescription = null,
						tint = if (isFocused) {
							MaterialTheme.colorScheme.tertiary
						} else {
							MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
						},
						modifier = Modifier.size(20.dp)
					)
					
					// فاصله
					Spacer(modifier = Modifier.width(12.dp))
					
					Box(
						modifier = Modifier.weight(1f),
						contentAlignment = Alignment.CenterStart
					) {
						// متن پلیس‌هولدر
						if (value.isEmpty()) {
							Text(
								text = placeholder,
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
							)
						}
						
						// فیلد ورودی
						innerTextField()
					}
					
					// دکمه پاک کردن
					if (value.isNotEmpty()) {
						IconButton(
							onClick = { onValueChange("") },
							modifier = Modifier.size(20.dp)
						) {
							Icon(
								imageVector = Icons.Default.Clear,
								contentDescription = "پاک کردن",
								tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
								modifier = Modifier.size(16.dp)
							)
						}
					}
				}
			}
		)
	}
}

@Composable
fun EmptyVoucherList() {
	Box(
			modifier = Modifier
				.fillMaxWidth()
			.aspectRatio(1f),
		contentAlignment = Alignment.Center
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			Icon(
				imageVector = Icons.Default.Inventory,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
				modifier = Modifier.size(80.dp)
			)
			
			Spacer(modifier = Modifier.height(16.dp))
			
				Text(
				text = "حواله‌ای یافت نشد",
				style = MaterialTheme.typography.titleLarge,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			
			Spacer(modifier = Modifier.height(8.dp))
			
				Text(
				text = "لطفا فیلترها را تغییر دهید یا جستجوی دیگری انجام دهید",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
				textAlign = TextAlign.Center
			)
		}
	}
}

@Composable
fun ModernVoucherItem(voucher: VoucherDetail) {
	var expanded by remember { mutableStateOf(false) }
	val rotationState by animateFloatAsState(
		targetValue = if (expanded) 180f else 0f,
		animationSpec = tween(durationMillis = 300),
		label = "rotation"
	)

	Card(
		onClick = { expanded = !expanded },
		modifier = Modifier
			.fillMaxWidth()
			.animateContentSize(
				animationSpec = spring(
					dampingRatio = Spring.DampingRatioMediumBouncy,
					stiffness = Spring.StiffnessLow
				)
			),
		shape = RoundedCornerShape(16.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
		),
		elevation = CardDefaults.cardElevation(
			defaultElevation = 0.dp
		)
	) {
		Column(
			modifier = Modifier.padding(12.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			// هدر با اطلاعات اصلی
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				// اطلاعات اصلی سمت راست
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(12.dp)
				) {
					// نشانگر وضعیت تایید - مینیمال
					StatusIndicator(isConfirmed = voucher.confirmUsername != null)
					
					// اطلاعات شماره پیگیری و قبض
					Column {
						Text(
							text = voucher.trackingNumber,
							style = MaterialTheme.typography.titleMedium,
							fontWeight = FontWeight.Bold
						)
						
						Row(
							verticalAlignment = Alignment.CenterVertically,
							horizontalArrangement = Arrangement.spacedBy(4.dp)
						) {
							Text(
								text = "قبض باسکول:",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
							)
							
							Text(
								text = voucher.scaleReceiptNumber,
								style = MaterialTheme.typography.bodySmall,
								fontWeight = FontWeight.Medium,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}
				}
				
				// آیکون باز/بسته کردن و وزن - سمت چپ
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					// نمایش وزن
					Surface(
						shape = RoundedCornerShape(8.dp),
						color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
						modifier = Modifier.padding(end = 4.dp)
					) {
						Row(
							modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
							verticalAlignment = Alignment.CenterVertically,
							horizontalArrangement = Arrangement.spacedBy(4.dp)
						) {
							Icon(
								imageVector = Icons.Default.Scale,
								contentDescription = null,
								tint = MaterialTheme.colorScheme.tertiary,
								modifier = Modifier.size(12.dp)
							)
							
							Text(
								text = formatNumber(voucher.netWeight.toInt()),
								style = MaterialTheme.typography.labelMedium,
								fontWeight = FontWeight.Medium,
								color = MaterialTheme.colorScheme.tertiary
							)
						}
					}
					
					// آیکون باز/بسته کردن
					Icon(
						imageVector = Icons.Default.KeyboardArrowDown,
						contentDescription = if (expanded) "بستن" else "باز کردن",
						tint = MaterialTheme.colorScheme.onSurfaceVariant,
						modifier = Modifier
							.size(24.dp)
							.rotate(rotationState)
					)
				}
			}
			
			// جزئیات بیشتر (قابل بازشدن)
			AnimatedVisibility(
				visible = expanded,
				enter = expandVertically() + fadeIn(),
				exit = shrinkVertically() + fadeOut()
			) {
				Column {
					// خط جداکننده مینیمال
					HorizontalDivider(
						color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
						thickness = 0.5.dp,
						modifier = Modifier.padding(vertical = 8.dp)
					)
					
					// اطلاعات بیشتر با طراحی جدید
					MinimalVoucherDetails(voucher)
				}
			}
		}
	}
}

@Composable
fun StatusIndicator(isConfirmed: Boolean) {
	Box(
		modifier = Modifier
			.size(10.dp)
			.background(
				color = if (isConfirmed) {
					MaterialTheme.colorScheme.tertiary
				} else {
					MaterialTheme.colorScheme.error
				},
				shape = CircleShape
			)
	)
}

@Composable
fun MinimalVoucherDetails(voucher: VoucherDetail) {
	Column(
		verticalArrangement = Arrangement.spacedBy(16.dp),
		modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
	) {
		// بخش تاریخ و اطلاعات کاربران
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			// تاریخ خروج
			VoucherInfoItem(
				icon = Icons.Default.DateRange,
				value = persianDateFormat(voucher.exitDate),
				label = "تاریخ خروج"
			)
			
			// ساعت ورود و خروج
			Column(
				horizontalAlignment = Alignment.End
			) {
				// ساعت ورود
				UserInfo(
					label = "ساعت ورود",
					username = voucher.entryTime,
					isConfirmed = true
				)
				
				Spacer(modifier = Modifier.height(8.dp))
				
				// ساعت خروج
				UserInfo(
					label = "ساعت خروج",
					username = voucher.exitTime,
					isConfirmed = true
				)
			}
		}
	}
}

@Composable
fun VoucherInfoItem(
	icon: ImageVector,
	value: String,
	label: String
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(8.dp)
	) {
		Surface(
			shape = CircleShape,
			color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
			modifier = Modifier.size(32.dp)
		) {
			Box(contentAlignment = Alignment.Center) {
				Icon(
					imageVector = icon,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.tertiary,
					modifier = Modifier.size(16.dp)
				)
			}
		}
		
		Column {
			Text(
				text = value,
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.SemiBold
			)
			
			Text(
				text = label,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
			)
		}
	}
}

@Composable
fun UserInfo(
	label: String,
	username: String,
	isConfirmed: Boolean
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(6.dp)
	) {
		Text(
			text = username,
			style = MaterialTheme.typography.bodyMedium,
			fontWeight = FontWeight.SemiBold,
			color = if (isConfirmed) {
				MaterialTheme.colorScheme.onSurface
			} else {
				MaterialTheme.colorScheme.error
			}
		)
		
		Surface(
			shape = RoundedCornerShape(4.dp),
			color = if (isConfirmed) {
				MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
			} else {
				MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
			}
		) {
			Text(
				text = label,
				style = MaterialTheme.typography.labelSmall,
				color = if (isConfirmed) {
					MaterialTheme.colorScheme.tertiary
				} else {
					MaterialTheme.colorScheme.error
				},
				modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
			)
		}
	}
}

enum class VoucherSortType(val persianName: String) {
	DATE_DESC("تاریخ نزولی"),
	DATE_ASC("تاریخ صعودی"),
	WEIGHT_DESC("وزن نزولی"),
	WEIGHT_ASC("وزن صعودی")
}

@Composable
fun MinimalSortChip(
	type: VoucherSortType,
	isSelected: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isPressed by interactionSource.collectIsPressedAsState()
	
	val backgroundColor by animateColorAsState(
		targetValue = when {
			isSelected -> MaterialTheme.colorScheme.tertiary
			isPressed -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
			else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
		},
		label = "background color"
	)
	
	val contentColor by animateColorAsState(
		targetValue = if (isSelected) {
			MaterialTheme.colorScheme.onTertiary
		} else {
			MaterialTheme.colorScheme.onSurface
		},
		label = "content color"
	)
	
	val scale by animateFloatAsState(
		targetValue = if (isPressed) 0.95f else 1f,
		animationSpec = spring(
			dampingRatio = Spring.DampingRatioMediumBouncy,
			stiffness = Spring.StiffnessLow
		),
		label = "scale animation"
	)
	
	// آیکون متناسب با نوع مرتب‌سازی
	val icon = when (type) {
		VoucherSortType.DATE_ASC -> Icons.Default.ArrowUpward
		VoucherSortType.DATE_DESC -> Icons.Default.ArrowDownward
		VoucherSortType.WEIGHT_ASC -> Icons.AutoMirrored.Filled.TrendingUp
		VoucherSortType.WEIGHT_DESC -> Icons.AutoMirrored.Filled.TrendingDown
	}
	
	Surface(
		onClick = onClick,
		interactionSource = interactionSource,
		shape = RoundedCornerShape(20.dp),
		color = backgroundColor,
		modifier = modifier
			.graphicsLayer {
				scaleX = scale
				scaleY = scale
			}
	) {
		Row(
			modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
			horizontalArrangement = Arrangement.spacedBy(4.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				imageVector = icon,
				contentDescription = null,
				tint = contentColor,
				modifier = Modifier.size(16.dp)
			)
			Text(
				text = type.persianName,
				style = MaterialTheme.typography.labelMedium,
				color = contentColor,
				fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
			)
		}
	}
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

@SuppressLint("RememberReturnType")
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
						leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
						trailingIcon = {
							if (searchQuery.isNotEmpty()) {
								IconButton(onClick = { searchQuery = "" }) {
									Icon(Icons.Default.Clear, contentDescription = "Clear")
								}
							}
						}
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
	
	// رنگ‌های کارت بر اساس وضعیت فعال/غیرفعال
	val cardColor = if (quota.isActive) {
		MaterialTheme.colorScheme.surface
	} else {
		MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
	}
	val contentColor = if (quota.isActive) {
		MaterialTheme.colorScheme.onSurface
	} else {
		MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
	}
	val accentColor = if (quota.isActive) {
		MaterialTheme.colorScheme.primary
	} else {
		MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
	}
	
	// محاسبه پیشرفت بارگیری
	val progress = calculateProgress(quota.loadedTonnage, quota.totalTonnage)
	
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.clickable { isExpanded = !isExpanded },
		colors = CardDefaults.cardColors(containerColor = cardColor),
		border = BorderStroke(
			width = 1.dp,
			color = if (quota.isActive) {
				accentColor.copy(alpha = 0.2f)
			} else {
				MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
			}
		),
		elevation = CardDefaults.cardElevation(
			defaultElevation = if (quota.isActive) 1.dp else 0.dp
		)
	) {
		Column(modifier = Modifier.padding(10.dp)) {
			// Header - طراحی مینیمال‌تر
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				// اطلاعات اصلی کوتاژ
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					// آیکون کوتاژ با طراحی مینیمال‌تر
					Surface(
						modifier = Modifier.size(32.dp),
						shape = CircleShape,
						color = accentColor.copy(alpha = 0.1f),
						border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
					) {
						Box(contentAlignment = Alignment.Center) {
							Icon(
								imageVector = Icons.Default.Description,
								contentDescription = null,
								tint = accentColor,
								modifier = Modifier.size(16.dp)
							)
						}
					}

					// اطلاعات کوتاژ
					Column {
						Text(
							text = "کوتاژ ${quota.number}",
							style = MaterialTheme.typography.titleMedium,
							color = contentColor,
							fontWeight = FontWeight.Bold,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis
						)
						
						// نمایش نوع کالا و تناژ مانده در یک خط
						Row(
							horizontalArrangement = Arrangement.spacedBy(4.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							// نوع کالا
							quota.cargoType?.let { type ->
								Text(
									text = type,
									style = MaterialTheme.typography.bodySmall,
									color = contentColor.copy(alpha = 0.7f),
									maxLines = 1,
									overflow = TextOverflow.Ellipsis
								)
								Text(
									text = " | ",
									style = MaterialTheme.typography.bodySmall,
									color = contentColor.copy(alpha = 0.5f)
								)
							}
							
							// نمایش تناژ مانده با آیکون
							Icon(
								imageVector = Icons.Default.Scale,
								contentDescription = null,
								tint = accentColor,
								modifier = Modifier.size(12.dp)
							)
							Spacer(modifier = Modifier.width(2.dp))
							Text(
								text = formatWeightWithDetail(quota.remainingTonnage),
								style = MaterialTheme.typography.bodySmall,
								color = accentColor,
								fontWeight = FontWeight.Medium
							)
						}
					}
				}
				
				// وضعیت و دکمه باز/بسته کردن
				Row(
					horizontalArrangement = Arrangement.spacedBy(4.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					// نمایش وضعیت فعال/غیرفعال با آیکون به جای متن
					Icon(
						imageVector = if (quota.isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
						contentDescription = if (quota.isActive) "فعال" else "غیرفعال",
						tint = if (quota.isActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
						modifier = Modifier.size(16.dp)
					)
					
					// نمایش درصد پیشرفت در حالت بسته
					if (!isExpanded) {
						Text(
							text = "${(progress * 100).toInt()}%",
							style = MaterialTheme.typography.labelMedium,
							color = accentColor,
							fontWeight = FontWeight.Bold
						)
					}
					
					// آیکون باز/بسته کردن
					Icon(
						imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
						contentDescription = if (isExpanded) "بستن" else "بازکردن",
						tint = contentColor.copy(alpha = 0.7f)
					)
				}
			}
			
			// نمایش نوار پیشرفت در حالت بسته
			if (!isExpanded) {
				Spacer(modifier = Modifier.height(8.dp))
				LinearProgressIndicator(
					progress = { progress },
					modifier = Modifier
						.fillMaxWidth()
						.height(4.dp)
						.clip(RoundedCornerShape(2.dp)),
					color = accentColor,
					trackColor = accentColor.copy(alpha = 0.1f)
				)
			}

			// محتوای باز شده
			AnimatedVisibility(
				visible = isExpanded,
				enter = expandVertically() + fadeIn(),
				exit = shrinkVertically() + fadeOut()
			) {
				Column(
					modifier = Modifier.padding(top = 12.dp),
					verticalArrangement = Arrangement.spacedBy(12.dp)
				) {
					// بخش پیشرفت بارگیری
					Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.SpaceBetween
						) {
							Text(
								text = "پیشرفت بارگیری",
								style = MaterialTheme.typography.bodySmall,
								color = contentColor.copy(alpha = 0.7f)
							)
							Text(
								text = "${(progress * 100).toInt()}%",
								style = MaterialTheme.typography.bodySmall,
								color = accentColor,
								fontWeight = FontWeight.Bold
							)
						}

						LinearProgressIndicator(
							progress = { progress },
							modifier = Modifier
								.fillMaxWidth()
								.height(6.dp)
								.clip(RoundedCornerShape(3.dp)),
							color = accentColor,
							trackColor = accentColor.copy(alpha = 0.1f)
						)
					}

					// بخش آمار در یک ردیف
					Surface(
						color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
						shape = RoundedCornerShape(8.dp)
					) {
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.padding(8.dp),
							horizontalArrangement = Arrangement.SpaceEvenly
						) {
							StatItem("تناژ کل", "${formatNumber(quota.totalTonnage.toInt())} تن", accentColor)
							VerticalDivider(
								modifier = Modifier.height(24.dp),
								color = contentColor.copy(alpha = 0.1f)
							)
							StatItem("بارگیری شده", "${formatNumber(quota.loadedTonnage.toInt())} تن", accentColor)
							VerticalDivider(
								modifier = Modifier.height(24.dp),
								color = contentColor.copy(alpha = 0.1f)
							)
							StatItem("تعداد حواله", formatNumber(quota.voucherCount), accentColor)
						}
					}

					// بخش دکمه‌های عملیات
					HorizontalDivider(
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

	// دیالوگ‌ها
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
		Surface(
			modifier = Modifier.size(36.dp),
			shape = CircleShape,
			color = color.copy(alpha = 0.1f),
			border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
		) {
			Box(contentAlignment = Alignment.Center) {
				Icon(
					imageVector = icon,
					contentDescription = label,
					tint = color,
					modifier = Modifier.size(18.dp)
				)
			}
		}
		Spacer(modifier = Modifier.height(4.dp))
		Text(
			text = label,
			style = MaterialTheme.typography.labelSmall,
			color = color,
			fontWeight = FontWeight.Medium
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
		}.filter { it.isDigit() || it.isLetter() || it == ' ' }.joinToString("")
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
	val isDarkTheme = isSystemInDarkTheme()
	val defaultColor = MaterialTheme.colorScheme.primary
	var remainingSeconds by remember { mutableIntStateOf(30) }
	var lastUpdateTime by remember { mutableStateOf("") }
	var isRefreshing by remember { mutableStateOf(false) }
	var expandedShip by remember { mutableStateOf<String?>(null) }
	var showWeightDetailsDialog by remember { mutableStateOf(false) }
	var searchQuery by remember { mutableStateOf("") }
	val context = LocalContext.current
	val totalEntryVouchers = remember(loadingData) { loadingData.sumOf { it.entryVouchers } }
	val totalExitVouchers = remember(loadingData) { loadingData.sumOf { it.exitVouchers } }
	val totalNetWeight = remember(loadingData) { loadingData.sumOf { it.totalNetWeight.toDouble() }.toFloat() }
	remember(loadingData, totalExitVouchers) {
		if (totalExitVouchers > 0) totalNetWeight / totalExitVouchers else 0f
	}

	// فیلتر کردن داده‌ها بر اساس جستجو
	val filteredLoadingData = remember(loadingData, searchQuery) {
		if (searchQuery.isBlank()) {
			loadingData
		} else {
			loadingData.filter { data ->
				data.shipName.contains(searchQuery, ignoreCase = true) ||
				data.loadingWarehouse.contains(searchQuery, ignoreCase = true) ||
				data.loadingQuotaNumber.contains(searchQuery, ignoreCase = true) ||
				data.shippingCompany.contains(searchQuery, ignoreCase = true)
			}
		}
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
						remainingSeconds = remainingSeconds,
						lastUpdateTime = lastUpdateTime,
						shiftInfo = shiftInfo,
						onShareClick = {
							// تهیه متن اشتراک‌گذاری
							val shareText = viewModel.shareRealTimeLoadingData(filteredLoadingData, shiftInfo)
							
							// ایجاد Intent اشتراک‌گذاری
							val sendIntent = Intent().apply {
								action = Intent.ACTION_SEND
								putExtra(Intent.EXTRA_TEXT, shareText)
								type = "text/plain"
							}
							
							// نمایش دیالوگ انتخاب برنامه برای اشتراک‌گذاری
							val shareIntent = Intent.createChooser(sendIntent, "اشتراک‌گذاری")
							context.startActivity(shareIntent)
						}
					)
						Spacer(modifier = Modifier.height(8.dp))

					// فیلد جستجو - طراحی مینیمال و بهینه
					ModernSearchField(
						searchQuery = searchQuery,
						onSearchQueryChange = { searchQuery = it },
						modifier = Modifier.fillMaxWidth()
					)
					
					Spacer(modifier = Modifier.height(8.dp))

						StatisticItem(
							totalVouchers = totalEntryVouchers + totalExitVouchers,
							totalEntryVouchers = totalEntryVouchers,
							totalExitVouchers = totalExitVouchers,
							totalNetWeight = totalNetWeight,
							onWeightDetailsClick = { showWeightDetailsDialog = true }
						)
						Spacer(modifier = Modifier.height(8.dp))

						AnimatedContent(
						targetState = filteredLoadingData,
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
										// گروه‌بندی کوتاژها بر اساس انبار
										val warehouseGroups = shipData.groupBy { it.loadingWarehouse }
										
										Column(
											modifier = Modifier
												.fillMaxWidth()
												.padding(top = 8.dp)
										) {
											warehouseGroups.forEach { (warehouse, quotas) ->
												// نمایش نام انبار
												Row(
													modifier = Modifier
														.fillMaxWidth()
														.padding(vertical = 4.dp),
													verticalAlignment = Alignment.CenterVertically
												) {
													Icon(
														imageVector = Icons.Default.Warehouse,
														contentDescription = null,
														tint = shipColor,
														modifier = Modifier.size(16.dp)
													)
													Spacer(modifier = Modifier.width(4.dp))
													Text(
														text = "انبار: $warehouse",
														style = MaterialTheme.typography.titleSmall,
														fontWeight = FontWeight.Bold,
														color = shipColor
													)
												}
												
												// نمایش کوتاژهای این انبار
												quotas.sortedWith(
													compareBy<RealTimeLoadingData> { it.shippingCompany }
														.thenByDescending { it.entryVouchers }
												).forEach { quota ->
													RealTimeLoadingCard(
														data = quota,
														color = shipColor
													)
													Spacer(modifier = Modifier.height(8.dp))
												}
												
												if (warehouse != warehouseGroups.keys.last()) {
													HorizontalDivider(
														modifier = Modifier.padding(vertical = 8.dp),
														color = shipColor.copy(alpha = 0.1f)
													)
												}
											}
										}
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
	val elevation by animateDpAsState(
		targetValue = if (isExpanded) 4.dp else 1.dp,
		label = "Card Elevation"
	)

	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.animateContentSize(),
		shape = RoundedCornerShape(16.dp),
		color = MaterialTheme.colorScheme.surface,
		tonalElevation = elevation,
		border = BorderStroke(1.dp, color.copy(alpha = 0.15f))
	) {
		Column(modifier = Modifier.padding(vertical = 0.dp)) {
			// Header
			Surface(
				modifier = Modifier
					.fillMaxWidth()
					.clickable(onClick = onExpandToggle),
				color = color.copy(alpha = 0.08f)
			) {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 16.dp, vertical = 12.dp),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					// Ship name and icon
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(12.dp)
					) {
						// Icon in a circle
						Box(
							modifier = Modifier
								.size(36.dp)
								.background(color.copy(alpha = 0.15f), CircleShape),
							contentAlignment = Alignment.Center
						) {
							Icon(
								imageVector = Icons.Default.DirectionsBoat,
								contentDescription = null,
								tint = color,
								modifier = Modifier.size(20.dp)
							)
						}
						
						// Ship name
						Text(
							text = shipName,
							style = MaterialTheme.typography.titleMedium,
							fontWeight = FontWeight.Bold,
							color = MaterialTheme.colorScheme.onSurface
						)
					}
					
					// Stats and expand/collapse button
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(8.dp)
					) {
						// Entry vouchers badge
						VoucherBadge(
							count = entryVouchers,
							icon = Icons.Default.ArrowDownward,
							color = MaterialTheme.colorScheme.tertiary
						)
						
						// Exit vouchers badge
						VoucherBadge(
							count = exitVouchers,
							icon = Icons.Default.ArrowUpward,
							color = MaterialTheme.colorScheme.secondary
						)
						
						// Expand/collapse button
						Box(
							modifier = Modifier
								.size(28.dp)
								.background(color.copy(alpha = 0.1f), CircleShape),
							contentAlignment = Alignment.Center
						) {
							Icon(
								imageVector = Icons.Default.ExpandMore,
								contentDescription = if (isExpanded) "بستن" else "بازکردن",
								modifier = Modifier
									.size(16.dp)
									.rotate(rotationState),
								tint = color
							)
						}
					}
				}
			}
			
			// Expanded content
			AnimatedVisibility(
				visible = isExpanded,
				enter = expandVertically() + fadeIn(),
				exit = shrinkVertically() + fadeOut()
			) {
				Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
					content()
				}
			}
		}
	}
}

@Composable
fun VoucherBadge(
	count: Int,
	icon: ImageVector,
	color: Color
) {
	Surface(
		shape = RoundedCornerShape(12.dp),
		color = color.copy(alpha = 0.1f),
		border = BorderStroke(1.dp, color.copy(alpha = 0.15f))
	) {
		Row(
			modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(4.dp)
		) {
			Icon(
				imageVector = icon,
				contentDescription = null,
				tint = color,
				modifier = Modifier.size(10.dp)
			)
			Text(
				text = formatNumber(count),
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.SemiBold,
				color = color
			)
		}
	}
}

@Composable
fun DialogHeader(
	remainingSeconds: Int,
	lastUpdateTime: String,
	shiftInfo: ShiftInfo,
	onShareClick: () -> Unit = {}
) {
	Surface(
		modifier = Modifier.fillMaxWidth(),
		shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
		color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 12.dp)
		) {
			// Header row - Title and share button
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				// Title and countdown
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					Icon(
						imageVector = Icons.Default.Refresh,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.primary,
						modifier = Modifier.size(18.dp)
					)
					Text(
						"بارگیری لحظه‌ای",
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.SemiBold
					)
					MiniCountdown(seconds = remainingSeconds)
				}
				
				// Share button
				IconButton(
					onClick = onShareClick,
					modifier = Modifier
						.size(36.dp)
						.background(
							MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
							CircleShape
						)
				) {
					Icon(
						imageVector = Icons.Outlined.Share,
						contentDescription = "اشتراک‌گذاری",
						tint = MaterialTheme.colorScheme.primary,
						modifier = Modifier.size(16.dp)
					)
				}
			}

			Spacer(modifier = Modifier.height(8.dp))

			// Info row - Shift and update time
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				// Shift info
				Surface(
					shape = RoundedCornerShape(8.dp),
					color = if (shiftInfo.type.contains("روز"))
						Color(0xFFFFB74D).copy(alpha = 0.2f) else Color(0xFF5C6BC0).copy(alpha = 0.2f),
					modifier = Modifier.wrapContentWidth()
				) {
					Row(
						modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(4.dp)
					) {
						Icon(
							imageVector = if (shiftInfo.type.contains("روز"))
								Icons.Default.LightMode else Icons.Default.DarkMode,
							contentDescription = null,
							tint = if (shiftInfo.type.contains("روز"))
								Color(0xFFFFB74D) else Color(0xFF5C6BC0),
							modifier = Modifier.size(14.dp)
						)
						Text(
							text = shiftInfo.type,
							style = MaterialTheme.typography.bodySmall,
							color = if (shiftInfo.type.contains("روز"))
								Color(0xFFFFB74D) else Color(0xFF5C6BC0)
						)
					}
				}
				
				// Last update time
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(4.dp)
				) {
					Icon(
						imageVector = Icons.Default.Update,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
						modifier = Modifier.size(12.dp)
					)
					Text(
						lastUpdateTime,
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
					)
				}
			}
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
		label = "expand icon rotation"
	)
	val scaleState by animateFloatAsState(
		targetValue = if (expandedInfo) 1.01f else 1f,
		animationSpec = spring(
			dampingRatio = Spring.DampingRatioLowBouncy,
			stiffness = Spring.StiffnessLow
		),
		label = "card scale"
	)

	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.graphicsLayer {
				scaleX = scaleState
				scaleY = scaleState
			}
			.clickable { expandedInfo = !expandedInfo }
			.animateContentSize(),
		shape = RoundedCornerShape(12.dp),
		color = MaterialTheme.colorScheme.surface,
		tonalElevation = if (expandedInfo) 2.dp else 0.dp,
		border = BorderStroke(1.dp, color.copy(alpha = 0.12f))
	) {
		Column {
			// Header Row
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.background(color.copy(alpha = 0.05f))
					.padding(horizontal = 12.dp, vertical = 10.dp),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				// Quota Info (right side)
				Row(
					horizontalArrangement = Arrangement.spacedBy(10.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					// قسمت شماره کوتاژ
					Surface(
						shape = RoundedCornerShape(8.dp),
						color = color.copy(alpha = 0.1f),
						modifier = Modifier.wrapContentWidth()
					) {
						Row(
							modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
							verticalAlignment = Alignment.CenterVertically,
							horizontalArrangement = Arrangement.spacedBy(6.dp)
						) {
							// Quota number
							Text(
								text = data.loadingQuotaNumber,
								style = MaterialTheme.typography.bodyMedium,
								fontWeight = FontWeight.Bold,
								color = color
							)
						}
					}
				}

				// Voucher stats (left side)
				Row(
					horizontalArrangement = Arrangement.spacedBy(6.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					// Entry badge
					MicroBadge(
						count = data.entryVouchers,
						icon = Icons.Default.ArrowDownward,
						color = MaterialTheme.colorScheme.tertiary
					)
					
					// Divider
					Text(
						text = "/",
						style = MaterialTheme.typography.labelSmall,
						color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
					)
					
					// Exit badge
					MicroBadge(
						count = data.exitVouchers,
						icon = Icons.Default.ArrowUpward,
						color = MaterialTheme.colorScheme.secondary
					)
					
					// Expand button
					Box(
						modifier = Modifier
							.size(24.dp)
							.background(color.copy(alpha = 0.05f), CircleShape)
							.clickable { expandedInfo = !expandedInfo },
						contentAlignment = Alignment.Center
					) {
						Icon(
							imageVector = Icons.Default.ExpandMore,
							contentDescription = if (expandedInfo) "بستن" else "باز کردن",
							tint = color.copy(alpha = 0.7f),
							modifier = Modifier
								.size(16.dp)
								.rotate(rotationState)
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
						.fillMaxWidth()
						.background(MaterialTheme.colorScheme.surface)
						.padding(horizontal = 12.dp, vertical = 10.dp)
				) {
					Spacer(modifier = Modifier.height(4.dp))
					
					// Detail grid
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceEvenly
					) {
						// باربری
						CompactInfo(
							icon = Icons.Default.LocalShipping,
							label = "باربری",
							value = data.shippingCompany,
							color = color,
							modifier = Modifier.weight(1f)
						)

						// وزن خالص
						CompactInfo(
							icon = Icons.Default.Scale,
							label = "وزن خالص",
							value = "${formatNumber(data.totalNetWeight)} کیلو",
							color = color,
							modifier = Modifier.weight(1f)
						)
						
						// ساعت ورود و خروج
						CompactInfo(
							icon = Icons.AutoMirrored.Filled.Login,
							label = "ورود/خروج",
							value = "${data.entryVouchers}/${data.exitVouchers}",
							color = color,
							modifier = Modifier.weight(1.2f)
						)

						// تعداد کل
						CompactInfo(
							icon = Icons.Default.Inventory,
							label = "تعداد کل",
							value = formatNumber(totalVouchers),
							color = color,
							modifier = Modifier.weight(0.8f)
						)
					}
				}
			}
		}
	}
}

@Composable
fun MicroBadge(
	count: Int,
	icon: ImageVector,
	color: Color
) {
	Surface(
		shape = CircleShape,
		color = color.copy(alpha = 0.1f),
		modifier = Modifier.size(22.dp)
	) {
		Box(contentAlignment = Alignment.Center) {
			Icon(
				imageVector = icon,
				contentDescription = null,
				tint = color,
				modifier = Modifier.size(10.dp)
			)
		}
	}
	
	Text(
		text = formatNumber(count),
		style = MaterialTheme.typography.labelSmall,
		fontWeight = FontWeight.Medium,
		color = color
	)
}

@Composable
fun CompactInfo(
	icon: ImageVector,
	label: String,
	value: String,
	color: Color,
	modifier: Modifier = Modifier
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(6.dp),
		modifier = modifier
	) {
		// آیکون با پس‌زمینه گرد
		Box(
			modifier = Modifier
				.size(24.dp)
				.background(color.copy(alpha = 0.07f), CircleShape),
			contentAlignment = Alignment.Center
		) {
			Icon(
				imageVector = icon,
				contentDescription = null,
				tint = color,
				modifier = Modifier.size(12.dp)
			)
		}
		
		// متن‌ها
		Column(
			horizontalAlignment = Alignment.Start,
			verticalArrangement = Arrangement.spacedBy(2.dp)
		) {
			// مقدار
			Text(
				text = value,
				style = MaterialTheme.typography.bodySmall,
				fontWeight = FontWeight.Medium,
				color = MaterialTheme.colorScheme.onSurface,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			
			// برچسب
			Text(
				text = label,
				style = MaterialTheme.typography.labelSmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}
	}
}

@Composable
fun StatisticItem(
	totalVouchers: Int,
	totalEntryVouchers: Int,
	totalExitVouchers: Int,
	totalNetWeight: Float,
	onWeightDetailsClick: () -> Unit
) {
	var startAnimation by remember { mutableStateOf(false) }
	val animatedTotalVouchers by animateIntAsState(
		targetValue = if (startAnimation) totalVouchers else 0,
		animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing), 
		label = "total animation"
	)
	val animatedEntryVouchers by animateIntAsState(
		targetValue = if (startAnimation) totalEntryVouchers else 0,
		animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing), 
		label = "entry animation"
	)
	val animatedExitVouchers by animateIntAsState(
		targetValue = if (startAnimation) totalExitVouchers else 0,
		animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing), 
		label = "exit animation"
	)
	val animatedTotalWeight by animateFloatAsState(
		targetValue = if (startAnimation) totalNetWeight else 0f,
		animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing), 
		label = "weight animation"
	)

	LaunchedEffect(Unit) {
		startAnimation = true
	}

	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 4.dp),
		shape = RoundedCornerShape(10.dp),
		color = MaterialTheme.colorScheme.surface,
		border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
	) {
		Row(
			modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			// قسمت آمار قبض‌ها
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				// کل قبض‌ها
				CompactStatCounter(
					value = animatedTotalVouchers,
					label = "کل",
					icon = Icons.Default.Description,
					color = MaterialTheme.colorScheme.primary
				)
				
				// جداکننده
				VerticalDivider(
					modifier = Modifier.height(24.dp),
					color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
					thickness = 1.dp
				)
				
				// ورودی
				CompactStatCounter(
					value = animatedEntryVouchers,
					label = "ورودی",
					icon = Icons.Default.ArrowDownward,
					color = MaterialTheme.colorScheme.tertiary
				)
				
				// جداکننده
				VerticalDivider(
					modifier = Modifier.height(24.dp),
					color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
					thickness = 1.dp
				)
				
				// خروجی
				CompactStatCounter(
					value = animatedExitVouchers,
					label = "خروجی",
					icon = Icons.Default.ArrowUpward,
					color = MaterialTheme.colorScheme.secondary
				)
			}
			
			// آمار وزنی با قابلیت کلیک
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				modifier = Modifier
					.clip(RoundedCornerShape(6.dp))
					.clickable(onClick = onWeightDetailsClick)
					.padding(horizontal = 6.dp, vertical = 4.dp)
			) {
				
				// وزن کل
				CompactStatCounter(
					value = animatedTotalWeight.toInt(),
					label = "وزن کل",
					icon = Icons.Default.Scale,
					color = MaterialTheme.colorScheme.error,
					suffix = "کیلو"
				)
			}
		}
	}
}

@Composable
private fun CompactStatCounter(
	value: Int,
	label: String,
	icon: ImageVector,
	color: Color,
	suffix: String = ""
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(2.dp)
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(4.dp)
		) {
			Icon(
				imageVector = icon,
				contentDescription = null,
				tint = color,
				modifier = Modifier.size(14.dp)
			)
			Text(
				text = "${formatNumber(value)}${if (suffix.isNotEmpty()) " $suffix" else ""}",
				style = MaterialTheme.typography.bodyMedium.copy(
					fontSize = MaterialTheme.typography.bodyMedium.fontSize * 1.15
				),
				fontWeight = FontWeight.Bold,
				color = color
			)
		}
		
		Text(
			text = label,
			style = MaterialTheme.typography.labelSmall,
			color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)
	}
}

@Composable
fun MiniCountdown(seconds: Int, totalSeconds: Int = 30) {
	val animatedProgress by animateFloatAsState(
		targetValue = seconds.toFloat() / totalSeconds.toFloat(),
		animationSpec = tween(durationMillis = 1000),
		label = "countdown progress"
	)

	// افکت پالس برای جلب توجه به شمارنده
	val infiniteTransition = rememberInfiniteTransition(label = "pulse transition")
	val pulseScale by infiniteTransition.animateFloat(
		initialValue = 1f,
		targetValue = 1.02f,
		animationSpec = infiniteRepeatable(
			animation = tween(1000),
			repeatMode = RepeatMode.Reverse
		),
		label = "pulse animation"
	)

	Surface(
		shape = RoundedCornerShape(14.dp),
		color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
		border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
		modifier = Modifier.wrapContentSize()
	) {
		Row(
			modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(6.dp)
		) {

			// وسط - متن
			Text(
				text = "بروزرسانی",
				style = MaterialTheme.typography.labelSmall,
				color = MaterialTheme.colorScheme.primary
			)

			// سمت چپ - شمارنده
			Box(
				modifier = Modifier
					.size(32.dp)  // کاهش اندازه از 42dp به 32dp
					.scale(pulseScale)
					.background(
						color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
						shape = CircleShape
					),
				contentAlignment = Alignment.Center
			) {
				CircularProgressIndicator(
					progress = { animatedProgress },
					modifier = Modifier
						.size(28.dp)  // کاهش اندازه از 36dp به 28dp
						.padding(1.dp),
					color = MaterialTheme.colorScheme.primary,
					strokeWidth = 2.dp,
				)
				Text(
					text = "$seconds",
					modifier = Modifier.align(Alignment.Center),
					style = MaterialTheme.typography.labelMedium,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.primary
				)
			}
		}
	}
}

@Composable
fun FloatingActionButton(
	onRealTimeLoadingClick: () -> Unit,
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
fun ComprehensiveAnalyticsDialog(
	isVisible: Boolean,
	onDismiss: () -> Unit,
	viewModel: ReportsViewModel
) {
	var selectedTab by remember { mutableStateOf(AnalyticsTabType.QUOTAS) }
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
					// هدر دیالوگ
					AnalyticsHeader(
						title = "تحلیل جامع عملیات",
						onClose = onDismiss
					)

					Spacer(modifier = Modifier.height(16.dp))

					// نوار تب‌ها
					AnalyticsTabRow(
						selectedTab = selectedTab,
						onTabSelected = { selectedTab = it }
					)

					Spacer(modifier = Modifier.height(16.dp))

					// محتوای اصلی با توجه به وضعیت بارگذاری
					Box(
						modifier = Modifier
							.fillMaxSize()
							.weight(1f)
					) {
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
									Column(
										horizontalAlignment = Alignment.CenterHorizontally,
										verticalArrangement = Arrangement.spacedBy(8.dp)
									) {
										Icon(
											imageVector = Icons.Default.Error,
											contentDescription = null,
											tint = MaterialTheme.colorScheme.error,
											modifier = Modifier.size(48.dp)
										)
										Text(
											text = error,
											style = MaterialTheme.typography.bodyLarge,
											textAlign = TextAlign.Center,
											color = MaterialTheme.colorScheme.error
										)
									}
								}
							}
							ReportsViewModel.LoadingState.Success -> {
								// نمایش محتوای تب انتخاب شده
								AnimatedContent(
									targetState = selectedTab,
									transitionSpec = {
										fadeIn(animationSpec = tween(300)) togetherWith
												fadeOut(animationSpec = tween(300))
									},
									label = "تغییر تب"
								) { tab ->
									when (tab) {
										AnalyticsTabType.QUOTAS ->
											analyticsData?.quotaCompletionAnalysis?.let { quotaData ->
												analyticsData?.quotaPredictionAnalysis?.let {
													QuotaAnalysis(
														completionData = quotaData,
														predictionData = it,
														viewModel = viewModel
													)
												}
											}
										AnalyticsTabType.CARGO_OWNERS ->
											analyticsData?.cargoOwnerAnalysis?.let { cargoOwnerData ->
												CargoOwnerAnalysis(cargoOwnerData = cargoOwnerData)
											}
										AnalyticsTabType.PEAK_HOURS ->
											analyticsData?.shiftPerformanceAnalysis?.let { shiftData ->
												PeakHoursAnalysis(shiftData = shiftData)
											}
										AnalyticsTabType.CARRIERS ->
											analyticsData?.carrierPerformanceAnalysis?.let { carrierData ->
												CarrierAnalysis(carrierPerformanceData = carrierData)
											}
										AnalyticsTabType.WAREHOUSES ->
											analyticsData?.warehouseEfficiencyAnalysis?.let { warehouseData ->
												WarehouseAnalysis(efficiencyData = warehouseData)
											} ?: Box(
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
}

enum class AnalyticsTabType {
	QUOTAS,          // 0- وضعیت کوتاژها
	CARGO_OWNERS,    // 1- صاحبان کالا
	PEAK_HOURS,      // 2- 24 ساعت گذشته
	CARRIERS,        // 3- باربری‌ها
	WAREHOUSES       // 4- انبارها
}

@Composable
fun CargoOwnerAnalysis(
	cargoOwnerData: List<CargoOwnerData>
) {
	var expandedShipId by remember { mutableStateOf<String?>(null) }
	var searchQuery by remember { mutableStateOf("") }
	
	val filteredShips = remember(cargoOwnerData, searchQuery) {
		if (searchQuery.isEmpty()) {
			cargoOwnerData.sortedByDescending { it.total_net_weight }
		} else {
			cargoOwnerData.filter { ship -> 
				ship.shipName.contains(searchQuery, ignoreCase = true) ||
				ship.owners.any { owner -> owner.cargoOwner.contains(searchQuery, ignoreCase = true) }
			}.sortedByDescending { it.total_net_weight }
		}
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(horizontal = 4.dp)
	) {
		// هدر جستجو
		Card(
			modifier = Modifier
				.fillMaxWidth()
				.padding(vertical = 4.dp),
			colors = CardDefaults.cardColors(
				containerColor = MaterialTheme.colorScheme.surface
			),
		) {
			OutlinedTextField(
				value = searchQuery,
				onValueChange = { searchQuery = it },
				modifier = Modifier
					.fillMaxWidth()
					.padding(8.dp),
				placeholder = { Text("جستجوی کشتی یا صاحب کالا...") },
				leadingIcon = {
					Icon(
						imageVector = Icons.Default.Search,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSurfaceVariant
					)
				},
				trailingIcon = {
					if (searchQuery.isNotEmpty()) {
						IconButton(onClick = { searchQuery = "" }) {
							Icon(
								imageVector = Icons.Default.Clear,
								contentDescription = "پاک کردن",
								tint = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}
				},
				singleLine = true,
				shape = RoundedCornerShape(12.dp),
				colors = OutlinedTextFieldDefaults.colors(
					focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
					unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
				)
			)
		}

		// لیست کشتی‌ها
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.padding(top = 4.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			items(
				items = filteredShips,
				key = { it.shipName }
			) { shipData ->
				ShipCard(
					shipData = shipData,
					isExpanded = expandedShipId == shipData.shipName,
					onExpandChange = { shouldExpand ->
						expandedShipId = if (shouldExpand) shipData.shipName else null
					}
				)
			}
		}
	}
}

@Composable
private fun ShipCard(
	shipData: CargoOwnerData,
	isExpanded: Boolean,
	onExpandChange: (Boolean) -> Unit,
	modifier: Modifier = Modifier
) {
	val animateColor = MaterialTheme.colorScheme.primary
	
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
			containerColor = animateColor.copy(alpha = 0.05f)
		),
		border = BorderStroke(1.dp, animateColor.copy(alpha = 0.2f))
	) {
		Column(modifier = Modifier.padding(12.dp)) {
			// هدر کارت
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				// اطلاعات کشتی
				Row(
					horizontalArrangement = Arrangement.spacedBy(12.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					// آیکون کشتی
					Box(
						modifier = Modifier
							.size(40.dp)
							.background(
								color = animateColor.copy(alpha = 0.1f),
								shape = CircleShape
							),
						contentAlignment = Alignment.Center
					) {
						Icon(
							imageVector = Icons.Default.DirectionsBoat,
							contentDescription = null,
							tint = animateColor,
							modifier = Modifier.size(24.dp)
						)
					}

					// اطلاعات کشتی
					Column {
						Text(
							text = shipData.shipName,
							style = MaterialTheme.typography.titleMedium,
							fontWeight = FontWeight.Bold
						)
						
						Row(
							horizontalArrangement = Arrangement.spacedBy(8.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							Badge(
								containerColor = animateColor.copy(alpha = 0.1f),
								contentColor = animateColor
							) {
								Text("${formatNumber(shipData.owner_count)} صاحب کالا")
							}
							Badge(
								containerColor = animateColor.copy(alpha = 0.1f),
								contentColor = animateColor
							) {
								Text("${formatNumber(shipData.total_vouchers)} حواله")
							}
						}
					}
				}

				// وزن و آیکون گسترش
				Row(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Column(horizontalAlignment = Alignment.End) {
						Text(
							text = "${formatNumber(shipData.total_net_weight.roundToInt())} تن",
							style = MaterialTheme.typography.bodyMedium,
							fontWeight = FontWeight.Bold
						)
					}
					
					IconButton(onClick = { onExpandChange(!isExpanded) }) {
						Icon(
							imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
							contentDescription = null,
							tint = animateColor
						)
					}
				}
			}

			// محتوای گسترش یافته
			AnimatedVisibility(
				visible = isExpanded,
				enter = expandVertically() + fadeIn(),
				exit = shrinkVertically() + fadeOut()
			) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(top = 12.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					
					// لیست صاحبان کالا
					shipData.owners.sortedByDescending { it.net_weight }.forEach { ownerData ->
						CargoOwnerDetailsCard(ownerData = ownerData, color = animateColor)
					}
				}
			}
		}
	}
}

@Composable
private fun CargoOwnerDetailsCard(
	ownerData: CargoOwnerDetailsData,
	color: Color,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier.fillMaxWidth(),
		color = color.copy(alpha = 0.05f),
		shape = RoundedCornerShape(8.dp)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(8.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			// اطلاعات صاحب کالا
			Row(
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					imageVector = Icons.Default.Person,
					contentDescription = null,
					tint = color,
					modifier = Modifier.size(20.dp)
				)
				
				Column {
					Text(
						text = ownerData.cargoOwner,
						style = MaterialTheme.typography.bodyMedium,
						fontWeight = FontWeight.Medium
					)
					
					Row(
						horizontalArrangement = Arrangement.spacedBy(4.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						ChipText("${formatNumber(ownerData.quota_count)} کوتاژ", color)
						ChipText("${formatNumber(ownerData.voucher_count)} حواله", color)
					}
				}
			}
			
			// اطلاعات وزن
			Column(horizontalAlignment = Alignment.End) {
				Text(
					text = "${formatNumber(ownerData.net_weight.roundToInt())} تن",
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.Medium,
					color = color
				)
			}
		}
	}
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
			TabInfo(AnalyticsTabType.QUOTAS, "وضعیت کوتاژها", Icons.Default.Description),
			TabInfo(AnalyticsTabType.CARGO_OWNERS, "صاحبان کالا", Icons.Default.Person),
			TabInfo(AnalyticsTabType.PEAK_HOURS, "24 ساعت گذشته", Icons.Default.Schedule),
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
								val newIndex =
									(currentIndex + direction).coerceIn(0, tabs.lastIndex)

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
								else -> (swipeOffset + dragAmount).coerceIn(
									-size.width.toFloat(),
									size.width.toFloat()
								)
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
		CarriersSection(shift, color)

		// ساعت اوج
		if (shift.peak_hour != null && shift.peak_hour_detail != null) {
			PeakHoursSection(shift, color)
		}

		// عملیات‌های تاخیردار
		if (!shift.delayed_operations_detail.isNullOrEmpty()) {
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

			val details = shift.peak_hour_detail?.split("|") ?: emptyList()
			val firstDetail = details.firstOrNull()
			val startTime = if (firstDetail != null) {
				val parts = firstDetail.split(":", limit = 6)
				if (parts.size >= 6) {
					val startTimeFull = parts[5].substringBefore("-")
					parts[4] + ":" + startTimeFull
				} else {
					""
				}
			} else {
				""
			}

			val endTimes = details.mapNotNull { detail ->
				val parts = detail.split(":", limit = 6)
				if (parts.size >= 6) {
					parts[5].substringAfter("-")
				} else {
					null
				}
			}

			Text(
				text = buildString {
					append("${formatNumber(shift.peak_hour_operations ?: 0)} کوتاژ")
					append(" | ${formatNumber(shift.peak_hour_vouchers ?: 0)} حواله")
				},
				style = MaterialTheme.typography.bodyMedium,
				color = color
			)

			// نمایش جزئیات هر ساعت اوج عملیات
			shift.peak_hour_detail?.split("|")?.forEach { detail ->
				val parts = detail.split(":", limit = 6)
				if (parts.size >= 6) {
					val carrier = parts[0]
					val kotazh = parts[1]
					val vouchers = parts[2].toIntOrNull() ?: 0
					val weight = parts[3].toDoubleOrNull() ?: 0.0
					val timePart = parts[5]
					val start = "${parts[4]}:${timePart.substringBefore("-")}"
					val end = timePart.substringAfter("-")
					val time = "از $start تا $end"

					PeakHourItem(
						carrier = carrier,
						kotazh = kotazh,
						voucherCount = vouchers,
						weight = weight,
						time = time,
						color = color
					)
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
					ChipText(time, color)
				}
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
						style = MaterialTheme.typography.bodySmall,
						color = if (hours > 2) MaterialTheme.colorScheme.error else color
					)
					ChipText("$voucherCount حواله", color)
				}
			}
			if (voucherNumbers.isNotEmpty()) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
				) {
					Text(
						text = "حواله‌ها:",
						style = MaterialTheme.typography.bodySmall,
						color = color.copy(alpha = 0.7f),
						modifier = Modifier
							.fillMaxWidth(),
						textAlign = TextAlign.Start
					)

					Text(
						text = voucherNumbers,
						style = MaterialTheme.typography.bodySmall,
						color = color.copy(alpha = 0.7f),
						modifier = Modifier
							.fillMaxWidth(),
						textAlign = TextAlign.End
					)
				}
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
			.size(38.dp)
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
	predictionData: List<QuotaPredictionData>,
	viewModel: ReportsViewModel
) {
	LaunchedEffect(completionData) {
		viewModel.updateInitialQuotas(completionData)
	}

	var expandedQuotaNumber by remember { mutableStateOf<String?>(null) }
	var expandedGroup by remember { mutableStateOf<String?>(null) }
	val groupingMode by viewModel.groupingMode.collectAsState()
	val searchQuery by viewModel.searchQuery.collectAsState()
	val filteredQuotas by viewModel.filteredQuotas.collectAsState()

	val activeQuotas = filteredQuotas
		.filter { it.last_24h_vouchers > 0 }
		.sortedByDescending { it.last_24h_vouchers }

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(horizontal = 4.dp)
	) {
		// هدر جستجو و فیلتر
		Card(
			modifier = Modifier
				.fillMaxWidth()
				.padding(vertical = 4.dp),
			colors = CardDefaults.cardColors(
				containerColor = MaterialTheme.colorScheme.surface
			),
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(4.dp),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				// جستجو
				OutlinedTextField(
					value = searchQuery,
					onValueChange = { viewModel.updateSearchQuery(it) },
					modifier = Modifier.weight(1f),
					placeholder = { Text("جستجو...") },
					leadingIcon = {
						Icon(
							imageVector = Icons.Default.Search,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.onSurfaceVariant
						)
					},
					trailingIcon = {
						if (searchQuery.isNotEmpty()) {
							IconButton(onClick = { viewModel.updateSearchQuery("") }) {
								Icon(
									imageVector = Icons.Default.Clear,
									contentDescription = "پاک کردن"
								)
							}
						}
					},
					singleLine = true,
					shape = RoundedCornerShape(12.dp),
					colors = OutlinedTextFieldDefaults.colors(
						unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
					)
				)

				// دکمه‌های تغییر حالت گروه‌بندی
				Surface(
					shape = RoundedCornerShape(12.dp),
					color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
					modifier = Modifier.height(56.dp)
				) {
					Row(
						modifier = Modifier.padding(4.dp),
						horizontalArrangement = Arrangement.spacedBy(4.dp)
					) {
						IconToggleButton(
							checked = groupingMode == QuotaGroupingMode.BY_SHIP,
							onCheckedChange = { viewModel.setGroupingMode(QuotaGroupingMode.BY_SHIP) },
							colors = IconButtonDefaults.iconToggleButtonColors(
								checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
								checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
								containerColor = Color.Transparent
							),
							modifier = Modifier.size(48.dp)
						) {
							Icon(
								imageVector = Icons.Default.DirectionsBoat,
								contentDescription = "براساس کشتی"
							)
						}
						IconToggleButton(
							checked = groupingMode == QuotaGroupingMode.BY_CARRIER,
							onCheckedChange = { viewModel.setGroupingMode(QuotaGroupingMode.BY_CARRIER) },
							colors = IconButtonDefaults.iconToggleButtonColors(
								checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
								checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
								containerColor = Color.Transparent
							),
							modifier = Modifier.size(48.dp)
						) {
							Icon(
								imageVector = Icons.Default.LocalShipping,
								contentDescription = "براساس باربری"
							)
						}
					}
				}
			}
		}

		// لیست کوتاژها
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
		} else {
			LazyColumn(
				modifier = Modifier.fillMaxSize(),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				val grouped = when (groupingMode) {
					QuotaGroupingMode.BY_SHIP -> activeQuotas.groupBy { it.shipName }
					QuotaGroupingMode.BY_CARRIER -> activeQuotas.groupBy { it.shippingCompany }
				}

				// مرتب‌سازی گروه‌ها براساس تعداد کوتاژ و تناژ کل
				val sortedGroups = grouped.map { (groupName, quotas) ->
					Triple(
						groupName,
						quotas,
						quotas.sumOf { it.last_24h_weight.toDouble() }.toFloat()
					)
				}.sortedWith(
					compareByDescending<Triple<String, List<QuotaCompletionData>, Float>> { it.second.size }
						.thenByDescending { it.third }
				)

				for ((groupName, quotas, _) in sortedGroups) {
					item(key = "header_$groupName") {
						GroupHeader(
							title = groupName,
							quotas = quotas,
							icon = if (groupingMode == QuotaGroupingMode.BY_SHIP) {
								Icons.Default.DirectionsBoat
							} else {
								Icons.Default.LocalShipping
							},
							isExpanded = expandedGroup == groupName,
							onExpandClick = {
								expandedGroup = if (expandedGroup == groupName) null else groupName
							}
						)
					}

					if (expandedGroup == groupName) {
						items(
							items = quotas,
							key = { it.loadingQuotaNumber }
						) { quota ->
							AnimatedVisibility(
								visible = true,
								enter = expandVertically(
									animationSpec = spring(
										dampingRatio = Spring.DampingRatioMediumBouncy,
										stiffness = Spring.StiffnessLow
									)
								) + fadeIn(
									animationSpec = tween(
										durationMillis = 300
									)
								),
								exit = shrinkVertically() + fadeOut()
							) {
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
				}
			}
		}
	}
}

@Composable
private fun GroupHeader(
	title: String,
	quotas: List<QuotaCompletionData>,
	icon: ImageVector,
	isExpanded: Boolean,
	onExpandClick: () -> Unit
) {
	val totalWeight = quotas.sumOf { it.last_24h_weight.toDouble() }.toFloat()

	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 4.dp)
			.clickable(onClick = onExpandClick)
			.animateContentSize(
				animationSpec = spring(
					dampingRatio = Spring.DampingRatioMediumBouncy,
					stiffness = Spring.StiffnessLow
				)
			),
		color = MaterialTheme.colorScheme.surfaceVariant,
		shape = RoundedCornerShape(12.dp),
		tonalElevation = 2.dp
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Row(
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Surface(
					shape = CircleShape,
					color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
					modifier = Modifier
						.size(40.dp)
						.rotate(
							animateFloatAsState(
								targetValue = if (isExpanded) 180f else 0f,
								animationSpec = spring(
									dampingRatio = Spring.DampingRatioMediumBouncy,
									stiffness = Spring.StiffnessLow
								), label = ""
							).value
						)
				) {
					Icon(
						imageVector = icon,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.primary,
						modifier = Modifier
							.padding(8.dp)
							.size(24.dp)
					)
				}
				Column {
					Text(
						text = title,
						style = MaterialTheme.typography.titleMedium,
						color = MaterialTheme.colorScheme.onSurface
					)
					Row(
						horizontalArrangement = Arrangement.spacedBy(8.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						Text(
							text = "${quotas.size} کوتاژ",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
						Text(
							text = "•",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
						Text(
							text = "${formatNumber(totalWeight.roundToInt())} تن",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				}
			}

			Icon(
				imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.primary,
				modifier = Modifier.rotate(
					animateFloatAsState(
						targetValue = if (isExpanded) 180f else 0f,
						animationSpec = spring(
							dampingRatio = Spring.DampingRatioMediumBouncy,
							stiffness = Spring.StiffnessLow
						), label = ""
					).value
				)
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
	val isDarkTheme = isSystemInDarkTheme()
	val color = getCompletionColor(quota.completion_percentage, isDarkTheme)

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
								Text(getCompletionStatus(quota.completion_percentage))
							}
							Badge(
								containerColor = color.copy(alpha = 0.1f),
								contentColor = color
							) {
								Text("${formatNumber(quota.last_24h_vouchers)} حواله")
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
			.size(38.dp)
			.background(color.copy(alpha = 0.1f), CircleShape),
		contentAlignment = Alignment.Center
	) {
		Icon(
			imageVector = when {
				percentage >= 95f -> Icons.Default.Stars
				percentage >= 85f -> Icons.Default.Star
				percentage >= 75f -> Icons.Default.CheckCircle
				percentage >= 65f -> Icons.Default.Check
				percentage >= 55f -> Icons.Default.ThumbUp
				percentage >= 45f -> Icons.AutoMirrored.Filled.TrendingUp
				percentage >= 35f -> Icons.AutoMirrored.Filled.TrendingFlat
				percentage >= 25f -> Icons.AutoMirrored.Filled.TrendingDown
				percentage >= 15f -> Icons.Default.LowPriority
				else -> Icons.Default.NewReleases
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
			value = formatNumber(carrier.avg_net_weight.roundToInt()),
			label = "میانگین",
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
) {
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
				warehouse = warehouse
			)
		}
	}
}

@Composable
private fun ModernWarehouseCard(
	warehouse: WarehouseEfficiencyData,
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
			.padding(vertical = 2.dp),
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
			}

			// آمار سریع
			QuickWarehouseStats(warehouse, color)
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
		StatItems(
			icon = Icons.AutoMirrored.Filled.Assignment,
			value = formatNumber(warehouse.total_operations),
			label = "عملیات",
			color = color
		)
		StatItems(
			icon = Icons.Default.Speed,
			value = String.format("%.1f", warehouse.daily_operations),
			label = "عملیات در روز",
			color = color
		)
		StatItems(
			icon = Icons.Default.Scale,
			value = formatNumber((warehouse.total_processed_weight / 1000).roundToInt()),
			label = "تناژ بارگیری (تن)",
			color = color
		)
	}
}

@Composable
private fun StatItems(
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

fun formatNumber(number: Int): String {
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