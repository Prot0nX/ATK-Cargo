<?php
// PHP/User/logout.php

declare(strict_types=1);

require_once __DIR__ . '/_guard.php';

use App\Core\Csrf;

// خروج فقط با POST و توکن CSRF معتبر پذیرفته می‌شود؛ یک <img src="logout.php"> در سایتی دیگر نباید بتواند نشست کاربر را ببندد.
if ($_SERVER['REQUEST_METHOD'] === 'POST' && Csrf::validate($_POST['csrf_token'] ?? null)) {
    ou_destroy_session();
}

$returnTo = ou_sanitize_return($_POST['return'] ?? null);
header('Location: login.php' . ($returnTo !== null ? ('?return=' . urlencode($returnTo)) : ''));
exit;
