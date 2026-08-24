<?php
// PHP/MySQL_Manager/index.php — پنل مدیریت/مانیتورینگ/بهینه‌سازی MySQL.
// فاز ۱: اسکلت + Auth + Sidebar + Dashboard + Databases/Tables (Read-only).
// بخش‌های Optimization/Backup/Restore/Cleanup/Storage/Indexes/Monitoring/Scheduler/Logs در فازهای بعدی تکمیل می‌شوند
// و فعلاً به‌صورت placeholder با نشان «به‌زودی» نمایش داده می‌شوند تا ساختار کامل Sidebar از همان فاز ۱ دیده شود.

declare(strict_types=1);

require_once __DIR__ . '/_guard.php';

use App\Core\Csrf;

mys_require_auth_page();
mys_send_page_headers();

$csrfToken = Csrf::token();
$theme = mys_theme();
?>
<!DOCTYPE html>
<html lang="fa" dir="rtl"<?= $theme !== '' ? ' data-theme="' . e($theme) . '"' : '' ?>>

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="csrf-token" content="<?= e($csrfToken) ?>">
    <title>مدیریت MySQL — ATK Cargo</title>
    <link rel="icon" type="image/x-icon" href="assets/img/fav.ico">
    <link rel="preload" href="../assets/ui/fonts/Vazirmatn-Regular.woff2" as="font" type="font/woff2" crossorigin>
    <link rel="stylesheet" href="../assets/ui/core.css?v=<?= filemtime(__DIR__ . '/../assets/ui/core.css') ?>">
    <link rel="stylesheet" href="assets/app.css?v=<?= filemtime(__DIR__ . '/assets/app.css') ?>">
</head>

