<?php
// PHP/Realtime Dashboard/index.php داشبورد گزارش لحظه‌ای بارگیری — رندر سمت سرور، پشت گیت نشست مشترک با Quota_Reports.

declare(strict_types=1);

require_once __DIR__ . '/_guard.php';

rtd_require_auth_page();
rtd_send_page_headers();

$theme = rtd_theme();
?>
<!DOCTYPE html>
<html lang="fa" dir="rtl"<?= $theme !== '' ? ' data-theme="' . e($theme) . '"' : '' ?>>

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>داشبورد لحظه‌ای بارگیری — ATK Cargo</title>
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
            <span>داشبورد لحظه‌ای بارگیری</span>
        </div>

        <div class="topbar-actions">
            <span class="badge" id="connectionStatus">
                <span>زنده</span>
            </span>
            <button type="button" class="btn-icon" id="settingsBtn" aria-label="تنظیمات">
                <svg class="icon" aria-hidden="true"><use href="#settings"></use></svg>
            </button>
            <button type="button" class="btn-icon" id="themeToggle" aria-label="تغییر پوسته">
                <svg class="icon icon-light-only" aria-hidden="true"><use href="#moon"></use></svg>
                <svg class="icon icon-dark-only" aria-hidden="true"><use href="#sun"></use></svg>
            </button>
            <form method="post" action="../Quota_Reports/logout.php" class="inline-form">
                <input type="hidden" name="csrf_token" value="<?= e(\App\Core\Csrf::token()) ?>">
                <input type="hidden" name="return" value="<?= e(RTD_RETURN_URL) ?>">
                <button type="submit" class="btn btn-ghost">
                    <svg class="icon" aria-hidden="true"><use href="#logout"></use></svg>
                    <span>خروج</span>
                </button>
            </form>
        </div>
    </header>

    <main class="container">

        <div class="page-title">
            <h1>کوتاژهای فعال</h1>
            <span class="muted" id="dashboardCount"></span>
            <span class="badge badge-active" id="shiftBadge">—</span>
        </div>

        <section class="toolbar">
            <form class="kotazh-search" id="searchForm">
                <svg class="icon" aria-hidden="true"><use href="#search"></use></svg>
                <input type="text" id="searchInput"
                    placeholder="جستجو در کوتاژ، کشتی، انبار، باربری، کالا…"
                    aria-label="جستجوی کوتاژها" autocomplete="off">
            </form>

            <div class="filter-row">
                <select id="shipFilter" aria-label="فیلتر کشتی"><option value="">همه‌ی کشتی‌ها</option></select>
                <select id="carrierFilter" aria-label="فیلتر باربری"><option value="">همه‌ی باربری‌ها</option></select>
                <select id="warehouseFilter" aria-label="فیلتر انبار"><option value="">همه‌ی انبارها</option></select>
            </div>

            <div class="toolbar-actions">
                <div class="refresh-ring" id="refreshRing" title="زمان تا بازخوانی خودکار">
                    <svg viewBox="0 0 36 36" class="refresh-ring-svg" aria-hidden="true">
                        <circle class="refresh-ring-track" cx="18" cy="18" r="15.5"></circle>
                        <circle class="refresh-ring-progress" id="refreshRingProgress" cx="18" cy="18" r="15.5"></circle>
                    </svg>
                    <span class="refresh-ring-label" id="refreshRingLabel">—</span>
                </div>
                <button type="button" class="btn btn-ghost" id="refreshBtn">
                    <svg class="icon" aria-hidden="true"><use href="#refresh"></use></svg>
                    <span>بازخوانی</span>
                </button>
            </div>
        </section>

        <section class="stats" aria-label="خلاصه‌ی آماری">
            <article class="stat">
                <p class="stat-label">تعداد کوتاژ</p>
                <p class="stat-value" id="statQuotaCount">—</p>
            </article>
            <article class="stat">
                <p class="stat-label">حواله ورودی</p>
                <p class="stat-value" id="statEntry">—</p>
            </article>
            <article class="stat">
                <p class="stat-label">حواله خروجی</p>
                <p class="stat-value" id="statExit">—</p>
            </article>
            <article class="stat">
                <p class="stat-label">جمع وزن خالص (تن)</p>
                <p class="stat-value" id="statWeight">—</p>
            </article>
            <article class="stat">
                <p class="stat-label">باربری‌های فعال</p>
                <p class="stat-value" id="statCarriers">—</p>
            </article>
        </section>

        <section class="table-wrap">
            <table class="data-table">
                <thead>
                    <tr id="tableHeaderRow">
                        <th scope="col" data-sort="loadingQuotaNumber">کوتاژ</th>
                        <th scope="col" data-sort="shipName">کشتی</th>
                        <th scope="col" data-sort="loadingWarehouse">انبار</th>
                        <th scope="col" data-sort="shippingCompany">باربری</th>
                        <th scope="col" data-sort="cargoType">کالا</th>
                        <th scope="col" data-sort="cargoOwner">مالک بار</th>
                        <th scope="col" data-sort="entryVouchers">ورودی</th>
                        <th scope="col" data-sort="exitVouchers">خروجی</th>
                        <th scope="col" data-sort="totalNetWeight">وزن خالص (کیلوگرم)</th>
                    </tr>
                </thead>
                <tbody id="dataBody"></tbody>
            </table>
            <p class="table-state" id="tableLoading">در حال بارگذاری…</p>
            <p class="table-state is-hidden" id="tableEmpty">در شیفت جاری کوتاژ فعالی وجود ندارد.</p>
        </section>

        <div class="pagination" id="pagination">
            <button type="button" class="btn btn-ghost" id="prevPageBtn">قبلی</button>
            <span class="muted" id="pageInfo"></span>
            <button type="button" class="btn btn-ghost" id="nextPageBtn">بعدی</button>
        </div>
    </main>

    <dialog id="settingsDialog" class="dialog dialog-sm">
        <form method="dialog" class="dialog-body">
            <header class="dialog-header">
                <h2>تنظیمات داشبورد</h2>
                <button type="button" class="btn-icon" data-close aria-label="بستن">
                    <svg class="icon" aria-hidden="true"><use href="#close"></use></svg>
                </button>
            </header>
            <div class="dialog-content">
                <label class="field">
                    <span class="field-label">بازه‌ی بازخوانی خودکار</span>
                    <select id="refreshIntervalInput">
                        <option value="10000">۱۰ ثانیه</option>
                        <option value="30000" selected>۳۰ ثانیه</option>
                        <option value="60000">۱ دقیقه</option>
                        <option value="300000">۵ دقیقه</option>
                    </select>
                </label>
                <label class="field">
                    <span class="field-label">تعداد ردیف در هر صفحه</span>
                    <select id="pageSizeInput">
                        <option value="10">۱۰</option>
                        <option value="25" selected>۲۵</option>
                        <option value="50">۵۰</option>
                        <option value="100">۱۰۰</option>
                    </select>
                </label>
                <label class="field field-check">
                    <input type="checkbox" id="notifyToastInput" checked>
                    <span>نمایش اعلان هنگام ورود کوتاژ جدید</span>
                </label>
                <label class="field field-check">
                    <input type="checkbox" id="notifySoundInput">
                    <span>پخش صدا هنگام اعلان</span>
                </label>
                <label class="field field-check">
                    <input type="checkbox" id="notifyBrowserInput">
                    <span>اعلان مرورگر (خارج از صفحه)</span>
                </label>
            </div>
            <footer class="dialog-footer">
                <button type="button" class="btn btn-primary" data-close>بستن</button>
            </footer>
        </form>
    </dialog>

    <div class="toasts" id="toasts" aria-live="polite" aria-atomic="false"></div>

    <script src="assets/app.js" defer></script>
</body>

</html>
