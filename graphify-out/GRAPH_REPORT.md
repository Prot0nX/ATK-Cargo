# Graph Report - ATK-Cargo  (2026-08-16)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 2281 nodes · 4823 edges · 164 communities (128 shown, 36 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 97 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `3f9ce3cb`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- CargoInfoDetailsDialogSection.kt
- HomeScreen.kt
- UpdateManager
- CargoViewModel
- LoginScreen.kt
- UsersManager
- ReportsViewModel
- ExportPdfUseCase
- ClassLoader
- Color.kt
- AppApiController
- AuthController
- SecurityVerifier
- QuotasListScreen.kt
- گزارش جامع بازبینی `app/proguard-rules.pro` — ATK-Cargo
- CargoRepository
- StartupViewModel
- ReportsRepository
- ATKCargoTheme
- UserPreferencesManager
- Database
- ApiService
- QuotaManagementDialog.kt
- SessionService
- ReportModels.kt
- Exception
- QuotaWarningDialog.kt
- What You Must Do When Invoked
- AnalyticsController
- FontWeight
- UserManagementScreen.kt
- ChatRepository
- QuotaAnalysisSection.kt
- ChatViewModel
- InitialInfoScreen.kt
- Logger
- secrets.cpp
- MessageType
- CargoCounterScreen.kt
- AuthModels.kt
- ManageReportsScreen.kt
- ActiveQuotasDialogSection.kt
- CargoModels.kt
- ChatMessageEntity
- SelectInfoScreen.kt
- Ship
- LoadingNotificationService
- CargoInfo
- CargoController
- formatNumber
- hashPassword
- SessionRepository
- CargoDetailsScreen.kt
- composer.json
- SessionManager
- SessionManager
- CargoDetailsComponents.kt
- ChatScreen.kt
- ChatToolbar.kt
- ActiveShipInfo
- CargoDetailsDialogSection.kt
- MessageBubble.kt
- ChatController
- WarehouseDetailsScreen.kt
- Request
- SaveOrUpdateResponse
- CoroutineScope
- UserManagementDialogsSection.kt
- ProtectedProxy
- QuotaSelectionDialog.kt
- SearchDialogs.kt
- DataModel.kt
- RealTimeLoadingBottomSheet
- DatabaseSchemaExporter
- PermissionPoller
- preprocessImage
- ShipsListScreen.kt
- graphify reference: extra exports and benchmark
- MainScreen.kt
- QuotaValidationUseCase.kt
- RealTimeLoadingData
- CargoEntryNavigation.kt
- QuotaEntryDialog.kt
- ComprehensiveAnalyticsDialog.kt
- DateRangePicker.kt
- PasswordGateService
- MessageInputArea
- ProfileMenu.kt
- ReportsNavigation.kt
- proxy_generator.php
- CargoRegistrationNavigation.kt
- NavRoutes
- HomeNotificationSettingRow.kt
- LoadingState
- JalaliDateUtils
- ChatNavigation.kt
- AnimationManager
- graphify reference: query, path, explain
- gradlew
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
- `CargoCounterScreen()` --calls--> `ColorSelector`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_counter/presentation/CargoCounterScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt
- `SelectInfoScreenContent()` --calls--> `ColorSelector`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/SelectInfoScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt
- `QuotasDialog()` --calls--> `ColorSelector`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/reports/presentation/quota_details/QuotaDetailsScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt
- `CargoDetailsScreen()` --calls--> `CargoViewModelFactory`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_details/presentation/CargoDetailsScreen.kt → app/src/main/java/com/atk/atk_cargo/ui/viewmodel/CargoViewModel.kt
- `validateServerSession()` --calls--> `SessionCheckRequest`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/api/SessionValidator.kt → app/src/main/java/com/atk/atk_cargo/data/model/AuthModels.kt

## Import Cycles
- None detected.

## Communities (164 total, 36 thin omitted)

### Community 0 - "CargoInfoDetailsDialogSection.kt"
Cohesion: 0.06
Nodes (70): ShipInfo, formatNumber(), toEnglishNumbers(), EnhancedNumberAnalyzer, extractNumber(), ImageProxy, InputImage, recognizeTextFromImage() (+62 more)

