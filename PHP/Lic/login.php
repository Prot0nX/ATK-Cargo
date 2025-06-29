<?php
session_start();

header('Content-Type: application/json');
header('X-Content-Type-Options: nosniff');
header('X-Frame-Options: DENY');
header('X-XSS-Protection: 1; mode=block');
header('Content-Security-Policy: default-src \'self\'');
header('Strict-Transport-Security: max-age=31536000; includeSubDomains');

require_once __DIR__ . '/../config/config.php';

// دریافت داده‌های POST
$postData = json_decode(file_get_contents('php://input'), true);

if (!isset($postData['password'])) {
    echo json_encode([
        'success' => false,
        'message' => 'رمز عبور وارد نشده است'
    ]);
    exit;
}

$inputPassword = $postData['password'];

try {
    $conn = getDbConnection();
    
    // بررسی رمز عبور در دیتابیس
    $stmt = $conn->prepare("SELECT password FROM Passwords WHERE passwordType = 'delete_info'");
    
    if (!$stmt->execute()) {
        throw new Exception("خطا در اجرای کوئری");
    }
    
    $result = $stmt->get_result();
    
    if ($result->num_rows === 0) {
        throw new Exception("رمز عبور در سیستم تعریف نشده است");
    }
    
    $row = $result->fetch_assoc();
    $storedPassword = $row['password'];
    
    // مقایسه رمز عبور
    if (password_verify($inputPassword, $storedPassword)) {
        // ایجاد توکن برای احراز هویت
        $token = bin2hex(random_bytes(32));
        $_SESSION['auth_token'] = $token;
        
        echo json_encode([
            'success' => true,
            'message' => 'ورود موفقیت‌آمیز',
            'token' => $token
        ]);
    } else {
        echo json_encode([
            'success' => false,
            'message' => 'رمز عبور نادرست است'
        ]);
    }
    
} catch (Exception $e) {
    error_log("خطا در لاگین: " . $e->getMessage());
    echo json_encode([
        'success' => false,
        'message' => 'خطا در ورود به سیستم'
    ]);
}

$stmt->close();
$conn->close(); 