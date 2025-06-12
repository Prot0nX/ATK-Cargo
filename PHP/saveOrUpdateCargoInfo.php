<?php
header('Content-Type: application/json; charset=utf-8');
error_reporting(E_ALL);
ini_set('display_errors', 0);

// تنظیم منطقه زمانی ایران
date_default_timezone_set('Asia/Tehran');

require_once __DIR__ . '/config/config.php';
require_once __DIR__ . '/jdf.php';

// تنظیمات عملکرد PHP برای بهینه‌سازی بیشتر
ini_set('memory_limit', '128M');
ini_set('max_execution_time', 30);
ini_set('zlib.output_compression', 'On'); // فعال‌سازی فشرده‌سازی خروجی
ini_set('output_buffering', 4096); // بافر خروجی برای پاسخ سریع‌تر

/**
 * پاک‌سازی و تأیید داده‌های ورودی
 */
function sanitize_input($input) {
    if (is_array($input)) {
        return array_map('sanitize_input', $input);
    }
    return htmlspecialchars(trim($input), ENT_QUOTES, 'UTF-8');
}

/**
 * ارسال پاسخ JSON با کد وضعیت مناسب
 */
function send_json_response($data, $status_code = 200) {
    http_response_code($status_code);
    echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_PARTIAL_OUTPUT_ON_ERROR);
    exit();
}

