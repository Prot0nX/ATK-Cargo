# Graph Report - .  (2026-08-13)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 2127 nodes · 4659 edges · 158 communities (128 shown, 30 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 78 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `0856976a`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- InitialInfoScreen.kt
- LoginScreen.kt
- ManageReportsScreen.kt
- ExportPdfUseCase
- ReportsViewModel
- UsersManager
- UpdateManager
- CargoViewModel
- ClassLoader
- AuthController
- AppApiController
- SecurityVerifier
- QuotasListScreen.kt
- Ship
- Database
- CargoDetailsScreen.kt
- QuotaManagementDialog.kt
- CargoRepository
- StartupViewModel
- QuotaWarningDialog.kt
- Logger
- ReportModels.kt
- ATKCargoTheme
- ChatViewModel
- SelectInfoScreen.kt
- UserPreferencesManager
- ReportsRepository
- AnalyticsController
- SessionService
- SessionManager
- ApiService
- CargoCounterScreen.kt
- preprocessImage
- VoucherDetailsDialogSection.kt
- MessageType
- WarehouseDetailsScreen.kt
- EmptyState.kt
- ActiveQuotasDialogSection.kt
- CargoInfo
- QuotaPercentageDialogSection.kt
- secrets.cpp
- CargoModels.kt
- LoadingNotificationService
- QuotaAnalysisSection.kt
- RealTimeLoadingBottomSheet
- AuthModels.kt
- AppNotificationManager
- ChatMessageEntity
- CargoController
- ActiveShipInfo
- MessageBubble.kt
- composer.json
- SessionManager
- ChatToolbar.kt
- UserManagementScreen.kt
- StartupViewModel.kt
- FontWeight
- SecurityScreen.kt
- UtilityController
- ChatRepository
- CargoDetailsDialogSection.kt
- ChatController
- RetrofitClient
- CargoInfoDetailsDialogSection.kt
- ChatScreen.kt
- CoroutineScope
- UserManagementDialogsSection.kt
- MainActivity.kt
- ProtectedProxy
- Request
- ChatRepository.kt
- QuotaSelectionDialog.kt
- SearchDialogs.kt
- Config
- User
- ProfileMenu.kt
- CryptoManager
- DatabaseSchemaExporter
- PermissionPoller
- ReportsRepository.kt
- formatNumber
- PasswordGateService
- ApiResponse
- QuotaValidationUseCase.kt
- CargoCounterOperationScreen
- QuotaEntryDialog.kt
- ComprehensiveAnalyticsDialog.kt
- CargoViewModel.kt
- hashPassword
- ChatNotificationWorker.kt
- proxy_generator.php
- QuotaExistenceMultipleResponse
- NotificationActionReceiver.kt
- NavRoutes
- StartupState
- AppDatabase
- MessageInputArea
- HomeNotificationSettingRow.kt
- LoadingState
- JalaliDateUtils
- ChatNavigation.kt
- gradlew
- WarehouseDateTimePicker.kt
- config.php
- ComposerAutoloaderInit5f285832cbbfb0bffdb861048ac414db
- ExampleInstrumentedTest
- Constants
- ColorExtensions.kt
- QuotasListDialogs.kt
- HardwarePerformanceEvaluator
- ComponentDefaults.kt
- ExampleUnitTest
- BaselineProfileGenerator
- AdvancedSearchDialog.kt
- .clearMutedShips
- ExitStatusDialog.kt
- ConfirmationDialog.kt

## God Nodes (most connected - your core abstractions)
1. `ReportsViewModel` - 105 edges
2. `CargoViewModel` - 81 edges
3. `CargoInfo` - 57 edges
4. `ApiService` - 56 edges
5. `UserPreferencesManager` - 46 edges
6. `formatNumber()` - 46 edges
7. `AppApiController` - 40 edges
8. `UpdateManager` - 38 edges
9. `Logger` - 35 edges
10. `ActiveShipInfo` - 35 edges

