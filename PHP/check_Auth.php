<?php
// تنظیم منطقه زمانی تهران
date_default_timezone_set('Asia/Tehran');

header('Content-Type: application/json; charset=UTF-8');
error_reporting(E_ALL);
ini_set('display_errors', 0);
require_once __DIR__ . '/config/config.php';
require_once __DIR__ . '/SessionManager.php';

// تنظیم هدرهای امنیتی
header('X-Content-Type-Options: nosniff');
header('X-Frame-Options: DENY');
header('X-XSS-Protection: 1; mode=block');
header('Content-Security-Policy: default-src \'self\'');
header('Strict-Transport-Security: max-age=31536000; includeSubDomains; preload');
header('Cache-Control: no-store, no-cache, must-revalidate');

function send_json_response($status, $message, $http_code = 200, $userType = null) {
    http_response_code($http_code);
    echo json_encode(["success" => $status, "message" => $message, "userType" => $userType], JSON_UNESCAPED_UNICODE);
    exit();
}

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    send_json_response(false, "روش درخواست مجاز نیست. لطفاً از روش POST استفاده کنید.", 405);
}

$jsonInput = file_get_contents('php://input');
if (empty($jsonInput)) {
    send_json_response(false, "داده‌ای دریافت نشد", 400);
}

$data = json_decode($jsonInput, true);

if (json_last_error() !== JSON_ERROR_NONE) {
    error_log("Invalid JSON received: " . substr($jsonInput, 0, 100));
    send_json_response(false, "فرمت JSON نامعتبر است", 400);
}

// اعتبارسنجی و پاکسازی ورودی‌ها
$username = isset($data['username']) ? htmlspecialchars(trim($data['username']), ENT_QUOTES, 'UTF-8') : '';
$password = isset($data['password']) ? $data['password'] : '';
$userType = isset($data['userType']) ? htmlspecialchars(trim($data['userType']), ENT_QUOTES, 'UTF-8') : '';
$deviceModel = isset($data['deviceModel']) ? htmlspecialchars(trim($data['deviceModel']), ENT_QUOTES, 'UTF-8') : '';
$deviceId = isset($data['deviceId']) ? htmlspecialchars(trim($data['deviceId']), ENT_QUOTES, 'UTF-8') : '';
$androidVersion = isset($data['androidVersion']) ? htmlspecialchars(trim($data['androidVersion']), ENT_QUOTES, 'UTF-8') : '';

if (empty($username) || empty($password)) {
    send_json_response(false, "نام کاربری و رمز عبور الزامی است.", 400);
}

try {
    $pdo = new PDO("mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=utf8mb4", DB_USER, DB_PASSWORD, [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES => false
    ]);

    // استفاده از prepared statement برای جلوگیری از حملات SQL injection
    $stmt = $pdo->prepare("SELECT password, userType FROM Users WHERE username = :username LIMIT 1");
    $stmt->bindParam(':username', $username, PDO::PARAM_STR);
    $stmt->execute();
    $user = $stmt->fetch();

    // ثبت تلاش ورود با اطلاعات محدود برای امنیت بیشتر
    error_log("Login attempt - Username hash: " . md5($username) . ", Result: " . ($user ? 'Found' : 'Not found'));

    if ($user && hash_equals($user['password'], $password)) {
        $allowedAccess = false;
        switch ($user['userType']) {
            case 'admin':
                $allowedAccess = true;
                break;
            case 'operator':
                $allowedAccess = ($userType === '' || $userType === 'select_info' || $userType === 'initial_info');
                break;
            case 'verifier':
                $allowedAccess = ($userType === '' || $userType === 'cargo_counter');
                break;
            default:
                $allowedAccess = ($user['userType'] === $userType);
        }

        if ($allowedAccess) {
            try {
                // استفاده از SessionManager برای مدیریت جلسات
                $sessionManager = new SessionManager();
                $ipAddress = $_SERVER['REMOTE_ADDR'] ?? 'Unknown';
                
                // ایجاد جلسه جدید با ارسال نوع کاربر
                $sessionResult = $sessionManager->createSession(
                    $username, 
                    $deviceId, 
                    $deviceModel, 
                    $androidVersion, 
                    $ipAddress,
                    $user['userType'] // ارسال نوع کاربر
                );
                
                if ($sessionResult['success']) {
                    // ایجاد تأخیر ثابت برای جلوگیری از حملات timing-based
                    usleep(rand(5000, 10000));
                    send_json_response(true, "ورود موفقیت‌آمیز", 200, $user['userType']);
                } else {
                    send_json_response(false, $sessionResult['message'], 409);
                }
                
            } catch (Exception $sessionError) {
                if (strpos($sessionError->getMessage(), 'دستگاه دیگری') !== false) {
                    send_json_response(false, "شما در حال حاضر در دستگاه دیگری وارد سیستم هستید. لطفاً ابتدا از آن دستگاه خارج شوید.", 409);
                } else {
                    error_log("خطا در مدیریت جلسه: " . $sessionError->getMessage());
                    send_json_response(false, "خطا در ایجاد جلسه کاربری", 500);
                }
            }
        } else {
            send_json_response(false, "شما دسترسی لازم برای این بخش را ندارید.", 403);
        }
    } else {
        // ایجاد تأخیر مشابه برای عدم موفقیت در ورود
        usleep(rand(5000, 10000));
        send_json_response(false, "نام کاربری یا رمز عبور اشتباه است!", 401);
    }
} catch (PDOException $e) {
    error_log("Database error: " . $e->getMessage());
    send_json_response(false, "خطایی رخ داده است. لطفا بعدا تلاش کنید.", 500);
}
?>