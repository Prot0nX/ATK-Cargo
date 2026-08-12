# Graph Report - ATK-Cargo  (2026-08-12)

## Corpus Check
- 269 files · ~339,733 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2235 nodes · 4711 edges · 171 communities (127 shown, 44 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 96 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `ccc2d1b8`
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
- ShipCard.kt
- QuotaManagementDialog.kt
- QuotaWarningDialog.kt
- preprocessImage
- CargoRepository
- RetrofitClient
- SelectInfoScreen.kt
- ExportPdfUseCase
- UserManagementScreen.kt
- Database
- Logger
- ChatViewModel
- UserPreferencesManager
- InitialInfoScreen.kt
- Color.kt
- ۴. باگ‌ها
- AnalyticsController
- MessageType
- What You Must Do When Invoked
- CargoCounterScreen.kt
- UserRepository
- MainActivity.kt
- AuthModels.kt
- LoginScreen.kt
- CargoDetailsScreen.kt
- StartupViewModel
- VoucherDetailsDialogSection.kt
- QuotaDetailsScreen.kt
- secrets.cpp
- Context
- UserService
- QuotaAnalysisSection.kt
- ChatMessageEntity
- CargoCounterComponents.kt
- MessageBubble.kt
- composer.json
- SessionManager
- SessionManager
- ChatToolbar.kt
- ShipInfoSection.kt
- SecurityScreen.kt
- SessionRepository
- CargoRegistrationNavigation.kt
- AuthRepositoryImpl.kt
- ChatController
- WarehouseDetailsScreen.kt
- AppModule.kt
- ChatScreen.kt
- ReportsCommonWidgets.kt
- Request
- CargoViewModel.kt
- AppNotificationManager
- SessionService
- UserManagementDialogsSection.kt
- ActiveQuotasDialogSection.kt
- ProtectedProxy
- QuotaSelectionDialog.kt
- sortShips
- ShipSortingMode
- CargoCounterOperationScreen
- UtilityController
- ColorScheme.kt
- FontWeight
- QuotaEntryDialog.kt
- Config
- ChatNotificationWorker.kt
- proxy_generator.php
- NotificationActionReceiver.kt
- NavRoutes
- ApiService.kt
- GroupSortingMode
- AnimationManager
- MessageInputArea
- SearchBar.kt
- LoadingState
- JalaliDateUtils
- Intent
- gradlew
- ComprehensiveAnalyticsDialog.kt
- LoginScreen
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
- KoinComponent
- Exception
- Result
- InitialInfo
- AuthViewModel
- NavController
- ShipDetailsScreen.kt
- AndroidViewModel
- formatNumber
- Job
- StateFlow
- graphify reference: extra exports and benchmark
- ColorSelector
- QuotasListDialogs.kt
- graphify reference: query, path, explain
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
1. `ReportsViewModel` - 104 edges
2. `CargoViewModel` - 84 edges
3. `ApiService` - 55 edges
4. `CargoInfo` - 51 edges
5. `UserPreferencesManager` - 44 edges
6. `formatNumber()` - 41 edges
7. `AppApiController` - 40 edges
8. `UpdateManager` - 38 edges
9. `ActiveShipInfo` - 35 edges
10. `ReportsRepository` - 35 edges

## Surprising Connections (you probably didn't know these)
- `ManageReportsScreen()` --calls--> `QuotaDetails`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/ui/screens/ManageReportsScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt
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

## Communities (171 total, 44 thin omitted)

### Community 0 - "ATKCargoTheme"
Cohesion: 0.14
Nodes (11): ComponentStyles, Dimensions, Elevation, ATKCargoTheme, Motion, SemanticColors, AppShapes, Spacing (+3 more)

### Community 1 - "ManageReportsScreen.kt"
Cohesion: 0.07
Nodes (41): LoadingNotificationService, RealTimeLoadingData, ShiftInfo, AdvancedSearchDialog(), Modifier, SearchTabInfo, SearchTypeTabRow(), DialogHeader() (+33 more)

### Community 2 - "AppApiController"
Cohesion: 0.08
Nodes (3): AppApiController, CargoController, MicroCache

### Community 3 - "CargoInfo"
Cohesion: 0.08
Nodes (53): CargoInfo, CargoInfoRequest, CargoDetailsDialog(), Color, ImageVector, Modifier, ModernCargoDetailsGrid(), ModernCargoStatusSection() (+45 more)

### Community 4 - "UsersManager"
Cohesion: 0.07
Nodes (7): ApiManager, CONFIG, ModalManager, ThemeManager, UIManager, UsersManager, Utils

### Community 5 - "UpdateManager"
Cohesion: 0.07
Nodes (28): UpdateInfo, Completed, Downloading, DownloadProgress, DownloadState, Error, Idle, Context (+20 more)

### Community 6 - "ApiService"
Cohesion: 0.07
Nodes (15): ApiService, CargoInfoResponse, CargoInfoSearch, CargoSearchResponse, CargoStats, CheckExistenceResponse, ExistingCargo, LoadableTonnageResponse (+7 more)

### Community 8 - "ClassLoader"
Cohesion: 0.05
Nodes (4): Composer\Semver\VersionParser, ComposerStaticInit5f285832cbbfb0bffdb861048ac414db, ClassLoader, InstalledVersions

### Community 9 - "AuthController"
Cohesion: 0.10
Nodes (4): AuthController, Response, PermissionService, InputValidator

### Community 10 - "ReportModels.kt"
Cohesion: 0.07
Nodes (25): AndroidViewModel, ApiResponse2, ThirdPartyApiService, AnalyticsData, DateInfo, ExistingQuota, ExitDateInfo, QuotaCompletionAnalysis (+17 more)

### Community 12 - "SecurityVerifier"
Cohesion: 0.09
Nodes (11): Secrets, SecurityErrorType, LICENSE_INACTIVE, LICENSE_NOT_FOUND, NETWORK_ERROR, TAMPERED, UNKNOWN_ERROR, SecurityVerifier (+3 more)

### Community 13 - "QuotasListScreen.kt"
Cohesion: 0.18
Nodes (28): Quota, QuotaEditData, QuotaPercentageData, buildQuotasShareText(), shareQuotasData(), DialogHeader(), QuotaPercentageDialog(), ActionButton() (+20 more)

### Community 14 - "ReportsRepository"
Cohesion: 0.09
Nodes (12): ComprehensiveAnalysisResponse, QuotaStatusResponse, ErrorResponse, HttpStatusException, CargoInfo, ReportsRepository, CargoInfoResponse, Exception (+4 more)

### Community 15 - "ChatRepository"
Cohesion: 0.12
Nodes (9): ChatMessage, ChatMessagesResponse, SendMessageRequest, SendMessageResponse, UnreadCountResponse, ChatRepository, com, Flow (+1 more)

### Community 16 - "ShipCard.kt"
Cohesion: 0.25
Nodes (13): ErrorState(), ImageVector, Modifier, ImageVector, Modifier, ShipCard(), ShipCardContent(), ShipSortingModeButton() (+5 more)

### Community 17 - "QuotaManagementDialog.kt"
Cohesion: 0.16
Nodes (30): QuotaItem, AdvancedFiltersDialog(), AnalyticsStatChipMini(), CompactStatChip(), EmptyQuotaState(), FilterOptionsCard(), InfoChip(), IntegratedQuotaCard() (+22 more)

### Community 18 - "QuotaWarningDialog.kt"
Cohesion: 0.26
Nodes (16): WarningStatus, CompactInfoSection(), DialogHeader(), Color, ImageVector, Modifier, LoadingActionButton(), NavigationButton() (+8 more)

### Community 19 - "preprocessImage"
Cohesion: 0.13
Nodes (22): adaptiveThresholding(), applyContrastEnhancement(), enhanceEdges(), ImageProxy, InputImage, medianFilter(), preprocessImage(), EnhancedNumberAnalyzer (+14 more)

### Community 20 - "CargoRepository"
Cohesion: 0.08
Nodes (3): InvalidArgumentException, CargoRepository, CargoService

### Community 21 - "RetrofitClient"
Cohesion: 0.15
Nodes (13): FloatTypeAdapter, Context, RetrofitClient, ThirdPartyRetrofitClient, CryptoManager, ByteArray, Cipher, JsonReader (+5 more)

### Community 22 - "SelectInfoScreen.kt"
Cohesion: 0.17
Nodes (26): formatNumber(), ActiveShipInfo, ShipSelectionDialog(), EmptySearchResult(), extractLastDigits(), FilterChip(), FilterState, ALL (+18 more)

### Community 23 - "ExportPdfUseCase"
Cohesion: 0.26
Nodes (10): ExportPdfUseCase, PdfColorScheme, PdfFonts, PersianFontManager, ShamsiDate, Document, Font, PdfPCell (+2 more)

### Community 24 - "UserManagementScreen.kt"
Cohesion: 0.20
Nodes (19): DeleteUserRequest, ForceLogoutRequest, User, EnhancedDeleteConfirmationDialog(), EnhancedForceLogoutDialog(), DashboardSummaryRow(), EmptyStateView(), EnterpriseUserCard() (+11 more)

### Community 25 - "Database"
Cohesion: 0.13
Nodes (8): mysqli, mysqli_stmt, PDO, PDOException, Database, PDO, self, DatabaseManager

### Community 26 - "Logger"
Cohesion: 0.14
Nodes (4): LicenseController, OnlineUsersController, Logger, self

### Community 27 - "ChatViewModel"
Cohesion: 0.10
Nodes (7): ChatViewModel, ChatViewModelFactory, com, StateFlow, T, ViewModel, ViewModelProvider

### Community 28 - "UserPreferencesManager"
Cohesion: 0.05
Nodes (30): AtkCargoApplication, BootReceiver, BroadcastReceiver, Context, Intent, KoinComponent, TypeToken, Job (+22 more)

### Community 29 - "InitialInfoScreen.kt"
Cohesion: 0.06
Nodes (60): MainScreen(), RouteTransitions, standardTransitions(), CheckExistenceRequest, MenuItem, formatNumber(), isValidPersianText(), isValidQuotaNumber() (+52 more)

### Community 30 - "Color.kt"
Cohesion: 0.18
Nodes (21): CargoOperationScreen(), NavController, AnimatedIcon(), DialogContent(), DialogPassword(), DuplicateConfirmationDialog(), DuplicateTrackingNumbersDialog(), ExitStatusDialog() (+13 more)

### Community 31 - "۴. باگ‌ها"
Cohesion: 0.05
Nodes (41): 🔴 A-1 | نبود کامل احراز هویت روی endpoint (بحرانی), 🟠 A-2 | نبود هدرهای کنترل کش روی پاسخ, 🟠 A-3 | نبود اعتبارسنجی/کلمپ روی `shiftOffset`, 🟡 A-4 | Rate-limit مشترک و مبتنی بر IP, 🟡 A-5 | نشتی اطلاعات در پیام خطا, 🟡 B-10 | فیلد مرده در مدل, 🟡 B-11 | `LEFT JOIN` که در عمل `INNER JOIN` است, 🟡 B-12 | فیلتر `isActive` اعمال نمی‌شود (+33 more)

### Community 32 - "AnalyticsController"
Cohesion: 0.14
Nodes (16): App\Core\Database, App\Core\Logger, App\Core\MicroCache, App\Core\Request, App\Services\SessionService, Logger, gregorian_to_jalali(), jalali_to_gregorian() (+8 more)

### Community 33 - "MessageType"
Cohesion: 0.16
Nodes (14): Modifier, StatusSnackbar(), MessageType, ERROR, SUCCESS, WARNING, CargoSnackbarQueue, StateFlow (+6 more)

### Community 34 - "What You Must Do When Invoked"
Cohesion: 0.07
Nodes (26): For /graphify add and --watch, For /graphify query, For the commit hook and native CLAUDE.md integration, For --update and --cluster-only, /graphify, Honesty Rules, Interpreter guard for subcommands, Part A - Structural extraction for code files (+18 more)

### Community 35 - "CargoCounterScreen.kt"
Cohesion: 0.19
Nodes (18): CargoCounterScreen(), CargoCounterViewModel, filterShipsByTab(), com, NavController, SnackbarHostState, StateFlow, ViewModel (+10 more)

### Community 37 - "MainActivity.kt"
Cohesion: 0.25
Nodes (5): ServerSyncingScreen(), Intent, MainActivity, Bundle, ComponentActivity

### Community 38 - "AuthModels.kt"
Cohesion: 0.14
Nodes (9): ActiveSessionResponse, ForceLogoutResponse, LogoutRequest, LogoutResponse, PasswordCheckResponse, PermissionSyncRequest, PermissionSyncResponse, Result (+1 more)

### Community 39 - "LoginScreen.kt"
Cohesion: 0.24
Nodes (16): CompactLoginLayout(), ExpandedLoginLayout(), IndustrialBrandingHeaderContent(), IndustrialFeatureBadge(), IndustrialFooterInfo(), IndustrialInputField(), IndustrialLoginFormCard(), androidx (+8 more)

### Community 40 - "CargoDetailsScreen.kt"
Cohesion: 0.18
Nodes (17): Result, validateServerSession(), CargoDetailsScreen(), confirmCargo(), FloatingActionButtonItem(), handleCargoConfirmation(), handleQuotaChangeInDetails(), Color (+9 more)

### Community 41 - "StartupViewModel"
Cohesion: 0.09
Nodes (16): AndroidViewModel, Intent, StateFlow, Ready, RequestBatteryOptimization, SecurityBlocked, SecurityCheckState, ShowMessage (+8 more)

### Community 42 - "VoucherDetailsDialogSection.kt"
Cohesion: 0.17
Nodes (20): VoucherDetail, persianDateFormat(), DateTimePicker(), Modifier, PersianDateItem(), TimePickerDialog(), EmptyVoucherList(), Modifier (+12 more)

### Community 43 - "QuotaDetailsScreen.kt"
Cohesion: 0.26
Nodes (13): ApiQuotaDetails, adjustColorForTheme(), Color, toTon(), calculatePercentage(), calculateProgress(), ProgressBar(), QuotaAdditionalInfo() (+5 more)

### Community 44 - "secrets.cpp"
Cohesion: 0.41
Nodes (17): decryptXor(), Java_com_atk_atk_1cargo_api_Secrets_getApiKey(), Java_com_atk_atk_1cargo_api_Secrets_getAuthenticationX365(), Java_com_atk_atk_1cargo_api_Secrets_getAuthUser(), Java_com_atk_atk_1cargo_api_Secrets_getBaseUrl(), Java_com_atk_atk_1cargo_api_Secrets_getExpectedSignatureHash(), Java_com_atk_atk_1cargo_api_Secrets_getLicenseCheckUrl(), Java_com_atk_atk_1cargo_api_Secrets_getLicenseInfoUrl() (+9 more)

### Community 47 - "QuotaAnalysisSection.kt"
Cohesion: 0.28
Nodes (16): QuotaCompletionData, QuotaGroupingMode, BY_CARGO_OWNER, BY_CARRIER, BY_SHIP, AnalyticsGroupingModeButton(), AnalyticsOwnerSummaryCard(), AnalyticsQuotaCard() (+8 more)

### Community 48 - "ChatMessageEntity"
Cohesion: 0.14
Nodes (3): ChatDao, Flow, ChatMessageEntity

### Community 49 - "CargoCounterComponents.kt"
Cohesion: 0.37
Nodes (12): CargoSnackbarMessage, GroupedShipList(), Color, Modifier, QuotaCard(), ShipGroup(), ShipHeader(), snackbarAccent() (+4 more)

### Community 50 - "MessageBubble.kt"
Cohesion: 0.27
Nodes (13): extractShipInfoAndText(), com, replaceUsernamesWithFullNames(), ShipInfoModel, ChatInfoRow(), EditMessageDialog(), androidx, Color (+5 more)

### Community 51 - "composer.json"
Cohesion: 0.12
Nodes (15): autoload, autoload-dev, psr-4, psr-4, config, optimize-autoloader, sort-packages, description (+7 more)

### Community 54 - "ChatToolbar.kt"
Cohesion: 0.26
Nodes (12): ColorWheel(), Color, Modifier, AdvancedColorPickerDialog(), ChatSettingsDialogEnhanced(), ChatSettingsPreviewRefined(), Color, ImageVector (+4 more)

### Community 55 - "ShipInfoSection.kt"
Cohesion: 0.30
Nodes (13): ShipInfo, formatNumber(), toEnglishNumbers(), DetailedInfoGrid(), DetailInfoItem(), ExpandedContent(), Color, ImageVector (+5 more)

### Community 56 - "SecurityScreen.kt"
Cohesion: 0.25
Nodes (13): AdvancedOrbitalScanner(), AnimatedUpdateIcon(), AnimatedWarningIcon(), DynamicPremiumBackground(), InteractiveExitButton(), Modifier, RetryButton(), SecurityBlockScreen() (+5 more)

### Community 58 - "CargoRegistrationNavigation.kt"
Cohesion: 0.60
Nodes (4): CargoRegistrationRoute, cargoRegistrationScreen(), NavController, navigateToCargoRegistration()

### Community 59 - "AuthRepositoryImpl.kt"
Cohesion: 0.24
Nodes (5): LoginRequest, LoginResponse, SessionResponse, AuthRepositoryImpl, Flow

### Community 61 - "WarehouseDetailsScreen.kt"
Cohesion: 0.29
Nodes (12): FilteredSummary, Warehouse, ExportOptions(), Modifier, QuotaChip(), QuotaSelector(), VoucherDetailsButton(), WarehouseCard() (+4 more)

### Community 62 - "AppModule.kt"
Cohesion: 0.16
Nodes (10): AppDatabase, Context, AuthRepository, ConflictSession, Error, Flow, LoginResult, Success (+2 more)

### Community 63 - "ChatScreen.kt"
Cohesion: 0.26
Nodes (13): ChatUiItem, getAdaptiveBubbleColor(), getChatBackgroundColor(), getDateHeaderColor(), Header, Color, Message, ChatScreen() (+5 more)

### Community 64 - "ReportsCommonWidgets.kt"
Cohesion: 0.29
Nodes (14): FabItem, CompactStatChip(), EmptyQuotaState(), EmptyShipsState(), EmptyStateCard(), ErrorStateCard(), FloatingActionButton(), InfoCard() (+6 more)

### Community 66 - "CargoViewModel.kt"
Cohesion: 0.33
Nodes (5): CargoViewModelFactory, StateFlow, T, ViewModel, ViewModelProvider

### Community 69 - "UserManagementDialogsSection.kt"
Cohesion: 0.17
Nodes (15): CreateUserRequest, UpdateUserRequest, UserTypeInfo, DesignTextField(), EnhancedAddUserDialog(), EnhancedEditUserDialog(), getUserTypes(), androidx (+7 more)

### Community 70 - "ActiveQuotasDialogSection.kt"
Cohesion: 0.29
Nodes (11): ActiveQuotasDialog(), FilterBar(), Color, Dp, Modifier, QuotasHeader(), StatItem(), VerticalDivider() (+3 more)

### Community 72 - "QuotaSelectionDialog.kt"
Cohesion: 0.42
Nodes (10): MatchingQuota, DialogHeader(), findDifferentFields(), InactiveIndicator(), ImageVector, QuotaDetailItem(), QuotaDetails(), QuotaHeader() (+2 more)

### Community 74 - "ShipSortingMode"
Cohesion: 0.25
Nodes (7): ShipSortingMode, LOADED_TONNAGE_ASC, LOADED_TONNAGE_DESC, NAME_ASC, NAME_DESC, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC

### Community 75 - "CargoCounterOperationScreen"
Cohesion: 0.36
Nodes (6): CargoCounterRoute, cargoCounterScreen(), NavController, navigateToCargoCounter(), CargoCounterOperationScreen(), NavController

### Community 77 - "ColorScheme.kt"
Cohesion: 0.80
Nodes (5): buildAppDarkColorScheme(), buildAppLightColorScheme(), compositeOver(), Color, ColorScheme

### Community 78 - "FontWeight"
Cohesion: 0.07
Nodes (39): ConfirmationDialog(), ImageVector, DateClickableField(), DateRangePicker(), getDaysInPersianMonth(), isPersianLeapYear(), Color, Modifier (+31 more)

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

### Community 85 - "ApiService.kt"
Cohesion: 0.17
Nodes (5): ApiResponse, DeleteMessageRequest, EditMessageRequest, RealTimeDataResponse, JsonElement

### Community 86 - "GroupSortingMode"
Cohesion: 0.40
Nodes (4): GroupSortingMode, ALPHABETICAL, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC

### Community 88 - "MessageInputArea"
Cohesion: 0.52
Nodes (6): rotateIcon(), Color, com, MessageInputArea(), QuotaSelectionDialog(), ShipSelectionDialog()

### Community 90 - "LoadingState"
Cohesion: 0.47
Nodes (6): Error, Idle, Loading, LoadingState, Success, UiState

### Community 93 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 94 - "ComprehensiveAnalyticsDialog.kt"
Cohesion: 0.57
Nodes (6): AnalyticsDateNavigation(), AnalyticsHeaderCard(), ComprehensiveAnalyticsDialog(), EmptyStateCard(), ErrorStateCard(), Modifier

### Community 95 - "LoginScreen"
Cohesion: 0.29
Nodes (7): LoginScreen(), AdaptiveLayoutConfig, rememberAdaptiveLayoutConfig(), WindowSizeClass, COMPACT, EXPANDED, MEDIUM

### Community 146 - "SplashScreen.kt"
Cohesion: 0.53
Nodes (5): SplashScreen(), SplashScreenConstants, SplashScreenContent(), SplashSubtitleBadge(), SplashVersionBadge()

### Community 150 - "InitialInfo"
Cohesion: 0.13
Nodes (18): InitialInfo, QuotaValidationResult, CargoInfoCard(), CargoListSection(), GroupStats(), InfoChips(), InfoGrid(), InfoGridCard() (+10 more)

### Community 151 - "AuthViewModel"
Cohesion: 0.13
Nodes (11): AuthViewModel, Error, Idle, StateFlow, ViewModel, Loading, LoginUiState, Success (+3 more)

### Community 153 - "ShipDetailsScreen.kt"
Cohesion: 0.30
Nodes (10): Ship, QuotaWarningThresholds, calculateWarningStatus(), ErrorStateCard(), ImageVector, ShipDetails(), ShipHeaderCard(), TabItem() (+2 more)

### Community 155 - "formatNumber"
Cohesion: 0.40
Nodes (9): addOneDayToPersianDate(), format(), formatHoursToPersian(), formatNumber(), formatWeightWithDetail(), getDaysInPersianMonth(), isPersianLeapYear(), Context (+1 more)

### Community 159 - "graphify reference: extra exports and benchmark"
Cohesion: 0.22
Nodes (8): graphify reference: extra exports and benchmark, Step 6b - Wiki (only if --wiki flag), Step 7 - Neo4j export (only if --neo4j or --neo4j-push flag), Step 7a - FalkorDB export (only if --falkordb or --falkordb-push flag), Step 7b - SVG export (only if --svg flag), Step 7c - GraphML export (only if --graphml flag), Step 7d - MCP server (only if --mcp flag), Step 8 - Token reduction benchmark (only if total_words > 5000)

### Community 161 - "ColorSelector"
Cohesion: 0.39
Nodes (3): adjustColorForTheme(), ColorSelector, Color

### Community 162 - "QuotasListDialogs.kt"
Cohesion: 0.53
Nodes (5): DeleteQuotaDialog(), androidx, Color, QuotaConfirmDialog(), ToggleQuotaStatusDialog()

### Community 165 - "graphify reference: query, path, explain"
Cohesion: 0.40
Nodes (5): For /graphify explain, For /graphify path, graphify reference: query, path, explain, Step 0 — Constrained query expansion (REQUIRED before traversal), Step 1 — Traversal

### Community 167 - "graphify reference: add a URL and watch a folder"
Cohesion: 0.50
Nodes (3): For /graphify add, For --watch, graphify reference: add a URL and watch a folder

### Community 168 - "graphify reference: commit hook and native CLAUDE.md integration"
Cohesion: 0.50
Nodes (3): For git commit hook, For native CLAUDE.md integration, graphify reference: commit hook and native CLAUDE.md integration

## Knowledge Gaps
- **173 isolated node(s):** `ExitDateInfo`, `QuotaStatusDetails`, `ExistingQuota`, `DateInfo`, `AnalyticsData` (+168 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **44 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReportsRepository` connect `ReportsRepository` to `ManageReportsScreen.kt`, `CargoViewModel.kt`, `CargoDetailsScreen.kt`, `ReportModels.kt`, `InitialInfo`, `SelectInfoScreen.kt`, `InitialInfoScreen.kt`, `AppModule.kt`?**
  _High betweenness centrality (0.130) - this node is a cross-community bridge._
- **Why does `UserPreferencesManager` connect `UserPreferencesManager` to `CargoViewModel.kt`, `CargoCounterScreen.kt`, `MainActivity.kt`, `AuthModels.kt`, `CargoDetailsScreen.kt`, `StartupViewModel`, `ChatNotificationWorker.kt`, `SelectInfoScreen.kt`, `UserManagementScreen.kt`, `AuthRepositoryImpl.kt`, `InitialInfoScreen.kt`, `AppModule.kt`, `ChatScreen.kt`?**
  _High betweenness centrality (0.087) - this node is a cross-community bridge._
- **Why does `ReportsViewModel` connect `ReportsViewModel` to `ManageReportsScreen.kt`, `ColorSelector`, `CargoInfo`, `ReportModels.kt`, `QuotaDetailsScreen.kt`, `ShipSortingMode`, `QuotasListScreen.kt`, `QuotaAnalysisSection.kt`, `ShipCard.kt`, `QuotaManagementDialog.kt`, `QuotaWarningDialog.kt`, `WarehouseDetailsScreen.kt`, `GroupSortingMode`, `ComprehensiveAnalyticsDialog.kt`, `ShipDetailsScreen.kt`, `LoadingState`, `InitialInfoScreen.kt`, `AppModule.kt`?**
  _High betweenness centrality (0.087) - this node is a cross-community bridge._
- **What connects `ExitDateInfo`, `QuotaStatusDetails`, `ExistingQuota` to the rest of the system?**
  _173 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `ATKCargoTheme` be split into smaller, more focused modules?**
  _Cohesion score 0.1368421052631579 - nodes in this community are weakly interconnected._
- **Should `ManageReportsScreen.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.06896551724137931 - nodes in this community are weakly interconnected._
- **Should `AppApiController` be split into smaller, more focused modules?**
  _Cohesion score 0.08333333333333333 - nodes in this community are weakly interconnected._