# Graph Report - ATK-Cargo  (2026-08-21)

## Corpus Check
- 339 files · ~385,120 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2868 nodes · 5764 edges · 247 communities (186 shown, 61 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 175 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `cd1000e7`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- StartupViewModel
- ApiException
- LicenseAdminServiceTest
- MainScreen.kt
- TextAlign
- ShipSortingMode
- ChatMessageEntity
- app.js
- ReportsRepository
- UserService
- ShipDetailsScreen.kt
- ApiServiceV2
- ReportsViewModel
- JalaliDateUtilsTest
- CargoModels.kt
- Config
- ChatViewModel
- Medium Issues
- PermissionService
- Deep Code Audit Report
- SessionService
- QuotaValidationUseCase.kt
- CargoInfo
- QuotaManagementContent.kt
- LoginScreen.kt
- SessionRepository
- CargoViewModel.kt
- TokenAuthenticator
- CargoViewModelTest
- SecurityScreen.kt
- SessionServiceTest
- Context
- CrashReportRateLimiter
- ApiServiceV2.kt
- ReportsDomainCalculationsTest
- MessageBubble.kt
- UsersManager
- CargoDetailsDialogSection.kt
- CargoRepository
- CameraSection.kt
- Logger
- CargoDetailsScreen.kt
- CargoViewModel
- ATKCargoTheme
- UserManagementScreen.kt
- MainActivity.kt
- ActiveQuotasDialogSection.kt
- ActiveShipInfo
- MicroCache
- SelectInfoScreen.kt
- ShipsListScreen.kt
- LoginAttemptLimiterTest
- ReportsViewModel.kt
- CargoDetailsComponents.kt
- ChatScreen.kt
- UpdateDialog.kt
- AnalyticsController.php
- SplashScreen.kt
- CoroutineScope
- RealTimeLoadingBottomSheet.kt
- RetrofitClient
- LoadingNotificationWorker
- CargoController
- formatNumber
- TokenRefresherTest
- User
- UserManagementDialogsSection.kt
- RegisterCargoScreen.kt
- AuthViewModel
- SecurityAlerter
- ChatPreferencesStore
- ManageReportsScreen.kt
- TokenStore
- ActiveQuotasGroupedComponents.kt
- ReportModels.kt
- ReportsCommonWidgets.kt
- ApiV2Routes
- MessageInputArea
- ChatRepository
- Router
- ComprehensiveAnalyticsDialog.kt
- DownloadState
- LicenseAdminService
- استانداردسازی کامنت‌های چندخطی به تک‌خطی (کلاینت + سرور)
- LoginResult
- Intent
- preprocessImage
- KoinComponent
- ActiveQuotasContent.kt
- IBinder
- Intent
- AppModule.kt
- AnimationManager
- Migrations
- .onError
- PasswordGateService
- پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13)
- Database
- AnimationManager
- FontWeight
- Service
- ChatToolbar.kt
- ColorPicker.kt
- Code Smells شناسایی‌شده
- LoginAttemptLimiter
- HardwarePerformanceEvaluator
- نقاط قوت
- ShipInfoSection.kt
- DataModel.kt
- Executive Summary
- jdate
- PHPUnit\Framework\TestCase
- AppNotificationManager
- quota_details/QuotaCardComponents.kt
- چک‌لیست فعال‌سازی `STRICT_TRANS_TABLES` (DEEP_CODE_AUDIT.md #Phase4.10)
- What You Must Do When Invoked
- rememberAdaptiveLayoutConfig
- Final Recommendations
- ComponentDefaults.kt
- VoucherDetailsDialogSection.kt
- نقاط قوت
- ProfileMenu.kt
- PermissionPoller
- حذف/محدودسازی phpMyAdmin روی سرور production (Phase 1, آیتم #۲)
- AnalyticsController
- QuotaDetailsScreen.kt
- ColorScheme.kt
- UserSettingsStore
- Authentication & Authorization
- وضعیت کلی
- Performance Audit
- نقاط قوت
- چه چیزی بد است
- DateRangePicker.kt
- UserPreferencesManager
- SecurityVerifier
- نقاط قوت (تأیید‌شده در کد)
- جدول endpointها
- نقاط قوت
- چرخش رازهای `secrets.cpp` (Phase 1, آیتم #۱)
- Constants.kt
- Prioritized Action Plan
- Testing Audit
- AppApiController
- ColorSelector
- وضعیت: **بسیار خوب** — این نقطه‌ی قوت پروژه است
- وضعیت
- UserPreferencesStore
- AdvancedSearchDialog.kt
- SearchDialogs.kt
- AndroidViewModel
- Flow
- SelectInfoSnackbar.kt
- InputValidator
- StateFlow
- self
- HomeNotificationSettingRow.kt
- Color.kt
- graphify reference: query, path, explain
- StatisticsCard.kt
- LoginUiState
- وضعیت کلی: خوب
- Technical Debt
- PHP/composer.json
- com
- NavController
- StateFlow
- ViewModel
- Modifier
- InitialInfoScreen
- QuotaCalculatorTest
- UpdateManager
- LoadingState
- QuotaSelectionDialog.kt
- QuotaEntryDialog.kt
- SearchBar.kt
- Cargo
- CargoCounterScreen.kt
- secrets.cpp
- SessionManager
- SessionManager
- BootReceiver.kt
- CryptoManager
- mysqli
- MessageType
- ChatNotificationWorker.kt
- Request
- NotificationActionReceiver.kt
- AuthenticatesRequests.php
- QuotasListScreen.kt
- ChatController
- StartupController
- DatabaseSchemaExporter
- graphify reference: extra exports and benchmark
- JalaliDateUtils
- NavRoutes
- gradlew
- graphify reference: add a URL and watch a folder
- graphify reference: commit hook and native CLAUDE.md integration
- ExampleInstrumentedTest
- ExampleUnitTest
- BaselineProfileGenerator
- graphify reference: GitHub clone and cross-repo merge
- graphify reference: transcribe video and audio
- rules/graphify.md
- workflows/graphify.md
- CLAUDE.md
- .claude/CLAUDE.md
- extraction-spec.md

## God Nodes (most connected - your core abstractions)
1. `ReportsViewModel` - 96 edges
2. `CargoViewModel` - 75 edges
3. `InputValidator` - 59 edges
4. `ApiServiceV2` - 58 edges
5. `formatNumber()` - 45 edges
6. `UserPreferencesManager` - 38 edges
7. `UpdateManager` - 36 edges
8. `SessionRepository` - 35 edges
9. `Request` - 35 edges
10. `Logger` - 35 edges

## Surprising Connections (you probably didn't know these)
- `CargoCounterScreen()` --calls--> `ColorSelector`  [INFERRED]
  feature/cargo-workflow/src/main/java/com/atk/atk_cargo/feature/cargo_counter/presentation/CargoCounterScreen.kt → core/designsystem/src/main/java/com/atk/atk_cargo/data/model/ColorSelector.kt
- `CargoCounterScreen()` --calls--> `validateServerSession()`  [INFERRED]
  feature/cargo-workflow/src/main/java/com/atk/atk_cargo/feature/cargo_counter/presentation/CargoCounterScreen.kt → core/network/src/main/java/com/atk/atk_cargo/api/SessionValidator.kt
- `PersianDatePickerDialog()` --calls--> `FilterChip()`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/core/ui/components/DateRangePicker.kt → feature/cargo-workflow/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/ActiveQuotasContent.kt
- `RegisterCargoScreen()` --calls--> `ShipInfo`  [EXTRACTED]
  feature/cargo-workflow/src/main/java/com/atk/atk_cargo/feature/cargo_registration/presentation/RegisterCargoScreen.kt → core/domain/src/main/java/com/atk/atk_cargo/domain/model/ShipInfo.kt
- `ManageReportsScreen()` --references--> `ReportsViewModel`  [EXTRACTED]
  app/src/main/java/com/atk/atk_cargo/ui/screens/ManageReportsScreen.kt → feature/reports/src/main/java/com/atk/atk_cargo/ui/viewmodel/ReportsViewModel.kt

## Import Cycles
- None detected.

## Communities (247 total, 61 thin omitted)

### Community 0 - "StartupViewModel"
Cohesion: 0.10
Nodes (16): AndroidViewModel, Intent, Ready, RequestBatteryOptimization, SecurityBlocked, SecurityCheckState, ShowMessage, Splash (+8 more)

### Community 1 - "ApiException"
Cohesion: 0.09
Nodes (3): ApiException, LicenseRepository, PHPUnit\Framework\MockObject\MockObject

### Community 3 - "MainScreen.kt"
Cohesion: 0.06
Nodes (43): AppError, Network, Server, Timeout, toAppError(), Validation, MainScreen(), RouteTransitions (+35 more)

### Community 4 - "TextAlign"
Cohesion: 0.28
Nodes (21): DeleteDialog(), DialogBadge(), DialogButtonRow(), DialogContentCard(), DialogMessageText(), DialogTitle(), Color, ImageVector (+13 more)

### Community 5 - "ShipSortingMode"
Cohesion: 0.19
Nodes (9): ShipSortingMode, LOADED_TONNAGE_ASC, LOADED_TONNAGE_DESC, NAME_ASC, NAME_DESC, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC, sortShips() (+1 more)

### Community 6 - "ChatMessageEntity"
Cohesion: 0.14
Nodes (3): ChatDao, Flow, ChatMessageEntity

### Community 7 - "app.js"
Cohesion: 0.15
Nodes (25): buildRow(), cell(), confirmAction(), cleanup(), onClose(), onOk(), copyKey(), currentTheme() (+17 more)

### Community 8 - "ReportsRepository"
Cohesion: 0.15
Nodes (5): ErrorResponse, HttpStatusException, Exception, Result, ReportsRepository

### Community 9 - "UserService"
Cohesion: 0.16
Nodes (3): App\Core\AuthenticatesRequests, UserController, UserService

### Community 10 - "ShipDetailsScreen.kt"
Cohesion: 0.17
Nodes (24): WarningStatus, calculateWarningStatus(), ErrorStateCard(), ImageVector, ShipDetails(), ShipHeaderCard(), TabItem(), WarehouseQuotasTabs() (+16 more)

### Community 11 - "ApiServiceV2"
Cohesion: 0.16
Nodes (4): ApiResponse, ApiServiceV2, SuccessResponse, JsonElement

### Community 12 - "ReportsViewModel"
Cohesion: 0.06
Nodes (14): ComprehensiveAnalytics, GroupSortingMode, ALPHABETICAL, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC, QuotaSortingMode, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC (+6 more)

### Community 14 - "CargoModels.kt"
Cohesion: 0.09
Nodes (13): toDto(), CargoDeleteResponse, CargoInfoSearch, CargoSearchResponse, CargoStats, CheckExistenceResponse, ExistingCargo, InitialInfo (+5 more)

### Community 15 - "Config"
Cohesion: 0.10
Nodes (8): lic_destroy_session(), lic_idle_timeout(), lic_is_authenticated(), lic_require_auth_json(), lic_require_auth_page(), Config, self, Csrf

### Community 16 - "ChatViewModel"
Cohesion: 0.10
Nodes (7): ChatViewModel, ChatViewModelFactory, com, StateFlow, T, ViewModel, ViewModelProvider

### Community 17 - "Medium Issues"
Cohesion: 0.09
Nodes (23): Critical Issues, [CRITICAL] تخلیه‌ی worker pool سرور از طریق `sleep()` مسدودکننده در مسیر لاگین, [HIGH] endpoint گزارش کرش بدون auth با rate-limiter مسدودکننده, [HIGH] Foreground Service با poll دائمی، ناسازگار با سقف `dataSync` در Android 14+, High Issues, [HIGH] رازهای کلاینت با XOR تک‌بایتی «محافظت» شده‌اند, [HIGH] ~~مسیر build نسخه‌ی release هرگز در CI اجرا نمی‌شود~~ — تصحیح‌شده، سپس رفع شد, [HIGH] گیت نسخه‌ی حداقلی با حذف یک هدر دور زده می‌شود (+15 more)

### Community 19 - "Deep Code Audit Report"
Cohesion: 0.09
Nodes (21): Animation Audit, Architecture Overview, Deep Code Audit Report, Final Score, [LOW] اسپلش با تأخیر ثابت به‌جای انیمیشن مبتنی بر پیشرفت, Overall Score, **Overall Score: 5.8 / 10**, Production Readiness (+13 more)

### Community 21 - "QuotaValidationUseCase.kt"
Cohesion: 0.36
Nodes (4): QuotaValidationResult, InputValidationResult, QuotaValidationUseCase, TempTonnageValidationResult

### Community 22 - "CargoInfo"
Cohesion: 0.22
Nodes (19): toDomain(), toDto(), CargoInfo, CargoChangesPreview(), CargoEditConfirmDialog(), CargoEditConfirmHeader(), CargoMainInfo(), CargoShippingInfo() (+11 more)

### Community 23 - "QuotaManagementContent.kt"
Cohesion: 0.13
Nodes (31): QuotaItem, AnalyticsStatChipMini(), CompactStatChip(), InfoChip(), IntegratedQuotaCard(), Color, ImageVector, Modifier (+23 more)

### Community 24 - "LoginScreen.kt"
Cohesion: 0.26
Nodes (16): CompactLoginLayout(), ExpandedLoginLayout(), IndustrialBrandingHeaderContent(), IndustrialFeatureBadge(), IndustrialFooterInfo(), IndustrialInputField(), IndustrialLoginFormCard(), androidx (+8 more)

### Community 26 - "CargoViewModel.kt"
Cohesion: 0.11
Nodes (11): Kilograms, CargoDialog, CargoUiState, CargoViewModelFactory, Duplicates, StateFlow, T, ViewModel (+3 more)

### Community 27 - "TokenAuthenticator"
Cohesion: 0.19
Nodes (5): Authenticator, TokenAuthenticator, AuthErrorBody, TokenAuthenticatorTest, Route

### Community 29 - "SecurityScreen.kt"
Cohesion: 0.25
Nodes (13): AdvancedOrbitalScanner(), AnimatedUpdateIcon(), AnimatedWarningIcon(), DynamicPremiumBackground(), InteractiveExitButton(), Modifier, RetryButton(), SecurityBlockScreen() (+5 more)

### Community 33 - "ApiServiceV2.kt"
Cohesion: 0.10
Nodes (17): ApiResponse2, ActiveSessionResponse, ForceLogoutRequest, ForceLogoutResponse, LoginRequest, LoginResponse, LogoutRequest, LogoutResponse (+9 more)

### Community 34 - "ReportsDomainCalculationsTest"
Cohesion: 0.13
Nodes (7): calculatePercentage(), calculateProgress(), format(), formatWeightWithDetail(), Context, QuotaWarningThresholds, ReportsDomainCalculationsTest

### Community 35 - "MessageBubble.kt"
Cohesion: 0.24
Nodes (14): extractShipInfoAndText(), com, replaceUsernamesWithFullNames(), rotateIcon(), ShipInfoModel, ChatInfoRow(), EditMessageDialog(), androidx (+6 more)

### Community 36 - "UsersManager"
Cohesion: 0.07
Nodes (7): ApiManager, CONFIG, ModalManager, ThemeManager, UIManager, UsersManager, Utils

### Community 37 - "CargoDetailsDialogSection.kt"
Cohesion: 0.31
Nodes (13): CargoDetailsDialog(), Color, ImageVector, Modifier, ModernCargoDetailsGrid(), ModernCargoStatusSection(), ModernCargoStatusTimeline(), ModernDialogActions() (+5 more)

### Community 38 - "CargoRepository"
Cohesion: 0.07
Nodes (4): App\Enums\CargoConfirmStatus, App\Enums\CargoStatus, CargoRepository, CargoService

### Community 39 - "CameraSection.kt"
Cohesion: 0.21
Nodes (11): EnhancedNumberAnalyzer, extractNumber(), ImageProxy, InputImage, recognizeTextFromImage(), EnhancedCameraPreview(), ImageCapture, Color (+3 more)

### Community 40 - "Logger"
Cohesion: 0.11
Nodes (4): LicenseController, UtilityController, Logger, self

### Community 41 - "CargoDetailsScreen.kt"
Cohesion: 0.22
Nodes (13): Result, validateServerSession(), SessionCheckRequest, CargoCounterOperationScreen(), NavController, CargoDetailsScreen(), FloatingActionButtonItem(), Color (+5 more)

### Community 42 - "CargoViewModel"
Cohesion: 0.10
Nodes (6): SaveOrUpdateResponse, CargoViewModel, DuplicateConfirmation, CargoOperationScreen(), NavController, message

### Community 43 - "ATKCargoTheme"
Cohesion: 0.14
Nodes (11): ComponentStyles, Dimensions, Elevation, ATKCargoTheme, Motion, SemanticColors, AppShapes, Spacing (+3 more)

### Community 44 - "UserManagementScreen.kt"
Cohesion: 0.28
Nodes (12): DashboardSummaryRow(), EmptyStateView(), EnterpriseUserCard(), androidx, Color, RoleFilterChipRow(), RoleGroupHeader(), ShimmerUserLoadingList() (+4 more)

### Community 45 - "MainActivity.kt"
Cohesion: 0.26
Nodes (6): Intent, MainActivity, StartupErrorScreen(), Bundle, ComponentActivity, ServerSyncingScreen()

### Community 46 - "ActiveQuotasDialogSection.kt"
Cohesion: 0.29
Nodes (11): ViewMode, FLAT, GROUPED, ActiveQuotasDialog(), FilterBar(), Color, Dp, Modifier (+3 more)

### Community 47 - "ActiveShipInfo"
Cohesion: 0.35
Nodes (10): ActiveShipInfo, GroupedShipList(), Color, QuotaCard(), ShipGroup(), ShipHeader(), WarehouseCard(), WarehouseList() (+2 more)

### Community 48 - "MicroCache"
Cohesion: 0.07
Nodes (5): MicroCache, AppApiCacheKeys, AuditLogger, QuotaService, ShipService

### Community 49 - "SelectInfoScreen.kt"
Cohesion: 0.24
Nodes (15): RealTimeLoadingData, AnimatedHeader(), GroupedShipList(), Color, Modifier, NavController, Result, navigateToRegisterCargoActivity() (+7 more)

### Community 50 - "ShipsListScreen.kt"
Cohesion: 0.25
Nodes (13): ErrorState(), ImageVector, Modifier, ImageVector, Modifier, ShipCard(), ShipCardContent(), ShipSortingModeButton() (+5 more)

### Community 52 - "ReportsViewModel.kt"
Cohesion: 0.20
Nodes (13): FilteredSummary, Quota, QuotaDetails, Warehouse, Modifier, QuotaChip(), QuotaSelector(), VoucherDetailsButton() (+5 more)

### Community 53 - "CargoDetailsComponents.kt"
Cohesion: 0.24
Nodes (14): QuotaInfo, toDomain(), CargoInfoCard(), CargoListSection(), GroupStats(), InfoChips(), InfoGrid(), InfoGridCard() (+6 more)

### Community 54 - "ChatScreen.kt"
Cohesion: 0.27
Nodes (12): ChatUiItem, getChatBackgroundColor(), getDateHeaderColor(), Header, Color, Message, ChatScreen(), DateHeader() (+4 more)

### Community 55 - "UpdateDialog.kt"
Cohesion: 0.33
Nodes (11): DownloadActionButtons(), DownloadingState(), DownloadProgressInfo(), DownloadSizeInfo(), DownloadSpeedInfo(), ErrorState(), ModernUpdateActionSection(), ModernUpdateContent() (+3 more)

### Community 56 - "AnalyticsController.php"
Cohesion: 0.13
Nodes (10): App\Core\Database, App\Core\DatabaseManager, App\Core\MicroCache, App\Core\Request, App\Core\Response, App\Exceptions\ApiException, App\Validators\InputValidator, PDOException (+2 more)

### Community 57 - "SplashScreen.kt"
Cohesion: 0.27
Nodes (8): VazirmatnFontFamily, AnimatedBrandTitle(), SplashScreen(), SplashScreenConstants, SplashScreenContent(), SplashSubtitleBadge(), SplashVersionBadge(), FontFamily

### Community 58 - "CoroutineScope"
Cohesion: 0.29
Nodes (6): AtkCargoApplication, CrashReporter, Context, PendingCrashReport, Application, CoroutineScope

### Community 59 - "RealTimeLoadingBottomSheet.kt"
Cohesion: 0.20
Nodes (15): ShiftInfo, DialogHeader(), Color, Modifier, RealTimeLoadingBottomSheet(), RealTimeShiftNavigation(), RefreshOverlay(), ShipCard() (+7 more)

### Community 60 - "RetrofitClient"
Cohesion: 0.26
Nodes (9): FloatTypeAdapter, Context, OkHttpClient, RetrofitClient, HttpLoggingInterceptor, JsonReader, JsonWriter, Retrofit (+1 more)

### Community 61 - "LoadingNotificationWorker"
Cohesion: 0.26
Nodes (7): KoinComponent, UserPreferencesManager, LoadingNotificationWorker, TypeToken, RealTimeLoadingData, ReportsRepository, ShiftInfo

### Community 63 - "formatNumber"
Cohesion: 0.19
Nodes (22): QuotaCompletionData, QuotaGroupingMode, BY_CARGO_OWNER, BY_CARRIER, BY_SHIP, buildQuotaGroups(), QuotaGroup, formatNumber() (+14 more)

### Community 65 - "User"
Cohesion: 0.17
Nodes (8): User, EnhancedDeleteConfirmationDialog(), EnhancedForceLogoutDialog(), getUserTypeDisplay(), sortUsersByType(), ProfileSettingsDialog(), ViewModel, ProfileViewModel

### Community 66 - "UserManagementDialogsSection.kt"
Cohesion: 0.35
Nodes (11): UpdateUserRequest, DesignTextField(), EnhancedAddUserDialog(), EnhancedEditUserDialog(), getUserTypes(), androidx, KeyboardOptions, Modifier (+3 more)

### Community 67 - "RegisterCargoScreen.kt"
Cohesion: 0.16
Nodes (14): CargoConfirmStatus, AWAITING_CONFIRMATION, CONFIRMED, PENDING, CargoStatus, ENTERED, EXITED, DuplicateTrackingNumbersDialog() (+6 more)

### Community 68 - "AuthViewModel"
Cohesion: 0.18
Nodes (6): For --cluster-only, For --update (incremental re-extraction), graphify reference: incremental update and cluster-only, AuthViewModel, StateFlow, ViewModel

### Community 69 - "SecurityAlerter"
Cohesion: 0.21
Nodes (3): App\Core\Logger, SecurityAlerter, self

### Community 70 - "ChatPreferencesStore"
Cohesion: 0.31
Nodes (6): ChatPreferencesStore, Flow, AdminChatRoute, adminChatScreen(), NavController, navigateToAdminChat()

### Community 71 - "ManageReportsScreen.kt"
Cohesion: 0.29
Nodes (14): addOneDayToPersianDate(), DatePickerDialog(), DateTimeSelectionCard(), getDaysInPersianMonth(), isPersianLeapYear(), Color, ImageVector, Modifier (+6 more)

### Community 72 - "TokenStore"
Cohesion: 0.17
Nodes (4): OkHttpClient, TokenRefresher, TokenStore, RefreshTokenResponse

### Community 73 - "ActiveQuotasGroupedComponents.kt"
Cohesion: 0.50
Nodes (8): formatNumber(), extractLastDigits(), ActiveShipInfo, Color, QuotaItem(), ShipCard(), ShipHeader(), WarehouseSection()

### Community 74 - "ReportModels.kt"
Cohesion: 0.09
Nodes (14): QuotaRepository, CargoInfoResponse, AnalyticsData, ComprehensiveAnalysisResponse, DateInfo, ExistingQuota, ExitDateInfo, FilteredSummaryResponse (+6 more)

### Community 75 - "ReportsCommonWidgets.kt"
Cohesion: 0.29
Nodes (14): CompactStatChip(), EmptyQuotaState(), EmptyShipsState(), EmptyStateCard(), ErrorStateCard(), FabItem, FloatingActionButton(), InfoCard() (+6 more)

### Community 77 - "MessageInputArea"
Cohesion: 0.67
Nodes (5): Color, com, MessageInputArea(), QuotaSelectionDialog(), ShipSelectionDialog()

### Community 78 - "ChatRepository"
Cohesion: 0.12
Nodes (11): ChatMessage, ChatMessagesResponse, SendMessageRequest, SendMessageResponse, UnreadCountResponse, DeleteMessageRequest, EditMessageRequest, ChatRepository (+3 more)

### Community 79 - "Router"
Cohesion: 0.14
Nodes (4): ApiAuthGate, MinVersionGate, Request, Router

### Community 80 - "ComprehensiveAnalyticsDialog.kt"
Cohesion: 0.57
Nodes (6): AnalyticsDateNavigation(), AnalyticsHeaderCard(), ComprehensiveAnalyticsDialog(), EmptyStateCard(), ErrorStateCard(), Modifier

### Community 81 - "DownloadState"
Cohesion: 0.29
Nodes (6): Completed, Downloading, DownloadState, Error, Idle, Paused

### Community 83 - "استانداردسازی کامنت‌های چندخطی به تک‌خطی (کلاینت + سرور)"
Cohesion: 0.29
Nodes (6): Context, Verification, استانداردسازی کامنت‌های چندخطی به تک‌خطی (کلاینت + سرور), دایرکتوری‌های مستثنی (اصلاً لمس نشوند), قوانین استاندارد تبدیل, نحوه‌ی اجرا

### Community 84 - "LoginResult"
Cohesion: 0.23
Nodes (7): AuthRepository, ConflictSession, Error, Flow, LoginResult, Success, LoginUseCase

### Community 86 - "preprocessImage"
Cohesion: 0.50
Nodes (8): adaptiveThresholding(), applyContrastEnhancement(), enhanceEdges(), ImageProxy, InputImage, medianFilter(), preprocessImage(), IntArray

### Community 88 - "ActiveQuotasContent.kt"
Cohesion: 0.30
Nodes (11): EmptySearchResult(), FilterChip(), FilterState, ALL, COMPLETED, PENDING, FlatQuotasContent(), GroupedShipsContent() (+3 more)

### Community 91 - "AppModule.kt"
Cohesion: 0.47
Nodes (3): AppDatabase, Context, RoomDatabase

### Community 93 - "Migrations"
Cohesion: 0.33
Nodes (5): Migrations, migrationهای اعمال‌شده تاکنون, قرارداد نام‌گذاری, نحوه‌ی اجرا, وضعیت schema.sql

### Community 94 - ".onError"
Cohesion: 0.21
Nodes (6): CreateUserRequest, DeleteUserRequest, ViewModel, UserManagementViewModel, CargoInfoRow(), ImageCaptureException

### Community 96 - "پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13)"
Cohesion: 0.22
Nodes (8): ساختار فعلی (مستند شده در این جلسه), ساختار هدف, مراحل اجرا (ترتیب دقیق مهم است), معیار موفقیت, پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13), پیش‌نیازهای جانبی که باید قبل از این کار حل شوند, چرا فقط .htaccess کافی نیست, چرا لازم است

### Community 97 - "Database"
Cohesion: 0.12
Nodes (5): PDO, Database, PDO, self, UserRepository

### Community 99 - "FontWeight"
Cohesion: 0.18
Nodes (9): ConfirmationDialog(), ImageVector, EmptyState(), ImageVector, Modifier, AnimatedCounter(), ExitStatusDialog(), ImageVector (+1 more)

### Community 101 - "ChatToolbar.kt"
Cohesion: 0.35
Nodes (10): getAdaptiveBubbleColor(), AdvancedColorPickerDialog(), ChatSettingsDialogEnhanced(), ChatSettingsPreviewRefined(), Color, ImageVector, Modifier, LazyRowColorsRefined() (+2 more)

### Community 102 - "ColorPicker.kt"
Cohesion: 0.83
Nodes (3): ColorWheel(), Color, Modifier

### Community 103 - "Code Smells شناسایی‌شده"
Cohesion: 0.25
Nodes (8): Code Quality, Code Smells شناسایی‌شده, [HIGH] God ViewModel — `CargoViewModel`, [HIGH] نشت لایه — ViewModelها مستقیم `ApiServiceV2` را صدا می‌زنند, [LOW] اعداد و رشته‌های جادویی, [MEDIUM] `ChatRepository` مسئولیت‌های نامرتبط دارد (Feature Envy), [MEDIUM] ViewModel داخل فایل Composable, [MEDIUM] فایل‌های بسیار بزرگ در سراسر پروژه

### Community 109 - "نقاط قوت"
Cohesion: 0.25
Nodes (8): Database Audit, [HIGH] کلیدهای JOIN ناسازگار بین کوئری‌های تحلیلی و سرویس‌ها, [LOW] collation ناسازگار در جدول `Passwords`, [LOW] ایندکس‌های زائد و کم‌ارزش روی `admin_chat_messages`, [MEDIUM] `CargoInfo` هیچ کلید خارجی به `InitialInfo` ندارد, [MEDIUM] تاریخ و زمان به‌صورت `varchar(100)` ذخیره می‌شوند, [MEDIUM] نوع‌دهی ناسازگار ستون‌های وزن, نقاط قوت

### Community 110 - "ShipInfoSection.kt"
Cohesion: 0.28
Nodes (13): ShipInfo, formatNumber(), toEnglishNumbers(), DetailedInfoGrid(), DetailInfoItem(), ExpandedContent(), Color, ImageVector (+5 more)

### Community 115 - "Executive Summary"
Cohesion: 0.25
Nodes (8): Executive Summary, Overall Score: **5.8 / 10**, Production Readiness, Technical Debt, مهم‌ترین ریسک‌های امنیتی, مهم‌ترین مشکلات Architecture, مهم‌ترین مشکلات Performance, وضعیت کلی

### Community 116 - "jdate"
Cohesion: 0.53
Nodes (9): gregorian_to_jalali(), jalali_to_gregorian(), jcheckdate(), jdate(), jdate_words(), jgetdate(), jmktime(), jstrftime() (+1 more)

### Community 119 - "quota_details/QuotaCardComponents.kt"
Cohesion: 0.09
Nodes (42): ActionButtonTest, createTypography(), CalculationResult, QuotaEditData, QuotaPercentageData, DeleteQuotaDialog(), androidx, Color (+34 more)

### Community 120 - "چک‌لیست فعال‌سازی `STRICT_TRANS_TABLES` (DEEP_CODE_AUDIT.md #Phase4.10)"
Cohesion: 0.25
Nodes (7): ریسک واقعی کشف‌شده: بدون سقف طول روی فیلدهای متنی آزاد, مراحل امن (ترتیب مهم است), مسئله چیست, معیار موفقیت, نتایج تست عملی (Phase 5.15), چرا نمی‌شود همین الان فعالش کرد, چک‌لیست فعال‌سازی `STRICT_TRANS_TABLES` (DEEP_CODE_AUDIT.md #Phase4.10)

### Community 122 - "What You Must Do When Invoked"
Cohesion: 0.07
Nodes (26): For /graphify add and --watch, For /graphify query, For the commit hook and native CLAUDE.md integration, For --update and --cluster-only, /graphify, Honesty Rules, Interpreter guard for subcommands, Part A - Structural extraction for code files (+18 more)

### Community 123 - "rememberAdaptiveLayoutConfig"
Cohesion: 0.33
Nodes (6): AdaptiveLayoutConfig, rememberAdaptiveLayoutConfig(), WindowSizeClass, COMPACT, EXPANDED, MEDIUM

### Community 124 - "Final Recommendations"
Cohesion: 0.29
Nodes (7): Final Recommendations, ۱. اول جلوی خون‌ریزی را بگیر، بعد بازسازی کن, ۲. بدهی معماری را با فیچرهای جدید بپرداز، نه با یک پروژه‌ی جداگانه, ۳. تست را از جایی شروع کن که شکستش بی‌صدا و پرهزینه است, ۴. مسیر release را در CI گیت کن — همین امروز, ۵. آنچه نباید تغییر کند, ۶. تصمیم‌های محصولی که باید گرفته شوند

### Community 128 - "VoucherDetailsDialogSection.kt"
Cohesion: 0.17
Nodes (20): VoucherDetail, persianDateFormat(), DateTimePicker(), Modifier, PersianDateItem(), TimePickerDialog(), EmptyVoucherList(), Modifier (+12 more)

### Community 129 - "نقاط قوت"
Cohesion: 0.29
Nodes (7): [LOW] PHPStan فایل‌های ریشه و پنل مدیریتی را پوشش نمی‌دهد, [MEDIUM] `AppApiController` عمدتاً یک Middle Man است, [MEDIUM] دو مسیر دسترسی به دیتابیس (PDO و mysqli) به‌صورت موازی, [MEDIUM] رمزگذاری خروجی ناسازگار بین دو مسیر نوشتن, [MEDIUM] گیت احراز هویت دوبار پیاده‌سازی شده است, PHP Backend Audit, نقاط قوت

### Community 130 - "ProfileMenu.kt"
Cohesion: 0.52
Nodes (6): ActionButtons(), getUserTypeDisplay(), Color, ProfileMenu(), ProfilePalette, rememberProfilePalette()

### Community 131 - "PermissionPoller"
Cohesion: 0.36
Nodes (3): Job, StateFlow, PermissionPoller

### Community 132 - "حذف/محدودسازی phpMyAdmin روی سرور production (Phase 1, آیتم #۲)"
Cohesion: 0.40
Nodes (4): حذف/محدودسازی phpMyAdmin روی سرور production (Phase 1, آیتم #۲), راستی‌آزمایی نهایی, گزینه‌ی ۱ — حذف کامل (توصیه‌شده), گزینه‌ی ۲ — اگر حذف فوری ممکن نیست

### Community 134 - "QuotaDetailsScreen.kt"
Cohesion: 0.54
Nodes (7): ApiQuotaDetails, ProgressBar(), QuotaAdditionalInfo(), QuotaDetails(), QuotaInfoCards(), QuotaMainCard(), QuotaProgressBar()

### Community 135 - "ColorScheme.kt"
Cohesion: 0.80
Nodes (5): ColorScheme, buildAppDarkColorScheme(), buildAppLightColorScheme(), compositeOver(), Color

### Community 136 - "UserSettingsStore"
Cohesion: 0.28
Nodes (4): Flow, UserSettingsStore, ThemeColorOption, ThemeColorPickerRow()

### Community 137 - "Authentication & Authorization"
Cohesion: 0.33
Nodes (6): Authentication & Authorization, [MEDIUM] TOCTOU در ایجاد نشست, [MEDIUM] ویرایش پروفایل خود کاربر، او را از سیستم خارج می‌کند, ارزیابی, جریان کامل (استخراج‌شده از کد), مسائل باقی‌مانده

### Community 138 - "وضعیت کلی"
Cohesion: 0.33
Nodes (6): Error Handling, [LOW] `CryptoManager` شکست رمزگشایی را به رشته‌ی خالی تبدیل می‌کند, [MEDIUM] `catch (Exception)` سراسری به «دستکاری‌شده» ترجمه می‌شود, [MEDIUM] `refreshMessages` کش محلی را روی پاسخ خالی پاک می‌کند, [MEDIUM] `sendMessage` در صورت پاسخ ناقص، موفقیت کاذب گزارش می‌کند, وضعیت کلی

### Community 139 - "Performance Audit"
Cohesion: 0.33
Nodes (6): [HIGH] اسپلش اجباری ۳٫۸ ثانیه‌ای در هر cold start, [HIGH] معماری مبتنی بر polling در سراسر کلاینت, [HIGH] هیچ صفحه‌بندی در هیچ endpoint گزارش‌گیری وجود ندارد, [MEDIUM] سه استک HTTP موازی با سه connection pool مجزا, [MEDIUM] کوئری‌های `DATE()` و زیرپرس‌وجوهای همبسته که از ایندکس استفاده نمی‌کنند, Performance Audit

### Community 140 - "نقاط قوت"
Cohesion: 0.33
Nodes (6): [HIGH] محاسبات سنگین مجموعه‌ها بدون `remember` داخل composition, Jetpack Compose Audit, [MEDIUM] Composableهای بسیار بزرگ و فایل‌های چندمسئولیتی, [MEDIUM] `LocalLayoutDirection` به‌صورت سراسری روی RTL قفل شده, [MEDIUM] ساخت Flow جدید در هر recomposition, نقاط قوت

### Community 141 - "چه چیزی بد است"
Cohesion: 0.33
Nodes (6): [HIGH] هفت پکیج بین ماژول‌ها split شده‌اند, [LOW] ماژول‌های بیش‌ازحد ریز و DI متمرکز, [MEDIUM] کد فیچر در ماژول `:app` باقی مانده, Project Structure, چه چیزی بد است, چه چیزی خوب است

### Community 142 - "DateRangePicker.kt"
Cohesion: 0.57
Nodes (7): DateClickableField(), DateRangePicker(), getDaysInPersianMonth(), isPersianLeapYear(), Color, Modifier, PersianDatePickerDialog()

### Community 144 - "UserPreferencesManager"
Cohesion: 0.09
Nodes (5): Flow, T, UserPreferencesManager, DataStore, Preferences

### Community 145 - "SecurityVerifier"
Cohesion: 0.10
Nodes (11): SecurityErrorType, LICENSE_INACTIVE, LICENSE_NOT_FOUND, NETWORK_ERROR, TAMPERED, UNKNOWN_ERROR, SecurityVerifier, Secrets (+3 more)

### Community 147 - "نقاط قوت (تأیید‌شده در کد)"
Cohesion: 0.40
Nodes (5): Android Audit, [HIGH] `targetSdk = 34` عقب‌تر از `compileSdk = 36`, [MEDIUM] race در مقداردهی `AuthSession` هنگام cold start, [MEDIUM] `System.loadLibrary("secrets")` در `init` — نقطه‌ی شکست تک‌نقطه‌ای, نقاط قوت (تأیید‌شده در کد)

### Community 148 - "جدول endpointها"
Cohesion: 0.40
Nodes (5): API Audit, [MEDIUM] API از قراردادهای REST پیروی نمی‌کند, [MEDIUM] معنای کدهای وضعیت HTTP ناسازگار است, [MEDIUM] هیچ محدودیت نرخی روی endpointهای غیر ورود وجود ندارد, جدول endpointها

### Community 149 - "نقاط قوت"
Cohesion: 0.40
Nodes (5): Kotlin Audit, [MEDIUM] `AnimationManager` حالت سراسری غیر-snapshot و غیر‌ایمن نسبت به thread است, [MEDIUM] `FloatTypeAdapter` خطاهای پارس را به `0f` خاموش تبدیل می‌کند, [MEDIUM] بلوک `catch` غیرقابل دسترس، لغو coroutine را بلعیده می‌کند, نقاط قوت

### Community 150 - "چرخش رازهای `secrets.cpp` (Phase 1, آیتم #۱)"
Cohesion: 0.50
Nodes (3): تاریخچه‌ی git, مراحل, چرخش رازهای `secrets.cpp` (Phase 1, آیتم #۱)

### Community 156 - "Prioritized Action Plan"
Cohesion: 0.40
Nodes (5): Phase 1 — Immediate (قبل از انتشار بعدی · تخمین ۲–۳ روز), Phase 2 — High Priority (۲–۳ هفته), Phase 3 — Medium Priority (۱–۲ ماه), Phase 4 — Optimization (بلندمدت), Prioritized Action Plan

### Community 157 - "Testing Audit"
Cohesion: 0.40
Nodes (5): Testing Audit, آنچه تست شده, آنچه تست نشده (بحرانی), تست‌های پیشنهادی (به ترتیب اولویت), وضعیت: **ضعیف‌ترین بخش پروژه**

### Community 159 - "ColorSelector"
Cohesion: 0.46
Nodes (3): adjustColorForTheme(), ColorSelector, Color

### Community 160 - "وضعیت: **بسیار خوب** — این نقطه‌ی قوت پروژه است"
Cohesion: 0.50
Nodes (4): Dependency Audit, [LOW] دو استک سریال‌سازی JSON هم‌زمان, [LOW] نام مستعارهای version catalog با نسخه‌های واقعی نمی‌خوانند, وضعیت: **بسیار خوب** — این نقطه‌ی قوت پروژه است

### Community 161 - "وضعیت"
Cohesion: 0.50
Nodes (4): Logging & Observability, [MEDIUM] لاگ‌های debug محتوای پیام چت را ثبت می‌کنند, [MEDIUM] هیچ مانیتورینگ یا هشدار خودکاری در تولید وجود ندارد, وضعیت

### Community 163 - "AdvancedSearchDialog.kt"
Cohesion: 0.36
Nodes (7): AdvancedSearchDialog(), Modifier, SearchTabInfo, SearchTypeTabRow(), SearchType, RECEIPT_NUMBER, TRACKING_NUMBER

### Community 164 - "SearchDialogs.kt"
Cohesion: 0.46
Nodes (7): CargoSearchResultCard(), InfoRowCompact(), Color, Context, ImageVector, MultipleSearchResultDialog(), shareCargoInfo()

### Community 167 - "SelectInfoSnackbar.kt"
Cohesion: 0.57
Nodes (6): Color, Modifier, snackbarAccent(), snackbarAccentBg(), StatusSnackbar(), SnackbarMessage

### Community 168 - "InputValidator"
Cohesion: 0.08
Nodes (3): AuthController, InputValidator, InputValidatorTest

### Community 171 - "HomeNotificationSettingRow.kt"
Cohesion: 0.67
Nodes (5): CompactSwitch(), Color, ImageVector, Modifier, NotificationSettingRow()

### Community 173 - "graphify reference: query, path, explain"
Cohesion: 0.40
Nodes (5): For /graphify explain, For /graphify path, graphify reference: query, path, explain, Step 0 — Constrained query expansion (REQUIRED before traversal), Step 1 — Traversal

### Community 174 - "StatisticsCard.kt"
Cohesion: 0.70
Nodes (4): Color, ImageVector, Modifier, StatisticsCard()

### Community 175 - "LoginUiState"
Cohesion: 0.40
Nodes (5): Error, Idle, Loading, LoginUiState, Success

### Community 176 - "وضعیت کلی: خوب"
Cohesion: 0.50
Nodes (4): [LOW] `applicationScope` بدون لغو در `AtkCargoApplication`, [MEDIUM] نتایج بدون سقف در سمت سرور، نه در کلاینت, Memory Audit, وضعیت کلی: خوب

### Community 177 - "Technical Debt"
Cohesion: 0.67
Nodes (3): Technical Debt, بدهی ریپازیتوری, کد مرده (تأییدشده با جستجوی کل ریپازیتوری)

### Community 178 - "PHP/composer.json"
Cohesion: 0.09
Nodes (21): autoload, autoload-dev, psr-4, psr-4, config, optimize-autoloader, sort-packages, description (+13 more)

### Community 188 - "InitialInfoScreen"
Cohesion: 0.10
Nodes (34): CheckExistenceRequest, formatNumber(), isValidPersianText(), isValidQuotaNumber(), isValidShipName(), isValidWarehouseName(), isValidWeight(), ConfirmationDialog() (+26 more)

### Community 195 - "UpdateManager"
Cohesion: 0.13
Nodes (7): DownloadProgress, Context, Job, StateFlow, UpdateInfo, UpdateManager, VersionCheckResult

### Community 197 - "LoadingState"
Cohesion: 0.47
Nodes (6): Error, Idle, Loading, LoadingState, Success, UiState

### Community 204 - "QuotaSelectionDialog.kt"
Cohesion: 0.42
Nodes (10): MatchingQuota, DialogHeader(), findDifferentFields(), InactiveIndicator(), ImageVector, QuotaDetailItem(), QuotaDetails(), QuotaHeader() (+2 more)

### Community 206 - "QuotaEntryDialog.kt"
Cohesion: 0.57
Nodes (7): FocusRequester, QuotaEntryActions(), QuotaEntryContent(), QuotaEntryDialog(), QuotaEntryPalette, rememberQuotaEntryPalette(), validateAndSubmit()

### Community 208 - "Cargo"
Cohesion: 0.38
Nodes (12): Cargo, CargoInfoRequest, CargoDetailsPalette, CargoDetailTabItem(), CargoInfoDetailsDialog(), DetailInfoRow(), ImageVector, Modifier (+4 more)

### Community 209 - "CargoCounterScreen.kt"
Cohesion: 0.22
Nodes (14): com, CargoCounterScreen(), CargoCounterViewModel, filterShipsByTab(), ActiveShipInfo, navigateToCargoDetailsScreen(), ShipFilterTab, ALL (+6 more)

### Community 221 - "secrets.cpp"
Cohesion: 0.34
Nodes (16): decryptXor(), JNI_OnLoad(), n0(), n1(), n2(), n3(), n4(), n5() (+8 more)

### Community 257 - "BootReceiver.kt"
Cohesion: 0.39
Nodes (6): BootReceiver, BroadcastReceiver, Context, Intent, KoinComponent, UserPreferencesManager

### Community 260 - "CryptoManager"
Cohesion: 0.33
Nodes (4): CryptoManager, ByteArray, Cipher, SecretKey

### Community 262 - "mysqli"
Cohesion: 0.17
Nodes (3): mysqli, mysqli_stmt, DatabaseManager

### Community 265 - "MessageType"
Cohesion: 0.16
Nodes (14): Modifier, StatusSnackbar(), MessageType, ERROR, SUCCESS, WARNING, CargoSnackbarQueue, StateFlow (+6 more)

### Community 266 - "ChatNotificationWorker.kt"
Cohesion: 0.48
Nodes (4): ChatNotificationWorker, KoinComponent, CoroutineWorker, ListenableWorker

### Community 272 - "Request"
Cohesion: 0.07
Nodes (8): Exception, InvalidArgumentException, lic_required_id(), OnlineUsersController, Request, Response, ConflictException, DatabaseException

### Community 275 - "NotificationActionReceiver.kt"
Cohesion: 0.52
Nodes (4): BroadcastReceiver, Context, Intent, NotificationActionReceiver

### Community 276 - "AuthenticatesRequests.php"
Cohesion: 0.48
Nodes (6): App\Services\PermissionService, App\Services\SessionService, enforceMinAppVersion(), requireAuthenticatedSession(), requirePermission(), sendAuthErrorResponse()

### Community 282 - "QuotasListScreen.kt"
Cohesion: 0.20
Nodes (16): WarehouseQuotaGroupingMode, BY_CARGO_OWNER, BY_SHIPPING_COMPANY, BY_WAREHOUSE, buildQuotasShareText(), shareQuotasData(), GroupingModeButton(), GroupingModeSelector() (+8 more)

### Community 319 - "graphify reference: extra exports and benchmark"
Cohesion: 0.22
Nodes (8): graphify reference: extra exports and benchmark, Step 6b - Wiki (only if --wiki flag), Step 7 - Neo4j export (only if --neo4j or --neo4j-push flag), Step 7a - FalkorDB export (only if --falkordb or --falkordb-push flag), Step 7b - SVG export (only if --svg flag), Step 7c - GraphML export (only if --graphml flag), Step 7d - MCP server (only if --mcp flag), Step 8 - Token reduction benchmark (only if total_words > 5000)

### Community 373 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 406 - "graphify reference: add a URL and watch a folder"
Cohesion: 0.50
Nodes (3): For /graphify add, For --watch, graphify reference: add a URL and watch a folder

### Community 407 - "graphify reference: commit hook and native CLAUDE.md integration"
Cohesion: 0.50
Nodes (3): For git commit hook, For native CLAUDE.md integration, graphify reference: commit hook and native CLAUDE.md integration

## Knowledge Gaps
- **290 isolated node(s):** `وضعیت کلی`, `مهم‌ترین ریسک‌های امنیتی`, `مهم‌ترین مشکلات Performance`, `مهم‌ترین مشکلات Architecture`, `Technical Debt` (+285 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **61 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReportsViewModel` connect `ReportsViewModel` to `ColorSelector`, `MainScreen.kt`, `ShipSortingMode`, `QuotaDetailsScreen.kt`, `ManageReportsScreen.kt`, `LoadingState`, `RealTimeLoadingBottomSheet.kt`, `ShipDetailsScreen.kt`, `ComprehensiveAnalyticsDialog.kt`, `ShipsListScreen.kt`, `ReportsViewModel.kt`, `CargoInfo`, `QuotaManagementContent.kt`, `QuotasListScreen.kt`, `AppModule.kt`, `formatNumber`?**
  _High betweenness centrality (0.061) - this node is a cross-community bridge._
- **Why does `UserPreferencesManager` connect `UserPreferencesManager` to `UserPreferencesStore`, `MainScreen.kt`, `ChatPreferencesStore`, `ManageReportsScreen.kt`, `UserSettingsStore`, `TokenStore`, `ChatNotificationWorker.kt`, `MainActivity.kt`, `AppModule.kt`?**
  _High betweenness centrality (0.025) - this node is a cross-community bridge._
- **Why does `CargoViewModel` connect `CargoViewModel` to `MainScreen.kt`, `TextAlign`, `RegisterCargoScreen.kt`, `MessageType`, `CargoDetailsScreen.kt`, `CargoModels.kt`, `Cargo`, `SelectInfoScreen.kt`, `QuotaValidationUseCase.kt`, `CargoDetailsComponents.kt`, `CargoViewModel.kt`, `AppModule.kt`, `CargoViewModelTest`?**
  _High betweenness centrality (0.016) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `InputValidator` (e.g. with `.getShipQuotasRemaining()` and `.handleQuotaRemaining()`) actually correct?**
  _`InputValidator` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `وضعیت کلی`, `مهم‌ترین ریسک‌های امنیتی`, `مهم‌ترین مشکلات Performance` to the rest of the system?**
  _290 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `StartupViewModel` be split into smaller, more focused modules?**
  _Cohesion score 0.0962566844919786 - nodes in this community are weakly interconnected._
- **Should `ApiException` be split into smaller, more focused modules?**
  _Cohesion score 0.09057971014492754 - nodes in this community are weakly interconnected._