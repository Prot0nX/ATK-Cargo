# Graph Report - ATK-Cargo  (2026-08-18)

## Corpus Check
- 280 files · ~384,936 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2552 nodes · 5267 edges · 187 communities (148 shown, 39 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 134 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `1f24ed41`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- StartupViewModel
- ExportPdfUseCase
- Security Audit
- CargoCounterScreen.kt
- TextAlign
- LoadingNotificationService
- ChatMessageEntity
- **5.2 / 10**
- SessionServiceTest
- Database
- ActiveQuotasContent.kt
- ChatToolbar.kt
- SelectInfoScreen.kt
- JalaliDateUtilsTest
- CargoDetailsScreen.kt
- QuotaPercentageDialogSection.kt
- Executive Summary
- Cargo
- ApiServiceV2
- quota_details/QuotaCardComponents.kt
- SessionService
- ApiV2Routes
- HomeScreen.kt
- PermissionServiceTest
- LoginScreen.kt
- SessionRepository
- MessageType
- MicroCache
- CameraSection.kt
- QuotaManagementContent.kt
- Color.kt
- CargoViewModel
- ApiException
- AuthModels.kt
- ReportsRepository
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
- formatNumber
- FontWeight
- ChatScreen.kt
- CargoInfoDetailsDialogSection.kt
- ActiveShipInfo
- MessageBubble.kt
- LoginAttemptLimiterTest
- UserService
- Performance Audit
- ShipDetailsScreen.kt
- Kotlin Audit
- LoginAttemptLimiter
- ManageReportsScreen.kt
- CrashReporter.kt
- AuthRepositoryImpl
- ReportModels.kt
- PermissionPoller
- ActiveQuotasDialogSection.kt
- CargoViewModel.kt
- UpdateDialog.kt
- ComprehensiveAnalyticsDialog.kt
- CryptoManager
- AuthViewModel
- JalaliDateUtils
- Android Audit
- Code Quality
- Database Audit
- SplashScreen.kt
- MainScreen.kt
- DatabaseManager
- Jetpack Compose Audit
- RegisterCargoScreen.kt
- PHP Backend Audit
- ChatRepository
- CargoDetailsComponents.kt
- Logging & Observability
- CargoInfo
- SearchBar.kt
- InitialInfoScreen
- ColorScheme.kt
- AppDatabase
- Authentication & Authorization
- MessageInputArea
- Quota
- update.md
- RealTimeLoadingBottomSheet
- Dependency Audit
- Animation Audit
- Migrations
- Router
- Config
- پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13)
- UserRepository
- Ship
- RetrofitClient
- QuotaSelectionDialog.kt
- preprocessImage
- Memory Audit
- Prioritized Action Plan
- AuthenticatesRequests.php
- ChatViewModel
- ProfileSettingsDialogSection.kt
- jdate
- DownloadState
- ProfileMenu.kt
- QuotaEntryDialog.kt
- ColorExtensions.kt
- UpdateManager
- rememberAdaptiveLayoutConfig
- CargoCounterOperationScreen
- LoginUiState
- CargoEntryNavigation.kt
- ChatNavigation.kt
- AnimationManager
- TokenAuthenticator
- ChatNotificationWorker.kt
- What You Must Do When Invoked
- graphify reference: query, path, explain
- ReportsNavigation.kt
- QuotaExistenceMultipleResponse
- ComponentDefaults.kt
- InitialInfoFormComponents.kt
- HomeNotificationSettingRow.kt
- Architecture Overview
- Error Handling
- AnalyticsController
- UserPreferencesManager
- SecurityVerifier
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
- TokenRefresher.kt
- ChatController
- DatabaseSchemaExporter
- graphify reference: extra exports and benchmark
- BootReceiver.kt
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
- Constants.kt
- CLAUDE.md
- .claude/CLAUDE.md
- extraction-spec.md

## God Nodes (most connected - your core abstractions)
1. `ReportsViewModel` - 100 edges
2. `CargoViewModel` - 77 edges
3. `InputValidator` - 61 edges
4. `ApiServiceV2` - 58 edges
5. `UserPreferencesManager` - 49 edges
6. `formatNumber()` - 45 edges
7. `UpdateManager` - 37 edges
8. `ActiveShipInfo` - 36 edges
9. `Request` - 35 edges
10. `SessionRepository` - 35 edges

## Surprising Connections (you probably didn't know these)
- `LoginScreen()` --calls--> `rememberAdaptiveLayoutConfig()`  [EXTRACTED]
  app/src/main/java/com/atk/atk_cargo/feature/auth/presentation/LoginScreen.kt → core/designsystem/src/main/java/com/atk/atk_cargo/ui/theme/AdaptiveLayout.kt