## Surprising Connections (you probably didn't know these)
- `InitialInfoScreen()` --calls--> `InitialInfo`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/InitialInfoScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/CargoModels.kt
- `navigateToRegisterCargoActivity()` --calls--> `InitialInfo`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/SelectInfoScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/CargoModels.kt
- `CargoCounterScreen()` --calls--> `ColorSelector`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_counter/presentation/CargoCounterScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt
- `SelectInfoScreenContent()` --calls--> `ColorSelector`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/SelectInfoScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt
- `QuotasDialog()` --calls--> `ColorSelector`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/reports/presentation/quota_details/QuotaDetailsScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt

## Import Cycles
- None detected.

## Communities (158 total, 30 thin omitted)

### Community 0 - "InitialInfoScreen.kt"
Cohesion: 0.08
Nodes (44): CheckExistenceRequest, MenuItem, formatNumber(), isValidPersianText(), isValidQuotaNumber(), isValidShipName(), isValidWarehouseName(), isValidWeight() (+36 more)

### Community 1 - "LoginScreen.kt"
Cohesion: 0.06
Nodes (42): LoginRequest, LoginResponse, SessionResponse, AuthRepository, ConflictSession, Error, Flow, LoginResult (+34 more)

### Community 2 - "ManageReportsScreen.kt"
Cohesion: 0.05
Nodes (67): ApiQuotaDetails, adjustColorForTheme(), FabItem, Color, toTon(), Result, validateServerSession(), MainScreen() (+59 more)

### Community 3 - "ExportPdfUseCase"
Cohesion: 0.20
Nodes (13): FilteredSummary, ExportPdfUseCase, PdfColorScheme, PdfFonts, PersianFontManager, ShamsiDate, VazirmatnFontFamily, Document (+5 more)

### Community 5 - "UsersManager"
Cohesion: 0.07
Nodes (7): ApiManager, CONFIG, ModalManager, ThemeManager, UIManager, UsersManager, Utils

### Community 6 - "UpdateManager"
Cohesion: 0.07
Nodes (28): UpdateInfo, Completed, Downloading, DownloadProgress, DownloadState, Error, Idle, Context (+20 more)

### Community 8 - "ClassLoader"
Cohesion: 0.05
Nodes (4): Composer\Semver\VersionParser, ComposerStaticInit5f285832cbbfb0bffdb861048ac414db, ClassLoader, InstalledVersions

### Community 9 - "AuthController"
Cohesion: 0.06
Nodes (6): AuthController, UserController, Response, PermissionService, UserService, InputValidator

### Community 11 - "SecurityVerifier"
Cohesion: 0.09
Nodes (11): Secrets, SecurityErrorType, LICENSE_INACTIVE, LICENSE_NOT_FOUND, NETWORK_ERROR, TAMPERED, UNKNOWN_ERROR, SecurityVerifier (+3 more)

### Community 12 - "QuotasListScreen.kt"
Cohesion: 0.18
Nodes (22): QuotaEditData, WarehouseQuotaGroupingMode, BY_CARGO_OWNER, BY_SHIPPING_COMPANY, BY_WAREHOUSE, ActionButton(), ConfirmationDialogHeader(), EditFieldBox() (+14 more)

### Community 13 - "Ship"
Cohesion: 0.11
Nodes (23): ErrorState(), ImageVector, Modifier, Ship, ShipSortingMode, LOADED_TONNAGE_ASC, LOADED_TONNAGE_DESC, NAME_ASC (+15 more)

### Community 14 - "Database"
Cohesion: 0.09
Nodes (12): App\Core\AuthenticatesRequests, Exception, InvalidArgumentException, mysqli, mysqli_stmt, PDOException, Database, self (+4 more)

### Community 15 - "CargoDetailsScreen.kt"
Cohesion: 0.13
Nodes (25): InitialInfo, CargoDetailsScreen(), confirmCargo(), FloatingActionButtonItem(), handleCargoConfirmation(), handleQuotaChangeInDetails(), Color, ImageVector (+17 more)

