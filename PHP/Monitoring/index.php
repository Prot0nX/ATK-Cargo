<?php
// PHP/Monitoring/index.php داشبورد مانیتورینگ — رندر سمت سرور، پشت گیت نشست.

declare(strict_types=1);

require_once __DIR__ . '/_guard.php';

use App\Core\Csrf;

mon_require_auth_page();
mon_send_page_headers();

$csrfToken = Csrf::token();
$theme = mon_theme();
?>
<!DOCTYPE html>
<html lang="fa" dir="rtl"<?= $theme !== '' ? ' data-theme="' . e($theme) . '"' : '' ?>>

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="csrf-token" content="<?= e($csrfToken) ?>">
    <title>مانیتورینگ — ATK Cargo</title>
    <link rel="icon" type="image/x-icon" href="assets/img/fav.ico">
    <link rel="stylesheet" href="assets/app.css">
</head>

<body>
<?php require __DIR__ . '/_icons.php'; ?>

    <header class="topbar">
        <div class="topbar-brand">
            <svg class="icon" aria-hidden="true"><use href="#activity"></use></svg>
            <span>مانیتورینگ</span>
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
        <section class="stats" aria-label="خلاصه‌ی وضعیت">
            <article class="stat stat-health">
                <p class="stat-label">وضعیت سیستم</p>
                <p class="stat-value" id="statHealth">—</p>
                <p class="stat-hint" id="statHealthHint"></p>
            </article>
            <article class="stat">
                <p class="stat-label">بحرانی (باز)</p>
                <p class="stat-value" id="statCritical">—</p>
            </article>
            <article class="stat">
                <p class="stat-label">هشدار (باز)</p>
                <p class="stat-value" id="statWarning">—</p>
            </article>
            <article class="stat">
                <p class="stat-label">اطلاعاتی (باز)</p>
                <p class="stat-value" id="statInfo">—</p>
            </article>
            <article class="stat">
                <p class="stat-label">کل باز</p>
                <p class="stat-value" id="statTotal">—</p>
            </article>
        </section>

        <section class="toolbar">
            <div class="toolbar-filters" role="group" aria-label="فیلتر وضعیت">
                <button type="button" class="chip is-selected" data-status="open">باز</button>
                <button type="button" class="chip" data-status="acknowledged">تأییدشده</button>
                <button type="button" class="chip" data-status="all">همه</button>
            </div>

            <div class="toolbar-actions">
                <span class="toolbar-hint" id="lastUpdated"></span>
                <button type="button" class="btn btn-ghost" id="refreshBtn">
                    <svg class="icon" aria-hidden="true"><use href="#refresh"></use></svg>
                    <span>بازخوانی</span>
                </button>
            </div>
        </section>

        <section class="table-wrap">
            <table class="data-table">
                <thead>
                    <tr>
                        <th scope="col">شدت</th>
                        <th scope="col">رویداد</th>
                        <th scope="col">پیام</th>
                        <th scope="col">منبع</th>
                        <th scope="col">زمان</th>
                        <th scope="col">وضعیت</th>
                        <th scope="col"><span class="sr-only">عملیات</span></th>
                    </tr>
                </thead>
                <tbody id="eventsBody"></tbody>
            </table>

            <p class="table-state" id="loadingState">در حال بارگذاری…</p>
            <p class="table-state is-hidden" id="emptyState">رویدادی برای نمایش وجود ندارد.</p>
        </section>
    </main>

 <!-- جزئیات رویداد -->
    <dialog id="detailsDialog" class="dialog">
        <div class="dialog-body">
            <header class="dialog-header">
                <h2>جزئیات رویداد</h2>
                <button type="button" class="btn-icon" data-close aria-label="بستن">
                    <svg class="icon" aria-hidden="true"><use href="#close"></use></svg>
                </button>
            </header>
            <div class="dialog-content" id="detailsContent"></div>
            <footer class="dialog-footer">
                <button type="button" class="btn btn-primary is-hidden" id="ackFromDetails">
                    <svg class="icon" aria-hidden="true"><use href="#check"></use></svg>
                    <span>تأیید رویداد</span>
                </button>
                <button type="button" class="btn btn-ghost" data-close>بستن</button>
            </footer>
        </div>
    </dialog>

    <div class="toasts" id="toasts" aria-live="polite" aria-atomic="false"></div>

    <script src="assets/app.js"></script>
</body>

</html>