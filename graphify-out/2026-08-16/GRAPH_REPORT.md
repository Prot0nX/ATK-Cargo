# Graph Report - ATK-Cargo  (2026-08-16)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 2281 nodes · 4823 edges · 168 communities (130 shown, 38 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 97 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `3f9ce3cb`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- CargoInfoDetailsDialogSection.kt
- InitialInfoScreen.kt
- CargoViewModel
- LoginScreen.kt
- UsersManager
- ReportsViewModel
- WarehouseDetailsScreen.kt
- ClassLoader
- AuthController
- Color.kt
- AppApiController
- SecurityVerifier
- QuotasListScreen.kt
- گزارش جامع بازبینی `app/proguard-rules.pro` — ATK-Cargo
- mysqli
- CargoRepository
- StartupViewModel
- ReportsRepository
- ATKCargoTheme
- UserPreferencesManager
- ApiService
- QuotaManagementDialog.kt
- ReportModels.kt
- Database
- ShipDetailsScreen.kt
- What You Must Do When Invoked
- AnalyticsController
- QuotaPercentageDialogSection.kt
- SessionService
- UserManagementScreen.kt
- ChatRepository
- formatNumber
- CargoModels.kt
- ChatViewModel
- SelectInfoScreen.kt
- AppNotificationManager
- SecurityScreen.kt
- Logger
- UpdateManager
- CargoCounterScreen.kt
- secrets.cpp
- MessageType
- AuthModels.kt
- ReportsCommonWidgets.kt
- ChatMessageEntity
- LoadingNotificationService
- ActiveShipInfo
- ReportsViewModel.kt
- CargoEditSearchDialogsSection.kt
- CargoController
- QuotaDetailsScreen.kt
- hashPassword
- CargoDetailsScreen.kt
- MessageBubble.kt
- composer.json
- SessionManager
- SessionManager
- CargoDetailsComponents.kt
- ChatToolbar.kt
- CargoDetailsDialogSection.kt
- ChatController
- ColorSelector
- ChatScreen.kt
- Request
- CargoInfo
- CoroutineScope
- UserManagementDialogsSection.kt
- ActiveQuotasDialogSection.kt
- ChatNotificationWorker.kt
- ProtectedProxy
- QuotaSelectionDialog.kt
- SplashScreen.kt
- ShipCard.kt
- ManageReportsScreen.kt
- QuotaExistenceMultipleResponse
- DatabaseSchemaExporter
- PermissionPoller
- AnimationManager
- preprocessImage
- ReportsDomain.kt
- graphify reference: extra exports and benchmark
- UtilityController
- GroupSortingMode
- QuotaValidationUseCase.kt
- RealTimeLoadingCardSection.kt
- ShipSortingMode
- QuotaEntryDialog.kt
- WarehouseQuotaGroupingMode
- Config
- PasswordGateService
- ChatNavigation.kt
- ProfileMenu.kt
- proxy_generator.php
- NotificationActionReceiver.kt
- NavRoutes
- MessageInputArea
- HomeNotificationSettingRow.kt
- LoadingState
- JalaliDateUtils
- DataModel.kt
- graphify reference: query, path, explain
- gradlew
- FontWeight
- graphify reference: add a URL and watch a folder
- graphify reference: commit hook and native CLAUDE.md integration
- config.php
- ComposerAutoloaderInit5f285832cbbfb0bffdb861048ac414db
- ExampleInstrumentedTest
- ColorExtensions.kt
- HardwarePerformanceEvaluator
- ComponentDefaults.kt
- ExampleUnitTest
- BaselineProfileGenerator
- graphify reference: GitHub clone and cross-repo merge
- graphify reference: transcribe video and audio
- rules/graphify.md
- workflows/graphify.md
- Constants.kt
- CLAUDE.md
- .claude/CLAUDE.md
- extraction-spec.md
- Job
- SnackbarHostState
- FocusRequester
- T
- ViewModel
- ViewModelProvider

## God Nodes (most connected - your core abstractions)
1. `ReportsViewModel` - 102 edges
2. `CargoViewModel` - 77 edges
3. `ApiService` - 57 edges
4. `UserPreferencesManager` - 44 edges
5. `formatNumber()` - 44 edges
6. `CargoInfo` - 42 edges
7. `AppApiController` - 41 edges
8. `UpdateManager` - 37 edges
9. `ActiveShipInfo` - 35 edges
10. `ReportsRepository` - 34 edges

