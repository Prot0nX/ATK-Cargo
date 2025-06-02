<?php
header('Content-Type: application/json; charset=utf-8');
error_reporting(E_ALL);
ini_set('display_errors', 0);

require_once __DIR__ . '/config/config.php';
require_once __DIR__ . '/jdf.php';

// تنظیمات عملکرد PHP برای بهینه‌سازی
ini_set('memory_limit', '128M');
ini_set('max_execution_time', 30);

/**
 * پاک‌سازی و تأیید داده‌های ورودی
 * @param mixed $input داده ورودی
 * @return string داده پاک‌سازی شده
 */
function sanitize_input($input) {
    return htmlspecialchars(trim($input), ENT_QUOTES, 'UTF-8');
}

/**
 * ارسال پاسخ JSON با کد وضعیت مناسب
 * @param array $data داده‌های پاسخ
 * @param int $status_code کد وضعیت HTTP
 */
function send_json_response($data, $status_code = 200) {
    http_response_code($status_code);
    echo json_encode($data, JSON_UNESCAPED_UNICODE);
    exit();
}

/**
 * اعتبارسنجی داده‌های خروج
 * @param string $netWeight وزن خالص
 * @param string $scaleReceiptNumber شماره قبض باسکول
 * @throws Exception در صورت نامعتبر بودن داده‌ها
 */
function validateExitData($netWeight, $scaleReceiptNumber) {
    if (empty($netWeight) || !is_numeric($netWeight) || $netWeight <= 0) {
        throw new Exception("وزن خالص باید عددی مثبت و بزرگتر از صفر باشد.");
    }
    
    if (empty($scaleReceiptNumber)) {
        throw new Exception("شماره قبض باسکول نمی‌تواند خالی باشد.");
    }
    
    if (strlen($scaleReceiptNumber) < 8 || strlen($scaleReceiptNumber) > 10) {
        throw new Exception("شماره قبض باسکول باید بین 8 تا 10 رقم باشد.");
    }
    
    if (!preg_match('/^[0-9]+$/', $scaleReceiptNumber)) {
        throw new Exception("شماره قبض باسکول باید فقط شامل اعداد باشد.");
    }
}

/**
 * اعتبارسنجی داده‌های ورودی
 * @param array $params پارامترهای درخواست
 * @param array $required_fields فیلدهای اجباری
 * @return array خطاها
 */
function validate_request_data($params, $required_fields) {
    $errors = [];
    
    foreach ($required_fields as $field) {
        if (empty($params[$field])) {
            $errors[] = "پارامتر $field الزامی است.";
        }
    }
    
    return $errors;
}

// دریافت و تجزیه داده‌های JSON ورودی
$data = json_decode(file_get_contents("php://input"), true);
if (json_last_error() !== JSON_ERROR_NONE) {
    send_json_response(['error' => true, 'message' => 'داده JSON نامعتبر است: ' . json_last_error_msg()], 400);
}

// تعریف فیلدهای اجباری و اختیاری
$required_fields = ['shipName', 'loadingWarehouse', 'cargoType', 'shippingCompany', 'loadingQuotaNumber', 'trackingNumber', 'username', 'userType'];
$optional_fields = ['entryTime', 'netWeight', 'scaleReceiptNumber', 'shortageWeight', 'excessWeight', 'exitTime', 'exitDate', 'status', 'confirmation', 'numberOfPeople'];

// بهینه‌سازی: استفاده از آرایه $params با مقادیر پیش‌فرض برای فیلدهای اختیاری
$params = [];
foreach ($required_fields as $field) {
    $params[$field] = isset($data[$field]) ? sanitize_input($data[$field]) : '';
}

foreach ($optional_fields as $field) {
    $params[$field] = isset($data[$field]) ? sanitize_input($data[$field]) : '';
}

