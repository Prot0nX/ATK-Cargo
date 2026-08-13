# Graph Report - .  (2026-08-13)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 2289 nodes · 4795 edges · 229 communities (133 shown, 96 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 149 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `0315bd63`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- InitialInfoScreen.kt
- Dp
- UsersManager
- UpdateManager
- Exception
- ClassLoader
- CargoViewModel
- CargoDetailsScreen.kt
- ManageReportsScreen.kt
- Color.kt
- AppApiController
- SecurityVerifier
- ReportsViewModel
- QuotaManagementDialog.kt
- ReportsCommonWidgets.kt
- CargoRepository
- StartupViewModel
- SelectInfoScreen.kt
- ReportModels.kt
- ApiService
- QuotasListScreen.kt
- CameraSection.kt
- InputValidator
- ChatRepository
- CargoModels.kt
- ShipInfoSection.kt
- mysqli
- AuthController
- ChatViewModel
- ExportPdfUseCase
- ChatController
- UserPreferencesManager
- AppModule.kt
- UserManagementDialogsSection.kt
- CargoInfo
- LoginScreen.kt
- CargoCounterScreen.kt
- UserManagementScreen.kt
- ATKCargoTheme
- LoadingNotificationService
- ShipCard.kt
- MessageBubble.kt
- AnalyticsController
- secrets.cpp
- QuotaWarningDialog.kt
- QuotaAnalysisSection.kt
- CargoController
- Logger
- ActiveShipInfo
- QuotaPercentageDialogSection.kt
- ChatScreen.kt
- composer.json
- SessionManager
- SessionManager
- RetrofitClient.kt
- AppNotificationManager
- ChatDao
- AuthModels.kt
- ReportsViewModel.kt
- CargoDetailsDialogSection.kt
- Database
- SessionRepository
- ChatToolbar.kt
- CargoViewModel.kt
- CargoInfoDetailsDialogSection.kt
- RegisterCargoDialogs.kt
- ShipDetailsScreen.kt
- FontWeight
- Request
- CoroutineScope
- Composable
- formatNumber
- ProtectedProxy
- QuotaSelectionDialog.kt
- MessageType
- sortShips
- SearchDialogs.kt
- SessionService
- ApiService.kt
- CryptoManager
- PDO
- jdate
- UserRepository
- ApiResponse
- QuotaEntryDialog.kt
- BootReceiver.kt
- DateRangePicker.kt
- ShipSortingMode
- LoginScreen
- ChatNotificationWorker.kt
- Config
- PasswordGateService
- SelectInfoSnackbar.kt
- ChatInputBar.kt
- proxy_generator.php
- NotificationActionReceiver.kt
- NavRoutes
- StartupState
- AppDatabase
- AnimationManager
- ColorScheme.kt
- LoadingState
- JalaliDateUtils
- Response
- StatisticsCard.kt
- LogoutUseCase.kt
- ColorSelector
- CargoRegistrationNavigation.kt
- gradlew
- .getActiveDeviceId
- .saveOrUpdateCargoInfo
- .clearMutedShips
- EmptyState.kt
- config.php
- PermissionService
- ComposerAutoloaderInit5f285832cbbfb0bffdb861048ac414db
- ExampleInstrumentedTest
- .checkExistence
- .checkLogin
- .checkSession
- .deleteCargo
- .forceLogoutUser
- .getAllUsers
- .logout
- .sendChatMessage
- .syncPermissions
- Constants
- ViewMode
- HardwarePerformanceEvaluator
- ComponentDefaults.kt
- ExampleUnitTest
- BaselineProfileGenerator
- .checkScaleReceiptNumber
- .getCargoInfoByReceiptNumber
- .getCargoInfoByTrackingNumber
- .getChatMessages
- .getLoadableTonnage
- .getUnreadChatCount
- App\Exceptions\ConflictException
- ActiveShipInfo
- Quota
- QuotaItem
- Response
- Ship
- Warehouse
- Color
- Job
- UpdateInfo
- ViewModel
- T
- ViewModelProvider
- T
- AndroidViewModel
- Exception
- Result
- Composable
- CargoInfo
- InitialInfo
- Dp
- ReportsViewModel
- KeyboardType
- ReportsViewModel
- NavController
- CargoInfo
- InitialInfo
- AndroidViewModel
- Job
- StateFlow
- ComprehensiveAnalysisResponse
- FilteredSummaryResponse
- MatchingQuota
- MessageType
- PDO
- Logger
- Logger
- QuotaCompletionData
- QuotaGroupingMode
- QuotaStatusResponse
- QuotaValidationResult
- RealTimeDataResponse
- ReportsViewModel
- SharedFlow
- ShipsData
- StartupViewModel
- ThirdPartyOrderRequest
- ThirdPartyOrderResponse
- ViewModelProvider
- WarehouseQuotaGroupingMode

