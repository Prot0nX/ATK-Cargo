<?php
// PHP/Quota Reports/logout.php

declare(strict_types=1);

require_once __DIR__ . '/_guard.php';

use App\Core\Csrf;

// خروج فقط با POST و توکن CSRF معتبر پذیرفته می‌شود؛ یک <img src="logout.php"> در سایتی دیگر نباید بتواند نشست کاربر را ببندد.
if ($_SERVER['REQUEST_METHOD'] === 'POST' && Csrf::validate($_POST['csrf_token'] ?? null)) {
    qr_destroy_session();
}

// اگر خروج از پنل دیگری (مثلاً Realtime_Dashboard) درخواست شده، بعد از ورود مجدد باید به همان‌جا برگردیم — همان return که آن پنل در فرم خروج جاسازی کرده، به login.php منتقل می‌شود.
$returnTo = qr_sanitize_return($_POST['return'] ?? null);
header('Location: login.php' . ($returnTo !== null ? ('?return=' . urlencode($returnTo)) : ''));
exit;
