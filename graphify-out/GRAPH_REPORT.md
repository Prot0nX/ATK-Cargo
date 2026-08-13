# Graph Report - .  (2026-08-13)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 2158 nodes · 4654 edges · 160 communities (124 shown, 36 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 89 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `16ec36c1`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- QuotasListScreen.kt
- InitialInfoScreen.kt
- ManageReportsScreen.kt
- Color.kt
- WarehouseDetailsScreen.kt
- RegisterCargoScreen.kt
- UsersManager
- CargoViewModel
- ReportsViewModel
- ClassLoader
- AuthController
- CargoInfo
- SecurityVerifier
- AppApiController
- Ship
- StartupViewModel
- Database
- ATKCargoTheme
- preprocessImage
- SelectInfoScreen.kt
- UserPreferencesManager
- UpdateManager
- CargoRepository
- MicroCache
- ChatViewModel
- QuotaManagementDialog.kt
- SessionService
- ReportsRepository
- CargoDetailsScreen.kt
- RetrofitClient
- ReportModels.kt
- CargoCounterScreen.kt
- ChatRepository.kt
- CargoModels.kt
- ActiveQuotasDialogSection.kt
- Logger
- secrets.cpp
- ApiService
- LoadingNotificationService
- formatNumber
- RealTimeLoadingBottomSheet
- AppNotificationManager
- MainActivity.kt
- ChatMessageEntity
- LoginScreen.kt
- CargoController
- ReportsRepository.kt
- ActiveShipInfo
- MessageBubble.kt
- composer.json
- SessionManager
- SessionManager
- ChatToolbar.kt
- AuthViewModel
- CargoDetailsComponents.kt
- CargoDetailsDialogSection.kt
- SecurityScreen.kt
- Exception
- SessionRepository
- ChatRepository
- FontWeight
- AnalyticsController
- ChatController
- ChatScreen.kt
- LoginResult
- ProtectedProxy
- Request
- QuotaSelectionDialog.kt
- QuotaValidationUseCase.kt
- InitialInfo
- CargoViewModel.kt
- DatabaseSchemaExporter
- jdate
- PermissionPoller
- UpdateDialog.kt
- UtilityController
- PasswordGateService
- AppModule.kt
- CargoCounterOperationScreen
- QuotaEntryDialog.kt
- Config
- BootReceiver.kt
- DownloadState
- ProfileMenu.kt
- ComprehensiveAnalyticsDialog.kt
- SortType
- ChatNotificationWorker.kt
- proxy_generator.php
- NotificationActionReceiver.kt
- NavRoutes
- UserRepository
- AppDatabase
- MessageInputArea
- HomeNotificationSettingRow.kt
- SplashScreen.kt
- LoadingState
- JalaliDateUtils
- UpdateManagerFactory
- AnimationManager
- gradlew
- EmptyState.kt
- config.php
- ComposerAutoloaderInit5f285832cbbfb0bffdb861048ac414db
- ExampleInstrumentedTest
- Constants
- ColorExtensions.kt
- ConfirmationDialog.kt
- SearchBar.kt
- HardwarePerformanceEvaluator
- ComponentDefaults.kt
- ExampleUnitTest
- BaselineProfileGenerator
- Result
- StateFlow
- T
- ViewModel
- ViewModelProvider
- QuotaExistenceMultipleResponse

## God Nodes (most connected - your core abstractions)
1. `ReportsViewModel` - 103 edges
2. `CargoViewModel` - 79 edges
3. `ApiService` - 56 edges
4. `formatNumber()` - 46 edges
5. `UserPreferencesManager` - 44 edges
6. `AppApiController` - 40 edges
7. `CargoInfo` - 39 edges
8. `UpdateManager` - 38 edges
9. `ActiveShipInfo` - 35 edges
10. `StartupViewModel` - 30 edges

## Surprising Connections (you probably didn't know these)
- `validateServerSession()` --calls--> `SessionCheckRequest`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/api/SessionValidator.kt → app/src/main/java/com/atk/atk_cargo/data/model/AuthModels.kt
- `CargoCounterScreen()` --calls--> `ColorSelector`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_counter/presentation/CargoCounterScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt
- `SelectInfoScreenContent()` --calls--> `ColorSelector`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/SelectInfoScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt
- `QuotasDialog()` --calls--> `ColorSelector`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/reports/presentation/quota_details/QuotaDetailsScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt
- `ActiveQuotasDialog()` --calls--> `ActiveShipInfo`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/ActiveQuotasDialogSection.kt → app/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt

## Import Cycles
- None detected.

## Communities (160 total, 36 thin omitted)

### Community 0 - "QuotasListScreen.kt"
Cohesion: 0.05
Nodes (86): CalculationResult, Quota, QuotaEditData, QuotaPercentageData, WarningStatus, addOneDayToPersianDate(), buildQuotasShareText(), calculateProgress() (+78 more)

### Community 1 - "InitialInfoScreen.kt"
Cohesion: 0.06
Nodes (60): MainScreen(), RouteTransitions, standardTransitions(), CheckExistenceRequest, MenuItem, LoginScreen(), formatNumber(), isValidPersianText() (+52 more)

### Community 2 - "ManageReportsScreen.kt"
Cohesion: 0.06
Nodes (59): ApiQuotaDetails, adjustColorForTheme(), FabItem, Color, toTon(), Result, validateServerSession(), DateClickableField() (+51 more)

### Community 3 - "Color.kt"
Cohesion: 0.06
Nodes (44): ActiveSessionResponse, CreateUserRequest, DeleteUserRequest, ForceLogoutRequest, ForceLogoutResponse, LoginRequest, LoginResponse, PasswordCheckResponse (+36 more)

### Community 4 - "WarehouseDetailsScreen.kt"
Cohesion: 0.09
Nodes (40): FilteredSummary, VoucherDetail, Warehouse, ExportPdfUseCase, PdfColorScheme, PdfFonts, PersianFontManager, ShamsiDate (+32 more)

### Community 5 - "RegisterCargoScreen.kt"
Cohesion: 0.08
Nodes (43): Modifier, StatusSnackbar(), MessageType, ERROR, SUCCESS, WARNING, ShipInfo, CargoSnackbarQueue (+35 more)

### Community 6 - "UsersManager"
Cohesion: 0.07
Nodes (7): ApiManager, CONFIG, ModalManager, ThemeManager, UIManager, UsersManager, Utils

### Community 7 - "CargoViewModel"
Cohesion: 0.11
Nodes (6): CargoViewModel, CargoInfo, InitialInfo, CargoInfoRequest, MatchingQuota, SaveOrUpdateResponse

### Community 8 - "ReportsViewModel"
Cohesion: 0.06
Nodes (18): ComprehensiveAnalytics, GroupSortingMode, ALPHABETICAL, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC, QuotaSortingMode, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC (+10 more)

### Community 9 - "ClassLoader"
Cohesion: 0.05
Nodes (4): Composer\Semver\VersionParser, ComposerStaticInit5f285832cbbfb0bffdb861048ac414db, ClassLoader, InstalledVersions

### Community 10 - "AuthController"
Cohesion: 0.08
Nodes (5): AuthController, UserController, Response, PermissionService, InputValidator

### Community 11 - "CargoInfo"
Cohesion: 0.12
Nodes (37): CargoInfo, CargoInfoRequest, CargoDetailsPalette, CargoDetailTabItem(), CargoInfoDetailsDialog(), DeleteDialog(), DetailInfoRow(), ImageVector (+29 more)

### Community 12 - "SecurityVerifier"
Cohesion: 0.09
Nodes (11): Secrets, SecurityErrorType, LICENSE_INACTIVE, LICENSE_NOT_FOUND, NETWORK_ERROR, TAMPERED, UNKNOWN_ERROR, SecurityVerifier (+3 more)

### Community 14 - "Ship"
Cohesion: 0.11
Nodes (23): ErrorState(), ImageVector, Modifier, Ship, ShipSortingMode, LOADED_TONNAGE_ASC, LOADED_TONNAGE_DESC, NAME_ASC (+15 more)

### Community 15 - "StartupViewModel"
Cohesion: 0.08
Nodes (19): AtkCargoApplication, AndroidViewModel, Intent, StateFlow, Ready, RequestBatteryOptimization, SecurityBlocked, SecurityCheckState (+11 more)

### Community 16 - "Database"
Cohesion: 0.10
Nodes (9): mysqli, mysqli_stmt, PDO, PDOException, LicenseController, Database, PDO, self (+1 more)