## God Nodes (most connected - your core abstractions)
1. `ReportsViewModel` - 80 edges
2. `CargoViewModel` - 80 edges
3. `ApiService` - 58 edges
4. `AppApiController` - 41 edges
5. `CargoInfo` - 40 edges
6. `formatNumber()` - 39 edges
7. `UpdateManager` - 37 edges
8. `UserPreferencesManager` - 32 edges
9. `StartupViewModel` - 31 edges
10. `ReportsRepository` - 29 edges

## Surprising Connections (you probably didn't know these)
- `RegisterCargoScreen()` --calls--> `DuplicateConfirmationDialog()`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_registration/presentation/RegisterCargoScreen.kt → app/src/main/java/com/atk/atk_cargo/feature/cargo_registration/presentation/components/RegisterCargoDialogs.kt
- `validateServerSession()` --calls--> `SessionCheckRequest`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/api/SessionValidator.kt → app/src/main/java/com/atk/atk_cargo/data/model/AuthModels.kt
- `CargoCounterScreen()` --calls--> `ColorSelector`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_counter/presentation/CargoCounterScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt
- `SelectInfoScreenContent()` --calls--> `ColorSelector`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/SelectInfoScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt
- `QuotasDialog()` --calls--> `ColorSelector`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/reports/presentation/quota_details/QuotaDetailsScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt

## Import Cycles
- None detected.

## Communities (229 total, 96 thin omitted)

### Community 0 - "InitialInfoScreen.kt"
Cohesion: 0.05
Nodes (59): Job, StateFlow, PermissionPoller, MainScreen(), RouteTransitions, standardTransitions(), PermissionSyncRequest, CheckExistenceRequest (+51 more)

### Community 1 - "Dp"
Cohesion: 0.06
Nodes (45): ActiveQuotasDialog(), FilterBar(), Color, Modifier, QuotasHeader(), StatItem(), VerticalDivider(), ActiveShipInfo (+37 more)

### Community 2 - "UsersManager"
Cohesion: 0.07
Nodes (7): ApiManager, CONFIG, ModalManager, ThemeManager, UIManager, UsersManager, Utils

### Community 3 - "UpdateManager"
Cohesion: 0.06
Nodes (29): adjustColorForTheme(), FabItem, UpdateInfo, Completed, Downloading, DownloadProgress, DownloadState, Error (+21 more)

### Community 4 - "Exception"
Cohesion: 0.08
Nodes (13): ComprehensiveAnalysisResponse, Ship, ErrorResponse, HttpStatusException, CargoInfo, CargoInfoResponse, SaveOrUpdateResponse, ReportsRepository (+5 more)

### Community 5 - "ClassLoader"
Cohesion: 0.05
Nodes (4): Composer\Semver\VersionParser, ComposerStaticInit5f285832cbbfb0bffdb861048ac414db, ClassLoader, InstalledVersions

