<?php
require_once __DIR__ . '/config/config.php';

header('Content-Type: application/json; charset=utf-8');
header('X-Content-Type-Options: nosniff');
header('X-Frame-Options: DENY');
header('X-XSS-Protection: 1; mode=block');
header('Content-Security-Policy: default-src \'self\'');

// بررسی اینکه درخواست از طریق POST ارسال شده باشد
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Method Not Allowed']);
    exit;
}

// دریافت و تصفیه‌سازی ورودی‌ها
$receivedPassword = isset($_POST['password']) ? trim($_POST['password']) : '';
$passwordType = isset($_POST['passwordType']) ? trim($_POST['passwordType']) : '';

$response = [
    'success' => false,
    'message' => 'رمز عبور اشتباه است!'
];

session_start();

// محدودیت تعداد تلاش‌ها برای جلوگیری از حملات brute force
if (!isset($_SESSION['attempt_count'])) {
    $_SESSION['attempt_count'] = 0;
}

if ($_SESSION['attempt_count'] >= 5) {
    http_response_code(429); // Too Many Requests
    echo json_encode(['success' => false, 'message' => 'تعداد تلاش‌های ناموفق بیش از حد مجاز است. لطفا بعدا تلاش کنید.']);
    exit;
}

try {
    $conn = new mysqli(DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);
    if ($conn->connect_error) {
        throw new Exception("Connection failed: " . $conn->connect_error);
    }

    $conn->set_charset("utf8mb4");

    $stmt = $conn->prepare("SELECT password FROM Passwords WHERE passwordType = ?");
    if (!$stmt) {
        throw new Exception("Prepare failed: " . $conn->error);
    }

    $stmt->bind_param("s", $passwordType);
    if (!$stmt->execute()) {
        throw new Exception("Execute failed: " . $stmt->error);
    }

    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $row = $result->fetch_assoc();
        if (password_verify($receivedPassword, $row['password'])) {
            $response['success'] = true;
            $response['message'] = 'رمز عبور صحیح است!';
            $_SESSION['attempt_count'] = 0; // بازنشانی تعداد تلاش‌ها پس از موفقیت
        } else {
            $_SESSION['attempt_count']++;
        }
    } else {
        $_SESSION['attempt_count']++;
    }

} catch (Exception $e) {
    error_log($e->getMessage());
    $response['message'] = 'خطایی رخ داده است. لطفا بعدا تلاش کنید.';
} finally {
    if (isset($stmt)) {
        $stmt->close();
    }
    if (isset($conn)) {
        $conn->close();
    }
}

echo json_encode($response);
?>