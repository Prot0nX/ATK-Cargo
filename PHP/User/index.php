<?php
// PHP/User/index.php مدیریت کاربران آنلاین — رندر سمت سرور، پشت گیت نشست اختصاصی این پنل.

declare(strict_types=1);

require_once __DIR__ . '/_guard.php';

ou_require_auth_page();
ou_send_page_headers();

$theme = ou_theme();
?>
<!DOCTYPE html>
<html lang="fa" dir="rtl"<?= $theme !== '' ? ' data-theme="' . e($theme) . '"' : '' ?>>

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="csrf-token" content="<?= e(\App\Core\Csrf::token()) ?>">
    <title>مدیریت کاربران آنلاین — ATK Cargo</title>
    <link rel="icon" type="image/x-icon" href="assets/img/fav.ico">
    <link rel="preload" href="../assets/ui/fonts/Vazirmatn-Regular.woff2" as="font" type="font/woff2" crossorigin>
    <link rel="stylesheet" href="../assets/ui/core.css?v=<?= filemtime(__DIR__ . '/../assets/ui/core.css') ?>">
    <link rel="stylesheet" href="assets/app.css?v=<?= filemtime(__DIR__ . '/assets/app.css') ?>">
</head>

<body>
<?php require __DIR__ . '/_icons.php'; ?>

    <header class="topbar">
        <div class="topbar-brand">
            <svg class="icon" aria-hidden="true"><use href="#users"></use></svg>
            <span>مدیریت کاربران آنلاین</span>
        </div>

        <div class="topbar-actions">
            <button type="button" class="btn-icon" id="themeToggle" aria-label="تغییر پوسته">
                <svg class="icon icon-light-only" aria-hidden="true"><use href="#moon"></use></svg>
                <svg class="icon icon-dark-only" aria-hidden="true"><use href="#sun"></use></svg>
            </button>
            <button type="button" class="btn-icon" id="refreshBtn" aria-label="بروزرسانی">
                <svg class="icon" aria-hidden="true"><use href="#refresh"></use></svg>
            </button>
            <form method="post" action="logout.php" class="inline-form">
                <input type="hidden" name="csrf_token" value="<?= e(\App\Core\Csrf::token()) ?>">
                <button type="submit" class="btn btn-ghost">
                    <svg class="icon" aria-hidden="true"><use href="#logout"></use></svg>
                    <span>خروج</span>
                </button>
            </form>
        </div>
    </header>

    <main class="container">

        <div class="page-title">
            <h1>کاربران آنلاین</h1>
            <span class="muted" id="totalCount"></span>
        </div>

        <section class="stats" aria-label="خلاصه‌ی آماری">
            <article class="stat">
                <p class="stat-label">کاربران فعال</p>
                <p class="stat-value" id="activeSessionsCount">۰</p>
                <p class="stat-hint">آنلاین</p>
            </article>
            <article class="stat">
                <p class="stat-label">ورودی‌های امروز</p>
                <p class="stat-value" id="todayLoginsCount">۰</p>
                <p class="stat-hint">مجموع</p>
            </article>
            <article class="stat">
                <p class="stat-label">آخرین بروزرسانی</p>
                <p class="stat-value stat-value-text" id="lastUpdate" dir="ltr">—</p>
                <p class="stat-hint">دقیق</p>
            </article>
        </section>

        <section class="toolbar-card">
            <div class="toolbar toolbar-card-row">
                <div class="toolbar-search">
                    <svg class="icon" aria-hidden="true"><use href="#search"></use></svg>
                    <input type="text" id="searchInput" placeholder="جستجو در کاربران (نام، دستگاه، IP)…" aria-label="جستجوی کاربران" autocomplete="off">
                    <button type="button" class="btn-icon is-hidden" id="clearSearch" aria-label="پاک کردن جستجو">
                        <svg class="icon" aria-hidden="true"><use href="#close"></use></svg>
                    </button>
                </div>

                <label class="field-check autorefresh-check">
                    <input type="checkbox" id="autoRefresh">
                    <span class="muted">
                        بروزرسانی خودکار
                        <span id="refreshTimer" class="is-hidden refresh-timer">(۶۰)</span>
                    </span>
                </label>

                <div class="toolbar-actions">
                    <button type="button" class="btn btn-ghost" id="filtersToggle" aria-expanded="false">
                        <svg class="icon" aria-hidden="true"><use href="#filter"></use></svg>
                        <span>فیلترها</span>
                        <svg class="icon date-group-chevron" id="filtersToggleIcon" aria-hidden="true"><use href="#chevron-down"></use></svg>
                    </button>
                    <button type="button" class="btn-icon" id="exportCsvBtn" title="دانلود CSV" aria-label="دانلود CSV">
                        <svg class="icon" aria-hidden="true"><use href="#download"></use></svg>
                    </button>
                    <button type="button" class="btn-icon" id="advancedStatsBtn" title="آمار پیشرفته" aria-label="آمار پیشرفته">
                        <svg class="icon" aria-hidden="true"><use href="#chart-pie"></use></svg>
                    </button>
                    <div class="logout-menu">
                        <button type="button" class="btn-icon is-danger" id="logoutUsersBtn" title="مدیریت خروج" aria-label="مدیریت خروج">
                            <svg class="icon" aria-hidden="true"><use href="#sign-out"></use></svg>
                        </button>
                        <div class="logout-menu-panel is-hidden" id="logoutDropdownMenu">
                            <button type="button" class="menu-item is-danger logout-option" data-type="all">
                                <svg class="icon" aria-hidden="true"><use href="#users"></use></svg>
                                خروج همه کاربران
                            </button>
                            <button type="button" class="menu-item logout-option" data-type="except-admin">
                                <svg class="icon" aria-hidden="true"><use href="#user-shield"></use></svg>
                                همه به‌جز مدیران
                            </button>
                            <button type="button" class="menu-item logout-option" data-type="operators">
                                <svg class="icon" aria-hidden="true"><use href="#headset"></use></svg>
                                فقط اپراتورها
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <div class="filters-panel" id="filtersSection">
                <div class="filters-grid">
                    <div>
                        <h4 class="filter-group-label">
                            <svg class="icon" aria-hidden="true"><use href="#history"></use></svg>
                            بازه زمانی
                        </h4>
                        <div class="toolbar-filters">
                            <button type="button" class="chip is-selected filter-btn time-filter" data-filter="all">همه</button>
                            <button type="button" class="chip filter-btn time-filter" data-filter="today">امروز</button>
                            <button type="button" class="chip filter-btn time-filter" data-filter="24h">۲۴ ساعت</button>
                        </div>
                    </div>

                    <div>
                        <h4 class="filter-group-label">
                            <svg class="icon" aria-hidden="true"><use href="#sign-in"></use></svg>
                            وضعیت اتصال
                        </h4>
                        <div class="toolbar-filters">
                            <button type="button" class="chip is-selected filter-btn status-filter" data-status="all">همه</button>
                            <button type="button" class="chip filter-btn status-filter" data-status="online">آنلاین</button>
                            <button type="button" class="chip filter-btn status-filter" data-status="offline">آفلاین</button>
                        </div>
                    </div>

                    <div>
                        <h4 class="filter-group-label">
                            <svg class="icon" aria-hidden="true"><use href="#user"></use></svg>
                            نقش کاربری
                        </h4>
                        <div class="filter-row">
                            <button type="button" class="chip is-selected filter-btn user-type-filter" data-usertype="all">همه</button>
                            <button type="button" class="chip filter-btn user-type-filter" data-usertype="admin">مدیر</button>
                            <button type="button" class="chip filter-btn user-type-filter" data-usertype="operator">اپراتور</button>
                            <button type="button" class="chip filter-btn user-type-filter" data-usertype="verifier">تأیید کننده</button>
                        </div>
                    </div>
                </div>

                <div class="filters-summary">
                    <span id="activeFiltersDisplay">فیلترهای فعال: <strong id="activeFiltersCount">۰</strong></span>
                    <button type="button" class="btn btn-sm btn-ghost" id="clearFiltersBtn">پاک کردن همه</button>
                </div>
            </div>
        </section>

        <div class="date-group-list" id="usersGrid">
            <div class="user-card-skeleton is-hidden" id="skeletonLoader"></div>
        </div>

        <div class="empty-state is-hidden" id="emptyState">
            <div class="empty-state-icon">
                <svg class="icon" aria-hidden="true"><use href="#users-slash"></use></svg>
            </div>
            <h3>هیچ کاربری یافت نشد</h3>
            <p>با فیلترهای انتخاب‌شده هیچ کاربری پیدا نشد. لطفاً فیلترها را تغییر دهید.</p>
            <button type="button" class="btn btn-primary" id="emptyClearFiltersBtn">پاک کردن فیلترها</button>
        </div>

    </main>

    <dialog id="userModal" class="dialog">
        <form method="dialog" class="dialog-body">
            <header class="dialog-header">
                <h2 class="dialog-title">
                    <svg class="icon" aria-hidden="true"><use href="#id-card"></use></svg>
                    جزئیات کاربر
                </h2>
                <button type="button" class="btn-icon" data-close aria-label="بستن">
                    <svg class="icon" aria-hidden="true"><use href="#close"></use></svg>
                </button>
            </header>
            <div class="dialog-content" id="modalContent"></div>
        </form>
    </dialog>

    <dialog id="activityModal" class="dialog">
        <form method="dialog" class="dialog-body">
            <header class="dialog-header">
                <h2 class="dialog-title">
                    <svg class="icon" aria-hidden="true"><use href="#history"></use></svg>
                    سابقه فعالیت
                </h2>
                <button type="button" class="btn-icon" data-close aria-label="بستن">
                    <svg class="icon" aria-hidden="true"><use href="#close"></use></svg>
                </button>
            </header>
            <div class="dialog-content" id="activityModalContent"></div>
        </form>
    </dialog>

    <dialog id="statsModal" class="dialog dialog-lg">
        <form method="dialog" class="dialog-body">
            <header class="dialog-header">
                <h2 class="dialog-title">
                    <svg class="icon" aria-hidden="true"><use href="#chart-pie"></use></svg>
                    آمار پیشرفته سیستم
                </h2>
                <button type="button" class="btn-icon" data-close aria-label="بستن">
                    <svg class="icon" aria-hidden="true"><use href="#close"></use></svg>
                </button>
            </header>
            <div class="dialog-content" id="statsModalContent"></div>
        </form>
    </dialog>

    <dialog id="logoutConfirmModal" class="dialog dialog-sm">
        <form method="dialog" class="dialog-body">
            <div class="dialog-content confirm-body">
                <div class="confirm-icon">
                    <svg class="icon" aria-hidden="true"><use href="#exclamation-triangle"></use></svg>
                </div>
                <h2>تأیید خروج</h2>
                <p class="muted" id="logoutConfirmText">آیا مطمئن هستید؟</p>
            </div>
            <footer class="dialog-footer confirm-footer">
                <button type="button" class="btn btn-ghost" id="cancelLogout">انصراف</button>
                <button type="button" class="btn btn-danger" id="confirmLogout">تأیید خروج</button>
            </footer>
        </form>
    </dialog>

    <div class="toasts" id="toasts" aria-live="polite" aria-atomic="false"></div>

    <script src="online_users_script.js?v=<?= filemtime(__DIR__ . '/online_users_script.js') ?>" defer></script>
</body>

</html>
