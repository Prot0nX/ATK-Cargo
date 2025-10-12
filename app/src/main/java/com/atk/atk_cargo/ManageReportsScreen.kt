@file:Suppress("DEPRECATION")

package com.atk.atk_cargo

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableDefaults
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.foundation.pager.PagerState
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
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
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
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.atk.atk_cargo.api.CalculationResult
import com.atk.atk_cargo.api.CargoInfo
import com.atk.atk_cargo.api.CargoOwnerData
import com.atk.atk_cargo.api.CargoOwnerDetailsData
import com.atk.atk_cargo.api.CarrierPerformanceAnalysis
import com.atk.atk_cargo.api.ColorSelector
import com.atk.atk_cargo.api.FabItem
import com.atk.atk_cargo.api.FilteredSummary
import com.atk.atk_cargo.api.GroupSortingMode
import com.atk.atk_cargo.api.Quota
import com.atk.atk_cargo.api.QuotaCompletionData
import com.atk.atk_cargo.api.QuotaDetails
import com.atk.atk_cargo.api.QuotaEditData
import com.atk.atk_cargo.api.QuotaGroupingMode
import com.atk.atk_cargo.api.QuotaItem
import com.atk.atk_cargo.api.QuotaPercentageData
import com.atk.atk_cargo.api.QuotaSortingMode
import com.atk.atk_cargo.api.RealTimeLoadingData
import com.atk.atk_cargo.api.ReportsViewModel
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.api.ShiftInfo
import com.atk.atk_cargo.api.ShiftPerformanceData
import com.atk.atk_cargo.api.Ship
import com.atk.atk_cargo.api.ShipSortingMode
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.api.VoucherDetail
import com.atk.atk_cargo.api.Warehouse
import com.atk.atk_cargo.api.WarehouseEfficiencyData
import com.atk.atk_cargo.api.WarehouseQuotaGroupingMode
import com.atk.atk_cargo.api.WarningStatus
import com.atk.atk_cargo.api.adjustColorForTheme
import com.atk.atk_cargo.api.cardColors
import com.atk.atk_cargo.api.toTon
import com.atk.atk_cargo.api.validateServerSession
import com.atk.atk_cargo.ui.theme.ATKCargoTheme
import com.atk.atk_cargo.ui.theme.Blue700
import com.atk.atk_cargo.ui.theme.Green700
import com.atk.atk_cargo.ui.theme.Purple700
import com.atk.atk_cargo.ui.theme.Red500
import com.atk.atk_cargo.ui.theme.Red900
import com.atk.atk_cargo.ui.theme.getCompletionColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ManageReportsScreen(viewModel: ReportsViewModel, navController: NavController? = null) {
	val context = LocalContext.current
	val userPreferencesManager = remember { UserPreferencesManager(context) }

	LaunchedEffect(Unit) {
		try {
			val result = validateServerSession(userPreferencesManager)
			result.fold(
				onSuccess = {
					// Session معتبر است، ادامه می‌دهد
				},
				onFailure = {
					if (navController != null) {
						navController.navigate("home") {
							popUpTo(0) { inclusive = true }
						}
					} else {
						val intent = Intent(context, MainActivity::class.java)
						intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
						context.startActivity(intent)
					}
				}
			)
		} catch (e: Exception) {
			Log.e("ManageReportsScreen", "خطا در بررسی وضعیت ورود: ${e.message}")
			if (navController != null) {
				navController.navigate("home") {
					popUpTo(0) { inclusive = true }
				}
			} else {
				val intent = Intent(context, MainActivity::class.java)
				intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
				context.startActivity(intent)
			}
		}
	}

	val navController = rememberNavController()
	var showQuotasDialog by remember { mutableStateOf(false) }
	var selectedShipForQuotas by remember { mutableStateOf<String?>(null) }
	var showRealTimeDialog by remember { mutableStateOf(false) }
	var showAdvancedSearchDialog by remember { mutableStateOf(false) }
	var showAnalyticsDialog by remember { mutableStateOf(false) }
	var showDateRangeDialog by remember { mutableStateOf(false) }
	var showQuotaManagementDialog by remember { mutableStateOf(false) }
	var currentSelectedSection by remember { mutableIntStateOf(0) }
	val realTimeLoadingData by viewModel.realTimeLoadingData.collectAsState()
	val shiftInfo by viewModel.shiftInfo.collectAsState()
	var searchResult by remember { mutableStateOf<CargoInfo?>(null) }
	var multipleSearchResults by remember { mutableStateOf<List<CargoInfo>?>(null) }
	var errorMessage by remember { mutableStateOf<String?>(null) }
	var lastSearchType by remember { mutableStateOf<SearchType?>(null) }
	var lastSearchValue by remember { mutableStateOf<String?>(null) }
	val isDarkTheme = isSystemInDarkTheme()
	val defaultColor = MaterialTheme.colorScheme.primary
	val currentShipName by viewModel.selectedShip.collectAsState()
	val loadingError by viewModel.loadingError.collectAsState()

	ATKCargoTheme(darkTheme = isSystemInDarkTheme()) {
		CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
			Scaffold { innerPadding ->
				NavHost(
					navController = navController,
					startDestination = "shipsList",
					modifier = Modifier.padding(innerPadding)
				) {
					composable("shipsList") {
						LaunchedEffect(Unit) {
							currentSelectedSection = 0
							viewModel.clearSelectedDateRange()
						}

						ShipsList(
							viewModel = viewModel,
							onShipSelected = { shipName ->
								navController.navigate("shipDetails/$shipName") {
									launchSingleTop = true
								}
							}
						)
					}
					composable("shipDetails/{shipName}") { backStackEntry ->
						val shipName = backStackEntry.arguments?.getString("shipName") ?: return@composable
						ShipDetails(
							initialShipName = shipName,
							viewModel = viewModel,
							onWarehouseSelected = { warehouseName ->
								navController.navigate("warehouseDetails/$shipName/$warehouseName") {
									launchSingleTop = true
								}
							},
							onSectionChanged = { section ->
								currentSelectedSection = section
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
		},
		onDateRangeClick = if (currentSelectedSection == 1) {
			{ showDateRangeDialog = true }
		} else null,
		onQuotaManagementClick = {
			showQuotaManagementDialog = true
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
		onSearchReceipt = { receiptNumber ->
			lastSearchType = SearchType.RECEIPT_NUMBER
			lastSearchValue = receiptNumber
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
		},
		onSearchTracking = { trackingNumber ->
			lastSearchType = SearchType.TRACKING_NUMBER
			lastSearchValue = trackingNumber
			viewModel.performAdvancedSearchByTracking(trackingNumber) { result ->
				result.fold(
					onSuccess = { cargoInfoList ->
						if (cargoInfoList.isNotEmpty()) {
							multipleSearchResults = cargoInfoList
							showAdvancedSearchDialog = false
						} else {
							errorMessage = "اطلاعاتی برای این شماره حواله یافت نشد."
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
			onDismiss = { searchResult = null },
			onRefresh = { updatedCargo ->
				searchResult = updatedCargo
			},
			searchType = lastSearchType,
			searchValue = lastSearchValue,
			viewModel = viewModel
		)
	}

	multipleSearchResults?.let { cargoList ->
		MultipleSearchResultDialog(
			cargoInfoList = cargoList,
			onDismiss = { multipleSearchResults = null },
			onSelectCargo = { selectedCargo ->
				multipleSearchResults = null
				searchResult = selectedCargo
			}
		)
	}

	QuotaManagementDialog(
		isVisible = showQuotaManagementDialog,
		onDismiss = { showQuotaManagementDialog = false },
		viewModel = viewModel
	)

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

	// نمایش خطاهای بارگیری
	if (loadingError != null) {
		AlertDialog(
			onDismissRequest = { viewModel.clearLoadingError() },
			title = { Text("خطا") },
			text = { Text(loadingError!!) },
			confirmButton = {
				Button(onClick = { viewModel.clearLoadingError() }) {
					Text("تایید")
				}
			}
		)
	}

	PersianDateRangePickerDialog(
		isOpen = showDateRangeDialog,
		onDismiss = { showDateRangeDialog = false },
		onDateRangeSelected = { startDate, endDate ->
			val shipName = currentShipName?.name
			if (shipName != null) {
				viewModel.loadFilteredShipQuotas(shipName, startDate, endDate)
			}
			showDateRangeDialog = false
		}
	)
}

@Composable
fun ShipsList(viewModel: ReportsViewModel, onShipSelected: (String) -> Unit) {
	val shipsData by viewModel.ships.collectAsState()
	var searchTerm by remember { mutableStateOf("") }
	var selectedTabIndex by remember { mutableIntStateOf(0) }
	val realTimeLoadingData by viewModel.realTimeLoadingData.collectAsState()
	var showRealTimeDialog by remember { mutableStateOf(false) }
	val shiftInfo by viewModel.shiftInfo.collectAsState()
	val isDarkTheme = isSystemInDarkTheme()
	val defaultColor = MaterialTheme.colorScheme.primary
	val currentShipSortingMode by viewModel.shipSortingMode.collectAsState()
	val filteredActiveShips by remember(shipsData.activeShips, searchTerm, currentShipSortingMode) {
		derivedStateOf {
			val filtered = shipsData.activeShips.filter {
				it.name.contains(searchTerm, ignoreCase = true)
			}
			Log.d("ShipsList_Log", "کشتی‌های فعال فیلتر شده: ${filtered.size} - با عبارت جستجو: '$searchTerm'")
			sortShips(filtered, currentShipSortingMode)
		}
	}
	val filteredInactiveShips by remember(shipsData.inactiveShips, searchTerm, currentShipSortingMode) {
		derivedStateOf {
			val filtered = shipsData.inactiveShips.filter {
				it.name.contains(searchTerm, ignoreCase = true)
			}
			Log.d("ShipsList_Log", "کشتی‌های غیرفعال فیلتر شده: ${filtered.size} - با عبارت جستجو: '$searchTerm'")
			sortShips(filtered, currentShipSortingMode)
		}
	}

	LaunchedEffect(Unit) {
		Log.d("ShipsList_Log", "درخواست بارگذاری لیست کشتی‌ها از سرور با استفاده از app_api_2.php")
		try {
			viewModel.loadShips()
			Log.d("ShipsList_Log", "درخواست بارگذاری لیست کشتی‌ها ارسال شد")
		} catch (e: Exception) {
			Log.e("ShipsList_Log", "خطا در بارگذاری لیست کشتی‌ها: ${e.message}", e)
		}
	}

	Box(modifier = Modifier.fillMaxSize()) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 16.dp, vertical = 8.dp)
		) {
			// فیلد جستجو
			SearchField(
				searchQuery = searchTerm,
				onSearchQueryChange = { searchTerm = it }
			)

			Spacer(modifier = Modifier.height(8.dp))

			// تب‌های دسته‌بندی کشتی‌ها
			ShipsTabSelector(
				selectedTabIndex = selectedTabIndex,
				onTabSelected = { selectedTabIndex = it },
				activeShipsCount = filteredActiveShips.size,
				inactiveShipsCount = filteredInactiveShips.size
			)

			Spacer(modifier = Modifier.height(8.dp))

			// انتخابگر مرتب‌سازی کشتی‌ها
			ShipSortingSelector(
				currentMode = currentShipSortingMode,
				onModeChange = viewModel::setShipSortingMode
			)

			Spacer(modifier = Modifier.height(8.dp))

			// محتوای تب انتخاب شده
			Box(
				modifier = Modifier
					.weight(1f)
					.fillMaxWidth()
			) {
				when (selectedTabIndex) {
					0 -> ShipsTabContent(
						ships = filteredActiveShips,
						isActive = true,
						onShipSelected = { shipName ->
							viewModel.setCurrentShipName(shipName)
							onShipSelected(shipName)
						}
					)
					1 -> ShipsTabContent(
						ships = filteredInactiveShips,
						isActive = false,
						onShipSelected = { shipName ->
							viewModel.setCurrentShipName(shipName)
							onShipSelected(shipName)
						}
					)
				}
			}
		}
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

@Composable
fun ShipsTabSelector(
	selectedTabIndex: Int,
	onTabSelected: (Int) -> Unit,
	activeShipsCount: Int,
	inactiveShipsCount: Int,
	modifier: Modifier = Modifier
) {
	val tabs = listOf(
		TabData("کشتی فعال", activeShipsCount, Icons.Default.DirectionsBoat),
		TabData("کشتی غیرفعال", inactiveShipsCount, Icons.Default.Archive)
	)

	Card(
		modifier = modifier.fillMaxWidth(),
		shape = RoundedCornerShape(12.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
		)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(4.dp),
			horizontalArrangement = Arrangement.spacedBy(4.dp)
		) {
			tabs.forEachIndexed { index, tab ->
				ShipTabItem(
					tab = tab,
					isSelected = selectedTabIndex == index,
					onClick = { onTabSelected(index) },
					modifier = Modifier.weight(1f)
				)
			}
		}
	}
}

private fun shareCargoInfo(cargoInfo: CargoInfo, context: Context) {
	// ایجاد متن برای اشتراک‌گذاری
	val shareText = buildString {
		appendLine("📦 اطلاعات حواله")
		appendLine("━━━━━━━━━━━━━━━━━━━━")
		appendLine("🔢 شماره حواله: ${cargoInfo.trackingNumber}")
		appendLine("🧾 قبض باسکول: ${cargoInfo.scaleReceiptNumber}")
		appendLine("⚖️ وزن خالص: ${formatNumber(cargoInfo.netWeight.toIntOrNull() ?: 0)} کیلوگرم")
		appendLine("🚢 کشتی: ${cargoInfo.shipName}")
		appendLine("🏢 شرکت: ${cargoInfo.shippingCompany}")
		appendLine("📅 زمان ورود: ${cargoInfo.entryTime}")
		if (cargoInfo.exitTime != null) {
			appendLine("🚪 زمان خروج: ${cargoInfo.exitTime}")
			appendLine("✅ وضعیت: خروج شده")
		} else {
			appendLine("⏳ وضعیت: در انتظار خروج")
		}
		appendLine("━━━━━━━━━━━━━━━━━━━━")
		appendLine("📱 ارسال شده از اپلیکیشن ATK Cargo")
	}

	// ایجاد Intent برای اشتراک‌گذاری
	val shareIntent = Intent().apply {
		action = Intent.ACTION_SEND
		type = "text/plain"
		putExtra(Intent.EXTRA_TEXT, shareText)
		putExtra(Intent.EXTRA_SUBJECT, "اطلاعات حواله ${cargoInfo.trackingNumber}")
	}

	// نمایش انتخابگر اشتراک‌گذاری
	val chooserIntent = Intent.createChooser(shareIntent, "اشتراک‌گذاری اطلاعات حواله")

	// شروع Activity اشتراک‌گذاری
	try {
		context.startActivity(chooserIntent)
	} catch (e: Exception) {
		// مدیریت خطا در صورت عدم موفقیت در اشتراک‌گذاری
		Toast.makeText(context, "خطا در اشتراک‌گذاری: ${e.message}", Toast.LENGTH_SHORT).show()
	}
}

data class TabData(
	val title: String,
	val count: Int,
	val icon: ImageVector
)

@Composable
fun ShipTabItem(
	tab: TabData,
	isSelected: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	val backgroundColor by animateColorAsState(
		targetValue = if (isSelected) {
			MaterialTheme.colorScheme.primary
		} else {
			Color.Transparent
		},
		animationSpec = tween(300),
		label = "background"
	)

	val contentColor by animateColorAsState(
		targetValue = if (isSelected) {
			MaterialTheme.colorScheme.onPrimary
		} else {
			MaterialTheme.colorScheme.onSurface
		},
		animationSpec = tween(300),
		label = "content"
	)

	Surface(
		modifier = modifier
			.clip(RoundedCornerShape(8.dp))
			.clickable { onClick() },
		color = backgroundColor,
		shape = RoundedCornerShape(8.dp)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(vertical = 12.dp, horizontal = 16.dp),
			horizontalArrangement = Arrangement.Center,
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				imageVector = tab.icon,
				contentDescription = null,
				tint = contentColor,
				modifier = Modifier.size(18.dp)
			)

			Spacer(modifier = Modifier.width(8.dp))

			Text(
				text = "${tab.title} (${tab.count})",
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
				color = contentColor
			)
		}
	}
}

@Composable
fun ShipsTabContent(
	ships: List<Ship>,
	isActive: Boolean,
	onShipSelected: (String) -> Unit,
	modifier: Modifier = Modifier
) {
	if (ships.isEmpty()) {
		EmptyShipsState(isActive = isActive)
	} else {
		LazyColumn(
			modifier = modifier.fillMaxSize(),
			verticalArrangement = Arrangement.spacedBy(8.dp),
			contentPadding = PaddingValues(vertical = 4.dp)
		) {
			items(ships) { ship ->
				ShipCard(
					ship = ship,
					isActive = isActive,
					onClick = { onShipSelected(ship.name) }
				)
			}
		}
	}
}

@Composable
fun EmptyShipsState(
	isActive: Boolean,
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier.fillMaxSize(),
		contentAlignment = Alignment.Center
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			Icon(
				imageVector = if (isActive) Icons.Default.DirectionsBoat else Icons.Default.Archive,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.outline,
				modifier = Modifier.size(48.dp)
			)

			Text(
				text = if (isActive) "کشتی فعالی یافت نشد" else "کشتی غیرفعالی یافت نشد",
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.outline,
				textAlign = TextAlign.Center
			)
		}
	}
}

@Composable
fun ShipCard(
	ship: Ship,
	isActive: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	val cardColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
	val contentAlpha = if (isActive) 1f else 0.7f

	Card(
		modifier = modifier
			.fillMaxWidth()
			.clickable { onClick() },
		shape = RoundedCornerShape(12.dp),
		colors = CardDefaults.cardColors(
			containerColor = cardColor.copy(alpha = 0.05f)
		),
		border = BorderStroke(1.dp, cardColor.copy(alpha = 0.1f))
	) {
		// هدر کارت
		ShipCardContent(
			ship = ship,
			cardColor = cardColor,
			contentAlpha = contentAlpha
		)
	}
}

@Composable
fun ShipCardContent(
	ship: Ship,
	cardColor: Color,
	contentAlpha: Float,
	modifier: Modifier = Modifier
) {
	val totalTonnageColor = MaterialTheme.colorScheme.primary
	val loadedTonnageColor = MaterialTheme.colorScheme.error
	val remainingTonnageColor = MaterialTheme.colorScheme.onBackground

	Row(
		modifier = modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(8.dp))
			.padding(8.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		// اطلاعات اصلی کشتی
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(8.dp)
		) {
			// آیکون کشتی
			Surface(
				shape = CircleShape,
				color = cardColor.copy(alpha = 0.1f),
				modifier = Modifier.size(40.dp)
			) {
				Box(contentAlignment = Alignment.Center) {
					Icon(
						imageVector = Icons.Default.DirectionsBoat,
						contentDescription = null,
						tint = cardColor.copy(alpha = contentAlpha),
						modifier = Modifier.size(20.dp)
					)
				}
			}

			Column {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(
						text = ship.name,
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.Bold,
						color = cardColor.copy(alpha = contentAlpha),
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
						modifier = Modifier.weight(1f)
					)

					StatChip(
						icon = Icons.Default.ArrowDownward,
						value = formatNumber(ship.remainingTonnage.toInt()),
						color = remainingTonnageColor,
						label = "مانده"
					)
				}

				Spacer(modifier = Modifier.height(4.dp))
				val loadedTonnage = ship.totalTonnage - ship.remainingTonnage

				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					StatChip(
						icon = Icons.Default.Scale,
						value = formatNumber(ship.totalTonnage.toInt()),
						color = totalTonnageColor,
						label = "کل"
					)
					StatChip(
						icon = Icons.Default.ArrowUpward,
						value = formatNumber(loadedTonnage.toInt()),
						color = loadedTonnageColor,
						label = "بارگیری"
					)
				}
			}
		}
	}
}

fun sortShips(ships: List<Ship>, sortingMode: ShipSortingMode): List<Ship> {
	return when (sortingMode) {
		ShipSortingMode.REMAINING_TONNAGE_ASC -> ships.sortedBy { it.remainingTonnage }
		ShipSortingMode.REMAINING_TONNAGE_DESC -> ships.sortedByDescending { it.remainingTonnage }
		ShipSortingMode.LOADED_TONNAGE_ASC -> ships.sortedBy { it.totalTonnage - it.remainingTonnage }
		ShipSortingMode.LOADED_TONNAGE_DESC -> ships.sortedByDescending { it.totalTonnage - it.remainingTonnage }
		ShipSortingMode.NAME_ASC -> ships.sortedBy { it.name }
		ShipSortingMode.NAME_DESC -> ships.sortedByDescending { it.name }
	}
}

