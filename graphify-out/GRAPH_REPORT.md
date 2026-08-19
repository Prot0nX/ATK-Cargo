# Graph Report - ATK-Cargo  (2026-08-19)

## Corpus Check
- 291 files · ~389,433 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2583 nodes · 5305 edges · 203 communities (160 shown, 43 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 134 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `194eb062`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- StartupViewModel
- ReportsRepository
- Security Audit
- CargoCounterScreen.kt
- TextAlign
- QuotaExistenceMultipleResponse
- ChatMessageEntity
- **5.2 / 10**
- SaveOrUpdateResponse
- CargoDetailsComponents.kt
- ActiveQuotasContent.kt
- ChatToolbar.kt
- SelectInfoScreen.kt
- JalaliDateUtilsTest
- ApiServiceV2
- ApiException
- Executive Summary
- Cargo
- PermissionService
- quota_details/QuotaCardComponents.kt
- SessionService
- CargoEditSearchDialogsSection.kt
- HomeScreen.kt
- ManageReportsScreen.kt
- LogoutRequest
- SessionRepository
- MessageType
- QuotaService
- CameraSection.kt
- QuotaManagementContent.kt
- MainActivity.kt
- CargoViewModel
- SecurityAlerter
- AuthModels.kt
- WarehouseDetailsScreen.kt
- UserManagementDialogsSection.kt
- UsersManager
- AppModule.kt
- CargoRepository
- ReportsViewModel
- Logger
- API Audit
- SecurityScreen.kt
- ATKCargoTheme
- UserManagementScreen.kt
- QuotaAnalysisSection.kt
- FontWeight
- ChatScreen.kt
- CargoInfoDetailsDialogSection.kt
- ActiveShipInfo
- MessageBubble.kt
- LoginAttemptLimiterTest
- SessionServiceTest
- Performance Audit
- QuotaDetailsScreen.kt
- Kotlin Audit
- sortShips
- ReportsDomainCalculationsTest
- CrashReporter.kt
- MicroCache
- Secrets
- PermissionPoller
- QuotaPercentageDialogSection.kt
- RegisterCargoScreen.kt
- LoginScreen.kt
- DateRangePicker.kt
- QuotaWarningDialog.kt
- ColorSelector
- AdvancedSearchDialog.kt
- Android Audit
- Code Quality
- Database Audit
- SplashScreen.kt
- MainScreen.kt
- UpdateDialog.kt
- Jetpack Compose Audit
- ApiV2Routes
- PHP Backend Audit
- ChatRepository
- CargoDetailsScreen.kt
- Logging & Observability
- CargoInfo
- LoginAttemptLimiter
- InitialInfoScreen
- ShipSortingMode
- AppDatabase
- Authentication & Authorization
- MessageInputArea
- Quota
- ReportsCommonWidgets.kt
- formatNumber
- Dependency Audit
- Animation Audit
- Migrations
- TokenStore
- Config
- پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13)
- UserRepository
- Ship
- Database
- QuotaSelectionDialog.kt
- preprocessImage
- Memory Audit
- Prioritized Action Plan
- AuthenticatesRequests.php
- ChatViewModel
- AuthViewModel
- jdate
- ReportsNavigation.kt
- CargoViewModel.kt
- QuotaEntryDialog.kt
- PasswordGateService
- UpdateManager
- ComprehensiveAnalyticsDialog.kt
- CargoCounterOperationScreen
- ActiveQuotasDialogSection.kt
- ShipDetailsScreen.kt
- CargoCounterComponents.kt
- JalaliDateUtils
- چک‌لیست فعال‌سازی `STRICT_TRANS_TABLES` (DEEP_CODE_AUDIT.md #Phase4.10)
- ChatNotificationWorker.kt
- What You Must Do When Invoked
- graphify reference: query, path, explain
- DownloadState
- ComponentDefaults.kt
- ReportsViewModel.kt
- RetrofitClient
- Architecture Overview
- Error Handling
- CryptoManager
- AnalyticsController
- CargoEntryNavigation.kt
- AnimationManager
- Color.kt
- TokenAuthenticator
- rememberAdaptiveLayoutConfig
- ColorScheme.kt
- StartupState
- SearchBar.kt
- SecurityErrorType
- UserPreferencesManager
- SecurityVerifier
- ReportModels.kt
- Application
- StatisticsCard.kt
- TokenRefresher
- Constants.kt
- AppApiController
- InputValidator
- PHP/composer.json
- QuotaCalculatorTest
- secrets.cpp
- AppNotificationManager
- SessionManager
- SessionManager
- ShipInfoSection.kt
- Request
- ChatController
- DatabaseSchemaExporter
- graphify reference: extra exports and benchmark
- NavRoutes
- LoadingState
- NotificationActionReceiver.kt
- gradlew
- graphify reference: add a URL and watch a folder
- graphify reference: commit hook and native CLAUDE.md integration
- ExampleInstrumentedTest
- HardwarePerformanceEvaluator
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
1. `ReportsViewModel` - 97 edges
2. `CargoViewModel` - 77 edges
3. `InputValidator` - 61 edges
4. `ApiServiceV2` - 58 edges
5. `UserPreferencesManager` - 52 edges
6. `formatNumber()` - 45 edges
7. `UpdateManager` - 37 edges
8. `ActiveShipInfo` - 37 edges
9. `Request` - 35 edges
10. `SessionRepository` - 35 edges

## Surprising Connections (you probably didn't know these)
- `toDto()` --calls--> `InitialInfo`  [EXTRACTED]
  app/src/main/java/com/atk/atk_cargo/domain/model/QuotaInfo.kt → core/network/src/main/java/com/atk/atk_cargo/data/model/CargoModels.kt
- `ActiveQuotasDialog()` --calls--> `ActiveShipInfo`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/ActiveQuotasDialogSection.kt → core/network/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt
- `InitialInfoScreen()` --calls--> `InitialInfo`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/InitialInfoScreen.kt → core/network/src/main/java/com/atk/atk_cargo/data/model/CargoModels.kt
- `navigateToRegisterCargoActivity()` --calls--> `InitialInfo`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/SelectInfoScreen.kt → core/network/src/main/java/com/atk/atk_cargo/data/model/CargoModels.kt
- `ChatViewModel` --references--> `ChatMessageEntity`  [EXTRACTED]
  app/src/main/java/com/atk/atk_cargo/api/ChatViewModel.kt → core/database/src/main/java/com/atk/atk_cargo/data/db/ChatMessageEntity.kt

## Import Cycles
- None detected.

## Communities (203 total, 43 thin omitted)

### Community 0 - "StartupViewModel"
Cohesion: 0.13
Nodes (9): AndroidViewModel, Flow, Intent, StateFlow, RequestBatteryOptimization, SecurityCheckState, ShowMessage, StartupEvent (+1 more)

### Community 1 - "ReportsRepository"
Cohesion: 0.10
Nodes (9): ErrorResponse, HttpStatusException, Exception, Result, ReportsRepository, ComprehensiveAnalysisResponse, QuotaDetails, QuotaStatusResponse (+1 more)

### Community 2 - "Security Audit"
Cohesion: 0.09
Nodes (23): Critical Issues, [CRITICAL] فضای رمز عبور ۴ رقمی عددی + هش بدون salt + قفل قابل دور زدن = تصاحب کامل حساب, [HIGH] `app_api.php` تمام داده‌ی گزارش‌گیری را بدون بررسی مجوز `view_reports` می‌دهد, High Issues, [HIGH] `PermissionManager.php`: نبود `session_regenerate_id` و نبود قفل brute-force, [HIGH] `PHP/vendor/` (شامل phpunit و phpstan) در web root و کامیت‌شده در گیت, [HIGH] `protected_proxy.php` یک مرز امنیتی نیست — همه‌ی endpointها مستقیماً هم قابل فراخوانی‌اند, [HIGH] session token و refresh token به‌صورت plaintext در دیتابیس ذخیره می‌شوند (+15 more)

### Community 3 - "CargoCounterScreen.kt"
Cohesion: 0.19
Nodes (18): CargoCounterScreen(), CargoCounterViewModel, filterShipsByTab(), com, CoroutineScope, NavController, SnackbarHostState, StateFlow (+10 more)

### Community 4 - "TextAlign"
Cohesion: 0.26
Nodes (21): DeleteDialog(), DialogBadge(), DialogButtonRow(), DialogContentCard(), DialogMessageText(), DialogTitle(), Color, ImageVector (+13 more)

### Community 6 - "ChatMessageEntity"
Cohesion: 0.15
Nodes (3): ChatDao, Flow, ChatMessageEntity

### Community 7 - "**5.2 / 10**"
Cohesion: 0.11
Nodes (18): **5.2 / 10**, [CRITICAL] نبود پوشش تست روی مسیرهای بحرانی, Final Score, Overall Score, Production Readiness, Project Overview, Project Structure, Recommended Architecture (+10 more)

### Community 9 - "CargoDetailsComponents.kt"
Cohesion: 0.22
Nodes (15): QuotaInfo, toDomain(), toDto(), CargoInfoCard(), CargoListSection(), GroupStats(), InfoChips(), InfoGrid() (+7 more)

### Community 10 - "ActiveQuotasContent.kt"
Cohesion: 0.23
Nodes (17): formatNumber(), EmptySearchResult(), extractLastDigits(), FilterChip(), FilterState, ALL, COMPLETED, PENDING (+9 more)

### Community 11 - "ChatToolbar.kt"
Cohesion: 0.25
Nodes (13): ColorWheel(), Color, Modifier, getAdaptiveBubbleColor(), AdvancedColorPickerDialog(), ChatSettingsDialogEnhanced(), ChatSettingsPreviewRefined(), Color (+5 more)

### Community 12 - "SelectInfoScreen.kt"
Cohesion: 0.25
Nodes (14): AnimatedHeader(), GroupedShipList(), Color, Modifier, NavController, navigateToRegisterCargoActivity(), SelectInfoScreenContent(), ShipCardDesign() (+6 more)

### Community 14 - "ApiServiceV2"
Cohesion: 0.07
Nodes (19): ApiResponse, ApiResponse2, ApiServiceV2, CargoDeleteResponse, CargoInfoResponse, CargoInfoSearch, CargoSearchResponse, CargoStats (+11 more)

### Community 15 - "ApiException"
Cohesion: 0.10
Nodes (7): App\Enums\CargoStatus, Exception, mysqli_stmt, DatabaseManager, ApiException, ConflictException, DatabaseException

### Community 16 - "Executive Summary"
Cohesion: 0.10
Nodes (19): Deep Code Audit Report, Executive Summary, Final Recommendations, **Overall: 5.2 / 10**, Overall Score, Production Readiness, Technical Debt, Top 20 Priority List (+11 more)

### Community 17 - "Cargo"
Cohesion: 0.32
Nodes (14): Cargo, CargoDetailsDialog(), Color, ImageVector, Modifier, ModernCargoDetailsGrid(), ModernCargoStatusSection(), ModernCargoStatusTimeline() (+6 more)

### Community 18 - "PermissionService"
Cohesion: 0.07
Nodes (5): ApiAuthGate, Router, PermissionRepository, PermissionService, PermissionServiceTest

### Community 19 - "quota_details/QuotaCardComponents.kt"
Cohesion: 0.17
Nodes (20): ActionButtonTest, DeleteQuotaDialog(), androidx, Color, QuotaConfirmDialog(), ToggleQuotaStatusDialog(), ActionButton(), ConfirmationDialogHeader() (+12 more)

### Community 21 - "CargoEditSearchDialogsSection.kt"
Cohesion: 0.30
Nodes (16): CargoChangesPreview(), CargoEditConfirmDialog(), CargoEditConfirmHeader(), CargoMainInfo(), CargoShippingInfo(), CargoTimeInfo(), CargoWeightInfo(), ChangeItem() (+8 more)

### Community 22 - "HomeScreen.kt"
Cohesion: 0.13
Nodes (24): getMenuItemsForUserType(), CompactSwitch(), Color, ImageVector, Modifier, NotificationSettingRow(), ActionButtons(), getUserTypeDisplay() (+16 more)

### Community 23 - "ManageReportsScreen.kt"
Cohesion: 0.29
Nodes (14): QuotasDialog(), addOneDayToPersianDate(), DatePickerDialog(), DateTimeSelectionCard(), getDaysInPersianMonth(), isPersianLeapYear(), Color, ImageVector (+6 more)

### Community 24 - "LogoutRequest"
Cohesion: 0.33
Nodes (4): Result, LogoutUseCase, LogoutRequest, LogoutResponse

### Community 26 - "MessageType"
Cohesion: 0.17
Nodes (14): Modifier, StatusSnackbar(), CargoSnackbarQueue, StateFlow, Color, Modifier, snackbarAccent(), snackbarAccentBg() (+6 more)

### Community 27 - "QuotaService"
Cohesion: 0.08
Nodes (4): AppApiCacheKeys, AuditLogger, QuotaService, UserService

### Community 28 - "CameraSection.kt"
Cohesion: 0.17
Nodes (13): EnhancedNumberAnalyzer, extractNumber(), ImageProxy, InputImage, recognizeTextFromImage(), EnhancedCameraPreview(), ImageCapture, Color (+5 more)

### Community 29 - "QuotaManagementContent.kt"
Cohesion: 0.15
Nodes (29): AnalyticsStatChipMini(), CompactStatChip(), InfoChip(), IntegratedQuotaCard(), Color, ImageVector, Modifier, QuotaShipExpansionPanel() (+21 more)

### Community 30 - "MainActivity.kt"
Cohesion: 0.24
Nodes (6): ServerSyncingScreen(), Intent, MainActivity, StartupErrorScreen(), Bundle, ComponentActivity

### Community 33 - "AuthModels.kt"
Cohesion: 0.11
Nodes (13): AuthRepositoryImpl, Flow, Result, validateServerSession(), ActiveSessionResponse, ForceLogoutResponse, LoginRequest, LoginResponse (+5 more)

### Community 34 - "WarehouseDetailsScreen.kt"
Cohesion: 0.14
Nodes (25): EmptyVoucherList(), Modifier, SearchTextField(), SortChip(), VoucherDetailsDialog(), VoucherExpandedDetails(), VoucherItem(), VoucherSearchAndFilter() (+17 more)

### Community 35 - "UserManagementDialogsSection.kt"
Cohesion: 0.32
Nodes (11): UserTypeInfo, DesignTextField(), EnhancedAddUserDialog(), EnhancedEditUserDialog(), getUserTypes(), androidx, KeyboardOptions, Modifier (+3 more)

### Community 36 - "UsersManager"
Cohesion: 0.07
Nodes (7): ApiManager, CONFIG, ModalManager, ThemeManager, UIManager, UsersManager, Utils

### Community 37 - "AppModule.kt"
Cohesion: 0.22
Nodes (7): AuthRepository, ConflictSession, Error, Flow, LoginResult, Success, LoginUseCase

### Community 40 - "Logger"
Cohesion: 0.15
Nodes (3): AuthController, Logger, self

### Community 41 - "API Audit"
Cohesion: 0.17
Nodes (12): API Audit, [HIGH] دو API stack موازی — نسخه‌ی امن‌تر بلااستفاده است, [MEDIUM] طراحی API غیر-RESTful و ناسازگار, فاز ۳.۲ — ✅ انجام شد: حذف ۲۰ فایل ورودی legacy نسخه ۱, فاز ۳.۳ — ✅ کامل شد: تست‌های Phase 1/2, فاز ۳.۴ — ✅ انجام شد: وابستگی‌های تست, فاز ۳.۵ — ✅ کامل: `UiState` واحد در `CargoViewModel` و `ReportsViewModel`, فهرست endpointها (+4 more)

### Community 42 - "SecurityScreen.kt"
Cohesion: 0.25
Nodes (13): AdvancedOrbitalScanner(), AnimatedUpdateIcon(), AnimatedWarningIcon(), DynamicPremiumBackground(), InteractiveExitButton(), Modifier, RetryButton(), SecurityBlockScreen() (+5 more)

### Community 43 - "ATKCargoTheme"
Cohesion: 0.14
Nodes (11): ComponentStyles, Dimensions, Elevation, ATKCargoTheme, Motion, SemanticColors, AppShapes, Spacing (+3 more)

### Community 44 - "UserManagementScreen.kt"
Cohesion: 0.13
Nodes (21): EnhancedDeleteConfirmationDialog(), EnhancedForceLogoutDialog(), DashboardSummaryRow(), EmptyStateView(), EnterpriseUserCard(), getUserTypeDisplay(), androidx, Color (+13 more)

### Community 45 - "QuotaAnalysisSection.kt"
Cohesion: 0.25
Nodes (16): AnalyticsGroupingModeButton(), AnalyticsOwnerSummaryCard(), AnalyticsQuotaCard(), AnalyticsQuotaGroupExpansionPanel(), AnalyticsStatChip(), Context, ImageVector, KeyboardType (+8 more)

### Community 46 - "FontWeight"
Cohesion: 0.20
Nodes (9): ConfirmationDialog(), ImageVector, EmptyState(), ImageVector, Modifier, AnimatedCounter(), ExitStatusDialog(), ImageVector (+1 more)

### Community 47 - "ChatScreen.kt"
Cohesion: 0.27
Nodes (12): ChatUiItem, getChatBackgroundColor(), getDateHeaderColor(), Header, Color, Message, ChatScreen(), DateHeader() (+4 more)

### Community 48 - "CargoInfoDetailsDialogSection.kt"
Cohesion: 0.35
Nodes (12): CargoDetailsPalette, CargoDetailTabItem(), CargoInfoDetailsDialog(), DetailInfoRow(), ImageVector, Modifier, SnackbarHostState, MainInfoTabContent() (+4 more)

### Community 49 - "ActiveShipInfo"
Cohesion: 0.34
Nodes (11): GroupedShipList(), Color, QuotaCard(), ShipGroup(), ShipHeader(), WarehouseCard(), WarehouseList(), ShipSelectionDialog() (+3 more)

### Community 50 - "MessageBubble.kt"
Cohesion: 0.27
Nodes (13): extractShipInfoAndText(), com, replaceUsernamesWithFullNames(), ShipInfoModel, ChatInfoRow(), EditMessageDialog(), androidx, Color (+5 more)

### Community 53 - "Performance Audit"
Cohesion: 0.20
Nodes (10): [HIGH] `permissions.json` در هر بررسی مجوز از دیسک خوانده و parse می‌شود, [LOW] subquery همبسته در فهرست پیام‌های چت, [MEDIUM] `updateUser` کل جدول کاربران را برای یافتن رکورد خود کاربر می‌خواند, [MEDIUM] `user_sessions` با ۱۳ ایندکس که در هر درخواست به‌روزرسانی می‌شود, [MEDIUM] درخواست‌های شبکه‌ی زائد بعد از هر ثبت حواله, [MEDIUM] نبود صفحه‌بندی در endpointهای فهرستی, Performance Audit, خلاصه‌ی اثر (+2 more)

### Community 54 - "QuotaDetailsScreen.kt"
Cohesion: 0.32
Nodes (10): ApiQuotaDetails, adjustColorForTheme(), Color, toTon(), ProgressBar(), QuotaAdditionalInfo(), QuotaDetails(), QuotaInfoCards() (+2 more)

### Community 55 - "Kotlin Audit"
Cohesion: 0.22
Nodes (9): [HIGH] باگ منطقی: مسیر پاک‌سازی نشست منقضی در `PermissionPoller` هرگز اجرا نمی‌شود, Kotlin Audit, [LOW] `Strictness.LENIENT` در Gson, [MEDIUM] `runBlocking` داخل `Authenticator` روی thread شبکه‌ی OkHttp, [MEDIUM] بسته‌بندی مکرر استثنا که نوع و stack trace را نابود می‌کند, [MEDIUM] بلعیدن خطا و بازگرداندن نتیجه‌ی خالی در جستجوها, [MEDIUM] پیاده‌سازی دوگانه‌ی تبدیل تاریخ جلالی, نکات مثبت (+1 more)

### Community 57 - "ReportsDomainCalculationsTest"
Cohesion: 0.13
Nodes (7): calculatePercentage(), calculateProgress(), format(), formatWeightWithDetail(), Context, QuotaWarningThresholds, ReportsDomainCalculationsTest

### Community 58 - "CrashReporter.kt"
Cohesion: 0.46
Nodes (4): CrashReporter, Context, CoroutineScope, PendingCrashReport

### Community 59 - "MicroCache"
Cohesion: 0.11
Nodes (3): CargoController, MicroCache, CargoService

### Community 61 - "PermissionPoller"
Cohesion: 0.36
Nodes (3): Job, StateFlow, PermissionPoller

### Community 62 - "QuotaPercentageDialogSection.kt"
Cohesion: 0.17
Nodes (21): calculateValues(), AnimatedNumber(), DialogHeader(), Color, ImageVector, Modifier, lerp(), PercentageDisplay() (+13 more)

### Community 63 - "RegisterCargoScreen.kt"
Cohesion: 0.16
Nodes (14): CargoConfirmStatus, AWAITING_CONFIRMATION, CONFIRMED, PENDING, CargoStatus, ENTERED, EXITED, DuplicateTrackingNumbersDialog() (+6 more)

### Community 64 - "LoginScreen.kt"
Cohesion: 0.19
Nodes (20): CompactLoginLayout(), ExpandedLoginLayout(), IndustrialBrandingHeaderContent(), IndustrialFeatureBadge(), IndustrialFooterInfo(), IndustrialInputField(), IndustrialLoginFormCard(), androidx (+12 more)

### Community 65 - "DateRangePicker.kt"
Cohesion: 0.57
Nodes (7): DateClickableField(), DateRangePicker(), getDaysInPersianMonth(), isPersianLeapYear(), Color, Modifier, PersianDatePickerDialog()

### Community 66 - "QuotaWarningDialog.kt"
Cohesion: 0.26
Nodes (16): CompactInfoSection(), DialogHeader(), Color, ImageVector, Modifier, LoadingActionButton(), NavigationButton(), PageIndicatorDot() (+8 more)

### Community 67 - "ColorSelector"
Cohesion: 0.46
Nodes (3): adjustColorForTheme(), ColorSelector, Color

### Community 68 - "AdvancedSearchDialog.kt"
Cohesion: 0.36
Nodes (7): AdvancedSearchDialog(), Modifier, SearchTabInfo, SearchTypeTabRow(), SearchType, RECEIPT_NUMBER, TRACKING_NUMBER

### Community 69 - "Android Audit"
Cohesion: 0.29
Nodes (7): Android Audit, [LOW] `versionCode = 11` در برابر `versionName = "4.0.1"`, [MEDIUM] `targetSdk = 34` در حالی که `compileSdk = 36` است, [MEDIUM] `viewBinding = true` در یک اپ کاملاً Compose, [MEDIUM] وابستگی‌های بلااستفاده در گراف build, نکات مثبت (تأییدشده), یافته‌ها

### Community 70 - "Code Quality"
Cohesion: 0.33
Nodes (6): Code Quality, Code Smells شناسایی‌شده, [HIGH] Primitive Obsession روی مقادیر وزن و تناژ, [MEDIUM] DTO و مدل دامنه تفکیک نشده‌اند, [MEDIUM] Magic Numbers بدون نام, مهم‌ترین‌ها با جزئیات

### Community 71 - "Database Audit"
Cohesion: 0.29
Nodes (7): Database Audit, [LOW] `SET SESSION sql_mode = 'NO_ENGINE_SUBSTITUTION'` حالت strict را خاموش می‌کند, [MEDIUM] `schema.sql` قدیمی و ناقص است, [MEDIUM] هیچ FOREIGN KEY در کل schema وجود ندارد, ساختار (از `schema.sql` — ✅ به‌روزرسانی شد در Phase 2.3، اکنون کامل), نکات مثبت, یافته‌ها

### Community 72 - "SplashScreen.kt"
Cohesion: 0.27
Nodes (8): AnimatedBrandTitle(), SplashScreen(), SplashScreenConstants, SplashScreenContent(), SplashSubtitleBadge(), SplashVersionBadge(), VazirmatnFontFamily, FontFamily

### Community 73 - "MainScreen.kt"
Cohesion: 0.16
Nodes (16): MainScreen(), RouteTransitions, standardTransitions(), LoginScreen(), CargoRegistrationRoute, cargoRegistrationScreen(), NavController, navigateToCargoRegistration() (+8 more)

### Community 74 - "UpdateDialog.kt"
Cohesion: 0.33
Nodes (11): DownloadActionButtons(), DownloadingState(), DownloadProgressInfo(), DownloadSizeInfo(), DownloadSpeedInfo(), ErrorState(), ModernUpdateActionSection(), ModernUpdateContent() (+3 more)

### Community 75 - "Jetpack Compose Audit"
Cohesion: 0.29
Nodes (7): [HIGH] God Composable — فایل‌های نمایشی با بیش از ۱۰۰۰ خط, [HIGH] صفر مورد `collectAsStateWithLifecycle` در برابر ۸۵ مورد `collectAsState`, Jetpack Compose Audit, [LOW] تمام رشته‌های UI به‌صورت literal در کد, [MEDIUM] استفاده‌ی کم از `derivedStateOf` در جاهایی که واقعاً لازم است, [MEDIUM] نبود `UiState` واحد — ۲۰+ `StateFlow` مجزا در هر ViewModel, [MEDIUM] ۲۲ مورد از ۳۵ فراخوانی `items(...)` بدون `key`

### Community 76 - "ApiV2Routes"
Cohesion: 0.10
Nodes (5): InputValidationResult, QuotaValidationUseCase, TempTonnageValidationResult, ApiV2Routes, QuotaValidationResult

### Community 77 - "PHP Backend Audit"
Cohesion: 0.29
Nodes (7): [HIGH] `migrations/` خالی است و `schema.sql` ناقص — جدول `audit_log` هرگز ساخته نمی‌شود, [LOW] `Config::$settings['session_timeout']` و `admin_password_hash` بلااستفاده‌اند, [MEDIUM] `execute()` در پروکسی فقط `Exception` را می‌گیرد، نه `Throwable`, [MEDIUM] `flushLog` پروکسی وقتی هدف `exit` می‌کند اجرا نمی‌شود, PHP Backend Audit, نکات مثبت, یافته‌ها

### Community 78 - "ChatRepository"
Cohesion: 0.11
Nodes (11): ChatRepository, com, Flow, Result, ChatMessage, ChatMessagesResponse, SendMessageRequest, SendMessageResponse (+3 more)

### Community 79 - "CargoDetailsScreen.kt"
Cohesion: 0.30
Nodes (11): CargoDetailsScreen(), confirmCargo(), FloatingActionButtonItem(), handleCargoConfirmation(), Color, ImageVector, NavController, Result (+3 more)

### Community 80 - "Logging & Observability"
Cohesion: 0.29
Nodes (7): [HIGH] پوشه‌های لاگ ممکن است از طریق وب قابل دسترس باشند, Logging & Observability, [LOW] ~۹۰ فراخوانی `Log.*` در کد تولید, [MEDIUM] نبود مانیتورینگ و هشدار, آنچه لاگ می‌شود, نکات مثبت, یافته‌ها

### Community 81 - "CargoInfo"
Cohesion: 0.19
Nodes (11): toDomain(), toDto(), SubmitCargoUseCase, CargoSearchResultCard(), InfoRowCompact(), Color, Context, ImageVector (+3 more)

### Community 83 - "InitialInfoScreen"
Cohesion: 0.17
Nodes (23): formatNumber(), isValidPersianText(), isValidQuotaNumber(), isValidShipName(), isValidWarehouseName(), isValidWeight(), ConfirmationDialog(), DuplicateDialog() (+15 more)

### Community 84 - "ShipSortingMode"
Cohesion: 0.25
Nodes (7): ShipSortingMode, LOADED_TONNAGE_ASC, LOADED_TONNAGE_DESC, NAME_ASC, NAME_DESC, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC

### Community 85 - "AppDatabase"
Cohesion: 0.47
Nodes (3): AppDatabase, Context, RoomDatabase

### Community 86 - "Authentication & Authorization"
Cohesion: 0.40
Nodes (5): Authentication & Authorization, [MEDIUM] `check_logout.php` بدون احراز هویت, ارزیابی, جریان کامل احراز هویت, یافته‌ی اضافی

### Community 87 - "MessageInputArea"
Cohesion: 0.52
Nodes (6): rotateIcon(), Color, com, MessageInputArea(), QuotaSelectionDialog(), ShipSelectionDialog()

### Community 88 - "Quota"
Cohesion: 0.28
Nodes (14): buildQuotasShareText(), shareQuotasData(), MinimalQuotaCard(), GroupingModeButton(), GroupingModeSelector(), ImageVector, Modifier, QuotaGroupExpansionPanel() (+6 more)

### Community 89 - "ReportsCommonWidgets.kt"
Cohesion: 0.29
Nodes (14): FabItem, CompactStatChip(), EmptyQuotaState(), EmptyShipsState(), EmptyStateCard(), ErrorStateCard(), FloatingActionButton(), InfoCard() (+6 more)

### Community 90 - "formatNumber"
Cohesion: 0.22
Nodes (16): formatNumber(), DialogHeader(), Color, Modifier, RealTimeLoadingBottomSheet(), RealTimeShiftNavigation(), RefreshOverlay(), ShipCard() (+8 more)

### Community 91 - "Dependency Audit"
Cohesion: 0.33
Nodes (6): Android, Dependency Audit, [LOW] `material3 = "1.3.2"` صریح، نسخه‌ی BOM را override می‌کند, [MEDIUM] `composer.json` قید `php: >=7.4` دارد ولی کد PHP 8 لازم دارد, [MEDIUM] `itextpdf 5.5.13.4` — مجوز AGPL و پایان پشتیبانی, PHP

### Community 92 - "Animation Audit"
Cohesion: 0.33
Nodes (6): Animation Audit, [INFO] نقاط قوت انیمیشن, [LOW] انیمیشن شمارنده‌ی معکوس ثانیه‌ای که هر ثانیه recomposition ایجاد می‌کند, [MEDIUM] نبود پشتیبانی از Reduce Motion / دسترس‌پذیری, وضعیت کلی, یافته‌ها

### Community 93 - "Migrations"
Cohesion: 0.33
Nodes (5): Migrations, migrationهای اعمال‌شده تاکنون, قرارداد نام‌گذاری, نحوه‌ی اجرا, وضعیت schema.sql

### Community 96 - "پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13)"
Cohesion: 0.22
Nodes (8): ساختار فعلی (مستند شده در این جلسه), ساختار هدف, مراحل اجرا (ترتیب دقیق مهم است), معیار موفقیت, پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13), پیش‌نیازهای جانبی که باید قبل از این کار حل شوند, چرا فقط .htaccess کافی نیست, چرا لازم است

