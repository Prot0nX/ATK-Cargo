# Graph Report - ATK-Cargo  (2026-08-20)

## Corpus Check
- 334 files · ~432,287 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2709 nodes · 4903 edges · 311 communities (153 shown, 158 thin omitted)
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 204 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `53f1a507`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- StartupViewModel
- Exception
- Security Audit
- formatNumber
- RegisterCargoDialogs.kt
- QuotaWarningDialog.kt
- ChatDao
- **5.2 / 10**
- Exception
- QuotaService
- MessageType
- Color
- ReportsViewModel
- JalaliDateUtilsTest
- CargoModels.kt
- Database
- Executive Summary
- DEEP_CODE_REVIEW.md
- PermissionService
- FontWeight
- SessionService
- Color
- ManageReportsScreen.kt
- QuotaManagementContent.kt
- Result
- SessionRepository
- CargoDetailsScreen.kt
- CargoEditSearchDialogsSection.kt
- Medium Issues
- Color
- SessionServiceTest
- HomeScreen.kt
- SecurityAlerter
- ApiServiceV2.kt
- CameraSection.kt
- androidx
- UsersManager
- Flow
- CargoRepository
- QuotaAnalysisSection.kt
- Logger
- API Audit
- CargoViewModel
- ATKCargoTheme
- ApiServiceV2
- ImageVector
- QuotaPercentageDialogSection.kt
- Color
- Executive Summary
- AppModule.kt
- com
- LoginAttemptLimiterTest
- ReportModels.kt
- Performance Audit
- Color.kt
- Kotlin Audit
- UserService
- Context
- CrashReporter.kt
- MicroCache
- RetrofitClient
- RegisterCargoScreen.kt
- RealTimeLoadingBottomSheet.kt
- StartupState
- androidx
- CargoViewModelTest
- LoadingNotificationService
- Color
- Modifier
- Android Audit
- Code Quality
- Database Audit
- quota_details/QuotaCardComponents.kt
- CargoInfoDetailsDialogSection.kt
- Final Recommendations
- Jetpack Compose Audit
- ApiV2Routes
- PHP Backend Audit
- ChatMessage.kt
- CargoViewModel.kt
- Logging & Observability
- SecurityScreen.kt
- ReportsCommonWidgets.kt
- InitialInfoScreen
- UtilityController
- Code Smells شناسایی‌شده
- Authentication & Authorization
- Color
- ImageVector
- WarehouseQuotaGroupingMode
- Color
- Dependency Audit
- Animation Audit
- Migrations
- ProfileSettingsDialogSection.kt
- com
- پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13)
- UserRepository
- ShipSortingMode
- Config
- CoroutineScope
- High Issues
- Memory Audit
- Prioritized Action Plan
- SnackbarHostState
- com
- StateFlow
- jdate
- ViewModel
- Android Audit
- preprocessImage
- PasswordGateService
- Color
- Modifier
- API Audit
- Testing Audit
- Jetpack Compose Audit
- MainActivity.kt
- JalaliDateUtils
- چک‌لیست فعال‌سازی `STRICT_TRANS_TABLES` (DEEP_CODE_AUDIT.md #Phase4.10)
- Composable
- What You Must Do When Invoked
- Performance Audit
- Prioritized Action Plan
- ComponentDefaults.kt
- CargoDetailsDialogSection.kt
- androidx
- Architecture Overview
- Error Handling
- حذف/محدودسازی phpMyAdmin روی سرور production (Phase 1, آیتم #۲)
- AnalyticsController
- ColorSelector
- Context
- QuotasListScreen.kt
- Animation Audit
- Authentication & Authorization
- Database Audit
- Color
- Error Handling
- Kotlin Audit
- UserPreferencesManager
- SecurityVerifier
- ShipService
- Application
- PHP Backend Audit
- چرخش رازهای `secrets.cpp` (Phase 1, آیتم #۱)
- Constants.kt
- StateFlow
- T
- AppApiController
- ViewModel
- ViewModelProvider
- Modifier
- com
- Flow
- Result
- KeyboardOptions
- Modifier
- androidx
- InputValidator
- Color
- Flow
- Composable
- FocusRequester
- KeyboardActions
- KeyboardType
- StateFlow
- ViewModel
- NavController
- PHP/composer.json
- com
- Color
- ImageVector
- Modifier
- androidx
- Color
- com
- ShipInfoSection.kt
- Job
- Secrets
- QuotaGroupContent.kt
- QuotaCalculatorTest
- NavController
- StateFlow
- KeyboardType
- Modifier
- UpdateManager
- LoadingState
- ComprehensiveAnalyticsDialog.kt
- Result
- AndroidViewModel
- Color
- Job
- StateFlow
- ChatNotificationWorker.kt
- .showErrorMessage
- DatabaseManager
- LoginAttemptLimiter
- ReportsViewModel.kt
- AuthenticatesRequests.php
- PermissionServiceTest
- ImageVector
- Modifier
- Color
- ImageVector
- Modifier
- Color
- ImageVector
- Modifier
- ImageVector
- secrets.cpp
- AppNotificationManager
- ImageVector
- SessionManager
- Context
- SessionManager
- ImageVector
- Modifier
- Modifier
- Color
- ImageVector
- Modifier
- Modifier
- ImageVector
- Modifier
- Color
- Context
- ImageVector
- Color
- Color
- Composable
- ImageVector
- KeyboardOptions
- Modifier
- Modifier
- Color
- ImageVector
- Modifier
- ImageVector
- Modifier
- StateFlow
- ImageVector
- Modifier
- Modifier
- Modifier
- Modifier
- AdvancedSearchDialog.kt
- Router
- QuotaRepository
- SearchDialogs.kt
- .prepareCargoInfoForSubmission
- ApiException
- BootReceiver.kt
- CargoDetailsNavigation.kt
- GroupSortingMode
- QuotaGroupingMode
- Project Structure
- StateFlow
- StateFlow
- T
- ViewModel
- Request
- ViewModelProvider
- ChatController
- DatabaseSchemaExporter
- graphify reference: extra exports and benchmark
- NavRoutes
- NotificationActionReceiver.kt
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
1. `ReportsViewModel` - 62 edges
2. `InputValidator` - 58 edges
3. `CargoViewModel` - 55 edges
4. `ApiServiceV2` - 50 edges
5. `UserPreferencesManager` - 42 edges
6. `UpdateManager` - 35 edges
7. `SessionRepository` - 35 edges
8. `Logger` - 34 edges
9. `formatNumber()` - 34 edges
10. `ActiveShipInfo` - 32 edges

## Surprising Connections (you probably didn't know these)
- `CargoCounterScreen()` --calls--> `ColorSelector`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_counter/presentation/CargoCounterScreen.kt → core/designsystem/src/main/java/com/atk/atk_cargo/data/model/ColorSelector.kt
- `QuotasDialog()` --calls--> `ColorSelector`  [INFERRED]
  feature/reports/src/main/java/com/atk/atk_cargo/feature/reports/presentation/quota_details/QuotaDetailsScreen.kt → core/designsystem/src/main/java/com/atk/atk_cargo/data/model/ColorSelector.kt
- `InitialInfoScreen()` --calls--> `InitialInfo`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/InitialInfoScreen.kt → core/network/src/main/java/com/atk/atk_cargo/data/model/CargoModels.kt
- `navigateToRegisterCargoActivity()` --calls--> `InitialInfo`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/SelectInfoScreen.kt → core/network/src/main/java/com/atk/atk_cargo/data/model/CargoModels.kt
- `QuotaPercentageDialog()` --calls--> `QuotaPercentageData`  [INFERRED]
  feature/reports/src/main/java/com/atk/atk_cargo/feature/reports/presentation/quota_details/QuotaPercentageDialogSection.kt → core/network/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt

## Import Cycles
- None detected.

## Communities (311 total, 158 thin omitted)

### Community 0 - "StartupViewModel"
Cohesion: 0.13
Nodes (9): AndroidViewModel, Flow, Intent, StateFlow, RequestBatteryOptimization, SecurityCheckState, ShowMessage, StartupEvent (+1 more)

### Community 2 - "Security Audit"
Cohesion: 0.09
Nodes (23): Critical Issues, [CRITICAL] فضای رمز عبور ۴ رقمی عددی + هش بدون salt + قفل قابل دور زدن = تصاحب کامل حساب, [HIGH] `app_api.php` تمام داده‌ی گزارش‌گیری را بدون بررسی مجوز `view_reports` می‌دهد, High Issues, [HIGH] `PermissionManager.php`: نبود `session_regenerate_id` و نبود قفل brute-force, [HIGH] `PHP/vendor/` (شامل phpunit و phpstan) در web root و کامیت‌شده در گیت, [HIGH] `protected_proxy.php` یک مرز امنیتی نیست — همه‌ی endpointها مستقیماً هم قابل فراخوانی‌اند, [HIGH] session token و refresh token به‌صورت plaintext در دیتابیس ذخیره می‌شوند (+15 more)

### Community 3 - "formatNumber"
Cohesion: 0.06
Nodes (81): formatNumber(), GroupedShipList(), Color, QuotaCard(), ShipGroup(), ShipHeader(), WarehouseCard(), WarehouseList() (+73 more)

### Community 4 - "RegisterCargoDialogs.kt"
Cohesion: 0.30
Nodes (19): DeleteDialog(), DialogBadge(), DialogButtonRow(), DialogContentCard(), DialogMessageText(), DialogTitle(), Color, ImageVector (+11 more)

### Community 5 - "QuotaWarningDialog.kt"
Cohesion: 0.05
Nodes (47): ErrorState(), ImageVector, Modifier, buildQuotasShareText(), calculatePercentage(), calculateProgress(), format(), formatNumber() (+39 more)

### Community 6 - "ChatDao"
Cohesion: 0.09
Nodes (11): For /graphify explain, For /graphify path, graphify reference: query, path, explain, Step 0 — Constrained query expansion (REQUIRED before traversal), Step 1 — Traversal, AppDatabase, Context, ChatDao (+3 more)

### Community 7 - "**5.2 / 10**"
Cohesion: 0.14
Nodes (14): **5.2 / 10**, [CRITICAL] نبود پوشش تست روی مسیرهای بحرانی, Final Score, Overall Score, Production Readiness, Project Overview, Recommended Architecture, Recommended Project Structure (+6 more)

### Community 8 - "Exception"
Cohesion: 0.09
Nodes (15): ComprehensiveAnalysisResponse, Exception, ErrorResponse, HttpStatusException, CargoInfo, Quota, ReportsRepository, ConflictException (+7 more)

### Community 9 - "QuotaService"
Cohesion: 0.11
Nodes (4): AppApiCacheKeys, AuditLogger, QuotaService, QuotaCalculator

### Community 10 - "MessageType"
Cohesion: 0.17
Nodes (18): Modifier, StatusSnackbar(), CargoSnackbarMessage, Color, Modifier, snackbarAccent(), snackbarAccentBg(), StatusSnackbar() (+10 more)

### Community 12 - "ReportsViewModel"
Cohesion: 0.07
Nodes (9): ComprehensiveAnalytics, QuotaCompletionData, RealTimeUiState, ReportsUiState, ReportsViewModel, GroupSortingMode, QuotaPercentageData, QuotaSortingMode (+1 more)

### Community 14 - "CargoModels.kt"
Cohesion: 0.08
Nodes (16): CargoDeleteResponse, CargoInfo, CargoInfoResponse, CargoInfoSearch, CargoSearchResponse, CargoStats, CheckExistenceResponse, ExistingCargo (+8 more)

### Community 15 - "Database"
Cohesion: 0.11
Nodes (10): App\Core\AuthenticatesRequests, InvalidArgumentException, mysqli, PDO, PDOException, Database, PDO, self (+2 more)

### Community 16 - "Executive Summary"
Cohesion: 0.10
Nodes (19): Deep Code Audit Report, Executive Summary, Final Recommendations, **Overall: 5.2 / 10**, Overall Score, Production Readiness, Technical Debt, Top 20 Priority List (+11 more)

### Community 17 - "DEEP_CODE_REVIEW.md"
Cohesion: 0.10
Nodes (19): Dependency Audit, Final Score, Logging & Observability, [LOW] Koin 3.5.6, [LOW] `lateinit var currentDownloadFile` بدون گارد کامل, [MEDIUM] Compose BOM عملاً بی‌اثر است, [MEDIUM] آدرس پایه به مسیر `test_api/` اشاره می‌کند, [MEDIUM] نبود مانیتورینگ خودکار (+11 more)

### Community 19 - "FontWeight"
Cohesion: 0.14
Nodes (12): ConfirmationDialog(), ImageVector, EmptyState(), ImageVector, Modifier, AnimatedCounter(), ExitStatusDialog(), ImageVector (+4 more)

### Community 22 - "ManageReportsScreen.kt"
Cohesion: 0.08
Nodes (32): Job, StateFlow, PermissionPoller, MainScreen(), RouteTransitions, standardTransitions(), DateClickableField(), DateRangePicker() (+24 more)

### Community 23 - "QuotaManagementContent.kt"
Cohesion: 0.20
Nodes (20): AdvancedFiltersDialog(), EmptyQuotaState(), FilterOptionsCard(), Modifier, QuotaItem, ReportsViewModel, QuotaFilters, QuotaManagementContent() (+12 more)

### Community 26 - "CargoDetailsScreen.kt"
Cohesion: 0.16
Nodes (24): CargoDetailsScreen(), confirmCargo(), FloatingActionButtonItem(), handleCargoConfirmation(), Color, ImageVector, NavController, Result (+16 more)

### Community 27 - "CargoEditSearchDialogsSection.kt"
Cohesion: 0.30
Nodes (18): CargoChangesPreview(), CargoEditConfirmDialog(), CargoEditConfirmHeader(), CargoMainInfo(), CargoShippingInfo(), CargoTimeInfo(), CargoWeightInfo(), ChangeItem() (+10 more)

### Community 28 - "Medium Issues"
Cohesion: 0.12
Nodes (16): Critical Issues, [CRITICAL] کلیدهای API و لایسنس با XOR تک‌بایتی محافظت شده و در git ذخیره‌اند, Low Issues, [LOW] استفاده از `!!` روی state در Composable, [LOW] تابع native تعریف‌شده اما هرگز استفاده نشده, [LOW] فایل اطلاعات سرور در `.gitignore` نیست, [MEDIUM] endpointهای لایسنس بدون احراز هویت و بدون rate limit, Medium Issues (+8 more)

### Community 31 - "HomeScreen.kt"
Cohesion: 0.08
Nodes (34): CargoCounterRoute, cargoCounterScreen(), NavController, navigateToCargoCounter(), CargoCounterOperationScreen(), NavController, getMenuItemsForUserType(), HomeRoute (+26 more)

### Community 33 - "ApiServiceV2.kt"
Cohesion: 0.12
Nodes (18): ApiResponse2, Result, validateServerSession(), ActiveSessionResponse, CreateUserRequest, DeleteUserRequest, ForceLogoutRequest, ForceLogoutResponse (+10 more)

### Community 34 - "CameraSection.kt"
Cohesion: 0.21
Nodes (12): EnhancedNumberAnalyzer, extractNumber(), ImageProxy, InputImage, recognizeTextFromImage(), EnhancedCameraPreview(), ImageCapture, Color (+4 more)

### Community 36 - "UsersManager"
Cohesion: 0.07
Nodes (7): ApiManager, CONFIG, ModalManager, ThemeManager, UIManager, UsersManager, Utils

### Community 38 - "CargoRepository"
Cohesion: 0.08
Nodes (4): App\Enums\CargoConfirmStatus, App\Enums\CargoStatus, CargoRepository, CargoService

### Community 39 - "QuotaAnalysisSection.kt"
Cohesion: 0.20
Nodes (20): buildQuotaGroups(), QuotaGroup, AnalyticsGroupingModeButton(), AnalyticsOwnerSummaryCard(), AnalyticsQuotaCard(), AnalyticsQuotaGroupExpansionPanel(), AnalyticsStatChip(), Context (+12 more)

### Community 40 - "Logger"
Cohesion: 0.18
Nodes (3): LicenseController, Logger, self

### Community 41 - "API Audit"
Cohesion: 0.17
Nodes (12): API Audit, [HIGH] دو API stack موازی — نسخه‌ی امن‌تر بلااستفاده است, [MEDIUM] طراحی API غیر-RESTful و ناسازگار, فاز ۳.۲ — ✅ انجام شد: حذف ۲۰ فایل ورودی legacy نسخه ۱, فاز ۳.۳ — ✅ کامل شد: تست‌های Phase 1/2, فاز ۳.۴ — ✅ انجام شد: وابستگی‌های تست, فاز ۳.۵ — ✅ کامل: `UiState` واحد در `CargoViewModel` و `ReportsViewModel`, فهرست endpointها (+4 more)

### Community 42 - "CargoViewModel"
Cohesion: 0.12
Nodes (5): Cargo, CargoInfoRequest, CargoViewModel, message, MessageType

### Community 43 - "ATKCargoTheme"
Cohesion: 0.09
Nodes (22): ColorScheme, AdaptiveLayoutConfig, rememberAdaptiveLayoutConfig(), WindowSizeClass, COMPACT, EXPANDED, MEDIUM, buildAppDarkColorScheme() (+14 more)

### Community 44 - "ApiServiceV2"
Cohesion: 0.14
Nodes (5): ApiServiceV2, User, SuccessResponse, Quota, JsonElement

### Community 46 - "QuotaPercentageDialogSection.kt"
Cohesion: 0.17
Nodes (21): createTypography(), QuotaPercentageData, AnimatedNumber(), DialogHeader(), CalculationResult, Color, ImageVector, Modifier (+13 more)

### Community 48 - "Executive Summary"
Cohesion: 0.13
Nodes (15): Architecture Overview, Deep Code Audit Report, Executive Summary, Overall Score, Overall Score: **6.1 / 10**, Production Readiness, Project Overview, Technical Debt (+7 more)

### Community 49 - "AppModule.kt"
Cohesion: 0.16
Nodes (15): ActiveShipInfo, CargoCounterScreen(), CargoCounterViewModel, filterShipsByTab(), NavController, navigateToCargoDetailsScreen(), ShipFilterTab, ALL (+7 more)

### Community 52 - "ReportModels.kt"
Cohesion: 0.06
Nodes (24): AnalyticsData, CalculationResult, ComprehensiveAnalysisResponse, ComprehensiveAnalytics, DateInfo, ExistingQuota, ExitDateInfo, FilteredSummary (+16 more)

### Community 53 - "Performance Audit"
Cohesion: 0.20
Nodes (10): [HIGH] `permissions.json` در هر بررسی مجوز از دیسک خوانده و parse می‌شود, [LOW] subquery همبسته در فهرست پیام‌های چت, [MEDIUM] `updateUser` کل جدول کاربران را برای یافتن رکورد خود کاربر می‌خواند, [MEDIUM] `user_sessions` با ۱۳ ایندکس که در هر درخواست به‌روزرسانی می‌شود, [MEDIUM] درخواست‌های شبکه‌ی زائد بعد از هر ثبت حواله, [MEDIUM] نبود صفحه‌بندی در endpointهای فهرستی, Performance Audit, خلاصه‌ی اثر (+2 more)

### Community 54 - "Color.kt"
Cohesion: 0.07
Nodes (48): CompactStatChipTest, ActionButtonTest, persianDateFormat(), calculateWarningStatus(), ErrorStateCard(), ImageVector, Quota, ReportsViewModel (+40 more)

### Community 55 - "Kotlin Audit"
Cohesion: 0.22
Nodes (9): [HIGH] باگ منطقی: مسیر پاک‌سازی نشست منقضی در `PermissionPoller` هرگز اجرا نمی‌شود, Kotlin Audit, [LOW] `Strictness.LENIENT` در Gson, [MEDIUM] `runBlocking` داخل `Authenticator` روی thread شبکه‌ی OkHttp, [MEDIUM] بسته‌بندی مکرر استثنا که نوع و stack trace را نابود می‌کند, [MEDIUM] بلعیدن خطا و بازگرداندن نتیجه‌ی خالی در جستجوها, [MEDIUM] پیاده‌سازی دوگانه‌ی تبدیل تاریخ جلالی, نکات مثبت (+1 more)

### Community 56 - "UserService"
Cohesion: 0.10
Nodes (11): App\Core\DatabaseManager, App\Core\MicroCache, App\Core\Request, App\Core\Response, App\Exceptions\ApiException, App\Repositories\UserRepository, App\Services\LoginAttemptLimiter, App\Services\ShipService (+3 more)

### Community 58 - "CrashReporter.kt"
Cohesion: 0.46
Nodes (4): CrashReporter, Context, CoroutineScope, PendingCrashReport

### Community 60 - "RetrofitClient"
Cohesion: 0.07
Nodes (21): CryptoManager, Authenticator, ByteArray, Cipher, FloatTypeAdapter, Context, OkHttpClient, RetrofitClient (+13 more)

### Community 61 - "RegisterCargoScreen.kt"
Cohesion: 0.26
Nodes (10): Modifier, SearchBar(), DuplicateTrackingNumbersDialog(), NetWeightDialog(), FormSection(), ErrorHandlingCargoInfoRow(), RegisterCargoScreen(), RegisterPalette (+2 more)

### Community 62 - "RealTimeLoadingBottomSheet.kt"
Cohesion: 0.20
Nodes (16): DialogHeader(), Color, Modifier, RealTimeLoadingBottomSheet(), RealTimeShiftNavigation(), RefreshOverlay(), ShipCard(), CompactInfo() (+8 more)

### Community 63 - "StartupState"
Cohesion: 0.33
Nodes (6): Ready, SecurityBlocked, Splash, StartupState, Syncing, VersionExpired

### Community 66 - "LoadingNotificationService"
Cohesion: 0.22
Nodes (6): Context, Intent, KoinComponent, LoadingNotificationService, IBinder, Service

### Community 69 - "Android Audit"
Cohesion: 0.29
Nodes (7): Android Audit, [LOW] `versionCode = 11` در برابر `versionName = "4.0.1"`, [MEDIUM] `targetSdk = 34` در حالی که `compileSdk = 36` است, [MEDIUM] `viewBinding = true` در یک اپ کاملاً Compose, [MEDIUM] وابستگی‌های بلااستفاده در گراف build, نکات مثبت (تأییدشده), یافته‌ها

### Community 70 - "Code Quality"
Cohesion: 0.33
Nodes (6): Code Quality, Code Smells شناسایی‌شده, [HIGH] Primitive Obsession روی مقادیر وزن و تناژ, [MEDIUM] DTO و مدل دامنه تفکیک نشده‌اند, [MEDIUM] Magic Numbers بدون نام, مهم‌ترین‌ها با جزئیات

### Community 71 - "Database Audit"
Cohesion: 0.29
Nodes (7): Database Audit, [LOW] `SET SESSION sql_mode = 'NO_ENGINE_SUBSTITUTION'` حالت strict را خاموش می‌کند, [MEDIUM] `schema.sql` قدیمی و ناقص است, [MEDIUM] هیچ FOREIGN KEY در کل schema وجود ندارد, ساختار (از `schema.sql` — ✅ به‌روزرسانی شد در Phase 2.3، اکنون کامل), نکات مثبت, یافته‌ها

### Community 72 - "quota_details/QuotaCardComponents.kt"
Cohesion: 0.27
Nodes (16): ActionButton(), calculateValues(), ConfirmationDialogHeader(), EditFieldBox(), EditFieldColumn(), EditQuotaDialog(), EditQuotaDialogHeader(), CalculationResult (+8 more)

### Community 73 - "CargoInfoDetailsDialogSection.kt"
Cohesion: 0.35
Nodes (12): CargoDetailsPalette, CargoDetailTabItem(), CargoInfoDetailsDialog(), DetailInfoRow(), ImageVector, Modifier, SnackbarHostState, MainInfoTabContent() (+4 more)

### Community 74 - "Final Recommendations"
Cohesion: 0.25
Nodes (8): Final Recommendations, Roadmap عملی, ارزیابی صادقانه, سه توصیه‌ی کلیدی, معیار موفقیت, ۱. رازها را همین امروز بچرخانید — سپس مدل را عوض کنید, ۲. مرز لایه‌ها را ببندید — این باگ است، نه سلیقه, ۳. تست‌هایی که دارید را اجرا کنید

### Community 75 - "Jetpack Compose Audit"
Cohesion: 0.29
Nodes (7): [HIGH] God Composable — فایل‌های نمایشی با بیش از ۱۰۰۰ خط, [HIGH] صفر مورد `collectAsStateWithLifecycle` در برابر ۸۵ مورد `collectAsState`, Jetpack Compose Audit, [LOW] تمام رشته‌های UI به‌صورت literal در کد, [MEDIUM] استفاده‌ی کم از `derivedStateOf` در جاهایی که واقعاً لازم است, [MEDIUM] نبود `UiState` واحد — ۲۰+ `StateFlow` مجزا در هر ViewModel, [MEDIUM] ۲۲ مورد از ۳۵ فراخوانی `items(...)` بدون `key`

### Community 77 - "PHP Backend Audit"
Cohesion: 0.29
Nodes (7): [HIGH] `migrations/` خالی است و `schema.sql` ناقص — جدول `audit_log` هرگز ساخته نمی‌شود, [LOW] `Config::$settings['session_timeout']` و `admin_password_hash` بلااستفاده‌اند, [MEDIUM] `execute()` در پروکسی فقط `Exception` را می‌گیرد، نه `Throwable`, [MEDIUM] `flushLog` پروکسی وقتی هدف `exit` می‌کند اجرا نمی‌شود, PHP Backend Audit, نکات مثبت, یافته‌ها

### Community 78 - "ChatMessage.kt"
Cohesion: 0.29
Nodes (5): ChatMessage, ChatMessagesResponse, SendMessageRequest, SendMessageResponse, UnreadCountResponse

### Community 79 - "CargoViewModel.kt"
Cohesion: 0.14
Nodes (12): CargoDialog, CargoUiState, CargoViewModelFactory, DuplicateConfirmation, Duplicates, NetWeight, None, MatchingQuota (+4 more)

### Community 80 - "Logging & Observability"
Cohesion: 0.29
Nodes (7): [HIGH] پوشه‌های لاگ ممکن است از طریق وب قابل دسترس باشند, Logging & Observability, [LOW] ~۹۰ فراخوانی `Log.*` در کد تولید, [MEDIUM] نبود مانیتورینگ و هشدار, آنچه لاگ می‌شود, نکات مثبت, یافته‌ها

### Community 81 - "SecurityScreen.kt"
Cohesion: 0.25
Nodes (13): AdvancedOrbitalScanner(), AnimatedUpdateIcon(), AnimatedWarningIcon(), DynamicPremiumBackground(), InteractiveExitButton(), Modifier, RetryButton(), SecurityBlockScreen() (+5 more)

### Community 82 - "ReportsCommonWidgets.kt"
Cohesion: 0.11
Nodes (30): ApiQuotaDetails, adjustColorForTheme(), Color, Color, ImageVector, Modifier, StatisticsCard(), CompactStatChip() (+22 more)

### Community 83 - "InitialInfoScreen"
Cohesion: 0.11
Nodes (32): formatNumber(), isValidPersianText(), isValidQuotaNumber(), isValidShipName(), isValidWarehouseName(), isValidWeight(), InitialInfoRoute, initialInfoScreen() (+24 more)

### Community 85 - "Code Smells شناسایی‌شده"
Cohesion: 0.29
Nodes (7): Code Quality, Code Smells شناسایی‌شده, God Files (۳۱ فایل > ۶۰۰ خط), [LOW] دایرکتوری‌های خالی باقی‌مانده از ماژول‌بندی, [LOW] نام کاملاً واجد شرایط (FQN) درون بدنه‌ی کلاس, [MEDIUM] `CargoUiState` با ۱۶ فیلد, نکته‌ی مثبت درباره‌ی کیفیت: کامنت‌گذاری

### Community 86 - "Authentication & Authorization"
Cohesion: 0.40
Nodes (5): Authentication & Authorization, [MEDIUM] `check_logout.php` بدون احراز هویت, ارزیابی, جریان کامل احراز هویت, یافته‌ی اضافی

### Community 89 - "WarehouseQuotaGroupingMode"
Cohesion: 0.50
Nodes (4): WarehouseQuotaGroupingMode, BY_CARGO_OWNER, BY_SHIPPING_COMPANY, BY_WAREHOUSE

### Community 91 - "Dependency Audit"
Cohesion: 0.33
Nodes (6): Android, Dependency Audit, [LOW] `material3 = "1.3.2"` صریح، نسخه‌ی BOM را override می‌کند, [MEDIUM] `composer.json` قید `php: >=7.4` دارد ولی کد PHP 8 لازم دارد, [MEDIUM] `itextpdf 5.5.13.4` — مجوز AGPL و پایان پشتیبانی, PHP

### Community 92 - "Animation Audit"
Cohesion: 0.33
Nodes (6): Animation Audit, [INFO] نقاط قوت انیمیشن, [LOW] انیمیشن شمارنده‌ی معکوس ثانیه‌ای که هر ثانیه recomposition ایجاد می‌کند, [MEDIUM] نبود پشتیبانی از Reduce Motion / دسترس‌پذیری, وضعیت کلی, یافته‌ها

### Community 93 - "Migrations"
Cohesion: 0.33
Nodes (5): Migrations, migrationهای اعمال‌شده تاکنون, قرارداد نام‌گذاری, نحوه‌ی اجرا, وضعیت schema.sql

### Community 94 - "ProfileSettingsDialogSection.kt"
Cohesion: 0.22
Nodes (5): ProfileSettingsDialog(), ApiResponse, DeleteMessageRequest, EditMessageRequest, UpdateUserRequest

### Community 96 - "پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13)"
Cohesion: 0.22
Nodes (8): ساختار فعلی (مستند شده در این جلسه), ساختار هدف, مراحل اجرا (ترتیب دقیق مهم است), معیار موفقیت, پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13), پیش‌نیازهای جانبی که باید قبل از این کار حل شوند, چرا فقط .htaccess کافی نیست, چرا لازم است

### Community 98 - "ShipSortingMode"
Cohesion: 0.29
Nodes (7): ShipSortingMode, LOADED_TONNAGE_ASC, LOADED_TONNAGE_DESC, NAME_ASC, NAME_DESC, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC

### Community 101 - "High Issues"
Cohesion: 0.33
Nodes (6): [HIGH] build release هیچ signingConfig ندارد, [HIGH] Composableها مستقیماً شبکه را صدا می‌زنند و عملیات نوشتن با ناوبری cancel می‌شود, High Issues, [HIGH] phpMyAdmin روی سرور production نصب است, [HIGH] سرور production روی نسخه‌ی PHP بدون پشتیبانی امنیتی اجرا می‌شود, [HIGH] مسیر fallback احراز هویت، رمز عبور متن‌خام را می‌پذیرد

### Community 102 - "Memory Audit"
Cohesion: 0.40
Nodes (5): [LOW] `ChatNotificationWorker` و چرخه‌ی حیات دیتابیس, [MEDIUM] فهرست‌های بدون سقف در `StateFlow`, Memory Audit, وضعیت کلی: **۷/۱۰ — بهترین حوزه‌ی پروژه**, یافته‌ها

### Community 103 - "Prioritized Action Plan"
Cohesion: 0.33
Nodes (6): Phase 1 — Immediate (این هفته — بازدارنده‌های تولید), Phase 2 — High Priority (۲ تا ۴ هفته), Phase 3 — Medium Priority (۱ تا ۳ ماه), Phase 4 — Optimization (۳ تا ۶ ماه), Phase 5 — تکمیل موارد باقی‌مانده (اجرا شده پس از بازبینی نهایی گزارش), Prioritized Action Plan

### Community 107 - "jdate"
Cohesion: 0.53
Nodes (9): gregorian_to_jalali(), jalali_to_gregorian(), jcheckdate(), jdate(), jdate_words(), jgetdate(), jmktime(), jstrftime() (+1 more)

### Community 109 - "Android Audit"
Cohesion: 0.40
Nodes (5): Android Audit, [LOW] پرچم منسوخ در `gradle.properties`, [MEDIUM] `targetSdk = 34` در حالی که `compileSdk = 36`, مشکلات, نقاط قوت

### Community 110 - "preprocessImage"
Cohesion: 0.50
Nodes (8): adaptiveThresholding(), applyContrastEnhancement(), enhanceEdges(), ImageProxy, InputImage, medianFilter(), preprocessImage(), IntArray

### Community 115 - "API Audit"
Cohesion: 0.40
Nodes (5): API Audit, [LOW] نبود Rate Limiting سراسری روی Router v2, [MEDIUM] دو شکل ناسازگار برای پاسخ خطا, [MEDIUM] عدم رعایت معنای متدهای HTTP, استخراج Endpointها

### Community 116 - "Testing Audit"
Cohesion: 0.40
Nodes (5): [HIGH] CI هیچ تست اندرویدی اجرا نمی‌کند, Test Caseهای پیشنهادی (اولویت‌دار), Testing Audit, بخش‌های بحرانی بدون تست, وضعیت موجود

### Community 117 - "Jetpack Compose Audit"
Cohesion: 0.40
Nodes (5): [HIGH] انیمیشن‌ها باعث recomposition در هر فریم می‌شوند, Jetpack Compose Audit, [MEDIUM] Composableهای خدای‌گونه, مشکلات, نقاط قوت (واقعاً چشمگیر)

### Community 118 - "MainActivity.kt"
Cohesion: 0.32
Nodes (5): Intent, MainActivity, StartupErrorScreen(), Bundle, ComponentActivity

### Community 120 - "چک‌لیست فعال‌سازی `STRICT_TRANS_TABLES` (DEEP_CODE_AUDIT.md #Phase4.10)"
Cohesion: 0.33
Nodes (5): مراحل امن (ترتیب مهم است), مسئله چیست, معیار موفقیت, چرا نمی‌شود همین الان فعالش کرد, چک‌لیست فعال‌سازی `STRICT_TRANS_TABLES` (DEEP_CODE_AUDIT.md #Phase4.10)

### Community 121 - "Composable"
Cohesion: 0.26
Nodes (10): androidx, Composable, ReportsViewModel, QuotaManagementDialog(), QuotaManagementHeaderCard(), DeleteQuotaDialog(), Color, QuotaConfirmDialog() (+2 more)

### Community 122 - "What You Must Do When Invoked"
Cohesion: 0.07
Nodes (26): For /graphify add and --watch, For /graphify query, For the commit hook and native CLAUDE.md integration, For --update and --cluster-only, /graphify, Honesty Rules, Interpreter guard for subcommands, Part A - Structural extraction for code files (+18 more)

### Community 123 - "Performance Audit"
Cohesion: 0.40
Nodes (5): [HIGH] ایندکس گمشده روی `trackingNumber`, [MEDIUM] تاریخ و زمان به‌صورت `varchar(100)` ذخیره می‌شوند, [MEDIUM] حلقه‌ی شمارش معکوس با `delay(200)`, Performance Audit, نقاط قوت Performance

### Community 124 - "Prioritized Action Plan"
Cohesion: 0.40
Nodes (5): Phase 1 — Immediate (هفته‌ی ۱), Phase 2 — High Priority (هفته‌های ۲–۴), Phase 3 — Medium Priority (ماه‌های ۲–۳), Phase 4 — Optimization (ماه‌های ۴+), Prioritized Action Plan

### Community 128 - "CargoDetailsDialogSection.kt"
Cohesion: 0.31
Nodes (13): CargoDetailsDialog(), Color, ImageVector, Modifier, ModernCargoDetailsGrid(), ModernCargoStatusSection(), ModernCargoStatusTimeline(), ModernDialogActions() (+5 more)

### Community 130 - "Architecture Overview"
Cohesion: 0.50
Nodes (4): Architecture Overview, جریان طراحی‌شده ولی بلااستفاده, جریان واقعی داده (آنچه امروز اجرا می‌شود), نقاط مرزی پرریسک بین لایه‌ها

### Community 131 - "Error Handling"
Cohesion: 0.50
Nodes (4): Error Handling, [MEDIUM] نبود مکانیزم مرکزی گزارش خطا (Crash Reporting), نکات مثبت, یافته‌های تجمیعی

### Community 132 - "حذف/محدودسازی phpMyAdmin روی سرور production (Phase 1, آیتم #۲)"
Cohesion: 0.40
Nodes (4): حذف/محدودسازی phpMyAdmin روی سرور production (Phase 1, آیتم #۲), راستی‌آزمایی نهایی, گزینه‌ی ۱ — حذف کامل (توصیه‌شده), گزینه‌ی ۲ — اگر حذف فوری ممکن نیست

### Community 134 - "ColorSelector"
Cohesion: 0.39
Nodes (3): adjustColorForTheme(), ColorSelector, Color

### Community 136 - "QuotasListScreen.kt"
Cohesion: 0.42
Nodes (8): ImageVector, Modifier, Quota, ReportsViewModel, WarehouseQuotaGroupingMode, QuotasList(), SortPill(), StateFlow

### Community 137 - "Animation Audit"
Cohesion: 0.50
Nodes (4): Animation Audit, [LOW] عدم پشتیبانی از Reduce Motion, [MEDIUM] انیمیشن‌های بی‌نهایت روی صفحات پرکاربرد, آمار

### Community 138 - "Authentication & Authorization"
Cohesion: 0.50
Nodes (4): Authentication & Authorization, [MEDIUM] نشست‌های پنل وب هرگز نمی‌توانند از گیت API عبور کنند, جریان کامل, نقاط قوت (تأییدشده)

### Community 139 - "Database Audit"
Cohesion: 0.50
Nodes (4): Database Audit, [LOW] نبود جدول ردیابی migration, مشکلات, نقاط قوت

### Community 141 - "Error Handling"
Cohesion: 0.50
Nodes (4): Error Handling, [MEDIUM] مدیریت خطا در UI به‌صورت پراکنده و تکراری, مشکلات, نقاط قوت

### Community 142 - "Kotlin Audit"
Cohesion: 0.50
Nodes (4): Kotlin Audit, [MEDIUM] UseCaseها به‌جای تزریق وابستگی، singleton را مستقیم می‌گیرند, ارزیابی: 8/10 — تمیزترین بخش پروژه, مشکلات

### Community 144 - "UserPreferencesManager"
Cohesion: 0.08
Nodes (8): TypeToken, Flow, T, UserPreferencesManager, ThemeColorOption, ThemeColorPickerRow(), DataStore, Preferences

### Community 145 - "SecurityVerifier"
Cohesion: 0.17
Nodes (10): SecurityErrorType, LICENSE_INACTIVE, LICENSE_NOT_FOUND, NETWORK_ERROR, TAMPERED, UNKNOWN_ERROR, SecurityVerifier, JSONObject (+2 more)

### Community 149 - "PHP Backend Audit"
Cohesion: 0.50
Nodes (4): [MEDIUM] معماری دوگانه: shimهای مستقیم، Router را دور می‌زنند, PHP Backend Audit, دفاع لایه‌ای (نمونه‌ی خوب), معماری

### Community 150 - "چرخش رازهای `secrets.cpp` (Phase 1, آیتم #۱)"
Cohesion: 0.50
Nodes (3): تاریخچه‌ی git, مراحل, چرخش رازهای `secrets.cpp` (Phase 1, آیتم #۱)

### Community 178 - "PHP/composer.json"
Cohesion: 0.09
Nodes (21): autoload, autoload-dev, psr-4, psr-4, config, optimize-autoloader, sort-packages, description (+13 more)

### Community 186 - "ShipInfoSection.kt"
Cohesion: 0.30
Nodes (12): formatNumber(), toEnglishNumbers(), DetailedInfoGrid(), DetailInfoItem(), ExpandedContent(), Color, ImageVector, Modifier (+4 more)

### Community 189 - "QuotaGroupContent.kt"
Cohesion: 0.43
Nodes (7): GroupingModeButton(), GroupingModeSelector(), ImageVector, Modifier, Quota, WarehouseQuotaGroupingMode, QuotaGroupExpansionPanel()

### Community 195 - "UpdateManager"
Cohesion: 0.08
Nodes (23): Context, Completed, Downloading, DownloadProgress, DownloadState, Error, Idle, Paused (+15 more)

### Community 197 - "LoadingState"
Cohesion: 0.47
Nodes (6): Error, Idle, Loading, LoadingState, Success, UiState

### Community 198 - "ComprehensiveAnalyticsDialog.kt"
Cohesion: 0.54
Nodes (7): AnalyticsDateNavigation(), AnalyticsHeaderCard(), ComprehensiveAnalyticsDialog(), EmptyStateCard(), ErrorStateCard(), Modifier, ReportsViewModel

### Community 204 - "ChatNotificationWorker.kt"
Cohesion: 0.48
Nodes (4): ChatNotificationWorker, KoinComponent, CoroutineWorker, ListenableWorker

### Community 209 - "ReportsViewModel.kt"
Cohesion: 0.18
Nodes (8): AndroidViewModel, For --cluster-only, For --update (incremental re-extraction), graphify reference: incremental update and cluster-only, CargoInfo, Color, Quota, FilteredSummary

### Community 210 - "AuthenticatesRequests.php"
Cohesion: 0.80
Nodes (4): enforceMinAppVersion(), requireAuthenticatedSession(), requirePermission(), sendAuthErrorResponse()

### Community 221 - "secrets.cpp"
Cohesion: 0.34
Nodes (16): decryptXor(), JNI_OnLoad(), n0(), n1(), n2(), n3(), n4(), n5() (+8 more)

### Community 257 - "AdvancedSearchDialog.kt"
Cohesion: 0.29
Nodes (9): AdvancedSearchDialog(), Modifier, SearchTabInfo, SearchTypeTabRow(), SearchType, RECEIPT_NUMBER, TRACKING_NUMBER, ImageVector (+1 more)

### Community 259 - "QuotaRepository"
Cohesion: 0.31
Nodes (4): CargoInfoResponse, QuotaRepository, QuotaStatusResponse, UserPreferencesStore

### Community 260 - "SearchDialogs.kt"
Cohesion: 0.47
Nodes (8): CargoSearchResultCard(), InfoRowCompact(), CargoInfo, Color, Context, ImageVector, MultipleSearchResultDialog(), shareCargoInfo()

### Community 263 - "BootReceiver.kt"
Cohesion: 0.48
Nodes (5): BootReceiver, BroadcastReceiver, Context, Intent, KoinComponent

### Community 264 - "CargoDetailsNavigation.kt"
Cohesion: 0.60
Nodes (4): CargoDetailsRoute, cargoDetailsScreen(), NavController, navigateToCargoDetails()

### Community 265 - "GroupSortingMode"
Cohesion: 0.50
Nodes (4): GroupSortingMode, ALPHABETICAL, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC

### Community 266 - "QuotaGroupingMode"
Cohesion: 0.50
Nodes (4): QuotaGroupingMode, BY_CARGO_OWNER, BY_CARRIER, BY_SHIP

### Community 267 - "Project Structure"
Cohesion: 0.50
Nodes (4): Project Structure, آنچه باید تغییر کند ❌, آنچه خوب است ✅, ساختار پیشنهادی

### Community 272 - "Request"
Cohesion: 0.08
Nodes (5): AuthController, DiagnosticsController, OnlineUsersController, MinVersionGate, Request

### Community 319 - "graphify reference: extra exports and benchmark"
Cohesion: 0.22
Nodes (8): graphify reference: extra exports and benchmark, Step 6b - Wiki (only if --wiki flag), Step 7 - Neo4j export (only if --neo4j or --neo4j-push flag), Step 7a - FalkorDB export (only if --falkordb or --falkordb-push flag), Step 7b - SVG export (only if --svg flag), Step 7c - GraphML export (only if --graphml flag), Step 7d - MCP server (only if --mcp flag), Step 8 - Token reduction benchmark (only if total_words > 5000)

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
- **402 isolated node(s):** `None`, `NetWeight`, `RequestBatteryOptimization`, `RealTimeUiState`, `ReportsUiState` (+397 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **158 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `HttpStatusException` connect `Exception` to `ReportsViewModel.kt`?**
  _High betweenness centrality (0.082) - this node is a cross-community bridge._
- **Why does `ReportsRepository` connect `Exception` to `CargoDetailsNavigation.kt`, `AppModule.kt`, `QuotaRepository`, `ReportsViewModel.kt`?**
  _High betweenness centrality (0.073) - this node is a cross-community bridge._
- **Why does `ApiException` connect `ApiException` to `CargoRepository`, `Exception`, `InputValidator`, `Database`, `Request`?**
  _High betweenness centrality (0.071) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `ReportsViewModel` (e.g. with `AppModule.kt` and `buildQuotaGroups()`) actually correct?**
  _`ReportsViewModel` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 10 inferred relationships involving `InputValidator` (e.g. with `.handle()` and `.handlePost()`) actually correct?**
  _`InputValidator` has 10 INFERRED edges - model-reasoned connections that need verification._
- **What connects `None`, `NetWeight`, `RequestBatteryOptimization` to the rest of the system?**
  _402 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `StartupViewModel` be split into smaller, more focused modules?**
  _Cohesion score 0.13105413105413105 - nodes in this community are weakly interconnected._