- `RetrofitClient` --calls--> `UserPreferencesManager`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/api/RetrofitClient.kt → app/src/main/java/com/atk/atk_cargo/api/UserPreferencesManager.kt
- `validateServerSession()` --calls--> `SessionCheckRequest`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/api/SessionValidator.kt → app/src/main/java/com/atk/atk_cargo/data/model/AuthModels.kt
- `PersianDatePickerDialog()` --calls--> `FilterChip()`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/core/ui/components/DateRangePicker.kt → app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/ActiveQuotasContent.kt
- `InitialInfoScreen()` --calls--> `InitialInfo`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/InitialInfoScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/CargoModels.kt

## Import Cycles
- None detected.

## Communities (187 total, 39 thin omitted)

### Community 0 - "StartupViewModel"
Cohesion: 0.10
Nodes (15): AndroidViewModel, Flow, Intent, StateFlow, Ready, RequestBatteryOptimization, SecurityBlocked, SecurityCheckState (+7 more)

### Community 1 - "ExportPdfUseCase"
Cohesion: 0.26
Nodes (10): ExportPdfUseCase, PdfColorScheme, PdfFonts, PersianFontManager, ShamsiDate, Document, Font, PdfPCell (+2 more)

### Community 2 - "Security Audit"
Cohesion: 0.09
Nodes (23): Critical Issues, [CRITICAL] فضای رمز عبور ۴ رقمی عددی + هش بدون salt + قفل قابل دور زدن = تصاحب کامل حساب, [HIGH] `app_api.php` تمام داده‌ی گزارش‌گیری را بدون بررسی مجوز `view_reports` می‌دهد, High Issues, [HIGH] `PermissionManager.php`: نبود `session_regenerate_id` و نبود قفل brute-force, [HIGH] `PHP/vendor/` (شامل phpunit و phpstan) در web root و کامیت‌شده در گیت, [HIGH] `protected_proxy.php` یک مرز امنیتی نیست — همه‌ی endpointها مستقیماً هم قابل فراخوانی‌اند, [HIGH] session token و refresh token به‌صورت plaintext در دیتابیس ذخیره می‌شوند (+15 more)

### Community 3 - "CargoCounterScreen.kt"
Cohesion: 0.15
Nodes (24): CargoCounterScreen(), CargoCounterViewModel, CargoSnackbarMessage, filterShipsByTab(), com, CoroutineScope, NavController, SnackbarHostState (+16 more)

### Community 4 - "TextAlign"
Cohesion: 0.28
Nodes (21): DeleteDialog(), DialogBadge(), DialogButtonRow(), DialogContentCard(), DialogMessageText(), DialogTitle(), Color, ImageVector (+13 more)

### Community 5 - "LoadingNotificationService"
Cohesion: 0.21
Nodes (7): Context, Intent, KoinComponent, LoadingNotificationService, TypeToken, IBinder, Service

### Community 6 - "ChatMessageEntity"
Cohesion: 0.15
Nodes (3): ChatDao, Flow, ChatMessageEntity

### Community 7 - "**5.2 / 10**"
Cohesion: 0.11
Nodes (18): **5.2 / 10**, [CRITICAL] نبود پوشش تست روی مسیرهای بحرانی, Final Score, Overall Score, Production Readiness, Project Overview, Project Structure, Recommended Architecture (+10 more)

### Community 9 - "Database"
Cohesion: 0.09
Nodes (12): App\Core\AuthenticatesRequests, InvalidArgumentException, mysqli, PDO, PDOException, LicenseController, Database, PDO (+4 more)

### Community 10 - "ActiveQuotasContent.kt"
Cohesion: 0.23
Nodes (17): formatNumber(), EmptySearchResult(), extractLastDigits(), FilterChip(), FilterState, ALL, COMPLETED, PENDING (+9 more)

### Community 11 - "ChatToolbar.kt"
Cohesion: 0.26
Nodes (12): ColorWheel(), Color, Modifier, AdvancedColorPickerDialog(), ChatSettingsDialogEnhanced(), ChatSettingsPreviewRefined(), Color, ImageVector (+4 more)

### Community 12 - "SelectInfoScreen.kt"
Cohesion: 0.20
Nodes (17): Result, validateServerSession(), RealTimeLoadingData, refreshData(), AnimatedHeader(), GroupedShipList(), Color, Modifier (+9 more)

### Community 14 - "CargoDetailsScreen.kt"
Cohesion: 0.33
Nodes (10): CargoDetailsScreen(), confirmCargo(), FloatingActionButtonItem(), handleCargoConfirmation(), Color, ImageVector, NavController, Result (+2 more)

### Community 15 - "QuotaPercentageDialogSection.kt"
Cohesion: 0.18
Nodes (20): CalculationResult, QuotaPercentageData, AnimatedNumber(), DialogHeader(), Color, ImageVector, Modifier, lerp() (+12 more)

### Community 16 - "Executive Summary"
Cohesion: 0.12
Nodes (15): Deep Code Audit Report, Executive Summary, Final Recommendations, **Overall: 5.2 / 10**, Overall Score, Production Readiness, Technical Debt, Top 20 Priority List (+7 more)

