<?php
// PHP/Lic/login.php ورود به پنل مدیریت لایسنس.

declare(strict_types=1);

require_once __DIR__ . '/_guard.php';

use App\Core\Config;
use App\Core\Csrf;
use App\Core\Logger;
use App\Core\Request;
use App\Services\LoginAttemptLimiter;

lic_send_page_headers();

if (lic_is_authenticated()) {
    header('Location: index.php');
    exit;
}

$error = '';
$passwordHash = (string)Config::getInstance()->get('lic_admin_password_hash', '');

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $clientIp = (new Request())->getClientIp();
    $limiter = new LoginAttemptLimiter();
    $logger = Logger::getInstance();

    if (!Csrf::validate($_POST['csrf_token'] ?? null)) {
        $error = 'توکن امنیتی نامعتبر است. لطفاً دوباره تلاش کنید.';
    } elseif ($passwordHash === '') {
        // همان رفتار PermissionManager: بدون پیکربندی، ورود ممکن نیست — نه اینکه به یک مقدار پیش‌فرض برگردد.
        $error = 'خطای پیکربندی: متغیر LIC_ADMIN_PASSWORD_HASH در فایل .env تنظیم نشده است.';
        $logger->security('Lic panel login attempted while LIC_ADMIN_PASSWORD_HASH is unset');
    } elseif ($limiter->isLocked(LIC_ACTOR, $clientIp)) {
        // پیام عمداً با پیام «رمز اشتباه» یکسان است تا مهاجم نفهمد آیا به سقف تلاش رسیده یا صرفاً رمز را غلط زده.
        $error = 'رمز عبور نادرست است.';
    } elseif (password_verify((string)($_POST['password'] ?? ''), $passwordHash)) {
        $limiter->resetAttempts(LIC_ACTOR, $clientIp);

        // جلوگیری از session fixation: شناسه‌ی نشست پس از احراز هویت عوض می‌شود تا شناسه‌ای که مهاجم از قبل به قربانی داده بی‌اثر شود.
        session_regenerate_id(true);
        $_SESSION[LIC_SESSION_KEY] = true;
        $_SESSION['last_activity'] = time();
        Csrf::token();

        $logger->security("Lic panel login successful from {$clientIp}");
        header('Location: index.php');
        exit;
    } else {
        // registerFailedAttempt خودش تأخیر تصاعدی (۲/۴/۸ ثانیه) اعمال می‌کند.
        $limiter->registerFailedAttempt(LIC_ACTOR, $clientIp);
        $logger->security("Lic panel login failed from {$clientIp}");
        $error = 'رمز عبور نادرست است.';
    }
}

$csrfToken = Csrf::token();
$theme = lic_theme();
?>
<!DOCTYPE html>
<html lang="fa" dir="rtl"<?= $theme !== '' ? ' data-theme="' . e($theme) . '"' : '' ?>>

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>ورود — مدیریت لایسنس ATK</title>
    <link rel="icon" type="image/x-icon" href="assets/img/fav.ico">
    <link rel="stylesheet" href="assets/app.css">
</head>

<body class="page-centered">
<?php require __DIR__ . '/_icons.php'; ?>

    <main class="auth-card">
        <div class="auth-brand">
            <svg class="icon icon-lg" aria-hidden="true"><use href="#key"></use></svg>
            <div>
                <h1>مدیریت لایسنس</h1>
                <p class="muted">ATK Cargo</p>
            </div>
        </div>

        <?php if ($error !== ''): ?>
            <p class="alert alert-error" role="alert"><?= e($error) ?></p>
        <?php endif; ?>

        <form method="post" action="login.php" autocomplete="off">
            <input type="hidden" name="csrf_token" value="<?= e($csrfToken) ?>">

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

    <script src="assets/login.js"></script>
</body>

</html>