### Community 17 - "ATKCargoTheme"
Cohesion: 0.09
Nodes (22): AdaptiveLayoutConfig, rememberAdaptiveLayoutConfig(), WindowSizeClass, COMPACT, EXPANDED, MEDIUM, buildAppDarkColorScheme(), buildAppLightColorScheme() (+14 more)

### Community 18 - "preprocessImage"
Cohesion: 0.13
Nodes (22): adaptiveThresholding(), applyContrastEnhancement(), enhanceEdges(), ImageProxy, InputImage, medianFilter(), preprocessImage(), EnhancedNumberAnalyzer (+14 more)

### Community 19 - "SelectInfoScreen.kt"
Cohesion: 0.17
Nodes (24): formatNumber(), RealTimeLoadingData, AnimatedHeader(), ShipSelectionDialog(), extractLastDigits(), FilterChip(), FlatQuotaCard(), GroupedShipList() (+16 more)

### Community 20 - "UserPreferencesManager"
Cohesion: 0.10
Nodes (8): TypeToken, Flow, T, UserPreferencesManager, ThemeColorOption, ThemeColorPickerRow(), DataStore, Preferences

### Community 21 - "UpdateManager"
Cohesion: 0.12
Nodes (8): DownloadProgress, Context, Job, StateFlow, ViewModel, UpdateManager, VersionCheckResult, OkHttpClient

### Community 22 - "CargoRepository"
Cohesion: 0.08
Nodes (4): App\Core\Database, Logger, CargoRepository, Logger

### Community 23 - "MicroCache"
Cohesion: 0.13
Nodes (11): App\Core\AuthenticatesRequests, App\Core\Logger, App\Core\MicroCache, App\Core\Request, App\Core\Response, App\Exceptions\ApiException, App\Exceptions\ConflictException, App\Services\PasswordGateService (+3 more)

### Community 24 - "ChatViewModel"
Cohesion: 0.10
Nodes (7): ChatViewModel, ChatViewModelFactory, com, StateFlow, T, ViewModel, ViewModelProvider

### Community 25 - "QuotaManagementDialog.kt"
Cohesion: 0.24
Nodes (23): QuotaItem, AdvancedFiltersDialog(), AnalyticsStatChipMini(), CompactStatChip(), EmptyQuotaState(), FilterOptionsCard(), InfoChip(), IntegratedQuotaCard() (+15 more)

### Community 27 - "ReportsRepository"
Cohesion: 0.16
Nodes (5): ErrorResponse, HttpStatusException, Exception, Result, ReportsRepository

### Community 28 - "CargoDetailsScreen.kt"
Cohesion: 0.16
Nodes (21): CargoDetailsScreen(), confirmCargo(), FloatingActionButtonItem(), handleCargoConfirmation(), CargoInfo, Color, ImageVector, NavController (+13 more)

### Community 29 - "RetrofitClient"
Cohesion: 0.16
Nodes (12): FloatTypeAdapter, Context, RetrofitClient, ThirdPartyRetrofitClient, CryptoManager, ByteArray, Cipher, JsonReader (+4 more)

### Community 30 - "ReportModels.kt"
Cohesion: 0.13
Nodes (12): ThirdPartyApiService, adjustColorForTheme(), AnalyticsData, ColorSelector, DateInfo, ExistingQuota, ExitDateInfo, Color (+4 more)

### Community 31 - "CargoCounterScreen.kt"
Cohesion: 0.20
Nodes (17): CargoCounterScreen(), CargoCounterViewModel, filterShipsByTab(), com, NavController, SnackbarHostState, StateFlow, ViewModel (+9 more)

### Community 32 - "ChatRepository.kt"
Cohesion: 0.13
Nodes (9): ApiResponse, ChatMessage, ChatMessagesResponse, SendMessageRequest, SendMessageResponse, UnreadCountResponse, DeleteMessageRequest, EditMessageRequest (+1 more)

### Community 33 - "CargoModels.kt"
Cohesion: 0.12
Nodes (10): CargoDeleteResponse, CargoInfoSearch, CargoSearchResponse, CargoStats, CheckExistenceResponse, ExistingCargo, LoadableTonnageResponse, QuotaTonnageWarning (+2 more)

### Community 34 - "ActiveQuotasDialogSection.kt"
Cohesion: 0.18
Nodes (18): ActiveQuotasDialog(), FilterBar(), Color, Dp, Modifier, QuotasHeader(), StatItem(), VerticalDivider() (+10 more)

