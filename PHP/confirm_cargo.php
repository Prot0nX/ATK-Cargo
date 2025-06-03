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
    
    // استفاده از JSON_NUMERIC_CHECK برای تبدیل اعداد به عدد واقعی (نه رشته) و کاهش حجم JSON
    echo json_encode($response, JSON_UNESCAPED_UNICODE | JSON_NUMERIC_CHECK);
    exit();
}

// بررسی روش درخواست
if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    send_json_response("error", "روش درخواست مجاز نیست. لطفاً از روش POST استفاده کنید.", [], 405);
}

// شروع بافر خروجی برای بهینه‌سازی ارسال پاسخ
ob_start();

// دریافت و اعتبارسنجی داده‌های ورودی
$raw_data = file_get_contents("php://input");
$data = json_decode($raw_data, true);
if (json_last_error() !== JSON_ERROR_NONE) {
    ob_end_clean();
    send_json_response("error", "فرمت JSON نامعتبر است", [], 400);
}

// بررسی همه پارامترهای مورد نیاز (4 پارامتر اصلی + اطلاعات کاربر)
$requiredFields = ['trackingNumber', 'loadingQuotaNumber', 'loadingWarehouse', 'shippingCompany', 'cargoType', 'username', 'userType'];
$missingFields = [];
foreach ($requiredFields as $field) {
    if (empty($data[$field])) {
        $missingFields[] = $field;
    }
}

if (!empty($missingFields)) {
    ob_end_clean();
    send_json_response("error", "فیلدهای ضروری وجود ندارند: " . implode(', ', $missingFields), [], 400);
}

// پاکسازی داده‌های ورودی - فقط برای فیلدهایی که نیاز داریم
$sanitizedData = [];
foreach ($requiredFields as $field) {
    $sanitizedData[$field] = htmlspecialchars(trim($data[$field]), ENT_QUOTES, 'UTF-8');
}

try {
    // ایجاد اتصال به دیتابیس
    $conn = new mysqli(DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);
    
    // بررسی خطای اتصال
    if ($conn->connect_error) {
        throw new Exception("خطا در اتصال به پایگاه داده: " . $conn->connect_error);
    }
    
    // تنظیمات بهینه‌سازی اتصال
    $conn->set_charset("utf8mb4");
    $conn->query("SET SESSION sql_mode = ''");
    $conn->query("SET SESSION wait_timeout = 30");
    $conn->query("SET SESSION interactive_timeout = 30");
    
    // تراکنش برای اطمینان از یکپارچگی داده‌ها و بهبود کارایی
    $conn->begin_transaction();

    // استفاده از یک کوئری ترکیبی برای بررسی و به‌روزرسانی همزمان
    $query = "UPDATE CargoInfo 
             SET confirm = 'تائید شده', 
                 confirm_username = ?, 
                 confirm_usertype = ?, 
                 updated_at = NOW() 
             WHERE trackingNumber = ? 
               AND loadingQuotaNumber = ? 
               AND loadingWarehouse = ? 
               AND shippingCompany = ? 
               AND cargoType = ?
               AND (confirm IS NULL OR confirm != 'تائید شده')";

    $stmt = $conn->prepare($query);
    if (!$stmt) {
        throw new Exception("خطا در آماده‌سازی دستور SQL: " . $conn->error);
    }
    
    $stmt->bind_param("sssssss", 
        $sanitizedData['username'], 
        $sanitizedData['userType'], 
        $sanitizedData['trackingNumber'], 
        $sanitizedData['loadingQuotaNumber'], 
        $sanitizedData['loadingWarehouse'], 
        $sanitizedData['shippingCompany'], 
        $sanitizedData['cargoType']
    );
    
    // اجرای کوئری بهینه‌سازی شده
    if (!$stmt->execute()) {
        throw new Exception("خطا در اجرای دستور SQL: " . $stmt->error);
    }
    
    // بررسی تعداد رکوردهای تأثیرپذیر
    if ($stmt->affected_rows > 0) {
        // تأیید تراکنش
        $conn->commit();
        
        // زمان تایید فعلی
        $confirmTime = date('H:i:s');
        $confirmDate = date('Y/m/d');
        
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
        
        ob_end_clean();
        send_json_response(
            "success", 
            "حواله شماره {$sanitizedData['trackingNumber']} با کوتاژ {$sanitizedData['loadingQuotaNumber']} در ساعت {$confirmTime} توسط {$sanitizedData['username']} با موفقیت تأیید شد", 
            $responseData
        );
    } else {
        // بررسی وجود رکورد برای پیام خطای مناسب
        $checkQuery = "SELECT id FROM CargoInfo 
                      WHERE trackingNumber = ? 
                        AND loadingQuotaNumber = ? 
                        AND loadingWarehouse = ? 
                        AND shippingCompany = ? 
                        AND cargoType = ?";
        
        $checkStmt = $conn->prepare($checkQuery);
        $checkStmt->bind_param("sssss", 
            $sanitizedData['trackingNumber'], 
            $sanitizedData['loadingQuotaNumber'], 
            $sanitizedData['loadingWarehouse'], 
            $sanitizedData['shippingCompany'], 
            $sanitizedData['cargoType']
        );
        
        $checkStmt->execute();
        $checkResult = $checkStmt->get_result();
        
        $conn->commit();
        ob_end_clean();
        
        if ($checkResult->num_rows > 0) {
            send_json_response("error", "حواله قبلاً تأیید شده است یا تغییری اعمال نشد", [], 200);
        } else {
            send_json_response("error", "حواله با مشخصات ارسالی یافت نشد. لطفاً همه پارامترها را بررسی کنید.", [], 404);
        }
    }
} catch (Exception $e) {
    // برگرداندن تراکنش در صورت خطا
    if (isset($conn) && $conn->ping()) {
        $conn->rollback();
    }
    
    ob_end_clean();
    send_json_response("error", $e->getMessage(), [], 500);
} finally {
    // آزادسازی منابع
    if (isset($stmt)) $stmt->close();
    if (isset($checkStmt)) $checkStmt->close();
    if (isset($conn)) $conn->close();
}
?>