### Community 17 - "Cargo"
Cohesion: 0.32
Nodes (14): Cargo, CargoDetailsDialog(), Color, ImageVector, Modifier, ModernCargoDetailsGrid(), ModernCargoStatusSection(), ModernCargoStatusTimeline() (+6 more)

### Community 18 - "ApiServiceV2"
Cohesion: 0.08
Nodes (14): ApiServiceV2, CargoDeleteResponse, CargoInfoRequest, CargoInfoResponse, CargoInfoSearch, CargoSearchResponse, CargoStats, CheckExistenceResponse (+6 more)

### Community 19 - "quota_details/QuotaCardComponents.kt"
Cohesion: 0.16
Nodes (21): ActionButtonTest, QuotaEditData, DeleteQuotaDialog(), androidx, Color, QuotaConfirmDialog(), ToggleQuotaStatusDialog(), ActionButton() (+13 more)

### Community 21 - "ApiV2Routes"
Cohesion: 0.09
Nodes (5): ApiResponse2, ApiV2Routes, RealTimeDataResponse, ShipsData, JsonElement

### Community 22 - "HomeScreen.kt"
Cohesion: 0.23
Nodes (13): MenuItem, getMenuItemsForUserType(), CategorizedMenuGrid(), CategoryHeader(), CompactMenuCard(), Header(), HomeScreen(), isWideCard() (+5 more)

### Community 24 - "LoginScreen.kt"
Cohesion: 0.26
Nodes (15): CompactLoginLayout(), ExpandedLoginLayout(), IndustrialBrandingHeaderContent(), IndustrialFeatureBadge(), IndustrialFooterInfo(), IndustrialInputField(), IndustrialLoginFormCard(), androidx (+7 more)

### Community 26 - "MessageType"
Cohesion: 0.17
Nodes (14): Modifier, StatusSnackbar(), MessageType, ERROR, SUCCESS, WARNING, CargoSnackbarQueue, StateFlow (+6 more)

### Community 27 - "MicroCache"
Cohesion: 0.08
Nodes (6): writePermissionsFileAtomic(), MicroCache, AppApiCacheKeys, AuditLogger, PermissionService, QuotaService

### Community 28 - "CameraSection.kt"
Cohesion: 0.17
Nodes (13): EnhancedNumberAnalyzer, extractNumber(), ImageProxy, InputImage, recognizeTextFromImage(), EnhancedCameraPreview(), ImageCapture, Color (+5 more)

### Community 29 - "QuotaManagementContent.kt"
Cohesion: 0.15
Nodes (29): QuotaItem, AnalyticsStatChipMini(), CompactStatChip(), InfoChip(), IntegratedQuotaCard(), Color, ImageVector, Modifier (+21 more)

### Community 30 - "Color.kt"
Cohesion: 0.19
Nodes (7): CompactStatChipTest, ServerSyncingScreen(), Intent, MainActivity, StartupErrorScreen(), Bundle, ComponentActivity

### Community 31 - "CargoViewModel"
Cohesion: 0.12
Nodes (3): SaveOrUpdateResponse, CargoViewModel, message

### Community 32 - "ApiException"
Cohesion: 0.19
Nodes (4): Exception, ApiException, ConflictException, DatabaseException

### Community 33 - "AuthModels.kt"
Cohesion: 0.15
Nodes (9): ActiveSessionResponse, ForceLogoutResponse, LogoutRequest, LogoutResponse, PasswordCheckResponse, PermissionSyncRequest, PermissionSyncResponse, Result (+1 more)

### Community 34 - "ReportsRepository"
Cohesion: 0.06
Nodes (38): ComprehensiveAnalysisResponse, FilteredSummary, QuotaDetails, QuotaStatusResponse, VoucherDetail, Warehouse, ErrorResponse, HttpStatusException (+30 more)

### Community 35 - "UserManagementDialogsSection.kt"
Cohesion: 0.36
Nodes (11): CreateUserRequest, UserTypeInfo, DesignTextField(), EnhancedAddUserDialog(), EnhancedEditUserDialog(), getUserTypes(), androidx, KeyboardOptions (+3 more)

### Community 36 - "UsersManager"
Cohesion: 0.07
Nodes (7): ApiManager, CONFIG, ModalManager, ThemeManager, UIManager, UsersManager, Utils

### Community 37 - "AppModule.kt"
Cohesion: 0.16
Nodes (9): AtkCargoApplication, AuthRepository, ConflictSession, Error, Flow, LoginResult, Success, LoginUseCase (+1 more)

### Community 38 - "CargoRepository"
Cohesion: 0.06
Nodes (3): CargoController, CargoRepository, CargoService

### Community 39 - "ReportsViewModel"
Cohesion: 0.07
Nodes (12): ComprehensiveAnalytics, QuotaSortingMode, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC, QuotaManagementDialog(), QuotaManagementHeaderCard(), AndroidViewModel, Color (+4 more)

