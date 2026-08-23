<?php
// PHP/Quota Reports/index.php داشبورد گزارش آماری کوتاژ — رندر سمت سرور، پشت گیت نشست.

declare(strict_types=1);

require_once __DIR__ . '/_guard.php';

use App\Core\Csrf;

qr_require_auth_page();
qr_send_page_headers();

$csrfToken = Csrf::token();
$theme = qr_theme();
?>
<!DOCTYPE html>
<html lang="fa" dir="rtl"<?= $theme !== '' ? ' data-theme="' . e($theme) . '"' : '' ?>>

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="csrf-token" content="<?= e($csrfToken) ?>">
    <title>گزارش آماری کوتاژ — ATK Cargo</title>
    <link rel="icon" type="image/x-icon" href="assets/img/fav.ico">
    <link rel="preload" href="../assets/ui/fonts/Vazirmatn-Regular.woff2" as="font" type="font/woff2" crossorigin>
    <link rel="stylesheet" href="../assets/ui/core.css?v=<?= filemtime(__DIR__ . '/../assets/ui/core.css') ?>">
    <link rel="stylesheet" href="assets/app.css?v=<?= filemtime(__DIR__ . '/assets/app.css') ?>">
</head>

<body>
<?php require __DIR__ . '/_icons.php'; ?>

    <header class="topbar">
        <div class="topbar-brand">
            <svg class="icon" aria-hidden="true"><use href="#chart"></use></svg>
            <span>گزارش آماری کوتاژ</span>
        </div>

        <div class="topbar-actions">
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

    <main class="container">

        <!-- نمای داشبورد: فهرست همه‌ی کوتاژهای فعال -->
        <section class="view" id="dashboardView">
            <div class="page-title">
                <h1>کوتاژها</h1>
                <span class="muted" id="dashboardCount"></span>
            </div>

            <section class="toolbar">
                <form class="kotazh-search" id="kotazhSearchForm">
                    <svg class="icon" aria-hidden="true"><use href="#search"></use></svg>
                    <input type="text" id="kotazhInput"
                        placeholder="جستجو در همه‌ی داده‌ها: شماره کوتاژ، کشتی، انبار، شرکت حمل، کالا…"
                        aria-label="جستجوی کوتاژها" autocomplete="off">
                </form>

                <div class="toolbar-actions">
                    <button type="button" class="btn btn-ghost" id="advancedSearchBtn">
                        <svg class="icon" aria-hidden="true"><use href="#scale"></use></svg>
                        <span>جستجو با قبض باسکول</span>
                    </button>
                    <button type="button" class="btn btn-ghost" id="dashboardRefreshBtn">
                        <svg class="icon" aria-hidden="true"><use href="#refresh"></use></svg>
                        <span>بازخوانی</span>
                    </button>
                    <a class="btn btn-ghost" id="exportSummaryExcel" href="export.php?format=excel&amp;scope=summary">
                        <svg class="icon" aria-hidden="true"><use href="#file-excel"></use></svg>
                        <span>خروجی کامل داشبورد</span>
                    </a>
                </div>
            </section>

            <section class="table-wrap">
                <table class="data-table">
                    <thead>
                        <tr id="quotasHeaderRow">
                            <th scope="col" data-sort="number">کوتاژ</th>
                            <th scope="col" data-sort="warehouse">انبار</th>
                            <th scope="col" data-sort="shippingCompany">شرکت حمل</th>
                            <th scope="col" data-sort="cargoType">کالا</th>
                            <th scope="col" data-sort="totalTonnage">تناژ کل (تن)</th>
                            <th scope="col" data-sort="remainingTonnage">باقی‌مانده (تن)</th>
                            <th scope="col" data-sort="percentageLoaded">درصد بارگیری</th>
                            <th scope="col" data-sort="exitVoucherCount">حواله خروج</th>
                            <th scope="col" data-sort="isActive">وضعیت</th>
                        </tr>
                    </thead>
                    <tbody id="quotasBody"></tbody>
                </table>
                <p class="table-state" id="dashboardLoading">در حال بارگذاری…</p>
                <p class="table-state is-hidden" id="dashboardEmpty">کوتاژی برای نمایش وجود ندارد.</p>
            </section>
        </section>

        <!-- نمای جزئیات: یک کوتاژ مشخص -->
        <section class="view is-hidden" id="detailView">
            <button type="button" class="btn btn-ghost back-link" id="backToDashboard">
                <svg class="icon" aria-hidden="true"><use href="#arrow-back"></use></svg>
                <span>بازگشت به داشبورد</span>
            </button>

            <div class="page-title">
                <h1 id="detailTitle">کوتاژ</h1>
                <span class="muted" id="detailSubtitle"></span>
            </div>

            <section class="stats" id="detailStats" aria-label="خلاصه کوتاژ">
                <article class="stat">
                    <p class="stat-label">تناژ کل</p>
                    <p class="stat-value" id="statTotal">—</p>
                </article>
                <article class="stat">
                    <p class="stat-label">بارگیری‌شده</p>
                    <p class="stat-value" id="statLoaded">—</p>
                </article>
                <article class="stat">
                    <p class="stat-label">باقی‌مانده</p>
                    <p class="stat-value" id="statRemaining">—</p>
                </article>
                <article class="stat">
                    <p class="stat-label">درصد بارگیری</p>
                    <p class="stat-value" id="statPercentage">—</p>
                    <progress class="progress-fill" id="statPercentageBar" value="0" max="100"></progress>
                </article>
                <article class="stat">
                    <p class="stat-label">تعداد حواله خروج</p>
                    <p class="stat-value" id="statVouchers">—</p>
                </article>
                <article class="stat">
                    <p class="stat-label">مالک بار</p>
                    <p class="stat-value stat-value-text" id="statOwner">—</p>
                </article>
                <article class="stat">
                    <p class="stat-label">وضعیت کوتاژ</p>
                    <p class="stat-value stat-value-text" id="statStatus">—</p>
                </article>
            </section>

            <section class="chart-card">
                <h2>روند خروج روزانه</h2>
                <div id="exitChart" class="chart-svg-wrap"></div>
            </section>

            <section class="toolbar">
                <div class="toolbar-filters" role="group" aria-label="فیلتر وضعیت حواله">
                    <button type="button" class="chip is-selected" data-filter="all">همه</button>
                    <button type="button" class="chip" data-filter="enter">در انبار</button>
                    <button type="button" class="chip" data-filter="exit">خارج شده</button>
                </div>

                <div class="toolbar-search">
                    <svg class="icon" aria-hidden="true"><use href="#search"></use></svg>
                    <input type="search" id="voucherSearch" placeholder="جستجوی شماره حواله یا قبض باسکول…"
                        aria-label="جستجوی حواله" autocomplete="off">
                </div>

                <div class="toolbar-actions">
                    <button type="button" class="btn btn-ghost" id="detailRefreshBtn">
                        <svg class="icon" aria-hidden="true"><use href="#refresh"></use></svg>
                        <span>بازخوانی</span>
                    </button>
                    <div class="export-actions" role="group" aria-label="خروجی گزارش">
                        <a class="btn-icon" id="exportExcel" title="خروجی Excel" href="#">
                            <svg class="icon" aria-hidden="true"><use href="#file-excel"></use></svg>
                        </a>
                        <a class="btn-icon" id="exportHtml" title="خروجی HTML" href="#">
                            <svg class="icon" aria-hidden="true"><use href="#file-html"></use></svg>
                        </a>
                    </div>
                </div>
            </section>

            <div class="date-filter-row">
                <label class="field field-inline">
                    <span class="field-label">از تاریخ</span>
                    <select id="startDateFilter"><option value="">—</option></select>
                </label>
                <label class="field field-inline">
                    <span class="field-label">تا تاریخ</span>
                    <select id="endDateFilter"><option value="">—</option></select>
                </label>
                <button type="button" class="btn btn-ghost" id="applyDateFilter">اعمال فیلتر تاریخ</button>
                <button type="button" class="btn btn-ghost" id="clearDateFilter">پاک‌کردن</button>
            </div>

            <section class="table-wrap">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th scope="col">شماره حواله</th>
                            <th scope="col">ساعت ورود</th>
                            <th scope="col">وزن خالص</th>
                            <th scope="col">قبض باسکول</th>
                            <th scope="col">کسری بار</th>
                            <th scope="col">اضافه بار</th>
                            <th scope="col">ساعت خروج</th>
                            <th scope="col">تاریخ خروج</th>
                            <th scope="col">وضعیت</th>
                        </tr>
                    </thead>
                    <tbody id="cargoBody"></tbody>
                </table>
                <p class="table-state" id="detailLoading">در حال بارگذاری…</p>
                <p class="table-state is-hidden" id="detailEmpty">حواله‌ای برای نمایش وجود ندارد.</p>
            </section>
        </section>
    </main>

    <!-- جستجوی پیشرفته با شماره قبض باسکول -->
    <dialog id="advancedSearchDialog" class="dialog dialog-sm">
        <form method="dialog" id="advancedSearchForm" class="dialog-body">
            <header class="dialog-header">
                <h2>جستجو با شماره قبض باسکول</h2>
                <button type="button" class="btn-icon" data-close aria-label="بستن">
                    <svg class="icon" aria-hidden="true"><use href="#close"></use></svg>
                </button>
            </header>
            <div class="dialog-content">
                <p class="alert alert-error is-hidden" id="advancedSearchError" role="alert"></p>
                <label class="field">
                    <span class="field-label">شماره قبض باسکول</span>
                    <input type="text" id="receiptInput" required autocomplete="off" autofocus>
                </label>
            </div>
            <footer class="dialog-footer">
                <button type="button" class="btn btn-ghost" data-close>انصراف</button>
                <button type="submit" class="btn btn-primary" id="advancedSearchSubmit">جستجو</button>
            </footer>
        </form>
    </dialog>

    <div class="toasts" id="toasts" aria-live="polite" aria-atomic="false"></div>

    <script src="assets/app.js" defer></script>
</body>

</html>
