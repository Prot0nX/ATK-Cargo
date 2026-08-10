# Graph Report - .  (2026-08-10)

## Corpus Check
- 324 files · ~356,634 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2185 nodes · 4712 edges · 169 communities (136 shown, 33 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 163 edges (avg confidence: 0.8)
- Token cost: 284,646 input · 0 output

## Community Hubs (Navigation)
- Presentation
- Dialogs
- Online Users Script
- Class Loader
- App Api Controller
- Domain
- Php
- Cargo View Model
- Security Verifier
- Quota Details
- Reports View Model
- Report Models
- Reports Repository
- Domain
- Theme
- Presentation
- Skill
- Quota Management Dialog
- Ocr
- Cargo Repository
- Api Service
- Components
- Dialogs
- Quota Warning Dialog
- User Management Screen
- Update Manager
- Chat View Model
- User Preferences Manager
- Register Cargo Dialogs
- Api
- Quota Percentage Dialog Section
- Analytics Controller
- Components
- Logger
- Core
- Cargo Models
- Presentation
- Navigation
- Startup View Model
- Api
- Secrets
- Loading Notification Service
- Home Screen
- Core
- Chat Dao
- Quota Analysis Section
- Login Screen
- Session Repository
- User Repository
- Components
- Security Screen
- Composer
- Session Manager
- Input Validator Test
- Session Manager
- Quota Details
- Auth Models
- Reports Common Widgets
- Components
- Ship Info Section
- Auth View Model
- Chat Repository
- Dialogs
- Chat Controller
- Warehouse Details Screen
- App Notification Manager
- Update Dialog
- Data
- Domain
- Main Activity
- Request
- Api
- User Management Dialogs Section
- Auth Repository
- Manage Reports Screen
- Protected Proxy
- Session Service
- Permission Poller
- Startup View Model
- Utility Controller
- Domain
- Date Range Picker
- Cargo Counter Navigation
- Advanced Search Dialog
- Profile Menu
- User Service
- Update Manager
- Comprehensive Analytics Dialog
- Viewmodel
- Chat Notification Worker
- Proxy Generator
- Notification Action Receiver
- Nav Routes
- Startup View Model
- App Database
- Chat Input Bar
- Home Notification Setting Row
- Splash Screen
- Reports View Model
- Jalali Date Utils
- Auth Controller
- User Controller
- Update Manager Factory
- Components
- Cargo Registration Navigation
- Chat Navigation
- Gradlew
- Response
- Cargo Service Test
- Online Users Manager
- Security Dictionary
- Auth Session
- Empty State
- Config
- Session Service Test
- Autoload Real
- Example Instrumented Test
- Constants
- Color Extensions
- Confirmation Dialog
- Search Bar
- Hardware Performance Evaluator
- Component Defaults
- Example Unit Test
- Baseline Profile Generator
- Index

## God Nodes (most connected - your core abstractions)
1. `ReportsViewModel` - 106 edges
2. `CargoViewModel` - 84 edges
3. `CargoInfo` - 57 edges
4. `ApiService` - 56 edges
5. `UserPreferencesManager` - 46 edges
6. `formatNumber()` - 46 edges
7. `UpdateManager` - 38 edges
8. `Logger` - 35 edges
9. `ActiveShipInfo` - 35 edges
10. `ReportsRepository` - 33 edges

## Surprising Connections (you probably didn't know these)
- `CargoInfo composite covering indexes` --semantically_similar_to--> `گزارش بررسی عملکرد و بهینه‌سازی — ATK-Cargo (Performance Optimization Report)`  [INFERRED] [semantically similar]
  گزارش-بررسی-لیست-کشتی‌ها.md → گزارش-بهینه‌سازی-عملکرد.md
- `graphify Always-On Rule` --semantically_similar_to--> `.claude/CLAUDE.md graphify Trigger`  [INFERRED] [semantically similar]
  .agents/rules/graphify.md → .claude/CLAUDE.md
- `graphify Always-On Rule` --semantically_similar_to--> `ATK-Cargo Root CLAUDE.md graphify Instructions`  [INFERRED] [semantically similar]
  .agents/rules/graphify.md → CLAUDE.md
- `ATK-Cargo Root CLAUDE.md graphify Instructions` --semantically_similar_to--> `.claude/CLAUDE.md graphify Trigger`  [INFERRED] [semantically similar]
  CLAUDE.md → .claude/CLAUDE.md
- `Database transactions must be properly handled` --semantically_similar_to--> `P0: DELETE from CargoInfo missing cargoType condition`  [INFERRED] [semantically similar]
  lifeguard.yaml → گزارش-بررسی-کوتاژها-و-انبارها.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Duplicated graphify Project Rule Instructions** — agents_rules_graphify_rule, claude_claude_trigger, claude_project_instructions [INFERRED 0.90]
- **graphify Query/Path/Explain Subsystem** — claude_skills_graphify_skill_query_command, claude_skills_graphify_skill_path_command, claude_skills_graphify_skill_explain_command [EXTRACTED 0.95]
- **graphify Step 3 Extraction Pipeline** — claude_skills_graphify_skill_ast_extraction, claude_skills_graphify_skill_semantic_extraction, claude_skills_graphify_skill_extraction_cache [EXTRACTED 0.95]
- **ATK Cargo comprehensive app audit report series** — gozaresh_barrasi_list_keshtiha, gozaresh_barrasi_kotaj_anbar, گزارش_بهینه_سازی_عملکرد, گزارش_مسیر_راه_اندازی [INFERRED 0.85]
- **Android build obfuscation/R8 dictionary set** — app_dictionary, app_proguard_dictionary, app_security_dictionary [INFERRED 0.85]
- **P0 quota deletion/permission/scoping critical cluster** — gozaresh_barrasi_kotaj_anbar_delete_bug, gozaresh_barrasi_kotaj_anbar_permission_missing, gozaresh_barrasi_kotaj_anbar_toggle_unscoped [EXTRACTED 1.00]

## Communities (169 total, 33 thin omitted)

### Community 0 - "Presentation"
Cohesion: 0.05
Nodes (84): formatNumber(), MatchingQuota, ActiveShipInfo, CargoCounterScreen(), CargoCounterViewModel, CargoSnackbarMessage, filterShipsByTab(), com (+76 more)

### Community 1 - "Dialogs"
Cohesion: 0.08
Nodes (53): CargoInfo, CargoInfoRequest, CargoDetailsDialog(), Color, ImageVector, Modifier, ModernCargoDetailsGrid(), ModernCargoStatusSection() (+45 more)

### Community 2 - "Online Users Script"
Cohesion: 0.07
Nodes (7): ApiManager, CONFIG, ModalManager, ThemeManager, UIManager, UsersManager, Utils

### Community 3 - "Class Loader"
Cohesion: 0.05
Nodes (4): Composer\Semver\VersionParser, ComposerStaticInit5f285832cbbfb0bffdb861048ac414db, ClassLoader, InstalledVersions

### Community 4 - "App Api Controller"
Cohesion: 0.10
Nodes (3): AppApiController, CargoController, MicroCache

### Community 5 - "Domain"
Cohesion: 0.11
Nodes (31): FilteredSummary, VoucherDetail, ExportPdfUseCase, PdfColorScheme, PdfFonts, PersianFontManager, ShamsiDate, persianDateFormat() (+23 more)

### Community 6 - "Php"
Cohesion: 0.05
Nodes (45): CMakeLists.txt (secrets native library build), 16KB page-size linker fix (max-page-size=16384), secrets SHARED library target, baseline-prof.txt (Baseline Profile rules), androidx.activity.ComponentActivity profile rules, گزارش بررسی جزئیات کشتی — کوتاژها و انبارها (Quotas & Warehouses Review Report), P0: DELETE from CargoInfo missing cargoType condition, P0: All write/delete operations use HTTP GET (+37 more)

### Community 8 - "Security Verifier"
Cohesion: 0.09
Nodes (11): Secrets, SecurityErrorType, LICENSE_INACTIVE, LICENSE_NOT_FOUND, NETWORK_ERROR, TAMPERED, UNKNOWN_ERROR, SecurityVerifier (+3 more)

### Community 9 - "Quota Details"
Cohesion: 0.12
Nodes (33): Quota, QuotaEditData, WarehouseQuotaGroupingMode, BY_CARGO_OWNER, BY_SHIPPING_COMPANY, BY_WAREHOUSE, buildQuotasShareText(), shareQuotasData() (+25 more)

### Community 11 - "Report Models"
Cohesion: 0.08
Nodes (24): ThirdPartyApiService, adjustColorForTheme(), AnalyticsData, ColorSelector, DateInfo, ExistingQuota, ExitDateInfo, GroupSortingMode (+16 more)

### Community 12 - "Reports Repository"
Cohesion: 0.09
Nodes (8): QuotaStatusResponse, ErrorResponse, Result, ReportsRepository, Result, validateQuota(), Exception, DatabaseException

### Community 13 - "Domain"
Cohesion: 0.11
Nodes (23): ErrorState(), ImageVector, Modifier, Ship, ShipSortingMode, LOADED_TONNAGE_ASC, LOADED_TONNAGE_DESC, NAME_ASC (+15 more)

### Community 14 - "Theme"
Cohesion: 0.09
Nodes (22): AdaptiveLayoutConfig, rememberAdaptiveLayoutConfig(), WindowSizeClass, COMPACT, EXPANDED, MEDIUM, buildAppDarkColorScheme(), buildAppLightColorScheme() (+14 more)

### Community 15 - "Presentation"
Cohesion: 0.14
Nodes (29): CheckExistenceRequest, formatNumber(), isValidPersianText(), isValidQuotaNumber(), isValidShipName(), isValidWarehouseName(), isValidWeight(), InitialInfoRoute (+21 more)

### Community 16 - "Skill"
Cohesion: 0.11
Nodes (31): graphify Always-On Rule, graphify Workflow Command, .claude/CLAUDE.md graphify Trigger, ATK-Cargo Root CLAUDE.md graphify Instructions, add & watch Reference Doc, Exports & Benchmark Reference Doc, Extraction Subagent Spec Reference Doc, GitHub Clone & Cross-Repo Merge Reference Doc (+23 more)

### Community 17 - "Quota Management Dialog"
Cohesion: 0.16
Nodes (30): QuotaItem, AdvancedFiltersDialog(), AnalyticsStatChipMini(), CompactStatChip(), EmptyQuotaState(), FilterOptionsCard(), InfoChip(), IntegratedQuotaCard() (+22 more)

### Community 18 - "Ocr"
Cohesion: 0.13
Nodes (22): adaptiveThresholding(), applyContrastEnhancement(), enhanceEdges(), ImageProxy, InputImage, medianFilter(), preprocessImage(), EnhancedNumberAnalyzer (+14 more)

### Community 20 - "Api Service"
Cohesion: 0.11
Nodes (9): ApiResponse2, ApiService, CargoInfoResponse, SuccessResponse, ComprehensiveAnalysisResponse, FilteredSummaryResponse, RealTimeDataResponse, ShipsData (+1 more)

### Community 21 - "Components"
Cohesion: 0.13
Nodes (18): InitialInfo, QuotaValidationResult, CargoInfoCard(), CargoListSection(), GroupStats(), InfoChips(), InfoGrid(), InfoGridCard() (+10 more)

### Community 22 - "Dialogs"
Cohesion: 0.15
Nodes (23): RealTimeLoadingData, ShiftInfo, addOneDayToPersianDate(), format(), formatHoursToPersian(), formatNumber(), formatWeightWithDetail(), getDaysInPersianMonth() (+15 more)

### Community 23 - "Quota Warning Dialog"
Cohesion: 0.17
Nodes (24): WarningStatus, calculateWarningStatus(), ShipDetails(), ShipHeaderCard(), WarehouseQuotasTabs(), WarehousesAndQuotasTab(), CompactInfoSection(), DialogHeader() (+16 more)

### Community 24 - "User Management Screen"
Cohesion: 0.16
Nodes (21): DeleteUserRequest, ForceLogoutRequest, UpdateUserRequest, User, EnhancedDeleteConfirmationDialog(), EnhancedForceLogoutDialog(), DashboardSummaryRow(), EmptyStateView() (+13 more)

### Community 25 - "Update Manager"
Cohesion: 0.13
Nodes (7): DownloadProgress, Context, Job, StateFlow, ViewModel, UpdateManager, VersionCheckResult

### Community 26 - "Chat View Model"
Cohesion: 0.10
Nodes (7): ChatViewModel, ChatViewModelFactory, com, StateFlow, T, ViewModel, ViewModelProvider

### Community 27 - "User Preferences Manager"
Cohesion: 0.11
Nodes (6): TypeToken, Flow, T, UserPreferencesManager, DataStore, Preferences

### Community 28 - "Register Cargo Dialogs"
Cohesion: 0.19
Nodes (21): CargoOperationScreen(), NavController, AnimatedIcon(), DialogContent(), DialogPassword(), DuplicateConfirmationDialog(), DuplicateTrackingNumbersDialog(), ExitStatusDialog() (+13 more)

### Community 29 - "Api"
Cohesion: 0.15
Nodes (13): FloatTypeAdapter, Context, RetrofitClient, ThirdPartyRetrofitClient, CryptoManager, ByteArray, Cipher, JsonReader (+5 more)

### Community 30 - "Quota Percentage Dialog Section"
Cohesion: 0.17
Nodes (21): CalculationResult, QuotaPercentageData, AnimatedNumber(), DialogHeader(), Color, ImageVector, Modifier, lerp() (+13 more)

### Community 31 - "Analytics Controller"
Cohesion: 0.21
Nodes (10): gregorian_to_jalali(), jalali_to_gregorian(), jcheckdate(), jdate(), jdate_words(), jgetdate(), jmktime(), jstrftime() (+2 more)

### Community 32 - "Components"
Cohesion: 0.16
Nodes (14): Modifier, StatusSnackbar(), MessageType, ERROR, SUCCESS, WARNING, CargoSnackbarQueue, StateFlow (+6 more)

### Community 33 - "Logger"
Cohesion: 0.14
Nodes (4): LicenseController, OnlineUsersController, Logger, self

### Community 34 - "Core"
Cohesion: 0.18
Nodes (3): mysqli, mysqli_stmt, DatabaseManager

### Community 35 - "Cargo Models"
Cohesion: 0.11
Nodes (11): CargoInfoSearch, CargoSearchResponse, CargoStats, CheckExistenceResponse, ExistingCargo, LoadableTonnageResponse, QuotaExistenceMultipleResponse, QuotaTonnageWarning (+3 more)

### Community 36 - "Presentation"
Cohesion: 0.18
Nodes (17): Result, validateServerSession(), CargoDetailsScreen(), confirmCargo(), FloatingActionButtonItem(), handleCargoConfirmation(), handleQuotaChangeInDetails(), Color (+9 more)

### Community 37 - "Navigation"
Cohesion: 0.17
Nodes (17): MainScreen(), RouteTransitions, standardTransitions(), LoginScreen(), HomeRoute, homeScreen(), NavController, navigateToHome() (+9 more)

### Community 38 - "Startup View Model"
Cohesion: 0.16
Nodes (3): Intent, SecurityCheckState, StartupViewModel

### Community 39 - "Api"
Cohesion: 0.13
Nodes (9): ApiResponse, ChatMessage, ChatMessagesResponse, SendMessageRequest, SendMessageResponse, UnreadCountResponse, DeleteMessageRequest, EditMessageRequest (+1 more)

### Community 40 - "Secrets"
Cohesion: 0.41
Nodes (17): decryptXor(), Java_com_atk_atk_1cargo_api_Secrets_getApiKey(), Java_com_atk_atk_1cargo_api_Secrets_getAuthenticationX365(), Java_com_atk_atk_1cargo_api_Secrets_getAuthUser(), Java_com_atk_atk_1cargo_api_Secrets_getBaseUrl(), Java_com_atk_atk_1cargo_api_Secrets_getExpectedSignatureHash(), Java_com_atk_atk_1cargo_api_Secrets_getLicenseCheckUrl(), Java_com_atk_atk_1cargo_api_Secrets_getLicenseInfoUrl() (+9 more)

### Community 41 - "Loading Notification Service"
Cohesion: 0.22
Nodes (6): Context, Intent, KoinComponent, LoadingNotificationService, IBinder, Service

### Community 42 - "Home Screen"
Cohesion: 0.23
Nodes (13): MenuItem, getMenuItemsForUserType(), CategorizedMenuGrid(), CategoryHeader(), CompactMenuCard(), Header(), HomeScreen(), isWideCard() (+5 more)

### Community 43 - "Core"
Cohesion: 0.13
Nodes (5): Config, self, Database, PDO, self

### Community 44 - "Chat Dao"
Cohesion: 0.16
Nodes (3): ChatDao, Flow, ChatMessageEntity

### Community 45 - "Quota Analysis Section"
Cohesion: 0.28
Nodes (16): QuotaCompletionData, QuotaGroupingMode, BY_CARGO_OWNER, BY_CARRIER, BY_SHIP, AnalyticsGroupingModeButton(), AnalyticsOwnerSummaryCard(), AnalyticsQuotaCard() (+8 more)

### Community 46 - "Login Screen"
Cohesion: 0.26
Nodes (16): CompactLoginLayout(), ExpandedLoginLayout(), IndustrialBrandingHeaderContent(), IndustrialFeatureBadge(), IndustrialFooterInfo(), IndustrialInputField(), IndustrialLoginFormCard(), androidx (+8 more)

### Community 47 - "Session Repository"
Cohesion: 0.12
Nodes (3): PDO, PDO, SessionRepository

### Community 49 - "Components"
Cohesion: 0.24
Nodes (14): extractShipInfoAndText(), com, replaceUsernamesWithFullNames(), rotateIcon(), ShipInfoModel, ChatInfoRow(), EditMessageDialog(), androidx (+6 more)

### Community 50 - "Security Screen"
Cohesion: 0.25
Nodes (13): AdvancedOrbitalScanner(), AnimatedUpdateIcon(), AnimatedWarningIcon(), DynamicPremiumBackground(), InteractiveExitButton(), Modifier, RetryButton(), SecurityBlockScreen() (+5 more)

### Community 51 - "Composer"
Cohesion: 0.12
Nodes (15): autoload, autoload-dev, psr-4, psr-4, config, optimize-autoloader, sort-packages, description (+7 more)

### Community 53 - "Input Validator Test"
Cohesion: 0.17
Nodes (3): InputValidator, InputValidatorTest, PHPUnit\Framework\TestCase

### Community 55 - "Quota Details"
Cohesion: 0.26
Nodes (13): ApiQuotaDetails, adjustColorForTheme(), Color, toTon(), calculatePercentage(), calculateProgress(), ProgressBar(), QuotaAdditionalInfo() (+5 more)

### Community 56 - "Auth Models"
Cohesion: 0.15
Nodes (8): ActiveSessionResponse, ForceLogoutResponse, LoginRequest, LoginResponse, PasswordCheckResponse, PermissionSyncRequest, PermissionSyncResponse, SessionResponse

### Community 57 - "Reports Common Widgets"
Cohesion: 0.29
Nodes (14): FabItem, CompactStatChip(), EmptyQuotaState(), EmptyShipsState(), EmptyStateCard(), ErrorStateCard(), FloatingActionButton(), InfoCard() (+6 more)

### Community 58 - "Components"
Cohesion: 0.25
Nodes (13): ColorWheel(), Color, Modifier, getAdaptiveBubbleColor(), AdvancedColorPickerDialog(), ChatSettingsDialogEnhanced(), ChatSettingsPreviewRefined(), Color (+5 more)

### Community 59 - "Ship Info Section"
Cohesion: 0.30
Nodes (13): ShipInfo, formatNumber(), toEnglishNumbers(), DetailedInfoGrid(), DetailInfoItem(), ExpandedContent(), Color, ImageVector (+5 more)

### Community 60 - "Auth View Model"
Cohesion: 0.17
Nodes (8): AuthViewModel, Error, Idle, StateFlow, ViewModel, Loading, LoginUiState, Success

### Community 61 - "Chat Repository"
Cohesion: 0.22
Nodes (3): ChatRepository, com, Result

### Community 62 - "Dialogs"
Cohesion: 0.19
Nodes (6): AnimatedCounter(), ExitStatusDialog(), ImageVector, VazirmatnFontFamily, FontFamily, FontWeight

### Community 64 - "Warehouse Details Screen"
Cohesion: 0.29
Nodes (11): Warehouse, ExportOptions(), Modifier, QuotaChip(), QuotaSelector(), VoucherDetailsButton(), WarehouseCard(), WarehouseContent() (+3 more)

### Community 66 - "Update Dialog"
Cohesion: 0.31
Nodes (12): UpdateInfo, DownloadActionButtons(), DownloadingState(), DownloadProgressInfo(), DownloadSizeInfo(), DownloadSpeedInfo(), ErrorState(), ModernUpdateActionSection() (+4 more)

### Community 67 - "Data"
Cohesion: 0.23
Nodes (4): AuthRepositoryImpl, Flow, hashPassword(), SecurityUtilsTest

### Community 68 - "Domain"
Cohesion: 0.27
Nodes (12): ChatUiItem, getChatBackgroundColor(), getDateHeaderColor(), Header, Color, Message, ChatScreen(), DateHeader() (+4 more)

### Community 69 - "Main Activity"
Cohesion: 0.28
Nodes (5): ServerSyncingScreen(), Intent, MainActivity, Bundle, ComponentActivity

### Community 71 - "Api"
Cohesion: 0.27
Nodes (8): AtkCargoApplication, BootReceiver, BroadcastReceiver, Context, Intent, KoinComponent, Application, CoroutineScope

### Community 72 - "User Management Dialogs Section"
Cohesion: 0.36
Nodes (11): CreateUserRequest, UserTypeInfo, DesignTextField(), EnhancedAddUserDialog(), EnhancedEditUserDialog(), getUserTypes(), androidx, KeyboardOptions (+3 more)

### Community 73 - "Auth Repository"
Cohesion: 0.23
Nodes (7): AuthRepository, ConflictSession, Error, Flow, LoginResult, Success, LoginUseCase

### Community 74 - "Manage Reports Screen"
Cohesion: 0.36
Nodes (11): addOneDayToPersianDate(), DatePickerDialog(), DateTimeSelectionCard(), getDaysInPersianMonth(), isPersianLeapYear(), Color, ImageVector, Modifier (+3 more)

### Community 77 - "Permission Poller"
Cohesion: 0.36
Nodes (3): Job, StateFlow, PermissionPoller

### Community 78 - "Startup View Model"
Cohesion: 0.22
Nodes (7): AndroidViewModel, StateFlow, RequestBatteryOptimization, ShowMessage, StartupEvent, SessionCheckRequest, SharedFlow

### Community 81 - "Domain"
Cohesion: 0.29
Nodes (4): LogoutRequest, LogoutResponse, Result, LogoutUseCase

### Community 82 - "Date Range Picker"
Cohesion: 0.57
Nodes (7): DateClickableField(), DateRangePicker(), getDaysInPersianMonth(), isPersianLeapYear(), Color, Modifier, PersianDatePickerDialog()

### Community 83 - "Cargo Counter Navigation"
Cohesion: 0.36
Nodes (6): CargoCounterRoute, cargoCounterScreen(), NavController, navigateToCargoCounter(), CargoCounterOperationScreen(), NavController

### Community 84 - "Advanced Search Dialog"
Cohesion: 0.36
Nodes (5): ThemeColorOption, AdvancedSearchDialog(), Modifier, SearchTabInfo, SearchTypeTabRow()

### Community 85 - "Profile Menu"
Cohesion: 0.43
Nodes (7): ThemeColorPickerRow(), ActionButtons(), getUserTypeDisplay(), Color, ProfileMenu(), ProfilePalette, rememberProfilePalette()

### Community 87 - "Update Manager"
Cohesion: 0.29
Nodes (6): Completed, Downloading, DownloadState, Error, Idle, Paused

### Community 88 - "Comprehensive Analytics Dialog"
Cohesion: 0.57
Nodes (6): AnalyticsDateNavigation(), AnalyticsHeaderCard(), ComprehensiveAnalyticsDialog(), EmptyStateCard(), ErrorStateCard(), Modifier

### Community 89 - "Viewmodel"
Cohesion: 0.33
Nodes (5): CargoViewModelFactory, StateFlow, T, ViewModel, ViewModelProvider

### Community 90 - "Chat Notification Worker"
Cohesion: 0.48
Nodes (4): ChatNotificationWorker, KoinComponent, CoroutineWorker, ListenableWorker

### Community 91 - "Proxy Generator"
Cohesion: 0.57
Nodes (6): _o1(), _o2(), _o3(), _o4(), _o5(), _o6()

### Community 92 - "Notification Action Receiver"
Cohesion: 0.53
Nodes (4): BroadcastReceiver, Context, Intent, NotificationActionReceiver

### Community 94 - "Startup View Model"
Cohesion: 0.33
Nodes (6): Ready, SecurityBlocked, Splash, StartupState, Syncing, VersionExpired

### Community 95 - "App Database"
Cohesion: 0.47
Nodes (3): AppDatabase, Context, RoomDatabase

### Community 96 - "Chat Input Bar"
Cohesion: 0.67
Nodes (5): Color, com, MessageInputArea(), QuotaSelectionDialog(), ShipSelectionDialog()

### Community 97 - "Home Notification Setting Row"
Cohesion: 0.67
Nodes (5): CompactSwitch(), Color, ImageVector, Modifier, NotificationSettingRow()

### Community 98 - "Splash Screen"
Cohesion: 0.53
Nodes (5): SplashScreen(), SplashScreenConstants, SplashScreenContent(), SplashSubtitleBadge(), SplashVersionBadge()

### Community 99 - "Reports View Model"
Cohesion: 0.47
Nodes (6): Error, Idle, Loading, LoadingState, Success, UiState

### Community 103 - "Update Manager Factory"
Cohesion: 0.50
Nodes (3): T, ViewModelProvider, UpdateManagerFactory

### Community 104 - "Components"
Cohesion: 0.70
Nodes (4): Color, ImageVector, Modifier, StatisticsCard()

### Community 105 - "Cargo Registration Navigation"
Cohesion: 0.60
Nodes (4): CargoRegistrationRoute, cargoRegistrationScreen(), NavController, navigateToCargoRegistration()

### Community 106 - "Chat Navigation"
Cohesion: 0.50
Nodes (4): AdminChatRoute, adminChatScreen(), NavController, navigateToAdminChat()

### Community 107 - "Gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 110 - "Online Users Manager"
Cohesion: 0.50
Nodes (4): Online Users Manager Page, exportToCSV function call, Logout options (all/except-admin/operators), showAdvancedStats function call

### Community 111 - "Security Dictionary"
Cohesion: 0.50
Nodes (4): app/dictionary.txt obfuscation token list, app/proguard-dictionary.txt name list, app/security-dictionary.txt (adaptclassstrings mapping), com.atk.atk_cargo.MainActivity reference

### Community 113 - "Empty State"
Cohesion: 0.83
Nodes (3): EmptyState(), ImageVector, Modifier

## Ambiguous Edges - Review These
- `Online Users Manager Page` → `Online Users Manager Page`  [AMBIGUOUS]
  PHP/User/online_users_manager.html · relation: conceptually_related_to

## Knowledge Gaps
- **109 isolated node(s):** `CONFIG`, `Utils`, `name`, `description`, `type` (+104 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **33 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Online Users Manager Page` and `Online Users Manager Page`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **Why does `validateServerSession()` connect `Presentation` to `Presentation`, `Navigation`, `Manage Reports Screen`, `Reports Repository`, `Startup View Model`, `User Preferences Manager`?**
  _High betweenness centrality (0.090) - this node is a cross-community bridge._
- **Why does `ReportsViewModel` connect `Reports View Model` to `Warehouse Details Screen`, `Dialogs`, `Reports View Model`, `Navigation`, `Domain`, `Quota Details`, `Manage Reports Screen`, `Report Models`, `Quota Analysis Section`, `Domain`, `Domain`, `Quota Management Dialog`, `Api Service`, `Quota Details`, `Dialogs`, `Quota Warning Dialog`, `Comprehensive Analytics Dialog`?**
  _High betweenness centrality (0.089) - this node is a cross-community bridge._
- **Why does `UserPreferencesManager` connect `User Preferences Manager` to `Presentation`, `Data`, `Presentation`, `Navigation`, `Domain`, `Api`, `Main Activity`, `Loading Notification Service`, `Chat Navigation`, `Manage Reports Screen`, `Startup View Model`, `Domain`, `Advanced Search Dialog`, `Profile Menu`, `User Management Screen`, `Viewmodel`, `Chat Notification Worker`?**
  _High betweenness centrality (0.072) - this node is a cross-community bridge._
- **What connects `CONFIG`, `Utils`, `name` to the rest of the system?**
  _109 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Presentation` be split into smaller, more focused modules?**
  _Cohesion score 0.05009276437847866 - nodes in this community are weakly interconnected._
- **Should `Dialogs` be split into smaller, more focused modules?**
  _Cohesion score 0.08408953418027829 - nodes in this community are weakly interconnected._