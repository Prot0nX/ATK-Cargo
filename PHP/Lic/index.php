<?php
// PHP/Lic/index.php
//
// داشبورد پنل مدیریت لایسنس — رندر سمت سرور، پشت گیت نشست.
//
// برخلاف license-manager.html قبلی (فایل استاتیک عمومی که هر کسی می‌توانست
// بگیرد و «محافظت»‌اش فقط یک بررسی localStorage در جاوااسکریپت بود)، کاربر
// احراز نشده اینجا حتی یک بایت از بدنه‌ی صفحه را دریافت نمی‌کند.

declare(strict_types=1);

require_once __DIR__ . '/_guard.php';

use App\Core\Csrf;
use App\Services\LicenseAdminService;

lic_require_auth_page();
lic_send_page_headers();

$csrfToken = Csrf::token();
$theme = lic_theme();
$plans = LicenseAdminService::PLANS;
?>
<!DOCTYPE html>
<html lang="fa" dir="rtl"<?= $theme !== '' ? ' data-theme="' . e($theme) . '"' : '' ?>>

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="csrf-token" content="<?= e($csrfToken) ?>">
    <title>مدیریت لایسنس — ATK Cargo</title>
    <link rel="icon" type="image/x-icon" href="assets/img/fav.ico">
    <link rel="stylesheet" href="assets/app.css">
</head>