### Community 1 - "HomeScreen.kt"
Cohesion: 0.23
Nodes (13): MenuItem, getMenuItemsForUserType(), CategorizedMenuGrid(), CategoryHeader(), CompactMenuCard(), Header(), HomeScreen(), isWideCard() (+5 more)

### Community 2 - "UpdateManager"
Cohesion: 0.05
Nodes (37): UpdateInfo, FloatTypeAdapter, Context, RetrofitClient, ThirdPartyRetrofitClient, Completed, Downloading, DownloadProgress (+29 more)

### Community 3 - "CargoViewModel"
Cohesion: 0.07
Nodes (20): CargoCounterRoute, cargoCounterScreen(), NavController, navigateToCargoCounter(), CargoCounterOperationScreen(), NavController, CargoViewModel, CargoViewModelFactory (+12 more)

### Community 4 - "LoginScreen.kt"
Cohesion: 0.06
Nodes (39): Modifier, SearchBar(), AppDatabase, Context, AuthRepository, ConflictSession, Error, Flow (+31 more)

### Community 5 - "UsersManager"
Cohesion: 0.07
Nodes (7): ApiManager, CONFIG, ModalManager, ThemeManager, UIManager, UsersManager, Utils

### Community 6 - "ReportsViewModel"
Cohesion: 0.09
Nodes (3): ComprehensiveAnalytics, RealTimeUiState, ReportsViewModel

### Community 7 - "ExportPdfUseCase"
Cohesion: 0.10
Nodes (33): FilteredSummary, VoucherDetail, ExportPdfUseCase, PdfColorScheme, PdfFonts, PersianFontManager, ShamsiDate, persianDateFormat() (+25 more)

### Community 8 - "ClassLoader"
Cohesion: 0.05
Nodes (4): Composer\Semver\VersionParser, ComposerStaticInit5f285832cbbfb0bffdb861048ac414db, ClassLoader, InstalledVersions

### Community 9 - "Color.kt"
Cohesion: 0.09
Nodes (28): AdvancedSearchDialog(), Modifier, SearchTabInfo, SearchTypeTabRow(), ServerSyncingScreen(), SplashScreen(), SplashScreenConstants, SplashScreenContent() (+20 more)

### Community 11 - "AuthController"
Cohesion: 0.07
Nodes (5): AuthController, UserController, Response, UserService, InputValidator

### Community 12 - "SecurityVerifier"
Cohesion: 0.09
Nodes (11): Secrets, SecurityErrorType, LICENSE_INACTIVE, LICENSE_NOT_FOUND, NETWORK_ERROR, TAMPERED, UNKNOWN_ERROR, SecurityVerifier (+3 more)

### Community 13 - "QuotasListScreen.kt"
Cohesion: 0.16
Nodes (26): QuotaEditData, QuotaPercentageData, DeleteQuotaDialog(), androidx, Color, QuotaConfirmDialog(), ToggleQuotaStatusDialog(), DialogHeader() (+18 more)

### Community 14 - "گزارش جامع بازبینی `app/proguard-rules.pro` — ATK-Cargo"
Cohesion: 0.05
Nodes (36): C1 — قوانین `-keep` بیش‌ازحد باز، obfuscation را روی حساس‌ترین کد خنثی می‌کنند, C2 — keep کامل روی `retrofit2` و `okhttp3` — ٪۳۵ کل seedها، shrinking صفر, C3 — `-keepattributes !SourceFile,!LineNumberTable` معنایی معکوس دارد, H1 — حذف null-checkهای Kotlin: تبدیل fail-fast به رفتار تعریف‌نشده, H2 — حذف `Log.e` و `printStackTrace` = نابینایی کامل در production, H3 — سکشن ۹ (Koin) کاملاً کد مرده است, H4 — iText: هم ریسک runtime، هم مشکل لایسنس, H5 — `proguardFiles` دوبار اعلام شده (+28 more)