@Composable
fun ShipSortingSelector(
	currentMode: ShipSortingMode,
	onModeChange: (ShipSortingMode) -> Unit,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier.fillMaxWidth()
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(vertical = 4.dp, horizontal = 4.dp)
				.clip(RoundedCornerShape(8.dp))
				.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
				.border(
					BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
					RoundedCornerShape(8.dp)
				),
			horizontalArrangement = Arrangement.SpaceEvenly,
			verticalAlignment = Alignment.CenterVertically
		) {
			// تناژ مانده
			SortingModeButton(
				text = "تناژ مانده",
				icon = if (currentMode == ShipSortingMode.REMAINING_TONNAGE_ASC) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
				isAscending = currentMode == ShipSortingMode.REMAINING_TONNAGE_ASC,
				isSelected = currentMode == ShipSortingMode.REMAINING_TONNAGE_ASC || currentMode == ShipSortingMode.REMAINING_TONNAGE_DESC,
				onClick = {
					val newMode = if (currentMode == ShipSortingMode.REMAINING_TONNAGE_ASC) {
						ShipSortingMode.REMAINING_TONNAGE_DESC
					} else {
						ShipSortingMode.REMAINING_TONNAGE_ASC
					}
					onModeChange(newMode)
				},
				modifier = Modifier.weight(1f)
			)

			// تناژ بارگیری
			SortingModeButton(
				text = "تناژ بارگیری",
				icon = if (currentMode == ShipSortingMode.LOADED_TONNAGE_ASC) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
				isAscending = currentMode == ShipSortingMode.LOADED_TONNAGE_ASC,
				isSelected = currentMode == ShipSortingMode.LOADED_TONNAGE_ASC || currentMode == ShipSortingMode.LOADED_TONNAGE_DESC,
				onClick = {
					val newMode = if (currentMode == ShipSortingMode.LOADED_TONNAGE_ASC) {
						ShipSortingMode.LOADED_TONNAGE_DESC
					} else {
						ShipSortingMode.LOADED_TONNAGE_ASC
					}
					onModeChange(newMode)
				},
				modifier = Modifier.weight(1f)
			)

			// ترتیب اسم
			SortingModeButton(
				text = "ترتیب اسم",
				icon = if (currentMode == ShipSortingMode.NAME_ASC) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
				isAscending = currentMode == ShipSortingMode.NAME_ASC,
				isSelected = currentMode == ShipSortingMode.NAME_ASC || currentMode == ShipSortingMode.NAME_DESC,
				onClick = {
					val newMode = if (currentMode == ShipSortingMode.NAME_ASC) {
						ShipSortingMode.NAME_DESC
					} else {
						ShipSortingMode.NAME_ASC
					}
					onModeChange(newMode)
				},
				modifier = Modifier.weight(1f)
			)
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShipDetails(
	initialShipName: String,
	viewModel: ReportsViewModel,
	onWarehouseSelected: (String) -> Unit,
	onSectionChanged: (Int) -> Unit
) {
	val ship by viewModel.selectedShip.collectAsState()
	val selectedShipQuotas by viewModel.selectedShipQuotas.collectAsState()
	var showWarningDialog by remember { mutableStateOf(false) }
	val uiState by viewModel.uiState.collectAsState()
	
	// متغیرهای StateFlow جدید برای مدیریت بهتر وضعیت بارگذاری
	val isLoadingShipDetails by viewModel.isLoadingShipDetails.collectAsState()
	val isLoadingShipQuotas by viewModel.isLoadingShipQuotas.collectAsState()
	val shipDetailsLoadingState by viewModel.shipDetailsLoadingState.collectAsState()
	
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
		// استفاده از تابع بهینه‌شده برای بارگذاری موازی
		viewModel.loadShipDataAsync(initialShipName)
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

			when {
				uiState is ReportsViewModel.UiState.Error -> {
					Box(
						modifier = Modifier.fillMaxSize(),
						contentAlignment = Alignment.Center
					) {
						Column(
							modifier = Modifier
								.fillMaxWidth(0.8f)
								.wrapContentHeight()
								.clip(RoundedCornerShape(16.dp))
								.background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f))
								.padding(24.dp),
							horizontalAlignment = Alignment.CenterHorizontally,
							verticalArrangement = Arrangement.spacedBy(16.dp)
						) {
							Icon(
								imageVector = Icons.Default.Error,
								contentDescription = null,
								tint = MaterialTheme.colorScheme.error,
								modifier = Modifier.size(48.dp)
							)
							Text(
								text = "خطا در سیستم",
								style = MaterialTheme.typography.titleMedium,
								color = MaterialTheme.colorScheme.onErrorContainer,
								textAlign = TextAlign.Center,
								fontWeight = FontWeight.Bold
							)
							Text(
								text = (uiState as? ReportsViewModel.UiState.Error)?.message ?: "خطای نامشخص",
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
								textAlign = TextAlign.Center
							)
						}
					}
				}
				// اگر هر دو در حال بارگذاری هستند و هیچ داده‌ای موجود نیست
				(isLoadingShipDetails && isLoadingShipQuotas) && ship == null -> {
					Box(
						modifier = Modifier.fillMaxSize(),
						contentAlignment = Alignment.Center
					) {
						Column(
							horizontalAlignment = Alignment.CenterHorizontally,
							verticalArrangement = Arrangement.spacedBy(16.dp)
						) {
							CircularProgressIndicator()
							Text(
								text = "در حال بارگذاری اطلاعات کشتی...",
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
							)
						}
					}
				}
				// اگر خطا در بارگذاری اطلاعات کشتی رخ داده و هیچ داده‌ای موجود نیست
				shipDetailsLoadingState is ReportsViewModel.LoadingState.Error && ship == null -> {
					Box(
						modifier = Modifier.fillMaxSize(),
						contentAlignment = Alignment.Center
					) {
						Column(
							modifier = Modifier
								.fillMaxWidth(0.8f)
								.wrapContentHeight()
								.clip(RoundedCornerShape(16.dp))
								.background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f))
								.padding(24.dp),
							horizontalAlignment = Alignment.CenterHorizontally,
							verticalArrangement = Arrangement.spacedBy(16.dp)
						) {
							Icon(
								imageVector = Icons.Default.Error,
								contentDescription = null,
								tint = MaterialTheme.colorScheme.error,
								modifier = Modifier.size(48.dp)
							)
							Text(
								text = "خطا در بارگذاری اطلاعات کشتی",
								style = MaterialTheme.typography.titleMedium,
								color = MaterialTheme.colorScheme.onErrorContainer,
								textAlign = TextAlign.Center,
								fontWeight = FontWeight.Bold
							)
							Text(
								text = (shipDetailsLoadingState as? ReportsViewModel.LoadingState.Error)?.message ?: "خطای نامشخص",
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
								textAlign = TextAlign.Center
							)
						}
					}
				}
				// نمایش اطلاعات موجود حتی اگر برخی قسمت‌ها در حال بارگذاری باشند
				ship != null -> {
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
								0 -> ship?.let { shipDetails ->
									WarehousesAndQuotasTab(
										shipDetails = shipDetails,
										selectedShipQuotas = selectedShipQuotas,
										onWarehouseSelected = onWarehouseSelected,
										viewModel = viewModel,
										onSectionChanged = onSectionChanged
									)
								}
							}
						}
					}
				}
				// حالت پیش‌فرض برای مواردی که هیچ داده‌ای موجود نیست
				else -> {
					Box(
						modifier = Modifier
							.fillMaxSize()
							.padding(16.dp),
						contentAlignment = Alignment.Center
					) {
						Column(
							modifier = Modifier
								.fillMaxWidth(0.8f)
								.wrapContentHeight()
								.clip(RoundedCornerShape(16.dp))
								.background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f))
								.padding(24.dp),
							horizontalAlignment = Alignment.CenterHorizontally,
							verticalArrangement = Arrangement.spacedBy(16.dp)
						) {
							Icon(
								imageVector = Icons.Default.Error,
								contentDescription = null,
								tint = MaterialTheme.colorScheme.error,
								modifier = Modifier.size(48.dp)
							)
							Text(
								text = "اطلاعات کشتی در دسترس نیست",
								style = MaterialTheme.typography.titleMedium,
								color = MaterialTheme.colorScheme.onErrorContainer,
								textAlign = TextAlign.Center,
								fontWeight = FontWeight.Bold
							)
							Text(
								text = "اطلاعات مورد نظر یافت نشد. لطفاً دوباره تلاش کنید یا با پشتیبانی تماس بگیرید.",
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
								textAlign = TextAlign.Center
							)
						}
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
fun WarehousesAndQuotasTab(
	shipDetails: Ship,
	selectedShipQuotas: List<Quota>,
	onWarehouseSelected: (String) -> Unit,
	viewModel: ReportsViewModel,
	onSectionChanged: (Int) -> Unit
) {
	var selectedSection by remember { mutableIntStateOf(0) }
	val sections = listOf("کوتاژها", "انبارها")
	var searchQuery by remember { mutableStateOf("") }
	
	// مشاهده وضعیت‌های بارگذاری جداگانه
	val isLoadingShipQuotas by viewModel.isLoadingShipQuotas.collectAsState()
	val shipQuotasLoadingState by viewModel.shipQuotasLoadingState.collectAsState()

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
			// فیلد جستجو
			Spacer(modifier = Modifier.height(8.dp))
			SearchField(
				searchQuery = searchQuery,
				onSearchQueryChange = { searchQuery = it },
				modifier = Modifier.fillMaxWidth(),
				keyboardType = if (selectedSection == 0) KeyboardType.Number else KeyboardType.Text
			)

			Spacer(modifier = Modifier.height(8.dp))

			// تب‌های دسته‌بندی
			SegmentedTabs(
				selectedTabIndex = selectedSection,
				onTabSelected = { index ->
					selectedSection = index
					searchQuery = ""
					onSectionChanged(index)
				},
				tabs = sections
			)

			Spacer(modifier = Modifier.height(8.dp))

			// محتوای انتخاب شده
			when (selectedSection) {
				0 -> {
					// نمایش بهینه‌شده کوتاژها با مدیریت وضعیت بارگذاری
					Box(modifier = Modifier.fillMaxSize()) {
						QuotasList(
							quotas = selectedShipQuotas,
							searchQuery = searchQuery,
							groupingMode = viewModel.warehouseQuotaGroupingMode,
							onGroupingModeChange = viewModel::setWarehouseQuotaGroupingMode,
							onEdit = viewModel::editQuota,
							onToggleStatus = viewModel::toggleQuotaStatus,
							onDelete = viewModel::deleteQuota,
							viewModel = viewModel
						)
						
						// نمایش اندیکاتور بارگذاری برای کوتاژها
						if (isLoadingShipQuotas) {
							Surface(
								modifier = Modifier
									.align(Alignment.TopEnd)
									.padding(16.dp),
								shape = RoundedCornerShape(20.dp),
								color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
								shadowElevation = 4.dp
							) {
								Row(
									modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
									horizontalArrangement = Arrangement.spacedBy(8.dp),
									verticalAlignment = Alignment.CenterVertically
								) {
									CircularProgressIndicator(
										modifier = Modifier.size(16.dp),
										strokeWidth = 2.dp,
										color = MaterialTheme.colorScheme.primary
									)
									Text(
										text = "به‌روزرسانی کوتاژها...",
										style = MaterialTheme.typography.bodySmall,
										color = MaterialTheme.colorScheme.onPrimaryContainer
									)
								}
							}
						}
						
						// نمایش خطا در صورت وجود
						if (shipQuotasLoadingState is ReportsViewModel.LoadingState.Error && selectedShipQuotas.isNotEmpty()) {
							Surface(
								modifier = Modifier
									.align(Alignment.TopEnd)
									.padding(16.dp),
								shape = RoundedCornerShape(8.dp),
								color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
								shadowElevation = 4.dp
							) {
								Row(
									modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
									horizontalArrangement = Arrangement.spacedBy(8.dp),
									verticalAlignment = Alignment.CenterVertically
								) {
									Icon(
										imageVector = Icons.Default.Warning,
										contentDescription = null,
										tint = MaterialTheme.colorScheme.error,
										modifier = Modifier.size(16.dp)
									)
									Text(
										text = "خطا در به‌روزرسانی",
										style = MaterialTheme.typography.bodySmall,
										color = MaterialTheme.colorScheme.onErrorContainer
									)
								}
							}
						}
					}
				}
				1 -> WarehousesSection(
					warehouses = shipDetails.warehouses.filter {
						it.name.contains(searchQuery, ignoreCase = true)
					},
					onWarehouseSelected = onWarehouseSelected
				)
			}
		}
	}
}

@Composable
private fun ShipHeaderCard(shipDetails: Ship) {
	val loadedTonnage = shipDetails.totalTonnage - shipDetails.remainingTonnage
	val progress = calculateProgress(loadedTonnage, shipDetails.totalTonnage)

	Card(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 12.dp, vertical = 8.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
		),
		border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
		shape = RoundedCornerShape(12.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp),
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
					val composition by rememberLottieComposition(
						LottieCompositionSpec.RawRes(R.raw.ship)
					)
					
					LottieAnimation(
						composition = composition,
						iterations = LottieConstants.IterateForever,
						modifier = Modifier.size(36.dp)
					)
					
					Text(
						text = shipDetails.name,
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.onSurface,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis
					)
				}

				Row(
					horizontalArrangement = Arrangement.spacedBy(12.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Row(
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
							text = formatNumber(shipDetails.quotaCount),
							style = MaterialTheme.typography.bodyMedium,
							fontWeight = FontWeight.Medium,
							color = MaterialTheme.colorScheme.onSurface
						)
					}

					Row(
						horizontalArrangement = Arrangement.spacedBy(4.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						Icon(
							imageVector = Icons.Default.Warehouse,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.secondary,
							modifier = Modifier.size(16.dp)
						)
						Text(
							text = formatNumber(shipDetails.warehouses.size),
							style = MaterialTheme.typography.bodyMedium,
							fontWeight = FontWeight.Medium,
							color = MaterialTheme.colorScheme.onSurface
						)
					}
				}
			}

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
				verticalAlignment = Alignment.CenterVertically
			) {
				StatChip(
					icon = Icons.Default.ArrowUpward,
					value = formatNumber(shipDetails.totalTonnage.toInt()),
					color = MaterialTheme.colorScheme.primary,
					label = "کل"
				)
				
				StatChip(
					icon = Icons.Default.ArrowDownward,
					value = formatNumber(shipDetails.remainingTonnage.toInt()),
					color = MaterialTheme.colorScheme.tertiary,
					label = "مانده"
				)
				
				ProgressIndicator(progress = progress)
			}
		}
	}
}