### Community 97 - "UserRepository"
Cohesion: 0.12
Nodes (3): PDO, PDO, UserRepository

### Community 98 - "Ship"
Cohesion: 0.22
Nodes (14): ErrorState(), ImageVector, Modifier, ImageVector, Modifier, ShipCard(), ShipCardContent(), ShipSortingModeButton() (+6 more)

### Community 99 - "Database"
Cohesion: 0.08
Nodes (11): App\Core\AuthenticatesRequests, App\Enums\CargoConfirmStatus, InvalidArgumentException, mysqli, PDOException, LicenseController, UtilityController, Database (+3 more)

### Community 100 - "QuotaSelectionDialog.kt"
Cohesion: 0.42
Nodes (10): DialogHeader(), findDifferentFields(), InactiveIndicator(), ImageVector, QuotaDetailItem(), QuotaDetails(), QuotaHeader(), QuotaItem() (+2 more)

### Community 101 - "preprocessImage"
Cohesion: 0.50
Nodes (8): adaptiveThresholding(), applyContrastEnhancement(), enhanceEdges(), ImageProxy, InputImage, medianFilter(), preprocessImage(), IntArray

### Community 102 - "Memory Audit"
Cohesion: 0.40
Nodes (5): [LOW] `ChatNotificationWorker` و چرخه‌ی حیات دیتابیس, [MEDIUM] فهرست‌های بدون سقف در `StateFlow`, Memory Audit, وضعیت کلی: **۷/۱۰ — بهترین حوزه‌ی پروژه**, یافته‌ها

