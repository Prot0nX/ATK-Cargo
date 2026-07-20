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

// بررسی پارامترهای مورد نیاز (فقط id و اطلاعات کاربر)
$requiredFields = ['id', 'username', 'userType'];
$missingFields = [];
foreach ($requiredFields as $field) {
    if (!isset($data[$field]) || (is_string($data[$field]) && trim($data[$field]) === '')) {
        $missingFields[] = $field;
    }
}

if (!empty($missingFields)) {
    ob_end_clean();
    send_json_response("error", "فیلدهای ضروری وجود ندارند: " . implode(', ', $missingFields), [], 400);
}

// پاکسازی داده‌های ورودی
$cargoId = intval($data['id']);
$username = htmlspecialchars(trim($data['username']), ENT_QUOTES, 'UTF-8');
$userType = htmlspecialchars(trim($data['userType']), ENT_QUOTES, 'UTF-8');

if ($cargoId <= 0) {
    ob_end_clean();
    send_json_response("error", "شناسه حواله نامعتبر است", [], 400);
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

    // استفاده از id برای بروزرسانی
    $query = "UPDATE CargoInfo 
             SET confirm = 'تائید شده', 
                 confirm_username = ?, 
                 confirm_usertype = ?, 
                 updated_at = NOW() 
             WHERE id = ?
               AND (confirm IS NULL OR confirm != 'تائید شده')";

    $stmt = $conn->prepare($query);
    if (!$stmt) {
        throw new Exception("خطا در آماده‌سازی دستور SQL: " . $conn->error);
    }
    
    $stmt->bind_param("ssi", 
        $username, 
        $userType, 
        $cargoId
    );
    
    // اجرای کوئری بهینه‌سازی شده
    if (!$stmt->execute()) {
        throw new Exception("خطا در اجرای دستور SQL: " . $stmt->error);
    }
    
    // بررسی تعداد رکوردهای تأثیرپذیر
    if ($stmt->affected_rows > 0) {
        // دریافت اطلاعات حواله برای پاسخ
        $infoQuery = "SELECT trackingNumber, loadingQuotaNumber FROM CargoInfo WHERE id = ?";
        $infoStmt = $conn->prepare($infoQuery);
        $infoStmt->bind_param("i", $cargoId);
        $infoStmt->execute();
        $infoResult = $infoStmt->get_result();
        $cargoData = $infoResult->fetch_assoc();
        $infoStmt->close();
        
        // تأیید تراکنش
        $conn->commit();
        
        // زمان تایید فعلی
        $confirmTime = date('H:i:s');
        $confirmDate = date('Y/m/d');
        
        $responseData = [
            'id' => $cargoId,
            'trackingNumber' => $cargoData['trackingNumber'] ?? '',
            'loadingQuotaNumber' => $cargoData['loadingQuotaNumber'] ?? '',
            'confirmTime' => $confirmTime,
            'confirmDate' => $confirmDate,
            'confirmUsername' => $username,
            'confirmUserType' => $userType
        ];
        
        ob_end_clean();
        send_json_response(
            "success", 
            "حواله شماره {$cargoData['trackingNumber']} با کوتاژ {$cargoData['loadingQuotaNumber']} در ساعت {$confirmTime} توسط {$username} با موفقیت تأیید شد", 
            $responseData
        );
    } else {
        // بررسی وجود رکورد برای پیام خطای مناسب
        $checkQuery = "SELECT id, confirm FROM CargoInfo WHERE id = ?";
        
        $checkStmt = $conn->prepare($checkQuery);
        $checkStmt->bind_param("i", $cargoId);
        
        $checkStmt->execute();
        $checkResult = $checkStmt->get_result();
        
        $conn->commit();
        ob_end_clean();
        
        if ($checkResult->num_rows > 0) {
            send_json_response("error", "حواله قبلاً تأیید شده است یا تغییری اعمال نشد", [], 200);
        } else {
            send_json_response("error", "حواله با شناسه ارسالی یافت نشد", [], 404);
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
    if (isset($infoStmt)) $infoStmt->close();
    if (isset($checkStmt)) $checkStmt->close();
    if (isset($conn)) $conn->close();
}
?>