## Surprising Connections (you probably didn't know these)
- `CargoDetailsScreen()` --calls--> `CargoViewModelFactory`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_details/presentation/CargoDetailsScreen.kt → app/src/main/java/com/atk/atk_cargo/ui/viewmodel/CargoViewModel.kt
- `CargoCounterScreen()` --calls--> `ColorSelector`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_counter/presentation/CargoCounterScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt
- `SelectInfoScreenContent()` --calls--> `ColorSelector`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/SelectInfoScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt
- `QuotasDialog()` --calls--> `ColorSelector`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/reports/presentation/quota_details/QuotaDetailsScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt
- `validateServerSession()` --calls--> `SessionCheckRequest`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/api/SessionValidator.kt → app/src/main/java/com/atk/atk_cargo/data/model/AuthModels.kt

## Import Cycles
- None detected.

## Communities (168 total, 38 thin omitted)

### Community 0 - "CargoInfoDetailsDialogSection.kt"
Cohesion: 0.06
Nodes (70): ShipInfo, formatNumber(), toEnglishNumbers(), EnhancedNumberAnalyzer, extractNumber(), ImageProxy, InputImage, recognizeTextFromImage() (+62 more)

### Community 1 - "InitialInfoScreen.kt"
Cohesion: 0.06
Nodes (62): MainScreen(), RouteTransitions, standardTransitions(), MenuItem, LoginScreen(), formatNumber(), isValidPersianText(), isValidQuotaNumber() (+54 more)

### Community 2 - "CargoViewModel"
Cohesion: 0.07
Nodes (20): CargoCounterRoute, cargoCounterScreen(), NavController, navigateToCargoCounter(), CargoCounterOperationScreen(), NavController, CargoViewModel, CargoViewModelFactory (+12 more)

### Community 3 - "LoginScreen.kt"
Cohesion: 0.06
Nodes (39): Modifier, SearchBar(), AppDatabase, Context, AuthRepository, ConflictSession, Error, Flow (+31 more)

### Community 4 - "UsersManager"
Cohesion: 0.07
Nodes (7): ApiManager, CONFIG, ModalManager, ThemeManager, UIManager, UsersManager, Utils

### Community 5 - "ReportsViewModel"
Cohesion: 0.09
Nodes (3): ComprehensiveAnalytics, RealTimeUiState, ReportsViewModel

### Community 6 - "WarehouseDetailsScreen.kt"
Cohesion: 0.08
Nodes (42): FilteredSummary, VoucherDetail, Warehouse, ExportPdfUseCase, PdfColorScheme, PdfFonts, PersianFontManager, ShamsiDate (+34 more)

### Community 7 - "ClassLoader"
Cohesion: 0.05
Nodes (4): Composer\Semver\VersionParser, ComposerStaticInit5f285832cbbfb0bffdb861048ac414db, ClassLoader, InstalledVersions

### Community 8 - "AuthController"
Cohesion: 0.06
Nodes (6): AuthController, UserController, Response, PermissionService, UserService, InputValidator

### Community 9 - "Color.kt"
Cohesion: 0.23
Nodes (6): ServerSyncingScreen(), Intent, MainActivity, StartupErrorScreen(), Bundle, ComponentActivity

### Community 11 - "SecurityVerifier"
Cohesion: 0.09
Nodes (11): Secrets, SecurityErrorType, LICENSE_INACTIVE, LICENSE_NOT_FOUND, NETWORK_ERROR, TAMPERED, UNKNOWN_ERROR, SecurityVerifier (+3 more)

### Community 12 - "QuotasListScreen.kt"
Cohesion: 0.15
Nodes (28): Quota, QuotaEditData, shareQuotasData(), DeleteQuotaDialog(), androidx, Color, QuotaConfirmDialog(), ToggleQuotaStatusDialog() (+20 more)