### Community 7 - "CargoDetailsScreen.kt"
Cohesion: 0.09
Nodes (39): Result, UserPreferencesManager, validateServerSession(), Modifier, StatusSnackbar(), CargoDetailsScreen(), confirmCargo(), FloatingActionButtonItem() (+31 more)

### Community 8 - "ManageReportsScreen.kt"
Cohesion: 0.09
Nodes (39): ShiftInfo, AdvancedSearchDialog(), Modifier, SearchTabInfo, SearchTypeTabRow(), AnalyticsDateNavigation(), AnalyticsHeaderCard(), ComprehensiveAnalyticsDialog() (+31 more)

### Community 9 - "Color.kt"
Cohesion: 0.09
Nodes (38): UserPreferencesManager, ThemeColorOption, ThemeColorPickerRow(), persianDateFormat(), DateTimePicker(), Modifier, PersianDateItem(), TimePickerDialog() (+30 more)

### Community 11 - "SecurityVerifier"
Cohesion: 0.09
Nodes (11): Secrets, SecurityErrorType, LICENSE_INACTIVE, LICENSE_NOT_FOUND, NETWORK_ERROR, TAMPERED, UNKNOWN_ERROR, SecurityVerifier (+3 more)

### Community 12 - "ReportsViewModel"
Cohesion: 0.07
Nodes (9): ComprehensiveAnalytics, GroupSortingMode, ALPHABETICAL, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC, QuotaSortingMode, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC (+1 more)

### Community 13 - "QuotaManagementDialog.kt"
Cohesion: 0.16
Nodes (31): AdvancedFiltersDialog(), AnalyticsStatChipMini(), CompactStatChip(), EmptyQuotaState(), FilterOptionsCard(), InfoChip(), IntegratedQuotaCard(), Color (+23 more)

### Community 14 - "ReportsCommonWidgets.kt"
Cohesion: 0.14
Nodes (28): ApiQuotaDetails, adjustColorForTheme(), Color, calculatePercentage(), calculateProgress(), CompactStatChip(), EmptyQuotaState(), EmptyShipsState() (+20 more)

### Community 15 - "CargoRepository"
Cohesion: 0.08
Nodes (3): InvalidArgumentException, CargoRepository, CargoService

### Community 16 - "StartupViewModel"
Cohesion: 0.10
Nodes (11): AndroidViewModel, Flow, Intent, StateFlow, RequestBatteryOptimization, SecurityCheckState, ShowMessage, StartupEvent (+3 more)

### Community 17 - "SelectInfoScreen.kt"
Cohesion: 0.15
Nodes (27): formatNumber(), RealTimeLoadingData, EmptySearchResult(), extractLastDigits(), FilterState, ALL, COMPLETED, PENDING (+19 more)

### Community 18 - "ReportModels.kt"
Cohesion: 0.11
Nodes (14): AnalyticsData, DateInfo, ExistingQuota, ExitDateInfo, FilteredSummary, FilteredSummaryResponse, QuotaCompletionAnalysis, QuotaDetails (+6 more)

### Community 19 - "ApiService"
Cohesion: 0.14
Nodes (7): ApiService, CreateUserRequest, InitialInfo, PasswordCheckResponse, QuotaExistenceMultipleResponse, Response, SuccessResponse

### Community 20 - "QuotasListScreen.kt"
Cohesion: 0.19
Nodes (27): Quota, QuotaEditData, QuotaPercentageData, shareQuotasData(), DialogHeader(), QuotaPercentageDialog(), ActionButton(), calculateValues() (+19 more)

### Community 21 - "CameraSection.kt"
Cohesion: 0.15
Nodes (20): adaptiveThresholding(), applyContrastEnhancement(), enhanceEdges(), ImageProxy, InputImage, medianFilter(), preprocessImage(), EnhancedNumberAnalyzer (+12 more)

### Community 22 - "InputValidator"
Cohesion: 0.09
Nodes (3): UserController, UserService, InputValidator

