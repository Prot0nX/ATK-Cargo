# Graph Report - ATK-Cargo  (2026-08-21)

## Corpus Check
- 339 files · ~386,514 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2899 nodes · 5747 edges · 226 communities (165 shown, 61 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 196 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `49a49692`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- StartupViewModel
- LicenseAdminService
- LicenseAdminServiceTest
- MainScreen.kt
- TextAlign
- Ship
- ChatMessageEntity
- app.js
- ReportsRepository
- UserRepository
- QuotaWarningDialog.kt
- CrashReporter.kt
- ReportsViewModel
- JalaliDateUtilsTest
- ApiServiceV2
- Config
- ChatViewModel
- Medium Issues
- PermissionService
- Deep Code Audit Report
- SessionService
- CargoDetailsComponents.kt
- ManageReportsScreen.kt
- QuotaManagementContent.kt
- LoginScreen.kt
- SessionRepository
- CargoViewModel.kt
- TokenAuthenticator
- CargoViewModelTest
- ApiException
- SessionServiceTest
- Context
- CrashReportRateLimiter
- ApiServiceV2.kt
- ReportsDomainCalculationsTest
- MessageBubble.kt
- UsersManager
- Cargo
- CargoRepository
- AnalyticsController
- Logger
- VoucherDetailsDialogSection.kt
- CargoViewModel
- ATKCargoTheme
- User
- SecurityScreen.kt
- QuotaPercentageDialogSection.kt
- UserManagementScreen.kt
- MicroCache
- SelectInfoScreen.kt
- CargoInfo
- LoginAttemptLimiterTest
- ApiV2Routes
- CargoDetailsScreen.kt
- ChatScreen.kt
- LoginAttemptLimiter
- CargoController
- AppError
- Secrets
- ReportModels.kt
- RetrofitClient
- QuotasListDialogs.kt
- PasswordGateService
- formatNumber
- update.md
- UpdateDialog.kt
- MainActivity.kt
- Color.kt
- AuthViewModel
- SecurityAlerter
- dialogs/QuotaCardComponents.kt
- وضعیت: **بسیار خوب** — این نقطه‌ی قوت پروژه است
- TokenStore
- ProfileSettingsDialogSection.kt
- QuotaRepository
- Animation Audit
- AppDatabase
- MessageInputArea
- ChatRepository
- Recommended Architecture
- AtkCargoApplication.kt
- DownloadState
- StartupViewModel
- استانداردسازی کامنت‌های چندخطی به تک‌خطی (کلاینت + سرور)
- AppModule.kt
- Intent
- BootReceiver.kt
- KoinComponent
- SplashScreen.kt
- IBinder
- UserSettingsStore
- PermissionPoller
- Migrations
- StartupState
- پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13)
- Database
- FontWeight
- Service
- ChatToolbar.kt
- T
- Code Smells شناسایی‌شده
- ShipDetailsScreen.kt
- HardwarePerformanceEvaluator
- نقاط قوت
- DataModel.kt
- Executive Summary
- AppNotificationManager
- quota_details/QuotaCardComponents.kt
- چک‌لیست فعال‌سازی `STRICT_TRANS_TABLES` (DEEP_CODE_AUDIT.md #Phase4.10)
- What You Must Do When Invoked
- LoginScreen
- Final Recommendations
- ComponentDefaults.kt
- CargoEditSearchDialogsSection.kt
- نقاط قوت
- Logger
- حذف/محدودسازی phpMyAdmin روی سرور production (Phase 1, آیتم #۲)
- androidx
- ColorScheme.kt
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
- ProfileMenu.kt
- ComprehensiveAnalyticsDialog.kt
- وضعیت
- AndroidViewModel
- Flow
- Project Overview
- InputValidator
- StateFlow
- self
- HomeNotificationSettingRow.kt
- LoadingState
- graphify reference: query, path, explain
- PHP/composer.json
- com
- NavController
- StateFlow
- ViewModel
- Modifier
- ColorPicker.kt
- Context
- OkHttpClient
- InitialInfoScreen
- QuotaCalculatorTest
- UpdateManager
- SearchBar.kt
- HomeScreen.kt
- secrets.cpp
- SessionManager
- SessionManager
- MessageType
- Request
- AuthenticatesRequests.php
- Quota
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
3. `InputValidator` - 59 edges
4. `ApiServiceV2` - 57 edges
5. `formatNumber()` - 38 edges
6. `UserPreferencesManager` - 36 edges
7. `SessionRepository` - 35 edges
8. `Request` - 33 edges
9. `ReportsRepository` - 33 edges
10. `Logger` - 32 edges

## Surprising Connections (you probably didn't know these)
- `CargoCounterScreen()` --calls--> `ColorSelector`  [INFERRED]
  feature/cargo-workflow/src/main/java/com/atk/atk_cargo/feature/cargo_counter/presentation/CargoCounterScreen.kt → core/designsystem/src/main/java/com/atk/atk_cargo/data/model/ColorSelector.kt
- `CargoCounterScreen()` --calls--> `validateServerSession()`  [INFERRED]
  feature/cargo-workflow/src/main/java/com/atk/atk_cargo/feature/cargo_counter/presentation/CargoCounterScreen.kt → core/network/src/main/java/com/atk/atk_cargo/api/SessionValidator.kt
- `PersianDatePickerDialog()` --calls--> `FilterChip()`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/core/ui/components/DateRangePicker.kt → feature/cargo-workflow/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/ActiveQuotasContent.kt
- `calculateWarningStatus()` --calls--> `WarningStatus`  [EXTRACTED]
  feature/reports/src/main/java/com/atk/atk_cargo/feature/reports/presentation/details/ShipDetailsScreen.kt → core/network/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt
- `RegisterCargoScreen()` --calls--> `ShipInfo`  [INFERRED]
  feature/cargo-workflow/src/main/java/com/atk/atk_cargo/feature/cargo_registration/presentation/RegisterCargoScreen.kt → core/domain/src/main/java/com/atk/atk_cargo/domain/model/ShipInfo.kt

## Import Cycles
- None detected.

## Communities (226 total, 61 thin omitted)

### Community 0 - "StartupViewModel"
Cohesion: 0.13
Nodes (10): AndroidViewModel, Intent, RequestBatteryOptimization, SecurityCheckState, ShowMessage, StartupEvent, StartupViewModel, Flow (+2 more)

### Community 1 - "LicenseAdminService"
Cohesion: 0.10
Nodes (3): LicenseRepository, LicenseAdminService, PHPUnit\Framework\MockObject\MockObject

### Community 3 - "MainScreen.kt"
Cohesion: 0.06
Nodes (44): MainScreen(), RouteTransitions, standardTransitions(), Cargo, CargoViewModel, ShipInfo, CargoCounterRoute, CargoDetailsRoute (+36 more)

### Community 4 - "TextAlign"
Cohesion: 0.08
Nodes (52): CargoInfoRequest, adaptiveThresholding(), applyContrastEnhancement(), enhanceEdges(), ImageProxy, InputImage, medianFilter(), preprocessImage() (+44 more)

### Community 5 - "Ship"
Cohesion: 0.11
Nodes (23): ErrorState(), ImageVector, Modifier, Ship, ShipSortingMode, LOADED_TONNAGE_ASC, LOADED_TONNAGE_DESC, NAME_ASC (+15 more)

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
Cohesion: 0.26
Nodes (16): WarningStatus, CompactInfoSection(), DialogHeader(), Color, ImageVector, Modifier, LoadingActionButton(), NavigationButton() (+8 more)

### Community 11 - "CrashReporter.kt"
Cohesion: 0.52
Nodes (3): CrashReporter, Context, PendingCrashReport

### Community 12 - "ReportsViewModel"
Cohesion: 0.06
Nodes (15): ComprehensiveAnalytics, GroupSortingMode, ALPHABETICAL, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC, QuotaSortingMode, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC (+7 more)

### Community 14 - "ApiServiceV2"
Cohesion: 0.08
Nodes (16): toDomain(), toDto(), ApiServiceV2, CargoDeleteResponse, CargoInfoSearch, CargoSearchResponse, CargoStats, CheckExistenceResponse (+8 more)

### Community 15 - "Config"
Cohesion: 0.10
Nodes (8): lic_destroy_session(), lic_idle_timeout(), lic_is_authenticated(), lic_require_auth_json(), lic_require_auth_page(), Config, self, Csrf

### Community 16 - "ChatViewModel"
Cohesion: 0.10
Nodes (7): ChatViewModel, ChatViewModelFactory, com, StateFlow, T, ViewModel, ViewModelProvider

### Community 17 - "Medium Issues"
Cohesion: 0.09
Nodes (23): Critical Issues, [CRITICAL] تخلیه‌ی worker pool سرور از طریق `sleep()` مسدودکننده در مسیر لاگین, [HIGH] endpoint گزارش کرش بدون auth با rate-limiter مسدودکننده, [HIGH] Foreground Service با poll دائمی، ناسازگار با سقف `dataSync` در Android 14+, High Issues, [HIGH] رازهای کلاینت با XOR تک‌بایتی «محافظت» شده‌اند, [HIGH] ~~مسیر build نسخه‌ی release هرگز در CI اجرا نمی‌شود~~ — تصحیح‌شده، سپس رفع شد, [HIGH] گیت نسخه‌ی حداقلی با حذف یک هدر دور زده می‌شود (+15 more)

### Community 18 - "PermissionService"
Cohesion: 0.06
Nodes (7): ApiAuthGate, MinVersionGate, Request, Router, PermissionRepository, PermissionService, PermissionServiceTest

### Community 19 - "Deep Code Audit Report"
Cohesion: 0.10
Nodes (19): Architecture Overview, Deep Code Audit Report, Final Score, [LOW] `applicationScope` بدون لغو در `AtkCargoApplication`, [MEDIUM] نتایج بدون سقف در سمت سرور، نه در کلاینت, Memory Audit, Overall Score, **Overall Score: 5.8 / 10** (+11 more)

### Community 21 - "CargoDetailsComponents.kt"
Cohesion: 0.17
Nodes (17): QuotaInfo, QuotaValidationResult, InputValidationResult, QuotaValidationUseCase, TempTonnageValidationResult, CargoInfoCard(), CargoListSection(), GroupStats() (+9 more)

### Community 22 - "ManageReportsScreen.kt"
Cohesion: 0.06
Nodes (56): ApiQuotaDetails, DateClickableField(), DateRangePicker(), getDaysInPersianMonth(), isPersianLeapYear(), Color, Modifier, PersianDatePickerDialog() (+48 more)

### Community 23 - "QuotaManagementContent.kt"
Cohesion: 0.22
Nodes (18): AdvancedFiltersDialog(), EmptyQuotaState(), FilterOptionsCard(), Modifier, QuotaFilters, QuotaManagementContent(), QuotaTabContent(), QuotaTabItem() (+10 more)

### Community 24 - "LoginScreen.kt"
Cohesion: 0.26
Nodes (16): CompactLoginLayout(), ExpandedLoginLayout(), IndustrialBrandingHeaderContent(), IndustrialFeatureBadge(), IndustrialFooterInfo(), IndustrialInputField(), IndustrialLoginFormCard(), androidx (+8 more)

### Community 26 - "CargoViewModel.kt"
Cohesion: 0.10
Nodes (12): Kilograms, CargoDialog, CargoUiState, CargoViewModelFactory, DuplicateConfirmation, Duplicates, StateFlow, T (+4 more)

### Community 27 - "TokenAuthenticator"
Cohesion: 0.19
Nodes (5): Authenticator, TokenAuthenticator, AuthErrorBody, TokenAuthenticatorTest, Route

### Community 29 - "ApiException"
Cohesion: 0.14
Nodes (6): App\Enums\CargoConfirmStatus, App\Enums\CargoStatus, Exception, ApiException, ConflictException, DatabaseException

### Community 33 - "ApiServiceV2.kt"
Cohesion: 0.11
Nodes (16): ApiResponse2, Result, validateServerSession(), ActiveSessionResponse, ForceLogoutResponse, LoginRequest, LoginResponse, LogoutResponse (+8 more)

### Community 34 - "ReportsDomainCalculationsTest"
Cohesion: 0.13
Nodes (7): calculatePercentage(), calculateProgress(), format(), formatWeightWithDetail(), Context, QuotaWarningThresholds, ReportsDomainCalculationsTest

### Community 35 - "MessageBubble.kt"
Cohesion: 0.24
Nodes (14): extractShipInfoAndText(), com, replaceUsernamesWithFullNames(), rotateIcon(), ShipInfoModel, ChatInfoRow(), EditMessageDialog(), androidx (+6 more)

### Community 36 - "UsersManager"
Cohesion: 0.07
Nodes (7): ApiManager, CONFIG, ModalManager, ThemeManager, UIManager, UsersManager, Utils

### Community 37 - "Cargo"
Cohesion: 0.14
Nodes (22): Cargo, CargoConfirmStatus, AWAITING_CONFIRMATION, CONFIRMED, PENDING, CargoStatus, ENTERED, EXITED (+14 more)

### Community 39 - "AnalyticsController"
Cohesion: 0.16
Nodes (11): gregorian_to_jalali(), jalali_to_gregorian(), jcheckdate(), jdate(), jdate_words(), jgetdate(), jmktime(), jstrftime() (+3 more)

### Community 40 - "Logger"
Cohesion: 0.17
Nodes (3): LicenseController, Logger, self

### Community 41 - "VoucherDetailsDialogSection.kt"
Cohesion: 0.17
Nodes (21): FilteredSummary, VoucherDetail, persianDateFormat(), DateTimePicker(), Modifier, PersianDateItem(), TimePickerDialog(), EmptyVoucherList() (+13 more)

### Community 42 - "CargoViewModel"
Cohesion: 0.11
Nodes (3): SaveOrUpdateResponse, CargoViewModel, message

### Community 43 - "ATKCargoTheme"
Cohesion: 0.14
Nodes (11): ComponentStyles, Dimensions, Elevation, ATKCargoTheme, Motion, SemanticColors, AppShapes, Spacing (+3 more)

### Community 44 - "User"
Cohesion: 0.12
Nodes (11): ApiResponse, CreateUserRequest, DeleteUserRequest, ForceLogoutRequest, UpdateUserRequest, User, EnhancedForceLogoutDialog(), ViewModel (+3 more)

### Community 45 - "SecurityScreen.kt"
Cohesion: 0.23
Nodes (14): AdvancedOrbitalScanner(), AnimatedUpdateIcon(), AnimatedWarningIcon(), DynamicPremiumBackground(), InteractiveExitButton(), Modifier, RetryButton(), SecurityBlockScreen() (+6 more)

### Community 46 - "QuotaPercentageDialogSection.kt"
Cohesion: 0.20
Nodes (17): createTypography(), CalculationResult, AnimatedNumber(), Color, ImageVector, Modifier, lerp(), PercentageDisplay() (+9 more)

### Community 47 - "UserManagementScreen.kt"
Cohesion: 0.26
Nodes (14): androidx, DashboardSummaryRow(), EmptyStateView(), EnterpriseUserCard(), Color, RoleFilterChipRow(), RoleGroupHeader(), ShimmerUserLoadingList() (+6 more)

### Community 48 - "MicroCache"
Cohesion: 0.08
Nodes (5): MicroCache, AppApiCacheKeys, AuditLogger, QuotaService, UserService

### Community 49 - "SelectInfoScreen.kt"
Cohesion: 0.06
Nodes (65): MatchingQuota, ActiveShipInfo, EmptySearchResult(), FilterChip(), FilterState, ALL, COMPLETED, PENDING (+57 more)

### Community 50 - "CargoInfo"
Cohesion: 0.19
Nodes (10): toDomain(), toDto(), CargoInfo, CargoSearchResultCard(), InfoRowCompact(), Color, Context, ImageVector (+2 more)

### Community 53 - "CargoDetailsScreen.kt"
Cohesion: 0.33
Nodes (9): CargoCounterOperationScreen(), NavController, CargoDetailsScreen(), FloatingActionButtonItem(), Color, ImageVector, NavController, refreshData() (+1 more)

### Community 54 - "ChatScreen.kt"
Cohesion: 0.17
Nodes (16): ChatPreferencesStore, Flow, ChatUiItem, getDateHeaderColor(), Header, Message, AdminChatRoute, adminChatScreen() (+8 more)

### Community 56 - "CargoController"
Cohesion: 0.09
Nodes (15): App\Core\Database, App\Core\DatabaseManager, App\Core\Logger, App\Core\MicroCache, App\Core\Request, App\Core\Response, App\Exceptions\ApiException, App\Repositories\CargoRepository (+7 more)

### Community 57 - "AppError"
Cohesion: 0.32
Nodes (6): AppError, Network, Server, Timeout, toAppError(), Validation

### Community 59 - "ReportModels.kt"
Cohesion: 0.14
Nodes (15): AnalyticsData, DateInfo, ExistingQuota, ExitDateInfo, QuotaCompletionAnalysis, QuotaStatusDetails, RealTimeLoadingData, ShiftInfo (+7 more)

### Community 60 - "RetrofitClient"
Cohesion: 0.13
Nodes (16): ApiServiceV2, CryptoManager, ByteArray, Cipher, Context, FloatTypeAdapter, RetrofitClient, HttpLoggingInterceptor (+8 more)

### Community 61 - "QuotasListDialogs.kt"
Cohesion: 0.53
Nodes (5): DeleteQuotaDialog(), androidx, Color, QuotaConfirmDialog(), ToggleQuotaStatusDialog()

### Community 63 - "formatNumber"
Cohesion: 0.19
Nodes (22): QuotaCompletionData, QuotaGroupingMode, BY_CARGO_OWNER, BY_CARRIER, BY_SHIP, buildQuotaGroups(), QuotaGroup, formatNumber() (+14 more)

### Community 64 - "update.md"
Cohesion: 0.50
Nodes (3): For --cluster-only, For --update (incremental re-extraction), graphify reference: incremental update and cluster-only

### Community 65 - "UpdateDialog.kt"
Cohesion: 0.30
Nodes (13): DownloadActionButtons(), DownloadingState(), DownloadProgressInfo(), DownloadSizeInfo(), DownloadSpeedInfo(), ErrorState(), ModernUpdateActionSection(), ModernUpdateContent() (+5 more)

### Community 66 - "MainActivity.kt"
Cohesion: 0.26
Nodes (6): Intent, MainActivity, StartupErrorScreen(), Bundle, ComponentActivity, ServerSyncingScreen()

### Community 67 - "Color.kt"
Cohesion: 0.23
Nodes (11): CompactStatChipTest, DesignTextField(), EnhancedAddUserDialog(), EnhancedEditUserDialog(), getUserTypes(), androidx, KeyboardOptions, Modifier (+3 more)

### Community 68 - "AuthViewModel"
Cohesion: 0.17
Nodes (8): AuthViewModel, Error, Idle, StateFlow, ViewModel, Loading, LoginUiState, Success

### Community 70 - "dialogs/QuotaCardComponents.kt"
Cohesion: 0.29
Nodes (13): QuotaItem, AnalyticsStatChipMini(), CompactStatChip(), InfoChip(), IntegratedQuotaCard(), Color, ImageVector, Modifier (+5 more)

### Community 71 - "وضعیت: **بسیار خوب** — این نقطه‌ی قوت پروژه است"
Cohesion: 0.50
Nodes (4): Dependency Audit, [LOW] دو استک سریال‌سازی JSON هم‌زمان, [LOW] نام مستعارهای version catalog با نسخه‌های واقعی نمی‌خوانند, وضعیت: **بسیار خوب** — این نقطه‌ی قوت پروژه است

### Community 72 - "TokenStore"
Cohesion: 0.11
Nodes (6): OkHttpClient, TokenRefresher, TokenStore, RefreshTokenResponse, TokenRefresherTest, MockWebServer

### Community 73 - "ProfileSettingsDialogSection.kt"
Cohesion: 0.67
Nodes (3): EnhancedDeleteConfirmationDialog(), getUserTypeDisplay(), ProfileSettingsDialog()

### Community 74 - "QuotaRepository"
Cohesion: 0.24
Nodes (3): QuotaRepository, CargoInfoResponse, QuotaStatusResponse

### Community 75 - "Animation Audit"
Cohesion: 0.67
Nodes (3): Animation Audit, [LOW] اسپلش با تأخیر ثابت به‌جای انیمیشن مبتنی بر پیشرفت, وضعیت کلی

### Community 76 - "AppDatabase"
Cohesion: 0.47
Nodes (3): AppDatabase, Context, RoomDatabase

### Community 77 - "MessageInputArea"
Cohesion: 0.67
Nodes (5): Color, com, MessageInputArea(), QuotaSelectionDialog(), ShipSelectionDialog()

### Community 78 - "ChatRepository"
Cohesion: 0.11
Nodes (11): ChatMessage, ChatMessagesResponse, SendMessageRequest, SendMessageResponse, UnreadCountResponse, DeleteMessageRequest, EditMessageRequest, ChatRepository (+3 more)

### Community 79 - "Recommended Architecture"
Cohesion: 0.67
Nodes (3): Recommended Architecture, سمت بک‌اند, قواعد قابل اجرا

### Community 81 - "DownloadState"
Cohesion: 0.29
Nodes (6): Completed, Downloading, DownloadState, Error, Idle, Paused

### Community 83 - "استانداردسازی کامنت‌های چندخطی به تک‌خطی (کلاینت + سرور)"
Cohesion: 0.29
Nodes (6): Context, Verification, استانداردسازی کامنت‌های چندخطی به تک‌خطی (کلاینت + سرور), دایرکتوری‌های مستثنی (اصلاً لمس نشوند), قوانین استاندارد تبدیل, نحوه‌ی اجرا

### Community 84 - "AppModule.kt"
Cohesion: 0.09
Nodes (14): Flow, UserPreferencesStore, LogoutRequest, AuthRepository, ConflictSession, Error, Flow, LoginResult (+6 more)

### Community 86 - "BootReceiver.kt"
Cohesion: 0.39
Nodes (6): BootReceiver, BroadcastReceiver, Context, Intent, KoinComponent, UserPreferencesManager

### Community 88 - "SplashScreen.kt"
Cohesion: 0.27
Nodes (8): VazirmatnFontFamily, AnimatedBrandTitle(), SplashScreen(), SplashScreenConstants, SplashScreenContent(), SplashSubtitleBadge(), SplashVersionBadge(), FontFamily

### Community 90 - "UserSettingsStore"
Cohesion: 0.28
Nodes (4): Flow, UserSettingsStore, ThemeColorOption, ThemeColorPickerRow()

### Community 91 - "PermissionPoller"
Cohesion: 0.33
Nodes (4): Job, StateFlow, PermissionPoller, CoroutineScope

### Community 93 - "Migrations"
Cohesion: 0.33
Nodes (5): Migrations, migrationهای اعمال‌شده تاکنون, قرارداد نام‌گذاری, نحوه‌ی اجرا, وضعیت schema.sql

### Community 95 - "StartupState"
Cohesion: 0.29
Nodes (7): NativeLibraryUnavailable, Ready, SecurityBlocked, Splash, StartupState, Syncing, VersionExpired

### Community 96 - "پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13)"
Cohesion: 0.22
Nodes (8): ساختار فعلی (مستند شده در این جلسه), ساختار هدف, مراحل اجرا (ترتیب دقیق مهم است), معیار موفقیت, پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13), پیش‌نیازهای جانبی که باید قبل از این کار حل شوند, چرا فقط .htaccess کافی نیست, چرا لازم است

### Community 97 - "Database"
Cohesion: 0.11
Nodes (7): mysqli, mysqli_stmt, PDO, Database, PDO, self, DatabaseManager

### Community 99 - "FontWeight"
Cohesion: 0.20
Nodes (9): ConfirmationDialog(), ImageVector, EmptyState(), ImageVector, Modifier, AnimatedCounter(), ExitStatusDialog(), ImageVector (+1 more)

### Community 101 - "ChatToolbar.kt"
Cohesion: 0.29
Nodes (12): getAdaptiveBubbleColor(), getChatBackgroundColor(), Color, AdvancedColorPickerDialog(), ChatSettingsDialogEnhanced(), ChatSettingsPreviewRefined(), Color, ImageVector (+4 more)

### Community 103 - "Code Smells شناسایی‌شده"
Cohesion: 0.25
Nodes (8): Code Quality, Code Smells شناسایی‌شده, [HIGH] God ViewModel — `CargoViewModel`, [HIGH] نشت لایه — ViewModelها مستقیم `ApiServiceV2` را صدا می‌زنند, [LOW] اعداد و رشته‌های جادویی, [MEDIUM] `ChatRepository` مسئولیت‌های نامرتبط دارد (Feature Envy), [MEDIUM] ViewModel داخل فایل Composable, [MEDIUM] فایل‌های بسیار بزرگ در سراسر پروژه

### Community 104 - "ShipDetailsScreen.kt"
Cohesion: 0.42
Nodes (8): calculateWarningStatus(), ErrorStateCard(), ImageVector, ShipDetails(), ShipHeaderCard(), TabItem(), WarehouseQuotasTabs(), WarehousesAndQuotasTab()

### Community 109 - "نقاط قوت"
Cohesion: 0.25
Nodes (8): Database Audit, [HIGH] کلیدهای JOIN ناسازگار بین کوئری‌های تحلیلی و سرویس‌ها, [LOW] collation ناسازگار در جدول `Passwords`, [LOW] ایندکس‌های زائد و کم‌ارزش روی `admin_chat_messages`, [MEDIUM] `CargoInfo` هیچ کلید خارجی به `InitialInfo` ندارد, [MEDIUM] تاریخ و زمان به‌صورت `varchar(100)` ذخیره می‌شوند, [MEDIUM] نوع‌دهی ناسازگار ستون‌های وزن, نقاط قوت

### Community 115 - "Executive Summary"
Cohesion: 0.25
Nodes (8): Executive Summary, Overall Score: **5.8 / 10**, Production Readiness, Technical Debt, مهم‌ترین ریسک‌های امنیتی, مهم‌ترین مشکلات Architecture, مهم‌ترین مشکلات Performance, وضعیت کلی

### Community 118 - "AppNotificationManager"
Cohesion: 0.06
Nodes (26): AppNotificationManager, BroadcastReceiver, Context, Intent, NotificationActionReceiver, ChatNotificationWorker, KoinComponent, KoinComponent (+18 more)

### Community 119 - "quota_details/QuotaCardComponents.kt"
Cohesion: 0.17
Nodes (20): ActionButtonTest, QuotaEditData, QuotaPercentageData, ActionButton(), calculateValues(), ConfirmationDialogHeader(), EditFieldBox(), EditFieldColumn() (+12 more)

### Community 120 - "چک‌لیست فعال‌سازی `STRICT_TRANS_TABLES` (DEEP_CODE_AUDIT.md #Phase4.10)"
Cohesion: 0.25
Nodes (7): ریسک واقعی کشف‌شده: بدون سقف طول روی فیلدهای متنی آزاد, مراحل امن (ترتیب مهم است), مسئله چیست, معیار موفقیت, نتایج تست عملی (Phase 5.15), چرا نمی‌شود همین الان فعالش کرد, چک‌لیست فعال‌سازی `STRICT_TRANS_TABLES` (DEEP_CODE_AUDIT.md #Phase4.10)

### Community 122 - "What You Must Do When Invoked"
Cohesion: 0.07
Nodes (26): For /graphify add and --watch, For /graphify query, For the commit hook and native CLAUDE.md integration, For --update and --cluster-only, /graphify, Honesty Rules, Interpreter guard for subcommands, Part A - Structural extraction for code files (+18 more)

### Community 123 - "LoginScreen"
Cohesion: 0.29
Nodes (7): AdaptiveLayoutConfig, rememberAdaptiveLayoutConfig(), WindowSizeClass, COMPACT, EXPANDED, MEDIUM, LoginScreen()

### Community 124 - "Final Recommendations"
Cohesion: 0.29
Nodes (7): Final Recommendations, ۱. اول جلوی خون‌ریزی را بگیر، بعد بازسازی کن, ۲. بدهی معماری را با فیچرهای جدید بپرداز، نه با یک پروژه‌ی جداگانه, ۳. تست را از جایی شروع کن که شکستش بی‌صدا و پرهزینه است, ۴. مسیر release را در CI گیت کن — همین امروز, ۵. آنچه نباید تغییر کند, ۶. تصمیم‌های محصولی که باید گرفته شوند

### Community 128 - "CargoEditSearchDialogsSection.kt"
Cohesion: 0.11
Nodes (43): ActiveShipInfo, CargoInfo, AnimationManager, GroupedShipList(), Color, QuotaCard(), ShipGroup(), ShipHeader() (+35 more)

### Community 129 - "نقاط قوت"
Cohesion: 0.29
Nodes (7): [LOW] PHPStan فایل‌های ریشه و پنل مدیریتی را پوشش نمی‌دهد, [MEDIUM] `AppApiController` عمدتاً یک Middle Man است, [MEDIUM] دو مسیر دسترسی به دیتابیس (PDO و mysqli) به‌صورت موازی, [MEDIUM] رمزگذاری خروجی ناسازگار بین دو مسیر نوشتن, [MEDIUM] گیت احراز هویت دوبار پیاده‌سازی شده است, PHP Backend Audit, نقاط قوت

### Community 132 - "حذف/محدودسازی phpMyAdmin روی سرور production (Phase 1, آیتم #۲)"
Cohesion: 0.40
Nodes (4): حذف/محدودسازی phpMyAdmin روی سرور production (Phase 1, آیتم #۲), راستی‌آزمایی نهایی, گزینه‌ی ۱ — حذف کامل (توصیه‌شده), گزینه‌ی ۲ — اگر حذف فوری ممکن نیست

### Community 135 - "ColorScheme.kt"
Cohesion: 0.80
Nodes (5): ColorScheme, buildAppDarkColorScheme(), buildAppLightColorScheme(), compositeOver(), Color

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
Cohesion: 0.09
Nodes (7): Flow, UserPreferencesManager, ChatPreferencesStore, DataStore, Preferences, T, UserPreferencesStore

### Community 145 - "SecurityVerifier"
Cohesion: 0.17
Nodes (10): SecurityErrorType, LICENSE_INACTIVE, LICENSE_NOT_FOUND, NETWORK_ERROR, TAMPERED, UNKNOWN_ERROR, SecurityVerifier, JSONObject (+2 more)

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

### Community 159 - "ProfileMenu.kt"
Cohesion: 0.52
Nodes (6): ActionButtons(), getUserTypeDisplay(), Color, ProfileMenu(), ProfilePalette, rememberProfilePalette()

### Community 160 - "ComprehensiveAnalyticsDialog.kt"
Cohesion: 0.57
Nodes (6): AnalyticsDateNavigation(), AnalyticsHeaderCard(), ComprehensiveAnalyticsDialog(), EmptyStateCard(), ErrorStateCard(), Modifier

### Community 161 - "وضعیت"
Cohesion: 0.50
Nodes (4): Logging & Observability, [MEDIUM] لاگ‌های debug محتوای پیام چت را ثبت می‌کنند, [MEDIUM] هیچ مانیتورینگ یا هشدار خودکاری در تولید وجود ندارد, وضعیت

### Community 167 - "Project Overview"
Cohesion: 0.67
Nodes (3): Project Overview, استک, ساختار ماژول‌ها

### Community 168 - "InputValidator"
Cohesion: 0.08
Nodes (3): AuthController, InputValidator, InputValidatorTest

### Community 171 - "HomeNotificationSettingRow.kt"
Cohesion: 0.67
Nodes (5): CompactSwitch(), Color, ImageVector, Modifier, NotificationSettingRow()

### Community 172 - "LoadingState"
Cohesion: 0.47
Nodes (6): Error, Idle, Loading, LoadingState, Success, UiState

### Community 174 - "graphify reference: query, path, explain"
Cohesion: 0.40
Nodes (5): For /graphify explain, For /graphify path, graphify reference: query, path, explain, Step 0 — Constrained query expansion (REQUIRED before traversal), Step 1 — Traversal

### Community 178 - "PHP/composer.json"
Cohesion: 0.09
Nodes (21): autoload, autoload-dev, psr-4, psr-4, config, optimize-autoloader, sort-packages, description (+13 more)

### Community 184 - "ColorPicker.kt"
Cohesion: 0.83
Nodes (3): ColorWheel(), Color, Modifier

### Community 188 - "InitialInfoScreen"
Cohesion: 0.10
Nodes (34): CheckExistenceRequest, formatNumber(), isValidPersianText(), isValidQuotaNumber(), isValidShipName(), isValidWarehouseName(), isValidWeight(), ConfirmationDialog() (+26 more)

### Community 190 - "QuotaCalculatorTest"
Cohesion: 0.09
Nodes (3): QuotaCalculator, QuotaCalculatorTest, PHPUnit\Framework\TestCase

### Community 195 - "UpdateManager"
Cohesion: 0.13
Nodes (7): DownloadProgress, Context, Job, StateFlow, UpdateInfo, UpdateManager, VersionCheckResult

### Community 209 - "HomeScreen.kt"
Cohesion: 0.10
Nodes (25): com, MenuItem, CargoCounterScreen(), CargoCounterViewModel, filterShipsByTab(), ActiveShipInfo, navigateToCargoDetailsScreen(), ShipFilterTab (+17 more)

### Community 221 - "secrets.cpp"
Cohesion: 0.34
Nodes (16): decryptXor(), JNI_OnLoad(), n0(), n1(), n2(), n3(), n4(), n5() (+8 more)

### Community 265 - "MessageType"
Cohesion: 0.13
Nodes (20): Modifier, StatusSnackbar(), MessageType, ERROR, SUCCESS, WARNING, CargoSnackbarQueue, StateFlow (+12 more)

### Community 272 - "Request"
Cohesion: 0.05
Nodes (9): App\Core\AuthenticatesRequests, InvalidArgumentException, lic_required_id(), DiagnosticsController, OnlineUsersController, UserController, UtilityController, Request (+1 more)

### Community 276 - "AuthenticatesRequests.php"
Cohesion: 0.48
Nodes (6): App\Services\PermissionService, App\Services\SessionService, enforceMinAppVersion(), requireAuthenticatedSession(), requirePermission(), sendAuthErrorResponse()

### Community 282 - "Quota"
Cohesion: 0.21
Nodes (17): Quota, WarehouseQuotaGroupingMode, BY_CARGO_OWNER, BY_SHIPPING_COMPANY, BY_WAREHOUSE, buildQuotasShareText(), shareQuotasData(), GroupingModeButton() (+9 more)

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
- **61 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CargoViewModel` connect `CargoViewModel` to `MainScreen.kt`, `TextAlign`, `MessageType`, `ApiServiceV2`, `SelectInfoScreen.kt`, `AppModule.kt`, `CargoDetailsComponents.kt`, `CargoDetailsScreen.kt`, `CargoViewModel.kt`, `CargoViewModelTest`?**
  _High betweenness centrality (0.045) - this node is a cross-community bridge._
- **Why does `ReportsViewModel` connect `ReportsViewModel` to `ComprehensiveAnalyticsDialog.kt`, `MainScreen.kt`, `Ship`, `dialogs/QuotaCardComponents.kt`, `ShipDetailsScreen.kt`, `QuotaWarningDialog.kt`, `LoadingState`, `CargoInfo`, `AppModule.kt`, `ManageReportsScreen.kt`, `QuotaManagementContent.kt`, `Quota`, `ReportModels.kt`, `formatNumber`?**
  _High betweenness centrality (0.042) - this node is a cross-community bridge._
- **Why does `ApiV2Routes` connect `ApiV2Routes` to `ApiServiceV2.kt`, `ReportsRepository`, `User`, `ChatRepository`, `CargoViewModel.kt`?**
  _High betweenness centrality (0.022) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `InputValidator` (e.g. with `.getShipQuotasRemaining()` and `.handleQuotaRemaining()`) actually correct?**
  _`InputValidator` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `وضعیت کلی`, `مهم‌ترین ریسک‌های امنیتی`, `مهم‌ترین مشکلات Performance` to the rest of the system?**
  _293 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `StartupViewModel` be split into smaller, more focused modules?**
  _Cohesion score 0.1282051282051282 - nodes in this community are weakly interconnected._
- **Should `LicenseAdminService` be split into smaller, more focused modules?**
  _Cohesion score 0.09879032258064516 - nodes in this community are weakly interconnected._