### Community 13 - "گزارش جامع بازبینی `app/proguard-rules.pro` — ATK-Cargo"
Cohesion: 0.05
Nodes (36): C1 — قوانین `-keep` بیش‌ازحد باز، obfuscation را روی حساس‌ترین کد خنثی می‌کنند, C2 — keep کامل روی `retrofit2` و `okhttp3` — ٪۳۵ کل seedها، shrinking صفر, C3 — `-keepattributes !SourceFile,!LineNumberTable` معنایی معکوس دارد, H1 — حذف null-checkهای Kotlin: تبدیل fail-fast به رفتار تعریف‌نشده, H2 — حذف `Log.e` و `printStackTrace` = نابینایی کامل در production, H3 — سکشن ۹ (Koin) کاملاً کد مرده است, H4 — iText: هم ریسک runtime، هم مشکل لایسنس, H5 — `proguardFiles` دوبار اعلام شده (+28 more)

### Community 14 - "mysqli"
Cohesion: 0.10
Nodes (10): App\Core\AuthenticatesRequests, App\Core\Database, Exception, InvalidArgumentException, mysqli, mysqli_stmt, DatabaseManager, ApiException (+2 more)

### Community 15 - "CargoRepository"
Cohesion: 0.07
Nodes (7): App\Core\Logger, App\Core\MicroCache, App\Exceptions\ApiException, App\Exceptions\ConflictException, Logger, CargoRepository, CargoService

### Community 16 - "StartupViewModel"
Cohesion: 0.10
Nodes (15): AndroidViewModel, Flow, Intent, StateFlow, Ready, RequestBatteryOptimization, SecurityBlocked, SecurityCheckState (+7 more)

### Community 17 - "ReportsRepository"
Cohesion: 0.11
Nodes (11): CargoInfoResponse, ComprehensiveAnalysisResponse, QuotaDetails, QuotaStatusResponse, RealTimeDataResponse, ShipsData, ErrorResponse, HttpStatusException (+3 more)

### Community 18 - "ATKCargoTheme"
Cohesion: 0.09
Nodes (22): AdaptiveLayoutConfig, rememberAdaptiveLayoutConfig(), WindowSizeClass, COMPACT, EXPANDED, MEDIUM, buildAppDarkColorScheme(), buildAppLightColorScheme() (+14 more)

### Community 19 - "UserPreferencesManager"
Cohesion: 0.11
Nodes (6): TypeToken, Flow, T, UserPreferencesManager, DataStore, Preferences

### Community 20 - "ApiService"
Cohesion: 0.09
Nodes (8): ApiResponse, ApiResponse2, ApiService, DeleteMessageRequest, EditMessageRequest, SuccessResponse, FilteredSummaryResponse, JsonElement

### Community 21 - "QuotaManagementDialog.kt"
Cohesion: 0.08
Nodes (42): FloatTypeAdapter, Context, RetrofitClient, ThirdPartyRetrofitClient, QuotaItem, AdvancedFiltersDialog(), AnalyticsStatChipMini(), CompactStatChip() (+34 more)

### Community 22 - "ReportModels.kt"
Cohesion: 0.11
Nodes (13): ThirdPartyApiService, AnalyticsData, DateInfo, ExistingQuota, ExitDateInfo, QuotaCompletionAnalysis, QuotaSortingMode, REMAINING_TONNAGE_ASC (+5 more)

### Community 23 - "Database"
Cohesion: 0.09
Nodes (7): PDO, PDOException, Database, PDO, self, PDO, SessionRepository

### Community 24 - "ShipDetailsScreen.kt"
Cohesion: 0.15
Nodes (27): Ship, WarningStatus, format(), formatWeightWithDetail(), calculateWarningStatus(), ErrorStateCard(), ImageVector, ShipDetails() (+19 more)

### Community 25 - "What You Must Do When Invoked"
Cohesion: 0.07
Nodes (26): For /graphify add and --watch, For /graphify query, For the commit hook and native CLAUDE.md integration, For --update and --cluster-only, /graphify, Honesty Rules, Interpreter guard for subcommands, Part A - Structural extraction for code files (+18 more)

### Community 26 - "AnalyticsController"
Cohesion: 0.18
Nodes (10): gregorian_to_jalali(), jalali_to_gregorian(), jcheckdate(), jdate(), jdate_words(), jgetdate(), jmktime(), jstrftime() (+2 more)

### Community 27 - "QuotaPercentageDialogSection.kt"
Cohesion: 0.17
Nodes (21): CalculationResult, QuotaPercentageData, AnimatedNumber(), DialogHeader(), Color, ImageVector, Modifier, lerp() (+13 more)