### Community 40 - "Logger"
Cohesion: 0.13
Nodes (3): UtilityController, Logger, self

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
Cohesion: 0.15
Nodes (19): DeleteUserRequest, ForceLogoutRequest, User, EnhancedDeleteConfirmationDialog(), EnhancedForceLogoutDialog(), DashboardSummaryRow(), EmptyStateView(), EnterpriseUserCard() (+11 more)

### Community 45 - "formatNumber"
Cohesion: 0.19
Nodes (22): QuotaCompletionData, QuotaGroupingMode, BY_CARGO_OWNER, BY_CARRIER, BY_SHIP, buildQuotaGroups(), QuotaGroup, formatNumber() (+14 more)

### Community 46 - "FontWeight"
Cohesion: 0.18
Nodes (9): ConfirmationDialog(), ImageVector, EmptyState(), ImageVector, Modifier, AnimatedCounter(), ExitStatusDialog(), ImageVector (+1 more)

### Community 47 - "ChatScreen.kt"
Cohesion: 0.26
Nodes (13): ChatUiItem, getAdaptiveBubbleColor(), getChatBackgroundColor(), getDateHeaderColor(), Header, Color, Message, ChatScreen() (+5 more)

### Community 48 - "CargoInfoDetailsDialogSection.kt"
Cohesion: 0.38
Nodes (11): CargoDetailsPalette, CargoDetailTabItem(), CargoInfoDetailsDialog(), DetailInfoRow(), ImageVector, Modifier, SnackbarHostState, MainInfoTabContent() (+3 more)

### Community 49 - "ActiveShipInfo"
Cohesion: 0.34
Nodes (11): ActiveShipInfo, GroupedShipList(), Color, QuotaCard(), ShipGroup(), ShipHeader(), WarehouseCard(), WarehouseList() (+3 more)

### Community 50 - "MessageBubble.kt"
Cohesion: 0.27
Nodes (13): extractShipInfoAndText(), com, replaceUsernamesWithFullNames(), ShipInfoModel, ChatInfoRow(), EditMessageDialog(), androidx, Color (+5 more)

### Community 53 - "Performance Audit"
Cohesion: 0.20
Nodes (10): [HIGH] `permissions.json` در هر بررسی مجوز از دیسک خوانده و parse می‌شود, [LOW] subquery همبسته در فهرست پیام‌های چت, [MEDIUM] `updateUser` کل جدول کاربران را برای یافتن رکورد خود کاربر می‌خواند, [MEDIUM] `user_sessions` با ۱۳ ایندکس که در هر درخواست به‌روزرسانی می‌شود, [MEDIUM] درخواست‌های شبکه‌ی زائد بعد از هر ثبت حواله, [MEDIUM] نبود صفحه‌بندی در endpointهای فهرستی, Performance Audit, خلاصه‌ی اثر (+2 more)

### Community 54 - "ShipDetailsScreen.kt"
Cohesion: 0.14
Nodes (25): WarningStatus, formatWeightWithDetail(), calculateWarningStatus(), ErrorStateCard(), ImageVector, ShipDetails(), ShipHeaderCard(), TabItem() (+17 more)

### Community 55 - "Kotlin Audit"
Cohesion: 0.22
Nodes (9): [HIGH] باگ منطقی: مسیر پاک‌سازی نشست منقضی در `PermissionPoller` هرگز اجرا نمی‌شود, Kotlin Audit, [LOW] `Strictness.LENIENT` در Gson, [MEDIUM] `runBlocking` داخل `Authenticator` روی thread شبکه‌ی OkHttp, [MEDIUM] بسته‌بندی مکرر استثنا که نوع و stack trace را نابود می‌کند, [MEDIUM] بلعیدن خطا و بازگرداندن نتیجه‌ی خالی در جستجوها, [MEDIUM] پیاده‌سازی دوگانه‌ی تبدیل تاریخ جلالی, نکات مثبت (+1 more)

### Community 57 - "ManageReportsScreen.kt"
Cohesion: 0.05
Nodes (59): ApiQuotaDetails, adjustColorForTheme(), FabItem, Color, toTon(), DateClickableField(), DateRangePicker(), getDaysInPersianMonth() (+51 more)

### Community 58 - "CrashReporter.kt"
Cohesion: 0.46
Nodes (4): CrashReporter, Context, CoroutineScope, PendingCrashReport

### Community 59 - "AuthRepositoryImpl"
Cohesion: 0.21
Nodes (6): LoginRequest, LoginResponse, SessionCheckRequest, SessionResponse, AuthRepositoryImpl, Flow

### Community 60 - "ReportModels.kt"
Cohesion: 0.12
Nodes (13): adjustColorForTheme(), AnalyticsData, ColorSelector, DateInfo, ExistingQuota, ExitDateInfo, GroupSortingMode, ALPHABETICAL (+5 more)

