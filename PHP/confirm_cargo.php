<?php
header('Content-Type: application/json; charset=UTF-8');
error_reporting(E_ALL);
ini_set('display_errors', 0);
require_once __DIR__ . '/config/config.php';

// تنظیم منطقه زمانی تهران/ایران
date_default_timezone_set('Asia/Tehran');

function send_json_response($status, $message, $data = [], $http_code = 200) {
    http_response_code($http_code);
    $response = [
        "status" => $status, 
        "message" => $message
    ];
    
    if (!empty($data)) {
        $response["data"] = $data;
    }
    
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
    exit();
}

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    send_json_response("error", "روش درخواست مجاز نیست. لطفاً از روش POST استفاده کنید.", [], 405);
}

$data = json_decode(file_get_contents("php://input"), true);
if (json_last_error() !== JSON_ERROR_NONE) {
    send_json_response("error", "فرمت JSON نامعتبر است", [], 400);
}

// بررسی همه پارامترهای مورد نیاز (4 پارامتر اصلی + اطلاعات کاربر)
$requiredFields = ['trackingNumber', 'loadingQuotaNumber', 'loadingWarehouse', 'shippingCompany', 'cargoType', 'username', 'userType'];
foreach ($requiredFields as $field) {
    if (empty($data[$field])) {
        send_json_response("error", "فیلد ضروری وجود ندارد: $field", [], 400);
    }
}

$sanitizedData = array_map(function($value) {
    return htmlspecialchars(trim($value), ENT_QUOTES, 'UTF-8');
}, $data);

try {
    $conn = new mysqli(DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);
    if ($conn->connect_error) {
        throw new Exception("خطا در اتصال به پایگاه داده: " . $conn->connect_error);
    }
    $conn->set_charset("utf8mb4");

    // بررسی وجود حواله با 4 پارامتر
    $stmt = $conn->prepare("SELECT * FROM CargoInfo WHERE 
        trackingNumber = ? AND 
        loadingQuotaNumber = ? AND 
        loadingWarehouse = ? AND 
        shippingCompany = ? AND 
        cargoType = ?");
        
    if (!$stmt) {
        throw new Exception("خطا در آماده‌سازی دستور SQL: " . $conn->error);
    }
    
    $stmt->bind_param("sssss", 
        $sanitizedData['trackingNumber'], 
        $sanitizedData['loadingQuotaNumber'], 
        $sanitizedData['loadingWarehouse'], 
        $sanitizedData['shippingCompany'], 
        $sanitizedData['cargoType']
    );
    
    if (!$stmt->execute()) {
        throw new Exception("خطا در اجرای دستور SQL: " . $stmt->error);
    }
    
    $result = $stmt->get_result();
    if ($result->num_rows === 0) {
        send_json_response("error", "حواله با مشخصات ارسالی یافت نشد. لطفاً همه پارامترها را بررسی کنید.", [], 404);
    }
    $stmt->close();

    // زمان تایید فعلی بر اساس منطقه زمانی تهران
    $confirmTime = date('H:i:s');
    $confirmDate = date('Y-m-d');

    // به‌روزرسانی با 4 پارامتر برای اطمینان از صحت تایید
    $updateStmt = $conn->prepare("UPDATE CargoInfo SET 
        confirm = 'تائید شده', 
        confirm_username = ?, 
        confirm_usertype = ?, 
        updated_at = NOW() 
        WHERE trackingNumber = ? AND 
        loadingQuotaNumber = ? AND 
        loadingWarehouse = ? AND 
        shippingCompany = ? AND 
        cargoType = ?");
        
    if (!$updateStmt) {
        throw new Exception("خطا در آماده‌سازی دستور به‌روزرسانی: " . $conn->error);
    }
    
    $updateStmt->bind_param("sssssss", 
        $sanitizedData['username'], 
        $sanitizedData['userType'], 
        $sanitizedData['trackingNumber'], 
        $sanitizedData['loadingQuotaNumber'], 
        $sanitizedData['loadingWarehouse'], 
        $sanitizedData['shippingCompany'], 
        $sanitizedData['cargoType']
    );
    
    if (!$updateStmt->execute()) {
        throw new Exception("خطا در اجرای دستور به‌روزرسانی: " . $updateStmt->error);
    }
    
    if ($updateStmt->affected_rows > 0) {
        $responseData = [
            'trackingNumber' => $sanitizedData['trackingNumber'],
            'loadingQuotaNumber' => $sanitizedData['loadingQuotaNumber'],
            'loadingWarehouse' => $sanitizedData['loadingWarehouse'],
            'shippingCompany' => $sanitizedData['shippingCompany'],
            'cargoType' => $sanitizedData['cargoType'],
            'confirmTime' => $confirmTime,
            'confirmDate' => $confirmDate,
            'confirmUsername' => $sanitizedData['username'],
            'confirmUserType' => $sanitizedData['userType']
        ];
        
        send_json_response(
            "success", 
            "حواله شماره {$sanitizedData['trackingNumber']} با کوتاژ {$sanitizedData['loadingQuotaNumber']} در ساعت {$confirmTime} توسط {$sanitizedData['username']} با موفقیت تأیید شد", 
            $responseData
        );
    } else {
        send_json_response("error", "حواله قبلاً تأیید شده است یا تغییری اعمال نشد", [], 200);
    }
} catch (Exception $e) {
    send_json_response("error", $e->getMessage(), [], 500);
} finally {
    if (isset($updateStmt)) $updateStmt->close();
    if (isset($conn)) $conn->close();
}
?>