### Community 28 - "SessionService"
Cohesion: 0.10
Nodes (4): enforceMinAppVersion(), requireAuthenticatedSession(), UserRepository, SessionService

### Community 29 - "UserManagementScreen.kt"
Cohesion: 0.16
Nodes (21): DeleteUserRequest, ForceLogoutRequest, UpdateUserRequest, User, EnhancedDeleteConfirmationDialog(), EnhancedForceLogoutDialog(), DashboardSummaryRow(), EmptyStateView() (+13 more)

### Community 30 - "ChatRepository"
Cohesion: 0.12
Nodes (9): ChatMessage, ChatMessagesResponse, SendMessageRequest, SendMessageResponse, UnreadCountResponse, ChatRepository, com, Flow (+1 more)

### Community 31 - "formatNumber"
Cohesion: 0.31
Nodes (16): QuotaCompletionData, formatNumber(), AnalyticsGroupingModeButton(), AnalyticsOwnerSummaryCard(), AnalyticsQuotaCard(), AnalyticsQuotaGroupExpansionPanel(), AnalyticsStatChip(), Context (+8 more)

### Community 32 - "CargoModels.kt"
Cohesion: 0.12
Nodes (12): CargoDeleteResponse, CargoInfoRequest, CargoInfoSearch, CargoSearchResponse, CargoStats, CheckExistenceRequest, CheckExistenceResponse, ExistingCargo (+4 more)

### Community 33 - "ChatViewModel"
Cohesion: 0.10
Nodes (7): ChatViewModel, ChatViewModelFactory, com, StateFlow, T, ViewModel, ViewModelProvider

### Community 34 - "SelectInfoScreen.kt"
Cohesion: 0.23
Nodes (19): formatNumber(), RealTimeLoadingData, extractLastDigits(), FilterChip(), FlatQuotaCard(), GroupedShipList(), Color, Modifier (+11 more)

### Community 36 - "SecurityScreen.kt"
Cohesion: 0.25
Nodes (13): AdvancedOrbitalScanner(), AnimatedUpdateIcon(), AnimatedWarningIcon(), DynamicPremiumBackground(), InteractiveExitButton(), Modifier, RetryButton(), SecurityBlockScreen() (+5 more)

### Community 37 - "Logger"
Cohesion: 0.14
Nodes (4): LicenseController, OnlineUsersController, Logger, self

### Community 38 - "UpdateManager"
Cohesion: 0.07
Nodes (25): UpdateInfo, Completed, Downloading, DownloadProgress, DownloadState, Error, Idle, Context (+17 more)

### Community 39 - "CargoCounterScreen.kt"
Cohesion: 0.19
Nodes (18): CargoCounterScreen(), CargoCounterViewModel, filterShipsByTab(), com, NavController, SnackbarHostState, StateFlow, ViewModel (+10 more)

### Community 40 - "secrets.cpp"
Cohesion: 0.31
Nodes (20): decryptXor(), JNI_OnLoad(), n0(), n1(), n10(), n11(), n2(), n3() (+12 more)

### Community 41 - "MessageType"
Cohesion: 0.17
Nodes (14): Modifier, StatusSnackbar(), MessageType, ERROR, SUCCESS, WARNING, CargoSnackbarQueue, StateFlow (+6 more)

### Community 42 - "AuthModels.kt"
Cohesion: 0.12
Nodes (11): ActiveSessionResponse, ForceLogoutResponse, LogoutRequest, LogoutResponse, PasswordCheckResponse, PermissionSyncRequest, PermissionSyncResponse, SessionCheckRequest (+3 more)

### Community 43 - "ReportsCommonWidgets.kt"
Cohesion: 0.20
Nodes (18): FabItem, Color, ImageVector, Modifier, StatisticsCard(), CompactStatChip(), EmptyQuotaState(), EmptyShipsState() (+10 more)

### Community 44 - "ChatMessageEntity"
Cohesion: 0.14
Nodes (3): ChatDao, Flow, ChatMessageEntity

### Community 45 - "LoadingNotificationService"
Cohesion: 0.22
Nodes (6): Context, Intent, KoinComponent, LoadingNotificationService, IBinder, Service

