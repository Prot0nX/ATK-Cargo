# Graph Report - ATK-Cargo  (2026-08-13)

## Corpus Check
- 278 files · ~361,169 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2273 nodes · 4795 edges · 225 communities (140 shown, 85 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 130 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `271c7c64`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- InitialInfoScreen.kt
- ShipInfoSection.kt
- ReportsCommonWidgets.kt
- UserPreferencesManager
- AppApiController
- UsersManager
- MainActivity.kt
- QuotaWarningDialog.kt
- ManageReportsScreen.kt
- ClassLoader
- ReportsViewModel
- SecurityVerifier
- ApiService
- AuthController
- CargoViewModel
- Exception
- QuotaManagementDialog.kt
- Database
- CameraSection.kt
- CargoRepository
- QuotasListScreen.kt
- AppModule.kt
- UpdateManager
- SelectInfoScreen.kt
- AnalyticsController
- Logger
- ChatViewModel
- mysqli
- .getActiveDeviceId
- UserManagementScreen.kt
- MessageType
- UserManagementDialogsSection.kt
- QuotaPercentageDialogSection.kt
- CargoCounterScreen.kt
- ChatRepository
- StartupViewModel
- ReportModels.kt
- ATKCargoTheme
- CargoInfo
- secrets.cpp
- AuthModels.kt
- HomeScreen.kt
- ChatToolbar.kt
- ChatMessageEntity
- CargoViewModel.kt
- SecurityScreen.kt
- ChatScreen.kt
- ActiveShipInfo
- LoginScreen.kt
- MessageBubble.kt
- QuotaAnalysisSection.kt
- WarehouseDetailsScreen.kt
- composer.json
- SessionManager
- SessionManager
- AnimationManager
- QuotaDetailsScreen.kt
- CargoDetailsDialogSection.kt
- CargoInfoDetailsDialogSection.kt
- AppNotificationManager
- ShipDetailsScreen.kt
- ChatController
- SessionService
- RetrofitClient.kt
- formatNumber
- Request
- PasswordGateService
- ApiService.kt
- RegisterCargoDialogs.kt
- ProtectedProxy
- hashPassword
- CargoModels.kt
- QuotaSelectionDialog.kt
- FontWeight
- ShipCard.kt
- CryptoManager
- DatabaseSchemaExporter
- FilterState
- SearchDialogs.kt
- .sendChatMessage
- ShipSortingMode
- CargoCounterNavigation.kt
- ChatNotificationWorker.kt
- PermissionPoller
- MainScreen.kt
- ApiResponse
- QuotaEntryDialog.kt
- proxy_generator.php
- DataModel.kt
- NotificationActionReceiver.kt
- DateRangePicker.kt
- NavRoutes
- StartupState
- AppDatabase
- ChatInputBar.kt
- QuotasListDialogs.kt
- ColorScheme.kt
- ColorSelector
- LoadingState
- JalaliDateUtils
- Response
- StatisticsCard.kt
- GroupSortingMode
- WarehouseQuotaGroupingMode
- gradlew
- .clearMutedShips
- CargoEntryNavigation.kt
- ExitStatusDialog.kt
- config.php
- ComposerAutoloaderInit5f285832cbbfb0bffdb861048ac414db
- ExampleInstrumentedTest
- Constants
- ViewMode
- HardwarePerformanceEvaluator
- ComponentDefaults.kt
- ExampleUnitTest
- BaselineProfileGenerator
- Dp
- User
- App\Exceptions\ApiException
- App\Exceptions\ConflictException
- App\Services\PasswordGateService
- Composable
- CargoInfo
- InitialInfo
- Dp
- CargoInfo
- InitialInfo
- .deleteCargo
- MatchingQuota
- MessageType
- Logger
- Logger
- SelectInfoSnackbar.kt
- QuotaValidationResult
- ReportsViewModel
- ComprehensiveAnalyticsDialog.kt
- ChatNavigation.kt
- HomeNotificationSettingRow.kt
- CargoRegistrationNavigation.kt
- T
- ViewModelProvider
- .saveOrUpdateCargoInfo
- Intent
- EmptyState.kt
- QuotaSortingMode
- Font.kt
- .checkExistence
- .checkLogin
- .checkSession
- .forceLogoutUser
- .getAllUsers
- .logout
- .syncPermissions
- .checkScaleReceiptNumber
- .getCargoInfoByReceiptNumber
- .getCargoInfoByTrackingNumber
- .getChatMessages
- .getLoadableTonnage
- .getUnreadChatCount
- ActiveShipInfo
- Quota
- QuotaItem
- Response
- Ship
- Warehouse
- Exception
- Result
- ReportsViewModel
- KeyboardType
- ReportsViewModel
- NavController
- AndroidViewModel
- Job
- StateFlow
- ComprehensiveAnalysisResponse
- FilteredSummaryResponse
- QuotaCompletionData
- QuotaGroupingMode
- QuotaStatusResponse
- RealTimeDataResponse
- ShipsData
- ThirdPartyOrderRequest
- ThirdPartyOrderResponse

## God Nodes (most connected - your core abstractions)
1. `ReportsViewModel` - 80 edges
2. `CargoViewModel` - 80 edges
3. `ApiService` - 59 edges
4. `CargoInfo` - 40 edges
5. `AppApiController` - 40 edges
6. `formatNumber()` - 39 edges
7. `UpdateManager` - 38 edges
8. `UserPreferencesManager` - 33 edges
9. `Logger` - 32 edges
10. `StartupViewModel` - 30 edges

## Surprising Connections (you probably didn't know these)
- `RegisterCargoScreen()` --calls--> `DuplicateConfirmationDialog()`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_registration/presentation/RegisterCargoScreen.kt → app/src/main/java/com/atk/atk_cargo/feature/cargo_registration/presentation/components/RegisterCargoDialogs.kt
- `ManageReportsScreen()` --calls--> `QuotaDetails`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/ui/screens/ManageReportsScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt
- `CargoCounterScreen()` --calls--> `ColorSelector`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_counter/presentation/CargoCounterScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt
- `SelectInfoScreenContent()` --calls--> `ColorSelector`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/SelectInfoScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt
- `QuotasDialog()` --calls--> `ColorSelector`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/reports/presentation/quota_details/QuotaDetailsScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt

## Import Cycles
- None detected.

## Communities (225 total, 85 thin omitted)

### Community 0 - "InitialInfoScreen.kt"
Cohesion: 0.22
Nodes (22): CheckExistenceRequest, formatNumber(), isValidPersianText(), isValidQuotaNumber(), isValidShipName(), isValidWarehouseName(), isValidWeight(), ConfirmationDialog() (+14 more)

### Community 1 - "ShipInfoSection.kt"
Cohesion: 0.18
Nodes (20): ShipInfo, CargoOperationScreen(), NavController, formatNumber(), toEnglishNumbers(), CargoInfoRow(), DetailedInfoGrid(), DetailInfoItem() (+12 more)

### Community 2 - "ReportsCommonWidgets.kt"
Cohesion: 0.27
Nodes (15): CompactStatChip(), EmptyQuotaState(), EmptyShipsState(), EmptyStateCard(), ErrorStateCard(), FloatingActionButton(), InfoCard(), InfoChip() (+7 more)

### Community 3 - "UserPreferencesManager"
Cohesion: 0.06
Nodes (25): BootReceiver, BroadcastReceiver, Context, Intent, KoinComponent, UserPreferencesManager, Context, Intent (+17 more)

### Community 4 - "AppApiController"
Cohesion: 0.08
Nodes (3): AppApiController, CargoController, MicroCache

### Community 5 - "UsersManager"
Cohesion: 0.07
Nodes (7): ApiManager, CONFIG, ModalManager, ThemeManager, UIManager, UsersManager, Utils

### Community 6 - "MainActivity.kt"
Cohesion: 0.21
Nodes (9): SplashScreen(), SplashScreenConstants, SplashScreenContent(), SplashSubtitleBadge(), SplashVersionBadge(), Intent, MainActivity, Bundle (+1 more)

### Community 7 - "QuotaWarningDialog.kt"
Cohesion: 0.25
Nodes (17): CompactInfoSection(), DialogHeader(), Color, ImageVector, Modifier, ReportsViewModel, WarningStatus, LoadingActionButton() (+9 more)

### Community 8 - "ManageReportsScreen.kt"
Cohesion: 0.05
Nodes (68): Result, UserPreferencesManager, validateServerSession(), Modifier, StatusSnackbar(), ShiftInfo, CargoDetailsScreen(), confirmCargo() (+60 more)

### Community 9 - "ClassLoader"
Cohesion: 0.05
Nodes (4): Composer\Semver\VersionParser, ComposerStaticInit5f285832cbbfb0bffdb861048ac414db, ClassLoader, InstalledVersions

### Community 11 - "SecurityVerifier"
Cohesion: 0.09
Nodes (11): Secrets, SecurityErrorType, LICENSE_INACTIVE, LICENSE_NOT_FOUND, NETWORK_ERROR, TAMPERED, UNKNOWN_ERROR, SecurityVerifier (+3 more)

### Community 12 - "ApiService"
Cohesion: 0.14
Nodes (7): ApiService, CreateUserRequest, InitialInfo, PasswordCheckResponse, QuotaExistenceMultipleResponse, Response, SuccessResponse

### Community 13 - "AuthController"
Cohesion: 0.07
Nodes (6): App\Core\Response, AuthController, UserController, PermissionService, UserService, InputValidator

### Community 15 - "Exception"
Cohesion: 0.08
Nodes (11): ErrorResponse, HttpStatusException, CargoInfo, CargoInfoResponse, SaveOrUpdateResponse, ReportsRepository, Exception, ApiException (+3 more)

### Community 16 - "QuotaManagementDialog.kt"
Cohesion: 0.16
Nodes (31): AdvancedFiltersDialog(), AnalyticsStatChipMini(), CompactStatChip(), EmptyQuotaState(), FilterOptionsCard(), InfoChip(), IntegratedQuotaCard(), Color (+23 more)

### Community 17 - "Database"
Cohesion: 0.06
Nodes (9): PDO, PDOException, Config, self, Database, PDO, self, PDO (+1 more)

### Community 18 - "CameraSection.kt"
Cohesion: 0.14
Nodes (21): adaptiveThresholding(), applyContrastEnhancement(), enhanceEdges(), ImageProxy, InputImage, medianFilter(), preprocessImage(), EnhancedNumberAnalyzer (+13 more)

### Community 20 - "QuotasListScreen.kt"
Cohesion: 0.19
Nodes (27): Quota, QuotaEditData, QuotaPercentageData, shareQuotasData(), DialogHeader(), QuotaPercentageDialog(), ActionButton(), calculateValues() (+19 more)

### Community 21 - "AppModule.kt"
Cohesion: 0.07
Nodes (26): AtkCargoApplication, LogoutRequest, AuthRepository, ConflictSession, Error, Flow, LoginResult, Success (+18 more)

### Community 22 - "UpdateManager"
Cohesion: 0.07
Nodes (30): Completed, Downloading, DownloadProgress, DownloadState, Error, Idle, Context, Job (+22 more)

### Community 23 - "SelectInfoScreen.kt"
Cohesion: 0.21
Nodes (20): formatNumber(), RealTimeLoadingData, AnimatedHeader(), extractLastDigits(), FilterChip(), GroupedShipList(), Color, Modifier (+12 more)

### Community 24 - "AnalyticsController"
Cohesion: 0.12
Nodes (16): App\Core\Logger, App\Core\MicroCache, App\Repositories\UserRepository, App\Services\PermissionService, App\Services\SessionService, Logger, gregorian_to_jalali(), jalali_to_gregorian() (+8 more)

### Community 25 - "Logger"
Cohesion: 0.14
Nodes (4): LicenseController, OnlineUsersController, Logger, self

### Community 26 - "ChatViewModel"
Cohesion: 0.10
Nodes (7): ChatViewModel, ChatViewModelFactory, com, StateFlow, T, ViewModel, ViewModelProvider

### Community 27 - "mysqli"
Cohesion: 0.12
Nodes (8): App\Core\AuthenticatesRequests, App\Core\Database, App\Core\Request, InvalidArgumentException, mysqli, mysqli_stmt, UtilityController, DatabaseManager

### Community 29 - "UserManagementScreen.kt"
Cohesion: 0.23
Nodes (15): DeleteUserRequest, ForceLogoutRequest, DashboardSummaryRow(), EmptyStateView(), EnterpriseUserCard(), getUserTypeDisplay(), androidx, Color (+7 more)

### Community 30 - "MessageType"
Cohesion: 0.29
Nodes (6): MessageType, ERROR, SUCCESS, WARNING, CargoSnackbarQueue, StateFlow

### Community 31 - "UserManagementDialogsSection.kt"
Cohesion: 0.36
Nodes (11): CreateUserRequest, UserTypeInfo, DesignTextField(), EnhancedAddUserDialog(), EnhancedEditUserDialog(), getUserTypes(), androidx, KeyboardOptions (+3 more)

### Community 32 - "QuotaPercentageDialogSection.kt"
Cohesion: 0.20
Nodes (17): CalculationResult, AnimatedNumber(), Color, ImageVector, Modifier, lerp(), PercentageDisplay(), PercentageInputTab() (+9 more)

### Community 33 - "CargoCounterScreen.kt"
Cohesion: 0.17
Nodes (19): CargoCounterScreen(), CargoCounterViewModel, filterShipsByTab(), com, NavController, SnackbarHostState, StateFlow, ViewModel (+11 more)

### Community 34 - "ChatRepository"
Cohesion: 0.12
Nodes (11): ChatMessage, ChatMessagesResponse, SendMessageRequest, SendMessageResponse, UnreadCountResponse, DeleteMessageRequest, EditMessageRequest, ChatRepository (+3 more)

### Community 35 - "StartupViewModel"
Cohesion: 0.13
Nodes (9): AndroidViewModel, StateFlow, RequestBatteryOptimization, SecurityCheckState, ShowMessage, StartupEvent, StartupViewModel, SessionCheckRequest (+1 more)

### Community 36 - "ReportModels.kt"
Cohesion: 0.09
Nodes (24): AndroidViewModel, AnalyticsData, ComprehensiveAnalysisResponse, DateInfo, ExistingQuota, ExitDateInfo, FilteredSummary, FilteredSummaryResponse (+16 more)

### Community 37 - "ATKCargoTheme"
Cohesion: 0.09
Nodes (19): Modifier, SearchBar(), AdaptiveLayoutConfig, rememberAdaptiveLayoutConfig(), WindowSizeClass, COMPACT, EXPANDED, MEDIUM (+11 more)

### Community 38 - "CargoInfo"
Cohesion: 0.26
Nodes (19): CargoInfo, FormSection(), CargoChangesPreview(), CargoEditConfirmDialog(), CargoEditConfirmHeader(), CargoMainInfo(), CargoShippingInfo(), CargoTimeInfo() (+11 more)

### Community 39 - "secrets.cpp"
Cohesion: 0.41
Nodes (17): decryptXor(), Java_com_atk_atk_1cargo_api_Secrets_getApiKey(), Java_com_atk_atk_1cargo_api_Secrets_getAuthenticationX365(), Java_com_atk_atk_1cargo_api_Secrets_getAuthUser(), Java_com_atk_atk_1cargo_api_Secrets_getBaseUrl(), Java_com_atk_atk_1cargo_api_Secrets_getExpectedSignatureHash(), Java_com_atk_atk_1cargo_api_Secrets_getLicenseCheckUrl(), Java_com_atk_atk_1cargo_api_Secrets_getLicenseInfoUrl() (+9 more)

### Community 40 - "AuthModels.kt"
Cohesion: 0.29
Nodes (6): ActiveSessionResponse, ForceLogoutResponse, LoginResponse, LogoutResponse, PasswordCheckResponse, PermissionSyncResponse

### Community 41 - "HomeScreen.kt"
Cohesion: 0.23
Nodes (13): MenuItem, getMenuItemsForUserType(), CategorizedMenuGrid(), CategoryHeader(), CompactMenuCard(), Header(), HomeScreen(), isWideCard() (+5 more)

### Community 42 - "ChatToolbar.kt"
Cohesion: 0.26
Nodes (12): ColorWheel(), Color, Modifier, AdvancedColorPickerDialog(), ChatSettingsDialogEnhanced(), ChatSettingsPreviewRefined(), Color, ImageVector (+4 more)

### Community 43 - "ChatMessageEntity"
Cohesion: 0.16
Nodes (3): ChatDao, Flow, ChatMessageEntity

### Community 44 - "CargoViewModel.kt"
Cohesion: 0.16
Nodes (9): CargoDeleteResponse, QuotaExistenceMultipleResponse, CheckQuotaUseCase, CargoViewModelFactory, Response, StateFlow, T, ViewModel (+1 more)

### Community 45 - "SecurityScreen.kt"
Cohesion: 0.25
Nodes (13): AdvancedOrbitalScanner(), AnimatedUpdateIcon(), AnimatedWarningIcon(), DynamicPremiumBackground(), InteractiveExitButton(), Modifier, RetryButton(), SecurityBlockScreen() (+5 more)

### Community 46 - "ChatScreen.kt"
Cohesion: 0.26
Nodes (13): ChatUiItem, getAdaptiveBubbleColor(), getChatBackgroundColor(), getDateHeaderColor(), Header, Color, Message, ChatScreen() (+5 more)

### Community 47 - "ActiveShipInfo"
Cohesion: 0.32
Nodes (15): ActiveShipInfo, CargoSnackbarMessage, GroupedShipList(), Color, Modifier, QuotaCard(), ShipGroup(), ShipHeader() (+7 more)

### Community 48 - "LoginScreen.kt"
Cohesion: 0.20
Nodes (20): CompactLoginLayout(), ExpandedLoginLayout(), IndustrialBrandingHeaderContent(), IndustrialFeatureBadge(), IndustrialFooterInfo(), IndustrialInputField(), IndustrialLoginFormCard(), androidx (+12 more)

### Community 49 - "MessageBubble.kt"
Cohesion: 0.27
Nodes (13): extractShipInfoAndText(), com, replaceUsernamesWithFullNames(), ShipInfoModel, ChatInfoRow(), EditMessageDialog(), androidx, Color (+5 more)

### Community 50 - "QuotaAnalysisSection.kt"
Cohesion: 0.19
Nodes (21): QuotaCompletionData, QuotaGroupingMode, BY_CARGO_OWNER, BY_CARRIER, BY_SHIP, buildQuotaGroups(), QuotaGroup, AnalyticsGroupingModeButton() (+13 more)

### Community 51 - "WarehouseDetailsScreen.kt"
Cohesion: 0.11
Nodes (35): persianDateFormat(), DateTimePicker(), Modifier, PersianDateItem(), TimePickerDialog(), EmptyVoucherList(), FilteredSummary, Modifier (+27 more)

### Community 52 - "composer.json"
Cohesion: 0.12
Nodes (15): autoload, autoload-dev, psr-4, psr-4, config, optimize-autoloader, sort-packages, description (+7 more)

### Community 56 - "QuotaDetailsScreen.kt"
Cohesion: 0.26
Nodes (13): ApiQuotaDetails, adjustColorForTheme(), Color, calculatePercentage(), calculateProgress(), Quota, ReportsViewModel, QuotaAdditionalInfo() (+5 more)

### Community 57 - "CargoDetailsDialogSection.kt"
Cohesion: 0.32
Nodes (14): CargoDetailsDialog(), CargoInfo, Color, ImageVector, Modifier, ModernCargoDetailsGrid(), ModernCargoStatusSection(), ModernCargoStatusTimeline() (+6 more)

### Community 58 - "CargoInfoDetailsDialogSection.kt"
Cohesion: 0.32
Nodes (13): CargoInfoRequest, CargoDetailsPalette, CargoDetailTabItem(), CargoInfoDetailsDialog(), DeleteDialog(), DetailInfoRow(), ImageVector, Modifier (+5 more)

### Community 60 - "ShipDetailsScreen.kt"
Cohesion: 0.30
Nodes (13): QuotaWarningThresholds, calculateWarningStatus(), ErrorStateCard(), ImageVector, Quota, ReportsViewModel, Ship, WarningStatus (+5 more)

### Community 63 - "RetrofitClient.kt"
Cohesion: 0.26
Nodes (9): FloatTypeAdapter, Context, RetrofitClient, ThirdPartyRetrofitClient, JsonReader, JsonWriter, OkHttpClient, Retrofit (+1 more)

### Community 64 - "formatNumber"
Cohesion: 0.33
Nodes (11): addOneDayToPersianDate(), buildQuotasShareText(), format(), formatHoursToPersian(), formatNumber(), formatWeightWithDetail(), getDaysInPersianMonth(), isPersianLeapYear() (+3 more)

### Community 67 - "ApiService.kt"
Cohesion: 0.20
Nodes (5): ApiResponse2, ThirdPartyApiService, RealTimeDataResponse, ThirdPartyOrderResponse, JsonElement

### Community 68 - "RegisterCargoDialogs.kt"
Cohesion: 0.23
Nodes (9): WarningStatus, AnimatedIcon(), DialogContent(), DialogPassword(), DuplicateConfirmationDialog(), FocusRequester, processScannedQuota(), QuotaEntryDialog() (+1 more)

### Community 70 - "hashPassword"
Cohesion: 0.20
Nodes (6): LoginRequest, SessionResponse, AuthRepositoryImpl, Flow, hashPassword(), SecurityUtilsTest

### Community 71 - "CargoModels.kt"
Cohesion: 0.11
Nodes (17): CargoInfoResponse, CargoInfoSearch, CargoSearchResponse, CargoStats, CheckExistenceResponse, ExistingCargo, InitialInfo, LoadableTonnageResponse (+9 more)

### Community 72 - "QuotaSelectionDialog.kt"
Cohesion: 0.42
Nodes (10): MatchingQuota, DialogHeader(), findDifferentFields(), InactiveIndicator(), ImageVector, QuotaDetailItem(), QuotaDetails(), QuotaHeader() (+2 more)

### Community 73 - "FontWeight"
Cohesion: 0.13
Nodes (14): ConfirmationDialog(), ImageVector, ActiveShipInfo, ShipSelectionDialog(), UserPreferencesManager, ThemeColorOption, ThemeColorPickerRow(), AdvancedSearchDialog() (+6 more)

### Community 74 - "ShipCard.kt"
Cohesion: 0.13
Nodes (20): ErrorState(), ImageVector, Modifier, Ship, ShipSortingMode, sortShips(), ImageVector, Modifier (+12 more)

### Community 75 - "CryptoManager"
Cohesion: 0.38
Nodes (4): CryptoManager, ByteArray, Cipher, SecretKey

### Community 77 - "FilterState"
Cohesion: 0.24
Nodes (11): ActiveQuotasDialog(), FilterBar(), EmptySearchResult(), FilterState, ALL, COMPLETED, PENDING, FlatQuotaCard() (+3 more)

### Community 78 - "SearchDialogs.kt"
Cohesion: 0.29
Nodes (10): CargoSearchResultCard(), InfoRowCompact(), Color, Context, ImageVector, MultipleSearchResultDialog(), SearchType, RECEIPT_NUMBER (+2 more)

### Community 80 - "ShipSortingMode"
Cohesion: 0.25
Nodes (7): ShipSortingMode, LOADED_TONNAGE_ASC, LOADED_TONNAGE_DESC, NAME_ASC, NAME_DESC, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC

### Community 81 - "CargoCounterNavigation.kt"
Cohesion: 0.36
Nodes (6): CargoCounterRoute, cargoCounterScreen(), NavController, navigateToCargoCounter(), CargoCounterOperationScreen(), NavController

### Community 82 - "ChatNotificationWorker.kt"
Cohesion: 0.43
Nodes (5): ChatNotificationWorker, KoinComponent, UserPreferencesManager, CoroutineWorker, ListenableWorker

### Community 83 - "PermissionPoller"
Cohesion: 0.31
Nodes (4): Job, StateFlow, PermissionPoller, PermissionSyncRequest

### Community 84 - "MainScreen.kt"
Cohesion: 0.33
Nodes (8): MainScreen(), RouteTransitions, standardTransitions(), LoginScreen(), HomeRoute, homeScreen(), NavController, navigateToHome()

### Community 85 - "ApiResponse"
Cohesion: 0.22
Nodes (5): ApiResponse, DeleteMessageRequest, DeleteUserRequest, EditMessageRequest, UpdateUserRequest

### Community 86 - "QuotaEntryDialog.kt"
Cohesion: 0.53
Nodes (8): ActiveShipInfo, FocusRequester, QuotaEntryActions(), QuotaEntryContent(), QuotaEntryDialog(), QuotaEntryPalette, rememberQuotaEntryPalette(), validateAndSubmit()

### Community 87 - "proxy_generator.php"
Cohesion: 0.57
Nodes (6): _o1(), _o2(), _o3(), _o4(), _o5(), _o6()

### Community 88 - "DataModel.kt"
Cohesion: 0.40
Nodes (4): adjustColorForTheme(), FabItem, Color, UpdateInfo

### Community 89 - "NotificationActionReceiver.kt"
Cohesion: 0.53
Nodes (4): BroadcastReceiver, Context, Intent, NotificationActionReceiver

### Community 90 - "DateRangePicker.kt"
Cohesion: 0.57
Nodes (7): DateClickableField(), DateRangePicker(), getDaysInPersianMonth(), isPersianLeapYear(), Color, Modifier, PersianDatePickerDialog()

### Community 92 - "StartupState"
Cohesion: 0.33
Nodes (6): Ready, SecurityBlocked, Splash, StartupState, Syncing, VersionExpired

### Community 93 - "AppDatabase"
Cohesion: 0.47
Nodes (3): AppDatabase, Context, RoomDatabase

### Community 94 - "ChatInputBar.kt"
Cohesion: 0.52
Nodes (6): rotateIcon(), Color, com, MessageInputArea(), QuotaSelectionDialog(), ShipSelectionDialog()

### Community 95 - "QuotasListDialogs.kt"
Cohesion: 0.53
Nodes (5): DeleteQuotaDialog(), androidx, Color, QuotaConfirmDialog(), ToggleQuotaStatusDialog()

### Community 96 - "ColorScheme.kt"
Cohesion: 0.80
Nodes (5): buildAppDarkColorScheme(), buildAppLightColorScheme(), compositeOver(), Color, ColorScheme

### Community 97 - "ColorSelector"
Cohesion: 0.39
Nodes (3): adjustColorForTheme(), ColorSelector, Color

### Community 98 - "LoadingState"
Cohesion: 0.47
Nodes (6): Error, Idle, Loading, LoadingState, Success, UiState

### Community 101 - "StatisticsCard.kt"
Cohesion: 0.70
Nodes (4): Color, ImageVector, Modifier, StatisticsCard()

### Community 102 - "GroupSortingMode"
Cohesion: 0.40
Nodes (4): GroupSortingMode, ALPHABETICAL, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC

### Community 103 - "WarehouseQuotaGroupingMode"
Cohesion: 0.40
Nodes (4): WarehouseQuotaGroupingMode, BY_CARGO_OWNER, BY_SHIPPING_COMPANY, BY_WAREHOUSE

### Community 104 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 106 - "CargoEntryNavigation.kt"
Cohesion: 0.32
Nodes (7): InitialInfoRoute, initialInfoScreen(), NavController, navigateToInitialInfo(), navigateToSelectInfo(), SelectInfoRoute, selectInfoScreen()

### Community 107 - "ExitStatusDialog.kt"
Cohesion: 0.83
Nodes (3): AnimatedCounter(), ExitStatusDialog(), ImageVector

### Community 112 - "ViewMode"
Cohesion: 0.67
Nodes (3): ViewMode, FLAT, GROUPED

### Community 121 - "Dp"
Cohesion: 0.46
Nodes (7): Color, Modifier, QuotasHeader(), StatItem(), VerticalDivider(), Dp, ViewMode

### Community 122 - "User"
Cohesion: 0.38
Nodes (6): UpdateUserRequest, User, EnhancedDeleteConfirmationDialog(), EnhancedForceLogoutDialog(), sortUsersByType(), ProfileSettingsDialog()

### Community 173 - "SelectInfoSnackbar.kt"
Cohesion: 0.57
Nodes (6): Color, Modifier, snackbarAccent(), snackbarAccentBg(), StatusSnackbar(), SnackbarMessage

### Community 176 - "ComprehensiveAnalyticsDialog.kt"
Cohesion: 0.57
Nodes (6): AnalyticsDateNavigation(), AnalyticsHeaderCard(), ComprehensiveAnalyticsDialog(), EmptyStateCard(), ErrorStateCard(), Modifier

### Community 177 - "ChatNavigation.kt"
Cohesion: 0.47
Nodes (5): AdminChatRoute, adminChatScreen(), NavController, UserPreferencesManager, navigateToAdminChat()

### Community 178 - "HomeNotificationSettingRow.kt"
Cohesion: 0.67
Nodes (5): CompactSwitch(), Color, ImageVector, Modifier, NotificationSettingRow()

### Community 180 - "CargoRegistrationNavigation.kt"
Cohesion: 0.60
Nodes (4): CargoRegistrationRoute, cargoRegistrationScreen(), NavController, navigateToCargoRegistration()

### Community 185 - "EmptyState.kt"
Cohesion: 0.83
Nodes (3): EmptyState(), ImageVector, Modifier

### Community 186 - "QuotaSortingMode"
Cohesion: 0.50
Nodes (3): QuotaSortingMode, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC

## Knowledge Gaps
- **109 isolated node(s):** `ExitDateInfo`, `VoucherDetail`, `QuotaStatusDetails`, `ExistingQuota`, `DateInfo` (+104 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **85 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `validateServerSession()` connect `ManageReportsScreen.kt` to `CargoCounterScreen.kt`, `StartupViewModel`, `SelectInfoScreen.kt`, `Exception`?**
  _High betweenness centrality (0.089) - this node is a cross-community bridge._
- **Why does `ReportsViewModel` connect `ReportsViewModel` to `ColorSelector`, `LoadingState`, `ReportModels.kt`, `GroupSortingMode`, `WarehouseQuotaGroupingMode`, `ManageReportsScreen.kt`, `ComprehensiveAnalyticsDialog.kt`, `ShipSortingMode`, `QuotaAnalysisSection.kt`, `QuotasListScreen.kt`, `MainScreen.kt`, `AppModule.kt`, `SelectInfoScreen.kt`, `QuotaSortingMode`?**
  _High betweenness centrality (0.062) - this node is a cross-community bridge._
- **Why does `ApiService` connect `ApiService` to `.deleteCargo`, `AppModule.kt`, `.getActiveDeviceId`, `ChatRepository`, `ReportModels.kt`, `CargoViewModel.kt`, `RetrofitClient.kt`, `.saveOrUpdateCargoInfo`, `.checkExistence`, `.checkLogin`, `.checkSession`, `.forceLogoutUser`, `.getAllUsers`, `.logout`, `.syncPermissions`, `ApiService.kt`, `.checkScaleReceiptNumber`, `.getCargoInfoByReceiptNumber`, `.getCargoInfoByTrackingNumber`, `.getChatMessages`, `.getLoadableTonnage`, `.getUnreadChatCount`, `hashPassword`, `CargoModels.kt`, `.sendChatMessage`, `ApiResponse`?**
  _High betweenness centrality (0.062) - this node is a cross-community bridge._
- **What connects `ExitDateInfo`, `VoucherDetail`, `QuotaStatusDetails` to the rest of the system?**
  _109 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `UserPreferencesManager` be split into smaller, more focused modules?**
  _Cohesion score 0.05747126436781609 - nodes in this community are weakly interconnected._
- **Should `AppApiController` be split into smaller, more focused modules?**
  _Cohesion score 0.08333333333333333 - nodes in this community are weakly interconnected._
- **Should `UsersManager` be split into smaller, more focused modules?**
  _Cohesion score 0.06936026936026936 - nodes in this community are weakly interconnected._