### Community 15 - "CargoRepository"
Cohesion: 0.06
Nodes (9): App\Core\Database, App\Core\Logger, App\Core\MicroCache, App\Exceptions\ApiException, App\Exceptions\ConflictException, InvalidArgumentException, Logger, CargoRepository (+1 more)

### Community 16 - "StartupViewModel"
Cohesion: 0.05
Nodes (25): AppNotificationManager, AuthSession, BroadcastReceiver, Context, Intent, NotificationActionReceiver, AndroidViewModel, Flow (+17 more)

### Community 17 - "ReportsRepository"
Cohesion: 0.11
Nodes (11): CargoInfoResponse, ComprehensiveAnalysisResponse, QuotaDetails, QuotaStatusResponse, RealTimeDataResponse, ShipsData, ErrorResponse, HttpStatusException (+3 more)

### Community 18 - "ATKCargoTheme"
Cohesion: 0.09
Nodes (22): AdaptiveLayoutConfig, rememberAdaptiveLayoutConfig(), WindowSizeClass, COMPACT, EXPANDED, MEDIUM, buildAppDarkColorScheme(), buildAppLightColorScheme() (+14 more)

### Community 19 - "UserPreferencesManager"
Cohesion: 0.10
Nodes (8): TypeToken, Flow, T, UserPreferencesManager, ThemeColorOption, ThemeColorPickerRow(), DataStore, Preferences

### Community 20 - "Database"
Cohesion: 0.10
Nodes (9): mysqli, PDO, PDOException, LicenseController, Config, self, Database, PDO (+1 more)

### Community 21 - "ApiService"
Cohesion: 0.09
Nodes (8): ApiResponse, ApiResponse2, ApiService, DeleteMessageRequest, EditMessageRequest, SuccessResponse, FilteredSummaryResponse, JsonElement

### Community 22 - "QuotaManagementDialog.kt"
Cohesion: 0.16
Nodes (30): QuotaItem, AdvancedFiltersDialog(), AnalyticsStatChipMini(), CompactStatChip(), EmptyQuotaState(), FilterOptionsCard(), InfoChip(), IntegratedQuotaCard() (+22 more)

### Community 23 - "SessionService"
Cohesion: 0.09
Nodes (5): enforceMinAppVersion(), requireAuthenticatedSession(), UserRepository, PermissionService, SessionService

### Community 24 - "ReportModels.kt"
Cohesion: 0.06
Nodes (29): ThirdPartyApiService, adjustColorForTheme(), AnalyticsData, ColorSelector, DateInfo, ExistingQuota, ExitDateInfo, GroupSortingMode (+21 more)

### Community 25 - "Exception"
Cohesion: 0.13
Nodes (6): Exception, mysqli_stmt, DatabaseManager, ApiException, ConflictException, DatabaseException

### Community 26 - "QuotaWarningDialog.kt"
Cohesion: 0.26
Nodes (16): WarningStatus, CompactInfoSection(), DialogHeader(), Color, ImageVector, Modifier, LoadingActionButton(), NavigationButton() (+8 more)

### Community 27 - "What You Must Do When Invoked"
Cohesion: 0.07
Nodes (26): For /graphify add and --watch, For /graphify query, For the commit hook and native CLAUDE.md integration, For --update and --cluster-only, /graphify, Honesty Rules, Interpreter guard for subcommands, Part A - Structural extraction for code files (+18 more)

### Community 28 - "AnalyticsController"
Cohesion: 0.18
Nodes (10): gregorian_to_jalali(), jalali_to_gregorian(), jcheckdate(), jdate(), jdate_words(), jgetdate(), jmktime(), jstrftime() (+2 more)

### Community 29 - "FontWeight"
Cohesion: 0.07
Nodes (44): FabItem, ConfirmationDialog(), ImageVector, EmptyState(), ImageVector, Modifier, Color, ImageVector (+36 more)

### Community 30 - "UserManagementScreen.kt"
Cohesion: 0.16
Nodes (21): DeleteUserRequest, ForceLogoutRequest, UpdateUserRequest, User, EnhancedDeleteConfirmationDialog(), EnhancedForceLogoutDialog(), DashboardSummaryRow(), EmptyStateView() (+13 more)