### Community 46 - "ActiveShipInfo"
Cohesion: 0.27
Nodes (16): ActiveShipInfo, CargoSnackbarMessage, GroupedShipList(), Color, Modifier, QuotaCard(), ShipGroup(), ShipHeader() (+8 more)

### Community 47 - "ReportsViewModel.kt"
Cohesion: 0.19
Nodes (11): QuotaGroupingMode, BY_CARGO_OWNER, BY_CARRIER, BY_SHIP, ThirdPartyOrder, buildQuotaGroups(), QuotaGroup, AndroidViewModel (+3 more)

### Community 48 - "CargoEditSearchDialogsSection.kt"
Cohesion: 0.30
Nodes (16): CargoChangesPreview(), CargoEditConfirmDialog(), CargoEditConfirmHeader(), CargoMainInfo(), CargoShippingInfo(), CargoTimeInfo(), CargoWeightInfo(), ChangeItem() (+8 more)

### Community 50 - "QuotaDetailsScreen.kt"
Cohesion: 0.42
Nodes (9): ApiQuotaDetails, calculatePercentage(), calculateProgress(), ProgressBar(), QuotaAdditionalInfo(), QuotaDetails(), QuotaInfoCards(), QuotaMainCard() (+1 more)

### Community 51 - "hashPassword"
Cohesion: 0.18
Nodes (6): LoginRequest, LoginResponse, AuthRepositoryImpl, Flow, hashPassword(), SecurityUtilsTest

### Community 52 - "CargoDetailsScreen.kt"
Cohesion: 0.22
Nodes (14): Result, validateServerSession(), CargoDetailsScreen(), confirmCargo(), FloatingActionButtonItem(), handleCargoConfirmation(), Color, ImageVector (+6 more)

### Community 53 - "MessageBubble.kt"
Cohesion: 0.24
Nodes (14): extractShipInfoAndText(), com, replaceUsernamesWithFullNames(), rotateIcon(), ShipInfoModel, ChatInfoRow(), EditMessageDialog(), androidx (+6 more)

### Community 54 - "composer.json"
Cohesion: 0.12
Nodes (15): autoload, autoload-dev, psr-4, psr-4, config, optimize-autoloader, sort-packages, description (+7 more)

### Community 57 - "CargoDetailsComponents.kt"
Cohesion: 0.28
Nodes (13): InitialInfo, CargoInfoCard(), CargoListSection(), GroupStats(), InfoChips(), InfoGrid(), InfoGridCard(), InfoGridItem() (+5 more)

### Community 58 - "ChatToolbar.kt"
Cohesion: 0.25
Nodes (13): ColorWheel(), Color, Modifier, getAdaptiveBubbleColor(), AdvancedColorPickerDialog(), ChatSettingsDialogEnhanced(), ChatSettingsPreviewRefined(), Color (+5 more)

### Community 59 - "CargoDetailsDialogSection.kt"
Cohesion: 0.31
Nodes (13): CargoDetailsDialog(), Color, ImageVector, Modifier, ModernCargoDetailsGrid(), ModernCargoStatusSection(), ModernCargoStatusTimeline(), ModernDialogActions() (+5 more)

### Community 61 - "ColorSelector"
Cohesion: 0.39
Nodes (3): adjustColorForTheme(), ColorSelector, Color

### Community 62 - "ChatScreen.kt"
Cohesion: 0.27
Nodes (12): ChatUiItem, getChatBackgroundColor(), getDateHeaderColor(), Header, Color, Message, ChatScreen(), DateHeader() (+4 more)

### Community 64 - "CargoInfo"
Cohesion: 0.18
Nodes (11): CargoInfo, SaveOrUpdateResponse, SubmitCargoUseCase, FormSection(), CargoSearchResultCard(), InfoRowCompact(), Color, Context (+3 more)

### Community 65 - "CoroutineScope"
Cohesion: 0.27
Nodes (8): AtkCargoApplication, BootReceiver, BroadcastReceiver, Context, Intent, KoinComponent, Application, CoroutineScope

### Community 66 - "UserManagementDialogsSection.kt"
Cohesion: 0.36
Nodes (11): CreateUserRequest, UserTypeInfo, DesignTextField(), EnhancedAddUserDialog(), EnhancedEditUserDialog(), getUserTypes(), androidx, KeyboardOptions (+3 more)