### Community 35 - "Logger"
Cohesion: 0.16
Nodes (3): OnlineUsersController, Logger, self

### Community 36 - "secrets.cpp"
Cohesion: 0.41
Nodes (17): decryptXor(), Java_com_atk_atk_1cargo_api_Secrets_getApiKey(), Java_com_atk_atk_1cargo_api_Secrets_getAuthenticationX365(), Java_com_atk_atk_1cargo_api_Secrets_getAuthUser(), Java_com_atk_atk_1cargo_api_Secrets_getBaseUrl(), Java_com_atk_atk_1cargo_api_Secrets_getExpectedSignatureHash(), Java_com_atk_atk_1cargo_api_Secrets_getLicenseCheckUrl(), Java_com_atk_atk_1cargo_api_Secrets_getLicenseInfoUrl() (+9 more)

### Community 37 - "ApiService"
Cohesion: 0.14
Nodes (7): ApiResponse2, ApiService, SuccessResponse, FilteredSummaryResponse, RealTimeDataResponse, ShipsData, JsonElement

### Community 38 - "LoadingNotificationService"
Cohesion: 0.22
Nodes (6): Context, Intent, KoinComponent, LoadingNotificationService, IBinder, Service

### Community 39 - "formatNumber"
Cohesion: 0.28
Nodes (17): QuotaCompletionData, QuotaGroupingMode, BY_CARGO_OWNER, BY_CARRIER, BY_SHIP, formatNumber(), AnalyticsGroupingModeButton(), AnalyticsOwnerSummaryCard() (+9 more)

### Community 40 - "RealTimeLoadingBottomSheet"
Cohesion: 0.20
Nodes (15): ShiftInfo, DialogHeader(), Color, Modifier, RealTimeLoadingBottomSheet(), RealTimeShiftNavigation(), RefreshOverlay(), ShipCard() (+7 more)

### Community 42 - "MainActivity.kt"
Cohesion: 0.21
Nodes (9): UpdateInfo, ServerSyncingScreen(), ModernUpdateContent(), ModernUpdateHeader(), UpdateDialog(), Intent, MainActivity, Bundle (+1 more)

### Community 43 - "ChatMessageEntity"
Cohesion: 0.16
Nodes (3): ChatDao, Flow, ChatMessageEntity

### Community 44 - "LoginScreen.kt"
Cohesion: 0.26
Nodes (16): CompactLoginLayout(), ExpandedLoginLayout(), IndustrialBrandingHeaderContent(), IndustrialFeatureBadge(), IndustrialFooterInfo(), IndustrialInputField(), IndustrialLoginFormCard(), androidx (+8 more)

### Community 46 - "ReportsRepository.kt"
Cohesion: 0.20
Nodes (4): CargoInfoResponse, ComprehensiveAnalysisResponse, QuotaDetails, QuotaStatusResponse

### Community 47 - "ActiveShipInfo"
Cohesion: 0.32
Nodes (15): ActiveShipInfo, CargoSnackbarMessage, GroupedShipList(), Color, Modifier, QuotaCard(), ShipGroup(), ShipHeader() (+7 more)

### Community 48 - "MessageBubble.kt"
Cohesion: 0.24
Nodes (14): extractShipInfoAndText(), com, replaceUsernamesWithFullNames(), rotateIcon(), ShipInfoModel, ChatInfoRow(), EditMessageDialog(), androidx (+6 more)

### Community 49 - "composer.json"
Cohesion: 0.12
Nodes (15): autoload, autoload-dev, psr-4, psr-4, config, optimize-autoloader, sort-packages, description (+7 more)

### Community 52 - "ChatToolbar.kt"
Cohesion: 0.25
Nodes (13): ColorWheel(), Color, Modifier, getAdaptiveBubbleColor(), AdvancedColorPickerDialog(), ChatSettingsDialogEnhanced(), ChatSettingsPreviewRefined(), Color (+5 more)

### Community 53 - "AuthViewModel"
Cohesion: 0.17
Nodes (8): AuthViewModel, Error, Idle, StateFlow, ViewModel, Loading, LoginUiState, Success

### Community 54 - "CargoDetailsComponents.kt"
Cohesion: 0.30
Nodes (14): CargoInfoCard(), CargoListSection(), GroupStats(), InfoChips(), InfoGrid(), InfoGridCard(), InfoGridItem(), InitialInfoSection() (+6 more)

