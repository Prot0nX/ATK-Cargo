<?php
// PHP/User/login.php ورود به پنل مدیریت کاربران آنلاین.

declare(strict_types=1);

require_once __DIR__ . '/_guard.php';

use App\Core\Config;
use App\Core\Csrf;
use App\Core\Logger;
use App\Core\Request;
use App\Services\LoginAttemptLimiter;

ou_send_page_headers();

// اگر پنل دیگری کاربر را برای ورود به این‌جا فرستاده، بعد از ورود باید به همان‌جا برگردیم، نه همیشه به index.php همین پنل.
$returnTo = ou_sanitize_return($_GET['return'] ?? ($_POST['return'] ?? null)) ?? 'index.php';

if (ou_is_authenticated()) {
    header('Location: ' . $returnTo);
    exit;
}

$error = '';
$passwordHash = (string)Config::getInstance()->get('online_users_admin_password_hash', '');

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $clientIp = (new Request())->getClientIp();
    $limiter = new LoginAttemptLimiter();
    $logger = Logger::getInstance();

    if (!Csrf::validate($_POST['csrf_token'] ?? null)) {
        $error = 'توکن امنیتی نامعتبر است. لطفاً دوباره تلاش کنید.';
    } elseif ($passwordHash === '') {
        $error = 'خطای پیکربندی: متغیر ONLINE_USERS_ADMIN_PASSWORD_HASH در فایل .env تنظیم نشده است.';
        $logger->security('Online Users panel login attempted while ONLINE_USERS_ADMIN_PASSWORD_HASH is unset');
    } elseif ($limiter->isLocked(OU_ACTOR, $clientIp)) {
        // پیام عمداً با پیام «رمز اشتباه» یکسان است تا مهاجم نفهمد آیا به سقف تلاش رسیده یا صرفاً رمز را غلط زده.
        $error = 'رمز عبور نادرست است.';
    } elseif (password_verify((string)($_POST['password'] ?? ''), $passwordHash)) {
        $limiter->resetAttempts(OU_ACTOR, $clientIp);

        session_regenerate_id(true);
        $_SESSION[OU_SESSION_KEY] = true;
        $_SESSION['last_activity'] = time();
        Csrf::token();

        $logger->security("Online Users panel login successful from {$clientIp}");
        header('Location: ' . $returnTo);
        exit;
    } else {
        $limiter->registerFailedAttempt(OU_ACTOR, $clientIp);
        $logger->security("Online Users panel login failed from {$clientIp}");
        $error = 'رمز عبور نادرست است.';
    }
}

$csrfToken = Csrf::token();
$theme = ou_theme();
?>
<!DOCTYPE html>
<html lang="fa" dir="rtl"<?= $theme !== '' ? ' data-theme="' . e($theme) . '"' : '' ?>>

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>ورود — مدیریت کاربران آنلاین ATK</title>
    <link rel="icon" type="image/x-icon" href="assets/img/fav.ico">
    <link rel="preload" href="../assets/ui/fonts/Vazirmatn-Regular.woff2" as="font" type="font/woff2" crossorigin>
    <link rel="stylesheet" href="../assets/ui/core.css?v=<?= filemtime(__DIR__ . '/../assets/ui/core.css') ?>">
    <link rel="stylesheet" href="assets/app.css?v=<?= filemtime(__DIR__ . '/assets/app.css') ?>">
</head>

<body class="page-centered">
<?php require __DIR__ . '/_icons.php'; ?>

    <main class="auth-card">
        <div class="auth-brand">
            <svg class="icon icon-lg" aria-hidden="true"><use href="#users"></use></svg>
            <div>
                <h1>مدیریت کاربران آنلاین</h1>
                <p class="muted">ATK Cargo</p>
            </div>
        </div>

        <?php if ($error !== ''): ?>
            <p class="alert alert-error" role="alert"><?= e($error) ?></p>
        <?php endif; ?>

        <form method="post" action="login.php" autocomplete="off">
            <input type="hidden" name="csrf_token" value="<?= e($csrfToken) ?>">
            <input type="hidden" name="return" value="<?= e($returnTo) ?>">

            <label class="field">
                <span class="field-label">رمز عبور</span>
                <span class="field-input-group">
                    <input type="password" name="password" id="password" required autofocus
                        autocomplete="current-password" placeholder="رمز عبور پنل">
                    <button type="button" class="btn-icon" id="togglePassword"
                        aria-label="نمایش رمز عبور">
                        <svg class="icon" aria-hidden="true"><use href="#eye"></use></svg>
                    </button>
                </span>
            </label>

            <button type="submit" class="btn btn-primary btn-block">ورود</button>
        </form>
    </main>

    <script src="assets/login.js" defer></script>
</body>

</html>