### Community 16 - "QuotaManagementDialog.kt"
Cohesion: 0.16
Nodes (30): QuotaItem, AdvancedFiltersDialog(), AnalyticsStatChipMini(), CompactStatChip(), EmptyQuotaState(), FilterOptionsCard(), InfoChip(), IntegratedQuotaCard() (+22 more)

### Community 18 - "StartupViewModel"
Cohesion: 0.16
Nodes (3): Intent, SecurityCheckState, StartupViewModel

### Community 19 - "QuotaWarningDialog.kt"
Cohesion: 0.26
Nodes (16): WarningStatus, CompactInfoSection(), DialogHeader(), Color, ImageVector, Modifier, LoadingActionButton(), NavigationButton() (+8 more)

### Community 20 - "Logger"
Cohesion: 0.13
Nodes (4): LicenseController, OnlineUsersController, Logger, self

### Community 21 - "ReportModels.kt"
Cohesion: 0.08
Nodes (25): ThirdPartyApiService, adjustColorForTheme(), AnalyticsData, ColorSelector, DateInfo, ExistingQuota, ExitDateInfo, GroupSortingMode (+17 more)

### Community 22 - "ATKCargoTheme"
Cohesion: 0.12
Nodes (16): buildAppDarkColorScheme(), buildAppLightColorScheme(), compositeOver(), Color, ComponentStyles, Dimensions, Elevation, ATKCargoTheme (+8 more)

### Community 23 - "ChatViewModel"
Cohesion: 0.10
Nodes (7): ChatViewModel, ChatViewModelFactory, com, StateFlow, T, ViewModel, ViewModelProvider

### Community 24 - "SelectInfoScreen.kt"
Cohesion: 0.18
Nodes (22): formatNumber(), RealTimeLoadingData, AnimatedHeader(), ShipSelectionDialog(), extractLastDigits(), FilterChip(), GroupedShipList(), Color (+14 more)

### Community 25 - "UserPreferencesManager"
Cohesion: 0.11
Nodes (6): TypeToken, Flow, T, UserPreferencesManager, DataStore, Preferences

### Community 26 - "ReportsRepository"
Cohesion: 0.15
Nodes (5): ErrorResponse, HttpStatusException, Exception, Result, ReportsRepository

### Community 27 - "AnalyticsController"
Cohesion: 0.19
Nodes (10): gregorian_to_jalali(), jalali_to_gregorian(), jcheckdate(), jdate(), jdate_words(), jgetdate(), jmktime(), jstrftime() (+2 more)

### Community 28 - "SessionService"
Cohesion: 0.05
Nodes (6): PDO, PDO, PDO, SessionRepository, UserRepository, SessionService

### Community 30 - "ApiService"
Cohesion: 0.13
Nodes (7): ApiResponse2, ApiService, SuccessResponse, FilteredSummaryResponse, RealTimeDataResponse, ShipsData, JsonElement

### Community 31 - "CargoCounterScreen.kt"
Cohesion: 0.20
Nodes (17): CargoCounterScreen(), CargoCounterViewModel, filterShipsByTab(), com, NavController, SnackbarHostState, StateFlow, ViewModel (+9 more)

### Community 32 - "preprocessImage"
Cohesion: 0.13
Nodes (22): adaptiveThresholding(), applyContrastEnhancement(), enhanceEdges(), ImageProxy, InputImage, medianFilter(), preprocessImage(), EnhancedNumberAnalyzer (+14 more)

### Community 33 - "VoucherDetailsDialogSection.kt"
Cohesion: 0.26
Nodes (14): VoucherDetail, EmptyVoucherList(), Modifier, SearchTextField(), SortChip(), VoucherDetailsDialog(), VoucherExpandedDetails(), VoucherItem() (+6 more)

### Community 34 - "MessageType"
Cohesion: 0.06
Nodes (51): Modifier, SearchBar(), Modifier, StatusSnackbar(), MessageType, ERROR, SUCCESS, WARNING (+43 more)