@Composable
fun QuotaManagementDialog(
	isVisible: Boolean,
	onDismiss: () -> Unit,
	viewModel: ReportsViewModel
) {
	if (!isVisible) return

	val currentShipName by viewModel.selectedShip.collectAsState()
	var quotaData by remember { mutableStateOf<Map<String, Map<String, List<QuotaItem>>>>(emptyMap()) }
	var isLoading by remember { mutableStateOf(true) }
	var errorMessage by remember { mutableStateOf<String?>(null) }
	var expandedShip by remember { mutableStateOf<String?>(null) }
	var expandedQuota by remember { mutableStateOf<String?>(null) }
	var searchQuery by remember { mutableStateOf("") }
	var refreshTrigger by remember { mutableStateOf(0) }

	// تابع بارگذاری مجدد داده‌ها
	val refreshData: () -> Unit = {
		refreshTrigger++
	}

	// بارگذاری داده‌ها
	LaunchedEffect(currentShipName, refreshTrigger) {
		try {
			isLoading = true
			val response = RetrofitClient.apiService.getGroupedQuotas(shipName = currentShipName?.name ?: "")
			if (response.isSuccessful) {
				quotaData = response.body() ?: emptyMap()
				errorMessage = null
			} else {
				errorMessage = "خطا در دریافت داده‌ها: ${response.code()}"
			}
		} catch (e: Exception) {
			errorMessage = e.message
		} finally {
			isLoading = false
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
				.fillMaxWidth(0.92f)
				.fillMaxHeight(0.9f),
			shape = RoundedCornerShape(16.dp),
			color = MaterialTheme.colorScheme.surface,
			tonalElevation = 6.dp
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.background(MaterialTheme.colorScheme.background)
			) {
				// هدر دیالوگ
				QuotaManagementHeaderCard(onClose = onDismiss)

				// محتوای اصلی
				Column(
					modifier = Modifier
						.fillMaxSize()
						.padding(12.dp)
				) {
					Spacer(modifier = Modifier.height(8.dp))

					// Search Bar
					SearchField(
						searchQuery = searchQuery,
						onSearchQueryChange = { searchQuery = it },
						modifier = Modifier.fillMaxWidth(),
						placeholder = "جستجوی کوتاژ",
						keyboardType = KeyboardType.Number
					)

					Spacer(modifier = Modifier.height(8.dp))

					// محتوای اصلی با توجه به وضعیت بارگذاری
					Box(
						modifier = Modifier
							.fillMaxSize()
							.weight(1f)
					) {
						when {
							isLoading -> {
								Box(
									modifier = Modifier.fillMaxSize(),
									contentAlignment = Alignment.Center
								) {
									Column(
										horizontalAlignment = Alignment.CenterHorizontally,
										verticalArrangement = Arrangement.spacedBy(16.dp)
									) {
										CircularProgressIndicator(
											color = MaterialTheme.colorScheme.primary
										)
										Text(
											text = "در حال بارگذاری کوتاژها...",
											style = MaterialTheme.typography.bodyMedium,
											color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
										)
									}
								}
							}
							errorMessage != null -> {
								Card(
									modifier = Modifier
										.fillMaxWidth()
										.padding(horizontal = 24.dp),
									colors = CardDefaults.cardColors(
										containerColor = MaterialTheme.colorScheme.errorContainer
									),
									shape = RoundedCornerShape(12.dp)
								) {
									Row(
										modifier = Modifier.padding(16.dp),
										verticalAlignment = Alignment.CenterVertically,
										horizontalArrangement = Arrangement.spacedBy(12.dp)
									) {
										Icon(
											imageVector = Icons.Default.Error,
											contentDescription = null,
											tint = MaterialTheme.colorScheme.onErrorContainer
										)
										Text(
											text = "خطا: $errorMessage",
											style = MaterialTheme.typography.bodyMedium,
											color = MaterialTheme.colorScheme.onErrorContainer
										)
									}
								}
							}
							else -> {
								QuotaManagementContent(
									quotaData = quotaData,
									searchQuery = searchQuery,
									expandedShip = expandedShip,
									expandedQuota = expandedQuota,
									onShipToggle = { shipName ->
										expandedShip = if (expandedShip == shipName) null else shipName
										expandedQuota = null
									},
									onQuotaToggle = { quotaKey ->
										expandedQuota = if (expandedQuota == quotaKey) null else quotaKey
									},
									onRefreshData = refreshData,
									viewModel = viewModel
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
private fun SegmentedTabs(
	selectedTabIndex: Int,
	onTabSelected: (Int) -> Unit,
	tabs: List<String>
) {
	Surface(
		modifier = Modifier.fillMaxWidth(),
		shape = RoundedCornerShape(8.dp),
		color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
		border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
	) {
		Row(
			modifier = Modifier.padding(2.dp),
			horizontalArrangement = Arrangement.spacedBy(2.dp)
		) {
			tabs.forEachIndexed { index, title ->
				val isSelected = selectedTabIndex == index

				Surface(
					onClick = { onTabSelected(index) },
					modifier = Modifier.weight(1f),
					shape = RoundedCornerShape(6.dp),
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
	val currentSortingMode by viewModel.quotaSortingMode.collectAsState()
	val currentGroupSortingMode by viewModel.groupSortingMode.collectAsState()
	var expandedGroup by remember { mutableStateOf<String?>(null) }

	Column(modifier = Modifier.fillMaxSize()) {
		GroupingModeSelector(
			currentMode = currentGroupingMode,
			onModeChange = onGroupingModeChange
		)

		// دکمه‌های مرتب‌سازی کوتاژها و گروه‌ها
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(8.dp)
		) {
			QuotaSortingSelector(
				currentMode = currentSortingMode,
				onModeChange = viewModel::setQuotaSortingMode,
				modifier = Modifier.weight(1f)
			)

			GroupSortingSelector(
				currentMode = currentGroupSortingMode,
				onModeChange = viewModel::setGroupSortingMode,
				modifier = Modifier.weight(1f)
			)
		}

		// نمایش بازه زمانی انتخاب شده
		val selectedDateRange by viewModel.selectedDateRange.collectAsState()
		selectedDateRange?.let { (startDate, endDate) ->
			Card(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 4.dp, vertical = 2.dp),
				colors = CardDefaults.cardColors(
					containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
				),
				shape = RoundedCornerShape(8.dp)
			) {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 8.dp, vertical = 4.dp),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					Row(
						modifier = Modifier.weight(1f),
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.Center
					) {
						Icon(
							imageVector = Icons.Default.DateRange,
							contentDescription = null,
							modifier = Modifier.size(14.dp),
							tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
						)
						Spacer(modifier = Modifier.width(4.dp))
						Text(
							text = "از: ${startDate.replace(" ", " - ")} | تا: ${endDate.replace(" ", " - ")}",
							style = MaterialTheme.typography.labelSmall,
							color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
							textAlign = TextAlign.Center,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis
						)
					}
					IconButton(
						onClick = { viewModel.clearSelectedDateRange() },
						modifier = Modifier.size(20.dp)
					) {
						Icon(
							imageVector = Icons.Default.Clear,
							contentDescription = "حذف فیلتر",
							modifier = Modifier.size(12.dp),
							tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
						)
					}
				}
			}
		}

		Spacer(modifier = Modifier.height(4.dp))

		val groupedQuotas = remember(quotas, currentGroupingMode, currentSortingMode, currentGroupSortingMode, searchQuery) {
			// تابع کمکی برای تبدیل وزن به کیلوگرم برای مرتب‌سازی دقیق
			fun getWeightInKg(tonnage: Float): Float {
				return tonnage * 1000f // تبدیل تن به کیلوگرم
			}

			// تابع کمکی برای محاسبه مانده کوتاژ پس از کسر درصد
			fun calculateRemainingAfterPercentage(quota: Quota): Float {
				val percentageAmount = quota.totalTonnage * ((quota.percentage ?: 0.0) / 100)
				return quota.remainingTonnage - percentageAmount.toFloat()
			}

			// ابتدا کوتاژها را فیلتر و گروه‌بندی می‌کنیم
			val groupedMap = quotas
				.filter { quota ->
					quota.number.contains(searchQuery, ignoreCase = true) ||
							quota.shippingCompany.contains(searchQuery, ignoreCase = true) ||
							(quota.cargoOwner?.contains(searchQuery, ignoreCase = true) == true)
				}
				.groupBy {
					when (currentGroupingMode) {
						WarehouseQuotaGroupingMode.BY_SHIPPING_COMPANY -> it.shippingCompany
						WarehouseQuotaGroupingMode.BY_CARGO_OWNER -> it.cargoOwner
						WarehouseQuotaGroupingMode.BY_WAREHOUSE -> "${it.warehouse} | ${it.cargoOwner}"
					}
				}
				.mapValues { (_, groupQuotas) ->
					// مرتب‌سازی کوتاژها در هر گروه بر اساس مقدار دقیق وزن (کیلوگرم)
					groupQuotas.sortedWith(
						compareByDescending<Quota> { it.isActive }
							.thenBy { quota ->
								val remainingAfterPercentage = calculateRemainingAfterPercentage(quota)
								val weightInKg = getWeightInKg(remainingAfterPercentage)
								when (currentSortingMode) {
									QuotaSortingMode.REMAINING_TONNAGE_ASC -> weightInKg
									QuotaSortingMode.REMAINING_TONNAGE_DESC -> -weightInKg
								}
							}
					)
				}

			// مرتب‌سازی گروه‌ها بر اساس حالت انتخابی با استفاده از مقدار دقیق وزن
			when (currentGroupSortingMode) {
				// مرتب‌سازی بر اساس نام گروه (پیش‌فرض)
				GroupSortingMode.ALPHABETICAL -> {
					groupedMap.toSortedMap(compareBy { it })
				}
				// مرتب‌سازی بر اساس مجموع تناژ مانده گروه‌ها با دقت بالا
				GroupSortingMode.REMAINING_TONNAGE_ASC -> {
					groupedMap.toSortedMap(compareBy { groupName ->
						val totalRemainingKg = groupedMap[groupName]?.sumOf { quota ->
							val remainingAfterPercentage = calculateRemainingAfterPercentage(quota)
							getWeightInKg(remainingAfterPercentage).toDouble()
						} ?: 0.0
						totalRemainingKg
					})
				}
				GroupSortingMode.REMAINING_TONNAGE_DESC -> {
					groupedMap.toSortedMap(compareByDescending { groupName ->
						val totalRemainingKg = groupedMap[groupName]?.sumOf { quota ->
							val remainingAfterPercentage = calculateRemainingAfterPercentage(quota)
							getWeightInKg(remainingAfterPercentage).toDouble()
						} ?: 0.0
						totalRemainingKg
					})
				}
			}
		}

		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			groupedQuotas.forEach { (groupName, sortedQuotas) ->
				item {
					groupName?.let {
						QuotaGroupExpansionPanel(
							groupName = it,
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
	var expandedQuotaNumber by remember { mutableStateOf<String?>(null) }

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
					Column {
						Row(
							modifier = Modifier
								.fillMaxWidth(),
							horizontalArrangement = Arrangement.SpaceBetween,
							verticalAlignment = Alignment.CenterVertically
						) {
							Text(
								text = groupName,
								style = MaterialTheme.typography.titleSmall,
								fontWeight = FontWeight.Bold
							)

							if (currentGroupingMode == WarehouseQuotaGroupingMode.BY_CARGO_OWNER || currentGroupingMode == WarehouseQuotaGroupingMode.BY_WAREHOUSE) {
								Surface(
									shape = RoundedCornerShape(16.dp),
									color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
								) {
									Row(
										modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
										horizontalArrangement = Arrangement.spacedBy(8.dp),
										verticalAlignment = Alignment.CenterVertically
									) {
										Icon(
											imageVector = Icons.Default.Scale,
											contentDescription = null,
											tint = MaterialTheme.colorScheme.secondary,
											modifier = Modifier.size(12.dp)
										)
										Text(
											text = formatNumber(totalWeight.toInt()),
											style = MaterialTheme.typography.bodySmall,
											color = MaterialTheme.colorScheme.secondary,
											fontWeight = FontWeight.Bold
										)
									}
								}
							}
						}

						Spacer(modifier = Modifier.height(4.dp))

						QuotaStats(
							loadedWeight = loadedWeight,
							remainingWeight = remainingWeight
						)
					}
				}
				ExpandIcon(isExpanded)
			}

			if (isExpanded) {
				Spacer(modifier = Modifier.height(8.dp))
				HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
				Spacer(modifier = Modifier.height(8.dp))

				quotas.forEach { quota ->
					QuotaCard(
						quota = quota,
						isExpanded = expandedQuotaNumber == quota.number,
						onExpandToggle = { isExpand ->
							expandedQuotaNumber = if (isExpand) quota.number else null
						},
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
private fun QuotaStats(
	loadedWeight: Double,
	remainingWeight: Double
) {
	Row(
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier.fillMaxWidth()
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
			label = "مانده",
		)
	}
}

@Composable
private fun ProgressIndicator(
	progress: Float,
	modifier: Modifier = Modifier
) {
	Row(
		horizontalArrangement = Arrangement.spacedBy(6.dp),
		verticalAlignment = Alignment.CenterVertically,
		modifier = modifier
	) {
		Box(
			modifier = Modifier
				.width(75.dp)
				.height(6.dp)
				.clip(RoundedCornerShape(3.dp))
				.background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth(progress)
					.fillMaxHeight()
					.background(
						getCompletionColor(progress * 100, isSystemInDarkTheme())
					)
			)
		}
		
		Text(
			text = "${(progress * 100).roundToInt()}%",
			style = MaterialTheme.typography.bodySmall,
			fontWeight = FontWeight.Medium,
			color = getCompletionColor(progress * 100, isSystemInDarkTheme())
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
				Row(
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(
						text = "$label: ",
						style = MaterialTheme.typography.labelSmall,
						color = color.copy(alpha = 0.7f),
						fontWeight = FontWeight.Bold
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
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 8.dp, horizontal = 4.dp)
			.clip(RoundedCornerShape(12.dp))
			.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
			.border(
				BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
				RoundedCornerShape(12.dp)
			),
		horizontalArrangement = Arrangement.SpaceEvenly,
		verticalAlignment = Alignment.CenterVertically
	) {
		GroupingModeButton(
			text = "باربری",
			icon = Icons.Default.LocalShipping,
			isSelected = currentMode == WarehouseQuotaGroupingMode.BY_SHIPPING_COMPANY,
			onClick = { onModeChange(WarehouseQuotaGroupingMode.BY_SHIPPING_COMPANY) },
			modifier = Modifier.weight(1f)
		)
		VerticalDivider(
			modifier = Modifier.height(28.dp),
			thickness = 1.dp,
			color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
		)
		GroupingModeButton(
			text = "صاحب کالا",
			icon = Icons.Default.Person,
			isSelected = currentMode == WarehouseQuotaGroupingMode.BY_CARGO_OWNER,
			onClick = { onModeChange(WarehouseQuotaGroupingMode.BY_CARGO_OWNER) },
			modifier = Modifier.weight(1f)
		)
		VerticalDivider(
			modifier = Modifier.height(28.dp),
			thickness = 1.dp,
			color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
		)
		GroupingModeButton(
			text = "انبار",
			icon = Icons.Default.Warehouse,
			isSelected = currentMode == WarehouseQuotaGroupingMode.BY_WAREHOUSE,
			onClick = { onModeChange(WarehouseQuotaGroupingMode.BY_WAREHOUSE) },
			modifier = Modifier.weight(1f)
		)
	}
}

@Composable
private fun GroupingModeButton(
	text: String,
	icon: ImageVector,
	isSelected: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	val interactionSource = remember { MutableInteractionSource() }
	val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
	val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)

	Box(
		modifier = modifier
			.clickable(onClick = onClick, interactionSource = interactionSource, indication = null)
			.background(backgroundColor)
			.padding(vertical = 8.dp, horizontal = 6.dp),
		contentAlignment = Alignment.Center
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.Center,
		) {
			Icon(
				imageVector = icon,
				contentDescription = text,
				tint = contentColor,
				modifier = Modifier.size(18.dp)
			)
			Spacer(modifier = Modifier.width(6.dp))
			Text(
				text = text,
				style = MaterialTheme.typography.labelMedium,
				color = contentColor,
				fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
			)
		}
	}
}

@Composable
fun QuotaSortingSelector(
	currentMode: QuotaSortingMode,
	onModeChange: (QuotaSortingMode) -> Unit,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier.fillMaxWidth()
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(vertical = 4.dp, horizontal = 4.dp)
				.clip(RoundedCornerShape(8.dp))
				.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
				.border(
					BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
					RoundedCornerShape(8.dp)
				),
			horizontalArrangement = Arrangement.SpaceEvenly,
			verticalAlignment = Alignment.CenterVertically
		) {
			SortingModeButton(
				text = "مانده کوتاژ",
				icon = if (currentMode == QuotaSortingMode.REMAINING_TONNAGE_ASC) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
				isAscending = currentMode == QuotaSortingMode.REMAINING_TONNAGE_ASC,
				isSelected = true,
				onClick = {
					val newMode = if (currentMode == QuotaSortingMode.REMAINING_TONNAGE_ASC) {
						QuotaSortingMode.REMAINING_TONNAGE_DESC
					} else {
						QuotaSortingMode.REMAINING_TONNAGE_ASC
					}
					onModeChange(newMode)
				},
				modifier = Modifier.fillMaxWidth()
			)
		}
	}
}

@Composable
fun GroupSortingSelector(
	currentMode: GroupSortingMode,
	onModeChange: (GroupSortingMode) -> Unit,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier.fillMaxWidth()
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(vertical = 4.dp, horizontal = 4.dp)
				.clip(RoundedCornerShape(8.dp))
				.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
				.border(
					BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
					RoundedCornerShape(8.dp)
				),
			horizontalArrangement = Arrangement.SpaceEvenly,
			verticalAlignment = Alignment.CenterVertically
		) {

			SortingModeButton(
				text = "مانده گروه",
				icon = if (currentMode == GroupSortingMode.REMAINING_TONNAGE_ASC) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
				isAscending = currentMode == GroupSortingMode.REMAINING_TONNAGE_ASC,
				isSelected = currentMode == GroupSortingMode.REMAINING_TONNAGE_ASC || currentMode == GroupSortingMode.REMAINING_TONNAGE_DESC,
				onClick = {
					val newMode = if (currentMode == GroupSortingMode.REMAINING_TONNAGE_ASC) {
						GroupSortingMode.REMAINING_TONNAGE_DESC
					} else {
						GroupSortingMode.REMAINING_TONNAGE_ASC
					}
					onModeChange(newMode)
				},
				modifier = Modifier.weight(1f)
			)
		}
	}
}

@Composable
private fun SortingModeButton(
	text: String,
	icon: ImageVector,
	isAscending: Boolean,
	isSelected: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	val interactionSource = remember { MutableInteractionSource() }
	val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent
	val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

	Box(
		modifier = modifier
			.clickable(onClick = onClick, interactionSource = interactionSource, indication = null)
			.background(backgroundColor)
			.padding(vertical = 6.dp, horizontal = 4.dp),
		contentAlignment = Alignment.Center
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.Center,
		) {
			Icon(
				imageVector = icon,
				contentDescription = if (isAscending) "صعودی" else "نزولی",
				tint = contentColor,
				modifier = Modifier.size(14.dp)
			)
			Spacer(modifier = Modifier.width(4.dp))
			Text(
				text = text,
				style = MaterialTheme.typography.labelMedium,
				color = contentColor,
				fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
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
	val warningsPerPage = 3
	val groupedWarnings = warnings.chunked(warningsPerPage)
	val totalPages = groupedWarnings.size
	val pagerState = rememberPagerState(pageCount = { totalPages })
	val coroutineScope = rememberCoroutineScope()
	var expandedCardId by remember { mutableStateOf<String?>(null) }
	val scale by animateFloatAsState(
		targetValue = 1f,
		animationSpec = tween(
			durationMillis = 200,
			easing = FastOutSlowInEasing
		),
		label = "dialog scale"
	)
	val alpha by animateFloatAsState(
		targetValue = 1f,
		animationSpec = tween(
			durationMillis = 150,
			easing = FastOutSlowInEasing
		),
		label = "dialog alpha"
	)

	if (warnings.isEmpty()) return

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
				.fillMaxHeight(0.70f)
				.padding(12.dp)
				.scale(scale)
				.alpha(alpha)
				.animateContentSize(
					animationSpec = tween(
						durationMillis = 250,
						easing = FastOutSlowInEasing
					)
				),
			shape = RoundedCornerShape(24.dp),
			tonalElevation = 6.dp,
			color = MaterialTheme.colorScheme.surface
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.fillMaxHeight()
					.padding(16.dp)
			) {
				WarningDialogHeader(
					currentPage = pagerState.currentPage,
					totalPages = totalPages,
					onClose = onDismiss
				)

				Spacer(modifier = Modifier.height(24.dp))

				Column(
					modifier = Modifier.weight(1f)
				) {
					HorizontalPager(
						state = pagerState,
						modifier = Modifier
							.fillMaxWidth()
							.weight(1f, fill = false)
					) { page ->
						LazyColumn(
							modifier = Modifier
								.fillMaxWidth()
								.padding(horizontal = 8.dp),
							verticalArrangement = Arrangement.spacedBy(12.dp),
							contentPadding = PaddingValues(vertical = 8.dp)
						) {
							items(groupedWarnings[page]) { warning ->
								ElegantQuotaCard(
									warning = warning,
									viewModel = viewModel,
									isExpanded = expandedCardId == warning.quotaNumber,
									onExpandChange = { shouldExpand ->
										expandedCardId = if (shouldExpand) warning.quotaNumber else null
									}
								)
							}
						}
					}

					Spacer(modifier = Modifier.height(12.dp))

					if (totalPages > 1) {
						WarningPageNavigation(
							pagerState = pagerState,
							pageCount = totalPages
						)
					}
				}

				Spacer(modifier = Modifier.height(12.dp))

				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(12.dp)
				) {
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
							Text("بستن")
						}
					}

					Button(
						onClick = {
							coroutineScope.launch {
								warnings.forEach { warning ->
									viewModel.toggleQuotaStatus(warning.quotaNumber)
								}
								onDismiss()
							}
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
							Text("غیرفعال کردن")
						}
					}
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
			.padding(horizontal = 12.dp)
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
private fun WarningDialogHeader(
	currentPage: Int,
	totalPages: Int,
	onClose: () -> Unit
) {
	DialogHeader(currentPage, totalPages, onClose)
}

@Composable
private fun ElegantQuotaCard(
	warning: WarningStatus,
	viewModel: ReportsViewModel,
	isExpanded: Boolean,
	onExpandChange: (Boolean) -> Unit
) {
	QuotaCard(
		warning = warning,
		viewModel = viewModel,
		isExpanded = isExpanded,
		onExpandChange = onExpandChange
	)
}

@Composable
private fun WarningPageNavigation(
	pagerState: PagerState,
	pageCount: Int
) {
	PageNavigation(pagerState, pageCount)
}

@Composable
private fun PageNavigation(
	pagerState: PagerState,
	pageCount: Int
) {
	if (pageCount <= 1) return
	
	val coroutineScope = rememberCoroutineScope()

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
					isSelected = page == pagerState.currentPage,
					onClick = { 
						coroutineScope.launch {
							pagerState.animateScrollToPage(page)
						}
					}
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
				enabled = pagerState.currentPage > 0,
				onClick = { 
					pagerState.animateScrollToPage(pagerState.currentPage - 1)
				}
			)

			NavigationButton(
				text = "بعدی",
				icon = Icons.AutoMirrored.Filled.ArrowForward,
				enabled = pagerState.currentPage < pageCount - 1,
				onClick = { 
					pagerState.animateScrollToPage(pagerState.currentPage + 1)
				}
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
		animationSpec = tween(
			durationMillis = 150,
			easing = FastOutSlowInEasing
		),
		label = "dot color"
	)

	val size by animateDpAsState(
		targetValue = if (isSelected) 10.dp else 8.dp,
		animationSpec = tween(
			durationMillis = 150,
			easing = FastOutSlowInEasing
		),
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
	onClick: suspend () -> Unit
) {
	val coroutineScope = rememberCoroutineScope()
	
	OutlinedButton(
		onClick = { 
			coroutineScope.launch {
				onClick()
			}
		},
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
	viewModel: ReportsViewModel,
	isExpanded: Boolean,
	onExpandChange: (Boolean) -> Unit
) {
	var isPercentageRestrictionLoading by remember { mutableStateOf(false) }
	var isStatusToggleLoading by remember { mutableStateOf(false) }
	val mainColor = MaterialTheme.colorScheme.error
	val scope = rememberCoroutineScope()

	Card(
		modifier = Modifier
			.fillMaxWidth()
			.animateContentSize(
				animationSpec = tween(
					durationMillis = 200,
					easing = FastOutSlowInEasing
				)
			),
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
				onExpandClick = { onExpandChange(!isExpanded) }
			)

			AnimatedVisibility(
				visible = isExpanded,
				enter = expandVertically(
					animationSpec = tween(
						durationMillis = 200,
						easing = FastOutSlowInEasing
					)
				) + fadeIn(
					animationSpec = tween(
						durationMillis = 150,
						easing = FastOutSlowInEasing
					)
				),
				exit = shrinkVertically(
					animationSpec = tween(
						durationMillis = 150,
						easing = FastOutSlowInEasing
					)
				) + fadeOut(
					animationSpec = tween(
						durationMillis = 100,
						easing = FastOutSlowInEasing
					)
				)
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
						horizontalArrangement = Arrangement.spacedBy(8.dp)
					) {
						if (isPercentageRestrictionLoading) {
							LoadingActionButton(
								label = if (warning.isPercentageRestricted) "آزاد کردن درصد" else "محدود کردن درصد",
								color = mainColor,
								modifier = Modifier.weight(1f)
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
								},
								modifier = Modifier.weight(1f)
							)
						}

						if (isStatusToggleLoading) {
							LoadingActionButton(
								label = if (warning.isActive) "غیرفعال‌سازی کوتاژ" else "فعال‌سازی کوتاژ",
								color = mainColor,
								modifier = Modifier.weight(1f)
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
								},
								modifier = Modifier.weight(1f)
							)
						}
					}
				}
			}
		}
	}
}

@Composable
fun QuotaManagementHeaderCard(
	onClose: () -> Unit
) {
	Surface(
		modifier = Modifier.fillMaxWidth(),
		color = MaterialTheme.colorScheme.surface,
		tonalElevation = 2.dp
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 20.dp, vertical = 16.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			// عنوان مینیمال
			Row(
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				// آیکون ساده
				Icon(
					imageVector = Icons.Default.Inventory,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.primary,
					modifier = Modifier.size(24.dp)
				)

				// عنوان ساده
				Text(
					text = "مدیریت کوتاژها",
					style = MaterialTheme.typography.titleLarge,
					color = MaterialTheme.colorScheme.onSurface,
					fontWeight = FontWeight.Medium
				)
			}

			// دکمه بستن مینیمال
			IconButton(
				onClick = onClose,
				modifier = Modifier.size(32.dp)
			) {
				Icon(
					imageVector = Icons.Default.Close,
					contentDescription = "بستن",
					tint = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.size(20.dp)
				)
			}
		}
	}
}

enum class SortType {
	NAME_ASC, NAME_DESC,
	QUOTA_COUNT_ASC, QUOTA_COUNT_DESC,
	TOTAL_WEIGHT_ASC, TOTAL_WEIGHT_DESC
}

data class QuotaFilters(
	val showActiveOnly: Boolean = false,
	val showInactiveOnly: Boolean = false,
	val selectedWarehouses: Set<String> = emptySet(),
	val minWeight: Float? = null,
	val maxWeight: Float? = null,
	val hasTemporaryTonnage: Boolean? = null
)

@Composable
fun QuotaManagementContent(
	quotaData: Map<String, Map<String, List<QuotaItem>>>,
	searchQuery: String,
	expandedShip: String?,
	expandedQuota: String?,
	onShipToggle: (String) -> Unit,
	onQuotaToggle: (String) -> Unit,
	onRefreshData: () -> Unit,
	viewModel: ReportsViewModel
) {
	var selectedTabIndex by remember { mutableStateOf(0) }
	var sortType by remember { mutableStateOf(SortType.NAME_ASC) }
	var showSortMenu by remember { mutableStateOf(false) }
	var filters by remember { mutableStateOf(QuotaFilters()) }
	var showFiltersDialog by remember { mutableStateOf(false) }
	val filteredData = remember(quotaData, searchQuery, filters) {
		var result = quotaData

		// فیلتر بر اساس جستجوی متنی
		if (searchQuery.isNotBlank()) {
			result = result.mapNotNull { (shipName, cargoOwners) ->
				val filteredCargoOwners = cargoOwners.mapNotNull { (cargoOwner, quotas) ->
					val filteredQuotas = quotas.filter { quota ->
						// بررسی وجود دقیق عدد وارد شده در شماره کوتاژ
						quota.number.contains(searchQuery, ignoreCase = false)
					}
					if (filteredQuotas.isNotEmpty()) {
						cargoOwner to filteredQuotas
					} else null
				}.toMap()

				if (filteredCargoOwners.isNotEmpty()) {
					shipName to filteredCargoOwners
				} else null
			}.toMap()
		}

		// اعمال فیلترهای پیشرفته
		result = result.mapNotNull { (shipName, cargoOwners) ->
			val filteredCargoOwners = cargoOwners.mapNotNull { (cargoOwner, quotas) ->
				val filteredQuotas = quotas.filter { quota ->
					// فیلتر وضعیت فعال/غیرفعال
					val statusFilter = when {
						filters.showActiveOnly -> quota.isActive
						filters.showInactiveOnly -> !quota.isActive
						else -> true
					}

					// فیلتر انبار
					val warehouseFilter = if (filters.selectedWarehouses.isEmpty()) {
						true
					} else {
						filters.selectedWarehouses.contains(quota.warehouse)
					}

					// فیلتر وزن
					val weightFilter = {
						val weight = quota.temporaryTonnageValue ?: 0f
						val minOk = filters.minWeight?.let { weight >= it } ?: true
						val maxOk = filters.maxWeight?.let { weight <= it } ?: true
						minOk && maxOk
					}()

					// فیلتر تناژ موقت
					val tempTonnageFilter = filters.hasTemporaryTonnage?.let { hasTemp ->
						if (hasTemp) quota.temporaryTonnageEnabled else !quota.temporaryTonnageEnabled
					} ?: true

					statusFilter && warehouseFilter && weightFilter && tempTonnageFilter
				}

				if (filteredQuotas.isNotEmpty()) {
					cargoOwner to filteredQuotas
				} else null
			}.toMap()

			if (filteredCargoOwners.isNotEmpty()) {
				shipName to filteredCargoOwners
			} else null
		}.toMap()

		result
	}

	// تابع مرتب‌سازی داده‌ها
	val sortedData = remember(filteredData, sortType) {
		val sortedMap = filteredData.toList().sortedWith { (shipName1, cargoOwners1), (shipName2, cargoOwners2) ->
			when (sortType) {
				SortType.NAME_ASC -> shipName1.compareTo(shipName2)
				SortType.NAME_DESC -> shipName2.compareTo(shipName1)
				SortType.QUOTA_COUNT_ASC -> {
					val count1 = cargoOwners1.values.flatten().size
					val count2 = cargoOwners2.values.flatten().size
					count1.compareTo(count2)
				}
				SortType.QUOTA_COUNT_DESC -> {
					val count1 = cargoOwners1.values.flatten().size
					val count2 = cargoOwners2.values.flatten().size
					count2.compareTo(count1)
				}
				SortType.TOTAL_WEIGHT_ASC -> {
					val weight1 = cargoOwners1.values.flatten().sumOf { it.temporaryTonnageValue?.toDouble() ?: 0.0 }
					val weight2 = cargoOwners2.values.flatten().sumOf { it.temporaryTonnageValue?.toDouble() ?: 0.0 }
					weight1.compareTo(weight2)
				}
				SortType.TOTAL_WEIGHT_DESC -> {
					val weight1 = cargoOwners1.values.flatten().sumOf { it.temporaryTonnageValue?.toDouble() ?: 0.0 }
					val weight2 = cargoOwners2.values.flatten().sumOf { it.temporaryTonnageValue?.toDouble() ?: 0.0 }
					weight2.compareTo(weight1)
				}
			}
		}.toMap()
		sortedMap
	}

	// تقسیم داده‌ها بر اساس وضعیت فعال/غیرفعال
	val (activeShipsData, inactiveShipsData) = remember(sortedData) {
		val active = mutableMapOf<String, Map<String, List<QuotaItem>>>()
		val inactive = mutableMapOf<String, Map<String, List<QuotaItem>>>()

		sortedData.forEach { (shipName, cargoOwners) ->
			val allQuotas = cargoOwners.values.flatten()
			val hasActiveQuota = allQuotas.any { it.isActive }

			if (hasActiveQuota) {
				active[shipName] = cargoOwners
			} else {
				inactive[shipName] = cargoOwners
			}
		}

		Pair(active.toMap(), inactive.toMap())
	}

	Column {
		// تب‌های دسته‌بندی کوتاژها
		QuotaTabSelector(
			selectedTabIndex = selectedTabIndex,
			onTabSelected = { selectedTabIndex = it },
			activeShipsCount = activeShipsData.size,
			inactiveShipsCount = inactiveShipsData.size
		)

		Spacer(modifier = Modifier.height(8.dp))

		// ردیف ابزارهای مرتب‌سازی و فیلتر
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
		) {
			SortOptionsCard(
				sortType = sortType,
				onSortTypeChange = { sortType = it },
				showSortMenu = showSortMenu,
				onShowSortMenuChange = { showSortMenu = it }
			)

			FilterOptionsCard(
				filters = filters,
				onShowFiltersDialog = { showFiltersDialog = true }
			)
		}

		Spacer(modifier = Modifier.height(8.dp))

		// محتوای تب انتخاب شده
		Box(
			modifier = Modifier
				.weight(1f)
				.fillMaxWidth()
		) {
			val currentData = when (selectedTabIndex) {
				0 -> activeShipsData
				1 -> inactiveShipsData
				else -> activeShipsData
			}

			QuotaTabContent(
				quotaData = currentData,
				isActive = selectedTabIndex == 0,
				expandedShip = expandedShip,
				expandedQuota = expandedQuota,
				onShipToggle = onShipToggle,
				onQuotaToggle = onQuotaToggle,
				onRefreshData = onRefreshData,
				viewModel = viewModel
			)
		}

		// دیالوگ فیلترهای پیشرفته
		if (showFiltersDialog) {
			AdvancedFiltersDialog(
				filters = filters,
				onFiltersChanged = { newFilters -> filters = newFilters },
				onDismiss = { showFiltersDialog = false }
			)
		}
	}
}

@Composable
fun AdvancedFiltersDialog(
	filters: QuotaFilters,
	onFiltersChanged: (QuotaFilters) -> Unit,
	onDismiss: () -> Unit
) {
	var tempFilters by remember { mutableStateOf(filters) }

	Dialog(onDismissRequest = onDismiss) {
		Card(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp),
			shape = RoundedCornerShape(20.dp),
			colors = CardDefaults.cardColors(
				containerColor = MaterialTheme.colorScheme.surface
			),
			elevation = CardDefaults.cardElevation(
				defaultElevation = 8.dp
			)
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.background(
						brush = androidx.compose.ui.graphics.Brush.verticalGradient(
							colors = listOf(
								MaterialTheme.colorScheme.surface,
								MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
							)
						)
					)
					.padding(24.dp),
				verticalArrangement = Arrangement.spacedBy(20.dp)
			) {
				// هدر دیالوگ
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					// عنوان
					Text(
						text = "فیلترهای پیشرفته",
						style = MaterialTheme.typography.titleLarge,
						color = MaterialTheme.colorScheme.onSurface,
						fontWeight = FontWeight.Medium
					)

					// دکمه بستن
					IconButton(
						onClick = onDismiss,
						modifier = Modifier.size(32.dp)
					) {
						Icon(
							imageVector = Icons.Default.Close,
							contentDescription = "بستن",
							tint = MaterialTheme.colorScheme.onSurfaceVariant,
							modifier = Modifier.size(20.dp)
						)
					}
				}

				// فیلتر وضعیت
				Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
					Text(
						text = "وضعیت کوتاژها",
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.Medium,
						color = MaterialTheme.colorScheme.onSurface
					)

					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.spacedBy(8.dp)
					) {
						FilterChip(
							modifier = Modifier.weight(1f),
							selected = tempFilters.showActiveOnly,
							onClick = {
								tempFilters = if (tempFilters.showActiveOnly) {
									// اگر قبلاً انتخاب شده بود، آن را غیرفعال کن
									tempFilters.copy(
										showActiveOnly = false,
										showInactiveOnly = false
									)
								} else {
									// فعال کردن "فقط فعال" و غیرفعال کردن "فقط غیرفعال"
									tempFilters.copy(
										showActiveOnly = true,
										showInactiveOnly = false
									)
								}
							},
							label = { Text("فقط فعال") }
						)

						FilterChip(
							modifier = Modifier.weight(1f),
							selected = tempFilters.showInactiveOnly,
							onClick = {
								tempFilters = if (tempFilters.showInactiveOnly) {
									// اگر قبلاً انتخاب شده بود، آن را غیرفعال کن
									tempFilters.copy(
										showActiveOnly = false,
										showInactiveOnly = false
									)
								} else {
									// فعال کردن "فقط غیرفعال" و غیرفعال کردن "فقط فعال"
									tempFilters.copy(
										showActiveOnly = false,
										showInactiveOnly = true
									)
								}
							},
							label = { Text("فقط غیرفعال") }
						)
					}
				}

				// فیلتر تناژ موقت
				Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
					Text(
						text = "تناژ موقت",
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.Medium,
						color = MaterialTheme.colorScheme.onSurface
					)

					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.spacedBy(8.dp)
					) {
						FilterChip(
							modifier = Modifier.weight(1f),
							selected = tempFilters.hasTemporaryTonnage == true,
							onClick = {
								tempFilters = tempFilters.copy(
									hasTemporaryTonnage = if (tempFilters.hasTemporaryTonnage == true) null else true
								)
							},
							label = { Text("دارای تناژ موقت") }
						)

						FilterChip(
							modifier = Modifier.weight(1f),
							selected = tempFilters.hasTemporaryTonnage == false,
							onClick = {
								tempFilters = tempFilters.copy(
									hasTemporaryTonnage = if (tempFilters.hasTemporaryTonnage == false) null else false
								)
							},
							label = { Text("بدون تناژ موقت") }
						)
					}
				}

				// دکمه‌های عملیات
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(12.dp)
				) {
					OutlinedButton(
						modifier = Modifier.weight(1f),
						onClick = {
							tempFilters = QuotaFilters()
						},
						shape = RoundedCornerShape(12.dp)
					) {
						Text(
							text = "پاک کردن",
							style = MaterialTheme.typography.bodyMedium,
							fontWeight = FontWeight.Medium
						)
					}

					Button(
						modifier = Modifier.weight(1f),
						onClick = {
							onFiltersChanged(tempFilters)
							onDismiss()
						},
						shape = RoundedCornerShape(12.dp)
					) {
						Text(
							text = "اعمال کردن",
							style = MaterialTheme.typography.bodyMedium,
							fontWeight = FontWeight.Medium
						)
					}
				}
			}
		}
	}
}

@Composable
fun FilterOptionsCard(
	modifier: Modifier = Modifier,
	filters: QuotaFilters,
	onShowFiltersDialog: () -> Unit
) {
	// محاسبه تعداد فیلترهای فعال
	val activeFiltersCount = listOfNotNull(
		if (filters.showActiveOnly || filters.showInactiveOnly) 1 else null,
		if (filters.selectedWarehouses.isNotEmpty()) 1 else null,
		if (filters.minWeight != null || filters.maxWeight != null) 1 else null,
		if (filters.hasTemporaryTonnage != null) 1 else null
	).size

	Card(
		modifier = modifier.size(48.dp),
		shape = RoundedCornerShape(12.dp),
		colors = CardDefaults.cardColors(
			containerColor = if (activeFiltersCount > 0) {
				MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
			} else {
				MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
			}
		)
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.clickable { onShowFiltersDialog() },
			contentAlignment = Alignment.Center
		) {
			Box {
				Icon(
					imageVector = Icons.Default.Filter,
					contentDescription = "فیلترها",
					tint = if (activeFiltersCount > 0) {
						MaterialTheme.colorScheme.secondary
					} else {
						MaterialTheme.colorScheme.primary
					},
					modifier = Modifier.size(24.dp)
				)

				// نمایش تعداد فیلترهای فعال
				if (activeFiltersCount > 0) {
					Box(
						modifier = Modifier
							.offset(x = 8.dp, y = (-8).dp)
							.size(16.dp)
							.background(
								color = MaterialTheme.colorScheme.error,
								shape = CircleShape
							),
						contentAlignment = Alignment.Center
					) {
						Text(
							text = activeFiltersCount.toString(),
							style = MaterialTheme.typography.labelSmall,
							color = MaterialTheme.colorScheme.onError,
							fontWeight = FontWeight.Bold
						)
					}
				}
			}
		}
	}
}

@Composable
fun SortOptionsCard(
	sortType: SortType,
	onSortTypeChange: (SortType) -> Unit,
	showSortMenu: Boolean,
	onShowSortMenuChange: (Boolean) -> Unit,
	modifier: Modifier = Modifier
) {
	val sortOptions = listOf(
		SortType.NAME_ASC to "نام (الف تا ی)",
		SortType.NAME_DESC to "نام (ی تا الف)",
		SortType.QUOTA_COUNT_ASC to "تعداد کوتاژ (کم به زیاد)",
		SortType.QUOTA_COUNT_DESC to "تعداد کوتاژ (زیاد به کم)",
		SortType.TOTAL_WEIGHT_ASC to "وزن کل (کم به زیاد)",
		SortType.TOTAL_WEIGHT_DESC to "وزن کل (زیاد به کم)"
	)

	Card(
		modifier = modifier.size(48.dp),
		shape = RoundedCornerShape(12.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
		)
	) {
		Box {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.clickable { onShowSortMenuChange(!showSortMenu) },
				contentAlignment = Alignment.Center
			) {
				Icon(
					imageVector = Icons.AutoMirrored.Filled.Sort,
					contentDescription = "مرتب‌سازی",
					tint = MaterialTheme.colorScheme.primary,
					modifier = Modifier.size(24.dp)
				)
			}

			// منوی کشویی گزینه‌های مرتب‌سازی
			DropdownMenu(
				expanded = showSortMenu,
				onDismissRequest = { onShowSortMenuChange(false) },
				modifier = Modifier.width(250.dp)
			) {
				sortOptions.forEach { (sortTypeOption, label) ->
					DropdownMenuItem(
						text = {
							Row(
								horizontalArrangement = Arrangement.spacedBy(8.dp),
								verticalAlignment = Alignment.CenterVertically
							) {
								if (sortType == sortTypeOption) {
									Icon(
										imageVector = Icons.Default.Check,
										contentDescription = null,
										tint = MaterialTheme.colorScheme.primary,
										modifier = Modifier.size(16.dp)
									)
								} else {
									Spacer(modifier = Modifier.size(16.dp))
								}
								Text(
									text = label,
									style = MaterialTheme.typography.bodyMedium,
									color = if (sortType == sortTypeOption)
										MaterialTheme.colorScheme.primary
									else
										MaterialTheme.colorScheme.onSurface
								)
							}
						},
						onClick = {
							onSortTypeChange(sortTypeOption)
							onShowSortMenuChange(false)
						}
					)
				}
			}
		}
	}
}

@Composable
fun QuotaTabSelector(
	selectedTabIndex: Int,
	onTabSelected: (Int) -> Unit,
	activeShipsCount: Int,
	inactiveShipsCount: Int,
	modifier: Modifier = Modifier
) {
	val tabs = listOf(
		TabData("کشتی فعال", activeShipsCount, Icons.Default.CheckCircle),
		TabData("کشتی غیرفعال", inactiveShipsCount, Icons.Default.Cancel)
	)

	Card(
		modifier = modifier.fillMaxWidth(),
		shape = RoundedCornerShape(12.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
		)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(4.dp),
			horizontalArrangement = Arrangement.spacedBy(4.dp)
		) {
			tabs.forEachIndexed { index, tab ->
				QuotaTabItem(
					tab = tab,
					isSelected = selectedTabIndex == index,
					onClick = { onTabSelected(index) },
					modifier = Modifier.weight(1f)
				)
			}
		}
	}
}