### Community 55 - "CargoDetailsDialogSection.kt"
Cohesion: 0.32
Nodes (14): CargoDetailsDialog(), CargoInfo, Color, ImageVector, Modifier, ModernCargoDetailsGrid(), ModernCargoStatusSection(), ModernCargoStatusTimeline() (+6 more)

### Community 56 - "SecurityScreen.kt"
Cohesion: 0.27
Nodes (12): AdvancedOrbitalScanner(), AnimatedUpdateIcon(), AnimatedWarningIcon(), DynamicPremiumBackground(), InteractiveExitButton(), Modifier, RetryButton(), SecurityBlockScreen() (+4 more)

### Community 57 - "Exception"
Cohesion: 0.19
Nodes (4): Exception, ApiException, ConflictException, DatabaseException

### Community 59 - "ChatRepository"
Cohesion: 0.22
Nodes (3): ChatRepository, com, Result

### Community 60 - "FontWeight"
Cohesion: 0.19
Nodes (6): AnimatedCounter(), ExitStatusDialog(), ImageVector, VazirmatnFontFamily, FontFamily, FontWeight

### Community 64 - "ChatScreen.kt"
Cohesion: 0.27
Nodes (12): ChatUiItem, getChatBackgroundColor(), getDateHeaderColor(), Header, Color, Message, ChatScreen(), DateHeader() (+4 more)

### Community 65 - "LoginResult"
Cohesion: 0.23
Nodes (7): AuthRepository, ConflictSession, Error, Flow, LoginResult, Success, LoginUseCase

### Community 68 - "QuotaSelectionDialog.kt"
Cohesion: 0.42
Nodes (10): MatchingQuota, DialogHeader(), findDifferentFields(), InactiveIndicator(), ImageVector, QuotaDetailItem(), QuotaDetails(), QuotaHeader() (+2 more)

### Community 69 - "QuotaValidationUseCase.kt"
Cohesion: 0.27
Nodes (6): InputValidationResult, InitialInfo, QuotaValidationUseCase, TempTonnageValidationResult, MessageType, QuotaValidationResult

### Community 70 - "InitialInfo"
Cohesion: 0.24
Nodes (4): InitialInfo, SaveOrUpdateResponse, SubmitCargoUseCase, Parcelable

### Community 71 - "CargoViewModel.kt"
Cohesion: 0.22
Nodes (7): CargoViewModelFactory, QuotaExistenceMultipleResponse, Response, StateFlow, T, ViewModel, ViewModelProvider

### Community 73 - "jdate"
Cohesion: 0.53
Nodes (9): gregorian_to_jalali(), jalali_to_gregorian(), jcheckdate(), jdate(), jdate_words(), jgetdate(), jmktime(), jstrftime() (+1 more)

### Community 74 - "PermissionPoller"
Cohesion: 0.36
Nodes (3): Job, StateFlow, PermissionPoller

### Community 75 - "UpdateDialog.kt"
Cohesion: 0.44
Nodes (8): DownloadActionButtons(), DownloadingState(), DownloadProgressInfo(), DownloadSizeInfo(), DownloadSpeedInfo(), ErrorState(), ModernUpdateActionSection(), PausedState()

### Community 78 - "AppModule.kt"
Cohesion: 0.29
Nodes (4): LogoutRequest, LogoutResponse, Result, LogoutUseCase

### Community 79 - "CargoCounterOperationScreen"
Cohesion: 0.36
Nodes (6): CargoCounterRoute, cargoCounterScreen(), NavController, navigateToCargoCounter(), CargoCounterOperationScreen(), NavController

### Community 80 - "QuotaEntryDialog.kt"
Cohesion: 0.57
Nodes (7): FocusRequester, QuotaEntryActions(), QuotaEntryContent(), QuotaEntryDialog(), QuotaEntryPalette, rememberQuotaEntryPalette(), validateAndSubmit()

### Community 82 - "BootReceiver.kt"
Cohesion: 0.48
Nodes (5): BootReceiver, BroadcastReceiver, Context, Intent, KoinComponent

### Community 83 - "DownloadState"
Cohesion: 0.29
Nodes (6): Completed, Downloading, DownloadState, Error, Idle, Paused

### Community 84 - "ProfileMenu.kt"
Cohesion: 0.52
Nodes (6): ActionButtons(), getUserTypeDisplay(), Color, ProfileMenu(), ProfilePalette, rememberProfilePalette()