### Community 35 - "WarehouseDetailsScreen.kt"
Cohesion: 0.29
Nodes (11): Warehouse, ExportOptions(), Modifier, QuotaChip(), QuotaSelector(), VoucherDetailsButton(), WarehouseCard(), WarehouseContent() (+3 more)

### Community 36 - "EmptyState.kt"
Cohesion: 0.83
Nodes (3): EmptyState(), ImageVector, Modifier

### Community 37 - "ActiveQuotasDialogSection.kt"
Cohesion: 0.16
Nodes (19): ActiveQuotasDialog(), FilterBar(), Color, Dp, Modifier, QuotasHeader(), StatItem(), VerticalDivider() (+11 more)

### Community 38 - "CargoInfo"
Cohesion: 0.29
Nodes (17): CargoInfo, CargoChangesPreview(), CargoEditConfirmDialog(), CargoEditConfirmHeader(), CargoMainInfo(), CargoShippingInfo(), CargoTimeInfo(), CargoWeightInfo() (+9 more)

### Community 39 - "QuotaPercentageDialogSection.kt"
Cohesion: 0.20
Nodes (17): CalculationResult, AnimatedNumber(), Color, ImageVector, Modifier, lerp(), PercentageDisplay(), PercentageInputTab() (+9 more)

### Community 40 - "secrets.cpp"
Cohesion: 0.41
Nodes (17): decryptXor(), Java_com_atk_atk_1cargo_api_Secrets_getApiKey(), Java_com_atk_atk_1cargo_api_Secrets_getAuthenticationX365(), Java_com_atk_atk_1cargo_api_Secrets_getAuthUser(), Java_com_atk_atk_1cargo_api_Secrets_getBaseUrl(), Java_com_atk_atk_1cargo_api_Secrets_getExpectedSignatureHash(), Java_com_atk_atk_1cargo_api_Secrets_getLicenseCheckUrl(), Java_com_atk_atk_1cargo_api_Secrets_getLicenseInfoUrl() (+9 more)

### Community 41 - "CargoModels.kt"
Cohesion: 0.12
Nodes (11): CargoDeleteResponse, CargoInfoRequest, CargoInfoSearch, CargoSearchResponse, CargoStats, CheckExistenceResponse, ExistingCargo, LoadableTonnageResponse (+3 more)

### Community 42 - "LoadingNotificationService"
Cohesion: 0.22
Nodes (6): Context, Intent, KoinComponent, LoadingNotificationService, IBinder, Service

### Community 43 - "QuotaAnalysisSection.kt"
Cohesion: 0.28
Nodes (16): QuotaCompletionData, QuotaGroupingMode, BY_CARGO_OWNER, BY_CARRIER, BY_SHIP, AnalyticsGroupingModeButton(), AnalyticsOwnerSummaryCard(), AnalyticsQuotaCard() (+8 more)

### Community 44 - "RealTimeLoadingBottomSheet"
Cohesion: 0.21
Nodes (15): ShiftInfo, DialogHeader(), Color, Modifier, RealTimeLoadingBottomSheet(), RealTimeShiftNavigation(), RefreshOverlay(), ShipCard() (+7 more)

### Community 45 - "AuthModels.kt"
Cohesion: 0.14
Nodes (9): ActiveSessionResponse, ForceLogoutResponse, LogoutRequest, LogoutResponse, PasswordCheckResponse, PermissionSyncRequest, PermissionSyncResponse, Result (+1 more)

### Community 47 - "ChatMessageEntity"
Cohesion: 0.16
Nodes (3): ChatDao, Flow, ChatMessageEntity

### Community 49 - "ActiveShipInfo"
Cohesion: 0.32
Nodes (15): ActiveShipInfo, CargoSnackbarMessage, GroupedShipList(), Color, Modifier, QuotaCard(), ShipGroup(), ShipHeader() (+7 more)

