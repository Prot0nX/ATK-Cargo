<?php
//api.php

// تنظیم نوع محتوا به JSON و تنظیمات مربوط به خطاها
header('Content-Type: application/json; charset=UTF-8');
error_reporting(E_ALL);
ini_set('display_errors', 0);
ini_set('log_errors', 0);
ini_set('error_log', __DIR__ . '/error.log');

date_default_timezone_set('Asia/Tehran');

// تابع لاگ‌گیری
function logMessage($message, $type = 'INFO') {
    $logFile = __DIR__ . '/api_log.log';
    $timestamp = date('Y-m-d H:i:s');
    $logMessage = "[$timestamp] [$type] $message" . PHP_EOL;
    file_put_contents($logFile, $logMessage, FILE_APPEND);
}

// وارد کردن تنظیمات پایگاه داده
require_once __DIR__ . '/config/config.php';

// تابع برای ارسال پاسخ JSON
function sendJsonResponse($data, $statusCode = 200) {
    http_response_code($statusCode);
    echo json_encode($data);
    exit;
}

try {
    // بررسی روش درخواست
    if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
        throw new Exception('روش درخواست نامعتبر است');
    }

    // برقراری اتصال به پایگاه داده
    $conn = getDbConnection();


    // اگر پارامتر kotazh در درخواست وجود دارد
    if (isset($_GET['kotazh'])) {
        $kotazh = trim($_GET['kotazh']); // تمیز کردن ورودی
        if (empty($kotazh)) {
            throw new Exception('کوتاژ نمی‌تواند خالی باشد');
        }
        
        // اعتبارسنجی کوتاژ
        if (!preg_match('/^\d{8}$/', $kotazh)) {
            throw new Exception('فرمت کوتاژ نامعتبر است. باید شامل 8 رقم باشد.');
        }
        
        // آماده‌سازی پرس و جو برای استخراج اطلاعات اولیه کوتاژ
        $stmt = $conn->prepare("SELECT shipName, loadingWarehouse, cargoType, shippingCompany, cargoWeight, loadingQuotaNumber FROM InitialInfo WHERE loadingQuotaNumber = ?");
        if (!$stmt) {
            throw new Exception("آماده‌سازی پرس و جو ناموفق بود: " . $conn->error);
        }
        $stmt->bind_param("s", $kotazh);
        if (!$stmt->execute()) {
            throw new Exception("اجرای پرس و جو ناموفق بود: " . $stmt->error);
        }
        $result = $stmt->get_result();
        $kotazhInfo = $result->fetch_assoc();
        
        if ($kotazhInfo) {
            // آماده‌سازی پرس و جو برای استخراج اطلاعات بارگیری
            $stmt = $conn->prepare("SELECT trackingNumber, entryTime, netWeight, scaleReceiptNumber, shortageWeight, excessWeight, exitTime, exitDate, status FROM CargoInfo WHERE loadingQuotaNumber = ?");
            if (!$stmt) {
                throw new Exception("آماده‌سازی پرس و جو ناموفق بود: " . $conn->error);
            }
            $stmt->bind_param("s", $kotazh);
            if (!$stmt->execute()) {
                throw new Exception("اجرای پرس و جو ناموفق بود: " . $stmt->error);
            }
            $result = $stmt->get_result();
            $cargoInfo = $result->fetch_all(MYSQLI_ASSOC);
            
            // تهیه پاسخ نهایی شامل اطلاعات کوتاژ و اطلاعات بارگیری
	$response = [
		'kotazhInfo' => $kotazhInfo,
		'cargoInfo' => $cargoInfo
	];
	
	sendJsonResponse($response);
        } else {
           sendJsonResponse(['error' => 'کوتاژ مورد نظر یافت نشد'], 404);
        }
    } 
    // اگر پارامتر action با مقدار getRealTimeData در درخواست وجود دارد
    elseif (isset($_GET['action']) && $_GET['action'] === 'getRealTimeData') {
    // تعیین شیفت فعلی و محدوده زمانی
    $currentTime = date('H:i:s');
    $currentGregorianDate = date('Y-m-d');
    list($jY, $jM, $jD) = gregorianToJalali(date('Y'), date('m'), date('d'));
    $currentJalaliDate = sprintf('%04d/%02d/%02d', $jY, $jM, $jD);

    if ($currentTime >= '07:30:00' && $currentTime < '19:00:00') {
        // شیفت روز
        $shiftStartDate = $currentJalaliDate;
        $shiftEndDate = $currentJalaliDate;
        $shiftStartTime = '07:30:00';
        $shiftEndTime = '18:30:00';
        $shiftType = 'روز';
        
        $query = "
            SELECT 
                i.loadingQuotaNumber,
                i.shipName,
                i.loadingWarehouse,
                i.shippingCompany,
                COUNT(DISTINCT CASE WHEN c.status = 'ورود' THEN c.id END) AS entryVouchers,
                COUNT(DISTINCT CASE WHEN c.status = 'خروج' THEN c.id END) AS exitVouchers,
                COUNT(DISTINCT c.id) AS totalVouchers,
                SUM(CASE WHEN c.status = 'خروج' THEN c.netWeight ELSE 0 END) AS totalNetWeight
            FROM 
                InitialInfo i
            LEFT JOIN 
                CargoInfo c ON i.loadingQuotaNumber = c.loadingQuotaNumber
            WHERE 
                (c.exitDate = ? AND c.exitTime >= ? AND c.exitTime <= ?)
                OR (c.status = 'ورود' AND c.exitDate IS NULL)
            GROUP BY 
                i.loadingQuotaNumber, i.shipName, i.loadingWarehouse, i.shippingCompany
        ";
        
        $stmt = $conn->prepare($query);
        $stmt->bind_param("sss", $shiftStartDate, $shiftStartTime, $shiftEndTime);
    } else {
        // شیفت شب
        if ($currentTime >= '00:00:00' && $currentTime < '07:30:00') {
            // بعد از نیمه شب
            list($prevJY, $prevJM, $prevJD) = gregorianToJalali(date('Y', strtotime('-1 day')), date('m', strtotime('-1 day')), date('d', strtotime('-1 day')));
            $shiftStartDate = sprintf('%04d/%02d/%02d', $prevJY, $prevJM, $prevJD);
            $shiftEndDate = $currentJalaliDate;
        } else {
            // قبل از نیمه شب
            $shiftStartDate = $currentJalaliDate;
            list($nextJY, $nextJM, $nextJD) = gregorianToJalali(date('Y', strtotime('+1 day')), date('m', strtotime('+1 day')), date('d', strtotime('+1 day')));
            $shiftEndDate = sprintf('%04d/%02d/%02d', $nextJY, $nextJM, $nextJD);
        }
        $shiftStartTime = '19:00:00';
        $shiftEndTime = '07:00:00';
        $shiftType = 'شب';
        
        $query = "
            SELECT 
                i.loadingQuotaNumber,
                i.shipName,
                i.loadingWarehouse,
                i.shippingCompany,
                COUNT(DISTINCT CASE WHEN c.status = 'ورود' THEN c.id END) AS entryVouchers,
                COUNT(DISTINCT CASE WHEN c.status = 'خروج' THEN c.id END) AS exitVouchers,
                COUNT(DISTINCT c.id) AS totalVouchers,
                SUM(CASE WHEN c.status = 'خروج' THEN c.netWeight ELSE 0 END) AS totalNetWeight
            FROM 
                InitialInfo i
            LEFT JOIN 
                CargoInfo c ON i.loadingQuotaNumber = c.loadingQuotaNumber
            WHERE 
                (c.exitDate = ? AND c.exitTime >= ?) OR
                (c.exitDate = ? AND c.exitTime < ?) OR
                (c.status = 'ورود' AND c.exitDate IS NULL)
            GROUP BY 
                i.loadingQuotaNumber, i.shipName, i.loadingWarehouse, i.shippingCompany
        ";
        
        $stmt = $conn->prepare($query);
        $stmt->bind_param("ssss", $shiftStartDate, $shiftStartTime, $shiftEndDate, $shiftEndTime);
    }

    if (!$stmt->execute()) {
        throw new Exception("اجرای پرس و جو ناموفق بود: " . $stmt->error);
    }

    $result = $stmt->get_result();
    $realTimeData = $result->fetch_all(MYSQLI_ASSOC);

    // اضافه کردن اطلاعات شیفت به پاسخ
    $response = [
        'shiftInfo' => [
            'startDate' => $shiftStartDate,
            'endDate' => $shiftEndDate,
            'startTime' => $shiftStartTime,
            'endTime' => $shiftEndTime,
            'type' => $shiftType
        ],
        'data' => $realTimeData
    ];

    sendJsonResponse($response);
} else {
        throw new Exception('عملیات نامعتبر یا عدم تعیین عملیات');
    }
} catch (Exception $e) {
    sendJsonResponse(['error' => $e->getMessage()], 500);
}

// تابع تبدیل تاریخ میلادی به جلالی
function gregorianToJalali($gy, $gm, $gd) {
    $g_d_m = [0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334];
    $gy2 = ($gm > 2) ? ($gy + 1) : $gy;
    $days = 355666 + (365 * $gy) + ((int)(($gy2 + 3) / 4)) - ((int)(($gy2 + 99) / 100)) + ((int)(($gy2 + 399) / 400)) + $gd + $g_d_m[$gm - 1];
    $jy = -1595 + (33 * ((int)($days / 12053)));
    $days %= 12053;
    $jy += 4 * ((int)($days / 1461));
    $days %= 1461;
    if ($days > 365) {
        $jy += (int)(($days - 1) / 365);
        $days = ($days - 1) % 365;
    }
    if ($days < 186) {
        $jm = 1 + (int)($days / 31);
        $jd = 1 + ($days % 31);
    } else {
        $jm = 7 + (int)(($days - 186) / 30);
        $jd = 1 + (($days - 186) % 30);
    }
    return [$jy, $jm, $jd];
}
?>