### Community 67 - "ActiveQuotasDialogSection.kt"
Cohesion: 0.18
Nodes (18): ActiveQuotasDialog(), FilterBar(), Color, Dp, Modifier, QuotasHeader(), StatItem(), VerticalDivider() (+10 more)

### Community 68 - "ChatNotificationWorker.kt"
Cohesion: 0.48
Nodes (4): ChatNotificationWorker, KoinComponent, CoroutineWorker, ListenableWorker

### Community 70 - "QuotaSelectionDialog.kt"
Cohesion: 0.42
Nodes (10): MatchingQuota, DialogHeader(), findDifferentFields(), InactiveIndicator(), ImageVector, QuotaDetailItem(), QuotaDetails(), QuotaHeader() (+2 more)

### Community 71 - "SplashScreen.kt"
Cohesion: 0.53
Nodes (5): SplashScreen(), SplashScreenConstants, SplashScreenContent(), SplashSubtitleBadge(), SplashVersionBadge()

### Community 72 - "ShipCard.kt"
Cohesion: 0.25
Nodes (13): ErrorState(), ImageVector, Modifier, ImageVector, Modifier, ShipCard(), ShipCardContent(), ShipSortingModeButton() (+5 more)

### Community 73 - "ManageReportsScreen.kt"
Cohesion: 0.08
Nodes (44): DateClickableField(), DateRangePicker(), getDaysInPersianMonth(), isPersianLeapYear(), Color, Modifier, PersianDatePickerDialog(), AdvancedSearchDialog() (+36 more)

### Community 76 - "PermissionPoller"
Cohesion: 0.36
Nodes (3): Job, StateFlow, PermissionPoller

### Community 78 - "preprocessImage"
Cohesion: 0.50
Nodes (8): adaptiveThresholding(), applyContrastEnhancement(), enhanceEdges(), ImageProxy, InputImage, medianFilter(), preprocessImage(), IntArray

### Community 79 - "ReportsDomain.kt"
Cohesion: 0.23
Nodes (8): buildQuotasShareText(), Context, QuotaWarningThresholds, sortShips(), ReportsDomainTest, Quota, Ship, ShipSortingMode

### Community 80 - "graphify reference: extra exports and benchmark"
Cohesion: 0.22
Nodes (8): graphify reference: extra exports and benchmark, Step 6b - Wiki (only if --wiki flag), Step 7 - Neo4j export (only if --neo4j or --neo4j-push flag), Step 7a - FalkorDB export (only if --falkordb or --falkordb-push flag), Step 7b - SVG export (only if --svg flag), Step 7c - GraphML export (only if --graphml flag), Step 7d - MCP server (only if --mcp flag), Step 8 - Token reduction benchmark (only if total_words > 5000)

### Community 82 - "GroupSortingMode"
Cohesion: 0.40
Nodes (4): GroupSortingMode, ALPHABETICAL, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC

### Community 83 - "QuotaValidationUseCase.kt"
Cohesion: 0.36
Nodes (4): QuotaValidationResult, InputValidationResult, QuotaValidationUseCase, TempTonnageValidationResult

### Community 84 - "RealTimeLoadingCardSection.kt"
Cohesion: 0.43
Nodes (6): CompactInfo(), Color, ImageVector, Modifier, RealTimeLoadingCard(), StatisticItem()

### Community 85 - "ShipSortingMode"
Cohesion: 0.25
Nodes (7): ShipSortingMode, LOADED_TONNAGE_ASC, LOADED_TONNAGE_DESC, NAME_ASC, NAME_DESC, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC

### Community 86 - "QuotaEntryDialog.kt"
Cohesion: 0.57
Nodes (7): FocusRequester, QuotaEntryActions(), QuotaEntryContent(), QuotaEntryDialog(), QuotaEntryPalette, rememberQuotaEntryPalette(), validateAndSubmit()

### Community 87 - "WarehouseQuotaGroupingMode"
Cohesion: 0.40
Nodes (4): WarehouseQuotaGroupingMode, BY_CARGO_OWNER, BY_SHIPPING_COMPANY, BY_WAREHOUSE

### Community 90 - "ChatNavigation.kt"
Cohesion: 0.50
Nodes (4): AdminChatRoute, adminChatScreen(), NavController, navigateToAdminChat()