@Composable
fun QuotaTabItem(
	tab: TabData,
	isSelected: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	val backgroundColor by animateColorAsState(
		targetValue = if (isSelected) {
			MaterialTheme.colorScheme.primary
		} else {
			Color.Transparent
		},
		animationSpec = tween(300),
		label = "background"
	)

	val contentColor by animateColorAsState(
		targetValue = if (isSelected) {
			MaterialTheme.colorScheme.onPrimary
		} else {
			MaterialTheme.colorScheme.onSurface
		},
		animationSpec = tween(300),
		label = "content"
	)

	Surface(
		modifier = modifier
			.clip(RoundedCornerShape(8.dp))
			.clickable { onClick() },
		color = backgroundColor,
		shape = RoundedCornerShape(8.dp)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(vertical = 12.dp, horizontal = 16.dp),
			horizontalArrangement = Arrangement.Center,
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				imageVector = tab.icon,
				contentDescription = null,
				tint = contentColor,
				modifier = Modifier.size(18.dp)
			)

			Spacer(modifier = Modifier.width(8.dp))

			Text(
				text = "${tab.title} (${tab.count})",
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
				color = contentColor
			)
		}
	}
}

@Composable
fun QuotaTabContent(
	quotaData: Map<String, Map<String, List<QuotaItem>>>,
	isActive: Boolean,
	expandedShip: String?,
	expandedQuota: String?,
	onShipToggle: (String) -> Unit,
	onQuotaToggle: (String) -> Unit,
	onRefreshData: () -> Unit,
	viewModel: ReportsViewModel,
	modifier: Modifier = Modifier
) {
	if (quotaData.isEmpty()) {
		EmptyQuotaState(isActive = isActive)
	} else {
		LazyColumn(
			modifier = modifier.fillMaxSize(),
			verticalArrangement = Arrangement.spacedBy(16.dp),
			contentPadding = PaddingValues(vertical = 8.dp)
		) {
			items(quotaData.entries.toList()) { (shipName, cargoOwners) ->
				// بررسی اینکه آیا تمام کوتاژهای این کشتی غیرفعال هستند
				val allQuotas = cargoOwners.values.flatten()
				val allQuotasInactive = allQuotas.isNotEmpty() && allQuotas.all { !it.isActive }

				QuotaShipExpansionPanel(
					shipName = shipName,
					cargoOwners = cargoOwners,
					isExpanded = expandedShip == shipName,
					onToggleExpand = { onShipToggle(shipName) },
					expandedQuota = expandedQuota,
					onQuotaToggle = onQuotaToggle,
					onRefreshData = onRefreshData,
					viewModel = viewModel,
					allQuotasInactive = allQuotasInactive
				)
			}
		}
	}
}

@Composable
fun EmptyQuotaState(
	isActive: Boolean,
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier.fillMaxSize(),
		contentAlignment = Alignment.Center
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			Icon(
				imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
				contentDescription = null,
				modifier = Modifier.size(64.dp),
				tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
			)

			Spacer(modifier = Modifier.height(16.dp))

			Text(
				text = if (isActive) "هیچ کشتی با کوتاژ فعالی یافت نشد" else "هیچ کشتی با کوتاژ غیرفعالی یافت نشد",
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				textAlign = TextAlign.Center
			)

			Spacer(modifier = Modifier.height(8.dp))

			Text(
				text = if (isActive) "تمام کشتی‌ها دارای کوتاژ غیرفعال هستند" else "تمام کشتی‌ها دارای کوتاژ فعال هستند",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
				textAlign = TextAlign.Center
			)
		}
	}
}

@Composable
fun IntegratedQuotaCard(
	quota: QuotaItem,
	isExpanded: Boolean,
	onToggleExpand: () -> Unit,
	onRefreshData: () -> Unit,
	viewModel: ReportsViewModel,
	modifier: Modifier = Modifier
) {
	var isUpdating by remember { mutableStateOf(false) }
	var isStatusToggling by remember { mutableStateOf(false) }
	var tempTonnageEnabled by remember { mutableStateOf(quota.temporaryTonnageEnabled) }
	var tempTonnageValue by remember { mutableStateOf(quota.temporaryTonnageValue?.toString() ?: "") }

	Card(
		modifier = modifier
			.fillMaxWidth()
			.animateContentSize(
				animationSpec = tween(
					durationMillis = 200,
					easing = FastOutSlowInEasing
				)
			),
		shape = RoundedCornerShape(10.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
		),
		border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
	) {
		Column {
			// هدر کوتاژ - نمایش شماره کوتاژ و صاحب کالا
			Surface(
				onClick = onToggleExpand,
				color = Color.Transparent
			) {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(12.dp),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					Row(
						horizontalArrangement = Arrangement.spacedBy(10.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						// آیکون کوتاژ با نشانگر وضعیت
						Box(
							modifier = Modifier
								.size(36.dp)
								.background(
									color = if (quota.isActive)
										MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
									else
										MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
									shape = CircleShape
								),
							contentAlignment = Alignment.Center
						) {
							Icon(
								imageVector = Icons.Default.Description,
								contentDescription = null,
								tint = if (quota.isActive)
									MaterialTheme.colorScheme.primary
								else
									MaterialTheme.colorScheme.error,
								modifier = Modifier.size(18.dp)
							)
						}

						// اطلاعات کوتاژ و صاحب کالا
						Column(
							verticalArrangement = Arrangement.spacedBy(4.dp),
							modifier = Modifier.weight(1f)
						) {
							// شماره کوتاژ و انبار در یک خط
							Row(
								modifier = Modifier.fillMaxWidth(),
								horizontalArrangement = Arrangement.SpaceBetween,
								verticalAlignment = Alignment.CenterVertically
							) {
								Text(
									text = "کوتاژ ${quota.number}",
									style = MaterialTheme.typography.titleSmall,
									fontWeight = FontWeight.Bold,
									color = MaterialTheme.colorScheme.onSurface
								)

								CompactStatChip(
									icon = Icons.Default.Warehouse,
									value = quota.warehouse,
									color = MaterialTheme.colorScheme.tertiary
								)
							}

							// صاحب کالا و وضعیت
							Row(
								modifier = Modifier.fillMaxWidth(),
								horizontalArrangement = Arrangement.SpaceBetween,
								verticalAlignment = Alignment.CenterVertically
							) {
								Row(
									horizontalArrangement = Arrangement.spacedBy(4.dp),
									verticalAlignment = Alignment.CenterVertically
								) {
									Icon(
										imageVector = Icons.Default.Person,
										contentDescription = null,
										tint = MaterialTheme.colorScheme.secondary,
										modifier = Modifier.size(14.dp)
									)
									Text(
										text = quota.cargoOwner,
										style = MaterialTheme.typography.bodySmall,
										color = MaterialTheme.colorScheme.secondary,
										fontWeight = FontWeight.Medium
									)
								}

								Row(
									horizontalArrangement = Arrangement.spacedBy(6.dp),
									verticalAlignment = Alignment.CenterVertically
								) {
									Switch(
										checked = quota.isActive,
										onCheckedChange = {
											isStatusToggling = true
											viewModel.toggleQuotaStatus(quota.number) {
												isStatusToggling = false
												onRefreshData()
											}
										},
										enabled = !isStatusToggling
									)

									if (quota.temporaryTonnageEnabled && quota.temporaryTonnageValue != null) {
										CompactStatChip(
											icon = Icons.Default.Scale,
											value = "${formatNumber(quota.temporaryTonnageValue.roundToInt())} تن",
											color = MaterialTheme.colorScheme.primary
										)
									}
								}
							}
						}
					}

					// آیکون گسترش
					Icon(
						imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
						contentDescription = if (isExpanded) "بستن" else "گسترش",
						tint = MaterialTheme.colorScheme.onSurfaceVariant,
						modifier = Modifier.size(20.dp)
					)
				}
			}

			AnimatedVisibility(
				visible = isExpanded,
				enter = expandVertically(animationSpec = tween(200)) + fadeIn(),
				exit = shrinkVertically(animationSpec = tween(200)) + fadeOut()
			) {
				TempTonnageSection(
					quota = quota,
					tempTonnageEnabled = tempTonnageEnabled,
					tempTonnageValue = tempTonnageValue,
					isUpdating = isUpdating,
					isStatusToggling = isStatusToggling,
					onTempTonnageEnabledChange = { enabled ->
						tempTonnageEnabled = enabled
						// Automatically save when switch is toggled off
						if (!enabled) {
							isUpdating = true
							viewModel.updateTemporaryTonnage(
								quotaNumber = quota.number,
								enabled = false,
								tonnage = null
							) {
								isUpdating = false
								onRefreshData()
							}
						}
					},
					onTempTonnageValueChange = { newValue ->
						if (newValue.all { char -> char.isDigit() || char == '.' }) {
							tempTonnageValue = newValue
						}
					},
					onUpdateTonnage = {
						isUpdating = true
						viewModel.updateTemporaryTonnage(
							quotaNumber = quota.number,
							enabled = tempTonnageEnabled,
							tonnage = if (tempTonnageEnabled && tempTonnageValue.isNotEmpty()) {
								tempTonnageValue.toDoubleOrNull()
							} else null
						) {
							isUpdating = false
							onRefreshData()
						}
					},
					onStatusToggle = {
						isStatusToggling = true
						viewModel.toggleQuotaStatus(quota.number) {
							isStatusToggling = false
							onRefreshData()
						}
					}
				)
			}
		}
	}
}

@Composable
fun CompactStatChip(
	icon: ImageVector,
	value: String,
	color: Color,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier,
		shape = RoundedCornerShape(6.dp),
		color = color.copy(alpha = 0.1f)
	) {
		Row(
			modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
			horizontalArrangement = Arrangement.spacedBy(3.dp),
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
				style = MaterialTheme.typography.labelSmall,
				color = color,
				fontWeight = FontWeight.Medium
			)
		}
	}
}

