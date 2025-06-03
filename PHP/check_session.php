<?php
header('Content-Type: application/json; charset=UTF-8');
error_reporting(E_ALL);
ini_set('display_errors', 0);
require_once __DIR__ . '/config/config.php';

// تنظیم هدرهای امنیتی
header('X-Content-Type-Options: nosniff');
header('X-Frame-Options: DENY');
header('X-XSS-Protection: 1; mode=block');
header('Content-Security-Policy: default-src \'self\'');
header('Strict-Transport-Security: max-age=31536000; includeSubDomains; preload');

function send_json_response($status, $message, $http_code = 200, $userType = null) {
    http_response_code($http_code);
    echo json_encode(["success" => $status, "message" => $message, "userType" => $userType], JSON_UNESCAPED_UNICODE);
    exit();
}

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    send_json_response(false, "روش درخواست مجاز نیست. لطفاً از روش POST استفاده کنید.", 405);
}

$jsonInput = file_get_contents('php://input');
$data = json_decode($jsonInput, true);

if (json_last_error() !== JSON_ERROR_NONE) {
    error_log("Invalid JSON received: $jsonInput");
    send_json_response(false, "فرمت JSON نامعتبر است", 400);
}

$username = isset($data['username']) ? filter_var(trim($data['username']), FILTER_SANITIZE_STRING) : '';

if (empty($username)) {
    send_json_response(false, "نام کاربری الزامی است.", 400);
}

try {
    $pdo = new PDO("mysql:host=" . DB_HOST . ";dbname=" . DB_NAME, DB_USER, DB_PASSWORD);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    $pdo->exec("SET NAMES utf8mb4");

    $stmt = $pdo->prepare("SELECT userType FROM Users WHERE username = ?");
    $stmt->execute([$username]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    if ($user) {
        send_json_response(true, "جلسه کاربر معتبر است", 200, $user['userType']);
    } else {
        send_json_response(false, "کاربر در سیستم وجود ندارد", 404);
    }
} catch (PDOException $e) {
    error_log("Database error: " . $e->getMessage());
    send_json_response(false, "خطایی رخ داده است. لطفا بعدا تلاش کنید.", 500);
}
?>