### Community 50 - "MessageBubble.kt"
Cohesion: 0.24
Nodes (14): extractShipInfoAndText(), com, replaceUsernamesWithFullNames(), rotateIcon(), ShipInfoModel, ChatInfoRow(), EditMessageDialog(), androidx (+6 more)

### Community 51 - "composer.json"
Cohesion: 0.12
Nodes (15): autoload, autoload-dev, psr-4, psr-4, config, optimize-autoloader, sort-packages, description (+7 more)

### Community 53 - "ChatToolbar.kt"
Cohesion: 0.25
Nodes (13): ColorWheel(), Color, Modifier, getAdaptiveBubbleColor(), AdvancedColorPickerDialog(), ChatSettingsDialogEnhanced(), ChatSettingsPreviewRefined(), Color (+5 more)

### Community 54 - "UserManagementScreen.kt"
Cohesion: 0.25
Nodes (14): DeleteUserRequest, ForceLogoutRequest, DashboardSummaryRow(), EmptyStateView(), EnterpriseUserCard(), androidx, Color, RoleFilterChipRow() (+6 more)

### Community 55 - "StartupViewModel.kt"
Cohesion: 0.22
Nodes (7): AndroidViewModel, StateFlow, RequestBatteryOptimization, ShowMessage, StartupEvent, SessionCheckRequest, SharedFlow

### Community 56 - "FontWeight"
Cohesion: 0.20
Nodes (7): SplashScreen(), SplashScreenConstants, SplashScreenContent(), SplashSubtitleBadge(), SplashVersionBadge(), Brush, FontWeight

### Community 57 - "SecurityScreen.kt"
Cohesion: 0.27
Nodes (12): AdvancedOrbitalScanner(), AnimatedUpdateIcon(), AnimatedWarningIcon(), DynamicPremiumBackground(), InteractiveExitButton(), Modifier, RetryButton(), SecurityBlockScreen() (+4 more)

### Community 59 - "ChatRepository"
Cohesion: 0.22
Nodes (3): ChatRepository, com, Result

### Community 60 - "CargoDetailsDialogSection.kt"
Cohesion: 0.31
Nodes (13): CargoDetailsDialog(), Color, ImageVector, Modifier, ModernCargoDetailsGrid(), ModernCargoStatusSection(), ModernCargoStatusTimeline(), ModernDialogActions() (+5 more)

### Community 62 - "RetrofitClient"
Cohesion: 0.26
Nodes (9): FloatTypeAdapter, Context, RetrofitClient, ThirdPartyRetrofitClient, JsonReader, JsonWriter, OkHttpClient, Retrofit (+1 more)

### Community 63 - "CargoInfoDetailsDialogSection.kt"
Cohesion: 0.35
Nodes (12): CargoDetailsPalette, CargoDetailTabItem(), CargoInfoDetailsDialog(), DeleteDialog(), DetailInfoRow(), ImageVector, Modifier, SnackbarHostState (+4 more)

### Community 64 - "ChatScreen.kt"
Cohesion: 0.27
Nodes (12): ChatUiItem, getChatBackgroundColor(), getDateHeaderColor(), Header, Color, Message, ChatScreen(), DateHeader() (+4 more)

### Community 65 - "CoroutineScope"
Cohesion: 0.27
Nodes (8): AtkCargoApplication, BootReceiver, BroadcastReceiver, Context, Intent, KoinComponent, Application, CoroutineScope

### Community 66 - "UserManagementDialogsSection.kt"
Cohesion: 0.36
Nodes (11): CreateUserRequest, UserTypeInfo, DesignTextField(), EnhancedAddUserDialog(), EnhancedEditUserDialog(), getUserTypes(), androidx, KeyboardOptions (+3 more)

### Community 67 - "MainActivity.kt"
Cohesion: 0.32
Nodes (5): ServerSyncingScreen(), Intent, MainActivity, Bundle, ComponentActivity

