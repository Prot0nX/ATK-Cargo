# Graph Report - ATK-Cargo  (2026-08-14)

## Corpus Check
- 274 files · ~349,580 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2246 nodes · 4782 edges · 159 communities (125 shown, 34 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 76 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `ba567f86`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- formatNumber
- ShipDetailsScreen.kt
- CargoInfo
- UsersManager
- ReportsRepository
- ReportsCommonWidgets.kt
- ClassLoader
- CargoViewModel
- UpdateManager
- ReportModels.kt
- AppApiController
- SecurityVerifier
- StartupViewModel
- ReportsViewModel
- InitialInfoScreen.kt
- QuotaManagementDialog.kt
- CargoRepository
- MessageBubble.kt
- SelectInfoScreen.kt
- ChatRepository
- CargoModels.kt
- ATKCargoTheme
- ChatViewModel
- ManageReportsScreen.kt
- WarehouseDetailsScreen.kt
- AuthController
- ApiService
- SplashScreen.kt
- UserPreferencesManager
- SessionRepository
- CargoCounterScreen.kt
- preprocessImage
- RealTimeLoadingData
- CoroutineScope
- LoadingNotificationService
- FontWeight
- Database
- UserManagementScreen.kt
- Ship
- CargoEditSearchDialogsSection.kt
- MessageType
- AnalyticsController
- QuotasListScreen.kt
- secrets.cpp
- QuotaEntryDialog.kt
- گزارش جامع بازبینی `app/proguard-rules.pro` — ATK-Cargo
- AppModule.kt
- AppNotificationManager
- ChatMessageEntity
- ShipInfoSection.kt
- ActiveQuotasDialogSection.kt
- Request
- Logger
- ActiveShipInfo
- SecurityScreen.kt
- composer.json
- SessionManager
- SessionManager
- RealTimeLoadingBottomSheet
- What You Must Do When Invoked
- ProfileMenu.kt
- PermissionPoller
- AuthModels.kt
- ReportsDomain.kt
- ChatScreen.kt
- CargoCounterOperationScreen
- QuotaSelectionDialog.kt
- CargoInfoDetailsDialogSection.kt
- Exception
- RegisterCargoScreen.kt
- QuotaPercentageDialogSection.kt
- ChatNotificationWorker.kt
- MainActivity.kt
- DatabaseSchemaExporter
- ProtectedProxy
- ChatController
- SessionService
- graphify reference: extra exports and benchmark
- ComprehensiveAnalyticsDialog.kt
- ChatToolbar.kt
- UserManagementDialogsSection.kt
- Config
- hashPassword
- UtilityController
- HomeNotificationSettingRow.kt
- graphify reference: query, path, explain
- Color.kt
- graphify reference: add a URL and watch a folder
- graphify reference: commit hook and native CLAUDE.md integration
- CargoDetailsScreen.kt
- ColorExtensions.kt
- HardwarePerformanceEvaluator
- PasswordGateService
- MessageInputArea
- graphify reference: GitHub clone and cross-repo merge
- proxy_generator.php
- NotificationActionReceiver.kt
- NavRoutes
- AppDatabase
- graphify reference: transcribe video and audio
- rules/graphify.md
- workflows/graphify.md
- LoadingState
- JalaliDateUtils
- CLAUDE.md
- .claude/CLAUDE.md
- AnimationManager
- gradlew
- extraction-spec.md
- config.php
- ComposerAutoloaderInit5f285832cbbfb0bffdb861048ac414db
- ExampleInstrumentedTest
- update.md
- Constants
- ComponentDefaults.kt
- ExampleUnitTest
- BaselineProfileGenerator
- QuotaExistenceMultipleResponse

## God Nodes (most connected - your core abstractions)
1. `ReportsViewModel` - 105 edges
2. `CargoViewModel` - 80 edges
3. `ApiService` - 57 edges
4. `CargoInfo` - 57 edges
5. `formatNumber()` - 46 edges
6. `UserPreferencesManager` - 45 edges
7. `AppApiController` - 41 edges
8. `UpdateManager` - 37 edges
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

## Communities (159 total, 34 thin omitted)

### Community 0 - "formatNumber"
Cohesion: 0.19
Nodes (22): QuotaCompletionData, QuotaGroupingMode, BY_CARGO_OWNER, BY_CARRIER, BY_SHIP, buildQuotaGroups(), QuotaGroup, formatNumber() (+14 more)

### Community 1 - "ShipDetailsScreen.kt"
Cohesion: 0.17
Nodes (25): WarningStatus, formatWeightWithDetail(), calculateWarningStatus(), ErrorStateCard(), ImageVector, ShipDetails(), ShipHeaderCard(), TabItem() (+17 more)

### Community 2 - "CargoInfo"
Cohesion: 0.18
Nodes (21): CargoInfo, CargoDetailsDialog(), Color, ImageVector, Modifier, ModernCargoDetailsGrid(), ModernCargoStatusSection(), ModernCargoStatusTimeline() (+13 more)

### Community 3 - "UsersManager"
Cohesion: 0.07
Nodes (7): ApiManager, CONFIG, ModalManager, ThemeManager, UIManager, UsersManager, Utils

### Community 4 - "ReportsRepository"
Cohesion: 0.10
Nodes (9): CargoInfoResponse, ComprehensiveAnalysisResponse, QuotaDetails, QuotaStatusResponse, ErrorResponse, HttpStatusException, Exception, Result (+1 more)

### Community 5 - "ReportsCommonWidgets.kt"
Cohesion: 0.12
Nodes (30): ApiQuotaDetails, adjustColorForTheme(), FabItem, Color, toTon(), Color, ImageVector, Modifier (+22 more)

### Community 6 - "ClassLoader"
Cohesion: 0.05
Nodes (4): Composer\Semver\VersionParser, ComposerStaticInit5f285832cbbfb0bffdb861048ac414db, ClassLoader, InstalledVersions

### Community 8 - "UpdateManager"
Cohesion: 0.05
Nodes (37): UpdateInfo, FloatTypeAdapter, Context, RetrofitClient, ThirdPartyRetrofitClient, Completed, Downloading, DownloadProgress (+29 more)

### Community 9 - "ReportModels.kt"
Cohesion: 0.13
Nodes (12): ThirdPartyApiService, adjustColorForTheme(), AnalyticsData, ColorSelector, DateInfo, ExistingQuota, ExitDateInfo, Color (+4 more)

### Community 10 - "AppApiController"
Cohesion: 0.08
Nodes (3): AppApiController, CargoController, MicroCache

### Community 11 - "SecurityVerifier"
Cohesion: 0.09
Nodes (11): Secrets, SecurityErrorType, LICENSE_INACTIVE, LICENSE_NOT_FOUND, NETWORK_ERROR, TAMPERED, UNKNOWN_ERROR, SecurityVerifier (+3 more)

### Community 12 - "StartupViewModel"
Cohesion: 0.09
Nodes (15): AndroidViewModel, Flow, Intent, StateFlow, Ready, RequestBatteryOptimization, SecurityBlocked, SecurityCheckState (+7 more)

### Community 13 - "ReportsViewModel"
Cohesion: 0.07
Nodes (14): ComprehensiveAnalytics, GroupSortingMode, ALPHABETICAL, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC, QuotaSortingMode, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC (+6 more)

### Community 14 - "InitialInfoScreen.kt"
Cohesion: 0.12
Nodes (32): CheckExistenceRequest, CheckExistenceResponse, formatNumber(), isValidPersianText(), isValidQuotaNumber(), isValidShipName(), isValidWarehouseName(), isValidWeight() (+24 more)

### Community 15 - "QuotaManagementDialog.kt"
Cohesion: 0.17
Nodes (28): QuotaItem, AdvancedFiltersDialog(), AnalyticsStatChipMini(), CompactStatChip(), EmptyQuotaState(), FilterOptionsCard(), InfoChip(), IntegratedQuotaCard() (+20 more)

### Community 17 - "MessageBubble.kt"
Cohesion: 0.24
Nodes (14): extractShipInfoAndText(), com, replaceUsernamesWithFullNames(), rotateIcon(), ShipInfoModel, ChatInfoRow(), EditMessageDialog(), androidx (+6 more)

### Community 18 - "SelectInfoScreen.kt"
Cohesion: 0.17
Nodes (21): formatNumber(), ShipSelectionDialog(), EmptySearchResult(), extractLastDigits(), FilterChip(), FilterState, ALL, COMPLETED (+13 more)

### Community 19 - "ChatRepository"
Cohesion: 0.12
Nodes (9): ChatMessage, ChatMessagesResponse, SendMessageRequest, SendMessageResponse, UnreadCountResponse, ChatRepository, com, Flow (+1 more)

### Community 20 - "CargoModels.kt"
Cohesion: 0.09
Nodes (14): CargoInfoSearch, CargoSearchResponse, CargoStats, ExistingCargo, InitialInfo, LoadableTonnageResponse, QuotaTonnageWarning, QuotaValidationResult (+6 more)

### Community 21 - "ATKCargoTheme"
Cohesion: 0.09
Nodes (22): AdaptiveLayoutConfig, rememberAdaptiveLayoutConfig(), WindowSizeClass, COMPACT, EXPANDED, MEDIUM, buildAppDarkColorScheme(), buildAppLightColorScheme() (+14 more)

### Community 22 - "ChatViewModel"
Cohesion: 0.10
Nodes (7): ChatViewModel, ChatViewModelFactory, com, StateFlow, T, ViewModel, ViewModelProvider

### Community 23 - "ManageReportsScreen.kt"
Cohesion: 0.05
Nodes (62): MainScreen(), RouteTransitions, standardTransitions(), DateClickableField(), DateRangePicker(), getDaysInPersianMonth(), isPersianLeapYear(), Color (+54 more)

### Community 24 - "WarehouseDetailsScreen.kt"
Cohesion: 0.08
Nodes (42): FilteredSummary, VoucherDetail, Warehouse, ExportPdfUseCase, PdfColorScheme, PdfFonts, PersianFontManager, ShamsiDate (+34 more)

### Community 25 - "AuthController"
Cohesion: 0.06
Nodes (6): AuthController, UserController, Response, PermissionService, UserService, InputValidator

### Community 26 - "ApiService"
Cohesion: 0.08
Nodes (10): ApiResponse, ApiResponse2, ApiService, DeleteMessageRequest, EditMessageRequest, SuccessResponse, FilteredSummaryResponse, RealTimeDataResponse (+2 more)

### Community 27 - "SplashScreen.kt"
Cohesion: 0.53
Nodes (5): SplashScreen(), SplashScreenConstants, SplashScreenContent(), SplashSubtitleBadge(), SplashVersionBadge()

### Community 28 - "UserPreferencesManager"
Cohesion: 0.11
Nodes (6): TypeToken, Flow, T, UserPreferencesManager, DataStore, Preferences

### Community 30 - "CargoCounterScreen.kt"
Cohesion: 0.19
Nodes (18): CargoCounterScreen(), CargoCounterViewModel, filterShipsByTab(), com, NavController, SnackbarHostState, StateFlow, ViewModel (+10 more)

### Community 31 - "preprocessImage"
Cohesion: 0.13
Nodes (22): adaptiveThresholding(), applyContrastEnhancement(), enhanceEdges(), ImageProxy, InputImage, medianFilter(), preprocessImage(), EnhancedNumberAnalyzer (+14 more)

### Community 32 - "RealTimeLoadingData"
Cohesion: 0.25
Nodes (13): RealTimeLoadingData, GroupedShipList(), Color, ShipCardDesign(), ShipGroup(), ShipGroupWithRealTimeData(), StatChip(), CompactInfo() (+5 more)

### Community 33 - "CoroutineScope"
Cohesion: 0.27
Nodes (8): AtkCargoApplication, BootReceiver, BroadcastReceiver, Context, Intent, KoinComponent, Application, CoroutineScope

### Community 34 - "LoadingNotificationService"
Cohesion: 0.22
Nodes (6): Context, Intent, KoinComponent, LoadingNotificationService, IBinder, Service

### Community 35 - "FontWeight"
Cohesion: 0.06
Nodes (37): ConfirmationDialog(), ImageVector, EmptyState(), ImageVector, Modifier, Modifier, SearchBar(), CompactLoginLayout() (+29 more)

### Community 36 - "Database"
Cohesion: 0.13
Nodes (8): mysqli, mysqli_stmt, PDO, PDOException, Database, PDO, self, DatabaseManager

### Community 37 - "UserManagementScreen.kt"
Cohesion: 0.16
Nodes (21): DeleteUserRequest, ForceLogoutRequest, UpdateUserRequest, User, EnhancedDeleteConfirmationDialog(), EnhancedForceLogoutDialog(), DashboardSummaryRow(), EmptyStateView() (+13 more)

### Community 38 - "Ship"
Cohesion: 0.11
Nodes (23): ErrorState(), ImageVector, Modifier, Ship, ShipSortingMode, LOADED_TONNAGE_ASC, LOADED_TONNAGE_DESC, NAME_ASC (+15 more)

### Community 39 - "CargoEditSearchDialogsSection.kt"
Cohesion: 0.30
Nodes (16): CargoChangesPreview(), CargoEditConfirmDialog(), CargoEditConfirmHeader(), CargoMainInfo(), CargoShippingInfo(), CargoTimeInfo(), CargoWeightInfo(), ChangeItem() (+8 more)

### Community 40 - "MessageType"
Cohesion: 0.17
Nodes (14): Modifier, StatusSnackbar(), MessageType, ERROR, SUCCESS, WARNING, CargoSnackbarQueue, StateFlow (+6 more)

### Community 41 - "AnalyticsController"
Cohesion: 0.18
Nodes (10): gregorian_to_jalali(), jalali_to_gregorian(), jcheckdate(), jdate(), jdate_words(), jgetdate(), jmktime(), jstrftime() (+2 more)

### Community 42 - "QuotasListScreen.kt"
Cohesion: 0.13
Nodes (33): Quota, QuotaEditData, WarehouseQuotaGroupingMode, BY_CARGO_OWNER, BY_SHIPPING_COMPANY, BY_WAREHOUSE, buildQuotasShareText(), shareQuotasData() (+25 more)

### Community 43 - "secrets.cpp"
Cohesion: 0.31
Nodes (20): decryptXor(), JNI_OnLoad(), n0(), n1(), n10(), n11(), n2(), n3() (+12 more)

### Community 44 - "QuotaEntryDialog.kt"
Cohesion: 0.57
Nodes (7): FocusRequester, QuotaEntryActions(), QuotaEntryContent(), QuotaEntryDialog(), QuotaEntryPalette, rememberQuotaEntryPalette(), validateAndSubmit()

### Community 45 - "گزارش جامع بازبینی `app/proguard-rules.pro` — ATK-Cargo"
Cohesion: 0.05
Nodes (36): C1 — قوانین `-keep` بیش‌ازحد باز، obfuscation را روی حساس‌ترین کد خنثی می‌کنند, C2 — keep کامل روی `retrofit2` و `okhttp3` — ٪۳۵ کل seedها، shrinking صفر, C3 — `-keepattributes !SourceFile,!LineNumberTable` معنایی معکوس دارد, H1 — حذف null-checkهای Kotlin: تبدیل fail-fast به رفتار تعریف‌نشده, H2 — حذف `Log.e` و `printStackTrace` = نابینایی کامل در production, H3 — سکشن ۹ (Koin) کاملاً کد مرده است, H4 — iText: هم ریسک runtime، هم مشکل لایسنس, H5 — `proguardFiles` دوبار اعلام شده (+28 more)

### Community 46 - "AppModule.kt"
Cohesion: 0.13
Nodes (11): LogoutRequest, LogoutResponse, AuthRepository, ConflictSession, Error, Flow, LoginResult, Success (+3 more)

### Community 48 - "ChatMessageEntity"
Cohesion: 0.15
Nodes (3): ChatDao, Flow, ChatMessageEntity

### Community 49 - "ShipInfoSection.kt"
Cohesion: 0.30
Nodes (13): ShipInfo, formatNumber(), toEnglishNumbers(), DetailedInfoGrid(), DetailInfoItem(), ExpandedContent(), Color, ImageVector (+5 more)

### Community 50 - "ActiveQuotasDialogSection.kt"
Cohesion: 0.29
Nodes (11): ActiveQuotasDialog(), FilterBar(), Color, Dp, Modifier, QuotasHeader(), StatItem(), VerticalDivider() (+3 more)

### Community 52 - "Logger"
Cohesion: 0.12
Nodes (4): LicenseController, OnlineUsersController, Logger, self

### Community 53 - "ActiveShipInfo"
Cohesion: 0.32
Nodes (15): ActiveShipInfo, CargoSnackbarMessage, GroupedShipList(), Color, Modifier, QuotaCard(), ShipGroup(), ShipHeader() (+7 more)

### Community 54 - "SecurityScreen.kt"
Cohesion: 0.27
Nodes (12): AdvancedOrbitalScanner(), AnimatedUpdateIcon(), AnimatedWarningIcon(), DynamicPremiumBackground(), InteractiveExitButton(), Modifier, RetryButton(), SecurityBlockScreen() (+4 more)

### Community 55 - "composer.json"
Cohesion: 0.12
Nodes (15): autoload, autoload-dev, psr-4, psr-4, config, optimize-autoloader, sort-packages, description (+7 more)

### Community 58 - "RealTimeLoadingBottomSheet"
Cohesion: 0.35
Nodes (9): ShiftInfo, DialogHeader(), Color, Modifier, RealTimeLoadingBottomSheet(), RealTimeShiftNavigation(), RefreshOverlay(), ShipCard() (+1 more)

### Community 59 - "What You Must Do When Invoked"
Cohesion: 0.07
Nodes (26): For /graphify add and --watch, For /graphify query, For the commit hook and native CLAUDE.md integration, For --update and --cluster-only, /graphify, Honesty Rules, Interpreter guard for subcommands, Part A - Structural extraction for code files (+18 more)

### Community 60 - "ProfileMenu.kt"
Cohesion: 0.31
Nodes (8): ThemeColorOption, ThemeColorPickerRow(), ActionButtons(), getUserTypeDisplay(), Color, ProfileMenu(), ProfilePalette, rememberProfilePalette()

### Community 61 - "PermissionPoller"
Cohesion: 0.36
Nodes (3): Job, StateFlow, PermissionPoller

### Community 62 - "AuthModels.kt"
Cohesion: 0.15
Nodes (9): ActiveSessionResponse, ForceLogoutResponse, LoginRequest, LoginResponse, PasswordCheckResponse, PermissionSyncRequest, PermissionSyncResponse, SessionCheckRequest (+1 more)

### Community 63 - "ReportsDomain.kt"
Cohesion: 0.33
Nodes (8): addOneDayToPersianDate(), format(), formatHoursToPersian(), getDaysInPersianMonth(), isPersianLeapYear(), Context, QuotaWarningThresholds, shareCargoInfo()

### Community 64 - "ChatScreen.kt"
Cohesion: 0.31
Nodes (10): ChatUiItem, getDateHeaderColor(), Header, Message, ChatScreen(), DateHeader(), EmptyState(), processMessagesForDisplay() (+2 more)

### Community 65 - "CargoCounterOperationScreen"
Cohesion: 0.36
Nodes (6): CargoCounterRoute, cargoCounterScreen(), NavController, navigateToCargoCounter(), CargoCounterOperationScreen(), NavController

### Community 66 - "QuotaSelectionDialog.kt"
Cohesion: 0.42
Nodes (10): MatchingQuota, DialogHeader(), findDifferentFields(), InactiveIndicator(), ImageVector, QuotaDetailItem(), QuotaDetails(), QuotaHeader() (+2 more)

### Community 67 - "CargoInfoDetailsDialogSection.kt"
Cohesion: 0.16
Nodes (19): CargoDeleteResponse, CargoInfoRequest, CargoDetailsPalette, CargoDetailTabItem(), CargoInfoDetailsDialog(), DeleteDialog(), DetailInfoRow(), ImageVector (+11 more)

### Community 68 - "Exception"
Cohesion: 0.17
Nodes (6): App\Core\AuthenticatesRequests, Exception, InvalidArgumentException, ApiException, ConflictException, DatabaseException

### Community 69 - "RegisterCargoScreen.kt"
Cohesion: 0.16
Nodes (20): CargoRegistrationRoute, cargoRegistrationScreen(), NavController, navigateToCargoRegistration(), AnimatedIcon(), DialogContent(), DialogPassword(), DuplicateConfirmationDialog() (+12 more)

### Community 70 - "QuotaPercentageDialogSection.kt"
Cohesion: 0.17
Nodes (21): CalculationResult, QuotaPercentageData, AnimatedNumber(), DialogHeader(), Color, ImageVector, Modifier, lerp() (+13 more)

### Community 71 - "ChatNotificationWorker.kt"
Cohesion: 0.48
Nodes (4): ChatNotificationWorker, KoinComponent, CoroutineWorker, ListenableWorker

### Community 72 - "MainActivity.kt"
Cohesion: 0.26
Nodes (6): ServerSyncingScreen(), Intent, MainActivity, StartupErrorScreen(), Bundle, ComponentActivity

### Community 76 - "SessionService"
Cohesion: 0.10
Nodes (4): enforceMinAppVersion(), requireAuthenticatedSession(), UserRepository, SessionService

### Community 77 - "graphify reference: extra exports and benchmark"
Cohesion: 0.22
Nodes (8): graphify reference: extra exports and benchmark, Step 6b - Wiki (only if --wiki flag), Step 7 - Neo4j export (only if --neo4j or --neo4j-push flag), Step 7a - FalkorDB export (only if --falkordb or --falkordb-push flag), Step 7b - SVG export (only if --svg flag), Step 7c - GraphML export (only if --graphml flag), Step 7d - MCP server (only if --mcp flag), Step 8 - Token reduction benchmark (only if total_words > 5000)

### Community 78 - "ComprehensiveAnalyticsDialog.kt"
Cohesion: 0.57
Nodes (6): AnalyticsDateNavigation(), AnalyticsHeaderCard(), ComprehensiveAnalyticsDialog(), EmptyStateCard(), ErrorStateCard(), Modifier

### Community 79 - "ChatToolbar.kt"
Cohesion: 0.22
Nodes (15): ColorWheel(), Color, Modifier, getAdaptiveBubbleColor(), getChatBackgroundColor(), Color, AdvancedColorPickerDialog(), ChatSettingsDialogEnhanced() (+7 more)

### Community 80 - "UserManagementDialogsSection.kt"
Cohesion: 0.36
Nodes (11): CreateUserRequest, UserTypeInfo, DesignTextField(), EnhancedAddUserDialog(), EnhancedEditUserDialog(), getUserTypes(), androidx, KeyboardOptions (+3 more)

### Community 82 - "hashPassword"
Cohesion: 0.23
Nodes (4): AuthRepositoryImpl, Flow, hashPassword(), SecurityUtilsTest

### Community 84 - "HomeNotificationSettingRow.kt"
Cohesion: 0.67
Nodes (5): CompactSwitch(), Color, ImageVector, Modifier, NotificationSettingRow()

### Community 85 - "graphify reference: query, path, explain"
Cohesion: 0.40
Nodes (5): For /graphify explain, For /graphify path, graphify reference: query, path, explain, Step 0 — Constrained query expansion (REQUIRED before traversal), Step 1 — Traversal

### Community 88 - "graphify reference: add a URL and watch a folder"
Cohesion: 0.50
Nodes (3): For /graphify add, For --watch, graphify reference: add a URL and watch a folder

### Community 89 - "graphify reference: commit hook and native CLAUDE.md integration"
Cohesion: 0.50
Nodes (3): For git commit hook, For native CLAUDE.md integration, graphify reference: commit hook and native CLAUDE.md integration

### Community 90 - "CargoDetailsScreen.kt"
Cohesion: 0.14
Nodes (25): Result, validateServerSession(), CargoDetailsScreen(), confirmCargo(), FloatingActionButtonItem(), handleCargoConfirmation(), Color, ImageVector (+17 more)

### Community 95 - "MessageInputArea"
Cohesion: 0.67
Nodes (5): Color, com, MessageInputArea(), QuotaSelectionDialog(), ShipSelectionDialog()

### Community 97 - "proxy_generator.php"
Cohesion: 0.57
Nodes (6): _o1(), _o2(), _o3(), _o4(), _o5(), _o6()

### Community 98 - "NotificationActionReceiver.kt"
Cohesion: 0.53
Nodes (4): BroadcastReceiver, Context, Intent, NotificationActionReceiver

### Community 100 - "AppDatabase"
Cohesion: 0.47
Nodes (3): AppDatabase, Context, RoomDatabase

### Community 104 - "LoadingState"
Cohesion: 0.47
Nodes (6): Error, Idle, Loading, LoadingState, Success, UiState

### Community 109 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 117 - "update.md"
Cohesion: 0.50
Nodes (3): For --cluster-only, For --update (incremental re-extraction), graphify reference: incremental update and cluster-only

## Knowledge Gaps
- **166 isolated node(s):** `CONFIG`, `Utils`, `name`, `description`, `type` (+161 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **34 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReportsViewModel` connect `ReportsViewModel` to `formatNumber`, `ShipDetailsScreen.kt`, `CargoInfo`, `ReportsRepository`, `ReportsCommonWidgets.kt`, `Ship`, `CargoEditSearchDialogsSection.kt`, `LoadingState`, `ReportModels.kt`, `QuotasListScreen.kt`, `RealTimeLoadingBottomSheet`, `ComprehensiveAnalyticsDialog.kt`, `AppModule.kt`, `QuotaManagementDialog.kt`, `ManageReportsScreen.kt`, `WarehouseDetailsScreen.kt`, `ApiService`?**
  _High betweenness centrality (0.069) - this node is a cross-community bridge._
- **Why does `UserPreferencesManager` connect `UserPreferencesManager` to `ChatScreen.kt`, `CoroutineScope`, `LoadingNotificationService`, `CargoInfoDetailsDialogSection.kt`, `UserManagementScreen.kt`, `ChatNotificationWorker.kt`, `MainActivity.kt`, `StartupViewModel`, `AppModule.kt`, `hashPassword`, `SelectInfoScreen.kt`, `ManageReportsScreen.kt`, `CargoDetailsScreen.kt`, `ProfileMenu.kt`, `CargoCounterScreen.kt`?**
  _High betweenness centrality (0.048) - this node is a cross-community bridge._
- **Why does `ApiService` connect `ApiService` to `CargoInfoDetailsDialogSection.kt`, `ReportsRepository`, `UserManagementScreen.kt`, `Ship`, `QuotaExistenceMultipleResponse`, `UpdateManager`, `InitialInfoScreen.kt`, `AppModule.kt`, `hashPassword`, `ChatRepository`, `CargoModels.kt`, `WarehouseDetailsScreen.kt`, `AuthModels.kt`?**
  _High betweenness centrality (0.042) - this node is a cross-community bridge._
- **What connects `CONFIG`, `Utils`, `name` to the rest of the system?**
  _166 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `UsersManager` be split into smaller, more focused modules?**
  _Cohesion score 0.06936026936026936 - nodes in this community are weakly interconnected._
- **Should `ReportsRepository` be split into smaller, more focused modules?**
  _Cohesion score 0.09915966386554621 - nodes in this community are weakly interconnected._
- **Should `ReportsCommonWidgets.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.12121212121212122 - nodes in this community are weakly interconnected._