### Community 103 - "Prioritized Action Plan"
Cohesion: 0.33
Nodes (6): Phase 1 — Immediate (این هفته — بازدارنده‌های تولید), Phase 2 — High Priority (۲ تا ۴ هفته), Phase 3 — Medium Priority (۱ تا ۳ ماه), Phase 4 — Optimization (۳ تا ۶ ماه), Phase 5 — تکمیل موارد باقی‌مانده (اجرا شده پس از بازبینی نهایی گزارش), Prioritized Action Plan

### Community 104 - "AuthenticatesRequests.php"
Cohesion: 0.80
Nodes (4): enforceMinAppVersion(), requireAuthenticatedSession(), requirePermission(), sendAuthErrorResponse()

### Community 105 - "ChatViewModel"
Cohesion: 0.10
Nodes (7): ChatViewModel, ChatViewModelFactory, com, StateFlow, T, ViewModel, ViewModelProvider

### Community 106 - "AuthViewModel"
Cohesion: 0.18
Nodes (7): AuthViewModel, StateFlow, ViewModel, LoginFormState, For --cluster-only, For --update (incremental re-extraction), graphify reference: incremental update and cluster-only

### Community 107 - "jdate"
Cohesion: 0.53
Nodes (9): gregorian_to_jalali(), jalali_to_gregorian(), jcheckdate(), jdate(), jdate_words(), jgetdate(), jmktime(), jstrftime() (+1 more)