### Community 85 - "ComprehensiveAnalyticsDialog.kt"
Cohesion: 0.57
Nodes (6): AnalyticsDateNavigation(), AnalyticsHeaderCard(), ComprehensiveAnalyticsDialog(), EmptyStateCard(), ErrorStateCard(), Modifier

### Community 86 - "SortType"
Cohesion: 0.29
Nodes (7): SortType, NAME_ASC, NAME_DESC, QUOTA_COUNT_ASC, QUOTA_COUNT_DESC, TOTAL_WEIGHT_ASC, TOTAL_WEIGHT_DESC

### Community 87 - "ChatNotificationWorker.kt"
Cohesion: 0.48
Nodes (4): ChatNotificationWorker, KoinComponent, CoroutineWorker, ListenableWorker

### Community 88 - "proxy_generator.php"
Cohesion: 0.57
Nodes (6): _o1(), _o2(), _o3(), _o4(), _o5(), _o6()

### Community 89 - "NotificationActionReceiver.kt"
Cohesion: 0.53
Nodes (4): BroadcastReceiver, Context, Intent, NotificationActionReceiver

### Community 92 - "AppDatabase"
Cohesion: 0.47
Nodes (3): AppDatabase, Context, RoomDatabase

### Community 93 - "MessageInputArea"
Cohesion: 0.67
Nodes (5): Color, com, MessageInputArea(), QuotaSelectionDialog(), ShipSelectionDialog()

### Community 94 - "HomeNotificationSettingRow.kt"
Cohesion: 0.67
Nodes (5): CompactSwitch(), Color, ImageVector, Modifier, NotificationSettingRow()

### Community 95 - "SplashScreen.kt"
Cohesion: 0.53
Nodes (5): SplashScreen(), SplashScreenConstants, SplashScreenContent(), SplashSubtitleBadge(), SplashVersionBadge()

### Community 96 - "LoadingState"
Cohesion: 0.47
Nodes (6): Error, Idle, Loading, LoadingState, Success, UiState

### Community 98 - "UpdateManagerFactory"
Cohesion: 0.50
Nodes (3): T, ViewModelProvider, UpdateManagerFactory

### Community 100 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 101 - "EmptyState.kt"
Cohesion: 0.83
Nodes (3): EmptyState(), ImageVector, Modifier

## Knowledge Gaps
- **92 isolated node(s):** `ShipsTabItem`, `RequestBatteryOptimization`, `ThemeColorOption`, `ErrorResponse`, `AnalyticsData` (+87 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **36 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CargoViewModel` connect `CargoViewModel` to `InitialInfoScreen.kt`, `RegisterCargoScreen.kt`, `QuotaValidationUseCase.kt`, `CargoViewModel.kt`, `CargoInfo`, `AppModule.kt`, `CargoCounterOperationScreen`, `ActiveShipInfo`, `SelectInfoScreen.kt`, `CargoDetailsScreen.kt`?**
  _High betweenness centrality (0.154) - this node is a cross-community bridge._
- **Why does `confirmCargo()` connect `CargoDetailsScreen.kt` to `Exception`?**
  _High betweenness centrality (0.104) - this node is a cross-community bridge._
- **Why does `ReportsViewModel` connect `ReportsViewModel` to `QuotasListScreen.kt`, `InitialInfoScreen.kt`, `ManageReportsScreen.kt`, `LoadingState`, `WarehouseDetailsScreen.kt`, `ApiService`, `formatNumber`, `RealTimeLoadingBottomSheet`, `CargoInfo`, `AppModule.kt`, `Ship`, `ReportsRepository.kt`, `ComprehensiveAnalyticsDialog.kt`, `QuotaManagementDialog.kt`, `.showSnackbar`, `ReportModels.kt`?**
  _High betweenness centrality (0.068) - this node is a cross-community bridge._
- **What connects `ShipsTabItem`, `RequestBatteryOptimization`, `ThemeColorOption` to the rest of the system?**
  _92 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `QuotasListScreen.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.050615901455767075 - nodes in this community are weakly interconnected._
- **Should `InitialInfoScreen.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.0578386605783866 - nodes in this community are weakly interconnected._
- **Should `ManageReportsScreen.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.06196291270918137 - nodes in this community are weakly interconnected._