### Community 91 - "ProfileMenu.kt"
Cohesion: 0.31
Nodes (8): ThemeColorOption, ThemeColorPickerRow(), ActionButtons(), getUserTypeDisplay(), Color, ProfileMenu(), ProfilePalette, rememberProfilePalette()

### Community 92 - "proxy_generator.php"
Cohesion: 0.57
Nodes (6): _o1(), _o2(), _o3(), _o4(), _o5(), _o6()

### Community 93 - "NotificationActionReceiver.kt"
Cohesion: 0.53
Nodes (4): BroadcastReceiver, Context, Intent, NotificationActionReceiver

### Community 95 - "MessageInputArea"
Cohesion: 0.67
Nodes (5): Color, com, MessageInputArea(), QuotaSelectionDialog(), ShipSelectionDialog()

### Community 96 - "HomeNotificationSettingRow.kt"
Cohesion: 0.67
Nodes (5): CompactSwitch(), Color, ImageVector, Modifier, NotificationSettingRow()

### Community 97 - "LoadingState"
Cohesion: 0.47
Nodes (6): Error, Idle, Loading, LoadingState, Success, UiState

### Community 99 - "DataModel.kt"
Cohesion: 0.67
Nodes (3): adjustColorForTheme(), Color, toTon()

### Community 100 - "graphify reference: query, path, explain"
Cohesion: 0.40
Nodes (5): For /graphify explain, For /graphify path, graphify reference: query, path, explain, Step 0 — Constrained query expansion (REQUIRED before traversal), Step 1 — Traversal

### Community 101 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 102 - "FontWeight"
Cohesion: 0.15
Nodes (11): ConfirmationDialog(), ImageVector, EmptyState(), ImageVector, Modifier, AnimatedCounter(), ExitStatusDialog(), ImageVector (+3 more)

### Community 104 - "graphify reference: add a URL and watch a folder"
Cohesion: 0.50
Nodes (3): For /graphify add, For --watch, graphify reference: add a URL and watch a folder

### Community 105 - "graphify reference: commit hook and native CLAUDE.md integration"
Cohesion: 0.50
Nodes (3): For git commit hook, For native CLAUDE.md integration, graphify reference: commit hook and native CLAUDE.md integration

## Knowledge Gaps
- **168 isolated node(s):** `Constants`, `Ready`, `RequestBatteryOptimization`, `Splash`, `Syncing` (+163 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **38 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CargoViewModel` connect `CargoViewModel` to `CargoInfoDetailsDialogSection.kt`, `InitialInfoScreen.kt`, `SelectInfoScreen.kt`, `LoginScreen.kt`, `ActiveShipInfo`, `CargoDetailsScreen.kt`?**
  _High betweenness centrality (0.186) - this node is a cross-community bridge._
- **Why does `ReportsViewModel` connect `ReportsViewModel` to `InitialInfoScreen.kt`, `LoginScreen.kt`, `WarehouseDetailsScreen.kt`, `QuotasListScreen.kt`, `ReportsRepository`, `QuotaManagementDialog.kt`, `ReportModels.kt`, `ShipDetailsScreen.kt`, `formatNumber`, `ReportsViewModel.kt`, `CargoEditSearchDialogsSection.kt`, `QuotaDetailsScreen.kt`, `ColorSelector`, `ShipCard.kt`, `ManageReportsScreen.kt`, `GroupSortingMode`, `ShipSortingMode`, `WarehouseQuotaGroupingMode`, `LoadingState`?**
  _High betweenness centrality (0.116) - this node is a cross-community bridge._
- **Why does `UpdateManager` connect `UpdateManager` to `StartupViewModel`, `Color.kt`, `LoginScreen.kt`?**
  _High betweenness centrality (0.085) - this node is a cross-community bridge._
- **What connects `Constants`, `Ready`, `RequestBatteryOptimization` to the rest of the system?**
  _168 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `CargoInfoDetailsDialogSection.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.06253585771658061 - nodes in this community are weakly interconnected._
- **Should `InitialInfoScreen.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.05621621621621622 - nodes in this community are weakly interconnected._
- **Should `CargoViewModel` be split into smaller, more focused modules?**
  _Cohesion score 0.07033248081841433 - nodes in this community are weakly interconnected._