/**
 * اعتبارسنجی داده‌های خروج
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

// دریافت و تجزیه داده‌های JSON ورودی - استفاده از json_decode با پارامتر true برای بهینه‌سازی حافظه
$json_input = file_get_contents("php://input");
$data = json_decode($json_input, true);
if (json_last_error() !== JSON_ERROR_NONE) {
    send_json_response(['error' => true, 'message' => 'داده JSON نامعتبر است: ' . json_last_error_msg()], 400);
}

// آزادسازی حافظه
unset($json_input);

// تعریف فیلدهای اجباری و اختیاری
$required_fields = ['shipName', 'loadingWarehouse', 'cargoType', 'shippingCompany', 'loadingQuotaNumber', 'trackingNumber', 'username', 'userType'];
$optional_fields = ['entryTime', 'netWeight', 'scaleReceiptNumber', 'shortageWeight', 'excessWeight', 'exitTime', 'exitDate', 'status', 'confirmation', 'numberOfPeople', 'duplicateConfirmation'];

// بهینه‌سازی: استفاده از آرایه $params با پردازش مستقیم
$params = [];
foreach (array_merge($required_fields, $optional_fields) as $field) {
    $params[$field] = isset($data[$field]) ? sanitize_input($data[$field]) : '';
}

// آزادسازی حافظه
unset($data);

try {
    // اعتبارسنجی داده‌های درخواست
    $validation_errors = validate_request_data($params, $required_fields);
    if (!empty($validation_errors)) {
        throw new Exception(implode(" ", $validation_errors));
    }
    
    // اتصال به پایگاه داده با استفاده از persistent connection برای بهبود عملکرد
    $conn = new mysqli('p:'.DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);
    if ($conn->connect_error) {
        throw new Exception("خطا در اتصال به پایگاه داده: " . $conn->connect_error);
    }
    
    // تنظیم کاراکترست برای پشتیبانی از یونیکد
    $conn->set_charset("utf8mb4");
    
    // افزایش کارایی با غیرفعال کردن autocommit
    $conn->autocommit(FALSE);
    
    // پارامترهای کلیدی که باید حفظ شوند
    $critical_params = [
        'shipName' => $params['shipName'],
        'loadingWarehouse' => $params['loadingWarehouse'], 
        'cargoType' => $params['cargoType'], 
        'shippingCompany' => $params['shippingCompany'],
        'loadingQuotaNumber' => $params['loadingQuotaNumber'],
        'trackingNumber' => $params['trackingNumber']
    ];
    
    // بررسی وجود حواله با همین شماره در کل کشتی از ابتدای روز قبل تا الان
    $yesterdayStart = date('Y-m-d 00:00:00', strtotime('-1 day'));
    $now = date('Y-m-d H:i:s');
    

    
    $check24h_query = "SELECT loadingQuotaNumber, loadingWarehouse, shippingCompany, exitTime, exitDate, status, entryTime, updated_at FROM CargoInfo WHERE 
                      shipName = ? AND 
                      trackingNumber = ? AND 
                      updated_at BETWEEN ? AND ?
                      ORDER BY id DESC LIMIT 1";
    

    
    $check24h_stmt = $conn->prepare($check24h_query);
    if (!$check24h_stmt) {
        throw new Exception("خطا در آماده‌سازی دستور بررسی 24 ساعته: " . $conn->error);
    }
    
    $check24h_stmt->bind_param("ssss", 
        $critical_params['shipName'], 
        $critical_params['trackingNumber'],
        $yesterdayStart,
        $now
    );
    
    if (!$check24h_stmt->execute()) {
        throw new Exception("خطا در اجرای دستور بررسی 24 ساعته: " . $check24h_stmt->error);
    }
    
    $check24h_result = $check24h_stmt->get_result();
    $existing24hCargo = $check24h_result->fetch_assoc();
    $check24h_stmt->close();
    
    // اگر حواله در 24 ساعت گذشته ثبت شده باشد، هشدار ارسال کن
    if ($existing24hCargo && $existing24hCargo['loadingQuotaNumber'] != $critical_params['loadingQuotaNumber']) {
        // بررسی تأیید کاربر برای ثبت حواله تکراری
        if ($params['duplicateConfirmation'] != "proceed") {
            $warningMessage = "شماره حواله ({$params['trackingNumber']}) در 24 ساعت گذشته برای کشتی [ {$params['shipName']} ] قبلاً ثبت شده است:\n\n";
            $warningMessage .= "شماره کوتاژ ثبت شده: {$existing24hCargo['loadingQuotaNumber']}\n";
            $warningMessage .= "انبار ثبت شده: {$existing24hCargo['loadingWarehouse']}\n";
            if (!empty($existing24hCargo['shippingCompany'])) {
                $warningMessage .= "شرکت باربری: {$existing24hCargo['shippingCompany']}\n";
            }
            if (!empty($existing24hCargo['entryTime'])) {
                $warningMessage .= "ساعت ورود: {$existing24hCargo['entryTime']}\n";
            }
            if (!empty($existing24hCargo['exitTime'])) {
                $warningMessage .= "ساعت خروج: {$existing24hCargo['exitTime']}\n";
            }
            if (!empty($existing24hCargo['exitDate'])) {
                $warningMessage .= "تاریخ خروج: {$existing24hCargo['exitDate']}\n";
            }
            $warningMessage .= "وضعیت فعلی حواله: {$existing24hCargo['status']}";
            
            send_json_response([
                "warning" => true,
                "message" => $warningMessage,
                "existing_cargo" => $existing24hCargo,
                "requires_confirmation" => true
            ], 409);
        }
        // اگر کاربر تأیید کرده باشد، ادامه پردازش
    }
    
    // بررسی وجود حواله با شماره و کوتاژ مشخص - استفاده از کوئری بهینه‌تر با انتخاب فیلدهای مورد نیاز
    $query = "SELECT id, status, confirm, exitDate, exitTime FROM CargoInfo WHERE 
              shipName = ? AND 
              loadingWarehouse = ? AND 
              cargoType = ? AND 
              shippingCompany = ? AND 
              loadingQuotaNumber = ? AND 
              trackingNumber = ? 
              LIMIT 1";
    
    $stmt = $conn->prepare($query);
    if (!$stmt) {
        throw new Exception("خطا در آماده‌سازی دستور SQL: " . $conn->error);
    }
    
    $stmt->bind_param("ssssss", 
        $critical_params['shipName'], 
        $critical_params['loadingWarehouse'], 
        $critical_params['cargoType'], 
        $critical_params['shippingCompany'], 
        $critical_params['loadingQuotaNumber'], 
        $critical_params['trackingNumber']
    );
    
    if (!$stmt->execute()) {
        throw new Exception("خطا در اجرای دستور SQL: " . $stmt->error);
    }
    
    $result = $stmt->get_result();
    $existingCargo = $result->fetch_assoc();
    $stmt->close();
    
    // محاسبه ساعت فعلی یکبار برای استفاده مجدد
    $currentTime = jdate("H:i");
    $currentDate = jdate("Y/m/d");
    
    // منطق ثبت حواله جدید یا بروزرسانی حواله موجود
    if (!$existingCargo) {
        // اعتبارسنجی تعداد نفرات برای حواله‌های جدید
        if (empty($params['numberOfPeople']) || !is_numeric($params['numberOfPeople']) || $params['numberOfPeople'] < 1) {
            throw new Exception("تعداد نفرات باید عددی بزرگتر از صفر باشد");
        }

        // آماده‌سازی و اجرای دستور درج با استفاده از prepared statement
        $insert_query = "INSERT INTO CargoInfo (
            trackingNumber, entryTime, netWeight, scaleReceiptNumber, shortageWeight, excessWeight, 
            status, shipName, loadingWarehouse, cargoType, shippingCompany, loadingQuotaNumber, 
            numberOfPeople, username, userType
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        $insert_stmt = $conn->prepare($insert_query);
        if (!$insert_stmt) {
            throw new Exception("خطا در آماده‌سازی دستور درج: " . $conn->error);
        }
        
        $status = "ورود";
        
        $insert_stmt->bind_param("sssssssssssssss", 
            $params['trackingNumber'], 
            $currentTime, 
            $params['netWeight'], 
            $params['scaleReceiptNumber'], 
            $params['shortageWeight'], 
            $params['excessWeight'], 
            $status, 
            $critical_params['shipName'], 
            $critical_params['loadingWarehouse'], 
            $critical_params['cargoType'], 
            $critical_params['shippingCompany'], 
            $critical_params['loadingQuotaNumber'], 
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
            "message" => "حواله جدید با شماره {$params['trackingNumber']} و تعداد نفرات {$params['numberOfPeople']} در ساعت $currentTime توسط کاربر {$params['username']} با نقش {$params['userType']} با موفقیت ثبت شد"
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
                
                // آماده‌سازی و اجرای دستور بروزرسانی - با WHERE بهینه‌تر
                $update_query = "UPDATE CargoInfo SET 
                    netWeight = ?, 
                    scaleReceiptNumber = ?, 
                    exitTime = ?, 
                    exitDate = ?, 
                    status = 'خروج', 
                    username = ?, 
                    userType = ? 
                WHERE id = ?";
                
                $update_stmt = $conn->prepare($update_query);
                
                $update_stmt->bind_param("ssssssi", 
                    $params['netWeight'], 
                    $params['scaleReceiptNumber'], 
                    $currentTime, 
                    $currentDate, 
                    $params['username'], 
                    $params['userType'], 
                    $existingCargo['id']
                );
            } elseif (!empty($params['shortageWeight']) || !empty($params['excessWeight'])) {
                // بروزرسانی کسری/اضافه بار - با WHERE بهینه‌تر
                $update_query = "UPDATE CargoInfo SET 
                    shortageWeight = ?, 
                    excessWeight = ?, 
                    username = ?, 
                    userType = ? 
                WHERE id = ?";
                
                $update_stmt = $conn->prepare($update_query);
                
                $update_stmt->bind_param("ssssi", 
                    $params['shortageWeight'], 
                    $params['excessWeight'], 
                    $params['username'], 
                    $params['userType'], 
                    $existingCargo['id']
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
            
            // آماده‌سازی و اجرای دستور بروزرسانی - با WHERE بهینه‌تر
            $update_query = "UPDATE CargoInfo SET 
                netWeight = ?, 
                scaleReceiptNumber = ?, 
                exitTime = ?, 
                exitDate = ?, 
                username = ?, 
                userType = ? 
            WHERE id = ?";
            
            $update_stmt = $conn->prepare($update_query);
            
            $update_stmt->bind_param("ssssssi", 
                $params['netWeight'], 
                $params['scaleReceiptNumber'], 
                $currentTime, 
                $currentDate, 
                $params['username'], 
                $params['userType'], 
                $existingCargo['id']
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
            "exitDate" => $currentDate,
            "exitTime" => $currentTime,
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