### Community 31 - "ChatRepository"
Cohesion: 0.12
Nodes (9): ChatMessage, ChatMessagesResponse, SendMessageRequest, SendMessageResponse, UnreadCountResponse, ChatRepository, com, Flow (+1 more)

### Community 32 - "QuotaAnalysisSection.kt"
Cohesion: 0.19
Nodes (21): QuotaCompletionData, QuotaGroupingMode, BY_CARGO_OWNER, BY_CARRIER, BY_SHIP, buildQuotaGroups(), QuotaGroup, AnalyticsGroupingModeButton() (+13 more)

### Community 33 - "ChatViewModel"
Cohesion: 0.10
Nodes (7): ChatViewModel, ChatViewModelFactory, com, StateFlow, T, ViewModel, ViewModelProvider

### Community 34 - "InitialInfoScreen.kt"
Cohesion: 0.23
Nodes (21): formatNumber(), isValidPersianText(), isValidQuotaNumber(), isValidShipName(), isValidWarehouseName(), isValidWeight(), ConfirmationDialog(), CustomInput() (+13 more)

### Community 35 - "Logger"
Cohesion: 0.11
Nodes (5): App\Core\AuthenticatesRequests, OnlineUsersController, UtilityController, Logger, self

### Community 36 - "secrets.cpp"
Cohesion: 0.31
Nodes (20): decryptXor(), JNI_OnLoad(), n0(), n1(), n10(), n11(), n2(), n3() (+12 more)

### Community 37 - "MessageType"
Cohesion: 0.17
Nodes (14): Modifier, StatusSnackbar(), MessageType, ERROR, SUCCESS, WARNING, CargoSnackbarQueue, StateFlow (+6 more)

### Community 38 - "CargoCounterScreen.kt"
Cohesion: 0.16
Nodes (19): Result, validateServerSession(), CargoCounterScreen(), CargoCounterViewModel, filterShipsByTab(), com, NavController, SnackbarHostState (+11 more)

### Community 39 - "AuthModels.kt"
Cohesion: 0.12
Nodes (11): ActiveSessionResponse, ForceLogoutResponse, LogoutRequest, LogoutResponse, PasswordCheckResponse, PermissionSyncRequest, PermissionSyncResponse, SessionCheckRequest (+3 more)

### Community 40 - "ManageReportsScreen.kt"
Cohesion: 0.29
Nodes (14): QuotasDialog(), addOneDayToPersianDate(), DatePickerDialog(), DateTimeSelectionCard(), getDaysInPersianMonth(), isPersianLeapYear(), Color, ImageVector (+6 more)

### Community 41 - "ActiveQuotasDialogSection.kt"
Cohesion: 0.18
Nodes (18): ActiveQuotasDialog(), FilterBar(), Color, Dp, Modifier, QuotasHeader(), StatItem(), VerticalDivider() (+10 more)

### Community 42 - "CargoModels.kt"
Cohesion: 0.09
Nodes (14): CargoDeleteResponse, CargoInfoRequest, CargoInfoSearch, CargoSearchResponse, CargoStats, CheckExistenceRequest, CheckExistenceResponse, ExistingCargo (+6 more)

### Community 43 - "ChatMessageEntity"
Cohesion: 0.14
Nodes (3): ChatDao, Flow, ChatMessageEntity

### Community 44 - "SelectInfoScreen.kt"
Cohesion: 0.22
Nodes (19): formatNumber(), extractLastDigits(), FilterChip(), FlatQuotaCard(), GroupedShipList(), Color, Modifier, NavController (+11 more)

### Community 45 - "Ship"
Cohesion: 0.19
Nodes (15): Ship, ShipSortingMode, LOADED_TONNAGE_ASC, LOADED_TONNAGE_DESC, NAME_ASC, NAME_DESC, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC (+7 more)

### Community 46 - "LoadingNotificationService"
Cohesion: 0.22
Nodes (6): Context, Intent, KoinComponent, LoadingNotificationService, IBinder, Service

