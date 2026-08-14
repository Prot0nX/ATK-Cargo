# Graph Report - ATK-Cargo  (2026-08-14)

## Corpus Check
- 279 files · ~369,925 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2435 nodes · 4966 edges · 164 communities (130 shown, 34 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 76 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `c1704af3`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- formatNumber
- QuotasListScreen.kt
- CargoInfo
- UsersManager
- ReportsRepository
- ManageReportsScreen.kt
- ClassLoader
- CargoViewModel
- UpdateManager
- ReportModels.kt
- AppApiController
- SecurityVerifier
- StartupViewModel
- ReportsViewModel
- P2 — متوسط (عملکرد، معماری، کیفیت کد)
- QuotaManagementDialog.kt
- CargoRepository
- MessageBubble.kt
- SelectInfoScreen.kt
- ChatRepository
- InitialInfo
- ATKCargoTheme
- ChatViewModel
- InitialInfoScreen.kt
- ExportPdfUseCase
- AuthController
- ApiService
- SplashScreen.kt
- UserPreferencesManager
- FontWeight
- CargoCounterScreen.kt
- preprocessImage
- ۴. باگ‌ها
- ۴. باگ‌ها
- LoadingNotificationService
- LoginScreen.kt
- Database
- Color.kt
- Ship
- CargoEditSearchDialogsSection.kt
- MessageType
- AnalyticsController
- UserRepository
- secrets.cpp
- QuotaEntryDialog.kt
- گزارش جامع بازبینی `app/proguard-rules.pro` — ATK-Cargo
- Config
- AppNotificationManager
- ChatMessageEntity
- ShipInfoSection.kt
- ActiveQuotasDialogSection.kt
- CargoController
- Logger
- ActiveShipInfo
- SecurityScreen.kt
- composer.json
- SessionManager
- SessionManager
- P2 — متوسط
- What You Must Do When Invoked
- ۴. یافته‌های جدی (🟠)
- WarehouseDetailsScreen.kt
- SessionRepository
- VoucherDetailsDialogSection.kt
- ChatScreen.kt
- CargoViewModel.kt
- QuotaSelectionDialog.kt
- CargoInfoDetailsDialogSection.kt
- Request
- RegisterCargoScreen.kt
- RealTimeLoadingBottomSheet
- ChatNotificationWorker.kt
- MainActivity.kt
- DatabaseSchemaExporter
- ProtectedProxy
- RealTimeLoadingData
- SessionService
- graphify reference: extra exports and benchmark
- ComprehensiveAnalyticsDialog.kt
- ChatToolbar.kt
- SortType
- StartupState
- HomeNotificationSettingRow.kt
- UtilityController
- CoroutineScope
- graphify reference: query, path, explain
- ApiService.kt
- ApiResponse
- graphify reference: add a URL and watch a folder
- graphify reference: commit hook and native CLAUDE.md integration
- CargoDetailsScreen.kt
- ColorExtensions.kt
- HardwarePerformanceEvaluator
- ProfileMenu.kt
- PasswordGateService
- MessageInputArea
- graphify reference: GitHub clone and cross-repo merge
- proxy_generator.php
- NotificationActionReceiver.kt
- NavRoutes
- AppDatabase
- graphify reference: transcribe video and audio
- rules/graphify.md
- workflows/graphify.md
- LoadingState
- JalaliDateUtils
- CLAUDE.md
- .claude/CLAUDE.md
- AnimationManager
- gradlew
- extraction-spec.md
- config.php
- ComposerAutoloaderInit5f285832cbbfb0bffdb861048ac414db
- ExampleInstrumentedTest
- CargoModels.kt
- ChatRepository.kt
- Constants
- ComponentDefaults.kt
- ExampleUnitTest
- BaselineProfileGenerator
- QuotaExistenceMultipleResponse
- ReportsRepository.kt
- PermissionPoller

## God Nodes (most connected - your core abstractions)
1. `ReportsViewModel` - 105 edges
2. `CargoViewModel` - 80 edges
3. `ApiService` - 57 edges
4. `CargoInfo` - 57 edges
5. `formatNumber()` - 46 edges
6. `UserPreferencesManager` - 45 edges
7. `AppApiController` - 41 edges
8. `UpdateManager` - 37 edges
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

## Communities (164 total, 34 thin omitted)

### Community 0 - "formatNumber"
Cohesion: 0.19
Nodes (22): QuotaCompletionData, QuotaGroupingMode, BY_CARGO_OWNER, BY_CARRIER, BY_SHIP, buildQuotaGroups(), QuotaGroup, formatNumber() (+14 more)

### Community 1 - "QuotasListScreen.kt"
Cohesion: 0.05
Nodes (85): CalculationResult, Quota, QuotaEditData, QuotaPercentageData, WarningStatus, addOneDayToPersianDate(), buildQuotasShareText(), format() (+77 more)

### Community 2 - "CargoInfo"
Cohesion: 0.15
Nodes (22): CargoInfo, CargoDetailsDialog(), Color, ImageVector, Modifier, ModernCargoDetailsGrid(), ModernCargoStatusSection(), ModernCargoStatusTimeline() (+14 more)

### Community 3 - "UsersManager"
Cohesion: 0.07
Nodes (7): ApiManager, CONFIG, ModalManager, ThemeManager, UIManager, UsersManager, Utils

### Community 4 - "ReportsRepository"
Cohesion: 0.15
Nodes (6): RealTimeDataResponse, ErrorResponse, HttpStatusException, Exception, Result, ReportsRepository

### Community 5 - "ManageReportsScreen.kt"
Cohesion: 0.06
Nodes (58): ApiQuotaDetails, adjustColorForTheme(), FabItem, Color, toTon(), DateClickableField(), DateRangePicker(), getDaysInPersianMonth() (+50 more)

### Community 6 - "ClassLoader"
Cohesion: 0.05
Nodes (4): Composer\Semver\VersionParser, ComposerStaticInit5f285832cbbfb0bffdb861048ac414db, ClassLoader, InstalledVersions

### Community 8 - "UpdateManager"
Cohesion: 0.05
Nodes (37): UpdateInfo, FloatTypeAdapter, Context, RetrofitClient, ThirdPartyRetrofitClient, Completed, Downloading, DownloadProgress (+29 more)

### Community 9 - "ReportModels.kt"
Cohesion: 0.14
Nodes (11): ThirdPartyApiService, AnalyticsData, ColorSelector, DateInfo, ExistingQuota, ExitDateInfo, Color, QuotaCompletionAnalysis (+3 more)

### Community 10 - "AppApiController"
Cohesion: 0.09
Nodes (3): AppApiController, ChatController, MicroCache

### Community 11 - "SecurityVerifier"
Cohesion: 0.09
Nodes (11): Secrets, SecurityErrorType, LICENSE_INACTIVE, LICENSE_NOT_FOUND, NETWORK_ERROR, TAMPERED, UNKNOWN_ERROR, SecurityVerifier (+3 more)

### Community 12 - "StartupViewModel"
Cohesion: 0.11
Nodes (11): AndroidViewModel, Flow, Intent, StateFlow, RequestBatteryOptimization, SecurityCheckState, ShowMessage, StartupEvent (+3 more)

### Community 13 - "ReportsViewModel"
Cohesion: 0.06
Nodes (19): adjustColorForTheme(), ComprehensiveAnalytics, GroupSortingMode, ALPHABETICAL, REMAINING_TONNAGE_ASC, REMAINING_TONNAGE_DESC, QuotaSortingMode, REMAINING_TONNAGE_ASC (+11 more)

### Community 14 - "P2 — متوسط (عملکرد، معماری، کیفیت کد)"
Cohesion: 0.04
Nodes (48): P0-1. تمام endpointهای ثبت، خروج، ویرایش و حذف حواله بدون احراز هویت هستند, P0-2. هویت کاربر از بدنهٔ درخواست خوانده می‌شود و کاملاً جعل‌پذیر است, P0-3. هیچ کنترل سطح دسترسی روی حذف و ویرایش حواله وجود ندارد, P0-4. دیالوگ «تأیید ثبت تکراری» به دلیل ناسازگاری نام‌گذاری JSON هرگز باز نمی‌شود, P0-5. رِیس‌کاندیشن: خواندن بدون قفل → درج حوالهٔ تکراری در دیتابیس, P0 — بحرانی, P1-1. کنترل‌های کلیدی کسب‌وکار فقط سمت کلاینت اجرا می‌شوند, P1-2. تکراری بودن شماره قبض باسکول در مسیر اصلی خروج بررسی نمی‌شود (+40 more)

### Community 15 - "QuotaManagementDialog.kt"
Cohesion: 0.24
Nodes (23): QuotaItem, AdvancedFiltersDialog(), AnalyticsStatChipMini(), CompactStatChip(), EmptyQuotaState(), FilterOptionsCard(), InfoChip(), IntegratedQuotaCard() (+15 more)

### Community 17 - "MessageBubble.kt"
Cohesion: 0.24
Nodes (14): extractShipInfoAndText(), com, replaceUsernamesWithFullNames(), rotateIcon(), ShipInfoModel, ChatInfoRow(), EditMessageDialog(), androidx (+6 more)

### Community 18 - "SelectInfoScreen.kt"
Cohesion: 0.20
Nodes (20): formatNumber(), AnimatedHeader(), ShipSelectionDialog(), extractLastDigits(), FilterChip(), GroupedShipList(), Color, Modifier (+12 more)

### Community 19 - "ChatRepository"
Cohesion: 0.22
Nodes (3): ChatRepository, com, Result

### Community 20 - "InitialInfo"
Cohesion: 0.15
Nodes (18): InitialInfo, QuotaValidationResult, CargoInfoCard(), CargoListSection(), GroupStats(), InfoChips(), InfoGrid(), InfoGridCard() (+10 more)

### Community 21 - "ATKCargoTheme"
Cohesion: 0.09
Nodes (22): AdaptiveLayoutConfig, rememberAdaptiveLayoutConfig(), WindowSizeClass, COMPACT, EXPANDED, MEDIUM, buildAppDarkColorScheme(), buildAppLightColorScheme() (+14 more)

### Community 22 - "ChatViewModel"
Cohesion: 0.10
Nodes (7): ChatViewModel, ChatViewModelFactory, com, StateFlow, T, ViewModel, ViewModelProvider

### Community 23 - "InitialInfoScreen.kt"
Cohesion: 0.05
Nodes (67): MainScreen(), RouteTransitions, standardTransitions(), CheckExistenceRequest, MenuItem, LoginScreen(), formatNumber(), isValidPersianText() (+59 more)

### Community 24 - "ExportPdfUseCase"
Cohesion: 0.21
Nodes (12): ExportPdfUseCase, PdfColorScheme, PdfFonts, PersianFontManager, ShamsiDate, VazirmatnFontFamily, Document, Font (+4 more)

### Community 25 - "AuthController"
Cohesion: 0.08
Nodes (5): AuthController, UserController, Response, PermissionService, InputValidator

### Community 26 - "ApiService"
Cohesion: 0.15
Nodes (3): ApiService, SuccessResponse, FilteredSummaryResponse

### Community 27 - "SplashScreen.kt"
Cohesion: 0.53
Nodes (5): SplashScreen(), SplashScreenConstants, SplashScreenContent(), SplashSubtitleBadge(), SplashVersionBadge()

### Community 28 - "UserPreferencesManager"
Cohesion: 0.10
Nodes (8): TypeToken, Flow, T, UserPreferencesManager, ThemeColorOption, ThemeColorPickerRow(), DataStore, Preferences

### Community 29 - "FontWeight"
Cohesion: 0.15
Nodes (9): ConfirmationDialog(), ImageVector, EmptyState(), ImageVector, Modifier, AnimatedCounter(), ExitStatusDialog(), ImageVector (+1 more)

### Community 30 - "CargoCounterScreen.kt"
Cohesion: 0.16
Nodes (19): Result, validateServerSession(), CargoCounterScreen(), CargoCounterViewModel, filterShipsByTab(), com, NavController, SnackbarHostState (+11 more)

### Community 31 - "preprocessImage"
Cohesion: 0.13
Nodes (22): adaptiveThresholding(), applyContrastEnhancement(), enhanceEdges(), ImageProxy, InputImage, medianFilter(), preprocessImage(), EnhancedNumberAnalyzer (+14 more)

### Community 32 - "۴. باگ‌ها"
Cohesion: 0.04
Nodes (44): 🔴 A-1 | مجوز `view_reports` هرگز اعمال نمی‌شود (Broken Access Control), 🟠 A-2 | `offset` سمت سرور کلمپ نمی‌شود, 🟠 A-3 | نشت پیام خام سرور در UI و عدم تشخیص ۴۰۱, 🟡 A-4 | Rate-limit مشترک روی سنگین‌ترین کوئری, 🟡 A-5 | اشتراک‌گذاری داده تجاری بدون تأیید و بدون رد ممیزی, 🟢 A-6 | مواردی که درست انجام شده‌اند, 🟡 B-10 | `EmptyStateCard` عملاً کد مرده است و پیام خطای واقعی سرور بلعیده می‌شود, 🟡 B-11 | مرز روز ۰۷:۰۰ در برابر مرز شیفت ۰۷:۳۰ (+36 more)

### Community 33 - "۴. باگ‌ها"
Cohesion: 0.05
Nodes (41): 🔴 A-1 | نبود کامل احراز هویت روی endpoint (بحرانی), 🟠 A-2 | نبود هدرهای کنترل کش روی پاسخ, 🟠 A-3 | نبود اعتبارسنجی/کلمپ روی `shiftOffset`, 🟡 A-4 | Rate-limit مشترک و مبتنی بر IP, 🟡 A-5 | نشتی اطلاعات در پیام خطا, 🟡 B-10 | فیلد مرده در مدل, 🟡 B-11 | `LEFT JOIN` که در عمل `INNER JOIN` است, 🟡 B-12 | فیلتر `isActive` اعمال نمی‌شود (+33 more)

### Community 34 - "LoadingNotificationService"
Cohesion: 0.22
Nodes (6): Context, Intent, KoinComponent, LoadingNotificationService, IBinder, Service

### Community 35 - "LoginScreen.kt"
Cohesion: 0.06
Nodes (40): Modifier, SearchBar(), LogoutRequest, LogoutResponse, AuthRepository, ConflictSession, Error, Flow (+32 more)

### Community 36 - "Database"
Cohesion: 0.08
Nodes (13): Exception, mysqli, mysqli_stmt, PDO, PDOException, LicenseController, Database, PDO (+5 more)

### Community 37 - "Color.kt"
Cohesion: 0.07
Nodes (43): ActiveSessionResponse, CreateUserRequest, DeleteUserRequest, ForceLogoutRequest, ForceLogoutResponse, LoginRequest, LoginResponse, PasswordCheckResponse (+35 more)

### Community 38 - "Ship"
Cohesion: 0.11
Nodes (23): ErrorState(), ImageVector, Modifier, Ship, ShipSortingMode, LOADED_TONNAGE_ASC, LOADED_TONNAGE_DESC, NAME_ASC (+15 more)

### Community 39 - "CargoEditSearchDialogsSection.kt"
Cohesion: 0.30
Nodes (16): CargoChangesPreview(), CargoEditConfirmDialog(), CargoEditConfirmHeader(), CargoMainInfo(), CargoShippingInfo(), CargoTimeInfo(), CargoWeightInfo(), ChangeItem() (+8 more)

### Community 40 - "MessageType"
Cohesion: 0.17
Nodes (14): Modifier, StatusSnackbar(), MessageType, ERROR, SUCCESS, WARNING, CargoSnackbarQueue, StateFlow (+6 more)

### Community 41 - "AnalyticsController"
Cohesion: 0.18
Nodes (10): gregorian_to_jalali(), jalali_to_gregorian(), jcheckdate(), jdate(), jdate_words(), jgetdate(), jmktime(), jstrftime() (+2 more)

### Community 43 - "secrets.cpp"
Cohesion: 0.41
Nodes (17): decryptXor(), Java_com_atk_atk_1cargo_api_Secrets_getApiKey(), Java_com_atk_atk_1cargo_api_Secrets_getAuthenticationX365(), Java_com_atk_atk_1cargo_api_Secrets_getAuthUser(), Java_com_atk_atk_1cargo_api_Secrets_getBaseUrl(), Java_com_atk_atk_1cargo_api_Secrets_getExpectedSignatureHash(), Java_com_atk_atk_1cargo_api_Secrets_getLicenseCheckUrl(), Java_com_atk_atk_1cargo_api_Secrets_getLicenseInfoUrl() (+9 more)

### Community 44 - "QuotaEntryDialog.kt"
Cohesion: 0.57
Nodes (7): FocusRequester, QuotaEntryActions(), QuotaEntryContent(), QuotaEntryDialog(), QuotaEntryPalette, rememberQuotaEntryPalette(), validateAndSubmit()

### Community 45 - "گزارش جامع بازبینی `app/proguard-rules.pro` — ATK-Cargo"
Cohesion: 0.05
Nodes (36): C1 — قوانین `-keep` بیش‌ازحد باز، obfuscation را روی حساس‌ترین کد خنثی می‌کنند, C2 — keep کامل روی `retrofit2` و `okhttp3` — ٪۳۵ کل seedها، shrinking صفر, C3 — `-keepattributes !SourceFile,!LineNumberTable` معنایی معکوس دارد, H1 — حذف null-checkهای Kotlin: تبدیل fail-fast به رفتار تعریف‌نشده, H2 — حذف `Log.e` و `printStackTrace` = نابینایی کامل در production, H3 — سکشن ۹ (Koin) کاملاً کد مرده است, H4 — iText: هم ریسک runtime، هم مشکل لایسنس, H5 — `proguardFiles` دوبار اعلام شده (+28 more)

### Community 48 - "ChatMessageEntity"
Cohesion: 0.15
Nodes (3): ChatDao, Flow, ChatMessageEntity

### Community 49 - "ShipInfoSection.kt"
Cohesion: 0.30
Nodes (13): ShipInfo, formatNumber(), toEnglishNumbers(), DetailedInfoGrid(), DetailInfoItem(), ExpandedContent(), Color, ImageVector (+5 more)

### Community 50 - "ActiveQuotasDialogSection.kt"
Cohesion: 0.16
Nodes (19): ActiveQuotasDialog(), FilterBar(), Color, Dp, Modifier, QuotasHeader(), StatItem(), VerticalDivider() (+11 more)

### Community 52 - "Logger"
Cohesion: 0.12
Nodes (5): App\Core\AuthenticatesRequests, InvalidArgumentException, Logger, self, CargoService

### Community 53 - "ActiveShipInfo"
Cohesion: 0.32
Nodes (15): ActiveShipInfo, CargoSnackbarMessage, GroupedShipList(), Color, Modifier, QuotaCard(), ShipGroup(), ShipHeader() (+7 more)

### Community 54 - "SecurityScreen.kt"
Cohesion: 0.25
Nodes (13): AdvancedOrbitalScanner(), AnimatedUpdateIcon(), AnimatedWarningIcon(), DynamicPremiumBackground(), InteractiveExitButton(), Modifier, RetryButton(), SecurityBlockScreen() (+5 more)

### Community 55 - "composer.json"
Cohesion: 0.12
Nodes (15): autoload, autoload-dev, psr-4, psr-4, config, optimize-autoloader, sort-packages, description (+7 more)

### Community 58 - "P2 — متوسط"
Cohesion: 0.06
Nodes (30): P0-1. عملیات «تأیید حواله» در سرور هیچ گیت مجوزی ندارد (ارتقای سطح دسترسی), P0-2. IDOR: شناسه حواله بدون هیچ بررسی دامنه‌ای پذیرفته می‌شود, P0-3. کرش قطعی `LazyColumn` روی داده‌ای که خود اپ برایش هشدار دارد, P0-4. فیلتر جستجو هر ۳۰ ثانیه (و بعد از هر تأیید) بی‌صدا پاک می‌شود, P0 — بحرانی, P1-1. پیام معنادار ۴۰۹ سرور دور ریخته می‌شود, P1-2. نبود قفل ضد دوبار-زدن روی دکمه «تأیید حواله», P1-3. اعتبارسنجی نشست فقط یک بار انجام می‌شود و ۴۰۱ در ادامه مدیریت نمی‌شود (+22 more)

### Community 59 - "What You Must Do When Invoked"
Cohesion: 0.07
Nodes (26): For /graphify add and --watch, For /graphify query, For the commit hook and native CLAUDE.md integration, For --update and --cluster-only, /graphify, Honesty Rules, Interpreter guard for subcommands, Part A - Structural extraction for code files (+18 more)

### Community 60 - "۴. یافته‌های جدی (🟠)"
Cohesion: 0.08
Nodes (23): C-1 — قفل کامل برنامه با هر آپدیت موجود، حتی غیراجباری, C-2 — `runBlocking` روی Main Thread در `onCreate`, C-3 — زنجیره آپدیت APK بدون هیچ تضمین صحت, C-4 — `try/catch` روی کل `onCreate` ⇒ صفحه سفید بی‌صدا, C-5 — (سرور) `sessionToken` ارسال می‌شود ولی هرگز اعتبارسنجی نمی‌شود, S-1 — عدم تطابق قرارداد پاسخ `check_update.php`, S-2 — خروج اجباری همه کاربران با یک خطای موقت سرور, S-3 — کلید API در query string و به‌صورت رشته ثابت در سورس سرور (+15 more)

### Community 61 - "WarehouseDetailsScreen.kt"
Cohesion: 0.20
Nodes (16): FilteredSummary, Warehouse, persianDateFormat(), DateTimePicker(), Modifier, PersianDateItem(), TimePickerDialog(), ExportOptions() (+8 more)

### Community 63 - "VoucherDetailsDialogSection.kt"
Cohesion: 0.26
Nodes (14): VoucherDetail, EmptyVoucherList(), Modifier, SearchTextField(), SortChip(), VoucherDetailsDialog(), VoucherExpandedDetails(), VoucherItem() (+6 more)

### Community 64 - "ChatScreen.kt"
Cohesion: 0.31
Nodes (10): ChatUiItem, getDateHeaderColor(), Header, Message, ChatScreen(), DateHeader(), EmptyState(), processMessagesForDisplay() (+2 more)

### Community 65 - "CargoViewModel.kt"
Cohesion: 0.33
Nodes (5): CargoViewModelFactory, StateFlow, T, ViewModel, ViewModelProvider

### Community 66 - "QuotaSelectionDialog.kt"
Cohesion: 0.42
Nodes (10): MatchingQuota, DialogHeader(), findDifferentFields(), InactiveIndicator(), ImageVector, QuotaDetailItem(), QuotaDetails(), QuotaHeader() (+2 more)

### Community 67 - "CargoInfoDetailsDialogSection.kt"
Cohesion: 0.35
Nodes (12): CargoDetailsPalette, CargoDetailTabItem(), CargoInfoDetailsDialog(), DeleteDialog(), DetailInfoRow(), ImageVector, Modifier, SnackbarHostState (+4 more)

### Community 69 - "RegisterCargoScreen.kt"
Cohesion: 0.21
Nodes (16): AnimatedIcon(), DialogContent(), DialogPassword(), DuplicateConfirmationDialog(), DuplicateTrackingNumbersDialog(), FocusRequester, MessageDialog(), NetWeightDialog() (+8 more)

### Community 70 - "RealTimeLoadingBottomSheet"
Cohesion: 0.40
Nodes (9): ShiftInfo, DialogHeader(), Color, Modifier, RealTimeLoadingBottomSheet(), RealTimeShiftNavigation(), RefreshOverlay(), ShipCard() (+1 more)

### Community 71 - "ChatNotificationWorker.kt"
Cohesion: 0.48
Nodes (4): ChatNotificationWorker, KoinComponent, CoroutineWorker, ListenableWorker

### Community 72 - "MainActivity.kt"
Cohesion: 0.24
Nodes (6): ServerSyncingScreen(), Intent, MainActivity, StartupErrorScreen(), Bundle, ComponentActivity

### Community 75 - "RealTimeLoadingData"
Cohesion: 0.33
Nodes (7): RealTimeLoadingData, CompactInfo(), Color, ImageVector, Modifier, RealTimeLoadingCard(), StatisticItem()

### Community 76 - "SessionService"
Cohesion: 0.18
Nodes (3): enforceMinAppVersion(), requireAuthenticatedSession(), SessionService

### Community 77 - "graphify reference: extra exports and benchmark"
Cohesion: 0.22
Nodes (8): graphify reference: extra exports and benchmark, Step 6b - Wiki (only if --wiki flag), Step 7 - Neo4j export (only if --neo4j or --neo4j-push flag), Step 7a - FalkorDB export (only if --falkordb or --falkordb-push flag), Step 7b - SVG export (only if --svg flag), Step 7c - GraphML export (only if --graphml flag), Step 7d - MCP server (only if --mcp flag), Step 8 - Token reduction benchmark (only if total_words > 5000)

### Community 78 - "ComprehensiveAnalyticsDialog.kt"
Cohesion: 0.57
Nodes (6): AnalyticsDateNavigation(), AnalyticsHeaderCard(), ComprehensiveAnalyticsDialog(), EmptyStateCard(), ErrorStateCard(), Modifier

### Community 79 - "ChatToolbar.kt"
Cohesion: 0.22
Nodes (15): ColorWheel(), Color, Modifier, getAdaptiveBubbleColor(), getChatBackgroundColor(), Color, AdvancedColorPickerDialog(), ChatSettingsDialogEnhanced() (+7 more)

### Community 80 - "SortType"
Cohesion: 0.29
Nodes (7): SortType, NAME_ASC, NAME_DESC, QUOTA_COUNT_ASC, QUOTA_COUNT_DESC, TOTAL_WEIGHT_ASC, TOTAL_WEIGHT_DESC

### Community 81 - "StartupState"
Cohesion: 0.33
Nodes (6): Ready, SecurityBlocked, Splash, StartupState, Syncing, VersionExpired

### Community 82 - "HomeNotificationSettingRow.kt"
Cohesion: 0.67
Nodes (5): CompactSwitch(), Color, ImageVector, Modifier, NotificationSettingRow()

### Community 84 - "CoroutineScope"
Cohesion: 0.27
Nodes (8): AtkCargoApplication, BootReceiver, BroadcastReceiver, Context, Intent, KoinComponent, Application, CoroutineScope

### Community 85 - "graphify reference: query, path, explain"
Cohesion: 0.40
Nodes (5): For /graphify explain, For /graphify path, graphify reference: query, path, explain, Step 0 — Constrained query expansion (REQUIRED before traversal), Step 1 — Traversal

### Community 86 - "ApiService.kt"
Cohesion: 0.29
Nodes (3): ApiResponse2, ShipsData, JsonElement

### Community 87 - "ApiResponse"
Cohesion: 0.29
Nodes (3): ApiResponse, DeleteMessageRequest, EditMessageRequest

### Community 88 - "graphify reference: add a URL and watch a folder"
Cohesion: 0.50
Nodes (3): For /graphify add, For --watch, graphify reference: add a URL and watch a folder

### Community 89 - "graphify reference: commit hook and native CLAUDE.md integration"
Cohesion: 0.50
Nodes (3): For git commit hook, For native CLAUDE.md integration, graphify reference: commit hook and native CLAUDE.md integration

### Community 90 - "CargoDetailsScreen.kt"
Cohesion: 0.16
Nodes (18): CargoCounterRoute, cargoCounterScreen(), NavController, navigateToCargoCounter(), CargoCounterOperationScreen(), NavController, CargoDetailsScreen(), confirmCargo() (+10 more)

### Community 93 - "ProfileMenu.kt"
Cohesion: 0.52
Nodes (6): ActionButtons(), getUserTypeDisplay(), Color, ProfileMenu(), ProfilePalette, rememberProfilePalette()

### Community 95 - "MessageInputArea"
Cohesion: 0.67
Nodes (5): Color, com, MessageInputArea(), QuotaSelectionDialog(), ShipSelectionDialog()

### Community 97 - "proxy_generator.php"
Cohesion: 0.57
Nodes (6): _o1(), _o2(), _o3(), _o4(), _o5(), _o6()

### Community 98 - "NotificationActionReceiver.kt"
Cohesion: 0.53
Nodes (4): BroadcastReceiver, Context, Intent, NotificationActionReceiver

### Community 100 - "AppDatabase"
Cohesion: 0.47
Nodes (3): AppDatabase, Context, RoomDatabase

### Community 104 - "LoadingState"
Cohesion: 0.47
Nodes (6): Error, Idle, Loading, LoadingState, Success, UiState

### Community 109 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 117 - "CargoModels.kt"
Cohesion: 0.12
Nodes (10): CargoDeleteResponse, CargoInfoRequest, CargoInfoSearch, CargoSearchResponse, CargoStats, CheckExistenceResponse, ExistingCargo, LoadableTonnageResponse (+2 more)

### Community 123 - "ChatRepository.kt"
Cohesion: 0.22
Nodes (6): ChatMessage, ChatMessagesResponse, SendMessageRequest, SendMessageResponse, UnreadCountResponse, Flow

### Community 137 - "ReportsRepository.kt"
Cohesion: 0.17
Nodes (4): CargoInfoResponse, ComprehensiveAnalysisResponse, QuotaDetails, QuotaStatusResponse

### Community 154 - "PermissionPoller"
Cohesion: 0.36
Nodes (3): Job, StateFlow, PermissionPoller

## Knowledge Gaps
- **328 isolated node(s):** `CONFIG`, `Utils`, `name`, `description`, `type` (+323 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **34 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReportsViewModel` connect `ReportsViewModel` to `formatNumber`, `QuotasListScreen.kt`, `LoginScreen.kt`, `ManageReportsScreen.kt`, `Ship`, `CargoEditSearchDialogsSection.kt`, `LoadingState`, `ReportModels.kt`, `ReportsRepository.kt`, `RealTimeLoadingBottomSheet`, `RealTimeLoadingData`, `ComprehensiveAnalyticsDialog.kt`, `QuotaManagementDialog.kt`, `ApiService.kt`, `InitialInfoScreen.kt`, `ExportPdfUseCase`, `WarehouseDetailsScreen.kt`?**
  _High betweenness centrality (0.056) - this node is a cross-community bridge._
- **Why does `UserPreferencesManager` connect `UserPreferencesManager` to `ChatScreen.kt`, `CargoViewModel.kt`, `LoadingNotificationService`, `LoginScreen.kt`, `Color.kt`, `ManageReportsScreen.kt`, `ChatNotificationWorker.kt`, `MainActivity.kt`, `StartupViewModel`, `SelectInfoScreen.kt`, `CoroutineScope`, `InitialInfoScreen.kt`, `CargoDetailsScreen.kt`, `ProfileMenu.kt`, `CargoCounterScreen.kt`?**
  _High betweenness centrality (0.041) - this node is a cross-community bridge._
- **Why does `CargoViewModel` connect `CargoViewModel` to `CargoViewModel.kt`, `CargoInfo`, `LoginScreen.kt`, `CargoInfoDetailsDialogSection.kt`, `RegisterCargoScreen.kt`, `QuotaExistenceMultipleResponse`, `MessageType`, `SelectInfoScreen.kt`, `InitialInfo`, `ActiveShipInfo`, `InitialInfoScreen.kt`, `CargoDetailsScreen.kt`?**
  _High betweenness centrality (0.029) - this node is a cross-community bridge._
- **What connects `CONFIG`, `Utils`, `name` to the rest of the system?**
  _328 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `QuotasListScreen.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.050560512468542665 - nodes in this community are weakly interconnected._
- **Should `CargoInfo` be split into smaller, more focused modules?**
  _Cohesion score 0.14532019704433496 - nodes in this community are weakly interconnected._
- **Should `UsersManager` be split into smaller, more focused modules?**
  _Cohesion score 0.06936026936026936 - nodes in this community are weakly interconnected._