### Community 61 - "PermissionPoller"
Cohesion: 0.36
Nodes (3): Job, StateFlow, PermissionPoller

### Community 62 - "ActiveQuotasDialogSection.kt"
Cohesion: 0.29
Nodes (11): ViewMode, FLAT, GROUPED, ActiveQuotasDialog(), FilterBar(), Color, Dp, Modifier (+3 more)

### Community 63 - "CargoViewModel.kt"
Cohesion: 0.12
Nodes (13): CargoConfirmStatus, AWAITING_CONFIRMATION, CONFIRMED, PENDING, CargoStatus, ENTERED, EXITED, CargoUiState (+5 more)

### Community 64 - "UpdateDialog.kt"
Cohesion: 0.33
Nodes (11): DownloadActionButtons(), DownloadingState(), DownloadProgressInfo(), DownloadSizeInfo(), DownloadSpeedInfo(), ErrorState(), ModernUpdateActionSection(), ModernUpdateContent() (+3 more)

### Community 65 - "ComprehensiveAnalyticsDialog.kt"
Cohesion: 0.57
Nodes (6): AnalyticsDateNavigation(), AnalyticsHeaderCard(), ComprehensiveAnalyticsDialog(), EmptyStateCard(), ErrorStateCard(), Modifier

### Community 66 - "CryptoManager"
Cohesion: 0.33
Nodes (4): CryptoManager, ByteArray, Cipher, SecretKey

### Community 67 - "AuthViewModel"
Cohesion: 0.27
Nodes (4): AuthViewModel, StateFlow, ViewModel, LoginFormState

### Community 69 - "Android Audit"
Cohesion: 0.29
Nodes (7): Android Audit, [LOW] `versionCode = 11` در برابر `versionName = "4.0.1"`, [MEDIUM] `targetSdk = 34` در حالی که `compileSdk = 36` است, [MEDIUM] `viewBinding = true` در یک اپ کاملاً Compose, [MEDIUM] وابستگی‌های بلااستفاده در گراف build, نکات مثبت (تأییدشده), یافته‌ها

### Community 70 - "Code Quality"
Cohesion: 0.29
Nodes (7): Code Quality, Code Smells شناسایی‌شده, [HIGH] Primitive Obsession روی مقادیر وزن و تناژ, [MEDIUM] DTO و مدل دامنه تفکیک نشده‌اند, [MEDIUM] Magic Numbers بدون نام, [MEDIUM] Magic Strings برای وضعیت حواله, مهم‌ترین‌ها با جزئیات

### Community 71 - "Database Audit"
Cohesion: 0.29
Nodes (7): Database Audit, [LOW] `SET SESSION sql_mode = 'NO_ENGINE_SUBSTITUTION'` حالت strict را خاموش می‌کند, [MEDIUM] `schema.sql` قدیمی و ناقص است, [MEDIUM] هیچ FOREIGN KEY در کل schema وجود ندارد, ساختار (از `schema.sql` — ✅ به‌روزرسانی شد در Phase 2.3، اکنون کامل), نکات مثبت, یافته‌ها

### Community 72 - "SplashScreen.kt"
Cohesion: 0.27
Nodes (8): AnimatedBrandTitle(), SplashScreen(), SplashScreenConstants, SplashScreenContent(), SplashSubtitleBadge(), SplashVersionBadge(), VazirmatnFontFamily, FontFamily

### Community 73 - "MainScreen.kt"
Cohesion: 0.21
Nodes (13): MainScreen(), RouteTransitions, standardTransitions(), LoginScreen(), CargoRegistrationRoute, cargoRegistrationScreen(), NavController, navigateToCargoRegistration() (+5 more)

### Community 75 - "Jetpack Compose Audit"
Cohesion: 0.29
Nodes (7): [HIGH] God Composable — فایل‌های نمایشی با بیش از ۱۰۰۰ خط, [HIGH] صفر مورد `collectAsStateWithLifecycle` در برابر ۸۵ مورد `collectAsState`, Jetpack Compose Audit, [LOW] تمام رشته‌های UI به‌صورت literal در کد, [MEDIUM] استفاده‌ی کم از `derivedStateOf` در جاهایی که واقعاً لازم است, [MEDIUM] نبود `UiState` واحد — ۲۰+ `StateFlow` مجزا در هر ViewModel, [MEDIUM] ۲۲ مورد از ۳۵ فراخوانی `items(...)` بدون `key`

### Community 76 - "RegisterCargoScreen.kt"
Cohesion: 0.46
Nodes (7): DuplicateTrackingNumbersDialog(), NetWeightDialog(), FormSection(), ErrorHandlingCargoInfoRow(), RegisterCargoScreen(), RegisterPalette, rememberRegisterPalette()

