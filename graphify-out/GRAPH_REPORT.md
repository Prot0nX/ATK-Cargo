# Graph Report - ATK-Cargo  (2026-08-20)

## Corpus Check
- 336 files · ~432,822 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2561 nodes · 4831 edges · 239 communities (157 shown, 82 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 144 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `616e946c`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- StartupViewModel
- ReportsRepository
- Security Audit
- CargoCounterScreen.kt
- RegisterCargoDialogs.kt
- QuotaAnalysisSection.kt
- ChatDao
- **5.2 / 10**
- SecurityScreen.kt
- MicroCache
- ActiveQuotasContent.kt
- Color
- TextAlign
- JalaliDateUtilsTest
- CargoModels.kt
- Exception
- Executive Summary
- DEEP_CODE_REVIEW.md
- PermissionService
- quota_details/QuotaCardComponents.kt
- SessionService
- CargoInfo
- HomeScreen.kt
- ManageReportsScreen.kt
- Result
- SessionRepository
- MessageType
- MainActivity.kt
- Medium Issues
- QuotaManagementContent.kt
- SessionServiceTest
- CargoViewModel
- SecurityAlerter
- ApiServiceV2.kt
- VoucherDetailsDialogSection.kt
- androidx
- UsersManager
- Flow
- CargoRepository
- ReportsViewModel
- Logger
- API Audit
- DatabaseManager
- ATKCargoTheme
- ApiServiceV2
- ImageVector
- FontWeight
- Color
- Executive Summary
- CameraSection.kt
- com
- LoginAttemptLimiterTest
- ReportModels.kt
- Performance Audit
- QuotaDetailsScreen.kt
- Kotlin Audit
- App\Core\AuthenticatesRequests
- Context
- CrashReporter.kt
- CargoController
- SearchDialogs.kt
- PermissionPoller
- QuotaPercentageDialogSection.kt
- UserService
- androidx
- DateRangePicker.kt
- QuotaWarningDialog.kt
- ColorSelector
- AdvancedSearchDialog.kt
- Android Audit
- Code Quality
- Database Audit
- LoginAttemptLimiter
- ManageReportsScreen
- Final Recommendations
- Jetpack Compose Audit
- ApiV2Routes
- PHP Backend Audit
- ChatMessage.kt
- AuthController
- Logging & Observability
- PermissionServiceTest
- RetrofitClient
- InitialInfoScreen
- Router
- Code Smells شناسایی‌شده
- Authentication & Authorization
- Color
- Color.kt
- ReportsCommonWidgets.kt
- RealTimeLoadingBottomSheet
- Dependency Audit
- Animation Audit
- Migrations
- Quota
- SelectInfoScreen.kt
- پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13)
- UserRepository
- Ship
- Database
- ShipInfoSection.kt
- High Issues
- Memory Audit
- Prioritized Action Plan
- RegisterCargoScreen.kt
- com
- CargoInfoDetailsDialogSection.kt
- jdate
- ChatNotificationWorker.kt
- Android Audit
- StartupState
- PasswordGateService
- UpdateManager
- ComprehensiveAnalyticsDialog.kt
- API Audit
- Testing Audit
- Jetpack Compose Audit
- ActiveShipInfo
- QuotaSelectionDialog.kt
- چک‌لیست فعال‌سازی `STRICT_TRANS_TABLES` (DEEP_CODE_AUDIT.md #Phase4.10)
- preprocessImage
- What You Must Do When Invoked
- Performance Audit
- Prioritized Action Plan
- ComponentDefaults.kt
- QuotaEntryDialog.kt
- QuotaCard
- Architecture Overview
- Error Handling
- حذف/محدودسازی phpMyAdmin روی سرور production (Phase 1, آیتم #۲)
- AnalyticsController
- AuthenticatesRequests.php
- CargoCounterComponents.kt
- QuotaManagementDialog
- Animation Audit
- Authentication & Authorization
- Database Audit
- RealTimeLoadingCardSection.kt
- Error Handling
- Kotlin Audit
- UserPreferencesManager
- SecurityVerifier
- WindowSizeClass
- AppModule.kt
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
- QuotaExistenceMultipleResponse
- ColorScheme.kt
- CargoRegistrationNavigation.kt
- StatisticsCard.kt
- QuotaCalculatorTest
- update.md
- Typography.kt
- KeyboardType
- Modifier
- Context
- secrets.cpp
- AppNotificationManager
- SessionManager
- SessionManager
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
1. `ReportsViewModel` - 93 edges
2. `CargoViewModel` - 77 edges
3. `InputValidator` - 58 edges
4. `ApiServiceV2` - 55 edges
5. `UserPreferencesManager` - 45 edges
6. `ActiveShipInfo` - 37 edges
7. `UpdateManager` - 37 edges
8. `ReportsRepository` - 35 edges
9. `SessionRepository` - 35 edges
10. `Logger` - 34 edges

## Surprising Connections (you probably didn't know these)
- `ActiveQuotasDialog()` --calls--> `ActiveShipInfo`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/ActiveQuotasDialogSection.kt → core/network/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt
- `InitialInfoScreen()` --calls--> `InitialInfo`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/InitialInfoScreen.kt → core/network/src/main/java/com/atk/atk_cargo/data/model/CargoModels.kt
- `navigateToRegisterCargoActivity()` --calls--> `InitialInfo`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/SelectInfoScreen.kt → core/network/src/main/java/com/atk/atk_cargo/data/model/CargoModels.kt
- `navigateToRegisterCargoActivity()` --references--> `MatchingQuota`  [EXTRACTED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/SelectInfoScreen.kt → core/network/src/main/java/com/atk/atk_cargo/data/model/CargoModels.kt
- `filterShipsByTab()` --references--> `ActiveShipInfo`  [EXTRACTED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_counter/presentation/CargoCounterScreen.kt → core/network/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt

## Import Cycles
- None detected.

## Communities (239 total, 82 thin omitted)

### Community 0 - "StartupViewModel"
Cohesion: 0.13
Nodes (9): AndroidViewModel, Flow, Intent, StateFlow, RequestBatteryOptimization, SecurityCheckState, ShowMessage, StartupEvent (+1 more)

### Community 1 - "ReportsRepository"
Cohesion: 0.09
Nodes (10): ErrorResponse, HttpStatusException, Exception, Result, ReportsRepository, CargoInfoResponse, ComprehensiveAnalysisResponse, QuotaDetails (+2 more)

### Community 2 - "Security Audit"
Cohesion: 0.09
Nodes (23): Critical Issues, [CRITICAL] فضای رمز عبور ۴ رقمی عددی + هش بدون salt + قفل قابل دور زدن = تصاحب کامل حساب, [HIGH] `app_api.php` تمام داده‌ی گزارش‌گیری را بدون بررسی مجوز `view_reports` می‌دهد, High Issues, [HIGH] `PermissionManager.php`: نبود `session_regenerate_id` و نبود قفل brute-force, [HIGH] `PHP/vendor/` (شامل phpunit و phpstan) در web root و کامیت‌شده در گیت, [HIGH] `protected_proxy.php` یک مرز امنیتی نیست — همه‌ی endpointها مستقیماً هم قابل فراخوانی‌اند, [HIGH] session token و refresh token به‌صورت plaintext در دیتابیس ذخیره می‌شوند (+15 more)

### Community 3 - "CargoCounterScreen.kt"
Cohesion: 0.18
Nodes (19): CargoCounterScreen(), CargoCounterViewModel, filterShipsByTab(), com, CoroutineScope, NavController, SnackbarHostState, StateFlow (+11 more)

### Community 4 - "RegisterCargoDialogs.kt"
Cohesion: 0.29
Nodes (20): DeleteDialog(), DialogBadge(), DialogButtonRow(), DialogContentCard(), DialogMessageText(), DialogTitle(), Color, ImageVector (+12 more)

### Community 5 - "QuotaAnalysisSection.kt"
Cohesion: 0.07
Nodes (35): AnalyticsGroupingModeButton(), AnalyticsOwnerSummaryCard(), AnalyticsQuotaCard(), AnalyticsQuotaGroupExpansionPanel(), AnalyticsStatChip(), Context, OwnerQuotasDialog(), QuotaAnalysis() (+27 more)

### Community 6 - "ChatDao"
Cohesion: 0.09
Nodes (11): For /graphify explain, For /graphify path, graphify reference: query, path, explain, Step 0 — Constrained query expansion (REQUIRED before traversal), Step 1 — Traversal, AppDatabase, Context, ChatDao (+3 more)

### Community 7 - "**5.2 / 10**"
Cohesion: 0.11
Nodes (18): **5.2 / 10**, [CRITICAL] نبود پوشش تست روی مسیرهای بحرانی, Final Score, Overall Score, Production Readiness, Project Overview, Project Structure, Recommended Architecture (+10 more)

### Community 8 - "SecurityScreen.kt"
Cohesion: 0.25
Nodes (13): AdvancedOrbitalScanner(), AnimatedUpdateIcon(), AnimatedWarningIcon(), DynamicPremiumBackground(), InteractiveExitButton(), Modifier, RetryButton(), SecurityBlockScreen() (+5 more)

### Community 9 - "MicroCache"
Cohesion: 0.09
Nodes (5): MicroCache, AppApiCacheKeys, QuotaService, ShipService, QuotaCalculator

### Community 10 - "ActiveQuotasContent.kt"
Cohesion: 0.14
Nodes (28): formatNumber(), EmptySearchResult(), extractLastDigits(), FilterChip(), FilterState, ALL, COMPLETED, PENDING (+20 more)

### Community 12 - "TextAlign"
Cohesion: 0.22
Nodes (11): ConfirmationDialog(), ImageVector, EmptyState(), ImageVector, Modifier, DateTimePicker(), Modifier, PersianDateItem() (+3 more)

### Community 14 - "CargoModels.kt"
Cohesion: 0.11
Nodes (11): CargoDeleteResponse, CargoInfoSearch, CargoSearchResponse, CargoStats, CheckExistenceResponse, ExistingCargo, InitialInfo, LoadableTonnageResponse (+3 more)

### Community 15 - "Exception"
Cohesion: 0.12
Nodes (8): App\Enums\CargoConfirmStatus, App\Enums\CargoStatus, Exception, InvalidArgumentException, Response, ApiException, ConflictException, DatabaseException

### Community 16 - "Executive Summary"
Cohesion: 0.10
Nodes (19): Deep Code Audit Report, Executive Summary, Final Recommendations, **Overall: 5.2 / 10**, Overall Score, Production Readiness, Technical Debt, Top 20 Priority List (+11 more)

### Community 17 - "DEEP_CODE_REVIEW.md"
Cohesion: 0.10
Nodes (19): Dependency Audit, Final Score, Logging & Observability, [LOW] Koin 3.5.6, [LOW] `lateinit var currentDownloadFile` بدون گارد کامل, [MEDIUM] Compose BOM عملاً بی‌اثر است, [MEDIUM] آدرس پایه به مسیر `test_api/` اشاره می‌کند, [MEDIUM] نبود مانیتورینگ خودکار (+11 more)

### Community 19 - "quota_details/QuotaCardComponents.kt"
Cohesion: 0.21
Nodes (15): ActionButtonTest, ActionButton(), ConfirmationDialogHeader(), EditFieldBox(), EditFieldColumn(), EditQuotaDialog(), EditQuotaDialogHeader(), Color (+7 more)

### Community 21 - "CargoInfo"
Cohesion: 0.24
Nodes (18): SubmitCargoUseCase, CargoChangesPreview(), CargoEditConfirmDialog(), CargoEditConfirmHeader(), CargoMainInfo(), CargoShippingInfo(), CargoTimeInfo(), CargoWeightInfo() (+10 more)

### Community 22 - "HomeScreen.kt"
Cohesion: 0.13
Nodes (24): getMenuItemsForUserType(), CompactSwitch(), Color, ImageVector, Modifier, NotificationSettingRow(), ActionButtons(), getUserTypeDisplay() (+16 more)

### Community 23 - "ManageReportsScreen.kt"
Cohesion: 0.36
Nodes (11): addOneDayToPersianDate(), DatePickerDialog(), DateTimeSelectionCard(), getDaysInPersianMonth(), isPersianLeapYear(), Color, ImageVector, Modifier (+3 more)

### Community 26 - "MessageType"
Cohesion: 0.07
Nodes (51): Modifier, StatusSnackbar(), CargoDetailsScreen(), confirmCargo(), FloatingActionButtonItem(), handleCargoConfirmation(), Color, ImageVector (+43 more)

### Community 27 - "MainActivity.kt"
Cohesion: 0.28
Nodes (5): Intent, MainActivity, StartupErrorScreen(), Bundle, ComponentActivity

### Community 28 - "Medium Issues"
Cohesion: 0.12
Nodes (16): Critical Issues, [CRITICAL] کلیدهای API و لایسنس با XOR تک‌بایتی محافظت شده و در git ذخیره‌اند, Low Issues, [LOW] استفاده از `!!` روی state در Composable, [LOW] تابع native تعریف‌شده اما هرگز استفاده نشده, [LOW] فایل اطلاعات سرور در `.gitignore` نیست, [MEDIUM] endpointهای لایسنس بدون احراز هویت و بدون rate limit, Medium Issues (+8 more)

### Community 29 - "QuotaManagementContent.kt"
Cohesion: 0.15
Nodes (29): AnalyticsStatChipMini(), CompactStatChip(), InfoChip(), IntegratedQuotaCard(), Color, ImageVector, Modifier, QuotaShipExpansionPanel() (+21 more)

### Community 31 - "CargoViewModel"
Cohesion: 0.05
Nodes (26): CargoCounterRoute, cargoCounterScreen(), NavController, navigateToCargoCounter(), CargoCounterOperationScreen(), NavController, InitialInfoRoute, initialInfoScreen() (+18 more)

### Community 33 - "ApiServiceV2.kt"
Cohesion: 0.12
Nodes (18): ApiResponse2, Result, validateServerSession(), ActiveSessionResponse, CreateUserRequest, DeleteUserRequest, ForceLogoutRequest, ForceLogoutResponse (+10 more)

### Community 34 - "VoucherDetailsDialogSection.kt"
Cohesion: 0.22
Nodes (17): EmptyVoucherList(), Modifier, SearchTextField(), SortChip(), VoucherDetailsDialog(), VoucherExpandedDetails(), VoucherItem(), VoucherSearchAndFilter() (+9 more)

### Community 36 - "UsersManager"
Cohesion: 0.07
Nodes (7): ApiManager, CONFIG, ModalManager, ThemeManager, UIManager, UsersManager, Utils

### Community 40 - "Logger"
Cohesion: 0.14
Nodes (3): UtilityController, Logger, self

### Community 41 - "API Audit"
Cohesion: 0.17
Nodes (12): API Audit, [HIGH] دو API stack موازی — نسخه‌ی امن‌تر بلااستفاده است, [MEDIUM] طراحی API غیر-RESTful و ناسازگار, فاز ۳.۲ — ✅ انجام شد: حذف ۲۰ فایل ورودی legacy نسخه ۱, فاز ۳.۳ — ✅ کامل شد: تست‌های Phase 1/2, فاز ۳.۴ — ✅ انجام شد: وابستگی‌های تست, فاز ۳.۵ — ✅ کامل: `UiState` واحد در `CargoViewModel` و `ReportsViewModel`, فهرست endpointها (+4 more)

### Community 43 - "ATKCargoTheme"
Cohesion: 0.14
Nodes (11): ComponentStyles, Dimensions, Elevation, ATKCargoTheme, Motion, SemanticColors, AppShapes, Spacing (+3 more)

### Community 44 - "ApiServiceV2"
Cohesion: 0.09
Nodes (10): ProfileSettingsDialog(), ApiResponse, ApiServiceV2, DeleteMessageRequest, EditMessageRequest, UpdateUserRequest, User, SuccessResponse (+2 more)

### Community 46 - "FontWeight"
Cohesion: 0.23
Nodes (6): AnimatedCounter(), ExitStatusDialog(), ImageVector, VazirmatnFontFamily, FontFamily, FontWeight

### Community 48 - "Executive Summary"
Cohesion: 0.13
Nodes (15): Architecture Overview, Deep Code Audit Report, Executive Summary, Overall Score, Overall Score: **6.1 / 10**, Production Readiness, Project Overview, Technical Debt (+7 more)

### Community 49 - "CameraSection.kt"
Cohesion: 0.17
Nodes (13): EnhancedNumberAnalyzer, extractNumber(), ImageProxy, InputImage, recognizeTextFromImage(), EnhancedCameraPreview(), ImageCapture, Color (+5 more)

### Community 52 - "ReportModels.kt"
Cohesion: 0.08
Nodes (23): AndroidViewModel, Color, Job, StateFlow, AnalyticsData, ComprehensiveAnalytics, DateInfo, ExistingQuota (+15 more)

### Community 53 - "Performance Audit"
Cohesion: 0.20
Nodes (10): [HIGH] `permissions.json` در هر بررسی مجوز از دیسک خوانده و parse می‌شود, [LOW] subquery همبسته در فهرست پیام‌های چت, [MEDIUM] `updateUser` کل جدول کاربران را برای یافتن رکورد خود کاربر می‌خواند, [MEDIUM] `user_sessions` با ۱۳ ایندکس که در هر درخواست به‌روزرسانی می‌شود, [MEDIUM] درخواست‌های شبکه‌ی زائد بعد از هر ثبت حواله, [MEDIUM] نبود صفحه‌بندی در endpointهای فهرستی, Performance Audit, خلاصه‌ی اثر (+2 more)

### Community 54 - "QuotaDetailsScreen.kt"
Cohesion: 0.29
Nodes (11): ApiQuotaDetails, adjustColorForTheme(), Color, toTon(), ProgressBar(), QuotaAdditionalInfo(), QuotaDetails(), QuotaInfoCards() (+3 more)

### Community 55 - "Kotlin Audit"
Cohesion: 0.22
Nodes (9): [HIGH] باگ منطقی: مسیر پاک‌سازی نشست منقضی در `PermissionPoller` هرگز اجرا نمی‌شود, Kotlin Audit, [LOW] `Strictness.LENIENT` در Gson, [MEDIUM] `runBlocking` داخل `Authenticator` روی thread شبکه‌ی OkHttp, [MEDIUM] بسته‌بندی مکرر استثنا که نوع و stack trace را نابود می‌کند, [MEDIUM] بلعیدن خطا و بازگرداندن نتیجه‌ی خالی در جستجوها, [MEDIUM] پیاده‌سازی دوگانه‌ی تبدیل تاریخ جلالی, نکات مثبت (+1 more)

### Community 56 - "App\Core\AuthenticatesRequests"
Cohesion: 0.18
Nodes (9): App\Core\AuthenticatesRequests, App\Core\DatabaseManager, App\Core\MicroCache, App\Core\Request, App\Core\Response, App\Services\LoginAttemptLimiter, App\Services\ShipService, App\Validators\InputValidator (+1 more)

### Community 58 - "CrashReporter.kt"
Cohesion: 0.46
Nodes (4): CrashReporter, Context, CoroutineScope, PendingCrashReport

### Community 60 - "SearchDialogs.kt"
Cohesion: 0.29
Nodes (10): CargoSearchResultCard(), InfoRowCompact(), Color, Context, ImageVector, MultipleSearchResultDialog(), SearchType, RECEIPT_NUMBER (+2 more)

### Community 61 - "PermissionPoller"
Cohesion: 0.36
Nodes (3): Job, StateFlow, PermissionPoller

### Community 62 - "QuotaPercentageDialogSection.kt"
Cohesion: 0.21
Nodes (18): calculateValues(), AnimatedNumber(), DialogHeader(), Color, ImageVector, Modifier, lerp(), PercentageDisplay() (+10 more)

### Community 63 - "UserService"
Cohesion: 0.14
Nodes (4): App\Exceptions\ApiException, App\Repositories\UserRepository, AuditLogger, UserService

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
Cohesion: 0.70
Nodes (4): AdvancedSearchDialog(), Modifier, SearchTabInfo, SearchTypeTabRow()

### Community 69 - "Android Audit"
Cohesion: 0.29
Nodes (7): Android Audit, [LOW] `versionCode = 11` در برابر `versionName = "4.0.1"`, [MEDIUM] `targetSdk = 34` در حالی که `compileSdk = 36` است, [MEDIUM] `viewBinding = true` در یک اپ کاملاً Compose, [MEDIUM] وابستگی‌های بلااستفاده در گراف build, نکات مثبت (تأییدشده), یافته‌ها

### Community 70 - "Code Quality"
Cohesion: 0.33
Nodes (6): Code Quality, Code Smells شناسایی‌شده, [HIGH] Primitive Obsession روی مقادیر وزن و تناژ, [MEDIUM] DTO و مدل دامنه تفکیک نشده‌اند, [MEDIUM] Magic Numbers بدون نام, مهم‌ترین‌ها با جزئیات

### Community 71 - "Database Audit"
Cohesion: 0.29
Nodes (7): Database Audit, [LOW] `SET SESSION sql_mode = 'NO_ENGINE_SUBSTITUTION'` حالت strict را خاموش می‌کند, [MEDIUM] `schema.sql` قدیمی و ناقص است, [MEDIUM] هیچ FOREIGN KEY در کل schema وجود ندارد, ساختار (از `schema.sql` — ✅ به‌روزرسانی شد در Phase 2.3، اکنون کامل), نکات مثبت, یافته‌ها

### Community 73 - "ManageReportsScreen"
Cohesion: 0.18
Nodes (16): MainScreen(), RouteTransitions, standardTransitions(), HomeRoute, homeScreen(), NavController, navigateToHome(), CargoDetailsRoute (+8 more)

### Community 74 - "Final Recommendations"
Cohesion: 0.25
Nodes (8): Final Recommendations, Roadmap عملی, ارزیابی صادقانه, سه توصیه‌ی کلیدی, معیار موفقیت, ۱. رازها را همین امروز بچرخانید — سپس مدل را عوض کنید, ۲. مرز لایه‌ها را ببندید — این باگ است، نه سلیقه, ۳. تست‌هایی که دارید را اجرا کنید

### Community 75 - "Jetpack Compose Audit"
Cohesion: 0.29
Nodes (7): [HIGH] God Composable — فایل‌های نمایشی با بیش از ۱۰۰۰ خط, [HIGH] صفر مورد `collectAsStateWithLifecycle` در برابر ۸۵ مورد `collectAsState`, Jetpack Compose Audit, [LOW] تمام رشته‌های UI به‌صورت literal در کد, [MEDIUM] استفاده‌ی کم از `derivedStateOf` در جاهایی که واقعاً لازم است, [MEDIUM] نبود `UiState` واحد — ۲۰+ `StateFlow` مجزا در هر ViewModel, [MEDIUM] ۲۲ مورد از ۳۵ فراخوانی `items(...)` بدون `key`

### Community 76 - "ApiV2Routes"
Cohesion: 0.10
Nodes (5): InputValidationResult, QuotaValidationUseCase, TempTonnageValidationResult, ApiV2Routes, QuotaValidationResult

### Community 77 - "PHP Backend Audit"
Cohesion: 0.29
Nodes (7): [HIGH] `migrations/` خالی است و `schema.sql` ناقص — جدول `audit_log` هرگز ساخته نمی‌شود, [LOW] `Config::$settings['session_timeout']` و `admin_password_hash` بلااستفاده‌اند, [MEDIUM] `execute()` در پروکسی فقط `Exception` را می‌گیرد، نه `Throwable`, [MEDIUM] `flushLog` پروکسی وقتی هدف `exit` می‌کند اجرا نمی‌شود, PHP Backend Audit, نکات مثبت, یافته‌ها

### Community 78 - "ChatMessage.kt"
Cohesion: 0.29
Nodes (5): ChatMessage, ChatMessagesResponse, SendMessageRequest, SendMessageResponse, UnreadCountResponse

### Community 80 - "Logging & Observability"
Cohesion: 0.29
Nodes (7): [HIGH] پوشه‌های لاگ ممکن است از طریق وب قابل دسترس باشند, Logging & Observability, [LOW] ~۹۰ فراخوانی `Log.*` در کد تولید, [MEDIUM] نبود مانیتورینگ و هشدار, آنچه لاگ می‌شود, نکات مثبت, یافته‌ها

### Community 82 - "RetrofitClient"
Cohesion: 0.07
Nodes (21): CryptoManager, Authenticator, ByteArray, Cipher, FloatTypeAdapter, Context, OkHttpClient, RetrofitClient (+13 more)

### Community 83 - "InitialInfoScreen"
Cohesion: 0.17
Nodes (23): formatNumber(), isValidPersianText(), isValidQuotaNumber(), isValidShipName(), isValidWarehouseName(), isValidWeight(), ConfirmationDialog(), DuplicateDialog() (+15 more)

### Community 85 - "Code Smells شناسایی‌شده"
Cohesion: 0.29
Nodes (7): Code Quality, Code Smells شناسایی‌شده, God Files (۳۱ فایل > ۶۰۰ خط), [LOW] دایرکتوری‌های خالی باقی‌مانده از ماژول‌بندی, [LOW] نام کاملاً واجد شرایط (FQN) درون بدنه‌ی کلاس, [MEDIUM] `CargoUiState` با ۱۶ فیلد, نکته‌ی مثبت درباره‌ی کیفیت: کامنت‌گذاری

### Community 86 - "Authentication & Authorization"
Cohesion: 0.40
Nodes (5): Authentication & Authorization, [MEDIUM] `check_logout.php` بدون احراز هویت, ارزیابی, جریان کامل احراز هویت, یافته‌ی اضافی

### Community 88 - "Color.kt"
Cohesion: 0.16
Nodes (14): CompactStatChipTest, GroupingModeButton(), GroupingModeSelector(), ImageVector, Modifier, ImageVector, Modifier, StateFlow (+6 more)

### Community 89 - "ReportsCommonWidgets.kt"
Cohesion: 0.29
Nodes (14): FabItem, CompactStatChip(), EmptyQuotaState(), EmptyShipsState(), EmptyStateCard(), ErrorStateCard(), FloatingActionButton(), InfoCard() (+6 more)

### Community 90 - "RealTimeLoadingBottomSheet"
Cohesion: 0.35
Nodes (9): DialogHeader(), Color, Modifier, RealTimeLoadingBottomSheet(), RealTimeShiftNavigation(), RefreshOverlay(), ShipCard(), RealTimeUiState (+1 more)

### Community 91 - "Dependency Audit"
Cohesion: 0.33
Nodes (6): Android, Dependency Audit, [LOW] `material3 = "1.3.2"` صریح، نسخه‌ی BOM را override می‌کند, [MEDIUM] `composer.json` قید `php: >=7.4` دارد ولی کد PHP 8 لازم دارد, [MEDIUM] `itextpdf 5.5.13.4` — مجوز AGPL و پایان پشتیبانی, PHP

### Community 92 - "Animation Audit"
Cohesion: 0.33
Nodes (6): Animation Audit, [INFO] نقاط قوت انیمیشن, [LOW] انیمیشن شمارنده‌ی معکوس ثانیه‌ای که هر ثانیه recomposition ایجاد می‌کند, [MEDIUM] نبود پشتیبانی از Reduce Motion / دسترس‌پذیری, وضعیت کلی, یافته‌ها

### Community 93 - "Migrations"
Cohesion: 0.33
Nodes (5): Migrations, migrationهای اعمال‌شده تاکنون, قرارداد نام‌گذاری, نحوه‌ی اجرا, وضعیت schema.sql

### Community 94 - "Quota"
Cohesion: 0.17
Nodes (17): calculateWarningStatus(), ErrorStateCard(), ImageVector, ShipDetails(), ShipHeaderCard(), TabItem(), WarehouseQuotasTabs(), WarehousesAndQuotasTab() (+9 more)

### Community 95 - "SelectInfoScreen.kt"
Cohesion: 0.25
Nodes (14): AnimatedHeader(), GroupedShipList(), Color, Modifier, NavController, Result, navigateToRegisterCargoActivity(), SelectInfoScreenContent() (+6 more)

### Community 96 - "پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13)"
Cohesion: 0.22
Nodes (8): ساختار فعلی (مستند شده در این جلسه), ساختار هدف, مراحل اجرا (ترتیب دقیق مهم است), معیار موفقیت, پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13), پیش‌نیازهای جانبی که باید قبل از این کار حل شوند, چرا فقط .htaccess کافی نیست, چرا لازم است

### Community 97 - "UserRepository"
Cohesion: 0.12
Nodes (3): PDO, PDO, UserRepository

### Community 98 - "Ship"
Cohesion: 0.13
Nodes (21): ErrorState(), ImageVector, Modifier, ImageVector, Modifier, ShipCard(), ShipCardContent(), ShipSortingModeButton() (+13 more)

### Community 99 - "Database"
Cohesion: 0.10
Nodes (7): mysqli, PDOException, LicenseController, Config, self, Database, self

### Community 100 - "ShipInfoSection.kt"
Cohesion: 0.30
Nodes (12): formatNumber(), toEnglishNumbers(), DetailedInfoGrid(), DetailInfoItem(), ExpandedContent(), Color, ImageVector, Modifier (+4 more)

### Community 101 - "High Issues"
Cohesion: 0.33
Nodes (6): [HIGH] build release هیچ signingConfig ندارد, [HIGH] Composableها مستقیماً شبکه را صدا می‌زنند و عملیات نوشتن با ناوبری cancel می‌شود, High Issues, [HIGH] phpMyAdmin روی سرور production نصب است, [HIGH] سرور production روی نسخه‌ی PHP بدون پشتیبانی امنیتی اجرا می‌شود, [HIGH] مسیر fallback احراز هویت، رمز عبور متن‌خام را می‌پذیرد

### Community 102 - "Memory Audit"
Cohesion: 0.40
Nodes (5): [LOW] `ChatNotificationWorker` و چرخه‌ی حیات دیتابیس, [MEDIUM] فهرست‌های بدون سقف در `StateFlow`, Memory Audit, وضعیت کلی: **۷/۱۰ — بهترین حوزه‌ی پروژه**, یافته‌ها

### Community 103 - "Prioritized Action Plan"
Cohesion: 0.33
Nodes (6): Phase 1 — Immediate (این هفته — بازدارنده‌های تولید), Phase 2 — High Priority (۲ تا ۴ هفته), Phase 3 — Medium Priority (۱ تا ۳ ماه), Phase 4 — Optimization (۳ تا ۶ ماه), Phase 5 — تکمیل موارد باقی‌مانده (اجرا شده پس از بازبینی نهایی گزارش), Prioritized Action Plan

### Community 104 - "RegisterCargoScreen.kt"
Cohesion: 0.26
Nodes (10): Modifier, SearchBar(), DuplicateTrackingNumbersDialog(), NetWeightDialog(), FormSection(), ErrorHandlingCargoInfoRow(), RegisterCargoScreen(), RegisterPalette (+2 more)

### Community 106 - "CargoInfoDetailsDialogSection.kt"
Cohesion: 0.35
Nodes (12): CargoDetailsPalette, CargoDetailTabItem(), CargoInfoDetailsDialog(), DetailInfoRow(), ImageVector, Modifier, SnackbarHostState, MainInfoTabContent() (+4 more)

### Community 107 - "jdate"
Cohesion: 0.53
Nodes (9): gregorian_to_jalali(), jalali_to_gregorian(), jcheckdate(), jdate(), jdate_words(), jgetdate(), jmktime(), jstrftime() (+1 more)

### Community 108 - "ChatNotificationWorker.kt"
Cohesion: 0.48
Nodes (4): ChatNotificationWorker, KoinComponent, CoroutineWorker, ListenableWorker

### Community 109 - "Android Audit"
Cohesion: 0.40
Nodes (5): Android Audit, [LOW] پرچم منسوخ در `gradle.properties`, [MEDIUM] `targetSdk = 34` در حالی که `compileSdk = 36`, مشکلات, نقاط قوت

### Community 110 - "StartupState"
Cohesion: 0.33
Nodes (6): Ready, SecurityBlocked, Splash, StartupState, Syncing, VersionExpired

### Community 113 - "UpdateManager"
Cohesion: 0.08
Nodes (24): UpdateInfo, Completed, Downloading, DownloadProgress, DownloadState, Error, Idle, Context (+16 more)

### Community 114 - "ComprehensiveAnalyticsDialog.kt"
Cohesion: 0.57
Nodes (6): AnalyticsDateNavigation(), AnalyticsHeaderCard(), ComprehensiveAnalyticsDialog(), EmptyStateCard(), ErrorStateCard(), Modifier

### Community 115 - "API Audit"
Cohesion: 0.40
Nodes (5): API Audit, [LOW] نبود Rate Limiting سراسری روی Router v2, [MEDIUM] دو شکل ناسازگار برای پاسخ خطا, [MEDIUM] عدم رعایت معنای متدهای HTTP, استخراج Endpointها

### Community 116 - "Testing Audit"
Cohesion: 0.40
Nodes (5): [HIGH] CI هیچ تست اندرویدی اجرا نمی‌کند, Test Caseهای پیشنهادی (اولویت‌دار), Testing Audit, بخش‌های بحرانی بدون تست, وضعیت موجود

### Community 117 - "Jetpack Compose Audit"
Cohesion: 0.40
Nodes (5): [HIGH] انیمیشن‌ها باعث recomposition در هر فریم می‌شوند, Jetpack Compose Audit, [MEDIUM] Composableهای خدای‌گونه, مشکلات, نقاط قوت (واقعاً چشمگیر)

### Community 118 - "ActiveShipInfo"
Cohesion: 0.44
Nodes (9): GroupedShipList(), Color, QuotaCard(), ShipGroup(), ShipHeader(), WarehouseCard(), WarehouseList(), ShipSelectionDialog() (+1 more)

### Community 119 - "QuotaSelectionDialog.kt"
Cohesion: 0.42
Nodes (10): DialogHeader(), findDifferentFields(), InactiveIndicator(), ImageVector, QuotaDetailItem(), QuotaDetails(), QuotaHeader(), QuotaItem() (+2 more)

### Community 120 - "چک‌لیست فعال‌سازی `STRICT_TRANS_TABLES` (DEEP_CODE_AUDIT.md #Phase4.10)"
Cohesion: 0.33
Nodes (5): مراحل امن (ترتیب مهم است), مسئله چیست, معیار موفقیت, چرا نمی‌شود همین الان فعالش کرد, چک‌لیست فعال‌سازی `STRICT_TRANS_TABLES` (DEEP_CODE_AUDIT.md #Phase4.10)

### Community 121 - "preprocessImage"
Cohesion: 0.50
Nodes (8): adaptiveThresholding(), applyContrastEnhancement(), enhanceEdges(), ImageProxy, InputImage, medianFilter(), preprocessImage(), IntArray

### Community 122 - "What You Must Do When Invoked"
Cohesion: 0.07
Nodes (26): For /graphify add and --watch, For /graphify query, For the commit hook and native CLAUDE.md integration, For --update and --cluster-only, /graphify, Honesty Rules, Interpreter guard for subcommands, Part A - Structural extraction for code files (+18 more)

### Community 123 - "Performance Audit"
Cohesion: 0.40
Nodes (5): [HIGH] ایندکس گمشده روی `trackingNumber`, [MEDIUM] تاریخ و زمان به‌صورت `varchar(100)` ذخیره می‌شوند, [MEDIUM] حلقه‌ی شمارش معکوس با `delay(200)`, Performance Audit, نقاط قوت Performance

### Community 124 - "Prioritized Action Plan"
Cohesion: 0.40
Nodes (5): Phase 1 — Immediate (هفته‌ی ۱), Phase 2 — High Priority (هفته‌های ۲–۴), Phase 3 — Medium Priority (ماه‌های ۲–۳), Phase 4 — Optimization (ماه‌های ۴+), Prioritized Action Plan

### Community 128 - "QuotaEntryDialog.kt"
Cohesion: 0.57
Nodes (7): FocusRequester, QuotaEntryActions(), QuotaEntryContent(), QuotaEntryDialog(), QuotaEntryPalette, rememberQuotaEntryPalette(), validateAndSubmit()

### Community 129 - "QuotaCard"
Cohesion: 0.39
Nodes (7): DeleteQuotaDialog(), androidx, Color, QuotaConfirmDialog(), ToggleQuotaStatusDialog(), QuotaCard(), QuotaGroupExpansionPanel()

### Community 130 - "Architecture Overview"
Cohesion: 0.50
Nodes (4): Architecture Overview, جریان طراحی‌شده ولی بلااستفاده, جریان واقعی داده (آنچه امروز اجرا می‌شود), نقاط مرزی پرریسک بین لایه‌ها

### Community 131 - "Error Handling"
Cohesion: 0.50
Nodes (4): Error Handling, [MEDIUM] نبود مکانیزم مرکزی گزارش خطا (Crash Reporting), نکات مثبت, یافته‌های تجمیعی

### Community 132 - "حذف/محدودسازی phpMyAdmin روی سرور production (Phase 1, آیتم #۲)"
Cohesion: 0.40
Nodes (4): حذف/محدودسازی phpMyAdmin روی سرور production (Phase 1, آیتم #۲), راستی‌آزمایی نهایی, گزینه‌ی ۱ — حذف کامل (توصیه‌شده), گزینه‌ی ۲ — اگر حذف فوری ممکن نیست

### Community 134 - "AuthenticatesRequests.php"
Cohesion: 0.80
Nodes (4): enforceMinAppVersion(), requireAuthenticatedSession(), requirePermission(), sendAuthErrorResponse()

### Community 135 - "CargoCounterComponents.kt"
Cohesion: 0.57
Nodes (6): CargoSnackbarMessage, Color, Modifier, snackbarAccent(), snackbarAccentBg(), StatusSnackbar()

### Community 137 - "Animation Audit"
Cohesion: 0.50
Nodes (4): Animation Audit, [LOW] عدم پشتیبانی از Reduce Motion, [MEDIUM] انیمیشن‌های بی‌نهایت روی صفحات پرکاربرد, آمار

### Community 138 - "Authentication & Authorization"
Cohesion: 0.50
Nodes (4): Authentication & Authorization, [MEDIUM] نشست‌های پنل وب هرگز نمی‌توانند از گیت API عبور کنند, جریان کامل, نقاط قوت (تأییدشده)

### Community 139 - "Database Audit"
Cohesion: 0.50
Nodes (4): Database Audit, [LOW] نبود جدول ردیابی migration, مشکلات, نقاط قوت

### Community 140 - "RealTimeLoadingCardSection.kt"
Cohesion: 0.43
Nodes (6): CompactInfo(), Color, ImageVector, Modifier, RealTimeLoadingCard(), StatisticItem()

### Community 141 - "Error Handling"
Cohesion: 0.50
Nodes (4): Error Handling, [MEDIUM] مدیریت خطا در UI به‌صورت پراکنده و تکراری, مشکلات, نقاط قوت

### Community 142 - "Kotlin Audit"
Cohesion: 0.50
Nodes (4): Kotlin Audit, [MEDIUM] UseCaseها به‌جای تزریق وابستگی، singleton را مستقیم می‌گیرند, ارزیابی: 8/10 — تمیزترین بخش پروژه, مشکلات

### Community 144 - "UserPreferencesManager"
Cohesion: 0.05
Nodes (19): BootReceiver, BroadcastReceiver, Context, Intent, KoinComponent, Context, Intent, KoinComponent (+11 more)

### Community 145 - "SecurityVerifier"
Cohesion: 0.10
Nodes (11): SecurityErrorType, LICENSE_INACTIVE, LICENSE_NOT_FOUND, NETWORK_ERROR, TAMPERED, UNKNOWN_ERROR, SecurityVerifier, Secrets (+3 more)

### Community 147 - "WindowSizeClass"
Cohesion: 0.33
Nodes (6): AdaptiveLayoutConfig, rememberAdaptiveLayoutConfig(), WindowSizeClass, COMPACT, EXPANDED, MEDIUM

### Community 149 - "PHP Backend Audit"
Cohesion: 0.50
Nodes (4): [MEDIUM] معماری دوگانه: shimهای مستقیم، Router را دور می‌زنند, PHP Backend Audit, دفاع لایه‌ای (نمونه‌ی خوب), معماری

### Community 150 - "چرخش رازهای `secrets.cpp` (Phase 1, آیتم #۱)"
Cohesion: 0.50
Nodes (3): تاریخچه‌ی git, مراحل, چرخش رازهای `secrets.cpp` (Phase 1, آیتم #۱)

### Community 178 - "PHP/composer.json"
Cohesion: 0.09
Nodes (21): autoload, autoload-dev, psr-4, psr-4, config, optimize-autoloader, sort-packages, description (+13 more)

### Community 187 - "ColorScheme.kt"
Cohesion: 0.80
Nodes (5): ColorScheme, buildAppDarkColorScheme(), buildAppLightColorScheme(), compositeOver(), Color

### Community 188 - "CargoRegistrationNavigation.kt"
Cohesion: 0.60
Nodes (4): CargoRegistrationRoute, cargoRegistrationScreen(), NavController, navigateToCargoRegistration()

### Community 189 - "StatisticsCard.kt"
Cohesion: 0.70
Nodes (4): Color, ImageVector, Modifier, StatisticsCard()

### Community 191 - "update.md"
Cohesion: 0.50
Nodes (3): For --cluster-only, For --update (incremental re-extraction), graphify reference: incremental update and cluster-only

### Community 192 - "Typography.kt"
Cohesion: 0.83
Nodes (3): createTypography(), TextStyle, Typography

### Community 221 - "secrets.cpp"
Cohesion: 0.34
Nodes (16): decryptXor(), JNI_OnLoad(), n0(), n1(), n2(), n3(), n4(), n5() (+8 more)

### Community 272 - "Request"
Cohesion: 0.10
Nodes (5): DiagnosticsController, OnlineUsersController, MinVersionGate, Request, Throwable

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
- **388 isolated node(s):** `QuotaWarningThresholds`, `RequestBatteryOptimization`, `ErrorResponse`, `ShipsTabItem`, `Ready` (+383 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **82 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReportsViewModel` connect `ReportsViewModel` to `QuotaWarningDialog.kt`, `Ship`, `VoucherDetailsDialogSection.kt`, `ColorSelector`, `LoadingState`, `QuotaManagementDialog`, `ManageReportsScreen`, `ComprehensiveAnalyticsDialog.kt`, `AppModule.kt`, `CargoInfo`, `QuotaDetailsScreen.kt`, `ManageReportsScreen.kt`, `Color.kt`, `ReportModels.kt`, `RealTimeLoadingBottomSheet`, `QuotaManagementContent.kt`, `Quota`?**
  _High betweenness centrality (0.044) - this node is a cross-community bridge._
- **Why does `RetrofitClient` connect `RetrofitClient` to `StartupViewModel`, `CargoCounterScreen.kt`, `QuotaManagementDialog`, `ApiServiceV2`, `ChatNotificationWorker.kt`, `InitialInfoScreen`, `AppModule.kt`, `HomeScreen.kt`, `MessageType`?**
  _High betweenness centrality (0.036) - this node is a cross-community bridge._
- **Why does `ApiServiceV2` connect `ApiServiceV2` to `ReportsRepository`, `ApiServiceV2.kt`, `Ship`, `ApiV2Routes`, `CargoModels.kt`, `ChatMessage.kt`, `RetrofitClient`, `CargoInfo`, `ActiveShipInfo`, `QuotaExistenceMultipleResponse`, `QuotaManagementContent.kt`, `Quota`, `CargoViewModel`?**
  _High betweenness centrality (0.028) - this node is a cross-community bridge._
- **Are the 10 inferred relationships involving `InputValidator` (e.g. with `.handle()` and `.handlePost()`) actually correct?**
  _`InputValidator` has 10 INFERRED edges - model-reasoned connections that need verification._
- **What connects `QuotaWarningThresholds`, `RequestBatteryOptimization`, `ErrorResponse` to the rest of the system?**
  _388 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `StartupViewModel` be split into smaller, more focused modules?**
  _Cohesion score 0.13105413105413105 - nodes in this community are weakly interconnected._
- **Should `ReportsRepository` be split into smaller, more focused modules?**
  _Cohesion score 0.08961593172119488 - nodes in this community are weakly interconnected._