<?php
header('Content-Type: application/json; charset=utf-8');
error_reporting(E_ALL);
ini_set('display_errors', 0);

// تنظیم منطقه زمانی ایران
date_default_timezone_set('Asia/Tehran');

require_once __DIR__ . '/config/config.php';
require_once __DIR__ . '/jdf.php';

// تنظیمات عملکرد PHP برای بهینه‌سازی بیشتر
ini_set('memory_limit', '64M'); // کاهش حد حافظه
ini_set('max_execution_time', 15); // کاهش زمان اجرا
ini_set('zlib.output_compression', 'On');
ini_set('output_buffering', 8192); // افزایش بافر خروجی
ini_set('opcache.enable', 1); // فعال‌سازی OPcache
ini_set('opcache.memory_consumption', 64); // تنظیم حافظه OPcache

/**
 * پاک‌سازی بهینه داده‌های ورودی
 */
function sanitize_input($input) {
    if (is_array($input)) {
        return array_map('sanitize_input', $input);
    }
    // استفاده از filter_var برای عملکرد بهتر
    return filter_var(trim($input), FILTER_SANITIZE_STRING, FILTER_FLAG_NO_ENCODE_QUOTES);
}

/**
 * ارسال پاسخ JSON بهینه
 */
function send_json_response($data, $status_code = 200) {
    http_response_code($status_code);
    // استفاده از فلگ‌های بهینه برای JSON
    echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_THROW_ON_ERROR);
    exit();
}

/**
 * اعتبارسنجی بهینه داده‌های خروج
 */
function validateExitData($netWeight, $scaleReceiptNumber) {
    // بررسی وزن با استفاده از filter_var
    if (!filter_var($netWeight, FILTER_VALIDATE_FLOAT) || $netWeight <= 0) {
        throw new Exception("وزن خالص باید عددی مثبت و بزرگتر از صفر باشد.");
    }
    
    if (empty($scaleReceiptNumber)) {
        throw new Exception("شماره قبض باسکول نمی‌تواند خالی باشد.");
    }
    
    $len = strlen($scaleReceiptNumber);
    if ($len < 8 || $len > 10) {
        throw new Exception("شماره قبض باسکول باید بین 8 تا 10 رقم باشد.");
    }
    
    // استفاده از ctype_digit برای عملکرد بهتر
    if (!ctype_digit($scaleReceiptNumber)) {
        throw new Exception("شماره قبض باسکول باید فقط شامل اعداد باشد.");
    }
}

/**
 * اعتبارسنجی بهینه داده‌های ورودی
 */
function validate_request_data($params, $required_fields) {
    $errors = [];
    
    foreach ($required_fields as $field) {
        if (!isset($params[$field]) || trim($params[$field]) === '') {
            $errors[] = "پارامتر $field الزامی است.";
        }
    }
    
    return $errors;
}

// دریافت و تجزیه بهینه داده‌های JSON ورودی
$json_input = file_get_contents("php://input");
try {
    $data = json_decode($json_input, true, 512, JSON_THROW_ON_ERROR);
} catch (JsonException $e) {
    send_json_response(['error' => true, 'message' => 'داده JSON نامعتبر است: ' . $e->getMessage()], 400);
}

// آزادسازی حافظه
unset($json_input);

// تعریف فیلدهای اجباری و اختیاری - استفاده از const برای بهینه‌سازی
const REQUIRED_FIELDS = ['shipName', 'loadingWarehouse', 'cargoType', 'shippingCompany', 'loadingQuotaNumber', 'trackingNumber', 'username', 'userType'];
const OPTIONAL_FIELDS = ['entryTime', 'netWeight', 'scaleReceiptNumber', 'shortageWeight', 'excessWeight', 'exitTime', 'exitDate', 'status', 'confirmation', 'numberOfPeople', 'duplicateConfirmation'];

// بهینه‌سازی: پردازش مستقیم با array_intersect_key
$all_fields = array_merge(REQUIRED_FIELDS, OPTIONAL_FIELDS);
$params = array_intersect_key($data, array_flip($all_fields));

// پاک‌سازی داده‌ها
$params = array_map('sanitize_input', $params);