### Community 23 - "ChatRepository"
Cohesion: 0.12
Nodes (12): ChatMessage, ChatMessagesResponse, SendMessageRequest, SendMessageResponse, UnreadCountResponse, DeleteMessageRequest, EditMessageRequest, ChatMessageEntity (+4 more)

### Community 24 - "CargoModels.kt"
Cohesion: 0.10
Nodes (17): CargoInfoResponse, CargoInfoSearch, CargoSearchResponse, CargoStats, CheckExistenceResponse, ExistingCargo, InitialInfo, LoadableTonnageResponse (+9 more)

### Community 25 - "ShipInfoSection.kt"
Cohesion: 0.16
Nodes (21): ShipInfo, CargoOperationScreen(), NavController, formatNumber(), toEnglishNumbers(), CargoInfoRow(), DetailedInfoGrid(), DetailInfoItem() (+13 more)

### Community 26 - "mysqli"
Cohesion: 0.14
Nodes (8): App\Core\AuthenticatesRequests, App\Core\Database, App\Core\Request, App\Services\PasswordGateService, mysqli, LicenseController, UtilityController, SessionManager

### Community 27 - "AuthController"
Cohesion: 0.14
Nodes (14): App\Core\DatabaseManager, App\Core\Logger, App\Core\MicroCache, App\Core\Response, App\Exceptions\ApiException, App\Repositories\UserRepository, App\Services\PermissionService, App\Services\SessionService (+6 more)

### Community 28 - "ChatViewModel"
Cohesion: 0.10
Nodes (7): ChatViewModel, ChatViewModelFactory, com, StateFlow, T, ViewModel, ViewModelProvider

### Community 29 - "ExportPdfUseCase"
Cohesion: 0.26
Nodes (11): ExportPdfUseCase, FilteredSummary, PdfColorScheme, PdfFonts, PersianFontManager, ShamsiDate, Document, Font (+3 more)

### Community 30 - "ChatController"
Cohesion: 0.19
Nodes (3): mysqli_stmt, ChatController, DatabaseManager

### Community 31 - "UserPreferencesManager"
Cohesion: 0.11
Nodes (5): Flow, UserPreferencesManager, DataStore, Preferences, T

### Community 32 - "AppModule.kt"
Cohesion: 0.13
Nodes (10): AuthRepository, ConflictSession, Error, Flow, LoginResult, Success, LoginUseCase, AuthViewModel (+2 more)

### Community 33 - "UserManagementDialogsSection.kt"
Cohesion: 0.19
Nodes (15): CreateUserRequest, UpdateUserRequest, UserTypeInfo, DesignTextField(), EnhancedAddUserDialog(), EnhancedEditUserDialog(), getUserTypes(), androidx (+7 more)

### Community 34 - "CargoInfo"
Cohesion: 0.26
Nodes (19): CargoInfo, FormSection(), CargoChangesPreview(), CargoEditConfirmDialog(), CargoEditConfirmHeader(), CargoMainInfo(), CargoShippingInfo(), CargoTimeInfo() (+11 more)

### Community 35 - "LoginScreen.kt"
Cohesion: 0.20
Nodes (20): CompactLoginLayout(), ExpandedLoginLayout(), IndustrialBrandingHeaderContent(), IndustrialFeatureBadge(), IndustrialFooterInfo(), IndustrialInputField(), IndustrialLoginFormCard(), androidx (+12 more)

### Community 36 - "CargoCounterScreen.kt"
Cohesion: 0.20
Nodes (17): CargoCounterScreen(), CargoCounterViewModel, filterShipsByTab(), com, NavController, SnackbarHostState, StateFlow, ViewModel (+9 more)

### Community 37 - "UserManagementScreen.kt"
Cohesion: 0.19
Nodes (19): DeleteUserRequest, ForceLogoutRequest, User, EnhancedDeleteConfirmationDialog(), EnhancedForceLogoutDialog(), DashboardSummaryRow(), EmptyStateView(), EnterpriseUserCard() (+11 more)

