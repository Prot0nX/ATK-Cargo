<?php
session_start();

header('Content-Type: application/json');
header('X-Content-Type-Options: nosniff');
header('X-Frame-Options: DENY');
header('X-XSS-Protection: 1; mode=block');
header('Content-Security-Policy: default-src \'self\'');
header('Strict-Transport-Security: max-age=31536000; includeSubDomains');

// تولید توکن CSRF جدید اگر وجود نداشته باشد
if (empty($_SESSION['csrf_token'])) {
    $_SESSION['csrf_token'] = bin2hex(random_bytes(32));
}

echo json_encode(['token' => $_SESSION['csrf_token']]); 