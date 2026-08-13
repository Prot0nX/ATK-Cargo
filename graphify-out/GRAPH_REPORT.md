# Graph Report - ATK-Cargo  (2026-08-13)

## Corpus Check
- 276 files · ~347,996 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2289 nodes · 4826 edges · 170 communities (135 shown, 35 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 108 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `78f6feb8`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ATKCargoTheme
- LoadingNotificationService
- AppApiController
- CargoInfo
- UsersManager
- UpdateManager
- ApiService
- ReportsViewModel
- ClassLoader
- AuthController
- ReportModels.kt
- CargoViewModel
- SecurityVerifier
- QuotasListScreen.kt
- ReportsRepository
- ChatRepository
- Ship
- QuotaManagementDialog.kt
- QuotaWarningDialog.kt
- preprocessImage
- CargoRepository
- RetrofitClient
- SelectInfoScreen.kt
- WarehouseDetailsScreen.kt
- UserManagementScreen.kt
- DatabaseManager
- Logger
- ChatViewModel
- UserPreferencesManager
- ManageReportsScreen.kt
- RegisterCargoScreen.kt
- ۴. باگ‌ها
- AnalyticsController
- MessageType
- What You Must Do When Invoked
- CargoCounterScreen.kt
- UserRepository
- P2 — متوسط (عملکرد، معماری، کیفیت کد)
- AuthModels.kt
- LoginScreen.kt
- CargoDetailsScreen.kt
- StartupViewModel
- QuotaPercentageDialogSection.kt
- QuotaDetailsScreen.kt
- secrets.cpp
- mysqli
- InitialInfoScreen.kt
- formatNumber
- ChatMessageEntity
- ActiveShipInfo
- MessageBubble.kt
- composer.json
- CargoModels.kt
- SessionManager
- ChatToolbar.kt
- ShipInfoSection.kt
- CargoController
- SessionRepository
- CargoRegistrationNavigation.kt
- hashPassword
- ChatController
- SortType
- AppModule.kt
- ChatScreen.kt
- ReportsCommonWidgets.kt
- Request
- CargoViewModel.kt
- AppNotificationManager
- AuthController.php
- UserManagementDialogsSection.kt
- ActiveQuotasDialogSection.kt
- ProtectedProxy
- QuotaSelectionDialog.kt
- RealTimeLoadingBottomSheet
- RealTimeLoadingCardSection.kt
- CargoCounterOperationScreen
- UtilityController
- ColorScheme.kt
- FontWeight
- QuotaEntryDialog.kt
- Database
- ChatNotificationWorker.kt
- proxy_generator.php
- NotificationActionReceiver.kt
- NavRoutes
- ChatRepository.kt
- QuotaExistenceMultipleResponse
- CryptoManager
- MessageInputArea
- SearchBar.kt
- LoadingState
- JalaliDateUtils
- DatabaseSchemaExporter
- gradlew
- ComprehensiveAnalyticsDialog.kt
- rememberAdaptiveLayoutConfig
- config.php
- ComposerAutoloaderInit5f285832cbbfb0bffdb861048ac414db
- ExampleInstrumentedTest
- Constants
- ColorExtensions.kt
- HardwarePerformanceEvaluator
- ComponentDefaults.kt
- ExampleUnitTest
- BaselineProfileGenerator
- PermissionPoller
- ReportsDomain.kt
- PasswordGateService
- BootReceiver.kt
- InitialInfo
- AuthViewModel
- ProfileMenu.kt
- Quota
- AppDatabase
- AnimationManager
- HomeNotificationSettingRow.kt
- QuotasListDialogs.kt
- LoginUiState
- graphify reference: extra exports and benchmark
- graphify reference: query, path, explain
- graphify reference: add a URL and watch a folder
- graphify reference: commit hook and native CLAUDE.md integration
- graphify reference: GitHub clone and cross-repo merge
- graphify reference: transcribe video and audio
- rules/graphify.md
- workflows/graphify.md
- CLAUDE.md
- .claude/CLAUDE.md
- extraction-spec.md

## God Nodes (most connected - your core abstractions)
1. `ReportsViewModel` - 105 edges
2. `CargoViewModel` - 85 edges
3. `CargoInfo` - 57 edges
4. `ApiService` - 56 edges
5. `UserPreferencesManager` - 46 edges
6. `formatNumber()` - 46 edges
7. `AppApiController` - 39 edges
8. `UpdateManager` - 38 edges
9. `Logger` - 35 edges
10. `ActiveShipInfo` - 35 edges

## Surprising Connections (you probably didn't know these)
- `validateServerSession()` --calls--> `SessionCheckRequest`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/api/SessionValidator.kt → app/src/main/java/com/atk/atk_cargo/data/model/AuthModels.kt
- `PersianDatePickerDialog()` --calls--> `FilterChip()`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/core/ui/components/DateRangePicker.kt → app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/SelectInfoScreen.kt
- `InitialInfoScreen()` --calls--> `InitialInfo`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/InitialInfoScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/CargoModels.kt
- `navigateToRegisterCargoActivity()` --calls--> `InitialInfo`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/SelectInfoScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/CargoModels.kt
- `CargoCounterScreen()` --calls--> `ColorSelector`  [INFERRED]
  app/src/main/java/com/atk/atk_cargo/feature/cargo_counter/presentation/CargoCounterScreen.kt → app/src/main/java/com/atk/atk_cargo/data/model/ReportModels.kt

## Import Cycles
- None detected.

## Communities (170 total, 35 thin omitted)

### Community 0 - "ATKCargoTheme"
Cohesion: 0.14
Nodes (11): ComponentStyles, Dimensions, Elevation, ATKCargoTheme, Motion, SemanticColors, AppShapes, Spacing (+3 more)

### Community 1 - "LoadingNotificationService"
Cohesion: 0.22
Nodes (6): Context, Intent, KoinComponent, LoadingNotificationService, IBinder, Service

### Community 2 - "AppApiController"
Cohesion: 0.06
Nodes (4): SessionManager, AppApiController, MicroCache, SessionService

### Community 3 - "CargoInfo"
Cohesion: 0.08
Nodes (53): CargoInfo, CargoInfoRequest, CargoDetailsDialog(), Color, ImageVector, Modifier, ModernCargoDetailsGrid(), ModernCargoStatusSection() (+45 more)

### Community 4 - "UsersManager"
Cohesion: 0.07
Nodes (7): ApiManager, CONFIG, ModalManager, ThemeManager, UIManager, UsersManager, Utils

### Community 5 - "UpdateManager"
Cohesion: 0.07
Nodes (28): UpdateInfo, Completed, Downloading, DownloadProgress, DownloadState, Error, Idle, Context (+20 more)

### Community 6 - "ApiService"
Cohesion: 0.10
Nodes (10): ApiResponse2, ApiService, CargoInfoResponse, SuccessResponse, ComprehensiveAnalysisResponse, FilteredSummaryResponse, QuotaDetails, RealTimeDataResponse (+2 more)

### Community 8 - "ClassLoader"
Cohesion: 0.05
Nodes (4): Composer\Semver\VersionParser, ComposerStaticInit5f285832cbbfb0bffdb861048ac414db, ClassLoader, InstalledVersions

### Community 9 - "AuthController"
Cohesion: 0.13
Nodes (3): AuthController, UserController, InputValidator

### Community 10 - "ReportModels.kt"
Cohesion: 0.06
Nodes (28): ThirdPartyApiService, adjustColorForTheme(), AnalyticsData, ColorSelector, DateInfo, ExistingQuota, ExitDateInfo, GroupSortingMode (+20 more)

### Community 12 - "SecurityVerifier"
Cohesion: 0.09
Nodes (11): Secrets, SecurityErrorType, LICENSE_INACTIVE, LICENSE_NOT_FOUND, NETWORK_ERROR, TAMPERED, UNKNOWN_ERROR, SecurityVerifier (+3 more)

### Community 13 - "QuotasListScreen.kt"
Cohesion: 0.21
Nodes (23): QuotaEditData, buildQuotasShareText(), shareQuotasData(), ActionButton(), ConfirmationDialogHeader(), EditFieldBox(), EditFieldColumn(), EditQuotaDialog() (+15 more)

### Community 14 - "ReportsRepository"
Cohesion: 0.15
Nodes (5): ErrorResponse, HttpStatusException, Exception, Result, ReportsRepository

### Community 15 - "ChatRepository"
Cohesion: 0.22
Nodes (3): ChatRepository, com, Result

### Community 16 - "Ship"
Cohesion: 0.11
Nodes (23): ErrorState(), ImageVector, Modifier, Ship, ShipSortingMode, LOADED_TONNAGE_ASC, LOADED_TONNAGE_DESC, NAME_ASC (+15 more)

### Community 17 - "QuotaManagementDialog.kt"
Cohesion: 0.24
Nodes (23): QuotaItem, AdvancedFiltersDialog(), AnalyticsStatChipMini(), CompactStatChip(), EmptyQuotaState(), FilterOptionsCard(), InfoChip(), IntegratedQuotaCard() (+15 more)

### Community 18 - "QuotaWarningDialog.kt"
Cohesion: 0.25
Nodes (17): WarningStatus, formatWeightWithDetail(), CompactInfoSection(), DialogHeader(), Color, ImageVector, Modifier, LoadingActionButton() (+9 more)

### Community 19 - "preprocessImage"
Cohesion: 0.15
Nodes (20): adaptiveThresholding(), applyContrastEnhancement(), enhanceEdges(), ImageProxy, InputImage, medianFilter(), preprocessImage(), EnhancedNumberAnalyzer (+12 more)

### Community 21 - "RetrofitClient"
Cohesion: 0.26
Nodes (9): FloatTypeAdapter, Context, RetrofitClient, ThirdPartyRetrofitClient, JsonReader, JsonWriter, OkHttpClient, Retrofit (+1 more)

### Community 22 - "SelectInfoScreen.kt"
Cohesion: 0.17
Nodes (25): formatNumber(), RealTimeLoadingData, ShipSelectionDialog(), EmptySearchResult(), extractLastDigits(), FilterChip(), FilterState, ALL (+17 more)

### Community 23 - "WarehouseDetailsScreen.kt"
Cohesion: 0.09
Nodes (40): FilteredSummary, VoucherDetail, Warehouse, ExportPdfUseCase, PdfColorScheme, PdfFonts, PersianFontManager, ShamsiDate (+32 more)

### Community 24 - "UserManagementScreen.kt"
Cohesion: 0.16
Nodes (20): DeleteUserRequest, ForceLogoutRequest, UpdateUserRequest, User, EnhancedDeleteConfirmationDialog(), DashboardSummaryRow(), EmptyStateView(), EnterpriseUserCard() (+12 more)

### Community 26 - "Logger"
Cohesion: 0.14
Nodes (4): LicenseController, OnlineUsersController, Logger, self

### Community 27 - "ChatViewModel"
Cohesion: 0.10
Nodes (7): ChatViewModel, ChatViewModelFactory, com, StateFlow, T, ViewModel, ViewModelProvider

### Community 28 - "UserPreferencesManager"
Cohesion: 0.10
Nodes (8): TypeToken, Flow, T, UserPreferencesManager, ThemeColorOption, ThemeColorPickerRow(), DataStore, Preferences

### Community 29 - "ManageReportsScreen.kt"
Cohesion: 0.06
Nodes (59): MainScreen(), RouteTransitions, standardTransitions(), DateClickableField(), DateRangePicker(), getDaysInPersianMonth(), isPersianLeapYear(), Color (+51 more)

### Community 30 - "RegisterCargoScreen.kt"
Cohesion: 0.20
Nodes (19): CargoOperationScreen(), NavController, AnimatedIcon(), DialogContent(), DialogPassword(), DuplicateConfirmationDialog(), DuplicateTrackingNumbersDialog(), ExitStatusDialog() (+11 more)

### Community 31 - "۴. باگ‌ها"
Cohesion: 0.05
Nodes (41): 🔴 A-1 | نبود کامل احراز هویت روی endpoint (بحرانی), 🟠 A-2 | نبود هدرهای کنترل کش روی پاسخ, 🟠 A-3 | نبود اعتبارسنجی/کلمپ روی `shiftOffset`, 🟡 A-4 | Rate-limit مشترک و مبتنی بر IP, 🟡 A-5 | نشتی اطلاعات در پیام خطا, 🟡 B-10 | فیلد مرده در مدل, 🟡 B-11 | `LEFT JOIN` که در عمل `INNER JOIN` است, 🟡 B-12 | فیلتر `isActive` اعمال نمی‌شود (+33 more)

### Community 32 - "AnalyticsController"
Cohesion: 0.20
Nodes (10): gregorian_to_jalali(), jalali_to_gregorian(), jcheckdate(), jdate(), jdate_words(), jgetdate(), jmktime(), jstrftime() (+2 more)

### Community 33 - "MessageType"
Cohesion: 0.16
Nodes (14): Modifier, StatusSnackbar(), MessageType, ERROR, SUCCESS, WARNING, CargoSnackbarQueue, StateFlow (+6 more)

### Community 34 - "What You Must Do When Invoked"
Cohesion: 0.07
Nodes (26): For /graphify add and --watch, For /graphify query, For the commit hook and native CLAUDE.md integration, For --update and --cluster-only, /graphify, Honesty Rules, Interpreter guard for subcommands, Part A - Structural extraction for code files (+18 more)

### Community 35 - "CargoCounterScreen.kt"
Cohesion: 0.16
Nodes (20): Result, validateServerSession(), CargoCounterScreen(), CargoCounterViewModel, filterShipsByTab(), com, NavController, SnackbarHostState (+12 more)

### Community 37 - "P2 — متوسط (عملکرد، معماری، کیفیت کد)"
Cohesion: 0.04
Nodes (48): P0-1. تمام endpointهای ثبت، خروج، ویرایش و حذف حواله بدون احراز هویت هستند, P0-2. هویت کاربر از بدنهٔ درخواست خوانده می‌شود و کاملاً جعل‌پذیر است, P0-3. هیچ کنترل سطح دسترسی روی حذف و ویرایش حواله وجود ندارد, P0-4. دیالوگ «تأیید ثبت تکراری» به دلیل ناسازگاری نام‌گذاری JSON هرگز باز نمی‌شود, P0-5. رِیس‌کاندیشن: خواندن بدون قفل → درج حوالهٔ تکراری در دیتابیس, P0 — بحرانی, P1-1. کنترل‌های کلیدی کسب‌وکار فقط سمت کلاینت اجرا می‌شوند, P1-2. تکراری بودن شماره قبض باسکول در مسیر اصلی خروج بررسی نمی‌شود (+40 more)

### Community 38 - "AuthModels.kt"
Cohesion: 0.11
Nodes (12): ActiveSessionResponse, ForceLogoutResponse, LoginRequest, LoginResponse, LogoutRequest, LogoutResponse, PasswordCheckResponse, PermissionSyncRequest (+4 more)

### Community 39 - "LoginScreen.kt"
Cohesion: 0.24
Nodes (16): CompactLoginLayout(), ExpandedLoginLayout(), IndustrialBrandingHeaderContent(), IndustrialFeatureBadge(), IndustrialFooterInfo(), IndustrialInputField(), IndustrialLoginFormCard(), androidx (+8 more)

### Community 40 - "CargoDetailsScreen.kt"
Cohesion: 0.21
Nodes (15): CargoDetailsScreen(), confirmCargo(), FloatingActionButtonItem(), handleCargoConfirmation(), handleQuotaChangeInDetails(), Color, ImageVector, NavController (+7 more)

### Community 41 - "StartupViewModel"
Cohesion: 0.08
Nodes (19): AtkCargoApplication, AndroidViewModel, Intent, StateFlow, Ready, RequestBatteryOptimization, SecurityBlocked, SecurityCheckState (+11 more)

### Community 42 - "QuotaPercentageDialogSection.kt"
Cohesion: 0.17
Nodes (21): CalculationResult, QuotaPercentageData, AnimatedNumber(), DialogHeader(), Color, ImageVector, Modifier, lerp() (+13 more)

### Community 43 - "QuotaDetailsScreen.kt"
Cohesion: 0.26
Nodes (13): ApiQuotaDetails, adjustColorForTheme(), Color, toTon(), calculatePercentage(), calculateProgress(), ProgressBar(), QuotaAdditionalInfo() (+5 more)

### Community 44 - "secrets.cpp"
Cohesion: 0.41
Nodes (17): decryptXor(), Java_com_atk_atk_1cargo_api_Secrets_getApiKey(), Java_com_atk_atk_1cargo_api_Secrets_getAuthenticationX365(), Java_com_atk_atk_1cargo_api_Secrets_getAuthUser(), Java_com_atk_atk_1cargo_api_Secrets_getBaseUrl(), Java_com_atk_atk_1cargo_api_Secrets_getExpectedSignatureHash(), Java_com_atk_atk_1cargo_api_Secrets_getLicenseCheckUrl(), Java_com_atk_atk_1cargo_api_Secrets_getLicenseInfoUrl() (+9 more)

### Community 45 - "mysqli"
Cohesion: 0.15
Nodes (7): App\Core\AuthenticatesRequests, Exception, mysqli, PDO, ApiException, ConflictException, DatabaseException

### Community 46 - "InitialInfoScreen.kt"
Cohesion: 0.22
Nodes (22): CheckExistenceRequest, formatNumber(), isValidPersianText(), isValidQuotaNumber(), isValidShipName(), isValidWarehouseName(), isValidWeight(), ConfirmationDialog() (+14 more)

### Community 47 - "formatNumber"
Cohesion: 0.28
Nodes (17): QuotaCompletionData, QuotaGroupingMode, BY_CARGO_OWNER, BY_CARRIER, BY_SHIP, formatNumber(), AnalyticsGroupingModeButton(), AnalyticsOwnerSummaryCard() (+9 more)

### Community 48 - "ChatMessageEntity"
Cohesion: 0.15
Nodes (3): ChatDao, Flow, ChatMessageEntity

### Community 49 - "ActiveShipInfo"
Cohesion: 0.32
Nodes (15): ActiveShipInfo, CargoSnackbarMessage, GroupedShipList(), Color, Modifier, QuotaCard(), ShipGroup(), ShipHeader() (+7 more)

### Community 50 - "MessageBubble.kt"
Cohesion: 0.24
Nodes (14): extractShipInfoAndText(), com, replaceUsernamesWithFullNames(), rotateIcon(), ShipInfoModel, ChatInfoRow(), EditMessageDialog(), androidx (+6 more)

### Community 51 - "composer.json"
Cohesion: 0.12
Nodes (15): autoload, autoload-dev, psr-4, psr-4, config, optimize-autoloader, sort-packages, description (+7 more)

### Community 52 - "CargoModels.kt"
Cohesion: 0.12
Nodes (10): CargoDeleteResponse, CargoInfoSearch, CargoSearchResponse, CargoStats, CheckExistenceResponse, ExistingCargo, LoadableTonnageResponse, QuotaTonnageWarning (+2 more)

### Community 54 - "ChatToolbar.kt"
Cohesion: 0.22
Nodes (15): ColorWheel(), Color, Modifier, getAdaptiveBubbleColor(), getChatBackgroundColor(), Color, AdvancedColorPickerDialog(), ChatSettingsDialogEnhanced() (+7 more)

### Community 55 - "ShipInfoSection.kt"
Cohesion: 0.23
Nodes (15): ShipInfo, formatNumber(), toEnglishNumbers(), CargoInfoRow(), DetailedInfoGrid(), DetailInfoItem(), ExpandedContent(), Color (+7 more)

### Community 57 - "SessionRepository"
Cohesion: 0.09
Nodes (3): PDO, SessionRepository, PermissionService

### Community 58 - "CargoRegistrationNavigation.kt"
Cohesion: 0.60
Nodes (4): CargoRegistrationRoute, cargoRegistrationScreen(), NavController, navigateToCargoRegistration()

### Community 59 - "hashPassword"
Cohesion: 0.23
Nodes (4): AuthRepositoryImpl, Flow, hashPassword(), SecurityUtilsTest

### Community 61 - "SortType"
Cohesion: 0.29
Nodes (7): SortType, NAME_ASC, NAME_DESC, QUOTA_COUNT_ASC, QUOTA_COUNT_DESC, TOTAL_WEIGHT_ASC, TOTAL_WEIGHT_DESC

### Community 62 - "AppModule.kt"
Cohesion: 0.22
Nodes (7): AuthRepository, ConflictSession, Error, Flow, LoginResult, Success, LoginUseCase

### Community 63 - "ChatScreen.kt"
Cohesion: 0.31
Nodes (10): ChatUiItem, getDateHeaderColor(), Header, Message, ChatScreen(), DateHeader(), EmptyState(), processMessagesForDisplay() (+2 more)

### Community 64 - "ReportsCommonWidgets.kt"
Cohesion: 0.20
Nodes (18): FabItem, Color, ImageVector, Modifier, StatisticsCard(), CompactStatChip(), EmptyQuotaState(), EmptyShipsState() (+10 more)

### Community 66 - "CargoViewModel.kt"
Cohesion: 0.33
Nodes (5): CargoViewModelFactory, StateFlow, T, ViewModel, ViewModelProvider

### Community 69 - "UserManagementDialogsSection.kt"
Cohesion: 0.32
Nodes (12): CreateUserRequest, UserTypeInfo, DesignTextField(), EnhancedAddUserDialog(), EnhancedEditUserDialog(), EnhancedForceLogoutDialog(), getUserTypes(), androidx (+4 more)

### Community 70 - "ActiveQuotasDialogSection.kt"
Cohesion: 0.29
Nodes (11): ActiveQuotasDialog(), FilterBar(), Color, Dp, Modifier, QuotasHeader(), StatItem(), VerticalDivider() (+3 more)

### Community 72 - "QuotaSelectionDialog.kt"
Cohesion: 0.42
Nodes (10): MatchingQuota, DialogHeader(), findDifferentFields(), InactiveIndicator(), ImageVector, QuotaDetailItem(), QuotaDetails(), QuotaHeader() (+2 more)

### Community 73 - "RealTimeLoadingBottomSheet"
Cohesion: 0.35
Nodes (9): ShiftInfo, DialogHeader(), Color, Modifier, RealTimeLoadingBottomSheet(), RealTimeShiftNavigation(), RefreshOverlay(), ShipCard() (+1 more)

### Community 74 - "RealTimeLoadingCardSection.kt"
Cohesion: 0.53
Nodes (5): CompactInfo(), Color, ImageVector, Modifier, StatisticItem()

### Community 75 - "CargoCounterOperationScreen"
Cohesion: 0.36
Nodes (6): CargoCounterRoute, cargoCounterScreen(), NavController, navigateToCargoCounter(), CargoCounterOperationScreen(), NavController

### Community 77 - "ColorScheme.kt"
Cohesion: 0.80
Nodes (5): buildAppDarkColorScheme(), buildAppLightColorScheme(), compositeOver(), Color, ColorScheme

### Community 78 - "FontWeight"
Cohesion: 0.06
Nodes (38): ConfirmationDialog(), ImageVector, EmptyState(), ImageVector, Modifier, AnimatedCounter(), ExitStatusDialog(), ImageVector (+30 more)

### Community 79 - "QuotaEntryDialog.kt"
Cohesion: 0.57
Nodes (7): FocusRequester, QuotaEntryActions(), QuotaEntryContent(), QuotaEntryDialog(), QuotaEntryPalette, rememberQuotaEntryPalette(), validateAndSubmit()

### Community 80 - "Database"
Cohesion: 0.13
Nodes (5): Config, self, Database, PDO, self

### Community 81 - "ChatNotificationWorker.kt"
Cohesion: 0.48
Nodes (4): ChatNotificationWorker, KoinComponent, CoroutineWorker, ListenableWorker

### Community 82 - "proxy_generator.php"
Cohesion: 0.57
Nodes (6): _o1(), _o2(), _o3(), _o4(), _o5(), _o6()

### Community 83 - "NotificationActionReceiver.kt"
Cohesion: 0.53
Nodes (4): BroadcastReceiver, Context, Intent, NotificationActionReceiver

### Community 85 - "ChatRepository.kt"
Cohesion: 0.13
Nodes (9): ApiResponse, ChatMessage, ChatMessagesResponse, SendMessageRequest, SendMessageResponse, UnreadCountResponse, DeleteMessageRequest, EditMessageRequest (+1 more)

### Community 86 - "QuotaExistenceMultipleResponse"
Cohesion: 0.22
Nodes (3): QuotaExistenceMultipleResponse, QuotaStatusResponse, CheckQuotaUseCase

### Community 87 - "CryptoManager"
Cohesion: 0.38
Nodes (4): CryptoManager, ByteArray, Cipher, SecretKey

### Community 88 - "MessageInputArea"
Cohesion: 0.67
Nodes (5): Color, com, MessageInputArea(), QuotaSelectionDialog(), ShipSelectionDialog()

### Community 90 - "LoadingState"
Cohesion: 0.47
Nodes (6): Error, Idle, Loading, LoadingState, Success, UiState

### Community 93 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 94 - "ComprehensiveAnalyticsDialog.kt"
Cohesion: 0.57
Nodes (6): AnalyticsDateNavigation(), AnalyticsHeaderCard(), ComprehensiveAnalyticsDialog(), EmptyStateCard(), ErrorStateCard(), Modifier

### Community 95 - "rememberAdaptiveLayoutConfig"
Cohesion: 0.33
Nodes (6): AdaptiveLayoutConfig, rememberAdaptiveLayoutConfig(), WindowSizeClass, COMPACT, EXPANDED, MEDIUM

### Community 146 - "PermissionPoller"
Cohesion: 0.36
Nodes (3): Job, StateFlow, PermissionPoller

### Community 147 - "ReportsDomain.kt"
Cohesion: 0.33
Nodes (8): addOneDayToPersianDate(), format(), formatHoursToPersian(), getDaysInPersianMonth(), isPersianLeapYear(), Context, QuotaWarningThresholds, shareCargoInfo()

### Community 149 - "BootReceiver.kt"
Cohesion: 0.48
Nodes (5): BootReceiver, BroadcastReceiver, Context, Intent, KoinComponent

### Community 150 - "InitialInfo"
Cohesion: 0.13
Nodes (18): InitialInfo, QuotaValidationResult, CargoInfoCard(), CargoListSection(), GroupStats(), InfoChips(), InfoGrid(), InfoGridCard() (+10 more)

### Community 151 - "AuthViewModel"
Cohesion: 0.18
Nodes (6): AuthViewModel, StateFlow, ViewModel, For --cluster-only, For --update (incremental re-extraction), graphify reference: incremental update and cluster-only

### Community 152 - "ProfileMenu.kt"
Cohesion: 0.52
Nodes (6): ActionButtons(), getUserTypeDisplay(), Color, ProfileMenu(), ProfilePalette, rememberProfilePalette()

### Community 153 - "Quota"
Cohesion: 0.25
Nodes (11): Quota, calculateWarningStatus(), ErrorStateCard(), ImageVector, ShipDetails(), ShipHeaderCard(), TabItem(), WarehouseQuotasTabs() (+3 more)

### Community 154 - "AppDatabase"
Cohesion: 0.47
Nodes (3): AppDatabase, Context, RoomDatabase

### Community 156 - "HomeNotificationSettingRow.kt"
Cohesion: 0.67
Nodes (5): CompactSwitch(), Color, ImageVector, Modifier, NotificationSettingRow()

### Community 157 - "QuotasListDialogs.kt"
Cohesion: 0.53
Nodes (5): DeleteQuotaDialog(), androidx, Color, QuotaConfirmDialog(), ToggleQuotaStatusDialog()

### Community 158 - "LoginUiState"
Cohesion: 0.40
Nodes (5): Error, Idle, Loading, LoginUiState, Success

### Community 159 - "graphify reference: extra exports and benchmark"
Cohesion: 0.22
Nodes (8): graphify reference: extra exports and benchmark, Step 6b - Wiki (only if --wiki flag), Step 7 - Neo4j export (only if --neo4j or --neo4j-push flag), Step 7a - FalkorDB export (only if --falkordb or --falkordb-push flag), Step 7b - SVG export (only if --svg flag), Step 7c - GraphML export (only if --graphml flag), Step 7d - MCP server (only if --mcp flag), Step 8 - Token reduction benchmark (only if total_words > 5000)

### Community 165 - "graphify reference: query, path, explain"
Cohesion: 0.40
Nodes (5): For /graphify explain, For /graphify path, graphify reference: query, path, explain, Step 0 — Constrained query expansion (REQUIRED before traversal), Step 1 — Traversal

### Community 167 - "graphify reference: add a URL and watch a folder"
Cohesion: 0.50
Nodes (3): For /graphify add, For --watch, graphify reference: add a URL and watch a folder

### Community 168 - "graphify reference: commit hook and native CLAUDE.md integration"
Cohesion: 0.50
Nodes (3): For git commit hook, For native CLAUDE.md integration, graphify reference: commit hook and native CLAUDE.md integration

## Knowledge Gaps
- **215 isolated node(s):** `CONFIG`, `Utils`, `name`, `description`, `type` (+210 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **35 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReportsViewModel` connect `ReportsViewModel` to `CargoInfo`, `ApiService`, `RealTimeLoadingBottomSheet`, `ReportModels.kt`, `QuotaDetailsScreen.kt`, `QuotasListScreen.kt`, `formatNumber`, `Ship`, `QuotaManagementDialog.kt`, `QuotaWarningDialog.kt`, `WarehouseDetailsScreen.kt`, `ComprehensiveAnalyticsDialog.kt`, `Quota`, `LoadingState`, `ManageReportsScreen.kt`, `AppModule.kt`?**
  _High betweenness centrality (0.065) - this node is a cross-community bridge._
- **Why does `UserPreferencesManager` connect `UserPreferencesManager` to `LoadingNotificationService`, `CargoViewModel.kt`, `CargoCounterScreen.kt`, `AuthModels.kt`, `CargoDetailsScreen.kt`, `StartupViewModel`, `FontWeight`, `ChatNotificationWorker.kt`, `ProfileMenu.kt`, `BootReceiver.kt`, `SelectInfoScreen.kt`, `UserManagementScreen.kt`, `hashPassword`, `ManageReportsScreen.kt`, `AppModule.kt`, `ChatScreen.kt`?**
  _High betweenness centrality (0.044) - this node is a cross-community bridge._
- **Why does `CargoViewModel` connect `CargoViewModel` to `MessageType`, `CargoViewModel.kt`, `CargoInfo`, `CargoDetailsScreen.kt`, `CargoCounterOperationScreen`, `ActiveShipInfo`, `SelectInfoScreen.kt`, `RegisterCargoScreen.kt`, `InitialInfo`, `QuotaExistenceMultipleResponse`, `CargoRegistrationNavigation.kt`, `ManageReportsScreen.kt`, `AppModule.kt`?**
  _High betweenness centrality (0.040) - this node is a cross-community bridge._
- **What connects `CONFIG`, `Utils`, `name` to the rest of the system?**
  _215 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `ATKCargoTheme` be split into smaller, more focused modules?**
  _Cohesion score 0.1368421052631579 - nodes in this community are weakly interconnected._
- **Should `AppApiController` be split into smaller, more focused modules?**
  _Cohesion score 0.06153846153846154 - nodes in this community are weakly interconnected._
- **Should `CargoInfo` be split into smaller, more focused modules?**
  _Cohesion score 0.08408953418027829 - nodes in this community are weakly interconnected._