### Community 38 - "ATKCargoTheme"
Cohesion: 0.14
Nodes (11): ComponentStyles, Dimensions, Elevation, ATKCargoTheme, Motion, SemanticColors, AppShapes, Spacing (+3 more)

### Community 39 - "LoadingNotificationService"
Cohesion: 0.21
Nodes (7): Context, Intent, KoinComponent, LoadingNotificationService, TypeToken, IBinder, Service

### Community 40 - "ShipCard.kt"
Cohesion: 0.22
Nodes (16): ErrorState(), ImageVector, Modifier, ImageVector, Modifier, Ship, ShipSortingMode, ShipCard() (+8 more)

### Community 41 - "MessageBubble.kt"
Cohesion: 0.22
Nodes (17): extractShipInfoAndText(), getAdaptiveBubbleColor(), getChatBackgroundColor(), getDateHeaderColor(), Color, com, replaceUsernamesWithFullNames(), ShipInfoModel (+9 more)

### Community 43 - "secrets.cpp"
Cohesion: 0.41
Nodes (17): decryptXor(), Java_com_atk_atk_1cargo_api_Secrets_getApiKey(), Java_com_atk_atk_1cargo_api_Secrets_getAuthenticationX365(), Java_com_atk_atk_1cargo_api_Secrets_getAuthUser(), Java_com_atk_atk_1cargo_api_Secrets_getBaseUrl(), Java_com_atk_atk_1cargo_api_Secrets_getExpectedSignatureHash(), Java_com_atk_atk_1cargo_api_Secrets_getLicenseCheckUrl(), Java_com_atk_atk_1cargo_api_Secrets_getLicenseInfoUrl() (+9 more)

### Community 44 - "QuotaWarningDialog.kt"
Cohesion: 0.25
Nodes (17): CompactInfoSection(), DialogHeader(), Color, ImageVector, Modifier, ReportsViewModel, WarningStatus, LoadingActionButton() (+9 more)

### Community 45 - "QuotaAnalysisSection.kt"
Cohesion: 0.30
Nodes (15): QuotaCompletionData, AnalyticsGroupingModeButton(), AnalyticsOwnerSummaryCard(), AnalyticsQuotaCard(), AnalyticsQuotaGroupExpansionPanel(), AnalyticsStatChip(), ImageVector, Modifier (+7 more)

### Community 47 - "Logger"
Cohesion: 0.18
Nodes (3): OnlineUsersController, Logger, self

### Community 48 - "ActiveShipInfo"
Cohesion: 0.32
Nodes (15): ActiveShipInfo, CargoSnackbarMessage, GroupedShipList(), Color, Modifier, QuotaCard(), ShipGroup(), ShipHeader() (+7 more)

### Community 49 - "QuotaPercentageDialogSection.kt"
Cohesion: 0.25
Nodes (15): CalculationResult, AnimatedNumber(), Color, ImageVector, Modifier, lerp(), PercentageDisplay(), PercentageInputTab() (+7 more)

### Community 50 - "ChatScreen.kt"
Cohesion: 0.20
Nodes (14): ChatUiItem, Header, Message, AdminChatRoute, adminChatScreen(), NavController, UserPreferencesManager, navigateToAdminChat() (+6 more)

### Community 51 - "composer.json"
Cohesion: 0.12
Nodes (15): autoload, autoload-dev, psr-4, psr-4, config, optimize-autoloader, sort-packages, description (+7 more)

### Community 54 - "RetrofitClient.kt"
Cohesion: 0.21
Nodes (11): ApiService, FloatTypeAdapter, Context, RetrofitClient, ThirdPartyRetrofitClient, JsonReader, JsonWriter, OkHttpClient (+3 more)

### Community 57 - "AuthModels.kt"
Cohesion: 0.17
Nodes (10): ActiveSessionResponse, ForceLogoutResponse, LoginRequest, LoginResponse, LogoutResponse, PasswordCheckResponse, PermissionSyncResponse, SessionResponse (+2 more)