<body>
<?php require __DIR__ . '/_icons.php'; ?>

    <header class="topbar">
        <div class="topbar-brand">
            <button type="button" class="btn-icon mys-only-mobile" id="drawerToggle" aria-label="باز کردن فهرست">
                <svg class="icon" aria-hidden="true"><use href="#menu"></use></svg>
            </button>
            <svg class="icon" aria-hidden="true"><use href="#database"></use></svg>
            <span>مدیریت MySQL</span>
        </div>

        <div class="topbar-actions">
            <span class="badge" id="connectionBadge">
                <svg class="icon mys-badge-icon" aria-hidden="true"><use href="#activity"></use></svg>
                <span id="connectionLabel">در حال بررسی اتصال…</span>
            </span>
            <button type="button" class="btn-icon" id="themeToggle" aria-label="تغییر پوسته">
                <svg class="icon icon-light-only" aria-hidden="true"><use href="#moon"></use></svg>
                <svg class="icon icon-dark-only" aria-hidden="true"><use href="#sun"></use></svg>
            </button>
            <form method="post" action="logout.php" class="inline-form">
                <input type="hidden" name="csrf_token" value="<?= e($csrfToken) ?>">
                <button type="submit" class="btn btn-ghost">
                    <svg class="icon" aria-hidden="true"><use href="#logout"></use></svg>
                    <span>خروج</span>
                </button>
            </form>
        </div>
    </header>

    <div class="mys-shell">
        <div class="mys-drawer-backdrop" id="drawerBackdrop"></div>

        <nav class="mys-sidebar" id="sidebar" aria-label="فهرست اصلی">
            <button type="button" class="mys-sidebar-collapse" id="sidebarCollapse" aria-label="جمع کردن فهرست">
                <svg class="icon" aria-hidden="true"><use href="#chevrons-left"></use></svg>
            </button>

            <ul class="mys-sidebar-list">
                <li>
                    <button type="button" class="mys-sidebar-item is-active" data-view="dashboard">
                        <svg class="icon" aria-hidden="true"><use href="#gauge"></use></svg>
                        <span>داشبورد</span>
                    </button>
                </li>
                <li>
                    <button type="button" class="mys-sidebar-item" data-view="databases">
                        <svg class="icon" aria-hidden="true"><use href="#database"></use></svg>
                        <span>دیتابیس‌ها</span>
                    </button>
                </li>
                <li>
                    <button type="button" class="mys-sidebar-item" data-view="tables">
                        <svg class="icon" aria-hidden="true"><use href="#table"></use></svg>
                        <span>جدول‌ها</span>
                    </button>
                </li>
                <li>
                    <button type="button" class="mys-sidebar-item" data-view="optimization">
                        <svg class="icon" aria-hidden="true"><use href="#gauge"></use></svg>
                        <span>بهینه‌سازی</span>
                    </button>
                </li>
                <li>
                    <button type="button" class="mys-sidebar-item" data-view="backup">
                        <svg class="icon" aria-hidden="true"><use href="#download"></use></svg>
                        <span>پشتیبان‌گیری</span>
                    </button>
                </li>
                <li>
                    <button type="button" class="mys-sidebar-item" data-view="restore">
                        <svg class="icon" aria-hidden="true"><use href="#upload"></use></svg>
                        <span>بازیابی</span>
                    </button>
                </li>
                <li>
                    <button type="button" class="mys-sidebar-item" data-view="cleanup">
                        <svg class="icon" aria-hidden="true"><use href="#broom"></use></svg>
                        <span>پاکسازی</span>
                    </button>
                </li>
                <li>
                    <button type="button" class="mys-sidebar-item" data-view="storage">
                        <svg class="icon" aria-hidden="true"><use href="#layers"></use></svg>
                        <span>تحلیل حجم</span>
                    </button>
                </li>
                <li>
                    <button type="button" class="mys-sidebar-item" data-view="indexes">
                        <svg class="icon" aria-hidden="true"><use href="#list"></use></svg>
                        <span>تحلیل ایندکس</span>
                    </button>
                </li>
                <li>
                    <button type="button" class="mys-sidebar-item" data-view="monitoring">
                        <svg class="icon" aria-hidden="true"><use href="#activity"></use></svg>
                        <span>مانیتورینگ</span>
                    </button>
                </li>
                <li>
                    <button type="button" class="mys-sidebar-item" data-view="scheduler">
                        <svg class="icon" aria-hidden="true"><use href="#clock"></use></svg>
                        <span>زمان‌بندی</span>
                    </button>
                </li>
                <li>
                    <button type="button" class="mys-sidebar-item" data-view="logs">
                        <svg class="icon" aria-hidden="true"><use href="#shield"></use></svg>
                        <span>گزارش‌ها</span>
                    </button>
                </li>
            </ul>
        </nav>

        <main class="mys-main container">

            <!-- ================= Dashboard ================= -->
            <section class="mys-view is-active" data-view="dashboard" aria-label="داشبورد">
                <div class="page-title">
                    <h1>داشبورد</h1>
                    <button type="button" class="btn btn-ghost btn-sm" data-refresh="dashboard">
                        <svg class="icon" aria-hidden="true"><use href="#refresh"></use></svg>
                        <span>بروزرسانی</span>
                    </button>
                </div>

                <div class="mys-health-card">
                    <div class="mys-health-head">
                        <span>سلامت دیتابیس</span>
                        <span class="mys-health-label" id="healthLabel">—</span>
                    </div>
                    <div class="mys-health-bar">
                        <svg class="mys-health-bar-svg" viewBox="0 0 100 10" preserveAspectRatio="none" aria-hidden="true">
                            <rect class="mys-health-bar-fill" id="healthBarFill" x="0" y="0" width="0" height="10" rx="5" />
                        </svg>
                    </div>
                    <ul class="mys-health-reasons" id="healthReasons"></ul>
                </div>

                <section class="stats" id="dashboardStats" aria-label="آمار کلی"></section>
            </section>

            <!-- ================= Databases ================= -->
            <section class="mys-view" data-view="databases" aria-label="دیتابیس‌ها">
                <div class="page-title">
                    <h1>دیتابیس‌ها</h1>
                </div>
                <p class="muted">این پنل عمداً فقط روی دیتابیس متصل‌شده‌ی برنامه کار می‌کند.</p>
                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th scope="col">نام دیتابیس</th>
                                <th scope="col">Character Set</th>
                                <th scope="col">Collation</th>
                                <th scope="col">تعداد جدول</th>
                                <th scope="col">حجم داده</th>
                                <th scope="col">حجم ایندکس</th>
                                <th scope="col">حجم کل</th>
                                <th scope="col"><span class="sr-only">عملیات</span></th>
                            </tr>
                        </thead>
                        <tbody id="databasesBody"></tbody>
                    </table>
                    <p class="table-state" id="databasesLoading">در حال بارگذاری…</p>
                </div>
            </section>

            <!-- ================= Tables ================= -->
            <section class="mys-view" data-view="tables" aria-label="جدول‌ها">
                <div class="page-title">
                    <h1>جدول‌ها</h1>
                    <span class="muted" id="tablesCount"></span>
                </div>

                <div class="toolbar">
                    <div class="toolbar-search">
                        <svg class="icon" aria-hidden="true"><use href="#search"></use></svg>
                        <input type="search" id="tablesSearch" placeholder="جست‌وجوی نام جدول…" aria-label="جست‌وجوی جدول">
                    </div>
                    <div class="toolbar-actions">
                        <button type="button" class="btn btn-ghost btn-sm" data-refresh="tables">
                            <svg class="icon" aria-hidden="true"><use href="#refresh"></use></svg>
                            <span>بروزرسانی</span>
                        </button>
                    </div>
                </div>

                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th scope="col" data-sort="name">نام جدول</th>
                                <th scope="col">موتور</th>
                                <th scope="col" data-sort="rows">تعداد ردیف</th>
                                <th scope="col" data-sort="size">حجم داده</th>
                                <th scope="col">حجم ایندکس</th>
                                <th scope="col">فضای هدررفته</th>
                                <th scope="col">Collation</th>
                                <th scope="col" data-sort="updated_at">بروزرسانی</th>
                                <th scope="col">وضعیت</th>
                            </tr>
                        </thead>
                        <tbody id="tablesBody"></tbody>
                    </table>
                    <p class="table-state" id="tablesLoading">در حال بارگذاری…</p>
                    <p class="table-state is-hidden" id="tablesEmpty">جدولی برای نمایش وجود ندارد.</p>
                </div>

                <nav class="pagination" id="tablesPagination" aria-label="صفحه‌بندی جدول‌ها"></nav>
            </section>

            <!-- ================= Optimization ================= -->
            <section class="mys-view" data-view="optimization" aria-label="بهینه‌سازی">
                <div class="page-title">
                    <h1>بهینه‌سازی</h1>
                    <button type="button" class="btn btn-ghost btn-sm" data-refresh="optimization">
                        <svg class="icon" aria-hidden="true"><use href="#refresh"></use></svg>
                        <span>بروزرسانی</span>
                    </button>
                </div>

                <h2 class="mys-subheading">توصیه‌های بهینه‌سازی</h2>
                <div id="optimizationRecommendations" class="mys-recommend-list"></div>
                <p class="muted is-hidden" id="optimizationRecommendationsEmpty">در حال حاضر توصیه‌ای برای بهینه‌سازی وجود ندارد.</p>

                <div class="toolbar">
                    <div class="toolbar-search">
                        <svg class="icon" aria-hidden="true"><use href="#search"></use></svg>
                        <input type="search" id="optimizationSearch" placeholder="جست‌وجوی نام جدول…" aria-label="جست‌وجوی جدول">
                    </div>
                    <div class="toolbar-actions">
                        <span class="muted" id="optimizationSelectedCount"></span>
                        <button type="button" class="btn btn-ghost btn-sm" id="bulkOptimizeBtn" disabled>
                            <svg class="icon" aria-hidden="true"><use href="#gauge"></use></svg>
                            <span>Optimize گروهی</span>
                        </button>
                        <button type="button" class="btn btn-ghost btn-sm" id="bulkAnalyzeBtn" disabled>
                            <svg class="icon" aria-hidden="true"><use href="#play"></use></svg>
                            <span>Analyze گروهی</span>
                        </button>
                        <button type="button" class="btn btn-ghost btn-sm" id="bulkCheckBtn" disabled>
                            <svg class="icon" aria-hidden="true"><use href="#check"></use></svg>
                            <span>Check گروهی</span>
                        </button>
                    </div>
                </div>

                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th scope="col"><input type="checkbox" id="optimizationSelectAll" aria-label="انتخاب همه"></th>
                                <th scope="col">نام جدول</th>
                                <th scope="col">موتور</th>
                                <th scope="col">فضای هدررفته</th>
                                <th scope="col">حجم کل</th>
                                <th scope="col"><span class="sr-only">عملیات</span></th>
                            </tr>
                        </thead>
                        <tbody id="optimizationBody"></tbody>
                    </table>
                    <p class="table-state" id="optimizationLoading">در حال بارگذاری…</p>
                </div>

                <h2 class="mys-subheading">تاریخچه‌ی عملیات اخیر</h2>
                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th scope="col">جدول</th>
                                <th scope="col">عملیات</th>
                                <th scope="col">وضعیت</th>
                                <th scope="col">حجم قبل</th>
                                <th scope="col">حجم بعد</th>
                                <th scope="col">فضای آزادشده</th>
                                <th scope="col">مدت اجرا</th>
                                <th scope="col">زمان</th>
                            </tr>
                        </thead>
                        <tbody id="optimizationHistoryBody"></tbody>
                    </table>
                    <p class="table-state" id="optimizationHistoryEmpty">هنوز عملیاتی ثبت نشده است.</p>
                </div>
            </section>

            <!-- ================= Storage Analysis ================= -->
            <section class="mys-view" data-view="storage" aria-label="تحلیل حجم">
                <div class="page-title">
                    <h1>تحلیل حجم</h1>
                    <button type="button" class="btn btn-ghost btn-sm" data-refresh="storage">
                        <svg class="icon" aria-hidden="true"><use href="#refresh"></use></svg>
                        <span>بروزرسانی</span>
                    </button>
                </div>
                <section class="stats" id="storageStats" aria-label="آمار حجم"></section>
                <div class="mys-two-col">
                    <div>
                        <h2 class="mys-subheading">بزرگ‌ترین جدول‌ها</h2>
                        <div id="storageChart" class="chart-scroll"></div>
                    </div>
                    <div>
                        <h2 class="mys-subheading">بزرگ‌ترین ایندکس‌ها (بر اساس جدول)</h2>
                        <div class="table-wrap">
                            <table class="data-table">
                                <thead><tr><th scope="col">جدول</th><th scope="col">حجم ایندکس</th></tr></thead>
                                <tbody id="topIndexesBody"></tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </section>

            <!-- ================= Index Analysis ================= -->
            <section class="mys-view" data-view="indexes" aria-label="تحلیل ایندکس">
                <div class="page-title">
                    <h1>تحلیل ایندکس</h1>
                    <button type="button" class="btn btn-ghost btn-sm" data-refresh="indexes">
                        <svg class="icon" aria-hidden="true"><use href="#refresh"></use></svg>
                        <span>بروزرسانی</span>
                    </button>
                </div>
                <div id="indexIssues"></div>
                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th scope="col">جدول</th>
                                <th scope="col">ایندکس</th>
                                <th scope="col">نوع</th>
                                <th scope="col">ستون</th>
                                <th scope="col">Cardinality</th>
                                <th scope="col">یکتا</th>
                            </tr>
                        </thead>
                        <tbody id="indexesBody"></tbody>
                    </table>
                    <p class="table-state" id="indexesLoading">در حال بارگذاری…</p>
                </div>
            </section>

            <!-- ================= Backup ================= -->
            <section class="mys-view" data-view="backup" aria-label="پشتیبان‌گیری">
                <div class="page-title">
                    <h1>پشتیبان‌گیری</h1>
                    <button type="button" class="btn btn-ghost btn-sm" data-refresh="backup">
                        <svg class="icon" aria-hidden="true"><use href="#refresh"></use></svg>
                        <span>بروزرسانی</span>
                    </button>
                </div>

                <div class="mys-two-col">
                    <div class="mys-card">
                        <h2 class="mys-subheading mys-subheading-flush">پشتیبان‌گیری دستی</h2>
                        <fieldset class="mys-radio-group">
                            <legend class="field-label">نوع پشتیبان</legend>
                            <label class="field-check"><input type="radio" name="backupType" value="full" checked> Full Database (کل دیتابیس)</label>
                            <label class="field-check"><input type="radio" name="backupType" value="tables"> Selected Tables (جدول‌های منتخب)</label>
                            <label class="field-check"><input type="radio" name="backupType" value="structure"> Structure Only (فقط ساختار)</label>
                            <label class="field-check"><input type="radio" name="backupType" value="data"> Data Only (فقط داده)</label>
                        </fieldset>

                        <div id="backupTablesPicker" class="mys-table-picker is-hidden">
                            <span class="field-label">جدول‌های موردنظر</span>
                            <div id="backupTablesList" class="mys-checkbox-list"></div>
                        </div>

                        <label class="field-check">
                            <input type="checkbox" id="backupCompress" checked>
                            فشرده‌سازی خروجی (gzip)
                        </label>

                        <button type="button" class="btn btn-primary mys-mt-3" id="startBackupBtn">
                            <svg class="icon" aria-hidden="true"><use href="#download"></use></svg>
                            <span>شروع پشتیبان‌گیری</span>
                        </button>
                    </div>

                    <div class="mys-card">
                        <h2 class="mys-subheading mys-subheading-flush">پشتیبان‌گیری خودکار</h2>
                        <label class="field-check">
                            <input type="checkbox" id="autoBackupEnabled">
                            فعال‌سازی پشتیبان‌گیری خودکار
                        </label>

                        <label class="field">
                            <span class="field-label">دوره‌ی تکرار</span>
                            <select id="autoBackupFrequency">
                                <option value="daily">روزانه</option>
                                <option value="weekly">هفتگی</option>
                                <option value="monthly">ماهانه</option>
                            </select>
                        </label>

                        <label class="field">
                            <span class="field-label">ساعت اجرا</span>
                            <input type="time" id="autoBackupTime" value="02:00">
                        </label>

                        <label class="field">
                            <span class="field-label">تعداد پشتیبان نگه‌داشته‌شده (Retention)</span>
                            <select id="autoBackupRetention">
                                <option value="1">۱</option>
                                <option value="3">۳</option>
                                <option value="7" selected>۷</option>
                                <option value="14">۱۴</option>
                                <option value="30">۳۰</option>
                                <option value="90">۹۰</option>
                            </select>
                        </label>

                        <label class="field-check"><input type="checkbox" id="autoBackupCompression" checked> فشرده‌سازی</label>
                        <label class="field-check"><input type="checkbox" id="autoBackupIncludeStructure" checked> شامل ساختار</label>
                        <label class="field-check"><input type="checkbox" id="autoBackupIncludeData" checked> شامل داده</label>

                        <button type="button" class="btn btn-ghost mys-mt-3" id="saveBackupSettingsBtn">ذخیره‌ی تنظیمات</button>
                        <p class="muted mys-mt-2">اجرای واقعی زمان‌بندی‌شده در فاز ۴ (Scheduler) از طریق Cron سرور اضافه می‌شود؛ این تنظیمات از هم‌اکنون ذخیره می‌شوند.</p>
                    </div>
                </div>

                <h2 class="mys-subheading">تاریخچه‌ی پشتیبان‌ها</h2>
                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th scope="col">تاریخ</th>
                                <th scope="col">نوع</th>
                                <th scope="col">حجم</th>
                                <th scope="col">مدت</th>
                                <th scope="col">وضعیت</th>
                                <th scope="col">فایل</th>
                                <th scope="col"><span class="sr-only">عملیات</span></th>
                            </tr>
                        </thead>
                        <tbody id="backupHistoryBody"></tbody>
                    </table>
                    <p class="table-state" id="backupHistoryLoading">در حال بارگذاری…</p>
                    <p class="table-state is-hidden" id="backupHistoryEmpty">هنوز هیچ پشتیبانی ایجاد نشده است.</p>
                </div>
                <nav class="pagination" id="backupHistoryPagination" aria-label="صفحه‌بندی پشتیبان‌ها"></nav>
            </section>

            <!-- ================= Restore ================= -->
            <section class="mys-view" data-view="restore" aria-label="بازیابی">
                <div class="page-title">
                    <h1>بازیابی</h1>
                    <button type="button" class="btn btn-ghost btn-sm" data-refresh="restore">
                        <svg class="icon" aria-hidden="true"><use href="#refresh"></use></svg>
                        <span>بروزرسانی</span>
                    </button>
                </div>
                <p class="alert alert-error" role="alert">
                    بازیابی داده‌های فعلی دیتابیس را با محتوای پشتیبان جایگزین می‌کند و ممکن است غیرقابل بازگشت باشد.
                    این عملیات را فقط زمانی انجام دهید که از عواقب آن مطمئن هستید.
                </p>
                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th scope="col">تاریخ</th>
                                <th scope="col">نوع</th>
                                <th scope="col">حجم</th>
                                <th scope="col">وضعیت</th>
                                <th scope="col"><span class="sr-only">عملیات</span></th>
                            </tr>
                        </thead>
                        <tbody id="restoreListBody"></tbody>
                    </table>
                    <p class="table-state" id="restoreListLoading">در حال بارگذاری…</p>
                    <p class="table-state is-hidden" id="restoreListEmpty">پشتیبان سالمی برای بازیابی موجود نیست.</p>
                </div>
            </section>

            <!-- ================= Cleanup ================= -->
            <section class="mys-view" data-view="cleanup" aria-label="پاکسازی">
                <div class="page-title">
                    <h1>پاکسازی</h1>
                    <button type="button" class="btn btn-ghost btn-sm" data-refresh="cleanup">
                        <svg class="icon" aria-hidden="true"><use href="#refresh"></use></svg>
                        <span>بروزرسانی</span>
                    </button>
                </div>
                <p class="muted">پاکسازی فقط بر اساس Ruleهای صریح زیر انجام می‌شود؛ بدون Rule فعال، هیچ داده‌ای حذف نخواهد شد.</p>

                <div class="mys-card">
                    <h2 class="mys-subheading mys-subheading-flush">Rule جدید</h2>
                    <div class="field-row">
                        <label class="field">
                            <span class="field-label">جدول</span>
                            <select id="cleanupRuleTable"></select>
                        </label>
                        <label class="field">
                            <span class="field-label">ستون تاریخ</span>
                            <select id="cleanupRuleColumn"></select>
                        </label>
                    </div>
                    <div class="field-row">
                        <label class="field">
                            <span class="field-label">حذف رکوردهای قدیمی‌تر از (روز)</span>
                            <input type="number" id="cleanupRuleRetention" min="1" value="90">
                        </label>
                        <label class="field-check mys-align-center">
                            <input type="checkbox" id="cleanupRuleEnabled" checked> فعال
                        </label>
                    </div>
                    <button type="button" class="btn btn-primary" id="saveCleanupRuleBtn">ذخیره‌ی Rule</button>
                </div>

                <h2 class="mys-subheading">Ruleهای پاکسازی</h2>
                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th scope="col">جدول</th>
                                <th scope="col">ستون تاریخ</th>
                                <th scope="col">نگهداری (روز)</th>
                                <th scope="col">فعال</th>
                                <th scope="col">آخرین اجرا</th>
                                <th scope="col">آخرین تعداد حذف</th>
                                <th scope="col"><span class="sr-only">عملیات</span></th>
                            </tr>
                        </thead>
                        <tbody id="cleanupRulesBody"></tbody>
                    </table>
                    <p class="table-state is-hidden" id="cleanupRulesEmpty">هنوز هیچ Ruleای تعریف نشده است.</p>
                </div>
            </section>

            <!-- ================= Monitoring ================= -->
            <section class="mys-view" data-view="monitoring" aria-label="مانیتورینگ">
                <div class="page-title">
                    <h1>مانیتورینگ</h1>
                    <button type="button" class="btn btn-ghost btn-sm" data-refresh="monitoring">
                        <svg class="icon" aria-hidden="true"><use href="#refresh"></use></svg>
                        <span>بروزرسانی</span>
                    </button>
                </div>

                <section class="stats" id="monitoringStats" aria-label="آمار زنده"></section>

                <div class="toolbar">
                    <span class="muted" id="processListCount"></span>
                    <div class="toolbar-actions">
                        <span class="muted">بروزرسانی خودکار هر ۱۰ ثانیه</span>
                    </div>
                </div>

                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th scope="col">PID</th>
                                <th scope="col">کاربر</th>
                                <th scope="col">Host</th>
                                <th scope="col">دیتابیس</th>
                                <th scope="col">Command</th>
                                <th scope="col">مدت (ثانیه)</th>
                                <th scope="col">State</th>
                                <th scope="col">Query</th>
                                <th scope="col"><span class="sr-only">عملیات</span></th>
                            </tr>
                        </thead>
                        <tbody id="processListBody"></tbody>
                    </table>
                    <p class="table-state" id="processListLoading">در حال بارگذاری…</p>
                    <p class="table-state is-hidden" id="processListEmpty">Queryی در حال اجرا نیست.</p>
                    <p class="table-state is-hidden" id="processListNoAccess">دسترسی به PROCESSLIST موجود نیست (GRANT PROCESS لازم است).</p>
                </div>

                <h2 class="mys-subheading">Queryهای کند (Performance Schema)</h2>
                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th scope="col">Query</th>
                                <th scope="col">تعداد اجرا</th>
                                <th scope="col">میانگین (ms)</th>
                                <th scope="col">حداکثر (ms)</th>
                                <th scope="col">ردیف بررسی‌شده</th>
                                <th scope="col">ردیف ارسالی</th>
                            </tr>
                        </thead>
                        <tbody id="slowQueriesBody"></tbody>
                    </table>
                    <p class="table-state is-hidden" id="slowQueriesNoAccess">دسترسی به Performance Schema موجود نیست.</p>
                </div>
            </section>

            <!-- ================= Scheduler ================= -->
            <section class="mys-view" data-view="scheduler" aria-label="زمان‌بندی">
                <div class="page-title">
                    <h1>زمان‌بندی</h1>
                    <button type="button" class="btn btn-ghost btn-sm" data-refresh="scheduler">
                        <svg class="icon" aria-hidden="true"><use href="#refresh"></use></svg>
                        <span>بروزرسانی</span>
                    </button>
                </div>

                <div class="mys-card">
                    <h2 class="mys-subheading mys-subheading-flush">وضعیت فعلی نگهداری خودکار</h2>
                    <dl class="detail-list" id="schedulerSettingsMeta"></dl>
                    <p class="muted mys-mt-3">
                        برای اجرای واقعی و دوره‌ای، دستور زیر را در Cron سرور ثبت کنید (هر ۱۵ دقیقه کافی است):
                    </p>
                    <p class="mys-code-block">*/15 * * * * php /path/to/PHP/cron/mysql_manager_cron.php</p>
                    <button type="button" class="btn btn-primary mys-mt-3" id="runMaintenanceNowBtn">
                        <svg class="icon" aria-hidden="true"><use href="#play"></use></svg>
                        <span>اجرای اکنون (تست دستی)</span>
                    </button>
                    <p class="muted mys-mt-2">این دکمه دقیقاً همان منطقی را اجرا می‌کند که Cron اجرا می‌کند — برای تست بدون نیاز به دسترسی سرور.</p>
                </div>

                <h2 class="mys-subheading">نتیجه‌ی آخرین اجرا</h2>
                <dl class="detail-list" id="schedulerLastRunMeta"></dl>
                <p class="muted is-hidden" id="schedulerLastRunEmpty">هنوز اجرایی از این صفحه انجام نشده است.</p>
            </section>

            <!-- ================= Logs ================= -->
            <section class="mys-view" data-view="logs" aria-label="گزارش‌ها">
                <div class="page-title">
                    <h1>گزارش‌ها</h1>
                    <button type="button" class="btn btn-ghost btn-sm" data-refresh="logs">
                        <svg class="icon" aria-hidden="true"><use href="#refresh"></use></svg>
                        <span>بروزرسانی</span>
                    </button>
                </div>

                <div class="toolbar">
                    <div class="toolbar-filters" role="group" aria-label="فیلتر نوع رویداد">
                        <button type="button" class="chip is-selected" data-entity-type="">همه</button>
                        <button type="button" class="chip" data-entity-type="mysql_table">بهینه‌سازی</button>
                        <button type="button" class="chip" data-entity-type="mysql_backup">پشتیبان/بازیابی</button>
                        <button type="button" class="chip" data-entity-type="mysql_cleanup_rule">پاکسازی</button>
                        <button type="button" class="chip" data-entity-type="mysql_maintenance_settings">تنظیمات</button>
                        <button type="button" class="chip" data-entity-type="mysql_process">Kill Query</button>
                    </div>
                </div>

                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th scope="col">زمان</th>
                                <th scope="col">کاربر</th>
                                <th scope="col">عملیات</th>
                                <th scope="col">نوع</th>
                                <th scope="col">شناسه</th>
                                <th scope="col">جزئیات</th>
                            </tr>
                        </thead>
                        <tbody id="logsBody"></tbody>
                    </table>
                    <p class="table-state" id="logsLoading">در حال بارگذاری…</p>
                    <p class="table-state is-hidden" id="logsEmpty">گزارشی برای نمایش وجود ندارد.</p>
                </div>
                <div class="mys-load-more">
                    <button type="button" class="btn btn-ghost btn-sm is-hidden" id="logsLoadMoreBtn">بارگذاری موارد بیشتر</button>
                </div>
            </section>

        </main>
    </div>

    <!-- دیالوگ جزئیات جدول -->
    <dialog class="dialog" id="tableDetailDialog">
        <div class="dialog-body">
            <header class="dialog-header">
                <h2 id="tableDetailTitle">جزئیات جدول</h2>
                <button type="button" class="btn-icon" data-close aria-label="بستن">
                    <svg class="icon" aria-hidden="true"><use href="#close"></use></svg>
                </button>
            </header>
            <div class="dialog-content">
                <dl class="detail-list" id="tableDetailMeta"></dl>
                <h3 class="mys-subheading">ستون‌ها</h3>
                <div class="table-wrap">
                    <table class="data-table">
                        <thead><tr><th scope="col">نام</th><th scope="col">نوع</th><th scope="col">Null</th><th scope="col">کلید</th><th scope="col">پیش‌فرض</th><th scope="col">Extra</th></tr></thead>
                        <tbody id="tableDetailColumns"></tbody>
                    </table>
                </div>
                <h3 class="mys-subheading">ایندکس‌ها</h3>
                <div class="table-wrap">
                    <table class="data-table">
                        <thead><tr><th scope="col">نام</th><th scope="col">نوع</th><th scope="col">ستون</th><th scope="col">یکتا</th><th scope="col">Cardinality</th></tr></thead>
                        <tbody id="tableDetailIndexes"></tbody>
                    </table>
                </div>
            </div>
            <footer class="dialog-footer">
                <button type="button" class="btn btn-ghost" data-close>بستن</button>
            </footer>
        </div>
    </dialog>

    <!-- دیالوگ تأیید عملیات بهینه‌سازی (Optimize/Repair تکی و عملیات گروهی) -->
    <dialog class="dialog dialog-sm" id="optimizeConfirmDialog">
        <div class="dialog-body">
            <header class="dialog-header">
                <h2 id="optimizeConfirmTitle">تأیید عملیات</h2>
                <button type="button" class="btn-icon" data-close aria-label="بستن">
                    <svg class="icon" aria-hidden="true"><use href="#close"></use></svg>
                </button>
            </header>
            <div class="dialog-content">
                <p class="alert alert-error" id="optimizeConfirmWarning" role="alert"></p>
                <dl class="detail-list" id="optimizeConfirmMeta"></dl>
            </div>
            <footer class="dialog-footer">
                <button type="button" class="btn btn-ghost" data-close>انصراف</button>
                <button type="button" class="btn btn-primary" id="optimizeConfirmOk">ادامه</button>
            </footer>
        </div>
    </dialog>

    <!-- دیالوگ نمایش نتیجه‌ی عملیات تکی (وضعیت/زمان/حجم قبل و بعد/فضای آزادشده) -->
    <dialog class="dialog dialog-sm" id="optimizeResultDialog">
        <div class="dialog-body">
            <header class="dialog-header">
                <h2>نتیجه‌ی عملیات</h2>
                <button type="button" class="btn-icon" data-close aria-label="بستن">
                    <svg class="icon" aria-hidden="true"><use href="#close"></use></svg>
                </button>
            </header>
            <div class="dialog-content">
                <dl class="detail-list" id="optimizeResultMeta"></dl>
            </div>
            <footer class="dialog-footer">
                <button type="button" class="btn btn-primary" data-close>باشه</button>
            </footer>
        </div>
    </dialog>

    <!-- دیالوگ نتیجه‌ی پشتیبان‌گیری -->
    <dialog class="dialog dialog-sm" id="backupResultDialog">
        <div class="dialog-body">
            <header class="dialog-header">
                <h2>نتیجه‌ی پشتیبان‌گیری</h2>
                <button type="button" class="btn-icon" data-close aria-label="بستن">
                    <svg class="icon" aria-hidden="true"><use href="#close"></use></svg>
                </button>
            </header>
            <div class="dialog-content">
                <dl class="detail-list" id="backupResultMeta"></dl>
            </div>
            <footer class="dialog-footer">
                <button type="button" class="btn btn-primary" data-close>باشه</button>
            </footer>
        </div>
    </dialog>

    <!-- دیالوگ تأیید بازیابی — تأیید دومرحله‌ای واقعی با تایپ نام دیتابیس (بند ۱۶ spec) -->
    <dialog class="dialog dialog-sm" id="restoreConfirmDialog">
        <div class="dialog-body">
            <header class="dialog-header">
                <h2>بازیابی دیتابیس</h2>
                <button type="button" class="btn-icon" data-close aria-label="بستن">
                    <svg class="icon" aria-hidden="true"><use href="#close"></use></svg>
                </button>
            </header>
            <div class="dialog-content">
                <p class="alert alert-error" role="alert">این عملیات ممکن است داده‌های فعلی را بازنویسی کند.</p>
                <dl class="detail-list" id="restoreConfirmMeta"></dl>
                <label class="field mys-mt-4">
                    <span class="field-label">برای تأیید، نام دیتابیس را دقیقاً تایپ کنید</span>
                    <input type="text" id="restoreConfirmInput" autocomplete="off" spellcheck="false">
                </label>
            </div>
            <footer class="dialog-footer">
                <button type="button" class="btn btn-ghost" data-close>انصراف</button>
                <button type="button" class="btn btn-danger" id="restoreConfirmOk" disabled>بازیابی</button>
            </footer>
        </div>
    </dialog>

    <!-- دیالوگ نتیجه‌ی بازیابی -->
    <dialog class="dialog dialog-sm" id="restoreResultDialog">
        <div class="dialog-body">
            <header class="dialog-header">
                <h2>نتیجه‌ی بازیابی</h2>
                <button type="button" class="btn-icon" data-close aria-label="بستن">
                    <svg class="icon" aria-hidden="true"><use href="#close"></use></svg>
                </button>
            </header>
            <div class="dialog-content">
                <dl class="detail-list" id="restoreResultMeta"></dl>
            </div>
            <footer class="dialog-footer">
                <button type="button" class="btn btn-primary" data-close>باشه</button>
            </footer>
        </div>
    </dialog>

    <!-- دیالوگ پیش‌نمایش پاکسازی (Dry Run) -->
    <dialog class="dialog dialog-sm" id="cleanupPreviewDialog">
        <div class="dialog-body">
            <header class="dialog-header">
                <h2>پیش‌نمایش پاکسازی</h2>
                <button type="button" class="btn-icon" data-close aria-label="بستن">
                    <svg class="icon" aria-hidden="true"><use href="#close"></use></svg>
                </button>
            </header>
            <div class="dialog-content">
                <p class="alert alert-error" role="alert">این عملیات رکوردهای مطابق را برای همیشه حذف می‌کند.</p>
                <dl class="detail-list" id="cleanupPreviewMeta"></dl>
            </div>
            <footer class="dialog-footer">
                <button type="button" class="btn btn-ghost" data-close>انصراف</button>
                <button type="button" class="btn btn-danger" id="cleanupPreviewOk">حذف قطعی</button>
            </footer>
        </div>
    </dialog>

    <div class="toasts" id="toasts" aria-live="polite" aria-atomic="false"></div>

    <script src="assets/app.js" defer></script>
</body>

</html>