### Community 47 - "CargoInfo"
Cohesion: 0.23
Nodes (18): CargoInfo, FormSection(), CargoChangesPreview(), CargoEditConfirmDialog(), CargoEditConfirmHeader(), CargoMainInfo(), CargoShippingInfo(), CargoTimeInfo() (+10 more)

### Community 49 - "formatNumber"
Cohesion: 0.14
Nodes (30): ApiQuotaDetails, Quota, buildQuotasShareText(), calculatePercentage(), calculateProgress(), format(), formatNumber(), formatWeightWithDetail() (+22 more)

### Community 50 - "hashPassword"
Cohesion: 0.18
Nodes (6): LoginRequest, LoginResponse, AuthRepositoryImpl, Flow, hashPassword(), SecurityUtilsTest

### Community 52 - "CargoDetailsScreen.kt"
Cohesion: 0.28
Nodes (12): CargoDetailsScreen(), confirmCargo(), FloatingActionButtonItem(), handleCargoConfirmation(), Color, ImageVector, NavController, Result (+4 more)

### Community 53 - "composer.json"
Cohesion: 0.12
Nodes (15): autoload, autoload-dev, psr-4, psr-4, config, optimize-autoloader, sort-packages, description (+7 more)

### Community 56 - "CargoDetailsComponents.kt"
Cohesion: 0.28
Nodes (13): InitialInfo, CargoInfoCard(), CargoListSection(), GroupStats(), InfoChips(), InfoGrid(), InfoGridCard(), InfoGridItem() (+5 more)

### Community 57 - "ChatScreen.kt"
Cohesion: 0.27
Nodes (12): ChatUiItem, getChatBackgroundColor(), getDateHeaderColor(), Header, Color, Message, ChatScreen(), DateHeader() (+4 more)

### Community 58 - "ChatToolbar.kt"
Cohesion: 0.25
Nodes (13): ColorWheel(), Color, Modifier, getAdaptiveBubbleColor(), AdvancedColorPickerDialog(), ChatSettingsDialogEnhanced(), ChatSettingsPreviewRefined(), Color (+5 more)

### Community 59 - "ActiveShipInfo"
Cohesion: 0.27
Nodes (16): ActiveShipInfo, CargoSnackbarMessage, GroupedShipList(), Color, Modifier, QuotaCard(), ShipGroup(), ShipHeader() (+8 more)

### Community 60 - "CargoDetailsDialogSection.kt"
Cohesion: 0.31
Nodes (13): CargoDetailsDialog(), Color, ImageVector, Modifier, ModernCargoDetailsGrid(), ModernCargoStatusSection(), ModernCargoStatusTimeline(), ModernDialogActions() (+5 more)

### Community 61 - "MessageBubble.kt"
Cohesion: 0.24
Nodes (14): extractShipInfoAndText(), com, replaceUsernamesWithFullNames(), rotateIcon(), ShipInfoModel, ChatInfoRow(), EditMessageDialog(), androidx (+6 more)

### Community 63 - "WarehouseDetailsScreen.kt"
Cohesion: 0.29
Nodes (11): Warehouse, ExportOptions(), Modifier, QuotaChip(), QuotaSelector(), VoucherDetailsButton(), WarehouseCard(), WarehouseContent() (+3 more)

### Community 66 - "CoroutineScope"
Cohesion: 0.27
Nodes (8): AtkCargoApplication, BootReceiver, BroadcastReceiver, Context, Intent, KoinComponent, Application, CoroutineScope

### Community 67 - "UserManagementDialogsSection.kt"
Cohesion: 0.36
Nodes (11): CreateUserRequest, UserTypeInfo, DesignTextField(), EnhancedAddUserDialog(), EnhancedEditUserDialog(), getUserTypes(), androidx, KeyboardOptions (+3 more)

### Community 69 - "QuotaSelectionDialog.kt"
Cohesion: 0.42
Nodes (10): MatchingQuota, DialogHeader(), findDifferentFields(), InactiveIndicator(), ImageVector, QuotaDetailItem(), QuotaDetails(), QuotaHeader() (+2 more)