### Community 58 - "ReportsViewModel.kt"
Cohesion: 0.15
Nodes (12): QuotaGroupingMode, BY_CARGO_OWNER, BY_CARRIER, BY_SHIP, ThirdPartyOrder, ThirdPartyOrderRequest, buildQuotaGroups(), QuotaGroup (+4 more)

### Community 59 - "CargoDetailsDialogSection.kt"
Cohesion: 0.32
Nodes (14): CargoDetailsDialog(), CargoInfo, Color, ImageVector, Modifier, ModernCargoDetailsGrid(), ModernCargoStatusSection(), ModernCargoStatusTimeline() (+6 more)

### Community 60 - "Database"
Cohesion: 0.14
Nodes (5): PDOException, Logger, Database, PDO, self

### Community 62 - "ChatToolbar.kt"
Cohesion: 0.26
Nodes (12): ColorWheel(), Color, Modifier, AdvancedColorPickerDialog(), ChatSettingsDialogEnhanced(), ChatSettingsPreviewRefined(), Color, ImageVector (+4 more)

### Community 63 - "CargoViewModel.kt"
Cohesion: 0.16
Nodes (9): CargoDeleteResponse, QuotaExistenceMultipleResponse, CheckQuotaUseCase, CargoViewModelFactory, Response, StateFlow, T, ViewModel (+1 more)

### Community 64 - "CargoInfoDetailsDialogSection.kt"
Cohesion: 0.32
Nodes (13): CargoInfoRequest, CargoDetailsPalette, CargoDetailTabItem(), CargoInfoDetailsDialog(), DeleteDialog(), DetailInfoRow(), ImageVector, Modifier (+5 more)

### Community 65 - "RegisterCargoDialogs.kt"
Cohesion: 0.21
Nodes (10): WarningStatus, AnimatedIcon(), DialogContent(), DialogPassword(), DuplicateConfirmationDialog(), FocusRequester, MessageDialog(), processScannedQuota() (+2 more)

### Community 66 - "ShipDetailsScreen.kt"
Cohesion: 0.30
Nodes (13): QuotaWarningThresholds, calculateWarningStatus(), ErrorStateCard(), ImageVector, Quota, ReportsViewModel, Ship, WarningStatus (+5 more)

### Community 67 - "FontWeight"
Cohesion: 0.22
Nodes (9): AnimatedCounter(), ExitStatusDialog(), ImageVector, DeleteQuotaDialog(), androidx, Color, QuotaConfirmDialog(), ToggleQuotaStatusDialog() (+1 more)

### Community 69 - "CoroutineScope"
Cohesion: 0.27
Nodes (9): AtkCargoApplication, ActionButtons(), getUserTypeDisplay(), Color, ProfileMenu(), ProfilePalette, rememberProfilePalette(), Application (+1 more)

### Community 70 - "Composable"
Cohesion: 0.21
Nodes (7): ConfirmationDialog(), ImageVector, Modifier, SearchBar(), createTypography(), Composable, Typography

### Community 71 - "formatNumber"
Cohesion: 0.33
Nodes (11): addOneDayToPersianDate(), buildQuotasShareText(), format(), formatHoursToPersian(), formatNumber(), formatWeightWithDetail(), getDaysInPersianMonth(), isPersianLeapYear() (+3 more)

### Community 73 - "QuotaSelectionDialog.kt"
Cohesion: 0.42
Nodes (10): MatchingQuota, DialogHeader(), findDifferentFields(), InactiveIndicator(), ImageVector, QuotaDetailItem(), QuotaDetails(), QuotaHeader() (+2 more)

### Community 74 - "MessageType"
Cohesion: 0.29
Nodes (6): MessageType, ERROR, SUCCESS, WARNING, CargoSnackbarQueue, StateFlow