// تنظیم مقادیر پیش‌فرض برای فیلدهای اختیاری
foreach (OPTIONAL_FIELDS as $field) {
    if (!isset($params[$field])) {
        $params[$field] = '';
    }
}

// آزادسازی حافظه
unset($data, $all_fields);

try {
    // اعتبارسنجی بهینه داده‌های درخواست
    $validation_errors = validate_request_data($params, REQUIRED_FIELDS);
    if ($validation_errors) {
        throw new Exception(implode(" ", $validation_errors));
    }
    
    // اتصال بهینه به پایگاه داده
    $conn = new mysqli('p:'.DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);
    if ($conn->connect_error) {
        throw new Exception("خطا در اتصال به پایگاه داده: " . $conn->connect_error);
    }
    
    // تنظیمات بهینه دیتابیس
    $conn->set_charset("utf8mb4");
    $conn->autocommit(FALSE);
    $conn->query("SET SESSION sql_mode = 'STRICT_TRANS_TABLES'"); // حالت سخت‌گیرانه
    $conn->query("SET SESSION innodb_lock_wait_timeout = 5"); // کاهش زمان انتظار قفل
    
    // محاسبه زمان‌های مورد نیاز یکبار
    $yesterdayStart = date('Y-m-d 00:00:00', strtotime('-1 day'));
    $currentTime = jdate("H:i");
    $currentDate = jdate("Y/m/d");
    
    // پارامترهای کلیدی - استفاده از reference برای بهینه‌سازی حافظه
    $shipName = &$params['shipName'];
    $trackingNumber = &$params['trackingNumber'];
    $loadingQuotaNumber = &$params['loadingQuotaNumber'];
    
    // بررسی بهینه وجود حواله تکراری در 24 ساعت گذشته
    $check24h_query = "SELECT loadingQuotaNumber, loadingWarehouse, shippingCompany, exitTime, exitDate, status, entryTime 
                      FROM CargoInfo 
                      WHERE shipName = ? AND trackingNumber = ? AND updated_at >= ? 
                      ORDER BY id DESC LIMIT 1";
    
    $check24h_stmt = $conn->prepare($check24h_query);
    if (!$check24h_stmt) {
        throw new Exception("خطا در آماده‌سازی دستور بررسی 24 ساعته: " . $conn->error);
    }
    
    $check24h_stmt->bind_param("sss", $shipName, $trackingNumber, $yesterdayStart);
    
    if (!$check24h_stmt->execute()) {
        throw new Exception("خطا در اجرای دستور بررسی 24 ساعته: " . $check24h_stmt->error);
    }
    
    $check24h_result = $check24h_stmt->get_result();
    $existing24hCargo = $check24h_result->fetch_assoc();
    $check24h_stmt->close();
    unset($check24h_result);
    
    // بررسی بهینه حواله تکراری
    if ($existing24hCargo && $existing24hCargo['loadingQuotaNumber'] !== $loadingQuotaNumber) {
        // بررسی تأیید کاربر برای ثبت حواله تکراری
        if ($params['duplicateConfirmation'] !== "proceed") {
            // ساخت پیام هشدار بهینه
            $warningParts = [
                "شماره حواله ({$trackingNumber}) در 24 ساعت گذشته برای کشتی [ {$shipName} ] قبلاً ثبت شده است:\n\n",
                "شماره کوتاژ ثبت شده: {$existing24hCargo['loadingQuotaNumber']}\n",
                "انبار ثبت شده: {$existing24hCargo['loadingWarehouse']}\n"
            ];
            
            // اضافه کردن اطلاعات اختیاری
            $optionalFields = [
                'shippingCompany' => 'شرکت باربری',
                'entryTime' => 'ساعت ورود',
                'exitTime' => 'ساعت خروج',
                'exitDate' => 'تاریخ خروج'
            ];
            
            foreach ($optionalFields as $field => $label) {
                if (!empty($existing24hCargo[$field])) {
                    $warningParts[] = "$label: {$existing24hCargo[$field]}\n";
                }
            }
            
            $warningParts[] = "وضعیت فعلی حواله: {$existing24hCargo['status']}";
            
            send_json_response([
                "warning" => true,
                "message" => implode('', $warningParts),
                "existing_cargo" => $existing24hCargo,
                "requires_confirmation" => true
            ], 409);
        }
    }
    
    // بررسی بهینه وجود حواله با کلیدهای اصلی
    $query = "SELECT id, status, confirm, exitDate, exitTime FROM CargoInfo WHERE 
              shipName = ? AND loadingWarehouse = ? AND cargoType = ? AND 
              shippingCompany = ? AND loadingQuotaNumber = ? AND trackingNumber = ? 
              LIMIT 1";
    
    $stmt = $conn->prepare($query);
    if (!$stmt) {
        throw new Exception("خطا در آماده‌سازی دستور SQL: " . $conn->error);
    }
    
    $stmt->bind_param("ssssss", 
        $shipName, 
        $params['loadingWarehouse'], 
        $params['cargoType'], 
        $params['shippingCompany'], 
        $loadingQuotaNumber, 
        $trackingNumber
    );
    
    if (!$stmt->execute()) {
        throw new Exception("خطا در اجرای دستور SQL: " . $stmt->error);
    }
    
    $result = $stmt->get_result();
    $existingCargo = $result->fetch_assoc();
    $stmt->close();
    unset($result);
    
    // منطق بهینه ثبت حواله جدید یا بروزرسانی حواله موجود
    if (!$existingCargo) {
        // اعتبارسنجی بهینه تعداد نفرات
        $numberOfPeople = filter_var($params['numberOfPeople'], FILTER_VALIDATE_INT);
        if ($numberOfPeople === false || $numberOfPeople < 1) {
            throw new Exception("تعداد نفرات باید عددی بزرگتر از صفر باشد");
        }

        // آماده‌سازی دستور درج بهینه
        $insert_query = "INSERT INTO CargoInfo (
            trackingNumber, entryTime, netWeight, scaleReceiptNumber, shortageWeight, excessWeight, 
            status, shipName, loadingWarehouse, cargoType, shippingCompany, loadingQuotaNumber, 
            numberOfPeople, username, userType
        ) VALUES (?, ?, ?, ?, ?, ?, 'ورود', ?, ?, ?, ?, ?, ?, ?, ?)";
        
        $insert_stmt = $conn->prepare($insert_query);
        if (!$insert_stmt) {
            throw new Exception("خطا در آماده‌سازی دستور درج: " . $conn->error);
        }
        
        $insert_stmt->bind_param("ssssssssssssss", 
            $trackingNumber, 
            $currentTime, 
            $params['netWeight'], 
            $params['scaleReceiptNumber'], 
            $params['shortageWeight'], 
            $params['excessWeight'], 
            $shipName, 
            $params['loadingWarehouse'], 
            $params['cargoType'], 
            $params['shippingCompany'], 
            $loadingQuotaNumber, 
            $numberOfPeople, 
            $params['username'], 
            $params['userType']
        );
        
        if (!$insert_stmt->execute()) {
            throw new Exception("خطا در اجرای دستور درج: " . $insert_stmt->error);
        }
        
        $insert_stmt->close();
        $conn->commit();
        
        // ارسال پاسخ بهینه
        send_json_response([
            "success" => true, 
            "message" => "حواله جدید با شماره {$trackingNumber} و تعداد نفرات {$numberOfPeople} در ساعت $currentTime توسط کاربر {$params['username']} با نقش {$params['userType']} با موفقیت ثبت شد"
        ]);
    } else {
        // منطق بهینه بروزرسانی حواله موجود
        $cargoId = $existingCargo['id'];
        
        if ($existingCargo['status'] === "ورود") {
            if (!empty($params['netWeight'])) {
                // خروج حواله - بررسی تأیید
                if ($existingCargo['confirm'] !== "تائید شده") {
                    throw new Exception("حواله مورد نظر توسط بارشمار هنوز تائید نشده است!");
                }
                
                // اعتبارسنجی داده‌های خروج
                validateExitData($params['netWeight'], $params['scaleReceiptNumber']);
                
                // بروزرسانی بهینه برای خروج
                $update_query = "UPDATE CargoInfo SET 
                    netWeight = ?, scaleReceiptNumber = ?, exitTime = ?, exitDate = ?, 
                    status = 'خروج', username = ?, userType = ? 
                WHERE id = ?";
                
                $update_stmt = $conn->prepare($update_query);
                $update_stmt->bind_param("ssssssi", 
                    $params['netWeight'], 
                    $params['scaleReceiptNumber'], 
                    $currentTime, 
                    $currentDate, 
                    $params['username'], 
                    $params['userType'], 
                    $cargoId
                );
            } elseif (!empty($params['shortageWeight']) || !empty($params['excessWeight'])) {
                // بروزرسانی بهینه کسری/اضافه بار
                $update_query = "UPDATE CargoInfo SET 
                    shortageWeight = ?, excessWeight = ?, username = ?, userType = ? 
                WHERE id = ?";
                
                $update_stmt = $conn->prepare($update_query);
                $update_stmt->bind_param("ssssi", 
                    $params['shortageWeight'], 
                    $params['excessWeight'], 
                    $params['username'], 
                    $params['userType'], 
                    $cargoId
                );
            } else {
                throw new Exception("برای حواله در وضعیت ورود، باید وزن خالص یا کسری/اضافه بار وارد شود");
            }
        } elseif ($existingCargo['status'] === "خروج") {
            // بروزرسانی حواله خروج شده - بررسی تأیید کاربر
            if ($params['confirmation'] !== "yes") {
                send_json_response([
                    "message" => "شماره حواله {$trackingNumber} در تاریخ {$existingCargo['exitDate']} و ساعت {$existingCargo['exitTime']} خروج کرده و سرویس بسته شده است!", 
                    "status" => "confirmation_needed", 
                    "exitDate" => $existingCargo['exitDate'], 
                    "exitTime" => $existingCargo['exitTime']
                ]);
            }
            
            // اعتبارسنجی داده‌های خروج
            validateExitData($params['netWeight'], $params['scaleReceiptNumber']);
            
            // بروزرسانی بهینه حواله خروج شده
            $update_query = "UPDATE CargoInfo SET 
                netWeight = ?, scaleReceiptNumber = ?, exitTime = ?, exitDate = ?, 
                username = ?, userType = ? 
            WHERE id = ?";
            
            $update_stmt = $conn->prepare($update_query);
            $update_stmt->bind_param("ssssssi", 
                $params['netWeight'], 
                $params['scaleReceiptNumber'], 
                $currentTime, 
                $currentDate, 
                $params['username'], 
                $params['userType'], 
                $cargoId
            );
        } else {
            throw new Exception("وضعیت نامعتبر حواله");
        }
        
        // اجرای بهینه دستور بروزرسانی
        if (!$update_stmt->execute()) {
            throw new Exception("خطا در اجرای دستور به‌روزرسانی: " . $update_stmt->error);
        }
        
        $update_stmt->close();
        $conn->commit();
        
        // ارسال پاسخ بهینه
        send_json_response([
            "success" => true,
            "message" => "عملیات با موفقیت انجام شد",
            "exitDate" => $currentDate,
            "exitTime" => $currentTime,
            "trackingNumber" => $trackingNumber,
            "loadingQuotaNumber" => $loadingQuotaNumber
        ]);
    }
} catch(Exception $e) {
    // مدیریت بهینه خطا
    if (isset($conn) && $conn instanceof mysqli) {
        $conn->rollback();
    }
    
    // ثبت خطا با اطلاعات بیشتر
    error_log(sprintf("[%s] خطا در saveOrUpdateCargoInfo.php: %s | IP: %s", 
        date('Y-m-d H:i:s'), 
        $e->getMessage(), 
        $_SERVER['REMOTE_ADDR'] ?? 'unknown'
    ));
    
    // ارسال پیام خطا
    send_json_response([
        'error' => true, 
        'message' => $e->getMessage()
    ], 500);
} finally {
    // آزادسازی بهینه منابع
    $resources = ['stmt', 'insert_stmt', 'update_stmt', 'check24h_stmt'];
    foreach ($resources as $resource) {
        if (isset($$resource) && $$resource instanceof mysqli_stmt) {
            $$resource->close();
        }
    }
    
    if (isset($conn) && $conn instanceof mysqli) {
        $conn->close();
    }
    
    // آزادسازی متغیرهای بزرگ
    unset($params, $existing24hCargo, $existingCargo);
}
?>