### Community 108 - "ReportsNavigation.kt"
Cohesion: 0.36
Nodes (7): CargoDetailsRoute, cargoDetailsScreen(), NavController, ManageShipsRoute, manageShipsScreen(), navigateToCargoDetails(), navigateToManageShips()

### Community 109 - "CargoViewModel.kt"
Cohesion: 0.15
Nodes (7): Kilograms, CargoUiState, CargoViewModelFactory, StateFlow, T, ViewModel, ViewModelProvider

### Community 110 - "QuotaEntryDialog.kt"
Cohesion: 0.57
Nodes (7): FocusRequester, QuotaEntryActions(), QuotaEntryContent(), QuotaEntryDialog(), QuotaEntryPalette, rememberQuotaEntryPalette(), validateAndSubmit()

### Community 113 - "UpdateManager"
Cohesion: 0.13
Nodes (7): UpdateInfo, DownloadProgress, Context, Job, StateFlow, UpdateManager, VersionCheckResult

### Community 114 - "ComprehensiveAnalyticsDialog.kt"
Cohesion: 0.57
Nodes (6): AnalyticsDateNavigation(), AnalyticsHeaderCard(), ComprehensiveAnalyticsDialog(), EmptyStateCard(), ErrorStateCard(), Modifier

### Community 115 - "CargoCounterOperationScreen"
Cohesion: 0.36
Nodes (6): CargoCounterRoute, cargoCounterScreen(), NavController, navigateToCargoCounter(), CargoCounterOperationScreen(), NavController