### Community 77 - "PHP Backend Audit"
Cohesion: 0.29
Nodes (7): [HIGH] `migrations/` خالی است و `schema.sql` ناقص — جدول `audit_log` هرگز ساخته نمی‌شود, [LOW] `Config::$settings['session_timeout']` و `admin_password_hash` بلااستفاده‌اند, [MEDIUM] `execute()` در پروکسی فقط `Exception` را می‌گیرد، نه `Throwable`, [MEDIUM] `flushLog` پروکسی وقتی هدف `exit` می‌کند اجرا نمی‌شود, PHP Backend Audit, نکات مثبت, یافته‌ها

### Community 78 - "ChatRepository"
Cohesion: 0.11
Nodes (11): ChatMessage, ChatMessagesResponse, SendMessageRequest, SendMessageResponse, UnreadCountResponse, DeleteMessageRequest, EditMessageRequest, ChatRepository (+3 more)

### Community 79 - "CargoDetailsComponents.kt"
Cohesion: 0.17
Nodes (17): QuotaValidationResult, QuotaInfo, CargoInfoCard(), CargoListSection(), GroupStats(), InfoChips(), InfoGrid(), InfoGridCard() (+9 more)

### Community 80 - "Logging & Observability"
Cohesion: 0.29
Nodes (7): [HIGH] پوشه‌های لاگ ممکن است از طریق وب قابل دسترس باشند, Logging & Observability, [LOW] ~۹۰ فراخوانی `Log.*` در کد تولید, [MEDIUM] نبود مانیتورینگ و هشدار, آنچه لاگ می‌شود, نکات مثبت, یافته‌ها

### Community 81 - "CargoInfo"
Cohesion: 0.11
Nodes (31): CargoInfo, InitialInfo, toDomain(), toDto(), toDomain(), toDto(), SubmitCargoUseCase, CargoChangesPreview() (+23 more)

### Community 83 - "InitialInfoScreen"
Cohesion: 0.22
Nodes (18): CheckExistenceRequest, formatNumber(), isValidPersianText(), isValidQuotaNumber(), isValidShipName(), isValidWarehouseName(), isValidWeight(), ConfirmationDialog() (+10 more)

### Community 84 - "ColorScheme.kt"
Cohesion: 0.80
Nodes (5): ColorScheme, buildAppDarkColorScheme(), buildAppLightColorScheme(), compositeOver(), Color

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
Cohesion: 0.13
Nodes (23): Quota, WarehouseQuotaGroupingMode, BY_CARGO_OWNER, BY_SHIPPING_COMPANY, BY_WAREHOUSE, buildQuotasShareText(), format(), Context (+15 more)

### Community 89 - "update.md"
Cohesion: 0.50
Nodes (3): For --cluster-only, For --update (incremental re-extraction), graphify reference: incremental update and cluster-only

### Community 90 - "RealTimeLoadingBottomSheet"
Cohesion: 0.20
Nodes (15): ShiftInfo, DialogHeader(), Color, Modifier, RealTimeLoadingBottomSheet(), RealTimeShiftNavigation(), RefreshOverlay(), ShipCard() (+7 more)

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

### Community 98 - "Ship"
Cohesion: 0.11
Nodes (23): ErrorState(), ImageVector, Modifier, Ship, ShipSortingMode, LOADED_TONNAGE_ASC, LOADED_TONNAGE_DESC, NAME_ASC (+15 more)

### Community 99 - "RetrofitClient"
Cohesion: 0.27
Nodes (8): FloatTypeAdapter, Context, OkHttpClient, RetrofitClient, JsonReader, JsonWriter, Retrofit, TypeAdapter

### Community 100 - "QuotaSelectionDialog.kt"
Cohesion: 0.42
Nodes (10): MatchingQuota, DialogHeader(), findDifferentFields(), InactiveIndicator(), ImageVector, QuotaDetailItem(), QuotaDetails(), QuotaHeader() (+2 more)

### Community 101 - "preprocessImage"
Cohesion: 0.50
Nodes (8): adaptiveThresholding(), applyContrastEnhancement(), enhanceEdges(), ImageProxy, InputImage, medianFilter(), preprocessImage(), IntArray

### Community 102 - "Memory Audit"
Cohesion: 0.40
Nodes (5): [LOW] `ChatNotificationWorker` و چرخه‌ی حیات دیتابیس, [MEDIUM] فهرست‌های بدون سقف در `StateFlow`, Memory Audit, وضعیت کلی: **۷/۱۰ — بهترین حوزه‌ی پروژه**, یافته‌ها

### Community 103 - "Prioritized Action Plan"
Cohesion: 0.40
Nodes (5): Phase 1 — Immediate (این هفته — بازدارنده‌های تولید), Phase 2 — High Priority (۲ تا ۴ هفته), Phase 3 — Medium Priority (۱ تا ۳ ماه), Phase 4 — Optimization (۳ تا ۶ ماه), Prioritized Action Plan

### Community 104 - "AuthenticatesRequests.php"
Cohesion: 0.80
Nodes (4): enforceMinAppVersion(), requireAuthenticatedSession(), requirePermission(), sendAuthErrorResponse()