### Community 75 - "sortShips"
Cohesion: 0.35
Nodes (4): Ship, ShipSortingMode, sortShips(), ReportsDomainTest

### Community 76 - "SearchDialogs.kt"
Cohesion: 0.29
Nodes (10): CargoSearchResultCard(), InfoRowCompact(), Color, Context, ImageVector, MultipleSearchResultDialog(), SearchType, RECEIPT_NUMBER (+2 more)

### Community 78 - "ApiService.kt"
Cohesion: 0.20
Nodes (5): ApiResponse2, ThirdPartyApiService, RealTimeDataResponse, ThirdPartyOrderResponse, JsonElement

### Community 79 - "CryptoManager"
Cohesion: 0.38
Nodes (4): CryptoManager, ByteArray, Cipher, SecretKey

### Community 81 - "jdate"
Cohesion: 0.53
Nodes (9): gregorian_to_jalali(), jalali_to_gregorian(), jcheckdate(), jdate(), jdate_words(), jgetdate(), jmktime(), jstrftime() (+1 more)

### Community 83 - "ApiResponse"
Cohesion: 0.22
Nodes (5): ApiResponse, DeleteMessageRequest, DeleteUserRequest, EditMessageRequest, UpdateUserRequest

### Community 84 - "QuotaEntryDialog.kt"
Cohesion: 0.53
Nodes (8): ActiveShipInfo, FocusRequester, QuotaEntryActions(), QuotaEntryContent(), QuotaEntryDialog(), QuotaEntryPalette, rememberQuotaEntryPalette(), validateAndSubmit()

### Community 85 - "BootReceiver.kt"
Cohesion: 0.39
Nodes (6): BootReceiver, BroadcastReceiver, Context, Intent, KoinComponent, UserPreferencesManager

### Community 86 - "DateRangePicker.kt"
Cohesion: 0.57
Nodes (7): DateClickableField(), DateRangePicker(), getDaysInPersianMonth(), isPersianLeapYear(), Color, Modifier, PersianDatePickerDialog()

### Community 87 - "ShipSortingMode"
Cohesion: 0.25
Nodes (7): ShipSortingMode, LOADED_TONNAGE_ASC, LOADED_TONNAGE_DESC, NAME_ASC, NAME_DESC, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC

### Community 88 - "LoginScreen"
Cohesion: 0.29
Nodes (7): LoginScreen(), AdaptiveLayoutConfig, rememberAdaptiveLayoutConfig(), WindowSizeClass, COMPACT, EXPANDED, MEDIUM

### Community 89 - "ChatNotificationWorker.kt"
Cohesion: 0.43
Nodes (5): ChatNotificationWorker, KoinComponent, UserPreferencesManager, CoroutineWorker, ListenableWorker

### Community 92 - "SelectInfoSnackbar.kt"
Cohesion: 0.57
Nodes (6): Color, Modifier, snackbarAccent(), snackbarAccentBg(), StatusSnackbar(), SnackbarMessage

### Community 93 - "ChatInputBar.kt"
Cohesion: 0.52
Nodes (6): rotateIcon(), Color, com, MessageInputArea(), QuotaSelectionDialog(), ShipSelectionDialog()

### Community 94 - "proxy_generator.php"
Cohesion: 0.57
Nodes (6): _o1(), _o2(), _o3(), _o4(), _o5(), _o6()

### Community 95 - "NotificationActionReceiver.kt"
Cohesion: 0.53
Nodes (4): BroadcastReceiver, Context, Intent, NotificationActionReceiver

### Community 97 - "StartupState"
Cohesion: 0.33
Nodes (6): Ready, SecurityBlocked, Splash, StartupState, Syncing, VersionExpired

### Community 98 - "AppDatabase"
Cohesion: 0.47
Nodes (3): AppDatabase, Context, RoomDatabase

### Community 100 - "ColorScheme.kt"
Cohesion: 0.80
Nodes (5): buildAppDarkColorScheme(), buildAppLightColorScheme(), compositeOver(), Color, ColorScheme