### Community 116 - "ActiveQuotasDialogSection.kt"
Cohesion: 0.29
Nodes (11): ViewMode, FLAT, GROUPED, ActiveQuotasDialog(), FilterBar(), Color, Dp, Modifier (+3 more)

### Community 117 - "ShipDetailsScreen.kt"
Cohesion: 0.42
Nodes (8): calculateWarningStatus(), ErrorStateCard(), ImageVector, ShipDetails(), ShipHeaderCard(), TabItem(), WarehouseQuotasTabs(), WarehousesAndQuotasTab()

### Community 118 - "CargoCounterComponents.kt"
Cohesion: 0.57
Nodes (6): CargoSnackbarMessage, Color, Modifier, snackbarAccent(), snackbarAccentBg(), StatusSnackbar()

### Community 120 - "چک‌لیست فعال‌سازی `STRICT_TRANS_TABLES` (DEEP_CODE_AUDIT.md #Phase4.10)"
Cohesion: 0.33
Nodes (5): مراحل امن (ترتیب مهم است), مسئله چیست, معیار موفقیت, چرا نمی‌شود همین الان فعالش کرد, چک‌لیست فعال‌سازی `STRICT_TRANS_TABLES` (DEEP_CODE_AUDIT.md #Phase4.10)

### Community 121 - "ChatNotificationWorker.kt"
Cohesion: 0.48
Nodes (4): ChatNotificationWorker, KoinComponent, CoroutineWorker, ListenableWorker

### Community 122 - "What You Must Do When Invoked"
Cohesion: 0.07
Nodes (26): For /graphify add and --watch, For /graphify query, For the commit hook and native CLAUDE.md integration, For --update and --cluster-only, /graphify, Honesty Rules, Interpreter guard for subcommands, Part A - Structural extraction for code files (+18 more)

### Community 123 - "graphify reference: query, path, explain"
Cohesion: 0.40
Nodes (5): For /graphify explain, For /graphify path, graphify reference: query, path, explain, Step 0 — Constrained query expansion (REQUIRED before traversal), Step 1 — Traversal

### Community 124 - "DownloadState"
Cohesion: 0.29
Nodes (6): Completed, Downloading, DownloadState, Error, Idle, Paused

### Community 128 - "ReportsViewModel.kt"
Cohesion: 0.17
Nodes (12): buildQuotaGroups(), QuotaGroup, AndroidViewModel, Color, Job, StateFlow, ComprehensiveAnalytics, QuotaCompletionData (+4 more)

### Community 129 - "RetrofitClient"
Cohesion: 0.26
Nodes (9): FloatTypeAdapter, Context, OkHttpClient, RetrofitClient, HttpLoggingInterceptor, JsonReader, JsonWriter, Retrofit (+1 more)

### Community 130 - "Architecture Overview"
Cohesion: 0.50
Nodes (4): Architecture Overview, جریان طراحی‌شده ولی بلااستفاده, جریان واقعی داده (آنچه امروز اجرا می‌شود), نقاط مرزی پرریسک بین لایه‌ها

### Community 131 - "Error Handling"
Cohesion: 0.50
Nodes (4): Error Handling, [MEDIUM] نبود مکانیزم مرکزی گزارش خطا (Crash Reporting), نکات مثبت, یافته‌های تجمیعی

### Community 132 - "CryptoManager"
Cohesion: 0.33
Nodes (4): CryptoManager, ByteArray, Cipher, SecretKey

### Community 134 - "CargoEntryNavigation.kt"
Cohesion: 0.25
Nodes (9): InitialInfoRoute, initialInfoScreen(), NavController, navigateToInitialInfo(), navigateToSelectInfo(), SelectInfoRoute, selectInfoScreen(), CargoOperationScreen() (+1 more)

### Community 136 - "Color.kt"
Cohesion: 0.24
Nodes (7): CompactStatChipTest, persianDateFormat(), DateTimePicker(), Modifier, PersianDateItem(), TimePickerDialog(), TimePickerState

### Community 137 - "TokenAuthenticator"
Cohesion: 0.36
Nodes (4): Authenticator, TokenAuthenticator, AuthErrorBody, Route

### Community 138 - "rememberAdaptiveLayoutConfig"
Cohesion: 0.33
Nodes (6): AdaptiveLayoutConfig, rememberAdaptiveLayoutConfig(), WindowSizeClass, COMPACT, EXPANDED, MEDIUM

### Community 139 - "ColorScheme.kt"
Cohesion: 0.80
Nodes (5): ColorScheme, buildAppDarkColorScheme(), buildAppLightColorScheme(), compositeOver(), Color

### Community 140 - "StartupState"
Cohesion: 0.33
Nodes (6): Ready, SecurityBlocked, Splash, StartupState, Syncing, VersionExpired

### Community 142 - "SecurityErrorType"
Cohesion: 0.33
Nodes (6): SecurityErrorType, LICENSE_INACTIVE, LICENSE_NOT_FOUND, NETWORK_ERROR, TAMPERED, UNKNOWN_ERROR

### Community 144 - "UserPreferencesManager"
Cohesion: 0.05
Nodes (19): BootReceiver, BroadcastReceiver, Context, Intent, KoinComponent, Context, Intent, KoinComponent (+11 more)

### Community 145 - "SecurityVerifier"
Cohesion: 0.24
Nodes (4): SecurityVerifier, JSONObject, PackageInfo, Signature

### Community 147 - "ReportModels.kt"
Cohesion: 0.09
Nodes (17): AnalyticsData, DateInfo, ExistingQuota, ExitDateInfo, GroupSortingMode, ALPHABETICAL, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC (+9 more)

### Community 149 - "StatisticsCard.kt"
Cohesion: 0.70
Nodes (4): Color, ImageVector, Modifier, StatisticsCard()

### Community 150 - "TokenRefresher"
Cohesion: 0.50
Nodes (3): OkHttpClient, TokenRefresher, RefreshTokenResponse

### Community 178 - "PHP/composer.json"
Cohesion: 0.09
Nodes (21): autoload, autoload-dev, psr-4, psr-4, config, optimize-autoloader, sort-packages, description (+13 more)

### Community 190 - "QuotaCalculatorTest"
Cohesion: 0.09
Nodes (3): QuotaCalculator, QuotaCalculatorTest, PHPUnit\Framework\TestCase

### Community 221 - "secrets.cpp"
Cohesion: 0.34
Nodes (16): decryptXor(), JNI_OnLoad(), n0(), n1(), n2(), n3(), n4(), n5() (+8 more)

### Community 253 - "ShipInfoSection.kt"
Cohesion: 0.28
Nodes (13): ShipInfo, formatNumber(), toEnglishNumbers(), DetailedInfoGrid(), DetailInfoItem(), ExpandedContent(), Color, ImageVector (+5 more)

### Community 272 - "Request"
Cohesion: 0.09
Nodes (5): DiagnosticsController, OnlineUsersController, UserController, MinVersionGate, Request

### Community 319 - "graphify reference: extra exports and benchmark"
Cohesion: 0.22
Nodes (8): graphify reference: extra exports and benchmark, Step 6b - Wiki (only if --wiki flag), Step 7 - Neo4j export (only if --neo4j or --neo4j-push flag), Step 7a - FalkorDB export (only if --falkordb or --falkordb-push flag), Step 7b - SVG export (only if --svg flag), Step 7c - GraphML export (only if --graphml flag), Step 7d - MCP server (only if --mcp flag), Step 8 - Token reduction benchmark (only if total_words > 5000)

### Community 355 - "LoadingState"
Cohesion: 0.47
Nodes (6): Error, Idle, Loading, LoadingState, Success, UiState

### Community 371 - "NotificationActionReceiver.kt"
Cohesion: 0.53
Nodes (4): BroadcastReceiver, Context, Intent, NotificationActionReceiver

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
- **304 isolated node(s):** `CONFIG`, `Utils`, `name`, `description`, `type` (+299 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **43 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CargoViewModel` connect `CargoViewModel` to `TextAlign`, `AppModule.kt`, `CargoEntryNavigation.kt`, `QuotaExistenceMultipleResponse`, `SaveOrUpdateResponse`, `MainScreen.kt`, `CargoDetailsComponents.kt`, `SelectInfoScreen.kt`, `CargoViewModel.kt`, `ApiV2Routes`, `CargoDetailsScreen.kt`, `CargoInfoDetailsDialogSection.kt`, `ActiveShipInfo`, `CargoCounterOperationScreen`, `MessageType`, `RegisterCargoScreen.kt`?**
  _High betweenness centrality (0.052) - this node is a cross-community bridge._
- **Why does `ReportsViewModel` connect `ReportsViewModel` to `ReportsViewModel.kt`, `SelectInfoScreen.kt`, `ReportModels.kt`, `CargoEditSearchDialogsSection.kt`, `ManageReportsScreen.kt`, `QuotaManagementContent.kt`, `WarehouseDetailsScreen.kt`, `AppModule.kt`, `QuotaAnalysisSection.kt`, `QuotaDetailsScreen.kt`, `QuotaWarningDialog.kt`, `ColorSelector`, `MainScreen.kt`, `ShipSortingMode`, `Quota`, `formatNumber`, `Ship`, `LoadingState`, `ReportsNavigation.kt`, `ComprehensiveAnalyticsDialog.kt`, `ShipDetailsScreen.kt`?**
  _High betweenness centrality (0.041) - this node is a cross-community bridge._
- **Why does `UserPreferencesManager` connect `UserPreferencesManager` to `StartupViewModel`, `AuthModels.kt`, `CargoCounterScreen.kt`, `AppModule.kt`, `MainScreen.kt`, `UserManagementScreen.kt`, `SelectInfoScreen.kt`, `CargoViewModel.kt`, `CargoDetailsScreen.kt`, `ChatScreen.kt`, `ManageReportsScreen.kt`, `HomeScreen.kt`, `MainActivity.kt`, `LogoutRequest`, `ChatNotificationWorker.kt`, `TokenStore`?**
  _High betweenness centrality (0.040) - this node is a cross-community bridge._
- **What connects `CONFIG`, `Utils`, `name` to the rest of the system?**
  _304 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `StartupViewModel` be split into smaller, more focused modules?**
  _Cohesion score 0.13105413105413105 - nodes in this community are weakly interconnected._
- **Should `ReportsRepository` be split into smaller, more focused modules?**
  _Cohesion score 0.10338680926916222 - nodes in this community are weakly interconnected._
- **Should `Security Audit` be split into smaller, more focused modules?**
  _Cohesion score 0.08695652173913043 - nodes in this community are weakly interconnected._