### Community 105 - "ChatViewModel"
Cohesion: 0.10
Nodes (7): ChatViewModel, ChatViewModelFactory, com, StateFlow, T, ViewModel, ViewModelProvider

### Community 106 - "ProfileSettingsDialogSection.kt"
Cohesion: 0.33
Nodes (3): ApiResponse, UpdateUserRequest, ProfileSettingsDialog()

### Community 107 - "jdate"
Cohesion: 0.53
Nodes (9): gregorian_to_jalali(), jalali_to_gregorian(), jcheckdate(), jdate(), jdate_words(), jgetdate(), jmktime(), jstrftime() (+1 more)

### Community 108 - "DownloadState"
Cohesion: 0.29
Nodes (6): Completed, Downloading, DownloadState, Error, Idle, Paused

### Community 109 - "ProfileMenu.kt"
Cohesion: 0.52
Nodes (6): ActionButtons(), getUserTypeDisplay(), Color, ProfileMenu(), ProfilePalette, rememberProfilePalette()

### Community 110 - "QuotaEntryDialog.kt"
Cohesion: 0.57
Nodes (7): FocusRequester, QuotaEntryActions(), QuotaEntryContent(), QuotaEntryDialog(), QuotaEntryPalette, rememberQuotaEntryPalette(), validateAndSubmit()

### Community 113 - "UpdateManager"
Cohesion: 0.13
Nodes (7): UpdateInfo, DownloadProgress, Context, Job, StateFlow, UpdateManager, VersionCheckResult

### Community 114 - "rememberAdaptiveLayoutConfig"
Cohesion: 0.33
Nodes (6): AdaptiveLayoutConfig, rememberAdaptiveLayoutConfig(), WindowSizeClass, COMPACT, EXPANDED, MEDIUM

### Community 115 - "CargoCounterOperationScreen"
Cohesion: 0.36
Nodes (6): CargoCounterRoute, cargoCounterScreen(), NavController, navigateToCargoCounter(), CargoCounterOperationScreen(), NavController

### Community 116 - "LoginUiState"
Cohesion: 0.40
Nodes (5): Error, Idle, Loading, LoginUiState, Success

### Community 117 - "CargoEntryNavigation.kt"
Cohesion: 0.25
Nodes (9): InitialInfoRoute, initialInfoScreen(), NavController, navigateToInitialInfo(), navigateToSelectInfo(), SelectInfoRoute, selectInfoScreen(), CargoOperationScreen() (+1 more)

### Community 118 - "ChatNavigation.kt"
Cohesion: 0.50
Nodes (4): AdminChatRoute, adminChatScreen(), NavController, navigateToAdminChat()

### Community 120 - "TokenAuthenticator"
Cohesion: 0.36
Nodes (4): TokenAuthenticator, AuthErrorBody, Authenticator, Route

### Community 121 - "ChatNotificationWorker.kt"
Cohesion: 0.48
Nodes (4): ChatNotificationWorker, KoinComponent, CoroutineWorker, ListenableWorker

### Community 122 - "What You Must Do When Invoked"
Cohesion: 0.07
Nodes (26): For /graphify add and --watch, For /graphify query, For the commit hook and native CLAUDE.md integration, For --update and --cluster-only, /graphify, Honesty Rules, Interpreter guard for subcommands, Part A - Structural extraction for code files (+18 more)

### Community 123 - "graphify reference: query, path, explain"
Cohesion: 0.40
Nodes (5): For /graphify explain, For /graphify path, graphify reference: query, path, explain, Step 0 — Constrained query expansion (REQUIRED before traversal), Step 1 — Traversal

### Community 124 - "ReportsNavigation.kt"
Cohesion: 0.38
Nodes (6): CargoDetailsRoute, NavController, ManageShipsRoute, manageShipsScreen(), navigateToCargoDetails(), navigateToManageShips()

### Community 128 - "InitialInfoFormComponents.kt"
Cohesion: 0.60
Nodes (5): CustomInput(), ImageVector, KeyboardActions, KeyboardOptions, SummaryCard()

### Community 129 - "HomeNotificationSettingRow.kt"
Cohesion: 0.67
Nodes (5): CompactSwitch(), Color, ImageVector, Modifier, NotificationSettingRow()

### Community 130 - "Architecture Overview"
Cohesion: 0.50
Nodes (4): Architecture Overview, جریان طراحی‌شده ولی بلااستفاده, جریان واقعی داده (آنچه امروز اجرا می‌شود), نقاط مرزی پرریسک بین لایه‌ها

### Community 131 - "Error Handling"
Cohesion: 0.50
Nodes (4): Error Handling, [MEDIUM] نبود مکانیزم مرکزی گزارش خطا (Crash Reporting), نکات مثبت, یافته‌های تجمیعی

### Community 144 - "UserPreferencesManager"
Cohesion: 0.09
Nodes (7): Flow, T, UserPreferencesManager, ThemeColorOption, ThemeColorPickerRow(), DataStore, Preferences