### Community 70 - "ChatRepository.kt"
Cohesion: 0.22
Nodes (6): ChatMessage, ChatMessagesResponse, SendMessageRequest, SendMessageResponse, UnreadCountResponse, Flow

### Community 71 - "QuotaSelectionDialog.kt"
Cohesion: 0.42
Nodes (10): MatchingQuota, DialogHeader(), findDifferentFields(), InactiveIndicator(), ImageVector, QuotaDetailItem(), QuotaDetails(), QuotaHeader() (+2 more)

### Community 72 - "SearchDialogs.kt"
Cohesion: 0.29
Nodes (10): CargoSearchResultCard(), InfoRowCompact(), Color, Context, ImageVector, MultipleSearchResultDialog(), SearchType, RECEIPT_NUMBER (+2 more)

### Community 74 - "User"
Cohesion: 0.29
Nodes (7): UpdateUserRequest, User, EnhancedDeleteConfirmationDialog(), EnhancedForceLogoutDialog(), getUserTypeDisplay(), sortUsersByType(), ProfileSettingsDialog()

### Community 75 - "ProfileMenu.kt"
Cohesion: 0.31
Nodes (8): ThemeColorOption, ThemeColorPickerRow(), ActionButtons(), getUserTypeDisplay(), Color, ProfileMenu(), ProfilePalette, rememberProfilePalette()

### Community 76 - "CryptoManager"
Cohesion: 0.38
Nodes (4): CryptoManager, ByteArray, Cipher, SecretKey

### Community 78 - "PermissionPoller"
Cohesion: 0.36
Nodes (3): Job, StateFlow, PermissionPoller

### Community 79 - "ReportsRepository.kt"
Cohesion: 0.20
Nodes (4): CargoInfoResponse, ComprehensiveAnalysisResponse, QuotaDetails, QuotaStatusResponse

### Community 80 - "formatNumber"
Cohesion: 0.14
Nodes (27): Quota, addOneDayToPersianDate(), buildQuotasShareText(), format(), formatHoursToPersian(), formatNumber(), formatWeightWithDetail(), getDaysInPersianMonth() (+19 more)

### Community 82 - "ApiResponse"
Cohesion: 0.29
Nodes (3): ApiResponse, DeleteMessageRequest, EditMessageRequest

### Community 83 - "QuotaValidationUseCase.kt"
Cohesion: 0.36
Nodes (4): QuotaValidationResult, InputValidationResult, QuotaValidationUseCase, TempTonnageValidationResult

### Community 84 - "CargoCounterOperationScreen"
Cohesion: 0.36
Nodes (6): CargoCounterRoute, cargoCounterScreen(), NavController, navigateToCargoCounter(), CargoCounterOperationScreen(), NavController

### Community 85 - "QuotaEntryDialog.kt"
Cohesion: 0.57
Nodes (7): FocusRequester, QuotaEntryActions(), QuotaEntryContent(), QuotaEntryDialog(), QuotaEntryPalette, rememberQuotaEntryPalette(), validateAndSubmit()

### Community 86 - "ComprehensiveAnalyticsDialog.kt"
Cohesion: 0.57
Nodes (6): AnalyticsDateNavigation(), AnalyticsHeaderCard(), ComprehensiveAnalyticsDialog(), EmptyStateCard(), ErrorStateCard(), Modifier

### Community 87 - "CargoViewModel.kt"
Cohesion: 0.33
Nodes (5): CargoViewModelFactory, StateFlow, T, ViewModel, ViewModelProvider

### Community 89 - "ChatNotificationWorker.kt"
Cohesion: 0.48
Nodes (4): ChatNotificationWorker, KoinComponent, CoroutineWorker, ListenableWorker

### Community 90 - "proxy_generator.php"
Cohesion: 0.57
Nodes (6): _o1(), _o2(), _o3(), _o4(), _o5(), _o6()

### Community 92 - "NotificationActionReceiver.kt"
Cohesion: 0.53
Nodes (4): BroadcastReceiver, Context, Intent, NotificationActionReceiver

