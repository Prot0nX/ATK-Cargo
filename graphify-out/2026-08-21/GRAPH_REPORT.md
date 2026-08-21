# Graph Report - ATK-Cargo  (2026-08-21)

## Corpus Check
- 338 files · ~385,192 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2805 nodes · 5753 edges · 219 communities (172 shown, 47 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 158 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `fad71860`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- StartupViewModel
- LicenseAdminService
- LicenseAdminServiceTest
- MainScreen.kt
- TextAlign
- ShipSortingMode
- ChatMessageEntity
- app.js
- ReportsRepository
- MicroCache
- QuotaWarningDialog.kt
- FontWeight
- ReportsViewModel
- JalaliDateUtilsTest
- ApiServiceV2
- Config
- ChatViewModel
- DEEP_CODE_REVIEW.md
- PermissionService
- ShipsListScreen.kt
- SessionService
- CargoDetailsComponents.kt
- ManageReportsScreen.kt
- QuotaManagementContent.kt
- LoginScreen.kt
- SessionRepository
- CargoViewModel.kt
- TokenAuthenticator
- Medium Issues
- SecurityScreen.kt
- SessionServiceTest
- LoadingNotificationService
- SecurityAlerter
- AuthModels.kt
- QuotaDetailsScreen.kt
- MessageBubble.kt
- UsersManager
- Cargo
- CargoRepository
- preprocessImage
- Logger
- CargoDetailsScreen.kt
- CargoViewModel
- ATKCargoTheme
- User
- MainActivity.kt
- ActiveQuotasContent.kt
- ActiveShipInfo
- Executive Summary
- SelectInfoScreen.kt
- LicenseController
- LoginAttemptLimiterTest
- WarehouseDetailsScreen.kt
- dialogs/QuotaCardComponents.kt
- ChatScreen.kt
- UpdateDialog.kt
- ApiException
- SplashScreen.kt
- CrashReporter.kt
- RealTimeLoadingBottomSheet.kt
- RetrofitClient
- ProfileMenu.kt
- HomeScreen.kt
- formatNumber
- TokenRefresherTest
- InitialInfo
- UserManagementDialogsSection.kt
- Secrets
- AuthViewModel
- ChatToolbar.kt
- ChatPreferencesStore
- UserPreferencesStore
- TokenStore
- ActiveQuotasGroupedComponents.kt
- Final Recommendations
- AppError
- ApiV2Routes
- MessageInputArea
- ChatRepository
- ReportsNavigation.kt
- ComprehensiveAnalyticsDialog.kt
- DownloadState
- ReportsCommonWidgets.kt
- استانداردسازی کامنت‌های چندخطی به تک‌خطی (کلاینت + سرور)
- AppModule.kt
- Code Smells شناسایی‌شده
- InitialInfoFormComponents.kt
- HomeNotificationSettingRow.kt
- RealTimeLoadingCardSection.kt
- Color.kt
- graphify reference: query, path, explain
- AppDatabase
- AnimationManager
- Migrations
- .onError
- TokenRefresher
- پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13)
- UserRepository
- AnimationManager
- EmptyState.kt
- ExitStatusDialog.kt
- High Issues
- ColorPicker.kt
- ConfirmationDialog.kt
- ReportModels.kt
- HardwarePerformanceEvaluator
- Android Audit
- RegisterCargoScreen.kt
- DataModel.kt
- API Audit
- Testing Audit
- Jetpack Compose Audit
- AppNotificationManager
- quota_details/QuotaCardComponents.kt
- چک‌لیست فعال‌سازی `STRICT_TRANS_TABLES` (DEEP_CODE_AUDIT.md #Phase4.10)
- What You Must Do When Invoked
- Performance Audit
- Prioritized Action Plan
- ComponentDefaults.kt
- VoucherDetailsDialogSection.kt
- حذف/محدودسازی phpMyAdmin روی سرور production (Phase 1, آیتم #۲)
- AnalyticsController
- ColorSelector
- Animation Audit
- Authentication & Authorization
- Database Audit
- Error Handling
- Kotlin Audit
- UserPreferencesManager
- SecurityVerifier
- PHP Backend Audit
- چرخش رازهای `secrets.cpp` (Phase 1, آیتم #۱)
- Constants.kt
- AppApiController
- InputValidator
- PHP/composer.json
- Router
- InitialInfoScreen
- QuotaCalculatorTest
- UpdateManager
- LoadingState
- QuotaSelectionDialog.kt
- QuotaEntryDialog.kt
- SearchBar.kt
- CargoInfoDetailsDialogSection.kt
- CargoCounterScreen.kt
- ShipDetailsScreen.kt
- secrets.cpp
- SessionManager
- SessionManager
- CoroutineScope
- CryptoManager
- Database
- MessageType
- ChatNotificationWorker.kt
- Request
- NotificationActionReceiver.kt
- AuthenticatesRequests.php
- Quota
- ChatController
- LoginAttemptLimiter
- StartupState
- StartupController
- PermissionPoller
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
3. `InputValidator` - 61 edges
4. `ApiServiceV2` - 59 edges
5. `formatNumber()` - 45 edges
6. `UserPreferencesManager` - 41 edges
7. `Logger` - 40 edges
8. `Request` - 40 edges
9. `ActiveShipInfo` - 40 edges
10. `UpdateManager` - 38 edges

## Surprising Connections (you probably didn't know these)
- `PersianDatePickerDialog()` --calls--> `FilterChip()`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/core/ui/components/DateRangePicker.kt → feature/cargo-workflow/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/ActiveQuotasContent.kt
- `LoadingNotificationService` --references--> `ReportsRepository`  [EXTRACTED]
  app/src/main/java/com/atk/atk_cargo/api/LoadingNotificationService.kt → feature/reports/src/main/java/com/atk/atk_cargo/data/repository/ReportsRepository.kt
- `UserPreferencesManager` --implements--> `UserPreferencesStore`  [EXTRACTED]
  app/src/main/java/com/atk/atk_cargo/api/UserPreferencesManager.kt → core/domain/src/main/java/com/atk/atk_cargo/domain/session/UserPreferencesStore.kt
- `UserPreferencesManager` --implements--> `UserSettingsStore`  [EXTRACTED]
  app/src/main/java/com/atk/atk_cargo/api/UserPreferencesManager.kt → core/domain/src/main/java/com/atk/atk_cargo/domain/session/UserSettingsStore.kt
- `UserPreferencesManager` --implements--> `TokenStore`  [EXTRACTED]
  app/src/main/java/com/atk/atk_cargo/api/UserPreferencesManager.kt → core/network/src/main/java/com/atk/atk_cargo/api/TokenStore.kt

## Import Cycles
- None detected.

## Communities (219 total, 47 thin omitted)

### Community 0 - "StartupViewModel"
Cohesion: 0.13
Nodes (9): AndroidViewModel, Flow, Intent, StateFlow, RequestBatteryOptimization, SecurityCheckState, ShowMessage, StartupEvent (+1 more)

### Community 3 - "MainScreen.kt"
Cohesion: 0.13
Nodes (18): MainScreen(), RouteTransitions, standardTransitions(), CargoCounterRoute, navigateToCargoCounter(), InitialInfoRoute, navigateToInitialInfo(), navigateToSelectInfo() (+10 more)

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
Cohesion: 0.06
Nodes (12): QuotaRepository, CargoInfoResponse, ComprehensiveAnalysisResponse, QuotaDetails, QuotaStatusResponse, RealTimeDataResponse, CargoViewModelTest, ErrorResponse (+4 more)

### Community 9 - "MicroCache"
Cohesion: 0.07
Nodes (5): MicroCache, AppApiCacheKeys, AuditLogger, QuotaService, UserService

### Community 10 - "QuotaWarningDialog.kt"
Cohesion: 0.26
Nodes (16): WarningStatus, CompactInfoSection(), DialogHeader(), Color, ImageVector, Modifier, LoadingActionButton(), NavigationButton() (+8 more)

### Community 11 - "FontWeight"
Cohesion: 0.14
Nodes (21): createTypography(), CalculationResult, QuotaPercentageData, AnimatedNumber(), DialogHeader(), Color, ImageVector, Modifier (+13 more)

### Community 12 - "ReportsViewModel"
Cohesion: 0.07
Nodes (10): ComprehensiveAnalytics, QuotaSortingMode, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC, AndroidViewModel, Color, Job, StateFlow (+2 more)

### Community 14 - "ApiServiceV2"
Cohesion: 0.08
Nodes (18): ApiResponse2, ApiServiceV2, ActiveSessionResponse, CargoDeleteResponse, CargoInfoSearch, CargoSearchResponse, CargoStats, CheckExistenceRequest (+10 more)

### Community 15 - "Config"
Cohesion: 0.10
Nodes (8): lic_destroy_session(), lic_idle_timeout(), lic_is_authenticated(), lic_require_auth_json(), lic_require_auth_page(), Config, self, Csrf

### Community 16 - "ChatViewModel"
Cohesion: 0.10
Nodes (7): ChatViewModel, ChatViewModelFactory, com, StateFlow, T, ViewModel, ViewModelProvider

### Community 17 - "DEEP_CODE_REVIEW.md"
Cohesion: 0.10
Nodes (19): Dependency Audit, Final Score, Logging & Observability, [LOW] Koin 3.5.6, [LOW] `lateinit var currentDownloadFile` بدون گارد کامل, [MEDIUM] Compose BOM عملاً بی‌اثر است, [MEDIUM] آدرس پایه به مسیر `test_api/` اشاره می‌کند, [MEDIUM] نبود مانیتورینگ خودکار (+11 more)

### Community 18 - "PermissionService"
Cohesion: 0.08
Nodes (5): mysqli_stmt, DatabaseManager, PermissionRepository, PermissionService, PermissionServiceTest

### Community 19 - "ShipsListScreen.kt"
Cohesion: 0.25
Nodes (13): ErrorState(), ImageVector, Modifier, ImageVector, Modifier, ShipCard(), ShipCardContent(), ShipSortingModeButton() (+5 more)

### Community 21 - "CargoDetailsComponents.kt"
Cohesion: 0.17
Nodes (17): QuotaInfo, QuotaValidationResult, InputValidationResult, QuotaValidationUseCase, TempTonnageValidationResult, CargoInfoCard(), CargoListSection(), GroupStats() (+9 more)

### Community 22 - "ManageReportsScreen.kt"
Cohesion: 0.08
Nodes (54): DateClickableField(), DateRangePicker(), getDaysInPersianMonth(), isPersianLeapYear(), Color, Modifier, PersianDatePickerDialog(), addOneDayToPersianDate() (+46 more)

### Community 23 - "QuotaManagementContent.kt"
Cohesion: 0.22
Nodes (18): AdvancedFiltersDialog(), EmptyQuotaState(), FilterOptionsCard(), Modifier, QuotaFilters, QuotaManagementContent(), QuotaTabContent(), QuotaTabItem() (+10 more)

### Community 24 - "LoginScreen.kt"
Cohesion: 0.19
Nodes (22): CompactLoginLayout(), ExpandedLoginLayout(), IndustrialBrandingHeaderContent(), IndustrialFeatureBadge(), IndustrialFooterInfo(), IndustrialInputField(), IndustrialLoginFormCard(), androidx (+14 more)

### Community 26 - "CargoViewModel.kt"
Cohesion: 0.10
Nodes (12): Kilograms, CargoDialog, CargoUiState, CargoViewModelFactory, DuplicateConfirmation, Duplicates, StateFlow, T (+4 more)

### Community 27 - "TokenAuthenticator"
Cohesion: 0.19
Nodes (5): Authenticator, TokenAuthenticator, AuthErrorBody, TokenAuthenticatorTest, Route

### Community 28 - "Medium Issues"
Cohesion: 0.12
Nodes (16): Critical Issues, [CRITICAL] کلیدهای API و لایسنس با XOR تک‌بایتی محافظت شده و در git ذخیره‌اند, Low Issues, [LOW] استفاده از `!!` روی state در Composable, [LOW] تابع native تعریف‌شده اما هرگز استفاده نشده, [LOW] فایل اطلاعات سرور در `.gitignore` نیست, [MEDIUM] endpointهای لایسنس بدون احراز هویت و بدون rate limit, Medium Issues (+8 more)

### Community 29 - "SecurityScreen.kt"
Cohesion: 0.25
Nodes (13): AdvancedOrbitalScanner(), AnimatedUpdateIcon(), AnimatedWarningIcon(), DynamicPremiumBackground(), InteractiveExitButton(), Modifier, RetryButton(), SecurityBlockScreen() (+5 more)

### Community 31 - "LoadingNotificationService"
Cohesion: 0.22
Nodes (6): Context, Intent, KoinComponent, LoadingNotificationService, IBinder, Service

### Community 33 - "AuthModels.kt"
Cohesion: 0.09
Nodes (16): Result, validateServerSession(), ForceLogoutResponse, LoginRequest, LoginResponse, LogoutRequest, LogoutResponse, PasswordCheckResponse (+8 more)

### Community 34 - "QuotaDetailsScreen.kt"
Cohesion: 0.11
Nodes (14): ApiQuotaDetails, calculatePercentage(), calculateProgress(), format(), formatWeightWithDetail(), Context, QuotaWarningThresholds, ProgressBar() (+6 more)

### Community 35 - "MessageBubble.kt"
Cohesion: 0.24
Nodes (14): extractShipInfoAndText(), com, replaceUsernamesWithFullNames(), rotateIcon(), ShipInfoModel, ChatInfoRow(), EditMessageDialog(), androidx (+6 more)

### Community 36 - "UsersManager"
Cohesion: 0.07
Nodes (7): ApiManager, CONFIG, ModalManager, ThemeManager, UIManager, UsersManager, Utils

### Community 37 - "Cargo"
Cohesion: 0.32
Nodes (14): Cargo, CargoDetailsDialog(), Color, ImageVector, Modifier, ModernCargoDetailsGrid(), ModernCargoStatusSection(), ModernCargoStatusTimeline() (+6 more)

### Community 38 - "CargoRepository"
Cohesion: 0.06
Nodes (3): CargoController, CargoRepository, CargoService

### Community 39 - "preprocessImage"
Cohesion: 0.14
Nodes (20): adaptiveThresholding(), applyContrastEnhancement(), enhanceEdges(), ImageProxy, InputImage, medianFilter(), preprocessImage(), EnhancedNumberAnalyzer (+12 more)

### Community 40 - "Logger"
Cohesion: 0.15
Nodes (3): AuthController, Logger, self

### Community 41 - "CargoDetailsScreen.kt"
Cohesion: 0.23
Nodes (12): CargoCounterOperationScreen(), NavController, CargoDetailsRoute, cargoDetailsScreen(), NavController, navigateToCargoDetails(), CargoDetailsScreen(), FloatingActionButtonItem() (+4 more)

### Community 42 - "CargoViewModel"
Cohesion: 0.11
Nodes (3): SaveOrUpdateResponse, CargoViewModel, message

### Community 43 - "ATKCargoTheme"
Cohesion: 0.09
Nodes (22): ColorScheme, AdaptiveLayoutConfig, rememberAdaptiveLayoutConfig(), WindowSizeClass, COMPACT, EXPANDED, MEDIUM, buildAppDarkColorScheme() (+14 more)

### Community 44 - "User"
Cohesion: 0.16
Nodes (18): User, EnhancedDeleteConfirmationDialog(), EnhancedForceLogoutDialog(), DashboardSummaryRow(), EmptyStateView(), EnterpriseUserCard(), getUserTypeDisplay(), androidx (+10 more)

### Community 45 - "MainActivity.kt"
Cohesion: 0.26
Nodes (6): Intent, MainActivity, StartupErrorScreen(), Bundle, ComponentActivity, ServerSyncingScreen()

### Community 46 - "ActiveQuotasContent.kt"
Cohesion: 0.15
Nodes (22): EmptySearchResult(), FilterChip(), FilterState, ALL, COMPLETED, PENDING, FlatQuotasContent(), GroupedShipsContent() (+14 more)

### Community 47 - "ActiveShipInfo"
Cohesion: 0.35
Nodes (10): ActiveShipInfo, GroupedShipList(), Color, QuotaCard(), ShipGroup(), ShipHeader(), WarehouseCard(), WarehouseList() (+2 more)

### Community 48 - "Executive Summary"
Cohesion: 0.13
Nodes (15): Architecture Overview, Deep Code Audit Report, Executive Summary, Overall Score, Overall Score: **6.1 / 10**, Production Readiness, Project Overview, Technical Debt (+7 more)

### Community 49 - "SelectInfoScreen.kt"
Cohesion: 0.22
Nodes (16): RealTimeLoadingData, refreshData(), AnimatedHeader(), GroupedShipList(), Color, Modifier, NavController, Result (+8 more)

### Community 52 - "WarehouseDetailsScreen.kt"
Cohesion: 0.22
Nodes (15): FilteredSummary, Warehouse, persianDateFormat(), DateTimePicker(), Modifier, PersianDateItem(), TimePickerDialog(), Modifier (+7 more)

### Community 53 - "dialogs/QuotaCardComponents.kt"
Cohesion: 0.29
Nodes (13): QuotaItem, AnalyticsStatChipMini(), CompactStatChip(), InfoChip(), IntegratedQuotaCard(), Color, ImageVector, Modifier (+5 more)

### Community 54 - "ChatScreen.kt"
Cohesion: 0.27
Nodes (12): ChatUiItem, getChatBackgroundColor(), getDateHeaderColor(), Header, Color, Message, ChatScreen(), DateHeader() (+4 more)

### Community 55 - "UpdateDialog.kt"
Cohesion: 0.33
Nodes (11): DownloadActionButtons(), DownloadingState(), DownloadProgressInfo(), DownloadSizeInfo(), DownloadSpeedInfo(), ErrorState(), ModernUpdateActionSection(), ModernUpdateContent() (+3 more)

### Community 56 - "ApiException"
Cohesion: 0.13
Nodes (6): App\Enums\CargoConfirmStatus, App\Enums\CargoStatus, Exception, ApiException, ConflictException, DatabaseException

### Community 57 - "SplashScreen.kt"
Cohesion: 0.27
Nodes (8): VazirmatnFontFamily, AnimatedBrandTitle(), SplashScreen(), SplashScreenConstants, SplashScreenContent(), SplashSubtitleBadge(), SplashVersionBadge(), FontFamily

### Community 58 - "CrashReporter.kt"
Cohesion: 0.52
Nodes (3): CrashReporter, Context, PendingCrashReport

### Community 59 - "RealTimeLoadingBottomSheet.kt"
Cohesion: 0.35
Nodes (9): ShiftInfo, DialogHeader(), Color, Modifier, RealTimeLoadingBottomSheet(), RealTimeShiftNavigation(), RefreshOverlay(), ShipCard() (+1 more)

### Community 60 - "RetrofitClient"
Cohesion: 0.26
Nodes (9): FloatTypeAdapter, Context, OkHttpClient, RetrofitClient, HttpLoggingInterceptor, JsonReader, JsonWriter, Retrofit (+1 more)

### Community 61 - "ProfileMenu.kt"
Cohesion: 0.19
Nodes (10): Flow, UserSettingsStore, ThemeColorOption, ThemeColorPickerRow(), ActionButtons(), getUserTypeDisplay(), Color, ProfileMenu() (+2 more)

### Community 62 - "HomeScreen.kt"
Cohesion: 0.23
Nodes (13): Network, MenuItem, getMenuItemsForUserType(), CategorizedMenuGrid(), CategoryHeader(), CompactMenuCard(), Header(), HomeScreen() (+5 more)

### Community 63 - "formatNumber"
Cohesion: 0.19
Nodes (22): QuotaCompletionData, QuotaGroupingMode, BY_CARGO_OWNER, BY_CARRIER, BY_SHIP, buildQuotaGroups(), QuotaGroup, formatNumber() (+14 more)

### Community 65 - "InitialInfo"
Cohesion: 0.14
Nodes (15): toDomain(), toDto(), InitialInfo, ExistenceChecked, ExistenceCheckStatus, EXISTS, NOT_EXISTS, PARTIAL_MATCH (+7 more)

### Community 66 - "UserManagementDialogsSection.kt"
Cohesion: 0.38
Nodes (10): DesignTextField(), EnhancedAddUserDialog(), EnhancedEditUserDialog(), getUserTypes(), androidx, KeyboardOptions, Modifier, UserTypeOptionHorizontal() (+2 more)

### Community 68 - "AuthViewModel"
Cohesion: 0.27
Nodes (3): AuthViewModel, StateFlow, ViewModel

### Community 69 - "ChatToolbar.kt"
Cohesion: 0.35
Nodes (10): getAdaptiveBubbleColor(), AdvancedColorPickerDialog(), ChatSettingsDialogEnhanced(), ChatSettingsPreviewRefined(), Color, ImageVector, Modifier, LazyRowColorsRefined() (+2 more)

### Community 70 - "ChatPreferencesStore"
Cohesion: 0.31
Nodes (6): ChatPreferencesStore, Flow, AdminChatRoute, adminChatScreen(), NavController, navigateToAdminChat()

### Community 73 - "ActiveQuotasGroupedComponents.kt"
Cohesion: 0.50
Nodes (7): formatNumber(), extractLastDigits(), Color, QuotaItem(), ShipCard(), ShipHeader(), WarehouseSection()

### Community 74 - "Final Recommendations"
Cohesion: 0.25
Nodes (8): Final Recommendations, Roadmap عملی, ارزیابی صادقانه, سه توصیه‌ی کلیدی, معیار موفقیت, ۱. رازها را همین امروز بچرخانید — سپس مدل را عوض کنید, ۲. مرز لایه‌ها را ببندید — این باگ است، نه سلیقه, ۳. تست‌هایی که دارید را اجرا کنید

### Community 75 - "AppError"
Cohesion: 0.38
Nodes (5): AppError, Server, Timeout, toAppError(), Validation

### Community 77 - "MessageInputArea"
Cohesion: 0.67
Nodes (5): Color, com, MessageInputArea(), QuotaSelectionDialog(), ShipSelectionDialog()

### Community 78 - "ChatRepository"
Cohesion: 0.10
Nodes (12): ApiResponse, ChatMessage, ChatMessagesResponse, SendMessageRequest, SendMessageResponse, UnreadCountResponse, DeleteMessageRequest, EditMessageRequest (+4 more)

### Community 80 - "ComprehensiveAnalyticsDialog.kt"
Cohesion: 0.57
Nodes (6): AnalyticsDateNavigation(), AnalyticsHeaderCard(), ComprehensiveAnalyticsDialog(), EmptyStateCard(), ErrorStateCard(), Modifier

### Community 81 - "DownloadState"
Cohesion: 0.29
Nodes (6): Completed, Downloading, DownloadState, Error, Idle, Paused

### Community 82 - "ReportsCommonWidgets.kt"
Cohesion: 0.20
Nodes (18): Color, ImageVector, Modifier, StatisticsCard(), CompactStatChip(), EmptyQuotaState(), EmptyShipsState(), EmptyStateCard() (+10 more)

### Community 83 - "استانداردسازی کامنت‌های چندخطی به تک‌خطی (کلاینت + سرور)"
Cohesion: 0.29
Nodes (6): Context, Verification, استانداردسازی کامنت‌های چندخطی به تک‌خطی (کلاینت + سرور), دایرکتوری‌های مستثنی (اصلاً لمس نشوند), قوانین استاندارد تبدیل, نحوه‌ی اجرا

### Community 84 - "AppModule.kt"
Cohesion: 0.22
Nodes (7): AuthRepository, ConflictSession, Error, Flow, LoginResult, Success, LoginUseCase

### Community 85 - "Code Smells شناسایی‌شده"
Cohesion: 0.29
Nodes (7): Code Quality, Code Smells شناسایی‌شده, God Files (۳۱ فایل > ۶۰۰ خط), [LOW] دایرکتوری‌های خالی باقی‌مانده از ماژول‌بندی, [LOW] نام کاملاً واجد شرایط (FQN) درون بدنه‌ی کلاس, [MEDIUM] `CargoUiState` با ۱۶ فیلد, نکته‌ی مثبت درباره‌ی کیفیت: کامنت‌گذاری

### Community 86 - "InitialInfoFormComponents.kt"
Cohesion: 0.60
Nodes (5): CustomInput(), ImageVector, KeyboardActions, KeyboardOptions, SummaryCard()

### Community 87 - "HomeNotificationSettingRow.kt"
Cohesion: 0.67
Nodes (5): CompactSwitch(), Color, ImageVector, Modifier, NotificationSettingRow()

### Community 88 - "RealTimeLoadingCardSection.kt"
Cohesion: 0.53
Nodes (5): CompactInfo(), Color, ImageVector, Modifier, StatisticItem()

### Community 90 - "graphify reference: query, path, explain"
Cohesion: 0.40
Nodes (5): For /graphify explain, For /graphify path, graphify reference: query, path, explain, Step 0 — Constrained query expansion (REQUIRED before traversal), Step 1 — Traversal

### Community 91 - "AppDatabase"
Cohesion: 0.60
Nodes (3): AppDatabase, Context, RoomDatabase

### Community 93 - "Migrations"
Cohesion: 0.33
Nodes (5): Migrations, migrationهای اعمال‌شده تاکنون, قرارداد نام‌گذاری, نحوه‌ی اجرا, وضعیت schema.sql

### Community 94 - ".onError"
Cohesion: 0.17
Nodes (9): CreateUserRequest, DeleteUserRequest, ForceLogoutRequest, UpdateUserRequest, ViewModel, UserManagementViewModel, CargoInfoRow(), ViewModel (+1 more)

### Community 95 - "TokenRefresher"
Cohesion: 0.50
Nodes (3): OkHttpClient, TokenRefresher, RefreshTokenResponse

### Community 96 - "پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13)"
Cohesion: 0.22
Nodes (8): ساختار فعلی (مستند شده در این جلسه), ساختار هدف, مراحل اجرا (ترتیب دقیق مهم است), معیار موفقیت, پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13), پیش‌نیازهای جانبی که باید قبل از این کار حل شوند, چرا فقط .htaccess کافی نیست, چرا لازم است

### Community 99 - "EmptyState.kt"
Cohesion: 0.83
Nodes (3): EmptyState(), ImageVector, Modifier

### Community 100 - "ExitStatusDialog.kt"
Cohesion: 0.83
Nodes (3): AnimatedCounter(), ExitStatusDialog(), ImageVector

### Community 101 - "High Issues"
Cohesion: 0.33
Nodes (6): [HIGH] build release هیچ signingConfig ندارد, [HIGH] Composableها مستقیماً شبکه را صدا می‌زنند و عملیات نوشتن با ناوبری cancel می‌شود, High Issues, [HIGH] phpMyAdmin روی سرور production نصب است, [HIGH] سرور production روی نسخه‌ی PHP بدون پشتیبانی امنیتی اجرا می‌شود, [HIGH] مسیر fallback احراز هویت، رمز عبور متن‌خام را می‌پذیرد

### Community 102 - "ColorPicker.kt"
Cohesion: 0.83
Nodes (3): ColorWheel(), Color, Modifier

### Community 104 - "ReportModels.kt"
Cohesion: 0.15
Nodes (11): AnalyticsData, DateInfo, ExistingQuota, ExitDateInfo, GroupSortingMode, ALPHABETICAL, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC (+3 more)

### Community 109 - "Android Audit"
Cohesion: 0.40
Nodes (5): Android Audit, [LOW] پرچم منسوخ در `gradle.properties`, [MEDIUM] `targetSdk = 34` در حالی که `compileSdk = 36`, مشکلات, نقاط قوت

### Community 110 - "RegisterCargoScreen.kt"
Cohesion: 0.10
Nodes (29): CargoConfirmStatus, AWAITING_CONFIRMATION, CONFIRMED, PENDING, CargoStatus, ENTERED, EXITED, ShipInfo (+21 more)

### Community 115 - "API Audit"
Cohesion: 0.40
Nodes (5): API Audit, [LOW] نبود Rate Limiting سراسری روی Router v2, [MEDIUM] دو شکل ناسازگار برای پاسخ خطا, [MEDIUM] عدم رعایت معنای متدهای HTTP, استخراج Endpointها

### Community 116 - "Testing Audit"
Cohesion: 0.40
Nodes (5): [HIGH] CI هیچ تست اندرویدی اجرا نمی‌کند, Test Caseهای پیشنهادی (اولویت‌دار), Testing Audit, بخش‌های بحرانی بدون تست, وضعیت موجود

### Community 117 - "Jetpack Compose Audit"
Cohesion: 0.40
Nodes (5): [HIGH] انیمیشن‌ها باعث recomposition در هر فریم می‌شوند, Jetpack Compose Audit, [MEDIUM] Composableهای خدای‌گونه, مشکلات, نقاط قوت (واقعاً چشمگیر)

### Community 119 - "quota_details/QuotaCardComponents.kt"
Cohesion: 0.15
Nodes (22): ActionButtonTest, QuotaEditData, DeleteQuotaDialog(), androidx, Color, QuotaConfirmDialog(), ToggleQuotaStatusDialog(), ActionButton() (+14 more)

### Community 120 - "چک‌لیست فعال‌سازی `STRICT_TRANS_TABLES` (DEEP_CODE_AUDIT.md #Phase4.10)"
Cohesion: 0.25
Nodes (7): ریسک واقعی کشف‌شده: بدون سقف طول روی فیلدهای متنی آزاد, مراحل امن (ترتیب مهم است), مسئله چیست, معیار موفقیت, نتایج تست عملی (Phase 5.15), چرا نمی‌شود همین الان فعالش کرد, چک‌لیست فعال‌سازی `STRICT_TRANS_TABLES` (DEEP_CODE_AUDIT.md #Phase4.10)

### Community 122 - "What You Must Do When Invoked"
Cohesion: 0.07
Nodes (26): For /graphify add and --watch, For /graphify query, For the commit hook and native CLAUDE.md integration, For --update and --cluster-only, /graphify, Honesty Rules, Interpreter guard for subcommands, Part A - Structural extraction for code files (+18 more)

### Community 123 - "Performance Audit"
Cohesion: 0.40
Nodes (5): [HIGH] ایندکس گمشده روی `trackingNumber`, [MEDIUM] تاریخ و زمان به‌صورت `varchar(100)` ذخیره می‌شوند, [MEDIUM] حلقه‌ی شمارش معکوس با `delay(200)`, Performance Audit, نقاط قوت Performance

### Community 124 - "Prioritized Action Plan"
Cohesion: 0.40
Nodes (5): Phase 1 — Immediate (هفته‌ی ۱), Phase 2 — High Priority (هفته‌های ۲–۴), Phase 3 — Medium Priority (ماه‌های ۲–۳), Phase 4 — Optimization (ماه‌های ۴+), Prioritized Action Plan

### Community 128 - "VoucherDetailsDialogSection.kt"
Cohesion: 0.26
Nodes (14): VoucherDetail, EmptyVoucherList(), Modifier, SearchTextField(), SortChip(), VoucherDetailsDialog(), VoucherExpandedDetails(), VoucherItem() (+6 more)

### Community 132 - "حذف/محدودسازی phpMyAdmin روی سرور production (Phase 1, آیتم #۲)"
Cohesion: 0.40
Nodes (4): حذف/محدودسازی phpMyAdmin روی سرور production (Phase 1, آیتم #۲), راستی‌آزمایی نهایی, گزینه‌ی ۱ — حذف کامل (توصیه‌شده), گزینه‌ی ۲ — اگر حذف فوری ممکن نیست

### Community 133 - "AnalyticsController"
Cohesion: 0.17
Nodes (10): gregorian_to_jalali(), jalali_to_gregorian(), jcheckdate(), jdate(), jdate_words(), jgetdate(), jmktime(), jstrftime() (+2 more)

### Community 134 - "ColorSelector"
Cohesion: 0.46
Nodes (3): adjustColorForTheme(), ColorSelector, Color

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
Nodes (6): TypeToken, Flow, T, UserPreferencesManager, DataStore, Preferences

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

### Community 188 - "InitialInfoScreen"
Cohesion: 0.23
Nodes (17): formatNumber(), isValidPersianText(), isValidQuotaNumber(), isValidShipName(), isValidWarehouseName(), isValidWeight(), ConfirmationDialog(), DuplicateDialog() (+9 more)

### Community 190 - "QuotaCalculatorTest"
Cohesion: 0.09
Nodes (3): QuotaCalculator, QuotaCalculatorTest, PHPUnit\Framework\TestCase

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

### Community 208 - "CargoInfoDetailsDialogSection.kt"
Cohesion: 0.38
Nodes (11): CargoInfoRequest, CargoDetailsPalette, CargoDetailTabItem(), CargoInfoDetailsDialog(), DetailInfoRow(), ImageVector, Modifier, MainInfoTabContent() (+3 more)

### Community 209 - "CargoCounterScreen.kt"
Cohesion: 0.12
Nodes (24): For --cluster-only, For --update (incremental re-extraction), graphify reference: incremental update and cluster-only, CargoCounterScreen(), CargoCounterViewModel, CargoSnackbarMessage, filterShipsByTab(), com (+16 more)

### Community 211 - "ShipDetailsScreen.kt"
Cohesion: 0.35
Nodes (9): Ship, calculateWarningStatus(), ErrorStateCard(), ImageVector, ShipDetails(), ShipHeaderCard(), TabItem(), WarehouseQuotasTabs() (+1 more)

### Community 221 - "secrets.cpp"
Cohesion: 0.34
Nodes (16): decryptXor(), JNI_OnLoad(), n0(), n1(), n2(), n3(), n4(), n5() (+8 more)

### Community 257 - "CoroutineScope"
Cohesion: 0.27
Nodes (8): AtkCargoApplication, BootReceiver, BroadcastReceiver, Context, Intent, KoinComponent, Application, CoroutineScope

### Community 260 - "CryptoManager"
Cohesion: 0.33
Nodes (4): CryptoManager, ByteArray, Cipher, SecretKey

### Community 262 - "Database"
Cohesion: 0.08
Nodes (12): App\Core\AuthenticatesRequests, InvalidArgumentException, mysqli, PDO, PDOException, UtilityController, Database, PDO (+4 more)

### Community 265 - "MessageType"
Cohesion: 0.17
Nodes (14): Modifier, StatusSnackbar(), MessageType, ERROR, SUCCESS, WARNING, CargoSnackbarQueue, StateFlow (+6 more)

### Community 266 - "ChatNotificationWorker.kt"
Cohesion: 0.48
Nodes (4): ChatNotificationWorker, KoinComponent, CoroutineWorker, ListenableWorker

### Community 272 - "Request"
Cohesion: 0.12
Nodes (4): lic_required_id(), OnlineUsersController, MinVersionGate, Request

### Community 275 - "NotificationActionReceiver.kt"
Cohesion: 0.53
Nodes (4): BroadcastReceiver, Context, Intent, NotificationActionReceiver

### Community 276 - "AuthenticatesRequests.php"
Cohesion: 0.80
Nodes (4): enforceMinAppVersion(), requireAuthenticatedSession(), requirePermission(), sendAuthErrorResponse()

### Community 282 - "Quota"
Cohesion: 0.15
Nodes (19): Quota, WarehouseQuotaGroupingMode, BY_CARGO_OWNER, BY_SHIPPING_COMPANY, BY_WAREHOUSE, buildQuotasShareText(), shareQuotasData(), GroupingModeButton() (+11 more)

### Community 288 - "StartupState"
Cohesion: 0.33
Nodes (6): Ready, SecurityBlocked, Splash, StartupState, Syncing, VersionExpired

### Community 290 - "PermissionPoller"
Cohesion: 0.36
Nodes (3): Job, StateFlow, PermissionPoller

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
- **275 isolated node(s):** `CONFIG`, `Utils`, `name`, `description`, `type` (+270 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **47 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReportsViewModel` connect `ReportsViewModel` to `QuotaDetailsScreen.kt`, `MainScreen.kt`, `ShipSortingMode`, `ColorSelector`, `LoadingState`, `ReportModels.kt`, `QuotaWarningDialog.kt`, `ComprehensiveAnalyticsDialog.kt`, `ShipDetailsScreen.kt`, `AppModule.kt`, `dialogs/QuotaCardComponents.kt`, `ManageReportsScreen.kt`, `QuotaManagementContent.kt`, `ShipsListScreen.kt`, `WarehouseDetailsScreen.kt`, `Quota`, `RealTimeLoadingBottomSheet.kt`, `formatNumber`?**
  _High betweenness centrality (0.043) - this node is a cross-community bridge._
- **Why does `UserPreferencesManager` connect `UserPreferencesManager` to `StartupViewModel`, `CoroutineScope`, `MainScreen.kt`, `ChatPreferencesStore`, `UserPreferencesStore`, `TokenStore`, `ChatNotificationWorker.kt`, `MainActivity.kt`, `AppModule.kt`, `ManageReportsScreen.kt`, `ProfileMenu.kt`, `LoadingNotificationService`?**
  _High betweenness centrality (0.033) - this node is a cross-community bridge._
- **Why does `CargoViewModel` connect `CargoViewModel` to `MainScreen.kt`, `TextAlign`, `ReportsRepository`, `MessageType`, `CargoDetailsScreen.kt`, `ApiServiceV2`, `RegisterCargoScreen.kt`, `CargoInfoDetailsDialogSection.kt`, `SelectInfoScreen.kt`, `AppModule.kt`, `CargoDetailsComponents.kt`, `CargoViewModel.kt`?**
  _High betweenness centrality (0.023) - this node is a cross-community bridge._
- **What connects `CONFIG`, `Utils`, `name` to the rest of the system?**
  _275 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `StartupViewModel` be split into smaller, more focused modules?**
  _Cohesion score 0.13105413105413105 - nodes in this community are weakly interconnected._
- **Should `LicenseAdminService` be split into smaller, more focused modules?**
  _Cohesion score 0.0967741935483871 - nodes in this community are weakly interconnected._
- **Should `LicenseAdminServiceTest` be split into smaller, more focused modules?**
  _Cohesion score 0.0989247311827957 - nodes in this community are weakly interconnected._