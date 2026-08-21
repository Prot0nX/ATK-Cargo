# Graph Report - ATK-Cargo  (2026-08-21)

## Corpus Check
- 339 files · ~384,922 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2861 nodes · 5776 edges · 225 communities (170 shown, 55 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 172 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `851c0369`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- StartupViewModel
- LicenseAdminService
- LicenseAdminServiceTest
- MainScreen.kt
- TextAlign
- ReportsCommonWidgets.kt
- ChatMessageEntity
- app.js
- ReportsRepository
- UserService
- ShipDetailsScreen.kt
- QuotaPercentageDialogSection.kt
- ReportsViewModel
- JalaliDateUtilsTest
- ApiServiceV2
- LoginAttemptLimiter
- ChatViewModel
- Medium Issues
- PermissionService
- Deep Code Audit Report
- SessionService
- QuotaValidationUseCase.kt
- ManageReportsScreen.kt
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
- AppModule.kt
- ReportsDomainCalculationsTest
- MessageBubble.kt
- UsersManager
- Cargo
- CargoRepository
- CameraSection.kt
- Logger
- CargoDetailsScreen.kt
- CargoViewModel
- ATKCargoTheme
- User
- MainActivity.kt
- ActiveQuotasContent.kt
- ActiveShipInfo
- MicroCache
- SelectInfoScreen.kt
- LicenseController
- LoginAttemptLimiterTest
- ReportModels.kt
- CargoDetailsComponents.kt
- ChatScreen.kt
- UpdateDialog.kt
- ApiException
- SplashScreen.kt
- CoroutineScope
- RealTimeLoadingBottomSheet.kt
- RetrofitClient
- LoadingNotificationWorker
- CargoController
- formatNumber
- TokenRefresherTest
- Secrets
- Color.kt
- RegisterCargoScreen.kt
- AuthViewModel
- update.md
- ChatPreferencesStore
- Typography.kt
- TokenStore
- ActiveQuotasGroupedComponents.kt
- InitialInfo
- ExitStatusDialog.kt
- ApiV2Routes
- MessageInputArea
- ChatRepository
- Recommended Architecture
- ComprehensiveAnalyticsDialog.kt
- DownloadState
- CargoOperationScreen
- استانداردسازی کامنت‌های چندخطی به تک‌خطی (کلاینت + سرور)
- LoginResult
- Intent
- preprocessImage
- KoinComponent
- RealTimeLoadingCardSection.kt
- IBinder
- Intent
- AppDatabase
- AnimationManager
- Migrations
- .onError
- PasswordGateService
- پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13)
- UserRepository
- AnimationManager
- FontWeight
- Service
- ColorPicker.kt
- Code Smells شناسایی‌شده
- HardwarePerformanceEvaluator
- نقاط قوت
- ShipInfoSection.kt
- DataModel.kt
- Executive Summary
- AppNotificationManager
- quota_details/QuotaCardComponents.kt
- چک‌لیست فعال‌سازی `STRICT_TRANS_TABLES` (DEEP_CODE_AUDIT.md #Phase4.10)
- What You Must Do When Invoked
- LoginScreen
- Final Recommendations
- ComponentDefaults.kt
- VoucherDetailsDialogSection.kt
- نقاط قوت
- ProfileMenu.kt
- حذف/محدودسازی phpMyAdmin روی سرور production (Phase 1, آیتم #۲)
- AnalyticsController
- QuotaDetailsScreen.kt
- ColorScheme.kt
- Authentication & Authorization
- وضعیت کلی
- Performance Audit
- نقاط قوت
- چه چیزی بد است
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
- وضعیت: **بسیار خوب** — این نقطه‌ی قوت پروژه است
- وضعیت
- Animation Audit
- Project Overview
- AndroidViewModel
- Flow
- InputValidator
- StateFlow
- self
- PHP/composer.json
- InitialInfoScreen
- QuotaCalculatorTest
- UpdateManager
- LoadingState
- QuotaSelectionDialog.kt
- QuotaEntryDialog.kt
- SearchBar.kt
- CargoInfoDetailsDialogSection.kt
- CargoCounterScreen.kt
- secrets.cpp
- SessionManager
- SessionManager
- BootReceiver.kt
- CryptoManager
- Database
- MessageType
- ChatNotificationWorker.kt
- Request
- NotificationActionReceiver.kt
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
1. `ReportsViewModel` - 96 edges
2. `CargoViewModel` - 75 edges
3. `InputValidator` - 59 edges
4. `ApiServiceV2` - 59 edges
5. `formatNumber()` - 45 edges
6. `ActiveShipInfo` - 40 edges
7. `UserPreferencesManager` - 38 edges
8. `UpdateManager` - 36 edges
9. `SessionRepository` - 35 edges
10. `Request` - 35 edges

## Surprising Connections (you probably didn't know these)
- `PersianDatePickerDialog()` --calls--> `FilterChip()`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/core/ui/components/DateRangePicker.kt → feature/cargo-workflow/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/ActiveQuotasContent.kt
- `calculateValues()` --calls--> `CalculationResult`  [EXTRACTED]
  feature/reports/src/main/java/com/atk/atk_cargo/feature/reports/presentation/quota_details/QuotaCardComponents.kt → core/network/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt
- `RegisterCargoScreen()` --calls--> `ShipInfo`  [EXTRACTED]
  feature/cargo-workflow/src/main/java/com/atk/atk_cargo/feature/cargo_registration/presentation/RegisterCargoScreen.kt → core/domain/src/main/java/com/atk/atk_cargo/domain/model/ShipInfo.kt
- `ManageReportsScreen()` --references--> `ReportsViewModel`  [EXTRACTED]
  app/src/main/java/com/atk/atk_cargo/ui/screens/ManageReportsScreen.kt → feature/reports/src/main/java/com/atk/atk_cargo/ui/viewmodel/ReportsViewModel.kt
- `ReportsViewModel` --calls--> `ColorSelector`  [EXTRACTED]
  feature/reports/src/main/java/com/atk/atk_cargo/ui/viewmodel/ReportsViewModel.kt → core/designsystem/src/main/java/com/atk/atk_cargo/data/model/ColorSelector.kt

## Import Cycles
- None detected.

## Communities (225 total, 55 thin omitted)

### Community 0 - "StartupViewModel"
Cohesion: 0.09
Nodes (17): AndroidViewModel, Intent, Ready, RequestBatteryOptimization, SecurityBlocked, SecurityCheckState, ShowMessage, Splash (+9 more)

### Community 1 - "LicenseAdminService"
Cohesion: 0.09
Nodes (3): LicenseRepository, LicenseAdminService, PHPUnit\Framework\MockObject\MockObject

### Community 3 - "MainScreen.kt"
Cohesion: 0.06
Nodes (41): Job, StateFlow, PermissionPoller, AppError, Network, Server, Timeout, toAppError() (+33 more)

### Community 4 - "TextAlign"
Cohesion: 0.28
Nodes (21): DeleteDialog(), DialogBadge(), DialogButtonRow(), DialogContentCard(), DialogMessageText(), DialogTitle(), Color, ImageVector (+13 more)

### Community 5 - "ReportsCommonWidgets.kt"
Cohesion: 0.07
Nodes (41): ErrorState(), ImageVector, Modifier, Color, ImageVector, Modifier, StatisticsCard(), Ship (+33 more)

### Community 6 - "ChatMessageEntity"
Cohesion: 0.11
Nodes (8): For /graphify explain, For /graphify path, graphify reference: query, path, explain, Step 0 — Constrained query expansion (REQUIRED before traversal), Step 1 — Traversal, ChatDao, Flow, ChatMessageEntity

### Community 7 - "app.js"
Cohesion: 0.15
Nodes (25): buildRow(), cell(), confirmAction(), cleanup(), onClose(), onOk(), copyKey(), currentTheme() (+17 more)

### Community 8 - "ReportsRepository"
Cohesion: 0.11
Nodes (8): ComprehensiveAnalysisResponse, QuotaDetails, RealTimeDataResponse, ErrorResponse, HttpStatusException, Exception, Result, ReportsRepository

### Community 10 - "ShipDetailsScreen.kt"
Cohesion: 0.17
Nodes (24): WarningStatus, calculateWarningStatus(), ErrorStateCard(), ImageVector, ShipDetails(), ShipHeaderCard(), TabItem(), WarehouseQuotasTabs() (+16 more)

### Community 11 - "QuotaPercentageDialogSection.kt"
Cohesion: 0.27
Nodes (14): CalculationResult, AnimatedNumber(), Color, ImageVector, Modifier, lerp(), PercentageDisplay(), PercentageInputTab() (+6 more)

### Community 12 - "ReportsViewModel"
Cohesion: 0.06
Nodes (14): ComprehensiveAnalytics, GroupSortingMode, ALPHABETICAL, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC, QuotaSortingMode, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC (+6 more)

### Community 14 - "ApiServiceV2"
Cohesion: 0.07
Nodes (18): ApiResponse, ApiResponse2, ApiServiceV2, ActiveSessionResponse, CargoDeleteResponse, CargoInfoSearch, CargoSearchResponse, CargoStats (+10 more)

### Community 15 - "LoginAttemptLimiter"
Cohesion: 0.08
Nodes (9): lic_destroy_session(), lic_idle_timeout(), lic_is_authenticated(), lic_require_auth_json(), lic_require_auth_page(), Config, self, Csrf (+1 more)

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

### Community 21 - "QuotaValidationUseCase.kt"
Cohesion: 0.36
Nodes (4): QuotaValidationResult, InputValidationResult, QuotaValidationUseCase, TempTonnageValidationResult

### Community 22 - "ManageReportsScreen.kt"
Cohesion: 0.07
Nodes (55): DateClickableField(), DateRangePicker(), getDaysInPersianMonth(), isPersianLeapYear(), Color, Modifier, PersianDatePickerDialog(), addOneDayToPersianDate() (+47 more)

### Community 23 - "QuotaManagementContent.kt"
Cohesion: 0.15
Nodes (29): QuotaItem, AnalyticsStatChipMini(), CompactStatChip(), InfoChip(), IntegratedQuotaCard(), Color, ImageVector, Modifier (+21 more)

### Community 24 - "LoginScreen.kt"
Cohesion: 0.26
Nodes (16): CompactLoginLayout(), ExpandedLoginLayout(), IndustrialBrandingHeaderContent(), IndustrialFeatureBadge(), IndustrialFooterInfo(), IndustrialInputField(), IndustrialLoginFormCard(), androidx (+8 more)

### Community 26 - "CargoViewModel.kt"
Cohesion: 0.10
Nodes (12): Kilograms, CargoDialog, CargoUiState, CargoViewModelFactory, DuplicateConfirmation, Duplicates, StateFlow, T (+4 more)

### Community 27 - "TokenAuthenticator"
Cohesion: 0.19
Nodes (5): Authenticator, TokenAuthenticator, AuthErrorBody, TokenAuthenticatorTest, Route

### Community 29 - "SecurityScreen.kt"
Cohesion: 0.25
Nodes (13): AdvancedOrbitalScanner(), AnimatedUpdateIcon(), AnimatedWarningIcon(), DynamicPremiumBackground(), InteractiveExitButton(), Modifier, RetryButton(), SecurityBlockScreen() (+5 more)

### Community 33 - "AppModule.kt"
Cohesion: 0.09
Nodes (14): Flow, UserPreferencesStore, LoginRequest, LoginResponse, LogoutRequest, LogoutResponse, PasswordCheckResponse, PermissionSyncRequest (+6 more)

### Community 34 - "ReportsDomainCalculationsTest"
Cohesion: 0.13
Nodes (7): calculatePercentage(), calculateProgress(), format(), formatWeightWithDetail(), Context, QuotaWarningThresholds, ReportsDomainCalculationsTest

### Community 35 - "MessageBubble.kt"
Cohesion: 0.27
Nodes (13): extractShipInfoAndText(), com, replaceUsernamesWithFullNames(), ShipInfoModel, ChatInfoRow(), EditMessageDialog(), androidx, Color (+5 more)

### Community 36 - "UsersManager"
Cohesion: 0.07
Nodes (7): ApiManager, CONFIG, ModalManager, ThemeManager, UIManager, UsersManager, Utils

### Community 37 - "Cargo"
Cohesion: 0.32
Nodes (14): Cargo, CargoDetailsDialog(), Color, ImageVector, Modifier, ModernCargoDetailsGrid(), ModernCargoStatusSection(), ModernCargoStatusTimeline() (+6 more)

### Community 39 - "CameraSection.kt"
Cohesion: 0.21
Nodes (11): EnhancedNumberAnalyzer, extractNumber(), ImageProxy, InputImage, recognizeTextFromImage(), EnhancedCameraPreview(), ImageCapture, Color (+3 more)

### Community 40 - "Logger"
Cohesion: 0.10
Nodes (4): UtilityController, Logger, self, CargoService

### Community 41 - "CargoDetailsScreen.kt"
Cohesion: 0.23
Nodes (12): CargoCounterOperationScreen(), NavController, CargoDetailsRoute, cargoDetailsScreen(), NavController, navigateToCargoDetails(), CargoDetailsScreen(), FloatingActionButtonItem() (+4 more)

### Community 42 - "CargoViewModel"
Cohesion: 0.12
Nodes (3): SaveOrUpdateResponse, CargoViewModel, message

### Community 43 - "ATKCargoTheme"
Cohesion: 0.14
Nodes (11): ComponentStyles, Dimensions, Elevation, ATKCargoTheme, Motion, SemanticColors, AppShapes, Spacing (+3 more)

### Community 44 - "User"
Cohesion: 0.16
Nodes (18): User, EnhancedDeleteConfirmationDialog(), EnhancedForceLogoutDialog(), DashboardSummaryRow(), EmptyStateView(), EnterpriseUserCard(), getUserTypeDisplay(), androidx (+10 more)

### Community 45 - "MainActivity.kt"
Cohesion: 0.26
Nodes (6): Intent, MainActivity, StartupErrorScreen(), Bundle, ComponentActivity, ServerSyncingScreen()

### Community 46 - "ActiveQuotasContent.kt"
Cohesion: 0.16
Nodes (21): EmptySearchResult(), FilterChip(), FilterState, ALL, COMPLETED, PENDING, FlatQuotasContent(), GroupedShipsContent() (+13 more)

### Community 47 - "ActiveShipInfo"
Cohesion: 0.28
Nodes (12): ActiveShipInfo, GroupedShipList(), Color, QuotaCard(), ShipGroup(), ShipHeader(), WarehouseCard(), WarehouseList() (+4 more)

### Community 48 - "MicroCache"
Cohesion: 0.09
Nodes (5): MicroCache, AppApiCacheKeys, AuditLogger, QuotaService, ShipService

### Community 49 - "SelectInfoScreen.kt"
Cohesion: 0.23
Nodes (16): Result, validateServerSession(), SessionCheckRequest, RealTimeLoadingData, AnimatedHeader(), GroupedShipList(), Color, Modifier (+8 more)

### Community 52 - "ReportModels.kt"
Cohesion: 0.16
Nodes (18): AnalyticsData, DateInfo, ExistingQuota, ExitDateInfo, FilteredSummary, QuotaCompletionAnalysis, QuotaStatusDetails, toTon() (+10 more)

### Community 53 - "CargoDetailsComponents.kt"
Cohesion: 0.22
Nodes (15): QuotaInfo, toDomain(), toDto(), CargoInfoCard(), CargoListSection(), GroupStats(), InfoChips(), InfoGrid() (+7 more)

### Community 54 - "ChatScreen.kt"
Cohesion: 0.17
Nodes (22): ChatUiItem, getAdaptiveBubbleColor(), getChatBackgroundColor(), getDateHeaderColor(), Header, Color, Message, ChatScreen() (+14 more)

### Community 55 - "UpdateDialog.kt"
Cohesion: 0.31
Nodes (12): UpdateInfo, DownloadActionButtons(), DownloadingState(), DownloadProgressInfo(), DownloadSizeInfo(), DownloadSpeedInfo(), ErrorState(), ModernUpdateActionSection() (+4 more)

### Community 56 - "ApiException"
Cohesion: 0.09
Nodes (16): App\Core\Database, App\Core\DatabaseManager, App\Core\MicroCache, App\Core\Request, App\Core\Response, App\Enums\CargoConfirmStatus, App\Enums\CargoStatus, App\Exceptions\ApiException (+8 more)

### Community 57 - "SplashScreen.kt"
Cohesion: 0.27
Nodes (8): VazirmatnFontFamily, AnimatedBrandTitle(), SplashScreen(), SplashScreenConstants, SplashScreenContent(), SplashSubtitleBadge(), SplashVersionBadge(), FontFamily

### Community 58 - "CoroutineScope"
Cohesion: 0.29
Nodes (6): AtkCargoApplication, CrashReporter, Context, PendingCrashReport, Application, CoroutineScope

### Community 59 - "RealTimeLoadingBottomSheet.kt"
Cohesion: 0.35
Nodes (9): ShiftInfo, DialogHeader(), Color, Modifier, RealTimeLoadingBottomSheet(), RealTimeShiftNavigation(), RefreshOverlay(), ShipCard() (+1 more)

### Community 60 - "RetrofitClient"
Cohesion: 0.26
Nodes (9): FloatTypeAdapter, Context, OkHttpClient, RetrofitClient, HttpLoggingInterceptor, JsonReader, JsonWriter, Retrofit (+1 more)

### Community 61 - "LoadingNotificationWorker"
Cohesion: 0.29
Nodes (8): KoinComponent, UserPreferencesManager, LoadingNotificationWorker, TypeToken, CoroutineWorker, RealTimeLoadingData, ReportsRepository, ShiftInfo

### Community 63 - "formatNumber"
Cohesion: 0.19
Nodes (22): QuotaCompletionData, QuotaGroupingMode, BY_CARGO_OWNER, BY_CARRIER, BY_SHIP, buildQuotaGroups(), QuotaGroup, formatNumber() (+14 more)

### Community 66 - "Color.kt"
Cohesion: 0.21
Nodes (12): CompactStatChipTest, UpdateUserRequest, DesignTextField(), EnhancedAddUserDialog(), EnhancedEditUserDialog(), getUserTypes(), androidx, KeyboardOptions (+4 more)

### Community 67 - "RegisterCargoScreen.kt"
Cohesion: 0.16
Nodes (14): CargoConfirmStatus, AWAITING_CONFIRMATION, CONFIRMED, PENDING, CargoStatus, ENTERED, EXITED, DuplicateTrackingNumbersDialog() (+6 more)

### Community 68 - "AuthViewModel"
Cohesion: 0.17
Nodes (8): AuthViewModel, Error, Idle, StateFlow, ViewModel, Loading, LoginUiState, Success

### Community 69 - "update.md"
Cohesion: 0.50
Nodes (3): For --cluster-only, For --update (incremental re-extraction), graphify reference: incremental update and cluster-only

### Community 70 - "ChatPreferencesStore"
Cohesion: 0.31
Nodes (6): ChatPreferencesStore, Flow, AdminChatRoute, adminChatScreen(), NavController, navigateToAdminChat()

### Community 71 - "Typography.kt"
Cohesion: 0.83
Nodes (3): createTypography(), TextStyle, Typography

### Community 72 - "TokenStore"
Cohesion: 0.17
Nodes (4): OkHttpClient, TokenRefresher, TokenStore, RefreshTokenResponse

### Community 73 - "ActiveQuotasGroupedComponents.kt"
Cohesion: 0.50
Nodes (7): formatNumber(), extractLastDigits(), Color, QuotaItem(), ShipCard(), ShipHeader(), WarehouseSection()

### Community 74 - "InitialInfo"
Cohesion: 0.18
Nodes (5): QuotaRepository, CargoInfoResponse, InitialInfo, QuotaStatusResponse, Parcelable

### Community 75 - "ExitStatusDialog.kt"
Cohesion: 0.83
Nodes (3): AnimatedCounter(), ExitStatusDialog(), ImageVector

### Community 76 - "ApiV2Routes"
Cohesion: 0.11
Nodes (3): ApiV2Routes, ViewModel, ProfileViewModel

### Community 77 - "MessageInputArea"
Cohesion: 0.52
Nodes (6): rotateIcon(), Color, com, MessageInputArea(), QuotaSelectionDialog(), ShipSelectionDialog()

### Community 78 - "ChatRepository"
Cohesion: 0.11
Nodes (11): ChatMessage, ChatMessagesResponse, SendMessageRequest, SendMessageResponse, UnreadCountResponse, DeleteMessageRequest, EditMessageRequest, ChatRepository (+3 more)

### Community 79 - "Recommended Architecture"
Cohesion: 0.67
Nodes (3): Recommended Architecture, سمت بک‌اند, قواعد قابل اجرا

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

### Community 88 - "RealTimeLoadingCardSection.kt"
Cohesion: 0.43
Nodes (6): CompactInfo(), Color, ImageVector, Modifier, RealTimeLoadingCard(), StatisticItem()

### Community 91 - "AppDatabase"
Cohesion: 0.47
Nodes (3): AppDatabase, Context, RoomDatabase

### Community 93 - "Migrations"
Cohesion: 0.33
Nodes (5): Migrations, migrationهای اعمال‌شده تاکنون, قرارداد نام‌گذاری, نحوه‌ی اجرا, وضعیت schema.sql

### Community 94 - ".onError"
Cohesion: 0.15
Nodes (8): CreateUserRequest, DeleteUserRequest, ForceLogoutRequest, ForceLogoutResponse, ViewModel, UserManagementViewModel, CargoInfoRow(), ImageCaptureException

### Community 96 - "پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13)"
Cohesion: 0.22
Nodes (8): ساختار فعلی (مستند شده در این جلسه), ساختار هدف, مراحل اجرا (ترتیب دقیق مهم است), معیار موفقیت, پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13), پیش‌نیازهای جانبی که باید قبل از این کار حل شوند, چرا فقط .htaccess کافی نیست, چرا لازم است

### Community 99 - "FontWeight"
Cohesion: 0.24
Nodes (6): ConfirmationDialog(), ImageVector, EmptyState(), ImageVector, Modifier, FontWeight

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

### Community 119 - "quota_details/QuotaCardComponents.kt"
Cohesion: 0.14
Nodes (24): ActionButtonTest, QuotaEditData, QuotaPercentageData, DeleteQuotaDialog(), androidx, Color, QuotaConfirmDialog(), ToggleQuotaStatusDialog() (+16 more)

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

### Community 128 - "VoucherDetailsDialogSection.kt"
Cohesion: 0.17
Nodes (20): VoucherDetail, persianDateFormat(), DateTimePicker(), Modifier, PersianDateItem(), TimePickerDialog(), EmptyVoucherList(), Modifier (+12 more)

### Community 129 - "نقاط قوت"
Cohesion: 0.29
Nodes (7): [LOW] PHPStan فایل‌های ریشه و پنل مدیریتی را پوشش نمی‌دهد, [MEDIUM] `AppApiController` عمدتاً یک Middle Man است, [MEDIUM] دو مسیر دسترسی به دیتابیس (PDO و mysqli) به‌صورت موازی, [MEDIUM] رمزگذاری خروجی ناسازگار بین دو مسیر نوشتن, [MEDIUM] گیت احراز هویت دوبار پیاده‌سازی شده است, PHP Backend Audit, نقاط قوت

### Community 130 - "ProfileMenu.kt"
Cohesion: 0.28
Nodes (11): CompactSwitch(), Color, ImageVector, Modifier, NotificationSettingRow(), ActionButtons(), getUserTypeDisplay(), Color (+3 more)

### Community 132 - "حذف/محدودسازی phpMyAdmin روی سرور production (Phase 1, آیتم #۲)"
Cohesion: 0.40
Nodes (4): حذف/محدودسازی phpMyAdmin روی سرور production (Phase 1, آیتم #۲), راستی‌آزمایی نهایی, گزینه‌ی ۱ — حذف کامل (توصیه‌شده), گزینه‌ی ۲ — اگر حذف فوری ممکن نیست

### Community 133 - "AnalyticsController"
Cohesion: 0.09
Nodes (14): App\Core\Logger, Logger, gregorian_to_jalali(), jalali_to_gregorian(), jcheckdate(), jdate(), jdate_words(), jgetdate() (+6 more)

### Community 134 - "QuotaDetailsScreen.kt"
Cohesion: 0.24
Nodes (11): ApiQuotaDetails, adjustColorForTheme(), ColorSelector, Color, ProgressBar(), QuotaAdditionalInfo(), QuotaDetails(), QuotaInfoCards() (+3 more)

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
Cohesion: 0.07
Nodes (9): Flow, T, UserPreferencesManager, Flow, UserSettingsStore, DataStore, ThemeColorOption, ThemeColorPickerRow() (+1 more)

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

### Community 160 - "وضعیت: **بسیار خوب** — این نقطه‌ی قوت پروژه است"
Cohesion: 0.50
Nodes (4): Dependency Audit, [LOW] دو استک سریال‌سازی JSON هم‌زمان, [LOW] نام مستعارهای version catalog با نسخه‌های واقعی نمی‌خوانند, وضعیت: **بسیار خوب** — این نقطه‌ی قوت پروژه است

### Community 161 - "وضعیت"
Cohesion: 0.50
Nodes (4): Logging & Observability, [MEDIUM] لاگ‌های debug محتوای پیام چت را ثبت می‌کنند, [MEDIUM] هیچ مانیتورینگ یا هشدار خودکاری در تولید وجود ندارد, وضعیت

### Community 162 - "Animation Audit"
Cohesion: 0.67
Nodes (3): Animation Audit, [LOW] اسپلش با تأخیر ثابت به‌جای انیمیشن مبتنی بر پیشرفت, وضعیت کلی

### Community 163 - "Project Overview"
Cohesion: 0.67
Nodes (3): Project Overview, استک, ساختار ماژول‌ها

### Community 178 - "PHP/composer.json"
Cohesion: 0.09
Nodes (21): autoload, autoload-dev, psr-4, psr-4, config, optimize-autoloader, sort-packages, description (+13 more)

### Community 188 - "InitialInfoScreen"
Cohesion: 0.10
Nodes (34): CheckExistenceRequest, formatNumber(), isValidPersianText(), isValidQuotaNumber(), isValidShipName(), isValidWarehouseName(), isValidWeight(), ConfirmationDialog() (+26 more)

### Community 195 - "UpdateManager"
Cohesion: 0.13
Nodes (6): DownloadProgress, Context, Job, StateFlow, UpdateManager, VersionCheckResult

### Community 197 - "LoadingState"
Cohesion: 0.47
Nodes (6): Error, Idle, Loading, LoadingState, Success, UiState

### Community 204 - "QuotaSelectionDialog.kt"
Cohesion: 0.42
Nodes (10): MatchingQuota, DialogHeader(), findDifferentFields(), InactiveIndicator(), ImageVector, QuotaDetailItem(), QuotaDetails(), QuotaHeader() (+2 more)

### Community 206 - "QuotaEntryDialog.kt"
Cohesion: 0.57
Nodes (7): FocusRequester, QuotaEntryActions(), QuotaEntryContent(), QuotaEntryDialog(), QuotaEntryPalette, rememberQuotaEntryPalette(), validateAndSubmit()

### Community 208 - "CargoInfoDetailsDialogSection.kt"
Cohesion: 0.38
Nodes (11): CargoInfoRequest, CargoDetailsPalette, CargoDetailTabItem(), CargoInfoDetailsDialog(), DetailInfoRow(), ImageVector, Modifier, MainInfoTabContent() (+3 more)

### Community 209 - "CargoCounterScreen.kt"
Cohesion: 0.15
Nodes (22): CargoCounterScreen(), CargoCounterViewModel, CargoSnackbarMessage, filterShipsByTab(), com, NavController, StateFlow, ViewModel (+14 more)

### Community 221 - "secrets.cpp"
Cohesion: 0.34
Nodes (16): decryptXor(), JNI_OnLoad(), n0(), n1(), n2(), n3(), n4(), n5() (+8 more)

### Community 257 - "BootReceiver.kt"
Cohesion: 0.39
Nodes (6): BootReceiver, BroadcastReceiver, Context, Intent, KoinComponent, UserPreferencesManager

### Community 260 - "CryptoManager"
Cohesion: 0.33
Nodes (4): CryptoManager, ByteArray, Cipher, SecretKey

### Community 262 - "Database"
Cohesion: 0.11
Nodes (7): mysqli, mysqli_stmt, PDO, Database, PDO, self, DatabaseManager

### Community 265 - "MessageType"
Cohesion: 0.16
Nodes (14): Modifier, StatusSnackbar(), MessageType, ERROR, SUCCESS, WARNING, CargoSnackbarQueue, StateFlow (+6 more)

### Community 266 - "ChatNotificationWorker.kt"
Cohesion: 0.43
Nodes (3): ChatNotificationWorker, KoinComponent, ListenableWorker

### Community 272 - "Request"
Cohesion: 0.08
Nodes (6): App\Core\AuthenticatesRequests, lic_required_id(), OnlineUsersController, UserController, Request, Response

### Community 275 - "NotificationActionReceiver.kt"
Cohesion: 0.52
Nodes (4): BroadcastReceiver, Context, Intent, NotificationActionReceiver

### Community 276 - "AuthenticatesRequests.php"
Cohesion: 0.48
Nodes (6): App\Services\PermissionService, App\Services\SessionService, enforceMinAppVersion(), requireAuthenticatedSession(), requirePermission(), sendAuthErrorResponse()

### Community 282 - "Quota"
Cohesion: 0.20
Nodes (18): Quota, WarehouseQuotaGroupingMode, BY_CARGO_OWNER, BY_SHIPPING_COMPANY, BY_WAREHOUSE, buildQuotasShareText(), shareQuotasData(), MinimalQuotaCard() (+10 more)

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
- **55 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReportsViewModel` connect `ReportsViewModel` to `AppModule.kt`, `MainScreen.kt`, `ReportsCommonWidgets.kt`, `QuotaDetailsScreen.kt`, `LoadingState`, `ShipDetailsScreen.kt`, `ComprehensiveAnalyticsDialog.kt`, `ReportModels.kt`, `ManageReportsScreen.kt`, `QuotaManagementContent.kt`, `Quota`, `RealTimeLoadingBottomSheet.kt`, `formatNumber`?**
  _High betweenness centrality (0.038) - this node is a cross-community bridge._
- **Why does `UpdateManager` connect `UpdateManager` to `AppModule.kt`, `CryptoManager`, `MainActivity.kt`, `DownloadState`, `UpdateDialog.kt`?**
  _High betweenness centrality (0.017) - this node is a cross-community bridge._
- **Why does `ATKCargoTheme` connect `ATKCargoTheme` to `LoginScreen`?**
  _High betweenness centrality (0.016) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `InputValidator` (e.g. with `.getShipQuotasRemaining()` and `.handleQuotaRemaining()`) actually correct?**
  _`InputValidator` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `وضعیت کلی`, `مهم‌ترین ریسک‌های امنیتی`, `مهم‌ترین مشکلات Performance` to the rest of the system?**
  _290 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `StartupViewModel` be split into smaller, more focused modules?**
  _Cohesion score 0.09047619047619047 - nodes in this community are weakly interconnected._
- **Should `LicenseAdminService` be split into smaller, more focused modules?**
  _Cohesion score 0.09090909090909091 - nodes in this community are weakly interconnected._