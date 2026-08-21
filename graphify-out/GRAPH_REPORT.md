# Graph Report - ATK-Cargo  (2026-08-21)

## Corpus Check
- 339 files · ~386,727 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2903 nodes · 5746 edges · 245 communities (168 shown, 77 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 200 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `a52b5d63`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- StartupViewModel
- LicenseRepository
- LicenseAdminServiceTest
- MainScreen.kt
- TextAlign
- ShipSortingMode
- ChatMessageEntity
- app.js
- ReportsRepository
- UserRepository
- QuotaWarningDialog.kt
- CrashReporter.kt
- ReportsViewModel
- JalaliDateUtilsTest
- ApiServiceV2
- _guard.php
- ChatViewModel
- High Issues
- PermissionRepository
- Deep Code Audit Report
- SessionService
- CargoDetailsComponents.kt
- AuthModels.kt
- QuotaManagementContent.kt
- ATKCargoTheme
- SessionRepository
- CargoViewModel.kt
- TokenAuthenticator
- CargoViewModelTest
- ApiException
- SessionServiceTest
- Context
- CrashReportRateLimiter
- UserService
- QuotaDetailsScreen.kt
- MessageBubble.kt
- UsersManager
- Cargo
- CargoRepository
- AnalyticsController
- Logger
- CargoCounterScreen.kt
- CargoViewModel
- WarehouseDetailsScreen.kt
- .onError
- SecurityScreen.kt
- QuotaPercentageDialogSection.kt
- ShipInfoSection.kt
- QuotaService
- Modifier
- InitialInfo
- LoginAttemptLimiterTest
- CameraSection.kt
- CargoDetailsScreen.kt
- ChatScreen.kt
- LoginAttemptLimiter
- CargoController
- AppError
- Secrets
- ReportModels.kt
- api_v2.php
- ManageReportsScreen.kt
- PasswordGateService
- formatNumber
- AnalyticsController.php
- SelectInfoScreen.kt
- CargoInfo
- LicenseAdminService
- UserManagementScreen.kt
- SecurityAlerter
- CargoInfoDetailsDialogSection.kt
- ApiV2Routes
- LoginScreen.kt
- RegisterCargoScreen.kt
- jdate
- CargoConfirmStatus
- AppDatabase
- MessageInputArea
- ChatRepository
- Medium Issues
- MicroCache
- DownloadState
- StartupViewModel
- استانداردسازی کامنت‌های چندخطی به تک‌خطی (کلاینت + سرور)
- LoginResult
- Intent
- AppModule.kt
- KoinComponent
- AuthViewModel
- IBinder
- preprocessImage
- PermissionPoller
- ChatPreferencesStore
- Migrations
- AtkCargoApplication.kt
- Router
- پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13)
- Database
- graphify reference: query, path, explain
- FontWeight
- Service
- وضعیت کلی: خوب
- T
- Code Smells شناسایی‌شده
- MenuItem
- HardwarePerformanceEvaluator
- نقاط قوت
- Architecture Overview
- DataModel.kt
- Executive Summary
- SecurityErrorType
- PDO
- AppNotificationManager
- Quota
- چک‌لیست فعال‌سازی `STRICT_TRANS_TABLES` (DEEP_CODE_AUDIT.md #Phase4.10)
- What You Must Do When Invoked
- self
- Final Recommendations
- ComponentDefaults.kt
- CargoEditSearchDialogsSection.kt
- نقاط قوت
- DatabaseManager
- Logger
- حذف/محدودسازی phpMyAdmin روی سرور production (Phase 1, آیتم #۲)
- CargoCounterListContent.kt
- androidx
- ShipDetailsScreen.kt
- NavController
- Authentication & Authorization
- وضعیت کلی
- Performance Audit
- نقاط قوت
- چه چیزی بد است
- ImageVector
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
- QuotaSelectionDialog.kt
- ComprehensiveAnalyticsDialog.kt
- وضعیت
- TokenRefresherTest
- UserManagementDialogsSection.kt
- ActiveQuotasGroupedComponents.kt
- AndroidViewModel
- Flow
- Project Overview
- InputValidator
- StateFlow
- self
- UserSettingsStore
- LoadingState
- LoginScreen
- QuotaValidationUseCase.kt
- QuotaEntryDialog.kt
- LicenseController
- ApiResponse
- PHP/composer.json
- com
- NavController
- StateFlow
- ViewModel
- Modifier
- ColorPicker.kt
- Context
- OkHttpClient
- ProfileMenu.kt
- ColorScheme.kt
- HomeNotificationSettingRow.kt
- QuotaCalculatorTest
- RealTimeLoadingCardSection.kt
- Color.kt
- SearchBar.kt
- CargoCounterOperationScreen
- UpdateManager
- HomeScreen.kt
- secrets.cpp
- SessionManager
- SessionManager
- MessageType
- Request
- AuthController
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
1. `ReportsViewModel` - 91 edges
2. `CargoViewModel` - 73 edges
3. `InputValidator` - 58 edges
4. `ApiServiceV2` - 57 edges
5. `formatNumber()` - 38 edges
6. `UserPreferencesManager` - 36 edges
7. `SessionRepository` - 35 edges
8. `ReportsRepository` - 33 edges
9. `Deep Code Audit Report` - 31 edges
10. `LicenseAdminServiceTest` - 31 edges

## Surprising Connections (you probably didn't know these)
- `RegisterCargoScreen()` --calls--> `ShipInfo`  [INFERRED]
  feature/cargo-workflow/src/main/java/com/atk/atk_cargo/feature/cargo_registration/presentation/RegisterCargoScreen.kt → core/domain/src/main/java/com/atk/atk_cargo/domain/model/ShipInfo.kt
- `CargoCounterScreen()` --calls--> `validateServerSession()`  [INFERRED]
  feature/cargo-workflow/src/main/java/com/atk/atk_cargo/feature/cargo_counter/presentation/CargoCounterScreen.kt → core/network/src/main/java/com/atk/atk_cargo/api/SessionValidator.kt
- `PersianDatePickerDialog()` --calls--> `FilterChip()`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/core/ui/components/DateRangePicker.kt → feature/cargo-workflow/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/ActiveQuotasContent.kt
- `calculateWarningStatus()` --calls--> `WarningStatus`  [EXTRACTED]
  feature/reports/src/main/java/com/atk/atk_cargo/feature/reports/presentation/details/ShipDetailsScreen.kt → core/network/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt
- `ReportsViewModel` --references--> `GroupSortingMode`  [EXTRACTED]
  feature/reports/src/main/java/com/atk/atk_cargo/ui/viewmodel/ReportsViewModel.kt → core/network/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt

## Import Cycles
- None detected.

## Communities (245 total, 77 thin omitted)

### Community 0 - "StartupViewModel"
Cohesion: 0.05
Nodes (43): AndroidViewModel, Intent, NativeLibraryUnavailable, Ready, RequestBatteryOptimization, SecurityBlocked, SecurityCheckState, ShowMessage (+35 more)

### Community 3 - "MainScreen.kt"
Cohesion: 0.05
Nodes (54): MainScreen(), RouteTransitions, standardTransitions(), CheckExistenceRequest, CargoCounterRoute, CargoDetailsRoute, cargoDetailsScreen(), NavController (+46 more)

### Community 4 - "TextAlign"
Cohesion: 0.25
Nodes (20): DeleteDialog(), DialogBadge(), DialogButtonRow(), DialogContentCard(), DialogMessageText(), DialogTitle(), Color, ImageVector (+12 more)

### Community 5 - "ShipSortingMode"
Cohesion: 0.11
Nodes (22): ErrorState(), ImageVector, Modifier, ShipSortingMode, LOADED_TONNAGE_ASC, LOADED_TONNAGE_DESC, NAME_ASC, NAME_DESC (+14 more)

### Community 6 - "ChatMessageEntity"
Cohesion: 0.15
Nodes (3): ChatDao, Flow, ChatMessageEntity

### Community 7 - "app.js"
Cohesion: 0.15
Nodes (25): buildRow(), cell(), confirmAction(), cleanup(), onClose(), onOk(), copyKey(), currentTheme() (+17 more)

### Community 8 - "ReportsRepository"
Cohesion: 0.11
Nodes (9): ComprehensiveAnalysisResponse, QuotaDetails, RealTimeDataResponse, Warehouse, ErrorResponse, HttpStatusException, Exception, Result (+1 more)

### Community 10 - "QuotaWarningDialog.kt"
Cohesion: 0.17
Nodes (18): WarningStatus, format(), formatWeightWithDetail(), CompactInfoSection(), DialogHeader(), Color, ImageVector, Modifier (+10 more)

### Community 11 - "CrashReporter.kt"
Cohesion: 0.52
Nodes (3): CrashReporter, Context, PendingCrashReport

### Community 12 - "ReportsViewModel"
Cohesion: 0.06
Nodes (19): ComprehensiveAnalytics, QuotaCompletionData, QuotaGroupingMode, BY_CARGO_OWNER, BY_CARRIER, BY_SHIP, QuotaSortingMode, REMAINING_TONNAGE_ASC (+11 more)

### Community 14 - "ApiServiceV2"
Cohesion: 0.07
Nodes (18): ApiResponse2, ApiServiceV2, User, CargoDeleteResponse, CargoInfoSearch, CargoSearchResponse, CargoStats, CheckExistenceResponse (+10 more)

### Community 15 - "_guard.php"
Cohesion: 0.10
Nodes (8): lic_destroy_session(), lic_idle_timeout(), lic_is_authenticated(), lic_require_auth_json(), lic_require_auth_page(), Config, self, Csrf

### Community 16 - "ChatViewModel"
Cohesion: 0.10
Nodes (7): ChatViewModel, ChatViewModelFactory, com, StateFlow, T, ViewModel, ViewModelProvider

### Community 17 - "High Issues"
Cohesion: 0.15
Nodes (13): Critical Issues, [CRITICAL] تخلیه‌ی worker pool سرور از طریق `sleep()` مسدودکننده در مسیر لاگین, [HIGH] endpoint گزارش کرش بدون auth با rate-limiter مسدودکننده, [HIGH] Foreground Service با poll دائمی، ناسازگار با سقف `dataSync` در Android 14+, High Issues, [HIGH] رازهای کلاینت با XOR تک‌بایتی «محافظت» شده‌اند, [HIGH] ~~مسیر build نسخه‌ی release هرگز در CI اجرا نمی‌شود~~ — تصحیح‌شده، سپس رفع شد, [HIGH] گیت نسخه‌ی حداقلی با حذف یک هدر دور زده می‌شود (+5 more)

### Community 18 - "PermissionRepository"
Cohesion: 0.11
Nodes (3): PermissionRepository, PermissionService, PermissionServiceTest

### Community 19 - "Deep Code Audit Report"
Cohesion: 0.09
Nodes (22): Animation Audit, Deep Code Audit Report, Dependency Audit, Final Score, [LOW] اسپلش با تأخیر ثابت به‌جای انیمیشن مبتنی بر پیشرفت, [LOW] دو استک سریال‌سازی JSON هم‌زمان, [LOW] نام مستعارهای version catalog با نسخه‌های واقعی نمی‌خوانند, Overall Score (+14 more)

### Community 21 - "CargoDetailsComponents.kt"
Cohesion: 0.22
Nodes (15): QuotaInfo, toDomain(), toDto(), CargoInfoCard(), CargoListSection(), GroupStats(), InfoChips(), InfoGrid() (+7 more)

### Community 22 - "AuthModels.kt"
Cohesion: 0.12
Nodes (11): ActiveSessionResponse, ForceLogoutResponse, LoginRequest, LoginResponse, PasswordCheckResponse, PermissionSyncRequest, PermissionSyncResponse, SessionCheckRequest (+3 more)

### Community 23 - "QuotaManagementContent.kt"
Cohesion: 0.15
Nodes (29): QuotaItem, AnalyticsStatChipMini(), CompactStatChip(), InfoChip(), IntegratedQuotaCard(), Color, ImageVector, Modifier (+21 more)

### Community 24 - "ATKCargoTheme"
Cohesion: 0.14
Nodes (11): ComponentStyles, Dimensions, Elevation, ATKCargoTheme, Motion, SemanticColors, AppShapes, Spacing (+3 more)

### Community 26 - "CargoViewModel.kt"
Cohesion: 0.10
Nodes (12): Kilograms, CargoDialog, CargoUiState, CargoViewModelFactory, DuplicateConfirmation, Duplicates, StateFlow, T (+4 more)

### Community 27 - "TokenAuthenticator"
Cohesion: 0.19
Nodes (5): Authenticator, TokenAuthenticator, AuthErrorBody, TokenAuthenticatorTest, Route

### Community 29 - "ApiException"
Cohesion: 0.22
Nodes (4): Exception, ApiException, ConflictException, DatabaseException

### Community 33 - "UserService"
Cohesion: 0.13
Nodes (5): App\Core\AuthenticatesRequests, App\Exceptions\ApiException, App\Repositories\UserRepository, UserController, UserService

### Community 34 - "QuotaDetailsScreen.kt"
Cohesion: 0.15
Nodes (10): ApiQuotaDetails, calculatePercentage(), calculateProgress(), ProgressBar(), QuotaAdditionalInfo(), QuotaDetails(), QuotaInfoCards(), QuotaMainCard() (+2 more)

### Community 35 - "MessageBubble.kt"
Cohesion: 0.27
Nodes (13): extractShipInfoAndText(), com, replaceUsernamesWithFullNames(), ShipInfoModel, ChatInfoRow(), EditMessageDialog(), androidx, Color (+5 more)

### Community 36 - "UsersManager"
Cohesion: 0.07
Nodes (7): ApiManager, CONFIG, ModalManager, ThemeManager, UIManager, UsersManager, Utils

### Community 37 - "Cargo"
Cohesion: 0.32
Nodes (14): Cargo, CargoDetailsDialog(), Color, ImageVector, Modifier, ModernCargoDetailsGrid(), ModernCargoStatusSection(), ModernCargoStatusTimeline() (+6 more)

### Community 41 - "CargoCounterScreen.kt"
Cohesion: 0.12
Nodes (19): For --cluster-only, For --update (incremental re-extraction), graphify reference: incremental update and cluster-only, com, adjustColorForTheme(), ColorSelector, Color, CargoCounterScreen() (+11 more)

### Community 42 - "CargoViewModel"
Cohesion: 0.11
Nodes (3): SaveOrUpdateResponse, CargoViewModel, message

### Community 43 - "WarehouseDetailsScreen.kt"
Cohesion: 0.11
Nodes (34): FilteredSummary, VoucherDetail, persianDateFormat(), DateTimePicker(), Modifier, PersianDateItem(), TimePickerDialog(), EmptyVoucherList() (+26 more)

### Community 44 - ".onError"
Cohesion: 0.15
Nodes (9): CreateUserRequest, DeleteUserRequest, ForceLogoutRequest, UpdateUserRequest, ViewModel, UserManagementViewModel, ViewModel, ProfileViewModel (+1 more)

### Community 45 - "SecurityScreen.kt"
Cohesion: 0.23
Nodes (14): AdvancedOrbitalScanner(), AnimatedUpdateIcon(), AnimatedWarningIcon(), DynamicPremiumBackground(), InteractiveExitButton(), Modifier, RetryButton(), SecurityBlockScreen() (+6 more)

### Community 46 - "QuotaPercentageDialogSection.kt"
Cohesion: 0.20
Nodes (17): createTypography(), CalculationResult, AnimatedNumber(), Color, ImageVector, Modifier, lerp(), PercentageDisplay() (+9 more)

### Community 47 - "ShipInfoSection.kt"
Cohesion: 0.26
Nodes (15): ShipInfo, formatNumber(), toEnglishNumbers(), CargoInfoRow(), DetailedInfoGrid(), DetailInfoItem(), ErrorHandlingCargoInfoRow(), ExpandedContent() (+7 more)

### Community 49 - "Modifier"
Cohesion: 0.15
Nodes (23): EmptySearchResult(), FilterChip(), FilterState, ALL, COMPLETED, PENDING, FlatQuotasContent(), GroupedShipsContent() (+15 more)

### Community 50 - "InitialInfo"
Cohesion: 0.18
Nodes (5): QuotaRepository, CargoInfoResponse, InitialInfo, QuotaStatusResponse, Parcelable

### Community 52 - "CameraSection.kt"
Cohesion: 0.21
Nodes (12): EnhancedNumberAnalyzer, extractNumber(), ImageProxy, InputImage, recognizeTextFromImage(), EnhancedCameraPreview(), ImageCapture, Color (+4 more)

### Community 53 - "CargoDetailsScreen.kt"
Cohesion: 0.16
Nodes (10): Result, validateServerSession(), TokenStore, CargoDetailsScreen(), FloatingActionButtonItem(), Color, ImageVector, NavController (+2 more)

### Community 54 - "ChatScreen.kt"
Cohesion: 0.17
Nodes (22): ChatUiItem, getAdaptiveBubbleColor(), getChatBackgroundColor(), getDateHeaderColor(), Header, Color, Message, ChatScreen() (+14 more)

### Community 56 - "CargoController"
Cohesion: 0.15
Nodes (5): App\Repositories\CargoRepository, App\Services\CargoService, App\Services\PasswordGateService, Logger, CargoController

### Community 57 - "AppError"
Cohesion: 0.32
Nodes (6): AppError, Network, Server, Timeout, toAppError(), Validation

### Community 59 - "ReportModels.kt"
Cohesion: 0.15
Nodes (11): AnalyticsData, DateInfo, ExistingQuota, ExitDateInfo, GroupSortingMode, ALPHABETICAL, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC (+3 more)

### Community 61 - "ManageReportsScreen.kt"
Cohesion: 0.07
Nodes (48): DateClickableField(), DateRangePicker(), getDaysInPersianMonth(), isPersianLeapYear(), Color, Modifier, PersianDatePickerDialog(), addOneDayToPersianDate() (+40 more)

### Community 63 - "formatNumber"
Cohesion: 0.33
Nodes (15): formatNumber(), AnalyticsGroupingModeButton(), AnalyticsOwnerSummaryCard(), AnalyticsQuotaCard(), AnalyticsQuotaGroupExpansionPanel(), AnalyticsStatChip(), Context, ImageVector (+7 more)

### Community 64 - "AnalyticsController.php"
Cohesion: 0.21
Nodes (8): App\Core\Database, App\Core\MicroCache, App\Core\Response, App\Enums\CargoConfirmStatus, App\Enums\CargoStatus, App\Validators\InputValidator, PDOException, Throwable

### Community 65 - "SelectInfoScreen.kt"
Cohesion: 0.24
Nodes (17): ActiveShipInfo, RealTimeLoadingData, ShipSelectionDialog(), GroupedShipList(), Color, Modifier, NavController, Result (+9 more)

### Community 66 - "CargoInfo"
Cohesion: 0.19
Nodes (10): toDomain(), toDto(), CargoInfo, CargoSearchResultCard(), InfoRowCompact(), Color, Context, ImageVector (+2 more)

### Community 68 - "UserManagementScreen.kt"
Cohesion: 0.20
Nodes (17): androidx, EnhancedDeleteConfirmationDialog(), DashboardSummaryRow(), EmptyStateView(), EnterpriseUserCard(), getUserTypeDisplay(), Color, RoleFilterChipRow() (+9 more)

### Community 70 - "CargoInfoDetailsDialogSection.kt"
Cohesion: 0.38
Nodes (11): CargoInfoRequest, CargoDetailsPalette, CargoDetailTabItem(), CargoInfoDetailsDialog(), DetailInfoRow(), ImageVector, Modifier, MainInfoTabContent() (+3 more)

### Community 72 - "LoginScreen.kt"
Cohesion: 0.26
Nodes (16): CompactLoginLayout(), ExpandedLoginLayout(), IndustrialBrandingHeaderContent(), IndustrialFeatureBadge(), IndustrialFooterInfo(), IndustrialInputField(), IndustrialLoginFormCard(), androidx (+8 more)

### Community 73 - "RegisterCargoScreen.kt"
Cohesion: 0.29
Nodes (9): Cargo, CargoViewModel, CargoOperationScreen(), NavController, RegisterCargoScreen(), RegisterPalette, rememberRegisterPalette(), MessageType (+1 more)

### Community 74 - "jdate"
Cohesion: 0.53
Nodes (9): gregorian_to_jalali(), jalali_to_gregorian(), jcheckdate(), jdate(), jdate_words(), jgetdate(), jmktime(), jstrftime() (+1 more)

### Community 75 - "CargoConfirmStatus"
Cohesion: 0.18
Nodes (8): CargoConfirmStatus, AWAITING_CONFIRMATION, CONFIRMED, PENDING, CargoStatus, ENTERED, EXITED, FormSection()

### Community 76 - "AppDatabase"
Cohesion: 0.47
Nodes (3): AppDatabase, Context, RoomDatabase

### Community 77 - "MessageInputArea"
Cohesion: 0.52
Nodes (6): rotateIcon(), Color, com, MessageInputArea(), QuotaSelectionDialog(), ShipSelectionDialog()

### Community 78 - "ChatRepository"
Cohesion: 0.13
Nodes (9): ChatMessage, ChatMessagesResponse, SendMessageRequest, SendMessageResponse, UnreadCountResponse, ChatRepository, com, Flow (+1 more)

### Community 79 - "Medium Issues"
Cohesion: 0.20
Nodes (10): Medium Issues, [MEDIUM] تزریق به لاگ از طریق ورودی‌های کنترل‌شده توسط کاربر, [MEDIUM] تشخیص دیباگر/Frida بدون تفکیک نوع build, [MEDIUM] دستورهای `<Directory>` و `<DirectoryMatch>` در `.htaccess` نامعتبرند, [MEDIUM] دوره‌ی مهلت آفلاین امنیتی روی SharedPreferences قابل دستکاری است, [MEDIUM] شمارش نام کاربری و افشای `userType` بدون احراز هویت, [MEDIUM] شمارنده‌ی rate-limit فایل‌محور دچار race condition است, [MEDIUM] هر کاربر احرازشده می‌تواند فهرست کامل ادمین‌ها را بگیرد (+2 more)

### Community 80 - "MicroCache"
Cohesion: 0.11
Nodes (4): App\Core\DatabaseManager, MicroCache, AppApiCacheKeys, ShipService

### Community 81 - "DownloadState"
Cohesion: 0.29
Nodes (6): Completed, Downloading, DownloadState, Error, Idle, Paused

### Community 83 - "استانداردسازی کامنت‌های چندخطی به تک‌خطی (کلاینت + سرور)"
Cohesion: 0.29
Nodes (6): Context, Verification, استانداردسازی کامنت‌های چندخطی به تک‌خطی (کلاینت + سرور), دایرکتوری‌های مستثنی (اصلاً لمس نشوند), قوانین استاندارد تبدیل, نحوه‌ی اجرا

### Community 84 - "LoginResult"
Cohesion: 0.23
Nodes (7): AuthRepository, ConflictSession, Error, Flow, LoginResult, Success, LoginUseCase

### Community 86 - "AppModule.kt"
Cohesion: 0.15
Nodes (6): Flow, UserPreferencesStore, LogoutRequest, LogoutResponse, Result, LogoutUseCase

### Community 88 - "AuthViewModel"
Cohesion: 0.17
Nodes (8): AuthViewModel, Error, Idle, StateFlow, ViewModel, Loading, LoginUiState, Success

### Community 90 - "preprocessImage"
Cohesion: 0.50
Nodes (8): adaptiveThresholding(), applyContrastEnhancement(), enhanceEdges(), ImageProxy, InputImage, medianFilter(), preprocessImage(), IntArray

### Community 91 - "PermissionPoller"
Cohesion: 0.33
Nodes (4): Job, StateFlow, PermissionPoller, CoroutineScope

### Community 92 - "ChatPreferencesStore"
Cohesion: 0.31
Nodes (6): ChatPreferencesStore, Flow, AdminChatRoute, adminChatScreen(), NavController, navigateToAdminChat()

### Community 93 - "Migrations"
Cohesion: 0.33
Nodes (5): Migrations, migrationهای اعمال‌شده تاکنون, قرارداد نام‌گذاری, نحوه‌ی اجرا, وضعیت schema.sql

### Community 95 - "Router"
Cohesion: 0.14
Nodes (4): ApiAuthGate, MinVersionGate, Request, Router

### Community 96 - "پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13)"
Cohesion: 0.22
Nodes (8): ساختار فعلی (مستند شده در این جلسه), ساختار هدف, مراحل اجرا (ترتیب دقیق مهم است), معیار موفقیت, پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13), پیش‌نیازهای جانبی که باید قبل از این کار حل شوند, چرا فقط .htaccess کافی نیست, چرا لازم است

### Community 97 - "Database"
Cohesion: 0.13
Nodes (6): Config, InvalidArgumentException, mysqli, PDO, UtilityController, Database

### Community 98 - "graphify reference: query, path, explain"
Cohesion: 0.40
Nodes (5): For /graphify explain, For /graphify path, graphify reference: query, path, explain, Step 0 — Constrained query expansion (REQUIRED before traversal), Step 1 — Traversal

### Community 99 - "FontWeight"
Cohesion: 0.11
Nodes (14): ConfirmationDialog(), ImageVector, EmptyState(), ImageVector, Modifier, VazirmatnFontFamily, AnimationManager, AnimatedHeader() (+6 more)

### Community 101 - "وضعیت کلی: خوب"
Cohesion: 0.50
Nodes (4): [LOW] `applicationScope` بدون لغو در `AtkCargoApplication`, [MEDIUM] نتایج بدون سقف در سمت سرور، نه در کلاینت, Memory Audit, وضعیت کلی: خوب

### Community 103 - "Code Smells شناسایی‌شده"
Cohesion: 0.25
Nodes (8): Code Quality, Code Smells شناسایی‌شده, [HIGH] God ViewModel — `CargoViewModel`, [HIGH] نشت لایه — ViewModelها مستقیم `ApiServiceV2` را صدا می‌زنند, [LOW] اعداد و رشته‌های جادویی, [MEDIUM] `ChatRepository` مسئولیت‌های نامرتبط دارد (Feature Envy), [MEDIUM] ViewModel داخل فایل Composable, [MEDIUM] فایل‌های بسیار بزرگ در سراسر پروژه

### Community 109 - "نقاط قوت"
Cohesion: 0.25
Nodes (8): Database Audit, [HIGH] کلیدهای JOIN ناسازگار بین کوئری‌های تحلیلی و سرویس‌ها, [LOW] collation ناسازگار در جدول `Passwords`, [LOW] ایندکس‌های زائد و کم‌ارزش روی `admin_chat_messages`, [MEDIUM] `CargoInfo` هیچ کلید خارجی به `InitialInfo` ندارد, [MEDIUM] تاریخ و زمان به‌صورت `varchar(100)` ذخیره می‌شوند, [MEDIUM] نوع‌دهی ناسازگار ستون‌های وزن, نقاط قوت

### Community 110 - "Architecture Overview"
Cohesion: 0.67
Nodes (3): Architecture Overview, جریان واقعی داده (استخراج‌شده از کد), مشاهدات کلیدی

### Community 115 - "Executive Summary"
Cohesion: 0.25
Nodes (8): Executive Summary, Overall Score: **5.8 / 10**, Production Readiness, Technical Debt, مهم‌ترین ریسک‌های امنیتی, مهم‌ترین مشکلات Architecture, مهم‌ترین مشکلات Performance, وضعیت کلی

### Community 116 - "SecurityErrorType"
Cohesion: 0.33
Nodes (6): SecurityErrorType, LICENSE_INACTIVE, LICENSE_NOT_FOUND, NETWORK_ERROR, TAMPERED, UNKNOWN_ERROR

### Community 118 - "AppNotificationManager"
Cohesion: 0.05
Nodes (32): AppNotificationManager, BootReceiver, BroadcastReceiver, Context, Intent, KoinComponent, UserPreferencesManager, BroadcastReceiver (+24 more)

### Community 119 - "Quota"
Cohesion: 0.08
Nodes (44): ActionButtonTest, Quota, QuotaEditData, QuotaPercentageData, WarehouseQuotaGroupingMode, BY_CARGO_OWNER, BY_SHIPPING_COMPANY, BY_WAREHOUSE (+36 more)

### Community 120 - "چک‌لیست فعال‌سازی `STRICT_TRANS_TABLES` (DEEP_CODE_AUDIT.md #Phase4.10)"
Cohesion: 0.25
Nodes (7): ریسک واقعی کشف‌شده: بدون سقف طول روی فیلدهای متنی آزاد, مراحل امن (ترتیب مهم است), مسئله چیست, معیار موفقیت, نتایج تست عملی (Phase 5.15), چرا نمی‌شود همین الان فعالش کرد, چک‌لیست فعال‌سازی `STRICT_TRANS_TABLES` (DEEP_CODE_AUDIT.md #Phase4.10)

### Community 122 - "What You Must Do When Invoked"
Cohesion: 0.07
Nodes (26): For /graphify add and --watch, For /graphify query, For the commit hook and native CLAUDE.md integration, For --update and --cluster-only, /graphify, Honesty Rules, Interpreter guard for subcommands, Part A - Structural extraction for code files (+18 more)

### Community 124 - "Final Recommendations"
Cohesion: 0.29
Nodes (7): Final Recommendations, ۱. اول جلوی خون‌ریزی را بگیر، بعد بازسازی کن, ۲. بدهی معماری را با فیچرهای جدید بپرداز، نه با یک پروژه‌ی جداگانه, ۳. تست را از جایی شروع کن که شکستش بی‌صدا و پرهزینه است, ۴. مسیر release را در CI گیت کن — همین امروز, ۵. آنچه نباید تغییر کند, ۶. تصمیم‌های محصولی که باید گرفته شوند

### Community 128 - "CargoEditSearchDialogsSection.kt"
Cohesion: 0.27
Nodes (19): CargoInfo, CargoChangesPreview(), CargoEditConfirmDialog(), CargoEditConfirmHeader(), CargoMainInfo(), CargoShippingInfo(), CargoTimeInfo(), CargoWeightInfo() (+11 more)

### Community 129 - "نقاط قوت"
Cohesion: 0.29
Nodes (7): [LOW] PHPStan فایل‌های ریشه و پنل مدیریتی را پوشش نمی‌دهد, [MEDIUM] `AppApiController` عمدتاً یک Middle Man است, [MEDIUM] دو مسیر دسترسی به دیتابیس (PDO و mysqli) به‌صورت موازی, [MEDIUM] رمزگذاری خروجی ناسازگار بین دو مسیر نوشتن, [MEDIUM] گیت احراز هویت دوبار پیاده‌سازی شده است, PHP Backend Audit, نقاط قوت

### Community 132 - "حذف/محدودسازی phpMyAdmin روی سرور production (Phase 1, آیتم #۲)"
Cohesion: 0.40
Nodes (4): حذف/محدودسازی phpMyAdmin روی سرور production (Phase 1, آیتم #۲), راستی‌آزمایی نهایی, گزینه‌ی ۱ — حذف کامل (توصیه‌شده), گزینه‌ی ۲ — اگر حذف فوری ممکن نیست

### Community 133 - "CargoCounterListContent.kt"
Cohesion: 0.51
Nodes (10): ActiveShipInfo, GroupedShipList(), Color, QuotaCard(), ShipGroup(), ShipHeader(), TabBar(), WarehouseCard() (+2 more)

### Community 135 - "ShipDetailsScreen.kt"
Cohesion: 0.35
Nodes (9): Ship, calculateWarningStatus(), ErrorStateCard(), ImageVector, ShipDetails(), ShipHeaderCard(), TabItem(), WarehouseQuotasTabs() (+1 more)

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

### Community 144 - "UserPreferencesManager"
Cohesion: 0.05
Nodes (23): ApiServiceV2, Flow, UserPreferencesManager, CryptoManager, ByteArray, ChatPreferencesStore, Cipher, Context (+15 more)

### Community 145 - "SecurityVerifier"
Cohesion: 0.24
Nodes (4): SecurityVerifier, JSONObject, PackageInfo, Signature

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

### Community 159 - "QuotaSelectionDialog.kt"
Cohesion: 0.42
Nodes (10): MatchingQuota, DialogHeader(), findDifferentFields(), InactiveIndicator(), ImageVector, QuotaDetailItem(), QuotaDetails(), QuotaHeader() (+2 more)

### Community 160 - "ComprehensiveAnalyticsDialog.kt"
Cohesion: 0.57
Nodes (6): AnalyticsDateNavigation(), AnalyticsHeaderCard(), ComprehensiveAnalyticsDialog(), EmptyStateCard(), ErrorStateCard(), Modifier

### Community 161 - "وضعیت"
Cohesion: 0.50
Nodes (4): Logging & Observability, [MEDIUM] لاگ‌های debug محتوای پیام چت را ثبت می‌کنند, [MEDIUM] هیچ مانیتورینگ یا هشدار خودکاری در تولید وجود ندارد, وضعیت

### Community 163 - "UserManagementDialogsSection.kt"
Cohesion: 0.38
Nodes (10): DesignTextField(), EnhancedAddUserDialog(), EnhancedEditUserDialog(), getUserTypes(), androidx, KeyboardOptions, Modifier, UserTypeOptionHorizontal() (+2 more)

### Community 164 - "ActiveQuotasGroupedComponents.kt"
Cohesion: 0.36
Nodes (9): formatNumber(), FlatQuotaCard(), extractLastDigits(), ActiveShipInfo, Color, QuotaItem(), ShipCard(), ShipHeader() (+1 more)

### Community 167 - "Project Overview"
Cohesion: 0.67
Nodes (3): Project Overview, استک, ساختار ماژول‌ها

### Community 171 - "UserSettingsStore"
Cohesion: 0.28
Nodes (4): Flow, UserSettingsStore, ThemeColorOption, ThemeColorPickerRow()

### Community 172 - "LoadingState"
Cohesion: 0.47
Nodes (6): Error, Idle, Loading, LoadingState, Success, UiState

### Community 173 - "LoginScreen"
Cohesion: 0.29
Nodes (7): AdaptiveLayoutConfig, rememberAdaptiveLayoutConfig(), WindowSizeClass, COMPACT, EXPANDED, MEDIUM, LoginScreen()

### Community 174 - "QuotaValidationUseCase.kt"
Cohesion: 0.36
Nodes (4): QuotaValidationResult, InputValidationResult, QuotaValidationUseCase, TempTonnageValidationResult

### Community 175 - "QuotaEntryDialog.kt"
Cohesion: 0.57
Nodes (7): FocusRequester, QuotaEntryActions(), QuotaEntryContent(), QuotaEntryDialog(), QuotaEntryPalette, rememberQuotaEntryPalette(), validateAndSubmit()

### Community 177 - "ApiResponse"
Cohesion: 0.33
Nodes (3): ApiResponse, DeleteMessageRequest, EditMessageRequest

### Community 178 - "PHP/composer.json"
Cohesion: 0.09
Nodes (21): autoload, autoload-dev, psr-4, psr-4, config, optimize-autoloader, sort-packages, description (+13 more)

### Community 184 - "ColorPicker.kt"
Cohesion: 0.83
Nodes (3): ColorWheel(), Color, Modifier

### Community 187 - "ProfileMenu.kt"
Cohesion: 0.52
Nodes (6): ActionButtons(), getUserTypeDisplay(), Color, ProfileMenu(), ProfilePalette, rememberProfilePalette()

### Community 188 - "ColorScheme.kt"
Cohesion: 0.80
Nodes (5): ColorScheme, buildAppDarkColorScheme(), buildAppLightColorScheme(), compositeOver(), Color

### Community 189 - "HomeNotificationSettingRow.kt"
Cohesion: 0.67
Nodes (5): CompactSwitch(), Color, ImageVector, Modifier, NotificationSettingRow()

### Community 191 - "RealTimeLoadingCardSection.kt"
Cohesion: 0.53
Nodes (5): CompactInfo(), Color, ImageVector, Modifier, StatisticItem()

### Community 195 - "UpdateManager"
Cohesion: 0.13
Nodes (7): DownloadProgress, Context, Job, StateFlow, UpdateInfo, UpdateManager, VersionCheckResult

### Community 209 - "HomeScreen.kt"
Cohesion: 0.27
Nodes (11): CategorizedMenuGrid(), CategoryHeader(), CompactMenuCard(), Header(), HomeScreen(), isWideCard(), SystemAwarenessBanner(), ConnectivityManager (+3 more)

### Community 221 - "secrets.cpp"
Cohesion: 0.34
Nodes (16): decryptXor(), JNI_OnLoad(), n0(), n1(), n2(), n3(), n4(), n5() (+8 more)

### Community 265 - "MessageType"
Cohesion: 0.13
Nodes (20): Modifier, StatusSnackbar(), MessageType, ERROR, SUCCESS, WARNING, CargoSnackbarQueue, StateFlow (+12 more)

### Community 272 - "Request"
Cohesion: 0.10
Nodes (4): lic_required_id(), OnlineUsersController, Request, Response

### Community 276 - "AuthController"
Cohesion: 0.19
Nodes (11): App\Core\Logger, App\Core\Request, App\Services\LoginAttemptLimiter, App\Services\PermissionService, App\Services\SessionService, App\Services\UserService, AuthController, enforceMinAppVersion() (+3 more)

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
- **293 isolated node(s):** `وضعیت کلی`, `مهم‌ترین ریسک‌های امنیتی`, `مهم‌ترین مشکلات Performance`, `مهم‌ترین مشکلات Architecture`, `Technical Debt` (+288 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **77 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CargoViewModel` connect `CargoViewModel` to `SelectInfoScreen.kt`, `CargoCounterOperationScreen`, `MainScreen.kt`, `TextAlign`, `CargoInfoDetailsDialogSection.kt`, `MessageType`, `RegisterCargoScreen.kt`, `QuotaValidationUseCase.kt`, `ApiServiceV2`, `CargoDetailsComponents.kt`, `AppModule.kt`, `CargoDetailsScreen.kt`, `CargoViewModel.kt`, `CargoViewModelTest`?**
  _High betweenness centrality (0.040) - this node is a cross-community bridge._
- **Why does `ReportsViewModel` connect `ReportsViewModel` to `ComprehensiveAnalyticsDialog.kt`, `QuotaDetailsScreen.kt`, `MainScreen.kt`, `CargoInfo`, `ShipSortingMode`, `ShipDetailsScreen.kt`, `CargoCounterScreen.kt`, `QuotaWarningDialog.kt`, `LoadingState`, `Quota`, `AppModule.kt`, `QuotaManagementContent.kt`, `ReportModels.kt`, `ManageReportsScreen.kt`, `formatNumber`?**
  _High betweenness centrality (0.027) - this node is a cross-community bridge._
- **Why does `UpdateManager` connect `UpdateManager` to `UserPreferencesManager`, `DownloadState`, `AppModule.kt`?**
  _High betweenness centrality (0.019) - this node is a cross-community bridge._
- **Are the 8 inferred relationships involving `InputValidator` (e.g. with `.getShipQuotasRemaining()` and `.handleQuotaRemaining()`) actually correct?**
  _`InputValidator` has 8 INFERRED edges - model-reasoned connections that need verification._
- **What connects `وضعیت کلی`, `مهم‌ترین ریسک‌های امنیتی`, `مهم‌ترین مشکلات Performance` to the rest of the system?**
  _293 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `StartupViewModel` be split into smaller, more focused modules?**
  _Cohesion score 0.051251956181533644 - nodes in this community are weakly interconnected._
- **Should `LicenseRepository` be split into smaller, more focused modules?**
  _Cohesion score 0.11764705882352941 - nodes in this community are weakly interconnected._