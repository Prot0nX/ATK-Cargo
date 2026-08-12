# Graph Report - ATK-Cargo  (2026-08-12)

## Corpus Check
- 269 files · ~339,733 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2214 nodes · 4725 edges · 176 communities (143 shown, 33 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 81 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `9cd9bdcb`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ATKCargoTheme
- ManageReportsScreen.kt
- AppApiController
- CargoInfo
- UsersManager
- UpdateManager
- ApiService
- ReportsViewModel
- ClassLoader
- AuthController
- ReportModels.kt
- CargoViewModel
- SecurityVerifier
- QuotasListScreen.kt
- ReportsRepository
- ChatRepository
- ShipSortingMode
- QuotaManagementDialog.kt
- QuotaWarningDialog.kt
- preprocessImage
- CargoRepository
- CargoDetailsComponents.kt
- SelectInfoScreen.kt
- ExportPdfUseCase
- UserManagementScreen.kt
- Database
- Logger
- ChatViewModel
- UserPreferencesManager
- InitialInfoScreen.kt
- RegisterCargoScreen.kt
- ۴. باگ‌ها
- AnalyticsController
- MessageType
- What You Must Do When Invoked
- CargoCounterScreen.kt
- SessionService
- Color.kt
- AuthModels.kt
- LoginScreen.kt
- CargoDetailsScreen.kt
- StartupViewModel
- RealTimeLoadingBottomSheet
- QuotaDetailsScreen.kt
- secrets.cpp
- LoadingNotificationService
- HomeScreen.kt
- QuotaAnalysisSection.kt
- ChatMessageEntity
- ActiveShipInfo
- MessageBubble.kt
- composer.json
- SessionManager
- SessionManager
- ChatToolbar.kt
- ShipInfoSection.kt
- SecurityScreen.kt
- SessionRepository
- CargoRegistrationNavigation.kt
- hashPassword
- ChatController
- WarehouseDetailsScreen.kt
- AppModule.kt
- ChatScreen.kt
- Exception
- Request
- CoroutineScope
- AppNotificationManager
- BootReceiver.kt
- UserManagementDialogsSection.kt
- ActiveQuotasDialogSection.kt
- ProtectedProxy
- QuotaSelectionDialog.kt
- CargoModels.kt
- ProfileMenu.kt
- UpdateDialog.kt
- UtilityController
- ChatRepository.kt
- QuotaPercentageDialogSection.kt
- QuotaEntryDialog.kt
- Config
- ChatNotificationWorker.kt
- proxy_generator.php
- NotificationActionReceiver.kt
- NavRoutes
- ApiResponse
- AppDatabase
- AnimationManager
- MessageInputArea
- HomeNotificationSettingRow.kt
- LoadingState
- JalaliDateUtils
- DownloadState
- gradlew
- ComprehensiveAnalyticsDialog.kt
- MainScreen.kt
- config.php
- ComposerAutoloaderInit5f285832cbbfb0bffdb861048ac414db
- ExampleInstrumentedTest
- Constants
- ColorExtensions.kt
- HardwarePerformanceEvaluator
- ComponentDefaults.kt
- ExampleUnitTest
- BaselineProfileGenerator
- SplashScreen.kt
- UpdateManagerFactory
- FontWeight
- ReportsRepository.kt
- InitialInfo
- AuthViewModel
- ShipDetailsScreen.kt
- QuotaExistenceMultipleResponse
- formatNumber
- jdate
- PermissionPoller
- RealTimeLoadingData
- graphify reference: extra exports and benchmark
- ReportsNavigation.kt
- ColorSelector
- QuotasListDialogs.kt
- WarehouseQuotaGroupingMode
- ChatNavigation.kt
- graphify reference: query, path, explain
- QuotaSortingMode
- graphify reference: add a URL and watch a folder
- graphify reference: commit hook and native CLAUDE.md integration
- graphify reference: GitHub clone and cross-repo merge
- graphify reference: transcribe video and audio
- rules/graphify.md
- workflows/graphify.md
- CLAUDE.md
- .claude/CLAUDE.md
- extraction-spec.md

## God Nodes (most connected - your core abstractions)
1. `ReportsViewModel` - 105 edges
2. `CargoViewModel` - 84 edges
3. `CargoInfo` - 57 edges
4. `ApiService` - 56 edges
5. `UserPreferencesManager` - 46 edges
6. `formatNumber()` - 46 edges
7. `AppApiController` - 40 edges
8. `UpdateManager` - 38 edges
9. `Logger` - 35 edges
10. `ActiveShipInfo` - 35 edges

## Surprising Connections (you probably didn't know these)
- `validateServerSession()` --calls--> `SessionCheckRequest`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/api/SessionValidator.kt → app/src/main/java/com/atk/atk_cargo/data/model/AuthModels.kt
- `PersianDatePickerDialog()` --calls--> `FilterChip()`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/core/ui/components/DateRangePicker.kt → app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/SelectInfoScreen.kt
- `InitialInfoScreen()` --calls--> `InitialInfo`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/InitialInfoScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/CargoModels.kt
- `navigateToRegisterCargoActivity()` --calls--> `InitialInfo`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/SelectInfoScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/CargoModels.kt
- `CargoCounterScreen()` --calls--> `ColorSelector`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_counter/presentation/CargoCounterScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt

## Import Cycles
- None detected.

## Communities (176 total, 33 thin omitted)

### Community 0 - "ATKCargoTheme"
Cohesion: 0.09
Nodes (22): AdaptiveLayoutConfig, rememberAdaptiveLayoutConfig(), WindowSizeClass, COMPACT, EXPANDED, MEDIUM, buildAppDarkColorScheme(), buildAppLightColorScheme() (+14 more)

### Community 1 - "ManageReportsScreen.kt"
Cohesion: 0.08
Nodes (47): FabItem, DateClickableField(), DateRangePicker(), getDaysInPersianMonth(), isPersianLeapYear(), Color, Modifier, PersianDatePickerDialog() (+39 more)

### Community 2 - "AppApiController"
Cohesion: 0.08
Nodes (3): AppApiController, CargoController, MicroCache

### Community 3 - "CargoInfo"
Cohesion: 0.09
Nodes (50): CargoInfo, CargoInfoRequest, CargoDetailsDialog(), Color, ImageVector, Modifier, ModernCargoDetailsGrid(), ModernCargoStatusSection() (+42 more)

### Community 4 - "UsersManager"
Cohesion: 0.07
Nodes (7): ApiManager, CONFIG, ModalManager, ThemeManager, UIManager, UsersManager, Utils

### Community 5 - "UpdateManager"
Cohesion: 0.13
Nodes (8): UpdateInfo, DownloadProgress, Context, Job, StateFlow, ViewModel, UpdateManager, VersionCheckResult

### Community 6 - "ApiService"
Cohesion: 0.15
Nodes (3): ApiService, SuccessResponse, FilteredSummaryResponse

### Community 7 - "ReportsViewModel"
Cohesion: 0.09
Nodes (8): adjustColorForTheme(), ComprehensiveAnalytics, ThirdPartyOrder, AndroidViewModel, Color, Job, StateFlow, ReportsViewModel

### Community 8 - "ClassLoader"
Cohesion: 0.05
Nodes (4): Composer\Semver\VersionParser, ComposerStaticInit5f285832cbbfb0bffdb861048ac414db, ClassLoader, InstalledVersions

### Community 9 - "AuthController"
Cohesion: 0.06
Nodes (6): AuthController, UserController, Response, PermissionService, UserService, InputValidator

### Community 10 - "ReportModels.kt"
Cohesion: 0.12
Nodes (13): ThirdPartyApiService, AnalyticsData, DateInfo, ExistingQuota, ExitDateInfo, GroupSortingMode, ALPHABETICAL, REMAINING_TONNAGE_ASC (+5 more)

### Community 12 - "SecurityVerifier"
Cohesion: 0.09
Nodes (11): Secrets, SecurityErrorType, LICENSE_INACTIVE, LICENSE_NOT_FOUND, NETWORK_ERROR, TAMPERED, UNKNOWN_ERROR, SecurityVerifier (+3 more)

### Community 13 - "QuotasListScreen.kt"
Cohesion: 0.21
Nodes (24): Quota, QuotaEditData, buildQuotasShareText(), shareQuotasData(), ActionButton(), ConfirmationDialogHeader(), EditFieldBox(), EditFieldColumn() (+16 more)

### Community 14 - "ReportsRepository"
Cohesion: 0.16
Nodes (5): ErrorResponse, HttpStatusException, Exception, Result, ReportsRepository

### Community 15 - "ChatRepository"
Cohesion: 0.22
Nodes (3): ChatRepository, com, Result

### Community 16 - "ShipSortingMode"
Cohesion: 0.11
Nodes (22): ErrorState(), ImageVector, Modifier, ShipSortingMode, LOADED_TONNAGE_ASC, LOADED_TONNAGE_DESC, NAME_ASC, NAME_DESC (+14 more)

### Community 17 - "QuotaManagementDialog.kt"
Cohesion: 0.08
Nodes (41): FloatTypeAdapter, Context, RetrofitClient, ThirdPartyRetrofitClient, QuotaItem, AdvancedFiltersDialog(), AnalyticsStatChipMini(), CompactStatChip() (+33 more)

### Community 18 - "QuotaWarningDialog.kt"
Cohesion: 0.26
Nodes (16): WarningStatus, CompactInfoSection(), DialogHeader(), Color, ImageVector, Modifier, LoadingActionButton(), NavigationButton() (+8 more)

### Community 19 - "preprocessImage"
Cohesion: 0.13
Nodes (22): adaptiveThresholding(), applyContrastEnhancement(), enhanceEdges(), ImageProxy, InputImage, medianFilter(), preprocessImage(), EnhancedNumberAnalyzer (+14 more)

### Community 21 - "CargoDetailsComponents.kt"
Cohesion: 0.27
Nodes (13): CargoInfoCard(), CargoListSection(), GroupStats(), InfoChips(), InfoGrid(), InfoGridCard(), InfoGridItem(), InitialInfoSection() (+5 more)

### Community 22 - "SelectInfoScreen.kt"
Cohesion: 0.23
Nodes (17): formatNumber(), ShipSelectionDialog(), extractLastDigits(), FilterChip(), FlatQuotaCard(), GroupedShipList(), Color, Modifier (+9 more)

### Community 23 - "ExportPdfUseCase"
Cohesion: 0.10
Nodes (33): FilteredSummary, VoucherDetail, ExportPdfUseCase, PdfColorScheme, PdfFonts, PersianFontManager, ShamsiDate, persianDateFormat() (+25 more)

### Community 24 - "UserManagementScreen.kt"
Cohesion: 0.16
Nodes (21): DeleteUserRequest, ForceLogoutRequest, UpdateUserRequest, User, EnhancedDeleteConfirmationDialog(), EnhancedForceLogoutDialog(), DashboardSummaryRow(), EmptyStateView() (+13 more)

### Community 25 - "Database"
Cohesion: 0.15
Nodes (7): mysqli, PDO, PDOException, LicenseController, Database, PDO, self

### Community 26 - "Logger"
Cohesion: 0.13
Nodes (4): InvalidArgumentException, Logger, self, CargoService

### Community 27 - "ChatViewModel"
Cohesion: 0.10
Nodes (7): ChatViewModel, ChatViewModelFactory, com, StateFlow, T, ViewModel, ViewModelProvider

### Community 28 - "UserPreferencesManager"
Cohesion: 0.10
Nodes (7): Flow, T, UserPreferencesManager, ThemeColorOption, ThemeColorPickerRow(), DataStore, Preferences

### Community 29 - "InitialInfoScreen.kt"
Cohesion: 0.22
Nodes (22): CheckExistenceRequest, formatNumber(), isValidPersianText(), isValidQuotaNumber(), isValidShipName(), isValidWarehouseName(), isValidWeight(), ConfirmationDialog() (+14 more)

### Community 30 - "RegisterCargoScreen.kt"
Cohesion: 0.15
Nodes (26): refreshData(), CargoOperationScreen(), NavController, NavController, navigateToRegisterCargoActivity(), SelectInfoScreenContent(), StatusSnackbar(), AnimatedIcon() (+18 more)

### Community 31 - "۴. باگ‌ها"
Cohesion: 0.05
Nodes (41): 🔴 A-1 | نبود کامل احراز هویت روی endpoint (بحرانی), 🟠 A-2 | نبود هدرهای کنترل کش روی پاسخ, 🟠 A-3 | نبود اعتبارسنجی/کلمپ روی `shiftOffset`, 🟡 A-4 | Rate-limit مشترک و مبتنی بر IP, 🟡 A-5 | نشتی اطلاعات در پیام خطا, 🟡 B-10 | فیلد مرده در مدل, 🟡 B-11 | `LEFT JOIN` که در عمل `INNER JOIN` است, 🟡 B-12 | فیلتر `isActive` اعمال نمی‌شود (+33 more)

### Community 33 - "MessageType"
Cohesion: 0.16
Nodes (14): Modifier, StatusSnackbar(), MessageType, ERROR, SUCCESS, WARNING, CargoSnackbarQueue, StateFlow (+6 more)

### Community 34 - "What You Must Do When Invoked"
Cohesion: 0.07
Nodes (26): For /graphify add and --watch, For /graphify query, For the commit hook and native CLAUDE.md integration, For --update and --cluster-only, /graphify, Honesty Rules, Interpreter guard for subcommands, Part A - Structural extraction for code files (+18 more)

### Community 35 - "CargoCounterScreen.kt"
Cohesion: 0.19
Nodes (18): CargoCounterScreen(), CargoCounterViewModel, filterShipsByTab(), com, NavController, SnackbarHostState, StateFlow, ViewModel (+10 more)

### Community 37 - "Color.kt"
Cohesion: 0.26
Nodes (5): ServerSyncingScreen(), Intent, MainActivity, Bundle, ComponentActivity

### Community 38 - "AuthModels.kt"
Cohesion: 0.15
Nodes (8): ActiveSessionResponse, ForceLogoutResponse, LoginRequest, LoginResponse, PasswordCheckResponse, PermissionSyncRequest, PermissionSyncResponse, SessionResponse

### Community 39 - "LoginScreen.kt"
Cohesion: 0.16
Nodes (23): Modifier, SearchBar(), CompactLoginLayout(), ExpandedLoginLayout(), IndustrialBrandingHeaderContent(), IndustrialFeatureBadge(), IndustrialFooterInfo(), IndustrialInputField() (+15 more)

### Community 40 - "CargoDetailsScreen.kt"
Cohesion: 0.15
Nodes (17): Result, validateServerSession(), CargoCounterRoute, cargoCounterScreen(), NavController, navigateToCargoCounter(), CargoCounterOperationScreen(), NavController (+9 more)

### Community 41 - "StartupViewModel"
Cohesion: 0.09
Nodes (16): AndroidViewModel, Intent, StateFlow, Ready, RequestBatteryOptimization, SecurityBlocked, SecurityCheckState, ShowMessage (+8 more)

### Community 42 - "RealTimeLoadingBottomSheet"
Cohesion: 0.40
Nodes (9): ShiftInfo, DialogHeader(), Color, Modifier, RealTimeLoadingBottomSheet(), RealTimeShiftNavigation(), RefreshOverlay(), ShipCard() (+1 more)

### Community 43 - "QuotaDetailsScreen.kt"
Cohesion: 0.26
Nodes (13): ApiQuotaDetails, adjustColorForTheme(), Color, toTon(), calculatePercentage(), calculateProgress(), ProgressBar(), QuotaAdditionalInfo() (+5 more)

### Community 44 - "secrets.cpp"
Cohesion: 0.41
Nodes (17): decryptXor(), Java_com_atk_atk_1cargo_api_Secrets_getApiKey(), Java_com_atk_atk_1cargo_api_Secrets_getAuthenticationX365(), Java_com_atk_atk_1cargo_api_Secrets_getAuthUser(), Java_com_atk_atk_1cargo_api_Secrets_getBaseUrl(), Java_com_atk_atk_1cargo_api_Secrets_getExpectedSignatureHash(), Java_com_atk_atk_1cargo_api_Secrets_getLicenseCheckUrl(), Java_com_atk_atk_1cargo_api_Secrets_getLicenseInfoUrl() (+9 more)

### Community 45 - "LoadingNotificationService"
Cohesion: 0.19
Nodes (7): Context, Intent, KoinComponent, LoadingNotificationService, TypeToken, IBinder, Service

### Community 46 - "HomeScreen.kt"
Cohesion: 0.23
Nodes (13): MenuItem, getMenuItemsForUserType(), CategorizedMenuGrid(), CategoryHeader(), CompactMenuCard(), Header(), HomeScreen(), isWideCard() (+5 more)

### Community 47 - "QuotaAnalysisSection.kt"
Cohesion: 0.28
Nodes (16): QuotaCompletionData, QuotaGroupingMode, BY_CARGO_OWNER, BY_CARRIER, BY_SHIP, AnalyticsGroupingModeButton(), AnalyticsOwnerSummaryCard(), AnalyticsQuotaCard() (+8 more)

### Community 48 - "ChatMessageEntity"
Cohesion: 0.15
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

### Community 54 - "ChatToolbar.kt"
Cohesion: 0.22
Nodes (15): ColorWheel(), Color, Modifier, getAdaptiveBubbleColor(), getChatBackgroundColor(), Color, AdvancedColorPickerDialog(), ChatSettingsDialogEnhanced() (+7 more)

### Community 55 - "ShipInfoSection.kt"
Cohesion: 0.30
Nodes (13): ShipInfo, formatNumber(), toEnglishNumbers(), DetailedInfoGrid(), DetailInfoItem(), ExpandedContent(), Color, ImageVector (+5 more)

### Community 56 - "SecurityScreen.kt"
Cohesion: 0.27
Nodes (12): AdvancedOrbitalScanner(), AnimatedUpdateIcon(), AnimatedWarningIcon(), DynamicPremiumBackground(), InteractiveExitButton(), Modifier, RetryButton(), SecurityBlockScreen() (+4 more)

### Community 58 - "CargoRegistrationNavigation.kt"
Cohesion: 0.60
Nodes (4): CargoRegistrationRoute, cargoRegistrationScreen(), NavController, navigateToCargoRegistration()

### Community 59 - "hashPassword"
Cohesion: 0.23
Nodes (4): AuthRepositoryImpl, Flow, hashPassword(), SecurityUtilsTest

### Community 61 - "WarehouseDetailsScreen.kt"
Cohesion: 0.29
Nodes (11): Warehouse, ExportOptions(), Modifier, QuotaChip(), QuotaSelector(), VoucherDetailsButton(), WarehouseCard(), WarehouseContent() (+3 more)

### Community 62 - "AppModule.kt"
Cohesion: 0.13
Nodes (11): LogoutRequest, LogoutResponse, AuthRepository, ConflictSession, Error, Flow, LoginResult, Success (+3 more)

### Community 63 - "ChatScreen.kt"
Cohesion: 0.31
Nodes (10): ChatUiItem, getDateHeaderColor(), Header, Message, ChatScreen(), DateHeader(), EmptyState(), processMessagesForDisplay() (+2 more)

### Community 64 - "Exception"
Cohesion: 0.15
Nodes (5): Exception, mysqli_stmt, DatabaseManager, ApiException, DatabaseException

### Community 66 - "CoroutineScope"
Cohesion: 0.21
Nodes (8): AtkCargoApplication, CargoViewModelFactory, StateFlow, T, ViewModel, ViewModelProvider, Application, CoroutineScope

### Community 68 - "BootReceiver.kt"
Cohesion: 0.48
Nodes (5): BootReceiver, BroadcastReceiver, Context, Intent, KoinComponent

### Community 69 - "UserManagementDialogsSection.kt"
Cohesion: 0.36
Nodes (11): CreateUserRequest, UserTypeInfo, DesignTextField(), EnhancedAddUserDialog(), EnhancedEditUserDialog(), getUserTypes(), androidx, KeyboardOptions (+3 more)

### Community 70 - "ActiveQuotasDialogSection.kt"
Cohesion: 0.18
Nodes (18): ActiveQuotasDialog(), FilterBar(), Color, Dp, Modifier, QuotasHeader(), StatItem(), VerticalDivider() (+10 more)

### Community 72 - "QuotaSelectionDialog.kt"
Cohesion: 0.42
Nodes (10): MatchingQuota, DialogHeader(), findDifferentFields(), InactiveIndicator(), ImageVector, QuotaDetailItem(), QuotaDetails(), QuotaHeader() (+2 more)

### Community 73 - "CargoModels.kt"
Cohesion: 0.13
Nodes (9): CargoInfoSearch, CargoSearchResponse, CargoStats, CheckExistenceResponse, ExistingCargo, LoadableTonnageResponse, QuotaTonnageWarning, ScaleReceiptCheckResponse (+1 more)

### Community 74 - "ProfileMenu.kt"
Cohesion: 0.52
Nodes (6): ActionButtons(), getUserTypeDisplay(), Color, ProfileMenu(), ProfilePalette, rememberProfilePalette()

### Community 75 - "UpdateDialog.kt"
Cohesion: 0.33
Nodes (11): DownloadActionButtons(), DownloadingState(), DownloadProgressInfo(), DownloadSizeInfo(), DownloadSpeedInfo(), ErrorState(), ModernUpdateActionSection(), ModernUpdateContent() (+3 more)

### Community 77 - "ChatRepository.kt"
Cohesion: 0.22
Nodes (6): ChatMessage, ChatMessagesResponse, SendMessageRequest, SendMessageResponse, UnreadCountResponse, Flow

### Community 78 - "QuotaPercentageDialogSection.kt"
Cohesion: 0.17
Nodes (21): CalculationResult, QuotaPercentageData, AnimatedNumber(), DialogHeader(), Color, ImageVector, Modifier, lerp() (+13 more)

### Community 79 - "QuotaEntryDialog.kt"
Cohesion: 0.57
Nodes (7): FocusRequester, QuotaEntryActions(), QuotaEntryContent(), QuotaEntryDialog(), QuotaEntryPalette, rememberQuotaEntryPalette(), validateAndSubmit()

### Community 81 - "ChatNotificationWorker.kt"
Cohesion: 0.48
Nodes (4): ChatNotificationWorker, KoinComponent, CoroutineWorker, ListenableWorker

### Community 82 - "proxy_generator.php"
Cohesion: 0.57
Nodes (6): _o1(), _o2(), _o3(), _o4(), _o5(), _o6()

### Community 83 - "NotificationActionReceiver.kt"
Cohesion: 0.53
Nodes (4): BroadcastReceiver, Context, Intent, NotificationActionReceiver

### Community 85 - "ApiResponse"
Cohesion: 0.29
Nodes (3): ApiResponse, DeleteMessageRequest, EditMessageRequest

### Community 86 - "AppDatabase"
Cohesion: 0.47
Nodes (3): AppDatabase, Context, RoomDatabase

### Community 88 - "MessageInputArea"
Cohesion: 0.67
Nodes (5): Color, com, MessageInputArea(), QuotaSelectionDialog(), ShipSelectionDialog()

### Community 89 - "HomeNotificationSettingRow.kt"
Cohesion: 0.67
Nodes (5): CompactSwitch(), Color, ImageVector, Modifier, NotificationSettingRow()

### Community 90 - "LoadingState"
Cohesion: 0.47
Nodes (6): Error, Idle, Loading, LoadingState, Success, UiState

### Community 92 - "DownloadState"
Cohesion: 0.29
Nodes (6): Completed, Downloading, DownloadState, Error, Idle, Paused

### Community 93 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 94 - "ComprehensiveAnalyticsDialog.kt"
Cohesion: 0.57
Nodes (6): AnalyticsDateNavigation(), AnalyticsHeaderCard(), ComprehensiveAnalyticsDialog(), EmptyStateCard(), ErrorStateCard(), Modifier

### Community 95 - "MainScreen.kt"
Cohesion: 0.17
Nodes (15): MainScreen(), RouteTransitions, standardTransitions(), LoginScreen(), InitialInfoRoute, initialInfoScreen(), NavController, navigateToInitialInfo() (+7 more)

### Community 146 - "SplashScreen.kt"
Cohesion: 0.53
Nodes (5): SplashScreen(), SplashScreenConstants, SplashScreenContent(), SplashSubtitleBadge(), SplashVersionBadge()

### Community 147 - "UpdateManagerFactory"
Cohesion: 0.50
Nodes (3): T, ViewModelProvider, UpdateManagerFactory

### Community 148 - "FontWeight"
Cohesion: 0.15
Nodes (9): ConfirmationDialog(), ImageVector, EmptyState(), ImageVector, Modifier, AnimatedCounter(), ExitStatusDialog(), ImageVector (+1 more)

### Community 149 - "ReportsRepository.kt"
Cohesion: 0.14
Nodes (7): ApiResponse2, CargoInfoResponse, ComprehensiveAnalysisResponse, QuotaDetails, RealTimeDataResponse, ShipsData, JsonElement

### Community 150 - "InitialInfo"
Cohesion: 0.21
Nodes (6): InitialInfo, QuotaValidationResult, InputValidationResult, QuotaValidationUseCase, TempTonnageValidationResult, SubmitCargoUseCase

### Community 151 - "AuthViewModel"
Cohesion: 0.18
Nodes (6): AuthViewModel, StateFlow, ViewModel, For --cluster-only, For --update (incremental re-extraction), graphify reference: incremental update and cluster-only

### Community 153 - "ShipDetailsScreen.kt"
Cohesion: 0.30
Nodes (10): Ship, QuotaWarningThresholds, calculateWarningStatus(), ErrorStateCard(), ImageVector, ShipDetails(), ShipHeaderCard(), TabItem() (+2 more)

### Community 154 - "QuotaExistenceMultipleResponse"
Cohesion: 0.22
Nodes (3): QuotaExistenceMultipleResponse, QuotaStatusResponse, CheckQuotaUseCase

### Community 155 - "formatNumber"
Cohesion: 0.40
Nodes (9): addOneDayToPersianDate(), format(), formatHoursToPersian(), formatNumber(), formatWeightWithDetail(), getDaysInPersianMonth(), isPersianLeapYear(), Context (+1 more)

### Community 156 - "jdate"
Cohesion: 0.53
Nodes (9): gregorian_to_jalali(), jalali_to_gregorian(), jcheckdate(), jdate(), jdate_words(), jgetdate(), jmktime(), jstrftime() (+1 more)

### Community 157 - "PermissionPoller"
Cohesion: 0.36
Nodes (3): Job, StateFlow, PermissionPoller

### Community 158 - "RealTimeLoadingData"
Cohesion: 0.33
Nodes (7): RealTimeLoadingData, CompactInfo(), Color, ImageVector, Modifier, RealTimeLoadingCard(), StatisticItem()

### Community 159 - "graphify reference: extra exports and benchmark"
Cohesion: 0.22
Nodes (8): graphify reference: extra exports and benchmark, Step 6b - Wiki (only if --wiki flag), Step 7 - Neo4j export (only if --neo4j or --neo4j-push flag), Step 7a - FalkorDB export (only if --falkordb or --falkordb-push flag), Step 7b - SVG export (only if --svg flag), Step 7c - GraphML export (only if --graphml flag), Step 7d - MCP server (only if --mcp flag), Step 8 - Token reduction benchmark (only if total_words > 5000)

### Community 160 - "ReportsNavigation.kt"
Cohesion: 0.36
Nodes (7): CargoDetailsRoute, cargoDetailsScreen(), NavController, ManageShipsRoute, manageShipsScreen(), navigateToCargoDetails(), navigateToManageShips()

### Community 162 - "QuotasListDialogs.kt"
Cohesion: 0.53
Nodes (5): DeleteQuotaDialog(), androidx, Color, QuotaConfirmDialog(), ToggleQuotaStatusDialog()

### Community 163 - "WarehouseQuotaGroupingMode"
Cohesion: 0.40
Nodes (4): WarehouseQuotaGroupingMode, BY_CARGO_OWNER, BY_SHIPPING_COMPANY, BY_WAREHOUSE

### Community 164 - "ChatNavigation.kt"
Cohesion: 0.50
Nodes (4): AdminChatRoute, adminChatScreen(), NavController, navigateToAdminChat()

### Community 165 - "graphify reference: query, path, explain"
Cohesion: 0.40
Nodes (5): For /graphify explain, For /graphify path, graphify reference: query, path, explain, Step 0 — Constrained query expansion (REQUIRED before traversal), Step 1 — Traversal

### Community 166 - "QuotaSortingMode"
Cohesion: 0.50
Nodes (3): QuotaSortingMode, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC

### Community 167 - "graphify reference: add a URL and watch a folder"
Cohesion: 0.50
Nodes (3): For /graphify add, For --watch, graphify reference: add a URL and watch a folder

### Community 168 - "graphify reference: commit hook and native CLAUDE.md integration"
Cohesion: 0.50
Nodes (3): For git commit hook, For native CLAUDE.md integration, graphify reference: commit hook and native CLAUDE.md integration

## Knowledge Gaps
- **173 isolated node(s):** `CONFIG`, `Utils`, `name`, `description`, `type` (+168 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **33 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReportsViewModel` connect `ReportsViewModel` to `ManageReportsScreen.kt`, `CargoInfo`, `ReportModels.kt`, `QuotasListScreen.kt`, `ShipSortingMode`, `QuotaManagementDialog.kt`, `QuotaWarningDialog.kt`, `ReportsRepository.kt`, `ExportPdfUseCase`, `.showSnackbar`, `ShipDetailsScreen.kt`, `RealTimeLoadingData`, `ReportsNavigation.kt`, `ColorSelector`, `WarehouseQuotaGroupingMode`, `QuotaSortingMode`, `RealTimeLoadingBottomSheet`, `QuotaDetailsScreen.kt`, `QuotaAnalysisSection.kt`, `WarehouseDetailsScreen.kt`, `AppModule.kt`, `LoadingState`, `ComprehensiveAnalyticsDialog.kt`, `MainScreen.kt`?**
  _High betweenness centrality (0.068) - this node is a cross-community bridge._
- **Why does `ApiService` connect `ApiService` to `CargoInfo`, `AuthModels.kt`, `CargoModels.kt`, `ChatRepository.kt`, `ChatRepository`, `QuotaManagementDialog.kt`, `ReportsRepository.kt`, `ApiResponse`, `InitialInfo`, `UserManagementScreen.kt`, `ShipDetailsScreen.kt`, `QuotaExistenceMultipleResponse`, `hashPassword`, `WarehouseDetailsScreen.kt`, `AppModule.kt`?**
  _High betweenness centrality (0.051) - this node is a cross-community bridge._
- **Why does `UserPreferencesManager` connect `UserPreferencesManager` to `ManageReportsScreen.kt`, `CoroutineScope`, `CargoCounterScreen.kt`, `BootReceiver.kt`, `ChatNavigation.kt`, `Color.kt`, `CargoDetailsScreen.kt`, `StartupViewModel`, `ProfileMenu.kt`, `LoadingNotificationService`, `ChatNotificationWorker.kt`, `SelectInfoScreen.kt`, `UserManagementScreen.kt`, `hashPassword`, `ChatScreen.kt`, `AppModule.kt`, `MainScreen.kt`?**
  _High betweenness centrality (0.042) - this node is a cross-community bridge._
- **What connects `CONFIG`, `Utils`, `name` to the rest of the system?**
  _173 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `ATKCargoTheme` be split into smaller, more focused modules?**
  _Cohesion score 0.0928030303030303 - nodes in this community are weakly interconnected._
- **Should `ManageReportsScreen.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.07692307692307693 - nodes in this community are weakly interconnected._
- **Should `AppApiController` be split into smaller, more focused modules?**
  _Cohesion score 0.08333333333333333 - nodes in this community are weakly interconnected._