### Community 145 - "SecurityVerifier"
Cohesion: 0.10
Nodes (11): Secrets, SecurityErrorType, LICENSE_INACTIVE, LICENSE_NOT_FOUND, NETWORK_ERROR, TAMPERED, UNKNOWN_ERROR, SecurityVerifier (+3 more)

### Community 168 - "InputValidator"
Cohesion: 0.08
Nodes (3): AuthController, InputValidator, InputValidatorTest

### Community 178 - "PHP/composer.json"
Cohesion: 0.09
Nodes (21): autoload, autoload-dev, psr-4, psr-4, config, optimize-autoloader, sort-packages, description (+13 more)

### Community 221 - "secrets.cpp"
Cohesion: 0.34
Nodes (16): decryptXor(), JNI_OnLoad(), n0(), n1(), n2(), n3(), n4(), n5() (+8 more)

### Community 253 - "ShipInfoSection.kt"
Cohesion: 0.28
Nodes (13): ShipInfo, formatNumber(), toEnglishNumbers(), DetailedInfoGrid(), DetailInfoItem(), ExpandedContent(), Color, ImageVector (+5 more)

### Community 272 - "Request"
Cohesion: 0.11
Nodes (4): DiagnosticsController, OnlineUsersController, MinVersionGate, Request

### Community 277 - "TokenRefresher.kt"
Cohesion: 0.50
Nodes (3): OkHttpClient, TokenRefresher, RefreshTokenResponse

### Community 319 - "graphify reference: extra exports and benchmark"
Cohesion: 0.22
Nodes (8): graphify reference: extra exports and benchmark, Step 6b - Wiki (only if --wiki flag), Step 7 - Neo4j export (only if --neo4j or --neo4j-push flag), Step 7a - FalkorDB export (only if --falkordb or --falkordb-push flag), Step 7b - SVG export (only if --svg flag), Step 7c - GraphML export (only if --graphml flag), Step 7d - MCP server (only if --mcp flag), Step 8 - Token reduction benchmark (only if total_words > 5000)

### Community 340 - "BootReceiver.kt"
Cohesion: 0.48
Nodes (5): BootReceiver, BroadcastReceiver, Context, Intent, KoinComponent

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
- **297 isolated node(s):** `CONFIG`, `Utils`, `name`, `description`, `type` (+292 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **39 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `UserPreferencesManager` connect `UserPreferencesManager` to `StartupViewModel`, `CargoCounterScreen.kt`, `LoadingNotificationService`, `SelectInfoScreen.kt`, `CargoDetailsScreen.kt`, `TokenRefresher.kt`, `Color.kt`, `AuthModels.kt`, `AppModule.kt`, `UserManagementScreen.kt`, `ChatScreen.kt`, `ManageReportsScreen.kt`, `AuthRepositoryImpl`, `CargoViewModel.kt`, `MainScreen.kt`, `BootReceiver.kt`, `RetrofitClient`, `ProfileMenu.kt`, `ChatNavigation.kt`, `ChatNotificationWorker.kt`?**
  _High betweenness centrality (0.061) - this node is a cross-community bridge._
- **Why does `ReportsViewModel` connect `ReportsViewModel` to `ComprehensiveAnalyticsDialog.kt`, `Ship`, `ReportsRepository`, `ExportPdfUseCase`, `AppModule.kt`, `LoadingState`, `MainScreen.kt`, `formatNumber`, `ReportModels.kt`, `CargoInfo`, `ShipDetailsScreen.kt`, `Quota`, `ManageReportsScreen.kt`, `RealTimeLoadingBottomSheet`, `ReportsNavigation.kt`, `QuotaManagementContent.kt`?**
  _High betweenness centrality (0.037) - this node is a cross-community bridge._
- **Why does `ApiServiceV2` connect `ApiServiceV2` to `AuthModels.kt`, `ReportsRepository`, `Ship`, `RetrofitClient`, `ProfileSettingsDialogSection.kt`, `UserManagementScreen.kt`, `ChatRepository`, `CargoDetailsComponents.kt`, `ActiveShipInfo`, `QuotaManagementContent.kt`, `CargoInfo`, `ApiV2Routes`, `Quota`, `AuthRepositoryImpl`, `QuotaExistenceMultipleResponse`, `CargoViewModel.kt`?**
  _High betweenness centrality (0.031) - this node is a cross-community bridge._
- **What connects `CONFIG`, `Utils`, `name` to the rest of the system?**
  _297 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `StartupViewModel` be split into smaller, more focused modules?**
  _Cohesion score 0.10227272727272728 - nodes in this community are weakly interconnected._
- **Should `Security Audit` be split into smaller, more focused modules?**
  _Cohesion score 0.08695652173913043 - nodes in this community are weakly interconnected._
- **Should `CargoCounterScreen.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.14532019704433496 - nodes in this community are weakly interconnected._