### Community 70 - "SearchDialogs.kt"
Cohesion: 0.29
Nodes (10): CargoSearchResultCard(), InfoRowCompact(), Color, Context, ImageVector, MultipleSearchResultDialog(), SearchType, RECEIPT_NUMBER (+2 more)

### Community 71 - "DataModel.kt"
Cohesion: 0.67
Nodes (3): adjustColorForTheme(), Color, toTon()

### Community 72 - "RealTimeLoadingBottomSheet"
Cohesion: 0.40
Nodes (9): DialogHeader(), Color, Modifier, RealTimeLoadingBottomSheet(), RealTimeShiftNavigation(), RefreshOverlay(), ShipCard(), RealTimeUiState (+1 more)

### Community 74 - "PermissionPoller"
Cohesion: 0.36
Nodes (3): Job, StateFlow, PermissionPoller

### Community 75 - "preprocessImage"
Cohesion: 0.50
Nodes (8): adaptiveThresholding(), applyContrastEnhancement(), enhanceEdges(), ImageProxy, InputImage, medianFilter(), preprocessImage(), IntArray

### Community 76 - "ShipsListScreen.kt"
Cohesion: 0.19
Nodes (10): ErrorState(), ImageVector, Modifier, sortShips(), matchesSearch(), ShipsList(), ShipsTabItem, ReportsDomainTest (+2 more)

### Community 77 - "graphify reference: extra exports and benchmark"
Cohesion: 0.22
Nodes (8): graphify reference: extra exports and benchmark, Step 6b - Wiki (only if --wiki flag), Step 7 - Neo4j export (only if --neo4j or --neo4j-push flag), Step 7a - FalkorDB export (only if --falkordb or --falkordb-push flag), Step 7b - SVG export (only if --svg flag), Step 7c - GraphML export (only if --graphml flag), Step 7d - MCP server (only if --mcp flag), Step 8 - Token reduction benchmark (only if total_words > 5000)

### Community 78 - "MainScreen.kt"
Cohesion: 0.31
Nodes (9): MainScreen(), RouteTransitions, standardTransitions(), LoginScreen(), HomeRoute, homeScreen(), NavController, navigateToHome() (+1 more)

### Community 79 - "QuotaValidationUseCase.kt"
Cohesion: 0.36
Nodes (4): QuotaValidationResult, InputValidationResult, QuotaValidationUseCase, TempTonnageValidationResult

### Community 80 - "RealTimeLoadingData"
Cohesion: 0.39
Nodes (7): RealTimeLoadingData, CompactInfo(), Color, ImageVector, Modifier, RealTimeLoadingCard(), StatisticItem()

### Community 81 - "CargoEntryNavigation.kt"
Cohesion: 0.25
Nodes (9): InitialInfoRoute, initialInfoScreen(), NavController, navigateToInitialInfo(), navigateToSelectInfo(), SelectInfoRoute, selectInfoScreen(), CargoOperationScreen() (+1 more)

### Community 82 - "QuotaEntryDialog.kt"
Cohesion: 0.57
Nodes (7): FocusRequester, QuotaEntryActions(), QuotaEntryContent(), QuotaEntryDialog(), QuotaEntryPalette, rememberQuotaEntryPalette(), validateAndSubmit()

### Community 83 - "ComprehensiveAnalyticsDialog.kt"
Cohesion: 0.54
Nodes (7): AnalyticsDateNavigation(), AnalyticsHeaderCard(), ComprehensiveAnalyticsDialog(), EmptyStateCard(), ErrorStateCard(), Modifier, ReportsViewModel

### Community 84 - "DateRangePicker.kt"
Cohesion: 0.57
Nodes (7): DateClickableField(), DateRangePicker(), getDaysInPersianMonth(), isPersianLeapYear(), Color, Modifier, PersianDatePickerDialog()

### Community 86 - "MessageInputArea"
Cohesion: 0.67
Nodes (5): Color, com, MessageInputArea(), QuotaSelectionDialog(), ShipSelectionDialog()