### Community 94 - "StartupState"
Cohesion: 0.33
Nodes (6): Ready, SecurityBlocked, Splash, StartupState, Syncing, VersionExpired

### Community 95 - "AppDatabase"
Cohesion: 0.47
Nodes (3): AppDatabase, Context, RoomDatabase

### Community 96 - "MessageInputArea"
Cohesion: 0.67
Nodes (5): Color, com, MessageInputArea(), QuotaSelectionDialog(), ShipSelectionDialog()

### Community 97 - "HomeNotificationSettingRow.kt"
Cohesion: 0.67
Nodes (5): CompactSwitch(), Color, ImageVector, Modifier, NotificationSettingRow()

### Community 98 - "LoadingState"
Cohesion: 0.47
Nodes (6): Error, Idle, Loading, LoadingState, Success, UiState

### Community 100 - "ChatNavigation.kt"
Cohesion: 0.50
Nodes (4): AdminChatRoute, adminChatScreen(), NavController, navigateToAdminChat()

### Community 101 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 102 - "WarehouseDateTimePicker.kt"
Cohesion: 0.52
Nodes (6): persianDateFormat(), DateTimePicker(), Modifier, PersianDateItem(), TimePickerDialog(), TimePickerState

### Community 108 - "QuotasListDialogs.kt"
Cohesion: 0.53
Nodes (5): DeleteQuotaDialog(), androidx, Color, QuotaConfirmDialog(), ToggleQuotaStatusDialog()

### Community 154 - "AdvancedSearchDialog.kt"
Cohesion: 0.70
Nodes (4): AdvancedSearchDialog(), Modifier, SearchTabInfo, SearchTypeTabRow()

### Community 156 - "ExitStatusDialog.kt"
Cohesion: 0.83
Nodes (3): AnimatedCounter(), ExitStatusDialog(), ImageVector

## Knowledge Gaps
- **91 isolated node(s):** `ConflictSession`, `Error`, `Success`, `Error`, `Idle` (+86 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **30 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReportsViewModel` connect `ReportsViewModel` to `LoginScreen.kt`, `ManageReportsScreen.kt`, `WarehouseDetailsScreen.kt`, `ExportPdfUseCase`, `LoadingState`, `CargoInfo`, `QuotaAnalysisSection.kt`, `QuotasListScreen.kt`, `Ship`, `RealTimeLoadingBottomSheet`, `ReportsRepository.kt`, `formatNumber`, `QuotaManagementDialog.kt`, `QuotaWarningDialog.kt`, `ReportModels.kt`, `ComprehensiveAnalyticsDialog.kt`, `SelectInfoScreen.kt`, `ApiService`?**
  _High betweenness centrality (0.104) - this node is a cross-community bridge._
- **Why does `CargoViewModel` connect `CargoViewModel` to `InitialInfoScreen.kt`, `LoginScreen.kt`, `ManageReportsScreen.kt`, `MessageType`, `CargoInfo`, `CargoDetailsScreen.kt`, `ActiveShipInfo`, `QuotaValidationUseCase.kt`, `CargoCounterOperationScreen`, `CargoViewModel.kt`, `SelectInfoScreen.kt`, `QuotaExistenceMultipleResponse`, `CargoInfoDetailsDialogSection.kt`?**
  _High betweenness centrality (0.049) - this node is a cross-community bridge._
- **Why does `UpdateManager` connect `UpdateManager` to `LoginScreen.kt`, `StartupViewModel`, `MainActivity.kt`, `StartupViewModel.kt`?**
  _High betweenness centrality (0.034) - this node is a cross-community bridge._
- **What connects `ConflictSession`, `Error`, `Success` to the rest of the system?**
  _91 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `InitialInfoScreen.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.08200290275761973 - nodes in this community are weakly interconnected._
- **Should `LoginScreen.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.055811571940604196 - nodes in this community are weakly interconnected._
- **Should `ManageReportsScreen.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.05365686944634313 - nodes in this community are weakly interconnected._