try {
    // اعتبارسنجی داده‌های درخواست
    $validation_errors = validate_request_data($params, $required_fields);
    if (!empty($validation_errors)) {
        throw new Exception(implode(" ", $validation_errors));
    }
    
    // اتصال به پایگاه داده
    $conn = new mysqli(DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);
    if ($conn->connect_error) {
        throw new Exception("خطا در اتصال به پایگاه داده: " . $conn->connect_error);
    }
    
    // تنظیم کاراکترست برای پشتیبانی از یونیکد
    $conn->set_charset("utf8mb4");
    
    // شروع تراکنش برای اطمینان از ACID
    $conn->begin_transaction();
    
    // بررسی وجود حواله با شماره و کوتاژ مشخص
    $stmt = $conn->prepare("SELECT * FROM CargoInfo WHERE shipName = ? AND loadingWarehouse = ? AND cargoType = ? AND shippingCompany = ? AND loadingQuotaNumber = ? AND trackingNumber = ?");
    if (!$stmt) {
        throw new Exception("خطا در آماده‌سازی دستور SQL: " . $conn->error);
    }
    
    $stmt->bind_param("ssssss", 
        $params['shipName'], 
        $params['loadingWarehouse'], 
        $params['cargoType'], 
        $params['shippingCompany'], 
        $params['loadingQuotaNumber'], 
        $params['trackingNumber']
    );
    
    if (!$stmt->execute()) {
        throw new Exception("خطا در اجرای دستور SQL: " . $stmt->error);
    }
    
    $result = $stmt->get_result();
    $existingCargo = $result->fetch_assoc();
    
    // منطق ثبت حواله جدید یا بروزرسانی حواله موجود
    if (!$existingCargo) {
        // اعتبارسنجی تعداد نفرات برای حواله‌های جدید
        if (empty($params['numberOfPeople']) || !is_numeric($params['numberOfPeople']) || $params['numberOfPeople'] < 1) {
            throw new Exception("تعداد نفرات باید عددی بزرگتر از صفر باشد");
        }

        // آماده‌سازی و اجرای دستور درج
        $insert_stmt = $conn->prepare("INSERT INTO CargoInfo (
            trackingNumber, entryTime, netWeight, scaleReceiptNumber, shortageWeight, excessWeight, 
            status, shipName, loadingWarehouse, cargoType, shippingCompany, loadingQuotaNumber, 
            numberOfPeople, username, userType
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        
        if (!$insert_stmt) {
            throw new Exception("خطا در آماده‌سازی دستور درج: " . $conn->error);
        }
        
        $status = "ورود";
        $entryTime = jdate("H:i");
        
        $insert_stmt->bind_param("sssssssssssssss", 
            $params['trackingNumber'], 
            $entryTime, 
            $params['netWeight'], 
            $params['scaleReceiptNumber'], 
            $params['shortageWeight'], 
            $params['excessWeight'], 
            $status, 
            $params['shipName'], 
            $params['loadingWarehouse'], 
            $params['cargoType'], 
            $params['shippingCompany'], 
            $params['loadingQuotaNumber'], 
            $params['numberOfPeople'], 
            $params['username'], 
            $params['userType']
        );
        
        if (!$insert_stmt->execute()) {
            throw new Exception("خطا در اجرای دستور درج: " . $insert_stmt->error);
        }
        
        $insert_stmt->close();
        
        // تأیید تراکنش
        $conn->commit();
        
        // ارسال پاسخ موفقیت‌آمیز
        send_json_response([
            "success" => true, 
            "message" => "حواله جدید با شماره {$params['trackingNumber']} و تعداد نفرات {$params['numberOfPeople']} در ساعت $entryTime توسط کاربر {$params['username']} با نقش {$params['userType']} با موفقیت ثبت شد"
        ]);
    } else {
        // منطق بروزرسانی حواله موجود
        if ($existingCargo['status'] == "ورود") {
            if (!empty($params['netWeight'])) {
                // خروج حواله - نیاز به تأیید
                if ($existingCargo['confirm'] != "تائید شده") {
                    throw new Exception("حواله مورد نظر توسط بارشمار هنوز تائید نشده است!");
                }
                
                // اعتبارسنجی وزن خالص و شماره قبض باسکول
                validateExitData($params['netWeight'], $params['scaleReceiptNumber']);
                
                // آماده‌سازی و اجرای دستور بروزرسانی
                $update_stmt = $conn->prepare("UPDATE CargoInfo SET 
                    netWeight = ?, 
                    scaleReceiptNumber = ?, 
                    exitTime = ?, 
                    exitDate = ?, 
                    status = 'خروج', 
                    username = ?, 
                    userType = ? 
                WHERE trackingNumber = ? AND loadingQuotaNumber = ? AND shipName = ? AND loadingWarehouse = ? AND cargoType = ? AND shippingCompany = ?");
                
                $exitTime = jdate("H:i");
                $exitDate = jdate("Y/m/d");
                
                $update_stmt->bind_param("ssssssssssss", 
                    $params['netWeight'], 
                    $params['scaleReceiptNumber'], 
                    $exitTime, 
                    $exitDate, 
                    $params['username'], 
                    $params['userType'], 
                    $params['trackingNumber'], 
                    $params['loadingQuotaNumber'],
                    $params['shipName'],
                    $params['loadingWarehouse'],
                    $params['cargoType'],
                    $params['shippingCompany']
                );
            } elseif (!empty($params['shortageWeight']) || !empty($params['excessWeight'])) {
                // بروزرسانی کسری/اضافه بار
                $update_stmt = $conn->prepare("UPDATE CargoInfo SET 
                    shortageWeight = ?, 
                    excessWeight = ?, 
                    username = ?, 
                    userType = ? 
                WHERE trackingNumber = ? AND loadingQuotaNumber = ?");
                
                $update_stmt->bind_param("ssssss", 
                    $params['shortageWeight'], 
                    $params['excessWeight'], 
                    $params['username'], 
                    $params['userType'], 
                    $params['trackingNumber'], 
                    $params['loadingQuotaNumber']
                );
            } else {
                throw new Exception("برای حواله در وضعیت ورود، باید وزن خالص یا کسری/اضافه بار وارد شود");
            }
        } elseif ($existingCargo['status'] == "خروج") {
            // بروزرسانی حواله خروج شده - نیاز به تأیید کاربر
            if ($params['confirmation'] != "yes") {
                send_json_response([
                    "message" => "شماره حواله {$params['trackingNumber']} در تاریخ {$existingCargo['exitDate']} و ساعت {$existingCargo['exitTime']} خروج کرده و سرویس بسته شده است!", 
                    "status" => "confirmation_needed", 
                    "exitDate" => $existingCargo['exitDate'], 
                    "exitTime" => $existingCargo['exitTime']
                ]);
            }
            
            // اعتبارسنجی وزن خالص و شماره قبض باسکول
            validateExitData($params['netWeight'], $params['scaleReceiptNumber']);
            
            // آماده‌سازی و اجرای دستور بروزرسانی
            $update_stmt = $conn->prepare("UPDATE CargoInfo SET 
                netWeight = ?, 
                scaleReceiptNumber = ?, 
                exitTime = ?, 
                exitDate = ?, 
                username = ?, 
                userType = ? 
            WHERE trackingNumber = ? AND loadingQuotaNumber = ?");
            
            $exitTime = jdate("H:i");
            $exitDate = jdate("Y/m/d");
            
            $update_stmt->bind_param("ssssssss", 
                $params['netWeight'], 
                $params['scaleReceiptNumber'], 
                $exitTime, 
                $exitDate, 
                $params['username'], 
                $params['userType'], 
                $params['trackingNumber'], 
                $params['loadingQuotaNumber']
            );
        } else {
            throw new Exception("وضعیت نامعتبر حواله");
        }
        
        // اجرای دستور بروزرسانی
        if (!$update_stmt->execute()) {
            throw new Exception("خطا در اجرای دستور به‌روزرسانی: " . $update_stmt->error);
        }
        
        $update_stmt->close();
        
        // تأیید تراکنش
        $conn->commit();
        
        // ارسال پاسخ موفقیت‌آمیز
        $response = [
            "success" => true,
            "message" => "عملیات با موفقیت انجام شد",
            "exitDate" => $exitDate ?? null,
            "exitTime" => $exitTime ?? null,
            "trackingNumber" => $params['trackingNumber'],
            "loadingQuotaNumber" => $params['loadingQuotaNumber']
        ];
        
        send_json_response($response);
    }
} catch(Exception $e) {
    // لغو تراکنش در صورت بروز خطا
    if (isset($conn) && $conn instanceof mysqli) {
        $conn->rollback();
    }
    
    // ثبت خطا در فایل لاگ
    error_log("خطا در saveOrUpdateCargoInfo.php: " . $e->getMessage());
    
    // ارسال پیام خطا به کلاینت
    send_json_response([
        'error' => true, 
        'message' => $e->getMessage()
    ], 500);
} finally {
    // آزادسازی منابع
    if (isset($stmt) && $stmt instanceof mysqli_stmt) $stmt->close();
    if (isset($insert_stmt) && $insert_stmt instanceof mysqli_stmt) $insert_stmt->close();
    if (isset($update_stmt) && $update_stmt instanceof mysqli_stmt) $update_stmt->close();
    if (isset($conn) && $conn instanceof mysqli) $conn->close();
}
?>