@Composable
fun TempTonnageSection(
	quota: QuotaItem,
	tempTonnageEnabled: Boolean,
	tempTonnageValue: String,
	isUpdating: Boolean,
	isStatusToggling: Boolean,
	onTempTonnageEnabledChange: (Boolean) -> Unit,
	onTempTonnageValueChange: (String) -> Unit,
	onUpdateTonnage: () -> Unit,
	onStatusToggle: () -> Unit,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.padding(12.dp),
		verticalArrangement = Arrangement.spacedBy(12.dp)
	) {
		// اطلاعات ضروری کوتاژ به صورت فشرده
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			// شرکت باربری
			MinimalInfoChip(
				icon = Icons.Default.LocalShipping,
				value = quota.shippingCompany
			)

			// وضعیت کوتاژ
			MinimalStatusButton(
				isActive = quota.isActive,
				isLoading = isStatusToggling,
				onToggle = onStatusToggle
			)
		}

		// بخش مدیریت تناژ موقت
		Card(
			modifier = Modifier.fillMaxWidth(),
			shape = RoundedCornerShape(12.dp),
			colors = CardDefaults.cardColors(
				containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
			),
			border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
		) {
			Column(
				modifier = Modifier.padding(12.dp),
				verticalArrangement = Arrangement.spacedBy(12.dp)
			) {
				// هدر تناژ موقت
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(8.dp)
					) {
						Icon(
							imageVector = Icons.Default.Scale,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.primary,
							modifier = Modifier.size(20.dp)
						)
						Text(
							text = "تناژ موقت",
							style = MaterialTheme.typography.titleSmall,
							color = MaterialTheme.colorScheme.primary,
							fontWeight = FontWeight.Bold
						)
					}

					Switch(
						checked = tempTonnageEnabled,
						onCheckedChange = onTempTonnageEnabledChange,
						enabled = !isUpdating
					)
				}

				// فیلد ورودی تناژ
				AnimatedVisibility(
					visible = tempTonnageEnabled,
					enter = expandVertically() + fadeIn(),
					exit = shrinkVertically() + fadeOut()
				) {
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.spacedBy(12.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						OutlinedTextField(
							value = tempTonnageValue,
							onValueChange = { newValue ->
								val filteredValue = newValue.filter { it.isDigit() }
								onTempTonnageValueChange(filteredValue)
							},
							modifier = Modifier.weight(1f),
							label = {
								Text(
									"مقدار (کیلوگرم)",
									style = MaterialTheme.typography.labelMedium
								)
							},
							keyboardOptions = KeyboardOptions(
								keyboardType = KeyboardType.Number,
								imeAction = ImeAction.Done
							),
							enabled = !isUpdating,
							singleLine = true,
							shape = RoundedCornerShape(12.dp),
							colors = OutlinedTextFieldDefaults.colors(
								focusedBorderColor = MaterialTheme.colorScheme.primary,
								focusedLabelColor = MaterialTheme.colorScheme.primary
							)
						)

						// دکمه ذخیره
						if (isUpdating) {
							Box(
								modifier = Modifier
									.size(48.dp)
									.background(
										color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
										shape = CircleShape
									),
								contentAlignment = Alignment.Center
							) {
								CircularProgressIndicator(
									modifier = Modifier.size(24.dp),
									strokeWidth = 2.dp,
									color = MaterialTheme.colorScheme.primary
								)
							}
						} else {
							FilledIconButton(
								onClick = onUpdateTonnage,
								enabled = tempTonnageValue.isNotEmpty(),
								modifier = Modifier.size(48.dp),
								colors = IconButtonDefaults.filledIconButtonColors(
									containerColor = if (tempTonnageValue.isNotEmpty())
										MaterialTheme.colorScheme.primary
									else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
									contentColor = if (tempTonnageValue.isNotEmpty())
										MaterialTheme.colorScheme.onPrimary
									else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
								)
							) {
								Icon(
									imageVector = Icons.Default.Save,
									contentDescription = "ذخیره تناژ",
									modifier = Modifier.size(20.dp)
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
fun MinimalInfoChip(
	icon: ImageVector,
	value: String,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier,
		shape = RoundedCornerShape(8.dp),
		color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
	) {
		Row(
			modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
			horizontalArrangement = Arrangement.spacedBy(4.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				imageVector = icon,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.size(14.dp)
			)
			Text(
				text = value,
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				fontWeight = FontWeight.Bold,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}
	}
}

@Composable
fun MinimalStatusButton(
	isActive: Boolean,
	isLoading: Boolean,
	onToggle: () -> Unit,
	modifier: Modifier = Modifier
) {
	val containerColor = if (isActive) {
		Color(0xFF4CAF50).copy(alpha = 0.15f)
	} else {
		Color(0xFFF44336).copy(alpha = 0.15f)
	}
	val contentColor = if (isActive) {
		Color(0xFF2E7D32)
	} else {
		Color(0xFFC62828)
	}

	if (isLoading) {
		Box(
			modifier = modifier
				.background(
					color = containerColor,
					shape = RoundedCornerShape(8.dp)
				)
				.padding(horizontal = 12.dp, vertical = 6.dp),
			contentAlignment = Alignment.Center
		) {
			CircularProgressIndicator(
				modifier = Modifier.size(16.dp),
				strokeWidth = 2.dp,
				color = contentColor
			)
		}
	} else {
		FilledTonalButton(
			onClick = onToggle,
			modifier = modifier,
			colors = ButtonDefaults.filledTonalButtonColors(
				containerColor = containerColor,
				contentColor = contentColor
			),
			shape = RoundedCornerShape(8.dp)
		) {
			Icon(
				imageVector = if (isActive) Icons.Default.Check else Icons.Default.Close,
				contentDescription = null,
				modifier = Modifier.size(14.dp)
			)
			Spacer(modifier = Modifier.width(4.dp))
			Text(
				text = if (isActive) "فعال" else "غیرفعال",
				style = MaterialTheme.typography.labelSmall,
				fontWeight = FontWeight.Medium
			)
		}
	}
}

@Composable
fun QuotaShipExpansionPanel(
	shipName: String,
	cargoOwners: Map<String, List<QuotaItem>>,
	isExpanded: Boolean,
	onToggleExpand: () -> Unit,
	expandedQuota: String?,
	onQuotaToggle: (String) -> Unit,
	onRefreshData: () -> Unit,
	viewModel: ReportsViewModel,
	allQuotasInactive: Boolean = false,
	modifier: Modifier = Modifier
) {
	val totalQuotas = cargoOwners.values.sumOf { it.size }
	val activeQuotas = cargoOwners.values.flatten().count { it.isActive }
	val totalWeight = cargoOwners.values.flatten().map { it.temporaryTonnageValue ?: 0.0f }.sum()

	Card(
		modifier = modifier
			.fillMaxWidth()
			.animateContentSize(
				animationSpec = tween(
					durationMillis = 200,
					easing = FastOutSlowInEasing
				)
			),
		shape = RoundedCornerShape(12.dp),
		colors = CardDefaults.cardColors(
			containerColor = if (allQuotasInactive)
				MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
			else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
		),
		border = BorderStroke(
			width = if (allQuotasInactive) 2.dp else 1.dp,
			color = if (allQuotasInactive)
				MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
			else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
		)
	) {
		Column {
			// هدر کشتی
			Surface(
				onClick = onToggleExpand,
				color = Color.Transparent
			) {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(8.dp),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					Row(
						horizontalArrangement = Arrangement.spacedBy(12.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						// آیکون کشتی
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
								imageVector = Icons.Default.DirectionsBoat,
								contentDescription = null,
								tint = MaterialTheme.colorScheme.primary,
								modifier = Modifier.size(24.dp)
							)
						}

						// اطلاعات کشتی
						Column {
							Text(
								text = shipName,
								style = MaterialTheme.typography.titleMedium,
								fontWeight = FontWeight.Bold,
								color = MaterialTheme.colorScheme.onSurface
							)

							// آمار کشتی
							Row(
								horizontalArrangement = Arrangement.spacedBy(8.dp),
								verticalAlignment = Alignment.CenterVertically
							) {
								AnalyticsStatChip(
									value = "$totalQuotas",
									label = "کوتاژ",
									color = MaterialTheme.colorScheme.secondary
								)
								AnalyticsStatChip(
									value = "$activeQuotas",
									label = "فعال",
									color = MaterialTheme.colorScheme.tertiary
								)
								if (totalWeight > 0) {
									AnalyticsStatChip(
										value = formatNumber(totalWeight.roundToInt()),
										label = "تن",
										color = MaterialTheme.colorScheme.error
									)
								}
							}
						}
					}

					// آیکون گسترش
					Icon(
						imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
						contentDescription = if (isExpanded) "بستن" else "گسترش",
						tint = MaterialTheme.colorScheme.onSurfaceVariant,
						modifier = Modifier.size(24.dp)
					)
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
						.heightIn(max = 400.dp)
						.verticalScroll(rememberScrollState())
						.padding(12.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					// تجمیع تمام کوتاژها و مرتب‌سازی بر اساس صاحب کالا
					val allQuotas = cargoOwners.values.flatten()
						.sortedWith(
							compareBy<QuotaItem> { it.cargoOwner }
								.thenByDescending { it.isActive }
								.thenBy { it.number }
						)

					// نمایش مستقیم تمام کوتاژها با اطلاعات صاحب کالا
					allQuotas.forEach { quota ->
						IntegratedQuotaCard(
							quota = quota,
							isExpanded = expandedQuota == quota.quotaKey,
							onToggleExpand = { onQuotaToggle(quota.quotaKey) },
							onRefreshData = onRefreshData,
							viewModel = viewModel
						)
					}
				}
			}
		}
	}
}

@Composable
private fun LoadingActionButton(
	label: String,
	color: Color,
	modifier: Modifier = Modifier
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = modifier
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
					text = formatWeightWithDetail(diff.toFloat()),
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

		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(8.dp)
		) {
			CompactInfoSection(
				title = "اطلاعات اصلی",
				items = listOf(
					"شماره کوتاژ" to warning.quotaNumber,
					"درصد تنظیم شده" to "${warning.percentage.format(2)}%"
				),
				modifier = Modifier.weight(1f)
			)

			CompactInfoSection(
				title = "جزئیات تناژ",
				items = listOf(
					"تناژ درصد" to formatWeightWithDetail(warning.percentageAmount.toFloat()),
					"تناژ مانده" to formatWeightWithDetail(warning.remainingTonnage)
				),
				modifier = Modifier.weight(1f)
			)
		}
	}
}

@Composable
private fun CompactInfoSection(
	title: String,
	items: List<Pair<String, String>>,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.background(
				MaterialTheme.colorScheme.error.copy(alpha = 0.05f),
				RoundedCornerShape(8.dp)
			)
			.padding(6.dp),
		verticalArrangement = Arrangement.spacedBy(4.dp)
	) {
		Text(
			text = title,
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.error,
			fontWeight = FontWeight.Bold,
			modifier = Modifier.padding(bottom = 2.dp)
		)

		items.forEach { (label, value) ->
			Column {
				Text(
					text = label,
					style = MaterialTheme.typography.bodySmall.copy(
						fontWeight = FontWeight.Normal
					),
					color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
				Text(
					text = value,
					style = MaterialTheme.typography.bodySmall.copy(
						fontWeight = FontWeight.Bold
					),
					color = MaterialTheme.colorScheme.error,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
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

@Composable
fun QuotaPercentageDialog(
	quota: Quota,
	onDismiss: () -> Unit,
	onConfirm: (QuotaPercentageData) -> Unit
) {
	var percentage by remember { mutableDoubleStateOf(quota.percentage ?: 0.0) }
	var calculatedValues by remember { mutableStateOf(calculateValues(quota.totalTonnage, percentage, quota.remainingTonnage)) }

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
				.heightIn(max = 600.dp)
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

				// محتوای تنظیم درصد
				PercentageInputTab(
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
								isEnabled = if (percentage > 0.00) 1 else 0
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

private fun lerp(start: Int, end: Int, fraction: Float): Int {
	return (start + (end - start) * fraction).roundToInt()
}

@Composable
private fun PercentageInputTab(
	percentage: Double,
	calculatedValues: CalculationResult,
	onPercentageChange: (Double) -> Unit
) {
	var isFineMode by remember { mutableStateOf(true) }
	val adjustmentStep = if (isFineMode) 0.01 else 0.10

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

		Spacer(modifier = Modifier.height(16.dp))

		// Adjustment Mode Toggle
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.Center,
			verticalAlignment = Alignment.CenterVertically
		) {
			Card(
				shape = RoundedCornerShape(20.dp),
				colors = CardDefaults.cardColors(
					containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
				)
			) {
				Row(
					modifier = Modifier.padding(4.dp)
				) {
					// Fine mode button
					Button(
						onClick = { isFineMode = true },
						colors = ButtonDefaults.buttonColors(
							containerColor = if (isFineMode) {
								MaterialTheme.colorScheme.primary
							} else {
								Color.Transparent
							},
							contentColor = if (isFineMode) {
								MaterialTheme.colorScheme.onPrimary
							} else {
								MaterialTheme.colorScheme.onSurface
							}
						),
						shape = RoundedCornerShape(16.dp),
						modifier = Modifier.height(32.dp)
					) {
						Text(
							text = "دقیق (0.01%)",
							style = MaterialTheme.typography.bodySmall,
							fontWeight = FontWeight.Medium
						)
					}

					Spacer(modifier = Modifier.width(4.dp))

					// Coarse mode button
					Button(
						onClick = { isFineMode = false },
						colors = ButtonDefaults.buttonColors(
							containerColor = if (!isFineMode) {
								MaterialTheme.colorScheme.primary
							} else {
								Color.Transparent
							},
							contentColor = if (!isFineMode) {
								MaterialTheme.colorScheme.onPrimary
							} else {
								MaterialTheme.colorScheme.onSurface
							}
						),
						shape = RoundedCornerShape(16.dp),
						modifier = Modifier.height(32.dp)
					) {
						Text(
							text = "سریع (0.1%)",
							style = MaterialTheme.typography.bodySmall,
							fontWeight = FontWeight.Medium
						)
					}
				}
			}
		}

		Spacer(modifier = Modifier.height(16.dp))

		// Percentage Controls
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			// Decrease Button
			EnhancedIconButton(
				onClick = { onPercentageChange(percentage - adjustmentStep) },
				enabled = percentage > 0.00,
				icon = Icons.Default.Remove
			)

			// Current Percentage Display
			PercentageDisplay(percentage)

			// Increase Button
			EnhancedIconButton(
				onClick = { onPercentageChange(percentage + adjustmentStep) },
				enabled = percentage < 2.00,
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
				indication = null,
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
				text = "%.2f%%".format(percentage),
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
		listOf(0.00, 0.50, 0.70, 1.00, 1.50).forEach { value ->
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
			text = "%.2f%%".format(value),
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
			value = formatNumber(calculatedValues.percentageAmount.toInt())
		)
		ResultRow(
			label = "مانده درصد",
			value = formatNumber(calculatedValues.remainingAfterPercentage.toInt())
		)
		ResultRow(
			label = "مانده کل درصد",
			value = formatNumber(calculatedValues.totalRemainingAfterPercentage.toInt())
		)
	}
}

@Composable
private fun ResultRow(
	label: String,
	value: String
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

		val weightValue = value.replace(",", "").toFloatOrNull() ?: 0f

		val (displayValue, suffix) = when {
			weightValue >= 1_000_000 -> {
				val thousandTons = (weightValue / 1_000).toInt()
				thousandTons to "هزار تن"
			}
			weightValue >= 1_000 -> {
				val tons = (weightValue).toInt()
				tons to "تن"
			}
			else -> {
				weightValue.toInt() to "کیلو"
			}
		}

		AnimatedNumber(
			targetValue = displayValue,
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
	modifier: Modifier = Modifier
) {
	val colors = listOf(
		MaterialTheme.colorScheme.primary,
		MaterialTheme.colorScheme.secondary,
		MaterialTheme.colorScheme.tertiary,
		MaterialTheme.colorScheme.error,
		MaterialTheme.colorScheme.surfaceTint,
		MaterialTheme.colorScheme.inversePrimary
	)

	Column(
		modifier = modifier.fillMaxWidth(),
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		// لیست انبارها
		LazyColumn(
			verticalArrangement = Arrangement.spacedBy(12.dp),
			contentPadding = PaddingValues(vertical = 4.dp)
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
}

@Composable
private fun WarehouseCard(
	warehouse: Warehouse,
	color: Color,
	onClick: () -> Unit
) {
	// استفاده از اطلاعات کلی انبار
	val totalTonnage = warehouse.totalTonnage
	val loadedTonnage = warehouse.loadedTonnage
	val remainingTonnage = warehouse.remainingTonnage

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
						fontWeight = FontWeight.Bold
					)
				}

				// تعداد کوتاژ
				Badge(
					containerColor = color.copy(alpha = 0.1f),
					contentColor = color
				) {
					Text(
						text = "${warehouse.quotaCount} کوتاژ",
						style = MaterialTheme.typography.bodySmall,
						fontWeight = FontWeight.Bold,
						modifier = Modifier.padding(horizontal = 4.dp)
					)
				}
			}

			// نوار پیشرفت
			val progress = if (totalTonnage > 0) loadedTonnage / totalTonnage else 0f

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
						text = "${formatNumber(loadedTonnage.toInt())} بارگیری",
						style = MaterialTheme.typography.bodySmall,
						fontWeight = FontWeight.Bold,
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
						text = "${formatNumber(remainingTonnage.toInt())} مانده",
						style = MaterialTheme.typography.bodySmall,
						fontWeight = FontWeight.Bold,
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

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
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

fun persianDateFormat(dateString: String): String {
	// این تابع می‌تواند با کد واقعی تبدیل تاریخ جایگزین شود
	// در اینجا فقط قالب نمایش تاریخ را بهبود می‌دهیم
	return dateString
}

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
				InfoCard(
					icon = Icons.Default.DirectionsBoat,
					title = "کشتی",
					value = shipName,
					color = MaterialTheme.colorScheme.primary,
					modifier = Modifier.weight(1f)
				)

				// کارت انبار - طراحی افقی و مینیمال
				InfoCard(
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
fun InfoCard(
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
						QuotaChip(
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
fun QuotaChip(
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
							if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.error.copy(
								alpha = 0.7f
							)
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
								.background(
									MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.4f),
									CircleShape
								)
						)

						// وزن کل
						Text(
							text = formatNumber(summary.totalNetWeight.toInt()),
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
							VoucherItem(voucher)
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
	onSortTypeChange: (VoucherSortType) -> Unit,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier.fillMaxWidth(),
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
						SortChip(
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
						SortChip(
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
fun VoucherItem(voucher: VoucherDetail) {
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
					VoucherDetails(voucher)
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
fun VoucherDetails(voucher: VoucherDetail) {
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

		// بخش اطلاعات کاربران (باسکولچی و تائیدکننده)
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			// باسکولچی
			voucher.username?.let { username ->
				VoucherInfoItem(
					icon = Icons.Default.Person,
					value = username,
					label = "باسکولچی"
				)
			}

			// تائیدکننده
			Column(
				horizontalAlignment = Alignment.End
			) {
				voucher.confirmUsername?.let { confirmUser ->
					UserInfo(
						label = "تائیدکننده",
						username = confirmUser,
						isConfirmed = true
					)
				} ?: run {
					UserInfo(
						label = "تائید نشده",
						username = "در انتظار تائید",
						isConfirmed = false
					)
				}
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
fun SortChip(
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
	) {
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
		containerColor = MaterialTheme.colorScheme.surface
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
	isExpanded: Boolean = false,
	onExpandToggle: (Boolean) -> Unit = { _ -> },
	onEdit: (String, QuotaEditData) -> Unit,
	onToggleStatus: (String) -> Unit,
	onDelete: (Quota) -> Unit,
	onPercentageChange: (QuotaPercentageData) -> Unit
) {
	var showEditDialog by remember { mutableStateOf(false) }
	var showDeleteDialog by remember { mutableStateOf(false) }
	var showToggleDialog by remember { mutableStateOf(false) }
	var showPercentageDialog by remember { mutableStateOf(false) }
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
	val progress = calculateProgress(quota.loadedTonnage, quota.totalTonnage)

	Card(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.clickable { onExpandToggle(!isExpanded) },
		colors = CardDefaults.cardColors(containerColor = cardColor),
		border = BorderStroke(
			width = 1.dp,
			color = if (quota.isActive) {
				accentColor.copy(alpha = 0.2f)
			} else {
				MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
			}
		)
	) {
		Column(modifier = Modifier.padding(10.dp)) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
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
							horizontalArrangement = Arrangement.spacedBy(4.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
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

							Icon(
								imageVector = Icons.Default.Scale,
								contentDescription = null,
								tint = accentColor,
								modifier = Modifier.size(12.dp)
							)
							val calculatedValues = calculateValues(
								totalTonnage = quota.totalTonnage,
								percentage = quota.percentage ?: 0.0,
								remainingTonnage = quota.remainingTonnage
							)
							Text(
								text = "${formatWeightWithDetail(calculatedValues.totalRemainingAfterPercentage.toFloat())} (%.2f%%)".format(quota.percentage ?: 0.0),
								style = MaterialTheme.typography.bodySmall,
								color = accentColor,
								fontWeight = FontWeight.Medium
							)
						}
					}
				}

				Row(
					horizontalArrangement = Arrangement.spacedBy(4.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Icon(
						imageVector = if (quota.isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
						contentDescription = if (quota.isActive) "فعال" else "غیرفعال",
						tint = if (quota.isActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
						modifier = Modifier.size(16.dp)
					)

					if (!isExpanded) {
						Text(
							text = "${(progress * 100).toInt()}%",
							style = MaterialTheme.typography.labelMedium,
							color = accentColor,
							fontWeight = FontWeight.Bold
						)
					}

					Icon(
						imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
						contentDescription = if (isExpanded) "بستن" else "بازکردن",
						tint = contentColor.copy(alpha = 0.7f)
					)
				}
			}

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

			AnimatedVisibility(
				visible = isExpanded,
				enter = expandVertically() + fadeIn(),
				exit = shrinkVertically() + fadeOut()
			) {
				Column(
					modifier = Modifier.padding(top = 12.dp),
					verticalArrangement = Arrangement.spacedBy(12.dp)
				) {
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
							StatItem("تناژ کل",
								formatNumber(quota.totalTonnage.toInt()), accentColor)
							VerticalDivider(
								modifier = Modifier.height(24.dp),
								color = contentColor.copy(alpha = 0.1f)
							)
							StatItem("بارگیری شده",
								formatNumber(quota.loadedTonnage.toInt()), accentColor)
							VerticalDivider(
								modifier = Modifier.height(24.dp),
								color = contentColor.copy(alpha = 0.1f)
							)
							StatItem("تعداد حواله", formatNumber(quota.voucherCount), accentColor)
						}
					}

					HorizontalDivider(
						color = contentColor.copy(alpha = 0.1f)
					)

					Row(
						modifier = Modifier
							.fillMaxWidth()
							.padding(8.dp),
						horizontalArrangement = Arrangement.SpaceEvenly
					) {
						ActionButton(
							icon = Icons.Default.Edit,
							label = "ویرایش",
							color = Blue700,
							onClick = { showEditDialog = true }
						)
						ActionButton(
							icon = Icons.Default.Build,
							label = "درصد",
							color = Purple700,
							onClick = { showPercentageDialog = true }
						)
						ActionButton(
							icon = if (quota.isActive) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
							label = if (quota.isActive) "غیرفعال" else "فعال",
							color = if (quota.isActive) Red500 else Green700,
							onClick = { showToggleDialog = true }
						)
						ActionButton(
							icon = Icons.Default.Delete,
							label = "حذف",
							color = Red900,
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
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = modifier.clickable(onClick = onClick)
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
			color = color,
			textAlign = TextAlign.Center,
			maxLines = 2,
			overflow = TextOverflow.Ellipsis
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
				.heightIn(max = 700.dp)
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
				// هدر دیالوگ
				EditQuotaDialogHeader(quotaData.quotaNumber)

				Spacer(modifier = Modifier.height(24.dp))

				// فیلدهای ورودی
				OutlinedTextField(
					value = editedData.quotaNumber,
					onValueChange = {
						editedData = editedData.copy(quotaNumber = formatNumber(it))
					},
					label = { Text("شماره کوتاژ") },
					singleLine = true,
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
					isError = !isValidQuotaNumber(editedData.quotaNumber),
					modifier = Modifier.fillMaxWidth(),
					shape = RoundedCornerShape(12.dp),
					colors = OutlinedTextFieldDefaults.colors(
						focusedBorderColor = MaterialTheme.colorScheme.primary,
						unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
					)
				)
				if (!isValidQuotaNumber(editedData.quotaNumber)) {
					Text(
						"شماره کوتاژ باید حداقل 5 رقم باشد",
						color = MaterialTheme.colorScheme.error,
						style = MaterialTheme.typography.bodySmall,
						modifier = Modifier.padding(start = 16.dp, top = 4.dp)
					)
				}
				
				Spacer(modifier = Modifier.height(12.dp))

				OutlinedTextField(
					value = editedData.shipName,
					onValueChange = {
						editedData = editedData.copy(shipName = formatNumber(it).uppercase(Locale.ROOT))
					},
					label = { Text("نام کشتی") },
					singleLine = true,
					modifier = Modifier.fillMaxWidth(),
					shape = RoundedCornerShape(12.dp),
					colors = OutlinedTextFieldDefaults.colors(
						focusedBorderColor = MaterialTheme.colorScheme.primary,
						unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
					)
				)
				
				Spacer(modifier = Modifier.height(12.dp))

				OutlinedTextField(
					value = editedData.shippingCompany,
					onValueChange = {
						editedData = editedData.copy(shippingCompany = formatNumber(it))
					},
					label = { Text("شرکت باربری") },
					singleLine = true,
					modifier = Modifier.fillMaxWidth(),
					shape = RoundedCornerShape(12.dp),
					colors = OutlinedTextFieldDefaults.colors(
						focusedBorderColor = MaterialTheme.colorScheme.primary,
						unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
					)
				)
				
				Spacer(modifier = Modifier.height(12.dp))

				OutlinedTextField(
					value = editedData.warehouse,
					onValueChange = {
						editedData = editedData.copy(warehouse = formatNumber(it))
					},
					label = { Text("انبار") },
					singleLine = true,
					modifier = Modifier.fillMaxWidth(),
					shape = RoundedCornerShape(12.dp),
					colors = OutlinedTextFieldDefaults.colors(
						focusedBorderColor = MaterialTheme.colorScheme.primary,
						unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
					)
				)
				
				Spacer(modifier = Modifier.height(12.dp))

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
							.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
						shape = RoundedCornerShape(12.dp),
						colors = OutlinedTextFieldDefaults.colors(
							focusedBorderColor = MaterialTheme.colorScheme.primary,
							unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
						)
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
				
				Spacer(modifier = Modifier.height(12.dp))

				OutlinedTextField(
					value = formatNumber(editedData.totalTonnage.toInt().toString()),
					onValueChange = {
						val newValue = formatNumber(it).toIntOrNull() ?: editedData.totalTonnage.toInt()
						editedData = editedData.copy(totalTonnage = newValue.toFloat())
					},
					label = { Text("تناژ کل") },
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
					singleLine = true,
					modifier = Modifier.fillMaxWidth(),
					shape = RoundedCornerShape(12.dp),
					colors = OutlinedTextFieldDefaults.colors(
						focusedBorderColor = MaterialTheme.colorScheme.primary,
						unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
					)
				)

				Spacer(modifier = Modifier.height(24.dp))

				// دکمه‌های عملیات
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(16.dp)
				) {
					Button(
						onClick = {
							if (isValidQuotaNumber(editedData.quotaNumber)) {
								showConfirmationDialog = true
							}
						},
						modifier = Modifier.weight(1f),
						enabled = editedData != quotaData && isValidQuotaNumber(editedData.quotaNumber),
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

	// دیالوگ تأییدیه
	if (showConfirmationDialog) {
		Dialog(
			onDismissRequest = { showConfirmationDialog = false },
			properties = DialogProperties(
				dismissOnBackPress = true,
				dismissOnClickOutside = false,
				usePlatformDefaultWidth = false
			)
		) {
			Surface(
				modifier = Modifier
					.fillMaxWidth(0.9f)
					.wrapContentHeight()
					.padding(16.dp),
				shape = RoundedCornerShape(24.dp),
				tonalElevation = 6.dp,
				color = MaterialTheme.colorScheme.surface
			) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(24.dp)
				) {
					// هدر دیالوگ تأییدیه
					ConfirmationDialogHeader()

					Spacer(modifier = Modifier.height(24.dp))

					// متن توضیحات
					Text(
						text = "آیا از تغییرات انجام شده مطمئن هستید",
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
						textAlign = TextAlign.Center,
						modifier = Modifier.fillMaxWidth()
					)

					Spacer(modifier = Modifier.height(24.dp))

					// دکمه‌های عملیات
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.spacedBy(16.dp)
					) {
						Button(
							onClick = {
								onConfirm(editedData)
								showConfirmationDialog = false
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
								Text("ذخیره")
							}
						}

						OutlinedButton(
							onClick = { showConfirmationDialog = false },
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
}

@Composable
private fun ConfirmationDialogHeader() {
	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.Center
	) {
		// آیکون انیمیشن‌دار
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
				imageVector = Icons.Default.Warning,
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
				text = "تأیید ذخیره‌سازی",
				style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Bold
			)
			Text(
				text = "لطفاً تصمیم خود را تأیید کنید",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
			)
		}
	}
}

@Composable
private fun EditQuotaDialogHeader(quotaNumber: String) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically
	) {
		// آیکون انیمیشن‌دار
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
				imageVector = Icons.Default.Edit,
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
				text = "ویرایش اطلاعات کوتاژ",
				style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Bold
			)
			Text(
				text = "شماره کوتاژ: $quotaNumber",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
			)
		}
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
			sheetState = rememberModalBottomSheetState()
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
						SearchField(
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
							onWeightDetailsClick = { }
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
						horizontalArrangement = Arrangement.spacedBy(4.dp)
					) {
						// باربری
						CompactInfo(
							icon = Icons.Default.LocalShipping,
							label = "باربری",
							value = data.shippingCompany,
							color = color,
							modifier = Modifier.weight(1.3f)
						)

						// وزن خالص
						CompactInfo(
							icon = Icons.Default.Scale,
							label = "وزن خالص",
							value = formatNumber(data.totalNetWeight),
							color = color,
							modifier = Modifier.weight(1f)
						)

						// ساعت ورود و خروج
						CompactInfo(
							icon = Icons.AutoMirrored.Filled.Login,
							label = "ورود/خروج",
							value = "${data.exitVouchers}/${data.entryVouchers}",
							color = color,
							modifier = Modifier.weight(1f)
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
				StatCounter(
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
				StatCounter(
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
				StatCounter(
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
				StatCounter(
					value = animatedTotalWeight.toInt(),
					label = "وزن کل",
					icon = Icons.Default.Scale,
					color = MaterialTheme.colorScheme.error,
					suffix = ""
				)
			}
		}
	}
}

@Composable
private fun StatCounter(
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

@SuppressLint("ReturnFromAwaitPointerEventScope")
@Composable
fun FloatingActionButton(
	onRealTimeLoadingClick: () -> Unit,
	onAdvancedSearchClick: () -> Unit,
	onAnalyticsClick: () -> Unit,
	onDateRangeClick: (() -> Unit)? = null,
	onQuotaManagementClick: (() -> Unit)? = null
) {
	var expandedFab by remember { mutableStateOf(false) }
	var isTransparent by remember { mutableStateOf(true) }
	var longPressStartTime by remember { mutableLongStateOf(0L) }
	var isPressed by remember { mutableStateOf(false) }

	// انیمیشن‌های بهبود یافته
	val rotation by animateFloatAsState(
		targetValue = if (expandedFab) 45f else 0f,
		animationSpec = spring(
			dampingRatio = Spring.DampingRatioMediumBouncy,
			stiffness = Spring.StiffnessMedium
		),
		label = "fab_rotation"
	)

	val alpha by animateFloatAsState(
		targetValue = if (isTransparent) 0.6f else 1.0f,
		animationSpec = tween(300),
		label = "fab_alpha"
	)

	val scale by animateFloatAsState(
		targetValue = if (isPressed) 0.95f else 1.0f,
		animationSpec = spring(
			dampingRatio = Spring.DampingRatioMediumBouncy,
			stiffness = Spring.StiffnessHigh
		),
		label = "fab_scale"
	)

	Box(
		modifier = Modifier
			.fillMaxSize()
			.windowInsetsPadding(WindowInsets.navigationBars),
		contentAlignment = Alignment.BottomEnd
	) {
		// پس‌زمینه تیره هنگام باز بودن منو
		AnimatedVisibility(
			visible = expandedFab,
			enter = fadeIn(animationSpec = tween(200)),
			exit = fadeOut(animationSpec = tween(200))
		) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(Color.Black.copy(alpha = 0.3f))
					.clickable { expandedFab = false }
			)
		}

		Column(
			horizontalAlignment = Alignment.End,
			verticalArrangement = Arrangement.Bottom,
			modifier = Modifier.padding(16.dp)
		) {
			// منوی آیتم‌ها با انیمیشن بهبود یافته
			AnimatedVisibility(
				visible = expandedFab,
				enter = fadeIn(animationSpec = tween(300, delayMillis = 100)) +
						slideInVertically(
							animationSpec = spring(
								dampingRatio = Spring.DampingRatioMediumBouncy,
								stiffness = Spring.StiffnessMedium
							),
							initialOffsetY = { it / 2 }
						),
				exit = fadeOut(animationSpec = tween(200)) +
						slideOutVertically(
							animationSpec = tween(200),
							targetOffsetY = { it / 2 }
						)
			) {
				Column(
					verticalArrangement = Arrangement.spacedBy(16.dp),
					horizontalAlignment = Alignment.End
				) {
					// آیتم‌های منو با تاخیر انیمیشن
					val items = buildList {
						add(
							FabItem(
								icon = Icons.Default.Refresh,
								label = "بارگیری لحظه‌ای",
								onClick = onRealTimeLoadingClick
							)
						)
						add(
							FabItem(
								icon = Icons.Default.Search,
								label = "جستجوی پیشرفته",
								onClick = onAdvancedSearchClick
							)
						)
						add(
							FabItem(
								icon = Icons.Default.Analytics,
								label = "آمار جامع",
								onClick = onAnalyticsClick
							)
						)
						// نمایش آیتم مدیریت کوتاژها
						onQuotaManagementClick?.let { quotaManagementClick ->
							add(
								FabItem(
									icon = Icons.Default.ManageAccounts,
									label = "مدیریت کوتاژها",
									onClick = quotaManagementClick
								)
							)
						}
						// نمایش آیتم بازه زمانی فقط در تب کوتاژها
						onDateRangeClick?.let { dateRangeClick ->
							add(
								FabItem(
									icon = Icons.Default.DateRange,
									label = "بازه زمانی",
									onClick = dateRangeClick
								)
							)
						}
					}

					items.forEachIndexed { index, item ->
						MiniFab(
							item = item,
							index = index,
							onDismiss = { expandedFab = false }
						)
					}
				}
			}

			Spacer(modifier = Modifier.height(20.dp))

			// دکمه اصلی FAB
			Card(
				modifier = Modifier
					.size(56.dp)
					.scale(scale)
					.alpha(alpha)
					.clickable(
						interactionSource = remember { MutableInteractionSource() },
						indication = null
					) { expandedFab = !expandedFab }
					.pointerInput(Unit) {
						awaitPointerEventScope {
							while (true) {
								val event = awaitPointerEvent()
								val down = event.changes.firstOrNull()?.pressed == true

								if (down) {
									isPressed = true
									longPressStartTime = System.currentTimeMillis()

									do {
										val nextEvent = awaitPointerEvent()
										val stillDown =
											nextEvent.changes.firstOrNull()?.pressed == true
										if (!stillDown) {
											isPressed = false
											val pressDuration =
												System.currentTimeMillis() - longPressStartTime
											if (pressDuration > 1500) { // 1.5 seconds long press
												isTransparent = !isTransparent
											}
											break
										}
									} while (true)
								} else {
									isPressed = false
								}
							}
						}
					},
				shape = CircleShape,
				colors = CardDefaults.cardColors(
					containerColor = MaterialTheme.colorScheme.primary
				),
			) {
				Box(
					modifier = Modifier.fillMaxSize(),
					contentAlignment = Alignment.Center
				) {
					Icon(
						imageVector = Icons.Default.Add,
						contentDescription = "منو",
						tint = MaterialTheme.colorScheme.onPrimary,
						modifier = Modifier
							.size(28.dp)
							.rotate(rotation)
					)
				}
			}
		}
	}
}

@SuppressLint("ReturnFromAwaitPointerEventScope")
@Composable
private fun MiniFab(
	item: FabItem,
	index: Int,
	onDismiss: () -> Unit
) {
	var isPressed by remember { mutableStateOf(false) }
	var isHovered by remember { mutableStateOf(false) }
	val interactionSource = remember { MutableInteractionSource() }

	// انیمیشن‌های پیشرفته
	val scale by animateFloatAsState(
		targetValue = when {
			isPressed -> 0.92f
			isHovered -> 1.08f
			else -> 1f
		},
		animationSpec = spring(
			dampingRatio = Spring.DampingRatioMediumBouncy,
			stiffness = Spring.StiffnessHigh
		),
		label = "minifab_scale"
	)

	val labelAlpha by animateFloatAsState(
		targetValue = if (isHovered) 1f else 0.85f,
		animationSpec = tween(200),
		label = "label_alpha"
	)

	// انیمیشن ورود با تاخیر
	var visible by remember { mutableStateOf(false) }
	LaunchedEffect(Unit) {
		delay(index * 50L) // تاخیر بر اساس ایندکس
		visible = true
	}

	val slideOffset by animateFloatAsState(
		targetValue = if (visible) 0f else 100f,
		animationSpec = spring(
			dampingRatio = Spring.DampingRatioMediumBouncy,
			stiffness = Spring.StiffnessMedium
		),
		label = "slide_offset"
	)

	val fadeAlpha by animateFloatAsState(
		targetValue = if (visible) 1f else 0f,
		animationSpec = tween(300),
		label = "fade_alpha"
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
			.offset(x = slideOffset.dp)
			.alpha(fadeAlpha)
			.graphicsLayer {
				clip = false
			}
	) {
		// برچسب بهبود یافته
		Card(
			modifier = Modifier
				.padding(end = 16.dp)
				.alpha(labelAlpha),
			shape = RoundedCornerShape(12.dp),
			colors = CardDefaults.cardColors(
				containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
			),
		) {
			Text(
				text = item.label,
				style = MaterialTheme.typography.bodyMedium.copy(
					fontWeight = FontWeight.Medium
				),
				color = MaterialTheme.colorScheme.onSurface,
				modifier = Modifier.padding(
					horizontal = 16.dp,
					vertical = 8.dp
				)
			)
		}

		// دکمه کوچک بهبود یافته
		Card(
			modifier = Modifier
				.size(42.dp)
				.scale(scale)
				.hoverable(interactionSource)
				.clickable(
					interactionSource = interactionSource,
					indication = null
				) {
					item.onClick()
					onDismiss()
				}
				.pointerInput(Unit) {
					awaitPointerEventScope {
						while (true) {
							val event = awaitPointerEvent()
							val down = event.changes.firstOrNull()?.pressed == true
							isPressed = down
						}
					}
				},
			shape = CircleShape,
			colors = CardDefaults.cardColors(
				containerColor = MaterialTheme.colorScheme.secondaryContainer
			),
		) {
			Box(
				modifier = Modifier.fillMaxSize(),
				contentAlignment = Alignment.Center
			) {
				Icon(
					imageVector = item.icon,
					contentDescription = item.label,
					tint = MaterialTheme.colorScheme.onSecondaryContainer,
					modifier = Modifier.size(24.dp)
				)
			}
		}
	}
}

enum class SearchType {
	RECEIPT_NUMBER,
	TRACKING_NUMBER
}

@Composable
fun AdvancedSearchDialog(
	isOpen: Boolean,
	onDismiss: () -> Unit,
	onSearchReceipt: (String) -> Unit,
	onSearchTracking: (String) -> Unit
) {
	var searchNumber by remember { mutableStateOf("") }
	var selectedSearchType by remember { mutableStateOf(SearchType.RECEIPT_NUMBER) }

	if (isOpen) {
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
					.fillMaxWidth(0.92f)
					.fillMaxHeight(0.42f),
				shape = RoundedCornerShape(16.dp),
				color = MaterialTheme.colorScheme.surface,
				tonalElevation = 6.dp
			) {
				Column(
					modifier = Modifier
						.fillMaxSize()
						.background(MaterialTheme.colorScheme.background)
				) {
					// هدر دیالوگ
					SearchHeaderCard(
						onClose = onDismiss,
						selectedSearchType = selectedSearchType
					)

					// محتوای اصلی
					Column(
						modifier = Modifier
							.fillMaxSize()
							.padding(16.dp)
					) {
						Spacer(modifier = Modifier.height(8.dp))

						// نوار تب‌های نوع جستجو
						SearchTypeTabRow(
							selectedSearchType = selectedSearchType,
							onSearchTypeSelected = { 
								selectedSearchType = it
								searchNumber = ""
							}
						)

						Spacer(modifier = Modifier.height(24.dp))

						// فیلد جستجو
						OutlinedTextField(
							value = searchNumber,
							onValueChange = {
								if (it.all { char -> char.isDigit() }) {
									searchNumber = it
								}
							},
							modifier = Modifier.fillMaxWidth(),
							shape = RoundedCornerShape(12.dp),
							colors = OutlinedTextFieldDefaults.colors(
								focusedBorderColor = MaterialTheme.colorScheme.primary,
								unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
								focusedContainerColor = MaterialTheme.colorScheme.surface,
								unfocusedContainerColor = MaterialTheme.colorScheme.surface
							),
							leadingIcon = {
								Icon(
									imageVector = when (selectedSearchType) {
										SearchType.RECEIPT_NUMBER -> Icons.Default.Receipt
										SearchType.TRACKING_NUMBER -> Icons.Default.Numbers
									},
									contentDescription = null,
									tint = MaterialTheme.colorScheme.primary
								)
							},
							label = {
								Text(
									when (selectedSearchType) {
										SearchType.RECEIPT_NUMBER -> "شماره قبض باسکول"
										SearchType.TRACKING_NUMBER -> "شماره حواله"
									}
								)
							},
							keyboardOptions = KeyboardOptions(
								keyboardType = KeyboardType.Number,
								imeAction = ImeAction.Search
							),
							keyboardActions = KeyboardActions(
								onSearch = {
									if (searchNumber.isNotBlank()) {
										when (selectedSearchType) {
											SearchType.RECEIPT_NUMBER -> onSearchReceipt(searchNumber)
											SearchType.TRACKING_NUMBER -> onSearchTracking(searchNumber)
										}
									}
								}
							),
							singleLine = true
						)

						Spacer(modifier = Modifier.weight(1f))

						// دکمه‌های عملیات
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.spacedBy(12.dp)
						) {
							OutlinedButton(
								onClick = onDismiss,
								modifier = Modifier.weight(1f),
								shape = RoundedCornerShape(12.dp),
								border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
							) {
								Icon(
									imageVector = Icons.Default.Close,
									contentDescription = null,
									modifier = Modifier.size(18.dp)
								)
								Spacer(modifier = Modifier.width(8.dp))
								Text("انصراف")
							}

							Button(
								onClick = {
									if (searchNumber.isNotBlank()) {
										when (selectedSearchType) {
											SearchType.RECEIPT_NUMBER -> onSearchReceipt(searchNumber)
											SearchType.TRACKING_NUMBER -> onSearchTracking(searchNumber)
										}
									}
								},
								modifier = Modifier.weight(1f),
								enabled = searchNumber.isNotBlank(),
								shape = RoundedCornerShape(12.dp),
								colors = ButtonDefaults.buttonColors(
									containerColor = MaterialTheme.colorScheme.primary
								)
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

						Spacer(modifier = Modifier.height(8.dp))
					}
				}
			}
		}
	}
}

@Composable
private fun SearchHeaderCard(
	onClose: () -> Unit,
	selectedSearchType: SearchType
) {
	Surface(
		modifier = Modifier.fillMaxWidth(),
		color = MaterialTheme.colorScheme.primary
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 12.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			// نام دیالوگ و آیکون
			Row(
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				// آیکون جستجو
				Surface(
					shape = CircleShape,
					color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f),
					modifier = Modifier.size(48.dp)
				) {
					Box(contentAlignment = Alignment.Center) {
						Icon(
							imageVector = Icons.Default.Search,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.onPrimary,
							modifier = Modifier.size(24.dp)
						)
					}
				}

				// نام دیالوگ و توضیحات
				Column {
					Text(
						text = "جستجوی پیشرفته",
						style = MaterialTheme.typography.headlineSmall,
						color = MaterialTheme.colorScheme.onPrimary,
						fontWeight = FontWeight.Bold
					)

					Text(
						text = when (selectedSearchType) {
							SearchType.RECEIPT_NUMBER -> "جستجو بر اساس شماره قبض"
							SearchType.TRACKING_NUMBER -> "جستجو بر اساس شماره حواله"
						},
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
					)
				}
			}

			// دکمه بستن
			Surface(
				shape = CircleShape,
				color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f),
				modifier = Modifier.size(40.dp)
			) {
				IconButton(
					onClick = onClose,
					modifier = Modifier.fillMaxSize()
				) {
					Icon(
						imageVector = Icons.Default.Close,
						contentDescription = "بستن",
						tint = MaterialTheme.colorScheme.onPrimary,
						modifier = Modifier.size(20.dp)
					)
				}
			}
		}
	}
}

@Composable
private fun SearchTypeTabRow(
	selectedSearchType: SearchType,
	onSearchTypeSelected: (SearchType) -> Unit,
	modifier: Modifier = Modifier
) {
	val tabs = remember {
		listOf(
			SearchTabInfo(SearchType.RECEIPT_NUMBER, "شماره قبض", Icons.Default.Receipt),
			SearchTabInfo(SearchType.TRACKING_NUMBER, "شماره حواله", Icons.Default.Numbers)
		)
	}

	Surface(
		modifier = modifier.fillMaxWidth(),
		shape = RoundedCornerShape(8.dp),
		color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
		border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
	) {
		Row(
			modifier = Modifier.padding(2.dp),
			horizontalArrangement = Arrangement.spacedBy(2.dp)
		) {
			tabs.forEach { tab ->
				val isSelected = selectedSearchType == tab.type

				Surface(
					onClick = { onSearchTypeSelected(tab.type) },
					modifier = Modifier.weight(1f),
					shape = RoundedCornerShape(6.dp),
					color = if (isSelected) {
						MaterialTheme.colorScheme.primary
					} else {
						Color.Transparent
					}
				) {
					Row(
						modifier = Modifier
							.padding(horizontal = 12.dp, vertical = 10.dp),
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
						Spacer(modifier = Modifier.width(6.dp))
						Text(
							text = tab.label,
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
			}
		}
	}
}

private data class SearchTabInfo(
	val type: SearchType,
	val label: String,
	val icon: ImageVector
)

@Composable
fun MultipleSearchResultDialog(
	cargoInfoList: List<CargoInfo>,
	onDismiss: () -> Unit,
	onSelectCargo: (CargoInfo) -> Unit
) {
	val mainColor = MaterialTheme.colorScheme.primary

	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(
			dismissOnBackPress = true,
			dismissOnClickOutside = false,
			usePlatformDefaultWidth = false
		)
	) {
		Card(
			modifier = Modifier
				.fillMaxWidth(0.9f)
				.heightIn(max = 700.dp)
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
								imageVector = Icons.Default.List,
								contentDescription = null,
								tint = mainColor,
								modifier = Modifier.size(28.dp)
							)
						}
						Column {
							Text(
								text = "نتایج جستجو",
								style = MaterialTheme.typography.titleLarge,
								fontWeight = FontWeight.Bold,
								color = MaterialTheme.colorScheme.onPrimaryContainer
							)
							Text(
								text = "${cargoInfoList.size} نتیجه یافت شد",
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

				Spacer(modifier = Modifier.height(16.dp))

				// Results List
				LazyColumn(
					modifier = Modifier.weight(1f),
					verticalArrangement = Arrangement.spacedBy(12.dp)
				) {
					items(cargoInfoList) { cargoInfo ->
						val context = LocalContext.current
						CargoSearchResultCard(
							cargoInfo = cargoInfo,
							onClick = { onSelectCargo(cargoInfo) },
							mainColor = mainColor,
							onShare = { cargo ->
								// پیاده‌سازی اشتراک‌گذاری
								shareCargoInfo(cargo, context)
							}
						)
					}
				}

				Spacer(modifier = Modifier.height(16.dp))

				// Close Button
				Button(
					onClick = onDismiss,
					modifier = Modifier.fillMaxWidth(),
					shape = RoundedCornerShape(12.dp),
					colors = ButtonDefaults.buttonColors(
						containerColor = mainColor
					)
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
private fun CargoSearchResultCard(
	cargoInfo: CargoInfo,
	onClick: () -> Unit,
	mainColor: Color,
	onShare: (CargoInfo) -> Unit = {}
) {

	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onClick() },
		shape = RoundedCornerShape(12.dp),
		color = MaterialTheme.colorScheme.surface,
		border = BorderStroke(1.dp, mainColor.copy(alpha = 0.3f)),
		shadowElevation = 2.dp
	) {
		Column(
			modifier = Modifier.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			// Header Row
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					modifier = Modifier.weight(1f)
				) {
					Box(
						modifier = Modifier
							.size(32.dp)
							.background(mainColor.copy(alpha = 0.1f), CircleShape),
						contentAlignment = Alignment.Center
					) {
						Icon(
							imageVector = Icons.Default.Numbers,
							contentDescription = null,
							tint = mainColor,
							modifier = Modifier.size(18.dp)
						)
					}
					Text(
						text = "حواله: ${cargoInfo.trackingNumber}",
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.Bold,
						color = mainColor,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis
					)
				}

				Row(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					// Share Icon
					IconButton(
						onClick = { onShare(cargoInfo) },
						modifier = Modifier
							.size(32.dp)
							.background(
								color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
								shape = CircleShape
							)
					) {
						Icon(
							imageVector = Icons.Default.Share,
							contentDescription = "اشتراک‌گذاری",
							tint = mainColor,
							modifier = Modifier.size(16.dp)
						)
					}

					Icon(
						imageVector = Icons.Default.ChevronRight,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
					)
				}
			}

			// Main Info
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(16.dp)
			) {
				Column(
					modifier = Modifier.weight(1f),
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					val context = LocalContext.current
					InfoRowCompact(
						icon = Icons.Default.Receipt,
						label = "قبض باسکول",
						value = cargoInfo.scaleReceiptNumber,
						isClickable = true,
						onCopy = {
							// کپی شماره قبض باسکول
							val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
							val clipData = ClipData.newPlainText("شماره قبض باسکول", cargoInfo.scaleReceiptNumber)
							clipboardManager.setPrimaryClip(clipData)
							Toast.makeText(context, "شماره قبض باسکول کپی شد", Toast.LENGTH_SHORT).show()
						}
					)
					InfoRowCompact(
						icon = Icons.Default.Scale,
						label = "وزن خالص",
						value = "${formatNumber(cargoInfo.netWeight.toIntOrNull() ?: 0)} کیلوگرم"
					)
				}

				Column(
					modifier = Modifier.weight(1f),
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					InfoRowCompact(
						icon = Icons.Default.DirectionsBoat,
						label = "کشتی",
						value = cargoInfo.shipName
					)
					InfoRowCompact(
						icon = Icons.Default.LocalShipping,
						label = "شرکت",
						value = cargoInfo.shippingCompany
					)
				}
			}

			// Status and Time
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
						imageVector = Icons.Default.Schedule,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
						modifier = Modifier.size(16.dp)
					)
					Text(
						text = "ورود: ${cargoInfo.entryTime}",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
					)
				}

				Surface(
					shape = RoundedCornerShape(8.dp),
					color = if (cargoInfo.exitTime != null)
						Color(0xFF4CAF50).copy(alpha = 0.1f)
					else
						Color(0xFFFF9800).copy(alpha = 0.1f)
				) {
					Text(
						text = if (cargoInfo.exitTime != null) "خروج شده" else "در انتظار خروج",
						style = MaterialTheme.typography.bodySmall,
						color = if (cargoInfo.exitTime != null)
							Color(0xFF4CAF50)
						else
							Color(0xFFFF9800),
						modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
					)
				}
			}
		}
	}
}

@Composable
private fun InfoRowCompact(
	icon: ImageVector,
	label: String,
	value: String,
	isClickable: Boolean = false,
	onCopy: (() -> Unit)? = null
) {
	val context = LocalContext.current

	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(6.dp),
		modifier = if (isClickable && onCopy != null) {
			Modifier.clickable {
				onCopy()
				// کپی کردن مقدار در کلیپبرد
				val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
				val clip = ClipData.newPlainText(label, value)
				clipboard.setPrimaryClip(clip)

				// نمایش پیام تأیید
				Toast.makeText(context, "$label کپی شد", Toast.LENGTH_SHORT).show()
			}
		} else Modifier
	) {
		Icon(
			imageVector = icon,
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
			modifier = Modifier.size(14.dp)
		)
		Column(
			modifier = Modifier.weight(1f)
		) {
			Text(
				text = label,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			Text(
				text = value,
				style = MaterialTheme.typography.bodySmall,
				fontWeight = FontWeight.Medium,
				color = MaterialTheme.colorScheme.onSurface,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}

		// نمایش آیکن کپی برای آیتم‌های قابل کپی
		if (isClickable && onCopy != null) {
			Icon(
				imageVector = Icons.Default.ContentCopy,
				contentDescription = "کپی",
				tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
				modifier = Modifier.size(12.dp)
			)
		}
	}
}

@Composable
fun SearchResultDialog(
	cargoInfo: CargoInfo,
	onDismiss: () -> Unit,
	onRefresh: (CargoInfo) -> Unit,
	searchType: SearchType?,
	searchValue: String?,
	viewModel: ReportsViewModel
) {
	val mainColor = MaterialTheme.colorScheme.primary
	val snackbarHostState = remember { SnackbarHostState() }
	val scope = rememberCoroutineScope()
	
	var isEditMode by remember { mutableStateOf(false) }
	var isSaving by remember { mutableStateOf(false) }
	var showConfirmDialog by remember { mutableStateOf(false) }
	
	// Editable fields
	var editedTrackingNumber by remember { mutableStateOf(cargoInfo.trackingNumber) }
	var editedNumberOfPeople by remember { mutableStateOf(cargoInfo.numberOfPeople) }
	var editedEntryTime by remember { mutableStateOf(cargoInfo.entryTime) }
	var editedNetWeight by remember { mutableStateOf(cargoInfo.netWeight) }
	var editedScaleReceiptNumber by remember { mutableStateOf(cargoInfo.scaleReceiptNumber) }
	var editedShortageWeight by remember { mutableStateOf(cargoInfo.shortageWeight) }
	var editedExcessWeight by remember { mutableStateOf(cargoInfo.excessWeight) }
	var editedExitTime by remember { mutableStateOf(cargoInfo.exitTime ?: "") }
	var editedExitDate by remember { mutableStateOf(cargoInfo.exitDate ?: "") }
	var editedStatus by remember { mutableStateOf(cargoInfo.status) }
	var editedLoadingQuotaNumber by remember { mutableStateOf(cargoInfo.loadingQuotaNumber) }

	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(
			dismissOnBackPress = true,
			dismissOnClickOutside = false,
			usePlatformDefaultWidth = false
		)
	) {
		Scaffold(
			snackbarHost = {
				SnackbarHost(
					hostState = snackbarHostState,
					modifier = Modifier.padding(16.dp)
				)
			},
			containerColor = Color.Transparent
		) { paddingValues ->
			Card(
				modifier = Modifier
					.fillMaxWidth(0.95f)
					.fillMaxHeight(0.9f)
					.padding(paddingValues)
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
								imageVector = if (isEditMode) Icons.Default.Edit else Icons.Default.Receipt,
								contentDescription = null,
								tint = mainColor,
								modifier = Modifier.size(28.dp)
							)
						}
						Column {
							Text(
								text = if (isEditMode) "ویرایش اطلاعات" else "نتیجه جستجو",
								style = MaterialTheme.typography.titleLarge,
								fontWeight = FontWeight.Bold,
								color = MaterialTheme.colorScheme.onPrimaryContainer
							)
							Text(
								text = "قبض باسکول: ${if (isEditMode) editedScaleReceiptNumber else cargoInfo.scaleReceiptNumber}",
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

				Spacer(modifier = Modifier.height(16.dp))

				LazyColumn(
					modifier = Modifier.weight(1f),
					verticalArrangement = Arrangement.spacedBy(16.dp)
				) {
					if (isEditMode) {
						// Edit Mode
						item {
							EditableCargoMainInfo(
								trackingNumber = editedTrackingNumber,
								onTrackingNumberChange = { editedTrackingNumber = it },
								scaleReceiptNumber = editedScaleReceiptNumber,
								onScaleReceiptNumberChange = { editedScaleReceiptNumber = it },
								loadingQuotaNumber = editedLoadingQuotaNumber,
								onLoadingQuotaNumberChange = { editedLoadingQuotaNumber = it },
								numberOfPeople = editedNumberOfPeople,
								onNumberOfPeopleChange = { editedNumberOfPeople = it }
							)
						}

						item {
							EditableCargoWeightInfo(
								netWeight = editedNetWeight,
								onNetWeightChange = { editedNetWeight = it },
								shortageWeight = editedShortageWeight,
								onShortageWeightChange = { editedShortageWeight = it },
								excessWeight = editedExcessWeight,
								onExcessWeightChange = { editedExcessWeight = it }
							)
						}

						item {
							EditableCargoTimeInfo(
								entryTime = editedEntryTime,
								onEntryTimeChange = { editedEntryTime = it },
								exitTime = editedExitTime,
								onExitTimeChange = { editedExitTime = it },
								exitDate = editedExitDate,
								onExitDateChange = { editedExitDate = it },
								status = editedStatus,
								onStatusChange = { editedStatus = it }
							)
						}

						item {
							CargoShippingInfo(cargoInfo)
						}
					} else {
						// View Mode
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
				}

				Spacer(modifier = Modifier.height(16.dp))

				// Action Buttons
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					if (isEditMode) {
						Button(
							onClick = {
								if (cargoInfo.id == null) {
									scope.launch {
										snackbarHostState.showSnackbar("شناسه رکورد نامعتبر است")
									}
									return@Button
								}
								showConfirmDialog = true
							},
							modifier = Modifier.weight(1f),
							shape = RoundedCornerShape(12.dp),
							enabled = !isSaving
						) {
							if (isSaving) {
								CircularProgressIndicator(
									modifier = Modifier.size(20.dp),
									color = MaterialTheme.colorScheme.onPrimary,
									strokeWidth = 2.dp
								)
							} else {
								Icon(imageVector = Icons.Default.Save, contentDescription = null)
								Spacer(modifier = Modifier.width(8.dp))
								Text("ذخیره")
							}
						}

						Button(
							onClick = {
								isEditMode = false
								// Reset values
								editedTrackingNumber = cargoInfo.trackingNumber
								editedNumberOfPeople = cargoInfo.numberOfPeople
								editedEntryTime = cargoInfo.entryTime
								editedNetWeight = cargoInfo.netWeight
								editedScaleReceiptNumber = cargoInfo.scaleReceiptNumber
								editedShortageWeight = cargoInfo.shortageWeight
								editedExcessWeight = cargoInfo.excessWeight
								editedExitTime = cargoInfo.exitTime ?: ""
								editedExitDate = cargoInfo.exitDate ?: ""
								editedStatus = cargoInfo.status
								editedLoadingQuotaNumber = cargoInfo.loadingQuotaNumber
							},
							modifier = Modifier.weight(1f),
							shape = RoundedCornerShape(12.dp),
							colors = ButtonDefaults.buttonColors(
								containerColor = MaterialTheme.colorScheme.error
							),
							enabled = !isSaving
						) {
							Icon(imageVector = Icons.Default.Cancel, contentDescription = null)
							Spacer(modifier = Modifier.width(8.dp))
							Text("لغو")
						}
					} else {
						Button(
							onClick = { isEditMode = true },
							modifier = Modifier.weight(1f),
							shape = RoundedCornerShape(12.dp),
							enabled = cargoInfo.id != null
						) {
							Icon(imageVector = Icons.Default.Edit, contentDescription = null)
							Spacer(modifier = Modifier.width(8.dp))
							Text("ویرایش")
						}

						Button(
							onClick = onDismiss,
							modifier = Modifier.weight(1f),
							shape = RoundedCornerShape(12.dp)
						) {
							Icon(imageVector = Icons.Default.Close, contentDescription = null)
							Spacer(modifier = Modifier.width(8.dp))
							Text("بستن")
						}
					}
				}
			}
		}
	}
	
	// دیالوگ تأیید قبل از ذخیره
	if (showConfirmDialog) {
		CargoEditConfirmDialog(
			cargoInfo = cargoInfo,
			editedTrackingNumber = editedTrackingNumber,
			editedNumberOfPeople = editedNumberOfPeople,
			editedEntryTime = editedEntryTime,
			editedNetWeight = editedNetWeight,
			editedScaleReceiptNumber = editedScaleReceiptNumber,
			editedShortageWeight = editedShortageWeight,
			editedExcessWeight = editedExcessWeight,
			editedExitTime = editedExitTime,
			editedExitDate = editedExitDate,
			editedStatus = editedStatus,
			editedLoadingQuotaNumber = editedLoadingQuotaNumber,
			onDismiss = { showConfirmDialog = false },
			onConfirm = {
				showConfirmDialog = false
				isSaving = true
				
				val updatedCargo = cargoInfo.copy(
					trackingNumber = editedTrackingNumber,
					numberOfPeople = editedNumberOfPeople,
					entryTime = editedEntryTime,
					netWeight = editedNetWeight,
					scaleReceiptNumber = editedScaleReceiptNumber,
					shortageWeight = editedShortageWeight,
					excessWeight = editedExcessWeight,
					exitTime = editedExitTime.ifEmpty { null },
					exitDate = editedExitDate.ifEmpty { null },
					status = editedStatus,
					loadingQuotaNumber = editedLoadingQuotaNumber
				)
				
				Log.d("CargoEdit", "🔄 شروع بروزرسانی - ID: ${updatedCargo.id}")
				Log.d("CargoEdit", "📦 داده‌های ویرایش شده: $updatedCargo")
				
				viewModel.updateCargoInfo(updatedCargo) { result ->
					isSaving = false
					result.fold(
						onSuccess = { response ->
							Log.d("CargoEdit", "✅ پاسخ موفق: $response")
							if (response.error == false) {
								isEditMode = false
								
								// بروزرسانی خودکار از سرور
								if (searchType != null && searchValue != null) {
									when (searchType) {
										SearchType.RECEIPT_NUMBER -> {
											viewModel.performAdvancedSearch(searchValue) { searchResult ->
												searchResult.fold(
													onSuccess = { refreshedCargo ->
														if (refreshedCargo != null) {
															onRefresh(refreshedCargo)
															scope.launch {
																snackbarHostState.showSnackbar(
																	message = "✅ ${response.message}",
																	duration = SnackbarDuration.Short
																)
															}
														}
													},
													onFailure = {
														scope.launch {
															snackbarHostState.showSnackbar(
																message = "بروزرسانی انجام شد اما خطا در دریافت اطلاعات جدید",
																duration = SnackbarDuration.Long
															)
														}
													}
												)
											}
										}
										SearchType.TRACKING_NUMBER -> {
											// برای حواله، فقط پیام موفقیت نمایش بده
											scope.launch {
												snackbarHostState.showSnackbar(
													message = "✅ ${response.message}",
													duration = SnackbarDuration.Short
												)
											}
										}
									}
								} else {
									scope.launch {
										snackbarHostState.showSnackbar(
											message = "✅ ${response.message}",
											duration = SnackbarDuration.Short
										)
									}
								}
							} else {
								Log.e("CargoEdit", "❌ خطا در response: ${response.message}")
								scope.launch {
									snackbarHostState.showSnackbar(
										message = "❌ ${response.message}",
										duration = SnackbarDuration.Long
									)
								}
							}
						},
						onFailure = { error ->
							Log.e("CargoEdit", "💥 Exception: ${error.message}", error)
							scope.launch {
								snackbarHostState.showSnackbar(
									message = "❌ خطا: ${error.localizedMessage}",
									duration = SnackbarDuration.Long
								)
							}
						}
					)
				}
			}
		)
	}
	}
}

@Composable
fun CargoEditConfirmDialog(
	cargoInfo: CargoInfo,
	editedTrackingNumber: String,
	editedNumberOfPeople: String,
	editedEntryTime: String,
	editedNetWeight: String,
	editedScaleReceiptNumber: String,
	editedShortageWeight: String,
	editedExcessWeight: String,
	editedExitTime: String,
	editedExitDate: String,
	editedStatus: String,
	editedLoadingQuotaNumber: String,
	onDismiss: () -> Unit,
	onConfirm: () -> Unit
) {
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
				.heightIn(max = 700.dp)
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
				// Header با انیمیشن
				CargoEditConfirmHeader()

				Spacer(modifier = Modifier.height(24.dp))

				// نمایش تغییرات
				CargoChangesPreview(
					cargoInfo = cargoInfo,
					editedTrackingNumber = editedTrackingNumber,
					editedNumberOfPeople = editedNumberOfPeople,
					editedEntryTime = editedEntryTime,
					editedNetWeight = editedNetWeight,
					editedScaleReceiptNumber = editedScaleReceiptNumber,
					editedShortageWeight = editedShortageWeight,
					editedExcessWeight = editedExcessWeight,
					editedExitTime = editedExitTime,
					editedExitDate = editedExitDate,
					editedStatus = editedStatus,
					editedLoadingQuotaNumber = editedLoadingQuotaNumber
				)

				Spacer(modifier = Modifier.height(24.dp))

				// دکمه‌های عمل
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(16.dp)
				) {
					Button(
						onClick = onConfirm,
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
private fun CargoEditConfirmHeader() {
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
				imageVector = Icons.Default.Save,
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
				text = "تأیید بروزرسانی",
				style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Bold
			)
			Text(
				text = "آیا از ذخیره تغییرات اطمینان دارید؟",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
			)
		}
	}
}

@Composable
private fun CargoChangesPreview(
	cargoInfo: CargoInfo,
	editedTrackingNumber: String,
	editedNumberOfPeople: String,
	editedEntryTime: String,
	editedNetWeight: String,
	editedScaleReceiptNumber: String,
	editedShortageWeight: String,
	editedExcessWeight: String,
	editedExitTime: String,
	editedExitDate: String,
	editedStatus: String,
	editedLoadingQuotaNumber: String
) {
	val changes = mutableListOf<Triple<String, String, String>>()
	
	// بررسی تغییرات
	if (cargoInfo.trackingNumber != editedTrackingNumber) {
		changes.add(Triple("شماره حواله", cargoInfo.trackingNumber, editedTrackingNumber))
	}
	if (cargoInfo.numberOfPeople != editedNumberOfPeople) {
		changes.add(Triple("تعداد نفرات", cargoInfo.numberOfPeople, editedNumberOfPeople))
	}
	if (cargoInfo.entryTime != editedEntryTime) {
		changes.add(Triple("زمان ورود", cargoInfo.entryTime, editedEntryTime))
	}
	if (cargoInfo.netWeight != editedNetWeight) {
		changes.add(Triple("وزن خالص", cargoInfo.netWeight, editedNetWeight))
	}
	if (cargoInfo.scaleReceiptNumber != editedScaleReceiptNumber) {
		changes.add(Triple("شماره قبض باسکول", cargoInfo.scaleReceiptNumber, editedScaleReceiptNumber))
	}
	if (cargoInfo.shortageWeight != editedShortageWeight) {
		changes.add(Triple("وزن کسری", cargoInfo.shortageWeight, editedShortageWeight))
	}
	if (cargoInfo.excessWeight != editedExcessWeight) {
		changes.add(Triple("وزن اضافی", cargoInfo.excessWeight, editedExcessWeight))
	}
	if ((cargoInfo.exitTime ?: "") != editedExitTime) {
		changes.add(Triple("زمان خروج", cargoInfo.exitTime ?: "", editedExitTime))
	}
	if ((cargoInfo.exitDate ?: "") != editedExitDate) {
		changes.add(Triple("تاریخ خروج", cargoInfo.exitDate ?: "", editedExitDate))
	}
	if (cargoInfo.status != editedStatus) {
		changes.add(Triple("وضعیت", cargoInfo.status, editedStatus))
	}
	if (cargoInfo.loadingQuotaNumber != editedLoadingQuotaNumber) {
		changes.add(Triple("شماره کوتاژ", cargoInfo.loadingQuotaNumber, editedLoadingQuotaNumber))
	}

	if (changes.isNotEmpty()) {
		Card(
			modifier = Modifier.fillMaxWidth(),
			colors = CardDefaults.cardColors(
				containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
			),
			shape = RoundedCornerShape(12.dp)
		) {
			Column(
				modifier = Modifier.padding(16.dp),
				verticalArrangement = Arrangement.spacedBy(12.dp)
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					Icon(
						imageVector = Icons.Default.Edit,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.primary,
						modifier = Modifier.size(20.dp)
					)
					Text(
						text = "تغییرات اعمال شده (${changes.size} مورد)",
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.primary
					)
				}

				changes.forEach { (field, oldValue, newValue) ->
					ChangeItem(
						fieldName = field,
						oldValue = oldValue,
						newValue = newValue
					)
				}
			}
		}
	} else {
		Card(
			modifier = Modifier.fillMaxWidth(),
			colors = CardDefaults.cardColors(
				containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
			),
			shape = RoundedCornerShape(12.dp)
		) {
			Row(
				modifier = Modifier.padding(16.dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(12.dp)
			) {
				Icon(
					imageVector = Icons.Default.Info,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.size(24.dp)
				)
				Text(
					text = "هیچ تغییری اعمال نشده است",
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
	}
}

@Composable
private fun ChangeItem(
	fieldName: String,
	oldValue: String,
	newValue: String
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(
				MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
				RoundedCornerShape(8.dp)
			)
			.padding(12.dp),
		verticalArrangement = Arrangement.spacedBy(4.dp)
	) {
		Text(
			text = fieldName,
			style = MaterialTheme.typography.labelMedium,
			fontWeight = FontWeight.Bold,
			color = MaterialTheme.colorScheme.onSurface
		)
		
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			// مقدار قبلی
			Text(
				text = oldValue.ifEmpty { "خالی" },
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.error,
				modifier = Modifier
					.background(
						MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
						RoundedCornerShape(4.dp)
					)
					.padding(horizontal = 8.dp, vertical = 4.dp),
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			
			Icon(
				imageVector = Icons.AutoMirrored.Filled.ArrowForward,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
				modifier = Modifier.size(16.dp)
			)
			
			// مقدار جدید
			Text(
				text = newValue.ifEmpty { "خالی" },
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier
					.background(
						MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
						RoundedCornerShape(4.dp)
					)
					.padding(horizontal = 8.dp, vertical = 4.dp),
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
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
private fun EditableCargoMainInfo(
	trackingNumber: String,
	onTrackingNumberChange: (String) -> Unit,
	scaleReceiptNumber: String,
	onScaleReceiptNumberChange: (String) -> Unit,
	loadingQuotaNumber: String,
	onLoadingQuotaNumberChange: (String) -> Unit,
	numberOfPeople: String,
	onNumberOfPeopleChange: (String) -> Unit
) {
	InfoCard(
		mainColor = MaterialTheme.colorScheme.primary,
		title = "اطلاعات اصلی",
		icon = Icons.Default.Description,
		content = {
			Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
				OutlinedTextField(
					value = trackingNumber,
					onValueChange = onTrackingNumberChange,
					label = { Text("شماره حواله") },
					leadingIcon = {
						Icon(imageVector = Icons.Default.Numbers, contentDescription = null)
					},
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					shape = RoundedCornerShape(12.dp)
				)
				
				OutlinedTextField(
					value = scaleReceiptNumber,
					onValueChange = { 
						if (it.all { char -> char.isDigit() }) {
							onScaleReceiptNumberChange(it)
						}
					},
					label = { Text("قبض باسکول") },
					leadingIcon = {
						Icon(imageVector = Icons.Default.Receipt, contentDescription = null)
					},
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
					shape = RoundedCornerShape(12.dp)
				)
				
				OutlinedTextField(
					value = loadingQuotaNumber,
					onValueChange = onLoadingQuotaNumberChange,
					label = { Text("شماره کوتاژ") },
					leadingIcon = {
						Icon(imageVector = Icons.Default.Newspaper, contentDescription = null)
					},
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					shape = RoundedCornerShape(12.dp)
				)
				
				OutlinedTextField(
					value = numberOfPeople,
					onValueChange = onNumberOfPeopleChange,
					label = { Text("تعداد افراد") },
					leadingIcon = {
						Icon(imageVector = Icons.Default.People, contentDescription = null)
					},
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					shape = RoundedCornerShape(12.dp)
				)
			}
		}
	)
}

@Composable
private fun EditableCargoWeightInfo(
	netWeight: String,
	onNetWeightChange: (String) -> Unit,
	shortageWeight: String,
	onShortageWeightChange: (String) -> Unit,
	excessWeight: String,
	onExcessWeightChange: (String) -> Unit
) {
	InfoCard(
		mainColor = MaterialTheme.colorScheme.secondary,
		title = "اطلاعات وزن",
		icon = Icons.Default.Scale,
		content = {
			Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
				OutlinedTextField(
					value = netWeight,
					onValueChange = { 
						if (it.all { char -> char.isDigit() }) {
							onNetWeightChange(it)
						}
					},
					label = { Text("وزن خالص (کیلوگرم)") },
					leadingIcon = {
						Icon(imageVector = Icons.Default.Scale, contentDescription = null)
					},
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
					shape = RoundedCornerShape(12.dp)
				)
				
				OutlinedTextField(
					value = shortageWeight,
					onValueChange = { 
						if (it.all { char -> char.isDigit() }) {
							onShortageWeightChange(it)
						}
					},
					label = { Text("کسری بار (کیلوگرم)") },
					leadingIcon = {
						Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null)
					},
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
					shape = RoundedCornerShape(12.dp)
				)
				
				OutlinedTextField(
					value = excessWeight,
					onValueChange = { 
						if (it.all { char -> char.isDigit() }) {
							onExcessWeightChange(it)
						}
					},
					label = { Text("اضافه بار (کیلوگرم)") },
					leadingIcon = {
						Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null)
					},
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
					shape = RoundedCornerShape(12.dp)
				)
			}
		}
	)
}

@Composable
private fun EditableCargoTimeInfo(
	entryTime: String,
	onEntryTimeChange: (String) -> Unit,
	exitTime: String,
	onExitTimeChange: (String) -> Unit,
	exitDate: String,
	onExitDateChange: (String) -> Unit,
	status: String,
	onStatusChange: (String) -> Unit
) {
	InfoCard(
		mainColor = MaterialTheme.colorScheme.tertiary,
		title = "زمان‌بندی",
		icon = Icons.Default.Schedule,
		content = {
			Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
				OutlinedTextField(
					value = entryTime,
					onValueChange = onEntryTimeChange,
					label = { Text("ساعت ورود") },
					leadingIcon = {
						Icon(imageVector = Icons.AutoMirrored.Filled.Login, contentDescription = null)
					},
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					shape = RoundedCornerShape(12.dp)
				)
				
				OutlinedTextField(
					value = exitTime,
					onValueChange = onExitTimeChange,
					label = { Text("ساعت خروج") },
					leadingIcon = {
						Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
					},
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					shape = RoundedCornerShape(12.dp)
				)
				
				OutlinedTextField(
					value = exitDate,
					onValueChange = onExitDateChange,
					label = { Text("تاریخ خروج") },
					leadingIcon = {
						Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null)
					},
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					shape = RoundedCornerShape(12.dp)
				)
				
				OutlinedTextField(
					value = status,
					onValueChange = onStatusChange,
					label = { Text("وضعیت") },
					leadingIcon = {
						Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
					},
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					shape = RoundedCornerShape(12.dp)
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
					.fillMaxWidth(0.92f)
					.fillMaxHeight(0.9f),
				shape = RoundedCornerShape(16.dp),
				color = MaterialTheme.colorScheme.surface,
				tonalElevation = 6.dp
			) {
				Column(
					modifier = Modifier
						.fillMaxSize()
						.background(MaterialTheme.colorScheme.background)
				) {
					// هدر دیالوگ
					AnalyticsHeaderCard(onClose = onDismiss)

					// محتوای اصلی
					Column(
						modifier = Modifier
							.fillMaxSize()
							.padding(12.dp)
					) {
						Spacer(modifier = Modifier.height(8.dp))

						// نوار تب‌ها
						AnalyticsTabRow(
							selectedTab = selectedTab,
							onTabSelected = { selectedTab = it }
						)

						Spacer(modifier = Modifier.height(8.dp))

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
										Column(
											horizontalAlignment = Alignment.CenterHorizontally,
											verticalArrangement = Arrangement.spacedBy(16.dp)
										) {
											CircularProgressIndicator(
												color = MaterialTheme.colorScheme.primary
											)
											Text(
												text = "در حال بارگذاری تحلیل‌ها...",
												style = MaterialTheme.typography.bodyMedium,
												color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
											)
										}
									}
								}
								is ReportsViewModel.LoadingState.Error -> {
									val error = (loadingState as ReportsViewModel.LoadingState.Error).message
									ErrorStateCard(errorMessage = error)
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
															viewModel = viewModel
														)
													}
												} ?: EmptyStateCard("داده‌ای برای کوتاژها یافت نشد")
											AnalyticsTabType.CARGO_OWNERS ->
												analyticsData?.cargoOwnerAnalysis?.let { cargoOwnerData ->
													CargoOwnerAnalysis(cargoOwnerData = cargoOwnerData)
												} ?: EmptyStateCard("داده‌ای برای صاحبان کالا یافت نشد")
											AnalyticsTabType.PEAK_HOURS ->
												analyticsData?.shiftPerformanceAnalysis?.let { shiftData ->
													PeakHoursAnalysis(shiftData = shiftData)
												} ?: EmptyStateCard("داده‌ای برای 24 ساعت گذشته یافت نشد")
											AnalyticsTabType.CARRIERS ->
												analyticsData?.carrierPerformanceAnalysis?.let { carrierData ->
													CarrierAnalysis(carrierPerformanceData = carrierData)
												} ?: EmptyStateCard("داده‌ای برای باربری‌ها یافت نشد")
											AnalyticsTabType.WAREHOUSES ->
												analyticsData?.warehouseEfficiencyAnalysis?.let { warehouseData ->
													WarehouseAnalysis(efficiencyData = warehouseData)
												} ?: EmptyStateCard("داده‌ای برای انبارها یافت نشد")
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
		// فیلد جستجو - طراحی مینیمال
		SearchField(
			searchQuery = searchQuery,
			onSearchQueryChange = { searchQuery = it },
			modifier = Modifier.fillMaxWidth(),
			placeholder = "جستجوی کشتی یا صاحب کالا..."
		)

		Spacer(modifier = Modifier.height(8.dp))

		// لیست کشتی‌ها
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			verticalArrangement = Arrangement.spacedBy(8.dp),
			contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
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
			.clickable { onExpandChange(!isExpanded) }
			.animateContentSize(
				animationSpec = spring(
					dampingRatio = Spring.DampingRatioMediumBouncy,
					stiffness = Spring.StiffnessLow
				)
			),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
		),
		shape = RoundedCornerShape(12.dp),
		border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
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
					Surface(
						shape = CircleShape,
						color = animateColor.copy(alpha = 0.1f),
						modifier = Modifier.size(40.dp)
					) {
						Icon(
							imageVector = Icons.Default.DirectionsBoat,
							contentDescription = null,
							tint = animateColor,
							modifier = Modifier
								.padding(8.dp)
								.size(24.dp)
						)
					}

					// اطلاعات کشتی
					Column {
						Text(
							text = shipData.shipName,
							style = MaterialTheme.typography.titleSmall,
							fontWeight = FontWeight.Bold
						)

						Spacer(modifier = Modifier.height(4.dp))

						Row(
							horizontalArrangement = Arrangement.spacedBy(8.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							StatChip(
								icon = Icons.Default.Person,
								value = formatNumber(shipData.owner_count),
								color = animateColor
							)
							StatChip(
								icon = Icons.Default.Receipt,
								value = formatNumber(shipData.total_vouchers),
								color = MaterialTheme.colorScheme.secondary
							)
						}
					}
				}

				// وزن و آیکون گسترش
				Row(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Surface(
						shape = RoundedCornerShape(16.dp),
						color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
					) {
						Row(
							modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
							horizontalArrangement = Arrangement.spacedBy(4.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							Icon(
								imageVector = Icons.Default.Scale,
								contentDescription = null,
								tint = MaterialTheme.colorScheme.secondary,
								modifier = Modifier.size(16.dp)
							)
							Text(
								text = "${formatNumber(shipData.total_net_weight.roundToInt())} تن",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.secondary,
								fontWeight = FontWeight.Bold
							)
						}
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
					HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
					Spacer(modifier = Modifier.height(4.dp))

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
				.padding(12.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			// اطلاعات صاحب کالا
			Row(
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Surface(
					shape = CircleShape,
					color = color.copy(alpha = 0.1f),
					modifier = Modifier.size(32.dp)
				) {
					Icon(
						imageVector = Icons.Default.Person,
						contentDescription = null,
						tint = color,
						modifier = Modifier
							.padding(6.dp)
							.size(20.dp)
					)
				}

				Column {
					Text(
						text = ownerData.cargoOwner,
						style = MaterialTheme.typography.bodyMedium,
						fontWeight = FontWeight.Medium
					)

					Spacer(modifier = Modifier.height(4.dp))

					Row(
						horizontalArrangement = Arrangement.spacedBy(6.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						StatChip(
							icon = Icons.Default.Description,
							value = formatNumber(ownerData.quota_count),
							color = color
						)
						StatChip(
							icon = Icons.Default.Receipt,
							value = formatNumber(ownerData.voucher_count),
							color = MaterialTheme.colorScheme.secondary
						)
					}
				}
			}

			// اطلاعات وزن
			Surface(
				shape = RoundedCornerShape(16.dp),
				color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
			) {
				Row(
					modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
					horizontalArrangement = Arrangement.spacedBy(4.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Icon(
						imageVector = Icons.Default.Scale,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.tertiary,
						modifier = Modifier.size(14.dp)
					)
					Text(
						text = "${formatNumber(ownerData.net_weight.roundToInt())} تن",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.tertiary,
						fontWeight = FontWeight.Bold
					)
				}
			}
		}
	}
}

@Composable
private fun AnalyticsHeaderCard(
	onClose: () -> Unit
) {
	Surface(
		modifier = Modifier.fillMaxWidth(),
		color = MaterialTheme.colorScheme.primary
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 12.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			// نام دیالوگ و آیکون
			Row(
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				// آیکون تحلیل
				Surface(
					shape = CircleShape,
					color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f),
					modifier = Modifier.size(48.dp)
				) {
					Box(contentAlignment = Alignment.Center) {
						Icon(
							imageVector = Icons.Default.Analytics,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.onPrimary,
							modifier = Modifier.size(24.dp)
						)
					}
				}

				// نام دیالوگ و توضیحات
				Column {
					Text(
						text = "تحلیل جامع عملیات",
						style = MaterialTheme.typography.headlineSmall,
						color = MaterialTheme.colorScheme.onPrimary,
						fontWeight = FontWeight.Bold
					)

					Text(
						text = "گزارشات 24 ساعت گذشته",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
					)
				}
			}

			// دکمه بستن
			Surface(
				shape = CircleShape,
				color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f),
				modifier = Modifier.size(40.dp)
			) {
				IconButton(
					onClick = onClose,
					modifier = Modifier.fillMaxSize()
				) {
					Icon(
						imageVector = Icons.Default.Close,
						contentDescription = "بستن",
						tint = MaterialTheme.colorScheme.onPrimary,
						modifier = Modifier.size(20.dp)
					)
				}
			}
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
			TabInfo(AnalyticsTabType.QUOTAS, "آمار کوتاژها", Icons.Default.Description),
			TabInfo(AnalyticsTabType.CARGO_OWNERS, "صاحبان کالا", Icons.Default.Person),
			TabInfo(AnalyticsTabType.PEAK_HOURS, "24 ساعت گذشته", Icons.Default.Schedule),
			TabInfo(AnalyticsTabType.CARRIERS, "باربری‌ها", Icons.Default.LocalShipping),
			TabInfo(AnalyticsTabType.WAREHOUSES, "انبارها", Icons.Default.Warehouse)
		)
	}

	Surface(
		modifier = modifier.fillMaxWidth(),
		shape = RoundedCornerShape(8.dp),
		color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
		border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
	) {
		LazyRow(
			modifier = Modifier.padding(2.dp),
			horizontalArrangement = Arrangement.spacedBy(2.dp),
			contentPadding = PaddingValues(horizontal = 2.dp)
		) {
			items(tabs) { tab ->
				val isSelected = selectedTab == tab.type

				Surface(
					onClick = { onTabSelected(tab.type) },
					modifier = Modifier.widthIn(min = 100.dp),
					shape = RoundedCornerShape(6.dp),
					color = if (isSelected) {
						MaterialTheme.colorScheme.primary
					} else {
						Color.Transparent
					}
				) {
					Row(
						modifier = Modifier
							.padding(horizontal = 12.dp, vertical = 10.dp),
						horizontalArrangement = Arrangement.spacedBy(6.dp),
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
						Text(
							text = tab.label,
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
			}
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
			ShiftCard(
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
private fun ShiftCard(
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
private fun SearchField(
	searchQuery: String,
	onSearchQueryChange: (String) -> Unit,
	modifier: Modifier = Modifier.fillMaxWidth(),
	placeholder: String = "جستجو...",
	keyboardType: KeyboardType = KeyboardType.Text
) {
	OutlinedTextField(
		value = searchQuery,
		onValueChange = onSearchQueryChange,
		modifier = modifier,
		placeholder = {
			Text(
				text = placeholder,
				style = MaterialTheme.typography.bodyMedium
			)
		},
		leadingIcon = {
			Icon(
				imageVector = Icons.Default.Search,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			)
		},
		trailingIcon = {
			if (searchQuery.isNotEmpty()) {
				IconButton(onClick = { onSearchQueryChange("") }) {
					Icon(
						imageVector = Icons.Default.Clear,
						contentDescription = "پاک کردن",
						tint = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}
		},
		keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
		singleLine = true,
		shape = RoundedCornerShape(12.dp),
		colors = OutlinedTextFieldDefaults.colors(
			focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
			unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
		)
	)
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
	viewModel: ReportsViewModel
) {
	LaunchedEffect(completionData) {
		viewModel.updateInitialQuotas(completionData)
	}

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
			.padding(horizontal = 8.dp)
	) {
		// فیلد جستجو
		OutlinedTextField(
			value = searchQuery,
			onValueChange = { viewModel.updateSearchQuery(it) },
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 8.dp),
			placeholder = {
				Text(
					text = "جستجو ...",
					style = MaterialTheme.typography.bodyMedium
				)
			},
			leadingIcon = {
				Icon(
					imageVector = Icons.Default.Search,
					contentDescription = "جستجو",
					tint = MaterialTheme.colorScheme.onSurfaceVariant
				)
			},
			trailingIcon = {
				if (searchQuery.isNotEmpty()) {
					IconButton(
						onClick = { viewModel.updateSearchQuery("") }
					) {
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
			keyboardOptions = KeyboardOptions(
				keyboardType = KeyboardType.Number
			),
			colors = OutlinedTextFieldDefaults.colors(
				focusedBorderColor = MaterialTheme.colorScheme.primary,
				unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
			)
		)

		// انتخابگر نوع گروه‌بندی
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 8.dp, bottom = 12.dp)
				.clip(RoundedCornerShape(12.dp))
				.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
				.border(
					BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
					RoundedCornerShape(12.dp)
				),
			horizontalArrangement = Arrangement.SpaceEvenly,
			verticalAlignment = Alignment.CenterVertically
		) {
			AnalyticsGroupingModeButton(
				text = "کشتی",
				icon = Icons.Default.DirectionsBoat,
				isSelected = groupingMode == QuotaGroupingMode.BY_SHIP,
				onClick = { viewModel.setGroupingMode(QuotaGroupingMode.BY_SHIP) },
				modifier = Modifier.weight(1f)
			)
			VerticalDivider(
				modifier = Modifier.height(28.dp),
				thickness = 1.dp,
				color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
			)
			AnalyticsGroupingModeButton(
				text = "باربری",
				icon = Icons.Default.LocalShipping,
				isSelected = groupingMode == QuotaGroupingMode.BY_CARRIER,
				onClick = { viewModel.setGroupingMode(QuotaGroupingMode.BY_CARRIER) },
				modifier = Modifier.weight(1f)
			)
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
			// پیش‌پردازش گروه‌بندی خارج از LazyColumn برای بهینه‌سازی
			val grouped = when (groupingMode) {
				QuotaGroupingMode.BY_SHIP -> {
					activeQuotas.groupBy { it.shipName }
				}
				QuotaGroupingMode.BY_CARRIER -> {
					activeQuotas.groupBy { it.shippingCompany }
				}
			}

			// مرتب‌سازی گروه‌ها براساس تعداد کوتاژ و تناژ کل
			val sortedGroups = remember(activeQuotas, groupingMode) {
				grouped.map { (groupName, quotas) ->
					Triple(
						groupName,
						quotas,
						quotas.sumOf { it.last_24h_weight.toDouble() }.toFloat()
					)
				}.sortedWith(
					compareByDescending<Triple<String, List<QuotaCompletionData>, Float>> { it.second.size }
						.thenByDescending { it.third }
				)
			}

			LazyColumn(
				modifier = Modifier.fillMaxSize(),
				verticalArrangement = Arrangement.spacedBy(8.dp),
				// بهینه‌سازی اسکرول
				flingBehavior = ScrollableDefaults.flingBehavior(),
				userScrollEnabled = true
			) {
				items(
					items = sortedGroups,
					key = { (groupName, _, _) -> "group_$groupName" }
				) { (groupName, quotas, _) ->
					AnalyticsQuotaGroupExpansionPanel(
						groupName = groupName,
						quotas = quotas,
						groupingMode = groupingMode,
						isExpanded = expandedGroup == groupName,
						onExpandChange = { shouldExpand ->
							expandedGroup = if (shouldExpand) groupName else null
						}
					)
				}
			}
		}
	}
}

@Composable
private fun AnalyticsGroupingModeButton(
	text: String,
	icon: ImageVector,
	isSelected: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	val interactionSource = remember { MutableInteractionSource() }
	val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
	val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)

	Box(
		modifier = modifier
			.clickable(onClick = onClick, interactionSource = interactionSource, indication = null)
			.background(backgroundColor)
			.padding(vertical = 8.dp, horizontal = 6.dp),
		contentAlignment = Alignment.Center
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.Center,
		) {
			Icon(
				imageVector = icon,
				contentDescription = text,
				tint = contentColor,
				modifier = Modifier.size(18.dp)
			)
			Spacer(modifier = Modifier.width(6.dp))
			Text(
				text = text,
				style = MaterialTheme.typography.labelMedium,
				color = contentColor,
				fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
			)
		}
	}
}

@Composable
private fun AnalyticsQuotaGroupExpansionPanel(
	groupName: String,
	quotas: List<QuotaCompletionData>,
	groupingMode: QuotaGroupingMode,
	isExpanded: Boolean,
	onExpandChange: (Boolean) -> Unit,
	modifier: Modifier = Modifier
) {
	val totalWeight = quotas.sumOf { it.last_24h_weight.toDouble() }.toFloat()
	val totalVouchers = quotas.sumOf { it.last_24h_vouchers }

	val groupIcon = when (groupingMode) {
		QuotaGroupingMode.BY_SHIP -> Icons.Default.DirectionsBoat
		QuotaGroupingMode.BY_CARRIER -> Icons.Default.LocalShipping
	}

	Card(
		modifier = modifier
			.fillMaxWidth()
			.animateContentSize(
				animationSpec = spring(
					dampingRatio = Spring.DampingRatioMediumBouncy,
					stiffness = Spring.StiffnessLow
				)
			),
		shape = RoundedCornerShape(12.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
		),
		border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
	) {
		Column {
			Surface(
				onClick = { onExpandChange(!isExpanded) },
				color = Color.Transparent
			) {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp),
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
						imageVector = groupIcon,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.primary,
						modifier = Modifier.size(24.dp)
					)
				}

				Column {
					Text(
						text = groupName,
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.onSurface
					)

					Row(
						horizontalArrangement = Arrangement.spacedBy(8.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						AnalyticsStatChip(
							value = "${quotas.size}",
							label = "کوتاژ",
							color = MaterialTheme.colorScheme.primary
						)
						AnalyticsStatChip(
							value = formatNumber(totalWeight.roundToInt()),
							label = "تن",
							color = MaterialTheme.colorScheme.secondary
						)
						AnalyticsStatChip(
							value = formatNumber(totalVouchers),
							label = "حواله",
							color = MaterialTheme.colorScheme.tertiary
						)
					}
				}
					}

					IconButton(
						onClick = { onExpandChange(!isExpanded) }
					) {
						Icon(
							imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
							contentDescription = if (isExpanded) "بستن" else "گسترش",
							tint = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				}
			}

			AnimatedVisibility(
				visible = isExpanded,
				enter = expandVertically(
					animationSpec = spring(
						dampingRatio = Spring.DampingRatioMediumBouncy,
						stiffness = Spring.StiffnessLow
					)
				) + fadeIn(),
				exit = shrinkVertically(
					animationSpec = spring(
						dampingRatio = Spring.DampingRatioMediumBouncy,
						stiffness = Spring.StiffnessLow
					)
				) + fadeOut()
			) {
				Column(
					modifier = Modifier
						.heightIn(max = 400.dp)
						.verticalScroll(rememberScrollState())
						.padding(16.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					quotas.forEach { quota ->
						AnalyticsQuotaCard(
							quota = quota,
							groupingMode = groupingMode
						)
					}
				}
			}
		}
	}
}

@Composable
private fun AnalyticsStatChip(
	value: String,
	label: String,
	color: Color,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier,
		shape = RoundedCornerShape(12.dp),
		color = color.copy(alpha = 0.1f),
		border = BorderStroke(0.5.dp, color.copy(alpha = 0.3f))
	) {
		Row(
			modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
			horizontalArrangement = Arrangement.spacedBy(4.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = value,
				style = MaterialTheme.typography.bodySmall,
				color = color,
				fontWeight = FontWeight.Bold
			)
			Text(
				text = label,
				style = MaterialTheme.typography.bodySmall,
				color = color.copy(alpha = 0.8f)
			)
		}
	}
}

@Composable
private fun AnalyticsQuotaCard(
	quota: QuotaCompletionData,
	groupingMode: QuotaGroupingMode,
	modifier: Modifier = Modifier
) {
	val isDarkTheme = isSystemInDarkTheme()
	val completionColor = getCompletionColor(quota.completion_percentage, isDarkTheme)

	Card(
		modifier = modifier
			.fillMaxWidth()
			.wrapContentHeight(),
		shape = RoundedCornerShape(8.dp),
		colors = CardDefaults.cardColors(
			containerColor = completionColor.copy(alpha = 0.05f)
		),
		border = BorderStroke(1.dp, completionColor.copy(alpha = 0.2f))
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp)
		) {
			// هدر کارت
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				// آیکون کوتاژ
				Box(
					modifier = Modifier
						.size(32.dp)
						.background(
							color = completionColor.copy(alpha = 0.1f),
							shape = CircleShape
						),
					contentAlignment = Alignment.Center
				) {
					Icon(
						imageVector = Icons.Default.Receipt,
						contentDescription = null,
						tint = completionColor,
						modifier = Modifier.size(18.dp)
					)
				}

				// اطلاعات کوتاژ
				Column {
					Row(
						horizontalArrangement = Arrangement.spacedBy(4.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						Text(
							text = quota.loadingQuotaNumber,
							style = MaterialTheme.typography.bodyLarge,
							fontWeight = FontWeight.Bold,
							color = MaterialTheme.colorScheme.onSurface
						)
						Text(
							text = "|",
							style = MaterialTheme.typography.bodyMedium,
							fontWeight = FontWeight.Bold,
							color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
						)
						Text(
							text = when (groupingMode) {
								QuotaGroupingMode.BY_SHIP -> quota.shippingCompany
								QuotaGroupingMode.BY_CARRIER -> quota.shipName
							},
							style = MaterialTheme.typography.bodyMedium,
							fontWeight = FontWeight.Bold,
							color = MaterialTheme.colorScheme.primary
						)
					}

					Row(
						horizontalArrangement = Arrangement.spacedBy(6.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						AnalyticsStatChip(
							value = formatNumber(quota.last_24h_vouchers),
							label = "حواله",
							color = completionColor
						)
						AnalyticsStatChip(
							value = formatNumber(quota.last_24h_weight.roundToInt()),
							label = "تن",
							color = completionColor
						)
					}
				}
			}
		}
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
			CarrierCard(carrier = carrier)
		}
	}
}

@Composable
private fun CarrierCard(
	carrier: CarrierPerformanceAnalysis,
	modifier: Modifier = Modifier
) {
	val color = when {
		carrier.quality_score >= 80 -> MaterialTheme.colorScheme.primary
		carrier.quality_score >= 70 -> MaterialTheme.colorScheme.secondary
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
		Column(modifier = Modifier.padding(12.dp)) {
			// Header
			Row(
				modifier = Modifier.fillMaxWidth(),
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

			// Stats
			QuickStats(carrier, color)

			// Detailed Stats
			DetailedStats(carrier, color)
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

	return simplifiedWeight
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

@Composable
private fun ErrorStateCard(
	errorMessage: String,
	modifier: Modifier = Modifier
) {
	Card(
		modifier = modifier
			.fillMaxWidth()
			.padding(16.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.errorContainer
		),
		shape = RoundedCornerShape(12.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			Icon(
				imageVector = Icons.Default.Error,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onErrorContainer,
				modifier = Modifier.size(48.dp)
			)
			Text(
				text = "خطا در بارگذاری داده‌ها",
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onErrorContainer,
				fontWeight = FontWeight.Bold
			)
			Text(
				text = errorMessage,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onErrorContainer,
				textAlign = TextAlign.Center
			)
		}
	}
}

@Composable
private fun EmptyStateCard(
	message: String,
	modifier: Modifier = Modifier
) {
	Card(
		modifier = modifier
			.fillMaxWidth()
			.padding(16.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
		),
		shape = RoundedCornerShape(12.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Icon(
				imageVector = Icons.Default.Inbox,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.size(64.dp)
			)
			Text(
				text = "داده‌ای موجود نیست",
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				fontWeight = FontWeight.Bold
			)
			Text(
				text = message,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				textAlign = TextAlign.Center
			)
		}
	}
}

@SuppressLint("DefaultLocale")
@Composable
fun PersianDateRangePickerDialog(
	isOpen: Boolean,
	onDismiss: () -> Unit,
	onDateRangeSelected: (startDate: String, endDate: String) -> Unit
) {
	// State variables for selected dates - حفظ آخرین انتخاب
	var startDateText by rememberSaveable { mutableStateOf("1404/01/01") }
	var startTimeText by rememberSaveable { mutableStateOf("07:00") }
	var endDateText by rememberSaveable { mutableStateOf("1404/01/02") }
	var endTimeText by rememberSaveable { mutableStateOf("07:00") }

	if (isOpen) {
		// Dialog states
		var showStartDatePicker by remember { mutableStateOf(false) }
		var showStartTimePicker by remember { mutableStateOf(false) }
		var showEndDatePicker by remember { mutableStateOf(false) }
		var showEndTimePicker by remember { mutableStateOf(false) }

		Dialog(
			onDismissRequest = onDismiss,
			properties = DialogProperties(usePlatformDefaultWidth = false)
		) {
			Surface(
				modifier = Modifier
					.fillMaxWidth(0.95f)
					.wrapContentHeight(),
				shape = RoundedCornerShape(24.dp),
				color = MaterialTheme.colorScheme.surface,
				tonalElevation = 8.dp
			) {
				Column(
					modifier = Modifier
						.padding(20.dp)
						.fillMaxWidth()
				) {
					// عنوان دیالوگ
					Row(
						modifier = Modifier.fillMaxWidth(),
						verticalAlignment = Alignment.CenterVertically
					) {
						Icon(
							Icons.Default.DateRange,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.primary,
							modifier = Modifier.size(24.dp)
						)
						Spacer(modifier = Modifier.width(12.dp))
						Text(
							text = "انتخاب بازه زمانی",
							style = MaterialTheme.typography.titleLarge,
							fontWeight = FontWeight.Bold,
							color = MaterialTheme.colorScheme.onSurface
						)
					}

					Spacer(modifier = Modifier.height(20.dp))

					// کارت تاریخ شروع
					Card(
						modifier = Modifier.fillMaxWidth(),
						colors = CardDefaults.cardColors(
							containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
						),
						shape = RoundedCornerShape(16.dp)
					) {
						Column(
							modifier = Modifier.padding(16.dp)
						) {
							Text(
								text = "از تاریخ",
								style = MaterialTheme.typography.labelLarge,
								fontWeight = FontWeight.SemiBold,
								color = MaterialTheme.colorScheme.primary
							)

							Spacer(modifier = Modifier.height(8.dp))

							Row(
								modifier = Modifier.fillMaxWidth(),
								horizontalArrangement = Arrangement.spacedBy(8.dp)
							) {
								// فیلد تاریخ
								OutlinedTextField(
									value = startDateText,
									onValueChange = { },
									label = { Text("تاریخ", style = MaterialTheme.typography.labelSmall) },
									enabled = false,
									trailingIcon = {
										Icon(
											Icons.Default.CalendarToday,
											contentDescription = "انتخاب تاریخ",
											modifier = Modifier.size(20.dp)
										)
									},
									modifier = Modifier
										.weight(0.6f)
										.clickable(
											indication = null,
											interactionSource = remember { MutableInteractionSource() }
										) { showStartDatePicker = true },
									colors = OutlinedTextFieldDefaults.colors(
										disabledTextColor = MaterialTheme.colorScheme.onSurface,
										disabledBorderColor = MaterialTheme.colorScheme.primary,
										disabledLabelColor = MaterialTheme.colorScheme.primary,
										disabledTrailingIconColor = MaterialTheme.colorScheme.primary
									)
								)

								// فیلد زمان
								OutlinedTextField(
									value = startTimeText,
									onValueChange = { },
									label = { Text("زمان", style = MaterialTheme.typography.labelSmall) },
									enabled = false,
									trailingIcon = {
										Icon(
											Icons.Default.AccessTime,
											contentDescription = "انتخاب زمان",
											modifier = Modifier.size(20.dp)
										)
									},
									modifier = Modifier
										.weight(0.4f)
										.clickable(
											indication = null,
											interactionSource = remember { MutableInteractionSource() }
										) { showStartTimePicker = true },
									colors = OutlinedTextFieldDefaults.colors(
										disabledTextColor = MaterialTheme.colorScheme.onSurface,
										disabledBorderColor = MaterialTheme.colorScheme.primary,
										disabledLabelColor = MaterialTheme.colorScheme.primary,
										disabledTrailingIconColor = MaterialTheme.colorScheme.primary
									)
								)
							}
						}
					}

					Spacer(modifier = Modifier.height(16.dp))

					// کارت تاریخ پایان
					Card(
						modifier = Modifier.fillMaxWidth(),
						colors = CardDefaults.cardColors(
							containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
						),
						shape = RoundedCornerShape(16.dp)
					) {
						Column(
							modifier = Modifier.padding(16.dp)
						) {
							Text(
								text = "تا تاریخ",
								style = MaterialTheme.typography.labelLarge,
								fontWeight = FontWeight.SemiBold,
								color = MaterialTheme.colorScheme.secondary
							)

							Spacer(modifier = Modifier.height(8.dp))

							Row(
								modifier = Modifier.fillMaxWidth(),
								horizontalArrangement = Arrangement.spacedBy(8.dp)
							) {
								// فیلد تاریخ
								OutlinedTextField(
									value = endDateText,
									onValueChange = { },
									label = { Text("تاریخ", style = MaterialTheme.typography.labelSmall) },
									enabled = false,
									trailingIcon = {
										Icon(
											Icons.Default.CalendarToday,
											contentDescription = "انتخاب تاریخ",
											modifier = Modifier.size(20.dp)
										)
									},
									modifier = Modifier
										.weight(0.6f)
										.clickable(
											indication = null,
											interactionSource = remember { MutableInteractionSource() }
										) { showEndDatePicker = true },
									colors = OutlinedTextFieldDefaults.colors(
										disabledTextColor = MaterialTheme.colorScheme.onSurface,
										disabledBorderColor = MaterialTheme.colorScheme.secondary,
										disabledLabelColor = MaterialTheme.colorScheme.secondary,
										disabledTrailingIconColor = MaterialTheme.colorScheme.secondary
									)
								)

								// فیلد زمان
								OutlinedTextField(
									value = endTimeText,
									onValueChange = { },
									label = { Text("زمان", style = MaterialTheme.typography.labelSmall) },
									enabled = false,
									trailingIcon = {
										Icon(
											Icons.Default.AccessTime,
											contentDescription = "انتخاب زمان",
											modifier = Modifier.size(20.dp)
										)
									},
									modifier = Modifier
										.weight(0.4f)
										.clickable(
											indication = null,
											interactionSource = remember { MutableInteractionSource() }
										) { showEndTimePicker = true },
									colors = OutlinedTextFieldDefaults.colors(
										disabledTextColor = MaterialTheme.colorScheme.onSurface,
										disabledBorderColor = MaterialTheme.colorScheme.secondary,
										disabledLabelColor = MaterialTheme.colorScheme.secondary,
										disabledTrailingIconColor = MaterialTheme.colorScheme.secondary
									)
								)
							}
						}
					}

					Spacer(modifier = Modifier.height(20.dp))

					// دکمه‌های عمل
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start)
					) {
						Button(
							onClick = {
								val startDateTime = "$startDateText $startTimeText"
								val endDateTime = "$endDateText $endTimeText"
								onDateRangeSelected(startDateTime, endDateTime)
							},
							shape = RoundedCornerShape(12.dp),
							modifier = Modifier.height(48.dp)
						) {
							Icon(
								Icons.Default.Check,
								contentDescription = null,
								modifier = Modifier.size(18.dp)
							)
							Spacer(modifier = Modifier.width(8.dp))
							Text("تایید")
						}

						OutlinedButton(
							onClick = onDismiss,
							shape = RoundedCornerShape(12.dp),
							modifier = Modifier.height(48.dp)
						) {
							Icon(
								Icons.Default.Close,
								contentDescription = null,
								modifier = Modifier.size(18.dp)
							)
							Spacer(modifier = Modifier.width(8.dp))
							Text("لغو")
						}
					}
				}
			}
		}

		// دیالوگ انتخاب تاریخ شروع
		if (showStartDatePicker) {
			DatePickerDialog(
				isOpen = true,
				title = "انتخاب تاریخ شروع",
				initialDate = startDateText,
				containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
				titleColor = MaterialTheme.colorScheme.primary,
				onDismiss = { showStartDatePicker = false },
				onDateSelected = { date ->
					startDateText = date
					// انتخاب خودکار یک روز بعد برای تاریخ پایان
					val dateParts = date.split("/")
					val year = dateParts.getOrNull(0)?.toIntOrNull() ?: 1404
					val month = dateParts.getOrNull(1)?.toIntOrNull() ?: 1
					val day = dateParts.getOrNull(2)?.toIntOrNull() ?: 1

					// محاسبه یک روز بعد
					val nextDay = if (day < 31) day + 1 else 1
					val nextMonth = if (day == 31 && month < 12) month + 1 else if (day == 31 && month == 12) 1 else month
					val nextYear = if (day == 31 && month == 12) year + 1 else year

					endDateText = "%04d/%02d/%02d".format(nextYear, nextMonth, nextDay)
					showStartDatePicker = false
				}
			)
		}

		// دیالوگ انتخاب زمان شروع
		if (showStartTimePicker) {
			TimePickerDialog(
				isOpen = true,
				title = "انتخاب زمان شروع",
				initialTime = startTimeText,
				containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
				titleColor = MaterialTheme.colorScheme.primary,
				onDismiss = { showStartTimePicker = false },
				onTimeSelected = { time ->
					startTimeText = time
					showStartTimePicker = false
				}
			)
		}

		// دیالوگ انتخاب تاریخ پایان
		if (showEndDatePicker) {
			DatePickerDialog(
				isOpen = true,
				title = "انتخاب تاریخ پایان",
				initialDate = endDateText,
				containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
				titleColor = MaterialTheme.colorScheme.secondary,
				minDate = startDateText,
				onDismiss = { showEndDatePicker = false },
				onDateSelected = { date ->
					endDateText = date
					showEndDatePicker = false
				}
			)
		}

		// دیالوگ انتخاب زمان پایان
		if (showEndTimePicker) {
			TimePickerDialog(
				isOpen = true,
				title = "انتخاب زمان پایان",
				initialTime = endTimeText,
				containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
				titleColor = MaterialTheme.colorScheme.secondary,
				onDismiss = { showEndTimePicker = false },
				onTimeSelected = { time ->
					endTimeText = time
					showEndTimePicker = false
				}
			)
		}
	}
}

@SuppressLint("DefaultLocale")
@Composable
fun DatePickerDialog(
	isOpen: Boolean,
	title: String,
	initialDate: String,
	containerColor: Color,
	titleColor: Color,
	minDate: String? = null,
	onDismiss: () -> Unit,
	onDateSelected: (date: String) -> Unit
) {
	if (isOpen) {
		// Parse initial date
		val dateParts = initialDate.split("/")

		// State variables
		var selectedYear by remember { mutableIntStateOf(dateParts.getOrNull(0)?.toIntOrNull() ?: 1404) }
		var selectedMonth by remember { mutableIntStateOf(dateParts.getOrNull(1)?.toIntOrNull() ?: 1) }
		var selectedDay by remember { mutableIntStateOf(dateParts.getOrNull(2)?.toIntOrNull() ?: 1) }

		// Lists for scrollable selectors
		val years = (1403..1410).toList()
		val months = (1..12).toList()
		val days = (1..31).toList()

		Dialog(
			onDismissRequest = onDismiss,
			properties = DialogProperties(usePlatformDefaultWidth = false)
		) {
			Surface(
				modifier = Modifier
					.fillMaxWidth(0.9f)
					.wrapContentHeight(),
				shape = RoundedCornerShape(24.dp),
				color = MaterialTheme.colorScheme.surface,
				tonalElevation = 8.dp
			) {
				Column(
					modifier = Modifier
						.padding(20.dp)
						.fillMaxWidth()
				) {
					// عنوان دیالوگ
					Row(
						modifier = Modifier.fillMaxWidth(),
						verticalAlignment = Alignment.CenterVertically
					) {
						Icon(
							Icons.Default.CalendarToday,
							contentDescription = null,
							tint = titleColor,
							modifier = Modifier.size(24.dp)
						)
						Spacer(modifier = Modifier.width(12.dp))
						Text(
							text = title,
							style = MaterialTheme.typography.titleLarge,
							fontWeight = FontWeight.Bold,
							color = titleColor
						)
					}

					Spacer(modifier = Modifier.height(20.dp))

					// کارت انتخاب تاریخ
					Card(
						modifier = Modifier.fillMaxWidth(),
						colors = CardDefaults.cardColors(
							containerColor = containerColor
						),
						shape = RoundedCornerShape(16.dp)
					) {
						Column(
							modifier = Modifier.padding(16.dp)
						) {
							Row(
								modifier = Modifier.fillMaxWidth(),
								horizontalArrangement = Arrangement.spacedBy(8.dp)
							) {
								// روز
								ScrollableSelector(
									label = "روز",
									items = days,
									selectedItem = selectedDay,
									onItemSelected = { selectedDay = it },
									modifier = Modifier.weight(1f),
									formatItem = { "%02d".format(it) }
								)

								// ماه
								ScrollableSelector(
									label = "ماه",
									items = months,
									selectedItem = selectedMonth,
									onItemSelected = { selectedMonth = it },
									modifier = Modifier.weight(1f),
									formatItem = { "%02d".format(it) }
								)

								// سال
								ScrollableSelector(
									label = "سال",
									items = years,
									selectedItem = selectedYear,
									onItemSelected = { selectedYear = it },
									modifier = Modifier.weight(1f)
								)
							}
						}
					}

					Spacer(modifier = Modifier.height(20.dp))

					// بررسی validation
					val currentDate = "%04d/%02d/%02d".format(selectedYear, selectedMonth, selectedDay)

					val isValidDate = minDate?.let { minD ->
						val minDateParts = minD.split("/")
						val minYear = minDateParts.getOrNull(0)?.toIntOrNull() ?: 1404
						val minMonth = minDateParts.getOrNull(1)?.toIntOrNull() ?: 1
						val minDay = minDateParts.getOrNull(2)?.toIntOrNull() ?: 1

						// محاسبه کل روزها برای مقایسه
						val currentTotalDays = (selectedYear * 365) + (selectedMonth * 30) + selectedDay
						val minTotalDays = (minYear * 365) + (minMonth * 30) + minDay

						currentTotalDays >= minTotalDays
					} != false

					// نمایش پیام خطا در صورت نامعتبر بودن
					if (!isValidDate) {
						Card(
							modifier = Modifier.fillMaxWidth(),
							colors = CardDefaults.cardColors(
								containerColor = MaterialTheme.colorScheme.errorContainer
							),
							shape = RoundedCornerShape(8.dp)
						) {
							Row(
								modifier = Modifier.padding(12.dp),
								verticalAlignment = Alignment.CenterVertically
							) {
								Icon(
									Icons.Default.Warning,
									contentDescription = null,
									tint = MaterialTheme.colorScheme.error,
									modifier = Modifier.size(20.dp)
								)
								Spacer(modifier = Modifier.width(8.dp))
								Text(
									text = "تاریخ پایان باید بیشتر یا مساوی تاریخ شروع باشد",
									style = MaterialTheme.typography.bodySmall,
									color = MaterialTheme.colorScheme.error
								)
							}
						}
						Spacer(modifier = Modifier.height(16.dp))
					}

					// دکمه‌های عمل
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start)
					) {
						Button(
							onClick = {
								onDateSelected(currentDate)
							},
							enabled = isValidDate,
							shape = RoundedCornerShape(12.dp),
							modifier = Modifier.height(48.dp)
						) {
							Icon(
								Icons.Default.Check,
								contentDescription = null,
								modifier = Modifier.size(18.dp)
							)
							Spacer(modifier = Modifier.width(8.dp))
							Text("تایید")
						}

						OutlinedButton(
							onClick = onDismiss,
							shape = RoundedCornerShape(12.dp),
							modifier = Modifier.height(48.dp)
						) {
							Icon(
								Icons.Default.Close,
								contentDescription = null,
								modifier = Modifier.size(18.dp)
							)
							Spacer(modifier = Modifier.width(8.dp))
							Text("لغو")
						}
					}
				}
			}
		}
	}
}

@Composable
fun ScrollableSelector(
	label: String,
	items: List<Int>,
	selectedItem: Int,
	onItemSelected: (Int) -> Unit,
	modifier: Modifier = Modifier,
	formatItem: (Int) -> String = { it.toString() }
) {
	Column(
		modifier = modifier,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(
			text = label,
			style = MaterialTheme.typography.labelMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			fontWeight = FontWeight.Medium
		)

		Spacer(modifier = Modifier.height(8.dp))

		Card(
			modifier = Modifier
				.fillMaxWidth()
				.height(140.dp),
			colors = CardDefaults.cardColors(
				containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
			),
			shape = RoundedCornerShape(12.dp)
		) {
			Box(
				modifier = Modifier.fillMaxSize()
			) {
				val selectedIndex = items.indexOf(selectedItem)
				val itemHeight = 40.dp // ارتفاع هر آیتم
				val visibleItemsCount = 3 // تعداد آیتم‌های قابل مشاهده
				val centerOffset = (visibleItemsCount / 2) // آفست برای قرار دادن در وسط

				val listState = rememberLazyListState(
					// قرار دادن مقدار انتخابی در وسط اسکرول
					initialFirstVisibleItemIndex = maxOf(0, selectedIndex - centerOffset)
				)

				// اسکرول به مقدار انتخابی هنگام تغییر
				LaunchedEffect(selectedItem) {
					val newIndex = items.indexOf(selectedItem)
					if (newIndex >= 0) {
						listState.animateScrollToItem(maxOf(0, newIndex - centerOffset))
					}
				}

				LazyColumn(
					state = listState,
					modifier = Modifier.fillMaxSize(),
					contentPadding = PaddingValues(vertical = 50.dp),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					items(items) { item ->
						val isSelected = item == selectedItem
						Box(
							modifier = Modifier
								.fillMaxWidth()
								.height(itemHeight)
								.clickable { onItemSelected(item) },
							contentAlignment = Alignment.Center
						) {
							Text(
								text = formatItem(item),
								style = if (isSelected) {
									MaterialTheme.typography.titleLarge.copy(
										fontWeight = FontWeight.Bold
									)
								} else {
									MaterialTheme.typography.bodyLarge
								},
								color = if (isSelected) {
									MaterialTheme.colorScheme.primary
								} else {
									MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
								}
							)
						}
					}
				}

				// خط‌های راهنما برای نشان دادن ناحیه انتخاب
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(itemHeight)
						.align(Alignment.Center)
				) {
					// خط بالا
					HorizontalDivider(
						modifier = Modifier
							.fillMaxWidth(0.8f)
							.align(Alignment.TopCenter),
						color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
						thickness = 1.dp
					)
					// خط پایین
					HorizontalDivider(
						modifier = Modifier
							.fillMaxWidth(0.8f)
							.align(Alignment.BottomCenter),
						color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
						thickness = 1.dp
					)
				}
			}
		}
	}
}

@SuppressLint("DefaultLocale")
@Composable
fun TimePickerDialog(
	isOpen: Boolean,
	title: String,
	initialTime: String,
	containerColor: Color,
	titleColor: Color,
	onDismiss: () -> Unit,
	onTimeSelected: (time: String) -> Unit
) {
	if (isOpen) {
		// Parse initial time
		val timeParts = initialTime.split(":")

		// State variables
		var selectedHour by remember { mutableIntStateOf(timeParts.getOrNull(0)?.toIntOrNull() ?: 8) }
		var selectedMinute by remember { mutableIntStateOf(timeParts.getOrNull(1)?.toIntOrNull() ?: 0) }

		// Lists for scrollable selectors
		val hours = (0..23).toList()
		val minutes = (0..59).toList()

		Dialog(
			onDismissRequest = onDismiss,
			properties = DialogProperties(usePlatformDefaultWidth = false)
		) {
			Surface(
				modifier = Modifier
					.fillMaxWidth(0.85f)
					.wrapContentHeight(),
				shape = RoundedCornerShape(24.dp),
				color = MaterialTheme.colorScheme.surface,
				tonalElevation = 8.dp
			) {
				Column(
					modifier = Modifier
						.padding(20.dp)
						.fillMaxWidth()
				) {
					// عنوان دیالوگ
					Row(
						modifier = Modifier.fillMaxWidth(),
						verticalAlignment = Alignment.CenterVertically
					) {
						Icon(
							Icons.Default.AccessTime,
							contentDescription = null,
							tint = titleColor,
							modifier = Modifier.size(24.dp)
						)
						Spacer(modifier = Modifier.width(12.dp))
						Text(
							text = title,
							style = MaterialTheme.typography.titleLarge,
							fontWeight = FontWeight.Bold,
							color = titleColor
						)
					}

					Spacer(modifier = Modifier.height(20.dp))

					// کارت انتخاب زمان
					Card(
						modifier = Modifier.fillMaxWidth(),
						colors = CardDefaults.cardColors(
							containerColor = containerColor
						),
						shape = RoundedCornerShape(16.dp)
					) {
						Column(
							modifier = Modifier.padding(16.dp)
						) {
							Row(
								modifier = Modifier.fillMaxWidth(),
								horizontalArrangement = Arrangement.spacedBy(16.dp)
							) {
								// دقیقه
								ScrollableSelector(
									label = "دقیقه",
									items = minutes,
									selectedItem = selectedMinute,
									onItemSelected = { selectedMinute = it },
									modifier = Modifier.weight(1f),
									formatItem = { "%02d".format(it) }
								)

								// ساعت
								ScrollableSelector(
									label = "ساعت",
									items = hours,
									selectedItem = selectedHour,
									onItemSelected = { selectedHour = it },
									modifier = Modifier.weight(1f),
									formatItem = { "%02d".format(it) }
								)
							}
						}
					}

					Spacer(modifier = Modifier.height(20.dp))

					// دکمه‌های عمل
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start)
					) {
						Button(
							onClick = {
								val currentTime = "%02d:%02d".format(selectedHour, selectedMinute)
								onTimeSelected(currentTime)
							},
							shape = RoundedCornerShape(12.dp),
							modifier = Modifier.height(48.dp)
						) {
							Icon(
								Icons.Default.Check,
								contentDescription = null,
								modifier = Modifier.size(18.dp)
							)
							Spacer(modifier = Modifier.width(8.dp))
							Text("تایید")
						}

						OutlinedButton(
							onClick = onDismiss,
							shape = RoundedCornerShape(12.dp),
							modifier = Modifier.height(48.dp)
						) {
							Icon(
								Icons.Default.Close,
								contentDescription = null,
								modifier = Modifier.size(18.dp)
							)
							Spacer(modifier = Modifier.width(8.dp))
							Text("لغو")
						}
					}
				}
			}
		}
	}
}