### Community 101 - "LoadingState"
Cohesion: 0.47
Nodes (6): Error, Idle, Loading, LoadingState, Success, UiState

### Community 104 - "StatisticsCard.kt"
Cohesion: 0.70
Nodes (4): Color, ImageVector, Modifier, StatisticsCard()

### Community 105 - "LogoutUseCase.kt"
Cohesion: 0.50
Nodes (3): LogoutRequest, Result, LogoutUseCase

### Community 106 - "ColorSelector"
Cohesion: 0.39
Nodes (3): adjustColorForTheme(), ColorSelector, Color

### Community 107 - "CargoRegistrationNavigation.kt"
Cohesion: 0.60
Nodes (4): CargoRegistrationRoute, cargoRegistrationScreen(), NavController, navigateToCargoRegistration()

### Community 108 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 112 - "EmptyState.kt"
Cohesion: 0.83
Nodes (3): EmptyState(), ImageVector, Modifier

### Community 127 - "ViewMode"
Cohesion: 0.67
Nodes (3): ViewMode, FLAT, GROUPED

### Community 228 - "WarehouseQuotaGroupingMode"
Cohesion: 0.40
Nodes (4): WarehouseQuotaGroupingMode, BY_CARGO_OWNER, BY_SHIPPING_COMPANY, BY_WAREHOUSE

## Knowledge Gaps
- **109 isolated node(s):** `SplashScreenConstants`, `Idle`, `RequestBatteryOptimization`, `AnalyticsData`, `DateInfo` (+104 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **96 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `validateServerSession()` connect `CargoDetailsScreen.kt` to `Exception`, `CargoCounterScreen.kt`, `ManageReportsScreen.kt`, `StartupViewModel`, `SelectInfoScreen.kt`?**
  _High betweenness centrality (0.090) - this node is a cross-community bridge._
- **Why does `CargoViewModel` connect `CargoViewModel` to `InitialInfoScreen.kt`, `AppModule.kt`, `CargoInfoDetailsDialogSection.kt`, `RegisterCargoDialogs.kt`, `CargoInfo`, `CargoDetailsScreen.kt`, `MessageType`, `CargoRegistrationNavigation.kt`, `ActiveShipInfo`, `SelectInfoScreen.kt`, `CargoModels.kt`, `ShipInfoSection.kt`, `CargoViewModel.kt`?**
  _High betweenness centrality (0.049) - this node is a cross-community bridge._
- **Why does `ApiService` connect `ApiService` to `.checkScaleReceiptNumber`, `.getCargoInfoByReceiptNumber`, `.getCargoInfoByTrackingNumber`, `.getChatMessages`, `.getLoadableTonnage`, `.getUnreadChatCount`, `ReportModels.kt`, `ChatRepository`, `CargoModels.kt`, `AuthModels.kt`, `CargoViewModel.kt`, `ApiService.kt`, `ApiResponse`, `LogoutUseCase.kt`, `.getActiveDeviceId`, `.saveOrUpdateCargoInfo`, `.checkExistence`, `.checkLogin`, `.checkSession`, `.deleteCargo`, `.forceLogoutUser`, `.getAllUsers`, `.logout`, `.sendChatMessage`, `.syncPermissions`?**
  _High betweenness centrality (0.045) - this node is a cross-community bridge._
- **What connects `SplashScreenConstants`, `Idle`, `RequestBatteryOptimization` to the rest of the system?**
  _109 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `InitialInfoScreen.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.05331510594668489 - nodes in this community are weakly interconnected._
- **Should `Dp` be split into smaller, more focused modules?**
  _Cohesion score 0.060814383923849816 - nodes in this community are weakly interconnected._
- **Should `UsersManager` be split into smaller, more focused modules?**
  _Cohesion score 0.06936026936026936 - nodes in this community are weakly interconnected._