<body>
<?php require __DIR__ . '/_icons.php'; ?>

    <header class="topbar">
        <div class="topbar-brand">
            <svg class="icon" aria-hidden="true"><use href="#key"></use></svg>
            <span>مدیریت لایسنس</span>
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
        <section class="stats" aria-label="آمار لایسنس‌ها">
            <article class="stat">
                <p class="stat-label">فعال</p>
                <p class="stat-value" id="statActive">—</p>
            </article>
            <article class="stat">
                <p class="stat-label">منقضی</p>
                <p class="stat-value" id="statExpired">—</p>
            </article>
            <article class="stat">
                <p class="stat-label">غیرفعال</p>
                <p class="stat-value" id="statInactive">—</p>
            </article>
            <article class="stat">
                <p class="stat-label">کل</p>
                <p class="stat-value" id="statTotal">—</p>
                <p class="stat-hint" id="statMonthly"></p>
            </article>
        </section>

        <section class="toolbar">
            <div class="toolbar-search">
                <svg class="icon" aria-hidden="true"><use href="#search"></use></svg>
                <input type="search" id="searchInput" placeholder="جستجوی شرکت، کلید یا رابط…"
                    aria-label="جستجو" autocomplete="off">
            </div>

            <div class="toolbar-filters" role="group" aria-label="فیلتر وضعیت">
                <button type="button" class="chip is-selected" data-status="">همه</button>
                <button type="button" class="chip" data-status="active">فعال</button>
                <button type="button" class="chip" data-status="expired">منقضی</button>
                <button type="button" class="chip" data-status="inactive">غیرفعال</button>
            </div>

            <div class="toolbar-actions">
                <button type="button" class="btn btn-ghost" id="refreshBtn">
                    <svg class="icon" aria-hidden="true"><use href="#refresh"></use></svg>
                    <span>بازخوانی</span>
                </button>
                <a class="btn btn-ghost" id="exportLink" href="export.php">
                    <svg class="icon" aria-hidden="true"><use href="#download"></use></svg>
                    <span>خروجی CSV</span>
                </a>
                <button type="button" class="btn btn-primary" id="newLicenseBtn">
                    <svg class="icon" aria-hidden="true"><use href="#plus"></use></svg>
                    <span>لایسنس جدید</span>
                </button>
            </div>
        </section>

        <section class="table-wrap">
            <table class="data-table">
                <thead>
                    <tr>
                        <th scope="col">کلید</th>
                        <th scope="col">شرکت</th>
                        <th scope="col">پلن</th>
                        <th scope="col">وضعیت</th>
                        <th scope="col">انقضا</th>
                        <th scope="col">آخرین بررسی</th>
                        <th scope="col"><span class="sr-only">عملیات</span></th>
                    </tr>
                </thead>
                <tbody id="licensesBody"></tbody>
            </table>

            <p class="table-state" id="loadingState">در حال بارگذاری…</p>
            <p class="table-state is-hidden" id="emptyState">لایسنسی برای نمایش وجود ندارد.</p>
        </section>
    </main>

    <!-- فرم ایجاد/ویرایش -->
    <dialog id="licenseDialog" class="dialog">
        <form method="dialog" id="licenseForm" class="dialog-body">
            <header class="dialog-header">
                <h2 id="dialogTitle">لایسنس جدید</h2>
                <button type="button" class="btn-icon" data-close aria-label="بستن">
                    <svg class="icon" aria-hidden="true"><use href="#close"></use></svg>
                </button>
            </header>

            <div class="dialog-content">
                <p class="alert alert-error is-hidden" id="formError" role="alert"></p>

                <label class="field">
                    <span class="field-label">نام شرکت <span class="required">*</span></span>
                    <input type="text" name="company_name" required maxlength="255" autocomplete="off">
                </label>

                <div class="field-row">
                    <label class="field">
                        <span class="field-label">پلن</span>
                        <select name="plan">
                            <?php foreach ($plans as $value => $label): ?>
                                <option value="<?= e($value) ?>"><?= e($label) ?></option>
                            <?php endforeach; ?>
                        </select>
                    </label>

                    <label class="field">
                        <span class="field-label">تاریخ انقضا</span>
                        <input type="datetime-local" name="expires_at">
                        <span class="field-hint">خالی = نامحدود</span>
                    </label>
                </div>

                <div class="field-row">
                    <label class="field">
                        <span class="field-label">نام رابط</span>
                        <input type="text" name="contact_name" maxlength="150" autocomplete="off">
                    </label>

                    <label class="field">
                        <span class="field-label">شماره تماس</span>
                        <input type="tel" name="contact_phone" maxlength="32" autocomplete="off">
                    </label>
                </div>

                <label class="field">
                    <span class="field-label">ایمیل</span>
                    <input type="email" name="contact_email" maxlength="190" autocomplete="off">
                </label>

                <label class="field">
                    <span class="field-label">یادداشت</span>
                    <textarea name="notes" rows="3" maxlength="2000"></textarea>
                </label>
            </div>

            <footer class="dialog-footer">
                <button type="button" class="btn btn-ghost" data-close>انصراف</button>
                <button type="submit" class="btn btn-primary" id="saveBtn">ذخیره</button>
            </footer>
        </form>
    </dialog>

    <!-- جزئیات لایسنس -->
    <dialog id="detailsDialog" class="dialog">
        <div class="dialog-body">
            <header class="dialog-header">
                <h2>جزئیات لایسنس</h2>
                <button type="button" class="btn-icon" data-close aria-label="بستن">
                    <svg class="icon" aria-hidden="true"><use href="#close"></use></svg>
                </button>
            </header>
            <div class="dialog-content" id="detailsContent"></div>
            <footer class="dialog-footer">
                <button type="button" class="btn btn-ghost" data-close>بستن</button>
            </footer>
        </div>
    </dialog>

    <!-- تأیید عملیات -->
    <dialog id="confirmDialog" class="dialog dialog-sm">
        <div class="dialog-body">
            <header class="dialog-header">
                <h2 id="confirmTitle">تأیید</h2>
            </header>
            <div class="dialog-content">
                <p id="confirmMessage"></p>
            </div>
            <footer class="dialog-footer">
                <button type="button" class="btn btn-ghost" data-close>انصراف</button>
                <button type="button" class="btn btn-danger" id="confirmOk">تأیید</button>
            </footer>
        </div>
    </dialog>

    <div class="toasts" id="toasts" aria-live="polite" aria-atomic="false"></div>

    <script src="assets/app.js"></script>
</body>

</html>