### Community 87 - "ProfileMenu.kt"
Cohesion: 0.52
Nodes (6): ActionButtons(), getUserTypeDisplay(), Color, ProfileMenu(), ProfilePalette, rememberProfilePalette()

### Community 88 - "ReportsNavigation.kt"
Cohesion: 0.38
Nodes (6): CargoDetailsRoute, NavController, ManageShipsRoute, manageShipsScreen(), navigateToCargoDetails(), navigateToManageShips()

### Community 89 - "proxy_generator.php"
Cohesion: 0.57
Nodes (6): _o1(), _o2(), _o3(), _o4(), _o5(), _o6()

### Community 90 - "CargoRegistrationNavigation.kt"
Cohesion: 0.60
Nodes (4): CargoRegistrationRoute, cargoRegistrationScreen(), NavController, navigateToCargoRegistration()

### Community 92 - "HomeNotificationSettingRow.kt"
Cohesion: 0.67
Nodes (5): CompactSwitch(), Color, ImageVector, Modifier, NotificationSettingRow()

### Community 93 - "LoadingState"
Cohesion: 0.47
Nodes (6): Error, Idle, Loading, LoadingState, Success, UiState

### Community 95 - "ChatNavigation.kt"
Cohesion: 0.50
Nodes (4): AdminChatRoute, adminChatScreen(), NavController, navigateToAdminChat()

### Community 97 - "graphify reference: query, path, explain"
Cohesion: 0.40
Nodes (5): For /graphify explain, For /graphify path, graphify reference: query, path, explain, Step 0 — Constrained query expansion (REQUIRED before traversal), Step 1 — Traversal

### Community 98 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 101 - "graphify reference: add a URL and watch a folder"
Cohesion: 0.50
Nodes (3): For /graphify add, For --watch, graphify reference: add a URL and watch a folder

### Community 102 - "graphify reference: commit hook and native CLAUDE.md integration"
Cohesion: 0.50
Nodes (3): For git commit hook, For native CLAUDE.md integration, graphify reference: commit hook and native CLAUDE.md integration

## Knowledge Gaps
- **168 isolated node(s):** `Constants`, `Ready`, `RequestBatteryOptimization`, `Splash`, `Syncing` (+163 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **36 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CargoViewModel` connect `CargoViewModel` to `CargoInfoDetailsDialogSection.kt`, `LoginScreen.kt`, `SelectInfoScreen.kt`, `MainScreen.kt`, `CargoEntryNavigation.kt`, `CargoDetailsScreen.kt`, `CargoRegistrationNavigation.kt`, `ActiveShipInfo`?**
  _High betweenness centrality (0.192) - this node is a cross-community bridge._
- **Why does `ReportsViewModel` connect `ReportsViewModel` to `QuotaAnalysisSection.kt`, `LoginScreen.kt`, `ExportPdfUseCase`, `ManageReportsScreen.kt`, `ShipsListScreen.kt`, `QuotasListScreen.kt`, `MainScreen.kt`, `CargoInfo`, `Ship`, `formatNumber`, `ReportsRepository`, `QuotaManagementDialog.kt`, `ReportsNavigation.kt`, `ReportModels.kt`, `QuotaWarningDialog.kt`, `LoadingState`, `WarehouseDetailsScreen.kt`?**
  _High betweenness centrality (0.068) - this node is a cross-community bridge._
- **Why does `UpdateManager` connect `UpdateManager` to `StartupViewModel`, `Color.kt`, `LoginScreen.kt`?**
  _High betweenness centrality (0.064) - this node is a cross-community bridge._
- **What connects `Constants`, `Ready`, `RequestBatteryOptimization` to the rest of the system?**
  _168 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `CargoInfoDetailsDialogSection.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.06253585771658061 - nodes in this community are weakly interconnected._
- **Should `UpdateManager` be split into smaller, more focused modules?**
  _Cohesion score 0.05115089514066496 - nodes in this community are weakly interconnected._
- **Should `CargoViewModel` be split into smaller, more focused modules?**
  _Cohesion score 0.07033248081841433 - nodes in this community are weakly interconnected._